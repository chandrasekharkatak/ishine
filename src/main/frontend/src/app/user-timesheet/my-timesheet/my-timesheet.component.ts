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
import { DatePipe, LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { Sort } from '@angular/material/sort';
import { ClipboardService } from 'ngx-clipboard';
import { Employee } from 'src/app/models/employee';
import { TeamViewService } from 'src/app/services/team-view.service';
import { LeaveService } from 'src/app/services/leave.service';
import { Leave } from 'src/app/models/leave';
import { Team } from 'src/app/models/team';
import { ThemePalette } from '@angular/material/core';
import { AppComponent } from 'src/app/app.component';
import { EmployeeService } from 'src/app/services/employee.service';

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

  projectList: any[] = [];
  clientList: any[] = [];
  clientLocationList: any[] = [];
  // teamList:any[] = [];

  teamMemberList: any[] = [];
  errorMsg: any;

  leaveHistoryList: any[] = [];
  maxOutTimeDate: any;
 
  isTimesheetLockCheckEnable:any = "true";

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private datePipe: DatePipe,
    private clipboardService: ClipboardService,
    private teamViewService : TeamViewService,
    private leaveService : LeaveService,
    private locationStrategy: LocationStrategy,
    private employeeService : EmployeeService,
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
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.getAllMyLeaveApplicationsByEmpId(this.currentUser);
    this.getEmployeeBasicInfo();	
    this.sectionViewInit();
    this.preventBackButton();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
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
    this.getTimesheetMetadata();
  }

  showViewMyTimesheets() {
    this.isTimesheetTable = true;

    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.showSelfTimesheets();
  }

  showSelfTimesheets() {
    this.isSelfTimesheets = true;
    this.isTeamTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data = '';
  }

  showTeamTimesheets() {
    this.isTeamTimesheets = true;
    this.isSelfTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data = '';
  }

  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;

    this.timesheetObj = Object.assign({}, timesheetObj);
    this.timesheetObj.updatedTimesheetActivities = [];
    this.timesheetObj.date = (this.timesheetObj.date)? moment(timesheetObj.date, "DD-MM-YYYY").toDate() : '';
    this.timesheetObj.officeInTime = (this.timesheetObj.officeInTime)? moment(timesheetObj.officeInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.officeOutTime = (this.timesheetObj.officeOutTime)? moment(timesheetObj.officeOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.createdOn = (this.timesheetObj.createdOn)? moment(timesheetObj.createdOn, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.dayType = (this.timesheetObj.dayType == "Holiday")? "Week Off" : this.timesheetObj.dayType;

    if (this.timesheetObj.officeInTime) {
      this.maxOutTimeDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());
    }

    if(this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave"){
      this.__tempDescription = this.timesheetObj.description;
    }

    let userObj: User = new User();
    if (this.isSelfTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "self";
      this.timesheetObj.empId = this.currentUser.empId;

      userObj.empId = this.currentUser.empId;
    } else if (this.isTeamTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "team";

      userObj.empId = timesheetObj.empId;
    }
    this.getAllProjectsByEmpId(userObj);
    this.getTimesheetMetadata();
    setTimeout(()=>{
      this.getAllMyActivitiesByTimesheetId(timesheetObj);
    }, 500)
  }

  reset() {
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.timesheetObj.timesheetAppliedFor = "self";
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.allTimesheetActivities = [];
    this.addInputActivityField();
  }


  addInputActivityField(activityObj?: Activity) {

    console.log("before allTimesheetActivities : ", this.allTimesheetActivities)
    let newActivityObj = new Activity();
    if (activityObj != undefined) {
      newActivityObj.clientId = activityObj.clientId;
      newActivityObj.clientLocationId = activityObj.clientLocationId;
      newActivityObj.projectId = activityObj.projectId;
      newActivityObj.teamId = activityObj.teamId;
      console.log("newActivityObj : ", newActivityObj);

      this.allTimesheetActivities.push(newActivityObj);
      this.getClientLocationList(newActivityObj);
      this.getProjectList(newActivityObj);
      // this.getTeamList(newActivityObj)
      this.getAllActivitiesByProjectIdandEmpId(newActivityObj);
    } else {
      this.allTimesheetActivities.push(newActivityObj);
    }
    console.log("After allTimesheetActivities : ", this.allTimesheetActivities)
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
    if (!this.isTimesheetUpdate) {
      selectedActivityObj.activityId = '';
    } else {
      this.isTimesheetUpdateCounter--;
      if (this.isTimesheetUpdateCounter === 0) this.isTimesheetUpdate = false;
    }
    selectedActivityObj.projectActivities = allActivityList;
  }

  setActivity(activityObj) {
    this.allTimesheetActivities.find(activity => activity === activityObj).activity = activityObj.projectActivities.find(activity => activity.activityId == activityObj.activityId).activity;
    activityObj.description = null;
    console.log("Activity obj : ", activityObj)
    // console.log("Activity : ",activity)
  }

  // Manage Timesheet Dates
  timesheetDateFilter = (checkDate: Date) => {
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    const time = checkDate?.getTime();
    let currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let OPEN_BACKDATED_DAYS = 30;
    const CURRENT_DAY = 1;

    if(this.currentUser.timesheetBackDatedDays){
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
    }

    // timesheetLockDays (days) + 1 current Day
    let endDate = currentDate;
    let startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + CURRENT_DAY) * DAY_IN_MS));

    if (this.isTimesheetForm && this.isUpdation) {
      this.availableTimesheets = this.availableTimesheets.filter(timesheet => this.datePipe.transform(timesheet.date, "yyyy-MM-dd") != this.datePipe.transform(this.timesheetObj.date, "yyyy-MM-dd"));
    }

    console.log("isTimesheetLockCheckEnable : ", this.isTimesheetLockCheckEnable);
  

    if(this.isTimesheetLockCheckEnable == "false"){
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + CURRENT_DAY) * DAY_IN_MS));
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "YYYY-MM-dd"))) ? true : false;	
    }else{
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "YYYY-MM-dd"))) ? true : false;	
    }
  } 

  outTimeFilter = (checkDate: Date) => {
    const dateFormat = 'YYYY-MM-DD';

    let startDate = this.timesheetObj.officeInTime;
    let endDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());

    return (moment(checkDate).format(dateFormat) <= moment(endDate).format(dateFormat) && moment(checkDate).format(dateFormat) >= moment(startDate).format(dateFormat)) ? true : false;
  }

  setMaxInTimeDate(timesheetDate: any) {
    console.log("timesheetDate : ", moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL));
    
    if(this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave"){
      let inTimeDate = document.getElementById('officeInTime');
      let officeOutTime = document.getElementById('officeOutTime');
      inTimeDate.setAttribute('min', `${moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL)}`);
      officeOutTime.setAttribute('min', `${moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL)}`);
    }
  }

  resetTotalWorkingOfficeHours() {
    this.timesheetObj.officeOutTime = '';
    this.timesheetObj.totalWorkingOfficeHours = '';

    this.maxOutTimeDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());
  };

  resetTimeonDayTypeChange(){
    if(this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave"){
      this.timesheetObj.officeInTime = '';
      this.timesheetObj.officeOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
  }

  setTotalWorkingOfficeHours() {
    const dateFormat = 'YYYY-MM-DD';
    if (this.timesheetObj.officeOutTime) {
      console.log("officeInTime : ", this.timesheetObj.officeInTime);
      console.log("officeOutTime : ", this.timesheetObj.officeOutTime);

      let start = moment(this.timesheetObj.officeInTime).format('DD-MM-YYYY HH:mm');
      let end = moment(this.timesheetObj.officeOutTime).format('DD-MM-YYYY HH:mm');

      //const duration = moment.utc(moment(end,"yyyy-MM-DD HH:mm:ss").diff(moment(start,"yyyy-MM-DD HH:mm:ss"))).format("HH:mm");

      let ms = moment(end, "DD-MM-YYYY HH:mm").diff(moment(start, "DD-MM-YYYY HH:mm"));
      let d = moment.duration(ms);
      console.log("d : ", d);

      let duration = Math.floor(d.asHours()) + moment.utc(ms).format(":mm");

      this.timesheetObj.totalWorkingOfficeHours = duration;
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
    } else {
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
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

    if (timesheetObj.dayType != "Public Holiday" && timesheetObj.dayType != "Week Off" && timesheetObj.dayType != "Leave") {
      let flag = true;
      let totalActivityTime = 0;

      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.officeInTime)) {
        this.alertMessage = "Please enter In Date-Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.officeOutTime)) {
        this.alertMessage = "Please select Out Date-Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      this.allTimesheetActivities.forEach((activity, index) => {

        if (activity.description) activity.description = activity.description?.trim();

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

        // if (!this.validationService.validateNullUndefinedEmptyString(activity.projectId)) {
        //   this.alertMessage = `Please select Project - ${index + 1}!!`
        //   flag = false;
        //   return;
        // }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.activityId)) {
          this.alertMessage = `Please select Activity - ${index + 1}!!`
          flag = false;
          return;
        }
        if (activity.description != '') {

          if (!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
            this.alertMessage = `Please enter valid Activity Description  - ${index + 1}!!`
            flag = false;
            return;
          }
        }
        if (!this.validationService.validateCompletionTime(activity.completionTime) && !this.validationService.validateExperiencedNumber(activity.completionTime)) {
          this.alertMessage = `Please enter valid Activity Completion Time - ${index + 1}!!`
          flag = false;
          activity.completionTime = ''
          return;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(activity.completionTime)) {
          this.alertMessage = `Please enter Activity Completion Time - ${index + 1}!!`
          flag = false;
          return;
        }
        // validateCompletionTime
        // if (!this.validationService.validateTimesheetCompletionTime(activity.completionTime)) {
        //   this.alertMessage = `Please enter valid Activity Completion Time - ${index + 1}!!`
        //   flag = false;
        //   return;
        // }
        totalActivityTime = totalActivityTime + activity.completionTime;
        console.log(totalActivityTime, " totalActivityTime");
      });

      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (totalActivityTime <= 0 || totalActivityTime > 24) {
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
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

    this.timesheetObj.description = this.timesheetObj.description?.trim();

    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      console.log("allTimesheetActivities :", this.allTimesheetActivities, this.allTimesheetActivities[0]);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    } else {
      this.timesheetObj.allTimesheetActivities = null;
    }

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
    } else {
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.createdByName = this.currentUser.name
    console.log("Add timesheetObj : ", this.timesheetObj);
    this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        if (this.timesheetObj.timesheetAppliedFor == "self") {
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getAllMyTimesheetsByEmpId();
        } else {
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
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';
    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
      this.timesheetObj.createdOn = moment(this.timesheetObj.createdOn).format(dateTimeFormat);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;

      if (this.timesheetObj.allTimesheetActivities) {
        let newTimesheetActivities = this.timesheetObj.allTimesheetActivities.filter(activity => !activity.timesheetId);
        console.log("newTimesheetActivities : ", newTimesheetActivities);

        if (newTimesheetActivities) {
          if (this.timesheetObj.updatedTimesheetActivities === undefined || this.timesheetObj.updatedTimesheetActivities.length === 0) {
            this.timesheetObj.updatedTimesheetActivities = [];

          }
          this.timesheetObj.updatedTimesheetActivities.forEach(timesheet => {
            if (timesheet.activityId == "") {
              this.timesheetObj.updatedTimesheetActivities.splice(timesheet, 1);
            }
          });
          this.timesheetObj.updatedTimesheetActivities = this.timesheetObj.updatedTimesheetActivities.concat(newTimesheetActivities);
        }
      } else {
        if (this.timesheetObj.updatedTimesheetActivities == undefined || this.timesheetObj.updatedTimesheetActivities[0].length == 0) {
          this.timesheetObj.updatedTimesheetActivities = null;
        }
      }
    } else {
      this.timesheetObj.updatedTimesheetActivities = null;
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
      this.timesheetObj.createdOn = moment(this.timesheetObj.createdOn).format(dateTimeFormat);
      this.timesheetObj.officeInTime = '';
      this.timesheetObj.officeOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    console.log("Update timesheetObj : ", this.timesheetObj);
    this.timesheetService.updateTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        if (this.timesheetObj.timesheetAppliedFor == "self") {
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getAllMyTimesheetsByEmpId();
        } else {
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
      if(this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave"){
        this.timesheetObj.description = (this.__tempDescription != null)? this.__tempDescription : '';
        if(this.timesheetObj.description){
          this.__tempDescription = this.timesheetObj.description; 
        }
      } else if(this.timesheetObj.dayType == "Working" || this.timesheetObj.dayType == "Non-working"){
        this.__tempDescription = (this.timesheetObj.description != null) ? this.timesheetObj.description : '';
        this.timesheetObj.description = '';
      }
    }
  }

  getTimesheetMetadata() {
    console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);
    let userObj: User = new User();
    if (this.timesheetObj.timesheetAppliedFor == 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = this.currentUser.empId; 
      this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      console.log("this.currentUser  : ", this.currentUser);
      console.log("userObj  : ", userObj);

    }else{
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      console.log("Team Member : ", teamMember);
      userObj.empId = teamMember.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = teamMember.empId; 
    }

    const timesheetBkp = Object.assign({}, this.timesheetObj);

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

  getAllTeamMemberList() {
    this.teamMemberList = []
    this.timesheetObj.date = ''
    this.timesheetObj.dayType = ''
    this.timesheetObj.officeInTime = ''
    this.timesheetObj.officeOutTime = ''
    this.timesheetObj.totalWorkingOfficeHours = ''
    this.allTimesheetActivities.forEach((timesheet) => {
      timesheet.clientId = ''
      timesheet.clientLocationId = ''
      timesheet.teamId = ''
      timesheet.activityId = ''
      timesheet.description = ''
      timesheet.completionTime = ''
    })

    if (this.timesheetObj.timesheetAppliedFor == "team") {
      this.errorMsg = '';
      let employeeObj = new Employee();
      employeeObj.empId = this.currentUser.empId;
      this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.teamMemberList = response.serviceResponse;
          console.log("teamMemberList : ", this.teamMemberList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

  }

  getAllProjectsByEmpId(employeeObj: User) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];
    // this.teamList = [];
    // console.log(" team list :    ", this.teamList)

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log("allProjectsList :", this.allProjectsList);
        const key = "clientId";
        this.clientList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].map((project: Timesheet) => {
          return { clientId: project.clientId, clientName: project.clientName }
        });
        console.log("clientList :", this.clientList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getProjectList(activityObj: Activity) {
    this.projectList = [];

    const key = "teamId";
    this.projectList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
      if (project.clientId == activityObj.clientId) {
        return { teamId: project.teamId, teamName: project.teamName, projectName: project.projectName }
      }
    });
    console.log("projectList :", this.projectList);
    this.setAllProjects(activityObj, this.projectList);
  }

  // getTeamList(activityObj: Activity){
  //   this.teamList = []

  //   const key = "teamId"
  //   this.teamList = [...new Map(this.allProjectsList.map((team: Timesheet) => [team[key], team])).values()].filter((team: Timesheet) =>{
  //     if( team.teamId == activityObj.teamId) {
  //       return { teamId: team.teamId, teamName: team.teamName }
  //     }
  //   });
  //   console.log(" teamList :", this.teamList);
  //   this.setAllTeams(activityObj , this.teamList)
  // }

  // setAllTeams(activityObj, teamList: any){
  //   const selectedActivityObj:Activity = this.allTimesheetActivities.find( activity => activity == activityObj);
  //   selectedActivityObj.teamList = teamList;
  //   if((!this.isTimesheetUpdate && activityObj.teamId == "") || !this.teamList.find(team => team.teamId == selectedActivityObj.teamId)){
  //     selectedActivityObj.teamId = '';
  //   }
  // }


  setAllProjects(activityObj, projectList: any) {
    const selectedActivityObj: Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    selectedActivityObj.projectList = projectList;
    if ((!this.isTimesheetUpdate && activityObj.teamId == "") || !this.projectList.find(project => project.teamId == selectedActivityObj.teamId)) {
      selectedActivityObj.teamId = '';
    }
  }

  getClientLocationList(activityObj: any) {
    this.clientLocationList = [];

    // this.allTimesheetActivities.find(activity => activity == activityObj).clientLocationId = '';
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
    const selectedActivityObj: Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    selectedActivityObj.clientLocationList = clientLocationList;

    console.log("clientLocationList : ", clientLocationList);

    if ((!this.isTimesheetUpdate && activityObj.clientLocationId == "") || !this.clientLocationList.find(clientLocation => clientLocation.clientLocationId == selectedActivityObj.clientLocationId)) {
      selectedActivityObj.clientLocationId = '';
    }
  }

  getAllActivitiesByProjectIdandEmpId(activityObj: any) {
    let allActivityList = [];

    console.log("Current Timesheet : ", this.timesheetObj);

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.timesheetObj.empId;
    timesheetObj.teamId = activityObj.teamId;
    console.log(this.allProjectsList, " : all project list");
    console.log(timesheetObj.teamId, " : timesheetObj.teamId");
    
    
    let projectTimesheet = this.allProjectsList.find(project => project.teamId == timesheetObj.teamId);
    timesheetObj.projectId = projectTimesheet.projectId;
    timesheetObj.clientId = this.timesheetObj.clientId;
    timesheetObj.clientLocationId = this.timesheetObj.clientLocationId;
    console.log(" timesheetObj  :  ", timesheetObj)

    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        allActivityList = response.serviceResponse;
        console.log("Team name :  ", timesheetObj.teamName);
        console.log("allActivityList :", allActivityList);
        if(this.timesheetObj.timesheetAppliedFor == "team"){
          let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
          allActivityList = allActivityList.filter(x => x.departmentList?.map(x=>+x).includes(teamMember.departmentId));
        }else{
          allActivityList = allActivityList.filter(x => x.departmentList?.map(x=>+x).includes(this.currentUser.departmentId));
        }
      } else {
        console.error(response.serviceResponse)
      }
      this.setAllProjectActivities(activityObj, allActivityList);
    });
  }

  // getAllAvailableTimesheetByEmpId(employeeObj: User) {
  //   this.availableTimesheets = [];
  //   console.log(" -- logged availableTimesheets -- ");

  //   let timesheetObj = new Timesheet();
  //   timesheetObj.empId = employeeObj.empId;
  //   this.timesheetService.getbackdatedTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.availableTimesheets = response.serviceResponse;
  //       console.log("availableTimesheets :", this.availableTimesheets);
  //     } else {
  //       console.error(response.serviceResponse)
  //     }
  //   });
  // }


  getAllAvailableTimesheetByEmpId(employeeObj: User) {
    this.availableTimesheets = [];
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    let currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let endDate:any;
    let startDate:any;
    let OPEN_BACKDATED_DAYS = 30;

    if(this.currentUser.timesheetBackDatedDays){
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
    }

    if(this.isTimesheetLockCheckEnable == 'false'){
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + 1) * DAY_IN_MS));
    }else{
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));
    }

    let timesheetObj = new Timesheet();
      timesheetObj.empId = employeeObj.empId;
      timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
      timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);
      
      console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
      this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.availableTimesheets = response.serviceResponse;
          console.log("availableTimesheets :", this.availableTimesheets);
        } else {
          console.error(response.serviceResponse)
        }
      });
  }

  // getTimesheetData(template:TemplateRef<any>){

  //   if (this.endDate < this.startDate) {
  //     if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
  //       this.alertMessage = "Please enter Start Date !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }

  //     if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
  //       this.alertMessage = "Please enter End Date !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }
  //     console.log("end date is small");
  //     this.endDate = ''

  //   } else {
  //     this.allMyTimesheets = [];
  //   }
  // }

  /* View Timesheets */
  getAllMyTimesheetsByEmpId(template?: TemplateRef<any>) {
    this.allMyTimesheets = [];
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
      this.startDate = ''

    } else {

      let timesheetObj = new Timesheet();
      timesheetObj.empId = this.currentUser.empId;
      timesheetObj.startDate = this.startDate;
      timesheetObj.endDate = this.endDate;

      console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
      this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allMyTimesheets = response.serviceResponse;
          this.allMyTimesheets.forEach(timesheet => {
            timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
            timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          console.log("allMyTimesheets :", this.allMyTimesheets);
        } else {
          console.error(response.serviceResponse)
        }
      });

    }
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
        this.allMyTimesheets.forEach(timesheet => {
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("allMyTimesheets :", this.allMyTimesheets);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  resetToDate() {
    this.endDate = ''
    this.allMyTimesheets = [];
  }

  getAllMyActivitiesByTimesheetId(timesheet: any) {
    this.allTimesheetActivities = [];

    console.log("timesheet : ", timesheet);


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
        if (this.isTimesheetUpdate) this.isTimesheetUpdateCounter = this.allTimesheetActivities.length;
        this.allTimesheetActivities.forEach(activity => {
          this.getAllActivitiesByProjectIdandEmpId(activity)
          this.getClientLocationList(activity);
          this.getProjectList(activity);
          // this.getTeamList(activity);
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

  getEmployeeBasicInfo() {
    let employeeObj = new Employee();
    employeeObj.email = this.currentUser.email;
    this.employeeService.getEmployeeBasicInfo(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let employeeInfo = response.serviceResponse;
        this.currentUser.isTimesheetLockCheckEnable = JSON.parse(JSON.stringify(employeeInfo.isTimesheetLockCheckEnable));
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.isTimesheetLockCheckEnable = employeeInfo.isTimesheetLockCheckEnable;
        console.log("isTimesheetLockCheckEnable : ", this.isTimesheetLockCheckEnable);
        this.getTimesheetMetadata();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  exportToExcel(): void {

    if (this.isTimesheetTable == true) {
      this.excelName = 'MyTimeSheet.xlsx'

      const _allEmployeeList = this.allMyTimesheets.slice()
      this.allMyTimesheetsDataForExcel = _allEmployeeList.sort((a, b) => (new Date(a.date).getTime() > new Date(b.date).getTime())? 1 : -1);

      const onlySpecificDataArr = this.allMyTimesheetsDataForExcel.map(
        x => ({
          "Date": x.date,
          "Day Type": x.dayType,
          "In Time": x.officeInTime,
          "Out Time": x.officeOutTime,
          "Total Working Hours": x.totalWorkingOfficeHours,
          "Timesheet Details": x.description?.replaceAll('<br>', ' \n'),
          "Total Activity Time": x.totalTime,
          "Status": x.status,
          "Applied By": x.createdByName,
          "Applied On": x.createdOn,
          "Remarks": x.remarks
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

  }

  getAllMyLeaveApplicationsByEmpId(userObj: User) {
    this.leaveHistoryList = [];
    let leaveObj = new Leave();
    leaveObj.empId = userObj.empId;
    this.leaveService.getAllMyLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveHistoryList = response.serviceResponse;
        console.log("leaveHistoryList In Timesheet : ", this.leaveHistoryList);
        this.leaveHistoryList = this.leaveHistoryList.filter(leaveApplication => leaveApplication.status == 'Approved');
        console.log("leave Approved  History : ", this.leaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }






  //modals
  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openNightShiftTemplate(template: TemplateRef<any>, event){
    if(event.target.checked){
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    }
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

  // validateDescription(event,data:any){

  // if (!this.validationService.validateActivityTimesheetDiscription(data)) {
  //   this.errorMsg = "Please enter valid Description !!"   
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
  validateTime(event, data: any) {
    if (!this.validationService.validateTimesheetCompletionTime(data)) {
      this.errorMsg = "Please enter Time !!"
    } else if (!this.validationService.validateExperiencedNumber(data)) {
      this.errorMsg = "Please enter Valid Time !!"
    }
    else if (data <= 0 || data > 24) {
      this.errorMsg = "Total time must be greater than 0 hrs and maximum upto 24 hrs!! "
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }

  }

  omit_special_char(event) {

    var k;
    k = event.charCode;  //        k = event.keyCode;  (Both can be used)
    //console.log("omit function" + k);
    //console.log((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || (k >= 48 && k <= 57));
    if ((k == 43) || (k == 45) || (k == 69) || (k == 101)) {
      return (false);
    }
    else {
      return (true);
    }
    //return ((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || (k >= 48 && k <= 57));
  }

  onPaste(e) {
    e.preventDefault();
    return false;
  }



  validateClientName(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Client Name !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }
  validateClientLocation(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Client Location !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }
  validateProjectName(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Project Name !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }

  validateActivity(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Activity !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }
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
            case 'employeeName':
              return compare(a.employeeName, b.employeeName, isAsc)
            case 'date':
              return compare(a.date, b.date, isAsc)
            case 'dayType':
              return compare(a.dayType, b.dayType, isAsc)
            case 'officeInTime':
              return compare(new Date(a.officeInTime).getTime(), new Date(b.officeInTime).getTime(), isAsc);
            case 'officeOutTime':
              return compare(new Date(a.officeOutTime).getTime(), new Date(b.officeOutTime).getTime(), isAsc);
            case 'totalWorkingOfficeHours':
              return compare(a.totalWorkingOfficeHours, b.totalWorkingOfficeHours, isAsc);
            case 'description':
              return compare(a.description, b.description, isAsc);
            case 'totalTime':
              return compare(a.totalTime, b.totalTime, isAsc);
            case 'status':
              return compare(a.status, b.status, isAsc);
            case 'createdByName':
              return compare(a.createdByName, b.createdByName, isAsc);
            case 'createdOn':
              return compare(new Date(a.createdOn).getTime(), new Date(b.createdOn).getTime(), isAsc);
            case 'remarks':
              return compare(a.remarks, b.remarks, isAsc);
            case 'status':
              return compare(a.status, b.status, isAsc)
            default:
              return 0;
          }
        }
      )
    }


  }
  // sortMyTimesheetView
  sortMyTimesheetView(sort: Sort) {
    console.log(sort);
    const data = this.timesheetObj.allTimesheetActivities;
    if (!sort.active || sort.direction === '') {
      this.timesheetObj.allTimesheetActivities = data;
      return;
    } else {
      this.timesheetObj.allTimesheetActivities = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'employeeName':
              return compare(a.employeeName, b.employeeName, isAsc)
            case 'date':
              return compare(a.date, b.date, isAsc)
            case 'dayType':
              return compare(a.dayType, b.dayType, isAsc)
            case 'officeInTime':
              return compare(new Date(a.officeInTime).getTime(), new Date(b.officeInTime).getTime(), isAsc);
            case 'officeOutTime':
              return compare(new Date(a.officeOutTime).getTime(), new Date(b.officeOutTime).getTime(), isAsc);
            case 'totalWorkingOfficeHours':
              return compare(a.totalWorkingOfficeHours, b.totalWorkingOfficeHours, isAsc);
            case 'description':
              return compare(a.description, b.description, isAsc);
            case 'totalTime':
              return compare(a.totalTime, b.totalTime, isAsc);
            case 'status':
              return compare(a.status, b.status, isAsc);
            case 'createdByName':
              return compare(a.createdByName, b.createdByName, isAsc);
            case 'createdOn':
              return compare(new Date(a.createdOn).getTime(), new Date(b.createdOn).getTime(), isAsc);
            case 'remarks':
              return compare(a.remarks, b.remarks, isAsc);
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

