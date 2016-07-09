package com.pixtory.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by training3 on 09/07/2016 AD.
 */
public class InstallReferrerReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String referrerString = extras.getString("referrer");

        Log.i("InstallReferrerReceiver", "Referrer is: " + referrerString);
        Toast.makeText(context,"Referrer is->"+referrerString,Toast.LENGTH_SHORT);


    }
}
