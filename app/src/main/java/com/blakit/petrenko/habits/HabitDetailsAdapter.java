package com.blakit.petrenko.habits;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.blakit.petrenko.habits.model.HabitDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user_And on 17.07.2015.
 */
public class HabitDetailsAdapter extends RecyclerView.Adapter<HabitDetailsAdapter.HabitDetailsViewHolder> {

    private static final String PACKAGE_NAME = "com.blakit.petrenko.habits";

    private Context context;
    private List<HabitDetails> habits;


    public HabitDetailsAdapter(Context context, List<HabitDetails> habits) {
        this.context = context;
        if (habits != null) {
            this.habits = habits;
        } else {
            this.habits = new ArrayList<>();
        }
    }


    @Override
    public HabitDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_habit_item, parent, false);
        return new HabitDetailsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final HabitDetailsViewHolder holder, final int position) {
        final HabitDetails curHabitDetails = habits.get(position);

//        holder.dayImage.setImageResource(R.drawable.circle_mask);
        holder.name.setText(curHabitDetails.getHabit().getName());
        holder.action.setText(curHabitDetails.getHabit().getActions()
                .get(curHabitDetails.getCurrentDay()).getAction());
        holder.checkBox.setChecked(curHabitDetails.isChecked());
        if(curHabitDetails.isChecked()) {
            holder.bottomBlock.setCardBackgroundColor(ContextCompat
                    .getColor(context, R.color.colorPrimary));

        }

        View.OnTouchListener openHabitListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                holder.topBlock.setOnTouchListener(null);

                int[] screenLocation = new int[2];
                holder.topBlock.getLocationOnScreen(screenLocation);
                int orientation = context.getResources().getConfiguration().orientation;

                Intent openHabitIntent = new Intent(context, HabitActivity.class);
                openHabitIntent
                        .putExtra(PACKAGE_NAME + ".orientation", orientation)
                        .putExtra(PACKAGE_NAME + ".left", screenLocation[0])
                        .putExtra(PACKAGE_NAME + ".top", screenLocation[1])
                        .putExtra(PACKAGE_NAME + ".width", holder.topBlock.getWidth())
                        .putExtra(PACKAGE_NAME + ".height", holder.topBlock.getHeight());

                MainActivity activity = (MainActivity) context;
//                activity.startActivityWithExplodeAnimation(position, event.getRawX(), event.getRawY(),
//                        openHabitIntent, 1000);
                activity.startActivityWithScaleAnimation(openHabitIntent, 1000);

                return true;
            }
        };

        View.OnClickListener checkedListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = !curHabitDetails.isChecked();
                curHabitDetails.setIsChecked(isChecked);
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
            }
        };

        holder.topBlock.setOnTouchListener(openHabitListener);
        holder.bottomBlock.setOnClickListener(checkedListener);
        holder.checkBox.setOnClickListener(checkedListener);
    }


    @Override
    public int getItemCount() {
        return habits.size();
    }


    public List<HabitDetails> getHabits() {
        return habits;
    }


    public void setHabits(List<HabitDetails> habits) {
        this.habits = habits;
    }


    public void removeItemsBesidesPosition(int position) {
        for (int i = habits.size()-1; i >= 0; --i) {
            if (i != position)
                habits.remove(i);
            notifyDataSetChanged();
        }
    }


    public static class HabitDetailsViewHolder extends RecyclerView.ViewHolder {
        private CardView topBlock;
        private CardView bottomBlock;
        private ImageView dayImage;
        private TextView name;
        private TextView action;
        private CheckBox checkBox;
        private TextView done;

        public HabitDetailsViewHolder(View itemView) {
            super(itemView);

            topBlock = (CardView) itemView.findViewById(R.id.my_habits_top_block);
            bottomBlock = (CardView) itemView.findViewById(R.id.my_habits_bottom_block);

            dayImage = (ImageView) itemView.findViewById(R.id.my_habits_day_image);
            name = (TextView) itemView.findViewById(R.id.my_habits_name_block);
            action = (TextView) itemView.findViewById(R.id.my_habits_action_block);

            done = (TextView) itemView.findViewById(R.id.textView_done);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }

        public CardView getTopBlock() {
            return topBlock;
        }

        public void setTopBlock(CardView topBlock) {
            this.topBlock = topBlock;
        }

        public CardView getBottomBlock() {
            return bottomBlock;
        }

        public void setBottomBlock(CardView bottomBlock) {
            this.bottomBlock = bottomBlock;
        }

        public ImageView getDayImage() {
            return dayImage;
        }

        public void setDayImage(ImageView dayImage) {
            this.dayImage = dayImage;
        }

        public TextView getName() {
            return name;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        public TextView getAction() {
            return action;
        }

        public void setAction(TextView action) {
            this.action = action;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
    }
}
