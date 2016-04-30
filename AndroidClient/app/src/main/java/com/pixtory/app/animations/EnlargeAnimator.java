package com.pixtory.app.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by ajitesh.shukla on 12/28/15.
 */
public class EnlargeAnimator {
    public static void Animate(LinearLayout linearLayout) {
        final AnimatorSet bouncer1 = new AnimatorSet();

        final ObjectAnimator animY0 = ObjectAnimator.ofFloat(linearLayout, "scaleY", 0.0f, 1f);
        final ObjectAnimator animX0 = ObjectAnimator.ofFloat(linearLayout, "scaleX", 0.0f, 1f);

        //bouncer.playTogether(animX, animY);
        bouncer1.playTogether(animX0, animY0);
        bouncer1.setDuration(300);
        bouncer1.setInterpolator(new DecelerateInterpolator());
        bouncer1.start();
        bouncer1.addListener(new Animator.AnimatorListener() {
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
}
