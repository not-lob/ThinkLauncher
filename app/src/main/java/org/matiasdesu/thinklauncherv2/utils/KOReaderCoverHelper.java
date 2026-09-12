package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Best-effort book cover extraction for the Now Reading widget.
 *
 * KOReader's own bookinfo.sqlite3 stores covers as a raw BlitBuffer (cover_bb_data / cover_w /
 * cover_h / cover_bb_type), not a JPEG/PNG - there is no BitmapFactory path for that, and decoding
 * it correctly would mean hand-writing a pixel-format decoder per BlitBuffer type. Instead, for
 * EPUB books (a zip archive), the cover is pulled straight out of the book file: parse
 * META-INF/container.xml for the OPF path, parse the OPF for the cover image (EPUB3
 * properties="cover-image", or the older EPUB2 meta name="cover" + manifest item lookup), then
 * decode that entry directly with BitmapFactory.
 *
 * Every other format (PDF, CBZ, MOBI, FB2) - and any EPUB whose cover can't be resolved - simply
 * yields no cover. That is treated as normal, not an error: callers must degrade gracefully.
 *
 * Results are cached to disk (filesDir/koreader_covers) keyed by path + last-modified time, so the
 * zip/XML/bitmap work happens once per book, never on the launcher's cold-start path.
 */
public final class KOReaderCoverHelper {

    private static final int MAX_DIMENSION_PX = 300;
    private static final String CACHE_DIR_NAME = "koreader_covers";

    private KOReaderCoverHelper() {}

    /** Returns an absolute path to a cached cover thumbnail, or null if none could be extracted. */
    public static String getOrExtractCover(Context ctx, KOReaderHistoryHelper.BookItem book) {
        if (book.path == null || !book.path.toLowerCase().endsWith(".epub")) return null;
        File source = new File(book.path);
        if (!source.exists()) return null;

        File cacheDir = new File(ctx.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) return null;

        String cacheName = Math.abs(book.path.hashCode()) + "_" + source.lastModified() + ".png";
        File cacheFile = new File(cacheDir, cacheName);
        if (cacheFile.exists()) return cacheFile.getAbsolutePath();

        Bitmap cover = extractEpubCover(source);
        if (cover == null) return null;

        try (FileOutputStream out = new FileOutputStream(cacheFile)) {
            cover.compress(Bitmap.CompressFormat.PNG, 90, out);
            return cacheFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap extractEpubCover(File epubFile) {
        try (ZipFile zip = new ZipFile(epubFile)) {
            ZipEntry containerEntry = zip.getEntry("META-INF/container.xml");
            if (containerEntry == null) return null;
            String opfPath;
            try (InputStream in = zip.getInputStream(containerEntry)) {
                opfPath = parseOpfPath(in);
            }
            if (opfPath == null) return null;

            ZipEntry opfEntry = zip.getEntry(opfPath);
            if (opfEntry == null) return null;
            String coverHref;
            try (InputStream in = zip.getInputStream(opfEntry)) {
                coverHref = parseCoverHref(in, opfPath);
            }
            if (coverHref == null) return null;

            ZipEntry coverEntry = zip.getEntry(coverHref);
            if (coverEntry == null) return null;
            try (InputStream in = zip.getInputStream(coverEntry)) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap == null ? null : scaleDown(bitmap);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseOpfPath(InputStream in) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && "rootfile".equals(parser.getName())) {
                String fullPath = parser.getAttributeValue(null, "full-path");
                if (fullPath != null) return fullPath;
            }
            eventType = parser.next();
        }
        return null;
    }

    private static String parseCoverHref(InputStream in, String opfPath) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        Map<String, String> idToHref = new HashMap<>();
        String coverItemId = null;
        String coverImageHref = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("item".equals(tag)) {
                    String id = parser.getAttributeValue(null, "id");
                    String href = parser.getAttributeValue(null, "href");
                    String properties = parser.getAttributeValue(null, "properties");
                    if (id != null && href != null) idToHref.put(id, href);
                    if (properties != null && properties.contains("cover-image")) coverImageHref = href;
                } else if ("meta".equals(tag)) {
                    String name = parser.getAttributeValue(null, "name");
                    String content = parser.getAttributeValue(null, "content");
                    if ("cover".equals(name)) coverItemId = content;
                }
            }
            eventType = parser.next();
        }

        String href = coverImageHref != null ? coverImageHref : idToHref.get(coverItemId);
        if (href == null) return null;

        int lastSlash = opfPath.lastIndexOf('/');
        String opfDir = lastSlash >= 0 ? opfPath.substring(0, lastSlash + 1) : "";
        return normalize(opfDir + href);
    }

    /** Collapses "./" and "../" segments so a cover referenced relative to the OPF resolves to a
     *  real zip-entry path. */
    private static String normalize(String path) {
        Deque<String> stack = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!stack.isEmpty()) stack.removeLast();
            } else {
                stack.addLast(part);
            }
        }
        return String.join("/", stack);
    }

    private static Bitmap scaleDown(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int maxDim = Math.max(w, h);
        if (maxDim <= MAX_DIMENSION_PX) return src;
        float scale = MAX_DIMENSION_PX / (float) maxDim;
        return Bitmap.createScaledBitmap(src, Math.round(w * scale), Math.round(h * scale), true);
    }
}
