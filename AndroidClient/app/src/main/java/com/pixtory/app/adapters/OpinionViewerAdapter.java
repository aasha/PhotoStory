package com.pixtory.app.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.pixtory.app.app.App;
import com.pixtory.app.fragments.MainFragment;
import com.pixtory.app.model.ContentData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by aasha.medhi on 12/30/15.
 */
public class OpinionViewerAdapter extends PagerAdapter {
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private Fragment mCurrentPrimaryItem = null;
    ArrayList<ContentData> mData = null;
    private Fragment mCurrentHexFragment = null;

    private HashMap<Integer, MainFragment> mFragmentPool = null;

    public OpinionViewerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
        mFragmentPool = new HashMap<Integer, MainFragment>();
    }

    public Fragment getFragmentAtIndex(int position){
        if(mFragments.size() > position)
            return mFragments.get(position);
        return null;
    }
    public Fragment getItem(int position) {
        try {
            int pos = position % mData.size(); //  for testing.. needs to change once new content comes in
            int index = position % 4;
            MainFragment hf = null;
            if (mFragmentPool.get(index) == null) {
                hf = MainFragment.newInstance(position);
                mFragmentPool.put(index, hf);
            } else {
                hf = mFragmentPool.get(index);
                //hf.initForContentIndex(position);
                Bundle args = new Bundle();
                args.putInt(MainFragment.ARG_PARAM1, position);
                hf.setArguments(args);
            }


            return hf;
        }catch (Exception e){
            return MainFragment.newInstance(0);
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public void setData(ArrayList<ContentData> contentList) {
        mData = contentList;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);

        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        mFragments.set(position, null);

        mCurTransaction.remove(fragment);
    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    public Fragment getCurrentFragment() {
        return mCurrentPrimaryItem;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

}
