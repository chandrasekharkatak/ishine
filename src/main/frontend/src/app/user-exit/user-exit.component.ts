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

  isResign:boolean = false;
  isResignDetails: boolean = false;
  isConsentCheck:boolean = false;
  isReleivingDate:boolean = false;
  isConsentReceived:boolean = false;

  exitAssetDetailList:any[] = [];

  dateOfRelieving:any;
  currentUserName:any;
  data:string;
  
  constructor(
    private modalService: BsModalService,
    private employeeService : EmployeeService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.currentUserName = this.currentUser.name[0].toUpperCase() + this.currentUser.name.slice(1).toLowerCase();
    this.sectionViewInit();
  }

  sectionViewInit(){
    this.getEmployeeResignationDetails();
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

        console.log(this.employeeDetailObj);

        if(this.employeeDetailObj.dateOfResign != null){
          this.dateOfRelieving = moment(this.employeeDetailObj.dateOfResign).add(this.employeeDetailObj.noticePeriod, 'days').format('YYYY-MM-DD');
          this.employeeDetailObj.dateOfRelieving = this.dateOfRelieving;

          this.isResignDetails = true;

          if (moment(new Date()).format('YYYY-MM-DD') >= moment(this.dateOfRelieving).format('YYYY-MM-DD')) {
            this.isReleivingDate = true;
            this.isResignDetails = false;
            this.isResign = false;
            this.getEmployeeExitAssetDetails(this.alertTemplate);
          }
        }else{
          this.isResign = true;
        }
      } else {
        console.log(response.serviceResponse);
      }
    });
  }

  getEmployeeExitAssetDetails(template: TemplateRef<any>){
    this.cancelRequest();
    this.employeeObj.employeementId = this.currentUser.employeementId;

    this.employeeService.getEmployeeExitAssetDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.exitAssetDetailList = response.serviceResponse;
        
        this.exitAssetDetailList.forEach((x) => {
          if(x.deptConsent == 'not received'){
            return this.isConsentReceived = false;
          }else{
            this.isConsentReceived = false;
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

  cancelRequest() {
    this.modalRef.hide();
  }

}
