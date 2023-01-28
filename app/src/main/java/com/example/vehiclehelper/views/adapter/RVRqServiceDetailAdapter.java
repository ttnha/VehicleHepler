package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class RVRqServiceDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity activity;
    private final List<RequestService> requestServiceList;
    private View v_select_tmp;
    private final IControlData iControlData;

    public RVRqServiceDetailAdapter(Activity activity, RequestServiceManage requestServiceManage, IControlData iControlData) {
        this.activity = activity;
        this.requestServiceList = requestServiceManage.getRequestServiceList();
        this.iControlData = iControlData;
    }

    public void clearSelected() {
        if (v_select_tmp != null) {
            v_select_tmp.setVisibility(View.INVISIBLE);
        }
    }

    public void updateStatus(String id, String status) {
        RequestService requestService = requestServiceList.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
        if (requestService != null) {
            int index = requestServiceList.indexOf(requestService);
            requestService.setStatus(status);
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_mess_service, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestService requestService = this.requestServiceList.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tv_content.setText(requestService.getContent());
        viewHolder.tv_time.setText(requestService.getCreateDate());
        viewHolder.ll.setVisibility(View.VISIBLE);
        viewHolder.tv_phone.setText(requestService.getPhoneNumber());

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
            viewHolder.rl_mess.setBackgroundColor(activity.getColor(R.color.red_light));
            viewHolder.tv_status.setTextColor(activity.getColor(R.color.red_dark));
        }

        viewHolder.tv_phone.setOnClickListener(v -> ViewUtils.call(activity, requestService.getPhoneNumber()));

        viewHolder.rl_mess.setOnClickListener(v -> {
            if (requestService.getStatus().equals(RequestService.RSStatus.PROCESSING.name())) {
                if (v_select_tmp != null) {
                    if (v_select_tmp != viewHolder.v_select) {
                        v_select_tmp.setVisibility(View.INVISIBLE);
                        v_select_tmp = viewHolder.v_select;
                        v_select_tmp.setVisibility(View.VISIBLE);
                        iControlData.btsBehavior(BottomSheetBehavior.STATE_EXPANDED, requestService);
                    } else {
                        if (v_select_tmp.getVisibility() == View.VISIBLE) {
                            v_select_tmp.setVisibility(View.INVISIBLE);
                            iControlData.btsBehavior(BottomSheetBehavior.STATE_COLLAPSED, null);
                        } else {
                            v_select_tmp.setVisibility(View.VISIBLE);
                            iControlData.btsBehavior(BottomSheetBehavior.STATE_EXPANDED, requestService);
                        }
                    }
                } else {
                    v_select_tmp = viewHolder.v_select;
                    v_select_tmp.setVisibility(View.VISIBLE);
                    iControlData.btsBehavior(BottomSheetBehavior.STATE_EXPANDED, requestService);
                }
            } else {
                iControlData.btsBehavior(BottomSheetBehavior.STATE_COLLAPSED, null);
            }
        });
    }

    public interface IControlData {
        void btsBehavior(int behavior, RequestService requestService);
    }

    @Override
    public int getItemCount() {
        return this.requestServiceList != null ? requestServiceList.size() : 0;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rl_mess;
        private final LinearLayout ll;
        private final TextView tv_content;
        private final TextView tv_time;
        private final TextView tv_status;
        private final TextView tv_phone;
        private final View v_select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_mess = itemView.findViewById(R.id.rl_mess);
            ll = itemView.findViewById(R.id.ll);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            v_select = itemView.findViewById(R.id.v_select);
        }
    }
}
