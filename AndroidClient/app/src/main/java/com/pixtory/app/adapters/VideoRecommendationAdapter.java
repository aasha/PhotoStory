package com.pixtory.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.pixtory.app.R;
import com.pixtory.app.app.App;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.typeface.BigNoodleTitling;
import com.pixtory.app.typeface.Dekar;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

/**
 * Created by aasha.medhi on 12/23/15.
 */
public class VideoRecommendationAdapter extends RecyclerView.Adapter<VideoRecommendationAdapter.VideoViewHolder> {

    private Context mCtx;
    public VideoViewHolder.FollowClickListener followClickListener;
    public VideoRecommendationAdapter(Context ctx) {
        mCtx = ctx;
    }
    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.video_reco_adapter_layout, viewGroup, false);
        return new VideoViewHolder(itemView, followClickListener);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int i) {
        ContentData cd = App.getLikedContentData().get(i);
        if(cd.pictureUrl == null)
            cd.pictureUrl = "http://www.sdpb.org/s/photogallery/img/no-image-available.jpg";
        holder.coverImage.setImageURI(Uri.parse(cd.pictureUrl));
        holder.txtOpinion.setText(cd.name);
        if(null != cd.personDetails) {
            holder.txtExpert.setText(cd.personDetails.name);
        }
    }

    @Override
    public int getItemCount() {
        return App.getLikedContentData().size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected SimpleDraweeView coverImage;
        protected TextView txtOpinion;
        protected TextView txtExpert;
        protected TextView txtViews;
        public FollowClickListener mClickListener;
        public VideoViewHolder(View v, FollowClickListener followClickListener) {
            super(v);
            mClickListener = followClickListener;
            coverImage = (SimpleDraweeView) v.findViewById(R.id.yt_preview);
            txtOpinion = (TextView) v.findViewById(R.id.text_Opinion);
            Dekar.applyFont(v.getContext(), txtOpinion);

            txtExpert = (TextView) v.findViewById(R.id.text_Expert);
            BigNoodleTitling.applyFont(v.getContext(), txtExpert);

            txtViews = (TextView) v.findViewById(R.id.text_Views);
            Dekar.applyFont(v.getContext(), txtViews);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
                mClickListener.onFollowClick(v, getAdapterPosition());
        }

        public static interface FollowClickListener {
            public void onFollowClick(View caller, int pos);

        }
    }

}
