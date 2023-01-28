package com.example.vehiclehelper.views.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store_service.model.StoreService;
import com.example.vehiclehelper.views.store_service.ServiceInfoActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RVServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String INACTIVE_STATUS = "TẠM DỪNG";
    private final Activity activity;
    private List<StoreService> storeServices;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private int selectedCount = 0;
    private boolean isUpdating;
    private final boolean isCustomer;
    private final String BTN_STYLE;

    // View in parent
    private final Button btn_save;

    public void setBottomSheetBehavior(BottomSheetBehavior<View> bottomSheetBehavior) {
        this.bottomSheetBehavior = bottomSheetBehavior;
    }

    public RVServiceAdapter(Activity activity, boolean isCustomer) {
        this.btn_save = activity.findViewById(R.id.btn_save);
        this.isCustomer = isCustomer;
        this.BTN_STYLE = isCustomer ? "GỬI YÊU CẦU" : "LƯU";

        this.activity = activity;
        this.storeServices = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStoreList(List<StoreService> storeServices) {
        this.storeServices = storeServices;
        if (!isCustomer) {
            // Thêm thằng cuối cùng tượng trưng cho item "THÊM..."
            this.storeServices.add(new StoreService());
        }

        this.notifyDataSetChanged();
    }

    public void insertItem(StoreService storeService) {
        int index = this.storeServices.size() - 1;
        this.storeServices.add(index, storeService);
        notifyItemInserted(index);
        isUpdating = true;
    }

    public void updateItem(StoreService storeService) {
        StoreService st = this.storeServices.stream().filter(s -> s.getId().equals(storeService.getId())).findFirst().orElse(null);
        if (st != null) {
            int index = this.storeServices.indexOf(st);
            this.storeServices.remove(st);
            this.storeServices.add(index, storeService);
            notifyItemChanged(index);
            isUpdating = true;
        }
    }

    private void deleteItem(StoreService storeService) {
        int index = this.storeServices.indexOf(storeService);
        this.storeServices.remove(index);
        this.notifyItemRemoved(index);
        btn_save.setVisibility(View.VISIBLE);
        isUpdating = true;
    }

    public void setIsUpdate(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // viewType = 1 là item bình thường, else là item "THÊM ..."
        if (viewType == 1) {
            return new ServiceHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_service, parent, false));
        } else {
            return new ServiceLastHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_add, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 1) {
            StoreService storeService = storeServices.get(position);
            if (storeService != null) {
                ServiceHolder storeHolder = (ServiceHolder) holder;
                storeHolder.tv_service_name.setText(storeService.getServiceName());
                if (storeService.isActive()) {
                    storeHolder.tv_service_status.setText(ACTIVE_STATUS);
                    storeHolder.tv_service_status.setTextColor(activity.getColor(R.color.teal_700));
                    storeHolder.rl_all.setBackgroundColor(activity.getColor(R.color.teal_700_light));
                } else {
                    storeHolder.tv_service_status.setText(INACTIVE_STATUS);
                    storeHolder.tv_service_status.setTextColor(activity.getColor(R.color.red_dark));
                    storeHolder.rl_all.setBackgroundColor(activity.getColor(R.color.red_light));
                }
                storeHolder.tv_price.setText(String.valueOf(storeService.getPriceOfService()));

                if (bottomSheetBehavior != null) {
                    storeHolder.iv_remove.setOnClickListener(v -> ViewUtils.showDialogConfirm("Xác nhận xóa", storeService.getServiceName(), activity, is -> {
                        if (is) {
                            deleteItem(storeService);
                        }
                    }));

                    storeHolder.rl_all.setOnClickListener(v -> {
                        // Edit Store
                        Intent intent = new Intent(activity, ServiceInfoActivity.class);
                        String serviceStr = new Gson().toJson(storeService);
                        intent.putExtra("serviceStr", serviceStr);
                        activity.startActivityForResult(intent, 69);
                    });
                } else {
                    storeHolder.iv_remove.setVisibility(View.GONE);
                    storeHolder.cb_select.setVisibility(View.VISIBLE);

                    storeHolder.cb_select.setOnCheckedChangeListener((compoundButton, b) -> {
                        storeService.isSelect = compoundButton.isChecked();
                        selectedCount = storeService.isSelect ? selectedCount + 1 : selectedCount - 1;
                        if (selectedCount > 0) {
                            if (btn_save.getVisibility() != View.VISIBLE)
                                btn_save.setVisibility(View.VISIBLE);
                            btn_save.setText(String.format(Locale.getDefault(), "%s (%d/%d)", BTN_STYLE, selectedCount, isCustomer ? storeServices.size() : storeServices.size() - 1));
                        } else {
                            btn_save.setVisibility(View.INVISIBLE);
                        }
                    });

                    storeHolder.rl_all.setOnClickListener(v -> storeHolder.cb_select.setChecked(!storeHolder.cb_select.isChecked()));
                }
            }

        } else {
            ServiceLastHolder storeLastHolder = (ServiceLastHolder) holder;
            if (bottomSheetBehavior != null) {
                storeLastHolder.ll_add.setOnClickListener(v -> {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        if (btn_save.getVisibility() == View.VISIBLE) {
                            btn_save.setVisibility(View.GONE);
                        } else {
                            if (isUpdating) {
                                btn_save.setVisibility(View.VISIBLE);
                            }
                        }
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        if (isUpdating) {
                            btn_save.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } else {
                storeLastHolder.ll_add.setVisibility(View.GONE);
//                storeLastHolder.tv_add.setText("THÊM MỚI");
//                storeLastHolder.ll_add.setOnClickListener(v -> {
//                    // Mở activity thêm mới
//                    Intent intent = new Intent(activity, ServiceInfoActivity.class);
//                    activity.startActivityForResult(intent, 69);
//                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isCustomer ? 1 : position == this.storeServices.size() - 1 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return storeServices.size();
    }

    protected static class ServiceHolder extends RecyclerView.ViewHolder {
        private final TextView tv_service_name;
        private final TextView tv_service_status;
        private final TextView tv_price;
        private final ImageView iv_remove;
        private final CheckBox cb_select;
        private final RelativeLayout rl_all;

        public ServiceHolder(@NonNull View itemView) {
            super(itemView);
            tv_service_name = itemView.findViewById(R.id.tv_service_name);
            tv_service_status = itemView.findViewById(R.id.tv_service_status);
            tv_price = itemView.findViewById(R.id.tv_price);
            iv_remove = itemView.findViewById(R.id.iv_remove);
            cb_select = itemView.findViewById(R.id.cb_select);
            rl_all = itemView.findViewById(R.id.rl_all);
        }
    }

    protected static class ServiceLastHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ll_add;
        private final TextView tv_add;

        public ServiceLastHolder(@NonNull View itemView) {
            super(itemView);
            ll_add = itemView.findViewById(R.id.ll_add);
            tv_add = itemView.findViewById(R.id.tv_add);
        }
    }
}
