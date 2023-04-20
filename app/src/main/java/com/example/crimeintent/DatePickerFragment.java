package com.example.crimeintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    public static final String ARG="date";
    public DatePicker mDatePicker;
    public static final String EXTRA_DATE="com.bignerdranch.android.criminalintent.date";

    public static DatePickerFragment newInstance(LocalDate date){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG,date);

        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArguments(bundle);
        return datePickerFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LocalDate date = (LocalDate)getArguments().getSerializable(ARG);


        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date,null);
        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(date.getYear(),date.getMonthValue()-1,date.getDayOfMonth(),null);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                       LocalDate date = LocalDate.of(year,month+1,day);
                       sendResult(Activity.RESULT_OK,date);
                    }
                })
                .create();
    }

    private void sendResult(int ResultCode,LocalDate date){
        if(getTargetFragment()==null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE,date);

        getTargetFragment().onActivityResult(getTargetRequestCode(),ResultCode,intent);

    }
}
