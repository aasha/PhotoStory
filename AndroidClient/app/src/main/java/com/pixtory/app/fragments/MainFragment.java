package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.*;
import com.pixtory.app.R;
import com.pixtory.app.adapters.CommentsListAdapter;
import com.pixtory.app.animations.BounceAnimator;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetCommentDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.userprofile.UserProfileActivity2;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.ObservableScrollView;
import com.pixtory.app.views.ScrollViewListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class MainFragment extends Fragment implements ScrollViewListener{

    private static final String TAG = MainFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final String ARG_PARAM4 = "PROFILE_CONTENT";

    // TODO: Rename and change types of parameters
    private int mContentIndex;
    private boolean isProfileContent;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    private OnMainFragmentInteractionListener mListener;

    private static final String Vid_Tap_Like = "Vid_Tap_Like";
    private static final String Vid_Tap_Unlike = "Vid_Tap_Unlike";
    private static final String Vid_Reco_Tap = "Vid_Reco_Tap";

    public static final String Vid_Reco_VideoExpand = "Vid_Reco_VideoExpand";

    private Context mContext;

    @Bind(R.id.pic_story_layout)
    NestedScrollView mStoryParentLayout = null;

    @Bind(R.id.content_layout)
    LinearLayout mContentLayout =null;

    RelativeLayout mStoryLayout = null;
    LinearLayout mCommentsLayout = null;

    @Bind(R.id.image_main)
    ImageView mImageMain = null;

    @Bind(R.id.bottom_container)
    RelativeLayout mImageDetailBottomContainer = null;

    @Bind(R.id.image_details_layout)
    ObservableScrollView mImageDetailsLayout;

    int mImageInfoLayoutHeight;

    @Bind(R.id.text_title)
    TextView mTextTitle = null;

    @Bind(R.id.text_place)
    TextView mTextPlace = null;

    @Bind(R.id.image_like)
    ImageView mImageLike = null;

//    @Bind(R.id.slant_view)
//    SlantView mSlantView = null;

    @Bind(R.id.like_count)
    TextView mLikeCountTV = null;

    @Bind(R.id.text_expert)
    TextView mTextExpert = null;

    @Bind(R.id.like_layout)
    RelativeLayout mTopLikeLayout = null;

    @Bind(R.id.comment_share_ll)
    LinearLayout mCommentShareLayout;

    @Bind(R.id.btnComment)
    LinearLayout mCommentBtn = null;

    @Bind(R.id.btnShare)
    LinearLayout mShareBtn = null;

    @Bind(R.id.story_back_click)
    LinearLayout storyBackClick=null;

    @Bind(R.id.story_back_img)
    ImageView storyBackImg = null;

    @Bind(R.id.swipe_up_sign)
    TextView swipeUpSign = null;

    ImageView swipeUpArrow;

    ProgressDialog mProgressDialog;

    RelativeLayout.LayoutParams imgViewLayoutParams ;

    private int mSoftBarHeight = 0;
    private boolean isFullScreenShown = true;
    private boolean isCommentsVisible = false;
    private int mImageExtendedHeight;

    private float mLikeLayoutPaddingTop;
    private int mSlantViewHtInPx;
    private int mBottomScreenHt;
    final private float mHalfScreenPer = 0.9f;

    private int  scrollY,oldScrollY;
    private boolean isScrollingUp = true;
    
    @SuppressLint("NewApi")
    private int getSoftbuttonsbarHeight() {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param idx    Parameter 1.
     * @param param3 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(int idx, String contentJson, String param3) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, idx);
        args.putString(ARG_PARAM2, contentJson);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    public static MainFragment newInstance(int idx,boolean isProfileContent) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, idx);
        args.putBoolean(ARG_PARAM4,isProfileContent);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    private ContentData mContentData = null;

    // Used to test indes positions. TEMP VARIABLE
    private String mCIDX = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"callback->main fragment on create");
        mContext = getActivity();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Loading Pixstory For You...!!");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mProgressDialog.show();

        if (getArguments() != null) {
            isProfileContent = getArguments().getBoolean(ARG_PARAM4);
            if(!isProfileContent){

            try {
                mContentIndex = getArguments().getInt(ARG_PARAM1);
                mContentData = App.getContentData().get(mContentIndex);
                mCIDX = getArguments().getString(ARG_PARAM3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
            else{
                try {
                    mContentIndex = getArguments().getInt(ARG_PARAM1);
                    mContentData = App.getProfileContentData().get(mContentIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        mSoftBarHeight = getSoftbuttonsbarHeight();

        // Device info
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;

        mSlantViewHtInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 100, displayMetrics );
        mImageExtendedHeight = mDeviceHeightInPx +mSlantViewHtInPx;
        mBottomScreenHt = (int)(0.60f*mDeviceHeightInPx);
        mDeviceHeightInPx += mSoftBarHeight;


        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    View mRootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        storyBackClick = (LinearLayout)mRootView.findViewById(R.id.story_back_click);
        storyBackImg = (ImageView)mRootView.findViewById(R.id.story_back_img);
        swipeUpSign = (TextView)mRootView.findViewById(R.id.swipe_up_sign);

        //Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
        Animation upAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -0.03f);
        Animation downAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.03f);
        upAnimation.setStartOffset(500);
        upAnimation.setInterpolator(new LinearInterpolator());
        Animation   mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -0.03f);
        Animation upDown = AnimationUtils.loadAnimation(mContext,R.anim.up_down);

        //upDown.setStartOffset(5000);
        upDown.setInterpolator(new LinearInterpolator());
        upDown.setRepeatCount(Animation.INFINITE);
       /* AnimationSet mAnimation = new AnimationSet(true);
        mAnimation.addAnimation(upAnimation);
        mAnimation.addAnimation(downAnimation);*/
        //mAnimation.setStartOffset(500);
        mAnimation.setDuration(400);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());

        swipeUpArrow = (ImageView)mRootView.findViewById(R.id.swipe_up_arrow);
        swipeUpArrow.setAnimation(mAnimation);
        //swipeUpSign.setAnimation(animation);
        //swipeUpSign.startAnimation(animation);
        if(!isProfileContent)
        {
            storyBackClick.setVisibility(View.GONE);
            storyBackImg.setVisibility(View.GONE);
        }
        storyBackClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_PixtoryBack_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",mContentData.id+"")
                        .build());
                getActivity().onBackPressed();
            }
        });

        storyBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_PixtoryBack_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",mContentData.id+"")
                        .build());
                getActivity().onBackPressed();
            }
        });

        Log.d(TAG, "onCreateView is called for index = " + mContentIndex);
        ButterKnife.bind(this, mRootView);

        mImageInfoLayoutHeight = (int)getResources().getDimension(R.dimen.image_layout_height);
        Log.i("mImageInfoHeight is :::",""+mImageInfoLayoutHeight);

        setUpStoryContent();
        bindData();
        attachPixtoryContent(AppConstants.SHOW_PIC_STORY);

        mCommentShareLayout.setVisibility(View.GONE);
        mImageDetailsLayout.setSmoothScrollingEnabled(true);
        mImageDetailsLayout.setScrollViewListener(this);

        /** Adjusting padding top for the Like layout so that is sticks to bottom of screen**/
        mLikeLayoutPaddingTop = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);

        mHalfScreenSize = (int)(mHalfScreenPer *mDeviceHeightInPx);
        Log.i(TAG,"mHalfScreenSize::"+mHalfScreenSize);

        mStoryParentLayout.getLayoutParams().height =  (int)(0.75f *mDeviceHeightInPx);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        relativeParams.setMargins(0, (int)mLikeLayoutPaddingTop, 0, 0);
        mTopLikeLayout.setLayoutParams(relativeParams);
        mTopLikeLayout.requestLayout();
        setUpFullScreen();

        imgViewLayoutParams = (RelativeLayout.LayoutParams) mImageMain.getLayoutParams();
        return mRootView;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG,"OnActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG,"callback->main fragment on view created");

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Binding story data
     */
    private void bindData() {
        if (mContentData == null) {
            return;
        }

        final ContentData cd = mContentData;
        Picasso.with(mContext).load(mContentData.pictureUrl).fit().into(mImageMain
                ,new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onError() {
                        mProgressDialog.dismiss();

                    }
                });
        mTextTitle.setText(cd.name);
        mTextPlace.setText(cd.place);
        String name = (!(cd.personDetails.name.equals("")))? "By , "+cd.personDetails.name : " ";
        mTextExpert.setText(name);

        if (mContentData.likedByUser == true)
            mImageLike.setImageResource(R.drawable.liked);
        else
            mImageLike.setImageResource(R.drawable.like);

        mLikeCountTV.setText(String.valueOf(cd.likeCount));

        //***Binding StoryContent****/
        ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
        TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
        TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
        TextView mTextStoryDetails = (TextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
        mShareBtn = (LinearLayout) mRootView.findViewById(R.id.btnShare);
        mCommentBtn = (LinearLayout) mRootView.findViewById(R.id.btnComment);

        if (cd != null) {
            if (cd.personDetails != null) {
                if (cd.personDetails.imageUrl == null || cd.personDetails.imageUrl.trim().equals("")){
                    cd.personDetails.imageUrl = "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";
                }
                Picasso.with(mContext).load(cd.personDetails.imageUrl).fit().into(mProfileImage);
                mTextName.setText(cd.personDetails.name);
                mTextDesc.setText(cd.personDetails.description);
                mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mContext,cd.personDetails.id+"",Toast.LENGTH_SHORT).show();
                        navigateToUserProfile(cd);
                    }
                });
                mTextName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToUserProfile(cd);
                    }

                });
                mTextDesc.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         navigateToUserProfile(cd);
                     }
            });
            }
            Log.i(TAG,"bindStorycd data->date::"+cd.date);
            mTextStoryDetails.setText(cd.pictureDescription+"\n \n \n\n \n \n");
        }

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utils.isNotEmpty(Utils.getFbID(mContext))) {

                    sharePixtory(cd);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Share_Click")
                            .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                            .put("PIXTORY_ID", cd.id + "")
                            .build());
                }else {
                    mListener.showLoginAlert();
                }
            }
        });
        mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Comment_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"")
                        .build());

                buildCommentsLayout(cd);
                attachPixtoryContent(AppConstants.SHOW_PIC_COMMENTS);
            }
        });

    }

    /**
     * @param story_or_comment
     * Rationale - Adding story_view_layout fragment to MainFragment
     */
    public void attachPixtoryContent(int story_or_comment){
        mContentLayout.removeAllViews();
        if(story_or_comment == AppConstants.SHOW_PIC_STORY){
            mContentLayout.addView(mStoryLayout);
            setCommentsVisible(false);
            mStoryParentLayout.setNestedScrollingEnabled(true);
            mImageDetailsLayout.setScrollingEnabled(true);

        }else{
            mImageDetailsLayout.setScrollingEnabled(false);
            mContentLayout.addView(mCommentsLayout);
            setCommentsVisible(true);
            mStoryParentLayout.setNestedScrollingEnabled(false);
            mImageDetailsLayout.setScrollingEnabled(false);

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMainFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMainFragmentInteractionListener");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mContentData == null)
            return;

        // Make sure that we are currently visible
//        if (this.isVisible()) {
//            resetFragmentState();
//        }
    }

    private void setUpStoryContent() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        mStoryLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_view_layout, null);
        mCommentsLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.story_comment_layout , null);
    }

    public boolean isCommentsVisible() {
        return isCommentsVisible;
    }

    public void setCommentsVisible(boolean commentsVisible) {
        isCommentsVisible = commentsVisible;

        if(isCommentsVisible)
            mCommentShareLayout.setVisibility(View.GONE);
        else
            mCommentShareLayout.setVisibility(View.VISIBLE);

    }

    /**
     *
     * @param scrollView
     * @param x
     * @param y
     * @param oldX
     * @param oldY
     *
     * Rationale - OnScrollChange listener implementation for mImageDetailsLayout
     */
    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldX, int oldY) {
//        Log.i(TAG,"x:y="+x+":"+y);
        scrollY = y;
        oldScrollY = oldY;
        modifyScreenHeight(y);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMainFragmentInteractionListener {
        void showMenuIcon(boolean showMenuIcon);
        void showLoginAlert();
    }

//
//    public void resetFragmentState() {
//        setUpFullScreen();
//    }

    /********Swipe Up and Down Logic*********************/

    final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    Log.i(TAG, "MotionEvent.ACTION_DOWN");
                    swipeUpArrow.clearAnimation();
                    swipeUpArrow.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    //Toast.makeText(mContext,"Long tap detected",Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Long Tap");
                    if (isFullScreenShown) {
                        showWallpaperAlert();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Wallpaper_LongPress")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID", mContentData.id + "")
                                .put("POSITION_ID", mContentIndex + "")
                                .build());
                    }
                }

//                @Override
//                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                    if (distanceX < 5 && distanceX > -5) {
////                        mIsScrolling = true;
////                        mIsFling = false;
//                        Log.i(TAG, "gesture->OnScroll::::");
////
//
//                    }
//                    return super.onScroll(e1, e2, distanceX, distanceY);
//                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
                    Log.i(TAG, "gesture->OnFling::::");


                    final int SWIPE_MIN_DISTANCE = 50;
                    final int SWIPE_THRESHOLD_VELOCITY = 100;
                    try {
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                            mIsFling = true;
//                            Log.i(TAG, "onFlingUp::::"+mImageDetailsLayout.getScrollY());

//                            modifyScreenHeight(mImageDetailsLayout.getScrollY());

                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                            mIsFling = true;
//                            if(!isFullScreenShown){
////                                showFullScreen();
//                                isFullScreenShown = true;
//                                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PictureView")
//                                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
//                                        .put("PIXTORY_ID",""+mContentData.id)
//                                        .build());

                        } else {
//                            mIsFling = false;
                        return false;
                        }
                    } catch (Exception e) {
                        // nothing
//                        e.printStackTrace();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    private void navigateToUserProfile(ContentData cd){
        if(isProfileContent)
            getActivity().onBackPressed();
        else{
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Profile_Click")
                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                .put("PIXTORY_ID",cd.id+"")
                .put("POSITION_ID",mContentIndex+"")
                .build());
        Intent intent = new Intent(mContext, UserProfileActivity2.class);
        intent.putExtra("USER_ID",Utils.getUserId(mContext));
        intent.putExtra("PERSON_ID",cd.personDetails.id+"");
        startActivity(intent);
        }
    }
    
    private boolean modifyScreenHeight(int offset) {

        imgViewLayoutParams.height = (mImageExtendedHeight) -offset;
        mImageMain.setLayoutParams(imgViewLayoutParams);

        if(offset > mBottomScreenHt ){
            showCommentsShareLayout(true);
            mListener.showMenuIcon(false);
            storyBackImg.setVisibility(View.GONE);
        }
        else{
            mListener.showMenuIcon(true);
            showCommentsShareLayout(false);
            storyBackImg.setVisibility(View.VISIBLE);
        }

        return true;

    }

    private void showCommentsShareLayout(boolean showCommentsShare){
        if(!isCommentsVisible()) {
            if (showCommentsShare)
                mCommentShareLayout.setVisibility(View.VISIBLE);
            else
                mCommentShareLayout.setVisibility(View.GONE);
        }

    }

    @OnTouch(R.id.pic_story_layout)
    public boolean onTouchContent(NestedScrollView view, MotionEvent me) {

        //if comment section is visible , swipe up and down gesture for story and content is disabled
        if(isCommentsVisible()){
            return false;
        }

//        if (gesture.onTouchEvent(me)) {
//            return true;
//        }
        // Disallow the touch request for parent scroll on touch of child view
        mImageDetailsLayout.requestDisallowInterceptTouchEvent(true);
        if (me.getAction() == MotionEvent.ACTION_UP) {
            setUpHalfScreen();
        }

        return false;

    }

    int mHalfScreenSize;
    @OnTouch(R.id.image_details_layout)
    public boolean onTouchStory(ScrollView view, MotionEvent me) {

        //if comment section is visible , swipe up and down gesture for story and content is disabled
        if(isCommentsVisible()){
            return false;
        }

        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
//            Log.i(TAG , "MotionEvent.ACTION_UP::"+isScrollingUp+"::scrollY::"+scrollY);
            setUpHalfScreen();
        }

        return false;
    }

    private void setUpFullScreen(){
        isFullScreenShown = true;
        mImageDetailsLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.VISIBLE);
    }

    private void setUpHalfScreen(){
        mTextExpert.setVisibility(View.GONE);

        if(isCommentsVisible())
            attachPixtoryContent(AppConstants.SHOW_PIC_STORY);

        isScrollingUp= (scrollY < oldScrollY)?false:true;
//        Log.i(TAG,"scrollY::"+scrollY+"::mHalfSCreenSize::"+mHalfScreenSize);
        if(scrollY < mHalfScreenSize){

            mImageDetailsLayout.post(new Runnable() {
                @Override
                public void run() {
                    if(isScrollingUp){
                        mImageDetailsLayout.smoothScrollTo(0,mHalfScreenSize);

//                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_StoryView")
//                                    .put(AppConstants.USER_ID,Utils.getUserId(mContext))
//                                    .put("PIXTORY_ID",""+mContentData.id)
//                                    .build());
                    }
                    else {
                        mImageDetailsLayout.smoothScrollTo(0, 0);
                        mTextExpert.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    private void animateContent(View view , final boolean showContent , int fromY , int toY,int duration){

        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(view ,"translationY" , fromY, toY);
        transAnimation.setDuration(duration);//set duration
        transAnimation.start();//start animation

        transAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(showContent){
//                    mSlantView.setVisibility(View.GONE);
                    mTextExpert.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * Comments Section Implementation
     */

    private ArrayList<CommentData> commentDataList;
    private RecyclerView mCommentsRecyclerView;
    private CommentsListAdapter mCommentsRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mTVCommentCount , mCommentText;
    private RelativeLayout mRLCommentList = null;
    private TextView mTVLoading;

    private void buildCommentsLayout(ContentData data){

        Button mPostComment = (Button)mCommentsLayout.findViewById(R.id.postComment);
        ImageView mCommentCloseBtn = (ImageView)mCommentsLayout.findViewById(R.id.closeBtn);

        TextView mPlace = (TextView)mCommentsLayout.findViewById(R.id.txtPlace);
        TextView mDesc = (TextView)mCommentsLayout.findViewById(R.id.txtDesc);

        mPlace.setText(data.place);
        mDesc.setText(data.name);

        mRLCommentList = (RelativeLayout)mCommentsLayout.findViewById(R.id.comments_layout);
        mTVLoading = (TextView)mCommentsLayout.findViewById(R.id.loading_comments);
        mTVCommentCount = (TextView)mCommentsLayout.findViewById(R.id.tvCount);
        mCommentText = (TextView)mCommentsLayout.findViewById(R.id.comment_text);

        mCommentsRecyclerView = (RecyclerView)mCommentsLayout.findViewById(R.id.commentsList);
        mLayoutManager = new LinearLayoutManager(mContext);
        mCommentsRecyclerView.setLayoutManager(mLayoutManager);

        setCommentsVisible(true);

        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utils.isNotEmpty(Utils.getFbID(mContext))) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    CommentsDialogFragment commentsDialogFragment = CommentsDialogFragment.newInstance("Some title");
                    commentsDialogFragment.show(fm, "fragment_alert");
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_AddComment_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                            .put("PIXTORY_ID",mContentData.id+"")
                            .build());
                }
                else{
                    mListener.showLoginAlert();
                }

            }
        });

        fetchCommentList(data.id);

        mCommentCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachPixtoryContent(AppConstants.SHOW_PIC_STORY);
            }
        });

    }

    /**
     * Method to post a new comment
     * @param comment
     */
    public void postComment(String comment) {

            final int content_id = mContentData.id;

            if(!((Utils.getFbID(mContext)).equals(""))) {
                //User is allowed to comment only if loggedIn
                NetworkApiHelper.getInstance().addComment(Utils.getUserId(mContext), content_id, comment.trim(), new NetworkApiCallback<AddCommentResponse>() {

                    @Override
                    public void success(AddCommentResponse addCommentResponse, Response response) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_SubmitComment_Click")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID",mContentData.id+"")
                                .build());

                        fetchCommentList(content_id);
                        Log.i(TAG , "Comment Succes/sfully Posted");
                        Toast.makeText(getActivity(),"Comment Successfully Posted",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(AddCommentResponse addCommentResponse) {
                        Toast.makeText(getActivity(),"Oops Something went wrong!!",Toast.LENGTH_SHORT).show();

                        Log.i(TAG, "Add Comment Request Failure");
                    }
                    @Override
                    public void networkFailure(RetrofitError error) {
                        Log.i(TAG, "Add Comment Request Network Failure, Error Type::" + error.getMessage());
                        Toast.makeText(getActivity(),"Please check your network connection",Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //TODO: Redirect user to facebook login page
                Toast.makeText(getActivity(),"Please login",Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * Method to hide loading comments progressbar and show comments list
     */
    public void setCommentListVisibility(){

        if(commentDataList!= null && commentDataList.size()>0){

            Log.i(TAG,"Comment Count::"+commentDataList.size());
            mTVCommentCount.setText(String.valueOf(commentDataList.size()));
            mCommentText.setVisibility(View.VISIBLE);
            mCommentsRecyclerViewAdapter = new CommentsListAdapter(mContext);
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

    private void fetchCommentList(int story_id){

        mTVLoading.setVisibility(View.VISIBLE);
        mRLCommentList.setVisibility(View.INVISIBLE);

        NetworkApiHelper.getInstance().getCommentDetailList(Utils.getUserId(mContext), story_id , new NetworkApiCallback<GetCommentDetailsResponse>() {

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
     *
     * @param view
     * Implementation to like a story
     */
    @OnClick(R.id.image_like)
    public void onLikeClick(ImageView view) {
        if(Utils.isNotEmpty(Utils.getFbID(mContext))) {
            if (mContentData.likedByUser == false) {
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.flip_in);
                anim.setTarget(mImageLike);
                anim.setDuration(200);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mImageLike.setImageResource(R.drawable.liked);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mContentData.likedByUser = true;
                anim.start();
                if(isFullScreenShown)
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Like_Click")
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .put("PIXTORY_ID", "" + mContentData.id)
                        .put("POSITION_ID",""+mContentIndex)
                        .put("BOOLEAN","True")
                        .build());
                else
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_Like")
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("PIXTORY_ID", "" + mContentData.id)
                            .put("POSITION_ID",""+mContentIndex)
                            .put("BOOLEAN","False")
                            .build());

            } else {
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.flip_out);
                anim.setTarget(mImageLike);
                anim.setDuration(200);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mImageLike.setImageResource(R.drawable.like);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
                mContentData.likedByUser = false;
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Unlike)
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .put(AppConstants.OPINION_ID, "" + mContentData.id)
                        .build());
            }
            sendLikeToBackend(mContentData.id, mContentData.likedByUser);
        }
        else {
            mListener.showLoginAlert();
        }
    }

    private void sendLikeToBackend(int contentId, final boolean isLiked) {
        NetworkApiHelper.getInstance().likeContent(Utils.getUserId(getActivity()), contentId, isLiked, new NetworkApiCallback<BaseResponse>() {

            @Override
            public void success(BaseResponse baseResponse, Response response) {
                if(isLiked) {
                    mContentData.likeCount += 1;
                }else{
                    if(mContentData.likeCount>0)
                        mContentData.likeCount -= 1;
                }
                mLikeCountTV.setText(String.valueOf(mContentData.likeCount));
            }

            @Override
            public void failure(BaseResponse baseResponse) {

            }

            @Override
            public void networkFailure(RetrofitError error) {

            }
        });
    }

    /**
     * To share image with outside application
     * @param uriToImage
     */
    private void share(Uri uriToImage) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    /**
     * To switch view from comments to story content
     */
    public void onBackButtonClicked(){
        if(isCommentsVisible()){
            setCommentsVisible(false);
//            mListener.onAnimateMenuIcon(false);
            attachPixtoryContent(AppConstants.SHOW_PIC_STORY);
        }
    }

    public  boolean isFullScreenShown(){return isFullScreenShown;}
    
    private void showWallpaperAlert(){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wallpaper_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);


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
        String imgUrl = mContentData.pictureUrl;
        Picasso.with(mContext).load(imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Toast.makeText(mContext,"Wallpaper set",Toast.LENGTH_SHORT).show();
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(mContext.getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(bitmap);
                } catch (IOException e) {

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

    public void sharePixtory(final ContentData contentData)
    {
        Picasso.with(mContext).load(contentData.pictureUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {

                    File cachePath = new File(mContext.getCacheDir(), "images");
                    cachePath.mkdirs(); // don't forget to make the directory
                    FileOutputStream stream = new FileOutputStream(cachePath + "/"+contentData.name+".png"); // overwrites this image every time
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File imagePath = new File(mContext.getCacheDir(), "images");
                File newFile = new File(imagePath, contentData.name+".png");
                Uri contentUri = FileProvider.getUriForFile(mContext, "com.pixtory.app.fileprovider", newFile);

                if (contentUri != null) {

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                    shareIntent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                    PackageManager packageManager = getActivity().getPackageManager();
                    List<Intent> targetInviteIntents=new ArrayList<Intent>();
                    List<ResolveInfo> resInfos=packageManager.queryIntentActivities(shareIntent, 0);
                    if(!resInfos.isEmpty()){

                        for(ResolveInfo resInfo : resInfos){
                            String packageName=resInfo.activityInfo.packageName;
                            Log.i("Package Name", packageName);
                            if(packageName.contains("com.facebook.katana") || packageName.contains("android.gm") || packageName.contains("com.instagram.android")){
                                Intent intent=new Intent();
                                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                intent.setAction(Intent.ACTION_SEND);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                intent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                intent.putExtra(Intent.EXTRA_TEXT,contentData.name+"\nBy "+contentData.personDetails.name+"\n\n"+contentData.pictureDescription);
                                intent.setPackage(packageName);
                                targetInviteIntents.add(intent);
                            }
                            else if(packageName.contains("com.whatsapp")){
                                Intent intent=new Intent();
                                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                intent.setAction(Intent.ACTION_SEND);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                intent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                intent.putExtra(Intent.EXTRA_TEXT,"*"+contentData.name+"*\nBy _"+contentData.personDetails.name+"_\n\n"+contentData.pictureDescription);
                                intent.setPackage(packageName);
                                targetInviteIntents.add(intent);
                            }
                        }
                        if(!targetInviteIntents.isEmpty()){

                            Intent chooserIntent=Intent.createChooser(targetInviteIntents.remove(0), "Share Pixtory via");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetInviteIntents.toArray(new Parcelable[]{}));
                            startActivity(chooserIntent);
                        }else{
                            Toast.makeText(mContext,"No Apps to share",Toast.LENGTH_SHORT).show();
                        }
                    }

                    //shareIntent.putExtra(Intent.EXTRA_TEXT,"*"+contentData.name+"*\nBy _"+contentData.personDetails.name+"_\n\n"+contentData.pictureDescription);
                    //startActivity(Intent.createChooser(shareIntent, "Share pixtory via"));

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

    /**
     * Callback to show loading progress bar when image is loaded
     */
    private class ImageLoadedCallback implements Callback {
        ProgressDialog progressBar;

        public  ImageLoadedCallback(ProgressDialog progDialog){
            progressBar = progDialog;
        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {

        }
    }

}
