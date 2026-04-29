package com.crm.module.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point_record")
public class PointRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String type;

    private Integer points;

    private String reason;

    private LocalDateTime createdAt;
}
