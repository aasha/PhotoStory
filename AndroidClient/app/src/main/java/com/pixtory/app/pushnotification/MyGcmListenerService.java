/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixtory.app.pushnotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Calendar;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String SCREEN_NAME = "Notification";
    private static final String App_Notification_Shown = "NF_Notification_Shown";
    private static final String Silent_Notification_Shown = "Silent_Notification_Shown";

    PendingIntent pendingIntent ;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String image = data.getString("image");
        String isWallpaperNotification = data.getString("set_wallpaper_notification");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("NOTIFICATION_CLICK",true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */

        SharedPreferences mSharedPrefs = getApplicationContext().getSharedPreferences(
                AppConstants.APP_PREFS, 0);


        Log.i(TAG,"Opted_for_daily_wallpaper::"+mSharedPrefs.getBoolean("Opt_for_daily_wallpaper",false));
        Log.i(TAG,"is daily wallpaper set for today"+ mSharedPrefs.getBoolean("is_today_wallpaper_set",false));


        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_LISTENER_CALLED")
                .put(AppConstants.USER_ID,Utils.getUserId(getApplicationContext()))
                .build());

        sendNotification(message, image , isWallpaperNotification);

        if(mSharedPrefs.getBoolean("Opt_for_daily_wallpaper",false)){
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_LISTENER_USER_OPTED_FOR_DAILY_WP")
                    .put(AppConstants.USER_ID,Utils.getUserId(getApplicationContext()))
                    .build());
//                && !mSharedPrefs.getBoolean("is_today_wallpaper_set",true)){
            long timeDiff = App.getTimeDiffFromLastWallPaperSet(getApplicationContext());
            if(timeDiff > 1000*60*60*9) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_CHANGE_WALLPAPER_METHOD_CALLED")
                        .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                        .put("TIME_DIFF",""+timeDiff)
                        .build());
                changeWallPaper();
            }else{
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_CHANGE_WALLPAPER_METHOD_NOT_CALLED")
                        .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                        .put("TIME_DIFF",""+timeDiff)
                        .build());
            }

        }

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String image , String isWallpaperNotif) {


        boolean showNotification = isWallpaperNotif.contentEquals("false");

        Log.i(TAG,"show Notification ="+showNotification);
        if(showNotification){
            Log.i(TAG,"notification shown");
            Bitmap bitmap =  null;
                try {
                    URL url = new URL(image);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }


                NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                notiStyle.setSummaryText(message);
                notiStyle.bigPicture(bitmap);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.pixtory_icon)
                        .setContentTitle("Pixtory")
                        .setContentText(message)
                        .setPriority(1)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setStyle(notiStyle)
                        .setAutoCancel(true);

                if (bitmap != null)
                    notificationBuilder.setLargeIcon(bitmap);
                else
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.splash_bg));

                notificationBuilder.setContentIntent(pendingIntent);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(App_Notification_Shown)
                        .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                        .build());
                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }else{
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Silent_Notification_Shown)
                    .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                    .build());
        }


    }

    private void changeWallPaper(){

        Log.i(TAG,"changeWallpaper method called");

        if(Utils.isNotEmpty(Utils.getUserId(getApplicationContext()))) {

            int user_id = Integer.parseInt(Utils.getUserId(getApplicationContext()));

            NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
                @Override
                public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                    Log.i(TAG, "wallpaper URL is--" + getWallPaperResponse.wallPaper);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_SETWALLPAPER_RESPONSE_SUCCESS")
                            .put("WALLPAPER_URL",getWallPaperResponse.wallPaper)
                            .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                            .build());
                    setWallPaper(getApplicationContext(), getWallPaperResponse.wallPaper);
                }

                @Override
                public void failure(GetWallPaperResponse getWallPaperResponse) {
                    Log.i(TAG,"failure->"+getWallPaperResponse.errorMessage);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_SETWALLPAPER_FAILED")
                            .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                            .put("ERROR",getWallPaperResponse.errorMessage)
                            .build());

                }

                @Override
                public void networkFailure(RetrofitError error) {
                    Log.i(TAG,"networkFailure->"+error.toString());
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("DEBUG_MYGCM_SETWALLPAPER_FAILED_NETWORK_NOTCONNECTED")
                            .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                            .build());
                }
            });
        }

    }

    public void setWallPaper(final Context mContext , String imgUrl) {
        Picasso.with(mContext).load(imgUrl).into(App.mDailyWallpaperTarget);
    }
}
