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
  selector: 'app-comp-off',
  templateUrl: './comp-off.component.html',
  styleUrls: ['./comp-off.component.css']
})
export class CompOffComponent implements OnInit {

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

  compOffObj:Leave = new Leave();
  compOffReasons:any[] = [];
  allCompOffRequests:any[] = [];
  allCompOffApplications:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private datePipe: DatePipe,) {
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
  }

  sectionViewInit(){
    this.showCompOffRequestTable();
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isCompOffRequestsTable = false;
    this.isCompOffApplicationsTable = false;

    this.reset();
  }

  showCompOffRequestTable(){
    this.isCompOffRequestsTable = true;
    this.isCompOffApplicationsTable = false;
    this.isForm = false;
    this.isCreation = false;

    this.getAllCompOffRequestsByEmpId();
  }

  showCompOffApplicationsTable(){
    this.isCompOffApplicationsTable = true;
    this.isCompOffRequestsTable = false;
    this.isForm = false;
    this.isCreation = false;

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

    setMinToDate(template: TemplateRef<any>){
      if(!this.validationService.validateNullUndefinedEmptyString(this.compOffObj.fromDate)){
        this.alertMessage = "Please select from date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      let fromDate = this.compOffObj.fromDate;
      let toDate = document.getElementById('toDate');
      toDate?.setAttribute('min', fromDate);
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
      
      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.reason)){
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

      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.description)){
        this.alertMessage = "Please enter comp off description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      return true;
    }

    onApplyCompOff(template: TemplateRef<any>){
      let inputValidated:boolean  = this.validateLeavetObj(this.compOffObj, template)
      if(!inputValidated) return;

      const COMP_OFF_MASTER_ID  = 5;
      this.compOffObj.leaveTypeMasterId = COMP_OFF_MASTER_ID;

      this.compOffObj.fromDate = this.datePipe.transform(this.compOffObj.fromDate, 'dd-MM-yyyy');
      this.compOffObj.toDate = this.datePipe.transform(this.compOffObj.toDate, 'dd-MM-yyyy');
      this.compOffObj.empId = this.currentUser.empId;
      this.compOffObj.createdBy = this.currentUser.empId;
      this.compOffObj.managerId = this.currentUser.managerId; 
  
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
          this.allCompOffRequests = response.serviceResponse;
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
          console.log("allCompOffApplications : ", this.allCompOffApplications);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

}
