package com.system.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//元数据对象处理器
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    // 插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "date", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "submittedTime", LocalDateTime::now, LocalDateTime.class);
    }

    // 修改时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        // 使用当前用户的用户名填充"lastUpdatedBy"字段
        this.fillStrategy(metaObject, "lastUpdatedBy", UserMessage.getUsername());
        // 使用当前时间填充"lastUpdateDate"字段
        this.fillStrategy(metaObject, "lastUpdateDate", LocalDateTime.now());
    }
}
