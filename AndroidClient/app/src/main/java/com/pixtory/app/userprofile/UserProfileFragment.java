package com.pixtory.app.userprofile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixtory.app.HomeActivity;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.model.PersonInfo;
import com.pixtory.app.retrofit.GetPersonDetailsResponse;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.BlurBuilder;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.Bind;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String Get_Person_Details_Done = "Get_Person_Details_Done";
    private static final String Get_Person_Details_Failed = "Get_Person_Details_Failed";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private int mUserId;
    private PersonInfo mPersonInfo;
    private ArrayList<ContentData> mContentDataList;
    private RecyclerView.LayoutManager gridLayout;
    //private RecyclerView recyclerView;
    private CardLayoutAdapter cardLayoutAdapter;

    private String mParam2;

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
    public static UserProfileFragment newInstance(int userId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, userId);
        //  args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getInt(ARG_PARAM1);
            setPersonDetails();
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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (java.net.MalformedURLException e) {
            // Log exception
            return null;
        } catch (IOException e){

            return null;
        }
    }

    private void setPersonDetails(){
        NetworkApiHelper.getInstance().getPersonDetails(mUserId, 123 ,new NetworkApiCallback<GetPersonDetailsResponse>() {
            @Override
            public void success(GetPersonDetailsResponse o, Response response) {
                //mProgress.dismiss();
                if (o.contentList != null) {
                    mContentDataList = o.contentList;
                } else {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                            .put(AppConstants.USER_ID,Integer.toString(mUserId))
                            .put("MESSAGE", "No Data")
                            .build());
                    // Toast.makeText(this.getAcitvity(), "No content data!", Toast.LENGTH_SHORT).show();
                }

                if (o.personDetails!=null){
                    mPersonInfo = o.personDetails;
                }else {
                    AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                            .put(AppConstants.USER_ID,Integer.toString(mUserId))
                            .put("MESSAGE", "No Data")
                            .build());
                    //   Toast.makeText(super.get, "No person data!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(GetPersonDetailsResponse error) {
                // mProgress.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                        .put(AppConstants.USER_ID, Integer.toString(mUserId))
                        .put("MESSAGE", error.errorMessage)
                        .build());
                //Toast.makeText(this.getActivity(), "Please check your network connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void networkFailure(RetrofitError error) {
                //mProgress.dismiss();
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Get_Person_Details_Failed)
                        .put(AppConstants.USER_ID,Integer.toString(mUserId))
                        .put("MESSAGE", error.getMessage())
                        .build());
                //Toast.makeText(super.getActivity(), "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        userProfleView = inflater.inflate(R.layout.user_profile,container,false);

        setPersonDetails();

        //blur the image
        Bitmap blurredimage = BlurBuilder.blur(super.getActivity(), getBitmapFromURL(mPersonInfo.imageUrl));
        blurrPersonImage.setScaleType(ImageView.ScaleType.FIT_XY);
        blurrPersonImage.setImageBitmap(blurredimage);


        Picasso.with(super.getActivity()).load(mPersonInfo.imageUrl).fit().into(profileImage);
        personName.setText(mPersonInfo.name);
        personDesc.setText(mPersonInfo.desc);
        // blurrPersonImage.setImageResource(R.drawable.pixtory);

        //initialise recyclerview and set its layout as grid layout
        gridLayout = new GridLayoutManager(super.getActivity(),2);
        //recyclerView = (RecyclerView)findViewById(R.id.profile_recycler_view);
        recyclerView.setLayoutManager(gridLayout);

        //intialise card layout adapter and set it to recycler view
        cardLayoutAdapter = new CardLayoutAdapter(getActivity(),mContentDataList);
        recyclerView.setAdapter(cardLayoutAdapter);

        //get the screen the screen dimesions
        DisplayMetrics dm =  new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        //set spacing between cards proportional to width of the the screen (0.063 times the screen width
        double spacing = 0.063*dm.widthPixels;
        SpacesItemDecoration decoration = new SpacesItemDecoration((int)spacing);
        recyclerView.addItemDecoration(decoration);

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
        void onFragmentInteraction(Uri uri);
    }
}
