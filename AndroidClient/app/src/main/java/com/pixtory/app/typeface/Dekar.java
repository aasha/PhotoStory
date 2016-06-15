package com.pixtory.app.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by aasha.medhi on 11/24/15.
 */
public class Dekar {
    private static Typeface mFont = null;

    private static Typeface getFontTypeface(Context context) {
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), "fonts/Dekar.otf");
        return mFont;
    }

    public static TextView applyFont(Context context, TextView textView) {
        if (textView != null)
            textView.setTypeface(getFontTypeface(context));
        return textView;
    }

    public static TextView applyFont(Context context, TextView textView, String font){
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), font);

        if (textView != null)
            textView.setTypeface(getFontTypeface(context));

        return textView;

    }

    public static Button applyFont(Context context, Button btn, String font){
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), font);

        if (btn != null)
            btn.setTypeface(getFontTypeface(context));

        return btn;

    }
}

