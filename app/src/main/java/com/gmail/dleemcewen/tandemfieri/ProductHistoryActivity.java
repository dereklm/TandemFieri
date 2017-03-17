package com.gmail.dleemcewen.tandemfieri;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.ProductHistoryArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
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

public class ProductHistoryActivity extends AppCompatActivity implements DatePickerFragment.DateListener{
    int viewId;
    private TextView fromDate;
    private TextView toDate;
    private Date date_from;
    private Date date_to;
    private ListView listView;
    private DateListener dateListener;
    private Button showButton;
    private ShowButtonListener showButtonListener;
    private String restId;
    private String userId;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_history);

        getHandles();
        initialize();
        attachListeners();

    }//end on create

    private void initialize(){
        restId = getIntent().getStringExtra("restId");
        userId = getIntent().getStringExtra("ID");
        dateListener = new DateListener();
        showButtonListener = new ShowButtonListener();
    }

    private void getHandles(){
        fromDate = (TextView)findViewById(R.id.from_date);
        toDate = (TextView)findViewById(R.id.to_date);
        listView = (ListView)findViewById(R.id.product_list_view);
        showButton = (Button)findViewById(R.id.show_product_history);
    }

    private void attachListeners(){
        fromDate.setOnClickListener(dateListener);
        toDate.setOnClickListener(dateListener);
        showButton.setOnClickListener(showButtonListener);
    }

    private void retrieveData(){
        LogWriter.log(getApplicationContext(), Level.INFO, "show product history");
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
                        ArrayList<DisplayItem> displayItems = populateDisplayItems(selectedOrders);

                        //array adapter added here
                        if(selectedOrders.isEmpty()){
                            Toast.makeText(getApplicationContext(), "There are no orders for this time period.", Toast.LENGTH_LONG).show();
                        }else {
                            ProductHistoryArrayAdapter adapter = new ProductHistoryArrayAdapter(
                                    getApplicationContext(), displayItems);

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

        for(Order order : orders){
            for(OrderItem item : order.getItems()){
                if(!orderItems.contains(item.getName())){
                    orderItems.add(item.getName());
                    displayItems.add(new DisplayItem(item.getName(), item.getBasePrice()));
                }else{
                    int index = orderItems.indexOf(item.getName());
                    DisplayItem changeItem = displayItems.remove(index);
                    changeItem.addOne();
                    displayItems.add(changeItem);
                }
            }
        }
        return displayItems;
    }

    private ArrayList<Order> selectOrders(ArrayList<Order> orders){
        Criteria after = new CriteriaAfterStartDate(date_from);
        Criteria before = new CriteriaBeforeEndDate(date_to);
        Criteria between = new AndCriteria(after, before);
        return (ArrayList<Order>) between.meetCriteria(orders);
    }

    private double calculateItemSales(double basePrice, double currentAmount){
        return currentAmount + basePrice;
    }

    private boolean datesVerified(){
        //verify the start date is not after the end date
        if(date_from.after(date_to)){
            Toast.makeText(getApplicationContext(), "Please enter correct dates.", Toast.LENGTH_SHORT).show();
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
            if(datesVerified()) {
                retrieveData();
            }else{
                Toast.makeText(getApplicationContext(), "Please enter a valid date range.", Toast.LENGTH_LONG);
            }
        }
    }

}//end activity
