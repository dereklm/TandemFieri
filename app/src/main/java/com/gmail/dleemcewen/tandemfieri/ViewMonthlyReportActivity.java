package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Adapters.MonthlyReportArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Filters.AndCriteria;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaAfterStartDate;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaBeforeEndDate;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaRestaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.gmail.dleemcewen.tandemfieri.R.array.month_selection;
import static com.gmail.dleemcewen.tandemfieri.R.array.year_selection;

public class ViewMonthlyReportActivity extends AppCompatActivity {

    private ListView restaurantListView;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private ListView displayListView;
    private BootstrapButton executeButton;
    private User currentUser;
    private ArrayAdapter<String> restaurantAdapter;
    private MonthlyReportArrayAdapter monthlyReportArrayAdapter;
    private ArrayList<Order> orderList;
    private ArrayList<String> restaurantNamesList;
    private ArrayList<String> selectedRestaurants;
    private ArrayList<DisplayItem> displayList;
    private String monthSelected = "";
    private String yearSelected = "";
    private boolean show = true;

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_monthly_report);

        findViewsById();
        initialize();
        retrieveData();
    }//end on Create

    private void findViewsById(){
        restaurantListView = (ListView)findViewById(R.id.restaurant_name_spinner);
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        displayListView = (ListView)findViewById(R.id.display_sales_report);
        executeButton = (BootstrapButton) findViewById(R.id.go_button);
    }

    private void initialize() {
        Bundle bundle = this.getIntent().getExtras();
        currentUser = (User)bundle.getSerializable("User");
        orderList = new ArrayList<>();
        restaurantNamesList = new ArrayList<>();
        selectedRestaurants = new ArrayList<>();
        displayList = new ArrayList<>();
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeSearch();
            }
        });

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                month_selection, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected.
                monthSelected = parent.getItemAtPosition(pos).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                year_selection, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected.
                yearSelected = parent.getItemAtPosition(pos).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    private void retrieveData(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order").child(currentUser.getAuthUserID());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get list of orders for this owner
                for(DataSnapshot orders : dataSnapshot.getChildren()){
                    Order o = orders.getValue(Order.class);
                    orderList.add(o);
                }

                //populate spinner adapter
                for(Order order: orderList){
                    if(!restaurantNamesList.contains(order.getRestaurantName())) {
                        restaurantNamesList.add(order.getRestaurantName());
                    }
                }
                restaurantAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_multiple_choice, restaurantNamesList);
                restaurantListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                restaurantListView.setAdapter(restaurantAdapter);
                restaurantListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void executeSearch(){

        //clear display list
        if(!displayList.isEmpty()){
            displayList.clear();
        }
        //get the selected restaurants
        selectedRestaurants = getSelectedRestaurants();

        //create list of DisplayItem objects
        for(String name : selectedRestaurants){
            DisplayItem newItem = new DisplayItem(name);
            displayList.add(newItem);
        }

        //filter orders by selected restaurants
        ArrayList<Order> ordersSelected = new ArrayList<>();
        for(String s : selectedRestaurants){
            CriteriaRestaurant cr = new CriteriaRestaurant(s);
            ordersSelected.addAll((ArrayList<Order>) cr.meetCriteria(orderList));
        }

        //filter orders by month
        ordersSelected = filterMonth(ordersSelected);

        //accumulate order totals
        for(DisplayItem current : displayList){
            for(Order o : ordersSelected){
                if(current.getName().equals(o.getRestaurantName())){
                    current.setTotal(current.getTotal() + o.getTotal());
                }
            }
        }

        //set state of activity
        displayResults();

        if(show){
            show = false;
        }else{
            show = true;
        }
    }

    private ArrayList<Order> filterMonth(ArrayList<Order> orders){
        Calendar calendar = Calendar.getInstance();
        int year = Integer.parseInt(yearSelected);
        Date begin = null;
        Date end =  null;
        switch (monthSelected){
            case "January":
                calendar.set(year, 0, 1);
                begin = calendar.getTime();
                calendar.set(year, 0, 31);
                end = calendar.getTime();
                break;
            case "February" :
                calendar.set(year, 2, 1);
                begin = calendar.getTime();
                calendar.set(year, 2, 28);
                end = calendar.getTime();
                break;
            case "March" :
                calendar.set(year, 2, 1);
                begin = calendar.getTime();
                calendar.set(year, 2, 31);
                end = calendar.getTime();
                break;
            case "April" :
                calendar.set(year, 3, 1);
                begin = calendar.getTime();
                calendar.set(year, 3, 30);
                end = calendar.getTime();
                break;
            case "May" :
                calendar.set(year, 4, 1);
                begin = calendar.getTime();
                calendar.set(year, 4, 31);
                end = calendar.getTime();
                break;
            case "June" :
                calendar.set(year, 5, 1);
                begin = calendar.getTime();
                calendar.set(year, 5, 30);
                end = calendar.getTime();
                break;
            case "July" :
                calendar.set(year, 6, 1);
                begin = calendar.getTime();
                calendar.set(year, 6, 31);
                end = calendar.getTime();
                break;
            case "August" :
                calendar.set(year, 7, 1);
                begin = calendar.getTime();
                calendar.set(year, 7, 31);
                end = calendar.getTime();
                break;
            case "September" :
                calendar.set(year, 8, 1);
                begin = calendar.getTime();
                calendar.set(year, 8, 30);
                end = calendar.getTime();
                break;
            case "October" :
                calendar.set(year, 9, 1);
                begin = calendar.getTime();
                calendar.set(year, 9, 31);
                end = calendar.getTime();
                break;
            case "November" :
                calendar.set(year, 10, 1);
                begin = calendar.getTime();
                calendar.set(year, 10, 30);
                end = calendar.getTime();
                break;
            case "December" :
                calendar.set(year, 11, 1);
                begin = calendar.getTime();
                calendar.set(year, 11, 31);
                end = calendar.getTime();
                break;
            default:
                break;
        }

        CriteriaAfterStartDate beginCriteria = new CriteriaAfterStartDate(begin);
        CriteriaBeforeEndDate endCriteria = new CriteriaBeforeEndDate(end);
        AndCriteria between = new AndCriteria(beginCriteria, endCriteria);
        return (ArrayList<Order>) between.meetCriteria(orders);
    }

    private void displayResults(){
        //set adapter
        monthlyReportArrayAdapter = new MonthlyReportArrayAdapter(getApplicationContext(), displayList, monthSelected, yearSelected);
        displayListView.setAdapter(monthlyReportArrayAdapter);

        if(displayList.isEmpty()){
            Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
            displayListView.setVisibility(View.INVISIBLE);
            restaurantListView.setVisibility(View.VISIBLE);
        }

        //prepare view
        if(show) {
            restaurantListView.setVisibility(View.INVISIBLE);
            displayListView.setVisibility(View.VISIBLE);
        }else{
            restaurantListView.setVisibility(View.VISIBLE);
            displayListView.setVisibility(View.INVISIBLE);
        }
    }

    private ArrayList<String> getSelectedRestaurants(){
        SparseBooleanArray checked = restaurantListView.getCheckedItemPositions();
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                results.add(restaurantAdapter.getItem(position));
        }

        return results;
    }
}//end activity
