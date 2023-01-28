package com.example.vehiclehelper.helper.stats.model;

import androidx.annotation.NonNull;

import com.example.vehiclehelper.helper.store_service.model.RequestService;

public class Stats {
    public String timeStart;
    public String timeEnd;
    public boolean isCheckDone = true;
    public boolean isCheckCancel = true;
    public boolean isCheckProcessing = true;

    public String statusString() {
        String rs = "";
        if (isCheckDone) {
            rs += RequestService.RSStatus.DONE.name();
        }
        if (isCheckCancel) {
            rs += RequestService.RSStatus.CANCEL.name();
        }
        if (isCheckProcessing) {
            rs += RequestService.RSStatus.PROCESSING.name();
        }
        return rs;
    }

    @NonNull
    @Override
    public String toString() {
        return timeStart + timeEnd + isCheckDone + isCheckCancel + isCheckProcessing;
    }
}
