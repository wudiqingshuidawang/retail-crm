package com.crm.module.marketing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.marketing.entity.Campaign;
import com.crm.module.marketing.entity.Coupon;
import com.crm.module.marketing.entity.CouponRecord;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.marketing.mapper.CouponMapper;
import com.crm.module.marketing.mapper.CouponRecordMapper;
import com.crm.module.marketing.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CouponMapper couponMapper;
    private final CouponRecordMapper couponRecordMapper;
    private final CustomerMapper customerMapper;

    // ===== 活动 CRUD =====
    @GetMapping("/campaigns")
    public Result<PageResult<Campaign>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        var mp = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Campaign>(page, size);
        var result = campaignService.pageQuery(mp, name, status);
        return Result.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/campaigns/{id}")
    public Result<Campaign> detail(@PathVariable Long id) {
        return Result.ok(campaignService.getDetail(id));
    }

    @PostMapping("/campaigns")
    public Result<Campaign> create(@RequestBody Campaign campaign) {
        return Result.ok(campaignService.create(campaign));
    }

    @PutMapping("/campaigns/{id}")
    public Result<Campaign> update(@PathVariable Long id, @RequestBody Campaign campaign) {
        campaign.setId(id);
        return Result.ok(campaignService.update(campaign));
    }

    @DeleteMapping("/campaigns/{id}")
    public Result<?> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return Result.ok(null);
    }

    // ===== 活动优惠券管理 =====
    @PostMapping("/campaigns/{id}/distribute")
    public Result<Map<String, Object>> distribute(@PathVariable Long id) {
        return Result.ok(campaignService.distributeCoupons(id));
    }

    @GetMapping("/campaigns/{id}/coupon-stats")
    public Result<Map<String, Object>> couponStats(@PathVariable Long id) {
        return Result.ok(campaignService.getCouponStats(id));
    }

    @GetMapping("/campaigns/{id}/coupons")
    public Result<?> coupons(@PathVariable Long id) {
        var list = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getCampaignId, id));
        var result = list.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("discountValue", c.getDiscountValue());
            m.put("minAmount", c.getMinAmount());
            m.put("totalQty", c.getTotalQty());
            m.put("sentQty", c.getUsedQty()); // 已发数量
            Long used = couponRecordMapper.selectCount(
                    new LambdaQueryWrapper<CouponRecord>()
                            .eq(CouponRecord::getCouponId, c.getId())
                            .eq(CouponRecord::getStatus, "已使用"));
            m.put("usedQty", used); // 已核销数量
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @PostMapping("/campaigns/{id}/coupons")
    public Result<?> createCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setCampaignId(id);
        coupon.setUsedQty(0);
        couponMapper.insert(coupon);
        return Result.ok(coupon);
    }

    // ===== 全部优惠券记录 =====
    @GetMapping("/coupon-records")
    public Result<?> allCouponRecords(
            @RequestParam(required = false) String status) {
        var wrapper = new LambdaQueryWrapper<CouponRecord>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CouponRecord::getStatus, status);
        }
        wrapper.orderByDesc(CouponRecord::getCreatedAt);
        var records = couponRecordMapper.selectList(wrapper);
        var result = records.stream().map(r -> {
            Coupon c = couponMapper.selectById(r.getCouponId());
            var cust = customerMapper.selectById(r.getCustomerId());
            Map<String, Object> m = new HashMap<>();
            m.put("recordId", r.getId());
            m.put("couponId", r.getCouponId());
            m.put("customerId", r.getCustomerId());
            m.put("couponName", c != null ? c.getName() : "未知");
            m.put("customerName", cust != null ? cust.getName() : "未知");
            m.put("discountValue", c != null ? c.getDiscountValue() : null);
            m.put("minAmount", c != null ? c.getMinAmount() : null);
            m.put("status", r.getStatus());
            m.put("sendTime", r.getSendTime());
            m.put("useTime", r.getUseTime());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    // ===== 优惠券核销与客户查询 =====
    @PutMapping("/coupon-records/{recordId}/use")
    public Result<?> useCoupon(@PathVariable Long recordId) {
        CouponRecord record = couponRecordMapper.selectById(recordId);
        if (record == null) return Result.fail("优惠券记录不存在");
        if (!"未使用".equals(record.getStatus())) return Result.fail("优惠券已使用或已过期");
        record.setStatus("已使用");
        record.setUseTime(LocalDateTime.now());
        couponRecordMapper.updateById(record);
        return Result.ok(null);
    }

    @GetMapping("/customers/{customerId}/coupons")
    public Result<?> customerCoupons(@PathVariable Long customerId) {
        var records = couponRecordMapper.selectList(
                new LambdaQueryWrapper<CouponRecord>()
                        .eq(CouponRecord::getCustomerId, customerId)
                        .orderByDesc(CouponRecord::getCreatedAt));
        List<Map<String, Object>> result = records.stream().map(r -> {
            Coupon c = couponMapper.selectById(r.getCouponId());
            Map<String, Object> m = new HashMap<>();
            m.put("recordId", r.getId());
            m.put("couponName", c != null ? c.getName() : "未知");
            m.put("discountValue", c != null ? c.getDiscountValue() : null);
            m.put("minAmount", c != null ? c.getMinAmount() : null);
            m.put("status", r.getStatus());
            m.put("sendTime", r.getSendTime());
            m.put("useTime", r.getUseTime());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }
}
