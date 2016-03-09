package com.blakit.petrenko.habits;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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
                .inflate(R.layout.my_habit_item, parent, false);
        return new HabitDetailsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final HabitDetailsViewHolder holder, final int position) {
        final HabitDetails hd = habits.get(position);

        updateDay(holder, position);

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
                boolean isChecked = !hd.isChecked();
                setChecked(hd, isChecked);
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
        realm.commitTransaction();
    }


    public static class HabitDetailsViewHolder extends RecyclerView.ViewHolder {
        private CardView topBlock;
        private CardView bottomBlock;
        private MorphButton dayImage;
        private FontTextView dayText;
        private TextView name;
        private TextView action;
        private CheckBox checkBox;
        private TextView done;

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
