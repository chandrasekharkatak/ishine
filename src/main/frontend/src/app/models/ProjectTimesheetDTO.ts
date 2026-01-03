import { ActivityTimesheetDTO } from "./ActivityTimesheetDTO";

export class ProjectTimesheetDTO{
    timesheetId:any;
    projectId:any;
    poNo:any;
    poId:any;
    clientInTime:any;
    clientOutTime:any;
    clientApprovalStatus:any;
    status:any;
    shadowEmpId:any;
    totalClientWorkingMinutes:any;
    activities:ActivityTimesheetDTO[]=[];
}