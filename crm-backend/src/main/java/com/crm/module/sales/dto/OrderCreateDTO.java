package com.crm.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "总金额不能为空")
    private BigDecimal totalAmount;

    private String status;

    private String remark;

    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private String productName;
        private Integer qty;
        private BigDecimal price;
    }
}
