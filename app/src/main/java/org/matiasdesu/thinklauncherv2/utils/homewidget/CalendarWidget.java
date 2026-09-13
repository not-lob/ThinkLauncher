package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.matiasdesu.thinklauncherv2.ui.CalendarActivity;
import org.matiasdesu.thinklauncherv2.ui.StrokeTextView;
import org.matiasdesu.thinklauncherv2.utils.CalendarEventsHelper;
import org.matiasdesu.thinklauncherv2.utils.DialogEffectHelper;
import org.matiasdesu.thinklauncherv2.utils.MonthGridHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Static (non-navigable - see MainActivity/CalendarActivity for a full browsable calendar) month
 * grid plus an upcoming-events agenda, styled after the reference e-ink calendar screen: a month
 * title with a rule under it, an uppercase weekday header, and today filled solid rather than
 * outlined. Deliberately built from plain TextViews/LinearLayouts rather than a RecyclerView or
 * GridLayout - the grid never scrolls or resizes, so a recycler only adds inflation cost on the
 * launcher's startup path.
 */
public class CalendarWidget implements HomeWidget {

    private static class Data {
        boolean permissionMissing;
        List<CalendarEventsHelper.CalendarEvent> monthEvents;
        List<CalendarEventsHelper.CalendarEvent> agendaEvents;
    }

    private LinearLayout container;
    private TextView permissionPrompt;
    private LinearLayout monthSection;
    private TextView monthTitle;
    private LinearLayout weekdayRow;
    private final List<TextView> dayCells = new ArrayList<>();
    private LinearLayout agendaSection;

    private boolean showMonthGrid, showAgenda, showEventDots;
    private int agendaCount, lookaheadDays;
    private int textColor, bgColor;
    private int horizontalPosition;
    private List<Calendar> gridDays;
    private Calendar currentMonth;

    @Override
    public String id() { return "home_calendar"; }

    @Override
    public boolean isEnabled(SharedPreferences prefs) {
        return prefs.getInt("home_calendar_enabled", 0) == 1;
    }

    @Override
    public String[] prefKeys() {
        return new String[] {
                "home_calendar_enabled", "home_calendar_show_month_grid", "home_calendar_show_agenda",
                "home_calendar_agenda_count", "home_calendar_lookahead_days",
                "home_calendar_show_event_dots", "home_calendar_font_size", "home_calendar_cell_size",
                "home_calendar_horizontal_position"
        };
    }

    @Override
    public View createView(Activity host, RelativeLayout root, SharedPreferences prefs, int bgColor, int textColor) {
        this.textColor = textColor;
        this.bgColor = bgColor;
        showMonthGrid = prefs.getInt("home_calendar_show_month_grid", 1) == 1;
        showAgenda = prefs.getInt("home_calendar_show_agenda", 1) == 1;
        showEventDots = prefs.getInt("home_calendar_show_event_dots", 1) == 1;
        agendaCount = prefs.getInt("home_calendar_agenda_count", 3);
        lookaheadDays = prefs.getInt("home_calendar_lookahead_days", 7);
        int fontSize = prefs.getInt("home_calendar_font_size", 14);
        horizontalPosition = prefs.getInt("home_calendar_horizontal_position", 0);
        currentMonth = Calendar.getInstance();

        float density = host.getResources().getDisplayMetrics().density;
        int pad = (int) (16 * density / 2);

        container = new LinearLayout(host);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, pad, pad, pad);

        permissionPrompt = new StrokeTextView(host);
        permissionPrompt.setTextColor(textColor);
        permissionPrompt.setTextSize(fontSize);
        permissionPrompt.setText("Tap to grant calendar access");
        permissionPrompt.setVisibility(View.GONE);
        permissionPrompt.setOnClickListener(v -> host.startActivity(new Intent(host, CalendarActivity.class)));
        container.addView(permissionPrompt);

        if (showMonthGrid) {
            monthSection = new LinearLayout(host);
            monthSection.setOrientation(LinearLayout.VERTICAL);

            // Fixed-size square cells rather than weight-stretched (0dp + weight) ones: a
            // LinearLayout with weighted 0dp children still expands to fill an ancestor's
            // available width even when every LayoutParams in the chain says WRAP_CONTENT, so the
            // grid was always stretching edge-to-edge and squashing each day into a flat rectangle
            // instead of a square. Fixed widths keep the grid at its natural 7*cellSize size.
            // MATCH_PARENT has the exact same problem (it resolves against the nearest ancestor's
            // available space, not this section's own sibling-determined width) so the rule below
            // and the weekday columns get explicit pixel widths tied to cellSize too, instead.
            int cellSizeDp = prefs.getInt("home_calendar_cell_size", 32);
            int cellSize = (int) (cellSizeDp * density);
            // Font Size and Calendar Size are two independent controls, but a cell can never be
            // smaller than what its own day-number text needs - otherwise raising the font size
            // just clips inside a cell that never grows to match. This also means bumping Font
            // Size alone grows the whole grid once it exceeds Calendar Size's floor.
            int minCellSizeForFont = Math.round(fontSize * density * 2.2f);
            cellSize = Math.max(cellSize, minCellSizeForFont);
            int gridWidth = cellSize * 7;

            monthTitle = new StrokeTextView(host);
            monthTitle.setTextColor(textColor);
            monthTitle.setTextSize(fontSize + 6);
            monthTitle.setTypeface(null, Typeface.BOLD);
            monthTitle.setText(new SimpleDateFormat("MMMM", Locale.getDefault()).format(currentMonth.getTime()));
            monthSection.addView(monthTitle);

            View rule = new View(host);
            rule.setBackgroundColor(textColor);
            LinearLayout.LayoutParams ruleLp = new LinearLayout.LayoutParams(gridWidth, Math.max(1, (int) density));
            ruleLp.topMargin = (int) (4 * density);
            ruleLp.bottomMargin = (int) (8 * density);
            monthSection.addView(rule, ruleLp);

            // Weekday labels scale with the cell size, not the (unrelated) day-number font size
            // pref - otherwise a 3-letter label like "SUN" overflows a small cell's column.
            float weekdayTextSizeSp = Math.max(8f, cellSizeDp * 0.32f);

            weekdayRow = new LinearLayout(host);
            weekdayRow.setOrientation(LinearLayout.HORIZONTAL);
            for (String label : MonthGridHelper.weekdayLabels()) {
                TextView wd = new TextView(host);
                wd.setText(label);
                wd.setTextColor(textColor);
                wd.setAlpha(0.6f);
                wd.setTextSize(weekdayTextSizeSp);
                wd.setGravity(Gravity.CENTER);
                wd.setSingleLine(true);
                weekdayRow.addView(wd, new LinearLayout.LayoutParams(cellSize, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            monthSection.addView(weekdayRow);

            gridDays = MonthGridHelper.buildGridDays(currentMonth);
            dayCells.clear();
            for (int row = 0; row < 6; row++) {
                LinearLayout rowLayout = new LinearLayout(host);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                for (int col = 0; col < 7; col++) {
                    Calendar day = gridDays.get(row * 7 + col);
                    TextView dayNumber = new TextView(host);
                    dayNumber.setGravity(Gravity.CENTER);
                    dayNumber.setTextSize(fontSize);
                    dayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));
                    boolean inMonth = day.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                            && day.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR);
                    dayNumber.setAlpha(inMonth ? 1f : 0.35f);
                    dayNumber.setTextColor(textColor);
                    rowLayout.addView(dayNumber, new LinearLayout.LayoutParams(cellSize, cellSize));
                    dayCells.add(dayNumber);
                }
                monthSection.addView(rowLayout);
            }
            container.addView(monthSection);
        }

        if (showAgenda) {
            agendaSection = new LinearLayout(host);
            agendaSection.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams agendaLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            agendaLp.topMargin = showMonthGrid ? (int) (10 * density) : 0;
            container.addView(agendaSection, agendaLp);
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
        boolean hasPermission = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            data.permissionMissing = true;
            return data;
        }
        if (showAgenda) {
            long now = System.currentTimeMillis();
            long lookaheadMs = Math.max(1, lookaheadDays) * 24L * 60L * 60L * 1000L;
            data.agendaEvents = CalendarEventsHelper.query(ctx, now, now + lookaheadMs, Math.max(1, agendaCount));
        }
        if (showMonthGrid && showEventDots && gridDays != null && !gridDays.isEmpty()) {
            Calendar first = gridDays.get(0);
            Calendar last = (Calendar) gridDays.get(gridDays.size() - 1).clone();
            last.add(Calendar.DAY_OF_MONTH, 1);
            data.monthEvents = CalendarEventsHelper.query(ctx, first.getTimeInMillis(), last.getTimeInMillis(), 500);
        }
        return data;
    }

    @Override
    public void bind(Object result) {
        if (container == null || !(result instanceof Data)) return;
        Data data = (Data) result;

        if (data.permissionMissing) {
            permissionPrompt.setVisibility(View.VISIBLE);
            if (monthSection != null) monthSection.setVisibility(View.GONE);
            if (agendaSection != null) agendaSection.setVisibility(View.GONE);
            return;
        }
        permissionPrompt.setVisibility(View.GONE);
        if (monthSection != null) monthSection.setVisibility(View.VISIBLE);

        if (showMonthGrid && gridDays != null && data.monthEvents != null) {
            for (int i = 0; i < gridDays.size() && i < dayCells.size(); i++) {
                Calendar day = gridDays.get(i);
                boolean today = isSameDay(day, Calendar.getInstance());
                TextView cell = dayCells.get(i);
                boolean hasEvent = false;
                for (CalendarEventsHelper.CalendarEvent event : data.monthEvents) {
                    if (event.isOnDay(day)) { hasEvent = true; break; }
                }
                styleCell(cell, today, hasEvent);
            }
        } else if (showMonthGrid && gridDays != null) {
            // No event-dot data requested/available - still mark today.
            for (int i = 0; i < gridDays.size() && i < dayCells.size(); i++) {
                styleCell(dayCells.get(i), isSameDay(gridDays.get(i), Calendar.getInstance()), false);
            }
        }

        if (showAgenda && agendaSection != null) {
            agendaSection.setVisibility(View.VISIBLE);
            agendaSection.removeAllViews();
            List<CalendarEventsHelper.CalendarEvent> events = data.agendaEvents;
            if (events == null || events.isEmpty()) {
                TextView empty = new StrokeTextView(agendaSection.getContext());
                empty.setText("No upcoming events");
                empty.setTextColor(textColor);
                empty.setAlpha(0.6f);
                agendaSection.addView(empty);
            } else {
                SimpleDateFormat timeFmt = new SimpleDateFormat("EEE HH:mm", Locale.getDefault());
                for (CalendarEventsHelper.CalendarEvent event : events) {
                    TextView row = new StrokeTextView(agendaSection.getContext());
                    row.setTextColor(textColor);
                    row.setMaxLines(1);
                    row.setEllipsize(TextUtils.TruncateAt.END);
                    String when = event.allDay ? "" : timeFmt.format(new java.util.Date(event.begin)) + "  ";
                    row.setText(when + event.title);
                    row.setPadding(0, 2, 0, 2);
                    row.setOnClickListener(v -> openEvent(v.getContext(), event));
                    agendaSection.addView(row);
                }
            }
        } else if (agendaSection != null) {
            agendaSection.setVisibility(View.GONE);
        }
    }

    private void styleCell(TextView cell, boolean today, boolean hasEvent) {
        if (today) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(textColor);
            int radius = DialogEffectHelper.getCornerRadiusPx(cell.getContext());
            bg.setCornerRadius(radius > 0 ? radius : cell.getResources().getDisplayMetrics().density * 6);
            cell.setBackground(bg);
            cell.setTextColor(bgColor);
            cell.setTypeface(null, Typeface.BOLD);
        } else {
            cell.setBackground(null);
            cell.setTextColor(textColor);
            cell.setTypeface(null, Typeface.NORMAL);
        }
        // A trailing dot character keeps the event marker inside the existing TextView instead of
        // needing a second overlaid view per cell.
        String number = cell.getText().toString().replace("\u2022", "").trim();
        cell.setText(hasEvent && !today ? number + "\n\u2022" : number);
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private void openEvent(Context ctx, CalendarEventsHelper.CalendarEvent event) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.begin);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.end);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx.startActivity(intent);
        } catch (Exception ignored) {
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

    @Override
    public int horizontalPosition() { return horizontalPosition; }

}
