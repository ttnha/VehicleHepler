package com.example.vehiclehelper.helper.store_service.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestServiceManage {
    private final List<RequestService> requestServiceList;
    private final InfoName infoName;

    public RequestServiceManage(List<RequestService> requestServiceList, InfoName infoName) {
        this.requestServiceList = requestServiceList;
        sort();
        this.infoName = infoName;
    }

    public List<RequestService> getRequestServiceList() {
        return requestServiceList;
    }

    public InfoName getInfoName() {
        return infoName;
    }

    private void sort() {
        if (requestServiceList != null) {
            requestServiceList.sort(Comparator.comparing(RequestService::getCreateDate));
            Collections.reverse(requestServiceList);
        }
    }
}
