package com.crm.module.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("follow_up")
public class FollowUp {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long employeeId;

    private String type;

    private LocalDateTime planTime;

    private LocalDateTime actualTime;

    private String content;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
