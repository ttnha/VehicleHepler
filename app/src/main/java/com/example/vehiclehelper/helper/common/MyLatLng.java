package com.example.vehiclehelper.helper.common;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

public class MyLatLng {
    public double lat;
    public double lng;

    @Exclude
    public LatLng toLatLng() {
        return new LatLng(this.lat, this.lng);
    }
}
