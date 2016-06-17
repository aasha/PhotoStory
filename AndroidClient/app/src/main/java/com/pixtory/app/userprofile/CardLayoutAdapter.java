package com.pixtory.app.userprofile;

/**
 * Created by sriram on 24/05/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.model.ContentData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit.http.HEAD;


public class CardLayoutAdapter extends RecyclerView.Adapter<CardLayoutAdapter.ViewHolder>{

    private ArrayList<ContentData> cDlist;
    private Context context;
    private StoryFragment storyFragment;
    private String ARG_PARAM1 = "PROFILE_CONTENT";
    private String ARG_PARAM2 = "CONTENT_INDEX";


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView cardTitle;
        public TextView cardPlace;
        public ImageView cardImage;
        public FrameLayout cardFrame;

        public ViewHolder(View v){
            super(v);
            cardFrame = (FrameLayout)v.findViewById(R.id.card_frame);
            cardImage = (ImageView)v.findViewById(R.id.card_img);
            cardTitle = (TextView)v.findViewById(R.id.card_title);
            cardPlace = (TextView)v.findViewById(R.id.card_place);
        }
    }

    public CardLayoutAdapter(Context context,ArrayList<ContentData> cardList){
        cDlist = cardList;
        this.context = context;
    }

    public CardLayoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void onBindViewHolder(ViewHolder holder, final int position){



        Picasso.with(context).load(cDlist.get(position).pictureUrl).fit().centerCrop().into(holder.cardImage);
        //holder.cardImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.cardTitle.setText(cDlist.get(position).name);

        // holder.cardTitle.setText("Pixtory, every picture has a story to tell");

        holder.cardPlace.setText(cDlist.get(position).place);
        //holder.cardPlace.setText("Bengaluru, India");
        //holder.cardImage.setImageResource(R.drawable.cardimg);

        //Picasso.with(context).load(R.drawable.cardimg).fit().into(holder.cardImage);

        holder.cardFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                storyFragment = new StoryFragment();
                Bundle args = new Bundle();
                args.putBoolean(ARG_PARAM1,true);
                args.putInt(ARG_PARAM2,position);
                storyFragment.setArguments(args);
                if(context!=null && context instanceof UserProfileActivity2){
                    UserProfileActivity2 userProfileActivity2 = (UserProfileActivity2)context;
                    userProfileActivity2.switchFragment(storyFragment);
                }
            }
        });
    }

    public int getItemCount(){

        return cDlist.size();
    }
}
