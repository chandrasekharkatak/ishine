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
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-my-timesheet',
  templateUrl: './my-timesheet.component.html',
  styleUrls: ['./my-timesheet.component.css']
})
export class MyTimesheetComponent implements OnInit {
  
  feature="My Timesheets";
  currentUser:User;
  userMapping:any = {};

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;

  isTimesheetForm:boolean = false;
  isTimesheetTable:boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj
  timesheetObj:Timesheet = new Timesheet();
  allTimesheetActivities:any[] = []
  allProjectsList:any[] = [];
  allActivityList:any[] = [];
  
  allMyTimesheets:any[] = [];
  timesheetActivities:any[] = [];
  startDate:any;
  endDate:any;

  excelName = '';
  elementName = '';

  availableTimesheetDates:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private timesheetService : TimesheetService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    if(this.userMapping.add_timesheet){
      this.showCreateTimesheetForm();
    }else if(this.userMapping.view_my_timesheets || this.userMapping.update_timesheet){
      this.showViewMyTimesheets()
    }
  }

  showCreateTimesheetForm(){
    this.isTimesheetForm = true;
    this.isCreation = true;
    
    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.reset();
    this.getAllProjectsByEmpId();
  }

  showViewMyTimesheets(){
    this.isTimesheetTable = true;
    
    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
  }

  showUpdateTimesheetForm(timesheetObj:Timesheet){
    this.isTimesheetForm = true;
    this.isUpdation = true;
    
    this.isTimesheetTable = false;
    this.isCreation = false;

    this.timesheetObj = Object.assign({}, timesheetObj);
    this.timesheetObj.updatedTimesheetActivities = [];
    this.getAllMyActivitiesByTimesheetId(timesheetObj);
  }

  reset(){
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

  removeInputActivityField(activityObj:any) {
    this.allTimesheetActivities.forEach((value, index) => {
      if (value == activityObj){
        if(this.isUpdation){
          this.timesheetObj.updatedTimesheetActivities.push(value);
        }
        this.allTimesheetActivities.splice(index, 1);
      } 
    });
  }

  setAllProjectActivities(activityObj, allActivityList:any){
    this.allTimesheetActivities.find(activity => activity === activityObj).projectActivities = allActivityList;
  }

  setActivity(activityObj){
    this.allTimesheetActivities.find(activity => activity === activityObj).activity = activityObj.projectActivities.find(activity => activity.activityId == activityObj.activityId).activity;
  }

  // Manage Timesheet Dates
  timesheetDateFilter = (d: Date)=>{
    const time=d?.getTime();
     
    return this.availableTimesheetDates.find(x=>x.getTime()==time);
  }

  /* Timesheet */
  validateTimesheetObj(timesheetObj:Timesheet, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(timesheetObj.date)){
      this.alertMessage = "Please enter Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(timesheetObj.dayType)){
      this.alertMessage = "Please select Day Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(timesheetObj.dayType != 'Holiday'){
      let flag = true;

      this.allTimesheetActivities.forEach((activity, index) => {
        if(!this.validationService.validateNullUndefinedEmptyString(activity.projectId)){
          this.alertMessage = `Please select Project - ${index+1}!!`
          flag = false;
          return;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(activity.activityId)){
          this.alertMessage = `Please select Activity - ${index+1}!!`
          flag = false;
          return;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(activity.description)){
          this.alertMessage = `Please enter Activity Description - ${index+1}!!`
          flag = false;
          return;
        }

        if(!this.validationService.validateNullUndefinedEmptyString(activity.completionTime)){
          this.alertMessage = `Please enter Activity Completion Time - ${index+1}!!`
          flag = false;
          return;
        }
      });

      if(!flag){
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }else{
      if(!this.validationService.validateNullUndefinedEmptyString(timesheetObj.description)){
        this.alertMessage = "Please enter Timesheet Description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    return true;
  }

  onCreateTimesheet(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateTimesheetObj(this.timesheetObj, template)
    if(!inputValidated) return;
    
    if(this.timesheetObj.dayType != 'Holiday'){
      console.log("allTimesheetActivities :", this.allTimesheetActivities, this.allTimesheetActivities[0]);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    }else{
      this.timesheetObj.allTimesheetActivities = null;
    }
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

  onUpdateTimesheet(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateTimesheetObj(this.timesheetObj, template)
    if(!inputValidated) return;
    
    if(this.timesheetObj.dayType != 'Holiday'){
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;

      if(this.timesheetObj.allTimesheetActivities){
        let newTimesheetActivities = this.timesheetObj.allTimesheetActivities.filter(activity => !activity.timesheetId);
        console.log("newTimesheetActivities : ", newTimesheetActivities);
        
        if(newTimesheetActivities){
          if(this.timesheetObj.updatedTimesheetActivities === undefined || this.timesheetObj.updatedTimesheetActivities.length === 0){
            this.timesheetObj.updatedTimesheetActivities = [];
          }
          this.timesheetObj.updatedTimesheetActivities = this.timesheetObj.updatedTimesheetActivities.concat(newTimesheetActivities);
        }
      }else{
        if(this.timesheetObj.updatedTimesheetActivities == undefined || this.timesheetObj.updatedTimesheetActivities[0].length == 0){
          this.timesheetObj.updatedTimesheetActivities = null;
        }
      }
    }else{
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


  getAllProjectsByEmpId(){
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

  getAllActivitiesByProjectIdandEmpId(activityObj:any){
    let allActivityList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
    timesheetObj.projectId = activityObj.projectId;
    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        allActivityList = response.serviceResponse;
        console.log("allActivityList :", allActivityList);
        this.setAllProjectActivities(activityObj, allActivityList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }




  /* View Timesheets */
  getAllMyTimesheetsByEmpId(template?: TemplateRef<any>){
    this.allMyTimesheets = [];

    if(this.endDate){
      if(!this.validationService.validateNullUndefinedEmptyString(this.startDate)){
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(this.endDate)){
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }else{
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

  getAllMyActivitiesByTimesheetId(timesheet:any){
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

      if(this.allTimesheetActivities.length == 0){
        this.addInputActivityField();
      }else{
        this.allTimesheetActivities.forEach(activity => this.getAllActivitiesByProjectIdandEmpId(activity));
      }
    });
  }


  exportToExcel(): void {

    if(this.isTimesheetTable == true){
      this.elementName = 'teams-table';
      this.excelName = 'MyTimeSheet.xlsx'
    }
  
  
    let element = document.getElementById(this.elementName);
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element);
  
    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
  
    XLSX.writeFile(book, this.excelName);
  }










  //modals
  openUpdateConfimationModal(template: TemplateRef<any>, ){
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
 }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
