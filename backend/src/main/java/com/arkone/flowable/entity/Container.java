package com.arkone.flowable.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 容器实体
 * 对应数据库表：lims_container
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("lims_container")
public class Container extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 容器编码，唯一标识
     */
    @TableField("container_code")
    private String containerCode;

    /**
     * 容器类型
     * 例如：96孔板、48深孔板、EP管等
     */
    @TableField("container_type")
    private String containerType;

    /**
     * 容器容量（最大可容纳样本数）
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 已使用数量
     */
    @TableField("used_count")
    private Integer usedCount;

    /**
     * 容器状态
     * active: 可用
     * full: 已满
     * retired: 已退役
     */
    @TableField("status")
    private String status;
}
