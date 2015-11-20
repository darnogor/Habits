package com.blakit.petrenko.habits.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by user_And on 07.08.2015.
 */
@DatabaseTable(tableName = "actions")
public class Action implements Serializable {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING, canBeNull = false)
    private String action;

    @DatabaseField(dataType = DataType.INTEGER)
    private int day;

    @DatabaseField(dataType = DataType.BOOLEAN)
    private boolean isSkipped;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "habit_id")
    private transient Habit habit;

    Action() {}

    public Action(String action, int day) {
        this.action = action;
        this.day = day;
        this.isSkipped = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public void setIsSkipped(boolean isSkipped) {
        this.isSkipped = isSkipped;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }
}
