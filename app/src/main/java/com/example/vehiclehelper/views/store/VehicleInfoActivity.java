package com.example.vehiclehelper.views.store;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.helper.user.model.VehicleInfo;
import com.example.vehiclehelper.views.security.LoginPwdActivity;
import com.google.gson.Gson;

public class VehicleInfoActivity extends AppCompatActivity {
    private Activity activity;
    private EditText et_distributor, et_name, et_ounce, et_vehi_number;
    private TextView tv_warning;
    private LinearLayout ln_pbar;
    private Users users;
    private UserDAO userDAO;

    private VehicleInfo vehicleInfo;
    private String infoStr;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);
        activity = this;
        userDAO = new UserDAO(this);
        getWidgets();
        users = new Gson().fromJson(getIntent().getStringExtra("user"), Users.class);
        infoStr = getIntent().getStringExtra("info");
        if (infoStr != null) {
            vehicleInfo = new Gson().fromJson(infoStr, VehicleInfo.class);
            infoStr = getIntent().getStringExtra("uid");

            et_distributor.setText(vehicleInfo.getDistributor());
            et_name.setText(vehicleInfo.getName());
            et_ounce.setText(String.valueOf(vehicleInfo.getOunce()));
            et_vehi_number.setText(vehicleInfo.getNumber());
        }
    }

    public void getWidgets() {
        et_distributor = findViewById(R.id.et_distributor);
        et_name = findViewById(R.id.et_name);
        et_ounce = findViewById(R.id.et_ounce);
        et_vehi_number = findViewById(R.id.et_vehi_number);

        tv_warning = findViewById(R.id.tv_warning);

        ln_pbar = findViewById(R.id.ln_pbar);
    }

    public void setListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tv_warning.getVisibility() != View.GONE) {
                    tv_warning.setVisibility(View.GONE);
                }
            }
        };
        et_distributor.addTextChangedListener(textWatcher);
        et_name.addTextChangedListener(textWatcher);
        et_ounce.addTextChangedListener(textWatcher);
        et_vehi_number.addTextChangedListener(textWatcher);
    }

    public void back(View view) {
        finish();
    }

    public void done(View view) {
        String distributor = et_distributor.getText().toString();
        if (TextUtils.isEmpty(distributor)) {
            tv_warning.setVisibility(View.VISIBLE);
            return;
        }

        String name = et_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            tv_warning.setVisibility(View.VISIBLE);
            return;
        }

        String ounce = et_ounce.getText().toString();
        if (TextUtils.isEmpty(ounce)) {
            tv_warning.setVisibility(View.VISIBLE);
            return;
        }

        int ounceInt = Integer.parseInt(ounce);
        if (ounceInt < 10) {
            Toast.makeText(activity, "Phân khối không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String vehi_num = et_vehi_number.getText().toString();
        if (TextUtils.isEmpty(vehi_num)) {
            tv_warning.setVisibility(View.VISIBLE);
            return;
        }

        tv_warning.setVisibility(View.GONE);

        VehicleInfo vehicleInfo = new VehicleInfo();
        vehicleInfo.setDistributor(distributor);
        vehicleInfo.setName(name);
        vehicleInfo.setOunce(ounceInt);
        vehicleInfo.setNumber(vehi_num);
        // Show progress bar
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        if (users != null) {
            users.setVehicleInfo(vehicleInfo);
            userDAO.register(users, new UserDAO.IControlData() {
                @Override
                public void isSuccess(boolean is) {
                    if (is) {
                        Toast.makeText(activity, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        // Lưu thông tin tài khoản vào Session
                        SessionManager.getINSTANCE().createUserSession(users);
                        startActivity(new Intent(activity, LoginPwdActivity.class));
                        finish();
                    } else {
                        Toast.makeText(activity, "Có gì đó sai sai!!", Toast.LENGTH_SHORT).show();
                    }
                    // Hide progress bar
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                }
            });
        } else {
            // Kiểm tra coi có thay đổi gì không
            if (!vehicleInfo.toString().equals(this.vehicleInfo.toString())) {
                userDAO.changVehicleInfo(vehicleInfo, new UserDAO.IControlData() {
                    @Override
                    public void isSuccess(boolean is) {
                        ViewUtils.progressBarProcess(false, ln_pbar, activity);
                        Intent intent = new Intent();
                        intent.putExtra("info", new Gson().toJson(vehicleInfo));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            } else {
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                Toast.makeText(activity, "Không có gì thay đổi!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}