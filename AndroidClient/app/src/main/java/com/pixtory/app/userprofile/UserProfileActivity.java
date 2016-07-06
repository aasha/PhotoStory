package com.pixtory.app.userprofile;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.fragments.CommentsDialogFragment;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.Utils;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserProfileActivity extends FragmentActivity implements UserProfileFragment.OnFragmentInteractionListener, MainFragment.OnMainFragmentInteractionListener
, CommentsDialogFragment.OnAddCommentButtonClickListener{

    private MainFragment mainFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profle_2);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            UserProfileFragment userProfileFragment = new UserProfileFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            userProfileFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, userProfileFragment).commit();
        }
    }
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

    public void switchFragment(MainFragment fragment){
        this.mainFragment=fragment;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment,"MAIN_FRAGMENT");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void showMenuIcon(boolean showMenuIcon) {

    }

    @Override
    public void showLoginAlert(){
        final Dialog dialog = new Dialog(UserProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.login_alert);

        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int)(0.9*dm.widthPixels);
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setLayout(lp.width,lp.height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearLayout loginClick = (LinearLayout) dialog.findViewById(R.id.login_click);
        loginClick.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LoginManager.getInstance().logInWithReadPermissions(UserProfileActivity.this, AppConstants.mFBPermissions);
                setPersonDetails();
            }
        });

        dialog.show();
    }

    @Override
    public void onAddCommentButtonClicked(String str) {
        if(mainFragment!=null)
            mainFragment.postComment(str);
    }

    private void setPersonDetails(){

        NetworkApiHelper.getInstance().getPersonDetails(Integer.parseInt(Utils.getUserId(UserProfileActivity.this)), Integer.parseInt(Utils.getUserId(UserProfileActivity.this)),new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {

                if (o.contentList != null) {
                    App.setPersonConentData(o.contentList);
                } else {
                    Toast.makeText(UserProfileActivity.this, "No Person content data!", Toast.LENGTH_SHORT).show();

                }

                if (o.userDetails!=null){
                    App.setPersonInfo(o.userDetails);
                }else {
                    System.out.println("Person data null");
                    Toast.makeText(UserProfileActivity.this, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {
                // mProgress.dismiss();

                Toast.makeText(UserProfileActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Toast.makeText(UserProfileActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showCategoryStories(){

    }

}