package com.pixtory.app.transformations;

/**
 * Created by sriram on 01/06/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;


import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {

    private static final float BITMAP_SCALE = 0.1f;
    private static final float BLUR_RADIUS = 7.5f;
    protected static final int UP_LIMIT = 25;
    protected static final int LOW_LIMIT = 1;
    protected final Context context;
    protected final float blurRadius;


    public BlurTransformation(Context context, float radius) {
        this.context = context;

        if(radius<LOW_LIMIT){
            this.blurRadius = LOW_LIMIT;
        }else if(radius>UP_LIMIT){
            this.blurRadius = UP_LIMIT;
        }else
            this.blurRadius = radius;
    }

    public BlurTransformation(Context context){
        this.context = context;
        blurRadius = BLUR_RADIUS;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = Math.round(source.getWidth() * BITMAP_SCALE);
        int height = Math.round(source.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(source, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);


        source.recycle();
        return outputBitmap;
    }

    @Override
    public String key() {
        return "blurred";
    }
}
