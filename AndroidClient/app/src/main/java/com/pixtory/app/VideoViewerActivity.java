package com.pixtory.app;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pixtory.app.adapters.OpinionViewerAdapter;
import com.pixtory.app.adapters.ProductRecommendationAdapter;
import com.pixtory.app.adapters.VideoViewerAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.VideoFragment;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.Product;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.typeface.Intro;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class VideoViewerActivity extends FragmentActivity implements VideoFragment.OnMainFragmentInteractionListener, ProductRecommendationAdapter.ProductViewHolder.FollowClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mContentIndex;


    private Context mCtx = null;
    private ContentResolver mResolver = null;

    private ViewPager mPager = null;

    private int mCurrentFragmentPosition = 0;
    LinearLayout mRecLayout = null;
    private RecyclerView mRecomRecycle = null;
    private LinearLayoutManager mLayoutManager = null;
    private ProductRecommendationAdapter productRecommendationAdapter = null;
    private OpinionViewerAdapter mCursorPagerAdapter = null;
    //Analytics
    private static final String SCREEN_NAME = "User_Profile_Video_Page";
    private static final String MF_NextVideo_Swipe = "MF_NextVideo_Swipe";
    private static final String MF_Profile_Tap = "MF_Profile_Tap";
    VideoViewerAdapter mFragmentAdapter = null;
    int previousPage = 0;

    @Bind(R.id.imgUser)
    ImageView mImgClose;
    Tracker mTracker;
    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(SCREEN_NAME);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_viewer_layout);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            try {
                mContentIndex = getIntent().getIntExtra(ARG_PARAM1, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mTracker = App.getmInstance().getDefaultTracker();
        setUpRecomView();
        mFragmentAdapter = new VideoViewerAdapter(getSupportFragmentManager());
        mFragmentAdapter.setData(App.getLikedContentData());
        mPager = (ViewPager)findViewById(R.id.pager);
        mCursorPagerAdapter = new OpinionViewerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mFragmentAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(previousPage == 0)
                    previousPage = position;
                else
                    previousPage = mCurrentFragmentPosition;
                mCurrentFragmentPosition = position;
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( MF_NextVideo_Swipe)
                        .put(AppConstants.OPINION_ID, App.getLikedContentData().get(mContentIndex).id + "")
                        .put(AppConstants.USER_ID, Utils.getUserId(mCtx))
                        .build());
                //setUIForrotateExpertPanelAndAppear();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.d(TAG, "onPageScrollStateChanged = "+state);
            }
        });
        mPager.setCurrentItem(mContentIndex);
    }
    float previousPoint = 0.0f;

    @OnClick(R.id.imgUser)
    public void onUserImageClick(){
        onViewUserProfileScreen();
    }

    private void setUpRecomView() {

        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        mRecLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.recycler_view_layout, null);
        TextView textRecomendation = (TextView)mRecLayout.findViewById(R.id.textRecomendation);
        Intro.applyFont(this, textRecomendation);
        // Set up recycler view
        mRecomRecycle = (RecyclerView) mRecLayout.findViewById(R.id.recom_recy);
        mRecomRecycle.setHasFixedSize(true);

        mLayoutManager =  new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecomRecycle.setLayoutManager(mLayoutManager);

        productRecommendationAdapter = new ProductRecommendationAdapter(this);
        productRecommendationAdapter.followClickListener = this;
        mRecomRecycle.setAdapter(productRecommendationAdapter);
    }


    @Override
    public void onDetachRecoView(Fragment ff, int position) {
       // mPager.isScrollingEnabled = true;
        final ViewGroup parent = (ViewGroup) mRecLayout.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
    }

    @Override
    public void onAttachRecoView(Fragment ff, int position) {
       // mPager.isScrollingEnabled = false;
        VideoFragment f = (VideoFragment) ff;
        final ViewGroup parent = (ViewGroup) mRecLayout.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        f.attachRecycerView(mRecLayout);
        try {
            productRecommendationAdapter.setData(App.getLikedContentData().get(position).productList);
            productRecommendationAdapter.setSelected(0);
            mRecomRecycle.scrollToPosition(0);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public void onPDPPageSelected(Fragment f, int position) {
        mRecomRecycle.scrollToPosition(position);
        productRecommendationAdapter.setSelected(position);
    }

    public void onViewUserProfileScreen() {
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(MF_Profile_Tap)
                .put(AppConstants.USER_ID, Utils.getUserId(mCtx))
                .build());
        finish();
    }

    public void onCloseUserProfileScreen() {

    }

    @Override
    public void onFollowClick(View caller, int pos) {
        VideoFragment fr1 = (VideoFragment) mFragmentAdapter.getFragmentAtIndex(mCurrentFragmentPosition);
        fr1.showFullScreenPDP(pos);
    }

    @Override
    public void onBookMarkClick(int pos, boolean value) {
        VideoFragment fr1 = (VideoFragment) mFragmentAdapter.getFragmentAtIndex(mCurrentFragmentPosition);
        fr1.bookMarkInPDP(pos, value);
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
    public void onPDPPageBookMarked(Fragment f, int contentPositon, int positionOfProduct, boolean value) {
        List<ContentData> cd = App.getLikedContentData();
        List<Product> pdList = cd.get(contentPositon).productList;
        pdList.get(positionOfProduct).bookmarkedByUser = value;
        App.setContentData((ArrayList<ContentData>) cd);
        productRecommendationAdapter.setData(cd.get(contentPositon).productList);
        productRecommendationAdapter.setBookMarked();
        NetworkApiHelper.getInstance().addComment(Utils.getUserId(VideoViewerActivity.this), pdList.get(positionOfProduct).productId, value, new NetworkApiCallback<AddCommentResponse>() {
            @Override
            public void success(AddCommentResponse o, Response response) {

            }

            @Override
            public void failure(AddCommentResponse error) {

            }

            @Override
            public void networkFailure(RetrofitError error) {

            }
        });
    }
}
