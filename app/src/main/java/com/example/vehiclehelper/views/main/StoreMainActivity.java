package com.example.vehiclehelper.views.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.helper.user.model.VehicleInfo;
import com.example.vehiclehelper.views.chat.MessageListActivity;
import com.example.vehiclehelper.views.map.NearByStoreActivity;
import com.example.vehiclehelper.views.security.LoginActivity;
import com.example.vehiclehelper.views.stats.StatsActivity;
import com.example.vehiclehelper.views.store.StoreManageActivity;
import com.example.vehiclehelper.views.store.StoreMsgManageActivity;
import com.example.vehiclehelper.views.store.VehicleInfoActivity;
import com.example.vehiclehelper.views.store_service.RequestServiceStoreManageActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.Objects;

public class StoreMainActivity extends AppCompatActivity {
    private final Activity activity = this;

    private TextView tv_name, tv_store_type, tv_distributor, tv_vehicle_name, tv_ounce, tv_vehi_num;
    private View iv_logout, iv_sos, iv_store_search;
    private LinearLayout ln_store, ln_msg, ln_history, ln_stats, ln_account;
    private RelativeLayout rl_vehicle_info, rl_chang_pass, rl_change_vehi_info;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private UserDAO userDAO;
    private Users users;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_main);

        userDAO = new UserDAO(activity);
        users = userDAO.getUserSession();
        sessionManager = SessionManager.getINSTANCE();
        if (users == null) {
            finish();
        } else {
            if (sessionManager.isForgotPasswordFlag()) {
                showDialogChangePassword();
            }
            getWidgets();
            setListeners();

            tv_name.setText(users.getName());
            if (users.type == Users.TYPE.CUSTOMER) {
                tv_store_type.setText("Tìm cửa hàng");
                iv_sos.setVisibility(View.VISIBLE);
                iv_store_search.setBackgroundResource(R.drawable.ic_baseline_search_24);

                ln_stats.setVisibility(View.GONE);

                initVehicleInfo(users.getVehicleInfo());
            } else {
                rl_vehicle_info.setVisibility(View.GONE);
                rl_change_vehi_info.setVisibility(View.GONE);
            }
        }

    }

    private void showDialogChangePassword() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_change_password, null);
        TextInputLayout til_current_pass = view.findViewById(R.id.til_current_pass);
        TextInputLayout til_new_pass = view.findViewById(R.id.til_new_pass);
        TextView tv_err = view.findViewById(R.id.tv_err);
        RelativeLayout rl_pbar = view.findViewById(R.id.rl_pbar);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();
        ad.setCancelable(false);

        // Listeners
        if (!sessionManager.isForgotPasswordFlag()) {
            btn_cancel.setOnClickListener(v -> {
                ad.dismiss();
            });
        } else {
            btn_cancel.setVisibility(View.GONE);
            Toast.makeText(activity, "Bạn vừa yêu cầu cấp lại mật khẩu\nĐể đảm bảo an toàn, bạn hãy đổi mật khẩu trước khi sử dụng ứng dụng.", Toast.LENGTH_LONG).show();
        }
        btn_confirm.setOnClickListener(v -> {
            // Kiểm tra dữ liệu hợp lệ
            String currentPass = Objects.requireNonNull(til_current_pass.getEditText()).getText().toString();
            if (TextUtils.isEmpty(currentPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.nhap_day_du_du_lieu);
                return;
            }
            String newPass = Objects.requireNonNull(til_new_pass.getEditText()).getText().toString();
            if (TextUtils.isEmpty(newPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.nhap_day_du_du_lieu);
            } else if (newPass.length() < 6) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.mat_khau_6_ky_tu);
            } else if (newPass.equals(currentPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.trung_mat_khau);
            } else {
                tv_err.setVisibility(View.GONE);
                ViewUtils.progressBarProcess(true, rl_pbar, activity);
                userDAO.changePassword(currentPass, newPass, new UserDAO.IControlData() {
                    @Override
                    public void isSuccess(boolean is) {
                        ViewUtils.progressBarProcess(false, rl_pbar, activity);
                        if (is) {
                            if (sessionManager.isForgotPasswordFlag()) {
                                sessionManager.setForgotPasswordFlag(false);
                            }
                            Toast.makeText(activity, "Cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            ad.dismiss();
                        } else {
                            tv_err.setVisibility(View.VISIBLE);
                            tv_err.setText(R.string.mat_khau_hien_tai_sai);
                        }
                    }
                });
            }


        });
        ad.show();
        ad.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    private void getWidgets() {
        tv_name = findViewById(R.id.tv_name);
        tv_store_type = findViewById(R.id.tv_store_type);
        tv_distributor = findViewById(R.id.tv_distributor);
        tv_vehicle_name = findViewById(R.id.tv_vehicle_name);
        tv_ounce = findViewById(R.id.tv_ounce);
        tv_vehi_num = findViewById(R.id.tv_vehi_num);

        iv_logout = findViewById(R.id.iv_logout);
        iv_sos = findViewById(R.id.iv_sos);
        iv_store_search = findViewById(R.id.iv_store_search);

        ln_store = findViewById(R.id.ln_store);
        ln_msg = findViewById(R.id.ln_msg);
        ln_history = findViewById(R.id.ln_history);
        ln_stats = findViewById(R.id.ln_stats);
        ln_account = findViewById(R.id.ln_account);
        rl_vehicle_info = findViewById(R.id.rl_vehicle_info);
        rl_chang_pass = findViewById(R.id.rl_chang_pass);
        rl_change_vehi_info = findViewById(R.id.rl_change_vehi_info);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.ll_bt_s));
    }

    private void setListeners() {
        iv_logout.setOnClickListener(v -> {
            startActivity(new Intent(activity, LoginActivity.class));
            userDAO.clearSession();
            finish();
        });

        iv_sos.setOnClickListener(v -> {
            Intent intent = new Intent(activity, NearByStoreActivity.class);
            intent.putExtra("fromSOS", true);
            startActivity(intent);
        });

        rl_chang_pass.setOnClickListener(v -> showDialogChangePassword());
        rl_change_vehi_info.setOnClickListener(v -> {
            Intent intent = new Intent(activity, VehicleInfoActivity.class);
            intent.putExtra("info", new Gson().toJson(users.getVehicleInfo()));
            intent.putExtra("uid", users.getUid());
            startActivityForResult(intent, 69);
        });

        // Main action
        @SuppressLint("NonConstantResourceId")
        View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.ln_store:
                    if (users.type == Users.TYPE.CUSTOMER) {
                        startActivity(new Intent(activity, NearByStoreActivity.class));
                    } else {
                        startActivity(new Intent(activity, StoreManageActivity.class));
                    }
                    break;
                case R.id.ln_msg:
                    if (users.type == Users.TYPE.CUSTOMER) {
                        Intent intent = new Intent(activity, MessageListActivity.class);
                        intent.putExtra("id", users.getUid());
                        intent.putExtra("name", users.getName());
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(activity, StoreMsgManageActivity.class));
                    }
                    break;
                case R.id.ln_history:
                    startActivity(new Intent(activity, RequestServiceStoreManageActivity.class));
                    break;
                case R.id.ln_stats:
                    Intent intent = new Intent(activity, StatsActivity.class);
                    intent.putExtra("userID", users.getUid());
                    startActivity(intent);
                    break;
                case R.id.ln_account:
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    } else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    break;
                default:
                    break;
            }
        };

        ln_store.setOnClickListener(onClickListener);
        ln_msg.setOnClickListener(onClickListener);
        ln_history.setOnClickListener(onClickListener);
        ln_account.setOnClickListener(onClickListener);
        ln_stats.setOnClickListener(onClickListener);
    }

    public void onHideBottomSheet(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void initVehicleInfo(VehicleInfo vehicleInfo) {
        tv_distributor.setText(vehicleInfo.getDistributor());
        tv_vehicle_name.setText(vehicleInfo.getName());
        tv_ounce.setText(String.valueOf(vehicleInfo.getOunce()));
        tv_vehi_num.setText(vehicleInfo.getNumber());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 69 && resultCode == RESULT_OK && data != null) {
            Toast.makeText(activity, "Cập nhật thông tin xe thành công", Toast.LENGTH_SHORT).show();
            VehicleInfo vehicleInfo = new Gson().fromJson(data.getStringExtra("info"), VehicleInfo.class);
            initVehicleInfo(vehicleInfo);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            users.setVehicleInfo(vehicleInfo);
        }
    }
}