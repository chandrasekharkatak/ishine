import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Project } from 'src/app/models/project';
import { TeamMember } from 'src/app/models/teamMember';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
import { ResourceManagementComponent } from 'src/app/user-team/resource-management/resource-management.component';

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

isProjectVisible:boolean=true;
isProjectTeamVisible:boolean=false;
isProjectTeamMemberVisible:boolean=false;

  employeeData: any;
  getBillableType: any;
  newMemberInProject: any;
  lastDate: any;

  copyDepartment : any = [];
  currentBreadcrumbList: any[] = [];
  allProjectList: any[] = [];
  //Rahul Singh
  filterProjectByProjectId:any[]=[];
  filterTeamfromTeamId: any; 
  //end 
  filteredDeptList: any[] = [];
  allDeptList: any[] = [];
  allTeamList: any[] = [];
  previewTeamList: any[] = [];

  filters:any = {};
  isSearchEnabled:boolean = false;
  projectColumns:any[] = ['blank','projectName' ,'teamName' , 'clientName', 'billableType', 'startDate', 'updatedOn'];

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  newteamMember: TeamMember = new TeamMember();

  projectObj: Project = new Project();
  projectEditObj: Project = new Project();

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  abbreviationError: string = '';
  @ViewChild(ResourceManagementComponent) resourceManagementComponent: ResourceManagementComponent;
  employeesFor360:any[] = [];

  constructor(
    private breadcrumbService: BreadcrumbService,
    private projectService: ProjectService,
    private modalService: BsModalService,
    private router:Router,
    private departmentService: DepartmentService,
    private resourceManagementService: ResourceManagementService,
    private employeeService: EmployeeService,
    private utilityService: UtilityService,
    private emp360Service:Employee360Service
  ) {
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
   }

  ngOnInit(): void {

    const storedData = localStorage.getItem('employee360Data');
      const parsedData = storedData ? JSON.parse(storedData) : null;
      if(parsedData != null || parsedData != undefined ){
        this.employeeData =  parsedData;
      }else{
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

    this.getAllEmployeeFor360View();
    //get Project by Employee
    this.getExistingProjectsByUser();
  }
  clearBreadcrumbs(){
    // this.breadcrumbService.setBreadcrumbSubject(null);
    window.location.reload()
  }
//added by rahul singh
backfromvisibility(type:any){
  this.isProjectVisible=false;
  this.isProjectTeamVisible=false;
  this.isProjectTeamMemberVisible=false;
  
  if(type=="ProjectVisible"){
    this.isProjectVisible=true;
  }
  if(type=="ProjectTeamVisible"){
    this.isProjectTeamVisible=true;
  }
  if(type=="ProjectTeamMemberVisible"){
    this.isProjectTeamVisible=true
  }
}
redirecttoProjectTeam(id:any){
  this.isProjectVisible=false;
  this.isProjectTeamVisible=true;
  this.isProjectTeamMemberVisible=false;
  // filterProjectByProjectId:any[]=[];
  // filterTeamfromTeamId:any[]=[];
  console.log("this.projectDetails ", id);
  this.filterProjects(id);
}
redirecttoTeam(id:any,projectId:any){
  this.isProjectTeamMemberVisible=true;
  this.isProjectVisible=false;
  this.isProjectTeamVisible=false;
  this.getTeamEmployeeByTeamId(id);
  console.log("this.projectDetails ", projectId);
  this.filterProjects(projectId);
 

}

projectteamInfo: Project = new Project();
getTeamByProjectId(projectId:any){
 
  this.projectteamInfo.projectId = projectId;
  this.emp360Service.getTeamInfo(this.projectteamInfo).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.filterProjectByProjectId = response.serviceResponse;
  
    }
  });
}
filterProjects(id) {
  this.getTeamByProjectId(id);
   
}
filterTeamMemberProjects(id) {
  this.filterProjectByProjectId = this.allProjectList.filter(project =>
    project.projectId==id
  );
}
async getTeamEmployeeByTeamId(teamId: any) {
  try {
    const response: any = await this.projectService.getTeamMemberByTeamId(teamId).pipe(first()).toPromise();

    if (response.serviceStatus === 'Success') {
      this.filterTeamfromTeamId = response.serviceResponse;
      
      this.filterTeamfromTeamId.forEach((employee) => {
          let matchingEmployee = this.employeesFor360.find(emp => emp.empId == employee.empId);
          console.log("matchingEmployee ", matchingEmployee);
          employee.emp360 = matchingEmployee ? matchingEmployee : {};
      });
      console.log('filterTeamfromTeamId = ',this.filterTeamfromTeamId);
     
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

  async getExistingProjectsByUser() {
    let projectObj = new Project();
    projectObj.empId = this.employeeData.empId;
    projectObj.isAllProj = true;

    // getExistingProjectsAndTeamsByEmployee service impl
    this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectList = response.serviceResponse;
        console.log("this.projectDetails ", this.allProjectList);

        console.log("Existing project detauls fetched for employee",this.allProjectList);
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

  deleteResourceModal(template: TemplateRef<any>, projObj) {
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
  }

  deleteResourceFromProject(template: TemplateRef<any>) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.lastDate;

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getExistingProjectsByUser();
      }
    })
  }

  editProjectInfo(projectObj :any){
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project Edit - "+projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: { }});
   
    this.resourceManagementComponent.showEditProjectForm(projectObj);
    
    
  }

  viewProjectInfo(projectObj :any){
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project View - "+projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.router.navigate([breadcrumbObject.url], { queryParams: { }});
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

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  sortData(sort: Sort){
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  async getAllEmployeeFor360View(): Promise<void> {
    this.employeesFor360 = [];
    
    try {
        const response: any = await this.employeeService.getAllEmployeesFor360View().toPromise();
        
        if (response.serviceStatus === "Success") {
            this.employeesFor360 = response.serviceResponse;

            this.employeesFor360.forEach(employeeObj => {
                employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
                employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
                employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
                employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
                employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;

                if (employeeObj.isConsultant === 'true') {
                    employeeObj.employeeType = 'Consultant';
                } else if (employeeObj.isApprenticeship === 'true') {
                    employeeObj.employeeType = 'Apprentice';
                } else {
                    employeeObj.employeeType = 'Regular';
                }
            });

            // Sort the employees
            this.employeesFor360 = new SortPipe().transform(this.employeesFor360, ['name', 'string', 'asc']);
        } else {
            alert(response.serviceResponse);
        }
    } catch (error) {
        console.error("Error fetching employees:", error);
    }
}

}
