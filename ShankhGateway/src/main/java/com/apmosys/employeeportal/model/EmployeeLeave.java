package com.apmosys.employeeportal.model;


import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;



@Entity
@Getter
@Setter
@ToString
public class EmployeeLeave {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long leaveId;

	private Long empId;
	private Short leaveTypeMasterId;
	private Float noOfDays;	
	private Short leaveStatusId;
	private String reason;
	private LocalDate fromDate;
	private LocalDate toDate;
	@Column(length = 1000)
	private String description;
	private String remark;

	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
	private Long leaveStatusUpdatedBy;
	//techHead
	private Long hodId;
	
	private Float fromDateDayType;
	private Float toDateDayType;
	
	private Integer currentApprovalLevel;
	private Integer finalApprovalLevel;

	private Integer managerId;
	private String managerApprovalStatus;
	
	private Long level2ApproverId;
	private String level2ApprovalStatus;
	
	private Long level3ApproverId;
	private String level3ApprovalStatus;
	 
	private String maternityType;
	private Long maternityLeaveDays;
	
	
}
