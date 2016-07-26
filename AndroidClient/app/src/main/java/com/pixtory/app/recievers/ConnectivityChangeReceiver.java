package com.pixtory.app.recievers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by training3 on 22/07/2016 AD.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static String TAG = ConnectivityChangeReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_CALLED")
                .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                .build());

        Context appContext = context.getApplicationContext();

        if(cm.getActiveNetworkInfo()!=null) {
            String s = "device is = "+cm.getActiveNetworkInfo().isConnected() +"and network is= "+cm.getActiveNetworkInfo().getTypeName();
            Log.i(TAG ,"isconnected= " +s);
            Toast.makeText(appContext, s, Toast.LENGTH_SHORT).show();

            long timeDiff = App.getTimeDiffFromLastWallPaperSet(context.getApplicationContext());
            if(timeDiff > 1000*60*60*9)
                setWallpaper(context);
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_NETWORK_YES")
                    .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                    .put("TIME_DIFF",""+timeDiff)
                    .build());

        }
        else {
            Log.i(TAG ,"isconnected=" + false);
            Toast.makeText(context.getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_NETWORK_NOT_CONNECTED")
                    .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                    .build());
        }

        //Disabling Connection Change Listener
        ComponentName receiver = new ComponentName(appContext, ConnectivityChangeReceiver.class);
        PackageManager pm = appContext.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

    public void setWallpaper(final Context context){

        final Context ctx = context.getApplicationContext();

        if (Utils.isNotEmpty(Utils.getUserId(ctx))) {

            int user_id = Integer.parseInt(Utils.getUserId(ctx));
            NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
                @Override
                public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                    Log.i(TAG, "Wallpaper URL is--" + getWallPaperResponse.wallPaper);

                    setWallPaper(ctx, getWallPaperResponse.wallPaper);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_SET_WP_SUCCESS")
                            .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                            .put("WALLPAPER_URL",getWallPaperResponse.wallPaper)
                            .build());

                }

                @Override
                public void failure(GetWallPaperResponse getWallPaperResponse) {
                    Log.i(TAG, "failure->" + getWallPaperResponse.errorMessage);
//                    updateSharedPref(false);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_SET_WP_FAIL")
                            .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                            .put("ERROR",getWallPaperResponse.errorMessage)
                            .build());

                }

                @Override
                public void networkFailure(RetrofitError error) {
                    Log.i(TAG, "networkFailure->" + error.toString());
//                    updateSharedPref(false);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_WP_CONNEC_CHANGE_LISTENER_SET_WP_NETWORK_FAIL")
                            .put(AppConstants.USER_ID,""+Utils.getUserId(context))
                            .build());
                }
            });
        }
    }

    public void setWallPaper(final Context mContext , String imgUrl) {
        Picasso.with(mContext).load(imgUrl).into(App.mDailyWallpaperTarget);
    }
}
