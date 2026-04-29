package com.crm.module.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BusinessException;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.employee.entity.TodoTask;
import com.crm.module.employee.mapper.TodoTaskMapper;
import com.crm.module.sales.entity.FollowUp;
import com.crm.module.sales.mapper.FollowUpMapper;
import com.crm.security.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpMapper followUpMapper;
    private final CustomerMapper customerMapper;
    private final TodoTaskMapper todoTaskMapper;

    public Page<Map<String, Object>> pageQuery(Page<FollowUp> page, String type, String status) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(FollowUp::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(FollowUp::getStatus, status);
        }
        wrapper.orderByDesc(FollowUp::getCreatedAt);

        IPage<FollowUp> followUpPage = followUpMapper.selectPage(page, wrapper);
        List<FollowUp> followUps = followUpPage.getRecords();

        List<Map<String, Object>> records = followUps.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("customerId", f.getCustomerId());
            map.put("employeeId", f.getEmployeeId());
            map.put("type", f.getType());
            map.put("planTime", f.getPlanTime());
            map.put("actualTime", f.getActualTime());
            map.put("content", f.getContent());
            map.put("status", f.getStatus());
            map.put("createdAt", f.getCreatedAt());
            map.put("updatedAt", f.getUpdatedAt());

            Customer customer = customerMapper.selectById(f.getCustomerId());
            map.put("customerName", customer != null ? customer.getName() : "未知");
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), followUpPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Transactional
    public FollowUp create(Map<String, Object> body) {
        UserDetail userDetail = getUserDetail();

        Long customerId = Long.valueOf(body.get("customerId").toString());
        String type = (String) body.get("type");
        String planTimeStr = (String) body.get("planTime");
        String content = (String) body.get("content");

        // Parse date from ISO format
        LocalDateTime planTime = null;
        if (planTimeStr != null) {
            planTime = LocalDateTime.parse(planTimeStr.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // Create follow-up record
        FollowUp followUp = new FollowUp();
        followUp.setCustomerId(customerId);
        followUp.setEmployeeId(userDetail.getId());
        followUp.setType(type);
        followUp.setPlanTime(planTime);
        followUp.setContent(content);
        followUp.setStatus("待处理");
        followUp.setCreatedAt(LocalDateTime.now());
        followUp.setUpdatedAt(LocalDateTime.now());
        followUpMapper.insert(followUp);

        // Create todo task
        TodoTask todoTask = new TodoTask();
        todoTask.setEmployeeId(userDetail.getId());
        todoTask.setType(type);
        todoTask.setContent("跟进任务: " + (content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : ""));
        todoTask.setCustomerId(customerId);
        todoTask.setDeadline(planTime);
        todoTask.setStatus("待处理");
        todoTask.setCreatedAt(LocalDateTime.now());
        todoTask.setUpdatedAt(LocalDateTime.now());
        todoTaskMapper.insert(todoTask);

        return followUp;
    }

    @Transactional
    public FollowUp update(Long id, Map<String, Object> body) {
        FollowUp followUp = followUpMapper.selectById(id);
        if (followUp == null) {
            throw new BusinessException(404, "回访记录不存在");
        }

        String actualTimeStr = (String) body.get("actualTime");
        String status = (String) body.get("status");
        String content = (String) body.get("content");

        if (actualTimeStr != null) {
            followUp.setActualTime(LocalDateTime.parse(actualTimeStr.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (status != null) {
            followUp.setStatus(status);
        }
        if (content != null) {
            followUp.setContent(content);
        }
        followUp.setUpdatedAt(LocalDateTime.now());
        followUpMapper.updateById(followUp);

        return followUpMapper.selectById(id);
    }

    @Transactional
    public FollowUp updateStatus(Long id, Map<String, String> body) {
        FollowUp followUp = followUpMapper.selectById(id);
        if (followUp == null) {
            throw new BusinessException(404, "回访记录不存在");
        }

        String status = body.get("status");
        if (status != null) {
            followUp.setStatus(status);
        }
        followUp.setUpdatedAt(LocalDateTime.now());
        followUpMapper.updateById(followUp);

        return followUpMapper.selectById(id);
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
