import { Component, ElementRef, EventEmitter, OnInit, Output, TemplateRef, ViewChild,  HostListener, Input } from '@angular/core';
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
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import * as moment from 'moment';
import { first, firstValueFrom, pipe } from 'rxjs';
import { RMGDashboardProjectRequest } from 'src/app/models/rmgDashboardProjectRequest';
import * as Highcharts from 'highcharts';
import HC_more from 'highcharts/highcharts-more';
import HC_solidGauge from 'highcharts/modules/solid-gauge';
import HC_xrange from 'highcharts/modules/xrange';
import VennModule from 'highcharts/modules/venn';
import { RmgProjectTableComponent } from '../../rmg-project-table/rmg-project-table.component';

VennModule(Highcharts);
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
  allBillableProjectTypes = ['tnm', 'fixed cost', 'monitoring'];
  allNonBillableProjectTypes = ['internalrndproducts', 'bench', 'internal'];

  @Output() actionTriggered = new EventEmitter<{ action: string; project: any; }>();
  
  @ViewChild('rmgProjectTable') rmgProjectTableComponent!: RmgProjectTableComponent;
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
  projectTimesheetSummaryModalRef: NgbModalRef;

  // HTML Configs
  statusCards = [
    { label: 'TOTAL PROJECTS', value: null, icon: 'bi-globe', colorClass: 'text-info', key: 'ALL', color: '#7b8fc7', tooltip: ['Total number of projects including Active as well as Completed ones.'], display: true },
    {
      label: 'ACTIVE PROJECTS', value: null, icon: 'bi-lightning', colorClass: 'text-warning', key: 'TOTAL', color: '#6f85c0', tooltip: [
        'Currently active projects in execution phase.',
        'Projects active as of the current date.'
      ], display: true
    },
    {
      label: 'TNM PROJECTS', value: null, icon: 'bi-currency-rupee', colorClass: 'text-success', key: 'TOTAL_TNM', color: '#627bb8', tooltip: [
        'Projects classified under Time & Money-based active projects.'
      ], display: true
    },
    {
      label: 'FIXED COST', value: null, icon: 'bi-suitcase-lg', colorClass: 'text-info', key: 'TOTAL_FC', color: '#4f68a9', tooltip: [
        'Projects classified under Active projects with fixed pricing model.'
      ], display: true
    },
    {
      label: 'MONITORING', value: null, icon: 'bi-bar-chart', colorClass: 'text-navy', key: 'TOTAL_MONITORING', color: '#3f5796', tooltip: [
        'Projects currently under monitoring category.'
      ], display: true
    },
    {
      label: 'INTERNAL & BENCH', value: null, icon: 'bi-people', colorClass: 'text-success', key: 'TOTAL_INTERNAL', color: '#2f467f', tooltip: [
        'Internal initiatives and bench allocations, including projects designated for internal or bench resources.'
      ], display: true
    },
    {
      label: 'ALL TNM PROJECTS', value: null, icon: 'bi-currency-rupee', colorClass: 'text-success', key: 'ALL_TNM', color: '#627bb8', tooltip: [
        'All TNM related projects combined.',
        'Includes both active and archived TNM work.',
        'Used for consolidated reporting.'
      ], display: false
    },
  ];


  projectLifeCycleStages = [
    {
      value: null, label: 'Not Started', desc: 'Awaiting project kickoff', colorClass: 'text-muted', color: '#6c757d', icon: 'fa-solid fa-minus-circle ', key: 'NOT_STARTED', tooltip: [
        'Projects where resource onboarding has not yet commenced.',
        'Teams may be empty or not fully configured.'
      ]
    },
    {
      value: null, label: 'Pending', desc: 'Awaiting HOD / PM approval', colorClass: 'text-warning', color: '#CC8C33', icon: 'fa-solid fa-clock', key: 'PENDING_FOR_APPROVAL', tooltip: [
        'Projects where resource onboarding has started, but at least one employee is awaiting approval.',
        'Approval is required before moving forward.'
      ]
    },
    {
      value: null, label: 'Approved', desc: 'Approved by HOD / PM', colorClass: 'text-success', color: '#28a745', icon: 'fa-solid fa-check-circle', key: 'APPROVED', tooltip: [
        'Projects where all onboarded resources are approved.'
      ]
    },
    {
      value: null, label: 'Scheduled', desc: 'Planned for Future Start', colorClass: 'text-info', color: '#17a2b8', icon: 'fa-solid fa-calendar-check', key: 'SCHEDULED', tooltip: [
        'Projects where resources are onboarded with future start date.'
      ]
    },
    {
      value: null, label: 'Rejected', desc: 'Not approved by HOD / PM', colorClass: 'text-danger', color: '#dc3545', icon: 'fa-solid fa-times-circle', key: 'REJECTED', tooltip: [
        'Projects that have been rejected during the resource approval process.'
      ]
    },
  ];

  resourceCards = [
    {
      label: 'Underboarded', value: null, icon: 'bi-arrow-trend-down', colorClass: 'text-info', key: 'UNDERBOARDED', color: '#E69D23', subKey: 'UNDERBOARDED', tooltip: [
        'TNM projects where one or more role requirements are not yet fully fulfilled.'
      ], fullLabel: 'Underboarded (TNM)'
    },
    {
      label: 'Overboarded', value: null, icon: 'bi-arrow-trend-up', colorClass: 'text-danger', key: 'OVERBOARDED', color: '#DB3838', subKey: 'OVERBOARDED', tooltip: [
        'Resources are over-allocated beyond capacity.',
        'Requires workload balancing or redistribution.'
      ], fullLabel: 'Overboarded (TNM)'
    },
    {
      label: 'Deboarded', value: null, icon: 'bi-person-dash', colorClass: 'text-muted', key: 'OFFBOARDED', color: '#6B7280', tooltip: [
        'Resources have been released from active projects.',
        'No current allocation in ongoing work.'
      ], fullLabel: 'Deboarded (TNM)'
    },
  ];

  tnmExpiredBars = [
    { label: 'Expired TNM', value: null, color: '#2f467f', key: 'allExpiredTNMProjectsCount', display: false, subKey: 'TOTAL_EXPIRED_TNM', fullLabel: 'Expired (TNM)' },
    { label: 'All', value: null, color: '#2f467f', key: 'allExpiredTNMProjectsCount', display: true },
    { label: '0-1M', value: null, color: '#7b8fc7', key: 'expiredProjectsWithin1Month', display: true },
    { label: '1-2M', value: null, color: '#6f85c0', key: 'expiredProjects1To2Months', display: true },
    { label: '2-3M', value: null, color: '#627bb8', key: 'expiredProjects2To3Months', display: true },
    { label: '3-6M', value: null, color: '#4f68a9', key: 'expiredProjects3To6Months', display: true },
    { label: '6-12M', value: null, color: '#3f5796', key: 'expiredProjects6To12Months', display: true },
    { label: '12M+', value: null, color: '#2f467f', key: 'expiredProjectsAbove12Months', display: true },
  ];

  fixedCostItems = [
    { key: "all", label: 'Active', value: null, color: '#1B294B', display: false, fullLabel: 'Active (Fixed Cost)' },
    { key: "defaulter", label: 'Defaulter', value: null, color: '#A2AFCD', subKey: 'TOTAL_FC', display: true, fullLabel: 'Defaulter (Fixed Cost)' },
    { key: "ontime", label: 'On Time', value: null, color: '#4468BB', display: true, fullLabel: 'On Time (Fixed Cost)' },
  ];

  timesheetApplicableProjects = [
    { key: "tnm", label: 'TNM', value: null, color: '#7b8fc7', display: true },
    { key: "fixedCost", label: 'Fixed Cost', value: null, color: '#6f85c0', subKey: 'TOTAL_FC', display: true },
    { key: "monitoring", label: 'Monitoring', value: null, color: '#627bb8', display: true },
    { key: "internal", label: 'Internal & Bench', value: null, color: '#4f68a9', display: true }
  ];

  getTnmExpiredTooltip(): string[] {
    return [
      'TNM projects that have exceeded end date and resources are still onboarded.'
    ];
  }

  getFixedCostTooltip(): string[] {
    return [
      'Defaulter: Projects on which resources are onboarded despite po_end_date is crossed.',
      'On Time: Projects on which resoruces are onboarded and po_end_date is not crossed.',
      'Active: Sum of Defaulter and On Time.'
    ];
  }

  getTimesheetApplicableProjectTooltip(): string[] {
    return [
      'It represents the total number of Projects on which resources can fill timesheet.'
    ];
  }
  projectTypeColors = {
    TNM: '#2F467F',
    'Fixed Cost': '#355C7D',
    Monitoring: '#4f68a9',
    Internal: '#7b8fc7'
  };

  zeroTimesheetBars = [
    { label: 'No Timesheet', value: null, color: '#2f467f', key: 'All', display: false, subKey: 'TIMESHEET_NON_COMPLIANCE', fullLabel: 'No Timesheet Filled' },
    { label: '3 Months', value: null, color: '#5677C2', key: '3M', display: true, segments: { TNM: 0, 'Fixed Cost': 0, Monitoring: 0, Internal: 0 } },
    { label: '6 Months', value: null, color: '#CC9433', key: '6M', display: true, segments: { TNM: 0, 'Fixed Cost': 0, Monitoring: 0, Internal: 0 } },
    { label: '1 Year', value: null, color: '#C65353', key: '1Y', display: true,segments: { TNM: 0, 'Fixed Cost': 0, Monitoring: 0, Internal: 0 } },
  ];

  completedItems = [
    {
      key: 'COMPLETED_IN_ISHINE', label: 'iShine Closed', value: null, icon: 'bi-check-circle', color: '#6f85c0', tooltip: [
        'Projects marked as completed in iShine.'
      ]
    },
    {
      key: 'COMPLETED_IN_SHANKH', label: 'Shankh Closed', value: null, icon: 'bi-check-circle', color: '#6f85c0', tooltip: [
        'Projects completed in Shankh system.'
      ]
    },
    {
      key: 'COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE', label: 'Shankh + Active', value: null, icon: 'bi-flag', color: '#6f85c0', tooltip: [
        'Project is marked completed in Shankh.',
        'However, resources are still active on the project.',
        'Requires validation or proper deboarding of team members.'
      ]
    },
  ];

  attentionRequiredProjectAlerts = [
    this.resourceCards[0], // Underboarded
    this.resourceCards[1], // Overboarded
    this.fixedCostItems[1], // Defaulter FC
    this.tnmExpiredBars[0], // All Expired Tnm
    this.zeroTimesheetBars[0]
  ];

  insights = [
    { key: 'PENDING_FOR_APPROVAL', value: null, text: ' project(s) stuck in Pending Approval', subValue: null, subText: 'Approve or reject the team allocation to enable timesheet entry or initiate new resource allocation.', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 0, criticalThreshold: 30, valueList: this.projectLifeCycleStages },
    { key: 'UNDERBOARDED', value: null, text: ' TNM project(s) Underboarded', subValue: null, subText: 'Add required resources to meet the planned team capacity.', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 9, criticalThreshold: 30, valueList: this.resourceCards },
    { key: 'OVERBOARDED', value: null, text: ' TNM project(s) Overboarded', subValue: null, subText: 'Remove excess resources, deallocate from expired projects, or review and resolve rejected additional resource requests.', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 0, criticalThreshold: 30, valueList: this.resourceCards },
    { key: 'allExpiredTNMProjectsCount', value: null, text: ' TNM project(s) Expired', subValue: null, subText: 'Renew the project or mark it as complete, and deallocate remaining resources.', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 0, criticalThreshold: 30, valueList: this.tnmExpiredBars },
    { key: 'HIGHEST_EXPIRED_TNM_BUCKET', value: null, text: ' Highest TNM expiry: {projectCount} projects in {bucketName} bucket', subValue: null, subText: 'Largest concentration of expiring projects', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 0, criticalThreshold: 0, valueList: this.tnmExpiredBars },
    { key: 'defaulter', value: null, text: ' fixed cost defaulters', subValue: null, subText: 'Extend project milestones or mark the project as complete to resolve defaulter status.', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 30, criticalThreshold: 50, valueList: this.fixedCostItems },
    { key: 'TIMESHEET_NON_COMPLIANCE', value: null, text: ' project(s) with no timesheet filled', subValue: null, subText: 'Ensure timesheets are filled for inactive periods to maintain compliance and reporting accuracy. Or mark as complete the project as the project is complete. ', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 30, criticalThreshold: 50, valueList: this.zeroTimesheetBars },
    { key: 'COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE', value: null, text: ' completed project(s) have active teams', subValue: null, subText: 'Resources not yet released', stat: '', maxValue: null, percentage: 0.0, status: '', warningThreshold: 30, criticalThreshold: 50, valueList: this.completedItems },
  ];

  // EMPLOYEE MODAL DETAILS
  getEmployeeListByEmployeeGroupUrl: string = 'api/getEmployeeDetailsListByEmployeeGroup';
  employeeDetailsExtraParams: any = {};
  employeeDetailsDefaultSortColumn: string = '';
  employeeDetailsColumnConfig: any[] = [];
  employeeDetailsSubTableColumnConfig: any[] = [];

  getUnfilledTimesheetProjectDetailsListUrl: string = 'api/getUnfilledTimesheetProjectDetailsList';
  projectDetailsExtraParams: any = {};
  projectDetailsDefaultSortColumn: string = '';
  projectDetailsColumnConfig: any[] = [];
  projectDetailsSubTableColumnConfig: any[] = [];

  notMappedEmployeesColumConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'managerName', header: 'Manager Name', sortable: true, searchable: true }
    , { field: 'jobRoleName', header: 'Job Role', sortable: true, searchable: true }
  ];

  onBenchButProjectAssignedEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: true, searchable: true }
    , { field: 'clientRM', header: 'Client RM', sortable: true, searchable: true }
    , { field: 'poNo', header: 'PO No.', sortable: true, searchable: true }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: true, searchable: true }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: true, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: true, searchable: false }
  ];

  onBenchEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'onbenchDate', header: 'On Bench Date', sortable: true, searchable: false }
    , { field: 'dayOnbench', header: 'No Of Days On Bench', sortable: true, searchable: false }
    , { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: true, searchable: true }
    , { field: 'teamName', header: 'Team Name', sortable: true, searchable: true }
    , { field: 'employeeRole', header: 'Employee Role', sortable: true, searchable: true }
  ];

  withoutBillabilityEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'managerName', header: 'Manager Name', sortable: true, searchable: true }
    , { field: 'jobRoleName', header: 'Job Role', sortable: true, searchable: true }
  ];

  mappedToInternalEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: true, searchable: true }
    , { field: 'teamName', header: 'Team Name', sortable: true, searchable: true }
    , { field: 'employeeRole', header: 'Employee Role', sortable: true, searchable: true }
  ];

  otherEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: true, searchable: true }
    , { field: 'clientRM', header: 'Client RM', sortable: true, searchable: true }
    , { field: 'poNo', header: 'PO No.', sortable: true, searchable: true }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: true, searchable: true }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: true, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: true, searchable: false }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: true, searchable: true }
    , { field: 'teamName', header: 'Team Name', sortable: true, searchable: true }
    , { field: 'employeeRole', header: 'Employee Role', sortable: true, searchable: true }
  ];

  onBenchEmployeeDetailsSubTableColumnConfig = [
    { field: 'projectName', header: 'Project Name', sortable: false, searchable: false }
    , { field: 'clientName', header: 'Client Name', sortable: false, searchable: false }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: false, searchable: false }
    , { field: 'clientRM', header: 'Client RM', sortable: false, searchable: false }
    , { field: 'poNo', header: 'PO No.', sortable: false, searchable: false }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: false, searchable: false }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: false, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: false, searchable: false }
  ];

  mappedEmployeeDetailsSubTableColumnConfig = [
    { field: 'projectName', header: 'Project Name', sortable: false, searchable: false }
    , { field: 'clientName', header: 'Client Name', sortable: false, searchable: false }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: false, searchable: false }
    , { field: 'clientRM', header: 'Client RM', sortable: false, searchable: false }
    , { field: 'poNo', header: 'PO No.', sortable: false, searchable: false }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: false, searchable: false }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: false, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: false, searchable: false }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: false, searchable: false }
    , { field: 'teamName', header: 'Team Name', sortable: false, searchable: false }
    , { field: 'employeeRole', header: 'Employee Role', sortable: false, searchable: false }
  ];

  futureStartDateAssignedEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'teamName', header: 'Team Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: true, searchable: true }
    , { field: 'clientRM', header: 'Client RM', sortable: true, searchable: true }
    , { field: 'poNo', header: 'PO No.', sortable: true, searchable: true }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: true, searchable: true }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: true, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: true, searchable: false }
    , { field: 'etmStartDate', header: 'Employee Start Date', sortable: true, searchable: false }
    , { field: 'etmActive', header: 'Approval Status', sortable: false, searchable: false }
  ];

  unfilledTimesheetProjectColumnConfig = [
    { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'ApMSys RM', sortable: true, searchable: true }
    , { field: 'clientRM', header: 'Client RM', sortable: true, searchable: true }
    , { field: 'poNo', header: 'PO No.', sortable: true, searchable: true }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: true, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: true, searchable: false }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: true, searchable: true }
    , { field: 'teamName', header: 'Team Name', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'jobRoleName', header: 'Job Role Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'mobileNo', header: 'Mobile No.', sortable: true, searchable: true }
    , { field: 'email', header: 'Email', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'billableType', header: 'User Billable Type', sortable: true, searchable: true }
    , { field: 'effectiveStartDate', header: 'Effective Start Date', sortable: true, searchable: false }
  ];

  timesheetNonCompliance = { key: 'TIMESHEET_NON_COMPLIANCE', label: 'Timesheet Non-Compliance Projects', value: 'timesheet_non_compliance', count: null, style: 'color: #EC4899', bgstyle: 'background-color: #EC4899;color: #fff;', leftstyle: 'border-left:4px solid;color: #EC4899', i_class: 'fa-solid fa-calendar-xmark fa-beat-fade', columnConfig: this.unfilledTimesheetProjectColumnConfig, defaultSortColumn: 'projectName', subTableColumnConfig: [], color: '', infoLabel: '' };

  workforceOverview = [
    {
      key: 'TOTAL', icon: 'bi-people', label: 'Active Employees In ApMoSys', value: null, desc: 'Full organization headcount', colorClass: 'text-info', columnConfig: [], defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#1B294B', tooltip: [
        'Total active employees in ApMoSys including all categories.'
      ], display: false
    },
    {
      key: 'MAPPED_TO_PROJECT', icon: 'bi-person-check', label: 'Assigned to Projects', value: null, desc: 'Mapped to ≥ 1 project', colorClass: 'text-success', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#64B497', tooltip: [
        'Employees currently assigned to one or more active projects.'
      ], display: true
    },
    {
      key: 'NOT_MAPPED_TO_ANY_PROJECT', icon: 'bi-person-x', label: 'Unassigned Employees', value: null, desc: 'No active project mapping', colorClass: 'text-warning', columnConfig: this.notMappedEmployeesColumConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#D68585', tooltip: [
        'Employees not assigned to any active project at present.'
      ], display: true
    },
    {
      key: 'FUTURE_START_DATE', icon: 'bi-calendar', label: 'Scheduled Employees', value: null, desc: 'Mapped to Project with Future Start Date', colorClass: 'text-accent', columnConfig: this.futureStartDateAssignedEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#799ED2', tooltip: [
        'Employees Mapped to Project with Future Start Date.'
      ], display: true
    },
  ];

  projectDistribution = [
    {
      key: 'MAPPED_TO_SHANKH', icon: 'bi-briefcase', label: 'Client project Allocations', value: null, desc: 'Client projects only', colorClass: 'text-success', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.mappedEmployeeDetailsSubTableColumnConfig, bgColor: '#E7AA74', tooltip: [
        'Employees allocated only to client projects.'
      ]
    },
    {
      key: 'MAPPED_TO_INTERNAL_AND_SHANKH', icon: 'bi-arrow-left-right', label: 'Dual Allocation (Internal & Client)', value: null, desc: 'Both Internal & Client', colorClass: 'text-accent', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#799ED2', tooltip: [
        'Employees allocated to both internal and client projects.',
        'Represents dual utilization across domains.',
        'Balanced workload distribution.'
      ]
    },
    {
      key: 'MAPPED_TO_INTERNAL', icon: 'bi-building', label: 'Internal & Bench project Allocation', value: null, desc: 'Internal projects only', colorClass: 'text-info', columnConfig: this.mappedToInternalEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#64B4AF', tooltip: [
        'Employees allocated only to internal or bench projects.',
        'Used for internal capacity utilization tracking.'
      ]
    },
  ];

  riskItems = [
    {
      key: 'ON_BENCH_BUT_PROJECT_ASSIGNED', icon: 'bi-exclamation-triangle', label: 'Bench with Allocation', value: null, desc: 'On bench but assigned elsewhere', colorClass: 'text-warning', columnConfig: this.onBenchButProjectAssignedEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#DEB67C', tooltip: [
        'Employees currently on bench but still mapped to a project.',
        'Indicates allocation mismatch or inactive engagement.'
      ]
    },
    {
      key: 'ON_BENCH_FOR_MORE_THAN_30_DAYS', icon: 'bi-clock', label: 'Extended Bench', value: null, desc: 'Bench for 30+ days', colorClass: 'text-danger', columnConfig: this.onBenchEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#D68585', tooltip: [
        'Employees remaining on bench for more than 30 days.',
        'Shows extended idle time without assignment.'
      ]
    },
    {
      key: 'WITHOUT_ANY_BILLABILITY', icon: 'bi-person-x', label: 'No Billable Assignment', value: null, desc: 'No default billable type', colorClass: 'text-danger', columnConfig: this.withoutBillabilityEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#DEB67C', tooltip: [
        'Employees without any defined billable assignment.',
        'Not linked to any revenue-generating work.',
        'Needs proper assignment categorization.'
      ]
    },
  ];

  mappedToClientPercentage = { key: 'MAPPED_TO_SHANKH_PERCENTAGE', icon: 'bi-building', label: 'Internal & Bench project Allocation', value: null, desc: 'Internal projects only', colorClass: 'text-info', columnConfig: this.mappedToInternalEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig, bgColor: '#64B4AF' };

  kpis = [
    {
      label: 'ACTIVE PROJECTS', value: this.statusCards[1]?.value, change: null, period: 'vs last week', status: null, icon: 'bi-lightning', key: 'ALL', extraValue: null, tooltip: [
        'Currently active projects in execution phase.',
        'Projects active as of the current date.'
      ]
    },
    {
      label: 'EMPLOYEES ONBOARDED', value: this.workforceOverview[0]?.value, change: null, period: 'vs last week', status: null, icon: 'bi-people', key: 'TOTAL', extraValue: null, tooltip: [
        'Total employees currently mapped to active projects.',
        'Includes only onboarded (active) resources.',
        'Excludes deboarded or inactive employees.'
      ]
    },
    {
      label: '% OF EMPLOYEES ON CLIENT PROJECTS', value: null, suffix: '%', change: null, period: 'vs last month', status: null, icon: 'bi-graph-up', key: 'MAPPED_TO_SHANKH', extraValue: null, tooltip: [
        'Percentage of employees working on client projects.',
        'Calculated against total onboarded workforce.',
        'Represents billable utilization across projects.'
      ]
    },
  ];

  // Flags
  isEmployeeView: boolean = false;
  isReportTabVisible: boolean = false;
  selectedView: 'cards' | 'distribution' = 'cards';
  myDept: boolean = false;
  departmentFilterActionLabel: string = '';

  // Arrays
  selectedDepartmentIds: any[] = [];
  oldSelectedDepartmentIds: any[] = [];
  employeeList: Employee[] = [];
  filteredEmployeeList: Employee[] = [];
  departmentList: any[] = [];
  filteredDepartmentList: any[] = [];
  projectDetailsList: any[] = [];
  projectDetailsResolveMap: Record<string, { resolvedProjectViewId: string; redirected: boolean; resolvedProjectName?: string }> = {};
  /** Link-aware project name search (same contract as HR project view: serviceResponse1 / serviceResponse2). */
  rmgLinkedProjectSearchInfo: string | null = null;
  rmgLinkedPrimaryProjectIdsFromSearch: number[] = [];
  rmgLinkedSearchRowTooltipByProjectId: Record<number, string> = {};
  projectSummaryData: any[] = [];

  // Variables
  selectedEmpId: any = 0;
  attentionRequiredProjectCount: any = 0;
  selectedEmployeeStatusLabel: string = '';
  alertMessage: string = '';
  projectStatus: string = '';
  expiredTNMProjectFilter: string = '';
  fixedCostProjectFilter: string = '';
  timesheetApplicableProjectTypeFilter: string = '';
  activeEmployeesCount: number = 0;
  unassignedEmployeesCount: number = 0;
  assignedEmployeesCount: number = 0;

  // Objects

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

  async ngOnInit() {
    this.updateDepartmentLabel();
    this.projectStatus = this.filterStateService?.selectedProjectStatus ? this.filterStateService?.selectedProjectStatus : 'TOTAL';
    this.expiredTNMProjectFilter = 'allExpiredTNMProjectsCount';
    this.fixedCostProjectFilter = 'all';
    this.timesheetApplicableProjectTypeFilter = 'all';

    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();

    if (this.shouldFetchUserSpecificDepartments(deptName, empRole)) {
      await this.getDeptsByUser();
    } else {
      await this.getAllDepartmentsByCurrentUserIdAndRole();
    }

    this.setReportTabVisible();
    this.getEmployeeNameAndEmpld();
    this.mapSubFeatureFlag();

    if ((this.filterStateService.deptIdList && this.filterStateService.deptIdList.length > 0) || (this.filterStateService.deptIdListByUser && this.filterStateService.deptIdListByUser.length > 0)) {
      this.myDept = this.filterStateService.myDept;
      this.selectedDepartmentIds = this.filterStateService.deptIdList;
    }

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.projectStatus = 'ALL';
      setTimeout(() => this.scrollToTable());
    } 
    this.loadRMGDashboard();
  }

  // Department Table APIs & Methods Starts
  onDepartmentSelectionChange() {
    if (this.validationService.areArraysEqual(this.oldSelectedDepartmentIds, this.selectedDepartmentIds)) {
      return;
    }
    this.oldSelectedDepartmentIds = [...this.selectedDepartmentIds];
    this.getProjectDetailsList(true);
    this.loadRMGDashboard();
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

  async getAllDepartmentsByCurrentUserIdAndRole(): Promise<any> {
    this.selectedDepartmentIds = [];
    this.oldSelectedDepartmentIds = [];
    this.filteredDepartmentList = [];
    try {
      const response: any = await this.departmentService.getDeptsByRole(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch departments");
      }
      const serviceResponse = response.serviceResponse;
      this.filteredDepartmentList = serviceResponse?.departments || [];
      return;
    } catch (error) {
      console.error("Error fetching departments:", error);
      throw error;
    }
  }

  async getDeptsByUser(): Promise<any> {
    this.selectedDepartmentIds = [];
    this.oldSelectedDepartmentIds = [];
    this.filteredDepartmentList = [];
    try {
      const response: any = await this.departmentService.getDeptsByUser(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch user departments");
      }
      const serviceResponse = response.serviceResponse;
      this.filteredDepartmentList = serviceResponse?.departments || [];
      return;
    } catch (error) {
      console.error("Error fetching user departments:", error);
      throw error;
    }
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
    rmgProjectRequest.expiredProjectFilter = this.expiredTNMProjectFilter;
    rmgProjectRequest.fixedCostFilter = this.fixedCostProjectFilter;
    rmgProjectRequest.timesheetApplicableProjectTypeFilter = this.timesheetApplicableProjectTypeFilter;
    return rmgProjectRequest;
  }

  async loadRMGDashboard() {
    const promises = [];
    // this.getProjectDetailsList(false);
    promises.push(this.getEmployeeMappedToClientPercent());
    promises.push(this.getAllEmployeeGroupCount());
    promises.push(this.getAllProjectStatusCount());

    await Promise.all(promises);
    this.updateKpis();

    // this.intializeBillingLossProgressBar();
  }

  getPercentage(value: any = 0, total: any = 0): number {
    return total ? Math.round((value / total) * 10000) / 100 : 0;
  }

  get selectedProjectStatusLabel(): string {
    return [
      ...this.statusCards,
      ...this.projectLifeCycleStages,
      ...this.resourceCards,
      ...this.completedItems,
    ].find(item => item.key === this.projectStatus)?.label || 'Project Details';
  }

  get isValidEmployee(): boolean {
    if (!this.selectedEmpId || !this.isValidNumber(this.selectedEmpId) || !this.filteredEmployeeList?.length) {
      return false;
    }

    return this.filteredEmployeeList.some(
      emp => emp.empId === this.selectedEmpId
    );
  }

  get totalTimesheetApplicableProjects() {
    let total = 0;
    if (this.timesheetApplicableProjects) {
      this.timesheetApplicableProjects.map(type => {
        if (type && type.value) {
          total = total + type.value;
        }
      })
    }
    return total;
  }

  setReportTabVisible() {
    this.isReportTabVisible = this.currentUser?.userMapping?.some(m => m.featureName === 'Reports') ?? false;
  }

  goToReportTab() {
    this.router.navigate(['/user-reports/report-list'], {
      state: { returnUrl: this.router.url }
    });
  }

  onAction(action: string, project: any) {
    // this.filterStateService.selectedProjectStatus = this.projectStatus;
    // this.filterStateService.projectReportFilters = this.projectFilters;
    // this.filterStateService.deptIdList = this.selectedDepartmentIds;
    // this.filterStateService.myDept = this.myDept;
    this.actionTriggered.emit({ action, project });
  }

  isValidNumber(value: any): boolean {
    return typeof value === 'number' && !Number.isNaN(value);
  }

  scrollToTable() {
    document.getElementById('projectTable')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  onPieClick(point: any, chartId: any) {
    console.log(point);
    if (chartId) {
      this.projectStatus = chartId.split('_Chart')[0];
      this.filterStateService.selectedProjectStatus = this.projectStatus;
      if (this.projectStatus == 'TOTAL_FC') {
        this.fixedCostProjectFilter = this.fixedCostItems.find(t => t.label === point?.name).key;
      }
      if (this.projectStatus == 'TIMESHEET_APPLICABLE_PROJECT') {
        this.timesheetApplicableProjectTypeFilter = this.timesheetApplicableProjects.find(t => t.label === point?.name).key;
      }
      this.getProjectDetailsList(true);
    }
  }

  onBarClick(point: any, chartId: any) {
    console.log(point);
    if (chartId && point) {
      let projectStatus = chartId.split('_Chart')[0];
      // if (projectStatus == 'TIMESHEET_NON_COMPLIANCE') {
      //   let key = this.zeroTimesheetBars.find(t => t.label === point?.category).key || 'All';
      //   this.openUnfilledProjectTimesheetDetailsModal(key);
      //   return;
      // } else 
      if (projectStatus == 'TOTAL_EXPIRED_TNM') {
        this.projectStatus = projectStatus;
        this.filterStateService.selectedProjectStatus = this.projectStatus;
        this.expiredTNMProjectFilter = this.tnmExpiredBars.find(t => t.label === point?.category).key;
      }
      this.getProjectDetailsList(true);
    }
  }

  getInsightValues(key: string, data: any): number {
    return data?.find(s => s.key === key)?.value || 0;
  }

  handleInsightClick(insight: any) {
    console.log(insight);
    if (insight.key == 'TIMESHEET_NON_COMPLIANCE') {
      return;
    }

    this.projectStatus = insight.key;
    this.filterStateService.selectedProjectStatus = insight.key;
    if (insight.key == 'allExpiredTNMProjectsCount') {
      this.projectStatus = 'TOTAL_EXPIRED_TNM';
      this.filterStateService.selectedProjectStatus = 'TOTAL_EXPIRED_TNM';
      this.expiredTNMProjectFilter = 'allExpiredTNMProjectsCount';
    }
    else if (insight.key == 'HIGHEST_EXPIRED_TNM_BUCKET') {
      const filteredList = insight.valueList
        ?.filter(t => !['allExpiredTNMProjectsCount', 'TOTAL_EXPIRED_TNM'].includes(t.key));

      const maxItem = filteredList?.length
        ? filteredList.reduce((prev, curr) =>
          (curr.value > prev.value ? curr : prev)
        ) : null;

      this.projectStatus = 'TOTAL_EXPIRED_TNM'
      this.expiredTNMProjectFilter = maxItem?.key || 'allExpiredTNMProjectsCount';
    }
    else if (insight.key == 'defaulter') {
      this.projectStatus = 'TOTAL_FC'
      this.fixedCostProjectFilter = 'defaulter';
    }
    this.getProjectDetailsList(true);
  }

  get attentionRequiredProjectCountPercent() {
    return this.getPercentage(this.attentionRequiredProjectCount, this.statusCards[1]?.value) || 0;
  }

  toggleView(view: 'cards' | 'distribution') {
    this.selectedView = view;
    if (view === 'cards') {
      this.loadCards();
    } else {
      setTimeout(() => {
        this.renderVennChart(this.projectDistribution);
      }, 0);
    }
  }

  loadCards() {
    // console.log('Cards clicked');
  }


  async filterDepartment() {
    this.myDept = !this.myDept;
    this.filterStateService.myDept = this.myDept;
    this.updateDepartmentLabel();
    if (this.myDept) {
      await this.getDeptsByUser();
    } else {
      await this.getAllDepartmentsByCurrentUserIdAndRole();
    }
  }

  updateDepartmentLabel() {
    this.departmentFilterActionLabel = this.myDept
      ? 'Display All Department'
      : 'Display My Department';
  }

  private shouldFetchUserSpecificDepartments(deptName: string, empRole: string): boolean {
    const depts = ["Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR"];
    const roles = ["SuperAdmin", "Accounts"];
    return (!depts.some(dept => deptName.includes(dept)) &&
      !roles.some(role => empRole.includes(role)));
  }

  updateKpis() {
    const statusMap = Object.fromEntries(this.statusCards.map(s => [s.key, s.value]));
    const workforceMap = Object.fromEntries(this.workforceOverview.map(w => [w.key, w.value]));

    const totalEmployees = Number(workforceMap['TOTAL']) || 0;
    const percentage = Number(this.mappedToClientPercentage) || 0;

    const mappedCount = totalEmployees
      ? Math.round((percentage / 100) * totalEmployees)
      : 0;

    this.kpis = this.kpis.map(kpi => {
      switch (kpi.key) {
        case 'ALL':
          return { ...kpi, value: statusMap['TOTAL'] };

        case 'TOTAL':
          return { ...kpi, value: workforceMap['TOTAL'] };

        case 'MAPPED_TO_SHANKH':
          return { ...kpi, value: this.mappedToClientPercentage ? this.mappedToClientPercentage : 0.00, extraValue: mappedCount ? mappedCount : 0 };

        default:
          return kpi;
      }
    });
  }
  // Helpers End

  // Charts Start
  renderGaugeChart(chartName: any, chartId: any, value: number, arcColor: any, openMod?: any) {
    const el = document.getElementById(chartId);
    if (!el) {
      // Chart container is not present in DOM (e.g. card removed from template).
      // Skip rendering to avoid breaking downstream dashboard initialization.
      return;
    }
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
          [1, arcColor]
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

  renderBarChart(chartId: string, data: any[], chartHeight: any = 180) {

    const categories = data?.filter(d => d.display)?.map(d => d.label);
    const maxValue = Math.max(...data?.filter(d => d.display)?.map(d => d.value || 0), 1);

    Highcharts.chart(chartId, {
      chart: {
        type: 'bar',
        backgroundColor: 'transparent',
        height: chartHeight,
        spacing: [10, 50, 10, 10]
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
          grouping: false,
          pointWidth: 14,
          borderRadius: 6,
          point: {
            cursor: 'pointer',
            events: {
              click: (event) => {
                const point = event.point;
                this.onBarClick(point, chartId);
              }
            }
          }
        }
      },
      series: [
        {
          type: 'bar',
          data: data?.filter(d => d.display)?.map(d => ({
            y: maxValue,
            actualValue: d.value || 0
          })),
          color: '#e5e7eb',
          enableMouseTracking: false,
          dataLabels: {
            enabled: true,
            useHTML: true,
            align: 'right',
            alignTo: 'plotEdges',
            crop: false,
            overflow: 'allow',
            x: 30,
            formatter: function () {
              const point = this.point as any;
              return `<div style="width: 40px;text-align: right;font-weight: 600;color: #111827;">
                        ${point.actualValue}
                      </div>`;
            }
          }
        } as Highcharts.SeriesBarOptions,
        {
          type: 'bar',
          data: data?.filter(d => d.display)?.map(d => ({
            y: d.value || 0,
            color: d.color || '#4f68a9'
          }))
        } as Highcharts.SeriesBarOptions
      ]
    } as Highcharts.Options);
  }

  renderColumnChart(chartName: any, chartId: any, chartData: any, categories: any, yAxisTitle: string) {
    Highcharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      xAxis: {
        categories: categories,
        title: {
          text: 'Projects'
        },
        labels: {
          rotation: -45, // Rotate labels to prevent overlap
          style: {
            fontSize: '11px',
            fontFamily: 'Verdana, sans-serif'
          }
        }
      },
      yAxis: {
        min: 0,
        title: {
          text: yAxisTitle,
          align: 'high'
        },
        labels: {
          overflow: 'justify'
        }
      },
      tooltip: {
        valueSuffix: ' timesheets'
      },
      plotOptions: {
        column: {
          dataLabels: {
            enabled: true,
            format: '{y}',
            style: {
              fontSize: '10px',
            }
          }
        }
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: false
      },
      series: chartData
    });
  }

  renderPieChart(chartId: string, tempData: any[], chartHeight: any = 200) {
    const data = tempData?.filter(d => d.display);
    Highcharts.chart(chartId, {
      chart: {
        type: 'pie',
        backgroundColor: 'transparent',
        height: chartHeight,
        spacing: [10, 10, 10, 10]
      },

      title: { text: null },
      credits: { enabled: false },
      exporting: { enabled: false },
      tooltip: { enabled: false },

      legend: {
        enabled: true,
        align: 'right',
        verticalAlign: 'middle',
        layout: 'vertical',
        useHTML: true,
        itemMarginTop: 6,
        itemMarginBottom: 6,
        labelFormatter: function () {
          const point = this as Highcharts.Point;
          return `<div style="display:flex; justify-content:space-between; width:120px;">
                    <span>${point.name}</span>
                    <span><b>${point.y}</b></span>
                  </div>`;
        }
      },

      plotOptions: {
        pie: {
          borderWidth: 0,
          showInLegend: true,
          dataLabels: {
            enabled: false
          }
        },
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: (event) => {
                const point = event.point;
                this.onPieClick(point, chartId);
              }
            }
          }
        }
      },

      series: [
        {
          type: 'pie',
          data: data.map(d => ({
            name: d.label,
            y: d.value || 0,
            color:
            d.label === 'On Time'
              ? '#7B8FC7'
              : d.label === 'Defaulter'
              ? '#2F467F'
              : (d.color || '#4f68a9')
              }))
        }
      ]
    } as Highcharts.Options);
  }

  renderVennChart(data: any[], chartHeight: number = 300) {

    const internalTotal = data.find(d => d.key === 'MAPPED_TO_INTERNAL')?.value || 0;
    const shankhTotal = data.find(d => d.key === 'MAPPED_TO_SHANKH')?.value || 0;
    const overlap = data.find(d => d.key === 'MAPPED_TO_INTERNAL_AND_SHANKH')?.value || 0;

    const scale = (val: number) => Math.log(val + 1);
    Highcharts.chart({
      chart: {
        renderTo: 'distributionChart',
        type: 'venn',
        height: chartHeight,
        backgroundColor: 'transparent'
      },

      title: {
        text: 'DISTRIBUTION OVERVIEW',
        style: {
          fontSize: '14px',
          fontWeight: '400',
          letterSpacing: '1px',
          color: '#6b7280'
        }
      },

      credits: { enabled: false },
      exporting: { enabled: false },
      tooltip: { enabled: false },

      series: [{
        type: 'venn',
        name: 'Distribution',
        data: [
          {
            sets: ['Internal'],
            value: scale(internalTotal),
            name: 'Internal(A)',
            color: '#D8DBE0',
            borderColor: '#1B294B',
            custom: { actual: internalTotal }
          },
          {
            sets: ['Shankh'],
            value: scale(shankhTotal),
            name: 'Shankh(B)',
            color: '#F7EEE6',
            borderColor: '#E7AA74',
            custom: { actual: shankhTotal }
          },
          {
            sets: ['Internal', 'Shankh'],
            value: scale(overlap),
            name: 'A ∩ B',
            color: '#DDDEE1',
            borderColor: '#799ED2',
            custom: { actual: overlap }
          }
        ],
        borderWidth: 1.5,
        dataLabels: {
          enabled: true,
          useHTML: true,
          formatter: function () {
            const point: any = this.point;
            return `
            <div style="text-align:center;">
              <div style="font-size:18px;font-weight:400;color:#374151;">
                ${point.options.custom.actual}
              </div>
              <div style="font-size:11px;color:#6B7280;">
                ${point.name}
              </div>
            </div>
          `;
          }
        }
      }]
    });
  }
  // Charts End

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

  openEmployeeDetailsModal(employeeGroup: any) {
    if (employeeGroup.key == this.workforceOverview[0]?.key) {
      return;
    }
    this.selectedEmployeeStatusLabel = employeeGroup.label || 'Employee Information';
    this.employeeDetailsExtraParams = { "employeeGroupKey": employeeGroup?.key, "selectedDeptIds": this.selectedDepartmentIds, "projectStatus": this.projectStatus || 'ALL', "expiredProjectFilter": this.expiredTNMProjectFilter, "fixedCostFilter": this.fixedCostProjectFilter };
    this.employeeDetailsColumnConfig = employeeGroup?.columnConfig;
    this.employeeDetailsDefaultSortColumn = employeeGroup?.defaultSortColumn;
    this.employeeDetailsSubTableColumnConfig = employeeGroup?.columnConfig
    this.employeeDetailsModalRef = this.modalService.open(this.employeeDetailsTemplateRef, { modalDialogClass: 'modal-xl' });
  }

  closeEmployeeDetailsModal() {
    if (this.employeeDetailsModalRef) {
      this.employeeDetailsModalRef?.close();
    }
  }

  onTimesheetSegmentClick(
    bucketKey: any,
    projectType: any
  ) {
  
    this.openUnfilledProjectTimesheetDetailsModal(
      bucketKey,
      projectType
    );
  }
  

  openUnfilledProjectTimesheetDetailsModal(filter: any, projectType: any) {
    const today = moment();
    let fromDate: any;
    const value = parseInt(filter, 10);

    if (filter.includes('Y')) {
      fromDate = today.clone().subtract(value, 'years');
    } else if (filter.includes('M')) {
      fromDate = today.clone().subtract(value, 'months');
    } else {
      fromDate = today.clone().subtract(3, 'months');
    }

    fromDate = filter == 'All' ? null : fromDate.format('YYYY-MM-DD');
    let toDate = filter == 'All' ? null : today.format('YYYY-MM-DD');

    this.projectDetailsExtraParams = { "selectedDeptIds": this.selectedDepartmentIds, "projectStatus": this.timesheetNonCompliance.key, "fromDate": fromDate, "toDate": toDate,  projectType: projectType || null };
    this.projectDetailsColumnConfig = this.timesheetNonCompliance?.columnConfig;
    this.projectDetailsDefaultSortColumn = this.timesheetNonCompliance?.defaultSortColumn;
    this.projectDetailsSubTableColumnConfig = this.timesheetNonCompliance?.columnConfig
    this.timesheetNonComplianceProjectDetailsModalRef = this.modalService.open(this.timesheetNonComplianceProjectDetailsTemplateRef, { modalDialogClass: 'modal-xl' });
  }

  closeUnfilledProjectTimesheetDetailsModal() {
    if (this.timesheetNonComplianceProjectDetailsModalRef) {
      this.timesheetNonComplianceProjectDetailsModalRef?.close();
    }
  }
  // Modals End

  // Project Timesheet Summary Modal Start
  openProjectTimesheetSummaryModal(selectedEmpId: any) {
    if (!selectedEmpId || selectedEmpId == undefined || selectedEmpId == null || !this.isValidNumber(selectedEmpId)) {
      this.openAlertMessageModal("Kindly Select a Valid Employee!!");
      return;
    }
    if (!this.filteredEmployeeList.some(emp => emp.empId === selectedEmpId)) {
      this.openAlertMessageModal("Kindly Select a Valid Employee!!");
      return;
    }
    this.selectedEmpId = selectedEmpId;
    this.getProjectTimesheetSummaryData();
    this.projectTimesheetSummaryModalRef = this.modalService.open(this.projectTimesheetSummaryTemplateRef, { modalDialogClass: 'modal-lg' });
  }

  closeProjectTimesheetSummaryModal() {
    if (this.projectTimesheetSummaryModalRef) {
      this.projectTimesheetSummaryModalRef?.close();
    }
  }

  getProjectTimesheetSummaryData() {
    if (!this.selectedEmpId) {
      this.openAlertMessageModal("Cannot fetch summary. User information is missing.");
      if (this.projectTimesheetSummaryModalRef) {
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Could not load data: User not identified.</p>';
      }
      return;
    }
    const resourceManagementDTO = { empId: this.selectedEmpId };
    this.resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.projectSummaryData = response.serviceResponse;
          if (this.projectSummaryData && this.projectSummaryData.length > 0) {
            this.processProjectSummaryData(this.projectSummaryData);
          } else {
            document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center">No timesheet summary data found for your projects.</p>';
          }
        } else {
          // this.openAlertMod(this.alertTemplate, "Failed to load project summary data.");
          console.error(response.serviceResponse);
          document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Error:No timesheets filled till date</p>';
        }
      },
      error: (err) => {
        console.error(err);
        this.openAlertMessageModal("An error occurred while fetching summary data.");
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">A server error occurred. Please try again later.</p>';
      }
    });
  }

  processProjectSummaryData(summaryData: any[]) {
    const categories = [];
    const seriesData = [];
    const sortedData = summaryData
      .sort((a, b) => b.totalTimesheetsFilled - a.totalTimesheetsFilled).slice(0, 20);

    sortedData.forEach(item => {
      categories.push(item.projectName);
      seriesData.push(item.totalTimesheetsFilled);
    });

    const chartData = [{
      name: 'Timesheets Filled',
      data: seriesData,
      color: '#0275d8' // A bootstrap primary-like color
    }];

    this.renderColumnChart(
      'Top 20 Projects by Timesheets Filled',
      'projectTimesheetSummaryChart',
      chartData,
      categories
      , 'Number of Timesheets Filled'
    );
  }
  // Project Timesheet Summary Modal End

  // Count & List APIs & Methods Start
  async getAllProjectStatusCount() {
    const promises = [];
    this.statusCards.forEach(status => {
      promises.push(this.getCount(status.key, this.statusCards));
    });

    this.projectLifeCycleStages.forEach(status => {
      promises.push(this.getCount(status.key, this.projectLifeCycleStages));
    });

    this.completedItems.forEach(status => {
      promises.push(this.getCount(status.key, this.completedItems));
    });

    promises.push(this.loadResourceCardData());
    promises.push(this.loadExpiredTnmData());
    promises.push(this.loadFixedCostData());
    promises.push(this.loadTimesheetApplicableProjectData());
    promises.push(this.loadZeroTimesheetData());

    await Promise.all(promises);
    this.insights = this.processInsights(this.insights);
  }

  async loadResourceCardData() {
    const promises = this.resourceCards.map(async (status) => {
      await this.getCount(status.key, this.resourceCards);
      this.renderGaugeChart(status.label, status.key + '_Chart', status.value, status.color);
    });

    await Promise.all(promises);
  }

  async loadExpiredTnmData() {
    await this.getExpiredTNMFilterWiseProjectStatusCount();
    this.renderBarChart('TOTAL_EXPIRED_TNM_Chart', this.tnmExpiredBars);
  }

  async loadZeroTimesheetData() {
    await this.getUnFilledTimesheetProjectStatusCount();
    this.renderZeroTimesheetSegmentedChart('TIMESHEET_NON_COMPLIANCE_Chart', this.zeroTimesheetBars, 120);
  }

  async loadFixedCostData() {
    await this.getFCFilterWiseProjectStatusCount();
    this.renderPieChart('TOTAL_FC_Chart', this.fixedCostItems, 120);
  }

   async loadTimesheetApplicableProjectData() {
    await this.getTimesheetApplicableProjectData();
    this.renderPieChart('TIMESHEET_APPLICABLE_PROJECT_Chart', this.timesheetApplicableProjects, 140);
  }

  async getCount(status: any, statusList: any[]) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
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

  async getExpiredTNMFilterWiseProjectStatusCount() {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    newRmgDashboardProjectRequest.projectStatus = 'TOTAL_EXPIRED_TNM';

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getExpiredTNMFilterWiseProjectStatusCount(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.tnmExpiredBars) {
          filter.value = counts[filter.key];
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  private getAllEmployeeGroupCount() {
    this.workforceOverview.forEach(group => {
      this.getEmployeeCountByEmployeeGroup(group.key, this.workforceOverview);
    });

    this.projectDistribution.forEach(group => {
      this.getEmployeeCountByEmployeeGroup(group.key, this.projectDistribution);
    });

    this.riskItems.forEach(group => {
      this.getEmployeeCountByEmployeeGroup(group.key, this.riskItems);
    });
  }

  async getEmployeeCountByEmployeeGroup(key: any, statusList: any[]) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    newRmgDashboardProjectRequest.employeeGroupKey = key;

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getEmployeeCountByEmployeeGroup(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const group of statusList) {
          if (key === group.key) {
            group.value = counts[group.key];
          }
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  onProjectStatusSelect(status: string, projectStatusList: any, skipCountUpdate: boolean = false) {
    this.projectStatus = status;
    this.filterStateService.selectedProjectStatus = status;
    if (this.projectStatus === 'TOTAL_EXPIRED_TNM') {
      if (!this.expiredTNMProjectFilter) {
        this.expiredTNMProjectFilter = 'allExpiredTNMProjectsCount';
      }
    }
    else if (this.projectStatus == 'TOTAL_FC') {
      this.fixedCostProjectFilter = 'all';
    }
    else if (this.projectStatus == 'TIMESHEET_APPLICABLE_PROJECT') {
      this.timesheetApplicableProjectTypeFilter = 'all';
    }

    if (skipCountUpdate) {
      this.fixedCostProjectFilter = 'defaulter';
    } else {
      this.getProjectStatusCount(this.projectStatus, projectStatusList);
    }
    this.getAllEmployeeGroupCount();
    this.getProjectDetailsList(true);
  }

  getProjectStatusCount(projectStatus: any, projectStatusList: any[]) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    this.resourceManagementService.getProjectStatusCount(newRmgDashboardProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const project of projectStatusList) {
          if (projectStatus === project.key) {
            project.count = counts[project.key];
          }
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    },
      (error) => {
        this.openAlertMessageModal('Something went wrong!!');
      });
  }

  async getUnFilledTimesheetProjectStatusCount(unFilledProjectTimesheetFilter?: any) {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    newRmgDashboardProjectRequest.projectStatus = 'TIMESHEET_NON_COMPLIANCE';

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getAllUnfilledTimesheetProjectDetailsCount(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        // for (const filter of this.zeroTimesheetBars) {
        //   filter.value = counts[filter.key];
        // }
        for (const filter of this.zeroTimesheetBars) {

          // if (!filter.display) {
          //   continue;
          // }
  
          const bucketData =
            counts[filter.key] || {};
  
          filter.segments = {
  
            TNM:
              bucketData?.TNM || 0,
  
            'Fixed Cost':
              bucketData?.['Fixed Cost'] || 0,
  
            Monitoring:
              bucketData?.Monitoring || 0,
  
            Internal:
              bucketData?.Internal || 0
          };
  
          filter.value =
  
            (bucketData?.TNM || 0) +
  
            (bucketData?.['Fixed Cost'] || 0) +
  
            (bucketData?.Monitoring || 0) +
  
            (bucketData?.Internal || 0);
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  async getTimesheetApplicableProjectData() {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    newRmgDashboardProjectRequest.projectStatus = 'TIMESHEET_APPLICABLE_PROJECT';
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getTimesheetApplicableProjectData(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.timesheetApplicableProjects) {
          filter.value = counts[filter.key];
          continue;
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  async getFCFilterWiseProjectStatusCount() {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    newRmgDashboardProjectRequest.projectStatus = 'TOTAL_FC';
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getFCFilterWiseProjectStatusCount(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.fixedCostItems) {
          filter.value = counts[filter.key];
          continue;
        }
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  processInsights(insights: any) {
    let status: 'ok' | 'warning' | 'critical' = 'ok';

    return insights.map(insight => {

      let value = 0;
      let total = 0;
      let bucketName = '';
      let text = insight.text || '';
      let subText = insight.subText || '';

      if (insight.valueList?.length) {
        if (insight.key === 'allExpiredTNMProjectsCount') {
          value = this.getInsightValues('allExpiredTNMProjectsCount', insight.valueList);
          total = this.getInsightValues('ALL_TNM', this.statusCards);

          let subItemValue = this.getInsightValues('expiredProjectsAbove12Months', insight.valueList);
          subText = subText
            ?.replace('{projectCount}', subItemValue?.toString())
            ?.replace('{bucketName}', '12M+');
        }
        else if (insight.key === 'HIGHEST_EXPIRED_TNM_BUCKET') {
          const filteredList = insight.valueList
            ?.filter(t => !['allExpiredTNMProjectsCount', 'TOTAL_EXPIRED_TNM'].includes(t.key));

          const maxItem = filteredList?.length
            ? filteredList.reduce((prev, curr) =>
              (curr.value > prev.value ? curr : prev)
            ) : null;

          value = maxItem?.value || 0;
          total = this.getInsightValues('ALL_TNM', this.statusCards);
          bucketName = maxItem?.label || '';

          text = text
            ?.replace('{projectCount}', value?.toString())
            ?.replace('{bucketName}', bucketName);
        }
        else if (insight.key === 'defaulter') {
          value = this.getInsightValues('defaulter', insight.valueList);
          total = this.getInsightValues('TOTAL_FC', this.statusCards);
          let tempValue = this.getInsightValues('ontime', insight.valueList);

          subText = subText
            ?.replace('{projectCount}', tempValue)
            ?.replace('{totalProjectCount}', total?.toString());;
        }
        else if (insight.key === 'TIMESHEET_NON_COMPLIANCE') {
          value = this.getInsightValues('TIMESHEET_NON_COMPLIANCE', insight.valueList);
          total = this.getInsightValues('TOTAL', this.statusCards);
        }
        else if (insight.key === 'PENDING_FOR_APPROVAL') {
          value = this.getInsightValues('PENDING_FOR_APPROVAL', insight.valueList);
          total = this.getInsightValues('TOTAL', this.statusCards);
        }
        else {
          const item = insight.valueList.find(i => i.key === insight.key);
          value = item?.value || 0;
          total = this.getInsightValues('TOTAL_TNM', this.statusCards);
        }
      }
      const percentage = this.getPercentage(value, total);

      if (percentage >= insight.criticalThreshold) {
        status = 'critical';
      } else if (percentage >= insight.warningThreshold) {
        status = 'warning';
      }

      const stat = total ? `${value}/${total}` : `${value}`;

      subText = subText
        ?.replace('{projectCount}', value?.toString())
        ?.replace('{totalProjectCount}', total?.toString())
        ?.replace('{bucketName}', bucketName);

      return { ...insight, value, maxValue: total, percentage, status, stat, subText, text };
    });
  }

  async intializeBillingLossProgressBar() {
    let newRmgDashboardProjectRequest = this.getRMGRequestObject();
    newRmgDashboardProjectRequest.projectFilter = null;
    let subKeyKeyMap = new Map<string, string>();

    this.attentionRequiredProjectAlerts?.forEach(item => {
      const subKey = item?.subKey || item.key;
      subKeyKeyMap.set(subKey, item?.key);
    });
    newRmgDashboardProjectRequest.subKeyKeyMap = Object.fromEntries(subKeyKeyMap);

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getBillingLossRiskScore(newRmgDashboardProjectRequest));
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        this.attentionRequiredProjectCount = response?.serviceResponse?.length || 0;
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  async getEmployeeMappedToClientPercent() {
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getEmployeeMappedToClientPercent());
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        this.mappedToClientPercentage = response.serviceResponse || 0.00;
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }
  // Count & List APIs & Methods End

  // Projects Table APIs & Methods Start
  getProjectDetailsList(scrollToBottom: any) {
    // if (this.rmgProjectTableComponent) {
    //   this.rmgProjectTableComponent?.refreshProjectData();
    // }

    if (scrollToBottom) {
      setTimeout(() => this.scrollToTable());
    }
  }
  

  renderZeroTimesheetSegmentedChart(
    chartId: string,
    data: any[],
    chartHeight: any = 180
  ) {
  
    const displayData =
      data.filter(d => d.display);
  
    const categories =
      displayData.map(d => d.label);
  
    // =========================================
    // TOTALS
    // =========================================
    const totals = displayData.map(d =>
  
      (d.segments?.TNM ?? 0) +
  
      (d.segments?.['Fixed Cost'] ?? 0) +
  
      (d.segments?.Monitoring ?? 0) +
  
      (d.segments?.Internal ?? 0)
    );
  
    const maxValue =
      Math.max(...totals, 1);
  
    Highcharts.chart(chartId, {
  
      chart: {
  
        type: 'bar',
  
        backgroundColor: 'transparent',
  
        height: chartHeight,
  
        marginRight: 45,
  
        spacing: [8, 8, 0, 0]
      },
  
      title: {
        text: null
      },
  
      credits: {
        enabled: false
      },
  
      exporting: {
        enabled: false
      },
  
      legend: {
  
        enabled: true,
  
        align: 'center',
  
        verticalAlign: 'bottom',
  
        symbolRadius: 2,
  
        itemDistance: 12,
  
        itemStyle: {
  
          color: '#64748B',
  
          fontSize: '10px',
  
          fontWeight: '500'
        }
      },
  
      xAxis: {
  
        categories: categories,
  
        lineWidth: 0,
  
        tickWidth: 0,
  
        gridLineWidth: 0,
  
        labels: {
  
          style: {
  
            color: '#475569',
  
            fontSize: '11px',
  
            fontWeight: '600'
          }
        }
      },
  
      yAxis: {
  
        min: 0,
  
        max: maxValue,
  
        visible: false,
  
        title: {
          text: null
        }
  
      } as Highcharts.YAxisOptions,
  
      tooltip: {
  
        useHTML: true,
  
        backgroundColor: '#0F172A',
  
        borderWidth: 0,
  
        borderRadius: 8,
  
        shadow: false,
  
        style: {
          color: '#ffffff'
        },
  
        formatter: function () {
  
          const point: any =
            this.point;
  
          return `
            <div style="padding:4px 6px;">
  
              <div
                style="
                  font-size:12px;
                  font-weight:600;
                  margin-bottom:2px;
                "
              >
                ${point.projectType}
              </div>
  
              <div style="font-size:11px;">
                ${point.y} Projects
              </div>
  
            </div>
          `;
        }
      },
  
      plotOptions: {
  
        series: {
  
          stacking: 'normal',
  
          grouping: false,
  
          borderWidth: 0,
  
          pointWidth: 14,
  
          groupPadding: 0.22,
  
          animation: {
            duration: 800
          },
  
          states: {
  
            hover: {
              brightness: 0.08
            }
          },
  
          cursor: 'pointer',
  
          dataLabels: {
            enabled: false
          },
  
          point: {
  
            events: {
  
              click: (event) => {
  
                const point: any =
                  event.point;
  
                this.onTimesheetSegmentClick(
                  point.bucketKey,
                  point.projectType
                );
              }
            }
          }
        }
      },
  
      series: [
  
        // =========================================
        // BACKGROUND TRACK
        // =========================================
        {
          type: 'bar',

          showInLegend: false,

          data: totals.map(total => ({
  
            y: maxValue,
  
            actualValue: total
          })),
  
          color: '#E2E8F0',
  
          grouping: false,
  
          pointWidth: 14,
  
          borderRadius: 0,
  
          enableMouseTracking: false,
  
          dataLabels: {
  
            enabled: true,
  
            useHTML: true,
  
            align: 'right',
  
            alignTo: 'plotEdges',
  
            crop: false,
  
            overflow: 'allow',
  
            x: 28,
  
            formatter: function () {
  
              const point: any =
                this.point;
  
              return `
                <div
                  style="
                    width:40px;
                    text-align:right;
                    font-weight:600;
                    font-size:11px;
                    color:#0F172A;
                  "
                >
                  ${point.actualValue}
                </div>
              `;
            }
          }
        } as Highcharts.SeriesBarOptions,
  
  
  
        // =========================================
        // TNM
        // =========================================
        {
          type: 'bar',
  
          name: 'TNM',
  
          color: this.projectTypeColors.TNM,
  
          borderRadius: 0,
  
          data: displayData.map(bucket => ({
  
            y: bucket.segments.TNM,
  
            bucketKey: bucket.key,
  
            projectType: 'TNM'
          }))
        } as Highcharts.SeriesBarOptions,
  
  
  
        // =========================================
        // FIXED COST
        // =========================================
        {
          type: 'bar',
  
          name: 'Fixed Cost',
  
          color: this.projectTypeColors['Fixed Cost'],
  
          borderRadius: 0,
  
          data: displayData.map(bucket => ({
  
            y: bucket.segments['Fixed Cost'],
  
            bucketKey: bucket.key,
  
            projectType: 'Fixed Cost'
          }))
        } as Highcharts.SeriesBarOptions,
  
  
  
        // =========================================
        // MONITORING
        // =========================================
        {
          type: 'bar',
  
          name: 'Monitoring',
  
          color: this.projectTypeColors.Monitoring,
  
          borderRadius: 0,
  
          data: displayData.map(bucket => ({
  
            y: bucket.segments.Monitoring,
  
            bucketKey: bucket.key,
  
            projectType: 'Monitoring'
          }))
        } as Highcharts.SeriesBarOptions,
  
  
  
        // =========================================
        // INTERNAL
        // =========================================
        {
          type: 'bar',
  
          name: 'Internal',
  
          color: this.projectTypeColors.Internal,
  
          borderRadius: 0,
  
          data: displayData.map(bucket => ({
  
            y: bucket.segments.Internal,
  
            bucketKey: bucket.key,
  
            projectType: 'Internal'
          }))
        } as Highcharts.SeriesBarOptions
      ]
  
    } as Highcharts.Options);
  }
}
