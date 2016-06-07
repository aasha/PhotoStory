package com.pixtory.app.transformations;

import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sriram on 07/06/2016.
 */
public class PagerParallaxTransformer implements ViewPager.PageTransformer {

    private List<ParallaxTransformParameters> mViewsToParallax
            = new ArrayList<ParallaxTransformParameters>();

    public PagerParallaxTransformer() {
    }

    public PagerParallaxTransformer(List<ParallaxTransformParameters> viewsToParallax) {
        mViewsToParallax = viewsToParallax;
    }

    public PagerParallaxTransformer addViewToParallax(
            ParallaxTransformParameters viewInfo) {
        if (mViewsToParallax != null) {
            mViewsToParallax.add(viewInfo);
        }
        return this;
    }

    public void transformPage(View view, float position) {

        int pageWidth = view.getWidth();

        if (position < -1) {
            // This page is way off-screen to the left.
            view.setAlpha(1);

        } else if (position <= 1 && mViewsToParallax != null) { // [-1,1]
            for (ParallaxTransformParameters parallaxTransformInformation : mViewsToParallax) {
                applyParallaxEffect(view, position, pageWidth, parallaxTransformInformation,
                        position > 0);
            }
        } else {
            // This page is way off-screen to the right.
            view.setAlpha(1);
        }
    }

    private void applyParallaxEffect(View view, float position, int pageWidth,
                                     ParallaxTransformParameters information, boolean isEnter) {
        if (information.isValid() && view.findViewById(information.resourceId) != null) {
            if (isEnter && !information.isEnterDefault()) {
                view.findViewById(information.resourceId)
                        .setTranslationX(-position * (pageWidth / information.parallaxEnterEffect));
            } else if (!isEnter && !information.isExitDefault()) {
                view.findViewById(information.resourceId)
                        .setTranslationX(-position * (pageWidth / information.parallaxExitEffect));
            }
        }
    }


    /**
     * Parameters to make the parallax effect in a concrete view.
     */
    public static class ParallaxTransformParameters {

        public static final float DEFAULT_PARALLAX_EFFECT = -101.1986f;

        int resourceId = -1;
        float parallaxEnterEffect = 1f;
        float parallaxExitEffect = 1f;

        public ParallaxTransformParameters(int resource, float parallaxEnterEffect,
                                           float parallaxExitEffect) {
            this.resourceId = resource;
            this.parallaxEnterEffect = parallaxEnterEffect;
            this.parallaxExitEffect = parallaxExitEffect;
        }

        public boolean isValid() {
            return parallaxEnterEffect != 0 && parallaxExitEffect != 0 && resourceId != -1;
        }

        public boolean isEnterDefault() {
            return parallaxEnterEffect == DEFAULT_PARALLAX_EFFECT;
        }

        public boolean isExitDefault() {
            return parallaxExitEffect == DEFAULT_PARALLAX_EFFECT;
        }
    }
}