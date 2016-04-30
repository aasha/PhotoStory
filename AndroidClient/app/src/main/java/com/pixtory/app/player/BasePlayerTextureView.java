package com.pixtory.app.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.util.Util;
import com.pixtory.app.R;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class BasePlayerTextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = BasePlayerTextureView.class.getName();

    // Instance name
    private String mInstanceName = null;

    // Context of activity
    private Context mContext = null;

    // Layout inflater
    private LayoutInflater mInflater = null;

    // Exo player
    private DemoPlayer mExoPlayer = null;
    private AspectRatioFrameLayout mVideoFrame = null;
    public TextureView mTextureView = null;

    // Listener for clients
    private DemoPlayer.Listener mPlayerStateListener = null;

    // Player STATE flags
    private boolean mPlayerNeedsPrepare;
    private long mPlayerPosition = 0;
    private boolean mIsVideoPlaying = false;

    private ImageView mReplayBtn = null;
    private View mTranslucentView = null;

    // Content uri associated with this player
    private Uri mContentUri = null;
    private SimpleDraweeView mYTPreviewPlayerView = null;

    public SimpleDraweeView getYTPreviewPlayerView() {
        return mYTPreviewPlayerView;
    }

    public BasePlayerTextureView(Context context, String instanceName) {
        mContext = context;
        mInstanceName = instanceName;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mVideoFrame = (AspectRatioFrameLayout) mInflater.inflate(R.layout.base_player_texture_view, null);

        mTextureView = (TextureView) mVideoFrame.findViewById(R.id.surface_view);

        mTextureView.setSurfaceTextureListener(this);

        mReplayBtn = (ImageView) mVideoFrame.findViewById(R.id.player_pause_icon);
        mTranslucentView = (View) mVideoFrame.findViewById(R.id.translucent_view);

        mYTPreviewPlayerView = (SimpleDraweeView) mVideoFrame.findViewById(R.id.yt_preview_player);

    }

    public void preparePlayer(boolean playWhenReady, boolean isStreaming) {
        if (mExoPlayer == null) {
            if (isStreaming == true)
                mExoPlayer = new DemoPlayer(getStreamingRendererBuilder());
            else
                mExoPlayer = new DemoPlayer(getRendererBuilder());
            if (null != mPlayerStateListener) {
                mExoPlayer.addListener(mPlayerStateListener);
            }
            mPlayerNeedsPrepare = true;
        }
        mExoPlayer.seekTo(mPlayerPosition);
        if (mPlayerNeedsPrepare) {
            mExoPlayer.prepare();
            mPlayerNeedsPrepare = false;
        }
        if (mTextureView.getSurfaceTexture() != null)
            mExoPlayer.setSurface(new Surface(mTextureView.getSurfaceTexture()));
        mExoPlayer.setPlayWhenReady(playWhenReady);
        mIsVideoPlaying = true;
    }

    int previousDeviceVolume = 100;
    private boolean isMuted = false;

    public void setMute(boolean toMute) {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (toMute == true) {
            isMuted = true;
            previousDeviceVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        } else {
            isMuted = false;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousDeviceVolume, AudioManager.FLAG_ALLOW_RINGER_MODES);

        }
    }

    public boolean isMuted() {
        return isMuted;
    }


    public void releasePlayer() {
        if (mExoPlayer != null) {
            mPlayerPosition = mExoPlayer.getCurrentPosition();
            mExoPlayer.release();
            mExoPlayer = null;
        }
        mIsVideoPlaying = false;
    }

    public long getPlayerPosition() {
        if (mExoPlayer != null)
            return mExoPlayer.getCurrentPosition();
        return mPlayerPosition;
    }

    protected DemoPlayer.RendererBuilder getStreamingRendererBuilder() {
        String userAgent = Util.getUserAgent(mContext, "ExoPlayerDemo");
        return new HlsRendererBuilder(mContext, userAgent, mContentUri.toString());
    }

    protected DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(mContext, "ExoPlayerDemo");
        return new ExtractorRendererBuilder(mContext, userAgent, mContentUri);
    }

    public AspectRatioFrameLayout getVideoFrame() {
        return mVideoFrame;
    }

    public void setPlayerPosition(int seekPosition) {
        mPlayerPosition = seekPosition;
    }

    public void setContentUri(Uri uri) {
        mContentUri = uri;
    }

    public void setPlayerListener(DemoPlayer.Listener mDemoListener) {
        mPlayerStateListener = mDemoListener;
    }

    public void setPlayerNeedsPrepare(boolean b) {
        mPlayerNeedsPrepare = b;
    }

    public void setPlayIconVisibility(int v) {
        mReplayBtn.setVisibility(v);
    }

    public void setPlayIconClickListener(View.OnClickListener cl) {
        mReplayBtn.setOnClickListener(cl);
    }

    public void setVideoOverlayClickListener(View.OnClickListener cl) {
        mTranslucentView.setOnClickListener(cl);
    }

    public void setVideoOverlayTouchListener(View.OnTouchListener cl) {
        mTranslucentView.setOnTouchListener(cl);
    }

    public void setYtPreviewPlayerView(Uri uri) {
        if (uri != null) {
            //Enabling progressive download
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setProgressiveRenderingEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(mYTPreviewPlayerView.getController())
                    .build();
            mYTPreviewPlayerView.setController(controller);
        } else {
            mYTPreviewPlayerView.setVisibility(View.GONE);
        }
    }

    public void setYtPreviewVisibility(boolean isVisible) {
        mYTPreviewPlayerView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setYtPreviewAlpha(float alpha) {
        mYTPreviewPlayerView.setAlpha(alpha);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mExoPlayer != null)
            mExoPlayer.setSurface(new Surface(mTextureView.getSurfaceTexture()));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
