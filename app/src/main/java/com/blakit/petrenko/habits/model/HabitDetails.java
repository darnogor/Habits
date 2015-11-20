package com.blakit.petrenko.habits.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

/**
 * Created by user_And on 07.08.2015.
 */
@DatabaseTable(tableName = "habit_details")
public class HabitDetails {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "user_id")
    private User user;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "habit_id")
    private Habit habit;

    @DatabaseField(dataType = DataType.INTEGER, canBeNull = false, columnName = "current_day")
    private int currentDay;

    @DatabaseField(dataType = DataType.BOOLEAN, canBeNull = false, columnName = "checked")
    private boolean isChecked;

    @DatabaseField(dataType = DataType.STRING)
    private String reason;

//    @ForeignCollectionField(eager = true)
//    private ForeignCollection<VideoItem> myRelatedVideoItems;
//
//    @ForeignCollectionField(eager = true)
//    private ForeignCollection<Article> myRelatedArticles;

    //TODO: Add settings of the habit


    HabitDetails() {}

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

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getTodayAction() {
        Action todayAction = habit.getAction(currentDay);
        return todayAction.getAction();
    }
}
