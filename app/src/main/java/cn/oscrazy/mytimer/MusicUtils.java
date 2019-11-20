package cn.oscrazy.mytimer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    //简单数据存储
    private static SharedPreferences sp;

    private static MediaPlayer mediaPlayer = new MediaPlayer();

    private static File pFile = Environment.getExternalStorageDirectory();//SD卡根目录

    //歌曲路径
    private static String[] musicPaths = new String[]{
            pFile + "/MyTimerMusic/RADWIMPS - 陽菜と、走る帆高.flac",
            pFile + "/MyTimerMusic/test002.mp3",
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

    public static void setAlarmsByView(Context context){
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
    public static void startMusic(int index){
        if(!mediaPlayer.isPlaying()){
            initMediaPlayer(index);
            mediaPlayer.start();
        }
    }

    //初始化音乐
    public static void initMediaPlayer(int musicIndex){
        try {
            mediaPlayer.setDataSource(musicPaths[musicIndex]);//指定音频文件路径
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
