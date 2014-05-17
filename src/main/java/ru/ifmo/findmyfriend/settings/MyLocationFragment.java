package ru.ifmo.findmyfriend.settings;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
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
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getFragmentManager(), "datePicker");
    }

    private void chooseDurationTime() {
        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getFragmentManager(), "timePicker");
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

    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = customDuration.get(Calendar.YEAR);
            int month = customDuration.get(Calendar.MONTH);
            int day = customDuration.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            customDuration.set(Calendar.YEAR, year);
            customDuration.set(Calendar.MONTH, month);
            customDuration.set(Calendar.DAY_OF_MONTH, day);
            dismiss();
            chooseDurationTime();
        }
    }

    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = customDuration.get(Calendar.HOUR_OF_DAY);
            int minute = customDuration.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            customDuration.set(Calendar.HOUR_OF_DAY, hourOfDay);
            customDuration.set(Calendar.MINUTE, minute);
            dismiss();
            setCustomDuration();
        }
    }
}
