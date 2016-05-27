package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.*;
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
    private static final String Vid_Tap_Share = "Vid_Tap_Share";
    private static final String Vid_Tap_SeekClick = "Vid_Tap_SeekClick";
    private static final String Vid_Tap_Play = "Vid_Tap_Play";
    private static final String Vid_Tap_Pause = "Vid_Tap_Pause";
    private static final String Vid_Reco_Tap = "Vid_Reco_Tap";
    private static final String Vid_Complete_Q1 = "Vid_Complete_Q1";
    private static final String Vid_Complete_Q2 = "Vid_Complete_Q2";
    private static final String Vid_Complete_Q3 = "Vid_Complete_Q3";
    private static final String Vid_Complete_Q4 = "Vid_Complete_Q4";
    private static final String PDP_ProdCard_Close = "PDP_ProdCard_Close";
    private static final String MF_Video_Play = "MF_Video_Play";
    private static final String Video_Play_Time = "Video_Play_Time";
    private static final String Video_Buffering_Unit = "Video_Buffering_Unit";
    private static final String Video_Buffering_Complete = "Video_Buffering_Complete";

    public static final String Vid_Reco_VideoExpand = "Vid_Reco_VideoExpand";
    private static final String PDP_Card_Bkmrk = "PDP_Card_Bkmrk";
    private static final String PDP_Card_UnBkmrk = "PDP_Card_UnBkmrk";
    private static final String PLAY_POSITION = "PLAY_POSITION";
    private static final String BUFFER_LENGTH = "BUFFER_LENGTH";
    private boolean isFirstQuartileEventSent = false;
    private boolean isSecondQuartileEventSent = false;
    private boolean isThirdQuartileEventSent = false;


    /**********************************************
     *
     **********************************************/

    @Bind(R.id.pic_story_layout)
    LinearLayout mStoryLayout = null;

    @Bind(R.id.main_layout)
    FrameLayout mMainLayout = null;

    @Bind(R.id.image_main)
    ImageView mImageMain = null;

    @Bind(R.id.layout_image_details)
    RelativeLayout mImageDetailsLayout = null;

    @Bind(R.id.text_title)
    TextView mTextTitle = null;

    @Bind(R.id.text_place)
    TextView mTextPlace = null;

    @Bind(R.id.text_expert)
    TextView mTextExpert = null;

    @Bind(R.id.image_like)
    ImageView mImageLike = null;

    @Bind(R.id.slant_view)
    SlantView mSlantView = null;

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
        applyFonts();
        return mRootView;
    }

    private void applyFonts() {
        Dekar.applyFont(this.getActivity(), mTextPlace);
        Dekar.applyFont(this.getActivity(), mTextTitle);
        Dekar.applyFont(this.getActivity(), mTextExpert);
    }

    private void bindData() {
        if (mContentData == null) {
            return;
        }
        final ContentData cd = mContentData;
        Picasso.with(this.getContext()).load(mContentData.pictureUrl).fit().into(mImageMain);
        mTextTitle.setText(cd.name);
        mTextPlace.setText(cd.place);
        mTextExpert.setText("By " + cd.personDetails.name);
        if (mContentData.likedByUser == true)
            mImageLike.setImageResource(R.drawable.liked);
        else
            mImageLike.setImageResource(R.drawable.like);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindData();
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
        isRecoViewAdded = false;
        mListener.onDetachStoryView(this, mContentIndex);
        mSlantView.setVisibility(View.GONE);
        mStoryLayout.setVisibility(View.GONE);
        showFullScreen();
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
                    if (distanceX < 5 && distanceX > -5) {
                        mIsScrolling = true;
                        mIsFling = false;
                        modifyScreenHeight((int) e2.getRawY());
                        lastScrollPosition = (int) e2.getRawY();
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
            if (mIsScrolling && !mIsFling) {
                mIsScrolling = false;
                mIsFling = false;
                if (lastScrollPosition < (0.90 * mDeviceHeightInPx)) {
                    showHalfScreen();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_Tap)
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("META", "Swipe")
                            .put(AppConstants.OPINION_ID, "" + mContentData.id)
                            .build());
                } else {
                    showFullScreen();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_VideoExpand)
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put(AppConstants.OPINION_ID, "" + mContentData.id)
                            .build());
                }
            }
        }
        return false;
    }


    private void setUpFullScreenUI() {
        isRecoViewAdded = false;
        mListener.onDetachStoryView(this, mContentIndex);
        mSlantView.setVisibility(View.GONE);
        mStoryLayout.setVisibility(View.GONE);
    }

    private void setUpHalfScreenUI() {
        isRecoViewAdded = true;
    }

    private void showFullScreen() {
        //mYTPreview.setAlpha(1.0f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMainLayout.setLayoutParams(params);
        ViewGroup.LayoutParams recoScreenParams = (ViewGroup.LayoutParams) mStoryLayout.getLayoutParams();
        recoScreenParams.height = 0;
        mStoryLayout.setLayoutParams(recoScreenParams);
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 0);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mImageMain.getLayoutParams();
                mImageMain.setPadding(val, val, val, val);
            }
        });
        mPad.start();
        setUpFullScreenUI();
    }

    private void showHalfScreen() {
        modifyScreenHeight((int) (0.30 * mDeviceHeightInPx));
        setUpHalfScreenUI();
    }

    private boolean modifyScreenHeight(int newHeight) {
        //mYTPreview.setAlpha(0.0f);
        if (newHeight < (0.30 * mDeviceHeightInPx)) {
            return true;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMainLayout.getLayoutParams();
        params.height = newHeight;
        mMainLayout.setLayoutParams(params);
        ViewGroup.LayoutParams recoScreenParams = (ViewGroup.LayoutParams) mStoryLayout.getLayoutParams();
        recoScreenParams.height = mDeviceHeightInPx - newHeight;
        mStoryLayout.setLayoutParams(recoScreenParams);
        if (isRecoViewAdded == false) {
            mListener.onAttachStoryView(this, mContentIndex);
            mSlantView.setVisibility(View.VISIBLE);
            isRecoViewAdded = true;
        }
        return true;

    }

    public void scaleUpProdRecoView() {
        ValueAnimator mCon = ValueAnimator.ofInt(mDeviceHeightInPx, (int) (0.70 * mDeviceHeightInPx));
        if (isRecoViewAdded == false) {
            mListener.onAttachStoryView(this, mContentIndex);
            mSlantView.setVisibility(View.VISIBLE);
            isRecoViewAdded = true;
        }
        mCon.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mMainLayout.getLayoutParams();
                layoutParams.height = val;
                mMainLayout.setLayoutParams(layoutParams);
            }
        });
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 30);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mImageMain.getLayoutParams();
                mImageMain.setPadding(val, val, val, val);
            }
        });
        mPad.start();
        final ValueAnimator mRec = ValueAnimator.ofInt(0, (int) (0.30 * mDeviceHeightInPx));
        mRec.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mStoryLayout.getLayoutParams();
                layoutParams.height = val;
                mStoryLayout.setLayoutParams(layoutParams);
            }
        });
        ArrayList<ValueAnimator> arrayListObjectAnimators = new ArrayList<ValueAnimator>(); //ArrayList of ObjectAnimators
        arrayListObjectAnimators.add(mCon);
        ValueAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ValueAnimator[arrayListObjectAnimators.size()]);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(objectAnimators);
        animSetXY.setDuration(500);//1sec
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();
        mRec.setDuration(500);
        mRec.start();

        mRec.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setUpHalfScreenUI();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public void scaleDownProductRecoView() {
        ValueAnimator mCon = ValueAnimator.ofInt((int) (0.70 * mDeviceHeightInPx), mDeviceHeightInPx);
        mCon.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mMainLayout.getLayoutParams();
                layoutParams.height = val;
                mMainLayout.setLayoutParams(layoutParams);
            }
        });
        final ValueAnimator mRec = ValueAnimator.ofInt((int) (0.30 * mDeviceHeightInPx), 0);
        mRec.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mStoryLayout.getLayoutParams();
                layoutParams.height = val;
                mStoryLayout.setLayoutParams(layoutParams);
            }
        });
        ArrayList<ValueAnimator> arrayListObjectAnimators = new ArrayList<ValueAnimator>(); //ArrayList of ObjectAnimators
        arrayListObjectAnimators.add(mCon);
        ValueAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ValueAnimator[arrayListObjectAnimators.size()]);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(objectAnimators);
        animSetXY.setDuration(500);//1sec
        animSetXY.start();
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 0);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mImageMain.getLayoutParams();
                mImageMain.setPadding(val, val, val, val);
            }
        });
        mPad.start();
        mRec.setDuration(500);
        mRec.start();
        mRec.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //mYTPreview.setAlpha(1.0f);
                setUpFullScreenUI();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @OnClick(R.id.image_like)
    public void onLikeClick(ImageView view) {
        if (mContentData.likedByUser == false) {
            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.flip_in);
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
            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.flip_out);
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
