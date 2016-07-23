package com.pixtory.app.onboarding;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.crash.FirebaseCrash;
import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;



public class OnBoardingActivity  extends FragmentActivity {
    //
//    Dialog commentDialog ;
    private CallbackManager callbackManager;
    private ProgressDialog mProgressDialog = null;

    private final String TAG = OnBoardingActivity.class.getName();

    //Analytics
    final static String SCREEN_NAME = "Onboard";
    final static String OB_Card_Swipe = "OB_Card_Swipe";

    private static final int RC_SIGN_IN = 9001;


    Tracker mTracker;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_onboarding_layout);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        printSHA();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestProfile()
                .build();

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener(){
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(AppInvite.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();


        //Get Default Tracker
        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

        App application = (App) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName("Test Track");

        // Send a screen view.
        mTracker.send(new HitBuilders.AppViewBuilder().build());

        //For testing Firebase crash reports
//        FirebaseCrash.report(new Exception("My first Android non-fatal error - Proguard Error"));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        getDeepLinkData();

//        User will be directed to Main Feed page is already loggedIn
        if (redirectIfLoggedIn()) {
            return;
        }


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        LinearLayout imageViewFb = (LinearLayout) findViewById(R.id.fb_sign_btn);
        LinearLayout googleLogin = (LinearLayout)findViewById(R.id.google_plus_sign_btn);

        imageViewFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Click)
                        .build());
                LoginManager.getInstance().logInWithReadPermissions(OnBoardingActivity.this, AppConstants.mFBPermissions);
            }
        });

        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setMessage("Please wait");
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("OB_GoogleLogin_Click")
                        .build());
                mProgressDialog.show();
                startGoogleSignIn();
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

        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("APP_DOWNLOAD")
                .build());

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(OnBoardingActivity.this, "FB Login Success!", Toast.LENGTH_SHORT).show();
                        onFacebookLoginSuccess(loginResult);
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
        if(mProgressDialog!=null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void onFacebookLoginSuccess(LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    Profile profile = Profile.getCurrentProfile();

                    final String fbId = user.optString("id");
                    final String name = user.optString("name");
                    final String email = user.optString("email");
                    String accessToken = AccessToken.getCurrentAccessToken().getToken();
                    final String imgUrl = "https://graph.facebook.com/" + fbId + "/picture?width=500&height=500";

                    mProgressDialog.setTitle("Fetching pixtories for you");
                    mProgressDialog.show();
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_FBLogin_Success)
                            .put("FbName", name)
                            .put("FBID", fbId)
                            .put("FbEmail",email)
                            .put("FbDob",user.optString("birthday"))
                            .put("FbGender",user.optString("gender"))
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

                            gotoNextScreen(regResp.userId);
                        }

                        @Override
                        public void failure(RegisterResponse error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                                    .put("MESSAGE", error.errorMessage)
                                    .build());
                            Toast.makeText(OnBoardingActivity.this," Oops! We're facing some problems. Please try again later!",Toast.LENGTH_LONG).show();
                            closeDialog();
                        }

                        @Override
                        public void networkFailure(RetrofitError error) {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                                    .put("MESSAGE", error.getMessage())
                                    .build());
                            Toast.makeText(OnBoardingActivity.this, "Please check your network connection", Toast.LENGTH_LONG).show();
                            closeDialog();
                        }
                    });

                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,birthday,email,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void gotoNextScreen(String userId) {
        Intent i = new Intent(OnBoardingActivity.this, HomeActivity.class);
        i.putExtra("USER_ID",userId);
        i.putExtra("NOTIFICATION_CLICK",false);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(i);
        this.finish();
    }

    private Boolean redirectIfLoggedIn() {
        closeDialog();
        String userId = Utils.getUserId(OnBoardingActivity.this);

        if (null != userId && !userId.isEmpty()) {
            AmplitudeLog.sendUserInfo(userId);
            gotoNextScreen(userId);
            return true;
        }
//        LoginManager.getInstance().logOut();
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
                gotoNextScreen(regResp.userId);
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
                Toast.makeText(OnBoardingActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

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



    private void getDeepLinkData(){

        String deviceId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);


        Log.d(TAG,"getDeepLinkData is called");

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    Log.d(TAG,"Launched from deep link::"+deepLink);
                                    Log.d(TAG,"utm_source::"+intent.getData().getQueryParameter("utm_source"));
                                    Log.d(TAG,"utm_medium::"+intent.getData().getQueryParameter("utm_medium"));
                                    Log.d(TAG,"utm_campaign::"+intent.getData().getQueryParameter("utm_campaign"));


                                    // Handle the deep link. For example, open the linked
                                    // content, or apply promotional credit to the user's
                                    // account.

                                    // ...

                                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.INVITE_LINK_CLICKED)
                                            .put("SOURCE",intent.getData().getQueryParameter("utm_source"))
                                            .put("MEDIUM",intent.getData().getQueryParameter("utm_medium"))
                                            .put("CAMPAIGN",intent.getData().getQueryParameter("utm_campaign"))
                                            .build());
                                } else {
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }
                            }
                        });

    }

    //START handleSignInResult
    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.

            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            String imageUrl = acct.getPhotoUrl().toString();

            // Google + profile info
            Person person  = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            Log.i(TAG, "--------------------------------");
            Log.i(TAG, "Display Name: " + person.getDisplayName());
            Log.i(TAG, "Gender: " + person.getGender());
            Log.i(TAG, "AboutMe: " + person.getAboutMe());
            Log.i(TAG, "Birthday: " + person.getBirthday());
            Log.i(TAG, "Current Location: " + person.getCurrentLocation());
            Log.i(TAG, "Language: " + person.getLanguage());
            Log.i(TAG, "GOOGLE_PLUS_PROFILE : " + person.toString());

            String gender = person.getGender()==0?"MALE":"FEMALE";
            String ageRange = person.getAgeRange().toString();

            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("OB_GoogleLogin_Success")
                    .put("GOOGLE_NAME",name)
                    .put("GOOGLE_MAIL",email)
                    .put("GOOGLE_GENDER",gender)
                    .put("GOOGLE_AGE",ageRange)
                    .build());

            Utils.putFbId(OnBoardingActivity.this, "dummyFbId");
            Utils.putEmail(OnBoardingActivity.this, email);
            Utils.putUserName(OnBoardingActivity.this, name);
            Utils.putUserImage(OnBoardingActivity.this, imageUrl);

            registerGoogleAccount(name,email,imageUrl);
            Log.i(TAG,"Google plus details - "+name+" - "+email+" - "+imageUrl);

        } else {
            // Signed out, show unauthenticated UI.
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("OB_GoogleLogin_Failed")
                    .put("STATUS_CODE",result.getStatus()+"")
                    .build());
            Toast.makeText(this,result.toString(),Toast.LENGTH_LONG).show();
            Log.i(TAG,"GOOGLE STATUS CODE: "+result.getStatus());
        }
    }


    // START Google signIn
    private void startGoogleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void registerGoogleAccount(final String name, final String email,final String imageUrl){
        NetworkApiHelper.getInstance().registerUser(name, email, imageUrl,null,new NetworkApiCallback<RegisterResponse>() {
            @Override
            public void success(RegisterResponse regResp, Response response) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Utils.putUserId(OnBoardingActivity.this, regResp.userId);

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Success)
                        .put("USER_ID", regResp.userId)
                        .build());

                AmplitudeLog.sendUserInfo(regResp.userId);
                gotoNextScreen(regResp.userId);
            }

            @Override
            public void failure(RegisterResponse error) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                        .put("MESSAGE", error.errorMessage)
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Username is taken. Please insert a new username", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(AppConstants.OB_Register_Failure)
                        .put("MESSAGE", error.getMessage())
                        .build());
                Toast.makeText(OnBoardingActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

            }
        });
    }

}
