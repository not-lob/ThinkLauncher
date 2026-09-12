package org.matiasdesu.thinklauncherv2.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

/** Entry point for the three home-screen widgets (status row, now reading, calendar), linked from
 *  HomeScreenSettingsActivity - modelled on that screen's own flat list-of-links pattern. */
public class HomeWidgetsSettingsActivity extends BaseSettingsActivity {

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

        findViewById(R.id.status_row_settings_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, StatusRowSettingsActivity.class);
            if (!screenAnimations) intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, screenAnimations ? R.anim.slide_out_left : 0);
        });

        findViewById(R.id.now_reading_settings_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, NowReadingSettingsActivity.class);
            if (!screenAnimations) intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, screenAnimations ? R.anim.slide_out_left : 0);
        });

        findViewById(R.id.home_calendar_settings_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeCalendarSettingsActivity.class);
            if (!screenAnimations) intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, screenAnimations ? R.anim.slide_out_left : 0);
        });

        initPagination(null);
    }
}
