import { TimesheetDoc } from "./timesheetDoc";

export class Timesheet{
  [x: string]: any;
    checkId:any
    timesheetId:any;
    date:any;
    dayType:any;
    description:any;
    allTimesheetActivities:any;
    updatedTimesheetActivities:any;

    createdOn: any;	
	createdBy: any;
    createdByName : any;	
	updatedOn: any;	
	updatedBy: any;
    currentManagerId: any;


    projectId : any;
	projectName : any;
	projectManagerId : any;
	managerId : any;
	empId : any;
	startDate : any;
	endDate : any;
    status:any;

    applicationCount:any;
    employeementId : any;
	weekDayName : any;
	totalWorkingHours : any;
    totalWorkingHoursPercentage : any;

    isConsultant: any;


    // how many hour employee worked
    totalTime: any;

    timesheetStatusUpdatedBy:any;
    timesheetStatusUpdatedByName:any;
    employeeName:any;
    rejectReason:any;
    email:any;

    // for client location & Client 
    clientId : any;
	clientName : any;
    clientLocationId : any;
	clientLocation : any;

    timesheetAppliedFor:any;
    isSelected:boolean = false;
    bulkApprovedList:any;
    bulkRejectList:any;
    queryList:any[] = [];
    teamId:any;
    teamName:any;

    officeInTime:any;
    officeOutTime:any;
    totalWorkingOfficeHours:any;

    isNightShift:any;
    managerEmail: any;
    managerName: any;

    leaveType:any;

    isCron:any; //<-- for allEmployee DSR report cronJob
	year:any;
	month:any;

    displayTeam:any;
    inactiveTimesheetActivities:any;


    // 360 time sheet 
    name: any;
    activity: any;
    project: any;
    inTime:any;
    outTime: any;
    appliedOn: any;
    selected : any;
    timeSheet: any;
    nightShift: any;
    remarks: any;
    currentUser:any;

    clientSideId:any;
    clientInTime:any;
    clientOutTime:any;
    isShadowTimesheet:any;
    escalationFlag:any;
    currentEscalationLevel:any;
    totalClientWorkingHours:any;
    clientApprovalStatus:any=null;
    employmentId:any;
    hasClientSideId:Boolean=false;
    shadowEmpId:any;
    selectedFile:any;
    documentData:TimesheetDoc[];
    docId:any;
    rejectionId: any;
    hodId:any;
    rmId:any;
}