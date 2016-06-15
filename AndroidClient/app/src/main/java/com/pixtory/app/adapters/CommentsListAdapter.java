package com.pixtory.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixtory.app.R;
import com.pixtory.app.model.CommentData;
import com.pixtory.app.model.ContentData;
import com.pixtory.app.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skakrayne003 on 5/24/2016.
 */

public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.CommentViewHolder> {

    private Context  mContext;
    private ArrayList<CommentData> mDataSetList;

    public CommentsListAdapter(Context context){
        mContext = context;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        private ImageView mImgAvatar;
        private TextView mTVName;
        private TextView mTVShortDesc;
        private TextView mTVDate;
        private TextView mTVCommentText;
        private int id;

        public CommentViewHolder(View view){
            super(view);
            mImgAvatar = (ImageView)view.findViewById(R.id.avatarImg);
            mTVName = (TextView)view.findViewById(R.id.txtName);
            mTVShortDesc = (TextView)view.findViewById(R.id.txtDesc);
            mTVDate = (TextView)view.findViewById(R.id.txtDate) ;
            mTVCommentText = (TextView)view.findViewById(R.id.txtComment);
        }
    }


    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent ,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        CommentData data = mDataSetList.get(position);

        holder.id = data.commentId;
        holder.mTVName.setText(data.personDetails.name);
        if( data.personDetails.imageUrl != null)
            Picasso.with(mContext).load(data.personDetails.imageUrl).fit().into(holder.mImgAvatar);
        else
            holder.mImgAvatar.setImageResource(R.drawable.hexagon_bg);

        holder.mTVShortDesc.setText(data.personDetails.description);
        holder.mTVDate.setText(Utils.getFormattedDate(data.ingestionTime));
        holder.mTVCommentText.setText(data.comment);
    }

    @Override
    public int getItemCount() {
        return mDataSetList.size();
    }

    public void setData(ArrayList<CommentData> contentList) {
        mDataSetList = contentList;
        notifyDataSetChanged();
    }
}
