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
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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

    private static int MAX_CAPACITY = 10;

    private static Map<String,PersonInfo> mProfileCache = new LinkedHashMap<String, PersonInfo>(MAX_CAPACITY+1,0.75F,true){
        @Override
        protected boolean removeEldestEntry(Entry<String, PersonInfo> eldest) {
            return super.removeEldestEntry(eldest);
        }
    };

    private static Map<String,ArrayList<ContentData> > mProfileContentCache = new LinkedHashMap<String, ArrayList<ContentData>>(MAX_CAPACITY+1,0.75f,true){
        @Override
        protected boolean removeEldestEntry(Entry<String, ArrayList<ContentData>> eldest) {
            return super.removeEldestEntry(eldest);
        }
    };

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

        /**Enabling disk caching for Picasso**/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(false);
        Picasso.setSingletonInstance(built);
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

    public static void addToProfileCache(PersonInfo personInfo){
        //if(mProfileCache.isEmpty() || !mProfileCache.containsKey(personInfo.id+""))
            mProfileCache.put(personInfo.id+"", personInfo);
    }

    public static PersonInfo getProfileInfoFromCache(int userID){
        if(!mProfileCache.isEmpty() && mProfileCache.containsKey(userID+""))
            return mProfileCache.get(userID+"");
        return null;
    }

    public static void addToProfileContentCache(int userID,ArrayList<ContentData> contentDataArrayList){
        if(mProfileContentCache.isEmpty() || !mProfileContentCache.containsKey(userID+""))
            mProfileContentCache.put(userID+"",contentDataArrayList);
    }

    public static ArrayList<ContentData> getProfileContentFromCache(int userID){
        if(!mProfileContentCache.isEmpty() && mProfileContentCache.containsKey(userID+""))
            return mProfileContentCache.get(userID+"");
        return null;
    }

}
