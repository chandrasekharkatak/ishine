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

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private datePipe: DatePipe,
    private clipboardService: ClipboardService
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
    this.getAllProjectsByEmpId();
    this.getAllAvailableTimesheetByEmpId();
  }

  showViewMyTimesheets() {
    this.isTimesheetTable = true;

    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data=''
  }

  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;

    this.timesheetObj = Object.assign({}, timesheetObj);
    this.timesheetObj.updatedTimesheetActivities = [];
    this.getAllMyActivitiesByTimesheetId(timesheetObj);
  }

  reset() {
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';

    this.allTimesheetActivities = [];
    this.addInputActivityField()
  }


  // Manage Activity
  addInputActivityField() {
    let newActivityObj = new Activity();
    // newActivityObj.projectId = '';
    // newActivityObj.activityId = '';
    this.allTimesheetActivities.push(newActivityObj);
  }

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
    } 
    selectedActivityObj.projectActivities = allActivityList;
    this.isTimesheetUpdate = false;
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

      this.allTimesheetActivities.forEach((activity, index) => {
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

        if (!this.validationService.validateNullUndefinedEmptyString(activity.description)) {
          this.alertMessage = `Please enter Activity Description - ${index + 1}!!`
          flag = false;
          return;
        }else if(!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
          this.alertMessage = `Please enter valid Description.`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.completionTime)) {
          this.alertMessage = `Please enter Activity Completion Time - ${index + 1}!!`
          flag = false;
          return;
        }
      });

      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.description)) {
        this.alertMessage = "Please enter Timesheet Description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateActivityTimesheetDiscription(timesheetObj.description)) {
        this.alertMessage = "Please enter valid Timesheet Description  !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    return true;
  }

  onCreateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
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
    console.log("Add timesheetObj : ", this.timesheetObj);
    this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        this.startDate = this.endDate = this.timesheetObj.date;
        this.getAllMyTimesheetsByEmpId();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateTimesheet(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != 'Holiday') {
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
        this.startDate = this.endDate = this.timesheetObj.date;
        this.getAllMyTimesheetsByEmpId();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  getAllProjectsByEmpId() {
    this.allProjectsList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log("allProjectsList :", this.allProjectsList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllActivitiesByProjectIdandEmpId(activityObj: any) {
    let allActivityList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
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

  getAllAvailableTimesheetByEmpId() {
    this.availableTimesheets = [];
    console.log(" -- logged availableTimesheets -- ");

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
    this.timesheetService.getbackdatedTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
        console.log("availableTimesheets :", this.availableTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }



  /* View Timesheets */
  getAllMyTimesheetsByEmpId(template?: TemplateRef<any>) {
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
        this.allTimesheetActivities.forEach(activity => this.getAllActivitiesByProjectIdandEmpId(activity));
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
          "Total Working Hours":x.totalTime,
          "Description": x.description,
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
          + " comments: " + eodActivity.description + " Time taken: " + eodActivity.completionTime + "hrs. ";
      })
      this.clipboardService.copy(content + " " + totalActivity);
    } else if (response.serviceResponse = "No activities found.Activity list is empty") {
      this.clipboardService.copy(content + " " + " comments: " + timesheetObj.description)
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

