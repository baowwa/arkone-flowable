package com.arkone.flowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ArkOne Flowable 主应用
 *
 * @author ArkOne Team
 * @since 2026-02-25
 */
@SpringBootApplication
@MapperScan("com.arkone.flowable.repository")
public class FlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class, args);
        System.out.println("""

            ========================================
            ArkOne Flowable 启动成功!
            ========================================
            API文档: http://localhost:8080/api/doc.html
            ========================================
            """);
    }
}
