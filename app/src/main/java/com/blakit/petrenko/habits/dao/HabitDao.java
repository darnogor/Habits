package com.blakit.petrenko.habits.dao;

import android.content.Context;

import com.blakit.petrenko.habits.model.Category;
import com.blakit.petrenko.habits.model.Habit;

import java.util.List;

import io.realm.Realm;
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

    public void createOrUpdate(final Habit habit) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(habit);
            }
        });
    }

    public List<Habit> getHabits() {
        return realm.where(Habit.class).findAll();
    }

    public RealmResults<Habit> getPopularHabits() {
        return realm.where(Habit.class).findAllSortedAsync("completeCount", Sort.DESCENDING,
                "addCount", Sort.DESCENDING);
    }

    public RealmResults<Habit> getNewHabits() {
        return realm.where(Habit.class).findAllSortedAsync("creationDate", Sort.DESCENDING);
    }

    public RealmResults<Habit> getHabitsSortedNames() {
        return realm.where(Habit.class).findAllSortedAsync("name", Sort.ASCENDING);
    }

    public RealmResults<Habit> getHabitsBySearchString(String searchString) {
        return realm.where(Habit.class)
                .contains("name", searchString).or()
                .contains("author", searchString)
                .findAllSortedAsync("completeCount", Sort.DESCENDING,
                        "addCount", Sort.DESCENDING);
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
