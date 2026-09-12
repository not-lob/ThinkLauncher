package org.matiasdesu.thinklauncherv2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * A solid filled WiFi wedge, matching Android's own status-bar glyph (a single filled pie-slice
 * cone, not stroked arcs) rather than a hand-drawn dot-and-arcs icon. Signal level scales the
 * wedge's radius - smaller cone for a weaker signal, full-size cone at max - the same way Android's
 * own signal_wifi_N_bar icon set literally uses progressively larger cones per level. Offline shows
 * a faint full-size cone with a diagonal slash through it, echoing the standard "wifi off" glyph.
 */
public class WifiLevelView extends View {

    private final Paint solidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint faintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int level = 4;
    private boolean offline = false;

    public WifiLevelView(Context context) {
        super(context);
        init();
    }

    public WifiLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        solidPaint.setStyle(Paint.Style.FILL);
        faintPaint.setStyle(Paint.Style.FILL);
        setColor(0xFF000000);
    }

    public void setColor(int color) {
        solidPaint.setColor(color);
        faintPaint.setColor(color);
        faintPaint.setAlpha(60);
        invalidate();
    }

    /** level: 0 (weakest) - 4 (full signal). Ignored when offline. */
    public void setState(int level, boolean offline) {
        this.level = Math.max(0, Math.min(4, level));
        this.offline = offline;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float stroke = Math.max(1f, 1.6f * density);
        float strokeHalf = stroke / 2f;

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        // The wedge's vertex sits at (cx, cy); drawArc's bounding oval is centered there too, so
        // the same radius bounds a straight-up 90deg pie slice on both axes without clipping:
        // top of the arc is at (cx, cy - radius), sides at cx +/- radius*sin(45deg).
        float cy = h - strokeHalf;
        float maxRadiusVertical = cy - strokeHalf;
        float maxRadiusHorizontal = (cx - strokeHalf) / 0.7071f;
        float maxRadius = Math.min(maxRadiusVertical, maxRadiusHorizontal);

        if (offline) {
            RectF oval = new RectF(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius);
            canvas.drawArc(oval, 225, 90, true, faintPaint);
            canvas.drawLine(strokeHalf, strokeHalf, w - strokeHalf, h - strokeHalf, solidPaint);
            return;
        }

        // Levels 1-4 map to progressively larger cones (level 0 = smallest sliver, matching the
        // "weak signal" end of Android's own bar icons rather than disappearing entirely).
        float minRadiusFraction = 0.35f;
        float fraction = minRadiusFraction + (1f - minRadiusFraction) * (level / 4f);
        float radius = maxRadius * fraction;
        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(oval, 225, 90, true, solidPaint);
    }
}
