package com.gmail.dleemcewen.tandemfieri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.gmail.dleemcewen.tandemfieri.Entities.Nonce;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.EventListeners.QueryCompleteListener;
import com.gmail.dleemcewen.tandemfieri.Json.Braintree.Checkout;
import com.gmail.dleemcewen.tandemfieri.Repositories.Users;
import com.gmail.dleemcewen.tandemfieri.Utility.BraintreeUtil;
import com.gmail.dleemcewen.tandemfieri.Utility.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

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
                DropInRequest request = new DropInRequest().clientToken(token);

                startActivityForResult(request.getIntent(context), PAYMENT);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nonce == null || nonce.getNonce().equals("")) {
                    Util.JellyToast(getApplicationContext(), "No payment method selected.", Toast.LENGTH_LONG);
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

                Log.v("Braintree", "Nouce: " + result.getPaymentMethodNonce().getNonce());
                Util.JellyToast(getApplicationContext(), "Nounce: " + result.getPaymentMethodNonce().getNonce(), Toast.LENGTH_LONG);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.v("Braintree", "Canceled");
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    public void submitPayment() {
        //Log.v("Braintree", "UserID: " + diner.getAuthUserID());
        //Log.v("Braintree", "ID: " + diner.getBraintreeId());
        //updateUser();
        //Log.v("Braintree", "ID updated: " + diner.getBraintreeId());

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

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
                            Util.JellyToast(getApplicationContext(), "Payment submitted.", Toast.LENGTH_LONG);
                        }

                        //LogWriter.log(getApplicationContext(), Level.WARNING, "BraintreeClientToken: " + token);
                        Log.v("Braintree", "CheckoutResult: " + checkout.success);
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        //LogWriter.log(getApplicationContext(), Level.WARNING, "BraintreeClientTokenRequestFailed: " + t.getMessage());
                        Log.v("Braintree", "CheckoutFailure: " + t.getMessage());
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
                    Log.v("Braintree", "Test: " + diner.getBraintreeId());
                }
            }
        });
    }
}