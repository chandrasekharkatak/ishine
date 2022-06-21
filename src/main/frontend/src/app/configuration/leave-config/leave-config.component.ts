import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
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

@Component({
  selector: 'app-leave-config',
  templateUrl: './leave-config.component.html',
  styleUrls: ['./leave-config.component.css']
})
export class LeaveConfigComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;

  isHolidayForm:boolean = false;
  isLeaveTypeForm:boolean = false;
  isLeaveBalanceForm:boolean = false;
  isHolidayTable:boolean = false;
  isLeaveRuleTable:boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  feature="Leave";
  currentUser:User;
  userMapping:any = {};
  
  holidayObj:Holiday = new Holiday();
  holidayList:any[] = [];

  leaveTypeObj:Leave = new Leave();
  leaveTypes:any[] = [];

  leaveBalanceObj:Leave = new Leave();
  leaveBalanceList:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private holidayService : HolidayService,
    private datePipe: DatePipe,
    private leaveService : LeaveService) {
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
    this.showLeaveTypesTable();
  }

  showAddHolidayForm(){
    this.isHolidayForm = true;
    this.isCreation = true;

    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showHoliaysTable(){
    this.isHolidayTable = true;

    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllHolidays();
  }

  showLeaveTypesTable(){
    this.isLeaveRuleTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllLeaveTypes();
  }

  showAddLeaveTypeForm(){
    this.isLeaveTypeForm = true;
    this.isCreation = true;
    
    this.isHolidayForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isUpdation = false;

    this.reset();
  }


  showUpdateLeaveTypeForm(leaveType:Leave){
    this.isLeaveTypeForm = true;
    this.isUpdation = true;
    
    this.isHolidayForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isCreation = false;

    this.leaveTypeObj = Object.assign({}, leaveType);
  }

  showUpdateHolidayForm(holiday:Holiday){
    this.isHolidayForm = true;
    this.isUpdation = true;
    
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isCreation = false;

    this.holidayObj = Object.assign({}, holiday);
    this.holidayObj.optionalHoliday = (JSON.parse(holiday.optionalHoliday) != null) ? JSON.parse(holiday.optionalHoliday) : false;
  }

  showLeaveBalanceForm(){
    this.isLeaveBalanceForm = true;
    
    this.isLeaveTypeForm = false;
    this.isHolidayForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.reset();
  }

  reset() {
    this.holidayObj= new Holiday();
    this.holidayObj.optionalHoliday = false;
    this.holidayList = [];

    this.leaveTypeObj = new Leave();
    this.leaveTypes = [];

    this.leaveBalanceObj = new Leave();
    this.leaveBalanceList = [];
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  // Holiday
  validateHolidaytObj(holidayObj:Holiday, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(holidayObj.occasion)){
      this.alertMessage = "Please enter occasion Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(holidayObj.dateOfHoliday)){
      this.alertMessage = "Please select Date of Holiday !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }
  
  setHolidayWeekDay(){
    let weekDay = '';
    let day = new Date(this.holidayObj.dateOfHoliday).getDay();

    switch (day) {
      case 0: {
        weekDay = 'Sunday';
        break;
      }
      case 1: {
        weekDay = 'Monday';
        break;
      }
      case 2: {
        weekDay = 'Tuesday'; 
        break;
      }
      case 3: {
        weekDay = 'Wednesday';
        break;
      }
      case 4: {
        weekDay = 'Thursday'; 
        break;
      }
      case 5: {
        weekDay = 'Friday';
        break;
      }
      case 6: {
        weekDay = 'Saturday'; 
        break;
      }

      default: {
        weekDay = '';
        break;
      }
    } 
     this.holidayObj.dayOfTheWeek = weekDay;
  }

  onAddHoliday(template: TemplateRef<any>) {

    let inputValidated:boolean  = this.validateHolidaytObj(this.holidayObj, template)
    if(!inputValidated) return;

    this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    console.log("Add Holiday : ", this.holidayObj);
    this.holidayService.addHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showHoliaysTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateHoliday(template: TemplateRef<any>) {

    let inputValidated:boolean  = this.validateHolidaytObj(this.holidayObj, template)
    if(!inputValidated) return;

    this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    console.log("update Holiday : ", this.holidayObj);

    this.holidayService.updateHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showHoliaysTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  getAllHolidays(){
    this.holidayList = [];
    
    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("leaveTypes : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  
  // Leave Type
  validateLeaveTypeObj(leaveTypeObj:Leave, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveType)){
      this.alertMessage = "Please enter Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveTypeCode)){
      this.alertMessage = "Please enter Leave Type Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.noOfDays)){
      this.alertMessage = "Please enter Default Leave Days !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onAddLeaveType(template: TemplateRef<any>) {
    let inputValidated:boolean  = this.validateLeaveTypeObj(this.leaveTypeObj, template)
    if(!inputValidated) return;

    console.log("Add Leave Type : ", this.leaveTypeObj);

    this.leaveService.createLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateLeaveType(template: TemplateRef<any>) {
    console.log("update Leave Type : ", this.leaveTypeObj);

    this.leaveService.updateLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
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


  // Manage Leave Balance
  onGetEmpLeaveBalance(template: TemplateRef<any>){
    this.leaveBalanceList = [];

    if(!this.validationService.validateNullUndefinedEmptyString(this.leaveBalanceObj.empId)){
      this.alertMessage = "Please enter Employee ID !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    console.log("Employee :", this.leaveBalanceObj);
    this.leaveService.getMyLeaveBalancesByEmpId(this.leaveBalanceObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        console.log("leaveBalanceList : ", this.leaveBalanceList);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  leaveBalanceInputValidation(balance:any, template: TemplateRef<any>){
    if(!this.validationService.validateNullUndefinedEmptyString(balance)){
      this.alertMessage = "Please enter Valid Balance !!"
      this.openAlertMod(template, this.alertMessage);
    }
  }

  onUpdateLeaveBalance(template: TemplateRef<any>){
    let inputValidated = true;
    this.leaveBalanceList.forEach(leave => {
      if(!this.validationService.validateNullUndefinedEmptyString(leave.balance)){
        this.alertMessage = "Please enter Valid Balance !!"
        inputValidated = false;
        return;
      }
    });
    
    if(!inputValidated) {
      this.openAlertMod(template, this.alertMessage)
      return false;
    };
    
    this.leaveBalanceObj.employeeLeaveList = this.leaveBalanceList;
    console.log("manage Leave Balance :", this.leaveBalanceObj);
    this.leaveService.updateLeavesByEmpId(this.leaveBalanceObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveBalanceForm();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

}
