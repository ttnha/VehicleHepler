package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.store.model.StoreMessage;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.chat.MessageListActivity;

import java.util.ArrayList;
import java.util.List;

public class RVStoreMsgManageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final SessionManager sessionManager;
    private final Activity activity;
    private List<StoreMessage> storeMsgList;

    public RVStoreMsgManageAdapter(Activity activity) {
        this.activity = activity;
        this.storeMsgList = new ArrayList<>();
        this.sessionManager = SessionManager.getINSTANCE();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStoreMsgList(List<StoreMessage> storeMsgList) {
        this.storeMsgList = storeMsgList;
        this.notifyDataSetChanged();
    }

    public void updateItem(StoreMessage store) {
        StoreMessage st = this.storeMsgList.stream().filter(s -> s.getStoreID().equals(store.getStoreID())).findFirst().orElse(null);
        if (st != null) {
            int index = this.storeMsgList.indexOf(st);
            this.storeMsgList.remove(st);
            this.storeMsgList.add(index, store);
            notifyItemChanged(index);
        }
    }

    public void updateNotifyCount(String storeID) {
        StoreMessage st = this.storeMsgList.stream().filter(s -> s.getStoreID().equals(storeID)).findFirst().orElse(null);
        if (st != null) {
            int index = this.storeMsgList.indexOf(st);
            int count = st.getNotificationCount() + 1;
            st.setNotificationCount(count);
            notifyItemChanged(index);
            sessionManager.putMsgNotifyCount(st.getStoreID(), count);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StoreMsgHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_store_message, parent, false));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StoreMessage storeMsg = storeMsgList.get(position);
        if (storeMsg != null) {
            storeMsg.setNotificationCount(sessionManager.getMsgNotifyCount(storeMsg.getStoreID()));
            StoreMsgHolder msgHolder = (StoreMsgHolder) holder;
            msgHolder.tv_store_name.setText(storeMsg.getStoreName());
            if (storeMsg.getNotificationCount() > 0) {
                msgHolder.tv_notification.setText("Bạn có " + storeMsg.getNotificationCount() + " tin nhắn mới");
                msgHolder.tv_notification.setTextColor(activity.getColor(R.color.red_dark));
                msgHolder.tv_notification.setTypeface(null, Typeface.BOLD);
            } else {
                msgHolder.tv_notification.setText("Không có thông báo");
                msgHolder.tv_notification.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_notification.setTypeface(null, Typeface.NORMAL);
            }

            msgHolder.ll_all.setOnClickListener(v -> {
                msgHolder.tv_notification.setText("Không có thông báo");
                msgHolder.tv_notification.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_notification.setTypeface(null, Typeface.NORMAL);
                sessionManager.removeMsgNotifyCount(storeMsg.getStoreID());

                Intent intent = new Intent(activity, MessageListActivity.class);
                intent.putExtra("id", storeMsg.getStoreID());
                intent.putExtra("name", storeMsg.getStoreName());
                activity.startActivity(intent);
            });
        }

    }

    @Override
    public int getItemCount() {
        return storeMsgList.size();
    }

    protected static class StoreMsgHolder extends RecyclerView.ViewHolder {
        private final TextView tv_store_name;
        private final TextView tv_notification;
        private final LinearLayout ll_all;

        public StoreMsgHolder(@NonNull View itemView) {
            super(itemView);
            tv_store_name = itemView.findViewById(R.id.tv_store_name);
            tv_notification = itemView.findViewById(R.id.tv_notification);
            ll_all = itemView.findViewById(R.id.rl_all);
        }
    }
}
