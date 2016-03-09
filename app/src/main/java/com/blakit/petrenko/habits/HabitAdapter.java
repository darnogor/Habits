package com.blakit.petrenko.habits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.view.FontTextView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.ResourcesCompat;
import com.wnafee.vector.compat.VectorDrawable;

import org.parceler.Parcels;

import io.realm.RealmResults;

/**
 * Created by user_And on 04.12.2015.
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private Context context;
    private RealmResults<Habit> habits;


    public HabitAdapter(Context context, RealmResults<Habit> habits) {
        this.context = context;
        this.habits = habits;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.habit_item, parent, false);
        return new HabitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HabitViewHolder holder, int position) {
        final Habit habit = habits.get(position);

//        try {
//            R.string.class.getField("category_habit_first").getInt(null);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }

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
        holder.symbolBack.setForegroundTintList(ColorStateList.valueOf(ColorGenerator.MATERIAL.getRandomColor()));
        holder.number.setText(position+1+".");
        holder.title.setText(habit.getName());
        holder.author.setText(habit.getAuthor());
        holder.addCount.setText(habit.getAddCount()+"");
        holder.completeCount.setText(habit.getCompleteCount() + "");

        final PopupMenu menu = new PopupMenu(context, holder.menu);
        menu.inflate(R.menu.menu_create_habit);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_create_habit_copy_link:
                        return true;
                    case R.id.menu_create_habit_share:
                        return true;
                    case R.id.menu_create_habit_delete:
                        return true;
                }
                return false;
            }
        });

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
        Log.d("!!!!Message count:", habits.size()+"");
        return habits.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {

        private View itemView;

        private MorphButton symbolBack;
        private TextView symbol;
        private FontTextView number;
        private FontTextView title;
        private IconicsImageView menu;
        private FontTextView author;
        private FontTextView addCount;
        private FontTextView completeCount;

        public HabitViewHolder(View view) {
            super(view);

            itemView = view;
            itemView.setClickable(true);

            symbolBack = (MorphButton) itemView.findViewById(R.id.habit_item_symbol_back);
            symbol = (TextView) itemView.findViewById(R.id.habit_item_symbol_text);
            number = (FontTextView) itemView.findViewById(R.id.habit_item_number);
            title = (FontTextView) itemView.findViewById(R.id.habit_item_title);
            menu = (IconicsImageView) itemView.findViewById(R.id.habit_item_menu);
            author = (FontTextView) itemView.findViewById(R.id.habit_item_author);
            addCount = (FontTextView) itemView.findViewById(R.id.habit_item_add_count);
            completeCount = (FontTextView) itemView.findViewById(R.id.habit_item_complete_count);

            symbolBack.setClickable(false);
        }
    }
}
