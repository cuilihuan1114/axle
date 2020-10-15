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

    public boolean put(Message message) {
        if(message == null)
            return false;
        if (dataMap.containsKey(message.getNumber())) {
            Queue<Message> messageQueue = dataMap.get(message.getNumber());
            return messageQueue.offer(message);
        }else {
            Queue<Message> queue = new LimitQueue<>(Constant.POINTS_NUMS_DYNAMIC_STITCHING_DIAGRAM);
            queue.offer(message);
            dataMap.put(message.getNumber(), queue);
        }
        return true;
    }

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
