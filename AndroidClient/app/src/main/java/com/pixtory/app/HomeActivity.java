package com.pixtory.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.network.connectionclass.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pixtory.app.adapters.CommentsListAdapter;
import com.pixtory.app.adapters.OpinionViewerAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.CommentsDialogFragment;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.pushnotification.QuickstartPreferences;
import com.pixtory.app.pushnotification.RegistrationIntentService;
import com.pixtory.app.retrofit.GetMainFeedResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.typeface.Intro;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class HomeActivity extends AppCompatActivity implements MainFragment.OnMainFragmentInteractionListener {

    private static final String Get_Feed_Done = "Get_Feed_Done";
    private static final String Get_Feed_Failed = "Get_Feed_Failed";
    private final static String TAG = HomeActivity.class.getName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog mProgress = null;
    private Context mCtx = null;

    private ViewPager mPager = null;
    private int mCurrentFragmentPosition = 0;
    RelativeLayout mStoryLayout = null;
    RelativeLayout mCommentsLayout = null;
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
    @Bind(R.id.profileIcon)
    ImageView mImgUserProfile;

    Tracker mTracker;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;


    /** Naviagation Drawer Objects**/
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> mDrawerListAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mProfileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        mTracker = App.getmInstance().getDefaultTracker();
        mCtx = this;
        if (Utils.isConnectedViaWifi(mCtx) == false) {
            showAlert();
        }
        setUpNavigationDrawer();
        setUpRecomView();
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

        showShowcaseView();

        //Register for push notifs
        registerForPushNotification();
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( User_App_Entry)
                .put("TIMESTAMP", System.currentTimeMillis() + "")
                .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                .build());
    }


    @OnClick(R.id.profileIcon)
    public void onUserImageClick() {
    }

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


    private void setUpRecomView() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        mStoryLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_view_layout, null);
        mCommentsLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_comment_layout , null);
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

    @Override
    public void onDetachStoryView(Fragment ff, int position) {
        final ViewGroup parent = (ViewGroup) mStoryLayout.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
    }

    @Override
    public void onAttachStoryView(Fragment ff, int position) {
        //mPager.isScrollingEnabled = false;
        MainFragment f = (MainFragment) ff;
        final ViewGroup parent = (ViewGroup) mStoryLayout.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        bindStoryData(f);
        f.attachStoryView(mStoryLayout);
    }

    private void bindStoryData(final MainFragment mainFragment) {
        try {
            final ContentData data = App.getContentData().get(mCurrentFragmentPosition);
            ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
            TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
            TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
            TextView mTextDate = (TextView) mStoryLayout.findViewById(R.id.txtDate);
            TextView mTextStoryMainPara = (TextView) mStoryLayout.findViewById(R.id.txtMainPara);
            TextView mTextStoryDetails = (TextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
            Button mBtnShare = (Button) mStoryLayout.findViewById(R.id.btnShare);
            Button mBtnComment = (Button) mStoryLayout.findViewById(R.id.btnComment);
            if (data != null) {
                if (data.personDetails != null) {
                    if (data.personDetails.imageUrl == null || data.personDetails.imageUrl.trim().equals("")){
                        data.personDetails.imageUrl = "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";
                    }
                    Picasso.with(this).load(data.personDetails.imageUrl).fit().into(mProfileImage);
                    mTextName.setText(data.personDetails.name);
                    mTextDesc.setText(data.personDetails.desc);
                }
                mTextDate.setText(data.date);
                mTextStoryMainPara.setText(data.pictureFirstPara);
                mTextStoryDetails.setText(data.pictureDescription);
            }
            mBtnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    share(Uri.parse(data.pictureUrl));
                }
            });
            mBtnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ViewGroup parent = (ViewGroup) mCommentsLayout.getParent();
                    if (parent != null) {
                        parent.removeAllViews();
                    }
                    buildCommentsLayout(data.commentList);
                    mainFragment.attachStoryView(mCommentsLayout);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void buildCommentsLayout(List<CommentData> commentList){

        Button mPostComment = (Button)mCommentsLayout.findViewById(R.id.postComment);
        TextView mTVCommentCount = (TextView)mCommentsLayout.findViewById(R.id.tvCount);
        RecyclerView mCommentsList = (RecyclerView)mCommentsLayout.findViewById(R.id.commentsList);

        if(commentList!= null && commentList.size()>0 ){


            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(HomeActivity.this);
            RecyclerView.Adapter mCommentsListAdapter = new CommentsListAdapter(HomeActivity.this, commentList);
            mCommentsList.setLayoutManager(mLayoutManager);
            mCommentsList.setAdapter(mCommentsListAdapter);
            mCommentsList.setVisibility(View.VISIBLE);

            mTVCommentCount.setText(commentList.size() + " COMMENT");
        }else{
            mCommentsList.setVisibility(View.INVISIBLE);
            mCommentsList.setVisibility(View.INVISIBLE);
            mTVCommentCount.setText(" NO COMMENT YET ");
        }

        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                CommentsDialogFragment commentsDialogFragment = CommentsDialogFragment.newInstance("Some title");
                commentsDialogFragment.show(fm, "fragment_alert");
            }
        });
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
                    .put(AppConstants.OPINION_ID, App.getContentData().get(mCurrentFragmentPosition).id + "")
                    .put(AppConstants.USER_ID, Utils.getUserId(HomeActivity.this))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
   COACH MARK
    */
    private void showShowcaseView() {
        if (!Utils.hasCoachMarkShown(HomeActivity.this, AppConstants.HAS_TAP_COACH_MARK_SHOWN)) {
            final SimpleDraweeView coachMark = (SimpleDraweeView) findViewById(R.id.coach_mark);
            coachMark.setBackgroundResource(R.drawable.coachmarks);
            coachMark.setVisibility(View.VISIBLE);
            // coachMark.setAlpha(0.8f);
            Utils.setCoachMarkShown(HomeActivity.this, AppConstants.HAS_TAP_COACH_MARK_SHOWN);
            coachMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    coachMark.setVisibility(View.GONE);
                }
            });
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

    private void share(Uri uriToImage) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    /**
     *
     */
    private void setUpNavigationDrawer() {

        mProfileIcon = (ImageView)findViewById(R.id.profileIcon);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);

        String[] arr = getResources().getStringArray(R.array.menu_items);
        mDrawerListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        mDrawerList.setAdapter(mDrawerListAdapter);

        mProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mDrawerLayout.isDrawerOpen(mDrawerList))
                    mDrawerLayout.closeDrawer(mDrawerList);
                else
                    mDrawerLayout.openDrawer(mDrawerList);
            }
        });
    }

}



