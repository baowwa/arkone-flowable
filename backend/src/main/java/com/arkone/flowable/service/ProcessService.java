package com.arkone.flowable.service;

import com.arkone.flowable.dto.ProcessInstanceResponse;
import com.arkone.flowable.dto.ProcessStartRequest;

/**
 * 流程管理服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface ProcessService {

    /**
     * 启动全质粒测序流程
     *
     * @param request 流程启动请求
     * @return 流程实例响应
     */
    ProcessInstanceResponse startPlasmidProcess(ProcessStartRequest request);

    /**
     * 启动PCR产物测序流程
     *
     * @param request 流程启动请求
     * @return 流程实例响应
     */
    ProcessInstanceResponse startPcrProcess(ProcessStartRequest request);
}
