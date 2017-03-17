package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;

import static com.paypal.android.sdk.onetouch.core.metadata.ah.o;

public class CartActivity extends AppCompatActivity {

    private Order order;
    private Button cancelButton, checkoutButton;
    private double deliveryCharge;
    private ExpandableListView cartItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView delivery, total, subtotal, tax;
    private DatabaseReference mDatabase;
    private String uid = "", ownerId = "", restaurantId = "", restName = "", orderId;
    private FirebaseUser fireuser;
    private NotificationMessages<NotificationMessage> notificationsRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        order = (Order) getIntent().getSerializableExtra("cart");
        restaurantId = (String) getIntent().getSerializableExtra("restaurantId");
        ownerId = (String) getIntent().getSerializableExtra("ownerId");
        deliveryCharge = (double) getIntent().getSerializableExtra("deliveryCharge");
        restName = (String) getIntent().getSerializableExtra("restaurantName");
        notificationsRepository = new NotificationMessages<>(CartActivity.this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fireuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fireuser != null) {
            // User is signed in
            uid = fireuser.getUid();
        } else {
            // No user is signed in
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Exit me", true);
            startActivity(intent);
            finish();
        }

        total = (TextView) findViewById(R.id.total);
        subtotal = (TextView) findViewById(R.id.subTotal);
        tax = (TextView) findViewById(R.id.tax);
        delivery = (TextView) findViewById(R.id.delivery_charge);
        cancelButton = (Button) findViewById(R.id.cancel_purchase);
        checkoutButton = (Button) findViewById(R.id.checkout);
        cartItems = (ExpandableListView) findViewById(R.id.cart_items);
        orderItemAdapter = new OrderItemAdapter(CartActivity.this, this, order.getItems());

        order.setDeliveryCharge(deliveryCharge);
        order.updateTotals();
        updateTextViews();

        order.getKey();
        order.setOrderId(order.getKey());
        order.setCustomerId(uid);
        order.setRestaurantId(restaurantId);
        order.setRestaurantName(restName);

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
                if (order.getItems().size() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "Please add items to your order.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                mDatabase.child("Order").child(ownerId).child(order.getKey()).setValue(order);



                //sent order notification to restaurant
                notificationsRepository
                    .sendNotification(NotificationConstants.Action.ADDED, order)
                    .addOnCompleteListener(CartActivity.this, new OnCompleteListener<TaskResult<Order>>() {
                        @Override
                        public void onComplete(@NonNull Task<TaskResult<Order>> task) {
                            if (task.isSuccessful()) {
                                Toast
                                    .makeText(CartActivity.this, "Your order has been placed successfully.", Toast.LENGTH_LONG)
                                    .show();
                            } else {
                                Toast
                                    .makeText(CartActivity.this, "An error occurred while placing your order.  Please verify the status of your order with the restaurant.", Toast.LENGTH_LONG)
                                    .show();
                            }
                        }
                    });

                //enable delivery map option in main menu
                enableDeliveryMap();

                finish();
            }
        });

        cartItems.setAdapter(orderItemAdapter);

        cartItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    OrderItem item = (OrderItem) orderItemAdapter.getGroup(groupPosition);
                    order.removeItem(item);
                    order.updateTotals();
                    updateTextViews();
                    orderItemAdapter.notifyDataSetChanged();
                    return true;
                }

                return false;
            }
        });
    }

    private void enableDeliveryMap(){
        MenuItem item = DinerMainMenu.getDeliveryMenuItem();
        item.setVisible(true);
    }

    private void updateTextViews() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        total.setText("Total: " + formatter.format(order.getTotal()));
        tax.setText("Tax: " + formatter.format(order.getTax()));
        subtotal.setText("Subtotal: " + formatter.format(order.getSubTotal()));
        delivery.setText("Delivery: " + formatter.format(deliveryCharge));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            order = (Order) savedInstanceState.getSerializable("previousItems");
            Toast.makeText(getApplicationContext(), "restored", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "restored but null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO: figure out how to save order when going back to menu.
        super.onBackPressed();
    }
}