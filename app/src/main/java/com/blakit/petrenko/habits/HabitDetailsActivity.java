package com.blakit.petrenko.habits;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.blakit.petrenko.habits.adapter.ArticleLinearAdapter;
import com.blakit.petrenko.habits.adapter.DaysAdapter;
import com.blakit.petrenko.habits.adapter.VideoLinearAdapter;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AppBarStateChangeListener;
import com.blakit.petrenko.habits.view.decoration.DaysItemDecoration;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.decoration.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.parceler.Parcels;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class HabitDetailsActivity extends AppCompatActivity {

    private Realm realm;
    private HabitDetails habitDetails;

    private MenuItem notifyMenuItem;

    private DaysAdapter daysAdapter;
    private RecyclerView daysRecyclerView;

    private RelativeLayout reasonBack;
    private FontTextView reasonEmpty;
    private FontTextView reason;

    private int colorCategory = ColorGenerator.MATERIAL.getRandomColor();

    private FontTextView status;
    private FontTextView motivationProgress;
    private ProgressBar progressBar;

    private FontTextView author;
    private FontTextView addCount;
    private FontTextView completeCount;

    private AlertDialog checkInDialog;
    private AlertDialog createCopyDialog;
    private EditReasonDialog editReasonDialog;

    public static Handler finishHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        initDialogs();

        realm = Realm.getDefaultInstance();
        habitDetails = new UserDao(realm)
                .getHabitDetailsById(getIntent().getStringExtra("habit_details_id"));

        if (savedInstanceState != null) {
            editReasonDialog.setIsShowingText(savedInstanceState.getBoolean("edit_reason_is_showing"),
                    savedInstanceState.getString("edit_reason_text"));
        }

        colorCategory = Color.parseColor(habitDetails.getHabit().getCategory().getColor());

        finishHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    finish();
                }
            }
        };

        initToolbar();
        initHeaderAndDescription();
        initActions();
        initProgress();
        initReason();
        initVideosAndArticles();
        initFab();

        habitDetails.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                updateProgress();
                updateReasonViews();
                updateNotificationMenuItem(habitDetails.isNotificationActivated());
            }
        });
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

        collapsingToolbarLayout.setStatusBarScrimColor(colorCategory);
        collapsingToolbarLayout.setContentScrimColor(colorCategory);

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
        headerImage.setImageDrawable(new ColorDrawable(colorCategory));

        FontTextView title       = (FontTextView) findViewById(R.id.habit_details_title);
        FontTextView category    = (FontTextView) findViewById(R.id.habit_details_category);
        FontTextView description = (FontTextView) findViewById(R.id.habit_details_description);

        author      = (FontTextView) findViewById(R.id.habit_details_author);
        addCount      = (FontTextView) findViewById(R.id.habit_details_add_count);
        completeCount = (FontTextView) findViewById(R.id.habit_details_complete_count);

        title.setText(habitDetails.getHabit().getName());
        category.setText(Utils.getStringByResName(this, habitDetails.getHabit().getCategory().getNameRes()));
        description.setText(habitDetails.getHabit().getDescription());

        author.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_author) + " <b>" + habitDetails.getHabit().getAuthor() + "</b>"));
        addCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_add_count) + " <b>" + habitDetails.getHabit().getAddCount() + "</b>"));
        completeCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_complete_count) + " <b>" + habitDetails.getHabit().getCompleteCount() + "</b>"));
    }


    private void initActions() {
        FontTextView dayView     = (FontTextView) findViewById(R.id.habit_details_day);
        FontTextView actionView  = (FontTextView) findViewById(R.id.habit_details_action);

        daysAdapter = new DaysAdapter(this, dayView, actionView, habitDetails.getHabit().getActions());
        daysAdapter.setLastClickPosition(habitDetails.getCurrentDay() - 1);

        daysRecyclerView = (RecyclerView) findViewById(R.id.habit_details_days_recyclerView);

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this, org.solovyev.android.views.llm.LinearLayoutManager.HORIZONTAL, false));
        daysRecyclerView.addItemDecoration(new DaysItemDecoration(this));
        daysRecyclerView.setAdapter(daysAdapter);
        daysRecyclerView.setNestedScrollingEnabled(false);
        daysRecyclerView.getLayoutManager().scrollToPosition(habitDetails.getCurrentDay() - 1);
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
        if (habitDetails.isComplete()) {
            status.setText(R.string.habit_details_status_complete);
        } else if (habitDetails.isChecked()) {
            status.setText(R.string.habit_details_status_ok);
        } else {
            status.setText(R.string.habit_details_status_wait);
        }

        daysRecyclerView.getLayoutManager().scrollToPosition(habitDetails.getCurrentDay() - 1);
        daysAdapter.setLastClickPosition(habitDetails.getCurrentDay() - 1);
        daysAdapter.notifyDataSetChanged();

        //TODO: Motivation string set by progress
        // motivationProgress.setText("");

        int progressColor = Utils.getColorMaterialByProgress(habitDetails);

        LayerDrawable progressDrawable      = (LayerDrawable) progressBar.getProgressDrawable();
        Drawable foregroundProgressDrawable = progressDrawable.findDrawableByLayerId(android.R.id.progress);

        foregroundProgressDrawable.setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);

        progressBar.setProgress((int) (Utils.getPercent(habitDetails) * 100));

        author.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_author) + " <b>" + habitDetails.getHabit().getAuthor() + "</b>"));
        addCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_add_count) + " <b>" + habitDetails.getHabit().getAddCount() + "</b>"));
        completeCount.setText(Html.fromHtml(getString(R.string.add_habit_details_additional_info_complete_count) + " <b>" + habitDetails.getHabit().getCompleteCount() + "</b>"));
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
        RelativeLayout videosLayout       = (RelativeLayout) findViewById(R.id.habit_details_videos_layout);
        RelativeLayout articlesLayout     = (RelativeLayout) findViewById(R.id.habit_details_articles_layout);
        RecyclerView videosRecyclerView   = (RecyclerView) findViewById(R.id.habit_details_videos_recyclerView);
        RecyclerView articlesRecyclerView = (RecyclerView) findViewById(R.id.habit_details_articles_recyclerView);
        FontTextView videosShowAll        = (FontTextView) findViewById(R.id.habit_details_videos_all);
        FontTextView articlesShowAll      = (FontTextView) findViewById(R.id.habit_details_articles_all);

        if (habitDetails.getHabit().getRelatedVideoItems().isEmpty()) {
            videosLayout.setVisibility(View.GONE);
        } else {
            videosLayout.setVisibility(View.VISIBLE);
        }

        if (habitDetails.getHabit().getRelatedArticles().isEmpty()) {
            articlesLayout.setVisibility(View.GONE);
        } else {
            articlesLayout.setVisibility(View.VISIBLE);
        }

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.addItemDecoration(new MarginDecoration(this));
        videosRecyclerView.setAdapter(new VideoLinearAdapter(this, videosRecyclerView, habitDetails.getHabit().getRelatedVideoItems()));
        videosRecyclerView.setNestedScrollingEnabled(false);

        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        articlesRecyclerView.addItemDecoration(new MarginDecoration(this));
        articlesRecyclerView.setAdapter(new ArticleLinearAdapter(this,articlesRecyclerView, habitDetails.getHabit().getRelatedArticles()));
        articlesRecyclerView.setNestedScrollingEnabled(false);

        videosShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitDetailsActivity.this, ShowAllActivity.class);
                intent.putExtra("show_all_mode", ShowAllActivity.SHOW_ALL_VIDEOS);
                intent.putExtra("habit", Parcels.wrap(Habit.class, habitDetails.getHabit()));

                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
            }
        });
        articlesShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitDetailsActivity.this, ShowAllActivity.class);
                intent.putExtra("show_all_mode", ShowAllActivity.SHOW_ALL_ARTICLES);
                intent.putExtra("habit", Parcels.wrap(Habit.class, habitDetails.getHabit()));

                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
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
                int color = ContextCompat.getColor(HabitDetailsActivity.this, R.color.md_grey_600);
                checkInDialog.show();
                checkInDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                checkInDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        });
    }


    private void initDialogs() {
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(this, R.style.AlertDialogTheme);

        checkInDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.habit_details_dialog_check_in_title)
                .setMessage(R.string.habit_details_dialog_check_in_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new UserDao(realm).checkInHabitDetails(habitDetails.getId());
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
                        Intent intentCreate = new Intent(HabitDetailsActivity.this, CreateHabitActivity.class);

                        intentCreate.putExtra("habit", Parcels.wrap(Habit.class, habitDetails.getHabit()));
                        intentCreate.putExtra("is_edit", false);
                        intentCreate.putExtra("accName", habitDetails.getUser().getName());

                        startActivity(intentCreate);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        editReasonDialog = new EditReasonDialog();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_habit_details_toolbar, menu);

        notifyMenuItem = menu.findItem(R.id.menu_habit_details_notify);

        boolean isNotificationActivated = false;
        if (habitDetails != null) {
            isNotificationActivated = habitDetails.isNotificationActivated();
        }
        updateNotificationMenuItem(isNotificationActivated);

        return true;
    }


    private void updateNotificationMenuItem(boolean isNotificationActivated) {
        notifyMenuItem.setIcon(new IconicsDrawable(this)
                .icon((isNotificationActivated) ? GoogleMaterial.Icon.gmd_notifications_active
                                                : GoogleMaterial.Icon.gmd_notifications_off)
                .sizeDp(20)
                .color(ContextCompat.getColor(this, R.color.md_white_1000)));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int color = ContextCompat.getColor(this, R.color.md_grey_600);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_habit_details_notify:
                realm.beginTransaction();
                habitDetails.setNotificationActivated(!habitDetails.isNotificationActivated());
                realm.commitTransaction();
                break;
            case R.id.menu_habit_details_create_copy:
                createCopyDialog.show();
                createCopyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                createCopyDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
                break;
            case R.id.menu_habit_details_settings:
                Intent settings = new Intent(this, HabitDetailsSettingsActivity.class);
                settings.putExtra("habit_details_id", habitDetails.getId());
                startActivity(settings);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
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
