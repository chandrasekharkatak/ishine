import { TimesheetDocumentDataI } from "../user-timesheet/my-timesheet/timesheet-form/types";
import { ProjectTimesheetDTO } from "./ProjectTimesheetDTO";
import { LocationEntry } from "./locationEntry";

export class EmployeeTimesheetDTO {

    timesheetId?:any;
    empId?:any;
    date?:any;
    dayTypeId?:any;
    leaveTypeId?:any;
    status?:any;
    totalWorkingMinutes?:any;
    totalActivitiesMinutes?:any;
    isNightShift?:boolean;
    workCheckIn?:any;
    workCheckOut?:any;
    createdBy?:any;
    createdOn?:any;
    updatedBy?:any;
    description?:any;
    updatedOn?:any;
    currentManagerId?: number;
    isApmosysProduct: string;
    locationSessions: LocationEntry[] = [];
    documentData: TimesheetDocumentDataI[] = [];
}