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
import { RmgProject } from 'src/app/models/rmgProject';
import { RmgStatusCardsComponent } from './rmg-status-cards/rmg-status-cards/rmg-status-cards.component';
import { RmgProjectComponent } from './rmg-project-config/rmg-project-config.component';

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

@Component({
  standalone: false,
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})

export class ResourceManagementComponent implements OnInit {

  @ViewChild('rmgStatusCards') rmgStatusCardsComponent!: RmgStatusCardsComponent;
  @ViewChild('rmgProjectConfig') rmgProjectComponent!: RmgProjectComponent;
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

  showViewProjects() {
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
    if (this.rmgStatusCardsComponent) {
      this.rmgStatusCardsComponent.onDepartmentSelectionChange(this.selectedDepartmentIds);
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

    this.selectedDepartmentIds = this.filteredDepartments?.map(dept => dept.deptId) || [];
    this.oldSelectedDepartmentIds = [...this.selectedDepartmentIds];
    if (this.rmgStatusCardsComponent) {
      this.rmgStatusCardsComponent.onDepartmentSelectionChange(this.selectedDepartmentIds);
    }
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

  async getManagerList() {
    this.managerList = [];
    this.filteredManagerList = [];
    let reqObj = { ...this.employeeObj };
    reqObj.role = "Manager";
    if (reqObj.employeementId) {
      reqObj.employeementId = reqObj.employeementId.substring(2);
    }
    this.employeeObj.role = "Manager";
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    try {
      const response: any = await this.employeeService.getAllEmployeesByRole(reqObj).pipe(first()).toPromise();
      if (response.serviceStatus === "Success") {
        this.managerList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.overheadList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.managerList.forEach((emp) => {
          if (emp.employeementId) {
            let idStr = String(emp.employeementId); // Force convert to String
            if (!idStr.startsWith("A-")) {
              emp.employeementId = "A-" + idStr;
            } else {
              emp.employeementId = idStr;
            }
          }
        });

        this.overheadList.forEach((emp) => {
          if (emp.employeementId) {
            let idStr = String(emp.employeementId);
            if (!idStr.startsWith("A-")) {
              emp.employeementId = "A-" + idStr;
            } else {
              emp.employeementId = idStr;
            }
          }
        });
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
    this.completedProjectDetails.projectCompletionDate = this.selectedDate;
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
      employeementId: this.employeeObj?.employeementId?.substring(2)
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
      emp.employeementId = "A-".concat(emp.employeementId);
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
        this.openClientSideIdConfirmationModal(project.projectId);
        break;
      case 'RESTORE':
        this.openRestoreProjectModal(project.projectId);
        break;
    }
  }

}
