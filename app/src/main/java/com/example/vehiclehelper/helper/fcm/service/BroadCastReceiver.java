package com.example.vehiclehelper.helper.fcm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.vehiclehelper.helper.chat.model.MessageDetail;
import com.example.vehiclehelper.helper.chat.model.MessageRecent;
import com.example.vehiclehelper.helper.common.Utils;
import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;

public class BroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra("isChat", false)) {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    String content = remoteInput.getCharSequence(NotificationService.REPLY_KEY).toString();
                    if (!TextUtils.isEmpty(content)) {
                        String senderID = intent.getStringExtra("senderID");
                        String receiverID = intent.getStringExtra("receiverID");
                        String cusName = intent.getStringExtra("cusName");
                        String storeName = intent.getStringExtra("storeName");

                        final MessageDetail messageDetail = new MessageDetail().buildMessage(content, senderID);
                        messageDetail.setSendDate(Utils.buildCurrentDate());

                        String sessionChatID = Utils.buildSessionChatID(senderID, receiverID);
                        FireBaseInit.getInstance().getReference()
                                .child(TableName.ChatSession.name())
                                .child(sessionChatID)
                                .child(messageDetail.getId())
                                .setValue(messageDetail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Add message recent
                                        MessageRecent messageRecent = new MessageRecent(sessionChatID,
                                                content, messageDetail.getSendDate(),
                                                Utils.getCusIDOrStoreIDFromSSChatID(sessionChatID, true),
                                                cusName,
                                                Utils.getCusIDOrStoreIDFromSSChatID(sessionChatID, false),
                                                storeName);
                                        messageRecent.setSenderID(messageDetail.getUserID());

                                        FireBaseInit.getInstance().getReference()
                                                .child(TableName.MessageRecent.name())
                                                .child(sessionChatID)
                                                .setValue(messageRecent);
                                        NotificationManagerCompat.from(context).cancel(intent.getIntExtra("notifyID", 0));
                                    }
                                });
                    }
                }
            }
        }
    }
}
