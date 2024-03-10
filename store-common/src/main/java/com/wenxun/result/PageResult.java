package com.wenxun.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询的返回结果 total表示当前页数 records 表示当前页数据集合
 * @author wenxun
 * @date 2024.03.07 10:23
 */
public class PageResult implements Serializable {
    private long total;
    private List records;
}
