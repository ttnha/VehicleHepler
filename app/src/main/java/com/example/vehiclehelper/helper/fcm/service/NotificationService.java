package com.example.vehiclehelper.helper.fcm.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.application.MyApplication;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.views.chat.ChatActivity;
import com.example.vehiclehelper.views.store.RatingActivity;
import com.example.vehiclehelper.views.store_service.RequestServiceDetailActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {
    public static final String MESS_GROUP = "Messenger";
    public static final String STORE_SERVICE_GROUP = "StoreService";
    public static final String REPLY_KEY = "REPLY_KEY";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        sendNotifyGroup(message.getData());
    }

    private void sendNotifyGroup(Map<String, String> data) {
        if (data == null) {
            System.out.println("NULL");
            return;
        }
        String GROUP = data.get("group");
        if (GROUP == null) return;
        boolean isMessNotify = GROUP.equals(MESS_GROUP);
        String senderID = data.get("senderID");
        if (isMessNotify && SessionManager.getINSTANCE().isChatting(senderID)) {
            // Đang chat thì không cần thông báo
            return;
        }

        PendingIntent pendingIntent = null;
        NotificationCompat.Action action = null;
        String senderName = data.get("senderName");
        int messNotifyID = new Random().nextInt(Integer.MAX_VALUE);

        if (isMessNotify) {
            String receiverID = data.get("receiverID");
            String cusName = data.get("cusName");
            String storeName = data.get("storeName");
            String receiverName = data.get("receiverName");

            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("senderID", senderID);
            intent.putExtra("receiveID", receiverID);
            intent.putExtra("cusName", cusName);
            intent.putExtra("storeName", storeName);
            intent.putExtra("receiverName", receiverName);

            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Action reply
            RemoteInput remoteInput = new RemoteInput.Builder(REPLY_KEY)
                    .setLabel("Trả lời")
                    .build();

            // Build a PendingIntent for the reply action to trigger.
            Intent in = new Intent(this.getApplicationContext(), BroadCastReceiver.class);
            in.putExtra("notifyID", messNotifyID);
            in.putExtra("isChat", true);
            in.putExtra("senderID", senderID);
            in.putExtra("receiverID", receiverID);
            in.putExtra("cusName", cusName);
            in.putExtra("storeName", storeName);

            PendingIntent replyPendingIntent =
                    PendingIntent.getBroadcast(getApplicationContext(), 1, in, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create the reply action and add the remote input.
            action = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_send_24,
                    "Trả lời", replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();
        } else {
            // Dịch vụ báo HOÀN THÀNH, HỦY thì không cần mở intent
            Intent intent;
            if (data.get("isRating") == null) {
                intent = new Intent(this, RequestServiceDetailActivity.class);
                intent.putExtra("storeID", senderID);
                intent.putExtra("notifyID", messNotifyID);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else if (Objects.equals(data.get("isRating"), "1")) {
                intent = new Intent(this, RatingActivity.class);
                intent.putExtra("serviceContent", data.get("content"));
                intent.putExtra("ownerID", data.get("senderID"));
                intent.putExtra("storeID", data.get("receiverID"));
                intent.putExtra("storeName", data.get("storeName"));
                intent.putExtra("notifyID", messNotifyID);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification messNotification =
                new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                        .setSmallIcon(isMessNotify ? R.drawable.ic_baseline_message_24 : R.drawable.ic_baseline_notifications_24)
                        .setContentTitle(senderName)
                        .setContentText(data.get("content"))
                        .setGroup(GROUP)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(data.get("content")))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .addAction(action)
                        .setSound(alarmSound)
                        .build();

        Notification summaryNotification =
                new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                        .setSmallIcon(isMessNotify ? R.drawable.ic_baseline_message_24 : R.drawable.ic_baseline_store_24)
                        .setStyle(new NotificationCompat.InboxStyle().setSummaryText(getGroupName(isMessNotify)))
                        .setGroup(GROUP)
                        .setAutoCancel(true)
                        .setGroupSummary(true)
                        .setSound(alarmSound)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(messNotifyID, messNotification);
        notificationManager.notify(GROUP.length(), summaryNotification);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        SessionManager.getINSTANCE().createTokenSession(token);
    }

    private String getGroupName(boolean isMess) {
        return isMess ? "Tin nhắn mới" : "Dịch vụ cửa hàng";
    }

    /*
     package com.app.helper.Notification.Service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.MyApplication.MyApplication;
import com.app.helper.R;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Views.Guardian.ConfirmCodeActivity;
import com.app.helper.Views.Guardian.RescuerActivity;
import com.app.helper.Views.Security.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {
    public static final int TYPE_ADD_GUARDIAN = 0; // Chỉ hiện thông báo
    public static final int TYPE_CALL_GUARDIAN = 1; // Hiện màn hình khẩn cấp cho người giám hộ được gọi

    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String CODE = "code";
    public static final String TIME_LIMITED = "time_limit";
    public static final String NAME = "name";
    public static final String UID = "uid";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String CONTENT = "content";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.e("onMessageReceived", remoteMessage.toString());
        Map<String, String> dataMsg = remoteMessage.getData();

        if (dataMsg.containsKey("type")) {
            String typeStr = dataMsg.get("type");
            if (typeStr != null) { // Gửi thông báo thêm giám hộ, gọi điện giám hộ khi nhấn SOS
                int type = Integer.parseInt(typeStr);
                if (type == TYPE_ADD_GUARDIAN) {
                    sendNotificationAddGuardian(dataMsg);
                } else if (type == TYPE_CALL_GUARDIAN) {
                    sendNotificationCallGuardian(dataMsg);
                }
            }
        } else {
            if (dataMsg.containsKey("is_notification")) { // Gửi thông báo cập nhật khoanh vùng vào Session
                try {
                    DataRequestLocationZoning.Data data = new Gson().fromJson(dataMsg.toString(), DataRequestLocationZoning.Data.class);
                    SessionManager.getINSTANCE().createOrUpdateLocationZoningMap(data);


                } catch (Exception e) {
                    Log.e("EX", e.getMessage());
                }
            } else {
                sendNotificationLocationZoning(dataMsg);
            }

        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        SessionManager.getINSTANCE().createTokenSession(token);
    }

    private void sendNotificationAddGuardian(Map<String, String> data) {
        try {
            String code = data.get(CODE);
            String time_limited = data.get(TIME_LIMITED);
            String name = data.get(NAME);
            String content = data.get(CONTENT);

            int notificationId = new Random().nextInt();

            String title = "Chào " + name + "! Bạn có thông báo mới!";

            Intent intent = new Intent(this, ConfirmCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(NOTIFICATION_ID, notificationId);
            intent.putExtra(CODE, code);
            intent.putExtra(TIME_LIMITED, time_limited);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                            .setContentTitle(title)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setAutoCancel(true)
                            .setTimeoutAfter(1000 * 60)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationManager.IMPORTANCE_DEFAULT);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationCallGuardian(Map<String, String> data) {
        String name = data.get(NAME);
        String code = data.get(CODE);
        String uid = data.get(UID);
        String time_limited = data.get(TIME_LIMITED);
        String latitude = data.get(LATITUDE);
        String longitude = data.get(LONGITUDE);

        Intent intent = new Intent(this, RescuerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(CODE, code);
        intent.putExtra(NAME, name);
        intent.putExtra(UID, uid);
        intent.putExtra(TIME_LIMITED, time_limited);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    private void sendNotificationLocationZoning(Map<String, String> data) {
        try {
            String title = data.get("title");
            String content = data.get(CONTENT);

            int notificationId = Integer.parseInt(UtilsClazz.random6Code());

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_warning_24)
                            .setColor(Color.RED)
                            .setContentTitle(title)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationManager.IMPORTANCE_HIGH);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

     */
}
