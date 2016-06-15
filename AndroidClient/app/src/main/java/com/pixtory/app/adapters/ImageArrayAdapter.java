package com.pixtory.app.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.animations.FlipAnimation;
import com.pixtory.app.model.SideMenuData;

import java.util.ArrayList;

/**
 * Created by krish on 09/06/2016.
 */
public class ImageArrayAdapter extends ArrayAdapter<SideMenuData> {
    ArrayList<SideMenuData> items;
    ImageView menuImage;
    TextView menuText;
    Context context;
    int height;
    int itemHeight;
    private final int ANIMATION_DURATION = 175;

    public ImageArrayAdapter(Context context,int resources, ArrayList<SideMenuData> items,int height){
        super(context,resources,items);
        this.context=context;
        this.items=items;
        this.height = height;
        itemHeight = height/6;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_image,parent,false);
        menuImage = (ImageView)convertView.findViewById(R.id.image_menu);
        menuText = (TextView)convertView.findViewById(R.id.menu_text);
        menuImage.setImageDrawable(items.get(position).drawable);
        if(position!=0)
            menuText.setText(items.get(position).menuText);
        else
            menuText.setVisibility(View.GONE);
        Animation animation = new FlipAnimation(90,0,0,menuImage.getHeight()/2f);
        /*
        int delay = 3*ANIMATION_DURATION*position/items.size();
        if(position!=items.size()-1)
            animation.setStartOffset(delay);
        animation.setDuration(ANIMATION_DURATION);
        menuImage.setAnimation(animation);
        convertView.setAnimation(animation);
        */
        if(position==0){
            ViewGroup.LayoutParams layoutParams = menuImage.getLayoutParams();
            layoutParams.height = (int)(0.7*layoutParams.height);
            layoutParams.width = (int)(0.7*layoutParams.width);
            menuImage.setLayoutParams(layoutParams);
        }



        ViewGroup.LayoutParams lp = convertView.getLayoutParams();
        lp.height = itemHeight;
        if(position==items.size()-1){
            convertView.findViewById(R.id.menu_item_seperator).setVisibility(View.GONE);
            lp.height = height - 5*itemHeight;
        }
        convertView.setLayoutParams(lp);
        return convertView;
    }
}
