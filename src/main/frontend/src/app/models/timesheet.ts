export class Timesheet{
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

}