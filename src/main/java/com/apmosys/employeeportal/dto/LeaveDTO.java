package com.apmosys.employeeportal.dto;

import java.util.List;

import javax.persistence.Column;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;

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
	private String gender;
	private String paidLeave;
	private String rules;
	private String description;
	private Float  balance;
	private Float pendingForApproval;
	private List<EmployeeLeavesMap> employeeLeaveList;
	private Short compOffId;	
	private String compOffReasons;
	private String updateBalanceBy;
	private String message;
	private Long compOffLeaveId;
	
	private Short leavePolicyMasterId;
	private String leavePolicyName;
	private String employmentStatus;
	private String leaveApplication;
	private String increment;
	private Float incrementValue;
	private String oneTimeLeave;
	private Float oneTimeLeaveMinCount;
	private Float oneTimeLeaveCount;
	private String carryForward;
	private Integer carryForwardValue;
	private String expirationPeriod;
	private Integer expirationPeriodValue;
	private String lockingPeriod;
	private Integer lockingPeriodValue;
	private Integer lockingValue;
	private String probation;
	private Integer probationPeriod;
	
	private String financialYearStartDate;
	private String financialYearStartMonth;

	private Integer updatedBy;
	private String updatedOn;
	
	private Long leaveStatusUpdatedBy;
	private String leaveStatusUpdatedByName;
	
	private Long applicationCount;
	
	private Integer rowNumber;
	
	private Long employeementId;
	
	private Integer reasonId;
	
	private Short oldLeaveTypeMasterId;
	private String revokeReason;
}
