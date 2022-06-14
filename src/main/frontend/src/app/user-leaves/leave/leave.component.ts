import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
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

  leaveTypes:any = [];
  leaveHistoryList:any = [];
  leaveApplicationList:any = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
    private leaveService : LeaveService) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    console.log("this.currentUser : ", this.currentUser);
    this.leaveObj.leaveTypeMasterId = '';
    
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.getAllLeaveTypes();
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
  }

  showLeaveApplicationsTable() {
    this.isLeaveApplicationsTable = true;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllMyTeamsLeaveApplicationsByManagerId();
  }

  showLeaveLogTable() {
    this.isLeaveLogTable = true;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
  }

  reset() {
    this.leaveObj = new Leave();
    this.leaveObj.leaveTypeMasterId = '';

    this.leaveHistoryList = [];
    this.leaveApplicationList = [];

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

    const diff=(e,t)=> Math.abs(Math.floor((new Date(e).getTime()-new Date(t).getTime())/ (1000*60*60*24)));
    this.leaveObj.noOfDays = diff(this.leaveObj.fromDate, this.leaveObj.toDate);
  }
  
  onApplyLeave(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateLeavetObj(this.leaveObj, template)
    if(!inputValidated) return;

    this.leaveObj.empId = this.currentUser.empId;
    this.leaveObj.createdBy = this.currentUser.empId;
    this.leaveObj.managerId = 1; 

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

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected

    console.log("Update Leave Status : ", this.leaveObj);
    
    // this.leaveService.updateLeaveStatus(this.leaveObj).pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.openAlertMod(template, response.serviceResponse);
    //   } else {
    //     this.openAlertMod(template, response.serviceResponse);
    //   }
    // });
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

  getAllMyTeamsLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
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

}
