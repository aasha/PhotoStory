package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.*;

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
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.SlantView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class MainFragment extends Fragment {

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
    LinearLayout mImageDetailBottomContainer = null;

    @Bind(R.id.image_details_layout)
    RelativeLayout mImageDetailsLayout;

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

    private int mSoftBarHeight = 0;
    private boolean isFullScreenShown = true;
    private boolean isCommentsVisible = false;

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

        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
        mDeviceHeightInPx += mSoftBarHeight;
        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
    }

    View mRootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        Log.d("TAG", "onCreateView is called for index = " + mContentIndex);
        ButterKnife.bind(this, mRootView);

        mImageInfoLayoutHeight = (int)getResources().getDimension(R.dimen.image_layout_height);
        Log.i("mImageInfoHeight is :::",""+mImageInfoLayoutHeight);
        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpStoryContent();
        bindData();
        attachPixtoryContent(SHOW_PIC_STORY);
        setUpFullScreen();
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
                mTextDesc.setText(cd.personDetails.desc);
                mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext,cd.personDetails.userId+"",Toast.LENGTH_SHORT).show();
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
            }
        });
        mBtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    boolean isRecoViewAdded = false;
    boolean mIsScrolling = false;
    boolean mIsFling = false;
    int lastScrollPosition = 0;

    final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
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
                            if(isFullScreenShown){
                                showHalfScreen();
                                isFullScreenShown = false;
                            }

                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            if(!isFullScreenShown){
                                showFullScreen();
                                isFullScreenShown = true;
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

    @OnTouch(R.id.image_main)
    public boolean onTouch(ImageView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
        }
        return false;
    }
    
    @OnTouch(R.id.pic_story_layout)
    public boolean onTouchStory(LinearLayout view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
        }
        return false;
    }


    private void showHalfScreen() {
        setUpHalfScreen();
        int fromY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        int toY     = (int)0.70*mDeviceHeightInPx;
        animateContent(mImageDetailBottomContainer, false , fromY , toY);

    }

    private void showFullScreen() {
        int fromY     = (int)0.30*mDeviceHeightInPx;
        int toY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        animateContent(mImageDetailBottomContainer, true , fromY , toY);
        if(isCommentsVisible()){
            mListener.onAnimateMenuIcon(false);
            attachPixtoryContent(SHOW_PIC_STORY);
        }
    }

    private void setUpFullScreen(){
        isFullScreenShown = true;
        mSlantView.setVisibility(View.GONE);
        mStoryLayout.setVisibility(View.GONE);
        mImageDetailsLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.VISIBLE);
    }

    private void setUpHalfScreen(){
        mSlantView.setVisibility(View.VISIBLE);
        mStoryLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.GONE);
    }

    private void animateContent(View view , final boolean showContent , int fromY , int toY){

        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(view ,"translationY" , fromY, toY);
        transAnimation.setDuration(500);//set duration
        transAnimation.start();//start animatio

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
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Like)
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .put(AppConstants.OPINION_ID, "" + mContentData.id)
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
            Toast.makeText(mContext,"Please login",Toast.LENGTH_SHORT).show();
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

}
