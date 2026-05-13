import { Component, OnDestroy, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import * as Highcharts from 'highcharts';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

type DashTab =
  | 'overview'
  | 'lifecycle'
  | 'approval'
  | 'rejection'
  | 'employee'
  | 'alerts'
  | 'clientTrend'
  | 'projectTrend'
  | 'deptTrend';

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
  filterOptions: any = null;
  loadError: string | null = null;
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

  constructor(
    private authenticationService: AuthenticationService,
    private reimbursementService: ReimbursementService
  ) {
    this.authenticationService.currentUser.subscribe((x) => (this.currentUser = x));
  }

  ngOnInit(): void {
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

  resetFilters(): void {
    this.fySelection = '';
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

  formatRupee(v: any): string {
    if (v == null || v === '') {
      return '0';
    }
    const n = typeof v === 'string' ? Number(v.replace(/,/g, '')) : Number(v);
    if (Number.isNaN(n)) {
      return String(v);
    }
    return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(n);
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
        s: 'HOD / HR / Finance pending lines',
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
        l: 'Rejected claim lines',
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
    const hod = Number(d.pendingHodTickets || 0);
    const hr = Number(d.pendingHrTickets || 0);
    const fin = Number(d.pendingFinanceTickets || 0);
    const pendAny = hod + hr + fin > 0;
    const aging = Number(d.agingPendingTicketsOver7Days || 0);
    const finLines = Number(d.approvedClaimLines || 0);
    return [
      {
        l: 'Partial-approval tickets',
        v: this.formatNumber(d.partialApprovalTickets),
        s: 'Mixed outcomes at HOD/HR',
        vc: partial > 0 ? 'rmb-tone-warning' : 'rmb-tone-neutral',
        sc: partial > 0 ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Pending HOD / HR / Fin',
        v:
          this.formatNumber(d.pendingHodTickets) +
          ' / ' +
          this.formatNumber(d.pendingHrTickets) +
          ' / ' +
          this.formatNumber(d.pendingFinanceTickets),
        s: 'Ticket counts',
        vc: pendAny ? 'rmb-tone-warning' : 'rmb-tone-zero',
        sc: pendAny ? 'rmb-sub-warn' : 'rmb-sub-muted'
      },
      {
        l: 'Aging pending >7d',
        v: this.formatNumber(d.agingPendingTicketsOver7Days),
        s: 'Workflow backlog',
        vc: aging > 0 ? 'rmb-tone-danger' : 'rmb-tone-neutral',
        sc: aging > 0 ? 'rmb-sub-danger' : 'rmb-sub-muted'
      },
      {
        l: 'Finance-ready lines',
        v: this.formatNumber(d.approvedClaimLines),
        s: 'Approved at HR + Finance',
        vc: finLines > 0 ? 'rmb-tone-success' : 'rmb-tone-zero',
        sc: finLines > 0 ? 'rmb-sub-success' : 'rmb-sub-muted'
      }
    ];
  }

  funnelVal(f: any, key: string): number {
    if (!f || f[key] == null) {
      return 0;
    }
    return Number(f[key]);
  }

  funnelPct(value: number, submitted: number): string {
    return this.pct(value, submitted || 1);
  }

  /** Pending pipeline ₹ split by days since ticket submit (claim-line pipeline statuses). */
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

  trackDeptFilterOption(_index: number, d: string): string {
    return d || String(_index);
  }

  trackClientFilterOption(_index: number, c: { clientId?: number }): number {
    return c?.clientId ?? _index;
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

  alertsCount(): number {
    return (this.reimbursementDashboard?.alerts || []).length;
  }

  async loadDashboard(): Promise<void> {
    this.loadError = null;
    this.loading = true;
    this.reimbursementDashboard = null;
    this.filterOptions = null;
    this.destroyCharts();
    try {
      const body = this.buildFilterPayload();
      const dash: any = await this.reimbursementService
        .fetchReimbursementDashboard(body)
        .pipe(first())
        .toPromise();
      if (dash?.serviceStatus === 'Success') {
        this.reimbursementDashboard = dash.serviceResponse;
        this.filterOptions = dash.serviceResponse?.filterOptions || null;
        this.lastLoadedAt = new Date();
        this.lifecyclePage = 0;
        setTimeout(() => this.renderCharts(), 0);
      } else {
        this.loadError = dash?.serviceError || dash?.serviceResponse || 'Unable to load dashboard.';
      }
    } catch (e: any) {
      this.loadError = e?.message || 'Unable to load dashboard.';
    } finally {
      this.loading = false;
    }
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
    const d = this.reimbursementDashboard;
    if (!d) {
      return;
    }
    const tab = this.activeTab;
    if (tab === 'overview') {
      this.renderOverviewCharts(d);
    } else if (tab === 'rejection') {
      this.renderRejectionTypeChart(d);
    } else if (tab === 'clientTrend') {
      this.renderInsightTabCharts(d, 'client');
    } else if (tab === 'projectTrend') {
      this.renderInsightTabCharts(d, 'project');
    } else if (tab === 'deptTrend') {
      this.renderInsightTabCharts(d, 'department');
    }
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
              formatter(): string {
                const v = (this as any).value as number;
                if (v >= 100000) {
                  return '₹' + v / 100000 + 'L';
                }
                if (v >= 1000) {
                  return '₹' + v / 1000 + 'k';
                }
                return '₹' + v;
              }
            }
          },
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
              formatter(): string {
                const v = (this as any).value as number;
                return v >= 100000 ? '₹' + v / 100000 + 'L' : '₹' + v / 1000 + 'k';
              }
            }
          },
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

  private renderInsightTabCharts(d: any, kind: 'client' | 'project' | 'department'): void {
    const stackId =
      kind === 'client'
        ? 'rmbDashClientEmployeeStack'
        : kind === 'project'
          ? 'rmbDashProjectEmployeeStack'
          : 'rmbDashDeptInsightEmployeeStack';
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

    const lineCategories = linePack?.categories as string[] | undefined;
    const canLine = !!(linePack && Array.isArray(lineCategories) && lineCategories.length > 0);
    if (canLine) {
      /** Prototype: client `iCl` dashes rejected only; department `iDt` keeps all lines solid. */
      const dashRejectedOnly = kind !== 'department';
      this.renderDashboardLineSeriesChart(stackId, linePack, { dashRejectedOnly });
    } else if (kind === 'project') {
      this.renderStackedEmployeeColumn(stackId, stack);
    }
    this.renderTicketStatusDonut(donutId, d.ticketStatusBreakdown || {});
    this.renderTopDimensionBar(topDimId, barPack, 8);
    this.renderTopEmployeesHorizontalBar(topEmpId, d.employeeLeaderboard || [], 8);
  }

  private rupeeAxisShortFmt(): any {
    return function (this: any): string {
      const v = Number(this.value);
      if (v >= 100000) {
        return '₹' + v / 100000 + 'L';
      }
      if (v >= 1000) {
        return '₹' + v / 1000 + 'k';
      }
      return '₹' + v;
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
        tooltip: {
          shared: true,
          valueDecimals: 0,
          valuePrefix: '₹',
          backgroundColor: 'rgba(255,255,255,0.97)',
          borderColor: '#E2E8F0',
          borderRadius: 6
        },
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

  private renderTopDimensionBar(chartId: string, pack: any, maxItems: number): void {
    const rows = [...((pack?.rows as { label: string; requested?: number }[]) || [])];
    rows.sort((a, b) => Number(b.requested ?? 0) - Number(a.requested ?? 0));
    const top = rows.slice(-maxItems);
    if (!top.length) {
      return;
    }
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el) {
      return;
    }
    const categories = top.map((r) => this.truncateDashLabel(r.label, 48));
    const data = top.map((r) => Number(r.requested ?? 0));
    const h = Math.min(420, Math.max(180, 26 * top.length));
    const rupeeFmt = this.rupeeAxisShortFmt();
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'bar', backgroundColor: 'transparent', height: h },
        title: { text: undefined },
        xAxis: { min: 0, title: { text: undefined }, labels: { style: { fontSize: '10px' }, formatter: rupeeFmt } },
        yAxis: { categories, title: { text: undefined }, labels: { style: { fontSize: '10px' } } },
        plotOptions: { bar: { borderRadius: 3, dataLabels: { enabled: false } } },
        series: [{ type: 'bar', name: 'Raised (₹)', color: 'rgba(27,52,97,0.88)', data }],
        credits: { enabled: false },
        legend: { enabled: false }
      })
    );
  }

  private renderTopEmployeesHorizontalBar(chartId: string, board: any[], maxItems: number): void {
    const sorted = [...(board || [])].sort((a, b) => Number(b.requested ?? 0) - Number(a.requested ?? 0));
    const top = sorted.slice(0, maxItems).reverse();
    if (!top.length) {
      return;
    }
    const el = document.getElementById(chartId) as HTMLElement | null;
    if (!el) {
      return;
    }
    const categories = top.map((r) => this.truncateDashLabel(String(r.fullName ?? '—'), 40));
    const data = top.map((r) => Number(r.requested ?? 0));
    const h = Math.min(420, Math.max(180, 26 * top.length));
    const rupeeFmt = this.rupeeAxisShortFmt();
    this.charts.push(
      Highcharts.chart(el, {
        chart: { type: 'bar', backgroundColor: 'transparent', height: h },
        title: { text: undefined },
        xAxis: { min: 0, title: { text: undefined }, labels: { style: { fontSize: '10px' }, formatter: rupeeFmt } },
        yAxis: { categories, title: { text: undefined }, labels: { style: { fontSize: '10px' } } },
        plotOptions: { bar: { borderRadius: 3, dataLabels: { enabled: false } } },
        series: [{ type: 'bar', name: 'Raised (₹)', color: 'rgba(22,101,52,0.88)', data }],
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
