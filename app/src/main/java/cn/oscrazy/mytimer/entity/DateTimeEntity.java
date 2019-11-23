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
                return "にちようび(日)";
            case 2:
                return "げつようび(一)";
            case 3:
                return "かようび(二)";
            case 4:
                return "すいようび(三)";
            case 5:
                return "もくようび(四)";
            case 6:
                return "きんようび(五)";
            case 7:
                return "どようび(六)";
            default:
                return "なんようび(未知)";
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
