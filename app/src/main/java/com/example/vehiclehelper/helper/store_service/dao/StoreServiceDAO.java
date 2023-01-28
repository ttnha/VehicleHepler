package com.example.vehiclehelper.helper.store_service.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store_service.model.StoreService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StoreServiceDAO {
    private static final String TABLE_NAME = TableName.StoreService.name();

    public void getServiceList(String owner, String storeID, List<String> currentServiceIDs, IControlData iControlData) {
        if (storeID == null) {
            iControlData.serviceList(new ArrayList<>());
        } else {
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(owner)
                    .get()
                    .addOnCompleteListener(task -> {
                        List<StoreService> storeServices = new LinkedList<>();
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().hasChildren()) {
                                if (currentServiceIDs != null && !currentServiceIDs.isEmpty()) {
                                    for (DataSnapshot data : task.getResult().getChildren()) {
                                        StoreService storeService = data.getValue(StoreService.class);
                                        if (storeService != null) {
                                            if (!currentServiceIDs.contains(storeService.getId())) {
                                                storeServices.add(storeService);
                                            }
                                        }
                                    }
                                } else {
                                    for (DataSnapshot data : task.getResult().getChildren()) {
                                        StoreService storeService = data.getValue(StoreService.class);
                                        if (storeService != null && storeService.getStoreIDs() != null && storeService.getStoreIDs().contains(storeID)) {
                                            storeServices.add(storeService);
                                        }
                                    }
                                }

                            }
                        }
                        iControlData.serviceList(storeServices);
                        this.currentServiceIDs = storeServices.stream().map(StoreService::getId).filter(Objects::nonNull).collect(Collectors.toList());
                    });
        }

    }

    private List<String> currentServiceIDs = new ArrayList<>();

    public void updateStoreServices(List<StoreService> storeServiceList, String owner, IControlData iControlData) {
        storeServiceList = storeServiceList.stream().filter(s -> s.getId() != null).collect(Collectors.toList());

        DatabaseReference ref = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(owner);

        storeServiceList.forEach(s -> {
            if (s.isUpdate) {
                s.isUpdate = false;
                ref.child(s.getId()).setValue(s);
            }
        });

        // Xóa
        for (String id : currentServiceIDs) {
            if (id == null) continue;

            boolean isDelete = storeServiceList.stream().noneMatch(z -> z.getId().equals(id));
            if (isDelete) {
                ref.child(id).removeValue();
            }
        }

        currentServiceIDs = storeServiceList.stream().map(StoreService::getId).filter(Objects::nonNull).collect(Collectors.toList());

        iControlData.isSuccess(true);

    }


    public interface IControlData {
        default void isSuccess(boolean is) {
        }

        default void serviceList(List<StoreService> serviceList) {
        }
    }
}
