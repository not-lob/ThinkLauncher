package org.matiasdesu.thinklauncherv2.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;

/**
 * Reads WiFi/connectivity state for the status-row widget. Mirrors BatteryUtils: a short-TTL cache
 * so a widget refresh on every onResume doesn't re-query the system on every call, and no
 * registered receiver (the launcher doesn't tick - see MainActivity's on-resume-only refresh model).
 *
 * RSSI/signal-level reads are not location-gated (only SSID/BSSID lookups are), so this needs only
 * the already-declared ACCESS_NETWORK_STATE / ACCESS_WIFI_STATE install-time permissions - no
 * runtime prompt.
 */
public final class NetworkStatusHelper {

    public enum State { WIFI, CELLULAR, OFFLINE }

    public static class Status {
        public final State state;
        /** 0-4 signal bars, only meaningful when state == WIFI. */
        public final int wifiLevel;

        Status(State state, int wifiLevel) {
            this.state = state;
            this.wifiLevel = wifiLevel;
        }
    }

    private static final long CACHE_TTL_MS = 30_000L;
    private static long lastFetchElapsed = -1;
    private static Status cached = new Status(State.OFFLINE, 0);

    private NetworkStatusHelper() {}

    public static Status getStatus(Context context) {
        long now = SystemClock.elapsedRealtime();
        if (lastFetchElapsed != -1 && now - lastFetchElapsed < CACHE_TTL_MS) {
            return cached;
        }
        cached = fetch(context);
        lastFetchElapsed = now;
        return cached;
    }

    /**
     * Forces the next {@link #getStatus} call to re-query instead of returning the cached value.
     * Called when a {@link android.net.ConnectivityManager.NetworkCallback} reports an actual
     * connectivity change, so a network flap is reflected immediately instead of waiting out the
     * TTL (or, worse, the next onResume - see HomeWidgetHost's network callback).
     */
    public static void invalidate() {
        lastFetchElapsed = -1;
    }

    private static Status fetch(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return new Status(State.OFFLINE, 0);
            android.net.Network active = cm.getActiveNetwork();
            if (active == null) return new Status(State.OFFLINE, 0);
            NetworkCapabilities caps = cm.getNetworkCapabilities(active);
            if (caps == null) return new Status(State.OFFLINE, 0);

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                int level = 2;
                try {
                    WifiManager wm = (WifiManager) context.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE);
                    if (wm != null) {
                        int rssi = wm.getConnectionInfo().getRssi();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            level = wm.calculateSignalLevel(rssi);
                        } else {
                            level = WifiManager.calculateSignalLevel(rssi, 5);
                        }
                    }
                } catch (Exception ignored) {
                    // Fall back to a mid-level icon rather than no icon at all.
                }
                return new Status(State.WIFI, Math.max(0, Math.min(4, level)));
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return new Status(State.CELLULAR, 0);
            }
            return new Status(State.OFFLINE, 0);
        } catch (Exception e) {
            return new Status(State.OFFLINE, 0);
        }
    }
}
