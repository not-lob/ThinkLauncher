package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.LruCache;
import android.util.TypedValue;

public class DynamicIconHelper {

    /**
     * Processed-icon cache. Building a themed/shaped icon involves binder
     * calls ({@link PackageManager#getApplicationIcon}) plus drawable
     * processing; home, dock and app bar slots re-resolve the same package
     * constantly, so cache the final drawable per option combination.
     */
    private static final int ICON_CACHE_SIZE = 192;
    private static final LruCache<String, Drawable> ICON_CACHE = new LruCache<>(ICON_CACHE_SIZE);

    /**
     * Bumped whenever something outside the cache key can change an icon's
     * artwork: the selected icon pack, a per-app override being set or cleared,
     * or the pack being uninstalled. Baking that identity into the key instead
     * would still leave stale entries behind when an override is cleared.
     */
    private static volatile int CACHE_EPOCH = 0;

    public static void clearIconCache() {
        ICON_CACHE.evictAll();
    }

    public static void bumpCacheEpoch() {
        CACHE_EPOCH++;
        ICON_CACHE.evictAll();
    }

    /** Lets callers notice that icon artwork changed and repaint. */
    public static int getCacheEpoch() {
        return CACHE_EPOCH;
    }

    /**
     * Get the app icon, preferring adaptive/dynamic icons if available and enabled
     * 
     * @param context        Application context
     * @param packageName    Package name of the app
     * @param useDynamic     Whether to use dynamic icons (Material You themed
     *                       icons)
     * @param theme          Current theme (0 = light, 1 = dark)
     * @param iconBackground Whether to use background (true) or transparent (false)
     * @return The app icon drawable
     * @throws PackageManager.NameNotFoundException if package not found
     */
    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground) throws PackageManager.NameNotFoundException {
        return getAppIcon(context, packageName, useDynamic, theme, iconBackground, false, false,
                IconShapeHelper.SHAPE_SYSTEM, false);
    }

    /**
     * Get the app icon, preferring adaptive/dynamic icons if available and enabled
     * 
     * @param context        Application context
     * @param packageName    Package name of the app
     * @param useDynamic     Whether to use dynamic icons (Material You themed
     *                       icons)
     * @param theme          Current theme (0 = light, 1 = dark)
     * @param iconBackground Whether to use background (true) or transparent (false)
     * @param dynamicColors  Whether to use Android's Material You dynamic colors
     * @return The app icon drawable
     * @throws PackageManager.NameNotFoundException if package not found
     */
    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground, boolean dynamicColors) throws PackageManager.NameNotFoundException {
        return getAppIcon(context, packageName, useDynamic, theme, iconBackground, dynamicColors, false,
                IconShapeHelper.SHAPE_SYSTEM, false);
    }

    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground, boolean dynamicColors, int iconShape) throws PackageManager.NameNotFoundException {
        return getAppIcon(context, packageName, useDynamic, theme, iconBackground, dynamicColors, false, iconShape, false);
    }

    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground, boolean dynamicColors, boolean invertIconColors)
            throws PackageManager.NameNotFoundException {
        return getAppIcon(context, packageName, useDynamic, theme, iconBackground, dynamicColors, invertIconColors,
                IconShapeHelper.SHAPE_SYSTEM, false);
    }

    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground, boolean dynamicColors, boolean invertIconColors, int iconShape)
            throws PackageManager.NameNotFoundException {
        return getAppIcon(context, packageName, useDynamic, theme, iconBackground, dynamicColors, invertIconColors,
                iconShape, false);
    }

    public static Drawable getAppIcon(Context context, String packageName, boolean useDynamic, int theme,
            boolean iconBackground, boolean dynamicColors, boolean invertIconColors, int iconShape,
            boolean forceMonochromeFallback) throws PackageManager.NameNotFoundException {
        String key = CACHE_EPOCH + "|app|" + packageName + "|" + useDynamic + "|" + theme + "|" + iconBackground
                + "|" + dynamicColors + "|" + invertIconColors + "|" + iconShape
                + "|" + forceMonochromeFallback;
        Drawable cached = ICON_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        PackageManager pm = context.getPackageManager();

        Drawable icon = IconPackHelper.getIconForPackage(context, packageName);
        boolean fromPack = icon != null;

        if (icon == null) {
            icon = pm.getApplicationIcon(packageName);
        }

        if (fromPack) {
            int[] colors = getDynamicColors(context, theme, iconBackground, invertIconColors, dynamicColors);

            // Pack artwork is usually a flat bitmap, so it would sail past the
            // adaptive-icon branch below untinted - and line-art packs are white
            // on transparent, i.e. invisible on a white e-ink background.
            boolean tintPackIcon = useDynamic || !iconBackground
                    || context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            .getBoolean(IconPackHelper.PREF_ICON_PACK_TINT, true);

            Drawable packResult = tintPackIcon
                    ? finishTintedIcon(context, createTintedDrawable(context, icon, colors[0]), icon,
                            colors[1], iconBackground, iconShape)
                    : applyShapeIfNeeded(context, icon, iconShape, iconBackground);

            ICON_CACHE.put(key, packResult);
            return packResult;
        }

        if (useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (icon instanceof AdaptiveIconDrawable) {
                AdaptiveIconDrawable adaptiveIcon = (AdaptiveIconDrawable) icon;

                int[] colors = getDynamicColors(context, theme, iconBackground, invertIconColors, dynamicColors);
                int iconColor = colors[0];
                int backgroundColor = colors[1];

                Drawable tintedIcon = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Drawable monochromeDrawable = adaptiveIcon.getMonochrome();
                    if (monochromeDrawable != null) {
                        tintedIcon = createTintedDrawable(context, monochromeDrawable, iconColor);
                    }
                }

                if (tintedIcon == null
                        && (forceMonochromeFallback || !iconBackground)) {
                    // Without an icon background we want every app to render as its tinted
                    // glyph (like the launcher's own system icons) so the shadow/outline has
                    // the same visual weight on both. When a monochrome layer is missing, fall
                    // back to the adaptive foreground even if the user hasn't enabled the
                    // dedicated "force monochrome fallback" option; otherwise the full opaque
                    // adaptive tile is shown and its outline/shadow read much finer.
                    Drawable foreground = adaptiveIcon.getForeground();
                    if (foreground != null) {
                        tintedIcon = createTintedDrawable(context, foreground, iconColor);
                    }
                }

                if (tintedIcon != null) {
                    Drawable finished = finishTintedIcon(context, tintedIcon, icon, backgroundColor,
                            iconBackground, iconShape);
                    ICON_CACHE.put(key, finished);
                    return finished;
                }
            }
        }

        Drawable result = applyShapeIfNeeded(context, icon, iconShape, iconBackground);
        ICON_CACHE.put(key, result);
        return result;
    }

    /**
     * Shared tail of the tinting pipeline: shape mask, or transparent-background
     * expansion, or a two-tone tile. Both the adaptive-icon path and the icon
     * pack path funnel through here so pack icons can't drift from stock ones.
     */
    private static Drawable finishTintedIcon(Context context, Drawable tintedIcon, Drawable original,
            int backgroundColor, boolean iconBackground, int iconShape) {
        if (iconShape != IconShapeHelper.SHAPE_SYSTEM && iconBackground) {
            int size = 108;
            Drawable shapedIcon = IconShapeHelper.createShapedIcon(context, tintedIcon,
                    backgroundColor, iconShape, size, false);
            if (shapedIcon != null) {
                return shapedIcon;
            }
        }

        if (!iconBackground) {
            float expandFraction = -0.35f;
            return new InsetDrawable(tintedIcon, expandFraction);
        }

        ColorDrawable themedBackground = new ColorDrawable(backgroundColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdaptiveIconDrawable twoToneIcon = new AdaptiveIconDrawable(themedBackground, tintedIcon);

            if (original.getIntrinsicWidth() > 0 && original.getIntrinsicHeight() > 0) {
                twoToneIcon.setBounds(0, 0, original.getIntrinsicWidth(), original.getIntrinsicHeight());
            }

            return twoToneIcon;
        }

        return new LayerDrawable(new Drawable[] { themedBackground, new InsetDrawable(tintedIcon, 0.25f) });
    }

    private static Drawable createTintedDrawable(Context context, Drawable source, int iconColor) {
        Drawable tinted;
        if (source.getConstantState() != null) {
            tinted = source.getConstantState().newDrawable(context.getResources()).mutate();
        } else {
            int w = Math.max(source.getIntrinsicWidth(), 1);
            int h = Math.max(source.getIntrinsicHeight(), 1);
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            source.setBounds(0, 0, w, h);
            source.draw(canvas);
            source.setBounds(0, 0, source.getIntrinsicWidth(), source.getIntrinsicHeight());
            tinted = new BitmapDrawable(context.getResources(), bitmap);
        }

        if (tinted.getIntrinsicWidth() > 0 && tinted.getIntrinsicHeight() > 0) {
            tinted.setBounds(0, 0, tinted.getIntrinsicWidth(), tinted.getIntrinsicHeight());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tinted.setColorFilter(new BlendModeColorFilter(iconColor, BlendMode.SRC_IN));
        } else {
            tinted.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        }

        tinted.setFilterBitmap(true);
        return tinted;
    }

    private static Drawable applyShapeIfNeeded(Context context, Drawable drawable, int iconShape,
            boolean iconBackground) {
        if (iconShape != IconShapeHelper.SHAPE_SYSTEM && iconBackground) {
            
            // Use 108 to match the size used for Dynamic Icons
            int size = 108;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
                AdaptiveIconDrawable adaptiveIcon = (AdaptiveIconDrawable) drawable;
                Drawable bg = adaptiveIcon.getBackground();
                Drawable fg = adaptiveIcon.getForeground();
                
                if (bg != null && fg != null) {
                    // Try to use the layers directly to avoid system mask
                    try {
                        Drawable shaped = IconShapeHelper.createShapedIcon(context, fg.mutate(), bg.mutate(), iconShape, size);
                        if (shaped != null) return shaped;
                    } catch (Exception e) {
                        // Fallback to standard masking if something goes wrong
                    }
                }
            }

            return IconShapeHelper.applyShapeMask(context, drawable, iconShape, size);
        }
        return drawable;
    }

    /**
     * Get Material You dynamic colors for icon and background
     * 
     * @param context        Application context
     * @param theme          Current theme (0 = light, 1 = dark, 2 = dynamic light,
     *                       3 = dynamic dark)
     * @param iconBackground Whether to use background
     * @return int array where [0] is icon color and [1] is background color
     */
    public static int[] getDynamicColors(Context context, int theme, boolean iconBackground) {
        return getDynamicColors(context, theme, iconBackground, false);
    }

    /**
     * Get Material You dynamic colors for icon and background
     * 
     * @param context          Application context
     * @param theme            Current theme (0 = light, 1 = dark, 2 = dynamic
     *                         light, 3 = dynamic dark)
     * @param iconBackground   Whether to use background
     * @param invertIconColors Whether to invert icon and background colors
     * @return int array where [0] is icon color and [1] is background color
     */
    public static int[] getDynamicColors(Context context, int theme, boolean iconBackground, boolean invertIconColors, boolean useMaterialYou) {
        int iconColor;
        int backgroundColor;
        boolean isDark = ThemeUtils.isDarkTheme(theme, context);

        if (context != null && theme == ThemeUtils.THEME_CUSTOM) {
            int customBg = ThemeUtils.getBgColor(theme, context);
            int customTx = ThemeUtils.getTextColor(theme, context);
            
            if (iconBackground) {
                backgroundColor = customTx; // Accent
                iconColor = customBg; // Bg
            } else {
                backgroundColor = customBg; 
                iconColor = customTx;
            }

            if (invertIconColors) {
                int temp = iconColor;
                iconColor = backgroundColor;
                backgroundColor = temp;
            }
            return new int[] { iconColor, backgroundColor };
        }

        if (useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (!isDark) {
                    if (iconBackground) {
                        iconColor = context.getResources().getColor(android.R.color.system_accent2_50,
                                context.getTheme());
                        backgroundColor = context.getResources().getColor(android.R.color.system_accent1_700,
                                context.getTheme());
                    } else {
                        iconColor = context.getResources().getColor(android.R.color.system_accent1_700,
                                context.getTheme());
                        backgroundColor = ThemeUtils.getBgColor(theme, context);
                    }
                } else {
                    if (iconBackground) {
                        iconColor = context.getResources().getColor(android.R.color.system_accent2_900,
                                context.getTheme());
                        backgroundColor = context.getResources().getColor(android.R.color.system_accent1_100,
                                context.getTheme());
                    } else {
                        iconColor = context.getResources().getColor(android.R.color.system_accent1_100,
                                context.getTheme());
                        backgroundColor = ThemeUtils.getBgColor(theme, context);
                    }
                }

                if (invertIconColors) {
                    int temp = iconColor;
                    iconColor = backgroundColor;
                    backgroundColor = temp;
                }

                return new int[] { iconColor, backgroundColor };
            } catch (Exception e) {
                // Fallback to default colors if dynamic colors are not available
            }
        }

        if (iconBackground) {
            iconColor = isDark ? Color.BLACK : Color.WHITE;
            backgroundColor = isDark ? Color.WHITE : Color.BLACK;
        } else {
            iconColor = isDark ? Color.WHITE : Color.BLACK;
            backgroundColor = ThemeUtils.getBgColor(theme, context);
        }

        if (invertIconColors) {
            int temp = iconColor;
            iconColor = backgroundColor;
            backgroundColor = temp;
        }

        return new int[] { iconColor, backgroundColor };
    }

    public static int[] getDynamicColors(Context context, int theme, boolean iconBackground, boolean invertIconColors) {
        return getDynamicColors(context, theme, iconBackground, invertIconColors, true);
    }

    private static int getThemedIconColor(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
                return typedValue.data;
            } catch (Exception e) {
                // Fallback
            }
        }
        return Color.parseColor("#1976D2"); // Material Blue
    }

    private static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * Check if an app has an adaptive icon with monochrome layer (supports dynamic
     * theming)
     * 
     * @param context     Application context
     * @param packageName Package name of the app
     * @return true if the app has a monochrome layer for dynamic theming
     */
    public static boolean hasMonochromeIcon(Context context, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                PackageManager pm = context.getPackageManager();
                Drawable icon = pm.getApplicationIcon(packageName);

                if (icon instanceof AdaptiveIconDrawable) {
                    AdaptiveIconDrawable adaptiveIcon = (AdaptiveIconDrawable) icon;
                    Drawable monochromeLayer = adaptiveIcon.getMonochrome();
                    return monochromeLayer != null;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Create a special icon (settings, search, notifications) with adaptive icon
     * background
     * This follows the same style as other dynamic icons with backgrounds
     * 
     * @param context        Application context
     * @param drawableResId  The drawable resource ID for the icon
     * @param theme          Current theme
     * @param iconBackground Whether to show background
     * @param dynamicColors  Whether to use dynamic colors
     * @return Drawable with appropriate styling
     */
    public static Drawable createSpecialIcon(Context context, int drawableResId, int theme, boolean iconBackground,
            boolean dynamicColors) {
        return createSpecialIcon(context, drawableResId, theme, iconBackground, dynamicColors, false,
                IconShapeHelper.SHAPE_SYSTEM);
    }

    public static Drawable createSpecialIcon(Context context, int drawableResId, int theme, boolean iconBackground,
            boolean dynamicColors, boolean invertIconColors) {
        return createSpecialIcon(context, drawableResId, theme, iconBackground, dynamicColors, invertIconColors,
                IconShapeHelper.SHAPE_SYSTEM);
    }

    /**
     * Create a special icon (settings, search, notifications) with adaptive icon
     * background
     * This follows the same style as other dynamic icons with backgrounds
     * 
     * @param context        Application context
     * @param drawableResId  The drawable resource ID for the icon
     * @param theme          Current theme
     * @param iconBackground Whether to show background
     * @param dynamicColors  Whether to use dynamic colors
     * @param iconShape      The shape to apply to the icon (from IconShapeHelper)
     * @return Drawable with appropriate styling
     */
    public static Drawable createSpecialIcon(Context context, int drawableResId, int theme, boolean iconBackground,
            boolean dynamicColors, boolean invertIconColors, int iconShape) {
        String key = CACHE_EPOCH + "|special|" + drawableResId + "|" + theme + "|" + iconBackground
                + "|" + dynamicColors + "|" + invertIconColors + "|" + iconShape;
        Drawable cached = ICON_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        Drawable iconDrawable = context.getResources().getDrawable(drawableResId, context.getTheme()).mutate();

        int[] colors = getDynamicColors(context, theme, iconBackground, invertIconColors, dynamicColors);
        int iconColor = colors[0];
        int backgroundColor = colors[1];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            iconDrawable.setColorFilter(new BlendModeColorFilter(iconColor, BlendMode.SRC_IN));
        } else {
            iconDrawable.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        }

        Drawable result;
        if (iconBackground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (iconShape != IconShapeHelper.SHAPE_SYSTEM) {
                int size = 108;
                Drawable shapedIcon = IconShapeHelper.createShapedIcon(context, iconDrawable, backgroundColor,
                        iconShape, size);
                if (shapedIcon != null) {
                    result = shapedIcon;
                    ICON_CACHE.put(key, result);
                    return result;
                }
            }

            ColorDrawable bgDrawable = new ColorDrawable(backgroundColor);

            float insetFraction = 0.30f;
            InsetDrawable insetIcon = new InsetDrawable(iconDrawable, insetFraction);

            result = new AdaptiveIconDrawable(bgDrawable, insetIcon);
        } else {
            float expandFraction = 0.15f;
            result = new InsetDrawable(iconDrawable, expandFraction);
        }

        ICON_CACHE.put(key, result);
        return result;
    }
}
