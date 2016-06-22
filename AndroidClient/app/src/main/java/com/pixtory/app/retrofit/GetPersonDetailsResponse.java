package com.pixtory.app.retrofit;

import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;

import java.util.ArrayList;

/**
 * Created by krish on 26/05/2016.
 */
public class GetPersonDetailsResponse {
    public boolean success;
    public int errorCode;
    public String errorMessage;
    public PersonInfo userDetails;
    public ArrayList<ContentData> contentList;
}
