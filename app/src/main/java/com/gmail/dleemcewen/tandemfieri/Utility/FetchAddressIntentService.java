package com.gmail.dleemcewen.tandemfieri.Utility;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.gmail.dleemcewen.tandemfieri.Constants.AddressConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Derek on 2/26/2017.
 */

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        Location location = intent.getParcelableExtra(AddressConstants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(AddressConstants.RECEIVER);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                      location.getLatitude()
                    , location.getLongitude(), 1);
        } catch (IOException io) {
            io.printStackTrace();
        } catch (IllegalArgumentException arg) {
            arg.printStackTrace();
        }

        if (addresses == null || addresses.size() == 0) {
            deliverResultToReceiver(AddressConstants.FAILURE_RESULT, null);
        } else {
            Address address = addresses.get(0);

            deliverResultToReceiver(AddressConstants.SUCCESS_RESULT, address);
        }

    }

    private void deliverResultToReceiver(int resultCode, Address addr) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddressConstants.RESULT_DATA_KEY, addr);
        mReceiver.send(resultCode, bundle);
    }

}
