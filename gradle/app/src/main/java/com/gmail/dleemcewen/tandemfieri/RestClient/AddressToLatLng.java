package com.gmail.dleemcewen.tandemfieri.RestClient;

import android.content.Context;

import com.gmail.dleemcewen.tandemfieri.Interfaces.AsyncHttpResponse;
import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AddressToLatLng {
    private static AddressToLatLng instance;

    private AsyncHttpClient client;

    public AddressToLatLng() {
        client = new AsyncHttpClient();
    }

    public static AddressToLatLng getInstance() {
        if (instance == null) {
            instance = new AddressToLatLng();
        }

        return instance;
    }

    public void verifyAddress(Context context, String url, final AsyncHttpResponse response) {
        client.get(context, url
            ,new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject resp) {
                        String json = resp.toString();

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        AddressGeocode address = gson.fromJson(json, AddressGeocode.class);
                        if (address.status.equals("OK")) {
                            response.requestComplete(true, address);
                        } else {
                            response.requestComplete(false, null);
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String res, Throwable t) {
                        response.requestComplete(false, null);
                    }
        });
    }
}