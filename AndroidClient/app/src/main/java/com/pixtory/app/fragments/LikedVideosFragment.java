package com.pixtory.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pixtory.app.R;
import com.pixtory.app.adapters.VideoRecommendationAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class LikedVideosFragment extends Fragment implements VideoRecommendationAdapter.VideoViewHolder.FollowClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mContentIndex;

    OnFragmentInteractionListener mListener;

    public LikedVideosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    LinearLayout mRecLayout = null;
    private RecyclerView mRecomRecycle = null;
    private LinearLayoutManager mLayoutManager = null;
    private VideoRecommendationAdapter videoRecommendationAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = (View) inflater.inflate(R.layout.liked_videos_view_layout, container, false);
        mRecomRecycle = (RecyclerView) mRootView.findViewById(R.id.recom_recy);
        mRecomRecycle.setHasFixedSize(true);

        mLayoutManager =  new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecomRecycle.setLayoutManager(mLayoutManager);
        TextView mNoItems = (TextView) mRootView.findViewById(R.id.textNoItems);
        videoRecommendationAdapter = new VideoRecommendationAdapter(this.getContext());
        videoRecommendationAdapter.followClickListener = this;
        mRecomRecycle.setAdapter(videoRecommendationAdapter);
        if(App.getLikedContentData() == null || App.getLikedContentData().size() == 0){
            mNoItems.setVisibility(View.VISIBLE);
        }else {
            mNoItems.setVisibility(View.GONE);
            //videoRecommendationAdapter.setData(App.getLikedContentData());
        }
        return mRootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFollowClick(View caller, int pos) {
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_VidCard_Click)
                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                .put(AppConstants.OPINION_ID, "" + App.getLikedContentData().get(pos).id)
                .build());
        mListener.onVideoFragmentInteraction(pos);
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
        public void onVideoFragmentInteraction(int position);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_Vid_Tap)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .build());
        }
    }
}
