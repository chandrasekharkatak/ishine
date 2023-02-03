import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-comp-off',
  templateUrl: './comp-off.component.html',
  styleUrls: ['./comp-off.component.css']
})
export class CompOffComponent implements OnInit {

    data:string;
  //flags 
  isCreation:boolean = false;
  isForm: boolean = false;
  isCompOffRequestsTable: boolean = false;
  isCompOffApplicationsTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

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
  allCompOffApplications:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
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
    console.log(this.feature, this.userMapping);

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
    }else if(this.userMapping.view_reportee_comp_off_applications || this.userMapping.update_comp_off_applications_status){
      this.showCompOffApplicationsTable();
    }
  }

  disableMannualDateInput(){
    return false;
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;

    this.isCompOffRequestsTable = false;
    this.isCompOffApplicationsTable = false;

    this.reset();
    this.getAllCompOffRequestsByEmpId();
  }

  showCompOffRequestTable(){
    this.isCompOffRequestsTable = true;

    this.isCompOffApplicationsTable = false;
    this.isForm = false;
    this.isCreation = false;
    this.page=1;
    this.data=''
    this.getAllCompOffRequestsByEmpId();
  }

  showCompOffApplicationsTable(){
    this.isCompOffApplicationsTable = true;
    
    this.isCompOffRequestsTable = false;
    this.isForm = false;
    this.isCreation = false;
    this.page=1;
    this.data=''
    this.getPendingCompOffRequestsByManagerId();
  }

  reset(){
    this.compOffObj = new Leave();
    this.compOffObj.leaveType = 'Compensatory Off'
    this.compOffObj.compOffId = '';

    this.allCompOffRequests = [];
    this.allCompOffApplications = [];
  }

    // Modals
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    cancelRequest() {
      this.modalRef.hide();
    }

    getAllCompOffReasons(){
      this.compOffReasons = [];
  
      this.leaveService.getAllCompOffReasons().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.compOffReasons = response.serviceResponse;
          console.log("compOffReasons : ", this.compOffReasons);
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
        this.alertMessage = "Please select from date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.toDate)){
        this.alertMessage = "Please select To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateActivityTimesheetDiscription(compOffObj.description?.trim())){
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
      this.compOffObj.toDate = moment(this.compOffObj.toDate).format(dateFormat);
      this.compOffObj.empId = this.currentUser.empId;
      this.compOffObj.managerEmail = this.currentUser.managerEmail;
      this.compOffObj.managerName = this.currentUser.managerName;
      this.compOffObj.createdBy = this.currentUser.empId;
      this.compOffObj.managerId = this.currentUser.managerId; 
      this.compOffObj.employeementId = this.currentUser.employeementId;
      this.compOffObj.email = this.currentUser.email;
      this.compOffObj.employeeName = this.currentUser.name;
  
      console.log("Apply Comp off : ", this.compOffObj);
      this.leaveService.applyForCompOff(this.compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showCompOffRequestTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

    onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId){
      // 1 = pending , 2 = Approved , 3= Rejected
      compOffObj.leaveStatusId = updatedCompOffStatusId;
      compOffObj.leaveStatusUpdatedBy = this.currentUser.empId
      console.log("Update Comp off : ", compOffObj);
      this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getPendingCompOffRequestsByManagerId();
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
          this.allCompOffRequests = response.serviceResponse;
          this.allCompOffRequests.forEach(compOff => {
            compOff.fromDate = (compOff.fromDate)? moment(compOff.fromDate).format(AppComponent.DATE_FORMAT) : null;
            compOff.toDate = (compOff.toDate)? moment(compOff.toDate).format(AppComponent.DATE_FORMAT) : null 
          });
          console.log("allCompOffRequests : ", this.allCompOffRequests);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  
    getPendingCompOffRequestsByManagerId(){
      this.allCompOffApplications = []
  
      let compOff = new Leave();
      compOff.managerId = this.currentUser.empId;
      this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allCompOffApplications = response.serviceResponse;
          this.allCompOffApplications.forEach(compOff => {
            compOff.fromDate = (compOff.fromDate)? moment(compOff.fromDate).format(AppComponent.DATE_FORMAT) : null;
            compOff.toDate = (compOff.toDate)? moment(compOff.toDate).format(AppComponent.DATE_FORMAT) : null 
          });
          console.log("allCompOffApplications : ", this.allCompOffApplications);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }


    // download excel

exportToExcel(): void {

  if(this.isCompOffRequestsTable == true){
    this.elementName = 'compOffRequest-table';
    this.excelName = 'EmployeeCompOffRequest.xlsx'
  }
  if(this.isCompOffApplicationsTable == true){
    this.elementName = 'compOffApplication-table';
    this.excelName = 'CompOffRequestApplication.xlsx'
  }

  let element = document.getElementById(this.elementName);
  const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element);

  const book: XLSX.WorkBook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

  XLSX.writeFile(book, this.excelName);
}

//pagination 

page = 1;
handlePageChange(event) {
  this.page = event;
}

sortCompensatory(sort:Sort){	
  console.log(sort);	
  const data=this.allCompOffRequests;	
  if(!sort.active || sort.direction===''){	
    this.allCompOffRequests=data;	
    return ;	
  } else {	
    this.allCompOffRequests=data.sort(	
      (a,b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'compOffReasons':	
            return compare(a.compOffReasons , b.compOffReasons , isAsc)	
            case 'fromDate':	
              return compare(a.fromDate , b.fromDate , isAsc)	
              case 'toDate':	
                return compare(a.toDate , b.toDate , isAsc)	
                case 'noOfDays':	
                  return compare(a.noOfDays , b.noOfDays , isAsc)	
                  case 'description':	
                    return compare(a.description , b.description , isAsc)	
                    case 'status':	
                      return compare(a.status , b.status , isAsc)	
                    default :	
                    return 0;	
        }	
      }	
    )	
  }

}

sortReporteeCompensatory(sort:Sort){	
  console.log(sort);	
  const data=this.allCompOffApplications;	
  if(!sort.active || sort.direction===''){	
    this.allCompOffApplications=data;	
    return ;	
  } else {	
    this.allCompOffApplications=data.sort(	
      (a,b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'createdByName':	
            return compare(a.createdByName , b.createdByName , isAsc)	
            case 'compOffReasons':	
              return compare(a.compOffReasons , b.compOffReasons , isAsc)	
              case 'fromDate':	
                return compare(a.fromDate , b.fromDate , isAsc)	
                case 'toDate':	
                  return compare(a.toDate , b.toDate , isAsc)	
                  case 'noOfDays':	
                    return compare(a.noOfDays , b.noOfDays , isAsc)	
                    case 'description':	
                      return compare(a.description , b.description , isAsc)	
                      case 'status':	
                      return compare(a.status , b.status , isAsc)	
                    default :	
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