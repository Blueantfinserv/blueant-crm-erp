package com.blueant_crm_erp.meeting.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSearchRequest {

    /**
     * Search keyword.
     *
     * Search By:
     * - Meeting Code
     * - Lead ID
     * - Client Name
     * - Mobile Number
     * - Employee Code
     */
    @Size(max = 100, message = "Search keyword cannot exceed 100 characters.")
    private String keyword;

}