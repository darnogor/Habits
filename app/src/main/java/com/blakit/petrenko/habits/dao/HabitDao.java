package com.blakit.petrenko.habits.dao;

import android.text.TextUtils;

import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.model.Habit;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by user_And on 31.07.2015.
 */
public class HabitDao {

    private Realm realm;


    public HabitDao(Realm realm) {
        this.realm = realm;
    }


    public Habit createOrUpdate(final Habit habit) {
        realm.beginTransaction();
        Habit updatedHabit = realm.copyToRealmOrUpdate(habit);
        realm.commitTransaction();

        return updatedHabit;
    }


    public List<Habit> getHabits() {
        return realm.where(Habit.class).findAll();
    }


    public RealmResults<Habit> getPopularHabits(String categoryNameRes) {
        RealmQuery query = realm.where(Habit.class).equalTo("isDeleted", false);

        if (!TextUtils.isEmpty(categoryNameRes)) {
            query = query.equalTo("category.nameRes", categoryNameRes);
        }

        return query.findAllSortedAsync("completeCount", Sort.DESCENDING,
                "addCount", Sort.DESCENDING, "creationDate", Sort.DESCENDING);
    }


    public RealmResults<Habit> getNewHabits(String categoryNameRes) {
        RealmQuery query = realm.where(Habit.class).equalTo("isDeleted", false);

        if (!TextUtils.isEmpty(categoryNameRes)) {
            query = query.equalTo("category.nameRes", categoryNameRes);
        }

        return query.findAllSortedAsync("creationDate", Sort.DESCENDING);
    }


    public RealmResults<Habit> getHabitsSortedNames(String categoryNameRes) {
        RealmQuery query = realm.where(Habit.class).equalTo("isDeleted", false);

        if (!TextUtils.isEmpty(categoryNameRes)) {
            query = query.equalTo("category.nameRes", categoryNameRes);
        }

        return query.findAllSortedAsync("name", Sort.ASCENDING);
    }


    public RealmResults<Habit> getHabitsBySearchString(String searchString) {
        return realm.where(Habit.class).equalTo("isDeleted", false)
                .contains("name", searchString).or()
                .contains("author", searchString)
                .findAllSortedAsync("completeCount", Sort.DESCENDING,
                        "addCount", Sort.DESCENDING);
    }


    public RealmResults<Habit> getCreatedHabits(String userId) {
        return realm.where(Habit.class)
                .equalTo("isDeleted", false)
                .equalTo("author", userId)
                .findAllSortedAsync("creationDate", Sort.DESCENDING);
    }


    public void setDeleteHabit(final String habitId) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Habit habit = realm.where(Habit.class).equalTo("id", habitId).findFirst();
                if (habit != null) {
                    habit.setDeleted(true);
                }
            }
        });
    }


    public List<Category> getCategories() {
        return realm.where(Category.class).findAll();
    }


    public boolean isEmptyCategory() {
        return realm.where(Category.class).count() == 0;
    }


    public void clearAll() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(Habit.class);
            }
        });
    }
}
