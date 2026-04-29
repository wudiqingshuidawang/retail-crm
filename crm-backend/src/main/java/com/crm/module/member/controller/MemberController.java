package com.crm.module.member.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String cardLevel) {
        var mp = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.crm.module.member.entity.Member>(page, size);
        var result = memberService.pageQuery(mp, customerName, cardLevel);
        return Result.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, Long> body) {
        var member = memberService.register(body.get("customerId"));
        return Result.ok(member);
    }

    @PutMapping("/{id}/points")
    public Result<?> updatePoints(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        memberService.updatePoints(id,
                (String) body.get("type"),
                Integer.valueOf(body.get("points").toString()),
                (String) body.get("reason"));
        return Result.ok(null);
    }

    @GetMapping("/{id}/points")
    public Result<?> pointRecords(@PathVariable Long id) {
        return Result.ok(memberService.getPointRecords(id));
    }

    @GetMapping("/{id}/profile")
    public Result<?> profile(@PathVariable Long id) {
        return Result.ok(memberService.getProfile(id));
    }
}
