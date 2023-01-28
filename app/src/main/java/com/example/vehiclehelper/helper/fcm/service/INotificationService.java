package com.example.vehiclehelper.helper.fcm.service;


import com.example.vehiclehelper.helper.fcm.model.NotificationModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface INotificationService {
    String BASE_URL = "https://fcm.googleapis.com/fcm/";

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    INotificationService INSTANCE_SINGLE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(INotificationService.class);

    @Headers({
            "Authorization: key=AAAAHV7iXF0:APA91bETv_gezuHai2htsusHK_KNEDM39PLEXnisAWKIA8-4ixO0CmF_7LRPuEuhrQrZ28AarnvHDckcqgNsUq2ewVq-2VHHeSCCxXRjnQpbSwPym3K_1lMDC5MV6PdflCGCu_nuRkkK",
            "Content-Type: application/json"
    })
    @POST("send")
    Call<NotificationModel> sendNotification(@Body NotificationModel notificationModel);
}
