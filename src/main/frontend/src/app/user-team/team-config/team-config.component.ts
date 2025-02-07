import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Activity } from 'src/app/models/activity';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Project } from 'src/app/models/project';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { TeamService } from 'src/app/services/team.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-team-config',
  templateUrl: './team-config.component.html',
  styleUrls: ['./team-config.component.css']
})
export class TeamConfigComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  data: string;
  feature = "Team Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  dataObj: Project = new Project();
  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isTeamForm: boolean = false;
  isActivityForm: boolean = false;
  isTeamTable: boolean = false;
  isActivityTable: boolean = false;
  isDisabled: boolean = false;
  _allTeamList:any[] ;

  isActivityTemplate: boolean = false;
  isActivityCreate: boolean = false;
  isActivityUpdate: boolean = false;
  isActivityTemplateTable: boolean = false;

  templateActivityList: any[] = [];

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  modalRef5:  BsModalRef = new BsModalRef();
  //Obj 
  teamObj: Team = new Team();
  storedTeamObj: Team = new Team();
  activityTemplateObj: Team = new Team();
  allTeamMembers: any[] = [];
  allTeamsList: any[] = [];

  activityObj: Activity = new Activity();
  allActivityList: any[] = [];

  employeeObj: Employee = new Employee();

  clientList: any[] = [];
  projectList: any[] = [];
  employeeListByDept: Employee[] = [];
  teamLeadsList: any[] = [];
  newteamMember:TeamMember = new TeamMember();

  allDeptList: any[] = [];
  allProjectListByManagerId: any[] = [];
  allTeamList: any[] = [];
  employeeSpecificProjectList: any[] = [];

  filteredDeptList: any[] = [];

  allTemplateActivityList: any[] = [];
  updatedTemplateActivityList: any[] = [];
  activityPreviewList:any[] = [];

  //excel
  teamDataForExcel: any[];
  teamsByProjectIdDataForExcel: any[];
  teamsActivityDataForExcel: any[];

  selectedClient: any = '';
  selectedProject: any = '';
  selectedTeam: any = '';
  excelName = '';
  tableElement = '';
  isGoToTeamButton:boolean = false;

  filterStatus:any = '';

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin'];
  
  selectedDept:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  viewTeamColumns:any[] = ['blank', 'projectName','teamName','teamLeadName','projectManagerName','createdByName','createdOn'];
  viewActivityColumns:any[] = ['blank','activity','eta','employeeRole','createdByName','createdOn'];
  activityTemplateColumns:any[] = ['blank', 'employeeRole','activityDescription','departmentName'];
  projectObj2: Project = new Project();
  projectDetails: any = [];
  getBillableType: any;
  newMemberInProject: any;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private projectService: ProjectService,
    private teamService: TeamService,
    private router: Router,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private locationStrategy: LocationStrategy
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if (this.userMapping.add_team) {
      this.showCreateTeamForm();
    } else if (this.userMapping.view_teams || this.userMapping.update_team || this.userMapping.delete_team) {
      this.showViewTeams();
    } else if (this.userMapping.add_activity) {
      this.showCreateActivityForm();
    } else if (this.userMapping.view_activities || this.userMapping.update_activity || this.userMapping.delete_activity) {
      this.showViewActivities();
    }
  }

  showCreateTeamForm() {
    this.isTeamForm = true;
    this.isCreation = true;

    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isUpdation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.isGoToTeamButton = false;

    this.reset();
    this.getAllProjectListByProjectManagerId();
    this.getAllDepartmentList();
  }

  showViewTeams() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.selectedProject = '';
    this.selectedTeam = '';
    this.isTeamTable = true;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.page = 1;
    this.data = '';
    this.filterStatus= '';
    this.selectedDept = '';
    this.isGoToTeamButton = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.allTeamList = [];
    this.getAllProjectListByProjectManagerId();
    this.getAllDepartmentList();
    this.getAllTeamsByProjectId(0);
  }

  showViewTeams1() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    
    this.selectedTeam = '';
    this.isTeamTable = true;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.page = 1;
    this.data = '';
    this.filterStatus= '';
    this.selectedDept = '';
    this.isGoToTeamButton = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.allTeamList = [];
   
    this.getAllProjectListByProjectManagerId();
    this.getAllDepartmentList();
    this.getAllTeamsByProjectId(this.teamObj.projectId);
    
  }

  showUpdateTeamForm(teamObj: Team) {
    this.isTeamForm = true;
    this.isUpdation = true;

    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.isGoToTeamButton = false;
    this.selectedDept = '';
    
    // this.teamObj.departmentList = [];
    this.teamObj = Object.assign({}, teamObj);
    this.teamObj.updatedTeamMemberList = [];
    this.teamObj.departmentList = this.teamObj.departmentList?.map(x=>+x);
    // if(this.teamObj.teamLeadDeptId){
    //   this.teamObj.departmentList.push(this.teamObj.teamLeadDeptId);
    // }
    this.getTeamMembersByTeamId(this.teamObj.teamId);

    /* To Add New Members */
    this.allTeamMembers = [];
    this.newteamMember = new TeamMember();
    // this.addInputTeamMemberField();
  }

  showCreateActivityForm() {
    this.isActivityForm = true;
    this.isCreation = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isUpdation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;

    this.reset();
    if(this.isGoToTeamButton == true){
      this.activityObj.projectId = this.selectedProject;
      this.activityObj.teamId = this.selectedTeam;
      this.getDepartmentByTeam(this.selectedTeam);
    }
    if(this.selectedProject != null && this.selectedTeam != null){
      this.activityObj.projectId = this.selectedProject;
      this.activityObj.teamId = this.selectedTeam;
      this.getDepartmentByTeam(this.activityObj.teamId);
    }
    this.getAllDepartmentList();
    this.getAllProjectsByEmpId();
    this.getAllProjectListByProjectManagerId();
  }

  showViewActivities() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.selectedProject = '';
    this.selectedTeam = '';
    this.isActivityTable = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.page = 1;
    this.data = ''
    this.selectedDept = '';
    this.allActivityList = [];
    this.filters = {};
    this.isSearchEnabled = false;

    this.getAllProjectsByEmpId();
    this.getAllProjectListByProjectManagerId();
  }

  showUpdateActivityForm(activityObj: Activity) {
    this.isActivityForm = true;
    this.isUpdation = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;
    this.selectedDept = '';
    
    this.activityObj = Object.assign({}, activityObj);

    //console.log(this.activityObj, " : this.activityObj", " activityObj.departmentList" , activityObj.departmentList);
    

    this.activityObj.departmentList = activityObj.departmentList?.map(x=>+x);

    //console.log(this.activityObj, " : this.activityObj.departmentList");
    

    this.getDepartmentByTeam(this.activityObj.teamId);
    this.getAllProjectListByProjectManagerId();
  }

  showActivityTemplate(){
    this.isActivityTemplate = true;
    this.isActivityCreate = true;

    this.isActivityUpdate = false;
    this.isActivityTemplateTable = false;

    this.isCreation = false;
    this.isUpdation = false;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isDisabled = false;
    this.isGoToTeamButton = false;
    this.allTemplateActivityList = [];

    this.teamObj = new Team();
    this.reset();

    //Template Activity
    if (this.teamObj.templateActivityList == undefined || this.teamObj.templateActivityList == 0) {
      this.addInputTemplateActivityField();
    } else {
      this.allTemplateActivityList = this.teamObj.templateActivityList;
    }
  }

  showActivityTemplateTable(){
    this.data = ''
    this.isActivityTemplateTable = true;

    this.isActivityTemplate = false;
    this.isActivityCreate = false;
    this.isActivityUpdate = false;

    this.isCreation = false;
    this.isUpdation = false;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isDisabled = false;
    this.isGoToTeamButton = false;
    this.templateActivityList = [];
    this.allTemplateActivityList = [];
    this.filters = {};
    this.isSearchEnabled = false;

  }

  showUpdateActivityTemplateForm(activityTemplate: Team){
    this.isActivityTemplate = true;
    this.isActivityUpdate = true;

    this.isActivityCreate = false;
    this.isActivityTemplateTable = false;

    this.isCreation = false;
    this.isUpdation = false;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isDisabled = false;
    this.isGoToTeamButton = false;
    this.allTemplateActivityList = [];
    this.reset();

    this.teamService.getActivityTemplateById(activityTemplate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.activityTemplateObj = Object.assign({}, response.serviceResponse);

        this.allTemplateActivityList = this.activityTemplateObj.templateActivityList;

        //console.log( this.activityTemplateObj , " :  this.activityTemplateObj ");
        
      } else {
        //console.log(response.serviceStatus);
      }
    });
  }

  // Manage Template Activity
  addInputTemplateActivityField(){
    let newTeamObj = new Team();
    this.allTemplateActivityList.push(newTeamObj);
    //console.log(this.allTemplateActivityList, " : this.allClientLocation");
  }

  removeInputTemplateActivityField(activityObj) {
    this.allTemplateActivityList.forEach((value, index) => {
      if (value == activityObj) {
        this.updatedTemplateActivityList.push(value);
        this.allTemplateActivityList.splice(index, 1);
      }
    });
    //console.log(this.updatedTemplateActivityList, " :this.updatedTemplateActivityList");
  }

  reset() {
    this.teamObj = new Team();
    this.teamObj.projectId = '';
    this.teamObj.teamLeadId = '';
    // this.teamObj.departmentList = '';

    this.activityObj = new Activity();

    this.allTeamMembers = [];
    this.allTemplateActivityList = [];
    this.activityTemplateObj = new Team();
    this.newteamMember = new TeamMember();
    this.filterStatus= '';
    this.selectedDept = '';
    // this.addInputTeamMemberField();
  }

  // Manage team members
  addInputTeamMemberField() {
    let newTeamMemberObj = new TeamMember();
    // newTeamMemberObj.empId = '';
    this.allTeamMembers.push(newTeamMemberObj);
  }

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember){
        this.allTeamMembers.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });

    //console.log("Updated Members : ", this.allTeamMembers);
    
  }



  /* Team Configuration */
  validateTeamObj(teamObj: Team, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(teamObj.projectId)) {
      this.alertMessage = "Please select Project Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(teamObj.teamName)) {
      this.alertMessage = "Please enter Team Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateTeamName(teamObj.teamName)) {
      this.alertMessage = "Please enter valid Team Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.allTeamMembers.length !== 0) {
      let flag = true;
      this.allTeamMembers.forEach(obj => {
        if (obj.employeeRole == undefined || obj.employeeRole === null || obj.employeeRole.length == 0) {
          this.alertMessage = "Please select atleast one employee role for Activity mapping !!"
  
          flag = false
          return false;
        }
      });

      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else {
        return true;
      }
    }


    // if (!this.validationService.validateNullUndefinedEmptyString(teamObj.departmentList)) {
    //   this.alertMessage = "Please select Department !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    return true;
  }


  onCreateTeam(template: TemplateRef<any>) {
    this.teamObj.teamName = this.teamObj.teamName?.trim();
    let inputValidated: boolean = this.validateTeamObj(this.teamObj, template)
    if (!inputValidated) return;

    //console.log("allTeamMembers :", this.allTeamMembers);
    this.teamObj.allTeamMemberList = (this.allTeamMembers.length !== 0) ? this.allTeamMembers : null;
    this.teamObj.createdBy = this.currentUser.empId;
    //console.log("create teamObj : ", this.teamObj);

    

    if(this.teamObj.allTeamMemberList == undefined || this.teamObj.allTeamMemberList.length === 0){
      this.alertMessage = "Please select atleast one Team member !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.teamService.createTeam(this.teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewTeams();
        this.selectedProject = this.teamObj.projectId;
        this.getAllTeamsByProjectId(this.selectedProject);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  resetSelectSearch(matSelect:any){
    matSelect.searchValue = '';
    matSelect.filteredSource = matSelect.source;
  }

  addTeamMember(){
    
    const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);
    if(newTeamMember){
      newTeamMember.employeeRole = this.newteamMember.employeeRole;
      this.allTeamMembers.push(newTeamMember);
    }

    this.newteamMember = new TeamMember();
  }

  removeTeamMember(teamMember) {

    this.teamObj.allTeamMemberList?.forEach((value, index) => {
      if (value == teamMember) {
        if (this.teamObj.updatedTeamMemberList[0]) {
          this.teamObj.updatedTeamMemberList.push(value);
        } else {
          this.teamObj.updatedTeamMemberList = [];
          this.teamObj.updatedTeamMemberList.push(value);
        }
        this.teamObj.allTeamMemberList.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });

  }

  getMappedActivityPreview(teamMember:any){

    if(this.isUpdation == true){
      this.getActivityInUpdateTeam(teamMember);
    }else{
      if(teamMember.employeeRole.length == 0){
        this.activityPreviewList = [];
      }
  
      let teamObj = new Team();
      teamObj.empId = teamMember.empId;
      teamObj.employeeRole = teamMember.employeeRole;
      teamObj.departmentList = this.teamObj.departmentList;
  
      this.teamService.getMappedActivityPreview(teamObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.activityPreviewList = response.serviceResponse;
          //console.log(this.activityPreviewList, " : activityPreviewList");
        } else {
          //console.log(response.serviceResponse, " response");
        }
      });
    }
  }

  getActivityInUpdateTeam(existingMember:any){
    this.activityPreviewList = [];
    let teamObj = new Team();
    teamObj.teamId = this.teamObj.teamId;
    teamObj.empId = existingMember.empId;
    teamObj.employeeRole = existingMember.employeeRole;

    this.teamService.getMappedActivityInUpdateTeam(teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.activityPreviewList = response.serviceResponse;
        //console.log(this.activityPreviewList, " : activityPreviewList");
      } else {
        //console.log(response.serviceResponse, " response");
      }
    });
  }

  onUpdateTeam(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateTeamObj(this.teamObj, template)
    if (!inputValidated) return;

    if((this.teamObj.allTeamMemberList == undefined || this.teamObj.allTeamMemberList.length === 0) && (this.allTeamMembers == undefined || this.allTeamMembers.length === 0)){
      this.alertMessage = "Please select atleast one Team member !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.allTeamMembers = (this.allTeamMembers.length !== 0) ? this.allTeamMembers : null;

    if (this.teamObj.updatedTeamMemberList[0]) {
      if (this.allTeamMembers) {
        this.teamObj.updatedTeamMemberList = this.teamObj.updatedTeamMemberList.concat(this.allTeamMembers);
      }
    } else {
      if (this.allTeamMembers) {
        this.teamObj.updatedTeamMemberList = [];
        this.teamObj.updatedTeamMemberList = this.teamObj.updatedTeamMemberList.concat(this.allTeamMembers);
      }
    }

    this.teamObj.updatedBy = this.currentUser.empId;
    //console.log("update teamObj : ", this.teamObj);
    this.teamService.updateTeam(this.teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewTeams();
        this.selectedProject = this.teamObj.projectId;
        
        this.getAllTeamsByProjectId(this.selectedProject);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
 view:any;
  onDeleteTeam(template: TemplateRef<any>) {
    this.cancelRequest();

    this.teamService.deleteTeam(this.teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewTeams();
        this.selectedProject = this.teamObj.projectId;
        this.getAllTeamsByProjectId(this.selectedProject);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  getAllTeamsByProjectId(projectId: any) {
    this.allTeamList = [];
    this._allTeamList = [];
    this.allActivityList = [];
    this.filterStatus = "";
    this.activityObj.teamId = '';

    //console.log("selectedProject : ", this.selectedProject);
    
    if(projectId == 0){
        this.getAllMyTeamsByEmpId();
    }
    else if(projectId == -1){
      this.getAllTeams()
    }else{
      let teamObj = new Team();
      teamObj.projectId = projectId;
      this.teamService.getAllTeamsByProjectId(teamObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allTeamList = response.serviceResponse;
          this.allTeamList.forEach(team => {
            team.createdOn = (team.createdOn)? moment(team.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          this._allTeamList = this.allTeamList

          this.isDisabled = false;
          //console.log("allTeamList :", this.allTeamList);
        } else {
          console.error(response.serviceResponse)
        }

        this.filterStatus = "Active";
        this.changeEvent();
      });
    }
  }

  getDepartmentByTeam(teamId: any){
    this.filteredDeptList = [];

    const selectedTeam = this.allTeamList.find(x => x.teamId == teamId);
    let departmentList = selectedTeam.departmentList.map(x => +x);
    this.filteredDeptList = this.allDeptList.filter(x => departmentList.includes(x.deptId));
  }

  getAllMyTeamsByEmpId() {
    this.allTeamList = [];
    this._allTeamList = [];
    this.allActivityList = [];
    this.filterStatus = "";
    this.selectedProject = "0";

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    employeeObj.departmentName = this.currentUser.departmentName;
    employeeObj.employeeRole = this.currentUser.employeeRole;

    this.teamService.getAllMyTeamsByEmpId(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log("response.serviceResponse :", response.serviceResponse);
        
        this.allTeamList = response.serviceResponse;
        this.allTeamList.forEach(team => {
          team.createdOn = (team.createdOn)? moment(team.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this._allTeamList = this.allTeamList
        this.isDisabled = false;
        //console.log("getAllMyTeamsByEmpId -- allTeamList :", this.allTeamList);
      } else {
        console.error(response.serviceResponse)
      }

      this.filterStatus = "Active";
      this.changeEvent();
    });
  }

  getAllTeams() {
    this.allTeamList = [];
    this._allTeamList = [];
    this.allActivityList = [];
    this.filterStatus = "";
    this.selectedProject = "-1";

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    employeeObj.departmentName = this.currentUser.departmentName;
    employeeObj.employeeRole = this.currentUser.employeeRole;

    this.teamService.getAllTeams().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamList = response.serviceResponse;
        this.allTeamList.forEach(team => {
          team.createdOn = (team.createdOn)? moment(team.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this._allTeamList = this.allTeamList
        this.isDisabled = false;
        //console.log("getAllTeams :", this.allTeamList);
      } else {
        console.error(response.serviceResponse)
      }

      this.filterStatus = "Active";
      this.changeEvent();
    });
  }

  changeEvent(){
    if(this.filterStatus == "Active"){
      this.allTeamList = this._allTeamList.filter(x =>
        x.isActive == 'Y'
      );
    } else if(this.filterStatus == "InActive"){
      this.allTeamList = this._allTeamList.filter(x =>
        x.isActive == 'N'
      );
    }
    this.page = 1
  }

  getTeamMembersByTeamId(teamId: any) {
    this.teamObj.allTeamMemberList = [];

    let teamObj = new Team();
    teamObj.teamId = teamId;
    this.teamService.getTeamMembersByTeamId(teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamObj.allTeamMemberList = response.serviceResponse;
        if(this.teamObj.allTeamMemberList){
          // this.teamObj.allTeamMemberList.forEach(teamMember => {
          //   this.teamObj.departmentList.push(teamMember.teamMemberDeptId);
          // });

          // this.teamObj.departmentList = Array.from(new Set(this.teamObj.departmentList));

          this.getAllEmployeesByDepartmentIds();   
          this.getAllEmployeesByRole();      
        }
        //console.log("teamObj.allTeamMemberList :", this.teamObj.allTeamMemberList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  // Project Details 
  getAllProjectListByProjectManagerId() {
    let isAllProjectAllowed = false;
    this.allProjectListByManagerId = [];

    let projectObj = new Project();
    // projectObj.projectManagerId = 184; //! Here we are getting all projects
    this.projectService.getAllProjectListByProjectManagerId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log("Current user : Persona : "+ this.currentUser.employeeRole + " || Department : "+ this.currentUser.departmentName);
        let allProjectList = response.serviceResponse;

        if(this.userMapping.allow_all_projects){
          isAllProjectAllowed = this.userMapping.allow_all_projects;
        }

        allProjectList = allProjectList.sort((a, b) => a.projectName.localeCompare(b.projectName));
        if(this.currentUser.employeeRole == 'HOD' || this.currentUser.employeeRole == 'SuperAdmin' || this.currentUser.employeeRole == 'HR' || this.currentUser.employeeRole == 'Manager' || isAllProjectAllowed){
          this.allProjectListByManagerId = allProjectList;

          //revome repeated project

          this.allProjectListByManagerId = this.allProjectListByManagerId.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.projectId === value.projectId
            ))
          )
          //console.log("allProjectList For HOD / HR / SuperAdmin / Manager / ANurag :", this.allProjectListByManagerId);
        }else{
          this.allProjectListByManagerId = allProjectList;
          this.allProjectListByManagerId = allProjectList.filter((projectObj:Project) => projectObj.departmentName == this.currentUser.departmentName);
          
          //My Projects
          projectObj.empId = this.currentUser.empId;
          this.projectService.getAllMyProjectByEmpId(projectObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              let myProjectList = response.serviceResponse;

              if(myProjectList){
                this.allProjectListByManagerId.push(myProjectList);
              }

            } else {
              console.error(response.serviceResponse);
            }
          });

          //revome repeated project

          this.allProjectListByManagerId = this.allProjectListByManagerId.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.projectId === value.projectId
            ))
          )
          //console.log("allProjectList By Department :", this.allProjectListByManagerId);
        }

        this.allProjectListByManagerId = this.allProjectListByManagerId.sort((a, b) => a.projectName.localeCompare(b.projectName));

        if(this.isTeamTable){
          this.allProjectListByManagerId.unshift({
            projectId : 0,
            projectName : 'My Teams'
          });

          if(this.currentUser.employeeRole == 'SuperAdmin'){
            this.allProjectListByManagerId.unshift({
              projectId : -1,
              projectName : 'All Teams'
            });
          }
        }

      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllProjectsByEmpId() {
    this.employeeSpecificProjectList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeSpecificProjectList = response.serviceResponse;

        //console.log(this.employeeSpecificProjectList, " this.employeeSpecificProjectList==");
        
        // filter list if data is duplicate

        this.employeeSpecificProjectList = this.employeeSpecificProjectList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectName === value.projectName
          ))
        )
        //console.log("employeeSpecificProjectList :", this.employeeSpecificProjectList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // For Team Lead
  getAllEmployeesByRole() {
    this.teamLeadsList = [];
    let employeeList = [];

    // this.employeeObj.role = "TeamLead";
    this.employeeObj.role = this.currentUser.employeeRole;
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        //console.log("employeeList By Role : ", employeeList);
        //console.log("Department Selected : ", this.teamObj.departmentList);

        // remove teamLead if their department are not selected.
        // let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        // this.teamLeadsList = employeeList.filter(x => filterDepartmentList.includes(x.departmentId));
        // this.teamLeadsList = employeeList.filter(x => x.departmentId == this.teamObj.deptId);

        let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        this.teamLeadsList = employeeList.filter(x => this.teamObj.departmentList?.includes(x.departmentId));
        this.teamLeadsList = this.teamLeadsList.sort((a, b) => a.name.localeCompare(b.name));
        //console.log("teamLeadsList : ", this.teamLeadsList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  updateEmployeeListAccordingToTeamMembers() {
    //console.log("Existing Team Member : ", this.teamObj.allTeamMemberList);
    //console.log("Selected Team Member : ", this.allTeamMembers);
    this.employeeListByDept.forEach((employee, index) => {
      const existingEmployee = this.teamObj.allTeamMemberList?.find(member => member.empId == employee.empId);
      if (existingEmployee) {
        employee.isSelected = true;
      }else{
        const existingEmployee = this.allTeamMembers.find(member => member.empId == employee.empId);
        if (existingEmployee) {
          employee.isSelected = true;
        }
      }
    });
  }

  // getAllEmployeesByDepartmentIds() {
  //   this.employeeListByDept = [];

  //   let empObj = new Employee();
  //   empObj.departmentId = this.teamObj.deptId;
  //   // empObj.departmentList = this.teamObj.departmentList.map(deptId => {
  //   //    let dept =  new Department();
  //   //    dept.deptId = deptId;
  //   //    return dept;
  //   // });

  //   // //console.log("empObj.departmentList : ", empObj.departmentList);
    
  //   this.employeeService.getAllEmployeesByDepartmentId(empObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.employeeListByDept = response.serviceResponse;
  //       this.employeeListByDept = this.employeeListByDept.sort((a, b) => a.name.localeCompare(b.name));
  //       //console.log("employeeList By Department : ", this.employeeListByDept);
  //       this.updateEmployeeListAccordingToTeamMembers();
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  getAllEmployeesByDepartmentIds() {
    this.employeeListByDept = [];

    let empObj = new Employee();
    empObj.departmentList = this.teamObj.departmentList?.map(deptId => {
       let dept =  new Department();
       dept.deptId = deptId;
       return dept;
    });

    //console.log("empObj.departmentList : ", empObj.departmentList);
    
    this.employeeService.getAllEmployeesByDepartmentIds(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListByDept = response.serviceResponse;
        this.employeeListByDept = this.employeeListByDept.sort((a, b) => a.name.localeCompare(b.name));
        //console.log("employeeList By Department : ", this.employeeListByDept);
        this.updateEmployeeListAccordingToTeamMembers();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        //console.log("allDeptList : ", this.allDeptList)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  /* Activity Configuration */
  validateActivityObj(activityObj: Activity, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.projectId)) {
      this.alertMessage = "Please select Project !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.teamId)) {
      this.alertMessage = "Please select Team Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    activityObj.activity = activityObj.activity?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.activity)) {
      this.alertMessage = "Please enter Activity !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateTeamActivity(activityObj.activity)) {
      this.alertMessage = "Please enter Valid Activity !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.employeeRole)) {
      this.alertMessage = "Please select Employee Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (activityObj.departmentList == null || activityObj.departmentList.length == 0) {
      this.alertMessage = "Please select atleast one department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (!this.validationService.validateNullUndefinedEmptyString(activityObj.eta)) {
    //   this.alertMessage = "Please enter Activity ETA (Hours)!!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (activityObj.eta < 0 || activityObj.eta > 999) {
    //   this.alertMessage = "Please enter Valid Activity ETA (Hours) between 0-999 Hours!!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    return true;
  }

  onCreateActivity(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateActivityObj(this.activityObj, template)
    if (!inputValidated) return;

    this.activityObj.createdBy = this.currentUser.empId;
    //console.log("create activity : ", this.activityObj);
    this.teamService.createActivity(this.activityObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewActivities();
        this.selectedProject = this.activityObj.projectId;
        this.selectedTeam = this.activityObj.teamId;
        this.getAllActivitiesByProjectIdAndTeamId(this.selectedProject, this.selectedTeam);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateActivity(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateActivityObj(this.activityObj, template)
    if (!inputValidated) return;

    this.activityObj.updatedBy = this.currentUser.empId;
    //console.log("update Activity : ", this.activityObj);

    this.teamService.updateActivity(this.activityObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewActivities();
        this.selectedProject = this.activityObj.projectId;
        this.selectedTeam = this.activityObj.teamId;
        this.getAllActivitiesByProjectIdAndTeamId(this.selectedProject, this.selectedTeam);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteActivity(template: TemplateRef<any>) {
    this.cancelRequest();

    this.teamService.deleteActivity(this.activityObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewActivities();
        this.selectedProject = this.activityObj.projectId;
        this.selectedTeam = this.activityObj.teamId;
        this.getAllActivitiesByProjectIdAndTeamId(this.selectedProject, this.selectedTeam);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllActivitiesByProjectIdAndTeamId(projectId: any, teamId: any) {
    this.allActivityList = [];

    let activityObj = new Activity();
    activityObj.projectId = projectId;
    activityObj.teamId = teamId;
    this.teamService.getAllActivitiesByProjectIdAndTeamId(activityObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allActivityList = response.serviceResponse;
        this.allActivityList.forEach(activity => {
          activity.createdOn = (activity.createdOn)? moment(activity.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log("allActivityList :", this.allActivityList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // Activity template :: start

  validateActivityTemplateObj(team: Team, template: TemplateRef<any>,){
    let flag = true;
    if (!this.validationService.validateNullUndefinedEmptyString(team.employeeRole)) {
      this.alertMessage = "Please select Employee role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(team.deptId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const uniqueActivity = new Set(this.allTemplateActivityList.map(x => x.activity));
    if (uniqueActivity.size < this.allTemplateActivityList.length) {
      this.alertMessage = "Duplicate Activities are not allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.allTemplateActivityList.forEach((activity, index) => {

      activity.activity = activity.activity?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(activity.activity)) {
        this.alertMessage = `Please enter Activity - ${index + 1}!!`
        flag = false;
        return;
      } if (!this.validationService.validateActivityName(activity.activity)) {
        this.alertMessage = `Please enter valid Activity - ${index + 1}!!`
        // activity.activity = ''
        flag = false;
        return;
      }
    });

    if (!flag) {
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else{
      return true;
    }
  }

  createActivityTemplate(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateActivityTemplateObj(this.activityTemplateObj, template)
    if (!inputValidated) return;

    this.activityTemplateObj.templateActivityList = this.allTemplateActivityList;

    this.teamService.createActivityTemplate(this.activityTemplateObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.selectedDept = this.activityTemplateObj.deptId;
        this.showActivityTemplateTable();
        this.getActivityTemplate(template);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getActivityTemplate(template: TemplateRef<any>){
    if (!this.validationService.validateNullUndefinedEmptyString(this.selectedDept)) {
      this.alertMessage = "Please select department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let teamObj = new Team();
    teamObj.deptId = this.selectedDept;

    this.teamService.getActivityTemplate(teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.templateActivityList = response.serviceResponse;

        // filter list if data is duplicate

        this.templateActivityList = this.templateActivityList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.departmentName === value.departmentName && t.employeeRole === value.employeeRole
          ))
        )
        //console.log(this.templateActivityList, " this.templateActivityList");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  updateActivityTemplate(template: TemplateRef<any>){
    
    let inputValidated: boolean = this.validateActivityTemplateObj(this.activityTemplateObj, template)
    if (!inputValidated) return;
    this.activityTemplateObj.templateActivityList = (Object.keys(this.allTemplateActivityList[0]).length === 0) ? null : this.allTemplateActivityList;
    
    //console.log(this.activityTemplateObj, " :this.activityTemplateObj");
    
    this.teamService.updateActivityTemplate(this.activityTemplateObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showActivityTemplateTable();
        this.selectedDept = this.activityTemplateObj.deptId;
        this.getActivityTemplate(template);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Activity template :: end

  navigateToUpdateActivityPage(teamId:any){
    this.cancelRequest();
    this.isGoToTeamButton = true;
    this.isActivityTable = true;

    this.isUpdation = false;
    this.isTeamForm = false;

    this.selectedProject = this.storedTeamObj.projectId;
    this.selectedTeam = teamId;

    this.getAllTeamsByProjectId(this.selectedProject);
    this.getAllActivitiesByProjectIdAndTeamId(this.selectedProject, this.selectedTeam);
  }

  goToUpdateTeamPage(){
    this.showUpdateTeamForm(this.storedTeamObj);
  }

  // download excel

  exportToExcel(): void {

    if (this.isTeamTable == true) {
      this.excelName = 'TeamSheet.xlsx';

      this.teamsActivityDataForExcel = this.allTeamList;

      const onlySpecificDataArr = this.teamsActivityDataForExcel.map(
        x => ({
          "Project Name": x.projectName,
          "Team Name": x.teamName,
          "Team Lead": x.teamLeadName,
          "Project Manager": x.projectManagerName,
          "Created by": x.createdByName,
          "Created on": (x.createdOn)? moment(x.createdOn, "DD-MM-YYYY").format(AppComponent.DATETIME_FORMAT) : null
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);

    }
    if (this.isActivityTable == true) {
      this.excelName = 'ActivitiesSheet.xlsx';

      this.teamsByProjectIdDataForExcel = this.allActivityList;

      const onlySpecificDataArr = this.teamsByProjectIdDataForExcel.map(
        x => ({
          "Activity": x.activity,
          "ETA": x.eta,
          "Created by": x.createdByName,
          "Created on": (x.createdOn)? moment(x.createdOn, "DD-MM-YYYY").format(AppComponent.DATETIME_FORMAT) : null
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);

    }
  }

  checkTeamName(template: TemplateRef<any>) {
    this.teamService.checkTeamName(this.teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.teamObj.teamName = '';
      }
    });
  }


  //modals
  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openDeleteTeamMod(template: TemplateRef<any>, teamObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.teamObj = teamObj;
  }

  openDeleteActivityMod(template: TemplateRef<any>, activityObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.activityObj = activityObj;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  cancelRequest5(){
    this.modalRef5.hide();
  }

  

  cancelRequest2() {
    this.modalRef2.hide();
  }

  openActivityPreviewModal(template: TemplateRef<any>,teamObj:any) {
    this.storedTeamObj = teamObj;
    //console.log(this.storedTeamObj, " this.storedTeamObj");
    
    this.modalRef = this.modalService.show(template);
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
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

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  deleteResourceModal(template: TemplateRef<any>, teamId) {
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj2 = teamId;

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

  openProjectTemplateModal(template: TemplateRef<any>, employee) {
    this.modalRef5 = this.modalService.show(template, { class: 'modal-xl' });
    this.getExistingProjectsByUser(employee.empId);
    this.dataObj = employee;

    console.log("data employee newmenbfcg  ", employee)


  }

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

  pageNo = 1;
  handlePageChanges(event) {
    this.pageNo = event;
  }

}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}