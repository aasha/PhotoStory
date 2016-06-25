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
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmListenerService;
import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

import java.net.URL;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String SCREEN_NAME = "Notification";
    private static final String App_Notification_Shown = "App_Notification_Shown";

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
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        changeWallPaper();

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

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
        sendNotification(message, image);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String image) {

        Bitmap b = null;
        try {
            URL url = new URL(image);
            b = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.pixtory_icon)
                .setContentTitle("pixtory")
                .setContentText(message)
                .setAutoCancel(true)
                .setContent(remoteViews);
        if(b!=null)
            remoteViews.setImageViewBitmap(R.id.notification_image,b);
        remoteViews.setTextViewText(R.id.notification_message,message);
        //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.pixtory))
//        if (b != null)
//            notificationBuilder.setStyle(new NotificationCompat.().bigPicture(b));


        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(App_Notification_Shown)
                .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                .build());
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void changeWallPaper(){

//        if(Utils.isNotEmpty(imgUrl)) {
//            Utils.setWallpaper(this , getApplicationContext(), imgUrl);
//        }

    }
}
