package com.pixtory.app.userprofile;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixtory.app.R;
import com.pixtory.app.adapters.CommentsListAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.CommentsDialogFragment;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetCommentDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.ObservableScrollView;
import com.pixtory.app.views.ScrollViewListener;
import com.pixtory.app.views.SlantView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by krish on 16/06/2016.
 */



public class StoryFragment extends android.support.v4.app.Fragment implements ScrollViewListener {

    private static final String TAG = StoryFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "PROFILE_CONTENT";
    public static final String ARG_PARAM2 = "CONTENT_INDEX";

    // TODO: Rename and change types of parameters
    private int mContentIndex;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    private OnStoryFragmentInteractionListener mListener;

    private static final String Vid_Tap_Like = "Vid_Tap_Like";
    private static final String Vid_Tap_Unlike = "Vid_Tap_Unlike";
    private static final String Vid_Reco_Tap = "Vid_Reco_Tap";

    private static final int SHOW_PIC_STORY = 88;
    private static final int SHOW_PIC_COMMENTS = 89;


    public static final String Vid_Reco_VideoExpand = "Vid_Reco_VideoExpand";

    private Context mContext;

    @Bind(R.id.pic_story_layout)
    LinearLayout mStoryParentLayout = null;

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

    @Bind(R.id.slant_view)
    SlantView mSlantView = null;

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

    RelativeLayout.LayoutParams imgViewLayoutParams ;

    private int mSoftBarHeight = 0;
    private boolean isFullScreenShown = true;
    private boolean isCommentsVisible = false;
    private int mImageExtendedHeight;

    private float mLikeLayoutPaddingTop;
    private int mSlantViewHtInPx;
    private int mBottomScreenHt;
    final private float mHalfScreenPer = 0.55f;

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
     * @return A new instance of fragment StoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StoryFragment newInstance(boolean isProfileContent,int contentIndex) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isProfileContent);
        args.putInt(ARG_PARAM2,contentIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public StoryFragment() {
        // Required empty public constructor
    }



    private ContentData mContentData = null;

    // Used to test indes positions. TEMP VARIABLE
    private String mCIDX = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            try {
                if(getArguments().getBoolean(ARG_PARAM1)) {
                    mContentIndex = getArguments().getInt(ARG_PARAM2);
                    mContentData = App.getProfileContentData().get(mContentIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mSoftBarHeight = getSoftbuttonsbarHeight();

        // Device info
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;


        mSlantViewHtInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 100, displayMetrics );
        mImageExtendedHeight = mDeviceHeightInPx +mSlantViewHtInPx;
        mBottomScreenHt = (int)(0.30f*mDeviceHeightInPx);
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

        Log.d(TAG, "onCreateView is called for index = " + mContentIndex);
        ButterKnife.bind(this, mRootView);

        mImageInfoLayoutHeight = (int)getResources().getDimension(R.dimen.image_layout_height);
        Log.i("mImageInfoHeight is :::",""+mImageInfoLayoutHeight);

        setUpStoryContent();
        bindData();
        attachPixtoryContent(SHOW_PIC_STORY);

        mStoryParentLayout.setMinimumHeight((int)getResources().getDimension(R.dimen.story_min_height));

        mImageDetailsLayout.setSmoothScrollingEnabled(true);
        mImageDetailsLayout.setScrollViewListener(this);

        /** Adjusting padding top for the Like layout so that is sticks to bottom of screen**/
        mLikeLayoutPaddingTop = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);

        mHalfScreenSize = (int)(mHalfScreenPer *mDeviceHeightInPx);
        Log.i(TAG,"mHalfScreenSize::"+mHalfScreenSize);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        relativeParams.setMargins(0, (int)mLikeLayoutPaddingTop, 0, 0);
        mTopLikeLayout.setLayoutParams(relativeParams);
        mTopLikeLayout  .requestLayout();
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

    /**
     * Binding story data
     */
    private void bindData() {
        if (mContentData == null) {
            return;
        }

        final ContentData cd = mContentData;
        Picasso.with(mContext).load(mContentData.pictureUrl).fit().into(mImageMain);
        mTextTitle.setText(cd.name);
        mTextPlace.setText(cd.place);
        String name = (!(cd.personDetails.name.equals("")))? "By "+cd.personDetails.name : "";
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
        TextView mTextDate = (TextView) mStoryLayout.findViewById(R.id.txtDate);
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
            mTextDate.setText(cd.date);
            mTextStoryDetails.setText(cd.pictureDescription);
        }

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(Uri.parse(cd.pictureUrl));
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Share_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"")
                        .build());
            }
        });
        mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_AddComment_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"")
                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Comment_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"")
                        .build());

                boolean showBackArrow = true;
                mListener.onAnimateMenuIcon(showBackArrow);

                buildCommentsLayout(cd);
                attachPixtoryContent(SHOW_PIC_COMMENTS);
            }
        });

    }

    /**
     * @param story_or_comment
     * Rationale - Adding story_view_layout fragment to StoryFragment
     */
    public void attachPixtoryContent(int story_or_comment){
        mStoryParentLayout.removeAllViews();
        if(story_or_comment == SHOW_PIC_STORY){
            mStoryParentLayout.addView(mStoryLayout);
            setCommentsVisible(false);
        }else{
            mStoryParentLayout.addView(mCommentsLayout);
            setCommentsVisible(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnStoryFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStoryFragmentInteractionListener");
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
    public interface OnStoryFragmentInteractionListener {
        void onAnimateMenuIcon(boolean showBackArrow);
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
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    //Toast.makeText(mContext,"Long tap detected",Toast.LENGTH_SHORT).show();
                    if(isFullScreenShown){
                        showWallpaperAlert();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Wallpaper_LongPress")
                                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                .put("PIXTORY_ID",mContentData.id+"")
                                .put("POSITION_ID",mContentIndex+"")
                                .build());}
                }

//                @Override
//                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                    if (distanceX < 5 && distanceX > -5) {
//                        mIsScrolling = true;
//                        mIsFling = false;
//                        Log.i(TAG, "onScroll::::"+mImageDetailsLayout.getScrollY());
////
//
//                    }
//                    return super.onScroll(e1, e2, distanceX, distanceY);
//                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
//                    final int SWIPE_MIN_DISTANCE = 50;
//                    final int SWIPE_THRESHOLD_VELOCITY = 100;
//                    try {
//                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
//                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                            mIsFling = true;
//                            Log.i(TAG, "onFlingUp::::"+mImageDetailsLayout.getScrollY());
//
////                            modifyScreenHeight(mImageDetailsLayout.getScrollY());
//
//                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
//                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                            mIsFling = true;
//                            if(!isFullScreenShown){
//                                showFullScreen();
//                                isFullScreenShown = true;
//                                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PictureView")
//                                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
//                                        .put("PIXTORY_ID",""+mContentData.id)
//                                        .build());
//                            }
//
//                        } else {
//                            mIsFling = false;
//                        }
//                    } catch (Exception e) {
//                        // nothing
//                        e.printStackTrace();
//                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    private void navigateToUserProfile(ContentData cd){
        getActivity().onBackPressed();
    }

    private boolean modifyScreenHeight(int offset) {

        imgViewLayoutParams.height = (mImageExtendedHeight) -offset;
        mImageMain.setLayoutParams(imgViewLayoutParams);

        if(offset > mBottomScreenHt )
            mCommentShareLayout.setVisibility(View.VISIBLE);
        else
            mCommentShareLayout.setVisibility(View.GONE);

        return true;

    }

    int mHalfScreenSize;
    @OnTouch(R.id.image_details_layout)
    public boolean onTouchStory(ScrollView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
//            Log.i(TAG , "MotionEvent.ACTION_UP::"+isScrollingUp+"::scrollY::"+scrollY);

            isScrollingUp= (scrollY < oldScrollY)?false:true;

            if(scrollY < mHalfScreenSize){

                mImageDetailsLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isScrollingUp){
                            mImageDetailsLayout.smoothScrollTo(0,mHalfScreenSize);

                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_StoryView")
                                    .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                    .put("PIXTORY_ID",""+mContentData.id)
                                    .build());
                        }
                        else
                            mImageDetailsLayout.smoothScrollTo(0,0);
                    }
                });
            }
        }
        return false;
    }

    private void setUpFullScreen(){
        isFullScreenShown = true;
        mSlantView.setVisibility(View.VISIBLE);
        mImageDetailsLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.VISIBLE);
    }

    private void setUpHalfScreen(){
        mSlantView.setVisibility(View.VISIBLE);
        mStoryLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.GONE);

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
                    mSlantView.setVisibility(View.GONE);
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
        mCommentsRecyclerView.setHasFixedSize(true);

        setCommentsVisible(true);

        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                CommentsDialogFragment commentsDialogFragment = CommentsDialogFragment.newInstance("Some title");
                commentsDialogFragment.show(fm, "fragment_alert");
            }
        });

        NetworkApiHelper.getInstance().getCommentDetailList(Utils.getUserId(mContext), data.id, new NetworkApiCallback<GetCommentDetailsResponse>() {

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
     * Method to post a new comment
     * @param comment
     */
    public void postComment(String comment) {

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        int content_id = mContentData.id;

        if(!((Utils.getFbID(mContext)).equals(""))) {
            //User is allowed to comment only if loggedIn
            NetworkApiHelper.getInstance().addComment(Utils.getUserId(mContext), content_id, comment, new NetworkApiCallback<AddCommentResponse>() {

                @Override
                public void success(AddCommentResponse addCommentResponse, Response response) {
                    Log.i(TAG, "Add Comment Request Success");
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_SubmitComment_Click")
                            .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                            .put("PIXTORY_ID",mContentData.id+"")
                            .build());
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

    /**
     *
     * @param view
     * Implementation to like a story
     */
    @OnClick(R.id.image_like)
    public void onLikeClick(ImageView view) {
        if(Utils.isEmpty(Utils.getFbID(mContext))) {
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
            //TODO: Redirect user to login
            showLoginAlert();
            //Toast.makeText(mContext,"Please login",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendLikeToBackend(int contentId, boolean isLiked) {
        NetworkApiHelper.getInstance().likeContent(Utils.getUserId(getActivity()), contentId, isLiked, new NetworkApiCallback<BaseResponse>() {

            @Override
            public void success(BaseResponse baseResponse, Response response) {

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
            mListener.onAnimateMenuIcon(false);
            attachPixtoryContent(SHOW_PIC_STORY);
        }
    }

    public  boolean isFullScreenShown(){return isFullScreenShown;}

    private void showLoginAlert(){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.login_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);


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


}

