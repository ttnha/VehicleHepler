package com.example.vehiclehelper.views.store;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.store.dao.RatingDAO;
import com.example.vehiclehelper.helper.store.model.StoreRating;
import com.example.vehiclehelper.helper.user.dao.UserDAO;

public class RatingActivity extends AppCompatActivity {
    private TextView tv_store_name;
    private RatingBar rb_rating;
    private EditText et_content;
    private Button btn_send;
    private RatingDAO ratingDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        ratingDAO = new RatingDAO();
        getWidgets();
        setListeners();
        initDataFromIntent();
    }

    private void initDataFromIntent() {
        Intent intent = getIntent();
        String storeName = intent.getStringExtra("storeName");
        tv_store_name.setText(storeName);
        NotificationManagerCompat.from(RatingActivity.this).cancel(intent.getIntExtra("notifyID", 0));
    }

    private void setListeners() {
        btn_send.setOnClickListener(v -> {
            float rate = rb_rating.getRating();
            if (rate == 0f) {
                Toast.makeText(RatingActivity.this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            // Nội dung có thể để trống
            String content = et_content.getText().toString();
            String assessorName = null;
            try {
                assessorName = UserDAO.getUser().getName();
            } catch (Exception ignored) {
            }

            Intent intent = getIntent();
            String serviceContent = intent.getStringExtra("serviceContent");
            String ownerID = intent.getStringExtra("ownerID");
            String storeID = intent.getStringExtra("storeID");
            String id = String.join("_", ownerID, storeID, System.currentTimeMillis() + "");

            StoreRating storeRating = new StoreRating(id, rate, content, serviceContent, assessorName);
            ratingDAO.putRating(storeRating, isOK -> {
                String c = isOK ? "Đã gửi đánh giá" : "Có gì sai sai!!!";
                Toast.makeText(RatingActivity.this, c, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 500);
            });
        });
    }

    private void getWidgets() {
        tv_store_name = findViewById(R.id.tv_store_name);
        rb_rating = findViewById(R.id.rb_rating);
        et_content = findViewById(R.id.et_content);
        btn_send = findViewById(R.id.btn_send);
    }
}