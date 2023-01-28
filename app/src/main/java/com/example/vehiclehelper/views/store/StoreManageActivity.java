package com.example.vehiclehelper.views.store;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store.model.Store;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVStoreAdapter;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

public class StoreManageActivity extends AppCompatActivity {
    private final Activity activity = this;
    private RecyclerView rv_stores;
    private LinearLayout ln_pbar;
    private TextView tv_empty;
    private ImageView iv_back;
    private Button btn_save;

    private StoreDAO storeDAO;

    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_manage);

        users = UserDAO.getUser();
        if (users == null) {
            finish();
        } else {
            storeDAO = new StoreDAO();

            getWidgets();
            setListeners();
            initStoreList();
        }
    }


    private void getWidgets() {
        rv_stores = findViewById(R.id.rv_stores);

        ln_pbar = findViewById(R.id.ln_pbar);

        tv_empty = findViewById(R.id.tv_empty);

        iv_back = findViewById(R.id.iv_back);

        btn_save = findViewById(R.id.btn_save);
    }

    private void setListeners() {
        iv_back.setOnClickListener(view -> finish());

        btn_save.setOnClickListener(v -> {
            if (storeListTmp.isEmpty()) {
                btn_save.setVisibility(View.INVISIBLE);
                return;
            }
            ViewUtils.progressBarProcess(true, ln_pbar, activity);
            storeDAO.updateStores(storeListTmp, users.getUid(), new StoreDAO.IControlData() {
                @Override
                public void isSuccess(boolean is) {
                    Toast.makeText(activity, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                    if (storeListTmp.size() > 1) {
                        tv_empty.setVisibility(View.GONE);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                    btn_save.setVisibility(View.INVISIBLE);
                }
            });
        });
    }

    private List<Store> storeListTmp = new LinkedList<>();
    private RVStoreAdapter rv_StoresAdapter;

    private void initStoreList() {
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        rv_stores.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv_StoresAdapter = new RVStoreAdapter(activity);
        storeDAO.getStoreList(users.getUid(), new StoreDAO.IControlData() {
            @Override
            public void storeList(List<Store> storeList) {
                if (!storeList.isEmpty()) {
                    tv_empty.setVisibility(View.GONE);
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }

                storeListTmp = storeList;
                rv_StoresAdapter.setStoreList(storeListTmp);

                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            }
        });
        rv_stores.setAdapter(rv_StoresAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 69 && data != null) {
            if (resultCode == RESULT_OK) {
                String rs = data.getStringExtra("store");
                if (rs != null) {
                    Store store = new Gson().fromJson(rs, Store.class);
                    store.isUpdate = true;
                    // Update
                    boolean updateItem = data.getBooleanExtra("update", false);
                    if (updateItem) {
                        rv_StoresAdapter.updateItem(store);
                    } else {
                        store.setOwner(users.getUid());
                        rv_StoresAdapter.insertItem(store);
                    }
                    tv_empty.setVisibility(View.GONE);
                    btn_save.setVisibility(View.VISIBLE);
                }
            }
        }

    }

}