package com.blakit.petrenko.habits;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.MarginDecoration;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by user_And on 04.12.2015.
 */
public class HabitListFragment extends Fragment {

    public static final int ORDER_POPULAR = 0;
    public static final int ORDER_NEW = 1;
    public static final int ORDER_ALL = 2;

    private Realm realm;

    private int order;
    private HabitAdapter adapter;

    private RealmResults<Habit> habits;


    public static HabitListFragment newInstance(Realm realm, int order) {
        HabitListFragment fragment = new HabitListFragment();
//        fragment.setRealm(realm);
        fragment.setOrder(order);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_list, container, false);

        realm = Realm.getDefaultInstance();

        if (savedInstanceState != null) {
            order = savedInstanceState.getInt("order");
        }

        HabitDao habitDao = new HabitDao(realm);
        switch (order) {
            case ORDER_POPULAR:
                habits = habitDao.getPopularHabits();
                break;
            case ORDER_NEW:
                habits = habitDao.getNewHabits();
                break;
            case ORDER_ALL:
                habits = habitDao.getHabitsSortedNames();
        }

        habits.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "onChange triggered", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new HabitAdapter(getActivity(), habits);

        int spanCount = 1;
        if (Utils.isActivityLand(getActivity())) {
            spanCount = 2;
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.habit_list_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        recyclerView.addItemDecoration(new MarginDecoration(getActivity()));
        recyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        habits.removeChangeListeners();
        realm.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("order", order);
        super.onSaveInstanceState(outState);
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
