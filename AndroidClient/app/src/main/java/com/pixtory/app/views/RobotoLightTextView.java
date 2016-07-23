package com.pixtory.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by training3 on 01/06/2016 AD.
 */
public class RobotoLightTextView extends TextView{
    public RobotoLightTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        setTypeFace();
    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }

    private void setTypeFace(){
        setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }
}
