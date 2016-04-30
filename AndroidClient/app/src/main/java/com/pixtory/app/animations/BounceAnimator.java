package com.pixtory.app.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by ajitesh.shukla on 12/23/15.
 */
public final class BounceAnimator {
    public static void Animate(final ImageView imageView, final ImageView imageView2) {
        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet bouncer1 = new AnimatorSet();
        final AnimatorSet bouncer2 = new AnimatorSet();
        final AnimatorSet bouncer3 = new AnimatorSet();
        final AnimatorSet bouncer4 = new AnimatorSet();
        final AnimatorSet bouncer5 = new AnimatorSet();

        final ObjectAnimator animY0 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.0f, 1f);
        final ObjectAnimator animX0 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.0f, 1f);

        final ObjectAnimator animY1 = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.7f);
        final ObjectAnimator animX1 = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.7f);

        final ObjectAnimator animY2 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.7f, 1f);
        final ObjectAnimator animX2 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.7f, 1f);

        final ObjectAnimator animY3 = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.9f);
        final ObjectAnimator animX3 = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.9f);

        final ObjectAnimator animY4 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.9f, 1.0f);
        final ObjectAnimator animX4 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.9f, 1.0f);

        //bouncer.playTogether(animX, animY);
        bouncer1.playTogether(animX0, animY0);
        bouncer1.setDuration(200);
        bouncer1.setInterpolator(new DecelerateInterpolator());
        bouncer1.start();
        bouncer1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bouncer2.playTogether(animX1, animY1);
                bouncer2.setDuration(220);
                bouncer2.setInterpolator(new DecelerateInterpolator());
                bouncer2.start();
                bouncer2.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bouncer3.playTogether(animX2, animY2);
                        bouncer3.setDuration(240);
                        bouncer3.setInterpolator(new DecelerateInterpolator());
                        bouncer3.start();
                        bouncer3.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                bouncer4.playTogether(animX3, animY3);
                                bouncer4.setDuration(260);
                                bouncer4.setInterpolator(new DecelerateInterpolator());
                                bouncer4.start();
                                bouncer4.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        bouncer5.playTogether(animX4, animY4);
                                        bouncer5.setDuration(280);
                                        bouncer5.setInterpolator(new DecelerateInterpolator());
                                        bouncer5.start();
                                        bouncer5.addListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                BounceAnimator.Animate(imageView2);
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public static void Animate(final ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        final AnimatorSet bouncer1 = new AnimatorSet();
        final AnimatorSet bouncer2 = new AnimatorSet();
        final AnimatorSet bouncer3 = new AnimatorSet();
        final AnimatorSet bouncer4 = new AnimatorSet();
        final AnimatorSet bouncer5 = new AnimatorSet();

        final ObjectAnimator animY0 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.0f, 1f);
        final ObjectAnimator animX0 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.0f, 1f);

        final ObjectAnimator animY1 = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.6f);
        final ObjectAnimator animX1 = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.6f);

        final ObjectAnimator animY2 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.6f, 1f);
        final ObjectAnimator animX2 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.6f, 1f);

        final ObjectAnimator animY3 = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.8f);
        final ObjectAnimator animX3 = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.8f);

        final ObjectAnimator animY4 = ObjectAnimator.ofFloat(imageView, "scaleY", 0.8f, 0.9f);
        final ObjectAnimator animX4 = ObjectAnimator.ofFloat(imageView, "scaleX", 0.8f, 0.9f);

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
                bouncer2.playTogether(animX1, animY1);
                bouncer2.setDuration(320);
                bouncer2.setInterpolator(new DecelerateInterpolator());
                bouncer2.start();
                bouncer2.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bouncer3.playTogether(animX2, animY2);
                        bouncer3.setDuration(340);
                        bouncer3.setInterpolator(new DecelerateInterpolator());
                        bouncer3.start();
                        bouncer3.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                bouncer4.playTogether(animX3, animY3);
                                bouncer4.setDuration(360);
                                bouncer4.setInterpolator(new DecelerateInterpolator());
                                bouncer4.start();
                                bouncer4.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        bouncer5.playTogether(animX4, animY4);
                                        bouncer5.setDuration(380);
                                        bouncer5.setInterpolator(new DecelerateInterpolator());
                                        bouncer5.start();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
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
