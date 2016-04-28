package com.blakit.petrenko.habits.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.reciever.AlarmReceiver;
import com.blakit.petrenko.habits.reciever.AlarmSetReceiver;

import io.realm.Realm;


public class AlarmService extends IntentService {

    public AlarmService() {
        super("com.blakit.petrenko.habits.service.AlarmService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Realm realm = Realm.getDefaultInstance();
            final String action = intent.getAction();

            switch (action) {
                case AlarmReceiver.ACTION_UPDATE_DAY:
                    new UserDao(realm).updateDaysHabitDetailses();
                    AlarmSetReceiver.setNextUpdateDayAlarm(this);
                    break;
            }
            realm.close();
        }
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
