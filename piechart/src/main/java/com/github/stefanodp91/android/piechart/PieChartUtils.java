package com.github.stefanodp91.android.piechart;

class PieChartUtils {

     static final float HALF_DIVIDER = 0.5f;
     static final float DEFAULT_SIZE_DIVIDER = 0.15f; // 1/30

    // Offset = -90 indicates that the progress starts from 12 o'clock.
     static final int ANGLE_OFFSET = -90;
     static final int ARCH_ALPHA = 50;
     static final float CIRCUMFERENCE_DEGREES = 360f;
     static final float BASE_CIRCLE_SWEEP_ANGLE = CIRCUMFERENCE_DEGREES;
     static final float ARC_ANGLE_PADDING = 1.5f;
     static final float HIGHLIGHT_ARC_MULTIPLIER = 1.5f;
     static final int HIGHLIGHT_ARC_SLEEP = 5;
     static final float HIGHLIGHT_ARC_INCREMENT = 0.6f;
     static final float HIGHLIGHT_ARC_DECREMENT = HIGHLIGHT_ARC_INCREMENT;
     static final int DEFAULT_WIDTH = 32;

    static boolean isPointOnCircumference(float centerX, float centerY, float touchedX, float touchedY, float width, float radius) {
        float distance = distanceBetweenPoints(centerX, centerY, touchedX, touchedY);
        return radius - width <= distance && radius + width >= distance;
    }

    static float distanceBetweenPoints(float centerX, float centerY, float touchedX, float touchedY) {
        return (float) Math.sqrt(Math.pow(touchedX - centerX, 2) + Math.pow(touchedY - centerY, 2));
    }
}
