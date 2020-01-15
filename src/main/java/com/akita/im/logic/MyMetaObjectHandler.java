package com.akita.im.logic;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
        this.fillStrategy(metaObject, "createdTime", new Date());
        this.strictInsertFill(metaObject, "updatedTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
        this.fillStrategy(metaObject, "updatedTime", new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updatedTime", Date.class, new Date()); // 起始版本 3.3.0(推荐使用)
        this.fillStrategy(metaObject, "updatedTime", new Date());
    }
}
