package com.example.vehiclehelper.helper.chat.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vehiclehelper.helper.chat.model.MessageRecent;
import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class MessageRecentDAO {
    private static final String TABLE_NAME = TableName.MessageRecent.name();

    /**
     * @param id Có thể là userID hoặc storeID
     *           ID của msg recent là tổ hợp của userID_storeID
     */
    public void getMsgRecentList(String id, IControlData iControlData) {
        Users users = UserDAO.getUser();
        if (users != null) {
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .get()
                    .addOnCompleteListener(task -> {
                        boolean isOK = false;

                        if (task.isSuccessful() && task.getResult().exists()) {
                            if (users.type == Users.TYPE.CUSTOMER) {
                                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                    if (Objects.requireNonNull(snapshot.getKey()).startsWith(id + "_")) {
                                        addListenerOnItem(snapshot.getKey(), iControlData);
                                        isOK = true;
//                                        MessageRecent messageRecent = snapshot.getValue(MessageRecent.class);
//                                        if (messageRecent != null) {
//                                            messageRecentList.add(messageRecent);
//                                        }
                                    }
                                }
                            } else {
                                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                    if (Objects.requireNonNull(snapshot.getKey()).endsWith("_" + id)) {
                                        addListenerOnItem(snapshot.getKey(), iControlData);
                                        isOK = true;
//                                        MessageRecent messageRecent = snapshot.getValue(MessageRecent.class);
//                                        if (messageRecent != null) {
//                                            messageRecentList.add(messageRecent);
//                                        }
                                    }
                                }
                            }
                        }
                        iControlData.isOK(isOK);
                    });

        } else {
            iControlData.isOK(false);
        }
    }

    public void addListenerOnItem(final String recentID, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(recentID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MessageRecent messageRecent = snapshot.getValue(MessageRecent.class);
                        if (messageRecent != null) {
                            iControlData.msgRecent(messageRecent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addListenerOnStoreMsg(List<String> storeIDs, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            for (String st : storeIDs) {
                                if (Objects.requireNonNull(snapshot.getKey()).endsWith("_" + st)) {
                                    FireBaseInit.getInstance().getReference()
                                            .child(TABLE_NAME)
                                            .child(snapshot.getKey())
                                            .addChildEventListener(new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                                }

                                                @Override
                                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                                    if (snapshot.getKey() != null && snapshot.getKey().equals("sendDate")) {
                                                        iControlData.pushNotifyCount(st);
                                                    }
                                                }

                                                @Override
                                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                                }

                                                @Override
                                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                    storeIDs.remove(st);
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    public interface IControlData {
        default void pushNotifyCount(String storeMsgID) {
        }

        default void msgRecent(MessageRecent messageRecent) {
        }

        default void isOK(boolean is) {
        }
    }

}
