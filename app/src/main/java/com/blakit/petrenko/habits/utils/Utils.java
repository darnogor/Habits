package com.blakit.petrenko.habits.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.blakit.petrenko.habits.HabitApplication;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
