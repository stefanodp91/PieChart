package com.github.stefanodp91.android.piechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import static com.github.stefanodp91.android.piechart.PieChartUtils.ANGLE_OFFSET;
import static com.github.stefanodp91.android.piechart.PieChartUtils.ARCH_ALPHA;
import static com.github.stefanodp91.android.piechart.PieChartUtils.ARC_ANGLE_PADDING;
import static com.github.stefanodp91.android.piechart.PieChartUtils.BASE_CIRCLE_SWEEP_ANGLE;
import static com.github.stefanodp91.android.piechart.PieChartUtils.CIRCUMFERENCE_DEGREES;
import static com.github.stefanodp91.android.piechart.PieChartUtils.DEFAULT_SIZE_DIVIDER;
import static com.github.stefanodp91.android.piechart.PieChartUtils.DEFAULT_WIDTH;
import static com.github.stefanodp91.android.piechart.PieChartUtils.HALF_DIVIDER;
import static com.github.stefanodp91.android.piechart.PieChartUtils.HIGHLIGHT_ARC_DECREMENT;
import static com.github.stefanodp91.android.piechart.PieChartUtils.HIGHLIGHT_ARC_INCREMENT;
import static com.github.stefanodp91.android.piechart.PieChartUtils.HIGHLIGHT_ARC_MULTIPLIER;
import static com.github.stefanodp91.android.piechart.PieChartUtils.HIGHLIGHT_ARC_SLEEP;
import static com.github.stefanodp91.android.piechart.PieChartUtils.isPointOnCircumference;

public class PieChart extends View {

    // base circle
    private float mBaseCircleRadius = 0;
    private float mBaseCircleWidth = DEFAULT_WIDTH;
    private RectF mBaseCircleRect;
    private Paint mBaseCirclePaint;
    private Matrix baseCircleSweepGradientMatrix;
    private @ColorInt
    int[] mBaseCircleColorList;

    // arcs
    private List<Arc> mArcList;
    private float mArcRadius = 0;
    private RectF mArcRect;
    private float mArcWidth = DEFAULT_WIDTH;

    private OnArcClickListener mOnArcCLickListener;

    public PieChart(Context context) {
        this(context, null, -1);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttributes(attrs);
        init();
    }

    private void initAttributes(AttributeSet attributeSet) {
        if (attributeSet != null) {
            final TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PieChart);
            mBaseCircleWidth = typedArray.getDimension(R.styleable.PieChart_pcv_baseCircleWidth, mBaseCircleWidth);
            mArcWidth = mBaseCircleWidth;
            mBaseCircleRadius = typedArray.getDimension(R.styleable.PieChart_pcv_baseCircleRadius, 0);
            mArcRadius = mBaseCircleRadius;
            int colorListId = typedArray.getResourceId(R.styleable.PieChart_pcv_baseCircleColorList, R.array.default_base_circle_color_list);
            this.mBaseCircleColorList = getResources().getIntArray(colorListId);
            typedArray.recycle();
        }
    }

    private void init() {
        // base circle
        mBaseCircleRect = new RectF();
        mBaseCirclePaint = new Paint();
        mBaseCirclePaint.setAntiAlias(true);
        mBaseCirclePaint.setStyle(Paint.Style.STROKE);
        mBaseCirclePaint.setStrokeWidth(mBaseCircleWidth);
        baseCircleSweepGradientMatrix = new Matrix();

        // arcs
        mArcList = new ArrayList<>();
        mArcRect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mBaseCircleRadius == 0) {
            mBaseCircleRadius = height * DEFAULT_SIZE_DIVIDER;
            mArcRadius = mBaseCircleRadius;
        }

        // base circle
        float mBaseCircleDiameter = 2 * mBaseCircleRadius;
        float mTop = height * HALF_DIVIDER - (mBaseCircleDiameter * HALF_DIVIDER);
        float mLeft = width * HALF_DIVIDER - (mBaseCircleDiameter * HALF_DIVIDER);
        float mRight = mLeft + mBaseCircleDiameter;
        float mBottom = mTop + mBaseCircleDiameter;
        mBaseCircleRect.set(mLeft, mTop, mRight, mBottom);

        // arcs
        mArcRect.set(mLeft, mTop, mRight, mBottom);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // base circle
        baseCircleSweepGradientMatrix.setRotate(ANGLE_OFFSET, mBaseCircleRect.centerX(), mBaseCircleRect.centerY());
        SweepGradient sweepGradient = new SweepGradient(mBaseCircleRect.centerX(), mBaseCircleRect.centerY(), this.mBaseCircleColorList, null);
        sweepGradient.setLocalMatrix(baseCircleSweepGradientMatrix);
        mBaseCirclePaint.setShader(sweepGradient);
        mBaseCirclePaint.setAlpha(ARCH_ALPHA);

        // arc
        if (mArcList != null && mArcList.size() > 0) {
            for (Arc arc : mArcList) {
                if (arc != null) {

                    arc.setRect(mArcRect);
                    arc.setRadius(mArcRadius);

                    // shader
                    Matrix mArcPaintMatrix = new Matrix();
                    mArcPaintMatrix.setRotate(ANGLE_OFFSET, arc.getRect().centerX(), arc.getRect().centerY());
                    SweepGradient mArcShader = new SweepGradient(arc.getRect().centerX(), arc.getRect().centerY(), arc.getColorList(), null);
                    mArcShader.setLocalMatrix(mArcPaintMatrix);

                    // paint
                    Paint mArcPaint = new Paint();
                    mArcPaint.setAntiAlias(true);
                    mArcPaint.setStrokeWidth(mArcWidth);
                    mArcPaint.setShader(mArcShader);
                    mArcPaint.setStyle(Paint.Style.STROKE);
                    arc.setPaint(mArcPaint);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // base circle
        canvas.save();
        canvas.drawArc(mBaseCircleRect, ANGLE_OFFSET, BASE_CIRCLE_SWEEP_ANGLE, true, mBaseCirclePaint);
        canvas.restore();

        // arcs
        canvas.save();
        if (mArcList != null && mArcList.size() > 0) {
            for (Arc arc : mArcList) {
                if (arc != null) {
                    drawArc(canvas, arc);
                }
            }
        }
        canvas.restore();
    }

    private void drawArc(Canvas canvas, Arc arc) {
        canvas.save();
        canvas.drawArc(arc.getRect(), arc.getStartAngle(), arc.getSweepAngle() - ARC_ANGLE_PADDING, false, arc.getPaint());
        canvas.restore();
    }

    public void addArc(String id, float startAngle, float sweepAngle, @ColorInt int[] colorList) {

        Arc arc = createArc(id, startAngle, sweepAngle, colorList);

        mArcList.add(arc);

        invalidate();
    }

    private Arc createArc(String id, float startAngle, float sweepAngle, @ColorInt int[] colorList) {

        // arc
        Arc arc = new Arc();
        arc.setId(id);
        arc.setStartAngle(startAngle);
        arc.setSweepAngle(sweepAngle);
        arc.setColorList(colorList);

        return arc;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                unHighlightArcs();
                selectArc(e.getX(), e.getY());
                return true;
            default:
                return super.onTouchEvent(e);
        }
    }

    private void selectArc(float touchedX, float touchedY) {
        float centerX = getWidth() * HALF_DIVIDER;
        float centerY = getHeight() * HALF_DIVIDER;
        float distanceX = touchedX - centerX;
        float distanceY = touchedY - centerY;

        float angle = (float) (Math.toDegrees(Math.atan2(distanceY, distanceX)) + CIRCUMFERENCE_DEGREES) % CIRCUMFERENCE_DEGREES;
        angle = (angle < 0) ? (angle + CIRCUMFERENCE_DEGREES) : angle;

        boolean isOnCircumference = isPointOnCircumference(centerX, centerY, touchedX, touchedY, mArcWidth, mArcRadius);

        if (mArcList != null && mArcList.size() > 0) {
            for (int i = 0; i < mArcList.size(); i++) {
                Arc current = mArcList.get(i);
                if (current != null) {
                    float endAngle = current.startAngle + current.sweepAngle;
                    if (endAngle > CIRCUMFERENCE_DEGREES) {
                        current.startAngle = 0;
                        float diff = endAngle - CIRCUMFERENCE_DEGREES;
                        endAngle = current.startAngle + diff;
                    }
                    if (angle >= current.startAngle && angle <= endAngle && isOnCircumference) {
                        current.highlightArc(this, current.getPaint().getStrokeWidth() * HIGHLIGHT_ARC_MULTIPLIER);
                        if (mOnArcCLickListener != null) {
                            mOnArcCLickListener.onArcClicked(current);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void unHighlightArcs() {
        if (mArcList != null && mArcList.size() > 0) {
            for (int i = 0; i < mArcList.size(); i++) {
                final Arc arc = mArcList.get(i);
                arc.unHighlightArcs(this, mArcWidth);
            }
        }
    }

    public OnArcClickListener getOnArcCLickListener() {
        return mOnArcCLickListener;
    }

    public void setOnArcCLickListener(OnArcClickListener onArcCLickListener) {
        mOnArcCLickListener = onArcCLickListener;
    }

    public static class Arc {
        private String id;
        private float radius;
        private float startAngle;
        private float sweepAngle;
        private @ColorInt
        int[] colorList;
        private RectF rect;
        private Paint paint;
        private boolean highlighted;

        public Arc() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public float getStartAngle() {
            return startAngle;
        }

        public void setStartAngle(float startAngle) {
            this.startAngle = startAngle;
        }

        public float getSweepAngle() {
            return sweepAngle;
        }

        public void setSweepAngle(float sweepAngle) {
            this.sweepAngle = sweepAngle;
        }

        public int[] getColorList() {
            return colorList;
        }

        public void setColorList(int[] colorList) {
            this.colorList = colorList;
        }

        private RectF getRect() {
            return rect;
        }

        private void setRect(RectF rect) {
            this.rect = rect;
        }

        private Paint getPaint() {
            return paint;
        }

        private void setPaint(Paint paint) {
            this.paint = paint;
        }

        private float getRadius() {
            return radius;
        }

        private void setRadius(float radius) {
            this.radius = radius;
        }

        private void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

        private boolean isHighlighted() {
            return highlighted;
        }

        private void highlightArc(final PieChart pieChart, final float maxStrokeWidth) {
            if (!isHighlighted()) {
                new Thread(new Runnable() {
                    float increment = paint.getStrokeWidth();

                    public void run() {
                        while (increment < maxStrokeWidth) {
                            paint.setStrokeWidth(increment);
                            setHighlighted(true);
                            increment += HIGHLIGHT_ARC_INCREMENT;
                            pieChart.invalidate();
                            try {
                                Thread.sleep(HIGHLIGHT_ARC_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }

        private void unHighlightArcs(final PieChart pieChart, final float minStrokeWidth) {
            if (isHighlighted()) {
                new Thread(new Runnable() {
                    float decrement = paint.getStrokeWidth();

                    public void run() {
                        while (decrement > minStrokeWidth) {
                            paint.setStrokeWidth(decrement);
                            setHighlighted(false);
                            decrement -= HIGHLIGHT_ARC_DECREMENT;
                            pieChart.invalidate();
                            try {
                                Thread.sleep(HIGHLIGHT_ARC_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }
    }
}