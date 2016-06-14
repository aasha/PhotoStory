package com.pixtory.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.views.SlantView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * Created by training3 on 09/06/2016 AD.
 */
public class TestActivity extends Activity {

    MainFragment mainFragment;
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
    ScrollView mImageDetailsLayout;

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

    int mDeviceWidthInPx,mDeviceHeightInPx;
    private static final int SHOW_PIC_STORY = 88;
    private static final int SHOW_PIC_COMMENTS = 89;



    @SuppressLint("NewApi")
    private int getSoftbuttonsbarHeight() {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            this.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    };

    float top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Device info
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;

        ButterKnife.bind(this);
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        mStoryLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_view_layout, null);
        mCommentsLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_comment_layout , null);
        bindData();

        top = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);

//        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ScrollView.LayoutParams relativeParams = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);


        relativeParams.setMargins(0, (int)top, 0, 0);
        mImageDetailBottomContainer.setLayoutParams(relativeParams);
        mImageDetailBottomContainer.requestLayout();

//        attachPixtoryContent(SHOW_PIC_STORY);


    }

    //    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_test);
//        mContext = this;
//
//        mSoftBarHeight = getSoftbuttonsbarHeight();
//
//        // Device info
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        mDeviceWidthInPx = displayMetrics.widthPixels;
//        mDeviceHeightInPx = displayMetrics.heightPixels;
//
//        mDeviceHeightInPx += mSoftBarHeight;
//        bindData();
//    }




    private void bindData() {

        mTextTitle.setText("Beautiful Picture");
        mTextPlace.setText("bangalore");
        mTextExpert.setText("sonali kakrayne");

        mLikeCountTV.setText(String.valueOf(2344));


        //***Binding StoryContent****/
        ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
        TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
        TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
        TextView mTextDate = (TextView) mStoryLayout.findViewById(R.id.txtDate);
        TextView mTextStoryDetails = (TextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
        LinearLayout mBtnShare = (LinearLayout) mStoryLayout.findViewById(R.id.btnShare);
        LinearLayout mBtnComment = (LinearLayout) mStoryLayout.findViewById(R.id.btnComment);

        String picUrl =  "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";


                Picasso.with(this).load(picUrl).fit().into(mProfileImage);
                mTextName.setText("sonali kakrayne");
                mTextDesc.setText("Travel Enthusiast");

            mTextDate.setText("12 April 2015");
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



    public boolean isCommentsVisible() {
        return isCommentsVisible;
    }

    public void setCommentsVisible(boolean commentsVisible) {
        isCommentsVisible = commentsVisible;
    }

    boolean isRecoViewAdded = false;
    boolean mIsScrolling = false;
    boolean mIsFling = false;
    int lastScrollPosition = 0;

    boolean isFirstScroll = true;

    GestureDetector gesture;


    final Handler timerHandler = new Handler();

    ViewGroup.LayoutParams layoutParams;
    @Override
    protected void onStart() {
        super.onStart();
        gesture = new GestureDetector(TestActivity.this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        if(isFirstScroll)
                            return true;

//
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
                                    if(isFirstScroll){
                                        isFirstScroll = false;
                                        showHalfScreen();
                                        isFullScreenShown = false;
                                    }
                                }

                            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                mIsFling = true;
                               int scroll=  mImageDetailBottomContainer.getScrollY();
//                                mImageDetailBottomContainer.setTranslationY(0);
                                if(!isFullScreenShown){
//                                    showFullScreen();
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
    }

    @OnTouch(R.id.image_main)
    public boolean onTouch(ImageView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
        }
        return false;
    }

    @OnTouch(R.id.image_details_layout)
    public boolean onTouchStory(ScrollView view, MotionEvent me) {
        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
        }
        return false;
    }


    private void showHalfScreen() {
//        setUpHalfScreen();

        int fromY   = mDeviceHeightInPx - (int)top;
        double toY     = 0.30*mDeviceHeightInPx;

        animateContent(mImageDetailBottomContainer, false , (int)top , -800);

    }

    private void showFullScreen() {
        int fromY     = (int)0.30*mDeviceHeightInPx;
        int toY   = mDeviceHeightInPx - mImageInfoLayoutHeight;
        animateContent(mImageDetailBottomContainer, true , fromY , toY);
        if(isCommentsVisible()){
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
    }

    private void animateContent(View view , final boolean showContent , int fromY , int toY){

        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(view ,"translationY" , fromY, toY);
        transAnimation.setDuration(800);//set duration
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


}
