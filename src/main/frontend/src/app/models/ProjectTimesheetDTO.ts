import { ActivityTimesheetDTO } from "./ActivityTimesheetDTO";

export class ProjectTimesheetDTO{
    timesheetId:any;
    projectId:any;
    poNo:any;
    poId:any;
    logInTime:any;
    logOutTime:any;
    clientApprovalStatus:any;
    status:any;
    shadowEmpId:any;
    totalClientWorkingMinutes:any;
    activities:ActivityTimesheetDTO[]=[];
}