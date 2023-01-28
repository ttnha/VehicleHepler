package com.example.vehiclehelper.views.store_service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store_service.dao.RequestServiceDAO;
import com.example.vehiclehelper.helper.store_service.model.RequestServiceManage;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVRqServiceStoreManageAdapter;

import java.util.List;

public class RequestServiceStoreManageActivity extends AppCompatActivity {
    private Activity activity;
    private TextView tv_empty;
    private LinearLayout ln_pbar;
    private RecyclerView rv_rqServices;

    private RequestServiceDAO requestServiceDAO;
    private StoreDAO storeDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service_store_manage);
        activity = this;
        requestServiceDAO = new RequestServiceDAO();
        storeDAO = new StoreDAO();
        getWidgets();
        initRV();

    }

    private void initRV() {
        Users users = UserDAO.getUser();
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        if (users.type == Users.TYPE.CUSTOMER) {
            requestServiceDAO.getRequestServiceManageListByCusID(users.getUid(), new RequestServiceDAO.IControlData() {
                @Override
                public void requestServiceManageList(List<RequestServiceManage> requestServiceManages) {
                    if (requestServiceManages.isEmpty()) {
                        tv_empty.setVisibility(View.VISIBLE);
                    } else {
                        tv_empty.setVisibility(View.GONE);

                        RVRqServiceStoreManageAdapter rv = new RVRqServiceStoreManageAdapter(activity, requestServiceManages);
                        rv_rqServices.setAdapter(rv);
                    }
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                }
            });
        } else {
            storeDAO.getStoreIDByOwner(users.getUid(), new StoreDAO.IControlData() {
                @Override
                public void storeIDList(List<String> storeIDList) {
                    if (storeIDList.isEmpty()) {
                        tv_empty.setVisibility(View.VISIBLE);
                        ViewUtils.progressBarProcess(false, ln_pbar, activity);
                    } else {
                        requestServiceDAO.getRequestServiceManageListByStoreIDList(storeIDList, new RequestServiceDAO.IControlData() {
                            @Override
                            public void requestServiceManageList(List<RequestServiceManage> requestServiceManages) {
                                if (requestServiceManages.isEmpty()) {
                                    tv_empty.setVisibility(View.VISIBLE);
                                } else {
                                    tv_empty.setVisibility(View.GONE);

                                    RVRqServiceStoreManageAdapter rv = new RVRqServiceStoreManageAdapter(activity, requestServiceManages);
                                    rv_rqServices.setAdapter(rv);
                                }
                                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 69 && resultCode == RESULT_OK) {
            initRV();
        }
    }

    private void getWidgets() {
        tv_empty = findViewById(R.id.tv_empty);
        rv_rqServices = findViewById(R.id.rv_rqServices);
        ln_pbar = findViewById(R.id.ln_pbar);
    }

    public void back(View view) {
        finish();
    }
}