package com.crm.module.sales.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.sales.dto.OrderCreateDTO;
import com.crm.module.sales.entity.Order;
import com.crm.module.sales.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> pageQuery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {

        Page<Map<String, Object>> pageResult = orderService.pageQuery(
                new Page<>(page, size), customerName, status
        );

        PageResult<Map<String, Object>> result = PageResult.of(
                pageResult.getTotal(), pageResult.getRecords()
        );
        return Result.ok(result);
    }

    @PostMapping("/orders")
    public Result<Order> create(@Valid @RequestBody OrderCreateDTO dto) {
        Order order = orderService.create(dto);
        return Result.ok(order);
    }

    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = orderService.getDetail(id);
        return Result.ok(detail);
    }

    @GetMapping("/sales/funnel")
    public Result<Map<String, Long>> getFunnel() {
        Map<String, Long> funnel = orderService.getFunnel();
        return Result.ok(funnel);
    }
}
