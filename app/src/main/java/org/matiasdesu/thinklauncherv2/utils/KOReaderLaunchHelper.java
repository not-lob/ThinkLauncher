package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Builds the ACTION_VIEW intent that opens a book file in KOReader. Extracted from
 * ui/KOReaderHistoryActivity.openBook so the KOReader history screen and the Now Reading home
 * widget share one implementation instead of two copies of the package/mime-type guessing.
 */
public final class KOReaderLaunchHelper {

    private KOReaderLaunchHelper() {}

    private static String mimeTypeFor(String lowerPath) {
        if (lowerPath.endsWith(".pdf")) return "application/pdf";
        if (lowerPath.endsWith(".epub")) return "application/epub+zip";
        if (lowerPath.endsWith(".mobi")) return "application/x-mobipocket-ebook";
        if (lowerPath.endsWith(".fb2")) return "application/x-fictionbook+xml";
        if (lowerPath.endsWith(".cbz")) return "application/x-cbz";
        return "*/*";
    }

    /** Builds the intent, resolving org.koreader.launcher then falling back to the F-Droid build. */
    public static Intent buildOpenIntent(Context ctx, String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeTypeFor(path.toLowerCase()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String pkg = "org.koreader.launcher";
        intent.setPackage(pkg);
        if (intent.resolveActivity(ctx.getPackageManager()) == null) {
            intent.setPackage("org.koreader.launcher.fdroid");
        }
        return intent;
    }

    /** Returns false if the file is missing or KOReader isn't installed / couldn't be launched. */
    public static boolean openBook(Context ctx, String path) {
        if (path == null) return false;
        File file = new File(path);
        if (!file.exists()) return false;
        try {
            ctx.startActivity(buildOpenIntent(ctx, path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
