package com.crm.module.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BusinessException;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.entity.CustomerLead;
import com.crm.module.customer.mapper.CustomerLeadMapper;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.employee.entity.Performance;
import com.crm.module.employee.mapper.PerformanceMapper;
import com.crm.module.sales.dto.OrderCreateDTO;
import com.crm.module.sales.entity.Order;
import com.crm.module.sales.entity.OrderItem;
import com.crm.module.sales.mapper.OrderItemMapper;
import com.crm.module.sales.mapper.OrderMapper;
import com.crm.security.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CustomerMapper customerMapper;
    private final CustomerLeadMapper customerLeadMapper;
    private final PerformanceMapper performanceMapper;

    public Page<Map<String, Object>> pageQuery(Page<Order> page, String customerName, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);
        List<Order> orders = orderPage.getRecords();

        // Enrich with customer names
        List<Map<String, Object>> records = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("customerId", order.getCustomerId());
            map.put("employeeId", order.getEmployeeId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("status", order.getStatus());
            map.put("remark", order.getRemark());
            map.put("createdAt", order.getCreatedAt());
            map.put("updatedAt", order.getUpdatedAt());

            Customer customer = customerMapper.selectById(order.getCustomerId());
            map.put("customerName", customer != null ? customer.getName() : "未知");
            return map;
        }).collect(Collectors.toList());

        // Filter by customerName if provided
        if (StringUtils.hasText(customerName)) {
            records = records.stream()
                    .filter(m -> {
                        String name = (String) m.get("customerName");
                        return name != null && name.contains(customerName);
                    })
                    .collect(Collectors.toList());
        }

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), records.size());
        // Apply pagination manually (since we filtered in memory for customer name join)
        int offset = (int) ((page.getCurrent() - 1) * page.getSize());
        int end = Math.min(offset + (int) page.getSize(), records.size());
        if (offset >= records.size() || offset < 0) {
            resultPage.setRecords(List.of());
        } else {
            resultPage.setRecords(records.subList(offset, end));
        }
        // If no customer name filter, we can use the original total from DB
        if (!StringUtils.hasText(customerName)) {
            resultPage.setTotal(orderPage.getTotal());
        }
        return resultPage;
    }

    @Transactional
    public Order create(OrderCreateDTO dto) {
        UserDetail userDetail = getUserDetail();

        // Create order
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setEmployeeId(userDetail.getId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : "待付款");
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // Create order items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setProductName(itemDTO.getProductName());
                item.setQty(itemDTO.getQty());
                item.setPrice(itemDTO.getPrice());
                item.setCreatedAt(LocalDateTime.now());
                orderItemMapper.insert(item);
            }
        }

        // Update employee performance
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Performance performance = performanceMapper.selectOne(
                new LambdaQueryWrapper<Performance>()
                        .eq(Performance::getEmployeeId, userDetail.getId())
                        .eq(Performance::getMonth, month)
        );
        if (performance == null) {
            performance = new Performance();
            performance.setEmployeeId(userDetail.getId());
            performance.setMonth(month);
            performance.setSalesAmount(dto.getTotalAmount());
            performance.setNewCustomers(0);
            performance.setFollowUpCount(0);
            performance.setCreatedAt(LocalDateTime.now());
            performance.setUpdatedAt(LocalDateTime.now());
            performanceMapper.insert(performance);
        } else {
            performance.setSalesAmount(
                    performance.getSalesAmount() != null
                            ? performance.getSalesAmount().add(dto.getTotalAmount())
                            : dto.getTotalAmount()
            );
            performance.setUpdatedAt(LocalDateTime.now());
            performanceMapper.updateById(performance);
        }

        return order;
    }

    public Map<String, Object> getDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("customerId", order.getCustomerId());
        result.put("employeeId", order.getEmployeeId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("status", order.getStatus());
        result.put("remark", order.getRemark());
        result.put("createdAt", order.getCreatedAt());
        result.put("updatedAt", order.getUpdatedAt());

        Customer customer = customerMapper.selectById(order.getCustomerId());
        result.put("customerName", customer != null ? customer.getName() : "未知");

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, id)
        );
        result.put("items", items);

        return result;
    }

    public Map<String, Long> getFunnel() {
        Map<String, Long> result = new HashMap<>();

        long totalLeads = customerLeadMapper.selectCount(
                new LambdaQueryWrapper<CustomerLead>()
        );
        long leadCount = customerLeadMapper.selectCount(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getStatus, "跟进中")
        );
        long dealCount = customerLeadMapper.selectCount(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getStatus, "已转化")
        );
        // intentionCount = total - lead - deal (已流失 or other states are intentional prospects)
        long intentionCount = totalLeads - leadCount - dealCount;
        if (intentionCount < 0) intentionCount = 0;

        result.put("leadCount", leadCount);
        result.put("intentionCount", intentionCount);
        result.put("dealCount", dealCount);
        return result;
    }

    private UserDetail getUserDetail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetail)) {
            UserDetail defaultUser = new UserDetail();
            defaultUser.setId(1L);
            defaultUser.setUsername("admin");
            defaultUser.setRole("ADMIN");
            return defaultUser;
        }
        return (UserDetail) auth.getPrincipal();
    }
}
