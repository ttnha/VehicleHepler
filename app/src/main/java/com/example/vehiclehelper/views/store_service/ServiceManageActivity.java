package com.example.vehiclehelper.views.store_service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.chat.dao.ChatDAO;
import com.example.vehiclehelper.helper.chat.model.MessageDetail;
import com.example.vehiclehelper.helper.common.Utils;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.fcm.model.NotificationModel;
import com.example.vehiclehelper.helper.fcm.service.NotificationService;
import com.example.vehiclehelper.helper.store.dao.StoreDAO;
import com.example.vehiclehelper.helper.store.model.Store;
import com.example.vehiclehelper.helper.store_service.dao.RequestServiceDAO;
import com.example.vehiclehelper.helper.store_service.dao.StoreServiceDAO;
import com.example.vehiclehelper.helper.store_service.model.InfoName;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.helper.store_service.model.StoreService;
import com.example.vehiclehelper.helper.user.dao.UserDAO;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.views.adapter.RVServiceAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceManageActivity extends AppCompatActivity {
    private final Activity activity = this;
    private RecyclerView rv_services;
    private LinearLayout ln_pbar, ll_bt_s;
    private RelativeLayout rl_add_history, rl_add_new;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView tv_empty, tv_info;
    private ImageView iv_back;
    private Button btn_save;

    private StoreServiceDAO storeServiceDAO;
    private StoreDAO storeDAO;
    private UserDAO userDAO;
    private RequestServiceDAO requestServiceDAO;
    private ChatDAO chatDAO;
    private Users users;
    private Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_manage);

        getWidgets();
        storeServiceDAO = new StoreServiceDAO();
        users = UserDAO.getUser();

        String storeStr = getIntent().getStringExtra("storeStr");
        if (storeStr != null) {
            store = new Gson().fromJson(storeStr, Store.class);
            tv_info.setText(initStoreInfo());
            if (users.type == Users.TYPE.CUSTOMER) {
                btn_save.setText("G???I Y??U C???U");
                storeDAO = new StoreDAO();
                userDAO = new UserDAO(activity);
                requestServiceDAO = new RequestServiceDAO();
                chatDAO = new ChatDAO(Utils.buildSessionChatID(users.getUid(), store.getStoreID()), users.getName(), store.getStoreName());
            } else {
                initBottomSheet();
            }
        } else {
            tv_info.setVisibility(View.GONE);
            // M??? t??? th???ng th??m d???ch v??? c?? s???n
            String currentServiceIDs = getIntent().getStringExtra("currentServiceIDs");
            if (!TextUtils.isEmpty(currentServiceIDs)) {
                this.currentServiceIDs = new Gson().fromJson(currentServiceIDs, new TypeToken<List<String>>() {
                }.getType());
                store = new Store();
                store.setStoreID(getIntent().getStringExtra("storeID"));
            }
        }

        setListeners();
        initServiceList();

    }

    private String initStoreInfo() {
        return store.getStoreName() + " - " + store.getAddressName();
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(ll_bt_s);
    }

    private void getWidgets() {
        rv_services = findViewById(R.id.rv_services);

        ln_pbar = findViewById(R.id.ln_pbar);
        ll_bt_s = findViewById(R.id.ll_bt_s);

        rl_add_history = findViewById(R.id.rl_add_history);
        rl_add_new = findViewById(R.id.rl_add_new);

        tv_empty = findViewById(R.id.tv_empty);
        tv_info = findViewById(R.id.tv_info);
        tv_info.setSelected(true);

        iv_back = findViewById(R.id.iv_back);

        btn_save = findViewById(R.id.btn_save);
    }

    private void setListeners() {
        iv_back.setOnClickListener(view -> finish());

        btn_save.setOnClickListener(v -> {
//            if (btn_save.getText().toString().contains("G???I Y??U C???U")) {
            if (requestServiceDAO != null) {
                ViewUtils.progressBarProcess(true, ln_pbar, activity);
                String cusIDstoreID = String.format("%s_%s", users.getUid(), store.getStoreID());
                // Ki???m tra xem ??ang c?? dv n??o ??ang ??? tr???ng th??i x??? l?? kh??ng? status = PROCESSING
                requestServiceDAO.statusIsProcessing(cusIDstoreID, new RequestServiceDAO.IControlData() {
                    @Override
                    public void is(boolean is, RequestService rqS) {
                        if (is) { // C?? dv ??ang x??? l??
                            ViewUtils.progressBarProcess(false, ln_pbar, activity);
                            // H???i ng?????i d??ng xem c?? mu???n h???y d???ch v??? ??ang x??? l?? kia kh??ng?
                            String title = String.format("??ang c?? 1 d???ch v??? ??ang ???????c x??? l?? ??? c???a h??ng %s!", store.getStoreName());
                            String msgDetail = String.format("B???n c?? mu???n h???y d???ch v??? n??y kh??ng?\nChi ti???t\n%s", rqS.getContent());
                            ViewUtils.showDialogConfirm(title, msgDetail, activity, is1 -> {
                                if (is1) { // ?????ng ?? h???y
                                    ViewUtils.progressBarProcess(true, ln_pbar, activity);
                                    requestServiceDAO.changeStatus(cusIDstoreID, rqS.getId(), RequestService.RSStatus.CANCEL.name(),
                                            new RequestServiceDAO.IControlData() {
                                                @Override
                                                public void is(boolean is) {
                                                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                                    Toast.makeText(activity, "H???y th??nh c??ng, ????? th???c hi???n l???i vui l??ng nh???n v??o \"G???I Y??U C???U\" ", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(activity, "Y??u c???u th???t b???i", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            // Ki???m tra xem c?? b???t GPS ch??a
                            if (!ViewUtils.isOnGPSProvider(activity)) {
                                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                Toast.makeText(activity, "Vui l??ng m??? GPS tr??n thi???t b???!!!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            // Ki???m tra xem trong m??y c?? v??? tr?? m???c ?????nh ch??a
                            LatLng latLng = SessionManager.getINSTANCE().getLocationSession();
                            if (latLng == null) {
                                ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                Toast.makeText(activity, "Kh???i ?????ng l???i ???ng d???ng v?? th??? l???i!!!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            // Th??m d???ch v??? th??i
                            List<StoreService> selected = storeServiceListTmp.stream().filter(vz -> vz.isSelect).collect(Collectors.toList());
                            String content = Utils.buildMsgContent(selected, users.getVehicleInfo());
                            String serviceID = cusIDstoreID + "_" + System.currentTimeMillis();
                            RequestService requestService = new RequestService(serviceID,
                                    Utils.buildCurrentDate(),
                                    content,
                                    selected.stream().map(StoreService::getId).collect(Collectors.toList()));
                            requestService.setPhoneNumber(users.getUid());
                            requestService.setLat(latLng.latitude);
                            requestService.setLng(latLng.longitude);

                            InfoName infoName = new InfoName(users.getName(), store.getStoreName());
                            requestServiceDAO.putRequestService(cusIDstoreID, requestService, infoName, new RequestServiceDAO.IControlData() {
                                @Override
                                public void is(boolean is) {
                                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                                    if (is) {
                                        // G???i msg
                                        MessageDetail messageDetail = new MessageDetail();
                                        messageDetail.setId(System.currentTimeMillis() + "");
                                        messageDetail.setContent(content);
                                        messageDetail.setUserID(users.getUid());
                                        messageDetail.setSendDate(Utils.buildCurrentDate());
                                        messageDetail.setServiceID(serviceID);

                                        chatDAO.putMessage(messageDetail, new ChatDAO.IControlData() {
                                            @Override
                                            public void isOK(boolean is) {
                                                Toast.makeText(activity, "???? g???i y??u c???u", Toast.LENGTH_LONG).show();
                                                // B???n notify cho c???a h??ng
                                                // K??m theo intent key storeID
                                                storeDAO.getOwnerIDByStoreID(store.getStoreID(), new StoreDAO.IControlData() {
                                                    @Override
                                                    public void ownerID(String ownerID) {
                                                        if (ownerID != null) {
                                                            NotificationModel notificationModel = new NotificationModel();
                                                            notificationModel.data = new NotificationModel.Data();
                                                            notificationModel.data.group = NotificationService.STORE_SERVICE_GROUP;
                                                            notificationModel.data.senderID = store.getStoreID(); // -> key: storeID
                                                            notificationModel.data.senderName = store.getStoreName();
                                                            notificationModel.data.content = users.getName() + " " + "???? g???i y??u c???u d???ch v???!";
                                                            userDAO.sendNotify(ownerID, notificationModel);

                                                            // Th??ng b??o cho ng?????i g???i lu??n
                                                            userDAO.sendNotificationRequestService(store.getStoreName());
                                                        }
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(activity, "C?? l???i x???y ra, vui l??ng th??? l???i sau v??i ph??t", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                if (TextUtils.isEmpty(getIntent().getStringExtra("storeStr"))) {
                    Intent intent = new Intent();
                    intent.putExtra("resultReturn", new Gson().toJson(storeServiceListTmp.stream().filter(storeService -> storeService.isSelect).collect(Collectors.toList())));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    storeServiceDAO.updateStoreServices(storeServiceListTmp, users.getUid(), new StoreServiceDAO.IControlData() {
                        @Override
                        public void isSuccess(boolean is) {
                            Toast.makeText(activity, "C???p nh???t th??nh c??ng", Toast.LENGTH_SHORT).show();
                            ViewUtils.progressBarProcess(false, ln_pbar, activity);
                            if (storeServiceListTmp.size() > 1) {
                                tv_empty.setVisibility(View.GONE);

                                currentServiceIDs = storeServiceListTmp.stream().map(StoreService::getId).collect(Collectors.toList());

                            } else {
                                tv_empty.setVisibility(View.VISIBLE);
                            }
                            btn_save.setVisibility(View.INVISIBLE);
                            rvServiceAdapter.setIsUpdate(false);
                        }
                    });
                }
            }

        });


        rl_add_new.setOnClickListener(v ->
        {
            Intent intent = new Intent(activity, ServiceInfoActivity.class);
            activity.startActivityForResult(intent, 69);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        rl_add_history.setOnClickListener(v ->
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(activity, ServiceManageActivity.class);
            currentServiceIDs = storeServiceListTmp.stream().map(StoreService::getId).collect(Collectors.toList());
            intent.putExtra("currentServiceIDs", new Gson().toJson(currentServiceIDs));
            intent.putExtra("storeID", store.getStoreID());
            startActivityForResult(intent, 70);
        });
    }

    private List<StoreService> storeServiceListTmp = new LinkedList<>();
    private RVServiceAdapter rvServiceAdapter;
    private List<String> currentServiceIDs = new ArrayList<>();

    public void initServiceList() {
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        rv_services.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvServiceAdapter = new RVServiceAdapter(activity, users.type == Users.TYPE.CUSTOMER);
        rvServiceAdapter.setBottomSheetBehavior(bottomSheetBehavior);
        storeServiceDAO.getServiceList(users.type == Users.TYPE.STORE ? users.getUid() : store.getOwner(), store != null ? store.getStoreID() : null, currentServiceIDs, new StoreServiceDAO.IControlData() {
            @Override
            public void serviceList(List<StoreService> storeServiceList) {
                if (users.type == Users.TYPE.CUSTOMER) {
                    storeServiceList = storeServiceList.stream().filter(StoreService::isActive).collect(Collectors.toList());
                }

                if (!storeServiceList.isEmpty()) {
                    tv_empty.setVisibility(View.GONE);
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }

                storeServiceListTmp = storeServiceList;
                rvServiceAdapter.setStoreList(storeServiceListTmp);

                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            }
        });
        rv_services.setAdapter(rvServiceAdapter);
    }

    public void onHideBottomSheet(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            String rs = data.getStringExtra("resultReturn");
            if (rs != null) {
                if (requestCode == 69) {
                    StoreService storeService = new Gson().fromJson(rs, StoreService.class);
                    storeService.isUpdate = true;
                    // Update
                    boolean updateItem = data.getBooleanExtra("update", false);
                    if (updateItem) {
                        rvServiceAdapter.updateItem(storeService);
                    } else {
                        storeService.putStoreID(store.getStoreID());
                        storeService.setOwner(users.getUid());
                        rvServiceAdapter.insertItem(storeService);
                    }

                    tv_empty.setVisibility(View.GONE);
                    btn_save.setVisibility(View.VISIBLE);
                } else if (requestCode == 70) {
                    List<StoreService> storeServices = new Gson().fromJson(rs, new TypeToken<List<StoreService>>() {
                    }.getType());

                    storeServices.forEach(v -> {
                        v.isUpdate = true;
                        v.putStoreID(store.getStoreID());
                        rvServiceAdapter.insertItem(v);
                    });

                    tv_empty.setVisibility(View.GONE);
                    btn_save.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}