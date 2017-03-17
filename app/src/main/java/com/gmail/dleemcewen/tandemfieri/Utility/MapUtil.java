package com.gmail.dleemcewen.tandemfieri.Utility;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.EditText;

import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.gmail.dleemcewen.tandemfieri.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.SphericalUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MapUtil {
    private static boolean statusREST;

    public static String addressToURL(Context context, String street, String city, String state, String zip) {
        String url;

        url = context.getString(R.string.googleDecoderBaseURL)
                + "address="
                + street
                + ","
                + city
                + ","
                + state
                + ","
                + zip
                + "&" + context.getString(R.string.google_api_key);

        return url;
    }

    public static LatLngBounds calculateBounds(LatLng center, double radius) {
        return new LatLngBounds.Builder().
            include(SphericalUtil.computeOffset(center, radius, 0)).
            include(SphericalUtil.computeOffset(center, radius, 90)).
            include(SphericalUtil.computeOffset(center, radius, 180)).
            include(SphericalUtil.computeOffset(center, radius, 270)).build();
    }

    public static void moveCamera(GoogleMap map, LatLng center, double radius, int padding) {
        LatLngBounds bounds = calculateBounds(center, radius);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    public static boolean verifyAddress(Context context, EditText street, EditText city, String state, EditText zip) {
        String url;

        url = "address="
                + street.getText().toString()
                + ","
                + city.getText().toString()
                + ","
                + state
                + ","
                + zip.getText().toString()
                + "&" + context.getString(R.string.google_api_key);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(context
                , context.getString(R.string.googleDecoderBaseURL) + url
                , new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject resp) {
                        String json = resp.toString();

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        AddressGeocode address = gson.fromJson(json, AddressGeocode.class);

                        if (address.status.equals("OK")) {
                            statusREST = true;
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        statusREST = false;
                    }
                });

        return statusREST;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }
}