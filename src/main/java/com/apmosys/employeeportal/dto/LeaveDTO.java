package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LeaveDTO {

	private Long empId;
	private Long leaveId;
	private Short leaveTypeMasterId;
	private String leaveType;
	private Short noOfDays;
	private String reason;
	private String fromDate;
	private String toDate;
	private Long createdBy;
	private String createdOn;
	private String createdByName;
	private Integer managerId;
	private String status;
	private Short leaveStatusId;
	private String leaveTypeCode;
	private String paidLeave;
	private String rules;
	private String description;

}
