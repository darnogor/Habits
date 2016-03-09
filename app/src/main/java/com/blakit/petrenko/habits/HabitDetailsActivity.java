package com.blakit.petrenko.habits;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AppBarStateChangeListener;
import com.blakit.petrenko.habits.view.DaysItemDecoration;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import io.realm.Realm;

public class HabitDetailsActivity extends AppCompatActivity {

    private Realm realm;
    private HabitDetails habitDetails;

    private EditReasonDialog editReasonDialog;

    private RelativeLayout reasonBack;
    private FontTextView reasonEmpty;
    private FontTextView reason;

    private int color = ColorGenerator.MATERIAL.getRandomColor();

    private FontTextView status;
    private FontTextView motivationProgress;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        initDialogs();

        realm = Realm.getDefaultInstance();
        habitDetails = realm.where(HabitDetails.class)
                            .equalTo("id", getIntent().getStringExtra("habit_details_id"))
                            .findFirst();

        if (savedInstanceState != null) {
            editReasonDialog.setIsShowingText(savedInstanceState.getBoolean("edit_reason_is_showing"),
                    savedInstanceState.getString("edit_reason_text"));
        }

        initToolbar();
        initHeaderAndDescription();
        initActions();
        initProgress();
        initReason();
        initVideosAndArticles();
        initFab();
    }


    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.habit_details_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.habit_details_collapsingToolbarLayout);
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.habit_details_appBarLayout);

        collapsingToolbarLayout.setStatusBarScrimColor(color);
        collapsingToolbarLayout.setContentScrimColor(color);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener((int) (toolbar.getMeasuredHeight() * 1.2)) {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    collapsingToolbarLayout.setTitle("");
                } else {
                    collapsingToolbarLayout.setTitle(habitDetails.getHabit().getName());
                }
            }
        });
    }


    private void initHeaderAndDescription() {
        ImageView headerImage = (ImageView) findViewById(R.id.habit_details_header);
        headerImage.setImageDrawable(new ColorDrawable(color));

        FontTextView title       = (FontTextView) findViewById(R.id.habit_details_title);
        FontTextView category    = (FontTextView) findViewById(R.id.habit_details_category);
        FontTextView description = (FontTextView) findViewById(R.id.habit_details_description);

        title.setText(habitDetails.getHabit().getName());
        //category.setText(Utils.getStringByResName(this, habit.getCategory().getNameRes()));
        description.setText(habitDetails.getHabit().getDescription());
    }


    private void initActions() {
        FontTextView dayView     = (FontTextView) findViewById(R.id.habit_details_day);
        FontTextView actionView  = (FontTextView) findViewById(R.id.habit_details_action);

        RecyclerView daysRecyclerView = (RecyclerView) findViewById(R.id.habit_details_days_recyclerView);

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this, org.solovyev.android.views.llm.LinearLayoutManager.HORIZONTAL, false));
        daysRecyclerView.addItemDecoration(new DaysItemDecoration(this));
        daysRecyclerView.setAdapter(new DaysAdapter(this, dayView, actionView, habitDetails.getHabit().getActions()));
        daysRecyclerView.setNestedScrollingEnabled(false);
    }


    private void initProgress() {
        IconicsImageView shareProgress  = (IconicsImageView) findViewById(R.id.habit_details_progress_share);

        status             = (FontTextView) findViewById(R.id.habit_details_status);
        motivationProgress = (FontTextView) findViewById(R.id.habit_details_progress_motivation);
        progressBar        = (ProgressBar) findViewById(R.id.habit_details_progress_bar);

        shareProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        updateProgress();
    }


    private void updateProgress() {
        if (habitDetails.isChecked()) {
            status.setText(R.string.habit_details_status_ok);
        } else {
            status.setText(R.string.habit_details_status_wait);
        }

        //TODO: Motivation string set by progress
        // motivationProgress.setText("");

        int progressColor = Utils.getColorMaterialByProgress(habitDetails);

        LayerDrawable progressDrawable      = (LayerDrawable) progressBar.getProgressDrawable();
        Drawable foregroundProgressDrawable = progressDrawable.findDrawableByLayerId(android.R.id.progress);

        foregroundProgressDrawable.setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);

        progressBar.setProgress((int) (Utils.getPercent(habitDetails) * 100));
    }


    private void initReason() {
        IconicsImageView editReason = (IconicsImageView) findViewById(R.id.habit_details_reason_edit);

        reasonBack  = (RelativeLayout) findViewById(R.id.habit_details_reason_back);
        reasonEmpty = (FontTextView) findViewById(R.id.habit_details_reason_empty);
        reason      = (FontTextView) findViewById(R.id.habit_details_reason);

        View.OnClickListener editListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReasonDialog.setText(habitDetails.getReason());
                editReasonDialog.show();
            }
        };

        editReason.setOnClickListener(editListener);
        reasonBack.setOnClickListener(editListener);

        updateReasonViews();
    }


    private void updateReasonViews() {
        reason.setText(habitDetails.getReason());

        if (!TextUtils.isEmpty(habitDetails.getReason())) {
            reasonBack.setClickable(false);
            reasonEmpty.setVisibility(View.GONE);
        } else {
            reasonBack.setClickable(true);
            reasonEmpty.setVisibility(View.VISIBLE);
        }
    }


    private void initVideosAndArticles() {
        RecyclerView videosRecyclerView   = (RecyclerView) findViewById(R.id.habit_details_videos_recyclerView);
        RecyclerView articlesRecyclerView = (RecyclerView) findViewById(R.id.habit_details_articles_recyclerView);
        FontTextView videosShowAll        = (FontTextView) findViewById(R.id.habit_details_videos_all);
        FontTextView articlesShowAll      = (FontTextView) findViewById(R.id.habit_details_articles_all);

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.addItemDecoration(new MarginDecoration(this));
        videosRecyclerView.setAdapter(new VideoItemAdapter(this, videosRecyclerView, habitDetails.getHabit().getRelatedVideoItems()));
        videosRecyclerView.setNestedScrollingEnabled(false);

        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        articlesRecyclerView.addItemDecoration(new MarginDecoration(this));
        articlesRecyclerView.setAdapter(new ArticlesAdapter(this,articlesRecyclerView, habitDetails.getHabit().getRelatedArticles()));
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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.habit_details_checkin);
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_check)
                .color(Color.WHITE)
                .sizeDp(16));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDay = habitDetails.getCurrentDay();
                boolean isMaxDay = true;
                for (Action a: habitDetails.getHabit().getActions()) {
                    if (currentDay < a.getDay()) {
                        isMaxDay = false;
                        break;
                    }
                }

                realm.beginTransaction();
                habitDetails.setCurrentDay((isMaxDay) ? 1 : currentDay + 1);
                realm.commitTransaction();

                updateProgress();
            }
        });
    }


    private void initDialogs() {
        editReasonDialog = new EditReasonDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_habit_details_toolbar, menu);

        MenuItem notify = menu.findItem(R.id.menu_habit_details_notify);

        updateNotificationMenuItem(notify);

        return true;
    }


    private void updateNotificationMenuItem(MenuItem notify) {
        notify.setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_notifications_active)
                .sizeDp(20)
                .color(ContextCompat.getColor(this, R.color.md_white_1000)));
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
        overridePendingTransition(0, R.anim.exit_to_bottom);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("edit_reason_is_showing", editReasonDialog.isShowing());
        outState.putString("edit_reason_text", editReasonDialog.getText());

        super.onSaveInstanceState(outState);
    }


    private class EditReasonDialog {
        private AlertDialog dialog;
        private EditText reasonEditText;

        public EditReasonDialog() {
            View view = LayoutInflater.from(HabitDetailsActivity.this)
                    .inflate(R.layout.dialog_edit_reason, null, false);

            reasonEditText = (EditText) view.findViewById(R.id.dialog_edit_reason_editText);

            ContextThemeWrapper themeWrapper
                    = new ContextThemeWrapper(HabitDetailsActivity.this, R.style.AlertDialogTheme);
            dialog = new AlertDialog.Builder(themeWrapper)
                    .setTitle(R.string.edit_reason_title)
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.beginTransaction();
                            habitDetails.setReason(reasonEditText.getText().toString());
                            realm.commitTransaction();
                            updateReasonViews();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true)
                    .create();
        }


        public void show() {
            dialog.show();
            int color = ContextCompat.getColor(HabitDetailsActivity.this, R.color.md_grey_600);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
        }


        public boolean isShowing() {
            return dialog.isShowing();
        }


        public String getText() {
            return reasonEditText.getText().toString();
        }


        public void setText(String text) {
            reasonEditText.setText(text);
        }


        public void setIsShowingText(boolean isShowing, String text) {
            if (isShowing) {
                reasonEditText.setText(text);
                show();
            }
        }
    }
}
