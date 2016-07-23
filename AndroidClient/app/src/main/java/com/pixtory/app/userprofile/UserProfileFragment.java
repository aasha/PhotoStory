package com.pixtory.app.userprofile;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.retrofit.BaseResponse;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.transformations.BlurTransformation;
import com.pixtory.app.transformations.GrayscaleTransformation;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";
    private static final String ARG_PARAM2 = "PERSON_ID";
    private static final String Get_Person_Details_Done = "Get_Person_Details_Done";
    private static final String Get_Person_Details_Failed = "Get_Person_Details_Failed";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private Context mContext;
    private Activity mActivity;
    private int mUserId;
    private int mPersonId;
    private PersonInfo mPersonInfo;
    private ArrayList<ContentData> mContentDataList;
    private RecyclerView.LayoutManager gridLayout;
    //private RecyclerView recyclerView;
    private CardLayoutAdapter cardLayoutAdapter;

    private String mParam2;
    private String imgUrl = "http://vignette4.wikia.nocookie.net/naruto/images/0/09/Naruto_newshot.png/revision/latest/scale-to-width-down/300?cb=20150817151803";
    private OnFragmentInteractionListener mListener;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String userId,String personId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        args.putString(ARG_PARAM2, personId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = Integer.parseInt(getArguments().getString(ARG_PARAM1));
            mPersonId = Integer.parseInt(getArguments().getString(ARG_PARAM2));
            mContext = getActivity();
            mActivity = getActivity();
            //setPersonDetails();
            //mPersonInfo = App.getPersonInfo(mUserId);
            // mContentDataList = App.getContentData();
            //  mParam1 = getArguments().getString(ARG_PARAM1);
            //  mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View userProfleView=null;

    @Bind(R.id.person_image)
    CircularImageView profileImage = null;

    @Bind(R.id.person_image_boarder)
    CircularImageView profileImageBorder = null;

    @Bind(R.id.person_name)
    TextView personName = null;

    @Bind(R.id.person_desc)
    TextView personDesc = null;

    @Bind(R.id.blur_person_image)
    ImageView blurrPersonImage = null;

    @Bind(R.id.person_follow)
    TextView personFollow = null;

    @Bind(R.id.profile_recycler_view)
    RecyclerView recyclerView = null;

    @Bind(R.id.back_img)
    ImageView mBackImage = null;

    @Bind(R.id.back_click)
    LinearLayout mBackClick = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        userProfleView = inflater.inflate(R.layout.user_profile_fragment,container,false);

        mPersonInfo = new PersonInfo();
        mContentDataList = new ArrayList<ContentData>();

        personFollow = (TextView)userProfleView.findViewById(R.id.person_follow);
        personName = (TextView)userProfleView.findViewById(R.id.person_name);
        personDesc = (TextView)userProfleView.findViewById(R.id.person_desc);
        blurrPersonImage = (ImageView)userProfleView.findViewById(R.id.blur_person_image);
        profileImage = (CircularImageView)userProfleView.findViewById(R.id.person_image);
        profileImageBorder = (CircularImageView)userProfleView.findViewById(R.id.person_image_boarder);
        recyclerView = (RecyclerView)userProfleView.findViewById(R.id.profile_recycler_view);
        mBackClick = (LinearLayout)userProfleView.findViewById(R.id.back_click);
        mBackImage = (ImageView)userProfleView.findViewById(R.id.back_img);
        personFollow = (TextView)userProfleView.findViewById(R.id.person_follow);

        if(mUserId==mPersonId){
            personFollow.setVisibility(View.GONE);
            mPersonInfo = App.getPersonInfo();
            mContentDataList = App.getPersonContentData();
            /*if(mContentDataList!=null)
            for(ContentData cd:mContentDataList)
                cd.personDetails=mPersonInfo;*/
            App.setProfileContentData(mContentDataList);
            if(mContentDataList.size()==0)
                Toast.makeText(mContext,"You haven't liked any pixtory yet",Toast.LENGTH_SHORT).show();
            cardLayoutAdapter = new CardLayoutAdapter(getContext(), mContentDataList);
            personName.setText(mPersonInfo.name);
            personDesc.setText(mPersonInfo.description);

            if (mPersonInfo.imageUrl != null && mPersonInfo.imageUrl!="") {
                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().into(profileImage);
            } else {
                Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                Picasso.with(mContext).load(imgUrl).fit().into(profileImage);
            }

            //initialise recyclerview and set its layout as grid layout
            gridLayout = new GridLayoutManager(getContext(),2);
            recyclerView.setLayoutManager(gridLayout);

            //intialize card layout adapter and set it to recycler view
            recyclerView.setAdapter(cardLayoutAdapter);

            //get the screen dimesions
            DisplayMetrics dm =  new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            //set spacing proportional to width of the the screen (0.063 times the screen width
            double spacing = 0.063*dm.widthPixels;
            SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
            recyclerView.addItemDecoration(decoration);

        }
        else{
            if((mPersonInfo = App.getProfileInfoFromCache(mPersonId))!=null )
            {
                personName.setText(mPersonInfo.name);
                personDesc.setText(mPersonInfo.description);
                if(mPersonInfo.followedByUser)
                {
                    personFollow.setText("FOLLOWING");
                    if(Build.VERSION.SDK_INT>=21)
                        personFollow.setBackground(mContext.getDrawable(R.drawable.blue_rectangle));
                }

                if (mPersonInfo.imageUrl != null) {
                    Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                    Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                    Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().into(profileImage);
                } else {
                    Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                    Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                    Picasso.with(mContext).load(imgUrl).fit().into(profileImage);
                }
                mContentDataList = App.getProfileContentFromCache(mPersonId);
                /*for(ContentData cd:mContentDataList)
                    cd.personDetails=mPersonInfo;*/
                App.setProfileContentData(mContentDataList);
                cardLayoutAdapter = new CardLayoutAdapter(getContext(),mContentDataList);
                Toast.makeText(getContext(),mContentDataList.size()+"",Toast.LENGTH_SHORT);

                //initialise recyclerview and set its layout as grid layout
                gridLayout = new GridLayoutManager(mContext,2);
                recyclerView.setLayoutManager(gridLayout);

                //intialise card layout adapter and set it to recycler view
                recyclerView.setAdapter(cardLayoutAdapter);

                //get the screen dimesions
                DisplayMetrics dm =  new DisplayMetrics();
                mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

                //set spacing proportional to width of the the screen (0.063 times the screen width
                double spacing = 0.063*dm.widthPixels;
                SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
                recyclerView.addItemDecoration(decoration);
            }
            else
            {
                NetworkApiHelper.getInstance().getPersonDetails(mUserId, mPersonId, new NetworkApiCallback<GetPersonDetailsResponse>() {
                    @Override
                    public void success(GetPersonDetailsResponse o, Response response) {
                       // Toast.makeText(getContext(),"success",Toast.LENGTH_SHORT);
                        if (o.userDetails != null) {
                            mPersonInfo = o.userDetails;

                            personName.setText(mPersonInfo.name);
                            personDesc.setText(mPersonInfo.description);


                            if (mPersonInfo.imageUrl != null) {
                                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                                Picasso.with(mContext).load(mPersonInfo.imageUrl).fit().into(profileImage);
                            } else {
                                Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new GrayscaleTransformation(mContext)).transform(new BlurTransformation(mContext, 10)).into(profileImageBorder);
                                Picasso.with(mContext).load(imgUrl).fit().centerCrop().transform(new BlurTransformation(mContext, 10)).into(blurrPersonImage);
                                Picasso.with(mContext).load(imgUrl).fit().into(profileImage);
                            }
                        } else {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                                    .put(AppConstants.USER_ID, Integer.toString(mPersonId))
                                    .put("MESSAGE", "No Data")
                                    .build());
                            //System.out.println("Person data null");
                            Toast.makeText(getContext(), "No person data!", Toast.LENGTH_SHORT).show();
                        }

                        if (o.contentList != null) {
                            mContentDataList = o.contentList;
                            App.addToProfileContentCache(mPersonId,mContentDataList);
                            App.addToProfileCache(mContentDataList.get(0).personDetails);
                            App.setProfileContentData(mContentDataList);
                            if(mContentDataList.get(0).personDetails.followedByUser)
                            {
                                mPersonInfo.followedByUser=true;
                                personFollow.setText("FOLLOWING");
                                if(Build.VERSION.SDK_INT>=21)
                                    personFollow.setBackground(mContext.getDrawable(R.drawable.blue_rectangle));
                            }
                            cardLayoutAdapter = new CardLayoutAdapter(mContext,mContentDataList);
                           // Toast.makeText(getContext(),mContentDataList.size()+"",Toast.LENGTH_SHORT);
                        } else {
                            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                                    .put(AppConstants.USER_ID, Integer.toString(mPersonId))
                                    .put("MESSAGE", "No Data")
                                    .build());
                            Toast.makeText(getContext(), "No content data!", Toast.LENGTH_SHORT).show();
                        }


                        //initialise recyclerview and set its layout as grid layout
                        gridLayout = new GridLayoutManager(getContext(),2);
                        recyclerView.setLayoutManager(gridLayout);

                        //intialise card layout adapter and set it to recycler view

                        recyclerView.setAdapter(cardLayoutAdapter);

                        //get the screen dimesions
                        DisplayMetrics dm =  new DisplayMetrics();

                        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

                        //set spacing proportional to width of the the screen (0.063 times the screen width
                        double spacing = 0.063*dm.widthPixels;
                        SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
                        recyclerView.addItemDecoration(decoration);
                    }

                    @Override
                    public void failure(GetPersonDetailsResponse error) {

                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                                .put(AppConstants.USER_ID, Integer.toString(mPersonId))
                                .put("MESSAGE", error.errorMessage)
                                .build());
                        Toast.makeText(mContext, "Please check your network connection", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void networkFailure(RetrofitError error) {

                        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                                .put(AppConstants.USER_ID, Integer.toString(mPersonId))
                                .put("MESSAGE", error.getMessage())
                                .build());
                        Toast.makeText(mContext, "Please check your network connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

        mBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_Back_Click")
                 .put("USER_ID",mUserId+"")
                .build());
//                mActivity.onBackPressed();
                mActivity.finish();
            }});

        mBackClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_Back_Click")
                        .put("USER_ID",mUserId+"")
                        .build());
//                mActivity.onBackPressed();
                mActivity.finish();

            }});

        personFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isNotEmpty(Utils.getFbID(getContext())))
                {
                    if(!mPersonInfo.followedByUser)
                        sendFollowRequestToBackend(true);
                    else
                        sendFollowRequestToBackend(false);
                }else
                    mListener.showLoginAlert();

            }

        });

        return userProfleView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void sendFollowRequestToBackend(final boolean doFollow){
        NetworkApiHelper.getInstance().followPerson(mUserId,mPersonId,doFollow,new NetworkApiCallback<BaseResponse>() {
            @Override
            public void success(BaseResponse o, Response response) {
                if(doFollow){
                    Toast.makeText(mContext,"You are now following "+mPersonInfo.name,Toast.LENGTH_SHORT).show();
                    mPersonInfo.followedByUser = true;
                    personFollow.setText("FOLLOWING");
                    App.addToProfileCache(mPersonInfo);
                    if(Build.VERSION.SDK_INT>=21)
                        personFollow.setBackground(mContext.getDrawable(R.drawable.blue_rectangle));
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_Follow_Click")
                            .put(AppConstants.USER_ID,mUserId+"")
                            .put("CONTRIBUTOR_ID",mPersonId+"")
                            .build());
                }
                else{
                    Toast.makeText(mContext,"You've unfollwed "+mPersonInfo.name,Toast.LENGTH_SHORT).show();
                    mPersonInfo.followedByUser = false;
                    personFollow.setText("FOLLOW");
                    if(Build.VERSION.SDK_INT>=21)
                        personFollow.setBackground(mContext.getDrawable(R.drawable.back_rectangle));
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder("Profile_UnFollow_Click")
                        .put(AppConstants.USER_ID,mUserId+"")
                        .put("CONTRIBUTOR_ID",mPersonId+"")
                        .build());
                }
            }

            @Override
            public void failure(BaseResponse error) {
                // mProgress.dismiss();

                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                Toast.makeText(getContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void showLoginAlert();


    }
}
