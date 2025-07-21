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
	
	private String applicationCount;
	
	private Integer rowNumber;
	
	private Long employeementId;
	
	private Integer reasonId;
	
	private Short oldLeaveTypeMasterId;
	private String revokeReason;
	
	private String managerName;
	private String jobRoleName;
	private String departmentName;
	private String employeeName;
	
	private String hodName;
	private Integer hodId;
	private String hodEmail;
	List<CustomFilterDTO> queryList;
	
	private String startDate;
	private String endDate;
	
	private List<LeaveDTO> bulkLeaveApprovedList;
	private List<LeaveDTO> bulkLeaveRejectList;
	private String email;
	private String rejectReason;
	
	private Long leaveRevokeId;
	private Short leaveRevokeStatusId;
	private Long leaveRevokeStatusUpdatedBy;
	
	private String remark;
	private String managerEmail;
	private Float fromDateDayType;
	private Float toDateDayType;
	
	private Long deptId;
	private Long approverEmpId;
	private String approverName;
	private String approverEmail;
	private String name;
	private String updatedByName;
	
	
	private String dateOfJoining;
	private Long employeeLeavesMapId;
	private Float creditedBalance;
	
	private String teamName;
	private String projectName;
	private String isWeekOffsExcluded;
	
	private String compOffStatus;
	
	private Float compOffAppliedOnIshine;
	private Float balanceFromOldPortal;
	
	private Integer currentApprovalLevel;
	private Integer finalApprovalLevel;
	
	private Integer managerId;
	private String managerApprovalStatus;
	private Long level1ApproverId;
	
	private Long level2ApproverId;
	private String level2ApproverName;
	private String level2ApproverEmail;
	private String level2ApprovalStatus;
	
	private Long level3ApproverId;
	private String level3ApproverName;
	private String level3ApproverEmail;
	private String level3ApprovalStatus;
	private Long leaveEmpId;
	private String state;
	private String rejectCompOffReason;
	private Long reportingManagerId;
	private String maritalStatus;
	private String maternityType;
	private Long maternityLeaveDays;
	
	private String pipReason;
	private String pipFlag;
	private Long pipId;
	private String revReason;
	private String extendDays;
	private Long aging;
	private String extendReason;
	private Double profileCompletedPercent;
	private String employeeRole;
	
	private String isConsultant;
	private String isApprenticeship;
	private Long currentUserEmpId;
	private Long differenceInDays;
	private Long approverId;
	private String clientName;
	
	private String isApmosysProduct;
	private String employmentIdAcToET;
	
}
