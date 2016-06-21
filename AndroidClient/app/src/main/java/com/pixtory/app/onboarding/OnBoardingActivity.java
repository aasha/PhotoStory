package com.pixtory.app.onboarding;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.util.Base64;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;


public class OnBoardingActivity  extends FragmentActivity {
    //
//    Dialog commentDialog ;
    private CallbackManager callbackManager;
    private ProgressDialog mProgressDialog = null;

    private final String TAG = OnBoardingActivity.class.getName();

    //Analytics
    final static String SCREEN_NAME = "Onboard";
    final static String OB_Card_Swipe = "OB_Card_Swipe";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_onboarding_layout);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        printSHA();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


//        User will be directed to Main Feed page is already loggedIn
        if (redirectIfLoggedIn()) {
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        LinearLayout imageViewFb = (LinearLayout) findViewById(R.id.fb_sign_btn);

        imageViewFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Click)
                        .build());
                LoginManager.getInstance().logInWithReadPermissions(OnBoardingActivity.this, AppConstants.mFBPermissions);
            }
        });


        TextView skipLogin = (TextView) findViewById(R.id.skipLogin);
        skipLogin.setPaintFlags(skipLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserName("User");
                mProgressDialog.show();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("OB_Login_Skip").build());
//                gotoNextScreen();
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(OnBoardingActivity.this, "FB Login Success!", Toast.LENGTH_SHORT).show();
                        onFacebookLoginSuccess();
                    }

                    @Override
                    public void onCancel() {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Cancel)
                                .build());
                        closeDialog();
                        Toast.makeText(OnBoardingActivity.this, "Sorry, unable to login to facebook.Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Fail)
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
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Success)
                            .put("NAME", name)
                            .put("FBID", fbId)
                            .build());
                    NetworkApiHelper.getInstance().registerUser(name, email, imgUrl,fbId,new NetworkApiCallback<RegisterResponse>() {
                        @Override
                        public void success(RegisterResponse regResp, Response response) {
                            Log.i(TAG, "Registering user to pixtory sucess");
                            closeDialog();
                            Utils.putUserId(OnBoardingActivity.this, regResp.userId);
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Success)
                                    .put(AppConstants.USER_ID, regResp.userId)
                                    .build());
                            Utils.putFbId(OnBoardingActivity.this, fbId);
                            Utils.putEmail(OnBoardingActivity.this, email);
                            Utils.putUserName(OnBoardingActivity.this, name);
                            Utils.putUserImage(OnBoardingActivity.this, imgUrl);
                            AmplitudeLog.sendUserInfo(regResp.userId);
                            gotoNextScreen();
                        }

                        @Override
                        public void failure(RegisterResponse error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                                    .put("MESSAGE", error.errorMessage)
                                    .build());
                            closeDialog();
                        }

                        @Override
                        public void networkFailure(RetrofitError error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
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

    private void gotoNextScreen() {
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

    private void registerUserName(final String name) {
        NetworkApiHelper.getInstance().registerUser(name, null, null,null,new NetworkApiCallback<RegisterResponse>() {
            @Override
            public void success(RegisterResponse regResp, Response response) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Utils.putUserId(OnBoardingActivity.this, regResp.userId);
//                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Success)
//                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Success)
                        .put("USER_ID", regResp.userId)
                        .build());
                Utils.putUserName(OnBoardingActivity.this, name);

                AmplitudeLog.sendUserInfo(regResp.userId);
                gotoNextScreen();
            }

            @Override
            public void failure(RegisterResponse error) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
//                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Fail)
//                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                        .put("MESSAGE", error.errorMessage)
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Username is taken. Please insert a new username", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
//                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OB_UsernameLogin_Fail)
//                        .build());
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                        .put("MESSAGE", error.getMessage())
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Please connect to network", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void printSHA(){
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.pixtory.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }


}
