package com.wenxun;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;

/**
 * @author wenxun
 * @date 2024.03.14 9:35
 */

public class BloomFilterTest {


    @Test
    public  void test(){
        Integer capacity = 1000;
        double errorRate = 0.01;

        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(),capacity,errorRate);

        for(int i=0;i<capacity;i++){
            filter.put(i);
        }

        int count=0;

        for(int i=capacity;i<capacity*2;i++){
            if(filter.mightContain(i)){
                count++;
            }
        }
        System.out.println("错误率:"+(count*1.0/capacity));

    }
}
