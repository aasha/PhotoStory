package com.pixtory.app.userprofile;

/**
 * Created by sriram on 24/05/2016.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.model.ContentData;

import java.util.ArrayList;


public class CardLayoutAdapter extends RecyclerView.Adapter<CardLayoutAdapter.ViewHolder>{

    private ArrayList<ContentData> cDlist;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView cardTitle;
        public TextView cardPlace;
        public ImageView cardImage;

        public ViewHolder(View v){
            super(v);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardlayout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void onBindViewHolder(ViewHolder holder, int position){
        //holder.cardImage.setImageURI(cDlist.get(position).pictureUrl);
        holder.cardTitle.setText(cDlist.get(position).name);
        holder.cardPlace.setText(cDlist.get(position).place);
    }

    public int getItemCount(){
        return cDlist.size();
    }
}
