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
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { finalize, first, map, startWith, catchError } from 'rxjs/operators';
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
import { merge, of, forkJoin } from 'rxjs';
import { MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MilestoneUpdatedLog } from 'src/app/models/MilestoneUpdatedLog'; 
import { RmgProject } from 'src/app/models/rmgProject';
import { RmgStatusCardsComponent } from './rmg-status-cards/rmg-status-cards/rmg-status-cards.component';
import { RmgProjectConfigComponent } from './rmg-project-config/rmg-project-config.component';
import { SubfeatureService } from 'src/app/services/subfeature.service';
import { FeatureUsageLog } from 'src/app/models/featureUsageLog';
import { RmgDashboardComponent } from './new-rmg-dashboard/rmg-dashboard/rmg-dashboard.component';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';

export const PROJECT_COMPLETION_DATE_FORMATS = {
  parse: {
    dateInput: 'DD-MM-YYYY',
  },
  display: {
    dateInput: 'DD-MM-YYYY',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'DD-MM-YYYY',
    monthYearA11yLabel: 'MMMM YYYY',
  },
};

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

@Component({
  standalone: false,
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css'],
  providers: [
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_MOMENT_DATE_ADAPTER_OPTIONS] },
    { provide: MAT_DATE_FORMATS, useValue: PROJECT_COMPLETION_DATE_FORMATS },
  ],
})

export class ResourceManagementComponent implements OnInit {
  
  @ViewChild('rmgDashboardComponent') rmgDashboardComponent!: RmgDashboardComponent;
  @ViewChild('rmgStatusCards') rmgStatusCardsComponent!: RmgStatusCardsComponent;
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

  alertMessageModalRef: NgbModalRef;
  projectConfigurationModalRef: NgbModalRef;
  projectCompletionConfirmationModalRef!: NgbModalRef;
  projectCompletionDatePickerModalRef!: NgbModalRef;
  restoreInfoRef: NgbModalRef;
  restoreProjectModalRef: NgbModalRef;
  createInternalProjectConfirmationModalRef: NgbModalRef;
  projectTimesheetSummaryModalRef: NgbModalRef;
  rejectProjectModalRef: NgbModalRef;
  clientSideIdConfirmationModalRef:NgbModalRef;

  isNewRmgDashboard:boolean = true;
  showProjectConfig: boolean = false;
  rmgProjectObj: RmgProject = new RmgProject();
  projectCompletionObj:Project = new Project();
  isAllProjects: boolean = false;
  markProjectCompletionConfig: boolean = false;

  expandedProjects: Set<string> = new Set();
  projectFullText: Map<string, string> = new Map();
  shouldReload: boolean = false;
  hasClientSideIdFlagHistory: Boolean = false;
  isClientSideIdFormattedHistory: Boolean = false;
  selectedDepartments: string[] = [];
  allDepartmentsGraph: string[] = [];
  showDepartmentFilter: boolean = true;
  orgChartData: any[] = [];
  filterError: string | null = null;
  tableData: any[][] = [];
  maxRows: number = 0;

  selectedDepartmentIds: any[] = [];
  oldSelectedDepartmentIds: any[] = [];


  allStates: any[] = [
    "Maharashtra",
  ];

  nodes: OrgChartNode[] = [];

  // dateType: string = 'po';
  minDate: Date;
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
  employees = ['John Doe', 'Jane Smith'];
  statuses = ['Pending', 'Approved'];
  selectedEmployee = '';
  selectedStatus = '';

  summaryModalRef: NgbModalRef;
  projectSummaryData: any[] = [];

  selectedFile: File | null = null;
  selectedFilePreviewUrl: string | null = null;
  milestoneDocumentUrl: SafeResourceUrl | null = null;
  modalRef: NgbModalRef;
  modalRefRole: NgbModalRef;
  isModalFullscreen = false;

  isTNMCollapsed = false;

  notStartedCount: number = 0;
  pendingForApprovalCount: number = 0;
  approvedCount: number = 0;
  completedInIshineCount: number = 0;
  completedCount: number = 0;
  completedWithEmployeeCount: number = 0;
  currentPoProjectType: string | null = null;

  seempCountDeptSelected: any;
  searchEmployeeResultLen: any;
  notInListLen: any;
  employeeListNotInSearch: any[] = [];
  activeMetricView = "search";
  notInSearchPageNo = 1;
  filteredEmployeeListNotInSearch: any[] = [];
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
  isClientSideIdFormatted: Boolean = false;
  employeeRoleList: any = []
  projectNameForMilestoneUpdate: string;
  poNameForMilestoneUpdate: string;

  toggleExpand(): void {
    this.expandedIndex = !this.expandedIndex;
  }


  // new cards changes.....................................................................
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild("alertTemplateRole")
  alertTemplateRole: TemplateRef<any>;


  @ViewChild('alertTemplate') alertTemplateForMilestone!: TemplateRef<any>;
  @ViewChild('confirmMilestoneStatusModal') confirmMilestoneStatusModal!: TemplateRef<any>;

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

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
  updateProjectCompletionModalRef: NgbModalRef;
  modalRefWithReloadMilestone: NgbModalRef;


  @ViewChild('MarkAsCompleteDefaultProject') MarkAsCompleteDefaultProject1!: TemplateRef<any>;
  @ViewChild('OtherProjectDefaultMapping') OtherProjectDefaultMapping1!: TemplateRef<any>;
  @ViewChild('customDatePickerTemplate') customDatePickerTemplate1!: TemplateRef<any>;
  @ViewChild('confirmCompleteTemplate') confirmCompleteTemplate!: TemplateRef<any>;
  confirmCompleteTemplateModalRef!: NgbModalRef;
  @ViewChild('alert_message_without_reload') alert_message_without_reloadTemplate!: TemplateRef<any>;
  alert_message_without_reloadModalRef!: NgbModalRef;
  @ViewChild('resource_removal_alert') removeResourceModal: TemplateRef<any>;
  removeResourceModalRef: NgbModalRef;
  projectMilestoneDocumentModalRef: NgbModalRef;
  updateProjectMilestoneModalRef:NgbModalRef;
   projectLineItemListModalRef:NgbModalRef;
  @ViewChild("project_milestone_document") projectMilestoneDocumentTemplateRef: TemplateRef<any>;


  data: string;
  currentUser: User;
  feature = "Resource Management";
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  alertMessage: any;

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
  isClientSideIdTnmProjectType: boolean = false;
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
  projectRejectionReason:string = '';
  currentProjectId: any;
  selectedProjectManager: any;
  excelName: any;

  allProjectList: any[] = [];
  allDeptList: any[] = [];
  filteredDeptList: any[] = [];
  teamLeadsList: any[] = [];
  employeeListByDept: Employee[] = [];
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

  //milestone
  milestoneExtendReason:any
  isOtherReasonSelected: boolean = false;
  file:any
  isExtensionEnabled:boolean =false
  showMilestoneImageModal:boolean =false
  minExtendDate!: Date;
  expandedMilestoneId:any
  selectedLogs: any[] = [];
  selectedLogType: 'start' | 'end' | 'status' | null = null;
  logHeader:any;
  @ViewChild("project_milestone_extended_preview") projectMilestoneExtendedPreviewTemplateRef: TemplateRef<any>;
  isImageFile: boolean = false;
  isPdfFile: boolean = false;
  isStatusChanged: boolean = false;
  originalStatus:any
  confirmMilestoneStatus:any;

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  employeeNotInSearchColumns: any[] = ['employeementId', 'employeeName', 'deptName', 'jobRole', 'email', 'skillNames', 'certificateNames'];
  filters: any = {};
  SEfilters: any = {};
  isSearchEnabled: boolean = false;
  isSESearchEnabled: boolean = false;

  projectDetails: any = [];
  projectDetails2: any = [];
  copyDepartment: any = [];
  currentBreadcrumbList: any[] = [];
  poProjectListFromIshine: any[] = [];
  flagDialogueBox: boolean = false;
  tableName: string;
  isAccounts: boolean = false;
  isAllSelected: boolean = false;
  tabCounts: any;
  searchQuery: any;
  selectedEmpId: any = 0;
  deptIdList: any[] = [];
  employeeList: Employee[] = [];
  filteredEmployees: Employee[] = [];
  searchText: any;
  searchTextDept: any;
  selectedStatusTab: string = '';


  selectedDate: any | null = null;
  completedProjectDetails: Project = new Project();
  projectFilterDTO: ProjectFilterDTO = new ProjectFilterDTO();
  deptList: ProjectFilterDTO = new ProjectFilterDTO();
  deptListUser: ProjectFilterDTO = new ProjectFilterDTO();
  departments: any[] = [];
  totalCount: any;
  projectRequirementsList: ProjectRequirements = new ProjectRequirements();
  selectedOtherProjectId: any;
  showSearchInput = true;
  teamMembers: TeamMember[] = [];
  newMember: TeamMember = new TeamMember;
  addMemberCtrl = new FormControl();
  searchManagerText: any;
  filteredManagerList: any[] = [];
  isAllManagersSelected: boolean = false;
  selectedRequirement: any = null;
  teamMemberCtrl = new FormControl();
  spocCtrl = new FormControl();
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
  filterData: any = new FilterData();

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
  filteredTimesheetReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'departmentName', 'date', 'dayType', 'projectName', 'teamName', 'clientName', 'clientLocation', 'activity', 'description', 'managerName', 'status', 'totalTime', 'officeInTime', 'officeOutTime', 'leaveType', 'createdByName', 'createdOn'];

  queryList: any[] = [];
  // filterData: any = new FilterData();

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

  employeeReportColumnForDetailedProjectViewClub: any[] = ['blank', 'employeementId', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'projectStartDate', 'projectEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience', 'primaryProjectName'];
  employeeReportColumnForDetailedProjectView: any[] = ['blank', 'employeementId', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'projectStartDate', 'projectEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience'];
  leaveReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'noOfDays', 'reason', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'leaveStatusUpdatedByName'];
  timesheetReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'totalWorkingHours', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'leaveType', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  employeeReportColumn: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'projectName', 'poNo', 'projectStartDate', 'projectEndDate', 'poProjectType', 'clientName', 'billable', 'billableType', 'updatedOn', 'updatedByName', 'createdByName', 'createdOn'];
  leaveTimesheetReportColumn: any[] = ['employeementId', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  defaultMappingColumns: any[] = ['tabName', 'featureName', 'subFeatureName'];
  employeeReportColumnForDetailedProjecttttView: any[] = ['blank', 'projectName', 'projectManager', 'apmosysRM', 'clientRM', 'projectStartDate', 'projectEndDate', 'poNo', 'poProjectType', 'teamName', 'employeeName', 'jobRole', 'deptName', 'mobileNo', 'email', 'billable', 'billableType', 'effectiveStartDate'];

  employeesFor360: any[] = [];
  // departments: any[] = [];
  allEmployee: any[] = [];
  //filteredEmployees: any[] = [];
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
  //isAccounts: boolean = false;
  isDeptFilter: boolean = false;
  dept: any;
  isAdminOrHod = true;

  //added

  setDefaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
  projectSummary: {};
  projectList: any[];
  employeeBenchRepot: any[] = [];
  employeeBench: any;
  employeeBenchRepotMsg: any;
  deptId: any;
  teamLeadCtrl = new FormControl();
  filteredTeamLeads: Employee[] = [];
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
  fallBackMsg: any;
  isApproved: boolean = false;
  advanceFilter: any;
  isLoadingMilestones: any = false;
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

  selectedMembers: any[] = [];
  employeeSelectionHistory: any[] = [];

  isSkillMatrix = false;
  statusTab: any;

  clientSideIdObj: updateHasClientSideId = new updateHasClientSideId();
  hasClientSideIdFlag: Boolean = false;
  fetchClientSideIdObj: updateHasClientSideId = new updateHasClientSideId();
  restoreProjectPayload = new RestoreProjectPayload();
  isRestoreSuccess: Boolean = false;
  isCompletionSuccess: Boolean = false;
  defaultImagePath = 'assets/Images/default-user-image.jpeg';

  @ViewChild('teamMemberAuto', { read: MatAutocompleteTrigger }) teamMemberAutoTrigger!: MatAutocompleteTrigger;

  isReportTabToggleViewVisible :boolean = false;

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
    private loaderService: LoaderService,
    private subFeatureService : SubfeatureService
  ) {
    this.minDate = new Date(); 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const reportUrl = `${environment.baseUrl360}#/user-reports/report-list`;
    this.reportListUrlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(reportUrl);
  }

  async ngOnInit(): Promise<void> {
    this.isNewRmgDashboard = this.filterStateService.isNewRmgDashboard;
    if (this.isNewRmgDashboard) {
      this.mapSubFeatureFlag();
      this.showViewProjects(true);
      return;
    }

    this.myDept = true;
    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();
    this.isAccounts = deptName === 'Accounts';
    this.setReportToggleViewVisible();
    this.mapSubFeatureFlag();

    this.route.params.subscribe((params: Params) => {
      this.currentProjectId = params['id'];
    });

    if (this.shouldFetchUserSpecificDepartments(deptName, empRole)) {
      await this.getDeptsByUser();
    } else {
      await this.getAllDepartmentsByCurrentUserIdAndRole();
    }

    if (this.currentUser.employeeRole !== 'SuperAdmin') {
      this.selectAllFilteredDepartments();
    }

    this.sectionViewInit();
    this.getEmployeeByNameAndEmpld();
    // this.loadRMGDashboard(this.rmgDashboardProjectRequest);
    // this.restoreFilterState();
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
    this.selectedDepartmentIds = [];
    this.oldSelectedDepartmentIds = [];
    this.filteredDepartments = [];
    try {
      const response: any = await this.departmentService.getDeptsByRole(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch departments");
      }
      const serviceResponse = response.serviceResponse;
      this.filteredDepartments = serviceResponse?.departments || [];
      return;
    } catch (error) {
      console.error("Error fetching departments:", error);
      throw error;
    }
  }

  async getDeptsByUser(): Promise<any> {
    this.selectedDepartmentIds = [];
    this.oldSelectedDepartmentIds = [];
    this.filteredDepartments = [];
    try {
      const response: any = await this.departmentService.getDeptsByUser(this.currentUser.empId).pipe(first()).toPromise();
      if (response?.serviceStatus !== "Success") {
        throw new Error("Failed to fetch user departments");
      }
      const serviceResponse = response.serviceResponse;
      this.filteredDepartments = serviceResponse?.departments || [];
      return;
    } catch (error) {
      console.error("Error fetching user departments:", error);
      throw error;
    }
  }

  hasOrgWideHierarchyAccess(): boolean {
    const r = (this.currentUser?.employeeRole ?? '').toString().trim();
    if (!r) {
      return false;
    }
    if (/^superadmin$/i.test(r) || /^hod$/i.test(r) || /^hr$/i.test(r)) {
      return true;
    }
    if (/^hr\s*manager$/i.test(r)) {
      return true;
    }
    return false;
  }

  sectionViewInit(): void {
    const role = this.currentUser.employeeRole?.trim() || '';
    this.isHOD = role === 'HOD' || role === 'SuperAdmin';
    if (this.userMapping?.view_all_rmg_projects || (this.currentProjectId != undefined && this.currentProjectId != null)) {
      this.showViewProjects();
    }
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

  showViewProjects(isRouteFromEmployee360: boolean = false) {
    this.isProjectTable = true;
    this.allProjectTable = true;
    this.isSkillMatrix = false;
    this.isHideButton = false;
    this.isEditProject = false;
    this.isCreateForm = false;
    this.isCreation = false;
    this.showProjectConfig = false;
    this.markProjectCompletionConfig = false;
    this.filters = {};
    this.isSearchEnabled = false;
    this.allProjectList = [];
    this.teamCreatedProjectList = [];
    this.isNewRmgDashboard = this.filterStateService.isNewRmgDashboard;
    if (this.rmgStatusCardsComponent) {
      this.rmgStatusCardsComponent.onDepartmentSelectionChange(this.selectedDepartmentIds);
    }

    if (!isRouteFromEmployee360) {
      setTimeout(() => {
        if (this.rmgDashboardComponent) {
          this.rmgDashboardComponent?.ngOnInit();
        }
      }, 1);
    }
  }

  showEditProjectForm(project: any) {
    this.isEditProject = true;
    this.allProjectTable = true;
    this.isHideButton = true;
    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    // this.getTeamListByProjectName(project);
    this.getManagerList();
  }

  onDepartmentSelectionChange(event: any) {
    if (this.areArraysEqual(this.oldSelectedDepartmentIds, this.selectedDepartmentIds)) {
      return;
    }
    this.oldSelectedDepartmentIds = [...this.selectedDepartmentIds];
    if (this.rmgStatusCardsComponent) {
      this.rmgStatusCardsComponent.onDepartmentSelectionChange(event);
    }
  }

  async toggleDepartment() {
    this.filterStateService.myDept = this.myDept;
    if (this.myDept) {
      await this.getDeptsByUser();
    } else {
      await this.getAllDepartmentsByCurrentUserIdAndRole();
    }

    this.selectAllFilteredDepartments();
    if (this.rmgStatusCardsComponent) {
      this.rmgStatusCardsComponent.onDepartmentSelectionChange(this.selectedDepartmentIds);
    }
  }

  private selectAllFilteredDepartments(): void {
    this.selectedDepartmentIds = this.filteredDepartments?.map((dept) => dept.deptId) ?? [];
    this.oldSelectedDepartmentIds = [...this.selectedDepartmentIds];
    this.filterStateService.deptIdList = this.selectedDepartmentIds;
    this.filterStateService.myDept = this.myDept;
  }

  get selectedDepartmentsTooltip(): string {
    const ids = this.selectedDepartmentIds ?? [];
    if (ids.length === 0) {
      return 'No departments selected';
    }
    const names = (this.filteredDepartments ?? [])
      .filter((dept) => ids.includes(dept.deptId))
      .map((dept) => dept.name);
    return names.length > 0 ? names.join(', ') : 'No departments selected';
  }

  getAllClientList() {
    this.allClientList = [];
    this.projectService.getAllClients().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allClientList = response.serviceResponse;
        this.filteredClientList = this.allClientList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.clientId === value.clientId
          ))
        )
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

  filterDepartmentsInternal() {
    const lowerText = (this.searchTextDept || '').toLowerCase();
  
    this.filteredDepartmentsInternal = this.allDeptList.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  async getManagerList() {
    this.managerList = [];
    this.filteredManagerList = [];
    let reqObj = { ...this.employeeObj };
    reqObj.role = "Manager";
    if (reqObj.employeementId) {
      reqObj.employeementId = this.utilityService.stripEmploymentIdPrefix(reqObj.employeementId);
    }
    this.employeeObj.role = "Manager";
    this.employeeObj.employeementId = this.utilityService.stripEmploymentIdPrefix(this.employeeObj.employeementId);
    try {
      const response: any = await this.employeeService.getAllEmployeesByRole(reqObj).pipe(first()).toPromise();
      if (response.serviceStatus === "Success") {
        this.managerList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.overheadList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.managerList.forEach((emp) => this.utilityService.applyEmployeeDisplayFields(emp));
        this.overheadList.forEach((emp) => this.utilityService.applyEmployeeDisplayFields(emp));
        this.filteredManagerList = [...this.managerList];
        this.filteredOverheadList = [...this.overheadList];
      } else {
        console.error(response.serviceResponse);
      }
    } catch (error) {
      this.openAlertMessageModal("Error fetching manager list: " + error);
    }
  }

  getAllDepartments(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getDeptsByRole(this.currentUser.empId).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.deptList = response.serviceResponse;
            this.departments = this.deptList.departments;
            this.deptIdList = this.departments;
            this.departmentsList = [...(this.departments || [])];
            this.filteredDepartments = [...(this.departments || [])];
            this.filteredDepartmentsTeam = [...this.departmentsList];
            this.projectFilterDTO = this.deptList;
            this.projectFilterDTO.departments = [];
            this.projectFilterDTO.departmentsids = [];
            // this.onDepartmentToggle(this.projectFilterDTO);
            resolve(response.serviceResponse);
          } else {
            reject("Failed to fetch departments");
          }
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }

  getAllDepartmentsFromId(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getAllDepartmentsFromId(this.currentUser.empId).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.departments = response.serviceResponse;
            this.filteredDepartments = this.departments;
            resolve(response.serviceResponse);
          } else {
            reject("Failed to fetch departments");
          }
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }

  getAllDepartmentList(project: any) {
    this.allDeptList = [];
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
        this.filteredDepartments = [...this.allDeptList];
      } else {
        console.error(response.serviceResponse);
      }
    });
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

  getEmployeeByNameAndEmpld() {
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.filteredEmployees = this.employeeList;
        this.teamLeadsList = this.employeeList;
        this.filteredTeamLeads = this.teamLeadsList;
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }

  trackByEmpId(index: number, member: any): number {
    return member.empId;
  }

  toggleReportView() {
    this.showReportList = !this.showReportList;
  }

  setReportToggleViewVisible() {
    this.isReportTabToggleViewVisible = this.currentUser?.userMapping?.some(m => m.featureName === 'Reports') ?? false;
  }

  goToReportList() {
    this.router.navigate(['/user-reports/report-list'], {
      state: { returnUrl: this.router.url }
    });
  }

// Skill Matrix Start

  skillsPerPage = 3;
  rankedEmployees: any[] = [];
  certificationList: any[] = [];
  skillList: any[] = [];
  departmentList: any[] = [];
  filtersSkillMatrix: FilterMatrix = new FilterMatrix();
  searchTextSkill = '';
  filteredSkills: any[] = [];
  isAllSkillsSelected = false;
  filterMatrixObj: FilterMatrix = new FilterMatrix();
  isAllCertificatesSelected = false;
  searchTextCertificate: any;
  filteredCertificates: any[] = [];
  searchTextCertDept = '';
  filteredCertificationDepartments: any[] = [];
  isAllCertificationDepartmentsSelected = false;
  searchTextDeptMatrix = '';
  filteredDepartmentsMatrix: any[] = [];
  isAllDepartmentsSelected = false;
  apiResponsesCount: any;
  employeeWings = new Map<string, string | null>();
  employeesSkillMatrix: searchEmployeeResultSet[] = [];
  filteredEmployeesSkillMatrix: any[] = [];
  name = 'skills&Certifications.xlsx';

  wingImages = [
    "assets/Images/goldenwings.gif",
    "assets/Images/silverwings.gif",
    "assets/Images/bronzewings.gif"
  ];


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
      ? this.filteredSkills.map(s => s.skillId) : [];

    this.filtersSkillMatrix.skillNames = this.isAllSkillsSelected
      ? this.filteredSkills.map(s => s.skillName) : [];
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

  getCountOfEmployeeFromSelectedDepartment() {
    this.resourceManagementService.getEmployeeCountSDeptwise(this.filtersSkillMatrix).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.seempCountDeptSelected = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllCertificatesRbac() {
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

  filterCertificates() {
    const lower = this.searchTextCertificate.toLowerCase();
    const selectedIds = this.filtersSkillMatrix.certificateIds || [];

    this.filteredCertificates = this.certificationList.filter(cert =>
      cert.certificateName.toLowerCase().includes(lower) ||
      selectedIds.includes(cert.employeeCertificateId)
    );
  }

  resetCertficateSearch() {
    this.searchTextCertificate = '';
    this.filteredCertificates = [...this.certificationList];
  }

  clearCertificates(event: Event) {
    this.filtersSkillMatrix.certificateIds = [];
    this.isAllCertificatesSelected = false;
    this.searchTextCertificate = '';
    this.filteredCertificates = [...this.certificationList];
  }

  toggleSelectAllCertificates() {
    this.isAllCertificatesSelected = !this.isAllCertificatesSelected;
    this.filtersSkillMatrix.certificateIds = this.isAllCertificatesSelected
      ? this.filteredCertificates.map(c => c.employeeCertificateId) : [];
  }

  filterDepartmentsMatrix() {
    const lower = this.searchTextDeptMatrix.toLowerCase();
    const selectedIds = this.filtersSkillMatrix.deptIds || [];
    this.filteredDepartmentsMatrix = this.departmentList.filter(dept =>
      dept.name.toLowerCase().includes(lower) || selectedIds.includes(dept.deptId)
    );
  }

  clearDepartments(event: Event) {
    event.stopPropagation();
    this.filtersSkillMatrix.deptIds = [];
    this.isAllDepartmentsSelected = false;
    this.searchTextDeptMatrix = '';
    this.filteredDepartmentsMatrix = [...this.departmentList];
  }

  resetDeptSearch() {
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

  getAllDepartmentsRbac() {
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

  openShowSkillMatrix() {
    this.isSkillMatrix = true;
    this.isProjectTable = false;
    this.allProjectTable = false;
    this.isCreateForm = false;
    this.isCreation = false;
    this.apiResponsesCount = 0;
    this.getAllCertificatesRbac();
    this.getAllPredefinedSkills();
    this.getAllDepartmentsRbac();
  }

  applySearchFilter() {
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
    this.filtersSkillMatrix.certificateStatus = 'All';
    this.filtersSkillMatrix.specialization = '';
    this.isAllCertificatesSelected = false;
    this.isAllDepartmentsSelected = true;
    this.isAllCertificationDepartmentsSelected = false;
    this.isAllSkillsSelected = true;
    this.applySearchFilter();
  }

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

  getAllFilterBasedSearchEmployee() {
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

  getAllEmployyesNotInSearch() {
    this.resourceManagementService.searchEnployeesNotInSearch(this.filtersSkillMatrix).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListNotInSearch = response.serviceResponse;
        this.filteredEmployeeListNotInSearch = this.employeeListNotInSearch;
        this.notInListLen = this.employeeListNotInSearch.length;
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
  }

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
        "Employee ID": emp.employeementId,
        "Employee Name": emp.employeeName,
        "Department Name": emp.deptName,
        "Job Role": emp.jobRole,
        "Email": emp.email,
        "Skills": emp.skillNames,
        "Certifications": emp.certificateNames
      };
    });
    this.exportExcelService.exportTableDataToExcel(excelData, 'Unmatched_Employees.xlsx');
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

  onSearchSE(searchData) {
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

  toggleSESearch() {
    this.isSESearchEnabled = !this.isSESearchEnabled;
    if (!this.isSESearchEnabled) {
      this.SEfilters = {};
    }
  }

  // Helpers Start
  isValidString(string: any) {
    return this.validationService.validateNullUndefinedEmptyStringTrim(string);
  }

  openAlertMessageModal(alertMessage: any) {
    this.alertMessage = alertMessage;
    this.alertMessageModalRef = this.modalService.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();
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

  isValidNumber(value: any): boolean {
		return typeof value === 'number' && !Number.isNaN(value);
	}

  get isValidEmployee(): boolean {
    if (!this.selectedEmpId || !this.isValidNumber(this.selectedEmpId) || !this.filteredEmployees?.length) {
      return false;
    }

    return this.filteredEmployees.some(
      emp => emp.empId === this.selectedEmpId
    );
  }

  private areArraysEqual(arr1: number[], arr2: number[]): boolean {
    if (!arr1 || !arr2) {
      return false;
    }
    if (arr1?.length !== arr2?.length) {
      return false;
    }
    for (let i = 0; i < arr1?.length; i++) {
      if (arr1[i] !== arr2[i]) {
        return false;
      }
    }
    return true;
  }

  setNewRmgDashboardFlag() {
    this.filterStateService.isNewRmgDashboard =  this.isNewRmgDashboard;
    this.ngOnInit();
    let featureUsageLog = new FeatureUsageLog();
    featureUsageLog.empId = this.currentUser.empId;
    featureUsageLog.featureName = 'NEW RMG DASHBOARD';
    featureUsageLog.logMessage = this.isNewRmgDashboard ? 'SWITCHED FROM OLD RMG DASHBOARD TO NEW RMG DASHBOARD' : 'SWITCHED FROM NEW RMG DASHBOARD TO OLD RMG DASHBOARD';
    this.subFeatureService.saveFeatureUsageLog(featureUsageLog).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }
  // Helpers End

  // Project Timesheet Summary Modal Start
  openProjectTimesheetSummaryModal(selectedEmpId: any) {
    if (!selectedEmpId || selectedEmpId == undefined || selectedEmpId == null || !this.isValidNumber(selectedEmpId)) {
      this.openAlertMessageModal("Kindly Select a Valid Employee!!");
      return;
    }
    if (!this.filteredEmployees.some(emp => emp.empId === selectedEmpId)) {
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

  // Internal Project Start
  filterDepartments(deptObj?: any) {
    if (deptObj) { this.departments = deptObj }
    const lowerText = this.searchTextDept.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  showCreateInternalProjectForm() {
    this.isCreateForm = true;
    this.isCreation = true;
    this.isSkillMatrix = false;
    this.isProjectTable = false;
    this.allProjectTable = false;
    this.isUpdateForm = false;
    this.projectObj = new Project();
    this.getAllDepartmentListForCreateProject();
    this.getManagerList();
    this.getAllClientList();
    this.projectObj.clientId = '';
  }

  openCreateInternalProjectConfirmationModal() {
    this.createInternalProjectConfirmationModalRef = this.modalService.open(this.createInternalProjectConfirmationTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeCreateInternalProjectConfirmationModal() {
    if (this.createInternalProjectConfirmationModalRef) {
      this.createInternalProjectConfirmationModalRef?.close();
    }
  }

  checkProjectName(template: TemplateRef<any>) {
    let projectObj = new Project();
    projectObj.projectName = this.projectObj.projectName?.trim();

    if (projectObj.projectName.length >= 5) {
      if (!this.validationService.validateProjectName(projectObj.projectName)) {
        this.projectObj.projectName = '';
        this.openAlertMessageModal("Please enter valid Project Name !!");
        return false;
      }
    } else {
      this.projectObj.projectName = '';
      this.openAlertMessageModal("Please enter more than 4 letters in Project Name !!");
      return false;
    }
    this.projectService.checkProjectName(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.projectObj.projectName = '';
        this.openAlertMessageModal(response.serviceResponse);
      }
    })
  }

  validateCreateProjectObj(projectObj: Project, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectName)) {
      this.openAlertMessageModal("Please enter Project name !!");
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.departmentList)) {
      this.openAlertMessageModal( "Please select department !!");
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.state)) {
      this.openAlertMessageModal("Please select state !!");
      return false;
    }
    return true;
  }

  createProject(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateCreateProjectObj(this.projectObj, template)
    if (!inputValidated) {
      return;
    }
    this.projectObj.projectName = this.projectObj.projectName?.trim();
    this.projectObj.createdBy = this.currentUser.empId;
    this.projectService.createProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.showViewProjects();
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }
  // Internal Project End

  // Project Completion Start
  initiateProjectCompletion(project: any) {
    this.selectedDate = null;
    this.projectCompletionObj = project;
    this.markProjectCompletionConfig = true;
    this.isAllProjects = this.isValidString(this.projectFilterDTO.approvalStatus) && this.projectFilterDTO.approvalStatus?.toLowerCase() === 'all';
    forkJoin({
      managers: this.getManagerAndOverheadList(),
      departments: this.getAllDepartmentsList(),
      employees: this.getEmployeeNameAndEmpld(),
      projectConfig: this.getProjectConfigurationDetailsByProjectId(project)
    }).subscribe(result => {
      if (this.rmgProjectObj) {
        this.rmgProjectComponent.validateProjectForCompletion(project.projectId);
      } else {
          this.openAlertMessageModal("Unable to mark the project as complete at the moment. Please contact the administrator!!");
      }
    });
  }

  openProjectCompletionDatePickerModal() {
    this.projectCompletionDatePickerModalRef = this.modalService.open(this.projectCompletionDatePickerTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeProjectCompletionDatePickerModal() {
    if (this.projectCompletionDatePickerModalRef) {
      this.projectCompletionDatePickerModalRef?.close();
    }
  }

  openProjectCompletionConfirmationModal() {
    this.projectCompletionConfirmationModalRef = this.modalService.open(this.projectCompletionConfirmationTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeProjectCompletionConfirmationModal() {
    if (this.projectCompletionConfirmationModalRef) {
      this.projectCompletionConfirmationModalRef?.close();
    }
  }

  confirmMarkProjectAsComplete() {
    if (!this.selectedDate || this.selectedDate == undefined || this.selectedDate == null) {
      this.openAlertMessageModal('Kindly provide Project completion Date!!');
      return;
    }

    let projectObj: Project = new Project();
    projectObj.projectStatus = "Completed";
    projectObj.projectType = this.projectCompletionObj.projectType;
    projectObj.projectId = this.projectCompletionObj.projectId;
    projectObj.projectCompletionDate = moment(this.selectedDate).format('YYYY-MM-DD');
    projectObj.updatedBy = this.currentUser.empId;
    this.resourceManagementService.completionDateOfProject(projectObj).pipe(first()).subscribe(
      (response: any) => {
        if (response.serviceStatus === "Success") {
          this.closeProjectCompletionDatePickerModal();
          this.openAlertMessageModal(response.serviceResponse);
          if (this.rmgStatusCardsComponent) {
            this.rmgStatusCardsComponent.onDepartmentSelectionChange(this.selectedDepartmentIds);
          }
          if (this.rmgDashboardComponent) {
            this.rmgDashboardComponent?.ngOnInit();
          }
        } else {
          this.openAlertMessageModal(response.serviceResponse);
        }
      },
      (error) => {
        this.openAlertMessageModal("Something went wrong while marking the Project as complete.");
      }
    );
  }

  submitProjectCompletionDate() {
    this.completedProjectDetails.projectStatus = 'Completed';
    this.completedProjectDetails.projectCompletionDate = moment(this.selectedDate).format('YYYY-MM-DD');
    this.completedProjectDetails.updatedBy = this.currentUser.empId;

    if (!this.completedProjectDetails.projectCompletionDate) {
      this.openAlertMessageModal("Please Select Completion Date!!");
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
          this.isCompletionSuccess = true;
          this.openProjectCompletionDatePickerModal();
          this.openAlertMessageModal(response.serviceResponse);
        } else {
          this.selectedDate = '';
          this.isCompletionSuccess = false;
          this.openAlertMessageModal(response.serviceResponse);
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
        } else {
        }
      });
    } catch (error: unknown) {
      console.error('Unexpected error in try/catch:', error);
    }
  }
  // Project Completion End

  // Restore Project Start 
  openRestoreProjectModal(projectId: any) {
    this.selectedProjectId = projectId;
    this.restoreProjectModalRef = this.modalService.open(this.restoreProjectTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeRestoreProjectModal() {
    if (this.restoreProjectModalRef) {
      this.restoreProjectModalRef?.close();
    }
  }

  restorePreviousStateOfProject(projectId: any) {
    this.closeRestoreProjectModal();
    this.restoreProjectPayload.projectId = projectId;
    this.restoreProjectPayload.currentUserEmpId = this.currentUser.empId;
    this.resourceManagementService.restorePreviousStateOfProject(this.restoreProjectPayload).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isRestoreSuccess = true;
        this.openAlertMessageModal(response.serviceResponse);
      } else {
        this.isRestoreSuccess = false;
        this.openAlertMessageModal(response.serviceResponse);
      }
    }, (error: any) => {
      this.loaderService.requestEnded();
      this.isRestoreSuccess = false;
      this.openAlertMessageModal(error.message);
    });
  }
  // Restore Project End

  // Approve & Reject Project Start
  onApproveProject(project: any) {
    project.empId = this.currentUser.empId;
    if (project.poProjectType != null) {
      project.projectType = project.poProjectType;
      project.isDraftProject = "false";
    } else {
      project.projectType = "Internal";
    }

    this.resourceManagementService.approvePendingProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.showViewProjects();
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong');
      }
      console.log("APPROVE BUTTON :   ",response);
    });
  }

  onRejectProject() {
    let projObj = this.selectedProjToReject;
    projObj.empId = this.currentUser.empId;
    projObj.rejectReason = this.projectRejectionReason;
    // if (this.selectedProjToReject.poProjectType != null) {
    //   project.projectType = this.selectedProjToReject.poProjectType;
    // } else {
    //   project.projectType = "Internal";
    // }

    this.resourceManagementService.rejectPendingProject(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.showViewProjects();
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong');
      }
    });
  }

  openRejectProjectModal(projectObj: any) {
    this.projectRejectionReason = null;
    this.selectedProjToReject = projectObj;
    this.rejectProjectModalRef = this.modalService.open(this.rejectProjectTemplateRef, { modalDialogClass: 'modal-lg' });
  }

  closeRejectProjectModal() {
    if (this.rejectProjectModalRef) {
      this.rejectProjectModalRef?.close();
    }
  }
  // Approve & Reject Project End

  // Client Side Id Mapping Start
  openClientSideIdConfirmationModal(projectId: any) {
    this.clientSideIdObj.projectId = projectId;
    this.fetchHasClientSideId(projectId);
    this.clientSideIdConfirmationModalRef = this.modalService.open(this.clientSideIdConfirmationTemplateRef, { modalDialogClass: 'modal-md' });
  }

  closeClientSideIdConfirmationModal(): void {
    if (this.clientSideIdConfirmationModalRef) {
      this.clientSideIdConfirmationModalRef?.close();
    }
  }

  updateHasClientSideId(flag: Boolean) {
    this.clientSideIdObj.hasClientSideId = flag;
    this.clientSideIdObj.currentUserEmpId = this.currentUser.empId;
    this.clientSideIdObj.clientFlag = this.isClientSideIdFormatted;
    this.closeClientSideIdConfirmationModal();
    this.resourceManagementService.updateHasClientSideId(this.clientSideIdObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.hasClientSideIdFlag = false;
        this.isClientSideIdFormatted = false;
        this.hasClientSideIdFlagHistory = false;
        this.isClientSideIdFormattedHistory = false;
      } else {
        this.openAlertMessageModal(response.serviceResponse)
      }
    });
    this.hasClientSideIdFlag = false;
    this.isClientSideIdFormatted = false;
    this.hasClientSideIdFlagHistory = false;
    this.isClientSideIdFormattedHistory = false;
  }

  getValidateResponse() {
    if (this.hasClientSideIdFlag == this.hasClientSideIdFlagHistory && this.isClientSideIdFormatted == this.isClientSideIdFormattedHistory) {
      return true;
    }
    return false;
  }

  onMandatoryChange() {
    if (!this.hasClientSideIdFlag) {
      this.isClientSideIdFormatted = false;
    }
  }

  fetchHasClientSideId(projectId: any) {
    this.clientSideIdObj.projectId = projectId;
    this.resourceManagementService.fetchHasClientSideId(this.clientSideIdObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.fetchClientSideIdObj = response.serviceResponse;
        this.hasClientSideIdFlag = !!this.fetchClientSideIdObj.hasClientSideId;
        this.isClientSideIdFormatted = !!this.fetchClientSideIdObj.clientFlag;
        this.hasClientSideIdFlagHistory = !!this.fetchClientSideIdObj.hasClientSideId;
        this.isClientSideIdFormattedHistory = !!this.fetchClientSideIdObj.clientFlag;
      } else {
        this.openAlertMessageModal(response.serviceResponse)
      }
    });
  }
  // Client Side Id Mapping End

  // Project Configuration Start
  showProjectConfigurationDetails(project: any, isModal: boolean) {
    this.isAllProjects = this.isValidString(this.projectFilterDTO.approvalStatus) && this.projectFilterDTO.approvalStatus?.toLowerCase() === 'all';
    forkJoin({
      managers: this.getManagerAndOverheadList(),
      departments: this.getAllDepartmentsList(),
      employees: this.getEmployeeNameAndEmpld(),
      projectConfig: this.getProjectConfigurationDetailsByProjectId(project)
    }).subscribe(result => {
      if (this.rmgProjectObj) {
        if (isModal) {
          this.openProjectConfigurationModal();
        } else {
          this.showProjectConfiguration();
        }
      }
    });
  }

  showProjectConfiguration() {
    this.showProjectConfig = true;
    this.markProjectCompletionConfig = false;
    this.isSkillMatrix = false;
    this.isProjectTable = false;
    this.allProjectTable = false;
    this.isCreateForm = false;
    this.isCreation = false;
    this.filterStateService.isNewRmgDashboard = this.isNewRmgDashboard;
    this.isNewRmgDashboard = false;
  }

  closeProjectConfiguration() {
    this.showViewProjects();
  }

  getManagerAndOverheadList() {
    this.managerList = [];
    this.filteredManagerList = [];
    this.overheadList = [];
    this.filteredOverheadList = [];

    const reqObj = {
      ...this.employeeObj,
      role: 'Manager',
      employeementId: this.utilityService.stripEmploymentIdPrefix(this.employeeObj?.employeementId)
    };

    return this.employeeService.getAllEmployeesByRole(reqObj).pipe(
      first(),
      map((response: any) => {
        if (response.serviceStatus === 'Success') {
          const empList = [...response.serviceResponse];
          this.appendPrefixToEmployee(empList);

          this.managerList = [...empList];
          this.overheadList = [...empList];
          this.filteredManagerList = [...empList];
          this.filteredOverheadList = [...empList];
        }
        return true;
      }),
      catchError(error => {
        console.error('Manager list failed', error);
        return of(false);
      })
    );
  }

  appendPrefixToEmployee(employeeList: any) {
    if (!this.validationService.validateNullUndefinedEmptyList(employeeList)) {
      return;
    }
    employeeList.forEach((emp) => {
      this.utilityService.applyEmployeeDisplayFields(emp);
      emp.employeementId = emp.employmentIdAcToET ?? emp.employeementId;
    });
  }

  getAllDepartmentsList() {
    this.allDeptList = [];

    return this.departmentService.getAllDepartments().pipe(
      first(),
      map((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.allDeptList = response.serviceResponse;
        }
        return true;
      }),
      catchError(error => {
        console.error('Department list failed', error);
        return of(false);
      })
    );
  }

  getEmployeeNameAndEmpld() {
    this.employeeList = [];

    return this.employeeService.getAllActiveEmployeeInformation().pipe(
      first(),
      map((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.employeeList = response.serviceResponse;
        }
        return true;
      }),
      catchError(error => {
        console.error('Employee list failed', error);
        return of(false);
      })
    );
  }

  getProjectConfigurationDetailsByProjectId(project: any) {
    this.showProjectConfig = false;
    this.rmgProjectObj = null;
    return this.resourceManagementService.getProjectConfigurationDetailsByProjectId(project?.projectId, this.isAllProjects).pipe(
      first(),
      map((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.rmgProjectObj = response.serviceResponse;
          this.rmgProjectObj.state = this.isValidString(this.rmgProjectObj.state) ? this.rmgProjectObj.state : 'NA';
          this.rmgProjectObj.dbProjectManagerIds = this.rmgProjectObj?.projectManagerIds || [];
        }
        return true;
      }),
      catchError(error => {
        console.error('Fetch Project Details failed', error);
        return of(false);
      })
    );
  }

  openProjectConfigurationModal() {
    this.projectConfigurationModalRef = this.modalService.open(this.projectConfigurationTemplateRef, { modalDialogClass: 'modal-lg no-modal-content', backdrop: 'static', keyboard: false });
  }

  closeProjectConfigurationModal() {
    if (this.projectConfigurationModalRef) {
      this.projectConfigurationModalRef?.close();
    }
  }
  // Project Configuration End

  handleProjectAction(event: { action: string; project: any }) {
    const { action, project } = event;
    switch (action) {
      case 'PREVIEW':
        this.isEditProject = false;
        this.showProjectConfigurationDetails(project, true);
        break;
      case 'EDIT_NEW':
        this.showProjectConfigurationDetails(project, false);
        break;
      case 'MARK_AS_COMPLETE':
        this.initiateProjectCompletion(project);
        break;
      case 'APPROVE':
        this.onApproveProject(project);
        break;
      case 'REJECT':
        this.openRejectProjectModal(project);
        break;
      case 'CLIENT_SIDE_ID_STATUS':
        this.isClientSideIdTnmProjectType = project.poProjectType && project?.poProjectType?.trim()?.toLowerCase() === 'tnm';
        this.openClientSideIdConfirmationModal(project.projectId);
        break;
      case 'RESTORE':
        this.openRestoreProjectModal(project.projectId);
        break;
    }
  }

  openProjectLineItemListModal() {
    this.projectLineItemListModalRef = this.modalService.open(this.projectLineItemListModal, { modalDialogClass: 'modal-xl' });
  }

  closeProjectLineItemListModal() {
    this.projectLineItemListModalRef?.close();
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
            this.openAlertMod(this.alertTemplate, response.serviceResponse);
          }
          this.isLoadingMilestones = false;
        },
        error: (err) => {
          console.error("HTTP error fetching milestones:", err);
          this.openAlertMod(this.alertTemplate, "An unexpected error occurred while fetching milestones.");
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

  calculatePoStatus(projectDto: any) {
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
      // this.confirmComplete();
      this.confirmCompleteMailTrigger(projectDto)
    } else {

      this.updateMilestone();
      this.closeUpdateProjectMilestoneModal();
    }
    return allCompleted
  }

  confirmCompleteMailTrigger(projectDto: any) {
    console.log(projectDto.projectId);
    this.resourceManagementService.completeProjectReminder(projectDto.projectId).pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          console.log("Completion mail triggered successfully!!");
        }
      }, (error) => { console.log(error); }
      );
  }



  handleProjectMilestonePageChange(event) {
    this.projectMilestonepage = event;
  }

  openUpdateProjectMilestoneModal(milestone: any) {
    this.extensionReason()
    this.projectMilestone = JSON.parse(JSON.stringify(milestone));
    this.minExtendedDate()
    this.originalStatus = this.projectMilestone.status
    this.updateProjectMilestoneModalRef = this.modalService.open(this.updateProjectMilestoneModal, { modalDialogClass: 'modal-xl' });
  }

  closeUpdateProjectMilestoneModal() {
    this.updateProjectMilestoneModalRef?.close();
  }

  updateMilestone(): void {
    this.modalRef = this.modalService.open(this.updateProjectMilestoneSuccessModal, {
      modalDialogClass: 'modal-sm'
    });
  }

  viewDocument(): void {
    this.modalRef = this.modalService.open(this.milestoneDocumentModal, {
      modalDialogClass: 'modal-xm'
    });
  }
  closeModalViewDocument() {
    this.modalRef?.close();
  }

  CancelUpdateMilestonePopup() {
    this.modalRef?.close();
  }


  onFileSelected(event: any, projectMilestone: any): void {
    const file: File = event.target.files[0];
    this.selectedFile = null;
    this.selectedFilePreviewUrl = null;

    if (!file) { return; }

    if (file) {
      const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];
      if (!allowedTypes.includes(file.type)) {
        this.openAlertMod(this.alertTemplateForMilestone, "Invalid file type. Please upload only PDF, JPG, JPEG, or PNG files.");
        event.target.value = '';
        this.selectedFile = null;
        return;
      }

      const maxSize = 25 * 1024 * 1024; // 25MB
      if (file.size > maxSize) {
        this.openAlertMod(this.alertTemplateForMilestone, "File size should be less than 25MB!!");
        return;
      }
      const uniquefile = projectMilestone.id + '_' + file.name
      this.validateFileName(uniquefile, "status");
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedFilePreviewUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
      this.selectedFile = file;
      this.selectedFile = new File([file], uniquefile, { type: file.type });
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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  private requiresDocument(status: string): boolean {
    return status?.trim() === Status.COMPLETED || status?.trim() === Status.ON_HOLD;
  }


  openMileStoneStatusModal() {
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

    // if(this.projectObj.projectStatus==="Completed"){
    //   this.openAlertMod(this.alertTemplateForMilestone, 'Project is already completed. You cannot update the milestone.');
    //   return;
    // }

    this.modalRef = this.modalService.open(this.confirmMilestoneStatusModal, { modalDialogClass: 'modal-sm' });
    this.confirmMilestoneStatus = "Are you sure to update the milestone status from " + this.originalStatus + " to " + this.projectMilestone.status + " ?";
  }

  isLoadingMilestone: boolean = false;
  async updateMilestoneChanges() {
    this.projectNameForMilestoneUpdate = this.projectObj.name;
    this.poNameForMilestoneUpdate = this.projectObj.poNo;

    console.log("before updaed by updated on", this.projectMilestone);

    this.projectMilestone.updatedBy = this.currentUser.empId;
    this.projectMilestone.updatedOn = new Date();
    this.projectMilestone.updatedByName = this.currentUser.name;
    this.projectMilestone.projectNameForMilestoneUpdate = this.projectNameForMilestoneUpdate
    this.projectMilestone.poNameForMilestoneUpdate = this.poNameForMilestoneUpdate
    this.projectMilestone.poProjectId=this.projectObj.poProjectId

    const formData = new FormData();
    formData.append('dto', new Blob([JSON.stringify(this.projectMilestone)], { type: 'application/json' }));
    formData.append('projectName', this.projectNameForMilestoneUpdate);
    formData.append('previousStatus', this.originalStatus)

    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }
    this.isLoadingMilestone = true;

    this.projectService.updateMilestoneById(formData).pipe(first()).subscribe({
      next: (response: any) => {
        this.isLoadingMilestone = false;

        if (response.serviceStatus === "Success") {
          this.selectedFile = null;
          this.isStatusChanged = false;
          const index = this.fcProjectMilestoneList.findIndex(m => m.id === this.projectMilestone.id);
          if (index > -1) { this.fcProjectMilestoneList[index] = { ...this.projectMilestone }; }
          this.calculatePoStatus(this.projectMilestone);
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

  onStatusChange(newStatus: string) {
    this.isStatusChanged = newStatus !== this.originalStatus;
  }

  onExtendedDateSelected() {
    if (!this.projectMilestone.extendedDate) {
      this.projectMilestone.extensionReason = null;
      this.projectMilestone.extensionFile = null;
    }
  }

  clearExtensionFile(fileInput: HTMLInputElement) {
    this.projectMilestone.extensionFile = null;
    fileInput.value = '';
  }

  onExtensionFileSelected(event: any, projectMilestone: any) {

    const file = event.target.files[0];
    if (!file) return;
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];

    if (!allowedTypes.includes(file.type)) {
      this.openAlertMod(this.alertTemplateForMilestone, "Only PDF, JPG, JPEG, or PNG files are allowed.");
      event.target.value = '';
      return;
    }

    const maxSize = 25 * 1024 * 1024; // 25MB
    if (file.size > maxSize) {
      this.openAlertMod(this.alertTemplateForMilestone, "File size should be less than 25MB!!");
      return;
    }

    const uniquefile = projectMilestone.id + '_' + file.name
    this.validateFileName(uniquefile, "extended")
    this.projectMilestone.extensionFile = file;
    this.projectMilestone.extensionFile = new File([file], uniquefile, { type: file.type });



  }

  validateFileName(uniquefile: any, type: any) {
    this.projectService.validateDocName(uniquefile).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // No duplicate
          console.error(response.serviceResponse);
        } else if (response.serviceStatus === 'Fail') {
          // Duplicate found
          type === "extended" ? this.projectMilestone.extensionFile = null : this.selectedFile = null;
          this.openAlertMod(this.alertTemplateForMilestone, response.serviceResponse);
          return false;
        }
      },
      error: (error) => {
        type === "extended" ? this.projectMilestone.extensionFile = null : this.selectedFile = null;
        this.openAlertMod(this.alertTemplateForMilestone, "Error while validating file. Kindly try after sometime!!");
        return false;
      }
    });
  }

  previewMilestoneFile(file1: any) {
    if (!file1) return;

    const file = file1;
    const fileURL = URL.createObjectURL(file1);
    this.milestoneDocumentUrl = this.sanitizer.bypassSecurityTrustResourceUrl(fileURL);
    this.isPdfFile = file.type === 'application/pdf';
    this.isImageFile = file.type.startsWith('image/');

    this.projectMilestoneDocumentModalRef = this.modalService.open(
      this.projectMilestoneDocumentTemplateRef,
      {
        modalDialogClass: 'modal-xl',
        backdrop: 'static',
        keyboard: false
      }
    );
  }


  extensionReason() {
    this.projectService.getAllMilestoneExtendReason().subscribe({
      next: (response) => {
        if (response.serviceStatus === 'Success') {
          this.milestoneExtendReason = response.serviceResponse;
        }
      },
      error: (error) => {
        console.error('Error fetching milestones:', error);

      }
    });
  }

  updateMilestoneExtendedDateWithReason(): Promise<boolean> {

    if (!this.isExtensionEnabled) return
    else if (!this.projectMilestone.extensionReason) {
      this.openAlertMod(this.alertTemplateForMilestone, "Kindly select the extension reason!!");
      return Promise.resolve(false);
    } else if (this.projectMilestone?.extensionReason?.milestoneExtensionReason === 'Other' &&
      (!this.projectMilestone?.customReason || !this.projectMilestone?.customReason.trim())) {
      this.openAlertMod(this.alertTemplateForMilestone, "Kindly Enter Custom Reason!!");
      return Promise.resolve(false);
    }

    console.log("this.currentUser===>>", this.currentUser);
    this.projectNameForMilestoneUpdate = this.projectObj.name;
    this.poNameForMilestoneUpdate = this.projectObj.poNo;

    const payload: MilestoneUpdatedLog = {
      milestoneId: this.projectMilestone.id,
      poId: this.projectMilestone.poId,
      projectId: this.projectMilestone.projectId,
      milestoneName: this.projectMilestone.name,
      milestoneStartDate: this.projectMilestone.startDate ? this.projectMilestone.startDate : null,
      milestoneEndDate: this.projectMilestone.endDate ? this.projectMilestone.endDate : null,
      description: this.projectMilestone.description,
      remarks: this.projectMilestone.remarks,
      milestoneStatus: this.projectMilestone.status,
      extendedDate: this.projectMilestone.extendedDate,
      updatedBy: this.currentUser.empId,
      updatedByName: this.currentUser.name,
      projectName: this.projectNameForMilestoneUpdate,
      poNumber: this.poNameForMilestoneUpdate,
      lineItemName: this.projectMilestone.lineItemName,
      milestoneExtensionReasonId: this.projectMilestone.extensionReason?.id,
      milestoneExtensionReasonText: this.projectMilestone?.extensionReason?.milestoneExtensionReason === 'Other' ? this.projectMilestone.customReason : ""
    };

    const formData = new FormData();
    formData.append("milestoneData", new Blob([JSON.stringify(payload)], { type: "application/json" }));

    // optional file
    if (this.projectMilestone.extensionFile) {
      formData.append("extensionFile", this.projectMilestone.extensionFile);
    }

    return new Promise((resolve) => {
      this.projectService.updateMilestoneExtendedDate(formData).subscribe({
        next: (response: any) => {
          console.log(response);
          if (response?.serviceStatus === 'Success') {
            this.openAlertMod(this.alertTemplateForMilestone, "Date Extended successfully!!");
            this.isExtensionEnabled = false
            this.showProjectMilestones(this.projectObj);
            return
          } else {
            this.openAlertMod(this.alertTemplateForMilestone, "Something went wrong while updating extended date. Kindly try after sometime!!");
            resolve(false);
          }
        }, error: () => {
          this.openAlertMod(this.alertTemplateForMilestone, "Something went wrong while updating extended date. Kindly try after sometime!!");
          resolve(false);
        }
      });
    });
  }

  onExtendedDateChange(event: any) {
    if (event.value) { this.isExtensionEnabled = true; }
  }

  minExtendedDateformilestone: Date;

  /** Earliest selectable new end date: calendar day after milestone start (no max). */
  minExtendedDate(): void {
    const startDate = this.projectMilestone?.startDate;
    if (!startDate) return;
    this.minExtendedDateformilestone = moment(startDate).startOf('day').add(1, 'day').toDate();
  }

  get milestoneExtendEndDateTooltip(): string {
    const sd = this.projectMilestone?.startDate;
    if (!sd) {
      return (
        'Choose any date from the day after the milestone start date onward.\n' +
        'There is no latest date limit.'
      );
    }
    const formatted = moment(sd).format('D MMM YYYY');
    return (
      `Milestone start date is ${formatted}.\n` +
      'You may select any date after that day.\n' +
      'Pick a date before the current end date to finish earlier, or after it to extend.'
    );
  }

  closeProjectMilestoneDocumentsModal() {
    if (this.projectMilestoneDocumentModalRef) {
      this.projectMilestoneDocumentModalRef?.close();
      this.projectMilestoneDocumentModalRef = null;
    }
    this.milestoneDocumentUrl = null;
  }

  toggleExtensionLogs(milestone: any) {
    if (!milestone.extendedDate) { return; }
    this.expandedMilestoneId = this.expandedMilestoneId === milestone.id ? null : milestone.id;
  }

  toggleLogs(milestone: any, type: 'start' | 'end' | 'status') {

    // If clicking same milestone and same log type → collapse
    if (this.expandedMilestoneId === milestone.id && this.selectedLogType === type) {
      this.expandedMilestoneId = null;
      this.selectedLogs = [];
      this.selectedLogType = null;
      this.logHeader = '';
      return;
    }
    // Expand row
    this.expandedMilestoneId = milestone.id;
    this.selectedLogType = type;
    this.selectedLogs = [];

    switch (type) {
      case 'start':
        this.selectedLogs = milestone.milestoneExtendedStartDateLogs || [];
        this.logHeader = 'Start Date';
        break;

      case 'end':
        this.selectedLogs = milestone.milestoneExtendedEndDateLogs || [];
        this.logHeader = 'End Date';
        break;

      case 'status':
        this.selectedLogs = milestone.milestoneStatusLogs || [];
        this.logHeader = 'Status';
        break;
    }
  }

  trackByLog(index: number, log: any) {
    return log.documentId || index;
  }


  previewDocument(documentId: number, documentName: any) {

    this.projectService.getExtensionDocumentByName(documentName).subscribe((res: any) => {
      if (!res || !res.documentContent) {
        this.milestoneDocumentUrl = null;
        this.openAlertMod(this.alertTemplateForMilestone, "Error fetching document for preview. Kindly try after sometime!!");
        return;
      }
      const byteCharacters = atob(res.documentContent);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: res.documentType });
      const url = window.URL.createObjectURL(blob);

      this.milestoneDocumentUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      this.isPdfFile = res.documentType === 'application/pdf';
      this.isImageFile = res.documentType.startsWith('image/');

      this.projectMilestoneDocumentModalRef = this.modalService.open(
        this.projectMilestoneExtendedPreviewTemplateRef, { modalDialogClass: 'modal-xl', keyboard: false });
    },
      (error) => {
        console.error('Error fetching document:', error);
        this.openAlertMod(this.alertTemplateForMilestone, "Error fetching document for preview. Kindly try after sometime!!");
        this.milestoneDocumentUrl = null;
        return
      }
    );
  }

  closeProjectMilestoneImageModal() {
    this.showMilestoneImageModal = false;
    this.milestoneDocumentUrl = null;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
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

  clearSelectedFile(fileInput: HTMLInputElement) {
    this.selectedFile = null;
    fileInput.value = '';
  }

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

}
