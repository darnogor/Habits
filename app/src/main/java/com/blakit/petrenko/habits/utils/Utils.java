package com.blakit.petrenko.habits.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.blakit.petrenko.habits.HabitApplication;
import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.model.Habit;
import com.blakit.petrenko.habits.model.HabitDetails;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @ColorInt
    private static final int[] PROGRESS_COLORS = {
            0xffef5350,
            0xfff06292,
            0xffba68c8,
            0xff9575cd,
            0xff42a5f5,
            0xff1de9b6,
            0xff00e676,
            0xff4caf50
    };

    /**
     * Generate a value suitable for use in {View.setId(int)}.
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


    public static int getColorByProgress(int daysCount, int currentDay, boolean isChecked) {
        int percent = (int) (getPercent(daysCount, currentDay, isChecked) * 100);

        int red   = (percent < 50) ? 255 : (int) Math.round(256 - (percent-50)*5.12);
        int green = (percent > 50) ? 255 : (int) Math.round((percent)*5.12);

        return Color.argb(255, red, green, 0);
    }


    public static int getColorByProgress(HabitDetails habitDetails) {
        return getColorByProgress(habitDetails.getHabit().getActions().size(),
                habitDetails.getCurrentDay(),
                habitDetails.isChecked());
    }

    public static int getColorMaterialByProgress(HabitDetails habitDetails) {
        float percent = getPercent(habitDetails);

        return PROGRESS_COLORS[((int) (7 * percent))];
    }


    public static int getColorByProgressWithRange(int daysCount, int currentDay, boolean isChecked,
                                                  int startColor, int endColor) {
        float percent = getPercent(daysCount, currentDay, isChecked);

        return Color.argb((int) (percent * Color.alpha(endColor) + (1 - percent) * Color.alpha(startColor)),
                (int) (percent * Color.red(endColor) + (1 - percent) * Color.red(startColor)),
                (int) (percent * Color.green(endColor) + (1 - percent) * Color.green(startColor)),
                (int) (percent * Color.blue(endColor) + (1 - percent) * Color.blue(startColor)));
    }


    public static int getColorByProgressWithRange(HabitDetails habitDetails,
                                                  int startColor, int endColor) {
        return getColorByProgressWithRange(habitDetails.getHabit().getActions().size(),
                habitDetails.getCurrentDay(),
                habitDetails.isChecked(),
                startColor, endColor);
    }


    public static float getPercent(int daysCount, int currentDay, boolean isChecked) {
        return (float) (currentDay - ((isChecked) ? 0 : 1)) / daysCount;
    }


    public static float getPercent(HabitDetails habitDetails) {
        return getPercent(habitDetails.getHabit().getActions().size(),
                habitDetails.getCurrentDay(),
                habitDetails.isChecked());
    }


    public static int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }


    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


    public static void expand(final View v, long duration) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(duration);
        v.startAnimation(a);
    }


    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


    public static void collapse(final View v, long duration) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }


    public static void setMaxLinesByMaxHeight(final TextView textView, final int maxHeightPx) {
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxLines = maxHeightPx / textView.getLineHeight();

                textView.setMaxLines(maxLines);
                textView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }


    public static void parseTitleByURL(String urlStr, final Callable<String, Void> callable, final View progressView) {
        if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
            urlStr = "http://" + urlStr;
        }

        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                progressView.setVisibility(View.VISIBLE);
            }


            @Override
            protected String doInBackground(String... params) {
                Document document = null;
                try {
                    document = Jsoup.connect(params[0]).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (document != null) {
                    return document.title();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                progressView.setVisibility(View.GONE);
                callable.call(s);
            }

        }.execute(urlStr);
    }


    public static void parseTitleByURLRegEx(String urlStr, final Callable<String, Void> callable) {
        final Pattern TITLE_TAG =
                Pattern.compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    URLConnection urlConnection = url.openConnection();
                    DataInputStream dis = new DataInputStream(urlConnection.getInputStream());

                    String html = "", tmp;
                    while ((tmp = dis.readUTF()) != null) {
                        html += " " + tmp;
                    }
                    dis.close();

                    html = html.replaceAll("\\s+", " ");
                    Matcher m = TITLE_TAG.matcher(html);

                    if (m.find() == true) {
                        return m.group(1);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                callable.call(s);
            }
        }.execute(urlStr);
    }


    public static Date parseDate(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        try {
            if (TextUtils.isEmpty(dateStr)) {
                date = format.parse("00:00");
            }
            else {
                date = format.parse(dateStr);
            }
        } catch (ParseException e) {
            try {
                date = format.parse("00:00");
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        return date;
    }


    public static String getProfileStatusName(Context context, int level) {
        if (level <= 0) {
            level = 1;
        }
        if (level >= 22) {
            level = 21;
        }

        @StringRes
        int[] statusNamesRes = {
            R.string.profile_status_beginner,
            R.string.profile_status_apprentice,
            R.string.profile_status_best_apprentice,
            R.string.profile_status_journeyman,
            R.string.profile_status_master,
            R.string.profile_status_great_master,
            R.string.profile_status_guru
        };

        return context.getString(statusNamesRes[(level - 1) / 3]);
    }
}
