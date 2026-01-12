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
	
    @Column(name = "po_no")
    private String poNo;
    
    @Column(name = "po_id")
    private Long poId;
    
    @Column(name="client_location_id")
    private Integer clientLocationId;
    
    @Column(name = "client_approval_status")
    private Integer clientApprovalStatus;

    @Column(name = "status")
    private Integer status;

    @Column(name = "shadow_emp_id")
    private Long shadowEmpId;
    
    @Column(name = "total_client_working_minutes")
    private Integer totalClientWorkingMinutes; 
    
    @Column(name = "created_by")
	Long createdBy;
    
	@Column(name = "created_on")
	LocalDateTime createdOn;
	
	@Column(name = "updated_by")
	Long updatedBy;
	
	@Column(name = "updated_on")
	LocalDateTime updatedOn;
	
	@Column(name="description")
	private String description;

}
