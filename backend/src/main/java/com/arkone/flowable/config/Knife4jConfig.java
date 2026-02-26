package com.arkone.flowable.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArkOne Flowable API")
                        .version("1.0.0")
                        .description("测序流程管理系统API文档")
                        .contact(new Contact()
                                .name("ArkOne Team")
                                .email("support@arkone.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public GroupedOpenApi sampleApi() {
        return GroupedOpenApi.builder()
                .group("1. 样本管理")
                .pathsToMatch("/samples/**")
                .build();
    }

    @Bean
    public GroupedOpenApi batchApi() {
        return GroupedOpenApi.builder()
                .group("2. 批量操作")
                .pathsToMatch("/batch/**")
                .build();
    }

    @Bean
    public GroupedOpenApi processApi() {
        return GroupedOpenApi.builder()
                .group("3. 流程管理")
                .pathsToMatch("/process/**")
                .build();
    }

    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("4. 任务管理")
                .pathsToMatch("/tasks/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fieldApi() {
        return GroupedOpenApi.builder()
                .group("5. 字段定义")
                .pathsToMatch("/fields/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. 全部接口")
                .pathsToMatch("/**")
                .build();
    }
}
