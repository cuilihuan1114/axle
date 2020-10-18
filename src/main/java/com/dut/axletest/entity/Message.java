package com.dut.axletest.entity;

public class Message {

    //轴承所在的单位名
    private String ownerShip;

    //消息产生的时间
    private long time;

    //产生消息的轴承编号
    private long number;

    //消息的具体数据
    private Double data;

    public String getOwnerShip() {
        return ownerShip;
    }

    public void setOwnerShip(String ownerShip) {
        this.ownerShip = ownerShip;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }



    @Override
    public String toString() {
        return "Message{" +
                "ownerShip='" + ownerShip + '\'' +
                ", time='" + time + '\'' +
                ", number=" + number +
                ", data='" + data + '\'' +
                '}';
    }
}