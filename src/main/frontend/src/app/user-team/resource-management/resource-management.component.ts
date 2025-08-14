import { ViewportScroller } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild,Renderer2, ElementRef } from '@angular/core';
import { _MatAutocompleteBase, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { FormControl, NgForm } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import * as Highcharts from 'highcharts';
import * as moment from 'moment'; 
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, map, startWith } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Status } from 'src/app/enum/status';
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { employeeReport } from 'src/app/models/employeeReport';
import { FCProjectMilestone } from 'src/app/models/fcProjectMilestone';
import { Feature } from 'src/app/models/feature';
import { FilteredTimesheet } from 'src/app/models/filteredTimesheet';
import { GetProjectDetailsForBulkDefaultUpdate } from 'src/app/models/getProjectDetailsForBulkDefaultUpdate';
import { PreviousDefaultProject } from 'src/app/models/previousDefaultProject';
import { Project } from 'src/app/models/project';
import { ProjectFilterDTO } from 'src/app/models/projectFilterDTO';
import { ProjectRequirements } from 'src/app/models/projectRequirements';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { User } from 'src/app/models/user';
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
import { MatDialog } from '@angular/material/dialog';
import { OrgChartNode } from 'src/app/orgChatModule';




class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})
export class ResourceManagementComponent implements OnInit {

  nodes: OrgChartNode[] = [];

  topStats = [
    { value: 96, label: "Ishine's Billable", icon: "fa-users", iconColor: "#5E35B1", borderColor: "#5E35B1" },
    { value: 104, label: "Shankh's Billable", icon: "fa-users", iconColor: "#FFB300", borderColor: "#FFB300" },
    { value: -5, label: "TNM Difference", icon: "", iconColor: "#D32F2F", borderColor: "#D32F2F" },
    { value: -7, label: "Fixed Cost Difference", icon: "", iconColor: "#D32F2F", borderColor: "#D32F2F" }
  ];

  statusCards = [
    { value: 12, label: "Pending for approval", icon: "fa fa-clock", iconColor: "#FFB300", borderColor: "#FFB300" },
    { value: 45, label: "Approved", icon: "fa-check-circle", iconColor: "#4CAF50", borderColor: "#4CAF50" },
    { value: 28, label: "Not Started", icon: "fa-minus-circle", iconColor: "#9E9E9E", borderColor: "#9E9E9E" },
    { value: 28, label: "Completed", icon: "fa-check", iconColor: "#009688", borderColor: "#009688" },
    { value: 5, label: "On Hold", icon: "fa-pause-circle", iconColor: "#F44336", borderColor: "#F44336" },
    { value: 32, label: "Pending", icon: "fa-hourglass-half", iconColor: "#03A9F4", borderColor: "#03A9F4" }
  ];

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

  summaryModalRef: BsModalRef;
  projectSummaryData: any[] = [];

  selectedFile: File | null = null;
  selectedFilePreviewUrl: string | null = null;
  milestoneDocumentUrl: SafeResourceUrl | null = null;
  modalRef: BsModalRef = new BsModalRef();
  isModalFullscreen = false;

  isTNMCollapsed = false;

  notStartedCount:number=0;
  pendingForApprovalCount:number=0;
  approvedCount:number=0;
  completedInIshineCount:number=0;
  completedCount:number=0;
  completedWithEmployeeCount:number=0;




  // new cards changes.....................................................................
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;


  @ViewChild('alertTemplate') alertTemplateForMilestone!: TemplateRef<any>;

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

  @ViewChild('chartSection') 
  chartSection!: ElementRef;


  data: string;
  currentUser: User;
  feature = "Resource Management";
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

  projectObj: Project = new Project();
  projectObj2: Project = new Project();
  dataObj: Project = new Project()
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
  filters: any = {};
  isSearchEnabled: boolean = false;
  // projectColumns: any[] = ["blank", "draftStatus", "name", "poNo", "projectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "poStartDate", "poEndDate", "state", "createdOn", "status"];
  projectColumns: any[] = ["blank", "name", "poNo", "poProjectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "poStartDate", "poEndDate", "state", "createdOn", "status","projectStatus", "draftStatus"];

  projectDetails: any = [];
  copyDepartment: any = [];
  currentBreadcrumbList: any[] = [];
  // employeesFor360: any[] = [];
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
  employeeCtrl = new FormControl();
  searchText: any;
  searchTextDept: any;
  selectedStatusTab: string = '';
  @ViewChild('customDatePickerTemplate')
  customDatePickerTemplate!: TemplateRef<any>;

  selectedDate: String | null = null;
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
  fallBackMsg:any;
  isApproved:boolean = false;
  advanceFilter: any;
  skipSelectionChange: boolean = false;
isLoadingMilestones: any;
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
statusTab: any;
toggleDepartmentsVisible: boolean = false;

toggleDepartments() {
  this.toggleDepartmentsVisible = !this.toggleDepartmentsVisible;
}


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
    private el: ElementRef 
  ) {

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const reportUrl = `${environment.baseUrl360}#/user-reports/report-list`;
    this.reportListUrlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(reportUrl);

  }




  async ngOnInit(): Promise<void> {
    this.myDept = true;
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      console.log(sub,"sub")
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    // console.log("lalalalalalalalal", this.userMapping);
    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();
    if (!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") &&
      !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin") && !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")) {
      await this.getAllDepartments();
      await this.getDeptsByUser();
      // this.projectFilterDTO.isHod = true
    }
    else {
      await this.getAllDepartments();
      // await this.getDeptsByUser();
      if (this.filteredDepartments != null)
        this.projectFilterDTO.isAdmin = true;
      else {
        this.projectFilterDTO.isOther = true
        this.isAdminOrHod = false;
      }
    }
    console.log(this.projectFilterDTO);
    this.route.params.subscribe((params: Params) => {
      this.currentProjectId = params['id'];
    });
    //console.log(this.currentProjectId, " : this.currentProjectId");
    this.sectionViewInit();
    this.getEmployeeByNameAndEmpld();
    // this.initializeActiveProjectFilters();

    this.employeeCtrl.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value?.name || ''),
        map(name => this.filterEmployees(name))
      )
      .subscribe(filtered => {
        this.filteredEmployees = filtered;
      });

    this.teamLeadCtrl.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value?.name || ''),
        map(name => this.filterTeamLeads(name))
      )
      .subscribe(filtered => {
        this.filteredTeamLeads = filtered;
      });

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.toggleSearch();
    }
    // this.employee360Service.employeesFor360$.subscribe((employees) => {
    //   this.employeesFor360 = employees;
    //   console.log("Employee Data fetched by Shared service ",this.employeesFor360);
    // });
    // console.log('userMapping',this.userMapping);

    if (deptName === "Accounts") {
      this.isAccounts = true;
    }
    // this.toggleSelectAllDept();
    this.projectFilterDTO.approvalStatus = "All";
    this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
    // this. projectFilterDTO.departmentsids = this.filteredDepartments.map(dept => dept.deptId);
    // await this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);

    await this.RbacInternalProjects(this.projectFilterDTO);

    await this.getEmployeesWithoutBillability(this.projectFilterDTO);

    this.fetchTimesheetMissingCount();
   
    this.initializeExpiredProjectFilters();
     this.selectedExpiredProjectFilter = this.expiredProjectFilters[0];

    await this.RbacShankhProjects(this.projectFilterDTO);

    await this.RbacAllShankhInternalProjects(this.projectFilterDTO);
    await this.ExceptionEmployeeReport(this.projectFilterDTO);
    await this.RbacBothShankhInternal(this.projectFilterDTO);
    await this.ProjectLessEmployees(this.projectFilterDTO);
    await this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);


    this.fetchTimesheetMissingCount();
    
this.initializeExpiredProjectFilters();

  // this.loadExpiredProjectCounts(null).then(() => {
  //   console.log('Initial expired project counts loaded');
  // }).catch((error) => {
  //   console.error('Failed to load initial counts:', error);
  // });



    await this.getProjectDetailsForBulkDefaultUpdate();
    await this.TotalEmployeeCount();

    // const deptName = String(this.currentUser.departmentName).trim();
    // const empRole = String(this.currentUser.employeeRole).trim();
    // if(!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") && 
    // !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin")&& !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")){
    //   await this.onDepartmentToggle();
    // }
    // else{
    //   await this.onDepartmentToggle();
    // }

    if (this.filterStateService.projectReportFilters) {
      this.filters = this.filterStateService.projectReportFilters;
      this.isSearchEnabled = true;
    }
    if (this.filterStateService.deptIdList && this.filterStateService.deptIdList.length > 0) {
      console.log(this.filterStateService.deptIdList, "this.filterStateService.deptIdList");
      this.deptIdList = this.filterStateService.deptIdList;
      this.myDept = this.filterStateService.myDept;
      this.onDepartmentSelectionChange();
    }
    if (this.filterStateService.deptIdListByUser && this.filterStateService.deptIdListByUser.length > 0) {
      console.log(this.filterStateService.deptIdListByUser, "this.filterStateService.deptIdListByUser");
      this.myDept = this.filterStateService.myDept;
      this.deptIdListByUser = this.filterStateService.deptIdListByUser;
      this.onDepartmentSelectionChangeByUser();
    }
    if (this.filterStateService.selectedStatusTab) {
      this.selectedStatusTab = this.filterStateService.selectedStatusTab;
      this.selectStatusTab(this.selectedStatusTab);
      console.log(this.selectedStatusTab, "this.selectedStatusTab");
    }

    // this.getFixedCostProjectList("all");
    // this.getFixedCostProjectList("defaulter");
    // this.fetchCompletedProjectsCount("lastmonth");

    this.getFixedCostCount(this.projectFilterDTO);
    console.log("count",this.expiredProjectFilters);

    this.calculateCardLevels();
    
  }


   toggleModalFullscreen() {
    this.isModalFullscreen = !this.isModalFullscreen;
    if (this.modalRef) {
      if (this.isModalFullscreen) {
        
        let elem= this.modalRef;// this.modalRef.setClass('custom-modal modal-dialog.fullscreen-modal');
        // this.modalRef.requestFullscreen();
       elem.setClass('custom-modal modal-dialog.fullscreen-modal');

      } else {
        
        this.modalRef.setClass('custome-modal modal-lg');
      }
    }
  }
  sectionViewInit() {
    if (this.currentUser.employeeRole == 'HOD' || this.currentUser.employeeRole == 'SuperAdmin') {
      this.isHOD = true;
    } else {
      this.isHOD = false;
    }
    if (this.currentUser.employeeRole == '' || this.currentUser.employeeRole == undefined || this.currentUser.employeeRole == null) {
      this.isHOD = false;
    }

    if (this.userMapping.view_all_rmg_projects) {
      this.showViewProjects();
    }

    if (this.currentProjectId != undefined || this.currentProjectId != null) {
      this.showViewProjects();
    }
  }

  showViewProjects() {
    this.isProjectTable = true;
    this.allProjectTable = true;

    this.isHideButton = false;
    this.isEditProject = false;
    this.isCreateForm = false;
    this.isCreation = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.allProjectList = [];
    this.teamCreatedProjectList = [];
    // this.getManagerList();
    // this.alreadyCreatedTeam();
  }



  showEditProjectForm(project: any) {
    this.isEditProject = true;

    this.allProjectTable = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
  }

  closeEditProject() {
    this.isHideButton = false;
  }

  showCreateForm() {
    this.isCreateForm = true;
    this.isCreation = true;

    this.isProjectTable = false;
    this.allProjectTable = false;
    this.isUpdateForm = false;

    this.projectObj = new Project();
    this.getAllDepartmentListForCreateProject();
    this.getManagerList();
    this.getAllClientList();
    this.projectObj.clientId = '';
  }

  allStates: any[] = [
    "Maharashtra",
  ];

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

  clearSelection(event: Event) {
    
    console.log(this.skipSelectionChange,"this.skipSelectionChange")
    console.log(this.isAllSelected, "this.isAllSelected");
    // this.skipSelectionChange = true;
    console.log(this.skipSelectionChange,"this.skipSelectionChange")
    event.stopPropagation();
    if (this.isAllSelected == true) {
      console.log(this.isAllSelected, "this.isAllSelected");
      this.toggleSelectAllDept();

    }
    else {
      this.deptIdList = [];
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      //  this.projectFilterDTO.departmentsids = null;
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdList = this.deptIdList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.rbacApiCalls();
    }
    this.searchTextDept = '';
    this.filterDepartments();

  }


  filterDepartments() {
    const lowerText = this.searchTextDept.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }
 
  onDepartmentSelectionChange() { 
    this.skipSelectionChange = false 
    console.log(this.skipSelectionChange,"this.skipSelectionChange")
    if (this.skipSelectionChange) {
      return;
    }
    console.log(this.skipSelectionChange,"this.skipSelectionChange")
    console.log(this.isAllSelected, "this.isAllSelected", this.deptIdList.length, "this.dept length");
    if (!this.isAllSelected && this.deptIdList.length > 0 && this.deptIdList[0] != null) {
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdList;
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdList = this.deptIdList;
      this.filterStateService.myDept = this.myDept;
      this.rbacApiCalls();

      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      console.log(this.filterStateService.deptIdList, "this.filterStateService.deptIdListByUser");
    }
    else {
      this.projectFilterDTO = this.deptList2;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdList;
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdList = this.deptIdList;
      this.rbacApiCalls();
    } 
  }

  //   onDepartmentSelectionChange1() {
  //     console.log(this.isAllSelected, "this.isAllSelected");
  //     if (!this.isAllSelected && this.projectObj.departmentList.length > 0 && this.projectObj.departmentList[0] != null) {
  //         this.projectFilterDTO.approvalStatus = "All";
  //         this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
  //         this.projectFilterDTO.departmentsids = this.projectObj.departmentList;
  //         console.log(this.projectFilterDTO, "this.projectFilterDTO");
  //         this.fetchCountAndList();
  //     }
  // }

  //  onDepartmentSelectionChange2() {
  //     console.log(this.isAllSelected, "this.isAllSelected");
  //     if (!this.isAllSelected && this.teamObj.departmentList.length > 0 && this.teamObj.departmentList[0] != null) {
  //       this.projectFilterDTO.approvalStatus = "All";
  //       this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
  //       this.projectFilterDTO.departmentsids = this.teamObj.departmentList;
  //       console.log(this.projectFilterDTO, "this.projectFilterDTO");
  //       this.fetchCountAndList();
  //     }
  // }

  
  // toggleSelectAllDept() {
  //   console.log(this.skipSelectionChange,"this.skipSelectionChange")
  //   // this.skipSelectionChange = true
  //   console.log(this.isAllSelected, "this.isAllSelected")
    
  //   if (this.isAllSelected) {
  //     this.deptIdList = [];
  //     this.skipSelectionChange = false;
  //     this.isAllSelected = false;
  //     console.log(this.skipSelectionChange,"this.skipSelectionChange")
  //     console.log(this.isAllSelected, "this.isAllSelected")
  //   } else {
  //     this.deptIdList = this.filteredDepartments.map(dept => dept.deptId);
  //     console.log(this.deptIdList, "this.deptIdList");
  //     this.isAllSelected = true;
  //     this.skipSelectionChange = true;
  //      console.log(this.skipSelectionChange,"this.skipSelectionChange")
  //     console.log(this.isAllSelected, "this.isAllSelected")
  //   }

  //   this.projectFilterDTO.approvalStatus = "All";
  //   this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
  //   this.projectFilterDTO.departmentsids = this.deptIdList;
  //   this.projectFilterDTO.departments = [];
  //   this.filterStateService.deptIdList = this.deptIdList;
  //   this.rbacApiCalls();
  // }

  toggleSelectAllDept() {
    if (this.isAllSelected) {
      this.deptIdList = [];
    } else {
      this.deptIdList = [...this.filteredDepartments.map(dept => dept.deptId), 'all'];
    }
   
    this.isAllSelected = !this.isAllSelected;

    this.applyFilters(); 
}


onDeptSelectionChange2() {

  console.log(this.deptIdList)

  const selectAllWasClicked = this.deptIdList.includes('all');

 
  const allItemsSelected = this.filteredDepartments.length > 0 &&
    this.deptIdList.filter(id => id !== 'all').length === this.filteredDepartments.length;

  if (selectAllWasClicked && !this.isAllSelected) {
    
    this.deptIdList = [...this.filteredDepartments.map(dept => dept.deptId), 'all'];
    this.isAllSelected = true;
      console.log(this.deptIdList)

  } else if (!selectAllWasClicked && this.isAllSelected) {
  
    this.deptIdList = [];
    this.isAllSelected = false;
  } else if (!this.isAllSelected && allItemsSelected) {

    this.isAllSelected = true;
    this.deptIdList.push('all');
  } else if (this.isAllSelected && !allItemsSelected) {
   
    this.isAllSelected = false;
    this.deptIdList = this.deptIdList.filter(id => id !== 'all');
  }


  console.log(this.filteredDepartments, "this.filteredDepartments");
  console.log(this.deptIdList, "this.deptIdList");

  this.applyFilters();
}

onDeptSelectionChange1() {

  const selectAllWasTriggered = this.deptIdList.includes('all');
  const allItemsAreSelected = this.filteredDepartments.length > 0 &&
      (this.deptIdList.length - (selectAllWasTriggered ? 1 : 0)) === this.filteredDepartments.length;

  if (selectAllWasTriggered && !this.isAllSelected) {
    this.isAllSelected = true;
    this.deptIdList = [...this.filteredDepartments.map(dept => dept.deptId), 'all'];
  }

  else if (!selectAllWasTriggered && this.isAllSelected) {
    this.isAllSelected = false;
    this.deptIdList = [];
  }

  else if (allItemsAreSelected && !this.isAllSelected) {
    this.isAllSelected = true;
    if (!this.deptIdList.includes('all')) {
        this.deptIdList.push('all');
    }
  }
  else if (!allItemsAreSelected && this.isAllSelected) {
    this.isAllSelected = false;
    this.deptIdList = this.deptIdList.filter(id => id !== 'all');
  }
  this.applyFilters();
}

    applyFilters() {
        console.log('Applying filters with departments:', this.deptIdList);

        const selectedIds = this.deptIdList.filter(id => id !== 'all');
        const selectedIdsSet = new Set(selectedIds);
        const selectedDepartments = this.filteredDepartments.filter(dept => selectedIdsSet.has(dept.deptId));

        this.projectFilterDTO.approvalStatus = "All";
        this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
        this.projectFilterDTO.departmentsids = selectedIds;
        this.projectFilterDTO.departments = selectedDepartments;

        this.departmentsList = this.projectFilterDTO.departmentsids;
        console.log(this.departmentsList, "+++++++++++++++++++++++++++++++this.depatmentFilterDTO");
        // this.getAllEmployeesByDepartmentIds(this.departmentsList);

        this.rbacApiCalls();
    }

  toggleSelectAllDept1() {
    console.log(this.isAllSelected, "this.isAllSelected");
    if (this.isAllSelected) {
      // Deselect all if already selected
      this.projectObj.departmentList = [];
      this.isAllSelected = false;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
      this.projectFilterDTO.departmentsids = [];
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
    } else {
      // Select all departments
      this.projectObj.departmentList = this.filteredDepartments.map(dept => dept.deptId);
      this.isAllSelected = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
      this.projectFilterDTO.departmentsids = this.projectObj.departmentList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
    }
  }
  toggleSelectAllDept2() {
    // this.employeeReportObj.deptId = [];
    console.log(this.isAllSelected, "this.isAllSelected")
    if (this.isAllSelected) {
      // Deselect all if already selected
      this.teamObj.departmentList = [];
      this.isAllSelected = false;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.fetchCountAndList();
    } else {
      // Select all departments
      this.teamObj.departmentList = this.filteredDepartmentsTeam.map(dept => dept.deptId);
      this.isAllSelected = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.teamObj.departmentList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.fetchCountAndList();
    }

  }

  deptList2: any;


  getAllDepartments(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getDeptsByRole(this.currentUser.empId).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.deptList = response.serviceResponse;
            this.deptList2 = response.serviceResponse;
            this.departments = this.deptList.departments;
            this.deptIdList = this.departments;
            console.log("this.departments",this.departments);
            this.departmentsList = [...(this.departments || [])];
            this.filteredDepartments = [...(this.departments || [])];
            this.filteredDepartmentsTeam = [...this.departmentsList];
            // console.log(this.filteredDepartments, "this.filteredDepartments");
            this.projectFilterDTO = this.deptList;
            this.projectFilterDTO.departments = [];
            this.projectFilterDTO.departmentsids = [];
            this.onDepartmentToggle(this.projectFilterDTO);
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
            console.log(this.departments, "this.departments")
            // this.deptIdList = this.filteredDepartments.map(dept => dept.deptId);
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

  selectStatusTab(status: string) {
    this.page = 1;
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    this.selectedStatusTab = status;
    this.filterStateService.selectedStatusTab = status;
    console.log(this.selectedStatusTab)
    console.log(this.selectedStatusTab, "this.selectedStatusTab");
    this.projectFilterDTO.approvalStatus = this.selectedStatusTab;
    if (this.selectedStatusTab == "Completed") {
      this.projectFilterDTO.completionStatus = this.selectedStatusTab;
      this.projectFilterDTO.approvalStatus = "All";
    }
    else if (this.selectedStatusTab == "CompletedWithTeam") {
      this.projectFilterDTO.completionStatus = this.selectedStatusTab;
      this.projectFilterDTO.approvalStatus = "All";
    }
   else if (this.selectedStatusTab == "activeTNMProjects") {
        this.projectFilterDTO.completionStatus = null; 
        this.projectFilterDTO.approvalStatus = "activeTNM";
    }
    else if (this.selectedStatusTab == "allMonitoringProject") {
        this.projectFilterDTO.completionStatus = null; 
        this.projectFilterDTO.approvalStatus = "monitoring";
    }
    else if (this.selectedStatusTab == "allInternalProject") {
        this.projectFilterDTO.completionStatus = null; 
        this.projectFilterDTO.approvalStatus = "internal";
    }
     else if (this.selectedStatusTab == "expiredTNM") {
        // Set default filter if none selected
        if (!this.selectedExpiredProjectFilter) {
            this.selectedExpiredProjectFilter = this.expiredProjectFilters.find(f => f.key === 'allExpiredTNMProjectsCount') || this.expiredProjectFilters[0]; 
        }
        
        this.projectFilterDTO.approvalStatus = "expiredTNM";
        this.projectFilterDTO.completionStatus = null;
        
        this.projectFilterDTO.expiredProjectFilter = this.selectedExpiredProjectFilter.key;
        
        console.log('ExpiredTNM selected - Filter:', this.selectedExpiredProjectFilter);
        console.log('ExpiredTNM selected - Filter Key:', this.projectFilterDTO.expiredProjectFilter);
        console.log('ExpiredTNM selected - DepartmentIds:', this.projectFilterDTO.departmentsids);
    }
    else if (this.selectedStatusTab == "fixedCost"){
       this.getFixedCostProjectList("all",this.projectFilterDTO);
    }
    else {
      this.projectFilterDTO.completionStatus = null;
    }
    console.log(this.projectFilterDTO)
    this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    this.RbacInternalProjects(this.projectFilterDTO);
    this.RbacShankhProjects(this.projectFilterDTO);
    this.getEmployeesWithoutBillability(this.projectFilterDTO);
    this.RbacAllShankhInternalProjects(this.projectFilterDTO);
    this.ExceptionEmployeeReport(this.projectFilterDTO);
    this.RbacBothShankhInternal(this.projectFilterDTO);
    this.ProjectLessEmployees(this.projectFilterDTO);
    this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);
  }

   selectStatusTab1(status: string) {
    this.page = 1;
    this.selectedStatusTab = status;

    
    if (this.scroller) {
      const documentHeight = document.body.scrollHeight;
      this.scroller.scrollToPosition([0, documentHeight]);
    }
  
    this.getFixedCostProjectList(status,this.projectFilterDTO);
  }

getFixedCostCount(projectFilterDTO: any) {
    this.resourceManagementService.getFixedCostCount(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success" && response.serviceResponse) {
        
        this.totalProjectCount = response.serviceResponse.totalFixedCostcount;
        this.defaulterCount = response.serviceResponse.expiredCount;
        this.delayedCount = response.serviceResponse.delayedCount;
        this.ontTimeCount = response.serviceResponse.ontimeCount;
        
        
        const allRange = this.ranges1.find(range => range.value === 'all');
        if (allRange) {
          allRange.count = this.totalProjectCount; 
        }

        const defaulterRange = this.ranges1.find(range => range.value === 'defaulter');
        if (defaulterRange) {
          defaulterRange.count = this.defaulterCount; 
        }
        const delayedRange = this.ranges1.find(range => range.value === 'delays');
        if (delayedRange) {
          delayedRange.count = this.delayedCount; 
        }
        const ontimeRange = this.ranges1.find(range => range.value === 'ontime');
        if (ontimeRange) {
          ontimeRange.count = this.ontTimeCount; 
        }

        console.log("Total Fixed Cost Project Count:", this.totalProjectCount);
        console.log("Defaulter/Expired Count:", this.defaulterCount);
        console.log("Updated ranges1 array:", this.ranges1);

      } else {
        this.totalProjectCount = 0;
        this.defaulterCount = 0;
        this.ranges1.forEach(range => {
            if (range.value === 'all' || range.value === 'defaulter') {
                range.count = 0;
            }
        });
        console.error("Failed to get fixed cost count:", response.serviceResponse || response.serviceMessage);
      }
    });
  }

  getFixedCostProjectList(tabName:any,projectFilterDTO: any) {
    this.isCountLoading = true;
    const payload = {
      projectFilterDTO: projectFilterDTO,
      tabName: this.selectedRange1.value}
    this.resourceManagementService.getFixedCostProjectList(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const receivedProjects = response.serviceResponse;  
        if (tabName === 'all') {
          this.allProject_Po_Internal = receivedProjects;
          console.log("All Projects Updated:", this.allProject_Po_Internal);
        } else if (tabName === 'defaulter') {
          this.allProject_Po_Internal = receivedProjects;
          console.log("All Projects Updated:", this.allProject_Po_Internal);
        }
        else if (tabName === 'delays') {
          this.allProject_Po_Internal = receivedProjects;
          console.log("All Projects Updated:", this.allProject_Po_Internal);
        }
         else if (tabName === 'ontime') {
          this.allProject_Po_Internal = receivedProjects;
          console.log("All Projects Updated:", this.allProject_Po_Internal);
        }
        if (tabName === this.selectedStatusTab) {
          this.fcProjectList = receivedProjects.sort((a, b) => a.projectName.localeCompare(b.projectName));
          console.log("Displayed List Updated:", this.fcProjectList);
        }
        this.isCountLoading = false; 

      } else {
        if (tabName === this.selectedStatusTab) {
          this.fcProjectList = [];
        }
        this.isCountLoading = false; 
        console.error(`Failed to get list for ${tabName}:`, response.serviceResponse);

      }
    });
  }


  getUnfilledPositionsCount(projectFilterDTO: any) {}

  getUnfilledPositionList(projectFilterDTO: any,status: string) {}



   ranges1 = [
    {label: 'Active', value: "all",count:this.totalProjectCount},
    {label:'Default', value: "defaulter",count:this.defaulterCount},
    { label: 'Ontime', value: "ontime",count:this.ontTimeCount},
    { label: 'Delays', value: "delays",count:this.delayedCount},
    
    
  ];


 selectedRange1 = this.ranges1[0];

   
 selectRange1(range: any): void {
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    if (this.selectedRange1 === range) {
      return; 
    }
    
    this.selectedRange1 = range;
    this.getFixedCostProjectList(range.value, this.projectFilterDTO);
  }

  getSelectedRangeCount(): number {
    return this.selectedRange1?.count || 0;
}

  // fetchCompletedProjectsCount(timeRange: string): void {
  //   this.isCountLoading = true; 

  //   this.resourceManagementService.getFixedCostProjectList(timeRange).pipe(first()).subscribe({
  //     next: (response: any) => {
  //       if (response.serviceStatus === "Success") {
  //         this.completedProjectsCount = response.serviceResponse.length;
  //         this.allProject_Po_Internal = response.serviceResponse;
  //         console.log('Completed Projects Count:', this.completedProjectsCount);
  //         console.log('All Projects:', this.allProject_Po_Internal);
  //       } else {
  //         this.completedProjectsCount = 0;
  //         console.error('API Error:', response.serviceResponse);
  //       }
  //       this.isCountLoading = false; 
  //     },
  //     error: (err) => {
  //       this.completedProjectsCount = 0;
  //       this.isCountLoading = false; 
  //       console.error('Failed to fetch completed projects count', err);
  //     }
  //   });
  // }



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

  GetAllResourceRequirementForProject(project: Project) {
    this.resourceManagementService.getAllResourceRequirementForProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.resourceRequirements = response.serviceResponse
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
      return dept;
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

  createDraftProjectInfo(template: TemplateRef<any>, template2: TemplateRef<any>) {
    this.cancelRequest1();
    console.log(this.allTeamList, "this.allTeamList");
    if (this.allTeamList != null && this.allTeamList.length != 0) {


      this.allTeamList.forEach(team => {
        if (team.spoc) {
          team.spocId = team.spoc.empId;
        } else {
          console.log(`No SPOC data available for team: ${team.teamName}`);
        }
        if (team.teamMemberList) {
          team.teamMemberList.forEach(member => {
            if (member.shadow && member.shadow.empId) {
              member.shadowEmpId = member.shadow.empId;
              console.log("shadowEmpId set", member.shadowEmpId)
            } else {
              member.shadowEmpId = null;
              console.log(`No valid shadow data for member: ${member.empId || 'Unknown ID'} in team: ${team.teamName}`);
            }
          });
        }

      });
      this.projectObj.teamList = this.allTeamList;
      this.projectObj.createdBy = this.currentUser.empId;

      if (this.projectObj.poProjectType == null) {
        this.projectObj.projectType = "Internal";
      } else {
        this.projectObj.projectType = this.projectObj.poProjectType;
      }

      // this.projectObj.projectId=this.projectObj.id;

      let inputValidated: boolean = this.validateProjectObj(this.projectObj, template2)
      if (!inputValidated) return;

      if (this.isHOD == true) {
        this.projectObj.isHOD = true;
        //console.log(this.projectObj, " : this.projectObj");
        this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.allTeamMembers = [];

          } else {
            this.openAlertMod6(template2, response.serviceResponse);
          }
        });
      } else {
        this.projectObj.isHOD = false;
        console.log(this.projectObj, " : this.projectObj");
        this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod6(this.alertTemplate, response.serviceResponse);
            this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((response: any) => {
              if (response.serviceStatus == "Success") {
                this.allTeamMembers = [];
                // this.cancelRequest();
                // this.openAlertMod(template, response.serviceResponse);
              } else {
                // this.cancelRequest();
                // this.openAlertMod(template2, response.serviceResponse);
              }
            });
          } else {
            this.openAlertMod6(template2, response.serviceResponse);
          }
        });
      }
    }
    else {
      this.openAlertMod6(template2, "There are currently no teams to be set...!");
    }
  }

  getTeamListByProjectName(project: any) {
    // this.previewTeamList = [];
    //console.log(" project    ",project);
    project.active = null;
    this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj, "projectofthisteam");

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
          this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
        }
      }
    });
    this.getManagerList();
    // console.log("teamObj.teamLeadId ",this.teamObj.teamLeadId);
    // console.log("teamLeadsList ",this.teamLeadsList);

  }

  // getTeamListByProjectName(project: any) {
  //   this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus === 'Success') {
  //       this.projectObj.teamList = response.serviceResponse;

  //       this.projectObj.teamList.forEach((obj) => {
  //         obj.departmentList = obj.departmentList?.map(x => +x);
  //         this.copyDepartment = obj.departmentList;

  //         this.projectObj.resourceRequirements.forEach((requirement) => {
  //           requirement.teamMembers = [];
  //         });

  //         if (obj.teamMemberList) {
  //           obj.teamMemberList.forEach((member) => {
  //             const matchingRequirement = this.projectObj.resourceRequirements.find(
  //               (requirement) => requirement.id === member.id
  //             );

  //             if (matchingRequirement) {
  //               member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
  //               member.emp360 = member.empId;
  //               member.billability = 'Billable'; // Static value for billability
  //               member.shadowResources = member.shadowResources || [];
  //               matchingRequirement.teamMembers.push(member);
  //               matchingRequirement.assigned = matchingRequirement.teamMembers.length;
  //             }
  //           });
  //         }
  //       });

  //       this.projectObj.resourceRequirements.forEach((requirement) => {
  //         requirement.teamMembers = requirement.teamMembers.map(member => ({
  //           ...member,
  //           shadowResources: member.shadowResources.length > 0 ? member.shadowResources : [{}]
  //         }));
  //       });

  //       this.previewTeamList = this.projectObj.teamList;

  //       if (!this.projectObj.teamList?.length) {
  //         this.addInputTeamField();
  //       } else {
  //         this.allTeamList = this.projectObj.teamList;
  //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
  //       }

  //     } else {
  //       console.error(response.serviceResponse);

  //       if (!this.projectObj.teamList?.length) {
  //         this.addInputTeamField();
  //       } else {
  //         this.allTeamList = this.projectObj.teamList;
  //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
  //       }
  //     }
  //   });
  // }

  onApproveProject(project: any) {
    project.empId = this.currentUser.empId;
    // project.projectId = this.projectObj.projectId;
    console.log("this.projectObj.projectId   ", this.projectObj.projectId);
    console.log("this.projectObj.projectId   ", project.projectId);
    if (project.poProjectType != null) {
      project.projectType = project.poProjectType;
    } else {
      project.projectType = "Internal";
    }
    this.resourceManagementService.approvePendingProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showViewProjects();
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
        // this.showPendingForApprovalProject();
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
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
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
        // this.showPendingForApprovalProject();
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  getManagerList() {
    this.managerList = [];

    this.employeeObj.role = "Manager";
    // if( this.employeeObj.isConsultant == 'true' ){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(5)
    // }else{
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    // }
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.managerList = response.serviceResponse;
        this.overheadList = response.serviceResponse;

        this.managerList.forEach((emp) => {
          // if(emp.isConsultant == 'true'){
          //   emp.employeementId = "A-CS-".concat(emp.employeementId);
          // }else{
          //   emp.employeementId = "A-".concat(emp.employeementId);
          // }
          emp.employeementId = "A-".concat(emp.employeementId);
        });
        this.overheadList.forEach((emp) => {
          emp.employeementId = "A-".concat(emp.employeementId);
        });
        this.filteredManagerList = this.managerList;
        this.filteredOverheadList = this.overheadList;

        //console.log("managerList : ", this.managerList);
      } else {
        console.error(response.serviceResponse)
      }
    });
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

  addInputTeamField() {
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


  // addTeamMember() {
  //   const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);
  //   console.log("newteamMember  ", newTeamMember)
  //   if (newTeamMember) {
  //     newTeamMember.employeeRole = this.newteamMember.employeeRole;
  //     newTeamMember.resourceOverviewId = this.newteamMember.resourceOverviewId;

  //     this.allTeamMembers.push(newTeamMember);


  //   }

  //   this.newteamMember = new TeamMember();
  // }


  //raj
  //   private _resetAddMemberForm() {
  //   // 1. Reset the main data objects
  //   this.newteamMember = { // Use a plain object or 'new TeamMember()' if you have a class
  //     employeeTeamMappingId: null,
  //     teamId: null,
  //     empId: null,
  //     name: null,
  //     employeeRole: null,
  //     isTeamLead: null,
  //     billableType :  null,
  //     newBillableType :  null,
  //     isShadow: null,
  //     isDefaultProject: null,
  //     resourceOverviewId:  null,
  //     otherActiveProjects: null
  //     // ... any other default properties
  //   };
  //   this.selectedRequirement = null;

  //   // 2. Reset the Angular FormControl for the autocomplete
  //   // Using setValue('') is often more reliable than reset() for autocomplete display
  //   this.teamMemberCtrl.setValue('');

  //   // 3. Reset the UI state flags to disable the controls again
  //   this.isRoleSelectDisabled = true;
  //   this.isRequirementSelectDisabled = true;
  //   this.isCheckboxesDisabled = true;
  // }
  //   addTeamMember() {
  //   const selectedEmployee = this.employeeListByDept.find(
  //     (employee) => employee.empId == this.newteamMember.empId
  //   );

  //     if (!selectedEmployee) {
  //     console.error("Could not find the selected employee in the list.");
  //     return;
  //   }

  //       const memberToAdd = {
  //         ...selectedEmployee,
  //         employeeRole: this.newteamMember.employeeRole,
  //         resourceOverviewId: this.selectedRequirement.resourceOverviewId,
  //         isShadow: this.newteamMember.isShadow,
  //         isDefaultProject: this.newteamMember.isDefaultProject
  //       };

  //       this.allTeamMembers.push(memberToAdd);

  //       this.newteamMember = new TeamMember();
  //       this.addMemberCtrl.reset();
  //       this.selectedRequirement = null;
  //       this.teamMemberCtrl.reset();

  //     if (this.selectedRequirement) {
  //     memberToAdd['resourceOverviewId'] = this.selectedRequirement.resourceOverviewId;
  //     }

  //     }

  // ---- END-------//
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
  console.log("this.projectObj.resourceRequirements=================", this.projectObj);
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
    if (this.projectObj.resourceRequirements?.length > 0 && (!this.selectedRequirement || this.selectedRequirement === '' || this.selectedRequirement === null || this.selectedRequirement === undefined)) {
      return "Please select Requirement";
    }

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

      this.openAlertMod(this.alertTemplate, errorMessage);
    } else {
      this.addTeamMember();
      this.updateEmployeeListAccordingToTeamMembers();
      this.resetTeamMemberForm();

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
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openAlertMod3(template: TemplateRef<any>, message: any) {
    this.modalRef3 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openShowCreateForm(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  cancelRequest() {
    console.log("cancel call");

    this.modalRef.hide();
  }

  cancelRequest5() {
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
    this.projectObj = Object.assign({}, project);
    console.log(this.projectObj, "this.projectObj");
    this.setManagerName(project);
    this.getTeamListByProjectName(project);

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
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
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData: any) {
    this.filters = searchData;
    // Save the current filter state to the service
    this.filterStateService.projectReportFilters = this.filters;
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;

    if (!this.isSearchEnabled) {
      this.filters = {};
      this.filterStateService.clearProjectReportFilters();
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
            console.log("Project details fetched successfully:", this.projectDetails);

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
            } else {
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

  openProjectTemplateModal(template: TemplateRef<any>, employee: any) {
    this.getExistingProjectsByUser(employee.empId).then((projectDetails) => {
      this.dataObj = employee;

      if (projectDetails.length > 0) {
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
          this.openAlertMod(this.alertTemplate, "You have deselected a department! Wish to add it again, please ensure that the team members for that department are added as well.");

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
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj2 = teamId;

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
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
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
      endDate: this.lastDate || null
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

  // alert_message template is to be passed
CombinedPOInternalList(template: TemplateRef<any>, projectFilterDTO: ProjectFilterDTO) {
    this.allProject_Po_Internal = [];
    this.resourceManagementService.combinedPOINTERNALDataList(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        
         if (this.selectedStatusTab === 'expiredTNM' && response.serviceResponse) {
                let expiredProjects = [];
                
                if (this.selectedExpiredProjectFilter && this.selectedExpiredProjectFilter.key) {
                    const filterKey = this.selectedExpiredProjectFilter.key;
                    
                    const dataKeyMapping = {
                        'expiredProjectsWithin1Month': 'expiredTNMProjectsWithin1Month',
                        'expiredProjects1To2Months': 'expiredTNMProjects1To2Months',
                        'expiredProjects2To3Months': 'expiredTNMProjects2To3Months',
                        'expiredProjects3To6Months': 'expiredTNMProjects3To6Months',
                        'expiredProjects6To9Months': 'expiredTNMProjects6To9Months',
                        'expiredProjects9To12Months': 'expiredTNMProjects9To12Months',
                        'expiredProjectsAbove12Months': 'expiredTNMProjectsAbove12Months',
                        'allExpiredTNMProjectsCount': 'allExpiredTNMProjects'
                    };
                    
                    const dataKey = dataKeyMapping[filterKey] || 'allExpiredTNMProjects';
                    expiredProjects = response.serviceResponse[dataKey] || [];
                } else {
                    expiredProjects = response.serviceResponse.allExpiredTNMProjects || [];
                }
                
                if (expiredProjects && expiredProjects.length > 0) {
                    this.allProject_Po_Internal = expiredProjects.map((project: any) => {
                        project.combinedProjectType = this.getProjectType(project);
                        
                        if (project.projectManager && project.projectManager.trim() !== '') {
                            project.projectManagerName = project.projectManager;
                        } else {
                            project.projectManagerName = ''; 
                        }
                        return project;
                    });
                    
                    console.log(`Loaded ${expiredProjects.length} expired TNM projects for filter: ${this.selectedExpiredProjectFilter?.title}`);
                }
            } 
            else if (this.selectedStatusTab === 'allMonitoringProject' && response.serviceResponse.monitoringProjects && response.serviceResponse.monitoringProjects.length > 0) {
            this.allProject_Po_Internal = response.serviceResponse.monitoringProjects.map((project: any) => {
                project.combinedProjectType = this.getProjectType(project);

                if (project.projectManagers && Array.isArray(project.projectManagers) && project.projectManagers.length > 0) {
                    const managerNamesString = project.projectManagers
                        .map(manager => manager.projectManagerName)
                        .join(', ');
                    project.projectManagerName = managerNamesString;
                } else if (project.projectManager && project.projectManager.trim() !== '') {
                    project.projectManagerName = project.projectManager;
                } else {
                    project.projectManagerName = ''; 
                }
                return project;
            });
            console.log(`Loaded ${this.allProject_Po_Internal.length} monitoring projects`);
        }
        else if (this.selectedStatusTab === 'allInternalProject' && response.serviceResponse.internalProjects && response.serviceResponse.internalProjects.length > 0) {
            this.allProject_Po_Internal = response.serviceResponse.internalProjects.map((project: any) => {
                project.combinedProjectType = this.getProjectType(project);

                if (project.projectManagers && Array.isArray(project.projectManagers) && project.projectManagers.length > 0) {
                    const managerNamesString = project.projectManagers
                        .map(manager => manager.projectManagerName)
                        .join(', ');
                    project.projectManagerName = managerNamesString;
                } else if (project.projectManager && project.projectManager.trim() !== '') {
                    project.projectManagerName = project.projectManager;
                } else {
                    project.projectManagerName = ''; 
                }
                return project;
            });
            console.log(`Loaded ${this.allProject_Po_Internal.length} internal projects`);
        }
            else if (response.serviceResponse.activeTNMProjects && response.serviceResponse.activeTNMProjects.length > 0) {
                this.allProject_Po_Internal = response.serviceResponse.activeTNMProjects.map((project: any) => {
                    project.combinedProjectType = this.getProjectType(project);

                    if (project.projectManagers && Array.isArray(project.projectManagers) && project.projectManagers.length > 0) {
                        const managerNamesString = project.projectManagers
                            .map(manager => manager.projectManagerName)
                            .join(', ');
                        project.projectManagerName = managerNamesString;
                    } else if (project.projectManager && project.projectManager.trim() !== '') {
                        project.projectManagerName = project.projectManager;
                    } else {
                        project.projectManagerName = ''; 
                    }
                    return project;
                });
            }
            else if (response.serviceResponse.combinedNewProjects && response.serviceResponse.combinedNewProjects.length > 0) {
                this.allProject_Po_Internal = response.serviceResponse.combinedNewProjects.map((project: any) => {
                    project.combinedProjectType = this.getProjectType(project);

                    if (project.projectManagers && Array.isArray(project.projectManagers) && project.projectManagers.length > 0) {
                        const managerNamesString = project.projectManagers
                            .map(manager => manager.projectManagerName)
                            .join(', ');

            project.projectManagerName = managerNamesString;
          } else {

            project.projectManagerName = ''; 
          }
          return project;
          this.createDepartmentArray();
        });
        console.log("this.allProject_Po_Internal", this.allProject_Po_Internal);


        // this.tabCounts = response.serviceResponse.counts;
        // this.totalCount = this.tabCounts.rejectedCount + this.tabCounts.notStartedCount + this.tabCounts.approvedCount + this.tabCounts.pendingForApprovalCount
      } else {
        this.openAlertMod(template, "Error Fetching List");
      }
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

  RbacInternalProjects(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.rbacInternalProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.internalMappedEmployeesList = response.serviceResponse;
        this.internalMappedEmployees = this.internalMappedEmployeesList.length;
        console.log("this.internalMappedEmployees", this.internalMappedEmployees)
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });

  }

  RbacShankhProjects(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.rbacShankhProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.sankhMappedEmployeesList = response.serviceResponse;
        this.sankhMappedEmployees = this.sankhMappedEmployeesList.length;
        console.log("this.sankhMappedEmployees", this.sankhMappedEmployees)
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });

  }
  RbacAllShankhInternalProjects(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.rbacAllShankhInternalProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.allMappedEmployeesList = response.serviceResponse;
        this.allMappedEmployees = this.allMappedEmployeesList.length;
        console.log("this.allMappedEmployeesList", this.allMappedEmployeesList)
      }
      else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  ExceptionEmployeeReport(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.exceptionEmployeeReport(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.exceptionEmployeesList = response.serviceResponse;
        this.exceptionEmployees = this.exceptionEmployeesList.length;
        console.log("this.exceptionEmployeesList", this.exceptionEmployeesList)
      }
      else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  RbacBothShankhInternal(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.rbacBothShankhInternal(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.mappedToBothEmployeesList = response.serviceResponse;
        this.mappedToBothEmployees = this.mappedToBothEmployeesList.length;
        console.log("this.mappedToBothEmployeesList", this.mappedToBothEmployeesList)
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  ProjectLessEmployees(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.projectLessEmployees(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.employeesWithoutProjectList = response.serviceResponse;
        this.employeesWithoutProject = this.employeesWithoutProjectList.length;
        console.log("this.employeesWithoutProjectList", this.employeesWithoutProjectList)
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  getEmployeesWithoutBillability(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.getEmployeesWithoutBillability(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.employeesWithoutBillability = response.serviceResponse;
        this.employeesWithoutBillable = this.employeesWithoutBillability.length;
        console.log("this.employeesWithoutProjectList", this.employeesWithoutProjectList)
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }



  TotalEmployeeCount() {
    this.resourceManagementService.totalEmployeeCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.totalEmployees = response.serviceResponse;
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
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
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  filterEmployees(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.employeeList.filter(emp =>
      emp.name.toLowerCase().includes(lowerText) ||
      emp.employmentId.toLowerCase().includes(lowerText)
    );
  }

  onEmployeeSelected(event: any) {
    const selectedEmp = event.option.value;
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      this.employeeCtrl.setValue(selectedEmp.name);
      console.log('Selected Employee ID:', this.selectedEmpId);
    }
  }
  onEmployeeInputChange() {
    if (!this.employeeCtrl.value || this.employeeCtrl.value.trim() === '') {
        this.selectedEmpId = 0;
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

  openSummaryModal(template: TemplateRef<any>, selectedEmpId: any) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-lg' });
    this.selectedEmpId = selectedEmpId;

    this.getProjectTimesheetSummaryData();
  }

    openSummaryModal1(template: TemplateRef<any>) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-lg' });

    this.getfixedCostProjectGraph();
  }

  openSummaryModal2(template: TemplateRef<any>) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-lg' });

    this.getExpiredTNMProjectGraph();
  }

  openSummaryModal3(template: TemplateRef<any>) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-lg' });

    this.getStatusGraphData();
  }

    openSummaryModal4(template: TemplateRef<any>,statusTab : string) {
    this.summaryModalRef = this.modalService.show(template, { class: 'modal-xl' });
    this.selectedStatusTab = statusTab;

    this.prepareAndFetchChartData();
  }


  closeSummaryModal() {
    this.summaryModalRef.hide();
  }

  getProjectTimesheetSummaryData() {
    // 1. Check if the current user and their empId are available.
    if (!this.selectedEmpId) {
      this.openAlertMod(this.alertTemplateWithoutReload, "Cannot fetch summary. User information is missing.");
      console.error("Current user or empId is not available.");
      // Close the modal or show an error state in the chart container if the modal is already open
      if (this.summaryModalRef) {
        // You could display an error message inside the modal body here
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Could not load data: User not identified.</p>';
      }
      return;
    }

    // 2. Create the DTO object to send to the backend.
    const resourceManagementDTO = {
      empId: this.selectedEmpId
    };

    // 3. Pass the DTO to the service call.
    console.log("================================", resourceManagementDTO);
    this.resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.projectSummaryData = response.serviceResponse;

          if (this.projectSummaryData && this.projectSummaryData.length > 0) {
            this.processProjectSummaryData(this.projectSummaryData);
          } else {
            // Handle the case where the API succeeds but returns no data
            document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center">No timesheet summary data found for your projects.</p>';
          }
        } else {
          // this.openAlertMod(this.alertTemplate, "Failed to load project summary data.");
          console.error(response.serviceResponse);
          document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Error:No timesheets filled till date</p>';
        }
      },
      error: (err) => {
        this.openAlertMod(this.alertTemplate, "An error occurred while fetching summary data.");
        console.error(err);
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">A server error occurred. Please try again later.</p>';
      }
    });
  }

  processProjectSummaryData(summaryData: any[]) {
    const categories = [];
    const seriesData = [];

    // Sort data by totalTimesheetsFilled in descending order and take top 20 for better visualization
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

getExpiredTNMProjectGraph() {
  const categories = ['1M', '1-2M', '2-3M', '3-6M', '6-9M', '9-12M', '12M+'];
  
  const data = [
    {
      y: this.expiredProjectsWithin1Month || 0,
      color: '#28a745' 
    },
    {
      y: this.expiredProjects1To2Months || 0,
      color: '#17a2b8' 
    },
    {
      y: this.expiredProjects2To3Months || 0,
      color: '#ffc107' 
    },
    {
      y: this.expiredProjects3To6Months || 0,
      color: '#fd7e14' 
    },
    {
      y: this.expiredProjects6To9Months || 0,
      color: '#dc3545' 
    },
    {
      y: this.expiredProjects9To12Months || 0,
      color: '#6f42c1' 
    },
    {
      y: this.expiredProjectsAbove12Months || 0,
      color: '#e83e8c' 
    }
  ];
  
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

getfixedCostProjectGraph(){
  const categories = ['Active', 'Defaulter', 'Delayed', 'On Time'];
  const data = [
    {
      y: this.totalProjectCount,
      color: '#5cb85c'
    },
    {
      y: this.defaulterCount,
      color: '#d9534f' 
    },
    {
      y: this.delayedCount,
      color: '#f0ad4e' 
    },
    {
      y: this.ontTimeCount,
      color: '#5bc0de' 
    }
  ];
  
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

  renderColumnChart(chartName: any, chartId: any, chartData: any, categories: any,yAxisTitle: string) {
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









  openEditModal(template, project) {
    // console.log("Project ",project)
    this.isEditProject = true;

    this.allProjectTable = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.GetAllResourceRequirementForProject(this.projectObj);
     this.getAllDepartmentList1(project);
    this.getTeamListByProjectName(project);
    this.getEmployeeByNameAndEmpld();
    this.activeModalTab = 'info'; 
    // this.isModalFullscreen = false;

    this.modalRef1 = this.modalService.show(template, { class: 'modal-lg' });
   
  }


  openTeamMembersModal(template: any, projectObj, currentTeam) {
    if (projectObj.id) {
      this.getResourceRequirementByPoProjectId(projectObj.id);
    }

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

    this.modalRefTeamMember = this.modalService.show(template, { class: 'custom-modal' });

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
    this.getAllEmployeesByDepartmentIds(this.deptIdList);
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
            this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
          } else {
            this.selectedDate = '';
            this.modalRef5.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
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
  getResourceRequirementByPoProjectId(id) {
    this.loadingRequirements = true;
    this.resourceManagementService.getResourceRequirementByPoProjectId(id).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequirementsList = response.serviceResponse;
        this.projectObj.resourceRequirements
        this.loadingRequirements = false;
      } else {
        console.error("Error fetching project requirement list");
      }
    });
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

  // filterDepartmentsTeamForm() {
  //   const lowerText = this.searchTextDeptTeam.trim().toLowerCase();

  //   const filtered = this.departmentsList.filter(dept =>
  //     dept.name.toLowerCase().includes(lowerText)
  //   );

  //   const selected = this.departmentsList.filter(dept =>
  //     this.teamObj.departmentList?.includes(dept.deptId)
  //   );
  //   const selectedSet = new Set(filtered.map(dept => dept.deptId));
  //   const merged = [...filtered];

  //   selected.forEach(dept => {
  //     if (!selectedSet.has(dept.deptId)) {
  //       merged.push(dept);
  //     }
  //   });

  //   this.filteredDepartmentsTeam = merged;
  // }
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

    // Update select all state after filtering
    // if (!this.teamObj.departmentList || this.teamObj.departmentList.length === 0) {
    //     this.isAllDeptSelected = false;
    // } else if (this.filteredDepartmentsTeam.length > 0) {
    //     const allFilteredSelected = this.filteredDepartmentsTeam.every(dept => 
    //         this.teamObj.departmentList.includes(dept.deptId)
    //     );
    //     this.isAllDeptSelected = allFilteredSelected;
    // } else {
    //     this.isAllDeptSelected = false;
    // }
  }

  toggleSelectAllTeams() {
    if (this.isAllDeptSelected) {
      this.teamObj.departmentList = [];
      this.isAllDeptSelected = false;
      // console.log("test" , team);
    } else {
      this.teamObj.departmentList = this.filteredDepartmentsTeam.map(dept => dept.deptId);

      this.isAllDeptSelected = true;
      // console.log("test2" , team);
    }
  }



  clearSelectionDept(event: Event) {
    event.stopPropagation();
    this.teamObj.departmentList = [];
    this.isAllDeptSelected = false;
  }

  onSpocSelected(event: any) {
    const selectedSpoc = event.option.value;
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

    const filtered = this.managerList.filter(overhead =>
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

    this.filteredManagerList = merged;
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
      endDate: this.lastDate1 || null
    }));
    console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory, this.teamMemberList1);
    this.projectService.updateProjectResourcesAsInActiveBulk(this.employeeSelectionHistory)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.openAlertMod(template, response.serviceResponse);


          this.selectedMembers = [];
          this.employeeSelectionHistory = [];


          this.teamMemberList1.forEach(team => {

            team.employees.forEach(member => {
              member.selected = false;
            });
          });

          this.getExistingProjectsByUser(this.projectObj2.empId)


        }
        window.location.reload();
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
          this.openAlertMod6(template, response.serviceResponse);


          this.selectedTeamEntries = [];
          this.employeeSelectionHistory = [];


          this.teamMemberList1.forEach(team => {

            team.employees.forEach(member => {
              member.selected = false;
            });
          });

          this.getExistingProjectsByUser(this.projectObj2.empId)


        }
        window.location.reload();
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
      this.openAlertMod3(this.alertTemplateWithoutReload, "Please update Default Project");
      return;
    }

    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        this.activeProjects = this.activeProjects.filter(id => id !== details.empId);
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = this.currentProjectDetails;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        if (this.EmployessIds.length === 0 && this.activeProjects.length === 0) {
          this.modalRef.hide();
          this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
        }
        if (this.EmployessIds.length !== 0 && this.activeProjects.length === 0) {
          this.modalRef.hide();
          this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project of employees who are not mapped to other active projects.");
        }
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
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
          this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
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

  getBenchEmployeeMoreThan30Days(projectFilterDTO: ProjectFilterDTO) {
    this.resourceManagementService.getBenchEmployeeMoreThan30Days(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.employeeBenchRepot = response.serviceResponse;
        this.employeeBench = this.employeeBenchRepot.length;
        // console.log("this.employeeBenchRepot", this.employeeBenchRepot)
      }
      else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
        this.employeeBenchRepotMsg = response.serviceResponse;
        this.employeeBench = 0;
      }
    });
  }

  //router function
  // navigateToOtherPage() {
  //   const reportPageUrl = `${environment.baseUrl360}#/user-reports/report-list`;
  //   window.open(reportPageUrl, '_blank');
  //   //this.router.navigate(['/your-target-route']); 
  // }
  toggleReportView() {
    this.showReportList = !this.showReportList;
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

  getDeptsByUser(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getDeptsByUser(this.currentUser.empId).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.deptListUser = response.serviceResponse;
            this.departmentListByUser = this.deptListUser.departments;
            this.deptIdListByUser = this.departmentListByUser;
            this.filteredDepartmentsByUser = this.departmentListByUser;
            this.projectFilterDTO = this.deptList;
            this.projectFilterDTO.departments = [];
            this.projectFilterDTO.departmentsids = [];
            this.onDepartmentToggle(this.projectFilterDTO);
            // this.combinedPOINTERNALCountList(this.projectFilterDTO);
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

  onDepartmentSelectionChangeByUser() {
        console.log(this.skipSelectionChange,"this.skipSelectionChange")
    if (this.skipSelectionChange) {
      return;
    }
    console.log(this.skipSelectionChange,"this.skipSelectionChange")
    console.log(this.isAllSelectedByUser, "this.isAllSelectedByUser");
    if (!this.isAllSelectedByUser && this.deptIdListByUser.length > 0 && this.deptIdListByUser[0] != null) {
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departments = [];
      this.projectFilterDTO.departmentsids = this.deptIdListByUser;
      this.filterStateService.deptIdListByUser = this.deptIdListByUser;
      this.filterStateService.myDept = this.myDept;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.rbacApiCalls();
      // this.getEmployeeReportData();
      
    }else {
      this.projectFilterDTO = this.deptList2;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdListByUser;
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdList = this.deptIdList;
      this.rbacApiCalls();
     
    
    } 
  }

  filterDepartmentsByUser() {
    const lowerText = this.searchTextDeptByUser.toLowerCase();
    this.filteredDepartmentsByUser = this.departmentListByUser.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  clearSelectionByUser(event: Event) {
    console.log(this.isAllSelectedByUser, "this.isAllSelectedByUser");
    event.stopPropagation();
    if (this.isAllSelectedByUser == true) {
      console.log(this.isAllSelectedByUser, "this.isAllSelectedByUser");
      this.toggleSelectAllDeptByUser();
    }
    else {
      this.deptIdListByUser = [];
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdListByUser = this.deptIdListByUser;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.rbacApiCalls();
    }
  }

  toggleSelectAllDeptByUser() {
    // this.employeeReportObj.deptId = [];
    console.log(this.isAllSelectedByUser, "this.isAllSelectedByUser.......")
    if (this.isAllSelectedByUser) {
      // Deselect all if already selected
      this.deptIdListByUser = [];
      this.isAllSelectedByUser = false;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdListByUser = this.deptIdListByUser;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.rbacApiCalls();
      
    } else {
      // Select all departments
      this.deptIdListByUser = this.filteredDepartmentsByUser.map(dept => dept.deptId);
      this.isAllSelectedByUser = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdListByUser;
      this.projectFilterDTO.departments = [];
      this.filterStateService.deptIdListByUser = this.deptIdListByUser;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.rbacApiCalls();
      
    }

  }



  
  combinedPOINTERNALCountList(projectFilterDTO): Promise<any> {
    return new Promise((resolve, reject) => {
      this.resourceManagementService.combinedPOINTERNALCountList(projectFilterDTO).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.countList = response.serviceResponse;
            this.tabCounts = this.countList.counts;

            this.expiredProjects1To2Months=this.tabCounts.expiredProjects1To2Months;
            this.expiredProjects2To3Months=this.tabCounts.expiredProjects2To3Months;
            this.expiredProjects3To6Months=this.tabCounts.expiredProjects3To6Months;
            this.expiredProjects6To9Months=this.tabCounts.expiredProjects6To9Months;
            this.expiredProjects9To12Months=this.tabCounts.expiredProjects9To12Months;
            this.expiredProjectsAbove12Months=this.tabCounts.expiredProjectsAbove12Months;
            this.expiredProjectsWithin1Month=this.tabCounts.expiredProjectsWithin1Month;

            this.notStartedCount = this.tabCounts.notStartedCount;
            this.pendingForApprovalCount = this.tabCounts.pendingForApprovalCount;
            this.approvedCount = this.tabCounts.approvedCount;
            this.completedInIshineCount = this.tabCounts.completedInIshineCount;
            this.completedCount =  this.tabCounts.completedCount;
            this.completedWithEmployeeCount =   this.tabCounts.completedWithEmployeeCount;

            console.log("this.tabCounts", this.tabCounts);
            // this.selectActiveProjectFilter(this.selectedActiveProjectFilter);
            resolve(response.serviceResponse);
          } else {
            reject("Failed to fetch counts");
          }
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }

 getStatusGraphData() {
  const { data, categories } = this.showMoreCards 
    ? {
        data: [
          { y: this.completedInIshineCount, color: '#5cb85c' },
          { y: this.completedCount, color: '#d9534f' },
          { y: this.completedWithEmployeeCount, color: '#f0ad4e' }
        ],
        categories: ['Ishine Completed', 'Shankh Completed', 'Completed with Employee']
      }
    : {
        data: [
          { y: this.notStartedCount, color: '#5cb85c' },
          { y: this.pendingForApprovalCount, color: '#d9534f' },
          { y: this.approvedCount, color: '#f0ad4e' }
        ],
        categories: ['Not Started', 'Pending for Approval', 'Approved']
      };

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
  onDepartmentToggle(projectFilterDTO) {
    this.projectFilterDTO.approvalStatus = 'All';
    if (!this.myDept) {
      this.projectFilterDTO.departments = this.deptIdList;
      const deptIds: number[] = this.deptIdList.map(dept => dept.deptId);
      this.projectFilterDTO.departmentsids = deptIds;
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);     
    }
    else {
      this.projectFilterDTO.departments = this.deptIdListByUser;
      const deptIds: number[] = this.deptIdListByUser.map(dept => dept.deptId);
      this.projectFilterDTO.departmentsids = deptIds;
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }
  }

  fetchCountAndList() {
    this.myDept = !this.myDept;
    if (this.myDept) {
      this.projectFilterDTO.departments = this.deptIdList;
      const deptIds: number[] = this.deptIdList.map(dept => dept.deptId);
      this.projectFilterDTO.departmentsids = deptIds;
      this.projectFilterDTO.approvalStatus = 'All';
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }
    else {
      this.projectFilterDTO.departments = this.deptIdListByUser;
      const deptIds: number[] = this.deptIdListByUser.map(dept => dept.deptId);
      this.projectFilterDTO.departmentsids = deptIds;
      this.projectFilterDTO.approvalStatus = 'All';
      this.combinedPOINTERNALCountList(this.projectFilterDTO);
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }
  }

  toggleDept(): void {
    this.myDept = !this.myDept;
    this.filterStateService.myDept = this.myDept;
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

  rbacApiCalls() {
    // location.reload();
    this.combinedPOINTERNALCountList(this.projectFilterDTO);
    this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    this.RbacInternalProjects(this.projectFilterDTO);
    this.RbacShankhProjects(this.projectFilterDTO);
    this.getEmployeesWithoutBillability(this.projectFilterDTO);
    this.RbacAllShankhInternalProjects(this.projectFilterDTO);
    this.ExceptionEmployeeReport(this.projectFilterDTO);
    this.RbacBothShankhInternal(this.projectFilterDTO);
    this.ProjectLessEmployees(this.projectFilterDTO);
    this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);
    this.getFixedCostCount(this.projectFilterDTO);
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
    projectObjTemp.poProjectId = projectObj?.poProjectId;

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
        }
    });
}

   switchModalTab(tabName: 'info' | 'milestone') {
    this.activeModalTab = tabName;

    // If switching to the milestone tab, fetch the data
    if (tabName === 'milestone') {
      this.showProjectMilestones(this.projectObj);
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
    this.projectMilestone = milestone;
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
      const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];

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







  updateMilestoneChanges() {
    // Validate required fields
    let isValid = true;
    let errors: any;

    if (!this.projectMilestone.startDate) {
      isValid = false;
      errors = 'Start date is required for milestone';
    }
    if (!this.projectMilestone.endDate) {
      isValid = false;
      errors = 'End date is required for milestone';
    }
    if (this.projectMilestone.startDate && this.projectMilestone.endDate && this.projectMilestone.startDate > this.projectMilestone.endDate) {
      isValid = false;
      errors = 'End date must be after start date for milestone';
    }

    else if (
      !this.projectMilestone.remarks ||
      this.projectMilestone.remarks.trim().length === 0
    ) {
      isValid = false;
      errors = 'Remarks are required for milestone';
    }

    // Status validation
    else if (
      !this.projectMilestone.status ||
      this.projectMilestone.status.trim().length === 0
    ) {
      isValid = false;
      errors = 'Status is required for milestone';
    }

    else if (!this.selectedFile) {
      isValid = false;
      errors = 'Please upload a document for the milestone';
    }


    if (!isValid) {
      this.openAlertMod(this.alertTemplateForMilestone, errors);
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

    this.projectService.updateMilestoneById(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.fcProjectMilestoneList = response.serviceResponse;
        this.updateMilestone();
        this.closeUpdateProjectMilestoneModal();

      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
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

 viewFiles(mileStoneId:any){
 this.projectService.getMilestoneById(mileStoneId).subscribe((res:any)=>{
  if (res.documentContent && res.documentName) {
    const fileType = this.getFileType(res.documentName);
    const imageDataUrl = `data:${fileType};base64,${res .documentContent}`;
    console.log("image url"+imageDataUrl)
    this.dialog.open(ViewImageComponent, {
      width: '80%',
      data: {
        imageUrl: imageDataUrl,
        fileName: res.documentName
      }
    });
  }else{
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

 getAllDepartmentList1(project: any) {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;

        const deptIds = project.deptId
          ?.split(",")
          .map(id => Number(id.trim()))
          .filter(id => !isNaN(id));

        const departmentNames = this.allDeptList
          .filter(x => deptIds.includes(x.deptId))
          .map(x => x.name);



        this.projectObj.department = departmentNames;

      } else {
        console.error(response.serviceResponse);
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

expiredProjectFilters = [
    { key: 'allExpiredTNMProjectsCount', label: 'All', title: 'All Expired TNM Projects' },
    { key: 'expiredProjectsWithin1Month', label: '1M', title: 'TNM Projects Expired Within 1 Month' },
    { key: 'expiredProjects1To2Months', label: '1-2M', title: 'TNM Projects Expired 1 to 2 Months Ago '},
    { key: 'expiredProjects2To3Months', label: '2-3M', title: 'TNM Projects Expired 2 to 3 Months Ago' },
    { key: 'expiredProjects3To6Months', label: '3-6M', title: 'TNM Projects Expired 3 to 6 Months Ago' },
    { key: 'expiredProjects6To9Months', label: '6-9M', title: 'TNM Projects Expired 6 to 9 Months Ago' },
    { key: 'expiredProjects9To12Months', label: '9-12M', title: 'TNM Projects Expired 9 to 12 Months Ago' },
    { key: 'expiredProjectsAbove12Months', label: '12M+', title: 'TNM Projects Expired more than 1 year Ago' }
];

selectedExpiredProjectFilter: any = null;
expiredProjectDisplayCount: number | null = null;

getCurrentExpiredCount(): number {
    if (!this.tabCounts) return 0;
    if (this.selectedExpiredProjectFilter && this.selectedExpiredProjectFilter.key) {
        return this.tabCounts[this.selectedExpiredProjectFilter.key] || 0;
    }
    return this.tabCounts.allExpiredTNMProjectsCount || 0;
}

initializeExpiredProjectFilters() {
  this.selectedExpiredProjectFilter = this.expiredProjectFilters[0];
}

selectExpiredProjectFilter(filter: any) {
    this.selectedExpiredProjectFilter = filter;
    this.projectFilterDTO.expiredProjectFilter = filter.key;
    
    console.log('Selected expired project filter:', filter);
    console.log('Filter key set to:', this.projectFilterDTO.expiredProjectFilter);
    
    this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
}
showMoreCards: boolean = false;

 totalCards: number = 7;
  cardsPerLevel: number = 3;
  maxCardLevels: number;
  currentCardLevel: number;

  calculateCardLevels() {
    this.maxCardLevels = Math.ceil(this.totalCards / this.cardsPerLevel);
    this.currentCardLevel = 1;
  }

  toggleMoreCards() {
    this.currentCardLevel++;
    if (this.currentCardLevel > this.maxCardLevels) {
      this.currentCardLevel = 1;
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


prepareAndFetchChartData() {
  let filteredDepts = this.poDepartments;

  if (this.projectFilterDTO?.departmentsids?.length > 0) {
    const deptIdSet = new Set(this.projectFilterDTO.departmentsids);

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

  this.selectedDepartments = [];

  this.filterError = null;
  this.getClientDepartmentChart(this.projectFilterDTO);
}
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

getClientDepartmentChart(projectFilterDTO?: any) {
  if (projectFilterDTO) {
    this.projectFilterDTO = projectFilterDTO;
  }



  const payload = {
    projectStructure: {
      deptName: this.selectedDepartments?.length > 0 ? this.selectedDepartments : null,
      type: this.selectedStatusTab || 'All'
    },
    projectFilter: this.projectFilterDTO
  };

  this.projectService.getClientVsDepartment(payload).pipe(first()).subscribe((res: any) => {
    if (res.serviceStatus === "Success") {
      this.serviceResponse = res.serviceResponse || [];
      this.processDataForOrgChart();
    } else {
      console.error("Service call failed:", res.serviceResponse);
      this.serviceResponse = [];
      this.orgChartData = [];
    }
  });
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


expandedProjects: Set<string> = new Set(); 
projectFullText: Map<string, string> = new Map(); 

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


toggleAllDepartments(selectAll: boolean) {
  if (selectAll) {
    this.selectedDepartments = this.allDeptList.map(dept => dept.name);
  } else {
    this.selectedDepartments = [];
  }
  this.getClientDepartmentChart();
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



// New method to get full cell text
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

// Updated method to check if project/client is truncated (now using character limit)
isProjectTruncated(text: string): boolean {
  return this.isTextTruncated(text);
}

// New method to toggle expand all projects for a specific column
toggleExpandAllProjects(departmentName: string): void {
  if (this.expandedColumns.has(departmentName)) {
    this.expandedColumns.delete(departmentName);
  } else {
    this.expandedColumns.add(departmentName);
  }
}



// New method to check if a specific column is expanded
isColumnExpanded(departmentName: string): boolean {
  return this.expandedColumns.has(departmentName);
}

// Modified toggle method for individual projects/clients (now handles both)
toggleProjectExpansion(text: string): void {
  if (this.expandedProjects.has(text)) {
    this.expandedProjects.delete(text);
  } else {
    this.expandedProjects.add(text);
  }
}


// Method to toggle individual client expansion (same logic as projects)
toggleClientExpansion(clientName: string): void {
  this.toggleProjectExpansion(clientName); // Reuse the same expansion logic
}

// Updated method to allow multiple department selection
updateSelectedDepartments(department: string, event: any): void {
  const isChecked = event.target.checked;

  if (isChecked) {
    // Add department if not already selected
    if (!this.selectedDepartments.includes(department)) {
      this.selectedDepartments.push(department);
    }
  } else {

    this.selectedDepartments = this.selectedDepartments.filter(dept => dept !== department);
  }

  this.getClientDepartmentChart();
}

}
 


