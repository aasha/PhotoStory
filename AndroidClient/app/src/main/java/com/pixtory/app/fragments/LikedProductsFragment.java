package com.pixtory.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pixtory.app.R;
import com.pixtory.app.adapters.ProductRecommendationAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.Product;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class LikedProductsFragment extends Fragment implements ProductRecommendationAdapter.ProductViewHolder.FollowClickListener {

    OnFragmentInteractionListener mListener;

    public LikedProductsFragment() {
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
    private GridLayoutManager mLayoutManager = null;
    private ProductRecommendationAdapter productRecommendationAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = (View) inflater.inflate(R.layout.liked_products_view_layout, container, false);

        mRecomRecycle = (RecyclerView) mRootView.findViewById(R.id.recom_recy);
        mRecomRecycle.setHasFixedSize(true);

        TextView mNoItems = (TextView) mRootView.findViewById(R.id.textNoItems);
        mLayoutManager =  new GridLayoutManager(this.getContext(), 2);
        mRecomRecycle.setLayoutManager(mLayoutManager);

        productRecommendationAdapter = new ProductRecommendationAdapter(this.getContext());
        productRecommendationAdapter.followClickListener = this;
        mRecomRecycle.setAdapter(productRecommendationAdapter);
        if(App.getLikedProducts() == null || App.getLikedProducts().size() == 0){
            mNoItems.setVisibility(View.VISIBLE);
        }else {
            mNoItems.setVisibility(View.GONE);
            productRecommendationAdapter.setData(App.getLikedProducts());
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
        mListener.onProductFragmentInteraction(pos);
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_ProdCard_CTAclick)
                .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                .put(AppConstants.PRODUCT_ID, App.getLikedProducts().get(pos).productId + "")
                .build());
    }

    @Override
    public void onBookMarkClick(int pos, boolean value) {
        List<Product> pdList = App.getLikedProducts();
        pdList.get(pos).bookmarkedByUser = value;
        App.setLikedProducts((ArrayList<Product>) pdList);
        NetworkApiHelper.getInstance().addComment(Utils.getUserId(getActivity()), pdList.get(pos).productId, value, new NetworkApiCallback<AddCommentResponse>() {
            @Override
            public void success(AddCommentResponse o, Response response) {

            }

            @Override
            public void failure(AddCommentResponse error) {

            }

            @Override
            public void networkFailure(RetrofitError error) {
            }
        });
        if(value == true) {
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_ProdCard_Bkmrk)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.PRODUCT_ID, pdList.get(pos).productId + "")
                    .build());
        }else{
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_ProdCard_UnBkmrk)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .put(AppConstants.PRODUCT_ID, pdList.get(pos).productId + "")
                    .build());
        }
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
        public void onProductFragmentInteraction(int position);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(UserProfileFragment.Profile_Prod_Tap)
                    .put(AppConstants.USER_ID, Utils.getUserId(getActivity()))
                    .build());
        }
    }
}
