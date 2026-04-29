package com.crm.module.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_lead")
public class CustomerLead {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long employeeId;

    private String status;

    private Integer isPool;

    private Integer followCount;

    private LocalDateTime lastFollowAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
