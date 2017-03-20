package ee.tartu.jpg.minuposka.ui.widget;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ee.tartu.jpg.minuposka.ui.util.DataUtils;

/**
 * Provides custom drawer layout, which stays visible on tablet-sized devices
 */
public class CustomDrawerLayout extends DrawerLayout {
    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return DataUtils.hideDrawer(getContext()) && super.onInterceptTouchEvent(ev);
    }
}
