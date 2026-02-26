package com.arkone.flowable.entity;

import com.arkone.flowable.common.typehandler.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志实体
 * 对应数据库表：lims_audit_log
 * 记录系统所有重要操作的审计信息
 *
 * @author ArkOne Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lims_audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 操作用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 操作动作
     * CREATE: 创建
     * UPDATE: 更新
     * DELETE: 删除
     * QUERY: 查询
     * EXPORT: 导出
     * IMPORT: 导入
     */
    @TableField("action")
    private String action;

    /**
     * 实体类型
     * 例如：Project、Sample、Container等
     */
    @TableField("entity_type")
    private String entityType;

    /**
     * 实体ID
     */
    @TableField("entity_id")
    private String entityId;

    /**
     * 修改前的值（JSONB格式）
     */
    @TableField(value = "old_value", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> oldValue;

    /**
     * 修改后的值（JSONB格式）
     */
    @TableField(value = "new_value", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> newValue;

    /**
     * 操作IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
