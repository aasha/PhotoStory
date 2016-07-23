package service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.GetWallPaperResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by training3 on 16/07/2016 AD.
 */
public class WallpaperChangeService extends GcmTaskService {

    private static String TAG = WallpaperChangeService.class.getName();
    private GcmNetworkManager mGcmNetworkManager;


    @Override
    public int onRunTask(TaskParams taskParams) {

        Log.d(TAG,"onRunTask");

        switch (taskParams.getTag()) {
            case AppConstants.TAG_TASK_ONEOFF_LOG:
                Log.i(TAG, AppConstants.TAG_TASK_ONEOFF_LOG);
                // This is where useful work would go
                setWallpaper();
                return GcmNetworkManager.RESULT_SUCCESS;

            default:
                return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    public void setWallpaper(){
        if(Utils.isNotEmpty(Utils.getUserId(getApplicationContext()))) {

            int user_id = Integer.parseInt(Utils.getUserId(getApplicationContext()));

            NetworkApiHelper.getInstance().getWallPaper(user_id, new NetworkApiCallback<GetWallPaperResponse>() {
                @Override
                public void success(GetWallPaperResponse getWallPaperResponse, Response response) {
                    Log.i(TAG, "wallpaper URL is--" + getWallPaperResponse.wallPaper);
                    setWallPaper(getApplicationContext(), getWallPaperResponse.wallPaper);


                }

                @Override
                public void failure(GetWallPaperResponse getWallPaperResponse) {
                    Log.i(TAG,"failure->"+getWallPaperResponse.errorMessage);
//                    setJobSchedulerToSetWallpaper(getApplicationContext());
                }

                @Override
                public void networkFailure(RetrofitError error) {
                    Log.i(TAG,"networkFailure->"+error.toString());
//                    setJobSchedulerToSetWallpaper(getApplicationContext());
                }
            });
        }
    }

    public void setWallPaper(final Context mContext , String imgUrl) {
        Picasso.with(mContext).load(imgUrl).into(App.mDailyWallpaperTarget);

    }

    private void setJobSchedulerToSetWallpaper(Context ctx){
        if(mGcmNetworkManager==null)
            mGcmNetworkManager = GcmNetworkManager.getInstance(ctx);

        Task task = new OneoffTask.Builder()
                .setService(WallpaperChangeService.class)
                .setExecutionWindow(1000*60, 1000*60*60*9) // 45 seconds to nine hours
                .setTag(AppConstants.TAG_TASK_REPEAT)
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        mGcmNetworkManager.schedule(task);
    }
}
