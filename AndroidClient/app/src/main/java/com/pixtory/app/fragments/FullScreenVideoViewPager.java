package com.pixtory.app.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by aasha.medhi on 12/24/15.
 */
public class FullScreenVideoViewPager extends ViewPager {
    public boolean isScrollingEnabled = true;
    public FullScreenVideoViewPager(Context context) {
        super(context);
    }

    public FullScreenVideoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isScrollingEnabled == true)
            return super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(isScrollingEnabled == true)
            return super.onInterceptTouchEvent(event);
        return false;
    }


}
