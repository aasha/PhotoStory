package com.pixtory.app.views;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pixtory.app.R;

public class TestActivity extends AppCompatActivity {

    private CollapsingToolbarLayout mCollapsibleToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mCollapsibleToolBar = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);

    }



}
