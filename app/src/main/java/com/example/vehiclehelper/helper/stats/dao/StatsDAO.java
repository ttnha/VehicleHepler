package com.example.vehiclehelper.helper.stats.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store_service.dao.RequestServiceDAO;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsDAO {
    private static final String STORE_TABLE = TableName.Store.name();
    private final RequestServiceDAO requestServiceDAO;

    public StatsDAO() {
        this.requestServiceDAO = new RequestServiceDAO();
    }

    public void getFullRqService(String userID, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(STORE_TABLE)
                .child(userID)
                .get()
                .addOnCompleteListener(task -> {
                    //1. Lấy danh sách storeID
                    if (task.isSuccessful() && task.getResult().exists()) {
                        List<String> storeIDs = new ArrayList<>();
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            if (Boolean.TRUE.equals(ds.child("active").getValue(Boolean.class)))
                                storeIDs.add(ds.getKey());
                        }
                        if (!storeIDs.isEmpty()) {
                            requestServiceDAO.getRequestServiceManageListByStoreIDList(storeIDs, new RequestServiceDAO.IControlData() {
                                @Override
                                public void requestServiceManageList(List<RequestServiceManage> requestServiceManages) {
                                    iControlData.requestServiceMap(toRequestServiceMap(requestServiceManages));
                                }
                            });
                        } else {
                            iControlData.requestServiceMap(null);
                        }
                    } else {
                        iControlData.requestServiceMap(null);
                    }
                });
    }

    private Map<String, List<RequestService>> toRequestServiceMap(List<RequestServiceManage> requestServiceManages) {
        if (requestServiceManages == null || requestServiceManages.isEmpty()) return null;

        Map<String, List<RequestService>> rsMap = new HashMap<>();
        for (RequestServiceManage requestServiceManage : requestServiceManages) {
            String storeID = requestServiceManage.getRequestServiceList().get(0).getId().split("_")[1];
            requestServiceManage.getRequestServiceList().forEach(v -> v.infoName = requestServiceManage.getInfoName());
            rsMap.put(storeID, requestServiceManage.getRequestServiceList());
        }
        return rsMap;
    }

    public List<RequestService> flatMap(Map<String, List<RequestService>> mapRequest) {
        List<RequestService> list = mapRequest.values().stream().flatMap(Collection::stream).sorted(Comparator.comparing(RequestService::getCreateDate)).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    public interface IControlData {
        void requestServiceMap(Map<String, List<RequestService>> rqServiceMap);
    }

}
