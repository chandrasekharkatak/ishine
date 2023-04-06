import { enableAppreciation } from "./enableAppreciation";

export class User{
    empId:any;
    name:any;
    email:any;
    password:any;
    role:any
    newPassword:any;
    otp:any;
    userMapping:any;
    tabList:any;
    managerId:any;
    departmentId:any;
    isNew:any;

    departmentName:any;
    dateOfResign:any;
    employeeRole:any;

    /* For Leave Policy */
    gender:any;
    employmentstatus:any;
    dateOfJoining:any;
    timesheetLockDays:any;

    sessionString:any;

    employeementId:any;

    isUserInfoUpdated:boolean = true;
    updateFormCounter:number = 0;
    isAppreciationEnable: boolean =false;
    appreciationEventInfo:enableAppreciation;

    managerName:any;
    managerEmail:any;
    hodId:any;
	hodName:any;
    hodEmail:any;

    isTimesheetLockCheckEnable:any;
    timesheetLockUpdatedOn:any;
	timesheetBackDatedDays:any;
	compOffLockDays:any;
    leaveBackdatedLockDays:any;
    leaveFuturedatedLockDays:any;

    reportingManagerId:any;
	approvalsTo:any;
	reportingManagerName:any;
    reportingManagerEmail:any;

    revokeReporteeLeaveValidity:any;
    isAllPolicyMarkAsRead:any;
    notificationConsent:any;
    
    poPortalAllProjectApi:any;
}