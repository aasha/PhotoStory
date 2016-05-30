package com.pixtory.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by aasha.medhi on 08/10/15.
 */
public class Utils {

    public static void deleteFile(String fileUrl) {
        File file = new File(fileUrl);
        file.delete();
    }


    public static String getContentString(ContentData cd) {
        Gson gson = new Gson();
        String str = gson.toJson(cd);
        return str;
    }

    public static ContentData getContentObject(String contentJson) {
        Gson gson = new Gson();
        ContentData cd = gson.fromJson(contentJson, ContentData.class);
        return cd;
    }


    public static boolean nextVideosAvailable() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if (hours > 18)
            return true;
        return false;
    }

    public static boolean isConnectedViaWifi(Context ctxt) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

//    public static boolean nextVideosReceived() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        int hours = calendar.get(Calendar.HOUR_OF_DAY);
//        if (hours > 19)
//            return true;
//        return false;
//    }

    public static void deleteOldVideos(List<ContentData> newVideoList) {
        List<String> paths = new ArrayList<String>();
        File directory = new File(Environment.getExternalStorageDirectory() + "/vertical_vids/");
        List<Integer> newVideoIdList = new ArrayList<>();
        File[] files = directory.listFiles();
        String trial[] = null;
        List<String> toDel = new ArrayList<>();
        for (int index = 0; index < newVideoList.size(); index++) {
            newVideoIdList.add(newVideoList.get(index).id);
        }
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                try {
                    trial = files[i].getName().split("[_.]");
                    if (!newVideoIdList.contains(Integer.parseInt(trial[1]))) {
                        toDel.add(files[i].getAbsolutePath());
                    }
                } catch (Exception e) {
                    toDel.add(files[i].getAbsolutePath());
                    e.printStackTrace();
                }
            }
            for (int index = 0; index < toDel.size(); index++) {
                deleteFile(toDel.get(index));
            }
        }
    }

    public static boolean isWIFIConnected(Context c){
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;
    }

    public static void setInstallTime(Context context) {
        Utils.updateShreadPrefs(context, AppConstants.INSTALL_TIME, System.currentTimeMillis() + "");
    }

    public static String getInstallTime(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getString(
                AppConstants.INSTALL_TIME, null);
    }

    public static boolean isDisclaimerShown(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getBoolean(AppConstants.IS_DISCLAIMER_SHOWN, false);
    }

    public static void setDisclaimerShown(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
                SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(AppConstants.IS_DISCLAIMER_SHOWN, true);
                editor.apply(); // commit changes
    }
    private static void updateShreadPrefs(Context context, String key, String val) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(key, val);
        editor.commit(); // commit changes
    }

//    public static boolean isNDAAccepted(Context context) {
//        SharedPreferences mSharedPrefs = context.getSharedPreferences(
//                AppConstants.APP_PREFS, 0);
//        SharedPreferences.Editor editor = mSharedPrefs.edit();
//        return mSharedPrefs.getBoolean(AppConstants.IS_NDA_ACCEPTED, false);
//    }
//
//    public static void setNDAAccepted(Context context) {
//        SharedPreferences mSharedPrefs = context.getSharedPreferences(
//                AppConstants.APP_PREFS, 0);
//        SharedPreferences.Editor editor = mSharedPrefs.edit();
//        editor.putBoolean(AppConstants.IS_NDA_ACCEPTED, true);
//                editor.apply(); // commit changes
//    }

    public static void putFbId(Context context, String val) {
        updateShreadPrefs(context, "FB_ID", val);
    }

    public static String getFbID(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getString(
                "FB_ID", "");
    }

    public static void putUserId(Context context, String uId) {
        updateShreadPrefs(context, "UID", uId);
    }

    public static String getUserId(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, Context.MODE_PRIVATE);

//        if(mSharedPrefs.getString("UID","") != null || m)
//        return mSharedPrefs.getString("UID", "1494617261");
        Log.i("Utils class->UserId::",mSharedPrefs.getString("UID", ""));
        return mSharedPrefs.getString("UID", "");
    }

    public static void putUserName(Context context, String fname) {
        updateShreadPrefs(context, "USERNAME", fname);
    }

    public static String getUserName(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, Context.MODE_PRIVATE);
        return mSharedPrefs.getString(
                "USERNAME", "");
    }

    public static void putUserImage(Context context, String fname) {
        updateShreadPrefs(context, "USERIMAGE", fname);
    }

    public static String getUserImage(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getString(
                "USERIMAGE", "");
    }

    public static void putEmail(Context context, String email) {
        updateShreadPrefs(context, "EMAIL", email);
    }

    public static String getEmail(Context context) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getString(
                "EMAIL", "");
    }

    public static boolean hasCoachMarkShown(Context context, String type) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        return mSharedPrefs.getBoolean(type,
                false);
    }

    public static void setCoachMarkShown(Context context, String type) {
        SharedPreferences mSharedPrefs = context.getSharedPreferences(
                AppConstants.APP_PREFS, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(type, true);
        editor.apply(); // commit changes
    }

    public static String getFormattedDate(long date){

        SimpleDateFormat dtFormat = new SimpleDateFormat("d MMM y");
        String format = dtFormat.format(date);

        return format;

    }

    public static boolean isEmpty(String str){
        if(str == null || str.equals(""))
            return false;

        return true;
    }
}
