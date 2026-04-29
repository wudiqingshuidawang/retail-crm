package com.crm.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupon_record")
public class CouponRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private Long customerId;

    private String status;

    private LocalDateTime sendTime;

    private LocalDateTime useTime;

    private LocalDateTime createdAt;
}
