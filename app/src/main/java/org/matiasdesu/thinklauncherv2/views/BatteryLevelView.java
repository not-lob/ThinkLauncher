package org.matiasdesu.thinklauncherv2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * A minimal outline-and-fill battery gauge, matching the plain-lines e-ink look (no bitmap assets,
 * no vector level-drawable list to ship). Percent-filled from the left, proportional to
 * {@link #setPercent}. Color is set at runtime from the current theme's text color, same as every
 * other home-screen icon in this launcher.
 */
public class BatteryLevelView extends View {

    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int percent = 100;

    public BatteryLevelView(Context context) {
        super(context);
        init();
    }

    public BatteryLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        outlinePaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStyle(Paint.Style.FILL);
        setColor(0xFF000000);
    }

    public void setColor(int color) {
        outlinePaint.setColor(color);
        fillPaint.setColor(color);
        invalidate();
    }

    public void setPercent(int percent) {
        this.percent = Math.max(0, Math.min(100, percent));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float stroke = Math.max(1f, 1.5f * density);
        outlinePaint.setStrokeWidth(stroke);

        float capWidth = 2 * density;
        float capHeight = getHeight() * 0.4f;
        float bodyRight = getWidth() - capWidth;

        RectF body = new RectF(stroke / 2f, stroke / 2f, bodyRight, getHeight() - stroke / 2f);
        float corner = 2 * density;
        canvas.drawRoundRect(body, corner, corner, outlinePaint);

        RectF cap = new RectF(bodyRight, (getHeight() - capHeight) / 2f, getWidth() - stroke / 2f,
                (getHeight() + capHeight) / 2f);
        canvas.drawRoundRect(cap, corner / 2f, corner / 2f, fillPaint);

        float inset = stroke + density;
        float fillableWidth = body.width() - 2 * inset;
        float fillWidth = fillableWidth * (percent / 100f);
        if (fillWidth > 0) {
            RectF fill = new RectF(body.left + inset, body.top + inset,
                    body.left + inset + fillWidth, body.bottom - inset);
            canvas.drawRoundRect(fill, corner / 2f, corner / 2f, fillPaint);
        }
    }
}
