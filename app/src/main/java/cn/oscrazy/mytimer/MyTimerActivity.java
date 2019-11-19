package cn.oscrazy.mytimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MyTimerActivity extends AppCompatActivity {

    //组装时间日期的实体类
    DateTimeEntity dateTimeEntity = new DateTimeEntity();
    //标记打开APP是否第一次更新屏幕
    boolean firstFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_timer);

        //隐藏ActionBar
        getSupportActionBar().hide();

        //设置沉浸式
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //初始化字体样式
        TextView timeAMPM = findViewById(R.id.time_ampm);
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        TextView battery = findViewById(R.id.battery);
        //全局遮罩
        TextView allLight = findViewById(R.id.allLight);

        //使用自定义字体
        AssetManager assets = getAssets();
        Typeface fromAsset = Typeface.createFromAsset(assets, "fonts/font.ttf");
        timeAMPM.setTypeface(fromAsset);
        date.setTypeface(fromAsset);
        time.setTypeface(fromAsset);
        battery.setTypeface(fromAsset);

        //获取电池信息广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int batteryVal = intent.getIntExtra("level", 0);    ///电池剩余电量
                //2 -- 充电   4-- 放电
                int stateVal = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_UNKNOWN); ///获取电池状态
                int tmeperatureVal = intent.getIntExtra("temperature", 0);  ///获取电池温度
                battery.setText("电池电量 : " + batteryVal + "%    电池温度 : " + tmeperatureVal/10.0 +(stateVal==2?"℃    正在充电":"℃"));
                //电池警报
                if (batteryVal < 30 && stateVal != 2){
                    battery.setTextColor(Color.parseColor("#ff0000"));
                }else{
                    battery.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        };
        registerReceiver(receiver,filter);

        //创建线程刷新时间
        new Thread(() -> {
            while (true) {
                try {
                    //在UI线程中更新
                    runOnUiThread(() -> {
                        //每秒刷新屏幕上信息
                        updateTimerDisplay(time, date, timeAMPM, allLight);
                    });
                    Thread.sleep(1000);
                } catch (Throwable t) {
                    runOnUiThread(() -> {
                        //弹出错误消息
                        Toast.makeText(MyTimerActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }


    /**
     * 线程中更新的组件
     * 0 - 小时分钟
     * 1 - 日期栏
     * 2 - 上午下午
     * 3 - 全局遮罩
     * @param views 组件对象
     */
    private void updateTimerDisplay(TextView... views) {
        //获取当前时间的所有信息
        DateTimeEntity d = getNow();

        //如果是打开APP第一次刷新则更新上午状态和日期信息
        if(firstFlag){
            //am,pm状态
            views[2].setText(d.getAmpm());
            //日期栏
            views[1].setText(d.getYear() + "-" + d.getMonth() + "-" + d.getDay() + "     " + d.getWeek()
                    + "     农历" + d.getLunar().getMonth() + "月" + d.getLunar().getChinaDayString(d.getLunar().getDay()));
            //根据时间显示不同的样式
            displayMode(d.getHour(),views);
            firstFlag = false;
        }

        /** 每小时更新区域 */
        if(d.getMinute() == 0 && d.getSecond() == 0){
            //am,pm状态
            views[2].setText(d.getAmpm());
            //根据时间显示不同的样式
            displayMode(d.getHour(),views);
        }

        /** 每天0点更新区域 */
        if(d.getHour() == 0 && d.getMinute() == 0 && d.getSecond() == 0) {
            //日期栏
            views[1].setText(d.getYear() + "-" + d.getMonth() + "-" + d.getDay() + "     " + d.getWeek()
                    + "     农历" + d.getLunar().getMonth() + "月" + d.getLunar().getChinaDayString(d.getLunar().getDay()));
        }

        /** 每秒更新区域 */
        views[0].setText(Html.fromHtml(d.getHour() + ":" + handleZero(d.getMinute())
            + "<font><small><small><small><small>"
                +handleZero(d.getSecond())
            +"</small></small></small></small></font>"));

    }

    //根据不同时间显示不同样式
    private void displayMode(int hour,TextView... views) {
        TextView allLight = views[3];
        if((hour >= 6 && hour < 24) || (hour >= 0 && hour < 1)){
            //早上6点到中午凌晨1点,遮罩亮度设置100% 范围:0~255
            allLight.getBackground().setAlpha(0);
        }else if(hour >= 1 && hour < 6){
            //凌晨1点到中午凌晨6点,遮罩亮度设置50%
            allLight.getBackground().setAlpha(127);
        }
    }

    //获取当前的所有日期时间信息
    private DateTimeEntity getNow(){
        Calendar cal = Calendar.getInstance();
        Lunar lunar=new Lunar(cal);
        dateTimeEntity.setHour(cal.get(Calendar.HOUR_OF_DAY));
        dateTimeEntity.setMinute(cal.get(Calendar.MINUTE));
        dateTimeEntity.setSecond(cal.get(Calendar.SECOND));
        dateTimeEntity.setAmpm(dateTimeEntity.getHour() > 12 ? "PM" : "AM");
        dateTimeEntity.setWeek(cal.get(Calendar.DAY_OF_WEEK));
        dateTimeEntity.setYear(cal.get(Calendar.YEAR));
        dateTimeEntity.setMonth(cal.get(Calendar.MONTH));
        dateTimeEntity.setDay(cal.get(Calendar.DATE));
        dateTimeEntity.setLunar(lunar);
        return dateTimeEntity;
    }

    //小于10的前面补0
    private String handleZero(int val){
        return val < 10 ? ("0" + val) : ("" + val) ;
    }

}
