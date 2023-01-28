package com.example.vehiclehelper.helper.user.model;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.vehiclehelper.helper.application.MyApplication;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class SessionManager {
    private final SharedPreferences session;
    private final SharedPreferences.Editor editor;

    private static final String KEY_OTP = "OTP";
    private static final String KEY_USER = "USER";
    private static final String KEY_TOKEN = "TOKEN";

    // Location
    private static final String KEY_LOCATION = "LOCATION";
    private static final String KEY_LOCATION_REQUEST = "LOCATION_REQUEST";


    private static SessionManager INSTANCE;

    @SuppressLint("CommitPrefEdits")
    private SessionManager() {
        session = MyApplication.SESSION;
        editor = session.edit();
    }

    public static SessionManager getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    // OTP
    public void createOTPSession(String OTP) {
        editor.putString(KEY_OTP, OTP);
        editor.apply();
    }

    public String getOTPSession() {
        return session.getString(KEY_OTP, null);
    }

    public void removeOTPSession() {
        editor.remove(KEY_OTP);
        editor.apply();
    }

    // User
    public void createUserSession(Users users) {
        editor.putString(KEY_USER, new Gson().toJson(users));
        editor.apply();
    }

    public Users getUserSession() {
        String userJson = session.getString(KEY_USER, null);
        if (TextUtils.isEmpty(userJson)) return null;
        return new Gson().fromJson(userJson, Users.class);
    }

    // Forgot password
    private static final String KEY_FP_FLAG = "KEY_FP_FLAG";

    public void setForgotPasswordFlag(boolean value) {
        editor.putBoolean(KEY_FP_FLAG, value);
        editor.apply();
    }

    public boolean isForgotPasswordFlag() {
        return session.getBoolean(KEY_FP_FLAG, false);
    }

    // Remember Password
    private static final String KEY_REMEMBER_FLAG = "KEY_REMEMBER_FLAG";

    public void setRememberPasswordFlag(boolean value) {
        editor.putBoolean(KEY_REMEMBER_FLAG, value);
        editor.apply();
    }

    public boolean isRememberPasswordFlag() {
        return session.getBoolean(KEY_REMEMBER_FLAG, false);
    }

    //region Location
    public void createOrUpdateLocationSession(LatLng latLng) {
        editor.putString(KEY_LOCATION, new Gson().toJson(latLng));
        editor.apply();
    }

    public LatLng getLocationSession() {
        String lo = session.getString(KEY_LOCATION, null);
        return lo == null ? new LatLng(10.8603725, 106.7695497) : new Gson().fromJson(lo, LatLng.class);
    }

    public void upsertDistance(float distance) {
        editor.putFloat("upsertDistance", distance);
        editor.apply();
    }

    public float getCurrentDistance() {
        return session.getFloat("upsertDistance", -1F);
    }

    //endregion Location

    // Token Notification
    public void createTokenSession(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getTokenSession() {
        return session.getString(KEY_TOKEN, "");
    }

    //region Chat
    public void putMsgNotSeen(String key) {
        key += "MsgNotSeen";
        editor.putBoolean(key, true);
        editor.apply();
    }

    public void removeMsgNotSeen(String key) {
        key += "MsgNotSeen";
        editor.remove(key);
        editor.apply();
    }

    public boolean isSeen(String key) {
        key += "MsgNotSeen";
        return !session.getBoolean(key, false);
    }

    // Chỗ này xem đang nhắn tin với khứa nào
    public void putMsgCurrent(String receiver) {
        String key = "MsgCurrent" + receiver;
        editor.putBoolean(key, true);
        editor.apply();
    }

    public void removeMsgCurrent(String receiver) {
        String key = "MsgCurrent" + receiver;
        editor.remove(key);
        editor.apply();
    }

    public boolean isChatting(String receiver) {
        String key = "MsgCurrent" + receiver;
        return session.getBoolean(key, false);
    }

    // Chỗ này lưu thông báo số lượng message chưa đọc của của hàng nào đó
    public void putMsgNotifyCount(String storeID, int count) {
        String key = "MsgNotifyCount" + storeID;
        editor.putInt(key, count);
        editor.apply();
    }

    public void removeMsgNotifyCount(String storeID) {
        String key = "MsgNotifyCount" + storeID;
        editor.remove(key);
        editor.apply();
    }

    public int getMsgNotifyCount(String storeID) {
        String key = "MsgNotifyCount" + storeID;
        return session.getInt(key, 0);
    }

    //endregion

    // Clear
    public void clearSession() {
        String token = getTokenSession();
        editor.clear();
        editor.commit();
        createTokenSession(token);
    }
}
