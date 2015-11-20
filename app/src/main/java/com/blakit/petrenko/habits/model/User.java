package com.blakit.petrenko.habits.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by user_And on 16.07.2015.
 */
@DatabaseTable(tableName = "users")
public class User {

    @DatabaseField(dataType = DataType.STRING, id = true)
    private String name;

    @DatabaseField(dataType = DataType.STRING)
    private String displayName;

    @DatabaseField(dataType = DataType.STRING)
    private String nickName;

    @DatabaseField(dataType = DataType.STRING)
    private String imgURL;

    @DatabaseField(dataType = DataType.STRING)
    private String coverImgURL;

    @ForeignCollectionField
    private Collection<HabitDetails> myHabits;

    //TODO: Add global settings for user


    User() {}

    public User(String name) {
        this.name = name;
        myHabits = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getCoverImgURL() {
        return coverImgURL;
    }

    public void setCoverImgURL(String coverImgURL) {
        this.coverImgURL = coverImgURL;
    }

    public Collection<HabitDetails> getMyHabits() {
        return myHabits;
    }

    public void setMyHabits(List<HabitDetails> myHabits) {
        this.myHabits = myHabits;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        return name.equals(((User) o).getName());
    }
}
