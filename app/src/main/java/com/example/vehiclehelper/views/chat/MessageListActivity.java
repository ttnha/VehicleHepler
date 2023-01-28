package com.example.vehiclehelper.views.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.chat.dao.MessageRecentDAO;
import com.example.vehiclehelper.helper.chat.model.MessageRecent;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVMessageListAdapter;

public class MessageListActivity extends AppCompatActivity {
    private final Activity activity = this;
    private RecyclerView rv_msg_list;
    private TextView tv_name, tv_empty;
    private LinearLayout ln_pbar;

    private MessageRecentDAO messageRecentDAO;

    private String id;
    private boolean isCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        messageRecentDAO = new MessageRecentDAO();
        isCustomer = UserDAO.getUser().type == Users.TYPE.CUSTOMER;

        getWidgets();

        Intent intent = getIntent();
        tv_name.setText(intent.getStringExtra("name"));
        id = intent.getStringExtra("id");

        initMsgList();
    }

    private void getWidgets() {
        rv_msg_list = findViewById(R.id.rv_msg_list);
        tv_name = findViewById(R.id.tv_name);
        tv_empty = findViewById(R.id.tv_empty);
        ln_pbar = findViewById(R.id.ln_pbar);
    }

    private void initMsgList() {
        RVMessageListAdapter rvMessageListAdapter = new RVMessageListAdapter(activity, isCustomer);
        rv_msg_list.setAdapter(rvMessageListAdapter);

        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        messageRecentDAO.getMsgRecentList(id, new MessageRecentDAO.IControlData() {
            @Override
            public void isOK(boolean is) {
                tv_empty.setVisibility(is ? View.GONE : View.VISIBLE);
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            }

            @Override
            public void msgRecent(MessageRecent messageRecent) {
                rvMessageListAdapter.updateItem(messageRecent);
            }
        });
    }

    public void back(View view) {
        finish();
    }
}