package com.pixtory.app.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.pixtory.app.R;
import com.pixtory.app.animations.ScaleAnimator;
import com.pixtory.app.utils.AmplitudeLog;

/**
 * Created by ajitesh.shukla on 9/10/15.
 */
public class ScreenSlidePageFragment2 extends Fragment {
    private static View rootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding_fragment_layout2, container, false);
        //ImageView imageViewInt = (ImageView) rootView.findViewById(R.userId.badge_view);
        //ImageView imageViewExt = (ImageView) rootView.findViewById(R.userId.badge_view_ext);
        //com.inmobi.pixtory.ScaleAnimator.animate(/*imageViewInt, */imageViewExt);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && rootView!=null) {
            //ImageView imageViewInt = (ImageView) rootView.findViewById(R.userId.badge_view);
            //ImageView imageViewExt = (ImageView) rootView.findViewById(R.userId.badge_view_ext);
            //com.inmobi.pixtory.ScaleAnimator.animate(/*imageViewInt, */imageViewExt);
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            ImageView imageViewMobile = (ImageView) rootView.findViewById(R.id.badge_view_mobile);
            ImageView imageViewGlasses = (ImageView) rootView.findViewById(R.id.badge_view_glasses);
            //BounceAnimator.Animate(imageViewExt);
            ScaleAnimator.animateScreen2(imageViewExt, imageViewMobile, imageViewGlasses);
            ScreenSlidePageFragment.removeImageViews();
            ScreenSlidePageFragment3.removeImageViews();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( OnBoardingActivity.OB_Card_Swipe)
                    .put("SCREEN", "2")
                    .build());
        }
    }

    public static void removeImageViews() {
        if(rootView!=null) {
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            ImageView imageViewMobile = (ImageView) rootView.findViewById(R.id.badge_view_mobile);
            ImageView imageViewGlasses = (ImageView) rootView.findViewById(R.id.badge_view_glasses);
            imageViewExt.setVisibility(View.INVISIBLE);
            imageViewMobile.setVisibility(View.INVISIBLE);
            imageViewGlasses.setVisibility(View.INVISIBLE);
            imageViewGlasses.clearAnimation();
        }
    }
}
