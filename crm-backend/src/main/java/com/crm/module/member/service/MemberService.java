package com.crm.module.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.exception.BusinessException;
import com.crm.module.customer.entity.Customer;
import com.crm.module.customer.mapper.CustomerMapper;
import com.crm.module.member.entity.Member;
import com.crm.module.member.entity.PointRecord;
import com.crm.module.member.mapper.MemberMapper;
import com.crm.module.member.mapper.PointRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PointRecordMapper pointRecordMapper;
    private final CustomerMapper customerMapper;

    public Page<Map<String, Object>> pageQuery(Page<Member> page, String customerName, String cardLevel) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (cardLevel != null && !cardLevel.isEmpty()) {
            wrapper.eq(Member::getCardLevel, cardLevel);
        }
        wrapper.orderByDesc(Member::getUpdatedAt);

        var result = memberMapper.selectPage(page, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("customerId", m.getCustomerId());
            map.put("cardLevel", m.getCardLevel());
            map.put("points", m.getPoints());
            map.put("registeredAt", m.getRegisteredAt());
            Customer c = customerMapper.selectById(m.getCustomerId());
            map.put("customerName", c != null ? c.getName() : "未知");
            map.put("customerPhone", c != null ? c.getPhone() : "");
            return map;
        }).collect(Collectors.toList());

        if (customerName != null && !customerName.isEmpty()) {
            records = records.stream()
                    .filter(m -> ((String) m.get("customerName")).contains(customerName))
                    .collect(Collectors.toList());
        }

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Transactional
    public Member register(Long customerId) {
        Member existing = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getCustomerId, customerId));
        if (existing != null) {
            throw new BusinessException(400, "该客户已是会员");
        }
        Member member = new Member();
        member.setCustomerId(customerId);
        member.setCardLevel("普通");
        member.setPoints(0);
        member.setRegisteredAt(LocalDateTime.now());
        memberMapper.insert(member);
        return member;
    }

    @Transactional
    public void updatePoints(Long memberId, String type, Integer points, String reason) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "会员不存在");

        if ("获取".equals(type)) {
            member.setPoints(member.getPoints() + points);
        } else {
            member.setPoints(Math.max(0, member.getPoints() - points));
        }

        // Auto level upgrade
        if (member.getPoints() >= 5000) member.setCardLevel("金卡");
        else if (member.getPoints() >= 1000) member.setCardLevel("银卡");
        else member.setCardLevel("普通");

        memberMapper.updateById(member);

        PointRecord record = new PointRecord();
        record.setMemberId(memberId);
        record.setType(type);
        record.setPoints(points);
        record.setReason(reason);
        pointRecordMapper.insert(record);
    }

    public List<PointRecord> getPointRecords(Long memberId) {
        return pointRecordMapper.selectList(
                new LambdaQueryWrapper<PointRecord>()
                        .eq(PointRecord::getMemberId, memberId)
                        .orderByDesc(PointRecord::getCreatedAt));
    }

    public Map<String, Object> getProfile(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) throw new BusinessException(404, "会员不存在");

        Customer customer = customerMapper.selectById(member.getCustomerId());
        Map<String, Object> profile = new HashMap<>();
        profile.put("member", member);
        profile.put("customerName", customer != null ? customer.getName() : "未知");
        profile.put("customerPhone", customer != null ? customer.getPhone() : "");

        // Generate simple profile tags from customer data
        List<String> tags = new ArrayList<>();
        if (customer != null) {
            if (customer.getSource() != null) tags.add(customer.getSource());
            if (customer.getLevel() != null) tags.add(customer.getLevel());
        }
        if ("金卡".equals(member.getCardLevel())) tags.add("高忠诚度");
        if (member.getPoints() > 3000) tags.add("积分大户");
        profile.put("tags", tags);

        return profile;
    }
}
