package com.gmail.dleemcewen.tandemfieri;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrdersListAdapterAddress;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Orders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class DriverMainMenu extends AppCompatActivity {
    private User user;
    private String customerId, currentOrderId;
    private DatabaseReference mDatabaseDelivery, mDatabaseCurrentDelivery;
    private Order order, currentOrder;
    private Context context;
    private ListView ordersListView;
    private List<Order> entities;
    private Orders<Order> orders;
    private OrdersListAdapterAddress listAdapter;
    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_menu);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        context = this;

        ordersListView = (ListView) findViewById(R.id.orders);

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getAuthUserID());
        mDatabaseDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order");
        mDatabaseCurrentDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID());

        mDatabaseDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(), ""+dataSnapshot.child("Order").getValue(Order.class), Toast.LENGTH_LONG).show();
                //order = dataSnapshot.child("Order").getValue(Order.class);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for(DataSnapshot child2: child.getChildren()) {
                        order = child2.getValue(Order.class);
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }
              
                entities = new ArrayList<Order>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for(DataSnapshot child2: child.getChildren()) {
                        entities.add(child2.getValue(Order.class));
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }
                loadList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabaseCurrentDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentOrderId = (String) dataSnapshot.child("currentOrderId").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.OrderStatus);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFilter = adapterView.getItemAtPosition(i).toString().equalsIgnoreCase("all")? "":adapterView.getItemAtPosition(i).toString();
                loadList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }//end onCreate

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.driver_menu, menu);
        return true;
    }

    //determine which menu option was selected and call that option's action method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                return true;
            case R.id.edit_personal_info:
                editPersonalInformation();
                return true;
            case R.id.edit_password:
                editPassword();
                return true;
            case R.id.delivery:
                startDelivery();
                return true;
            case R.id.myDeliveries:
                showMyDeliveries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //called when user selects sign out from the drop down menu
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    private void startDelivery(){
        if(currentOrderId.equals(null)){
            Toast.makeText(getApplicationContext(), "Make sure you set a delivery as your current one", Toast.LENGTH_LONG).show();
        }else {
            for (Order order : entities) {
                if (order.getCustomerId().equals(currentOrderId)) {
                    currentOrder = order;
                }
            }
            Bundle driverBundle = new Bundle();
            Intent intent = new Intent(DriverMainMenu.this, DriverDeliveryActivity.class);
            driverBundle.putString("customerId", currentOrder.getCustomerId());

            driverBundle.putSerializable("Order", currentOrder);
            driverBundle.putSerializable("User", user);
            intent.putExtras(driverBundle);
            startActivity(intent);
        }
    }

    private void connect(){
        Intent intent = new Intent(DriverMainMenu.this, ConnectActivity.class);
        intent.putExtra("ID", user.getAuthUserID());
        startActivity(intent);
    }

    /**
     * showMyDeliveries displays all of the deliveries assigned to the current driver
     */
    private void showMyDeliveries() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        DriverOrdersFragment driverOrders = new DriverOrdersFragment();
        Bundle args = new Bundle();
        args.putString("driverId", user.getAuthUserID());
        args.putString("restaurantId", user.getRestaurantId());
        args.putSerializable("Order", order);
        driverOrders.setArguments(args);

        fragmentTransaction.add(R.id.activity_driver_main_menu, driverOrders);
        fragmentTransaction.commit();
    }

    //called when user selects edit information from the drop down menu
    private  void editPersonalInformation(){
        //need to send user type so that the user can be located in the database
        Bundle driverBundle = new Bundle();
        Intent intent = new Intent(DriverMainMenu.this, EditAccountActivity.class);
        driverBundle.putSerializable("User", user);
        intent.putExtras(driverBundle);
        intent.putExtra("UserType", "Driver");
        startActivity(intent);
    }

    //called when user selects edit password from the drop down menu
    private void editPassword(){
        //need to send user type so that the user can be located in the database
        Bundle driverBundle = new Bundle();
        Intent intent = new Intent(DriverMainMenu.this, EditPasswordActivity.class);
        driverBundle.putSerializable("User", user);
        intent.putExtras(driverBundle);
        intent.putExtra("UserType", "Driver");
        startActivity(intent);
    }

    private void loadList() {
        OrderEnum match = null;
        List<Order> toShow = new ArrayList<>();
        switch(currentFilter){
            case "":
                match= null;
                break;
            case "CREATING":
                match= OrderEnum.CREATING;
                break;
            case "PAYMENT PENDING":
                match = OrderEnum.PAYMENT_PENDING;
                break;
            case "EN ROUTE":
                match = OrderEnum.EN_ROUTE;
                break;
            case "COMPLETE":
                match = OrderEnum.COMPLETE;
                break;
        }
        if(match!=null){
            for(Order o : entities){
                if(o.getStatus() == match) toShow.add(o);
            }
            listAdapter = new OrdersListAdapterAddress(context,toShow);
            ordersListView.setAdapter(listAdapter);
        }
        else{
            if(entities!=null) {
                listAdapter = new OrdersListAdapterAddress(context, entities);
                ordersListView.setAdapter(listAdapter);
            }
        }
    }
}//end Activity
