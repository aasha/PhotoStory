package com.pixtory.app.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.pixtory.app.R;
import com.pixtory.app.animations.FlipAnimation;

import java.util.ArrayList;

/**
 * Created by krish on 09/06/2016.
 */
public class ImageArrayAdapter extends ArrayAdapter<Drawable> {
    ArrayList<Drawable> items;
    ImageView menuImage;
    Context context;
    private final int ANIMATION_DURATION = 175;

    public ImageArrayAdapter(Context context,int resources, ArrayList<Drawable> items){
        super(context,resources,items);
        this.context=context;
        this.items=items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_image,parent,false);
        menuImage = (ImageView)convertView.findViewById(R.id.image_menu);
        menuImage.setImageDrawable(items.get(position));
        Animation animation = new FlipAnimation(90,0,0,menuImage.getHeight()/2f);
        int delay = 3*ANIMATION_DURATION*position/items.size();
        //if(position!=items.size()-1)
            animation.setStartOffset(delay);
        animation.setDuration(ANIMATION_DURATION);
        //menuImage.setAnimation(animation);
        //convertView.setAnimation(animation);
        return convertView;
    }
}
