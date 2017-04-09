package com.gmail.dleemcewen.tandemfieri;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Adapters.ProductHistoryArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.DisplayItem;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Filters.AndCriteria;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaAfterStartDate;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaBeforeEndDate;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaSelectedItem;
import com.gmail.dleemcewen.tandemfieri.Interfaces.Criteria;
import com.gmail.dleemcewen.tandemfieri.Utility.CsvUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.gmail.dleemcewen.tandemfieri.R.id.select_item;

public class ProductHistoryActivity extends AppCompatActivity implements DatePickerFragment.DateListener{
    private int viewId;
    private TextView fromDate;
    private TextView toDate;
    private Date date_from;
    private Date date_to;
    private ListView listView;
    private DateListener dateListener;
    private Button showButton;
    private BootstrapButton csvButton;
    private ShowButtonListener showButtonListener;
    private String userId;
    private Restaurant restaurant;
    private DatabaseReference mDatabase;
    private String selectedMenuItem = "";
    private boolean itemSelected = false;
    private ArrayList<DisplayItem> displayItems;
    private ProductHistoryArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_history);

        getHandles();
        initialize();
        attachListeners();

    }//end on create

    private void initialize(){
        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");
        userId = getIntent().getStringExtra("ID");
        dateListener = new DateListener();
        showButtonListener = new ShowButtonListener();
        displayItems = new ArrayList<>();
    }

    private void getHandles(){
        fromDate = (TextView)findViewById(R.id.from_date);
        toDate = (TextView)findViewById(R.id.to_date);
        listView = (ListView)findViewById(R.id.product_list_view);
        showButton = (Button)findViewById(R.id.show_product_history);
        csvButton = (BootstrapButton) findViewById(R.id.csv_button_ph);

        csvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CsvUtil.emailCsv(displayItems, "", getApplicationContext());
            }
        });
    }

    private void attachListeners(){
        fromDate.setOnClickListener(dateListener);
        toDate.setOnClickListener(dateListener);
        showButton.setOnClickListener(showButtonListener);
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.product_history_menu, menu);
        return true;
    }

    //determine which menu option was selected and call that option's action method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case select_item:
                optionsDialog().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public AlertDialog optionsDialog() {

        ArrayAdapter<com.gmail.dleemcewen.tandemfieri.menubuilder.MenuItem> adapter = new ArrayAdapter<>(
                getApplicationContext(), R.layout.view_order_items, restaurant.getMenu().getSubItems());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Item: ");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedMenuItem = restaurant.getMenu().getSubItems().get(i).getName();
                itemSelected = true;
                //LogWriter.log(getApplicationContext(), Level.INFO, "The selected item is: " + selectedMenuItem);
            }
        });
        return builder.create();

    }

    private void retrieveData(){

        //get all the orders for this restaurant
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order").child(userId);
        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot orderSnapshot) {
                        ArrayList<Order> orders = new ArrayList<>();
                        for(DataSnapshot child: orderSnapshot.getChildren()){
                            Order order = child.getValue(Order.class);
                            orders.add(order);
                        }//end for loop

                        //selection criteria here
                        ArrayList<Order> selectedOrders = selectOrders(orders);

                        //create a list of all the items
                        displayItems = populateDisplayItems(selectedOrders);

                        //array adapter added here
                        if(selectedOrders.isEmpty()){
                            Toast.makeText(getApplicationContext(), "There are no orders for this time period.", Toast.LENGTH_LONG).show();
                        }else {
                            adapter = new ProductHistoryArrayAdapter(getApplicationContext(), displayItems);

                            listView.setAdapter(adapter);
                        }

                    }//end on data change

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                }
        );
    }

    private ArrayList<DisplayItem> populateDisplayItems(ArrayList<Order> orders){
        ArrayList<String> orderItems =new ArrayList<>();
        ArrayList<DisplayItem> displayItems = new ArrayList<>();
        if(itemSelected){
            DisplayItem displayItem = new DisplayItem(selectedMenuItem);
            //get base price:
            for(OrderItem item : orders.get(0).getItems()){
                if(item.getName().equals(selectedMenuItem)){
                    displayItem.setBasePrice(item.getBasePrice());
                    break;
                }
            }
            //count the number of times it is ordered
            for(Order order : orders){
                for(OrderItem item : order.getItems()){
                    if(item.getName().equals(selectedMenuItem)){
                        displayItem.addOne();
                    }
                }
            }
            displayItems.add(displayItem);
        }else {
            //display all order items in the orders list
            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    if (!orderItems.contains(item.getName())) {
                        orderItems.add(item.getName());
                        DisplayItem newItem = new DisplayItem(item.getName(), item.getBasePrice());
                        newItem.setQuantity(1);
                        displayItems.add(newItem);
                    } else {
                        int index = orderItems.indexOf(item.getName());
                        displayItems.get(index).addOne();
                    }
                }
            }
        }
        return displayItems;
    }

    private ArrayList<Order> selectOrders(ArrayList<Order> orders){
        //this criteria selects the orders between the chosen dates
        Criteria after = new CriteriaAfterStartDate(date_from);
        Criteria before = new CriteriaBeforeEndDate(date_to);
        Criteria between = new AndCriteria(after, before);
        if(itemSelected){
            //an item has been selected
            Criteria itemSelection = new CriteriaSelectedItem(selectedMenuItem);
            Criteria dateAndItem = new AndCriteria(between, itemSelection);
            return (ArrayList<Order>) dateAndItem.meetCriteria(orders);
        }else {
            return (ArrayList<Order>) between.meetCriteria(orders);
        }
    }

    private boolean datesVerified(){
        //verify the start date is not after the end date
        Date current = new Date();
        if (date_from == null || date_to == null){
            Toast.makeText(getApplicationContext(), "Please enter valid dates.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(date_from.after(date_to)){
            Toast.makeText(getApplicationContext(), "From date cannot be after To date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(date_from.after(current)){
            Toast.makeText(getApplicationContext(), "From date cannot be after today's date.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onFinishDialog(Date date) {
        Date current = new Date();
        SimpleDateFormat formatDateJava = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if(viewId == fromDate.getId()){
            date_from = date;
            fromDate.setText(formatDateJava.format(date_from));
        }else if(viewId == toDate.getId()){
            if(date.after(current)){
                date_to = current;
            }else{
                date_to = date;
            }
            toDate.setText(formatDateJava.format(date_to));
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

    public class ShowButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(!displayItems.isEmpty()){
                displayItems.clear();
                adapter.clear();
            }
            if(datesVerified()) {
                retrieveData();
                csvButton.setEnabled(true);
            }else{
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }
        }
    }

}//end activity
