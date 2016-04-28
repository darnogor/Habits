package com.blakit.petrenko.habits.dao;

import android.content.Context;
import android.util.Log;

import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.SearchHistory;
import com.blakit.petrenko.habits.model.User;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by user_And on 31.07.2015.
 */
public class UserDao {

    private Realm realm;


    public UserDao(Realm realm) {
        this.realm = realm;
    }


    public User createOrUpdate(User user) {
        realm.beginTransaction();
        User newUser = realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();

        return newUser;
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


    public RealmResults<HabitDetails> findAllAvailableHabitHetails(String userId) {
        return realm.where(HabitDetails.class)
                .equalTo("userId", userId)
                .equalTo("isDeleted", false)
                .findAllSortedAsync("currentDay", Sort.DESCENDING);
    }


    public RealmResults<HabitDetails> findAvailableCompleteHabitDetails(String userId) {
        return realm.where(HabitDetails.class)
                .equalTo("userId", userId)
                .equalTo("isDeleted", false)
                .equalTo("isComplete", true)
                .findAllAsync();
    }


    public HabitDetails getHabitDetailsById(String id) {
        return realm.where(HabitDetails.class).equalTo("id", id).findFirst();
    }


    public void createOrUpdateHabitDetails(final HabitDetails habitDetails) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = habitDetails.getUser();
                HabitDetails presentHD = null;
                for (HabitDetails hd: user.getMyHabits()) {
                    Log.d("!!!!HD ID: ",hd.getId());
                    if (hd.getHabitId().equals(habitDetails.getHabitId())) {
                        presentHD = hd;
                        break;
                    }
                }
                if (presentHD == null || presentHD.isDeleted()) {
                    if (presentHD == null) {
                        user.getMyHabits().add(habitDetails);
                    }
                    habitDetails.getHabit().setAddCount(habitDetails.getHabit().getAddCount() + 1);
                }

                realm.copyToRealmOrUpdate(habitDetails);
            }
        });
    }


    public void checkInHabitDetails(String hdId) {
        HabitDetails habitDetails = realm.where(HabitDetails.class).equalTo("id", hdId).findFirst();
        if (habitDetails != null) {
            realm.beginTransaction();
            if (habitDetails.isChecked()) {
                int lastDay = 1;
                int currentDay = habitDetails.getCurrentDay();
                for (Action a: habitDetails.getHabit().getActions()) {
                    if (a.getDay() > lastDay) {
                        lastDay = a.getDay();
                    }
                }
                ++currentDay;
                if (currentDay > lastDay) {
                    currentDay = 1;
                }
                habitDetails.setCurrentDay(currentDay);
                habitDetails.setChecked(false);
            } else {
                habitDetails.setChecked(true);
            }
            realm.commitTransaction();
        }
    }


    public void updateDaysHabitDetailses() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<HabitDetails> habitDetailses = realm.where(HabitDetails.class).findAll();

                for (int i = 0; i < habitDetailses.size(); ++i) {
                    HabitDetails hd = habitDetailses.get(i);

                    if (!hd.isComplete()) {
                        int currentDay = hd.getCurrentDay();
                        int lastDay = 1;

                        for (Action action : hd.getHabit().getActions()) {
                            if (action.getDay() > lastDay) {
                                lastDay = action.getDay();
                            }
                        }

                        if (currentDay == lastDay && hd.isChecked()) {
                            hd.setComplete(true);
                        } else {
                            if (hd.isChecked()) {
                                hd.setChecked(false);
                                hd.setCurrentDay(currentDay + 1);
                            } else {
                                hd.setCurrentDay(1);
                            }
                        }
                    }
                }
            }
        });
    }


    public Realm getRealm() {
        return realm;
    }


    public void deleteMarkHabitDetails(String id) {
        realm.beginTransaction();
        HabitDetails habitDetails = realm.where(HabitDetails.class)
                .equalTo("id", id)
                .findFirst();

        int addCount = habitDetails.getHabit().getAddCount() - 1;
        if (addCount < 0) {
            addCount = 0;
        }

        habitDetails.setDeleted(true);
        habitDetails.getHabit().setAddCount(addCount);
        realm.commitTransaction();
    }
}
