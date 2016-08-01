package com.zhaolongzhong.nytimessearch.helpers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = DatePickerFragment.class.getSimpleName();

    private DatePickerCallback datePickerCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        datePickerCallback.onDateSet(view, year, month, day);
        getDialog().dismiss();
    }

    public void setDatePickerCallback(DatePickerCallback datePickerCallback) {
        this.datePickerCallback = datePickerCallback;
    }

    public interface DatePickerCallback {
        void onDateSet(DatePicker view, int year, int month, int day);
    }
}