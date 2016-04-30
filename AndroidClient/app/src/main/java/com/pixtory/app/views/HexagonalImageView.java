package com.pixtory.app.views;

/**
 * Created by aasha.medhi on 11/21/15.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

public class HexagonalImageView extends ImageView {
    private Context mContext;
    public float cornerRadius = 30.0f;
    public HexagonalImageView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
    }

    private Path hexagonPath;

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
        int w = getWidth();

        Bitmap roundBitmap = getRoundedCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap finalBitmap;
        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
            finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
                    false);
        else
            finalBitmap = bitmap;


        Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
                finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
                finalBitmap.getHeight());
        hexagonPath = new Path();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        float density  = getResources().getDisplayMetrics().density;

        calculatePath(this.getPivotX(), this.getPivotY(), radius/2);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        paint.setStrokeWidth(6);
        CornerPathEffect corEffect = new CornerPathEffect(cornerRadius);
        paint.setPathEffect(corEffect);           // set the size
//        paint.setDither(true);                    // set the dither to true
//        paint.setStyle(Paint.Style.STROKE);       // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setAntiAlias(true);
        canvas.drawPath(hexagonPath, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        hexagonPath.close();
        canvas.drawBitmap(finalBitmap, rect, rect, paint);

        return output;
    }


    private void calculatePath(float centerX, float centerY, float radius) {
        float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
        hexagonPath.reset();
        hexagonPath.moveTo(centerX, centerY + radius);
        hexagonPath.lineTo(centerX - triangleHeight, centerY + radius / 2);
        hexagonPath.lineTo(centerX - triangleHeight, centerY - radius / 2);
        hexagonPath.lineTo(centerX, centerY - radius);
        hexagonPath.lineTo(centerX + triangleHeight, centerY - radius / 2);
        hexagonPath.lineTo(centerX + triangleHeight, centerY + radius / 2);
        hexagonPath.lineTo(centerX, centerY + radius);
        hexagonPath.lineTo(centerX - triangleHeight, centerY + radius / 2);
    }
}