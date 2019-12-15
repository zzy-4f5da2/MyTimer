package cn.oscrazy.mytimer.entity;

import cn.oscrazy.mytimer.Lunar;

public class DateTimeEntity {

    private int hour;
    private int minute;
    private int second;
    private String ampm;
    private int year;
    private int month;
    private int day;
    private int week;

    private Lunar lunar;

    public Lunar getLunar() {
        return lunar;
    }

    public void setLunar(Lunar lunar) {
        this.lunar = lunar;
    }

    public String getAmpm() {
        return ampm;
    }

    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month + 1;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getWeek() {
        switch (week) {
            case 1:
                return "日曜日(日)";
            case 2:
                return "月曜日(一)";
            case 3:
                return "火曜日(二)";
            case 4:
                return "水曜日(三)";
            case 5:
                return "木曜日(四)";
            case 6:
                return "金曜日(五)";
            case 7:
                return "土曜日(六)";
            default:
                return "何曜日(未知)";
        }
    }

    public String getWeekByEE() {
        switch (week) {
            case 1:
                return "周日";
            case 2:
                return "周一";
            case 3:
                return "周二";
            case 4:
                return "周三";
            case 5:
                return "周四";
            case 6:
                return "周五";
            case 7:
                return "周六";
            default:
                return "未知";
        }
    }

    public int getWeekByIndex() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
