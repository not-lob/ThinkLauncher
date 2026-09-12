package org.matiasdesu.thinklauncherv2.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Shared CalendarContract.Instances query, used by the home-screen calendar widget (and, going
 * forward, a natural place for MainActivity's single-next-event lookup to move to as well).
 */
public final class CalendarEventsHelper {

    public static class CalendarEvent {
        public final long id;
        public final String title;
        public final long begin;
        public final long end;
        public final boolean allDay;

        public CalendarEvent(long id, String title, long begin, long end, boolean allDay) {
            this.id = id;
            this.title = title;
            this.begin = begin;
            this.end = end;
            this.allDay = allDay;
        }

        public boolean isToday() {
            Calendar eventCal = Calendar.getInstance();
            if (allDay) eventCal.setTimeZone(TimeZone.getTimeZone("UTC"));
            eventCal.setTimeInMillis(begin);
            Calendar today = Calendar.getInstance();
            return eventCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && eventCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        }

        public boolean isOnDay(Calendar day) {
            Calendar eventCal = Calendar.getInstance();
            if (allDay) eventCal.setTimeZone(TimeZone.getTimeZone("UTC"));
            eventCal.setTimeInMillis(begin);
            return eventCal.get(Calendar.YEAR) == day.get(Calendar.YEAR)
                    && eventCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR);
        }
    }

    private CalendarEventsHelper() {}

    /** Queries events with begin time in the range from (inclusive) to to (exclusive), ascending,
     *  capped at limit. Empty list on any
     *  permission or provider error - callers should check READ_CALENDAR themselves beforehand. */
    public static List<CalendarEvent> query(Context ctx, long from, long to, int limit) {
        List<CalendarEvent> events = new ArrayList<>();
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, from);
        ContentUris.appendId(builder, to);

        String[] projection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY
        };
        String selection = CalendarContract.Instances.BEGIN + ">=? AND "
                + CalendarContract.Instances.BEGIN + "<?";
        String[] args = { String.valueOf(from), String.valueOf(to) };
        String sortOrder = CalendarContract.Instances.BEGIN + " ASC";

        try (Cursor cursor = ctx.getContentResolver().query(builder.build(), projection, selection, args, sortOrder)) {
            if (cursor != null) {
                while (cursor.moveToNext() && events.size() < limit) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    long begin = cursor.getLong(2);
                    long end = cursor.getLong(3);
                    boolean allDay = cursor.getInt(4) == 1;
                    if (title == null || title.trim().isEmpty()) title = "Untitled event";
                    events.add(new CalendarEvent(id, title, begin, end, allDay));
                }
            }
        } catch (SecurityException e) {
            // Permission missing/revoked mid-session - return what we have (nothing).
        }
        return events;
    }
}
