package com.gmail.dleemcewen.tandemfieri;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gmail.dleemcewen.tandemfieri.Constants.AddressConstants;
import com.gmail.dleemcewen.tandemfieri.Interfaces.AsyncHttpResponse;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.gmail.dleemcewen.tandemfieri.RestClient.AddressToLatLng;
import com.gmail.dleemcewen.tandemfieri.Utility.FetchAddressIntentService;
import com.gmail.dleemcewen.tandemfieri.Utility.MapUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import static com.gmail.dleemcewen.tandemfieri.R.id.phone;
import static com.gmail.dleemcewen.tandemfieri.Validator.Validator.isValid;

public class CreateAccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public BootstrapButton nextButton, myLocation;
    public EditText firstName, lastName, address, city, zip, phoneNumber, email;

    protected Location location;

    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient googleApiClient;
    private String state = "";
    private Spinner states;
    private static CreateAccountActivity activityInstance;
    private boolean verifiedAddr;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        activityInstance = this;

        nextButton = (BootstrapButton) findViewById(R.id.nextButton);
        myLocation = (BootstrapButton) findViewById(R.id.location_button);
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        address = (EditText) findViewById(R.id.address);
        city = (EditText) findViewById(R.id.city);
        states = (Spinner) findViewById(R.id.state);
        zip = (EditText) findViewById(R.id.zip);
        phoneNumber = (EditText) findViewById(phone);
        email = (EditText) findViewById(R.id.email);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext()
                        , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(CreateAccountActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }

                if (MapUtil.isLocationEnabled(getApplicationContext())) {
                    try {
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                        if (googleApiClient.isConnected() && location != null) {
                            if (mResultReceiver == null)
                                mResultReceiver = new AddressResultReceiver(new Handler());
                            Intent addressIntent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
                            addressIntent.putExtra(AddressConstants.RECEIVER, mResultReceiver);
                            addressIntent.putExtra(AddressConstants.LOCATION_DATA_EXTRA, location);
                            startService(addressIntent);
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable location service.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (formValid()) {
                    String url = MapUtil.addressToURL(getApplicationContext()
                            , address.getText().toString()
                            , city.getText().toString()
                            , state
                            , zip.getText().toString());

                    AddressToLatLng client = AddressToLatLng.getInstance();
                    client.verifyAddress(getApplicationContext(), url, new AsyncHttpResponse() {
                        @Override
                        public void requestComplete(boolean success, AddressGeocode addr) {
                            if (success) {
                                Intent intent = new Intent(CreateAccountActivity.this, AlmostDoneActivity.class);

                                intent.putExtra("firstName", firstName.getText().toString());
                                intent.putExtra("lastName", lastName.getText().toString());
                                intent.putExtra("address", address.getText().toString());
                                intent.putExtra("city", city.getText().toString());
                                intent.putExtra("state", state);
                                intent.putExtra("zip", zip.getText().toString());
                                intent.putExtra("phoneNumber", phoneNumber.getText().toString());
                                intent.putExtra("email", email.getText().toString());

                                startActivity(intent);
                            } else {
                                address.setError(FormConstants.ERROR_TAG_ADDRESS);
                                city.setError(FormConstants.ERROR_TAG_CITY);
                                zip.setError(FormConstants.ERROR_TAG_ZIP);

                                Toast.makeText(getApplicationContext(), "Not a valid address!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        states.setOnItemSelectedListener(activityInstance);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        states.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public static CreateAccountActivity getInstance() {
        return activityInstance;
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        state = (String) parent.getItemAtPosition(position);
    }

    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            final Address addressOutput = resultData.getParcelable(AddressConstants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                Toast.makeText(getApplicationContext(), "Couldn't Retrieve Location.", Toast.LENGTH_SHORT).show();
            }

            String[] statesArray = getResources().getStringArray(R.array.states);
            address.setText(addressOutput.getAddressLine(0));
            for (int i = 0; i < statesArray.length; i++) {
                if (addressOutput.getAdminArea().trim().equals(statesArray[i]))
                    states.setSelection(i);
            }
            city.setText(addressOutput.getLocality());
            zip.setText(addressOutput.getPostalCode());
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //not implemented
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    } catch (SecurityException se) {
                        se.printStackTrace();
                    }
                } else {
                }
                verifiedAddr = MapUtil.verifyAddress(getApplicationContext(), address, city, state, zip);
                return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public boolean formValid() {
        return isValid(firstName, FormConstants.REG_EX_FIRSTNAME, FormConstants.ERROR_TAG_FIRSTNAME)
                && isValid(lastName, FormConstants.REG_EX_LASTNAME, FormConstants.ERROR_TAG_LASTNAME)
                && isValid(address, FormConstants.REG_EX_ADDRESS, FormConstants.ERROR_TAG_ADDRESS)
                && isValid(city, FormConstants.REG_EX_CITY, FormConstants.ERROR_TAG_CITY)
                && isValid(email, FormConstants.REG_EX_EMAIL, FormConstants.ERROR_TAG_EMAIL)
                && isValid(phoneNumber, FormConstants.REG_EX_PHONE, FormConstants.ERROR_TAG_PHONE)
                && isValid(zip, FormConstants.REG_EX_ZIP, FormConstants.ERROR_TAG_ZIP);
    }
}