package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timesheet_rejection_details_new")
public class TimesheetRejectionDetailsNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rejection_detail_id")
    private Long rejectionDetailId;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "location_mapping_id", nullable = false)
    private Long locationMappingId;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "rejection_id", nullable = false)
    private Long rejectionId;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "rejected_by", nullable = false)
    private Long rejectedBy;

    @Column(name = "rejected_on")
    private LocalDateTime rejectedOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_active")
    private Boolean isActive;
}

