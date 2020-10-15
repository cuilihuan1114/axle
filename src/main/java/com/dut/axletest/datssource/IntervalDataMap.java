package com.dut.axletest.datssource;

import com.dut.axletest.entity.Message;
import com.dut.axletest.util.Constant;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IntervalDataMap {
    private HashMap<Long, LimitQueue<Message>> writeMap = new HashMap<>();
    private HashMap<Long, LimitQueue<Message>> readMap = new HashMap<>();

    public boolean put(Message message) {
        if (!writeMap.containsKey(message.getNumber())) {
            LimitQueue<Message> limitQueue = new LimitQueue<>(Constant.POINTS_NUMS_INCREMENT);
            writeMap.put(message.getNumber(), limitQueue);
            readMap.put(message.getNumber(), new LimitQueue<>(Constant.POINTS_NUMS_INCREMENT));
            return limitQueue.offer(message);
        }else{
            return writeMap.get(message.getNumber()).offer(message);
        }
    }

    public List<Message> get(Long id) {
        List<Message> list = new LinkedList<>();
        if (!writeMap.containsKey(id)) {
            return list;
        }
        LimitQueue<Message> writeQueue = writeMap.get(id);
        LimitQueue<Message> readQueue = readMap.get(id);
        writeMap.put(id, readQueue);
        while (!writeQueue.isEmpty()) {
            list.add(writeQueue.poll());
        }
        readMap.put(id, writeQueue);
        return list;
    }

    public void remove(Long id) {
        if (writeMap.containsKey(id)) {
            writeMap.put(id, new LimitQueue<>(Constant.POINTS_NUMS_INCREMENT));
        }
    }


}
