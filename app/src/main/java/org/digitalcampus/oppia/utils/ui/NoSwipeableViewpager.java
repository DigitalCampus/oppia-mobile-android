package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSwipeableViewpager extends ViewPager {

    private boolean canScroll;

    public NoSwipeableViewpager(Context context) {
        super(context);
        canScroll = false;
    }
    public NoSwipeableViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
        canScroll = false;
    }
    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll && super.onTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canScroll && super.onInterceptTouchEvent(ev);
    }

}