package com.pixtory.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.design.widget.NavigationView;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.ButterKnife;
import com.facebook.network.connectionclass.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pixtory.app.adapters.OpinionViewerAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.pushnotification.QuickstartPreferences;
import com.pixtory.app.pushnotification.RegistrationIntentService;

import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetMainFeedResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;

import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

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

//        setPersonDetails();
        setUpNavigationDrawer();
        mPager = (ViewPager) findViewById(R.id.pager);
        mUserProfileFragmentLayout = (LinearLayout) findViewById(R.id.user_profile_fragment_layout);
        mCursorPagerAdapter = new OpinionViewerAdapter(getSupportFragmentManager());

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
                    .put(AppConstants.OPINION_ID, App.getContentData().get(mCurrentFragmentPosition).id + "")
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
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_Bandwidth_Changed)
                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                .put(AppConstants.CONNECTION_QUALITY, cq.toString())
                .build());
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


    private class ConnectionChangedListener
            implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_Bandwidth_Changed)
                            .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                            .put(AppConstants.CONNECTION_QUALITY, mConnectionClass.toString())
                            .build());
                }
            });
        }
    }

    /**
     * Navigation Drawer Implementation
     */
    private void setUpNavigationDrawer() {

        menuIcon = (ImageView) findViewById(R.id.profileIcon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mainFragment = (MainFragment)mCursorPagerAdapter.getCurrentFragment();

                if(!mainFragment.isCommentsVisible()) {
                //Navigation drawer is shown
                    if (mDrawerLayout.isDrawerOpen(navigationView))
                        mDrawerLayout.closeDrawer(navigationView);
                    else
                        mDrawerLayout.openDrawer(navigationView);
                }else{
                //menu icon behaves as back arrow, on click of which user is navigated back to story content from
                //comments section
                    mainFragment.onBackButtonClicked();
                }

            }
        });

        View header = navigationView.getHeaderView(0);
        final CircularImageView userImage = (CircularImageView)header.findViewById(R.id.dr_profile_img) ;
        final TextView userName = (TextView)header.findViewById(R.id.dr_profile_name);

        if(Utils.isNotEmpty(Utils.getUserImage(this)))
            Picasso.with(HomeActivity.this).load(Utils.getUserImage(this)).fit().into(userImage);

        userName.setText(Utils.getUserName(this));

        userImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                startActivity(intent);

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

        public boolean onNavigationItemSelected(MenuItem menuItem){
            int id = menuItem.getItemId();
            menuItem.setChecked(true);
            switch (id){
                /*
                case R.id.dr_profile:Toast.makeText(HomeActivity.this,"My Profile is to be shown",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                    intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                    intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                    startActivity(intent);
                    break;
*/
                case R.id.dr_feedback:feedBackActivity();
                    break;

                case R.id.dr_invite: sendInvite();
                    break;

                case R.id.dr_contributor:
                    break;
            }
            return true;

        }

        });

    }

    private void sendFeedback() {
        final Intent _Intent = new Intent(android.content.Intent.ACTION_SEND);
        _Intent.setType("text/email");
        _Intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
        _Intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        _Intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
        startActivity(Intent.createChooser(_Intent, getString(R.string.title_send_feedback)));
    }

    private void sendInvite(){
        Toast.makeText(HomeActivity.this,"Invitation",Toast.LENGTH_SHORT).show();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey there. Try this new app PIXTORY.\n\n www.pixtory.in");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Send Invite"));
    }


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

    private void feedBackActivity(){
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

        final EditText feedbackText = (EditText)dialog.findViewById(R.id.feedback_text);
        TextView feedbackCancel = (TextView)dialog.findViewById(R.id.feedback_cancel);
        TextView feedbackSend =(TextView) dialog.findViewById(R.id.feedback_send);



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

}



