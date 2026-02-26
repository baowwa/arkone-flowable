package com.arkone.flowable.repository;

import com.arkone.flowable.entity.Container;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 容器数据访问层
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Mapper
public interface ContainerMapper extends BaseMapper<Container> {

    /**
     * 根据容器编码查询容器
     *
     * @param containerCode 容器编码
     * @return 容器实体
     */
    default Container selectByContainerCode(String containerCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Container>()
                .eq(Container::getContainerCode, containerCode));
    }

    /**
     * 根据容器类型查询容器列表
     *
     * @param containerType 容器类型
     * @return 容器列表
     */
    default java.util.List<Container> selectByContainerType(String containerType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Container>()
                .eq(Container::getContainerType, containerType)
                .orderByDesc(Container::getCreatedAt));
    }

    /**
     * 根据状态查询容器列表
     *
     * @param status 容器状态
     * @return 容器列表
     */
    default java.util.List<Container> selectByStatus(String status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Container>()
                .eq(Container::getStatus, status)
                .orderByDesc(Container::getCreatedAt));
    }

    /**
     * 查询可用容器（未满且状态为active）
     *
     * @param containerType 容器类型
     * @return 可用容器列表
     */
    default java.util.List<Container> selectAvailableContainers(String containerType) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Container>()
                .eq("container_type", containerType)
                .eq("status", "active")
                .apply("used_count < capacity")
                .orderByAsc("used_count"));
    }
}
