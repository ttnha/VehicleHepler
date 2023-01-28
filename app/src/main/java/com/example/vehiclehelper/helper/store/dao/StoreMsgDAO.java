package com.example.vehiclehelper.helper.store.dao;

import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.store.model.StoreMessage;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoreMsgDAO {
    private static final String TABLE_NAME = TableName.Store.name();

    public void getStoreMsg(IControlData iControlData) {
        Users users = UserDAO.getUser();
        if (users == null) {
            iControlData.storeList(new ArrayList<>());
        } else {
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(users.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        List<StoreMessage> storeMessageList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().hasChildren()) {
                                for (DataSnapshot data : task.getResult().getChildren()) {
                                    StoreMessage storeMsg = data.getValue(StoreMessage.class);
                                    if (storeMsg != null) {
                                        storeMessageList.add(storeMsg);
                                    }
                                }
                            }
                        }
                        iControlData.storeList(storeMessageList);
                    });
        }
    }

    public interface IControlData {
        default void storeList(List<StoreMessage> storeMessageList) {
        }
    }

}
