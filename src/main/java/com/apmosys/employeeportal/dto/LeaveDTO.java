package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LeaveDTO {

	private Long empId;
	private Long leaveId;
	private Short leaveTypeMasterId;
	private String leaveType;
	private Float noOfDays;
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
	private Float  balance;
	private Float pendingForApproval;
	private List<EmployeeLeavesMap> employeeLeaveList;

}
