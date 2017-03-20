package ee.tartu.jpg.minuposka.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

/**
 * Provides useful calculations.
 */
public class Calculations {

    public static float toPixels(Context context, float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static boolean isDarkColor(int color) {
        return Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114 <= 120;
    }
}
