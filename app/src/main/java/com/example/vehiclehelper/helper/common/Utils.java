package com.example.vehiclehelper.helper.common;

import androidx.annotation.NonNull;

import com.example.vehiclehelper.helper.store_service.model.StoreService;
import com.example.vehiclehelper.helper.user.model.VehicleInfo;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

public class Utils {
    public static String buildSessionChatID(@NonNull String s1, @NonNull String s2) {
        return s1.length() < s2.length() ? s1 + "_" + s2 : s2 + "_" + s1;
    }

    public static String getCusIDOrStoreIDFromSSChatID(String ssc, boolean isGetCus) {
        return ssc.split("_")[isGetCus ? 0 : 1];
    }

    private static final DecimalFormat df = new DecimalFormat("###,###,###");

    public static String buildMsgContent(List<StoreService> storeServiceList, VehicleInfo vehicleInfo) {
        int num = 1;
        int sumMoney = 0;
        StringBuilder builder = new StringBuilder();
        builder.append("Thông tin xe: ")
                .append(vehicleInfo.getDistributor()).append(" ")
                .append(vehicleInfo.getName()).append(" ")
                .append(vehicleInfo.getOunce())
                .append(" - ")
                .append("BSX: ")
                .append(vehicleInfo.getNumber())
                .append("\n");
        builder.append("Dịch vụ yêu cầu:").append("\n");
        for (StoreService storeService : storeServiceList) {
            builder.append(num++).append(". ")
                    .append(storeService.getServiceName())
                    .append(" - ").append("GIÁ: ")
                    .append(df.format(storeService.getPriceOfService()).replace(",", ".")).append(" VNĐ")
                    .append("\n");
            sumMoney += storeService.getPriceOfService();
        }
        builder.append("*** TỔNG TIỀN: ").append(buildMoney(sumMoney));
        return builder.toString();
    }

    public static String buildMoney(long money) {
        return df.format(money).replace(",", ".") + " VNĐ";
    }

    private static final transient DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");

    public static String buildCurrentDate() {
        return formatter.format(LocalDateTime.now());
    }

    public static String convertTimeMillisToDate(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // 0 - 11
        int year = calendar.get(Calendar.YEAR);
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
    }

    /**
     * 0: ==, 1:>, -1:<
     */
    public static int compareDateStr(String d1, String d2) {
        String[] t1 = d1.split("-");
        String[] t2 = d2.split("-");
        LocalDate l1 = LocalDate.of(Integer.parseInt(t1[0]), Integer.parseInt(t1[1]), Integer.parseInt(t1[2]));
        LocalDate l2 = LocalDate.of(Integer.parseInt(t2[0]), Integer.parseInt(t2[1]), Integer.parseInt(t2[2]));
        return l1.isAfter(l2) ? 1 : l1.isEqual(l2) ? 0 : -1;
    }

    public static int getMoneyFromContent(String content) {
        int lastIndex = content.lastIndexOf(":");
        int indexOfVND = content.lastIndexOf("VNĐ");
        String s = content.substring(lastIndex + 1, indexOfVND).trim().replace(".", "");
        return Integer.parseInt(s);
    }

}
