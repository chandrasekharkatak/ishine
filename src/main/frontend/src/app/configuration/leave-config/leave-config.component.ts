import { DatePipe, LocationStrategy } from '@angular/common';
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
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';

@Component({
  selector: 'app-leave-config',
  templateUrl: './leave-config.component.html',
  styleUrls: ['./leave-config.component.css']
})
export class LeaveConfigComponent implements OnInit {
  data: string;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isHolidayForm: boolean = false;
  isLeaveTypeForm: boolean = false;
  isLeaveBalanceForm: boolean = false;
  isHolidayTable: boolean = false;
  isLeaveRuleTable: boolean = false;
  isLeavePolicyForm: boolean = false;
  isLeavePolicyTable: boolean = false;


  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //excel
  excelName = '';
  holidayDataForExcel: any[];
  leaveDataForExcel: any[];
  leavePolicyDataForExcel: any[];

  //obj
  feature = "Leave Config";
  currentUser: User;
  userMapping: any = {};

  holidayObj: Holiday = new Holiday();
  holidayList: any[] = [];
  holidayListFilter: any[] = [];

  leaveTypeObj: Leave = new Leave();
  leaveTypes: any[] = [];
  filterLeaveType: any[] = [];
  newLeaveType: any;
  oldLeaveType: any;

  leaveBalanceObj: Leave = new Leave();
  leaveBalanceList: any[] = [];
  employeeData: any[] = [];

  leavePolicyObj: Leave = new Leave();
  leavePolicyList: any[] = [];

  years: any[] = [];
  selectedYearholidayList: any[] = [];

  allStates: any[] = [
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
  selectedState: any = '';

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private holidayService: HolidayService,
    private datePipe: DatePipe,
    private leaveService: LeaveService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.preventBackButton();
    this.dynamicYearForDropdown();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if (this.userMapping.add_holidays) {
      this.showAddHolidayForm();
    } else if (this.userMapping.view_holidays || this.userMapping.update_holidays || this.userMapping.delete_holiday) {
      this.showHoliaysTable();
    } else if (this.userMapping.add_leave_type) {
      this.showAddLeaveTypeForm();
    } else if (this.userMapping.view_leave_types || this.userMapping.update_leave_type || this.userMapping.delete_leave_type) {
      this.showLeaveTypesTable();
    } else if (this.userMapping.manage_employee_leave_balance) {
      this.showLeaveBalanceForm();
    } else if (this.userMapping.add_leave_policy) {
      this.showAddLeavePolicyForm();
    } else if (this.userMapping.view_leave_policies || this.userMapping.update_leave_policy || this.userMapping.delete_leave_policy) {
      this.showLeavePoliciesTable();
    }
  }

  disableMannualDateInput() {
    return false;
  }

  showAddHolidayForm() {
    this.employeeData = []
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
    setTimeout(this.setCurrentYearLimit, 500);
  }

  showHoliaysTable() {
    this.employeeData = []
    this.isHolidayTable = true;

    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = ''
    this.getAllHolidays();
  }

  showLeaveTypesTable() {
    this.employeeData = []
    this.isLeaveRuleTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = ''
    this.getAllLeaveTypes();
  }

  showAddLeaveTypeForm() {
    this.employeeData = []
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


  showUpdateLeaveTypeForm(leaveType: Leave) {
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

  showUpdateHolidayForm(holiday: Holiday) {
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
    this.holidayObj.optionalHoliday = (holiday.optionalHoliday != null) ? JSON.parse(holiday.optionalHoliday) : false;
    this.holidayObj.customHoliday = (holiday.customHoliday != null) ? JSON.parse(holiday.customHoliday) : false;
    this.holidayObj.dateOfHoliday = (this.holidayObj.dateOfHoliday)? moment(this.holidayObj.dateOfHoliday).format(AppComponent.DB_DATE_FORMAT) : '';

    setTimeout(this.setCurrentYearLimit, 500);
  }

  showLeaveBalanceForm() {
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

  showAddLeavePolicyForm() {
    this.employeeData = []
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

  showUpdateLeavePolicyForm(leavePolicyObj: Leave) {
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
    this.getAllLeaveTypes();
  }


  showLeavePoliciesTable() {
    this.employeeData = []
    this.isLeavePolicyTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveRuleTable = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = ''
    this.getAllLeavePolicies();
  }

  reset() {
    this.holidayObj = new Holiday();
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

  dynamicYearForDropdown() {
    let currentYear = new Date().getFullYear();
    this.years = [];
    this.years.push(currentYear);
    for (var i = 1; i < 2; i++) {
      this.years.push(currentYear - i);
    }
    console.log(this.years, "dynamic year");

  }

  checkLeaveType(leaveTypeObj:Leave, template: TemplateRef<any>){
    let leaveCheck = Object.assign({}, leaveTypeObj);
    console.log("Leave Check : ", leaveCheck);
    this.leaveService.checkLeaveType(leaveCheck).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // Valid Leave Type
      } else {
        this.openAlertMod(template, response.serviceResponse);
        this.leaveTypeObj.leaveType = "";
      }
    });
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

  openDeleteLeaveType(template: TemplateRef<any>, leaveType: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.leaveTypeObj = leaveType;
    console.log(this.leaveTypeObj);
  }

  // Holiday
  setCurrentYearLimit() {
    let currentYear = new Date().getFullYear();
    let currentMonth = new Date().getMonth();
    let nextYear;

    if (currentMonth == 10 || currentMonth == 11) {
      nextYear = currentYear + 1;
    } else {
      nextYear = currentYear;
    }

    let occasionDate = document.getElementById('occasionDate');
    occasionDate?.setAttribute('min', `${currentYear}-01-01`);
    occasionDate?.setAttribute('max', `${nextYear}-12-31`);
  }
  validateHolidayObj(holidayObj: Holiday, template: TemplateRef<any>) {

    if (holidayObj.holidayType == "Festival" || holidayObj.holidayType == "nonWorking") {

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.holidayType)) {
        this.alertMessage = "Please select Holiday Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.occasion)) {
        this.alertMessage = "Please enter occasion Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateAlphabeticCharacters(holidayObj.occasion)) {
        this.alertMessage = "Please enter valid occasion Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.dateOfHoliday)) {
        this.alertMessage = "Please select Date of Holiday !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.state)) {
        this.alertMessage = "Please select State !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;

    } else if (holidayObj.holidayType == "WeekOff") {

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.holidayType)) {
        this.alertMessage = "Please select Holiday Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.dateOfHoliday)) {
        this.alertMessage = "Please select Date of Holiday !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
    }
  }

  setHolidayWeekDay() {
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

    let inputValidated: boolean = this.validateHolidayObj(this.holidayObj, template)
    if (!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    this.holidayObj.createdBy = this.currentUser.empId;
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

    let inputValidated: boolean = this.validateHolidayObj(this.holidayObj, template)
    if (!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    this.holidayObj.updatedBy = this.currentUser.empId;
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


  onDeleteHoliday(template: TemplateRef<any>) {
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
    if (this.selectedState == 'all state') {
      this.holidayListFilter = this.holidayList;
      this.page = 1;
    } else {
      this.holidayListFilter = this.holidayList.filter(x => x.state == this.selectedState);
      this.page = 1;
    }
  }

  getAllHolidays() {

    this.holidayList = [];
    this.holidayListFilter = [];
    this.data = ''

    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayListFilter = response.serviceResponse;
        this.holidayListFilter.forEach(holiday => {
          holiday.dateOfHoliday = (holiday.dateOfHoliday) ? moment(holiday.dateOfHoliday).format(AppComponent.DATE_FORMAT) : null;
          holiday.createdOn = (holiday.createdOn) ? moment(holiday.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          holiday.updatedOn = (holiday.updatedOn) ? moment(holiday.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("holidayList : ", this.holidayListFilter);
        // this.holidayListFilter = this.holidayList;
        this.holidayList = this.holidayListFilter;
        const currentYear = new Date().getFullYear();	
        this.holidayListFilter = this.holidayListFilter.filter(x=>new Date (x.dateOfHoliday).getFullYear() == currentYear);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getFilterHolidayList(value: any) {
    this.selectedYearholidayList = [];
    let searchYear = parseInt(value);
    console.log(searchYear, "searchYear")
    console.log(this.holidayList, "this.holidayListthis.holidayList")
    this.holidayList.forEach(holiday => {
      const year = new Date(holiday.dateOfHoliday).getFullYear();
      if (searchYear === year) {
        this.selectedYearholidayList.push(holiday);
      }
    });
    this.holidayListFilter = this.selectedYearholidayList;
    console.log(this.holidayListFilter, "this.holidayListFilter")
  }

  checkOccasion(template: TemplateRef<any>) {
    this.holidayService.checkOccasionIfAlreadyExist(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // download excel
  exportToExcel(): void {

    if (this.isHolidayTable == true) {
      this.excelName = 'HolidaySheet.xlsx';

      this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.holidayDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.holidayDataForExcel.map(
          x => ({
            "Occasion": x.occasion,
            "Day": x.dayOfTheWeek,
            "Date": (x.dateOfHoliday) ? moment(x.dateOfHoliday).format(AppComponent.DATETIME_FORMAT) : null,
            "State": x.state
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });

    }
    if (this.isLeaveRuleTable == true) {
      this.excelName = 'LeaveSheet.xlsx';

      this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.leaveDataForExcel.map(
          x => ({
            "Leave Type": x.leaveType,
            "Leave Code": x.leaveTypeCode,
            "Employee Gender": x.gender,
            "Default Leaves": x.noOfDays,
            "Rules": x.rules,
            "Description": x.description
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });

    }
    if (this.isLeavePolicyTable == true) {
      this.excelName = 'LeavePolicySheet.xlsx';

      this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leavePolicyDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.leavePolicyDataForExcel.map(
          x => ({
            "Leave Policy Name": x.leavePolicyName,
            "Leave Type": x.leaveType,
            "Description": x.description,
            "Created By": x.createdByName,
            "Created On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });

    }

  }


  // Leave Type
  validateLeaveTypeObj(leaveTypeObj: Leave, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveType)) {
      this.alertMessage = "Please enter Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(leaveTypeObj.leaveType)) {
      this.alertMessage = "Please enter Valid Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveTypeCode)) {
      this.alertMessage = "Please enter Leave Type Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateUppercaseAlpha(leaveTypeObj.leaveTypeCode)) {
      this.alertMessage = "Please enter Valid Leave Type Code, Only Uppercase Alphabets Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (leaveTypeObj.leaveTypeCode.length > 4) {
      this.alertMessage = "Please enter Valid Leave Type Code, Only upto 4 Characters Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.gender)) {
      this.alertMessage = "Please Select Employee Gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.noOfDays)) {
      this.alertMessage = "Please enter Default Leave Days !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onAddLeaveType(template: TemplateRef<any>) {
    this.leaveTypeObj.leaveType = this.leaveTypeObj.leaveType?.trim();
    this.leaveTypeObj.leaveTypeCode = this.leaveTypeObj.leaveTypeCode?.trim();
    this.leaveTypeObj.description = this.leaveTypeObj.description?.trim();
    this.leaveTypeObj.rules = this.leaveTypeObj.rules?.trim();

    let inputValidated: boolean = this.validateLeaveTypeObj(this.leaveTypeObj, template)
    if (!inputValidated) return;

    let existingLeaveType = this.leaveTypes.find(leaveType => leaveType.leaveTypeCode == this.leaveTypeObj.leaveTypeCode)
    if (existingLeaveType) {
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
    this.leaveTypeObj.updatedBy = this.currentUser.empId;

    this.leaveService.updateLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllLeaveTypes() {
    this.leaveTypes = [];
    this.data = ''
    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        console.log("leaveTypes : ", this.leaveTypes);
        this.leaveTypes.forEach((leaveObj)=>{
          leaveObj.updatedOn = (leaveObj.updatedOn) ? moment(leaveObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        })
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  onDeleteLeaveType(template: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    this.cancelRequest();

    this.filterLeaveType = this.leaveTypes.filter(x => x.leaveTypeMasterId !== this.leaveTypeObj.leaveTypeMasterId)
    this.oldLeaveType = this.leaveTypeObj.leaveTypeMasterId;

    this.leaveService.deleteLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(alertTemplate, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.leaveTypeObj.newLeaveTypeMasterId = '';
        this.modalRef = this.modalService.show(template);
      }
    });
  }

  validateLeaveTypeMappingObj(leaveTypeObj: Leave, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.newLeaveTypeMasterId)) {
      this.alertMessage = "Please select Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onChangeLeaveTypeMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateLeaveTypeMappingObj(this.leaveTypeObj, template)
    if (!inputValidated) return;

    this.leaveTypeObj.leaveTypeMasterId = this.leaveTypeObj.newLeaveTypeMasterId;
    this.leaveTypeObj.oldLeaveTypeMasterId = this.oldLeaveType;

    this.leaveService.changeLeaveTypeMapping(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.leaveTypeObj.leaveTypeMasterId = this.oldLeaveType;
        this.leaveService.deleteLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {

            this.openAlertMod(template, response.serviceResponse);
            this.showLeaveTypesTable();
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  fieldRestictCharacter(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 123) || (k == 124) || (k == 125) || (k == 126)) {
      return (false);
    }
    return (true);

  }

  // Manage Leave Balance
  onGetEmpLeaveBalance(template: TemplateRef<any>) {
    this.leaveBalanceList = [];
    this.employeeData = [];
    let leaveObj: Leave = new Leave();
    if (this.leaveBalanceObj.employeementId.startsWith('A-')) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.leaveBalanceObj.employeementId)) {
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      leaveObj.employeementId = this.leaveBalanceObj.employeementId.substring(2);
      console.log("Employee :", this.leaveBalanceObj);
    } else {
      leaveObj.employeementId = this.leaveBalanceObj.employeementId
    }
    this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        this.leaveBalanceObj.empId = response.serviceResponse1;
        this.employeeData = response.serviceResponse2;
        console.log("employeeData : ", this.employeeData);
        console.log("leaveBalanceList : ", this.leaveBalanceList);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  leaveBalanceInputValidation(balance: any, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(balance)) {
      this.alertMessage = "Please enter Valid Balance !!"
      this.openAlertMod(template, this.alertMessage);
    }
  }

  onUpdateLeaveBalance(template: TemplateRef<any>) {

    let inputValidated = true;
    this.leaveBalanceList.forEach(leave => {
      if (!this.validationService.validateNullUndefinedEmptyString(leave.balance)) {
        this.alertMessage = "Please enter Valid Balance !!"
        inputValidated = false;
        return;
      }
    });

    if (!inputValidated) {
      this.openAlertMod(template, this.alertMessage)
      return false;
    };

    this.leaveBalanceObj.employeeLeaveList = this.leaveBalanceList;
    this.leaveBalanceObj.employeementId = this.leaveBalanceObj.employeementId?.substring(2)
    console.log("manage Leave Balance :", this.leaveBalanceObj);
    this.leaveService.updateLeavesByEmpId(this.leaveBalanceObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveBalanceForm();
        this.employeeData = []
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Leave Policies
  validateLeavepolicyObj(leavePolicyObj: Leave, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leavePolicyName)) {
      this.alertMessage = "Please enter Leave Policy Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveTypeMasterId)) {
      this.alertMessage = "Please Select Leave Type!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.employmentStatus)) {
      this.alertMessage = "Please Select Employment Status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // Policy Checks

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveApplication)) {
      this.alertMessage = "Please Select Allow Leave Application !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.increment)) {
      this.alertMessage = "Please Select Monthly Increment !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.increment == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.incrementValue)) {
        this.alertMessage = "Please Select Monthly Increment Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeave)) {
      this.alertMessage = "Please Select One time Leave Limit !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.oneTimeLeave == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveMinCount)) {
        this.alertMessage = "Please Select One time Leave Minimum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveCount)) {
        this.alertMessage = "Please Select One time Leave Maximum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForward)) {
      this.alertMessage = "Please Select Carry Forward !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.carryForward == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForwardValue)) {
        this.alertMessage = "Please Select Carry Forward Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriod)) {
      this.alertMessage = "Please Select Leave Validity Expiration !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.expirationPeriod == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriodValue)) {
        this.alertMessage = "Please Select Leave Validity Expiration Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriod)) {
      this.alertMessage = "Please Select Leave Application Count Locking !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.lockingPeriod == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriodValue)) {
        this.alertMessage = "Please Select Leave Application Count Locking Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingValue)) {
        this.alertMessage = "Please Select Leave Application Count Locking Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probation)) {
      this.alertMessage = "Please Select Leave Application Probation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.probation == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probationPeriod)) {
        this.alertMessage = "Please Enter Leave Application Probation Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    return true;
  }

  onAddLeavePolicy(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if (!inputValidated) return;

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
    let inputValidated: boolean = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if (!inputValidated) return;

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

  getAllLeavePolicies() {

    this.data = ''
    this.leavePolicyList = [];

    this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leavePolicyList = response.serviceResponse;
        this.leavePolicyList.forEach(leavePolicy => {
          leavePolicy.createdOn = (leavePolicy.createdOn) ? moment(leavePolicy.createdOn).format(AppComponent.DATE_FORMAT) : null;
          leavePolicy.updatedOn = (leavePolicy.updatedOn) ? moment(leavePolicy.updatedOn).format(AppComponent.DATE_FORMAT) : null;
        });
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

  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
