package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timesheet_project_entries_new", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"timesheet_id", "project_id"}))
public class TimesheetProjectEntryNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_entry_id")
    private Long projectEntryId;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "client_side_id")
    private String clientSideId;

    @Column(name = "office_in_time")
    private LocalDateTime officeInTime;

    @Column(name = "office_out_time")
    private LocalDateTime officeOutTime;

    @Column(name = "client_in_time")
    private LocalDateTime clientInTime;

    @Column(name = "client_out_time")
    private LocalDateTime clientOutTime;

    @Column(name = "total_working_minutes")
    private Integer totalWorkingMinutes;

    @Column(name = "total_client_working_minutes")
    private Integer totalClientWorkingMinutes;

    @Column(name = "client_approval_status_id")
    private Integer clientApprovalStatusId;

    @Column(name = "has_client_side_id")
    private Boolean hasClientSideId;

    @Column(name = "shadow_emp_id")
    private Long shadowEmpId;

    @Column(name = "is_shadow_timesheet")
    private Boolean isShadowTimesheet;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}

