package com.gmail.dleemcewen.tandemfieri;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {
    final Calendar c = Calendar.getInstance();
    int day = c.get(Calendar.DAY_OF_MONTH);
    int month = c.get(Calendar.MONTH);
    int year = c.get(Calendar.YEAR);

    public interface DateListener {
        public void onFinishDialog(Date date);
    }

    private DatePicker datePicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.date_picker_dialog,null);

        datePicker = (DatePicker)v.findViewById(R.id.dialog_date_picker);

        return new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Select a Date")
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int day = datePicker.getDayOfMonth();
                                int month = datePicker.getMonth();
                                int year = datePicker.getYear();
                                DatePickerFragment.DateListener activity = (DatePickerFragment.DateListener) getActivity();
                                activity.onFinishDialog(updateDate(day, month, year));
                                dismiss();
                            }
                        })
                .create();
    }

    private Date updateDate(int day, int month, int year){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(year, month, day);
        return newCalendar.getTime();
    }
}
