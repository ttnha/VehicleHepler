package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.store_service.model.InfoName;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.example.vehiclehelper.views.store_service.RequestServiceDetailActivity;
import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

public class RVRqServiceStoreManageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity activity;
    private final List<RequestServiceManage> requestServiceManageList;


    public RVRqServiceStoreManageAdapter(Activity activity, List<RequestServiceManage> requestServiceManageList) {
        this.activity = activity;
        // Sắp xếp lại theo ngày đặt giảm dần
        this.requestServiceManageList = requestServiceManageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RqServiceStoreHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_rq_service_store, parent, false));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestServiceManage requestServiceManage = requestServiceManageList.get(position);
        if (requestServiceManage != null) {
            RqServiceStoreHolder msgHolder = (RqServiceStoreHolder) holder;

            InfoName infoName = requestServiceManage.getInfoName();
            msgHolder.tv_store_name.setText(infoName.getStoreName());
            msgHolder.tv_cus_name.setText(infoName.getCusName());

            List<RequestService> requestServices = requestServiceManage.getRequestServiceList();
            int[] counts = new int[3];
            if (requestServices != null && requestServices.size() > 0) {
                counts = buildCount(requestServices);
            }
            msgHolder.tv_process_count.setText(String.valueOf(counts[0]));
            msgHolder.tv_done_count.setText(String.valueOf(counts[1]));
            msgHolder.tv_cancel_count.setText(String.valueOf(counts[2]));


            msgHolder.ll_all.setOnClickListener(v -> {
                Intent intent = new Intent(activity, RequestServiceDetailActivity.class);
                intent.putExtra("requestServiceManage", new Gson().toJson(requestServiceManage));
                activity.startActivityForResult(intent, 69);
            });
        }
    }

    //index: 0-PROCESSING, 1:DONE, 2:CANCEL
    private int[] buildCount(List<RequestService> requestServices) {
        int[] rs = new int[3];
        for (RequestService rq : requestServices) {
            if (Objects.equals(rq.getStatus(), RequestService.RSStatus.PROCESSING.name())) {
                rs[0]++;
            } else if (Objects.equals(rq.getStatus(), RequestService.RSStatus.DONE.name())) {
                rs[1]++;
            } else {
                rs[2]++;
            }
        }
        return rs;
    }

    @Override
    public int getItemCount() {
        return requestServiceManageList != null ? requestServiceManageList.size() : 0;
    }

    protected static class RqServiceStoreHolder extends RecyclerView.ViewHolder {
        private final TextView tv_store_name;
        private final TextView tv_cus_name;
        private final TextView tv_process_count;
        private final TextView tv_done_count;
        private final TextView tv_cancel_count;
        private final LinearLayout ll_all;

        public RqServiceStoreHolder(@NonNull View itemView) {
            super(itemView);
            tv_store_name = itemView.findViewById(R.id.tv_store_name);
            tv_cus_name = itemView.findViewById(R.id.tv_cus_name);
            tv_process_count = itemView.findViewById(R.id.tv_process_count);
            tv_done_count = itemView.findViewById(R.id.tv_done_count);
            tv_cancel_count = itemView.findViewById(R.id.tv_cancel_count);
            ll_all = itemView.findViewById(R.id.rl_all);
        }
    }
}
