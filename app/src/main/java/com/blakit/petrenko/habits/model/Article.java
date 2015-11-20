package com.blakit.petrenko.habits.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by user_And on 07.08.2015.
 */
@DatabaseTable(tableName = "articles")
public class Article implements Serializable{

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING)
    private String uri;

    @DatabaseField(dataType = DataType.STRING)
    private String title;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "habit_id")
    private Habit habit;

    Article() {}

    public Article(String uri) {
        this.uri = uri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }
}
