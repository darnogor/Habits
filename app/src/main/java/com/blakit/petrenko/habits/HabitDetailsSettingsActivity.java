package com.blakit.petrenko.habits;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blakit.petrenko.habits.dao.UserDao;
import com.blakit.petrenko.habits.model.HabitDetails;
import com.blakit.petrenko.habits.utils.Utils;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import io.realm.Realm;

public class HabitDetailsSettingsActivity extends AppCompatActivity {

    private Realm realm;

    private HabitDetails habitDetails;

    private LinearLayout reminderEnable;
    private CheckBox reminderEnableCheck;
    private LinearLayout reminderTime;
    private TextView reminderTimeText;

    private AlertDialog resetDialog;
    private AlertDialog cancelDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details_settings);

        realm = Realm.getDefaultInstance();

        habitDetails = new UserDao(realm)
                .getHabitDetailsById(getIntent().getStringExtra("habit_details_id"));

        initToolbar();
        initDialogs();
        initReminderItems();
        initResetItems();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.settings_habit_details);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void initDialogs() {
        ContextThemeWrapper themeWrapper
                = new ContextThemeWrapper(this, R.style.AlertDialogTheme);

        resetDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.settings_habit_details_reset)
                .setMessage(R.string.settings_habit_details_reset_question)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realm.beginTransaction();
                        habitDetails.setChecked(false);
                        habitDetails.setCurrentDay(1);
                        realm.commitTransaction();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();

        cancelDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.settings_habit_details_cancel)
                .setMessage(R.string.settings_habit_details_cancel_question)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HabitDetailsActivity.finishHandler.sendEmptyMessage(0);
                        new UserDao(realm).deleteMarkHabitDetails(habitDetails.getId());
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .create();
    }


    private void initResetItems() {
        LinearLayout reset  = (LinearLayout) findViewById(R.id.settings_habit_details_reset);
        LinearLayout cancel = (LinearLayout) findViewById(R.id.settings_habit_details_cancel);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDialog.show();
                int color = ContextCompat.getColor(HabitDetailsSettingsActivity.this, R.color.md_grey_600);
                resetDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                resetDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDialog.show();
                int color = ContextCompat.getColor(HabitDetailsSettingsActivity.this, R.color.md_grey_600);
                cancelDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                cancelDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
            }
        });
    }


    private void initReminderItems() {
        reminderEnable      = (LinearLayout) findViewById(R.id.settings_habit_details_notifications_on);
        reminderEnableCheck = (CheckBox) findViewById(R.id.settings_habit_details_notifications_on_checkbox);
        reminderTime        = (LinearLayout) findViewById(R.id.settings_habit_details_notifications_time);
        reminderTimeText    = (TextView) findViewById(R.id.settings_habit_details_notifications_time_text);

        reminderEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.beginTransaction();
                habitDetails.setNotificationActivated(!reminderEnableCheck.isChecked());
                realm.commitTransaction();

                updateData();
            }
        });
        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.parseDate(habitDetails.getNotificationTime()));

                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        String timeStr = ((hourOfDay < 10) ? "0" : "") + hourOfDay + ":"
                                + ((minute < 10) ? "0" : "") + minute;

                    Log.d("!!!!Time Picker: ", timeStr);

                        realm.beginTransaction();
                        habitDetails.setNotificationTime(timeStr);
                        realm.commitTransaction();

                        updateData();
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(HabitDetailsSettingsActivity.this));

                timePickerDialog.show(getFragmentManager(), "TimePicker");
            }
        });

        updateData();
    }


    private void updateData() {
        reminderEnableCheck.setChecked(habitDetails.isNotificationActivated());

        String dateStr = DateFormat.getTimeFormat(this).format(Utils.parseDate(habitDetails.getNotificationTime()));

        reminderTime.setEnabled(reminderEnableCheck.isChecked());
        reminderTime.setAlpha(reminderEnableCheck.isChecked() ? 1.0f : 0.4f);
        reminderTimeText.setText(dateStr);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left_half, R.anim.exit_to_right);
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
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
