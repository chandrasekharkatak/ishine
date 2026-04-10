import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as XLSX from 'xlsx';

@Component({
  standalone: false,
  selector: 'app-comp-off',
  templateUrl: './comp-off.component.html',
  styleUrls: ['./comp-off.component.css']
})
export class CompOffComponent implements OnInit {

  data:string;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //flags
  isCreation:boolean = false;
  isForm: boolean = false;
  isUpdation:boolean = false;
  isCompOffRequestsTable: boolean = false;

  //modal
  alertMessage:any;
  modalRef:NgbModalRef;

  //obj
  feature="Comp off";
  currentUser:User;
  userMapping:any = {};

  excelName = '';
  elementName = '';

  compOffObj:Leave = new Leave();
  compOffReasons:any[] = [];
  allCompOffRequests:any[] = [];
  previousCompOffRequests:any[] = [];

  filters:any = {};
  isSearchEnabled:boolean = false;
  compOffReqColumns:any[] = ['blank','compOffReasons','fromDate','noOfDays','description','status','rejectCompOffReason','currentApprovalLevel','approverName','managerApprovalStatus','level2ApproverName','level2ApprovalStatus','blank'];
  tabName: string;

  constructor(
    private validationService:ValidationService,
    private modalService: NgbModal,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private exportExcelService: ExportExcelService,
    private datePipe: DatePipe,
    private locationStrategy: LocationStrategy
    ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.getAllCompOffReasons();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit(){
    if(this.userMapping.apply_comp_off_req){
      this.showCreateForm();
    }else if(this.userMapping.view_comp_off_req_status || this.userMapping.update_comp_off_req){
      this.showCompOffRequestTable();
    }
  }

  disableMannualDateInput(){
    return false;
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isUpdation = false;

    this.isCompOffRequestsTable = false;

    this.reset();
    this.getAllCompOffRequestsByEmpId();
  }

  showUpdateForm(compOff:Leave){
    this.isForm = true;
    this.isUpdation = true;
    this.isCreation = false;

    this.isCompOffRequestsTable = false;

    this.compOffObj = Object.assign({}, compOff);
    this.compOffObj.leaveType = 'Compensatory Off'
    this.compOffObj.fromDate = (this.compOffObj.fromDate)? moment(this.compOffObj.fromDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;
    this.compOffObj.toDate = (this.compOffObj.toDate)? moment(this.compOffObj.toDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;

    let selectedReason =  this.compOffReasons.find(compOffReson => compOffReson.compOffReasons == this.compOffObj.compOffReasons);
    if(selectedReason) this.compOffObj.reasonId = selectedReason.compOffId;

    //console.log("For Update Comp-Off : ", this.compOffObj, selectedReason);
  }

  showCompOffRequestTable(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isCompOffRequestsTable = true;

    this.isForm = false;
    this.isCreation = false;
    this.page=1;
    this.data=''
    this.getAllCompOffRequestsByEmpId();
  }

  reset(){
    this.compOffObj = new Leave();
    this.compOffObj.leaveType = 'Compensatory Off'
    this.compOffObj.compOffId = '';

    this.allCompOffRequests = [];
  }

    // Modals
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
      this.alertMessage = message;
    }

    cancelRequest() {
      this.modalRef?.close();
    }

    openDeleteCompOff(template: TemplateRef<any>, compOff: any) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
      this.compOffObj = compOff;
      //console.log("compOffObj : ", this.compOffObj);
    }

    getAllCompOffReasons(){
      this.compOffReasons = [];

      this.leaveService.getAllCompOffReasons().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.compOffReasons = response.serviceResponse;
          //console.log("compOffReasons : ", this.compOffReasons);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

    // Allow Only Past 1 month Days for Comp-off Application
    fromDateFilter = (d: Date)=>{
      const dateFormat = 'YYYY-MM-DD';
      const currentDate = new Date();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      let BACKDATED_LEAVE_PERIOD = 31;

      if(this.currentUser.compOffLockDays){
        BACKDATED_LEAVE_PERIOD = this.currentUser.compOffLockDays;
      }

      // const FUTUREDATED_LEAVE_PERIOD = 180;
      let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
      let maxDate = new Date(currentDate.getTime());

      if (this.isUpdation) {
        this.previousCompOffRequests = this.previousCompOffRequests.filter(compOff => this.datePipe.transform(compOff.fromDate, "yyyy-MM-dd") != this.datePipe.transform(this.compOffObj.fromDate, "yyyy-MM-dd"));
      }


      return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previousCompOffRequests.find(compOffApplication => moment(d).format(dateFormat) >= moment(compOffApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(compOffApplication.toDate).format(dateFormat)));
    }

    toDateFilter = (d: Date)=>{
      const dateFormat = 'YYYY-MM-DD';
      const currentDate = new Date();
      let maxDate = new Date(currentDate.getTime());

      if(!this.compOffObj.fromDate){
        return false;
      }
      return ((moment(d).format(dateFormat) >= moment(this.compOffObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previousCompOffRequests.find(compOffApplication => moment(d).format(dateFormat) >= moment(compOffApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(compOffApplication.toDate).format(dateFormat))) ? true : false;
    }

    setNoOfDays(template: TemplateRef<any>){
      if(!this.validationService.validateNullUndefinedEmptyString(this.compOffObj.fromDate)){
        this.alertMessage = "Please select from date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(this.compOffObj.toDate)){
        this.alertMessage = "Please select To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      const START_DAY_COUNT = 1;
      const diff=(e,t)=> Math.abs(Math.floor((new Date(e).getTime()-new Date(t).getTime())/ (1000*60*60*24)));
      this.compOffObj.noOfDays = START_DAY_COUNT + diff(this.compOffObj.fromDate, this.compOffObj.toDate);
    }

    validateLeavetObj(compOffObj:Leave, template: TemplateRef<any>){

      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.reasonId)){
        this.alertMessage = "Please select comp off reason !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.fromDate)){
        this.alertMessage = "Please select date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.description?.trim())){
        this.alertMessage = "Please enter description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.validationService.validateActivityTimesheetDiscription(compOffObj.description?.trim())){
        this.alertMessage = "Please enter valid description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      return true;
    }

    resetToDate(){
      this.compOffObj.toDate = ''
      this.compOffObj.noOfDays = ''
    }

    onApplyCompOff(template: TemplateRef<any>){
      const dateFormat = 'YYYY-MM-DD';
      let inputValidated:boolean  = this.validateLeavetObj(this.compOffObj, template)
      if(!inputValidated) return;

      // const COMP_OFF_MASTER_ID  = 5;
      // this.compOffObj.leaveTypeMasterId = COMP_OFF_MASTER_ID;
      this.compOffObj.description = this.compOffObj.description?.trim();
      this.compOffObj.fromDate = moment(this.compOffObj.fromDate).format(dateFormat);
      // this.compOffObj.toDate = moment(this.compOffObj.toDate).format(dateFormat);
      this.compOffObj.empId = this.currentUser.empId;
      this.compOffObj.createdBy = this.currentUser.empId;
      this.compOffObj.employeementId = this.currentUser.employeementId;
      this.compOffObj.email = this.currentUser.email;
      this.compOffObj.employeeName = this.currentUser.name;
      this.compOffObj.hodId = this.currentUser.hodId;

      if(this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager"){
        this.compOffObj.reportingManagerId = this.currentUser.reportingManagerId;
        this.compOffObj.managerId = this.currentUser.reportingManagerId;
        this.compOffObj.managerEmail = this.currentUser.reportingManagerEmail;
        this.compOffObj.managerName = this.currentUser.reportingManagerName;
      }else{
        this.compOffObj.reportingManagerId = this.currentUser.managerId;
        this.compOffObj.managerId = this.currentUser.managerId;
        this.compOffObj.managerEmail = this.currentUser.managerEmail;
        this.compOffObj.managerName = this.currentUser.managerName;
      }

      this.compOffObj.hodId = this.currentUser.hodId;
      this.compOffObj.hodEmail = this.currentUser.hodEmail;
      this.compOffObj.hodName = this.currentUser.hodName;
      this.compOffObj.approverName=this.currentUser.managerName;
      this.compOffObj.currentApprovalLevel=1;
      this.compOffObj.managerApprovalStatus="Pending";
      this.compOffObj.level2ApproverName=this.currentUser.hodName;
      this.compOffObj.finalApprovalLevel=2;

      //console.log("Apply Comp off : ", this.compOffObj);
      //console.log("compoff currentuser    ::   ",this.currentUser);
      this.leaveService.applyForCompOff(this.compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showCompOffRequestTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }


    getAllCompOffRequestsByEmpId(){
      this.allCompOffRequests = [];

      let compOff = new Leave();
      compOff.empId = this.currentUser.empId;
      this.leaveService.getAllCompOffRequestsByEmpId(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.previousCompOffRequests = JSON.parse(JSON.stringify(response.serviceResponse));
          this.previousCompOffRequests = this.previousCompOffRequests.filter(compOffApplication => compOffApplication.status != "Rejected");

          this.allCompOffRequests = response.serviceResponse;
          this.allCompOffRequests.forEach(compOff => {
            compOff.fromDate = (compOff.fromDate)? moment(compOff.fromDate).format(AppComponent.DATE_FORMAT) : null;
            compOff.toDate = (compOff.toDate)? moment(compOff.toDate).format(AppComponent.DATE_FORMAT) : null
          });
          //console.log("allCompOffRequests : ", this.allCompOffRequests);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

    onUpdateCompOff(template: TemplateRef<any>){
      const dateFormat = 'YYYY-MM-DD';
      let inputValidated:boolean  = this.validateLeavetObj(this.compOffObj, template)
      if(!inputValidated) return;

      let compOff = new Leave();
      compOff = Object.assign({}, this.compOffObj);
      compOff.description = this.compOffObj.description?.trim();
      compOff.fromDate = moment(this.compOffObj.fromDate).format(dateFormat);
      // compOff.toDate = moment(this.compOffObj.toDate).format(dateFormat);
      compOff.updatedBy = this.currentUser.empId;
      // compOff.reportingManagerId = this.currentUser.reportingManagerId

      if(this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager"){
        compOff.reportingManagerId = this.currentUser.reportingManagerId;
        compOff.managerEmail = this.currentUser.reportingManagerEmail;
        compOff.managerId = this.currentUser.reportingManagerId;
        compOff.managerName = this.currentUser.reportingManagerName;
      }else{
        compOff.reportingManagerId = this.currentUser.managerId;
        compOff.managerId = this.currentUser.managerId;
        compOff.managerEmail = this.currentUser.managerEmail;
        compOff.managerName = this.currentUser.managerName;
      }


      //console.log("Update comp off : ", compOff);
      this.leaveService.updateCompOff(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showCompOffRequestTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

    deleteCompOff(template: TemplateRef<any>) {
      this.cancelRequest();

      let compOff = new Leave;
      compOff = Object.assign({}, this.compOffObj);

      compOff.empId = this.currentUser.empId;
      compOff.email = this.currentUser.email;
      compOff.employeeName = this.currentUser.name;
      compOff.employeementId = this.currentUser.employeementId;
      compOff.managerId = this.currentUser.managerId;
      compOff.managerEmail = this.currentUser.managerEmail;
      compOff.managerName = this.currentUser.managerName;

      compOff.hodId = this.currentUser.hodId;
      compOff.hodEmail = this.currentUser.hodEmail;
      compOff.hodName = this.currentUser.hodName;

      //console.log("Delete compOff ",compOff)
      this.leaveService.deleteCompOff(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showCompOffRequestTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }


    // download excel

  exportToExcel(): void {

    if(this.isCompOffRequestsTable == true){
      this.elementName = 'compOffRequest-table';
      this.excelName = 'EmployeeCompOffRequest.xlsx'
    }

    let element = document.getElementById(this.elementName);
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element);

    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

    XLSX.writeFile(book, this.excelName);
  }
  // exportToExcel(id:any): void {
  //   const tableId = id; // Replace with your actual table ID
  //   this.excelName = "QuarterCycle.xlsx";
  //   this.tabName= 'Quarter Cycle Table';

  //   this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tabName);
  // }

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
  }

  onSearch(searchData){
  this.filters = searchData;
  //console.log("Updated Filter : ", this.filters);
  }

}


function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
