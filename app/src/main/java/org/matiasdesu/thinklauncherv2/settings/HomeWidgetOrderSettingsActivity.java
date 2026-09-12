package org.matiasdesu.thinklauncherv2.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.RepeatListener;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the user reorder the whole home stack - the clock/date block and the three pluggable
 * widgets - and set the spacing between them, writing home_stack_order/home_stack_spacing that
 * MainActivity.createHomeWidgets reads. SLOT_IDS mirrors MainActivity's DEFAULT_STACK_ORDER: any
 * id missing from a saved order is appended, so a slot never silently disappears.
 */
public class HomeWidgetOrderSettingsActivity extends BaseSettingsActivity {

    private static final String[] SLOT_IDS = { "clock_date", "status_row", "now_reading", "home_calendar" };

    private static String labelFor(String id) {
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

    private final List<String> order = new ArrayList<>();
    private LinearLayout listContainer;
    private int spacing;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home_widget_order_settings;
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

            ImageButton upBtn = row.findViewById(R.id.btn_up);
            ImageButton downBtn = row.findViewById(R.id.btn_down);
            int index = i;
            upBtn.setEnabled(index > 0);
            upBtn.setAlpha(index > 0 ? 1f : 0.3f);
            downBtn.setEnabled(index < order.size() - 1);
            downBtn.setAlpha(index < order.size() - 1 ? 1f : 0.3f);
            upBtn.setOnClickListener(v -> move(index, index - 1));
            downBtn.setOnClickListener(v -> move(index, index + 1));

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
