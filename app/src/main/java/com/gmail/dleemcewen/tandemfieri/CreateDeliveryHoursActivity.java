package com.gmail.dleemcewen.tandemfieri;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Entities.DeliveryHours;
import com.gmail.dleemcewen.tandemfieri.Events.ActivityEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class CreateDeliveryHoursActivity extends AppCompatActivity implements TimePickerFragment.TimeListener {

    LinearLayout sun, mon, tue, wed, thur, fri, sat;
    CheckBox sunBox, monBox, tueBox, wedBox, thurBox, friBox, satBox;
    TextView sunOpen, sunClose, monOpen, monClose, tueOpen, tueClose, wedOpen, wedClose, thurOpen, thurClose, friOpen, friClose, satOpen, satClose;
    BootstrapButton saveButton, clearButton;

    DatabaseReference mDatabase;

    int id;
    String restId, editOrCreate;

    ArrayList<CheckBox> boxlist;
    ArrayList<TextView> opentimes;
    ArrayList<TextView> closedtimes;

    View.OnClickListener timeListener;
    View.OnClickListener checkBoxListener;
    View.OnClickListener saveButtonListener;
    View.OnClickListener clearButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_business_hours_actvity);

        initialize();
        getHandlers();
        attachListeners();
    }//end on create

    public class TimeListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
           //begin time picker
            id = view.getId();
            DialogFragment fragment = new TimePickerFragment();
            fragment.show(getFragmentManager(), "timePicker");
        }
    }

    public class CheckBoxListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            setComponents((int) view.getTag());
        }
    }

    public class SaveButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            DeliveryHours deliveryHours = null;
            //create obj & save
            if(isValid()) {
                //create Business Hours object
                DeliveryHoursBuilder builder = new DeliveryHoursBuilder(restId);
                for(int i = 0; i< boxlist.size(); i++){
                    if(boxlist.get(i).isChecked()){
                        builder.add(boxlist.get(i).getText().toString(),
                                getTimeFromView(opentimes.get(i)),
                                getTimeFromView(closedtimes.get(i)));
                    }else {
                        builder.add(boxlist.get(i).getText().toString());
                    }
                }
                deliveryHours = builder.getDeliveryHours();
                if(!deliveryHours.isComplete()){
                    Toast.makeText(CreateDeliveryHoursActivity.this,
                            "error", Toast.LENGTH_SHORT).show();
                }
            }//end isValid

            if(deliveryHours != null && deliveryHours.isComplete()){
                save(deliveryHours);
            }

        }//end on click
    }//end listener

    public class ClearButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            clear();
        }
    }

    private void initialize(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DeliveryHours");

        restId = this.getIntent().getStringExtra("restId");
        editOrCreate = this.getIntent().getStringExtra("editOrCreate");

        timeListener = new TimeListener();
        checkBoxListener = new CheckBoxListener();
        saveButtonListener = new SaveButtonListener();
        clearButtonListener = new ClearButtonListener();

        boxlist = new ArrayList<>();
        opentimes = new ArrayList<>();
        closedtimes = new ArrayList<>();
    }

    private void getHandlers(){
        saveButton = (BootstrapButton)findViewById(R.id.save_button);
        clearButton = (BootstrapButton)findViewById(R.id.clear_button);

        sun = (LinearLayout)findViewById(R.id.sunG);
        sun.setVisibility(View.INVISIBLE);
        sunBox = (CheckBox)findViewById(R.id.boxSunday);
        sunBox.setTag(R.id.boxSunday);
        sunOpen = (TextView) findViewById(R.id.SunOpenTime);
        sunClose = (TextView)findViewById(R.id.SunClosedTime);
        boxlist.add(sunBox);
        opentimes.add(sunOpen);
        closedtimes.add(sunClose);

        mon = (LinearLayout)findViewById(R.id.monG);
        mon.setVisibility(View.INVISIBLE);
        monBox = (CheckBox)findViewById(R.id.boxMonday);
        monBox.setTag(R.id.boxMonday);
        monOpen = (TextView) findViewById(R.id.MonOpenTime);
        monClose = (TextView)findViewById(R.id.MonClosedTime);
        boxlist.add(monBox);
        opentimes.add(monOpen);
        closedtimes.add(monClose);

        tue = (LinearLayout)findViewById(R.id.tueG);
        tue.setVisibility(View.INVISIBLE);
        tueBox = (CheckBox)findViewById(R.id.boxTuesday);
        tueBox.setTag(R.id.boxTuesday);
        tueOpen = (TextView) findViewById(R.id.TueOpenTime);
        tueClose = (TextView)findViewById(R.id.TueClosedTime);
        boxlist.add(tueBox);
        opentimes.add(tueOpen);
        closedtimes.add(tueClose);

        wed = (LinearLayout)findViewById(R.id.wedG);
        wed.setVisibility(View.INVISIBLE);
        wedBox = (CheckBox)findViewById(R.id.boxWed);
        wedBox.setTag(R.id.boxWed);
        wedOpen = (TextView) findViewById(R.id.WedOpenTime);
        wedClose = (TextView)findViewById(R.id.WedClosedTime);
        boxlist.add(wedBox);
        opentimes.add(wedOpen);
        closedtimes.add(wedClose);

        thur = (LinearLayout)findViewById(R.id.ThurG);
        thur.setVisibility(View.INVISIBLE);
        thurBox = (CheckBox)findViewById(R.id.boxThursday);
        thurBox.setTag(R.id.boxThursday);
        thurOpen = (TextView) findViewById(R.id.ThurOpenTime);
        thurClose = (TextView)findViewById(R.id.ThurClosedTime);
        boxlist.add(thurBox);
        opentimes.add(thurOpen);
        closedtimes.add(thurClose);

        fri = (LinearLayout)findViewById(R.id.FriG);
        fri.setVisibility(View.INVISIBLE);
        friBox = (CheckBox)findViewById(R.id.boxFriday);
        friBox.setTag(R.id.boxFriday);
        friOpen = (TextView) findViewById(R.id.FriOpenTime);
        friClose = (TextView)findViewById(R.id.FriClosedTime);
        boxlist.add(friBox);
        opentimes.add(friOpen);
        closedtimes.add(friClose);

        sat = (LinearLayout)findViewById(R.id.SatG);
        sat.setVisibility(View.INVISIBLE);
        satBox = (CheckBox)findViewById(R.id.boxSaturday);
        satBox.setTag(R.id.boxSaturday);
        satOpen = (TextView) findViewById(R.id.SatOpenTime);
        satClose = (TextView)findViewById(R.id.SatClosedTime);
        boxlist.add(satBox);
        opentimes.add(satOpen);
        closedtimes.add(satClose);
    }

    private void attachListeners(){
        saveButton.setOnClickListener(saveButtonListener);
        clearButton.setOnClickListener(clearButtonListener);

        for(TextView tv: opentimes){
            tv.setOnClickListener(timeListener);
        }

        for(TextView tv: closedtimes){
            tv.setOnClickListener(timeListener);
        }

        for(CheckBox box: boxlist){
            box.setOnClickListener(checkBoxListener);
        }
    }

    private void setComponents(int boxId){
        switch(boxId){
            case R.id.boxSunday:
                if(sunBox.isChecked())
                    sun.setVisibility(View.VISIBLE);
                else
                    sun.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxMonday:
                if(monBox.isChecked())
                    mon.setVisibility(View.VISIBLE);
                else
                    mon.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxTuesday:
                if(tueBox.isChecked())
                    tue.setVisibility(View.VISIBLE);
                else
                    tue.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxWed:
                if(wedBox.isChecked())
                    wed.setVisibility(View.VISIBLE);
                else
                    wed.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxThursday:
                if(thurBox.isChecked())
                    thur.setVisibility(View.VISIBLE);
                else
                    thur.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxFriday:
                if(friBox.isChecked())
                    fri.setVisibility(View.VISIBLE);
                else
                    fri.setVisibility(View.INVISIBLE);
                break;
            case R.id.boxSaturday:
                if(satBox.isChecked())
                    sat.setVisibility(View.VISIBLE);
                else
                    sat.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFinishDialog(int time){

        for(TextView tv: opentimes){
            if(tv.getId() == id){
                tv.setText(displayTime(time));
            }
        }

        for(TextView tv: closedtimes){
            if(tv.getId() == id){
                tv.setText(displayTime(time));
            }
        }
    }

    //change name to delivery hours
    private String displayTime (int time) {
        //time is in the military format: 600 means 6 am and 1800 means 6 pm
        int mins = time % 100;
        int hours = (time - mins)/100;
        String min = "";
        String hour = "";
        String meridian = "";

        if(mins < 10)
            min = "0" + valueOf(mins);
        else
            min = valueOf(mins);

        if(hours == 12) {
            hour += "12";
            meridian = "PM";
        }
        else if(hours == 0) {
            hour += "12";
            meridian += "AM";
        }else if (hours > 12){
            hour = valueOf(hours - 12);
            meridian += "PM";
        }else if(hours < 12){
            hour = valueOf(hours);
            meridian += "AM";
        }

        return hour + ":" + min + " " + meridian;

    }

    private boolean isValid(){
        String msg = "";
        boolean result = true;
        if(!checkBoxIsChecked()){
            msg = "You haven't set your business hours.";
            result = false;
        }else if(!hoursAreValid()){
            msg = "Each day selected must have both and open time and a close time set.";
            result = false;
        }else if(hoursAreEqual()) {
            msg = "Opening time cannot equal Closing time.";
            result = false;
        }else{
            msg = "Saving your business hours.";
        }
        Toast.makeText(CreateDeliveryHoursActivity.this, msg, Toast.LENGTH_SHORT).show();
        return result;
    }

    private boolean checkBoxIsChecked(){
        for(CheckBox box: boxlist){
            if(box.isChecked())
                return true;
        }
        return false;
    }

    private boolean hoursAreValid(){

        for(int i = 0; i < boxlist.size(); i++) {
            if (boxlist.get(i).isChecked()) {
                String o = opentimes.get(i).getText().toString();
                String c = closedtimes.get(i).getText().toString();
                if (o.equals("") || c.equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hoursAreEqual(){

        for(int i = 0; i < boxlist.size(); i++){
            if(boxlist.get(i).isChecked()) {
                String open = opentimes.get(i).getText().toString();
                String closed = closedtimes.get(i).getText().toString();
                if (open.equals(closed) && !open.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void clear(){

        sun.setVisibility(View.INVISIBLE);
        mon.setVisibility(View.INVISIBLE);
        tue.setVisibility(View.INVISIBLE);
        wed.setVisibility(View.INVISIBLE);
        thur.setVisibility(View.INVISIBLE);
        fri.setVisibility(View.INVISIBLE);
        sat.setVisibility(View.INVISIBLE);
        for(CheckBox box: boxlist){
            box.setChecked(false);
        }
        for(TextView tv: opentimes){
            tv.setText("");
        }
        for(TextView tv1: closedtimes){
            tv1.setText("");
        }
    }

    private void save(DeliveryHours object){
        final DeliveryHours obj = object;

        //check if this is a create or edit
        if(editOrCreate.equals("create"))
            mDatabase.child(obj.getKey()).setValue(obj);
        else if(editOrCreate.equals("edit")){
            //find and remove the old object and save the new
            mDatabase.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean isFound = false;
                            for (DataSnapshot ps : dataSnapshot.getChildren()) {
                                DeliveryHours d = ps.getValue(DeliveryHours.class);
                                if(restId != null && d.getRestaurantId().equals(restId)){
                                    //correct restaurant found
                                    isFound = true;
                                    mDatabase.child(ps.getKey()).removeValue();
                                    mDatabase.child(obj.getKey()).setValue(obj);
                                }
                            }
                            if(!isFound && restId != null){

                                //restaurant # wasn't found in delivery hours so create a new delivery hours
                                mDatabase.child(obj.getKey()).setValue(obj);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    }
            );
        }


        EventBus.getDefault().post(new ActivityEvent(ActivityEvent.Result.REFRESH_RESTAURANT_LIST));
        finish();
    }

    private int getTimeFromView(TextView tv){
        String thistime = tv.getText().toString();
        String hr = thistime.substring(0, thistime.indexOf(':'));
        String m = thistime.substring(thistime.indexOf(':') + 1, thistime.indexOf(':') + 3);
        String meridian = thistime.substring(thistime.length()-2);
        int hour = Integer.parseInt(hr);
        int min = Integer.parseInt(m);
        if(hour == 12){
            if(meridian.equals("AM"))
                hour = 0;
        }else {
            if (meridian.equals("PM"))
                hour = hour + 12;
        }
        return hour * 100 + min;

    }

}//end activity
