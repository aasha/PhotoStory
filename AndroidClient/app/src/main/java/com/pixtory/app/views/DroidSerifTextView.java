package com.pixtory.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.pixtory.app.R;

/**
 * Created by krish on 24/06/2016.
 */
public class DroidSerifTextView extends TextView {
    public DroidSerifTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public DroidSerifTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public DroidSerifTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs!=null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DroidSerifTextView);
            String fontName = a.getString(R.styleable.DroidSerifTextView_fontName);
            if (fontName!=null) {
                Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+fontName);
                setTypeface(myTypeface);
            }
            a.recycle();
        }
    }

}


