export class TimesheetDashboardCount {
  summary = {
    totalApplicableCount: 0,
    approvedCount: 0,
    defaulterCount: 0,
    clientSidePendingCount: 0,
    totaldefaulterCount: 0
  };

  departmentWise: Record<string, {
    total: number;
    departments: {
      deptCode: string;
      deptName: string;
      count: number;
    }[];
  }> = {};

    totalApplicableCount:any;
    approvedCount:any;
    defaulterCount:any;
    clientSidePendingCount:any;
    totaldefaulterCount:any;
}
