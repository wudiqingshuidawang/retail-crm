package com.crm.module.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.employee.entity.Performance;
import com.crm.module.employee.entity.TodoTask;
import com.crm.module.employee.mapper.PerformanceMapper;
import com.crm.module.employee.mapper.TodoTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final PerformanceMapper performanceMapper;
    private final TodoTaskMapper todoTaskMapper;

    @GetMapping("/performance")
    public Result<?> performance(@RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        var list = performanceMapper.selectList(
                new LambdaQueryWrapper<Performance>()
                        .eq(Performance::getMonth, month)
                        .orderByDesc(Performance::getSalesAmount));
        return Result.ok(list);
    }

    @GetMapping("/todos")
    public Result<PageResult<TodoTask>> todos(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        var wrapper = new LambdaQueryWrapper<TodoTask>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TodoTask::getStatus, status);
        }
        wrapper.orderByDesc(TodoTask::getCreatedAt);
        var mp = new Page<TodoTask>(page, size);
        var result = todoTaskMapper.selectPage(mp, wrapper);
        return Result.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @PutMapping("/todos/{id}/complete")
    public Result<?> completeTodo(@PathVariable Long id) {
        TodoTask task = todoTaskMapper.selectById(id);
        if (task != null) {
            task.setStatus("已完成");
            task.setUpdatedAt(LocalDateTime.now());
            todoTaskMapper.updateById(task);
        }
        return Result.ok(null);
    }
}
