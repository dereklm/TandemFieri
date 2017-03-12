package com.gmail.dleemcewen.tandemfieri.Interfaces;

import com.gmail.dleemcewen.tandemfieri.Json.AddressGeocode.AddressGeocode;

public interface AsyncHttpResponse {
    void requestComplete(boolean success, AddressGeocode address);
}