package com.pixtory.app.retrofit;


import android.content.Context;
import android.util.Log;

import com.pixtory.app.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 08/09/15.
 */
public class NetworkApiHelper {

    private final static String TAG = NetworkApiHelper.class.getSimpleName();

    private static NetworkApiHelper sInstance = null;
    private static RetrofitManager sRetrofit = null;
    private static NetworkInterface sAPI = null;

    public synchronized static NetworkApiHelper getInstance() {
        if (sInstance == null) {
            sInstance = new NetworkApiHelper();
            sRetrofit = RetrofitManager.getInstance();
            sAPI = sRetrofit.getNetworkInterface();
        }
        return sInstance;
    }


    private NetworkApiHelper() {
        //Constructor is made private to ensure singleton behavior
    }

    /**
     * ============================================================================================================================================================================================
     */

    public void registerUser(final String userName, final String userEmail, final String userImageUrl, final String fbId, final NetworkApiCallback cb) {
        RegisterRequest req = new RegisterRequest();
        req.userName = userName;
        req.userEmail = userEmail;
        req.userImageUrl = userImageUrl;
        req.fbId = fbId;
        sAPI.register(req, new Callback<RegisterResponse>() {
            @Override
            public void success(RegisterResponse registerResponse, Response response) {
                if (registerResponse.success == true)
                    cb.success(registerResponse, response);
                else
                    cb.failure(registerResponse);

            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void getMainFeed(Context ctxt, String userId, final NetworkApiCallback cb) {
        GetMainFeedRequest req = new GetMainFeedRequest();
//        req.userId = Integer.parseInt(Utils.getUserId(ctxt));
        if (Utils.isNotEmpty(userId)) {
            req.userId = Integer.parseInt(userId);

            sAPI.getMainFeed(req, new Callback<GetMainFeedResponse>() {
                @Override
                public void success(GetMainFeedResponse contentResponse, Response response) {
                    if (contentResponse.success == true)
                        cb.success(contentResponse, response);
                    else
                        cb.failure(contentResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    cb.networkFailure(error);
                }
            });
        } else {
            Log.i(TAG, "getMainFeed error--user Id is null");
        }
    }

    /**
     * ============================================================================================================================================================================================
     */
    public void likeContent(String userId, int contentId, boolean action, final NetworkApiCallback cb) {
        final LikeContentRequest req = new LikeContentRequest();
        req.userId = Integer.parseInt(userId);
        req.contentId = contentId;
        req.doLike = action;

        sAPI.likeContent(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    /**
     * ============================================================================================================================================================================================
     */
    public void addComment(String userId, int contentId, String comment, final NetworkApiCallback cb) {
        AddCommentRequest req = new AddCommentRequest();
        req.userId = Integer.parseInt(userId);
        req.contentId = contentId;
        req.comment = comment;

        sAPI.addComment(req, new Callback<AddCommentResponse>() {
            @Override
            public void success(AddCommentResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    /**
     * ============================================================================================================================================================================================
     */
    public void addPushNotifsId(String userId, String notificationId, final NetworkApiCallback cb) {
        AddPushNotifsIdRequest req = new AddPushNotifsIdRequest();
        req.userId = Integer.parseInt(userId);
        req.gcmId = notificationId;

        sAPI.addPushNotifsId(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    /**
     * ============================================================================================================================================================================================
     */
    public void deleteComment(int userId, int contentId, int commentId, final NetworkApiCallback cb) {
        DeleteCommentRequest req = new DeleteCommentRequest();
        req.userId = userId;
        req.contentId = contentId;
        req.commentId = commentId;

        sAPI.deleteComment(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    /**
     * =============================================================================================================================================================
     */
    public void getCommentDetailList(String userId, int contentId, final NetworkApiCallback cb) {
        GetCommentDetailsRequest req = new GetCommentDetailsRequest();
        req.userId = Integer.parseInt(userId);
        req.contentId = contentId;

        sAPI.getCommentDetailList(req, new Callback<GetCommentDetailsResponse>() {
            @Override
            public void success(GetCommentDetailsResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void getPersonDetails(int userId, int personId, final NetworkApiCallback cb) {
        GetPersonDetailsRequest req = new GetPersonDetailsRequest();
        req.userId = userId;
        req.personId = personId;


        sAPI.getPersonDetails(req, new Callback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void userFeedBack(int userId, String feedBack, String category, String subCategory, String details, final NetworkApiCallback cb) {
        userFeedBackRequest req = new userFeedBackRequest();
        req.userId = userId;
        req.feedBack = feedBack;
        req.category = category;
        req.subCategory = subCategory;
        req.details = details;

        sAPI.userFeedBack(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void getContributorMail(int userId, String mailId, String userName, String userMobileNumber, final NetworkApiCallback cb) {
        GetContributorMailRequest req = new GetContributorMailRequest();
        req.userId = userId;
        req.userEmail = mailId;
        req.userName = userName;
        req.mobileNumber = userMobileNumber;

        sAPI.getContributorMail(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void followPerson(int userId, int personId, boolean doFollow, final NetworkApiCallback cb) {
        FollowPersonRequest req = new FollowPersonRequest();
        req.userId = userId;
        req.personId = personId;
        req.doFollow = doFollow;

        sAPI.followPerson(req, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void getWallPaper(int userId, final NetworkApiCallback cb) {
        GetWallPaperRequest req = new GetWallPaperRequest();
        req.userId = userId;

        sAPI.getWallPaper(req, new Callback<GetWallPaperResponse>() {
            @Override
            public void success(GetWallPaperResponse resp, Response response) {
                if (resp.success == true)
                    cb.success(resp, response);
                else
                    cb.failure(resp);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }

    public void getContentByCategory(int userId, int categoryIdParam, final NetworkApiCallback cb) {
        GetContentByCategoryRequest req = new GetContentByCategoryRequest();
        req.categoryId = categoryIdParam;
        req.userId = userId;

        sAPI.getContentByCategory(req, new Callback<GetMainFeedResponse>() {
            @Override
            public void success(GetMainFeedResponse contentResponse, Response response) {
                if (contentResponse.success == true)
                    cb.success(contentResponse, response);
                else
                    cb.failure(contentResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                cb.networkFailure(error);
            }
        });
    }


    public void shareContent(int userId , int contentId, final NetworkApiCallback cb) {
        ShareContentRequest req = new ShareContentRequest();
        req.userId = userId;
        req.contentId = contentId;

        sAPI.shareContent(req, new Callback<ShareContentIdResponse>() {
            @Override
            public void success(ShareContentIdResponse shareContentIdResponse, Response response) {
                if (shareContentIdResponse.success == true)
                    cb.success(shareContentIdResponse, response);
                else
                    cb.failure(shareContentIdResponse);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


}
