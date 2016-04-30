package com.pixtory.app.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.pixtory.app.R;
import com.pixtory.app.adapters.ProductRecommendationAdapter;
import com.pixtory.app.app.App;
import com.pixtory.app.model.Product;
import com.pixtory.app.retrofit.AddCommentResponse;
import com.pixtory.app.retrofit.NetworkApiHelper;
import com.pixtory.app.retrofit.NetworkApiCallback;
import com.pixtory.app.utils.Utils;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class ProductViewerFragment extends Fragment implements ProductRecommendationAdapter.ProductViewHolder.FollowClickListener, ProductDataBinder.OnBookmarkListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private int mProductIndex;

    public OnFragmentInteractionListener mListener;

    private RecyclerView mRecomRecycle = null;
    private LinearLayoutManager mLayoutManager = null;
    private ProductRecommendationAdapter productRecommendationAdapter = null;
    ProductDataBinder mProductDataBinder;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ExpertFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductViewerFragment newInstance(String param1) {
        ProductViewerFragment fragment = new ProductViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public ProductViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                mProductIndex = Integer.parseInt(getArguments().getString(ARG_PARAM1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @SuppressLint("NewApi")
    private int getSoftbuttonsbarHeight() {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
    private int mSoftBarHeight = 0;
    private int mDeviceWidthInPx = 0;
    private int mDeviceHeightInPx = 0;
    @Bind(R.id.prd_detail_layout)
    FrameLayout mProdDetailsLayout = null;
    @Bind(R.id.product_detail)
    RelativeLayout mPDPDetailLayout = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = (View) inflater.inflate(R.layout.product_viewer_layout, container, false);
        ButterKnife.bind(this, mRootView);
        mRecomRecycle = (RecyclerView) mRootView.findViewById(R.id.recom_recy);
        //Set height
        mSoftBarHeight = getSoftbuttonsbarHeight();

        // Device info
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mDeviceWidthInPx = displayMetrics.widthPixels;
        mDeviceHeightInPx = displayMetrics.heightPixels;

        Log.d("TAG", "w:h : sbw =" + mDeviceWidthInPx + ":" + mDeviceHeightInPx + "::" + mSoftBarHeight);
        mDeviceHeightInPx += mSoftBarHeight;
        mProdDetailsLayout.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams params = mProdDetailsLayout.getLayoutParams();
        params.height = (int)(0.65 * mDeviceHeightInPx);
        mProdDetailsLayout.setLayoutParams(params);
        ImageButton closeBtn = (ImageButton) mRootView.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onProductFragmentClose();
            }
        });
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProductDataBinder = new ProductDataBinder(getContext(), this);
        setUpRecomView();
    }

    private void setUpRecomView() {
        mRecomRecycle.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        //mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecomRecycle.setLayoutManager(mLayoutManager);

        productRecommendationAdapter = new ProductRecommendationAdapter(this.getActivity());
        productRecommendationAdapter.followClickListener = this;
        mRecomRecycle.setAdapter(productRecommendationAdapter);
        productRecommendationAdapter.setData(App.getLikedProducts());
        mProductDataBinder.bindData(App.getLikedProducts().get(mProductIndex), mPDPDetailLayout, mProductIndex);
        productRecommendationAdapter.setSelected(mProductIndex);
        mRecomRecycle.scrollToPosition(mProductIndex);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFollowClick(View caller, int pos) {
        mProductDataBinder.bindData(App.getLikedProducts().get(pos), mPDPDetailLayout, pos);
    }

    @Override
    public void onBookMarkClick(int pos, boolean value) {
        List<Product> products = App.getLikedProducts();
        products.get(pos).bookmarkedByUser = value;
        mProductDataBinder.setBookMarked(products.get(pos));
        NetworkApiHelper.getInstance().addComment(Utils.getUserId(getActivity()), products.get(pos).productId, value, new NetworkApiCallback<AddCommentResponse>() {
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
    public interface OnFragmentInteractionListener {
        public void onProductFragmentClose();
    }

    @Override
    public void onBookMarked(int positionOfProduct, boolean value) {
        List<Product> pdList = App.getLikedProducts();
        pdList.get(positionOfProduct).bookmarkedByUser = value;
        productRecommendationAdapter.setData((ArrayList<Product>) pdList);
        productRecommendationAdapter.setBookMarked();
        NetworkApiHelper.getInstance().addComment(Utils.getUserId(getActivity()), pdList.get(positionOfProduct).productId, value, new NetworkApiCallback<AddCommentResponse>() {
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
    }

}
