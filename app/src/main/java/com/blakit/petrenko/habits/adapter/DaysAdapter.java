package com.blakit.petrenko.habits.adapter;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;

import io.realm.RealmList;

/**
 * Created by user_And on 09.02.2016.
 */
public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DaysViewHolder> {

    private Context context;
    private RealmList<Action> actions;

    private TextView dayView;
    private TextView actionView;

    private int lastClickPosition = 0;
    private int colorClicked;
    private int colorNormal;

    private float sp;

    public DaysAdapter(Context context, TextView dayView, TextView actionView, RealmList<Action> actions) {
        this.context = context;
        this.dayView = dayView;
        this.actionView = actionView;
        this.actions = actions;

        colorClicked = ContextCompat.getColor(context, R.color.md_white_1000);
        colorNormal = ContextCompat.getColor(context, R.color.md_grey_700);

        sp = context.getResources().getDisplayMetrics().scaledDensity;
    }


    @Override
    public DaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_day_fab, parent, false);
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
        holder.number.setText(action.isSkipped() ? "X" : text);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickPosition != position && !action.isSkipped()) {
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


    public void setLastClickPosition(int lastClickPosition) {
        this.lastClickPosition = lastClickPosition;
    }


    void startFabAnimation(View fab) {
        ScaleAnimation animation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);

        fab.startAnimation(animation);
    }


    public class DaysViewHolder extends RecyclerView.ViewHolder {
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
