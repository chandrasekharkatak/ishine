package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_timesheet_status_new")
public class ProjectTimesheetStatusNew {

	@EmbeddedId
    private ProjectTimesheetStatusId id;

    @Column(name = "po_no", nullable = false)
    private String poNo;

    @Column(name = "client_in_time")
    private LocalDateTime clientInTime;

    @Column(name = "client_out_time")
    private LocalDateTime clientOutTime;

    @Column(name = "is_night_shift")
    private Boolean isNightShift;

    @Column(name = "client_approval_status")
    private Integer clientApprovalStatus;

    @Column(name = "status")
    private Integer status;

    @Column(name = "shadow_emp_id")
    private Long shadowEmpId;

    @Column(name = "total_client_working_minutes")
    private Integer totalClientWorkingMinutes;
}
