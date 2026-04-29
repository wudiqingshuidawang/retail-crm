package com.crm.module.analytics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.entity.CustomerLead;
import com.crm.module.customer.mapper.CustomerLeadMapper;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.sales.entity.Order;
import com.crm.module.sales.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CustomerMapper customerMapper;
    private final CustomerLeadMapper customerLeadMapper;
    private final OrderMapper orderMapper;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total customers (status = 1 means active)
        Long totalCustomers = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getStatus, 1)
        );
        stats.put("totalCustomers", totalCustomers);

        // New customers this month
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        Long newCustomersThisMonth = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getStatus, 1)
                        .ge(Customer::getCreatedAt, monthStart)
                        .lt(Customer::getCreatedAt, monthEnd)
        );
        stats.put("newCustomersThisMonth", newCustomersThisMonth);

        // Pending leads (跟进中)
        Long pendingLeads = customerLeadMapper.selectCount(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getStatus, "跟进中")
        );
        stats.put("pendingLeads", pendingLeads);

        // Monthly sales (this month)
        BigDecimal monthlySales = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreatedAt, monthStart)
                        .lt(Order::getCreatedAt, monthEnd)
        ).stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthlySales", monthlySales);

        return stats;
    }

    public List<Map<String, Object>> getSalesTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();

        // Last 7 days
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            BigDecimal amount = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .ge(Order::getCreatedAt, dayStart)
                            .lt(Order::getCreatedAt, dayEnd)
            ).stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            point.put("amount", amount);
            trend.add(point);
        }

        return trend;
    }
}
