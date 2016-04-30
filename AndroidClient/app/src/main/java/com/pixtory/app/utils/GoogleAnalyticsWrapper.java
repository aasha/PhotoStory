package com.pixtory.app.utils;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pixtory.app.app.App;

import java.util.HashMap;

public class GoogleAnalyticsWrapper {

    public static void logEvent(AppEvent ev) {
        Tracker tracker = App.getmInstance().getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(ev.category)
                .setAction(ev.action)
                .setLabel(ev.label)
                .build());
    }

    private static class AppEvent {
        String category = null;
        String action = null;
        String label = null;
    }

    public static void sendUserInfo(String userId){
        Tracker t = App.getmInstance().getDefaultTracker();

        // You only need to set User ID on a tracker once. By setting it on the tracker, the ID will be
        // sent with all subsequent hits.
        t.set("&uid", userId);

        // This hit will be sent with the User ID value and be visible in User-ID-enabled views (profiles).
        t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign In").build());
    }
    public static class AppEventBuilder {

        HashMap<String, String> data = null;
        AppEvent appEvent = null;

        private AppEventBuilder() {

        }

        public AppEventBuilder(String category, String action) {
            this();

            appEvent = new AppEvent();
            appEvent.category = category;
            appEvent.action = action;
            data = new HashMap<String, String>();
        }

        public AppEventBuilder put(String key, String value) {
            data.put(key, value);
            return this;
        }

        public AppEvent build() {
            StringBuilder labelBuilder = new StringBuilder();
            try {
                for (String key : data.keySet()) {
                    labelBuilder.append(key + ":" + data.get(key) + ",");
                }
                appEvent.label = labelBuilder.substring(0, labelBuilder.length() - 2);
            }catch (Exception e){
                e.printStackTrace();
            }
            return appEvent;
        }
    }
}
