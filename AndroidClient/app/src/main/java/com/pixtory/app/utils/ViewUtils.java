package com.pixtory.app.utils;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Toast;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Created by aasha.medhi on 07/09/15.
 */
public class ViewUtils {


    public static void expandTouchArea(final View delegate, final int extraPadding) {
        final View parent = (View) delegate.getParent();
        parent.post(new Runnable() {
            // Post in the parent's message queue to make sure the parent
            // lays out its children before we call getHitRect()
            public void run() {
                final Rect r = new Rect();
                delegate.getHitRect(r);
                r.top -= extraPadding;
                r.bottom += extraPadding;
                r.left -= extraPadding;
                r.right += extraPadding;
                parent.setTouchDelegate(new TouchDelegate(r, delegate));
            }
        });
    }

    public static void showToast(Context ctx, String msg){
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void addHexPostProcessor(Context ctx,SimpleDraweeView sdv, Uri uri, final float px, final float py, final float w){

        Postprocessor redMeshPostprocessor = new BasePostprocessor() {
            @Override
            public String getName() {
                return "redMeshPostprocessor";
            }

            @Override
            public void process(Bitmap bitmap) {
                Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                        bitmap.getHeight());

                Canvas canvas = new Canvas(output);
                Path hexagonPath = new Path();
                calculatePath(hexagonPath,px,py,w);

                Paint paint = new Paint();
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(Color.parseColor("#BAB399"));
                paint.setStrokeWidth(6);
                CornerPathEffect corEffect = new CornerPathEffect(8.0f);
                paint.setPathEffect(corEffect);           // set the size
                paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
                paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
                paint.setAntiAlias(true);
                canvas.drawPath(hexagonPath, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                hexagonPath.close();
                canvas.drawBitmap(bitmap, rect, rect, paint);



            }
        };

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setPostprocessor(redMeshPostprocessor)
                .build();

        PipelineDraweeController controller = (PipelineDraweeController)
                Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setOldController(sdv.getController())
                                // other setters as you need
                        .build();
        sdv.setController(controller);
    }


    private static void calculatePath(Path hexagonPath, float centerX, float centerY, float radius) {
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
