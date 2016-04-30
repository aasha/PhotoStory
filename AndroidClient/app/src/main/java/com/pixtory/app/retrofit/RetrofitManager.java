package com.pixtory.app.retrofit;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by aasha.medhi on 14/05/15.
 */
public class RetrofitManager {

    private static final String PUBLIC_URL = "http://appdemo.ops.ev1.inmobi.com:4001";

    private static final String APP_URL = PUBLIC_URL;


    private static RetrofitManager sInstance = null;

    public static RetrofitManager getInstance() {
        if (sInstance == null) {
            sInstance = new RetrofitManager();
        }
        return sInstance;
    }



    public NetworkInterface getNetworkInterface() {

        RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder();
        restAdapterBuilder.setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(APP_URL);

        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        restAdapterBuilder.setClient(new OkClient(client));

        RestAdapter restAdapter = restAdapterBuilder.build();


        NetworkInterface service = restAdapter.create(NetworkInterface.class);
        return service;
    }
}
