package com.blakit.petrenko.habits.model;

import org.parceler.Parcel;

import java.util.UUID;

import io.realm.CategoryRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 02.12.2015.
 */
@Parcel(implementations = { CategoryRealmProxy.class },
        value = Parcel.Serialization.BEAN,
        analyze = { Category.class })
public class Category extends RealmObject {

    @PrimaryKey
    private String id;

    private String nameRes;
    private String color;

    public Category() {
        this.id = UUID.randomUUID().toString();
    }

    public Category(String nameRes) {
        this.id = UUID.randomUUID().toString();
        this.nameRes = nameRes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameRes() {
        return nameRes;
    }

    public void setNameRes(String nameRes) {
        this.nameRes = nameRes;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
