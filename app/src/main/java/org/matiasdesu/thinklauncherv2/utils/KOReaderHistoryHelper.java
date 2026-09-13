package org.matiasdesu.thinklauncherv2.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KOReaderHistoryHelper {
    private static final String TAG = "KOReaderHistoryHelper";

    private static final String[] STATS_PATTERNS = {
            "settings/statistics.sqlite3",
            "settings/statistics.sqlite",
            "statistics.sqlite3",
            "statistics.sqlite",
            ".koreader/statistics.sqlite3",
            ".koreader/statistics.sqlite"
    };

    private static final String[] HISTORY_PATTERNS = {
            "history.sqlite",
            "settings/history.sqlite",
            ".koreader/history.sqlite"
    };

    public static class BookItem {
        public String title;
        public String author;
        public String path;
        public long lastOpen;
        /** book.id from statistics.sqlite3, or -1 when the book came from history.sqlite instead
         *  (that table has no numeric id), used to look up reading progress. */
        public long id = -1;
        /** 0f-1f fraction read, or null when no progress could be resolved for this book. */
        public Float progress;
        /** Path to a cached cover thumbnail on disk, or null when none is available. */
        public String coverCachePath;

        public BookItem(String title, String author, String path, long lastOpen) {
            this.title = title;
            this.author = author;
            this.path = path;
            this.lastOpen = lastOpen;
        }
    }

    public static List<BookItem> getRecentBooks(String customPath) {
        List<BookItem> books = new ArrayList<>();

        if (customPath == null || customPath.trim().isEmpty()) {
            return books;
        }

        String root = normalizeRoot(customPath);

        File statsDb = findStatsDb(root);
        if (statsDb != null) {
            File infoDb = findBookInfoDb(statsDb.getParentFile());
            books = fetchFromStatistics(statsDb, infoDb);
            if (!books.isEmpty())
                return books;
        }

        for (String pattern : HISTORY_PATTERNS) {
            File historyDb = new File(root, pattern);
            if (historyDb.exists()) {
                books = fetchFromHistoryDb(historyDb);
                if (!books.isEmpty())
                    return books;
            }
        }

        return books;
    }

    /** The most recently opened book, enriched with nothing beyond what getRecentBooks provides. */
    public static BookItem getCurrentBook(String customPath) {
        List<BookItem> books = getRecentBooks(customPath);
        return books.isEmpty() ? null : books.get(0);
    }

    private static String normalizeRoot(String customPath) {
        String root = customPath.trim();
        if (root.endsWith("/"))
            root = root.substring(0, root.length() - 1);
        return root;
    }

    /** Exposed so KOReaderProgressHelper can reuse the same statistics.sqlite3 probing logic. */
    public static File findStatsDb(String customPath) {
        if (customPath == null || customPath.trim().isEmpty()) return null;
        String root = normalizeRoot(customPath);
        for (String pattern : STATS_PATTERNS) {
            File f = new File(root, pattern);
            if (f.exists()) return f;
        }
        return null;
    }

    private static File findBookInfoDb(File statsDir) {
        if (statsDir == null) return null;
        File f1 = new File(statsDir, "bookinfo.sqlite3");
        File f2 = new File(statsDir, "bookinfo.sqlite");
        File f3 = new File(statsDir, "bookinfo_cache.sqlite3");
        if (f1.exists()) return f1;
        if (f2.exists()) return f2;
        if (f3.exists()) return f3;
        return null;
    }

    private static List<BookItem> fetchFromStatistics(File statsDbFile, File infoDbFile) {
        List<BookItem> books = new ArrayList<>();
        SQLiteDatabase statsDb = null;
        SQLiteDatabase infoDb = null;
        try {
            statsDb = SQLiteDatabase.openDatabase(statsDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

            String query = "SELECT id, title, authors, last_open FROM book ORDER BY last_open DESC LIMIT 50";
            Cursor cursor = statsDb.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                if (infoDbFile != null && infoDbFile.exists()) {
                    infoDb = SQLiteDatabase.openDatabase(infoDbFile.getAbsolutePath(), null,
                            SQLiteDatabase.OPEN_READONLY);
                }

                do {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String authors = cursor.getString(2);
                    long lastOpen = cursor.getLong(3);
                    String path = null;

                    if (infoDb != null) {
                        try {
                            Cursor infoCursor = infoDb.rawQuery(
                                    "SELECT directory, filename FROM bookinfo WHERE title = ? AND authors = ?",
                                    new String[] { title, authors });
                            if (infoCursor.moveToFirst()) {
                                path = infoCursor.getString(0) + "/" + infoCursor.getString(1);
                                if (path.startsWith("/storage/emulated/0/")) {

                                } else if (path.startsWith("/sdcard/")) {
                                    path = "/storage/emulated/0/" + path.substring(8);
                                } else if (!path.startsWith("/") && !path.contains("://")) {
                                    path = "/storage/emulated/0/" + path;
                                }
                            }
                            infoCursor.close();
                        } catch (Exception e) {
                            // Table 'bookinfo' not found or error in query
                        }
                    }

                    if (path != null) {
                        BookItem item = new BookItem(title, authors, path, lastOpen);
                        item.id = id;
                        books.add(item);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            // Error fetching from statistics
        } finally {
            if (statsDb != null)
                statsDb.close();
            if (infoDb != null)
                infoDb.close();
        }
        return books;
    }

    private static List<BookItem> fetchFromHistoryDb(File historyDbFile) {
        List<BookItem> books = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(historyDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = db.rawQuery("SELECT path, title, last_open FROM history ORDER BY last_open DESC LIMIT 50",
                    null);
            if (cursor.moveToFirst()) {
                do {
                    String path = cursor.getString(0);
                    String title = cursor.getString(1);
                    long lastOpen = cursor.getLong(2);
                    books.add(new BookItem(title, "", path, lastOpen));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
        return books;
    }
}
