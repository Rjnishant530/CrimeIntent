package com.example.crimeintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;

public class TimePickerFragment extends DialogFragment {

    LocalTime localTime;
    TimePicker timePicker;
    public static final String LOCAL_TIME="com.example.crimeintent.LocalTime";
    public static final String EXTRA_TIME="com.bignerdranch.android.criminalintent.time";

    public static final TimePickerFragment newinstance(LocalTime localTime){

        Bundle bundle = new Bundle();
        bundle.putSerializable(LOCAL_TIME,localTime);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(bundle);
        return  fragment;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time,null);
        localTime=(LocalTime)getArguments().getSerializable(LOCAL_TIME);
        timePicker = v.findViewById(R.id.timePicker);
        timePicker.setMinute(localTime.getMinute());
        timePicker.setHour(localTime.getHour());

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Time :")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour = timePicker.getHour();
                        int minute = timePicker.getMinute();

                        LocalTime localTime = LocalTime.of(hour,minute);
                        sendResult(Activity.RESULT_OK,localTime);

                    }
                })
                .create();
    }

    public void sendResult(int ResultCode,LocalTime time){
        if(getTargetFragment()==null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME,time);

        getTargetFragment().onActivityResult(getTargetRequestCode(),ResultCode,intent);

    }
}
