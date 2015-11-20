package com.blakit.petrenko.habits.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Article;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.model.VideoItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by user_And on 31.07.2015.
 */
public class HabitsDBOpenHelper extends OrmLiteSqliteOpenHelper {
    public static final String DB_NAME = "com_blakitteam_habits.db";
    public static final int DB_VERSION = 1;

    private UserDao userDao;
    private HabitDao habitDao;

    public HabitsDBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public HabitsDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Habit.class);
            TableUtils.createTableIfNotExists(connectionSource, User.class);
            TableUtils.createTableIfNotExists(connectionSource, HabitDetails.class);
            TableUtils.createTableIfNotExists(connectionSource, Action.class);
            TableUtils.createTableIfNotExists(connectionSource, Article.class);
            TableUtils.createTableIfNotExists(connectionSource, VideoItem.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {

    }


    public UserDao getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = new UserDao(this);
        }
        return userDao;
    }

    public HabitDao getHabitDao() throws SQLException {
        if (habitDao == null) {
            habitDao = new HabitDao(this);
        }
        return habitDao;
    }
}
