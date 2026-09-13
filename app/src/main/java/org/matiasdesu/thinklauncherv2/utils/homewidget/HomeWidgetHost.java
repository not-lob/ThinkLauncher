package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.RelativeLayout;

import org.matiasdesu.thinklauncherv2.utils.EinkRefreshHelper;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the ordered list of pluggable {@link HomeWidget}s (status row, now reading, calendar) and
 * everything MainActivity needs to treat them as one unit: creating/chaining their views below the
 * clock/date stack, tearing them down on a layout rebuild, re-applying insets, and loading their
 * data off the main thread the same way the rest of the home screen is built (no ticking timers,
 * no WorkManager - see MainActivity's on-resume-only refresh model).
 */
public class HomeWidgetHost {

    private final Activity host;
    private final List<HomeWidget> widgets = new ArrayList<>();
    private final Map<String, View> createdViews = new LinkedHashMap<>();

    private HandlerThread bgThread;
    private Handler bgHandler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int lastPrefsSignature;
    private boolean hasSnapshot;

    public HomeWidgetHost(Activity host) {
        this.host = host;
        widgets.add(new StatusRowWidget());
        widgets.add(new NowReadingWidget());
        widgets.add(new CalendarWidget());
    }

    /**
     * Clears the previous build's views, ready for a fresh sequence of {@link #createOne} calls.
     * Callers building the whole home stack in a user-configured order (MainActivity's
     * home_stack_order) call this once, then createOne per slot in that order - unlike the old
     * single createAll() batch, widgets are no longer always built (and thus always positioned)
     * as one fixed-order group.
     */
    public void beginBuild() {
        createdViews.clear();
    }

    /**
     * Creates one widget's view (if enabled) and chains it below {@code anchorId} (or
     * ALIGN_PARENT_TOP if {@code anchorId} is View.NO_ID, i.e. it's the first thing in the stack),
     * with {@code spacingPx} as the gap when it's not first. Returns the new trailing view id, or
     * {@code anchorId} unchanged if the widget is disabled/unknown - so callers can thread the
     * return value straight into the next createOne/anchor without special-casing "nothing built".
     * Does not load data - callers trigger that once per build via {@link #loadAllAsync}.
     */
    public int createOne(String widgetId, RelativeLayout root, int anchorId, int spacingPx,
            SharedPreferences prefs, int bgColor, int textColor) {
        for (HomeWidget widget : widgets) {
            if (!widget.id().equals(widgetId)) continue;
            if (!widget.isEnabled(prefs)) return anchorId;
            View view = widget.createView(host, root, prefs, bgColor, textColor);
            if (view == null) return anchorId;
            if (view.getId() == View.NO_ID) {
                view.setId(View.generateViewId());
            }
            // createView already set a LayoutParams carrying the widget's configured horizontal
            // rule; addView(view, lp) below replaces it wholesale, so re-apply that rule here or a
            // widget set to CENTER/RIGHT silently renders left (and getVisibleHorizontalPositions
            // then tells MainActivity the app grid may sit on a side the stack isn't actually on).
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(WidgetLayoutUtils.relativeHorizontalRule(widget.horizontalPosition()));
            if (anchorId == View.NO_ID) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                lp.addRule(RelativeLayout.BELOW, anchorId);
                lp.topMargin = spacingPx;
            }
            root.addView(view, lp);
            createdViews.put(widget.id(), view);
            return view.getId();
        }
        return anchorId;
    }

    /** True if the last build actually created at least one widget view. */
    public boolean hasVisibleWidgets() {
        return !createdViews.isEmpty();
    }

    /** The configured horizontalPosition() of every currently built widget. */
    public List<Integer> getVisibleHorizontalPositions() {
        List<Integer> positions = new ArrayList<>();
        for (HomeWidget widget : widgets) {
            if (createdViews.containsKey(widget.id())) {
                positions.add(widget.horizontalPosition());
            }
        }
        return positions;
    }

    /** Every currently built widget's view, for callers that need to measure the whole stack. */
    public List<View> getVisibleViews() {
        return new ArrayList<>(createdViews.values());
    }

    /** Removes every currently-built widget view from root, ready for a fresh createAll. */
    public void teardown(RelativeLayout root) {
        for (View view : createdViews.values()) {
            root.removeView(view);
        }
        createdViews.clear();
    }

    public void applyInsets(int homePaddingLeftPx, int homePaddingRightPx) {
        for (HomeWidget widget : widgets) {
            if (createdViews.containsKey(widget.id())) {
                widget.applyInsets(homePaddingLeftPx, homePaddingRightPx);
            }
        }
    }

    /**
     * True if any pref that any widget declares via {@link HomeWidget#prefKeys()} - including
     * enablement - has changed since the last call. Folded into MainActivity's existing
     * layoutChanged OR-chain so toggling a widget (or one of its sub-options) triggers the same
     * recreateLayout() path as any other layout-affecting setting, with no per-widget diff code.
     */
    public boolean prefsChanged(SharedPreferences prefs) {
        int signature = computeSignature(prefs);
        boolean changed = !hasSnapshot || signature != lastPrefsSignature;
        lastPrefsSignature = signature;
        hasSnapshot = true;
        return changed;
    }

    /** Establishes the initial baseline without reporting a change; call once from onCreate. */
    public void snapshotPrefs(SharedPreferences prefs) {
        lastPrefsSignature = computeSignature(prefs);
        hasSnapshot = true;
    }

    /**
     * Global (not per-widget) prefs that affect how the whole home stack is built, e.g. widget
     * order and spacing - tracked here so a change to either also folds into MainActivity's
     * layoutChanged OR-chain via prefsChanged, with no hand-written diff code (see prefsChanged).
     */
    private static final String[] GLOBAL_PREF_KEYS = { "home_stack_order", "home_stack_spacing" };

    private int computeSignature(SharedPreferences prefs) {
        // One snapshot for the whole pass: getAll() builds and copies the entire pref map on every
        // call, and this runs once per onResume across ~40 keys.
        Map<String, ?> all = prefs.getAll();
        int signature = 0;
        for (HomeWidget widget : widgets) {
            signature = 31 * signature + Boolean.hashCode(widget.isEnabled(prefs));
            for (String key : widget.prefKeys()) {
                signature = 31 * signature + valueSignature(all, key);
            }
        }
        for (String key : GLOBAL_PREF_KEYS) {
            signature = 31 * signature + valueSignature(all, key);
        }
        return signature;
    }

    /**
     * Hashes one pref value, folding in whether the key is present at all. Most widget toggles
     * default to 1 and are stored as 0/1 ints, and Integer(0).hashCode() is 0 - the same value a
     * plain "missing key hashes to 0" scheme gives an absent key. So the very first time a user
     * turned an on-by-default option off, the signature didn't move, prefsChanged() reported no
     * change, and the home screen kept rendering the option as if it were still on until some
     * other widget pref forced a rebuild.
     */
    private static int valueSignature(Map<String, ?> all, String key) {
        Object value = all.get(key);
        return value == null ? 0 : 31 * (1 + value.hashCode());
    }

    /**
     * Loads every built widget's data on a single shared background thread and binds results back
     * on the main thread, firing one coalesced e-ink flash once every widget has bound - never one
     * flash per widget. Safe to call every onResume; widgets torn down mid-load are simply skipped.
     */
    public void loadAllAsync(Context ctx) {
        if (createdViews.isEmpty()) return;
        ensureBgThread();
        final Context appCtx = ctx.getApplicationContext();
        final SharedPreferences prefs = host.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final List<HomeWidget> toLoad = new ArrayList<>();
        for (HomeWidget widget : widgets) {
            if (createdViews.containsKey(widget.id())) toLoad.add(widget);
        }
        final int[] remaining = { toLoad.size() };
        if (remaining[0] == 0) return;

        for (HomeWidget widget : toLoad) {
            bgHandler.post(() -> {
                Object data;
                try {
                    data = widget.loadData(appCtx, prefs);
                } catch (Exception e) {
                    data = null;
                }
                final Object result = data;
                mainHandler.post(() -> {
                    View view = createdViews.get(widget.id());
                    if (view != null) {
                        widget.bind(result);
                        // bind() is what materialises the data-driven rows (calendar agenda lines,
                        // the now-reading title/author), and it lands long after MainActivity's
                        // onResume font pass has already walked the tree - so those rows would
                        // keep the system font while everything around them used the custom one.
                        FontHelper.applyToViewTree(host, view);
                    }
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        EinkRefreshHelper.refreshEink(host.getWindow(), prefs,
                                prefs.getInt("eink_refresh_delay", 100));
                    }
                });
            });
        }
    }

    private void ensureBgThread() {
        if (bgThread == null) {
            bgThread = new HandlerThread("home-widget-loader");
            bgThread.start();
            bgHandler = new Handler(bgThread.getLooper());
        }
    }

    /** Stops the background loader thread; call from onDestroy. */
    public void destroy() {
        if (bgThread != null) {
            bgThread.quitSafely();
            bgThread = null;
            bgHandler = null;
        }
    }
}
