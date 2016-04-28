package com.blakit.petrenko.habits.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blakit.petrenko.habits.AddHabitDetailsActivity;
import com.blakit.petrenko.habits.CreateHabitActivity;
import com.blakit.petrenko.habits.HabitApplication;
import com.blakit.petrenko.habits.HabitDetailsActivity;
import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.wnafee.vector.MorphButton;

import org.parceler.Parcels;

import java.util.List;

import io.realm.RealmResults;

/**
 * Created by user_And on 04.12.2015.
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public static final int HABIT_VIEW_TYPE = 0;
    public static final int HABIT_WITH_PROGRESS_VIEW_TYPE = 1;

    private Context context;
    private UserDao userDao;
    private List<Habit> habits;
    private List<HabitDetails> habitDetailses;
    private boolean isUseOnlyHabitDetails;

    public HabitAdapter(Context context, UserDao userDao, List<Habit> habits, List<HabitDetails> habitDetailses) {
        this.context = context;
        this.userDao = userDao;
        this.habits = habits;
        this.habitDetailses = habitDetailses;
        this.isUseOnlyHabitDetails = false;
    }

    public HabitAdapter(Context context, UserDao userDao, List<HabitDetails> habitDetailses) {
        this.context = context;
        this.userDao = userDao;
        this.habitDetailses = habitDetailses;
        this.isUseOnlyHabitDetails = true;
    }


    private String getUsername() {
        return HabitApplication.getInstance().getUsername();
    }


    @Override
    public int getItemViewType(int position) {
        if (isUseOnlyHabitDetails) {
            return HABIT_WITH_PROGRESS_VIEW_TYPE;
        }

        String habitId = habits.get(position).getId();

        for (HabitDetails hd: habitDetailses) {
            if (hd.getHabit().getId().equals(habitId)) {
                return HABIT_WITH_PROGRESS_VIEW_TYPE;
            }
        }

        return HABIT_VIEW_TYPE;
    }


    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HABIT_WITH_PROGRESS_VIEW_TYPE) {
            return new HabitDetailsViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_habit_with_progress, parent, false));
        }
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HabitViewHolder holder, int position) {
        final Habit habit = (isUseOnlyHabitDetails) ?
                habitDetailses.get(position).getHabit() : habits.get(position);

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AlertDialogTheme);

        holder.addHabitDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_add_habit_title)
                .setMessage(R.string.add_habit_details_dialog_add_habit_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User user = userDao.getUserByName(HabitApplication.getInstance().getUsername());
                        userDao.createOrUpdateHabitDetails(new HabitDetails(user, habit));
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        holder.createCopyDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.add_habit_details_dialog_create_copy_title)
                .setMessage(R.string.add_habit_details_dialog_create_copy_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity activity = (Activity) context;

                        Intent intentCreate = new Intent(activity, CreateHabitActivity.class);

                        intentCreate.putExtra("habit", Parcels.wrap(Habit.class, habit));
                        intentCreate.putExtra("is_edit", false);
                        intentCreate.putExtra("accName", getUsername());

                        activity.startActivity(intentCreate);
                        activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;

                Intent intent = new Intent(activity, AddHabitDetailsActivity.class);
                intent.putExtra("habit", Parcels.wrap(Habit.class, habit));

                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.enter_from_bottom, 0);
            }
        });
        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                holder.author.setMaxWidth((int) (holder.itemView.getWidth() / 2.205));
            }
        });

        holder.symbol.setText(new String("" + habit.getName().charAt(0)).toUpperCase());
        holder.symbolBack.setForegroundTintList(ColorStateList.valueOf(Color.parseColor(habit.getCategory().getColor())));
        holder.number.setText(position+1+".");
        holder.title.setText(habit.getName());
        holder.author.setText(habit.getAuthor());
        holder.addCount.setText(habit.getAddCount()+"");
        holder.completeCount.setText(habit.getCompleteCount() + "");

        final PopupMenu menu = new PopupMenu(context, holder.menu);
        menu.inflate(R.menu.menu_add_habit_item);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int color = ContextCompat.getColor(context, R.color.md_grey_600);
                switch (item.getItemId()) {
                    case R.id.menu_add_habit_item_add:
                        holder.addHabitDialog.show();
                        holder.addHabitDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                        holder.addHabitDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
                        return true;

                    case R.id.menu_add_habit_item_create_copy:
                        holder.createCopyDialog.show();
                        holder.createCopyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                        holder.createCopyDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
                        return true;
                }
                return false;
            }
        });

        if (holder instanceof HabitDetailsViewHolder) {
            HabitDetailsViewHolder vh = (HabitDetailsViewHolder) holder;
            HabitDetails habitDetails = null;

            if (isUseOnlyHabitDetails) {
                habitDetails = habitDetailses.get(position);
            } else {
                for (HabitDetails hd: habitDetailses) {
                    if (hd.getHabit().getId().equals(habit.getId())) {
                        habitDetails = hd;
                        break;
                    }
                }
            }

            int progressColor = Utils.getColorMaterialByProgress(habitDetails);

            LayerDrawable progressDrawable      = (LayerDrawable) vh.progressBar.getProgressDrawable();
            Drawable foregroundProgressDrawable = progressDrawable.findDrawableByLayerId(android.R.id.progress);

            foregroundProgressDrawable.setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);

            vh.progressBar.setProgress((int) (Utils.getPercent(habitDetails) * 100));

            int lastDay = 1;
            for (Action a: habitDetails.getHabit().getActions()) {
                if (a.getDay() > lastDay) {
                    lastDay = a.getDay();
                }
            }
            vh.progressNumbersTextView.setText(habitDetails.getCurrentDay() + "/" + lastDay);

            if (habitDetails.isComplete()) {
                vh.statusTextView.setText(context.getString(R.string.habit_item_progress_status,
                        context.getString(R.string.habit_details_status_complete)));

                String progressNumbers = vh.progressNumbersTextView.getText().toString();

                vh.progressNumbersTextView.setText(Html.fromHtml("<b>" + progressNumbers + "</b>"));
            } else if (habitDetails.isChecked()) {
                vh.statusTextView.setText(context.getString(R.string.habit_item_progress_status,
                        context.getString(R.string.habit_details_status_ok)));
            } else {
                vh.statusTextView.setText(context.getString(R.string.habit_item_progress_status,
                        context.getString(R.string.habit_details_status_wait)));
            }

            final HabitDetails finalHabitDetails = habitDetails;
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = (Activity) context;

                    Intent intent = new Intent(activity, HabitDetailsActivity.class);
                    intent.putExtra("habit_details_id", finalHabitDetails.getId());

                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.enter_from_bottom, 0);
                }
            });

            menu.getMenu().findItem(R.id.menu_add_habit_item_add)
                    .setVisible(false);
        }

        holder.menu.setIcon(GoogleMaterial.Icon.gmd_more_vert);
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isUseOnlyHabitDetails) {
            return habitDetailses.size();
        }
        Log.d("!!!!Message count:", habits.size()+"");
        return habits.size();
    }

    public class HabitViewHolder extends RecyclerView.ViewHolder {

        protected View itemView;

        protected MorphButton symbolBack;
        protected TextView symbol;
        protected FontTextView number;
        protected FontTextView title;
        protected IconicsImageView menu;
        protected FontTextView author;
        protected FontTextView addCount;
        protected FontTextView completeCount;

        private AlertDialog addHabitDialog;
        protected AlertDialog createCopyDialog;

        public HabitViewHolder(View view) {
            super(view);

            itemView = view.findViewById(R.id.habit_item_card);

            symbolBack    = (MorphButton) view.findViewById(R.id.habit_item_symbol_back);
            symbol        = (TextView) view.findViewById(R.id.habit_item_symbol_text);
            number        = (FontTextView) view.findViewById(R.id.habit_item_number);
            title         = (FontTextView) view.findViewById(R.id.habit_item_title);
            menu          = (IconicsImageView) view.findViewById(R.id.habit_item_menu);
            author        = (FontTextView) view.findViewById(R.id.habit_item_author);
            addCount      = (FontTextView) view.findViewById(R.id.habit_item_add_count);
            completeCount = (FontTextView) view.findViewById(R.id.habit_item_complete_count);

            symbolBack.setClickable(false);
        }
    }


    public class HabitDetailsViewHolder extends HabitViewHolder {

        private FontTextView statusTextView;
        private FontTextView progressNumbersTextView;
        private ProgressBar progressBar;

        public HabitDetailsViewHolder(View view) {
            super(view);

            statusTextView          = (FontTextView) view.findViewById(R.id.habit_item_progress_status);
            progressNumbersTextView = (FontTextView) view.findViewById(R.id.habit_item_progress_numbers);
            progressBar             = (ProgressBar) view.findViewById(R.id.habit_item_progress_bar);
        }
    }
}
