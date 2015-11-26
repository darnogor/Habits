package com.blakit.petrenko.habits;

import android.app.Application;

import com.blakit.petrenko.habits.dao.HabitDao;
import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.User;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by user_And on 27.08.2015.
 */
public class HabitApplication extends Application {

    private static HabitApplication singleton;
    private User user;
    private Realm realm;
    private UserDao userDao;
    private HabitDao habitDao;

    public static HabitApplication getInstance() {
        return singleton;
    }

    public User getUser() {
        return user;
    }

    public void updateUser(User user) {
        userDao.createOrUpdate(user);
        this.user = userDao.getUserByName(user.getName());
    }

    public void updateUser(String username) {
        User newUser = new User(username);
        updateUser(newUser);
    }

    public void setCurrentUser(User user) {
        this.user = userDao.getUserByName(user.getName());
        if (this.user == null) {
            updateUser(user);
        }
    }

    public void setCurrentUser(String username) {
        User user = new User(username);
        setCurrentUser(user);
    }

    public UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDao(realm);
        }
        return userDao;
    }

    public HabitDao getHabitDao() {
        if (habitDao == null) {
            habitDao = new HabitDao(realm);
        }
        return habitDao;
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
        realm = Realm.getInstance(this);

        userDao = new UserDao(realm);
        habitDao = new HabitDao(realm);

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

    @Override
    public void onTerminate() {
        super.onTerminate();
        realm.close();
    }
}
