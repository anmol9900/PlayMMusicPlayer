package com.example.anmolpc.playmmusicplayer;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.anmolpc.playmmusicplayer.adapter.PageAdapter;


public class BaseFragment extends Fragment{

    //Variables
    private static final String TAG = BaseFragment.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static Bundle mBundleTabLayoutState;


    //Constructor
    public BaseFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.base_fragcontent,container,false);
        tabLayout = (TabLayout)v.findViewById(R.id.tabs);
        viewPager = (ViewPager)v.findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getChildFragmentManager()));
        tabLayout.setTabTextColors(Color.WHITE,Color.BLACK);
        tabLayout.setupWithViewPager(viewPager);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleTabLayoutState != null) {
            int pos = mBundleTabLayoutState.getInt("position");
            viewPager.setCurrentItem(pos);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleTabLayoutState = new Bundle();
        int pos = viewPager.getCurrentItem();
        mBundleTabLayoutState.putInt("position",pos);
    }
}
