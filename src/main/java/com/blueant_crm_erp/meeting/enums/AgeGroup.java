package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {

    BELOW_25("Below 25"),
    AGE_25_35("25–35"),
    AGE_36_45("36–45"),
    AGE_46_55("46–55"),
    AGE_56_65("56–65"),
    ABOVE_65("65+"),
    NOT_DISCLOSED("Not Disclosed");

    private final String displayName;

}
