package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import butterknife.*;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.pixtory.app.FeedbackOverlay;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.Product;
import com.pixtory.app.player.BasePlayerTextureView;
import com.pixtory.app.player.DemoPlayer;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.BlurBuilder;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class MainFragment extends Fragment implements ProductDataBinder.OnBookmarkListener {

    private static final String TAG = MainFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private int mContentIndex;
    private String mContentJson;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    private boolean mIsControlsShown = false;
    private boolean mIsControlsInteracted = false;

    private boolean isBuffering = false;

    private int playerState = 0;
    private OnMainFragmentInteractionListener mListener;

    private static final String SCREEN_NAME = "Video";
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
     * Opinion & Profile Player
     **********************************************/

    BasePlayerTextureView mOpinionPlayer = null;


    /**********************************************
     *
     **********************************************/

    @Bind(R.id.layout_complete)
    FrameLayout mCompleteLayout = null;

    @Bind(R.id.player_opinion)
    LinearLayout mOpinionPlayerLayout = null;

    @Bind(R.id.progressBarBuffer)
    ProgressBar mProgressBar = null;

    private boolean mHasVideoPlayedAtleastOnce = false;

    @Bind(R.id.rec_prd_layout)
    LinearLayout mPrdRecLayout = null;

    @Bind(R.id.main_layout)
    FrameLayout mMainLayout = null;

    @Bind(R.id.prd_detail_layout)
    FrameLayout mProdDetailsLayout = null;

    @Bind(R.id.product_detail)
    RelativeLayout mPDPDetailLayout = null;

    @Bind(R.id.img_product_down_arrow)
    ImageView mFullScreenPDPDownArrow = null;

    @Bind(R.id.layout_cta)
    LinearLayout mCTALayout = null;

    @Bind(R.id.layout_seek)
    LinearLayout mLayoutSeek = null;

    @Bind(R.id.layout_controls_opinion)
    RelativeLayout mLayoutControlsOpinion = null;

    @Bind(R.id.layout_opinion)
    RelativeLayout mOpinionLayout = null;

    @Bind(R.id.text_Opinion)
    TextView mTextOpinion = null;

    @Bind(R.id.text_Views)
    TextView mTextNoOfViews = null;

    @Bind(R.id.img_like)
    ImageView mImageLike = null;

    @Bind(R.id.img_play)
    ImageView mImagePlay = null;

    @Bind(R.id.seekBar)
    SeekBar mSeekBar = null;

    @Bind(R.id.textStartLength)
    TextView mTextStartLength = null;

    @Bind(R.id.textMaxLength)
    TextView mTextMaxLength = null;

    @Bind(R.id.img_up_arrow)
    ImageView mImgShowReco = null;

    private int mSoftBarHeight = 0;


    private int mOpinionVideoState = OPINION_PLAYER_STATE_BEGIN;

    private static int prevPopUpSelection = -1;

    private static final int OPINION_PLAYER_STATE_BEGIN = 1;
    private static final int OPINION_PLAYER_STATE_PLAYING = 2;
    private static final int OPINION_PLAYER_STATE_PAUSED = 3;
    private static final int OPINION_PLAYER_STATE_END = 4;
    /**************************
     * Recommendation List
     *************************/
    private int mMaxContentLength = 60;
    ProductDataBinder mProductDataBinder;

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
        Dekar.applyFont(this.getActivity(), mTextNoOfViews);
        Dekar.applyFont(this.getActivity(), mTextOpinion);
    }

    private void bindData() {
        if (mContentData == null) {
            return;
        }
        final ContentData cd = mContentData;

        mMaxContentLength = cd.contentLength;
        mTextMaxLength.setText("00:" + mMaxContentLength);
        mSeekBar.setMax(mMaxContentLength);
        mTextNoOfViews.setText(cd.viewCount + " views");
        mTextOpinion.setText(cd.name);
        if (mContentData.likedByUser == true)
            mImageLike.setImageResource(R.drawable.liked);
        else
            mImageLike.setImageResource(R.drawable.like);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(0xFFCC094F, PorterDuff.Mode.SRC_IN));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextStartLength.setText("00:" + String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsControlsInteracted = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mOpinionPlayer.setPlayerPosition(seekBar.getProgress() * 1000);
                if (mIsOpinionVideoPlaying)
                    playOpinionVid(true);
                else
                    releaseOpinionVid();
                mIsControlsInteracted = false;
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_SeekClick)
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .put(AppConstants.OPINION_ID, "" + mContentData.id)
                        .build());
            }
        });
        // Set opinion player
        mOpinionPlayer = new BasePlayerTextureView(getActivity(), "OPINION");
        mOpinionPlayerLayout.addView(mOpinionPlayer.getVideoFrame());
        mOpinionPlayer.setPlayerListener(mOpinionPlayerListener);
        mImagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsOpinionVideoPlaying) {
                    releaseOpinionVid();
                    if (isFullscreenVideo == true) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Pause)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put("SOURCE", "Full Screen Video")
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                    } else {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Pause)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put("SOURCE", "Recomendation Page")
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                    }
                } else {
                    playOpinionVid(true);
                    if (isFullscreenVideo == true) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Play)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put("SOURCE", "Full Screen Video")
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                    } else {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Play)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put("SOURCE", "Recomendation Page")
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                    }
                }
            }
        });

        mOpinionPlayer.setVideoOverlayTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (videoOverlaygesture.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });
        Log.d(TAG, "Instantiating OPINION & PROFILE player for fragement position = " + mContentIndex);
        mProductDataBinder = new ProductDataBinder(getContext(), this);
        bindData();
        CreateBlurredImage task = new CreateBlurredImage();
        task.execute();
        initPlayerData();
    }

    private void initPlayerData() {
        Uri uri = null;
        try {
            playOpinionVid(false);
            String img = "http://www.sdpb.org/s/photogallery/img/no-image-available.jpg";
            Uri u;
            if (mContentData.pictureUrl == null)
                u = Uri.parse(img);
            else
                u = Uri.parse(mContentData.pictureUrl);
            mOpinionPlayer.setYtPreviewPlayerView(u);
            mOpinionPlayer.setYtPreviewVisibility(true);
        } catch (Exception e) {
            e.printStackTrace();
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

    // Dummy variable for testing .. replace it with proper logic post demo. might loose state
    private boolean mWasOpinionPlaying = false;

    @Override
    public void onPause() {
        super.onPause();
        if (mIsOpinionVideoPlaying) {
            mWasOpinionPlaying = true;
            mOpinionPlayer.releasePlayer();
            mIsOpinionVideoPlaying = false;
        } else {
            if (isFullscreenVideo == false)
                mOpinionPlayer.setYtPreviewVisibility(true);
            mWasOpinionPlaying = false;
        }
        Log.d(TAG, "{" + mContentIndex + "} " + mOpinionPlayer.getPlayerPosition() + " B | state " + mOpinionVideoState + "|" + mWasOpinionPlaying + " ||| was video playing = " + mIsOpinionVideoPlaying);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWasOpinionPlaying) {
            playOpinionVid(true);
            mIsOpinionVideoPlaying = true;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
        if (mOpinionPlayer != null) {
            mOpinionPlayer.releasePlayer();
            mOpinionPlayer.setYtPreviewVisibility(true);
        }
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

    @Override
    public void onBookMarked(int positionOfProduct, boolean value) {
        mListener.onPDPPageBookMarked(this, mContentIndex, positionOfProduct, value);
        if (value == true) {
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(PDP_Card_Bkmrk)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.PRODUCT_ID, "" + mContentData.productList.get(positionOfProduct).productId)
                    .build());
        } else {
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(PDP_Card_UnBkmrk)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.PRODUCT_ID, "" + mContentData.productList.get(positionOfProduct).productId)
                    .build());
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

        public void onDetachRecoView(Fragment f, int pos);

        public void onAttachRecoView(Fragment f, int pos);

        public void onPDPPageSelected(Fragment f, int position);

        public void onPDPPageBookMarked(Fragment f, int contentPositon, int positionOfProduct, boolean value);

    }
    private long bufferTime = 0;
    private long totalBufferTime = 0;
    private DemoPlayer.Listener mOpinionPlayerListener = new DemoPlayer.Listener() {
        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            String text = "playWhenReady=" + playWhenReady + ", playbackState=";
            playerState = playbackState;
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    isBuffering = true;
                    bufferTime = System.currentTimeMillis();
                    if (!mProgressBar.isShown()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    break;
                case ExoPlayer.STATE_ENDED:
                    isBuffering = false;
                    mOpinionPlayer.releasePlayer();
                    mOpinionPlayer.setPlayerPosition(0);
                    mSeekBar.setProgress(0);
                    handler.removeCallbacksAndMessages(null);
                    mIsOpinionVideoPlaying = false;
                    mOpinionVideoState = OPINION_PLAYER_STATE_END;
                    setUIForOpinionVideoStoppedState();
                    text += "ended";
                    if (mProgressBar.isShown()) {
                        mProgressBar.setVisibility(View.GONE);
                    }

                    reportCompleteVideoPlay();
                    if(totalBufferTime != 0){
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Video_Buffering_Complete)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .put(BUFFER_LENGTH, "" + String.valueOf(totalBufferTime))
                                .build());
                    }
                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";
                    isBuffering = false;
                    if (mProgressBar.isShown()) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                    if(bufferTime != 0) {
                        bufferTime = System.currentTimeMillis() - bufferTime;
                        Log.e("AASHA", "Buffer time" + bufferTime);
                        totalBufferTime += bufferTime;
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Video_Buffering_Unit)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .put(BUFFER_LENGTH, "" + String.valueOf(bufferTime))
                                .build());
                        bufferTime = 0;
                    }
                    break;
                default:
                    text += "unknown";
                    break;
            }
        }

        @Override
        public void onError(Exception e) {
            if (e instanceof UnsupportedDrmException) {
                // Special case DRM failures.
                UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
                int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                        : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                        ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            }
            mOpinionPlayer.setPlayerNeedsPrepare(true);
        }


        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
            //shutterView.setVisibility(View.GONE);
            mOpinionPlayer.getVideoFrame().setAspectRatio(
                    height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
        }
    };

    private boolean mIsOpinionVideoPlaying = false;
    final Handler handler = new Handler();
    Runnable r;

    private void playOpinionVid(boolean playWhenReady) {
        mImagePlay.setImageResource(R.drawable.pause);
        mOpinionPlayer.mTextureView.setVisibility(View.VISIBLE);
        Uri uri;
            uri = Uri.parse("http://ono.inmobicdn.net/test/delivery/40/b6/40b61271-6b2a-41c0-9d5d-82aad138b789/testVideo708ad58f-735b-336d-2b70-8eae51c9c481.m3u8");
            mOpinionPlayer.setContentUri(uri);
            mOpinionPlayer.preparePlayer(playWhenReady, true);
        if(playWhenReady == false)
            return;
        if (!mHasVideoPlayedAtleastOnce) {
            mHasVideoPlayedAtleastOnce = true;
            setUIForVideoStart();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(MF_Video_Play)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.OPINION_ID, "" + mContentData.id)
                    .build());
        }
        mIsOpinionVideoPlaying = true;
        mOpinionVideoState = OPINION_PLAYER_STATE_PLAYING;
        //Set timer to update seek bar

        r = new Runnable() {
            @Override
            public void run() {
                if (mOpinionPlayer != null) {
                    int currentPosition = (int) mOpinionPlayer.getPlayerPosition() / 1000;
                    mSeekBar.setProgress(currentPosition);
                    mTextStartLength.setText("00:" + String.valueOf(currentPosition));
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Video_Play_Time)
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put(AppConstants.OPINION_ID, "" + mContentData.id)
                            .put(PLAY_POSITION, "" + String.valueOf(currentPosition))
                            .build());
                    if (currentPosition >= mMaxContentLength * 3 / 4 && (isThirdQuartileEventSent == false)) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Complete_Q3)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                        isThirdQuartileEventSent = true;
                    } else if (currentPosition >= mMaxContentLength / 2 && (isSecondQuartileEventSent == false)) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Complete_Q2)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                        isSecondQuartileEventSent = true;
                    } else if (currentPosition >= mMaxContentLength / 4 && (isFirstQuartileEventSent == false)) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Complete_Q1)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                        isFirstQuartileEventSent = true;
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(r, 1000);
        //mExpertOverlay.setVisibility(View.GONE);
    }

    private void releaseOpinionVid() {
        mImagePlay.setImageResource(R.drawable.play);
        mOpinionPlayer.releasePlayer();
        mIsOpinionVideoPlaying = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void setUIForVideoStart() {
        if (isFullscreenVideo == true) {
            final Animation animationFadeIn = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fade_in);
            animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mImgShowReco.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showControlsAndDismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mImgShowReco.startAnimation(animationFadeIn);
        }
    }

    private void showControls() {
        final Animation animationFadeIn = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fade_in);
        animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                showAllControls();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLayoutControlsOpinion.startAnimation(animationFadeIn);
        mIsControlsShown = true;
    }

    private void hideControls() {
        final Animation animationFadeOut = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fade_out);
        mLayoutControlsOpinion.startAnimation(animationFadeOut);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideAllControls();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIsControlsShown = false;
    }

    private void hideAllControls() {
        if (isBuffering == true)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mProgressBar.setVisibility(View.GONE);
        mLayoutControlsOpinion.setBackgroundResource(R.drawable.transparent_gradient);
        mOpinionLayout.setVisibility(View.INVISIBLE);
        mImagePlay.setVisibility(View.INVISIBLE);
        mCTALayout.setVisibility(View.INVISIBLE);
        mLayoutSeek.setVisibility(View.INVISIBLE);
    }

    private void showAllControls() {
        mLayoutControlsOpinion.setVisibility(View.VISIBLE);
        mLayoutControlsOpinion.setBackgroundResource(R.drawable.gray_gradient);
        if (isBuffering == true)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mProgressBar.setVisibility(View.GONE);
        if (isFullscreenVideo == true)
            mOpinionLayout.setVisibility(View.VISIBLE);
        mImagePlay.setVisibility(View.VISIBLE);
        mCTALayout.setVisibility(View.VISIBLE);
        mLayoutSeek.setVisibility(View.VISIBLE);
    }


    private void setUIForOpinionVideoStoppedState() {
        mHasVideoPlayedAtleastOnce = false;
        mOpinionPlayer.mTextureView.setVisibility(View.GONE);
        mOpinionPlayer.setYtPreviewAlpha(1.0f);
        mOpinionPlayer.setYtPreviewVisibility(true);
        mProgressBar.setVisibility(View.GONE);
        if (isFullscreenVideo == true) {
            //mOpinionPlayer.setYtPreviewAlpha(1.0f);
            //mExpertOverlay.setVisibility(View.VISIBLE);
            mImgShowReco.setVisibility(View.GONE);
        }
        mLayoutControlsOpinion.setVisibility(View.GONE);
        mIsControlsShown = false;
    }

    public void resetFragmentState() {
        isRecoViewAdded = false;
        mFullScreenPDPDownArrow.setVisibility(View.GONE);
        mOpinionPlayerLayout.setVisibility(View.VISIBLE);
        mProdDetailsLayout.setVisibility(View.GONE);
        mListener.onDetachRecoView(this, mContentIndex);
        if (mOpinionPlayer != null) {
            mOpinionPlayer.releasePlayer();
            mOpinionPlayer.setPlayerPosition(0);
        }
        mIsOpinionVideoPlaying = false;
        mOpinionVideoState = OPINION_PLAYER_STATE_BEGIN;
        setUIForOpinionVideoStoppedState();
        showFullScreen();
        handler.removeCallbacksAndMessages(null);
        if(totalBufferTime != 0){
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Video_Buffering_Complete)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.OPINION_ID, "" + mContentData.id)
                    .put(BUFFER_LENGTH, "" + String.valueOf(totalBufferTime))
                    .build());
        }
    }

    public void attachRecycerView(View v) {
        mPrdRecLayout.removeAllViews();
        mPrdRecLayout.addView(v);
    }

    private void share(Uri filepath) {
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Share)
                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                .build());
        //Check if whats com.pixtory.app is installed
        try {
            getActivity().getPackageManager().getApplicationInfo("com.whatsapp", 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "Whats com.pixtory.app not installed", Toast.LENGTH_LONG).show();
            return;
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mContentData.pictureUrl);
        shareIntent.setType("text/plain");
        //shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("https://s3-ap-southeast-1.amazonaws.com/inmobi-mcom/expert_videos/10000.mp4"));
        //shareIntent.putExtra(Intent.EXTRA_STREAM,filepath);
        //shareIntent.setType("*/*");
        shareIntent.setPackage("com.whatsapp");
        startActivity(shareIntent);
    }

    boolean isRecoViewAdded = false;
    boolean isFullscreenVideo = true;
    boolean mIsScrolling = false;
    boolean mIsFling = false;
    int lastScrollPosition = 0;


    final GestureDetector videoOverlaygesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    FeedbackOverlay.drawFullscreenOverlay(getContext());
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    mOpinionPlayer.setYtPreviewVisibility(false);
                    if (mIsControlsShown == true) {
                        hideAllControls();
                        mIsControlsShown = false;
                        mHandler.removeCallbacksAndMessages(null);
                    } else {
                        if (mHasVideoPlayedAtleastOnce == true) {
                            showControlsAndDismiss();
                        } else {
                            playOpinionVid(true);
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Tap_Play)
                                    .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                    .put("SOURCE", "Full Screen Video")
                                    .build());
                        }
                    }
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    final int SWIPE_MIN_DISTANCE = 50;
                    final int SWIPE_THRESHOLD_VELOCITY = 150;
                    try {
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            return false;
                        }
                        if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            if (isFullscreenVideo == false) {
                                showFullScreen();
                                isFullscreenVideo = true;
                                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_VideoExpand)
                                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                        .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                        .build());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // nothing
                    }
                    return false;
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
            });

    private class CreateBlurredImage extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap image) {
            try {
                Bitmap bluuredImage = BlurBuilder.blur(getActivity(), image);
                mCompleteLayout.setBackground(new BitmapDrawable(getResources(), bluuredImage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL url = new URL(mContentData.pictureUrl);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return image;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (isFullscreenVideo == true) {
                        scaleUpProdRecoView();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_Tap)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .put("META", "Tap")
                                .build());
                    } else {
                        scaleDownProductRecoView();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_VideoExpand)
                                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                .build());
                    }
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                    if (isFullscreenVideo == true && mExpertOverlay.getVisibility() == View.VISIBLE) {
//                        return super.onScroll(e1, e2, distanceX, distanceY);
//                    }
                    if (isFullscreenVideo == true && mIsOpinionVideoPlaying == false) {
                        return super.onScroll(e1, e2, distanceX, distanceY);
                    }
                    if (distanceX < 5 && distanceX > -5) {
                        mIsScrolling = true;
                        mIsFling = false;
                        mIsControlsInteracted = true;
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
//                        if (isFullscreenVideo == true && mExpertOverlay.getVisibility() == View.VISIBLE) {
//                            return true;
//                        }
                        if (isFullscreenVideo == true && mIsOpinionVideoPlaying == false) {
                            return true;
                        }
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            mIsControlsInteracted = true;
                            showHalfScreen();
                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            mIsFling = true;
                            mIsControlsInteracted = true;
                            showFullScreen();
                            if (mIsOpinionVideoPlaying == false && playerState == ExoPlayer.STATE_ENDED)
                                setUIForOpinionVideoStoppedState();
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Reco_VideoExpand)
                                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                                    .put(AppConstants.OPINION_ID, "" + mContentData.id)
                                    .build());
                        } else {
                            mIsFling = false;
                            mIsControlsInteracted = false;
                        }
                    } catch (Exception e) {
                        // nothing
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    @OnTouch(R.id.img_up_arrow)
    public boolean onTouch(ImageView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            mIsControlsInteracted = false;
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

    final Handler mHandler = new Handler();

    private void showControlsAndDismiss() {
        //Set timer to hide controls
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mIsOpinionVideoPlaying == false && playerState != ExoPlayer.STATE_ENDED) {
                    mHandler.postDelayed(this, 4000);
                    return;
                }
                if (mIsControlsInteracted == true) {
                    mIsControlsInteracted = false;
                    mHandler.postDelayed(this, 4000);
                    return;
                }
                if (mIsControlsShown == true) {
                    hideControls();
                }
            }
        };
        showControls();
        mHandler.postDelayed(runnable, 4000);
    }

    public void showFullScreenPDP(int position) {
        hideAllControls();
        mProgressBar.setVisibility(View.GONE);
        mIsControlsShown = false;
        releaseOpinionVid();
        mOpinionPlayerLayout.setVisibility(View.GONE);
        int width = mOpinionPlayer.getVideoFrame().getWidth();
        int height = mOpinionPlayer.getVideoFrame().getHeight();
        ViewGroup.LayoutParams params = mProdDetailsLayout.getLayoutParams();
        params.width = width;
        params.height = height;
        mProdDetailsLayout.setLayoutParams(params);
        mImgShowReco.setVisibility(View.GONE);
        mProdDetailsLayout.setVisibility(View.VISIBLE);
        mFullScreenPDPDownArrow.setVisibility(View.VISIBLE);
        mProductDataBinder.bindData(mContentData.productList.get(position), mPDPDetailLayout, position);
    }

    public void bookMarkInPDP(int position, boolean value) {
        ContentData cd = App.getContentData().get(mContentIndex);
        List<Product> products = cd.productList;
        products.get(position).bookmarkedByUser = value;
        mProductDataBinder.setBookMarked(products.get(position));
        NetworkApiHelper.getInstance().addComment(Utils.getUserId(getActivity()), products.get(position).productId, value, new NetworkApiCallback<AddCommentResponse>() {
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
        //mProductDetailsPager.setPageMargin(width/3);
    }

    public void hideFullScreenPDP() {
        mFullScreenPDPDownArrow.setVisibility(View.GONE);
        mOpinionPlayerLayout.setVisibility(View.VISIBLE);
        showControlsAndDismiss();
        mProdDetailsLayout.setVisibility(View.GONE);
        mImgShowReco.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.img_product_down_arrow)
    public void onTouchImage(View view) {
        hideFullScreenPDP();
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(PDP_ProdCard_Close)
                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                .build());
    }

    private void setUpFullScreenUI() {
        isRecoViewAdded = false;
        mListener.onDetachRecoView(this, mContentIndex);
        isFullscreenVideo = true;
        mLayoutControlsOpinion.setVisibility(View.GONE);
        mIsControlsShown = false;
        mImgShowReco.setImageResource(R.drawable.up);
    }

    private void setUpHalfScreenUI() {
        mOpinionLayout.setVisibility(View.GONE);
        mImgShowReco.setImageResource(R.drawable.down);
        isRecoViewAdded = true;
        isFullscreenVideo = false;
    }

    private void showFullScreen() {
        //mYTPreview.setAlpha(1.0f);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMainLayout.setLayoutParams(params);
        ViewGroup.LayoutParams recoScreenParams = (ViewGroup.LayoutParams) mPrdRecLayout.getLayoutParams();
        recoScreenParams.height = 0;
        mPrdRecLayout.setLayoutParams(recoScreenParams);
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 0);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mOpinionPlayerLayout.getLayoutParams();
                mOpinionPlayerLayout.setPadding(val, val, val, val);
            }
        });
        mPad.start();
        setUpFullScreenUI();
    }

    private void showHalfScreen() {
        //mYTPreview.setAlpha(0.0f);
        modifyScreenHeight((int) (0.70 * mDeviceHeightInPx));
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 30);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mOpinionPlayerLayout.getLayoutParams();

                mOpinionPlayerLayout.setPadding(val, val, val, val);
                //mRecLayout.setLayoutParams(layoutParams);
            }
        });
        mPad.start();
        setUpHalfScreenUI();
    }

    private boolean modifyScreenHeight(int newHeight) {
        //mYTPreview.setAlpha(0.0f);
        if (newHeight < (0.70 * mDeviceHeightInPx)) {
            isFullscreenVideo = false;
            return true;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMainLayout.getLayoutParams();
        params.height = newHeight;
        mMainLayout.setLayoutParams(params);
        ViewGroup.LayoutParams recoScreenParams = (ViewGroup.LayoutParams) mPrdRecLayout.getLayoutParams();
        recoScreenParams.height = mDeviceHeightInPx - newHeight;
        mPrdRecLayout.setLayoutParams(recoScreenParams);
        if (isRecoViewAdded == false) {
            mListener.onAttachRecoView(this, mContentIndex);
            isRecoViewAdded = true;
        }
        return true;

    }

    public void scaleUpProdRecoView() {
        //mYTPreview.setAlpha(0.0f);
        mOpinionLayout.setVisibility(View.GONE);
        ValueAnimator mCon = ValueAnimator.ofInt(mDeviceHeightInPx, (int) (0.70 * mDeviceHeightInPx));
        if (isRecoViewAdded == false) {
            mListener.onAttachRecoView(this, mContentIndex);
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
                ViewGroup.LayoutParams layoutParams = mOpinionPlayerLayout.getLayoutParams();
                mOpinionPlayerLayout.setPadding(val, val, val, val);
            }
        });
        mPad.start();
        final ValueAnimator mRec = ValueAnimator.ofInt(0, (int) (0.30 * mDeviceHeightInPx));
        mRec.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mPrdRecLayout.getLayoutParams();
                layoutParams.height = val;
                mPrdRecLayout.setLayoutParams(layoutParams);
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
                ViewGroup.LayoutParams layoutParams = mPrdRecLayout.getLayoutParams();
                layoutParams.height = val;
                mPrdRecLayout.setLayoutParams(layoutParams);
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
                ViewGroup.LayoutParams layoutParams = mOpinionPlayerLayout.getLayoutParams();
                mOpinionPlayerLayout.setPadding(val, val, val, val);
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
                if (mIsOpinionVideoPlaying == false && playerState == ExoPlayer.STATE_ENDED)
                    setUIForOpinionVideoStoppedState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @OnClick(R.id.img_share)
    public void onShareClick(ImageView view) {
        share(Uri.parse(mContentData.pictureUrl));
    }

    @OnClick(R.id.img_like)
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
        NetworkApiHelper.getInstance().likeContent(Utils.getUserId(getActivity()), contentId, isLiked, new NetworkApiCallback<LikeContentResponse>() {
            @Override
            public void success(LikeContentResponse likeContentResponse, Response response) {
            }

            @Override
            public void failure(LikeContentResponse error) {

            }

            @Override
            public void networkFailure(RetrofitError error) {
            }
        });
    }

    private void reportCompleteVideoPlay() {
        isFirstQuartileEventSent = false;
        isSecondQuartileEventSent = false;
        isThirdQuartileEventSent = false;
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Vid_Complete_Q4)
                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                .put(AppConstants.OPINION_ID, "" + mContentData.id)
                .build());
        List<ContentData> cdList = App.getContentData();
        ContentData cd = cdList.get(mContentIndex);
        cd.viewCount += 1;
        App.setContentData((ArrayList<ContentData>) cdList);
        mTextNoOfViews.setText(cd.viewCount + " views");
        NetworkApiHelper.getInstance().deleteComment(mContentData.id, new NetworkApiCallback() {
            @Override
            public void success(Object o, Response response) {

            }

            @Override
            public void failure(Object error) {

            }

            @Override
            public void networkFailure(RetrofitError error) {
            }
        });
    }


    @OnLongClick(R.id.main_layout)
    public boolean onLongClickMain(ViewGroup view) {
        FeedbackOverlay.drawFullscreenOverlay(getContext());
        return false;
    }

}
