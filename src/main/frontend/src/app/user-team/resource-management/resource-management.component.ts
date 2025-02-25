import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Feature } from 'src/app/models/feature';
import { Project } from 'src/app/models/project';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { DepartmentService } from 'src/app/services/department.service';
import { Team } from 'src/app/models/team';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { Department } from 'src/app/models/department';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { TeamMember } from 'src/app/models/teamMember';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ActivatedRoute, Router, Params } from '@angular/router';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectService } from 'src/app/services/project.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { SortPipe } from 'src/app/sort.pipe';

@Component({
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})
export class ResourceManagementComponent implements OnInit {

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

  getBillableType: any;
  newMemberInProject: any;
  currentDepartment: any = []

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  filters: any = {};
  isSearchEnabled: boolean = false;
  projectColumns: any[] = ["blank", "blank", "name", "projectManagerName", "clientName", "clientState", "createdOn", "status", "isDraftProject"];

  projectDetails: any = [];
  copyDepartment : any = [];
  currentBreadcrumbList: any[] = [];
  // employeesFor360: any[] = [];
  allEmployeeList360: any[] = [];

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


  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.route.params.subscribe((params: Params) => {
      this.currentProjectId = params['id'];
    });
    //console.log(this.currentProjectId, " : this.currentProjectId");
    this.sectionViewInit();

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.toggleSearch();
    }
    // this.employee360Service.employeesFor360$.subscribe((employees) => {
    //   this.employeesFor360 = employees;
    //   console.log("Employee Data fetched by Shared service ",this.employeesFor360);
    // });
    console.log('userMapping',this.userMapping);
    this.getAllEmployeeFor360View();
  }
  getAllEmployeeFor360View(){
      this.allEmployeeList360 = [];
      this.employeeService.getAllEmployeesFor360View().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList360 = response.serviceResponse;
          console.log("allEmployeeListFor360 : ", this.allEmployeeList360)
          this.allEmployeeList360.forEach(employeeObj => {
            employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
            employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            if (employeeObj.isConsultant == 'true')
              employeeObj.employeeType = 'Consultant';
            else if (employeeObj.isApprenticeship == 'true')
              employeeObj.employeeType = 'Apprentice';
            else
              employeeObj.employeeType = 'Regular';
            });
            this.allEmployeeList360 = this.allEmployeeList360;
            this.allEmployeeList360 = new SortPipe().transform(this.allEmployeeList360, ['name', 'string', 'asc']);
          } else {
            alert(response.serviceResponse);
          }
      });
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
    this.getManagerList();
    this.alreadyCreatedTeam();
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

  // getAllProjects(){
  //   fetch(this.currentUser.poPortalAllProjectApi).then(res => res.json()).then(async data => {
  //     let allPoProject = data;
  //     this.resourceManagementService.getInternalProject().pipe(first()).subscribe((response: any) => {
  //       if (response.serviceStatus == "Success") {
  //         this.internalProjectList = response.serviceResponse;

  //         let _projectList = [...allPoProject,...this.internalProjectList];

  //         if((_projectList != null || _projectList != undefined) && (this.teamCreatedProjectList != null || this.teamCreatedProjectList != undefined)){

  //           //sort according to dateTime
  //           _projectList = _projectList.sort((a, b) => (new Date(a.createdOn).getTime() < new Date(b.createdOn).getTime()) ? 1 : -1);

  //           _projectList.sort((a) => {
  //             if (a.isTeamCreated && a.isDraftProject == 'Pending For Approval') {
  //               return -1;
  //             } else {
  //               return 1;
  //             }
  //           });

  //           //console.log("AllPo projects   ::  ",allPoProject);
  //           //console.log(this.teamCreatedProjectList, " : teamCreatedProjectList");

  //           //console.log(" _projectList    ::  ",_projectList);
  //           _projectList.forEach((proj) => {

  //             if(proj.id != null){
  //               let selectedProj = this.teamCreatedProjectList.find((projTeam) => proj.id == projTeam.poProjectId);


  //               if(selectedProj){
  //                 proj.isTeamCreated = true;
  //                 proj.isDraftProject = selectedProj.isDraftProject == 'true' ? 'Pending For Approval' : selectedProj.isDraftProject == 'Rejected' ? 'Rejected' : selectedProj.isDraftProject == 'false' ? 'Approved' : "NA";
  //                 proj.createdOn = (proj.createdOn)? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //                 //console.log("proj.isDraftProject",  proj.isDraftProject);
  //               }else{
  //                 proj.isTeamCreated = false;
  //                 proj.isDraftProject = "NA";
  //                 proj.createdOn = (proj.createdOn)? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //               }
  //             }else{
  //               let selectedProj = this.teamCreatedProjectList.find((projTeam) => proj.projectId == projTeam.projectId);

  //               if(selectedProj){
  //                 proj.isTeamCreated = true;
  //                 proj.isDraftProject = proj.projectType;
  //                 proj.createdOn = (proj.createdOn)? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //               }else{
  //                 proj.isTeamCreated = false;
  //                 proj.isDraftProject = proj.projectType;
  //                 proj.createdOn = (proj.createdOn)? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //               }
  //             }

  //             if (proj.status != null) {
  //               if (proj.status == "true") {
  //                 proj.status = "InProgress";
  //               } else if (proj.status == "false") {
  //                 proj.status = "Completed";
  //               } else {
  //                 proj.status = proj.status;
  //               }
  //             } else {
  //               return null;
  //             }

  //           });

  //         }

  //         this.allProjectList = _projectList;
  //         //console.log(this.allProjectList, " all projects");

  //         // Navigate to Project when used link

  //         if(this.currentProjectId != undefined || this.currentProjectId != null){
  //           this.allProjectList.forEach((proj) => {
  //             if(proj.id == this.currentProjectId){
  //               proj.isEditProject = true;
  //               this.showEditProjectForm(proj);
  //             }
  //           })
  //         }
  //         //console.log(this.internalProjectList, " this.internalProjectList");
  //       } else {
  //         console.error(response.serviceResponse);
  //       }
  //     });
  //   });
  // }

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
              console.log("Priyadarshini",proj.projectId," ",proj.projectName);

              if (selectedProj) {
                proj.isTeamCreated = true;
                proj.isDraftProject = selectedProj.isDraftProject == 'true' ? 'Pending For Approval' : selectedProj.isDraftProject == 'Rejected' ? 'Rejected' : selectedProj.isDraftProject == 'false' ? 'Approved' : "NA";
                // proj.isDraftProject = proj.projectType;
                proj.createdOn = (proj.createdOn) ? moment(proj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
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

            this.allProject_Po_Internal = [...this.poPortalProjectList, ...this.internalProjectList];

            for(let y of this.allProject_Po_Internal){
              let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === y.projectManager);
              console.log('matches++',matchingEmployee);
              y.emp360 = matchingEmployee ? matchingEmployee : {};
            }

            //console.log(_projectList, " all projects");
            console.log(this.internalProjectList, " this.internalProjectList");

            console.error("  allProject_Po_Internal   ", this.allProject_Po_Internal);
          } else {
            console.error(response.serviceResponse);
          }
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
        //console.log("allDeptList : ", this.allDeptList)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // Team

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
        this.updateEmployeeListAccordingToTeamMembers();
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
    this.cancelRequest1();
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
    this.cancelRequest();

    this.projectObj.teamList = this.allTeamList;
    this.projectObj.createdBy = this.currentUser.empId;
    // this.projectObj.projectId=this.projectObj.id;

    let inputValidated: boolean = this.validateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    if (this.isHOD == true) {
      this.projectObj.isHOD = true;
      //console.log(this.projectObj, " : this.projectObj");
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.showViewProjects();
          this.openAlertMod(template, response.serviceResponse);
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
          // this.openAlertMod(template, response.serviceResponse);

          // this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((response: any) => {
          //   if (response.serviceStatus == "Success") {
          //     this.cancelRequest();
          //     this.openAlertMod(template, response.serviceResponse);
          //   } else {
          //     this.cancelRequest();
          //     this.openAlertMod(template, response.serviceResponse);
          //   }
          // });
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
        //console.log(this.projectObj.teamList, " this.projectObj.teamList");
        this.projectObj.teamList.forEach((obj) => {
          obj.departmentList = obj.departmentList?.map(x => +x);
          console.log(" obj.departmentList     ",obj.departmentList);
          this.copyDepartment = obj.departmentList;

          // obj.teamMemberList.forEach((member) => {
          //   //console.log(" teamMemberList    ",obj.teamMemberList);
          //   member.startDate = (member.startDate) ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
          // });

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                let matchingEmployee = this.allEmployeeList360.find(emp => emp.empId === member.empId);
                console.log('matches++',matchingEmployee);
                member.emp360 = matchingEmployee ? matchingEmployee : {};
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
  }



  onApproveProject(project: any) {
    project.empId = this.currentUser.empId;
    // project.projectId = this.projectObj.projectId;
    console.log("this.projectObj.projectId   ",this.projectObj.projectId);
    console.log("this.projectObj.projectId   ",project.projectId);
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

  // removeInputTeamField(teamObj) {
  //   console.log("teamObj anurag   ",teamObj);
  //   this.allTeamList.forEach((value, index) => {
  //     if (value == teamObj) {
  //       this.updatedTeamList.push(value);
  //       this.allTeamList.splice(index, 1);
  //       this.allTeamListCopy.splice(index, 1);
  //     }
  //   });
  // }

  tempTeam = new Project();

  openDeleteModalForTeam(template: TemplateRef<any>, teamObj) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.tempTeam = teamObj;
  }

  deleteTeam(template: TemplateRef<any>) {
    console.log("delete team method call ", this.tempTeam);
    this.projectService.deleteTeam(this.tempTeam).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })




  }

  tempArray: any[] = [];


  addTeamMember() {
    const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);
    console.log("newteamMember  ", newTeamMember)
    if (newTeamMember) {
      newTeamMember.employeeRole = this.newteamMember.employeeRole;

      this.allTeamMembers.push(newTeamMember);
      //console.log("New member ===== ::  ",newTeamMember);

    }
    // this.tempArrays =  Object.assign({},this.allTe)
    this.newteamMember = new TeamMember();
  }

  removeTeamMember(teamMember, index) {
    const currentTeam = this.currentTeam;
    this.allTeamList?.forEach((team) => {
      if (team.teamName == currentTeam.teamName) {
        team.teamMemberList.splice(index, 1);
        //console.log(team.teamMemberList, " : team.teamMemberList");

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
    //console.log("Updated Members : ", this.allTeamMembers);
  }

  // Modals

  openTeamMemberModal(template: TemplateRef<any>, currentTeam) {
    this.allTeamMembers = [];
    this.teamObj.teamLeadId = '';
    this.newteamMember.empId = '';
    this.newteamMember.employeeRole = null;
    // this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    // this.getAllEmployeesByRole(currentTeam.departmentList);
    console.log("this.copyDepartment ",this.copyDepartment);
    this.getAllEmployeesByDepartmentIds(this.copyDepartment);
    this.getAllEmployeesByRole(this.copyDepartment);
    this.allTeamList?.forEach((team: any) => {
      if (team.teamName == currentTeam.teamName) {
        let teamLeadObj = team.teamMemberList?.filter(x => x.isTeamLead == true || x.isTeamLead == "true");
        if (teamLeadObj) {
          this.teamObj.teamLeadId = teamLeadObj[0]?.empId;
        }
        //console.log(teamLeadObj, " :teamLeadObj");
        //console.log(this.teamObj.teamLeadId, " : this.teamObj.teamLeadId");

        console.log("team.teammemberList ",team.allTeamMemberList);

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

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
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

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
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

  // Excel Export

  exportToExcel() {
    this.excelName = 'Projects.xlsx';

    const onlySpecificDataArr = this.allProjectList.map(
      x => ({
        "Project Name": x.name,
        "Project Manager": x.projectManagerName,
        "Client Name": x.clientName,
        "Client State": x.clientState,
        "Created On": x.createdOn,
        "Approval Status": x.isDraftProject,
        "Project Status": x.status
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
  }


  // check resource template

  getExistingProjectsByUser(employee) {
    console.log("employee details in resoiurce mapping ", employee);

    let projectObj = new Project();

    projectObj.empId = employee;

    // getExistingProjectsAndTeamsByEmployee service impl
    this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectDetails = response.serviceResponse;
        console.log("this.projectDetails ", this.projectDetails);

        console.log("Existing project detauls fetched for employee",this.projectDetails);
        if (this.projectDetails.length > 0) {
          if (this.projectDetails[0].billableType == "TNM") {
            this.openAlertMod(this.alertTemplate, "This Employee is already mapped to TNM project. Can't add to another project or Team !!");
            this.getBillableType = this.projectDetails.find(employee => this.newteamMember.billableType = employee.billableType);
          } else {
            this.newMemberInProject = "NewMember";
            this.newteamMember.billableType = this.newMemberInProject;
          }
        } else {
          this.newMemberInProject = "NewMember";
          this.newteamMember.billableType = this.newMemberInProject;
        }

        console.log("this.projectDetails ", this.projectDetails);
        console.log("this.getBillableType ", this.getBillableType);
        console.log(" newTeamMember   details   ", this.newteamMember)


      }
    })


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

  openProjectTemplateModal(template: TemplateRef<any>, employee) {
    this.modalRef2 = this.modalService.show(template, { class: 'modal-xl' });
    this.getExistingProjectsByUser(employee.empId);
    this.dataObj = employee;

    console.log("data employee newmenbfcg  ", employee)
  }


changeDepartment(event: any): void {
    const selectedDepartmentIds = event.value;
console.log("this.copyDepartment ",this.copyDepartment);
    // Identify deselected departments by finding the difference between the previous and current selections
    const deselectedDepartmentIds = this.copyDepartment.filter(
        id => !selectedDepartmentIds.includes(id)
    );

    console.log("deselectedDepartmentIds    ",deselectedDepartmentIds)

    // Update the current department selection
    this.copyDepartment = selectedDepartmentIds;

    // If there are any deselected departments, show the alert
    if (deselectedDepartmentIds.length > 0) {
        this.allTeamList.forEach(team => {
            if (team.teamMemberList === '' || team.teamMemberList === null || team.teamMemberList === undefined) {
                return;
            } else {
                this.openAlertMod(this.alertTemplate,"You have deselected a department! Wish to add it again, please ensure that the team members for that department are added as well.");

                const departmentList = team.departmentList;
                console.log("team.teamMemberList  ",team.teamMemberList)
                console.log("departmentList     ",departmentList);
                
                  // Use strict equality check and type casting if necessary
                  const filteredTeamMemberList = team.teamMemberList.filter(member => {
                    const memberDeptId = String(member.departmentId); // Convert to string for comparison
                    const isMatch = departmentList.some(deptId => String(deptId) === memberDeptId);
                    console.log(`Checking if department ID ${memberDeptId} is in departmentList:`, isMatch);
                    return isMatch;
                });
                console.log("filteredTeamMemberList    ",filteredTeamMemberList)
                team.teamMemberList = filteredTeamMemberList;
                console.log(team.teamMemberList);
            }
        });
    }

    // Update the previousDepartmentIds with the current selection for future comparison
    this.copyDepartment = selectedDepartmentIds;
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


}
