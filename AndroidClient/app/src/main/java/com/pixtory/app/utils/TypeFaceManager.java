package com.pixtory.app.utils;

import android.content.Context;

/**
 * Created by aasha.medhi on 11/24/15.
 */
public class TypeFaceManager {

    private static TypeFaceManager mInstance = null;

    private TypeFaceManager() {
        // Exists only to defeat instantiation.
    }
    public static TypeFaceManager getInstance(Context mctx) {
        if(mInstance == null) {
            mInstance = new TypeFaceManager();
        }
        return mInstance;
    }
}
