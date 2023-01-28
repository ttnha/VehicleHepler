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
import com.example.vehiclehelper.helper.chat.model.MessageRecent;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.chat.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class RVMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity activity;
    private final List<MessageRecent> msgRecentList;
    private final boolean isCustomer;
    private final SessionManager sessionManager;

    public RVMessageListAdapter(Activity activity, boolean isCustomer) {
        this.isCustomer = isCustomer;
        this.activity = activity;
        this.msgRecentList = new ArrayList<>();
        this.sessionManager = SessionManager.getINSTANCE();
    }

//    @SuppressLint("NotifyDataSetChanged")
//    public void setMsgRecentList(List<MessageRecent> msgRecentList) {
//        this.msgRecentList = msgRecentList;
//        this.notifyDataSetChanged();
//    }

    public void updateItem(MessageRecent messageRecent) {
        MessageRecent recent = this.msgRecentList.stream().filter(s -> s.getId().equals(messageRecent.getId())).findFirst().orElse(null);
        if (recent != null) {
            int index = this.msgRecentList.indexOf(recent);
            this.msgRecentList.remove(recent);
            this.msgRecentList.add(index, messageRecent);
            notifyItemChanged(index);
            if (!sessionManager.isChatting(recent.getCusID()) && !sessionManager.isChatting(recent.getStoreID())) {
                sessionManager.putMsgNotSeen(recent.getId());
            }
        } else {
            this.msgRecentList.add(messageRecent);
            notifyItemInserted(this.msgRecentList.size());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MsgRecentHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_msg_recent, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageRecent messageRecent = msgRecentList.get(position);
        if (messageRecent != null) {
            MsgRecentHolder msgHolder = (MsgRecentHolder) holder;
            if (isCustomer)
                msgHolder.tv_name.setText(messageRecent.getStoreName());
            else
                msgHolder.tv_name.setText(messageRecent.getCusName());

            msgHolder.tv_content.setText(messageRecent.getContent());
            if ((messageRecent.getSenderID() != null && ((isCustomer && messageRecent.getSenderID().equals(messageRecent.getCusID()))
                    || (!isCustomer && messageRecent.getSenderID().equals(messageRecent.getStoreID()))))
                    || sessionManager.isSeen(messageRecent.getId())) {
                msgHolder.tv_content.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_content.setTypeface(null, Typeface.NORMAL);
                msgHolder.tv_name.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_name.setTypeface(null, Typeface.NORMAL);
            } else {
                msgHolder.tv_content.setTextColor(activity.getColor(R.color.black));
                msgHolder.tv_content.setTypeface(null, Typeface.BOLD);
                msgHolder.tv_name.setTextColor(activity.getColor(R.color.black));
                msgHolder.tv_name.setTypeface(null, Typeface.BOLD);
            }

            msgHolder.ll_all.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ChatActivity.class);
                if (isCustomer) {
                    intent.putExtra("senderID", messageRecent.getCusID());
                    intent.putExtra("receiveID", messageRecent.getStoreID());
                    intent.putExtra("receiverName", messageRecent.getStoreName());
                } else {
                    intent.putExtra("senderID", messageRecent.getStoreID());
                    intent.putExtra("receiveID", messageRecent.getCusID());
                    intent.putExtra("receiverName", messageRecent.getCusName());
                    intent.putExtra("owner", messageRecent.getCusID());
                }

                intent.putExtra("cusName", messageRecent.getCusName());
                intent.putExtra("storeName", messageRecent.getStoreName());

                msgHolder.tv_content.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_content.setTypeface(null, Typeface.NORMAL);
                msgHolder.tv_name.setTextColor(activity.getColor(R.color.gray));
                msgHolder.tv_name.setTypeface(null, Typeface.NORMAL);

                sessionManager.removeMsgNotSeen(messageRecent.getId());

                activity.startActivity(intent);
            });
        }

    }

    @Override
    public int getItemCount() {
        return msgRecentList.size();
    }

    protected static class MsgRecentHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name;
        private final TextView tv_content;
        private final LinearLayout ll_all;

        public MsgRecentHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_content = itemView.findViewById(R.id.tv_content);
            ll_all = itemView.findViewById(R.id.rl_all);
        }
    }
}
