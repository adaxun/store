package com.wenxun.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 手撕雪花算法
 * 长度 64 Long类型
 * 0-41-5-5-12
 * 0 符号位
 * 41 时间戳
 * 5 数据中心
 * 5 机器id
 * 12 自增序列
 * @author wenxun
 * @date 2024.03.14 11:48
 */
public class SnowflakeUtils {
    //通过 sout(System.currenTimeMillis()) 获取到当前时间戳

    private static final long  BEGIN_TIME =1577836800000L;
    private static final long TIME_BITS = 41L;
    private static final long DATA_CENTER_BITS = 5L;
    private static final long COMPUTER_BITS = 5L;
    private static final long QUEUE_BITS = 12L;
    private static final AtomicLong INDEX = new AtomicLong();
    private static long dataCenter=1;
    private static long computer=1;
    /**
     *  通过二进制获取最大值
     *  -1L: 1111111111111111111   -1<<5       1111111111111100000
     *  -1 ^ -1<<5 = 000000000011111 ,是正数的最大值
     */
    private static final long  DATA_CENTER_LIMIT = -1L ^(-1L<<DATA_CENTER_BITS);
    private static final long  COMPUTER_LIMIT = -1L ^(-1L<<COMPUTER_BITS);
    private  static final long QUEUE_LIMIT = -1^(-1L<<QUEUE_BITS);

    private  static long lastTime=-1L;

    public SnowflakeUtils(long dataCenter ,long computer){
        if(dataCenter<0||computer<0||dataCenter>DATA_CENTER_LIMIT||computer>COMPUTER_LIMIT){
            throw new IllegalArgumentException("数据中心id和机器id设定错误");
        }
        SnowflakeUtils.dataCenter = dataCenter;
        SnowflakeUtils.computer = computer;
    }

    public synchronized static long getId(){
        if(lastTime==-1L){
            lastTime=System.currentTimeMillis();
        }
        long now = System.currentTimeMillis();
        if(now<lastTime){
            throw new RuntimeException("雪花算法_时间戳错误");
        }
        INDEX.incrementAndGet();
            if(now==lastTime&&INDEX.get()>QUEUE_LIMIT){
                //超过了限制 则只能等下一毫秒
                while(now<=lastTime){
                    now=System.currentTimeMillis();
                }
                INDEX.set(0L);
            }
            lastTime=now;
        return (now-BEGIN_TIME)<<(DATA_CENTER_BITS+COMPUTER_BITS+QUEUE_BITS)|
                dataCenter<<(COMPUTER_BITS+QUEUE_BITS)|
                computer<<(QUEUE_BITS)|INDEX.get();
    }

}
