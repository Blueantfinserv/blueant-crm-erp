package com.blueant_crm_erp.target.validator;

import com.blueant_crm_erp.target.exception.InvalidTargetException;
import org.springframework.stereotype.Component;

@Component
public class TargetValidator {

    public void validateTargetMonth(String targetMonth) {
        if (targetMonth == null || !targetMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new InvalidTargetException("Target month must be in YYYY-MM format");
        }
    }
}
