package com.example.vehiclehelper.helper.user.dao;

import android.app.Activity;
import android.app.Notification;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.vehiclehelper.R;
import com.example.vehiclehelper.helper.application.MyApplication;
import com.example.vehiclehelper.helper.fcm.model.NotificationModel;
import com.example.vehiclehelper.helper.fcm.service.INotificationService;
import com.example.vehiclehelper.helper.firebase.FireBaseInit;
import com.example.vehiclehelper.helper.firebase.TableName;
import com.example.vehiclehelper.helper.user.model.LoginOTP;
import com.example.vehiclehelper.helper.user.model.SessionManager;
import com.example.vehiclehelper.helper.user.model.Users;
import com.example.vehiclehelper.helper.user.model.VehicleInfo;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDAO {
    private Activity activity;
    private static final String TABLE_NAME = TableName.Users.name();

    private static SessionManager session;

    public UserDAO(Activity activity) {
        this();
        this.activity = activity;
    }

    public static Users getUser() {
        return SessionManager.getINSTANCE().getUserSession();
    }

    public UserDAO() {
        session = SessionManager.getINSTANCE();
    }

    public void register(Users users, IControlData iControlData) {
        users.setPwd(BCrypt.hashpw(users.getPwd(), BCrypt.gensalt(12)));
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(users.getUid())
                .setValue(users)
                .addOnCompleteListener(task -> iControlData.isSuccess(task.isSuccessful()));
    }

    public void changVehicleInfo(VehicleInfo vehicleInfo, IControlData iControlData) {
        mRefLogin.child("vehicleInfo")
                .setValue(vehicleInfo)
                .addOnCompleteListener(task -> iControlData.isSuccess(task.isSuccessful()));
    }

    public void sendOTP(String phoneNumber) {
        String phoneNumberFormat = "+84" + (phoneNumber.charAt(0) == '0' ? phoneNumber : phoneNumber.substring(1));
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberFormat,
                1L,
                TimeUnit.SECONDS,
                activity,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        session.createOTPSession(phoneAuthCredential.getSmsCode());
                        senLoginOTP(phoneNumber, phoneAuthCredential.getSmsCode());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        senLoginOTP(phoneNumber, "123456");
                    }

                }
        );

    }

    // Ghi OTP l??n database
    private void senLoginOTP(String phoneNumber, String otp) {
        LoginOTP loginOTP = new LoginOTP();
        loginOTP.setOtp(otp);
        loginOTP.setRequestTime(System.currentTimeMillis());

        FireBaseInit.getInstance().getReference()
                .child(TableName.LoginOTP.name())
                .child(phoneNumber)
                .setValue(loginOTP);
    }

    public void isMatchOTP(String phoneNumber, String OTP, IControlData iControlData) {
        DatabaseReference db = FireBaseInit.getInstance().getReference()
                .child(TableName.LoginOTP.name())
                .child(phoneNumber);
        db.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            LoginOTP loginOTP = task.getResult().getValue(LoginOTP.class);
                            if (loginOTP != null) {
                                // Ki???m tra ????ng OTP v?? th???i gian kh??ng qu?? 60s
                                if (loginOTP.getOtp() != null && loginOTP.getOtp().equals(OTP)) {
                                    // Ki???m tra th???i gian
                                    long milis = System.currentTimeMillis() - loginOTP.getRequestTime();
                                    if (milis / 1000 <= 60) {
                                        iControlData.isSuccess(true);
                                        db.removeValue();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    iControlData.isSuccess(false);
                });
    }

    private static DatabaseReference mRefLogin;

    public void login(String pwd, IControlData iControlData) {
        Users userSession = session.getUserSession();
        if (userSession == null) {
            iControlData.isSuccess(false);
        } else {
            String uid = userSession.getUid();
            // Check Pwd
            mRefLogin = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRefLogin.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        Users users = task.getResult().getValue(Users.class);
                        if (users != null) {
                            boolean isPasswordValid = BCrypt.checkpw(pwd, users.getPwd());
                            if (isPasswordValid) {
                                iControlData.isSuccess(true);
                                // L??u notification token v??o DB
                                String token = session.getTokenSession();
                                mRefLogin.child("token").setValue(token);
                                // L???ng nghe s??? ki???n m???i l???n t??c ?????ng l??n user
                                mRefLogin.addValueEventListener(loginListenerCallback);
                                return;
                            }
                        }
                    }
                }
                iControlData.isSuccess(false);
            });
        }
    }

    // H??m ki???m tra s??? ??i???n tho???i ???? c?? trong h??? th???ng hay ch??a
    public void isExistsPhoneNumber(String phoneNumber, IControlData iControlData) {
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(phoneNumber)
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        // N???u t???n t???i th?? t???i ????y m??nh l??u th???ng user n??y v??o Session lu??n
                        Users users = task.getValue(Users.class);
                        if (users != null) {
                            iControlData.isSuccess(true);
                            session.createUserSession(users);
                        } else {
                            iControlData.isSuccess(false);
                        }
                    } else {
                        iControlData.isSuccess(false);
                    }
                });
    }

    public static void loginWithRemember() {
        Users userSession = SessionManager.getINSTANCE().getUserSession();
        if (userSession != null) {
            String uid = userSession.getUid();
            mRefLogin = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRefLogin.addValueEventListener(loginListenerCallback);
        }
    }

    // H??m callbacks khi user ???? c?? s??? thay ?????i v??? m???t d??? li???u
    private static final ValueEventListener loginListenerCallback = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Users users = dataSnapshot.getValue(Users.class);
                if (users != null) {
                    session.createUserSession(dataSnapshot.getValue(Users.class));
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };


    public void removeEventListener() {
        mRefLogin.removeEventListener(loginListenerCallback);
    }

    public Users getUserSession() {
        return session.getUserSession();
    }


    public void clearSession() {
        session.clearSession();
        removeEventListener();
    }

    // Forgot Password
    public void forgotPassword() {
        Users users = session.getUserSession();
        if (users != null) {
            String phoneNumber = users.getUid();
            String phoneNumberFormat = "+84" + phoneNumber.substring(1);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumberFormat,
                    60L,
                    TimeUnit.SECONDS,
                    activity,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            String code = phoneAuthCredential.getSmsCode();
                            session.createOTPSession(code);
                            setPasswordForgot(phoneNumber, code);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                        }

                    }
            );
            // Trong v??ng 5s, n???u OTP ch??a v??? th?? set default gi?? tr??? n??y. Ph??ng tr?????ng h???p limited OTP trong ng??y
            new Handler().postDelayed(() -> {
                if (TextUtils.isEmpty(session.getOTPSession())) {
                    setPasswordForgot(phoneNumber, "123456");
                } else {
                    session.removeOTPSession();
                }
            }, 5 * 1000);
        }
    }

    private void setPasswordForgot(String uid, String password) {
        password = BCrypt.hashpw(password, BCrypt.gensalt(12));
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid)
                .child("pwd")
                .setValue(password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        session.setForgotPasswordFlag(true);
                    }
                });
    }

    // Change Password
    public void changePassword(String currentPass, String newPassword, IControlData iControlData) {
        Users users = session.getUserSession();
        if (users != null) {
            String uid = users.getUid();
            DatabaseReference mRef = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRef.get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    Users usersDB = task.getResult().getValue(Users.class);
                    if (usersDB != null) {
                        if (isValidPassword(usersDB.getPwd(), currentPass)) {
                            String newPass = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                            mRef.child("pwd")
                                    .setValue(newPass)
                                    .addOnCompleteListener(task2 -> iControlData.isSuccess(true));
                        } else {
                            iControlData.isSuccess(false);
                        }
                    }
                }
            });
        }
    }

    private boolean isValidPassword(String realPass, String pass) {
        return BCrypt.checkpw(pass, realPass);
    }

    public void sendNotify(String receiverID, NotificationModel notificationModel) {
        // L???y token c???a ng?????i nh???n
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(receiverID)
                .child("token")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String token = task.getResult().getValue(String.class);
                        if (token != null) {
                            notificationModel.to = token;
                            INotificationService.INSTANCE_SINGLE.sendNotification(notificationModel).enqueue(new Callback<NotificationModel>() {
                                @Override
                                public void onResponse(@NonNull Call<NotificationModel> call, @NonNull Response<NotificationModel> response) {

                                }

                                @Override
                                public void onFailure(@NonNull Call<NotificationModel> call, @NonNull Throwable t) {

                                }
                            });
                        }
                    }
                });
    }

    public void sendNotificationRequestService(String storeName) {
        float currentDistance = session.getCurrentDistance();
        // 1km -> 2 ph??t
        String content = String.format(Locale.getDefault(),
                "%s ???? nh???n ???????c y??u c???u, trong v??ng %.0f ph??t n???a s??? t???i v??? tr?? c???a b???n!",
                storeName, currentDistance * 2);


        Users users = getUser();
        Notification messNotification =
                new NotificationCompat.Builder(activity, MyApplication.CHANNEL_ID)
                        .setContentTitle(users.getName() + " ??i, b???n c?? th??ng b??o m???i!!!")
                        .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        notificationManager.notify(content.length(), messNotification);
    }

    public interface IControlData {
        default void isSuccess(boolean is) {
        }
    }
}
