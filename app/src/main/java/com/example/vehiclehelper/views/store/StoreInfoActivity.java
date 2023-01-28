package com.example.vehiclehelper.views.store;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.MyLatLng;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store.model.Store;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.chat.ChatActivity;
import com.example.vehiclehelper.views.map.MapActivity;
import com.example.vehiclehelper.views.security.LoginPwdActivity;
import com.example.vehiclehelper.views.store_service.ServiceManageActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StoreInfoActivity extends AppCompatActivity {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String INACTIVE_STATUS = "TẠM DỪNG";
    private Activity activity;
    private LinearLayout ln_pbar, ll_status;
    private RelativeLayout rl_service;
    private View v_edit_address, v_clear_contact, v_clear_store_name, v_move_address;
    private EditText et_address, et_contact, et_store_name, et_service;
    private TextView tv_back, tv_warning, tv_store_status, tv_title, tv_call;
    private Button btn_done;
    private SwitchMaterial sw_status;

    private UserDAO userDAO;
    private StoreDAO storeDAO;

    private boolean isFromStoreManage;

    private Store store = new Store();
    private String storeStr;
    private boolean fromCustomer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_info);

        // Add
        isFromStoreManage = getIntent().getBooleanExtra("store_manage", false);
        fromCustomer = getIntent().getBooleanExtra("from_customer", false);
        activity = this;

        getWidgets();
        setListeners();

        if (!isFromStoreManage && !fromCustomer) {
            userDAO = new UserDAO();
            storeDAO = new StoreDAO();
        } else if (fromCustomer) {
            // Được mở từ
            tv_title.setText("THÔNG TIN CỬA HÀNG");
            ll_status.setVisibility(View.GONE);
            btn_done.setText("Nhắn tin");
            et_service.setHint("CHỌN DỊCH VỤ");

            et_store_name.setCursorVisible(false);
            et_store_name.setFocusable(false);
            et_contact.setCursorVisible(false);
            et_contact.setFocusable(false);

            tv_call.setVisibility(View.VISIBLE);
            v_clear_contact.setVisibility(View.GONE);

            v_move_address.setVisibility(View.VISIBLE);
            v_edit_address.setVisibility(View.GONE);
        }

        storeStr = getIntent().getStringExtra("storeStr");
        if (!TextUtils.isEmpty(storeStr)) {
            et_service.setVisibility(View.VISIBLE);
            store = new Gson().fromJson(storeStr, Store.class);
            if (store != null) {
                et_store_name.setText(store.getStoreName());
                et_contact.setText(store.getPhone());
                et_address.setText(store.getAddressName());

                if (ll_status.getVisibility() != View.GONE) {
                    sw_status.setChecked(store.isActive());

                    if (store.isActive()) {
                        tv_store_status.setText(ACTIVE_STATUS);
                        tv_store_status.setTextColor(getColor(R.color.teal_700));
                    } else {
                        tv_store_status.setText(INACTIVE_STATUS);
                        tv_store_status.setTextColor(getColor(R.color.red_dark));
                    }
                }
            }
        } else {
            rl_service.setVisibility(View.GONE);
        }
    }

    private void getWidgets() {
        // EditText
        et_address = findViewById(R.id.et_address);
        et_contact = findViewById(R.id.et_contact);
        et_store_name = findViewById(R.id.et_store_name);
        et_service = findViewById(R.id.et_service);

        // View
        v_edit_address = findViewById(R.id.v_edit_address);
        v_clear_contact = findViewById(R.id.v_clear_contact);
        v_clear_store_name = findViewById(R.id.v_clear_store_name);
        v_move_address = findViewById(R.id.v_move_address);

        // TextView
        tv_back = findViewById(R.id.tv_back);
        tv_warning = findViewById(R.id.tv_warning);
        tv_store_status = findViewById(R.id.tv_store_status);
        tv_title = findViewById(R.id.tv_title);
        tv_call = findViewById(R.id.tv_call);

        // Button
        btn_done = findViewById(R.id.btn_done);
        // ProgressBar
        ln_pbar = findViewById(R.id.ln_pbar);
        ll_status = findViewById(R.id.ll_status);

        rl_service = findViewById(R.id.rl_service);

        // Switch
        sw_status = findViewById(R.id.sw_status);
    }

    private void setListeners() {
        et_address.setOnClickListener(view -> {
            if (et_address.getText().length() == 0) {
                startActivityForResult(new Intent(activity, MapActivity.class), 69);
            } else {
                if (fromCustomer) {
                    ViewUtils.moveMap(activity, store.getLatLng().lat, store.getLatLng().lng);
                } else {
                    Toast.makeText(activity, et_address.getText(), Toast.LENGTH_LONG).show();
                }
            }
        });

        et_service.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ServiceManageActivity.class);
            intent.putExtra("storeStr", storeStr);
            startActivityForResult(intent, 69);
        });

        v_move_address.setOnClickListener(v -> {
            ViewUtils.moveMap(activity, store.getLatLng().lat, store.getLatLng().lng);
        });

        v_edit_address.setOnClickListener(v -> startActivityForResult(new Intent(activity, MapActivity.class), 69));

        tv_back.setOnClickListener(v -> finish());

        tv_call.setOnClickListener(v -> ViewUtils.call(activity, et_contact.getText().toString()));

        // TextView
        v_clear_contact.setOnClickListener(v -> et_contact.setText(null));

        v_clear_store_name.setOnClickListener(v -> et_store_name.setText(null));

        sw_status.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                tv_store_status.setText(ACTIVE_STATUS);
                tv_store_status.setTextColor(getColor(R.color.teal_700));
            } else {
                tv_store_status.setText(INACTIVE_STATUS);
                tv_store_status.setTextColor(getColor(R.color.red_dark));
            }
        });

        btn_done.setOnClickListener(v -> {
            // Nhắn tin
            if (fromCustomer) {
                Intent intent = new Intent(activity, ChatActivity.class);
                Users users = UserDAO.getUser();
                intent.putExtra("senderID", users.getUid());
                intent.putExtra("owner", store.getOwner());
                intent.putExtra("receiveID", store.getStoreID());
                intent.putExtra("cusName", users.getName());
                intent.putExtra("storeName", store.getStoreName());
                intent.putExtra("receiverName", store.getStoreName());
                startActivity(intent);
            } else { // Lưu
                // Kiểm tra tính hợp lệ của dữ liệu
                String storeName = et_store_name.getText().toString();
                if (TextUtils.isEmpty(storeName)) {
                    tv_warning.setVisibility(View.VISIBLE);
                    return;
                }
                String contact = et_contact.getText().toString();
                if (TextUtils.isEmpty(contact)) {
                    tv_warning.setVisibility(View.VISIBLE);
                    return;
                }

                String address = et_address.getText().toString();
                if (TextUtils.isEmpty(address)) {
                    tv_warning.setVisibility(View.VISIBLE);
                    return;
                }

                boolean isActive = sw_status.isChecked();

                tv_warning.setVisibility(View.GONE);

                if (isFromStoreManage) {
                    if (TextUtils.isEmpty(storeStr)) {
                        String storeID = System.currentTimeMillis() + "";
                        store.setStoreID(storeID);
                        store.setCreateDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
                    }
                    store.setStoreName(storeName);
                    store.setPhone(contact);
                    store.setAddressName(address);
                    store.setActive(isActive);

                    if (isDiffStore(store)) {
                        String dataStr = new Gson().toJson(store);
                        Intent intent = new Intent();
                        intent.putExtra("store", dataStr);
                        intent.putExtra("update", !TextUtils.isEmpty(storeStr));
                        setResult(RESULT_OK, intent);
                    }

                    finish();

                } else {
                    String userStr = getIntent().getStringExtra("user");
                    if (userStr != null) {
                        try {
                            Users users = new Gson().fromJson(userStr, Users.class);

                            String storeID = System.currentTimeMillis() + "";
                            store.setStoreID(storeID);
                            store.setStoreName(storeName);
                            store.setPhone(contact);
                            store.setAddressName(address);
                            store.setCreateDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
                            store.setOwner(users.getUid());
                            store.setActive(isActive);
                            users.type = Users.TYPE.STORE;

                            // Push to Firebase
                            // Show progress bar
                            ViewUtils.progressBarProcess(true, ln_pbar, activity);
                            userDAO.register(users, new UserDAO.IControlData() {
                                @Override
                                public void isSuccess(boolean is) {
                                    if (is) {
                                        storeDAO.putStore(users.getUid(), store, new StoreDAO.IControlData() {
                                            @Override
                                            public void isSuccess(boolean is) {
                                                if (is) {
                                                    // Lưu thông tin tài khoản vào Session
                                                    SessionManager.getINSTANCE().createUserSession(users);
                                                    startActivity(new Intent(activity, LoginPwdActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(activity, "Lỗi, không đăng ký được cửa hàng!!", Toast.LENGTH_SHORT).show();
                                                }
                                                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                            }
                                        });
                                    } else {
                                        Toast.makeText(activity, "Lỗi, không đăng ký được user!!", Toast.LENGTH_SHORT).show();
                                        ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                    }
                                }
                            });

                        } catch (Exception e) {
                            Toast.makeText(activity, "Có gì đó sai sai!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private boolean isDiffStore(Store store1) {
        if (TextUtils.isEmpty(storeStr) || TextUtils.isEmpty(store1.getStoreID())) return true;

        return !new Gson().toJson(store1).equals(storeStr);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 113 && permissions[0].equals(android.Manifest.permission.CALL_PHONE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + et_contact.getText())));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 69 && data != null) {
            if (resultCode == RESULT_OK) {
                String rs = data.getStringExtra("result");
                String[] arr = rs.split("\\|");
                try {
                    MyLatLng storeLocation = new MyLatLng();
                    storeLocation.lat = Double.parseDouble(arr[0]);
                    storeLocation.lng = Double.parseDouble(arr[1]);
                    store.setLatLng(storeLocation);

                    et_address.setText(arr[2]);
                } catch (Exception e) {
                    et_address.setText(null);
                    store.setLatLng(null);
                }
            }
        }

    }

}