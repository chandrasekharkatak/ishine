import { Timesheet } from "./timesheet";

export class Biomax {
    attendanceDateStr:any;
	logDate:any
    employeeCode:any
    employeeName:any
    totalDuration:any
    shiftName:any
    beginTime:any
    endTime:any
    status:any
    punchRecords:any
    earlyBy:any
    lateBy:any
    duration:any
    inTime:any
    outTime:any
    shiftDuration:any
    teamMemberName:any;
    timesheetId:any;
    timesheetdto:Timesheet[]=[];
}
export class biomaxFilter{
    empId:any[]=[];
    startDate:any;
    endDate:any;
    viewtype:any;
    
}