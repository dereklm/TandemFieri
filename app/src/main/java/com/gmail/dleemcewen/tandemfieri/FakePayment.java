package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.gmail.dleemcewen.tandemfieri.Entities.Nonce;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Json.Braintree.Checkout;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.gmail.dleemcewen.tandemfieri.Utility.BraintreeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import cz.msebera.android.httpclient.Header;

public class FakePayment extends AppCompatActivity {
    private Button payment, submit;
    private Nonce nonce;
    private User diner;

    private static final int PAYMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_payment);

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        diner = (User) bundle.getSerializable("User");

        payment = (Button) findViewById(R.id.launchPayment);
        submit = (Button) findViewById(R.id.submitPayment);

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                String token = BraintreeUtil.getClientToken(context);

                Log.v("BRAINTREE DEBUG", "TOKEN PRE DROPIN: " + token);  //REMOVE ME, TESTING ONLY

                DropInRequest request = new DropInRequest().clientToken(token);

                startActivityForResult(request.getIntent(context), PAYMENT);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nonce == null || nonce.getNonce().equals("")) {
                    LogWriter.log(getApplicationContext(), Level.SEVERE, "No payment method selected.");
                } else {
                    submitPayment();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                nonce = new Nonce(result.getPaymentMethodNonce().getNonce());

                Log.v("BRAINTREE DEBUG", "NONCE: " + nonce.getNonce());  //REMOVE ME, TESTING ONLY
                LogWriter.log(getApplicationContext(), Level.WARNING, "Nounce: " + result.getPaymentMethodNonce().getNonce());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LogWriter.log(getApplicationContext(), Level.WARNING, "Canceled");
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                LogWriter.log(getApplicationContext(), Level.WARNING, "Error: " + error.getMessage());
            }
        }
    }

    public void submitPayment() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        Log.v("BRAINTREE DEBUG", "Payment - Cust ID: " + diner.getBraintreeId());  //REMOVE ME, TESTING ONLY

        params.put("customerID", diner.getBraintreeId());
        params.put("amount", "15.23");
        params.put("payment_method_nonce", nonce.getNonce());

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
                            LogWriter.log(getApplicationContext(), Level.WARNING, "Payment submitted.");
                        }

                        LogWriter.log(getApplicationContext(), Level.WARNING, checkout.success);
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        LogWriter.log(getApplicationContext(), Level.WARNING, "CheckoutFailure: " + t.getMessage());
                    }
                });
        //End rest api
    }

    public void updateUser() {
        Users<User> users = new Users<>(getApplicationContext());

        users.find(Arrays.asList("Diner", "authUserID"), diner.getAuthUserID(), new QueryCompleteListener<User>() {
            @Override
            public void onQueryComplete(ArrayList<User> entities) {

                for (User entity : entities) {
                    diner = entity;
                    LogWriter.log(getApplicationContext(), Level.WARNING, "Test: " + diner.getBraintreeId());
                }
            }
        });
    }
}