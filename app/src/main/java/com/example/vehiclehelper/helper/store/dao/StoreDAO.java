package com.example.vehiclehelper.helper.store.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store.model.Store;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StoreDAO {
    private static final String TABLE_NAME = TableName.Store.name();
    private static final String STORE_OWNER = TableName.StoreOwner.name();

    public void getStoreIDByOwner(String owner, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(owner)
                .get()
                .addOnCompleteListener(task -> {
                    List<String> storeIDList = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) { // List owner
                            storeIDList.add(ds.getKey());
                        }
                    }
                    iControlData.storeIDList(storeIDList);
                });
    }

    public void putStore(String owner, Store store, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(owner)
                .child(store.getStoreID())
                .setValue(store)
                .addOnCompleteListener(task -> {
                    iControlData.isSuccess(task.isSuccessful());
                    FireBaseInit.getInstance().getReference()
                            .child(STORE_OWNER)
                            .child(store.getStoreID())
                            .setValue(owner);
                });
    }

    public void getStoreList(String owner, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(owner)
                .get()
                .addOnCompleteListener(task -> {
                    List<Store> storeList = new LinkedList<>();
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().hasChildren()) {
                            for (DataSnapshot data : task.getResult().getChildren()) {
                                Store store = data.getValue(Store.class);
                                if (store != null) {
                                    storeList.add(store);
                                }
                            }
                        }
                    }
                    iControlData.storeList(storeList);
                    currentStoreIDs = storeList.stream().map(Store::getStoreID).filter(Objects::nonNull).collect(Collectors.toList());
                });
    }

    /**
     * Lấy lên danh sách các store cách vị trị hiện tại 20 km (mặc định)
     */
    public void getFullStore(LatLng currentLocation, final double maxDistance, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    //Lv 1 OwnerID
                    List<Store> storeList = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().hasChildren()) {
                        for (DataSnapshot ownerData : task.getResult().getChildren()) {
                            // Lv 2 StoreID
                            if (ownerData.hasChildren()) {
                                for (DataSnapshot storeData : ownerData.getChildren()) {
                                    Store store = storeData.getValue(Store.class);
                                    if (store != null && store.isActive()) {
                                        // Tính khoảng cách
                                        double distanceTo = calculateDistance(currentLocation, store.getLatLng().toLatLng());
                                        if (distanceTo <= maxDistance) {
                                            store.distance = distanceTo;
                                            storeList.add(store);
                                        }
                                    }
                                }
                            }
                        }
                        // Sort danh sách theo khoảng cách tăng dần
                        storeList.sort(Comparator.comparingDouble(v -> v.distance));

                    }
                    iControlData.storeList(storeList);
                });

    }

    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }

    private List<String> currentStoreIDs = new ArrayList<>();

    public void updateStores(List<Store> storeList, String owner, IControlData iControlData) {
        storeList = storeList.stream().filter(s -> s.getStoreID() != null).collect(Collectors.toList());

        DatabaseReference ref = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(owner);

        storeList.forEach(s -> {
            if (s.isUpdate) {
                s.isUpdate = false;
                ref.child(s.getStoreID())
                        .setValue(s)
                        .addOnCompleteListener(task -> {
                            FireBaseInit.getInstance().getReference()
                                    .child(STORE_OWNER)
                                    .child(s.getStoreID())
                                    .setValue(owner);
                        });
            }
        });

        // Xóa
        for (String id : currentStoreIDs) {
            if (id == null) continue;

            boolean isDelete = storeList.stream().noneMatch(z -> z.getStoreID().equals(id));
            if (isDelete) {
                ref.child(id).removeValue();
            }
        }

        currentStoreIDs = storeList.stream().map(Store::getStoreID).filter(Objects::nonNull).collect(Collectors.toList());

        iControlData.isSuccess(true);

    }

    public void getOwnerIDByStoreID(String storeID, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(STORE_OWNER)
                .child(storeID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String owner = task.getResult().getValue(String.class);
                        if (owner != null) {
                            iControlData.ownerID(owner);
                        }
                    }
                });
    }

    public interface IControlData {
        default void isSuccess(boolean is) {
        }

        default void storeList(List<Store> storeList) {
        }

        default void storeIDList(List<String> storeIDList) {
        }

        default void ownerID(String ownerID) {
        }
    }
}
