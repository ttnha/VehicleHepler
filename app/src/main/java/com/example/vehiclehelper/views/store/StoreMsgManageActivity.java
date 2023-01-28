package com.example.vehiclehelper.views.store;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.chat.dao.MessageRecentDAO;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.dao.StoreMsgDAO;
import com.example.vehiclehelper.helper.store.model.StoreMessage;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.adapter.RVStoreMsgManageAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class StoreMsgManageActivity extends AppCompatActivity {
    private final Activity activity = this;
    private RecyclerView rv_stores_msg;
    private TextView tv_empty;
    private LinearLayout ln_pbar;

    private StoreMsgDAO storeMsgDAO;
    private MessageRecentDAO messageRecentDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_msg_manage);

        storeMsgDAO = new StoreMsgDAO();
        messageRecentDAO = new MessageRecentDAO();

        getWidgets();
        initStoreMsgList();
    }

    private void initStoreMsgList() {
        RVStoreMsgManageAdapter rvStoreMsgManageAdapter = new RVStoreMsgManageAdapter(activity);
        rv_stores_msg.setAdapter(rvStoreMsgManageAdapter);
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        storeMsgDAO.getStoreMsg(new StoreMsgDAO.IControlData() {
            @Override
            public void storeList(List<StoreMessage> storeMessageList) {
                if (storeMessageList.isEmpty()) {
                    tv_empty.setVisibility(View.VISIBLE);
                } else {
                    tv_empty.setVisibility(View.GONE);
                    rvStoreMsgManageAdapter.setStoreMsgList(storeMessageList);

                    List<String> storeIDs = storeMessageList.stream().map(StoreMessage::getStoreID).collect(Collectors.toList());
                    messageRecentDAO.addListenerOnStoreMsg(storeIDs, new MessageRecentDAO.IControlData() {
                        @Override
                        public void pushNotifyCount(String storeMsgID) {
                            rvStoreMsgManageAdapter.updateNotifyCount(storeMsgID);
                        }
                    });

                }
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            }
        });
    }

    private void getWidgets() {
        rv_stores_msg = findViewById(R.id.rv_stores_msg);
        tv_empty = findViewById(R.id.tv_empty);
        ln_pbar = findViewById(R.id.ln_pbar);
    }

    public void back(View view) {
        finish();
    }
}