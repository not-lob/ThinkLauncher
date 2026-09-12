package org.matiasdesu.thinklauncherv2.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.MainActivity;
import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.DynamicIconHelper;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.IconPackHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

import java.util.List;

public class IconPackSettingsActivity extends BaseSettingsActivity {

    private BroadcastReceiver homeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    Intent mainIntent = new Intent(IconPackSettingsActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(mainIntent);
                }
            }
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_icon_pack_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        LinearLayout container = findViewById(R.id.settings_items_container);
        String selected = IconPackHelper.getSelectedPack(this);
        container.addView(buildRow("System default", "", selected));

        // Querying every theme intent touches the package manager for each
        // installed app, so keep it off the main thread.
        new Thread(() -> {
            final List<IconPackHelper.PackInfo> found = IconPackHelper.getAvailablePacks(this);
            runOnUiThread(() -> populate(container, found));
        }, "icon-pack-discovery").start();
    }

    private void populate(LinearLayout container, List<IconPackHelper.PackInfo> packs) {
        String selected = IconPackHelper.getSelectedPack(this);

        container.removeAllViews();
        container.addView(buildRow("System default", "", selected));

        for (IconPackHelper.PackInfo pack : packs) {
            container.addView(buildRow(pack.label, pack.packageName, selected));
        }

        if (packs.isEmpty()) {
            container.addView(buildMessage("No icon packs installed."));
        }

        ThemeUtils.applyThemeToViewGroup((ViewGroup) findViewById(R.id.root_layout), theme, this);
        FontHelper.applyToViewTree(this, rootLayout);
        initPagination(() -> {
        });
    }

    private View buildRow(String label, String packageName, String selectedPack) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(true);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dp(16);
        rowParams.setMargins(margin, margin, margin, margin);
        row.setLayoutParams(rowParams);

        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(ThemeUtils.getTextColor(theme, this));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(title);

        if (packageName.equals(selectedPack)) {
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

            ImageView check = new ImageView(this);
            check.setImageResource(R.drawable.check);
            check.setColorFilter(ThemeUtils.getTextColor(theme, this));
            check.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
            row.addView(check);
        }

        row.setOnClickListener(v -> select(packageName));
        return row;
    }

    private View buildMessage(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(ThemeUtils.getTextColor(theme, this));
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dp(16);
        params.setMargins(margin, margin, margin, margin);
        text.setLayoutParams(params);
        return text;
    }

    private void select(String packageName) {
        if (packageName.isEmpty()) {
            prefs.edit().remove(IconPackHelper.PREF_ICON_PACK).apply();
        } else {
            prefs.edit().putString(IconPackHelper.PREF_ICON_PACK, packageName).apply();
        }

        IconPackHelper.invalidate();
        IconPackHelper.ensureLoadedAsync(this);
        DynamicIconHelper.bumpCacheEpoch();

        onBackPressed();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(homeButtonReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"),
                Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(homeButtonReceiver);
    }
}
