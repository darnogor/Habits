package com.blakit.petrenko.habits;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.AppBarStateChangeListener;
import com.blakit.petrenko.habits.view.DaysItemDecoration;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.HeaderDecoration;
import com.blakit.petrenko.habits.view.MarginDecoration;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.parceler.Parcels;

import io.realm.RealmList;

public class AddHabitDetailsActivity extends AppCompatActivity {

    private Habit habit;

    private FontTextView dayView;
    private FontTextView actionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit_details);

        habit = Parcels.unwrap(getIntent().getParcelableExtra("habit"));

        initToolbar();
        initHeaderAndDescription();
        initActions();
        initVideos();
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
        headerImage.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getRandomColor()));

        FontTextView title       = (FontTextView) findViewById(R.id.add_habit_details_title);
        FontTextView category    = (FontTextView) findViewById(R.id.add_habit_details_category);
        FontTextView description = (FontTextView) findViewById(R.id.add_habit_details_description);

        title.setText(habit.getName());
        //category.setText(Utils.getStringByResName(this, habit.getCategory().getNameRes()));
        description.setText(habit.getDescription());
    }


    private void initActions() {
        dayView     = (FontTextView) findViewById(R.id.add_habit_details_day);
        actionView  = (FontTextView) findViewById(R.id.add_habit_details_action);

        RecyclerView daysRecyclerView = (RecyclerView) findViewById(R.id.add_habit_details_days_recyclerView);

        daysRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        daysRecyclerView.addItemDecoration(new DaysItemDecoration(this));
        daysRecyclerView.setAdapter(new DaysAdapter(this, habit.getActions()));
    }


    private void initVideos() {
        RecyclerView videosRecyclerView = (RecyclerView) findViewById(R.id.add_habit_details_videos_recyclerView);

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.addItemDecoration(new MarginDecoration(this));
        videosRecyclerView.addItemDecoration(HeaderDecoration.with(videosRecyclerView)
                    .inflate(R.layout.video_item_header)
                    .parallax(0.5f)
                    .build());
        videosRecyclerView.setAdapter(new VideoItemAdapter(this, habit.getRelatedVideoItems()));
    }


    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_habit_details_add);
        fab.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_add)
                .color(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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


    private class DaysAdapter extends RecyclerView.Adapter<DaysViewHolder> {

        private Context context;
        private RealmList<Action> actions;

        private int lastClickPosition = 0;
        private int colorClicked;
        private int colorNormal;

        private float sp;

        public DaysAdapter(Context context, RealmList<Action> actions) {
            this.context = context;
            this.actions = actions;

            colorClicked = ContextCompat.getColor(context, R.color.md_white_1000);
            colorNormal = ContextCompat.getColor(context, R.color.md_grey_700);

            sp = context.getResources().getDisplayMetrics().scaledDensity;
        }


        @Override
        public DaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.day_item, parent, false);
            return new DaysViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final DaysViewHolder holder, final int position) {
            final Action action = Utils.getAction(actions, position + 1);

            String text = ""+action.getDay();

            TextDrawable numberDr = TextDrawable.builder()
                    .beginConfig()
                        .useFont(holder.number.getTypeface())
                        .fontSize((int) (holder.number.getTextSize() + 2 * sp))
                    .endConfig()
                    .buildRound(text, 0x0000);

            holder.fab.setImageDrawable(numberDr);
            holder.number.setText(text);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastClickPosition != position) {
                        lastClickPosition = position;
                        notifyDataSetChanged();
                    }
                }
            });

            if (lastClickPosition == position) {
                holder.number.setTextColor(colorClicked);
                holder.fab.setVisibility(View.VISIBLE);
                startFabAnimation(holder.fab);

                dayView.setText(context.getString(R.string.day)+" "+action.getDay());
                actionView.setText(action.getAction());
            } else {
                holder.number.setTextColor(colorNormal);
                holder.fab.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return actions.size();
        }


        void startFabAnimation(View fab) {
            ScaleAnimation animation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);

            fab.startAnimation(animation);
        }
    }



    private class DaysViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private FloatingActionButton fab;
        private FontTextView number;

        public DaysViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            fab = (FloatingActionButton) itemView.findViewById(R.id.day_item_fab);
            number = (FontTextView) itemView.findViewById(R.id.day_item_number);


        }
    }
}
