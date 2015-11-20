package com.blakit.petrenko.habits;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

/**
 * Created by user_And on 19.09.2015.
 */
public class CustomSelectedDrawerItem extends PrimaryDrawerItem {
    @Override
    public void bindView(RecyclerView.ViewHolder holder) {
        if (isSelected()) {
            withTypeface(Typeface.DEFAULT_BOLD);
        } else {
            withTypeface(Typeface.DEFAULT);
        }
        super.bindView(holder);
    }
}
