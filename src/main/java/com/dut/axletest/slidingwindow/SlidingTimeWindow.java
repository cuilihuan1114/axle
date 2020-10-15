package com.dut.axletest.slidingwindow;

import com.dut.axletest.datssource.DataMap;
import com.dut.axletest.datssource.LimitQueue;
import com.dut.axletest.entity.Message;
import com.dut.axletest.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by CUI on 2020/10/11
 */
@Component
public class SlidingTimeWindow {

    private static final Logger log = LoggerFactory.getLogger(SlidingTimeWindow.class);

    private HashMap<Long, LimitQueue<Bucket>> windowDataMap = new HashMap<>();
    private HashMap<Long, Bucket> currentBucket = new HashMap<>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    private DataMap produceDataMap;

    public void putMessage(Message message) {
        if (!currentBucket.containsKey(message.getNumber())) {
            startCollectData(message.getNumber());
        }
        Bucket bucket = currentBucket.get(message.getNumber());
        bucket.put(message);
    }

    public void startCollectData(long id) {
        Timer timer = new Timer();
        if (checkInit(id))
            return;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //产生新的桶
                Bucket newBucket = new Bucket();
                //之前的桶
                Bucket prebucket = currentBucket.get(id);
                //用来计算旧桶的一些信息
                calculationBucketInfo(prebucket);

                //如果当前滑动窗口的桶数已经满了 表示可以产生新数据了
                if (windowDataMap.get(id).size() >= Constant.BUCKETS_SLIDING_WINDOWS) {
                    log.debug("滑动窗口已满,当前桶的个数:" + windowDataMap.get(id).size());
                    produceBehalfMessage(windowDataMap.get(id));
                }
                windowDataMap.get(id).offer(newBucket);
                currentBucket.put(id, newBucket);

                //下一次在规定的时间内唤醒这个线程
            }
        }, Constant.SLIDING_WINDOW_PERUNIT_TIME / Constant.BUCKETS_SLIDING_WINDOWS, Constant.SLIDING_WINDOW_PERUNIT_TIME / Constant.BUCKETS_SLIDING_WINDOWS);


    }

    private boolean checkInit(long id) {
        if (!currentBucket.containsKey(id)) {
            Bucket initBucket = new Bucket();
            currentBucket.put(id, initBucket);
            LimitQueue<Bucket> limitQueue = new LimitQueue<>(Constant.BUCKETS_SLIDING_WINDOWS);
            limitQueue.add(initBucket);
            windowDataMap.put(id, limitQueue);
            return false;
        }
        return true;
    }

    private void produceBehalfMessage(LimitQueue<Bucket> buckets) {
        Double data = 0.0;
        long time = 0;
        Message message = new Message();
        for (Bucket bucket : buckets) {
            Message behalfMessageQueue = bucket.getBehalfMessageQueue();
            data += behalfMessageQueue.getData();
            time += behalfMessageQueue.getTime();
        }
        data = data / Constant.BUCKETS_SLIDING_WINDOWS;
        time = time / Constant.BUCKETS_SLIDING_WINDOWS;
        message.setData(data);
        message.setTime(time);
        message.setNumber(buckets.peek().getBehalfMessageQueue().getNumber());
        produceDataMap.put(message);

    }

    private void calculationBucketInfo(final Bucket bucket) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LimitQueue<Message> dataQueue = bucket.getDataQueue();
                    double data = 0.0;
                    long time = 0;
                    for (Message message : dataQueue) {
                        data += Double.valueOf(message.getData());
                    }
                    data = data / dataQueue.size();
                    time = (bucket.getStartTime() + bucket.getEndTime()) >> 1;
                    Message message = new Message();
                    message.setData(data);
                    message.setTime(time);
                    message.setNumber(dataQueue.peek().getNumber());
                    bucket.setBehalfMessageQueue(message);
                    bucket.done();
                } catch (Exception e) {
                    log.error("计算桶信息发生异常,异常信息为" + e.toString() + "桶的信息为:" + bucket);
                }
            }
        });
    }


}
