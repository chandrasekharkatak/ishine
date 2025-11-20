import { TimesheetData } from "./timesheetData";

export interface ProjectInfoDTO {
  projectId: number | null;
  projectName: string | null;
  projectManagerName: string | null;
  poNo: string | null;
  clientName: string | null;
  monthName: string | null;
  expectedTimesheetFillCount: number | null;
  clientSideNotFilledCount: number | null;
  clientSidePendingCount: number | null;
  clientSideApprovedCount: number | null;

  // days
  timesheetData: {
    [day: string]: TimesheetData;
  };

  present: string | null;
  weekOff: string | null;
  holiday: string | null;
  leave: string | null;
  compOff: string | null;
  na: string | null;
  halfDay: string | null;
  totalNoOfDays: string | null;
  teamName: any | null;
  apmosysTimesheetFilledCount: number | null;
  employmentStatus: string | null;
  startDate: string | null;
  endDate: string | null;
  readyForInvoicing: string | null;
  projectStatus: string | null;
  projectActive: string | null;
}