package com.pixtory.app.app;


import java.util.Arrays;
import java.util.List;

/**
 * Created by aasha.medhi on 11/05/15.
 */
public interface AppConstants {

    String APP_PREFS = "app_prefs";

    String USER_MEEP_COUNT = "USER_MEEP_COUNT";
    String INSTALL_TIME = "INSTALL_TIME";
    String USER_TAGS_PRD_COUNT = "USER_TAGS_PRD_COUNT";
    String USER_FOLLOW_COUNT = "USER_FOLLOW_COUNT";
    String USER_FOLLOW_CONN_COUNT = "USER_FOLLOW_CONN_COUNT";

    String VARIANT_ID = "VARIANT_ID"; // GCM ID

    String IS_GCM_SAVED = "is_gcm_saved";
    public static final String GCM_ID = "gcm_id";
    public static final String APP_VERSION = "app_version";

    String IS_NDA_ACCEPTED = "IS_NDA_ACCEPTED";
    String IS_DISCLAIMER_SHOWN = "IS_DISCLAIMER_SHOWN";

    String HAS_TAP_COACH_MARK_SHOWN = "HAS_TAP_COACH_MARK_SHOWN";
    String HAS_SWIPE_DOWN_COACH_MARK_SHOWN = "HAS_SWIPE_DOWN_COACH_MARK_SHOWN";



    public static String LAST_SYNC = "lastSync";


    public static final String OPINION_ID = "OPINION_ID";

    public static final String PRODUCT_ID = "PRODUCT_ID";

    public static final String USER_ID ="USER_ID";
    public static final String CONNECTION_QUALITY ="CONNECTION_QUALITY";

    final static String OB_Card_Swipe = "OB_Card_Swipe";
    final static String OB_FBLogin_Click = "OB_FBLogin_Click";
    final static String OB_FBLogin_Success = "OB_FBLogin_Success";
    final static String OB_FBLogin_Cancel = "OB_FBLogin_Cancel";
    final static String OB_FBLogin_Fail = "OB_FBLogin_Fail";
    final static String OB_Register_Success = "OB_Register_Success";
    final static String OB_Register_Failure = "OB_Register_Failure";
    final static String INVITE_LINK_CLICKED = "INVITE_LINK_CLICKED";
    final static String IS_WALLPAPER_SET_FOR_TODAY = "IS_WALLPAPER_SET_FOR_TODAY";
    final static String TAG_TASK_ONEOFF_LOG = "TAG_TASK_ONEOFF_LOG";
    final static String TAG_TASK_REPEAT = "TAG_TASK_REPEAT";

    static final int SHOW_PIC_STORY = 88;
    static final int SHOW_PIC_COMMENTS = 89;

    static final int SET_WALLPAPER = 0;
    static final int CACHE_WALLPAPER_IMAGE = 1;



    final static List<String> mFBPermissions = Arrays.asList("public_profile",
            "email","user_birthday");

    final static String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.pixtory.app&referrer=";

    final static String INVITE_WHATSAPP_LINK = "https://n7e3g.app.goo.gl/Siao";
    final static String INVITE_EMAIL_LINK = "https://n7e3g.app.goo.gl/7BOF";
    final static String INVITE_FACEBOOK_LINK = "https://n7e3g.app.goo.gl/yhnF";
    final static int WHATSAPP_INVITE = 89;
    final static int FB_INVITE = 90;
    final static int EMAIL_INVITE = 91;
    final static int SOCIAL_MEDIA_FACEBOOK_SHARE = 92;
    final static int SOCIAL_MEDIA_INSTAGRAM_SHARE = 93;
    final static String SCM_WHATSAPP_SHARE = "https://n7e3g.app.goo.gl/Z8OJ";
    final static int ORGANIC_PLAYSTORE = 95;
    final static int WEBSITE_TO_PLAYSTORE = 96;


}
