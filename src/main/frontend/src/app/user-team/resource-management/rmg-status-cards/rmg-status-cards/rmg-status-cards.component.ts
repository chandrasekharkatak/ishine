import { ViewportScroller } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, Output, Renderer2, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { first } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { RMGDashboardProjectRequest } from 'src/app/models/rmgDashboardProjectRequest';
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

@Component({
  standalone: false,
  selector: 'app-rmg-status-cards',
  templateUrl: './rmg-status-cards.component.html',
  styleUrl: './rmg-status-cards.component.css'
})

export class RmgStatusCardsComponent {

  @Input() selectedDepartmentIds: any[] = [];
  @Output() actionTriggered = new EventEmitter<{ action: string; project: any; }>();

  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
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

  unfilledTimesheetProjectColumnConfig = [
    { field: 'projectName', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client Name', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: true, searchable: true }
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

  PROJECT_STATUS = {
    ALL: { key: 'ALL', label: 'All Projects', value: 'all', count: null, style: '', leftstyle: 'border-left:4px solid;color: #053885', i_class: 'fa-solid fa-plus-circle fa-beat-fade', type: 'All', color: '' },
    TOTAL: { key: 'TOTAL', label: 'Total Projects', value: 'total_projects', count: null, style: 'color: #03A9F4', leftstyle: 'border-left:4px solid;color: #03A9F4', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type: 'All', color: '' },
    TOTAL_FC: { key: 'TOTAL_FC', label: 'Total Fixed Cost', value: 'total_fixed_cost', count: null, style: 'color: #45556C', leftstyle: 'border-left:4px solid;color: #45556C', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type: 'Fixed Cost', color: '' },
    TOTAL_FILTER_FC: { key: 'TOTAL_FC', label: 'Fixed Cost Projects', value: 'total_fixed_cost', count: null, style: 'color: #0e6dcd', bgstyle: 'background-color: #0e6dcd;color: #fff;', leftstyle: 'border-left:4px solid;color: #0e6dcd', i_class: 'fa-solid fa-chart-simple fa-beat-fade', color: '' },
    TOTAL_TNM: { key: 'TOTAL_TNM', label: 'Total TNM', value: 'total_tnm', count: null, style: 'color: #AD46FF', leftstyle: 'border-left:4px solid;color: #AD46FF', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type: 'TNM', color: '' },
    TOTAL_ACTIVE_TNM: { key: 'TOTAL_ACTIVE_TNM', label: 'TNM Active', value: 'active_tnm', count: null, style: 'color: #107503', leftstyle: 'border-left:4px solid;color: #107503', i_class: 'fa-solid fa-thumbs-up fa-beat-fade', color: '' },
    TOTAL_EXPIRED_TNM: { key: 'TOTAL_EXPIRED_TNM', label: 'All Expired TNM Projects', value: 'expired_tnm', count: null, style: 'color: #8B0000', bgstyle: 'background-color: #8B0000;color: #fff;', leftstyle: 'color: #8B0000; border-left: 4px solid', i_class: 'fa-solid fa-chart-simple fa-beat-fade', color: '' },
    TOTAL_MONITORING: { key: 'TOTAL_MONITORING', label: 'Total Monitoring', value: 'total_monitoring', count: null, style: 'color: #FE9A37', leftstyle: 'border-left:4px solid;color: #FE9A37', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type: 'Monitoring', color: '' },
    TOTAL_INTERNAL: { key: 'TOTAL_INTERNAL', label: 'Total Internal', value: 'total_internal', count: null, style: 'color: #2D9966', leftstyle: 'border-left:4px solid;color: #2D9966', i_class: 'fa-solid fa-plus-circle fa-beat-fade', type: 'Internal', color: '' },
    PENDING_FOR_APPROVAL: { key: 'PENDING_FOR_APPROVAL', label: 'Pending for Approval', value: 'pending_for_approval', count: null, style: 'color: #FFB300', leftstyle: 'border-left:4px solid;color: #FFB300', i_class: 'fa-solid fa-clock fa-beat-fade', color: '#FFB300' },
    APPROVED: { key: 'APPROVED', label: 'Approved', value: 'approved', count: null, style: 'color: #4CAF50', leftstyle: 'border-left:4px solid;color: #4CAF50', i_class: 'fa-solid fa-check-circle fa-beat-fade', color: '#4CAF50' },
    REJECTED: { key: 'REJECTED', label: 'Rejected', value: 'rejected', count: null, style: 'color: #e6241a', leftstyle: 'border-left:4px solid;color: #e6241a', i_class: 'fa-solid fa-times-circle fa-beat-fade', color: '#e6241a' },
    NOT_STARTED: { key: 'NOT_STARTED', label: 'Not Started', value: 'not_started', count: null, style: 'color: #9E9E9E', leftstyle: 'border-left:4px solid;color: #9E9E9E', i_class: 'fa-solid fa-minus-circle fa-beat-fade', color: '#9E9E9E' },
    COMPLETED_IN_ISHINE: { key: 'COMPLETED_IN_ISHINE', label: 'Completed In iShine', value: 'completed_in_ishine', count: null, style: 'color: #17ceee', leftstyle: 'border-left:4px solid;color: #17ceee', i_class: 'fa-solid fa-file-circle-check fa-beat-fade', color: '#17ceee' },
    COMPLETED_IN_SHANKH: { key: 'COMPLETED_IN_SHANKH', label: 'Completed In Shankh', value: 'completed_in_shankh', count: null, style: 'color: #00ec33', leftstyle: 'border-left:4px solid;color: #00ec33', i_class: 'fa-solid fa-file-circle-check fa-beat-fade', color: '#00ec33' },
    COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE: { key: 'COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE', label: 'Completed In Shankh,Team Active', value: 'completed_in_shankh_but_team_active', count: null, style: 'color: #7836c4', leftstyle: 'border-left:4px solid;color: #7836c4', i_class: 'fa-solid fa-flag-checkered fa-beat-fade', color: '#7836c4' },
    UNDERBOARDED: { key: 'UNDERBOARDED', label: 'Underboarded', value: 'underboarded', count: null, style: 'color: #f8f548ff', leftstyle: 'border-left:4px solid;color: #f8f548ff', i_class: 'fa-solid fa-arrow-trend-down fa-beat-fade', color: '#f8f548ff' },
    OVERBOARDED: { key: 'OVERBOARDED', label: 'Overboarded', value: 'overbo', count: null, style: 'color: #e92128ff', leftstyle: 'border-left:4px solid;color: #e92128ff', i_class: 'fa-solid fa-arrow-trend-up fa-beat-fade', color: '#e92128ff' },
    TIMESHEET_NON_COMPLIANCE: { key: 'TIMESHEET_NON_COMPLIANCE', label: 'Timesheet Non-Compliance Projects', value: 'timesheet_non_compliance', count: null, style: 'color: #e91e63', bgstyle: 'background-color: #e91e63;color: #fff;', leftstyle: 'border-left:4px solid;color: #e91e63', i_class: 'fa-solid fa-calendar-xmark fa-beat-fade', columnConfig: this.unfilledTimesheetProjectColumnConfig, defaultSortColumn: 'projectName', subTableColumnConfig: [], color: '' },
  };

  PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.ALL
    , this.PROJECT_STATUS.TOTAL
    , this.PROJECT_STATUS.TOTAL_FC
    , this.PROJECT_STATUS.TOTAL_FILTER_FC
    , this.PROJECT_STATUS.TOTAL_TNM
    , this.PROJECT_STATUS.TOTAL_ACTIVE_TNM
    , this.PROJECT_STATUS.TOTAL_EXPIRED_TNM
    , this.PROJECT_STATUS.TOTAL_MONITORING
    , this.PROJECT_STATUS.TOTAL_INTERNAL
    , this.PROJECT_STATUS.PENDING_FOR_APPROVAL
    , this.PROJECT_STATUS.APPROVED
    , this.PROJECT_STATUS.REJECTED
    , this.PROJECT_STATUS.NOT_STARTED
    , this.PROJECT_STATUS.COMPLETED_IN_ISHINE
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE
    , this.PROJECT_STATUS.UNDERBOARDED
    , this.PROJECT_STATUS.OVERBOARDED
  ];

  TOTAL_PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.ALL
    , this.PROJECT_STATUS.TOTAL
    , this.PROJECT_STATUS.TOTAL_FC
    , this.PROJECT_STATUS.TOTAL_TNM
    , this.PROJECT_STATUS.TOTAL_MONITORING
    , this.PROJECT_STATUS.TOTAL_INTERNAL
  ];

  TEAM_PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.NOT_STARTED
    , this.PROJECT_STATUS.PENDING_FOR_APPROVAL
    , this.PROJECT_STATUS.APPROVED
    , this.PROJECT_STATUS.REJECTED
    , this.PROJECT_STATUS.UNDERBOARDED
    , this.PROJECT_STATUS.OVERBOARDED
    , this.PROJECT_STATUS.COMPLETED_IN_ISHINE
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE
  ];

  // TEAM2_PROJECT_STATUS_LIST = [
  //   this.PROJECT_STATUS.TOTAL_ACTIVE_TNM
  // ];

  TEAM3_PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.TOTAL_EXPIRED_TNM
  ];

  TEAM4_PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.TOTAL_FILTER_FC
  ];

  TEAM5_PROJECT_STATUS_LIST = [
    this.PROJECT_STATUS.TIMESHEET_NON_COMPLIANCE
  ];

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
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: true, searchable: true }
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
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: true, searchable: true }
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
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: false, searchable: false }
    , { field: 'clientRM', header: 'Client RM', sortable: false, searchable: false }
    , { field: 'poNo', header: 'PO No.', sortable: false, searchable: false }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: false, searchable: false }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: false, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: false, searchable: false }
  ];

  mappedEmployeeDetailsSubTableColumnConfig = [
    { field: 'projectName', header: 'Project Name', sortable: false, searchable: false }
    , { field: 'clientName', header: 'Client Name', sortable: false, searchable: false }
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: false, searchable: false }
    , { field: 'clientRM', header: 'Client RM', sortable: false, searchable: false }
    , { field: 'poNo', header: 'PO No.', sortable: false, searchable: false }
    , { field: 'poProjectType', header: 'PO Project Type', sortable: false, searchable: false }
    , { field: 'poStartDate', header: 'PO Start Date', sortable: false, searchable: false }
    , { field: 'poEndDate', header: 'PO End Date', sortable: false, searchable: false }
    , { field: 'projectManagerName', header: 'Project Manager Name', sortable: false, searchable: false }
    , { field: 'teamName', header: 'Team Name', sortable: false, searchable: false }
    , { field: 'employeeRole', header: 'Employee Role', sortable: false, searchable: false }
  ];

  EMPLOYEE_GROUPS = {
    TOTAL: { key: 'TOTAL', label: 'TOTAL ApMoSys EMPLOYEES', value: 'total', count: null, style: 'color: #45556C;', i_class: 'fa fa-link', columnConfig: [], defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    MAPPED_TO_SHANKH: { key: 'MAPPED_TO_SHANKH', label: 'Mapped to Shankh Projects', value: 'mapped_to_shankh', count: null, style: 'color: #2E7D32;', i_class: 'fa fa-link', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.mappedEmployeeDetailsSubTableColumnConfig },
    MAPPED_TO_INTERNAL: { key: 'MAPPED_TO_INTERNAL', label: 'Mapped to Internal Projects', value: 'mapped_to_internal', count: null, style: 'color: #F9A825;', i_class: 'fa fa-link', columnConfig: this.mappedToInternalEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    MAPPED_TO_INTERNAL_AND_SHANKH: { key: 'MAPPED_TO_INTERNAL_AND_SHANKH', label: 'Mapped to Internal & Shankh Projects', value: 'mapped_to_internal_and_shankh', count: null, style: 'color: #820beb;', i_class: 'fa fa-link', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    NOT_MAPPED_TO_ANY_PROJECT: { key: 'NOT_MAPPED_TO_ANY_PROJECT', label: 'Employees Not Mapped to Any Project', value: 'not_mapped_to_any', count: null, style: 'color: #dc3545;', i_class: 'fa fa-unlink', columnConfig: this.notMappedEmployeesColumConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    MAPPED_TO_PROJECT: { key: 'MAPPED_TO_PROJECT', label: 'Employees Mapped to Project', value: 'mapped_to_project', count: null, style: 'color: #6F1D1B;', i_class: 'fa fa-link', columnConfig: this.otherEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    ON_BENCH_BUT_PROJECT_ASSIGNED: { key: 'ON_BENCH_BUT_PROJECT_ASSIGNED', label: 'Employees on Bench but Assigned to Other Projects', value: 'on_bench_but_project_assigned', count: null, style: 'color: #ee0a0a;', i_class: 'fa fa-link', columnConfig: this.onBenchButProjectAssignedEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    ON_BENCH_FOR_MORE_THAN_30_DAYS: { key: 'ON_BENCH_FOR_MORE_THAN_30_DAYS', label: 'Employee On Bench For More Than 30 Days', value: 'on_bench_for_more_than_30_days', count: null, style: 'color: #144552;', i_class: 'fa fa-link', columnConfig: this.onBenchEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
    WITHOUT_ANY_BILLABILITY: { key: 'WITHOUT_ANY_BILLABILITY', label: 'Employee Without Any Billability', value: 'without_any_billability', count: null, style: 'color: #a04e1f;', i_class: 'fa fa-link', columnConfig: this.withoutBillabilityEmployeesColumnConfig, defaultSortColumn: 'employmentIdAcToET', subTableColumnConfig: this.onBenchEmployeeDetailsSubTableColumnConfig },
  }

  EMPLOYEE_GROUPS_LIST = [
    this.EMPLOYEE_GROUPS.TOTAL,
    this.EMPLOYEE_GROUPS.MAPPED_TO_PROJECT,
    this.EMPLOYEE_GROUPS.NOT_MAPPED_TO_ANY_PROJECT,
    this.EMPLOYEE_GROUPS.MAPPED_TO_INTERNAL,
    this.EMPLOYEE_GROUPS.MAPPED_TO_SHANKH,
    this.EMPLOYEE_GROUPS.MAPPED_TO_INTERNAL_AND_SHANKH,
    this.EMPLOYEE_GROUPS.ON_BENCH_BUT_PROJECT_ASSIGNED,
    this.EMPLOYEE_GROUPS.ON_BENCH_FOR_MORE_THAN_30_DAYS,
    this.EMPLOYEE_GROUPS.WITHOUT_ANY_BILLABILITY
  ];

  EXPIRED_TNM_PROJECT_FILTERS_LIST = [
    { key: 'allExpiredTNMProjectsCount', label: 'All', title: 'All Expired TNM Projects', count: null, color: '' },
    { key: 'expiredProjectsWithin1Month', label: '1M', title: 'Expired Within 1 Month', count: null, color: '#28a745' },
    { key: 'expiredProjects1To2Months', label: '1-2M', title: 'Expired 1 to 2 Months', count: null, color: '#17a2b8' },
    { key: 'expiredProjects2To3Months', label: '2-3M', title: 'Expired 2 to 3 Months', count: null, color: '#ffc107' },
    { key: 'expiredProjects3To6Months', label: '3-6M', title: 'Expired 3 to 6 Months', count: null, color: '#fd7e14' },
    { key: 'expiredProjects6To9Months', label: '6-9M', title: 'Expired 6 to 9 Months', count: null, color: '#dc3545' },
    { key: 'expiredProjects9To12Months', label: '9-12M', title: 'Expired 9 to 12 Months', count: null, color: '#6f42c1' },
    { key: 'expiredProjectsAbove12Months', label: '12M+', title: 'Expired Above 12 Months', count: null, color: '#e83e8c' }
  ];

  FC_PROJECT_FILTERS_LIST = [
    { key: "all", label: 'Active', title: 'Active', count: null, color: '#5cb85c' },
    { key: "defaulter", label: 'Defaulter', title: 'Defaulter', count: null, color: '#d9534f' },
    // { key: "delays", label: 'Delayed', title: 'Delayed', count: null, color: '#5bc0de' },
    { key: "ontime", label: 'On Time', title: 'On Time', count: null, color: '#f0ad4e' },
  ];

  TIMESHEET_NON_COMPLIANCE_PROJECT_LIST = [
    { key: '3M', label: '3M', value: 3, unit: 'M', count: null },
    { key: '6M', label: '6M', value: 6, unit: 'M', count: null },
    { key: '1Y', label: '1Y', value: 1, unit: 'Y', count: null },
  ];

  projectTableActions = [
    { action: 'Preview', icon: 'fas fa-eye', name: 'Preview' },
    { action: 'Edit', icon: 'fas fa-pen', name: 'Edit' },
    { action: 'Mark as Completed', icon: 'fas fa-check-double', name: 'Mark as Completed' },
    { action: 'Approve', icon: 'fas fa-square-check', name: 'Approve' },
    { action: 'Reject', icon: 'fas fa-times', name: 'Reject' },
    { action: 'ClientID Status', icon: 'fas fa-id-badge', name: 'ClientID Status' },
  ];

  projectDetailsTableColumnConfig = [
    { field: 'name', header: 'Project Name', sortable: true, searchable: true }
    , { field: 'poNo', header: 'PO Number', sortable: true, searchable: true }
    , { field: 'projectType', header: 'Project Type', sortable: true, searchable: true }
    , { field: 'projectManagerName', header: 'Project Manager', sortable: true, searchable: true }
    , { field: 'clientName', header: 'Client', sortable: true, searchable: true }
    , { field: 'apmosysRM', header: 'Apmosys RM', sortable: true, searchable: true }
    , { field: 'clientRM', header: 'Client RM', sortable: true, searchable: true }
    , { field: 'poStartDate', header: 'Start Date', sortable: true, searchable: true }
    , { field: 'poEndDate', header: 'End Date', sortable: true, searchable: true }
    , { field: 'state', header: 'State', sortable: true, searchable: true }
    , { field: 'createdOn', header: 'Created On', sortable: true, searchable: false }
    , { field: 'status', header: 'PO Project Status', sortable: true, searchable: true }
    , { field: 'projectStatus', header: 'Ishine Project Status', sortable: true, searchable: true }
    , { field: 'draftStatus', header: 'Approval Status', sortable: true, searchable: true }
  ];

  allStates: any[] = ["Maharashtra"];
  projectColumns: any[] = ["blank", "name", "poNo", "poProjectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "blank", "blank", "state", "blank", "status", "projectStatus", "draftStatus"];


  currentUser: User;
  feature = "Resource Management";
  userMapping: any = {};

  selectedEmpId: any;
  alertMessage: '';
  selectedProjectStatus: string = '';
  viewType: 'Table' | 'Create Project' | 'Skill Matrix' = 'Table';


  selectedFCProjectFilter: any = this.FC_PROJECT_FILTERS_LIST[0].key;
  selectedExpiredTNMProjectFilter: any = this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0].key;
  selectedUnfilledTimesheetProjectFilter: any = this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST[0].key;

  isTotalProjectsSummaryDepartmentFilterActive: boolean = false;
  isAccounts: boolean = false;
  isEditProject: boolean;
  selectAllTeams: boolean = false;

  rmgDashboardProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
  rmgProjectFilterDTO: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();

  projectDetailsList: any[] = [];
  projectSummaryData: any[] = [];
  employeeList: Employee[] = [];
  teamLeadsList: Employee[] = [];
  filteredEmployees: Employee[] = [];
  filteredTeamLeads: Employee[] = [];
  employeeListByDept: Employee[] = [];
  selectedDeptIds: any[] = [];
  departmentList: any[] = [];

  searchOnEnter: boolean = true;
  isProjectSearchEnabled: boolean = false;
  totalProjectsCount: number;
  projectPage: number = 0;
  projectPageSize: number = 10;
  projectSortDirection: string = 'asc';
  projectSortColumn: string;
  projectSortColumnType: string;
  projectFilters: any = {};

  getEmployeeListByEmployeeGroupUrl: string = 'api/getEmployeeDetailsListByEmployeeGroup';
  employeeDetailsExtraParams: any = {};
  employeeDetailsDefaultSortColumn: string = '';
  employeeDetailsColumnConfig = [] = [];
  employeeDetailsSubTableColumnConfig = [] = [];

  getUnfilledTimesheetProjectDetailsListUrl: string = 'api/getUnfilledTimesheetProjectDetailsList';
  projectDetailsExtraParams: any = {};
  projectDetailsDefaultSortColumn: string = '';
  projectDetailsColumnConfig = [] = [];
  projectDetailsSubTableColumnConfig = [] = [];

  // Org Chart 
  maxRows: number = 0;
  expandAllProjects: boolean = false;
  showDepartmentFilter: boolean = true;
  toggleProjectSummaryDepartmentsVisible: boolean = false;
  tableData: any[][] = [];
  orgChartData: any[] = [];
  selectedDepartments: string[] = [];
  allDeptartmentList: any[] = [];
  expandedProjects: Set<string> = new Set();
  expandedColumns: Set<string> = new Set();
  projectFullText: Map<string, string> = new Map();
  filterError: string | null = null;
  // Currently Kept will make it dynamic later after discussion
  poDepartments = [
    {
      "deptab": "PT",
      "name": "Performance Testing"
    },
    {
      "deptab": "DEV, IT",
      "name": "Development, IT"
    },
    {
      "deptab": "FT",
      "name": "Functional Testing"
    },
    {
      "deptab": "APM, ST",
      "name": "APM, Security Testing"
    },
    {
      "deptab": "AC",
      "name": "Accounts"
    },
    {
      "deptab": "RPA",
      "name": "RPA"
    },
    {
      "deptab": "APM, DEV, FT",
      "name": "APM, Development, Functional Testing"
    },
    {
      "deptab": "APM",
      "name": "APM"
    },
    {
      "deptab": "APM, MONT, PS",
      "name": "APM, Application Performance Monitoring, Production Support"
    },
    {
      "deptab": "AC, ADM",
      "name": "Accounts, Admin"
    },
    {
      "deptab": "ADM",
      "name": "Admin"
    },
    {
      "deptab": "BD, DEV",
      "name": "Business Development, Development"
    },
    {
      "deptab": "BD, HR, RMG, SA",
      "name": "Business Development, HR, Resource Management Group, Super Admin"
    },
    {
      "deptab": "AUT, IT, RND",
      "name": "Automation Testing, IT, Products and RND"
    },
    {
      "deptab": "PRSALE",
      "name": "Presales"
    },
    {
      "deptab": "APM, FT, PT, TRNA",
      "name": "APM, Functional Testing, Performance Testing, Training"
    },
    {
      "deptab": "AC, AUT",
      "name": "Accounts, Automation Testing"
    },
    {
      "deptab": "BD",
      "name": "Business Development"
    },
    {
      "deptab": "DEV, FT, RND",
      "name": "Development, Functional Testing, Products and RND"
    },
    {
      "deptab": "DEV",
      "name": "Development"
    },
    {
      "deptab": "APM, DEV",
      "name": "APM, Development"
    },
    {
      "deptab": "FA, PS",
      "name": "Floor Automation, Production Support"
    },
    {
      "deptab": "AUT, FT",
      "name": "Automation Testing, Functional Testing"
    },
    {
      "deptab": "AUT, IT",
      "name": "Automation Testing, IT"
    },
    {
      "deptab": "AUT, DEV, RND",
      "name": "Automation Testing, Development, Products and RND"
    },
    {
      "deptab": "APM, PS",
      "name": "APM, Production Support"
    },
    {
      "deptab": "APM, FT, PT",
      "name": "APM, Functional Testing, Performance Testing"
    },
    {
      "deptab": "AUT, RPA",
      "name": "Automation Testing, RPA"
    },
    {
      "deptab": "DEV, FT, RPA, ST",
      "name": "Development, Functional Testing, RPA, Security Testing"
    },
    {
      "deptab": "HR",
      "name": "HR"
    },
    {
      "deptab": "APM, DEV, PT",
      "name": "APM, Development, Performance Testing"
    },
    {
      "deptab": "AUT, DEV",
      "name": "Automation Testing, Development"
    },
    {
      "deptab": "AUT",
      "name": "Automation Testing"
    },
    {
      "deptab": "AUT, FT, PS",
      "name": "Automation Testing, Functional Testing, Production Support"
    },
    {
      "deptab": "DEV, PS",
      "name": "Development, Production Support"
    },
    {
      "deptab": "PS",
      "name": "Production Support"
    },
    {
      "deptab": "IT, PS",
      "name": "IT, Production Support"
    },
    {
      "deptab": "AC, APM, AUT, BD, DEV, FT, HR, SA, ST",
      "name": "Accounts, APM, Automation Testing, Business Development, Development, Functional Testing, HR, Security Testing, Super Admin"
    },
    {
      "deptab": "AUT, FT, ST",
      "name": "Automation Testing, Functional Testing, Security Testing"
    },
    {
      "deptab": "FT, PS",
      "name": "Functional Testing, Production Support"
    },
    {
      "deptab": "AUT, FT, PT",
      "name": "Automation Testing, Functional Testing, Performance Testing"
    },
    {
      "deptab": "AUT, FT, RPA",
      "name": "Automation Testing, Functional Testing, RPA"
    },
    {
      "deptab": "APM, PT",
      "name": "APM, Performance Testing"
    },
    {
      "deptab": "FT, ST",
      "name": "Functional Testing, Security Testing"
    },
    {
      "deptab": "BD, FT",
      "name": "Business Development, Functional Testing"
    },
    {
      "deptab": "AUT, RND",
      "name": "Automation Testing, Products and RND"
    },
    {
      "deptab": "BD, DEV, FT, ST",
      "name": "Business Development, Development, Functional Testing, Security Testing"
    },
    {
      "deptab": "RND",
      "name": "Products and RND"
    },
    {
      "deptab": "APM, AUT, DEV, FT, PS, PT, RND, RPA, ST",
      "name": "APM, Automation Testing, Development, Functional Testing, Performance Testing, Production Support, Products and RND, RPA, Security Testing"
    },
    {
      "deptab": "AC, APM",
      "name": "Accounts, APM"
    },
    {
      "deptab": "BD, PT",
      "name": "Business Development, Performance Testing"
    },
    {
      "deptab": "APM, AUT, DEV, FT, PS, RPA",
      "name": "APM, Automation Testing, Development, Functional Testing, Production Support, RPA"
    },
    {
      "deptab": "FT, PT",
      "name": "Functional Testing, Performance Testing"
    },
    {
      "deptab": "DIR, FT, PT",
      "name": "Director, Functional Testing, Performance Testing"
    }
  ];

  constructor(
    private filterStateService: FilterStateService,
    private scroller: ViewportScroller,
    private departmentService: DepartmentService,
    public validationService: ValidationService,
    private employeeService: EmployeeService,
    private modalService: NgbModal,
    private teamService: TeamService,
    private resourceManagementService: ResourceManagementService,
    private authenticationService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private exportExcelService: ExportExcelService,
    private utilityService: UtilityService,
    private breadcrumbService: BreadcrumbService,
    private employee360Service: Employee360Service,
    private sanitizer: DomSanitizer,
    private dialog: MatDialog,
    private renderer: Renderer2,
    private appComponent: AppComponent,
    private el: ElementRef,
    private loaderService: LoaderService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit() {
    const deptName = String(this.currentUser.departmentName).trim();
    this.isAccounts = deptName === 'Accounts';
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    this.selectedExpiredTNMProjectFilter = this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0].key;
    this.selectedFCProjectFilter = this.FC_PROJECT_FILTERS_LIST[0].key;
    this.rmgDashboardProjectRequest.projectStatus = this.PROJECT_STATUS.TOTAL.key;
    this.rmgDashboardProjectRequest.currentUserEmpId = this.currentUser.empId;
    this.rmgDashboardProjectRequest.currentUserType = this.determineUserType();
    this.projectPageSize = this.filterStateService?.projectPageSize ? this.filterStateService.projectPageSize : 10;
    this.rmgDashboardProjectRequest.pageSize = this.projectPageSize
    this.selectedDeptIds = this.selectedDepartmentIds?.length > 0 ? this.selectedDepartmentIds : this.selectedDeptIds;
    this.rmgDashboardProjectRequest.departmentIds = this.selectedDeptIds;
    this.getAllDepartmentsList();
    this.mapSubFeatureFlag();
    this.loadRMGDashboard(this.rmgDashboardProjectRequest);
  }

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

  openEmployeeProjectTimesheetSummaryModal(selectedEmpId: any) {
    this.selectedEmpId = selectedEmpId;
    this.employeeProjectTimesheetSummaryModalRef = this.modalService.open(this.employeeProjectTimesheetSummaryTemplateRef, { modalDialogClass: 'modal-lg' });
    this.getEmployeeProjectTimesheetSummaryData();
  }

  closeEmployeeProjectTimesheetSummaryModal() {
    if (this.employeeProjectTimesheetSummaryModalRef) {
      this.employeeProjectTimesheetSummaryModalRef?.close();
    }
  }

  openExpiredTNMProjectsSummaryModal() {
    this.expiredTNMProjectsSummaryModalRef = this.modalService.open(this.expiredTNMProjectsSummaryTemplateRef, { modalDialogClass: 'modal-lg' });
    this.getExpiredTNMProjectSummaryData();
  }

  closeExpiredTNMProjectsSummaryModal() {
    if (this.expiredTNMProjectsSummaryModalRef) {
      this.expiredTNMProjectsSummaryModalRef?.close();
    }
  }

  openTotalProjectsSummaryModal(projectStatus: any) {
    if (projectStatus === this.PROJECT_STATUS.TOTAL_INTERNAL.key || projectStatus === this.PROJECT_STATUS.ALL.key) {
      return;
    }
    this.selectedProjectStatus = projectStatus;
    this.isTotalProjectsSummaryDepartmentFilterActive = false;
    this.totalProjectsSummaryModalRef = this.modalService.open(this.totalProjectsSummaryTemplateRef, { modalDialogClass: 'modal-xl' });
    this.prepareAndFetchTotalProjectsSummaryChartData();
  }

  closeTotalProjectsSummaryModal() {
    if (this.totalProjectsSummaryModalRef) {
      this.totalProjectsSummaryModalRef?.close();
    }
  }

  openTeamProjectStatusSummaryModal() {
    this.teamProjectStatusSummaryModalRef = this.modalService.open(this.teamProjectStatusSummaryTemplateRef, { modalDialogClass: 'modal-lg' });
    this.getTeamProjectStatusSummaryChartData();
  }

  closeTeamProjectStatusSummaryModal() {
    if (this.teamProjectStatusSummaryModalRef) {
      this.teamProjectStatusSummaryModalRef?.close();
    }
  }

  openFixedCostProjectsSummaryModal() {
    this.fixedCostProjectsSummaryModalRef = this.modalService.open(this.fixedCostProjectsSummaryTemplateRef, { modalDialogClass: 'modal-lg' });
    this.getFixedCostProjectSummaryData();
  }

  closeFixedCostProjectsSummaryModal() {
    if (this.fixedCostProjectsSummaryModalRef) {
      this.fixedCostProjectsSummaryModalRef?.close();
    }
  }

  openEmployeeDetailsModal(employeeGroup: any) {
    if (employeeGroup.key == this.EMPLOYEE_GROUPS.TOTAL.key) {
      return;
    }
    this.employeeDetailsExtraParams = { "employeeGroupKey": employeeGroup?.key, "selectedDeptIds": this.selectedDeptIds, "projectStatus": this.selectedProjectStatus || this.PROJECT_STATUS.ALL.key, "expiredProjectFilter": this.selectedExpiredTNMProjectFilter, "fixedCostFilter": this.selectedFCProjectFilter };
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

  openProjectDetailsModal(projectStatus: any) {
    const today = new Date();
    const fDate = new Date(today);
    const filter = this.selectedUnfilledTimesheetProjectFilter;
    const value = parseInt(filter, 10);
    if (filter.includes('Y')) {
      fDate.setFullYear(fDate.getFullYear() - value);
    } else if (filter.includes('M')) {
      fDate.setMonth(fDate.getMonth() - value);
    } else {
      fDate.setMonth(fDate.getMonth() - 3);
    }

    const fromDate = fDate.toISOString().split('T')[0];
    const toDate = today.toISOString().split('T')[0];

    this.projectDetailsExtraParams = { "selectedDeptIds": this.selectedDeptIds, "projectStatus": this.selectedProjectStatus, "fromDate": fromDate, "toDate": toDate };
    this.projectDetailsColumnConfig = projectStatus?.columnConfig;
    this.projectDetailsDefaultSortColumn = projectStatus?.defaultSortColumn;
    this.projectDetailsSubTableColumnConfig = projectStatus?.columnConfig
    this.timesheetNonComplianceProjectDetailsModalRef = this.modalService.open(this.timesheetNonComplianceProjectDetailsTemplateRef, { modalDialogClass: 'modal-xl' });
  }

  closeProjectDetailsModal() {
    if (this.timesheetNonComplianceProjectDetailsModalRef) {
      this.timesheetNonComplianceProjectDetailsModalRef?.close();
    }
  }

  showMessageInEmployeeProjectTimesheetSummary(message: any, isError: any) {
    if (isError) {
      document.getElementById('projectTimesheetSummaryChart').innerHTML = `<p class="text-center text-danger">${message}</p>`;
    } else {
      document.getElementById('projectTimesheetSummaryChart').innerHTML = `<p class="text-center">${message}</p>`;
    }
  }
  // Modals End

  // Helpers Start
  private mapSubFeatureFlag() {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  determineUserType(): 'HOD' | 'ADMIN' | 'USER' {
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

  mapToRMGRequest(rmgDashboardProjectRequest: any) {
    let rmgProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    rmgProjectRequest.currentUserEmpId = this.currentUser?.empId;
    rmgProjectRequest.currentUserType = this.determineUserType();
    rmgProjectRequest.page = this.projectPage || 0;
    rmgProjectRequest.pageSize = this.projectPageSize;
    rmgProjectRequest.projectFilter = this.projectFilters;
    rmgProjectRequest.sortColumn = this.projectSortColumn || 'name';
    rmgProjectRequest.sortDirection = this.projectSortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.projectSortColumnType || 'string';
    rmgProjectRequest.departmentIds = rmgDashboardProjectRequest?.departmentIds;
    rmgProjectRequest.projectStatus = rmgDashboardProjectRequest?.projectStatus;
    rmgProjectRequest.expiredProjectFilter = rmgDashboardProjectRequest?.expiredProjectFilter;
    rmgProjectRequest.fixedCostFilter = rmgDashboardProjectRequest?.fixedCostFilter;

    this.rmgProjectFilterDTO = new RMGDashboardProjectRequest();
    this.rmgProjectFilterDTO.departmentIds = rmgDashboardProjectRequest?.departmentIds;
    this.rmgProjectFilterDTO.projectStatus = rmgDashboardProjectRequest?.projectStatus;
    this.rmgProjectFilterDTO.expiredProjectFilter = rmgDashboardProjectRequest?.expiredProjectFilter;
    this.rmgProjectFilterDTO.fixedCostFilter = rmgDashboardProjectRequest?.fixedCostFilter;
    return rmgProjectRequest;
  }

  getNewRMGRequestObject() {
    let newRmgDashboardProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    newRmgDashboardProjectRequest.currentUserEmpId = this.currentUser.empId;
    newRmgDashboardProjectRequest.currentUserType = this.determineUserType();
    newRmgDashboardProjectRequest.departmentIds = this.selectedDeptIds;
    return newRmgDashboardProjectRequest;
  }

  loadRMGDashboard(rmgDashboardProjectRequest: any) {
    this.getAllEmployeeGroupCount();
    this.getAllProjectStatusCount();
    this.fetchProjectDetailsList(rmgDashboardProjectRequest);
  }

  onAction(action: string, project: any) {
    this.actionTriggered.emit({ action, project });
  }

  onDepartmentSelectionChange(event: any) {
    this.selectedDeptIds = event;
    this.projectPage = 0;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.departmentIds = this.selectedDeptIds;
    newRmgDashboardProjectRequest.projectStatus = this.rmgProjectFilterDTO?.projectStatus;
    newRmgDashboardProjectRequest.expiredProjectFilter = this.rmgProjectFilterDTO?.expiredProjectFilter;
    newRmgDashboardProjectRequest.fixedCostFilter = this.rmgProjectFilterDTO?.fixedCostFilter;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  normalizeDate(dateInput: any): Date | null {
    if (!dateInput) {
      return null;
    }

    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return null;
    }

    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }
  // Helpers End

  // List 
  onProjectStatusSelect(status: string, projectStatusList: any) {
    this.projectPage = 0;
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    this.selectedProjectStatus = status;
    this.filterStateService.selectedProjectStatus = status;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;

    if (this.selectedProjectStatus === this.PROJECT_STATUS.TOTAL_EXPIRED_TNM.key) {
      if (!this.selectedExpiredTNMProjectFilter) {
        this.selectedExpiredTNMProjectFilter = this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0].key;
      }
      newRmgDashboardProjectRequest.expiredProjectFilter = this.selectedExpiredTNMProjectFilter;
    }
    else if (this.selectedProjectStatus == this.PROJECT_STATUS.TOTAL_FC.key) {
      this.selectedFCProjectFilter = this.FC_PROJECT_FILTERS_LIST[0].key;
      newRmgDashboardProjectRequest.fixedCostFilter = this.selectedFCProjectFilter?.value;
    }
    this.getProjectStatusCount(this.selectedProjectStatus, projectStatusList);
    this.getAllEmployeeGroupCount();
    this.fetchProjectDetailsList(newRmgDashboardProjectRequest);
  }
  // 

  // Count & List APIs & Methods Start
  private getAllEmployeeGroupCount() {
    this.EMPLOYEE_GROUPS_LIST.forEach(group => {
      this.getEmployeeCountByEmployeeGroup(group.key, this.selectedProjectStatus);
    });
  }

  private getAllProjectStatusCount() {
    this.TOTAL_PROJECT_STATUS_LIST.forEach(project => {
      this.getProjectStatusCount(project.key, this.TOTAL_PROJECT_STATUS_LIST);
    });
    this.TEAM_PROJECT_STATUS_LIST.forEach(project => {
      this.getProjectStatusCount(project.key, this.TEAM_PROJECT_STATUS_LIST);
    });
    // this.TEAM2_PROJECT_STATUS_LIST.forEach(project => {
    //   this.getProjectStatusCount(project.key, this.TEAM2_PROJECT_STATUS_LIST);
    // });
    this.TEAM3_PROJECT_STATUS_LIST.forEach(project => {
      this.getProjectStatusCount(project.key, this.TEAM3_PROJECT_STATUS_LIST);
    });
    this.EXPIRED_TNM_PROJECT_FILTERS_LIST.forEach(filter => {
      this.getExpiredTNMFilterWiseProjectStatusCount(this.PROJECT_STATUS.TOTAL_EXPIRED_TNM.key, filter.key);
    });
    this.TEAM4_PROJECT_STATUS_LIST.forEach(project => {
      this.getProjectStatusCount(project.key, this.TEAM4_PROJECT_STATUS_LIST);
    });
    this.FC_PROJECT_FILTERS_LIST.forEach(filter => {
      this.getFCFilterWiseProjectStatusCount(this.PROJECT_STATUS.TOTAL_FILTER_FC.key, filter.key);
    });
    this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST.forEach(filter => {
      this.getUnFilledTimesheetProjectStatusCount(filter.key);
    })
  }

  getEmployeeCountByEmployeeGroup(key: any, projectStatus?: any) {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.employeeGroupKey = key;
    newRmgDashboardProjectRequest.projectStatus = projectStatus;
    newRmgDashboardProjectRequest.expiredProjectFilter = this.selectedExpiredTNMProjectFilter;
    newRmgDashboardProjectRequest.fixedCostFilter = this.selectedFCProjectFilter;
    this.resourceManagementService.getEmployeeCountByEmployeeGroup(newRmgDashboardProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const group of this.EMPLOYEE_GROUPS_LIST) {
          if (key === group.key) {
            group.count = counts[group.key];
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

  getProjectStatusCount(projectStatus: any, projectStatusList: any[]) {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = projectStatus;
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

  getExpiredTNMFilterWiseProjectStatusCount(projectStatus: any, expiredTNMProjectFilter: any) {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = projectStatus;
    newRmgDashboardProjectRequest.expiredProjectFilter = expiredTNMProjectFilter;
    this.resourceManagementService.getProjectStatusCount(newRmgDashboardProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.EXPIRED_TNM_PROJECT_FILTERS_LIST) {
          if (expiredTNMProjectFilter === filter.key) {
            filter.count = counts[projectStatus];
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

  getFCFilterWiseProjectStatusCount(projectStatus: any, fcProjectFilter: any) {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = projectStatus;
    newRmgDashboardProjectRequest.fixedCostFilter = fcProjectFilter;
    this.resourceManagementService.getProjectStatusCount(newRmgDashboardProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        for (const filter of this.FC_PROJECT_FILTERS_LIST) {
          if (fcProjectFilter === filter.key) {
            filter.count = counts[projectStatus];
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

  getUnFilledTimesheetProjectStatusCount(unFilledProjectTimesheetFilter?: any) {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.PROJECT_STATUS.TIMESHEET_NON_COMPLIANCE.key;
    const today = new Date();
    const fDate = new Date(today);
    const filter = unFilledProjectTimesheetFilter || this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST[0].key;
    const value = parseInt(filter, 10);
    if (filter.includes('Y')) {
      fDate.setFullYear(fDate.getFullYear() - value);
    } else if (filter.includes('M')) {
      fDate.setMonth(fDate.getMonth() - value);
    } else {
      fDate.setMonth(fDate.getMonth() - 3);
    }
    newRmgDashboardProjectRequest.fromDate = fDate.toISOString().split('T')[0];
    newRmgDashboardProjectRequest.toDate = today.toISOString().split('T')[0];
    newRmgDashboardProjectRequest.unfilledTimesheetFilter = unFilledProjectTimesheetFilter || this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST[0].key;
    this.resourceManagementService.getUnfilledTimesheetProjectDetailsCount(newRmgDashboardProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null) {
        const counts = response.serviceResponse;
        this.TEAM5_PROJECT_STATUS_LIST[0].count = counts[this.PROJECT_STATUS.TIMESHEET_NON_COMPLIANCE.key];
        for (const filter of this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST) {
          if (unFilledProjectTimesheetFilter === filter.key) {
            filter.count = counts[this.PROJECT_STATUS.TIMESHEET_NON_COMPLIANCE.key];
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

  fetchProjectDetailsList(rmgDashboardProjectRequest: any) {
    this.totalProjectsCount = 0;
    this.projectDetailsList = [];
    let rmgProjectRequest = this.mapToRMGRequest(rmgDashboardProjectRequest);
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList)) {
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

  selectExpiredTNMProjectFilter(expiredTNMProjectStatusObj: any, filter: any) {
    this.projectPage = 0;
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    this.selectedExpiredTNMProjectFilter = filter.key;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL_EXPIRED_TNM.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = expiredTNMProjectStatusObj?.key;
    newRmgDashboardProjectRequest.expiredProjectFilter = filter.key;
    this.fetchProjectDetailsList(newRmgDashboardProjectRequest);
    this.getAllEmployeeGroupCount();
  }

  selectFCProjectFilter(fcProjectStatusObj: any, filter: any) {
    this.projectPage = 0;
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    this.selectedFCProjectFilter = filter.key;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL_FILTER_FC.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = fcProjectStatusObj?.key;
    newRmgDashboardProjectRequest.fixedCostFilter = filter.key;
    this.fetchProjectDetailsList(newRmgDashboardProjectRequest);
    this.getAllEmployeeGroupCount();
  }

  selectUnfilledTimesheetProjectFilter(filter: any) {
    this.selectedUnfilledTimesheetProjectFilter = filter.key;
    this.getUnFilledTimesheetProjectStatusCount(filter.key);

  }

  get selectedProjectCount(): number | undefined {
    const found = this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST
      ?.find(t => t.key === this.selectedUnfilledTimesheetProjectFilter);
    return found?.count ?? this.TEAM5_PROJECT_STATUS_LIST[0]?.count;
  }
  // Count & List APIs & Methods End

  // Chart APIs & Methods Start 
  getEmployeeProjectTimesheetSummaryData() {
    if (!this.selectedEmpId) {
      this.openAlertMessageModal("Cannot fetch summary. User information is missing.");
      if (this.employeeProjectTimesheetSummaryModalRef) {
        this.showMessageInEmployeeProjectTimesheetSummary('Could not load data: User not identified.', true);
      }
      return;
    }
    const resourceManagementDTO = {
      empId: this.selectedEmpId
    };

    this.resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.projectSummaryData = response.serviceResponse;
          if (this.projectSummaryData && this.projectSummaryData.length > 0) {
            this.processTotalProjectsSummaryData(this.projectSummaryData);
          } else {
            this.showMessageInEmployeeProjectTimesheetSummary('No timesheet summary data found for your projects.', false);
          }
        } else {
          this.showMessageInEmployeeProjectTimesheetSummary('Error:No timesheets filled till date', true);
        }
      },
      error: (err) => {
        this.openAlertMessageModal("An error occurred while fetching summary data.");
        this.showMessageInEmployeeProjectTimesheetSummary('A server error occurred. Please try again later.', true);
      }
    });
  }

  processTotalProjectsSummaryData(summaryData: any[]) {
    const categories = [];
    const seriesData = [];

    const sortedData = summaryData
      .sort((a, b) => b.totalTimesheetsFilled - a.totalTimesheetsFilled)
      .slice(0, 20);

    sortedData.forEach(item => {
      categories.push(item.projectName);
      seriesData.push(item.totalTimesheetsFilled);
    });

    const chartData = [{
      name: 'Timesheets Filled',
      data: seriesData,
      color: '#0275d8'
    }];

    this.renderColumnChart(
      'Top 20 Projects by Timesheets Filled',
      'projectTimesheetSummaryChart',
      chartData,
      categories,
      'Number of Timesheets Filled'
    );
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

  getTeamProjectStatusSummaryChartData() {
    const categories = this.TEAM_PROJECT_STATUS_LIST.map(projectStatus => { return projectStatus.label });
    const data = this.TEAM_PROJECT_STATUS_LIST.map(projectStatus => ({
      y: projectStatus.count || 0,
      color: projectStatus.color
    }));

    const chartData = [{
      name: 'Project Status',
      data: data
    }];

    this.renderColumnChart(
      'Projects Overview',
      'ProjectChart',
      chartData,
      categories,
      'Number of Projects'
    );
  }

  getExpiredTNMProjectSummaryData() {
    const categories = ['1M', '1-2M', '2-3M', '3-6M', '6-9M', '9-12M', '12M+'];
    const data = this.EXPIRED_TNM_PROJECT_FILTERS_LIST.slice(1).map(item => ({
      y: item.count || 0,
      color: item.color
    }));

    const chartData = [{
      name: 'Expired TNM Projects',
      data: data
    }];

    this.renderColumnChart(
      'Expired TNM Projects by Time Period',
      'expiredTNMProjectChart',
      chartData,
      categories,
      'Number of Projects'
    );
  }

  getFixedCostProjectSummaryData() {
    const categories = ['Active', 'Defaulter', 'Delayed', 'On Time'];
    const data = this.FC_PROJECT_FILTERS_LIST.map(item => ({
      y: item.count || 0,
      color: item.color
    }));

    const chartData = [{
      name: 'Fixed Cost Projects',
      data: data
    }];

    this.renderColumnChart(
      'Fixed Cost Projects Overview',
      'fixedCostProjectChart',
      chartData,
      categories
      , 'Number of Projects'
    );
  }

  prepareAndFetchTotalProjectsSummaryChartData() {
    let filteredDepts = this.poDepartments;
    if (this.selectedDeptIds?.length) {
      const allowedDeptNames = new Set(this.departmentList?.filter(d => this.selectedDeptIds?.includes(d.deptId)).map(d => d.name.toLowerCase().trim()));

      filteredDepts = this.poDepartments.filter(po => {
        const subDepts = po.name.toLowerCase().split(',').map(d => d.trim());
        return subDepts.some(sd => allowedDeptNames.has(sd));
      });
    }
    this.allDeptartmentList = filteredDepts.map(({ name, deptab }) => ({ name, deptab })).sort((a, b) => a.name.localeCompare(b.name));
    this.filterError = null;
    this.selectedDepartments = [];
    this.getProjectStructure();
  }

  getProjectStructure() {
    this.orgChartData = [];
    const projectStructure = {
      deptName: this.selectedDepartments?.length > 0 ? this.selectedDepartments : null,
      type: this.selectedProjectStatus || this.PROJECT_STATUS.TOTAL.key,
      departmentIds: this.selectedDeptIds
    };

    this.projectService.getProjectStructure(projectStructure).pipe(first()).subscribe((res: any) => {
      if (res.serviceStatus === "Success") {
        const projectStructureResponse = res.serviceResponse || [];
        this.processDataForOrgChart(projectStructureResponse);
      } else {
        this.orgChartData = [];
        console.error("Service call failed:", res.serviceResponse);
      }
    });
  }

  processDataForOrgChart(projectStructureResponse: any) {
    const departmentMap = this.buildDepartmentMap(projectStructureResponse);
    this.orgChartData = Array.from(departmentMap.entries()).map(([deptAb, clientMap]) => ({
      name: deptAb,
      cssClass: 'department-node',
      childs: Array.from(clientMap.entries()).map(([clientName, projects]) => ({
        name: clientName,
        cssClass: 'client-node',
        childs: projects.map(p => ({
          name: p,
          cssClass: 'project-node'
        }))
      }))
    }));
    this.processDataForTable(departmentMap);
  }

  private buildDepartmentMap(data: any[]) {
    const departmentMap = new Map<string, Map<string, string[]>>();
    data.forEach(({ deptAb, clientName, projectName }) => {
      if (!departmentMap.has(deptAb)) {
        departmentMap.set(deptAb, new Map());
      }

      const clientMap = departmentMap.get(deptAb)!;
      if (!clientMap.has(clientName)) {
        clientMap.set(clientName, []);
      }
      clientMap.get(clientName)!.push(projectName);
    });
    return departmentMap;
  }

  processDataForTable(departmentMap: Map<string, Map<string, string[]>>) {
    this.expandedProjects.clear();
    this.projectFullText.clear();
    const tableColumns: any[][] = [];

    departmentMap?.forEach((clientMap, dept) => {
      const deptColumn: any[] = [dept];
      clientMap.forEach((projects, clientName) => {
        deptColumn.push({ type: 'client', name: clientName });
        projects.forEach(project =>
          deptColumn.push({ type: 'project', name: project })
        );
      });
      tableColumns.push(deptColumn);
    });

    this.maxRows = Math.max(...tableColumns.map(c => c.length));
    tableColumns?.forEach(col => {
      while (col.length < this.maxRows) col.push('');
    });

    this.tableData = Array.from({ length: this.maxRows }, (_, rowIndex) =>
      tableColumns.map(col => col[rowIndex] || '')
    );
  }

  toggleExpansion(text: string) {
    this.expandedProjects.has(text)
      ? this.expandedProjects.delete(text)
      : this.expandedProjects.add(text);
  }

  toggleExpandAllProjects(departmentName: string) {
    this.expandedColumns.has(departmentName)
      ? this.expandedColumns.delete(departmentName)
      : this.expandedColumns.add(departmentName);
  }

  truncate(text: string, limit = 15, fromStart = false): string {
    if (!text || text.length <= limit) {
      return text;
    }

    return fromStart
      ? text.substring(0, limit) + '...'
      : '...' + text.substring(text.length - limit + 3);
  }

  getCellFullText(cell: any): string {
    if (typeof cell === 'string') {
      return cell;
    }
    if (cell && cell.name) {
      return cell.name;
    }
    return '';
  }

  getCellDisplayText(cell: any): string {
    if (typeof cell === 'string') {
      return cell;
    }
    if (cell && cell.name) {
      if (cell.type === 'project') {
        return this.getDisplayProjectName(cell.name);
      }
      return cell.name;
    }
    return '';
  }

  getDisplayProjectName(projectName: string, departmentName?: string): string {
    if (!projectName) {
      return '';
    }

    this.projectFullText.set(projectName, projectName);
    const expanded = (departmentName && this.expandedColumns.has(departmentName)) || this.expandedProjects.has(projectName);
    return expanded ? projectName : this.truncate(projectName);
  }

  getDisplayClientName(clientName: string, departmentName?: string): string {
    if (!clientName) {
      return '';
    }

    this.projectFullText.set(clientName, clientName);
    const expanded = departmentName && this.expandedColumns.has(departmentName) || this.expandedProjects.has(clientName);
    return expanded ? clientName : this.truncate(clientName, 15, true);
  }

  isDepartmentHeader(rowIndex: number): boolean {
    return rowIndex === 0;
  }

  isClientName(rowIndex: number, colIndex: number): boolean {
    if (rowIndex === 0) return false;
    const currentCell = this.tableData[rowIndex][colIndex];
    return currentCell && typeof currentCell === 'object' && currentCell.type === 'client';
  }

  isProjectName(rowIndex: number, colIndex: number): boolean {
    if (rowIndex === 0) return false;
    const currentCell = this.tableData[rowIndex][colIndex];
    return currentCell && typeof currentCell === 'object' && currentCell.type === 'project';
  }

  isDepartmentCell(rowIndex: number, colIndex: number): boolean {
    if (rowIndex !== 0) return false;
    const currentCell = this.tableData[rowIndex][colIndex];
    return typeof currentCell === 'string' && currentCell !== '';
  }

  isColumnExpanded(departmentName: string): boolean {
    return this.expandedColumns.has(departmentName);
  }

  toggleProjectSummaryChartDepartments() {
    this.toggleProjectSummaryDepartmentsVisible = !this.toggleProjectSummaryDepartmentsVisible;
  }

  updateSelectedDepartments(department: string, event: any): void {
    const isChecked = event.target.checked;
    if (isChecked) {
      this.selectedDepartments = [department];
    } else {
      this.selectedDepartments = [];
    }
    this.getProjectStructure();
  }
  // Chart Data APIs & Methods End 

  // Projects Table APIs & Methods Start
  onProjectSearch(searchData: any) {
    this.projectPage = 0;
    this.projectFilters = searchData;
    this.filterStateService.projectReportFilters = this.projectFilters;
    this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
  }

  onProjectPageChange(event: any) {
    this.projectPage = event.pageIndex;
    this.projectPageSize = event.pageSize;
    this.filterStateService.projectPageSize = this.projectPageSize;
    this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
  }

  sortProjectData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.projectSortColumn = sortParams[0];
      this.projectSortColumnType = sortParams[1];
      this.projectSortDirection = sort.direction;
      this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
    }
  }

  toggleProjectSearch(): void {
    this.isProjectSearchEnabled = !this.isProjectSearchEnabled;
    if (!this.isProjectSearchEnabled) {
      this.projectFilters = {};
      this.filterStateService.clearProjectReportFilters();
      this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
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
    let rmgProjectRequest = this.getNewRMGRequestObject();
    rmgProjectRequest.departmentIds = this.selectedDeptIds;
    rmgProjectRequest.projectStatus = this.rmgProjectFilterDTO?.projectStatus;
    rmgProjectRequest.expiredProjectFilter = this.rmgProjectFilterDTO?.expiredProjectFilter;
    rmgProjectRequest.fixedCostFilter = this.rmgProjectFilterDTO?.fixedCostFilter;
    rmgProjectRequest.currentUserEmpId = this.currentUser?.empId;
    rmgProjectRequest.currentUserType = this.determineUserType();
    rmgProjectRequest.projectFilter = this.projectFilters;
    rmgProjectRequest.page = 0;
    rmgProjectRequest.pageSize = 100000;
    rmgProjectRequest.sortColumn = this.projectSortColumn || 'name';
    rmgProjectRequest.sortDirection = this.projectSortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.projectSortColumnType || 'string';

    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList)) {
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
  // Projects Table APIs & Methods End

  getAllDepartmentsList() {
    this.departmentList = [];
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.departmentList = response.serviceResponse || [];
      }
    });
  }
}
