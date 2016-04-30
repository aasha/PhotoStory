package com.pixtory.app.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.pixtory.app.R;
import com.pixtory.app.animations.BounceAnimator;
import com.pixtory.app.utils.AmplitudeLog;

/**
 * Created by ajitesh.shukla on 9/10/15.
 */
public class ScreenSlidePageFragment3 extends Fragment {
    private static View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding_fragment_layout3, container, false);
        ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
        ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
        //com.inmobi.pixtory.ScaleAnimator.animate(/*imageViewInt, */imageViewExt);
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && rootView!=null) {
            ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            //com.inmobi.pixtory.ScaleAnimator.animate(/*imageViewInt, */imageViewExt);
            BounceAnimator.Animate(imageViewExt);
            ScreenSlidePageFragment.removeImageViews();
            ScreenSlidePageFragment2.removeImageViews();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OnBoardingActivity.OB_Card_Swipe)
                    .put("SCREEN", "3")
                    .build());
        }
    }

    public static void removeImageViews() {
        if(rootView!=null) {
            ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            imageViewExt.setVisibility(View.INVISIBLE);
            imageViewInt.setVisibility(View.INVISIBLE);
        }
    }
}
