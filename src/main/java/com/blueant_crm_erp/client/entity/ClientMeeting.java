package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "client_meetings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_client_meeting_code", columnNames = "meeting_code"),
                @UniqueConstraint(name = "uk_client_meeting_sequence", columnNames = {"client_id", "meeting_sequence"})
        },
        indexes = {
                @Index(name = "idx_cl_meeting_client", columnList = "client_id"),
                @Index(name = "idx_cl_meeting_employee", columnList = "assigned_employee_id"),
                @Index(name = "idx_cl_meeting_status", columnList = "meeting_status"),
                @Index(name = "idx_cl_meeting_date", columnList = "meeting_date")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeeting extends BaseVersionEntity {

    @Column(name = "meeting_code", nullable = false, length = 30)
    private String meetingCode;

    @Column(name = "meeting_sequence", nullable = false)
    private Integer meetingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_meeting_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_meeting_employee"))
    private User assignedEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", nullable = false, length = 30)
    private MeetingMode meetingMode;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Column(name = "meeting_location", length = 255)
    private String meetingLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false, length = 30)
    private MeetingStatus meetingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_conducted", nullable = false, length = 30)
    @Builder.Default
    private MeetingConductStatus meetingConducted = MeetingConductStatus.NOT_CONDUCTED;

    @Column(name = "client_meeting_status", length = 50)
    private String clientMeetingStatus;

    @Column(name = "alone_with", length = 20)
    private String aloneWith;

    @Column(name = "person_name", length = 100)
    private String personName;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "business_generated", length = 10)
    private String businessGenerated;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "next_plan_date")
    private LocalDate nextPlanDate;

    @Column(name = "next_plan_time")
    private LocalTime nextPlanTime;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "location_accuracy")
    private Double locationAccuracy;

    @Column(name = "location_captured_at")
    private LocalDateTime locationCapturedAt;

    @Column(name = "google_maps_url", length = 512)
    private String googleMapsUrl;

    @Column(name = "verified_by_pc")
    @Builder.Default
    private Boolean verifiedByPc = Boolean.FALSE;
}
