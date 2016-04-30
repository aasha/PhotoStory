package com.pixtory.app.adapters;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.pixtory.app.R;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.Product;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.typeface.FontBrandonLight;
import com.pixtory.app.typeface.FontBrandonMed;
import com.pixtory.app.typeface.Intro;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;
import com.pixtory.app.utils.ViewUtils;

import java.util.ArrayList;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class ProductRecommendationAdapter extends RecyclerView.Adapter<ProductRecommendationAdapter.ProductViewHolder> {

    private Context mCtx;
    public ProductViewHolder.FollowClickListener followClickListener;

    public ProductRecommendationAdapter(Context ctx) {
        mCtx = ctx;
    }
    static CardView mPreviousSelectedCard = null;
    private ArrayList<Product> mData = null;
    static int mSelectedPosition = -1;
    private static final String Reco_Card_Swipe = "Reco_Card_Swipe";

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.product_reco_adapter_layout, viewGroup, false);

        return new ProductViewHolder(mCtx, itemView, followClickListener, new ProductViewHolder.BookMarkClickListener() {
            @Override
            public void onBookMarkClick(final View caller, int pos) {
                Product pd = mData.get(pos);
                if (pd.bookmarkedByUser == true) {
                    ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mCtx, R.animator.flip_out);
                    anim.setTarget(caller);
                    anim.setDuration(200);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ImageView)caller).setImageResource(R.drawable.bk_like);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    anim.start();
                    pd.bookmarkedByUser = false;
                } else {
                    ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mCtx, R.animator.flip_in);
                    anim.setTarget(caller);
                    anim.setDuration(200);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ImageView)caller).setImageResource(R.drawable.bk_liked);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    anim.start();
                    pd.bookmarkedByUser = true;
                }
                mData.set(pos, pd);
                followClickListener.onBookMarkClick(pos, pd.bookmarkedByUser);
            }

            @Override
            public void onItemSelected(CardView card, int pos) {
                //notifyDataSetChanged();
                if(null != mPreviousSelectedCard){
                    mPreviousSelectedCard.setCardBackgroundColor(Color.TRANSPARENT);
                }
                mPreviousSelectedCard = card;


            }

        });
    }

    public void setSelected(int position){
        mSelectedPosition = position;
        this.notifyDataSetChanged();

    }
    public void setBookMarked(){
        this.notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(ProductViewHolder holder, int i) {
        Product pd = mData.get(i);
        String img = "http://www.sdpb.org/s/photogallery/img/no-image-available.jpg";
        if (pd.productImages != null && !pd.productImages.isEmpty()) {
            img = pd.productImages.get(0);
        }
        Uri uri = Uri.parse(img);
        float ar = i % 2 == 0 ? 1f : .5f;
        //Log.d("AR", "ar = " + ar);
        holder.vImage.setImageURI(uri);
        holder.vImage.setAspectRatio(Float.valueOf(pd.aspectRatio));


        holder.vTxt.setText(pd.productName);
        holder.vMerName.setText(pd.merchantName);

        holder.mPriceView.setVisibility(View.GONE);
        holder.mDiscountView.setVisibility(View.GONE);
        holder.mImgDisc.setVisibility(View.GONE);
        if (pd.contentType.contains("READ") || pd.contentType.contains("Read")) {
            holder.mCtaImg.setImageResource(R.drawable.cta_read);
        } else if (pd.contentType.contains("BUY") || pd.contentType.contains("Buy")) {
            holder.mCtaImg.setImageResource(R.drawable.cta_buy);
            holder.mPriceView.setText(pd.productPrice + "");
            holder.mPriceView.setVisibility(View.VISIBLE);
            if (pd.discountedPercentage != 0) {
                holder.mDiscountView.setText(pd.discountedPercentage + "% OFF");
                holder.mDiscountView.setVisibility(View.VISIBLE);
                holder.mImgDisc.setVisibility(View.VISIBLE);
                holder.mDiscountView.setPivotX(0);
                holder.mDiscountView.setPivotY(0);
                holder.mDiscountView.setRotation(-50);
            } else {
                // ignore
            }
        } else if (pd.contentType.contains("LISTEN") || pd.contentType.contains("Listen")) {
            holder.mCtaImg.setImageResource(R.drawable.cta_listen);
        } else if (pd.contentType.contains("D/L")) {
            holder.mCtaImg.setImageResource(R.drawable.cta_dl);
            holder.mPriceView.setText(pd.productPrice + "");
            holder.mPriceView.setVisibility(View.VISIBLE);
        }else if (pd.contentType.contains("WATCH") || pd.contentType.contains("Watch")) {
            holder.mCtaImg.setImageResource(R.drawable.cta_watch);
        }
        if (pd.bookmarkedByUser) {
            holder.mBookMarkBg.setImageResource(R.drawable.bk_liked);
        } else {
            holder.mBookMarkBg.setImageResource(R.drawable.bk_like);
        }
        if(mSelectedPosition != i){
            holder.mCardView.setCardBackgroundColor(Color.TRANSPARENT);
        }else {
            holder.mCardView.setCardBackgroundColor(Color.RED);
        }
        AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(Reco_Card_Swipe)
                .put(AppConstants.USER_ID, Utils.getUserId(mCtx))
                .put(AppConstants.PRODUCT_ID, "" + pd.productId)
                .build());
    }

    public void setData(ArrayList<Product> pd) {
        mData = pd;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        protected CardView mCardView = null;
        protected SimpleDraweeView vImage;
        protected TextView vTxt;
        protected TextView vMerName;
        protected TextView mPriceView;
        protected TextView mDiscountView;
        protected ImageView mImgDisc;
        protected ImageView mCtaImg = null;
        public FollowClickListener mClickListener;
        public BookMarkClickListener mListener;
        protected ImageView mBookMarkBg = null;
        private Context mCtxt;
        public ProductViewHolder(Context ctxt, View v, FollowClickListener followClickListener, BookMarkClickListener bookMarkClickListener) {
            super(v);
            mCtxt = ctxt;
            mCardView = (CardView) v;
            mClickListener = followClickListener;
            mListener = bookMarkClickListener;
            vImage = (SimpleDraweeView) v.findViewById(R.id.pic);
            vTxt = (TextView) v.findViewById(R.id.product_name);
            Dekar.applyFont(v.getContext(), vTxt);

            vMerName = (TextView) v.findViewById(R.id.merc_name);
            Intro.applyFont(v.getContext(), vMerName);

            mPriceView = (TextView) v.findViewById(R.id.mrp);
            FontBrandonLight.applyFont(v.getContext(), mPriceView);

            mDiscountView = (TextView) v.findViewById(R.id.prd_discount);
            FontBrandonMed.applyFont(v.getContext(), mDiscountView);
            mImgDisc =(ImageView)v.findViewById(R.id.img_discount);
            mBookMarkBg = (ImageView) v.findViewById(R.id.btnBookmark);
            mBookMarkBg.setOnClickListener(this);

            mCtaImg =(ImageView)v.findViewById(R.id.cta_img);
            ViewUtils.expandTouchArea(mBookMarkBg, 8);

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemSelected(mCardView, getAdapterPosition());
                    mCardView.setCardBackgroundColor(Color.RED);
                    mClickListener.onFollowClick(mCardView, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnBookmark) {
                mListener.onBookMarkClick(v, getAdapterPosition());
            }

        }
        static interface  BookMarkClickListener{
            public void onBookMarkClick(View caller, int pos);
            public void onItemSelected(CardView card, int pos);
        }
        public static interface FollowClickListener {
            public void onFollowClick(View caller, int pos);
            public void onBookMarkClick(int pos, boolean value);

        }
    }




}
