package com.pixtory.app.model;

/**
 * Created by aasha.medhi on 5/2/16.
 */
public class CommentData {

    public int commentId;
    public String comment;
    public PersonInfo personDetails;
    public String ingestionTime;
    public int contentId;

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getIngestionTime() {
        return ingestionTime;
    }

    public void setIngestionTime(String ingestionTime) {
        this.ingestionTime = ingestionTime;
    }

    public PersonInfo getPersonDetails() {
        return personDetails;
    }

    public void setPersonDetails(PersonInfo personDetails) {
        this.personDetails = personDetails;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
