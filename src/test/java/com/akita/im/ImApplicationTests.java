package com.akita.im;

import com.akita.im.logic.dao.MessageInfoDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ImApplication.class)
public class ImApplicationTests {
    @Autowired
    private MessageInfoDao dao;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test(){
    }

}
