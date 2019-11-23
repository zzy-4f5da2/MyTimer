package cn.oscrazy.mytimer.utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import cn.oscrazy.mytimer.entity.BatteryEntity;

import java.util.ArrayList;
import java.util.List;

public class BatteryUtils {

    private static List<BatteryEntity> batteryList = new ArrayList<>();

    //记录临时的电池记录
    public static void recode(String... infos){
        BatteryEntity be = new BatteryEntity();
        be.setDate(infos[0]);
        be.setTime(infos[1]);
        be.setBattery(infos[2]);
        be.setTemp(infos[3]);
        batteryList.add(be);
    }

    //显示电池记录弹框
    public static void showBatteryLog(Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle("电池使用记录")
                .setMessage(batteryList.toString())
                .setPositiveButton("隐藏",(b,i) -> {
                    b.dismiss();
                })
                .setNegativeButton("清除记录",(b,i) -> {
                    batteryList.clear();
                    Toast.makeText(activity, "已清除所有的电池记录", Toast.LENGTH_SHORT).show();
                    b.dismiss();
                })
                .show();
    }

}
