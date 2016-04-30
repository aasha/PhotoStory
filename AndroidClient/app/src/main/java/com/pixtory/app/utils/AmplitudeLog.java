package com.pixtory.app.utils;


import com.amplitude.api.Amplitude;
import org.json.JSONObject;

import java.util.HashMap;

public class AmplitudeLog {

    public static void logEvent(AppEvent ev) {
        Amplitude.getInstance().logEvent(ev.label, ev.properties);
    }

    private static class AppEvent {
        JSONObject properties = null;
        String label = null;
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
        }

        public AppEventBuilder put(String key, String value) {
            data.put(key, value);
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
}
