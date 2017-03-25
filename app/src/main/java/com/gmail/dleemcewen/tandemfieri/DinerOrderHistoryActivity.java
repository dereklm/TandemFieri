package com.gmail.dleemcewen.tandemfieri;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.DinerOrderHistoryArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Filters.AndCriteria;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaAfterStartDate;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaBeforeEndDate;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

public class DinerOrderHistoryActivity extends AppCompatActivity implements DatePickerFragment.DateListener{

    private Button sortByButton, executeSortButton;
    private LinearLayout checkboxLayout, selectDateLayout, spinnerLayout;
    private CheckBox sortByDateBox, sortByRestaurantBox;
    private TextView fromDateView, toDateView;
    private Spinner restaurantSpinner;
    private ListView orderHistoryView;

    private User user;
    private ArrayList<Order> ordersList, displayList;
    private ArrayList<String> restaurantList;
    private DatabaseReference mDatabase;
    private DinerOrderHistoryArrayAdapter dinerOrderHistoryArrayAdapter;
    private ArrayAdapter<String> restaurantAdapter;
    private Date dateFrom, dateTo;
    private String restaurantSelected = "";
    private int viewId;

    private DateListener dateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_order_history);

        getHandles();
        initialize();

    }//end on create

    private void getHandles(){
        sortByButton = (Button)findViewById(R.id.sort_by_button);
        executeSortButton = (Button)findViewById(R.id.execute_button);
        checkboxLayout = (LinearLayout)findViewById(R.id.sort_checkbox_layout);
        selectDateLayout = (LinearLayout)findViewById(R.id.date_options_layout);
        spinnerLayout = (LinearLayout)findViewById(R.id.restaurant_spinner_layout);
        sortByDateBox = (CheckBox)findViewById(R.id.sort_by_date);
        sortByRestaurantBox = (CheckBox)findViewById(R.id.sort_by_restaurant);
        fromDateView = (TextView)findViewById(R.id.from_date);
        toDateView = (TextView)findViewById(R.id.to_date);
        restaurantSpinner = (Spinner)findViewById(R.id.restaurant_name_spinner);
        orderHistoryView = (ListView)findViewById(R.id.diner_order_history_listview);
    }

    private void initialize(){
        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        ordersList = new ArrayList<>();
        restaurantList = new ArrayList<>();
        displayList = new ArrayList<>();
        dateListener = new DateListener();
        orderHistoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openOrder((Order) parent.getItemAtPosition(position), position);
            }
        });

        fromDateView.setOnClickListener(dateListener);
        toDateView.setOnClickListener(dateListener);

        sortByButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(!displayList.isEmpty()) {
                    resetView();
                }
                if(checkboxLayout.getVisibility() == View.INVISIBLE) {
                    sortByDateBox.setChecked(false);
                    sortByRestaurantBox.setChecked(false);
                    checkboxLayout.setVisibility(View.VISIBLE);
                }else{
                    checkboxLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        executeSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!displayList.isEmpty()) {
                    resetView();
                }else{
                    checkboxLayout.setVisibility(View.INVISIBLE);
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    selectDateLayout.setVisibility(View.INVISIBLE);
                }
                retrieveData();
                orderHistoryView.setVisibility(View.VISIBLE);
            }
        });

        sortByDateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortByDateBox.isChecked()){
                    selectDateLayout.setVisibility(View.VISIBLE);
                }else{
                    selectDateLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        sortByRestaurantBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortByRestaurantBox.isChecked()){
                    //this needs to be an async task, I think
                   // fetchRestaurants();
                    spinnerLayout.setVisibility(View.VISIBLE);
                }else{
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    //restaurantAdapter.clear();
                    //restaurantList.clear();
                }
            }
        });

        restaurantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                restaurantSelected = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                restaurantSelected = "";
            }
        });
    }

    private void resetView(){
        displayList.clear();
        ordersList.clear();
        dinerOrderHistoryArrayAdapter.clear();
        orderHistoryView.setVisibility(View.INVISIBLE);
        spinnerLayout.setVisibility(View.INVISIBLE);
        selectDateLayout.setVisibility(View.INVISIBLE);
        checkboxLayout.setVisibility(View.INVISIBLE);
        sortByDateBox.setChecked(false);
        sortByRestaurantBox.setChecked(false);
    }

    private void openOrder(Order order, int position){
        Bundle orderBundle = new Bundle();
        Intent intent = new Intent(DinerOrderHistoryActivity.this, ViewOrderActivity.class);
        orderBundle.putSerializable("Order", order);
        intent.putExtras(orderBundle);
        startActivity(intent);
    }

    private void retrieveData(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //everything to do with order list code here
                for(DataSnapshot owners: dataSnapshot.getChildren()){
                    //LogWriter.log(getApplicationContext(), Level.INFO, "owner #: " + owners.getKey());
                    for(DataSnapshot orders : owners.getChildren()){
                        //LogWriter.log(getApplicationContext(), Level.INFO, "order #: " + orders.getKey());
                        Order o = orders.getValue(Order.class);
                       //LogWriter.log(getApplicationContext(), Level.INFO, "customer #: " + o.getCustomerId());
                        if(o.getCustomerId() != null){
                            if(o.getCustomerId().equals(user.getAuthUserID())){
                                ordersList.add(o);
                            }
                        }
                    }
                }

                if(sortByDateBox.isChecked()){
                    if(datesVerified()){
                        displayList = selectOrders(ordersList);
                    }else{
                        Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
                    }
                }else if(sortByRestaurantBox.isChecked()){
                    LogWriter.log(getApplicationContext(), Level.INFO, "This is not implemented yet");
                }else{
                    displayList = ordersList;
                }


                if(displayList.isEmpty()){
                    Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
                }else {

                    dinerOrderHistoryArrayAdapter = new DinerOrderHistoryArrayAdapter(getApplicationContext(), displayList);
                    orderHistoryView.setAdapter(dinerOrderHistoryArrayAdapter);
                    }

            }//end on data change

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });//end listener
    }//end retrieve data

    private ArrayList<Order> selectOrders(ArrayList<Order> orders){
        //this criteria selects the orders between the chosen dates
        Criteria after = new CriteriaAfterStartDate(dateFrom);
        Criteria before = new CriteriaBeforeEndDate(dateTo);
        Criteria between = new AndCriteria(after, before);
        return (ArrayList<Order>) between.meetCriteria(orders);
    }

    private boolean datesVerified(){
        //verify the start date is not after the end date
        Date current = new Date();
        if (dateFrom == null || dateTo == null){
            Toast.makeText(getApplicationContext(), "Please enter valid dates.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(dateFrom.after(dateTo)){
            Toast.makeText(getApplicationContext(), "From date cannot be after To date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dateFrom.after(current)){
            Toast.makeText(getApplicationContext(), "From date cannot be after today's date.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onFinishDialog(Date date) {
        Date current = new Date();
        SimpleDateFormat formatDateJava = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if(viewId == fromDateView.getId()){
            dateFrom = date;
            fromDateView.setText(formatDateJava.format(dateFrom));
        }else if(viewId == toDateView.getId()){
            if(date.after(current)){
                dateTo = current;
            }else{
                dateTo = date;
            }
            toDateView.setText(formatDateJava.format(dateTo));
        }
    }

    public class DateListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //begin date picker
            viewId = view.getId();
            DialogFragment fragment = new DatePickerFragment();
            fragment.show(getFragmentManager(), "datePicker");
        }
    }
}//end activity
