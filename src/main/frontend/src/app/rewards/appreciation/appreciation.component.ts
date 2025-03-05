import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ClipboardService } from 'ngx-clipboard';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { enableAppreciation } from 'src/app/models/enableAppreciation';
import { Feature } from 'src/app/models/feature';
import { Help } from 'src/app/models/help';
import { Holiday } from 'src/app/models/holiday';
import { Query } from 'src/app/models/query';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { HelpService } from 'src/app/services/help.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { PortalService } from 'src/app/services/portal.service';
import { ValidationService } from 'src/app/services/validation.service';



class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-appreciation',
  templateUrl: './appreciation.component.html',
  styleUrls: ['./appreciation.component.css']
})
export class AppreciationComponent implements OnInit {

 
  viewEventConfig: boolean = false;
  [x: string]: any;
  feature = "Portal Config";
  currentUser: User;
  userMapping: any = {};
  allEmployeeList: any;
  allAppreciationEvent: any;
  data: any;
  appByCategory: any;
  src: any;
  fileName: any;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  helpObj: Help = new Help();
  holidayObj: Holiday = new Holiday();

  maxFileSize: any;
  maxRequestSize: any;
  fileSize: number = 0;
  helpDocumentName: any;

  //flag
  portalConfig: boolean = false;
  appreciationConfig: boolean = false;
 
  isTable: boolean = false;
  isCreation: boolean = false;
  viewAppreciationForm: boolean = false;
  isAppreciationTable: boolean = false;
  isUpdation: boolean = false;
  fromDate: any;
  toDate: any;
  isHelpConfiguration: boolean = false;
  isHelpTable: boolean = false;
  isUploadForm: boolean = false;
  isAppreciationInfo: boolean = false;

  alertMessage: any;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  modalRef: BsModalRef = new BsModalRef();

  // portalObj: Portal = new Portal();
  timesheetObj: Timesheet = new Timesheet();

  portalConfigList: any[] = [];
  appreciationColumns: any[] = ['Department'];
  queryList: any[] = [];
  filterData: any = new FilterData();
  display = null;
  appreciationObj: enableAppreciation = new enableAppreciation();
  all: any;
  enableAppreciationList: any[] = [];
  holidayList: any[] = [];
  holidayDates: any[] = [];

  allDeptList: any[] = [];
  employeeList: any[] = [];
  document: any[] = [];
  files: any[] = [];
  EventSummaryInfo: any;
  EmployeeAppreciationList: any[] = [];
  AppreciationInfo: any[] = [];


  allMonth: any[] = [
    "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
  ]

  year: any[] = [];

  filters: any = {};
  isSearchEnabled: boolean = false;
  employeeColumns: any[] = ['employeementId', 'name', 'email', 'departmentName', 'employmentstatus', 'dateOfJoining'];
  appreciationTableColumns: any[] = ['appreciateType', 'appreciationToName', 'appreciationByName', 'appreciationDate', 'managerName', 'reason'];
  documentsColumns: any[] = ['blank', 'fileName', 'helpDocumentName', 'createdByName', 'createdOn'];
  constructor(private portalService: PortalService,
    private validationService: ValidationService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private locationStrategy: LocationStrategy,
    private departmentService: DepartmentService,
    private helpService: HelpService,
    private datePipe: DatePipe,
    private clipboardService: ClipboardService,
    private holidayService: HolidayService,) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

   ngOnInit(): void {
      // Dynamic Subfeature Flags
      let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });


      console.log(this.feature, " : ", this.userMapping);
      this.getYear();
      this.getAllEvent();
      this.sectionViewInit();
      this.preventBackButton();
      this.getAllHolidays();
      this.viewEventConfig = true;
    }
    preventBackButton() {
      history.pushState(null, null, location.href);
      this.locationStrategy.onPopState(() => {
        history.pushState(null, null, location.href);
      })
    }
  
    reset() {
      this.appreciationObj = new enableAppreciation();
      this.appreciationObj.fromDate = null;
      this.appreciationObj.toDate = null;
      this.appreciationObj.appreciationEventName = null;
      this.appreciationObj.appreciationEventType = "";
      this.appreciationObj.appreciationEventName = "";
      this.appreciationObj.appreciateType = "";
  
    }
  
    sectionViewInit() {
      if (this.userMapping.update_portal_global_configuration) {
        this.getAllPortalConfig();
      }
      else if (this.userMapping.appreciation_configuration) {
        this.enableAppreciationOnclick();
      }
      else if (this.userMapping.appreciation_configuration) {
        this.viewAppreciationEventOnClick();
      }
      else if (this.userMapping.view_employees_appreciation) {
        this.viewAllAppreciation();
      }
    }
  
    viewHelpDocument(){
      this.isHelpConfiguration = true;
      this.isHelpTable = true;
  
      this.isUploadForm = false;
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.isTable = false;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.viewEventConfig = false;
      this.isTable = false;
      this.isUpdation = false;
  
      this.getAllHelpDocument();
    }
  
    showUploadForm(){
      this.isHelpConfiguration = true;
      this.isUploadForm = true;
  
      this.isHelpTable = false;
  
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.isTable = false;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.viewEventConfig = false;
      this.isTable = false;
      this.isUpdation = false;
  
      this.maxFileSize = parseInt(sessionStorage.maxFileSize);
      this.maxRequestSize = parseInt(sessionStorage.maxRequestSize);
    }
  
    getAllEvent() {
      this.allAppreciationEvent = [];
      this.portalService.getAllEvent().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allAppreciationEvent = response.serviceResponse;
          this.allAppreciationEvent.forEach(event => {
            // event.fromDate = (event.fromDate)? moment(event.fromDate).format(AppComponent.DATE_FORMAT) : null;
            // event.toDate = (event.toDate)? moment(event.toDate).format(AppComponent.DATE_FORMAT) : null;
            event.createdOn = (event.createdOn) ? moment(event.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          // this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
          //console.log("allAppreciationEventList : ", this.allAppreciationEvent);
        } else {
          //this.openAlertMod(this.alertTemplate, response.serviceResponse)
          //console.log("allAppreciationEventList: ", this.allAppreciationEvent);
  
        }
      });
  
    }
  
    onDateChange(template: TemplateRef<any>) {
      if (this.appreciationObj.toDate < this.appreciationObj.fromDate) {
        if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.fromDate)) {
          this.alertMessage = "Please enter from Date !!"
          this.openAlertMod(template, this.alertMessage);
          this.appreciationObj.toDate = ''
          return false;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.toDate)) {
          this.alertMessage = "Please enter To Date !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
        //console.log("end date is small");
        // this.appreciationObj.toDate = ''
        this.alertMessage = "From Date should be less Than To Date !!"
        this.openAlertMod(template, this.alertMessage);
        this.appreciationObj.fromDate = ''
  
      } else {
        this.allEmployeeList = [];
      }
    }
  
    onToDateChange(template: TemplateRef<any>) {
      if (this.appreciationObj.toDate > this.appreciationObj.fromDate) {
        if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.fromDate)) {
          this.alertMessage = "Please enter From Date !!"
          this.openAlertMod(template, this.alertMessage);
          // this.appreciationObj.fromDate = ''
          return false;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.toDate)) {
          this.alertMessage = "Please enter To Date !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
  
      } else {
        this.alertMessage = "To Date should be Greater Than From Date !!"
        this.openAlertMod(template, this.alertMessage);
        this.appreciationObj.toDate = ''
        this.allEmployeeList = []
      }
    }
  
    OnCheckEventName(template: TemplateRef<any>) {
      //console.log("here in checkPoint event name")
  
      this.portalService.OnCheckEventName(this.appreciationObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Fail") {
          this.openAlertMod(template, response.serviceResponse);
          this.appreciationObj.appreciationEventName = '';
        }
      });
  
    }
  
    getAllPortalConfig() {
      this.appreciationObj.fromDate = ''
      this.appreciationObj.toDate = ''
      this.portalConfig = true;
      this.appreciationConfig = false;
      this.isTable = false;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.viewEventConfig = false;
      this.isTable = false;
      this.isUpdation = false;
      this.isHelpConfiguration = false;
  
      this.getAllPortalConfigData();
      this.getAllDepartmentList();
      this.getEmployeeList();
    }
  
    holidayDateFilter = (d: Date)=>{
      const time=d?.getTime();
  
      return (this.holidayDates.find(x=>x.getTime()==time));
    }
  
    getAllHolidays(){
      this.holidayList = [];
      this.holidayDates = [];
      this.holidayObj.state = this.currentUser.workLocation
      this.holidayService.getAllHolidays(this.holidayObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.holidayList = response.serviceResponse;
          this.holidayDates = this.holidayList.map(holiday => new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')));
          //console.log("holidayDates : ", this.holidayDates);
          //console.log("holidayList : ", this.holidayList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  
    reconsileHolidayTimesheet(template: TemplateRef<any>){
      this.holidayObj.dateOfHoliday = (this.holidayObj.dateOfHoliday)? moment(this.holidayObj.dateOfHoliday).format(AppComponent.DB_DATE_FORMAT) : null;
  
      this.holidayService.reconsileHolidayTimesheet(this.holidayObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
    getAllPortalConfigData() {
  
      this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.portalObj = Object.assign({}, response.serviceResponse);
          this.portalConfigList = response.serviceResponse;
  
          //console.log(this.portalConfigList, "   :  portalConfigList");
          //console.log(this.portalObj, "   :  portalObj");
  
          for (let portal of this.portalConfigList) {
            if (portal.configName == 'Probation Period') {
              this.portalObj.probationPeriod = portal.configPeriod;
              this.portalObj.probationMailTrigger = portal.mailTrigger;
            }
            if (portal.configName == 'Notice Period') {
              this.portalObj.noticePeriod = portal.configPeriod;
              this.portalObj.noticeMailTrigger = portal.mailTrigger;
            }
            if (portal.configName == 'OTRS Link') {
              this.portalObj.otrsLink = portal.configValue;
            }
            if (portal.configName == 'SNIPIT Link') {
              this.portalObj.snipitLink = portal.configValue;
            }
            if (portal.configName == 'DSR Download Path') {
              this.portalObj.dsrDownloadPath = portal.configValue;
            }
            if (portal.configName == 'DSR Day') {
              this.portalObj.dsrGenerateDay = portal.configValue;
            }
            if (portal.configName == 'Leave Approval Escalation (Level 1)') {
              this.portalObj.level1MinNoOfDays = portal.configPeriod;
              this.portalObj.level1ApprovalTo = portal.configValue;
            }
            if (portal.configName == 'Leave Approval Escalation (Level 2)') {
              this.portalObj.level2MinNoOfDays = portal.configPeriod;
              this.portalObj.level2ApprovalTo = JSON.parse(portal.configValue);
            }
            if (portal.configName == 'Leave week-off/holiday exclusion') {
              this.portalObj.weekOffExcludedDepartmentList = JSON.parse(portal.configValue);
            }
          }
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  
    enableAppreciationOnclick() {
     this.queryList=[];
      this.appreciationObj.fromDate = ''
      this.appreciationObj.toDate = ''
      this.appreciationObj.appreciationEventName = ''
      this.appreciationObj.appreciationEventType = ''
      this.portalConfig = false;
      this.appreciationConfig = true;
      this.viewAppreciationForm = false;
      this.isTable = false;
      this.fromDate = null;
      this.toDate = null;
      this.isAppreciationTable = false;
      this.isCreation = true;
      this.viewEventConfig = false;
      this.isUpdation =  false;
      this.isHelpConfiguration = false;
      this.reset();
    }
  
    getAllEmployees(template: TemplateRef<any>) {
      this.portalService.getAllEmployees().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;
          this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
          this.allEmployeeList.forEach((employee) => {
            employee.employeementId = "A-".concat(employee.employeementId)
          });
          //console.log("allEmployeeList : ", this.allEmployeeList);
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  
    viewAllAppreciation() {
      this.appreciationObj.appreciateType = '';
      this.appreciationObj.appreciationEventId = ''
      this.appreciationObj.fromDate = ''
      this.appreciationObj.toDate = ''
      this.data = ''
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.viewAppreciationForm = true;
      this.isAppreciationTable = false;
      this.isTable = false;
      this.viewEventConfig = false;
      this.isCreation = false;
      this.isUpdation = false;
      this.isHelpConfiguration = false;
    }
    isEmployeeSelectionChanged: boolean = false;
    changeEvent(template: TemplateRef<any>, columns: any[], title: any, value: string) {
      this.isEmployeeSelectionChanged = true;
      if (value == "all") {
        this.isTable = true;
        this.isDataAVailableInFilter = true;
        this.allEmployeeList = [];
      }
      else if (value == "custom") {
        this.isTable = true;
        this.openFilterModal(template, columns, title);
      }
      this.getAllEmployees(template);
    }
  
    updatePortalGlobalConfiguration(portalObj, template: TemplateRef<any>) {
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationPeriod)) {
        this.alertMessage = "Please enter Probation Period !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }if (portalObj.probationPeriod > 365 || portalObj.probationPeriod < 0) {
        this.alertMessage = "Please enter value 0 to 365 in probation period field !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } if (!this.validationService.validateNumber(portalObj.probationPeriod)) {
        this.alertMessage = "Please enter valid probation period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationMailTrigger)) {
        this.alertMessage = "Please enter Probation Period Mail Trigger !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticePeriod)) {
        this.alertMessage = "Please enter Notice Period !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }if (portalObj.noticePeriod > 365 || portalObj.noticePeriod < 0) {
        this.alertMessage = "Please enter value 0 to 365 in notice period field !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } if (!this.validationService.validateNumber(portalObj.noticePeriod)) {
        this.alertMessage = "Please enter valid notice period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticeMailTrigger)) {
        this.alertMessage = "Please enter Notice Period Mail Trigger !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.otrsLink)) {
        this.alertMessage = "Please enter OTRS Link !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      } else if (this.validationService.validateUrl(portalObj.otrsLink) == false) {
        this.alertMessage = "Please valid OTRS Link !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.snipitLink)) {
        this.alertMessage = "Please enter SNIPIT Link !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      } else if (this.validationService.validateUrl(portalObj.snipitLink) == false) {
        this.alertMessage = "Please valid SNIPIT Link !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.dsrDownloadPath)) {
        this.alertMessage = "Please enter DSR folder path !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.dsrGenerateDay)) {
        this.alertMessage = "Please enter DSR Generation Day !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      } else if (!this.validationService.validateMonthDays(portalObj.dsrGenerateDay)) {
        this.alertMessage = "Please enter valid day !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.level1MinNoOfDays)) {
        this.alertMessage = "Please Leave Approval Escalation (Level 1) : Min. No. of Days !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.level1ApprovalTo)) {
        this.alertMessage = "Please select Leave Approval Escalation (Level 1) : Approval To !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.level2MinNoOfDays)) {
        this.alertMessage = "Please enter Leave Approval Escalation (Level 2) : Min. No. of Days !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(portalObj.level2ApprovalTo)) {
        this.alertMessage = "Please select Leave Approval Escalation (Level 2) : Approval To !!"
        this.openAlertMod(template, this.alertMessage);
        return;
      }
  
  
  
      let tempArray = JSON.parse(JSON.stringify(this.portalConfigList));
  
      tempArray.forEach((portalConfig, index) => {
  
        if (index == 0) {
          portalConfig.configPeriod = portalObj.probationPeriod;
          portalConfig.mailTrigger = portalObj.probationMailTrigger;
        }
        else if (index == 1) {
          portalConfig.configPeriod = portalObj.noticePeriod;
          portalConfig.mailTrigger = portalObj.noticeMailTrigger;
        } else if (index == 2) {
          portalConfig.configValue = portalObj.otrsLink;
        } else if (index == 3) {
          portalConfig.configValue = portalObj.snipitLink;
        } else if (index == 4) {
          portalConfig.configValue = portalObj.dsrDownloadPath;
        } else if (index == 5) {
          portalConfig.configValue = portalObj.dsrGenerateDay;
        } else if (index == 6) {
          portalConfig.configPeriod = portalObj.level1MinNoOfDays
          portalConfig.configValue = portalObj.level1ApprovalTo;
        } else if (index == 7) {
          portalConfig.configPeriod = portalObj.level2MinNoOfDays
          portalConfig.configValue = portalObj.level2ApprovalTo;
        } else if (index == 8) {
          portalObj.weekOffExcludedDepartmentList = JSON.stringify(portalObj.weekOffExcludedDepartmentList);
          portalConfig.configValue = portalObj.weekOffExcludedDepartmentList;
        }
  
      })
      portalObj.allPortalConfigData = tempArray;
  
      this.portalService.updatePortalConfig(portalObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getAllPortalConfigData();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
  
    generatePerviousMonthDSR(template: TemplateRef<any>) {
      this.portalService.generatePerviousMonthDSR().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
    getYear(){
      this.year = [];
      let currentYear = new Date().getFullYear();
      this.year.push(currentYear);
      this.year.push(currentYear -1);
      this.year.push(currentYear -2);
    }
  
    generateAllEmployeeDSR(template: TemplateRef<any>){
      if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.month)) {
        this.alertMessage = "Please Select Month !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.year)) {
        this.alertMessage = "Please Select Year !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      this.timesheetObj.isCron = false;
      this.portalService.generateAllEmployeeDSR(this.timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
    /* Filter */
    openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
      //console.log("columns : ", columns);
  
      this.filterData.title = title;
      this.filterData.columns = columns;
      this.filterData.queryList = JSON.stringify(this.queryList);
  
      //console.log("filterData : ", this.filterData);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
  
    onFilterSubmit(queryList: any, template: TemplateRef<any>) {
      //console.log("queryList : ", queryList);
  
      /* queryList Store query object and Stored data to re-populate same conditions if filter is re-opened.
        Here we dont require any Stored data from Custom filter, hence assigning query object from queryList at index 0
      */
  
      let queryObj = queryList[0]
      this.queryList = queryList[0];
      //console.log("queryObj : ", queryObj);
  
      this.cancelRequest();
  
      if (this.filterData.title == 'Filter Appreciation') {
        this.getCustomEmployeeList(queryObj, template);
      }
    }
  
    isDataAVailableInFilter:boolean = false;
    getCustomEmployeeList(queryObjList: any, template: TemplateRef<any>) {
      this.allEmployeeList = [];
      let queryObj = new Query();
      queryObj.queryList = queryObjList;
      if (queryObjList == '') {
        this.getAllEmployees(template);
  
      } else {
        this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.allEmployeeList = response.serviceResponse;
            //console.log("response" + response);
  
            this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
            //console.log(this.allEmployeeList, "   this.allEmployeeList");
            this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
              index === self.findIndex((t) => (
                t.employeementId === value.employeementId
              ))
            )
  
            if (this.allEmployeeList.length != 0) {
              this.isDataAVailableInFilter = true;
              this.openAlertMod(template, "Employee Record found")
            } else {
              this.isDataAVailableInFilter = false;
              this.openAlertMod(template, "No Data found")
            }
            this.allEmployeeList.forEach(employee => {
              employee.employeementId = "A-".concat(employee.employeementId);
            });
            //console.log("allEmployeeList : ", this.allEmployeeList)
          } else {
            this.openAlertMod(template, response.serviceResponse)
          }
        });
      }
    }
    validateAppreciation(appreciationObj: enableAppreciation, template: TemplateRef<any>) {
  
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.fromDate)) {
        this.alertMessage = "Please Select From Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.toDate)) {
        this.alertMessage = "Please select To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.appreciationEventName)) {
        this.alertMessage = "Event Name field should not be empty!!!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateAlphaWithSpaceInbetween(appreciationObj.appreciationEventName)) {
        this.alertMessage = "Please Enter Valid Event Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
    }
  
  
    fromDateFilter = (d: Date) => {
      const dateFormat = 'YYYY-MM-DD';
      const currentDate = new Date();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      const BACKDATED_LEAVE_PERIOD = 0;
      const FUTUREDATED_LEAVE_PERIOD = 365;
      const time = d?.getTime();
      let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
      let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));
  
      return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)));
  
    }
    toDateFilter = (d: Date) => {
      const dateFormat = 'YYYY-MM-DD';
      const currentDate = new Date();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      const BACKDATED_LEAVE_PERIOD = 0;
      const FUTUREDATED_LEAVE_PERIOD = 365;
      const time = d?.getTime();
      let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
      let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));
  
      return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && (moment(d).format(dateFormat) >= moment(this.appreciationObj.fromDate).format(dateFormat)));
  
    }
  
    enableAppreciation(template: TemplateRef<any>) {
      const dateFormat = 'YYYY-MM-DD';
      let inputValidated: boolean = this.validateAppreciation(this.appreciationObj, template)
      if (!inputValidated) return;

      if (!this.isDataAVailableInFilter) {
        this.openAlertMod(template, "No employee data found. Appreciation cannot be enabled.");
        return;
    }
      this.enableAppreciationList = [];
  
      this.enableAppreciationList = this.allEmployeeList.map(employee => {
        return {
          // employeementId : employee.employeementId,
          empId: employee.empId,
          isAppreciationEnable: true
        }
      });
  
      //console.log("enableAppreciationList : ", this.enableAppreciationList);
      this.appreciationObj.fromDate = moment(this.appreciationObj.fromDate).format(dateFormat)
      this.appreciationObj.toDate = moment(this.appreciationObj.toDate).format(dateFormat)
  
      this.appreciationObj.appreciationEventName = this.appreciationObj.appreciationEventName;
      this.appreciationObj.enableAppreciationList = this.enableAppreciationList;
      //console.log("enableAppreciation : ", this.appreciationObj)
  
      let checkEventDate = this.allAppreciationEvent.find(x => x.fromDate == this.appreciationObj.fromDate || x.toDate == this.appreciationObj.toDate || ((x.fromDate <= this.appreciationObj.toDate) && (this.appreciationObj.fromDate <= x.toDate)));
      if (checkEventDate != undefined) {
        this.openAlertMod(template, "Event is already exist on this date");
        this.appreciationObj.fromDate = [];
        this.appreciationObj.toDate = [];
        this.allEmployeeList = []
        this.appreciationObj.appreciationEventName = ''
        this.appreciationObj.appreciationEventType = ''
        this.isTable = false
      }
      else {
        this.portalService.enableAppreciation(this.appreciationObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.all = response.serviceResponse;
            //console.log("appreciation : ", this.all)
            this.openAlertMod(template, response.serviceResponse);
            this.viewAppreciationEventOnClick();
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      }
    }
  
    disableMannualDateInput() {
      return false;
    }
  
    viewAppreciationsOnSubmit(appreciationObj: enableAppreciation, template: TemplateRef<any>) {
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.viewAppreciationForm = false;
      // this.isAppreciationTable = true;
      this.isTable = false;
  
      this.isAppreciationTable = true;
      this.viewEventConfig = false;
      this.isCreation = false;
      this.isUpdation = false;
      this.filters = {};
      this.isSearchEnabled = false;
  
      this.viewAppreciations(appreciationObj, template);
  
  
    }
  
    viewAppreciations(appreciationObj: enableAppreciation, template: TemplateRef<any>) {
  
      this.appByCategory = [];
      this.appreciationObj.appreciationEventId = this.appreciationObj.appreciationEventId;
      this.appreciationObj.appreciateType = this.appreciationObj.appreciateType;
  
      //console.log(appreciationObj, "appreciationObj");
  
      let inputValidated: boolean = this.validateViewAppreciation(appreciationObj, template)
      if (!inputValidated) return;
      //console.log(appreciationObj, "appreciationObj");
  
      this.portalService.viewAppreciations(this.appreciationObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.appByCategory = response.serviceResponse;
          this.appByCategory.forEach(appr => {
            appr.appreciationDate = (appr.appreciationDate) ? moment(appr.appreciationDate).format(AppComponent.DATETIME_FORMAT) : null;
          });
          //console.log("appByCategory : ", this.appByCategory)
          this.isAppreciationTable = true;
          //this.reset();
        } else {
          // this.openAlertMod(template, response.serviceResponse)
          console.error(response.serviceResponse);
        }
        this.isAppreciationTable = true;
      });
  
    }
    getAppreciationEventSummaryInfo(appreciationEvent: any){
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.viewAppreciationForm = true;
      this.isAppreciationTable = false;
      this.isTable = false;
      this.viewEventConfig = false;
      this.isCreation = false;
      this.isUpdation = false;
      this.isHelpConfiguration = false;
      this.appreciationObj = appreciationEvent;
      //console.log("appreciationObj :",this.appreciationObj);
      this.portalService.getAppreciationEventSummaryInfo(this.appreciationObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
           this.EventSummaryInfo= response.serviceResponse;
           //console.log("EventSummaryInfo :",this.EventSummaryInfo);
          }
        else {
          console.error(response.serviceResponse);
  
        }
      });
  
      this.portalService.getAllEmployeeAppreciationListByCategory(this.appreciationObj).pipe(first()).subscribe((response:any) =>{
        if (response.serviceStatus =="Success"){
          this.EmployeeAppreciationList = response.serviceResponse;
          //console.log("EmployeeAppreciationList :",this.EmployeeAppreciationList);
  
  
        }
        else {
          console.error(response.serviceResponse);
  
        }
      });
  
    }
  
  
    viewAppreciationInfo(EmployeeAppreciationList:any){
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.isTable = false;
      this.viewEventConfig = false;
      this.isCreation = false;
      this.isUpdation = false;
      this.isHelpConfiguration = false;
      this.isAppreciationInfo = true;
      this.EmployeeAppreciationList.forEach(element => {
        this.appreciationObj[element.employeement_id] = element.employeement_id;
      });
      this.portalService.viewAppreciationInfo(this.appreciationObj).pipe(first()).subscribe((response:any) =>{
        if (response.serviceStatus =="Success"){
          this.AppreciationInfo = response.serviceResponse;
          //console.log("appreciationObj:" ,this.appreciationObj);
          //console.log("AppreciationInfo :",this.AppreciationInfo);
        }
        else {
          console.error(response.serviceResponse);
  
        }
      });
    }
  
  
    validateViewAppreciation(appreciationObj: enableAppreciation, template: TemplateRef<any>) {
  
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.appreciationEventId)) {
        this.alertMessage = "Please Select Appreciation EventName !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.appreciateType)) {
        this.alertMessage = "Please select appreciateType !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
    }
  
  
    viewAppreciationEventOnClick() {
      this.portalConfig = false;
      this.appreciationConfig = false;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.isTable = false;
      this.viewEventConfig = true;
      this.isCreation = false;
      this.isUpdation = false;
      this.isAppreciationInfo = false;
      this.getAllEvent();
  
    }
    validateEnableAppreciationObj(appreciationObj: enableAppreciation, template: TemplateRef<any>) {
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.fromDate)) {
        this.alertMessage = "Please enter From Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (!this.validationService.validateAlphaWithSpace(appreciationObj.appreciationEventName)) {
        this.alertMessage = "Please enter Valid Event Name!!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.toDate)) {
        this.alertMessage = "Please select To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.appreciationEventType)) {
        this.alertMessage = "Please select Employees  !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
    }
    onUpdateAppreciationEvent(template: TemplateRef<any>) {
      const dateFormat = 'YYYY-MM-DD';
      let inputValidated: boolean = this.validateEnableAppreciationObj(this.appreciationObj, template)
      if (!inputValidated) return;
      

      if (this.isEmployeeSelectionChanged) {
      this.enableAppreciationList = this.allEmployeeList.map(employee => {
        return {
          empId: employee.empId,
          isAppreciationEnable: true
        }
      });
    }
  
      
      this.appreciationObj.fromDate = moment(this.appreciationObj.fromDate).format(dateFormat)
      this.appreciationObj.toDate = moment(this.appreciationObj.toDate).format(dateFormat)
      this.appreciationObj.appreciationEventName = this.appreciationObj.appreciationEventName;
      this.appreciationObj.enableAppreciationList = this.enableAppreciationList; //console.log("updateAppreciation : ", this.appreciationObj)
  
  
      this.appreciationObj.updatedBy = this.currentUser.empId;;
     
      //console.log("Update dept : ", this.appreciationObj);
      this.allAppreciationEvent = this.allAppreciationEvent.filter(x => x.appreciationEventId != this.appreciationObj.appreciationEventId);
      let checkEventDate = this.allAppreciationEvent.find(x => x.fromDate == this.appreciationObj.fromDate || x.toDate == this.appreciationObj.toDate || ((x.fromDate <= this.appreciationObj.toDate) && (this.appreciationObj.fromDate <= x.toDate)));
      if (checkEventDate != undefined) {
        this.openAlertMod(template, "Event is already exist on this date");
      } else {
       
        this.portalService.updateAppreciationEvent(this.appreciationObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.reset();
            this.viewAppreciationEventOnClick()
          } else {
            this.openAlertMod(template, response.serviceResponse);
            this.reset();
          }
        });
      }
    }
    showUpdateForm(appreciationEvent: enableAppreciation) {
      this.portalConfig = false;
      this.appreciationConfig = true;
      this.viewAppreciationForm = false;
      this.isAppreciationTable = false;
      this.isTable = false;
      this.viewEventConfig = false;
      this.isCreation = false;
      this.isUpdation = true;
      this.appreciationObj = JSON.parse(JSON.stringify(appreciationEvent));
      this.appreciationObj.appreciationEventType = this.appreciationObj.appreciationEventType
      // this.appreciationObj.fromDate = new Date(moment(this.appreciationObj.fromDate).format('DD-MM-YYYY'));
      // this.appreciationObj.toDate = new Date(moment(this.appreciationObj.toDate).format('DD-MM-YYYY'));
      //console.log("this.appreciationObj : ", this.appreciationObj)
    }
    openDeleteAppreciationEvent(template: TemplateRef<any>, appreciationEvent: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.appreciationObj = appreciationEvent;
      //console.log(this.appreciationObj);
    }
    onDeleteAppreciationEvent(template: TemplateRef<any>) {
      this.cancelRequest();
      // this.appreciationObj.createdOn = (this.appreciationObj.createdOn)? moment(this.appreciationObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
      this.portalService.deleteAppreciationEvent(this.appreciationObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getAllEvent();
        } else {
          this.openAlertMod(template, response.serviceResponse);
          this.getAllEvent();
        }
      });
    }
  
  
    getAllDepartmentList() {
      this.allDeptList = [];
  
      this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allDeptList = response.serviceResponse;
        } else {
          console.error(response.serviceResponse)
        }
      });
    }
  
    getEmployeeList(employee?: Employee) {
      this.employeeList = [];
      let _employeeList = [];
  
      //console.log("Skip employee : ", employee)
  
      this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          _employeeList = response.serviceResponse;
  
          this.employeeList = _employeeList.filter(x => x.employmentstatus != 'InActive');
          //console.log("employeeList : ", this.employeeList)
        } else {
          console.error(response.serviceResponse)
        }
      });
    }
  
    // Help Config :: start
  
    onFileSelect(event: any,template:TemplateRef<any>) {
      this.files = [];
      const allowedTypes = ['application/pdf'];
      const maxSizeInBytes = 20 * 1024 * 1024; // 20MB
      let totalSize: number = 0;
      let isSizeInRange:boolean = false;
      this.fileSize = 0;
      const uploadedFiles = event.target.files;
  //console.log("maxFileSize  ::  ",maxSizeInBytes)
      if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
        this.openAlertMod(template,'Please select a valid file (pdf).');
        event.target.value = ''; // Clear the input
        return;
      }
      if(event.target.files[0].size > maxSizeInBytes){
        this.openAlertMod(template, "File size is more than 20MB");
        event.target.value = null;
        isSizeInRange = false;
     }else{
      isSizeInRange = true;
     }
  
     if(isSizeInRange){
      this.files = [];
     
      //console.log("maxfilesize: " + this.maxFileSize);
      if (uploadedFiles.length != 0) {
        for (let i = 0; i < uploadedFiles.length; i++) {
          let document = uploadedFiles[i];
          let fileName = document.name;
          this.fileSize = this.fileSize + uploadedFiles[i].size / 1024 / 1024;
          //console.log(this.fileSize);
          let fileObj1 = { document: document, fileName: fileName }
          this.files.push(fileObj1);
          //console.log("Files : ", this.files);
        }
      };
    }
    }
  
    onUploadFiles(template: TemplateRef<any>){
  
      this.helpDocumentName = this.helpDocumentName?.trim();
      if(!this.validationService.validateNullUndefinedEmptyString(this.helpDocumentName)){
        this.alertMessage = "Please enter Help Document Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.validationService.validateAlphaNumericWithSpace(this.helpDocumentName)){
        this.alertMessage = "Please enter Valid Help Document Name, Alphabets, Numericals & space allowed !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if (this.files.length == 0) {
        this.alertMessage = "Kindly Select Document !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      let totalSize = parseFloat(this.fileSize.toFixed(2));
      if(totalSize>this.maxFileSize && totalSize>this.maxRequestSize){
        this.alertMessage ="File exceeds the size limit";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      const formData = new FormData();
      this.files.forEach((file) =>{
        formData.append(`file`, file.document , file.fileName);
      });
      formData.append("helpDocumentName", this.helpDocumentName);
      formData.append("uploadedBy", this.currentUser.empId);
  
      //console.log("Upload files : ", formData);
      this.helpService.uploadHelpDocument(formData).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse);
          this.reset();
          this.viewHelpDocument();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
  
    }
  
    getAllHelpDocument(){
      this.data='';
      this.document = [];
      this.helpService.getAllHelpDocument().pipe(first()).subscribe((response:any) => {
        if (response.serviceStatus == "Success") {
          this.document =  response.serviceResponse;
          this.document.forEach(doc => {
            doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          //console.log("DocumentList : ", this.document);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  
    onDeleteDocument(template: TemplateRef<any>) {
      this.cancelRequest();
  
      //console.log(this.helpObj, " : this.helpObj");
  
  
      this.helpService.deleteHelpDocument(this.helpObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.viewHelpDocument();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
    downloadFile(doc: any) {
      this.helpService.downloadHelpDocument(doc.helpDocId).subscribe(blob => saveAs(blob,doc.fileName));
    }
  
    previewHelpDocument(template: TemplateRef<any>,doc: any) {
      this.src = null;
      this.fileName = doc.helpDocumentName;
  
      this.helpService.downloadHelpDocument(doc.helpDocId).pipe(first()).subscribe((response:any) => {
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
  
        this.src =  a.href;
  
        if(this.src != null){
          this.openPreviewDocument(template);
        }
      });
    }
  
    copyHelpDocumentLink(doc:any,template: TemplateRef<any>) {
      let url = window.location.href.split("#")[0].concat("#/helpdesk/").concat(doc.helpDocId);
      //console.log(url, " : url");
  
      this.clipboardService.copy(url);
      this.openAlertMod(template, "Link copied to clipboard !!");
    }
  
    //Help Config :: end
  
    //modal
  
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    openDeleteDocument(template: TemplateRef<any>, helpDoc: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.helpObj = helpDoc;
    }
  
    openPreviewDocument(template: TemplateRef<any>){
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
  
    cancelRequest() {
      this.modalRef.hide();
    }
  
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
      if(!this.isSearchEnabled){
        this.filters = {};
      }
    }
  
    onSearch(searchData){
      this.filters = searchData;
      //console.log("Updated Filter : ", this.filters);
    }
  }
  
  function compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  
  }
  
function saveAs(blob: Blob, fileName: any): void {
  throw new Error('Function not implemented.');
}

