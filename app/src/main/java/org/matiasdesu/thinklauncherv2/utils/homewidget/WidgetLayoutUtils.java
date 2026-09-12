package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.view.Gravity;
import android.widget.RelativeLayout;

/**
 * Small shared helpers for home widgets, mirroring MainActivity's private
 * getHorizontalGravity/getRelativeHorizontalRule (0 = left, 1 = center, 2 = right) so widget
 * horizontal-position prefs behave exactly like the existing time/date ones.
 */
public final class WidgetLayoutUtils {
    private WidgetLayoutUtils() {}

    public static int horizontalGravity(int horizontal) {
        switch (horizontal) {
            case 0: return Gravity.LEFT;
            case 2: return Gravity.RIGHT;
            default: return Gravity.CENTER_HORIZONTAL;
        }
    }

    public static int relativeHorizontalRule(int horizontal) {
        switch (horizontal) {
            case 0: return RelativeLayout.ALIGN_PARENT_LEFT;
            case 2: return RelativeLayout.ALIGN_PARENT_RIGHT;
            default: return RelativeLayout.CENTER_HORIZONTAL;
        }
    }
}
