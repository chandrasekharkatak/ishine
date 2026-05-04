package com.apmosys.employeeportal.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "leave_rejection_master")
public class LeaveRejectionMaster {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rejectionReasonId;

    @Column(nullable = false, length = 255, unique = true)
    private String reason;

    private Boolean isActive = true;
}
