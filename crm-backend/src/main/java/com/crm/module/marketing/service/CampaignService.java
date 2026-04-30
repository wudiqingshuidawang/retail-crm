package com.crm.module.marketing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BusinessException;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.marketing.entity.Campaign;
import com.crm.module.marketing.entity.Coupon;
import com.crm.module.marketing.entity.CouponRecord;
import com.crm.module.marketing.mapper.CampaignMapper;
import com.crm.module.marketing.mapper.CouponMapper;
import com.crm.module.marketing.mapper.CouponRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignMapper campaignMapper;
    private final CouponMapper couponMapper;
    private final CouponRecordMapper couponRecordMapper;
    private final CustomerMapper customerMapper;

    public Page<Campaign> pageQuery(Page<Campaign> page, String name, String status) {
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like(Campaign::getName, name);
        if (status != null && !status.isEmpty()) wrapper.eq(Campaign::getStatus, status);
        wrapper.orderByDesc(Campaign::getCreatedAt);
        return campaignMapper.selectPage(page, wrapper);
    }

    public Campaign getDetail(Long id) {
        Campaign c = campaignMapper.selectById(id);
        if (c == null) throw new BusinessException(404, "活动不存在");
        return c;
    }

    @Transactional
    public Campaign create(Campaign campaign) {
        campaign.setStatus("进行中");
        campaignMapper.insert(campaign);
        return campaign;
    }

    public Campaign update(Campaign campaign) {
        campaignMapper.updateById(campaign);
        return campaignMapper.selectById(campaign.getId());
    }

    @Transactional
    public void delete(Long id) {
        // Delete coupon records for all coupons under this campaign
        List<Coupon> coupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getCampaignId, id));
        for (Coupon c : coupons) {
            couponRecordMapper.delete(
                    new LambdaQueryWrapper<CouponRecord>().eq(CouponRecord::getCouponId, c.getId()));
            couponMapper.deleteById(c.getId());
        }
        campaignMapper.deleteById(id);
    }

    @Transactional
    public Map<String, Object> distributeCoupons(Long campaignId) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) throw new BusinessException(404, "活动不存在");

        // Find target customers
        List<Customer> targets;
        String targetGroup = campaign.getTargetGroup();
        if (targetGroup == null || targetGroup.isEmpty() || "全部".equals(targetGroup)) {
            targets = customerMapper.selectList(new LambdaQueryWrapper<>());
        } else {
            targets = customerMapper.selectList(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getLevel, targetGroup));
        }

        if (targets.isEmpty()) {
            return Map.of("sent", 0, "message", "无匹配的目标客户");
        }

        // Find coupons for this campaign
        List<Coupon> coupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getCampaignId, campaignId));
        if (coupons.isEmpty()) {
            throw new BusinessException(400, "该活动没有可用优惠券，请先创建");
        }

        int sent = 0;
        for (Customer c : targets) {
            for (Coupon coupon : coupons) {
                if (coupon.getUsedQty() >= coupon.getTotalQty()) continue;

                // Check if this customer already has this coupon (unduplicated)
                Long existingCount = couponRecordMapper.selectCount(
                        new LambdaQueryWrapper<CouponRecord>()
                                .eq(CouponRecord::getCouponId, coupon.getId())
                                .eq(CouponRecord::getCustomerId, c.getId()));
                if (existingCount > 0) continue;

                CouponRecord record = new CouponRecord();
                record.setCouponId(coupon.getId());
                record.setCustomerId(c.getId());
                record.setStatus("未使用");
                record.setSendTime(LocalDateTime.now());
                couponRecordMapper.insert(record);

                coupon.setUsedQty(coupon.getUsedQty() + 1);
                couponMapper.updateById(coupon);
                sent++;
            }
        }

        return Map.of("sent", sent, "targetCount", targets.size(), "message", "发放完成");
    }

    public Map<String, Object> getCouponStats(Long campaignId) {
        List<Coupon> coupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getCampaignId, campaignId));
        int totalSent = 0, totalUsed = 0;
        for (Coupon c : coupons) {
            totalSent += c.getUsedQty();
            Long used = couponRecordMapper.selectCount(
                    new LambdaQueryWrapper<CouponRecord>()
                            .eq(CouponRecord::getCouponId, c.getId())
                            .eq(CouponRecord::getStatus, "已使用"));
            totalUsed += used.intValue();
        }
        double rate = totalSent > 0 ? (double) totalUsed / totalSent * 100 : 0;
        return Map.of("totalSent", totalSent, "totalUsed", totalUsed,
                "usageRate", String.format("%.1f%%", rate));
    }
}
