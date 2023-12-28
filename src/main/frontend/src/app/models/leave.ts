export class Leave{
	checkId:any
    empId:any;
	leaveId:any;
	leaveTypeMasterId:any;
	leaveType:any;
	noOfDays:any;
	reason:any;
	fromDate:any;
	toDate:any;
	createdBy:any;
	createdOn:any;
	createdByName:any;
	status:any;
	leaveStatusId:any;
	leaveTypeCode:any;
	gender: any;
	paidLeave:any;
	rules:any;
	description:any;
	balance:any;
	pendingForApproval:any;
	employeeLeaveList:any;
	compOffId:any;	
	compOffReasons:any;
	updateBalanceBy:any;
	message:any;
	compOffLeaveId:any;

	leavePolicyMasterId: any;
	leavePolicyName: any;
	employmentStatus: any;
	leaveApplication:any;
	increment: any;
	incrementValue: any;
	oneTimeLeave: any;
	oneTimeLeaveMinCount: any;
	oneTimeLeaveCount: any;
	carryForward: any;
	carryForwardValue: any;
	expirationPeriod: any;
	expirationPeriodValue: any;
	lockingPeriod: any;
	lockingPeriodValue: any;
	lockingValue: any;
	probation: any;
	probationPeriod: any;

	financialYearStartDate:any;
	financialYearStartMonth:any;

	updatedBy:any;
	updatedOn:any;
	updatedByName: any;

	leaveAppliedFor:any;
	fromDateDayType:any;
	toDateDayType:any;

	leaveStatusUpdatedBy:any;
	leaveStatusUpdatedByName:any;

	employeementId:any;

	reasonId:any;

	revokeReason:any;

	employeeName:any;

	//for changing leavetype mapping  	
	newLeaveTypeMasterId:any;	
	oldLeaveTypeMasterId:any;

	managerName:any;
	jobRoleName:any;
	departmentName:any;
	hodName:any;

	// for graph
	startDate:any;
	endDate:any

	isSelected:boolean = false;
    bulkLeaveApprovedList:any;
    bulkLeaveRejectList:any;
	email:any;
	rejectReason:any;

	remark:any;

	leaveBalance:any = 0;
	approvedApplicationsCount:any = 0;
	pendingApplicationsCount:any = 0;
	rejectedApplicationsCount:any = 0;

	deptId:any;
	approverName:any;
	approverEmail:any;

	name:any;
	checkDate:any;
	managerEmail:any;
	
	isWeekOffsExcluded:any;

	hodId:any;
	hodEmail:any;

	currentApprovalLevel:any;
	finalApprovalLevel:any;
	
	managerId:any;
	managerApprovalStatus:any;
	
	level2ApproverId:any;
	level2ApproverName:any;
	level2ApproverEmail:any;
	level2ApprovalStatus:any;
	
	level3ApproverId:any;
	level3ApproverName:any;
	level3ApproverEmail:any;
	level3ApprovalStatus:any;
	leaveEmpId:any;
	seniorManagerId:any;
}