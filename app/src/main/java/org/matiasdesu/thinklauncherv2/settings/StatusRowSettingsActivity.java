package org.matiasdesu.thinklauncherv2.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.RepeatListener;
import org.matiasdesu.thinklauncherv2.utils.TextWidthHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

/** Settings for the home-screen status row (battery icon/percent + WiFi signal). Follows the same
 *  [-] VALUE [+] stepper pattern as every other settings screen - see DateSettingsActivity. */
public class StatusRowSettingsActivity extends BaseSettingsActivity {

    private int enabled;
    private int showBatteryIcon;
    private int showBatteryPercent;
    private int showWifi;
    private int fontSize;
    private int iconSize;
    private int horizontalPosition;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_status_row_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        enabled = prefs.getInt("status_row_enabled", 0);
        showBatteryIcon = prefs.getInt("status_show_battery_icon", 1);
        showBatteryPercent = prefs.getInt("status_show_battery_percent", 1);
        showWifi = prefs.getInt("status_show_wifi", 1);
        fontSize = prefs.getInt("status_row_font_size", 16);
        iconSize = prefs.getInt("status_row_icon_size", 20);
        horizontalPosition = prefs.getInt("status_row_horizontal_position", 0);

        View enabledContainer = findViewById(R.id.status_row_enabled_container);
        TextView enabledValueTv = enabledContainer.findViewById(R.id.value_text);
        enabledValueTv.setText(onOff(enabled));
        enabledValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(enabledValueTv, new String[] { "OFF", "ON" }));

        View batteryIconContainer = findViewById(R.id.status_battery_icon_container);
        TextView batteryIconValueTv = batteryIconContainer.findViewById(R.id.value_text);
        batteryIconValueTv.setText(onOff(showBatteryIcon));

        View batteryPercentContainer = findViewById(R.id.status_battery_percent_container);
        TextView batteryPercentValueTv = batteryPercentContainer.findViewById(R.id.value_text);
        batteryPercentValueTv.setText(onOff(showBatteryPercent));

        View wifiContainer = findViewById(R.id.status_wifi_container);
        TextView wifiValueTv = wifiContainer.findViewById(R.id.value_text);
        wifiValueTv.setText(onOff(showWifi));

        View fontSizeContainer = findViewById(R.id.status_font_size_container);
        TextView fontSizeValueTv = fontSizeContainer.findViewById(R.id.value_text);
        fontSizeValueTv.setText(String.valueOf(fontSize));

        View iconSizeContainer = findViewById(R.id.status_icon_size_container);
        TextView iconSizeValueTv = iconSizeContainer.findViewById(R.id.value_text);
        iconSizeValueTv.setText(String.valueOf(iconSize));

        View horizontalContainer = findViewById(R.id.status_horizontal_container);
        TextView horizontalValueTv = horizontalContainer.findViewById(R.id.value_text);
        horizontalValueTv.setText(horizontalText(horizontalPosition));
        horizontalValueTv.setMinWidth(
                TextWidthHelper.getMaxTextWidthPx(horizontalValueTv, new String[] { "LEFT", "CENTER", "RIGHT" }));

        ImageButton minusEnabled = enabledContainer.findViewById(R.id.btn_minus);
        ImageButton plusEnabled = enabledContainer.findViewById(R.id.btn_plus);
        minusEnabled.setOnClickListener(v -> {
            enabled = (enabled - 1 + 2) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("status_row_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusEnabled.setOnClickListener(v -> {
            enabled = (enabled + 1) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("status_row_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusBatteryIcon = batteryIconContainer.findViewById(R.id.btn_minus);
        ImageButton plusBatteryIcon = batteryIconContainer.findViewById(R.id.btn_plus);
        minusBatteryIcon.setOnClickListener(v -> {
            showBatteryIcon = (showBatteryIcon - 1 + 2) % 2;
            batteryIconValueTv.setText(onOff(showBatteryIcon));
            prefs.edit().putInt("status_show_battery_icon", showBatteryIcon).apply();
        });
        plusBatteryIcon.setOnClickListener(v -> {
            showBatteryIcon = (showBatteryIcon + 1) % 2;
            batteryIconValueTv.setText(onOff(showBatteryIcon));
            prefs.edit().putInt("status_show_battery_icon", showBatteryIcon).apply();
        });

        ImageButton minusBatteryPercent = batteryPercentContainer.findViewById(R.id.btn_minus);
        ImageButton plusBatteryPercent = batteryPercentContainer.findViewById(R.id.btn_plus);
        minusBatteryPercent.setOnClickListener(v -> {
            showBatteryPercent = (showBatteryPercent - 1 + 2) % 2;
            batteryPercentValueTv.setText(onOff(showBatteryPercent));
            prefs.edit().putInt("status_show_battery_percent", showBatteryPercent).apply();
        });
        plusBatteryPercent.setOnClickListener(v -> {
            showBatteryPercent = (showBatteryPercent + 1) % 2;
            batteryPercentValueTv.setText(onOff(showBatteryPercent));
            prefs.edit().putInt("status_show_battery_percent", showBatteryPercent).apply();
        });

        ImageButton minusWifi = wifiContainer.findViewById(R.id.btn_minus);
        ImageButton plusWifi = wifiContainer.findViewById(R.id.btn_plus);
        minusWifi.setOnClickListener(v -> {
            showWifi = (showWifi - 1 + 2) % 2;
            wifiValueTv.setText(onOff(showWifi));
            prefs.edit().putInt("status_show_wifi", showWifi).apply();
        });
        plusWifi.setOnClickListener(v -> {
            showWifi = (showWifi + 1) % 2;
            wifiValueTv.setText(onOff(showWifi));
            prefs.edit().putInt("status_show_wifi", showWifi).apply();
        });

        ImageButton minusFontSize = fontSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusFontSize = fontSizeContainer.findViewById(R.id.btn_plus);
        minusFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (fontSize > 8) {
                fontSize--;
                fontSizeValueTv.setText(String.valueOf(fontSize));
                prefs.edit().putInt("status_row_font_size", fontSize).apply();
            }
        }));
        plusFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (fontSize < 40) {
                fontSize++;
                fontSizeValueTv.setText(String.valueOf(fontSize));
                prefs.edit().putInt("status_row_font_size", fontSize).apply();
            }
        }));

        ImageButton minusIconSize = iconSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusIconSize = iconSizeContainer.findViewById(R.id.btn_plus);
        minusIconSize.setOnTouchListener(new RepeatListener(v -> {
            if (iconSize > 10) {
                iconSize--;
                iconSizeValueTv.setText(String.valueOf(iconSize));
                prefs.edit().putInt("status_row_icon_size", iconSize).apply();
            }
        }));
        plusIconSize.setOnTouchListener(new RepeatListener(v -> {
            if (iconSize < 60) {
                iconSize++;
                iconSizeValueTv.setText(String.valueOf(iconSize));
                prefs.edit().putInt("status_row_icon_size", iconSize).apply();
            }
        }));

        ImageButton minusHorizontal = horizontalContainer.findViewById(R.id.btn_minus);
        ImageButton plusHorizontal = horizontalContainer.findViewById(R.id.btn_plus);
        minusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition - 1 + 3) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("status_row_horizontal_position", horizontalPosition).apply();
        });
        plusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition + 1) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("status_row_horizontal_position", horizontalPosition).apply();
        });

        initPagination(this::refreshVisibility);
    }

    private void refreshVisibility() {
        int vis = enabled == 1 ? View.VISIBLE : View.GONE;
        findViewById(R.id.status_battery_icon_layout).setVisibility(vis);
        findViewById(R.id.status_battery_percent_layout).setVisibility(vis);
        findViewById(R.id.status_wifi_layout).setVisibility(vis);
        findViewById(R.id.status_font_size_layout).setVisibility(vis);
        findViewById(R.id.status_icon_size_layout).setVisibility(vis);
        findViewById(R.id.status_horizontal_layout).setVisibility(vis);
    }

    private String onOff(int v) { return v == 1 ? "ON" : "OFF"; }

    private String horizontalText(int pos) {
        switch (pos) {
            case 0: return "LEFT";
            case 2: return "RIGHT";
            default: return "CENTER";
        }
    }
}
