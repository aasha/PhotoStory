package com.pixtory.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.Utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class WallpaperNetworkRetryReceiver extends BroadcastReceiver {

    String TAG = WallpaperNetworkRetryReceiver.class.getName();


    public WallpaperNetworkRetryReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG,"WallpaperNetworkRetryReceiver onRecieve is called");
        if(Utils.isNotEmpty(Utils.getUserId(context))) {

            int user_id = Integer.parseInt(Utils.getUserId(context));

            PendingIntent alarmIntent;
            alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, WallpaperNetworkRetryReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(alarmIntent);

        NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
            @Override
            public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                Log.i(TAG, "wallpaper URL is--" + getWallPaperResponse.wallPaper);
                setWallPaper(context, getWallPaperResponse.wallPaper);
            }

            @Override
            public void failure(GetWallPaperResponse getWallPaperResponse) {
                Log.i(TAG,"GetWallPaperResponse failure->"+getWallPaperResponse.errorMessage);
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Log.i(TAG,"GetWallPaperResponse networkFailure->"+error.getMessage());
            }

            });
        }
    }




    public void setWallPaper(final Context mContext , String imgUrl){

        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(imgUrl))
                .setRequestPriority(Priority.HIGH)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();

        ImagePipeline frescoImagePipeline = Fresco.getImagePipeline();

        DataSource<CloseableReference<CloseableImage>> dataSource =
                frescoImagePipeline.fetchDecodedImage(imageRequest, mContext);

        try {
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (bitmap == null) {
                        Log.d(TAG, "Bitmap data source returned success, but bitmap null.");
                        return;
                    }
                    Log.i(TAG, "Bitmap is not null-" + bitmap.toString());
                    if(bitmap != null && mContext !=null){
                        WallpaperManager wallpaperManager
                                = WallpaperManager.getInstance(mContext);

                        try {
                            wallpaperManager.setBitmap(bitmap);
                            Log.i("WallPaper","Set wallpaper done");
                            Toast.makeText(mContext,"Hooray! Wallpaper set!",Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            Log.i("WallPaper","Set wallpaper IO exception");
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.i("Utils-","imageBitMap or mContext is null");
                    }

                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here
                }
            }, CallerThreadExecutor.getInstance());
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }


}
