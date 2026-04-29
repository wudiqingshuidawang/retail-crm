package com.crm.module.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Result<PageResult<Customer>> pageQuery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String level) {

        Page<Customer> pageResult = customerService.pageQuery(
                new Page<>(page, size), name, phone, level
        );

        PageResult<Customer> result = PageResult.of(
                pageResult.getTotal(), pageResult.getRecords()
        );
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<Customer> getDetail(@PathVariable Long id) {
        Customer customer = customerService.getDetail(id);
        return Result.ok(customer);
    }

    @PostMapping
    public Result<Customer> create(@Valid @RequestBody Customer customer) {
        Customer created = customerService.save(customer);
        return Result.ok(created);
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        customer.setId(id);
        Customer updated = customerService.update(customer);
        return Result.ok(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok();
    }

    @GetMapping("/pool")
    public Result<PageResult<Customer>> poolQuery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Customer> pageResult = customerService.poolQuery(new Page<>(page, size));

        PageResult<Customer> result = PageResult.of(
                pageResult.getTotal(), pageResult.getRecords()
        );
        return Result.ok(result);
    }

    @PostMapping("/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        customerService.claim(id);
        return Result.ok();
    }
}
