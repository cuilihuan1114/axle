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

    //一个滑动窗口下面所有的桶
    private HashMap<Long, LimitQueue<Bucket>> windowBucketDataMap = new HashMap<>();
    //当前产生的消息存放的对应的桶
    private HashMap<Long, Bucket> currentBucket = new HashMap<>();

    //线程池 核心大小为cpu的核心数
    private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    private DataMap rowDataMap;


    /**
     * 向桶中存放消息
     * @param message 消息
     */
    public void putMessage(Message message) {
        //如果桶之前不存在 则要开启这个桶对应的定时器 也就是用来开启对应的滑动窗口
        if (!currentBucket.containsKey(message.getNumber())) {
            startCollectData(message.getNumber());
        }
        Bucket bucket = currentBucket.get(message.getNumber());
        bucket.setDrop(false);
        bucket.put(message);
    }

    /**
     * 根据轴承id 开启这个id对应的滑动窗口
     * @param id  轴承编号
     */
    public void startCollectData(long id) {
        Timer timer = new Timer();
        //检测之前是否已经初始化 如果没有则会进行初始化
        if (checkInit(id))
            return;
        //定时器 每隔一个桶的时间会产生一个新的桶 并把窗口向移动一个桶的大小位置
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    //产生新的桶
                    Bucket newBucket = new Bucket();
                    //之前的桶
                    Bucket prebucket = currentBucket.get(id);
                    currentBucket.put(id, newBucket);

                    //用来计算旧桶的一些信息
                    calculationBucketInfo(prebucket);
                    //如果当前滑动窗口的桶数已经满了
                    if (windowBucketDataMap.get(id).size() >= Constant.BUCKETS_SLIDING_WINDOWS) {
                        log.debug("滑动窗口已满,当前桶的个数:" + windowBucketDataMap.get(id).size());
                        //用来计算滑动容器的代表值
                        produceBehalfMessage(windowBucketDataMap.get(id));
                    }
                    windowBucketDataMap.get(id).offer(newBucket);
                }catch (Exception e){
                    System.out.println(e);
                }


                //下一次在规定的时间内唤醒这个线程
            }
        }, Constant.SLIDING_WINDOW_PERUNIT_TIME / Constant.BUCKETS_SLIDING_WINDOWS, Constant.SLIDING_WINDOW_PERUNIT_TIME / Constant.BUCKETS_SLIDING_WINDOWS);


    }

    /**
     * 根据id来检测是否初始化 没有则进行实始化
     * @param id 对应的轴承编号
     * @return 返回是否之前初始化过
     */
    private boolean checkInit(long id) {
        if (!currentBucket.containsKey(id)) {
            Bucket initBucket = new Bucket(); //产生一个初始化的桶
            currentBucket.put(id, initBucket);
            LimitQueue<Bucket> limitQueue = new LimitQueue<>(Constant.BUCKETS_SLIDING_WINDOWS); //产生用来存放桶的一个队列
            limitQueue.add(initBucket);
            windowBucketDataMap.put(id, limitQueue);
            return false;
        }
        return true;
    }

    /**
     * 计算滑动窗口的代表信息
     * @param buckets 滑动窗口中的所有桶
     */
    private void produceBehalfMessage(LimitQueue<Bucket> buckets) {
        Double data = 0.0;
        long time = 0;
        Message message = new Message();
        int size = 0;
        long number=-1;
        try {
            //遍历所有的桶的信息
            for (Bucket bucket : buckets) {
                if(bucket.isDrop())
                    continue;
                //获取每一个桶的代表
                Message behalfMessageQueue = bucket.getBehalfMessageQueue();
                data += behalfMessageQueue.getData();
                time += behalfMessageQueue.getTime();
                number = behalfMessageQueue.getNumber();
                size++;
            }
            if(size==0)
                return;
            //计算均值
            data = data / size;
            time = time / size;
            message.setData(data);
            message.setTime(time);
            message.setNumber(number);

            //将产生的数据保存起来
            rowDataMap.put(message);
        }catch (Exception e){
           e.printStackTrace();
        }

    }

    /**
     * 当一个桶收集完毕 计算桶的信息
     * @param bucket 桶
     */
    private void calculationBucketInfo(final Bucket bucket) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //进行收集
                    bucket.done();
                } catch (Exception e) {
                    log.error("计算桶信息发生异常,异常信息为" + e.toString() + "桶的信息为:" + bucket);
                }
            }
        });
    }


}
