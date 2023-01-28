package com.example.vehiclehelper.helper.store.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store.model.StoreRating;

public class RatingDAO {
    private static final String TABLE_NAME = TableName.Rating.name();

    public void putRating(StoreRating storeRating, IControlData iControlData) {
        String[] ids = storeRating.getId().split("_");
        String ownerID = ids[0];
        String storeID = ids[1];

        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(ownerID)
                .child(storeID)
                .child(storeRating.getId())
                .setValue(storeRating)
                .addOnCompleteListener(task -> {
                    iControlData.isOK(task.isSuccessful());
                });
    }

    public interface IControlData {
        void isOK(boolean isOK);
    }
}
