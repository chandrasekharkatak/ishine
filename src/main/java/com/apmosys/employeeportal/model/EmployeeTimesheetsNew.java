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
@Table(name="employee_timesheets_new")
public class EmployeeTimesheetsNew {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "timesheet_id")
	Long timesheetId;
	
	@Column(name = "created_by")
	Long createdBy;
	
	@Column(name = "created_on")
	LocalDateTime createdOn;
	
	@Column(name = "updated_by")
	Long updatedBy;
	
	@Column(name = "is_night_shift")
	private Boolean isNightShift;
	 
	@Column(name = "updated_on")
	LocalDateTime updatedOn;
	
	@Column(name = "date")
	LocalDate date;
	
	@Column(name = "day_type_id")
	Integer dayTypeId;
	
	@Column(name = "emp_id")
	Long empId;
	
	@Column(name = "status")
	Integer status;
	
	@Column(name = "work_in_time")
	private LocalDateTime workCheckIn;
		
	@Column(name = "work_out_time")
	 private LocalDateTime workCheckOut;
	
	@Column(name = "total_working_minutes")
	Integer totalWorkingMinutes;
	
	@Column(name = "leave_type_master_id")
	Long leaveTypeMasterId;
	
	private String description;
}
