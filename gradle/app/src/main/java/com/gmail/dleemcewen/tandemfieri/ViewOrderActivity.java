package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;
import java.util.logging.Level;

public class ViewOrderActivity extends AppCompatActivity {
    Order order;
    TextView subTotal, tax, total, restaurantName, orderDate;
    ListView viewOrderItems;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        getHandles();
        initialize();

    }//end on Create

    private void getHandles(){
        subTotal = (TextView)findViewById(R.id.subTotal);
        tax = (TextView)findViewById(R.id.tax);
        total = (TextView)findViewById(R.id.total);
        restaurantName = (TextView)findViewById(R.id.restaurant_name);
        orderDate = (TextView)findViewById(R.id.date);
        viewOrderItems = (ListView)findViewById(R.id.cart_items);
    }

    private void initialize(){
        Bundle bundle = this.getIntent().getExtras();
        order = (Order) bundle.getSerializable("Order");
        if(order != null) {
            finishLayout();
        }
        LogWriter.log(getApplicationContext(), Level.INFO, order.getItems().get(0).toString());
        ArrayAdapter<OrderItem> adapter = new ArrayAdapter<OrderItem>(
                getApplicationContext(),
                R.layout.view_order_items,
                order.getItems());
        viewOrderItems.setAdapter(adapter);
    }

    private void finishLayout(){
        restaurantName.setText(order.getRestaurantName());
        subTotal.setText("Subtotal: $" + String.format(Locale.US, "%.2f", order.getSubTotal()));
        tax.setText("Tax: $" + String.format(Locale.US, "%.2f", order.getTax()));
        total.setText("Total: $" + String.format(Locale.US, "%.2f", order.getTotal()));
        orderDate.setText("Date of order: " + order.dateToString());
    }

}//end activity
