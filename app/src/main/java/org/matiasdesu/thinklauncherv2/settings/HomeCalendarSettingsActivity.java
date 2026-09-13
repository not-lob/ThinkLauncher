package org.matiasdesu.thinklauncherv2.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.ui.RenameDialog;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.FontRowBinder;
import org.matiasdesu.thinklauncherv2.utils.RepeatListener;
import org.matiasdesu.thinklauncherv2.utils.TextWidthHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

/** Settings for the home-screen calendar widget (month grid + agenda), following the same
 *  [-] VALUE [+] stepper pattern as DateSettingsActivity/CalendarOptionsDialog. */
public class HomeCalendarSettingsActivity extends BaseSettingsActivity {

    private int enabled;
    private int showMonthGrid;
    private int monthTitleBold;
    private int showEventDots;
    private int showAgenda;
    private int agendaCount;
    private int lookaheadDays;
    private int fontSize;
    private int cellSize;
    private int horizontalPosition;
    private int showHeader;
    private String headerText;
    private int headerFontSize;
    private int headerBold;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home_calendar_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        enabled = prefs.getInt("home_calendar_enabled", 0);
        showMonthGrid = prefs.getInt("home_calendar_show_month_grid", 1);
        monthTitleBold = prefs.getInt("home_calendar_month_title_bold", 1);
        showEventDots = prefs.getInt("home_calendar_show_event_dots", 1);
        showAgenda = prefs.getInt("home_calendar_show_agenda", 1);
        agendaCount = prefs.getInt("home_calendar_agenda_count", 3);
        lookaheadDays = prefs.getInt("home_calendar_lookahead_days", 7);
        fontSize = prefs.getInt("home_calendar_font_size", 14);
        cellSize = prefs.getInt("home_calendar_cell_size", 32);
        horizontalPosition = prefs.getInt("home_calendar_horizontal_position", 0);
        showHeader = prefs.getInt("home_calendar_show_header", 0);
        headerText = prefs.getString("home_calendar_header_text", "Calendar");
        headerFontSize = prefs.getInt("home_calendar_header_font_size", 20);
        headerBold = prefs.getInt("home_calendar_header_bold", 1);

        View enabledContainer = findViewById(R.id.home_calendar_enabled_container);
        TextView enabledValueTv = enabledContainer.findViewById(R.id.value_text);
        enabledValueTv.setText(onOff(enabled));
        enabledValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(enabledValueTv, new String[] { "OFF", "ON" }));

        View monthGridContainer = findViewById(R.id.home_calendar_month_grid_container);
        TextView monthGridValueTv = monthGridContainer.findViewById(R.id.value_text);
        monthGridValueTv.setText(onOff(showMonthGrid));

        View monthTitleBoldContainer = findViewById(R.id.home_calendar_month_title_bold_container);
        TextView monthTitleBoldValueTv = monthTitleBoldContainer.findViewById(R.id.value_text);
        monthTitleBoldValueTv.setText(onOff(monthTitleBold));
        monthTitleBoldValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(monthTitleBoldValueTv, new String[] { "OFF", "ON" }));

        View eventDotsContainer = findViewById(R.id.home_calendar_event_dots_container);
        TextView eventDotsValueTv = eventDotsContainer.findViewById(R.id.value_text);
        eventDotsValueTv.setText(onOff(showEventDots));

        View agendaContainer = findViewById(R.id.home_calendar_agenda_container);
        TextView agendaValueTv = agendaContainer.findViewById(R.id.value_text);
        agendaValueTv.setText(onOff(showAgenda));

        View agendaCountContainer = findViewById(R.id.home_calendar_agenda_count_container);
        TextView agendaCountValueTv = agendaCountContainer.findViewById(R.id.value_text);
        agendaCountValueTv.setText(String.valueOf(agendaCount));

        View lookaheadContainer = findViewById(R.id.home_calendar_lookahead_container);
        TextView lookaheadValueTv = lookaheadContainer.findViewById(R.id.value_text);
        lookaheadValueTv.setText(String.valueOf(lookaheadDays));

        View fontSizeContainer = findViewById(R.id.home_calendar_font_size_container);
        TextView fontSizeValueTv = fontSizeContainer.findViewById(R.id.value_text);
        fontSizeValueTv.setText(String.valueOf(fontSize));

        View cellSizeContainer = findViewById(R.id.home_calendar_cell_size_container);
        TextView cellSizeValueTv = cellSizeContainer.findViewById(R.id.value_text);
        cellSizeValueTv.setText(String.valueOf(cellSize));

        View horizontalContainer = findViewById(R.id.home_calendar_horizontal_container);
        TextView horizontalValueTv = horizontalContainer.findViewById(R.id.value_text);
        horizontalValueTv.setText(horizontalText(horizontalPosition));
        horizontalValueTv.setMinWidth(
                TextWidthHelper.getMaxTextWidthPx(horizontalValueTv, new String[] { "LEFT", "CENTER", "RIGHT" }));

        View headerContainer = findViewById(R.id.home_calendar_header_container);
        TextView headerValueTv = headerContainer.findViewById(R.id.value_text);
        headerValueTv.setText(onOff(showHeader));
        headerValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(headerValueTv, new String[] { "OFF", "ON" }));

        TextView headerTextValueTv = findViewById(R.id.home_calendar_header_text_value);
        headerTextValueTv.setText(headerText);
        headerTextValueTv.setOnClickListener(v -> new RenameDialog(this, headerText, newText -> {
            headerText = newText;
            headerTextValueTv.setText(headerText);
            prefs.edit().putString("home_calendar_header_text", headerText).apply();
        }).show());

        View headerFontSizeContainer = findViewById(R.id.home_calendar_header_font_size_container);
        TextView headerFontSizeValueTv = headerFontSizeContainer.findViewById(R.id.value_text);
        headerFontSizeValueTv.setText(String.valueOf(headerFontSize));

        View headerBoldContainer = findViewById(R.id.home_calendar_header_bold_container);
        TextView headerBoldValueTv = headerBoldContainer.findViewById(R.id.value_text);
        headerBoldValueTv.setText(onOff(headerBold));
        headerBoldValueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(headerBoldValueTv, new String[] { "OFF", "ON" }));

        ImageButton minusEnabled = enabledContainer.findViewById(R.id.btn_minus);
        ImageButton plusEnabled = enabledContainer.findViewById(R.id.btn_plus);
        minusEnabled.setOnClickListener(v -> {
            enabled = (enabled - 1 + 2) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("home_calendar_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusEnabled.setOnClickListener(v -> {
            enabled = (enabled + 1) % 2;
            enabledValueTv.setText(onOff(enabled));
            prefs.edit().putInt("home_calendar_enabled", enabled).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusMonthGrid = monthGridContainer.findViewById(R.id.btn_minus);
        ImageButton plusMonthGrid = monthGridContainer.findViewById(R.id.btn_plus);
        minusMonthGrid.setOnClickListener(v -> {
            showMonthGrid = (showMonthGrid - 1 + 2) % 2;
            monthGridValueTv.setText(onOff(showMonthGrid));
            prefs.edit().putInt("home_calendar_show_month_grid", showMonthGrid).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusMonthGrid.setOnClickListener(v -> {
            showMonthGrid = (showMonthGrid + 1) % 2;
            monthGridValueTv.setText(onOff(showMonthGrid));
            prefs.edit().putInt("home_calendar_show_month_grid", showMonthGrid).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusMonthTitleBold = monthTitleBoldContainer.findViewById(R.id.btn_minus);
        ImageButton plusMonthTitleBold = monthTitleBoldContainer.findViewById(R.id.btn_plus);
        minusMonthTitleBold.setOnClickListener(v -> {
            monthTitleBold = (monthTitleBold - 1 + 2) % 2;
            monthTitleBoldValueTv.setText(onOff(monthTitleBold));
            prefs.edit().putInt("home_calendar_month_title_bold", monthTitleBold).apply();
        });
        plusMonthTitleBold.setOnClickListener(v -> {
            monthTitleBold = (monthTitleBold + 1) % 2;
            monthTitleBoldValueTv.setText(onOff(monthTitleBold));
            prefs.edit().putInt("home_calendar_month_title_bold", monthTitleBold).apply();
        });

        ImageButton minusEventDots = eventDotsContainer.findViewById(R.id.btn_minus);
        ImageButton plusEventDots = eventDotsContainer.findViewById(R.id.btn_plus);
        minusEventDots.setOnClickListener(v -> {
            showEventDots = (showEventDots - 1 + 2) % 2;
            eventDotsValueTv.setText(onOff(showEventDots));
            prefs.edit().putInt("home_calendar_show_event_dots", showEventDots).apply();
        });
        plusEventDots.setOnClickListener(v -> {
            showEventDots = (showEventDots + 1) % 2;
            eventDotsValueTv.setText(onOff(showEventDots));
            prefs.edit().putInt("home_calendar_show_event_dots", showEventDots).apply();
        });

        ImageButton minusAgenda = agendaContainer.findViewById(R.id.btn_minus);
        ImageButton plusAgenda = agendaContainer.findViewById(R.id.btn_plus);
        minusAgenda.setOnClickListener(v -> {
            showAgenda = (showAgenda - 1 + 2) % 2;
            agendaValueTv.setText(onOff(showAgenda));
            prefs.edit().putInt("home_calendar_show_agenda", showAgenda).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusAgenda.setOnClickListener(v -> {
            showAgenda = (showAgenda + 1) % 2;
            agendaValueTv.setText(onOff(showAgenda));
            prefs.edit().putInt("home_calendar_show_agenda", showAgenda).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusAgendaCount = agendaCountContainer.findViewById(R.id.btn_minus);
        ImageButton plusAgendaCount = agendaCountContainer.findViewById(R.id.btn_plus);
        minusAgendaCount.setOnTouchListener(new RepeatListener(v -> {
            if (agendaCount > 1) {
                agendaCount--;
                agendaCountValueTv.setText(String.valueOf(agendaCount));
                prefs.edit().putInt("home_calendar_agenda_count", agendaCount).apply();
            }
        }));
        plusAgendaCount.setOnTouchListener(new RepeatListener(v -> {
            if (agendaCount < 10) {
                agendaCount++;
                agendaCountValueTv.setText(String.valueOf(agendaCount));
                prefs.edit().putInt("home_calendar_agenda_count", agendaCount).apply();
            }
        }));

        ImageButton minusLookahead = lookaheadContainer.findViewById(R.id.btn_minus);
        ImageButton plusLookahead = lookaheadContainer.findViewById(R.id.btn_plus);
        minusLookahead.setOnTouchListener(new RepeatListener(v -> {
            if (lookaheadDays > 1) {
                lookaheadDays--;
                lookaheadValueTv.setText(String.valueOf(lookaheadDays));
                prefs.edit().putInt("home_calendar_lookahead_days", lookaheadDays).apply();
            }
        }));
        plusLookahead.setOnTouchListener(new RepeatListener(v -> {
            if (lookaheadDays < 30) {
                lookaheadDays++;
                lookaheadValueTv.setText(String.valueOf(lookaheadDays));
                prefs.edit().putInt("home_calendar_lookahead_days", lookaheadDays).apply();
            }
        }));

        ImageButton minusFontSize = fontSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusFontSize = fontSizeContainer.findViewById(R.id.btn_plus);
        minusFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (fontSize > 8) {
                fontSize--;
                fontSizeValueTv.setText(String.valueOf(fontSize));
                prefs.edit().putInt("home_calendar_font_size", fontSize).apply();
            }
        }));
        plusFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (fontSize < 32) {
                fontSize++;
                fontSizeValueTv.setText(String.valueOf(fontSize));
                prefs.edit().putInt("home_calendar_font_size", fontSize).apply();
            }
        }));

        ImageButton minusCellSize = cellSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusCellSize = cellSizeContainer.findViewById(R.id.btn_plus);
        minusCellSize.setOnTouchListener(new RepeatListener(v -> {
            if (cellSize > 20) {
                cellSize--;
                cellSizeValueTv.setText(String.valueOf(cellSize));
                prefs.edit().putInt("home_calendar_cell_size", cellSize).apply();
            }
        }));
        plusCellSize.setOnTouchListener(new RepeatListener(v -> {
            if (cellSize < 64) {
                cellSize++;
                cellSizeValueTv.setText(String.valueOf(cellSize));
                prefs.edit().putInt("home_calendar_cell_size", cellSize).apply();
            }
        }));

        ImageButton minusHorizontal = horizontalContainer.findViewById(R.id.btn_minus);
        ImageButton plusHorizontal = horizontalContainer.findViewById(R.id.btn_plus);
        minusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition - 1 + 3) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("home_calendar_horizontal_position", horizontalPosition).apply();
        });
        plusHorizontal.setOnClickListener(v -> {
            horizontalPosition = (horizontalPosition + 1) % 3;
            horizontalValueTv.setText(horizontalText(horizontalPosition));
            prefs.edit().putInt("home_calendar_horizontal_position", horizontalPosition).apply();
        });

        ImageButton minusHeader = headerContainer.findViewById(R.id.btn_minus);
        ImageButton plusHeader = headerContainer.findViewById(R.id.btn_plus);
        minusHeader.setOnClickListener(v -> {
            showHeader = (showHeader - 1 + 2) % 2;
            headerValueTv.setText(onOff(showHeader));
            prefs.edit().putInt("home_calendar_show_header", showHeader).apply();
            refreshVisibility();
            refreshPagination();
        });
        plusHeader.setOnClickListener(v -> {
            showHeader = (showHeader + 1) % 2;
            headerValueTv.setText(onOff(showHeader));
            prefs.edit().putInt("home_calendar_show_header", showHeader).apply();
            refreshVisibility();
            refreshPagination();
        });

        ImageButton minusHeaderFontSize = headerFontSizeContainer.findViewById(R.id.btn_minus);
        ImageButton plusHeaderFontSize = headerFontSizeContainer.findViewById(R.id.btn_plus);
        minusHeaderFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (headerFontSize > 10) {
                headerFontSize--;
                headerFontSizeValueTv.setText(String.valueOf(headerFontSize));
                prefs.edit().putInt("home_calendar_header_font_size", headerFontSize).apply();
            }
        }));
        plusHeaderFontSize.setOnTouchListener(new RepeatListener(v -> {
            if (headerFontSize < 48) {
                headerFontSize++;
                headerFontSizeValueTv.setText(String.valueOf(headerFontSize));
                prefs.edit().putInt("home_calendar_header_font_size", headerFontSize).apply();
            }
        }));

        ImageButton minusHeaderBold = headerBoldContainer.findViewById(R.id.btn_minus);
        ImageButton plusHeaderBold = headerBoldContainer.findViewById(R.id.btn_plus);
        minusHeaderBold.setOnClickListener(v -> {
            headerBold = (headerBold - 1 + 2) % 2;
            headerBoldValueTv.setText(onOff(headerBold));
            prefs.edit().putInt("home_calendar_header_bold", headerBold).apply();
        });
        plusHeaderBold.setOnClickListener(v -> {
            headerBold = (headerBold + 1) % 2;
            headerBoldValueTv.setText(onOff(headerBold));
            prefs.edit().putInt("home_calendar_header_bold", headerBold).apply();
        });

        FontRowBinder.bind(this, findViewById(R.id.home_calendar_font_container), FontHelper.SLOT_HOME_CALENDAR);

        initPagination(this::refreshVisibility);
    }

    private void refreshVisibility() {
        int vis = enabled == 1 ? View.VISIBLE : View.GONE;
        findViewById(R.id.home_calendar_month_grid_layout).setVisibility(vis);
        findViewById(R.id.home_calendar_agenda_layout).setVisibility(vis);
        findViewById(R.id.home_calendar_font_size_layout).setVisibility(vis);
        findViewById(R.id.home_calendar_font_layout).setVisibility(vis);
        findViewById(R.id.home_calendar_horizontal_layout).setVisibility(vis);
        findViewById(R.id.home_calendar_header_layout).setVisibility(vis);

        findViewById(R.id.home_calendar_header_text_layout)
                .setVisibility(enabled == 1 && showHeader == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_header_font_size_layout)
                .setVisibility(enabled == 1 && showHeader == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_header_bold_layout)
                .setVisibility(enabled == 1 && showHeader == 1 ? View.VISIBLE : View.GONE);

        findViewById(R.id.home_calendar_month_title_bold_layout)
                .setVisibility(enabled == 1 && showMonthGrid == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_event_dots_layout)
                .setVisibility(enabled == 1 && showMonthGrid == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_cell_size_layout)
                .setVisibility(enabled == 1 && showMonthGrid == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_agenda_count_layout)
                .setVisibility(enabled == 1 && showAgenda == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.home_calendar_lookahead_layout)
                .setVisibility(enabled == 1 && showAgenda == 1 ? View.VISIBLE : View.GONE);
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
