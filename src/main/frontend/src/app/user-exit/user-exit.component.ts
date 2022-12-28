import { AbstractType, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { DatePipe } from '@angular/common';
import { EmployeeService } from '../services/employee.service';
import { first } from 'rxjs/operators';
import { Employee } from '../models/employee';
import { AuthenticationService } from '../services/authentication.service';
import { User } from '../models/user';
import * as moment from 'moment';
import { Asset } from '../models/asset';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'app-user-exit',
  templateUrl: './user-exit.component.html',
  styleUrls: ['./user-exit.component.css']
})
export class UserExitComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  employeeObj: Employee = new Employee();
  employeeDetailObj: Employee = new Employee();
  assetObj:Asset = new Asset();

  currentUser:User;
  exitEmployeeId:any;

  isResign:boolean = false;
  isResignDetails: boolean = false;
  isConsentCheck:boolean = false;
  isReleivingDate:boolean = false;
  isConsentReceived:boolean = false;
  isCurrentUser:boolean = false;

  exitAssetDetailList:any[] = [];
  employeeInfo:any[] = [];
  updatedConsentList:any[] = [];

  dateOfRelieving:any;
  currentUserName:any;
  data:string;
  
  constructor(
    private modalService: BsModalService,
    private employeeService : EmployeeService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
    private route: ActivatedRoute,
    private router : Router
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.route.params.subscribe((params:Params) => {
      this.exitEmployeeId = params['id'];
    });

    console.log( this.router.url, " : url");
    
    
    this.currentUserName = this.currentUser.name[0].toUpperCase() + this.currentUser.name.slice(1).toLowerCase();
    this.sectionViewInit();
  }

  sectionViewInit(){
    if(this.exitEmployeeId != null && (this.currentUser.employeeRole != 'Employee' && this.currentUser.employeeRole != 'TeamLead')){
      this.getEmployeeInfo(this.exitEmployeeId);
    }else{
      this.getEmployeeResignationDetails();
    }

    if(this.currentUser.employeementId == this.exitEmployeeId || this.exitEmployeeId == undefined){
      this.isCurrentUser = true;
      this.router.navigate(['/user-exit']);
    }else{
      this.isCurrentUser = false;
    }
  }

  openResignRuleModal(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  consentCheckbox(event){
    if(event.target.checked){
      this.isConsentCheck = true;
    }
  }

  getEmployeeInfo(employmentId:any){
    this.employeeObj.employeementId = employmentId;
    this.employeeService.getEmployeeInfo(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeInfo = response.serviceResponse;

        this.employeeInfo.forEach((obj) => {
          if(obj.dateOfResign != null){
            this.isReleivingDate = true;
            this.getEmployeeExitAssetDetails(this.alertTemplate, this.exitEmployeeId);
          }else{
            this.employeeInfo = [];
            this.exitEmployeeId = null;
            this.router.navigate(['/user-exit']);
            // this.getEmployeeResignationDetails();
          }
        });
        console.log(this.employeeInfo, " :   this.employeeInfo");
      } else {
        console.log(response.serviceResponse);
      }
    });
  }

  resign(template: TemplateRef<any>){
    this.cancelRequest();
    const currentDate = this.datePipe.transform(new Date(), 'yyyy-MM-dd');

    this.employeeObj.dateOfResign = currentDate;
    this.employeeObj.empId = this.currentUser.empId;

    this.employeeService.updateEmployeeResignationDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isResignDetails = true;
        this.isResign = false;
        this.getEmployeeResignationDetails();
        console.log(response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getEmployeeResignationDetails(){
    this.cancelRequest();
    this.employeeObj.empId = this.currentUser.empId;
    this.employeeService.getEmployeeResignationDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeDetailObj = response.serviceResponse;

        console.log(this.employeeDetailObj, " : employeeDetailObj");

        if(this.employeeDetailObj.dateOfResign != null){
          this.dateOfRelieving = moment(this.employeeDetailObj.dateOfResign).add(this.employeeDetailObj.noticePeriod, 'days').format('YYYY-MM-DD');
          this.employeeDetailObj.dateOfRelieving = this.dateOfRelieving;

          this.isResignDetails = true;

          if (moment(new Date()).format('YYYY-MM-DD') >= moment(this.dateOfRelieving).format('YYYY-MM-DD')) {
            this.isReleivingDate = true;
            this.isResignDetails = false;
            this.isResign = false;
            this.getEmployeeExitAssetDetails(this.alertTemplate,this.exitEmployeeId);
          }
        }else{
          this.isResign = true;
        }
      } else {
        console.log(response.serviceResponse);
      }
    });
  }

  getEmployeeExitAssetDetails(template: TemplateRef<any>, exitEmployeeId:any){
    this.cancelRequest();

    if(exitEmployeeId != null){
      this.employeeObj.employeementId = exitEmployeeId;
    }else{
      this.employeeObj.employeementId = this.currentUser.employeementId;
    }

    this.employeeService.getEmployeeExitAssetDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.exitAssetDetailList = response.serviceResponse;
        
        this.exitAssetDetailList.forEach((x) => {
          if(x.deptConsent == 'N' || x.deptConsent == null){
            x.deptConsent = false;
          }else{
            x.deptConsent = true;
          }

          if(x.departmentName == null){
            x.departmentName = 'Manager';
          }

          if(x.isAssigned == 'true'){
            x.isAssigned = 'Yes';
          }else{
            x.isAssigned = 'No';
          }
        });
        console.log(this.exitAssetDetailList, " : exitAssetDetailList");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  selectDeptConsentCheckbox(updatedConsent){
    const alreadyUpdatedConsent = this.updatedConsentList.find((x) => x.deptConsent == updatedConsent.deptConsent && x.assestName == updatedConsent.assestName);
      if(alreadyUpdatedConsent){
        this.updatedConsentList.splice(alreadyUpdatedConsent,1);
      }else{
        this.updatedConsentList.push(updatedConsent);
      }
      console.log(this.updatedConsentList);
  }

  /*
     - submit consent API
     - mail trigger with ishine link when date of releving == today
     - add aprroved by in getEmployeeExitAssetDetails api
  */

  submitConsent(){

  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
