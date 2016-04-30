package com.pixtory.app.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.player.BasePlayerTextureView;
import com.pixtory.app.player.DemoPlayer;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class LastFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String MF_8thScreen_Play = "MF_8thScreen_Play";

    // TODO: Rename and change types of parameters
    private int mContentIndex;
    private String mParam2;

    private ContentData mContentData = null;
    private OnFragmentInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpertFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LastFragment newInstance(String param1, String param2) {
        LastFragment fragment = new LastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                mContentIndex = getArguments().getInt(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
                mContentData = App.getContentData().get(mContentIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = (View) inflater.inflate(R.layout.last_fragment, container, false);
        ButterKnife.bind(this, mRootView);
        mOpinionPlayer = new BasePlayerTextureView(getActivity(), "OPINION");
        mOpinionPlayer.setYtPreviewPlayerView(Uri.parse(mContentData.pictureUrl));
        mOpinionPlayer.setYtPreviewVisibility(false);
        mOpinionPlayerLayout.addView(mOpinionPlayer.getVideoFrame());
        mOpinionPlayer.setPlayerListener(mOpinionPlayerListener);
        Uri uri = null;
        uri = Uri.parse(mContentData.streamUrl);
        mOpinionPlayer.setContentUri(uri);
        mOpinionPlayer.preparePlayer(false, true);
        mOpinionPlayer.setVideoOverlayClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOpinionPlayer.getPlayerPosition() == 0) {
                    Uri uri = null;
                    uri = Uri.parse(mContentData.streamUrl);
                    mOpinionPlayer.setContentUri(uri);
                    mOpinionPlayer.preparePlayer(true, true);
                    mOpinionPlayer.setYtPreviewVisibility(false);
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(MF_8thScreen_Play)
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .build());
                }
                if (mOpinionPlayer.isMuted())
                    mOpinionPlayer.setMute(false);
                else
                    mOpinionPlayer.setMute(true);
            }
        });
        return mRootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mContentData == null)
            return;
        if (isVisibleToUser) {
            if (mOpinionPlayer != null) {
                mOpinionPlayer.setYtPreviewVisibility(false);
                mOpinionPlayer.setPlayerPosition(0);
                mOpinionPlayer.setMute(true);
                Uri uri = null;
                uri = Uri.parse(mContentData.streamUrl);
                mOpinionPlayer.setContentUri(uri);
                mOpinionPlayer.preparePlayer(true, true);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(MF_8thScreen_Play)
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .build());
            }
        }
        // Make sure that we are currently visible
        else if (this.isVisible()) {
            resetFragmentState();
            mOpinionPlayer.setMute(false);
        }
    }

    private void resetFragmentState() {
        if (mOpinionPlayer != null) {
            mOpinionPlayer.releasePlayer();
            mOpinionPlayer.setPlayerPosition(0);
            mOpinionPlayer.setYtPreviewVisibility(true);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private BasePlayerTextureView mOpinionPlayer = null;
    @Bind(R.id.video_layout)
    LinearLayout mOpinionPlayerLayout = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
        if (mOpinionPlayer != null) {
            mOpinionPlayer.releasePlayer();
        }
    }

    private DemoPlayer.Listener mOpinionPlayerListener = new DemoPlayer.Listener() {
        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            String text = "playWhenReady=" + playWhenReady + ", playbackState=";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    break;
                case ExoPlayer.STATE_ENDED:
                    mOpinionPlayer.releasePlayer();
                    mOpinionPlayer.setPlayerPosition(0);
                    mOpinionPlayer.setYtPreviewVisibility(true);
                    text += "ended";
                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    break;
                case ExoPlayer.STATE_PREPARING:
                    text += "preparing";
                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";
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

}
