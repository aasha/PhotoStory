package com.pixtory.app.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by ajitesh.shukla on 1/10/16.
 */
public class RotateAnimator {

    public static void animate(ImageView imageView/*, final ImageView imageView2, final ImageView imageView3*/) {

        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f).setDuration(3500);
        ObjectAnimator objectAnimatorScaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.6f, 1.0f)
                .setDuration(2500);
        objectAnimatorScaleX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator objectAnimatorScaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.6f, 1.0f)
                .setDuration(2500);
        objectAnimatorScaleY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0, 360,
                Animation.RELATIVE_TO_SELF, 0.55f, Animation.RELATIVE_TO_SELF, 0.55f).setDuration(3500);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(objectAnimatorAlpha, objectAnimatorScaleX, objectAnimatorScaleY, objectAnimator);
        animatorSet.start();
    }

    public static void animateScreen2(ImageView imageView, float initValue, float finalValue) {
        imageView.setVisibility(View.VISIBLE);
        RotateAnimation rotateAnimation = new RotateAnimation(initValue, finalValue, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.3f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatMode(Animation.REVERSE);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        imageView.startAnimation(rotateAnimation);
    }
}
