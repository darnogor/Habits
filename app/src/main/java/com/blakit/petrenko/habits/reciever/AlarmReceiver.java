package com.blakit.petrenko.habits.reciever;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.blakit.petrenko.habits.service.AlarmService;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION_UPDATE_DAY = "com.blakit.petrenko.habits.UPDATE_DAY";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, AlarmService.class);
        intentService.setAction(intent.getAction());

        startWakefulService(context, intentService);

        Log.d("!!!!Alarm Receiver", "onReceive()");
    }
}
