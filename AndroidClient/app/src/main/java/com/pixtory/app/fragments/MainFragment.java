package com.pixtory.app.fragments;

import android.animation.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import butterknife.*;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.login.LoginManager;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.adapters.CommentsListAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.Category;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetCommentDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.ShareContentIdResponse;
import com.pixtory.app.userprofile.UserProfileActivity;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.DroidSerifTextView;
import com.pixtory.app.views.ObservableScrollView;
import com.pixtory.app.views.ScrollViewListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
    public static final String ARG_PARAM4 = "PROFILE_CONTENT";

    // TODO: Rename and change types of parameters
    private int mContentIndex;
    private boolean isProfileContent;

    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;

    private OnMainFragmentInteractionListener mListener;

    private static final String Vid_Tap_Unlike = "Vid_Tap_Unlike";
    private static final String Whatsapp_Package_name = "com.whatsapp";
    private static final String Fb_Package_name = "com.facebook.katana";
    private static final String Instagram_Package_name = "com.instagram.android";
    private static final String Gmail_Package_name = "com.google.android.gm";
    private static final String MF_Picture_StoryView = "MF_Picture_StoryView";
    private static final String ST_Story_PictureView = "ST_Story_PictureView";
    private static final String Like_Count = "Like_Count";
    private static final String Have_Commented = "Have_Commented";

    private String PreviousEvent;

    private static Animation mAnimation;

    Dialog dialog;

    private Context mContext;
    private Activity mActivity;

    @Bind(R.id.pic_story_layout)
    NestedScrollView mStoryParentLayout = null;

    @Bind(R.id.content_layout)
    LinearLayout mContentLayout =null;

    RelativeLayout mStoryLayout = null;
    LinearLayout mCommentsLayout = null;

    @Bind(R.id.picture_summary)
    TextView mPicSummary;

    @Bind(R.id.image_main)
    SimpleDraweeView mImageMain = null;

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

    @Bind(R.id.like_count)
    TextView mLikeCountTV = null;

    @Bind(R.id.text_expert)
    TextView mTextExpert = null;

    @Bind(R.id.like_layout)
    RelativeLayout mTopLikeLayout = null;

    @Bind(R.id.comment_share_ll)
    LinearLayout mCommentShareLayout;

    @Bind(R.id.btnComment)
    LinearLayout mCommentBtn = null;

    @Bind(R.id.btnShare)
    LinearLayout mShareBtn = null;

    @Bind(R.id.story_back_click)
    LinearLayout storyBackClick=null;

    @Bind(R.id.story_back_img)
    ImageView storyBackImg = null;

    @Bind(R.id.swipe_up_sign)
    TextView swipeUpSign = null;


    @Bind(R.id.wallpaper_settings)
    LinearLayout mWallpaperSettingsIcon;


    @Bind(R.id.loading_text)
    TextView mLoadingText;


    @Bind(R.id.postBtn_ll)
    LinearLayout mCommentPostBtnLayout;

    @Bind(R.id.share_img)
    ImageView mShareImg;

    @Bind(R.id.editor_pick_text)
    TextView mEditorPickText;

//    @Bind(R.id.loadingPanel)
//    RelativeLayout mLoadingPanel;

    ImageView swipeUpArrow;

    private boolean isSwipeUpArrowShown = false;

    ProgressDialog mProgressDialog;

    RelativeLayout.LayoutParams imgViewLayoutParams ;

    private int mSoftBarHeight = 0;
    private boolean isFullScreenShown = true;
    private boolean isCommentsVisible = false;
    private int mImageExtendedHeight;

    private float mLikeLayoutPaddingTop;
    private int mSlantViewHtInPx;
    private int mBottomScreenHt;
    final private float mHalfScreenPer = 0.9f;

    private int  scrollY,oldScrollY;
    private boolean isScrollingUp = true;
    private boolean isPixtorySwipeUp = true;

    private boolean isLongTap = false;


    private Bitmap mImageBitmap = null;

    private ImagePipeline frescoImagePipeline;

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

    public static MainFragment newInstance(int idx,boolean isProfileContent) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, idx);
        args.putBoolean(ARG_PARAM4,isProfileContent);
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

        Log.i(TAG,"callback->main fragment on create");
        mContext = mActivity =  getActivity();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Loading Pixstory For You...!!");
        mProgressDialog.setCanceledOnTouchOutside(false);

        dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.please_wait_alert);

//        mProgressDialog.show();
        if (getArguments() != null) {
            isProfileContent = getArguments().getBoolean(ARG_PARAM4);
            if(!isProfileContent){
                try {
                    mContentIndex = getArguments().getInt(ARG_PARAM1);
                    if(mListener.isCategoryViewOpen())
                        mContentData = App.getCategoryContentData().get(mContentIndex);
                    else
                        mContentData = App.getContentData().get(mContentIndex);
                    mCIDX = getArguments().getString(ARG_PARAM3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    mContentIndex = getArguments().getInt(ARG_PARAM1);
                    mContentData = App.getProfileContentData().get(mContentIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mSoftBarHeight = getSoftbuttonsbarHeight();

        // Device info
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;

        mSlantViewHtInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 100, displayMetrics );
        mImageExtendedHeight = mDeviceHeightInPx + ((int)(2.5f*mSlantViewHtInPx));
        mBottomScreenHt = (int)(0.60f*mDeviceHeightInPx);
//        mDeviceHeightInPx += mSoftBarHeight;

        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    View mRootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "onCreateView is called for index = " + mContentIndex);
        ButterKnife.bind(this, mRootView);

//        getView();

//        storyBackClick = (LinearLayout)mRootView.findViewById(R.id.story_back_click);
//        storyBackImg = (ImageView)mRootView.findViewById(R.id.story_back_img);
//        swipeUpSign = (TextView)mRootView.findViewById(R.id.swipe_up_sign);

        //Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.bounce);
        Animation upAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -0.03f);
        Animation downAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.03f);
        upAnimation.setStartOffset(500);
        upAnimation.setInterpolator(new LinearInterpolator());
           mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -0.03f);
        Animation upDown = AnimationUtils.loadAnimation(mContext,R.anim.up_down);

        //upDown.setStartOffset(5000);
        upDown.setInterpolator(new LinearInterpolator());
        upDown.setRepeatCount(Animation.INFINITE);
       /* AnimationSet mAnimation = new AnimationSet(true);
        mAnimation.addAnimation(upAnimation);
        mAnimation.addAnimation(downAnimation);*/
        //mAnimation.setStartOffset(500);
        mAnimation.setDuration(400);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());

        swipeUpArrow = (ImageView)mRootView.findViewById(R.id.swipe_up_arrow);
        isSwipeUpArrowShown = false;
        if(!isSwipeUpArrowShown){
            swipeUpArrow.setVisibility(View.VISIBLE);
            swipeUpArrow.setAnimation(mAnimation);
            isSwipeUpArrowShown=true;
        }


        if(!isProfileContent)
        {
            storyBackClick.setVisibility(View.GONE);
            storyBackImg.setVisibility(View.GONE);
        }
        storyBackClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_PixtoryBack_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",mContentData.id+"")
                        .build());
                getActivity().onBackPressed();
            }
        });

        storyBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_PixtoryBack_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",mContentData.id+"")
                        .build());
                getActivity().onBackPressed();
            }
        });

        mWallpaperSettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("MF_WpIcon_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",mContentData.id+"");

                sendAmplitudeLog(appEventBuilder);
                showPopUpMenu();
            }
        });

        mImageInfoLayoutHeight = (int)getResources().getDimension(R.dimen.image_layout_height);
        Log.i("mImageInfoHeight is :::",""+mImageInfoLayoutHeight);

        setUpStoryContent();
        bindData();

        mImageDetailsLayout.setSmoothScrollingEnabled(true);
//        mStoryParentLayout.setSmoothScrollingEnabled(true);
        mImageDetailsLayout.setScrollViewListener(this);

        /** Adjusting padding top for the Like layout so that is sticks to bottom of screen**/
        mLikeLayoutPaddingTop = mDeviceHeightInPx - getResources().getDimension(R.dimen.image_layout_height);

        mHalfScreenSize = (int)(mHalfScreenPer *mDeviceHeightInPx);
        Log.i(TAG,"mHalfScreenSize::"+mHalfScreenSize);

        if(mDeviceHeightInPx > 1280){
            mStoryParentLayout.getLayoutParams().height =  (int)(0.75f *mDeviceHeightInPx);
        }else {
            mStoryParentLayout.getLayoutParams().height =  (int)(0.72f *mDeviceHeightInPx);
        }

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        relativeParams.setMargins(0, (int)mLikeLayoutPaddingTop, 0, 0);
        mTopLikeLayout.setLayoutParams(relativeParams);
        mTopLikeLayout.requestLayout();
        mTopLikeLayout.bringToFront();

//        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)

//        mStoryParentLayout.setOnTouchListener(new NestedScrollView.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//
//                if(isCommentsVisible()){
//                    return false;
//                }
//
////                // Disallow the touch request for parent scroll on touch of child view
////                if (me.getAction() == MotionEvent.ACTION_UP) {
////                    Log.i(TAG,"min dist-"+(scrollY-oldScrollY));
////
////                    if((scrollY-oldScrollY) != 0){
////                        setUpHalfScreen();
////                    }
////                    return false;
////                }
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        // Disallow ScrollView to intercept touch events.
//                        v.getParent().requestDisallowInterceptTouchEvent(true);
//
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        // Allow ScrollView to intercept touch events.
//                        v.getParent().requestDisallowInterceptTouchEvent(false);
//                        Log.i(TAG,"min dist-"+(scrollY-oldScrollY));
//
//                        if((scrollY-oldScrollY) != 0){
//                            setUpHalfScreen();
//                        }
//                    break;
//
//
//                }
//                v.onTouchEvent(event);
//                return false;
//
//                // Handle ListView touch events.
////                return false;
//            }
//        });

        setUpFullScreen();

        imgViewLayoutParams = (RelativeLayout.LayoutParams) mImageMain.getLayoutParams();
        return mRootView;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG,"OnActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG,"callback->main fragment on view created");
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Binding story data
     */
    private void bindData() {
        if (mContentData == null) {
            return;
        }
        final ContentData cd = mContentData;

        mLoadingText.setVisibility(View.VISIBLE);
        frescoImagePipeline = Fresco.getImagePipeline();

        if(Utils.isNotEmpty(mContentData.pictureUrl)) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mContentData.pictureUrl))
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                    .build();
            GenericDraweeHierarchyBuilder builder =
                    new GenericDraweeHierarchyBuilder(getResources())
                            .setPlaceholderImage(getResources().getDrawable(R.drawable.progress_image));
//                            .setFailureImage(getResources().getDrawable(R.drawable.splash_bg));

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mImageMain.getController())
                    .setImageRequest(request)
                    .build();

            mImageMain.setHierarchy(builder.setFadeDuration(500).
                    build());
            mImageMain.setController(controller);
        }


        mTextTitle.setText(cd.name);
        mTextPlace.setText(cd.place);
        mPicSummary.setText(cd.pictureSummary);

        if(cd.editorsPick)
            mEditorPickText.setVisibility(View.VISIBLE);
        else
            mEditorPickText.setVisibility(View.INVISIBLE);



        if (mContentData.likedByUser == true)
            mImageLike.setImageResource(R.drawable.liked);
        else
            mImageLike.setImageResource(R.drawable.like);

        mLikeCountTV.setText(String.valueOf(cd.likeCount));

        //***Binding StoryContent****/
        ImageView mProfileImage = (ImageView) mStoryLayout.findViewById(R.id.imgProfile);
        TextView mTextName = (TextView) mStoryLayout.findViewById(R.id.txtName);
        TextView mTextDesc = (TextView) mStoryLayout.findViewById(R.id.txtDesc);
        TextView mCategory1 = (TextView)mStoryLayout.findViewById(R.id.category_1);
        TextView mCategory2 = (TextView)mStoryLayout.findViewById(R.id.category_2);
        TextView mCategory3 = (TextView)mStoryLayout.findViewById(R.id.category_3);
        TextView mseperator1 = (TextView)mStoryLayout.findViewById(R.id.separator_1);
        TextView mseperator2 = (TextView)mStoryLayout.findViewById(R.id.separator_2);
        //mCategory1.setPaintFlags(mCategory1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //mCategory2.setPaintFlags(mCategory2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //mCategory3.setPaintFlags(mCategory3.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        DroidSerifTextView mTextStoryDetails = (DroidSerifTextView) mStoryLayout.findViewById(R.id.txtDetailsPara);
        mShareBtn = (LinearLayout) mRootView.findViewById(R.id.btnShare);
        mCommentBtn = (LinearLayout) mRootView.findViewById(R.id.btnComment);

        if (cd != null) {
            if (cd.personDetails != null) {
                if (cd.personDetails.imageUrl == null || cd.personDetails.imageUrl.trim().equals("")){
                    cd.personDetails.imageUrl = "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";
                }
                String name = (!(cd.personDetails.name.equals("")))? cd.personDetails.name : " ";
                if(cd.categoryNameList!=null)
                {
                    name += (" in #"+cd.categoryNameList.get(0).categoryName);
                    if(cd.categoryNameList.size()>1)
                        name+=(" #"+cd.categoryNameList.get(1).categoryName);
                    if(cd.categoryNameList.size()>2)
                        name+=(" #"+cd.categoryNameList.get(2).categoryName);
                }
                mTextExpert.setText(name);
                Picasso.with(mContext).load(cd.personDetails.imageUrl).placeholder(R.drawable.profile_icon_3).fit().into(mProfileImage);
                mTextName.setText(cd.personDetails.name);
                if(cd.categoryNameList!=null){
                //mTextDesc.setVisibility(View.VISIBLE);
                mCategory1.setVisibility(View.VISIBLE);
                mCategory1.setText("#"+cd.categoryNameList.get(0).categoryName);
                mCategory1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!mListener.isCategoryViewOpen() && !isProfileContent) {

                            Category category = cd.categoryNameList.get(0);
                            Toast.makeText(mContext, category.categoryName, Toast.LENGTH_SHORT).show();
                            mListener.stopStoryTimer(cd.id);
                            mListener.showCategoryStories(category.categoryId ,category.categoryName,cd.id );
                        }
                        else
                            Toast.makeText(mContext,"You can access categories only from your home screen",Toast.LENGTH_LONG).show();
                    }
                });

                if(cd.categoryNameList.size()>1){
                    //mseperator1.setVisibility(View.VISIBLE);
                    mCategory2.setVisibility(View.VISIBLE);
                    mCategory2.setText("#"+cd.categoryNameList.get(1).categoryName);
                    mCategory2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!mListener.isCategoryViewOpen() && !isProfileContent) {
                                Category category = cd.categoryNameList.get(1);
                                Toast.makeText(mContext, category.categoryName, Toast.LENGTH_SHORT).show();
                                mListener.stopStoryTimer(cd.id);
                                mListener.showCategoryStories(category.categoryId ,category.categoryName,cd.id );
                            }
                            else
                                Toast.makeText(mContext,"You can access categories only from your home screen",Toast.LENGTH_LONG).show();

                        }
                    });

                    if(cd.categoryNameList.size()>2){
                        //mseperator1.setVisibility(View.GONE);
                        //mCategory1.setText(cd.categoryNameList.get(0)+",");
                        //mseperator2.setVisibility(View.VISIBLE);
                        mCategory3.setVisibility(View.VISIBLE);
                        mCategory3.setText("#"+cd.categoryNameList.get(2).categoryName);
                        mCategory3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!mListener.isCategoryViewOpen() && !isProfileContent) {
                                    Category category = cd.categoryNameList.get(2);
                                    Toast.makeText(mContext, category.categoryName, Toast.LENGTH_SHORT).show();
                                    mListener.stopStoryTimer(cd.id);
                                    mListener.showCategoryStories(category.categoryId ,category.categoryName, cd.id );
                                }
                                else
                                    Toast.makeText(mContext,"You can access categories only from your home screen",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                }

                mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            //Toast.makeText(mContext,cd.personDetails.id+"",Toast.LENGTH_SHORT).show();

                        if(!mListener.isCategoryViewOpen() && !isProfileContent){
                            mListener.stopStoryTimer(cd.id);
                            navigateToUserProfile(cd);
                        }
                        else
                            Toast.makeText(mContext,"You can access expert's profile only from your home screen",Toast.LENGTH_LONG).show();
                    }
                });
              /*  mTextName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToUserProfile(cd);
                    }

                });
                mTextDesc.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         navigateToUserProfile(cd);
                     }
            });*/
            }
            Log.i(TAG,"bindStorycd data->date::"+cd.date);
            mTextStoryDetails.setText(Html.fromHtml(cd.pictureDescription+"<br></br><br></br><br></br><br></br><br>" +
                    "</br><br></br><br></br>"))
            ;
        }

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utils.isNotEmpty(Utils.getFbID(mContext))) {
                   /* final Handler handler = new Handler();
                    final Handler handler1 = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            mShareImg.setVisibility(View.GONE);
                            mLoadingPanel.setVisibility(View.VISIBLE);
                        }
                    };
                    handler.post(runnable);*/
                    //mShareImg.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.rotate));
                    //mShareImg.setVisibility(View.GONE);
                    //mLoadingPanel.setVisibility(View.VISIBLE);

                    mListener.stopStoryTimer(cd.id);

                   // mProgressDialog.setTitle("Please wait...!!");
                    //mProgressDialog.setCanceledOnTouchOutside(false);
                    //mProgressDialog.show();

                    dialog.show();
                    sharePixtory(cd);
                    AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("ST_Share_Click")
                            .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                            .put("PIXTORY_ID", cd.id + "");
                    sendAmplitudeLog(appEventBuilder);
                }else {
                    mListener.showLoginAlert();
                }
            }
        });
        mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("ST_Comment_Click")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",cd.id+"");
                sendAmplitudeLog(appEventBuilder);
                mListener.stopStoryTimer(cd.id);
                buildCommentsLayout(cd);
                attachPixtoryContent(AppConstants.SHOW_PIC_COMMENTS);
            }
        });

    }

    /**
     * @param story_or_comment
     * Rationale - Adding story_view_layout fragment to MainFragment
     */
    public void attachPixtoryContent(int story_or_comment){
        mContentLayout.removeAllViews();
        if(story_or_comment == AppConstants.SHOW_PIC_STORY){
            mContentLayout.addView(mStoryLayout);
            setCommentsVisible(false);
            mStoryParentLayout.setNestedScrollingEnabled(true);
            mImageDetailsLayout.setScrollingEnabled(true);

            mCommentPostBtnLayout.setVisibility(View.GONE);

        }else{
            mImageDetailsLayout.setScrollingEnabled(false);
            mContentLayout.addView(mCommentsLayout);
            setCommentsVisible(true);
            mStoryParentLayout.setNestedScrollingEnabled(false);
            mImageDetailsLayout.setScrollingEnabled(false);
            mCommentPostBtnLayout.setVisibility(View.VISIBLE);
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

//         Make sure that we are currently visible
        if (this.isVisible()) {
            Log.i(TAG,"user visible hint");
            resetFragmentState();
        }
    }

    private void setUpStoryContent() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        mStoryLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.story_view_layout, null);
        mCommentsLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.story_comment_layout , null);
    }

    public boolean isCommentsVisible() {
        return isCommentsVisible;
    }

    public void setCommentsVisible(boolean commentsVisible) {
        isCommentsVisible = commentsVisible;

        if(isCommentsVisible) {
            mCommentShareLayout.setVisibility(View.GONE);
        }
        else {
            mCommentShareLayout.setVisibility(View.VISIBLE);
        }

    }

    /**
     *
     * @param scrollView
     * @param x
     * @param y
     * @param oldX
     * @param oldY
     *
     * Rationale - OnScrollChange listener implementation for mImageDetailsLayout
     */
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
        void showMenuIcon(boolean showMenuIcon);
        void showLoginAlert();
        void showCategoryStories(int categoryId,String categoryName,int pixtoryId);
        void showShareDialog(ContentData contentData);
        boolean isCategoryViewOpen();
        void showWallPaperCoachMark();
        void startStoryTimer(int pixtoryId);
        void stopStoryTimer(int pixtoryId);
    }

    public void resetFragmentState() {
        setUpFullScreen();
    }

    /********Swipe Up and Down Logic*********************/

    final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {

                    //swipeUpArrow.clearAnimation();
                    //swipeUpArrow.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    //Toast.makeText(mContext,"Long tap detected",Toast.LENGTH_SHORT).show();
                    /*Log.i(TAG, "Long Tap");
                    if (isFullScreenShown) {
                        isLongTap = true;
                        showWallpaperAlert();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_Wallpaper_LongPress")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID", mContentData.id + "")
                                .put("POSITION_ID", mContentIndex + "")
                                .build());
                    }*/
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
                    Log.i(TAG, "gesture->OnFling::::");


                    final int SWIPE_MIN_DISTANCE = 50;
                    final int SWIPE_THRESHOLD_VELOCITY = 100;
                    try {
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {


                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                        } else {
                        return false;
                        }
                    } catch (Exception e) {
                        // nothing
//                        e.printStackTrace();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    private void navigateToUserProfile(ContentData cd){

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

    private boolean modifyScreenHeight(int offset) {

//        imgViewLayoutParams.height = (mImageExtendedHeight) -offset;
//        mImageMain.setLayoutParams(imgViewLayoutParams);
        mImageMain.setTranslationY((-1.0f)*(offset/2));

        if(offset > mBottomScreenHt ){
            showCommentsShareLayout(true);
        }
        else{
            showCommentsShareLayout(false);
        }
        return true;

    }

    private void showCommentsShareLayout(boolean showCommentsShare){
        if(!isCommentsVisible()) {
            if (showCommentsShare)
                mCommentShareLayout.setVisibility(View.VISIBLE);
            else
                mCommentShareLayout.setVisibility(View.GONE);
        }

    }

    @OnTouch(R.id.pic_story_layout)
    public boolean onTouchContent(NestedScrollView view, MotionEvent me) {

//        if comment section is visible , swipe up and down gesture for story and content is disabled
        if(isCommentsVisible()){
            return false;
        }

        if (me.getAction() == MotionEvent.ACTION_SCROLL){
            Log.i(TAG,"scrolling++"+me.getYPrecision());

        }

        // Disallow the touch request for parent scroll on touch of child view
        if (me.getAction() == MotionEvent.ACTION_UP) {
            Log.i(TAG,"min dist-"+(scrollY-oldScrollY));

            if((scrollY-oldScrollY) != 0){
                setUpHalfScreen();
            }
            return false;
        }
        return false;

    }

    int mHalfScreenSize;
    @OnTouch(R.id.image_details_layout)
    public boolean onTouchStory(NestedScrollView view, MotionEvent me) {

        //if comment section is visible , swipe up and down gesture for story and content is disabled
        if(isCommentsVisible()){
            return false;
        }

        if (gesture.onTouchEvent(me)) {
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
////            Log.i(TAG , "MotionEvent.ACTION_UP::"+isScrollingUp+"::scrollY::"+scrollY);
//            Log.i(TAG,"min dist-"+(scrollY-oldScrollY));
//
            if( (scrollY-oldScrollY) != 0){
                setUpHalfScreen();
            }
//            if(!isLongTap) {
//                setUpHalfScreen();
//            }
//            isLongTap = false;
        }

        return false;
    }

    public void setUpFullScreen(){
        isPixtorySwipeUp=true;
        isFullScreenShown = true;
        mStoryParentLayout.fullScroll(View.FOCUS_UP);
        attachPixtoryContent(AppConstants.SHOW_PIC_STORY);

        mImageDetailsLayout.setVisibility(View.VISIBLE);
        mTextExpert.setVisibility(View.VISIBLE);

        mCommentShareLayout.setVisibility(View.GONE);
        mImageDetailsLayout.smoothScrollTo(0, 0);

        if(!isSwipeUpArrowShown){
            swipeUpArrow.setVisibility(View.VISIBLE);
            swipeUpArrow.setAnimation(mAnimation);
            isSwipeUpArrowShown=true;
        }

        if(mListener!=null)
            mListener.showMenuIcon(true);
    }

    public void setUpHalfScreen(){
        isFullScreenShown = false;

        mTextExpert.setVisibility(View.GONE);
        swipeUpArrow.setVisibility(View.GONE);
        swipeUpArrow.clearAnimation();
        isSwipeUpArrowShown=false;
        if(isCommentsVisible())
            attachPixtoryContent(AppConstants.SHOW_PIC_STORY);

        isScrollingUp= (scrollY < oldScrollY) ?false:true;
//        Log.i(TAG,"scrollY::"+scrollY+"::mHalfSCreenSize::"+mHalfScreenSize);
        if(scrollY < mHalfScreenSize){

            mImageDetailsLayout.post(new Runnable() {
                @Override
                public void run() {
                    if(isScrollingUp){
                        mListener.showMenuIcon(false);
                        storyBackImg.setVisibility(View.GONE);
                        mWallpaperSettingsIcon.setVisibility(View.GONE);
                        mImageDetailsLayout.smoothScrollTo(0,mHalfScreenSize);
                        isFullScreenShown=false;
                        Log.i(TAG, "MotionEvent.ACTION_DOWN");
                        if(isPixtorySwipeUp){
                        AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder(MF_Picture_StoryView)
                                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                .put("PIXTORY_ID",""+mContentData.id);
                        sendAmplitudeLog(appEventBuilder);
                        Log.i(TAG,"MF_Picture_StoryView_Amplitude");
                        PreviousEvent = MF_Picture_StoryView;
                        mListener.startStoryTimer(mContentData.id);
                        }
                        isPixtorySwipeUp=false;

                    }
                    else {

                        if(mListener != null)
                            mListener.showMenuIcon(true);
                        storyBackImg.setVisibility(View.VISIBLE);
                        mWallpaperSettingsIcon.setVisibility(View.VISIBLE);
                        mImageDetailsLayout.smoothScrollTo(0, 0);
                        isFullScreenShown = true;
                        mStoryParentLayout.fullScroll(View.FOCUS_UP);

                        mTextExpert.setVisibility(View.VISIBLE);
                        if(PreviousEvent==MF_Picture_StoryView){
                            AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder(ST_Story_PictureView)
                                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                .put("PIXTORY_ID",""+mContentData.id);
                            sendAmplitudeLog(appEventBuilder);
                            PreviousEvent = ST_Story_PictureView;
                            Log.i(TAG,"ST_Story_PictureView_Amplitude");
                            mListener.stopStoryTimer(mContentData.id);
                        }
                        if(!isSwipeUpArrowShown){
                            swipeUpArrow.setVisibility(View.VISIBLE);
                            swipeUpArrow.setAnimation(mAnimation);
                            isSwipeUpArrowShown=true;
                        }
                        isPixtorySwipeUp = true;
                    }
                }
            });
        }

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

        Button mPostComment = (Button)mRootView.findViewById(R.id.postComment);
        ImageView mCommentCloseBtn = (ImageView)mCommentsLayout.findViewById(R.id.closeBtn);

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

        setCommentsVisible(true);

        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utils.isNotEmpty(Utils.getFbID(mContext))) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    CommentsDialogFragment commentsDialogFragment = CommentsDialogFragment.newInstance("Some title");
                    commentsDialogFragment.show(fm, "fragment_alert");
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_AddComment_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                            .put("PIXTORY_ID",mContentData.id+"")
                            .build());
                }
                else{
                    mListener.showLoginAlert();
                }
            }
        });

        fetchCommentList(data.id);

        mCommentCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachPixtoryContent(AppConstants.SHOW_PIC_STORY);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_CommentView_close")
                        .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                        .put("PIXTORY_ID",(mContentData.id)+"")
                        .build());
            }
        });

    }

    /**
     * Method to post a new comment
     * @param comment
     */
    public void postComment(String comment) {

            final int content_id = mContentData.id;

            if(!((Utils.getFbID(mContext)).equals(""))) {
                //User is allowed to comment only if loggedIn
                NetworkApiHelper.getInstance().addComment(Utils.getUserId(mContext), content_id, comment.trim(), new NetworkApiCallback<AddCommentResponse>() {

                    @Override
                    public void success(AddCommentResponse addCommentResponse, Response response) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("CM_SubmitComment_Click")
                                .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                                .put("PIXTORY_ID",mContentData.id+"")
                                .build());

                        fetchCommentList(content_id);
                        Log.i(TAG , "Comment Succes/sfully Posted");
                        Toast.makeText(getActivity(),"Comment Successfully Posted",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(AddCommentResponse addCommentResponse) {
                        Toast.makeText(getActivity(),"Oops Something went wrong!!",Toast.LENGTH_SHORT).show();

                        Log.i(TAG, "Add Comment Request Failure");
                    }
                    @Override
                    public void networkFailure(RetrofitError error) {
                        Log.i(TAG, "Add Comment Request Network Failure, Error Type::" + error.getMessage());
                        Toast.makeText(getActivity(),"Please check your network connection",Toast.LENGTH_SHORT).show();
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
            mTVCommentCount.setText("Be the first to comment! ");
            mCommentText.setVisibility(View.GONE);
        }
        mTVLoading.setVisibility(View.GONE);
        mRLCommentList.setVisibility(View.VISIBLE);
    }

    private void fetchCommentList(int story_id){

        mTVLoading.setVisibility(View.VISIBLE);
        mRLCommentList.setVisibility(View.INVISIBLE);

        NetworkApiHelper.getInstance().getCommentDetailList(Utils.getUserId(mContext), story_id , new NetworkApiCallback<GetCommentDetailsResponse>() {

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
     *
     * @param view
     * Implementation to like a story
     */
    @OnClick(R.id.image_like)
    public void onLikeClick(ImageView view) {
        if(Utils.isNotEmpty(Utils.getFbID(mContext))) {
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
                if(isFullScreenShown){
                    AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("MF_Picture_Like")
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("PIXTORY_ID", "" + mContentData.id)
                            .put("POSITION_ID",""+mContentIndex)
                            .put("BOOLEAN","True");
                    sendAmplitudeLog(appEventBuilder);
                }
                else{
                    AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("ST_Story_Like")
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("PIXTORY_ID", "" + mContentData.id)
                            .put("POSITION_ID",""+mContentIndex)
                            .put("BOOLEAN","False");
                    sendAmplitudeLog(appEventBuilder);

                }

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
                if(isFullScreenShown){
                    AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("MF_Picture_UnLike")
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("PIXTORY_ID", "" + mContentData.id)
                            .put("POSITION_ID",""+mContentIndex)
                            .put("BOOLEAN","True");
                    sendAmplitudeLog(appEventBuilder);

                }
                else{
                    AmplitudeLog.AppEventBuilder appEventBuilder = new AmplitudeLog.AppEventBuilder("ST_Story_UnLike")
                            .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                            .put("PIXTORY_ID", "" + mContentData.id)
                            .put("POSITION_ID",""+mContentIndex)
                            .put("BOOLEAN","False");
                    sendAmplitudeLog(appEventBuilder);
                }
            }
            sendLikeToBackend(mContentData.id, mContentData.likedByUser);
        }
        else {
            mListener.showLoginAlert();
        }
    }

    private void sendLikeToBackend(int contentId, final boolean isLiked) {
        NetworkApiHelper.getInstance().likeContent(Utils.getUserId(getActivity()), contentId, isLiked, new NetworkApiCallback<BaseResponse>() {

            @Override
            public void success(BaseResponse baseResponse, Response response) {
                if(isLiked) {
                    App.addToPersonContentData(mContentData);
                    mContentData.likeCount += 1;
                }else{
                    App.removeFromPersonContentData(mContentData);
                    if(mContentData.likeCount>0)
                        mContentData.likeCount -= 1;
                }
                mLikeCountTV.setText(String.valueOf(mContentData.likeCount));
            }

            @Override
            public void failure(BaseResponse baseResponse) {
                Utils.showToastMessage(mContext,"Oops something went wrong",0);
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Utils.showToastMessage(mContext,"Please check your network connection",0);
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
            attachPixtoryContent(AppConstants.SHOW_PIC_STORY);
        }
    }

    public  boolean isFullScreenShown(){return isFullScreenShown;}

    private void showWallpaperAlert(){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.set_wallpaper_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        FrameLayout wallpaperNo = (FrameLayout) dialog.findViewById(R.id.wallpaper_no_2);
        FrameLayout wallpaperYes =(FrameLayout) dialog.findViewById(R.id.wallpaper_yes_2);

        wallpaperYes.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_SingleWpConfirm_Click")
                        .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                        .put("PIXTORY_ID", mContentData.id + "")
                        .put("POSITION_ID", mContentIndex + "")
                        .build());
//                setWallpaper();
                Picasso.with(mContext).load(mContentData.pictureUrl).into(App.mWallpaperTarget);
                dialog.dismiss();
            }
        });

        wallpaperNo.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_SingleWpCancel_Click")
                        .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                        .put("PIXTORY_ID", mContentData.id + "")
                        .put("POSITION_ID", mContentIndex + "")
                        .build());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void setWallpaper(){

        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(mContentData.pictureUrl))
                .setRequestPriority(Priority.HIGH)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();

        DataSource<CloseableReference<CloseableImage>> dataSource =
                frescoImagePipeline.fetchDecodedImage(imageRequest, mContext);

        try {
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (bitmap == null) {
                        Log.d(TAG, "Bitmap data source returned success, but bitmap null.");
                        return;
                    }
                    Log.i(TAG,"Bitmap is not null-"+bitmap.toString());
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask(mContext,bitmap);
                    setWallpaperTask.execute();

                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here
                }
            }, CallerThreadExecutor.getInstance());
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }


    }

    public void sharePixtory(final ContentData contentData) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(mContentData.pictureUrl))
                .setRequestPriority(Priority.HIGH)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();

        DataSource<CloseableReference<CloseableImage>> dataSource =
                frescoImagePipeline.fetchDecodedImage(imageRequest, mContext);

        try {
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (bitmap == null) {
                        Log.d(TAG, "Bitmap data source returned success, but bitmap null.");
                        Toast.makeText(mContext,"Oops! something went wrong. Please try again later",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG, "Bitmap is not null-" + bitmap.toString());
                    mImageBitmap = bitmap;
                    shareBitMap(bitmap);
                    // The bitmap provided to this method is only guaranteed to be around
                    // for the lifespan of this method. The image pipeline frees the
                    // bitmap's memory after this method has completed.
                    //
                    // This is fine when passing the bitmap to a system process as
                    // Android automatically creates a copy.
                    //
                    // If you need to keep the bitmap around, look into using a
                    // BaseDataSubscriber instead of a BaseBitmapDataSubscriber.
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here
                }
            }, CallerThreadExecutor.getInstance());
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }



    }

    public void shareBitMap(Bitmap bitmap){

        if(bitmap !=null){

            try {
                File cachePath = new File(mContext.getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/" + contentData.name + ".png"); // overwrites this image every time
                mImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File imagePath = new File(mContext.getCacheDir(), "images");
            File newFile = new File(imagePath, contentData.name + ".png");
            final Uri contentUri = FileProvider.getUriForFile(mContext, "com.pixtory.app.fileprovider", newFile);

            final List<String> sharingAppList = new ArrayList<String>();

            if (Utils.isAppInstalled(mContext, Whatsapp_Package_name)) {
                sharingAppList.add("Whatsapp");
            }
            sharingAppList.add("Facebook");

            if (sharingAppList.size() > 0) {

                final CharSequence[] AppList = sharingAppList.toArray(new String[sharingAppList.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Share Pixtory Via");
                builder.setItems(AppList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // Do something with the selection
                        if (sharingAppList.get(item).equals("Whatsapp")) {
                            shareOnWassapp(contentUri);
                        } else if (sharingAppList.get(item).equals("Facebook")) {

                            NetworkApiHelper.getInstance().shareContent(Integer.parseInt(Utils.getUserId(mContext)),
                                    mContentData.id, new NetworkApiCallback<ShareContentIdResponse>() {


                                        @Override
                                        public void success(ShareContentIdResponse shareContentIdResponse, Response response) {
                                            mContentData.id = shareContentIdResponse.sharedContentId;
                                            if(mListener!=null)
                                                mListener.showShareDialog(mContentData);

                                        }

                                        @Override
                                        public void failure(ShareContentIdResponse shareContentIdResponse) {

                                        }

                                        @Override
                                        public void networkFailure(RetrofitError error) {

                                        }
                                    });
                        }
    //                            else if (sharingAppList.get(item).equals("Instagram")) {
    //                                shareOnInstagram(contentUri);
    //                            } else if (sharingAppList.get(item).equals("Gmail")) {
    //                                shareOnGmail(contentUri);
    //                            }
                    }
                });


                AlertDialog alert = builder.create();
                //mProgressDialog.dismiss();
                dialog.dismiss();
                alert.show();
            } else {
                Utils.showToastMessage(mContext, "No Apps To Share", 0);
            }
        }else
            Toast.makeText(mContext,"Oops! something went wrong. Please try again later",Toast.LENGTH_SHORT).show();
        }
    },20);

    }

    private boolean isTouchEnabled = true;
    public  void setActioUpEnabled(boolean isEnable){
        isTouchEnabled = isEnable;
    }

    public boolean getActionUpEnabled(){
        return isTouchEnabled;
    }

    public class SetWallpaperTask extends AsyncTask<Void,Void, Void> {

        Bitmap bitmap;
        Context context;

        public SetWallpaperTask(Context pContext , Bitmap pBitmap){
            bitmap = pBitmap;
            context = pContext;
        }

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread
            // Do any pre-executing tasks here, for example display a progress bar
            Log.d(TAG, "About to set wallpaper...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Runs on the background thread
//            Utils.setWallpaper(context,bitmap);
            if(bitmap != null && context !=null){
                WallpaperManager wallpaperManager
                        = WallpaperManager.getInstance(mContext);

                try {
                    wallpaperManager.setBitmap(bitmap);
                    Log.i("WallPaper","Set wallpaper done");
//                    Toast.makeText(mContext,"Yayy!! Wallpaper set",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.i("WallPaper","Set wallpaper IO exception");
//                    Toast.makeText(mContext,"Oops we couldn't set your wallpaper",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            else {
                Log.i("Utils-","imageBitMap or mContext is null");
//                Toast.makeText(mContext,"Oops we couldn't set your wallpaper",Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread
            // Here you can perform any post-execute tasks, for example remove the
            // progress bar (if you set one).
            Toast.makeText(mContext,"Hooray! Wallpaper set!",Toast.LENGTH_SHORT).show();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("WP_DeviceWallpaper_Set")
                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                .build());
            Log.d(TAG, "New wallpaper set");
        }

    }

//    public void shareOnFacebook(Bitmap bitmap){
//        SharePhoto photo = new SharePhoto.Builder()
//                .setBitmap(bitmap)
//                .build();
//        SharePhotoContent content = new SharePhotoContent.Builder()
//                .addPhoto(photo)
//                .build();
//
//    }

    public void showPopUpMenu(){
        final PopupMenu popupMenu = new PopupMenu(mContext,mWallpaperSettingsIcon);
        popupMenu.inflate(R.menu.wallpaper_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AmplitudeLog.AppEventBuilder appEventBuilder;
                switch (item.getItemId()){
                    case R.id.wallpaper_overflow_set:showWallpaperAlert();
                        appEventBuilder = new AmplitudeLog.AppEventBuilder("MF_SingleWp_Click")
                                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                .put("PIXTORY_ID",mContentData.id+"");
                        sendAmplitudeLog(appEventBuilder);
                        popupMenu.dismiss();
                        return true;

                    case  R.id.wallpaper_overflow_set_today:showWallpaperAlert();
                        appEventBuilder = new AmplitudeLog.AppEventBuilder("MF_SingleOverDailyWp_Click")
                                .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                                .put("PIXTORY_ID",mContentData.id+"");
                        sendAmplitudeLog(appEventBuilder);
                        popupMenu.dismiss();
                        return true;

                    case R.id.wallpaper_overflow_daily:mListener.showWallPaperCoachMark();
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("MF_DailyWp_Click")
                            .put(AppConstants.USER_ID,Utils.getUserId(mContext))
                            .put("PIXTORY_ID",mContentData.id+"")
                            .build());
                        popupMenu.dismiss();
                        return true;

                    case R.id.wallpaper_overflow_close:popupMenu.dismiss();
                        return true;

                    default:return true;
                }

            }
        });

        boolean isOpted = getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(HomeActivity.OPT_FOR_DAILY_WALLPAPER,false);
        if(isOpted || isProfileContent || mListener.isCategoryViewOpen())
            popupMenu.getMenu().removeItem(R.id.wallpaper_overflow_daily);
        if(isOpted)
            popupMenu.getMenu().removeItem(R.id.wallpaper_overflow_set);
        else
            popupMenu.getMenu().removeItem(R.id.wallpaper_overflow_set_today);
        popupMenu.show();
    }

    public void shareOnWassapp(Uri contentUri){
        String data = mContentData.pictureDescription;
        data = data.replace("<b>","*").replace("</b>","*").replace("<i>","_").replace("</i>","_").replace("<p>","\n").replace("</p>","").replace("<br>","").replace("</br>","");
        //data = Html.fromHtml(data).toString();
        Intent whatsappIntent=new Intent();
//                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
        whatsappIntent.setAction(Intent.ACTION_SEND);
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        whatsappIntent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        whatsappIntent.putExtra(android.content.Intent.EXTRA_TEXT, mContext.getPackageName());
        SpannableString str = new SpannableString("For more such stories\n"+"Checkout our app  "+AppConstants.SCM_WHATSAPP_SHARE);
//        whatsappIntent.putExtra(Intent.EXTRA_TEXT,"*"+mContentData.name+"*\nBy _"+mContentData.personDetails.name
//                +"_\n\n"+mContentData.pictureDescription + "Check out our website www.pixtory.in");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT,"*"+mContentData.name+"*\nBy _"+mContentData.personDetails.name+"_\n\n"+data + "\n\n"+str);
        whatsappIntent.setPackage(Whatsapp_Package_name);


        startActivity(whatsappIntent);
//        targetInviteIntents.add(whatsappIntent);
    }


    public  void shareOnInstagram(Uri contentUri){
        String content = mContentData.pictureDescription;
        content = content.replace("<b>","").replace("</b>","").replace("<i>","").replace("</i>","").replace("<p>","\n").replace("</p>","").replace("<br>","").replace("</br>","");
        Intent intent=new Intent();
//                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        intent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT,mContentData.name+"\nBy "+mContentData.personDetails.name+"\n\n"+content);
        intent.setPackage(Instagram_Package_name);
        startActivity(intent);
    }

    public void shareOnGmail(Uri contentUri){
        String content = mContentData.pictureDescription;
        content = content.replace("<b>","").replace("</b>","").replace("<i>","").replace("</i>","").replace("<p>","\n").replace("</p>","").replace("<br>","").replace("</br>","");
        Intent intent=new Intent();
//                        intent.setComponent(new ComponentName(Gmail_Package_name , getActivity().getPackageCodePath()));
        intent.setAction(Intent.ACTION_SEND);
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        intent.setType("application/image");
        mContext.grantUriPermission(Gmail_Package_name, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra(Intent.EXTRA_SUBJECT, "Pixtory!");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT,mContentData.name+"\nBy "+mContentData.personDetails.name+"\n\n"+content);
        intent.setPackage(Gmail_Package_name);
        startActivity(intent);
    }

    private void sendAmplitudeLog(AmplitudeLog.AppEventBuilder appEventBuilder){
        if(isProfileContent)
            AmplitudeLog.logEvent(appEventBuilder.put("IS_PROFILE","TRUE")
                    .put("IS_CATEGORY","FALSE")
                    .build());
        else if(mListener.isCategoryViewOpen())
            AmplitudeLog.logEvent(appEventBuilder.put("IS_PROFILE","FALSE")
                    .put("IS_CATEGORY","TRUE")
                    .build());
        else
            AmplitudeLog.logEvent(appEventBuilder.put("IS_PROFILE","FALSE")
                    .put("IS_CATEGORY","FALSE")
                    .build());
    }

}
