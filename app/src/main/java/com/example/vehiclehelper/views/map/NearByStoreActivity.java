package com.example.vehiclehelper.views.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store.model.Store;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.store.StoreInfoActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NearByStoreActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private final Activity activity = this;
    private GoogleMap mMap;

    private LinearLayout ln_pbar;
    private RelativeLayout rl_detail;
    private TextView tv_store_name, tv_address, tv_distance;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private StoreDAO storeDAO;
    private String currentStoreIDSelect;
    private SessionManager sessionManager;
    private boolean isFromSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_store);

        storeDAO = new StoreDAO();
        isFromSOS = getIntent().getBooleanExtra("fromSOS", false);
        getWidgets();
        setListeners();
        initMap();
    }

    private void getWidgets() {
        LinearLayout ll_bt_s = findViewById(R.id.ll_bt_s);
        bottomSheetBehavior = BottomSheetBehavior.from(ll_bt_s);

        rl_detail = findViewById(R.id.rl_detail);

        ln_pbar = findViewById(R.id.ln_pbar);

        tv_store_name = findViewById(R.id.tv_store_name);
        tv_address = findViewById(R.id.tv_address);
        tv_distance = findViewById(R.id.tv_distance);
    }

    private void setListeners() {
        rl_detail.setOnClickListener(v -> {
            Intent intent = new Intent(activity, StoreInfoActivity.class);
            intent.putExtra("from_customer", true);
            intent.putExtra("storeStr", new Gson().toJson(storeMap.get(currentStoreIDSelect)));
            startActivity(intent);
        });
    }

    private void moveCameraAnimation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
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

    Map<String, Store> storeMap = new HashMap<>();

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(marker -> Toast.makeText(activity, "VAI", Toast.LENGTH_SHORT).show());

        final LatLng currentLocation = (sessionManager = SessionManager.getINSTANCE()).getLocationSession();

        final Marker currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Vị trí của bạn").visible(true));
        if (!isFromSOS) {
            moveCameraAnimation(currentLocation);
            Objects.requireNonNull(currentMarker).showInfoWindow();
        }


        // getStoreList
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        storeDAO.getFullStore(currentLocation, 50D, new StoreDAO.IControlData() {
            @Override
            public void storeList(List<Store> storeList) {
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                if (!storeList.isEmpty()) {
                    for (int i = storeList.size() - 1; i >= 0; i--) {
                        Store store = storeList.get(i);
                        storeMap.put(store.getStoreID(), store);
                        final Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(store.getLatLng().toLatLng())
                                .title(store.getStoreName())
                                .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_user)));
                        assert marker != null;
                        marker.setTag(store.getStoreID());
                        if (i == 0 && isFromSOS) {
                            marker.showInfoWindow();
                        }
                    }
                }
                if (isFromSOS && !storeList.isEmpty()) {
                    // Move camera tới thằng gần nhất và show thông tin lên luôn
                    moveCameraAnimation(storeList.get(0).getLatLng().toLatLng());
                    processMarkerClick(storeList.get(0));
                } else if (storeList.isEmpty()) {
                    Objects.requireNonNull(currentMarker).showInfoWindow();
                    moveCameraAnimation(currentLocation);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.getTag() == null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        } else {
            String storeID = marker.getTag().toString();
            Store currentStoreSelect = storeMap.get(storeID);
            if (currentStoreSelect != null) {
                processMarkerClick(currentStoreSelect);
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void processMarkerClick(Store currentStoreSelect) {
        if (currentStoreSelect != null) {
            currentStoreIDSelect = currentStoreSelect.getStoreID();
            tv_store_name.setText("Cửa hàng: " + currentStoreSelect.getStoreName());
            tv_address.setText("Địa chỉ: " + currentStoreSelect.getAddressName());
            if (currentStoreSelect.distance >= 1) {
                tv_distance.setText(String.format(Locale.getDefault(), "Cách bạn: %.2f km", currentStoreSelect.distance));
            } else {
                tv_distance.setText(String.format(Locale.getDefault(), "Cách bạn:  %d m", (int) (currentStoreSelect.distance * 1000)));
            }
            sessionManager.upsertDistance((float) currentStoreSelect.distance);
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    public void onHideBottomSheet(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void back(View view) {
        finish();
    }
}