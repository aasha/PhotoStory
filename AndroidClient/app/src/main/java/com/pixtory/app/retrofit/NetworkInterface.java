package com.pixtory.app.retrofit;

import retrofit.Callback;
import retrofit.http.*;

/**
 * Created by aasha.medhi on 14/05/15.
 */
public interface NetworkInterface {

    @POST("/registerUser")
    void register(@Body RegisterRequest req, Callback<RegisterResponse> cb);

    @POST("/getMainFeed")
    void getMainFeed(@Body GetMainFeedRequest req, Callback<GetMainFeedResponse> cb);

    @POST("/likeContent")
    void likeContent(@Body LikeContentRequest req, Callback<BaseResponse> cb);

    @POST("/addComment")
    void addComment(@Body AddCommentRequest req, Callback<AddCommentResponse> cb);

    @POST("/deleteComment")
    void deleteComment(@Body DeleteCommentRequest req, Callback<BaseResponse> cb);

    @POST("/addPushNotifsId")
    void addPushNotifsId(@Body AddPushNotifsIdRequest req, Callback<BaseResponse> cb);

    @POST("/getCommentDetailList")
    void getCommentDetailList(@Body GetCommentDetailsRequest req, Callback<GetCommentDetailsResponse> cd);

    @POST("/getPersonDetails")
    void getPersonDetails(@Body GetPersonDetailsRequest req, Callback<GetPersonDetailsResponse> cd);

}
