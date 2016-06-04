package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import butterknife.*;

import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.SlantView;
import com.squareup.picasso.Picasso;
import retrofit.RetrofitError;
import retrofit.client.Response;
import java.util.ArrayList;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class MainFragment extends Fragment{

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


    public static final String Vid_Reco_VideoExpand = "Vid_Reco_VideoExpand";

    private Context mContext;


    /**********************************************
     *
     **********************************************/

    @Bind(R.id.pic_story_layout)
    LinearLayout mStoryLayout = null;

    @Bind(R.id.image_main)
    ImageView mImageMain = null;

    @Bind(R.id.layout_image_details)
    LinearLayout mImageDetailsLayout = null;
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

    private Toolbar mToolBar;

    ScrollView mStoryContentScrollView = null;

    private int mSoftBarHeight = 0;

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
        bindData();
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
        if (this.isVisible()) {
            resetFragmentState();
        }
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

        public void onDetachStoryView(Fragment f, int pos);

        public void onAttachStoryView(Fragment f, int pos);
    }


    public void resetFragmentState() {

        mListener.onDetachStoryView(this, mContentIndex);
        mSlantView.setVisibility(View.GONE);
        mStoryLayout.setVisibility(View.GONE);
        mTextExpert.setVisibility(View.VISIBLE);
//        showFullScreen();
    }

    public void attachStoryView(View v) {
        mStoryLayout.setVisibility(View.VISIBLE);
        mStoryLayout.removeAllViews();
        mStoryLayout.addView(v);
    }

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
                            showHalfScreen();
                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            showFullScreen();
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
        int fromY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        int toY     = (int)0.70*mDeviceHeightInPx;
        animateContent(mImageDetailsLayout, true , fromY , toY);

        mListener.onAttachStoryView(MainFragment.this, mContentIndex);
        mSlantView.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.GONE);
    }

    private void showFullScreen() {
        int fromY     = (int)0.70*mDeviceHeightInPx;
        int toY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        animateContent(mImageDetailsLayout, false , fromY , toY);
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

                if(!showContent){
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

}
