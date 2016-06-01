package com.pixtory.app.views;

import android.animation.ValueAnimator;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pixtory.app.R;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class TestActivity extends AppCompatActivity {

    private CollapsingToolbarLayout mCollapsibleToolBar;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    boolean isRecoViewAdded = false;
    boolean mIsScrolling = false;
    boolean mIsFling = false;
    int lastScrollPosition = 0;

    @Bind(R.id.img_view_rl)
    RelativeLayout mImageViewLayout;

    @Bind(R.id.content_ll)
    LinearLayout mContentLayout;

    @Bind(R.id.image)
    ImageView mImgView;

    private RelativeLayout mRootView;

    GestureDetector gesture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test2);
        ButterKnife.bind(this);

        // Device info
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;

          gesture = new GestureDetector(this,
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



    }



    private void setUpFullScreenUI() {
        isRecoViewAdded = false;
//        mListener.onDetachStoryView(this, mContentIndex);
//
//        mSlantView.setVisibility(View.INVISIBLE);
//        mStoryLayout.setVisibility(View.GONE);
//        mTextExpert.setVisibility(View.VISIBLE);
    }

    private void setUpHalfScreenUI() {
        isRecoViewAdded = true;
    }

    private void showFullScreen() {
        //mYTPreview.setAlpha(1.0f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mImageViewLayout.setLayoutParams(params);

        LinearLayout.LayoutParams recoScreenParams = (LinearLayout.LayoutParams) mContentLayout.getLayoutParams();
        recoScreenParams.height = 0;

        mContentLayout.setLayoutParams(recoScreenParams);
        final ValueAnimator mPad = ValueAnimator.ofInt(0, 0);
        mPad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mImgView.getLayoutParams();
                mImgView.setPadding(val, val, val, val);
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
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) mImageViewLayout.getLayoutParams();
        params.height = newHeight;
        params.width = mDeviceWidthInPx;

        mImageViewLayout.setLayoutParams(params);

        ViewGroup.LayoutParams recoScreenParams = (ViewGroup.LayoutParams) mContentLayout.getLayoutParams();
        recoScreenParams.height = mDeviceHeightInPx - newHeight;

        mContentLayout.setLayoutParams(recoScreenParams);
        if (isRecoViewAdded == false) {

            isRecoViewAdded = true;
        }
        return true;

    }

    @OnTouch(R.id.img_view_rl)
    public boolean onTouch(RelativeLayout view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            scrollScreen();
        }
        return false;
    }

    @OnTouch(R.id.content_ll)
    public boolean onTouchStory(LinearLayout view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            scrollScreen();
        }
        return false;
    }

    private void scrollScreen(){
        if (mIsScrolling && !mIsFling) {
            mIsScrolling = false;
            mIsFling = false;
            if (lastScrollPosition < (0.90 * mDeviceHeightInPx)) {
                showHalfScreen();

            } else {
                showFullScreen();

            }
        }
    }

}
