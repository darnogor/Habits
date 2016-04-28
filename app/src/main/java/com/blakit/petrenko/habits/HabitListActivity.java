package com.blakit.petrenko.habits;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.blakit.petrenko.habits.adapter.HabitAdapter;
import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class HabitListActivity extends AppCompatActivity {

    public static final int HABITS_CREATED = 0;
    public static final int HABITS_COMPLETED = 1;

    private int habitsType;

    private Realm realm;

    private RealmResults<Habit> habits;
    private RealmResults<HabitDetails> habitDetailses;
    private HabitAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        realm = Realm.getDefaultInstance();

        habitsType = getIntent().getIntExtra("habits_type", HABITS_CREATED);

        initToolbar();
        initRecyclerView();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            switch (habitsType) {
                case HABITS_COMPLETED:
                    getSupportActionBar().setTitle(R.string.nav_menu_item_complete);
                    break;
                case HABITS_CREATED:
                    getSupportActionBar().setTitle(R.string.nav_menu_item_created_habits);
                    break;
            }
        }
    }


    private void initRecyclerView() {
        HabitDao habitDao = new HabitDao(realm);
        UserDao userDao = new UserDao(realm);

        String userId = HabitApplication.getInstance().getUsername();

        RealmChangeListener changeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                adapter.notifyDataSetChanged();
            }
        };

        switch (habitsType) {
            case HABITS_CREATED:
                habits = habitDao.getCreatedHabits(userId);
                habitDetailses = userDao.findAllAvailableHabitHetails(userId);

                habits.addChangeListener(changeListener);
                habitDetailses.addChangeListener(changeListener);

                adapter = new HabitAdapter(this, userDao, habits, habitDetailses);
                break;

            case HABITS_COMPLETED:
                habitDetailses = userDao.findAvailableCompleteHabitDetails(userId);

                habitDetailses.addChangeListener(changeListener);

                adapter = new HabitAdapter(this, userDao, habitDetailses);
        }



        int spanCount = 1;
        if (Utils.isActivityLand(this)) {
            spanCount = 2;
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.habit_list_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left_half, R.anim.exit_to_right);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
    }
}
