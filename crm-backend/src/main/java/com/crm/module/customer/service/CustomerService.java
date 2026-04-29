package com.crm.module.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BusinessException;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.entity.CustomerLead;
import com.crm.module.customer.mapper.CustomerLeadMapper;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.security.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerLeadMapper customerLeadMapper;

    public Page<Customer> pageQuery(Page<Customer> page, String name, String phone, String level) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, 1); // not deleted

        if (StringUtils.hasText(name)) {
            wrapper.like(Customer::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Customer::getPhone, phone);
        }
        if (StringUtils.hasText(level)) {
            wrapper.eq(Customer::getLevel, level);
        }

        wrapper.orderByDesc(Customer::getCreatedAt);

        IPage<Customer> result = customerMapper.selectPage(page, wrapper);
        Page<Customer> customerPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        customerPage.setRecords(result.getRecords());
        return customerPage;
    }

    public Customer getDetail(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(404, "Customer not found");
        }

        // Include lead info
        CustomerLead lead = customerLeadMapper.selectOne(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getCustomerId, id)
        );
        if (lead != null) {
            customer.setOwnerId(lead.getEmployeeId());
        }

        return customer;
    }

    @Transactional
    public Customer save(Customer customer) {
        customer.setStatus(1);
        customerMapper.insert(customer);

        // Create lead record
        CustomerLead lead = new CustomerLead();
        lead.setCustomerId(customer.getId());
        lead.setStatus("跟进中");
        lead.setIsPool(0);
        lead.setFollowCount(0);

        // Set current user as owner
        UserDetail userDetail = getUserDetail();
        lead.setEmployeeId(userDetail.getId());

        customerLeadMapper.insert(lead);

        return customer;
    }

    public Customer update(Customer customer) {
        Customer existing = customerMapper.selectById(customer.getId());
        if (existing == null) {
            throw new BusinessException(404, "Customer not found");
        }
        customerMapper.updateById(customer);
        return customerMapper.selectById(customer.getId());
    }

    public void delete(Long id) {
        Customer existing = customerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "Customer not found");
        }
        // Soft delete
        existing.setStatus(0);
        customerMapper.updateById(existing);
    }

    public Page<Customer> poolQuery(Page<Customer> page) {
        // Query customers whose lead is in the pool (is_pool = 1)
        // We need to join with customer_lead or use a subquery
        // Using MyBatis-Plus, we'll query leads first then map to customers
        LambdaQueryWrapper<CustomerLead> leadWrapper = new LambdaQueryWrapper<>();
        leadWrapper.eq(CustomerLead::getIsPool, 1);
        leadWrapper.select(CustomerLead::getCustomerId);

        java.util.List<Long> customerIds = customerLeadMapper.selectList(leadWrapper)
                .stream()
                .map(CustomerLead::getCustomerId)
                .toList();

        if (customerIds.isEmpty()) {
            Page<Customer> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            emptyPage.setRecords(java.util.Collections.emptyList());
            return emptyPage;
        }

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Customer::getId, customerIds);
        wrapper.eq(Customer::getStatus, 1);
        wrapper.orderByDesc(Customer::getCreatedAt);

        IPage<Customer> result = customerMapper.selectPage(page, wrapper);
        Page<Customer> customerPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        customerPage.setRecords(result.getRecords());
        return customerPage;
    }

    @Transactional
    public void claim(Long customerId) {
        CustomerLead lead = customerLeadMapper.selectOne(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getCustomerId, customerId)
        );
        if (lead == null) {
            throw new BusinessException(404, "Lead record not found");
        }
        if (lead.getIsPool() != 1) {
            throw new BusinessException(400, "Customer is not in the pool");
        }

        UserDetail userDetail = getUserDetail();
        lead.setIsPool(0);
        lead.setEmployeeId(userDetail.getId());
        lead.setStatus("跟进中");
        customerLeadMapper.updateById(lead);
    }

    @Transactional
    public void releaseToPool(Long customerId) {
        CustomerLead lead = customerLeadMapper.selectOne(
                new LambdaQueryWrapper<CustomerLead>()
                        .eq(CustomerLead::getCustomerId, customerId)
        );
        if (lead == null) {
            throw new BusinessException(404, "Lead record not found");
        }

        lead.setIsPool(1);
        lead.setEmployeeId(null);
        customerLeadMapper.updateById(lead);
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
