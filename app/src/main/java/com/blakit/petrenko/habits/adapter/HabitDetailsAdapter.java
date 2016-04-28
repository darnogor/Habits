package com.blakit.petrenko.habits.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blakit.petrenko.habits.HabitDetailsActivity;
import com.blakit.petrenko.habits.MainActivity;
import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;
import com.wnafee.vector.MorphButton;

import java.util.List;

import io.realm.Realm;

/**
 * Created by user_And on 17.07.2015.
 */
public class HabitDetailsAdapter extends RecyclerView.Adapter<HabitDetailsAdapter.HabitDetailsViewHolder> {

    private Context context;
    private List<HabitDetails> habits;


    public HabitDetailsAdapter(Context context, List<HabitDetails> habits) {
        this.context = context;
        this.habits = habits;
    }


    @Override
    public HabitDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_details, parent, false);
        return new HabitDetailsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final HabitDetailsViewHolder holder, final int position) {
        final HabitDetails hd = habits.get(position);

        updateDay(holder, position);

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AlertDialogTheme);

        holder.checkInDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.habit_details_dialog_check_in_title)
                .setMessage(R.string.habit_details_dialog_check_in_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isChecked = !hd.isChecked();

                        Realm realm = ((MainActivity) context).getRealm();
                        new UserDao(realm).checkInHabitDetails(hd.getId());

                        holder.checkBox.setChecked(isChecked);
                        if(isChecked) {
                            holder.bottomBlock.setCardBackgroundColor(ContextCompat
                                    .getColor(context, R.color.colorPrimary));
                            holder.done.setTextColor(ContextCompat
                                    .getColor(context, R.color.md_white_1000));
                        } else {
                            holder.bottomBlock.setCardBackgroundColor(ContextCompat
                                    .getColor(context, R.color.md_white_1000));
                            holder.done.setTextColor(ContextCompat
                                    .getColor(context, R.color.textColor));
                        }
                        updateDay(holder, position);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        holder.name.setText(hd.getHabit().getName());
        holder.action.setText(Utils.getAction(hd.getHabit(), hd.getCurrentDay()).getAction());
        holder.checkBox.setChecked(hd.isChecked());

        if(hd.isChecked()) {
            holder.bottomBlock.setCardBackgroundColor(ContextCompat
                    .getColor(context, R.color.colorPrimary));
            holder.done.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000));
        } else {
            holder.bottomBlock.setCardBackgroundColor(ContextCompat
                    .getColor(context, R.color.md_white_1000));
            holder.done.setTextColor(ContextCompat
                    .getColor(context, R.color.textColor));
        }

        Utils.setMaxLinesByMaxHeight(holder.name, Utils.dpToPx(context, 77));
        Utils.setMaxLinesByMaxHeight(holder.action, Utils.dpToPx(context, 80));

        View.OnClickListener checkedListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int color = ContextCompat.getColor(context, R.color.md_grey_600);
                holder.checkInDialog.show();
                holder.checkInDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                holder.checkInDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        };

        holder.topBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HabitDetailsActivity.class);
                intent.putExtra("habit_details_id", hd.getId());

                context.startActivity(intent);
                ((MainActivity) context).overridePendingTransition(R.anim.enter_from_bottom, 0);
            }
        });
        holder.bottomBlock.setOnClickListener(checkedListener);
        holder.checkBox.setOnClickListener(checkedListener);
    }


    private void updateDay(HabitDetailsViewHolder holder, int position) {
        final HabitDetails hd = habits.get(position);

        holder.dayImage.setForegroundTintList(
                ColorStateList.valueOf(Utils.getColorMaterialByProgress(hd)));
        holder.dayText.setText(""+hd.getCurrentDay());
    }


    @Override
    public int getItemCount() {
        if (habits == null) {
            return 0;
        }
        return habits.size();
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    public List<HabitDetails> getHabits() {
        return habits;
    }


    public void setHabits(List<HabitDetails> habits) {
        this.habits = habits;
    }


    private void setChecked(HabitDetails hd, boolean isChecked) {
        Realm realm = ((MainActivity) context).getRealm();
        realm.beginTransaction();

        hd.setChecked(isChecked);

        int currentDay = hd.getCurrentDay();
        boolean isMaxDay = true;

        for (Action a: hd.getHabit().getActions()) {
            if (currentDay < a.getDay()) {
                isMaxDay = false;
                break;
            }
        }

        hd.setComplete(isChecked && isMaxDay);

        realm.commitTransaction();
    }


    public class HabitDetailsViewHolder extends RecyclerView.ViewHolder {
        private CardView topBlock;
        private CardView bottomBlock;
        private MorphButton dayImage;
        private FontTextView dayText;
        private TextView name;
        private TextView action;
        private CheckBox checkBox;
        private TextView done;

        private AlertDialog checkInDialog;

        public HabitDetailsViewHolder(View itemView) {
            super(itemView);

            topBlock    = (CardView) itemView.findViewById(R.id.my_habits_top_block);
            bottomBlock = (CardView) itemView.findViewById(R.id.my_habits_bottom_block);

            dayImage  = (MorphButton) itemView.findViewById(R.id.my_habits_day_image);
            dayText      = (FontTextView) itemView.findViewById(R.id.my_habits_day_text);
            name     = (TextView) itemView.findViewById(R.id.my_habits_name_block);
            action   = (TextView) itemView.findViewById(R.id.my_habits_action_block);

            done     = (TextView) itemView.findViewById(R.id.textView_done);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
