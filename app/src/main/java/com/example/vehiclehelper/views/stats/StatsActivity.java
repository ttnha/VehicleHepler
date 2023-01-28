package com.example.vehiclehelper.views.stats;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.common.Utils;
import com.example.vehiclehelper.helper.common.ViewUtils;
import com.example.vehiclehelper.helper.stats.dao.StatsDAO;
import com.example.vehiclehelper.helper.stats.model.Stats;
import com.example.vehiclehelper.helper.store_service.model.RequestService;
import com.example.vehiclehelper.views.adapter.RVStatsRqServiceAdapter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatsActivity extends AppCompatActivity {
    private Activity activity;
    private TextView tv_time_start, tv_time_end, tv_total_money;
    private View v_reset;
    private CheckBox cb_done, cb_cancel, cb_processing;
    private LinearLayout ln_pbar;
    private Button btn_run;
    private RecyclerView rv_stats;
    private String userID;
    private Stats stats;
    private StatsDAO statsDAO;
    private List<RequestService> requestServiceList;
    private Map<String, List<RequestService>> requestServiceMap;
    private RVStatsRqServiceAdapter adapter;
    private String tmpFlag;
    private long current_selection;
    private static final String time_tmp = "Chọn ngày";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        userID = getIntent().getStringExtra("userID");
        if (TextUtils.isEmpty(userID)) {
            finish();
            return;
        }
        activity = this;
        stats = new Stats();
        statsDAO = new StatsDAO();
        getWidgets();
        setListeners();

        tv_time_start.setText(time_tmp);
        tv_time_end.setText(time_tmp);
        tmpFlag = stats.toString();
        initData(null);
    }

    private void getWidgets() {
        tv_time_start = findViewById(R.id.tv_time_start);
        tv_time_end = findViewById(R.id.tv_time_end);
        tv_total_money = findViewById(R.id.tv_total_money);

        cb_done = findViewById(R.id.cb_done);
        cb_cancel = findViewById(R.id.cb_cancel);
        cb_processing = findViewById(R.id.cb_processing);

        rv_stats = findViewById(R.id.rv_stats);

        btn_run = findViewById(R.id.btn_run);

        ln_pbar = findViewById(R.id.ln_pbar);

        v_reset = findViewById(R.id.v_reset);
    }

    private void setListeners() {
        tv_time_start.setOnClickListener(v -> dateSelect(true));

        tv_time_end.setOnClickListener(v -> dateSelect(false));

        cb_done.setOnCheckedChangeListener((compoundButton, b) -> stats.isCheckDone = b);
        cb_cancel.setOnCheckedChangeListener((compoundButton, b) -> stats.isCheckCancel = b);
        cb_processing.setOnCheckedChangeListener((compoundButton, b) -> stats.isCheckProcessing = b);


        btn_run.setOnClickListener(v -> {
            if (stats.toString().equals(tmpFlag)) {
                Toast.makeText(activity, "Vui lòng thay đổi điều kiện lọc", Toast.LENGTH_LONG).show();
            } else {
                if (stats.statusString().equals("")) {
                    Toast.makeText(activity, "Chọn ít nhất 1 trạng thái để lọc", Toast.LENGTH_LONG).show();
                    return;
                }
                String dateStart = tv_time_start.getText().toString();
                String dateEnd = tv_time_end.getText().toString();
                // Kiểm tra xem ngày bắt đầu có sau ngày kết thúc không?
                if (!dateStart.equals(time_tmp) && !dateEnd.equals(time_tmp) && Utils.compareDateStr(dateStart, dateEnd) == 1) {
                    Toast.makeText(activity, "Ngày không hợp lệ", Toast.LENGTH_LONG).show();
                    return;
                }
                stats.timeStart = dateStart;
                stats.timeEnd = dateEnd;
                filter();
                v_reset.setVisibility(View.VISIBLE);
            }
        });
    }

    private void dateSelect(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setSelection(current_selection == 0 ? System.currentTimeMillis() : current_selection)
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
                    current_selection = selection;
                    String date = Utils.convertTimeMillisToDate(selection);
                    if (isStart) {
                        tv_time_start.setText(date);
                        stats.timeStart = date;
                    } else {
                        tv_time_end.setText(date);
                        stats.timeEnd = date;
                    }
                }
        );
        picker.show(getSupportFragmentManager(), "Date picker");
    }

    public void initData(Map<String, List<RequestService>> map) {
        if (map == null) {
            ViewUtils.progressBarProcess(true, ln_pbar, activity);
            statsDAO.getFullRqService(userID, rqServiceMap -> {
                if (rqServiceMap != null) {
                    requestServiceMap = rqServiceMap;
                    requestServiceList = statsDAO.flatMap(requestServiceMap);
                    adapter = new RVStatsRqServiceAdapter(activity, requestServiceList);
                    rv_stats.setAdapter(adapter);
                    tv_total_money.setText(Utils.buildMoney(getTotalMoney(requestServiceList)));
                }
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            });
        } else {
            // Trường hợp reset điều kiện lọc
            adapter.update(requestServiceList);
        }
    }

    private void filter() {
        final String statusString = stats.statusString();
        List<RequestService> list = requestServiceList.stream().filter(v -> {
            String date = v.getCreateDate().split(" ")[0].replace("/", "-");
            return (stats.timeEnd.equals(time_tmp) || Utils.compareDateStr(stats.timeEnd, date) >= 0)
                    && (stats.timeStart.equals(time_tmp) || Utils.compareDateStr(stats.timeStart, date) <= 0)
                    && statusString.contains(v.getStatus());
        }).sorted(Comparator.comparing(RequestService::getCreateDate)).collect(Collectors.toList());
        Collections.reverse(list);
        tv_total_money.setText(Utils.buildMoney(getTotalMoney(list)));
        adapter.update(list);
        tmpFlag = stats.toString();
    }

    private long getTotalMoney(List<RequestService> list) {
        try {
            return list.stream()
                    .filter(v -> Objects.equals(v.getStatus(), RequestService.RSStatus.DONE.name()))
                    .mapToLong(v -> Utils.getMoneyFromContent(v.getContent()))
                    .sum();
        } catch (Throwable e) {
            return 0;
        }
    }

    public void back(View view) {
        finish();
    }

    public void resetFilter(View view) {
        stats = new Stats();
        tv_time_start.setText(time_tmp);
        tv_time_end.setText(time_tmp);
        stats.timeStart = time_tmp;
        stats.timeEnd = time_tmp;

        cb_processing.setChecked(true);
        cb_done.setChecked(true);
        cb_cancel.setChecked(true);

        filter();
        v_reset.setVisibility(View.GONE);
    }
}