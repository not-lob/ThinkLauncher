package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.BatteryUtils;
import org.matiasdesu.thinklauncherv2.utils.NetworkStatusHelper;
import org.matiasdesu.thinklauncherv2.views.BatteryLevelView;

/**
 * Battery-icon + percent + WiFi-signal status row, styled after the reference "98% | date" line but
 * as its own togglable widget (each of the three pieces can be hidden independently). Note the
 * existing date line already has a battery_info option (MainActivity.updateDateText); that keeps
 * working unchanged for back-compat, this is an additive, richer alternative.
 */
public class StatusRowWidget implements HomeWidget {

    private static class Data {
        int batteryPercent;
        NetworkStatusHelper.Status network;
    }

    private LinearLayout container;
    private BatteryLevelView batteryIcon;
    private TextView percentText;
    private ImageView wifiIcon;
    private int horizontalPosition;
    private int textColor;

    @Override
    public String id() { return "status_row"; }

    @Override
    public boolean isEnabled(SharedPreferences prefs) {
        return prefs.getInt("status_row_enabled", 0) == 1;
    }

    @Override
    public String[] prefKeys() {
        return new String[] {
                "status_row_enabled", "status_show_battery_icon", "status_show_battery_percent",
                "status_show_wifi", "status_row_font_size", "status_row_icon_size",
                "status_row_horizontal_position"
        };
    }

    @Override
    public View createView(Activity host, RelativeLayout root, SharedPreferences prefs, int bgColor, int textColor) {
        this.textColor = textColor;
        boolean showBatteryIcon = prefs.getInt("status_show_battery_icon", 1) == 1;
        boolean showBatteryPercent = prefs.getInt("status_show_battery_percent", 1) == 1;
        boolean showWifi = prefs.getInt("status_show_wifi", 1) == 1;
        int fontSize = prefs.getInt("status_row_font_size", 16);
        int iconSizeDp = prefs.getInt("status_row_icon_size", 20);
        horizontalPosition = prefs.getInt("status_row_horizontal_position", 0);

        float density = host.getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        int gapPx = (int) (6 * density);

        container = new LinearLayout(host);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding((int) (32 * density / 2), (int) (5 * density / 2),
                (int) (32 * density / 2), (int) (5 * density / 2));

        if (showBatteryIcon) {
            batteryIcon = new BatteryLevelView(host);
            batteryIcon.setColor(textColor);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int) (iconSizePx * 1.7f), iconSizePx);
            lp.setMarginEnd(gapPx);
            container.addView(batteryIcon, lp);
        }
        if (showBatteryPercent) {
            percentText = new TextView(host);
            percentText.setTextColor(textColor);
            percentText.setTextSize(fontSize);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(gapPx * 2);
            container.addView(percentText, lp);
        }
        if (showWifi) {
            wifiIcon = new ImageView(host);
            wifiIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Drawable wifiDrawable = ContextCompat.getDrawable(host, R.drawable.wifi_signal);
            if (wifiDrawable != null) {
                wifiDrawable = wifiDrawable.mutate();
                wifiDrawable.setTint(textColor);
                wifiIcon.setImageDrawable(wifiDrawable);
            }
            container.addView(wifiIcon, new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
        }

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(WidgetLayoutUtils.relativeHorizontalRule(horizontalPosition));
        container.setLayoutParams(rlp);
        return container;
    }

    @Override
    public Object loadData(Context ctx, SharedPreferences prefs) {
        Data data = new Data();
        data.batteryPercent = BatteryUtils.getBatteryPercentage(ctx);
        data.network = NetworkStatusHelper.getStatus(ctx);
        return data;
    }

    @Override
    public void bind(Object result) {
        if (!(result instanceof Data) || container == null) return;
        Data data = (Data) result;
        if (batteryIcon != null) {
            batteryIcon.setPercent(data.batteryPercent);
        }
        if (percentText != null) {
            percentText.setText(data.batteryPercent + "%");
        }
        if (wifiIcon != null) {
            boolean offline = data.network.state != NetworkStatusHelper.State.WIFI
                    && data.network.state != NetworkStatusHelper.State.CELLULAR;
            // The glyph itself doesn't have separate lightable bands (it's one fixed shape), so
            // signal strength is conveyed by fading it instead: faint when offline, ramping up to
            // full opacity at max signal.
            int alpha = offline ? 60 : 90 + (data.network.wifiLevel * 165 / 4);
            wifiIcon.setImageAlpha(alpha);
        }
    }

    @Override
    public void applyInsets(int homePaddingLeftPx, int homePaddingRightPx) {
        if (container == null) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) container.getLayoutParams();
        if (horizontalPosition == 0) {
            lp.leftMargin = homePaddingLeftPx;
        } else if (horizontalPosition == 2) {
            lp.rightMargin = homePaddingRightPx;
        }
        container.setLayoutParams(lp);
    }

    @Override
    public View getView() { return container; }
}
