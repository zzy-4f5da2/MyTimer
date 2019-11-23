package cn.oscrazy.mytimer;

import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.oscrazy.mytimer.entity.DateTimeEntity;
import cn.oscrazy.mytimer.utils.BatteryUtils;

import java.util.Calendar;

import static cn.oscrazy.mytimer.utils.MusicUtils.*;

public class MyTimerActivity extends AppCompatActivity {

    //组装时间日期的实体类
    DateTimeEntity dateTimeEntity = new DateTimeEntity();
    //标记打开APP是否第一次更新屏幕
    boolean firstFlag = true;
    //闹铃窗口
    private RelativeLayout alarmLayout;

    String tempBattery = "未开始监测";
    String tempTemp = "未开始监测";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_timer);

        //初始化闹钟数据
        init(this);

        //隐藏ActionBar
        getSupportActionBar().hide();

        //设置导航栏为透明颜色
        Window window = getWindow();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(Color.TRANSPARENT);

        //强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //初始化字体样式
        TextView timeAMPM = findViewById(R.id.time_ampm);
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        TextView battery = findViewById(R.id.battery);
        TextView alarmInfo = findViewById(R.id.alarmInfo);
        TextView alarmNext = findViewById(R.id.alarmNext);
        //全局遮罩
        TextView allLight = findViewById(R.id.allLight);
        //背景遮罩
        TextView behindLight = findViewById(R.id.behindLight);
        //关闭闹钟按钮
        TextView stopMusic = findViewById(R.id.stopMusic);
        alarmLayout = findViewById(R.id.alarmLayout);

        //初始化遮罩,方便布局调试
        allLight.setVisibility(View.VISIBLE);

        //设置点击事件
        //关闭闹钟按钮
        stopMusic.setOnClickListener(v -> {
            stopMusic();
            alarmLayout.setVisibility(View.GONE);
        });
        //点击下一闹钟信息设置闹钟
        alarmNext.setOnClickListener(v -> {
            setAlarmsByView(MyTimerActivity.this);
        });
        //显示电池日志
        battery.setOnClickListener(v -> {
            BatteryUtils.showBatteryLog(MyTimerActivity.this);
        });


        //使用自定义字体
        AssetManager assets = getAssets();
        Typeface fromAsset = Typeface.createFromAsset(assets, "fonts/font.ttf");
        timeAMPM.setTypeface(fromAsset);
        date.setTypeface(fromAsset);
        time.setTypeface(fromAsset);
        battery.setTypeface(fromAsset);
        alarmInfo.setTypeface(fromAsset);
        alarmNext.setTypeface(fromAsset);

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
                battery.setText((stateVal==2?"祈祷晴天 : ":"晴天覆盖 : ") + batteryVal + (stateVal==2?"%+/":"%/")+tmeperatureVal/10+"℃");
                //电池警报
                if (batteryVal < 30 && stateVal != 2){
                    battery.setTextColor(Color.parseColor("#ff0000"));
                }else if (stateVal == 2){
                    battery.setTextColor(Color.parseColor("#66ffbb"));
                }else{
                    battery.setTextColor(Color.parseColor("#ffffff"));
                }
                tempBattery = batteryVal + "";
                tempTemp = tmeperatureVal/10.0 + "";
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
                        updateTimerDisplay(time, date, timeAMPM, allLight,alarmInfo,alarmNext,behindLight);
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
     * 4 - 闹钟提示
     * 5 - 下一闹钟
     * 6 - 背景遮罩
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

        /** 每分钟更新区域 */
        if(d.getSecond() == 0){
            //响铃
            isAlarm(d,views[4]);
            if(d.getMinute() % 10 == 0){
                BatteryUtils.recode(d.getMonth() + "/" + d.getDay(),
                        d.getHour() + ":" + handleZero(d.getMinute()),tempBattery,tempTemp
                        );
            }
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
        views[5].setText(getNextAlarm(d));

    }

    //检测是否是闹铃或整点报时
    private void isAlarm(DateTimeEntity d,TextView alarmInfo) {
        for(int i = 0 ; i < myAlarmList.size() ; i++){
            DateTimeEntity dte = myAlarmList.get(i);
            //当前秒存在闹钟
            if(d.getWeek() == dte.getWeek() && d.getHour() == dte.getHour()
                    && d.getMinute() == dte.getMinute()){
                //随机播放闹钟音乐
                startMusic(MUSIC_ALARM_TYPE, (int)(Math.random() * musicPaths[MUSIC_ALARM_TYPE].length),MyTimerActivity.this);
                alarmLayout.setVisibility(View.VISIBLE);
                alarmInfo.setText("你设置的闹铃(" + d.getWeek() + " " + d.getHour() + ":" + handleZero(d.getMinute()) + ")已生效");
                return;
            }
        }
        //如果不存在闹钟,检测是否整点
        //周一到周五 9点到24点开启报时,周六日 11点到25点开启报时
        if(d.getMinute() == 0){
            if(d.getWeekByEE().equals("周六") || d.getWeekByEE().equals("周日")){
                if(d.getHour() == 0){
                    startMusic(MUSIC_TIMETIP_TYPE,musicPaths[MUSIC_TIMETIP_TYPE].length - 2,MyTimerActivity.this);
                }else if(d.getHour() == 1){
                    startMusic(MUSIC_TIMETIP_TYPE,musicPaths[MUSIC_TIMETIP_TYPE].length - 1,MyTimerActivity.this);
                }else if(d.getHour() >= 11 && d.getHour() <= 23){
                    startMusic(MUSIC_TIMETIP_TYPE, d.getHour() - 8,MyTimerActivity.this);
                }
            }else{
                if(d.getHour() == 0){
                    startMusic(MUSIC_TIMETIP_TYPE,musicPaths[MUSIC_TIMETIP_TYPE].length - 2,MyTimerActivity.this);
                }else if(d.getHour() >= 9 && d.getHour() <= 23){
                    startMusic(MUSIC_TIMETIP_TYPE, d.getHour() - 8,MyTimerActivity.this);
                }
            }

        }
        //调试
        //startMusic(MUSIC_TIMETIP_TYPE,d.getMinute()%15,MyTimerActivity.this);
    }

    //根据不同时间显示不同样式
    private void displayMode(int hour,TextView... views) {
        TextView allLight = views[3];
        TextView behindLight = views[6];
        if((hour >= 6 && hour < 24) || (hour >= 0 && hour < 1)){
            //早上6点到中午凌晨1点
            allLight.setBackgroundColor(Color.parseColor("#00000000"));
            behindLight.setBackgroundColor(Color.parseColor("#60000000"));
        }else if(hour >= 1 && hour < 6){
            //凌晨1点到中午凌晨6点
            allLight.setBackgroundColor(Color.parseColor("#BB000000"));
            behindLight.setBackgroundColor(Color.parseColor("#FF000000"));
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



}
