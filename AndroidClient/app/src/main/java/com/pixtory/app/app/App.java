package com.pixtory.app.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.LayoutInflater;
import com.amplitude.api.Amplitude;
import com.crittercism.app.Crittercism;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;

import java.util.ArrayList;

/**
 * Created by aasha.medhi on 12/05/15.
 */
public class App extends Application implements AppConstants {

    private static App mInstance = null;

    public static App getmInstance() {
        return mInstance;
    }

    private static ArrayList<ContentData> mCData = null;

    private static ArrayList<ContentData> mLikedContentData = null;

    private LayoutInflater mInfater = null;

    private Tracker mTracker;

    private static PersonInfo mPersonInfo;

    private static ArrayList<ContentData> mPersonConentData;

    private static ArrayList<ContentData> mProfileContentData;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        Crittercism.initialize(getApplicationContext(), "67496ab9c7094339adf79c54d369ccc900555300");
        Fresco.initialize(getApplicationContext());

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());


        mInfater = LayoutInflater.from(getApplicationContext());
        //youTubePlayerView = (YouTubePlayerView) mInfater.inflate(R.layout.yt_view, null);



//        GoogleAnalytics.getInstance(this).getLogger()
//                .setLogLevel(Logger.LogLevel.VERBOSE);
        Log.d("Amplitude", "Amplitude init");
        Amplitude.getInstance().initialize(this, "7c657990d3385956001836ca63567102").enableForegroundTracking(this);
        Amplitude.getInstance().trackSessionEvents(true);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-73056382-1");
        }
        return mTracker;
    }

    public static void setLikedContentData(ArrayList<ContentData> mC) {
        mLikedContentData = mC;

    }

    public static ArrayList<ContentData> getLikedContentData() {
        return mLikedContentData;
    }

    public static void setContentData(ArrayList<ContentData> mC) {
        mCData = mC;

    }

    public static ArrayList<ContentData> getContentData() {
        return mCData;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void setPersonInfo(PersonInfo personInfo){
        mPersonInfo = personInfo;
    }

    public static PersonInfo getPersonInfo(){ return mPersonInfo; }

    public static void setPersonConentData(ArrayList<ContentData> conentData){
        mPersonConentData = conentData;
    }

    public static ArrayList<ContentData> getPersonConentData(){ return mPersonConentData; }

    public static void setProfileContentData(ArrayList<ContentData> contentData){
        mProfileContentData = contentData;
    }

    public static ArrayList<ContentData> getProfileContentData(){return mProfileContentData;}
}
