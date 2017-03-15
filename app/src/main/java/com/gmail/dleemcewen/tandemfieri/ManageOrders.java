package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import static com.paypal.android.sdk.onetouch.core.metadata.ah.d;


public class ManageOrders extends AppCompatActivity {
    public DatabaseReference mDatabaseOrders, mDatabaseDrivers, mDatabaseSendToDrivers;
    public String ID, restID;
    public Order order;
    private ListView driverListView;
    private List<User> driverList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        /*
        Some kind of on click listener will be used to send the order to driver for the driver to get it use
        mDatabase.child("Delivery").child(driverID).child("Order").setValue(order);

         */

        driverList = new ArrayList<>();
        driverListView = (ListView) findViewById(R.id.driver_listview);
        driverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                User user= (User) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Sent the order to"+user.getFirstName(), Toast.LENGTH_LONG).show();
                mDatabase.child("Delivery").child(user.getAuthUserID()).child("Order").setValue(order);
            }
        });

        Bundle bundle = this.getIntent().getExtras();
        order = (Order) bundle.getSerializable("Order");

        restID = order.getRestaurantId();


        mDatabaseDrivers = FirebaseDatabase.getInstance().getReference()
                .child("User").child("Driver");


        mDatabaseDrivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(restID.equals(""+child.child("restaurantId").getValue())){
                        Toast.makeText(getApplicationContext(), child.child("firstName").getValue().toString(), Toast.LENGTH_LONG).show();
                        User d = child.getValue(User.class);
                        driverList.add(d);

                    }

                    ArrayAdapter<User> adapter = new ArrayAdapter<>(
                            getApplicationContext(),
                            R.layout.diner_mainmenu_item_view,
                            driverList);

                    driverListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}