package com.blakit.petrenko.habits.model;

import org.parceler.Parcel;

import java.util.UUID;

import io.realm.ActionRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 07.08.2015.
 */
@Parcel(implementations = {ActionRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Action.class})
public class Action extends RealmObject {

    @PrimaryKey
    private String id;
    private String action;
    private int day;
    private boolean isUseDefault;
    private boolean isSkipped;

    public Action() {
        this.id = UUID.randomUUID().toString();
    }

    public Action(String action, int day) {
        this.id = UUID.randomUUID().toString();
        this.action = action;
        this.day = day;
        this.isSkipped = false;
        this.isUseDefault = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public void setSkipped(boolean isSkipped) {
        this.isSkipped = isSkipped;
    }

    public boolean isUseDefault() {
        return isUseDefault;
    }

    public void setUseDefault(boolean isUseDefault) {
        this.isUseDefault = isUseDefault;
    }
}
