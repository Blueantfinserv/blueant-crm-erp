package com.blueant_crm_erp.target.service.impl;

import com.blueant_crm_erp.target.entity.Target;
import com.blueant_crm_erp.target.repository.TargetRepository;
import com.blueant_crm_erp.target.service.TargetService;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TargetServiceImpl implements TargetService {

    private final TargetRepository targetRepository;
    private final UserRepository userRepository;

    @Override
    public Target setMonthlyTarget(Long userId, String targetMonth, BigDecimal revenue, Integer meetings, Integer leads, Integer followups, String currentUser) {
        log.info("Setting target for user {} for month {} by {}", userId, targetMonth, currentUser);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID"));
                
        Optional<Target> existing = targetRepository.findByUserIdAndTargetMonth(userId, targetMonth);
        
        Target target = existing.orElseGet(() -> Target.builder()
                .user(user)
                .targetMonth(targetMonth)
                .build());
                
        target.setRevenueTarget(revenue);
        target.setMeetingTarget(meetings);
        target.setLeadTarget(leads);
        target.setFollowupTarget(followups);
        
        return targetRepository.save(target);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Target> getTarget(Long userId, String targetMonth) {
        return targetRepository.findByUserIdAndTargetMonth(userId, targetMonth);
    }
}
