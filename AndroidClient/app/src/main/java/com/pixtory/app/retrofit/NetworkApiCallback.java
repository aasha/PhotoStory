package com.pixtory.app.retrofit;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aasha.medhi on 08/09/15.
 */
public interface NetworkApiCallback<T> {

    /** Successful HTTP response. */
    void success(T t, Response response);
    /**
     * Failure  response
     */
    void failure(T t);
    /**
     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
     * exception.
     */
    void networkFailure(RetrofitError error);
}
