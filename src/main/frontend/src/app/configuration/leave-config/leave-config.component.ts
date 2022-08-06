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
import { ExportExcelService } from 'src/app/services/export-excel.service';

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
  isLeavePolicyForm:boolean = false;
  isLeavePolicyTable:boolean = false;
  

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //excel
  excelName = '';
  holidayDataForExcel: any[];
  leaveDataForExcel: any[];
  leavePolicyDataForExcel: any[];

  //obj
  feature="Leave Config";
  currentUser:User;
  userMapping:any = {};
  
  holidayObj:Holiday = new Holiday();
  holidayList:any[] = [];
  holidayListFilter:any[] = [];

  leaveTypeObj:Leave = new Leave();
  leaveTypes:any[] = [];

  leaveBalanceObj:Leave = new Leave();
  leaveBalanceList:any[] = [];

  leavePolicyObj:Leave = new Leave();
  leavePolicyList:any[] = [];

  allStates:any[] = [
    "Andaman & Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar", 
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and  Daman & Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu & Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana", 
    "Tripura",
    "Uttar Pradesh", 
    "Uttarakhand",
    "West Bengal",
  ]

  // for View Holidays by State 
  selectedState:any;

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private holidayService : HolidayService,
    private datePipe: DatePipe,
    private leaveService : LeaveService,
    private exportExcelService: ExportExcelService,) {
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
    this.isLeavePolicyTable = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;

    this.reset();
    setTimeout(this.setCurrentYearLimit,500);
  }

  showHoliaysTable(){
    this.isHolidayTable = true;

    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
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
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
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
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
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
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
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
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;

    this.holidayObj = Object.assign({}, holiday);
    this.holidayObj.optionalHoliday = (JSON.parse(holiday.optionalHoliday) != null) ? JSON.parse(holiday.optionalHoliday) : false;
    this.holidayObj.customHoliday = (JSON.parse(holiday.customHoliday) != null) ? JSON.parse(holiday.customHoliday) : false;

    setTimeout(this.setCurrentYearLimit,500);
  }

  showLeaveBalanceForm(){
    this.isLeaveBalanceForm = true;
    
    this.isLeaveTypeForm = false;
    this.isHolidayForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.reset();
  }

  showAddLeavePolicyForm(){
    this.isLeavePolicyForm = true;
    this.isCreation = true;
    
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;

    this.reset();
    this.getAllLeaveTypes();
  }

  showUpdateLeavePolicyForm(leavePolicyObj:Leave){
    this.isLeavePolicyForm = true;
    this.isUpdation = true;
    
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;

    this.leavePolicyObj = Object.assign({}, leavePolicyObj);
  }


  showLeavePoliciesTable(){
    this.isLeavePolicyTable = true;
    
    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveRuleTable = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllLeavePolicies();
  }

  reset() {
    this.holidayObj= new Holiday();
    this.holidayObj.optionalHoliday = false;
    this.holidayObj.customHoliday = false;
    this.holidayObj.state = '';
    this.selectedState = '';
    this.holidayList = [];

    this.leaveTypeObj = new Leave();
    this.leaveTypes = [];

    this.leaveBalanceObj = new Leave();
    this.leaveBalanceList = [];

    this.leavePolicyObj = new Leave();
    this.leavePolicyObj.employmentStatus = '';
    this.leavePolicyObj.leaveTypeMasterId = '';
    this.leavePolicyObj.leaveApplication = '';
    this.leavePolicyObj.increment = '';
    this.leavePolicyObj.oneTimeLeave = '';
    this.leavePolicyObj.carryForward = '';
    this.leavePolicyObj.expirationPeriod = '';
    this.leavePolicyObj.lockingPeriod = '';
    this.leavePolicyObj.probation = '';

    this.leavePolicyList = [];
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openDeleteHoliday(template: TemplateRef<any>, holiday: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.holidayObj = holiday;
  }

  openDeleteLeavePolicy(template: TemplateRef<any>, leavePolicy: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.leavePolicyObj = leavePolicy;
  }

  // Holiday
  setCurrentYearLimit(){
    let currentYear = new Date().getFullYear();
    
    let occasionDate = document.getElementById('occasionDate');
    occasionDate?.setAttribute('min', `${currentYear}-01-01`);
    occasionDate?.setAttribute('max', `${currentYear}-12-31`);
  }
  validateHolidayObj(holidayObj:Holiday, template: TemplateRef<any>){

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

    if(!this.validationService.validateNullUndefinedEmptyString(holidayObj.state)){
      this.alertMessage = "Please select State !!"
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

    let inputValidated:boolean  = this.validateHolidayObj(this.holidayObj, template)
    if(!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
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

    let inputValidated:boolean  = this.validateHolidayObj(this.holidayObj, template)
    if(!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
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

  
  onDeleteHoliday(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.holidayService.deleteHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllHolidays();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  onSelect() {
    console.log("selecteddddddd");
    if(this.selectedState == 'all state'){
      this.holidayListFilter = this.holidayList;
    }else{
      this.holidayListFilter = this.holidayList.filter(x => x.state == this.selectedState);
    }
    
  }

  getAllHolidays(){
    this.holidayList = [];
    
    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("holidayList : ", this.holidayList);
        this.holidayListFilter = this.holidayList;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  checkOccasion(template: TemplateRef<any>){
    this.holidayService.checkOccasionIfAlreadyExist(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
              this.openAlertMod(template, response.serviceResponse);
            }
    });
  }

   // download excel
exportToExcel(): void {

  if(this.isHolidayTable == true){
    this.excelName = 'HolidaySheet.xlsx';

    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayDataForExcel = response.serviceResponse;
      }
  
      const onlySpecificDataArr: Partial<Holiday>[] = this.holidayDataForExcel.map(
        x => ({
          occasion: x.occasion,
          dayOfTheWeek: x.dayOfTheWeek,
          dateOfHoliday: x.dateOfHoliday,
          state: x.state
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
    });

  }
  if(this.isLeaveRuleTable == true){
    this.excelName = 'LeaveSheet.xlsx';

    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveDataForExcel = response.serviceResponse;
      }
  
      const onlySpecificDataArr: Partial<Leave>[] = this.leaveDataForExcel.map(
        x => ({
          leaveType: x.leaveType,
          leaveTypeCode: x.leaveTypeCode,
          gender: x.gender,
          noOfDays: x.noOfDays,
          rules: x.rules,
          description: x.description
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
    });

  }
  if(this.isLeavePolicyTable == true){
    this.excelName = 'LeavePolicySheet.xlsx';

    this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leavePolicyDataForExcel = response.serviceResponse;
      }
  
      const onlySpecificDataArr: Partial<Leave>[] = this.leavePolicyDataForExcel.map(
        x => ({
          leavePolicyName: x.leavePolicyName,
          leaveType: x.leaveType,
          description: x.description,
          createdByName: x.createdByName,
          createdOn: x.createdOn
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
    });

  }

}

  
  // Leave Type
  validateLeaveTypeObj(leaveTypeObj:Leave, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveType)){
      this.alertMessage = "Please enter Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(leaveTypeObj.leaveType)){
      this.alertMessage = "Please enter Valid Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveTypeCode)){
      this.alertMessage = "Please enter Leave Type Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateUppercaseAlpha(leaveTypeObj.leaveTypeCode)){
      this.alertMessage = "Please enter Valid Leave Type Code, Only Uppercase Alphabets Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(leaveTypeObj.leaveTypeCode.length > 4){
      this.alertMessage = "Please enter Valid Leave Type Code, Only upto 4 Characters Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    

    if(!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.gender)){
      this.alertMessage = "Please Select Employee Gender !!"
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

    let existingLeaveType = this.leaveTypes.find(leaveType => leaveType.leaveTypeCode == this.leaveTypeObj.leaveTypeCode)
    if(existingLeaveType){
      this.openAlertMod(template, `Leave type against ${existingLeaveType.leaveTypeCode} Already Exist.`);
      return;
    }

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

  // Leave Policies
  validateLeavepolicyObj(leavePolicyObj:Leave, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leavePolicyName)){
      this.alertMessage = "Please enter Leave Policy Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveTypeMasterId)){
      this.alertMessage = "Please Select Leave Type!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.employmentStatus)){
      this.alertMessage = "Please Select Employment Status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // Policy Checks

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveApplication)){
      this.alertMessage = "Please Select Allow Leave Application !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.increment)){
      this.alertMessage = "Please Select Monthly Increment !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.increment == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.incrementValue)){
        this.alertMessage = "Please Select Monthly Increment Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeave)){
      this.alertMessage = "Please Select One time Leave Limit !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.oneTimeLeave == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveMinCount)){
        this.alertMessage = "Please Select One time Leave Minimum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveCount)){
        this.alertMessage = "Please Select One time Leave Maximum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForward)){
      this.alertMessage = "Please Select Carry Forward !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.carryForward == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForwardValue)){
        this.alertMessage = "Please Select Carry Forward Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriod)){
      this.alertMessage = "Please Select Leave Validity Expiration !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.expirationPeriod == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriodValue)){
        this.alertMessage = "Please Select Leave Validity Expiration Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriod)){
      this.alertMessage = "Please Select Leave Application Count Locking !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.lockingPeriod == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriodValue)){
        this.alertMessage = "Please Select Leave Application Count Locking Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingValue)){
        this.alertMessage = "Please Select Leave Application Count Locking Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probation)){
      this.alertMessage = "Please Select Leave Application Probation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(leavePolicyObj.probation == 'Yes'){
      if(!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probation)){
        this.alertMessage = "Please Select Leave Application Probation Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    return true;
  }

  onAddLeavePolicy(template: TemplateRef<any>) {
    let inputValidated:boolean  = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if(!inputValidated) return;

    this.leavePolicyObj.createdBy = this.currentUser.empId;
    console.log("Add Leave Policy : ", this.leavePolicyObj);

    this.leaveService.addLeavePolicy(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateLeavePolicy(template: TemplateRef<any>) {
    let inputValidated:boolean  = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if(!inputValidated) return;

    this.leavePolicyObj.updatedBy = this.currentUser.empId;
    console.log("update Leave Policy : ", this.leavePolicyObj);

    this.leaveService.updateLeavePolicy(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  ondeleteLeavePolicy(template: TemplateRef<any>) {
    console.log("Delete Leave Policy : ", this.leavePolicyObj);

    this.leaveService.deleteLeavePolicyByLeavePolicyMasterId(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllLeavePolicies(){
    this.leavePolicyList = [];

    this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leavePolicyList = response.serviceResponse;
        console.log("leavePolicyList : ", this.leavePolicyList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getLeavePolicyByEmploymentStatusAndLeaveType() {
    console.log("get Leave Policy : ", this.leavePolicyObj);
    this.leaveService.getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log("Leave Policy : ", response.serviceResponse);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

}
