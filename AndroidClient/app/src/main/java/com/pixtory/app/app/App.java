package com.pixtory.app.app;

import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.amplitude.api.Amplitude;
import com.crittercism.app.Crittercism;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by aasha.medhi on 12/05/15.
 */
public class App extends Application implements AppConstants {

    private static App mInstance = null;

    public static App getmInstance() {
        return mInstance;
    }

    private static ArrayList<ContentData> mCData = null;
    private static ArrayList<ContentData> mCategoryContentData = null;

    private static ArrayList<ContentData> mLikedContentData = null;

    private LayoutInflater mInfater = null;

    private Tracker mTracker;

    private static PersonInfo mPersonInfo;

    private static ArrayList<ContentData> mPersonConentData;

    private static ArrayList<ContentData> mProfileContentData;

    private static int MAX_CAPACITY = 10;

    public static String getDailyWallpaperUrl() {
        return dailyWallpaperUrl;
    }

    public static void setDailyWallpaperUrl(String dailyWallpaperUrl) {
        App.dailyWallpaperUrl = dailyWallpaperUrl;
    }

    private static String dailyWallpaperUrl;

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

    public static Target mWallpaperTarget, mDailyWallpaperTarget;

    public  static boolean isLoginRequired;
    private static Map<String,String> mOriginalIndices = new HashMap<String, String>();
    private static Map<String,String> mOriginalCategoryIndices = new HashMap<String, String>();


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        isLoginRequired = true;

        Crittercism.initialize(getApplicationContext(), "67496ab9c7094339adf79c54d369ccc900555300");

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(getApplicationContext())
                .setDownsampleEnabled(true)
                .build();

        Fresco.initialize(getApplicationContext(), config);

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
        Amplitude.getInstance().initialize(this, "7c63201294234fbe66c73d36c4ae206c").enableForegroundTracking(this);
        Amplitude.getInstance().trackSessionEvents(true);

        /**Enabling disk caching for Picasso**/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(false);
        Picasso.setSingletonInstance(built);

        mWallpaperTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                    Toast.makeText(getApplicationContext(), "Hurray!! Pixtory updated your wallpaper", Toast.LENGTH_SHORT).show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_DeviceWallpaper_Set")
                        .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                        .build());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Oops we couldn't set your wallpaper", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(getApplicationContext(), "Bitmap Loadig Failed, Couldn't change your wallpaper", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        mDailyWallpaperTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                    Toast.makeText(getApplicationContext(), "Hurray!! Pixtory updated your wallpaper", Toast.LENGTH_SHORT).show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_Device_EveryDay_Wallpaper_Set")
                            .put(AppConstants.USER_ID, Utils.getUserId(getApplicationContext()))
                            .build());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Oops we couldn't set your wallpaper", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(getApplicationContext(), "Bitmap Loading Failed, Couldn't change your wallpaper", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };


    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // To enable debug logging use: adb shell set prop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-80782200-1");

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
        for(int i=0;i< mC.size();i++)
            mOriginalIndices.put(mC.get(i).id+"",i+"");
    }


    public static void setCategoryContentData(ArrayList<ContentData> mC){
        mCategoryContentData = mC;
        for(int i=0;i< mC.size();i++)
            mOriginalCategoryIndices.put(mC.get(i).id+"",i+"");
    }

    public static ArrayList<ContentData> getContentData() {
        return mCData;
    }

    public static ArrayList<ContentData> getCategoryContentData() {
        return mCategoryContentData;
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

    public static void addToPersonContentData(ContentData contentData){
        mPersonConentData.add(contentData);
    }

    public static void removeFromPersonContentData(ContentData contentData){
        mPersonConentData.remove(contentData);

    }

    public static ArrayList<ContentData> getPersonContentData(){ return mPersonConentData; }

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


    public static void shuffleContentData(int startPos){
        if(mCData!=null&&startPos<mCData.size()&&startPos>0){
            ArrayList<ContentData> sublist = new ArrayList<ContentData>(mCData.subList(0,startPos));
            Collections.reverse(sublist);
            mCData.subList(0,startPos).clear();
            mCData.addAll(sublist);
        }
    }

    public static int getOriginalIndex(int contentId){
        if(mOriginalIndices!=null) {
            if(mOriginalIndices.get(contentId + "") == null)
                return Integer.parseInt(mOriginalIndices.get(contentId + ""));
        }
        return 0;
    }
}
