package com.blakit.petrenko.habits;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.blakit.petrenko.habits.adapter.ArticleTilesAdapter;
import com.blakit.petrenko.habits.adapter.VideoTilesAdapter;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AutofitGridRecyclerView;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;

import org.parceler.Parcels;

public class ShowAllActivity extends AppCompatActivity {

    public static final int SHOW_ALL_VIDEOS   = 0;
    public static final int SHOW_ALL_ARTICLES = 1;

    private int   showAllMode;
    private int   titleRes;
    private Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all);

        showAllMode = getIntent().getIntExtra("show_all_mode", 0);
        habit       = Parcels.unwrap(getIntent().getParcelableExtra("habit"));

        switch (showAllMode) {
            case SHOW_ALL_VIDEOS:
                titleRes = R.string.show_all_videos_title;
                break;
            case SHOW_ALL_ARTICLES:
                titleRes = R.string.show_all_articles_title;
                break;
        }

        initToolbar();
        initRecyclerView();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(titleRes);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void initRecyclerView() {
        AutofitGridRecyclerView recyclerView = (AutofitGridRecyclerView) findViewById(R.id.show_all_recyclerView);

        int columnWidth = -1;
        boolean isLand = Utils.isActivityLand(this);

        if (isLand) {
            columnWidth = Utils.dpToPx(this, 170);
        }

        RecyclerView.Adapter adapter = null;
        switch (showAllMode) {
            case SHOW_ALL_VIDEOS:
                adapter = new VideoTilesAdapter(this, habit.getRelatedVideoItems(), isLand);
                break;
            case SHOW_ALL_ARTICLES:
                adapter = new ArticleTilesAdapter(this, habit.getRelatedArticles(), isLand);
                break;
        }

        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.setColumnWidth(columnWidth);
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
}
