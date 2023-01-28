package com.example.vehiclehelper.views.security;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.store.StoreInfoActivity;
import com.example.vehiclehelper.views.store.VehicleInfoActivity;
import com.google.gson.Gson;

public class RegisterActivity extends AppCompatActivity {
    private final Activity activity = this;
    private EditText et_name, et_pass;
    private TextView tv_warning, tv_change_phone;
    private Button btn_done;
    private CheckBox cb_user_type;
    private View clear_name, clear_pass;
    private LinearLayout ln_pbar;

    private String phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.phoneNumber = getPhoneNumber();
        if (TextUtils.isEmpty(phoneNumber)) return;

        getWidgets();
        setListeners();
    }

    private String getPhoneNumber() {
        return getIntent().getExtras().getString(getString(R.string.phone_number_otp));
    }

    private void getWidgets() {
        et_name = findViewById(R.id.et_name);
        et_pass = findViewById(R.id.et_pass);

        tv_warning = findViewById(R.id.tv_warning);
        tv_change_phone = findViewById(R.id.tv_change_phone);
        clear_name = findViewById(R.id.clear_name);
        clear_pass = findViewById(R.id.clear_pass);

        cb_user_type = findViewById(R.id.cb_user_type);
        btn_done = findViewById(R.id.btn_done);

        ln_pbar = findViewById(R.id.ln_pbar);
    }

    private void setListeners() {
        // Button
        btn_done.setOnClickListener(v -> {
            // Get Name
            String name = et_name.getText().toString();

            // Check valid
            if (TextUtils.isEmpty(name)) {
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }

            String pass = et_pass.getText().toString();
            if (TextUtils.isEmpty(pass)) {
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }
            tv_warning.setVisibility(View.GONE);

            Users users = new Users();
            users.setUid(phoneNumber);
            users.setPwd(pass);
            users.setName(name);

            String userStr = new Gson().toJson(users);
            if (cb_user_type.isChecked()) {
                /**
                 * Nếu là cửa hàng thì chuyển sang trang thông tin cửa hàng
                 */

                Intent intent = new Intent(activity, StoreInfoActivity.class);
                intent.putExtra("user", userStr);
                startActivity(intent);
            } else {
                // Chuyển sang trang điền thông tin xe
                Intent intent = new Intent(activity, VehicleInfoActivity.class);
                intent.putExtra("user", userStr);
                startActivity(intent);
            }
        });

        // EditText
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    clear_name.setVisibility(View.INVISIBLE);
                    tv_warning.setVisibility(View.VISIBLE);
                    ViewUtils.showSoftKeyboard(et_name);
                } else {
                    clear_name.setVisibility(View.VISIBLE);
                    tv_warning.setVisibility(View.GONE);
                }
            }
        });

        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    clear_pass.setVisibility(View.INVISIBLE);
                    tv_warning.setVisibility(View.VISIBLE);
                    ViewUtils.showSoftKeyboard(et_pass);
                } else {
                    clear_pass.setVisibility(View.VISIBLE);
                    tv_warning.setVisibility(View.GONE);
                }
            }
        });

        // TextView
        clear_name.setOnClickListener(v -> et_name.setText(null));

        clear_pass.setOnClickListener(v -> et_pass.setText(null));

        tv_change_phone.setOnClickListener(v -> {
            startActivity(new Intent(activity, LoginActivity.class));
            finish();
        });

        // Checkbox
        cb_user_type.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btn_done.setText(R.string.tiep_tuc);
            } else {
                btn_done.setText(R.string.hoan_thanh);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ViewUtils.progressBarProcess(false, ln_pbar, this);
    }

}