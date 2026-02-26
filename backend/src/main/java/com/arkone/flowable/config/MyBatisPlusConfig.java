package com.arkone.flowable.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 * 配置字段自动填充
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }


    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);

        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 填充创建人（TODO: 从上下文获取当前用户）
        this.strictInsertFill(metaObject, "createdBy", String.class, "system");

        // 填充更新人
        this.strictInsertFill(metaObject, "updatedBy", String.class, "system");

        // 填充版本号
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

        // 填充更新人（TODO: 从上下文获取当前用户）
        this.strictUpdateFill(metaObject, "updatedBy", String.class, "system");
    }
}
