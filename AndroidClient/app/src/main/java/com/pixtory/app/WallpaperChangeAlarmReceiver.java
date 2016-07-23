package com.pixtory.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.recievers.ConnectivityChangeReceiver;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Sonali on 25/06/2016 AD.
 */

public class WallpaperChangeAlarmReceiver extends BroadcastReceiver{

    private String TAG = WallpaperChangeAlarmReceiver.class.getName();
    private Context appContext;

    @Override
    public void onReceive(final Context mContext, final Intent intent) {

        appContext = mContext.getApplicationContext();

        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_CALLED")
                .put(AppConstants.USER_ID, "" + !Utils.isNotEmpty(Utils.getUserId(mContext)))
                .build());

        long timeDiff = App.getTimeDiffFromLastWallPaperSet(appContext);

        if(timeDiff > 1000*60*60*12) {

            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_CHANGE_WP_CALLED_TIME_DIFF=")
                    .put(AppConstants.USER_ID, "" + !Utils.isNotEmpty(Utils.getUserId(mContext)))
                    .put("TIME_DIFF",""+timeDiff)
                    .build());

            if (!Utils.isNotEmpty(Utils.getUserId(mContext))) {
                //return if userId is null
                return;
            }

            final int user_id = Integer.parseInt(Utils.getUserId(mContext));

            if (Utils.isNetworkConnected(mContext)) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_NETWORK_CONNECTED")
                        .put(AppConstants.USER_ID, "" + user_id)
                        .build());

                Log.i("Alarm", "WallpaperChangeAlarmReceiver onRecieve Called");

                SharedPreferences mSharedPrefs = appContext.getSharedPreferences(
                        AppConstants.APP_PREFS, 0);


                NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
                    @Override
                    public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                        Log.i(TAG, "Wallpaper URL is--" + getWallPaperResponse.wallPaper);
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_RESP_SUCCESS")
                                .put(AppConstants.USER_ID, "" + user_id)
                                .put("WALLPAPER_URL", getWallPaperResponse.wallPaper)
                                .build());
                        setWallPaper(mContext, getWallPaperResponse.wallPaper);
                    }

                    @Override
                    public void failure(GetWallPaperResponse getWallPaperResponse) {
                        Log.i(TAG, "failure->" + getWallPaperResponse.errorMessage);
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_SET_WP_FAIL")
                                .put(AppConstants.USER_ID, "" + user_id)
                                .put("ERROR", getWallPaperResponse.errorMessage)
                                .build());

                    }

                    @Override
                    public void networkFailure(RetrofitError error) {
                        Log.i(TAG, "networkFailure->" + error.toString());
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_WP_SET_FAIL_NETWORK_NOTCONNECTED")
                                .put(AppConstants.USER_ID, "" + user_id)
                                .build());

                    }
                });
            } else {
                //if device is not connected to network register connection change listener
                appContext.registerReceiver(
                        new ConnectivityChangeReceiver(),
                        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_ALARM_ONRECIEVE_NETWORK_NOT_CONNECTED")
                        .put(AppConstants.USER_ID, "" + user_id)
                        .build());

            }
        }else{
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_ALARM_ONRECIEVE_CHANGE_WP_NOTCALLED")
                    .put(AppConstants.USER_ID, "" + !Utils.isNotEmpty(Utils.getUserId(mContext)))
                    .put("TIME_DIFF",""+timeDiff)
                    .build());
        }
    }


     public void setWallPaper(final Context mContext , String imgUrl) {
         Picasso.with(mContext).load(imgUrl).into(App.mDailyWallpaperTarget);
     }

}
