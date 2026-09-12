package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Resolves app icons from a third-party icon pack APK (ADW/Nova style).
 *
 * A pack maps launcher components to drawable names in its own appfilter.xml;
 * we parse that once per selected pack on a background thread and look icons up
 * by package name, which is all the launcher's home/dock model stores.
 */
public final class IconPackHelper {

    private static final String PREFS = "prefs";

    public static final String PREF_ICON_PACK = "icon_pack";
    public static final String PREF_ICON_PACK_TINT = "icon_pack_tint";
    public static final String PREF_CUSTOM_ICON = "custom_icon_";
    public static final String PREF_CUSTOM_ICON_PACK = "custom_icon_pack_";

    /**
     * Packs declare themselves under whichever launcher's convention they were
     * built for, either as a bare action or as a category on ACTION_MAIN, so
     * both forms of every known action are queried and the results deduped.
     */
    private static final String[] THEME_ACTIONS = {
            "org.adw.launcher.THEME",
            "com.anddoes.launcher.THEME",
            "com.novalauncher.THEME",
            "com.gau.go.launcherex.theme",
            "com.sonyericsson.home.ICON_PACK",
            "app.lawnchair.icons.THEME",
            "ch.deletescape.lawnchair.ICONPACK",
    };

    /**
     * Bounded wait so a warm home screen doesn't render un-themed then repaint.
     * Big packs parse far slower than this (Arcticons ships ~48k entries in a
     * 6MB appfilter), so a miss is expected on first load and the listener
     * below is what actually gets those icons on screen.
     */
    private static final long READY_TIMEOUT_MS = 150;

    public interface LoadListener {
        void onIconPackLoaded();
    }

    private static volatile LoadListener loadListener;

    public static void setLoadListener(LoadListener listener) {
        loadListener = listener;
    }

    private static final Object LOCK = new Object();

    private static volatile String loadedPack;
    private static volatile Map<String, String> componentToDrawable = Collections.emptyMap();
    private static volatile CountDownLatch readyLatch;

    private static final Map<String, String> PKG_TO_COMPONENT = new ConcurrentHashMap<>();
    private static final Map<String, String> RESOLVED_NAMES = new ConcurrentHashMap<>();
    private static final Map<String, Resources> RES_CACHE = new ConcurrentHashMap<>();

    private IconPackHelper() {
    }

    public static final class PackInfo {
        public final String packageName;
        public final String label;

        PackInfo(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    public static String getSelectedPack(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_ICON_PACK, "");
    }

    public static boolean isPackInstalled(Context context, String packPkg) {
        if (packPkg == null || packPkg.isEmpty()) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(packPkg, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<PackInfo> getAvailablePacks(Context context) {
        PackageManager pm = context.getPackageManager();
        LinkedHashMap<String, PackInfo> found = new LinkedHashMap<>();

        for (String action : THEME_ACTIONS) {
            collectPacks(pm, new Intent(action), found);
            collectPacks(pm, new Intent(Intent.ACTION_MAIN).addCategory(action), found);
        }

        List<PackInfo> packs = new ArrayList<>(found.values());
        Collections.sort(packs, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return packs;
    }

    private static void collectPacks(PackageManager pm, Intent intent, Map<String, PackInfo> found) {
        try {
            List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
            if (matches == null) {
                return;
            }
            for (ResolveInfo ri : matches) {
                String pkg = ri.activityInfo.packageName;
                if (found.containsKey(pkg)) {
                    continue;
                }
                CharSequence label = ri.loadLabel(pm);
                found.put(pkg, new PackInfo(pkg, label == null ? pkg : label.toString()));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Starts parsing the selected pack if it isn't already parsed or in flight.
     * Safe to call repeatedly; call it early in MainActivity so the parse
     * overlaps view inflation.
     */
    public static void ensureLoadedAsync(Context context) {
        final String pack = getSelectedPack(context);
        final Context appContext = context.getApplicationContext();

        synchronized (LOCK) {
            if (pack.equals(loadedPack) && readyLatch != null) {
                return;
            }

            loadedPack = pack;
            componentToDrawable = Collections.emptyMap();
            RESOLVED_NAMES.clear();

            final CountDownLatch latch = new CountDownLatch(1);
            readyLatch = latch;

            if (pack.isEmpty()) {
                latch.countDown();
                return;
            }

            Thread loader = new Thread(() -> {
                Map<String, String> parsed = parseAppFilter(appContext, pack);
                boolean current;
                synchronized (LOCK) {
                    current = readyLatch == latch;
                    if (current) {
                        componentToDrawable = parsed;
                    }
                }
                latch.countDown();

                LoadListener listener = loadListener;
                if (current && listener != null && !parsed.isEmpty()) {
                    listener.onIconPackLoaded();
                }
            }, "icon-pack-loader");
            loader.setPriority(Thread.NORM_PRIORITY - 1);
            loader.start();
        }
    }

    public static boolean awaitReady(long timeoutMs) {
        CountDownLatch latch = readyLatch;
        if (latch == null) {
            return false;
        }
        try {
            return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Drops all parsed and memoized state; call when the pack changes or is uninstalled. */
    public static void invalidate() {
        synchronized (LOCK) {
            loadedPack = null;
            readyLatch = null;
            componentToDrawable = Collections.emptyMap();
        }
        PKG_TO_COMPONENT.clear();
        RESOLVED_NAMES.clear();
        RES_CACHE.clear();
    }

    /**
     * The icon for an app, or null to fall back to the system icon. A per-app
     * override wins over the globally selected pack, and remembers which pack it
     * came from so it survives switching packs.
     */
    public static Drawable getIconForPackage(Context context, String appPkg) {
        if (appPkg == null || appPkg.isEmpty()) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        String override = prefs.getString(PREF_CUSTOM_ICON + appPkg, "");
        if (!override.isEmpty()) {
            String overridePack = prefs.getString(PREF_CUSTOM_ICON_PACK + appPkg, "");
            if (overridePack.isEmpty()) {
                overridePack = getSelectedPack(context);
            }
            Drawable overrideIcon = loadPackDrawable(context, overridePack, override);
            if (overrideIcon != null) {
                return overrideIcon;
            }
        }

        String pack = getSelectedPack(context);
        if (pack.isEmpty()) {
            return null;
        }

        ensureLoadedAsync(context);
        awaitReady(READY_TIMEOUT_MS);

        String name = lookupDrawableName(context, appPkg);
        if (name == null) {
            return null;
        }
        return loadPackDrawable(context, pack, name);
    }

    private static String lookupDrawableName(Context context, String appPkg) {
        Map<String, String> map = componentToDrawable;
        if (map.isEmpty()) {
            return null;
        }

        String cached = RESOLVED_NAMES.get(appPkg);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }

        String name = null;
        String componentKey = getComponentKey(context, appPkg);
        if (componentKey != null) {
            name = map.get(componentKey);
        }

        if (name == null) {
            // Appfilters routinely name an activity that has since been renamed,
            // and virtually always carry one entry per package, so a prefix hit
            // is a safe second chance.
            String prefix = appPkg + "/";
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    name = entry.getValue();
                    break;
                }
            }
        }

        RESOLVED_NAMES.put(appPkg, name == null ? "" : name);
        return name;
    }

    private static String getComponentKey(Context context, String appPkg) {
        String cached = PKG_TO_COMPONENT.get(appPkg);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }

        PackageManager pm = context.getPackageManager();
        String className = null;

        try {
            Intent launch = pm.getLaunchIntentForPackage(appPkg);
            if (launch != null && launch.getComponent() != null) {
                className = launch.getComponent().getClassName();
            }
        } catch (Exception ignored) {
        }

        if (className == null) {
            try {
                Intent main = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setPackage(appPkg);
                List<ResolveInfo> matches = pm.queryIntentActivities(main, 0);
                if (matches != null && !matches.isEmpty()) {
                    className = matches.get(0).activityInfo.name;
                }
            } catch (Exception ignored) {
            }
        }

        String key = className == null ? "" : appPkg + "/" + className;
        PKG_TO_COMPONENT.put(appPkg, key);
        return key.isEmpty() ? null : key;
    }

    public static Drawable loadPackDrawable(Context context, String packPkg, String drawableName) {
        if (packPkg == null || packPkg.isEmpty() || drawableName == null || drawableName.isEmpty()) {
            return null;
        }
        try {
            Resources res = getPackResources(context, packPkg);
            if (res == null) {
                return null;
            }
            int id = res.getIdentifier(drawableName, "drawable", packPkg);
            if (id == 0) {
                return null;
            }
            return res.getDrawable(id, null);
        } catch (Exception e) {
            return null;
        }
    }

    public static Resources getPackResources(Context context, String packPkg) {
        Resources cached = RES_CACHE.get(packPkg);
        if (cached != null) {
            return cached;
        }
        try {
            Resources res = context.getPackageManager().getResourcesForApplication(packPkg);
            RES_CACHE.put(packPkg, res);
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    /** Every drawable the pack offers, for the picker grid. */
    public static List<String> listPackDrawables(Context context, String packPkg) {
        List<String> names = parseDrawableList(context, packPkg, "drawable");
        if (names.isEmpty()) {
            names = parseDrawableList(context, packPkg, "icon_pack");
        }
        if (names.isEmpty()) {
            Map<String, String> map = packPkg.equals(loadedPack) && !componentToDrawable.isEmpty()
                    ? componentToDrawable
                    : parseAppFilter(context, packPkg);
            names = new ArrayList<>(new LinkedHashSet<>(map.values()));
        }
        return names;
    }

    private static Map<String, String> parseAppFilter(Context context, String packPkg) {
        XmlParserHandle handle = openPackXml(context, packPkg, "appfilter");
        if (handle == null) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new HashMap<>();
        try {
            XmlPullParser parser = handle.parser;
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    String component = parser.getAttributeValue(null, "component");
                    String drawable = parser.getAttributeValue(null, "drawable");
                    if (component != null && drawable != null && !drawable.isEmpty()) {
                        String key = normalizeComponent(component);
                        if (key != null && !map.containsKey(key)) {
                            map.put(key, drawable);
                        }
                    }
                }
                event = parser.next();
            }
        } catch (Exception e) {
            return map.isEmpty() ? Collections.emptyMap() : map;
        } finally {
            handle.close();
        }
        return map;
    }

    private static List<String> parseDrawableList(Context context, String packPkg, String xmlName) {
        XmlParserHandle handle = openPackXml(context, packPkg, xmlName);
        if (handle == null) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            XmlPullParser parser = handle.parser;
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    String drawable = parser.getAttributeValue(null, "drawable");
                    if (drawable != null && !drawable.isEmpty()) {
                        names.add(drawable);
                    }
                }
                event = parser.next();
            }
        } catch (Exception ignored) {
        } finally {
            handle.close();
        }
        return new ArrayList<>(names);
    }

    /**
     * Packs ship their XML either as a raw asset or as a compiled res/xml
     * resource; both expose XmlPullParser, so callers get one parse loop.
     */
    private static XmlParserHandle openPackXml(Context context, String packPkg, String xmlName) {
        try {
            Context packContext = context.createPackageContext(packPkg, Context.CONTEXT_IGNORE_SECURITY);
            InputStream stream = packContext.getAssets().open(xmlName + ".xml");
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(stream, null);
            return new XmlParserHandle(parser, stream);
        } catch (Exception ignored) {
        }

        try {
            Resources res = getPackResources(context, packPkg);
            if (res != null) {
                int id = res.getIdentifier(xmlName, "xml", packPkg);
                if (id != 0) {
                    return new XmlParserHandle(res.getXml(id), null);
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String normalizeComponent(String component) {
        String value = component.trim();
        if (value.startsWith("ComponentInfo{")) {
            value = value.substring("ComponentInfo{".length());
        }
        if (value.endsWith("}")) {
            value = value.substring(0, value.length() - 1);
        }

        int slash = value.indexOf('/');
        if (slash <= 0 || slash == value.length() - 1) {
            return null;
        }

        String pkg = value.substring(0, slash);
        String className = value.substring(slash + 1);
        if (className.startsWith(".")) {
            className = pkg + className;
        }
        return pkg + "/" + className;
    }

    private static final class XmlParserHandle {
        final XmlPullParser parser;
        private final InputStream stream;

        XmlParserHandle(XmlPullParser parser, InputStream stream) {
            this.parser = parser;
            this.stream = stream;
        }

        void close() {
            try {
                if (parser instanceof XmlResourceParser) {
                    ((XmlResourceParser) parser).close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
