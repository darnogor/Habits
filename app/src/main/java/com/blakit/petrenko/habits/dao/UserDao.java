package com.blakit.petrenko.habits.dao;

import android.content.Context;

import com.blakit.petrenko.habits.model.User;

import io.realm.Realm;

/**
 * Created by user_And on 31.07.2015.
 */
public class UserDao {

    private Realm realm;

    public UserDao(Realm realm) {
        this.realm = realm;
    }

    public void createOrUpdate(User user) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }


    public User getUserByName(String username) {
        return realm.where(User.class).equalTo("name", username).findFirst();
    }

}
