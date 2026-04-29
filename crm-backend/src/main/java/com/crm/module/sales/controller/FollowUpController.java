package com.crm.module.sales.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.sales.entity.FollowUp;
import com.crm.module.sales.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> pageQuery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        Page<Map<String, Object>> pageResult = followUpService.pageQuery(
                new Page<>(page, size), type, status
        );

        PageResult<Map<String, Object>> result = PageResult.of(
                pageResult.getTotal(), pageResult.getRecords()
        );
        return Result.ok(result);
    }

    @PostMapping
    public Result<FollowUp> create(@RequestBody Map<String, Object> body) {
        FollowUp followUp = followUpService.create(body);
        return Result.ok(followUp);
    }

    @PutMapping("/{id}")
    public Result<FollowUp> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        FollowUp followUp = followUpService.update(id, body);
        return Result.ok(followUp);
    }

    @PutMapping("/{id}/status")
    public Result<FollowUp> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FollowUp followUp = followUpService.updateStatus(id, body);
        return Result.ok(followUp);
    }
}
