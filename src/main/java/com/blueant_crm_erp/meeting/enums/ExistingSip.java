package com.blueant_crm_erp.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExistingSip {

    YES("Yes"),
    NO("No"),
    NOT_DISCLOSED("Not Disclosed");

    private final String displayName;

}
