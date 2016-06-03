package com.pixtory.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.network.connectionclass.*;
import com.google.android.exoplayer.util.Util;
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
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.pushnotification.QuickstartPreferences;
import com.pixtory.app.pushnotification.RegistrationIntentService;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.GetCommentDetailsResponse;
import com.pixtory.app.retrofit.GetMainFeedResponse;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.transformations.BlurTransformation;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.typeface.Intro;
import com.pixtory.app.userprofile.CircularImageBehaviour;
import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.OnClick;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class HomeActivity extends AppCompatActivity implements
        MainFragment.OnMainFragmentInteractionListener,CommentsDialogFragment.OnAddCommentButtonClickListener {

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
        setPersonDetails();
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
            LinearLayout mBtnShare = (LinearLayout) mStoryLayout.findViewById(R.id.btnShare);
            LinearLayout mBtnComment = (LinearLayout) mStoryLayout.findViewById(R.id.btnComment);

            Dekar.applyFont(HomeActivity.this,mTextName,"fonts/Roboto-Regular.ttf");
            Dekar.applyFont(HomeActivity.this,mTextDesc,"fonts/Roboto-Regular.ttf");
            Dekar.applyFont(HomeActivity.this,mTextDate,"fonts/Roboto-Regular.ttf");
            Dekar.applyFont(HomeActivity.this,mTextStoryDetails,"fonts/Roboto-Regular.ttf");

            final int content_id = App.getContentData().get(mCurrentFragmentPosition).id;

            if (data != null) {
                if (data.personDetails != null) {
                    if (data.personDetails.imageUrl == null || data.personDetails.imageUrl.trim().equals("")){
                        data.personDetails.imageUrl = "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";
                    }
                    Picasso.with(this).load(data.personDetails.imageUrl).fit().into(mProfileImage);
                    mTextName.setText(data.personDetails.name);
                    mTextDesc.setText(data.personDetails.desc);
                    mProfileImage.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(HomeActivity.this,data.personDetails.userId+"",Toast.LENGTH_SHORT).show();
                            /*
                            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                            intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                            intent.putExtra("PERSON_ID",data.personDetails.userId);
                            startActivity(intent);*/

                        }
                    });
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
                    buildCommentsLayout(content_id , data);
                    mainFragment.attachStoryView(mCommentsLayout);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<CommentData> commentDataList;
    private RecyclerView mCommentsRecyclerView;
    private CommentsListAdapter mCommentsRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mTVCommentCount , mCommentText;
    private RelativeLayout mRLCommentList = null;
    private TextView mTVLoading;


    private void buildCommentsLayout(int content_id ,ContentData data){

        Button mPostComment = (Button)mCommentsLayout.findViewById(R.id.postComment);
        ImageView mAvatarImgView = (ImageView)mCommentsLayout.findViewById(R.id.imgProfile);
        TextView mName = (TextView)mCommentsLayout.findViewById(R.id.txtName);
        TextView mDesc = (TextView)mCommentsLayout.findViewById(R.id.txtDesc);

        Picasso.with(this).load(data.personDetails.imageUrl).fit().into(mAvatarImgView);
        mName.setText(data.personDetails.name);
        mDesc.setText(data.personDetails.desc);

        mRLCommentList = (RelativeLayout)mCommentsLayout.findViewById(R.id.comments_layout);
        mTVLoading = (TextView)mCommentsLayout.findViewById(R.id.loading_comments);
        mTVCommentCount = (TextView)mCommentsLayout.findViewById(R.id.tvCount);
        mCommentText = (TextView)mCommentsLayout.findViewById(R.id.comment_text);

        mCommentsRecyclerView = (RecyclerView)mCommentsLayout.findViewById(R.id.commentsList);
        mLayoutManager = new LinearLayoutManager(HomeActivity.this);
        mCommentsRecyclerView.setLayoutManager(mLayoutManager);
        mCommentsRecyclerView.setHasFixedSize(true);

        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                CommentsDialogFragment commentsDialogFragment = CommentsDialogFragment.newInstance("Some title");
                commentsDialogFragment.show(fm, "fragment_alert");
            }
        });

        NetworkApiHelper.getInstance().getCommentDetailList(Utils.getUserId(HomeActivity.this), content_id, new NetworkApiCallback<GetCommentDetailsResponse>() {
            @Override
            public void success(GetCommentDetailsResponse getCommentDetailsResponse, Response response) {

                Log.i(TAG , "GetCommentDetails Request Success");

                commentDataList = getCommentDetailsResponse.getCommentList();
                setCommentListVisibility();
            }

            @Override
            public void failure(GetCommentDetailsResponse getCommentDetailsResponse) {
                Log.i(TAG , "GetCommentDetails Request Failure::"+getCommentDetailsResponse.toString());

                commentDataList = null;
                setCommentListVisibility();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Log.i(TAG , "GetCommentDetails Request Network Failure Error::"+error.getMessage());

                commentDataList = null;
                setCommentListVisibility();
            }
        });

    }

    /**
     * Method to hide loading comments progressbar and show comments list
     */
    public void setCommentListVisibility(){

        if(commentDataList!= null && commentDataList.size()>0){

            Log.i(TAG,"Comment Count::"+commentDataList.size());
            mTVCommentCount.setText(String.valueOf(commentDataList.size()));
            mCommentText.setVisibility(View.VISIBLE);
            mCommentsRecyclerViewAdapter = new CommentsListAdapter(HomeActivity.this);
            mCommentsRecyclerViewAdapter.setData(commentDataList);
            mCommentsRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        }else{
            Log.i(TAG,"No Comment Yet for this story");
            mTVCommentCount.setText(" NO COMMENT YET ");
            mCommentText.setVisibility(View.GONE);
        }

        mTVLoading.setVisibility(View.GONE);
        mRLCommentList.setVisibility(View.VISIBLE);
    }

    /**
     * Method to post new comment on the story
     */
    @Override
    public void onAddCommentButtonClicked(String comment) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        int content_id = App.getContentData().get(mCurrentFragmentPosition).id;

        if(!(Utils.getUserId(HomeActivity.this)).equals("")) {
            //User is allowed to comment only if loggedIn
            NetworkApiHelper.getInstance().addComment(Utils.getUserId(HomeActivity.this), content_id, comment, new NetworkApiCallback<AddCommentResponse>() {

                @Override
                public void success(AddCommentResponse addCommentResponse, Response response) {
                    Log.i(TAG, "Add Comment Request Success");
                    if (mCommentsRecyclerViewAdapter != null)
                        mCommentsRecyclerViewAdapter.notifyDataSetChanged();
                }

                @Override
                public void failure(AddCommentResponse addCommentResponse) {
                    Log.i(TAG, "Add Comment Request Failure");
                }

                @Override
                public void networkFailure(RetrofitError error) {
                    Log.i(TAG, "Add Comment Request Network Failure, Error Type::" + error.getMessage());

                }
            });
        }else{
            //TODO: Redirect user to facebook login page
            Toast.makeText(this,"Please login",Toast.LENGTH_SHORT);
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
       // mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        final NavigationView mNavigationView = (NavigationView)findViewById(R.id.navigation_view);


/*
        String[] arr = getResources().getStringArray(R.array.menu_items);
        mDrawerListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        mDrawerList.setAdapter(mDrawerListAdapter);*/

        mProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mDrawerLayout.isDrawerOpen(mNavigationView))
                    mDrawerLayout.closeDrawer(mNavigationView);
                else
                    mDrawerLayout.openDrawer(mNavigationView);
            }
        });
        PersonInfo myDetails = new PersonInfo();
        myDetails = App.getPersonInfo();
        View header = mNavigationView.getHeaderView(0);

        CircularImageView mPImg = (CircularImageView)header.findViewById(R.id.dr_profile_img) ;
        TextView mPN = (TextView)header.findViewById(R.id.dr_profile_name);
        if(myDetails!=null) {
            if (myDetails.imageUrl != null && myDetails.imageUrl != "")
                Picasso.with(HomeActivity.this).load(myDetails.imageUrl).fit().centerCrop().into(mPImg);
            else
                Picasso.with(HomeActivity.this).load(R.drawable.sample_pimg).fit().centerCrop().into(mPImg);
            mPN.setText(myDetails.name);
        }
        else{
            Picasso.with(HomeActivity.this).load("http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803").fit().centerCrop().into(mPImg);
            mPN.setText("Guest");
        }
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

       /* mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i){
                    case 0: Toast.makeText(HomeActivity.this,"My Profile is to be shown",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                        intent.putExtra("USER_ID",Utils.getUserId(HomeActivity.this));
                        intent.putExtra("PERSON_ID",Utils.getUserId(HomeActivity.this));
                        startActivity(intent);
                        //finish();*//**TODO: Add code to show My Profile Page**//*
                        break;*//**TODO: Add code to show My Profile Page**//*

                    case 1: sendFeedback();
                        break;

                    case 2: sendInvite();
                        break;
                }
            }
        });*/
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

        final EditText feedbackText = (EditText)findViewById(R.id.feedback_text);
        TextView feedbackCancel = (TextView)dialog.findViewById(R.id.feedback_cancel);
        TextView feedbackSend =(TextView) dialog.findViewById(R.id.feedback_send);



        feedbackCancel.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        feedbackSend.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent _Intent = new Intent(android.content.Intent.ACTION_SEND);
                _Intent.setType("text/email");
                _Intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
                _Intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
                _Intent.putExtra(android.content.Intent.EXTRA_TEXT, feedbackText.getText());
                startActivity(Intent.createChooser(_Intent, getString(R.string.title_send_feedback)));
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}



