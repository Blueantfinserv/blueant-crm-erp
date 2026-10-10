package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "client_meeting_updates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cl_meeting_update_seq", columnNames = {"client_meeting_id", "update_number"})
        },
        indexes = {
                @Index(name = "idx_cl_update_meeting", columnList = "client_meeting_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingUpdate extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_meeting_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_update_meeting"))
    private ClientMeeting clientMeeting;

    @Column(name = "update_number", nullable = false)
    private Integer updateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", nullable = false, length = 30)
    private MeetingMode meetingMode;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_conducted", nullable = false, length = 30)
    private MeetingConductStatus meetingConducted;

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
}
