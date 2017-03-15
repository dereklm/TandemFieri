package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DinerOrderHistoryActivity extends AppCompatActivity {
    User user;
    List<Order> ordersList;
    DatabaseReference mDatabase;
    ListView orderHistoryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_order_history);

        orderHistoryView = (ListView)findViewById(R.id.diner_order_history_listview);

        initialize();
        retrieveData();

    }//end on create

    private void initialize(){
        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        ordersList = new ArrayList<>();
        orderHistoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openOrder((Order) parent.getItemAtPosition(position));
            }
        });
    }

    private void openOrder(Order order){
        LogWriter.log(getApplicationContext(), Level.INFO, "This will open the Order");
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
                for (DataSnapshot child1 : dataSnapshot.getChildren()) {
                    for(DataSnapshot child2 : child1.getChildren()){
                        for(DataSnapshot child : child2.getChildren()){
                            Order o = child.getValue(Order.class);
                            if(o.getCustomerId().equals(user.getAuthUserID())) {
                                ordersList.add(o);
                                LogWriter.log(getApplicationContext(), Level.INFO, o.toString());
                            }
                        }

                    }
                }
                if(ordersList.isEmpty()){
                    Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
                }else {

                    ArrayAdapter<Order> adapter = new ArrayAdapter<>(
                            getApplicationContext(),
                            R.layout.diner_order_history_item,
                            ordersList);
                    orderHistoryView.setAdapter(adapter);
                    }

            }//end on data change

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });//end listener
    }//end retrieve data
}//end activity
