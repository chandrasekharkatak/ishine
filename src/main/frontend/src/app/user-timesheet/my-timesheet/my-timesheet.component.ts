import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ClipboardService } from 'ngx-clipboard';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Activity } from 'src/app/models/activity';
import { Employee } from 'src/app/models/employee';
import { EmployeeClientSideIdMapping } from 'src/app/models/employeeClientSideIdMapping';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { Project } from 'src/app/models/project';
import { ProjectClientSideId } from 'src/app/models/projectClientSideId';
import { Timesheet } from 'src/app/models/timesheet';
import { TimesheetDoc } from 'src/app/models/timesheetDoc';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { InputValidationService } from 'src/app/services/input-validation.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-my-timesheet',
  templateUrl: './my-timesheet.component.html',
  styleUrls: ['./my-timesheet.component.css']
})
export class MyTimesheetComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;

  @ViewChild("clientSideIdNotMandatoryFound")
  clientSideIdNotMandatoryFound: TemplateRef<any>;

  @ViewChild('fileInput') fileInput!: ElementRef;


  data: string;
  feature = "My Timesheets";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  //flags
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isTimesheetForm: boolean = false;
  isTimesheetBulkForm: boolean = false;
  isTimesheetTable: boolean = false;

  isTimesheetUpdate: boolean = false;
  isTimesheetUpdateCounter = 0;

  isSelfTimesheets: boolean = false;
  isTeamTimesheets: boolean = false;


  Allholidays: any[] = [];
  AllWeekOfList: any[] = [];

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj
  timesheetObj: Timesheet = new Timesheet();
  allTimesheetActivities: any[] = []
  allProjectsList: any[] = [];
  allActivityList: any[] = [];

  allMyTimesheets: any[] = [];
  timesheetActivities: any[] = [];
  startDate: any;
  endDate: any;
  isPolicySidebarOpen = false;
  expandedSection = "attendance";

  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  availableTimesheetDates: any[] = [];
  availableTimesheets: any[] = [];

  projectList: any[] = [];
  clientList: any[] = [];
  clientLocationList: any[] = [];
  // teamList:any[] = [];

  teamMemberList: any[] = [];
  errorMsg: any;
  serverDate: any;

  leaveHistoryList: any[] = [];
  maxOutTimeDate: any;
  disableCreateUpdateTimesheet: boolean = false;
  isAutoFilled: boolean = false;
  selectedDate: Date | undefined;

  isTimesheetLockCheckEnable: any = "true";
  employeeInTNMProject: boolean = false;




  selectedTimesheet: any;
  today = new Date().toISOString().split('T')[0];

  filters: any = {};
  isSearchEnabled: boolean = false;
  selfTimesheetColumns: any[] = ['blank', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'description', 'totalTime', 'clientInTime', 'clientOutTime', 'totalClientWorkingHours', 'clientApprovalStatus', 'filledDocument', 'approvedDocument', 'status', 'createdByName', 'createdOn', 'isNightShiftDisplay', 'leaveType', 'remarks'];
  teamTimesheetColumns: any[] = ['blank', 'employeeName', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'description', 'totalTime', 'clientInTime', 'clientOutTime', 'totalClientWorkingHours', 'clientApprovalStatus', 'filledDocument', 'approvedDocument', 'status', 'createdOn', 'isNightShiftDisplay', 'leaveType', 'remarks'];
  tableName: string;
  activeProjectList: Project[];
  selectedProjectId: any;
  // selfClientIdModalRef: BsModalRef = new BsModalRef();
  selfClientIdUpdateModalRef: BsModalRef = new BsModalRef();
  updateClientIdModalRef: BsModalRef = new BsModalRef();
  noNotAppliedYetModalRef: BsModalRef = new BsModalRef();
  clientSideIdNotMandatoryFoundModalRef: BsModalRef = new BsModalRef();
  clientSideIdNotMandatory: Boolean = false;
  employeeList: any[];
  selectedFile: File | null = null;
  selectedFile2: File | null = null;
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  projectClientIdList: ProjectClientSideId[] = [];
  shadowForSelf: Boolean = false;
  previewUrl1: SafeResourceUrl | null = null;
  previewUrl2: SafeResourceUrl | null = null;
  rawObjectUrl1: string | null = null;
  rawObjectUrl2: string | null = null;
  fileType1: '' | 'pdf' | 'image' | null = null;
  fileType2: '' | 'pdf' | 'image' | null = null;
  fileError1: string = '';
  fileError2: string = '';
  fileName1: any = null;
  fileName2: any = null;
  docData1: any;
  docData2: any;
  activePreviewUrl: SafeResourceUrl | null = null;
  activeFileType: string | null = null;
  mimeType: any;
  projectRequiresClientId: Boolean = false;
  fromDate: any = null;
  toDate: any = null;
  finalFromDate: any = null;
  finalToDate: any = null;
  clientSideIdForm: BsModalRef = new BsModalRef();
  noClientSideIdProvided: BsModalRef = new BsModalRef();
  @ViewChild("update_clientId")
  updateClientId: TemplateRef<any>;
  @ViewChild("clientSideIdForm")
  clientSideIdFormRef: TemplateRef<any>;
  alertWithResetModRef: BsModalRef = new BsModalRef();
  @ViewChild("alert_message_with_reset")
  alertModalWithoutReload: TemplateRef<any>;

  previousFilledDocument: any;
  previousApprovedDocument: any;
  minDate: string;
  maxDate: string;
  maxToDate: Date | null = null;
  disableList: any;
  disableListFormatted: Date[] = [];
  hours: string[] = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'));
  minutes: string[] = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'));
  periods: string[] = ['AM', 'PM'];
  selectedInHour: any = null;
  selectedInMinute: any = null;
  selectedInPeriod: any = null;
  selectedOutHour: any = null;
  selectedOutMinute: any = null;
  selectedOutPeriod: any = null;
  selectedClientInHour: any = null;
  selectedClientInMinute: any = null;
  selectedClientInPeriod: any = null;
  selectedClientOutHour: any = null;
  selectedClientOutMinute: any = null;
  selectedClientOutPeriod: any = null;
  timesheetFillable = true;
  projectId: any;
  clientIdNeeded: boolean;
  autoFillTimesheet:boolean = false;

  //latestProjectId = this.activeProjectList

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
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute,
    private inputValidationService:InputValidationService,
    private router: Router

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    //this.getProjectClientSideStatus();
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.timesheetService.getServerDate().pipe(first()).subscribe((response: any) => {
      this.serverDate = response;
    });

    this.route.queryParams.subscribe(params => {
      if (params['date']) {
        this.isAutoFilled = true;
        this.timesheetObj.timesheetAppliedFor = 'self';
        this.selectedDate = new Date(params['date']);
      }
      if (this.isAutoFilled) {
        this.loadAutofillData()
      }
    });
    this.clientSideIdNotMandatory = true;
    this.shadowForSelf = false;

    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.getAllHolidays();
    this.getAllHolidaysbystate();
    this.getAllMyLeaveApplicationsByEmpId(this.currentUser);
    this.sectionViewInit();
    this.preventBackButton();
    this.isEmployeeInTNMProject();
    this.getActiveProjectsByEmpId();
    this.thisMonthValidation();
    // this.setStartDateMinMax();
    this.timeReset();
    this.timesheetFillable = true;
    // this.makeApmosysInTime();
    // this.makeApmosysOutTime();
    // this.setTotalWorkingOfficeHours();
    // this.setTotalWorkingClientHours()
    console.log("timesheetObj:", this.timesheetObj);

  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }




  getFormattedTime(selectedHour: any, selectedMinute: any, selectedPeriod: any): string {
    return `${selectedHour}:${selectedMinute} ${selectedPeriod}`;
  }

  get24HourTime(hour: string, minute: string, period: string): string {
    let hr = parseInt(hour, 10);
    if (period === 'PM' && hr < 12) hr += 12;
    if (period === 'AM' && hr === 12) hr = 0;
    return `${String(hr).padStart(2, '0')}:${minute}:00`;
  }

  getFullDateTime(date: Date, hour: string, minute: string, period: string): Date {
  let h = parseInt(hour, 10);
  const m = parseInt(minute, 10);

  if (period === 'PM' && h < 12) h += 12;
  if (period === 'AM' && h === 12) h = 0;

  const newDate = new Date(date);
  newDate.setHours(h, m, 0, 0);

  const now = new Date();

  // If date is today and time is greater than now → throw error
  const isSameDate =
    newDate.getFullYear() === now.getFullYear() &&
    newDate.getMonth() === now.getMonth() &&
    newDate.getDate() === now.getDate();

  if (isSameDate && newDate.getTime() > now.getTime()) {
    throw new Error('Selected time cannot be greater than the current time for today.');
  }

  return newDate;
}



 makeApmosysInTime() {
  try {
    if (this.fromDate && this.selectedInHour && this.selectedInMinute && this.selectedInPeriod) {
      const officeInTime = this.getFullDateTime(
        this.fromDate,
        this.selectedInHour,
        this.selectedInMinute,
        this.selectedInPeriod
      );

      this.timesheetObj.officeInTime = new Date(officeInTime);
      this.setTotalWorkingOfficeHours();
    } else {
      this.timesheetObj.officeInTime = null;
    }

    if (this.syncTimes) {
      this.selectedClientInHour = this.selectedInHour;
      this.selectedClientInMinute = this.selectedInMinute;
      this.selectedClientInPeriod = this.selectedInPeriod;
      this.makeClientInTime();
    }

    console.log('Office In Time: ', this.timesheetObj.officeInTime);

  } catch (error) {
    console.error(error);
    alert('Selected time cannot be greater than the current time for today.');

    this.selectedInHour = null;
    this.selectedInMinute = null;
    this.selectedInPeriod = null;
    this.timesheetObj.officeInTime = null;

    
  }
}


  makeApmosysOutTime() {
    try {
      if ((this.fromDate || this.toDate) && this.selectedOutHour && this.selectedOutMinute && this.selectedOutPeriod) {
        const outDate = this.toDate || this.fromDate;
        const officeOutTime = this.getFullDateTime(
          outDate,
          this.selectedOutHour,
          this.selectedOutMinute,
          this.selectedOutPeriod
        );
        this.timesheetObj.officeOutTime = new Date(officeOutTime);
        this.setTotalWorkingOfficeHours()
      } else {
        this.timesheetObj.officeOutTime = null;
      }

      if (this.syncTimes) {
        this.selectedClientOutHour = this.selectedOutHour;
        this.selectedClientOutMinute = this.selectedOutMinute;
        this.selectedClientOutPeriod = this.selectedOutPeriod;
        this.makeClientOutTime();
      }
      console.log("Office Out Time: ", this.timesheetObj.officeOutTime);
    } catch (error) {
      console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      this.selectedOutHour = null;
      this.selectedOutMinute = null;
      this.selectedOutPeriod = null;
      this.timesheetObj.officeOutTime = null;
    }
  }

  makeClientInTime() {
    try{
    if (this.fromDate && this.selectedClientInHour && this.selectedClientInMinute
      && this.selectedClientInPeriod) {
      let officeClientInTime = null;
      officeClientInTime = this.getFullDateTime(
        this.fromDate,
        this.selectedClientInHour,
        this.selectedClientInMinute,
        this.selectedClientInPeriod
      );
      this.timesheetObj.clientInTime = new Date(officeClientInTime);
      this.setTotalWorkingClientHours();
    } else {
      this.timesheetObj.clientInTime = null;
    }
    console.log("Client In Time: ", this.timesheetObj.clientInTime);
  } catch (error) {
    console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      this.selectedClientInHour = null;
      this.selectedClientInMinute = null;
      this.selectedClientInPeriod = null;
       this.timesheetObj.clientInTime = null;
  }
  }
  makeClientOutTime() {
    try{
    let officeClientOutTime = null;
    if (this.fromDate && this.selectedClientOutHour && this.selectedClientOutMinute
      && this.selectedClientOutPeriod) {
      if (this.toDate) {
        officeClientOutTime = this.getFullDateTime(
          this.toDate,
          this.selectedClientOutHour,
          this.selectedClientOutMinute,
          this.selectedClientOutPeriod
        );
      } else {
        officeClientOutTime = this.getFullDateTime(
          this.fromDate,
          this.selectedClientOutHour,
          this.selectedClientOutMinute,
          this.selectedClientOutPeriod
        );
      }
      this.timesheetObj.clientOutTime = new Date(officeClientOutTime);
      this.setTotalWorkingClientHours();
    } else {
      this.timesheetObj.clientOutTime = null;
    }
    console.log("Client Out Time: ", this.timesheetObj.clientOutTime);
  } catch (error) {
    alert('Selected time cannot be greater than the current time for today.');
    this.selectedClientOutHour = null;
    this.selectedClientOutMinute = null;
    this.selectedClientOutPeriod = null;
    this.timesheetObj.clientOutTime = null
  }
  }
  // AndOutTime(){
  //   let officeClientInTime = null;
  //   let officeClientOutTime = null;
  //   if (this.fromDate && this.selectedClientInHour && this.selectedClientInMinute
  //     && this.selectedClientInPeriod && this.selectedClientOutHour && this.selectedClientOutMinute
  //     && this.selectedClientOutPeriod) {
  //     officeClientInTime = this.getFullDateTime(
  //       this.fromDate,
  //       this.selectedClientInHour,
  //       this.selectedClientInMinute,
  //       this.selectedClientInPeriod
  //     );

  //     if (this.toDate) {
  //       officeClientOutTime = this.getFullDateTime(
  //         this.toDate,
  //         this.selectedClientOutHour,
  //         this.selectedClientOutMinute,
  //         this.selectedClientOutPeriod
  //       );
  //     } else {
  //       officeClientOutTime = this.getFullDateTime(
  //         this.fromDate,
  //         this.selectedClientOutHour,
  //         this.selectedClientOutMinute,
  //         this.selectedClientOutPeriod
  //       );
  //     }
  //     this.timesheetObj.clientInTime = officeClientInTime;
  //     this.timesheetObj.clientOutTime = officeClientOutTime;
  //   }
  // }

 thisMonthValidation() {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth(); // 0-based

  const minDate = new Date(year, month - 1, 1);
  const maxDate = new Date();

  const formatDate = (date: Date): string => {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().split('T')[0];
  };

  this.minDate = formatDate(minDate);
  this.maxDate = formatDate(maxDate);

  console.log('Min Date:', this.minDate, 'Max Date:', this.maxDate);
}

  sectionViewInit() {
    if (this.userMapping.add_timesheet) {
      this.showCreateTimesheetForm();
    } else if (this.userMapping.view_my_timesheets || this.userMapping.update_timesheet) {
      this.showViewMyTimesheets()
    }
  }

  onFromDateChange(){
    this.timesheetObj.date = this.fromDate;
    this.toDate = null;
    if(this.fromDate && this.timesheetObj.isNightShift){
      const nextDate = new Date(this.fromDate);
      console.log("Next Date: ", nextDate);
    nextDate.setDate(nextDate.getDate() + 1);
    console.log("Next Date: ", nextDate);
    this.maxToDate = nextDate;
    console.log("Max To Date: ", this.maxToDate);
    this.toDate = nextDate;
    }else if(!this.timesheetObj.isNightShift){
      this.maxToDate = null;
      this.toDate = null;
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
    this.isTimesheetBulkForm = false;

    this.rawObjectUrl1 = null;
    this.previewUrl1 = null;
    this.fileType1 = null;
    this.selectedFile = null;
    this.fileName1 = null;
    this.rawObjectUrl2 = null;
    this.previewUrl2 = null;
    this.fileType2 = null;
    this.selectedFile2 = null;
    this.fileName2 = null;

    this.reset();
    this.getEmployeeBasicInfo();
    this.getAllProjectsByEmpId(this.currentUser);
  }

  showBulkUploadForm() {
    this.clientSideIdNotMandatory = true;
    this.isTimesheetForm = false;
    this.isCreation = false;

    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.isTimesheetBulkForm = true;
    this.reset();
  }

  showViewMyTimesheets() {
    this.isTimesheetTable = true;

    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isTimesheetBulkForm = false;

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
    this.selectedTimesheet = null;
    this.selectedTimesheet = Object.assign({}, timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    // this.selectedTimesheet = null;
    // this.selectedTimesheet = Object.assign({}, timesheetObj);

  }

  checkTimesheetForInActiveActivities(timesheetObj: Timesheet, template: TemplateRef<any>) {

    if (timesheetObj.dayType == "Working" && (timesheetObj.status == "Pending" || timesheetObj.status == "Rejected") && timesheetObj?.inactiveTimesheetActivities) {
      this.openInActiveUpdateConfimationModal(template, timesheetObj);
      this.previousFilledDocument = timesheetObj.filledDocument;
      this.previousApprovedDocument = timesheetObj.approvedDocument;

    } else {
      this.showUpdateTimesheetForm(timesheetObj);
      this.previousFilledDocument = timesheetObj.filledDocument;
      this.previousApprovedDocument = timesheetObj.approvedDocument;
    }
  }

  updateInactiveActivitiesTimesheet() {
    this.showUpdateTimesheetForm(this.selectedTimesheet);
  }

  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    console.log(timesheetObj)
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;
    console.log("OLD", timesheetObj);


    this.timesheetObj = Object.assign({}, timesheetObj);
    console.log(timesheetObj.docId);
    console.log(this.timesheetObj.docId);


    this.timesheetObj.updatedTimesheetActivities = [];
    this.timesheetObj.date = (this.timesheetObj.date) ? moment(timesheetObj.date, "DD-MM-YYYY").toDate() : '';
    this.fromDate = new Date(this.timesheetObj.date);
    this.timesheetObj.officeInTime = (this.timesheetObj.officeInTime) ? moment(timesheetObj.officeInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.officeOutTime = (this.timesheetObj.officeOutTime) ? moment(timesheetObj.officeOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.createdOn = (this.timesheetObj.createdOn) ? moment(timesheetObj.createdOn, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.dayType = (this.timesheetObj.dayType == "Holiday") ? "Week Off" : this.timesheetObj.dayType;
    this.timesheetObj.clientInTime = (this.timesheetObj.clientInTime) ? moment(timesheetObj.clientInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.clientOutTime = (this.timesheetObj.clientOutTime) ? moment(timesheetObj.clientOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    console.log("NEW", this.timesheetObj);

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

    this.fromDate = new Date(this.timesheetObj.date);
    this.toDate = this.timesheetObj.officeOutTime ? new Date(moment(this.timesheetObj.officeOutTime, "DD-MM-YYYY HH:mm:ss").format("YYYY-MM-DD")) : '';
    if (this.timesheetObj.officeInTime) {
      this.setTimeDropdowns(this.timesheetObj.officeInTime, 'In');
    }
    if (this.timesheetObj.officeOutTime) {
      this.setTimeDropdowns(this.timesheetObj.officeOutTime, 'Out');
    }
    if (!this.clientSideIdNotMandatory) {
      if (this.timesheetObj.clientInTime) {
        this.setTimeDropdowns(this.timesheetObj.clientInTime, 'ClientIn');
      }
      if (this.timesheetObj.clientOutTime) {
        this.setTimeDropdowns(this.timesheetObj.clientOutTime, 'ClientOut');
      }
    }
    this.onProjectSelect(timesheetObj.projectId);
    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
    setTimeout(() => {
      this.getAllMyActivitiesByTimesheetId(timesheetObj);
    }, 500)
  }


  setTimeDropdowns(dateTime: Date, type: 'In' | 'Out' | 'ClientIn' | 'ClientOut') {
    if (!dateTime) return;

    const dateObj = new Date(dateTime);
    let hour = dateObj.getHours();
    const minute = dateObj.getMinutes();
    const period = hour >= 12 ? 'PM' : 'AM';

    // Convert 24h -> 12h format
    hour = hour % 12;
    if (hour === 0) hour = 12;

    // Assign to correct dropdowns based on type
    if (type === 'In') {
      this.selectedInHour = String(hour).padStart(2, '0');
      this.selectedInMinute = String(minute).padStart(2, '0');
      this.selectedInPeriod = period;
    }
    if (type === 'Out') {
      this.selectedOutHour = String(hour).padStart(2, '0');
      this.selectedOutMinute = String(minute).padStart(2, '0');
      this.selectedOutPeriod = period;
    }
    if (type === 'ClientIn') {
      this.selectedClientInHour = String(hour).padStart(2, '0');
      this.selectedClientInMinute = String(minute).padStart(2, '0');
      this.selectedClientInPeriod = period;
    }
    if (type === 'ClientOut') {
      this.selectedClientOutHour = String(hour).padStart(2, '0');
      this.selectedClientOutMinute = String(minute).padStart(2, '0');
      this.selectedClientOutPeriod = period;
    }
  }

  reset() {
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
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
      newActivityObj.teamId = activityObj.teamId;
      //console.log("newActivityObj : ", newActivityObj);

      this.allTimesheetActivities.push(newActivityObj);
      this.getClientLocationList(newActivityObj);
      this.getProjectList(newActivityObj);
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
      console.log(this.availableTimesheets, "availableTimesheets");
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

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
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

  resetTotalWorkingClientHours(template: TemplateRef<any>) {

    if (this.timesheetObj.clientSideId) {
      if (this.timesheetObj.clientInTime) {

        const systemCurrentTime = moment();
        const userOfficeInTime = moment(this.timesheetObj.clientInTime);

        console.log("data==", userOfficeInTime);

        if (systemCurrentTime.isSame(userOfficeInTime, 'minute')) {
          const updatedInTime = userOfficeInTime.subtract(2, 'minutes').toDate();
          this.timesheetObj.clientInTime = updatedInTime;
        }
        else if (userOfficeInTime.isAfter(systemCurrentTime, 'minute') && userOfficeInTime.isSame(systemCurrentTime, 'day')) {
          const updatedInTime = userOfficeInTime.subtract(2, 'minutes').toDate();
          this.timesheetObj.clientInTime = updatedInTime;
        }
      }

      this.timesheetObj.clientOutTime = '';
      this.timesheetObj.totalClientWorkingHours = '';

      this.maxOutTimeDate = new Date(moment(this.timesheetObj.clientInTime).add(1, 'd').toString());
    }
  };


  resetTimeonDayTypeChange() {
    console.log(this.timesheetObj.dayType);
    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave" || this.timesheetObj.dayType == "Client Holiday") {
      this.timesheetObj.officeInTime = '';
      this.timesheetObj.officeOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
      this.timesheetObj.clientInTime = '';
      this.timesheetObj.clientOutTime = '';
      this.timesheetObj.totalClientWorkingHours = '';
      this.timesheetFillable = false
    }
    else {
      this.timesheetFillable = true;
      this.makeApmosysInTime();
      this.makeApmosysOutTime();
      this.makeClientInTime();
      this.makeClientOutTime();
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

  setTotalWorkingClientHours() {
    console.log(this.timesheetObj.clientSideId);
    if (this.timesheetObj.clientSideId) {
      const dateFormat = 'YYYY-MM-DD';
      const systemCurrentTime = moment();
      const userOfficeOutTime = moment(this.timesheetObj.clientOutTime);

      console.log('systemCurrentTime', systemCurrentTime);
      console.log('userOfficeOutTime', userOfficeOutTime);

      if (
        (userOfficeOutTime.isAfter(systemCurrentTime, 'minute') || userOfficeOutTime.isSame(systemCurrentTime, 'minute')) &&
        userOfficeOutTime.isSame(systemCurrentTime, 'day')
      ) {
        const updatedInTime = systemCurrentTime.subtract(1, 'minute').toDate();
        this.timesheetObj.clientOutTime = updatedInTime;

        console.log('Updated Office In Time:', updatedInTime);
      }
      if (this.timesheetObj.clientInTime && this.timesheetObj.clientOutTime) {

        let start = moment(this.timesheetObj.clientInTime).format('DD-MM-YYYY HH:mm');
        let end = moment(this.timesheetObj.clientOutTime).format('DD-MM-YYYY HH:mm');
        let ms = moment(end, "DD-MM-YYYY HH:mm").diff(moment(start, "DD-MM-YYYY HH:mm"));
        let d = moment.duration(ms);


        let duration = Math.floor(d.asHours()) + moment.utc(ms).format(":mm");

        this.timesheetObj.totalClientWorkingHours = duration;
        this.timesheetObj.date = moment(this.timesheetObj.clientInTime).format(dateFormat);


      } else {
        this.timesheetObj.totalClientWorkingHours = '';
      }
    }
  }

  /* Timesheet */
  validateTimesheetObj(timesheetObj: Timesheet, template: TemplateRef<any>) {
    console.log(timesheetObj.date,"timesheetObj.date")
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

    if (timesheetObj.dayType != "Public Holiday" && timesheetObj.dayType != "Week Off" && timesheetObj.dayType != "Leave" && this.timesheetFillable) {
      let flag = true;
      let totalActivityTime = 0;
      let totalWorkingHoursInSeconds = 0;

      let hm = (timesheetObj.totalWorkingOfficeHours) ? timesheetObj.totalWorkingOfficeHours : '00:00';
      let timeData = hm.split(':');

      // minutes are worth 60 seconds. Hours are worth 60 minutes.
      totalWorkingHoursInSeconds = (+timeData[0]) * 60 * 60 + (+timeData[1]) * 60;

      //console.log("totalWorkingHoursInSeconds : ", totalWorkingHoursInSeconds);

      // if(this.timesheetObj.hasClientSideId && !this.clientSideIdNotMandatory && this.timesheetFillable){
      //   if(this.timesheetObj.selectedFile == null){
      //     this.openAlertMod(template,"Please upload Client Side Attendance Proof!")
      //     return false;
      //   } else {
      //     this.timesheetObj.selectedFile = this.selectedFile;
      //   }
      // 
      // this.timesheetObj.clientInTime = this.timesheetObj.clientInTime ? moment(this.timesheetObj.clientInTime).isValid() ? moment(this.timesheetObj.clientInTime).format(dateTimeFormat) : null : null;
      //   this.timesheetObj.clientOutTime}
      if (!this.clientSideIdNotMandatory){
        if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.clientInTime)) {
        this.alertMessage = "Please enter Client In Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
       if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.clientOutTime)) {
        this.alertMessage = "Please enter Client Out Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      }

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
        if (!this.timesheetFillable) return;
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

    console.log("test ", this.timesheetObj.description)
    this.timesheetObj.description = this.timesheetObj.description?.trim();
    console.log("test ", this.timesheetObj.description)

    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
      //console.log("allTimesheetActivities :", this.allTimesheetActivities, this.allTimesheetActivities[0]);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    } else {
      this.timesheetObj.allTimesheetActivities = null;
    }

    if (this.timesheetObj.timesheetAppliedFor == 'asShadow') {
      this.timesheetObj.isShadowTimesheet = true;
    } else {
      this.timesheetObj.isShadowTimesheet = false;
    }

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
      if (this.timesheetObj.clientSideId) {
        this.timesheetObj.clientInTime = this.timesheetObj.clientInTime ? moment(this.timesheetObj.clientInTime).isValid() ? moment(this.timesheetObj.clientInTime).format(dateTimeFormat) : null : null;
        this.timesheetObj.clientOutTime = this.timesheetObj.clientOutTime ? moment(this.timesheetObj.clientOutTime).isValid() ? moment(this.timesheetObj.clientOutTime).format(dateTimeFormat) : null : null;
      }
    } else {
      this.timesheetObj.date = moment(this.timesheetObj.date).format(dateFormat);
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.createdByName = this.currentUser.name
    if (this.currentUser.approvalsTo == 'Reporting Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.reportingManagerId;
    } else if (this.currentUser.approvalsTo == 'Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    } else {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    }
    if (!this.timesheetFillable) {
      this.timesheetObj.clientInTime = null;
      this.timesheetObj.clientOutTime = null;
      this.timesheetObj.officeInTime = null;
      this.timesheetObj.officeOutTime = null;
    }
    if (this.clientSideIdNotMandatory) {
      this.timesheetObj.clientInTime = null;
      this.timesheetObj.clientOutTime = null;
      // this.timesheetObj.documentData.createdBy = this.currentUser.empId;
    }
    else {
      console.log(this.timesheetFillable);
      if (this.timesheetFillable) {
        this.timesheetObj.clientInTime = this.timesheetObj.clientInTime ? moment(this.timesheetObj.clientInTime).isValid() ? moment(this.timesheetObj.clientInTime).format(dateTimeFormat) : null : null;
        this.timesheetObj.clientOutTime = this.timesheetObj.clientOutTime ? moment(this.timesheetObj.clientOutTime).isValid() ? moment(this.timesheetObj.clientOutTime).format(dateTimeFormat) : null : null;
        if (this.selectedFile == null && this.timesheetObj.clientApprovalStatus == "pending") {
          this.openAlertMod(template, "Please upload valid Attendance Proof!")
          return;
        }
        else if ((this.selectedFile2 == null || this.selectedFile == null) && this.timesheetObj.clientApprovalStatus == "approved") {
          this.openAlertMod(template, "Please upload valid Attendance Proof!")
          return;
        }
        this.payloadForFileUpload();
      }
    }

    console.log("Add timesheetObj : ", this.timesheetObj);
    this.timesheetService.addTimesheetWithClient(this.timesheetObj, this.selectedFile, this.selectedFile2).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        if (this.autoFillTimesheet) {
          this.router.navigate(['/home']);
          sessionStorage.setItem('autoFillTimesheet', 'true');
        }

        this.resetTimesheetForm();
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
        this.clearPreviousSelections();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
      // location.reload();
    });
  }

  onUpdateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';
    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    console.log("test befor", this.timesheetObj);

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
      this.timesheetObj.date = moment(this.timesheetObj.officeInTime).format(dateFormat);
      this.timesheetObj.officeInTime = moment(this.timesheetObj.officeInTime).format(dateTimeFormat);
      this.timesheetObj.officeOutTime = moment(this.timesheetObj.officeOutTime).format(dateTimeFormat);
      if (this.timesheetObj.clientSideId) {
        this.timesheetObj.clientInTime = this.timesheetObj.clientInTime ? moment(this.timesheetObj.clientInTime).isValid() ? moment(this.timesheetObj.clientInTime).format(dateTimeFormat) : null : null;
        this.timesheetObj.clientOutTime = this.timesheetObj.clientOutTime ? moment(this.timesheetObj.clientOutTime).isValid() ? moment(this.timesheetObj.clientOutTime).format(dateTimeFormat) : null : null;
      }
      else{
        this.timesheetObj.clientInTime = null;
        this.timesheetObj.clientOutTime = null;
      }
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
      this.timesheetObj.clientInTime = '';
      this.timesheetObj.clientOutTime = '';
      this.timesheetObj.totalWorkingOfficeHours = '';
      this.timesheetObj.totalClientWorkingHours = '';
    }
    this.timesheetObj.createdBy = this.currentUser.empId;
    this.timesheetObj.currentManagerId = this.currentUser.managerId;
    //console.log("Update timesheetObj : ", this.timesheetObj);
    this.payloadForFileUpload();
    this.timesheetObj.documentData = [];

    if (this.selectedFile !== null && this.selectedFile != undefined) {

      let newDoc1: TimesheetDoc = {
        docId: this.previousFilledDocument,
        docName: this.fileName1,
        empId: this.timesheetObj.empId,
        clientApprovalStatus: "Pending",
        finalFlag: false
      };
      this.timesheetObj.documentData.push(newDoc1);
    }
    this.previousApprovedDocument = this.timesheetObj.approvedDocument;

    if (this.selectedFile2 !== null && this.selectedFile2 != undefined) {

      let newDoc2: TimesheetDoc = {
        docId: this.previousApprovedDocument,
        docName: this.fileName2,
        empId: this.timesheetObj.empId,
        clientApprovalStatus: "Approved",
        finalFlag: true
      };

      this.timesheetObj.documentData.push(newDoc2);

    }
    

    this.timesheetService.updateTimesheetWithClient(this.timesheetObj, this.selectedFile, this.selectedFile2).pipe(first()).subscribe({
    next:(response: any) => {
      if (response.serviceStatus == "Success") {
        this.resetTimesheetForm()
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
    },
    error: (error: any) => {
      if (error.status === 500 && error.error?.message?.includes('Malicious content in request body')) {
        this.openAlertMod(template, 'Request blocked: Malicious content detected in the request body.');
      } 
      else if (error.status === 500) {
        this.openAlertMod(template, 'Internal server error occurred. Please try again later.');
      } 
      else if (error.status === 403) {
        this.openAlertMod(template, 'You are not authorized to perform this action.');
      } 
      else if (error.status === 401) {
        this.openAlertMod(template, 'Your session has expired. Please log in again.');
        // Example: this.authService.logout();
      } 
      else {
        this.openAlertMod(template, `Unexpected error (${error.status}): ${error.message || 'Unknown error'}`);
      }
    }
  });
  }

  __tempDescription = '';
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

  getTimesheetMetadata(eventTarget?: any) {
    //console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);

    let userObj: User = new User();
    if (this.timesheetObj.timesheetAppliedFor == 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = this.currentUser.empId;
      this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      //console.log("this.currentUser  : ", this.currentUser);
      //console.log("userObj  : ", userObj);

    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      //console.log("Team Member : ", teamMember);
      userObj.empId = teamMember.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = teamMember.empId;

      if (teamMember.isTimesheetFilledByMember == "true") {
        this.openAlertMod(this.alertTemplate, "Timesheet cannot be filled for team member more than 2 days.");
        this.timesheetObj.empId = '';
        eventTarget.value = "";
        this.disableCreateUpdateTimesheet = true;
        eventTarget.value = '';
        //console.log(eventTarget.value, " : eventTarget");



      } else {
        this.disableCreateUpdateTimesheet = false;
      }
    }

    const timesheetBkp = Object.assign({}, this.timesheetObj);

    // reset timesheet
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.allTimesheetActivities = [];
    this.addInputActivityField()

    // set leave AppliedFor User data to fetch activities for project & for display
    this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
    this.timesheetObj.empId = timesheetBkp.empId;

    //console.log("preset Timesheet : ", this.timesheetObj);

    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
  }

  getAllTeamMemberList() {
    this.resetTimesheetFormForAutoFill();
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
        this.openAlertWithResetMod(this.alertModalWithoutReload, "Please contact the RMG team and set up your default project mapping!");
      }
    });
  }

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
      selectedActivityObj.teamId = '';
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

  getAllActivitiesByProjectIdandEmpId(activityObj: any) {
    let allActivityList = [];

    //console.log("Current Timesheet : ", this.timesheetObj);

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.timesheetObj.empId;
    timesheetObj.teamId = activityObj.teamId;
    //console.log(this.allProjectsList, " : all project list");
    //console.log(timesheetObj.teamId, " : timesheetObj.teamId");


    let projectTimesheet = this.allProjectsList.find(project => project.teamId == timesheetObj.teamId);
    //console.log(" projectTimesheet  :  ", projectTimesheet)


    timesheetObj.projectId = projectTimesheet.projectId;
    timesheetObj.clientId = this.timesheetObj.clientId;
    timesheetObj.clientLocationId = this.timesheetObj.clientLocationId;
    //console.log(" timesheetObj  :  ", timesheetObj)

    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
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
          if (allActivityList.length === 0) {
            this.openAlertMod(this.alertModalWithoutReload, "No activity found for your department!");
          }
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
    console.log("this.timesheetObj", this.timesheetObj);
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
            if (timesheet.clientSideId) {
              timesheet.clientInTime = (timesheet.clientInTime) ? moment(timesheet.clientInTime).format(AppComponent.DATETIME_FORMAT) : null;
              timesheet.clientOutTime = (timesheet.clientOutTime) ? moment(timesheet.clientOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            }
          });
          console.log("allMyTimesheets :", this.allMyTimesheets);
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
        console.log("allTimesheetActivities :", this.allTimesheetActivities);
        const allActivities = [...this.allTimesheetActivities];
        let inactiveActivities: any[] = timesheet.inactiveTimesheetActivities;

        // Removing Inactive Activities from AllTimesheetActivities and added in UpdatedTimesheetActivities
        if (inactiveActivities != null) {
          allActivities.forEach(activityObj => {
            if (inactiveActivities.find(activity => activity.timesheetActivityMapId == activityObj.timesheetActivityMapId)) this.removeInputActivityField(activityObj)
          });
        }
        console.log(this.allTimesheetActivities, "this.allTimesheetActivities");
        console.log(inactiveActivities, "this.inactiveActivities");
        console.log(timesheetObj.hasClientSideId, "timesheetObj.hasClientSideId");
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
    else{
      this.toDate = null;
      this.makeApmosysInTime();
      if(!this.clientSideIdNotMandatory){
        this.makeClientInTime();
      }
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

    let content = "Employment ID: A-" + this.currentUser.employeementId
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

  validateDescription(event: any, activityObj: any): void {
    const input = event.target.value;
    const sanitizedValue = this.inputValidationService.validateInput(input, 'Description');

    activityObj.description = sanitizedValue;
    event.target.value = sanitizedValue; // reflect the change in the UI
  }
  
  validateTime(event, data: any) {
    if (!this.validationService.validateTimesheetCompletionTime(data)) {
      this.errorMsg = "Please enter Time !!"
    } else if (!this.validationService.validateExperiencedNumber(data)) {
      this.errorMsg = "Please enter Valid Time !!"
    }
    else if(data > this.timesheetObj.totalWorkingOfficeHours && (this.timesheetObj.totalClientWorkingHours == null || this.timesheetObj.totalClientWorkingHours == '')){
            this.errorMsg = "Please enter Valid Time !!"
    }
    else if (this.timesheetObj.totalClientWorkingHours == null && this.timesheetObj.totalClientWorkingHours == '' && data > this.timesheetObj.totalClientWorkingHours){
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

  getActiveProjectsByEmpId() {
    this.timesheetService.getActiveProjectsByEmpId(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.activeProjectList = response.serviceResponse;
        console.log("Active Project List :::::::::", this.activeProjectList);

        this.activeProjectList.forEach(project => {
          if (project.projectId) {
            this.onProjectSelect(project.projectId);
          }
        });
      } else {
        console.error("Service Response for this.activeProjectList :::::::", response.serviceResponse);
      }
    });
  }

  isEmployeeInTNMProject(){
     this.timesheetService.isEmployeeInTNMProject(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeInTNMProject = (response.serviceResponse === true || response.serviceResponse === 'true');
      } else {
        console.error("err while checking if employee is in any TNM Project", response.serviceResponse);
      }
    });
  }




  openPolicySidebar() {
    this.isPolicySidebarOpen = true
  }

  closePolicySidebar() {
    this.isPolicySidebarOpen = false
  }

  toggleAccordion(section: string) {
    this.expandedSection = this.expandedSection === section ? "" : section
  }




  getDoscForPreview(docId: any) {
    console.log(docId, ":docId");
    this.timesheetService.getDocumentDataByDocId(docId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log(response.serviceResponse);
        this.docData2 = response.serviceResponse.docData;
        console.log(typeof (this.docData2), ":docDataType")
        this.mimeType = response.serviceResponse.docMimeType
        this.showPreview(this.docData2, this.mimeType)
      }
    });
  }
  formatDateToLocalYMD(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // month is 0-based
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  bulkFinalDocumentUpload(template?: TemplateRef<any>) {
    this.finalToDate = this.finalToDate instanceof Date
      ? this.formatDateToLocalYMD(this.finalToDate)
      : this.finalToDate;
    this.finalFromDate = this.finalFromDate instanceof Date
      ? this.formatDateToLocalYMD(this.finalFromDate)
      : this.finalFromDate;
    console.log(this.currentUser.empId)
    console.log(this.finalFromDate);
    console.log(this.finalToDate);
    console.log(this.timesheetObj.projectId);
    if (this.selectedFile2 != null && this.finalFromDate != null && this.finalToDate != null && this.currentUser.empId != null) {
      this.timesheetService.bulkFinalDocumentUpload(this.selectedFile2, this.finalFromDate, this.finalToDate, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.timesheetObj.projectId = null;
          this.selectedFile2 = null;
          this.finalFromDate = '';
          this.finalToDate = '';
          this.fileName2 = '';
          this.fileType2 = '';
          this.previewUrl2 = '';
          if (this.fileInput) {
            this.fileInput.nativeElement.value = '';
          }

          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    } else {

      if (this.finalFromDate == null) {
        this.openAlertMod(template, "Select from date..!!");
      }
      else if (this.finalToDate == null) {
        this.openAlertMod(template, "Select to date..!!");
      }
      else if (this.selectedFile2 == null) {
        this.openAlertMod(template, "File not provided..!!");
      } else {
        this.openAlertMod(template, "Employee Id is null. Please contact HR...!!");
      }
    }
  }
  showPreview(base64Data: string, mimeType: string) {
    const dataUrl = `data:${mimeType};base64,${base64Data}`;
    this.activePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.activeFileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.activeFileType = 'image';
    } else {
      this.activeFileType = '';
    }


    // Open modal
    this.modalRef = this.modalService.show(this.previewModal, { class: 'modal-lg' });
  }

  openPreviewModal() {
    this.modalRef = this.modalService.show(this.previewModal, { class: 'modal-lg' });
  }

  openPreviewModalForTwo(docType: 'doc1' | 'doc2'): void {
    this.activePreviewUrl = docType === 'doc1' ? this.previewUrl1 : this.previewUrl2;
    this.activeFileType = docType === 'doc1'
      ? (this.selectedFile?.type === 'application/pdf' ? 'pdf' : 'image')
      : (this.selectedFile2?.type === 'application/pdf' ? 'pdf' : 'image');

    this.modalRef = this.modalService.show(this.previewModal, { class: 'modal-lg' });
  }
  onFileSelected(event: any, docType: 'doc1' | 'doc2'): void {
    const file: File = event.target.files[0];
    if (!file) return;

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSize = 500 * 1024; // 500kB

    if (!allowedTypes.includes(file.type)) {
      if (docType === 'doc1') this.fileError1 = 'Only PDF, JPG, JPEG, PNG files allowed.';
      else this.fileError2 = 'Only PDF, JPG, JPEG, PNG files allowed.';
      return;
    }
    if (file.size > maxSize) {
      if (docType === 'doc1') this.fileError1 = 'File size must be 500KB or less.';
      else this.fileError2 = 'File size must be 500KB or less.';
      return;
    }
    if (docType === 'doc1' && this.rawObjectUrl1) URL.revokeObjectURL(this.rawObjectUrl1);
    if (docType === 'doc2' && this.rawObjectUrl2) URL.revokeObjectURL(this.rawObjectUrl2);

    // if (!allowedTypes.includes(file.type)) {
    //   this.fileError = 'Only PDF, JPG, JPEG, and PNG files are allowed.';
    //   return;
    // }

    // if (file.size > maxSize) {
    //   this.fileError = 'File size must be 1MB or less.';
    //   return;
    // }

    if (file.size > maxSize) {
      if (docType === 'doc1') {
        this.fileError1 = 'File size must be 500KB or less.';
        this.openAlertMod(this.alertTemplate, this.fileError1);
        this.selectedFile = null;
        this.fileName1 = '';
        this.previewUrl1 = null;
        this.rawObjectUrl1 = null;
        this.fileType1 = null;
      }
      if (docType === 'doc2') {
        this.fileError2 = 'File size must be 500KB or less.';
        this.openAlertMod(this.alertTemplate, this.fileError2);
        this.selectedFile2 = null;
        this.fileName2 = '';
        this.previewUrl2 = null;
        this.rawObjectUrl2 = null;
        this.fileType2 = null;
      }
      return;
    }
    const objectUrl = URL.createObjectURL(file);
    const previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
    const fileType = file.type === 'application/pdf' ? 'pdf' : 'image';

    if (docType === 'doc1') {
      this.selectedFile = file;
      this.fileName1 = file.name;
      this.previewUrl1 = previewUrl;
      this.rawObjectUrl1 = objectUrl;
      this.fileError1 = '';
    } else {
      this.selectedFile2 = file;
      this.fileName2 = file.name;
      this.previewUrl2 = previewUrl;
      this.rawObjectUrl2 = objectUrl;
      this.fileError2 = '';
    }
  }

  onFinalFileSelected(event: any): void {
    const file: File = event.target.files[0];
    this.fileError2 = '';
    this.previewUrl2 = null;
    this.fileType2 = null;

    if (!file) return;

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSize = 500 * 1024;

    if (!allowedTypes.includes(file.type)) {
      this.fileError2 = 'Only PDF, JPG, JPEG, and PNG files are allowed.';
      return;
    }

    if (file.size > maxSize) {
      this.fileError2 = 'File size must be 500Kb or less.';
      return;
    }

    if (this.rawObjectUrl2) {
      URL.revokeObjectURL(this.rawObjectUrl2);
    }

    const objectUrl = URL.createObjectURL(file);
    this.rawObjectUrl2 = objectUrl;
    this.previewUrl2 = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
    this.fileType2 = file.type === 'application/pdf' ? 'pdf' : 'image';
    this.selectedFile2 = file;
    this.fileName2 = file.name;
    console.log(this.selectedFile2, "::this.selectedFile", this.fileName2, "::this.fileName")
  }


  clearPreviousSelections() {
    this.selectedProjectId = null;
  }

  openSelfModal3(template: TemplateRef<any>) {
    this.empClientSideObj.clientSideId = '';
    this.empClientSideObj.projectId = this.timesheetObj.projectId;
    this.updateClientIdModalRef = this.modalService.show(template, { class: 'modal-lg' });
    this.getActiveProjectsAndClientSideIdByEmpId();
  }

  hideSelfModal3(): void {
    if (this.updateClientIdModalRef) {
      this.updateClientIdModalRef.hide();
    }
  }

  fetchEmploymentIdByEmpId() {
    this.timesheetService.fetchEmploymentIdByEmpId(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.employmentId = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getEmployeeListByProjectId(projectId) {
    this.timesheetService.getEmployeeListByProjectId(projectId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.getTimesheetMetadata();
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  updateClientSideIdMapping(template: TemplateRef<any>) {
    this.empClientSideObj.empId = this.currentUser.empId;
    this.timesheetService.updateClientSideIdMapping(this.empClientSideObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.currentUser.empId);
      } else {
        this.openAlertMod(template, response.serviceResponse)
      }
    });
    this.resetUpdateClientSideId();
  }

  onProjectChange(projId: any): void {
    this.getClientSideIdByProjectIdAndEmpId(projId, this.currentUser.empId);
  }

  resetUpdateClientSideId() {
    this.empClientSideObj = new EmployeeClientSideIdMapping();
  }

  getActiveProjectsAndClientSideIdByEmpId() {
    var empId: any;
    if (this.timesheetObj.timesheetAppliedFor == 'team') {
      empId = this.timesheetObj.empId
    } else {
      empId = this.currentUser.empId
    }
    this.timesheetService.getActiveProjectsAndClientSideIdByEmpId(empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectClientIdList = response.serviceResponse;
        if (this.projectClientIdList) {
          const matchedProject = this.projectClientIdList.find(p => p.projectId === this.empClientSideObj.projectId);
          if (matchedProject) {
            this.empClientSideObj.clientSideId = matchedProject.clientSideId;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  payloadForFileUpload() {

    this.timesheetObj.documentData = [];

    if (this.selectedFile !== null && this.selectedFile != undefined) {
      let newDoc1: TimesheetDoc = {
        docId: this.timesheetObj.docId ?? null,
        docName: this.fileName1,
        empId: this.timesheetObj.empId,
        clientApprovalStatus: "Pending",
        finalFlag: false
      };
      this.timesheetObj.documentData.push(newDoc1);
    }
    if (this.selectedFile2 !== null && this.selectedFile2 != undefined) {
      let newDoc2: TimesheetDoc = {
        docId: this.timesheetObj.docId ?? null,
        docName: this.fileName2,
        empId: this.timesheetObj.empId,
        clientApprovalStatus: this.timesheetObj.clientApprovalStatus,
        finalFlag: this.timesheetObj.clientApprovalStatus === 'approved'
      };
      this.timesheetObj.documentData.push(newDoc2);
    }

  }

  // getClientSideIdByProjectId(projectId:any){
  //   if(this.timesheetObj.shadowEmpId == this.currentUser.empId){
  //     this.shadowForSelf = true;
  //   }
  //   this.timesheetService.getClientSideIdByProjectId(projectId).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.timesheetObj.clientSideId = response.serviceResponse;
  //       if(this.updateExistingClientSideId){
  //         this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;
  //       }
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  openNoNotAppliedYet(template: TemplateRef<any>) {
    this.noNotAppliedYetModalRef = this.modalService.show(template, { class: 'modal-md' });
  }

  hideNoNotAppliedYet(): void {
    if (this.noNotAppliedYetModalRef) {
      this.noNotAppliedYetModalRef.hide();
      this.resetTimesheetForm();
    }
  }

  onClientApprovalStatusChange(event: any, template: TemplateRef<any>): void {
    const selectedValue = event.target.value;
    if (selectedValue === 'no') {
      // this.resetTimesheetForm();
      this.openNoNotAppliedYet(template);
    }

  }

  checkClientSideIdPresentOrNot(timesheetObj: Timesheet) {
    if (timesheetObj.clientSideId == null || timesheetObj.clientSideId == '')
      this.openclientSideIdNotMandatoryFound(this.clientSideIdNotMandatoryFound);
  }

  openclientSideIdNotMandatoryFound(template: TemplateRef<any>) {
    this.clientSideIdNotMandatoryFoundModalRef = this.modalService.show(template, { class: 'modal-md' });
  }

  hideclientSideIdNotMandatoryFound(): void {
    if (this.clientSideIdNotMandatoryFoundModalRef) {
      this.clientSideIdNotMandatoryFoundModalRef.hide();
    }
  }

  getProjectName(projectId: number): string {
    const project = this.projectClientIdList?.find(p => p.projectId === projectId);
    return project ? project.projectName : '';
  }

  hideClientSideIdForm() {
    this.clientSideIdForm.hide();
  }

  onCancelClientSideId(template: TemplateRef<any>) {
    this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.currentUser.empId);
    this.hideClientSideIdForm();
    this.openclientSideIdNotMandatoryFound(template);
  }

  hideNoClientSideIdProvided() {
    this.noClientSideIdProvided.hide();
    this.resetTimesheetForm();
  }

  onProjectSelect(projectId: any) {
    if (this.timesheetObj.timesheetAppliedFor == "asShadow") {
      this.getEmployeeListByProjectId(projectId)
    } else {
      this.checkIfProjectRequiresClientId(projectId);
    }
  }

  onProjectSelectBulk(projectId: any) {

    this.checkIfProjectRequiresClientId(projectId);
    this.getAllDisabledDateListForBulkDocSubmit(projectId);

  }

  getAllDisabledDateListForBulkDocSubmit(projectId: any) {
    if (projectId != null) {
      this.timesheetService.getAllDisabledDateListForBulkDocSubmit(projectId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.disableList = response.serviceResponse;
          this.disableListFormatted = this.disableList.map(d => new Date(d));
        }
      });
    }
    else {

    }

  }

  disableDates = (date: Date | null): boolean => {
    if (!date) return true;

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const day = String(date.getDate()).padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;

    // Disable if the formatted date exists in disableList
    return !this.disableList.includes(formattedDate);
  };

  checkIfProjectRequiresClientId(projectId: any) {
    this.timesheetService.checkIfProjectRequiresClientId(projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequiresClientId = response.serviceResponse;
        if (this.projectRequiresClientId) {
          this.clientSideIdNotMandatory = false;
          this.timesheetObj.clientSideId = null;
          this.timesheetObj.hasClientSideId = true;
          this.clientIdNeeded = true;
          this.onProjectRequiresClientId(projectId, this.currentUser.empId);
        } else {
          this.fetchEmploymentIdByEmpId();
          this.clientIdNeeded = false;
          this.clientSideIdNotMandatory = true;
          this.timesheetObj.hasClientSideId = false;
          // this.timesheetObj.clientSideId = false;
        }
        console.log(this.clientSideIdNotMandatory, "::clientSideIdNotMandatory");
      } else {
        console.error(response.serviceResponse);
        this.fetchEmploymentIdByEmpId();
        this.clientSideIdNotMandatory = true;
      }
    });
  }

  onProjectRequiresClientId(projectId: any, empId: any) {

    this.timesheetObj.clientSideId == null;
    if (this.timesheetObj.timesheetAppliedFor == 'team') {
      this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.timesheetObj.empId);
      if (this.timesheetObj.clientSideId == null && this.projectRequiresClientId) {
        this.getActiveProjectsAndClientSideIdByEmpId();
        this.empClientSideObj.projectId = projectId;
        this.openClientSideIdForm();
      }
    } else {
      this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.timesheetObj.clientSideId = response.serviceResponse;
          if (this.timesheetObj.clientSideId) {
            this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;
            this.setTotalWorkingClientHours();
          }
        } else {
          console.error(response.serviceResponse);
        }
        if (this.timesheetObj.clientSideId == null && this.projectRequiresClientId) {
          this.getActiveProjectsAndClientSideIdByEmpId();
          this.empClientSideObj.projectId = projectId;
          this.openClientSideIdForm();
        }
      });
    }
  }

  openClientSideIdForm() {
    this.empClientSideObj.clientSideId = '';
    this.clientSideIdForm = this.modalService.show(this.clientSideIdFormRef, { class: 'modal-lg' });
  }

  getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any) {
    this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.clientSideId = response.serviceResponse;
        if (this.timesheetObj.clientSideId) {
          this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  resetTimesheetForm(){
    this.timeReset();
    this.fromDate = null;
    this.toDate = null;
    this.timesheetObj.projectId = null;
    this.timesheetObj.clientSideId = null;
    this.timesheetObj.hasClientSideId = false;
    this.timesheetObj.shadowEmpId = null;
    this.timesheetObj.timesheetAppliedFor = null;
    // this.timesheetObj.empId = null;
    // this.timesheetObj.employmentId = null;
    this.timesheetObj.clientApprovalStatus = null;
    this.timesheetObj.dayType = null;
    this.timesheetObj.date = null;
    this.timesheetObj.description = null;
    this.timesheetObj.officeInTime = null;
    this.timesheetObj.officeOutTime = null;
    this.timesheetObj.totalWorkingOfficeHours = null;
    this.timesheetObj.isNightShift = null;
    this.timesheetObj.clientInTime = null;
    this.timesheetObj.clientOutTime = null;
    this.timesheetObj.totalClientWorkingHours = null;
    this.timesheetObj.docId = null;
    // this.allTimesheetActivities = [];
  }

  // resetTimesheetForm() {
  //   this.timesheetObj.projectId = null;
  //   this.timesheetObj.clientSideId = null;
  //   this.timesheetObj.hasClientSideId = false;
  //   this.timesheetObj.shadowEmpId = '';
  //   this.timesheetObj.timesheetAppliedFor = '';
  //   this.timesheetObj.empId = '';
  //   this.timesheetObj.employmentId = '';
  //   this.timesheetObj.clientApprovalStatus = '';
  //   this.timesheetObj.dayType = '';
  //   this.timesheetObj.date = '';
  //   this.timesheetObj.description = '';
  //   this.timesheetObj.officeInTime = '';
  //   this.timesheetObj.officeOutTime = '';
  //   this.timesheetObj.totalWorkingOfficeHours = '';
  //   this.timesheetObj.isNightShift = '';
  //   this.timesheetObj.clientInTime = '';
  //   this.timesheetObj.clientOutTime = '';
  //   this.timesheetObj.totalClientWorkingHours = '';
  //   this.timesheetObj.docId = '';
  //   this.allTimesheetActivities = [];
  // }


  resetTimesheetFormForAutoFill() {
    this.timeReset();
    this.fromDate = null;
    this.toDate = null;
    this.timesheetObj.projectId = '';
    this.timesheetObj.clientSideId = '';
    this.timesheetObj.hasClientSideId = false;
    this.timesheetObj.shadowEmpId = '';
    // this.timesheetObj.timesheetAppliedFor = '';
    // this.timesheetObj.empId = '';
    this.timesheetObj.employmentId = '';
    this.timesheetObj.clientApprovalStatus = '';
    this.timesheetObj.dayType = '';
    this.timesheetObj.date = '';
    this.timesheetObj.description = '';
    this.timesheetObj.officeInTime = '';
    this.timesheetObj.officeOutTime = '';
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.timesheetObj.isNightShift = '';
    this.timesheetObj.clientInTime = '';
    this.timesheetObj.clientOutTime = '';
    this.timesheetObj.totalClientWorkingHours = '';
    this.timesheetObj.docId = '';
    // this.allTimesheetActivities = [];
  }










  openNoClientSideIdProvided(template: TemplateRef<any>) {
    if (this.timesheetObj.clientSideId.length == 0) {
      this.noClientSideIdProvided = this.modalService.show(template, { class: 'modal-sm' });
    }
  }

  openAlertWithResetMod(template: TemplateRef<any>, message: any) {
    this.alertWithResetModRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest2() {
    this.alertWithResetModRef.hide();
    this.resetTimesheetForm();
  }

  timeReset() {
    this.selectedInHour = null;
    this.selectedInMinute = null;
    this.selectedInPeriod = null;
    this.selectedOutHour = null;
    this.selectedOutMinute = null;
    this.selectedOutPeriod = null;
    this.selectedClientInHour = null;
    this.selectedClientInMinute = null;
    this.selectedClientInPeriod = null;
    this.selectedClientOutHour = null;
    this.selectedClientOutMinute = null;
    this.selectedClientOutPeriod = null;
  }


  loadAutofillData() {
    this.isUpdation = false;
    this.isTimesheetForm = true;
    this.timesheetObj.timesheetAppliedFor = 'self';
    this.timesheetObj.dayType = 'Working';
    this.fromDate = this.selectedDate;
    this.makeApmosysInTime();
    this.makeApmosysOutTime();
    this.makeClientInTime();
    this.makeClientOutTime();
    this.getAllProjectsByEmpId(this.currentUser);
    this.getActiveProjectsByEmpId();

    let timesheet: Partial<Timesheet> = { empId: this.currentUser.empId };

    this.timesheetService.getLastFilledTimesheetByEmp(timesheet)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          const autoData = response.serviceResponse[0];
          console.log("autoFillTimesheet: " + JSON.stringify(autoData));

          const lockDate = new Date(autoData.timesheetLockUpdatedOn);
          const selectedDate = new Date(this.selectedDate!);
          const lockCheckEnable = autoData.istimesheetLockCheckEnable;


          if (lockCheckEnable === "true" && lockDate > selectedDate) {
             const formattedDate = lockDate.toLocaleDateString("en-GB", {
              day: "2-digit",
               month: "2-digit",
               year: "numeric"
             });
            this.openAlertWithResetMod(
              this.alertModalWithoutReload,
              "Timesheet is locked upto " + formattedDate
            );
            return;
          }



          if (this.activeProjectList?.some(p => p.projectId === autoData.projectId)) {
            this.timesheetObj.projectId = autoData.projectId;
            this.checkIfProjectRequiresClientId(this.timesheetObj.projectId);
          }
          this.timesheetObj.clientApprovalStatus = autoData.clientApprovalStatus;


          setTimeout(() => {
            const defaultClient = this.clientList?.find(c => c.clientId === autoData.clientId);
            if (!defaultClient) {
              console.error("Client not found in list");
                this.openAlertWithResetMod(
              this.alertModalWithoutReload,
              "Client not found in list " 
            );
              return;
            }

            this.allTimesheetActivities.forEach(activityObj => {
              activityObj.clientId = defaultClient.clientId;
              this.getClientLocationList(activityObj);
            });

            setTimeout(() => {
              this.allTimesheetActivities.forEach(activityObj => {
                activityObj.clientLocationId = autoData.clientLocationID;
                this.getProjectList(activityObj);
              });


              setTimeout(() => {
                this.allTimesheetActivities.forEach(activityObj => {
                  activityObj.projectId = autoData.projectId;

                  if (activityObj.projectList?.some(t => t.teamId === autoData.teamId)) {
                    activityObj.teamId = autoData.teamId;
                    this.getAllActivitiesByProjectIdandEmpId(activityObj);
                  }
                });


                setTimeout(() => {
                  this.allTimesheetActivities.forEach(activityObj => {
                    if (activityObj.projectActivities?.some(a => a.activityId === autoData.activityID)) {
                      activityObj.activityId = autoData.activityID;
                      activityObj.activity = autoData.activity;
                      activityObj.description = autoData.description || '';
                      activityObj.completionTime = autoData.completionTime;
                    }
                  });
                  this.autoFillTimesheet = true;
                }, 200);

              }, 200);

            }, 200);

          }, 200);


        } else if (!response.serviceResponse || response.serviceResponse.length < 1) {
          this.openAlertWithResetMod(
            this.alertModalWithoutReload,
            "No timesheet found"
          );
          return;
        }

         else {
          console.error("No autofill data found:", response.serviceResponse);
           this.openAlertWithResetMod(
              this.alertModalWithoutReload,
              "Something went wrong " + response.serviceMessage
            );
            return;

        }
      });
  }


  syncTimes = false;

onSyncToggle() {
  if (this.syncTimes) {
    // Copy ApMoSys In time to Client In time
    this.selectedClientInHour = this.selectedInHour;
    this.selectedClientInMinute = this.selectedInMinute;
    this.selectedClientInPeriod = this.selectedInPeriod;

    this.selectedClientOutHour = this.selectedOutHour;
    this.selectedClientOutMinute = this.selectedOutMinute;
    this.selectedClientOutPeriod = this.selectedOutPeriod;

    this.makeClientInTime();
    this.makeClientOutTime();
  }
}




}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}