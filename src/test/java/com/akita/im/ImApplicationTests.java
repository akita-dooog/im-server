package com.akita.im;

import com.akita.im.constants.PlatformType;
import org.junit.Test;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ImApplicationTests {

    @Test
    public void contextLoads() {
        for (PlatformType type : PlatformType.values()) {
            System.out.println(type);
        }

    }

}
