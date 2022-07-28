import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as XLSX from 'xlsx';

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

  //excel	
  excelName = '';		
  elementName = '';	
  leaveApplicationListDataForExcel:any[] = [];

  holidayList:any;
  holidayDates:any[] = [];

  leavePolicyRules:any[] = [];
  leavePolicyObj:Leave = new Leave();

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
    private leaveService : LeaveService,
    private holidayService : HolidayService,
    private exportExcelService: ExportExcelService,) {
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
    // this.getAllLeaveTypes();
    this.getAllLeaveTypesByLeavePolicies();
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

  setLeaveTypeCode(leaveTypeMasterId:any){
    let leaveType = this.leaveTypes.find(leaveType => leaveType.leaveTypeMasterId == leaveTypeMasterId);  
    this.leaveObj.leaveTypeCode = leaveType.leaveTypeCode;
  }

  setPolicyObj(leaveTypeMasterId:any){
    this.leavePolicyObj = new Leave();
    let leavePolicyObj = this.leaveTypes.find(leaveType => leaveType.leaveTypeMasterId == leaveTypeMasterId);  
    this.leavePolicyObj = Object.assign({}, leavePolicyObj);
  }

  async checkPolicy(leaveObj:Leave, leavePolicyObj:Leave, template: TemplateRef<any>){
    let flag = true;

    // One Time Leave Application Count 
    if(leavePolicyObj.oneTimeLeave == "Yes"){
      if(leavePolicyObj.oneTimeLeaveCount < leaveObj.noOfDays){
        this.alertMessage = `Leave Application days are exceeding limit of ${leavePolicyObj.oneTimeLeaveCount} days !!`
        this.openAlertMod(template, this.alertMessage);
        flag = false;
      }
    }

    // Leave Probation Period 
    if (leavePolicyObj.probation == "Yes") {
      let date = this.leaveObj.fromDate;
      let dateOfJoining = new Date();;
      if (this.currentUser?.dateOfJoining) {
        dateOfJoining = new Date(this.currentUser.dateOfJoining)
      }

      const START_DAY_COUNT = 1;
      const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
      let probationDays = START_DAY_COUNT + diff(dateOfJoining, date);

      if (probationDays < leavePolicyObj.probationPeriod) {
        this.alertMessage = `Probation Period of ${leavePolicyObj.probationPeriod} days is not completed, Please try after ${leavePolicyObj.probationPeriod-probationDays} day(s)!!`
        this.openAlertMod(template, this.alertMessage);
        flag = false;
      }
    }

    // Leave Locking Period 
    if (leavePolicyObj.lockingPeriod == "Yes") {
      let currentYear = new Date().getFullYear();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      const financialStartDate: Date = new Date(currentYear, (leavePolicyObj.financialYearStartMonth - 1), leavePolicyObj.financialYearStartDate, 0, 0, 0, 0);
      let checkDate = this.leaveObj.fromDate;

      let startDate: Date = financialStartDate;
      let endDate: Date = new Date(startDate.getTime() + (leavePolicyObj.lockingPeriodValue * DAY_IN_MS));

      await this.checkDateInRange(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template).then(response => {
        if(!response) flag = false;
      });
    }

    return flag;
  }

  async checkDateInRange(startDate: Date, endDate: Date, checkDate: Date, leaveObj:Leave, leavePolicyObj:Leave, template: TemplateRef<any>){
    let flag = true;
    if ((checkDate <= endDate && checkDate >= startDate)) {
      //Check Leave Applications Count 
      let approvedLeaveApplications = [];
      let leaveApplicationCount = 0;
      let totalAppliedLeaves = 0;

      let leaveAppObj = new Leave();
      leaveAppObj.empId = this.currentUser.empId;
      leaveAppObj.leaveTypeMasterId = leaveObj.leaveTypeMasterId;
      leaveAppObj.fromDate = startDate;
      leaveAppObj.toDate = endDate;
      
      const response:any = await this.leaveService.getAppliedLeaveApplicationsByEmpIdAndDateRange(leaveAppObj).toPromise();
        if (response.serviceStatus == "Success") {
          approvedLeaveApplications = response.serviceResponse;

          totalAppliedLeaves = approvedLeaveApplications.reduce((acc, currLeave) => {
            acc += currLeave.noOfDays;
            return acc;
          }, 0);

          leaveApplicationCount = totalAppliedLeaves + leaveObj.noOfDays;
          if (leaveApplicationCount > leavePolicyObj.lockingValue) {
            this.alertMessage = `Leave Application days are exceeding limit of ${leavePolicyObj.lockingValue} days, Available only ${leavePolicyObj.lockingValue - totalAppliedLeaves} day(s) for ${leavePolicyObj.lockingPeriodValue} days period !!`
            this.openAlertMod(template, this.alertMessage);
            flag = false;
          }
        } else {
          console.error(response.serviceResponse);
        }
        return flag;
    }
    else {
      this.updateStartDate(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template);
    }
    return flag;
  }

  updateStartDate(startDate: Date, endDate: Date, checkDate: Date, leaveObj:Leave, leavePolicyObj:Leave, template: TemplateRef<any>) {
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    startDate = endDate;
    endDate = new Date(startDate.getTime() + (leavePolicyObj.lockingPeriodValue * DAY_IN_MS));

    this.checkDateInRange(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template);
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

    this.checkPolicy(this.leaveObj, this.leavePolicyObj, template).then(response => {
      if (!response) return;

      this.leaveObj.empId = this.currentUser.empId;
      this.leaveObj.createdBy = this.currentUser.empId;
      this.leaveObj.managerId = this.currentUser.managerId;

      // this.leaveObj.fromDate = this.datePipe.transform(this.leaveObj.fromDate, 'dd-MM-yyyy');
      // this.leaveObj.toDate = this.datePipe.transform(this.leaveObj.toDate, 'dd-MM-yyyy');

      console.log("Apply Leave : ", this.leaveObj);
      this.leaveService.applyLeave(this.leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showLeaveHistoryTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
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

  getAllLeaveTypesByLeavePolicies(){
    this.leaveTypes = [];

    let leaveObj = new Leave();
    leaveObj.employmentStatus = this.currentUser.employmentstatus;
    leaveObj.gender = this.currentUser.gender;

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
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


  exportToExcel(): void {	
    if(this.isLeaveHistoryTable == true){	
      this.elementName = 'history-table';	
      this.excelName = 'MyLeaveHistory.xlsx';	
    }	
    if(this.isLeaveApplicationsTable == true){	
      this.excelName = 'MyReporteeLeaveApplication.xlsx';

      let leaveObj = new Leave();	
      leaveObj.managerId = this.currentUser.empId;	
      this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {	
        if (response.serviceStatus == "Success") {	
          this.leaveApplicationListDataForExcel = response.serviceResponse;	
        }	
    	
        const onlySpecificDataArr: Partial<Leave>[] = this.leaveApplicationListDataForExcel.map(	
          x => ({	
            leaveType: x.leaveType,	
            fromDate: x.fromDate,	
            toDate: x.toDate,	
            noOfDays: x.noOfDays,	
            status: x.status,	
            createdByName: x.createdByName,	
            createdOn: x.createdOn,	
            reason: x.reason	
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      });

    }	
    if(this.isLeaveLogTable == true){	
      this.elementName = 'log-table';	
      this.excelName = 'MyLeaveLogs.xlsx';	
    }	
  	
    let element = document.getElementById(this.elementName);	
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element);	
  	
    const book: XLSX.WorkBook = XLSX.utils.book_new();	
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');	
  	
    XLSX.writeFile(book, this.excelName);
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

    //pagination 	
    page = 1;	
    handlePageChange(event) {	
      this.page = event;	
    }

}
