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

    //起始时间
    private Long startTime;
    private Long endTime;

    //代表这个桶的值
    private Message behalfMessageQueue;

    //同步计算
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void done() {
        countDownLatch.countDown();
        logger.debug("bucket信息统计完毕");
    }

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

    @Override
    public String toString() {
        return "Bucket{" +
                "dataQueue=" + dataQueue +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", behalfMessageQueue=" + behalfMessageQueue +
                ", countDownLatch=" + countDownLatch +
                '}';
    }
}
