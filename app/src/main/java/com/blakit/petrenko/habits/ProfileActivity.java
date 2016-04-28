package com.blakit.petrenko.habits;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.model.User;
import com.blakit.petrenko.habits.utils.Resources;
import com.blakit.petrenko.habits.utils.Utils;
import com.blakit.petrenko.habits.view.FontTextView;
import com.blakit.petrenko.habits.view.NotZeroPercentFormatter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.util.Charsets;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.common.io.CharStreams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQ_SIGN_IN_REQUIRED = 42;

    private Realm realm;
    private User user;
    private RealmResults<HabitDetails> habitsDetails;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressBar levelProgressBar;
    private ImageView profileImageView;
    private FontTextView nameTextView;
    private FontTextView levelTextView;
    private FontTextView statusTextView;
    private FontTextView googleTextView;

    private FontTextView completeHabitsTextView;
    private FontTextView performedHabitsTextView;
    private FontTextView zeroProgressHabitsTextView;

    private PieChart pieChart;
    private List<String> xVals;
    private List<Entry> yVals;
    private int[] pieColors;

    private FontTextView nickNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        realm = Realm.getDefaultInstance();

        initToolbar();
        initProfileData();
    }



    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nav_menu_item_profile);
        }
    }


    private void initUser() {
        String username = HabitApplication.getInstance().getUsername();
        UserDao userDao = new UserDao(realm);

        user = userDao.getUserByName(username);

        user.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                updateProfileData();
            }
        });
    }


    private void initHabitDetails() {
        habitsDetails = new UserDao(realm).findAllAvailableHabitHetails(user.getName());

        habitsDetails.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                updateProfileData();
            }
        });
    }


    private void initProfileData() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.profile_swipe_refresh);

        levelProgressBar = (ProgressBar) findViewById(R.id.profile_level_progress);
        profileImageView = (ImageView) findViewById(R.id.profile_image);
        nameTextView     = (FontTextView) findViewById(R.id.profile_name);
        levelTextView    = (FontTextView) findViewById(R.id.profile_level);
        statusTextView   = (FontTextView) findViewById(R.id.profile_status);
        googleTextView   = (FontTextView) findViewById(R.id.profile_google_account);

        completeHabitsTextView     = (FontTextView) findViewById(R.id.profile_statistics_habits_complete);
        performedHabitsTextView    = (FontTextView) findViewById(R.id.profile_statistics_habits_performed);
        zeroProgressHabitsTextView = (FontTextView) findViewById(R.id.profile_statistics_habits_zero_progress);

        nickNameTextView = (FontTextView) findViewById(R.id.profile_settings_nickname);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RetrieveTokenTask().execute(user.getName());
            }
        });

        levelTextView.setText(getString(R.string.profile_level, 1));
        statusTextView.setText(getString(R.string.profile_status, Utils.getProfileStatusName(this, 1)));
        googleTextView.setText(getString(R.string.profile_google_account, HabitApplication.getInstance().getUsername()));

        completeHabitsTextView.setText(getString(R.string.profile_statistics_habits_complete, 0));
        performedHabitsTextView.setText(getString(R.string.profile_statistics_habits_performed, 0));
        zeroProgressHabitsTextView.setText(getString(R.string.profile_statistics_habits_zero_progress, 0));

        initPieChart();
        initUser();
        initHabitDetails();
    }


    private void initPieChart() {
        pieChart = (PieChart) findViewById(R.id.profile_statistics_chart);

        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setRotationEnabled(true);
        pieChart.setRotationAngle(0);
        pieChart.getLegend().setEnabled(false);

        xVals = new ArrayList<>();
        yVals = new ArrayList<>();

        xVals.add(completeHabitsTextView.getText()
                .subSequence(0, completeHabitsTextView.getText().length() - 3).toString());
        xVals.add(performedHabitsTextView.getText()
                .subSequence(0, performedHabitsTextView.getText().length() - 3).toString());
        xVals.add(zeroProgressHabitsTextView.getText()
                .subSequence(0, zeroProgressHabitsTextView.getText().length() - 3).toString());

        yVals.add(new Entry(0, 0));
        yVals.add(new Entry(0, 1));
        yVals.add(new Entry(0, 2));

        pieColors = new int[]{
                R.color.md_green_A400,
                R.color.md_amber_300,
                R.color.md_red_400
        };

        PieDataSet pieDataSet = new PieDataSet(yVals, "Habits");
        pieDataSet.setColors(pieColors, this);

        PieData pieData = new PieData(xVals, pieDataSet);
        pieData.setValueFormatter(new NotZeroPercentFormatter());
        pieData.setValueTextColor(ContextCompat.getColor(this, R.color.md_white_1000));
        pieData.setValueTextSize(12);
        pieData.setValueTypeface(Resources.getInstance().getTypeface("OpenSans-Regular"));

        pieChart.setData(pieData);
        pieChart.invalidate();
    }


    private void updateProfileData() {
        if (!TextUtils.isEmpty(user.getImgURL())) {
            ImageLoader.getInstance().displayImage(user.getImgURL(), profileImageView);
        }
        if(!TextUtils.isEmpty(user.getDisplayName())) {
            nameTextView.setText(user.getDisplayName());
        }
        if (!TextUtils.isEmpty(user.getNickName())) {
            String nickName = "<b>" + user.getNickName() + "</b>";
            nickNameTextView.setText(Html.fromHtml(nickName));
        } else {
            nickNameTextView.setText(R.string.profile_settings_nick_non_set);
        }

        if (habitsDetails != null) {
            int completeCount     = 0;
            int performedCount    = 0;
            int zeroProgressCount = 0;

            for (HabitDetails hd: habitsDetails) {
                if (hd.isComplete()) {
                    ++completeCount;
                } else if (hd.getCurrentDay() == 1 && !hd.isChecked()) {
                    ++zeroProgressCount;
                } else {
                    ++performedCount;
                }
            }
            yVals.get(0).setVal(completeCount);
            yVals.get(1).setVal(performedCount);
            yVals.get(2).setVal(zeroProgressCount);

            completeHabitsTextView.setText(getString(R.string.profile_statistics_habits_complete, completeCount));
            performedHabitsTextView.setText(getString(R.string.profile_statistics_habits_performed, performedCount));
            zeroProgressHabitsTextView.setText(getString(R.string.profile_statistics_habits_zero_progress, zeroProgressCount));

            pieChart.getData().getXVals().set(0, (completeCount == 0) ? "" : xVals.get(0));
            pieChart.getData().getXVals().set(1, (performedCount == 0) ? "" : xVals.get(1));
            pieChart.getData().getXVals().set(2, (zeroProgressCount == 0) ? "" : xVals.get(2));

            IPieDataSet pieDataSet = pieChart.getData().getDataSet();

            pieDataSet.getEntryForIndex(0).setVal(completeCount);
            pieDataSet.getEntryForIndex(1).setVal(performedCount);
            pieDataSet.getEntryForIndex(2).setVal(zeroProgressCount);

            pieChart.getData().notifyDataChanged();
            pieChart.notifyDataSetChanged();
            pieChart.invalidate();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left_half, R.anim.exit_to_right);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_SIGN_IN_REQUIRED:
                if (resultCode == RESULT_OK) {
                    // We had to sign in - now we can finish off the mToken request.
                    new RetrieveTokenTask().execute(user.getName());
                }
                break;
        }
    }


    private class RetrieveTokenTask extends AsyncTask<String, Void, JSONObject> {

        private static final String TAG = "Retrieve Token Task";
        private String displayedName;
        private String imgUrl;
        private String coverImgUrl;


        @Override
        protected JSONObject doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:https://www.googleapis.com/auth/plus.login "
                    + YouTubeScopes.YOUTUBE_READONLY;
            String token;
            JSONObject jsonObject = null;

            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);

                URL url = new URL("https://www.googleapis.com/plus/v1/people/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("Authorization","Bearer "+token);

                int sc = conn.getResponseCode();
                if (sc == 200) {
                    String content = CharStreams.toString(new InputStreamReader(conn.getInputStream(), Charsets.UTF_8));
                    if(!TextUtils.isEmpty(content)) {
                        try {
                            jsonObject = new JSONObject(content);

                            if (jsonObject.has("displayName")) {
                                displayedName = jsonObject.getString("displayName");
                            }
                            if (jsonObject.has("image")) {
                                imgUrl = jsonObject.getJSONObject("image").getString("url");
                            }
                            if (jsonObject.has("cover") &&
                                    jsonObject.getJSONObject("cover").has("coverPhoto")) {
                                coverImgUrl = jsonObject
                                        .getJSONObject("cover")
                                        .getJSONObject("coverPhoto")
                                        .getString("url");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Empty content");
                    }
                }
                if (sc == 401) {
                    GoogleAuthUtil.invalidateToken(ProfileActivity.this, token);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null || realm.isClosed()) {
                return;
            }

            realm.beginTransaction();

            if (!TextUtils.isEmpty(displayedName) && !displayedName.equals(user.getDisplayName())) {
                user.setDisplayName(displayedName);
            }
            if (!TextUtils.isEmpty(imgUrl) && !imgUrl.equals(user.getImgURL())) {
                user.setImgURL(imgUrl);
            }
            if (!TextUtils.isEmpty(coverImgUrl) && !coverImgUrl.equals(user.getCoverImgURL())) {
                user.setCoverImgURL(coverImgUrl);
            }

            realm.commitTransaction();

            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
