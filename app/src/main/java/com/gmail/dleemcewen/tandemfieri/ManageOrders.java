package com.gmail.dleemcewen.tandemfieri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ManageOrders extends AppCompatActivity {
    public DatabaseReference mDatabaseOrders, mDatabaseDrivers, mDatabaseSendToDrivers;
    public String ID, restID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        /*
        Some kind of on click listener will be used to send the order to driver for the driver to get it use
        mDatabase.child("Delivery").child(driverID).child("Order").setValue(order);

         */
        ID = getIntent().getStringExtra("ID");
        restID = getIntent().getStringExtra("restId");

        mDatabaseOrders = FirebaseDatabase.getInstance().getReference()
                .child("Order").child(ID).child(restID);


        mDatabaseDrivers = FirebaseDatabase.getInstance().getReference()
                .child("User").child("Driver");

        /*
        Get the order information and display it however you want to
         */
        mDatabaseOrders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Toast.makeText(getApplicationContext(), child.child("Test").getValue().toString(), Toast.LENGTH_LONG).show();
                    //currentOrder.setText(child.child("subTotal").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*
        You need to get what drivers you have available and send them the order. To do that you need the drivers ID
         */
        mDatabaseDrivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(restID.equals(""+child.child("restaurantId").getValue())){
                        Toast.makeText(getApplicationContext(), child.child("authUserID").getValue().toString(), Toast.LENGTH_LONG).show();

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}