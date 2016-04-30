package com.pixtory.app.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.pixtory.app.R;
import com.pixtory.app.VideoViewerActivity;
import com.pixtory.app.adapters.UserProfileAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.typeface.BigNoodleTitling;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserProfileFragment extends Fragment implements LikedProductsFragment.OnFragmentInteractionListener,
        ProductViewerFragment.OnFragmentInteractionListener,
        LikedVideosFragment.OnFragmentInteractionListener{
    private ProgressDialog mProgress = null;
    ViewPager viewPager;
    TabLayout tabLayout;
    UserProfileAdapter adapter;
    TextView mTextUserName;
    private OnUserFragmentInteractionListener mListener;

    public static final String SCREEN_NAME = "Profile";

    private static final String Profile_Close_Click = "Profile_Close_Click";
    public static final String Profile_Vid_Tap = "Profile_Vid_Tap";
    public static final String Profile_Prod_Tap = "Profile_Prod_Tap";
    public static final String Profile_Int_Tap = "Profile_Int_Tap";
    public static final String Profile_VidCard_Click = "Profile_VidCard_Click";
    public static final String Profile_VidCard_Scroll = "Profile_VidCard_Scroll";
    public static final String Profile_IntCard_Unselect = "Profile_IntCard_Unselect";
    public static final String Profile_IntCard_Select = "Profile_IntCard_Select";
    public static final String Profile_ProdCard_UnBkmrk = "Profile_ProdCard_UnBkmrk";
    public static final String Profile_ProdCard_Bkmrk = "Profile_ProdCard_Bkmrk";
    public static final String Profile_ProdCard_Scroll = "Profile_ProdCard_Scroll";
    public static final String Profile_ProdCard_CTAclick = "Profile_ProdCard_CTAclick";

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    LinearLayout mFullViewerFragmentLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_profile_layout, container, false);
        ImageButton closeBtn = (ImageButton) v.findViewById(R.id.close_btn);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        mTextUserName = (TextView) v.findViewById(R.id.txtUserName);
        mTextUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        BigNoodleTitling.applyFont(getActivity(), mTextUserName);
        mFullViewerFragmentLayout = (LinearLayout) v.findViewById(R.id.full_fragment_layout);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClose(false);
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Profile_Close_Click)
                        .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                        .build());
            }
        });
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Fetching your profile data");
        mProgress.setCanceledOnTouchOutside(false);
        getUserLikes();
        return v;
    }

    private void setupTabIcons(TabLayout tabLayout) {
        RelativeLayout tabOne = (RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.custom_tab_column_layout, null);
        tabOne.setBackgroundColor(Color.TRANSPARENT);
        TextView tabOneText = (TextView) tabOne.findViewById(R.id.txtTabName);
        tabOneText.setText("VIDEOS");
        ImageView tabOneImage = (ImageView) tabOne.findViewById(R.id.imgTabIcon);
        tabOneImage.setImageResource(R.drawable.tab_like);
        TextView tabOneCount = (TextView) tabOne.findViewById(R.id.txtTabCount);
        tabOneCount.setText(App.getLikedContentData().size() + "");
        tabLayout.getTabAt(0).setCustomView(tabOne);

        RelativeLayout tabTwo = (RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.custom_tab_column_layout, null);
        tabTwo.setBackgroundColor(0x00000000);
        TextView tabTwoText = (TextView) tabTwo.findViewById(R.id.txtTabName);
        tabTwoText.setText("PRODUCTS");
        ImageView tabTwoImage = (ImageView) tabTwo.findViewById(R.id.imgTabIcon);
        tabTwoImage.setImageResource(R.drawable.tab_bm);
        TextView tabTwoCount = (TextView) tabTwo.findViewById(R.id.txtTabCount);
        tabTwoCount.setText(App.getLikedProducts().size() + "");
        View divider = tabTwo.findViewById(R.id.firstDivider);
        divider.setVisibility(View.GONE);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new UserProfileAdapter(this.getChildFragmentManager());
        LikedVideosFragment likedVideosFragment = new LikedVideosFragment();
        likedVideosFragment.mListener = this;
        adapter.addFragment(likedVideosFragment, "VIDEOS");

        LikedProductsFragment likedProductsFragment = new LikedProductsFragment();
        likedProductsFragment.mListener = this;
        adapter.addFragment(likedProductsFragment, "PRODUCTS");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnUserFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUserFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    ProductViewerFragment mProductViewerFragment;

    @Override
    public void onProductFragmentInteraction(int position) {
        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mProductViewerFragment = ProductViewerFragment.newInstance(String.valueOf(position));
        mProductViewerFragment.mListener = this;
        fragmentTransaction.add(R.id.full_fragment_layout, mProductViewerFragment).commit();
        mFullViewerFragmentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductFragmentClose() {
        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(mProductViewerFragment).commit();
        mFullViewerFragmentLayout.setVisibility(View.GONE);
    }

    //Start video activity
    @Override
    public void onVideoFragmentInteraction(int position) {
        Intent i = new Intent(this.getContext(), VideoViewerActivity.class);
        i.putExtra("param1", position);
        this.getContext().startActivity(i);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnUserFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onClose(boolean interestModified);
    }

    private void getUserLikes() {
        mProgress.show();
        NetworkApiHelper.getInstance().getAllUserLikes(Utils.getUserId(getActivity()),new NetworkApiCallback<GetAllUserLikesResponse>() {
            @Override
            public void success(GetAllUserLikesResponse o, Response response) {
                mProgress.dismiss();
                if (o.contentList != null) {
                    //TODO set username
                    mTextUserName.setText(o.userDetails.userName);
                    App.setLikedContentData(o.contentList);
                    App.setLikedProducts(o.productList);
                    if (isVisible()) {
                        setupViewPager(viewPager);
                        tabLayout.setupWithViewPager(viewPager);
                        setupTabIcons(tabLayout);
                    }
                } else {
                    //Show no products
                }
            }

            @Override
            public void failure(GetAllUserLikesResponse error) {
                mProgress.dismiss();
                Toast.makeText(getActivity(), "Please check your network connection", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void networkFailure(RetrofitError error) {
                mProgress.dismiss();
                Toast.makeText(getActivity(), "Please check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
