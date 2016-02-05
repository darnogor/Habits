package com.blakit.petrenko.habits;

import android.app.Application;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by user_And on 27.08.2015.
 */
public class HabitApplication extends Application {

    private static HabitApplication singleton;
    private User user;

    public static HabitApplication getInstance() {
        return singleton;
    }

    public User getUser() {
        return user;
    }

    public void updateCurrentUserSearchHistory(UserDao userDao, String searchStr) {
        this.user = userDao.updateUserSearchHistory(user, searchStr);
    }

    public void updateCurrentUserPresentSearchHistory(UserDao userDao, String searchStr) {
        this.user = userDao.updatePresentUserSearchHistory(user, searchStr);
    }

    public void updateUser(UserDao userDao, User user) {
        userDao.createOrUpdate(user);
        this.user = userDao.getUserByName(user.getName());
    }

    public void updateUser(UserDao userDao, String username) {
        User newUser = new User(username);
        updateUser(userDao, newUser);
    }

    public void setCurrentUser(UserDao userDao, User user) {
        this.user = userDao.getUserByName(user.getName());
        if (this.user == null) {
            updateUser(userDao, user);
        }
    }

    public void setCurrentUser(UserDao userDao, String username) {
        User user = new User(username);
        setCurrentUser(userDao, user);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
//        try {
//            realm = Realm.getInstance(this);
//        } catch (RealmMigrationNeededException e) {
//            new File(realm.getPath()).delete();
//            realm = Realm.getInstance(this);
//        }

        RealmConfiguration myConfig = new RealmConfiguration.Builder(this)
                .name("habitsrealm.realm")
                .schemaVersion(1)
                .build();

        Realm.setDefaultConfiguration(myConfig);

        File cacheDir = StorageUtils.getCacheDirectory(this, false);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(this)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .defaultDisplayImageOptions(options)
                .build());

    }


}
