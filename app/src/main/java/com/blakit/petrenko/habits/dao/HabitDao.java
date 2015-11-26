package com.blakit.petrenko.habits.dao;

import android.content.Context;

import com.blakit.petrenko.habits.model.Habit;

import java.util.List;

import io.realm.Realm;

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

    public void clearAll() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(Habit.class);
            }
        });
    }
}
