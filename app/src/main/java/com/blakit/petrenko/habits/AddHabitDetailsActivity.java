package com.blakit.petrenko.habits;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blakit.petrenko.habits.adapter.ArticleLinearAdapter;
import com.blakit.petrenko.habits.adapter.DaysAdapter;
import com.blakit.petrenko.habits.adapter.VideoLinearAdapter;
import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AppBarStateChangeListener;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.decoration.DaysItemDecoration;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.parceler.Parcels;

import io.realm.Realm;

public class AddHabitDetailsActivity extends AppCompatActivity {

    private Realm realm;
    private Habit habit;
    private HabitDetails habitDetails;

    private int colorCategory = ColorGenerator.MATERIAL.getRandomColor();

    private AlertDialog addHabitDialog;
    private AlertDialog createCopyDialog;
    private AlertDialog editDialog;
    private AlertDialog deleteDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit_details);

        realm = Realm.getDefaultInstance();

        initData();
        initToolbar();
        initDialogs();
        initHeaderAndDescription();
        initActions();
        initVideosAndArticles();
        initFab();
    }


    private void initDialogs() {
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(this, R.style.AlertDialogTheme);

        addHabitDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_add_habit_title)
                .setMessage(R.string.add_habit_details_dialog_add_habit_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserDao userDao = new UserDao(realm);
                        userDao.createOrUpdateHabitDetails(habitDetails);

                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        createCopyDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_create_copy_title)
                .setMessage(R.string.add_habit_details_dialog_create_copy_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentCreate = new Intent(AddHabitDetailsActivity.this, CreateHabitActivity.class);

                        intentCreate.putExtra("habit", Parcels.wrap(Habit.class, habit));
                        intentCreate.putExtra("is_edit", false);
                        intentCreate.putExtra("accName", habitDetails.getUser().getName());

                        startActivity(intentCreate);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        editDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_edit_title)
                .setMessage(R.string.add_habit_details_dialog_edit_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentEdit = new Intent(AddHabitDetailsActivity.this, CreateHabitActivity.class);

                        intentEdit.putExtra("habit", Parcels.wrap(Habit.class, habit));
                        intentEdit.putExtra("is_edit", true);
                        intentEdit.putExtra("accName", habitDetails.getUser().getName());

                        startActivity(intentEdit);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        deleteDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_delete_title)
                .setMessage(R.string.add_habit_details_dialog_delete_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new HabitDao(realm).setDeleteHabit(habit.getId());
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();
    }


    private void initData() {
        User user = new UserDao(realm).getUserByName(HabitApplication.getInstance().getUsername());

        habit = Parcels.unwrap(getIntent().getParcelableExtra("habit"));
        habitDetails = new HabitDetails(user, habit);

        colorCategory = Color.parseColor(habit.getCategory().getColor());
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

        collapsingToolbarLayout.setContentScrimColor(colorCategory);
        collapsingToolbarLayout.setStatusBarScrimColor(colorCategory);

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
        headerImage.setImageDrawable(new ColorDrawable(colorCategory));

        FontTextView title         = (FontTextView) findViewById(R.id.add_habit_details_title);
        FontTextView category      = (FontTextView) findViewById(R.id.add_habit_details_category);
        FontTextView description   = (FontTextView) findViewById(R.id.add_habit_details_description);
        FontTextView author        = (FontTextView) findViewById(R.id.add_habit_details_author);
        FontTextView addCount      = (FontTextView) findViewById(R.id.add_habit_details_add_count);
        FontTextView completeCount = (FontTextView) findViewById(R.id.add_habit_details_complete_count);

        title.setText(habit.getName());
        category.setText(Utils.getStringByResName(this, habit.getCategory().getNameRes()));
        description.setText(habit.getDescription());

        author.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_author) + " <b>" + habit.getAuthor() + "</b>"));
        addCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_add_count) + " <b>" + habit.getAddCount() + "</b>"));
        completeCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_complete_count) + " <b>" + habit.getCompleteCount() + "</b>"));
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
        RelativeLayout videosLayout       = (RelativeLayout) findViewById(R.id.add_habit_details_videos_layout);
        RelativeLayout articlesLayout     = (RelativeLayout) findViewById(R.id.add_habit_details_articles_layout);
        RecyclerView videosRecyclerView   = (RecyclerView) findViewById(R.id.add_habit_details_videos_recyclerView);
        RecyclerView articlesRecyclerView = (RecyclerView) findViewById(R.id.add_habit_details_articles_recyclerView);
        FontTextView videosShowAll        = (FontTextView) findViewById(R.id.add_habit_details_videos_all);
        FontTextView articlesShowAll      = (FontTextView) findViewById(R.id.add_habit_details_articles_all);

        if (habit.getRelatedVideoItems().isEmpty()) {
            videosLayout.setVisibility(View.GONE);
        } else {
            videosLayout.setVisibility(View.VISIBLE);
        }

        if (habit.getRelatedArticles().isEmpty()) {
            articlesLayout.setVisibility(View.GONE);
        } else {
            articlesLayout.setVisibility(View.VISIBLE);
        }

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.addItemDecoration(new MarginDecoration(this));
        videosRecyclerView.setAdapter(new VideoLinearAdapter(this, videosRecyclerView, habit.getRelatedVideoItems()));
        videosRecyclerView.setNestedScrollingEnabled(false);


        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        articlesRecyclerView.addItemDecoration(new MarginDecoration(this));
        articlesRecyclerView.setAdapter(new ArticleLinearAdapter(this, articlesRecyclerView, habit.getRelatedArticles()));
        articlesRecyclerView.setNestedScrollingEnabled(false);

        videosShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddHabitDetailsActivity.this, ShowAllActivity.class);
                intent.putExtra("show_all_mode", ShowAllActivity.SHOW_ALL_VIDEOS);
                intent.putExtra("habit", Parcels.wrap(Habit.class, habit));

                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
            }
        });
        articlesShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddHabitDetailsActivity.this, ShowAllActivity.class);
                intent.putExtra("show_all_mode", ShowAllActivity.SHOW_ALL_ARTICLES);
                intent.putExtra("habit", Parcels.wrap(Habit.class, habit));

                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
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
                int color = ContextCompat.getColor(AddHabitDetailsActivity.this, R.color.md_grey_600);

                addHabitDialog.show();
                addHabitDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                addHabitDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        });
    }


    private void addHabitClicked() {
        UserDao userDao = new UserDao(realm);
        userDao.createOrUpdateHabitDetails(habitDetails);

        finish();
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

        if (!HabitApplication.getInstance().getUsername()
                .equals(habitDetails.getHabit().getAuthor())) {
            edit.setVisible(false);
            delete.setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int color = ContextCompat.getColor(this, R.color.md_grey_600);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menu_add_habit_details_add:
                addHabitClicked();
                break;

            case R.id.menu_add_habit_details_create_copy:
                createCopyDialog.show();
                createCopyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                createCopyDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
                break;

            case R.id.menu_add_habit_details_edit:
                editDialog.show();
                editDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                editDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
                break;

            case R.id.menu_add_habit_details_delete:
                deleteDialog.show();
                deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                deleteDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
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
