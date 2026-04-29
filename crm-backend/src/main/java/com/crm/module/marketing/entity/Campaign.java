package com.crm.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("marketing_campaign")
public class Campaign {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private String targetGroup;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
