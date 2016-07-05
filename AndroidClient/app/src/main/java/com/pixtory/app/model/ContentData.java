package com.pixtory.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aasha.medhi on 21/10/15.
 */
public class ContentData {
    public int id = -1;
    public String name = null;
    public String pictureUrl = null;
    public int likeCount = 0;
    public String place = null;
    public String date = null;
    public String pictureDescription = "";
    public String pictureSummary = "";
    public boolean editorsPick = false;
    public String pictureFirstPara = null;
    public boolean likedByUser = false;
    public PersonInfo personDetails = null;
    public List<CommentData> commentList = null;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ContentData)) {
            return false;
        }

        ContentData c = (ContentData) o;
        if(c.id==this.id)
            return true;
        return false;
    }
}
