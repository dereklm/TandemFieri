package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.gmail.dleemcewen.tandemfieri.Adapters.OrderItemAdapter;
import com.gmail.dleemcewen.tandemfieri.Constants.NotificationConstants;
import com.gmail.dleemcewen.tandemfieri.Entities.Nonce;
import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItem;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOption;
import com.gmail.dleemcewen.tandemfieri.Entities.OrderItemOptionGroup;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Json.Braintree.Checkout;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.NotificationMessages;
import com.gmail.dleemcewen.tandemfieri.Utility.BraintreeUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;

import cz.msebera.android.httpclient.Header;

public class CartActivity extends AppCompatActivity {

    private Order order;
    private BootstrapButton cancelButton, checkoutButton, paymentMethodButton;
    private double deliveryCharge;
    private ExpandableListView cartItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView delivery, total, subtotal, tax;
    private DatabaseReference mDatabase;
    private String uid = "", ownerId = "", restaurantId = "", restName = "", orderId, braintreeID;
    private FirebaseUser fireuser;
    private NotificationMessages<NotificationMessage> notificationsRepository;
    private String latitude, longitude;
    private Nonce nonce;
    private View mView;
    private ProgressDialog mDialog;
    private Context mContext;

    private static final int PAYMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        mView = findViewById(R.id.activity_cart);
        order = (Order) getIntent().getSerializableExtra("cart");
        restaurantId = (String) getIntent().getSerializableExtra("restaurantId");
        ownerId = (String) getIntent().getSerializableExtra("ownerId");
        deliveryCharge = (double) getIntent().getSerializableExtra("deliveryCharge");
        restName = (String) getIntent().getSerializableExtra("restaurantName");
        latitude = getIntent().getStringExtra("Latitude");
        longitude = getIntent().getStringExtra("Longitude");
        braintreeID = getIntent().getStringExtra("braintreeID");
        notificationsRepository = new NotificationMessages<>(CartActivity.this);

        mContext = this;

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
        cancelButton = (BootstrapButton) findViewById(R.id.cancel_purchase);
        checkoutButton = (BootstrapButton) findViewById(R.id.checkout);
        paymentMethodButton = (BootstrapButton) findViewById(R.id.payment_method);
        cartItems = (ExpandableListView) findViewById(R.id.cart_items);
        setCorrectOptions();
        orderItemAdapter = new OrderItemAdapter(CartActivity.this, this, order.getItems());

        order.setDeliveryCharge(deliveryCharge);
        order.updateTotals();
        updateTextViews();

        order.getKey();
        order.setLatitude(latitude);
        order.setLongitude(longitude);
        order.setOrderId(order.getKey());
        order.setCustomerId(uid);
        order.setRestaurantId(restaurantId);
        order.setRestaurantName(restName);
        order.setStatus(OrderEnum.PAYMENT_PENDING);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        paymentMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                String token = BraintreeUtil.getClientToken(context);

                if (token == null || token.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Unable to connect to payment server. Try logging back in.",
                            Toast.LENGTH_LONG).show();
                } else {
                    DropInRequest request = new DropInRequest().clientToken(token);

                    startActivityForResult(request.getIntent(context), PAYMENT);
                }
            }
        });

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.getItems().size() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "Please add items to your order.",
                            Toast.LENGTH_LONG).show();

                    finish();
                }

                if (nonce == null || nonce.getNonce().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please select a payment method.",
                            Toast.LENGTH_LONG).show();
                } else {
                    completeOrderDialog();
                }
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

    private void completeOrderDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle(getString(R.string.submit_payment))
                .setMessage(getString(R.string.confirm_checkout))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                                mDialog = new ProgressDialog(mContext);
                                mDialog.setMessage("Submitting payment!");
                                mDialog.setCancelable(false);
                                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mDialog.show();

                                submitPayment();
                            }
                        })
                .setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void submitPayment() {
        mDatabase.child("Order").child(ownerId).child(order.getKey()).setValue(order);

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        formatter.setDecimalFormatSymbols(symbols);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        Log.v("BRAINTREE DEBUG", "Payment - Cust ID: " + braintreeID);  //REMOVE ME, TESTING ONLY

        params.put("customerID", braintreeID);
        params.put("amount", formatter.format(order.getTotal()));
        params.put("payment_method_nonce", nonce.getNonce());
        params.put("orderID", order.getKey());

        String url = getString(R.string.braintreeBaseURL) + "Checkout";

        //Start rest api
        client.post(this
                ,url
                ,params
                ,new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject resp) {
                        String json = resp.toString();

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        Checkout checkout = gson.fromJson(json, Checkout.class);

                        if (checkout.success.equals("true")) {
                            order.setBraintreeTransactionId(checkout.transactionID);
                            order.setStatus(OrderEnum.CREATING);
                            mDatabase.child("Order").child(ownerId).child(order.getKey()).setValue(order);

                            Toast.makeText(getApplicationContext(),
                                    "Order completed.",
                                    Toast.LENGTH_LONG).show();

                            notificationsRepository
                                    .sendNotification(NotificationConstants.Action.ADDED, order, ownerId);

                            enableDeliveryMap();
                            finish();
                        } else {
                            Log.v("Braintree", checkout.error);

                            Toast.makeText(getApplicationContext(),
                                    "Unable to complete order at this time.",
                                    Toast.LENGTH_LONG).show();
                        }

                        mDialog.hide();
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        mDialog.hide();

                        Toast.makeText(getApplicationContext(),
                                "Unable to complete order at this time.",
                                Toast.LENGTH_LONG).show();

                        LogWriter.log(getApplicationContext(), Level.WARNING, "CheckoutFailure: " + t.getMessage());
                    }
                });
        //End rest api
    }

    private void enableDeliveryMap(){
        MenuItem item = DinerMainMenu.getDeliveryMenuItem();
        item.setVisible(true);
    }

    private void updateTextViews() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        if (order.getItems().size() > 0) {
            total.setText("Total: " + formatter.format(order.getTotal()));
            tax.setText("Tax: " + formatter.format(order.getTax()));
            subtotal.setText("Subtotal: " + formatter.format(order.getSubTotal()));
            delivery.setText("Delivery: " + formatter.format(deliveryCharge));
        } else {
            total.setText("Total: " + formatter.format(0));
            tax.setText("Tax: " + formatter.format(0));
            subtotal.setText("Subtotal: " + formatter.format(0));
            delivery.setText("Delivery: " + formatter.format(0));
        }
    }

    private void setCorrectOptions() {
        for (OrderItem item : order.getItems()) {
            Iterator<OrderItemOptionGroup> groupIterator = item.getOptionGroups().iterator();
            while (groupIterator.hasNext()) {
                OrderItemOptionGroup group = groupIterator.next();

                // if option group has no child selected remove it.
                if (!group.isHasChildSelected()) {
                    groupIterator.remove();
                } else {
                    // group has options selected, if the options are false remove them.
                    Iterator<OrderItemOption> optionIterator = group.getOptions().iterator();
                    while (optionIterator.hasNext()) {
                        if (!optionIterator.next().isSelected()) optionIterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("order", order);
        setResult(Activity.RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                nonce = new Nonce(result.getPaymentMethodNonce().getNonce());

                Log.v("BRAINTREE DEBUG", "NONCE: " + nonce.getNonce());  //REMOVE ME, TESTING ONLY
                LogWriter.log(getApplicationContext(), Level.WARNING, "Nonce: " + result.getPaymentMethodNonce().getNonce());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LogWriter.log(getApplicationContext(), Level.WARNING, "Canceled");
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                LogWriter.log(getApplicationContext(), Level.WARNING, "Error: " + error.getMessage());
            }
        }
    }
}