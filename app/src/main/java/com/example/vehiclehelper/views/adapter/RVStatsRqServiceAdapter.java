package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.store_service.model.RequestService;

import java.util.ArrayList;
import java.util.List;

public class RVStatsRqServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity activity;
    private List<RequestService> requestServiceList;

    public RVStatsRqServiceAdapter(Activity activity, List<RequestService> requestServiceList) {
        this.activity = activity;
        this.requestServiceList = requestServiceList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<RequestService> requestServiceList) {
        this.requestServiceList = new ArrayList<>();
        this.requestServiceList.addAll(requestServiceList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_service_stats, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestService requestService = this.requestServiceList.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tv_content.setText(requestService.getContent());
        viewHolder.tv_time.setText(requestService.getCreateDate());
        viewHolder.tv_store_name.setText(requestService.infoName.getStoreName());
        viewHolder.tv_cus_name.setText(requestService.infoName.getCusName());

        if (requestService.getStatus().equals(RequestService.RSStatus.DONE.name())) {
            viewHolder.tv_status.setText("Hoàn thành");
            viewHolder.tv_status.setTextColor(activity.getColor(R.color.teal_700));
            viewHolder.rl_mess.setBackgroundColor(activity.getColor(R.color.teal_700_light));
        } else if (requestService.getStatus().equals(RequestService.RSStatus.PROCESSING.name())) {
            viewHolder.tv_status.setText("Đang xử lý");
            viewHolder.tv_status.setTextColor(activity.getColor(R.color.yellow));
            viewHolder.rl_mess.setBackgroundColor(activity.getColor(R.color.yellow_light));
        } else {
            viewHolder.tv_status.setText("Đã hủy");
            viewHolder.tv_status.setTextColor(activity.getColor(R.color.red_dark));
            viewHolder.rl_mess.setBackgroundColor(activity.getColor(R.color.red_light));
        }
    }

    @Override
    public int getItemCount() {
        return this.requestServiceList != null ? requestServiceList.size() : 0;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rl_mess;
        private final TextView tv_status;
        private final TextView tv_time;
        private final TextView tv_store_name;
        private final TextView tv_cus_name;
        private final TextView tv_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_mess = itemView.findViewById(R.id.rl_mess);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_store_name = itemView.findViewById(R.id.tv_store_name);
            tv_cus_name = itemView.findViewById(R.id.tv_cus_name);
        }
    }
}
