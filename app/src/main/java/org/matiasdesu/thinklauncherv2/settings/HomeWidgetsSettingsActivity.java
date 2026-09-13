package org.matiasdesu.thinklauncherv2.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.RepeatListener;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Single home for everything about the home-screen widget stack: the order it renders in, the
 * spacing between items, and (per row) a way into that item's own enable/configure screen -
 * order and enablement used to live on two separate screens (this one and a "Widget Order"
 * screen), which made no sense since you can't meaningfully set one without the other. SLOT_IDS
 * mirrors MainActivity's DEFAULT_STACK_ORDER: any id missing from a saved home_stack_order is
 * appended, so a slot never silently disappears.
 */
public class HomeWidgetsSettingsActivity extends BaseSettingsActivity {

    private static final String[] SLOT_IDS = { "clock_date", "status_row", "now_reading", "home_calendar" };

    private String labelFor(String id) {
        switch (id) {
            case "clock_date": return "Clock & Date";
            case "status_row": return "Status Row";
            case "now_reading": return "Now Reading";
            case "home_calendar": return "Calendar";
            default: return id;
        }
    }

    private static int iconFor(String id) {
        switch (id) {
            case "clock_date": return R.drawable.time;
            case "status_row": return R.drawable.battery;
            case "now_reading": return R.drawable.koreader;
            case "home_calendar": return R.drawable.date;
            default: return R.drawable.date;
        }
    }

    /** Settings screen this row opens, or null if it isn't a single configurable widget
     *  (clock_date is governed by the separate Time Settings/Date Settings screens instead). */
    private static Class<? extends BaseSettingsActivity> settingsScreenFor(String id) {
        switch (id) {
            case "status_row": return StatusRowSettingsActivity.class;
            case "now_reading": return NowReadingSettingsActivity.class;
            case "home_calendar": return HomeCalendarSettingsActivity.class;
            default: return null;
        }
    }

    private final List<String> order = new ArrayList<>();
    private LinearLayout listContainer;
    private int spacing;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home_widgets_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        listContainer = findViewById(R.id.order_list_container);
        parseOrder(prefs.getString("home_stack_order", String.join(",", SLOT_IDS)));

        spacing = prefs.getInt("home_stack_spacing", 0);
        View spacingContainer = findViewById(R.id.spacing_container);
        TextView spacingValueTv = spacingContainer.findViewById(R.id.value_text);
        spacingValueTv.setText(String.valueOf(spacing));
        ImageButton minusSpacingBtn = spacingContainer.findViewById(R.id.btn_minus);
        ImageButton plusSpacingBtn = spacingContainer.findViewById(R.id.btn_plus);

        minusSpacingBtn.setOnTouchListener(new RepeatListener(v -> {
            if (spacing > 0) {
                spacing = Math.max(0, spacing - 4);
                spacingValueTv.setText(String.valueOf(spacing));
                prefs.edit().putInt("home_stack_spacing", spacing).apply();
            }
        }));
        plusSpacingBtn.setOnTouchListener(new RepeatListener(v -> {
            if (spacing < 200) {
                spacing = Math.min(200, spacing + 4);
                spacingValueTv.setText(String.valueOf(spacing));
                prefs.edit().putInt("home_stack_spacing", spacing).apply();
            }
        }));

        renderList();
        initPagination(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A widget's own screen can flip its enablement; nothing here reflects that today besides
        // the row itself always being present, but re-reading order keeps a reorder made via the
        // system back button (rather than this screen) from ever going stale.
        parseOrder(prefs.getString("home_stack_order", String.join(",", SLOT_IDS)));
        renderList();
    }

    private void parseOrder(String raw) {
        order.clear();
        for (String id : raw.split(",")) {
            id = id.trim();
            for (String known : SLOT_IDS) {
                if (known.equals(id) && !order.contains(id)) {
                    order.add(id);
                    break;
                }
            }
        }
        for (String known : SLOT_IDS) {
            if (!order.contains(known)) order.add(known);
        }
    }

    private void renderList() {
        listContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < order.size(); i++) {
            String id = order.get(i);
            View row = inflater.inflate(R.layout.home_widget_order_row, listContainer, false);
            ((ImageView) row.findViewById(R.id.row_icon)).setImageResource(iconFor(id));
            ((TextView) row.findViewById(R.id.row_label)).setText(labelFor(id));

            Class<? extends BaseSettingsActivity> screen = settingsScreenFor(id);
            View forwardArrow = row.findViewById(R.id.row_forward_arrow);
            if (screen != null) {
                forwardArrow.setVisibility(View.VISIBLE);
                row.findViewById(R.id.row_root).setOnClickListener(v -> {
                    Intent intent = new Intent(this, screen);
                    if (!screenAnimations) intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, screenAnimations ? R.anim.slide_out_left : 0);
                });
            }

            ImageButton upBtn = row.findViewById(R.id.btn_up);
            ImageButton downBtn = row.findViewById(R.id.btn_down);
            int index = i;
            upBtn.setEnabled(index > 0);
            upBtn.setAlpha(index > 0 ? 1f : 0.3f);
            downBtn.setEnabled(index < order.size() - 1);
            downBtn.setAlpha(index < order.size() - 1 ? 1f : 0.3f);
            upBtn.setOnClickListener(v -> move(index, index - 1));
            downBtn.setOnClickListener(v -> move(index, index + 1));

            // renderList() runs from onResume (and again after every reorder), i.e. after
            // BaseSettingsActivity's font pass and the onCreate theme pass have already walked the
            // tree - so each freshly inflated row has to be themed and fonted itself, or these
            // rows alone render in the system font. Same per-row call AppBarAppsActivity makes.
            ThemeUtils.applyThemeToViewGroup((ViewGroup) row, theme, this);
            FontHelper.applyToViewTree(this, row);

            listContainer.addView(row);
        }
    }

    private void move(int from, int to) {
        if (to < 0 || to >= order.size()) return;
        String tmp = order.get(from);
        order.set(from, order.get(to));
        order.set(to, tmp);
        prefs.edit().putString("home_stack_order", String.join(",", order)).apply();
        renderList();
    }
}
