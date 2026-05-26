import { Component, OnDestroy, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import * as Highcharts from 'highcharts';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { displayApprovalLevelLabel } from '../rmb-approval-levels.helper';

type DashTab =
  | 'overview'
  | 'lifecycle'
  | 'approval'
  | 'rejection'
  | 'employee'
  | 'alerts'
  | 'clientTrend'
  | 'projectTrend'
  | 'deptTrend'
  | 'travelDesk';

/** Overview grid cell: optional semantic classes for main value and subtitle */
interface OverviewKpiCell {
  l: string;
  v: string;
  s: string;
  vc?: string;
  sc?: string;
}

@Component({
  standalone: false,
  selector: 'app-reimbursement-dashboard',
  templateUrl: './reimbursement-dashboard.component.html',
  styleUrls: ['./reimbursement-dashboard.component.css']
})
export class ReimbursementDashboardComponent implements OnInit, OnDestroy {
  currentUser: any;
  reimbursementDashboard: any = null;
  travelDeskDashboard: any = null;
  filterOptions: any = null;
  loadError: string | null = null;
  travelDeskLoadError: string | null = null;
  loading = false;
  lastLoadedAt: Date | null = null;

  activeTab: DashTab = 'overview';
  expandedTicketId: number | null = null;
  lifecyclePage = 0;
  readonly lifecyclePageSize = 8;

  readonly funnelSteps = [
    { k: 'Submitted', f: 'submitted' },
    { k: 'Past HOD (ticket left HOD queue)', f: 'l1Reviewed' },
    { k: 'Past HR', f: 'l2Reviewed' },
    { k: 'Finance engaged', f: 'l3Reviewed' },
    { k: 'Paid / closed', f: 'paidClosed' }
  ];

  /** Funnel for tickets matched to reimbursement approval matrix (workflow uses PENDING_LEVEL + finance). */
  readonly matrixFunnelSteps = [
    { k: 'Submitted (matrix-matched tickets)', f: 'submitted' },
    { k: 'Past configured approval levels', f: 'pastMatrixQueue' },
    { k: 'Finance engaged', f: 'financeEngaged' },
    { k: 'Paid / closed', f: 'paidClosed' }
  ];

  /** Preset Indian FY (Apr–Mar); dates sent as yyyy-MM-dd */
  readonly fyPresets = [
    { label: 'All dates', from: '', to: '' },
    { label: 'FY 2026–27', from: '2026-04-01', to: '2027-03-31' },
    { label: 'FY 2025–26', from: '2025-04-01', to: '2026-03-31' },
    { label: 'FY 2024–25', from: '2024-04-01', to: '2025-03-31' }
  ];
  fySelection = 'All dates';

  filter = {
    fromDate: '' as string,
    toDate: '' as string,
    department: '',
    employeeEmpId: '' as string | number,
    projectId: '' as string | number,
    clientId: '' as string | number,
    ticketStatus: '',
    workflowStage: '',
    expenditureType: ''
  };

  private charts: Highcharts.Chart[] = [];

  /** Top-N limit for client / project / department / employee bar charts on insight tabs. */
  private readonly topInsightBarLimit = 10;

  /** Main 4-metric chart on client / project / department tabs (grouped columns, not full master line). */
  private readonly insightFourSeriesTopLimit = 12;

  /** Users with Approve/Dashboard reimbursement access see org-wide metrics, not approval-chain-only. */
  private dashboardFullScope = false;

  constructor(
    private authenticationService: AuthenticationService,
    private reimbursementService: ReimbursementService,
    private travelDeskService: TravelDeskService
  ) {
    this.authenticationService.currentUser.subscribe((x) => {
      this.currentUser = x;
      this.dashboardFullScope = this.resolveDashboardFullScope(x);
    });
  }

  ngOnInit(): void {
    const u = this.authenticationService.currentUserValue;
    if (u) {
      this.currentUser = u;
      this.dashboardFullScope = this.resolveDashboardFullScope(u);
    }
    void this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  setTab(tab: DashTab): void {
    this.activeTab = tab;
    setTimeout(() => this.renderCharts(), 0);
  }

  onFyChange(): void {
    const preset = this.fyPresets.find((p) => p.label === this.fySelection);
    if (preset) {
      this.filter.fromDate = preset.from;
      this.filter.toDate = preset.to;
    }
    void this.loadDashboard();
  }

  /** Options for department search-select (master department names). */
  get departmentSelectOptions(): { name: string }[] {
    return (this.filterOptions?.departments || []).map((name: string) => ({ name }));
  }

  /** Options for claim-type search-select (expenditure_type master). */
  get expenditureTypeSelectOptions(): { expenditureTypeName: string }[] {
    return (this.filterOptions?.expenditureTypes || []).map((n: string) => ({
      expenditureTypeName: n
    }));
  }

  /** All projects when no client selected; otherwise projects whose {@code clientId} matches the filter. */
  get projectSelectOptions(): { projectId: number; projectName: string; clientId?: number | null }[] {
    const all = this.filterOptions?.projects || [];
    const cid = this.filter.clientId;
    if (cid === '' || cid == null) {
      return all;
    }
    const clientNum = Number(cid);
    return all.filter(
      (p: { clientId?: number | null }) => p.clientId != null && Number(p.clientId) === clientNum
    );
  }

  get projectFilterPlaceholder(): string {
    if (this.filter.clientId !== '' && this.filter.clientId != null) {
      return 'Search and select project for client';
    }
    return 'Search and select project';
  }

  onMasterFilterChange(): void {
    void this.loadDashboard();
  }

  onClientFilterChange(): void {
    this.syncProjectFilterToSelectedClient();
    void this.loadDashboard();
  }

  /** Clear project when it does not belong to the newly selected client. */
  private syncProjectFilterToSelectedClient(): void {
    if (this.filter.projectId === '' || this.filter.projectId == null) {
      return;
    }
    if (this.filter.clientId === '' || this.filter.clientId == null) {
      return;
    }
    const cid = Number(this.filter.clientId);
    const pid = Number(this.filter.projectId);
    const stillValid = (this.filterOptions?.projects || []).some(
      (p: { projectId?: number; clientId?: number | null }) =>
        Number(p.projectId) === pid && p.clientId != null && Number(p.clientId) === cid
    );
    if (!stillValid) {
      this.filter.projectId = '';
    }
  }

  clearDepartmentFilter(): void {
    this.filter.department = '';
    void this.loadDashboard();
  }

  clearEmployeeFilter(): void {
    this.filter.employeeEmpId = '';
    void this.loadDashboard();
  }

  clearProjectFilter(): void {
    this.filter.projectId = '';
    void this.loadDashboard();
  }

  clearClientFilter(): void {
    this.filter.clientId = '';
    void this.loadDashboard();
  }

  clearClaimTypeFilter(): void {
    this.filter.expenditureType = '';
    void this.loadDashboard();
  }

  resetFilters(): void {
    this.fySelection = 'All dates';
    this.filter = {
      fromDate: '',
      toDate: '',
      department: '',
      employeeEmpId: '',
      projectId: '',
      clientId: '',
      ticketStatus: '',
      workflowStage: '',
      expenditureType: ''
    };
    void this.loadDashboard();
  }

  toggleRow(ticketId: number): void {
    this.expandedTicketId = this.expandedTicketId === ticketId ? null : ticketId;
  }

  isRowOpen(ticketId: number): boolean {
    return this.expandedTicketId === ticketId;
  }

  lifecycleTotalPages(): number {
    const n = (this.reimbursementDashboard?.ticketRows || []).length;
    return Math.max(1, Math.ceil(n / this.lifecyclePageSize));
  }

  lifecycleSlice(): any[] {
    const rows = this.reimbursementDashboard?.ticketRows || [];
    const start = this.lifecyclePage * this.lifecyclePageSize;
    return rows.slice(start, start + this.lifecyclePageSize);
  }

  prevLifecyclePage(): void {
    this.lifecyclePage = Math.max(0, this.lifecyclePage - 1);
  }

  nextLifecyclePage(): void {
    this.lifecyclePage = Math.min(this.lifecycleTotalPages() - 1, this.lifecyclePage + 1);
  }

  /** Indian rupee display: show up to 2 decimals when the value has a fractional part. */
  formatRupee(v: any): string {
    if (v == null || v === '') {
      return '0';
    }
    const raw = typeof v === 'string' ? v.replace(/,/g, '').trim() : v;
    const n = Number(raw);
    if (Number.isNaN(n)) {
      return String(v);
    }
    const hasDecimals =
      (typeof raw === 'string' && raw.includes('.')) ||
      Math.abs(n - Math.round(n)) > 1e-9;
    return new Intl.NumberFormat('en-IN', {
      minimumFractionDigits: hasDecimals ? 2 : 0,
      maximumFractionDigits: 2
    }).format(n);
  }

  /** Compact axis label (k / L) while preserving decimals when present. */
  private rupeeAxisLabel(value: number): string {
    const v = Number(value);
    if (!Number.isFinite(v)) {
      return '₹0';
    }
    const abs = Math.abs(v);
    if (abs >= 100000) {
      return '₹' + this.formatRupee(v / 100000) + 'L';
    }
    if (abs >= 1000) {
      return '₹' + this.formatRupee(v / 1000) + 'k';
    }
    return '₹' + this.formatRupee(v);
  }

  formatNumber(v: any): string {
    if (v == null || v === '') {
      return '0';
    }
    const n = typeof v === 'string' ? Number(v) : Number(v);
    if (Number.isNaN(n)) {
      return String(v);
    }
    return new Intl.NumberFormat('en-IN').format(n);
  }

  pct(part: any, whole: any): string {
    const p = Number(part);
    const w = Number(whole);
    if (!w || w <= 0 || Number.isNaN(w)) {
      return '0';
    }
    if (Number.isNaN(p)) {
      return '0';
    }
    return ((100 * p) / w).toFixed(1);
  }

  travelDeskKpiRow1(): OverviewKpiCell[] {
    const d = this.travelDeskDashboard;
    if (!d) {
      return [];
    }
    return [
      { l: 'Travel tickets', v: this.formatNumber(d.ticketCount), s: 'Scoped to current filters', vc: 'rmb-tone-neutral' },
      { l: 'Requests', v: this.formatNumber(d.requestCount), s: 'Travel + hotel request lines', vc: 'rmb-tone-neutral' },
      { l: 'Booked requests', v: this.formatNumber(d.bookedRequestCount), s: 'Lines with booked amount', vc: 'rmb-tone-success' },
      { l: 'Total spend', v: '₹' + this.formatRupee(d.totalSpend), s: 'Sum of booked travel amounts', vc: 'rmb-tone-primary' }
    ];
  }

  travelDeskKpiRow2(): OverviewKpiCell[] {
    const d = this.travelDeskDashboard;
    if (!d) {
      return [];
    }
    return [
      {
        l: 'Avg spend / booked request',
        v: '₹' + this.formatRupee(d.averageSpendPerBookedRequest),
        s: 'Average booking amount',
        vc: 'rmb-tone-info'
      },
      { l: 'Projects with spend', v: this.formatNumber(d.projectsWithSpend), s: 'Projects having booked travel cost', vc: 'rmb-tone-neutral' },
      { l: 'Clients with spend', v: this.formatNumber(d.clientsWithSpend), s: 'Clients mapped to booked travel', vc: 'rmb-tone-neutral' },
      {
        l: 'Pending travel admin',
        v: this.formatNumber(d.pendingAdminTickets),
        s: 'Tickets waiting for booking',
        vc: Number(d.pendingAdminTickets || 0) > 0 ? 'rmb-tone-warning' : 'rmb-tone-zero'
      }
    ];
  }

  travelWorkflowStageRows(): { stage: string; count: number }[] {
    const rows = this.travelDeskDashboard?.workflowStageBreakdown || {};
    return Object.keys(rows).map((stage) => ({ stage, count: Number(rows[stage] || 0) }));
  }

  travelProjectSpendRows(limit?: number): any[] {
    const rows = Array.isArray(this.travelDeskDashboard?.projectSpendRows)
      ? this.travelDeskDashboard.projectSpendRows
      : [];
    return limit != null ? rows.slice(0, limit) : rows;
  }

  travelClientSpendRows(limit?: number): any[] {
    const rows = Array.isArray(this.travelDeskDashboard?.clientSpendRows)
      ? this.travelDeskDashboard.clientSpendRows
      : [];
    return limit != null ? rows.slice(0, limit) : rows;
  }

  overviewKpiRow1(): OverviewKpiCell[] {
    const d = this.reimbursementDashboard;
    if (!d) {
      return [];
    }
    const paid = Number(d.totalPaidAmount || 0);
    const req = Number(d.totalSubmittedAmount || 0);
    return [
      { l: 'Total tickets', v: this.formatNumber(d.ticketCount), s: '', vc: 'rmb-tone-neutral' },
      { l: 'Total claims', v: this.formatNumber(d.claimCount), s: '', vc: 'rmb-tone-neutral' },
      {
        l: 'Total requested',
        v: '₹' + this.formatRupee(d.totalSubmittedAmount),
        s: 'Scoped to filters',
        vc: 'rmb-tone-primary',
        sc: 'rmb-sub-muted'
      },
      {
        l: 'Total paid',
        v: '₹' + this.formatRupee(d.totalPaidAmount),
        s: this.pct(d.totalPaidAmount, d.totalSubmittedAmount) + '% of requested',
        vc: 'rmb-tone-success',
        sc: paid > 0 ? 'rmb-sub-success' : 'rmb-sub-muted'
      }
    ];
  }

  overviewKpiRow2(): OverviewKpiCell[] {
    const d = this.reimbursementDashboard;
    if (!d) {
      return [];
    }
    const pipeline = Number(d.pipelinePendingAmount || 0);
    const rejAmt = Number(d.totalRejectedClaimsAmount || 0);
    const rejLines = Number(d.rejectedClaimLines || 0);
    const claims = Number(d.claimCount || 0);
    return [
      {
        l: 'Pipeline pending (₹)',
        v: '₹' + this.formatRupee(d.pipelinePendingAmount),
        s: 'Matrix level / HOD / HR / Finance pending claims',
        vc: pipeline > 0 ? 'rmb-tone-warning' : 'rmb-tone-zero',
        sc: pipeline > 0 ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Rejected amount',
        v: '₹' + this.formatRupee(d.totalRejectedClaimsAmount),
        s: '',
        vc: rejAmt > 0 ? 'rmb-tone-danger' : 'rmb-tone-zero',
        sc: rejAmt > 0 ? 'rmb-sub-danger' : 'rmb-sub-muted'
      },
      {
        l: 'Approved-for-payment (₹)',
        v: '₹' + this.formatRupee(d.totalApprovedAmount),
        s: 'Finance queue + paid',
        vc: 'rmb-tone-info',
        sc: 'rmb-sub-info'
      },
      {
        l: 'Rejected claims',
        v: this.formatNumber(d.rejectedClaimLines),
        s: this.pct(d.rejectedClaimLines, d.claimCount) + '% of claims',
        vc: rejLines > 0 ? 'rmb-tone-danger' : 'rmb-tone-zero',
        sc: claims > 0 && rejLines > 0 ? 'rmb-sub-danger' : 'rmb-sub-muted'
      }
    ];
  }

  overviewKpiRow3(): OverviewKpiCell[] {
    const d = this.reimbursementDashboard;
    if (!d) {
      return [];
    }
    const partial = Number(d.partialApprovalTickets || 0);
    const matrix = Number(d.matrixTicketCount || 0);
    const legacy = Number(d.legacyTicketCount || 0);
    const pendMatrix = Number(d.pendingApprovalTickets || 0);
    const hod = Number(d.pendingHodTickets || 0);
    const hr = Number(d.pendingHrTickets || 0);
    const fin = Number(d.pendingFinanceTickets || 0);
    const pendAny = pendMatrix + hod + hr + fin > 0;
    const matrixCount = Number(d.activeApprovalMatrixCount ?? (d.activeApprovalMatrices || []).length);
    return [
      {
        l: 'Tickets (matrix / legacy)',
        v: this.formatNumber(matrix) + ' / ' + this.formatNumber(legacy),
        s: 'Configured matrix path vs default HOD→HR→Finance',
        vc: matrix > 0 ? 'rmb-tone-primary' : 'rmb-tone-neutral',
        sc: 'rmb-sub-muted'
      },
      {
        l: 'Pending matrix / HOD / HR / Fin',
        v:
          this.formatNumber(pendMatrix) +
          ' / ' +
          this.formatNumber(hod) +
          ' / ' +
          this.formatNumber(hr) +
          ' / ' +
          this.formatNumber(fin),
        s: 'Tickets waiting at each queue type',
        vc: pendAny ? 'rmb-tone-warning' : 'rmb-tone-zero',
        sc: pendAny ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Partial-approval tickets',
        v: this.formatNumber(d.partialApprovalTickets),
        s: 'Mixed claim outcomes on one ticket',
        vc: partial > 0 ? 'rmb-tone-warning' : 'rmb-tone-neutral',
        sc: partial > 0 ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Active approval matrices',
        v: this.formatNumber(matrixCount),
        s: this.overviewMatrixConfigHint(),
        vc: matrixCount > 0 ? 'rmb-tone-info' : 'rmb-tone-zero',
        sc: 'rmb-sub-info'
      }
    ];
  }

  overviewKpiRow4(): OverviewKpiCell[] {
    const d = this.reimbursementDashboard;
    if (!d) {
      return [];
    }
    const aging = Number(d.agingPendingTicketsOver7Days || 0);
    const finLines = Number(d.approvedClaimLines || 0);
    const levelRejected = this.claimStatusLineCount('LEVEL_REJECTED');
    const pendingApproval = this.claimStatusLineCount('PENDING_APPROVAL');
    return [
      {
        l: 'Claims pending approval',
        v: this.formatNumber(pendingApproval),
        s: 'At matrix or legacy approval levels',
        vc: pendingApproval > 0 ? 'rmb-tone-warning' : 'rmb-tone-zero',
        sc: pendingApproval > 0 ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Matrix-level rejections',
        v: this.formatNumber(levelRejected),
        s: 'Rejected at configured approval level',
        vc: levelRejected > 0 ? 'rmb-tone-danger' : 'rmb-tone-zero',
        sc: levelRejected > 0 ? 'rmb-sub-danger' : 'rmb-sub-muted'
      },
      {
        l: 'Aging pending >7d',
        v: this.formatNumber(d.agingPendingTicketsOver7Days),
        s: 'Tickets in any approval queue > 7 days',
        vc: aging > 0 ? 'rmb-tone-danger' : 'rmb-tone-neutral',
        sc: aging > 0 ? 'rmb-sub-danger' : 'rmb-sub-muted'
      },
      {
        l: 'Finance-ready claims',
        v: this.formatNumber(d.approvedClaimLines),
        s: 'Approved for finance / paid path',
        vc: finLines > 0 ? 'rmb-tone-success' : 'rmb-tone-zero',
        sc: finLines > 0 ? 'rmb-sub-success' : 'rmb-sub-muted'
      }
    ];
  }

  claimStatusLineCount(status: string): number {
    const raw = this.reimbursementDashboard?.claimStatusLineCounts;
    if (!raw || typeof raw !== 'object') {
      return 0;
    }
    return Number(raw[status] || 0);
  }

  overviewActiveMatrices(): { matrixId?: number; matrixName?: string; levelCount?: number; approvalFlowSummary?: string }[] {
    return this.reimbursementDashboard?.activeApprovalMatrices || [];
  }

  overviewMatrixConfigHint(): string {
    const mats = this.overviewActiveMatrices();
    if (!mats.length) {
      return 'No active matrix in configuration';
    }
    const first = mats[0];
    const name = first?.matrixName || 'Matrix';
    const levels = first?.levelCount ?? 0;
    return levels > 0 ? `${name} · ${levels} level(s) + Finance` : name;
  }

  /** Flow summary from API with legacy label normalization. */
  displayApprovalFlowSummary(summary: string | undefined | null): string {
    if (!summary) {
      return '';
    }
    return String(summary)
      .split(' → ')
      .map((part) => displayApprovalLevelLabel(part.trim()))
      .join(' → ');
  }

  overviewUsesMatrixWorkflow(): boolean {
    return Number(this.reimbursementDashboard?.matrixTicketCount || 0) > 0;
  }

  workflowStageLabel(stage: string): string {
    const labels: Record<string, string> = {
      PENDING_HOD: 'Pending HOD',
      PENDING_LEVEL: 'Pending matrix level',
      PENDING_HR: 'Pending HR',
      PENDING_FINANCE: 'Pending finance',
      PAID: 'Paid',
      REJECTED: 'Rejected'
    };
    return labels[stage] || stage;
  }

  funnelVal(f: any, key: string): number {
    if (!f || f[key] == null) {
      return 0;
    }
    return Number(f[key]);
  }

  /** Exclusive ticket buckets between cumulative funnel milestones (valid pie slices). */
  funnelExclusivePieData(
    funnel: Record<string, unknown> | null | undefined,
    steps: readonly { k: string; f: string }[]
  ): { name: string; y: number }[] {
    if (!funnel || !steps.length) {
      return [];
    }
    const counts = steps.map((s) => this.funnelVal(funnel, s.f));
    const submitted = this.funnelVal(funnel, 'submitted');
    if (submitted <= 0) {
      return [];
    }
    const slices: { name: string; y: number }[] = [];
    for (let i = 0; i < steps.length; i++) {
      const cur = counts[i];
      const exclusive = i === steps.length - 1 ? cur : Math.max(0, cur - counts[i + 1]);
      if (exclusive > 0) {
        slices.push({ name: steps[i].k, y: exclusive });
      }
    }
    return slices;
  }

  funnelHasPieSlices(
    funnel: Record<string, unknown> | null | undefined,
    steps: readonly { k: string; f: string }[]
  ): boolean {
    return this.funnelExclusivePieData(funnel, steps).length > 0;
  }

  funnelPct(value: number, submitted: number): string {
    return this.pct(value, submitted || 1);
  }

  /** Pending pipeline ₹ split by days since ticket submit (claim pipeline statuses). */
  pendingAgingList(): { label: string; amount: number; pctLabel: string }[] {
    const d = this.reimbursementDashboard;
    const aging = d?.pendingAmountAging as Record<string, number> | undefined;
    const pipeline = Number(d?.pipelinePendingAmount ?? 0);
    const order: { key: string; label: string }[] = [
      { key: '0-7', label: '0–7 days' },
      { key: '8-15', label: '8–15 days' },
      { key: '16-30', label: '16–30 days' },
      { key: '30+', label: '> 30 days' }
    ];
    if (!aging) {
      return [];
    }
    return order.map(({ key, label }) => {
      const amount = Number(aging[key] ?? 0);
      const pctLabel = pipeline > 0 ? this.pct(amount, pipeline) : '0';
      return { label, amount, pctLabel };
    });
  }

  insightDimensionCount(kind: 'client' | 'project' | 'department'): number {
    const d = this.reimbursementDashboard;
    const pack =
      kind === 'client' ? d?.barTotalsByClient : kind === 'project' ? d?.barTotalsByProject : d?.barTotalsByDepartment;
    return (pack?.rows as unknown[] | undefined)?.length ?? 0;
  }

  monthlyTrendPairs(): { key: string; requested: number; paid: number }[] {
    const mt = this.reimbursementDashboard?.monthlyTrend;
    if (!mt || typeof mt !== 'object') {
      return [];
    }
    return Object.keys(mt)
      .sort()
      .map((k) => ({
        key: k,
        requested: Number(mt[k]?.requested || 0),
        paid: Number(mt[k]?.paid || 0)
      }));
  }

  monthLabel(ym: string): string {
    if (!ym || ym.length < 7) {
      return ym;
    }
    const [y, m] = ym.split('-').map((x) => parseInt(x, 10));
    const d = new Date(y, (m || 1) - 1, 1);
    return d.toLocaleString('en-IN', { month: 'short', year: 'numeric' });
  }

  rejectionReasonList(): { reason: string; count: number; pct: number }[] {
    const raw = this.reimbursementDashboard?.rejectionReasonBuckets;
    if (!raw || typeof raw !== 'object') {
      return [];
    }
    const entries = Object.entries(raw).map(([reason, count]) => ({
      reason,
      count: Number(count || 0)
    }));
    const total = entries.reduce((s, e) => s + e.count, 0);
    return entries
      .sort((a, b) => b.count - a.count)
      .map((e) => ({ ...e, pct: total ? Math.round((1000 * e.count) / total) / 10 : 0 }));
  }

  /** Rejected claim lines grouped by HOD / matrix level / HR / Finance (matches backend `rejectionByChannel`). */
  rejectionByChannelList(): { channel: string; count: number; pct: number }[] {
    const raw = this.reimbursementDashboard?.rejectionByChannel;
    if (!raw || typeof raw !== 'object') {
      return [];
    }
    const order = ['HOD', 'Matrix (approval level)', 'HR', 'Finance'];
    const keys = [...order, ...Object.keys(raw).filter((k) => !order.includes(k))];
    const entries = keys.map((channel) => ({ channel, count: Number(raw[channel] || 0) }));
    const total = entries.reduce((s, e) => s + e.count, 0);
    return entries.map((e) => ({
      ...e,
      pct: total ? Math.round((1000 * e.count) / total) / 10 : 0
    }));
  }

  claimStatusLineRows(): { status: string; count: number }[] {
    const raw = this.reimbursementDashboard?.claimStatusLineCounts;
    if (!raw || typeof raw !== 'object') {
      return [];
    }
    return Object.entries(raw)
      .map(([status, count]) => ({ status, count: Number(count || 0) }))
      .sort((a, b) => b.count - a.count);
  }

  workflowStageRows(): { stage: string; count: number }[] {
    const raw = this.reimbursementDashboard?.workflowStageTicketCounts;
    if (!raw || typeof raw !== 'object') {
      return [];
    }
    return Object.entries(raw)
      .map(([stage, count]) => ({ stage, count: Number(count || 0) }))
      .filter((r) => r.count > 0);
  }

  pendingMatrixLevelRows(): { label: string; count: number }[] {
    const raw = this.reimbursementDashboard?.pendingMatrixByLevel;
    if (!raw || typeof raw !== 'object') {
      return [];
    }
    return Object.entries(raw).map(([label, count]) => ({ label, count: Number(count || 0) }));
  }

  alertsCount(): number {
    return (this.reimbursementDashboard?.alerts || []).length;
  }

  async loadDashboard(): Promise<void> {
    this.loadError = null;
    this.travelDeskLoadError = null;
    this.loading = true;
    this.reimbursementDashboard = null;
    this.travelDeskDashboard = null;
    this.destroyCharts();
    if (this.currentUser) {
      this.dashboardFullScope = this.resolveDashboardFullScope(this.currentUser);
    }
    try {
      const body = this.buildFilterPayload();
      const [dashRes, travelRes] = await Promise.allSettled([
        this.reimbursementService.fetchReimbursementDashboard(body).pipe(first()).toPromise(),
        this.travelDeskService.fetchTravelDeskDashboard(body).pipe(first()).toPromise()
      ]);
      const dash: any = dashRes.status === 'fulfilled' ? dashRes.value : null;
      const travelDash: any = travelRes.status === 'fulfilled' ? travelRes.value : null;
      if (dash?.serviceStatus === 'Success') {
        this.reimbursementDashboard = dash.serviceResponse;
        this.filterOptions = dash.serviceResponse?.filterOptions || null;
        this.lastLoadedAt = new Date();
        this.lifecyclePage = 0;
      } else {
        this.loadError =
          dash?.serviceError ||
          dash?.serviceResponse ||
          (dashRes.status === 'rejected' ? dashRes.reason?.message : null) ||
          'Unable to load dashboard.';
      }
      if (travelDash?.serviceStatus === 'Success') {
        this.travelDeskDashboard = travelDash.serviceResponse;
      } else {
        this.travelDeskLoadError =
          travelDash?.serviceError ||
          travelDash?.serviceResponse ||
          (travelRes.status === 'rejected' ? travelRes.reason?.message : null) ||
          'Unable to load travel desk analytics.';
      }
      if (!this.loadError) {
        setTimeout(() => this.renderCharts(), 0);
      }
    } catch (e: any) {
      this.loadError = e?.message || 'Unable to load dashboard.';
    } finally {
      this.loading = false;
    }
  }

  /**
   * Org-wide dashboard for SuperAdmin/Admin and anyone with Approve Reimbursement / Dashboard tab access.
   */
  private resolveDashboardFullScope(user: any): boolean {
    if (!user) {
      return false;
    }
    const er = String(user.employeeRole || user.role || '').trim();
    if (/superadmin/i.test(er) || /^admin$/i.test(er) || /administrator/i.test(er)) {
      return true;
    }
    const feat = (user.userMapping || []).find((m: any) => m.featureName === 'Reimbursement');
    const subs = feat?.subFeatures || [];
    return subs.some((s: any) => {
      const key = String(s.subFeatureName || '')
        .replace(/\s+/g, '_')
        .toLowerCase();
      return (key === 'approve_reimbursement' || key === 'total_reimbursementrequest') && s.isActive;
    });
  }

  private buildFilterPayload(): any {
    const o: any = {};
    const f = this.filter;
    if (f.fromDate) {
      o.fromDate = f.fromDate;
    }
    if (f.toDate) {
      o.toDate = f.toDate;
    }
    if (f.department) {
      o.department = f.department;
    }
    if (f.ticketStatus) {
      o.ticketStatus = f.ticketStatus;
    }
    if (f.workflowStage) {
      o.workflowStage = f.workflowStage;
    }
    if (f.expenditureType) {
      o.expenditureType = f.expenditureType;
    }
    if (f.employeeEmpId !== '' && f.employeeEmpId != null) {
      o.employeeEmpId = Number(f.employeeEmpId);
    }
    if (f.projectId !== '' && f.projectId != null) {
      o.projectId = Number(f.projectId);
    }
    if (f.clientId !== '' && f.clientId != null) {
      o.clientId = Number(f.clientId);
    }
    if (this.currentUser?.empId != null) {
      o.actorEmpId = Number(this.currentUser.empId);
    }
    if (this.currentUser?.email) {
      o.actorEmail = this.currentUser.email;
    }
    if (this.currentUser?.employeeRole) {
      o.actorEmployeeRole = this.currentUser.employeeRole;
    } else if (this.currentUser?.role) {
      o.actorEmployeeRole = this.currentUser.role;
    }
    if (this.dashboardFullScope) {
      o.dashboardFullScope = true;
    }
    return o;
  }

  private destroyCharts(): void {
    this.charts.forEach((c) => {
      try {
        c.destroy();
      } catch {
        /* ignore */
      }
    });
    this.charts = [];
  }

  private renderCharts(): void {
    this.destroyCharts();
    const tab = this.activeTab;
    if (tab === 'travelDesk') {
      if (this.travelDeskDashboard) {
        this.renderTravelAnalyticsCharts(this.travelDeskDashboard);
      }
      return;
    }
    const d = this.reimbursementDashboard;
    if (!d) {
      return;
    }
    if (tab === 'overview') {
      this.renderOverviewCharts(d);
    } else if (tab === 'rejection') {
      this.renderRejectionTypeChart(d);
      this.renderRejectionChannelChart(d);
    } else if (tab === 'clientTrend') {
      this.renderInsightTabCharts(d, 'client');
    } else if (tab === 'projectTrend') {
      this.renderInsightTabCharts(d, 'project');
    } else if (tab === 'deptTrend') {
      this.renderInsightTabCharts(d, 'department');
    } else if (tab === 'approval') {
      this.renderApprovalCharts(d);
    }
  }

  private renderTravelAnalyticsCharts(d: any): void {
    if (!d) {
      return;
    }
    this.renderInsightSingleMetricLineChart(
      'rmbDashTravelSpendTrend',
      'Travel spend',
      '#1B3461',
      (d.monthlySpendPack?.categories || []) as string[],
      ((d.monthlySpendPack?.spend || []) as any[]).map((n: any) => Number(n || 0)),
      `${this.formatNumber(d.bookedRequestCount || 0)} booked request(s)`
    );
    this.renderTicketStatusDonut('rmbDashTravelTicketDonut', d.ticketStatusBreakdown || {});
    this.renderTopDimensionColumnChart(
      'rmbDashTravelProjectTop',
      { rows: d.projectSpendRows || [] },
      this.topInsightBarLimit,
      'Spend (₹)',
      'rgba(27,52,97,0.88)'
    );
    this.renderTopDimensionColumnChart(
      'rmbDashTravelClientTop',
      { rows: d.clientSpendRows || [] },
      this.topInsightBarLimit,
      'Spend (₹)',
      'rgba(2,132,199,0.88)'
    );
    this.renderTopEmployeesColumnChart(
      'rmbDashTravelTopEmp',
      d.employeeSpendRows || [],
      this.topInsightBarLimit,
      'Spend (₹)',
      'rgba(22,101,52,0.88)'
    );
  }

  private renderApprovalCharts(d: any): void {
    const legacyColors = ['#1B3461', '#0ea5e9', '#64748b', '#f59e0b', '#16a34a'];
    const matrixColors = ['#1B3461', '#0ea5e9', '#f59e0b', '#16a34a'];
    if (d.approvalFunnel) {
      this.renderFunnelPieChart('rmbDashApprovalFunnelPie', d.approvalFunnel, this.funnelSteps, legacyColors);
    }
    if (d.approvalFunnelMatrix && Number(d.matrixTicketCount || 0) > 0) {
      this.renderFunnelPieChart('rmbDashMatrixFunnelPie', d.approvalFunnelMatrix, this.matrixFunnelSteps, matrixColors);
    }
  }

  private renderFunnelPieChart(
    hostId: string,
    funnel: Record<string, unknown>,
    steps: readonly { k: string; f: string }[],
    colors: string[]
  ): void {
    const el = document.getElementById(hostId) as HTMLElement | null;
    const data = this.funnelExclusivePieData(funnel, steps);
    if (!el || !data.length) {
      return;
    }
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'pie', backgroundColor: 'transparent', height: 300 },
        title: { text: undefined },
        tooltip: {
          pointFormat: '<b>{point.y}</b> ticket(s) — {point.percentage:.1f}% of scoped tickets'
        },
        plotOptions: {
          pie: {
            innerSize: '52%',
            dataLabels: {
              enabled: true,
              distance: 12,
              format: '{point.name}<br><b>{point.y}</b> ({point.percentage:.1f}%)',
              style: { fontSize: '10px', fontWeight: 'normal', textOutline: 'none' }
            },
            borderWidth: 0,
            showInLegend: true
          }
        },
        series: [
          {
            type: 'pie',
            name: 'Tickets',
            data: data.map((p, i) => ({
              name: p.name,
              y: p.y,
              color: colors[i % colors.length]
            }))
          }
        ],
        credits: { enabled: false },
        legend: {
          enabled: data.length > 3,
          itemStyle: { fontSize: '10px' },
          layout: 'horizontal',
          align: 'center',
          verticalAlign: 'bottom'
        }
      })
    );
  }

  private renderOverviewCharts(d: any): void {
    const el = (id: string): HTMLElement | null => document.getElementById(id) as HTMLElement | null;

    const statusEl = el('rmbDashChartStatus');
    const breakdown = d.ticketStatusBreakdown || {};
    if (statusEl && Object.keys(breakdown).length) {
      this.charts.push(
        Highcharts.chart(statusEl, {
          chart: { type: 'pie', backgroundColor: 'transparent', height: 220 },
          title: { text: undefined },
          plotOptions: {
            pie: { innerSize: '62%', dataLabels: { enabled: false }, borderWidth: 0 }
          },
          series: [
            {
              type: 'pie',
              name: 'Tickets',
              data: Object.keys(breakdown).map((name) => ({
                name,
                y: Number(breakdown[name] || 0)
              }))
            }
          ],
          credits: { enabled: false },
          legend: { itemStyle: { fontSize: '10px' } }
        })
      );
    }

    const trend = this.monthlyTrendPairs();
    const trendEl = el('rmbDashChartTrend');
    if (trendEl && trend.length) {
      this.charts.push(
        Highcharts.chart(trendEl, {
          chart: { type: 'column', backgroundColor: 'transparent', height: 260 },
          title: { text: undefined },
          xAxis: {
            categories: trend.map((t) => this.monthLabel(t.key)),
            labels: { style: { fontSize: '10px' } }
          },
          yAxis: {
            min: 0,
            title: { text: undefined },
            labels: {
              style: { fontSize: '10px' },
              formatter: this.rupeeAxisShortFmt()
            }
          },
          tooltip: this.rupeeTooltipOptions(),
          plotOptions: { column: { borderRadius: 3, groupPadding: 0.08 } },
          series: [
            { type: 'column', name: 'Requested', color: 'rgba(27,52,97,0.85)', data: trend.map((t) => t.requested) },
            { type: 'column', name: 'Paid', color: 'rgba(22,163,74,0.85)', data: trend.map((t) => t.paid) }
          ],
          credits: { enabled: false },
          legend: { itemStyle: { fontSize: '10px' } }
        })
      );
    }

    const deptEl = el('rmbDashChartDept');
    const deptSpend = d.departmentSpending || {};
    if (deptEl && Object.keys(deptSpend).length) {
      const pairs = Object.entries(deptSpend)
        .map(([name, val]) => ({ name, y: Number(val || 0) }))
        .sort((a, b) => a.y - b.y);
      this.charts.push(
        Highcharts.chart(deptEl, {
          chart: { type: 'bar', backgroundColor: 'transparent', height: 240 },
          title: { text: undefined },
          xAxis: { type: 'category', categories: pairs.map((p) => p.name), labels: { style: { fontSize: '10px' } } },
          yAxis: {
            min: 0,
            title: { text: undefined },
            labels: {
              style: { fontSize: '10px' },
              formatter: this.rupeeAxisShortFmt()
            }
          },
          tooltip: this.rupeeTooltipOptions(),
          plotOptions: { bar: { borderRadius: 3, dataLabels: { enabled: false } } },
          series: [{ type: 'bar', name: 'Submitted', color: '#1B3461', data: pairs.map((p) => p.y) }],
          credits: { enabled: false },
          legend: { enabled: false }
        })
      );
    }

    const catEl = el('rmbDashChartCategory');
    const cats = d.claimsByExpenditureType || {};
    if (catEl && Object.keys(cats).length) {
      this.charts.push(
        Highcharts.chart(catEl, {
          chart: { type: 'pie', backgroundColor: 'transparent', height: 220 },
          title: { text: undefined },
          plotOptions: { pie: { innerSize: '55%', dataLabels: { enabled: false }, borderWidth: 0 } },
          series: [
            {
              type: 'pie',
              name: 'Claims',
              data: Object.keys(cats).map((name) => ({ name, y: Number(cats[name] || 0) }))
            }
          ],
          credits: { enabled: false },
          legend: { itemStyle: { fontSize: '10px' } }
        })
      );
    }
  }

  private renderRejectionTypeChart(d: any): void {
    const rejEl = document.getElementById('rmbDashChartRejectType') as HTMLElement | null;
    const rj = d.rejectionsByExpenditureType || {};
    if (rejEl && Object.keys(rj).length) {
      const pairs = Object.entries(rj)
        .map(([name, val]) => ({ name, y: Number(val || 0) }))
        .sort((a, b) => b.y - a.y);
      this.charts.push(
        Highcharts.chart(rejEl, {
          chart: { type: 'column', backgroundColor: 'transparent', height: 240 },
          title: { text: undefined },
          xAxis: { categories: pairs.map((p) => p.name), labels: { style: { fontSize: '10px' } } },
          yAxis: { min: 0, title: { text: undefined }, allowDecimals: false },
          plotOptions: { column: { color: '#DC2626', borderRadius: 4 } },
          series: [{ type: 'column', name: 'Rejected claims', data: pairs.map((p) => p.y) }],
          credits: { enabled: false },
          legend: { enabled: false }
        })
      );
    }
  }

  private renderRejectionChannelChart(d: any): void {
    const el = document.getElementById('rmbDashChartRejectChannel') as HTMLElement | null;
    const raw = d.rejectionByChannel || {};
    const order = ['HOD', 'Matrix (approval level)', 'HR', 'Finance'];
    const pairs = order
      .map((name) => ({ name, y: Number(raw[name] || 0) }))
      .filter((p) => p.y > 0);
    if (!el || pairs.length === 0) {
      return;
    }
    const colors = ['#EA580C', '#7C3AED', '#2563EB', '#0D9488'];
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'bar', backgroundColor: 'transparent', height: Math.max(200, 56 + pairs.length * 44) },
        title: { text: undefined },
        xAxis: { min: 0, title: { text: undefined }, allowDecimals: false },
        yAxis: { categories: pairs.map((p) => p.name), title: { text: undefined }, labels: { style: { fontSize: '11px' } } },
        plotOptions: {
          bar: {
            borderRadius: 4,
            dataLabels: { enabled: true, style: { fontSize: '10px' } }
          }
        },
        series: [
          {
            type: 'bar',
            name: 'Rejected lines',
            data: pairs.map((p, i) => ({ y: p.y, color: colors[i % colors.length] }))
          }
        ],
        credits: { enabled: false },
        legend: { enabled: false }
      })
    );
  }

  private insightFourSeriesChartIds(kind: 'client' | 'project' | 'department'): {
    raised: string;
    paid: string;
    pending: string;
    rejected: string;
  } {
    const base =
      kind === 'client'
        ? 'rmbDashClientInsight'
        : kind === 'project'
          ? 'rmbDashProjectInsight'
          : 'rmbDashDeptInsight';
    return {
      raised: `${base}LineRaised`,
      paid: `${base}LinePaid`,
      pending: `${base}LinePending`,
      rejected: `${base}LineRejected`
    };
  }

  private renderInsightTabCharts(d: any, kind: 'client' | 'project' | 'department'): void {
    const donutId =
      kind === 'client'
        ? 'rmbDashClientTicketDonut'
        : kind === 'project'
          ? 'rmbDashProjectTicketDonut'
          : 'rmbDashDeptInsightTicketDonut';
    const topDimId =
      kind === 'client'
        ? 'rmbDashClientTopDim'
        : kind === 'project'
          ? 'rmbDashProjectTopDim'
          : 'rmbDashDeptInsightTopDim';
    const topEmpId =
      kind === 'client'
        ? 'rmbDashClientTopEmp'
        : kind === 'project'
          ? 'rmbDashProjectTopEmp'
          : 'rmbDashDeptInsightTopEmp';

    const stack = kind === 'project' ? d.projectEmployeeStack : null;
    const linePack =
      kind === 'client'
        ? d.clientLineChartPack
        : kind === 'department'
          ? d.departmentLineChartPack
          : d.projectLineChartPack;
    const barPack =
      kind === 'client' ? d.barTotalsByClient : kind === 'project' ? d.barTotalsByProject : d.barTotalsByDepartment;

    const dimensionLabel =
      kind === 'client' ? 'clients' : kind === 'project' ? 'projects' : 'departments';
    const lineCategories = linePack?.categories as string[] | undefined;
    const canLine = !!(linePack && Array.isArray(lineCategories) && lineCategories.length > 0);
    if (canLine) {
      this.renderInsightFourSeriesLineCharts(kind, linePack, dimensionLabel);
    } else if (kind === 'project' && stack) {
      const ids = this.insightFourSeriesChartIds(kind);
      this.renderStackedEmployeeColumn(ids.raised, stack);
    }
    this.renderTicketStatusDonut(donutId, d.ticketStatusBreakdown || {});
    this.renderTopDimensionColumnChart(topDimId, barPack, this.topInsightBarLimit);
    this.renderTopEmployeesColumnChart(topEmpId, d.employeeLeaderboard || [], this.topInsightBarLimit);
  }

  private rupeeAxisShortFmt(): Highcharts.AxisLabelsFormatterCallbackFunction {
    return (ctx) => this.rupeeAxisLabel(Number(ctx.value));
  }

  private rupeeTooltipOptions(): Highcharts.TooltipOptions {
    return {
      shared: true,
      valueDecimals: 2,
      valuePrefix: '₹',
      backgroundColor: 'rgba(255,255,255,0.97)',
      borderColor: '#E2E8F0',
      borderRadius: 6
    };
  }

  private renderStackedEmployeeColumn(chartId: string, pack: any): void {
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el || !pack) {
      return;
    }
    const categories = (pack.categories as string[] | undefined) || [];
    const raised = (pack.raised || []).map((n: any) => Number(n || 0));
    if (!categories.length) {
      return;
    }
    const paid = (pack.paid || []).map((n: any) => Number(n || 0));
    const pending = (pack.pending || []).map((n: any) => Number(n || 0));
    const rejected = (pack.rejected || []).map((n: any) => Number(n || 0));
    const h = Math.min(520, Math.max(280, 14 * categories.length));
    const rupeeFmt = this.rupeeAxisShortFmt();
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'column', backgroundColor: 'transparent', height: h },
        title: { text: undefined },
        xAxis: {
          categories,
          labels: { style: { fontSize: '9px' }, rotation: -52, align: 'right' }
        },
        yAxis: {
          min: 0,
          title: { text: undefined },
          stackLabels: { enabled: false },
          labels: { style: { fontSize: '10px' }, formatter: rupeeFmt }
        },
        tooltip: this.rupeeTooltipOptions(),
        plotOptions: { column: { stacking: 'normal', borderRadius: 2, groupPadding: 0.06 } },
        series: [
          { type: 'column', name: 'Raised', color: 'rgba(27,52,97,0.9)', data: raised },
          { type: 'column', name: 'Paid', color: 'rgba(22,163,74,0.9)', data: paid },
          { type: 'column', name: 'Pending', color: 'rgba(234,88,12,0.9)', data: pending },
          { type: 'column', name: 'Rejected', color: 'rgba(220,38,38,0.88)', data: rejected }
        ],
        credits: { enabled: false },
        legend: { itemStyle: { fontSize: '10px' } }
      })
    );
  }

  /**
   * Keeps only categories with any amount in scope, ranks by raised, shows top N as grouped columns and rolls the rest
   * into a single “Others” bucket so master lists (hundreds of clients/projects/depts) stay readable.
   */
  private trimFourSeriesPackForInsight(
    pack: any,
    maxItems: number
  ): {
    categories: string[];
    raised: number[];
    paid: number[];
    pending: number[];
    rejected: number[];
    totalWithActivity: number;
    shown: number;
    othersCount: number;
  } {
    const categories = (pack?.categories as string[]) || [];
    const raised = ((pack?.raised as number[]) || []).map((n) => Number(n || 0));
    const paid = ((pack?.paid as number[]) || []).map((n) => Number(n || 0));
    const pending = ((pack?.pending as number[]) || []).map((n) => Number(n || 0));
    const rejected = ((pack?.rejected as number[]) || []).map((n) => Number(n || 0));
    const rows = categories.map((label, i) => ({
      label: String(label ?? '—'),
      raised: raised[i] ?? 0,
      paid: paid[i] ?? 0,
      pending: pending[i] ?? 0,
      rejected: rejected[i] ?? 0
    }));
    const active = rows.filter((r) => r.raised + r.paid + r.pending + r.rejected > 0);
    active.sort((a, b) => b.raised - a.raised);
    const top = active.slice(0, maxItems);
    const rest = active.slice(maxItems);
    if (rest.length) {
      top.push({
        label: `Others (${rest.length})`,
        raised: rest.reduce((s, r) => s + r.raised, 0),
        paid: rest.reduce((s, r) => s + r.paid, 0),
        pending: rest.reduce((s, r) => s + r.pending, 0),
        rejected: rest.reduce((s, r) => s + r.rejected, 0)
      });
    }
    return {
      categories: top.map((r) => this.truncateDashLabel(r.label, 42)),
      raised: top.map((r) => r.raised),
      paid: top.map((r) => r.paid),
      pending: top.map((r) => r.pending),
      rejected: top.map((r) => r.rejected),
      totalWithActivity: active.length,
      shown: Math.min(maxItems, active.length),
      othersCount: rest.length
    };
  }

  /** Four separate line charts (Raised / Paid / Pending / Rejected) for top active dimensions. */
  private renderInsightFourSeriesLineCharts(
    kind: 'client' | 'project' | 'department',
    pack: any,
    dimensionLabel: string
  ): void {
    const ids = this.insightFourSeriesChartIds(kind);
    const trimmed = this.trimFourSeriesPackForInsight(pack, this.insightFourSeriesTopLimit);
    const scopeNote =
      trimmed.othersCount > 0
        ? `Top ${trimmed.shown} + Others (${trimmed.othersCount})`
        : `${trimmed.totalWithActivity} ${dimensionLabel}`;
    const metrics: { id: string; title: string; color: string; data: number[] }[] = [
      { id: ids.raised, title: 'Raised', color: '#1B3461', data: trimmed.raised },
      { id: ids.paid, title: 'Paid', color: '#16A34A', data: trimmed.paid },
      { id: ids.pending, title: 'Pending', color: '#D97706', data: trimmed.pending },
      { id: ids.rejected, title: 'Rejected', color: '#DC2626', data: trimmed.rejected }
    ];
    if (!trimmed.categories.length) {
      this.renderInsightSingleMetricLineChart(
        ids.raised,
        'Raised',
        '#1B3461',
        [],
        [],
        `No ${dimensionLabel} with reimbursement in current filters`
      );
      return;
    }
    for (const m of metrics) {
      this.renderInsightSingleMetricLineChart(
        m.id,
        m.title,
        m.color,
        trimmed.categories,
        m.data,
        scopeNote
      );
    }
  }

  /** One metric line chart: markers, grid, value labels on points (sample-chart style). */
  private renderInsightSingleMetricLineChart(
    chartId: string,
    seriesTitle: string,
    color: string,
    categories: string[],
    data: number[],
    scopeNote?: string
  ): void {
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el) {
      return;
    }
    const rupeeFmt = this.rupeeAxisShortFmt();
    const n = categories.length;
    const pointLabelFmt: Highcharts.DataLabelsFormatterCallbackFunction = function () {
      const v = Number(this.y);
      if (!v || v <= 0) {
        return '';
      }
      if (v >= 100000) {
        return '₹' + (v / 100000).toFixed(1) + 'L';
      }
      if (v >= 1000) {
        return '₹' + (v / 1000).toFixed(1) + 'k';
      }
      return '₹' + v.toFixed(0);
    };
    this.charts.push(
      Highcharts.chart(el, {
        chart: {
          type: 'line',
          backgroundColor: '#ffffff',
          height: 220,
          plotBorderWidth: 1,
          plotBorderColor: '#CBD5E1',
          spacingTop: 6,
          spacingRight: 8,
          spacingLeft: 4,
          spacingBottom: 4
        },
        title: {
          text: seriesTitle,
          style: { fontSize: '12px', fontWeight: '600', color }
        },
        subtitle: scopeNote
          ? { text: scopeNote, style: { fontSize: '9px', color: '#94A3B8' }, align: 'left' }
          : undefined,
        xAxis: {
          categories,
          gridLineWidth: 1,
          gridLineColor: '#E2E8F0',
          lineColor: '#CBD5E1',
          tickmarkPlacement: 'on',
          labels: {
            style: { fontSize: '9px', color: '#475569' },
            rotation: n > 4 ? -32 : -18,
            align: 'right'
          }
        },
        yAxis: {
          min: 0,
          title: { text: undefined },
          gridLineWidth: 1,
          gridLineColor: '#E2E8F0',
          labels: { style: { fontSize: '9px', color: '#64748B' }, formatter: rupeeFmt }
        },
        tooltip: {
          ...this.rupeeTooltipOptions(),
          shared: false,
          headerFormat: '<span style="font-size:10px">{point.key}</span><br/>'
        },
        plotOptions: {
          line: {
            lineWidth: 3,
            marker: {
              enabled: true,
              radius: 5,
              symbol: 'circle',
              fillColor: color,
              lineWidth: 2,
              lineColor: '#ffffff'
            },
            dataLabels: {
              enabled: true,
              formatter: pointLabelFmt,
              style: { fontSize: '9px', fontWeight: '600', color: '#334155', textOutline: 'none' },
              y: -8
            }
          },
          series: { animation: { duration: 400 } }
        },
        series: [{ type: 'line', name: seriesTitle, color, data }],
        credits: { enabled: false },
        legend: { enabled: false }
      })
    );
  }

  /**
   * 4-series line chart aligned with HRMS prototype (Chart.js `iCl` / `iDt`): colors raised #1B3461 · paid #16A34A ·
   * pending #D97706 · rejected #DC2626; spline (~tension 0.35); filled markers + white ring; legend top; light Y grid;
   * rejected dashed only when {@link opts.dashRejectedOnly} (client + project match `iCl`, department matches `iDt`).
   */
  private renderDashboardLineSeriesChart(
    chartId: string,
    pack: any,
    opts?: { dashRejectedOnly?: boolean }
  ): void {
    const dashRejectedOnly = opts?.dashRejectedOnly !== false;
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el || !pack) {
      return;
    }
    const categories = (pack.categories as string[]) || [];
    if (!categories.length) {
      return;
    }
    const raised = (pack.raised || []).map((n: any) => Number(n || 0));
    const paid = (pack.paid || []).map((n: any) => Number(n || 0));
    const pending = (pack.pending || []).map((n: any) => Number(n || 0));
    const rejected = (pack.rejected || []).map((n: any) => Number(n || 0));
    const rupeeFmt = this.rupeeAxisShortFmt();
    const step = categories.length > 36 ? Math.ceil(categories.length / 36) : 1;
    const n = categories.length;
    const lineWidth = 2.5;
    const markerRadius = n > 56 ? 3.25 : n > 32 ? 3.75 : 4;

    const C = {
      raised: '#1B3461',
      paid: '#16A34A',
      pending: '#D97706',
      rejected: '#DC2626'
    };
    const yGrid = 'rgba(0,0,0,0.04)';

    /** Prototype: point fill = series color, white border ~1.5px */
    const marker = (seriesColor: string): Highcharts.PointMarkerOptionsObject => ({
      enabled: true,
      symbol: 'circle',
      radius: markerRadius,
      lineWidth: 1.5,
      lineColor: '#ffffff',
      fillColor: seriesColor
    });

    const splineSeries = (
      name: string,
      color: string,
      data: number[],
      dashStyle?: Highcharts.DashStyleValue
    ): Highcharts.SeriesSplineOptions => {
      const s: Highcharts.SeriesSplineOptions = {
        type: 'spline',
        name,
        color,
        lineWidth,
        data,
        marker: marker(color)
      };
      if (dashStyle) {
        s.dashStyle = dashStyle;
      }
      return s;
    };

    this.charts.push(
      Highcharts.chart(el, {
        chart: {
          backgroundColor: 'transparent',
          height: Math.min(480, Math.max(230, 200 + Math.min(n, 20) * 8)),
          plotBackgroundColor: '#ffffff',
          plotBorderWidth: 1,
          plotBorderColor: '#E2E8F0',
          spacingTop: 8,
          spacingRight: 10,
          spacingLeft: 6,
          spacingBottom: 6
        },
        title: { text: undefined },
        xAxis: {
          categories,
          tickmarkPlacement: 'on',
          lineColor: '#CBD5E1',
          gridLineWidth: 0,
          labels: {
            style: { fontSize: '9px', color: '#64748B' },
            rotation: n > 14 ? -25 : -12,
            align: 'right',
            step
          }
        },
        yAxis: {
          min: 0,
          title: { text: undefined },
          lineWidth: 0,
          gridLineWidth: 1,
          gridLineColor: yGrid,
          labels: { style: { fontSize: '9px', color: '#64748B' }, formatter: rupeeFmt }
        },
        tooltip: this.rupeeTooltipOptions(),
        plotOptions: {
          spline: {
            lineWidth,
            states: { hover: { lineWidthPlus: 0.5 } }
          },
          series: {
            turboThreshold: 0,
            animation: { duration: 450 },
            dataLabels: { enabled: false }
          }
        },
        series: [
          splineSeries('Raised', C.raised, raised),
          splineSeries('Paid', C.paid, paid),
          splineSeries('Pending', C.pending, pending),
          splineSeries(
            'Rejected',
            C.rejected,
            rejected,
            dashRejectedOnly ? 'Dash' : undefined
          )
        ],
        credits: { enabled: false },
        legend: {
          align: 'center',
          verticalAlign: 'top',
          layout: 'horizontal',
          floating: false,
          margin: 0,
          padding: 8,
          itemDistance: 10,
          itemStyle: { fontSize: '9px', fontWeight: '500', color: '#555555' },
          symbolRadius: 5,
          symbolWidth: 10,
          symbolHeight: 10,
          squareSymbol: false
        }
      } as Highcharts.Options)
    );
  }

  private renderTicketStatusDonut(chartId: string, breakdown: Record<string, number>): void {
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el || !Object.keys(breakdown).length) {
      return;
    }
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'pie', backgroundColor: 'transparent', height: 280 },
        title: { text: undefined },
        plotOptions: {
          pie: { innerSize: '66%', dataLabels: { enabled: false }, borderWidth: 0, showInLegend: true }
        },
        series: [
          {
            type: 'pie',
            name: 'Tickets',
            data: Object.keys(breakdown).map((name) => ({
              name,
              y: Number(breakdown[name] || 0)
            }))
          }
        ],
        credits: { enabled: false },
        legend: { align: 'right', layout: 'vertical', verticalAlign: 'middle', itemStyle: { fontSize: '10px' } }
      })
    );
  }

  /** Display name for bar-total rows (never raw numeric ids on chart axis). */
  private dimensionBarRowLabel(row: Record<string, unknown>): string {
    const label = row['label'] ?? row['clientName'] ?? row['projectName'] ?? row['department'];
    if (label != null && String(label).trim() !== '') {
      return String(label).trim();
    }
    return '—';
  }

  private renderTopDimensionColumnChart(
    chartId: string,
    pack: any,
    maxItems: number,
    seriesName = 'Raised (₹)',
    color = 'rgba(27,52,97,0.88)'
  ): void {
    const rows = [...((pack?.rows as Record<string, unknown>[]) || [])];
    rows.sort((a, b) => Number(b['requested'] ?? 0) - Number(a['requested'] ?? 0));
    const top = rows.slice(0, maxItems);
    if (!top.length) {
      return;
    }
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el) {
      return;
    }
    const categories = top.map((r) => this.truncateDashLabel(this.dimensionBarRowLabel(r), 36));
    const data = top.map((r) => Number(r['requested'] ?? 0));
    const h = Math.min(420, Math.max(280, 220 + Math.min(top.length, maxItems) * 8));
    const rupeeFmt = this.rupeeAxisShortFmt();
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'column', backgroundColor: 'transparent', height: h },
        title: { text: undefined },
        xAxis: {
          categories,
          labels: {
            style: { fontSize: '10px' },
            rotation: top.length > 5 ? -42 : -28,
            align: 'right'
          }
        },
        yAxis: {
          min: 0,
          title: { text: undefined },
          labels: { style: { fontSize: '10px' }, formatter: rupeeFmt }
        },
        tooltip: this.rupeeTooltipOptions(),
        plotOptions: {
          column: {
            borderRadius: 4,
            groupPadding: 0.12,
            pointPadding: 0.06,
            dataLabels: { enabled: false }
          }
        },
        series: [{ type: 'column', name: seriesName, color, data }],
        credits: { enabled: false },
        legend: { enabled: false }
      })
    );
  }

  private renderTopEmployeesColumnChart(
    chartId: string,
    board: any[],
    maxItems: number,
    seriesName = 'Raised (₹)',
    color = 'rgba(22,101,52,0.88)'
  ): void {
    const sorted = [...(board || [])].sort((a, b) => Number(b.requested ?? 0) - Number(a.requested ?? 0));
    const top = sorted.slice(0, maxItems);
    if (!top.length) {
      return;
    }
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el) {
      return;
    }
    const categories = top.map((r) =>
      this.truncateDashLabel(String(r.fullName ?? r.name ?? '—').trim(), 36)
    );
    const data = top.map((r) => Number(r.requested ?? 0));
    const h = Math.min(420, Math.max(280, 220 + Math.min(top.length, maxItems) * 8));
    const rupeeFmt = this.rupeeAxisShortFmt();
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'column', backgroundColor: 'transparent', height: h },
        title: { text: undefined },
        xAxis: {
          categories,
          labels: {
            style: { fontSize: '10px' },
            rotation: top.length > 5 ? -42 : -28,
            align: 'right'
          }
        },
        yAxis: {
          min: 0,
          title: { text: undefined },
          labels: { style: { fontSize: '10px' }, formatter: rupeeFmt }
        },
        tooltip: this.rupeeTooltipOptions(),
        plotOptions: {
          column: {
            borderRadius: 4,
            groupPadding: 0.12,
            pointPadding: 0.06,
            dataLabels: { enabled: false }
          }
        },
        series: [{ type: 'column', name: seriesName, color, data }],
        credits: { enabled: false },
        legend: { enabled: false }
      })
    );
  }

  private truncateDashLabel(s: string, maxLen: number): string {
    if (s == null || s === '') {
      return '';
    }
    if (s.length <= maxLen) {
      return s;
    }
    return s.slice(0, maxLen - 1) + '…';
  }
}
