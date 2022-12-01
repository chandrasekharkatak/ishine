import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Activity } from 'src/app/models/activity';
import { Feature } from 'src/app/models/feature';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { DatePipe } from '@angular/common';
import * as moment from 'moment';
import { Sort } from '@angular/material/sort';
import { ClipboardService } from 'ngx-clipboard';
import { Employee } from 'src/app/models/employee';
import { TeamViewService } from 'src/app/services/team-view.service';

@Component({
  selector: 'app-my-timesheet',
  templateUrl: './my-timesheet.component.html',
  styleUrls: ['./my-timesheet.component.css']
})
export class MyTimesheetComponent implements OnInit {

  data: string;
  feature = "My Timesheets";
  currentUser: User;
  userMapping: any = {};

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isTimesheetForm: boolean = false;
  isTimesheetTable: boolean = false;

  isTimesheetUpdate: boolean = false;
  isTimesheetUpdateCounter = 0;

  isSelfTimesheets: boolean = false;
  isTeamTimesheets: boolean = false;
 
  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj
  timesheetObj: Timesheet = new Timesheet();
  allTimesheetActivities: any[] = []
  allProjectsList: any[] = [];
  allActivityList: any[] = [];

  allMyTimesheets: any[] = [];
  timesheetActivities: any[] = [];
  startDate: any;
  endDate: any;

  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  availableTimesheetDates: any[] = [];
  availableTimesheets: any[] = [];

  projectList:any[] = [];
  clientList:any[] = [];
  clientLocationList:any[] = [];

  teamMemberList:any[] = [];
  errorMsg:any;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private datePipe: DatePipe,
    private clipboardService: ClipboardService,
    private teamViewService : TeamViewService,
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

    this.timesheetObj.timesheetAppliedFor = "self";
    this.timesheetObj.empId = this.currentUser.empId;
    this.sectionViewInit();
  }

  sectionViewInit() {
    if (this.userMapping.add_timesheet) {
      this.showCreateTimesheetForm();
    } else if (this.userMapping.view_my_timesheets || this.userMapping.update_timesheet) {
      this.showViewMyTimesheets()
    }
  }

  disableMannualDateInput() {
    return false;
  }

  showCreateTimesheetForm() {
    this.isTimesheetForm = true;
    this.isCreation = true;

    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.reset();
    this.getAllProjectsByEmpId(this.currentUser);
    this.getAllAvailableTimesheetByEmpId(this.currentUser);
  }

  showViewMyTimesheets() {
    this.isTimesheetTable = true;

    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.showSelfTimesheets();
  }

  showSelfTimesheets(){
    this.isSelfTimesheets = true;
    this.isTeamTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data='';
  }

  showTeamTimesheets(){
    this.isTeamTimesheets = true;
    this.isSelfTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data='';
  }

  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;

    this.timesheetObj = Object.assign({}, timesheetObj);
    this.timesheetObj.updatedTimesheetActivities = [];

    let userObj:User = new User();
    if (this.isSelfTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "self";
      this.timesheetObj.empId = this.currentUser.empId;

      userObj.empId = this.currentUser.empId;
    } else if (this.isTeamTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "team";

      userObj.empId = timesheetObj.empId;
    }
    
    this.getAllProjectsByEmpId(userObj);
    setTimeout(()=>{
      this.getAllMyActivitiesByTimesheetId(timesheetObj);
    },500)
  }

  reset() {
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.timesheetObj.timesheetAppliedFor = "self";
    this.timesheetObj.empId = this.currentUser.empId;
    
    this.allTimesheetActivities = [];
    this.addInputActivityField();;
  }


  addInputActivityField(activityObj?:Activity) {
        console.log("before allTimesheetActivities : " , this.allTimesheetActivities)
        let newActivityObj = new Activity();
        if(activityObj != undefined){
          newActivityObj.clientId = activityObj.clientId;
          newActivityObj.clientLocationId = activityObj.clientLocationId;
          newActivityObj.projectId = activityObj.projectId;
          console.log("newActivityObj : ",newActivityObj);

          this.allTimesheetActivities.push(newActivityObj);
          this.getClientLocationList(newActivityObj);
          this.getProjectList(newActivityObj);
          this.getAllActivitiesByProjectIdandEmpId(newActivityObj);
        }else{
          this.allTimesheetActivities.push(newActivityObj);
        }
        console.log("After allTimesheetActivities : ",this.allTimesheetActivities)
  }

  // Manage Activity

  // --> pREV
  // addInputActivityField() {
  //   let newActivityObj = new Activity();
  //   // newActivityObj.projectId = '';
  //   // newActivityObj.activityId = '';
  //   this.allTimesheetActivities.push(newActivityObj);
  // }

  removeInputActivityField(activityObj: any) {
    this.allTimesheetActivities.forEach((value, index) => {
      if (value == activityObj) {
        if (this.isUpdation) {
          this.timesheetObj.updatedTimesheetActivities.push(value);
        }
        this.allTimesheetActivities.splice(index, 1);
      }
    });
  }

  setAllProjectActivities(activityObj, allActivityList: any) {
    const selectedActivityObj = this.allTimesheetActivities.find(activity => activity === activityObj);
    if(!this.isTimesheetUpdate){
      selectedActivityObj.activityId = '';
    }else{
      this.isTimesheetUpdateCounter--;
      if(this.isTimesheetUpdateCounter === 0) this.isTimesheetUpdate = false;
    } 
    selectedActivityObj.projectActivities = allActivityList;
  } 

  setActivity(activityObj) {
    this.allTimesheetActivities.find(activity => activity === activityObj).activity = activityObj.projectActivities.find(activity => activity.activityId == activityObj.activityId).activity;
  }

  // Manage Timesheet Dates
  timesheetDateFilter = (checkDate: Date) => {
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    const time = checkDate?.getTime();
    let currentDate = new Date();

    // 7 days + 1 current Day
    let endDate = currentDate;
    let startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));

    return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "YYYY-MM-dd"))) ? true : false;
  }

  /* Timesheet */
  validateTimesheetObj(timesheetObj: Timesheet, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.date)) {
      this.alertMessage = "Please enter Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.dayType)) {
      this.alertMessage = "Please select Day Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (timesheetObj.dayType != 'Holiday') {
      let flag = true;
      let totalActivityTime = 0;

      this.allTimesheetActivities.forEach((activity, index) => {

        if(activity.description) activity.description = activity.description?.trim();

        if (!this.validationService.validateNullUndefinedEmptyString(activity.clientId)) {
          this.alertMessage = `Please select Client - ${index + 1}!!`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.clientLocationId)) {
          this.alertMessage = `Please select Client Location - ${index + 1}!!`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.projectId)) {
          this.alertMessage = `Please select Project - ${index + 1}!!`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.activityId)) {
          this.alertMessage = `Please select Activity - ${index + 1}!!`
          flag = false;
          return;
        }
        if(activity.description != ''){
  
        if(!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
          this.alertMessage = `Please enter valid Activity Description  - ${index + 1}!!`
          flag = false;
          return;
        }
      }
        if (!this.validationService.validateNullUndefinedEmptyString(activity.completionTime)) {
          this.alertMessage = `Please enter Activity Completion Time - ${index + 1}!!`
          flag = false;
          return;
        }
        if (!this.validationService.validateTimesheetCompletionTime(activity.completionTime)) {
          this.alertMessage = `Please enter valid Activity Completion Time - ${index + 1}!!`
          flag = false;
          return;
        }
        totalActivityTime = totalActivityTime + activity.completionTime;
        console.log(totalActivityTime, " totalActivityTime");
      });

      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(totalActivityTime <= 0 || totalActivityTime > 24){
        this.alertMessage = 'Total time must be greater than 0 hrs and maximum upto 24 hrs!! '
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.description)) {
        this.alertMessage = "Please enter Timesheet Description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateActivityTimesheetDiscription(timesheetObj.description)) {
        this.alertMessage = `Please enter valid Description  !!`
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    return true;
  }

  onCreateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';

    this.timesheetObj.description = this.timesheetObj.description?.trim();

    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != 'Holiday') {
      console.log("allTimesheetActivities :", this.allTimesheetActivities, this.allTimesheetActivities[0]);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    } else {
      this.timesheetObj.allTimesheetActivities = null;
    }

    this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat)
    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.createdByName = this.currentUser.name
    console.log("Add timesheetObj : ", this.timesheetObj);
    this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        if(this.timesheetObj.timesheetAppliedFor == "self"){
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getAllMyTimesheetsByEmpId();
        }else{
          this.showTeamTimesheets();
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getMyTeamTimesheets();
        }
        
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != 'Holiday') {
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat)
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;

      if (this.timesheetObj.allTimesheetActivities) {
        let newTimesheetActivities = this.timesheetObj.allTimesheetActivities.filter(activity => !activity.timesheetId);
        console.log("newTimesheetActivities : ", newTimesheetActivities);

        if (newTimesheetActivities) {
          if (this.timesheetObj.updatedTimesheetActivities === undefined || this.timesheetObj.updatedTimesheetActivities.length === 0) {
            this.timesheetObj.updatedTimesheetActivities = [];
            
          }
          this.timesheetObj.updatedTimesheetActivities = this.timesheetObj.updatedTimesheetActivities.concat(newTimesheetActivities);
        }
      } else {
        if (this.timesheetObj.updatedTimesheetActivities == undefined || this.timesheetObj.updatedTimesheetActivities[0].length == 0) {
          this.timesheetObj.updatedTimesheetActivities = null;
        }
      }
    } else {
      this.timesheetObj.updatedTimesheetActivities = null;
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    console.log("Update timesheetObj : ", this.timesheetObj);
    this.timesheetService.updateTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        if(this.timesheetObj.timesheetAppliedFor == "self"){
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getAllMyTimesheetsByEmpId();
        }else{
          this.showTeamTimesheets();
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getMyTeamTimesheets();
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  __tempDescription = '';
  onTimesheetDescriptionChange(){
    if(this.isUpdation){
      if(this.timesheetObj.dayType == 'Holiday'){
        this.timesheetObj.description = this.__tempDescription;
        this.__tempDescription = '';
      }else{
        this.__tempDescription = (this.timesheetObj.description != null) ? this.timesheetObj.description : ''; 
        this.timesheetObj.description = '';
      }
    }
  }

  getTimesheetMetadata(){
    console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);
    let userObj:User = new User();
    if(this.timesheetObj.timesheetAppliedFor == 'self'){
      userObj.empId = this.currentUser.empId;
      this.timesheetObj.empId = this.currentUser.empId; 
    }else{
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      console.log("Team Member : ", teamMember);
      userObj.empId = teamMember.empId;
      this.timesheetObj.empId = teamMember.empId; 
    }

    const timesheetBkp = Object.assign({},this.timesheetObj);
    
    // reset timesheet 
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.allTimesheetActivities = [];
    this.addInputActivityField()

    // set leave AppliedFor User data to fetch activities for project & for display 
    this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
    this.timesheetObj.empId = timesheetBkp.empId;
    
    console.log("preset Timesheet : ", this.timesheetObj);

    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
  }

  getAllTeamMemberList(){
    this.teamMemberList = []

    if(this.timesheetObj.timesheetAppliedFor == "team"){
      let employeeObj = new Employee();
      employeeObj.empId = this.currentUser.empId;
      this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response : any) => {
        if (response.serviceStatus == "Success") {
          this.teamMemberList = response.serviceResponse;
          console.log("teamMemberList : ", this.teamMemberList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

  }

  getAllProjectsByEmpId(employeeObj:User) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];

    
    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log("allProjectsList :", this.allProjectsList);
        const key = "clientId";
        this.clientList = [...new Map(this.allProjectsList.map((project:Timesheet) => [project[key], project])).values()].map((project:Timesheet) => {
          return { clientId: project.clientId, clientName: project.clientName}
        });
        console.log("clientList :", this.clientList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getProjectList(activityObj: Activity){
    this.projectList = [];

    const key = "projectId";
    this.projectList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
      if (project.clientId == activityObj.clientId) {
        return { projectId: project.projectId, projectName: project.projectName }
      }
    });
    console.log("projectList :", this.projectList);
    this.setAllProjects(activityObj, this.projectList);
  }

  setAllProjects(activityObj, projectList: any) {
    const selectedActivityObj:Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    if(!this.isTimesheetUpdate && activityObj.projectId == ""){
      selectedActivityObj.projectId = '';
    }
    selectedActivityObj.projectList = projectList;
  } 

  getClientLocationList(activityObj: any){
    this.clientLocationList = [];

    const key = "clientLocationId";
    this.clientLocationList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
      if (project.clientId == activityObj.clientId) {
        return { clientLocationId: project.clientLocationId, clientLocation: project.clientLocation }
      }
    });
    console.log("clientLocationList :", this.clientLocationList);
    this.setAllClientLocations(activityObj, this.clientLocationList)
  }

  setAllClientLocations(activityObj, clientLocationList: any) {
    const selectedActivityObj:Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    if(!this.isTimesheetUpdate && activityObj.clientLocationId == ""){
      selectedActivityObj.clientLocationId = '';
    }
    selectedActivityObj.clientLocationList = clientLocationList;
  } 


  getAllActivitiesByProjectIdandEmpId(activityObj: any) {
    let allActivityList = [];

    console.log("Current Timesheet : ", this.timesheetObj);
    
    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.timesheetObj.empId;
    timesheetObj.projectId = activityObj.projectId;
    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        allActivityList = response.serviceResponse;
        console.log("allActivityList :", allActivityList);
      } else {
        console.error(response.serviceResponse)
      }
      this.setAllProjectActivities(activityObj, allActivityList);
    });
  }

  getAllAvailableTimesheetByEmpId(employeeObj:User) {
    this.availableTimesheets = [];
    console.log(" -- logged availableTimesheets -- ");

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    this.timesheetService.getbackdatedTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
        console.log("availableTimesheets :", this.availableTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getTimesheetData(template:TemplateRef<any>){

    if (this.endDate < this.startDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      console.log("end date is small");
      this.endDate = ''
      
    } else {
      this.allMyTimesheets = [];
    }
  }

  /* View Timesheets */
  getAllMyTimesheetsByEmpId(template?: TemplateRef<any>) {
    this.allMyTimesheets = [];


    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;
    
    console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allMyTimesheets = response.serviceResponse;
        console.log("allMyTimesheets :", this.allMyTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Timesheets Applied By ME for My Team Members */
  getMyTeamTimesheets(template?: TemplateRef<any>) {
    this.allMyTimesheets = [];

    if (this.endDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      return;
    }

    let timesheetObj = new Timesheet();
    timesheetObj.createdBy = this.currentUser.empId;
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;
    
    console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetService.getAllMyTeamTimesheets(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allMyTimesheets = response.serviceResponse;
        console.log("allMyTimesheets :", this.allMyTimesheets);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  resetToDate(){
    this.endDate = ''
    this.allMyTimesheets=[]; 
  }

  getAllMyActivitiesByTimesheetId(timesheet: any) {
    this.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTimesheetActivities = response.serviceResponse;
        console.log("allTimesheetActivities :", this.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }

      if (this.allTimesheetActivities.length == 0) {
        this.addInputActivityField();
      } else {
        if(this.isTimesheetUpdate) this.isTimesheetUpdateCounter = this.allTimesheetActivities.length;
        this.allTimesheetActivities.forEach(activity => {
          this.getAllActivitiesByProjectIdandEmpId(activity)
          this.getClientLocationList(activity);
          this.getProjectList(activity);
        });
      }
    });
  }

  viewAllMyActivitiesByTimesheetId(timesheet: any) {
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  exportToExcel(): void {

    if (this.isTimesheetTable == true) {
      this.excelName = 'MyTimeSheet.xlsx'

      this.allMyTimesheetsDataForExcel = this.allMyTimesheets;

      const onlySpecificDataArr = this.allMyTimesheetsDataForExcel.map(
        x => ({
          "Date": x.date,
          "Day Type": x.dayType,
          "Timesheet Details":x.description,
          "Total Time":x.totalTime,
          "Status": x.status
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

  }








  //modals
  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.viewAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  async copyTimesheetDetailsToNotepad(timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;

    let content = "Employment Id: A-" + this.currentUser.employeementId
      + " Name: " + this.currentUser.name
      + " Date: " + this.timesheetObj.date
      + " Daytype: " + this.timesheetObj.dayType
      + " ";

    const response: any = await this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).toPromise();

    if (response.serviceStatus == "Success") {
      let activityList = response.serviceResponse;
      let totalActivity = "";
      activityList.forEach((eodActivity, index) => {
        index = index + 1;
        totalActivity += index + ")" + "Project Name: " + eodActivity.projectName + " Activity: " + eodActivity.activity
          + " description: " + eodActivity.description + " Time taken: " + eodActivity.completionTime + "hrs. ";
      })
      this.clipboardService.copy(content + " " + totalActivity);
    } else if (response.serviceResponse = "No activities found.Activity list is empty") {
      this.clipboardService.copy(content + " " + " description: " + timesheetObj.description)
    }
  }

  validateNullUndefinedEmptyDescription(event,data:any){
 
  if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Description !!"   
  }
  else if (!this.validationService.validateActivityTimesheetDiscription(data)) {
    this.errorMsg = "Please enter valid Description !!"   
  }
  else{
    this.errorMsg = ""
  }
  if(this.errorMsg == ""){
    event.target.nextElementSibling.textContent = ""
  }else{
    event.target.nextElementSibling.textContent =  this.errorMsg
  }
  }
  // validateTime(event,data:any){
  //   if (!this.validationService.validateNullUndefinedEmptyString(data)) {
  //     this.errorMsg = "Please enter Time !!"
  //   }  
  //   else if(data <= 0 || data > 24){
  //     this.errorMsg ="Total time must be greater than 0 hrs and maximum upto 24 hrs!! "
  // } 
  // else{
  //   this.errorMsg = ""
  // }
  // if(this.errorMsg == ""){
  //   event.target.nextElementSibling.textContent = ""
  // }else{
  //   event.target.nextElementSibling.textContent =  this.errorMsg
  // }

  // }
  // validateNullUndefinedEmptyclientId(event,data:any){
  //   if (!this.validationService.validateNullUndefinedEmptyString(data)) {
  //     this.errorMsg = "Please enter Timessssssss !!"
  //   } 

  // }
  // validateNullUndefinedEmptyActivity(event,data:any){
  //   if (!this.validationService.validateNullUndefinedEmptyString(data)) {
  //     this.errorMsg = "Please enter Time !!"
  //   } 

  // }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // sorting .....	
  sortMyTimesheet(sort: Sort) {
    console.log(sort);
    const data = this.allMyTimesheets;
    if (!sort.active || sort.direction === '') {
      this.allMyTimesheets = data;
      return;
    } else {
      this.allMyTimesheets = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'date':
              return compare(a.date, b.date, isAsc)
            case 'dayType':
              return compare(a.dayType, b.dayType, isAsc)
            case 'status':
              return compare(a.status, b.status, isAsc)
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

