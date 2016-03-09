package com.blakit.petrenko.habits;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.view.AppBarStateChangeListener;
import com.blakit.petrenko.habits.view.DaysItemDecoration;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.parceler.Parcels;

import io.realm.Realm;

public class AddHabitDetailsActivity extends AppCompatActivity {

    private Realm realm;
    private Habit habit;
    private HabitDetails habitDetails;

    private int color = ColorGenerator.MATERIAL.getRandomColor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit_details);

        realm = Realm.getDefaultInstance();

        habit = Parcels.unwrap(getIntent().getParcelableExtra("habit"));
        habitDetails = new HabitDetails(HabitApplication.getInstance().getUser(), habit);

        initToolbar();
        initHeaderAndDescription();
        initActions();
        initVideosAndArticles();
        initFab();
    }


    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.add_habit_details_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.add_habit_details_collapsingToolbarLayout);
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.add_habit_details_appBarLayout);

        collapsingToolbarLayout.setContentScrimColor(color);
        collapsingToolbarLayout.setStatusBarScrimColor(color);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener((int) (toolbar.getMeasuredHeight()*1.2)) {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    collapsingToolbarLayout.setTitle("");
                } else {
                    collapsingToolbarLayout.setTitle(habit.getName());
                }
            }
        });
    }


    private void initHeaderAndDescription() {
        ImageView headerImage = (ImageView) findViewById(R.id.add_habit_details_header);
        headerImage.setImageDrawable(new ColorDrawable(color));

        FontTextView title       = (FontTextView) findViewById(R.id.add_habit_details_title);
        FontTextView category    = (FontTextView) findViewById(R.id.add_habit_details_category);
        FontTextView description = (FontTextView) findViewById(R.id.add_habit_details_description);

        title.setText(habit.getName());
        //category.setText(Utils.getStringByResName(this, habit.getCategory().getNameRes()));
        description.setText(habit.getDescription());
    }


    private void initActions() {
        FontTextView dayView     = (FontTextView) findViewById(R.id.add_habit_details_day);
        FontTextView actionView  = (FontTextView) findViewById(R.id.add_habit_details_action);

        RecyclerView daysRecyclerView = (RecyclerView) findViewById(R.id.add_habit_details_days_recyclerView);

        daysRecyclerView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        daysRecyclerView.addItemDecoration(new DaysItemDecoration(this));
        daysRecyclerView.setAdapter(new DaysAdapter(this, dayView, actionView, habit.getActions()));
        daysRecyclerView.setNestedScrollingEnabled(false);
    }


    private void initVideosAndArticles() {
        final RecyclerView videosRecyclerView   = (RecyclerView) findViewById(R.id.add_habit_details_videos_recyclerView);
        RecyclerView articlesRecyclerView = (RecyclerView) findViewById(R.id.add_habit_details_articles_recyclerView);
        FontTextView videosShowAll        = (FontTextView) findViewById(R.id.add_habit_details_videos_all);
        FontTextView articlesShowAll      = (FontTextView) findViewById(R.id.add_habit_details_articles_all);

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.addItemDecoration(new MarginDecoration(this));
        videosRecyclerView.setAdapter(new VideoItemAdapter(this, videosRecyclerView, habit.getRelatedVideoItems()));
        videosRecyclerView.setNestedScrollingEnabled(false);


        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        articlesRecyclerView.addItemDecoration(new MarginDecoration(this));
        articlesRecyclerView.setAdapter(new ArticlesAdapter(this, articlesRecyclerView, habit.getRelatedArticles()));
        articlesRecyclerView.setNestedScrollingEnabled(false);

        videosShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        articlesShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_habit_details_add);
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_add)
                .color(Color.WHITE)
                .sizeDp(16));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHabitClicked();
            }
        });
    }


    private void addHabitClicked() {
        UserDao userDao = new UserDao(realm);
        userDao.createOrUpdateHabitDetails(habitDetails);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_habit_details_toolbar, menu);

        MenuItem edit   = menu.findItem(R.id.menu_add_habit_details_edit);
        MenuItem delete = menu.findItem(R.id.menu_add_habit_details_delete);

        if (!HabitApplication.getInstance().getUser().getName()
                .equals(habitDetails.getHabit().getAuthor())) {
            edit.setVisible(false);
            delete.setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_add_habit_details_add:
                addHabitClicked();
                break;
            case R.id.menu_add_habit_details_create_copy:
                break;
            case R.id.menu_add_habit_details_edit:
                break;
            case R.id.menu_add_habit_details_delete:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.exit_to_bottom);
    }

}
