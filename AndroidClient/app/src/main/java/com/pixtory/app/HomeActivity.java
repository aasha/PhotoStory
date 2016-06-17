package com.pixtory.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pixtory.app.adapters.ImageArrayAdapter;
import com.pixtory.app.adapters.OpinionViewerAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.model.SideMenuData;
import com.pixtory.app.pushnotification.QuickstartPreferences;
import com.pixtory.app.pushnotification.RegistrationIntentService;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetMainFeedResponse;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.transformations.ParallaxPagerTransformer;
import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.userprofile.UserProfileActivity2;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class HomeActivity extends AppCompatActivity implements MainFragment.OnMainFragmentInteractionListener{

    private static final String Get_Feed_Done = "Get_Feed_Done";
    private static final String Get_Feed_Failed = "Get_Feed_Failed";
    private final static String TAG = HomeActivity.class.getName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog mProgress = null;
    private Context mCtx = null;

    private ViewPager mPager = null;
    private int mCurrentFragmentPosition = 0;

    //Analytics
    public static final String SCREEN_NAME = "Main_Feed";
    private static final String MF_Bandwidth_Changed = "MF_Bandwidth_Changed";

    private static final String User_App_Entry = "User_App_Entry";
    private static final String User_App_Exit = "User_App_Exit";

    //Push notification
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private OpinionViewerAdapter mCursorPagerAdapter = null;

    LinearLayout mUserProfileFragmentLayout = null;
    int previousPage = 0;

    private MainFragment mainFragment = null;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //TODO to be removed later
//        checkForDeviceDensity();
        ButterKnife.bind(this);
        mTracker = App.getmInstance().getDefaultTracker();
        mCtx = this;
        if (Utils.isConnectedViaWifi(mCtx) == false) {
            showAlert();
        }

        setPersonDetails();
        setUpNavigationDrawer();
        mPager = (ViewPager) findViewById(R.id.pager);
        mUserProfileFragmentLayout = (LinearLayout) findViewById(R.id.user_profile_fragment_layout);
        mCursorPagerAdapter = new OpinionViewerAdapter(getSupportFragmentManager());

        //PagerParallaxTransformer pagerParallaxTransformer = new PagerParallaxTransformer().addViewToParallax(new PagerParallaxTransformer.ParallaxTransformParameters(R.id.image_main,1.5f,1.5f));
        ParallaxPagerTransformer parallaxPagerTransformer = new ParallaxPagerTransformer(HomeActivity.this,R.id.image_main,0.5f);
        mPager.setPageTransformer(true,parallaxPagerTransformer);
        mPager.setPageMargin(6);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (previousPage == 0)
                    previousPage = position;
                else
                    previousPage = mCurrentFragmentPosition;
                mCurrentFragmentPosition = position;
                mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();
               //Toast.makeText(HomeActivity.this,"Page swipe",Toast.LENGTH_SHORT).show();
               if(mainFragment!=null && mainFragment.isFullScreenShown())
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getContentData().get(previousPage).id)
                            .put("POSITION_ID",""+previousPage)
                            .build());
                else
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PixtorySwipe")
                            .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                            .put("PIXTORY_ID",""+App.getContentData().get(previousPage).id)
                            .build());

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d(TAG, "onPageScrollStateChanged = "+state);
            }
        });

        //Measuring network condition
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mListener = new ConnectionChangedListener();
        prepareFeed();

        //Register for push notifs
        registerForPushNotification();
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( User_App_Entry)
                .put("TIMESTAMP", System.currentTimeMillis() + "")
                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                .build());

        menuIcon.setImageResource(R.drawable.menu_icon);
    }


//    @OnClick(R.id.profileIcon)
//    public void onUserImageClick() {
//    }

    private void prepareFeed() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("pixtory coming up for you");
        mProgress.setCanceledOnTouchOutside(false);

        NetworkApiHelper.getInstance().getMainFeed(HomeActivity.this, new NetworkApiCallback<GetMainFeedResponse>() {
            @Override
            public void success(GetMainFeedResponse o, Response response) {
                mProgress.dismiss();
                if (o.contentList != null) {
                    App.setContentData(o.contentList);
                    Utils.deleteOldVideos(o.contentList);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Done)
                            .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                            .build());
                    mCursorPagerAdapter.setData(App.getContentData());
                    mPager.setAdapter(mCursorPagerAdapter);
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
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Failed)
                        .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                        .put("MESSAGE", error.errorMessage)
                        .build());
                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                mProgress.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Feed_Failed)
                        .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                        .put("MESSAGE", error.getMessage())
                        .build());
                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
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
                    Log.e("AASHA", "Registered for push notifs");
                } else {
                    Log.e("AASHA", "Failed");
                }
            }
        };
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }





//    /**
//     * Method to hide loading comments progressbar and show comments list
//     */
//    public void setCommentListVisibility(){
//
//        if(commentDataList!= null && commentDataList.size()>0){
//
//            Log.i(TAG,"Comment Count::"+commentDataList.size());
//            mTVCommentCount.setText(String.valueOf(commentDataList.size()));
//            mCommentText.setVisibility(View.VISIBLE);
//            mCommentsRecyclerViewAdapter = new CommentsListAdapter(HomeActivity.this);
//            mCommentsRecyclerViewAdapter.setData(commentDataList);
//            mCommentsRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);
//
//        }else{
//            Log.i(TAG,"No Comment Yet for this story");
//            mTVCommentCount.setText(" NO COMMENT YET ");
//            mCommentText.setVisibility(View.GONE);
//        }
//
//        mTVLoading.setVisibility(View.GONE);
//        mRLCommentList.setVisibility(View.VISIBLE);
//    }


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
        mConnectionClassManager.remove(mListener);
        mDeviceBandwidthSampler.stopSampling();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(SCREEN_NAME);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mConnectionClassManager.register(mListener);
        mDeviceBandwidthSampler.startSampling();
        ConnectionQuality cq = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
        /*AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_Bandwidth_Changed)
                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                .put(AppConstants.CONNECTION_QUALITY, cq.toString())
                .build());*/
        Log.e("AASHA", "Connection q " + cq.toString());
    }

    private void showAlert() {
        new AlertDialog.Builder(mCtx)
                .setTitle("Warning")
                .setMessage("You are not on a Wi-fi connection. pixtory recommends a wi-fi environment for a better experience")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

//    @Override
//    public void onAddCommentButtonClicked(String str) {
//        mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();
//        if(mainFragment !=null)
//            mainFragment.postComment(str);
//    }

    private class ConnectionChangedListener
            implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_Bandwidth_Changed)
                            .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                            .put(AppConstants.CONNECTION_QUALITY, mConnectionClass.toString())
                            .build());
                }
            });*/
        }
    }


    /**
     * Navigation Drawer Implementation
     */
/*

    private void setUpNavigationDrawer() {

        menuIcon = (ImageView) findViewById(R.id.profileIcon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();

                    if(!mainFragment.isCommentsVisible()) {
                        if (mDrawerLayout.isDrawerOpen(mNavigationView))
                            mDrawerLayout.closeDrawer(mNavigationView);
                        else
                            mDrawerLayout.openDrawer(mNavigationView);
                    }else{
                        mainFragment.onBackButtonClicked();
                    }

            }
        });
        PersonInfo myDetails = new PersonInfo();
        //setPersonDetails();
        myDetails = App.getPersonInfo();
        View header = mNavigationView.getHeaderView(0);

        final CircularImageView mPImg = (CircularImageView)header.findViewById(R.id.dr_profile_img) ;
        final TextView mPN = (TextView)header.findViewById(R.id.dr_profile_name);

        NetworkApiHelper.getInstance().getPersonDetails(Integer.parseInt(Utils.getUserId(HomeActivity.this)), Integer.parseInt(Utils.getUserId(HomeActivity.this)),new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {

                if (o.contentList != null) {
                    App.setPersonConentData(o.contentList);
                } else {
                    Toast.makeText(HomeActivity.this, "No Person content data!", Toast.LENGTH_SHORT).show();
                }

                if (o.personDetails!=null){
                    App.setPersonInfo(o.personDetails);
                    if(o.personDetails.imageUrl==""||o.personDetails.imageUrl==null)
                        Picasso.with(HomeActivity.this).load("http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803").fit().centerCrop().into(mPImg);
                    else
                        Picasso.with(HomeActivity.this).load(o.personDetails.imageUrl).fit().centerCrop().into(mPImg);
                    mPN.setText(o.personDetails.name);
                }else {
                    System.out.println("Person data null");
                    Toast.makeText(HomeActivity.this, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {
                // mProgress.dismiss();

                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });

        mPImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                startActivity(intent);

            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

        public boolean onNavigationItemSelected(MenuItem menuItem){
            int id = menuItem.getItemId();
            menuItem.setChecked(true);
            switch (id){

                case R.id.dr_feedback:showFeedBackDialog();
                    break;

                case R.id.dr_invite: sendInvite();
                    break;

                case R.id.dr_contributor:showContributeDialog();
                    break;

                case R.id.dr_wallpaper:mDrawerLayout.closeDrawer(mNavigationView);
                    showWallpaperAlert();
                    //setWallpaper();
                    break;
            }
            return true;

        }

        });

    }
*/

/*

    private void sendFeedback() {
        final Intent _Intent = new Intent(android.content.Intent.ACTION_SEND);
        _Intent.setType("text/email");
        _Intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
        _Intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        _Intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
        startActivity(Intent.createChooser(_Intent, getString(R.string.title_send_feedback)));

    }
*/


    private void setPersonDetails(){

        NetworkApiHelper.getInstance().getPersonDetails(Integer.parseInt(Utils.getUserId(HomeActivity.this)), Integer.parseInt(Utils.getUserId(HomeActivity.this)),new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {

                if (o.contentList != null) {
                    App.setPersonConentData(o.contentList);
                } else {
                    Toast.makeText(HomeActivity.this, "No Person content data!", Toast.LENGTH_SHORT).show();

                }

                if (o.personDetails!=null){
                    App.setPersonInfo(o.personDetails);
                }else {
                    System.out.println("Person data null");
                    Toast.makeText(HomeActivity.this, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {
                // mProgress.dismiss();

                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Toast.makeText(HomeActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
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
        LinearLayout feedbackCancel = (LinearLayout)dialog.findViewById(R.id.feedback_cancel);
        LinearLayout feedbackSend =(LinearLayout) dialog.findViewById(R.id.feedback_send);


        feedbackCancel.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        feedbackSend.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {

                NetworkApiHelper.getInstance().userFeedBack(Integer.parseInt(Utils.getUserId(HomeActivity.this)), feedbackText.getText().toString(),"","","",new NetworkApiCallback<BaseResponse>() {
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


//    TODO: test method to be removed later


    /**
     * Code to animate menu icon to back arrow
     * @param showBackArrow
     */
    @Override
    public void onAnimateMenuIcon(final boolean showBackArrow){

        ObjectAnimator anim = null;

        if(showBackArrow)
            anim  = (ObjectAnimator) AnimatorInflater.loadAnimator(HomeActivity.this, R.animator.flip_out);
        else
            anim = (ObjectAnimator) AnimatorInflater.loadAnimator(HomeActivity.this, R.animator.flip_in);

        anim.setTarget(menuIcon);
        anim.setDuration(200);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if(showBackArrow)
                    menuIcon.setImageResource(R.drawable.back_arrow);
                else
                    menuIcon.setImageResource(R.drawable.menu_icon);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
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
        dialog.setContentView(R.layout.contribute_dialog);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText cEmail = (EditText)dialog.findViewById(R.id.c_email);
        LinearLayout contributeCancel = (LinearLayout)dialog.findViewById(R.id.contribute_cancel);
        LinearLayout contriuteSubmit =(LinearLayout) dialog.findViewById(R.id.contribute_submit);


            contributeCancel.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        contriuteSubmit.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isEmailValid(cEmail.getText().toString()))
                    Toast.makeText(HomeActivity.this, "Please enter a valid email id.", Toast.LENGTH_SHORT).show();
                else{
                    NetworkApiHelper.getInstance().getContributorMail(Integer.parseInt(Utils.getUserId(HomeActivity.this)), cEmail.getText().toString(),  new NetworkApiCallback<BaseResponse>() {
                        @Override
                        public void success(BaseResponse o, Response response) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("BecomeContributor_Submit_Click")
                                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                                    .build());

                            Toast.makeText(HomeActivity.this, "Email Id Sent", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(BaseResponse error) {
                            // mProgress.dismiss();

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

    private void showWallpaperAlert(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wallpaper_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //final EditText feedbackText = (EditText)dialog.findViewById(R.id.feedback_text);
        LinearLayout wallpaperNo = (LinearLayout) dialog.findViewById(R.id.wallpaper_no_2);
        LinearLayout wallpaperYes =(LinearLayout) dialog.findViewById(R.id.wallpaper_yes_2);
        /*ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(wallpaperYes.getWidth(),wallpaperYes.getHeight());
        layoutParams.width = (int)(0.5*dm.widthPixels);
        wallpaperYes.setLayoutParams(layoutParams);*/
        wallpaperYes.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper();
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

    public void setWallpaper(){
        String imgUrl = App.getContentData().get(mPager.getCurrentItem()).pictureUrl;
        Picasso.with(HomeActivity.this).load(imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Toast.makeText(HomeActivity.this,"Wallpaper set",Toast.LENGTH_SHORT).show();
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }


    private void setUpNavigationDrawer() {

        menuIcon = (ImageView)findViewById(R.id.profileIcon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.cross_icon),""));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.profile_icon_2),"My Profile"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.contributor_icon_2),"Become a Contributor"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.invite_icon_2),"Invite Friends"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.feedback_icon_2),"Feedback"));
        items.add(new SideMenuData(getResources().getDrawable(R.drawable.wallpaper_icon_2),"Wallpaper"));

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        DisplayMetrics dm = new DisplayMetrics();
        if(Build.VERSION.SDK_INT>=17)
            this.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        else
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        final ImageArrayAdapter imageArrayAdapter = new ImageArrayAdapter(HomeActivity.this,0,items,dm.heightPixels);
        mDrawerList.setAdapter(imageArrayAdapter);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        break;

                    case 1:AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("HB_Profile_Click")
                    .put(AppConstants.USER_ID,Utils.getUserId(HomeActivity.this))
                    .build());
                        Intent intent = new Intent(HomeActivity.this, UserProfileActivity2.class);
                        intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                        intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                        startActivity(intent);
                       // mDrawerLayout.closeDrawer(mDrawerList);
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
                        showWallpaperAlert();
                        break;
                }
            }
        });
    }

    private void showLoginAlert(){
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
                if(packageName.contains("com.whatsapp") || packageName.contains("com.facebook.katana") || packageName.contains("mms") || packageName.contains("android.gm")){
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Hey there. Try this new app PIXTORY.\n\n www.pixtory.in");
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

}



