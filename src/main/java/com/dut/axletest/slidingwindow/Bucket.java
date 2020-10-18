package com.dut.axletest.slidingwindow;

import com.dut.axletest.datssource.LimitQueue;
import com.dut.axletest.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.basic.BasicButtonUI;
import java.util.concurrent.CountDownLatch;

/**
 * Created by CUI on 2020/10/11
 */
public class Bucket {
    private static Logger logger = LoggerFactory.getLogger(Bucket.class);

    //桶中的数据
    private LimitQueue<Message> dataQueue = new LimitQueue<Message>(Integer.MAX_VALUE);

    //第一个消息的存放时间
    private Long startTime;
    //最后一个消息的存放时间
    private Long endTime;

    //代表这个桶的消息
    private Message behalfMessageQueue;

    //同步计算
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    //如果是空桶的话 则要进行丢弃 因为没有产生任务数据
    private boolean isDrop = true;


    /**
     * 桶的信息已经收集完毕 开始计算桶的信息
     */
    public void done() {

        if(isDrop)
            return;

        double data = 0.0;
        long time = 0;

        //获得桶中的所有的数据
        for (Message message : dataQueue) {
            data += Double.valueOf(message.getData());
        }
        //对数据进行取平均值
        data = data / dataQueue.size();

        //对第一个放消息与最后一个放消息进行取均值 来代表这个桶的产生时间
        time = (this.getStartTime() + this.getEndTime()) >> 1;

        //产生可以代表这个桶的消息
        Message message = new Message();
        message.setData(data);
        message.setTime(time);
        assert dataQueue.peek() != null;
        message.setNumber(dataQueue.peek().getNumber());
        this.setBehalfMessageQueue(message);

        //表示已经计算完成
        countDownLatch.countDown();
        logger.debug("bucket信息统计完毕："+this);
    }

    /**
     * 向桶中放消息
     * @param message 具体的消息
     */
    public void put(Message message){
        if (startTime == null) {
            startTime = message.getTime();
            endTime = message.getTime();
        }
        endTime = message.getTime();
        dataQueue.add(message);
    }

    public LimitQueue getDataQueue() {
        return dataQueue;
    }

    public void setDataQueue(LimitQueue dataQueue) {
        this.dataQueue = dataQueue;
    }



    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Message getBehalfMessageQueue() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("桶中断异常:"+e.toString());
        }
        return behalfMessageQueue;
    }

    public void setBehalfMessageQueue(Message behalfMessageQueue) {
        this.behalfMessageQueue = behalfMessageQueue;
    }

    public boolean isDrop() {
        return isDrop;
    }

    public void setDrop(boolean drop) {
        isDrop = drop;
    }


    @Override
    public String toString() {
        return "Bucket{" +
                "dataQueue=" + dataQueue +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", behalfMessageQueue=" + behalfMessageQueue +
                ", countDownLatch=" + countDownLatch +
                ", isDrop=" + isDrop +
                '}';
    }
}
