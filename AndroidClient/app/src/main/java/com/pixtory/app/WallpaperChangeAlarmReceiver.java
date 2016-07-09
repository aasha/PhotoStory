package com.pixtory.app;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by training3 on 25/06/2016 AD.
 */
public class WallpaperChangeAlarmReceiver extends BroadcastReceiver{

    String TAG = WallpaperChangeAlarmReceiver.class.getName();

    @Override
    public void onReceive(final Context mContext, Intent intent) {
        Log.i("Alarm","WallpaperChangeAlarmReceiver onRecieve Called");

        if(Utils.isNotEmpty(Utils.getUserId(mContext))){

        int user_id = Integer.parseInt(Utils.getUserId(mContext));

        NetworkApiHelper.getInstance().getWallPaper(user_id,  new NetworkApiCallback<GetWallPaperResponse>() {
            @Override
            public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                Log.i(TAG,"wallpaper URL is--"+getWallPaperResponse.wallPaper);
                setWallPaper(mContext , getWallPaperResponse.wallPaper);
            }

            @Override
            public void failure(GetWallPaperResponse getWallPaperResponse) {

            }

            @Override
            public void networkFailure(RetrofitError error) {
            }

        });
    }



        public void setWallPaper(final Context mContext , String imgUrl){
            Picasso.with(mContext).load(imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(mContext.getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                    Toast.makeText(mContext.getApplicationContext(),"Hurray!! Pixtory updated your wallpaper",Toast.LENGTH_SHORT).show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_DeviceWallpaper_Set")
                    .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                    .build());
                } catch (IOException e) {
                    Toast.makeText(mContext.getApplicationContext(),"Oops we couldn't set your wallpaper",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(mContext,"Bitmap Loading Failed, Couldn't change your wallpaper",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        }


    public void setCachedWallpaper(){

    }

}
