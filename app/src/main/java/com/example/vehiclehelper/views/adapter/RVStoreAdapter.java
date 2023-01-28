package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.model.Store;
import com.example.vehiclehelper.views.store.StoreInfoActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RVStoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String INACTIVE_STATUS = "TẠM DỪNG";
    private final Activity activity;
    private List<Store> storeList;

    // View in parent
    private final Button btn_save;


    public RVStoreAdapter(Activity activity) {
        this.btn_save = activity.findViewById(R.id.btn_save);

        this.activity = activity;
        this.storeList = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStoreList(List<Store> storeList) {
        this.storeList = storeList;

        // Thêm thằng cuối cùng tượng trưng cho item "THÊM..."
        this.storeList.add(new Store());

        this.notifyDataSetChanged();
    }

    public void insertItem(Store store) {
        int index = this.storeList.size() - 1;
        this.storeList.add(index, store);
        notifyItemInserted(index);
    }

    public void updateItem(Store store) {
        Store st = this.storeList.stream().filter(s -> s.getStoreID().equals(store.getStoreID())).findFirst().orElse(null);
        if (st != null) {
            int index = this.storeList.indexOf(st);
            this.storeList.remove(st);
            this.storeList.add(index, store);
            notifyItemChanged(index);
        }
    }

    private void deleteItem(Store store) {
        int index = this.storeList.indexOf(store);
        this.storeList.remove(index);
        this.notifyItemRemoved(index);
        btn_save.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // viewType = 1 là item bình thường, else là item "THÊM ..."
        if (viewType == 1) {
            return new StoreHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_store, parent, false));
        } else {
            return new StoreLastHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_add, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 1) {
            Store store = storeList.get(position);
            if (store != null) {
                StoreHolder storeHolder = (StoreHolder) holder;
                storeHolder.tv_store_name.setText(store.getStoreName());
                if (store.isActive()) {
                    storeHolder.tv_store_status.setText(ACTIVE_STATUS);
                    storeHolder.tv_store_status.setTextColor(activity.getColor(R.color.teal_700));
                    storeHolder.rl_all.setBackgroundColor(activity.getColor(R.color.teal_700_light));
                } else {
                    storeHolder.tv_store_status.setText(INACTIVE_STATUS);
                    storeHolder.tv_store_status.setTextColor(activity.getColor(R.color.red_dark));
                    storeHolder.rl_all.setBackgroundColor(activity.getColor(R.color.red_light));
                }
                storeHolder.tv_address.setText(store.getAddressName());
                storeHolder.iv_remove.setOnClickListener(v -> ViewUtils.showDialogConfirm("Xác nhận xóa", store.getStoreName(), activity, is -> {
                    if (is) {
                        deleteItem(store);
                    }
                }));

                storeHolder.rl_all.setOnClickListener(v -> {
                    // Edit Store
                    Intent intent = new Intent(activity, StoreInfoActivity.class);
                    String storeStr = new Gson().toJson(store);
                    intent.putExtra("storeStr", storeStr);
                    intent.putExtra("store_manage", true);
                    activity.startActivityForResult(intent, 69);
                });

            }

        } else {
            StoreLastHolder storeLastHolder = (StoreLastHolder) holder;
            storeLastHolder.ll_add.setOnClickListener(v -> {
                Intent intent = new Intent(activity, StoreInfoActivity.class);
                intent.putExtra("store_manage", true);
                activity.startActivityForResult(intent, 69);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == this.storeList.size() - 1 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    protected static class StoreHolder extends RecyclerView.ViewHolder {
        private final TextView tv_store_name;
        private final TextView tv_store_status;
        private final TextView tv_address;
        private final ImageView iv_remove;
        private final RelativeLayout rl_all;

        public StoreHolder(@NonNull View itemView) {
            super(itemView);
            tv_store_name = itemView.findViewById(R.id.tv_store_name);
            tv_store_status = itemView.findViewById(R.id.tv_store_status);
            tv_address = itemView.findViewById(R.id.tv_address);
            iv_remove = itemView.findViewById(R.id.iv_remove);
            rl_all = itemView.findViewById(R.id.rl_all);
        }
    }

    protected static class StoreLastHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ll_add;

        public StoreLastHolder(@NonNull View itemView) {
            super(itemView);
            ll_add = itemView.findViewById(R.id.ll_add);
        }
    }
}
