package org.matiasdesu.thinklauncherv2;

import android.app.Activity;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import androidx.browser.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowInsets;
import androidx.core.view.WindowCompat;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.os.Build;

import org.matiasdesu.thinklauncherv2.adapters.AppAdapter;
import org.matiasdesu.thinklauncherv2.services.LockAccessibilityService;
import org.matiasdesu.thinklauncherv2.services.MusicNotificationListenerService;
import org.matiasdesu.thinklauncherv2.settings.MusicDockSettingsActivity;
import org.matiasdesu.thinklauncherv2.ui.AppLauncherActivity;
import org.matiasdesu.thinklauncherv2.ui.AppSelectorActivity;
import org.matiasdesu.thinklauncherv2.ui.AppShortcutsDialog;
import org.matiasdesu.thinklauncherv2.ui.IconPickerActivity;
import org.matiasdesu.thinklauncherv2.ui.MusicDockOptionsDialog;
import org.matiasdesu.thinklauncherv2.ui.StrokeTextView;
import org.matiasdesu.thinklauncherv2.ui.ShadowOutlineDrawable;
import org.matiasdesu.thinklauncherv2.utils.AppNamePositionHelper;
import org.matiasdesu.thinklauncherv2.utils.DynamicIconHelper;
import org.matiasdesu.thinklauncherv2.utils.IconPackHelper;
import org.matiasdesu.thinklauncherv2.utils.HomePagesManager;
import org.matiasdesu.thinklauncherv2.utils.HomePositionHelper;
import org.matiasdesu.thinklauncherv2.utils.IconMonochromeHelper;
import org.matiasdesu.thinklauncherv2.utils.IconShapeHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;
import org.matiasdesu.thinklauncherv2.utils.ShortcutHelper;
import org.matiasdesu.thinklauncherv2.utils.EinkRefreshHelper;
import org.matiasdesu.thinklauncherv2.utils.SystemBarsHelper;
import org.matiasdesu.thinklauncherv2.utils.BigmeShims;
import org.matiasdesu.thinklauncherv2.utils.WallpaperHelper;
import org.matiasdesu.thinklauncherv2.utils.BatteryUtils;
import org.matiasdesu.thinklauncherv2.utils.DialogEffectHelper;
import org.matiasdesu.thinklauncherv2.utils.DockBackdropHelper;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.SettingsBackupHelper;
import org.matiasdesu.thinklauncherv2.utils.SystemAppHelper;
import org.matiasdesu.thinklauncherv2.utils.OnyxHelper;
import org.matiasdesu.thinklauncherv2.utils.homewidget.WidgetLayoutUtils;
import android.graphics.Bitmap;

public class MainActivity extends Activity {

    private List<String> appLabels;
    private List<String> appPackages;
    private LinearLayout[] appSlots;
    private int maxApps;
    private int textSize;
    private int iconSize;
    private boolean boldText;
    private int textEffect;
    private int effectColor;
    private int iconEffect;
    private int iconEffectColor;
    private boolean showIcons;
    private boolean showAppNames;
    private int appNamePosition;
    private boolean monochromeIcons;
    private boolean dynamicIcons;
    private boolean forceMonochromeFallback;
    private boolean dynamicColors;
    private boolean invertIconColors;
    private boolean invertHomeColors;
    private boolean iconBackground;
    private int iconShape;
    private int homeAlignment;
    private int homeVerticalAlignment;
    private int homePosition;
    private int homeColumns;
    private int homePages;
    private boolean hidePagination;
    private int timePosition;
    private int timeFormat24h;
    private int dateVerticalPosition;
    private int clockDateGap;
    private int datePosition;
    private int dateHorizontalPosition;
    private int dateCalendarEvents;
    private int timeHorizontalPosition;
    private int dateFormat;
    private int timeFontSize;
    private int timeColor;
    private int timeEffect;
    private int timeEffectColor;
    private int dateFontSize;
    private int dateColor;
    private int dateEffect;
    private int dateEffectColor;
    private int fullMonthName;
    private int batteryInfo;
    private int batteryPosition;
    private int theme;
    private boolean hasWallpaper;
    private float wallpaperOffsetX = 0.5f;
    private float wallpaperOffsetY = 0.5f;
    private float wallpaperScale = 1f;
    private long wallpaperFileModified = 0L;
    private int textColor;
    private int appTextColor;
    private int doubleTapLock;
    private int showSettingsButton;
    private int showSearchButton;
    private int settingsButtonSize;
    private int settingsButtonColor;
    private int settingsButtonEffect;
    private int settingsButtonEffectColor;
    private int searchButtonSize;
    private int searchButtonColor;
    private int searchButtonEffect;
    private int searchButtonEffectColor;
    private String clockAppPkg;
    private String dateAppPkg;
    private TextView timeView;
    private TextView dateView;
    private TextView calendarEventView;
    private int calendarEventFontSize;
    private CalendarEventSummary calendarEventSummary;
    private int homeContentBottomId = View.NO_ID;
    private final org.matiasdesu.thinklauncherv2.utils.homewidget.HomeWidgetHost homeWidgetHost =
            new org.matiasdesu.thinklauncherv2.utils.homewidget.HomeWidgetHost(this);
    private RelativeLayout rootLayout;
    private LinearLayout mainLayout;
    private HomePagesManager homePagesManager;
    private SimpleDateFormat timeSdf;
    private SimpleDateFormat dateSdf;
    private Handler handler;
    private GestureHandler gestureHandler;
    private GestureLibrary customGestureLibrary;
    private ImageView settingsButton;
    private ImageView searchButton;
    private ImageView wallpaperView;
    private LinearLayout appBarView;
    private LinearLayout dockView;
    private LinearLayout musicDockView;
    private TextView musicDockTitleView;
    private View musicDockTitleSlot;
    private ImageView musicDockPlayPause;
    private MediaSessionManager mediaSessionManager;
    private MediaController activeMediaController;
    private MediaSessionManager.OnActiveSessionsChangedListener mediaSessionsListener;
    private MediaController.Callback mediaControllerCallback;
    private final Handler musicDockHideHandler = new Handler(Looper.getMainLooper());
    private Runnable musicDockHideRunnable;
    private static final int REQUEST_EDIT_DOCK_BASE = 10000;
    /**
     * Slot id for the clock/date/calendar-event block in home_stack_order - see
     * {@link #createHomeWidgets} and {@link #parseStackOrder}. The block's own internal ordering
     * (date-above-time vs time-above-date) stays governed by date_vertical_position; this id only
     * places the block as a whole relative to the pluggable widgets.
     */
    private static final String STACK_SLOT_CLOCK_DATE = "clock_date";
    private static final String DEFAULT_STACK_ORDER = "clock_date,status_row,now_reading,home_calendar";
    private String pendingDockPrefix = null;
    private int statusBarInset = 0;
    private int navBarInset = 0;
    private int homePaddingTop;
    private int homePaddingBottom;
    private int homePaddingLeft;
    private int homePaddingRight;
    private int homePaddingTopPx;
    private int homePaddingBottomPx;
    private int homePaddingLeftPx;
    private int homePaddingRightPx;
    private int customBgColor;
    private int customAccentColor;
    private boolean calendarPermissionGranted;
    private volatile boolean prefsDirty = true;
    private String iconPack = "";
    private int iconEpoch;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsChangeListener;

    private BroadcastReceiver homeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(mainIntent);
                }
            }
        }
    };

    private void handleSwipe(float dx, float dy) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String leftApp = prefs.getString("swipe_left_app", "");
        String rightApp = prefs.getString("swipe_right_app", "");
        String downApp = prefs.getString("swipe_down_app", "");
        String upApp = prefs.getString("swipe_up_app", "");
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        float minDist = 50 * getResources().getDisplayMetrics().density;
        if (absDx > absDy && absDx > minDist) {
            launchApp(dx > 0 ? rightApp : leftApp);
        } else if (absDy > absDx && absDy > minDist) {
            launchApp(dy > 0 ? downApp : upApp);
        }
    }

    private int getEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            return ThemeUtils.getBgColor(theme, this);
        }
        switch (effectColor) {
            case 0:
                return android.graphics.Color.BLACK;
            case 1:
                return android.graphics.Color.WHITE;
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return android.graphics.Color.BLACK;
        }
    }

    private int getTimeEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            if (invertHomeColors) {
                return this.textColor;
            }
            return ThemeUtils.getBgColor(theme, this);
        }
        int color;
        switch (timeEffectColor) {
            case 0:
                color = android.graphics.Color.BLACK;
                break;
            case 1:
                color = android.graphics.Color.WHITE;
                break;
            case 2:
                color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                break;
            case 3:
                color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                break;
            default:
                color = android.graphics.Color.BLACK;
                break;
        }
        if (invertHomeColors && timeEffectColor == 4) {

        }
        return color;
    }

    private int getDateEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            if (invertHomeColors) {
                return this.textColor;
            }
            return ThemeUtils.getBgColor(theme, this);
        }
        int color;
        switch (dateEffectColor) {
            case 0:
                color = android.graphics.Color.BLACK;
                break;
            case 1:
                color = android.graphics.Color.WHITE;
                break;
            case 2:
                color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                break;
            case 3:
                color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                break;
            default:
                color = android.graphics.Color.BLACK;
                break;
        }
        return color;
    }

    private int getIconEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            return ThemeUtils.getBgColor(theme, this);
        }
        switch (iconEffectColor) {
            case 0:
                return android.graphics.Color.BLACK;
            case 1:
                return android.graphics.Color.WHITE;
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return android.graphics.Color.BLACK;
        }
    }

    private int getTimeColorValue() {
        int color;
        if (timeColor == 0) {
            color = this.textColor;
        } else {
            switch (timeColor) {
                case 1:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_LIGHT, this);
                    break;
                case 2:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DARK, this);
                    break;
                case 3:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                    break;
                case 4:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                    break;
                default:
                    color = this.textColor;
                    break;
            }
        }

        if (invertHomeColors && timeColor == 0) {
            return ThemeUtils.getBgColor(theme, this);
        }
        return color;
    }

    private int getAppTextColorValue() {
        if (theme == ThemeUtils.THEME_CUSTOM) {
            return this.textColor;
        }
        if (appTextColor == 0) {
            return this.textColor;
        }
        switch (appTextColor) {
            case 1:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_LIGHT, this);
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DARK, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 4:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return this.textColor;
        }
    }

    private int getDateColorValue() {
        int color;
        if (dateColor == 0) {
            color = this.textColor;
        } else {
            switch (dateColor) {
                case 1:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_LIGHT, this);
                    break;
                case 2:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DARK, this);
                    break;
                case 3:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                    break;
                case 4:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                    break;
                default:
                    color = this.textColor;
                    break;
            }
        }

        if (invertHomeColors && dateColor == 0) {
            return ThemeUtils.getBgColor(theme, this);
        }
        return color;
    }

    private int getSettingsButtonColorValue() {
        int color;
        if (theme == ThemeUtils.THEME_CUSTOM) {
            color = this.textColor;
        } else if (settingsButtonColor == 0) {
            color = this.textColor;
        } else {
            switch (settingsButtonColor) {
                case 1:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_LIGHT, this);
                    break;
                case 2:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DARK, this);
                    break;
                case 3:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                    break;
                case 4:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                    break;
                default:
                    color = this.textColor;
                    break;
            }
        }

        if (invertHomeColors && (theme == ThemeUtils.THEME_CUSTOM || settingsButtonColor == 0)) {
            return ThemeUtils.getBgColor(theme, this);
        }
        return color;
    }

    private int getSettingsButtonEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            if (invertHomeColors) {
                return this.textColor;
            }
            return ThemeUtils.getBgColor(theme, this);
        }
        switch (settingsButtonEffectColor) {
            case 0:
                return android.graphics.Color.BLACK;
            case 1:
                return android.graphics.Color.WHITE;
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return android.graphics.Color.BLACK;
        }
    }

    private int getSearchButtonColorValue() {
        int color;
        if (theme == ThemeUtils.THEME_CUSTOM) {
            color = this.textColor;
        } else if (searchButtonColor == 0) {
            color = this.textColor;
        } else {
            switch (searchButtonColor) {
                case 1:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_LIGHT, this);
                    break;
                case 2:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DARK, this);
                    break;
                case 3:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
                    break;
                case 4:
                    color = ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
                    break;
                default:
                    color = this.textColor;
                    break;
            }
        }

        if (invertHomeColors && (theme == ThemeUtils.THEME_CUSTOM || searchButtonColor == 0)) {
            return ThemeUtils.getBgColor(theme, this);
        }
        return color;
    }

    private int getSearchButtonEffectColorValue() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (theme == ThemeUtils.THEME_CUSTOM) {
            if (invertHomeColors) {
                return this.textColor;
            }
            return ThemeUtils.getBgColor(theme, this);
        }
        switch (searchButtonEffectColor) {
            case 0:
                return android.graphics.Color.BLACK;
            case 1:
                return android.graphics.Color.WHITE;
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return android.graphics.Color.BLACK;
        }
    }

    private int getPaginationColorValue() {
        if (invertHomeColors) {
            return ThemeUtils.getBgColor(theme, this);
        }
        return this.textColor;
    }

    private String getTimePattern() {
        return timeFormat24h == 1 ? "HH:mm" : "hh:mm a";
    }

    private String getDatePattern() {
        switch (dateFormat) {
            case 0:
                return "dd MMM yyyy";
            case 1:
                return "dd MMMM yyyy";
            case 2:
                return "MMM dd, yyyy";
            case 3:
                return "MMMM dd, yyyy";
            case 4:
                return "yyyy-MM-dd";
            default:
                return "dd MMM yyyy";
        }
    }

    private void applyTextEffect(TextView tv) {
        applyTextEffect(tv, textEffect, getEffectColorValue());
    }

    private void applyTextEffect(TextView tv, int effect, int colorValue) {
        if (effect == 0) {
            tv.setShadowLayer(0, 0, 0, 0);
            if (tv instanceof StrokeTextView) {
                ((StrokeTextView) tv).setStroke(0, 0);
            }
            return;
        }

        if (effect == 1) {
            if (tv instanceof StrokeTextView) {
                ((StrokeTextView) tv).setStroke(0, 0);
            }
            tv.setShadowLayer(4.0f, 2.0f, 2.0f, colorValue);
        } else if (effect == 2) {
            tv.setShadowLayer(0, 0, 0, 0);
            if (tv instanceof StrokeTextView) {
                float width = android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 4f,
                        getResources().getDisplayMetrics());
                ((StrokeTextView) tv).setStroke(colorValue, width);
            } else {
                tv.setShadowLayer(5.0f, 0.0f, 0.0f, colorValue);
            }
        }
    }

    private void applyIconEffect(ImageView iv) {
        applyIconEffect(iv, iconEffect, getIconEffectColorValue());
    }

    private void applyIconEffect(ImageView iv, int effect, int color) {
        if (iv == null)
            return;

        int iconPaddingLeft = 0, iconPaddingRight = 0, iconPaddingTop = 0, iconPaddingBottom = 0;
        if (showAppNames) {
            if (appNamePosition == AppNamePositionHelper.POSITION_RIGHT) {
                iconPaddingRight = 16;
            } else if (appNamePosition == AppNamePositionHelper.POSITION_LEFT) {
                iconPaddingLeft = 16;
            } else if (appNamePosition == AppNamePositionHelper.POSITION_TOP) {
                iconPaddingTop = 8;
            } else if (appNamePosition == AppNamePositionHelper.POSITION_BOTTOM) {
                iconPaddingBottom = 8;
            }
        }
        iv.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);

        if (effect == 0) {
            Drawable current = iv.getDrawable();
            if (current instanceof ShadowOutlineDrawable) {
                iv.setImageDrawable(((ShadowOutlineDrawable) current).getInnerDrawable());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv.setElevation(0);
            }
            return;
        }

        Drawable original = iv.getDrawable();
        if (original != null) {
            if (original instanceof ShadowOutlineDrawable) {
                original = ((ShadowOutlineDrawable) original).getInnerDrawable();
            }

            if (iv == settingsButton || iv == searchButton) {
                if (original != null && !(original instanceof InsetDrawable)) {
                    original = new InsetDrawable(original, 0.15f);
                }
            }

            float offset = android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());

            // Apply the same offset to every icon (system, dynamic or regular) so shadow and
            // outline magnitude stay consistent across the home grid, dock and app bar.
            float adjustedOffset = offset * 1.5f;

            int p = (int) (adjustedOffset * 1.5f);

            iv.setPadding(iv.getPaddingLeft() + p, iv.getPaddingTop() + p,
                    iv.getPaddingRight() + p, iv.getPaddingBottom() + p);

            iv.setImageDrawable(new ShadowOutlineDrawable(original, effect, color, adjustedOffset));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv.setElevation(0);
            }
        }
    }

    private int getAppBarIconEffectColorValue(int effectColor, int theme) {
        if (theme == ThemeUtils.THEME_CUSTOM) {
            return ThemeUtils.getBgColor(theme, this);
        }
        switch (effectColor) {
            case 0:
                return android.graphics.Color.BLACK;
            case 1:
                return android.graphics.Color.WHITE;
            case 2:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            case 3:
                return ThemeUtils.getTextColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            default:
                return android.graphics.Color.BLACK;
        }
    }

    private void applyAppBarIconEffect(ImageView iv, int effect, int effectColor) {
        applyAppBarIconEffect(iv, effect, effectColor, appBarUsesBackground());
    }

    private void applyAppBarIconEffect(ImageView iv, int effect, int effectColor, boolean usesBackground) {
        if (iv == null)
            return;
        if (effect == 0) {
            return;
        }
        Drawable original = iv.getDrawable();
        if (original == null)
            return;
        if (original instanceof ShadowOutlineDrawable) {
            original = ((ShadowOutlineDrawable) original).getInnerDrawable();
        }
        float offset = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());
        float adjustedOffset = offset * 1.5f;
        if (!(original instanceof InsetDrawable) && !usesBackground) {
            original = new InsetDrawable(original, 0.15f);
        }
        int p = (int) (adjustedOffset * 1.5f);
        iv.setPadding(p, p, p, p);
        iv.setImageDrawable(new ShadowOutlineDrawable(original, effect,
                getAppBarIconEffectColorValue(effectColor, theme), adjustedOffset));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iv.setElevation(0);
        }
    }

    private boolean appBarUsesBackground() {
        android.content.SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        return prefs.getBoolean("app_bar_icon_background", true);
    }

private int resolveAppBarThemeColor(int colorSource, boolean isBackground) {
        switch (colorSource) {
            case 1: // Dark
                return ThemeUtils.getBgColor(ThemeUtils.THEME_DARK, this);
            case 2: // White
                return ThemeUtils.getBgColor(ThemeUtils.THEME_LIGHT, this);
            case 3: // Dynamic Dark
                return ThemeUtils.getBgColor(ThemeUtils.THEME_DYNAMIC_DARK, this);
            case 4: // Dynamic Light
                return ThemeUtils.getBgColor(ThemeUtils.THEME_DYNAMIC_LIGHT, this);
            default: // Follow Theme
                return isBackground
                        ? ThemeUtils.getBgColor(theme, this)
                        : ThemeUtils.getTextColor(theme, this);
        }
    }

    /**
     * Builds the whole home stack - the clock/date/calendar-event block and the pluggable widgets
     * (status row, now reading, calendar) - in the user-configured order (home_stack_order),
     * chaining each item below the previous one with home_stack_spacing between them. Renamed from
     * createTimeViews: the calendar-event line already made this the de-facto home-widget
     * renderer, so new widgets extend it rather than duplicating it.
     */
    private void createHomeWidgets(int bgColor, int textColor) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int spacingPx = (int) (prefs.getInt("home_stack_spacing", 0) * getResources().getDisplayMetrics().density);
        List<String> order = parseStackOrder(prefs.getString("home_stack_order", DEFAULT_STACK_ORDER));

        homeWidgetHost.beginBuild();
        int prevId = View.NO_ID;
        for (String slot : order) {
            if (STACK_SLOT_CLOCK_DATE.equals(slot)) {
                prevId = createClockDateBlock(prevId, spacingPx, bgColor, textColor);
            } else {
                prevId = homeWidgetHost.createOne(slot, rootLayout, prevId, spacingPx, prefs, bgColor, textColor);
            }
        }
        homeContentBottomId = prevId;
    }

    /**
     * Every known home-stack slot id, in the built-in default order. A user's saved
     * home_stack_order can predate a slot added in a later version (or, in principle, name one
     * that no longer exists) - sanitize() reconciles that by keeping only known ids and appending
     * any missing ones at the end, so a new slot always shows up rather than silently vanishing.
     */
    private static List<String> parseStackOrder(String raw) {
        String[] known = DEFAULT_STACK_ORDER.split(",");
        List<String> result = new ArrayList<>();
        for (String id : raw.split(",")) {
            id = id.trim();
            for (String k : known) {
                if (k.equals(id) && !result.contains(id)) {
                    result.add(id);
                    break;
                }
            }
        }
        for (String k : known) {
            if (!result.contains(k)) result.add(k);
        }
        return result;
    }

    /**
     * Insets a clock/date-block view by the same padding every pluggable home widget uses, so the
     * whole home stack shares one text edge. These views used to hard-code a raw {@code 32}/{@code
     * 5} *pixel* padding, which only lined up with the widgets' dp-based padding at density 2 and
     * drifted on every other device.
     */
    private void applyStackPadding(TextView view) {
        float density = getResources().getDisplayMetrics().density;
        int padX = WidgetLayoutUtils.horizontalPaddingPx(density);
        int padY = WidgetLayoutUtils.verticalPaddingPx(density);
        view.setPadding(padX, padY, padX, padY);
    }

    /**
     * Builds the clock/date/calendar-event block anchored below {@code anchorId} (or
     * ALIGN_PARENT_TOP if {@code anchorId} is View.NO_ID), returning the block's own trailing view
     * id - or {@code anchorId} unchanged if neither the clock nor the date is enabled. The block's
     * internal ordering (date-above-time vs time-above-date) is still governed by
     * date_vertical_position; only where the block as a whole sits in the home stack is new.
     */
    private int createClockDateBlock(int anchorId, int spacingPx, int bgColor, int textColor) {
        boolean showTime = timePosition == 1;
        boolean showDate = datePosition != 0;
        boolean showCalendarEvents = dateCalendarEvents == 1 && showDate && hasCalendarPermission();
        boolean hasWallpaper = WallpaperHelper.hasWallpaper(this);
        int timeDateBgColor = hasWallpaper ? android.graphics.Color.TRANSPARENT : bgColor;
        // Gap between the date group (date + its calendar-event line) and the time, on top of the
        // fixed ~5dp applyStackPadding already leaves - the calendar-event line stays glued to the
        // date it belongs to either way, so this only ever sits between the two groups.
        int clockDateGapPx = (int) (clockDateGap * getResources().getDisplayMetrics().density);

        if (showDate && dateVerticalPosition == 0) {

            String format = getDatePattern();
            dateSdf = new SimpleDateFormat(format);
            dateView = new StrokeTextView(this);
            dateView.setId(View.generateViewId());
            dateView.setIncludeFontPadding(false);
            updateDateText();
            dateView.setTextColor(getDateColorValue());
            dateView.setTextSize(dateFontSize);
            FontHelper.applyStyled(this, dateView, FontHelper.SLOT_DATE, boldText);
            applyTextEffect(dateView, dateEffect, getDateEffectColorValue());
            applyStackPadding(dateView);
            dateView.setBackgroundColor(timeDateBgColor);
            dateView.setGravity(getHorizontalGravity(dateHorizontalPosition));

            RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (anchorId == View.NO_ID) {
                dateParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                dateParams.addRule(RelativeLayout.BELOW, anchorId);
                dateParams.topMargin = spacingPx;
            }
            dateParams.addRule(getRelativeHorizontalRule(dateHorizontalPosition));
            if ((showSettingsButton == 1 || showSearchButton == 1) && dateHorizontalPosition == 2) {
                int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                        showSearchButton == 1 ? searchButtonSize : 0);
                int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                dateParams.rightMargin = buttonSizePx + 16;
            }
            rootLayout.addView(dateView, dateParams);

            if (showCalendarEvents) {
                calendarEventView = new StrokeTextView(this);
                calendarEventView.setId(View.generateViewId());
                calendarEventView.setIncludeFontPadding(false);
                updateCalendarEventText();
                calendarEventView.setTextColor(getDateColorValue());
                calendarEventView.setTextSize(calendarEventFontSize);
                FontHelper.applyStyled(this, calendarEventView, FontHelper.SLOT_CALENDAR_EVENT, boldText);
                applyTextEffect(calendarEventView, dateEffect, getDateEffectColorValue());
                applyStackPadding(calendarEventView);
                calendarEventView.setBackgroundColor(timeDateBgColor);
                calendarEventView.setGravity(getHorizontalGravity(dateHorizontalPosition));
                calendarEventView.setMaxLines(1);
                calendarEventView.setEllipsize(TextUtils.TruncateAt.END);
                calendarEventView.setOnClickListener(v -> openCalendarEvent(calendarEventSummary));
                RelativeLayout.LayoutParams eventParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                eventParams.addRule(RelativeLayout.BELOW, dateView.getId());
                eventParams.addRule(getRelativeHorizontalRule(dateHorizontalPosition));
                if ((showSettingsButton == 1 || showSearchButton == 1) && dateHorizontalPosition == 2) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    eventParams.rightMargin = buttonSizePx + 16;
                }
                rootLayout.addView(calendarEventView, eventParams);
            }
            if (showTime) {
                timeSdf = new SimpleDateFormat(getTimePattern());
                timeView = new StrokeTextView(this);
                timeView.setId(View.generateViewId());
                timeView.setIncludeFontPadding(false);
                timeView.setText(timeSdf.format(new Date()));
                timeView.setTextColor(getTimeColorValue());
                timeView.setTextSize(timeFontSize);
                FontHelper.applyStyled(this, timeView, FontHelper.SLOT_TIME, boldText);
                applyTextEffect(timeView, timeEffect, getTimeEffectColorValue());
                applyStackPadding(timeView);
                timeView.setBackgroundColor(timeDateBgColor);
                timeView.setGravity(getHorizontalGravity(timeHorizontalPosition));
                RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                timeParams.addRule(RelativeLayout.BELOW,
                    showCalendarEvents ? calendarEventView.getId() : dateView.getId());
                timeParams.topMargin = clockDateGapPx;
                timeParams.addRule(getRelativeHorizontalRule(timeHorizontalPosition));
                if ((showSettingsButton == 1 || showSearchButton == 1) && timeHorizontalPosition == 2) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    timeParams.rightMargin = buttonSizePx + 16;
                }
                rootLayout.addView(timeView, timeParams);
            }
            return showTime ? timeView.getId()
                    : showCalendarEvents ? calendarEventView.getId()
                    : dateView.getId();
        } else {

            if (showTime) {
                timeSdf = new SimpleDateFormat(getTimePattern());
                timeView = new StrokeTextView(this);
                timeView.setId(View.generateViewId());
                timeView.setIncludeFontPadding(false);
                timeView.setText(timeSdf.format(new Date()));
                timeView.setTextColor(getTimeColorValue());
                timeView.setTextSize(timeFontSize);
                FontHelper.applyStyled(this, timeView, FontHelper.SLOT_TIME, boldText);
                applyTextEffect(timeView, timeEffect, getTimeEffectColorValue());
                applyStackPadding(timeView);
                timeView.setBackgroundColor(timeDateBgColor);
                timeView.setGravity(getHorizontalGravity(timeHorizontalPosition));
                RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                if (anchorId == View.NO_ID) {
                    timeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                } else {
                    timeParams.addRule(RelativeLayout.BELOW, anchorId);
                    timeParams.topMargin = spacingPx;
                }
                timeParams.addRule(getRelativeHorizontalRule(timeHorizontalPosition));
                if ((showSettingsButton == 1 || showSearchButton == 1) && timeHorizontalPosition == 2) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    timeParams.rightMargin = buttonSizePx + 16;
                }
                rootLayout.addView(timeView, timeParams);
            }

            if (showDate)

            {
                String format = getDatePattern();
                dateSdf = new SimpleDateFormat(format);
                dateView = new StrokeTextView(this);
                dateView.setId(View.generateViewId());
                dateView.setIncludeFontPadding(false);
                updateDateText();
                dateView.setTextColor(getDateColorValue());
                dateView.setTextSize(dateFontSize);
                FontHelper.applyStyled(this, dateView, FontHelper.SLOT_DATE, boldText);
                applyTextEffect(dateView, dateEffect, getDateEffectColorValue());
                applyStackPadding(dateView);
                dateView.setBackgroundColor(timeDateBgColor);
                dateView.setGravity(getHorizontalGravity(dateHorizontalPosition));
                RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                if (showTime) {
                    dateParams.addRule(RelativeLayout.BELOW, timeView.getId());
                    dateParams.topMargin = clockDateGapPx;
                } else if (anchorId == View.NO_ID) {
                    dateParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                } else {
                    dateParams.addRule(RelativeLayout.BELOW, anchorId);
                    dateParams.topMargin = spacingPx;
                }
                dateParams.addRule(getRelativeHorizontalRule(dateHorizontalPosition));
                if ((showSettingsButton == 1 || showSearchButton == 1) && dateHorizontalPosition == 2) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    dateParams.rightMargin = buttonSizePx + 16;
                }
                rootLayout.addView(dateView, dateParams);
            }

            if (showCalendarEvents) {
                calendarEventView = new StrokeTextView(this);
                calendarEventView.setId(View.generateViewId());
                calendarEventView.setIncludeFontPadding(false);
                updateCalendarEventText();
                calendarEventView.setTextColor(getDateColorValue());
                calendarEventView.setTextSize(calendarEventFontSize);
                FontHelper.applyStyled(this, calendarEventView, FontHelper.SLOT_CALENDAR_EVENT, boldText);
                applyTextEffect(calendarEventView, dateEffect, getDateEffectColorValue());
                applyStackPadding(calendarEventView);
                calendarEventView.setBackgroundColor(timeDateBgColor);
                calendarEventView.setGravity(getHorizontalGravity(dateHorizontalPosition));
                calendarEventView.setMaxLines(1);
                calendarEventView.setEllipsize(TextUtils.TruncateAt.END);
                calendarEventView.setOnClickListener(v -> openCalendarEvent(calendarEventSummary));
                RelativeLayout.LayoutParams eventParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                eventParams.addRule(RelativeLayout.BELOW, dateView.getId());
                eventParams.addRule(getRelativeHorizontalRule(dateHorizontalPosition));
                if ((showSettingsButton == 1 || showSearchButton == 1) && dateHorizontalPosition == 2) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    eventParams.rightMargin = buttonSizePx + 16;
                }
                rootLayout.addView(calendarEventView, eventParams);
            }
            return showCalendarEvents ? calendarEventView.getId()
                    : showDate ? dateView.getId()
                    : showTime ? timeView.getId()
                    : anchorId;
        }
    }

    private void createSettingsButton(int bgColor, int textColor) {
        if (showSettingsButton == 1) {
            settingsButton = new ImageView(this);
            settingsButton.setId(View.generateViewId());
            settingsButton.setTag("special");
            settingsButton.setImageResource(R.drawable.settings);
            settingsButton.setColorFilter(getSettingsButtonColorValue());
            applyIconEffect(settingsButton, settingsButtonEffect, getSettingsButtonEffectColorValue());
            int sizePx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                    settingsButtonSize, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(sizePx, sizePx);
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            buttonParams.rightMargin = 16;
            buttonParams.topMargin = 5;
            settingsButton.setPadding(16, 8, 16, 8);
            settingsButton.setOnClickListener(v -> {
                try {
                    Class<?> clazz = Class.forName("org.matiasdesu.thinklauncherv2.settings.SettingsActivity");
                    Intent intent = new Intent(MainActivity.this, clazz);
                    boolean animate = getSharedPreferences("prefs", MODE_PRIVATE).getInt("screen_animations", 0) == 1;
                    if (!animate) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    }
                    startActivity(intent);
                    if (animate) {
                        overridePendingTransition(R.anim.dialog_fade_in, 0);
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            });
            rootLayout.addView(settingsButton, buttonParams);
        }
    }

    private void createSearchButton(int bgColor, int textColor) {
        if (showSearchButton == 1) {
            searchButton = new ImageView(this);
            searchButton.setId(View.generateViewId());
            searchButton.setTag("special");
            searchButton.setImageResource(R.drawable.search);
            searchButton.setColorFilter(getSearchButtonColorValue());
            applyIconEffect(searchButton, searchButtonEffect, getSearchButtonEffectColorValue());
            int sizePx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                    searchButtonSize, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(sizePx, sizePx);
            if (showSettingsButton == 1) {
                buttonParams.addRule(RelativeLayout.BELOW, settingsButton.getId());
            } else {
                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            buttonParams.rightMargin = 16;
            buttonParams.topMargin = 5;
            searchButton.setPadding(16, 8, 16, 8);
            searchButton.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                boolean animate = prefs.getInt("screen_animations", 0) == 1;
                Intent intent = new Intent(MainActivity.this, AppLauncherActivity.class);
                if (!animate) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                startActivity(intent);
                if (animate) {
                    overridePendingTransition(R.anim.dialog_fade_in, 0);
                }
            });
            rootLayout.addView(searchButton, buttonParams);
        }
    }

    private void createAppBar() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("app_bar_enabled", 0) != 1) {
            return;
        }
        int position = prefs.getInt("app_bar_position", 0);
        int iconSizeDp = prefs.getInt("app_bar_icon_size", 24);
        int numApps = prefs.getInt("app_bar_num_apps", 4);
        boolean vertical = prefs.getInt("app_bar_orientation", 0) == 1;

        boolean appDynamicIcons = prefs.getBoolean("app_bar_dynamic_icons", false);
        boolean appIconBackground = prefs.getBoolean("app_bar_icon_background", true);
        boolean appDynamicColors = prefs.getBoolean("app_bar_dynamic_colors", false);
        boolean appInvertIconColors = prefs.getBoolean("app_bar_invert_icon_colors", false);
        int appIconShape = prefs.getInt("app_bar_icon_shape", IconShapeHelper.SHAPE_SYSTEM);
        boolean appForceMonochromeFallback = prefs.getBoolean("app_bar_force_monochrome_fallback", false);
        boolean appMonochrome = prefs.getBoolean("app_bar_monochrome_icons", false);
        int appIconEffect = prefs.getInt("app_bar_icon_effect", 0);
        int appIconEffectColor = prefs.getInt("app_bar_icon_effect_color", 0);

        LinearLayout bar = new LinearLayout(this);
        bar.setId(View.generateViewId());
        bar.setOrientation(vertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);

        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        int slotMargin = (int) (4 * density);

        for (int i = 0; i < numApps; i++) {
            String pkg = prefs.getString("app_bar_app_package_" + i, "");
            if (pkg == null || pkg.isEmpty()) {
                continue;
            }
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            if (vertical) {
                lp.topMargin = slotMargin;
                lp.bottomMargin = slotMargin;
            } else {
                lp.leftMargin = slotMargin;
                lp.rightMargin = slotMargin;
            }
            iv.setLayoutParams(lp);
            boolean appIsSpecial = "launcher_settings".equals(pkg) || "app_launcher".equals(pkg)
                    || "notification_panel".equals(pkg) || "koreader_history".equals(pkg)
                    || "calendar".equals(pkg) || "gallery".equals(pkg) || "clock".equals(pkg) || "calculator".equals(pkg) || "bigme_control_panel".equals(pkg)
                    || "bigme_eink_settings".equals(pkg)
                    || OnyxHelper.isOnyxPseudoPackage(pkg)
                    || (pkg != null && pkg.startsWith("folder_"))
                    || (pkg != null && pkg.startsWith("webapp_"));
            if (appIsSpecial) {
                if (OnyxHelper.isOnyxPseudoPackage(pkg)) {
                    Drawable onyxIcon = OnyxHelper.getOnyxIcon(this, pkg);
                    iv.setImageDrawable(onyxIcon);
                    if (appMonochrome) {
                        iv.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iv.clearColorFilter();
                    }
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                } else {
                    int drawableRes = "launcher_settings".equals(pkg) ? R.drawable.settings
                            : "app_launcher".equals(pkg) ? R.drawable.search
                                    : "notification_panel".equals(pkg) ? R.drawable.notifications
                                            : "koreader_history".equals(pkg) ? R.drawable.koreader
                                                    : "calendar".equals(pkg) ? R.drawable.date
                                                                    : "gallery".equals(pkg) ? R.drawable.gallery
                                                                            : "clock".equals(pkg) ? R.drawable.time
                                                                            : "calculator".equals(pkg) ? R.drawable.calculator
                                                                    : "bigme_control_panel".equals(pkg) ? R.drawable.generic_app
                                                                            : "bigme_eink_settings".equals(pkg) ? R.drawable.generic_app
                                                                                    : (pkg != null && pkg.startsWith("webapp_")) ? R.drawable.webapps
                                                                                            : R.drawable.folder;
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, drawableRes, theme,
                            appIconBackground, appDynamicColors, appInvertIconColors, appIconShape);
                    iv.setImageDrawable(specialIcon);
                    iv.clearColorFilter();
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                }
            } else if (pkg != null && pkg.startsWith("hidden_app_")) {
                iv.setImageDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            } else {
                try {
                    Drawable drawable = DynamicIconHelper.getAppIcon(this, pkg, appDynamicIcons, theme,
                            appIconBackground, appDynamicColors, appInvertIconColors, appIconShape, appForceMonochromeFallback);
                    iv.setImageDrawable(drawable);
                    if (appMonochrome) {
                        iv.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iv.clearColorFilter();
                    }
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                } catch (Exception e) {
                    continue;
                }
            }
            final String fpkg = pkg;
            setupDockItemTap(iv, "app_bar", i, fpkg);
            bar.addView(iv);
        }

        if (bar.getChildCount() == 0) {
            return;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) (8 * density);
        switch (position) {
            case 0:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 1:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 2:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 3:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 4:
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 5:
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 6:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 8:
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                break;
            default:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
        }

        boolean topAnchored = (position == 0 || position == 1 || position == 6);
        boolean bottomAnchored = (position == 2 || position == 3 || position == 7);
        updatePaddingPx();
        params.leftMargin = margin + homePaddingLeftPx;
        params.rightMargin = margin + homePaddingRightPx;
        params.topMargin = margin + homePaddingTopPx + (topAnchored ? statusBarInset : 0);
        params.bottomMargin = margin + homePaddingBottomPx + (bottomAnchored ? navBarInset : 0);

        boolean appBarBorderEnabled = prefs.getInt("app_bar_border", 0) == 1;
        boolean appBarBgEnabled = prefs.getInt("app_bar_background", 0) == 1;
        if (appBarBorderEnabled || appBarBgEnabled) {
            android.graphics.drawable.GradientDrawable barBg = new android.graphics.drawable.GradientDrawable();
            barBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            if (appBarBorderEnabled) {
                barBg.setStroke((int) (2 * density),
                        resolveAppBarThemeColor(prefs.getInt("app_bar_border_color", 0), false));
            }
            barBg.setColor(appBarBgEnabled
                    ? resolveAppBarThemeColor(prefs.getInt("app_bar_background_color", 0), true)
                    : android.graphics.Color.TRANSPARENT);
            barBg.setCornerRadius(DialogEffectHelper.getCornerRadiusPx(this));
            bar.setBackground(barBg);
            int barPad = (int) (6 * density);
            bar.setPadding(barPad, barPad, barPad, barPad);
            bar.setClipToPadding(true);
        }

        rootLayout.addView(bar, params);
        appBarView = bar;

        if (appBarBgEnabled) {
            DockBackdropHelper.applyBackdrop(bar, rootLayout, prefs, "app_bar",
                    resolveAppBarThemeColor(prefs.getInt("app_bar_background_color", 0), true),
                    appBarBorderEnabled,
                    resolveAppBarThemeColor(prefs.getInt("app_bar_border_color", 0), false),
                    2 * density,
                    DialogEffectHelper.getCornerRadiusPx(this));
        }
    }

    private void refreshAppBar() {
        if (appBarView != null && appBarView.getParent() == rootLayout) {
            rootLayout.removeView(appBarView);
        }
        appBarView = null;
        createAppBar();
    }

    private void createDock() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("dock_enabled", 0) != 1) {
            return;
        }
        int iconSizeDp = prefs.getInt("dock_icon_size", 24);
        int numApps = prefs.getInt("dock_num_apps", 4);

        boolean appDynamicIcons = prefs.getBoolean("dock_dynamic_icons", false);
        boolean appIconBackground = prefs.getBoolean("dock_icon_background", true);
        boolean appDynamicColors = prefs.getBoolean("dock_dynamic_colors", false);
        boolean appInvertIconColors = prefs.getBoolean("dock_invert_icon_colors", false);
        int appIconShape = prefs.getInt("dock_icon_shape", IconShapeHelper.SHAPE_SYSTEM);
        boolean appForceMonochromeFallback = prefs.getBoolean("dock_force_monochrome_fallback", false);
        boolean appMonochrome = prefs.getBoolean("dock_monochrome_icons", false);
        int appIconEffect = prefs.getInt("dock_icon_effect", 0);
        int appIconEffectColor = prefs.getInt("dock_icon_effect_color", 0);

        LinearLayout bar = new LinearLayout(this);
        bar.setId(View.generateViewId());
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);

        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        int slotMargin = (int) (4 * density);

        for (int i = 0; i < numApps; i++) {
            String pkg = prefs.getString("dock_app_package_" + i, "");
            if (pkg == null || pkg.isEmpty()) {
                continue;
            }
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
            lp.leftMargin = slotMargin;
            lp.rightMargin = slotMargin;
            iv.setLayoutParams(lp);
            boolean appIsSpecial = "launcher_settings".equals(pkg) || "app_launcher".equals(pkg)
                    || "notification_panel".equals(pkg) || "koreader_history".equals(pkg)
                    || "calendar".equals(pkg) || "gallery".equals(pkg) || "clock".equals(pkg) || "calculator".equals(pkg) || "bigme_control_panel".equals(pkg)
                    || "bigme_eink_settings".equals(pkg)
                    || OnyxHelper.isOnyxPseudoPackage(pkg)
                    || (pkg != null && pkg.startsWith("folder_"))
                    || (pkg != null && pkg.startsWith("webapp_"));
            if (appIsSpecial) {
                if (OnyxHelper.isOnyxPseudoPackage(pkg)) {
                    Drawable onyxIcon = OnyxHelper.getOnyxIcon(this, pkg);
                    iv.setImageDrawable(onyxIcon);
                    if (appMonochrome) {
                        iv.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iv.clearColorFilter();
                    }
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                } else {
                    int drawableRes = "launcher_settings".equals(pkg) ? R.drawable.settings
                            : "app_launcher".equals(pkg) ? R.drawable.search
                                    : "notification_panel".equals(pkg) ? R.drawable.notifications
                                            : "koreader_history".equals(pkg) ? R.drawable.koreader
                                                    : "calendar".equals(pkg) ? R.drawable.date
                                                                    : "gallery".equals(pkg) ? R.drawable.gallery
                                                                            : "clock".equals(pkg) ? R.drawable.time
                                                                            : "calculator".equals(pkg) ? R.drawable.calculator
                                                                    : "bigme_control_panel".equals(pkg) ? R.drawable.generic_app
                                                                            : "bigme_eink_settings".equals(pkg) ? R.drawable.generic_app
                                                                                    : (pkg != null && pkg.startsWith("webapp_")) ? R.drawable.webapps
                                                                                            : R.drawable.folder;
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, drawableRes, theme,
                            appIconBackground, appDynamicColors, appInvertIconColors, appIconShape);
                    iv.setImageDrawable(specialIcon);
                    iv.clearColorFilter();
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                }
            } else if (pkg != null && pkg.startsWith("hidden_app_")) {
                iv.setImageDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            } else {
                try {
                    Drawable drawable = DynamicIconHelper.getAppIcon(this, pkg, appDynamicIcons, theme,
                            appIconBackground, appDynamicColors, appInvertIconColors, appIconShape, appForceMonochromeFallback);
                    iv.setImageDrawable(drawable);
                    if (appMonochrome) {
                        iv.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iv.clearColorFilter();
                    }
                    applyAppBarIconEffect(iv, appIconEffect, appIconEffectColor);
                } catch (Exception e) {
                    continue;
                }
            }
            final String fpkg = pkg;
            setupDockItemTap(iv, "dock", i, fpkg);
            bar.addView(iv);
        }

        if (bar.getChildCount() == 0) {
            return;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        int margin = (int) (8 * density);
        updatePaddingPx();
        params.leftMargin = margin + homePaddingLeftPx;
        params.rightMargin = margin + homePaddingRightPx;
        params.topMargin = margin;
        params.bottomMargin = margin + homePaddingBottomPx + navBarInset;

        boolean appBarBorderEnabled = prefs.getInt("dock_border", 0) == 1;
        boolean appBarBgEnabled = prefs.getInt("dock_background", 0) == 1;
        if (appBarBorderEnabled || appBarBgEnabled) {
            android.graphics.drawable.GradientDrawable barBg = new android.graphics.drawable.GradientDrawable();
            barBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            if (appBarBorderEnabled) {
                barBg.setStroke((int) (2 * density),
                        resolveAppBarThemeColor(prefs.getInt("dock_border_color", 0), false));
            }
            barBg.setColor(appBarBgEnabled
                    ? resolveAppBarThemeColor(prefs.getInt("dock_background_color", 0), true)
                    : android.graphics.Color.TRANSPARENT);
            barBg.setCornerRadius(DialogEffectHelper.getCornerRadiusPx(this));
            bar.setBackground(barBg);
            int barPad = (int) (6 * density);
            bar.setPadding(barPad, barPad, barPad, barPad);
            bar.setClipToPadding(true);
        }

        rootLayout.addView(bar, params);
        dockView = bar;

        if (appBarBgEnabled) {
            DockBackdropHelper.applyBackdrop(bar, rootLayout, prefs, "dock",
                    resolveAppBarThemeColor(prefs.getInt("dock_background_color", 0), true),
                    appBarBorderEnabled,
                    resolveAppBarThemeColor(prefs.getInt("dock_border_color", 0), false),
                    2 * density,
                    DialogEffectHelper.getCornerRadiusPx(this));
        }
    }

    private void refreshDock() {
        if (dockView != null && dockView.getParent() == rootLayout) {
            rootLayout.removeView(dockView);
        }
        dockView = null;
        createDock();
    }

    // Music dock buttons placement relative to the title
    public static final int MUSIC_DOCK_BUTTON_TOP = 0;
    public static final int MUSIC_DOCK_BUTTON_RIGHT = 1;
    public static final int MUSIC_DOCK_BUTTON_BOTTOM = 2;
    public static final int MUSIC_DOCK_BUTTON_LEFT = 3;

    private void createMusicDock() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("music_dock_enabled", 0) != 1 || !hasNotificationListenerAccess()) {
            return;
        }
        int position = prefs.getInt("music_dock_position", 7);
        int iconSizeDp = prefs.getInt("music_dock_icon_size", 20);
        int textSizeSp = prefs.getInt("music_dock_text_size", 16);
        boolean vertical = prefs.getInt("music_dock_orientation", 0) == 1;
        int buttonPos = prefs.getInt("music_dock_button_position", -1);
        if (buttonPos < MUSIC_DOCK_BUTTON_TOP || buttonPos > MUSIC_DOCK_BUTTON_LEFT) {
            buttonPos = vertical ? MUSIC_DOCK_BUTTON_BOTTOM : MUSIC_DOCK_BUTTON_LEFT;
        }

        LinearLayout bar = new LinearLayout(this);
        bar.setId(View.generateViewId());
        // Buttons keep their own stacking axis; the bar flips when the group
        // sits beside the title instead of in line with it (two rows/columns)
        if (vertical) {
            boolean besideTitle = buttonPos == MUSIC_DOCK_BUTTON_LEFT
                    || buttonPos == MUSIC_DOCK_BUTTON_RIGHT;
            bar.setOrientation(besideTitle ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        } else {
            boolean stackedRows = buttonPos == MUSIC_DOCK_BUTTON_TOP
                    || buttonPos == MUSIC_DOCK_BUTTON_BOTTOM;
            bar.setOrientation(stackedRows ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        }
        bar.setGravity(Gravity.CENTER);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);

        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        int slotMargin = (int) (4 * density);

        ImageView prevButton = createMusicTransportButton(R.drawable.ic_media_previous, iconSizePx,
                slotMargin, vertical, "prev");
        ImageView playPauseButton = createMusicTransportButton(R.drawable.ic_media_play, iconSizePx,
                slotMargin, vertical, "play_pause");
        ImageView nextButton = createMusicTransportButton(R.drawable.ic_media_next, iconSizePx,
                slotMargin, vertical, "next");
        musicDockPlayPause = playPauseButton;

        StrokeTextView titleView = new StrokeTextView(this);
        titleView.setTextColor(resolveAppBarThemeColor(prefs.getInt("music_dock_text_color", 0), false));
        titleView.setTextSize(textSizeSp);
        FontHelper.applyStyled(this, titleView, FontHelper.SLOT_MUSIC_DOCK, boldText);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setMaxWidth((int) (160 * density));
        titleView.setGravity(Gravity.CENTER);
        titleView.setBackgroundColor(0);
        applyTextEffect(titleView, prefs.getInt("music_dock_text_effect", 0),
                getAppBarIconEffectColorValue(prefs.getInt("music_dock_text_effect_color", 0), theme));
        musicDockTitleView = titleView;

        // Transport buttons always stack along the dock's main axis
        LinearLayout buttonsGroup = new LinearLayout(this);
        buttonsGroup.setOrientation(vertical ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        buttonsGroup.setGravity(Gravity.CENTER);
        buttonsGroup.addView(prevButton);
        buttonsGroup.addView(playPauseButton);
        buttonsGroup.addView(nextButton);

        boolean buttonsFirst = buttonPos == MUSIC_DOCK_BUTTON_TOP
                || buttonPos == MUSIC_DOCK_BUTTON_LEFT;

        int lineHeight = 0;
        int stripLength = 0;
        View titleSlot;
        if (vertical) {
            lineHeight = (int) Math.ceil(titleView.getPaint().getFontSpacing())
                    + getMusicDockTitleCrossExtra();
            stripLength = (int) (160 * density);
            FrameLayout titleContainer = new FrameLayout(this);
            titleContainer.setClipChildren(false);
            titleContainer.setClipToPadding(false);
            FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(
                    stripLength, lineHeight, Gravity.CENTER);
            titleView.setLayoutParams(innerParams);
            titleView.setRotation(270f);
            titleContainer.addView(titleView);
            titleSlot = titleContainer;
            musicDockTitleSlot = titleContainer;
        } else {
            titleSlot = titleView;
            musicDockTitleSlot = titleView;
        }

        LinearLayout.LayoutParams buttonsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // The container must match the rotated strip footprint (narrow and
        // tall), not the unrotated TextView size, or neighbors will hug or
        // overlap the visible strip
        LinearLayout.LayoutParams titleParams = vertical
                ? new LinearLayout.LayoutParams(lineHeight, stripLength)
                : new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        int titleGap;
        if (vertical) {
            titleGap = bar.getOrientation() == LinearLayout.HORIZONTAL ? slotMargin * 2 : 0;
        } else {
            titleGap = slotMargin;
        }
        if (bar.getOrientation() == LinearLayout.HORIZONTAL) {
            if (buttonsFirst) titleParams.leftMargin = titleGap;
            else titleParams.rightMargin = titleGap;
        } else {
            if (buttonsFirst) titleParams.topMargin = titleGap;
            else titleParams.bottomMargin = titleGap;
        }
        if (buttonsFirst) {
            bar.addView(buttonsGroup, buttonsParams);
            bar.addView(titleSlot, titleParams);
        } else {
            bar.addView(titleSlot, titleParams);
            bar.addView(buttonsGroup, buttonsParams);
        }
        if (vertical) {
            updateMusicDockTitleStripSize();
        }

        boolean borderEnabled = prefs.getInt("music_dock_border", 0) == 1;
        boolean bgEnabled = prefs.getInt("music_dock_background", 0) == 1;
        if (borderEnabled || bgEnabled) {
            android.graphics.drawable.GradientDrawable barBg = new android.graphics.drawable.GradientDrawable();
            barBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            if (borderEnabled) {
                barBg.setStroke((int) (2 * density),
                        resolveAppBarThemeColor(prefs.getInt("music_dock_border_color", 0), false));
            }
            barBg.setColor(bgEnabled
                    ? resolveAppBarThemeColor(prefs.getInt("music_dock_background_color", 0), true)
                    : android.graphics.Color.TRANSPARENT);
            barBg.setCornerRadius(DialogEffectHelper.getCornerRadiusPx(this));
            bar.setBackground(barBg);
            int barPad = (int) (6 * density);
            bar.setPadding(barPad, barPad, barPad, barPad);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) (8 * density);
        boolean aboveBottomDock = false;
        switch (position) {
            case 0:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 1:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 2:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 3:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 4:
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 5:
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case 6:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 7:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                if (dockView != null && dockView.getParent() == rootLayout) {
                    params.addRule(RelativeLayout.ABOVE, dockView.getId());
                    aboveBottomDock = true;
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                break;
            default:
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                break;
        }

        updatePaddingPx();
        boolean topAnchored = position == 0 || position == 1 || position == 6;
        boolean bottomAnchored = (position == 2 || position == 3 || position == 7) && !aboveBottomDock;
        params.leftMargin = margin + homePaddingLeftPx;
        params.rightMargin = margin + homePaddingRightPx;
        params.topMargin = margin + homePaddingTopPx + (topAnchored ? statusBarInset : 0);
        params.bottomMargin = margin + homePaddingBottomPx + (bottomAnchored ? navBarInset : 0);

        rootLayout.addView(bar, params);
        musicDockView = bar;
        bar.setVisibility(View.GONE);

        if (bgEnabled) {
            DockBackdropHelper.applyBackdrop(bar, rootLayout, prefs, "music_dock",
                    resolveAppBarThemeColor(prefs.getInt("music_dock_background_color", 0), true),
                    borderEnabled,
                    resolveAppBarThemeColor(prefs.getInt("music_dock_border_color", 0), false),
                    2 * density,
                    DialogEffectHelper.getCornerRadiusPx(this));
        }
    }

    private ImageView createMusicTransportButton(int drawableRes, int sizePx, int margin,
            boolean vertical, String action) {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
        if (vertical) {
            lp.topMargin = margin;
            lp.bottomMargin = margin;
        } else {
            lp.leftMargin = margin;
            lp.rightMargin = margin;
        }
        iv.setLayoutParams(lp);
        iv.setTag(action);
        applyMusicTransportIcon(iv, drawableRes);
        return iv;
    }

    private int getMusicDockTitleCrossExtra() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        float density = getResources().getDisplayMetrics().density;
        float extraDp = 2f;
        if (prefs.getInt("music_dock_text_effect", 0) != 0) {
            extraDp += 4f;
        }
        return (int) Math.ceil(extraDp * density);
    }

    private void updateMusicDockTitleStripSize() {
        if (!(musicDockTitleView instanceof StrokeTextView)
                || !(musicDockTitleSlot instanceof FrameLayout)) {
            return;
        }
        StrokeTextView titleView = (StrokeTextView) musicDockTitleView;
        CharSequence text = titleView.getText();
        float density = getResources().getDisplayMetrics().density;
        int maxLength = (int) (160 * density);
        int lineHeight = (int) Math.ceil(titleView.getPaint().getFontSpacing())
                + getMusicDockTitleCrossExtra();
        int textLength = 0;
        if (text != null && text.length() > 0) {
            titleView.measure(
                    View.MeasureSpec.makeMeasureSpec(maxLength, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textLength = Math.max(1, Math.min(titleView.getMeasuredWidth(), maxLength));
        }
        FrameLayout.LayoutParams innerParams = (FrameLayout.LayoutParams) titleView.getLayoutParams();
        innerParams.width = textLength;
        innerParams.height = lineHeight;
        titleView.setLayoutParams(innerParams);
        ViewGroup.LayoutParams slotParams = musicDockTitleSlot.getLayoutParams();
        slotParams.width = lineHeight;
        slotParams.height = textLength;
        musicDockTitleSlot.setLayoutParams(slotParams);
    }

    private void applyMusicTransportIcon(ImageView iv, int drawableRes) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean iconBackgroundEnabled = prefs.getBoolean("music_dock_icon_background", true);
        int iconShape = prefs.getInt("music_dock_icon_shape", IconShapeHelper.SHAPE_SYSTEM);
        Drawable icon = DynamicIconHelper.createSpecialIcon(this, drawableRes, theme,
                iconBackgroundEnabled, false, false, iconShape);
        iv.setImageDrawable(icon);
        iv.clearColorFilter();
        int effect = prefs.getInt("music_dock_icon_effect", 0);
        int effectColor = prefs.getInt("music_dock_icon_effect_color", 0);
        applyAppBarIconEffect(iv, effect, effectColor, iconBackgroundEnabled);
    }

    private void refreshMusicDock() {
        if (musicDockView != null && musicDockView.getParent() == rootLayout) {
            rootLayout.removeView(musicDockView);
        }
        musicDockView = null;
        musicDockTitleView = null;
        musicDockTitleSlot = null;
        musicDockPlayPause = null;
        createMusicDock();
        updateMusicDockContent();
    }

    private boolean hasNotificationListenerAccess() {
        String flat = android.provider.Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (flat != null) {
            String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && getPackageName().equals(cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateServiceComponents() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean musicEnabled = prefs.getInt("music_dock_enabled", 0) == 1;
        boolean lockEnabled = prefs.getInt("double_tap_lock", 0) == 1;
        setComponentEnabled(MusicNotificationListenerService.class, musicEnabled);
        setComponentEnabled(LockAccessibilityService.class, lockEnabled);
    }

    private void setComponentEnabled(Class<?> clazz, boolean enabled) {
        try {
            PackageManager pm = getPackageManager();
            ComponentName cn = new ComponentName(this, clazz);
            int current = pm.getComponentEnabledSetting(cn);
            int expected = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            // Only change if needed to avoid unnecessary writes (DEFAULT means enabled via manifest)
            if (current == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                if (!enabled) {
                    pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
            } else if (current != expected) {
                pm.setComponentEnabledSetting(cn, expected, PackageManager.DONT_KILL_APP);
            }
        } catch (Exception ignored) {}
    }

    private void startMusicMonitoring() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("music_dock_enabled", 0) != 1 || !hasNotificationListenerAccess()) {
            stopMusicMonitoring();
            return;
        }
        try {
            if (mediaSessionManager == null) {
                mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            }
            if (mediaSessionManager == null) return;
            ComponentName listenerComponent = new ComponentName(this, MusicNotificationListenerService.class);
            if (mediaSessionsListener == null) {
                mediaSessionsListener = controllers -> handleActiveSessions(controllers);
                mediaSessionManager.addOnActiveSessionsChangedListener(mediaSessionsListener, listenerComponent);
            }
            handleActiveSessions(mediaSessionManager.getActiveSessions(listenerComponent));
        } catch (Exception e) {
            // Notification listener access may have been revoked
        }
    }

    private void stopMusicMonitoring() {
        if (mediaSessionManager != null && mediaSessionsListener != null) {
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(mediaSessionsListener);
            } catch (Exception e) {
                // Already removed
            }
        }
        mediaSessionsListener = null;
        detachMediaControllerCallback();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("music_dock_enabled", 0) != 1 || !hasNotificationListenerAccess()) {
            hideMusicDock();
        } else {
            scheduleMusicDockHide();
        }
    }

    private void detachMediaControllerCallback() {
        if (activeMediaController != null && mediaControllerCallback != null) {
            try {
                activeMediaController.unregisterCallback(mediaControllerCallback);
            } catch (Exception e) {
                // Session already released
            }
        }
        activeMediaController = null;
    }

    private void handleActiveSessions(List<MediaController> controllers) {
        detachMediaControllerCallback();
        MediaController best = null;
        int bestRank = 0;
        if (controllers != null) {
            for (int i = 0; i < controllers.size(); i++) {
                MediaController candidate = controllers.get(i);
                PlaybackState playbackState = candidate.getPlaybackState();
                if (playbackState == null) continue;
                int state = playbackState.getState();
                int rank;
                if (state == PlaybackState.STATE_PLAYING) rank = 2;
                else if (state == PlaybackState.STATE_PAUSED) rank = 1;
                else continue;
                if (rank > bestRank) {
                    best = candidate;
                    bestRank = rank;
                }
            }
        }
        if (best == null) {
            setMusicDockTitle(null);
            scheduleMusicDockHide();
            return;
        }
        activeMediaController = best;
        if (mediaControllerCallback == null) {
            mediaControllerCallback = new MediaController.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackState state) {
                    updateMusicDockContent();
                }

                @Override
                public void onMetadataChanged(MediaMetadata metadata) {
                    updateMusicDockContent();
                }
            };
        }
        try {
            best.registerCallback(mediaControllerCallback);
        } catch (Exception e) {
            // Ignore registration failures
        }
        updateMusicDockContent();
    }

    private void updateMusicDockContent() {
        if (musicDockView == null) return;
        MediaController controller = activeMediaController;
        PlaybackState playbackState = controller != null ? controller.getPlaybackState() : null;
        int state = playbackState != null ? playbackState.getState() : PlaybackState.STATE_NONE;
        boolean playing = state == PlaybackState.STATE_PLAYING;
        boolean paused = state == PlaybackState.STATE_PAUSED;
        if (controller == null || (!playing && !paused)) {
            setMusicDockTitle(null);
            scheduleMusicDockHide();
            return;
        }
        String title = null;
        MediaMetadata metadata = controller.getMetadata();
        if (metadata != null) {
            CharSequence text = metadata.getText(MediaMetadata.METADATA_KEY_TITLE);
            if (text != null && text.toString().trim().length() > 0) {
                title = text.toString();
            }
        }
        if (title == null || title.isEmpty()) {
            try {
                PackageManager pm = getPackageManager();
                CharSequence label = pm.getApplicationLabel(
                        pm.getApplicationInfo(controller.getPackageName(), 0));
                if (label != null) {
                    title = label.toString();
                }
            } catch (Exception e) {
                title = "";
            }
        }
        setMusicDockTitle(title);
        if (musicDockPlayPause != null) {
            applyMusicTransportIcon(musicDockPlayPause,
                    playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
        }
        if (playing) {
            showMusicDock();
        } else {
            scheduleMusicDockHide();
        }
    }

    /**
     * Show the track title, or collapse its slot entirely so the dock shrinks
     * to just the transport buttons when there is nothing playing
     */
    private void setMusicDockTitle(String title) {
        boolean hasTitle = title != null && !title.isEmpty();
        if (musicDockTitleView != null) {
            musicDockTitleView.setText(hasTitle ? title : "");
        }
        if (musicDockTitleSlot != null && musicDockTitleSlot.getVisibility()
                != (hasTitle ? View.VISIBLE : View.GONE)) {
            musicDockTitleSlot.setVisibility(hasTitle ? View.VISIBLE : View.GONE);
        }
        if (hasTitle) {
            updateMusicDockTitleStripSize();
        }
    }

    private void showMusicDockOptionsDialog() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean alwaysActive = prefs.getInt("music_dock_keep_active", 0) == 1;
        new MusicDockOptionsDialog(this, alwaysActive, newValue -> {
            prefs.edit().putInt("music_dock_keep_active", newValue ? 1 : 0).apply();
            if (newValue) {
                showMusicDock();
            } else {
                scheduleMusicDockHide();
            }
        }, () -> {
            boolean animate = prefs.getInt("screen_animations", 0) == 1;
            Intent intent = new Intent(MainActivity.this, MusicDockSettingsActivity.class);
            intent.putExtra(MusicDockSettingsActivity.EXTRA_FROM_HOME, true);
            if (!animate) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            startActivity(intent);
            if (animate) {
                overridePendingTransition(R.anim.dialog_fade_in, 0);
            }
        }).show();
    }

    private void showMusicDock() {
        cancelMusicDockHide();
        if (musicDockView != null && musicDockView.getVisibility() != View.VISIBLE) {
            musicDockView.setVisibility(View.VISIBLE);
            DockBackdropHelper.reapply(musicDockView);
            EinkRefreshHelper.refreshEink(getWindow(),
                    getSharedPreferences("prefs", MODE_PRIVATE),
                    getSharedPreferences("prefs", MODE_PRIVATE).getInt("eink_refresh_delay", 100));
        }
    }

    private void hideMusicDock() {
        cancelMusicDockHide();
        setMusicDockTitle(null);
        if (musicDockView != null && musicDockView.getVisibility() != View.GONE) {
            musicDockView.setVisibility(View.GONE);
            EinkRefreshHelper.refreshEink(getWindow(),
                    getSharedPreferences("prefs", MODE_PRIVATE),
                    getSharedPreferences("prefs", MODE_PRIVATE).getInt("eink_refresh_delay", 100));
        }
    }

    private void cancelMusicDockHide() {
        if (musicDockHideRunnable != null) {
            musicDockHideHandler.removeCallbacks(musicDockHideRunnable);
        }
    }

    private void scheduleMusicDockHide() {
        cancelMusicDockHide();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getInt("music_dock_keep_active", 0) == 1) {
            showMusicDock();
            return;
        }
        if (musicDockView == null) return;
        PlaybackState state = activeMediaController != null
                ? activeMediaController.getPlaybackState() : null;
        if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
            showMusicDock();
            return;
        }
        int delaySeconds = prefs.getInt("music_dock_hide_delay", 0);
        if (musicDockHideRunnable == null) {
            musicDockHideRunnable = () -> {
                SharedPreferences p = getSharedPreferences("prefs", MODE_PRIVATE);
                if (p.getInt("music_dock_keep_active", 0) == 1) {
                    return;
                }
                PlaybackState s = activeMediaController != null
                        ? activeMediaController.getPlaybackState() : null;
                boolean playing = s != null && s.getState() == PlaybackState.STATE_PLAYING;
                if (!playing) {
                    hideMusicDock();
                }
            };
        }
        if (delaySeconds <= 0) {
            musicDockHideRunnable.run();
        } else {
            musicDockHideHandler.postDelayed(musicDockHideRunnable, delaySeconds * 1000L);
        }
    }

    private void launchPlayingApp() {
        if (activeMediaController == null) return;
        launchApp(activeMediaController.getPackageName());
    }

    private void musicSkipToPrevious() {        if (activeMediaController != null) {
            try {
                activeMediaController.getTransportControls().skipToPrevious();
            } catch (Exception e) {
                // Ignore transport errors
            }
        }
    }

    private void musicSkipToNext() {
        if (activeMediaController != null) {
            try {
                activeMediaController.getTransportControls().skipToNext();
            } catch (Exception e) {
                // Ignore transport errors
            }
        }
    }

    private void musicTogglePlayPause() {
        if (activeMediaController == null) return;
        PlaybackState playbackState = activeMediaController.getPlaybackState();
        boolean playing = playbackState != null
                && playbackState.getState() == PlaybackState.STATE_PLAYING;
        try {
            if (playing) {
                activeMediaController.getTransportControls().pause();
            } else {
                activeMediaController.getTransportControls().play();
            }
        } catch (Exception e) {
            // Ignore transport errors
        }
    }

    private void adjustMainLayoutPosition() {
        RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        View topView = null;
        if (timePosition == 1 || datePosition != 0) {
            if (timePosition == 1 && datePosition != 0) {
                if (dateVerticalPosition == 0) {
                    topView = timeView;
                } else {
                    topView = calendarEventView != null ? calendarEventView : dateView;
                }
            } else if (timePosition == 1) {
                topView = timeView;
            } else if (datePosition != 0) {
                topView = calendarEventView != null ? calendarEventView : dateView;
            }
        }
        // If every visible clock/date/widget item is pinned to one edge and the app grid is
        // pinned to the other, there is free horizontal space beside the stack: let the grid sit
        // there instead of below it. getSideBySideStackSide() returns -1 whenever that isn't
        // safely true (mixed/centered stack, apps centered, multi-column grid, etc).
        int stackSide = getSideBySideStackSide();
        if (stackSide != -1) {
            mainParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            mainParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mainParams.addRule(homeAlignment == 0 ? RelativeLayout.ALIGN_PARENT_LEFT : RelativeLayout.ALIGN_PARENT_RIGHT);
            mainLayout.setLayoutParams(mainParams);
            checkSideBySideFits(stackSide);
            return;
        }
        // homeContentBottomId is the trailing id of the whole clock/date + home-widgets stack (see
        // createHomeWidgets/HomeWidgetHost.createAll) - preferred over topView so the app grid sits
        // below any enabled widgets instead of overlapping them.
        if (homeContentBottomId != View.NO_ID) {
            mainParams.addRule(RelativeLayout.BELOW, homeContentBottomId);
        } else if (topView != null) {
            mainParams.addRule(RelativeLayout.BELOW, topView.getId());
        } else if (showSettingsButton == 1 || showSearchButton == 1) {
            if (showSearchButton == 1) {
                mainParams.addRule(RelativeLayout.BELOW, searchButton.getId());
            } else {
                mainParams.addRule(RelativeLayout.BELOW, settingsButton.getId());
            }
        } else {
            mainParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        mainLayout.setLayoutParams(mainParams);
    }

    /**
     * Returns the single horizontal side (0=left, 2=right) that every currently visible
     * clock/date/widget item is pinned to, provided it's the opposite side from the app grid's own
     * homeAlignment - or -1 if side-by-side placement isn't safely applicable (nothing in the
     * stack, the stack is mixed/centered, the apps are centered, or a weighted multi-column grid
     * that needs mainLayout's real width to divide into columns).
     */
    private int getSideBySideStackSide() {
        if (homeColumns > 1) return -1;
        if (homeAlignment != 0 && homeAlignment != 2) return -1;

        boolean showTime = timePosition == 1;
        boolean showDate = datePosition != 0;
        boolean showCalendarEvents = dateCalendarEvents == 1 && showDate && hasCalendarPermission();

        Integer stackSide = null;
        List<Integer> positions = new ArrayList<>();
        if (showTime) positions.add(timeHorizontalPosition);
        if (showDate) positions.add(dateHorizontalPosition);
        if (showCalendarEvents) positions.add(dateHorizontalPosition);
        positions.addAll(homeWidgetHost.getVisibleHorizontalPositions());

        for (int pos : positions) {
            if (stackSide == null) {
                stackSide = pos;
            } else if (stackSide != pos) {
                return -1;
            }
        }
        if (stackSide == null || stackSide == 1 || stackSide == homeAlignment) return -1;
        return stackSide;
    }

    /**
     * After layout settles, verifies the side-by-side placement set up in adjustMainLayoutPosition
     * didn't actually collide with the stack (e.g. a long "now reading" title or large clock font
     * pushing past the grid's edge) and falls back to stacking below it if so.
     */
    private void checkSideBySideFits(int stackSide) {
        rootLayout.post(() -> {
            if (mainLayout == null || !(mainLayout.getLayoutParams() instanceof RelativeLayout.LayoutParams)) return;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mainLayout.getLayoutParams();
            if (lp.width != RelativeLayout.LayoutParams.WRAP_CONTENT) return; // already rebuilt/fell back

            List<View> stackViews = new ArrayList<>();
            if (timeView != null && timeView.getParent() == rootLayout) stackViews.add(timeView);
            if (dateView != null && dateView.getParent() == rootLayout) stackViews.add(dateView);
            if (calendarEventView != null && calendarEventView.getParent() == rootLayout) stackViews.add(calendarEventView);
            stackViews.addAll(homeWidgetHost.getVisibleViews());

            Integer stackEdge = null;
            for (View v : stackViews) {
                if (v.getWidth() == 0) continue;
                int edge = stackSide == 0 ? v.getRight() : v.getLeft();
                if (stackEdge == null) {
                    stackEdge = edge;
                } else {
                    stackEdge = stackSide == 0 ? Math.max(stackEdge, edge) : Math.min(stackEdge, edge);
                }
            }
            if (stackEdge == null) return;

            int gapPx = (int) (12 * getResources().getDisplayMetrics().density);
            int gridEdge = stackSide == 0 ? mainLayout.getLeft() : mainLayout.getRight();
            boolean fits = stackSide == 0 ? (gridEdge - stackEdge >= gapPx) : (stackEdge - gridEdge >= gapPx);
            if (!fits) {
                RelativeLayout.LayoutParams fallback = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                if (homeContentBottomId != View.NO_ID) {
                    fallback.addRule(RelativeLayout.BELOW, homeContentBottomId);
                }
                mainLayout.setLayoutParams(fallback);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsBackupHelper.repairFloatKeys(this);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (!prefs.contains("theme")) {
            SettingsBackupHelper.applyInitialDefaults(this);
        }
        theme = prefs.getInt("theme", 0);
        if (ThemeUtils.isDarkTheme(theme, this)) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        // Started before inflation so the appfilter parse overlaps it and the
        // first draw already has themed icons.
        iconPack = IconPackHelper.getSelectedPack(this);
        iconEpoch = DynamicIconHelper.getCacheEpoch();
        // Large packs take seconds to parse, so the first draw shows stock
        // icons; repaint once when the map lands rather than blocking on it.
        IconPackHelper.setLoadListener(() -> runOnUiThread(() -> {
            DynamicIconHelper.bumpCacheEpoch();
            iconEpoch = DynamicIconHelper.getCacheEpoch();
            recreateHome();
        }));
        IconPackHelper.ensureLoadedAsync(this);
        BigmeShims.registerUnlockReceiver(this);
        BigmeShims.queryLauncherProvider(this);
        setContentView(R.layout.activity_main);

        try {
            OnyxHelper.showFreezeNoticeIfNeeded(this);
        } catch (Exception ignored) {}

        WallpaperHelper.warmCacheAsync(this);

        handler = new Handler(Looper.getMainLooper());

        int bgColor = ThemeUtils.getBgColor(theme, this);
        this.textColor = ThemeUtils.getTextColor(theme, this);
        this.hasWallpaper = WallpaperHelper.hasWallpaper(this);
        SharedPreferences initPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
        this.wallpaperOffsetX = initPrefs.getFloat("wallpaper_offset_x", 0.5f);
        this.wallpaperOffsetY = initPrefs.getFloat("wallpaper_offset_y", 0.5f);
        this.wallpaperScale = initPrefs.getFloat("wallpaper_scale", 1f);
        this.wallpaperFileModified = initPrefs.getLong("wallpaper_file_modified", 0L);

        applyWindowLayoutMode(this.hasWallpaper, bgColor);
        maxApps = prefs.getInt("max_apps", 4);
        textSize = prefs.getInt("text_size", 32);
        iconSize = prefs.getInt("icon_size", 32);
        boldText = prefs.getBoolean("bold_text", true);
        appTextColor = prefs.getInt("app_text_color", 0);
        textEffect = prefs.getInt("text_effect", 0);
        effectColor = prefs.getInt("effect_color", 0);
        iconEffect = prefs.getInt("icon_effect", 0);
        iconEffectColor = prefs.getInt("icon_effect_color", 0);
        showIcons = prefs.getBoolean("show_icons", false);
        showAppNames = prefs.getBoolean("show_app_names", true);
        if (!showIcons)
            showAppNames = true;
        appNamePosition = prefs.getInt("app_name_position", AppNamePositionHelper.POSITION_RIGHT);
        monochromeIcons = prefs.getBoolean("monochrome_icons", false);
        dynamicIcons = prefs.getBoolean("dynamic_icons", false);
        forceMonochromeFallback = prefs.getBoolean("force_monochrome_fallback", false);
        dynamicColors = prefs.getBoolean("dynamic_colors", false);
        invertIconColors = prefs.getBoolean("invert_icon_colors", false);
        invertHomeColors = prefs.getBoolean("invert_home_colors", false);
        iconBackground = prefs.getBoolean("icon_background", true);
        iconShape = prefs.getInt("icon_shape", IconShapeHelper.SHAPE_SYSTEM);
        homePosition = prefs.getInt("home_position", -1);
        if (homePosition < 0 || homePosition > 8) {
            homePosition = HomePositionHelper.positionFromAlignment(
                    prefs.getInt("home_vertical_alignment", 1), prefs.getInt("home_alignment", 1));
            prefs.edit().putInt("home_position", homePosition).apply();
        }
        homeAlignment = HomePositionHelper.horizontalFromPosition(homePosition);
        homeVerticalAlignment = HomePositionHelper.verticalFromPosition(homePosition);
        homeColumns = prefs.getInt("home_columns", 1);
        if (homeColumns < 1) homeColumns = 1;
        if (homeColumns > 10) homeColumns = 10;
        homePages = prefs.getInt("home_pages", 1);
        hidePagination = prefs.getBoolean("hide_pagination", false);
        timePosition = prefs.getInt("time_position", 0);
        timeFormat24h = prefs.getInt("time_format_24h", 1);
        dateFormat = prefs.contains("date_format") ? prefs.getInt("date_format", 0)
                : (prefs.getInt("full_month_name", 0) == 1 ? 1 : 0);
        dateVerticalPosition = prefs.getInt("date_vertical_position", 0);
        clockDateGap = prefs.getInt("clock_date_gap", 0);
        datePosition = prefs.getInt("date_position", 0);
        dateHorizontalPosition = prefs.getInt("date_horizontal_position", 0);
        dateCalendarEvents = prefs.getInt("date_calendar_events", 0);
        timeHorizontalPosition = prefs.getInt("time_horizontal_position", 0);
        timeFontSize = prefs.getInt("time_font_size", 54);
        timeColor = prefs.getInt("time_color", 0);
        timeEffect = prefs.getInt("time_effect", 0);
        timeEffectColor = prefs.getInt("time_effect_color", 0);
        dateFontSize = prefs.getInt("date_font_size", 22);
        calendarEventFontSize = prefs.getInt("calendar_event_font_size", 16);
        dateColor = prefs.getInt("date_color", 0);
        dateEffect = prefs.getInt("date_effect", 0);
        dateEffectColor = prefs.getInt("date_effect_color", 0);
        homePaddingTop = prefs.getInt("home_padding_top", 0);
        homePaddingBottom = prefs.getInt("home_padding_bottom", 0);
        homePaddingLeft = prefs.getInt("home_padding_left", 0);
        homePaddingRight = prefs.getInt("home_padding_right", 0);
        updatePaddingPx();
        fullMonthName = prefs.getInt("full_month_name", 0);
        doubleTapLock = prefs.getInt("double_tap_lock", 0);
        showSettingsButton = prefs.getInt("show_settings_button", 0);
        showSearchButton = prefs.getInt("show_search_button", 0);
        clockAppPkg = prefs.getString("clock_app_pkg", "system_default");
        dateAppPkg = prefs.getString("date_app_pkg", "system_default");
        calendarPermissionGranted = hasCalendarPermission();
        settingsButtonSize = prefs.getInt("settings_button_size", 42);
        settingsButtonColor = prefs.getInt("settings_button_color", 0);
        settingsButtonEffect = prefs.getInt("settings_button_effect", 0);
        settingsButtonEffectColor = prefs.getInt("settings_button_effect_color", 0);
        searchButtonSize = prefs.getInt("search_button_size", 42);
        searchButtonColor = prefs.getInt("search_button_color", 0);
        searchButtonEffect = prefs.getInt("search_button_effect", 0);
        searchButtonEffectColor = prefs.getInt("search_button_effect_color", 0);
        customBgColor = prefs.getInt("custom_bg_color", android.graphics.Color.WHITE);
        customAccentColor = prefs.getInt("custom_accent_color", android.graphics.Color.BLACK);

        appLabels = new ArrayList<>();
        appPackages = new ArrayList<>();
        int totalApps = homeColumns * maxApps;
        appSlots = new LinearLayout[totalApps];

        homePagesManager = new HomePagesManager(this, prefs, homePages, homeColumns, maxApps);
        homePagesManager.loadAppsForCurrentPage();
        appLabels.addAll(homePagesManager.getAppLabels());
        appPackages.addAll(homePagesManager.getAppPackages());

        this.rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        rootLayout.setVisibility(View.INVISIBLE);
        rootLayout.setBackgroundColor(bgColor);
        rootLayout.setClipChildren(false);
        rootLayout.setClipToPadding(false);
        this.mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setClipChildren(false);
        mainLayout.setClipToPadding(false);

        wallpaperView = findViewById(R.id.wallpaper_view);
        loadWallpaper();

        TextView pageIndicator = findViewById(R.id.page_indicator);
        pageIndicator.setTextColor(getPaginationColorValue());
        homePagesManager.setPageIndicator(pageIndicator);
        homePagesManager.updatePageIndicator();
        pageIndicator.setVisibility((homePages > 1 && !hidePagination) ? View.VISIBLE : View.GONE);

        LinearLayout bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility((homePages > 1 && !hidePagination) ? View.VISIBLE : View.GONE);
        updateGravity();

        ImageView prevButton = findViewById(R.id.prev_page_button);
        prevButton.setColorFilter(getPaginationColorValue());
        prevButton.setOnClickListener(v -> {
            int currentPage = homePagesManager.getCurrentPage();
            int newPage = currentPage > 0 ? currentPage - 1 : homePages - 1;
            homePagesManager.setCurrentPage(newPage);
            recreateHome();
        });

        ImageView nextButton = findViewById(R.id.next_page_button);
        nextButton.setColorFilter(getPaginationColorValue());
        nextButton.setOnClickListener(v -> {
            int currentPage = homePagesManager.getCurrentPage();
            int newPage = currentPage < homePages - 1 ? currentPage + 1 : 0;
            homePagesManager.setCurrentPage(newPage);
            recreateHome();
        });

        createHomeWidgets(bgColor, textColor);

        createSettingsButton(bgColor, textColor);
        createSearchButton(bgColor, textColor);

        createAppBar();
        createDock();
        createMusicDock();

        adjustMainLayoutPosition();

        createHomeLayout();

        gestureHandler = new GestureHandler();
        findViewById(R.id.root_layout).setOnTouchListener(gestureHandler::onTouch);
        findViewById(R.id.root_layout).setClickable(true);

        customGestureLibrary = GestureLibraries.fromFile(new java.io.File(getFilesDir(), "custom_gestures"));
        customGestureLibrary.load();

        prefsChangeListener = (sharedPreferences, key) -> prefsDirty = true;
        getSharedPreferences("prefs", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(prefsChangeListener);
        homeWidgetHost.snapshotPrefs(getSharedPreferences("prefs", MODE_PRIVATE));
        homeWidgetHost.loadAllAsync(this);
        updateServiceComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(homeButtonReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"),
                Context.RECEIVER_NOT_EXPORTED);
        gestureHandler.loadApps();
        dropIconPackIfUninstalled();
        if (customGestureLibrary != null) {
            customGestureLibrary = GestureLibraries.fromFile(new java.io.File(getFilesDir(), "custom_gestures"));
            customGestureLibrary.load();
        }

        if (prefsDirty) {
            prefsDirty = false;

            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

            String newIconPack = prefs.getString(IconPackHelper.PREF_ICON_PACK, "");
            if (!newIconPack.equals(iconPack)) {
                iconPack = newIconPack;
                IconPackHelper.invalidate();
                IconPackHelper.ensureLoadedAsync(this);
                DynamicIconHelper.bumpCacheEpoch();
            }
            // A pack switch or a per-app override changes the artwork without
            // changing any pref this block compares, so the epoch is what tells
            // us to repaint.
            boolean iconArtworkChanged = DynamicIconHelper.getCacheEpoch() != iconEpoch;
            iconEpoch = DynamicIconHelper.getCacheEpoch();

            int newMaxApps = prefs.getInt("max_apps", 4);
            int newTextSize = prefs.getInt("text_size", 32);
            int newIconSize = prefs.getInt("icon_size", 32);
            boolean newBoldText = prefs.getBoolean("bold_text", true);
            int newAppTextColor = prefs.getInt("app_text_color", 0);
            int newTextEffect = prefs.getInt("text_effect", 0);
            int newEffectColor = prefs.getInt("effect_color", 0);
            int newIconEffect = prefs.getInt("icon_effect", 0);
            int newIconEffectColor = prefs.getInt("icon_effect_color", 0);
            int newHomePosition = prefs.getInt("home_position", -1);
            if (newHomePosition < 0 || newHomePosition > 8) {
                newHomePosition = HomePositionHelper.positionFromAlignment(
                        prefs.getInt("home_vertical_alignment", 1), prefs.getInt("home_alignment", 1));
            }
            int newHomeAlignment = HomePositionHelper.horizontalFromPosition(newHomePosition);
            int newHomeVerticalAlignment = HomePositionHelper.verticalFromPosition(newHomePosition);
            int newHomeColumns = prefs.getInt("home_columns", 1);
            if (newHomeColumns < 1) newHomeColumns = 1;
            if (newHomeColumns > 10) newHomeColumns = 10;
            int newHomePages = prefs.getInt("home_pages", 1);
            boolean newHidePagination = prefs.getBoolean("hide_pagination", false);
            int newTimePosition = prefs.getInt("time_position", 0);
            int newTimeFormat24h = prefs.getInt("time_format_24h", 1);
            int newDateVerticalPosition = prefs.getInt("date_vertical_position", 0);
            int newClockDateGap = prefs.getInt("clock_date_gap", 0);
            int newDatePosition = prefs.getInt("date_position", 0);
            int newSettingsButtonSize = prefs.getInt("settings_button_size", 42);
            int newSettingsButtonColor = prefs.getInt("settings_button_color", 0);
            int newSettingsButtonEffect = prefs.getInt("settings_button_effect", 0);
            int newSettingsButtonEffectColor = prefs.getInt("settings_button_effect_color", 0);
            int newSearchButtonSize = prefs.getInt("search_button_size", 42);
            int newSearchButtonColor = prefs.getInt("search_button_color", 0);
            int newSearchButtonEffect = prefs.getInt("search_button_effect", 0);
            int newSearchButtonEffectColor = prefs.getInt("search_button_effect_color", 0);
            int newDateHorizontalPosition = prefs.getInt("date_horizontal_position", 0);
            int newTimeHorizontalPosition = prefs.getInt("time_horizontal_position", 0);
            int newTimeFontSize = prefs.getInt("time_font_size", 54);
            int newTimeColor = prefs.getInt("time_color", 0);
            int newTimeEffect = prefs.getInt("time_effect", 0);
            int newTimeEffectColor = prefs.getInt("time_effect_color", 0);
            int newDateFontSize = prefs.getInt("date_font_size", 22);
            int newDateColor = prefs.getInt("date_color", 0);
            int newDateEffect = prefs.getInt("date_effect", 0);
            int newDateEffectColor = prefs.getInt("date_effect_color", 0);
            int newDateCalendarEvents = prefs.getInt("date_calendar_events", 0);
            int newCalendarEventFontSize = prefs.getInt("calendar_event_font_size", 16);
            int newHomePaddingTop = prefs.getInt("home_padding_top", 0);
            int newHomePaddingBottom = prefs.getInt("home_padding_bottom", 0);
            int newHomePaddingLeft = prefs.getInt("home_padding_left", 0);
            int newHomePaddingRight = prefs.getInt("home_padding_right", 0);
            int newDateFormat = prefs.contains("date_format") ? prefs.getInt("date_format", 0)
                    : (prefs.getInt("full_month_name", 0) == 1 ? 1 : 0);
            int newFullMonthName = prefs.getInt("full_month_name", 0);
            int newBatteryInfo = prefs.getInt("battery_info", 0);
            int newBatteryPosition = prefs.getInt("battery_position", 1);
            int newTheme = prefs.getInt("theme", 0);
            int newShowSettingsButton = prefs.getInt("show_settings_button", 0);
            int newShowSearchButton = prefs.getInt("show_search_button", 0);
            String newClockAppPkg = prefs.getString("clock_app_pkg", "system_default");
            String newDateAppPkg = prefs.getString("date_app_pkg", "system_default");
            int newCustomBgColor = prefs.getInt("custom_bg_color", android.graphics.Color.WHITE);
            int newCustomAccentColor = prefs.getInt("custom_accent_color", android.graphics.Color.BLACK);
            boolean newShowIcons = prefs.getBoolean("show_icons", false);
            boolean newShowAppNames = prefs.getBoolean("show_app_names", true);
            if (!newShowIcons)
                newShowAppNames = true;
            int newAppNamePosition = prefs.getInt("app_name_position", AppNamePositionHelper.POSITION_RIGHT);
            boolean newMonochromeIcons = prefs.getBoolean("monochrome_icons", false);
            boolean newDynamicIcons = prefs.getBoolean("dynamic_icons", false);
            boolean newForceMonochromeFallback = prefs.getBoolean("force_monochrome_fallback", false);
            boolean newDynamicColors = prefs.getBoolean("dynamic_colors", false);
            boolean newInvertIconColors = prefs.getBoolean("invert_icon_colors", false);
            boolean newInvertHomeColors = prefs.getBoolean("invert_home_colors", false);
            boolean newIconBackground = prefs.getBoolean("icon_background", true);
            int newIconShape = prefs.getInt("icon_shape", IconShapeHelper.SHAPE_SYSTEM);
            boolean newCalendarPermissionGranted = hasCalendarPermission();
            boolean calendarPermissionChanged = newCalendarPermissionGranted != calendarPermissionGranted;
            int bgColor = ThemeUtils.getBgColor(newTheme, this);
            int textColor = ThemeUtils.getTextColor(newTheme, this);
            boolean newHasWallpaper = WallpaperHelper.hasWallpaper(this);
            float newWallpaperOffsetX = prefs.getFloat("wallpaper_offset_x", 0.5f);
            float newWallpaperOffsetY = prefs.getFloat("wallpaper_offset_y", 0.5f);
            float newWallpaperScale = prefs.getFloat("wallpaper_scale", 1f);
            long newWallpaperFileModified = prefs.getLong("wallpaper_file_modified", 0L);
            boolean themeChanged = newTheme != theme ||
                    (newTheme == ThemeUtils.THEME_CUSTOM
                            && (newCustomBgColor != customBgColor || newCustomAccentColor != customAccentColor));
            boolean textChanged = newTextSize != textSize || newBoldText != boldText || newAppTextColor != appTextColor
                    || newTimeFontSize != timeFontSize
                    || newTimeFormat24h != timeFormat24h
                    || newDateFontSize != dateFontSize || newDateFormat != dateFormat || newIconSize != iconSize
                    || newSettingsButtonSize != settingsButtonSize || newSearchButtonSize != searchButtonSize
                    || newTextEffect != textEffect || newEffectColor != effectColor
                    || newTimeEffect != timeEffect || newTimeEffectColor != timeEffectColor
                    || newDateEffect != dateEffect || newDateEffectColor != dateEffectColor
                    || newBatteryInfo != batteryInfo || newBatteryPosition != batteryPosition
                    || newCalendarEventFontSize != calendarEventFontSize;
            boolean iconChanged = newIconEffect != iconEffect || newIconEffectColor != iconEffectColor;
            boolean wallpaperChanged = newHasWallpaper != hasWallpaper
                    || newWallpaperOffsetX != wallpaperOffsetX || newWallpaperOffsetY != wallpaperOffsetY
                    || newWallpaperScale != wallpaperScale || newWallpaperFileModified != wallpaperFileModified;
            // Evaluated up front rather than as the tail of the || chain below: prefsChanged()
            // also advances the host's snapshot, and short-circuiting past it leaves that snapshot
            // stale, so the next resume rebuilds the home screen for a change already applied.
            boolean widgetPrefsChanged = homeWidgetHost.prefsChanged(prefs);
            boolean layoutChanged = newMaxApps != maxApps || newHomeColumns != homeColumns || newHomePages != homePages
                    || newHomeAlignment != homeAlignment || newHomeVerticalAlignment != homeVerticalAlignment
                    || newTimePosition != timePosition || newDateVerticalPosition != dateVerticalPosition
                    || newClockDateGap != clockDateGap
                    || newTimeFormat24h != timeFormat24h
                    || newDateFormat != dateFormat
                    || newDatePosition != datePosition || newDateHorizontalPosition != dateHorizontalPosition
                    || newDateCalendarEvents != dateCalendarEvents
                    || newHomePaddingTop != homePaddingTop || newHomePaddingBottom != homePaddingBottom
                    || newHomePaddingLeft != homePaddingLeft || newHomePaddingRight != homePaddingRight
                    || newTimeHorizontalPosition != timeHorizontalPosition || newFullMonthName != fullMonthName
                    || newSearchButtonEffect != searchButtonEffect || newSearchButtonEffectColor != searchButtonEffectColor
                    || newTimeEffect != timeEffect || newTimeEffectColor != timeEffectColor
                    || newDateEffect != dateEffect || newDateEffectColor != dateEffectColor
                    || newShowIcons != showIcons || newShowAppNames != showAppNames || newTimeColor != timeColor
                    || newDateColor != dateColor
                    || newShowSettingsButton != showSettingsButton || newShowSearchButton != showSearchButton
                    || newAppNamePosition != appNamePosition
                    || newTextEffect != textEffect || newEffectColor != effectColor || newIconEffect != iconEffect
                    || newIconEffectColor != iconEffectColor || newMonochromeIcons != monochromeIcons
                    || newDynamicIcons != dynamicIcons || newForceMonochromeFallback != forceMonochromeFallback || newDynamicColors != dynamicColors
                    || newInvertIconColors != invertIconColors || newInvertHomeColors != invertHomeColors
                    || newIconBackground != iconBackground
                    || newIconShape != iconShape || newHidePagination != hidePagination
                    || !newClockAppPkg.equals(clockAppPkg) || !newDateAppPkg.equals(dateAppPkg)
                    || newSettingsButtonColor != settingsButtonColor || newSearchButtonColor != searchButtonColor
                    || wallpaperChanged
                    || (newDateCalendarEvents == 1 && calendarPermissionChanged)
                    || widgetPrefsChanged;
            boolean onlyAlignmentChanged = (newHomeAlignment != homeAlignment
                    || newHomeVerticalAlignment != homeVerticalAlignment)
                    && !(newMaxApps != maxApps || newHomeColumns != homeColumns || newHomePages != homePages
                            || newTimePosition != timePosition || newDateVerticalPosition != dateVerticalPosition
                            || newClockDateGap != clockDateGap
                            || newTimeFormat24h != timeFormat24h
                            || newDateFormat != dateFormat
                            || newDatePosition != datePosition || newDateHorizontalPosition != dateHorizontalPosition
                            || newTimeHorizontalPosition != timeHorizontalPosition || newFullMonthName != fullMonthName
                            || newShowSettingsButton != showSettingsButton || newShowSearchButton != showSearchButton
                            || newSettingsButtonColor != settingsButtonColor || newSearchButtonColor != searchButtonColor
                            || newShowIcons != showIcons || newShowAppNames != showAppNames
                            || newAppNamePosition != appNamePosition || newTextEffect != textEffect
                            || newEffectColor != effectColor || newIconEffect != iconEffect
                            || newIconEffectColor != iconEffectColor || !newClockAppPkg.equals(clockAppPkg)
                            || !newDateAppPkg.equals(dateAppPkg) || wallpaperChanged
                            || newInvertIconColors != invertIconColors || newInvertHomeColors != invertHomeColors);
            boolean visibilityChanged = newShowAppNames != showAppNames || newTextEffect != textEffect
                    || newEffectColor != effectColor || newIconEffect != iconEffect
                    || newIconEffectColor != iconEffectColor;

            if (themeChanged || textChanged || layoutChanged || iconChanged || wallpaperChanged) {
                rootLayout.setVisibility(View.INVISIBLE);
                theme = newTheme;
                customBgColor = newCustomBgColor;
                customAccentColor = newCustomAccentColor;
                hasWallpaper = newHasWallpaper;
                wallpaperOffsetX = newWallpaperOffsetX;
                wallpaperOffsetY = newWallpaperOffsetY;
                wallpaperScale = newWallpaperScale;
                wallpaperFileModified = newWallpaperFileModified;
                textSize = newTextSize;
                iconSize = newIconSize;
                boldText = newBoldText;
                appTextColor = newAppTextColor;
                textEffect = newTextEffect;
                effectColor = newEffectColor;
                iconEffect = newIconEffect;
                iconEffectColor = newIconEffectColor;
                homeAlignment = newHomeAlignment;
                homeVerticalAlignment = newHomeVerticalAlignment;
                homePosition = newHomePosition;
                homeColumns = newHomeColumns;
                homePages = newHomePages;
                hidePagination = newHidePagination;
                timePosition = newTimePosition;
                timeFormat24h = newTimeFormat24h;
                dateFormat = newDateFormat;
                timeEffect = newTimeEffect;
                timeEffectColor = newTimeEffectColor;
                dateEffect = newDateEffect;
                dateEffectColor = newDateEffectColor;
                dateCalendarEvents = newDateCalendarEvents;
                dateVerticalPosition = newDateVerticalPosition;
                clockDateGap = newClockDateGap;
                datePosition = newDatePosition;
                dateHorizontalPosition = newDateHorizontalPosition;
                timeHorizontalPosition = newTimeHorizontalPosition;
                timeFontSize = newTimeFontSize;
                timeColor = newTimeColor;
                dateFontSize = newDateFontSize;
                calendarEventFontSize = newCalendarEventFontSize;
                dateColor = newDateColor;
                homePaddingTop = newHomePaddingTop;
                homePaddingBottom = newHomePaddingBottom;
                homePaddingLeft = newHomePaddingLeft;
                homePaddingRight = newHomePaddingRight;
                updatePaddingPx();
                fullMonthName = newFullMonthName;
                batteryInfo = newBatteryInfo;
                batteryPosition = newBatteryPosition;
                if (newMaxApps != maxApps || newHomeColumns != homeColumns) {
                    adjustSlotsForMaxAppsChange(maxApps, newMaxApps, newHomePages, newHomeColumns);
                }
                maxApps = newMaxApps;
                showSettingsButton = newShowSettingsButton;
                showSearchButton = newShowSearchButton;
                settingsButtonSize = newSettingsButtonSize;
                settingsButtonColor = newSettingsButtonColor;
                settingsButtonEffect = newSettingsButtonEffect;
                settingsButtonEffectColor = newSettingsButtonEffectColor;
                searchButtonSize = newSearchButtonSize;
                searchButtonColor = newSearchButtonColor;
                searchButtonEffect = newSearchButtonEffect;
                searchButtonEffectColor = newSearchButtonEffectColor;
                clockAppPkg = newClockAppPkg;
                dateAppPkg = newDateAppPkg;
                showIcons = newShowIcons;
                showAppNames = newShowAppNames;
                appNamePosition = newAppNamePosition;
                monochromeIcons = newMonochromeIcons;
                dynamicIcons = newDynamicIcons;
                forceMonochromeFallback = newForceMonochromeFallback;
                dynamicColors = newDynamicColors;
                invertIconColors = newInvertIconColors;
                invertHomeColors = newInvertHomeColors;
                iconBackground = newIconBackground;
                iconShape = newIconShape;
                calendarPermissionGranted = newCalendarPermissionGranted;

                if (layoutChanged && !onlyAlignmentChanged) {
                    recreateLayout();
                }

                if (themeChanged || wallpaperChanged || layoutChanged) {
                    updateTheme();
                }

                if (textChanged) {
                    updateTextStyles();
                }
            }

            if (onlyAlignmentChanged) {
                homeAlignment = newHomeAlignment;
                homeVerticalAlignment = newHomeVerticalAlignment;
                homePosition = newHomePosition;
                monochromeIcons = newMonochromeIcons;
                dynamicIcons = newDynamicIcons;
                forceMonochromeFallback = newForceMonochromeFallback;
                dynamicColors = newDynamicColors;
                updateGravity();
            }

            if (visibilityChanged && !layoutChanged) {
                showAppNames = newShowAppNames;
                updateVisibility();
            }

            if (wallpaperChanged || themeChanged || layoutChanged) {
                loadWallpaper();
            }

            // Only the artwork changed, so none of the branches above redrew the
            // slots. recreateHome also restores root visibility, which the
            // block above hides whenever it runs.
            if (iconArtworkChanged && !layoutChanged && !themeChanged && !textChanged && !wallpaperChanged) {
                recreateHome();
            }
        }

        TextView pageIndicator = findViewById(R.id.page_indicator);
        pageIndicator.setVisibility((homePages > 1 && !hidePagination) ? View.VISIBLE : View.GONE);
        homePagesManager.updatePageIndicator();

        LinearLayout bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility((homePages > 1 && !hidePagination) ? View.VISIBLE : View.GONE);
        updateGravity();

        if (timeView != null && timeSdf != null) {
            timeView.setText(timeSdf.format(new Date()));
        }
        updateDateText();
        refreshAppBar();
        refreshDock();
        refreshMusicDock();
        homeWidgetHost.loadAllAsync(this);
        FontHelper.applyToViewTree(this, rootLayout);
        applyWindowInsetsToUI(statusBarInset, navBarInset);
        updateServiceComponents();
        startMusicMonitoring();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SystemBarsHelper.apply(this, theme);
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            EinkRefreshHelper.refreshEink(getWindow(), prefs, prefs.getInt("eink_refresh_delay", 100));
        }
    }

    private void updateDateText() {
        if (dateView != null && dateSdf != null) {
            String dateStr = dateSdf.format(new Date());
            if (batteryInfo == 1) {
                String batteryStr = BatteryUtils.getBatteryPercentage(this) + "%";
                if (batteryPosition == 0) {
                    dateStr = batteryStr + " | " + dateStr;
                } else {
                    dateStr = dateStr + " | " + batteryStr;
                }
            }
            dateView.setText(dateStr);
        }
        updateCalendarEventText();
    }

    private void updateCalendarEventText() {
        if (calendarEventView == null) {
            return;
        }
        CalendarEventSummary nextEvent = getNextCalendarEvent();
        calendarEventSummary = nextEvent;
        if (nextEvent == null) {
            calendarEventView.setVisibility(View.GONE);
        } else {
            calendarEventView.setVisibility(View.VISIBLE);
            if (nextEvent.messageOnly) {
                calendarEventView.setText(nextEvent.title);
            } else {
                calendarEventView.setText(formatCalendarEventText(nextEvent));
            }
        }
    }

    private boolean hasCalendarPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    private CalendarEventSummary getNextCalendarEvent() {
        long now = System.currentTimeMillis();
        long end = now + 24L * 60L * 60L * 1000L;
        // Delegates to the shared query helper (also used by CalendarWidget) rather than
        // duplicating the Instances query here; +1 keeps the original inclusive "<=" upper bound.
        java.util.List<org.matiasdesu.thinklauncherv2.utils.CalendarEventsHelper.CalendarEvent> events =
                org.matiasdesu.thinklauncherv2.utils.CalendarEventsHelper.query(this, now, end + 1, 1);
        if (events.isEmpty()) return null;
        org.matiasdesu.thinklauncherv2.utils.CalendarEventsHelper.CalendarEvent event = events.get(0);
        return new CalendarEventSummary(event.id, event.title, event.begin, event.end, event.allDay);
    }

    private String formatCalendarEventText(CalendarEventSummary event) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM d - HH:mm", Locale.getDefault());
        SimpleDateFormat allDayDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        allDayDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        boolean isToday = isCalendarEventToday(event);
        if (event.allDay) {
            if (isToday) {
                return event.title;
            }
            return event.title + ", " + allDayDateFormat.format(new Date(event.begin));
        }

        String startTime = timeFormat.format(new Date(event.begin));
        String endTime = timeFormat.format(new Date(event.end));
        String timeRange = startTime + " - " + endTime;

        if (isToday) {
            return event.title + ", " + timeRange;
        }

        return event.title + ", " + dateTimeFormat.format(new Date(event.begin)) + " - " + endTime;
    }

    private boolean isCalendarEventToday(CalendarEventSummary event) {
        Calendar eventCalendar = Calendar.getInstance();
        if (event.allDay) {
            eventCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        eventCalendar.setTimeInMillis(event.begin);
        Calendar todayCalendar = Calendar.getInstance();
        return eventCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
                && eventCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private void openCalendarEvent(CalendarEventSummary event) {
        if (event == null || event.messageOnly) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.begin);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.end);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Calendar app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private static class CalendarEventSummary {
        long id;
        String title;
        long begin;
        long end;
        boolean allDay;
        boolean messageOnly;

        CalendarEventSummary(long id, String title, long begin, long end, boolean allDay) {
            this.id = id;
            this.title = title;
            this.begin = begin;
            this.end = end;
            this.allDay = allDay;
        }

        static CalendarEventSummary message(String title) {
            CalendarEventSummary event = new CalendarEventSummary(-1, title, 0, 0, false);
            event.messageOnly = true;
            return event;
        }
    }

    private void updateTheme() {
        int bgColor = ThemeUtils.getBgColor(theme, this);
        this.textColor = ThemeUtils.getTextColor(theme, this);
        boolean hasWallpaper = WallpaperHelper.hasWallpaper(this);

        if (hasWallpaper) {
            rootLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            rootLayout.setBackgroundColor(bgColor);
            mainLayout.setPadding(0, 0, 0, 0);
        }
        applyWindowLayoutMode(hasWallpaper, bgColor);
        int timeDateBgColor = hasWallpaper ? android.graphics.Color.TRANSPARENT : bgColor;
        if (timeView != null) {
            timeView.setBackgroundColor(timeDateBgColor);
            timeView.setTextColor(getTimeColorValue());
            applyTextEffect(timeView, timeEffect, getTimeEffectColorValue());
        }
        if (dateView != null) {
            dateView.setBackgroundColor(timeDateBgColor);
            dateView.setTextColor(getDateColorValue());
            applyTextEffect(dateView, dateEffect, getDateEffectColorValue());
        }
        if (calendarEventView != null) {
            calendarEventView.setBackgroundColor(timeDateBgColor);
            calendarEventView.setTextColor(getDateColorValue());
            applyTextEffect(calendarEventView, dateEffect, getDateEffectColorValue());
        }
        int slotBgColor = hasWallpaper ? android.graphics.Color.TRANSPARENT : bgColor;
        for (int i = 0; i < appSlots.length; i++) {
            LinearLayout slot = appSlots[i];
            if (slot != null) {
                slot.setBackgroundColor(slotBgColor);
                TextView tv = getSlotTextView(slot);
                if (tv != null) {
                    tv.setTextColor(getAppTextColorValue());
                    applyTextEffect(tv);
                }
                ImageView iconView = getSlotImageView(slot);
                if (iconView != null) {
                    String pkg = appPackages.get(i);
                    boolean isSpecial = "launcher_settings".equals(pkg) || "app_launcher".equals(pkg)
                            || "notification_panel".equals(pkg) || "koreader_history".equals(pkg)
                            || "calendar".equals(pkg) || "gallery".equals(pkg) || "clock".equals(pkg) || "calculator".equals(pkg) || "bigme_control_panel".equals(pkg)
                            || (pkg != null && pkg.startsWith("folder_"))
                            || (pkg != null && pkg.startsWith("webapp_"));
                    iconView.setTag(isSpecial ? "special" : "app");

                    if (isSpecial) {
                        if (dynamicIcons || iconBackground) {
                            int drawableRes = "launcher_settings".equals(pkg) ? R.drawable.settings
                                    : "app_launcher".equals(pkg) ? R.drawable.search
                                            : "notification_panel".equals(pkg) ? R.drawable.notifications
                                                    : "koreader_history".equals(pkg) ? R.drawable.koreader
                                                            : "calendar".equals(pkg) ? R.drawable.date
                                                                    : "gallery".equals(pkg) ? R.drawable.gallery
                                                                        : "clock".equals(pkg) ? R.drawable.time
                                                                         : "calculator".equals(pkg) ? R.drawable.calculator
                                                                            : (pkg != null && pkg.startsWith("webapp_"))
                                                                                    ? R.drawable.webapps
                                                                                    : R.drawable.folder;
                            Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, drawableRes, theme,
                                    iconBackground, dynamicColors, invertIconColors, iconShape);
                            iconView.setImageDrawable(specialIcon);
                            iconView.clearColorFilter();
                        } else {
                            iconView.setColorFilter(getSpecialIconColor());
                        }
                    } else {

                        try {
                            Drawable drawable = DynamicIconHelper.getAppIcon(this, pkg, dynamicIcons, theme,
                                    iconBackground, dynamicColors, invertIconColors, iconShape,
                                    forceMonochromeFallback);
                            iconView.setImageDrawable(drawable);
                        } catch (Exception e) {

                        }

                        if (monochromeIcons) {
                            iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                        } else {
                            iconView.clearColorFilter();
                        }
                    }
                    applyIconEffect(iconView);
                }
            }
        }
        mainLayout.setBackgroundColor(bgColor);
        if (settingsButton != null) {
            settingsButton.setColorFilter(getSettingsButtonColorValue());
            applyIconEffect(settingsButton, settingsButtonEffect, getSettingsButtonEffectColorValue());
        }
        if (searchButton != null) {
            searchButton.setColorFilter(getSearchButtonColorValue());
            applyIconEffect(searchButton, searchButtonEffect, getSearchButtonEffectColorValue());
        }

        ImageView prevButton = findViewById(R.id.prev_page_button);
        if (prevButton != null) {
            prevButton.setColorFilter(getPaginationColorValue());
        }
        ImageView nextButton = findViewById(R.id.next_page_button);
        if (nextButton != null) {
            nextButton.setColorFilter(getPaginationColorValue());
        }

        TextView pageIndicator = findViewById(R.id.page_indicator);
        if (pageIndicator != null) {
            pageIndicator.setTextColor(getPaginationColorValue());
        }
    }

    private void updateTextStyles() {
        if (timeView != null) {
            timeView.setTextSize(timeFontSize);
            FontHelper.applyStyled(this, timeView, FontHelper.SLOT_TIME, boldText);
            applyTextEffect(timeView, timeEffect, getTimeEffectColorValue());
        }
        if (dateView != null) {
            dateView.setTextSize(dateFontSize);
            FontHelper.applyStyled(this, dateView, FontHelper.SLOT_DATE, boldText);
            applyTextEffect(dateView, dateEffect, getDateEffectColorValue());
        }
        if (calendarEventView != null) {
            calendarEventView.setTextSize(calendarEventFontSize);
            FontHelper.applyStyled(this, calendarEventView, FontHelper.SLOT_CALENDAR_EVENT, boldText);
            applyTextEffect(calendarEventView, dateEffect, getDateEffectColorValue());
        }
        for (LinearLayout slot : appSlots) {
            if (slot != null) {
                TextView tv = getSlotTextView(slot);
                if (tv != null) {
                    tv.setTextSize(textSize);
                    FontHelper.applyStyled(this, tv, FontHelper.SLOT_APP_LIST, boldText);
                    tv.setTextColor(getAppTextColorValue());
                    applyTextEffect(tv);
                }
                ImageView iv = getSlotImageView(slot);
                if (iv != null) {
                    int iconSizePx = (int) (iconSize * getResources().getDisplayMetrics().scaledDensity);
                    iv.getLayoutParams().width = iconSizePx;
                    iv.getLayoutParams().height = iconSizePx;
                    iv.requestLayout();

                    applyIconEffect(iv);
                }
            }
        }
        if (settingsButton != null) {
            applyIconEffect(settingsButton, settingsButtonEffect, getSettingsButtonEffectColorValue());
            int sizePx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                    settingsButtonSize, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) settingsButton.getLayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            settingsButton.setLayoutParams(params);

            if (timeView != null && timeHorizontalPosition == 2) {
                int maxBtnSize = Math.max(settingsButtonSize, showSearchButton == 1 ? searchButtonSize : 0);
                int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
                timeParams.rightMargin = buttonSizePx + 16;
            }
        }
        if (searchButton != null) {
            applyIconEffect(searchButton, searchButtonEffect, getSearchButtonEffectColorValue());
        }
        if (settingsButton != null) {
            int sizePx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                    settingsButtonSize, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) settingsButton.getLayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            settingsButton.setLayoutParams(params);

            if (timeView != null && timeHorizontalPosition == 2) {
                int maxBtnSize = Math.max(settingsButtonSize, showSearchButton == 1 ? searchButtonSize : 0);
                int marginSizePx = (int) android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
                timeParams.rightMargin = marginSizePx + 16;
                timeView.setLayoutParams(timeParams);
            }
            if (dateView != null && dateHorizontalPosition == 2) {
                int maxBtnSize = Math.max(settingsButtonSize, showSearchButton == 1 ? searchButtonSize : 0);
                int marginSizePx = (int) android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams dateParams = (RelativeLayout.LayoutParams) dateView.getLayoutParams();
                dateParams.rightMargin = marginSizePx + 16;
                dateView.setLayoutParams(dateParams);
            }
                if (calendarEventView != null && dateHorizontalPosition == 2) {
                int maxBtnSize = Math.max(settingsButtonSize, showSearchButton == 1 ? searchButtonSize : 0);
                int marginSizePx = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams eventParams = (RelativeLayout.LayoutParams) calendarEventView
                    .getLayoutParams();
                eventParams.rightMargin = marginSizePx + 16;
                calendarEventView.setLayoutParams(eventParams);
                }
        }
        if (searchButton != null) {
            int sizePx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                    searchButtonSize, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchButton.getLayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            searchButton.setLayoutParams(params);
            if (showSettingsButton == 0) {

                if (timeView != null && timeHorizontalPosition == 2) {
                    RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams) timeView.getLayoutParams();
                    timeParams.rightMargin = sizePx + 16;
                    timeView.setLayoutParams(timeParams);
                }
                if (dateView != null && dateHorizontalPosition == 2) {
                    RelativeLayout.LayoutParams dateParams = (RelativeLayout.LayoutParams) dateView.getLayoutParams();
                    dateParams.rightMargin = sizePx + 16;
                    dateView.setLayoutParams(dateParams);
                }
                if (calendarEventView != null && dateHorizontalPosition == 2) {
                    RelativeLayout.LayoutParams eventParams = (RelativeLayout.LayoutParams) calendarEventView
                            .getLayoutParams();
                    eventParams.rightMargin = sizePx + 16;
                    calendarEventView.setLayoutParams(eventParams);
                }
            }
        }
    }

    private void updateVisibility() {
        for (LinearLayout slot : appSlots) {
            if (slot != null) {
                TextView tv = getSlotTextView(slot);
                if (tv != null)
                    tv.setVisibility(showAppNames ? View.VISIBLE : View.GONE);
                if (showAppNames) {
                    slot.setPadding(0, 0, 0, 0);
                    slot.setGravity(Gravity.CENTER_VERTICAL);
                } else {
                    int topBottom = homeColumns == 1 ? 20 : 16;

                    if (homeAlignment == 1) {
                        int symmetricPadding = homeColumns == 1 ? 20 : 12;
                        slot.setPadding(symmetricPadding, topBottom, symmetricPadding, topBottom);
                    } else {
                        int leftPadding = homeColumns == 1 ? (showIcons ? 16 : 32) : (showIcons ? 8 : 16);
                        slot.setPadding(leftPadding, topBottom, 32, topBottom);
                    }
                    slot.setGravity(Gravity.CENTER);
                }
            }
        }
    }

    private void updatePaddingPx() {
        homePaddingLeftPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                homePaddingLeft, getResources().getDisplayMetrics());
        homePaddingRightPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                homePaddingRight, getResources().getDisplayMetrics());
        homePaddingTopPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                homePaddingTop, getResources().getDisplayMetrics());
        homePaddingBottomPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP,
                homePaddingBottom, getResources().getDisplayMetrics());
    }

    private void updateGravity() {
        mainLayout.setGravity(getHorizontalGravity(homeAlignment) | getVerticalGravity(homeVerticalAlignment));
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View child = mainLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                ((LinearLayout) child).setGravity(getHorizontalGravity(homeAlignment) | Gravity.CENTER_VERTICAL);
            }
        }
        applyWindowInsetsToUI(statusBarInset, navBarInset);
    }

    private void recreateLayout() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        homePagesManager = new HomePagesManager(this, prefs, homePages, homeColumns, maxApps);
        TextView pageIndicator = findViewById(R.id.page_indicator);
        pageIndicator.setTextColor(getPaginationColorValue());
        homePagesManager.setPageIndicator(pageIndicator);
        homePagesManager.updatePageIndicator();
        homePagesManager.loadAppsForCurrentPage();
        appLabels.clear();
        appPackages.clear();
        appLabels.addAll(homePagesManager.getAppLabels());
        appPackages.addAll(homePagesManager.getAppPackages());
        if (timeView != null) {
            rootLayout.removeView(timeView);
            timeView = null;
        }
        if (dateView != null) {
            rootLayout.removeView(dateView);
            dateView = null;
        }
        if (calendarEventView != null) {
            rootLayout.removeView(calendarEventView);
            calendarEventView = null;
        }
        if (settingsButton != null) {
            rootLayout.removeView(settingsButton);
            settingsButton = null;
        }
        if (searchButton != null) {
            rootLayout.removeView(searchButton);
            searchButton = null;
        }
        homeWidgetHost.teardown(rootLayout);
        mainLayout.removeAllViews();
        int totalApps = homeColumns * maxApps;
        appSlots = new LinearLayout[totalApps];
        int bgColor = ThemeUtils.getBgColor(theme, this);
        this.textColor = ThemeUtils.getTextColor(theme, this);
        createHomeWidgets(bgColor, textColor);
        createHomeLayout();
        createSettingsButton(bgColor, textColor);
        createSearchButton(bgColor, textColor);
        refreshAppBar();
        refreshDock();
        refreshMusicDock();
        adjustMainLayoutPosition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(homeButtonReceiver);
        stopMusicMonitoring();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelMusicDockHide();
        IconPackHelper.setLoadListener(null);
        homeWidgetHost.destroy();
        if (prefsChangeListener != null) {
            getSharedPreferences("prefs", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefsChangeListener);
        }
    }

    public void showAppSelector(int position) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean animate = prefs.getInt("screen_animations", 0) == 1;
        Intent intent = new Intent(this, AppSelectorActivity.class);
        intent.putExtra(AppSelectorActivity.EXTRA_POSITION, position);
        if (!animate) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivityForResult(intent, position);
        if (animate) {
            overridePendingTransition(R.anim.dialog_fade_in, 0);
        }
    }

    private boolean isSlotRealApp(String pkg) {
        if (pkg == null || pkg.isEmpty() || pkg.equals("blank")) return false;
        if (pkg.startsWith("folder_") || pkg.startsWith("webapp_")) return false;
        if (SystemAppHelper.isSystemApp(pkg)) return false;
        return true;
    }

    private void showAppShortcutsDialog(int slotIndex, java.util.List<ShortcutInfo> shortcuts) {
        new AppShortcutsDialog(this, shortcuts, "Edit",
                () -> showAppSelector(slotIndex),
                shortcut -> launchShortcut(shortcut),
                changeIconCallbackFor(appPackages.get(slotIndex))
        ).show();
    }

    /**
     * Only real apps can carry a pack icon, and only when a pack is selected -
     * otherwise there would be nothing to pick from.
     */
    private AppShortcutsDialog.OnChangeIconCallback changeIconCallbackFor(String pkg) {
        if (!isSlotRealApp(pkg) || IconPackHelper.getSelectedPack(this).isEmpty()) {
            return null;
        }
        final String realPkg = pkg.startsWith("hidden_app_") ? pkg.substring("hidden_app_".length()) : pkg;
        return () -> openIconPicker(realPkg);
    }

    private void openIconPicker(String pkg) {
        Intent intent = new Intent(this, IconPickerActivity.class);
        intent.putExtra(IconPickerActivity.EXTRA_PACKAGE, pkg);
        if (getSharedPreferences("prefs", MODE_PRIVATE).getInt("screen_animations", 0) != 1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivity(intent);
    }

    private void launchShortcut(ShortcutInfo shortcut) {
        LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (launcherApps != null) {
            launcherApps.startShortcut(shortcut, null, null);
        }
    }

    private void showDockShortcuts(String pkg, String dockPrefix, int dockSlotIndex) {
        if (pkg == null || pkg.isEmpty()) {
            return;
        }
        java.util.List<ShortcutInfo> shortcuts = null;
        String realPkg = pkg.startsWith("hidden_app_") ? pkg.substring("hidden_app_".length()) : pkg;
        if (isSlotRealApp(pkg)) {
            shortcuts = ShortcutHelper.getShortcuts(this, realPkg);
        }
        final String prefix = dockPrefix;
        final int slotIndex = dockSlotIndex;
        new AppShortcutsDialog(this, shortcuts, "Edit",
                () -> editDockItem(prefix, slotIndex),
                shortcut -> launchShortcut(shortcut),
                changeIconCallbackFor(pkg)).show();
    }

    private void editDockItem(String prefix, int slotIndex) {
        if (prefix == null || slotIndex < 0) {
            return;
        }
        pendingDockPrefix = prefix;
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean animate = prefs.getInt("screen_animations", 0) == 1;
        Intent intent = new Intent(this, AppSelectorActivity.class);
        intent.putExtra(AppSelectorActivity.EXTRA_POSITION, slotIndex);
        intent.putExtra(AppSelectorActivity.EXTRA_NO_BLANK, true);
        if (!animate) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivityForResult(intent, REQUEST_EDIT_DOCK_BASE + slotIndex);
        if (animate) {
            overridePendingTransition(R.anim.dialog_fade_in, 0);
        }
    }

    private void setupDockItemTap(ImageView iv, String prefix, int slotIndex, String pkg) {
        iv.setTag("dock_pkg:" + prefix + ":" + slotIndex + ":" + pkg);
    }

    public void launchApp(String packageName) {
        if (SystemAppHelper.isSpecialLaunchable(packageName)
                && !packageName.equals(SystemAppHelper.NEXT_HOME_PAGE)
                && !packageName.equals(SystemAppHelper.PREVIOUS_HOME_PAGE)) {
            if (SystemAppHelper.launch(this, packageName)) return;
        }
        if ("next_home_page".equals(packageName)) {
            int current = homePagesManager.getCurrentPage();
            int nextPage = (current + 1) % homePages;
            homePagesManager.setCurrentPage(nextPage);
            recreateHome();
        } else if ("previous_home_page".equals(packageName)) {
            int current = homePagesManager.getCurrentPage();
            int prevPage = (current - 1 + homePages) % homePages;
            homePagesManager.setCurrentPage(prevPage);
            recreateHome();
        } else if ("blank".equals(packageName)) {
            // Do nothing for blank
        } else if (packageName != null && packageName.startsWith("webapp_")) {

            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            String url = prefs.getString(packageName + "_url", "");
            if (!url.isEmpty()) {
                boolean animate = prefs.getInt("app_launch_animation", 0) == 1;
                int pwaMode = prefs.getInt("webapp_pwa_mode", 0);
                if (pwaMode == 1) {
                    try {
                        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build();
                        Intent ctIntent = customTabsIntent.intent;
                        ctIntent.setData(android.net.Uri.parse(url));
                        ctIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (!animate) {
                            ctIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        }
                        startActivity(ctIntent);
                        if (!animate) {
                            overridePendingTransition(0, 0);
                        }
                    } catch (Exception e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(url));
                        if (!animate) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        }
                        try {
                            startActivity(intent);
                            if (!animate) {
                                overridePendingTransition(0, 0);
                            }
                        } catch (Exception e2) { }
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse(url));
                    if (!animate) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    }
                    try {
                        startActivity(intent);
                        if (!animate) {
                            overridePendingTransition(0, 0);
                        }
                    } catch (Exception e) { }
                }
            }
        } else if (packageName != null && packageName.startsWith("folder_")) {

            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            String folderName = prefs.getString(packageName + "_name", "Folder");
            boolean folderAnimations = prefs.getInt("screen_animations", 0) == 1;
            Intent intent = new Intent(MainActivity.this, org.matiasdesu.thinklauncherv2.ui.FolderActivity.class);
            intent.putExtra(org.matiasdesu.thinklauncherv2.ui.FolderActivity.EXTRA_FOLDER_ID, packageName);
            intent.putExtra(org.matiasdesu.thinklauncherv2.ui.FolderActivity.EXTRA_FOLDER_NAME, folderName);
            if (!folderAnimations) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            startActivityForResult(intent, 9999);
            if (folderAnimations) {
                overridePendingTransition(R.anim.dialog_fade_in, 0);
            }
        } else if (packageName != null && packageName.startsWith("hidden_app_")) {
            String realPkg = packageName.substring("hidden_app_".length());
            launchApp(realPkg);
        } else if (packageName != null && OnyxHelper.isOnyxPseudoPackage(packageName)) {
            OnyxHelper.launchOnyxApp(this, packageName);
        } else if (!packageName.isEmpty()) {
            if (OnyxHelper.isOnyxDevice() && OnyxHelper.isAppFrozen(this, packageName)) {
                OnyxHelper.showFrozenAppDialog(this, packageName, theme);
                return;
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                boolean animate = prefs.getInt("app_launch_animation", 0) == 1;
                if (!animate) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                }
                try {
                    startActivity(intent);
                    if (animate) {
                        overridePendingTransition(R.anim.dialog_fade_in, 0);
                    } else {
                        overridePendingTransition(0, 0);
                    }
                } catch (Exception e) {
                    if (OnyxHelper.isOnyxDevice() && OnyxHelper.isAppFrozen(this, packageName)) {
                        OnyxHelper.showFrozenAppDialog(this, packageName, theme);
                    }
                }
            } else {
                if (OnyxHelper.isOnyxDevice() && OnyxHelper.isAppFrozen(this, packageName)) {
                    OnyxHelper.showFrozenAppDialog(this, packageName, theme);
                }
            }
        }
    }

    public void updateSlot(int position) {
        LinearLayout slot = appSlots[position];
        TextView tv = getSlotTextView(slot);
        if (tv == null)
            return;
        tv.setText(appLabels.get(position));
        tv.setTextColor(getAppTextColorValue());
        tv.setTextSize(textSize);
        FontHelper.applyStyled(this, tv, FontHelper.SLOT_APP_LIST, boldText);
        tv.setGravity(getHorizontalGravity(homeAlignment));
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        applyTextEffect(tv);
        tv.setVisibility(showAppNames ? View.VISIBLE : View.GONE);
        if (appPackages.get(position).equals("blank") || appPackages.get(position).isEmpty()) {
            tv.setText(appPackages.get(position).equals("blank") ? "" : appLabels.get(position));
            tv.setVisibility(appPackages.get(position).equals("blank") ? View.GONE : View.VISIBLE);
        }

        slot.setClipChildren(false);
        slot.setClipToPadding(false);
        if (showAppNames && (appNamePosition == AppNamePositionHelper.POSITION_TOP
                || appNamePosition == AppNamePositionHelper.POSITION_BOTTOM)) {
            slot.setOrientation(LinearLayout.VERTICAL);
            slot.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            slot.setOrientation(LinearLayout.HORIZONTAL);
            slot.setGravity(Gravity.CENTER_VERTICAL);
        }

        if (appNamePosition == AppNamePositionHelper.POSITION_LEFT && showIcons && showAppNames) {
            slot.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            slot.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        int vPadding = homeColumns == 1 ? 20 : 16;
        if (showAppNames) {
            slot.setPadding(0, 0, 0, 0);
            int leftOuterPadding = homeColumns == 1 ? (showIcons ? 16 : 32) : (showIcons ? 8 : 16);
            if (appNamePosition == AppNamePositionHelper.POSITION_TOP
                    || appNamePosition == AppNamePositionHelper.POSITION_BOTTOM) {
                tv.setPadding(8, appNamePosition == AppNamePositionHelper.POSITION_TOP ? vPadding : 8, 8,
                        appNamePosition == AppNamePositionHelper.POSITION_BOTTOM ? vPadding : 8);
            } else {
                tv.setPadding(leftOuterPadding, vPadding, 32, vPadding);
            }
        } else {

            if (homeAlignment == 1) {
                int symmetricPadding = homeColumns == 1 ? 20 : 12;
                slot.setPadding(symmetricPadding, vPadding, symmetricPadding, vPadding);
            } else {
                int leftPadding = homeColumns == 1 ? (showIcons ? 16 : 32) : (showIcons ? 8 : 16);
                slot.setPadding(leftPadding, vPadding, 32, vPadding);
            }
        }

        boolean shouldHaveIcon = showIcons || appPackages.get(position).equals("blank");
        boolean isBlankOrEmpty = appPackages.get(position).isEmpty() || appPackages.get(position).equals("blank");
        ImageView existingIcon = getSlotImageView(slot);
        boolean hasIcon = existingIcon != null;

        if (shouldHaveIcon) {
            if (!hasIcon) {

                ImageView iconView = new ImageView(this);
                int iconSizePx = (int) (iconSize * getResources().getDisplayMetrics().scaledDensity);
                iconView.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));

                if (isBlankOrEmpty) {
                    iconView.setVisibility(View.INVISIBLE);
                    iconView.setImageDrawable(
                            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                    if (showAppNames) {
                        iconView.setPadding(0, 0, 16, 0);
                    }
                    int addIndex = (appNamePosition == AppNamePositionHelper.POSITION_TOP
                            || appNamePosition == AppNamePositionHelper.POSITION_LEFT) ? 1 : 0;
                    slot.addView(iconView, Math.min(addIndex, slot.getChildCount()));
                    return;
                }
                if ("launcher_settings".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.settings, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.settings);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("app_launcher".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.search, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.search);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("notification_panel".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.notifications,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.notifications);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("koreader_history".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.koreader,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.koreader);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("calendar".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.date,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.date);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("gallery".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.gallery,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.gallery);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("clock".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.time,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.time);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("calculator".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.calculator, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.calculator);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (appPackages.get(position).startsWith("folder_")) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.folder, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.folder);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (appPackages.get(position).startsWith("webapp_")) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.webapps, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.webapps);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (OnyxHelper.isOnyxPseudoPackage(appPackages.get(position))) {
                    Drawable onyxDrawable = OnyxHelper.getOnyxIcon(this, appPackages.get(position));
                    iconView.setImageDrawable(onyxDrawable);
                    if (monochromeIcons) {
                        iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iconView.clearColorFilter();
                    }
                } else {
                    try {
                        Drawable drawable = DynamicIconHelper.getAppIcon(this, appPackages.get(position), dynamicIcons,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape,
                                forceMonochromeFallback);
                        iconView.setImageDrawable(drawable);
                        if (monochromeIcons) {
                            iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                        } else {
                            iconView.clearColorFilter();
                        }
                    } catch (Exception e) {

                        return;
                    }
                }
                int addIndex = (appNamePosition == AppNamePositionHelper.POSITION_TOP
                        || appNamePosition == AppNamePositionHelper.POSITION_LEFT) ? 1 : 0;
                slot.addView(iconView, Math.min(addIndex, slot.getChildCount()));
                applyIconEffect(iconView);
            } else {

                ImageView iconView = existingIcon;

                if (isBlankOrEmpty) {
                    iconView.setVisibility(View.INVISIBLE);
                    iconView.setImageDrawable(
                            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                    if (showAppNames) {
                        iconView.setPadding(0, 0, 16, 0);
                    } else {
                        iconView.setPadding(0, 0, 0, 0);
                    }
                    return;
                }

                iconView.setVisibility(View.VISIBLE);

                if ("launcher_settings".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.settings, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.settings);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("app_launcher".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.search, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.search);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("notification_panel".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.notifications,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.notifications);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("koreader_history".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.koreader,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.koreader);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("calendar".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.date,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.date);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("gallery".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.gallery,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.gallery);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("clock".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.time,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.time);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if ("calculator".equals(appPackages.get(position))) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.calculator, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.calculator);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (appPackages.get(position).startsWith("folder_")) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.folder, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.folder);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (appPackages.get(position).startsWith("webapp_")) {
                    if (dynamicIcons || iconBackground) {
                        Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.webapps, theme,
                                iconBackground, dynamicColors, invertIconColors, iconShape);
                        iconView.setImageDrawable(specialIcon);
                        iconView.clearColorFilter();
                    } else {
                        iconView.setImageResource(R.drawable.webapps);
                        iconView.setColorFilter(getSpecialIconColor());
                    }
                } else if (OnyxHelper.isOnyxPseudoPackage(appPackages.get(position))) {
                    Drawable onyxDrawable = OnyxHelper.getOnyxIcon(this, appPackages.get(position));
                    iconView.setImageDrawable(onyxDrawable);
                    if (monochromeIcons) {
                        iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iconView.clearColorFilter();
                    }
                } else {
                    try {
                        Drawable drawable = DynamicIconHelper.getAppIcon(this, appPackages.get(position), dynamicIcons,
                                theme, iconBackground, dynamicColors, invertIconColors, iconShape,
                                forceMonochromeFallback);
                        iconView.setImageDrawable(drawable);
                        if (monochromeIcons) {
                            iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                        } else {
                            iconView.clearColorFilter();
                        }
                    } catch (Exception e) {
                        // Icon not found, skip
                    }
                }

                applyIconEffect(iconView);
            }
        } else if (!shouldHaveIcon && hasIcon) {

            slot.removeView(existingIcon);
        }

        if (appPackages.get(position) != null && appPackages.get(position).startsWith("hidden_app_")) {
            tv.setVisibility(View.INVISIBLE);
            ImageView icon = getSlotImageView(slot);
            if (icon != null) {
                icon.setVisibility(View.INVISIBLE);
            }
        }

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        prefs.edit().putString("slot_label_" + position, appLabels.get(position))
                .putString("slot_pkg_" + position, appPackages.get(position)).apply();
    }

    private int getVerticalGravity(int vertical) {
        switch (vertical) {
            case 0:
                return Gravity.TOP;
            case 1:
                return Gravity.CENTER;
            case 2:
                return Gravity.BOTTOM;
            default:
                return Gravity.CENTER;
        }
    }

    private int getHorizontalGravity(int horizontal) {
        switch (horizontal) {
            case 0:
                return Gravity.LEFT;
            case 1:
                return Gravity.CENTER_HORIZONTAL;
            case 2:
                return Gravity.RIGHT;
            default:
                return Gravity.CENTER_HORIZONTAL;
        }
    }

    private int getRelativeHorizontalRule(int horizontal) {
        switch (horizontal) {
            case 0:
                return RelativeLayout.ALIGN_PARENT_LEFT;
            case 1:
                return RelativeLayout.CENTER_HORIZONTAL;
            case 2:
                return RelativeLayout.ALIGN_PARENT_RIGHT;
            default:
                return RelativeLayout.CENTER_HORIZONTAL;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == 9999) {
                String folderId = data.getStringExtra(org.matiasdesu.thinklauncherv2.ui.FolderActivity.EXTRA_FOLDER_ID);
                String updatedName = data
                        .getStringExtra(org.matiasdesu.thinklauncherv2.ui.FolderActivity.EXTRA_UPDATED_FOLDER_NAME);

                if (folderId != null && updatedName != null) {

                    for (int i = 0; i < appPackages.size(); i++) {
                        if (folderId.equals(appPackages.get(i))) {
                            appLabels.set(i, updatedName);
                            homePagesManager.setAppLabel(i, updatedName);
                            updateSlot(i);
                            break;
                        }
                    }
                }
                return;
            }

            if (requestCode >= REQUEST_EDIT_DOCK_BASE && requestCode < REQUEST_EDIT_DOCK_BASE + 100) {
                int slotIndex = requestCode - REQUEST_EDIT_DOCK_BASE;
                String prefix = pendingDockPrefix;
                pendingDockPrefix = null;
                if (prefix != null && slotIndex >= 0) {
                    String label = data.getStringExtra(AppSelectorActivity.EXTRA_LABEL);
                    String pkg = data.getStringExtra(AppSelectorActivity.EXTRA_PACKAGE);
                    if (pkg != null) {
                        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        if (pkg.startsWith("folder_")) {
                            editor.putString(pkg + "_name", label == null ? "" : label);
                        }
                        editor.putString(prefix + "_app_package_" + slotIndex, pkg);
                        editor.putString(prefix + "_app_label_" + slotIndex, label == null ? "" : label);
                        editor.apply();
                        if ("dock".equals(prefix)) {
                            refreshDock();
                        } else if ("app_bar".equals(prefix)) {
                            refreshAppBar();
                        }
                    }
                }
                return;
            }

            String label = data.getStringExtra(AppSelectorActivity.EXTRA_LABEL);
            String pkg = data.getStringExtra(AppSelectorActivity.EXTRA_PACKAGE);
            int position = data.getIntExtra(AppSelectorActivity.EXTRA_POSITION, -1);

            if (position >= 0 && position < appLabels.size()) {

                if (pkg != null && pkg.startsWith("folder_")) {
                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    prefs.edit().putString(pkg + "_name", label).apply();
                }

                appLabels.set(position, label);
                appPackages.set(position, pkg);
                homePagesManager.setAppLabel(position, label);
                homePagesManager.setAppPackage(position, pkg);

                updateSlot(position);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            String action = prefs.getString("hardware_key_volume_up", "");
            if (action != null && !action.isEmpty()) {
                launchApp(action);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            String action = prefs.getString("hardware_key_volume_down", "");
            if (action != null && !action.isEmpty()) {
                launchApp(action);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // Do nothing - disable back button on home
    }

    private class GestureHandler {

        private GestureDetector gestureDetector;
        private boolean doubleTapDone = false;

        private float downX, downY;
        private long downTime;
        private int touchedSlotIndex = -1;
        private boolean touchedClock = false;
        private boolean touchedDate = false;
        private boolean isLongPress = false;
        private String touchedDockPkg = null;
        private String touchedDockPrefix = null;
        private int touchedDockSlotIndex = -1;
        private View touchedMusicButton = null;
        private boolean touchedMusicBar = false;

        private final Handler handler = new Handler(Looper.getMainLooper());
        private final java.util.ArrayList<GesturePoint> touchPoints = new java.util.ArrayList<>();
        private Runnable longPressRunnable;

        private static final float SWIPE_THRESHOLD_DP = 50;
        private static final float TAP_MAX_DISTANCE_DP = 20;
        private static final int LONG_PRESS_MS = 200;
        private static final int MIN_CUSTOM_GESTURE_POINTS = 10;
        private static final float CUSTOM_GESTURE_SCORE_THRESHOLD = 2.5f;

        public GestureHandler() {
            loadApps();
            GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (doubleTapLock == 1) {
                        if (LockAccessibilityService.lockScreen()) {
                            doubleTapDone = true;
                            return true;
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Please enable accessibility to use double tap to lock",
                                    Toast.LENGTH_SHORT).show();
                            doubleTapDone = false;
                            return false;
                        }
                    }
                    doubleTapDone = false;
                    return false;
                }
            };
            gestureDetector = new GestureDetector(getApplicationContext(), gestureListener);
        }

        public void loadApps() {
            // Swipe apps are loaded from prefs on each use; nothing to cache here.
        }

        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    downTime = event.getEventTime();
                    isLongPress = false;
                    doubleTapDone = false;
                    touchPoints.clear();
                    touchPoints.add(new GesturePoint(event.getX(), event.getY(), event.getEventTime()));

                    touchedSlotIndex = findTouchedSlot(event.getX(), event.getY());
                    touchedDockPkg = findDockPkg(event.getX(), event.getY());
                    touchedMusicButton = findMusicButton(event.getX(), event.getY());
                    touchedMusicBar = isPointInsideView(event.getX(), event.getY(), musicDockView);
                    touchedClock = isPointInsideView(event.getX(), event.getY(), timeView);
                    touchedDate = isPointInsideView(event.getX(), event.getY(), dateView);

                    longPressRunnable = () -> {
                        isLongPress = true;
                        if (touchedDockPkg != null) {
                            showDockShortcuts(touchedDockPkg, touchedDockPrefix, touchedDockSlotIndex);
                        } else if (touchedMusicButton != null || touchedMusicBar) {
                            showMusicDockOptionsDialog();
                        } else if (touchedSlotIndex >= 0) {
                            String pkg = appPackages.get(touchedSlotIndex);
                            java.util.List<ShortcutInfo> shortcuts = null;
                            if (!pkg.isEmpty() && !pkg.equals("blank")) {
                                String realPkg = pkg.startsWith("hidden_app_") ? pkg.substring("hidden_app_".length()) : pkg;
                                if (isSlotRealApp(pkg)) {
                                    shortcuts = ShortcutHelper.getShortcuts(MainActivity.this, realPkg);
                                }
                            }
                            showAppShortcutsDialog(touchedSlotIndex, shortcuts);
                        } else {
                            try {
                                Class<?> clazz = Class.forName("org.matiasdesu.thinklauncherv2.settings.SettingsActivity");
                                Intent intent = new Intent(MainActivity.this, clazz);
                                boolean animate = getSharedPreferences("prefs", MODE_PRIVATE).getInt("screen_animations", 0) == 1;
                                if (!animate) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                }
                                startActivity(intent);
                                if (animate) {
                                    overridePendingTransition(R.anim.dialog_fade_in, 0);
                                }
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    handler.postDelayed(longPressRunnable, LONG_PRESS_MS);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    touchPoints.add(new GesturePoint(event.getX(), event.getY(), event.getEventTime()));
                    if (movedTooFar(event)) {
                        handler.removeCallbacks(longPressRunnable);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(longPressRunnable);
                    touchPoints.add(new GesturePoint(event.getX(), event.getY(), event.getEventTime()));
                    if (isLongPress) return true;

                    float totalDx = event.getX() - downX;
                    float totalDy = event.getY() - downY;
                    float totalDist = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);
                    float tapThreshold = TAP_MAX_DISTANCE_DP * getResources().getDisplayMetrics().density;

                    if (totalDist < tapThreshold) {
                        if (!doubleTapDone) handleTap();
                        return true;
                    }

                    if (tryCustomGesture()) return true;

                    handleSwipe(totalDx, totalDy);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(longPressRunnable);
                    break;
            }
            return true;
        }

        private boolean movedTooFar(MotionEvent event) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            float threshold = TAP_MAX_DISTANCE_DP * getResources().getDisplayMetrics().density;
            return (dx * dx + dy * dy) > (threshold * threshold);
        }

        private void handleTap() {
            if (touchedMusicButton != null) {
                Object action = touchedMusicButton.getTag();
                if ("prev".equals(action)) {
                    musicSkipToPrevious();
                } else if ("play_pause".equals(action)) {
                    musicTogglePlayPause();
                } else if ("next".equals(action)) {
                    musicSkipToNext();
                } else {
                    launchPlayingApp();
                }
                return;
            }
            if (touchedMusicBar) {
                launchPlayingApp();
                return;
            }
            if (touchedDockPkg != null) {
                launchApp(touchedDockPkg);
            } else if (touchedSlotIndex >= 0) {
                String pkg = appPackages.get(touchedSlotIndex);
                if (!pkg.isEmpty()) {
                    launchApp(pkg);
                }
            } else if (touchedClock) {
                if (clockAppPkg.equals("system_default")) {
                    Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else if (!clockAppPkg.isEmpty()) {
                    launchApp(clockAppPkg);
                }
            } else if (touchedDate) {
                if (dateAppPkg.equals("system_default")) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else if (!dateAppPkg.isEmpty()) {
                    launchApp(dateAppPkg);
                }
            }
        }

        private boolean tryCustomGesture() {
            if (customGestureLibrary == null || touchPoints.size() < MIN_CUSTOM_GESTURE_POINTS) return false;
            if (customGestureLibrary.getGestureEntries().isEmpty()) return false;

            try {
                GestureStroke stroke = new GestureStroke(new java.util.ArrayList<>(touchPoints));
                Gesture gesture = new Gesture();
                gesture.addStroke(stroke);
                java.util.ArrayList<Prediction> predictions = customGestureLibrary.recognize(gesture);
                if (predictions != null && !predictions.isEmpty()
                        && predictions.get(0).score > CUSTOM_GESTURE_SCORE_THRESHOLD) {
                    String name = predictions.get(0).name;
                    String pkg = getSharedPreferences("prefs", MODE_PRIVATE)
                            .getString("custom_gesture_" + name + "_app", "");
                    if (!pkg.isEmpty()) {
                        launchApp(pkg);
                        return true;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
            return false;
        }

        private void handleSwipe(float dx, float dy) {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            String leftApp = prefs.getString("swipe_left_app", "");
            String rightApp = prefs.getString("swipe_right_app", "");
            String downApp = prefs.getString("swipe_down_app", "");
            String upApp = prefs.getString("swipe_up_app", "");
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);
            float minDist = SWIPE_THRESHOLD_DP * getResources().getDisplayMetrics().density;
            if (absDx > absDy && absDx > minDist) {
                launchApp(dx > 0 ? rightApp : leftApp);
            } else if (absDy > absDx && absDy > minDist) {
                launchApp(dy > 0 ? downApp : upApp);
            }
        }

        private int findTouchedSlot(float x, float y) {
            for (int i = 0; i < appSlots.length; i++) {
                if (appSlots[i] != null && appSlots[i].getVisibility() == View.VISIBLE) {
                    if (isPointInsideView(x, y, appSlots[i])) {
                        return i;
                    }
                }
            }
            return -1;
        }

        private String findDockPkg(float x, float y) {
            View[] docks = { appBarView, dockView };
            for (View dock : docks) {
                if (dock == null || dock.getParent() != rootLayout) continue;
                if (dock instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) dock;
                    for (int i = 0; i < vg.getChildCount(); i++) {
                        View item = vg.getChildAt(i);
                        if (item.getVisibility() != View.VISIBLE) continue;
                        Object tag = item.getTag();
                        if (tag instanceof String && ((String) tag).startsWith("dock_pkg:")
                                && isPointInsideView(x, y, item)) {
                            String t = ((String) tag).substring("dock_pkg:".length());
                            int firstSep = t.indexOf(':');
                            if (firstSep > 0) {
                                touchedDockPrefix = t.substring(0, firstSep);
                                int secondSep = t.indexOf(':', firstSep + 1);
                                if (secondSep > firstSep) {
                                    try {
                                        touchedDockSlotIndex = Integer.parseInt(
                                                t.substring(firstSep + 1, secondSep));
                                    } catch (NumberFormatException e) {
                                        touchedDockSlotIndex = -1;
                                    }
                                    return t.substring(secondSep + 1);
                                }
                            }
                            touchedDockPrefix = null;
                            touchedDockSlotIndex = -1;
                            return t;
                        }
                    }
                }
            }
            touchedDockPrefix = null;
            touchedDockSlotIndex = -1;
            return null;
        }

        private View findMusicButton(float x, float y) {
            if (musicDockView == null || musicDockView.getParent() != rootLayout) return null;
            return findTaggedMusicButton(musicDockView, x, y);
        }

        private View findTaggedMusicButton(ViewGroup group, float x, float y) {
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View item = group.getChildAt(i);
                if (item.getVisibility() != View.VISIBLE) continue;
                Object tag = item.getTag();
                boolean isTransportButton = tag instanceof String
                        && ("prev".equals(tag) || "play_pause".equals(tag) || "next".equals(tag));
                if (isTransportButton && isPointInsideView(x, y, item)) {
                    return item;
                }
                if (item instanceof ViewGroup) {
                    View found = findTaggedMusicButton((ViewGroup) item, x, y);
                    if (found != null) return found;
                }
            }
            return null;
        }

        private boolean isPointInsideView(float x, float y, View view) {
            if (view == null) return false;
            int[] viewLoc = new int[2];
            int[] rootLoc = new int[2];
            view.getLocationOnScreen(viewLoc);
            rootLayout.getLocationOnScreen(rootLoc);
            float vx = viewLoc[0] - rootLoc[0];
            float vy = viewLoc[1] - rootLoc[1];
            return x >= vx && x <= vx + view.getWidth()
                    && y >= vy && y <= vy + view.getHeight();
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Uninstalling the pack changes no preference, so this has to run on every
     * resume - otherwise the launcher keeps serving icons cached from a pack
     * that is no longer on the device. Clearing the pref marks prefs dirty,
     * which repaints.
     */
    private void dropIconPackIfUninstalled() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String selected = prefs.getString(IconPackHelper.PREF_ICON_PACK, "");
        if (selected.isEmpty() || IconPackHelper.isPackInstalled(this, selected)) {
            return;
        }

        prefs.edit().remove(IconPackHelper.PREF_ICON_PACK).apply();
        iconPack = "";
        IconPackHelper.invalidate();
        DynamicIconHelper.bumpCacheEpoch();
    }

    private void recreateHome() {
        mainLayout.removeAllViews();
        appLabels.clear();
        appPackages.clear();
        homePagesManager.loadAppsForCurrentPage();
        appLabels.addAll(homePagesManager.getAppLabels());
        appPackages.addAll(homePagesManager.getAppPackages());

        createHomeLayout();

        EinkRefreshHelper.refreshEink(getWindow(), getSharedPreferences("prefs", MODE_PRIVATE),
                getSharedPreferences("prefs", MODE_PRIVATE).getInt("eink_refresh_delay", 100));
    }

    /**
     * Puts the window into edge-to-edge or fitted mode and applies the matching
     * system-bar colors, then re-asserts the status-bar state. Edge-to-edge is
     * needed for a wallpaper, and also whenever the status bar is hidden: a
     * fitted window keeps its frame below the (now invisible) bar and leaves
     * that strip unpainted.
     */
    private void applyWindowLayoutMode(boolean hasWallpaper, int bgColor) {
        boolean edgeToEdge = hasWallpaper || SystemBarsHelper.isHideStatusBar(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), !edgeToEdge);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int barColor = hasWallpaper ? android.graphics.Color.TRANSPARENT : bgColor;
            getWindow().setStatusBarColor(barColor);
            getWindow().setNavigationBarColor(barColor);
        }
        SystemBarsHelper.apply(this, theme);
    }

    /**
     * Feeds the live system-bar insets into {@link #applyWindowInsetsToUI}. Only
     * needed in edge-to-edge mode; a hidden status bar simply reports a top
     * inset of 0.
     */
    private void installWindowInsetsListener() {
        rootLayout.setOnApplyWindowInsetsListener((v, insets) -> {
            int statusBarHeight;
            int navBarHeight;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
                navBarHeight = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            } else {
                statusBarHeight = insets.getSystemWindowInsetTop();
                navBarHeight = insets.getSystemWindowInsetBottom();
            }
            statusBarInset = statusBarHeight;
            navBarInset = navBarHeight;

            applyWindowInsetsToUI(statusBarHeight, navBarHeight);

            return insets;
        });
        rootLayout.requestApplyInsets();
    }

    private void loadWallpaper() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        }
        SystemBarsHelper.apply(this, theme);
        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                int viewWidth = rootLayout.getWidth();
                int viewHeight = rootLayout.getHeight();
                if (viewWidth <= 0 || viewHeight <= 0) {
                    rootLayout.post(this);
                    return;
                }
                startWallpaperLoad(viewWidth, viewHeight);
            }
        });
    }

    private void startWallpaperLoad(final int viewWidth, final int viewHeight) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                int theme = prefs.getInt("theme", 0);
                int bgColor = ThemeUtils.getBgColor(theme, this);
                Bitmap wallpaper = null;

                if (WallpaperHelper.hasWallpaper(this)) {
                    wallpaper = WallpaperHelper.getWallpaperForScreenCached(this, viewWidth, viewHeight);
                }

                final Bitmap finalWallpaper = wallpaper;
                runOnUiThread(() -> {
                    if (finalWallpaper != null) {
                        wallpaperView.setImageBitmap(finalWallpaper);
                        wallpaperView.setVisibility(View.VISIBLE);
                        mainLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        rootLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);

                        installWindowInsetsListener();
                    } else {
                        wallpaperView.setVisibility(View.GONE);
                        mainLayout.setBackgroundColor(bgColor);
                        rootLayout.setBackgroundColor(bgColor);
                        statusBarInset = 0;
                        navBarInset = 0;
                        updateGravity();

                        applyWindowLayoutMode(false, bgColor);

                        if (SystemBarsHelper.isHideStatusBar(this)) {
                            // Still edge-to-edge, so the navigation-bar inset has
                            // to come from the listener (the top one is 0 while
                            // the status bar is hidden).
                            installWindowInsetsListener();
                        } else {
                            rootLayout.setOnApplyWindowInsetsListener(null);
                            applyWindowInsetsToUI(0, 0);
                        }
                    }

                    rootLayout.setVisibility(View.VISIBLE);

                    DockBackdropHelper.reapplyAll(rootLayout);

                    EinkRefreshHelper.refreshEink(getWindow(), getSharedPreferences("prefs", MODE_PRIVATE),
                            getSharedPreferences("prefs", MODE_PRIVATE).getInt("eink_refresh_delay", 100));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> rootLayout.setVisibility(View.VISIBLE));
            }
        }).start();
    }

    private void applyWindowInsetsToUI(int topInset, int bottomInset) {
        if (dateView != null && dateView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dateView.getLayoutParams();
            boolean isTimeVisible = (timePosition == 1 && timeView != null && timeView.getParent() == rootLayout);
            if (dateVerticalPosition == 0 || !isTimeVisible) {
                params.topMargin = topInset + homePaddingTopPx;
            }
            if (dateHorizontalPosition == 0) {
                params.leftMargin = homePaddingLeftPx;
            } else if (dateHorizontalPosition == 2) {
                int rightMargin = homePaddingRightPx;
                if (showSettingsButton == 1 || showSearchButton == 1) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    rightMargin += buttonSizePx + 16;
                }
                params.rightMargin = rightMargin;
            }
            dateView.setLayoutParams(params);
        }

        if (calendarEventView != null && calendarEventView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) calendarEventView.getLayoutParams();
            if (dateHorizontalPosition == 0) {
                params.leftMargin = homePaddingLeftPx;
            } else if (dateHorizontalPosition == 2) {
                int rightMargin = homePaddingRightPx;
                if (showSettingsButton == 1 || showSearchButton == 1) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    rightMargin += buttonSizePx + 16;
                }
                params.rightMargin = rightMargin;
            }
            calendarEventView.setLayoutParams(params);
        }

        if (timeView != null && timeView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeView.getLayoutParams();

            if (dateView == null || dateVerticalPosition != 0) {
                params.topMargin = topInset + homePaddingTopPx;
            }
            if (timeHorizontalPosition == 0) {
                params.leftMargin = homePaddingLeftPx;
            } else if (timeHorizontalPosition == 2) {
                int rightMargin = homePaddingRightPx;
                if (showSettingsButton == 1 || showSearchButton == 1) {
                    int maxBtnSize = Math.max(showSettingsButton == 1 ? settingsButtonSize : 0,
                            showSearchButton == 1 ? searchButtonSize : 0);
                    int buttonSizePx = (int) android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_DIP, maxBtnSize, getResources().getDisplayMetrics());
                    rightMargin += buttonSizePx + 16;
                }
                params.rightMargin = rightMargin;
            }
            timeView.setLayoutParams(params);
        }

        if (settingsButton != null && settingsButton.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) settingsButton.getLayoutParams();
            params.topMargin = topInset + homePaddingTopPx + 5;
            params.rightMargin = homePaddingRightPx + 16;
            settingsButton.setLayoutParams(params);
        }

        if (searchButton != null && searchButton.getParent() == rootLayout && showSettingsButton != 1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchButton.getLayoutParams();
            params.topMargin = topInset + homePaddingTopPx + 5;
            params.rightMargin = homePaddingRightPx + 16;
            searchButton.setLayoutParams(params);
        } else if (searchButton != null && searchButton.getParent() == rootLayout) {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchButton.getLayoutParams();
            params.rightMargin = homePaddingRightPx + 16;
            searchButton.setLayoutParams(params);
        }

        if (appBarView != null && appBarView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) appBarView.getLayoutParams();
            int appBarPosition = getSharedPreferences("prefs", MODE_PRIVATE).getInt("app_bar_position", 0);
            boolean topAnchored = (appBarPosition == 0 || appBarPosition == 1 || appBarPosition == 6);
            boolean bottomAnchored = (appBarPosition == 2 || appBarPosition == 3 || appBarPosition == 7);
            float density = getResources().getDisplayMetrics().density;
            int appbarMargin = (int) (8 * density);
            params.leftMargin = appbarMargin + homePaddingLeftPx;
            params.rightMargin = appbarMargin + homePaddingRightPx;
            params.topMargin = appbarMargin + homePaddingTopPx + (topAnchored ? topInset : 0);
            params.bottomMargin = appbarMargin + homePaddingBottomPx + (bottomAnchored ? bottomInset : 0);
            appBarView.setLayoutParams(params);
        }

        if (dockView != null && dockView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dockView.getLayoutParams();
            float density = getResources().getDisplayMetrics().density;
            int dockMargin = (int) (8 * density);
            params.leftMargin = dockMargin + homePaddingLeftPx;
            params.rightMargin = dockMargin + homePaddingRightPx;
            params.topMargin = dockMargin;
            params.bottomMargin = dockMargin + homePaddingBottomPx + bottomInset;
            dockView.setLayoutParams(params);
        }

        if (musicDockView != null && musicDockView.getParent() == rootLayout) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) musicDockView.getLayoutParams();
            SharedPreferences musicPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
            int musicPosition = musicPrefs.getInt("music_dock_position", 7);
            boolean topAnchored = (musicPosition == 0 || musicPosition == 1 || musicPosition == 6);
            boolean bottomAnchored = (musicPosition == 2 || musicPosition == 3 || musicPosition == 7);
            float density = getResources().getDisplayMetrics().density;
            int musicMargin = (int) (8 * density);
            params.leftMargin = musicMargin + homePaddingLeftPx;
            params.rightMargin = musicMargin + homePaddingRightPx;
            params.topMargin = musicMargin + homePaddingTopPx + (topAnchored ? topInset : 0);
            params.bottomMargin = musicMargin + homePaddingBottomPx + (bottomAnchored ? bottomInset : 0);
            musicDockView.setLayoutParams(params);
        }

        if (mainLayout != null) {
            boolean hasTopAnchor = (timePosition == 1 && timeView != null) ||
                    (datePosition != 0 && dateView != null) ||
                    (showSettingsButton == 1 && settingsButton != null) ||
                    (showSearchButton == 1 && searchButton != null) ||
                    homeWidgetHost.hasVisibleWidgets();

            int effectiveTopPx = 0;
            if (!hasTopAnchor) {
                effectiveTopPx = homePaddingTopPx;
                if (homeVerticalAlignment == 0 && topInset > 0) {
                    effectiveTopPx += topInset;
                }
            }

            LinearLayout bottomBar = findViewById(R.id.bottom_bar);
            int bottomBarHeight = (bottomBar.getVisibility() == View.VISIBLE && homeVerticalAlignment == 2)
                    ? (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 48,
                            getResources().getDisplayMetrics())
                    : 0;
            int effectiveBottomPx = homePaddingBottomPx + bottomBarHeight + bottomInset;

            if (dockView != null && dockView.getParent() == rootLayout) {
                SharedPreferences dockPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
                boolean dockBorderEnabled = dockPrefs.getInt("dock_border", 0) == 1;
                boolean dockBgEnabled = dockPrefs.getInt("dock_background", 0) == 1;
                float density = getResources().getDisplayMetrics().density;
                int dockIconPx = (int) (dockPrefs.getInt("dock_icon_size", 24) * density);
                int dockSlotMargin = (int) (4 * density);
                int dockHeight = dockIconPx + 2 * dockSlotMargin;
                if (dockBorderEnabled || dockBgEnabled) {
                    int dockPad = (int) (6 * density);
                    dockHeight += 2 * dockPad;
                }
                effectiveBottomPx += dockHeight + (int) (8 * density);
            }

            if (mainLayout.getPaddingLeft() != homePaddingLeftPx ||
                    mainLayout.getPaddingTop() != effectiveTopPx ||
                    mainLayout.getPaddingRight() != homePaddingRightPx ||
                    mainLayout.getPaddingBottom() != effectiveBottomPx) {
                mainLayout.setPadding(homePaddingLeftPx, effectiveTopPx, homePaddingRightPx, effectiveBottomPx);
            }
        }

        homeWidgetHost.applyInsets(homePaddingLeftPx, homePaddingRightPx);
    }

    private void createHomeLayout() {

        if (mainLayout != null) {
            mainLayout.setClipChildren(false);
            mainLayout.setClipToPadding(false);
        }

        if (homeColumns > 1) {
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < homeColumns; col++) {
                LinearLayout columnLayout = new LinearLayout(this);
                columnLayout.setOrientation(LinearLayout.VERTICAL);
                columnLayout.setGravity(getHorizontalGravity(homeAlignment) | Gravity.CENTER_VERTICAL);
                columnLayout
                        .setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                columnLayout.setClipChildren(false);
                columnLayout.setClipToPadding(false);
                for (int i = 0; i < maxApps; i++) {
                    int index = col * maxApps + i;
                    createAppSlot(columnLayout, index);
                }
                mainLayout.addView(columnLayout);
            }
        } else {
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            if (mainLayout != null) {
                mainLayout.setClipChildren(false);
                mainLayout.setClipToPadding(false);
            }
            for (int i = 0; i < maxApps; i++) {
                createAppSlot(mainLayout, i);
            }
        }
        updateGravity();
    }

    private void createAppSlot(LinearLayout parent, int index) {
        LinearLayout slotLayout = new LinearLayout(this);
        slotLayout.setClipChildren(false);
        slotLayout.setClipToPadding(false);

        if (showAppNames && (appNamePosition == AppNamePositionHelper.POSITION_TOP
                || appNamePosition == AppNamePositionHelper.POSITION_BOTTOM)) {
            slotLayout.setOrientation(LinearLayout.VERTICAL);
            slotLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            slotLayout.setOrientation(LinearLayout.HORIZONTAL);
            slotLayout.setGravity(Gravity.CENTER_VERTICAL);
        }

        if (appNamePosition == AppNamePositionHelper.POSITION_LEFT && showIcons && showAppNames) {
            slotLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            slotLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        TextView tv = new StrokeTextView(this);
        tv.setText(appLabels.get(index));
        tv.setTextColor(getAppTextColorValue());
        tv.setTextSize(textSize);
        FontHelper.applyStyled(this, tv, FontHelper.SLOT_APP_LIST, boldText);
        applyTextEffect(tv);
        tv.setBackgroundColor(0);

        if (appNamePosition == AppNamePositionHelper.POSITION_LEFT && showIcons && showAppNames) {
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        tv.setGravity(getHorizontalGravity(homeAlignment));
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setVisibility(showAppNames ? View.VISIBLE : View.GONE);
        if (appPackages.get(index).equals("blank") || appPackages.get(index).isEmpty()) {
            tv.setText(appPackages.get(index).equals("blank") ? "" : appLabels.get(index));
            tv.setVisibility(appPackages.get(index).equals("blank") ? View.GONE : View.VISIBLE);
        }

        int vPadding = homeColumns == 1 ? 20 : 16;
        int leftOuterPadding = homeColumns == 1 ? (showIcons ? 16 : 32) : (showIcons ? 8 : 16);
        if (appNamePosition == AppNamePositionHelper.POSITION_TOP
                || appNamePosition == AppNamePositionHelper.POSITION_BOTTOM) {
            tv.setPadding(8, appNamePosition == AppNamePositionHelper.POSITION_TOP ? vPadding : 8, 8,
                    appNamePosition == AppNamePositionHelper.POSITION_BOTTOM ? vPadding : 8);
        } else {
            tv.setPadding(leftOuterPadding, vPadding, 32, vPadding);
        }

        if (appNamePosition == AppNamePositionHelper.POSITION_TOP) {
            slotLayout.addView(tv);
        } else if (appNamePosition == AppNamePositionHelper.POSITION_LEFT) {
            slotLayout.addView(tv);
        }

        if (showIcons || appPackages.get(index).equals("blank")) {
            ImageView iconView = new ImageView(this);
            String pkg = appPackages.get(index);
            boolean isSpecial = "launcher_settings".equals(pkg) || "app_launcher".equals(pkg)
                    || "notification_panel".equals(pkg) || "koreader_history".equals(pkg)
                    || "calendar".equals(pkg) || "bigme_control_panel".equals(pkg)
                    || (pkg != null && pkg.startsWith("folder_"))
                    || (pkg != null && pkg.startsWith("webapp_"));
            iconView.setTag(isSpecial ? "special" : "app");

            int iconSizePx = (int) (iconSize * getResources().getDisplayMetrics().scaledDensity);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));

            int iconPaddingLeft = 0, iconPaddingRight = 0, iconPaddingTop = 0, iconPaddingBottom = 0;
            if (showAppNames) {
                if (appNamePosition == AppNamePositionHelper.POSITION_RIGHT) {
                    iconPaddingRight = 16;
                } else if (appNamePosition == AppNamePositionHelper.POSITION_LEFT) {
                    iconPaddingLeft = 16;
                } else if (appNamePosition == AppNamePositionHelper.POSITION_TOP) {
                    iconPaddingTop = 8;
                } else if (appNamePosition == AppNamePositionHelper.POSITION_BOTTOM) {
                    iconPaddingBottom = 8;
                }
            }

            if (appPackages.get(index).isEmpty() || appPackages.get(index).equals("blank")) {
                iconView.setImageDrawable(
                        new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                iconView.setVisibility(View.INVISIBLE);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("launcher_settings".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.settings, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.settings);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("app_launcher".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.search, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.search);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("koreader_history".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.koreader, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.koreader);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("calendar".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.date, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.date);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("gallery".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.gallery, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.gallery);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("clock".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.time, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.time);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("calculator".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.calculator, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.calculator);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("notification_panel".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.notifications, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.notifications);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if ("bigme_control_panel".equals(appPackages.get(index))) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.generic_app, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.generic_app);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if (appPackages.get(index).startsWith("folder_")) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.folder, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.folder);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else if (appPackages.get(index).startsWith("webapp_")) {
                if (dynamicIcons || iconBackground) {
                    Drawable specialIcon = DynamicIconHelper.createSpecialIcon(this, R.drawable.webapps, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape);
                    iconView.setImageDrawable(specialIcon);
                    iconView.clearColorFilter();
                } else {
                    iconView.setImageResource(R.drawable.webapps);
                    iconView.setColorFilter(getSpecialIconColor());
                }
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                slotLayout.addView(iconView);
            } else {
                try {
                    Drawable drawable = DynamicIconHelper.getAppIcon(this, appPackages.get(index), dynamicIcons, theme,
                            iconBackground, dynamicColors, invertIconColors, iconShape,
                            forceMonochromeFallback);
                    iconView.setImageDrawable(drawable);
                    if (monochromeIcons) {
                        iconView.setColorFilter(IconMonochromeHelper.getMonochromeFilter());
                    } else {
                        iconView.clearColorFilter();
                    }
                    iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    iconView.setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                    slotLayout.addView(iconView);
                } catch (Exception e) {
                    return;
                }
            }
            applyIconEffect(iconView);
        }

        if (appNamePosition == AppNamePositionHelper.POSITION_RIGHT) {
            slotLayout.addView(tv);
        } else if (appNamePosition == AppNamePositionHelper.POSITION_BOTTOM) {
            slotLayout.addView(tv);
        }

        if (!showAppNames) {
            int vPaddingFull = homeColumns == 1 ? 20 : 16;
            if (homeAlignment == 1) {
                int symmetricPadding = homeColumns == 1 ? 20 : 12;
                slotLayout.setPadding(symmetricPadding, vPaddingFull, symmetricPadding, vPaddingFull);
            } else {
                int leftPaddingIconOnly = homeColumns == 1 ? (showIcons ? 16 : 32) : (showIcons ? 8 : 16);
                slotLayout.setPadding(leftPaddingIconOnly, vPaddingFull, 32, vPaddingFull);
            }
        }

        final int pos = index;

        parent.addView(slotLayout);
        appSlots[index] = slotLayout;
    }

    private void adjustSlotsForMaxAppsChange(int oldMaxApps, int newMaxApps, int homePages, int homeColumns) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int page = 0; page < homePages; page++) {
            List<String> newLabels = new ArrayList<>();
            List<String> newPackages = new ArrayList<>();

            for (int col = 0; col < homeColumns; col++) {

                List<String> colLabels = new ArrayList<>();
                List<String> colPackages = new ArrayList<>();
                for (int i = 0; i < oldMaxApps; i++) {
                    int index = col * oldMaxApps + i;
                    String label = prefs.getString("slot_label_page_" + page + "_" + index, "Empty");
                    String pkg = prefs.getString("slot_pkg_page_" + page + "_" + index, "");
                    colLabels.add(label);
                    colPackages.add(pkg);
                }

                if (newMaxApps > oldMaxApps) {

                    int diff = newMaxApps - oldMaxApps;
                    for (int i = 0; i < diff; i++) {
                        colLabels.add("Empty");
                        colPackages.add("");
                    }
                } else if (newMaxApps < oldMaxApps) {

                    int diff = oldMaxApps - newMaxApps;
                    for (int i = 0; i < diff; i++) {
                        colLabels.remove(colLabels.size() - 1);
                        colPackages.remove(colPackages.size() - 1);
                    }
                }

                newLabels.addAll(colLabels);
                newPackages.addAll(colPackages);
            }

            int totalApps = homeColumns * newMaxApps;
            for (int i = 0; i < totalApps; i++) {
                editor.putString("slot_label_page_" + page + "_" + i, newLabels.get(i));
                editor.putString("slot_pkg_page_" + page + "_" + i, newPackages.get(i));
            }
        }

        editor.apply();
    }

    private int getSpecialIconColor() {
        int[] dynamicColorPair = DynamicIconHelper.getDynamicColors(this, theme, iconBackground, invertIconColors,
                dynamicColors);
        return dynamicColorPair[0];
    }

    private TextView getSlotTextView(LinearLayout slot) {
        if (slot == null)
            return null;
        for (int i = 0; i < slot.getChildCount(); i++) {
            if (slot.getChildAt(i) instanceof TextView) {
                return (TextView) slot.getChildAt(i);
            }
        }
        return null;
    }

    private ImageView getSlotImageView(LinearLayout slot) {
        if (slot == null)
            return null;
        for (int i = 0; i < slot.getChildCount(); i++) {
            if (slot.getChildAt(i) instanceof ImageView) {
                return (ImageView) slot.getChildAt(i);
            }
        }
        return null;
    }
}
