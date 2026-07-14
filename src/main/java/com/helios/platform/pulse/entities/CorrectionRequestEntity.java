package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "etecc_cbt_correction_requests")
public class CorrectionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "requester_name")
    private String requesterName;

    @Column(name = "field_to_modify")
    private String fieldToModify;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;
}
