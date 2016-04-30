package com.pixtory.app.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajitesh.shukla on 1/9/16.
 */
public class ScaleAnimator {
    public static void animate(ImageView imageView, final ImageView imageView2/*, final ImageView imageView3*/) {

        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet animatorSet = new AnimatorSet();
        final AnimatorSet animatorSetFinal = new AnimatorSet();
        List<Animator> objectAnimatorList = new ArrayList<>();
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.0f, 1.0f).setDuration(2000);
        objectAnimatorX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.0f, 1.0f).setDuration(2000);
        objectAnimatorY.setInterpolator(new AccelerateInterpolator());
        objectAnimatorList.add(objectAnimatorX);
        objectAnimatorList.add(objectAnimatorY);
        animatorSet.playTogether(objectAnimatorList);
        animatorSet.start();
        RotateAnimator.animate(imageView2);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public static void animate(ImageView imageView, float initVal, float finVal) {

        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet animatorSet = new AnimatorSet();
        final AnimatorSet animatorSetFinal = new AnimatorSet();
        List<Animator> objectAnimatorList = new ArrayList<>();
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(imageView, "scaleX", initVal, finVal).setDuration(3000);
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(imageView, "scaleY", initVal, finVal).setDuration(3000);
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f).setDuration(3000);
        objectAnimatorList.add(objectAnimatorX);
        objectAnimatorList.add(objectAnimatorY);
        objectAnimatorList.add(objectAnimatorAlpha);
        animatorSet.playTogether(objectAnimatorList);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public static void animate(ImageView imageView, final ImageView imageViewGlasses, float initVal, float finVal) {

        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet animatorSet = new AnimatorSet();
        final AnimatorSet animatorSetFinal = new AnimatorSet();
        List<Animator> objectAnimatorList = new ArrayList<>();
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(imageView, "scaleX", initVal, finVal).setDuration(1500);
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(imageView, "scaleY", initVal, finVal).setDuration(1500);
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f).setDuration(1500);
        objectAnimatorList.add(objectAnimatorX);
        objectAnimatorList.add(objectAnimatorY);
        objectAnimatorList.add(objectAnimatorAlpha);
        animatorSet.playTogether(objectAnimatorList);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //RotateAnimator.animate(imageView2);
                RotateAnimator.animateScreen2(imageViewGlasses, -30f, 30f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public static void animateScreen2(ImageView imageViewExt, ImageView imageViewMobile, ImageView imageViewGlasses) {
        animate(imageViewExt, imageViewGlasses, 0.0f, 1.0f);
        //Animation fadeIn = new AlphaAnimation(0, 1);
        //fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        //fadeIn.setDuration(3000);
        //imageViewExt.startAnimation(fadeIn);
        //imageViewExt.setVisibility(View.VISIBLE);
        //animate(imageViewGlasses, 0.0f, 1.0f);
        animate(imageViewMobile, 0.0f, 1.0f);
    }
}
