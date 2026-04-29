package com.crm.module.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee_performance")
public class Performance {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private String month;

    private BigDecimal salesAmount;

    private Integer newCustomers;

    private Integer followUpCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
