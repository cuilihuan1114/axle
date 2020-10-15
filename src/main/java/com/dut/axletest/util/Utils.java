package com.dut.axletest.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by CUI on 2020/10/10
 */
public class Utils {
    //返回延迟时间
    public static long getData(int delay) {

        return System.currentTimeMillis() - delay * 1000;
    }

    public static Double getRandom(double min, double max) {
        return min + ((max - min) * new Random().nextDouble());
    }
}
