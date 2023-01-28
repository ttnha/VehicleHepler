package com.example.vehiclehelper.helper.common;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.vehiclehelper.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class ViewUtils {
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static void progressBarProcess(boolean isShow, View progressBar, Activity activity) {
        if (isShow) {
            progressBar.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            IBinder iBinder = activity.getCurrentFocus().getWindowToken();
            if (iBinder != null) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) activity.getSystemService(
                                Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager.isAcceptingText()) {
                    inputMethodManager.hideSoftInputFromWindow(
                            iBinder, 0
                    );
                }
            }
        } catch (NullPointerException e) {
            Log.e("CATCH", e.getMessage());
        }
    }

    public static void showSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    public static void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void showDialogConfirm(String title, String message, Activity activity, IControlData iControlData) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", (dialog, which) -> {
                    iControlData.isOK(false);
                    dialog.cancel();
                })
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    iControlData.isOK(true);
                    dialog.cancel();
                })
                .show();
    }

    public static void showSettingsGPS(Activity activity) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams") final View view = activity.getLayoutInflater().inflate(R.layout.layout_dialog_gps, null);
        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();
        view.findViewById(R.id.btn_accept).setOnClickListener(v -> {
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            ad.cancel();
        });
        view.findViewById(R.id.btn_denied).setOnClickListener(v -> ad.cancel());
        ad.show();
    }

    public static boolean isOnGPSProvider(Activity activity) {
        return ((LocationManager) activity.getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void call(Activity activity, String phone) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CALL_PHONE},
                    113);
        } else {
            activity.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
        }
    }

    public static void moveMap(Activity activity, double lat, double lng) {
//        d: cho việc lái xe (mặc định)
//        b: đi xe đạp
//        l: cho xe mô tô hai bánh
//        w: để đi bộ
        String latStr = String.valueOf(lat).replace(",", ".");
        String lngStr = String.valueOf(lng).replace(",", ".");
        Uri gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "google.navigation:q=%s,%s&mode=l&z=15", latStr, lngStr));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        activity.startActivity(mapIntent);
    }

    public interface IControlData {
        void isOK(boolean is);
    }

}
