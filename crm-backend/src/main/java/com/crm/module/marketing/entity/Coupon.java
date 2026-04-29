package com.crm.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long campaignId;

    private String name;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private Integer totalQty;

    private Integer usedQty;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
