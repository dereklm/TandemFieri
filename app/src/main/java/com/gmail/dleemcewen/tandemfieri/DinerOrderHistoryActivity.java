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
import android.widget.ImageButton;
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
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaRestaurant;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import static android.R.drawable.arrow_down_float;
import static android.R.drawable.arrow_up_float;
import static java.lang.String.valueOf;

public class DinerOrderHistoryActivity extends AppCompatActivity implements DatePickerFragment.DateListener{

    private Button sortByButton, executeSortButton;
    private ImageButton amountButton;
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
    private boolean descending = true;

    private DateListener dateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_order_history);

        getHandles();
        initialize();
        retrieveData();

    }//end on create

    private void getHandles(){
        sortByButton = (Button)findViewById(R.id.sort_by_button);
        executeSortButton = (Button)findViewById(R.id.execute_button);
        amountButton = (ImageButton)findViewById(R.id.sort_by_amount_button);
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
        orderHistoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openOrder((Order) parent.getItemAtPosition(position), position);
            }
        });

        dateListener = new DateListener();
        fromDateView.setOnClickListener(dateListener);
        toDateView.setOnClickListener(dateListener);

        sortByButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(checkboxLayout.getVisibility() == View.INVISIBLE) {
                    if(!displayList.isEmpty()){
                        resetView();
                    }
                    checkboxLayout.setVisibility(View.VISIBLE);
                }else{
                    resetView();
                    sortByDateBox.setChecked(false);
                    sortByRestaurantBox.setChecked(false);
                    checkboxLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        executeSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeSort();
            }
        });

        amountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(displayList.isEmpty()){
                    executeSort();
                }
                sortByAmount();
            }
        });

        sortByDateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortByDateBox.isChecked()){
                    selectDateLayout.setVisibility(View.VISIBLE);
                }else{
                    selectDateLayout.setVisibility(View.INVISIBLE);
                    fromDateView.setText("tap here");
                    toDateView.setText("tap here");
                    dateFrom = null;
                    dateTo = null;
                }
            }
        });

        sortByRestaurantBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortByRestaurantBox.isChecked()){
                    spinnerLayout.setVisibility(View.VISIBLE);
                }else{
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    restaurantSelected = "";
                }
            }
        });

        restaurantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                restaurantSelected = (String) parent.getItemAtPosition(position);
                LogWriter.log(getApplicationContext(), Level.INFO, "restaurant selected is " + restaurantSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                restaurantSelected = "";
            }
        });
    }

    private void executeSort(){
        if(sortByDateBox.isChecked()){
            if(datesVerified()){
                resetView();
                sortOrders();
                sortByDateBox.setChecked(false);
                sortByRestaurantBox.setChecked(false);
                orderHistoryView.setVisibility(View.VISIBLE);
            }else{
                orderHistoryView.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }

        } else{
            resetView();
            sortOrders();
            sortByDateBox.setChecked(false);
            sortByRestaurantBox.setChecked(false);
            orderHistoryView.setVisibility(View.VISIBLE);
        }
    }

    private void sortByAmount() {
        if(descending){
             Comparator<Order> descendingSort = new Comparator<Order>() {

                 @Override
                 public int compare(Order order1, Order order2) {
                     return (int) (order2.getTotal() - order1.getTotal());
                 }
            };

            Collections.sort(displayList, descendingSort);

            amountButton.setBackgroundResource(arrow_down_float);
            dinerOrderHistoryArrayAdapter.notifyDataSetChanged();
            descending = false;
        }else{
            Comparator<Order> ascendingSort = new Comparator<Order>() {

                @Override
                public int compare(Order order1, Order order2) {
                    return (int) (order1.getTotal() - order2.getTotal());
                }
            };

            Collections.sort(displayList, ascendingSort);

            amountButton.setBackgroundResource(arrow_up_float);
            dinerOrderHistoryArrayAdapter.notifyDataSetChanged();
            descending = true;
        }
    }

    private void resetView(){
        orderHistoryView.setVisibility(View.INVISIBLE);
        spinnerLayout.setVisibility(View.INVISIBLE);
        selectDateLayout.setVisibility(View.INVISIBLE);
        checkboxLayout.setVisibility(View.INVISIBLE);
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
                //this populates orderList with all the orders associated with this diner
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

                //populate spinner adapter
                for(Order order: ordersList){
                    if(!restaurantList.contains(order.getRestaurantName())) {
                        restaurantList.add(order.getRestaurantName());
                    }
                }
                restaurantAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.view_order_items, restaurantList);
                restaurantSpinner.setAdapter(restaurantAdapter);

            }//end on data change

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });//end listener
    }//end retrieve data

    public void sortOrders(){
        if(ordersList.isEmpty()){
            LogWriter.log(getApplicationContext(), Level.INFO, "order list is empty");
        }else{
            LogWriter.log(getApplicationContext(), Level.INFO, "order list has " + valueOf(ordersList.size()) + " items.");
        }

        if(sortByDateBox.isChecked() && sortByRestaurantBox.isChecked()){
            if(datesVerified()){
                displayList = selectOrdersByDate(ordersList);
                displayList = selectOrdersByRestaurant(displayList);
                LogWriter.log(getApplicationContext(), Level.INFO, "display list has " + valueOf(displayList.size()) + " items.");
            }else{
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }
        }
        else if(sortByDateBox.isChecked()){
            displayList = selectOrdersByDate(ordersList);
           /* if(datesVerified()){
                displayList = selectOrdersByDate(ordersList);
                LogWriter.log(getApplicationContext(), Level.INFO, "display list has " + valueOf(displayList.size()) + " items.");
            }else{
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }*/
        }else if(sortByRestaurantBox.isChecked()){
            displayList = selectOrdersByRestaurant(ordersList);
        }else{
            displayList = ordersList;
            LogWriter.log(getApplicationContext(), Level.INFO, "display list has " + valueOf(displayList.size()) + " items.");
        }

        dinerOrderHistoryArrayAdapter = new DinerOrderHistoryArrayAdapter(getApplicationContext(), displayList);
        orderHistoryView.setAdapter(dinerOrderHistoryArrayAdapter);

        if(displayList.isEmpty()){
            Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
            orderHistoryView.setVisibility(View.INVISIBLE);
        }

    }

    private ArrayList<Order> selectOrdersByRestaurant(ArrayList<Order> orders){
        Criteria selectedRestaurant = new CriteriaRestaurant(restaurantSelected);
        return (ArrayList<Order>)selectedRestaurant.meetCriteria(orders);
    }

    private ArrayList<Order> selectOrdersByDate(ArrayList<Order> orders){
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
