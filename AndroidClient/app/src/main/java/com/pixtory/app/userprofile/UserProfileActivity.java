package com.pixtory.app.userprofile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.transformations.BlurTransformation;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.BlurBuilder;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by sriram on 24/05/2016.
 */
public class UserProfileActivity extends Activity{

    private PersonInfo personInfo;
    private ArrayList<ContentData> contentDataList;
    private RecyclerView.LayoutManager gridLayout;
    private RecyclerView recyclerView;
    private CardLayoutAdapter cardLayoutAdapter;
    private int personId;
    private int userId;
    private static final String Get_Person_Details_Done = "Get_Person_Details_Done";
    private static final String Get_Person_Details_Failed = "Get_Person_Details_Failed";

    public UserProfileActivity(PersonInfo personInfo,ArrayList<ContentData> contentDataList){
        this.personInfo = personInfo;
        this.contentDataList = contentDataList;
    }

    public UserProfileActivity(){

    }

    private class GetBitmapFromUrl extends AsyncTask<String,Void,Bitmap>{
        String tsrc;

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(tsrc);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                //connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (java.net.MalformedURLException e) {
                // Log exception
                return null;
            } catch (IOException e){

                return null;
            }
        }

        public GetBitmapFromUrl(String src) {
            super();
            tsrc = src;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView blurImg = (ImageView)findViewById(R.id.blur_person_image);
            blurImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            blurImg.setImageBitmap(BlurBuilder.blur(UserProfileActivity.this,bitmap));

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        Bundle extras = getIntent().getExtras();
        userId = Integer.parseInt(extras.getString("USER_ID"));
        personId = Integer.parseInt(extras.getString("PERSON_ID"));
        //personId = 463896090;

        final CircularImageView profileImage = (CircularImageView)findViewById(R.id.person_image);
        CircularImageView profileImageBorder = (CircularImageView)findViewById(R.id.person_image_boarder);
        final TextView personName = (TextView)findViewById(R.id.person_name);
        final TextView personDesc = (TextView)findViewById(R.id.person_desc);
        final ImageView blurrPersonImage = (ImageView)findViewById(R.id.blur_person_image);
        ImageView backImage = (ImageView)findViewById(R.id.back_img);
        LinearLayout backClick = (LinearLayout)findViewById(R.id.back_click);

        /*
        final Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap blurredimage = BlurBuilder.blur(UserProfileActivity.this, bitmap);
                blurrPersonImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                blurrPersonImage.setImageBitmap(blurredimage);// Do whatever you want with the Bitmap
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(UserProfileActivity.this,"Blur failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        */

        personInfo = new PersonInfo();
        contentDataList = new ArrayList<ContentData>();
        NetworkApiHelper.getInstance().getPersonDetails(userId, personId,new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {

                if (o.contentList != null) {
                    contentDataList = o.contentList;
               }
                else {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                            .put(AppConstants.USER_ID,Integer.toString(personId))
                            .put("MESSAGE", "No Data")
                            .build());
                    Toast.makeText(UserProfileActivity.this, "No content data!", Toast.LENGTH_SHORT).show();
                }

                if (o.personDetails!=null){
                    personInfo = o.personDetails;
                    personName.setText(personInfo.name);
                    personDesc.setText(personInfo.desc);

                    if(personInfo.imageUrl!=null)
                    {
                        Picasso.with(UserProfileActivity.this).load(personInfo.imageUrl).fit().centerCrop().transform(new BlurTransformation(UserProfileActivity.this, 10)).into(blurrPersonImage);
                        Picasso.with(UserProfileActivity.this).load(personInfo.imageUrl).fit().into(profileImage);
                    }else {
                        Picasso.with(UserProfileActivity.this).load("http://pixtory.in/assets/img/story-2-image.png").fit().centerCrop().transform(new BlurTransformation(UserProfileActivity.this, 10)).into(blurrPersonImage);
                        Picasso.with(UserProfileActivity.this).load(R.drawable.sample_pimg).fit().into(profileImage);
                    }
                }else {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                            .put(AppConstants.USER_ID,Integer.toString(personId))
                            .put("MESSAGE", "No Data")
                            .build());
                    System.out.println("Person data null");
                    Toast.makeText(UserProfileActivity.this, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {

                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                        .put(AppConstants.USER_ID, Integer.toString(personId))
                        .put("MESSAGE", error.errorMessage)
                        .build());
                Toast.makeText(UserProfileActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                //mProgress.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                        .put(AppConstants.USER_ID,Integer.toString(personId))
                        .put("MESSAGE", error.getMessage())
                        .build());
                Toast.makeText(UserProfileActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });

        //initialise recyclerview and set its layout as grid layout
        gridLayout = new GridLayoutManager(this,2);
        recyclerView = (RecyclerView)findViewById(R.id.profile_recycler_view);
        recyclerView.setLayoutManager(gridLayout);

        //intialise card layout adapter and set it to recycler view
        cardLayoutAdapter = new CardLayoutAdapter(this, App.getContentData());
        recyclerView.setAdapter(cardLayoutAdapter);

        //get the screen the screen dimesions
        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        //set spacing proportional to width of the the screen (0.063 times the screen width
        double spacing = 0.063*dm.widthPixels;
        SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
        recyclerView.addItemDecoration(decoration);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }});

        backClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }});
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
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
