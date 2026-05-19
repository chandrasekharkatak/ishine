package com.apmosys.employeeportal.model;

import lombok.Data;

import java.time.LocalDateTime;

import javax.persistence.*;

@Data
@Entity
@Table(name = "leave_rejection_detail")
public class LeaveRejectionDetail {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rejectionDetailId;

    @Column(nullable = false)
    private Long leaveId;

    @Column(nullable = false)
    private Long rejectionId;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private Long rejectedBy;

    private LocalDateTime rejectedOn;

    private Boolean isActive = true;
}
