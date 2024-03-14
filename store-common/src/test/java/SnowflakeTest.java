import com.wenxun.utils.SnowflakeUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

/**
 * @author wenxun
 * @date 2024.03.14 13:47
 */
public class SnowflakeTest {

    @Test
    public void snakeflakeTest() {
//        SnowflakeUtils snowflakeUtils = new SnowflakeUtils();
//        System.out.println(snowflakeUtils.nextId());
        SnowflakeUtils snowflakeUtils = new SnowflakeUtils(1,1);
        HashSet<Long> hashSet= new HashSet<>();
        for(int i=0;i<1000;i++){
            hashSet.add(snowflakeUtils.getId());
        }
        System.out.println(hashSet.size());
    }
}
