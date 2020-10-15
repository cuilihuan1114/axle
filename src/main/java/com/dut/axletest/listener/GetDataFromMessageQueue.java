package com.dut.axletest.listener;

import com.dut.axletest.entity.Message;
import com.dut.axletest.slidingwindow.SlidingTimeWindow;
import com.dut.axletest.util.Constant;
import com.dut.axletest.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


@Component
public class GetDataFromMessageQueue implements ApplicationListener {

    @Autowired
    SlidingTimeWindow slidingTimeWindow;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            getDataFromMessage();

        }
    }

    public void getDataFromMessage(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = new Message();
                    message.setTime(Utils.getData(0));
                    message.setNumber(Constant.POINT_NUM);
                    message.setData(Utils.getRandom(-2, 2));
                    slidingTimeWindow.putMessage(message);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }



}
