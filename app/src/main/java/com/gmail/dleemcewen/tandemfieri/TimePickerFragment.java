package com.gmail.dleemcewen.tandemfieri;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment  {
    final Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);

    public interface TimeListener {
        public void onFinishDialog(int time);
    }

    private TimePicker timePicker;
    public interface TimeDialogListener {
        void onFinishDialog(int time);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time,null);

        timePicker = (TimePicker) v.findViewById(R.id.dialog_time_picker);
        return new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Select a Time")
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = 0;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    hour = timePicker.getHour();
                                }else{
                                    hour = timePicker.getCurrentHour();
                                }
                                int minute = 0;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    minute = timePicker.getMinute();
                                }else{
                                    minute = timePicker.getCurrentMinute();
                                }
                                TimeListener activity = (TimeListener) getActivity();
                                activity.onFinishDialog(updateTime(hour, minute));
                                dismiss();
                            }
                        })
                .create();
    }

    private int updateTime(int hours, int mins){
       /*String myTime = "";
        if(hours < 10){
            myTime += "0" + hours;
        }else{
            myTime += hours;
        }
        if(mins < 10){
            myTime += "0" + mins;
        }else{
            myTime += mins;
        }

        return myTime;*/
        return hours * 100 + mins;
    }

    private String displayTime (int hours, int mins) {

        String timeSet = "";
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";

        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        String myTime = new StringBuilder().append(hours).append(':')
                .append(minutes).append(" ").append(timeSet).toString();

        return myTime;
    }
}
