package com.example.vehiclehelper.helper.store_service.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store_service.model.InfoName;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestServiceDAO {
    private static final String TABLE_NAME = TableName.RequestService.name();

    // Hàm kiểm tra trạng thái yêu cầu dịch vụ của khách hàng có đang tồn tại 1 dịch vụ
    // đang xử lý hay không, nếu không thì cho phép thêm dịch vụ, ngược lại thì hỏi có muốn cancel dv đang xử lý và
    // thêm dv mới hay không?
    public void statusIsProcessing(String cusIDstoreID, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(cusIDstoreID)
                .get()
                .addOnCompleteListener(task -> {
                    boolean is = false;
                    RequestService rqTmp = null;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            if (!Objects.equals(ds.getKey(), "InfoName")) {
                                RequestService requestService = ds.getValue(RequestService.class);
                                if (requestService != null && requestService.getStatus().equals(RequestService.RSStatus.PROCESSING.name())) {
                                    is = true;
                                    rqTmp = requestService;
                                    break;
                                }
                            }
                        }
                    }
                    iControlData.is(is, rqTmp);
                });
    }

    public void changeStatus(String cusIDstoreID, String key, String status, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(cusIDstoreID)
                .child(key)
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> iControlData.is(task.isSuccessful()));
    }

    public void putRequestService(String cusIDstoreID, RequestService requestService, InfoName infoName, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(cusIDstoreID)
                .child(requestService.getId())
                .setValue(requestService)
                .addOnCompleteListener(task -> {
                    iControlData.is(task.isSuccessful());
                    FireBaseInit.getInstance().getReference()
                            .child(TABLE_NAME)
                            .child(cusIDstoreID)
                            .child("InfoName")
                            .setValue(infoName);
                });
    }

    public void getRequestServiceManageListByCusID(String cusID, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    List<RequestServiceManage> requestServiceManage = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            if (Objects.requireNonNull(ds.getKey()).startsWith(cusID + "_")) {
                                List<RequestService> requestServiceList = new ArrayList<>();
                                InfoName infoName = null;
                                for (DataSnapshot dsRqS : ds.getChildren()) {
                                    if (!Objects.equals(dsRqS.getKey(), "InfoName")) {
                                        requestServiceList.add(dsRqS.getValue(RequestService.class));
                                    } else {
                                        infoName = dsRqS.getValue(InfoName.class);
                                    }
                                }
                                if (requestServiceList.size() > 0)
                                    requestServiceManage.add(new RequestServiceManage(requestServiceList, infoName));
                            }
                        }
                    }
                    iControlData.requestServiceManageList(requestServiceManage);
                });
    }

    public void getRequestServiceManageListByStoreIDList(List<String> storeIDs, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    List<RequestServiceManage> requestServiceManage = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            boolean isOK;
                            if (storeIDs.size() == 1) {
                                isOK = Objects.requireNonNull(ds.getKey()).endsWith("_" + storeIDs.get(0));
                            } else {
                                isOK = storeIDs.stream().anyMatch(v -> Objects.requireNonNull(ds.getKey()).endsWith("_" + v));
                            }
                            if (isOK) {
                                List<RequestService> requestServiceList = new ArrayList<>();
                                InfoName infoName = null;
                                for (DataSnapshot dsRqS : ds.getChildren()) {
                                    if (!Objects.equals(dsRqS.getKey(), "InfoName")) {
                                        requestServiceList.add(dsRqS.getValue(RequestService.class));
                                    } else {
                                        infoName = dsRqS.getValue(InfoName.class);
                                    }
                                }
                                if (requestServiceList.size() > 0)
                                    requestServiceManage.add(new RequestServiceManage(requestServiceList, infoName));
                            }
                        }
                    }
                    iControlData.requestServiceManageList(requestServiceManage);
                });
    }

    public interface IControlData {
        //key: Key đang ở trạng thái PROCESSING && is==true
        default void is(boolean is, RequestService data) {
        }

        default void is(boolean is) {
        }

        default void requestServiceList(List<RequestService> requestServiceList) {
        }

        default void requestServiceManageList(List<RequestServiceManage> requestServiceManages) {
        }

        default void keyList(List<String> keyList) {
        }
    }
}
