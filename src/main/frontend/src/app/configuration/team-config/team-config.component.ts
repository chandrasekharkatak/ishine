import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
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
  employeeListByDept: any[] = [];
  teamLeadsList: any[] = [];


  allDeptList: any[] = [];
  allProjectListByManagerId: any[] = [];
  allTeamList: any[] = [];

  //excel
  teamDataForExcel: any[];
  teamsByProjectIdDataForExcel: any[];
  teamsActivityDataForExcel: any[];

  selectedClient: any = '';
  selectedProject: any = '';
  selectedTeam: any = '';
  excelName = '';
  tableElement = '';



  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private projectService: ProjectService,
    private teamService: TeamService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,
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
    this.getAllDepartmentList();
    this.getAllEmployeesByRole();
    this.getAllProjectListByProjectManagerId();
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

    this.allTeamList = [];
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
    this.addInputTeamMemberField();
  }

  showCreateActivityForm() {
    this.isActivityForm = true;
    this.isCreation = true;

    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isUpdation = false;

    this.reset();
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

    this.allActivityList = [];
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
    this.addInputTeamMemberField();
  }

  // Manage team members
  addInputTeamMemberField() {
    let newTeamMemberObj = new TeamMember();
    // newTeamMemberObj.empId = '';
    this.allTeamMembers.push(newTeamMemberObj);
  }

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember) this.allTeamMembers.splice(index, 1);
    });
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

    if (!this.validationService.validateNullUndefinedEmptyString(teamObj.teamLeadId)) {
      this.alertMessage = "Please select Team Lead !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(teamObj.departmentList)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }


  onCreateTeam(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateTeamObj(this.teamObj, template)
    if (!inputValidated) return;

    console.log("allTeamMembers :", this.allTeamMembers, this.allTeamMembers[0]);
    this.teamObj.allTeamMemberList = (Object.keys(this.allTeamMembers[0]).length === 0) ? null : this.allTeamMembers;
    this.teamObj.createdBy = this.currentUser.empId;
    console.log("create teamObj : ", this.teamObj);
    return;
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

  removeTeamMember(teamMember) {
    console.log("this.teamObj : ", this.teamObj);
    console.log();


    this.teamObj.allTeamMemberList?.forEach((value, index) => {
      if (value == teamMember) {
        if (this.teamObj.updatedTeamMemberList[0]) {
          this.teamObj.updatedTeamMemberList.push(value);
        } else {
          this.teamObj.updatedTeamMemberList = [];
          this.teamObj.updatedTeamMemberList.push(value);
        }
        this.teamObj.allTeamMemberList.splice(index, 1);
      }
    });
    console.log("Updated Team Members : ", this.teamObj.updatedTeamMemberList);

  }

  onUpdateTeam(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateTeamObj(this.teamObj, template)
    if (!inputValidated) return;

    this.allTeamMembers = (Object.keys(this.allTeamMembers[0]).length === 0) ? null : this.allTeamMembers;

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

    let teamObj = new Team();
    teamObj.projectId = projectId;
    this.teamService.getAllTeamsByProjectId(teamObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamList = response.serviceResponse;
        this.allTeamList = this.allTeamList.sort(function (a, b) {
          return a.teamName.toLowerCase().localeCompare(b.teamName.toLowerCase());
        });

        this.isDisabled = false;
        console.log("allTeamList :", this.allTeamList);
      } else {
        console.error(response.serviceResponse)
      }
    });
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
    projectObj.projectManagerId = 5; //! Temp
    this.projectService.getAllProjectListByProjectManagerId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectListByManagerId = response.serviceResponse;
        console.log("allProjectListByManagerId :", this.allProjectListByManagerId);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // For Team Lead
  getAllEmployeesByRole() {
    this.teamLeadsList = [];
    let employeeList = [];

    this.employeeObj.role = "Team Lead";
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList)
        this.teamLeadsList = employeeList;
        console.log("teamLeadsList : ", this.teamLeadsList)
      } else {
        console.error(response.serviceResponse)
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
        console.log("employeeList By Department : ", this.employeeListByDept);
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

    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.activity)) {
      this.alertMessage = "Please enter Activity !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(activityObj.eta)) {
      this.alertMessage = "Please enter Activity ETA (Hours)!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (activityObj.eta < 0 || activityObj.eta > 999) {
      this.alertMessage = "Please enter Valid Activity ETA (Hours) between 0-999 Hours!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

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
          "Team Lead Id": x.teamLeadId,
          "Created By": x.createdByName,
          "Created On": x.createdOn
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);

    }
    if (this.isActivityTable == true) {
      this.excelName = 'ActivitiesSheet.xlsx';

      this.teamsByProjectIdDataForExcel = this.allActivityList;

      const onlySpecificDataArr: Partial<Activity>[] = this.teamsByProjectIdDataForExcel.map(
        x => ({
          activity: x.activity,
          eta: x.eta,
          createdBy: x.createdBy,
          createdOn: x.createdOn
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);

    }
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