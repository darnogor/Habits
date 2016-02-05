package com.blakit.petrenko.habits.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.blakit.petrenko.habits.HabitApplication;
import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmList;

/**
 * Created by user_And on 26.09.2015.
 */
public class Utils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link View.setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            while (true) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /**
     * Check state of the Internet Network
     * @return <b>true</b>, if network is available; <b>false</b> otherwise
     */
    public static boolean isNetworkAvalible() {
        ConnectivityManager cm = (ConnectivityManager)
                HabitApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null &&
                cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnected();
    }


    public static boolean isUri(String uriString) {
        if (Patterns.WEB_URL.matcher(uriString).matches()) {
            return true;
        }
        return false;
    }


    public static String parseYoutubeVideoId(String input) {
        String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";
//        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
//        String pattern = "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$";

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(input);
        if(matcher.matches()) {
            String str = matcher.group(1);
            Log.d("PARSING OF YOUTUBE URL", str);
            return str;
        }
        matcher = Pattern.compile("^[a-zA-Z0-9_-]{11}$").matcher(input);
        if(matcher.find()) {
            String str = matcher.group();
            Log.d("PARSING OF YOUTUBE URL", str);
            return str;
        }
        return null;
    }


    public static Action getAction(@NonNull Habit habit, int day) {
        for (Action a: habit.getActions()) {
            if (a.getDay() == day) {
                return a;
            }
        }
        return null;
    }


    public static Action getAction(@NonNull RealmList<Action> actions, int day) {
        for (Action a: actions) {
            if (a.getDay() == day) {
                return a;
            }
        }
        return null;
    }


    public static String videoDurationByFormattedString(String formattedDuration) {
        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = formatter.parsePeriod(formattedDuration);

        StringBuilder builder = new StringBuilder();

        if (p.toStandardHours().getHours() > 0) {
            builder.append(p.toStandardHours().getHours());
            builder.append(":");
        }
        if (p.getMinutes() < 10) {
            builder.append("0");
        }
        builder.append(p.getMinutes());
        builder.append(":");
        if (p.getSeconds() < 10) {
            builder.append("0");
        }
        builder.append(p.getSeconds());

        return builder.toString();
    }


    public static String viewsCountByNumberString(Context context, String num) {
        StringBuilder builder = new StringBuilder();
        builder.append(num);
        for (int i = num.length()-3; i > 0; i -= 3) {
            builder.insert(i, ',');
        }
        builder.insert(0, context.getString(R.string.create_habit_youtube_views) + " ");

        return builder.toString();
    }


    public static String randomString(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz   ".toCharArray();
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        String first = String.valueOf(chars[random.nextInt(chars.length-3)]).toUpperCase();
        builder.append(first);
        for (int i = 1; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            builder.append(c);
        }
        return builder.toString();
    }


    public static boolean isAccountManagerHasAccount(Context context, String accountName) {
        for (Account account: AccountManager.get(context).getAccounts()) {
            if (account.name.equals(accountName)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isActivityLand(Activity activity) {
        return activity.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }


    public static String getStringByResName(Context context, String resName) {
        try {
            Field field = R.string.class.getField(resName);
            if (field != null) {
                int resId = field.getInt(null);
                return context.getString(resId);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
}
