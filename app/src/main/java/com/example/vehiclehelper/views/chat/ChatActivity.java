package com.example.vehiclehelper.views.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.chat.dao.ChatDAO;
import com.example.vehiclehelper.helper.chat.model.MessageDetail;
import com.example.vehiclehelper.helper.common.Utils;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.fcm.model.NotificationModel;
import com.example.vehiclehelper.helper.fcm.service.NotificationService;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVChatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity {
    private Activity activity;

    private TextView tv_name;
    private RecyclerView rv_chat;
    private EditText et_content;
    private ImageView iv_send;
    private LinearLayout ln_pbar;

    private ChatDAO chatDAO;
    private UserDAO userDAO;
    private StoreDAO storeDAO;
    private RVChatAdapter rvChatAdapter;
    private String senderID;
    private String receiverID;
    private String cusName;
    private String storeName;
    private String receiverName;
    private String owner;
    private SessionManager sessionManager;
    private boolean isCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        activity = this;
        sessionManager = SessionManager.getINSTANCE();
        isCustomer = sessionManager.getUserSession().type.equals(Users.TYPE.CUSTOMER);

        Intent intent = getIntent();
        senderID = intent.getStringExtra("senderID");
        receiverID = intent.getStringExtra("receiveID");
        owner = intent.getStringExtra("owner");
        cusName = intent.getStringExtra("cusName");
        storeName = intent.getStringExtra("storeName");
        receiverName = intent.getStringExtra("receiverName");

        String sessionChatID = Utils.buildSessionChatID(senderID, receiverID);
        chatDAO = new ChatDAO(sessionChatID, cusName, storeName);
        userDAO = new UserDAO();
        storeDAO = new StoreDAO();
        rvChatAdapter = new RVChatAdapter(activity, senderID, sessionChatID);
        getWidgets();
        setListener();
        tv_name.setText(receiverName);
        sessionManager.putMsgCurrent(receiverID);
        initChatList();
    }

    private void getWidgets() {
        tv_name = findViewById(R.id.tv_name);

        rv_chat = findViewById(R.id.rv_chat);

        et_content = findViewById(R.id.et_content);

        iv_send = findViewById(R.id.iv_send);

        ln_pbar = findViewById(R.id.ln_pbar);
    }

    private void setListener() {
        iv_send.setOnClickListener(v -> {
            String content = et_content.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                et_content.setText(null);
                ViewUtils.progressBarProcess(true, ln_pbar, activity);
                final MessageDetail messageDetail = new MessageDetail().buildMessage(content, senderID);
                messageDetail.setSendDate(Utils.buildCurrentDate());
                chatDAO.putMessage(messageDetail, new ChatDAO.IControlData() {
                    @Override
                    public void isOK(boolean is) {
                        ViewUtils.progressBarProcess(false, ln_pbar, activity);

                        // Notify FCM
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.data = new NotificationModel.Data();
                        notificationModel.data.group = NotificationService.MESS_GROUP;
                        notificationModel.data.content = messageDetail.getContent();
                        notificationModel.data.senderName = isCustomer ? cusName : storeName;
                        notificationModel.data.senderID = receiverID;
                        notificationModel.data.receiverName = notificationModel.data.senderName;
                        notificationModel.data.receiverID = senderID;
                        notificationModel.data.cusName = cusName;
                        notificationModel.data.storeName = storeName;

                        if (owner != null) {
                            userDAO.sendNotify(owner, notificationModel);
                        } else {
                            // Tìm owner từ storeID
                            storeDAO.getOwnerIDByStoreID(receiverID, new StoreDAO.IControlData() {
                                @Override
                                public void ownerID(String ownerID) {
                                    userDAO.sendNotify(ownerID, notificationModel);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void initChatList() {
        rv_chat.setAdapter(rvChatAdapter);
        chatDAO.initChatSession(new ChatDAO.IControlData() {
            @Override
            public void chatList(List<MessageDetail> messageDetails) {
                if (messageDetails == null) messageDetails = new ArrayList<>();
                rvChatAdapter.setChatList(messageDetails);
                chatDAO.setRef(messageDetails.stream().map(MessageDetail::getId).collect(Collectors.toList()));
            }

            @Override
            public void chatMsg(MessageDetail messageDetail) {
                if (messageDetail != null) {
                    rvChatAdapter.insertItem(messageDetail);
                    rv_chat.smoothScrollToPosition(rvChatAdapter.getItemCount());
                }
            }
        });

    }

    public void back(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionManager.removeMsgCurrent(receiverID);
        chatDAO.removeListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionManager.putMsgCurrent(receiverID);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sessionManager.removeMsgCurrent(receiverID);
    }
}