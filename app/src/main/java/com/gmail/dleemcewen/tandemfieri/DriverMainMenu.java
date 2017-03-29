package com.gmail.dleemcewen.tandemfieri;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrdersListAdapterAddress;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Repositories.Orders;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private List<Order> entities;
    private OrdersListAdapterAddress listAdapter;
    private TextView noOrders;
    private boolean ordersShown = false;
    private String currentFilter = "";
    private NotificationMessages<NotificationMessage> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_menu);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        context = this;
        notifications = new NotificationMessages<>(DriverMainMenu.this);

        noOrders = (TextView) findViewById(R.id.no_assigned_orders);

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getAuthUserID());
        mDatabaseDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order");
        mDatabaseCurrentDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID());

        final int notificationId = bundle.getInt("notificationId");
        if (notificationId != 0) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(notificationId);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    notifications
                        .find("notificationId = '" + notificationId + "'")
                        .addOnCompleteListener(DriverMainMenu.this, new OnCompleteListener<TaskResult<NotificationMessage>>() {
                            @Override
                            public void onComplete(@NonNull Task<TaskResult<NotificationMessage>> task) {
                                List<NotificationMessage> messages = task.getResult().getResults();
                                if (!messages.isEmpty()) {
                                    notifications.remove(messages.get(0));
                                }
                            }
                        });
                }
            }).start();
        }//end notification block

        mDatabaseDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(), ""+dataSnapshot.child("Order").getValue(Order.class), Toast.LENGTH_LONG).show();
                //order = dataSnapshot.child("Order").getValue(Order.class);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (DataSnapshot child2 : child.getChildren()) {
                        order = child2.getValue(Order.class);
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }

                entities = new ArrayList<Order>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (DataSnapshot child2 : child.getChildren()) {
                        entities.add(child2.getValue(Order.class));
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }

                if (!ordersShown && order != null) {
                    noOrders.setVisibility(View.INVISIBLE);
                    ordersShown = true;
                    showMyDeliveries();
                } else if (order == null) {
                    noOrders.setVisibility(View.VISIBLE);
                }
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
    }//end onCreate

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ordersShown && order != null) {
            noOrders.setVisibility(View.INVISIBLE);
            currentOrder = null;
            showMyDeliveries();
        } else if (order == null) {
            noOrders.setVisibility(View.VISIBLE);
        }
    }

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
            case R.id.completedItems:
                editCompletedOrders();
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
        if(currentOrderId == null || currentOrderId.equals("")){
            Toast.makeText(getApplicationContext(), "Make sure you set a delivery as your current one", Toast.LENGTH_LONG).show();
        }else {
            for (Order order : entities) {
                if (order.getOrderId().equals(currentOrderId)) {
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
        args.putString("customerId", order.getCustomerId());
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

    private void editCompletedOrders(){
        Bundle driverBundle = new Bundle();
        Intent intent = new Intent(DriverMainMenu.this, CompetedOrdersDriverActivity.class);
        driverBundle.putSerializable("User", user);
        intent.putExtras(driverBundle);
        intent.putExtra("UserType", "Driver");
        startActivity(intent);
    }
}//end Activity