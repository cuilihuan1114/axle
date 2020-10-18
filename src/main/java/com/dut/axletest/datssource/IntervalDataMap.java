package com.dut.axletest.datssource;

import com.dut.axletest.entity.Message;
import com.dut.axletest.util.Constant;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IntervalDataMap {
    //两个集合 一个是用来写的集合 用来承受当前产生的新数据
    private final HashMap<Long, LimitQueue<Message>> writeMap = new HashMap<>();
    //读集合 如果有请示获取的时候 则会把写集合与读集合进行互换 保证一定的并发
    private final HashMap<Long, LimitQueue<Message>> readMap = new HashMap<>();

    /**
     * 向写集合当中写入消息
     * @param message 消息体
     * @return 返回是否写入成功
     */
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

    /**
     * 获得消息根据编号
     * @param id 编号的id
     * @return 返回消息的列表
     */
    public List<Message> get(Long id) {
        List<Message> list = new LinkedList<>();
        if (!writeMap.containsKey(id)) {
            return list;
        }
        LimitQueue<Message> writeQueue = writeMap.get(id);
        LimitQueue<Message> readQueue = readMap.get(id);
        writeMap.put(id, readQueue); //这里是重点 将读的队列换成写的队列 此时是一个空的队列
        while (!writeQueue.isEmpty()) { //将写的队列的消息弹出来
            list.add(writeQueue.poll());
        }
        readMap.put(id, writeQueue); //将写的队列重新当成一个读的队列 放入到读的集合当中 等待下一次进行交接
        return list;
    }

    /**
     * 根据id 将写队列移除掉 换成一个空的写队列
     * @param id 编号id
     */
    public void remove(Long id) {
        if (writeMap.containsKey(id)) {
            writeMap.put(id, new LimitQueue<>(Constant.POINTS_NUMS_INCREMENT));
        }
    }
}
