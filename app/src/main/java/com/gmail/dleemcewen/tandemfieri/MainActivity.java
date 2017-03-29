package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Entities.NotificationMessage;
import com.gmail.dleemcewen.tandemfieri.Entities.Restaurant;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Filters.SubscriberFilter;
import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;
import com.gmail.dleemcewen.tandemfieri.Json.Braintree.ClientToken;
import com.gmail.dleemcewen.tandemfieri.Json.Braintree.CreateCustomer;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.gmail.dleemcewen.tandemfieri.Logging.ToastLogger;
import com.gmail.dleemcewen.tandemfieri.Publishers.NotificationPublisher;
import com.gmail.dleemcewen.tandemfieri.Repositories.Restaurants;
import com.gmail.dleemcewen.tandemfieri.Services.NotificationService;
import com.gmail.dleemcewen.tandemfieri.Subscribers.DinerSubscriber;
import com.gmail.dleemcewen.tandemfieri.Subscribers.DriverSubscriber;
import com.gmail.dleemcewen.tandemfieri.Subscribers.RestaurantSubscriber;
import com.gmail.dleemcewen.tandemfieri.Tasks.TaskResult;
import com.gmail.dleemcewen.tandemfieri.Utility.BraintreeUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public TextView createAccount;
    public EditText email, password;
    public Button signInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authenticatorListener;
    private DatabaseReference dBase;
    private User user;
    private Resources resources;
    private Restaurants<Restaurant> restaurantsRepository;
    private DatabaseReference dataContext;
    private boolean sendNotificationMessages = false;
    private ChildEventListener notificationChildEventListener;

    private boolean verifiedEmailNotRequiredForLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Clear token so we can request an updated token
        BraintreeUtil.setClientToken(this, "");

        resources = getResources();
        setupLogging();
        registerNotificationListener();
        verifiedEmailNotRequiredForLogin = resources.getBoolean(R.bool.verified_email_not_required_for_login);
        //this if statement is used when the user clicks the sign out option from the drop down menu
        //it closed all open activities and then the main activity.
//        if( getIntent().getBooleanExtra("Exit me", false)){
//            finish();
//            return; // add this to prevent from doing unnecessary stuffs
//        }

        createAccount = (TextView) findViewById(R.id.createAccount);
        signInButton = (Button) findViewById(R.id.signInButton);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        dBase = FirebaseDatabase.getInstance().getReference().child("User");
        restaurantsRepository = new Restaurants<>(MainActivity.this);

        user = new User();
        mAuth = FirebaseAuth.getInstance();
        authenticatorListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    LogWriter.log(getApplicationContext(), Level.FINEST, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    LogWriter.log(getApplicationContext(), Level.FINEST, "onAuthStateChanged:signed_out");
                }
            }
        };

        createAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (verifiedEmailNotRequiredForLogin || task.getResult().getUser().isEmailVerified()) {
                                Toast.makeText(getApplicationContext(), task.getResult().getUser().getEmail() + " was successfully signed in", Toast.LENGTH_LONG)
                                      .show();

                                dBase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        navigateToMenu(dataSnapshot);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "That user is not verified, check email for verification link.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        } else {
                            Toast
                                    .makeText(getApplicationContext(), "Sign in was not successful. Check login details please.", Toast.LENGTH_LONG)
                                    .show();
                        }//end if task.successful
                    }//end onComplete
                });//end sign in


            }//end on click
        });//end sign in button
    }//end onCreate

    private void createBraintreeCustomer(User diner) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("clientID", diner.getAuthUserID());

        String url = getString(R.string.braintreeBaseURL) + "createcustomer";

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

                        CreateCustomer customer = gson.fromJson(json, CreateCustomer.class);

                        if (customer.success.equals("true")) {
                            requestBraintreeClientToken(customer.customerID);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("User")
                                    .child("Diner")
                                    .child(customer.clientID)
                                    .child("braintreeId")
                                    .setValue(customer.customerID);

                            LogWriter.log(getApplicationContext(), Level.FINEST, "CreateCustomerCustomerID: " + customer.customerID);
                        }

                        LogWriter.log(getApplicationContext(), Level.FINEST, "CreateCustomerResult: " + customer.success);
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        LogWriter.log(getApplicationContext(), Level.WARNING, "BraintreeCreateCustomerFailed: " + t.getMessage());
                    }
                });
        //End rest api
    }

    private void requestBraintreeClientToken(String customerID) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("customerID", customerID);

        String url = getString(R.string.braintreeBaseURL) + "generatetoken";

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

                        ClientToken token = gson.fromJson(json, ClientToken.class);

                        if (token.status.equals("true")) {
                            Log.v("BRAINTREE DEBUG", "TOKEN: " + token.token);  //REMOVE ME, TESTING ONLY
                            Log.v("BRAINTREE DEBUG", "CUST ID: " + token.customerID);  //REMOVE ME, TESTING ONLY
                            BraintreeUtil.setClientToken(getApplicationContext(), token.token);

                            LogWriter.log(getApplicationContext(), Level.FINEST, "BraintreeClientToken: " + token.token);
                        } else {
                            LogWriter.log(getApplicationContext(), Level.WARNING, "BraintreeClientTokenRequestFailed: customer does not exist");
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        LogWriter.log(getApplicationContext(), Level.WARNING, "BraintreeClientTokenRequestFailed: " + t.getMessage());
                    }
                });
        //End rest api
    }

    public void navigateToMenu(DataSnapshot dataSnapshot) {

        Bundle bundle = new Bundle();
                           
        User diner = dataSnapshot.child("Diner").child(mAuth.getCurrentUser().getUid()).getValue(User.class);
        User driver = dataSnapshot.child("Driver").child(mAuth.getCurrentUser().getUid()).getValue(User.class);
        final User restaurantOwner = dataSnapshot.child("Restaurant").child(mAuth.getCurrentUser().getUid()).getValue(User.class);

        Intent intent = null;

        if(diner != null){
            List<Object> customerIds = new ArrayList<>();
            customerIds.add(diner.getAuthUserID());

            List<Object> orderStatuses = new ArrayList<>();
            orderStatuses.add(OrderEnum.COMPLETE.toString());

            List<SubscriberFilter> subscriberFilters = new ArrayList<>();
            subscriberFilters.add(new SubscriberFilter("customerId", customerIds));
            subscriberFilters.add(new SubscriberFilter("status", orderStatuses));

            //register new diner subscriber
            registerNewSubscriber(new DinerSubscriber(
                    MainActivity.this,
                    diner,
                    subscriberFilters));

            intent = new Intent(MainActivity.this, DinerMainMenu.class);
            bundle.putSerializable("User", diner);

            if (diner.getBraintreeId() == null || diner.getBraintreeId().equals("")) {
                createBraintreeCustomer(diner);
            } else {
                Log.v("BRAINTREE DEBUG", "CUS ID FROM MENU: " + diner.getBraintreeId());  //REMOVE ME, TESTING ONLY
                requestBraintreeClientToken(diner.getBraintreeId());
            }

            intent.putExtras(bundle);
            startActivity(intent);
        }else if(driver != null){
            List<Object> restaurantIds = new ArrayList<>();
            restaurantIds.add(driver.getRestaurantId());

            List<Object> orderStatuses = new ArrayList<>();
            orderStatuses.add(OrderEnum.EN_ROUTE.toString());

            List<SubscriberFilter> subscriberFilters = new ArrayList<>();
            subscriberFilters.add(new SubscriberFilter("restaurantId", restaurantIds));
            subscriberFilters.add(new SubscriberFilter("status", orderStatuses));

            //register new driver subscriber
            registerNewSubscriber(new DriverSubscriber(
                    MainActivity.this,
                    driver,
                    subscriberFilters));

            intent = new Intent(MainActivity.this, DriverMainMenu.class);
            bundle.putSerializable("User", driver);
            intent.putExtras(bundle);
            startActivity(intent);
        }else if(restaurantOwner != null){
            //Find all the restaurant ids for the restaurant owner
            //to be able to associate them with the notifications
            restaurantsRepository
                .find("ownerId = '" + mAuth.getCurrentUser().getUid() + "'")
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<TaskResult<Restaurant>>() {
                    @Override
                    public void onComplete(@NonNull Task<TaskResult<Restaurant>> task) {
                        List<Restaurant> restaurants = task.getResult().getResults();

                        List<Object> restaurantIds = new ArrayList<>();
                        for (Restaurant ownerRestaurant : restaurants) {
                            restaurantIds.add(ownerRestaurant.getKey());
                        }

                        List<Object> orderStatuses = new ArrayList<>();
                        orderStatuses.add(OrderEnum.CREATING.toString());

                        List<SubscriberFilter> subscriberFilters = new ArrayList<>();
                        subscriberFilters.add(new SubscriberFilter("restaurantId", restaurantIds));
                        subscriberFilters.add(new SubscriberFilter("status", orderStatuses));


                        //register new restaurant subscriber
                        registerNewSubscriber(new RestaurantSubscriber(
                                MainActivity.this,
                                restaurantOwner,
                                subscriberFilters));

                        Intent restaurantIntent = new Intent(MainActivity.this, RestaurantMainMenu.class);
                        Bundle restaurantBundle = new Bundle();
                        restaurantBundle.putSerializable("User", restaurantOwner);
                        restaurantIntent.putExtras(restaurantBundle);
                        startActivity(restaurantIntent);
                    }
                });
        }
    }

    private void setupLogging() {
        LogWriter.setMinimumLoggingLevel(resources.getString(R.string.minimum_logging_level));
        LogWriter.addLogger(new ToastLogger());
    }

    private void registerNewSubscriber(ISubscriber subscriber) {
        NotificationPublisher notificationPublisher = NotificationPublisher.getInstance();
        notificationPublisher.subscribe(subscriber);
    }

    private void resetSubscribers() {
        NotificationPublisher notificationPublisher = NotificationPublisher.getInstance();
        List<ISubscriber> subscribers = notificationPublisher.getSubscribers();

        for (ISubscriber subscriber : subscribers) {
            notificationPublisher.unsubscribe(subscriber);
        }
    }

    private void registerNotificationListener() {
        dataContext = FirebaseDatabase.getInstance().getReference(NotificationMessage.class.getSimpleName());

        notificationChildEventListener = dataContext.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (sendNotificationMessages) {
                    NotificationMessage childNotificationMessageRecord = dataSnapshot.getValue(NotificationMessage.class);
                    childNotificationMessageRecord.setKey(dataSnapshot.getKey());

                    sendNotificationMessage(childNotificationMessageRecord);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //not implemented
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //not implemented
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //not implemented
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //not implemented
            }
        });
        dataContext.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sendNotificationMessages = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * sendNotificationMessage sends a notification message to the notification service that
     * contains the information in the provided NotificationMessage record
     * @param childNotificationMessageRecord indicates the notificationmessage record which contains
     *                                       the data to send to the notification service
     */
    private void sendNotificationMessage(NotificationMessage childNotificationMessageRecord) {
        Intent intent = new Intent(MainActivity.this, NotificationService.class);
        intent.setAction(childNotificationMessageRecord.getAction());
        intent.putExtra("notificationId", childNotificationMessageRecord.getNotificationId());
        intent.putExtra("notificationType", childNotificationMessageRecord.getNotificationType());
        intent.putExtra("entity", (Serializable) childNotificationMessageRecord.getData());
        intent.putExtra("key", childNotificationMessageRecord.getKey());
        intent.putExtra("userId", childNotificationMessageRecord.getUserId());
        MainActivity.this.startService(intent);
    }

    private void clear(){
        email.setText("");
        password.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authenticatorListener);
        resetSubscribers();
    }

    @Override
    protected void onStop() {
        mAuth.removeAuthStateListener(authenticatorListener);
        super.onStop();
    }

    @Override
    protected void onPause(){
        mAuth.removeAuthStateListener(authenticatorListener);
        clear();
        super.onPause();

    }

    @Override
    protected void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(authenticatorListener);
    }
}//end activity