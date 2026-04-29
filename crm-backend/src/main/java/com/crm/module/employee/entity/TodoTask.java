package com.crm.module.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("todo_task")
public class TodoTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private String type;

    private String content;

    private Long customerId;

    private LocalDateTime deadline;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
