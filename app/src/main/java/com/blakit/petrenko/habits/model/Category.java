package com.blakit.petrenko.habits.model;

import org.parceler.Parcel;

import java.util.UUID;

import io.realm.CategoryRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by user_And on 02.12.2015.
 */
@Parcel(implementations = { CategoryRealmProxy.class },
        value = Parcel.Serialization.BEAN,
        analyze = { Category.class })
public class Category extends RealmObject {

    @PrimaryKey
    private String nameRes;
    private String color;


    public Category() {
    }


    public Category(String nameRes) {
        this.nameRes = nameRes;
    }


    public Category(String nameRes, String color) {
        this.nameRes = nameRes;
        this.color = color;
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
