import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, takeUntil } from 'rxjs/operators';
import { Activity } from 'src/app/models/activity';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Project } from 'src/app/models/project';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ProjectService } from 'src/app/services/project.service';
import { TeamService } from 'src/app/services/team.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as XLSX from 'xlsx';
import { Sort } from '@angular/material/sort';
import { Timesheet } from 'src/app/models/timesheet';
import { TimesheetService } from 'src/app/services/timesheet.service';

@Component({
  selector: 'app-team-config',
  templateUrl: './team-config.component.html',
  styleUrls: ['./team-config.component.css']
})
export class TeamConfigComponent implements OnInit {

  data: string;
  feature = "Team Config";
  currentUser: User;
  userMapping: any = {};

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isTeamForm: boolean = false;
  isActivityForm: boolean = false;
  isTeamTable: boolean = false;
  isActivityTable: boolean = false;
  isDisabled: boolean = false;
  _allTeamList:any[] ;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj 
  teamObj: Team = new Team();
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

  //excel
  teamDataForExcel: any[];
  teamsByProjectIdDataForExcel: any[];
  teamsActivityDataForExcel: any[];

  selectedClient: any = '';
  selectedProject: any = '';
  selectedTeam: any = '';
  excelName = '';
  tableElement = '';

  filterStatus:any = '';

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private projectService: ProjectService,
    private teamService: TeamService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
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

    this.reset();
    this.getAllProjectListByProjectManagerId();
    this.getAllDepartmentList();
  }

  showViewTeams() {
    this.selectedProject = '';
    this.selectedTeam = '';
    this.isTeamTable = true;

    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.page = 1;
    this.data = '';
    this.filterStatus= '';

    this.allTeamList = [];
    this.getAllProjectListByProjectManagerId();
    this.getAllDepartmentList();
    this.getAllTeamsByProjectId(0);
  }

  showUpdateTeamForm(teamObj: Team) {
    this.isTeamForm = true;
    this.isUpdation = true;

    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;

    this.teamObj = Object.assign({}, teamObj);
    this.teamObj.updatedTeamMemberList = [];
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

    this.reset();
    this.getAllProjectsByEmpId();
  }

  showViewActivities() {
    this.selectedProject = '';
    this.selectedTeam = '';
    this.isActivityTable = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.page = 1;
    this.data = ''
    this.allActivityList = [];

    this.getAllProjectsByEmpId();
  }

  showUpdateActivityForm(activityObj: Activity) {
    this.isActivityForm = true;
    this.isUpdation = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isCreation = false;

    this.activityObj = Object.assign({}, activityObj);
  }

  reset() {
    this.teamObj = new Team();
    this.teamObj.projectId = '';
    this.teamObj.teamLeadId = '';
    // this.teamObj.departmentList = '';

    this.activityObj = new Activity();

    this.allTeamMembers = [];
    this.newteamMember = new TeamMember();
    this.filterStatus= '';
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

    console.log("Updated Members : ", this.allTeamMembers);
    
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
    if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(teamObj.teamName)) {
      this.alertMessage = "Please enter valid Team Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
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

    console.log("allTeamMembers :", this.allTeamMembers);
    this.teamObj.allTeamMemberList = (this.allTeamMembers.length !== 0) ? this.allTeamMembers : null;
    this.teamObj.createdBy = this.currentUser.empId;
    console.log("create teamObj : ", this.teamObj);

    

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

  addTeamMember(){
    const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId); 
    if(newTeamMember){
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
    console.log("update teamObj : ", this.teamObj);
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
    this.allActivityList = [];
    this.filterStatus = "";

    console.log("selectedProject : ", this.selectedProject);
    
    if(projectId == 0){
        this.getAllMyTeamsByEmpId();
    }else{
      let teamObj = new Team();
      teamObj.projectId = projectId;
      this.teamService.getAllTeamsByProjectId(teamObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allTeamList = response.serviceResponse;
          this._allTeamList = this.allTeamList
          this.filterStatus = "Active";
          this.changeEvent();

          this.isDisabled = false;
          console.log("allTeamList :", this.allTeamList);
        } else {
          console.error(response.serviceResponse)
        }
      });
    }
  }

  getAllMyTeamsByEmpId() {
    this.allTeamList = [];
    this.allActivityList = [];
    this.filterStatus = "";
    this.selectedProject = "0";

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    employeeObj.departmentName = this.currentUser.departmentName;
    employeeObj.employeeRole = this.currentUser.employeeRole;

    this.teamService.getAllMyTeamsByEmpId(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log("response.serviceResponse :", response.serviceResponse);
        
        this.allTeamList = response.serviceResponse;
        this._allTeamList = this.allTeamList
        this.filterStatus = "Active";
        this.changeEvent();
        this.isDisabled = false;
        console.log("getAllMyTeamsByEmpId -- allTeamList :", this.allTeamList);
      } else {
        console.error(response.serviceResponse)
      }
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
        console.log("teamObj.allTeamMemberList :", this.teamObj.allTeamMemberList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  // Project Details 
  getAllProjectListByProjectManagerId() {
    this.allProjectListByManagerId = [];

    let projectObj = new Project();
    // projectObj.projectManagerId = 184; //! Here we are getting all projects
    this.projectService.getAllProjectListByProjectManagerId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log("Current user : Persona : "+ this.currentUser.employeeRole + " || Department : "+ this.currentUser.departmentName);
        let allProjectList = response.serviceResponse;
        allProjectList = allProjectList.sort((a, b) => a.projectName.localeCompare(b.projectName));
        if(this.currentUser.employeeRole == 'HOD' || this.currentUser.employeeRole == 'SuperAdmin' || this.currentUser.employeeRole == 'HR'){
          this.allProjectListByManagerId = allProjectList;
          console.log("allProjectList For HOD / HR / SuperAdmin :", this.allProjectListByManagerId);
        }else{
          this.allProjectListByManagerId = allProjectList;
          this.allProjectListByManagerId = allProjectList.filter((projectObj:Project) => projectObj.departmentName == this.currentUser.departmentName);
          console.log("allProjectList By Department :", this.allProjectListByManagerId);
        }

        this.allProjectListByManagerId = this.allProjectListByManagerId.sort((a, b) => a.projectName.localeCompare(b.projectName));

        if(this.isTeamTable){
          this.allProjectListByManagerId.unshift({
            projectId : 0,
            projectName : 'My Teams'
          });
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

        // filter list if data is duplicate

        this.employeeSpecificProjectList = this.employeeSpecificProjectList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectName === value.projectName
          ))
        )
        console.log("employeeSpecificProjectList :", this.employeeSpecificProjectList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // For Team Lead
  getAllEmployeesByRole() {
    this.teamLeadsList = [];
    let employeeList = [];

    this.employeeObj.role = "TeamLead";
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList)

        // remove teamLead if their department are not selected.

        let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        this.teamLeadsList = employeeList.filter(x => filterDepartmentList.includes(x.departmentId));
        this.teamLeadsList = this.teamLeadsList.sort((a, b) => a.name.localeCompare(b.name));
        console.log("teamLeadsList : ", this.teamLeadsList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  updateEmployeeListAccordingToTeamMembers() {
    console.log("Existing Team Member : ", this.teamObj.allTeamMemberList);
    console.log("Selected Team Member : ", this.allTeamMembers);
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

  getAllEmployeesByDepartmentIds() {
    this.employeeListByDept = [];

    let empObj = new Employee();
    empObj.departmentList = this.teamObj.departmentList;
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

  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        console.log("allDeptList : ", this.allDeptList)
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
      this.alertMessage = "Please select Team Lead !!"
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
    console.log("create activity : ", this.activityObj);
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
    console.log("update Activity : ", this.activityObj);

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
        console.log("allActivityList :", this.allActivityList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // download excel

  exportToExcel(): void {

    if (this.isTeamTable == true) {
      this.excelName = 'TeamSheet.xlsx';

      this.teamsActivityDataForExcel = this.allTeamList;

      const onlySpecificDataArr = this.teamsActivityDataForExcel.map(
        x => ({
          "Team Name": x.teamName,
          "Team Lead": x.teamLeadId,
          "Created by": x.createdByName,
          "Created on": x.createdOn
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
          "Created by": x.createdBy,
          "Created on": x.createdOn
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

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortTeam(sort: Sort) {
    console.log(sort);

    const data = this.allTeamList;
    if (!sort.active || sort.direction === '') {
      this.allTeamList = data;
      return;
    } else {
      this.allTeamList = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'teamName':
              return compare(a.teamName.toLowerCase(), b.teamName.toLowerCase(), isAsc)
            case 'teamLeadName':
              return compare(a.teamLeadName.toLowerCase(), b.teamLeadName.toLowerCase(), isAsc)
            case 'createdByName':
              return compare(a.createdByName.toLowerCase(), b.createdByName.toLowerCase(), isAsc)
            case 'createdOn':
              return compare(a.createdOn, b.createdOn, isAsc)
            default:
              return 0;
          }
        }
      )
    }
  }
  sortActivity(sort: Sort) {
    console.log(sort);
    const data = this.allActivityList;
    if (!sort.active || sort.direction === '') {
      this.allActivityList = data;
      return;
    } else {
      this.allActivityList = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'activity':
              return compare(a.activity.toLowerCase(), b.activity.toLowerCase(), isAsc)
            case 'eta':
              return compare(a.eta, b.eta, isAsc)
            case 'createdByName':
              return compare(a.createdByName.toLowerCase(), b.createdByName.toLowerCase(), isAsc)
            case 'createdOn':
              return compare(a.createdOn, b.createdOn, isAsc)
            default:
              return 0;
          }
        }
      )
    }
  }
}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}