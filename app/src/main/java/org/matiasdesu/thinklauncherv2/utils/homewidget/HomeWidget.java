package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * A pluggable, always-on-e-ink-friendly block that stacks below the clock/date views on the home
 * screen. See {@link HomeWidgetHost} for the container that creates, positions, and refreshes these.
 *
 * Lifecycle per home-screen build: {@link #createView} runs on the main thread and must return
 * immediately with placeholder/empty content so layout settles without blocking; {@link #loadData}
 * runs on a background thread and may do file/DB/provider work; {@link #bind} runs back on the main
 * thread once loadData returns, and should only update the view createView already built.
 */
public interface HomeWidget {

    /** Stable id used as a SharedPreferences key prefix and as the widget's registry key. */
    String id();

    /** Whether this widget should be built into the home screen at all right now. */
    boolean isEnabled(SharedPreferences prefs);

    /**
     * Every pref key that affects this widget's enablement, content or layout. Used by
     * {@link HomeWidgetHost#prefsChanged} to fold widget-pref changes into the launcher's existing
     * coarse-grained "layoutChanged -> recreateLayout()" pattern, so a new widget pref needs no
     * hand-written diff in MainActivity.onResume.
     */
    String[] prefKeys();

    /** Build the (empty/placeholder) view tree. Must not do I/O. */
    View createView(Activity host, RelativeLayout root, SharedPreferences prefs, int bgColor, int textColor);

    /** Background-thread data load. May return null (e.g. permission missing, nothing to show). */
    Object loadData(Context ctx, SharedPreferences prefs);

    /** Main-thread bind of the loaded data onto the view built by createView. */
    void bind(Object data);

    /** Re-apply left/right home padding after a padding/inset change. */
    void applyInsets(int homePaddingLeftPx, int homePaddingRightPx);

    /** The view most recently created by createView, or null if not currently built. */
    View getView();
}
