package com.pixtory.app;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pixtory.app.adapters.ImageArrayAdapter;
import com.pixtory.app.adapters.OpinionViewerAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.CommentsDialogFragment;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.SideMenuData;
import com.pixtory.app.pushnotification.QuickstartPreferences;
import com.pixtory.app.pushnotification.RegistrationIntentService;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetMainFeedResponse;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.RegisterResponse;
import com.pixtory.app.transformations.ParallaxPagerTransformer;
import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.BlurBuilder;
import com.pixtory.app.utils.ImageDownloadManager;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class HomeActivity extends AppCompatActivity implements
        MainFragment.OnMainFragmentInteractionListener,CommentsDialogFragment.OnAddCommentButtonClickListener,ImageDownloadManager.ImageDownloadListener{

    private static final String Get_Feed_Done = "Get_Feed_Done";
    private static final String Get_Feed_Failed = "Get_Feed_Failed";
    private static String Is_First_Run = "FirstRun";
    private static String Swipe_Count = "SwipeCount";
    private static String Page_Index = "View_Pager_Index";
    private final static String TAG = HomeActivity.class.getName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public ProgressDialog mProgress = null;
    private Context mCtx = null;

    private static final int LONG_TAP = 0;
    private static final int SWIPE_UP = 1;
    private static final int SWIPE_LEFT = 2;
    private static String longTapText,swipeUpTextTop,swipeUpTextBottom,swipeLeftText;

    private CallbackManager callbackManager;

    private ViewPager mPager = null;
    private ViewPager mCategoryViewPager = null;

    private int mCurrentFragmentPosition = 0;
    private int mCurrentFragmentPositionInCategory =0;
    private int mPreviousFragmentPositionInCategory =0;
    private int mPreviousFragmentPosition = 0;

    //Analytics
    public static final String SCREEN_NAME = "Main_Feed";
    public static final String OPT_FOR_DAILY_WALLPAPER = "Opt_for_daily_wallpaper";
    private static final String MF_Bandwidth_Changed = "MF_Bandwidth_Changed";

    private static final String User_App_Entry = "User_App_Entry";
    private static final String User_App_Exit = "User_App_Exit";

    //Push notification
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private OpinionViewerAdapter mCursorPagerAdapter = null;
    private OpinionViewerAdapter mCategoryPagerAdapter = null;

    LinearLayout mUserProfileFragmentLayout = null;
    int previousPage = 0;

    private MainFragment mainFragment = null;
    private MainFragment categoryFragment;

    Tracker mTracker;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;

    /** Navigation Drawer Objects**/
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> mDrawerListAdapter;
    private ImageView menuIcon;
    private ActionBarDrawerToggle mDrawerToggle;

    //coachmark overlays
    private FrameLayout mSwipeUpCoachMark;
    private FrameLayout mWallpaperCoachMark;
    private TextView mWallpaperCoachMarkText;
    private LinearLayout mSwipeUpCoachMarkLongTap;
    private ImageView mWallpaperCoachMarkBlurBg;
    private TextView mWallpaperClose;
    private TextView mSwipeUpClose;
    private TextView mWallpaperYes;
    private TextView mWallpaperNo;
    private TextView mSwipeUpText1;
    private TextView mSwipeUpText2;
    private TextView mLongTapText;
    private ToggleButton mToggleButton;
    private TextView mWallpaperTopText;
    private TextView mSwipeLeftText;

    @Bind(R.id.whole_frame)
    FrameLayout mOuterContainer;

    @Bind(R.id.loading_text)
    TextView mLoadingText;

    private AlarmManager mAlarmManager = null;
    private PendingIntent mPendingIntent = null;
    private Intent mWallpaperReceiverIntent = null;

    public String userId;
    public int mFeedSize;
    public int mCategoryFeedSize;

    @Bind(R.id.other_frame)
    FrameLayout mOtherFrame;

    @Bind(R.id.category_title)
    TextView mCategoryTitle;

    @Bind(R.id.backButton)
    LinearLayout backButton;

    @Bind(R.id.dailywallpaper_yes)
    FrameLayout dailyWallpaperYes;

    @Bind(R.id.daily_wallpaper_no)
    FrameLayout dailyWallpaperNo;


    private int categoryDataSize;
    private ShareDialog shareDialog = null;
    private String categoryName;
    public boolean isCategoryViewOpen = false;

    private long storyStartTime;
    private long storyEndTime;
    private boolean isTimerStarted;
    private int pixtoryId;
    private long storyTimeInSecs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isFirstTimeOpen())
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( "App_FirstOpen")
                    .put("TIMESTAMP", System.currentTimeMillis() + "")
                    .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                    .build());

        if(getIntent().getBooleanExtra("NOTIFICATION_CLICK",false))
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("NF_Notification_Clicked")
                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                    .build());



        setContentView(R.layout.activity_home);

        userId = getIntent().getStringExtra("USER_ID");


        //TODO to be removed later
//        checkForDeviceDensity();
        ButterKnife.bind(this);
        mTracker = App.getmInstance().getDefaultTracker();
        mCtx = this;

        Log.i(TAG, "home activity oncreate");
        callbackManager = CallbackManager.Factory.create();

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading....");
        mProgress.setCanceledOnTouchOutside(false);

        mLoadingText.setVisibility(View.VISIBLE);
        mOuterContainer.setVisibility(View.GONE);

        prepareFeed();

        mPager = (ViewPager)findViewById(R.id.pager);
        mCategoryViewPager = (ViewPager)findViewById(R.id.category_pager);

        mUserProfileFragmentLayout = (LinearLayout) findViewById(R.id.user_profile_fragment_layout);

        //Measuring network condition
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();

        mListener = new ConnectionChangedListener();
        //while(App.getContentData()==null);

        mCategoryPagerAdapter = new OpinionViewerAdapter(getSupportFragmentManager());
        mCursorPagerAdapter = new OpinionViewerAdapter(getSupportFragmentManager());


        mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();
        //PagerParallaxTransformer pagerParallaxTransformer = new PagerParallaxTransformer().addViewToParallax(new PagerParallaxTransformer.ParallaxTransformParameters(R.id.image_main,1.5f,1.5f));
        ParallaxPagerTransformer parallaxPagerTransformer = new ParallaxPagerTransformer(HomeActivity.this,R.id.image_main,0.5f);
        mPager.setPageTransformer(true,parallaxPagerTransformer);
        mPager.setPageMargin(6);
        //swipeCount();

        mCategoryViewPager.setPageTransformer(true,parallaxPagerTransformer);
        mCategoryViewPager.setPageMargin(6);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                preFetchImages(position);

                mPreviousFragmentPosition = mCurrentFragmentPosition;
                if (previousPage == 0)
                    previousPage = position;
                else
                    previousPage = mCurrentFragmentPosition;
                mCurrentFragmentPosition = position;
                SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                int lastIndex = sharedPreferences.getInt(Page_Index,0);
                int currentIndex = App.getOriginalIndex(App.getContentData().get(mCurrentFragmentPosition).id);
                if(currentIndex>lastIndex)
                    sharedPreferences.edit().putInt(Page_Index,currentIndex).apply();
                mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();

                //Toast.makeText(HomeActivity.this,"Page swipe",Toast.LENGTH_SHORT).show();
                if(mainFragment!=null && mainFragment.isFullScreenShown()){
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getContentData().get(mPreviousFragmentPosition).id)
                            .put("POSITION_ID",""+mPreviousFragmentPosition)
                            .put("IS_CATEGORY","FALSE")
                            .build());
                    Log.i(TAG,"MF_Picture_PixtorySwipe_Amplitude :: "+App.getContentData().get(mPreviousFragmentPosition).name);}
                else{
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getContentData().get(mPreviousFragmentPosition).id)
                            .put("IS_CATEGORY","FALSE")
                            .build());
                    Log.i(TAG,"ST_Story_PixtorySwipe_Amplitude :: "+App.getContentData().get(mPreviousFragmentPosition).name);
                    stopStoryTimer(App.getContentData().get(mPreviousFragmentPosition).id);
                }
                swipeCount();
                Log.i(TAG,"Page swipe");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d(TAG, "onPageScrollStateChanged = "+state);
            }
        });


        mCategoryViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mPreviousFragmentPositionInCategory = mCurrentFragmentPositionInCategory;
                mCurrentFragmentPositionInCategory = position;
                position = position+1;
                mCategoryTitle.setText(categoryName+" ("+position+"/"+mCategoryFeedSize+")");
                categoryFragment = (MainFragment)mCategoryPagerAdapter.getCurrentFragment();
                if(categoryFragment!=null && categoryFragment.isFullScreenShown()){
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getCategoryContentData().get(mPreviousFragmentPositionInCategory).id)
                            .put("POSITION_ID",""+mPreviousFragmentPositionInCategory)
                            .put("IS_CATEGORY","TRUE")
                            .build());
                    Log.i(TAG,"MF_Picture_PixtorySwipe_Amplitude_Category :: "+App.getCategoryContentData().get(mPreviousFragmentPositionInCategory).name);}
                else if(categoryFragment!=null){
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getCategoryContentData().get(mPreviousFragmentPositionInCategory).id)
                            .put("IS_CATEGORY","TRUE")
                            .build());
                    stopStoryTimer(App.getCategoryContentData().get(mPreviousFragmentPositionInCategory).id);
                    Log.i(TAG,"ST_Story_PixtorySwipe_Amplitude_Category :: "+App.getCategoryContentData().get(mPreviousFragmentPositionInCategory).name);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d(TAG, "onPageScrollStateChanged = "+state);
            }
        });

        setUpNavigationDrawer();
        //Binding coachmark overlays
        mWallpaperCoachMarkBlurBg = (ImageView)findViewById(R.id.blur_layer);
        mSwipeUpCoachMark = (FrameLayout) findViewById(R.id.top_overlay_swipe_up);
        mWallpaperCoachMark = (FrameLayout) findViewById(R.id.top_overlay_wallpaper_setting);
        mWallpaperCoachMarkText = (TextView)findViewById(R.id.wallpaper_coachmark_text);
        mSwipeUpCoachMarkLongTap = (LinearLayout)findViewById(R.id.top_overlay_long_tap);
        mWallpaperClose = (TextView)findViewById(R.id.wallpaper_close);

        
        mSwipeUpText1 = (TextView)findViewById(R.id.swipe_up_text_1);
        mSwipeUpText2 = (TextView)findViewById(R.id.swipe_up_text_2);

        mLongTapText = (TextView)findViewById(R.id.long_tap_text);
       // mToggleButton = (ToggleButton)findViewById(R.id.toggle_button);

        mWallpaperTopText = (TextView)findViewById(R.id.wallpaper_top_text);

        String swipeText1 = "<b>Swipe up</b> for the <b>Story</b> behind the picture.";
        swipeUpTextTop = "<b>Swipe up</b> for the <b>Story</b> behind the picture.";
        String swipeText2 = "A <b>Story</b> is the photographer\'s experience, emotion, inspiration or expression behind the picture.";
        swipeUpTextBottom = "A <b>Story</b> is the photographer\'s experience, emotion, inspiration or expression behind the picture.";
        swipeLeftText = "<b>Swipe left</b> for the next <b>Pixtory</b>";
        mSwipeUpText1.setText(Html.fromHtml(swipeText1));
        mSwipeUpText2.setText(Html.fromHtml(swipeText2));

        longTapText = "<b>Long press</b> on an image to set it as your wallpaper";
        mLongTapText.setText(Html.fromHtml(longTapText));

        mWallpaperYes = (TextView)findViewById(R.id.wallpaper_yes);
        mWallpaperNo = (TextView)findViewById(R.id.wallpaper_no);

        mWallpaperClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWallpaperCoachMark.setVisibility(View.INVISIBLE);
                mWallpaperCoachMarkBlurBg.setVisibility(View.GONE);
            }
        });

//        //Register for push notifs
//        registerForPushNotification();

        menuIcon.setImageResource(R.drawable.hamburger_icon_2);
        FacebookSdk.sdkInitialize(getApplicationContext());

        shareDialog = new ShareDialog(this);


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(HomeActivity.this, "FB Login Success!", Toast.LENGTH_SHORT).show();
                        onFacebookLoginSuccess();

                    }

                    @Override
                    public void onCancel() {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Cancel)
                                .build());
                        closeDialog();
                        Toast.makeText(HomeActivity.this, "Sorry, unable to login to facebook.Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Fail)
                                .put("MESSAGE", exception.getMessage() + "")
                                .build());

                        closeDialog();
                        Toast.makeText(HomeActivity.this, "Sorry, unable to login to facebook.Please check your network connection or try again later.(" + exception.getMessage() + ")", Toast.LENGTH_LONG).show();

                    }
                });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCategoryViewOpen = false;
                mOuterContainer.setVisibility(View.VISIBLE);
                mOtherFrame.setVisibility(View.GONE);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CAT_Back_Click")
                .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                .build());
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    public void setAlarmManagerToSetWallPaper(){

        mAlarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        mWallpaperReceiverIntent = new Intent(HomeActivity.this, WallpaperChangeAlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, mWallpaperReceiverIntent, 0);
        mainFragment = getCurrentFragment();

        setWallpaperNow(AppConstants.SET_WALLPAPER);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 30);

        //Alarm set for 6 hours
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000*60*60*6, mPendingIntent);
        Log.i("Alarm","armManagerToSetWallPaper called");

    }

    public void cancelAlarm(){
        if(mAlarmManager != null && mPendingIntent != null){
            mAlarmManager.cancel(mPendingIntent);
            Log.i("Alarm", "Alarm Manager Cancelled");
        }
    }


    private void prepareFeed() {
        if(Utils.isNotEmpty(Utils.getUserId(HomeActivity.this))){

            Log.i(TAG, "prepareFeed--user id::"+Utils.getUserId(HomeActivity.this));

            NetworkApiHelper.getInstance().getMainFeed(HomeActivity.this, Utils.getUserId(HomeActivity.this) ,new NetworkApiCallback<GetMainFeedResponse>() {
                @Override
                public void success(GetMainFeedResponse o, Response response) {
                    mProgress.dismiss();
                    setPersonDetails();
                    Log.i(TAG, "prepare feed successful!!");

                    if (o.contentList != null) {
                        mFeedSize = o.contentList.size();
                        App.setContentData(o.contentList);
                        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                        int startPos = sharedPreferences.getInt(Page_Index,0);

                        App.shuffleContentData(startPos);
                        Utils.deleteOldVideos(o.contentList);

                        ImageDownloadManager imageDownloadManager =
                                new ImageDownloadManager(HomeActivity.this,o.contentList , 0 , 20);
                        AsyncTaskCompat.executeParallel( imageDownloadManager);

                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Done)
                                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                                .build());
                        mCursorPagerAdapter.setData(App.getContentData());

                        mPager.setAdapter(mCursorPagerAdapter);

                        mLoadingText.setVisibility(View.GONE);
                        mOuterContainer.setVisibility(View.VISIBLE);
                        swipeCount();

                    } else {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Failed)
                                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                                .put("MESSAGE", "No Data")
                                .build());
                        Toast.makeText(HomeActivity.this, "No data!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(GetMainFeedResponse error) {
                    mProgress.dismiss();
                    mLoadingText.setVisibility(View.VISIBLE);
                    mOuterContainer.setVisibility(View.GONE);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Failed)
                            .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                            .put("MESSAGE", error.errorMessage)
                            .build());
                    Log.i(TAG,"GetMainFeedResponse error"+error.errorMessage);
                    Toast.makeText(HomeActivity.this, "GetMainFeedResponse error--"+error.errorMessage, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void networkFailure(RetrofitError error) {
                    mProgress.dismiss();
                    mLoadingText.setVisibility(View.VISIBLE);
                    mOuterContainer.setVisibility(View.GONE);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Failed)
                            .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                            .put("MESSAGE", error.getMessage())
                            .build());
                    Toast.makeText(HomeActivity.this, "Please check your network connection, cannot proceed further", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            if(BuildConfig.BUILD_TYPE.equals(BuildConfig.DEBUG))
                Utils.showToastMessage(this,"Yes this was the issue , user id was null in getMainFeed",1);
            else
                Utils.showToastMessage(this,"Oops internal server occured, close and then open app again",1);

        }
    }

    private void registerForPushNotification() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.e(TAG, "Registered for push notifs");
                } else {
                    Log.e(TAG, "Registration for push notification failed");
                }
            }
        };
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Log.i(TAG,"PushNotifications");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
////            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            mTracker.setScreenName(SCREEN_NAME);
//            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
//            mConnectionClassManager.register(mListener);
//            mDeviceBandwidthSampler.startSampling();
//            ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
//        /*AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_Bandwidth_Changed)
//                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
//                .put(AppConstants.CONNECTION_QUALITY, cq.toString())
//                .build());*/
//            Log.e("AASHA", "Connection q " + cq.toString());


//            if(mProgress.isShowing()){
//                mProgress.dismiss();
//            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( User_App_Exit)
                    .put("TIMESTAMP", System.currentTimeMillis() + "")
                    .put("PIXTORY_ID", App.getContentData().get(mCurrentFragmentPosition).id + "")
                    .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mConnectionClassManager.remove(mListener);
//        mDeviceBandwidthSampler.stopSampling();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "home activity on Start");
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( User_App_Entry)
                .put("TIMESTAMP", System.currentTimeMillis() + "")
                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                .build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAddCommentButtonClicked(String str) {

        mainFragment = getCurrentFragment();

        if(mainFragment !=null)
            mainFragment.postComment(str);
    }

    int i=0;
    @Override
    public void onImageFetched() {
        Log.i(TAG,"onImageFetched");
        i++;
        Log.i(TAG,"onImageFetched-"+i);
        if(i==9){
            Log.i(TAG,"Image fetching sucessful");
        }
    }

    private class ConnectionChangedListener
            implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
        }
    }

    private void setPersonDetails(){

        Log.i(TAG,"get person details->"+Integer.parseInt(Utils.getUserId(HomeActivity.this)));

        NetworkApiHelper.getInstance().getPersonDetails(Integer.parseInt(Utils.getUserId(HomeActivity.this)), Integer.parseInt(Utils.getUserId(HomeActivity.this)),new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {
                //Register for push notifs
                registerForPushNotification();

                if (o.contentList != null) {
                    App.setPersonConentData(o.contentList);
                } else {
                    Toast.makeText(HomeActivity.this, "No Person content data!", Toast.LENGTH_SHORT).show();
                }

                if (o.userDetails!=null){
                    App.setPersonInfo(o.userDetails);
                }else {
                    System.out.println("Person data null");
                    Toast.makeText(HomeActivity.this, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {
                Toast.makeText(HomeActivity.this, "GetPersonDetailsResponse error--"+error.errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Toast.makeText(HomeActivity.this, "Please check your network connection--"+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFeedBackDialog(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.feedback_dialog);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText feedbackText = (EditText)dialog.findViewById(R.id.feedback_text);
        FrameLayout feedbackCancel = (FrameLayout)dialog.findViewById(R.id.feedback_cancel);
        FrameLayout feedbackSend =(FrameLayout) dialog.findViewById(R.id.feedback_send);

        final Spinner spinnerFeedback = (Spinner)dialog.findViewById(R.id.spinner_feedback);
        final Spinner spinnerCategory = (Spinner)dialog.findViewById(R.id.spinner_category);

        final String feedbacks[] = new String[]{"Feedback","I liked this","I disliked this","I'm reporting a problem"};
        String categories[] = new String[]{"Category","App On-boarding","Image Screen","Story Screen","Profile screen","Comments","Sharing"};

        final List<String> feedbackList = new ArrayList<>(Arrays.asList(feedbacks));
        final List<String> categoryList = new ArrayList<>(Arrays.asList(categories));

        ArrayAdapter<String> spinnerFeedbackAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,feedbackList){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position==0){
                    tv.setBackgroundColor(getResources().getColor(R.color.grey_3));
                    tv.setTextColor(getResources().getColor(R.color.white));
                }
                return view;
            }
        };
        ArrayAdapter<String> spinnerCategoryAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,categoryList){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position==0){
                    tv.setBackgroundColor(getResources().getColor(R.color.grey_3));
                    tv.setTextColor(getResources().getColor(R.color.white));
                }
                return view;
            }
        };

        spinnerFeedbackAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerCategoryAdapter.setDropDownViewResource(R.layout.spinner_item);

        spinnerFeedback.setPrompt("Feedback");

        spinnerFeedback.setAdapter(spinnerFeedbackAdapter);
        spinnerCategory.setAdapter(spinnerCategoryAdapter);

        feedbackCancel.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Feedback_Cancel_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                        .build());
                dialog.dismiss();
            }
        });

        feedbackSend.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {

                NetworkApiHelper.getInstance().userFeedBack(Integer.parseInt(Utils.getUserId(HomeActivity.this)), "FEEDBACK : "+feedbackText.getText().toString(),spinnerFeedback.getSelectedItem().toString(),spinnerCategory.getSelectedItem().toString(),"",new NetworkApiCallback<BaseResponse>() {
                    @Override
                    public void success(BaseResponse o, Response response) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("FeedBack_Submit_Click")
                                .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                                .build());

                        Toast.makeText(HomeActivity.this,"Feedback Sent",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(BaseResponse error) {
                        // mProgress.dismiss();

                        Toast.makeText(HomeActivity.this, "Error sending feedback", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void networkFailure(RetrofitError error) {
                        Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void showMenuIcon(boolean show){

        if(isCategoryViewOpen){
            if(show) {
                backButton.setVisibility(View.VISIBLE);
                mCategoryTitle.setVisibility(View.VISIBLE);
            }
            else {
                backButton.setVisibility(View.GONE);
                mCategoryTitle.setVisibility(View.GONE);
            }
        }
        else{
            if(show)
                menuIcon.setVisibility(View.VISIBLE);
            else
                menuIcon.setVisibility(View.GONE);
        }

    }


//TODO: test method to be removed later

    private void checkForDeviceDensity(){

        StringBuilder density = new StringBuilder("");
        float dpi = getResources().getDisplayMetrics().densityDpi;

        switch ((int)dpi) {
            case DisplayMetrics.DENSITY_LOW:
                density.append( "Low Density Display");
                Log.i(TAG, density.toString());
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                density.append( "Medium Density Display");
                Log.i(TAG, density.toString());
                break;
            case DisplayMetrics.DENSITY_HIGH:
                density.append( "High Density Display");
                Log.i(TAG, density.toString());
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                density.append( "X-high Density Display");
                Log.i(TAG, density.toString());
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                density.append( "XX-high Density Display");
                Log.i(TAG, density.toString());
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                density.append( "XXX-high Density Display");
                Log.i(TAG, density.toString());
                break;

        }

        if(density.toString().equals("")) {
            Log.i(TAG, "Screen density::"+dpi);
            Toast.makeText(HomeActivity.this,"Screen density::"+dpi,Toast.LENGTH_SHORT).show();
        }
        else {
            Log.i(TAG, "Screen density::" + density);
            Toast.makeText(HomeActivity.this,"Screen density::" + density,Toast.LENGTH_SHORT).show();
        }

    }

    /*
    method for 'become a contributor' item
    */
    private void showContributeDialog(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contributor_dialog);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText cEmail = (EditText)dialog.findViewById(R.id.c_email);
        final EditText cName = (EditText)dialog.findViewById(R.id.c_name);
        final EditText cNumber = (EditText)dialog.findViewById(R.id.c_phone);
        FrameLayout contributeCancel = (FrameLayout)dialog.findViewById(R.id.contributor_cancel);
        FrameLayout contributeSubmit =(FrameLayout) dialog.findViewById(R.id.contributor_submit);


        contributeCancel.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("BecomeContributor_Cancel_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                        .build());
                dialog.dismiss();
            }
        });

        contributeSubmit.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isEmailValid(cEmail.getText().toString()))
                    Toast.makeText(HomeActivity.this, "Please enter a valid email id.", Toast.LENGTH_SHORT).show();
                else{
                    NetworkApiHelper.getInstance().getContributorMail(Integer.parseInt(Utils.getUserId(HomeActivity.this)), cEmail.getText().toString(),cName.getText().toString(),cNumber.getText().toString(),  new NetworkApiCallback<BaseResponse>() {
                        @Override
                        public void success(BaseResponse o, Response response) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("BecomeContributor_Submit_Click")
                                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                                    .build());
                            Toast.makeText(HomeActivity.this, "Thanks! The team at Pixtory will reach out to you", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(BaseResponse error) {
                            Toast.makeText(HomeActivity.this, "Error sending Email id", Toast.LENGTH_SHORT).show();
                        }   

                        @Override
                        public void networkFailure(RetrofitError error) {
                            Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
                        }
                    });

                dialog.dismiss();
            }
            }
        });

        dialog.show();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showWallpaperDialog(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wallpaper_dialog);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //final EditText feedbackText = (EditText)dialog.findViewById(R.id.feedback_text);
        TextView wallpaperNo = (TextView) dialog.findViewById(R.id.wallpaper_no);
        TextView wallpaperYes =(TextView) dialog.findViewById(R.id.wallpaper_yes);
        /*ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(wallpaperYes.getWidth(),wallpaperYes.getHeight());
        layoutParams.width = (int)(0.5*dm.widthPixels);
        wallpaperYes.setLayoutParams(layoutParams);*/
        wallpaperYes.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        wallpaperNo.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setUpNavigationDrawer() {

        menuIcon = (ImageView)findViewById(R.id.profileIcon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,null,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                menuIcon.setVisibility(View.GONE);
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                menuIcon.setVisibility(View.VISIBLE);
                super.onDrawerClosed(drawerView);
                //Toast.makeText(HomeActivity.this,"Drawer closed",Toast.LENGTH_SHORT).show();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        final ArrayList<SideMenuData> items = new ArrayList<SideMenuData>();
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.cross_icon_3),""));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.profile_icon_3),"My Profile"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.contributor_icon_3),"Become a Contributor"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.invite_icon_3),"Invite Friends"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.feedback_icon_3),"Feedback"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.wallpaper_icon_3),"Daily wallpapers"));

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        DisplayMetrics dm = new DisplayMetrics();
//        if(Build.VERSION.SDK_INT>=17)
//            this.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
//        else
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        final ImageArrayAdapter imageArrayAdapter = new ImageArrayAdapter(HomeActivity.this,0,items,dm.heightPixels);
        mDrawerList.setAdapter(imageArrayAdapter);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainFragment == null)
                    mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();

                if(mainFragment.isFullScreenShown())
                {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Hamburger_Click")
                        .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                        .put("PIXTORY_ID",App.getContentData().get(mPager.getCurrentItem()).id+"")
                        .put("POSITION_ID",mPager.getCurrentItem()+"")
                        .build());
                }
                else
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Hamburger_Click")
                        .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                        .put("PIXTORY_ID",App.getContentData().get(mPager.getCurrentItem()).id+"")
                        .put("POSITION_ID",mPager.getCurrentItem()+"")
                        .build());


                if(!mainFragment.isCommentsVisible()){
                if(mDrawerLayout.isDrawerOpen(mDrawerList)){
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
                else{
                    //mDrawerList.setAdapter(imageArrayAdapter);
                    mDrawerList.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(HomeActivity.this,R.anim.rotate_in),0.2f));
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                }else{
                    mainFragment.onBackButtonClicked();
                }
            }
        });


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i){
                    case 0:if(Build.VERSION.SDK_INT>=16)
                        {
                            mDrawerList.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(HomeActivity.this,R.anim.left_out),0.2f));
                            mDrawerList.postOnAnimationDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Toast.makeText(HomeActivity.this,"Animation done",Toast.LENGTH_SHORT).show();
                                    mDrawerLayout.closeDrawer(mDrawerList);
                                }
                            },560);
                        }
                        else
                            mDrawerLayout.closeDrawer(mDrawerList);
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_Close_Click")
                                .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                                .build());
                        break;

                    case 1:

                        if(Utils.isNotEmpty(Utils.getFbID(HomeActivity.this))) {

                                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_Profile_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                            intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                            intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                            startActivity(intent);

                        }else{
                            showLoginAlert();
                        }
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;

                    case 2: AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_BecomeContributor_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                        mDrawerLayout.closeDrawer(mDrawerList);
                        showContributeDialog();
                        break;

                    case 3:AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_InviteFriends_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                        mDrawerLayout.closeDrawer(mDrawerList);
                        sendInvite();
                        break;

                    case 4:AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_Feedback_Click")
                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                    .build());
                        mDrawerLayout.closeDrawer(mDrawerList);
                        showFeedBackDialog();
                        break;

                    case 5:mDrawerLayout.closeDrawer(mDrawerList);
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_Wallpaper_Click")
                                .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                                .build());
                        prepareWallpaperCoachmark(true);
                        break;
                }
            }
        });
    }

    @Override
     public void showLoginAlert(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.login_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearLayout loginClick = (LinearLayout) dialog.findViewById(R.id.login_click);
        loginClick.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LoginManager.getInstance().logInWithReadPermissions(HomeActivity.this, AppConstants.mFBPermissions);
            }
        });

        dialog.show();
    }

    private void sendInvite(){
        List<Intent> targetInviteIntents=new ArrayList<Intent>();
        Intent inviteIntent=new Intent();
        inviteIntent.setAction(Intent.ACTION_SEND);
        inviteIntent.setType("text/plain");
        List<ResolveInfo> resInfos=getPackageManager().queryIntentActivities(inviteIntent, 0);
        if(!resInfos.isEmpty()){

            for(ResolveInfo resInfo : resInfos){
                String packageName=resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);
                if( packageName.contains("com.facebook.katana") || packageName.contains("mms") || packageName.contains("android.gm")){
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Hey, I've been using this new app called Pixtory, a platform for stunning fullscreen images and the stories behind them. I think you should check it out! Download it on the Play Store or go to \n\nwww.pixtory.in to know more.");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "App Invitation");
                    intent.setPackage(packageName);
                    targetInviteIntents.add(intent);
                }else if(packageName.contains("com.whatsapp")){
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Hey, I've been using this new app called *Pixtory*, a platform for stunning fullscreen images and the stories behind them. I think you should check it" +
                            " out! Download it on the Play Store \n\n"
                            +AppConstants.PLAY_STORE_LINK+AppConstants.SOCIAL_MEDIA_WHATSAPP_SHARE+
                            " to know more.");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "App Invitation");
                    intent.setPackage(packageName);
                    targetInviteIntents.add(intent);
                }
            }
            if(!targetInviteIntents.isEmpty()){

                Intent chooserIntent=Intent.createChooser(targetInviteIntents.remove(0), "Invite via");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetInviteIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            }else{
                Toast.makeText(this,"No Apps to share",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *
     */
    private void onFacebookLoginSuccess() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    final String fbId = user.optString("id");
                    final String name = user.optString("name");
                    final String email = user.optString("email");
                    String accessToken = AccessToken.getCurrentAccessToken().getToken();
                    final String imgUrl = "https://graph.facebook.com/" + fbId + "/picture?width=500&height=500";

                    mProgress.setTitle("Registering user...");
                    mProgress.show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Success)
                            .put("NAME", name)
                            .put("FBID", fbId)
                            .build());
                    NetworkApiHelper.getInstance().registerUser(name, email, imgUrl,fbId,new NetworkApiCallback<RegisterResponse>() {
                        @Override
                        public void success(RegisterResponse regResp, Response response) {
                            Log.i(TAG, "Registering user to pixtory sucess");
                            mProgress.dismiss();
                            Utils.putUserId(HomeActivity.this, regResp.userId);
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Success)
                                    .put(AppConstants.USER_ID, regResp.userId)
                                    .build());
                            Utils.putFbId(HomeActivity.this, fbId);
                            Utils.putEmail(HomeActivity.this, email);
                            Utils.putUserName(HomeActivity.this, name);
                            Utils.putUserImage(HomeActivity.this, imgUrl);
                            AmplitudeLog.sendUserInfo(regResp.userId);
                            setPersonDetails();
                            prepareFeed();
                        }

                        @Override
                        public void failure(RegisterResponse error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                                    .put("MESSAGE", error.errorMessage)
                                    .build());
                            mProgress.dismiss();
                        }

                        @Override
                        public void networkFailure(RetrofitError error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                                    .put("MESSAGE", error.getMessage())
                                    .build());
                            mProgress.dismiss();
                        }
                    });

                }
            }
        });
        request.executeAsync();
    }

    private void closeDialog(){
        mProgress.dismiss();
    }


    private int swipeCount(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int count = sharedPreferences.getInt(Swipe_Count,1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(count<13)
        {
         editor.putInt(Swipe_Count,count+1);
         editor.commit();

            final Handler handler = new Handler();

            switch (count){
                case 1:handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCoachmarksDialog(SWIPE_LEFT);
                    }
                },2000);
                    break;

                case 3:handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCoachmarksDialog(SWIPE_UP);
                    }
                },800);
                    break;

                case 10: showWallPaperCoachMark();
                    break;

            }
        }

        return count;
    }

    public void showCoachMarks(final View coachMarkView){
        mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
                        Animation.RELATIVE_TO_PARENT, 0f,
                        Animation.RELATIVE_TO_PARENT, 1f,
                        Animation.RELATIVE_TO_PARENT, 0f);
                animation.setDuration(1500);
                animation.setFillEnabled(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        coachMarkView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
//                              coachMarkView.setVisibility(View.GONE);
                                Animation fadeOut = new AlphaAnimation(1, 0);
                                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                                fadeOut.setStartOffset(300);
                                fadeOut.setDuration(200);

                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        coachMarkView.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });

                                coachMarkView.startAnimation(fadeOut);
                                mainFragment.setActioUpEnabled(false);

                                return false;
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                coachMarkView.setVisibility(View.VISIBLE);
                coachMarkView.setAnimation(animation);
            }
        },1500);

    }

    public void showWallPaperCoachMark(){

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    prepareWallpaperCoachmark(false);
                }
            },500);

    }


    private  void prepareWallpaperCoachmark(boolean isCalledFromMenuOptions){

        final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean isOpted = sharedPreferences.getBoolean(OPT_FOR_DAILY_WALLPAPER,false);
        boolean isShownBefore = sharedPreferences.getBoolean("Is_Shown_Before",false);
        if(!isShownBefore && !isCalledFromMenuOptions){
            mWallpaperTopText.setVisibility(View.VISIBLE);
            sharedPreferences.edit().putBoolean("Is_Shown_Before",true).apply();
        }
        else
            mWallpaperTopText.setVisibility(View.GONE);
        mWallpaperCoachMark.setVisibility(View.VISIBLE);
        mWallpaperCoachMarkBlurBg.setImageBitmap(BlurBuilder.blur(findViewById(R.id.whole_frame)));
        mWallpaperCoachMarkBlurBg.setVisibility(View.VISIBLE);

        if(isOpted){
            mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_changed_text));
          //  mToggleButton.setChecked(true);
            dailyWallpaperYes.setBackground(getResources().getDrawable(R.drawable.yes_on));
            dailyWallpaperNo.setBackground(getResources().getDrawable(R.drawable.no_off));
        }else{
            mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_text));
            //mToggleButton.setChecked(false);
            dailyWallpaperYes.setBackground(getResources().getDrawable(R.drawable.yes_off));
            dailyWallpaperNo.setBackground(getResources().getDrawable(R.drawable.no_on));
        }


        dailyWallpaperYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sharedPreferences.getBoolean(OPT_FOR_DAILY_WALLPAPER,false)){
                    dailyWallpaperYes.setBackground(getResources().getDrawable(R.drawable.yes_on));
                    dailyWallpaperNo.setBackground(getResources().getDrawable(R.drawable.no_off));
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_EverydayWallaperConfirm_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());

                    sharedPreferences.edit().putBoolean(OPT_FOR_DAILY_WALLPAPER,true).apply();
                    setAlarmManagerToSetWallPaper();
                    mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_changed_text));
                    Toast.makeText(HomeActivity.this,"A new wallpaper will be set on your phone every morning. We're sure you'll love them!",Toast.LENGTH_LONG).show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWallpaperCoachMark.setVisibility(View.INVISIBLE);
                            mWallpaperCoachMarkBlurBg.setVisibility(View.GONE);
                        }
                    },500);
                }

            }
        });

        dailyWallpaperNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getBoolean(OPT_FOR_DAILY_WALLPAPER,false)){
                    dailyWallpaperYes.setBackground(getResources().getDrawable(R.drawable.yes_off));
                    dailyWallpaperNo.setBackground(getResources().getDrawable(R.drawable.no_on));
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_EverydayWallaperCancel_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                    mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_text));
                    Toast.makeText(HomeActivity.this,"You can switch on the daily wallpapers anytime from the menu",Toast.LENGTH_LONG).show();

                    sharedPreferences.edit().putBoolean(OPT_FOR_DAILY_WALLPAPER,false).apply();
                    cancelAlarm();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWallpaperCoachMark.setVisibility(View.INVISIBLE);
                            mWallpaperCoachMarkBlurBg.setVisibility(View.GONE);
                        }
                    },500);
                }
            }
        });
/*
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

                Log.i(TAG,"Toggle button state changed");
                if(isChecked && !sharedPreferences.getBoolean(OPT_FOR_DAILY_WALLPAPER,false)){
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_HB_EverydayWallaperConfirm_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());

                    sharedPreferences.edit().putBoolean(OPT_FOR_DAILY_WALLPAPER,true).apply();
                    setAlarmManagerToSetWallPaper();
                    mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_changed_text));
                    Toast.makeText(HomeActivity.this,"A new wallpaper will be set on your phone every morning. We're sure you'll love them!",Toast.LENGTH_LONG).show();

                }
                else if(!isChecked && sharedPreferences.getBoolean(OPT_FOR_DAILY_WALLPAPER,false))
                {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_HB_EverydayWallaperCancel_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                    mWallpaperCoachMarkText.setText(getResources().getString(R.string.wallpaper_text));
                    Toast.makeText(HomeActivity.this,"You can switch on the daily wallpapers anytime from the menu",Toast.LENGTH_LONG).show();

                    sharedPreferences.edit().putBoolean(OPT_FOR_DAILY_WALLPAPER,false).apply();
                    cancelAlarm();
                }*/



    }

    private boolean isFirstTimeOpen(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean firstRun = sharedPreferences.getBoolean(Is_First_Run,true);
        if(firstRun){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Is_First_Run,false);
            editor.commit();
        }
        return firstRun;
    }

    private void preFetchImages(final int index){

        if((index+10)> mFeedSize || (index+10)== mFeedSize){
            Log.i(TAG,"Out of index");
            return;
        }else {

            ImagePipeline imagePipeline = Fresco.getImagePipeline();

            if(Utils.isNotEmpty(App.getContentData().get(index+10).pictureUrl)) {
                Log.i(TAG,"uri for--"+(index+10)+"is--"+App.getContentData().get(index+10).pictureUrl);
                DataSource<Boolean> inDiskCacheSource = imagePipeline.isInDiskCache(Uri.parse(App.getContentData().get(index + 10).pictureUrl));

                DataSubscriber<Boolean> subscriber = new BaseDataSubscriber<Boolean>() {
                    @Override
                    protected void onNewResultImpl(DataSource<Boolean> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }
                        boolean isInCache = dataSource.getResult();
                        if (isInCache) {
                            Log.i(TAG, "image with index==" + (index + 10) + "is cached");
                        } else {
                            Log.i(TAG, "image with index==" + (index + 10) + "is not cached");
                            ImageDownloadManager imageDownloadManager =
                                    new ImageDownloadManager(HomeActivity.this, App.getContentData(), index + 10, 10);

                            AsyncTaskCompat.executeParallel(imageDownloadManager);
//                        imageDownloadManager.execute();
                        }
                        // your code here
                    }

                    @Override
                    protected void onFailureImpl(DataSource<Boolean> dataSource) {

                    }
                };

                inDiskCacheSource.subscribe(subscriber, CallerThreadExecutor.getInstance());
            }else{
                Log.i(TAG,"uri for--"+(index+10)+"is empty--url--"+App.getContentData().get(index+10).pictureUrl);

            }
        }

    }

    @Override
    public void onBackPressed() {


        Log.i(TAG,"current fragment position is ---"+mCurrentFragmentPosition);

        mainFragment = getCurrentFragment();

        if (mainFragment!=null && mainFragment.isCommentsVisible()) {
            Log.i(TAG, "onBackPressed - User is navigated to story view");
            mainFragment.attachPixtoryContent(AppConstants.SHOW_PIC_STORY);

        } else if (mainFragment!=null && !mainFragment.isFullScreenShown()) {
            Log.i(TAG, "onBackPressed - User is navigated to full image view");
            mainFragment.setUpFullScreen();
        }
        else if(isCategoryViewOpen) {
            mOuterContainer.setVisibility(View.VISIBLE);
            mOtherFrame.setVisibility(View.GONE);
            isCategoryViewOpen = false;
        }
        else{
            super.onBackPressed();
        }
        stopStoryTimer(pixtoryId);

    }

    public void showCoachmarksDialog(int CoachmarkType){
        final Dialog dialog = new Dialog(HomeActivity.this,R.style.PauseDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View testView;

        switch (CoachmarkType){
            case LONG_TAP:dialog.setContentView(R.layout.long_tap_caochmark_dialog);
                ((TextView)dialog.findViewById(R.id.long_tap_text)).setText(Html.fromHtml(longTapText));
                break;

            case SWIPE_UP:dialog.setContentView(R.layout.swipe_up_caochmark_dialog);
                ((TextView)dialog.findViewById(R.id.swipe_up_text_top)).setText(Html.fromHtml(swipeUpTextTop));
                ((TextView)dialog.findViewById(R.id.swipe_up_text_bottom)).setText(Html.fromHtml(swipeUpTextBottom));
                break;

            case SWIPE_LEFT:dialog.setContentView(R.layout.swipe_left_coachmark_dialog);
                ((TextView)dialog.findViewById(R.id.swipe_left_text_top)).setText(Html.fromHtml(swipeLeftText));
                break;
        }

        testView = dialog.findViewById(R.id.swipe_up_coachmark_close);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void showCategoryStories(int id,String name,int pixtoryId){
        categoryName = name;
        isCategoryViewOpen = true;
        mCurrentFragmentPositionInCategory=0;
        if(Utils.isNotEmpty(Utils.getUserId(this))) {

            mProgress.setMessage("Fetching Pixtories");
            mProgress.show();

            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Category_Click")
                    .put("PIXTORY_ID",pixtoryId+"")
                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                    .put("CATEGORY_ID",id+"")
                    .put("CATEGORY_NAME",name)
                    .build());


            Log.d(TAG,"showCategoryStories->id="+id+"::name="+name);
            NetworkApiHelper.getInstance().getContentByCategory(Integer.parseInt(Utils.getUserId(this)), id, new NetworkApiCallback<GetMainFeedResponse>() {

                @Override
                public void success(GetMainFeedResponse feedResponse, Response response) {

                    mCategoryFeedSize = feedResponse.contentList.size();

                    for(ContentData data : feedResponse.contentList){
                        Log.i(TAG,data.pictureSummary);
                    }
//                    App.setCategoryContentData(getMainFeedResponse.contentList);
                    mCategoryPagerAdapter.clearData();
                    App.setCategoryContentData(feedResponse.contentList);

                    mCategoryPagerAdapter.setData(App.getCategoryContentData());

                    Log.i(TAG,"categoryData->"+feedResponse.contentList.toString());
                    mCategoryViewPager.setAdapter(mCategoryPagerAdapter);

                    mProgress.dismiss();

                    mCategoryViewPager.setCurrentItem(0);
                    mCategoryTitle.setText(categoryName + " (" + 1 + "/" + mCategoryFeedSize + ")");
                    mOuterContainer.setVisibility(View.GONE);
                    mOtherFrame.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(GetMainFeedResponse getMainFeedResponse) {
                    mProgress.dismiss();
                   Utils.showToastMessage(HomeActivity.this,"Internal error occurred::"+getMainFeedResponse.errorMessage,0);
                }

                @Override
                public void networkFailure(RetrofitError error) {
                    mProgress.dismiss();
                    Utils.showToastMessage(HomeActivity.this,"Internal error occurred::"+error.getMessage(),0);
                }
            });

        }else{
            Log.d(TAG,"showCategoryStories method-> user id is null");
        }
    }

    @Override
    public boolean isCategoryViewOpen(){
        return isCategoryViewOpen;
    }

    public MainFragment getCurrentFragment(){

        MainFragment mainFragment;
        if(isCategoryViewOpen)
            mainFragment = (MainFragment)mCategoryPagerAdapter.getFragmentAtIndex(mCurrentFragmentPositionInCategory);
        else
            mainFragment = (MainFragment)mCursorPagerAdapter.getFragmentAtIndex(mCurrentFragmentPosition);

        return mainFragment;
    }

    public void showShareDialog(ContentData contentData){

        String description = contentData.pictureDescription;
        description = description.replace("<b>","").replace("</b>","").replace("<i>","").replace("</i>","").replace("<p>","\n").replace("</p>","").replace("<br>","").replace("</br>","");

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("http://pixtory.in/share/mail.html?id="+contentData.id))
                .setContentTitle(contentData.pictureSummary)
                .setContentDescription(description)
                .setImageUrl(Uri.parse(contentData.pictureUrl))
                .build();

        shareDialog.show(content);


    }

    public void setWallPaper(String imgUrl){
        Picasso.with(HomeActivity.this).load(imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                    Toast.makeText(HomeActivity.this,"Hurray!! Pixtory updated your wallpaper",Toast.LENGTH_SHORT).show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_DeviceWallpaper_Set")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .build());
                } catch (IOException e) {
                    Toast.makeText(HomeActivity.this,"Oops we couldn't set your wallpaper",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(HomeActivity.this,"Bitmap Loadig Failed, Couldn't change your wallpaper",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public void setWallpaperNow(final int wallpaper_action){

        NetworkApiHelper.getInstance().getWallPaper(Integer.parseInt(Utils.getUserId(this)),  new NetworkApiCallback<GetWallPaperResponse>() {
            @Override
            public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                Log.i(TAG,"wallpaper URL is--"+getWallPaperResponse.wallPaper);
                if(wallpaper_action == AppConstants.SET_WALLPAPER)
                    setWallPaper(HomeActivity.this , getWallPaperResponse.wallPaper);

            }

            @Override
            public void failure(GetWallPaperResponse getWallPaperResponse) {

            }

            @Override
            public void networkFailure(RetrofitError error) {

            }

        });
    }

    public void setWallPaper(final Context mContext , String imgUrl) {
        Picasso.with(mContext).load(imgUrl).into( App.mWallpaperTarget);
    }


    @Override
    public void startStoryTimer(int startPixtoryId) {
        isTimerStarted = true;
        storyStartTime = System.currentTimeMillis();
        pixtoryId = startPixtoryId;
        Log.i(TAG,"Story timer started for Pixtory id - "+pixtoryId);

    }

    @Override
    public void stopStoryTimer(int endPixtoryId) {
        if(isTimerStarted && endPixtoryId == pixtoryId){
            storyEndTime = System.currentTimeMillis();
            isTimerStarted=false;
            storyTimeInSecs = (storyEndTime - storyStartTime)/1000;
            Log.i(TAG,"Story timer stopped for Pixtory id - "+pixtoryId + "Read time - "+storyTimeInSecs);
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_Read_Time")
                .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                .put("PIXTORY_ID",pixtoryId+"")
                .put("READ_TIME",storyTimeInSecs+"")
                .build());
        }

    }



    @Override
    protected void onStop() {
        super.onStop();
        stopStoryTimer(pixtoryId);
    }
}





