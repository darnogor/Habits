package com.blakit.petrenko.habits.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;

import com.blakit.petrenko.habits.CategoryFragment;
import com.blakit.petrenko.habits.HabitListFragment;
import com.blakit.petrenko.habits.R;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by user_And on 30.11.2015.
 */
public class TabsPagerFragmentAdapter extends FragmentStatePagerAdapter {

    private List<String> tabTitles;
    private Realm realm;

    private String categoryNameRes;

    public TabsPagerFragmentAdapter(Context context, FragmentManager fm, Realm realm, String categoryNameRes) {
        super(fm);
        this.realm = realm;
        this.categoryNameRes = categoryNameRes;

        tabTitles = new ArrayList<>();

        if (TextUtils.isEmpty(categoryNameRes)) {
            tabTitles.add(context.getString(R.string.add_habit_tab_categories));
        }
        tabTitles.add(context.getString(R.string.add_habit_tab_popular));
        tabTitles.add(context.getString(R.string.add_habit_tab_new));
        tabTitles.add(context.getString(R.string.add_habit_tab_all));
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        if (!TextUtils.isEmpty(categoryNameRes)) {
            ++position;
        }

        switch (position) {
            case 0:
                return CategoryFragment.newInstance(realm);
            case 1:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_POPULAR, categoryNameRes);
            case 2:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_NEW, categoryNameRes);
            case 3:
                return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_ALL, categoryNameRes);
        }

        return HabitListFragment.newInstance(realm, HabitListFragment.ORDER_POPULAR, categoryNameRes);
    }

    @Override
    public int getCount() {
        return tabTitles.size();
    }
}
