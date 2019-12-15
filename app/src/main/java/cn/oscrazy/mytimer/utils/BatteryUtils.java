package cn.oscrazy.mytimer.utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import cn.oscrazy.mytimer.entity.BatteryEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取当前电流
     */
    public static Map<String,String> getCurrent() {
        Map<String,String> returnMap = new HashMap<>();
        try {
            Class systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getDeclaredMethod("get", String.class);
            String platName = (String) get.invoke(null, "ro.hardware");
            if (platName.startsWith("mt") || platName.startsWith("MT")) {
                String filePath = "/sys/class/power_supply/battery/device/FG_Battery_CurrentConsumption";
                // MTK平台该值不区分充放电，都为负数，要想实现充放电电流增加广播监听充电状态即可
                String dianLiu = Math.round(getMeanCurrentVal(filePath, 5, 0) / 10.0f) / 1000.0f + "A";
                String dianYa = readFile("/sys/class/power_supply/battery/batt_vol", 0) / 1000.0f + "V";
                returnMap.put("dianLiu",dianLiu);
                returnMap.put("dianYa",dianYa);
            } else if (platName.startsWith("qcom")) {
                String filePath ="/sys/class/power_supply/battery/current_now";
                int current = Math.round(getMeanCurrentVal(filePath, 5, 0) / 10.0f);
                int voltage = readFile("/sys/class/power_supply/battery/voltage_now", 0) / 1000;
                // 高通平台该值小于0时电池处于放电状态，大于0时处于充电状态
                if (current < 0) {
                    returnMap.put("dianLiu",-current + "");
                    returnMap.put("dianYa",voltage + "");
                } else {
                    returnMap.put("dianLiu",current + "");
                    returnMap.put("dianYa",voltage + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.e("eeeeee",returnMap.toString());
        return returnMap;
    }

    private static int readFile(String path, int defaultValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    path));
            int i = Integer.parseInt(bufferedReader.readLine(), 10);
            bufferedReader.close();
            return i;
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 获取平均电流值
     * 获取 filePath 文件 totalCount 次数的平均值，每次采样间隔 intervalMs 时间
     */
    private static float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {
        float meanVal = 0.0f;
        if (totalCount <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < totalCount; i++) {
            try {
                float f = Float.valueOf(readFile(filePath, 0));
                meanVal += f / totalCount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intervalMs <= 0) {
                continue;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meanVal;
    }

}
