package com.pixtory.app.fragments;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixtory.app.R;
import com.pixtory.app.app.AppConstants;
import com.pixtory.app.utils.AmplitudeLog;
import com.pixtory.app.utils.Utils;

/**
 * Created by Sonali Kakrayne on 5/23/2016.
 */

public class CommentsDialogFragment extends DialogFragment{

    private EditText mCommentText;
    private Button mPostCommentBtn;
    private ImageView mCloseBtn;
    private OnAddCommentButtonClickListener mAddCommentListener;

    /**
     * Empty Constructor
     */
    public CommentsDialogFragment(){

    }

    public static CommentsDialogFragment newInstance(String title) {
        CommentsDialogFragment frag = new CommentsDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);

        if(!(getActivity() instanceof OnAddCommentButtonClickListener)){
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnAddCommentButtonClickListener");
        }

        mAddCommentListener = (OnAddCommentButtonClickListener)getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().setCanceledOnTouchOutside(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setCancelable(false);
        return inflater.inflate(R.layout.fragment_post_comment, container);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCommentText = (EditText) view.findViewById(R.id.et_comment);
        mCloseBtn = (ImageView)view.findViewById(R.id.closeBtn);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(params);


        mCommentText.requestFocus();
        mPostCommentBtn = (Button)view.findViewById(R.id.postCommentBtn);
        mPostCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((mCommentText.getText().toString()).equals("")){
                    Toast.makeText(getActivity(),"You have not entered any comment!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    mAddCommentListener.onAddCommentButtonClicked(mCommentText.getText().toString());
                    dismiss();
                }

            }
        });
    }

    public interface OnAddCommentButtonClickListener{
        void onAddCommentButtonClicked(String str);
    }
}
