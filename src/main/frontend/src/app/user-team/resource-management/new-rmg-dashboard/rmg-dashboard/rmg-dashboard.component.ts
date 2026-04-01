import { Component, ElementRef, EventEmitter, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from 'src/app/app.component';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { FilterStateService } from 'src/app/services/filter-state.service';
import { LoaderService } from 'src/app/services/loader.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { RmgProjectConfigComponent } from '../../rmg-project-config/rmg-project-config.component';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import * as moment from 'moment';
import { Sort } from '@angular/material/sort';
import { first, firstValueFrom, pipe } from 'rxjs';
import { RMGDashboardProjectRequest } from 'src/app/models/rmgDashboardProjectRequest';
import * as Highcharts from 'highcharts';
import HC_more from 'highcharts/highcharts-more';
import HC_solidGauge from 'highcharts/modules/solid-gauge';
import HC_xrange from 'highcharts/modules/xrange';

HC_more(Highcharts);
HC_solidGauge(Highcharts);
HC_xrange(Highcharts);

@Component({
  standalone: false,
  selector: 'app-rmg-dashboard',
  templateUrl: './rmg-dashboard.component.html',
  styleUrl: './rmg-dashboard.component.css'
})

export class RmgDashboardComponent implements OnInit {

  feature = "Resource Management";
  todaysDate: any
  currentUser: User;
  userMapping: any = {};
  currentBreadcrumbList: any[] = [];

  @Output() actionTriggered = new EventEmitter<{ action: string; project: any; }>();

  @ViewChild('rmgProjectConfig') rmgProjectComponent!: RmgProjectConfigComponent;
  @ViewChild('chartSection') chartSection!: ElementRef;
  @ViewChild("project_configuration") projectConfigurationTemplateRef: TemplateRef<any>;
  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
  @ViewChild('project_completion_date_picker') projectCompletionDatePickerTemplateRef!: TemplateRef<any>;
  @ViewChild('project_completion_confirmation') projectCompletionConfirmationTemplateRef!: TemplateRef<any>;
  @ViewChild("restore_project") restoreProjectTemplateRef: TemplateRef<any>;
  @ViewChild("create_internal_project_confirmation") createInternalProjectConfirmationTemplateRef: TemplateRef<any>;
  @ViewChild("project_timesheet_summary") projectTimesheetSummaryTemplateRef: TemplateRef<any>;
  @ViewChild("reject_project") rejectProjectTemplateRef: TemplateRef<any>;
  @ViewChild("approve_project") approveProjectTemplateRef: TemplateRef<any>;
  @ViewChild("client_side_id_confirmation") clientSideIdConfirmationTemplateRef: TemplateRef<any>;

  @ViewChild('employee_details') employeeDetailsTemplateRef: TemplateRef<any>;
  @ViewChild('employee_project_timesheet_summary') employeeProjectTimesheetSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('expired_tnm_projects_summary') expiredTNMProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('fixed_cost_projects_summary') fixedCostProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('total_projects_summary') totalProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('team_project_status_summary') teamProjectStatusSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('timesheet_non_compliance_project_details') timesheetNonComplianceProjectDetailsTemplateRef: TemplateRef<any>;

  alertMessageModalRef: NgbModalRef;
  employeeDetailsModalRef: NgbModalRef;
  employeeProjectTimesheetSummaryModalRef: NgbModalRef;
  expiredTNMProjectsSummaryModalRef: NgbModalRef;
  fixedCostProjectsSummaryModalRef: NgbModalRef;
  totalProjectsSummaryModalRef: NgbModalRef;
  timesheetNonComplianceProjectDetailsModalRef: NgbModalRef;
  teamProjectStatusSummaryModalRef: NgbModalRef;

  // HTML Configs 
  kpis = [
    { label: 'TOTAL PROJECTS', value: null, change: null, period: 'vs last week', status: null, icon: 'bi-building' },
    { label: 'EMPLOYEES ONBOARDED', value: null, change: null, period: 'vs last week', status: null, icon: 'bi-people' },
    { label: '% OF EMPLOYEES ON CLIENT PROJECTS', value: null, suffix: '%', change: null, period: 'vs last month', status: null, icon: 'bi-graph-up' },
  ];

  attentionRequiredProjectAlerts = [
    { value: null, label: 'Understaffed TNM' },
    { value: null, label: 'Overstaffed TNM' },
    { value: null, label: 'Defaulter FC' },
    { value: null, label: 'Expired TNM' },
    { value: null, label: 'No Timesheet' },
  ];

  statusCards = [
    { label: 'TOTAL PROJECTS', value: null, icon: 'bi-globe', colorClass: 'text-info', key: 'ALL' },
    { label: 'ACTIVE PROJECTS', value: null, icon: 'bi-lightning', colorClass: 'text-warning', key: 'TOTAL' },
    { label: 'TNM PROJECTS', value: null, icon: 'bi-activity', colorClass: 'text-success', key: 'TOTAL_TNM' },
    { label: 'FIXED COST', value: null, icon: 'bi-currency-dollar', colorClass: 'text-info', key: 'TOTAL_FC' },
    { label: 'MONITORING', value: null, icon: 'bi-bar-chart', colorClass: 'text-navy', key: 'TOTAL_MONITORING' },
    { label: 'INTERNAL & BENCH', value: null, icon: 'bi-people', colorClass: 'text-success', key: 'TOTAL_INTERNAL' },
  ];

  projectLifeCycleStages = [
    { value: null, label: 'Not Started', desc: 'Awaiting project kickoff', colorClass: 'text-muted', icon: 'fa-solid fa-minus-circle ', key: 'NOT_STARTED' },
    { value: null, label: 'Pending', desc: 'Awaiting HOD / PM approval', colorClass: 'text-warning', icon: 'fa-solid fa-clock', key: 'PENDING_FOR_APPROVAL' },
    { value: null, label: 'Approved', desc: 'Approved by HOD / PM', colorClass: 'text-success', icon: 'fa-solid fa-check-circle', key: 'APPROVED' },
    { value: null, label: 'Scheduled', desc: 'Planned for Future Start', colorClass: 'text-info', icon: 'fa-solid fa-calendar-check', key: 'SCHEDULED' },
    { value: null, label: 'Rejected', desc: 'Not approved by HOD / PM', colorClass: 'text-danger', icon: 'fa-solid fa-times-circle', key: 'REJECTED' },
  ];

  resourceCards = [
    { label: 'Understaffed', value: null, icon: 'bi-arrow-trend-down', colorClass: 'text-info', key: 'UNDERBOARDED' },
    { label: 'Overboarded', value: null, icon: 'bi-arrow-trend-up', colorClass: 'text-warning', key: 'OVERBOARDED' }
  ];

  tnmExpiredBars = [
    { label: '12M+', value: null, width: 8, color: '#2f467f', key: 'expiredProjectsAbove12Months' },
    { label: '6-12M', value: null, width: 14, color: '#3f5796', key: 'expiredProjects6To12Months' },
    { label: '3-6M', value: null, width: 25, color: '#4f68a9', key: 'expiredProjects3To6Months' },
    { label: '2-3M', value: null, width: 14, color: '#627bb8', key: 'expiredProjects2To3Months' },
    { label: '1-2M', value: null, width: 17, color: '#6f85c0', key: 'expiredProjects1To2Months' },
    { label: '0-1M', value: null, width: 22, color: '#7b8fc7', key: 'expiredProjectsWithin1Month' },
  ];

  insights = [
    { text: '70 projects need attention', sub: '12 Understaffed · 8 Overstaffed · 5 Defaulter FC · 36 Expired · 9 No Timesheet', stat: '70/77', colorClass: 'bg-danger' },
    { text: '1 project stuck in Pending Approval', sub: 'Approval bottleneck detected', stat: '1/52', colorClass: 'bg-warning' },
    { text: '12 TNM projects understaffed', sub: '+2 since last week', stat: '12/36', colorClass: 'bg-danger', badge: '+2' },
    { text: '8 TNM projects overboarded', sub: 'Wrong roles or excess staff', stat: '8/36', colorClass: 'bg-danger' },
    { text: '8 TNM projects expired since 6+ months', sub: '3 expired >12 months — highest priority', stat: '8/36', colorClass: 'bg-warning' },
    { text: '5 fixed cost defaulters', sub: '5 on time out of 10 active', stat: '5/10', colorClass: 'bg-warning' },
  ];

  fixedCostItems = [
    { label: '3 Months', value: null },
    { label: '6 Months', value: null },
    { label: '1 Year', value: null },
  ];

  completedItems = [
    { label: 'iShine Closed', value: null },
    { label: 'Shankh Closed', value: null },
    { label: 'Shankh + Active', value: null },
  ];

  workforceOverview = [
    { icon: 'bi-people', label: 'Active Employees In Apmosys', value: '1,302', pct: '100%', desc: 'Full organization headcount', colorClass: 'text-info' },
    { icon: 'bi-person-check', label: 'Assigned to Projects', value: '429', pct: '32.9%', desc: 'Mapped to ≥1 project', colorClass: 'text-success' },
    { icon: 'bi-person-x', label: 'Unassigned Employees', value: '873', pct: '67.1%', desc: 'No active project mapping', colorClass: 'text-warning' },
  ];

  projectDistribution = [
    { icon: 'bi-building', label: 'Internal & Bench project Allocation', value: '187', pct: '14.4%', desc: 'Internal projects only', colorClass: 'text-info' },
    { icon: 'bi-arrow-left-right', label: 'Dual Allocation (Internal & Client)', value: '42', pct: '3.2%', desc: 'Both Internal & Client', colorClass: 'text-accent', tag: 'OVERLAP' },
    { icon: 'bi-briefcase', label: 'Client project Allocations', value: '284', pct: '21.8%', desc: 'Client projects only', colorClass: 'text-success' },
  ];

  riskItems = [
    { icon: 'bi-exclamation-triangle', label: 'Bench with Allocation', value: '131', pct: '10.1%', desc: 'On bench but assigned elsewhere', colorClass: 'text-warning' },
    { icon: 'bi-clock', label: 'Extended Bench', value: '6', pct: '0.5%', desc: 'Bench for 30+ days', colorClass: 'text-danger' },
    { icon: 'bi-person-x', label: 'No Billable Assignment', value: '10', pct: '0.8%', desc: 'No default billable type', colorClass: 'text-danger' },
  ];

  // Flags
  isEmployeeView: boolean = false;

  // Arrays
  selectedDepartmentIds: any[] = [];
  oldSelectedDepartmentIds: any[] = [];
  employeeList: Employee[] = [];
  filteredEmployeeList: Employee[] = [];
  departmentList: any[] = [];
  filteredDepartmentList: any[] = [];
  projectDetailsList: any[] = [];

  // Variables
  selectedEmpId: any = 0;
  attentionRequiredProjectCount: any = 0;
  selectedProjectStatusLabel: string = '';
  alertMessage: string = '';
  projectStatus: string = '';
  expiredTNMProjectFilter: string = '';
  fixedCostProjectFilter: string = '';

  // Objects 

  // Project Details Table
  searchOnEnter: boolean = true;
  isProjectSearchEnabled: boolean = false;
  totalProjectsCount: number;
  projectPage: number = 0;
  projectPageSize: number = 10;
  pageSizeOptions: any[] = ['5', '10', '20', '50'];
  projectSortDirection: string = 'asc';
  projectSortColumn: string;
  projectSortColumnType: string;
  projectFilters: any = {};
  projectColumns: any[] = ["blank", "name", "poNo", "poProjectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "blank", "blank", "state", "blank", "status", "projectStatus", "draftStatus"];


  constructor(
    private route: ActivatedRoute,
    private appComponent: AppComponent,
    private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
    private employee360Service: Employee360Service,
    private exportExcelService: ExportExcelService,
    private filterStateService: FilterStateService,
    private loaderService: LoaderService,
    private modalService: NgbModal,
    private projectService: ProjectService,
    private resourceManagementService: ResourceManagementService,
    private router: Router,
    private teamService: TeamService,
    private utilityService: UtilityService,
    public validationService: ValidationService,
  ) {
    this.todaysDate = new Date();
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
  }

  ngOnInit() {
    this.projectStatus = 'TOTAL';
    this.expiredTNMProjectFilter = 'allExpiredTNMProjectsCount';
    this.fixedCostProjectFilter = 'all';
    this.projectPageSize = this.filterStateService?.projectPageSize ? this.filterStateService.projectPageSize : 10;

    this.getDepartmentsList();
    this.getEmployeeNameAndEmpld();
    this.mapSubFeatureFlag();
    this.loadRMGDashboard();
  }

  // Department Table APIs & Methods Starts
  onDepartmentSelectionChange(event: any) {
    if (this.validationService.areArraysEqual(this.oldSelectedDepartmentIds, this.selectedDepartmentIds)) {
      return;
    }
    this.oldSelectedDepartmentIds = [...this.selectedDepartmentIds];
    // if (this.rmgStatusCardsComponent) {
    //   this.rmgStatusCardsComponent.onDepartmentSelectionChange(event);
    // }
  }

  getDepartmentsList() {
    this.departmentList = [];
    this.filteredDepartmentList = [];
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.departmentList = response.serviceResponse || [];
        this.filteredDepartmentList = [...this.departmentList];
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }

  getEmployeeNameAndEmpld() {
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.filteredEmployeeList = [...this.employeeList];
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }
  // Department Table APIs & Methods End

  // Helpers Start
  get isAccounts(): boolean {
    const deptName = String(this.currentUser?.departmentName || '').trim();
    return deptName === 'Accounts';
  }

  get currentUserType(): 'HOD' | 'ADMIN' | 'USER' {
    const deptName = String(this.currentUser?.departmentName || '').trim();
    const empRole = String(this.currentUser?.employeeRole || '').trim();
    const adminKeywords = ['Admin', 'Resource Management Group', 'Director', 'Super Admin', 'Accounts', 'HR'];

    const isAdmin = adminKeywords.some(keyword => deptName.includes(keyword) || empRole.includes(keyword));
    if (isAdmin) {
      return 'ADMIN';
    }
    if (empRole === 'HOD' || empRole === 'SuperAdmin' || empRole === 'Super Admin') {
      return 'HOD';
    }
    return 'USER';
  }

  private mapSubFeatureFlag() {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  normalizeDate(dateInput: any) {
    if (!dateInput) {
      return null;
    }
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return null;
    }
    return moment(dateInput).startOf('day').format('YYYY-MM-DD');
  }

  getRMGRequestObject() {
    let rmgProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    rmgProjectRequest.currentUserEmpId = this.currentUser.empId;
    rmgProjectRequest.currentUserType = this.currentUserType;

    // FILTERS
    rmgProjectRequest.projectStatus = this.projectStatus;
    rmgProjectRequest.departmentIds = this.selectedDepartmentIds;
    rmgProjectRequest.projectFilter = this.projectFilters;
    rmgProjectRequest.expiredProjectFilter = this.expiredTNMProjectFilter;
    rmgProjectRequest.fixedCostFilter = this.fixedCostProjectFilter;

    // PAGINATION
    rmgProjectRequest.page = this.projectPage || 0;
    rmgProjectRequest.pageSize = this.projectPageSize;
    rmgProjectRequest.sortColumn = this.projectSortColumn || 'name';
    rmgProjectRequest.sortDirection = this.projectSortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.projectSortColumnType || 'string';

    return rmgProjectRequest;
  }

  loadRMGDashboard() {
    // this.getAllEmployeeGroupCount();
    this.getAllProjectStatusCount();
    this.getProjectDetailsList();
  }

  // ------------------ Reusable Gauge Config ------------------
  renderGaugeChart(chartName: any, chartId: any, value: number, maxValue: number = 36, openMod?: any) {
    Highcharts.chart(chartId, {
      credits: { enabled: false },

      chart: {
        type: 'solidgauge',
        backgroundColor: 'transparent',
        spacing: [0, 0, 0, 0] // remove extra spacing
      },

      title: {
        text: null // no title
      },

      tooltip: {
        enabled: false
      },

      pane: {
        startAngle: -90,
        endAngle: 90,
        background: [
          {
            backgroundColor: '#e5e7eb',
            innerRadius: '80%',         // thickness of arc
            outerRadius: '100%',
            shape: 'arc',
            borderWidth: 0
          }
        ]
      },

      yAxis: {
        min: 0,
        max: this.statusCards[2].value || 0,
        stops: [
          [1, '#4CAF50']
        ],

        lineWidth: 0,
        tickWidth: 0,
        tickLength: 0,
        minorTickInterval: undefined,
        gridLineWidth: 0,
        minorGridLineWidth: 0,

        labels: {
          enabled: false
        }
      },

      plotOptions: {
        solidgauge: {
          rounded: true,
          linecap: 'round',
          cursor: 'pointer',
          innerRadius: '80%',
          events: {
            click: function () {
              if (openMod) {
                openMod(chartName);
              }
            }
          },
          dataLabels: {
            useHTML: true,
            borderWidth: 0,
            color: '#020202',
            format: `<div style="text-align:center"><h6 class="fw-bold mb-1" >${value || 0}</h6></div>`
          }
        }
      },

      exporting: {
        enabled: false
      },

      series: [
        {
          name: chartName,
          type: 'solidgauge',
          data: [
            {
              y: value,
              color: '#4CAF50' // main arc color
            }
          ]
        }
      ]
    });
  }

  renderTnmExpiredChart(chartId: string, data: any[]) {

    const categories = data.map(d => d.label);
    const maxValue = Math.max(...data.map(d => d.value || 0), 1);

    Highcharts.chart(chartId, {
      chart: {
        type: 'bar',
        backgroundColor: 'transparent',
        height: 260,
        spacing: [10, 10, 10, 10]
      },

      title: { text: null },
      credits: { enabled: false },
      legend: { enabled: false },
      tooltip: { enabled: false },
      exporting: { enabled: false },

      xAxis: {
        categories: categories,
        lineWidth: 0,
        tickWidth: 0,
        labels: {
          style: {
            color: '#6b7280',
            fontSize: '12px'
          }
        }
      },

      yAxis: {
        max: maxValue,
        visible: false
      },

      plotOptions: {
        series: {
          stacking: 'normal',
          pointWidth: 15,
          borderRadius: 4,
          groupPadding: 0.2,
          dataLabels: {
            animation: true,
            enabled: true,
            align: 'right',
            inside: false,
            style: {
              color: '#000000',
              fontWeight: 'bold',
            },
            formatter: function () {
              return this.y ?? '';   // ✅ no external reference
            }
          }
        }
      },

      series: [
        {
          type: 'bar',
          data: categories.map(() => maxValue),
          color: '#e5e7eb',
          enableMouseTracking: false,
          dataLabels: {
            enabled: false
          }
        } as Highcharts.SeriesBarOptions,
        {
          type: 'bar',
          data: data.map((d, i) => ({
            y: d.value || 0,
            color: d.color || '#4f68a9'
          }))
        } as Highcharts.SeriesBarOptions
      ]
    } as Highcharts.Options);
  }


  onAction(action: string, project: any) {
    this.actionTriggered.emit({ action, project });
  }
  // Helpers End

  // Modals Start
  openAlertMessageModal(modalMessage: any) {
    this.alertMessage = modalMessage;
    if (this.alertMessageModalRef) {
      this.closeAlertMessageModal();
    }
    this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();
    }
  }
  // Modals End

  // Count & List APIs & Methods Start
  private getAllProjectStatusCount() {

    this.statusCards.forEach(status => {
      this.getCount(status.key, this.statusCards);
    });

    this.projectLifeCycleStages.forEach(status => {
      this.getCount(status.key, this.projectLifeCycleStages);
    });

    this.loadResourceCardData();
    this.loadExpiredTnmData();

  }

  async loadResourceCardData() {
    this.resourceCards.forEach(async status => {
      await this.getCount(status.key, this.resourceCards);
      if (status.key === 'UNDERBOARDED') {
        this.renderGaugeChart('Understaffed', 'understaffedChart', status.value);
      }
      if (status.key === 'OVERBOARDED') {
        this.renderGaugeChart('Overboarded', 'overboardedChart', status.value);
      }
    });
  }

  async loadExpiredTnmData() {
    const promises = this.tnmExpiredBars.map(filter =>
      this.getExpiredTNMFilterWiseProjectStatusCount('TOTAL_EXPIRED_TNM', filter.key)
    );

    await Promise.all(promises);
    this.renderTnmExpiredChart('tnmExpiredChart', this.tnmExpiredBars);
  }

  async getCount(status: any, statusList: any[]) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = status;
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getProjectStatusCount(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const statusObj of statusList) {
          if (status === statusObj.key) {
            statusObj.value = counts[statusObj.key];
          }
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  async getExpiredTNMFilterWiseProjectStatusCount(projectStatus: any, expiredTNMProjectFilter: any) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = projectStatus;
    newRmgDashboardProjectRequest.expiredProjectFilter = expiredTNMProjectFilter;

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getProjectStatusCount(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.tnmExpiredBars) {
          if (expiredTNMProjectFilter === filter.key) {
            filter.value = counts[projectStatus];
          }
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }
  // Count & List APIs & Methods End

  // Projects Table APIs & Methods Start
  onProjectSearch(searchData: any) {
    this.projectPage = 0;
    this.projectFilters = searchData;
    this.filterStateService.projectReportFilters = this.projectFilters;
    this.getProjectDetailsList();
  }

  onProjectPageSizeChange() {
    this.filterStateService.projectPageSize = this.projectPageSize;
    this.getProjectDetailsList();
  }

  sortProjectData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.projectSortColumn = sortParams[0];
      this.projectSortColumnType = sortParams[1];
      this.projectSortDirection = sort.direction;
      this.getProjectDetailsList();
    }
  }

  toggleProjectSearch(): void {
    this.isProjectSearchEnabled = !this.isProjectSearchEnabled;
    if (!this.isProjectSearchEnabled) {
      this.projectFilters = {};
      this.filterStateService.clearProjectReportFilters();
      this.getProjectDetailsList();
    }
  }

  get totalPages(): number {
    return Math.ceil(this.totalProjectsCount / this.projectPageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get startIndex(): number {
    return (this.projectPage - 1) * this.projectPageSize + 1;
  }

  get endIndex(): number {
    return Math.min(this.projectPage * this.projectPageSize, this.totalProjectsCount);
  }

  get visiblePages(): (number | string)[] {
    const total = this.totalPages;
    const current = this.projectPage;
    const pages: (number | string)[] = [];
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    pages.push(1);

    if (current > 4) {
      pages.push('...');
    }
    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 3) {
      pages.push('...');
    }
    pages.push(total);
    return pages;
  }

  goToPage(page: number | string) {
    if (page !== '...' && typeof page === 'number') {
      this.projectPage = page;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList();
    }
  }

  nextProjectPage() {
    if (this.projectPage < this.totalPages) {
      this.projectPage++;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList();
    }
  }

  prevProjectPage() {
    if (this.projectPage > 1) {
      this.projectPage--;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList();
    }
  }

  exportPageProjectDetailsToExcel(projectDetailsList: any[]): void {
    const excelName = "Project Report.xlsx";
    const exportData = projectDetailsList?.map(x => ({
      'Project Name': x.name || 'NA',
      'PO Number': x.poNo || 'NA',
      'Project Type': x.poProjectType || 'NA',
      'Project Manager': x.projectManagers && x.projectManagers.length > 0 ? x.projectManagers[0].projectManagerName : 'NA',
      'Client': x.clientName || 'NA',
      'Apmosys RM': x.apmosysRM || 'NA',
      'Client RM': x.clientRM || 'NA',
      'Start Date': this.normalizeDate(x.projectStartDate) || 'NA',
      'End Date': this.normalizeDate(x.projectEndDate) || 'NA',
      'State': x.state || 'NA',
      'Created On': this.normalizeDate(x.createdOn) || 'NA',
      'IShine Project Status': x.projectStatus || 'NA',
      'Approval Status': x.draftStatus || 'NA',
    }));
    this.exportExcelService.exportTableDataToExcel(exportData, excelName);
  }

  exportAllFilteredPageProjectDetailsToExcel() {
    let rmgProjectRequest = this.getRMGRequestObject();
    rmgProjectRequest.page = 0;
    rmgProjectRequest.pageSize = 100000;
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList?.content)) {
        const apiResponse = response?.serviceResponse?.projectList?.content || [];
        this.exportPageProjectDetailsToExcel(apiResponse);
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    },
      (error) => {
        this.openAlertMessageModal('Something went wrong!!');
      });
  }

  getProjectDetailsList() {
    this.totalProjectsCount = 0;
    this.projectDetailsList = [];
    let rmgProjectRequest = this.getRMGRequestObject();
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList?.content)) {
        const apiResponse = response?.serviceResponse?.projectList;
        this.totalProjectsCount = apiResponse?.totalElements || 0;
        this.projectDetailsList = [...apiResponse?.content];
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    },
      (error) => {
        this.openAlertMessageModal('Something went wrong!!');
      });
  }
  // Projects Table APIs & Methods End

} 
