package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;

import java.text.NumberFormat;

public class CartActivity extends AppCompatActivity {

    private Order order;
    private Button cancelButton, checkoutButton;
    private ExpandableListView cartItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView total, subtotal, tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        order = (Order) getIntent().getSerializableExtra("cart");

        total = (TextView) findViewById(R.id.total);
        subtotal = (TextView) findViewById(R.id.subTotal);
        tax = (TextView) findViewById(R.id.tax);
        cancelButton = (Button) findViewById(R.id.cancel_purchase);
        checkoutButton = (Button) findViewById(R.id.checkout);
        cartItems = (ExpandableListView) findViewById(R.id.cart_items);
        orderItemAdapter = new OrderItemAdapter(CartActivity.this, this, order.getItems());

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        total.setText("Total: " + formatter.format(order.getTotal()));
        tax.setText("Tax: " + formatter.format(order.getTax()));
        subtotal.setText("Subtotal: " + formatter.format(order.getSubTotal()));


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Brandon, this is where you will hook into brain tree.
                finish();
            }
        });

        cartItems.setAdapter(orderItemAdapter);
    }
}
