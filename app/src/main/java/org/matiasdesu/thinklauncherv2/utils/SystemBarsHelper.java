package org.matiasdesu.thinklauncherv2.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * Owns the launcher window's status-bar state: icon appearance (light vs. dark
 * icons for the current theme) and the optional "hide status bar" setting.
 *
 * Both live in one place on purpose: on pre-R the legacy path writes the whole
 * {@code systemUiVisibility} value at once, so setting the immersive flags
 * separately from the light-status-bar flag would silently clear one or the
 * other.
 */
public final class SystemBarsHelper {

    /** Int pref, 0 = show status bar (default), 1 = hide it. */
    public static final String KEY_HIDE_STATUS_BAR = "hide_status_bar";

    private SystemBarsHelper() {
    }

    public static boolean isHideStatusBar(Context context) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getInt(KEY_HIDE_STATUS_BAR, 0) == 1;
    }

    /**
     * Applies status-bar icon appearance and the hide-status-bar pref to this
     * activity's window. Safe to call repeatedly, and must be called after any
     * {@code setDecorFitsSystemWindows} call, which rewrites the legacy flags.
     */
    public static void apply(Activity activity, int theme) {
        Window window = activity.getWindow();
        boolean hide = isHideStatusBar(activity);
        boolean lightIcons = !ThemeUtils.isDarkTheme(theme, activity);

        applyWindowAttributes(window, hide);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        lightIcons ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                if (hide) {
                    controller.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    controller.hide(WindowInsets.Type.statusBars());
                } else {
                    controller.show(WindowInsets.Type.statusBars());
                }
            }
        } else {
            int flags = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lightIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (hide) {
                flags |= View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    /**
     * Window attributes that decide how far the window frame reaches. Hiding the
     * bar is not enough on its own:
     *
     * <ul>
     * <li>the frame is sized from {@code fitInsetsTypes}, so unless the system
     * bars are dropped from it the window keeps its old frame and leaves the
     * freed strip unpainted. Hiding therefore means going fully edge-to-edge,
     * and the caller feeds the navigation-bar inset back into the layout through
     * its insets listener.</li>
     * <li>on a device with a display cutout the window is still kept below the
     * cutout safe area, which leaves the same strip unpainted, so it also has to
     * opt into laying out behind the cutout.</li>
     * </ul>
     *
     * The fit types are only ever cleared, never set: this is the main window of
     * an activity that fills its task, and the WM rejects such a window asking to
     * fit any insets at all ("Illegal attributes: Main activity window that isn't
     * translucent trying to fit insets"), which kills the launcher at window-add
     * time. Showing the bar again is the caller's
     * {@code setDecorFitsSystemWindows(window, true)}, which pads the content
     * from the insets instead of shrinking the frame.
     */
    private static void applyWindowAttributes(Window window, boolean hide) {
        WindowManager.LayoutParams params = window.getAttributes();
        boolean changed = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hide
                && params.getFitInsetsTypes() != 0) {
            params.setFitInsetsTypes(0);
            changed = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int cutoutMode = hide
                    ? WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    : WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            if (params.layoutInDisplayCutoutMode != cutoutMode) {
                params.layoutInDisplayCutoutMode = cutoutMode;
                changed = true;
            }
        }

        if (changed) {
            window.setAttributes(params);
        }
    }
}
