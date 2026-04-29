package com.crm.module.marketing.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.module.marketing.entity.Campaign;
import com.crm.module.marketing.entity.Coupon;
import com.crm.module.marketing.mapper.CouponMapper;
import com.crm.module.marketing.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CouponMapper couponMapper;

    @GetMapping
    public Result<PageResult<Campaign>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        var mp = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Campaign>(page, size);
        var result = campaignService.pageQuery(mp, name, status);
        return Result.ok(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Campaign> detail(@PathVariable Long id) {
        return Result.ok(campaignService.getDetail(id));
    }

    @PostMapping
    public Result<Campaign> create(@RequestBody Campaign campaign) {
        return Result.ok(campaignService.create(campaign));
    }

    @PutMapping("/{id}")
    public Result<Campaign> update(@PathVariable Long id, @RequestBody Campaign campaign) {
        campaign.setId(id);
        return Result.ok(campaignService.update(campaign));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/distribute")
    public Result<Map<String, Object>> distribute(@PathVariable Long id) {
        return Result.ok(campaignService.distributeCoupons(id));
    }

    @GetMapping("/{id}/coupon-stats")
    public Result<Map<String, Object>> couponStats(@PathVariable Long id) {
        return Result.ok(campaignService.getCouponStats(id));
    }

    // Coupon sub-resource
    @GetMapping("/{id}/coupons")
    public Result<?> coupons(@PathVariable Long id) {
        var list = couponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getCampaignId, id));
        return Result.ok(list);
    }

    @PostMapping("/{id}/coupons")
    public Result<?> createCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setCampaignId(id);
        coupon.setUsedQty(0);
        couponMapper.insert(coupon);
        return Result.ok(coupon);
    }
}
