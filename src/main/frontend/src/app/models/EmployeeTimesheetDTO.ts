import { ProjectTimesheetDTO } from "./ProjectTimesheetDTO";

export class EmployeeTimesheetDTO {

    timesheetId:any;
    empId:any;
    date:any;
    dayTypeId:any;
    leaveTypeId:any;
    status:any;
    totalWorkingMinutes:any;
    totalActivitiesMinutes:any;
    isNightShift:boolean;
    officeInTime:any;
    officeOutTime:any;
    createdBy:any;
    createdOn:any;
    updatedBy:any;
    description:any;
    updatedOn:any;
    projectTimesheets: ProjectTimesheetDTO[] = [];
}