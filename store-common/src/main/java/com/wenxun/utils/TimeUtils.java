package com.wenxun.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 根据patten格式 生成日期字符串
 * @author wenxun
 * @date 2024.03.10 10:23
 */
public class TimeUtils {

    public static String format(Date date, String patten){
        return new SimpleDateFormat(patten).format(date);
    }
}
