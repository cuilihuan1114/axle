package com.dut.axletest.datssource;

import com.dut.axletest.entity.Message;
import com.dut.axletest.util.Constant;

import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class InitDataMap {
    //用来保存一个初始化为POINTS_NUMS_DYNAMIC_STITCHING_DIAGRAM的集合
    private Map<Long, Queue<Message>> dataMap = new HashMap<>();

    /**
     * 向集合当中放入数据
     * @param message 消息体
     * @return 返回是否放入成功
     */
    public boolean put(Message message) {
        if(message == null)
            return false;
        if (dataMap.containsKey(message.getNumber())) { //如果之前已经存放过了 直接获得对应的队列 放入消息
            Queue<Message> messageQueue = dataMap.get(message.getNumber());
            return messageQueue.offer(message);
        }else {//为这个id创建一个对应的队列
            Queue<Message> queue = new LimitQueue<>(Constant.POINTS_NUMS_DYNAMIC_STITCHING_DIAGRAM);
            queue.offer(message);
            dataMap.put(message.getNumber(), queue);
        }
        return true;
    }

    /**
     * 根据编号从集合当中拿取对应的消息列表
     * @param number 编号
     * @return 返回消息集合
     */
    public List<Message> get(Long number) {
        List<Message> list = new LinkedList<>();
        if (dataMap.containsKey(number)) {
            Queue<Message> messageQueue = dataMap.get(number);
            for (Message message : messageQueue) {
                list.add(message);
            }
        }
        return list;
    }


}
