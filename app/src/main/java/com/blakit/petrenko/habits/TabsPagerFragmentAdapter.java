package com.blakit.petrenko.habits;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.realm.Realm;

/**
 * Created by user_And on 30.11.2015.
 */
public class TabsPagerFragmentAdapter extends FragmentStatePagerAdapter {

    private String[] tabTitles;
    private Realm realm;

    public TabsPagerFragmentAdapter(Context context,FragmentManager fm, Realm realm) {
        super(fm);
        tabTitles = new String[]{
                context.getString(R.string.add_habit_tab_categories),
                context.getString(R.string.add_habit_tab_popular),
                context.getString(R.string.add_habit_tab_new),
                context.getString(R.string.add_habit_tab_all)
        };
        this.realm = realm;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CategoryFragment.newInstance(realm);
            case 1:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_POPULAR);
            case 2:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_NEW);
            case 3:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_ALL);
        }
        return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_POPULAR);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
