package com.pixtory.app.retrofit;

import com.pixtory.app.model.CommentData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by training3 on 27/05/2016 AD.
 */
public class GetCommentDetailsResponse {

    private ArrayList<CommentData> commentList=null;
    public  boolean success = false;
    public String errorMessage = null;
    public int errorCode = -1;

    public ArrayList<CommentData> getCommentList() {
        return commentList;
    }

    public void setCommentList(ArrayList<CommentData> dataSet) {
        this.commentList = commentList;
    }
}
