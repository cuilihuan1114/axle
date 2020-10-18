package com.dut.axletest.datssource;

import com.dut.axletest.entity.Message;
import com.dut.axletest.util.Constant;
import com.dut.axletest.util.Utils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by CUI on 2020/10/10
 */
@Component
public class DataMap {
    //收集初始图像的数据
    private InitDataMap initDataMap = new InitDataMap();
    //获得增量数据
    private IntervalDataMap intervalDataMap = new IntervalDataMap();

    public DataMap() {
        init();
    }

    //用来模拟产生初始数据
    public void init() {
        for (int i = Constant.POINTS_NUMS_DYNAMIC_STITCHING_DIAGRAM; i >= 0; i--) {
            Message message = new Message();
            message.setTime(Utils.getData(i));
            message.setNumber(Constant.POINT_NUM);
            message.setData(Utils.getRandom(-2, 2));
            initDataMap.put(message);
        }
    }

    /**
     * 根据编号查找所有初始化数据
     * @param number  查询的编号
     * @return  返回一个Message集合
     */
    public List<Message> getInitAllMessage(long number) {
        intervalDataMap.remove(number);
        return initDataMap.get(number);
    }

    /**
     * 根据编号来查找增量的消息
     * @param number 查询的编号
     * @return 返回的Message集合
     */
    public List<Message> getIncreamMessage(long number) {
        return intervalDataMap.get(number);
    }


    /**
     * 将产生的消息放入到集合当中
     * @param message 消息体
     */
    public void put(Message message){
        initDataMap.put(message);
        intervalDataMap.put(message);
    }



    public static void main(String[] args) {
        DataMap dataMap = new DataMap();
        //dataMap.getData(0);
        //dataMap.getData(1);
    }



}
