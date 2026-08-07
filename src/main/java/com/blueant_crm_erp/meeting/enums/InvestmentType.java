package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvestmentType {

    SIP("SIP"),
    LUMPSUM("Lumpsum"),
    SIP_AND_LUMPSUM("SIP + Lumpsum");

    private final String displayName;

}
