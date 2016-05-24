package com.pixtory.app.userprofile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
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

    public void dummyFillData(){
        personInfo.desc = "Photographer";
        personInfo.imageUrl = null;
        personInfo.userId = -1;
        personInfo.name = "Nikhil";

        ContentData tmp = new ContentData();
        tmp.name = "Registan";
        tmp.place = "Samarkand, Uzbekistan";

        for(int i=0;i<10;i++)
            contentDataList.add(tmp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        dummyFillData();

        CircularImageView profileImage = (CircularImageView)findViewById(R.id.Profile_Image);
        TextView personName = (TextView)findViewById(R.id.Person_Name);
        TextView personDesc = (TextView)findViewById(R.id.Person_Desc);
        //Picasso.with(this).load(personInfo.imageUrl).fit().into(profileImage);
        profileImage.setImageResource(R.drawable.pixtory);
        personName.setText(personInfo.name);
        personDesc.setText(personInfo.desc);

        gridLayout = new GridLayoutManager(this,2);
        recyclerView = (RecyclerView)findViewById(R.id.profile_recycler_view);

        recyclerView.setLayoutManager(gridLayout);

        cardLayoutAdapter = new CardLayoutAdapter(this,contentDataList);
        recyclerView.setAdapter(cardLayoutAdapter);
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
