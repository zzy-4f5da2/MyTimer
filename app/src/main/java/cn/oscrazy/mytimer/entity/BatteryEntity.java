package cn.oscrazy.mytimer.entity;

public class BatteryEntity {

    private String date;
    private String time;
    private String battery;
    private String temp;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "BatteryEntity{" +
                "日期='" + date + '\'' +
                ", 时间='" + time + '\'' +
                ", 电量='" + battery + '\'' +
                ", 温度='" + temp + '\'' +
                '}';
    }
}
