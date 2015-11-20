package com.blakit.petrenko.habits.dao;

import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.User;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * Created by user_And on 31.07.2015.
 */
public class UserDao {
    private Dao<User, String> userDao;
    private Dao<HabitDetails, Integer> habitDetailsDao;

    public UserDao(HabitsDBOpenHelper habitsDBOpenHelper) throws SQLException {
        userDao = habitsDBOpenHelper.getDao(User.class);
        habitDetailsDao = habitsDBOpenHelper.getDao(HabitDetails.class);
    }

    public void createOrUpdate(User user) throws SQLException {
        userDao.createOrUpdate(user);
    }

    public void createIfNotExist(User user) throws SQLException {
        userDao.createIfNotExists(user);
    }

    public User getUserByName(String username) throws SQLException {
        return userDao.queryForId(username);
    }

}
