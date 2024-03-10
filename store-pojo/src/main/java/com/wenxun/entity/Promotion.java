package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
public class Promotion implements Serializable {

    private Integer id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Integer itemId;
    private BigDecimal promotionPrice;

    public boolean getStatus(){
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if(now.before(startTime)){
            return false;
        }
        else if(now.after(endTime)){
            return false;
        }
        return true;
    }
}