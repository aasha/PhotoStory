package com.pixtory.app.transformations;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

/**
 * Created by krish on 10/06/2016.
 */
public class ParallaxPagerTransformer implements ViewPager.PageTransformer {

    private int mResourceId;
    private float mSpeedFactor = 0.2f;
    private Context mContext;

    public ParallaxPagerTransformer(Context context,int resourceId, float speedFactor){
        this.mContext = context;
        this.mResourceId = resourceId;
        this.mSpeedFactor = speedFactor;
    }

    public ParallaxPagerTransformer(Context context,int mResourceId){
        this.mContext = context;
        this.mResourceId = mResourceId;
    }

    @Override
    public void transformPage(View page, float position) {
        View parallaxView = page.findViewById(mResourceId);
        if(parallaxView==null)
            Toast.makeText(mContext,"No view to parallax",Toast.LENGTH_SHORT).show();
        else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
            if(position > -1 && position < 1){
                float width = parallaxView.getWidth();
                parallaxView.setTranslationX(-(position*width*mSpeedFactor));
            }
            else
                page.setAlpha(1);
        }
    }
}
