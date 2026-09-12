package org.matiasdesu.thinklauncherv2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Draws the classic dot-plus-arcs WiFi glyph, with 0-4 arcs lit solid and the rest left faint, or a
 * slashed dot when offline. Hand-drawn rather than shipped as vector-drawable levels: this keeps the
 * icon a plain runtime-tinted View like {@link BatteryLevelView}, matching every other themed icon
 * on the home screen, without hand-typing vector path data for five separate drawables.
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
        solidPaint.setStyle(Paint.Style.STROKE);
        solidPaint.setStrokeCap(Paint.Cap.ROUND);
        faintPaint.setStyle(Paint.Style.STROKE);
        faintPaint.setStrokeCap(Paint.Cap.ROUND);
        setColor(0xFF000000);
    }

    public void setColor(int color) {
        solidPaint.setColor(color);
        faintPaint.setColor(color);
        faintPaint.setAlpha(60);
        invalidate();
    }

    /** level: 0 (no bars) - 4 (full signal). Ignored when offline. */
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
        solidPaint.setStrokeWidth(stroke);
        faintPaint.setStrokeWidth(stroke);

        float w = getWidth();
        float h = getHeight();
        float dotRadius = Math.max(1.5f * density, w * 0.06f);
        float cx = w / 2f;
        float cy = h - dotRadius - stroke;

        canvas.drawCircle(cx, cy, dotRadius, offline ? faintPaint : solidPaint);

        // Three concentric arcs above the dot, mapped from 4 signal levels: level 4 lights all
        // three, level 1 lights none (just the dot), matching the familiar phone-status-bar look.
        int arcs = 3;
        float maxRadius = Math.min(w, h) - stroke;
        for (int i = 0; i < arcs; i++) {
            float radius = maxRadius * (i + 1) / (float) arcs;
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            boolean lit = !offline && level >= i + 2;
            canvas.drawArc(oval, 225, 90, false, lit ? solidPaint : faintPaint);
        }

        if (offline) {
            float pad = stroke;
            canvas.drawLine(pad, pad, w - pad, h - pad, solidPaint);
        }
    }
}
