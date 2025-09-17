import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { saveAs } from 'file-saver';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { GetProjectDetailsForBulkDefaultUpdate } from 'src/app/models/getProjectDetailsForBulkDefaultUpdate';
import { Project } from 'src/app/models/project';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { TeamMember } from 'src/app/models/teamMember';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { EncryptionService } from 'src/app/services/EncryptionService';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { ResourceManagementComponent } from 'src/app/user-team/resource-management/resource-management.component';
import * as XLSX from 'xlsx';
@Component({
  selector: 'app-employee360-project',
  templateUrl: './employee360-project.component.html',
  styleUrls: ['./employee360-project.component.css']
})
export class Employee360ProjectComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  isEditProject: boolean = false;
  isHideButton: boolean = false;
  //added by rahul
  flag: boolean = false;
  isProjectVisible: boolean = true;
  isProjectTeamVisible: boolean = false;
  isProjectTeamMemberVisible: boolean = false;
  projectListBulk: GetProjectDetailsForBulkDefaultUpdate = new GetProjectDetailsForBulkDefaultUpdate();
  employeeData: any;
  getBillableType: any;
  newMemberInProject: any;
  lastDate: any;
  startDate: any;
  endDate: any;
  page = 1;
  copyDepartment: any = [];
  currentBreadcrumbList: any[] = [];
  allProjectList: any[] = [];
  //Rahul Singh
  filterProjectByProjectId: any[] = [];
  filterTeamfromTeamId: any;
  //end 
  filteredDeptList: any[] = [];
  allDeptList: any[] = [];
  allTeamList: any[] = [];
  previewTeamList: any[] = [];
  currentUser: User;
  filters: any = {};
  isSearchEnabled: boolean = false;
  projectColumns: any[] = ['blank', 'projectName', 'teamName', 'clientName', 'billableType', 'startDate', 'updatedOn', 'poStartDate', 'poEndDate', 'status'];
  employeesColumns: any[] = ['blank', 'teamName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];
  teamColumns: any[] = ['blank','employmentIdAcToET','name','teamName','teamLeadName']
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  newteamMember: TeamMember = new TeamMember();

  projectObj: Project = new Project();
  projectEditObj: Project = new Project();
  selectedOtherProjectId: any;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  abbreviationError: string = '';
  @ViewChild(ResourceManagementComponent) resourceManagementComponent: ResourceManagementComponent;


  constructor(
    private breadcrumbService: BreadcrumbService,
    private projectService: ProjectService,
    private modalService: BsModalService,
    private router: Router,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private departmentService: DepartmentService,
    private resourceManagementService: ResourceManagementService,
    private employeeService: EmployeeService,
    private emp360Service: Employee360Service,
    private encryptionService: EncryptionService,
  ) {

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
  }

  ngOnInit(): void {

    let encryptedEmployeeData = sessionStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
  const storedData = employeeData;
    const parsedData = storedData ? JSON.parse(storedData) : null;
    if (parsedData != null || parsedData != undefined) {
      this.employeeData = parsedData;
    } else {
      this.employeeData = history.state.data;
    }

    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Project");
    if (findbreadcrumbObject >= 0) {
      this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Project";
      breadcrumbObject.url = "/employee-360/project";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }
    this.getExistingProjectsByUser();
  }
  clearBreadcrumbs() {
    // this.breadcrumbService.setBreadcrumbSubject(null);
    window.location.reload()
  }
  //added by rahul singh
  backfromvisibility(type: any) {
    this.isProjectVisible = false;
    this.isProjectTeamVisible = false;
    this.isProjectTeamMemberVisible = false;
    this.getExistingProjectsByUser();
    if (type == "ProjectVisible" && this.flag == true) {
      this.isProjectVisible = true;
      this.flag = false;
      this.filterProjectByProjectId = [];

    }
    else if (type == "ProjectTeamVisible") {
      this.isProjectTeamVisible = true;
    }
    else if (type == "ProjectTeamMemberVisible" && this.flag == false) {
      this.isProjectVisible = true;
      this.isProjectTeamVisible = false;
    } else {
      this.isProjectTeamVisible = true;
      this.isProjectVisible = false;
    }

  }
  redirecttoProjectTeam(id: any) {
    this.isProjectVisible = true;
    this.isProjectTeamVisible = false;
    this.isProjectTeamMemberVisible = false;
    this.flag = true;

    this.filterProjects(id);
  }
  redirecttoTeam(id: any, projectId: any) {
    this.isProjectTeamMemberVisible = true;
    this.isProjectVisible = false;
    this.isProjectTeamVisible = false;
    this.getTeamEmployeeByTeamId(id);
    this.filterProjects(projectId);


  }

  projectteamInfo: Project = new Project();

  getTeamByProjectId(projectId: any) {

    this.projectteamInfo.projectId = projectId;

    this.emp360Service.getTeamInfo(this.projectteamInfo).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.filterProjectByProjectId = response.serviceResponse;
           console.log("getTeamInfo ", this.filterProjectByProjectId);

          const groupedData = {};

          this.filterProjectByProjectId.forEach((member) => {
            const teamKey = member.teamId;

            if (!groupedData[teamKey]) {
              groupedData[teamKey] = {
                teamId: member.teamId,
                teamName: member.teamName,
                employees: [],
                projectId: member.projectId,
                projectName: member.projectName
              };
            }

            groupedData[teamKey].employees.push({
              empId: member.empId,
              employeeName: member.employeeName,
              employeeRole: member.employeeRole ? member.employeeRole.split(',').filter(role => role.trim() !== '').join(', ') : "",
              startDate: member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null,
              billableType: member.billableType,
              active: member.active,
              employeeTeamMapId:member.employeeTeamMapId,
              emp360: {}
            });

            groupedData[teamKey].employees = groupedData[teamKey].employees || [];
          });

          this.filterProjectByProjectId = Object.values(groupedData);

          this.filterProjectByProjectId.forEach((team) => {
            team.employees.forEach((employee) => {
              employee.emp360 = employee.empId;
            });
          });
          // console.log("Formatted Team Data: ", this.teamMemberList);
        } else {
          console.warn("Failed to fetch team info");
        }
      }
    });
  }
  filterProjects(id) {
    this.getTeamByProjectId(id);

  }
  filterTeamMemberProjects(id) {
    this.filterProjectByProjectId = this.allProjectList.filter(project =>
      project.projectId == id
    );
  }

  exportToExcel(id: any): void {
    let exportToExcelTeamfile = id + ".xlsx";
    const table = document.getElementById('' + id); // Get table by ID
    if (!table) {
      console.error('Table not found');
      return;
    }

    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table); // Convert table to worksheet
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Project Data');

    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });

    saveAs(data, exportToExcelTeamfile);
  }
  async getTeamEmployeeByTeamId(teamId: any) {
    try {
      const response: any = await this.projectService.getTeamMemberByTeamId(teamId).pipe(first()).toPromise();

      if (response.serviceStatus === 'Success') {
        this.filterTeamfromTeamId = response.serviceResponse;

        this.filterTeamfromTeamId.forEach((employee) => {
          employee.emp360 = employee.empId;
          employee.emp360teamLeadId = employee.teamLeadId;
        });
        // console.log('filterTeamfromTeamId = ',this.filterTeamfromTeamId);

      } else {
        // Handle failure case, if needed
        console.log('Service failed:', response);
      }
    } catch (error) {
      // Handle error case
      console.error('Error fetching team members:', error);
    }
  }
  //end

  showEditProjectForm(project: any) {
    this.isEditProject = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
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

async getExistingProjectsByUser() {
  let projectObj = new Project();
  projectObj.empId = this.employeeData.empId;
  projectObj.isAllProj = true;

  // getExistingProjectsAndTeamsByEmployee service impl
  this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.allProjectList = response.serviceResponse;

      // Add combined project type to each project in the list
      this.allProjectList = this.allProjectList.map((project: any) => {
        project.combinedProjectType = this.getProjectType(project);
        return project;
      });

      console.log("dekhauchi re project details ::::::::", this.allProjectList);
      console.log("this.projectDetails ", this.allProjectList);
      console.log("Existing project detauls fetched for employee", this.allProjectList);

      // if (this.allProjectList.length > 0) {
      //   if (this.allProjectList[0].billableType == "TNM") {
      //     this.openAlertMod(this.alertTemplate, "This Employee is already mapped to TNM project. Can't add to another project or Team !!");
      //     this.getBillableType = this.allProjectList.find(employee => this.newteamMember.billableType = employee.billableType);
      //   } else {
      //     this.newMemberInProject = "NewMember";
      //     this.newteamMember.billableType = this.newMemberInProject;
      //   }
      // } else {
      //   this.newMemberInProject = "NewMember";
      //   this.newteamMember.billableType = this.newMemberInProject;
      // }

      console.log("this.allProjectList ", this.allProjectList);
      console.log("this.getBillableType ", this.getBillableType);
      console.log(" newTeamMember   details   ", this.newteamMember)
    }
  });
}


  deleteResourceModal1(template: TemplateRef<any>, projObj, member) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
    this.projectObj.empId = member.empId;
  
  }



  deleteResourceFromProject(template: TemplateRef<any>) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.lastDate;
    projectObj.employeeTeamMapId = this.employeeTeamMapId;

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.lastDate = '';
        this.lastDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
        this.getTeamByProjectId(this.projectObj.projectId);
      }
    })
  }

  editProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project Edit - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: {} });

    this.resourceManagementComponent.showEditProjectForm(projectObj);



    this.resourceManagementComponent.showEditProjectForm(projectObj);


  }

  viewProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project View - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.router.navigate([breadcrumbObject.url], { queryParams: {} });
  }

  getAllDepartmentList(project: any) {
    // this.allDeptList = [];

    // this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.allDeptList = response.serviceResponse;
    //     this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
    //     //console.log("allDeptList : ", this.allDeptList)
    //   } else {
    //     console.error(response.serviceResponse);
    //   }
    // });
  }

  getTeamListByProjectName(project: any) {
    //   // this.previewTeamList = [];
    //   //console.log(" project    ",project);

    //   this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
    //     if (response.serviceStatus == "Success") {
    //       this.projectObj.teamList = response.serviceResponse;
    //       //console.log(this.projectObj.teamList, " this.projectObj.teamList");
    //       this.projectObj.teamList.forEach((obj) => {
    //         obj.departmentList = obj.departmentList?.map(x => +x);
    //         console.log(" obj.departmentList     ",obj.departmentList);
    //         this.copyDepartment = obj.departmentList;

    //         if (obj.teamMemberList) {
    //           obj.teamMemberList.forEach((member) => {
    //             if (member) { // Check if member is not null
    //               // Format the startDate if it exists, otherwise set it to null
    //               member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
    //             }
    //           });
    //         } else {
    //           console.warn('teamMemberList is null or undefined');
    //         }
    //       });
    //       //console.log(this.projectObj.teamList, " this.projectObj.teamList");
    //       this.previewTeamList = this.projectObj.teamList;

    //       //console.log(" length of previewTeamList  ",this.previewTeamList.length);
    //       //Project Team List
    //       if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
    //         this.addInputTeamField();
    //       } else {
    //         //console.log(" find error in else part ")
    //         this.allTeamList = this.projectObj.teamList;
    //         // this.allTeamListCopy = this.projectObj.teamList;
    //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
    //       }

    //     } else {
    //       console.error(response.serviceResponse);

    //       //Project Team List
    //       if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
    //         this.addInputTeamField();
    //       } else {
    //         this.allTeamList = this.projectObj.teamList;
    //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
    //       }
    //     }
    //   });
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
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


  handlePageChange(event) {
    this.page = event;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  editStartdateModal(template: TemplateRef<any>, projObj) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
  }

  editEnddateModal(template: TemplateRef<any>, projObj) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
  }


  editStartdate(template: TemplateRef<any>) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.startDate = this.startDate;


    console.log("team details ", projectObj)
    projectObj.updatedBy = this.currentUser.empId;
    this.projectService.updateProjectStartAndEndDate(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.startDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
        // this.getTeamByProjectId(this.projectObj.projectId);
      }
    })
  }


  editEnddate(template: TemplateRef<any>) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.endDate;


    console.log("team details ", projectObj)
    this.projectService.updateProjectStartAndEndDate(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.endDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
        // this.getTeamByProjectId(this.projectObj.projectId);
      }
    })
  }

  isFutureDate(dateString: string | Date): boolean {
    if (!dateString) return false;
    const today = new Date();
    const inputDate = new Date(dateString);
    // Set time to 0:00:00 to compare only by date (optional)
    today.setHours(0, 0, 0, 0);
    inputDate.setHours(0, 0, 0, 0);
    return inputDate > today;
  }
  modalRef2: BsModalRef = new BsModalRef();
  modalRef3: BsModalRef = new BsModalRef();
  activeProjects: any;
  EmployessIds: any;
  employeeTeamMapId:any;
  setDefaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();


  deleteResourceModal(template: TemplateRef<any>, template1: TemplateRef<any>, template2: TemplateRef<any>, projObj, teamId) {
    console.log("test", projObj);
    const empIds: number[] = [projObj.empId];
    this.EmployessIds = empIds;
    this.projectteamInfo.projectId = projObj.projectId;
   
 console.log("test", projObj.empId);
    this.resourceManagementService.getTeamListByProjectName(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj, "projectofthisteam");

        this.projectObj.teamList.forEach((obj) => {
          obj.departmentList = obj.departmentList?.map(x => +x);
          this.copyDepartment = obj.departmentList;

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                member.emp360 = member.empId;
                // member.shadowControl = new FormControl(member.shadow || null);
              }
            });
          } else {
            console.warn('teamMemberList is null or undefined');
          }
        });

        this.previewTeamList = this.projectObj.teamList;

        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
       
        } else { 
          this.allTeamList = this.projectObj.teamList;

          this.allTeamList = this.allTeamList.filter(team => team.teamId === teamId);


          const empIds: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
            const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];
const targetMember = members.find(member => member.empId === projObj.empId);

this.employeeTeamMapId = targetMember ? targetMember.employeeTeamMapId : null;
console.log("mapping ID",this.employeeTeamMapId);
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
          this.EmployessIds = this.EmployessIds.filter(id => id === projObj.empId);
          this.activeProjects = this.activeProjects.filter(id => id === projObj.empId);
          console.log("test",this.activeProjects,this.EmployessIds);
          if (this.activeProjects.length !== 0) {
            this.setDefaultProjectObj.empIds = this.activeProjects;
            this.setDefaultProjectObj.projectId = projObj.projectId;
            this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
            this.modalRef2 = this.modalService.show(template1, { class: 'modal-xl' });

          } else if (this.EmployessIds.length !== 0) {
            this.getEmployeeInformationBulk(this.EmployessIds);
            this.modalRef3 = this.modalService.show(template, { class: 'modal-xl' });
          }
          else {
            this.modalRef = this.modalService.show(template2, { class: 'modal-sm' });
          }

        }

      } else {
        console.error(response.serviceResponse);

      }
    });
   
    this.projectObj = projObj;
 console.log("test",this.projectObj);

    this.lastDate = projObj.poEndDate
      ? moment(projObj.poEndDate).format('YYYY-MM-DD')
      : moment().format('YYYY-MM-DD');

    if (this.modalRef) this.modalRef.hide();
    if (this.modalRef2) this.modalRef2.hide();


    this.getProjectDetailsForBulkDefaultUpdate();

  }

  getEmployeeInformationBulk(empIds) {
    this.resourceManagementService.getEmployeeInformationBulk(empIds).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeList = response.serviceResponse;
        console.error("Unable to fetch Employee List!", this.bulkEmployeeList);
      } else {
           if (this.EmployessIds.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      }
    });
  }


  bulkEmployeeList: EmployeeInformation[] = [];

  @ViewChild("alert_message_without_reload")
  alertTemplateWithoutReload: TemplateRef<any>;

  openAlertMod3(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
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
  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  searchTerm: any
  searchTermTeam: any;
  resourceRequirementListBulk: any;
  defaultProjectUpdate: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredOtherProjectList: any[] = [];
  otherProjectList: any[] = [];
  filteredTeamsForDefaultBulk: any;
  isBulkDelete: boolean = false;
  isBulkUpdateMode: boolean = true;
  filteredProjectsForDefaultBulkBench: any;
  defaultProjectUpdateBulk: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredProjectsForDefaultBulkOther: any;
  bulkProjectType: any;
  benchProjectListBulk: any;
  otherProjectListBulk: any;

  teamListBulk: any;
  filterOtherProjects() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredOtherProjectList = this.otherProjectList.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }
  setDefaultProjectUpdateForActiveProject(details: any, projectId, template: TemplateRef<any>) {
    console.log("test id", details.empId, details);
    this.defaultProjectUpdate.empIds = [details.empId];
    this.defaultProjectUpdate.projectId = projectId;
    this.defaultProjectUpdate.createdBy = this.currentUser.empId;
     if(!this.defaultProjectUpdate.projectId){
      this.openAlertMod3(this.alertTemplateWithoutReload, "Please update Default Project");
      return;
    }
    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        this.activeProjects = this.activeProjects.filter(id => id !== details.empId);
        if (this.activeProjects.length === 0) {
          this.modalRef2.hide();  
          this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
        }
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error setting the default project!");
      }
    });
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

  filterTeamsForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
  }



  setProjectMappingAndDefaultProjectBulk(setDefaultProjectObj, template: TemplateRef<any>) {

    setDefaultProjectObj.empId = this.EmployessIds;

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
          this.modalRef.hide();
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
          } else {
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the Employees default Projects");
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

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


  setProjectMappingAndDefaultProject(template: TemplateRef<any>, emp) {
    console.log("empId", emp, emp.empId);
    this.setDefaultProjectObj.empId = [emp.empId];
    this.setDefaultProjectObj.createdBy = this.currentUser.empId;
    this.setDefaultProjectObj.projectId = emp.projectId;
    this.setDefaultProjectObj.teamId = emp.teamId;
    this.setDefaultProjectObj.employeeRole = emp.employeeRole;
    this.setDefaultProjectObj.resourceOverViewId = emp.resourceOverViewId;

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
          this.EmployessIds = this.EmployessIds.filter(id => id !== emp.empId); 
          if (this.EmployessIds.length === 0) {
            this.modalRef3.hide();
            this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

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
}
