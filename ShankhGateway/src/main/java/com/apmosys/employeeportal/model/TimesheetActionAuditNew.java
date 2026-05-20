package com.apmosys.employeeportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="timesheet_action_audit")
public class TimesheetActionAuditNew {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id")
	Long auditId;
	
	@Column(name = "timesheet_id")
	Long timesheetId;
	
	@Column(name = "project_id")
	Integer projectId;
	
	@Column(name = "action_type")
	String actionType;
	
	@Column(name = "action_by")
	Long actionBy;
	
	@Column(name = "action_on")
	LocalDateTime actionOn;
}
