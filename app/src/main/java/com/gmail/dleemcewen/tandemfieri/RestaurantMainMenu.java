package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.RestaurantMainMenuExpandableListAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Events.ActivityEvent;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RestaurantMainMenu extends AppCompatActivity {

    protected static final String TAG = "RestaurantMainMenu";

    private User user;
    private NotificationMessages<NotificationMessage> notificationsRepository;
    private ExpandableListView orderList, assignedOrderList;
    private RestaurantMainMenuExpandableListAdapter listAdapter, listAdapterAssigned;
    private DatabaseReference mDatabase;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_main_menu);

        context = this;

        EventBus.getDefault().register(this);

        notificationsRepository = new NotificationMessages<>(RestaurantMainMenu.this);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        orderList = (ExpandableListView)findViewById(R.id.order_list);
        assignedOrderList = (ExpandableListView)findViewById(R.id.order_list_assigned);
        //header = (TextView) findViewById(R.id.header);

        final int notificationId = bundle.getInt("notificationId");
        if (notificationId != 0) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(notificationId);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    notificationsRepository
                        .find("notificationId = '" + notificationId + "'")
                        .addOnCompleteListener(RestaurantMainMenu.this, new OnCompleteListener<TaskResult<NotificationMessage>>() {
                            @Override
                            public void onComplete(@NonNull Task<TaskResult<NotificationMessage>> task) {
                                List<NotificationMessage> messages = task.getResult().getResults();
                                if (!messages.isEmpty()) {
                                    notificationsRepository.remove(messages.get(0));
                                }
                            }
                        });
                }
            }).start();
        }//end notification block

        retrieveData();

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getEmail());
    }//end onCreate

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.restaurant_owner_menu, menu);
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
            case R.id.manage_restaurants:
                goToManageRestaurants();
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

    //called when user selects edit information from the drop down menu
    private  void editPersonalInformation(){
        //need to send user type so that the user can be located in the database
        Bundle bundle = new Bundle();
        Intent intent = new Intent(RestaurantMainMenu.this, EditAccountActivity.class);
        bundle.putSerializable("User", user);
        intent.putExtras(bundle);
        intent.putExtra("UserType", "Restaurant");
        startActivity(intent);
    }

    //called when user selects edit password from the drop down menu
    private void editPassword(){
        //need to send user type so that the user can be located in the database
        Bundle bundle = new Bundle();
        Intent intent = new Intent(RestaurantMainMenu.this, EditPasswordActivity.class);
        bundle.putSerializable("User", user);
        intent.putExtras(bundle);
        intent.putExtra("UserType", "Restaurant");
        startActivity(intent);
    }

    //called when user selects manage restaurants from the drop down menu
    //for now this goes to main menu until I get the name of the activity - look on git hub?
    private void goToManageRestaurants(){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(RestaurantMainMenu.this, ManageRestaurants.class);
        bundle.putSerializable("User", user);
        intent.putExtras(bundle);
        startActivity(intent);
    }

   private void retrieveData() {
        //find all the orders where the restaurantid matches the current user id
        //Order table: userID -> order# -> order entity

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order").child(user.getAuthUserID());
        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot orderSnapshot) {
                        List<Order> orderEntities = new ArrayList<Order>();
                        List<Order> orderAssigned = new ArrayList<Order>();
                        //Toast.makeText(context, "this is the list the user id pulls up: " + orderSnapshot.getKey(), Toast.LENGTH_LONG).show();
                        for(DataSnapshot orders: orderSnapshot.getChildren()){
                            //Toast.makeText(context, "outer loop: " + orders.getKey(), Toast.LENGTH_LONG).show();//this gives me the order id

                            Order order = orders.getValue(Order.class);

                            if (order.getStatus() != OrderEnum.COMPLETE
                                    && order.getStatus() != OrderEnum.REFUNDED) {
                                //add the children to the adapter list
                                if (orders.child("Assigned").exists()) {
                                    orderAssigned.add(order);
                                } else {
                                    orderEntities.add(order);
                                }
                            }
                        }

                        //if (orderEntities.isEmpty() && orderAssigned.isEmpty()){
                            //Toast.makeText(getApplicationContext(), "There are no orders on file.", Toast.LENGTH_LONG).show();
                        //}

                        listAdapter = new RestaurantMainMenuExpandableListAdapter(
                                (Activity) context, orderEntities, buildExpandableChildData(orderEntities), user);
                        listAdapterAssigned = new RestaurantMainMenuExpandableListAdapter(
                                (Activity) context, orderAssigned, buildExpandableChildData(orderAssigned), user);
                        orderList.setAdapter(listAdapter);
                        assignedOrderList.setAdapter(listAdapterAssigned);
                    }//end on data change

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                }
        );
    }

    /**
     * buildExpandableChildData builds the data that is associated with the expandable child entries
     * @param entities indicates a list of restaurants returned by the retrieveData method
     * @return Map of the expandable child data
     */
    private Map<String, List<Order>> buildExpandableChildData(List<Order> entities) {
        HashMap<String, List<Order>> childData = new HashMap<>();
        for (Order entity : entities) {
            childData.put(entity.getKey(), Arrays.asList(entity));
        }

        return childData;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        retrieveData();
    }

    @Subscribe
    public void onEvent(ActivityEvent event) {
        if (event.result == ActivityEvent.Result.REFRESH_RESTAURANT_MAIN_MENU) {
            retrieveData();
        }
    }

}//end Activity