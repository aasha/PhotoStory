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
    /**
     * Max blur radius supported by the Renderscript library
     **/
    protected static final float MAX_RADIUS = 25;
    /**
     * Min blur radius supported by the Renderscript library
     **/
    protected static final float MIN_RADIUS = 1;
    /**
     * Application context to instantiate the Renderscript
     **/
    protected final Context context;
    /**
     * Selected radius
     **/
    protected final float radius;

    /**
     * Creates a new Blur transformation
     *
     * @param context Application context to instantiate the Renderscript
     **/
    public BlurTransformation(Context context, float radius) {
        this.context = context;
        this.radius = radius < MIN_RADIUS ? MIN_RADIUS :
                radius > MAX_RADIUS ? MAX_RADIUS : radius;
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
        theIntrinsic.setRadius(7.5f);
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