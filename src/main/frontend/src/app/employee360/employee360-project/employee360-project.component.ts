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
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';

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

  employeeData: any;
  getBillableType: any;
  newMemberInProject: any;
  lastDate: any;

  copyDepartment : any = [];
  currentBreadcrumbList: any[] = [];
  allProjectList: any[] = [];
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

  constructor(
    private breadcrumbService: BreadcrumbService,
    private projectService: ProjectService,
    private modalService: BsModalService,
    private router:Router,
    private departmentService: DepartmentService,
    private resourceManagementService: ResourceManagementService,
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

    //get Project by Employee
    this.getExistingProjectsByUser();
  }

  showEditProjectForm(project: any) {
    this.isEditProject = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
  }

  getExistingProjectsByUser() {
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
}
