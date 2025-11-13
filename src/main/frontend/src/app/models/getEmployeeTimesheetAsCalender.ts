import { TimesheetData } from "./timesheetData";

export interface GetEmployeeTimesheetAsCalender {
  empId: any;
  clientSideId: any;
  startDate: any;
  teamName: any;
  teamId: any;
  employeeName: any;
  spoc: any;
  billableType: any;
  employeeRole: any;
  department: any;
  projectId: any;
  projectName: any;
  projectManagerName: any;
  poNo: any;
  clientName: any;
  reportingManagerId: any;
  monthName: any;
  expectedTimesheetFillCount: any;
  apmosysTimesheetFilledCount: any;
  clientSideNotFilledCount: any;
  clientSidePendingCount: any;
  clientSideApprovedCount: any;
  timesheetData: {
    [key: string]: TimesheetData; 
  };
  employmentStatus: any;
	endDate: any;
	readyForInvoicing: any;
  projectstatus: any;
  active : any;
}