package org.matiasdesu.thinklauncherv2.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Month-grid maths shared by the home-screen calendar widget, lifted out of
 * ui/CalendarActivity's buildGridDays()/weekday-label logic so both can use one implementation.
 */
public final class MonthGridHelper {

    private MonthGridHelper() {}

    /** 42 days (6 full weeks) covering {@code month}, starting on the locale's first day of week -
     *  including the leading/trailing days of the adjacent months that fill out the grid. */
    public static List<Calendar> buildGridDays(Calendar month) {
        List<Calendar> gridDays = new ArrayList<>();
        Calendar first = (Calendar) month.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = first.get(Calendar.DAY_OF_WEEK);
        int firstWeekDay = Calendar.getInstance().getFirstDayOfWeek();
        int offset = (firstDow - firstWeekDay + 7) % 7;
        Calendar start = (Calendar) first.clone();
        start.add(Calendar.DAY_OF_MONTH, -offset);
        for (int i = 0; i < 42; i++) {
            Calendar c = (Calendar) start.clone();
            c.add(Calendar.DAY_OF_MONTH, i);
            gridDays.add(c);
        }
        return gridDays;
    }

    /** Abbreviated weekday labels ("SUN".."SAT"), ordered from the locale's first day of week. */
    public static String[] weekdayLabels() {
        String[] labels = new String[7];
        SimpleDateFormat wdFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar tmp = Calendar.getInstance();
        tmp.set(Calendar.DAY_OF_WEEK, tmp.getFirstDayOfWeek());
        for (int i = 0; i < 7; i++) {
            String s = wdFormat.format(tmp.getTime()).toUpperCase(Locale.getDefault());
            labels[i] = s.length() > 3 ? s.substring(0, 3) : s;
            tmp.add(Calendar.DAY_OF_WEEK, 1);
        }
        return labels;
    }
}
