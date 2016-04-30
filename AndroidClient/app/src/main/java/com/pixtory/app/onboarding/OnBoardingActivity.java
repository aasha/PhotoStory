package com.pixtory.app.onboarding;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import app.viewpagerindicator.CirclePageIndicator;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.RegisterResponse;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import org.json.JSONObject;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ajitesh.shukla on 1/8/16.
 */
public class OnBoardingActivity  extends FragmentActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public static ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public static PagerAdapter mPagerAdapter;

    Dialog commentDialog ;
    private CallbackManager callbackManager;
    private ProgressDialog mProgressDialog = null;
    //Ananlytics
    final static String SCREEN_NAME = "Onboard";
    final static String OB_Card_Swipe = "OB_Card_Swipe";
    private final static String OB_FBLogin_Click = "OB_FBLogin_Click";
    private final static String OB_FBLogin_Success = "OB_FBLogin_Success";
    private final static String OB_FBLogin_Cancel = "OB_FBLogin_Cancel";
    private final static String OB_FBLogin_Fail = "OB_FBLogin_Fail";
    private final static String OB_Register_Success = "OB_Register_Success";
    private final static String OB_Register_Failure = "OB_Register_Failure";
    private final static String OB_UsernameLogin_Click = "OB_UsernameLogin_Click";
    private final static String OB_UsernameLogin_Success = "OB_UsernameLogin_Success";
    private final static String OB_UsernameLogin_Fail = "OB_UsernameLogin_Fail";

    private List<String> mFBPermissions = Arrays.asList("public_profile",
            "email", "user_about_me");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        if (redirectIfLoggedIn()) {
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        //Prompt to input username
        commentDialog = new Dialog(this);
        commentDialog.setContentView(R.layout.dialog);
        commentDialog.setCancelable(false);
        final EditText txtName = (EditText)commentDialog.findViewById(R.id.body);
        Button okBtn = (Button) commentDialog.findViewById(R.id.ok);
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                registerUserName(txtName.getText().toString());
            }
        });
        Button cancelBtn = (Button) commentDialog.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                commentDialog.dismiss();
            }
        });
        ImageView imageViewFb = (ImageView) findViewById(R.id.fb1);
        imageViewFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_FBLogin_Click)
                        .build());
                LoginManager.getInstance().logInWithReadPermissions(OnBoardingActivity.this, mFBPermissions);
            }
        });
        ImageView imageViewLogin = (ImageView) findViewById(R.id.login1);
        imageViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Click)
                        .build());
                commentDialog.show();
            }
        });

        TextView skipLogin = (TextView)findViewById(R.id.skipLogin);
        skipLogin.setPaintFlags(skipLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserName("User");
                mProgressDialog.show();
            }
        });
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.pagerindicator);
        circlePageIndicator.setFillColor(Color.WHITE);
        circlePageIndicator.setStrokeColor(Color.WHITE);
        circlePageIndicator.setRadius(20f);
        circlePageIndicator.setViewPager(mPager);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(OnBoardingActivity.this, "FB Login Success!", Toast.LENGTH_SHORT).show();
                        onFacebookLoginSuccess();
                    }

                    @Override
                    public void onCancel() {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_FBLogin_Cancel)
                                .build());
                        closeDialog();
                        Toast.makeText(OnBoardingActivity.this, "Sorry, unable to login to facebook.Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_FBLogin_Fail)
                                .put("MESSAGE", exception.getMessage() + "")
                                .build());
                        closeDialog();
                        Toast.makeText(OnBoardingActivity.this, "Sorry, unable to login to facebook.Please check your network connection or try again later.(" + exception.getMessage() + ")", Toast.LENGTH_LONG).show();

                    }
                });
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    ScreenSlidePageFragment2.removeImageViews();
                    ScreenSlidePageFragment3.removeImageViews();
                    return new ScreenSlidePageFragment();
                case 1:
                    ScreenSlidePageFragment.removeImageViews();
                    ScreenSlidePageFragment3.removeImageViews();
                    return new ScreenSlidePageFragment2();
                case 2:
                    ScreenSlidePageFragment.removeImageViews();
                    ScreenSlidePageFragment2.removeImageViews();
                    return new ScreenSlidePageFragment3();
                default: return new ScreenSlidePageFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void onFacebookLoginSuccess() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    final String fbId = user.optString("id");
                    final String name = user.optString("name");
                    final String email = user.optString("email");
                    String accessToken = AccessToken.getCurrentAccessToken().getToken();
                    final String imgUrl = "https://graph.facebook.com/" + fbId + "/picture?width=500&height=500";


                    mProgressDialog.setTitle("Registering user...");
                    mProgressDialog.show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_FBLogin_Success)
                            .put("NAME", name)
                            .put("FBID", fbId)
                            .build());
                    NetworkApiHelper.getInstance().registerUser(name, email, imgUrl, fbId, new NetworkApiCallback<RegisterResponse>() {
                        @Override
                        public void success(RegisterResponse regResp, Response response) {
                            closeDialog();
                            Utils.putUserId(OnBoardingActivity.this, regResp.userId);
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Success)
                                    .put(AppConstants.USER_ID, regResp.userId)
                                    .build());
                            Utils.putFbId(OnBoardingActivity.this,fbId);
                            Utils.putEmail(OnBoardingActivity.this,email);
                            Utils.putUserName(OnBoardingActivity.this,name);
                            Utils.putUserImage(OnBoardingActivity.this,imgUrl);
                            AmplitudeLog.sendUserInfo(regResp.userId);
                            gotoNextScreen();
                        }

                        @Override
                        public void failure(RegisterResponse error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Failure)
                                    .put("MESSAGE", error.errorMessage)
                                    .build());
                            closeDialog();
                        }

                        @Override
                        public void networkFailure(RetrofitError error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Failure)
                                    .put("MESSAGE", error.getMessage())
                                    .build());
                            closeDialog();
                        }
                    });

                }
            }
        });
        request.executeAsync();
    }

    private void gotoNextScreen(){
        Intent i = new Intent(OnBoardingActivity.this, HomeActivity.class);
        startActivity(i);
        this.finish();
    }
    private Boolean redirectIfLoggedIn() {
        closeDialog();
        String userId = Utils.getUserId(OnBoardingActivity.this);
        if (null != userId && !userId.isEmpty()) {
            gotoNextScreen();
            return true;
        }
        LoginManager.getInstance().logOut();
        return false;
    }

    private void closeDialog() {
        try {
            if (mProgressDialog != null
                    && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ignored) {
        }
    }
    private void registerUserName(final String name){
        NetworkApiHelper.getInstance().registerUser(name, null, null, null, new NetworkApiCallback<RegisterResponse>() {
            @Override
            public void success(RegisterResponse regResp, Response response) {
                if(commentDialog.isShowing())
                    commentDialog.dismiss();
                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Utils.putUserId(OnBoardingActivity.this,regResp.userId);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Success)
                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Success)
                        .put("USER_ID", regResp.userId)
                        .build());
                Utils.putUserName(OnBoardingActivity.this, name);
                AmplitudeLog.sendUserInfo(regResp.userId);
                gotoNextScreen();
            }

            @Override
            public void failure(RegisterResponse error) {
                if(commentDialog.isShowing())
                    commentDialog.dismiss();
                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Fail)
                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Failure)
                        .put("MESSAGE", error.errorMessage)
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Username is taken. Please insert a new username", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                if(commentDialog.isShowing())
                    commentDialog.dismiss();
                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Fail)
                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_Register_Failure)
                        .put("MESSAGE", error.getMessage())
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Please connect to network", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
