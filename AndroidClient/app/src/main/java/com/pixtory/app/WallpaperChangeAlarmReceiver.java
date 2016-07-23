package com.pixtory.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;
import service.WallpaperChangeService;

/**
 * Created by training3 on 25/06/2016 AD.
 */

public class WallpaperChangeAlarmReceiver extends BroadcastReceiver{

    private String TAG = WallpaperChangeAlarmReceiver.class.getName();
    private GcmNetworkManager mGcmNetworkManager;
    private Context context;


    @Override
    public void onReceive(final Context mContext, final Intent intent) {

        if(isHourAM()){
            Log.i("Alarm","WallpaperChangeAlarmReceiver onRecieve Called");
            context = mContext;

            SharedPreferences mSharedPrefs = mContext.getSharedPreferences(
                    AppConstants.APP_PREFS, 0);


            if(Utils.isNotEmpty(Utils.getUserId(mContext)) &&
                    !mSharedPrefs.getBoolean("is_today_wallpaper_set",false)) {

                int user_id = Integer.parseInt(Utils.getUserId(mContext));

                NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
                    @Override
                    public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                        Log.i(TAG, "Wallpaper URL is--" + getWallPaperResponse.wallPaper);

                        setWallPaper(mContext, getWallPaperResponse.wallPaper);
                        updateSharedPref(true);

                    }

                    @Override
                    public void failure(GetWallPaperResponse getWallPaperResponse) {
    //                    setJobSchedulerToSetWallpaper(mContext);
                        Log.i(TAG, "failure->" + getWallPaperResponse.errorMessage);
                        updateSharedPref(false);

                    }

                    @Override
                    public void networkFailure(RetrofitError error) {
    //                    setJobSchedulerToSetWallpaper(mContext);
                        Log.i(TAG, "networkFailure->" + error.toString());
                        updateSharedPref(false);
                    }
                });
             }
        }else{
            updateSharedPref(false);
        }

    }


    private void setJobSchedulerToSetWallpaper(Context ctx){
        if(mGcmNetworkManager==null)
            mGcmNetworkManager = GcmNetworkManager.getInstance(ctx);

        Task task = new OneoffTask.Builder()
                .setService(WallpaperChangeService.class)
                .setExecutionWindow(0, 1000*60*60*9) // 45 seconds to nine hours
                .setTag(AppConstants.TAG_TASK_ONEOFF_LOG)
                .setUpdateCurrent(false)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        mGcmNetworkManager.schedule(task);
    }


     public void setWallPaper(final Context mContext , String imgUrl) {
         Picasso.with(mContext).load(imgUrl).into(App.mDailyWallpaperTarget);

     }

    public void updateSharedPref(boolean bool){
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        mSharedPrefs.edit().putBoolean("is_today_wallpaper_set",bool).apply();
    }

    public boolean isHourAM(){
        int mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if(mHour<12){
            return true;
        }

        return false;

    }



}
