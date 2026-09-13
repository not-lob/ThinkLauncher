package org.matiasdesu.thinklauncherv2.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves how far into a book the reader has gotten, for the Now Reading home widget.
 *
 * Primary source: KOReader's per-book sidecar file (<book>.sdr/metadata.<ext>.lua), which stores
 * `["percent_finished"] = 0.42` as plain Lua - read as text and regex-matched rather than parsed as
 * Lua, since that's all this needs. KOReader can also relocate sidecars into a central
 * `<koreader_root>/docsettings/` folder, so both locations are probed.
 *
 * Fallback: statistics.sqlite3's page_stat_data table, keyed by the book id already retained on
 * KOReaderHistoryHelper.BookItem. This table's schema was not verified on a real device for this
 * change (no KOReader install / adb available in this environment) - treat it as best-effort and
 * confirm the column names below against a real statistics.sqlite3 before relying on it.
 */
public final class KOReaderProgressHelper {

    private static final Pattern PERCENT_PATTERN =
            Pattern.compile("\\[\"percent_finished\"\\]\\s*=\\s*([0-9]*\\.?[0-9]+)");

    private KOReaderProgressHelper() {}

    public static Float resolveProgress(String koreaderRoot, KOReaderHistoryHelper.BookItem book) {
        Float fromSidecar = readSidecarProgress(koreaderRoot, book.path);
        if (fromSidecar != null) return fromSidecar;
        if (book.id >= 0) {
            return readStatsProgress(koreaderRoot, book.id);
        }
        return null;
    }

    private static Float readSidecarProgress(String koreaderRoot, String bookPath) {
        File book = new File(bookPath);
        String name = book.getName();
        int dot = name.lastIndexOf('.');
        String base = dot >= 0 ? name.substring(0, dot) : name;
        String ext = dot >= 0 ? name.substring(dot + 1) : "";

        List<File> candidates = new ArrayList<>();
        File dir = book.getParentFile();
        if (dir != null) {
            candidates.add(new File(dir, base + ".sdr/metadata." + ext + ".lua"));
        }
        if (koreaderRoot != null && !koreaderRoot.trim().isEmpty()) {
            candidates.add(new File(koreaderRoot, "docsettings/" + base + ".sdr/metadata." + ext + ".lua"));
        }

        for (File candidate : candidates) {
            Float progress = parseSidecar(candidate);
            if (progress != null) return progress;
        }
        return null;
    }

    private static Float parseSidecar(File file) {
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Matcher m = PERCENT_PATTERN.matcher(sb);
            if (m.find()) {
                float value = Float.parseFloat(m.group(1));
                return Math.max(0f, Math.min(1f, value));
            }
        } catch (Exception ignored) {
            // Missing/unreadable sidecar - fall through to the stats DB fallback.
        }
        return null;
    }

    private static Float readStatsProgress(String koreaderRoot, long bookId) {
        File statsDbFile = KOReaderHistoryHelper.findStatsDb(koreaderRoot);
        if (statsDbFile == null) return null;
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(statsDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            try (Cursor cursor = db.rawQuery(
                    "SELECT page, total_pages FROM page_stat_data WHERE id_book = ? ORDER BY start_time DESC LIMIT 1",
                    new String[] { String.valueOf(bookId) })) {
                if (cursor.moveToFirst()) {
                    int page = cursor.getInt(0);
                    int totalPages = cursor.getInt(1);
                    if (totalPages > 0) {
                        return Math.max(0f, Math.min(1f, page / (float) totalPages));
                    }
                }
            }
        } catch (Exception ignored) {
            // page_stat_data schema unverified - degrade to "no progress" rather than crash.
        } finally {
            if (db != null) db.close();
        }
        return null;
    }
}
