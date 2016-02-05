package com.blakit.petrenko.habits;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.model.Category;

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

        List<Category> categories = new HabitDao(realm)
                .getCategories();

        LinearLayout categoryList = (LinearLayout) view.findViewById(R.id.category_list);
        for (Category c: categories) {
            View itemView = inflater.inflate(R.layout.category_item, null);



            TextView tv = (TextView) itemView.findViewById(R.id.category_name);
            tv.setText(c.getNameRes());

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
