package com.blakit.petrenko.habits.model;

import android.support.annotation.NonNull;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 07.08.2015.
 */
public class HabitDetails extends RealmObject {

    @PrimaryKey
    private String id;

    private String userId;
    private String habitId;

    private User user;
    private Habit habit;

    private int currentDay;
    private boolean isChecked;
    private String reason;

    //TODO: Add settings of the habit


    public HabitDetails() {}

    public HabitDetails(@NonNull User user, @NonNull Habit habit) {
        this.userId = user.getName();
        this.habitId = habit.getId();
        this.id = userId + "_" + habitId;
        this.user = user;
        this.habit = habit;

        this.currentDay = 1;
        this.isChecked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
