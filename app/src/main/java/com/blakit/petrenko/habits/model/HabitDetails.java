package com.blakit.petrenko.habits.model;

import android.support.annotation.NonNull;

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
    private boolean isComplete;

    private String reason;

    //TODO: Add settings of the habit
    private boolean isNotificationActivated;
    private String notificationTime;

    private boolean isDeleted;
    private boolean isSyncronized;


    public HabitDetails() {
    }


    public HabitDetails(@NonNull User user, @NonNull Habit habit) {
        this.userId = user.getName();
        this.habitId = habit.getId();
        this.id = userId + "_" + habitId;
        this.user = user;
        this.habit = habit;

        this.currentDay = 1;
        this.isChecked = false;
        this.isComplete = false;

        this.isNotificationActivated = true;
        this.notificationTime = "12:00";

        this.isDeleted = false;
        this.isSyncronized = false;
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


    public boolean isComplete() {
        return isComplete;
    }


    public void setComplete(boolean complete) {
        isComplete = complete;
    }


    public String getReason() {
        return reason;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }


    public boolean isNotificationActivated() {
        return isNotificationActivated;
    }


    public void setNotificationActivated(boolean isNotificationActivated) {
        this.isNotificationActivated = isNotificationActivated;
    }


    public String getNotificationTime() {
        return notificationTime;
    }


    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }


    public boolean isDeleted() {
        return isDeleted;
    }


    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }


    public boolean isSyncronized() {
        return isSyncronized;
    }


    public void setSyncronized(boolean isSyncronized) {
        this.isSyncronized = isSyncronized;
    }
}
