package com.pixtory.app;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

/**
 * Created by training3 on 25/06/2016 AD.
 */
public class WallpaperChangeAlarmReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(final Context mContext, Intent intent) {
        Log.i("Alarm","WallpaperChangeAlarmReceiver onRecieve Called");

//        Picasso.with(mContext).load(mContentData.pictureUrl).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                WallpaperManager myWallpaperManager
//                        = WallpaperManager.getInstance(mContext.getApplicationContext());
//                try {
//                    myWallpaperManager.setBitmap(bitmap);
//                    Toast.makeText(mContext.getApplicationContext(),"Pixtory -- Wallpaper set",Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    Toast.makeText(mContext.getApplicationContext(),"Oops we couldn't set your wallpaper",Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//                Toast.makeText(mContext,"Bitmap Loadig Failed, Couldn't change your wallpaper",Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//            }
//        });

    }
}
