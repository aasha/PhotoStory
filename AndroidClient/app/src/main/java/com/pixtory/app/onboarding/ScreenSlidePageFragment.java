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
public class ScreenSlidePageFragment extends Fragment {
    private static View rootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        boolean flagPrevState = false;
        if (rootView != null)
            flagPrevState = true;
        rootView = inflater.inflate(R.layout.onboarding_fragment_layout1, container, false);
        ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
        ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
        if (!flagPrevState) {
            ScaleAnimator.animate(imageViewInt, imageViewExt);
        } else {
            imageViewExt.setVisibility(View.INVISIBLE);
            imageViewInt.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && rootView != null) {
            ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            ScaleAnimator.animate(imageViewInt, imageViewExt);
            ScreenSlidePageFragment2.removeImageViews();
            ScreenSlidePageFragment3.removeImageViews();
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(OnBoardingActivity.OB_Card_Swipe)
                    .put("SCREEN", "1")
                    .build());
        }
    }

    public static void removeImageViews() {
        if (rootView != null) {
            ImageView imageViewInt = (ImageView) rootView.findViewById(R.id.badge_view);
            ImageView imageViewExt = (ImageView) rootView.findViewById(R.id.badge_view_ext);
            imageViewExt.setVisibility(View.INVISIBLE);
            imageViewInt.setVisibility(View.INVISIBLE);
        }
    }
}
