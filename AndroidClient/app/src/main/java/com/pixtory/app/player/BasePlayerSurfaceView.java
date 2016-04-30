package com.pixtory.app.player;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.util.Util;
import com.squareup.picasso.Picasso;
import com.pixtory.app.R;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class BasePlayerSurfaceView{

    private static final String TAG = BasePlayerSurfaceView.class.getName();

    // Instance name
    private String mInstanceName = null;

    // Context of activity
    private Context mContext = null;

    // Layout inflater
    private LayoutInflater mInflater = null;

    // Exo player
    private DemoPlayer mExoPlayer = null;
    private AspectRatioFrameLayout mVideoFrame = null;
    public SurfaceView mSurfaceView = null;

    // Listener for clients
    private DemoPlayer.Listener mPlayerStateListener = null;

    // Player STATE flags
    private boolean mPlayerNeedsPrepare;
    private long mPlayerPosition = 3000;
    private boolean mIsVideoPlaying = false;

    private ImageView mReplayBtn = null;
    private View mTranslucentView = null;

    // Content uri associated with this player
    private Uri mContentUri = null;

    private ImageView mYTPreviewPlayer = null;


    public BasePlayerSurfaceView(Context context, String instanceName) {
        mContext = context;
        mInstanceName = instanceName;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mVideoFrame = (AspectRatioFrameLayout) mInflater.inflate(R.layout.base_player_surface_view, null);

        mSurfaceView = (SurfaceView) mVideoFrame.findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                holder.setFormat(PixelFormat.TRANSPARENT);
                if (mExoPlayer != null) {
                    mExoPlayer.setSurface(holder.getSurface());

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                holder.setFormat(PixelFormat.TRANSPARENT);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mExoPlayer != null) {
                    mExoPlayer.blockingClearSurface();
                }
            }
        });
        //mSurfaceView.setSurfaceTextureListener(this);

        mReplayBtn = (ImageView)mVideoFrame.findViewById(R.id.player_pause_icon);
        mTranslucentView = (View) mVideoFrame.findViewById(R.id.translucent_view);

        mYTPreviewPlayer = (ImageView) mVideoFrame.findViewById(R.id.yt_preview_player);

    }

    public void preparePlayer(boolean playWhenReady) {
        if (mExoPlayer == null) {
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
        mExoPlayer.setSurface(mSurfaceView.getHolder().getSurface());
        mExoPlayer.setPlayWhenReady(playWhenReady);
        mIsVideoPlaying = true;
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
        if(mExoPlayer != null)
            return mExoPlayer.getCurrentPosition();
        return mPlayerPosition;
    }

    public void restartPlayer() {
        if (mExoPlayer == null) {
            return;
        }
        releasePlayer();
        mPlayerPosition = 0;
        preparePlayer(true);
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

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        mSurfaceView.setZOrderMediaOverlay(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

    }

    //  public void setZOrderOnTop(boolean onTop) {
    //      mSurfaceView.setZOrderOnTop(onTop);
    //  }

    public void setPlayIconVisibility(int v){
        mReplayBtn.setVisibility(v);
    }
    public void setPlayIconClickListener(View.OnClickListener cl){
        mReplayBtn.setOnClickListener(cl);
    }

    public void setVideoOverlayClickListener(View.OnClickListener cl){
        mTranslucentView.setOnClickListener(cl);
    }

    public void setVideoOverlayTouchListener(View.OnTouchListener cl){
        mTranslucentView.setOnTouchListener(cl);
    }
    public void setYtPreviewPlayer(Uri uri){
        if(uri != null) {
            Picasso.with(mContext).load(uri).fit().into(mYTPreviewPlayer);
        }else{
            mYTPreviewPlayer.setVisibility(View.GONE);
        }
    }

    public void setYtPreviewVisibility(boolean isVisible){
        mYTPreviewPlayer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
