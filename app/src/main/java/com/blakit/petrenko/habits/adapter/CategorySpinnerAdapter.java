package com.blakit.petrenko.habits.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.utils.Utils;

import java.util.List;

/**
 * Created by user_And on 10.03.2016.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    private List<Category> categories;

    public CategorySpinnerAdapter(Context context, List<Category> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        this.categories = categories;
    }


    public int getCategoryPosition(String categoryNameRes) {
        for (int i = 0; i < categories.size(); ++i) {
            if (categories.get(i).getNameRes().equals(categoryNameRes)) {
                return i;
            }
        }
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) super.getView(position, convertView, parent);

        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.createActionsSmallTextColor));
        tv.setText(Utils.getStringByResName(getContext(), getItem(position).getNameRes()));

        return tv;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);

        tv.setText(Utils.getStringByResName(getContext(), getItem(position).getNameRes()));

        return tv;
    }
}
