package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.chat.model.MessageDetail;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.store_service.RequestServiceStoreManageActivity;

import java.util.ArrayList;
import java.util.List;

public class RVChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String currentUserID;
    private final String sessionChatID;
    private final List<MessageDetail> messageDetails;
    private final Activity activity;
    private final SessionManager sessionManager;

    public RVChatAdapter(Activity activity, String currentUserID, String sessionChatID) {
        this.messageDetails = new ArrayList<>();
        this.activity = activity;
        this.currentUserID = currentUserID;
        this.sessionChatID = sessionChatID;
        this.sessionManager = SessionManager.getINSTANCE();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChatList(List<MessageDetail> messageDetails) {
        this.messageDetails.addAll(messageDetails);
        notifyDataSetChanged();
    }

    public void insertItem(MessageDetail messageDetail) {
        int index = this.messageDetails.size();
        this.messageDetails.add(index, messageDetail);
        notifyItemInserted(index);
        sessionManager.removeMsgNotSeen(sessionChatID);
        System.out.println(messageDetail.getContent());
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_mess_send, parent, false));
        } else if (viewType == 0) {
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_mess_recieve, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_mess_service, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDetail messageDetail = this.messageDetails.get(position);
        boolean isService = messageDetail.getServiceID() != null;

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tv_content.setText(messageDetail.getContent());
        viewHolder.tv_time.setText(messageDetail.getSendDate());

        viewHolder.rl_mess.setOnClickListener(v -> {
            if (!isService) {
                if (viewHolder.tv_time.getVisibility() == View.VISIBLE) {
                    viewHolder.tv_time.setVisibility(View.GONE);
                } else {
                    viewHolder.tv_time.setVisibility(View.VISIBLE);
                }
            } else {
                activity.startActivity(new Intent(activity, RequestServiceStoreManageActivity.class));
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
//        return this.messageDetails.get(position).getUserID().equals(this.currentUserID) ? 1 : 0;
        return this.messageDetails.get(position).getServiceID() != null ? 2 : this.messageDetails.get(position).getUserID().equals(this.currentUserID) ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return messageDetails.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rl_mess;
        private final TextView tv_content;
        private final TextView tv_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_mess = itemView.findViewById(R.id.rl_mess);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }
}
