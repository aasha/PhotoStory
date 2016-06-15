package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.*;

import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.adapters.CommentsListAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetCommentDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.ObservableScrollView;
import com.pixtory.app.views.ScrollViewListener;
import com.pixtory.app.views.SlantView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

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

    // TODO: Rename and change types of parameters
    private int mContentIndex;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    private OnMainFragmentInteractionListener mListener;

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
    RelativeLayout mCommentsLayout = null;

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

    private int mSoftBarHeight = 0;
    private boolean isFullScreenShown = true;
    private boolean isCommentsVisible = false;

    ViewGroup.LayoutParams imageViewLayoutParams;

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

    public static MainFragment newInstance(int idx) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, idx);
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
        mContext = getActivity();
        if (getArguments() != null) {
            try {
                mContentIndex = getArguments().getInt(ARG_PARAM1);
                mContentData = App.getContentData().get(mContentIndex);
                mCIDX = getArguments().getString(ARG_PARAM3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mSoftBarHeight = getSoftbuttonsbarHeight();

        // Device info
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;


        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 100, displayMetrics );
        newHt = mDeviceHeightInPx +px;

        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
        mDeviceHeightInPx += mSoftBarHeight;
        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
    }

    View mRootView = null;
    RelativeLayout.LayoutParams imgParams ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        Log.d("TAG", "onCreateView is called for index = " + mContentIndex);
        ButterKnife.bind(this, mRootView);

        mImageInfoLayoutHeight = (int)getResources().getDimension(R.dimen.image_layout_height);
        Log.i("mImageInfoHeight is :::",""+mImageInfoLayoutHeight);

        setUpStoryContent();
        bindData();

        attachPixtoryContent(SHOW_PIC_STORY);


        top = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);
        mHalfScreenSize = (int)(0.55f*mDeviceHeightInPx);
        Log.i(TAG,"mHalfScreenSize::"+mHalfScreenSize);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);

        relativeParams.setMargins(0, (int)top, 0, 0);
        mTopLikeLayout.setLayoutParams(relativeParams);
        mTopLikeLayout  .requestLayout();
        setUpFullScreen();

        mImageDetailsLayout.setSmoothScrollingEnabled(true);

        mImageDetailsLayout.setScrollViewListener(this);

        imageViewLayoutParams = mImageMain.getLayoutParams();
        imgParams = (RelativeLayout.LayoutParams) mImageMain.getLayoutParams();
        return mRootView;
    }




    float top;
    int px;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG,"OnActivityCreated");
        super.onActivityCreated(savedInstanceState);
//        setUpStoryContent();
//        bindData();
//
//        attachPixtoryContent(SHOW_PIC_STORY);
//
//        top = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);

    }


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
//        ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
//        TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
//        TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
//        TextView mTextDate = (TextView) mStoryLayout.findViewById(R.id.txtDate);
//        TextView mTextStoryDetails = (TextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
//        LinearLayout mBtnShare = (LinearLayout) mStoryLayout.findViewById(R.id.btnShare);
//        LinearLayout mBtnComment = (LinearLayout) mStoryLayout.findViewById(R.id.btnComment);

        ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
        TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
        TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
        TextView mTextDate = (TextView) mStoryLayout.findViewById(R.id.txtDate);
        TextView mTextStoryDetails = (TextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
        LinearLayout mBtnShare = (LinearLayout) mStoryLayout.findViewById(R.id.btnShare);
        LinearLayout mBtnComment = (LinearLayout) mStoryLayout.findViewById(R.id.btnComment);

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
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Profile_Click")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID",cd.id+"")
                                .put("POSITION_ID",mContentIndex+"")
                                .build());
                        Intent intent = new Intent(mContext, UserProfileActivity.class);
                        intent.putExtra("USER_ID",Utils.getUserId(mContext));
                        intent.putExtra("PERSON_ID",cd.personDetails.id+"");
                        startActivity(intent);
                    }
                });
                mTextName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Profile_Click")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID",cd.id+"")
                                .put("POSITION_ID",mContentIndex+"")
                                .build());
                        Intent intent = new Intent(mContext, UserProfileActivity.class);
                        intent.putExtra("USER_ID",Utils.getUserId(mContext));
                        intent.putExtra("PERSON_ID",cd.personDetails.id+"");
                        startActivity(intent);
                    }

                });
                mTextDesc.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Profile_Click")
                                                             .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                                             .put("PIXTORY_ID",cd.id+"")
                                                             .put("POSITION_ID",mContentIndex+"")
                                                             .build());
                                                     Intent intent = new Intent(mContext, UserProfileActivity.class);
                                                     intent.putExtra("USER_ID",Utils.getUserId(mContext));
                                                     intent.putExtra("PERSON_ID",cd.personDetails.id+"");
                                                     startActivity(intent);
                                                 }
            });
            }
            Log.i(TAG,"bindStorycd data->date::"+cd.date);
            mTextDate.setText(cd.date);
            mTextStoryDetails.setText(cd.pictureDescription);
        }

        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(Uri.parse(cd.pictureUrl));
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Share_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"")
                        .build());
            }
        });
        mBtnComment.setOnClickListener(new View.OnClickListener() {
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
        mCommentsLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_comment_layout , null);
    }

    public boolean isCommentsVisible() {
        return isCommentsVisible;
    }

    public void setCommentsVisible(boolean commentsVisible) {
        isCommentsVisible = commentsVisible;
    }


    int  scrollY,oldScrollY;
    boolean scrollUp = true;
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
        void onAnimateMenuIcon(boolean showBackArrow);
    }

//
//    public void resetFragmentState() {
//        setUpFullScreen();
//    }
//
//    public void attachStoryView(View v) {
//        mStoryLayout.setVisibility(View.VISIBLE);
//        mStoryLayout.removeAllViews();
//        mStoryLayout.addView(v);
//    }
//
    /********Swipe Up and Down Logic*********************/
    boolean mIsScrolling = false;
    boolean mIsFling = false;

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

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (distanceX < 5 && distanceX > -5) {
                        mIsScrolling = true;
                        mIsFling = false;
                        Log.i(TAG, "onScroll::::"+mImageDetailsLayout.getScrollY());
//
//                        modifyScreenHeight(mImageDetailsLayout.getScrollY());

                    }
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    final int SWIPE_MIN_DISTANCE = 50;
                    final int SWIPE_THRESHOLD_VELOCITY = 100;
                    try {
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            Log.i(TAG, "onFlingUp::::"+mImageDetailsLayout.getScrollY());

//                            modifyScreenHeight(mImageDetailsLayout.getScrollY());

                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            if(!isFullScreenShown){
                                showFullScreen();
                                isFullScreenShown = true;
                                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("ST_Story_PictureView")
                                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                        .put("PIXTORY_ID",""+mContentData.id)
                                        .build());
                            }

                        } else {
                            mIsFling = false;
                        }
                    } catch (Exception e) {
                        // nothing
                        e.printStackTrace();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });



    int newHt;
    private boolean modifyScreenHeight(int offset) {

        imgParams.height = (newHt) -offset;
        mImageMain.setLayoutParams(imgParams);

        return true;

    }

//    @OnTouch(R.id.image_main)
//    public boolean onTouch(ImageView view, MotionEvent me) {
//        if (gesture.onTouchEvent(me)) {
//            return true;
//        }
//
////        if (me.getAction() == MotionEvent.ACTION_UP) {
////        }
//        return false;
//    }

    int mHalfScreenSize;
    @OnTouch(R.id.image_details_layout)
    public boolean onTouchStory(ScrollView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            Log.i(TAG , "MotionEvent.ACTION_UP::"+scrollUp+"::scrollY::"+scrollY);

            scrollUp= (scrollY < oldScrollY)?false:true;

            if(scrollY < mHalfScreenSize){

                mImageDetailsLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(scrollUp)
                            mImageDetailsLayout.smoothScrollTo(0,mHalfScreenSize);
                        else
                            mImageDetailsLayout.smoothScrollTo(0,0);
                    }
                });


            }


        }
        return false;
    }


    private void showHalfScreen(int lastScrollPos) {
        setUpHalfScreen();
//        modifyScreenHeight((int) (0.30 * mDeviceHeightInPx));

        int fromY = 0;
//        double toY = (-1)*0.50*mDeviceHeightInPx;
//        int toY = -200;
        float toY = 0.70f*mDeviceHeightInPx;
//        float scrollBy =  (mTopLikeLayout.getTop()) - toY;
//        mImageDetailsLayout.smoothScrollBy(0 , (int)scrollBy);
        Log.i(TAG,"smooth scrollTo="+toY);
        mImageDetailsLayout.smoothScrollTo(0,(int)toY);
        modifyScreenHeight((int)toY);
//        animateContent(mImageDetailBottomContainer, false , 0 ,  , 500);
//        animateContent(mImageMain, false ,fromY , -500 , 500);
    }

    private void showFullScreen() {
        int fromY     = (int)0.30*mDeviceHeightInPx;
        int toY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        animateContent(mImageDetailBottomContainer, true , -800 , 0 , 500);
        if(isCommentsVisible()){
            mListener.onAnimateMenuIcon(false);
            attachPixtoryContent(SHOW_PIC_STORY);
        }
    }

    private void setUpFullScreen(){
        isFullScreenShown = true;
        mSlantView.setVisibility(View.VISIBLE);
//        mStoryLayout.setVisibility(View.GONE);
        mImageDetailsLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.VISIBLE);
    }

    private void setUpHalfScreen(){
        mSlantView.setVisibility(View.VISIBLE);
        mStoryLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.GONE);
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Picture_StoryView")
                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                .put("PIXTORY_ID",""+mContentData.id)
                .build());
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
