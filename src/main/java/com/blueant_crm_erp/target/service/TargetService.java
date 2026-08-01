package com.blueant_crm_erp.target.service;

import com.blueant_crm_erp.target.entity.Target;
import java.math.BigDecimal;
import java.util.Optional;

public interface TargetService {
    Target setMonthlyTarget(Long userId, String targetMonth, BigDecimal revenue, Integer meetings, Integer leads, Integer followups, String currentUser);
    Optional<Target> getTarget(Long userId, String targetMonth);
}
