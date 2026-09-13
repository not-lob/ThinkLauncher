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

    /**
     * The horizontal padding every item in the home stack - the clock/date block included - insets
     * its content by, so their text edges line up in a column instead of each item picking its own
     * value. The clock/date views historically used a raw 32 *pixel* padding, which is 16dp on the
     * density-2 devices this was tuned on but drifts on every other density; 16dp keeps those
     * devices pixel-identical and makes the rest match.
     */
    public static final int HORIZONTAL_PADDING_DP = 16;

    /** Matching vertical padding, likewise the dp equivalent of the clock/date block's old 5px. */
    public static final float VERTICAL_PADDING_DP = 2.5f;

    public static int horizontalPaddingPx(float density) {
        return Math.round(HORIZONTAL_PADDING_DP * density);
    }

    public static int verticalPaddingPx(float density) {
        return Math.round(VERTICAL_PADDING_DP * density);
    }

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
