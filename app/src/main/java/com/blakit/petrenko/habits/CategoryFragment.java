package com.blakit.petrenko.habits;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.utils.Utils;
import com.wnafee.vector.MorphButton;

import java.util.List;

import io.realm.Realm;


public class CategoryFragment extends Fragment {

    private Realm realm;

    public static CategoryFragment newInstance(Realm realm) {
        CategoryFragment fragment = new CategoryFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        realm = Realm.getDefaultInstance();

        List<Category> categories = new HabitDao(realm).getCategories();
        LinearLayout categoryList = (LinearLayout) view.findViewById(R.id.category_list);

        for (final Category c: categories) {
            View itemView = inflater.inflate(R.layout.item_category, null);

            MorphButton icon = (MorphButton) itemView.findViewById(R.id.category_icon);
            TextView tv      = (TextView) itemView.findViewById(R.id.category_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddHabitActivity.class);
                    intent.putExtra("category", c.getNameRes());

                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left_half);
                }
            });

            icon.setForegroundTintList(ColorStateList.valueOf(Color.parseColor(c.getColor())));
            tv.setText(Utils.getStringByResName(getActivity(), c.getNameRes()));

            categoryList.addView(itemView);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }
}
