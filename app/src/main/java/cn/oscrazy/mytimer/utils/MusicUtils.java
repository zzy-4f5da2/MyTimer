package cn.oscrazy.mytimer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import cn.oscrazy.mytimer.entity.DateTimeEntity;

import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    public static final int MUSIC_ALARM_TYPE = 0;
    public static final int MUSIC_TIMETIP_TYPE = 1;

    //简单数据存储
    private static SharedPreferences sp;

    private static MediaPlayer mediaPlayer = new MediaPlayer();

    //SD卡根目录
    //private static File pFile = Environment.getExternalStorageDirectory();

    //歌曲路径
    public static String[][] musicPaths = new String[][]{
        {
            "MyTimerMusic/RADWIMPS-陽菜と、走る帆高.mp3",
            "MyTimerMusic/RADWIMPS-初の晴れ女バイト.mp3",
            "MyTimerMusic/RADWIMPS-花火大会.mp3",
            "MyTimerMusic/RADWIMPS-晴れゆく空.mp3"
        },
        {
            "MyTimerMusic/zdbs08.mp3",
            "MyTimerMusic/zdbs09.mp3",
            "MyTimerMusic/zdbs10.mp3",
            "MyTimerMusic/zdbs11.mp3",
            "MyTimerMusic/zdbs12.mp3",
            "MyTimerMusic/zdbs13.mp3",
            "MyTimerMusic/zdbs14.mp3",
            "MyTimerMusic/zdbs15.mp3",
            "MyTimerMusic/zdbs16.mp3",
            "MyTimerMusic/zdbs17.mp3",
            "MyTimerMusic/zdbs18.mp3",
            "MyTimerMusic/zdbs19.mp3",
            "MyTimerMusic/zdbs20.mp3",
            "MyTimerMusic/zdbs21.mp3",
            "MyTimerMusic/zdbs22.mp3",
            "MyTimerMusic/zdbs23.mp3",
            "MyTimerMusic/zdbs24.mp3",
            "MyTimerMusic/zdbs25.mp3",
        }
    };

    //我设置的所有闹钟
    public static List<DateTimeEntity> myAlarmList = new ArrayList<>();

    public static void init(Context context){
        sp = context.getSharedPreferences("app",Context.MODE_PRIVATE);
        getAlarms();
    }

    //从简单存储中过去闹钟数据
    public static void getAlarms(){
        String alarms = sp.getString("alarms","");
        myAlarmList.clear();
        //如果闹钟数据不为空
        if (alarms != null && alarms.length() > 0 && !alarms.equals("null")){
            String[] alarmArr = alarms.split(",");
            if(alarmArr != null && alarmArr.length > 0){
                for (String item : alarmArr){
                    String[] entity = item.split(":");
                    int week = getWeekIndex(entity[0]);
                    int hour = Integer.parseInt(entity[1]);
                    int minute = Integer.parseInt(entity[2]);
                    DateTimeEntity dateTimeEntity = new DateTimeEntity();
                    dateTimeEntity.setWeek(week);
                    dateTimeEntity.setHour(hour);
                    dateTimeEntity.setMinute(minute);
                    myAlarmList.add(dateTimeEntity);
                }
            }
        }
    }

    //从简单存储中过去闹钟数据
    public static void setAlarms(String val){
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("alarms",val).apply();
        getAlarms();
    }

    public static void setAlarmsByView(Activity context){
        final EditText editText = new EditText(context);
        editText.setText(sp.getString("alarms",""));
        new AlertDialog.Builder(context).setTitle("设置闹钟")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("确定", (d, i) -> {
                    setAlarms(editText.getText().toString());
                    Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", (d, i) -> {
                    d.dismiss();
                })
                .setNeutralButton("设置教程",(d, i) -> {
                    Toast.makeText(context, "设置格式如:[周三:5:23] 多个用逗号分隔[周二:11:23,周四:13:23],", Toast.LENGTH_LONG).show();
                })
                .show();

    }

    public static String getNextAlarm(DateTimeEntity d) {
        int len = myAlarmList.size();
        if (len < 1) {
            return "闹钟未设置";
        }
        //日期比较算法
        long nowVal = d.getWeekByIndex() * 10000 + d.getHour() * 100 + d.getMinute();
        DateTimeEntity finalEntity = null;
        for (int i = 0; i < myAlarmList.size(); i++) {
            DateTimeEntity myd = myAlarmList.get(i);
            long alarmVal = myd.getWeekByIndex() * 10000 + myd.getHour() * 100 + myd.getMinute();
            if (alarmVal > nowVal) {
                if (finalEntity != null) {
                    long preVal = finalEntity.getWeekByIndex() * 10000 + finalEntity.getHour() * 100 + finalEntity.getMinute();
                    if(preVal > alarmVal){
                        finalEntity = myd;
                    }
                }else{
                    finalEntity = myd;
                }
            }
        }
        if(finalEntity == null){
            finalEntity = myAlarmList.get(0);
        }
        return "下一闹钟 : " + finalEntity.getWeekByEE()
                + " " + finalEntity.getHour() + ":" + handleZero(finalEntity.getMinute());
    }


    //获取闹钟数据的星期索引
    private static int getWeekIndex(String week) {
        switch (week) {
            case "周日":
                return 1;
            case "周一":
                return 2;
            case "周二":
                return 3;
            case "周三":
                return 4;
            case "周四":
                return 5;
            case "周五":
                return 6;
            case "周六":
                return 7;
            default:
                return 0;
        }
    }

    //停止音乐
    public static void stopMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.reset();
            //initMediaPlayer(index);
        }
    }

    //播放音乐,传入音源下标
    public static void startMusic(int type, int index, Context context){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.reset();
            initMediaPlayer(type, index, context);
            mediaPlayer.start();
        }
    }

    //初始化音乐
    public static void initMediaPlayer(int type, int musicIndex, Context context){
        try {
            //播放 assets/a2.mp3 音乐文件
            AssetFileDescriptor fd = context.getAssets().openFd(musicPaths[type][musicIndex]);
            mediaPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());//指定音频文件路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //小于10的前面补0
    public static String handleZero(int val){
        return val < 10 ? ("0" + val) : ("" + val) ;
    }


}
