package com.pixtory.app.userprofile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.utils.BlurBuilder;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sriram on 24/05/2016.
 */
public class UserProfileActivity extends Activity{

    private PersonInfo personInfo;
    private ArrayList<ContentData> contentDataList;
    private RecyclerView.LayoutManager gridLayout;
    private RecyclerView recyclerView;
    private CardLayoutAdapter cardLayoutAdapter;

    public UserProfileActivity(PersonInfo personInfo,ArrayList<ContentData> contentDataList){
        this.personInfo = personInfo;
        this.contentDataList = contentDataList;
    }

    public UserProfileActivity(){

    }

    public void dummyFillData(){
        personInfo = new PersonInfo();
        personInfo.desc = "Photographer";
        // personInfo.imageUrl = null;
        personInfo.userId = -1;
        personInfo.name = "Nikhil";

        ContentData tmp = new ContentData();
        tmp.name = "Registan";
        tmp.place = "Samarkand, Uzbekistan";
        contentDataList = new ArrayList<ContentData>();
        for(int i=0;i<10;i++)
            contentDataList.add(tmp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        dummyFillData();

        CircularImageView profileImage = (CircularImageView)findViewById(R.id.person_image);
        CircularImageView profileImageBorder = (CircularImageView)findViewById(R.id.person_image_boarder);
        TextView personName = (TextView)findViewById(R.id.person_name);
        TextView personDesc = (TextView)findViewById(R.id.person_desc);
        ImageView blurrPersonImage = (ImageView)findViewById(R.id.blur_person_image);
        //Picasso.with(this).load(personInfo.imageUrl).fit().into(profileImage);

        //blur the image
        Bitmap blurredimage = BlurBuilder.blur(this, BitmapFactory.decodeResource(getResources(),R.drawable.cardimg));
        blurrPersonImage.setScaleType(ImageView.ScaleType.FIT_XY);
        blurrPersonImage.setImageBitmap(blurredimage);

        Picasso.with(this).load(R.drawable.pixtory).fit().into(profileImage);
        //Picasso.with(this).load().fit().into(blurrPersonImage);
        //BlurBuilder.blur(blurrPersonImage);
        //profileImage.setImageResource(R.drawable.pixtory);
        personName.setText(personInfo.name);
        personDesc.setText(personInfo.desc);
        // blurrPersonImage.setImageResource(R.drawable.pixtory);

        //initialise recyclerview and set its layout as grid layout
        gridLayout = new GridLayoutManager(this,2);
        recyclerView = (RecyclerView)findViewById(R.id.profile_recycler_view);
        recyclerView.setLayoutManager(gridLayout);

        //intialise card layout adapter and set it to recycler view
        cardLayoutAdapter = new CardLayoutAdapter(this,contentDataList);
        recyclerView.setAdapter(cardLayoutAdapter);

        //get the screen the screen dimesions
        DisplayMetrics dm =  new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        //set spacing proportional to width of the the screen (0.063 times the screen width
        double spacing = 0.063*dm.widthPixels;
        SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
        recyclerView.addItemDecoration(decoration);


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
