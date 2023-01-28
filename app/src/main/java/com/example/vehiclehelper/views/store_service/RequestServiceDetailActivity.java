package com.example.vehiclehelper.views.store_service;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.fcm.model.NotificationModel;
import com.example.vehiclehelper.helper.fcm.service.NotificationService;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store_service.dao.RequestServiceDAO;
import com.example.vehiclehelper.helper.store_service.model.InfoName;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVRqServiceDetailAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

public class RequestServiceDetailActivity extends AppCompatActivity {
    private Activity activity;
    private TextView tv_store_name;
    private RecyclerView rv_rqServices;
    private LinearLayout ln_pbar;
    private RelativeLayout rl_done, rl_cancel, rl_map;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private RequestService requestService;
    private RequestServiceDAO requestServiceDAO;
    private boolean userIsStore;
    private UserDAO userDAO;
    private StoreDAO storeDAO;
    private InfoName infoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service_detail);
        activity = this;
        requestServiceDAO = new RequestServiceDAO();
        userDAO = new UserDAO();
        storeDAO = new StoreDAO();
        userIsStore = UserDAO.getUser().type == Users.TYPE.STORE;
        getWidgets();
        setListeners();
        initRV();
    }

    private final RVRqServiceDetailAdapter.IControlData iControlData = (behavior, requestService) -> {
        this.requestService = requestService;
        if (bottomSheetBehavior.getState() != behavior) {
            bottomSheetBehavior.setState(behavior == 3 ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
        }
    };

    private RVRqServiceDetailAdapter detailAdapter;
    private boolean isChange;

    private void initRV() {
        // StoreID này đọc từ notification nè
        String storeID = getIntent().getStringExtra("storeID");
        if (TextUtils.isEmpty(storeID)) {
            RequestServiceManage requestServiceManage = new Gson().fromJson(getIntent().getStringExtra("requestServiceManage"), RequestServiceManage.class);
            if (requestServiceManage != null) {
                infoName = requestServiceManage.getInfoName();
                tv_store_name.setText(requestServiceManage.getInfoName().getStoreName());
                detailAdapter = new RVRqServiceDetailAdapter(activity, requestServiceManage, iControlData);
                rv_rqServices.setAdapter(detailAdapter);
            }
        } else {
            ViewUtils.progressBarProcess(true, ln_pbar, activity);
            requestServiceDAO.getRequestServiceManageListByStoreIDList(Collections.singletonList(storeID), new RequestServiceDAO.IControlData() {
                @Override
                public void requestServiceManageList(List<RequestServiceManage> requestServiceManages) {
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                    NotificationManagerCompat.from(activity).cancel(getIntent().getIntExtra("notifyID", 0));
                    if (requestServiceManages.isEmpty()) {
                        Toast.makeText(activity, "???", Toast.LENGTH_SHORT).show();
                    } else {
                        RequestServiceManage requestServiceManage = requestServiceManages.get(0);
                        infoName = requestServiceManage.getInfoName();
                        tv_store_name.setText(requestServiceManage.getInfoName().getStoreName());
                        detailAdapter = new RVRqServiceDetailAdapter(activity, requestServiceManage, iControlData);
                        rv_rqServices.setAdapter(detailAdapter);
                    }
                }
            });
        }
    }

    private void getWidgets() {
        rv_rqServices = findViewById(R.id.rv_rqServices);
        tv_store_name = findViewById(R.id.tv_store_name);

        ln_pbar = findViewById(R.id.ln_pbar);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.ll_bt_s));

        rl_done = findViewById(R.id.rl_done);
        rl_map = findViewById(R.id.rl_map);
        if (UserDAO.getUser().type == Users.TYPE.CUSTOMER) {
            rl_done.setVisibility(View.GONE);
            rl_map.setVisibility(View.GONE);
        }
        rl_cancel = findViewById(R.id.rl_cancel);
    }

    private void setListeners() {
        rl_done.setOnClickListener(v -> {
            if (requestService != null) {
                changeStatus(RequestService.RSStatus.DONE.name(), requestService.getId());
            }
        });
        rl_cancel.setOnClickListener(v -> {
            if (requestService != null) {
                changeStatus(RequestService.RSStatus.CANCEL.name(), requestService.getId());
            }
        });
        rl_map.setOnClickListener(v -> {
            if (requestService != null) {
                ViewUtils.moveMap(activity, requestService.getLat(), requestService.getLng());
            }
        });
    }

    private void changeStatus(String status, String id) {
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        int lastIndex = id.lastIndexOf("_");
        requestServiceDAO.changeStatus(id.substring(0, lastIndex), id, status, new RequestServiceDAO.IControlData() {
            @Override
            public void is(boolean is) {
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                detailAdapter.updateStatus(id, status);
                Toast.makeText(activity, "Hoàn thành yêu cầu", Toast.LENGTH_SHORT).show();
                hideBts();
                isChange = true;
                sendNotify(status.equals(RequestService.RSStatus.DONE.name()));
            }
        });
    }

    private void sendNotify(boolean isDone) {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.data = new NotificationModel.Data();
        notificationModel.data.group = NotificationService.STORE_SERVICE_GROUP;
        notificationModel.data.content = requestService.getContent();
        notificationModel.data.senderName = "Dịch vụ đã " + (isDone ? "HOÀN THÀNH \n(đánh giá)" : "BỊ HỦY");
        // Nếu là đơn HOÀN THÀNH mới có đánh giá
        if (isDone) {
            notificationModel.data.storeName = infoName.getStoreName();
            notificationModel.data.cusName = infoName.getCusName();
            notificationModel.data.senderID = UserDAO.getUser().getUid();
            notificationModel.data.receiverID = requestService.getId().split("_")[1];
        }
        notificationModel.data.isRating = isDone ? "1" : "0";

        if (userIsStore) {
            // Gửi noti đến người đặt dịch vụ
            // cusID_StoreID_Timestamps
            String uid = requestService.getId().split("_")[0];
            userDAO.sendNotify(uid, notificationModel);
        } else {
            String storeID = requestService.getId().split("_")[1];
            // Tìm owner từ storeID
            storeDAO.getOwnerIDByStoreID(storeID, new StoreDAO.IControlData() {
                @Override
                public void ownerID(String ownerID) {
                    userDAO.sendNotify(ownerID, notificationModel);
                }
            });
        }

    }

    public void back(View view) {
        if (isChange) {
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isChange) {
            setResult(RESULT_OK);
        }
    }

    public void onHideBottomSheet(View view) {
        hideBts();
    }

    private void hideBts() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        detailAdapter.clearSelected();
    }
}