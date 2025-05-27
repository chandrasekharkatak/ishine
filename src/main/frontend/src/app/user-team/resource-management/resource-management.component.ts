import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, map, startWith } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { Feature } from 'src/app/models/feature';
import { Project } from 'src/app/models/project';
import { ProjectFilterDTO } from 'src/app/models/projectFilterDTO';
import { ProjectRequirements } from 'src/app/models/projectRequirements';
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

  totalEmployees = 0;
  sankhMappedEmployees :any;
  sankhMappedEmployeesList : any[] = [];
  internalMappedEmployees :any;
  internalMappedEmployeesList : any[] = [];
  mappedToBothEmployees :any;
  mappedToBothEmployeesList : any[] = [];
  employeeData : any[] = [];
  employees = ['John Doe', 'Jane Smith'];
  statuses = ['Pending', 'Approved'];
  selectedEmployee = '';
  selectedStatus = '';

  // new cards changes.....................................................................
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  data: string;
  currentUser: User;
  feature = "Resource Management";
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  modalRef1: BsModalRef = new BsModalRef();

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

  getBillableType: any;
  newMemberInProject: any;
  currentDepartment: any = []

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  filters: any = {};
  isSearchEnabled: boolean = false;
  projectColumns: any[] = [ "blank", "blank","isDraftProject", "name", "poNo","projectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "startDate", "endDate", "clientState", "createdOn", "status"];

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
  selectedEmpId: any;
  deptIdList: any[] = [];
  employeeList: Employee[] = [];
  filteredEmployees: Employee[] = [];
  employeeCtrl = new FormControl();
  searchText : any;
  searchTextDept : any;
  selectedStatusTab: string ='';
  @ViewChild('customDatePickerTemplate') 
  customDatePickerTemplate!: TemplateRef<any>;

  selectedDate: String| null = null;
  completedProjectDetails: Project = new Project();
  projectFilterDTO:ProjectFilterDTO = new ProjectFilterDTO();
  departments: any[] = [];
  totalCount: any;
  projectRequirementsList: ProjectRequirements =  new ProjectRequirements();

  showSearchInput = true;
  teamMembers: TeamMember[] = [];
  newMember: { empId: number; name: string; newBillableType: string; employeeRole: any[]; employeeTeamMappingId: any; teamId: any; isTeamLead: any; billableType: any; isShadow: boolean; };
  addMemberCtrl = new FormControl();
  searchManagerText: any;
  filteredManagerList: any[] = [];
  isAllManagersSelected: boolean = false;
  selectedRequirement: any = null;
  teamMemberCtrl = new FormControl();
  spocCtrl = new FormControl();
  employeeInformation: EmployeeInformation = new EmployeeInformation();

  constructor(
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
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
  }


  async ngOnInit(): Promise<void> {

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);
    const deptName = String(this.currentUser.departmentName).trim();
    const empRole = String(this.currentUser.employeeRole).trim();
    if(!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") && 
    !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin")&& !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")){
      await this.getAllDepartmentsFromId();
    }
    else{
      await this.getAllDepartments();
    }

    this.route.params.subscribe((params: Params) => {
      this.currentProjectId = params['id'];
    });
    //console.log(this.currentProjectId, " : this.currentProjectId");
    this.sectionViewInit();
    this.getEmployeeByNameAndEmpld();

    this.employeeCtrl.valueChanges
    .pipe(
      startWith(''),
      map(value => this.filterEmployees(value))
    )
    .subscribe(filtered => {
      this.filteredEmployees = filtered;
    });

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.toggleSearch();
    }
    // this.employee360Service.employeesFor360$.subscribe((employees) => {
    //   this.employeesFor360 = employees;
    //   console.log("Employee Data fetched by Shared service ",this.employeesFor360);
    // });
    // console.log('userMapping',this.userMapping);
    
    if(deptName === "Accounts" ){
      this.isAccounts = true;
    }
    // this.toggleSelectAllDept();
    this.projectFilterDTO.approvalStatus = "All";
    this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
    await this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);

    await this.RbacInternalProjects(this.projectFilterDTO);

    await this.RbacShankhProjects(this.projectFilterDTO);

    await this.RbacShankhInternalProjects(this.projectFilterDTO)
    
    await this.TotalEmployeeCount();
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

            // added in single list  

            // this.allProject_Po_Internal = [...this.poPortalProjectList, ...this.internalProjectList];
            // let fileteredList : any[] = [];
            // let unfilteredList : any[] = this.allProject_Po_Internal;
            // console.log(unfilteredList,"unfilteredList");
            // console.log(this.allProject_Po_Internal, " this.allProject_Po_Internal");

            
            // const deptName = String(this.currentUser.departmentName).trim();
            // const empRole = String(this.currentUser.employeeRole).trim();
            // console.log(empRole);55
            // if(!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") && !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin")&& !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")){
            //   this.allProject_Po_Internal = [];
            //   this.departments.forEach(data1=>{
            //     console.log(data1,"data1");
            //     fileteredList = unfilteredList.filter(data => {
            //       return String(data.department).includes(data1.name);
            //       });
            //       console.log(fileteredList);
            //       this.allProject_Po_Internal.push(...fileteredList);
            //       console.log(this.allProject_Po_Internal,"this.allProject_Po_Internal");
            //   })
              // this.allProject_Po_Internal = this.allProject_Po_Internal.filter(data => {
              //   console.log(data.department)
              //   return String(data.department).includes(deptName);
              //   });
                // console.log('dept name ::',deptName);
            }
            // else{

            // }
           

            // this.allProject_Po_Internal.filter(data =>{
            //   data.department.includes(this.currentUser.departmentName);
            // })
            console.log(this.internalProjectList, " this.internalProjectList");


            console.log("  allProject_Po_Internal   ", this.allProject_Po_Internal);
            console.log("this.currentUser.departmentName",this.currentUser.departmentName)
          } else {
            console.error(response.serviceResponse);
          }
        
      });
    });
  }

  clearSelection(event: Event) {
    console.log(this.isAllSelected,"this.isAllSelected");
    event.stopPropagation(); // prevent dropdown from closing
    if(this.isAllSelected == true){
      console.log(this.isAllSelected,"this.isAllSelected");
      this.toggleSelectAllDept();
    }
    else{
      this.deptIdList = [];
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      console.log(this.projectFilterDTO,"this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);
    }
    
  }

  filterDepartments() {
    const lowerText = this.searchTextDept.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }

  onDepartmentSelectionChange() {
    console.log(this.isAllSelected,"this.isAllSelected");
    if (!this.isAllSelected && this.deptIdList.length > 0 && this.deptIdList[0] != null) {
      this.projectFilterDTO.approvalStatus = "All";
    this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
    this.projectFilterDTO.departmentsids = this.deptIdList;
    console.log(this.projectFilterDTO,"this.projectFilterDTO");
    this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);

      // this.getEmployeeReportData();
    }
  }

  toggleSelectAllDept() {
    // this.employeeReportObj.deptId = [];
    console.log(this.isAllSelected,"this.isAllSelected")
    if (this.isAllSelected) {
      // Deselect all if already selected
      this.deptIdList = [];
      this.isAllSelected = false;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = [];
      console.log(this.projectFilterDTO,"this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);

    } else {
      // Select all departments
      this.deptIdList  = this.filteredDepartments.map(dept => dept.deptId);
      this.isAllSelected = true;
      this.projectFilterDTO.approvalStatus = "All";
      this.projectFilterDTO.currentUserEmpId = this.currentUser.empId
      this.projectFilterDTO.departmentsids = this.deptIdList;
      console.log(this.projectFilterDTO,"this.projectFilterDTO");
      this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);
    }

  }
  getAllDepartments(): Promise<any> {
      return new Promise((resolve, reject) => {
        this.departmentService.getAllDepartments().pipe(first()).subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.departments = response.serviceResponse;
              this.filteredDepartments = this.departments;
              console.log(this.filteredDepartments,"this.filteredDepartments");
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
              console.log(this.departments,"this.departments")
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
      this.selectedStatusTab = status;
      console.log(this.selectedStatusTab,"this.selectedStatusTab");
      this.projectFilterDTO.approvalStatus =  this.selectedStatusTab;
      this.CombinedPOInternalList(this.alertTemplate,this.projectFilterDTO);
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
    this.cancelRequest();
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
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.allTeamListCopy.forEach((teamCopy) => {
            if (teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)) {

              this.allTeamList.forEach((presentTeam, index) => {
                if (index !== teamIndex) {
                  presentTeam.teamName = '';
                  this.openAlertMod(template, "Team Name already exists!!");
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
              this.openAlertMod(template, "Team Name already exists!!");
            }
          });
        }
      });
    }
  }

  validateProjectObj(projectObj, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(projectObj.projectManager)) {
      this.alertMessage = `Please select project manager!!`
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    if (projectObj.teamList.length == 0) {
      this.alertMessage = "Please add a Team !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (projectObj.teamList.length != 0) {
      let flag = true;
      projectObj.teamList.forEach((projObj, index) => {
        projObj.teamName = projObj.teamName?.trim();
        if (!this.validationService.validateNullUndefinedEmptyString(projObj.teamName)) {
          this.alertMessage = `Please enter Team Name - ${index + 1}!!`
          flag = false;
          return;
        }
        if (!this.validationService.validateTeamName(projObj.teamName)) {
          this.alertMessage = "Please enter valid Team Name !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (projObj.departmentList == undefined || projObj.departmentList.length == 0 || projObj.departmentList == null) {
          this.alertMessage = `Please select Team's department - ${index + 1}!!`
          flag = false;
          return;
        }

        if (projObj.spoc == undefined || projObj.spoc.length == 0 || projObj.spoc == null) {
          this.alertMessage = `Please select Team's SPOC - ${index + 1}!!`
          flag = false;
          return;
        }

        if (projObj.teamMemberList == undefined || projObj.teamMemberList.length == 0 || projObj.teamMemberList == null) {
          this.alertMessage = `Please add Team Member(s) - ${index + 1}!!`
          flag = false;
          return;
        }

        if (projObj.teamMemberList.length != 0) {
          let memberFlag = true;
          projObj.teamMemberList.forEach((member) => {
            if (!this.validationService.validateNullUndefinedEmptyString(member.name)) {
              this.alertMessage = `Please select Team Member- ${index + 1}!!`
              flag = false;
              return;
            }

            if (member.isTeamLead == null && (member.isTeamLead != true || member.isTeamLead != 'true')) {
              if (member.employeeRole == undefined || member.employeeRole.length == 0 || member.employeeRole == null) {
                this.alertMessage = `Please select member(s) Employee Role - ${index + 1}!!`
                flag = false;
                //console.log(projObj.teamMemberList, " : projObj.teamMemberList");

                return;
              }
            }
          });
          if (!memberFlag) {
            this.openAlertMod(template, this.alertMessage);
            return false;
          } else {
            return true;
          }
        }
      });
      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else {
        return true;
      }
    }
  }

  createDraftProjectInfo(template: TemplateRef<any>) {
    this.cancelRequest1();

    this.allTeamList.forEach(team => {
      if (team.spoc) {
        team.spocId = team.spoc.empId;
      } else {
        console.warn(`No SPOC data available for team: ${team.teamName}`);
      }
    });
    this.projectObj.teamList = this.allTeamList;
    this.projectObj.createdBy = this.currentUser.empId;
    
    // this.projectObj.projectId=this.projectObj.id;

    // let inputValidated: boolean = this.validateProjectObj(this.projectObj, template)
    // if (!inputValidated) return;

    if (this.isHOD == true) {
      this.projectObj.isHOD = true;
      //console.log(this.projectObj, " : this.projectObj");
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showViewProjects();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    } else {
      this.projectObj.isHOD = false;
      console.log(this.projectObj, " : this.projectObj");
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          // this.showViewProjects();
          this.openAlertMod(this.alertTemplate, response.serviceResponse);

          this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              // this.cancelRequest();
              // this.openAlertMod(template, response.serviceResponse);
            } else {
              // this.cancelRequest();
              // this.openAlertMod(template, response.serviceResponse);
            }
          });
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
      });
    }
  }

  getTeamListByProjectName(project: any) {
    // this.previewTeamList = [];
    //console.log(" project    ",project);

    this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj,"projectofthisteam");
        //console.log(this.projectObj.teamList, " this.projectObj.teamList");
        this.projectObj.teamList.forEach((obj) => {
          obj.departmentList = obj.departmentList?.map(x => +x);
          this.copyDepartment = obj.departmentList;

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                member.emp360 = member.empId;
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

        this.managerList.forEach((emp) => {
          // if(emp.isConsultant == 'true'){
          //   emp.employeementId = "A-CS-".concat(emp.employeementId);
          // }else{
          //   emp.employeementId = "A-".concat(emp.employeementId);
          // }
          emp.employeementId = "A-".concat(emp.employeementId);
        });
        this.filteredManagerList = this.managerList;

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
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

    } else {
      this.alertMessage = "Please enter more than 4 letters in Project Name !!"
      this.projectObj.projectName = '';
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.projectService.checkProjectName(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.projectObj.projectName = '';
        this.openAlertMod(template, response.serviceResponse);
      }
    })
  }

  validateCreateProjectObj(projectObj: Project, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectName)) {
      this.alertMessage = "Please enter Project name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    // if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.clientId)) {
    //   this.alertMessage = "Please select a client !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectManagerId)) {
      this.alertMessage = "Please select project manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.departmentName)) {
      this.alertMessage = "Please select department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.state)) {
      this.alertMessage = "Please select state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  createProject(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateCreateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    this.projectObj.departmentList = this.projectObj.departmentName;
    this.projectObj.departmentName = null;
    this.projectObj.projectName = this.projectObj.projectName?.trim();
    this.projectObj.createdBy = this.currentUser.empId;
    //console.log("     :   ",this.projectObj);

    this.projectService.createProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewProjects();
      } else {
        this.openAlertMod(template, response.serviceResponse);
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
  openDeleteModalForTeam(template: TemplateRef<any>,alert_message: TemplateRef<any>, teamObj, project) {
    this.tempTeam = teamObj;
    this.projectdetails1 = project;

    if (!this.tempTeam.teamId) {
      this.openAlertMod(alert_message, "Team is missing. Cannot proceed.");
      this.allTeamList.pop();
      this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
      this.copyDepartment = [];
      if (!this.allTeamList || this.allTeamList.length === 0) {
        this.addInputTeamField();
      }
      return;
   }else{
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
   }
   

  }


  openDeleteModalForTeamBulk(template: TemplateRef<any>,alert_message: TemplateRef<any>, teamObj, project) {
    this.tempTeam = teamObj;
    this.projectdetails1 = project;
    console.log("tesmp",this.projectdetails1);
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
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
        resourceOverviewId: this.selectedRequirement.resourceOverviewId
      };
  
      this.allTeamMembers.push(memberToAdd);
  
      this.newteamMember = new TeamMember();
      this.addMemberCtrl.reset();
      this.selectedRequirement = null;

    }else {
      const memberToAdd = {
        ...newTeamMember,
        employeeRole: this.newteamMember.employeeRole
      };
  
      this.allTeamMembers.push(memberToAdd);
  
      this.newteamMember = new TeamMember();
      this.addMemberCtrl.reset();
      this.selectedRequirement = null;
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
        //console.log(this.teamObj.teamLeadId, " : this.teamObj.teamLeadId");

        console.log("team.teammemberList ", team.allTeamMemberList);

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

  openShowCreateForm(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  cancelRequest() {
    console.log("cancel call ");

    this.modalRef.hide();
  }

  cancelRequest1() {
    console.log("cancel call ");

    this.modalRef1.hide();
  }

  cancelRequest2() {
    console.log("cancel call ");

    this.modalRef2.hide();
  }

  previewTeamModal(template: TemplateRef<any>, teamObj: any, projectObj: any) {
    this.selectedProjectManager = '';
    this.previewTeamList = [];
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == teamObj.teamName) {
        this.previewTeamList = [team];
      }
    });
    //by priyadarshini
    // if (Object.keys(this.previewTeamList[0]).length === 0) {
    //   this.previewTeamList = [];
    // }
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
  exportToExcel(id: any): void {
    const tableId = id; // Replace with your actual table ID
    this.excelName = "TeamMemberSheet.xlsx";
    this.tableName = 'Team Members';

    this.exportExcelService.exportTableFormat(tableId, this.excelName, this.tableName);
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
                  this.alertTemplate,
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

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getExistingProjectsByUser(this.projectObj2.empId)
      }
    })

  }

  // againCall(){
  //   this.getExistingProjectsByUser(this.projectObj);
  // }

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

  EmployeeViewDataModel(template: TemplateRef<any>, catagory: string){
    if(catagory === 'Sankh'){
      this.employeeData = this.sankhMappedEmployeesList;
    }
    else if(catagory === 'Internal'){
      this.employeeData = this.internalMappedEmployeesList;
    }
    else{
      this.employeeData = this.mappedToBothEmployeesList;
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
      this.lastDate = details.endDate
        ? moment(details.endDate).format('YYYY-MM-DD')
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
 CombinedPOInternalList(template: TemplateRef<any>, projectFilterDTO:ProjectFilterDTO){
  this.allProject_Po_Internal = [];
  this.resourceManagementService.combinedPOINTERNALList(projectFilterDTO).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.allProject_Po_Internal = response.serviceResponse.combinedProjects;
      this.tabCounts = response.serviceResponse.counts;
      this.totalCount = this.tabCounts.rejectedCount + this.tabCounts.notStartedCount + this.tabCounts.approvedCount + this.tabCounts.pendingForApprovalCount

    }else {
      this.openAlertMod(template, response.serviceResponse);
    }
  });

 }

RbacInternalProjects(projectFilterDTO: ProjectFilterDTO){
  this.resourceManagementService.rbacInternalProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
     if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.internalMappedEmployeesList = response.serviceResponse;
      this.internalMappedEmployees = this.internalMappedEmployeesList.length;
      console.log("this.internalMappedEmployees",this.internalMappedEmployees)
     }
});

}

RbacShankhProjects(projectFilterDTO: ProjectFilterDTO){
  this.resourceManagementService.rbacShankhProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
     if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.sankhMappedEmployeesList = response.serviceResponse;
      this.sankhMappedEmployees = this.sankhMappedEmployeesList.length;
      console.log("this.sankhMappedEmployees",this.sankhMappedEmployees)
     }
});

}
RbacShankhInternalProjects(projectFilterDTO: ProjectFilterDTO){
  this.resourceManagementService.rbacShankhInternalProjects(projectFilterDTO).pipe(first()).subscribe((response: any) => {
     if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.mappedToBothEmployeesList = response.serviceResponse;
      this.mappedToBothEmployees = this.mappedToBothEmployeesList.length;
      console.log("this.mappedToBothEmployees",this.mappedToBothEmployees)
     }
});

}
TotalEmployeeCount(){
  this.resourceManagementService.totalEmployeeCount().pipe(first()).subscribe((response:any)=>{
    if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.totalEmployees = response.serviceResponse;
     }
  });
}

  getEmployeeByNameAndEmpld() {
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => { 
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.filteredEmployees = this.employeeList;
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
    const selectedName = event.option.value;
    const selectedEmp = this.employeeList.find(emp => emp.name === selectedName);
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      console.log('Selected Employee ID:', this.selectedEmpId);
    }
  }

  displayEmployee(emp: any): string {
    console.log("emp",emp)
    return emp ? `${emp.name}` : '';
  }  

  openEditModal(template,project){
    // console.log("Project ",project)
    this.isEditProject = true;

    this.allProjectTable = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
    this.getEmployeeByNameAndEmpld();

    this.modalRef1 = this.modalService.show(template, { class: 'custom-modal' });
    // this.modalRef1 = this.modalService.show(template, { class: 'modal-lg' });
  }
  
  openTeamMembersModal(template: any,projectObj,currentTeam) {
    this.getResourceRequirementByPoProjectId(projectObj.id);

    // projectObj.resourceRequirements.forEach(requirement => {
    //   requirement.teamMembers = this.allTeamList[0]?.teamMemberList?.filter(member => member.empId) || [];
    //   requirement.assigned = requirement.teamMembers.length;
    // });

    this.projectObj.resourceRequirements.forEach(req => {
      const assignedCount = this.teamObj.allTeamMemberList?.filter(member => member.resourceOverviewId === req.resourceOverviewId).length || 0;
      req.assigned = assignedCount;
    });
    
    this.modalRef = this.modalService.show(template, { class: 'custom-modal' });

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
        //console.log(this.teamObj.teamLeadId, " : this.teamObj.teamLeadId");

        console.log("team.teammemberList ", team.allTeamMemberList);

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
  }

 

  openDatePicker(template: TemplateRef<any>,project:any) {
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });  
    this.completedProjectDetails = project;

  }

  submitDate(template: TemplateRef<any>) {
    this.completedProjectDetails.projectCompletionDate =this.selectedDate;
    this.completedProjectDetails.projectStatus = 'Completed'
    if (!this.completedProjectDetails.projectCompletionDate) {
      this.alertMessage = "Please Select Completion Date!!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
 
    this.resourceManagementService.completionDateOfProject(this.completedProjectDetails ).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedDate ='';
        this.modalRef.hide();
        console.log('Selected Date:', response.serviceResponse);
        this.openAlertMod(template, response.serviceResponse);
      }else{
        this.selectedDate ='';
        this.modalRef.hide();
        this.openAlertMod(template, response.serviceResponse);
      }
    })
   
  }

  closeModal1() {   
    this.selectedDate ='';
    this.modalRef.hide();
  }
  closeModal(){
    this.modalRef.hide();
  }
  getResourceRequirementByPoProjectId(id) {
    this.resourceManagementService.getResourceRequirementByPoProjectId(id).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequirementsList = response.serviceResponse;
      } else {
        console.error("Error fetching project requirement list");
      }
    });
  }
  
  // addMemberRow(member) {
  //   member.teamMembers.push({ showAdd: true });
  //   member.teamMembers.forEach((tm, index) => tm.showAdd = index === member.teamMembers.length - 1);
  // }

  billabilities: string[] = ['Billable', 'Non-Billable', 'Shadow'];

  addMemberRow(requirement: any) {
    const newMember = {
      name: this.newMember.name,
      role: this.newMember.employeeRole,
      billability: this.newMember.newBillableType,
      empId: null,  // Set empId if necessary
      showAdd: true,
    };
  
    requirement.teamMembers.push(newMember);
    requirement.assigned = requirement.teamMembers.length;
  
    // Clear new member input fields
    this.newMember = {
      empId: 0, name: '', newBillableType: '', employeeRole: [],
      employeeTeamMappingId: undefined,
      teamId: undefined,
      isTeamLead: undefined,
      billableType: undefined,
      isShadow: false
    };
  }
  
  newShadow = {
    name: '',
    role: '',
    billability: ''
  };
  
  addShadowResource(member: any) {
    if (this.newShadow.name.trim() !== '') {
      const shadowResource = { ...this.newShadow };
      member.shadowResources.push(shadowResource);
  
      // Reset new shadow object
      this.newShadow = {
        name: '',
        role: '',
        billability: ''
      };
    }
  }
  
  removeShadowResource(member: any, index: number) {
    member.shadowResources.splice(index, 1);
  }  
  
  onResourceSelected(event: any,member) {
    const selectedName = event.option.value;
    const selectedEmp = this.employeeList.find(emp => emp.name === selectedName);
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      console.log('Selected Employee ID:', this.selectedEmpId);
    }
  }
  
  onTeamMemberSelected(event: MatAutocompleteSelectedEvent): void {
    const selectedEmployee = event.option.value;
    this.newteamMember.empId = selectedEmployee.empId;
    this.newteamMember.name = selectedEmployee.name;
  }

  toggleSelectAllTeams() {
    if (this.isAllSelected) {
      this.teamObj.departmentList = [];
      this.isAllSelected = false;
    } else {
      this.teamObj.departmentList = this.filteredDepartments.map(dept => dept.deptId);
      this.isAllSelected = true;
    }
  }

  filterDepartmentsTeamForm() {
    const lowerText = this.searchText.toLowerCase();
    this.filteredDepartments = this.allDeptList.filter(dept =>
      dept.name.toLowerCase().includes(lowerText)
    );
  }
  
  clearSelectionDept(event: Event) {
    event.stopPropagation();
    this.teamObj.departmentList = [];
    this.isAllSelected = false;
  }

  onSpocSelected(event: any) {
    const selectedSpoc = event.option.value;
  }

  displaySPOC(emp: any): string {
    console.log("emp",emp)
    return emp ? `${emp.name}` : '';
  }  
  
  filterManagers() {
    const lowerText = this.searchManagerText.trim().toLowerCase();
    this.filteredManagerList = this.managerList.filter(manager =>
      manager.name.toLowerCase().includes(lowerText)
    );
  }

  toggleSelectAllManagers(): void {
    if (this.isAllManagersSelected) {
      this.projectObj.projectManagerId = [];
      this.isAllManagersSelected = false;
    } else {
      this.projectObj.projectManagerId = this.filteredManagerList.map(emp => emp.empId);
      this.isAllManagersSelected = true;
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

  getEmployeeInformation(empId){
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

  
}
