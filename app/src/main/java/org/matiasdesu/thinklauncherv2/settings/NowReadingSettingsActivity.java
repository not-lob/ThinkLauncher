package org.matiasdesu.thinklauncherv2.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.FontRowBinder;
import org.matiasdesu.thinklauncherv2.utils.RepeatListener;
import org.matiasdesu.thinklauncherv2.utils.TextWidthHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

/** Settings for the home-screen Now Reading (KOReader) widget - title/author/progress/cover are
 *  each independently togglable per the same [-] VALUE [+] stepper pattern as DateSettingsActivity. */
public class NowReadingSettingsActivity extends BaseSettingsActivity {

    private int enabled;
    private int showCover;
    private int coverSize;
    private int showTitle;
    private int titleSize;
    private int showAuthor;
    private int authorSize;
    private int showProgress;
    private int progressStyle;
    private int horizontalPosition;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_now_reading_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        enabled = prefs.getInt("now_reading_enabled", 0);
        showCover = prefs.getInt("now_reading_show_cover", 1);
        coverSize = prefs.getInt("now_reading_cover_size", 64);
        showTitle = prefs.getInt("now_reading_show_title", 1);
        titleSize = prefs.getInt("now_reading_title_font_size", 18);
        showAuthor = prefs.getInt("now_reading_show_author", 1);
        authorSize = prefs.getInt("now_reading_author_font_size", 14);
        showProgress = prefs.getInt("now_reading_show_progress", 1);
        progressStyle = prefs.getInt("now_reading_progress_style", 0);
        horizontalPosition = prefs.getInt("now_reading_horizontal_position", 0);

        View enabledContainer = findViewById(R.id.now_reading_enabled_container);
        TextView enabledValueTv = enabledContainer.findViewById(R.id.value_text);
        enabledValueTv.setText(onOff(enabled));
        enabledValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(enabledValueTv, new String[] { "OFF", "ON" }));

        View coverContainer = findViewById(R.id.now_reading_cover_container);
        TextView coverValueTv = coverContainer.findViewById(R.id.value_text);
        coverValueTv.setText(onOff(showCover));

        View coverSizeContainer = findViewById(R.id.now_reading_cover_size_container);
        TextView coverSizeValueTv = coverSizeContainer.findViewById(R.id.value_text);
        coverSizeValueTv.setText(String.valueOf(coverSize));

        View titleContainer = findViewById(R.id.now_reading_title_container);
        TextView titleValueTv = titleContainer.findViewById(R.id.value_text);
        titleValueTv.setText(onOff(showTitle));

        View titleSizeContainer = findViewById(R.id.now_reading_title_size_container);
        TextView titleSizeValueTv = titleSizeContainer.findViewById(R.id.value_text);
        titleSizeValueTv.setText(String.valueOf(titleSize));

        View authorContainer = findViewById(R.id.now_reading_author_container);
        TextView authorValueTv = authorContainer.findViewById(R.id.value_text);
        authorValueTv.setText(onOff(showAuthor));

        View authorSizeContainer = findViewById(R.id.now_reading_author_size_container);
        TextView authorSizeValueTv = authorSizeContainer.findViewById(R.id.value_text);
        authorSizeValueTv.setText(String.valueOf(authorSize));

        View progressContainer = findViewById(R.id.now_reading_progress_container);
        TextView progressValueTv = progressContainer.findViewById(R.id.value_text);
        progressValueTv.setText(onOff(showProgress));

        View progressStyleContainer = findViewById(R.id.now_reading_progress_style_container);
        TextView progressStyleValueTv = progressStyleContainer.findViewById(R.id.value_text);
        progressStyleValueTv.setText(progressStyleText(progressStyle));
        progressStyleValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(progressStyleValueTv,
                new String[] { "PERCENT", "BAR", "BOTH" }));

        View horizontalContainer = findViewById(R.id.now_reading_horizontal_container);
        TextView horizontalValueTv = horizontalContainer.findViewById(R.id.value_text);
        horizontalValueTv.setText(horizontalText(horizontalPosition));
        horizontalValueTv.setMinWidth(
                TextWidthHelper.getMaxTextWidthPx(horizontalValueTv, new String[] { "LEFT", "CENTER", "RIGHT" }));

        ImageButton minusEnabled = enabledContainer.findViewById(R.id.btn_minus);
        ImageButton plusEnabled = enabledContainer.findViewById(R.id.btn_plus);
        minusEnabled.setOnClickListener(v -> {
            enabled = (enabled - 1 + 2) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("now_reading_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusEnabled.setOnClickListener(v -> {
            enabled = (enabled + 1) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("now_reading_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusCover = coverContainer.findViewById(R.id.btn_minus);
        ImageButton plusCover = coverContainer.findViewById(R.id.btn_plus);
        minusCover.setOnClickListener(v -> {
            showCover = (showCover - 1 + 2) % 2;
            coverValueTv.setText(onOff(showCover));
            prefs.edit().putInt("now_reading_show_cover", showCover).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusCover.setOnClickListener(v -> {
            showCover = (showCover + 1) % 2;
            coverValueTv.setText(onOff(showCover));
            prefs.edit().putInt("now_reading_show_cover", showCover).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusCoverSize = coverSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusCoverSize = coverSizeContainer.findViewById(R.id.btn_plus);
        minusCoverSize.setOnTouchListener(new RepeatListener(v -> {
            if (coverSize > 32) {
                coverSize--;
                coverSizeValueTv.setText(String.valueOf(coverSize));
                prefs.edit().putInt("now_reading_cover_size", coverSize).apply();
            }
        }));
        plusCoverSize.setOnTouchListener(new RepeatListener(v -> {
            if (coverSize < 160) {
                coverSize++;
                coverSizeValueTv.setText(String.valueOf(coverSize));
                prefs.edit().putInt("now_reading_cover_size", coverSize).apply();
            }
        }));

        ImageButton minusTitle = titleContainer.findViewById(R.id.btn_minus);
        ImageButton plusTitle = titleContainer.findViewById(R.id.btn_plus);
        minusTitle.setOnClickListener(v -> {
            showTitle = (showTitle - 1 + 2) % 2;
            titleValueTv.setText(onOff(showTitle));
            prefs.edit().putInt("now_reading_show_title", showTitle).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusTitle.setOnClickListener(v -> {
            showTitle = (showTitle + 1) % 2;
            titleValueTv.setText(onOff(showTitle));
            prefs.edit().putInt("now_reading_show_title", showTitle).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusTitleSize = titleSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusTitleSize = titleSizeContainer.findViewById(R.id.btn_plus);
        minusTitleSize.setOnTouchListener(new RepeatListener(v -> {
            if (titleSize > 10) {
                titleSize--;
                titleSizeValueTv.setText(String.valueOf(titleSize));
                prefs.edit().putInt("now_reading_title_font_size", titleSize).apply();
            }
        }));
        plusTitleSize.setOnTouchListener(new RepeatListener(v -> {
            if (titleSize < 48) {
                titleSize++;
                titleSizeValueTv.setText(String.valueOf(titleSize));
                prefs.edit().putInt("now_reading_title_font_size", titleSize).apply();
            }
        }));

        ImageButton minusAuthor = authorContainer.findViewById(R.id.btn_minus);
        ImageButton plusAuthor = authorContainer.findViewById(R.id.btn_plus);
        minusAuthor.setOnClickListener(v -> {
            showAuthor = (showAuthor - 1 + 2) % 2;
            authorValueTv.setText(onOff(showAuthor));
            prefs.edit().putInt("now_reading_show_author", showAuthor).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusAuthor.setOnClickListener(v -> {
            showAuthor = (showAuthor + 1) % 2;
            authorValueTv.setText(onOff(showAuthor));
            prefs.edit().putInt("now_reading_show_author", showAuthor).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusAuthorSize = authorSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusAuthorSize = authorSizeContainer.findViewById(R.id.btn_plus);
        minusAuthorSize.setOnTouchListener(new RepeatListener(v -> {
            if (authorSize > 8) {
                authorSize--;
                authorSizeValueTv.setText(String.valueOf(authorSize));
                prefs.edit().putInt("now_reading_author_font_size", authorSize).apply();
            }
        }));
        plusAuthorSize.setOnTouchListener(new RepeatListener(v -> {
            if (authorSize < 36) {
                authorSize++;
                authorSizeValueTv.setText(String.valueOf(authorSize));
                prefs.edit().putInt("now_reading_author_font_size", authorSize).apply();
            }
        }));

        ImageButton minusProgress = progressContainer.findViewById(R.id.btn_minus);
        ImageButton plusProgress = progressContainer.findViewById(R.id.btn_plus);
        minusProgress.setOnClickListener(v -> {
            showProgress = (showProgress - 1 + 2) % 2;
            progressValueTv.setText(onOff(showProgress));
            prefs.edit().putInt("now_reading_show_progress", showProgress).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusProgress.setOnClickListener(v -> {
            showProgress = (showProgress + 1) % 2;
            progressValueTv.setText(onOff(showProgress));
            prefs.edit().putInt("now_reading_show_progress", showProgress).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusProgressStyle = progressStyleContainer.findViewById(R.id.btn_minus);
        ImageButton plusProgressStyle = progressStyleContainer.findViewById(R.id.btn_plus);
        minusProgressStyle.setOnClickListener(v -> {
            progressStyle = (progressStyle - 1 + 3) % 3;
            progressStyleValueTv.setText(progressStyleText(progressStyle));
            prefs.edit().putInt("now_reading_progress_style", progressStyle).apply();
        });
        plusProgressStyle.setOnClickListener(v -> {
            progressStyle = (progressStyle + 1) % 3;
            progressStyleValueTv.setText(progressStyleText(progressStyle));
            prefs.edit().putInt("now_reading_progress_style", progressStyle).apply();
        });

        ImageButton minusHorizontal = horizontalContainer.findViewById(R.id.btn_minus);
        ImageButton plusHorizontal = horizontalContainer.findViewById(R.id.btn_plus);
        minusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition - 1 + 3) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("now_reading_horizontal_position", horizontalPosition).apply();
        });
        plusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition + 1) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("now_reading_horizontal_position", horizontalPosition).apply();
        });

        FontRowBinder.bind(this, findViewById(R.id.now_reading_font_container), FontHelper.SLOT_NOW_READING);

        initPagination(this::refreshVisibility);
    }

    private void refreshVisibility() {
        int vis = enabled == 1 ? View.VISIBLE : View.GONE;
        findViewById(R.id.now_reading_cover_layout).setVisibility(vis);
        findViewById(R.id.now_reading_title_layout).setVisibility(vis);
        findViewById(R.id.now_reading_author_layout).setVisibility(vis);
        findViewById(R.id.now_reading_progress_layout).setVisibility(vis);
        findViewById(R.id.now_reading_horizontal_layout).setVisibility(vis);
        findViewById(R.id.now_reading_font_layout).setVisibility(vis);

        findViewById(R.id.now_reading_cover_size_layout)
                .setVisibility(enabled == 1 && showCover == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.now_reading_title_size_layout)
                .setVisibility(enabled == 1 && showTitle == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.now_reading_author_size_layout)
                .setVisibility(enabled == 1 && showAuthor == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.now_reading_progress_style_layout)
                .setVisibility(enabled == 1 && showProgress == 1 ? View.VISIBLE : View.GONE);
    }

    private String onOff(int v) { return v == 1 ? "ON" : "OFF"; }

    private String progressStyleText(int style) {
        switch (style) {
            case 1: return "BAR";
            case 2: return "BOTH";
            default: return "PERCENT";
        }
    }

    private String horizontalText(int pos) {
        switch (pos) {
            case 0: return "LEFT";
            case 2: return "RIGHT";
            default: return "CENTER";
        }
    }
}
