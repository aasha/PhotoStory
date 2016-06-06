package com.pixtory.app.transformations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by krish on 02/06/2016.
 */
public class GrayscaleTransformation implements Transformation {

    private final Context context;

    public GrayscaleTransformation(Context context) {
        this.context = context;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        ColorMatrix saturation = new ColorMatrix();
        saturation.setSaturation(0f);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(saturation));
        canvas.drawBitmap(source, 0, 0, paint);
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "grayscaleTransformation()";
    }
}
