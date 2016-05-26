package com.pixtory.app.retrofit;

import com.pixtory.app.model.PersonInfo;

/**
 * Created by sriram on 23/05/2016.
 */
public class GetPersonInfoResponse {
    public boolean success = false;
    public int errorCode = -1;
    public String errorMessage = null;
    public PersonInfo personInfo = null;
}
