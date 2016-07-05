package com.pixtory.app.utils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ProgressBar;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.pixtory.app.BuildConfig;
import com.pixtory.app.model.ContentData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonali kakrayne on 01/07/2016 AD.
 */
public class ImageDownloadManager extends AsyncTask {

    ArrayList<ContentData> urlList ;
    String TAG = ImageDownloadManager.class.getName();
    Context context;
    ProgressDialog mProgressBar;
    int initialIndex,endIndex;
    StringBuilder mImageUrl;

    ImageDownloadListener mImageDownloadListener;


    public interface ImageDownloadListener{
        public void onImageFetched();
    }

    public ImageDownloadManager(Context pContext,ArrayList<ContentData> list , int index , int pEndIndex ){
        context = pContext;
        mImageDownloadListener = (ImageDownloadListener)context;
        urlList = list;
        initialIndex = index;
        endIndex = pEndIndex;
    }

    @Override
    protected void onPreExecute() {
//        mProgressBar.show();
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.i(TAG,"hello");
        for(int i=0; i<endIndex;i++){
            if((initialIndex+i)> urlList.size() || (initialIndex+i)== urlList.size()){
                Log.i(TAG,"Arraylist Out of index");
                break;
            }else {

                if((urlList.get(initialIndex + i).pictureUrl) != null ) {
                    mImageUrl = new StringBuilder(urlList.get(initialIndex + i).pictureUrl);

                    if (mImageUrl != null && !(mImageUrl.equals(""))) {
                        Log.i(TAG, "Image url-" + mImageUrl.toString());
                        ImageRequest request = ImageRequestBuilder
                                .newBuilderWithSource(Uri.parse(urlList.get(initialIndex + i).pictureUrl))
                                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                                .setProgressiveRenderingEnabled(true)
                                .build();

                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        final DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(request, null);
                    }
                }

//                dataSource.subscribe(new DataSubscriber<Void>() {
//
//                    @Override
//                    public void onNewResult(DataSource dataSource) {
//                        if (dataSource.isFinished()){
//                            dataSource.close();
//                            Log.d(TAG,"image download finished");
//                            mImageDownloadListener.onImageFetched();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(DataSource dataSource) {
//                        if (dataSource != null) {
//                            dataSource.close();
//                        }
//                    }
//
//                    @Override
//                    public void onCancellation(DataSource dataSource) {
//
//                    }
//
//                    @Override
//                    public void onProgressUpdate(DataSource dataSource) {
//
//                    }
//
//
//                }, CallerThreadExecutor.getInstance());

            }


        }



        return null;


    }

    @Override
    protected void onPostExecute(Object o) {
        Log.i(TAG,"all image downloaded");
//        mProgressBar.hide();
        super.onPostExecute(o);
    }
}
