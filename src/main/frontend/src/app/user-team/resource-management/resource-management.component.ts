import { ViewportScroller } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, map, startWith } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { employeeReport } from 'src/app/models/employeeReport';
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
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { environment } from 'src/environments/environment';
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

  topStats = [
    { value: 96, label: "Ishine’s Billable", icon: "fa-users", iconColor: "#5E35B1", borderColor: "#5E35B1" },
    { value: 104, label: "Shankh’s Billable", icon: "fa-users", iconColor: "#FFB300", borderColor: "#FFB300" },
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

  showReportList = false;
  reportListUrlSafe: SafeResourceUrl;

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
  employeesWithoutBillability: any[] =[];
  employeesWithoutBillable:any;
  employeeData: any[] = [];
  catagory: any;
  employees = ['John Doe', 'Jane Smith'];
  statuses = ['Pending', 'Approved'];
  selectedEmployee = '';
  selectedStatus = '';

   summaryModalRef: BsModalRef;
  projectSummaryData: any[] = [];

  // new cards changes.....................................................................
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  @ViewChild("alert_message_sync")
  alertModalSync: TemplateRef<any>;

  @ViewChild("alert_message_without_reload")
  alertTemplateWithoutReload: TemplateRef<any>;

  data: string;
  currentUser: User;
  feature = "Resource Management";
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  alertMessage: any;
  alert_Message: any;
  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  modalRef1: BsModalRef = new BsModalRef();
  modalRef3: BsModalRef = new BsModalRef();
  modalRef4: BsModalRef = new BsModalRef();
  modalRef5: BsModalRef = new BsModalRef();
  modalRefTeamMember: BsModalRef = new BsModalRef();

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
  projectColumns: any[] = ["blank", "draftStatus", "name", "poNo", "projectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "poStartDate", "poEndDate", "state", "createdOn", "status"];


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
  departmentsList:any[] = [];

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
  dept:any;
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
  searchTextDeptInternal:any;
  searchTextDeptTeam:any;
  isAllDeptSelected: boolean = false;

  constructor(
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
  ) {

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const reportUrl = `${environment.baseUrl360}#/user-reports/report-list`;
    this.reportListUrlSafe = this.sanitizer.bypassSecurityTrustResourceUrl(reportUrl);

  }


  async ngOnInit(): Promise<void> {

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log("lalalalalalalalal", this.userMapping);
    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();
    if (!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") &&
      !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin") && !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")) {
      await this.getAllDepartmentsFromId();
      // this.projectFilterDTO.isHod = true
    }
    else {
      await this.getAllDepartments();
      if(this.filteredDepartments != null)
      this.projectFilterDTO.isAdmin = true;
      else{
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
    await this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);

    await this.RbacInternalProjects(this.projectFilterDTO);

    await this.getEmployeesWithoutBillability(this.projectFilterDTO);

    await this.RbacShankhProjects(this.projectFilterDTO);

    await this.RbacAllShankhInternalProjects(this.projectFilterDTO);
    await this.ExceptionEmployeeReport(this.projectFilterDTO);
    await this.RbacBothShankhInternal(this.projectFilterDTO);
    await this.ProjectLessEmployees(this.projectFilterDTO);
    await this.getProjectDetailsForBulkDefaultUpdate();
    await this.TotalEmployeeCount();
    await this.getBenchEmployeeMoreThan30Days(this.projectFilterDTO);
    // const deptName = String(this.currentUser.departmentName).trim();
    // const empRole = String(this.currentUser.employeeRole).trim();
    // if(!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") && 
    // !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin")&& !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")){
    //   await this.getAllDepartmentsFromId();
    // }
    // else{
    //   await this.getAllDepartments();
    // }
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
    console.log(this.isAllSelected, "this.isAllSelected");
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
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }

  }

  filterDepartments() {
    const lowerText = this.searchTextDept.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  onDepartmentSelectionChange() {
    console.log(this.isAllSelected, "this.isAllSelected");
    if (!this.isAllSelected && this.deptIdList.length > 0 && this.deptIdList[0] != null) {
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);

      // this.getEmployeeReportData();
    }
  }

  onDepartmentSelectionChange1() {
    console.log(this.isAllSelected, "this.isAllSelected");
    if (!this.isAllSelected && this.projectObj.departmentList.length > 0 && this.projectObj.departmentList[0] != null) {
        this.projectFilterDTO.approvalStatus = "All";
        this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
        this.projectFilterDTO.departmentsids = this.projectObj.departmentList;
        console.log(this.projectFilterDTO, "this.projectFilterDTO");
        this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }
}

 onDepartmentSelectionChange2() {
    console.log(this.isAllSelected, "this.isAllSelected");
    if (!this.isAllSelected && this.teamObj.departmentList.length > 0 && this.teamObj.departmentList[0] != null) {
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.teamObj.departmentList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);

    }
}

  toggleSelectAllDept() {
    // this.employeeReportObj.deptId = [];
    console.log(this.isAllSelected, "this.isAllSelected")
    if (this.isAllSelected) {
      // Deselect all if already selected
      this.deptIdList = [];
      this.isAllSelected = false;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);

    } else {
      // Select all departments
      this.deptIdList = this.filteredDepartments.map(dept => dept.deptId);
      this.isAllSelected = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }

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
        this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    } else {
        // Select all departments
        this.projectObj.departmentList = this.filteredDepartments.map(dept => dept.deptId);
        this.isAllSelected = true;
        this.projectFilterDTO.approvalStatus = "All";
        this.projectFilterDTO.currentUserEmpId = this.currentUser.empId;
        this.projectFilterDTO.departmentsids = this.projectObj.departmentList;
        console.log(this.projectFilterDTO, "this.projectFilterDTO");
        this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
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
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);

    } else {
      // Select all departments
      this.teamObj.departmentList = this.filteredDepartmentsTeam.map(dept => dept.deptId);
      this.isAllSelected = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.teamObj.departmentList;
      console.log(this.projectFilterDTO, "this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
    }

  }

  
  getAllDepartments(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getAllDepartments().pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.departments = response.serviceResponse;
            this.departmentsList = [...this.departments];
            this.filteredDepartments = this.departments;
            this.filteredDepartmentsTeam = [...this.departmentsList];
            // console.log(this.filteredDepartments, "this.filteredDepartments");
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
    const documentHeight = document.body.scrollHeight;
    this.scroller.scrollToPosition([0, documentHeight]);
    this.selectedStatusTab = status;
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
    else {
      this.projectFilterDTO.completionStatus = null;
    }
    console.log(this.projectFilterDTO)
    this.CombinedPOInternalList(this.alertTemplate, this.projectFilterDTO);
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
        //console.log("allDeptList : ", this.allDeptList)
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
          this.openAlertMod3(template, response.serviceResponse);
        } else {
          this.allTeamListCopy.forEach((teamCopy) => {
            if (teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)) {

              this.allTeamList.forEach((presentTeam, index) => {
                if (index !== teamIndex) {
                  presentTeam.teamName = '';
                  this.openAlertMod3(template, "Team Name already exists!!");
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
              this.openAlertMod3(template, "Team Name already exists!!");
            }
          });
        }
      });
    }
  }

  validateProjectObj(projectObj, template: TemplateRef<any>) {
    if (projectObj.projectManagerId == undefined || projectObj.projectManagerId.length == 0 || projectObj.projectManagerId == null) {
      this.alertMessage = `Please select atleast one project manager.`
      this.openAlertMod3(template, this.alertMessage);
      return;
    }

    if (projectObj.teamList.length == 0) {
      this.alertMessage = "Please add atleast one team."
      this.openAlertMod3(template, this.alertMessage);
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
          this.openAlertMod3(template, this.alertMessage);
          return false;
        }

        if (projObj.departmentList == undefined || projObj.departmentList.length == 0 || projObj.departmentList == null) {
          this.alertMessage = `Please select Team's department - ${index + 1}.`
          flag = false;
          return;
        }

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
            this.openAlertMod3(template, this.alertMessage);
            return false;
          } else {
            return true;
          }
        }
      });
      if (!flag) {
        this.openAlertMod3(template, this.alertMessage);
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
          console.warn(`No SPOC data available for team: ${team.teamName}`);
        }
        if (team.teamMemberList) {
          team.teamMemberList.forEach(member => {
            if (member.shadow && member.shadow.empId) {
              member.shadowEmpId = member.shadow.empId;
              console.log("shadowEmpId set", member.shadowEmpId)
            } else {
              member.shadowEmpId = null;
              console.warn(`No valid shadow data for member: ${member.empId || 'Unknown ID'} in team: ${team.teamName}`);
            }
          });
        }

      });
      this.projectObj.teamList = this.allTeamList;
      this.projectObj.createdBy = this.currentUser.empId;
      
      if(this.projectObj.poProjectType == null){
        this.projectObj.projectType = "Internal";
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
          
          } else {
            this.openAlertMod3(template2, response.serviceResponse);
          }
        });
      } else {
        this.projectObj.isHOD = false;
        console.log(this.projectObj, " : this.projectObj");
        this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(this.alertTemplate, response.serviceResponse);
            this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((response: any) => {
              if (response.serviceStatus == "Success") {
                // this.cancelRequest();
                // this.openAlertMod(template, response.serviceResponse);
              } else {
                // this.cancelRequest();
                // this.openAlertMod(template2, response.serviceResponse);
              }
            });
          } else {
            this.openAlertMod3(template2, response.serviceResponse);
          }
        });
      }
    }
    else {
      this.openAlertMod3(template2, "There are currently no teams to be set...!");
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
        this.openAlertMod3(template, this.alertMessage);
        return false;
      }

    } else {
      this.alertMessage = "Please enter more than 4 letters in Project Name !!"
      this.projectObj.projectName = '';
      this.openAlertMod3(template, this.alertMessage);
      return false;
    }
    this.projectService.checkProjectName(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.projectObj.projectName = '';
        this.openAlertMod3(template, response.serviceResponse);
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
    console

    if (!this.tempTeam.teamId) {
      this.openAlertMod(alert_message, "Team is missing. Cannot proceed.");
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

  addTeamMember() {
    const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);

    if (newTeamMember && this.selectedRequirement) {
      const memberToAdd = {
        ...newTeamMember,
        employeeRole: this.newteamMember.employeeRole,
        resourceOverviewId: this.selectedRequirement.resourceOverviewId,
        isShadow: this.newteamMember.isShadow,
        isDefaultProject: this.newteamMember.isDefaultProject
      };

      this.allTeamMembers.push(memberToAdd);

      this.newteamMember = new TeamMember();
      this.addMemberCtrl.reset();
      this.selectedRequirement = null;
      this.teamMemberCtrl.reset();

    } else {
      const memberToAdd = {
        ...newTeamMember,
        employeeRole: this.newteamMember.employeeRole,
        isShadow: this.newteamMember.isShadow,
        isDefaultProject: this.newteamMember.isDefaultProject
      };

      this.allTeamMembers.push(memberToAdd);

      this.newteamMember = new TeamMember();
      this.addMemberCtrl.reset();
      this.selectedRequirement = null;
      this.teamMemberCtrl.reset();
    }
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

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember) {
        this.allTeamMembers.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });

  }

  openTeamMemberModal(template: TemplateRef<any>, currentTeam) {
    this.allTeamMembers = [];
    this.teamObj.teamLeadId = '';
    this.newteamMember.empId = '';
    this.newteamMember.employeeRole = null;
    // this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    // this.getAllEmployeesByRole(currentTeam.departmentList);
    console.log("this.copyDepartment ", currentTeam);
    // this.getAllEmployeesByDepartmentIds(this.copyDepartment);
    this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
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
    console.log("cancel call ");

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

    this.modalRef = this.modalService.show(template, { class: 'custom-modal' });
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

    this.modalRef = this.modalService.show(template, { class: 'custom-modal' });
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

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
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
        'Approval Status': x.status ,
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
                this.openAlertMod(
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
    }
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }




  deleteResourceModal(template: TemplateRef<any>, teamId) {
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj2 = teamId;

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
    this.resourceManagementService.combinedPOINTERNALList(projectFilterDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.allProject_Po_Internal = response.serviceResponse.combinedNewProjects;
        this.tabCounts = response.serviceResponse.counts;
        this.totalCount = this.tabCounts.rejectedCount + this.tabCounts.notStartedCount + this.tabCounts.approvedCount + this.tabCounts.pendingForApprovalCount

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

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
        this.employeesWithoutBillable= this.employeesWithoutBillability.length;
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

  isEmployeeInList(list: any[]): boolean {
    return list?.some(emp => emp.empId === this.selectedEmpId);
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
    console.log("================================",resourceManagementDTO);
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
      );
  }

  renderColumnChart(chartName: any, chartId: any, chartData: any, categories: any) {
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
                text: 'Total Timesheets Filled',
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
            enabled: false // Not needed for a single series chart
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
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
    this.getEmployeeByNameAndEmpld();

    this.modalRef1 = this.modalService.show(template, { class: 'custom-modal' });
    // this.modalRef1 = this.modalService.show(template, { class: 'modal-lg' });
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
    // this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    // this.getAllEmployeesByRole(currentTeam.departmentList);
    console.log("this.copyDepartment ", this.copyDepartment);
    // this.getAllEmployeesByDepartmentIds(this.copyDepartment);
    this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
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
        this.modalRef = this.modalService.show(this.previousDefaultProject, { class: 'custom-modal' });
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
      console.log("test",this.projectObj.departmentList);
      
    } else {
      this.projectObj.departmentList = this.filteredDepartmentsInternal.map(dept => dept.deptId);
      this.isAllSelected = true;
      console.log("test2",this.projectObj.departmentList);
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
    console.log("test", this.slectedMemberFromResourceRequirement);
    const selectedEntries = selectedMembers.map(m => ({
      empId: m.empId,
      teamId: this.teamObj.teamId
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
          this.openAlertMod(template, response.serviceResponse);


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
  getTeamListForSelectedProject1(emp) {
    let selectedProjectId = emp.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }
  filterTeamsForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
  }

  filterResourceRequirementForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.resourceRequirementListBulk = this.teamListBulk.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
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

}
