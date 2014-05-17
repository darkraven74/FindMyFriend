package ru.ifmo.findmyfriend.settings;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ifmo.findmyfriend.MainActivity;
import ru.ifmo.findmyfriend.R;
import ru.ifmo.findmyfriend.UpdateService;

public class MyLocationFragment extends Fragment implements View.OnClickListener {
    private static final long[] sharingDurations = new long[]{TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(5)};

    private TextView status;
    private TextView cancel;
    private TextView share;
    private SharedPreferences prefs;

    private final Calendar customDuration = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.my_location_fragment, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        status = (TextView) view.findViewById(R.id.sharing_until);
        share = (TextView) view.findViewById(R.id.share);
        cancel = (TextView) view.findViewById(R.id.cancel);

        share.setOnClickListener(this);
        cancel.setOnClickListener(this);

        prefs = getActivity().getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

        updateState();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share:
                chooseSharingDuration();
                break;
            case R.id.cancel:
                setSharingTime(0);
                break;
        }
    }

    public void chooseSharingDuration() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.share_location);
        builder.setItems(R.array.dialog_array,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.cancel();
                        if (item <= 3) {
                            long time = System.currentTimeMillis() + sharingDurations[item];
                            setSharingTime(time);
                        } else {
                            customDuration.setTime(new Date());
                            chooseDurationDate();
                        }
                    }
                }
        );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void chooseDurationDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final DatePicker datePicker = createDatePicker();
        builder.setView(datePicker);
        builder.setTitle(getString(R.string.date_picker_title));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    customDuration.set(Calendar.YEAR, datePicker.getYear());
                    customDuration.set(Calendar.MONTH, datePicker.getMonth());
                    customDuration.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    chooseDurationTime();
                }
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(getString(R.string.dialog_negative_button), listener);
        builder.setPositiveButton(getString(R.string.dialog_positive_button), listener);
        builder.create().show();
    }

    private DatePicker createDatePicker() {
        DatePicker datePicker = new DatePicker(getActivity());
        datePicker.setCalendarViewShown(false);
        int year = customDuration.get(Calendar.YEAR);
        int month = customDuration.get(Calendar.MONTH);
        int day = customDuration.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, month, day, null);
        return datePicker;
    }

    private void chooseDurationTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final TimePicker timePicker = createTimePicker();
        builder.setView(timePicker);
        builder.setTitle(getString(R.string.time_picker_title));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    customDuration.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    customDuration.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    setCustomDuration();
                }
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(getString(R.string.dialog_negative_button), listener);
        builder.setPositiveButton(getString(R.string.dialog_positive_button), listener);
        builder.create().show();
    }

    private TimePicker createTimePicker() {
        TimePicker timePicker = new TimePicker(getActivity());
        int hour = customDuration.get(Calendar.HOUR_OF_DAY);
        int minute = customDuration.get(Calendar.MINUTE);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        return timePicker;
    }

    private void setCustomDuration() {
        long time = customDuration.getTimeInMillis();
        if (System.currentTimeMillis() > time) {
            Toast.makeText(getActivity(), getResources().getString(R.string.incorrect_time), Toast.LENGTH_SHORT).show();
            chooseDurationDate();
        } else {
            setSharingTime(time);
        }
    }

    private void setSharingTime(long time) {
        prefs.edit().putLong(MainActivity.PREFERENCE_SHARING_END_TIME, time).commit();
        updateState();
    }

    private void updateState() {
        long time = prefs.getLong(MainActivity.PREFERENCE_SHARING_END_TIME, 0);
        if (System.currentTimeMillis() > time) {
            status.setText(R.string.not_sharing);
            cancel.setVisibility(View.GONE);
            share.setText(getString(R.string.share_location));
        } else {
            cancel.setVisibility(View.VISIBLE);
            share.setText(getString(R.string.edit_time));
            String formattedSharingUntil = " "
                    + android.text.format.DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date(time));
            status.setText(getResources().getString(R.string.sharing_until) + formattedSharingUntil);
        }
    }
}
