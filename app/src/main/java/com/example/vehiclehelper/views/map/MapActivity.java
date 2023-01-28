package com.example.vehiclehelper.views.map;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView tv_address;
    private Button btn_done;
    private final Activity activity = this;
    private GoogleMap mMap;
    private Geocoder geocoder;

    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getWidgets();
        setListeners();

        geocoder = new Geocoder(activity, Locale.getDefault());

        initMap();
    }

    private void getWidgets() {
        tv_address = findViewById(R.id.tv_address);
        btn_done = findViewById(R.id.btn_done);
    }

    private void setListeners() {
        btn_done.setOnClickListener(v -> {
            Intent intent = new Intent();
            String rsStr = currentLocation.latitude + "|" + currentLocation.longitude + "|" + tv_address.getText();
            intent.putExtra("result", rsStr);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void moveCameraAnimation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    public void back(View view) {
        finish();
    }

    Marker marker;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(() -> {
            currentLocation = mMap.getCameraPosition().target;
            tv_address.setText(getAddressNameFromLatLng(currentLocation));
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(currentLocation));
        });

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            currentLocation = latLng;
            tv_address.setText(getAddressNameFromLatLng(currentLocation));
            marker = mMap.addMarker(new MarkerOptions().position(currentLocation));
        });

        currentLocation = SessionManager.getINSTANCE().getLocationSession();
        if (currentLocation == null) {
            currentLocation = new LatLng(10.762622, 106.660172);
        }
        marker = mMap.addMarker(new MarkerOptions().position(currentLocation));

        tv_address.setText(getAddressNameFromLatLng(currentLocation));

        moveCameraAnimation(currentLocation);

    }

    private String getAddressNameFromLatLng(LatLng latLng) {
        String name = "Unknown";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
//                System.out.println(address.toString());
                name = address.getAddressLine(0);
            }
        } catch (Exception ignored) {

        }
        return name;
    }
}