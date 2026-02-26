package com.arkone.flowable.service;

import com.arkone.flowable.dto.SampleCreateRequest;
import com.arkone.flowable.dto.SampleQueryRequest;
import com.arkone.flowable.dto.SampleResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 样本服务接口
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
public interface SampleService {

    /**
     * 创建样本
     *
     * @param request 样本创建请求
     * @return 样本响应
     */
    SampleResponse createSample(SampleCreateRequest request);

    /**
     * 分页查询样本列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<SampleResponse> querySamples(SampleQueryRequest request);

    /**
     * 获取样本详情
     *
     * @param id 样本ID
     * @return 样本响应
     */
    SampleResponse getSampleById(String id);

    /**
     * 更新样本状态
     *
     * @param id     样本ID
     * @param status 新状态
     * @return 样本响应
     */
    SampleResponse updateSampleStatus(String id, String status);
}
