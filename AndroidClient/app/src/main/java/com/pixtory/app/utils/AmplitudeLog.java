package com.pixtory.app.utils;


import android.os.Bundle;

import com.amplitude.api.Amplitude;
import com.pixtory.app.app.App;

import org.json.JSONObject;

import java.util.HashMap;

public class AmplitudeLog {

    public static void logEvent(AppEvent ev) {
        Amplitude.getInstance().logEvent(ev.label, ev.properties);
        App.getmFirebaseAnalytics().logEvent(ev.label,ev.params);
    }

    private static class AppEvent {
        JSONObject properties = null;
        String label = null;
        Bundle params = null;
    }


    public static void sendUserInfo(String userId){
        Amplitude.getInstance().setUserId(userId);
    }
    public static class AppEventBuilder {

        HashMap<String, String> data = null;
        AppEvent appEvent = null;

        private AppEventBuilder() {

        }

        public AppEventBuilder(String label) {
            this();

            appEvent = new AppEvent();
            appEvent.label = label;
            data = new HashMap<String, String>();
            appEvent.params = new Bundle();
        }

        public AppEventBuilder put(String key, String value) {
            data.put(key, value);
            appEvent.params.putString(key, value);
            return this;
        }

        public AppEvent build() {
            JSONObject labelBuilder = new JSONObject();
            try {
                for (String key : data.keySet()) {
                    labelBuilder.put(key, data.get(key));
                }
                appEvent.properties = labelBuilder;
            }catch (Exception e){
                e.printStackTrace();
            }
            return appEvent;
        }
    }

    public static void startSession(){
        Amplitude.startSession();
    }

    public static void endSession(){
        Amplitude.endSession();
    }
}
