package com.pixtory.app.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.pixtory.app.R;
import com.pixtory.app.WebviewActivity;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.Product;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.typeface.FontBrandonLight;
import com.pixtory.app.typeface.FontBrandonMed;
import com.pixtory.app.typeface.Intro;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

/**
 * Created by aasha.medhi on 1/14/16.
 */
public class ProductDataBinder {
    private static final String SCREEN_NAME = "PDP";
    private static final String PDP_Card_CTAClick = "PDP_Card_CTAClick";
    private OnBookmarkListener mListener;
    TextView mTextName = null;
    TextView mTextMerchant = null;
    TextView mTextPrice = null;

    ImageView mBtnCta = null;

    TextView mTextDesc = null;

    ImageView mBtnBookmark = null;

    TextView mLabelDesc = null;

    ImageView mImgDiscount = null;

    TextView mTextDiscount = null;

    LinearLayout mLytType = null;

    TextView mTxtType = null;

    TextView mLabelType = null;

    LinearLayout mLytSize = null;

    TextView mTxtSize = null;

    TextView mLabelSize = null;

    LinearLayout mLytStyle = null;

    TextView mTxtStyle = null;

    TextView mLabelStyle = null;

    LinearLayout mLytRating = null;

    TextView mTxtRating = null;

    TextView mLabelRating = null;

    LinearLayout mLytFabric = null;

    TextView mTxtFabric = null;

    TextView mLabelFabric = null;

    LinearLayout mLytLocation = null;

    TextView mTxtLocation = null;

    TextView mLabelLocation = null;

    LinearLayout mLytProductCode = null;

    TextView mTxtProductCode = null;

    TextView mLabelProductCode = null;

    LinearLayout mLytSoldBy = null;

    TextView mTxtSoldBy = null;

    TextView mLabelSoldBy = null;

    LinearLayout mLytCare = null;

    TextView mTxtCare = null;

    TextView mLabelCare = null;

    LinearLayout mLytComments = null;

    TextView mTxtComments = null;

    TextView mLabelComments = null;

    LinearLayout mLytSpecialFeatures = null;

    TextView mTxtSpecialFeatures = null;

    TextView mLabelSpecialFeatures = null;

    SimpleDraweeView mImageProduct = null;

    Context mContext;

    int mPosition;

    public ProductDataBinder(Context c, OnBookmarkListener listener) {
        mContext = c;
        mListener = listener;
    }

    public void bindData(final Product product, ViewGroup parent, int position) {
        mPosition = position;
        mImageProduct = (SimpleDraweeView) parent.findViewById(R.id.image);
        mTextName = (TextView) parent.findViewById(R.id.txtName);
        mTextMerchant = (TextView) parent.findViewById(R.id.txtMerchant);
        mTextPrice = (TextView) parent.findViewById(R.id.txtPrice);
        mBtnCta = (ImageView) parent.findViewById(R.id.btnCta);
        mTextDesc = (TextView) parent.findViewById(R.id.txtDesc);
        mBtnBookmark = (ImageView) parent.findViewById(R.id.btnBookmark);
        mImgDiscount = (ImageView) parent.findViewById(R.id.imgDiscount);
        mLabelDesc = (TextView) parent.findViewById(R.id.lblDesc);
        mTextDiscount = (TextView) parent.findViewById(R.id.txtDiscount);
        mTxtType = (TextView) parent.findViewById(R.id.txtType);
        mLabelType = (TextView) parent.findViewById(R.id.lblType);
        mTxtSize = (TextView) parent.findViewById(R.id.txtSize);
        mLabelSize = (TextView) parent.findViewById(R.id.lblSize);
        mTxtStyle = (TextView) parent.findViewById(R.id.txtStyle);
        mLabelStyle = (TextView) parent.findViewById(R.id.lblStyle);
        mTxtRating = (TextView) parent.findViewById(R.id.txtRating);
        mLabelRating = (TextView) parent.findViewById(R.id.lblRating);
        mTxtFabric = (TextView) parent.findViewById(R.id.txtFabric);
        mLabelFabric = (TextView) parent.findViewById(R.id.lblFabric);
        mTxtLocation = (TextView) parent.findViewById(R.id.txtLocation);
        mLabelLocation = (TextView) parent.findViewById(R.id.lblLocation);
        mTxtProductCode = (TextView) parent.findViewById(R.id.txtProductCode);
        mLabelProductCode = (TextView) parent.findViewById(R.id.lblProductCode);
        mTxtSoldBy = (TextView) parent.findViewById(R.id.txtsoldBy);
        mLabelSoldBy = (TextView) parent.findViewById(R.id.lblsoldBy);
        mTxtCare = (TextView) parent.findViewById(R.id.txtCare);
        mLabelCare = (TextView) parent.findViewById(R.id.lblCare);
        mTxtComments = (TextView) parent.findViewById(R.id.txtComments);
        mLabelComments = (TextView) parent.findViewById(R.id.lblComments);
        mTxtSpecialFeatures = (TextView) parent.findViewById(R.id.txtSpecialFeatures);
        mLabelSpecialFeatures = (TextView) parent.findViewById(R.id.lblSpecialFeatures);
        mLytRating = (LinearLayout) parent.findViewById(R.id.layout_rating);
        mLytStyle = (LinearLayout) parent.findViewById(R.id.layout_style);
        mLytSize = (LinearLayout) parent.findViewById(R.id.layout_size);
        mLytType = (LinearLayout) parent.findViewById(R.id.layout_type);
        mLytSpecialFeatures = (LinearLayout) parent.findViewById(R.id.layout_special_features);
        mLytComments = (LinearLayout) parent.findViewById(R.id.layout_comments);
        mLytCare = (LinearLayout) parent.findViewById(R.id.layout_care);
        mLytSoldBy = (LinearLayout) parent.findViewById(R.id.layout_soldBy);
        mLytProductCode = (LinearLayout) parent.findViewById(R.id.layout_product_code);
        mLytLocation = (LinearLayout) parent.findViewById(R.id.layout_location);
        mLytFabric = (LinearLayout) parent.findViewById(R.id.layout_fabric);
        applyFonts();
        if (product.bookmarkedByUser) {
            mBtnBookmark.setImageResource(R.drawable.bk_liked);
        } else {
            mBtnBookmark.setImageResource(R.drawable.bk_like);
        }
        mBtnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
//                if (product.bookmarkedByUser == false) {
//                    mBtnBookmark.setImageResource(R.drawable.bk_liked);
//                    product.bookmarkedByUser = true;
//                } else {
//                    mBtnBookmark.setImageResource(R.drawable.bk_like);
//                    product.bookmarkedByUser = false;
//                }
                if (product.bookmarkedByUser == true) {
                    flipOutBookMark(v);
                    product.bookmarkedByUser = false;
                } else {
                    flipInBookMArk(v);
                    product.bookmarkedByUser = true;
                }
                if (mListener != null)
                    mListener.onBookMarked(mPosition, product.bookmarkedByUser);
            }
        });
        if (product.productImages != null && !product.productImages.isEmpty()) {
            mImageProduct.setImageURI(Uri.parse(product.productImages.get(0)));
        }
        mTextName.setText(product.productName);
        if (product.discountedPercentage != 0) {
            mTextDiscount.setText(product.discountedPercentage + "% OFF");
            mTextDiscount.setVisibility(View.VISIBLE);
            mImgDiscount.setVisibility(View.VISIBLE);
            mTextDiscount.setPivotX(0);
            mTextDiscount.setPivotY(0);
            mTextDiscount.setRotation(-50);
        } else {
            mTextDiscount.setVisibility(View.GONE);
            mImgDiscount.setVisibility(View.GONE);
        }
        if (product.type != null) {
            mLytType.setVisibility(View.VISIBLE);
            mTxtType.setText(product.type);
        }
        if (product.size != null) {
            mLytSize.setVisibility(View.VISIBLE);
            mTxtSize.setText(product.size);
        }
        if (product.rating != null) {
            mLytRating.setVisibility(View.VISIBLE);
            mTxtRating.setText(product.rating);
        }
        if (product.style != null) {
            mLytStyle.setVisibility(View.VISIBLE);
            mTxtStyle.setText(product.style);
        }
        if (product.material != null) {
            mLytFabric.setVisibility(View.VISIBLE);
            mTxtFabric.setText(product.material);
        }
        if (product.care != null) {
            mLytCare.setVisibility(View.VISIBLE);
            mTxtCare.setText(product.care);
        }
        if (product.location != null) {
            mLytLocation.setVisibility(View.VISIBLE);
            mTxtLocation.setText(product.location);
        }
        if (product.specialFeatures != null && !product.specialFeatures.isEmpty()) {
            mLytSpecialFeatures.setVisibility(View.VISIBLE);
            StringBuilder specialFeatureList = new StringBuilder();
            for (int index = 0; index < product.specialFeatures.size(); index++) {
                specialFeatureList.append(product.specialFeatures.get(index) + "\n");
            }
            mTxtSpecialFeatures.setText(specialFeatureList);
        }
        if (product.comments != null && !product.comments.isEmpty()) {
            mLytComments.setVisibility(View.VISIBLE);
            StringBuilder commentsList = new StringBuilder();
            for (int index = 0; index < product.comments.size(); index++) {
                commentsList.append(product.comments.get(index) + "\n");
            }
            mTxtComments.setText(commentsList);
        }
        if (product.productCode != null) {
            mLytProductCode.setVisibility(View.VISIBLE);
            mTxtProductCode.setText(product.productCode);
        }
        if (product.soldBy != null) {
            mLytSoldBy.setVisibility(View.VISIBLE);
            mTxtSoldBy.setText(product.soldBy);
        }
        mTextMerchant.setText(product.merchantName);
        if (null == product.productPrice) {
            mTextPrice.setVisibility(View.GONE);
        } else {
            mTextPrice.setVisibility(View.VISIBLE);
            mTextPrice.setText(product.productPrice);
        }
        if (product.contentType != null) {
            if (product.contentType.contains("D/L")) {
                mBtnCta.setImageResource(R.drawable.cta_dl);
            } else if (product.contentType.contains("READ") || product.contentType.contains("Read")) {
                mBtnCta.setImageResource(R.drawable.cta_read);
            } else if (product.contentType.contains("LISTEN") || product.contentType.contains("Listen")) {
                mBtnCta.setImageResource(R.drawable.cta_listen);
            } else if (product.contentType.contains("Watch") || product.contentType.contains("WATCH")) {
                mBtnCta.setImageResource(R.drawable.cta_watch);
            } else {
                mBtnCta.setImageResource(R.drawable.cta_buy);
            }
        }
        mBtnCta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmplitudeLog.logEvent(new AmplitudeLog.AppEventBuilder(PDP_Card_CTAClick)
                        .put(AppConstants.USER_ID, Utils.getUserId(mContext))
                        .put(AppConstants.PRODUCT_ID, "" + product.productId)
                        .build());
                if (product.deepLink == true || product.contentType.contains("D/L") || product.contentType.contains("Watch") || product.contentType.contains("WATCH")) {
                    Uri uri = Uri.parse(product.productUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    Intent i = new Intent(mContext, WebviewActivity.class);
                    i.putExtra("WEB_LINK", product.productUrl);
                    mContext.startActivity(i);
                }
            }
        });
        String desc = "";
        try {
            if (product.productDesc != null && !product.productDesc.trim().isEmpty()) {
                desc = product.productDesc;
            } else if (product.productBullet != null && !product.productBullet.trim().isEmpty()) {
                desc = product.productBullet.replace("$*", "\n• ");
            } else if (product.textArea != null && !product.textArea.trim().isEmpty()) {
                desc = product.textArea;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextDesc.setText(desc);
        if(product.contentType.contains("READ") || product.contentType.contains("Read")) {
            mLabelDesc.setText("Excerpt");
            mTextDesc.setEllipsize(TextUtils.TruncateAt.END);
        }
        else {
            mLabelDesc.setText("Details");
        }
    }

    private void applyFonts() {
        Dekar.applyFont(mContext, mTextName);
        Intro.applyFont(mContext, mTextMerchant);
        FontBrandonLight.applyFont(mContext, mTextPrice);
        Intro.applyFont(mContext, mLabelDesc);
        Dekar.applyFont(mContext, mTextDesc);
        Intro.applyFont(mContext, mLabelType);
        Dekar.applyFont(mContext, mTxtType);
        Intro.applyFont(mContext, mLabelSize);
        Dekar.applyFont(mContext, mTxtSize);
        Intro.applyFont(mContext, mLabelFabric);
        Dekar.applyFont(mContext, mTxtFabric);
        Intro.applyFont(mContext, mLabelStyle);
        Dekar.applyFont(mContext, mTxtStyle);
        Intro.applyFont(mContext, mLabelRating);
        Dekar.applyFont(mContext, mTxtRating);
        FontBrandonMed.applyFont(mContext, mTextDiscount);
        Intro.applyFont(mContext, mLabelCare);
        Dekar.applyFont(mContext, mTxtCare);
        Intro.applyFont(mContext, mLabelComments);
        Dekar.applyFont(mContext, mTxtComments);
        Intro.applyFont(mContext, mLabelSpecialFeatures);
        Dekar.applyFont(mContext, mTxtSpecialFeatures);
        Intro.applyFont(mContext, mLabelSoldBy);
        Dekar.applyFont(mContext, mTxtSoldBy);
        Intro.applyFont(mContext, mLabelLocation);
        Dekar.applyFont(mContext, mTxtLocation);
        Intro.applyFont(mContext, mLabelProductCode);
        Dekar.applyFont(mContext, mTxtProductCode);
    }

    public interface OnBookmarkListener {
        public void onBookMarked(int position, boolean value);
    }

    public void setBookMarked(Product product) {
        if (mBtnBookmark == null)
            return;
        if (product.bookmarkedByUser == false) {
            flipOutBookMark(mBtnBookmark);
        } else {
            flipInBookMArk(mBtnBookmark);
        }
    }

    private void flipInBookMArk(final View v) {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.flip_in);
        anim.setTarget(v);
        anim.setDuration(200);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((ImageView) v).setImageResource(R.drawable.bk_liked);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    private void flipOutBookMark(final View v) {
        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext, R.animator.flip_out);
        anim.setTarget(v);
        anim.setDuration(200);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((ImageView) v).setImageResource(R.drawable.bk_like);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }
}
