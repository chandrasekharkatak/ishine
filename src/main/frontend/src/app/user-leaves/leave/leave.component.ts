import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-leave',
  templateUrl: './leave.component.html',
  styleUrls: ['./leave.component.css']
})
export class LeaveComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  // isTable: boolean = false;
  isLeaveHistoryTable: boolean = false;
  isLeaveBalanceTable: boolean = false;
  isLeaveApplicationsTable: boolean = false;
  isLeaveLogTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  feature="Leave";
  currentUser:User;
  userMapping:any = {};
  leaveObj:Leave = new Leave();

  leaveTypes:any[] = [];
  leaveHistoryList:any[] = [];
  leaveApplicationList:any[] = [];
  leaveLogList:any[] = [];
  leaveBalanceList:any[] = [];

  holidayList:any;
  // holidayDates:any[] = [];

  holidayDates:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
    private leaveService : LeaveService,
    private holidayService : HolidayService,) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    console.log("this.currentUser : ", this.currentUser);
    this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.fromDateDayType = 0;
    this.leaveObj.toDateDayType = 0;
    this.leaveObj.leaveAppliedFor = "me"
    
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.getAllLeaveTypes();
  //   this.holidayDates = [
  //     new Date("12/1/2022"),
  //     new Date("12/20/2022"),
  //     new Date("12/17/2022"),
  //     new Date("12/25/2022"),
  //     new Date("6/6/2022"),
  //     new Date("7/12/2022"),
  //     new Date("7/7/2022"),
  //     new Date("12/11/2022"),
  //     new Date("12/26/2022"),
  //     new Date("12/25/2022")
  // ];
  }

  sectionViewInit(){
    this.showLeaveBalanceTable();
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isUpdation = false;

    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;

    this.reset();
    this.getAllHolidays();
  }

  showLeaveHistoryTable() {
    this.isLeaveHistoryTable = true;
    this.isLeaveBalanceTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllMyLeaveApplicationsByEmpId();
  }

  showLeaveBalanceTable() {
    this.isLeaveBalanceTable = true;
    this.isLeaveHistoryTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getMyLeaveBalancesByEmpId();
  }

  showLeaveApplicationsTable() {
    this.isLeaveApplicationsTable = true;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
  }

  showLeaveLogTable() {
    this.isLeaveLogTable = true;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getLeaveLogsByEmpId();
  }

  reset() {
    this.leaveObj = new Leave();
    this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.fromDateDayType = 0;
    this.leaveObj.toDateDayType = 0;
    this.leaveObj.leaveAppliedFor = "me"

    this.leaveHistoryList = [];
    this.leaveApplicationList = [];
    this.leaveLogList = [];
    this.leaveBalanceList = [];
  }

  showUpdateForm(){
    this.isForm = true;
    this.isUpdation = true;
    this.isCreation = false;

    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  validateLeavetObj(leaveObj:Leave, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.leaveAppliedFor)){
      this.alertMessage = "Please select leave Applied for !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.leaveTypeMasterId)){
      this.alertMessage = "Please select Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.fromDate)){
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.toDate)){
      this.alertMessage = "Please select To Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.reason)){
      this.alertMessage = "Please enter Leave reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  // CRUD
  setMinToDate(template: TemplateRef<any>){
    if(!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.fromDate)){
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let fromDate = this.leaveObj.fromDate;
    let toDate = document.getElementById('toDate');
    toDate?.setAttribute('min', fromDate);
  }

  setNoOfDays(template: TemplateRef<any>){
    if(!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.fromDate)){
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.toDate)){
      this.alertMessage = "Please select To Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.leaveObj.fromDate == this.leaveObj.toDate){
      this.leaveObj.toDateDayType = 0;
    }

    const START_DAY_COUNT = 1;
    const diff=(e,t)=> Math.abs(Math.floor((new Date(e).getTime()-new Date(t).getTime())/ (1000*60*60*24)));
    this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType);
  }
  
  onApplyLeave(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateLeavetObj(this.leaveObj, template)
    if(!inputValidated) return;

    this.leaveObj.empId = this.currentUser.empId;
    this.leaveObj.createdBy = this.currentUser.empId;
    this.leaveObj.managerId = this.currentUser.managerId; 

    this.leaveObj.fromDate = this.datePipe.transform(this.leaveObj.fromDate, 'dd-MM-yyyy');
    this.leaveObj.toDate = this.datePipe.transform(this.leaveObj.toDate, 'dd-MM-yyyy');

    console.log("Apply Leave : ", this.leaveObj);
    this.leaveService.applyLeave(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveHistoryTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    console.log("leaveApplication : ", leaveApplication);
    
    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
      this.showLeaveApplicationsTable();
    });
  }

  getAllMyLeaveApplicationsByEmpId(){
    this.leaveHistoryList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.getAllMyLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveHistoryList = response.serviceResponse;
        console.log("leaveHistoryList : ", this.leaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        console.log("leaveApplicationList : ", this.leaveApplicationList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllLeaveTypes(){
    this.leaveTypes = [];

    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        console.log("leaveTypes : ", this.leaveTypes);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getLeaveLogsByEmpId(){
    this.leaveLogList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.getLeaveLogsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveLogList = response.serviceResponse;
        console.log("leaveLogList : ", this.leaveLogList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getMyLeaveBalancesByEmpId(){
    this.leaveBalanceList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        this.leaveBalanceList = this.leaveBalanceList.map(leaveType => {
          leaveType.totalLeaveBalance =  leaveType.pendingForApproval + leaveType.balance;
          return leaveType;
        });
        console.log("leaveBalanceList : ", this.leaveBalanceList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllHolidays(){
    this.holidayList = [];
    this.holidayDates = [];

    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayDates = this.holidayList.map(holiday => new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')));
        console.log("holidayDates : ", this.holidayDates); 
        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  holidayFilter = (d: Date)=>{
    const time=d?.getTime();
     
    return !this.holidayDates.find(x=>x.getTime()==time);
  }

  dateClass: MatCalendarCellClassFunction<Date> = (cellDate, view) => {
    // Only highligh dates inside the month view.
    if (view === 'month') {
      const time = cellDate.getTime()
      
      // Highlight the holidays.
      return (this.holidayDates.find(x=>x.getTime()==time)) ? 'holiday-date' : '';
    }
    return '';
  }

}
