package com.pixtory.app.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.analytics.CampaignTrackingReceiver;

/**
 * Created by training3 on 14/07/2016 AD.
 */
public class InstallReferrerReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        handleIntent(context, intent);
        Log.d("YES", "IT WORKS!!");

        Bundle extras = intent.getExtras();
        String referrerString = extras.getString("referrer");
        Log.d("onReceive->YES", "IT WORKS!! referrer="+referrerString);
        new CampaignTrackingReceiver().onReceive(context, intent);
    }

    // Handle the intent data
    public void handleIntent(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        Log.d("handleIntent->YEES", "IT WORKS!!!"+referrer);
    }
}
