/** Mirrors {@code RepeatedOffenderDashboardRequest} (Java). */
export interface RepeatedOffenderDashboardRequest {
  viewerEmpId: number;
  clientDashboard: boolean;
  rangeStartMonth: number;
  rangeStartYear: number;
  rangeEndMonth: number;
  rangeEndYear: number;
  billableTypes: string[];
  employeeActive: string;
  clientSideFilter: string;
  multiPOs: string;
  defaultedThreshold: number;
  /** Last 3 Months | Last 6 Months | Custom — drives snapshot KPI SQL. */
  repeatedOffenderPeriod?: string;
  deptId?: number;
  projectView: boolean;
  /** When projectView is true — same semantics as legacy getTimesheetDashboardCountForProject. */
  projectActive?: string;
  /** TOTAL_APPLICABLE | REPEATED_OFFENDERS | ALL_MONTHS_STREAK | THRESHOLD_EXACT — snapshot list segment. */
  tableSegment?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
  /** Server-side LIKE filters (trimmed non-empty strings only). */
  filterEmploymentId?: string;
  filterEmployeeName?: string;
  filterDepartment?: string;
  filterEmploymentStatus?: string;
  filterProjectName?: string;
  filterPoName?: string;
  filterBillableType?: string;
  filterManagerName?: string;
  filterProjectMapping?: string;
  filterTeamName?: string;
  filterTeamStartDate?: string;
  filterTeamEndDate?: string;
  filterDefaultedInPeriod?: string;
}

/** Mirrors {@code RepeatedOffenderSummaryPayload} (Java). */
export interface RepeatedOffenderSummaryPayload {
  totalApplicable: number;
  repeatedOffenders: number;
  repeatedPct: string;
  allMonthsStreak: number;
  thresholdBucket: number;
  monthLabels?: string[];
}
