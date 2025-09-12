package com.example.demothree.customer.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客户额度表
 * </p>
 *
 * @author GoryLee
 * @since 2025-09-12 15:02:07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("crm_quota")
public class Quota implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 额度内部号
     */
    @TableField("quota_no")
    private String quotaNo;

    /**
     * 额度名称
     */
    @TableField("name")
    private String name;

    /**
     * 总额度
     */
    @TableField("credit_quota")
    private BigDecimal creditQuota;

    /**
     * 额度说明
     */
    @TableField("description")
    private String description;

    /**
     * 截止时间
     */
    @TableField("deadline_date")
    private LocalDateTime deadlineDate;

    /**
     * 事业部
     */
    @TableField("division")
    private Integer division;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人id
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建人名称
     */
    @TableField("creator_name")
    private String creatorName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField("modifier_id")
    private Long modifierId;

    /**
     * 修改人名称
     */
    @TableField("modifier_name")
    private String modifierName;

    /**
     * 修改时间
     */
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    /**
     * 正常 0 已删除 1  
     */
    @TableField("deleted")
    private Boolean deleted;
}
