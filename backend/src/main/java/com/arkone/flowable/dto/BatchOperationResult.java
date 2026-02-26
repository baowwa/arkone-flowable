package com.arkone.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量操作结果DTO
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResult<T> {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failureCount;

    /**
     * 成功的数据列表
     */
    private List<T> successList;

    /**
     * 失败的数据列表
     */
    private List<BatchOperationError> failureList;

    /**
     * 批量操作错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOperationError {
        /**
         * 索引位置
         */
        private Integer index;

        /**
         * 错误码
         */
        private String errorCode;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 原始数据
         */
        private Object data;
    }
}
