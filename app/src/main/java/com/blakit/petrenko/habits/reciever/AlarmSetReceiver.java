package com.blakit.petrenko.habits.reciever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class AlarmSetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        setUpdateDayAlarm(context);

        Log.d("!!!!AlarmSetReceiver", "onReceive()");
    }


    private static void setEveryDayAlarm(Context context, PendingIntent pendingIntent, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        long beginNextDayMillis = calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY;

        if (Build.VERSION.SDK_INT < 19) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    beginNextDayMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    beginNextDayMillis,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    beginNextDayMillis,
                    pendingIntent);
        }
    }


    public static void setUpdateDayAlarm(Context context) {
        Intent updateDayIntent = new Intent(context, AlarmReceiver.class);
        updateDayIntent.setAction(AlarmReceiver.ACTION_UPDATE_DAY);

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(context, 0, updateDayIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        setEveryDayAlarm(context, pendingIntent, calendar);
    }


    public static void setNextUpdateDayAlarm(Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            setUpdateDayAlarm(context);
        }
        Log.d("!!!!AlarmSetReciever", "setNextExactAlarm()");
    }


    public static void setHabitNotifyAlarm(Context context, String userId) {

    }
}
