package com.pixtory.app.retrofit;

import com.pixtory.app.model.ContentData;

import java.util.ArrayList;

/**
 * Created by aasha.medhi on 21/10/15.
 */
public class GetMainFeedResponse {

    public boolean success = false;
    public int fullCount = 0;
    public ArrayList<ContentData> contentList = null;

    public String errorMessage = null;
    public int errorCode = -1;
}
