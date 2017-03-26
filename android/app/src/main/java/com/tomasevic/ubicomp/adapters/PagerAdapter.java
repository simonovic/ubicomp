package com.tomasevic.ubicomp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tomasevic.ubicomp.fragments.ChartsFragment;
import com.tomasevic.ubicomp.fragments.DataMapFragment;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Tomasevic on 14.11.2016..
 */

public class PagerAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> fragmentTitleList = new ArrayList<>();

    public PagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    public void addFragment(Fragment fragment, String title)
    {
        fragments.add(fragment);
        fragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return fragmentTitleList.get(position);
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }
}
