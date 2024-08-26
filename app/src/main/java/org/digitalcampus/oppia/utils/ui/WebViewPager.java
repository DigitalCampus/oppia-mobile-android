package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class WebViewPager extends ViewPager {
    public WebViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ExtendedWebView) {
            return ((ExtendedWebView) v).canScrollHor(-dx);
        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }
}