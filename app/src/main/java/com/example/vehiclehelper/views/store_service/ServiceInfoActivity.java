package com.example.vehiclehelper.views.store_service;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.store_service.model.StoreService;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

public class ServiceInfoActivity extends AppCompatActivity {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String INACTIVE_STATUS = "TẠM DỪNG";
    //    private Activity activity;
//    private LinearLayout ln_pbar;
    private View v_clear_service_name, v_clear_price;
    private EditText et_service_name, et_price;
    private TextView tv_back, tv_warning, tv_service_status;
    private Button btn_done;
    private SwitchMaterial sw_status;

    private String serviceStr;
    private StoreService storeService;

//    private StoreServiceDAO storeServiceDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_info);

        getWidgets();
        setListeners();

        serviceStr = getIntent().getStringExtra("serviceStr");
        if (serviceStr != null) {
            storeService = new Gson().fromJson(serviceStr, StoreService.class);

            et_service_name.setText(storeService.getServiceName());
            et_price.setText(String.valueOf(storeService.getPriceOfService()));
            sw_status.setChecked(storeService.isActive());
            if (storeService.isActive()) {
                tv_service_status.setText(ACTIVE_STATUS);
                tv_service_status.setTextColor(getColor(R.color.teal_700));
            } else {
                tv_service_status.setText(INACTIVE_STATUS);
                tv_service_status.setTextColor(getColor(R.color.red_dark));
            }
        }

    }


    private void getWidgets() {
        // EditText
        et_service_name = findViewById(R.id.et_service_name);
        et_price = findViewById(R.id.et_price);

        // View
        v_clear_service_name = findViewById(R.id.v_clear_service_name);
        v_clear_price = findViewById(R.id.v_clear_price);

        // TextView
        tv_back = findViewById(R.id.tv_back);
        tv_warning = findViewById(R.id.tv_warning);
        tv_service_status = findViewById(R.id.tv_service_status);

        // Button
        btn_done = findViewById(R.id.btn_done);
        // ProgressBar
//        ln_pbar = findViewById(R.id.ln_pbar);

        // Switch
        sw_status = findViewById(R.id.sw_status);
    }

    private void setListeners() {
        tv_back.setOnClickListener(v -> finish());

        // TextView
        v_clear_service_name.setOnClickListener(v -> et_service_name.setText(null));

        v_clear_price.setOnClickListener(v -> et_price.setText("0"));

        sw_status.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                tv_service_status.setText(ACTIVE_STATUS);
                tv_service_status.setTextColor(getColor(R.color.teal_700));
            } else {
                tv_service_status.setText(INACTIVE_STATUS);
                tv_service_status.setTextColor(getColor(R.color.red_dark));
            }
        });

        btn_done.setOnClickListener(v -> {
            String serviceName = et_service_name.getText().toString();
            if (TextUtils.isEmpty(serviceName)) {
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }

            String priceStr = et_price.getText().toString();
            if (TextUtils.isEmpty(priceStr)) {
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }
            int priceD;
            try {
                priceD = Integer.parseInt(priceStr);
                tv_warning.setText(getText(R.string.non_empty));
                if (priceD <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                tv_warning.setText(getText(R.string.non_valid_price));
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }

            StoreService storeService1 = new StoreService();
            if (storeService == null) {
                storeService1.setId(System.currentTimeMillis() + "");
            } else {
                storeService1.setId(storeService.getId());
                storeService1.setOwner(storeService.getOwner());
                storeService1.setStoreIDs(storeService.getStoreIDs());
            }


            storeService1.setServiceName(serviceName);
            storeService1.setPriceOfService(priceD);
            storeService1.setActive(sw_status.isChecked());

            if (isDiffStoreService(storeService1)) {
                Intent intent = new Intent();
                intent.putExtra("resultReturn", new Gson().toJson(storeService1));
                intent.putExtra("update", !TextUtils.isEmpty(serviceStr));
                setResult(RESULT_OK, intent);
            }
            finish();

        });

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tv_warning.getVisibility() == View.VISIBLE)
                    tv_warning.setVisibility(View.GONE);
            }
        };

        et_service_name.addTextChangedListener(textWatcher);
        et_price.addTextChangedListener(textWatcher);

    }

    private boolean isDiffStoreService(StoreService storeService) {
        if (TextUtils.isEmpty(serviceStr) || TextUtils.isEmpty(storeService.getId())) return true;

        return !new Gson().toJson(storeService).equals(serviceStr);
    }

}