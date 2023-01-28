package com.example.vehiclehelper.helper.store_service.model;

import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private String id;
    private String serviceName;
    private int priceOfService;
    // Chủ cửa hàng
    private String owner;
    // Service thuộc cửa hàng nào (1 service có thể thuộc nhiều cửa hàng cùng 1 owner)
    private List<String> storeIDs;
    private boolean isActive;
    public transient boolean isSelect;
    public transient boolean isUpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getPriceOfService() {
        return priceOfService;
    }

    public void setPriceOfService(int priceOfService) {
        this.priceOfService = priceOfService;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void putStoreID(String storeID) {
        if (this.storeIDs == null) {
            this.storeIDs = new ArrayList<>();
        }
        this.storeIDs.add(storeID);
    }

    public List<String> getStoreIDs() {
        return storeIDs;
    }

    public void setStoreIDs(List<String> storeIDs) {
        this.storeIDs = storeIDs;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
