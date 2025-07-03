
import {
  Component, OnInit, Input, Output,
  EventEmitter, OnChanges, SimpleChanges
} from '@angular/core';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { first } from 'rxjs/operators';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as moment from 'moment';
import { TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HolidayService } from 'src/app/services/holiday.service';

import { DatePipe, LocationStrategy } from '@angular/common';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';

import { Activity } from 'src/app/models/activity';


import { Sort } from '@angular/material/sort';

import { ClipboardService } from 'ngx-clipboard';

import { AppComponent } from 'src/app/app.component';


import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { Timesheet } from 'src/app/models/timesheet';



import { ExportExcelService } from 'src/app/services/export-excel.service';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { ValidationService } from 'src/app/services/validation.service';










@Component({
  selector: 'app-timesheet-create-self',
  templateUrl: './timesheet-create-self.component.html',
  styleUrls: ['./timesheet-create-self.component.css']
})
export class TimesheetCreateSelfComponent implements OnInit {

  @Input() clientList: any[] = [];
  selectedTeamMemberName: string = '';

  @Input() showNoTimesheetPopupTrigger: boolean = false;

   @Input() showNoTimesheetPopupTriggerForInActiveEmployee: boolean = false;


  leaveHistoryList: any[] = [];

  __tempDescription: string = '';

  maxOutTimeDate: Date;

  isUpdation: boolean = false;

  isTimesheetForm: boolean = false;
  serverDate: string = moment().format('YYYY-MM-DD');
  isTimesheetLockCheckEnable: any;

  @Output() createTimesheet = new EventEmitter<any>();
  @Output() teamMemberSelected = new EventEmitter<string>();

  modalRef: BsModalRef | null = null;
  alertMessage: string = '';
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;

 @ViewChild('alert_messageNotimesheet') alertNoTimesheetTemplate: TemplateRef<any>;

  @ViewChild('alert_messageForInActiveEmployee') alertNoTimesheetTemplateForInActive: TemplateRef<any>;

 





  @Input() autofillData: any;

  @Output() timesheetSubmitted = new EventEmitter<void>();




  ngOnChanges(changes: SimpleChanges): void {
    if (changes['autofillData'] && changes['autofillData'].currentValue) {
      console.log("auto fill data", changes['autofillData'].currentValue);
      this.patchFormFromLastData();
    }
    if (changes['showNoTimesheetPopupTrigger'] && changes['showNoTimesheetPopupTrigger'].currentValue) {
      setTimeout(() => {
        this.showNoTimesheetPopup(this.alertNoTimesheetTemplate);
      }, 2000);
    }
     if (changes['showNoTimesheetPopupTriggerForInActiveEmployee'] && changes['showNoTimesheetPopupTriggerForInActiveEmployee'].currentValue) {
      setTimeout(() => {
        this.showNoTimesheetPopup(this.alertNoTimesheetTemplateForInActive);
      }, 2000);
    }
  }







  patchFormFromLastData(): void {
    
    const data = this.autofillData;
    console.log("lalalala data",data);

    const activity = {
      clientId: data.clientId || '',
       activity: data?.activity ?? '',
      clientLocationId: data.clientLocationId || '',
      clientLocationList: data.clientLocation ? [{ clientLocationId: data.clientLocationId, clientLocation: data.clientLocation }] : [],
      teamId: data.teamId || '',
      projectList: data.teamName ? [{ teamId: data.teamId, displayTeam: data.teamName }] : [],
      activityId: data.activityId || '',
      projectActivities: data.activity ? [{ activityId: data.activityId, activity: data.activity }] : [],
      description: data.description || '',
      completionTime: data.totalTime || 0
    };

    this.allTimesheetActivities = [activity];
    this.clientList = data.clientName ? [{ clientId: data.clientId, clientName: data.clientName }] : [];

    this.timesheetObj = {
      checkId: data?.checkId ?? null,
      timesheetId: data?.timesheetId ?? null,
      allTimesheetActivities: this.allTimesheetActivities,
      updatedTimesheetActivities: data?.updatedTimesheetActivities ?? null,
      currentUser: this.currentUser?.empId,

      createdOn: data?.createdOn ?? null,
      createdBy: data?.createdBy ?? null,
      createdByName: data?.createdByName ?? null,
      updatedOn: data?.updatedOn ?? null,
      updatedBy: data?.updatedBy ?? null,
      currentManagerId: data?.currentManagerId ?? null,

      projectId: data?.projectId ?? null,
      projectName: data?.projectName ?? '',
      projectManagerId: data?.projectManagerId ?? null,
      managerId: data?.managerId ?? null,
      empId: data?.empId || this.timesheetObj.empId || this.currentUser?.empId,

      project: data?.project ?? null,

      startDate: data?.startDate ?? null,
      endDate: data?.endDate ?? null,
      status: data?.status ?? null,

      applicationCount: data?.applicationCount ?? null,
      employeementId: data?.employeementId ?? null,
      weekDayName: data?.weekDayName ?? null,
      totalWorkingHours: data?.totalWorkingHours ?? null,
      totalWorkingHoursPercentage: data?.totalWorkingHoursPercentage ?? null,

      isConsultant: data?.isConsultant ?? null,
      totalTime: data?.totalTime ?? 0,

      timesheetStatusUpdatedBy: data?.timesheetStatusUpdatedBy ?? null,
      timesheetStatusUpdatedByName: data?.timesheetStatusUpdatedByName ?? null,
      employeeName: data?.employeeName ?? null,
      rejectReason: data?.rejectReason ?? null,
      email: data?.email ?? null,

      clientId: data?.clientId ?? null,
      clientName: data?.clientName ?? '',
      clientLocationId: data?.clientLocationId ?? null,
      clientLocation: data?.clientLocation ?? '',

      timesheetAppliedFor: this.timesheetObj.timesheetAppliedFor,
      dayType: data?.dayType || '',
      date: data?.date || '',
      officeInTime: data?.officeInTime ? new Date(data.officeInTime.replace(' ', 'T')) : null,
      
      officeOutTime: data?.officeOutTime ? new Date(data.officeOutTime.replace(' ', 'T')) : null,
      totalWorkingOfficeHours: data?.totalWorkingOfficeHours || '00:00',
      isNightShift: data?.isNightShift || false,

      remarks: data?.remarks || '',
      description: data?.description || '',

      teamId: data?.teamId ?? null,
      teamName: data?.teamName ?? '',
      activity: data?.activity ?? '',
      activityId: data?.activityId ?? null,

      managerEmail: data?.managerEmail ?? null,
      managerName: data?.managerName ?? null,

      leaveType: data?.leaveType ?? null,
      isCron: data?.isCron ?? null,
      year: data?.year ?? null,
      month: data?.month ?? null,

      displayTeam: data?.displayTeam ?? '',
      inactiveTimesheetActivities: data?.inactiveTimesheetActivities ?? null,
      name: data?.name ?? '',
      inTime: data?.inTime ?? null,
      outTime: data?.outTime ?? null,
      appliedOn: data?.appliedOn ?? null,
      selected: data?.selected ?? null,
      timeSheet: data?.timeSheet ?? null,
      nightShift: data?.nightShift ?? null,

      isSelected: false,
      bulkApprovedList: [],
      bulkRejectList: [],
      queryList: []
    };

    console.log("clientLocationId" + activity.clientLocationId);
    console.log('activity' + JSON.stringify(activity));
    console.log('timesheetObj-appliedFor' + this.timesheetObj.timesheetAppliedFor);

    console.log('teamId' + this.timesheetObj.teamId);
    console.log('Activity_team_id' + activity.teamId);
  }
  compareClientLocation = (a: any, b: any) => a?.clientLocationId === b?.clientLocationId;
  compareTeam = (a: any, b: any) => a?.teamId === b?.teamId;
  compareActivity = (a: any, b: any) => a?.activityId === b?.activityId;

  compareById = (a: any, b: any): boolean => {
    return a === b || (a && b && a == b);
  };


  onTimesheetTypeChange(): void {

    console.log("Timesheet Type Changed: ", this.timesheetObj.timesheetAppliedFor);
    if (this.timesheetObj.timesheetAppliedFor === 'team') {
      this.getAllTeamMemberList();

    } else {
      this.timesheetObj.empId = this.currentUser.empId;
      this.teamMemberSelected.emit(this.timesheetObj.empId);
    }
  }

  onTeamMemberChange(empId: number): void {
    this.timesheetObj.empId = (empId);
    this.teamMemberSelected.emit(this.timesheetObj.empId);
    console.log("Selected Team Member empId: ", this.timesheetObj.empId);
    this.getTimesheetMetadata(empId);
    // const userObj: User = new User();
    // userObj.empId = empId;
    // this.getAllAvailableTimesheetByEmpId(userObj);
    const selectedMember = this.teamMemberList.find(m => m.empId == empId);
    this.selectedTeamMemberName = selectedMember ? selectedMember.name : '';
  }








  data: string;
  feature = "My Timesheets";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  //flags
  isCreation: boolean = false;



  isTimesheetTable: boolean = false;

  isTimesheetUpdate: boolean = false;
  isTimesheetUpdateCounter = 0;

  isSelfTimesheets: boolean = false;
  isTeamTimesheets: boolean = false;

  Allholidays: any[] = [];
  AllWeekOfList: any[] = [];

  //modal


  //Obj
  timesheetObj: Timesheet = new Timesheet();
  allTimesheetActivities: any[] = []
  allProjectsList: any[] = [];
  allActivityList: any[] = [];

  allMyTimesheets: any[] = [];
  timesheetActivities: any[] = [];
  startDate: any;
  endDate: any;

  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  availableTimesheetDates: any[] = [];
  availableTimesheets: any[] = [];

  projectList: any[] = [];

  clientLocationList: any[] = [];
  // teamList:any[] = [];

  teamMemberList: any[] = [];
  errorMsg: any;

  disableCreateUpdateTimesheet: boolean = false;







  selectedTimesheet: any;
  today = new Date().toISOString().split('T')[0];

  filters: any = {};
  isSearchEnabled: boolean = false;
  selfTimesheetColumns: any[] = ['blank', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'description', 'totalTime', 'status', 'createdByName', 'createdOn', 'isNightShiftDisplay', 'leaveType', 'remarks'];
  teamTimesheetColumns: any[] = ['blank', 'employeeName', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'description', 'totalTime', 'status', 'createdOn', 'isNightShiftDisplay', 'leaveType', 'remarks'];
  tableName: string;


  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private datePipe: DatePipe,
    private clipboardService: ClipboardService,
    private teamViewService: TeamViewService,
    private leaveService: LeaveService,
    private locationStrategy: LocationStrategy,
    private employeeService: EmployeeService,
    private holidayService: HolidayService,
    private router: Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }


  showNoTimesheetPopup(template: TemplateRef<any>): void {
    this.modalRef = this.modalService.show(template, {
      class: 'modal-dialog-centered modal-sm',
      backdrop: 'static',
      keyboard: false,
    });
  }


  ngOnInit(): void {

    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);




    this.timesheetService.getServerDate().pipe(first()).subscribe((response: any) => {
      this.serverDate = response;
    });

    this.timesheetObj.timesheetAppliedFor = "self";
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.getAllHolidays();
    this.getAllHolidaysbystate();
    this.getAllMyLeaveApplicationsByEmpId(this.currentUser);
    this.sectionViewInit();
    this.preventBackButton();

    this.patchFormFromLastData();


    // this.setStartDateMinMax();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }
  //method


  sectionViewInit() {
    if (this.userMapping.add_timesheet) {
      this.showCreateTimesheetForm();
    } else if (this.userMapping.view_my_timesheets || this.userMapping.update_timesheet) {
      this.showViewMyTimesheets()
    }
  }

  disableMannualDateInput() {
    return false;
  }

  showCreateTimesheetForm() {
    this.isTimesheetForm = true;
    this.isCreation = true;

    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.reset();
    this.getEmployeeBasicInfo();
    this.getAllProjectsByEmpId(this.currentUser);
  }

  showViewMyTimesheets() {
    this.isTimesheetTable = true;

    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;

    this.showSelfTimesheets();
  }

  showSelfTimesheets() {
    this.isSelfTimesheets = true;
    this.isTeamTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data = '';

    this.filters = {};
    this.isSearchEnabled = false;
    this.setStartDateMinMax();
  }

  showTeamTimesheets() {
    this.isTeamTimesheets = true;
    this.isSelfTimesheets = false;

    this.page = 1;

    this.startDate = null;
    this.endDate = null;

    this.allMyTimesheets = [];
    this.data = '';

    this.filters = {};
    this.isSearchEnabled = false;

    this.teamMemberList = [];
    this.setStartDateMinMax();

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamMemberList = response.serviceResponse;
        //console.log("teamMemberList : ", this.teamMemberList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  openInActiveUpdateConfimationModal(template: TemplateRef<any>, timesheetObj: Timesheet,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.selectedTimesheet = null;
    this.selectedTimesheet = Object.assign({}, timesheetObj);
  }


  checkTimesheetForInActiveActivities(timesheetObj: Timesheet, template: TemplateRef<any>) {


    if (timesheetObj.dayType == "Working" && (timesheetObj.status == "Pending" || timesheetObj.status == "Rejected") && timesheetObj?.inactiveTimesheetActivities) {
      this.openInActiveUpdateConfimationModal(template, timesheetObj);
    } else {
      this.showUpdateTimesheetForm(timesheetObj);
    }
  }

  updateInactiveActivitiesTimesheet() {
    this.showUpdateTimesheetForm(this.selectedTimesheet);
  }

  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;

    this.timesheetObj = Object.assign({}, timesheetObj);
    this.timesheetObj.updatedTimesheetActivities = [];
    this.timesheetObj.date = (this.timesheetObj.date) ? moment(timesheetObj.date, "DD-MM-YYYY").toDate() : '';
    this.timesheetObj.officeInTime = (this.timesheetObj.officeInTime) ? moment(timesheetObj.officeInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.officeOutTime = (this.timesheetObj.officeOutTime) ? moment(timesheetObj.officeOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.createdOn = (this.timesheetObj.createdOn) ? moment(timesheetObj.createdOn, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.dayType = (this.timesheetObj.dayType == "Holiday") ? "Week Off" : this.timesheetObj.dayType;

    if (this.timesheetObj.officeInTime) {
      this.maxOutTimeDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());
    }

    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave") {
      this.__tempDescription = this.timesheetObj.description;
    }

    let userObj: User = new User();
    if (this.isSelfTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "self";
      this.timesheetObj.empId = this.currentUser.empId;

      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
    } else if (this.isTeamTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "team";

      let teamMember = this.teamMemberList.find(employee => employee.empId == timesheetObj.empId)
      //console.log("Team Member : ", teamMember);
      userObj.empId = teamMember.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
    }

    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
    setTimeout(() => {
      this.getAllMyActivitiesByTimesheetId(timesheetObj);
    }, 500)
  }

  reset() {
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.timesheetObj.timesheetAppliedFor = "self";
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.allTimesheetActivities = [];
    this.addInputActivityField();
  }



  addInputActivityField(activityObj?: Activity) {

    //console.log("before allTimesheetActivities : ", this.allTimesheetActivities)
    let newActivityObj = new Activity();

    if (activityObj != undefined) {
      newActivityObj.clientId = activityObj.clientId;
      newActivityObj.clientLocationId = activityObj.clientLocationId;
      newActivityObj.projectId = activityObj.projectId;

      let teamId = activityObj.teamId;
      console.log("teamId" + teamId);
      newActivityObj.teamId = teamId;
      console.log("newActivityObj : ", newActivityObj);


      this.allTimesheetActivities.push(newActivityObj);
      this.getClientLocationList(newActivityObj);
      let result = this.getProjectList(newActivityObj);
      console.log("result : ", result);
      // this.getTeamList(newActivityObj)
      this.getAllActivitiesByProjectIdandEmpId(newActivityObj);
    } else {
      this.allTimesheetActivities.push(newActivityObj);
    }
    //console.log("After allTimesheetActivities : ", this.allTimesheetActivities)
  }

  // Manage Activity

  // --> pREV
  // addInputActivityField() {
  //   let newActivityObj = new Activity();
  //   // newActivityObj.projectId = '';
  //   // newActivityObj.activityId = '';
  //   this.allTimesheetActivities.push(newActivityObj);
  // }

  removeInputActivityField(activityObj: any) {
    this.allTimesheetActivities.forEach((value, index) => {
      if (value == activityObj) {
        if (this.isUpdation) {
          this.timesheetObj.updatedTimesheetActivities.push(value);
        }
        this.allTimesheetActivities.splice(index, 1);
      }
    });
  }

  setAllProjectActivities(activityObj, allActivityList: any) {
    const selectedActivityObj = this.allTimesheetActivities.find(activity => activity === activityObj);
    if (!this.isTimesheetUpdate) {
      selectedActivityObj.activityId = '';
    } else {
      this.isTimesheetUpdateCounter--;
      if (this.isTimesheetUpdateCounter === 0) this.isTimesheetUpdate = false;
    }
    selectedActivityObj.projectActivities = allActivityList;
    // console.log('setAllProjectActivities',selectedActivityObj.projectActivities)
  }

  setActivity(activityObj) {
    this.allTimesheetActivities.find(activity => activity === activityObj).activity = activityObj.projectActivities.find(activity => activity.activityId == activityObj.activityId).activity;
    activityObj.description = null;
    console.log('setActivity', activityObj.projectActivities)
    //console.log("Activity obj : ", activityObj)
    // //console.log("Activity : ",activity)
  }




  holidayList: any[] = [];
  holidayListFilter: any[] = [];
  WeekOfListFilter: any[] = [];

  // _holidayList:any[]=[];
  // selectedHolidayType:any;
  // selectedYear: any;
  // selectedState:any;




  holidaystateObj: Holiday = new Holiday();
  holidaystateList: any[] = [];


  getAllHolidaysbystate() {

    this.holidayList = [];



    this.holidaystateObj.state = this.currentUser.workLocation
    this.holidayService.getAllHolidays(this.holidaystateObj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidaystateList = response.serviceResponse;

        console.log("holidays  state wize", this.holidaystateList);

        this.filterHolidaystateListByYear(new Date().getFullYear());
      } else {
        console.error(response.serviceResponse);
      }
    });
  }




  filterHolidaystateListByYear(year: number): void {
    this.holidayListFilter = this.holidaystateList.filter((holiday) => {
      const holidayYear = new Date(holiday.dateOfHoliday).getFullYear();
      return (
        holidayYear === year &&
        holiday.holidayType !== 'WeekOff' &&
        holiday.holidayType !== 'nonWorking' &&
        (holiday.state.toLowerCase() === this.currentUser.workLocation.toLowerCase() || holiday.state.toLowerCase() === 'all')
      );
    });



    this.Allholidays = this.holidayListFilter.map((holiday) =>
      holiday.dateOfHoliday
    );


  }





  getAllHolidays() {

    this.holidayList = [];



    this.holidayService.getAllHoliday().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;

        console.log("holidaylist   ", this.holidayList);
        this.filterHolidayListByYear(new Date().getFullYear());
      } else {
        console.error(response.serviceResponse);
      }
    });
  }





  filterHolidayListByYear(year: number): void {

    this.WeekOfListFilter = this.holidayList.filter((holiday) => {
      const holidayYear = new Date(holiday.dateOfHoliday).getFullYear();
      return (
        holidayYear === year &&
        holiday.holidayType == 'WeekOff'
      );
    });


    this.AllWeekOfList = this.WeekOfListFilter.map((holiday) =>
      holiday.dateOfHoliday
    );
  }






  // filterHolidayListByYear(value: any) {
  //   // Reset selections
  //   this.selectedHolidayType = "";
  //   this.selectedYear = value;

  //   console.log("Selected year:", this.selectedYear);

  //   this.holidayListFilter = this.holidayList.filter((holiday: Holiday) => {
  //     const holidayYear = moment(holiday.dateOfHoliday, "DD-MM-YYYY").year();
  //     return (
  //       holiday.holidayType !== "WeekOff" &&
  //       holiday.holidayType !== "nonWorking" &&
  //       holidayYear === this.selectedYear
  //     );
  //   });


  //   this.Allholidays = this.holidayListFilter.map((holiday: Holiday) =>
  //     holiday.dateOfHoliday
  //       ? moment(holiday.dateOfHoliday, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT)
  //       : ''
  //   ).filter(date => date !== '');

  //   console.log("Selected holidayListFilter:", this.Allholidays);
  //   console.log("Selected Allholidays:", this.Allholidays);
  // }











  //Manage the weekoff and holidays 
  customDateFilter: (date: Date) => boolean = (date: Date): boolean => {

    const dayType = this.timesheetObj.dayType;
    const filteredDates = this.getFilteredDates(dayType);
    return filteredDates.some(filteredDate =>
      this.datePipe.transform(filteredDate, 'yyyy-MM-dd') === this.datePipe.transform(date, 'yyyy-MM-dd')
    );
  };




  // getFilteredDates(dayType: string): Date[] {


  // console.log("date filter ",this.Allholidays);

  //   const DAY_IN_MS = 24 * 60 * 60 * 1000;
  //   const currentDate = new Date();
  //   const backDatedDays = this.currentUser.timesheetBackDatedDays || 30;
  //   const startDate = new Date(currentDate.getTime() - backDatedDays * DAY_IN_MS);

  //   // const publicHolidays = ['2024-11-25', '2024-11-01']; // Add public holiday dates here.

  //   if (dayType === 'Non-working') {
  //     // Filter for Public Holidays

  //     console.log("hodays ",this.Allholidays)

  //     return this.Allholidays
  //       .map(date => new Date(date))
  //       .filter(holidayDate => holidayDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date === this.datePipe.transform(holidayDate, 'yyyy-MM-dd')));
  //   }

  //   if (dayType === 'Non-Working') {
  //     return this.AllWeekOfList
  //     .map(date => new Date(date))
  //     .filter(
  //       weekOffDate =>
  //         weekOffDate >= startDate &&
  //         weekOffDate <= currentDate &&
  //         !this.availableTimesheets.find(
  //           timesheet => timesheet.date === this.datePipe.transform(weekOffDate, 'yyyy-MM-dd')
  //         )
  //     );
  //   }

  //   return [];
  // }

  getFilteredDates(dayType: string): Date[] {


    console.log("date filter ", this.Allholidays);

    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    const currentDate = new Date();
    const backDatedDays = this.currentUser.timesheetBackDatedDays || 30;
    const startDate = new Date(currentDate.getTime() - backDatedDays * DAY_IN_MS);

    // const publicHolidays = ['2024-11-25', '2024-11-01']; // Add public holiday dates here.

    // if (dayType === 'Public Holiday') {
    //   // Filter for Public Holidays

    //   console.log("hodays ",this.Allholidays)

    //   return this.Allholidays
    //     .map(date => new Date(date))
    //     .filter(holidayDate => holidayDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date === this.datePipe.transform(holidayDate, 'yyyy-MM-dd')));
    // }

    if (dayType === 'Non-working') {
      console.log("holidays and week offs", this.AllWeekOfList, this.Allholidays);

      // Filter for holidays
      const holidayDates = this.Allholidays
        .map(date => new Date(date))
        .filter(holidayDate => holidayDate >= startDate &&
          holidayDate <= currentDate &&
          this.availableTimesheets.find(timesheet => timesheet.date === this.datePipe.transform(holidayDate, 'yyyy-MM-dd'))
        );

      // Filter for week-off dates
      const weekOffDates = this.AllWeekOfList
        .map(date => new Date(date))
        .filter(
          weekOffDate =>
            weekOffDate >= startDate &&
            weekOffDate <= currentDate &&
            this.availableTimesheets.find(
              timesheet => timesheet.date === this.datePipe.transform(weekOffDate, 'yyyy-MM-dd')
            )
        );

      // Combine the holidays and week off dates
      return [...holidayDates, ...weekOffDates];
    }


    return [];
  }
















  // Manage Timesheet Dates
  timesheetDateFilter = (checkDate: Date) => {
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    const time = checkDate?.getTime();


    let currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let OPEN_BACKDATED_DAYS = 30;
    const CURRENT_DAY = 1;

    let dateOfJoining = moment(this.currentUser.dateOfJoining, dateFormat);

    let daysDifference = moment(currentDate, dateFormat).diff(dateOfJoining, 'days');


    if (this.currentUser.timesheetBackDatedDays > daysDifference) {

      OPEN_BACKDATED_DAYS = daysDifference;

    } else {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
    }


    const dateObj = new Date(this.serverDate + 'T23:59:59');
    let serverDate = dateObj;

    //console.log(serverDate, " : serverDate");


    // timesheetLockDays (days) + 1 current Day
    let endDate = serverDate;
    let startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + CURRENT_DAY) * DAY_IN_MS));

    if (this.isTimesheetForm && this.isUpdation) {
      this.availableTimesheets = this.availableTimesheets.filter(timesheet => this.datePipe.transform(timesheet.date, "yyyy-MM-dd") != this.datePipe.transform(this.timesheetObj.date, "yyyy-MM-dd"));
    }

    //console.log("isTimesheetLockCheckEnable : ", this.isTimesheetLockCheckEnable);


    if (this.isTimesheetLockCheckEnable == "false") {
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + CURRENT_DAY) * DAY_IN_MS));
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "yyyy-MM-dd"))) ? true : false;
    } else {
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "yyyy-MM-dd"))) ? true : false;
    }
  }

  outTimeFilter = (checkDate: Date) => {
    const dateFormat = 'YYYY-MM-DD';

    let startDate = this.timesheetObj.officeInTime;
    let endDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());

    return (moment(checkDate).format(dateFormat) <= moment(endDate).format(dateFormat) && moment(checkDate).format(dateFormat) >= moment(startDate).format(dateFormat)) ? true : false;
  }









  setMaxInTimeDate(timesheetDate: any) {
    //console.log("timesheetDate : ", moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL));

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      let inTimeDate = document.getElementById('officeInTime');
      // //console.log("InTimeDate: ", inTimeDate);
      let officeOutTime = document.getElementById('officeOutTime');
      inTimeDate.setAttribute('min', `${moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL)}`);
      officeOutTime.setAttribute('min', `${moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL)}`);
    }
  }

  // added by anurag for viewMyTimesheet()
  setStartDateMinMax(): void {

    let startDateInput = document.getElementById('timesheetStartDate');
    let startEndDate = document.getElementById('timesheetEndDate');
    startDateInput.setAttribute('max', this.today);
    startEndDate.setAttribute('max', this.today);
    //console.log("set date :: ",startDateInput);

  }
  resetTotalWorkingOfficeHours(template: TemplateRef<any>) {

    if (this.timesheetObj.officeInTime) {

      const systemCurrentTime = moment();
      const userOfficeInTime = moment(this.timesheetObj.officeInTime);

      console.log("data==", userOfficeInTime);

      if (systemCurrentTime.isSame(userOfficeInTime, 'minute')) {
        const updatedInTime = userOfficeInTime.subtract(2, 'minutes').toDate();
        this.timesheetObj.officeInTime = updatedInTime;
      }
      else if (userOfficeInTime.isAfter(systemCurrentTime, 'minute') && userOfficeInTime.isSame(systemCurrentTime, 'day')) {
        const updatedInTime = userOfficeInTime.subtract(2, 'minutes').toDate();
        this.timesheetObj.officeInTime = updatedInTime;
      }



    }

    this.timesheetObj.officeOutTime = '';
    this.timesheetObj.totalWorkingOfficeHours = '';

    this.maxOutTimeDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());
  };


  resetTimeonDayTypeChange() {
    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave") {
      this.timesheetObj.officeInTime = '';
      this.timesheetObj.officeOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
  }

  setTotalWorkingOfficeHours() {
    const dateFormat = 'YYYY-MM-DD';
    const systemCurrentTime = moment();
    const userOfficeOutTime = moment(this.timesheetObj.officeOutTime);

    console.log('systemCurrentTime', systemCurrentTime);
    console.log('userOfficeOutTime', userOfficeOutTime);

    if (
      (userOfficeOutTime.isAfter(systemCurrentTime, 'minute') || userOfficeOutTime.isSame(systemCurrentTime, 'minute')) &&
      userOfficeOutTime.isSame(systemCurrentTime, 'day')
    ) {
      const updatedInTime = systemCurrentTime.subtract(1, 'minute').toDate();
      this.timesheetObj.officeOutTime = updatedInTime;

      console.log('Updated Office In Time:', updatedInTime);
    }


    if (this.timesheetObj.officeInTime && this.timesheetObj.officeOutTime) {

      let start = moment(this.timesheetObj.officeInTime).format('DD-MM-YYYY HH:mm');
      let end = moment(this.timesheetObj.officeOutTime).format('DD-MM-YYYY HH:mm');
      let ms = moment(end, "DD-MM-YYYY HH:mm").diff(moment(start, "DD-MM-YYYY HH:mm"));
      let d = moment.duration(ms);


      let duration = Math.floor(d.asHours()) + moment.utc(ms).format(":mm");

      this.timesheetObj.totalWorkingOfficeHours = duration;
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);


    } else {
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
  }









  /* Timesheet */
  validateTimesheetObj(timesheetObj: Timesheet, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.date)) {
      this.alertMessage = "Please enter Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.dayType)) {
      this.alertMessage = "Please select Day Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (timesheetObj.dayType != "Public Holiday" && timesheetObj.dayType != "Week Off" && timesheetObj.dayType != "Leave") {
      let flag = true;
      let totalActivityTime = 0;
      let totalWorkingHoursInSeconds = 0;

      let hm = (timesheetObj.totalWorkingOfficeHours) ? timesheetObj.totalWorkingOfficeHours : '00:00';
      let timeData = hm.split(':');

      // minutes are worth 60 seconds. Hours are worth 60 minutes.
      totalWorkingHoursInSeconds = (+timeData[0]) * 60 * 60 + (+timeData[1]) * 60;

      //console.log("totalWorkingHoursInSeconds : ", totalWorkingHoursInSeconds);

      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.officeInTime)) {
        this.alertMessage = "Please enter In Date-Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.officeOutTime)) {
        this.alertMessage = "Please select Out Date-Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (totalWorkingHoursInSeconds <= 0) {
        this.alertMessage = "Please select Valid IN-OUT Date-Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      this.allTimesheetActivities.forEach((activity, index) => {
        if (!flag) return;

        if (activity.description) activity.description = activity.description?.trim();

        if (!this.validationService.validateNullUndefinedEmptyString(activity.clientId)) {
          this.alertMessage = `Please select Client - ${index + 1}!!`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.clientLocationId)) {
          this.alertMessage = `Please select Client Location - ${index + 1}!!`
          flag = false;
          return;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(activity.activityId)) {
          this.alertMessage = `Please select Activity - ${index + 1}!!`
          flag = false;
          return;
        }


        // if (!this.validationService.validateNullUndefinedEmptyString(activity.projectId)) {
        //   this.alertMessage = `Please select Project - ${index + 1}!!`
        //   flag = false;
        //   return;
        // }


        if (activity.description != null && activity.description != '') {

          if (!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
            this.alertMessage = `Please enter valid Activity Description  - ${index + 1}!!`
            flag = false;
            return;
          }
        }
        if (!this.validationService.validateCompletionTime(activity.completionTime) && !this.validationService.validateExperiencedNumber(activity.completionTime)) {
          this.alertMessage = `Please enter valid Activity Completion Time - ${index + 1}!!`
          flag = false;
          activity.completionTime = ''
          return;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(activity.completionTime)) {
          this.alertMessage = `Please enter Activity Completion Time - ${index + 1}!!`
          flag = false;
          return;
        }

        // validateCompletionTime
        // if (!this.validationService.validateTimesheetCompletionTime(activity.completionTime)) {
        //   this.alertMessage = `Please enter valid Activity Completion Time - ${index + 1}!!`
        //   flag = false;
        //   return;
        // }
        totalActivityTime = totalActivityTime + activity.completionTime;

      });

      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (totalActivityTime <= 0 || totalActivityTime > 24) {
        this.alertMessage = 'Total time must be greater than 0 hrs and maximum upto 24 hrs!! '
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.description)) {
        this.alertMessage = "Please enter Timesheet Description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateActivityTimesheetDiscription(timesheetObj.description)) {
        this.alertMessage = `Please enter valid Description  !!`
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    return true;
  }

  onCreateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

    console.log("test ", this.timesheetObj.description);
    this.timesheetObj.description = this.timesheetObj.description?.trim();
    console.log("test ", this.timesheetObj.description);

    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template);
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      this.timesheetObj.allTimesheetActivities =
        (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    } else {
      this.timesheetObj.allTimesheetActivities = null;
    }

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      // this.timesheetObj.officeInTime = "wow";
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
    } else {
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
    }

    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.createdByName = this.currentUser.name;

    if (this.currentUser.approvalsTo == 'Reporting Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.reportingManagerId;
    } else if (this.currentUser.approvalsTo == 'Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    } else {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    }

    console.log("Add timesheetObj : ", this.timesheetObj);

    this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        setTimeout(() => {
          this.timesheetSubmitted.emit(); // should trigger immediately
        }, 2000);
        this.showViewMyTimesheets();


        this.startDate = this.endDate = this.timesheetObj.date;

        if (this.timesheetObj.timesheetAppliedFor == "self") {
          this.getAllMyTimesheetsByEmpId();


        } else {
          this.getMyTeamTimesheets();


        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';
    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
      this.timesheetObj.createdOn = moment(this.timesheetObj.createdOn).format(dateTimeFormat);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;

      if (this.timesheetObj.allTimesheetActivities) {
        let newTimesheetActivities = this.timesheetObj.allTimesheetActivities.filter(activity => !activity.timesheetId);
        //console.log("newTimesheetActivities : ", newTimesheetActivities);

        if (newTimesheetActivities) {
          if (this.timesheetObj.updatedTimesheetActivities === undefined || this.timesheetObj.updatedTimesheetActivities.length === 0) {
            this.timesheetObj.updatedTimesheetActivities = [];

          }
          this.timesheetObj.updatedTimesheetActivities.forEach(timesheet => {
            if (timesheet.activityId == "") {
              this.timesheetObj.updatedTimesheetActivities.splice(timesheet, 1);
            }
          });
          this.timesheetObj.updatedTimesheetActivities = this.timesheetObj.updatedTimesheetActivities.concat(newTimesheetActivities);
        }
      } else {
        if (this.timesheetObj.updatedTimesheetActivities == undefined || this.timesheetObj.updatedTimesheetActivities[0].length == 0) {
          this.timesheetObj.updatedTimesheetActivities = null;
        }
      }
    } else {
      this.timesheetObj.updatedTimesheetActivities = null;
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
      this.timesheetObj.createdOn = moment(this.timesheetObj.createdOn).format(dateTimeFormat);
      this.timesheetObj.officeInTime = '';
      this.timesheetObj.officeOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.currentManagerId = this.currentUser.managerId;
    //console.log("Update timesheetObj : ", this.timesheetObj);
    this.timesheetService.updateTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewMyTimesheets();
        if (this.timesheetObj.timesheetAppliedFor == "self") {
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getAllMyTimesheetsByEmpId();
        } else {
          this.showTeamTimesheets();
          this.startDate = this.endDate = this.timesheetObj.date;
          this.getMyTeamTimesheets();
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
      this.timesheetSubmitted.emit();
    });
  }


  onTimesheetDescriptionChange() {
    if (this.isUpdation) {
      if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave") {
        this.timesheetObj.description = (this.__tempDescription != null) ? this.__tempDescription : '';
        if (this.timesheetObj.description) {
          this.__tempDescription = this.timesheetObj.description;
        }
      } else if (this.timesheetObj.dayType == "Working" || this.timesheetObj.dayType == "Non-working") {
        this.__tempDescription = (this.timesheetObj.description != null) ? this.timesheetObj.description : '';
        this.timesheetObj.description = '';
      }
    }
  }

  // getTimesheetMetadata(eventTarget?: any) {
  //   //console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);
  //   let userObj: User = new User();
  //   if (this.timesheetObj.timesheetAppliedFor == 'self') {
  //     userObj.empId = this.currentUser.empId;
  //     userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
  //     this.timesheetObj.empId = this.currentUser.empId;
  //     this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
  //     //console.log("this.currentUser  : ", this.currentUser);
  //     //console.log("userObj  : ", userObj);

  //   } else {
  //     let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
  //     //console.log("Team Member : ", teamMember);
  //     userObj.empId = teamMember.empId;
  //     userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
  //     this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
  //     this.timesheetObj.empId = teamMember.empId;

  //     if (teamMember.isTimesheetFilledByMember == "true") {
  //       this.openAlertMod(this.alertTemplate, "Timesheet cannot be filled for team member more than 2 days.");
  //       this.timesheetObj.empId = '';
  //       eventTarget.value = "";
  //       this.disableCreateUpdateTimesheet = true;
  //       eventTarget.value = '';
  //       //console.log(eventTarget.value, " : eventTarget");



  //     } else {
  //       this.disableCreateUpdateTimesheet = false;
  //     }
  //   }

  //   const timesheetBkp = Object.assign({}, this.timesheetObj);

  //   // reset timesheet
  //   this.timesheetObj = new Timesheet();
  //   this.timesheetObj.dayType = '';
  //   this.allTimesheetActivities = [];
  //   this.addInputActivityField()

  //   // set leave AppliedFor User data to fetch activities for project & for display
  //   this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
  //   this.timesheetObj.empId = timesheetBkp.empId;

  //   //console.log("preset Timesheet : ", this.timesheetObj);

  //   this.getAllProjectsByEmpId(userObj);
  //   this.getAllAvailableTimesheetByEmpId(userObj);
  // }

  getTimesheetMetadata(empId?: number): void {
    let userObj: User = new User();

    if (this.timesheetObj.timesheetAppliedFor === 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = this.currentUser.empId;
      this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;

    } else {
      const teamMember = this.teamMemberList.find(employee => employee.empId === Number(empId));
      if (!teamMember) return;

      userObj.empId = teamMember.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = teamMember.empId;

      if (teamMember.isTimesheetFilledByMember === "true") {
        this.openAlertMod(this.alertTemplate, "Timesheet cannot be filled for team member more than 2 days.");
        this.timesheetObj.empId = '';
        this.disableCreateUpdateTimesheet = true;
        return;
      } else {
        this.disableCreateUpdateTimesheet = false;
      }
    }

    const timesheetBkp = { ...this.timesheetObj };

    // Reset the form
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.allTimesheetActivities = [];
    this.addInputActivityField();

    // Restore appliedFor
    this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
    this.timesheetObj.empId = userObj.empId;

    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
  }


  getAllTeamMemberList() {
    this.teamMemberList = []
    this.timesheetObj.date = ''
    this.timesheetObj.dayType = ''
    this.timesheetObj.officeInTime = ''
    this.timesheetObj.officeOutTime = ''
    this.timesheetObj.totalWorkingOfficeHours = ''
    this.allTimesheetActivities.forEach((timesheet) => {
      timesheet.clientId = ''
      timesheet.clientLocationId = ''
      timesheet.teamId = ''
      timesheet.activityId = ''
      timesheet.description = ''
      timesheet.completionTime = ''
    })

    if (this.timesheetObj.timesheetAppliedFor == "team") {
      this.errorMsg = '';
      let employeeObj = new Employee();
      employeeObj.empId = this.currentUser.empId;
      this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.teamMemberList = response.serviceResponse;
          //console.log("teamMemberList : ", this.teamMemberList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

  }

  getAllProjectsByEmpId(employeeObj: User) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];
    // this.teamList = [];
    // //console.log(" team list :    ", this.teamList)

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log("allProjectsList :", this.allProjectsList);
        if (this.allProjectsList.length == 0) {
        }
        else {
          const key = "clientId";
          this.clientList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].map((project: Timesheet) => {
            return { clientId: project.clientId, clientName: project.clientName }
          });
          //console.log("clientList :", this.clientList);
        }
      } else {
        console.error(response.serviceResponse)
        this.openAlertMod(this.alertTemplate, "Please contact the RMG team and set up your default project mapping!");

      }
    });
  }

  // getProjectList(activityObj: Activity) {
  //   this.projectList = [];

  //   const key = "teamId";
  //   this.projectList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
  //     if (project.clientId == activityObj.clientId) {
  //       project['displayTeam'] = `${project.projectName} | ${project.teamName}`;
  //       return { teamId: project.teamId, teamName: project.teamName, projectName: project.projectName, displayTeam : project.displayTeam}
  //     }
  //   });
  //   //console.log("projectList with displayTeam:", this.projectList);
  //   this.setAllProjects(activityObj, this.projectList);
  // }
  // getProjectList(activityObj: Activity) {
  //   if (!activityObj.clientId) return;

  //   // Filter allProjectsList for the selected clientId
  //   const filteredProjects = this.allProjectsList
  //     .filter((project: Timesheet) => project.clientId === activityObj.clientId)
  //     .map((project: Timesheet) => {
  //       return {
  //         teamId: project.teamId,
  //         teamName: project.teamName,
  //         projectName: project.projectName,
  //         displayTeam: `${project.projectName} | ${project.teamName}`
  //       };
  //     });

  //   // Remove duplicates by teamId
  //   const uniqueProjectsMap = new Map();
  //   for (const project of filteredProjects) {
  //     uniqueProjectsMap.set(project.teamId, project);
  //   }

  //   const uniqueProjects = Array.from(uniqueProjectsMap.values());

  //   // Set the filtered list on the activity object
  //   activityObj.projectList = uniqueProjects;

  //   // ✅ Preserve selected teamId if still valid
  //   const selectedTeamStillExists = uniqueProjects.some(p => p.teamId === activityObj.teamId);
  //   if (!selectedTeamStillExists) {
  //     activityObj.teamId = null;
  //   }
  // }

  getProjectList(activityObj: Activity) {
    this.projectList = [];

    const key = "teamId";
    this.projectList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
      if (project.clientId == activityObj.clientId) {
        project['displayTeam'] = `${project.projectName} | ${project.teamName}`;
        return { teamId: project.teamId, teamName: project.teamName, projectName: project.projectName, displayTeam: project.displayTeam }
      }
    });
    //console.log("projectList with displayTeam:", this.projectList);
    this.setAllProjects(activityObj, this.projectList);
  }

  // getTeamList(activityObj: Activity){
  //   this.teamList = []

  //   const key = "teamId"
  //   this.teamList = [...new Map(this.allProjectsList.map((team: Timesheet) => [team[key], team])).values()].filter((team: Timesheet) =>{
  //     if( team.teamId == activityObj.teamId) {
  //       return { teamId: team.teamId, teamName: team.teamName }
  //     }
  //   });
  //   //console.log(" teamList :", this.teamList);
  //   this.setAllTeams(activityObj , this.teamList)
  // }

  // setAllTeams(activityObj, teamList: any){
  //   const selectedActivityObj:Activity = this.allTimesheetActivities.find( activity => activity == activityObj);
  //   selectedActivityObj.teamList = teamList;
  //   if((!this.isTimesheetUpdate && activityObj.teamId == "") || !this.teamList.find(team => team.teamId == selectedActivityObj.teamId)){
  //     selectedActivityObj.teamId = '';
  //   }
  // }


  setAllProjects(activityObj, projectList: any) {
    const selectedActivityObj: Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    selectedActivityObj.projectList = projectList;
    if ((!this.isTimesheetUpdate && activityObj.teamId == "") || !this.projectList.find(project => project.teamId == selectedActivityObj.teamId)) {
      // selectedActivityObj.teamId = '';
    }
  }

  getClientLocationList(activityObj: any) {
    this.clientLocationList = [];

    // this.allTimesheetActivities.find(activity => activity == activityObj).clientLocationId = '';
    const key = "clientLocationId";
    this.clientLocationList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].filter((project: Timesheet) => {
      if (project.clientId == activityObj.clientId) {
        return { clientLocationId: project.clientLocationId, clientLocation: project.clientLocation }
      }
    });
    //console.log("clientLocationList :", this.clientLocationList);
    this.setAllClientLocations(activityObj, this.clientLocationList)
  }

  setAllClientLocations(activityObj, clientLocationList: any) {
    const selectedActivityObj: Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    selectedActivityObj.clientLocationList = clientLocationList;

    //console.log("clientLocationList : ", clientLocationList);

    if ((!this.isTimesheetUpdate && activityObj.clientLocationId == "") || !this.clientLocationList.find(clientLocation => clientLocation.clientLocationId == selectedActivityObj.clientLocationId)) {
      selectedActivityObj.clientLocationId = '';
    }
  }



  getAllActivitiesByProjectIdandEmpId(activityObj: any,) {
    let allActivityList = [];

    //console.log("Current Timesheet : ", this.timesheetObj);
    console.log("Activity Object : ", activityObj);
    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.timesheetObj.empId;
    timesheetObj.teamId = activityObj.teamId;
    //console.log(this.allProjectsList, " : all project list");
    console.log(timesheetObj.teamId, " : timesheetObj.teamId");


    // let projectTimesheet = this.allProjectsList.find(project => project.teamId == timesheetObj.teamId);
    // //console.log(" projectTimesheet  :  ", projectTimesheet)


    // timesheetObj.projectId = projectTimesheet.projectId;
    timesheetObj.clientId = this.timesheetObj.clientId;
    timesheetObj.clientLocationId = this.timesheetObj.clientLocationId;
    console.log(" timesheetObj for Activity  :  ", timesheetObj)

    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      console.log("getAllActivitiesByProjectIdandEmpId response : ", response);
      console.log("status" + response.serviceStatus);
      if (response.serviceStatus === "Success") {
        allActivityList = response.serviceResponse;
        // console.log('getAllActivitiesByProjectIdandEmpId',allActivityList)
        allActivityList = allActivityList.sort((a, b) => a.activity.localeCompare(b.activity));
        // console.log('after sorting',allActivityList)
        // console.log("Team name :  ", timesheetObj.teamId);
        // console.log("allActivityList :", allActivityList);
        if (this.timesheetObj.timesheetAppliedFor == "team") {
          let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
          allActivityList = allActivityList.filter(x => x.departmentList?.map(x => +x).includes(teamMember.departmentId));
          // console.log('if',allActivityList)
        } else {

          allActivityList = allActivityList.filter(x => x.departmentList?.map(x => +x).includes(this.currentUser.departmentId));
          // console.log('else',allActivityList)
        }

        //added by priyadarshini for debugging purpose
        // }else {
        //   allActivityList = allActivityList.filter((x) => {
        //     console.log("Processing Activity:", x);

        //     const departmentList = x.departmentList?.map((dep) => +dep);
        //     console.log("Mapped departmentList to numbers:", departmentList);

        //     const isIncluded = departmentList?.includes(this.currentUser.departmentId);
        //     console.log("Does it include currentUser.departmentId:", this.currentUser.departmentId, "=>", isIncluded);

        //     return isIncluded;
        //   });

        //   console.log("Filtered allActivityList:", allActivityList);
        // }

      } else {
        console.error(response.serviceResponse)
      }
      this.setAllProjectActivities(activityObj, allActivityList);
      // console.log("getAllActivitiesByProjectIdandEmpId",allActivityList);
    });
  }

  // getAllAvailableTimesheetByEmpId(employeeObj: User) {
  //   this.availableTimesheets = [];
  //   //console.log(" -- logged availableTimesheets -- ");

  //   let timesheetObj = new Timesheet();
  //   timesheetObj.empId = employeeObj.empId;
  //   this.timesheetService.getbackdatedTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.availableTimesheets = response.serviceResponse;
  //       //console.log("availableTimesheets :", this.availableTimesheets);
  //     } else {
  //       console.error(response.serviceResponse)
  //     }
  //   });
  // }


  getAllAvailableTimesheetByEmpId(employeeObj: User) {
    this.availableTimesheets = [];
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    let currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let endDate: any;
    let startDate: any;
    let OPEN_BACKDATED_DAYS = 30;

    if (this.currentUser.timesheetBackDatedDays) {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
      // console.log("OPEN_BACKDATED_DAYS",this.currentUser.timesheetBackDatedDays);
    }

    if (this.isTimesheetLockCheckEnable == 'false') {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + 1) * DAY_IN_MS));
      // startDate=this.currentUser.dateOfJoining;
      console.log("ch", this.currentUser.dateOfJoining);
    } else {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));
    }

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
    timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);

    //console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
        console.log("availableTimesheets :", this.availableTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // getTimesheetData(template:TemplateRef<any>){

  //   if (this.endDate < this.startDate) {
  //     if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
  //       this.alertMessage = "Please enter Start Date !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }

  //     if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
  //       this.alertMessage = "Please enter End Date !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }
  //     //console.log("end date is small");
  //     this.endDate = ''

  //   } else {
  //     this.allMyTimesheets = [];
  //   }
  // }

  /* View Timesheets */
  getAllMyTimesheetsByEmpId(template?: TemplateRef<any>) {
    this.allMyTimesheets = [];
    if (this.endDate < this.startDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      //console.log("end date is small");
      this.endDate = ''
      this.startDate = ''

    } else {

      let timesheetObj = new Timesheet();
      timesheetObj.empId = this.currentUser.empId;
      timesheetObj.startDate = this.startDate;
      timesheetObj.endDate = this.endDate;

      //console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
      this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allMyTimesheets = response.serviceResponse;
          this.allMyTimesheets.forEach(timesheet => {
            timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
            timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.isNightShiftDisplay = (timesheet.isNightShift == 'true') ? 'Night Shift' : 'Regular Shift';
          });
          //console.log("allMyTimesheets :", this.allMyTimesheets);
        } else {
          console.error(response.serviceResponse)
        }
      });

    }
  }

  /* Timesheets Applied By ME for My Team Members */
  getMyTeamTimesheets(template?: TemplateRef<any>) {
    this.allMyTimesheets = [];

    if (this.endDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      return;
    }

    let timesheetObj = new Timesheet();
    timesheetObj.createdBy = this.currentUser.empId;
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;

    //console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetService.getAllMyTeamTimesheets(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allMyTimesheets = response.serviceResponse;
        this.allMyTimesheets.forEach(timesheet => {
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.isNightShiftDisplay = (timesheet.isNightShift == 'true') ? 'Night Shift' : 'Regular Shift';
        });
        //console.log("allMyTimesheets :", this.allMyTimesheets);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  resetToDate() {
    this.endDate = ''
    this.allMyTimesheets = [];
  }

  getAllMyActivitiesByTimesheetId(timesheet: any) {
    this.allTimesheetActivities = [];

    //console.log("timesheet : ", timesheet);


    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTimesheetActivities = response.serviceResponse;
        const allActivities = [...this.allTimesheetActivities];
        let inactiveActivities: any[] = timesheet.inactiveTimesheetActivities;

        // Removing Inactive Activities from AllTimesheetActivities and added in UpdatedTimesheetActivities
        if (inactiveActivities != null) {
          allActivities.forEach(activityObj => {
            if (inactiveActivities.find(activity => activity.timesheetActivityMapId == activityObj.timesheetActivityMapId)) this.removeInputActivityField(activityObj)
          });
        }

      } else {
        console.error(response.serviceResponse)
      }

      if (this.allTimesheetActivities.length == 0) {
        this.addInputActivityField();
      } else {
        if (this.isTimesheetUpdate) this.isTimesheetUpdateCounter = this.allTimesheetActivities.length;
        this.allTimesheetActivities.forEach(activity => {
          this.getAllActivitiesByProjectIdandEmpId(activity)
          this.getClientLocationList(activity);
          this.getProjectList(activity);
          // this.getTeamList(activity);
        });
      }
    });
  }

  onTeamChanged(newTeamId: number, activityObj: Activity) {

    console.log("New Team ID selected: ", newTeamId);
    activityObj.teamId = newTeamId;

    // Update the activity list based on the selected team
    this.getClientLocationList(activityObj);
    this.getProjectList(activityObj);
    this.getAllActivitiesByProjectIdandEmpId(activityObj);
  }


  viewAllMyActivitiesByTimesheetId(timesheet: any) {
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        //console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getEmployeeBasicInfo() {
    let employeeObj = new Employee();
    employeeObj.email = this.currentUser.email;
    this.employeeService.getEmployeeBasicInfo(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let employeeInfo = response.serviceResponse;
        this.currentUser.isTimesheetLockCheckEnable = JSON.parse(JSON.stringify(employeeInfo.isTimesheetLockCheckEnable));
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.isTimesheetLockCheckEnable = employeeInfo.isTimesheetLockCheckEnable;
        // console.log("doj",this.currentUser.dateOfJoining);
        //console.log("isTimesheetLockCheckEnable : ", this.isTimesheetLockCheckEnable);

        let userObj: User = new User();
        userObj.empId = this.currentUser.empId;
        userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
        this.getAllAvailableTimesheetByEmpId(userObj);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // exportToExcel(id:any): void {

  //   // if (this.isTimesheetTable == true) {
  //   //   this.excelName = 'MyTimeSheet.xlsx'

  //   //   const _allEmployeeList = this.allMyTimesheets.slice()
  //   //   this.allMyTimesheetsDataForExcel = _allEmployeeList.sort((a, b) => (new Date(a.date).getTime() > new Date(b.date).getTime())? 1 : -1);

  //   //   const onlySpecificDataArr = this.allMyTimesheetsDataForExcel.map(
  //   //     x => ({
  //   //       "Date": x.date,
  //   //       "Day Type": x.dayType,
  //   //       "In Time": x.officeInTime,
  //   //       "Out Time": x.officeOutTime,
  //   //       "Total Working Hours": x.totalWorkingOfficeHours,
  //   //       "Timesheet Details": x.description?.replaceAll('<br>', ' \n'),
  //   //       "Total Activity Time": x.totalTime,
  //   //       "Status": x.status,
  //   //       "Applied By": x.createdByName,
  //   //       "Applied On": x.createdOn,
  //   //       "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
  //   //       "Leave Type": x.leaveType,
  //   //       "Remarks": x.remarks
  //   //     })
  //   //   )
  //   //   // this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  //   // }
  //   const tableId = id; // Replace with your actual table ID
  //   this.excelName = "MyTimeSheet.xlsx";
  //   this.tableName= 'My Timesheet';

  //   this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);

  // }
  exportToExcel(): void {

    if (this.isTimesheetTable == true) {
      this.excelName = 'MyTimeSheet.xlsx'

      const _allEmployeeList = this.allMyTimesheets.slice()
      this.allMyTimesheetsDataForExcel = _allEmployeeList.sort((a, b) => (new Date(a.date).getTime() > new Date(b.date).getTime()) ? 1 : -1);

      const onlySpecificDataArr = this.allMyTimesheetsDataForExcel.map(
        x => ({
          "Date": x.date,
          "Day Type": x.dayType,
          "In Time": x.officeInTime,
          "Out Time": x.officeOutTime,
          "Total Working Hours": x.totalWorkingOfficeHours,
          "Timesheet Details": x.description?.replaceAll('<br>', ' \n'),
          "Total Activity Time": x.totalTime,
          "Status": x.status,
          "Applied By": x.createdByName,
          "Applied On": x.createdOn,
          "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
          "Leave Type": x.leaveType,
          "Remarks": x.remarks
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

  }

  getAllMyLeaveApplicationsByEmpId(userObj: User) {
    this.leaveHistoryList = [];
    let leaveObj = new Leave();
    leaveObj.empId = userObj.empId;
    this.leaveService.getAllMyLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveHistoryList = response.serviceResponse;
        //console.log("leaveHistoryList In Timesheet : ", this.leaveHistoryList);
        this.leaveHistoryList = this.leaveHistoryList.filter(leaveApplication => leaveApplication.status == 'Approved');
        //console.log("leave Approved  History : ", this.leaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }






  //modals
  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openNightShiftTemplate(template: TemplateRef<any>, event) {
    if (event.target.checked) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    }
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.viewAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  async copyTimesheetDetailsToNotepad(timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;

    let content = "Employment Id: A-" + this.currentUser.employeementId
      + " Name: " + this.currentUser.name
      + " Date: " + this.timesheetObj.date
      + " Daytype: " + this.timesheetObj.dayType
      + " ";

    const response: any = await this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).toPromise();

    if (response.serviceStatus == "Success") {
      let activityList = response.serviceResponse;
      let totalActivity = "";
      activityList.forEach((eodActivity, index) => {
        index = index + 1;
        totalActivity += index + ")" + "Project Name: " + eodActivity.projectName + " Activity: " + eodActivity.activity
          + " description: " + eodActivity.description + " Time taken: " + eodActivity.completionTime + "hrs. ";
      })
      this.clipboardService.copy(content + " " + totalActivity);
    } else if (response.serviceResponse = "No activities found.Activity list is empty") {
      this.clipboardService.copy(content + " " + " description: " + timesheetObj.description)
    }
  }

  // validateDescription(event,data:any){

  // if (!this.validationService.validateActivityTimesheetDiscription(data)) {
  //   this.errorMsg = "Please enter valid Description !!"
  // }
  // else{
  //   this.errorMsg = ""
  // }
  // if(this.errorMsg == ""){
  //   event.target.nextElementSibling.textContent = ""
  // }else{
  //   event.target.nextElementSibling.textContent =  this.errorMsg
  // }
  // }
  validateTime(event, data: any) {
    if (!this.validationService.validateTimesheetCompletionTime(data)) {
      this.errorMsg = "Please enter Time !!"
    } else if (!this.validationService.validateExperiencedNumber(data)) {
      this.errorMsg = "Please enter Valid Time !!"
    }
    else if (data <= 0 || data > 24) {
      this.errorMsg = "Total Time Must be greater than 0 hrs and maximum upto 24 hrs!! "
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }

  }

  omit_special_char(event) {

    var k;
    k = event.charCode;  //        k = event.keyCode;  (Both can be used)
    //console.log("omit function" + k);
    //console.log((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || (k >= 48 && k <= 57));
    if ((k == 43) || (k == 45) || (k == 69) || (k == 101)) {
      return (false);
    }
    else {
      return (true)
    }
    //return ((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || (k >= 48 && k <= 57));
  }

  onPaste(e) {
    e.preventDefault();
    return false;
  }



  validateClientName(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Client Name !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }


  preventScroll(event: WheelEvent): void {
    event.preventDefault();
  }



  validateClientLocation(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Client Location !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }
  validateProjectName(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Project Name !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }

  validateActivity(event, data: any) {

    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please Select Activity !!"
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }
  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

}


//    ngOnChanges(changes: SimpleChanges): void {
//   if (changes['autofillData'] && changes['autofillData'].currentValue) {
//     console.log('ngOnChanges - autofillData updated', changes['autofillData'].currentValue);
//     this.autofill(changes['autofillData'].currentValue);
//   }
// }




// autofill(data: any): void {
//   const defaultTimesheet = new Timesheet();

//   this.timesheetObj = {
//     ...defaultTimesheet,
//     ...data,
//     timesheetAppliedFor: data.timesheetAppliedFor || 'self',
//     empId: data.empId ?? null,
//     dayType: data.dayType ?? null,
//     date: data.date ? new Date(data.date) : null,
//     officeInTime: data.officeInTime ? new Date(data.officeInTime) : null,
//     officeOutTime: data.officeOutTime ? new Date(data.officeOutTime) : null,
//     description: data.description || '',
//     isNightShift: data.isNightShift ?? false,
//     totalWorkingOfficeHours: data.totalWorkingOfficeHours ?? 0,
//     allTimesheetActivities: []
//   };

//   console.log('Autofilled timesheetObj:', this.timesheetObj);

//   // Bind activities
//   if (Array.isArray(data.activities)) {
//     this.bindActivities(data.activities);

//     console.log('Autofilled allTimesheetActivities:', this.allTimesheetActivities);
//   }
// }


// bindActivities(activities: any[]): void {
//   this.allTimesheetActivities = [];

//   for (let act of activities) {
//     const activityObj: any = {
//       clientId: act.clientId ?? null,
//       clientLocationId: act.clientLocationId ?? null,
//       teamId: act.teamId ?? null,
//       activityId: act.activityId ?? null,
//       description: act.description ?? '',
//       completionTime: act.completionTime ?? null,
//       clientLocationList: [],
//       projectList: [],
//       projectActivities: []
//     };

//     this.allTimesheetActivities.push(activityObj);

//     // Pre-load dropdowns
//     this.getClientLocationList(activityObj);
//     this.getProjectList(activityObj);
//     this.getAllActivitiesByProjectIdandEmpId(activityObj);
//   }

//   // Assign to main object (required if template depends on this field)
//   this.timesheetObj.allTimesheetActivities = this.allTimesheetActivities;
// }







//   timesheetObj: any = {
//     timesheetAppliedFor: 'self',
//     dayType: '',
//     officeInTime: '',
//     officeOutTime: '',
//     totalWorkingOfficeHours: 0,
//     isNightShift: false,
//     date: '',
//     empId: '',
//     remarks: '',
//     description: ''
//   };

//   allTimesheetActivities = [this.createEmptyActivity()];
//   teamMemberList: any[] = [];
//   currentUser: User;
//   selectedTeamMemberName: string = '';

//   holidayList: any[] = [];
//   holidaystateObj: any = {};

//   availableTimesheets: any[] = [];
//   AllWeekOfList: any[] = [];

//   disableCreateUpdateTimesheet: boolean = false;

//   constructor(
//     private authenticationService: AuthenticationService,
//     private teamViewService: TeamViewService,
//     private timesheetService: TimesheetService,
//     private modalService: BsModalService,
//     private router: Router,
//     private holidayService: HolidayService,
//     private datePipe: DatePipe,
//     private employeeService: EmployeeService,
//     private selfTimeSheetServiceService :SelfTimeSheetServiceService,
//   ) {
//     this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
//   }



// ngOnChanges(changes: SimpleChanges): void {
//   if (changes['autofillData'] && this.autofillData) {
//     console.log('Detected autofillData update:', this.autofillData);
//     this.patchFormFromLastData();
//   }
// }

//     openAlertMod(template: TemplateRef<any>, message: any) {
//     this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
//     this.alertMessage = message;
//   }

//  patchFormFromLastData(): void {
//   const data = this.autofillData;

//   this.timesheetObj = {
//   checkId: data?.checkId ?? null,
//   timesheetId: data?.timesheetId ?? null,
//   allTimesheetActivities: [],
//   updatedTimesheetActivities: data?.updatedTimesheetActivities ?? null,

//   createdOn: data?.createdOn ?? null,
//   createdBy: data?.createdBy ?? null,
//   createdByName: data?.createdByName ?? null,
//   updatedOn: data?.updatedOn ?? null,
//   updatedBy: data?.updatedBy ?? null,
//   currentManagerId: data?.currentManagerId ?? null,

//   projectId: data?.projectId ?? null,
//   projectName: data?.projectName ?? '',
//   projectManagerId: data?.projectManagerId ?? null,
//   managerId: data?.managerId ?? null,
//   empId: data?.empId || this.timesheetObj.empId || this.currentUser.empId,

//   // Add the required 'project' property
//   project: data?.project ?? null,

//   startDate: data?.startDate ?? null,
//   endDate: data?.endDate ?? null,
//   status: data?.status ?? null,

//   applicationCount: data?.applicationCount ?? null,
//   employeementId: data?.employeementId ?? null,
//   weekDayName: data?.weekDayName ?? null,
//   totalWorkingHours: data?.totalWorkingHours ?? null,
//   totalWorkingHoursPercentage: data?.totalWorkingHoursPercentage ?? null,

//   isConsultant: data?.isConsultant ?? null,
//   totalTime: data?.totalTime ?? 0,

//   timesheetStatusUpdatedBy: data?.timesheetStatusUpdatedBy ?? null,
//   timesheetStatusUpdatedByName: data?.timesheetStatusUpdatedByName ?? null,
//   employeeName: data?.employeeName ?? null,
//   rejectReason: data?.rejectReason ?? null,
//   email: data?.email ?? null,

//   clientId: data?.clientId ?? null,
//   clientName: data?.clientName ?? '',
//   clientLocationId: data?.clientLocationId ?? null,
//   clientLocation: data?.clientLocation ?? '',

//   timesheetAppliedFor: this.timesheetObj.timesheetAppliedFor || 'self',
//   dayType: data?.dayType || '',
//   date: data?.date || '',
//   officeInTime: data?.officeInTime ? new Date(data.officeInTime.replace(' ', 'T')) : '',
//   officeOutTime: data?.officeOutTime ? new Date(data.officeOutTime.replace(' ', 'T')) : '',
//   totalWorkingOfficeHours: data?.totalWorkingOfficeHours || '00:00',
//   isNightShift: data?.isNightShift || false,

//   remarks: data?.remarks || '',
//   description: data?.description || '',

//   teamId: data?.teamId ?? null,
//   teamName: data?.teamName ?? '',
//   activity: data?.activity ?? '',
//   activityId: data?.activityId ?? null,

//   managerEmail: data?.managerEmail ?? null,
//   managerName: data?.managerName ?? null,

//   leaveType: data?.leaveType ?? null,
//   isCron: data?.isCron ?? null,
//   year: data?.year ?? null,
//   month: data?.month ?? null,

//   displayTeam: data?.displayTeam ?? '',
//   inactiveTimesheetActivities: data?.inactiveTimesheetActivities ?? null,
//   name: data?.name ?? '',
//   inTime: data?.inTime ?? null,
//   outTime: data?.outTime ?? null,
//   appliedOn: data?.appliedOn ?? null,
//   selected: data?.selected ?? null,
//   timeSheet: data?.timeSheet ?? null,
//   nightShift: data?.nightShift ?? null,


//   isSelected: false,
//   bulkApprovedList: [],
//   bulkRejectList: [],
//   queryList: []
// } ;

// console.log('Patched timesheetObj:', this.timesheetObj);

// console.log('working type:', this.timesheetObj.dayType);
//   this.allTimesheetActivities = [
//     {
//       clientId: data.clientId || '',
//       clientName: data.clientName || '',
//       clientLocationId: data.clientLocationId || '',
//       clientLocation: data.clientLocation || '',
//       teamId: data.teamId || '',
//       teamName: data.teamName || '',
//       activityId: data.activityId || '',
//       activity: data.activity || '',
//       description: data.description || '',
//       projectId: data.projectId || '',
//       projectName: data.projectName || '',
//       completionTime: data.totalTime || 0,
//       clientLocationList: data.clientLocation ? [{ clientLocationId: data.clientLocationId, clientLocation: data.clientLocation }] : [],
//       projectList: data.teamName ? [{ teamId: data.teamId, teamName: data.teamName, projectName: data.projectName }] : [],
//       projectActivities: data.activity ? [{ activityId: data.activityId, activity: data.activity }] : []
//     }
//   ];

//   this.timesheetObj.allTimesheetActivities = this.allTimesheetActivities;

//   console.log('Patched allTimesheetActivities:', this.allTimesheetActivities); // Debugging line

//   this.clientList = data.clientName ? [{ clientId: data.clientId, clientName: data.clientName }] : [];

//   console.log('Patched allTimesheetActivities:', this.allTimesheetActivities); // Debugging line
// }





//   getAllTeamMemberList(): void {
//     const employeeObj = new Employee();
//     employeeObj.empId = this.currentUser.empId;

//     this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
//       if (response.serviceStatus === 'Success') {
//         this.teamMemberList = response.serviceResponse;
//         if (this.autofillData?.empId) {
//           this.timesheetObj.empId = String(this.autofillData.empId);
//           this.onTeamMemberChange(this.timesheetObj.empId);
//         }
//       } else {
//         console.error('Failed to load team members');
//       }
//     });
//   }

//   createEmptyActivity() {
//     return {
//       clientId: '',
//       clientName: '',
//       clientLocationId: '',
//       clientLocation: '',
//       teamId: '',
//       teamName: '',
//       activityId: '',
//       activity: '',
//       description: '',
//       projectId: '',
//       projectName: '',
//       completionTime: 0,
//       clientLocationList: [],
//       projectList: [],
//       projectActivities: [],
//     };
//   }

//   addInputActivityField(activity: any): void {
//     const newField = { ...JSON.parse(JSON.stringify(activity)), activityId: '', completionTime: 0 };
//     this.allTimesheetActivities.push(newField);
//   }
//   removeInputActivityField(index: number): void {
//   if (this.allTimesheetActivities.length > 1) {
//     this.allTimesheetActivities.splice(index, 1);
//   }
// }


//   // onCreateTimesheet(): void {
//   //   const dateFormat = 'YYYY-MM-DD';
//   //   const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

//   //   this.timesheetObj.description = this.timesheetObj.description?.trim();

//   //   if (this.timesheetObj.timesheetAppliedFor === 'self') {
//   //     this.timesheetObj.empId = this.currentUser.empId;
//   //   } else if (!this.timesheetObj.empId || this.timesheetObj.empId === '') {
//   //     alert("Please select a team member.");
//   //     return;
//   //   }

//   //   const isWorkingDay = !['Public Holiday', 'Week Off', 'Leave'].includes(this.timesheetObj.dayType);
//   //   this.timesheetObj.allTimesheetActivities = isWorkingDay && this.allTimesheetActivities.length > 0 ? this.allTimesheetActivities : null;

//   //   if (isWorkingDay) {
//   //     this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
//   //     this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
//   //     this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
//   //   } else {
//   //     this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
//   //   }

//   //   this.timesheetObj.createdBy = this.currentUser.empId;
//   //   this.timesheetObj.createdByName = this.currentUser.name;
//   //   this.timesheetObj.currentManagerId =
//   //     this.currentUser.approvalsTo === 'Reporting Manager'
//   //       ? this.currentUser.reportingManagerId
//   //       : this.currentUser.managerId;

//   //   console.log("Final payload to submit:", this.timesheetObj);
//   //   this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe({
//   //     next: (response: any) => {
//   //       if (response.serviceStatus === "Success") {
//   //         this.openAlertMod(this.alertTemplate, "Timesheet created successfully!");
//   //       } else {
//   //         this.openAlertMod(this.alertTemplate, "Failed: " + response.serviceResponse);
//   //         console.error("Error creating timesheet:", response.serviceResponse);
//   //       }
//   //     },
//   //     error: (err) => {
//   //       console.error("Server error:", err);
//   //       alert(" Server error occurred.");
//   //     }
//   //   });
//   // }


//   onCreateTimesheet(template: TemplateRef<any>): void {
//   const dateFormat = 'YYYY-MM-DD';
//   const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

//   this.timesheetObj.description = this.timesheetObj.description?.trim();

//   if (this.timesheetObj.timesheetAppliedFor === 'self') {
//     this.timesheetObj.empId = this.currentUser.empId;
//   } else if (!this.timesheetObj.empId || this.timesheetObj.empId === '') {
//     this.openAlertMod(template, "Please select a team member.");
//     return;
//   }

//   // Validation
//   let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template);
//   if (!inputValidated) return;

//   const isWorkingDay = !['Public Holiday', 'Week Off', 'Leave'].includes(this.timesheetObj.dayType);

//   this.timesheetObj.allTimesheetActivities =
//     isWorkingDay && this.allTimesheetActivities.length > 0 ? this.allTimesheetActivities : null;

//   if (isWorkingDay) {
//     this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
//     this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
//     this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
//   } else {
//     this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
//   }

//   this.timesheetObj.createdBy = this.currentUser.empId;
//   this.timesheetObj.createdByName = this.currentUser.name;

//   if (this.currentUser.approvalsTo === 'Reporting Manager') {
//     this.timesheetObj.currentManagerId = this.currentUser.reportingManagerId;
//   } else {
//     this.timesheetObj.currentManagerId = this.currentUser.managerId;
//   }

//   console.log("Submitting Timesheet:", this.timesheetObj);

//   this.timesheetService.addTimesheet(this.timesheetObj).pipe(first()).subscribe({
//     next: (response: any) => {
//       if (response.serviceStatus === "Success") {
//         this.openAlertMod(template, "Timesheet created successfully!");

//         // Redirect to view after slight delay (to allow modal to be seen)
//         setTimeout(() => {
//           if (this.timesheetObj.timesheetAppliedFor === "self") {
//             this.router.navigate(['/user-timesheet/my-timesheet']);
//           } else {
//             this.router.navigate(['/user-timesheet/team-timesheet']);
//           }
//         }, 1000);

//       } else {
//         this.openAlertMod(template, "Failed: " + response.serviceResponse);
//       }
//     },
//     error: (err) => {
//       console.error("Server error:", err);
//       this.openAlertMod(template, "Server error occurred.");
//     }
//   });
// }
// validateTimesheetObj(timesheetObj: any, template: TemplateRef<any>): boolean {
//   if (!timesheetObj.dayType) {
//     this.openAlertMod(template, 'Please select Day Type.');
//     return false;
//   }

//   if (['Working', 'Non-working'].includes(timesheetObj.dayType)) {
//     if (!timesheetObj.officeInTime || !timesheetObj.officeOutTime) {
//       this.openAlertMod(template, 'Please select both In Time and Out Time.');
//       return false;
//     }

//     if (!timesheetObj.totalWorkingOfficeHours || timesheetObj.totalWorkingOfficeHours === '00:00') {
//       this.openAlertMod(template, 'Total working hours cannot be zero.');
//       return false;
//     }

//     if (!this.allTimesheetActivities || this.allTimesheetActivities.length === 0) {
//       this.openAlertMod(template, 'Please add at least one activity.');
//       return false;
//     }

//     for (let i = 0; i < this.allTimesheetActivities.length; i++) {
//       const act = this.allTimesheetActivities[i];
//       if (!act.clientId || !act.clientLocationId || !act.teamId || !act.activityId || !act.completionTime) {
//         this.openAlertMod(template, `Please complete all fields in activity #${i + 1}.`);
//         return false;
//       }
//     }
//   }

//   return true;
// }



// initializeTimesheet() {
//   this.timesheetObj = this.selfTimeSheetServiceService.getEmptyTimesheetObject();
//   this.allTimesheetActivities = [this.selfTimeSheetServiceService.getEmptyActivity()];
//   this.clientList = this.selfTimeSheetServiceService.getClientList();
// }

// onTimesheetTypeChange(): void {
//   if (this.timesheetObj.timesheetAppliedFor === 'team') {
//     this.teamMemberList = this.timesheetService.getAllTeamMemberList();
//   }
// }

// onTeamMemberChange(empId: string): void {
//   const selectedMember = this.teamMemberList.find(member => member.empId === empId);
//   this.selectedTeamMemberName = selectedMember ? selectedMember.name : '';
// }

//   onClientChange(activityObj: Activity): void {
//     this.selfTimeSheetServiceService.getClientLocationList(activityObj);
//   }

//   onClientLocationChange(activityObj: Activity): void {
//     this.selfTimeSheetServiceService.getProjectList(activityObj);
//   }

//   onProjectChange(activityObj: Activity): void {
//     this.selfTimeSheetServiceService.getAllActivitiesByProjectIdandEmpId(activityObj);
//   }

//   setActivity(activityObj: Activity): void {
//     this.selfTimeSheetServiceService.setActivity(activityObj);
//   }

//   validateClientName(event: any, clientId: string): void {
//     this.selfTimeSheetServiceService.validateClientName(event, clientId);
//   }

//   validateClientLocation(event: any, locationId: string): void {
//     this.selfTimeSheetServiceService.validateClientLocation(event, locationId);
//   }

//   validateActivity(event: any, activityId: string): void {
//     this.selfTimeSheetServiceService.validateActivity(event, activityId);
//   }

//   validateTime(event: any, time: number): void {
//     this.selfTimeSheetServiceService.validateTime(event, time);
//   }

//   // addInputActivityField(activityObj: Activity): void {
//   //   this.selfTimeSheetServiceService.addInputActivityField(activityObj, this.allTimesheetActivities);
//   // }

//   // removeInputActivityField(index: number): void {
//   //   this.selfTimeSheetServiceService.removeInputActivityField(index, this.allTimesheetActivities);
//   // }

//   // onCreateTimesheet(alertTemplate: TemplateRef<any>): void {
//   //   const response = this.selfTimeSheetServiceService.createTimesheet(this.timesheetObj, this.allTimesheetActivities);
//   //   if (response.success) {
//   //     this.alertMessage = 'Timesheet created successfully!';
//   //   } else {
//   //     this.alertMessage = response.message;
//   //   }
//   //   this.modalRef = this.modalService.show(alertTemplate);
//   // }

//   preventScroll(event: WheelEvent): void {
//     event.preventDefault();
//   }

//   preventManualDateInput(event: KeyboardEvent): void {
//   this.timesheetService.preventManualDateInput(event);
// }

// omitSpecialChar(event: KeyboardEvent): boolean {
//   return this.timesheetService.omitSpecialChar(event);
// }


//   // omit_special_char(event: KeyboardEvent): boolean {
//   //   return this.selfTimeSheetServiceService.omitSpecialChar(event);
//   // }




