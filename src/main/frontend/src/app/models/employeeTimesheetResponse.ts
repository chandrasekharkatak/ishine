import { ProjectInfoDTO } from "./projectInfoDTO";

export interface EmployeeTimesheetResponse {
  empId: number | null;
  employeeName: string | null;
  billableType: string | null;
  department: string | null;
  employmentId: string | null;
  projectTimesheet: ProjectInfoDTO[];
}