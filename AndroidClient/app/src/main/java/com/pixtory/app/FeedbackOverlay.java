package com.pixtory.app;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.pixtory.app.animations.EnlargeAnimator;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ajitesh.shukla on 1/11/16.
 */
public class FeedbackOverlay {
    private static Context context;
    private static LinearLayout linearLayout;
    private static String SCREEN_NAME = "Feedback";
    private static String Feedback_Page_close ="Feedback_Page_close";
    private static String Feedback_Page_submit ="Feedback_Page_submit";
    private static String Feedback_Page_cancel ="Feedback_Page_cancel";
    private static String Feedback_Page_open ="Feedback_Page_open";

    public static boolean isOverlayDrawn() {
        if (FeedbackOverlay.context != null) {
            return true;
        }
        return false;
    }

    public static void drawFullscreenOverlay(final Context context) {
        if (isOverlayDrawn()) {
            //overlay already present return
            return;
        } else {

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            linearLayout = (LinearLayout) inflater.inflate(R.layout.feedback_layout, null);

            final Spinner spinnerTop = (Spinner) linearLayout.findViewById(R.id.spinnertop);
            ArrayAdapter<CharSequence> adapterTop = ArrayAdapter.createFromResource(context, R.array.feedback_array,
                            android.R.layout.simple_spinner_item);
            adapterTop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTop.setAdapter(adapterTop);

            final Spinner spinnerMiddle = (Spinner) linearLayout.findViewById(R.id.spinnermiddle);
            ArrayAdapter<CharSequence> adapterMiddle = ArrayAdapter.createFromResource(context, R.array.category_array,
                    android.R.layout.simple_spinner_item);
            adapterMiddle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMiddle.setAdapter(adapterMiddle);

            final Spinner spinnerBottom = (Spinner) linearLayout.findViewById(R.id.spinnerbottom);
            ArrayAdapter<CharSequence> adapterBottom = ArrayAdapter.createFromResource(context, R.array.subcategory_array,
                    android.R.layout.simple_spinner_item);
            adapterBottom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBottom.setAdapter(adapterBottom);

            //Add close button
            final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(linearLayout, params);

            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.close);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wm.removeView(linearLayout);
                    //mOverlayView = null;
                    linearLayout = null;
                    FeedbackOverlay.context = null;
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Feedback_Page_close)
                            .put(AppConstants.USER_ID, Utils.getUserId(context))
                            .build());
                }
            });

            final EditText details = (EditText)linearLayout.findViewById(R.id.enterdetail);
            EnlargeAnimator.Animate(linearLayout);
            FeedbackOverlay.context = context;

            Button submit = (Button)linearLayout.findViewById(R.id.submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String feedBack = spinnerTop.getSelectedItem().toString();
                    String category = spinnerMiddle.getSelectedItem().toString();
                    String subCat = spinnerBottom.getSelectedItem().toString();
                    String desc = details.getText().toString();
                    NetworkApiHelper.getInstance().getCommentDetailList(Utils.getUserId(context), feedBack, category, subCat, desc, new NetworkApiCallback<BaseResponse>() {
                        @Override
                        public void success(BaseResponse o, Response response) {

                        }

                        @Override
                        public void failure(BaseResponse error) {

                        }

                        @Override
                        public void networkFailure(RetrofitError error) {
                        }
                    });
                    wm.removeView(linearLayout);
                    //mOverlayView = null;
                    linearLayout = null;
                    FeedbackOverlay.context = null;
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( Feedback_Page_submit)
                            .put(AppConstants.USER_ID, Utils.getUserId(context))
                            .build());
                }
            });

            Button cancel = (Button)linearLayout.findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wm.removeView(linearLayout);
                    //mOverlayView = null;
                    linearLayout = null;
                    FeedbackOverlay.context = null;
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( Feedback_Page_cancel)
                            .put(AppConstants.USER_ID, Utils.getUserId(context))
                            .build());
                }
            });
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder( Feedback_Page_open)
                    .put(AppConstants.USER_ID, Utils.getUserId(context))
                    .build());
        }
    }

    public static void removeFullscreenOverlay() {
        if (!FeedbackOverlay.isOverlayDrawn()) {
            //not present return
            return;
        } else {
            //write logic to remove overlay
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            //wm.removeView(mOverlayView);
            wm.removeView(linearLayout);
            //mOverlayView = null;
            linearLayout = null;
            context = null;
        }
    }
}
