package com.gmail.dleemcewen.tandemfieri.Utility;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class MapUtil {
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
}