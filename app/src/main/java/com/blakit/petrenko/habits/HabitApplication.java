package com.blakit.petrenko.habits;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.blakit.petrenko.habits.dao.HabitsDBOpenHelper;
import com.blakit.petrenko.habits.model.User;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by user_And on 27.08.2015.
 */
public class HabitApplication extends Application {

    private static HabitApplication singleton;

    private User user;
    private SharedPreferences appPreferences;
    private HabitsDBOpenHelper dbOpenHelper;

    public static HabitApplication getInstance() {
        return singleton;
    }

    public User getUser() {
        return user;
    }

    public void updateUser(User user) throws SQLException{
        getDbOpenHelper().getUserDao().createOrUpdate(user);
        this.user = user;
    }

    public void updateUser(String username) throws SQLException {
        User newUser = new User(username);
        updateUser(newUser);
    }

    public void setCurrentUser(User user) throws SQLException {
        this.user = getDbOpenHelper().getUserDao().getUserByName(user.getName());
        if(this.user.getDisplayName() != null)
            Log.d("SNHJKJF:",this.user.getDisplayName());
        if (this.user == null) {
            getDbOpenHelper().getUserDao().createIfNotExist(user);
            this.user = getDbOpenHelper().getUserDao().getUserByName(user.getName());
        }
    }

    public void setCurrentUser(String username) throws SQLException {
        User user = new User(username);
        setCurrentUser(user);
    }

    public HabitsDBOpenHelper getDbOpenHelper() {
        if (dbOpenHelper == null) {
            dbOpenHelper = OpenHelperManager.getHelper(this, HabitsDBOpenHelper.class);
        }
        return dbOpenHelper;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

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

//        appPreferences = getSharedPreferences("HabitAppPreferences", MODE_PRIVATE);
//        String username = appPreferences.getString("default_account", "");
//        if (!username.equals(""))
//            try {
//                setUser(username);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (dbOpenHelper != null) {
            OpenHelperManager.releaseHelper();
        }
    }
}
