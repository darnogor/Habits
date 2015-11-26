package com.blakit.petrenko.habits.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 16.07.2015.
 */
public class User extends RealmObject{

    @PrimaryKey
    private String name;
    private String displayName;
    private String nickName;
    private String imgURL;
    private String coverImgURL;
    private RealmList<HabitDetails> myHabits;

    //TODO: Add global settings for user


    public User() {}

    public User(String name) {
        this.name = name;
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

    public RealmList<HabitDetails> getMyHabits() {
        return myHabits;
    }

    public void setMyHabits(RealmList<HabitDetails> myHabits) {
        this.myHabits = myHabits;
    }

}
