package com.blakit.petrenko.habits.dao;

import android.content.Context;

import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

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

    public User updateUserSearchHistory(final User user, final String searchStr) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.getSearchHistories().add(new SearchHistory(searchStr));
                realm.copyToRealmOrUpdate(user);
            }
        });
        return getUserByName(user.getName());
    }

    public User updatePresentUserSearchHistory(final User user, final String searchStr) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(SearchHistory h: user.getSearchHistories()) {
                    if (h.getWord().equals(searchStr)) {
                        h.setDate(new Date());
                        break;
                    }
                }
                realm.copyToRealmOrUpdate(user);
            }
        });
        return getUserByName(user.getName());
    }

    public Realm getRealm() {
        return realm;
    }


}
