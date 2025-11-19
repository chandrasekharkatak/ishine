import { ViewportScroller } from '@angular/common';
import { Component, ElementRef, OnInit, Renderer2, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog } from '@angular/material/dialog';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { finalize, first, map, startWith } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Status } from 'src/app/enum/status';
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { employeeReport } from 'src/app/models/employeeReport';
import { FCProjectMilestone } from 'src/app/models/fcProjectMileStone';
import { Feature } from 'src/app/models/feature';
import { FilteredTimesheet } from 'src/app/models/filteredTimesheet';
import { GetProjectDetailsForBulkDefaultUpdate } from 'src/app/models/getProjectDetailsForBulkDefaultUpdate';
import { LiftAndShift } from 'src/app/models/liftAndShift';
import { PreviousDefaultProject } from 'src/app/models/previousDefaultProject';
import { searchEmployeeResultSet } from 'src/app/models/searchEmployeeResultSet';
import { Project } from 'src/app/models/project';
import { FilterMatrix } from 'src/app/models/filterMatrix';
import { ProjectFilterDTO } from 'src/app/models/projectFilterDTO';
import { ProjectRequirements } from 'src/app/models/projectRequirements';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { updateHasClientSideId } from 'src/app/models/updateHasClientSideId';
import { User } from 'src/app/models/user';
import { OrgChartNode } from 'src/app/orgChatModule';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { FilterStateService } from 'src/app/services/filter-state.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { environment } from 'src/environments/environment';
import { ViewImageComponent } from '../view-image/view-image.component';
import { RestoreProjectPayload } from 'src/app/models/restoreProjectPayload';
import { LoaderService } from 'src/app/services/loader.service';
import { PaginationInstance } from 'ngx-pagination';
import { RMGDashboardProjectRequest } from 'src/app/models/rmgDashboardProjectRequest';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})

export class ResourceManagementComponent implements OnInit {

  @ViewChild("alert_message_modal") alertMessageTemplateRef: TemplateRef<any>;
  @ViewChild('project_configuration') projectConfigurationTemplateRef: TemplateRef<any>;
  @ViewChild('employee_details') employeeDetailsTemplateRef: TemplateRef<any>;
  @ViewChild('employee_project_timesheet_summary') employeeProjectTimesheetSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('expired_tnm_projects_summary') expiredTNMProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('fixed_cost_projects_summary') fixedCostProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('total_projects_summary') totalProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('team_project_status_summary') teamProjectStatusSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('project_details') projectDetailsTemplateRef: TemplateRef<any>;

  alertMessageModalRef: BsModalRef = new BsModalRef();
  projectConfigurationModalRef: BsModalRef = new BsModalRef();
  employeeDetailsModalRef: BsModalRef = new BsModalRef();
  employeeProjectTimesheetSummaryModalRef: BsModalRef = new BsModalRef();
  expiredTNMProjectsSummaryModalRef: BsModalRef = new BsModalRef();
  fixedCostProjectsSummaryModalRef: BsModalRef = new BsModalRef();
  totalProjectsSummaryModalRef: BsModalRef = new BsModalRef();
  projectDetailsModalRef: BsModalRef = new BsModalRef();
  teamProjectStatusSummaryModalRef: BsModalRef = new BsModalRef();

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
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
    , { field: 'effectiveStartDate', header: 'Effective Start Date', sortable: true, searchable: false }
  ];

  PROJECT_STATUS = {
    ALL: { key: 'ALL', label: 'All Projects', value: 'all', count: null, style: '',leftstyle: 'border-left:4px solid;color: #053885', i_class: 'fa-solid fa-plus-circle fa-beat-fade', type : 'All', color:'' },
    TOTAL: { key: 'TOTAL', label: 'Total Active Projects', value: 'total_projects', count: null, style: 'color: #03A9F4', leftstyle: 'border-left:4px solid;color: #03A9F4', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type : 'All', color:'' },
    TOTAL_FC: { key: 'TOTAL_FC', label: 'Total Fixed Cost', value: 'total_fixed_cost', count: null, style: 'color: #45556C', leftstyle: 'border-left:4px solid;color: #45556C', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type : 'Fixed Cost', color:'' },
    TOTAL_FILTER_FC: { key: 'TOTAL_FC', label: 'Total Fixed Cost', value: 'total_fixed_cost', count: null, style: 'color: #0e6dcd', bgstyle: 'background-color: #0e6dcd;color: #fff;', leftstyle: 'border-left:4px solid;color: #0e6dcd', i_class: 'fa-solid fa-chart-simple fa-beat-fade', color:'' },
    TOTAL_TNM: { key: 'TOTAL_TNM', label: 'Total TNM', value: 'total_tnm', count: null, style: 'color: #AD46FF', leftstyle: 'border-left:4px solid;color: #AD46FF', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type : 'TNM', color:'' },
    TOTAL_ACTIVE_TNM: { key: 'TOTAL_ACTIVE_TNM', label: 'TNM Active', value: 'active_tnm', count: null, style: 'color: #107503', leftstyle: 'border-left:4px solid;color: #107503', i_class: 'fa-solid fa-thumbs-up fa-beat-fade', color:'' },
    TOTAL_EXPIRED_TNM: { key: 'TOTAL_EXPIRED_TNM', label: 'All TNM Expired Projects', value: 'expired_tnm', count: null, style: 'color: #8B0000', bgstyle: 'background-color: #8B0000;color: #fff;', leftstyle: 'color: #8B0000; border-left: 4px solid', i_class: 'fa-solid fa-chart-simple fa-beat-fade', color:'' },
    TOTAL_MONITORING: { key: 'TOTAL_MONITORING', label: 'Total Monitoring', value: 'total_monitoring', count: null, style: 'color: #FE9A37', leftstyle: 'border-left:4px solid;color: #FE9A37', i_class: 'fa-solid fa-chart-simple fa-beat-fade', type : 'Monitoring', color:'' },
    TOTAL_INTERNAL: { key: 'TOTAL_INTERNAL', label: 'Total Internal', value: 'total_internal', count: null, style: 'color: #2D9966', leftstyle: 'border-left:4px solid;color: #2D9966', i_class: 'fa-solid fa-plus-circle fa-beat-fade',type : 'Internal', color:'' },
    PENDING_FOR_APPROVAL: { key: 'PENDING_FOR_APPROVAL', label: 'Pending for Approval', value: 'pending_for_approval', count: null, style: 'color: #FFB300', leftstyle: 'border-left:4px solid;color: #FFB300', i_class: 'fa-solid fa-clock fa-beat-fade', color:'#FFB300' },
    APPROVED: { key: 'APPROVED', label: 'Approved', value: 'approved', count: null, style: 'color: #4CAF50', leftstyle: 'border-left:4px solid;color: #4CAF50', i_class: 'fa-solid fa-check-circle fa-beat-fade', color:'#4CAF50' },
    REJECTED: { key: 'REJECTED', label: 'Rejected', value: 'rejected', count: null, style: 'color: #e6241a', leftstyle: 'border-left:4px solid;color: #e6241a', i_class: 'fa-solid fa-times-circle fa-beat-fade', color:'#e6241a' },
    NOT_STARTED: { key: 'NOT_STARTED', label: 'Not Started', value: 'not_started', count: null, style: 'color: #9E9E9E', leftstyle: 'border-left:4px solid;color: #9E9E9E', i_class: 'fa-solid fa-minus-circle fa-beat-fade', color:'#9E9E9E' },
    COMPLETED_IN_ISHINE: { key: 'COMPLETED_IN_ISHINE', label: 'Completed In iShine', value: 'completed_in_ishine', count: null, style: 'color: #17ceee', leftstyle: 'border-left:4px solid;color: #17ceee', i_class: 'fa-solid fa-circle-play fa-beat-fade', color:'#17ceee' },
    COMPLETED_IN_SHANKH: { key: 'COMPLETED_IN_SHANKH', label: 'Completed In Shankh', value: 'completed_in_shankh', count: null, style: 'color: #00ec33', leftstyle: 'border-left:4px solid;color: #00ec33', i_class: 'fa-solid fa-thumbs-up fa-beat-fade', color:'#00ec33' },
    COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE: { key: 'COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE', label: 'Completed In Shankh,Team Active', value: 'completed_in_shankh_but_team_active', count: null, style: 'color: #7836c4', leftstyle: 'border-left:4px solid;color: #7836c4', i_class: 'fa-solid fa-circle-play fa-beat-fade', color:'#7836c4' },
    UNFILLED_POSITIONS: { key: 'UNFILLED_POSITIONS', label: 'Unfilled Positions', value: 'unfilled_positions', count: null, style: 'color: #48aff8', leftstyle: 'border-left:4px solid;color: #48aff8', i_class: 'fa-solid fa-times-circle fa-beat-fade', color:'#48aff8' },
    TIMESHEET_NON_COMPLIANCE: { key: 'TIMESHEET_NON_COMPLIANCE', label: 'Unfilled Timesheet', value: 'timesheet_non_compliance', count: null, style: 'color: #e91e63', bgstyle: 'background-color: #e91e63;color: #fff;', leftstyle: 'border-left:4px solid;color: #e91e63', i_class: 'fa-solid fa-calendar-xmark fa-beat-fade', columnConfig: this.unfilledTimesheetProjectColumnConfig, defaultSortColumn: 'projectName', subTableColumnConfig: [] , color:'' },
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
    , this.PROJECT_STATUS.UNFILLED_POSITIONS
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
    , this.PROJECT_STATUS.COMPLETED_IN_ISHINE
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH
    , this.PROJECT_STATUS.COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE
    , this.PROJECT_STATUS.UNFILLED_POSITIONS
  ];

   TEAM2_PROJECT_STATUS_LIST = [
      this.PROJECT_STATUS.TOTAL_ACTIVE_TNM
  ];

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
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
    , { field: 'managerName', header: 'Manager Name', sortable: true, searchable: true }
    , { field: 'jobRoleName', header: 'Job Role', sortable: true, searchable: true }
  ];

  onBenchButProjectAssignedEmployeesColumnConfig = [
    { field: 'employmentIdAcToET', header: 'Employment Id', sortable: true, searchable: true }
    , { field: 'name', header: 'Employee Name', sortable: true, searchable: true }
    , { field: 'departmentName', header: 'Department', sortable: true, searchable: true }
    , { field: 'billable', header: 'Is Billable', sortable: true, searchable: true }
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
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
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
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
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
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
    , { field: 'billableType', header: 'Billable Type', sortable: true, searchable: true }
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
    this.EMPLOYEE_GROUPS.MAPPED_TO_INTERNAL,
    this.EMPLOYEE_GROUPS.MAPPED_TO_SHANKH,
    this.EMPLOYEE_GROUPS.MAPPED_TO_INTERNAL_AND_SHANKH,
    this.EMPLOYEE_GROUPS.NOT_MAPPED_TO_ANY_PROJECT,
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
    { key: "delays", label: 'Delayed', title: 'Delayed', count: null, color: '#5bc0de' },
    { key: "ontime", label: 'On Time', title: 'On Time', count: null, color: '#f0ad4e' },
  ];

  TIMESHEET_NON_COMPLIANCE_PROJECT_LIST = [
    { key: '3M', label: '3M', value: 3, unit: 'M', count: null },
    { key: '6M', label: '6M', value: 6, unit: 'M', count: null },
    { key: '1Y', label: '1Y', value: 1, unit: 'Y', count: null },
  ];

  feature = "Resource Management";
  selectedProjectStatus: string = '';
  selectedFCProjectFilter:any = this.FC_PROJECT_FILTERS_LIST[0].key;
  selectedExpiredTNMProjectFilter: any =  this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0].key;
  selectedUnfilledTimesheetProjectFilter: any =  this.TIMESHEET_NON_COMPLIANCE_PROJECT_LIST[0].key;
  
  expandedProjects: Set<string> = new Set();
  projectFullText: Map<string, string> = new Map(); 
  departmentIds: any[] = [];

  isTotalProjectsSummaryDepartmentFilterActive: boolean = false;


  isAccounts: boolean = false;

  rmgDashboardProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();

  employeeCtrl = new FormControl();
  teamLeadCtrl = new FormControl();
  spocCtrl = new FormControl();
  addMemberCtrl = new FormControl();
  teamMemberCtrl = new FormControl();

  
  allStates: any[] = ["Maharashtra"];

  employeeList: Employee[] = [];
  teamLeadsList: Employee[] = [];
  filteredEmployees: Employee[] = [];
  filteredTeamLeads: Employee[] = [];
  employeeListByDept: Employee[] = [];
  
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

  nodes: OrgChartNode[] = [];

  completedProjectsCount: number | null = null;
  isCountLoading: boolean = false;
  showReportList = false;
  reportListUrlSafe: SafeResourceUrl;
  isCollapsed: boolean = false;
  isCollapsedEmployee: boolean = false;
  hasNewTeamMembers: boolean = false;
  totalEmployees = 0;
  sankhMappedEmployees: any;
  sankhMappedEmployeesList: any[] = [];
  internalMappedEmployees: any;
  internalMappedEmployeesList: any[] = [];
  mappedToBothEmployees: any;
  mappedToBothEmployeesList: any[] = [];
  allMappedEmployees: any;
  exceptionEmployees: any;
  allMappedEmployeesList: any[] = [];
  exceptionEmployeesList: any[] = [];
  employeesWithoutProject: any;
  employeesWithoutProjectList: any[] = [];
  employeesWithoutBillability: any[] = [];
  employeesWithoutBillable: any;
  employeeData: any[] = [];
  catagory: any;
  selectedEmployee = '';
  selectedStatus = '';

  summaryModalRef: BsModalRef;
  projectSummaryData: any[] = [];

  selectedFile: File | null = null;
  selectedFilePreviewUrl: string | null = null;
  milestoneDocumentUrl: SafeResourceUrl | null = null;
  modalRef: BsModalRef = new BsModalRef();
  modalRefRole: BsModalRef = new BsModalRef();
  isModalFullscreen = false;

  isTNMCollapsed = false;

  notStartedCount:number=0;
  pendingForApprovalCount:number=0;
  approvedCount:number=0;
  completedInIshineCount:number=0;
  completedCount:number=0;
  completedWithEmployeeCount:number=0;
  currentPoProjectType: string | null = null;

  seempCountDeptSelected:any;
  searchEmployeeResultLen:any;
  notInListLen:any;
  employeeListNotInSearch:any[]=[];
  activeMetricView = "search";
   notInSearchPageNo = 1;
   filteredEmployeeListNotInSearch:any[]=[];
   employeeMatrixPageNo = 1;
   employeeMatrixPageSize = 3;
   totalEmployeeMatrixElements = 0;
   totalEmployeeMatrixPages = 0;
   paginationConfig: PaginationInstance = {
  id: 'employeeSkillMatrixId',
  itemsPerPage: 3,
  currentPage: 1,
  totalItems: 0
};

allEmployeeSkillSummary: { email: string; skillCount: number }[] = [];
topSkillCounts: number[] = [];

expandedIndex: boolean = false;

toggleExpand(): void {
  this.expandedIndex = !this.expandedIndex;
}


  // new cards changes.....................................................................
  
  @ViewChild("alertTemplateRole")
  alertTemplateRole: TemplateRef<any>;


  @ViewChild('alertTemplate') alertTemplateForMilestone!: TemplateRef<any>;

  @ViewChild("alert_message_sync")
  alertModalSync: TemplateRef<any>;

  @ViewChild("alert_message_without_reload")
  alertTemplateWithoutReload: TemplateRef<any>;

  @ViewChild("project_line_item_list_modal")
  projectLineItemListModal: TemplateRef<any>;

  @ViewChild("update_project_milestone_modal")
  updateProjectMilestoneModal: TemplateRef<any>;

  @ViewChild("update_project_milestone_success_modal")
  updateProjectMilestoneSuccessModal: TemplateRef<any>;

  @ViewChild("milestoneDocumentModal")
  milestoneDocumentModal: TemplateRef<any>;
  @ViewChild('update_project_completion_modal') updateProjectCompletionModal: TemplateRef<any>;
  updateProjectCompletionModalRef: BsModalRef;
  modalRefWithReloadMilestone: BsModalRef = new BsModalRef();

  @ViewChild('chartSection') 
  chartSection!: ElementRef;

  @ViewChild('MarkAsCompleteDefaultProject') MarkAsCompleteDefaultProject1!: TemplateRef<any>;
  @ViewChild('OtherProjectDefaultMapping') OtherProjectDefaultMapping1!: TemplateRef<any>;
  @ViewChild('customDatePickerTemplate') customDatePickerTemplate1!: TemplateRef<any>;
  @ViewChild('confirmCompleteTemplate') confirmCompleteTemplate!: TemplateRef<any>;
  confirmCompleteTemplateModalRef!: BsModalRef;
  @ViewChild('alert_message_without_reload') alert_message_without_reloadTemplate!: TemplateRef<any>;
  alert_message_without_reloadModalRef!: BsModalRef;
  @ViewChild('resource_removal_alert') removeResourceModal: TemplateRef<any>;
  removeResourceModalRef: BsModalRef;

  

  projectPageSize: number = 10;
  totalProjects: number = 0;
  totalProjectListPages: number = 0;
  rmgProjectFilterDTO: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
  searchOnEnter:boolean = true;

  data: string;
  currentUser: User;
  
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  alertMessage: any;
  alert_Message: any;
 

expiredProjects1To2Months: any;
expiredProjects2To3Months:any;
expiredProjects3To6Months:any;
expiredProjects6To9Months:any;
expiredProjects9To12Months:any;
expiredProjectsAbove12Months:any;
expiredProjectsWithin1Month:any;

  modalRef2: BsModalRef = new BsModalRef();
  modalRef1: BsModalRef = new BsModalRef();
  modalRef3: BsModalRef = new BsModalRef();
  modalRef4: BsModalRef = new BsModalRef();
  modalRef5: BsModalRef = new BsModalRef();
  modalRef6: BsModalRef = new BsModalRef();
  modalRefTeamMember: BsModalRef = new BsModalRef();
  projectLineItemListModalRef: BsModalRef = new BsModalRef();
  updateProjectMilestoneModalRef: BsModalRef = new BsModalRef();
  clientSideIdPresent : BsModalRef = new BsModalRef();
  modalRefWithReload: BsModalRef = new BsModalRef();
  modalRefWithoutReload: BsModalRef = new BsModalRef();
  modalRefWithoutReload2: BsModalRef = new BsModalRef();
  deleteResourceModalRef: BsModalRef = new BsModalRef();

  projectObj: Project = new Project();
  projectObj2: Project = new Project();
  dataObj: Employee = new Employee();
  teamObj: Team = new Team();
  employeeObj: Employee = new Employee();
  newteamMember: TeamMember = new TeamMember();
  isActive: any;
  temp: any
  lastDate: any;
  selectAll: boolean = false;
  selectedTeamsDetails: any[] = [];
  isProjectTable: boolean = false;
  isEditProject: boolean = false;
  isUpdation: boolean = false;
  isHideButton: boolean = false;
  allProjectTable: boolean = false;
  isAllPendingProjectAllowed: boolean = false;
  isHOD: boolean = false;
  isCreateForm: boolean = false;
  isCreation: boolean = false;
  isUpdateForm: boolean = false;

  currentTeam: any;
  selectedProjToReject: any;
  currentProjectId: any;
  selectedProjectManager: any;
  excelName: any;

  allProjectList: any[] = [];
  allDeptList: any[] = [];
  filteredDeptList: any[] = [];
  
  allTeamMembers: any[] = [];
  tempArrays: any[] = []
  allTeamList: any[] = [];
  updatedTeamList: any[] = [];
  teamCreatedProjectList: any[] = [];
  previewTeamList: any[] = [];
  allTeamListCopy: any[] = [];
  managerList: any[] = [];
  bulkSyncList: any[] = [];
  allClientList: any[] = [];
  filteredClientList: any[] = [];
  allClientLocationList: any[] = [];
  clientLocationList: any[] = [];
  internalProjectList: any[] = [];
  poPortalProjectList: any[] = [];
  allProject_Po_Internal: any[] = [];
  filteredDepartments: any[] = [];
  filteredDepartmentsInternal: any[] = [];
  filteredDepartmentsTeam: any[] = [];
  getBillableType: any;
  newMemberInProject: any;
  currentDepartment: any = []


  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  employeeNotInSearchColumns: any[] = ['employeementId','employeeName','deptName','jobRole','email','skillNames','certificateNames'];
  filters: any = {};
  SEfilters: any = {};
  isSearchEnabled: boolean = false;
  isSESearchEnabled: boolean = false;
  // projectColumns: any[] = ["blank", "draftStatus", "name", "poNo", "projectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "poStartDate", "poEndDate", "state", "createdOn", "status"];
  projectColumns: any[] = ["blank", "name", "poNo", "poProjectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "poStartDate", "poEndDate", "state", "createdOn", "status","projectStatus", "draftStatus"];

  projectDetails: any = [];
  projectDetails2: any = [];
  copyDepartment: any = [];
  currentBreadcrumbList: any[] = [];
  // employeesFor360: any[] = [];
  poProjectListFromIshine: any[] = [];
  flagDialogueBox: boolean = false;
  tableName: string;
 
  isAllSelected: boolean = false;
  tabCounts: any;
  searchQuery: any;
  selectedEmpId: any = 0;
  deptIdList: any[] = [];
  searchText: any;
  searchTextDept: any;
  @ViewChild('customDatePickerTemplate')
  customDatePickerTemplate!: TemplateRef<any>;

  selectedDate: String | null = null;
  completedProjectDetails: Project = new Project();
  // projectFilterDTO: any = new ProjectFilterDTO();
  deptList: ProjectFilterDTO = new ProjectFilterDTO();
  deptListUser: ProjectFilterDTO = new ProjectFilterDTO();
  departments: any[] = [];
  totalCount: any;
  projectRequirementsList: ProjectRequirements = new ProjectRequirements();
  selectedOtherProjectId: any;
  showSearchInput = true;
  teamMembers: TeamMember[] = [];
  newMember: TeamMember = new TeamMember;
  searchManagerText: any;
  filteredManagerList: any[] = [];
  isAllManagersSelected: boolean = false;
  selectedRequirement: any = null;
  
  employeeInformation: EmployeeInformation = new EmployeeInformation();
  loadingRequirements = true;
  projectCompletionDate: any;
  searchOverheadText: any;
  filteredOverheadList: any[] = [];
  isAllOverheadsSelected: boolean = false;
  overheadList: any[] = [];
  previousDefaultProjects: PreviousDefaultProject = new PreviousDefaultProject();
  filtered: any[] = [];
  filteredInternal: any[] = [];
  searchMappedProjectText: any;
  otherProjectList: any[] = [];
  filteredOtherProjectList: any[] = [];
  
  @ViewChild("previous_default_project")
  previousDefaultProject: TemplateRef<any>;

  @ViewChild("other_project_mappings")
  otherProjectMappings: TemplateRef<any>;
  searchTerm: any;
  defaultProjectUpdate: DefaultProjectUpdate = new DefaultProjectUpdate();
  defaultProjectUpdateEmpId: any;
  deletionDate: any;
  isBulkUpdateMode: boolean = true;
  projectListBulk: GetProjectDetailsForBulkDefaultUpdate = new GetProjectDetailsForBulkDefaultUpdate();
  filteredProjectsForDefaultBulkBench: any;
  defaultProjectUpdateBulk: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredProjectsForDefaultBulkOther: any;
  benchProjectListBulk: any;
  otherProjectListBulk: any;
  bulkProjectType: any;
  teamListBulk: any;
  searchTermTeam: any;
  filteredTeamsForDefaultBulk: any;
  resourceRequirementListBulk: any;
  bulkEmployeeList: EmployeeInformation[] = [];

  //added later

  designationData: string;   //Search Designation


  isLeaveReportTable: boolean = false;
  isTimesheetReportTable: boolean = false;
  isEmployeeReportTable: boolean = false;
  isAccessControlListTable: boolean = false;
  isLeaveTimesheetReportTable: boolean = false
  isCustomQueryForm: boolean = false;
  isAccessFeatureMapping: boolean = false;
  isDefaultFeatureMapping: boolean = false;

  //---------RAJ--------------------//
  isRoleSelectDisabled: boolean = true;
  isRequirementSelectDisabled: boolean = true;
  isCheckboxesDisabled: boolean = true;
  //--------------------------------//

  allEmployeeList: any[] = [];
  deptWiseConsolidated: any[] = [];

  allLeaveApplicationsList: any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList: any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

  allJobRoleList: any[] = [];
  personaWiseJobRole: any[] = [];
  accessControlList: any[] = [];
  mappedSubFeatureList: any[] = [];
  subfeatureList: any[] = [];
  defaultMappingList: any[] = [];
  defaultMappingListFilter: any[] = [];
  updateDefaultMapping: any[] = [];

  updatedRoleSubFeature: any[] = [];
  hiddenColumnObj: any[] = [];
  showColumnList: any[] = [];
  filteringTimesheet: FilteredTimesheet = new FilteredTimesheet();

  insideCols: any[] = [];

  storedDataList: any[] = [];

  // excelName: any;
  jobRoleName: any;
  departmentId: any;
  selectedProjectId: any;
  // employeeRole: any;
  selectedColumnToShow: any;
  departmentsList: any[] = [];

  leaveColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Leave Type', 'Team Name', 'Project Name', 'Client Name', 'Department', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department', 'Designation', 'Job Role', 'Manager', 'Team Name', 'Project Name', 'Po No', 'Po Start Date', 'Po End Date', 'Po Project Type', 'Client Name', 'Employment Status', 'Date Of Joining', 'Domain', 'Specialization', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On', 'Profile Completion'];
  timesheetColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By', 'Leave Type'];
  filteredTimesheetReportColumns: any[] = [
    'employeementId',
    'employeeType',
    'employeeName',
    'departmentName',
    'date',
    'dayType',
    'projectName',
    'teamName',
    'clientName',
    'clientLocation',
    'activity',
    'description',
    'managerName',
    'status',
    'totalTime',
    'officeInTime',
    'officeOutTime',
    'leaveType',
    'createdByName',
    'createdOn'
  ];

  queryList: any[] = [];

  columns: any[] = [];
  paginateData: any[] = [];
  pos: any;
  release: boolean = true;
  finalColumns: any[] = [];

  allLeaveTimesheets: any[] = [];
  endDate: any;
  startDate: any;
  employeeReportObj: employeeReport = new employeeReport();

  customQuery: any;
  leaveReportFlag: boolean = false;
  timesheetReportFlag: boolean = false;
  showDetails: boolean = false;
  showDetailsTimesheet: boolean = false;
  changeTable: boolean = true;
  totalProjectCount: number = 0;
  defaulterCount: number = 0;
  delayedCount: number = 0;
  ontTimeCount: number = 0;

  // filters: any = {};
  // isSearchEnabled: boolean = false;

  employeeReportColumnForDetailedProjectViewClub: any[] = ['blank', 'employeementId', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience', 'primaryProjectName'];
  employeeReportColumnForDetailedProjectView: any[] = ['blank', 'employeementId', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience'];
  leaveReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'noOfDays', 'reason', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'leaveStatusUpdatedByName'];
  timesheetReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'totalWorkingHours', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'leaveType', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  employeeReportColumn: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'projectName', 'poNo', 'poStartDate', 'poEndDate', 'poProjectType', 'clientName', 'billable', 'billableType', 'updatedOn', 'updatedByName', 'createdByName', 'createdOn'];
  // 'dateOfJoining', 'aadhar', 'aboutMe', 'address', 'permanentAddress', 'city', 'bloodGroup', 'dateOfBirth', 'gender', 'fatherName', 'panNumber', 'placeOfBirth', 'workLocation', 'probationPeriod', 'noticePeriod', 'country', 'totalExperience', 'emergencyContactMobile', 'emergencyContactPerson', 'landline', 'maritalStatus', 'motherTongue', 'alternateMobileNo', 'pincode', 'relation', 'state', 'viewsOnOrganisation', 'passportNumber', 'bankAccountNo', 'bankIFSCCode', 'bankName', 'pfAccountNumber', 'previousPfAccountNumber', 'uan', 'esicNumber', 'graduationType', 'pursuing', 'passingGrade', 'yearOfPassing',
  leaveTimesheetReportColumn: any[] = ['employeementId', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  defaultMappingColumns: any[] = ['tabName', 'featureName', 'subFeatureName'];
  employeeReportColumnForDetailedProjecttttView: any[] = ['blank',
    'projectName', 'projectManager', 'apmosysRM', 'clientRM',
    'poStartDate', 'poEndDate', 'poNo', 'poProjectType', 'teamName',
    'employeeName', 'jobRole', 'deptName', 'mobileNo', 'email',
    'billable', 'billableType', 'effectiveStartDate'
  ];

  employeesFor360: any[] = [];
  allEmployee: any[] = [];
  allProjectPOInternal: any[] = [];
  tnmPoExpiredCount = 0;
  tnmPOValidCount = 0;
  fixedCostPoExpiredCount = 0;
  fixedCostPoValidCount = 0;
  internalCount = 0;
  tnmProjectCount = 0;
  fixedCostProjectCount = 0;
  tnmPoProjectExpiredCount = 0;
  fixedCostPoProjectExpiredCount = 0;
  tnmPOProjectActiveCount = 0;
  fixedCostPoProjectActiveCount = 0;
  internalProjectCount = 0;
  selectedDepartment: string = 'All';
  flatProjectList: any[] = [];

  activeBox: string | null = null;
  isHovering: string | null = null;

  tnmPoExpiredCountList: any[] = [];
  tnmPOValidCountList: any[] = [];
  fixedCostPoExpiredCountList: any[] = [];
  fixedCostPoValidCountList: any[] = [];
  internalCountList: any[] = [];
  newemployeeObj: any;
  updatedEmpObj: any;
  show: number = -1;
  filteredTimesheets: any;
  toastr: any;
  isDeptFilter: boolean = false;
  dept: any;

  //added

  setDefaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
  projectSummary: {};
  projectList: any[];
  employeeBenchRepot: any[] = [];
  employeeBench: any;
  employeeBenchRepotMsg: any;
  deptId: any;
  searchTextDeptInternal: any;
  searchTextDeptTeam: any;
  isAllDeptSelected: boolean = false;
  departmentListByUser: any[] = [];
  deptIdListByUser: any[] = [];
  isAllSelectedByUser: boolean = false;
  searchTextDeptByUser: any;
  filteredDepartmentsByUser: any[] = [];
  myDept: boolean = false;
  countList: any;

  projectMilestoneSortDirection = 'asc';
  projectMilestoneSortColumn: any;
  projectMilestoneSortColumnType: any;
  projectMilestonepage = 1;
  milestonePanelState = true;
  fcProjectMilestoneList: FCProjectMilestone[] = [];
  statusList = [Status.NOT_STARTED, Status.IN_PROGRESS, Status.ON_HOLD, Status.COMPLETED];
  projectMilestone: any;
  fallBackMsg:any;
  isApproved:boolean = false;
  advanceFilter: any;
  skipSelectionChange: boolean = false;
  isLoadingMilestones: any=false;
   activeModalTab: 'info' | 'milestone' = 'info';
  teamMemberTemplate: any;
  preview_team_new_teams_only: TemplateRef<any>;
  delete_team_modal: TemplateRef<any>;
  MarkAsCompleteDefaultProject: TemplateRef<any>;
  OtherProjectDefaultMapping: TemplateRef<any>;
isCollapsed1: any;
  fcProjectList: any;
  notificationService: any;
  iscountLoading: boolean;
monitoringDefaulters: any;
monitoringCount: any;
tnmDefaulters: any;
fixedCostDefaulters: any;
unfilledTimesheetCounts: any;
  serviceResponse: any;


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
]

isSkillMatrix= false;
statusTab: any;

  clientSideIdObj: updateHasClientSideId = new updateHasClientSideId();
  projList:any[] = [];
  liftAndShiftObj = new LiftAndShift();
  @ViewChild('lift_and_shift_teams')
  liftAndShiftTeamsTemp!: TemplateRef<any>;
  liftAndShiftRef: BsModalRef = new BsModalRef();
  sourceProjectId:any;
  @ViewChild("alert_message_lift_shift")
  modalRefWithReloadTemp: TemplateRef<any>;
  hasClientSideIdFlag:Boolean=false;
  fetchClientSideIdObj:updateHasClientSideId = new updateHasClientSideId(); 
  @ViewChild("fcResourceMappedToTNMProject")
  fcResourceMappedToTNMProjectTemp: TemplateRef<any>;
  fcResourceMappedToTNMProjectRef: BsModalRef = new BsModalRef();
  @ViewChild("fcResourceMapped_no")
  fcResourceMapped_noTemp: TemplateRef<any>;
  fcResourceMapped_noRef: BsModalRef = new BsModalRef();
  @ViewChild("restore_info")
  restoreInfoTemp: TemplateRef<any>;
  restoreInfoRef: BsModalRef = new BsModalRef();
  restoreProjectPayload = new RestoreProjectPayload();
  @ViewChild("resource_alert")
  restoreAlertTemp: TemplateRef<any>;
  restoreAlertRef:BsModalRef = new BsModalRef();
  isRestoreSuccess: Boolean = false;
  @ViewChild("alertMessageMarkAsComplete")
  alertMessageMarkAsCompleteTemp: TemplateRef<any>;
  alertMessageMarkAsCompleteRef:BsModalRef = new BsModalRef();
  isCompletionSuccess: Boolean = false;

  @ViewChild("alertMEssageForPOResourceRequirementFetching")
  poResourceRequirementFetchTemp:TemplateRef<any>;
  poResourceRequirementFetchRef:BsModalRef = new BsModalRef();
  defaultImagePath = 'assets/Images/default-user-image.jpeg';

  constructor(
    private filterStateService: FilterStateService,
    private scroller: ViewportScroller,
    private departmentService: DepartmentService,
    public validationService: ValidationService,
    private employeeService: EmployeeService,
    private modalService: BsModalService,
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
    private dialog:MatDialog,
    private renderer: Renderer2,
    private appComponent: AppComponent,
    private el: ElementRef,
    private loaderService: LoaderService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const reportUrl = `${environment.baseUrl360}#/user-reports/report-list`;
    this.reportListUrlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(reportUrl);
  }

  async ngOnInit(): Promise<void> {
    this.myDept = true;
    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();
    this.isAccounts = deptName === 'Accounts';
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    this.selectedExpiredTNMProjectFilter = this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0].key;
    this.selectedFCProjectFilter = this.FC_PROJECT_FILTERS_LIST[0].key;
    this.rmgDashboardProjectRequest.projectStatus = this.PROJECT_STATUS.TOTAL.key;
    this.rmgDashboardProjectRequest.currentUserEmpId = this.currentUser.empId;
    this.rmgDashboardProjectRequest.currentUserType = this.determineUserType();

    this.mapSubFeatureFlag();

    this.route.params.subscribe(params => {
      this.rmgDashboardProjectRequest.projectStatus = this.rmgDashboardProjectRequest.projectStatus || this.PROJECT_STATUS.TOTAL.key;
    });

    this.route.params.subscribe((params: Params) => {
      this.currentProjectId = params['id'];
    });

    await this.getAllDepartmentsByCurrentUserIdAndRole();
    if (this.shouldFetchUserSpecificDepartments(deptName, empRole)) {
      await this.getDeptsByUser();
    }
    else {
      this.rmgDashboardProjectRequest.currentUserType = this.filteredDepartments != null ? 'ADMIN' : 'USER';
    }

    this.sectionViewInit();
    this.getEmployeeByNameAndEmpld();
    this.setAutocompleteControls();

    if (this.validationService.validateNullUndefinedEmptyList(this.currentBreadcrumbList) &&
      this.currentBreadcrumbList[this.currentBreadcrumbList?.length - 1]?.title.includes("Project")) {
      this.toggleSearch();
    }

    this.loadRMGDashboard(this.rmgDashboardProjectRequest);
    await this.getProjectDetailsForBulkDefaultUpdate();
    this.restoreFilterState();
  }

  private mapSubFeatureFlag() {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  private shouldFetchUserSpecificDepartments(deptName: string, empRole: string): boolean {
    const depts = ["Admin", "Resource Management Group", "Director", "Super Admin", "Accounts", "HR"];
    const roles = ["SuperAdmin", "Accounts"];
    return (!depts.some(dept => deptName.includes(dept)) &&
      !roles.some(role => empRole.includes(role)));
  }

  async getAllDepartmentsByCurrentUserIdAndRole(): Promise<any> {
    try {
      const response: any = await this.departmentService.getDeptsByRole(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch departments");
      }
      const serviceResponse = response.serviceResponse;
      this.departmentIds = [];
      this.setDepartmentLists(serviceResponse);
      this.onDepartmentToggle();
      return serviceResponse;
    } catch (error) {
      console.error("Error fetching departments:", error);
      throw error;
    }
  }

  async getDeptsByUser(): Promise<any> {
    try {
      const response: any = await this.departmentService.getDeptsByUser(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch user departments");
      }
      const serviceResponse = response.serviceResponse;
      this.departmentIds = [];
      this.setDepartmentListsUser(serviceResponse);
      this.onDepartmentToggle();
      return serviceResponse;
    } catch (error) {
      console.error("Error fetching user departments:", error);
      throw error;
    }
  }

  private setDepartmentLists(serviceResponse: any): void {
    this.deptList = serviceResponse;
    const departments = serviceResponse?.departments || [];
    this.departments = [...departments];
    this.deptIdList = [...departments];
    this.departmentsList = [...departments];
    this.filteredDepartments = [...departments];
    this.filteredDepartmentsTeam = [...departments];
  }

  private setDepartmentListsUser(serviceResponse: any): void {
    this.deptListUser = serviceResponse;
    const departments = serviceResponse?.departments || [];
    this.deptIdListByUser = [...departments];
    this.departmentListByUser = [...departments];
    this.filteredDepartmentsByUser = [...departments];
  }

  private filterEmployees(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.employeeList.filter(emp => {
      const name = emp?.name?.toString() || '';
      const employmentId = emp?.employmentId?.toString() || '';
      return name.toLowerCase().includes(lowerText) ||
        employmentId.toLowerCase().includes(lowerText);
    });
  }

  getEmployeeByNameAndEmpld() {
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response?.serviceResponse || [];
        this.teamLeadsList = this.employeeList;
        this.filteredEmployees = this.employeeList;
        this.filteredTeamLeads = this.employeeList;
      } else {
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
      }
    });
  }

  setAutocompleteControls() {
    this.employeeCtrl?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value?.name || ''),
      map(name => this.filterEmployees(name))
    ).subscribe(filtered => {
      this.filteredEmployees = filtered;
    });

    this.teamLeadCtrl?.valueChanges.pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value?.name || ''),
      map(name => this.filterTeamLeads(name))
    ).subscribe(filtered => {
      this.filteredTeamLeads = filtered;
    });
  }

  sectionViewInit(): void {
    const role = this.currentUser.employeeRole?.trim() || '';
    this.isHOD = role === 'HOD' || role === 'SuperAdmin';
    if (this.userMapping?.view_all_rmg_projects || (this.currentProjectId != undefined && this.currentProjectId != null)) {
      this.showViewProjects();
    }
  }

  onProjectStatusSelect(status: string, projectStatusList: any) {
    this.page = 1;
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

  private restoreFilterState(): void {
    const fs = this.filterStateService;
    if (fs?.projectReportFilters) {
      this.filters = fs.projectReportFilters;
      this.isSearchEnabled = true;
    }
    if (this.validationService.validateNullUndefinedEmptyList(fs?.deptIdList)) {
      this.myDept = fs.myDept;
      this.deptIdList = fs.deptIdList;
      this.onDepartmentSelectionChange();
    }
    if (this.validationService.validateNullUndefinedEmptyList(fs?.deptIdListByUser)) {
      this.myDept = fs.myDept;
      this.deptIdListByUser = fs.deptIdListByUser;
      this.onDepartmentSelectionChangeByUser();
    }
    if (fs?.selectedProjectStatus) {
      this.selectedProjectStatus = fs.selectedProjectStatus;
      // this.onProjectStatusSelect(this.selectedProjectStatus,);
    }
  }

  loadRMGDashboard(rmgDashboardProjectRequest: any) {
    this.isRestoreSuccess = false;
    this.getAllEmployeeGroupCount();
    this.getAllProjectStatusCount();
    this.fetchProjectDetailsList(rmgDashboardProjectRequest);
  }

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
    this.TEAM2_PROJECT_STATUS_LIST.forEach(project => {
      this.getProjectStatusCount(project.key, this.TEAM2_PROJECT_STATUS_LIST);
    });
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
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
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
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
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
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
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
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
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
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
      });
  }

  applyFilters() {
    const selectedIds = this.deptIdList.filter(id => id !== 'all');
    this.departmentsList = selectedIds;
    this.departmentIds = selectedIds;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
    newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  toggleDept(): void {
    this.myDept = !this.myDept;
    this.filterStateService.myDept = this.myDept;
  }

  toggleSelectAllDepartment() {
    if (this.isAllSelected) {
      this.deptIdList = [];
    } else {
      this.deptIdList = [...this.filteredDepartments.map(dept => dept.deptId), 'all'];
    }
    this.isAllSelected = !this.isAllSelected;
    this.applyFilters();
  }

  toggleSelectAllDeptartmentByUser() {
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;

    this.filterStateService.deptIdListByUser = this.deptIdListByUser;
    if (this.isAllSelectedByUser) {
      this.departmentIds = [];
      this.deptIdListByUser = [];
    } else {
      this.deptIdListByUser = this.filteredDepartmentsByUser.map(dept => dept.deptId);
      this.departmentIds = this.deptIdListByUser;
    }
    this.isAllSelectedByUser = !this.isAllSelectedByUser;
    newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  filterDepartments() {
    const lowerText = this.searchTextDept.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  filterDepartmentsByUser() {
    const lowerText = this.searchTextDeptByUser.toLowerCase();
    this.filteredDepartmentsByUser = this.departmentListByUser.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  clearDepartmentSelection(event: Event) {
    event.stopPropagation();
    if (this.isAllSelected == true) {
      this.toggleSelectAllDepartment();
    }
    else {
      this.deptIdList = [];
      this.departmentIds = [];
      this.filterStateService.deptIdList = [];
      this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
      let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
      newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
      newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
      this.loadRMGDashboard(newRmgDashboardProjectRequest);
    }
    this.searchTextDept = '';
    this.filterDepartments();
  }

  clearDepartmentSelectionByUser(event: Event) {
    event.stopPropagation();
    if (this.isAllSelectedByUser == true) {
      this.toggleSelectAllDeptartmentByUser();
    }
    else {
      this.departmentIds = [];
      this.deptIdListByUser = [];
      this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
      let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
      newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
      newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
      this.filterStateService.deptIdListByUser = this.deptIdListByUser;
      this.loadRMGDashboard(newRmgDashboardProjectRequest);
    }
  }

  onDepartmentSelectionChange() {
    this.skipSelectionChange = false
    if (this.skipSelectionChange) {
      return;
    }
    if (!this.isAllSelected && this.validationService.validateNullUndefinedEmptyList(this.deptIdList)) {
      this.filterStateService.myDept = this.myDept;
    }
    this.filterStateService.deptIdList = this.deptIdList;
    this.departmentIds = this.deptIdList;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
    newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  onDepartmentSelectionChangeByUser() {
    if (this.skipSelectionChange) {
      return;
    }
    if (!this.isAllSelectedByUser && this.validationService.validateNullUndefinedEmptyList(this.deptIdListByUser)) {
      this.filterStateService.myDept = this.myDept;
    } else {
      this.filterStateService.deptIdList = this.deptIdList;
    }
    this.filterStateService.deptIdListByUser = this.deptIdListByUser;
    this.departmentIds = this.deptIdListByUser;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
    newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  onDepartmentToggle() {
    const deptIds: number[] = (!this.myDept) ? this.deptIdList.map(dept => dept.deptId) : this.deptIdListByUser.map(dept => dept.deptId);
    this.departmentIds = deptIds;
  }

  updateSelectedDepartments(department: string, event: any): void {
    const isChecked = event.target.checked;
    if (isChecked) {
      this.selectedDepartments = [department];
    } else {
      this.selectedDepartments = [];
    }
    this.getTotalProjectsChartData();
  }

  onEmployeeInputChange() {
    if (!this.employeeCtrl.value || this.employeeCtrl.value.trim() === '') {
      this.selectedEmpId = 0;
    }
  }

  onEmployeeSelected(event: any) {
    const selectedEmp = event.option.value;
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      this.employeeCtrl.setValue(selectedEmp.name);
    }
  }

  getNewRMGRequestObject() {
    let newRmgDashboardProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    newRmgDashboardProjectRequest.currentUserEmpId = this.currentUser.empId;
    newRmgDashboardProjectRequest.currentUserType = this.determineUserType();
    newRmgDashboardProjectRequest.departmentIds = this.departmentIds;
    return newRmgDashboardProjectRequest;
  }

  openEmployeeProjectTimesheetSummaryModal(selectedEmpId: any) {
    this.selectedEmpId = selectedEmpId;
    this.employeeProjectTimesheetSummaryModalRef = this.modalService.show(this.employeeProjectTimesheetSummaryTemplateRef, { class: 'modal-lg' });
    this.getEmployeeProjectTimesheetSummaryData();
  }

  closeEmployeeProjectTimesheetSummaryModal() {
    if (this.employeeProjectTimesheetSummaryModalRef) {
      this.employeeProjectTimesheetSummaryModalRef.hide();
    }
  }

  openExpiredTNMProjectsSummaryModal() {
    this.expiredTNMProjectsSummaryModalRef = this.modalService.show(this.expiredTNMProjectsSummaryTemplateRef, { class: 'modal-lg' });
    this.getExpiredTNMProjectSummaryData();
  }

  closeExpiredTNMProjectsSummaryModal() {
    if (this.expiredTNMProjectsSummaryModalRef) {
      this.expiredTNMProjectsSummaryModalRef.hide();
    }
  }

  openTotalProjectsSummaryModal(projectStatus: any) {
    if (projectStatus === this.PROJECT_STATUS.TOTAL_INTERNAL.key || projectStatus === this.PROJECT_STATUS.ALL.key) {
      return;
    }
    this.selectedProjectStatus = projectStatus;
    this.isTotalProjectsSummaryDepartmentFilterActive = false;
    this.totalProjectsSummaryModalRef = this.modalService.show(this.totalProjectsSummaryTemplateRef, { class: 'modal-xl' });
    this.prepareAndFetchTotalProjectsSummaryChartData();
  }

  closeTotalProjectsSummaryModal() {
    if (this.totalProjectsSummaryModalRef) {
      this.totalProjectsSummaryModalRef.hide();
    }
  }

  openTeamProjectStatusSummaryModal() {
    this.teamProjectStatusSummaryModalRef = this.modalService.show(this.teamProjectStatusSummaryTemplateRef, { class: 'modal-lg' });
    this.getTeamProjectStatusSummaryChartData();
  }

  closeTeamProjectStatusSummaryModal() {
    if (this.teamProjectStatusSummaryModalRef) {
      this.teamProjectStatusSummaryModalRef.hide();
    }
  }

  openFixedCostProjectsSummaryModal() {
    this.fixedCostProjectsSummaryModalRef = this.modalService.show(this.fixedCostProjectsSummaryTemplateRef, { class: 'modal-lg' });
    this.getFixedCostProjectSummaryData();
  }

  closeFixedCostProjectsSummaryModal() {
    if (this.fixedCostProjectsSummaryModalRef) {
      this.fixedCostProjectsSummaryModalRef.hide();
    }
  }

  openEmployeeDetailsModal(employeeGroup: any) {
    if (employeeGroup.key == this.EMPLOYEE_GROUPS.TOTAL.key) {
      return;
    }
    this.employeeDetailsExtraParams = { "employeeGroupKey": employeeGroup?.key, "selectedDeptIds": this.departmentIds, "projectStatus": this.selectedProjectStatus || this.PROJECT_STATUS.ALL.key, "expiredProjectFilter": this.selectedExpiredTNMProjectFilter, "fixedCostFilter": this.selectedFCProjectFilter };
    this.employeeDetailsColumnConfig = employeeGroup?.columnConfig;
    this.employeeDetailsDefaultSortColumn = employeeGroup?.defaultSortColumn;
    this.employeeDetailsSubTableColumnConfig = employeeGroup?.columnConfig
    this.employeeDetailsModalRef = this.modalService.show(this.employeeDetailsTemplateRef, { class: 'modal-xl' });
  }

  closeEmployeeDetailsModal() {
    if (this.employeeDetailsModalRef) {
      this.employeeDetailsModalRef.hide();
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

    this.projectDetailsExtraParams = { "selectedDeptIds": this.departmentIds, "projectStatus": this.selectedProjectStatus, "fromDate": fromDate, "toDate": toDate };
    this.projectDetailsColumnConfig = projectStatus?.columnConfig;
    this.projectDetailsDefaultSortColumn = projectStatus?.defaultSortColumn;
    this.projectDetailsSubTableColumnConfig = projectStatus?.columnConfig
    this.projectDetailsModalRef = this.modalService.show(this.projectDetailsTemplateRef, { class: 'modal-xl' });
  }

  closeProjectDetailsModal() {
    if (this.projectDetailsModalRef) {
      this.projectDetailsModalRef.hide();
    }
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

  getEmployeeProjectTimesheetSummaryData() {
    if (!this.selectedEmpId) {
      this.openAlertMod(this.alertTemplateWithoutReload, "Cannot fetch summary. User information is missing.");
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
        this.openAlertMod(this.alertMessageTemplateRef, "An error occurred while fetching summary data.");
        this.showMessageInEmployeeProjectTimesheetSummary('A server error occurred. Please try again later.', true);
      }
    });
  }

  showMessageInEmployeeProjectTimesheetSummary(message: any, isError: any) {
    if (isError) {
      document.getElementById('projectTimesheetSummaryChart').innerHTML = `<p class="text-center text-danger">${message}</p>`;
    } else {
      document.getElementById('projectTimesheetSummaryChart').innerHTML = `<p class="text-center">${message}</p>`;
    }
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

  getTotalProjectsChartData() {
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.type = this.PROJECT_STATUS[this.selectedProjectStatus].type || this.PROJECT_STATUS.TOTAL.key;
    newRmgDashboardProjectRequest.departmentNames = this.selectedDepartments?.length > 0 ? this.selectedDepartments : null;
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
    newRmgDashboardProjectRequest.fixedCostFilter = this.selectedFCProjectFilter;
    newRmgDashboardProjectRequest.expiredProjectFilter = this.selectedExpiredTNMProjectFilter;

    this.projectService.getTotalProjectsChartData(newRmgDashboardProjectRequest).pipe(first()).subscribe((res: any) => {
      if (res.serviceStatus === "Success") {
        this.serviceResponse = res.serviceResponse || [];
        this.processDataForOrgChart();
      } else {
        this.serviceResponse = [];
        this.orgChartData = [];
      }
    });
  }

  prepareAndFetchTotalProjectsSummaryChartData() {
    let filteredDepts = this.poDepartments;
    if (this.departmentIds?.length > 0) {
      const deptIdSet = new Set(this.departmentIds);
      const allowedDeptNames = this.allDepartments
        .filter(dept => deptIdSet.has(dept.deptId))
        .map(d => d.name.toLowerCase().trim());

      filteredDepts = this.poDepartments.filter(po => {
        const subDepts = po.name.split(",").map(d => d.toLowerCase().trim());
        return subDepts.some(sd => allowedDeptNames.includes(sd));
      });
    }

    this.allDeptList = filteredDepts
      .map(dept => ({ name: dept.name, deptab: dept.deptab }))
      .sort((a, b) => a.name.localeCompare(b.name));
    this.filterError = null;
    this.selectedDepartments = [];
    this.getTotalProjectsChartData();
  }

  toggleTotalProjectsSummaryDepartmentFilter() {
    this.isTotalProjectsSummaryDepartmentFilterActive = !this.isTotalProjectsSummaryDepartmentFilterActive;
  }

  processDataForOrgChart() {
    const departmentMap = new Map<string, Map<string, any[]>>();
    const dataToProcess = this.serviceResponse;
    dataToProcess.forEach(item => {
      const { deptAb, clientName, projectName } = item;
      if (!departmentMap.has(deptAb)) {
        departmentMap.set(deptAb, new Map<string, any[]>());
      }
      const clientMap = departmentMap.get(deptAb)!;
      if (!clientMap.has(clientName)) {
        clientMap.set(clientName, []);
      }
      const projects = clientMap.get(clientName)!;
      projects.push({
        name: projectName,
        cssClass: 'project-node'
      });
    });
    this.orgChartData = Array.from(departmentMap.entries()).map(([deptAb, clientMap]) => {
      return {
        name: deptAb,
        cssClass: 'department-node',
        childs: Array.from(clientMap.entries()).map(([clientName, projects]) => {
          return {
            name: clientName,
            cssClass: 'client-node',
            childs: projects
          };
        })
      };
    });
    this.processDataForTable();
  }

  processDataForTable() {
    this.expandedProjects.clear();
    this.projectFullText.clear();
    const departmentMap = new Map<string, Map<string, any[]>>();
    const dataToProcess = this.serviceResponse;

    dataToProcess.forEach(item => {
      const { deptAb, clientName, projectName } = item;

      if (!departmentMap.has(deptAb)) {
        departmentMap.set(deptAb, new Map<string, any[]>());
      }
      const clientMap = departmentMap.get(deptAb)!;

      if (!clientMap.has(clientName)) {
        clientMap.set(clientName, []);
      }
      const projects = clientMap.get(clientName)!;
      projects.push(projectName);
    });

    const departments = Array.from(departmentMap.keys());
    const tableColumns: any[][] = [];
    departments.forEach(dept => {
      const clientMap = departmentMap.get(dept)!;
      const deptColumn: any[] = [dept];

      Array.from(clientMap.entries()).forEach(([clientName, projects]) => {
        deptColumn.push({ type: 'client', name: clientName });

        projects.forEach(project => {
          deptColumn.push({ type: 'project', name: project });
        });
      });

      tableColumns.push(deptColumn);
    });

    this.maxRows = Math.max(...tableColumns.map(col => col.length));

    tableColumns.forEach(column => {
      while (column.length < this.maxRows) {
        column.push('');
      }
    });
    this.tableData = [];
    for (let i = 0; i < this.maxRows; i++) {
      const row: any[] = [];
      tableColumns.forEach(column => {
        row.push(column[i] || '');
      });
      this.tableData.push(row);
    }
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

  selectExpiredTNMProjectFilter(expiredTNMProjectStatusObj: any, filter: any) {
    this.selectedExpiredTNMProjectFilter = filter.key;
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL_EXPIRED_TNM.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = expiredTNMProjectStatusObj?.key;
    newRmgDashboardProjectRequest.expiredProjectFilter = filter.key;
    this.fetchProjectDetailsList(newRmgDashboardProjectRequest);
    this.getAllEmployeeGroupCount();
  }

  selectFCProjectFilter(fcProjectStatusObj: any, filter: any) {
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

  loadRMGDashboarTemp() {
    this.selectedProjectStatus = this.PROJECT_STATUS.TOTAL.key;
    let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
    newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
    this.loadRMGDashboard(newRmgDashboardProjectRequest);
  }

  exportProjectDetailsToExcel(): void {
    this.excelName = "Project Report.xlsx";
    let rmgProjectRequest = this.getNewRMGRequestObject();
    rmgProjectRequest.page = 0;
    rmgProjectRequest.pageSize = 100000;
    rmgProjectRequest.projectFilter = this.filters;
    rmgProjectRequest.sortColumn = this.sortColumn || 'name';
    rmgProjectRequest.sortDirection = this.sortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.sortColumnType || 'string';
    rmgProjectRequest.departmentIds = this.departmentIds;
    rmgProjectRequest.projectStatus = this.selectedProjectStatus;
    rmgProjectRequest.expiredProjectFilter = this.selectedExpiredTNMProjectFilter;
    rmgProjectRequest.fixedCostFilter = this.selectedFCProjectFilter;

    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList)) {
        const apiResponse = response?.serviceResponse?.projectList?.content || [];
        if (!apiResponse?.length) {
          this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse || 'No data to export');
          return;
        }
        apiResponse.forEach(project => {
          project.createdOn = (project?.createdOn) ? moment(project?.createdOn).format('DD/MM/yyyy') : null;
        });
        const exportData = apiResponse.map(x => ({
          'Project Name': x.name || '',
          'PO Number': x.poNo || '',
          'Project Type':x.poProjectType ? x.poProjectType : (x.internalProjectType ? x.internalProjectType : 'NA'),
          'Project Manager': x.projectManagers && x.projectManagers.length > 0 ? x.projectManagers[0].projectManagerName : '',
          'Client': x.clientName || '',
          'Apmosys RM': x.apmosysRM || '',
          'Client RM': x.clientRM || '',
          'Start Date': x.poStartDate || '',
          'End Date': x.poEndDate || '',
          'State': x.state || '',
          'Created On': x.createdOn || '',
          'PO Project Status': x.status || '',
          'Project Status': x.projectStatus || '',
          'Approval Status' : x.draftStatus || ''
        }));
        this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMod3(this.alertTemplateWithoutReload, 'Something went wrong');
      });
  }

  showViewProjects() {
    this.isProjectTable = true;
    this.allProjectTable = true;
    this.isSkillMatrix = false;
    this.isHideButton = false;
    this.isEditProject = false;
    this.isCreateForm = false;
    this.isCreation = false;
    this.filters = {};
    this.isSearchEnabled = false;
    this.allProjectList = [];
    this.teamCreatedProjectList = [];
  }




  showEditProjectForm(project: any) {
    this.isEditProject = true;
    this.allProjectTable = true;
    this.isHideButton = true;
    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
    this.getManagerList();
  }

  closeEditProject() {
    this.isHideButton = false;
  }

  showCreateForm() {
    this.isCreateForm = true;
    this.isCreation = true;
    this.isSkillMatrix= false;

    this.isProjectTable = false;
    this.allProjectTable = false;
    this.isUpdateForm = false;


    this.projectObj = new Project();
    this.getAllDepartmentListForCreateProject();
    this.getManagerList();
    this.getAllClientList();
    this.projectObj.clientId = '';
  }

  getAllProjects() {
    fetch(this.currentUser.poPortalAllProjectApi).then(res => res.json()).then(async data => {
      let allPoProject = data;
      //console.log("allPoProject    V  allPoProject   ",allPoProject);
      this.resourceManagementService.getInternalProject().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.internalProjectList = response.serviceResponse;

          let _projectList = [...allPoProject, ...this.internalProjectList];

          if ((_projectList != null || _projectList != undefined) && (this.teamCreatedProjectList != null || this.teamCreatedProjectList != undefined)) {

            // sort according to dateTime
            _projectList = _projectList.sort((a, b) => (new Date(a.createdOn).getTime() < new Date(b.createdOn).getTime()) ? 1 : -1);

            _projectList.sort((a) => {
              if (a.isTeamCreated && a.isDraftProject == 'Pending For Approval') {
                return -1;
              } else {
                return 1;
              }
            });

            // Separate PoPortal projects and internal projects
            this.poPortalProjectList = _projectList.filter(proj => proj.id != null);
            this.internalProjectList = _projectList.filter(proj => proj.id == null);
            //console.log(" Anurag   ::    ",this.poPortalProjectList);
            //console.log(" Anurag internal   ::   ",this.internalProjectList);
            //console.log(" teamCreatedProjectList   ",this.teamCreatedProjectList);

            // Process PoPortal projects
            this.poPortalProjectList.forEach((proj) => {
              let selectedProj = this.teamCreatedProjectList.find((projTeam) => proj.id == projTeam.poProjectId);
              //console.log("selectedProj  ::   ",selectedProj);

              if (selectedProj) {
                proj.isTeamCreated = true;
                proj.isDraftProject = selectedProj.isActive == 2 ? 'Pending For Approval' : selectedProj.isDraftProject == 'true' ? 'Pending For Approval' : selectedProj.isDraftProject == 'Rejected' ? 'Rejected' : selectedProj.isDraftProject == 'false' ? 'Approved' : "NA";
                proj.isActive = selectedProj.isActive;
                proj.createdOn = (proj.createdOn) ? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
                proj.projectViewId = selectedProj.projectId;
              } else {
                proj.isTeamCreated = false;
                proj.isDraftProject = "NA";
                proj.createdOn = (proj.createdOn) ? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
              }

              if (proj.status != null) {
                if (proj.status == "true") {
                  proj.status = "InProgress";
                } else if (proj.status == "false") {
                  proj.status = "Completed";
                } else {
                  proj.status = proj.status;
                }
              }
            });

            // Process internal projects
            this.internalProjectList.forEach((proj) => {
              let selectedProj = this.teamCreatedProjectList.find((projTeam) => proj.projectId == projTeam.projectId);
              // console.log("Priyadarshini", proj.projectId, " ", proj.projectName);

              if (selectedProj) {
                proj.isTeamCreated = true;
                proj.isDraftProject = selectedProj.isDraftProject == 'true' ? 'Pending For Approval' : selectedProj.isDraftProject == 'Rejected' ? 'Rejected' : selectedProj.isDraftProject == 'false' ? 'Approved' : "NA";
                // proj.isDraftProject = proj.projectType;
                proj.createdOn = (proj.createdOn) ? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
                proj.projectViewId = selectedProj.projectId;
              } else {
                proj.isTeamCreated = false;
                proj.isDraftProject = proj.projectType;
                proj.createdOn = (proj.createdOn) ? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
              }

              if (proj.status != null) {
                if (proj.status == "true") {
                  proj.status = "InProgress";
                } else if (proj.status == "false") {
                  proj.status = "Completed";
                } else {
                  proj.status = proj.status;
                }
              }
            });

            this.allProjectList = _projectList;

            // Navigate to Project when used link
            if (this.currentProjectId != undefined || this.currentProjectId != null) {
              this.allProjectList.forEach((proj) => {
                if (proj.id == this.currentProjectId) {
                  proj.isEditProject = true;
                  this.showEditProjectForm(proj);
                }
              });
            }
          }
          console.log(this.internalProjectList, " this.internalProjectList");


          console.log("  allProject_Po_Internal   ", this.allProject_Po_Internal);
          console.log("this.currentUser.departmentName", this.currentUser.departmentName)
        } else {
          console.error(response.serviceResponse);
        }

      });
    });
  }
  
  alreadyCreatedTeam() {
    this.resourceManagementService.alreadyCreatedTeam().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamCreatedProjectList = response.serviceResponse;
        this.getAllProjects();
        //console.log(this.teamCreatedProjectList, " this.teamCreatedProjectList");
      } else {
        this.getAllProjects();
        console.error(response.serviceResponse);
      }
    });
  }

  
  getAllDepartmentList(project: any) {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
        this.filteredDepartments = [...this.allDeptList];
        console.log("allDeptList : ", this.allDeptList)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllResourceRequirementForProject(project: Project) {
    this.resourceManagementService.getAllResourceRequirementForProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       
        this.projectObj.resourceRequirements = response.serviceResponse || [];
     
      }
    });
  }

  // Team
  getSubstring(str: string): string {
    return str.substring(0, 5); // or any logic you want
  }
  getAllEmployeesByRole(departmentList: any) {
    this.teamLeadsList = [];
    let employeeList = [];
    this.employeeObj.role = this.currentUser.employeeRole;


    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        this.teamLeadsList = employeeList.filter(x => departmentList?.includes(x.departmentId));
        this.teamLeadsList = this.teamLeadsList.sort((a, b) => a.name.localeCompare(b.name));
        this.filteredTeamLeads = [...this.teamLeadsList];
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllEmployeesByDepartmentIds(departmentList: any) {
    this.employeeListByDept = [];

    let empObj = new Employee();
    empObj.departmentList = departmentList?.map(deptId => {
      let dept = new Department();
      dept.deptId = deptId;
      return deptId;
    });

    //console.log("empObj.departmentList : ", empObj.departmentList);

    this.employeeService.getAllEmployeesByDepartmentIds(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListByDept = response.serviceResponse;
        this.employeeListByDept = this.employeeListByDept.sort((a, b) => a.name.localeCompare(b.name));
        console.log("employeeList By Department : ", this.employeeListByDept);
        // this.updateEmployeeListAccordingToTeamMembers();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  updateEmployeeListAccordingToTeamMembers() {
    this.employeeListByDept.forEach((employee, index) => {
      const existingEmployee = this.teamObj.allTeamMemberList?.find(member => member.empId == employee.empId);
      if (existingEmployee) {
        employee.isSelected = true;
      } else {
        const existingEmployee = this.allTeamMembers.find(member => member.empId == employee.empId);
        if (existingEmployee) {
          employee.isSelected = true;
        }
      }
    });

    console.log("updateEmployeeListAccordingToTeamMembers this.allTeamMembers ",this.allTeamMembers);
  }

  setTeamLead(teamLeadId) {
    let teamLead: any = [];
    teamLead = this.teamLeadsList?.find(x => x.empId == teamLeadId);
    if (teamLead) {
      this.allTeamList?.forEach((team: any) => {
        if (team.teamName == this.currentTeam.teamName) {
          if (team.teamMemberList) {
            teamLead.isTeamLead = true;
            team.teamMemberList = team.teamMemberList.filter(x => x.isTeamLead != true);
            //console.log(team.teamMemberList, " : team memeber list");
            team.teamMemberList = [...team.teamMemberList, ...[teamLead]];
          } else {
            teamLead.isTeamLead = true;
            team.teamMemberList = [teamLead];

            //console.log(team.teamMemberList, " : team memeber list ===");
          }
        }
      });
    }
  }

  addTeamMemberMapping() {
    // console.log("Hii addTeamMemberMapping   ");
    // console.log(" this.allTeamList   ",this.allTeamList)
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == this.currentTeam.teamName) {
        // console.log("team.teamMemberList  ",team.teamMemberList)
        if (team.teamMemberList) {
          if (this.allTeamMembers.length) {
            // console.log("this.allTeamMembers.length   ",this.allTeamMembers.length);
            team.teamMemberList = [...team.teamMemberList, ...this.allTeamMembers];
            console.log("team.teamMemberList   ", team.teamMemberList);
          } else {
            team.teamMemberList = [...team.teamMemberList];
          }
        } else {
          team.teamMemberList = (this.allTeamMembers.length !== 0) ? this.allTeamMembers : null;
        }
      }
    });
    this.hideTeamMemberModal();
    this.hasNewTeamMembers = false;
  }

  checkTeamName(template: TemplateRef<any>, team: any, teamIndex: any) {
    if (this.projectObj.isTeamCreated == "true" || this.projectObj.isTeamCreated == true) {
      let teamObj = new Team();
      teamObj.teamId = team.teamId;
      teamObj.teamName = team.teamName;
      teamObj.projectName = this.projectObj.name;

      //console.log(teamObj, " : teamObj");


      this.teamService.checkTeamName(teamObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Fail") {
          this.allTeamList.forEach((presentTeam, index) => {
            if (index == teamIndex) {
              presentTeam.teamName = '';
            }
          });
          this.openAlertMod6(template, response.serviceResponse);
        } else {
          this.allTeamListCopy.forEach((teamCopy) => {
            if (teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)) {

              this.allTeamList.forEach((presentTeam, index) => {
                if (index !== teamIndex) {
                  presentTeam.teamName = '';
                  this.openAlertMod6(template, "Team Name already exists!!");
                }
              });
            }
          });
        }
      });
    } else {

      this.allTeamListCopy.forEach((teamCopy) => {
        if (teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)) {

          this.allTeamList.forEach((presentTeam, index) => {
            if (index !== teamIndex) {
              presentTeam.teamName = '';
              this.openAlertMod6(template, "Team Name already exists!!");
            }
          });
        }
      });
    }
  }

  validateProjectObj(projectObj, template: TemplateRef<any>) {
    if (projectObj.projectManagerId == undefined || projectObj.projectManagerId.length == 0 || projectObj.projectManagerId == null) {
      this.alertMessage = `Please select project manager.`
      this.openAlertMod6(template, this.alertMessage);
      return;
    }

    if (projectObj.teamList.length == 0) {
      this.alertMessage = "Please add atleast one team."
      this.openAlertMod6(template, this.alertMessage);
      return false;
    }

    if (projectObj.teamList.length != 0) {
      let flag = true;
      projectObj.teamList.forEach((projObj, index) => {
        projObj.teamName = projObj.teamName?.trim();
        if (!this.validationService.validateNullUndefinedEmptyString(projObj.teamName)) {
          this.alertMessage = `Please enter Team Name - ${index + 1}.`
          flag = false;
          return;
        }
        if (!this.validationService.validateTeamName(projObj.teamName)) {
          this.alertMessage = "Please enter valid Team Name."
          this.openAlertMod6(template, this.alertMessage);
          return false;
        }

        // if (projObj.departmentId == undefined || projObj.departmentId.length == 0 || projObj.departmentId == null) {
        //   this.alertMessage = `Please select Team's department - ${index + 1}.`
        //   flag = false;
        //   return;
        // }

        if (projObj.teamMemberList == undefined || projObj.teamMemberList.length == 0 || projObj.teamMemberList == null) {
          this.alertMessage = `Please add team member(s) - ${index + 1}.`
          flag = false;
          return;
        }

        if (projObj.teamMemberList.length != 0) {
          let memberFlag = true;
          projObj.teamMemberList.forEach((member) => {
            if (!this.validationService.validateNullUndefinedEmptyString(member.name)) {
              this.alertMessage = `Please select Team Member- ${index + 1}.`
              flag = false;
              return;
            }

            if (member.isTeamLead == null && (member.isTeamLead != true || member.isTeamLead != 'true')) {
              if (member.employeeRole == undefined || member.employeeRole.length == 0 || member.employeeRole == null) {
                this.alertMessage = `Please select member(s) Employee Role - ${index + 1}.`
                flag = false;
                //console.log(projObj.teamMemberList, " : projObj.teamMemberList");

                return;
              }
            }
          });
          if (!memberFlag) {
            this.openAlertMod6(template, this.alertMessage);
            return false;
          } else {
            return true;
          }
        }
      });
      if (!flag) {
        this.openAlertMod6(template, this.alertMessage);
        return false;
      } else {
        return true;
      }
    }
  }

  createDraftProjectInfo(template2: TemplateRef<any>) {
    if (this.validationService.validateNullUndefinedEmptyList(this.allTeamList)) {
      this.projectObj.createdBy = this.currentUser.empId;
      this.projectObj.projectType = this.projectObj.poProjectType ?? "Internal";

      this.allTeamList?.forEach(team => {
        if (team.spoc) {
          team.spocId = team?.spoc?.empId;
        }

        if (this.validationService.validateNullUndefinedEmptyList(team.teamMemberList)) {
          team.teamMemberList.forEach(member => {
            member.shadowEmpId = member?.shadow?.empId ?? null;
          });
        }
      });
      this.projectObj.teamList = this.allTeamList;

      let inputValidated: boolean = this.validateProjectObj(this.projectObj, template2);
      if (!inputValidated) return;

      this.projectObj.isHOD = this.isHOD === true;
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          if (this.isHOD) {
            this.openAlertMessageModal(response?.serviceResponse);
            this.allTeamMembers = [];
            this.closeProjectConfigurationEditModal();
          } else {
            this.openAlertMessageModal(response?.serviceResponse);
            this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((approvalRes: any) => {
              if (approvalRes.serviceStatus === "Success") {
                this.allTeamMembers = [];
                this.closeProjectConfigurationEditModal();
              }
            });
          }
        } else {
          this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong.');
        }
      });
    } else {
      this.openAlertMessageModal("There are currently no teams to be set...!");
    }
  }

  formatDateArrayToString(dateArray: number[]): string {
  if (!dateArray || dateArray.length < 6) return null;

  const [year, month, day, hour, minute, second] = dateArray;

  // Pad with leading zeros for consistency
  const pad = (n: number) => String(n).padStart(2, "0");

  return `${year}-${pad(month)}-${pad(day)}T${pad(hour)}:${pad(minute)}:${pad(second)}`;
}

  getTeamListByProjectName(project: any) {
    // this.previewTeamList = [];
    console.log(project.updatedOn, " : project.updatedOn");
    // const data = this.formatDateArrayToString(project.updatedOn);
    // project.updatedOn = data
    //console.log(" project    ",project);
    project.active = null;
    this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj, "projectofthisteam");
        console.log(this.projectObj.teamList, " this.projectObj.teamList");

        this.projectCompletionDate = this.projectObj.poEndDate ? moment(this.projectObj.poEndDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD');
        console.log(this.projectCompletionDate, " this.projectObj.endDtae");
        this.projectObj.teamList.forEach((obj) => { 
          obj.departmentList = obj.departmentList?.map(x => +x);
          this.copyDepartment = obj.departmentList;
          // this.getAllEmployeesByRole(this.copyDepartment);

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                member.emp360 = member.empId;
                member.shadowControl = new FormControl(member.shadow || null);
              }
            });
          } else {
            console.warn('teamMemberList is null or undefined');
          }
        });
        //console.log(this.projectObj.teamList, " this.projectObj.teamList");
        this.previewTeamList = this.projectObj.teamList;

        //console.log(" length of previewTeamList  ",this.previewTeamList.length);
        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          //console.log(" find error in else part ")
          this.allTeamList = this.projectObj.teamList;
          console.log("all team list is :--",this.allTeamList);
          // this.allTeamListCopy = this.projectObj.teamList;
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
        }
        this.fallBackMsg = '';
        this.isApproved = false;
      } else {
        console.error(response.serviceResponse);

        if (response.serviceStatus == "Fail") {
          this.fallBackMsg = "All teams are inactive for this project";
          if (project.draftStatus == "Approved")
            this.isApproved = true;
        }

        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          this.allTeamList = this.projectObj.teamList;
                    console.log("all after project team list is :--",this.allTeamList);
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
        }
      }
    });
    // this.getManagerList();
    // console.log("teamObj.teamLeadId ",this.teamObj.teamLeadId);
    // console.log("teamLeadsList ",this.teamLeadsList);

  }

  onApproveProject(project: any) {
    project.empId = this.currentUser.empId;
    // project.projectId = this.projectObj.projectId;
    console.log("this.projectObj.projectId   ", this.projectObj.projectId);
    console.log("this.projectObj.projectId   ", project.projectId);
    if (project.poProjectType != null) {
      project.projectType = project.poProjectType;
      project.isDraftProject= "false";
    } else {
      project.projectType = "Internal";
    }
    this.resourceManagementService.approvePendingProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showViewProjects();
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
        // this.showPendingForApprovalProject();
      } else {
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
      }
    });
  }

  onRejectProject(project: any) {

    let projObj = this.selectedProjToReject;
    projObj.rejectReason = project.rejectReason;
    projObj.empId = this.currentUser.empId;
    if (project.poProjectType != null) {
      project.projectType = project.poProjectType;
    } else {
      project.projectType = "Internal";
    }
    this.resourceManagementService.rejectPendingProject(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showViewProjects();
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
        // this.showPendingForApprovalProject();
      } else {
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
      }
    });
  }

  async getManagerList() {
    this.managerList = [];

    this.employeeObj.role = "Manager";
    // if( this.employeeObj.isConsultant == 'true' ){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(5)
    // }else{
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    // }
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    try {
      const response: any = await this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).toPromise();
      if (response.serviceStatus === "Success") {
        this.managerList = response.serviceResponse;
        this.overheadList = response.serviceResponse;

        this.managerList.forEach((emp) => {
          emp.employeementId = "A-".concat(emp.employeementId);
        });
        this.overheadList.forEach((emp) => {
          emp.employeementId = "A-".concat(emp.employeementId);
        });
        this.filteredManagerList = this.managerList;
        this.filteredOverheadList = this.overheadList;
      } else {
        console.error(response.serviceResponse);
      }
    } catch (error) {
      console.error("Error fetching manager list:", error);
    }
  }

  onSelectProjectForSync(project, event) {
    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkSyncList.push(project);
    } else {
      event.target.classList.remove('checked');
      const projectCheckboxes = document.querySelectorAll('.sync-project-checkbox.checked');
      this.bulkSyncList.forEach((projObj, index) => {
        if (projObj == project) this.bulkSyncList.splice(index, 1);
      });
    }
    //console.log("Updated Bulk List : ", this.bulkSyncList);
  }

  onBulkSyncProject(template: TemplateRef<any>) {

    let projObj = new Project();
    projObj.bulkSyncList = this.bulkSyncList;

    //console.log(projObj, " : this.projObj");

    this.resourceManagementService.bulkSyncProject(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showViewProjects();
        this.openAlertMod(template, response.serviceResponse);
        this.bulkSyncList = [];
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  setManagerName(projectObj: any) {
    this.selectedProjectManager = '';
    const managerFound = this.managerList.find(x => x.employeementId == projectObj.projectManager);
    if (managerFound) {
      this.selectedProjectManager = managerFound.name;
      //this.empId=managerFound.empId;
    }
  }

  getAllClientList() {
    this.allClientList = [];

    this.projectService.getAllClients().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allClientList = response.serviceResponse;

        //remove duplicate clients
        this.filteredClientList = this.allClientList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.clientId === value.clientId
          ))
        )

        //console.log(this.filteredClientList, " : this.filteredClientList");
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDepartmentListForCreateProject() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.filteredDepartmentsInternal = [...this.allDeptList];
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getClientLocationList(clientId: any) {
    this.clientLocationList = [];
    this.allClientLocationList = [];

    const key = "clientLocationId";
    this.clientLocationList = [...new Map(this.allClientList.map((project: Project) => [project[key], project])).values()].filter((project: Project) => {
      if (project.clientId == clientId) {
        return { clientLocationId: project.clientLocationId, clientLocation: project.clientLocation }
      }
    });
    //console.log("clientLocationList :", this.clientLocationList);
  }

  checkProjectName(template: TemplateRef<any>) {
    let projectObj = new Project();
    projectObj.projectName = this.projectObj.projectName?.trim();

    if (projectObj.projectName.length >= 5) {
      if (!this.validationService.validateProjectName(projectObj.projectName)) {
        this.alertMessage = "Please enter valid Project Name !!"
        this.projectObj.projectName = '';
        this.openAlertMod6(template, this.alertMessage);
        return false;
      }

    } else {
      this.alertMessage = "Please enter more than 4 letters in Project Name !!"
      this.projectObj.projectName = '';
      this.openAlertMod6(template, this.alertMessage);
      return false;
    }
    this.projectService.checkProjectName(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.projectObj.projectName = '';
        this.openAlertMod6(template, response.serviceResponse);
      }
    })
  }

  validateCreateProjectObj(projectObj: Project, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectName)) {
      this.alertMessage = "Please enter Project name !!"
      this.openAlertMod3(template, this.alertMessage);
      return false;
    }
    // if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.clientId)) {
    //   this.alertMessage = "Please select a client !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectManagerId)) {
    //   this.alertMessage = "Please select project manager !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.departmentList)) {
      this.alertMessage = "Please select department !!"
      this.openAlertMod3(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.state)) {
      this.alertMessage = "Please select state !!"
      this.openAlertMod3(template, this.alertMessage);
      return false;
    }
    return true;
  }

  createProject(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateCreateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    // this.projectObj.departmentList = this.projectObj.departmentName;
    // this.projectObj.departmentName = null;
    this.projectObj.projectName = this.projectObj.projectName?.trim();
    this.projectObj.createdBy = this.currentUser.empId;
    //console.log("     :   ",this.projectObj);

    this.projectService.createProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewProjects();
      } else {
        this.openAlertMod3(template, response.serviceResponse);
      }
    });
  }

  // Manage team & teamMemberList
  deletefiled:boolean=false;
  addInputTeamField() {
    this.deletefiled=true;
    let newTeamObj = new Team();
    this.allTeamList.push(newTeamObj);
    this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
    this.copyDepartment = [];
    //console.log(this.allTeamList, " : this.allTeamList");
  }



  tempTeam = new Project();
  projectdetails1: any[] = [];
  openDeleteModalForTeam(template: TemplateRef<any>, alert_message: TemplateRef<any>, teamObj, project) {
    this.tempTeam = teamObj;
    this.projectdetails1 = project;

    if (!this.tempTeam.teamId) {
      // this.openAlertMod(alert_message, "Team is missing. Cannot proceed.");
      this.allTeamList.pop();
      this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
      this.copyDepartment = [];
      if (!this.allTeamList || this.allTeamList.length === 0) {
        this.addInputTeamField();
      }
      return;
    } else {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    }


  }

  isBulkDelete: boolean = false;
  isBulkDeleteResource: boolean = false;
  isBulkDeleteResourceFromTNMProjects: boolean = false;
  openDeleteModalForTeamBulk(template: TemplateRef<any>, alert_message: TemplateRef<any>, teamObj, project, template1: TemplateRef<any>, template2: TemplateRef<any>,) {
    this.isBulkDelete = true;
    this.isBulkDeleteResource = false;
    this.isBulkDeleteResourceFromTNMProjects = false;
    this.currentProjectDetails = project.projectId;
    this.tempTeam = teamObj;
    this.projectdetails1 = project;
    const empIds: number[] = this.selectedTeamsDetails.reduce((acc: number[], team: any) => {
      const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

      members.forEach(member => {
        if (
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length === 0 &&
          member.isDefaultProject === 1
        ) {
          acc.push(member.empId);
        }
      });

      return acc;
    }, []);

    const empIdsHavingActiveProjects: number[] = this.selectedTeamsDetails.reduce((acc: number[], team: any) => {
      const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

      members.forEach(member => {
        if (
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length !== 0 &&
          member.isDefaultProject === 1
        ) {
          acc.push(member.empId);
        }
      });

      return acc;
    }, []);
    this.activeProjects = empIdsHavingActiveProjects;
    this.EmployessIds = empIds;
    if (empIds.length !== 0 && empIdsHavingActiveProjects.length !== 0) {
      this.getEmployeeInformationBulk(empIds);
      this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
      this.setDefaultProjectObj.empIds = empIdsHavingActiveProjects;
      this.setDefaultProjectObj.projectId = project.projectId;
      this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
      this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });

    } else if (empIds.length !== 0 && empIdsHavingActiveProjects.length === 0) {
      this.getEmployeeInformationBulk(empIds);
      this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
    } else if (empIds.length === 0 && empIdsHavingActiveProjects.length !== 0) {
      this.setDefaultProjectObj.empIds = empIdsHavingActiveProjects;
      this.setDefaultProjectObj.projectId = project.projectId;
      this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
      this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });
    } else {
      this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
    }

    console.log("tesmp", this.projectdetails1);
    this.getProjectDetailsForBulkDefaultUpdate();
  }





  tempArray: any[] = [];
  addTeamMember() {
    // 1. Basic Validation: Ensure an employee is selected from the dropdown.
    if (!this.newteamMember || !this.newteamMember.empId) {
      // You can add a user-friendly message here (e.g., using a toast service)
      console.error("No employee selected.");
      return; 
    }

    // 2. Uniqueness Check: Verify if the employee is already in the 'allTeamMembers' list.
    const isAlreadyAdded = this.allTeamMembers.some(member => member.empId === this.newteamMember.empId);

    if (isAlreadyAdded) {
      // Inform the user that this member is already added.
      // this.toastService.warning('This team member has already been added.', 'Duplicate');
      console.warn('This team member has already been added.');
      return; // Stop the function here.
    }

    // 3. Find the full employee object from the master list.
    const employeeData = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);
    if (!employeeData) {
      console.error("Could not find employee data for the selected ID.");
      return;
    }

    // 4. Create the new team member object with all required properties.
    const memberToAdd = {
      ...employeeData, // Copy all base properties from the master list
      employeeRole: this.newteamMember.employeeRole || [], // Ensure it's an array
      isShadow: this.newteamMember.isShadow ? 1 : 0,
      isDefaultProject: this.newteamMember.isDefaultProject ? 1 : 0,
      teamName: this.currentTeam.teamName,
      // Add resourceOverviewId only if a requirement was selected
      resourceOverviewId: this.selectedRequirement ? this.selectedRequirement.resourceOverviewId : null,
    };
    
    // 5. Add the new member to the array.
    this.allTeamMembers.push(memberToAdd);
    this.hasNewTeamMembers = true;

    // 6. Reset form controls for the next entry.
    this.newteamMember = new TeamMember(); 
    this.addMemberCtrl.reset();
    this.selectedRequirement = null;
    this.teamMemberCtrl.reset();
    
    // This is a crucial step to update the UI list (see Step 2 below)
    this.updateEmployeeListAccordingToTeamMembers();
  }
  removeTeamMember(teamMember, index) {
    const currentTeam = this.currentTeam;
    this.allTeamList?.forEach((team) => {
      if (team.teamName == currentTeam.teamName) {
        team.teamMemberList.splice(index, 1);

        this.teamObj.allTeamMemberList.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });
  }
  isEmployeeAllocated(employeeId: number): boolean {
    // Check if the employee is in the "New Team Members" list being built
    const isinNewMembers = this.allTeamMembers.some(member => member.empId === employeeId);
    
    // Check if the employee is already in the "Existing Team Members" list for this team
    const isinExistingMembers = this.teamObj?.allTeamMemberList?.some(member => member.empId === employeeId);

    return isinNewMembers || isinExistingMembers;
}

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember) {
        this.allTeamMembers.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });

  }
  //---------Raj(addButton)-------------//

  resetTeamMemberForm() {
    this.selectedRequirement = null;
    this.newteamMember = new TeamMember();
    // this.teamMemberCtrl.setValue('');
  }
errorMessage:any;

addTeamMember1(): boolean {
  // Hierarchical validation - each step must be completed in order
  console.log("this.projectObj.resourceRequirements=================", this.projectObj);
  // Step 1: Check requirement selection first (if requirements exist)
  if(this.projectObj.resourceRequirements?.length==0 ){
    this.errorMessage= "Please add requirements before adding team members.";
    return;
  }

  //second step
  //


  //service call
}
isAddButtonDisabled(): boolean {
  // console.log("this.projectObj.resourceRequirements=================", this.projectObj);
  // Step 1: Check requirement selection first (if requirements exist)

  if (this.projectObj.resourceRequirements?.length > 0 && 
      (this.selectedRequirement == null || this.selectedRequirement == undefined)
    ) {
      return true;
    }

    // Step 2: Check employee selection (only after requirement is selected)
    if (!this.newteamMember.empId) {
      return true;
    }

    // Step 3: Check role selection (only after employee is selected)
    if (!this.newteamMember.employeeRole?.length) {
      return true;
    }

    // Step 4: Additional business rule validation
    if (this.projectDetails.length !== 0 && this.newteamMember.billableType === 'TNM') {
      return true;
    }

    return false;
  }

 
  getValidationErrorMessage(): string {
    // Step 1: Check requirement selection first (if requirements exist)
    // if (this.projectObj.resourceRequirements?.length > 0 && (!this.selectedRequirement || this.selectedRequirement === '' || this.selectedRequirement === null || this.selectedRequirement === undefined)) {
    //   return "Please select Requirement";
    // }

    console.log("this.selectedRequirement", this.selectedRequirement);

    // Step 2: Check employee selection (only show this error after requirement is selected)
    if (!this.newteamMember.empId) {
      return "Please select Employee";
    }

    // Step 3: Check role selection (only show this error after employee is selected)
    if (!this.newteamMember.employeeRole?.length) {
      return "Please select Role";
    }


    // Step 4: Additional business rule validation
    if (this.projectDetails.length !== 0 && this.newteamMember.billableType === 'TNM') {
      return "TNM billable type is not allowed with existing project details";
    }

    return "";
  }

  handleAddButtonClick() {
    const errorMessage = this.getValidationErrorMessage();
    if (errorMessage) {

      this.openAlertModRole(this.alertTemplateRole, errorMessage);
    } else {
      this.addTeamMember();
      this.updateEmployeeListAccordingToTeamMembers();
      
      // this.resetTeamMemberForm();

    }

    console.log("this.allTeamMembers ",this.allTeamMembers);
  }
   closeModalRole() {
    if (this.modalRefRole) {
      this.modalRefRole.hide();
    }
  }

  openTeamMemberModal(template: TemplateRef<any>, currentTeam) {
    this.allTeamMembers = [];
    this.teamObj.teamLeadId = '';
    this.newteamMember.empId = '';
    this.newteamMember.employeeRole = null;

    this.resetTeamMemberForm();
    // this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    // this.getAllEmployeesByRole(currentTeam.departmentList);
    console.log("this.copyDepartment ", currentTeam);
    // this.getAllEmployeesByDepartmentIds(this.copyDepartment);
    // this.resetTeamMemberForm();
    this.getAllEmployeesByDepartmentIds(this.deptIdList);
    this.getAllEmployeesByRole(this.copyDepartment);
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == currentTeam.teamName) {
        let teamLeadObj = team.teamMemberList?.filter(x => x.isTeamLead == true || x.isTeamLead == "true");
        if (teamLeadObj) {
          this.teamObj.teamLeadId = teamLeadObj[0]?.empId;
        }
        //console.log(teamLeadObj, " :teamLeadObj");
        //console.log(this.teamObj.teamLeadId, " : this.teamObj.teamLeadId");

        console.log("team.teammemberList ", this.teamObj);

        this.teamObj.allTeamMemberList = team.teamMemberList?.filter(x => x.isTeamLead != true);
        if ((this.teamObj.allTeamMemberList != undefined || this.teamObj.allTeamMemberList != null) && this.teamObj.allTeamMemberList.length != 0) {
          this.isUpdation = true;
        } else {
          this.isUpdation = false;
        }
      }
    });
    //console.log(this.teamObj.allTeamMemberList, " allTeamMemberList");

    this.currentTeam = currentTeam;
    this.modalRef1 = this.modalService.show(template, { class: 'modal-lg' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.alertMessage = message;
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

   openAlertModRole(template: TemplateRef<any>, message: any) {
    this.modalRefRole = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  

  openAlertMod3(template: TemplateRef<any>, message: any) {
    this.modalRef3 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openShowCreateForm(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openAlertModWithReload(template: TemplateRef<any>, message: any) {
    this.modalRefWithReload = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequestWithReload() {
    this.modalRefWithReload.hide();
    this.hideLiftAndShiftTeamsMod();
    this.cancelRequest1();
    this.cancelRequest();
  }

  cancelRequest() {
    console.log("cancel call");

    this.modalRef.hide();
    this.modalRef1.hide();
    this.hideTeamMemberModal();
  }
  cancelRequestRole(){
    this.modalRef.hide();
  }

  cancelRequest5() {
    this.modalRef5.hide();
  }

    cancelRequest10() {
    this.modalRef5.hide();
  }
  
  cancelRequest1() {
    console.log("cancel call ");

    this.modalRef1.hide();
  }

  cancelRequest2() {
    console.log("cancel call ");

    this.modalRef2.hide();
  }

  cancelRequest3() {
    console.log("cancel call 3");
    
    this.modalRef3.hide();
  }
  cancelRequest6() {

    console.log("cancel call 3");
     this.alert_message_without_reloadModalRef.hide(); 
    // this.modalRef6.hide();
    this.cancelRequestWithoutReload();
  }
cancelRequest7() {
     this.modalRef6.hide();
  }
  
  openAlertMod6(template: TemplateRef<any>, message: any) {
    this.modalRef6 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  previewTeamModal(template: TemplateRef<any>, teamObj: any, projectObj: any) {
    this.selectedProjectManager = '';
    this.previewTeamList = [];
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == teamObj.teamName) {
        this.previewTeamList = [team];
      }
    });
    if (this.previewTeamList.length === 0) {
      console.warn('No team members available for this team.');
      return;
    }
    if (projectObj.projectManager != null) {
      this.setManagerName(projectObj);
    }
    //console.log(this.previewTeamList, " : this.previewTeamList");

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openProjectPreviewModal(template: TemplateRef<any>, project: any) {
    this.isHideButton = false
    this.allProjectList.forEach((proj) => {
      proj.isEditProject = false;
    });
    this.previewTeamList = [];
    this.projectObj = null;
    this.resourceManagementService.getProjectConfigurationDetailsByProjectId(project?.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj = response.serviceResponse;
        this.mapProjectManagerNameToProject(this.projectObj);
        this.setManagerName(this.projectObj);
        this.getTeamListByProjectName(this.projectObj);
        this.getManagerList();
        this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
      }
    });
  }

  cancelRequestWithoutReload() {
    this.modalRefWithoutReload.hide();
  }


  public getDisplayProjectStatus(project: any): string {
  if (project.draftStatus === 'Approved' && project.status != 'NA' && project.status != 'Pending') {
    return 'In Progress';
  }
  else if (project.status === 'Pending') {
    return 'Not Started';
  } 
  return project.projectStatus ? project.projectStatus : 'NA';
}

  openRejectModal(template: TemplateRef<any>, projectObj: any) {
    this.selectedProjToReject = projectObj;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  //pagination 

  page = 1;
  
  handlePageChange(event) {
    this.page = event;
    this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
      this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
    }
  }

  onSearch(searchData: any) {
    this.page = 1;
    this.filters = searchData;
    // Save the current filter state to the service
    this.filterStateService.projectReportFilters = this.filters;
    this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
      this.filterStateService.clearProjectReportFilters();
      this.fetchProjectDetailsList(this.rmgProjectFilterDTO);
    }
  }


  openMenuIndex: number | null = null;

  toggleMenu(index: number) {
    this.openMenuIndex = this.openMenuIndex === index ? null : index;
  }

  closeMenu(index: number) {
    if (this.openMenuIndex === index) {
      this.openMenuIndex = null;
    }
  }

  onAction(action: string, project: any) {
    console.log('Action:', action, 'on Project:', project);
    this.openMenuIndex = null;
    // handle your logic here...
  }


  // Excel Export

  //   exportToExcel(id:any) {
  //     this.excelName = 'Projects.xlsx';

  //     const onlySpecificDataArr = this.allProjectList.map(
  //       x => ({
  //         "Project Name": x.name,
  //         "PO Number": x.poNo,
  //         "Project Manager": x.projectManagerName,
  //         "Client Name": x.clientName,
  //         "Client State": x.clientState,
  //         "Apmosys RM": x.apmosysRM,
  //         "Client RM": x.clientRM,
  //         "Start Date": x.startDate ? x.startDate.split(/[\sT]/)[0].split('-').reverse().join('-') : x.startDate,
  // "End Date": x.endDate ? x.endDate.split(/[\sT]/)[0].split('-').reverse().join('-') : x.endDate,
  //         // "Start Date": x.startDate,
  //         // "End Date": x.endDate,
  //         "Created On": x.createdOn,
  //         "Approval Status": x.isDraftProject,
  //         "Project Status": x.status,
  //       })
  //     )
  //     this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
  //   }


  //  exportToExcel(id: any): void {
  //     this.excelName = "Project Report.xlsx";
  //     this.tableName = 'Team Members';

  //     this.exportExcelService.exportTableDataToExcel(this.allProject_Po_Internal, this.excelName);
  // }

  exportToExcel(id: any): void {
    this.excelName = "Project Report.xlsx";
    this.tableName = 'Team Members';

    // Transform data to match the table columns
    const exportData = this.allProject_Po_Internal.map(x => ({
      'Actions': '', // Add appropriate action text or leave empty
      'Approval Status': x.status,
      'Project Name': x.name || '',
      'PO Number': x.poNo || '',
      'Project Type': x.projectType || '',
      'Project Manager': x.projectManagers && x.projectManagers.length > 0
        ? x.projectManagers[0].projectManagerName
        : '',
      'Client': x.clientName || '',
      'Apmosys RM': x.apmosysRM || '',
      'Client RM': x.clientRM || '',
      'Start Date': x.poStartDate || '',
      'End Date': x.poEndDate || '',
      'State': x.state || '',
      'Created On': x.createdOn || '',
      'Project Status': x.projectStatus || ''
    }));

    this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
  }


  // check resource template

  getExistingProjectsByUser(employeeId: string): Promise<any[]> {
    return new Promise((resolve, reject) => {
      console.log("Fetching project details for employee ID:", employeeId);

      let projectObj = new Project();
      projectObj.empId = employeeId;

      this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe(
        (response: any) => {
          if (response.serviceStatus === "Success") {
            this.projectDetails = response.serviceResponse;
            this.projectDetails = this.projectDetails.map(project => ({ ...project,startDate: project.startDate ? new Date(project.startDate) : null,
            updatedOn: project.updatedOn ? new Date(project.updatedOn) : null
            }));

            if (this.projectDetails.length > 0) {
              if (this.projectDetails[0].billableType === "TNM") {
                this.openAlertMod6(
                  this.alertTemplateWithoutReload,
                  "This Employee is already mapped to TNM project. Can't add to another project or Team !!"
                );
                this.getBillableType = this.projectDetails.find(
                  (employee) => (this.newteamMember.billableType = employee.billableType)
                );
              } else {
                this.newMemberInProject = "NewMember";
                this.newteamMember.billableType = this.newMemberInProject;
              }
            }else {
              this.newMemberInProject = "NewMember";
              this.newteamMember.billableType = this.newMemberInProject;
            }

            resolve(this.projectDetails);
          } else {
            console.error("Failed to fetch project details:", response);
            reject(response);
          }
        },
        (error) => {
          console.error("Error in service call:", error);
          reject(error);
        }
      );
    });
  }


  deleteResourceFromProject(template: TemplateRef<any>) {
    let projectObj = new Project();
    projectObj.teamId = this.projectObj2.teamId;
    projectObj.empId = this.projectObj2.empId;
    projectObj.endDate = this.lastDate1;

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedMembers = [];
        this.openAlertMod(template, response.serviceResponse);
        this.getExistingProjectsByUser(this.projectObj2.empId)
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })

  }


 deleteResourceFromProject2(template: TemplateRef<any>) {
    let projectObj = new Project();
    projectObj.teamId = this.projectObj2.teamId;
    projectObj.empId = this.projectObj2.empId;
    projectObj.endDate = this.lastDate1;

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedMembers = [];
        this.openModalRefWithoutReload2(template, response.serviceResponse);
        this.getExistingProjectsByUser(this.projectObj2.empId)
      } else {
        this.openModalRefWithoutReload2(template, response.serviceResponse);
      }
    })

  }


    openModalRefWithoutReload2(template: TemplateRef<any>, message: any) {
    this.modalRefWithoutReload2 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

 openProjectTemplateModal(template: TemplateRef<any>, employee: any) {
    this.getExistingProjectsByUser(employee.empId).then((projectDetails) => {
      this.dataObj = employee;

      if (projectDetails.length > 0) {
        this.page = 1;
        this.modalRef2 = this.modalService.show(template, { class: 'modal-xl' });
      } else {
        console.log("No project details found for the employee.");
      }

      console.log("data employee newmenbfcg  ", employee);
    }).catch((error) => {
      console.error("Error fetching project details:", error);
    });
  }

  

  changeDepartment(event: any): void {
    const selectedDepartmentIds = event.value;
    console.log("this.copyDepartment ", this.copyDepartment);
    // Identify deselected departments by finding the difference between the previous and current selections
    const deselectedDepartmentIds = this.copyDepartment.filter(
      id => !selectedDepartmentIds.includes(id)
    );

    console.log("deselectedDepartmentIds    ", deselectedDepartmentIds)

    // Update the current department selection
    this.copyDepartment = selectedDepartmentIds;

    // If there are any deselected departments, show the alert
    if (deselectedDepartmentIds.length > 0) {
      this.allTeamList.forEach(team => {
        if (team.teamMemberList === '' || team.teamMemberList === null || team.teamMemberList === undefined) {
          return;
        } else {
          this.openAlertMod(this.alertMessageTemplateRef, "You have deselected a department! Wish to add it again, please ensure that the team members for that department are added as well.");

          const departmentList = team.departmentList;
          console.log("team.teamMemberList  ", team.teamMemberList)
          console.log("departmentList     ", departmentList);

          // Use strict equality check and type casting if necessary
          const filteredTeamMemberList = team.teamMemberList.filter(member => {
            const memberDeptId = String(member.departmentId); // Convert to string for comparison
            const isMatch = departmentList.some(deptId => String(deptId) === memberDeptId);
            console.log(`Checking if department ID ${memberDeptId} is in departmentList:`, isMatch);
            return isMatch;
          });
          console.log("filteredTeamMemberList    ", filteredTeamMemberList)
          team.teamMemberList = filteredTeamMemberList;
          console.log(team.teamMemberList);
        }
      });
    }

    // Update the previousDepartmentIds with the current selection for future comparison
    this.copyDepartment = selectedDepartmentIds;
  }

  EmployeeViewDataModel(template: TemplateRef<any>, catagory: string) {
    if (catagory === 'Sankh') {
      this.employeeData = this.sankhMappedEmployeesList;
      this.catagory = catagory;
    }
    else if (catagory === 'Internal') {
      this.employeeData = this.internalMappedEmployeesList;
      this.catagory = catagory;
    }
    else if (catagory === 'Both') {
      this.employeeData = this.mappedToBothEmployeesList;
      this.catagory = catagory;
    }
    else if (catagory === 'Not Mapped') {
      this.employeeData = this.employeesWithoutProjectList;
      this.catagory = catagory;
    }
    else if (catagory === 'Without Billability') {
      this.employeeData = this.employeesWithoutBillability;
      this.catagory = catagory;
    }

    else if (catagory === 'All Mapped') {
      this.employeeData = this.allMappedEmployeesList;
      this.catagory = catagory;
    }
    else if (catagory === 'Exception') {
      this.employeeData = this.exceptionEmployeesList;
      this.catagory = catagory;
    }
    else if (catagory === 'OnBench') {
      this.employeeData = this.employeeBenchRepot;
      this.catagory = catagory;
    } else if (catagory === 'Unfilled Timesheet Projects') {
      this.employeeData = this.unfilledTimesheetProjectList;
      this.catagory = catagory;
    }
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }




  deleteResourceModal(template: TemplateRef<any>, teamId) {
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.deleteResourceModalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj2 = teamId;

  }

  hideDeleteResourceModalRef(){
    this.deleteResourceModalRef.hide();
  }


  ranges = [
    { label: '3M', value: 3, unit: 'M' },
    { label: '6M', value: 6, unit: 'M' },
    { label: '1Y', value: 1, unit: 'Y' }
  ];

  selectedRange = this.ranges[0];

  selectRange(range: any) {
    this.selectedRange = range;
    this.fetchTimesheetMissingCount();
  }

  

  unfilledTimesheetProjectList: any[] = [];
  unfilledTimesheetProjectListCount: any;

  fetchTimesheetMissingCount() {
    const today = new Date();
    const fromDate = new Date(today);

    if (this.selectedRange.unit === 'M') {
      fromDate.setMonth(fromDate.getMonth() - this.selectedRange.value);
    } else if (this.selectedRange.unit === 'Y') {
      fromDate.setFullYear(fromDate.getFullYear() - this.selectedRange.value);
    }

    const payload = {
      empId: this.currentUser.empId,
      fromDate: fromDate.toISOString().split('T')[0],
      toDate: today.toISOString().split('T')[0]
    };

    console.log('non compliance:', payload);
    this.resourceManagementService.getProjectsunfilledTimesheet(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.unfilledTimesheetProjectList = response.serviceResponse;
        this.unfilledTimesheetProjectListCount = this.unfilledTimesheetProjectList.length;
        console.log("this.internalMappedEmployees", this.internalMappedEmployees)
      } else {
        this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
      }
    });

  }

  openTimesheetPopup() {
    // Load and show modal with data for selectedRange
    // this.modalService.openTimesheetModal(this.selectedRange);
  }


  pageNo = 1;
  handlePageChanges(event) {
    this.pageNo = event;
  }

  // closeProjectModal(){
  //   console.log("again called after deleted ");
  //   this.modalRef.hide();
  // }

  getRefreshPage() {
    this.showEditProjectForm(this.projectObj);
  }

  syncPoProjectDetailsByProjectId(template: TemplateRef<any>, project: any) {
    this.resourceManagementService.syncPoProjectDetailsByProjectId(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  sendEmailNotificationToBDTeam(template: TemplateRef<any>, project: any) {
    this.resourceManagementService.sendEmailNotificationToBDTeam(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  deleteTeam(template: TemplateRef<any>) {
    console.log("delete team method call ", this.tempTeam);
    this.tempTeam.endDate = this.lastDate;
    this.projectService.deleteTeam(this.tempTeam).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const teamIndex = this.allTeamList.findIndex(team => team.teamId === this.tempTeam.teamId);

        if (teamIndex !== -1) {
          this.allTeamList.splice(teamIndex, 1);
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
          this.copyDepartment = [];
        }
        if (!this.allTeamList || this.allTeamList.length === 0) {
          this.addInputTeamField();
        }
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })

  }
  deleteResourceModal1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    const detailsList = Array.isArray(this.projectdetails1) ? this.projectdetails1 : [this.projectdetails1];

    detailsList.forEach(details => {
      this.lastDate = details.poEndDate
        ? moment(details.poEndDate).format('YYYY-MM-DD')
        : moment().format('YYYY-MM-DD');
      console.log("Testing for end date", this.lastDate);
    });

  }


  toggleSelectAll(): void {
    this.allTeamList.forEach(teamObj => {
      teamObj.selected = this.selectAll;
    });
    this.updateSelectedTeamsDetails();
  }


  updateSelection(): void {

    this.selectAll = this.allTeamList.every(teamObj => teamObj.selected);
    this.updateSelectedTeamsDetails();
  }


  updateSelectedTeamsDetails(): void {
    this.allTeamList.forEach(teamObj => {
      teamObj.endDate = this.lastDate;
    });
    this.selectedTeamsDetails = this.allTeamList.filter(teamObj => teamObj.selected);
    console.log('Selected Team Details:', this.selectedTeamsDetails);
  }

  deleteTeamsByIdsBulk(template: TemplateRef<any>) {
    console.log("Deleting teams: ", this.selectedTeamsDetails);
    const detailsList = Array.isArray(this.projectdetails1) ? this.projectdetails1 : [this.projectdetails1];

    // detailsList.forEach(details => {
    //   this.lastDate = details.poEndDate
    //     ? moment(details.poEndDate).format('YYYY-MM-DD')
    //     : moment().format('YYYY-MM-DD');
    //   console.log("Testing for end date", this.lastDate);
    // });
    this.selectedTeamsDetails = this.selectedTeamsDetails.map(entry => ({
      ...entry,
      endDate: this.lastDate || null,
      createdBy: this.currentUser.empId
    }));
    this.projectService.deleteTeamsByIdsBulk(this.selectedTeamsDetails).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const deletedIds = this.selectedTeamsDetails.map(team => team.teamId);
        this.allTeamList = this.allTeamList.filter(team => !deletedIds.includes(team.teamId));
        this.selectedTeamsDetails = [];
        this.allTeamList.forEach(team => team.selected = false);
        this.selectAll = false;
        this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
        this.copyDepartment = [];
        if (!this.allTeamList || this.allTeamList.length === 0) {
          this.addInputTeamField();
        }
        console.log("Selected teams cleared: ", this.selectedTeamsDetails);

        this.openAlertMod(template, response.serviceResponse);

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getProjectType(project: any): string {
    if (project.poProjectType !== null && project.poProjectType !== undefined && project.poProjectType !== '') {
      return project.poProjectType;
    } else if (project.internalProjectType !== null && project.internalProjectType !== undefined && project.internalProjectType !== '') {
      return project.internalProjectType;
    } else {
      return 'NA';
    }
  }

  isEmployeeInList(list: any[]): boolean {
    return list?.some(emp => emp.empId === this.selectedEmpId);
  }

  isEmployeeInTeam(employee: any): boolean {
    // console.log("Checking if employee is in team:", employee);
    // console.log("All team members:", this.teamObj.allTeamMemberList);
    if (!this.teamObj.allTeamMemberList == undefined) {
  return this.teamObj.allTeamMemberList.some(
    (member: any) => member.empId === employee.empId
  );}
  else{
    return false;
  }
}

  displayEmployee(emp: any): string {
    console.log("emp", emp);  // This is helpful for debugging
    if (typeof emp === 'string') {
      return emp;  // User is typing or you manually set value to string
    }
    return emp && emp.name ? emp.name : '';
  }

    openSummaryModal1(template: TemplateRef<any>) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-lg' });

    this.getFixedCostProjectSummaryData();
  }

  closeSummaryModal() {
    this.summaryModalRef.hide();
  }

  async openEditModal(template, project) {
    // console.log("Project ",project)
    this.isEditProject = true;

    this.allProjectTable = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.selectedTeamsDetails = [];
    this.projectObj = Object.assign({}, project);
    this.getAllResourceRequirementForProject(this.projectObj);
    this.getAllDepartmentListAndSelectByProjectDeptId(project);
    this.getTeamListByProjectName(project);
    await this.getManagerList();
    this.getEmployeeByNameAndEmpld();
    this.activeModalTab = 'info'; 
    // this.isModalFullscreen = false;    
    
    this.modalRef1 = this.modalService.show(template, { class: 'modal-lg' });

    this.projectObj.projectManagerId = (
    this.projectObj.projectManager?.includes(",")
      ? this.projectObj.projectManager?.split(",")
      : [this.projectObj.projectManager]
    )
    .map((manager) => manager.trim().toLowerCase())
    .map(
      (manager) =>
        this.filteredManagerList.find((m) => m.name.toLowerCase() == manager)
      ?.empId,
    )
    .filter((empId) => empId != undefined);    
  }

  async openTeamMembersModal(template: any, projectObj, currentTeam) {
    this.teamMemberCtrl.reset(); 
    this.selectedMembers = [];
    this.hasSelectedMembers = false;
    this.selectedTeamEntries = [];
    // this.teamObj.allTeamMemberList = [];
    this.getAllMembers()?.forEach(m => {
       if (m.selected) {
        m.selected = false;
    }});
    
    this.modalRefTeamMember = this.modalService.show(template, { class: 'custom-modal' });
    this.currentPoProjectType = projectObj.poProjectType;
    console.log("The project object is",projectObj);
    console.log("Project object id is ",projectObj.id);
    if (projectObj.id && projectObj.poProjectType.toUpperCase() =='TNM') {
      // this.getResourceRequirementByPoProjectId(projectObj.id);
     await this.getResourceRequirementByPoProjectId(projectObj.id,projectObj.poProjectType,1);
     let totalRequirement = this.projectRequirementsList.totalRequirements;
     await this.getProjectAssignedDataByProjectIdfunc(projectObj.id,1,totalRequirement);
    }
    else{
      await this.getResourceRequirementByPoProjectId(projectObj.projectId,projectObj.poProjectType,0);
    //  await this.getProjectAssignedDataByProjectIdfunc(projectObj.projectId,0,0);

    }
    console.log("Current resource overview id",this.resourceOverViewIdList);
    await this.getAllResourceRequirementForProject1(projectObj);

    projectObj.resourceRequirements.forEach(requirement => {
      requirement.teamMembers = this.allTeamList[0]?.teamMemberList?.filter(member => member.empId) || [];
      requirement.assigned = requirement.teamMembers.length;
    });

    if (projectObj.resourceRequirements != null && projectObj.resourceRequirements.length != 0) {
      this.projectObj.resourceRequirements.forEach(req => {
        const assignedCount = this.teamObj.allTeamMemberList?.filter(member => member.resourceOverviewId === req.resourceOverviewId).length || 0;
        req.assigned = assignedCount;
      });
    }


    this.allTeamMembers = [];
    this.teamObj.teamLeadId = '';
    this.newteamMember.empId = '';
    this.newteamMember.employeeRole = null;
    // this.newteamMember.departmentId = null;
    this.newteamMember.name = null;
  
    // this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    // this.getAllEmployeesByRole(currentTeam.departmentList);
    console.log("this.copyDepartment ", this.copyDepartment);
    // this.getAllEmployeesByDepartmentIds(this.copyDepartment);

     const deptIdList = currentTeam.departmentList.map((id: number) => ({
           deptId: id
      }));
    this.getAllEmployeesByDepartmentIds(deptIdList);
    this.getAllEmployeesByRole(this.copyDepartment);
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == currentTeam.teamName) {
        let teamLeadObj = team.teamMemberList?.filter(x => x.isTeamLead == true || x.isTeamLead == "true");
        if (teamLeadObj) {
          this.teamObj.teamLeadId = teamLeadObj[0]?.empId;
        }
        //console.log(teamLeadObj, " :teamLeadObj");
        console.log(team, " : this.teamObj.teamLeadId");

        console.log("team.teammemberListj ", team.allTeamMemberList);

        this.teamObj.allTeamMemberList = team.teamMemberList?.filter(x => x.isTeamLead != true);
        this.teamObj.teamId = team.teamId;
        if ((this.teamObj.allTeamMemberList != undefined || this.teamObj.allTeamMemberList != null) && this.teamObj.allTeamMemberList.length != 0) {
          this.isUpdation = true;
        } else {
          this.isUpdation = false;
        }
      }
    });
    //console.log(this.teamObj.allTeamMemberList, " allTeamMemberList");

    this.currentTeam = currentTeam;
    this.expandedIndex = false;
  }


  EmployessIds: number[] = [];
  activeProjects: number[] = [];
  currentProjectDetails: any;
  openDatePicker(template: TemplateRef<any>, template1: TemplateRef<any>, project: any, template2: TemplateRef<any>) {
    this.isBulkDelete = false;
    this.isBulkDeleteResource = false;
    this.isBulkDeleteResourceFromTNMProjects = false;
    this.currentProjectDetails = project.projectId;
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    this.completedProjectDetails = project;
    console.log("projectid", this.completedProjectDetails);
    this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj, "projectofthisteam");

        this.projectCompletionDate = this.projectObj.poEndDate ? moment(this.projectObj.poEndDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD');
        console.log(this.projectCompletionDate, " this.projectObj.endDtae");
        this.projectObj.teamList.forEach((obj) => {
          obj.departmentList = obj.departmentList?.map(x => +x);
          this.copyDepartment = obj.departmentList;

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                member.emp360 = member.empId;
                member.shadowControl = new FormControl(member.shadow || null);
              }
            });
          } else {
            console.warn('teamMemberList is null or undefined');
          }
        });
        //console.log(this.projectObj.teamList, " this.projectObj.teamList");
        this.previewTeamList = this.projectObj.teamList;

        //console.log(" length of previewTeamList  ",this.previewTeamList.length);
        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          //console.log(" find error in else part ")
          this.allTeamList = this.projectObj.teamList;
          // this.allTeamListCopy = this.projectObj.teamList;
          const empIds: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
            const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

            members.forEach(member => {
              if (
                Array.isArray(member.otherActiveProjects) &&
                member.otherActiveProjects.length === 0 &&
                member.isDefaultProject === 1
              ) {
                acc.push(member.empId);
              }
            });

            return acc;
          }, []);

          const empIdsHavingActiveProjects: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
            const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

            members.forEach(member => {
              if (
                Array.isArray(member.otherActiveProjects) &&
                member.otherActiveProjects.length !== 0 &&
                member.isDefaultProject === 1
              ) {
                acc.push(member.empId);
              }
            });

            return acc;
          }, []);
          this.activeProjects = empIdsHavingActiveProjects;
          this.EmployessIds = empIds;
          if (empIds.length !== 0 && empIdsHavingActiveProjects.length !== 0) {
            this.getEmployeeInformationBulk(empIds);
            this.modalRef4 = this.modalService.show(template, { class: 'modal-xl' });
            this.setDefaultProjectObj.empIds = empIdsHavingActiveProjects;
            this.setDefaultProjectObj.projectId = project.projectId;
            this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
            this.modalRef = this.modalService.show(template1, { class: 'modal-xl' });

          } else if (empIds.length !== 0 && empIdsHavingActiveProjects.length === 0) {

            this.getEmployeeInformationBulk(empIds);
            this.modalRef4 = this.modalService.show(template, { class: 'modal-xl' });
          } else if (empIds.length === 0 && empIdsHavingActiveProjects.length !== 0) {
            this.setDefaultProjectObj.empIds = empIdsHavingActiveProjects;
            this.setDefaultProjectObj.projectId = project.projectId;
            this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
            this.modalRef = this.modalService.show(template1, { class: 'modal-xl' });
          } else {
            this.modalRef5 = this.modalService.show(template2, { class: 'modal-sm' });
          }
          console.error("test", empIdsHavingActiveProjects, empIds);
          // this.modalRef4 = this.modalService.show(template, { class: 'modal-xl' });
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
        }

      } else {
        console.error(response.serviceResponse);

        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
          this.completedProjectDetails.projectCompletionDate = new Date().toISOString().split('T')[0];
        this.completedProjectDetails.projectStatus = 'Completed';
        this.completedProjectDetails.updatedBy = this.currentUser.empId;

        if (!this.completedProjectDetails.projectCompletionDate) {
          this.alertMessage = "Please Select Completion Date!!"
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        try {

        if (this.completedProjectDetails.poProjectType == null) {
          this.completedProjectDetails.projectType = "Internal";
        } else {
          this.completedProjectDetails.projectType = this.completedProjectDetails.poProjectType;
        }

        this.resourceManagementService.completionDateOfProject(this.completedProjectDetails).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.selectedDate = '';
            this.closeModal1();
            console.log('Selected Date:', response.serviceResponse);
            this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
          } else {
            this.selectedDate = '';
            this.modalRef5.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
          }
        });
      } catch (error: unknown) {
        console.error('Unexpected error in try/catch:', error);
      }
        } else {
          this.allTeamList = this.projectObj.teamList;
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
        }
      }
    });
    this.getManagerList();



    //     const empIds: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
    //       const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

    //       members.forEach(member => {
    //         if (
    //           Array.isArray(member.otherActiveProjects) &&
    //           member.otherActiveProjects.length === 0 &&
    //           member.isDefaultProject === 1
    //         ) {
    //           acc.push(member.empId);
    //         }
    //       });

    //       return acc;
    //     }, []);



    //     const empIdsHavingActiveProjects: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
    //       const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

    //       members.forEach(member => {
    //         if (
    //           Array.isArray(member.otherActiveProjects) &&
    //           member.otherActiveProjects.length !== 0 &&
    //           member.isDefaultProject === 1
    //         ) {
    //           acc.push(member.empId);
    //         }
    //       });

    //       return acc;
    //     }, []);
    // let firstModalRef, secondModalRef;
    //     if(empIds.length !==0 && empIdsHavingActiveProjects.length !==0){
    //       this.modalRef4 = this.modalService.show(template, { class: 'modal-xl' });

    //     this.modalRef = this.modalService.show(template1, { class: 'modal-xl' });

    //     }else if(empIds.length !==0 && empIdsHavingActiveProjects.length ===0 ){
    //       this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    //     }else{
    //        this.modalRef = this.modalService.show(template1, { class: 'modal-xl' });
    //     }

    this.getProjectDetailsForBulkDefaultUpdate();
  }

  submitDate(template: TemplateRef<any>) {


    this.completedProjectDetails.projectCompletionDate = this.selectedDate;
    this.completedProjectDetails.projectStatus = 'Completed';
    this.completedProjectDetails.updatedBy = this.currentUser.empId;

    if (!this.completedProjectDetails.projectCompletionDate) {
      this.alertMessage = "Please Select Completion Date!!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (
      this.EmployessIds.length === 0 && this.activeProjects.length === 0
    ) {

      this.allTeamList.forEach(teamObj => {
        teamObj.endDate = this.selectedDate;
      });
      console.log("insidde", this.completedProjectDetails, this.selectedDate, this.allTeamList);
      try {

        if (this.completedProjectDetails.poProjectType == null) {
          this.completedProjectDetails.projectType = "Internal";
        } else {
          this.completedProjectDetails.projectType = this.completedProjectDetails.poProjectType;
        }

        this.resourceManagementService.completionDateOfProject(this.completedProjectDetails).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.selectedDate = '';
            this.closeModal1();
            console.log('Selected Date:', response.serviceResponse);
            // this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
             this.alertMessage = response.serviceResponse;
            // this.alert_message_without_reloadModalRef = this.modalService.show(this.alert_message_without_reloadTemplate, { class: 'modal-md' });
            this.openAlertMessageMarkAsCompleteTemp(this.alertMessage);
            this.isCompletionSuccess = true;
          } else {
            this.selectedDate = '';
            this.modalRef5.hide();
            this.alertMessage = response.serviceResponse;
            // this.alert_message_without_reloadModalRef = this.modalService.show(this.alert_message_without_reloadTemplate, { class: 'modal-md' });
            this.openAlertMessageMarkAsCompleteTemp(this.alertMessage);
            this.isCompletionSuccess = false;
          }
        });

        this.projectService.deleteTeamsByIdsBulk(this.selectedTeamsDetails).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus === "Success") {
            const deletedIds = this.selectedTeamsDetails.map(team => team.teamId);
            this.allTeamList = this.allTeamList.filter(team => !deletedIds.includes(team.teamId));
            this.selectedTeamsDetails = [];
            this.allTeamList.forEach(team => team.selected = false);
            this.selectAll = false;
            this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
            this.copyDepartment = [];
            if (!this.allTeamList || this.allTeamList.length === 0) {
              this.addInputTeamField();
            }
            console.log("Selected teams cleared: ", this.selectedTeamsDetails);

            // this.openAlertMod(template, response.serviceResponse);

          } else {
            // this.openAlertMod(template, response.serviceResponse);
          }
        });

      } catch (error: unknown) {
        console.error('Unexpected error in try/catch:', error);
      }

    }


  }

  closeModal1() {
    this.selectedDate = '';
    this.modalRef5.hide();
  }
  closeModal() {
    this.modalRef.hide();
  }
  // getResourceRequirementByPoProjectId(id) {
  //   this.loadingRequirements = true;
  //   this.resourceManagementService.getResourceRequirementByPoProjectId(id).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.projectRequirementsList = response.serviceResponse.resourceRequirements;
  //       this.projectObj.resourceRequirements=response.serviceResponse.resourceRequirementList;
  //       this.loadingRequirements = false;
  //     } else {
  //       console.error("Error fetching project requirement list");
  //       this.loadingRequirements = false;
  //     }
  //   });
  // }

  resourceOverViewIdList = [];
  infoTitle:String="Total number of requirements";
 async getResourceRequirementByPoProjectId(id, type,flagForPOProject) {
  this.infoTitle = "Total number of requirements";
   this.loadingRequirements = true;
    console.log("getResourceRequirementByPoProjectId called")
    this.projectRequirementsList =  new ProjectRequirements();
    this.projectObj.resourceRequirements=[];

    try{
      const response:any  = await this.resourceManagementService.getResourceRequirementByPoProjectId(id,type).pipe(first()).toPromise();
    
    // this.resourceManagementService.getResourceRequirementByPoProjectId(id,type).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequirementsList = response.serviceResponse.resourceRequirements;
        this.projectObj.resourceRequirements=response.serviceResponse.resourceRequirementList;
        console.log("Project requirement list",this.projectRequirementsList);
        console.log("Project resourceRequirements list",this.projectObj.resourceRequirements);
        this.projectObj.resourceRequirements.map(item=>{
          this.resourceOverViewIdList.push(item.resourceOverviewId);
        })
        console.log("resourceOverviewIdList",this.resourceOverViewIdList);
        this.loadingRequirements = false;
      } else {
        console.error("Error fetching project requirement list");
        this.loadingRequirements = false;
        if(flagForPOProject){
          // this.poResourceRequirementAlert("Unable to fetch resource requirement from Shankh!");
          this.infoTitle="Unable to fetch resource requirement from Shankh!";
        }else{
          // this.poResourceRequirementAlert("Unable to fetch Resource requirement");
          this.infoTitle ="Unable to fetch Resource requirement";
        }
      }
    // });
    }catch(error){
      console.log(error);
      this.poResourceRequirementAlert(error.message);
      this.loadingRequirements = true;
      
    }
  }

  removeShadowResource(member: any, index: number) {
    member.shadowResources.splice(index, 1);
  }

  onResourceSelected(event: any, member) {
    const selectedName = event.option.value;
    const selectedEmp = this.employeeList.find(emp => emp.name === selectedName);
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      console.log('Selected Employee ID:', this.selectedEmpId);
    }
    member.shadow = selectedName;
    member.shadowControl.setValue(selectedName);
  }

  onTeamMemberSelected(event: MatAutocompleteSelectedEvent): void {
    const selectedEmployee = event.option.value;
    this.newteamMember.empId = selectedEmployee.empId;
    this.newteamMember.name = selectedEmployee.name;
  }

  filterDepartmentsTeamForm() {
    const lowerText = this.searchTextDeptTeam.trim().toLowerCase();

    const filtered = this.departmentsList.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );

    const selected = this.departmentsList.filter(dept =>
      this.teamObj.departmentList?.includes(dept.deptId)
    );

    const selectedSet = new Set(filtered.map(dept => dept.deptId));
    const merged = [...filtered];

    selected.forEach(dept => {
      if (!selectedSet.has(dept.deptId)) {
        merged.push(dept);
      }
    });

    this.filteredDepartmentsTeam = merged;
  }

toggleSelectAllTeams(event: any, teamObj: any) {
  if (!event.isUserInput || !event.source.selected) {
    return;
  }

  const allDeptIds = this.filteredDepartmentsTeam.map(dept => dept.deptId);

  if (teamObj.departmentList?.length === allDeptIds.length) {
    teamObj.departmentList = [];
  } else {
    teamObj.departmentList = [...allDeptIds];
  }
  event.source.deselect();
}




  clearSelectionDept(event: Event) {
    event.stopPropagation();
    this.teamObj.departmentList = [];
    this.isAllDeptSelected = false;
  }

 onSpocSelected(event: any, teamObj: any) {
  teamObj.spoc = event.option.value;
}


  displaySPOC(emp: any): string {
    console.log("emp", emp)
    return emp ? `${emp.name}` : '';
  }

  filterManagers() {
    const lowerText = this.searchManagerText.trim().toLowerCase();

    const filtered = this.managerList.filter(manager =>
      manager.name.toLowerCase().includes(lowerText)
    );

    const selectedManagers = this.managerList.filter(manager =>
      this.projectObj.projectManagerId?.includes(manager.empId)
    );

    const selectedSet = new Set(filtered.map(emp => emp.empId));
    const merged = [...filtered];

    selectedManagers.forEach(manager => {
      if (!selectedSet.has(manager.empId)) {
        merged.push(manager);
      }
    });

    this.filteredManagerList = merged;
  }

  toggleSelectAllManagers(): void {
    if (this.isAllManagersSelected) {
      this.projectObj.projectManagerId = [];
      this.isAllManagersSelected = false;
    } else {
      this.projectObj.projectManagerId = this.filteredManagerList.map(emp => emp.empId);
      this.isAllManagersSelected = true;
      console.log("Selected Managers: ", this.projectObj.projectManagerId);
    }
  }

  clearManagerSelection(event: Event): void {
    event.stopPropagation();
    this.projectObj.projectManagerId = [];
    this.isAllManagersSelected = false;
  }

  onManagerSelectionChange(selectedManagers: string[]): void {
    this.isAllManagersSelected = selectedManagers.length === this.filteredManagerList.length;
  }

  filterOverhead() {
    const lowerText = this.searchOverheadText.trim().toLowerCase();

    const filtered = this.overheadList.filter(overhead =>
      overhead.name.toLowerCase().includes(lowerText)
    );

    const selectedOverheads = this.overheadList.filter(overhead =>
      this.projectObj.projectOverheadId?.includes(overhead.empId)
    );

    const selectedSet = new Set(filtered.map(emp => emp.empId));
    const merged = [...filtered];

    selectedOverheads.forEach(overhead => {
      if (!selectedSet.has(overhead.empId)) {
        merged.push(overhead);
      }
    });

    this.filteredOverheadList = merged;
  }

  toggleSelectAllOverhead(): void {
    if (this.isAllOverheadsSelected) {
      this.projectObj.projectOverheadId = [];
      this.isAllOverheadsSelected = false;
    } else {
      this.projectObj.projectOverheadId = this.filteredOverheadList.map(emp => emp.empId);
      this.isAllOverheadsSelected = true;
    }
  }

  clearOverheadSelection(event: Event): void {
    event.stopPropagation();
    this.projectObj.projectOverheadId = [];
    this.isAllOverheadsSelected = false;
  }

  onOverheadSelectionChange(selectedOverheads: string[]): void {
    this.isAllOverheadsSelected = selectedOverheads.length === this.filteredOverheadList.length;
  }

  getTeamMembersForRequirement(resourceOverviewId: number): any[] {
    return this.allTeamMembers?.filter(member => member.resourceOverviewId === resourceOverviewId) || [];
  }

  hasNoTeamMembersFor(requirementId: number): boolean {
    return !this.teamObj.allTeamMemberList?.some(
      member => member.resourceOverviewId === requirementId
    );
  }

  // openModal(template,empId) {
  //   this.getEmployeeInformation(empId);
  //   this.modalRef2 = this.modalService.show(template, { class: 'custom-modal' });
  // }

  getEmployeeInformation(empId) {
    this.resourceManagementService.getEmployeeInformation(empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeInformation = response.serviceResponse;
      } else {
        console.error("Error employee informations");
      }
    });
  }
  popoverEmpId: string | null = null;

  togglePopover(empId: string) {
    if (this.popoverEmpId === empId) {
      this.popoverEmpId = null;
      return;
    }

    this.getEmployeeInformation(empId);
    this.popoverEmpId = empId;
  }

  closePopover(empId: string) {
    if (this.popoverEmpId === empId) {
      this.popoverEmpId = null;
    }
  }

  selectedMembers: any[] = [];
  employeeSelectionHistory: any[] = [];
  globalSelectAll: boolean = false;
  onSelectionChange(team: any, object: any) {
    if (!this.selectedMembers) {
      this.selectedMembers = [];
    }
    console.log("kmjhg", team);
    const existingTeamIndex = this.selectedMembers.findIndex(item => item.team.teamId === team.teamId);

    if (object.selected) {

      if (existingTeamIndex === -1) {
        this.selectedMembers.push({ team, object: [object] });
      } else {

        const alreadyAdded = this.selectedMembers[existingTeamIndex].object
          .some((m: any) => m.empId === object.empId);
        if (!alreadyAdded) {
          this.selectedMembers[existingTeamIndex].object.push(object);
        }
      }


      const historyExists = this.employeeSelectionHistory.some(
        (entry) => entry.teamId === team.teamId && entry.empId === object.empId
      );
      if (!historyExists) {
        const deselectedMember = {
          teamId: team.teamId,
          empId: object.empId,
          employeeTeamMapId: object.employeeTeamMapId
        };
        this.employeeSelectionHistory.push(deselectedMember);
      }
    } else {

      if (existingTeamIndex !== -1) {
        const memberIndex = this.selectedMembers[existingTeamIndex].object
          .findIndex((m: any) => m.empId === object.empId);

        if (memberIndex !== -1) {

          this.selectedMembers[existingTeamIndex].object.splice(memberIndex, 1);


          if (this.selectedMembers[existingTeamIndex].object.length === 0) {
            this.selectedMembers.splice(existingTeamIndex, 1);
          }
        }
      }


      const historyIndex = this.employeeSelectionHistory.findIndex(
        (entry) => entry.teamId === team.teamId && entry.empId === object.empId
      );
      if (historyIndex !== -1) {
        this.employeeSelectionHistory.splice(historyIndex, 1);
      }
    }

    console.log("Selected Members:", this.selectedMembers);
    console.log("Employee Selection History:", this.employeeSelectionHistory);
  }

  teamMemberList1: any[] = [];
  isTeamFullySelected(team: any): boolean {
    return team.allTeamMemberList.every((member: any) => member.selected);

  }

  toggleTeamSelection(team: any, event: any) {
    const isChecked = event.target.checked;

    team.allTeamMemberList.forEach((member: any) => {
      if (member.selected !== isChecked) {
        member.selected = isChecked;
        this.onSelectionChange(team, member);
      }
    });

    if (!isChecked) {

      this.employeeSelectionHistory = this.employeeSelectionHistory.filter(
        (entry) => entry.teamId !== team.teamId
      );


      const teamIndex = this.selectedMembers.findIndex(sel => sel.team.teamId === team.teamId);
      if (teamIndex !== -1) {
        this.selectedMembers.splice(teamIndex, 1);
      }
    }
    console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory);
  }

  isMemberSelected(team: any, member: any): boolean {
    return !!member.selected;
  }

  areAllTeamsSelected(): boolean {
    const allTeamsHaveMembers = this.teamMemberList1.every(team => team.employees.length > 0);
    const allMembersSelected = this.teamMemberList1.every(team =>
      team.employees.every((member: any) => member.selected)
    );

    return this.teamMemberList1.length > 0 && allTeamsHaveMembers && allMembersSelected;
  }

  toggleAllTeams(event: any): void {
    const isChecked = event.target.checked;

    this.teamMemberList1.forEach(team => {
      team.employees.forEach(member => {
        if (member.selected !== isChecked) {
          member.selected = isChecked;
          this.onSelectionChange(team, member);
        }
      });
    });
    console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory, this.teamMemberList1);

    if (!isChecked) {
      this.employeeSelectionHistory = [];
      this.selectedMembers = [];
    }
  }
  lastDate1: any;
  deleteResourceModalBulk(template: TemplateRef<any>, template1: TemplateRef<any>, template2: TemplateRef<any>, project) {



    if (this.selectedMembers.length > 0) {
      this.isBulkDelete = false;
      this.isBulkDeleteResource = true;
      this.isBulkDeleteResourceFromTNMProjects = false;
      const empIds: number[] = this.selectedMembers.reduce((acc: number[], team: any) => {
        const members = Array.isArray(team.team?.allTeamMemberList) ? team.team.allTeamMemberList : [];

        members.forEach(member => {
          if (
            Array.isArray(member.otherActiveProjects) &&
            member.otherActiveProjects.length === 0 &&
            member.isDefaultProject === 1
          ) {
            acc.push(member.empId);
          }
        });

        return acc;
      }, []);


      const empIdsHavingActiveProjects: number[] = this.selectedMembers.reduce((acc: number[], team: any) => {
        const members = Array.isArray(team.team?.allTeamMemberList) ? team.team.allTeamMemberList : [];

        members.forEach(member => {
          if (
            Array.isArray(member.otherActiveProjects) &&
            member.otherActiveProjects.length !== 0 &&
            member.isDefaultProject === 1
          ) {
            acc.push(member.empId);
          }
        });

        return acc;
      }, []);

      this.activeProjects = empIdsHavingActiveProjects;
      this.EmployessIds = empIds;
      const selectedEmpIds = this.employeeSelectionHistory.map(item => item.empId);

      this.EmployessIds = this.EmployessIds.filter(empId =>
        selectedEmpIds.includes(empId)
      );

      this.activeProjects = this.activeProjects.filter(empId =>
        selectedEmpIds.includes(empId)
      );


      // console.log("test projectDeatils", this.activeProjects, this.EmployessIds, project, this.selectedMembers)
      if (this.EmployessIds.length !== 0 && this.activeProjects.length !== 0) {
        this.getEmployeeInformationBulk(this.EmployessIds);
        this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = project.projectId;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });

      } else if (this.EmployessIds.length !== 0 && this.activeProjects.length === 0) {
        this.getEmployeeInformationBulk(this.EmployessIds);
        this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
      } else if (this.EmployessIds.length === 0 && this.activeProjects.length !== 0) {
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = project.projectId;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });
      } else {
        this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
      }
    } else {

      this.isBulkDelete = false;
      this.isBulkDeleteResource = false;
      this.isBulkDeleteResourceFromTNMProjects = true;
      const empIds: number[] = this.slectedMemberFromResourceRequirement
        .filter(member =>
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length === 0 &&
          member.isDefaultProject === 1
        )
        .map(member => member.empId);




      const empIdsHavingActiveProjects: number[] = this.slectedMemberFromResourceRequirement
        .filter(member =>
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length > 0 &&
          member.isDefaultProject === 1
        )
        .map(member => member.empId);


      this.activeProjects = empIdsHavingActiveProjects;
      this.EmployessIds = empIds;
      console.log("test", this.EmployessIds, this.activeProjects);
      const selectedEmpIds = this.selectedTeamEntries.map(item => item.empId);

      this.EmployessIds = this.EmployessIds.filter(empId =>
        selectedEmpIds.includes(empId)
      );

      this.activeProjects = this.activeProjects.filter(empId =>
        selectedEmpIds.includes(empId)
      );


      console.log("test projectDeatils", this.activeProjects, this.EmployessIds, project, this.selectedMembers)
      if (this.EmployessIds.length !== 0 && this.activeProjects.length !== 0) {
        this.getEmployeeInformationBulk(this.EmployessIds);
        this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = project.projectId;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });

      } else if (this.EmployessIds.length !== 0 && this.activeProjects.length === 0) {
        this.getEmployeeInformationBulk(this.EmployessIds);
        this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
      } else if (this.EmployessIds.length === 0 && this.activeProjects.length !== 0) {
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = project.projectId;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });
      } else {
        this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
      }
    }
    // this.modalRef = this.modalService.show(template, { class: 'modal-md' });

    this.lastDate1 = this.projectCompletionDate;
    this.getProjectDetailsForBulkDefaultUpdate();
  }
   deleteResourceFromProjectBulk(template: TemplateRef<any>) {
    this.employeeSelectionHistory = this.employeeSelectionHistory.map(entry => ({
      ...entry,
      endDate: this.lastDate1 || null,
      createdBy: this.currentUser.empId
    }));
    console.log("Selected members", this.selectedMembers);
    console.log("Employee Selection History",this.employeeSelectionHistory)
    // console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory, this.teamMemberList1);
    this.projectService.updateProjectResourcesAsInActiveBulk(this.employeeSelectionHistory)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          // this.openAlertMod(template, response.serviceResponse);
          this.teamMemberList1.forEach(team => {

            team.employees.forEach(member => {
              member.selected = false;
            });
          });

          this.getExistingProjectsByUser(this.projectObj2.empId)
          //What happening here is first when we are deleting team member to append it directly in the frontend without api call we are removing the team member from the list
         //  the teamobj is the current team which is opend and allTeammember list contains the list of all the team members.
         //and after deleting it we are calling the api for getting the assigned count because this would also be changed , but why we need to call the api for the assigned count is
         //we doesnot have any idea about a team member if he is approved or not so we need to call the api to get data from backend
         
           this.teamObj.allTeamMemberList = this.teamObj.allTeamMemberList.filter(member => !this.employeeSelectionHistory.some(entry => +entry.empId == +member.empId));
          //  let totalRequirement = this.projectRequirementsList.totalRequirements;
           console.log("Project obj id is",this.projectObj.id)
           this.getResourceRequirementByPoProjectId(this.projectObj.projectId,this.projectObj.poProjectType,0);
           let currentTeam = this.allTeamList.filter(team => team.teamId == this.teamObj.teamId);
           
           let currentTeamIndex = this.allTeamList.findIndex(team => team.teamId == this.teamObj.teamId);
           currentTeam = currentTeam[0].teamMemberList.filter(member => !this.employeeSelectionHistory.some(entry => +entry.empId == +member.empId));
           this.allTeamList[currentTeamIndex].teamMemberList = currentTeam;
          
            //25 Oct comment started
          // this.RbacShankhProjects(this.projectFilterDTO);
          // this.RbacInternalProjects(this.projectFilterDTO);
          // this.RbacBothShankhInternal(this.projectFilterDTO);
          // this.ProjectLessEmployees(this.projectFilterDTO);
          // this.RbacAllShankhInternalProjects(this.projectFilterDTO);
          // this.ExceptionEmployeeReport(this.projectFilterDTO);
          // this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);
          // this.getEmployeesWithoutBillability(this.projectFilterDTO);
          //25 Oct comment ended
          this.getAllEmployeeGroupCount();
          
           this.updateProjectCompletionModalRef.hide();
           this.openremoveResourceModal(response.serviceResponse);

        }
        else{
          this.updateProjectCompletionModalRef.hide();
          this.openremoveResourceModal("Unable to delete. Something went wrong.");
        }
        this.selectedMembers = [];
        this.employeeSelectionHistory = [];
      });
  }

  getPreviousDefaultProjectDetails(empId) {
    this.resourceManagementService.getPreviousDefaultProjectDetails(empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.previousDefaultProjects = response.serviceResponse;
        // this.modalRef = this.modalService.show(this.previousDefaultProject, { class: 'custom-modal' });
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error employee informations");
      }
    });
  }

  onDefaultProjectCheckboxChange(event: Event, member) {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      member.isDefaultProject = 1;
      this.getPreviousDefaultProjectDetails(member.empId);
    } else {
      member.isDefaultProject = 0;
      if (member.otherActiveProjects && member.otherActiveProjects.length > 0) {
        this.otherProjectList = member.otherActiveProjects;
        this.filteredOtherProjectList = this.otherProjectList;  
        this.defaultProjectUpdateEmpId = member.empId;
        this.modalRef = this.modalService.show(this.otherProjectMappings, { class: 'custom-modal' });
      }
    }
  }

  filterDepartmentsInternalForm() {
    const lowerText = this.searchTextDeptInternal.trim().toLowerCase();

    const filtered = this.allDeptList.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );

    const selected = this.allDeptList.filter(dept =>
      this.projectObj.departmentList?.includes(dept.deptId)
    );
    const selectedSet = new Set(filtered.map(dept => dept.deptId));
    const merged = [...filtered];

    selected.forEach(dept => {
      if (!selectedSet.has(dept.deptId)) {
        merged.push(dept);
      }
    });

    this.filteredDepartmentsInternal = merged;
  }


  toggleSelectAllInternal() {
    if (this.isAllSelected) {
      this.projectObj.departmentList = [];
      this.isAllSelected = false;
      console.log("test", this.projectObj.departmentList);

    } else {
      this.projectObj.departmentList = this.filteredDepartmentsInternal.map(dept => dept.deptId);
      this.isAllSelected = true;
      console.log("test2", this.projectObj.departmentList);
    }
  }




  clearSelectionInternal(event: Event) {
    event.stopPropagation();
    this.projectObj.departmentName = [];
    this.isAllSelected = false;
  }



  selectedTeamData: any[] = [];
  hasSelectedMembers: boolean = false;

  getAllMembers(): any[] {
    // console.log("test",this.teamObj?.allTeamMemberList);
    return this.teamObj?.allTeamMemberList || [];

  }


  getMembersForRequirement(resourceOverviewId: any): any[] {
    return this.getAllMembers().filter(m => m.resourceOverviewId === resourceOverviewId);
  }

  getNewTeamMembers(): any[] {
    return this.allTeamMembers || [];
  }

  getNewMembersForRequirement(resourceOverviewId: any): any[] {
    return this.getNewTeamMembers().filter(m => m.resourceOverviewId === resourceOverviewId);
  }


  isAllSelected1(): boolean {
    const members = this.getAllMembers();
    return members.length > 0 && members.every(m => m.selected);
  }

  isPartiallySelected(): boolean {
    const members = this.getAllMembers();
    return members.some(m => m.selected) && !this.isAllSelected1();
  }

  toggleSelectAll1(event: any): void {
    const checked = event.target.checked;
    this.getAllMembers().forEach(m => m.selected = checked);
    this.getSelectedTeamData();
  }


  isRequirementFullySelected(requirement: any): boolean {
    const members = this.getMembersForRequirement(requirement.resourceOverviewId);
    return members.length > 0 && members.every(m => m.selected);
  }

  isRequirementPartiallySelected(requirement: any): boolean {
    const members = this.getMembersForRequirement(requirement.resourceOverviewId);
    return members.some(m => m.selected) && !this.isRequirementFullySelected(requirement);
  }

  toggleRequirementSelection(requirement: any, event: any): void {
    const checked = event.target.checked;
    this.getMembersForRequirement(requirement.resourceOverviewId).forEach(m => m.selected = checked);
    this.getSelectedTeamData();
  }


  onIndividualSelectionChange(): void {
    this.getSelectedTeamData();
  }

  trackByEmpId(index: number, member: any): number {
    return member.empId;
  }

  removeTeamMember1(member: any): void {
    // Find the index *before* any deletion
    const index = this.teamObj.allTeamMemberList.findIndex(m => m.empId === member.empId);

    // Call the unified removal function
    this.removeTeamMember(member, index);

    // Update selection
    this.getSelectedTeamData();
  }

  selectedTeamEntries: { empId: number; teamId: number }[] = [];

  slectedMemberFromResourceRequirement: any;
  getSelectedTeamData(): void {
    const allMembers = this.getAllMembers();
    const selectedMembers = allMembers.filter(m => m.selected);
    this.slectedMemberFromResourceRequirement = selectedMembers;
    console.log("test", allMembers);
    const selectedEntries = selectedMembers.map(m => ({
      empId: m.empId,
      teamId: this.teamObj.teamId,
      employeeTeamMapId: m.employeeTeamMapId
    }));


    this.selectedTeamEntries = selectedEntries;



    const groupedData = this.projectObj.resourceRequirements
      .map(requirement => {
        const membersForReq = selectedMembers.filter(
          member => member.resourceOverviewId === requirement.resourceOverviewId
        );

        if (membersForReq.length === 0) return null;

        return {
          requirementDetails: {
            role: requirement.role,
            department: requirement.department,
            experience: requirement.experience,
            resourceOverviewId: requirement.resourceOverviewId
          },
          selectedMembers: membersForReq
        };
      })
      .filter(group => group !== null);

    this.selectedTeamData = groupedData;
    this.hasSelectedMembers = this.selectedTeamEntries.length > 0;
    console.log('Selected Team Data:', this.selectedTeamEntries); // Optional: view in console
  }

  // Utility: show "No team members" message
  hasNoTeamMembersFor1(resourceOverviewId: any): boolean {
    return this.getMembersForRequirement(resourceOverviewId).length === 0;
  }

  deleteResourceFromProjectBulk1(template: TemplateRef<any>) {
    this.selectedTeamEntries = this.selectedTeamEntries.map(entry => ({
      ...entry,
      endDate: this.lastDate1 || null
    }));
    console.log("After deletion:", this.selectedTeamEntries);
    
    this.projectService.updateProjectResourcesAsInActiveBulk(this.selectedTeamEntries)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          // this.openAlertMod6(template, response.serviceResponse);


         
          this.employeeSelectionHistory = [];


          this.teamMemberList1.forEach(team => {

            team.employees.forEach(member => {
              member.selected = false;
            });
          });
          this.getExistingProjectsByUser(this.projectObj2.empId)
         //What happening here is first when we are deleting team member to append it directly in the frontend without api call we are removing the team member from the list
        //  the teamobj is the current team which is opend and allTeammember list contains the list of all the team members.
        //and after deleting it we are calling the api for getting the assigned count because this would also be changed , but why we need to call the api for the assigned count is
        //we doesnot have any idea about a team member if he is approved or not so we need to call the api to get data from backend
        
          this.teamObj.allTeamMemberList = this.teamObj.allTeamMemberList.filter(member => !this.selectedTeamEntries.some(entry => +entry.empId == +member.empId));
          let totalRequirement = this.projectRequirementsList.totalRequirements;
          console.log("Project obj id is",this.projectObj.id)
          this.getProjectAssignedDataByProjectIdfunc(this.projectObj.id,1,totalRequirement);
          // after removing the members what we are doing in here is we are getting all the team list that is in the project then getting the exact team fromwhich we have deletaed the 
          //members then after getting it we are updating the members here so the count value will look perfect without hitting any api.and will be updated simultaneously.
          let currentTeam = this.allTeamList.filter(team => team.teamId == this.teamObj.teamId);
         
          let currentTeamIndex = this.allTeamList.findIndex(team => team.teamId == this.teamObj.teamId);
          // console.log("before removing team members from current team",currentTeam);
          currentTeam = currentTeam[0].teamMemberList.filter(member => !this.selectedTeamEntries.some(entry => +entry.empId == +member.empId));
          // console.log("After removing it",currentTeam);
          this.allTeamList[currentTeamIndex].teamMemberList = currentTeam;
          
          //This are for to update the counts in the page.

          //25 Oct comment started
          // this.RbacShankhProjects(this.projectFilterDTO);
          // this.RbacInternalProjects(this.projectFilterDTO);
          // this.RbacBothShankhInternal(this.projectFilterDTO);
          // this.ProjectLessEmployees(this.projectFilterDTO);
          // this.RbacAllShankhInternalProjects(this.projectFilterDTO);
          // this.ExceptionEmployeeReport(this.projectFilterDTO);
          // this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);
          // this.getEmployeesWithoutBillability(this.projectFilterDTO);
          //25 Oct comment ended
          this.getAllEmployeeGroupCount();
          //  this.fetchTimesheetMissingCount();
         
          //  this.initializeExpiredProjectFilters();
          //  this.selectedExpiredProjectFilter = this.expiredProjectFilters[0];
      
          //  this.RbacShankhProjects(this.projectFilterDTO);
      
          //  this.ExceptionEmployeeReport(this.projectFilterDTO);
          this.updateProjectCompletionModalRef.hide();
          this.openremoveResourceModal(response.serviceResponse);
        }
        else{
          this.updateProjectCompletionModalRef.hide();
          this.openremoveResourceModal("Unable to delete. Something went wrong.");
          
        }
        this.selectedTeamEntries = [];
      });
      
  }

  filterMappedProjects() {
    const lowerText = this.searchMappedProjectText.trim().toLowerCase();
    const filteredOtherProjectList = this.otherProjectList.filter(project =>
      project.projectName.toLowerCase().includes(lowerText)
    );
  }

  clearSelectionMappedProjects(event: Event): void {
    event.stopPropagation();
    // binding variable
    // this.otherProjectList = [];
  }

  filterOtherProjects() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredOtherProjectList = this.otherProjectList.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  setDefaultProjectUpdateBillable(empId, projectId) {
    this.defaultProjectUpdate.empIds = [empId];
    this.defaultProjectUpdate.projectId = projectId;
    this.defaultProjectUpdate.updatedBy = this.currentUser.empId;
    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error setting the default project!");
      }
    });
  }
  setDefaultProjectUpdateForActiveProject(details: any, projectId, template: TemplateRef<any>) {
    console.log("test id", details.empId, details);
    this.defaultProjectUpdate.empIds = [details.empId];
    this.defaultProjectUpdate.projectId = projectId;
    this.defaultProjectUpdate.createdBy = this.currentUser.empId;
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    if (!this.defaultProjectUpdate.projectId) {
      this.openAlertMod6(this.alertTemplateWithoutReload, "Please update Default Project");
      return;
    }

    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod6(this.alertTemplateWithoutReload, response.serviceResponse);
        this.activeProjects = this.activeProjects.filter(id => id !== details.empId);
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = this.currentProjectDetails;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        const alertModalSub = this.modalRef6.onHidden?.subscribe(() => {
          alertModalSub.unsubscribe();
          if (this.EmployessIds.length === 0 && this.activeProjects.length === 0) {
            this.modalRef.hide();
            this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
          }
          if (this.EmployessIds.length !== 0 && this.activeProjects.length === 0) {
            this.modalRef.hide();
            this.openAlertMod6(this.alertTemplateWithoutReload, "Please update the default project of employees who are not mapped to other active projects.");
          }
        });
      } else {
        this.openAlertMod6(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error setting the default project!");
      }
    });
  }
  deleteResourceFromTeamModal(template: TemplateRef<any>, projectObj, teamId, empId) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj2 = projectObj;
    this.projectObj2.teamId = teamId;
    this.projectObj2.empId = empId;
  }

  getProjectDetailsForBulkDefaultUpdate() {
    this.resourceManagementService.getProjectDetailsForBulkDefaultUpdate().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectListBulk = response.serviceResponse;
        this.benchProjectListBulk = this.projectListBulk.benchProjectList;
        this.otherProjectListBulk = this.projectListBulk.otherProjectList;
        this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk;
        this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk;
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Project List");
        console.error("Unable to fetch Project List!");
      }
    });
  }

  filterProjectsForDefaultBulkBench() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  filterProjectsForDefaultBulkOther() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  getTeamListForSelectedProject() {
    let selectedProjectId = this.setDefaultProjectObj.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = this.bulkProjectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }

//   getTeamListForSelectedProject1(emp) {
//     let selectedProjectId = emp.projectId;


//     if (!selectedProjectId || !this.bulkProjectType) {
//         this.teamListBulk = [];
//         return;
//     }

//     const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
//     this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    
//     if (this.teamListBulk) {
//         this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
//         this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
//     }
// }
// getTeamListForSelectedProject1(emp) {
//     let selectedProjectId = emp.projectId;

//     if (!selectedProjectId || !emp.projectType) {
//         this.teamListBulk = null;
//         this.filteredTeamsForDefaultBulk = [];
//         this.resourceRequirementListBulk = [];
//         return;
//     }

//     const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
//     this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    
//     if (this.teamListBulk && this.teamListBulk.teamList) {
//         this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
//         this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
//     } else {
//         this.filteredTeamsForDefaultBulk = [];
//         this.resourceRequirementListBulk = [];
//     }
// }

  getTeamListForSelectedProject1(emp) {
    let selectedProjectId = emp.projectId;

    if (!selectedProjectId || !emp.projectType) {
      this.teamListBulk = null;
      this.filteredTeamsForDefaultBulk = [];
      this.resourceRequirementListBulk = [];
      this.filteredResourceRequirementListBulk = [];
      return;
    }

    const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);

    if (this.teamListBulk) {
      // this is hanlding teams 
      if (this.teamListBulk.teamList) {
        this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
      } else {
        this.filteredTeamsForDefaultBulk = [];
      }

      // this is handling requiremnts 
      if (this.teamListBulk.resourceRequirement) {
        this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
        this.filteredResourceRequirementListBulk = this.teamListBulk.resourceRequirement;
      } else {
        this.resourceRequirementListBulk = [];
        this.filteredResourceRequirementListBulk = [];
      }
    } else {
      this.filteredTeamsForDefaultBulk = [];
      this.resourceRequirementListBulk = [];
      this.filteredResourceRequirementListBulk = [];
    }
  }

  filterTeamsForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
  }  
filteredResourceRequirementListBulk:any;
searchTermRequirement:any;

  filterResourceRequirementForDefaultBulk() {
    const lowerSearch = this.searchTermRequirement.toLowerCase();
    console.log(this.resourceRequirementListBulk , "+++++++++++++++++++++++++++++++++++++++++++++++++++");
    this.filteredResourceRequirementListBulk = this.resourceRequirementListBulk.filter(req =>
        req.role.toLowerCase().includes(lowerSearch) ||
        req.department.toLowerCase().includes(lowerSearch) ||
        req.experience.toString().includes(lowerSearch)
    );
    console.log(this.resourceRequirementListBulk , "+++++++++++++++++++++++++++++++++++++++++++++++++++");
  }
searchTextProject:any;
projects: any[] = [];
filteredProjects: any[] = [];



 filterProjects(emp) {
    const lowerText = this.searchTextProject.toLowerCase();

    if (lowerText.trim() === '') {
       
        this.filteredProjectsForDefaultBulkBench = [...this.benchProjectListBulk];
        this.filteredProjectsForDefaultBulkOther = [...this.otherProjectListBulk];
    } else {
      
        if (emp.projectType === 'other') {
           
            this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk.filter(project =>
                project.projectName.toLowerCase().includes(lowerText)
                
            );
        } else if (emp.projectType === 'bench') {
         
            this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk.filter(project =>
                project.projectName.toLowerCase().includes(lowerText)
            );
        }
    }
    this.searchTextProject = '';   
}

  getEmployeeInformationBulk(empIds) {
    this.resourceManagementService.getEmployeeInformationBulk(empIds).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeList = response.serviceResponse;
      } else {
        if (this.EmployessIds.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }

      }
    });
  }

  setProjectMappingAndDefaultProject(template: TemplateRef<any>, emp) {
    console.log("empId", emp, emp.empId);
    this.setDefaultProjectObj.empId = [emp.empId];
    this.setDefaultProjectObj.createdBy = this.currentUser.empId;
    this.setDefaultProjectObj.projectId = emp.projectId;
    this.setDefaultProjectObj.teamId = emp.teamId;
    this.setDefaultProjectObj.employeeRole = emp.employeeRole;
    this.setDefaultProjectObj.resourceOverViewId = emp.resourceOverViewId;
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    if (
      !this.setDefaultProjectObj.projectId ||
      !this.setDefaultProjectObj.teamId ||
      !this.setDefaultProjectObj.employeeRole
    ) {
      this.openAlertMod3(this.alertTemplateWithoutReload, 'Project, Team, and Employee Role must be selected.');
      // alert('Project, Team, and Employee Role must be selected.');
      return;
    }
    if (this.setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(this.setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          console.log("The Response is",response.serviceResponse);
          // this.modalRef4.hide();
          // this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
          
          this.openremoveResourceModal(response.serviceResponse);
          this.EmployessIds = this.EmployessIds.filter(id => id !== emp.empId);
          this.getEmployeeInformationBulk(this.EmployessIds);
          console.log("empId", this.activeProjects.length, this.EmployessIds.length);
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef4.hide();
            this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
          }
          if (this.EmployessIds.length === 0 && this.activeProjects.length !== 0) {
            this.modalRef4.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project of employees who are currently mapped to other active projects.");
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }


  setProjectMappingAndDefaultProjectBulk(setDefaultProjectObj, template: TemplateRef<any>) {

    setDefaultProjectObj.empId = this.EmployessIds;
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    if (
      !setDefaultProjectObj.projectId ||
      !setDefaultProjectObj.teamId ||
      !setDefaultProjectObj.employeeRole
    ) {
      this.openAlertMod3(this.alertTemplateWithoutReload, 'Project, Team, and Employee Role must be selected.');
      // alert('Project, Team, and Employee Role must be selected.');
      return;
    }

    if (setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          this.EmployessIds = [];
          setDefaultProjectObj = [];
          // this.getEmployeeInformationBulk(this.EmployessIds);
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef4.hide();
            this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
          }
          if (this.EmployessIds.length === 0 && this.activeProjects.length !== 0) {
            this.modalRef4.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project  of employees who are currently mapped to other active projects.");
          }

        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }

  bulkEmployeeListActiveList: any[] = [];
  getEmployeeInformationForDefaultProject(setDefaultProjectObj: any) {

    this.resourceManagementService.getEmployeeInformationForDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeListActiveList = response.serviceResponse;
        console.error("Unable to fetch Employee List!", this.bulkEmployeeListActiveList);
        // this.modalRef = this.modalService.show(template, { class: 'modal-sm' });

      } else {
        if (this.activeProjects.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }

      }
    });
  }

  getBenchEmployeeMoreThan30Days(projectFilterDTO: any) {
    this.resourceManagementService.getBenchEmployeeMoreThan30Days(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.employeeBenchRepot = response.serviceResponse;
        this.employeeBench = this.employeeBenchRepot.length;
        // console.log("this.employeeBenchRepot", this.employeeBenchRepot)
      }
      else {
        // this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
        this.employeeBenchRepotMsg = response.serviceResponse;
        this.employeeBench = 0;
      }
    });
  }

  goToReportList() {
    this.router.navigate(['/user-reports/report-list'], {
      state: { returnUrl: this.router.url }
    });
  }

  hideTeamMemberModal(): void {
    if (this.modalRefTeamMember) {
      this.modalRefTeamMember.hide();
    }
  }

  displayTeamLead(emp: any): string {
    console.log("emp", emp)
    return emp ? `${emp.name}` : '';
  }

  onTeamLeadSelected(event: any) {
    const selectedTeamLead = event.option.value;
    this.teamObj.teamLeadId = selectedTeamLead.empId;
  }

  filterTeamLeads(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.teamLeadsList.filter(emp =>
      emp.name.toLowerCase().includes(lowerText) ||
      emp.employmentId.toLowerCase().includes(lowerText)
    );
  }

  createDepartmentArray() {
    this.allProject_Po_Internal.forEach(project => {
      if (Array.isArray(project.combinedProjectType)) {
        project.combinedProjectType.forEach((type: any) => {
          if (type.deptId) {
            // Split comma-separated string, trim and convert to numbers
            const deptIds = type.deptId.split(',')
              .map((id: string) => parseInt(id.trim()))
              .filter(id => !isNaN(id));

            // Match with this.departments to get names
            const departmentNames = deptIds.map(id => {
              const match = this.departments.find(dep => dep.deptId === id);
              return match ? match.deptName : null;
            }).filter(name => name !== null);

            // Assign to type.department
            type.department = departmentNames;
          } else {
            type.department = [];
          }
        });
      }
    });
  }

  clearField() {
    this.bulkProjectType = '';
    this.setDefaultProjectObj.projectId = '';
    this.setDefaultProjectObj.teamId = '';
    this.setDefaultProjectObj.employeeRole = '';
    this.setDefaultProjectObj.resourceOverViewId = '';
  }

  openProjectLineItemListModal() {
    this.projectLineItemListModalRef = this.modalService.show(this.projectLineItemListModal, { class: 'modal-xl' });
  }

  closeProjectLineItemListModal() {
    this.projectLineItemListModalRef.hide();
  }

showProjectMilestones(projectObj: any) {
    this.fcProjectMilestoneList = [];
     this.isLoadingMilestones = true; 
    
    let projectObjTemp = new Project();
    projectObjTemp.poProjectId = projectObj?.id;

    console.log("Fetching milestones for project:", projectObjTemp);

    this.projectService.getAllProjectFCLineItemListByProjectId(projectObjTemp)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            console.log("Milestone data received:", response.serviceResponse);
            this.fcProjectMilestoneList = response.serviceResponse;
          } else {
            console.error("Error fetching milestones:", response.serviceResponse);
            this.openAlertMod(this.alertMessageTemplateRef, response.serviceResponse);
          }
           this.isLoadingMilestones = false; 
        },
        error: (err) => {
            console.error("HTTP error fetching milestones:", err);
            this.openAlertMod(this.alertMessageTemplateRef, "An unexpected error occurred while fetching milestones.");
             this.isLoadingMilestones = false; 
        }
    });
}

   switchModalTab(tabName: 'info' | 'milestone') {
    this.activeModalTab = tabName;

    // If switching to the milestone tab, fetch the data
    if (tabName === 'milestone') {
      this.showProjectMilestones(this.projectObj);
    //    setTimeout(() => {
    //   this.openProjectLineItemListModal();
    // }, 100);
    }
  }








  calculatePoStatus() {
    let notStarted = 0, completed = 0, hold = 0, inProgress = 0;
    let lineItemList = this.fcProjectMilestoneList.filter(milestone => milestone.lineItemId == this.projectMilestone.lineItemId);
    for (let m of lineItemList) {
      if (m.status == Status.IN_PROGRESS) {
        inProgress++;
      } else if (m.status == Status.COMPLETED) {
        completed++;
      } else if (m.status == Status.ON_HOLD) {
        hold++;
      } else if (m.status == Status.NOT_STARTED) {
        notStarted++;
      }
    }
    if (inProgress > 0) { this.projectMilestone.lineItemStatus = Status.IN_PROGRESS }
    else if (hold > 0) { this.projectMilestone.lineItemStatus = Status.ON_HOLD }
    else if (notStarted > 0 && notStarted < lineItemList?.length) { this.projectMilestone.lineItemStatus = Status.IN_PROGRESS }
    else if (completed > 0) { this.projectMilestone.lineItemStatus = Status.COMPLETED }

   const allCompleted =
    this.fcProjectMilestoneList.length > 0 &&
    this.fcProjectMilestoneList.every(m => m.status === Status.COMPLETED);

  if (allCompleted) {
    this.confirmComplete();
  } else {
   
    this.updateMilestone();
    this.closeUpdateProjectMilestoneModal();
  }

  }
shouldReload: boolean = false;
confirmComplete() {

  this.projectObj.projectCompletionDate = new Date();

  this.projectObj.projectType = this.projectObj.poProjectType;
  this.projectObj.projectStatus="Completed";

  this.resourceManagementService
    .completionDateOfProject(this.projectObj)
    .pipe(first())
    .subscribe(
      (response: any) => {

        if (response.serviceStatus === "Success") {
          console.log('Selected Date:', response.serviceResponse);
          this.openUpdateProjectCompletionModal(
            "Since all milestones are completed, the project is marked as complete.", 
            true
          );
        } else {
          this.openUpdateProjectCompletionModal(response.serviceResponse, false);
        }
      },
      (error) => {

        this.openUpdateProjectCompletionModal("Something went wrong while completing the project.");
      }
    );
}

cancelComplete() {
  this.confirmCompleteTemplateModalRef.hide(); 
}


  sortProjectMilestoneData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.projectMilestoneSortColumn = sortParams[0];
      this.projectMilestoneSortColumnType = sortParams[1];
      this.projectMilestoneSortDirection = sort.direction;
      this.showProjectMilestones(this.projectObj);
    }
  }

  handleProjectMilestonePageChange(event) {
    this.projectMilestonepage = event;
  }

  openUpdateProjectMilestoneModal(milestone: any) {
    // this.projectMilestone = new ProjectM  ;
    // this.projectMilestone = milestone;
    this.projectMilestone = JSON.parse(JSON.stringify(milestone));
    this.updateProjectMilestoneModalRef = this.modalService.show(this.updateProjectMilestoneModal, { class: 'modal-xl' });
  }

  closeUpdateProjectMilestoneModal() {
    this.updateProjectMilestoneModalRef.hide();
  }

  updateMilestone(): void {
    this.modalRef = this.modalService.show(this.updateProjectMilestoneSuccessModal, {
      class: 'modal-sm'
    });
  }

  viewDocument(): void {
    this.modalRef = this.modalService.show(this.milestoneDocumentModal, {
      class: 'modal-xm'
    });
  }
  closeModalViewDocument() {
    this.modalRef.hide();
  }

  CancelUpdateMilestonePopup() {
    this.modalRef.hide();
  }


  onFileSelected(event: any): void {
    const file: File = event.target.files[0];

    this.selectedFile = null;
    this.selectedFilePreviewUrl = null;


    if(!file){return ;}

    if (file) {
      const allowedTypes = ['image/jpeg', 'image/png'];

      if (!allowedTypes.includes(file.type)) {
        alert('Invalid file type. Please upload only PDF, JPG, JPEG, or PNG files.');
        event.target.value = '';
        this.selectedFile = null;
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
      this.selectedFilePreviewUrl = reader.result as string;};    
      reader.readAsDataURL(file);

      this.selectedFile = file;

    }
  }
    previewSelectedFile(): void {
    if (!this.selectedFile || !this.selectedFilePreviewUrl) {
      alert('Please select a file to preview.');
      return;
    }

    this.dialog.open(ViewImageComponent, {
      width: '80%',
      data: {
        imageUrl: this.selectedFilePreviewUrl,
        fileName: this.selectedFile.name
      }
    });
  }

  openAlertModForMilestone(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }




 private requiresDocument(status: string): boolean {
  return status?.trim() === Status.COMPLETED || status?.trim() === Status.ON_HOLD;
}


  isLoadingMilestone:boolean=false;
  updateMilestoneChanges() {
    // Validate required fields

    let isValid = true;
    let errors: any;
    if (!this.projectMilestone.startDate) {
    this.openAlertMod(this.alertTemplateForMilestone, 'Start date is required for milestone');
    return;
  }

  if (!this.projectMilestone.endDate) {
    this.openAlertMod(this.alertTemplateForMilestone, 'End date is required for milestone');
    return;
  }

  if (this.projectMilestone.startDate > this.projectMilestone.endDate) {
    this.openAlertMod(this.alertTemplateForMilestone, 'End date must be after start date for milestone');
    return;
  }

  if (!this.projectMilestone.status || this.projectMilestone.status.trim().length === 0) {
    this.openAlertMod(this.alertTemplateForMilestone, 'Status is required for milestone');
    return;
  }
if (this.requiresDocument(this.projectMilestone.status) && !this.selectedFile) {
  this.openAlertMod(this.alertTemplateForMilestone, 'Please upload a document when completing/holding a milestone');
  return;
}


  if(this.projectObj.projectStatus==="Completed"){
    this.openAlertMod(this.alertTemplateForMilestone, 'Project is already completed. You cannot update the milestone.');
    return;
  }
    console.log("before updaed by updated on", this.projectMilestone);

    this.projectMilestone.updatedBy = this.currentUser.empId;
    this.projectMilestone.updatedOn = new Date();

    const formData = new FormData();
    formData.append('dto', new Blob([JSON.stringify(this.projectMilestone)], { type: 'application/json' }));

    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }
    console.log("after updaed by updated on", this.projectMilestone);


    this.isLoadingMilestone=true;


   
    this.projectService.updateMilestoneById(formData).pipe(first()).subscribe({
  next: (response: any) => {
    this.isLoadingMilestone = false;

    if (response.serviceStatus === "Success") {
        this.selectedFile = null;
       const index = this.fcProjectMilestoneList.findIndex(m => m.id === this.projectMilestone.id);
    if (index > -1) {
      this.fcProjectMilestoneList[index] = { ...this.projectMilestone };
    }
      this.calculatePoStatus();
      this.closeUpdateProjectMilestoneModal(); 
      this.showProjectMilestones(this.projectObj);
    } else {
      this.openAlertMod(this.alertTemplateForMilestone, response.serviceResponse || "Failed to update milestone.");
      this.showProjectMilestones(this.projectObj);
    }
  },
  error: (err) => {
    this.isLoadingMilestone = false;
    this.openAlertMod(this.alertTemplateForMilestone, "Error updating milestone: " + err.message);
  }
});
      this.closeUpdateProjectMilestoneModal(); 

  }




  // viewMilestoneFile(milestoneId: number): void {
  //   this.projectService.getMilestoneById(milestoneId).subscribe({
  //     next: (res) => {
  //       if (res.serviceStatus === 'Success') {
  //         const milestone = res.serviceResponse;
  //         if (milestone.documentBase64) {
  //           const mimeType = this.getMimeType(milestone.documentName);
  //           const base64Data = `data:${mimeType};base64,${milestone.documentContent}`;
  //           this.milestoneDocumentUrl = this.sanitizer.bypassSecurityTrustResourceUrl(base64Data);
  //           setTimeout(() => {
  //             this.viewDocument();
  //           }, 2000);

  //         } else {
  //           alert('No document available for this milestone.');
  //         }
  //       } else {
  //         console.error(res.serviceResponse);
  //       }
  //     },
  //     error: (err) => {
  //       console.error('Failed to retrieve milestone:', err);
  //     }
  //   });
  // }


  getFileType(filename: string): string {
  const extension = filename.split('.').pop()?.toLowerCase();
  switch (extension) {
    case 'png': return 'image/png';
    case 'jpg':
    case 'jpeg': return 'image/jpeg';
    case 'pdf': return 'application/pdf';
    default: return 'application/octet-stream';
  }
}

clearSelectedFile(fileInput: HTMLInputElement) {
  this.selectedFile = null;
  fileInput.value = ''; 
}

  viewFiles(mileStoneId: any) {
    this.isLoadingMilestone = true;
    this.projectService.getMilestoneById(mileStoneId).subscribe((res: any) => {
       this.isLoadingMilestone = false;
      
       

        if (res.documentContent && res.documentName) {
          const fileType = this.getFileType(res.documentName);
          const imageDataUrl = `data:${fileType};base64,${res.documentContent}`;
          console.log("image url" + imageDataUrl)
          this.dialog.open(ViewImageComponent, {
            width: '80%',
            data: {
              imageUrl: imageDataUrl,
              fileName: res.documentName
            }
          });
        } else {
          this.notificationService.showErrorMessage("image is not available")
        }
      

    },
      (error) => {
        this.notificationService.showErrorMessage(error.error.message);
      }
    )


  }



  getMimeType(fileName: string): string {
    const ext = fileName?.split('.').pop()?.toLowerCase();
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      default: return 'application/octet-stream';
    }
  }

 getAllDepartmentListAndSelectByProjectDeptId(project: any) {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        const deptIds = project?.deptId
          ?.split(",")
          .map(id => Number(id.trim()))
          .filter(id => !isNaN(id));

        const departmentNames = this.allDeptList
          .filter(x => deptIds?.includes(x.deptId))
          .map(x => x.name);



        this.projectObj.department = departmentNames;

      } else {
        console.error(response.serviceResponse);
      }
    });
  }






expiredProjectDisplayCount: number | null = null;



  openClientSideIdPresent(template: TemplateRef<any>,projectId:any) {
    this.clientSideIdPresent = this.modalService.show(template, { class: 'modal-md' });
    this.clientSideIdObj.projectId = projectId;
    this.fetchHasClientSideId(projectId);
  }

  hideClientSideIdPresent(): void {
    if (this.clientSideIdPresent) {
      this.clientSideIdPresent.hide();
    }
  }

  onClientSideIdOk() {
    this.updateHasClientSideId(this.hasClientSideIdFlag);
  }

  updateHasClientSideId(flag:Boolean){
    this.clientSideIdObj.hasClientSideId = flag;
    this.clientSideIdObj.currentUserEmpId = this.currentUser.empId;
    this.hideClientSideIdPresent();
    this.resourceManagementService.updateHasClientSideId(this.clientSideIdObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.hasClientSideIdFlag = false;
      } else {
        this.openAlertMessageModal(response.serviceResponse)
      }
    });
    this.hasClientSideIdFlag = false;
  }

  getActiveProjectList(){
    this.resourceManagementService.getActiveProjectList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projList = response.serviceResponse;
      } else {
        this.openAlertMod(this.alertTemplateWithoutReload, response.serviceResponse);
      }
    });
  }

  liftAndShiftTeams(){
    this.liftAndShiftObj.currentUserEmpId = this.currentUser.empId;
    this.liftAndShiftObj.teamIds = this.selectedTeamsDetails.map(team => team.teamId);
    this.resourceManagementService.liftAndShiftTeams(this.liftAndShiftObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertModWithReload(this.modalRefWithReloadTemp,response.serviceResponse);
      } else {
        this.openAlertMod(this.alertTemplateWithoutReload, response.serviceResponse);
      }
    });
  }

  openLiftAndShiftTeamsMod(projectObj:any) {
    this.liftAndShiftObj.sourceProjectId = projectObj.projectId;
    this.getActiveProjectList();
    this.liftAndShiftRef = this.modalService.show(this.liftAndShiftTeamsTemp, { class: 'modal-lg' });
  }

  hideLiftAndShiftTeamsMod() {
    this.liftAndShiftRef.hide();
  }

  fetchHasClientSideId(projectId:any){
    this.clientSideIdObj.projectId = projectId;
    this.resourceManagementService.fetchHasClientSideId(this.clientSideIdObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.fetchClientSideIdObj = response.serviceResponse;
        this.hasClientSideIdFlag = this.fetchClientSideIdObj.hasClientSideId;
      } else {
        this.openAlertMod(this.alertTemplateWithoutReload, response.serviceResponse)
      }
    });
  }


//    activeProjectDisplayCount: number | null = null;
//   selectedActiveProjectFilter: any;
//   activeProjectFilters: any[] = [];


//  initializeActiveProjectFilters() {
//     this.activeProjectFilters = [
//       { 
//         label: 'Total', 
//         key: 'allTotalActiveProjectCount', 
//         title: 'All Active Projects' 
//       },
//       { 
//         label: '<31', 
//         key: 'allActiveProjectsUpToMarch31Count', 
//         title: 'Projects Active Up To March 31' 
//       },
//       { 
//         label: '>31', 
//         key: 'activeProjectGreaterThan31MarchCount', 
//         title: 'Projects Active After March 31' 
//       },
//       { 
//         label: 'Int', 
//         key: 'allInternalActiveProjectsCounts', 
//         title: 'Internal Active Projects' 
//       }
//     ];
   
//     this.selectedActiveProjectFilter = this.activeProjectFilters[0];
//   }

//   selectActiveProjectFilter(filter: any) {
//     this.selectedActiveProjectFilter = filter;
//     if (this.tabCounts) {
//       this.activeProjectDisplayCount = this.tabCounts[filter.key];
//     }
//   }


getCurrentExpiredCount(): number {
    if (!this.tabCounts) return 0;
    if (this.selectedExpiredTNMProjectFilter && this.selectedExpiredTNMProjectFilter.key) {
        return this.tabCounts[this.selectedExpiredTNMProjectFilter.key] || 0;
    }
    return this.tabCounts.allExpiredTNMProjectsCount || 0;
}

initializeExpiredProjectFilters() {
  this.selectedExpiredTNMProjectFilter = this.EXPIRED_TNM_PROJECT_FILTERS_LIST[0];
}



showMoreCards: boolean = false;

totalCards: number = 9;
cardsPerLevel: number = 3;
maxCardLevels: number = 0;
currentCardLevel: number = 1;


toggleMoreCards() {
  if (this.currentCardLevel >= this.maxCardLevels) {
    this.currentCardLevel = 1;  // Reset to show first level
  } else {
    this.currentCardLevel++;    // Show next level
  }
}


selectedDepartments: string[] = [];
allDepartmentsGraph: string[] = [];
showDepartmentFilter: boolean = true;
orgChartData: any[] = [];
filterError: string | null = null;






renderColumnChart1(chartName: any, chartId: any, chartData: any, categories: any, yAxisTitle: string) {
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
        text: 'Clients'
      },
      labels: {
        rotation: -45,
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
      headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
      pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
        '<td style="padding:0"><b>{point.y}</b></td></tr>',
      footerFormat: '</table>',
      shared: true,
      useHTML: true
    },
    plotOptions: {
      column: {
        dataLabels: {
          enabled: true,
          format: '{y}',
          style: {
            fontSize: '10px',
          },
          // Only show labels for values > 0
          formatter: function() {
            return this.y > 0 ? this.y : '';
          }
        },
        pointPadding: 0.2,
        borderWidth: 0
      }
    },
    credits: {
      enabled: false,
    },
    legend: {
      enabled: true,
      layout: 'horizontal',
      align: 'center',
      verticalAlign: 'bottom'
    },
    series: chartData
  });
}



tableData: any[][] = [];
maxRows: number = 0;

 allDepartments = [
      { deptId: 1, name: 'Super Admin', isBillable: false },
      { deptId: 2, name: 'Accounts', isBillable: false },
      { deptId: 3, name: 'APM', isBillable: true },
      { deptId: 4, name: 'Application Performance Monitoring', isBillable: true },
      { deptId: 5, name: 'Automation Testing', isBillable: true },
      { deptId: 6, name: 'Business Development', isBillable: false },
      { deptId: 7, name: 'Development', isBillable: true },
      { deptId: 8, name: 'Functional Testing', isBillable: true },
      { deptId: 9, name: 'HR', isBillable: false },
      { deptId: 10, name: 'IT', isBillable: false },
      { deptId: 11, name: 'Performance Testing', isBillable: true },
      { deptId: 12, name: 'Production Support', isBillable: true },
      { deptId: 13, name: 'Security Testing', isBillable: true },
      { deptId: 14, name: 'Admin', isBillable: false },
      { deptId: 15, name: 'Director', isBillable: false },
      { deptId: 16, name: 'Resource Management Group', isBillable: false },
      { deptId: 18, name: 'Presales', isBillable: false },
      { deptId: 20, name: 'Production Support 24x7', isBillable: true },
      { deptId: 21, name: 'Unknown Department', isBillable: false },
      { deptId: 25, name: 'RPA', isBillable: true },
      { deptId: 26, name: 'Products and RND', isBillable: true },
      { deptId: 27, name: 'Consultant', isBillable: false },
      { deptId: 28, name: 'Training', isBillable: false },
      { deptId: 29, name: 'Floor Automation', isBillable: true }
    ];

 hideModalRefWithoutReload2() {
    this.modalRefWithoutReload2.hide();
    if(this.projectDetails.length === 0){
      this.modalRef2.hide();
    }
  }




  getChartsByDepartment() {
    let charts = this.orgChartData;

    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const lowerCaseSearchTerm = this.searchTerm.toLowerCase().trim();
      charts = this.orgChartData.filter(deptNode =>
        deptNode.name.toLowerCase().includes(lowerCaseSearchTerm)
      );
    }
    
    return charts.map(deptNode => [deptNode]);
  }


getColumnHeaders(): string[] {
  if (this.tableData.length === 0) return [];
  return this.tableData[0]; 
}

isDepartmentOrProjectHeader(rowIndex: number, colIndex: number): boolean {
  if (rowIndex !== 0) return false;
  
  const currentCell = this.tableData[rowIndex][colIndex];
  return currentCell === 'Projects' || (currentCell !== '' && colIndex % 2 === 0);
}

truncateToWords(text: string, wordLimit: number): string {
  if (!text) return '';
  const words = text.split(' ');
  if (words.length <= wordLimit) return text;
  return words.slice(0, wordLimit).join(' ') + '...';
}


getFullProjectName(projectName: string): string {
  return this.projectFullText.get(projectName) || projectName;
}


isProjectExpanded(projectName: string): boolean {
  return this.expandedProjects.has(projectName);
}

expandedColumns: Set<string> = new Set(); 
expandAllProjects: boolean = false;



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

getCellFullText(cell: any): string {
  if (typeof cell === 'string') {
    return cell;
  }
  if (cell && cell.name) {
    return cell.name;
  }
  return '';
}

truncateText1(text: string, charLimit: number = 15): string {
  if (!text || text.length <= charLimit) return text;
  return text.substring(0, charLimit) + '...';
}

truncateText(text: string, charLimit: number = 15): string {
  if (!text || text.length <= charLimit) return text;

  const startIndex = text.length - charLimit + 3; 
  return '...' + text.substring(startIndex);
}

isTextTruncated(text: string): boolean {
  return text && text.length > 15;
}

getDisplayProjectName(projectName: string, departmentName?: string): string {
  if (!projectName) return '';
  
  this.projectFullText.set(projectName, projectName);
  
  const isColumnExpanded = departmentName && this.expandedColumns.has(departmentName);
  const isIndividualExpanded = this.isProjectExpanded(projectName);
  
  if (isColumnExpanded || isIndividualExpanded) {
    return projectName;
  }
  
  return this.truncateText(projectName, 15);
}

getDisplayClientName(clientName: string, departmentName?: string): string {
  if (!clientName) return '';
  
  this.projectFullText.set(clientName, clientName);
  
  const isColumnExpanded = departmentName && this.expandedColumns.has(departmentName);
  const isIndividualExpanded = this.isProjectExpanded(clientName); 
  
  if (isColumnExpanded || isIndividualExpanded) {
    return clientName;
  }
  
  return this.truncateText1(clientName, 15);
}

isProjectTruncated(text: string): boolean {
  return this.isTextTruncated(text);
}

toggleExpandAllProjects(departmentName: string): void {
  if (this.expandedColumns.has(departmentName)) {
    this.expandedColumns.delete(departmentName);
  } else {
    this.expandedColumns.add(departmentName);
  }
}



isColumnExpanded(departmentName: string): boolean {
  return this.expandedColumns.has(departmentName);
}

toggleProjectExpansion(text: string): void {
  if (this.expandedProjects.has(text)) {
    this.expandedProjects.delete(text);
  } else {
    this.expandedProjects.add(text);
  }
}

toggleClientExpansion(clientName: string): void {
  this.toggleProjectExpansion(clientName); }


modalMessage: string = ''; 
openUpdateProjectCompletionModal(message: string, reload: boolean = false): void {
  this.modalMessage = message;
  this.shouldReload = reload;
  if (reload) {
    this.modalRefWithReload = this.modalService.show(this.updateProjectCompletionModal, {
      class: 'modal-dialog modal-sm modal-position-top'
    });
  } else {
    this.updateProjectCompletionModalRef = this.modalService.show(this.updateProjectCompletionModal, {
      class: 'modal-dialog modal-sm modal-position-top'
    });
  }
}
onModalOkClick(): void {
  if (this.shouldReload) {
    this.modalRefWithReload.hide();
    window.location.reload();
  } else {
    if (this.updateProjectCompletionModalRef) {
      this.updateProjectCompletionModalRef.hide();
    }
  }
}

// Close modal
closeUpdateProjectCompletionModal(): void {
  if (this.updateProjectCompletionModalRef) {
    this.updateProjectCompletionModalRef.hide();
  }
}



openremoveResourceModal(message: string): void {
  this.modalMessage = message;
  this.updateProjectCompletionModalRef = this.modalService.show(this.updateProjectCompletionModal, {
    class: 'modal-dialog modal-sm modal-position-top'
  });
}


closeremoveResourceModal(): void {
  if (this.updateProjectCompletionModalRef) {
    this.updateProjectCompletionModalRef.hide();
  }
}
 closeAlert(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.modalRef) {
      this.modalRef.hide();
    }
  }

 
openFcResourceMappedToTNMProjectTemp() {
  this.fcResourceMappedToTNMProjectRef = this.modalService.show(this.fcResourceMappedToTNMProjectTemp, { class: 'modal-sm' });
}

hideFcResourceMappedToTNMProjectTemp() {
  this.fcResourceMappedToTNMProjectRef.hide();
}

openFcResourceMapped_noTemp() {
  // this.removeInputTeamMemberField(teamMember); 
  this.updateEmployeeListAccordingToTeamMembers()
  this.fcResourceMapped_noRef = this.modalService.show(this.fcResourceMappedToTNMProjectTemp, { class: 'modal-sm' });
}

hideFcResourceMapped_noTemp() {
  this.fcResourceMapped_noRef.hide();
}
 
  async getAllResourceRequirementForProject1(project: Project) {
    try{

      let response:any = await this.resourceManagementService.getAllResourceRequirementForProject(project).pipe(first()).toPromise();
      // this.resourceManagementService.getAllResourceRequirementForProject(project).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.projectObj.oldresourceRequirements = response.serviceResponse;
          this.projectObj.oldresourceRequirements = this.projectObj.oldresourceRequirements.filter((item: any) => !this.resourceOverViewIdList.includes(item.resourceOverviewId));
          
          console.log("Old resource requirements ", this.projectObj.oldresourceRequirements);
        }
      }
      catch(error){
        this.poResourceRequirementAlert(error);
      }
    // });
  }

openRestoreInfoTemp(projectId:any) {
  this.selectedProjectId = projectId;
  this.restoreInfoRef = this.modalService.show(this.restoreInfoTemp, { class: 'modal-sm' });
}

hideRestoreInfoTemp() {
  this.restoreInfoRef.hide();
}

restorePreviousStateOfProject(projectId:any){
  this.hideRestoreInfoTemp();
  this.restoreProjectPayload.projectId = projectId;
  this.restoreProjectPayload.currentUserEmpId = this.currentUser.empId;
  this.resourceManagementService.restorePreviousStateOfProject(this.restoreProjectPayload).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.isRestoreSuccess = true; 
      this.openRestoreModal(response.serviceResponse);
    } else {
      this.isRestoreSuccess = false; 
      this.openRestoreModal(response.serviceResponse);
    }
  }, (error:any) => {
    this.loaderService.requestEnded();
    // console.log(" restorePreviousStateOfProject ",JSON.stringify(error));
    this.isRestoreSuccess = false; 
    this.openRestoreModal(error.message);
  });
}

openRestoreModal(message: any) {
  this.restoreAlertRef = this.modalService.show(this.restoreAlertTemp, { class: 'modal-sm' });
  this.alertMessage = message;
}

  hideRestoreModal() {
    this.restoreAlertRef.hide();
    if (this.isRestoreSuccess) {
      this.page = 1;
      this.selectedProjectStatus = this.PROJECT_STATUS.APPROVED.key;
      let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
      newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
      this.loadRMGDashboard(newRmgDashboardProjectRequest);
    }
  }

openAlertMessageMarkAsCompleteTemp(message: any) {
  this.alertMessageMarkAsCompleteRef = this.modalService.show(this.alertMessageMarkAsCompleteTemp, { class: 'modal-sm' });
  this.alertMessage = message;
}

  hideAlertMessageMarkAsCompleteTemp() {
    this.alertMessageMarkAsCompleteRef.hide();
    if (this.isCompletionSuccess) {
      this.page = 1;
      this.selectedProjectStatus = this.PROJECT_STATUS.APPROVED.key;
      let newRmgDashboardProjectRequest = this.getNewRMGRequestObject();
      newRmgDashboardProjectRequest.projectStatus = this.selectedProjectStatus;
      this.loadRMGDashboard(newRmgDashboardProjectRequest);
    }
  } 


  openProjectConfigurationEditModal() {
    this.projectConfigurationModalRef = this.modalService.show(this.projectConfigurationTemplateRef, { class: 'modal-lg' });
  }

  closeProjectConfigurationEditModal() {
    if (this.projectConfigurationModalRef) {
      this.projectConfigurationModalRef.hide();
    }
  }

  getProjectConfigurationDetailsByProjectId(project: any) {
    this.isEditProject = true;
    this.allProjectTable = true;
    this.isHideButton = true;
    this.allTeamList = [];
    this.projectObj = null;
    this.resourceManagementService.getProjectConfigurationDetailsByProjectId(project?.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.activeModalTab = 'info';
        this.projectObj = response.serviceResponse;
        this.getManagerList();
        this.mapProjectManagerNameToProject(this.projectObj);
        this.getAllResourceRequirementForProject(this.projectObj);
        this.getTeamListByProjectName(this.projectObj);
        this.createDepartmentArrayFromDeptIds(this.projectObj);
        this.getEmployeeByNameAndEmpld();
        this.projectConfigurationModalRef = this.modalService.show(this.projectConfigurationTemplateRef, { class: 'modal-lg' });
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
      }
    });
  }

  createDepartmentArrayFromDeptIds(project: any) {
    const departmentNames: string[] = project?.departmentNames
      ? project?.departmentNames.split(',').map(name => name.trim()).filter(name => name.length > 0)
      : [];
    project.department = departmentNames;
  }


  mapProjectManagerNameToProject(project: any) {
    if (!project) {
      return;
    }

    if (project?.projectManagers && Array.isArray(project?.projectManagers) && project?.projectManagers.length > 0) {
      const managerNamesString = project.projectManagers
        .map(manager => manager.projectManagerName)
        .join(', ');
      project.projectManagerName = managerNamesString;
    }
    else {
      project.projectManagerName = project?.projectManager?.trim() ? project?.projectManager.trim() : '';
    }
  }

  fetchProjectDetailsList(projectFilterDTO: any) {
    this.allProject_Po_Internal = [];
    this.totalProjects = 0;
    let rmgProjectRequest = this.mapProjectFilterDTOToRMGRequest(projectFilterDTO);
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList)) {
        const apiResponse = response?.serviceResponse?.projectList;
        this.allProject_Po_Internal = apiResponse?.content || [];
        this.totalProjects = apiResponse?.totalElements || 0;
        this.allProject_Po_Internal.forEach(project => {
          project.createdOn = (project?.createdOn) ? moment(project?.createdOn).format('DD/MM/yyyy') : null;
        });
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong');
      }
    },
      (error) => {
        this.openAlertMessageModal('Something went wrong');
      });
  }

  mapProjectFilterDTOToRMGRequest(projectFilterDTO: any) {
    let rmgProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    rmgProjectRequest.currentUserEmpId = this.currentUser?.empId;
    rmgProjectRequest.currentUserType = this.determineUserType();
    rmgProjectRequest.page = this.page - 1 || 0;
    rmgProjectRequest.pageSize = this.projectPageSize;
    rmgProjectRequest.projectFilter = this.filters;
    rmgProjectRequest.sortColumn = this.sortColumn || 'name';
    rmgProjectRequest.sortDirection = this.sortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.sortColumnType || 'string';
    rmgProjectRequest.departmentIds = projectFilterDTO?.departmentIds;
    rmgProjectRequest.projectStatus = projectFilterDTO?.projectStatus;
    rmgProjectRequest.expiredProjectFilter = projectFilterDTO?.expiredProjectFilter;
    rmgProjectRequest.fixedCostFilter = projectFilterDTO?.fixedCostFilter;
    this.rmgProjectFilterDTO = new RMGDashboardProjectRequest();
    this.rmgProjectFilterDTO.departmentIds = projectFilterDTO?.departmentIds;
    this.rmgProjectFilterDTO.projectStatus = projectFilterDTO?.projectStatus;
    this.rmgProjectFilterDTO.expiredProjectFilter = projectFilterDTO?.expiredProjectFilter;
    this.rmgProjectFilterDTO.fixedCostFilter = projectFilterDTO?.fixedCostFilter;
    return rmgProjectRequest;
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

  poResourceRequirementAlert(message) {
    this.poResourceRequirementFetchRef = this.modalService.show(this.poResourceRequirementFetchTemp, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  closePOResourceRequirementAlert() {
    if (this.poResourceRequirementFetchRef) {
      this.poResourceRequirementFetchRef.hide();
    }
  }

  openAlertMessageModal(modalMessage: any) {
    this.modalMessage = modalMessage;
    this.alertMessageModalRef = this.modalService.show(this.alertMessageTemplateRef, { class: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef.hide();
    }
  }

  handleTableAction(event: { type: string, row: any }) {
    console.log(event?.row);
    switch (event?.type) {
      // case 'view': this.viewUser(event.row); break;
      // case 'edit': this.openEditModal(event.row); break;
      // case 'delete': this.deleteUser(event.row); break;
    }
}

 

































skillsPerPage = 3;
// employeesSkillMatrix = [
//   {
//     name: "Prarthana Lenka",
//     designation: "Development : Full Stack Developer",
//     email: "prarthana.lenka@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer","lala secutity"],
//     skills: ["Angular", "C#", "Azure", "SQL","Java","lala","Angular", "C#", "Azure", "SQL","Java","lala"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//   {
//     name: "John Doe",
//     designation: "Development : Senior Developer",
//     email: "john.doe@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/men/32.jpg",
//     certifications: ["AWS Certified Solutions Architect", "Cloud Architecture"],
//     skills: ["Javascript", "React", "AWS", "Node.js","React", "AWS", "Node.js"],
//     domain: "Cloud",
//     department: "Development",
//     status: "Active",
//      skillIndex: 0
//   },
//   {
//     name: "Jane Smith",
//     designation: "Development : Full Stack Developer",
//     email: "jane.smith@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "C#", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//     {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//    {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//    {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//    {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//    {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   },
//    {
//     name: "SHivtosh Pal",
//     designation: "Development : Full Stack Developer",
//     email: "shivtosh.pal@company.com",
//     imageUrl: "https://randomuser.me/api/portraits/women/44.jpg",
//     certifications: ["Azure Fundamentals", "DevOps Engineer"],
//     skills: ["Angular", "Azure", "SQL"],
//     domain: "Web",
//     department: "Engineering",
//     status: "Expired",
//      skillIndex: 0
//   }
// ];

rankedEmployees:any[] =[];

// dropdown data
certificationList :any[]=[];
skillList:any[]=[];
departmentList :any[]=[];
filtersSkillMatrix: FilterMatrix = new FilterMatrix();

searchTextSkill = '';
filteredSkills: any[] = [];
isAllSkillsSelected = false;

// filterSkills() {
//   const lower = this.searchTextSkill.toLowerCase();
//   this.filteredSkills = this.skillList.filter(skill =>
//     skill.skillName.toLowerCase().includes(lower)
//   );
// }
filterSkills() {
  const lower = this.searchTextSkill.toLowerCase();
  const selectedIds = this.filtersSkillMatrix.skillIds || [];
  this.filteredSkills = this.skillList.filter(skill =>
    skill.skillName.toLowerCase().includes(lower) ||
    selectedIds.includes(skill.skillId)
  );
}


clearSkills(event: Event) {
  event.stopPropagation();
  this.filtersSkillMatrix.skillIds = [];
  this.filtersSkillMatrix.skillNames = [];
  this.isAllSkillsSelected = false;
  this.searchTextSkill = '';
  this.filteredSkills = [...this.skillList];
}

updateSelectedSkillNames() {
  const selectedSkills = this.skillList.filter(skill =>
    this.filtersSkillMatrix.skillIds.includes(skill.skillId)
  );

  this.filtersSkillMatrix.skillNames = selectedSkills.map(skill => skill.skillName);
}

toggleSelectAllSkills() {
  this.isAllSkillsSelected = !this.isAllSkillsSelected;
  this.filtersSkillMatrix.skillIds = this.isAllSkillsSelected
    ? this.filteredSkills.map(s => s.skillId)
    : [];

     this.filtersSkillMatrix.skillNames = this.isAllSkillsSelected
    ? this.filteredSkills.map(s => s.skillName)
    : [];
}

compareById(item1: any, item2: any): boolean {
  return item1 === item2;
}

onSkillSelectionChange() {
  this.isAllSkillsSelected =
    this.filtersSkillMatrix.skillIds.length === this.skillList.length;
}

resetSkillSearch() {
  this.searchTextSkill = '';
  this.filteredSkills = [...this.skillList];
}

getAllPredefinedSkills() {
    this.employeeService.getAllPredefinedSkills().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.skillList = response.serviceResponse;
        this.filteredSkills = this.skillList;
      } else {
        console.error(response.serviceResponse);
      }
       this.checkAllApiResponses();
    });
  }


  getCountOfEmployeeFromSelectedDepartment(){
    this.resourceManagementService.getEmployeeCountSDeptwise(this.filtersSkillMatrix).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.seempCountDeptSelected = response.serviceResponse;
     
      } else {
        console.error(response.serviceResponse);
      }
       
    });

  }




  filterMatrixObj:FilterMatrix = new FilterMatrix();
  isAllCertificatesSelected = false;
  searchTextCertificate :any;
  filteredCertificates: any[] = [];
  getAllCertificatesRbac(){
    this.filterMatrixObj.empId = this.currentUser.empId;
    this.resourceManagementService.getCertficatesRbac(this.filterMatrixObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.certificationList = response.serviceResponse;
          this.filteredCertificates = [...this.certificationList];
      } else {
        console.error(response.serviceResponse);
      }
       this.checkAllApiResponses();
    });
  }



  onCertificateSelectionChange() {
  this.isAllCertificatesSelected =
    this.filtersSkillMatrix.certificateIds.length === this.filteredCertificates.length;
}

// filterCertificates() {
//   const lower = this.searchTextCertificate.toLowerCase();
//   this.filteredCertificates = this.certificationList.filter(cert =>
//     cert.certificateName.toLowerCase().includes(lower)
//   );
// }

filterCertificates() {
  const lower = this.searchTextCertificate.toLowerCase();
  const selectedIds = this.filtersSkillMatrix.certificateIds || [];

  this.filteredCertificates = this.certificationList.filter(cert =>
    cert.certificateName.toLowerCase().includes(lower) ||
    selectedIds.includes(cert.employeeCertificateId)
  );
}

resetCertficateSearch(){
  this.searchTextCertificate = '';
  this.filteredCertificates = [...this.certificationList];
}

clearCertificates(event: Event) {
  // event.stopPropagation();
  this.filtersSkillMatrix.certificateIds = [];
  this.isAllCertificatesSelected = false;
  this.searchTextCertificate = '';
  this.filteredCertificates = [...this.certificationList];
}

toggleSelectAllCertificates() {
  this.isAllCertificatesSelected = !this.isAllCertificatesSelected;
  this.filtersSkillMatrix.certificateIds = this.isAllCertificatesSelected
    ? this.filteredCertificates.map(c => c.employeeCertificateId)
    : [];
}

searchTextDeptMatrix = '';
filteredDepartmentsMatrix: any[] = [];
isAllDepartmentsSelected = false;

filterDepartmentsMatrix() {
  const lower = this.searchTextDeptMatrix.toLowerCase();
 const selectedIds = this.filtersSkillMatrix.deptIds || [];
  this.filteredDepartmentsMatrix = this.departmentList.filter(dept =>
    dept.name.toLowerCase().includes(lower) ||  selectedIds.includes(dept.deptId)
  );
}

clearDepartments(event: Event) {
  event.stopPropagation();
  this.filtersSkillMatrix.deptIds = [];
  this.isAllDepartmentsSelected = false;
  this.searchTextDeptMatrix = '';
  this.filteredDepartmentsMatrix = [...this.departmentList];
}

resetDeptSearch(){
    this.searchTextDeptMatrix = '';
  this.filteredDepartmentsMatrix = [...this.departmentList];
}

toggleSelectAllDepartments() {
  this.isAllDepartmentsSelected = !this.isAllDepartmentsSelected;
  this.filtersSkillMatrix.deptIds = this.isAllDepartmentsSelected
    ? this.filteredDepartmentsMatrix.map(d => d.deptId)
    : [];
}

onDepartmentSelectionChangeMatrix() {
  this.isAllDepartmentsSelected =
    this.filtersSkillMatrix.deptIds.length === this.departmentList.length;
}

searchTextCertDept = '';
filteredCertificationDepartments: any[] = [];
isAllCertificationDepartmentsSelected = false;

filterCertificationDepartments() {
  const lower = this.searchTextCertDept.toLowerCase();
  this.filteredCertificationDepartments = this.departmentList.filter(dept =>
    dept.name.toLowerCase().includes(lower)
  );
}

clearCertificationDepartments(event: Event) {
  event.stopPropagation();
  this.filtersSkillMatrix.certificationDeptIds = [];
  this.isAllCertificationDepartmentsSelected = false;
  this.searchTextCertDept = '';
  this.filteredCertificationDepartments = [...this.departmentList];
}

toggleSelectAllCertificationDepartments() {
  this.isAllCertificationDepartmentsSelected = !this.isAllCertificationDepartmentsSelected;
  this.filtersSkillMatrix.certificationDeptIds = this.isAllCertificationDepartmentsSelected
    ? this.filteredCertificationDepartments.map(d => d.deptId)
    : [];
}

onCertificationDeptSelectionChange() {
  this.isAllCertificationDepartmentsSelected =
    this.filtersSkillMatrix.certificationDeptIds.length === this.departmentList.length;
}

 getAllDepartmentsRbac(){
    this.filterMatrixObj.empId = this.currentUser.empId;
    this.resourceManagementService.getDepartmentsRbac(this.filterMatrixObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.departmentList = response.serviceResponse;
        this.filteredDepartmentsMatrix = this.departmentList;
        this.filteredCertificationDepartments = this.departmentList;
      } else {
        console.error(response.serviceResponse);
      }
       this.checkAllApiResponses();
    });
  }



apiResponsesCount:any;
  openShowSkillMatrix(){
    this.isSkillMatrix= true;
      this.isProjectTable = false;
    this.allProjectTable = false;
     this.isCreateForm = false;
    this.isCreation = false;
   this.apiResponsesCount = 0;
  this.getAllCertificatesRbac();
  this.getAllPredefinedSkills();
  this.getAllDepartmentsRbac();

  }



  applySearchFilter(){
    console.log(this.filtersSkillMatrix);
    this.getAllFilterBasedSearchEmployee();
    this.getCountOfEmployeeFromSelectedDepartment();
    this.getAllEmployyesNotInSearch();
  }


  checkAllApiResponses() {
  this.apiResponsesCount++;
  if (this.apiResponsesCount === 3) {
    this.resetFilters();
    this.apiResponsesCount = 0; 
  }
}

resetFilters() {
  
  this.filtersSkillMatrix.certificateIds = [];

 
  this.filtersSkillMatrix.skillIds = this.skillList?.map(s => s.skillId) || [];

   this.filtersSkillMatrix.skillNames = this.skillList?.map(s => s.skillName) || [];

  
  this.filtersSkillMatrix.deptIds = this.departmentList?.map(d => d.deptId) || [];


  // this.filtersSkillMatrix.certificationDeptIds = [];


  this.filtersSkillMatrix.certificateStatus = 'All';
  this.filtersSkillMatrix.specialization = '';


  this.isAllCertificatesSelected = false;
  this.isAllDepartmentsSelected = true;
  this.isAllCertificationDepartmentsSelected = false;
  this.isAllSkillsSelected = true; 

 
  this.applySearchFilter();
}


wingImages = [
  "assets/Images/goldenwings.gif",
  "assets/Images/silverwings.gif",
  "assets/Images/bronzewings.gif"
];




handleEmployeePageChange(event: number) {
 this.employeeMatrixPageNo = event;
  this.paginationConfig.currentPage = event;
  this.getAllFilterBasedSearchEmployee();
  
}

getVisibleSkills(emp: any) {
  return emp.skillsEmp.slice(emp.skillIndex, emp.skillIndex + this.skillsPerPage);
}

// Next
nextSkills(emp: any) {
  if (emp.skillIndex + this.skillsPerPage < emp.skillsEmp.length) {
    emp.skillIndex += this.skillsPerPage;
  }
}

// Prev
prevSkills(emp: any) {
  if (emp.skillIndex - this.skillsPerPage >= 0) {
    emp.skillIndex -= this.skillsPerPage;
  } else {
    emp.skillIndex = 0;
  }
}

employeeWings = new Map<string, string | null>();

assignWings() {
  this.employeeWings.clear();

  this.employeesSkillMatrix.forEach(emp => {
    const count = emp.skillsEmp.length;

    if (count === this.topSkillCounts[0]) {
      this.employeeWings.set(emp.email, this.wingImages[0]); 
    } else if (count === this.topSkillCounts[1]) {
      this.employeeWings.set(emp.email, this.wingImages[1]); 
    } else if (count === this.topSkillCounts[2]) {
      this.employeeWings.set(emp.email, this.wingImages[2]); 
    } else {
      this.employeeWings.set(emp.email, null);
    }
  });
}


getWingImage(emp: any): string | null {
  return this.employeeWings.get(emp.email) || null;
}



employeesSkillMatrix:searchEmployeeResultSet[]=[];
filteredEmployeesSkillMatrix:any[]=[];
getAllFilterBasedSearchEmployee(){
  this.filtersSkillMatrix.page = this.employeeMatrixPageNo;
  this.filtersSkillMatrix.size = this.employeeMatrixPageSize;
  this.filtersSkillMatrix.export = false;

   this.resourceManagementService.searchEmployeesBySkillsAndCertificates(this.filtersSkillMatrix).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const result = response.serviceResponse;
        this.employeesSkillMatrix = result.content.map((emp: any) => ({
          ...emp,
          skillIndex: 0,
          skillCount: emp.skillsEmp.length || 0
        }));

        this.filteredEmployeesSkillMatrix = this.employeesSkillMatrix;
        this.paginationConfig.totalItems = result.totalElements;
        this.paginationConfig.itemsPerPage = result.pageSize;
       
       this.employeesSkillMatrix.forEach(emp => {
          const exists = this.allEmployeeSkillSummary.some(e => e.email === emp.email);
          if (!exists) {
            this.allEmployeeSkillSummary.push({ email: emp.email, skillCount: emp.skillCount });
          }
        });

        this.updateTopSkillCounts();


        this.assignWings();
      } else {
        console.error(response.serviceResponse);
      }
    });
}


updateTopSkillCounts() {
  const distinctCounts = [...new Set(this.allEmployeeSkillSummary.map(e => e.skillCount))];
  distinctCounts.sort((a, b) => b - a);
  this.topSkillCounts = distinctCounts.slice(0, 3);
}


getAllEmployyesNotInSearch(){
 this.resourceManagementService.searchEnployeesNotInSearch(this.filtersSkillMatrix).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListNotInSearch = response.serviceResponse;
        this.filteredEmployeeListNotInSearch = this.employeeListNotInSearch;
        this.notInListLen =  this.employeeListNotInSearch.length;
          this.employeeListNotInSearch.forEach((emp: any) => {
          const skillNames = emp.skillsEmp
            ?.map((skill: any) => skill.skillName || skill.additionalSkill || '')
            .filter((name: string) => name.trim() !== '')
            .join(', ') || 'NA';

          const certificateNames = emp.certificatesEmp
            ?.map((cert: any) => cert.certificationName)
            .filter((name: string) => name && name.trim() !== '')
            .join(', ') || 'NA';

        
          emp.skillNames = skillNames;
          emp.certificateNames = certificateNames;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });

}

toggleMetricView(view: string): void {
    this.activeMetricView = view

    // if (view === "notInSearch") {
    //   this.getAllEmployyesNotInSearch();
    // }
  }
  








  name = 'skills&Certifications.xlsx';

exportSkillsCertifications() {
  this.filtersSkillMatrix.export = true;
  this.resourceManagementService.searchEmployeesBySkillsAndCertificates(this.filtersSkillMatrix)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const allEmployees = response.serviceResponse.content;

        
        const excelData = allEmployees.map(emp => {
          const skillNames = emp.skillsEmp
              ?.map(skill => skill.skillName || skill.additionalSkill || '') 
              .filter(name => name.trim() !== '')                         
              .join(', ') || '';
          const certificateNames = emp.certificatesEmp?.map(cert => cert.certificationName)?.join(', ') || '';

          return {
            "Employee ID": emp.employeementId,
            "Employee Name": emp.employeeName,
            "Department Name": emp.deptName,
            "Job Role": emp.jobRole,
            "Email": emp.email,
            "Skills": skillNames,
            "Certifications": certificateNames
          };
        });

        
        this.exportExcelService.exportTableDataToExcel(excelData, this.name);
      }
    });
}

exportUnmatchedEmployees() {

 
  const excelData = this.employeeListNotInSearch.map((emp: any) => {
    return {
      "Employee ID": emp.employeementId ,
      "Employee Name": emp.employeeName ,
      "Department Name": emp.deptName ,
      "Job Role": emp.jobRole ,
      "Email": emp.email,
      "Skills": emp.skillNames,
      "Certifications": emp.certificateNames
    };
  });

 
  this.exportExcelService.exportTableDataToExcel(excelData, 'Unmatched_Employees.xlsx');
}



async getProjectAssignedDataByProjectIdfunc(id:number,flagForPOProject,totalRequirements:number){
  try{
  this.loadingRequirements = true;
    console.log("getResourceRequirementByPoProjectId called")
    console.log("type of id",typeof id);
    

  let response:any = await this.resourceManagementService.getProjectAssignedDataByProjectId(id,totalRequirements).pipe(first()).toPromise();
  console.log("the response is",response);
  if (response.serviceStatus == "Success") {
    this.projectRequirementsList.assigned = response.serviceResponse?.assigned;
    this.projectRequirementsList.difference = response.serviceResponse?.difference;
    this.projectRequirementsList.assignedPending = response.serviceResponse?.assignedPending;
    this.projectRequirementsList.assignedApproved = response.serviceResponse?.assignedApproved;
    this.loadingRequirements = false;
  }
  else {
    console.error("Error fetching project requirement list");
    this.loadingRequirements = false;
    if(flagForPOProject){
      this.poResourceRequirementAlert("Error while fetching resource requirement list from PO");
    }else{
      this.poResourceRequirementAlert("Error while fetching Resource requirement");
    }
}
}
catch(error){
  this.poResourceRequirementAlert(error.message);
  this.loadingRequirements = false;
}
}

  getProfileImage(imageBytes: string): SafeResourceUrl {
    if (imageBytes) {
      const objectURL = 'data:image/*;base64,' + imageBytes;
      return this.sanitizer.bypassSecurityTrustResourceUrl(objectURL);
    } else {
      return this.defaultImagePath;
    }
  }

  onImageError(event: any) {
    event.target.src = this.defaultImagePath;
    }

   onSearchSE(searchData){
    this.SEfilters = searchData;
    this.applySearchFilterNotInSearch();
   }

   applySearchFilterNotInSearch() {
  if (!this.SEfilters || Object.keys(this.SEfilters).length === 0) {
    this.filteredEmployeeListNotInSearch = this.employeeListNotInSearch;
    return;
  }

  this.filteredEmployeeListNotInSearch = this.employeeListNotInSearch.filter(emp => {
    return Object.entries(this.SEfilters).every(([key, value]) => {
      if (!value) return true;
      return emp[key]?.toString().toLowerCase().includes(value.toString().toLowerCase());
    });
  });
}

   

    toggleSESearch(){
    this.isSESearchEnabled = !this.isSESearchEnabled;
    if(!this.isSESearchEnabled){
      this.SEfilters = {};
    }
  }

  disableShadowAndDefaultCheckBox(): boolean {
  if(this.selectedRequirement && this.teamMemberCtrl?.value && this.newteamMember?.employeeRole?.length > 0){
    return true;
  }else{
    if(this.newteamMember?.isShadow){
      this.newteamMember.isShadow = 0;
    }
    if(this.newteamMember?.isDefaultProject){
      const event = { target: { checked: false } } as unknown as Event;
      this.onDefaultProjectCheckboxChange(event, this.newteamMember);
    } 
    return false;
  }

  
  }

  filteredEmployeeListForSpoc: any[] = [];

filterSPOC(searchText: string) {
  if (!searchText) {
    this.filteredEmployeeListForSpoc = this.filteredEmployees;
    return;
  }

  const search = searchText.toLowerCase();

  this.filteredEmployeeListForSpoc = this.filteredEmployees.filter(emp =>
    emp.name.toLowerCase().includes(search) ||
    emp.employeementId?.toString().includes(search)
  );
}


}