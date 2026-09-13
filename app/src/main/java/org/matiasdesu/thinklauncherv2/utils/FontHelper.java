package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matiasdesu.thinklauncherv2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Owns the imported-font library and per-surface ("slot") font overrides, and applies them to
 * TextViews. One imported .ttf is the default font (unchanged {@code custom_font_file} pref, for
 * back-compat with existing installs); any other imported font can be set as a slot's override via
 * {@link #setSlotFontFile}, falling back to the default when a slot has none.
 *
 * A TextView styled via {@link #applyStyled}/{@link #applySlotToViewTree} is tagged with its slot id
 * ({@code R.id.font_slot_tag}), so the blanket re-styling pass every screen already runs on resume
 * ({@link #applyToViewTree}) re-resolves that slot's font instead of stomping it back to the
 * default - the walk repairs a view's font rather than merely skipping ones it already touched.
 */
public class FontHelper {

    // Slot ids - text surfaces that can override the default font. Kept in one place so
    // fontPrefKeys() (below) and every call site agree on the same fixed set.
    public static final String SLOT_TIME = "time";
    public static final String SLOT_DATE = "date";
    public static final String SLOT_CALENDAR_EVENT = "calendar_event";
    public static final String SLOT_STATUS_ROW = "status_row";
    public static final String SLOT_NOW_READING = "now_reading";
    public static final String SLOT_HOME_CALENDAR = "home_calendar";
    public static final String SLOT_APP_LIST = "app_list";
    public static final String SLOT_MUSIC_DOCK = "music_dock";

    private static final String[] ALL_SLOTS = {
            SLOT_TIME, SLOT_DATE, SLOT_CALENDAR_EVENT, SLOT_STATUS_ROW, SLOT_NOW_READING,
            SLOT_HOME_CALENDAR, SLOT_APP_LIST, SLOT_MUSIC_DOCK
    };

    private static final String FONT_PREF = "custom_font_file";
    private static final String LIBRARY_PREF = "font_library_json";
    private static final String SLOT_PREF_PREFIX = "font_slot_";
    private static final String FONT_PREFIX = "custom_font_";
    private static final String FONT_SUFFIX = ".ttf";

    private static final Map<String, Typeface> typefaceCache = new HashMap<>();
    private static final WeakHashMap<TextView, String> appliedFonts = new WeakHashMap<>();

    public static final class FontEntry {
        public final String file;
        public final String name;

        public FontEntry(String file, String name) {
            this.file = file;
            this.name = name;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    private static String slotKey(String slotId) {
        return SLOT_PREF_PREFIX + slotId;
    }

    /** Every pref key this class owns - default font, library, and every slot override. Fold into
     *  HomeWidgetHost.GLOBAL_PREF_KEYS so a font change is treated like any other layout change. */
    public static String[] fontPrefKeys() {
        String[] keys = new String[2 + ALL_SLOTS.length];
        keys[0] = FONT_PREF;
        keys[1] = LIBRARY_PREF;
        for (int i = 0; i < ALL_SLOTS.length; i++) {
            keys[2 + i] = slotKey(ALL_SLOTS[i]);
        }
        return keys;
    }

    public static boolean hasCustomFont(Context context) {
        return getCurrentFontFile(context) != null;
    }

    private static File getCurrentFontFile(Context context) {
        String fileName = prefs(context).getString(FONT_PREF, null);
        if (fileName == null) {
            return null;
        }
        File file = new File(context.getFilesDir(), fileName);
        return file.exists() ? file : null;
    }

    // ---- Library ----------------------------------------------------------------------------

    /**
     * Every imported font still on disk. Folds a pre-existing {@code custom_font_file} (from
     * before the library existed) in as an entry so upgrading users don't lose their font, and
     * drops any entry whose file has gone missing (e.g. cleared app storage) - either kind of
     * change is written back so it only happens once.
     */
    public static List<FontEntry> getLibrary(Context context) {
        SharedPreferences p = prefs(context);
        List<FontEntry> result = new ArrayList<>();
        boolean dirty = false;

        String json = p.getString(LIBRARY_PREF, null);
        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String file = o.optString("file", null);
                    if (file == null || file.isEmpty()) {
                        dirty = true;
                        continue;
                    }
                    File f = new File(context.getFilesDir(), file);
                    if (!f.exists()) {
                        dirty = true;
                        continue;
                    }
                    String name = o.optString("name", file);
                    result.add(new FontEntry(file, name));
                }
            } catch (JSONException e) {
                result.clear();
                dirty = true;
            }
        }

        String legacyFile = p.getString(FONT_PREF, null);
        if (legacyFile != null) {
            File f = new File(context.getFilesDir(), legacyFile);
            if (f.exists()) {
                boolean present = false;
                for (FontEntry entry : result) {
                    if (entry.file.equals(legacyFile)) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    result.add(0, new FontEntry(legacyFile, "Imported Font"));
                    dirty = true;
                }
            }
        }

        if (dirty) {
            saveLibrary(context, result);
        }
        return result;
    }

    private static void saveLibrary(Context context, List<FontEntry> library) {
        JSONArray arr = new JSONArray();
        for (FontEntry entry : library) {
            JSONObject o = new JSONObject();
            try {
                o.put("file", entry.file);
                o.put("name", entry.name);
            } catch (JSONException e) {
                continue;
            }
            arr.put(o);
        }
        prefs(context).edit().putString(LIBRARY_PREF, arr.toString()).apply();
    }

    /** Imports a new font into the library. Unlike the old single-slot saveFont, this never
     *  evicts an existing font - the library accumulates. The very first font ever imported is
     *  made the default automatically, matching the old "importing sets the font" behavior. */
    public static FontEntry addFont(Context context, InputStream in, String displayName) {
        String fileName = FONT_PREFIX + System.currentTimeMillis() + FONT_SUFFIX;
        if (!writeFontFile(context, in, fileName)) {
            return null;
        }
        String name = (displayName == null || displayName.trim().isEmpty()) ? fileName : displayName.trim();
        FontEntry entry = new FontEntry(fileName, name);

        List<FontEntry> library = getLibrary(context);
        library.add(entry);
        saveLibrary(context, library);

        if (getDefaultFontFile(context) == null) {
            setDefaultFontFile(context, fileName);
        }
        clearCache();
        return entry;
    }

    private static boolean writeFontFile(Context context, InputStream in, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        FileOutputStream fos = null;
        boolean written = false;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            written = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return written;
    }

    /** Deletes one font from the library and disk, clearing the default and any slot override
     *  that pointed at it - those surfaces fall back to whatever the default resolves to next. */
    public static void deleteFont(Context context, String file) {
        if (file == null) {
            return;
        }
        File f = new File(context.getFilesDir(), file);
        if (f.exists()) {
            f.delete();
        }

        List<FontEntry> library = getLibrary(context);
        Iterator<FontEntry> it = library.iterator();
        while (it.hasNext()) {
            if (it.next().file.equals(file)) {
                it.remove();
            }
        }
        saveLibrary(context, library);

        SharedPreferences.Editor editor = prefs(context).edit();
        if (file.equals(getDefaultFontFile(context))) {
            editor.remove(FONT_PREF);
        }
        for (String slot : ALL_SLOTS) {
            if (file.equals(getSlotFontFile(context, slot))) {
                editor.remove(slotKey(slot));
            }
        }
        editor.apply();
        clearCache();
    }

    /** Wipes every font file and every font-related pref (library, default, all overrides). Used
     *  by the "reset all config" flow. */
    public static void removeFont(Context context) {
        File dir = context.getFilesDir();
        File[] files = dir == null ? null : dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().startsWith(FONT_PREFIX) && f.getName().endsWith(FONT_SUFFIX)) {
                    f.delete();
                }
            }
        }
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.remove(FONT_PREF);
        editor.remove(LIBRARY_PREF);
        for (String slot : ALL_SLOTS) {
            editor.remove(slotKey(slot));
        }
        editor.apply();
        clearCache();
    }

    public static String getDefaultFontFile(Context context) {
        return prefs(context).getString(FONT_PREF, null);
    }

    public static void setDefaultFontFile(Context context, String file) {
        SharedPreferences.Editor editor = prefs(context).edit();
        if (file == null) {
            editor.remove(FONT_PREF);
        } else {
            editor.putString(FONT_PREF, file);
        }
        editor.apply();
        clearCache();
    }

    // ---- Per-slot overrides -------------------------------------------------------------------

    /** The slot's override filename, or null if it inherits the default font. */
    public static String getSlotFontFile(Context context, String slotId) {
        String file = prefs(context).getString(slotKey(slotId), null);
        return (file == null || file.isEmpty()) ? null : file;
    }

    public static void setSlotFontFile(Context context, String slotId, String file) {
        SharedPreferences.Editor editor = prefs(context).edit();
        String key = slotKey(slotId);
        if (file == null || file.isEmpty()) {
            editor.remove(key);
        } else {
            editor.putString(key, file);
        }
        editor.apply();
        clearCache();
    }

    private static File resolveFontFile(Context context, String slotId) {
        File override = resolveSlotOverrideFile(context, slotId);
        return override != null ? override : getCurrentFontFile(context);
    }

    /** The slot's override file, only if one is set AND still exists on disk - null otherwise
     *  (including when slotId itself is null, i.e. "the default font" has no override). */
    private static File resolveSlotOverrideFile(Context context, String slotId) {
        if (slotId == null) {
            return null;
        }
        String slotFile = getSlotFontFile(context, slotId);
        if (slotFile == null) {
            return null;
        }
        File f = new File(context.getFilesDir(), slotFile);
        return f.exists() ? f : null;
    }

    // ---- Typeface loading -----------------------------------------------------------------

    public static Typeface getTypeface(Context context) {
        return loadTypeface(getCurrentFontFile(context));
    }

    public static Typeface getTypefaceForSlot(Context context, String slotId) {
        return loadTypeface(resolveFontFile(context, slotId));
    }

    /** Loads one specific library file directly by name, bypassing default/slot resolution -
     *  used to validate a just-imported font before it is ever set as a default or override. */
    public static Typeface getTypefaceByFile(Context context, String file) {
        if (file == null) {
            return null;
        }
        File f = new File(context.getFilesDir(), file);
        return f.exists() ? loadTypeface(f) : null;
    }

    private static Typeface loadTypeface(File file) {
        if (file == null) {
            return null;
        }
        String key = fontKeyFor(file);
        Typeface cached = typefaceCache.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            Typeface typeface = Typeface.createFromFile(file);
            typefaceCache.put(key, typeface);
            return typeface;
        } catch (Exception e) {
            return null;
        }
    }

    private static String fontKeyFor(File file) {
        return file == null ? "" : file.getAbsolutePath() + "@" + file.lastModified();
    }

    public static void clearCache() {
        typefaceCache.clear();
        appliedFonts.clear();
    }

    // ---- Applying to views ------------------------------------------------------------------

    private static int currentStyle(TextView tv) {
        Typeface current = tv.getTypeface();
        boolean bold = current != null && current.isBold();
        boolean italic = current != null && current.isItalic();
        return (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
    }

    private static void setTypefaceWithStyle(TextView tv, Typeface custom, int style) {
        if (custom == null) {
            tv.setTypeface(null, style);
        } else if (style == Typeface.NORMAL) {
            // Typeface.create(custom, NORMAL) still asks Android to resolve a style variant,
            // which for a real weight family (e.g. importing both Inter-Regular.ttf and
            // Inter-Bold.ttf as separate slot overrides) risks swapping in the wrong member of
            // that family instead of just returning the face that was loaded. Setting it directly
            // guarantees the exact imported file is what renders.
            tv.setTypeface(custom);
        } else {
            tv.setTypeface(Typeface.create(custom, style));
        }
    }

    /** Applies the default font to one TextView, preserving its current bold/italic style. Does
     *  not touch any slot tag the view may carry - unrelated to the home-widget slot system, this
     *  is what settings-screen rows and list items use. */
    public static void applyFont(Context context, TextView tv) {
        if (tv == null) {
            return;
        }
        File file = getCurrentFontFile(context);
        int style = currentStyle(tv);
        setTypefaceWithStyle(tv, loadTypeface(file), style);
        appliedFonts.put(tv, fontKeyFor(file) + "|" + style);
    }

    /**
     * Applies slotId's font (or the default, if the slot has no override) to one TextView with an
     * explicit bold flag, and tags the view with slotId so a later {@link #applyToViewTree} pass
     * re-resolves the same slot rather than falling back to the default. This is the home-screen
     * equivalent of the old {@code setTypeface(null, boldText ? BOLD : NORMAL)} calls - it owns
     * both the face and the style, so a style-only change (e.g. a new font size) can no longer
     * silently drop the slot's font the way the old two-step setTypeface-then-applyToViewTree
     * sequence could.
     *
     * When the slot has an explicit override, {@code bold} is ignored and NORMAL is used instead:
     * the whole point of importing e.g. Inter-Bold.ttf for the clock is to get that exact weight,
     * and synthetically fake-bolding an already-bold face on top of it would defeat that. The
     * global bold toggle only applies to surfaces still inheriting the default font.
     */
    public static void applyStyled(Context context, TextView tv, String slotId, boolean bold) {
        if (tv == null) {
            return;
        }
        boolean overridden = resolveSlotOverrideFile(context, slotId) != null;
        File file = resolveFontFile(context, slotId);
        int style = (overridden || !bold) ? Typeface.NORMAL : Typeface.BOLD;
        setTypefaceWithStyle(tv, loadTypeface(file), style);
        tv.setTag(R.id.font_slot_tag, slotId);
        appliedFonts.put(tv, fontKeyFor(file) + "|" + style);
    }

    public static void applyToViewTree(Context context, View view) {
        if (view == null) {
            return;
        }
        applyToViewTreeInternal(context, view);
    }

    private static void applyToViewTreeInternal(Context context, View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            Object tag = tv.getTag(R.id.font_slot_tag);
            String slotId = (tag instanceof String) ? (String) tag : null;
            File file = resolveFontFile(context, slotId);
            int style = currentStyle(tv);
            String cacheValue = fontKeyFor(file) + "|" + style;
            if (!cacheValue.equals(appliedFonts.get(tv))) {
                setTypefaceWithStyle(tv, loadTypeface(file), style);
                appliedFonts.put(tv, cacheValue);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyToViewTreeInternal(context, vg.getChildAt(i));
            }
        }
    }

    /** Tags every TextView under view with slotId, then immediately applies that slot's font -
     *  used by HomeWidgetHost to font a pluggable widget's whole subtree by its widget id, both
     *  right after it's built and again after its async bind() adds data-driven rows. */
    public static void applySlotToViewTree(Context context, View view, String slotId) {
        if (view == null) {
            return;
        }
        tagViewTree(view, slotId);
        applyToViewTreeInternal(context, view);
    }

    private static void tagViewTree(View view, String slotId) {
        if (view instanceof TextView) {
            view.setTag(R.id.font_slot_tag, slotId);
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                tagViewTree(vg.getChildAt(i), slotId);
            }
        }
    }
}
