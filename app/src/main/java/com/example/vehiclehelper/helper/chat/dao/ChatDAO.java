package com.example.vehiclehelper.helper.chat.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vehiclehelper.helper.chat.model.MessageDetail;
import com.example.vehiclehelper.helper.chat.model.MessageRecent;
import com.example.vehiclehelper.helper.common.Utils;
import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class ChatDAO {
    private static final String TABLE_NAME = TableName.ChatSession.name();
    private static final String TABLE_MESSAGE_RECENT = TableName.MessageRecent.name();

    private MessageRecent messageRecent;
    private final String chatSessionID;
    private final String cusName;
    private final String storeName;

    public ChatDAO(String chatSessionID, String cusName, String storeName) {
        this.chatSessionID = chatSessionID;
        this.cusName = cusName;
        this.storeName = storeName;
    }

    private final ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if (iControlChat != null) {
                if (!currentIDs.contains(snapshot.getKey())) {
                    MessageDetail messageDetail = snapshot.getValue(MessageDetail.class);
                    if (messageDetail != null) {
                        iControlChat.chatMsg(messageDetail);
                    }
                } else {
                    currentIDs.remove(snapshot.getKey());
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
    };

    private IControlData iControlChat;

    public void initChatSession(IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(chatSessionID)
                .get()
                .addOnCompleteListener(task -> {
                    List<MessageDetail> messageDetails = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult().exists()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            messageDetails.add(ds.getValue(MessageDetail.class));
                        }
                    }
                    iControlData.chatList(messageDetails);
                });


        mRefChat = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(chatSessionID);

        mRefRecent = FireBaseInit.getInstance().getReference()
                .child(TABLE_MESSAGE_RECENT)
                .child(chatSessionID);

        iControlChat = iControlData;
    }

    public void setRef(List<String> currentIDs) {
        this.currentIDs.addAll(currentIDs);
        mRefChat.addChildEventListener(childEventListener);
    }

    private final List<String> currentIDs = new ArrayList<>();

    public void putMessage(MessageDetail messageDetail, IControlData iControlData) {
        if (mRefChat == null) {
            mRefChat = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(chatSessionID);
        }
        mRefChat.child(messageDetail.getId())
                .setValue(messageDetail)
                .addOnCompleteListener(task -> {
                    if (messageDetail.getServiceID() == null) {
                        if (messageRecent == null) {
                            messageRecent = new MessageRecent(chatSessionID, messageDetail.getContent(),
                                    messageDetail.getSendDate(),
                                    Utils.getCusIDOrStoreIDFromSSChatID(chatSessionID, true),
                                    cusName,
                                    Utils.getCusIDOrStoreIDFromSSChatID(chatSessionID, false),
                                    storeName);
                        } else {
                            messageRecent.setContent(messageDetail.getContent());
                            messageRecent.setSendDate(messageDetail.getSendDate());
                        }
                        messageRecent.setSenderID(messageDetail.getUserID());
                        mRefRecent.setValue(messageRecent);
                    }
                    iControlData.isOK(task.isSuccessful());
                });

    }

    public void removeListener() {
        mRefChat.removeEventListener(childEventListener);
        mRefChat = null;
        mRefRecent = null;
    }

    private DatabaseReference mRefChat;
    private DatabaseReference mRefRecent;

    public interface IControlData {
        default void chatList(List<MessageDetail> messageDetails) {
        }

        default void chatMsg(MessageDetail messageDetail) {
        }

        default void isOK(boolean is) {
        }
    }
}
