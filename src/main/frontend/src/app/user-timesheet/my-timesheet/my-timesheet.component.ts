import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
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
import { DateTimePickerComponent } from 'src/app/helpers/date-time-picker/date-time-picker.component';
import { ProjectEntry } from 'src/app/models/projectEntry';
import { ActivityNew } from 'src/app/models/activityNew';
import { TimesheetNewService } from 'src/app/services/timesheet-new.service';
import { TimesheetFormComponent } from './timesheet-form/timesheet-form.component';
import { ProjectBasedBulkUploadPayload } from '../team-timesheet/types';

@Component({
  standalone: false,
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

  @ViewChild("previewRulesInfoModal")
   previewRulesInfoModal: TemplateRef<any>;

  @ViewChild("night_shift_template")
  night_shift_template: TemplateRef<any>;

  @ViewChild("update_timesheet_template")
  update_timesheet_template: TemplateRef<any>;

  @ViewChild("noNotAppliedYet")
  noNotAppliedYet: TemplateRef<any>;

  @ViewChild(TimesheetFormComponent)
  timesheetFormComponent!: TimesheetFormComponent;
  // Property aliases for template references used in HTML
  get alert_message(): TemplateRef<any> {
    return this.alertTemplate;
  }

  get update_clientId(): TemplateRef<any> {
    return this.updateClientId;
  }

   rulesInfoModalRef:NgbModalRef;
    rulesInfopreviewFileName:any;
  rulesfileType:any;
  rulespreviewUrl:any;

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
  withVms: boolean = false;

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
  modalRef:NgbModalRef;

  //Obj
  timesheetObj: Timesheet = new Timesheet();
  allTimesheetActivities: any[] = []
  allProjectsList: any[] = [];
  allActivityList: any[] = [];

  allMyTimesheets: any[] = [];
  timesheetActivities: any[] = [];
  startDate: any;
  endDate: any;
  
  // Hierarchical view expansion state
  expandedTimesheets: Set<number> = new Set(); // timesheetId
  expandedLocations: Map<string, Set<number>> = new Map(); // "timesheetId" -> Set<locationMappingId>
  expandedProjects: Map<string, Set<number>> = new Map(); // "timesheetId_locationId" -> Set<projectId>
  isPolicySidebarOpen = false;
  expandedSection = "attendance";

  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  availableTimesheetDates: any[] = [];
  availableTimesheets: any[] = [];

  projectList: any[] = [];
  clientList: any[] = [];
  filteredClients: any[] = [];
  clientLocationList: any[] = [];
  // teamList:any[] = [];

  teamMemberList: any[] = [];
  errorMsg: any;
  serverDate: any;
  showBulkButtonFlag: boolean = false;;

  leaveHistoryList: any[] = [];
  maxOutTimeDate: any;
  disableCreateUpdateTimesheet: boolean = false;
  isAutoFilled: boolean = false;
  selectedDate: Date | undefined;

  isTimesheetLockCheckEnable: any = "true";
  employeeInTNMProject: boolean = false;
  maxMonth: string;


  withVmsbullet:string[] = ["Applicable to resources working on projects with a client-side VMS system.",
"Daily timesheets must be filled directly in the client’s VMS system.",
"Ensure entries are accurate and complete for the entire month.",
"At month-end, submit the VMS timesheet for client-side manager approval.",
"After approval, download the approved VMS timesheet (PDF) or capture a screenshot.",
"Upload the approved document via Bulk Upload as proof of attendance and client approval."];

withoutVmsbullet:string[] = ["Applicable to resources without a client-side VMS system.",
"Maintain the daily timesheet in the prescribed Excel format.",
"Capture screenshots of the filled Excel timesheet as supporting evidence.",
"At month-end, email the timesheet to the client-side manager for approval.",
"Obtain email approval from the client-side manager (as per the defined email structure).",
"Upload the approval email screenshot/PDF file via Bulk Upload as proof of attendance and client approval."];




  selectedTimesheet: any;
  selectedTimesheetId: number | null = null; // For update timesheet flow
  today = new Date().toISOString().split('T')[0];

  filters: any = {};
  isSearchEnabled: boolean = false;
  // Simplified columns for card-based accordion view
  selfTimesheetColumns: any[] = ['blank', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'status', 'createdByName', 'createdOn', 'isNightShiftDisplay', 'leaveType','rejectReason' ,'remarks'];
  teamTimesheetColumns: any[] = ['blank', 'employeeName', 'date', 'dayType', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'status', 'createdOn', 'isNightShiftDisplay', 'leaveType', 'rejectReason','remarks'];
  tableName: string;
  activeProjectList: Project[];
  selectedProjectId: any;
  // selfClientIdModalRef:NgbModalRef;
  selfClientIdUpdateModalRef:NgbModalRef;
  updateClientIdModalRef:NgbModalRef;
  noNotAppliedYetModalRef:NgbModalRef;
  clientSideIdNotMandatoryFoundModalRef:NgbModalRef;
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
fileType1: '' | 'pdf' | 'image' | 'excel' | null = null;
fileType2: '' | 'pdf' | 'image' | 'excel' | null = null;
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
  projAlertRequireClientId: Boolean = false;
  fromDate: any = null;
  toDate: any = null;
  finalFromDate: any = null;
  finalToDate: any = null;
  clientSideIdForm:NgbModalRef;
  noClientSideIdProvided:NgbModalRef;
  @ViewChild("update_clientId")
  updateClientId: TemplateRef<any>;
  @ViewChild("clientSideIdForm")
  clientSideIdFormRef: TemplateRef<any>;
  alertWithResetModRef:NgbModalRef;
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
  clientIdNeeded: boolean =false;
  autoFillTimesheet:boolean = false;
  docRequiredForShadow :boolean = true;

  isLastDayOfMonth: boolean = false;

  isUploadAllowed: boolean = false;
  disableUploadTooltip = "Bulk upload is permitted only for the complete previous month or on the last day of the current month . Please select a date range that falls entirely within the allowed period to enable uploading. ";
  clientDetails: any; 
  clientDropdownList: any[] = []; 
  projectsInMonthYear: any[] = []; 
  clientIdEntryBulletPoints: string[] = ["Mandatory field for all resources while filling the timesheet.",
"Enter the client-side ID if already available.",
"If the client-side ID is not yet assigned, enter “NA (ApMoSys Employee ID)”.",
"Once the client-side ID is received, update the ID while filling subsequent timesheets."]
  zoomScale = 1;
  zoomLevel = 100;
  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
  @ViewChild("noOtherShadowResource")
  noOtherShadowResourceTemp: TemplateRef<any>;
  noOtherShadowResourceModalRef:NgbModalRef;

 
  //latestProjectId = this.activeProjectList

  constructor(
    private validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
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
        this.selectedDate = new Date(params['date']);
      }
      // if (this.isAutoFilled) {
      //   this.loadAutofillData()
      // }
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
    this.thisMonthValidation();
    this.setStartDateMinMax();
    this.showBulkButton();
    // this.setStartDateMinMax();
    this.timeReset();
    this.timesheetFillable = true;
    // this.makeApmosysInTime();
    // this.makeApmosysOutTime();
    // this.setTotalWorkingOfficeHours();
    // this.setTotalWorkingClientHours()
    // this.checkUploadEligibility();
    this.isFullMonthSelected();

  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  onFormTypeSelect(formType:string){
    if(formType == 'createTimesheet'){
      this.showCreateTimesheetForm();
    }else if(formType =='bulkUpload'){
      this.resetTimesheetForm();
      this.showBulkUploadForm();
    }else if(formType =='viewMyTimesheet'){
      this.resetTimesheetForm(); 
      this.showViewMyTimesheets()
    }
  }

//   openUserManualPdf(): void {
//   const pdfPath = 'assets/pdfFiles/Ishine_Timesheet_TNM.pdf';

//   this.rulesInfopreviewFileName = 'Timesheet User-Manual (TNM)';
//   this.rulesfileType = 'pdf';
//   this.mimeType = 'application/pdf';

//   this.rulespreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(pdfPath);

//   this.rulesInfoModalRef = this.modalService.open(this.previewRulesInfoModal,{ class: 'modal-xl modal-dialog-centered' });
// }


openUserManualPdf(): void {
  const pdfPath = 'assets/pdfFiles/Ishine_Timesheet_TNM.pdf';
  window.open(pdfPath, '_blank');
}

get tooltipContent(): string[] {
  return this.withVms
    ? this.withVmsbullet
    : this.withoutVmsbullet ;
}

get tooltipPdf(): string {
  return this.withVms
    ? 'assets/pdfFiles/Client side VMS.pdf'
    : 'assets/pdfFiles/No Client side VMS .pdf';
}

get tooltipCta(): string {
  return this.withVms
    ? 'View VMS Guide'
    : 'View Non-VMS Guide';
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
    nextDate.setDate(nextDate.getDate() + 1);
    nextDate.setHours(0, 0, 0, 0);
    this.maxToDate = nextDate;
    this.toDate = nextDate;
    }else if(!this.timesheetObj.isNightShift){
      this.maxToDate = null;
      this.toDate = null;
    }
    this.timesheetToDateFilter = this.timesheetToDateFilter.bind(this);
    this.makeApmosysInTime();
    this.makeApmosysOutTime();
    this.makeClientInTime();
    this.makeClientOutTime();
    this.getProjectListForDateAndEmpId();
    this.resetTimesheetFormOnDateChange();
    console.log("after method calls ", this.fromDate);
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
    // this.getAllProjectsByEmpId(this.currentUser);
  }

  showBulkUploadForm() {
    this.clientSideIdNotMandatory = true;
    this.isTimesheetForm = false;
    this.isCreation = false;

    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.isTimesheetBulkForm = true;
    console.log("Bulk Upload Form",this.isTimesheetBulkForm);
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    this.maxMonth = `${year}-${month}`;
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


  showBulkButton(){
     this.timesheetService.wasEmployeeInClientProjCurrAndPrevMon(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showBulkButtonFlag = response.serviceResponse;
        this.clientIdNeeded = this.showBulkButtonFlag;
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  openInActiveUpdateConfimationModal(template: TemplateRef<any>, timesheetObj: Timesheet,) {
    this.selectedTimesheet = null;
    this.selectedTimesheet = Object.assign({}, timesheetObj);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
    // this.selectedTimesheet = null;
    // this.selectedTimesheet = Object.assign({}, timesheetObj);

  }

  checkTimesheetForInActiveActivities(timesheetObj: any, template: TemplateRef<any>) {
    console.log("The timesheet object is", timesheetObj);
    
    // Handle hierarchical EmployeeTimesheetDTO structure
    const status = timesheetObj.statusDisplay || this.mapStatusToString(timesheetObj.status);
    const dayType = timesheetObj.dayType;
    
    // Store selected timesheet for inactive activities check
    this.selectedTimesheet = timesheetObj;
    
    if (dayType == "Working" && (status === "Pending" || status === "Rejected") && timesheetObj?.inactiveTimesheetActivities) {
      this.openInActiveUpdateConfimationModal(template, timesheetObj);
      // Note: Document handling may need adjustment for hierarchical structure
      this.previousFilledDocument = timesheetObj.documentData?.find((d: any) => d.docType === 'Filled')?.docId;
      this.previousApprovedDocument = timesheetObj.documentData?.find((d: any) => d.docType === 'Approved')?.docId;
    } else {
      this.showUpdateTimesheetForm(timesheetObj);
      // Note: Document handling may need adjustment for hierarchical structure
      this.previousFilledDocument = timesheetObj.documentData?.find((d: any) => d.docType === 'Filled')?.docId;
      this.previousApprovedDocument = timesheetObj.documentData?.find((d: any) => d.docType === 'Approved')?.docId;
    }
  }

  updateInactiveActivitiesTimesheet() {
    this.showUpdateTimesheetForm(this.selectedTimesheet);
  }


  onMonthYearChange() {
  this.resetBulkUploadForm('MONTH');  
  this.getMyProjectsInMonthYear();
  }

   getMyProjectsInMonthYear() {
  
      this.timesheetObj.empId = this.currentUser.empId;
      this.timesheetService.getMyProjectsInMonthYear(this.timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.projectsInMonthYear = response.serviceResponse;
        } else {
          console.error(response.serviceResponse);
        }
      });
  
    }


    resetBulkUploadForm(level: 'MONTH' | 'EMP' | 'PROJECT' | 'UPLOAD') {


    this.finalFromDate = null;
    this.finalToDate = null;
    this.disableList = [];
    this.disableListFormatted = [];
    this.isUploadAllowed = false;


    this.selectedFile2 = null;
    this.fileName2 = '';
    this.fileType2 = '';
    this.previewUrl2 = '';
    this.fileError2 = '';

    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }


    if (level === 'MONTH') {
      this.timesheetObj.projectId = null;
      this.projectsInMonthYear = [];
    }


    if (level === 'PROJECT') {


    }

    if (level === 'UPLOAD') {
      this.timesheetObj.monthYear = null;
      this.timesheetObj.projectId = null;
      this.projectsInMonthYear = [];
    }
  }



  showUpdateTimesheetForm(timesheetObj: Timesheet) {
    this.isTimesheetForm = true;
    this.isUpdation = true;

    this.isTimesheetTable = false;
    this.isCreation = false;
    this.isTimesheetUpdate = true;
    // console.log("OLD", timesheetObj);


    this.timesheetObj = Object.assign({}, timesheetObj);

    this.timesheetObj.updatedTimesheetActivities = [];
    this.timesheetObj.date = (this.timesheetObj.date) ? moment(timesheetObj.date, "DD-MM-YYYY").toDate() : '';
    this.fromDate = new Date(this.timesheetObj.date);
    this.timesheetObj.officeInTime = (this.timesheetObj.officeInTime) ? moment(timesheetObj.officeInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.officeOutTime = (this.timesheetObj.officeOutTime) ? moment(timesheetObj.officeOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.createdOn = (this.timesheetObj.createdOn) ? moment(timesheetObj.createdOn, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.dayType = (this.timesheetObj.dayType == "Holiday") ? "Week Off" : this.timesheetObj.dayType;
    this.timesheetObj.clientInTime = (this.timesheetObj.clientInTime) ? moment(timesheetObj.clientInTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    this.timesheetObj.clientOutTime = (this.timesheetObj.clientOutTime) ? moment(timesheetObj.clientOutTime, "DD-MM-YYYY HH:mm:ss").toDate() : '';
    console.log("timesheetObj.bulkApprovedDocId",timesheetObj.bulkApprovedDocId);
    this.timesheetObj.bulkApprovedDocId = timesheetObj.bulkApprovedDocId;
    console.log("NEW", this.timesheetObj);

    if (this.timesheetObj.officeInTime) {
      this.maxOutTimeDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());
    }

    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" ||this.timesheetObj.dayType == "Comp Off" || this.timesheetObj.dayType == "Leave") {
      this.__tempDescription = this.timesheetObj.description;
    }

    let userObj: User = new User();
    if (this.isSelfTimesheets) {
      this.timesheetObj.timesheetAppliedFor = "self";
      this.timesheetObj.empId = this.currentUser.empId;
      // this.timesheetObj.shadowFor = "Self";

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
    const status = (this.timesheetObj.clientApprovalStatus || '').toLowerCase();
    this.clientSideIdNotMandatory = !(status === 'pending' || status === 'approved');

    if (!this.clientSideIdNotMandatory) {
      const officeIn = this.timesheetObj.officeInTime;
      const officeOut = this.timesheetObj.officeOutTime;
      const clientIn = this.timesheetObj.clientInTime;
      const clientOut = this.timesheetObj.clientOutTime;

      if (officeIn && officeOut && clientIn && clientOut) {

        const isInSame = moment(officeIn).isSame(moment(clientIn), 'minute');
        const isOutSame = moment(officeOut).isSame(moment(clientOut), 'minute');

        this.syncTimes = isInSame && isOutSame;

      }
      if (this.timesheetObj.clientInTime) {
        this.setTimeDropdowns(this.timesheetObj.clientInTime, 'ClientIn');
      }
      if (this.timesheetObj.clientOutTime) {
        this.setTimeDropdowns(this.timesheetObj.clientOutTime, 'ClientOut');
      }
    }
    this.getProjectListForDateAndEmpId();
    this.getClientDetailsByProjectIdAndEmpId();
    this.getAllAvailableTimesheetByEmpId(this.timesheetObj.empId);
    this.onProjectSelect(timesheetObj.projectId);
  }

  /**
   * Handle timesheet updated event from form component
   * @param timesheetId - ID of the updated timesheet
   */
  onTimesheetUpdated(timesheetId: number): void {
    // Refresh the timesheet list after update
    this.getAllMyTimesheetsByEmpId();
    // Reset form state
    this.resetTimesheetForm();
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

    timesheetToDateFilter = (checkDate: Date) => {

    if (this.timesheetObj?.isNightShift && this.fromDate) {
        if (this.isNextDay(this.fromDate, checkDate)) {
            return true;
        }
    }

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

    let endDate = serverDate;
    let startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + CURRENT_DAY) * DAY_IN_MS));

    if (this.isTimesheetForm && this.isUpdation) {
      this.availableTimesheets = this.availableTimesheets.filter(timesheet => this.datePipe.transform(timesheet.date, "yyyy-MM-dd") != this.datePipe.transform(this.timesheetObj.date, "yyyy-MM-dd"));
    }

    if (this.isTimesheetLockCheckEnable == "false") {
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + CURRENT_DAY) * DAY_IN_MS));
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "yyyy-MM-dd"))) ? true : false;
    } else {
      return (checkDate <= endDate && checkDate >= startDate && !this.availableTimesheets.find(timesheet => timesheet.date == this.datePipe.transform(checkDate, "yyyy-MM-dd"))) ? true : false;
    }
  }

  private isNextDay(date1: Date, date2: Date): boolean {
  const DAY_IN_MS = 24 * 60 * 60 * 1000;
  return (
    new Date(date2).setHours(0, 0, 0, 0) -
    new Date(date1).setHours(0, 0, 0, 0)
  ) === DAY_IN_MS;
}


  outTimeFilter = (checkDate: Date) => {
    const dateFormat = 'YYYY-MM-DD';

    let startDate = this.timesheetObj.officeInTime;
    let endDate = new Date(moment(this.timesheetObj.officeInTime).add(1, 'd').toString());

    return (moment(checkDate).format(dateFormat) <= moment(endDate).format(dateFormat) && moment(checkDate).format(dateFormat) >= moment(startDate).format(dateFormat)) ? true : false;
  }









  setMaxInTimeDate(timesheetDate: any) {
    //console.log("timesheetDate : ", moment(timesheetDate).format(moment.HTML5_FMT.DATETIME_LOCAL));

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off"&& this.timesheetObj.dayType != "Comp Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
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
    startDateInput?.setAttribute('max', this.today);
    startEndDate?.setAttribute('max', this.today);
    //console.log("set date :: ",startDateInput);

  }
  resetTotalWorkingOfficeHours(template: TemplateRef<any>) {

    if (this.timesheetObj.officeInTime) {

      const systemCurrentTime = moment();
      const userOfficeInTime = moment(this.timesheetObj.officeInTime);


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
    this.fromDate = null;
    this.toDate = null;
    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" ||this.timesheetObj.dayType == "Comp Off" || this.timesheetObj.dayType == "Leave" || this.timesheetObj.dayType == "Client Holiday") {
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
    }
    this.makeApmosysInTime();
      this.makeApmosysOutTime();
      this.makeClientInTime();
      this.makeClientOutTime();
  }

  setTotalWorkingOfficeHours() {
    const dateFormat = 'YYYY-MM-DD';
    const systemCurrentTime = moment();
    const userOfficeOutTime = moment(this.timesheetObj.officeOutTime);



    if (
      (userOfficeOutTime.isAfter(systemCurrentTime, 'minute') || userOfficeOutTime.isSame(systemCurrentTime, 'minute')) &&
      userOfficeOutTime.isSame(systemCurrentTime, 'day')
    ) {
      const updatedInTime = systemCurrentTime.subtract(1, 'minute').toDate();
      this.timesheetObj.officeOutTime = updatedInTime;

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
    if (this.timesheetObj.clientSideId) {
      const dateFormat = 'YYYY-MM-DD';
      const systemCurrentTime = moment();
      const userOfficeOutTime = moment(this.timesheetObj.clientOutTime);



      if (
        (userOfficeOutTime.isAfter(systemCurrentTime, 'minute') || userOfficeOutTime.isSame(systemCurrentTime, 'minute')) &&
        userOfficeOutTime.isSame(systemCurrentTime, 'day')
      ) {
        const updatedInTime = systemCurrentTime.subtract(1, 'minute').toDate();
        this.timesheetObj.clientOutTime = updatedInTime;

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

    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.date)) {
      this.alertMessage = "Please enter Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.timesheetObj.isNightShift &&
      (!this.validationService.validateNullUndefinedEmptyString(this.toDate))) {
      this.alertMessage = "Please enter to Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.dayType)) {
      this.alertMessage = "Please select Day Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (timesheetObj.dayType != "Public Holiday" && timesheetObj.dayType != "Week Off"&& timesheetObj.dayType != "Comp Off" && timesheetObj.dayType != "Leave" && this.timesheetFillable) {
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

        if(this.timesheetObj.clientApprovalStatus == null || this.timesheetObj.clientApprovalStatus == ''){
          this.alertMessage = "Please select client approval status !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if(this.timesheetObj.clientSideId == null || this.timesheetObj.clientSideId == ''){
          this.alertMessage = "Please enter client side id !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

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
        this.alertMessage = "Please enter In Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(timesheetObj.officeOutTime)) {
        this.alertMessage = "Please select Out Time !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (totalWorkingHoursInSeconds <= 0) {
        this.alertMessage = "Please select Valid IN-OUT Time !!"
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
      if(totalActivityTime > 0){
        let totalActivityTimeInSeconds = totalActivityTime * 60 * 60;
        if (totalActivityTimeInSeconds > totalWorkingHoursInSeconds) {
          this.alertMessage = `Total Activity Completion Time cannot be greater than Total Working Hours!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }
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

  onShadowForChange() {
  if (this.timesheetObj.shadowEmpId === this.timesheetObj.empId) {
    // this.shadowForSelf = true;
    this.docRequiredForShadow =false;
    this.clientSideIdNotMandatory=true;
  } else {
    // this.shadowForSelf = false;
    this.docRequiredForShadow =true;
    this.clientSideIdNotMandatory= false;
  }
}

  onCreateTimesheet(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';


    this.timesheetObj.description = this.timesheetObj.description?.trim();

    let inputValidated: boolean = this.validateTimesheetObj(this.timesheetObj, template)
    if (!inputValidated) return;

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off"&& this.timesheetObj.dayType != "Comp Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
      //console.log("allTimesheetActivities :", this.allTimesheetActivities, this.allTimesheetActivities[0]);
      this.timesheetObj.allTimesheetActivities = (Object.keys(this.allTimesheetActivities[0]).length === 0) ? null : this.allTimesheetActivities;
    } else {
      this.timesheetObj.allTimesheetActivities = null;
    }

    // if (this.timesheetObj.timesheetAppliedFor == 'asShadow') {
    //   this.timesheetObj.isShadowTimesheet = true;
    //       if(this.timesheetObj.shadowEmpId == this.currentUser.empId){
    //   this.shadowForSelf = true;
    // }
    // } else {
    //   this.timesheetObj.isShadowTimesheet = false;
    // }

    if (this.timesheetObj.timesheetAppliedFor === 'asShadow') {
  this.timesheetObj.isShadowTimesheet = true;

  // Check if shadow is SELF or OTHER
  if (this.timesheetObj.shadowEmpId === this.currentUser.empId) {
    this.shadowForSelf = true;
    this.timesheetObj.shadowFor = "Self";
  } else {
    this.shadowForSelf = false;
  }

} else {
  this.timesheetObj.isShadowTimesheet = false;
  this.shadowForSelf = false;  // default
}

    // if (this.timesheetObj.timesheetAppliedFor == 'asShadow' && this.timesheetObj.shadowEmpId == this.timesheetObj.empId){
    //   this.timesheetObj.shadowFor = "Self";
    //   this.shadowForSelf = false;
    // }

    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Comp Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
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

    this.timesheetService.addTimesheetWithClient(this.timesheetObj, this.selectedFile, this.selectedFile2).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        if (this.autoFillTimesheet) {
          this.router.navigate(['/home']);
          sessionStorage.setItem('autoFillTimesheet', 'true');
        }

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
        this.resetTimesheetForm();
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


    if (this.timesheetObj.dayType != "Public Holiday" && this.timesheetObj.dayType != "Week Off" && this.timesheetObj.dayType != "Comp Off" && this.timesheetObj.dayType != "Leave" && this.timesheetObj.dayType != "Client Holiday") {
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
    if (this.currentUser.approvalsTo == 'Reporting Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.reportingManagerId;
    } else if (this.currentUser.approvalsTo == 'Manager') {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    } else {
      this.timesheetObj.currentManagerId = this.currentUser.managerId;
    }
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
      console.log(this.timesheetObj.bulkapprovedId);
      let newDoc2: TimesheetDoc = {
        docId: this.timesheetObj.bulkApprovedDocId != null?this.previousFilledDocument:this.previousApprovedDocument,
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
    console.log("onTimesheetDescriptionChange called");
    if (this.isUpdation) {
      if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" ||this.timesheetObj.dayType == "Comp Off" || this.timesheetObj.dayType == "Leave") {
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


    } else {
      console.log("Team member list is",this.teamMemberList);
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      console.log("Team member is ",teamMember);
      userObj.empId = teamMember?.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = teamMember.empId;

      if (teamMember.isTimesheetFilledByMember == "true") {
        this.openAlertMod(this.alertTemplate, "Timesheet cannot be filled for team member more than 2 days.");
        this.timesheetObj.empId = '';
        eventTarget.value = "";
        this.disableCreateUpdateTimesheet = true;
        eventTarget.value = '';



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

    // this.getAllProjectsByEmpId(userObj);
    // this.getAllAvailableTimesheetByEmpId(this.timesheetObj.empId);
  }

  getAllTeamMemberList() {
    console.log("Timesheet object is",this.timesheetObj)
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
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
    console.log("Timesheet object is",this.timesheetObj)
  }

  getAllProjectsByEmpId(employeeObj: User) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];
    // this.teamList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    this.timesheetNewService.getAllProjectsByEmpId(employeeObj.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        if (this.allProjectsList.length == 0) {
        }
        else {
          const key = "clientId";
          this.clientList = [...new Map(this.allProjectsList.map((project: Timesheet) => [project[key], project])).values()].map((project: Timesheet) => {
            return { clientId: project.clientId, clientName: project.clientName, projectId: project.projectId }
          });
        }
      } else {
        console.error(response.serviceResponse)
        this.openAlertWithResetMod(this.alertModalWithoutReload, "Please contact the RMG team and set up your default project mapping!");
      }
    });
  }

  getProjectList(activityObj: Activity) {
    this.projectList = [];

    if (!this.clientDetails?.project?.teams?.length) {
            return;
    }

    this.projectList = this.clientDetails.project.teams.map(team => ({
      teamId: team.teamId,
      teamName: team.teamName,
      projectId: this.clientDetails.project.projectId,
      projectName: this.clientDetails.project.projectName,
      displayTeam: `${this.clientDetails.project.projectName} | ${team.teamName}`
    }));

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
    activityObj.clientLocationList = [];

    if (!this.clientDetails?.clientLocations?.length) {
      return;
    }

    activityObj.clientLocationList = this.clientDetails.clientLocations.map(loc => ({
      clientLocationId: loc.clientLocationId,
      clientLocation: loc.clientLocation
    }));

    // reset dependent fields
    activityObj.clientLocationId = null;
    activityObj.teamId = null;
  }

  setAllClientLocations(activityObj, clientLocationList: any) {
    const selectedActivityObj: Activity = this.allTimesheetActivities.find(activity => activity === activityObj);
    selectedActivityObj.clientLocationList = clientLocationList;
    if ((!this.isTimesheetUpdate && activityObj.clientLocationId == "") || !this.clientLocationList.find(clientLocation => clientLocation.clientLocationId == selectedActivityObj.clientLocationId)) {
      selectedActivityObj.clientLocationId = '';
    }
  }

  getAllActivitiesByProjectIdandEmpId(activityObj: any) {
    if (!activityObj?.teamId || !this.clientDetails?.project) {
      return;
    }

    const payload = {
      empId: this.timesheetObj.empId,
      teamId: activityObj.teamId,
      projectId: this.clientDetails.project.projectId,
      clientId: this.timesheetObj.clientId,
      clientLocationId: this.timesheetObj.clientLocationId
    };

    this.timesheetService.getAllActivitiesByProjectIdandEmpId(payload).pipe(first()).subscribe((response: any) => {

        if (response.serviceStatus !== 'Success') {
          console.error(response.serviceResponse);
          return;
        }

        let activityList = [...response.serviceResponse];
        activityList.sort((a, b) => a.activity.localeCompare(b.activity));
        activityList = this.filterActivitiesByDepartment(activityList);
        this.setAllProjectActivities(activityObj, activityList);
      });
  }

  empIdForTeamMember:any='';
  getAllAvailableTimesheetByEmpId(empId:any) {
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
    } else {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));
    }

    let timesheetObj = new Timesheet();
    timesheetObj.empId = empId;
    timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
    timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);

    //console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetNewService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
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
      this.timesheetNewService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allMyTimesheets = response.serviceResponse;
          this.processHierarchicalTimesheetData();
        } else {
          console.error(response.serviceResponse)
          this.allMyTimesheets = [];
        }
      }, (error) => {
        console.error("Error fetching timesheets:", error);
        this.allMyTimesheets = [];
      });

    }
  }

  /**
   * Process hierarchical timesheet data from API response
   * Formats dates, calculates totals, and prepares data for display
   */
  processHierarchicalTimesheetData(): void {
    this.allMyTimesheets.forEach(timesheet => {
      // Format main timesheet dates
      timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
      timesheet.workCheckIn = (timesheet.workCheckIn) ? moment(timesheet.workCheckIn).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.workCheckOut = (timesheet.workCheckOut) ? moment(timesheet.workCheckOut).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.isNightShiftDisplay = (timesheet.isNightShift == true || timesheet.isNightShift == 'true') ? 'Night Shift' : 'Regular Shift';
      timesheet.statusDisplay = this.mapStatusToString(timesheet.status);
      
      // Process location sessions
      if (timesheet.locationSessions && timesheet.locationSessions.length > 0) {
        let totalActivityMinutes = 0;
        
        timesheet.locationSessions.forEach((location: any) => {
          // Format location times (they come as "HH:mm" or "HH:mm:ss" strings from backend)
          // If they need formatting, we can add it here
          // location.locationInTime and location.locationOutTime are already strings
          
          // Process projects within location
          if (location.projects && location.projects.length > 0) {
            location.projects.forEach((project: any) => {
              // Calculate project total hours from activities
              if (project.activities && project.activities.length > 0) {
                let projectTotalMinutes = 0;
                project.activities.forEach((activity: any) => {
                  if (activity.durationMinutes) {
                    projectTotalMinutes += activity.durationMinutes;
                    totalActivityMinutes += activity.durationMinutes;
                  }
                });
                project.totalActivityMinutes = projectTotalMinutes;
              }
            });
          }
        });
        
        // Store total activity minutes for display
        timesheet.totalActivitiesMinutes = totalActivityMinutes;
      }
    });
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

    // Backend now returns hierarchical EmployeeTimesheetDTO structure
    this.timesheetService.getAllMyTeamTimesheets(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allMyTimesheets = response.serviceResponse;
        this.processHierarchicalTimesheetData();
      } else {
        console.error(response.serviceResponse);
        this.allMyTimesheets = [];
      }
    }, (error) => {
      console.error("Error fetching team timesheets:", error);
      this.allMyTimesheets = [];
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
        this.getAllAvailableTimesheetByEmpId(this.currentUser.empId);
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
          "Project Name" :x.projectName,
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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openNightShiftTemplate(template: TemplateRef<any>, event) {

    if (event.target.checked) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    }
    else{
      this.toDate = null;
      this.makeApmosysInTime();
      this.makeApmosysOutTime();
      if(!this.clientSideIdNotMandatory){
        this.makeClientInTime();
        this.makeClientOutTime();
      }
    }
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  confirmNightShift(value:Boolean) {
    this.timesheetObj.isNightShift=value;
    if (value && this.timesheetObj.date) {
      this.fromDate = new Date(this.timesheetObj.date);
      this.fromDate.setHours(0, 0, 0, 0);
    }
    this.timesheetToDateFilter = this.timesheetToDateFilter.bind(this);
    this.modalRef?.close();
  }


  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.viewAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
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

  getProjectListForDateAndEmpId() {
    //employeeTeamMapping has startDate and endDate as localDateTime
    const d = new Date(this.fromDate);
    const localDateTime = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T00:00:00`;
    
    const payload = {
      empId: this.timesheetObj.empId,
      date: localDateTime
    };
    
    this.timesheetService.getProjectListForDateAndEmpId(payload).pipe(first()).subscribe(async(response: any) => {
      if (response.serviceStatus == "Success") {
        this.activeProjectList = response.serviceResponse;
        console.log("Active Project List :::::::::", this.activeProjectList);
     
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
  this.timesheetService.getDocumentDataByDocId(docId)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.docData2 = response.serviceResponse.docData;
        this.mimeType = response.serviceResponse.docMimeType;

        // Excel → Download
        if (
          this.mimeType === 'application/vnd.ms-excel' ||
          this.mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        ) {
          const fileName = response.serviceResponse.docName || 'document.xlsx';
          this.downloadExcel(this.docData2, this.mimeType, fileName);
        }
        // PDF / Image → Preview
        else {
          this.showPreview(this.docData2, this.mimeType);
        }
      }
    });
  }
  getFinalDocumentDataByDocId(timesheetId:any,docId:any){
        console.log(docId,":docId");
        this.timesheetService.getFinalDocumentDataByDocId(timesheetId,docId).pipe(first()).subscribe((response: any) => {
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

    if (this.selectedFile2 != null && this.finalFromDate != null && this.finalToDate != null && this.currentUser.empId != null) {
    //   this.timesheetService.bulkFinalDocumentUpload(this.selectedFile2, this.finalFromDate, this.finalToDate, this.currentUser.empId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
    //     if (response.serviceStatus === "Success") {
    //      this.resetBulkUploadForm('UPLOAD');

    //       this.openAlertMod(template, response.serviceResponse);
    //     } else {
    //       this.openAlertMod(template, response.serviceResponse);
    //     }
    //   });

    const payload: ProjectBasedBulkUploadPayload = {
        createdBy: this.currentUser.empId,
        empIds: [this.currentUser.empId],
        fromDate: this.finalFromDate,
        toDate: this.finalToDate,
        projectId: this.timesheetObj.projectId,
      }

    this.timesheetService.bulkFinalUploadProjectBased(payload, this.selectedFile2).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.resetBulkUploadForm('UPLOAD');
          this.alertMessage = "Success";
          this.openAlertMod(template, response.serviceResponse);
          this.showBulkUploadForm();

        } else {
          this.alertMessage = response.serviceResponse || "Error while bulk final upload";
          this.openAlertMod(template, this.alertMessage);
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
    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }

  openPreviewModal() {
    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }

  openPreviewModalForTwo(docType: 'doc1' | 'doc2'): void {
    this.activePreviewUrl = docType === 'doc1' ? this.previewUrl1 : this.previewUrl2;
    this.activeFileType = docType === 'doc1'
      ? (this.selectedFile?.type === 'application/pdf' ? 'pdf' : 'image')
      : (this.selectedFile2?.type === 'application/pdf' ? 'pdf' : 'image');

    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
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

  // onFinalFileSelected(event: any): void {
  //   const file: File = event.target.files[0];
  //   this.fileError2 = '';
  //   this.previewUrl2 = null;
  //   this.fileType2 = null;

  //   if (!file) return;

  //   const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
  //   const maxSize = 500 * 1024;

  //   if (!allowedTypes.includes(file.type)) {
  //     this.fileError2 = 'Only PDF, JPG, JPEG, and PNG files are allowed.';
  //     return;
  //   }

  //   if (file.size > maxSize) {
  //     this.fileError2 = 'File size must be 500Kb or less.';
  //     return;
  //   }

  //   if (this.rawObjectUrl2) {
  //     URL.revokeObjectURL(this.rawObjectUrl2);
  //   }

  //   const objectUrl = URL.createObjectURL(file);
  //   this.rawObjectUrl2 = objectUrl;
  //   this.previewUrl2 = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
  //   this.fileType2 = file.type === 'application/pdf' ? 'pdf' : 'image';
  //   this.selectedFile2 = file;
  //   this.fileName2 = file.name;
  // }
onFinalFileSelected(event: any): void {
  const file: File = event.target.files[0];
  this.fileError2 = '';
  this.previewUrl2 = null;
  this.fileType2 = null;

  if (!file) return;

  const allowedTypes = [
    'application/pdf',
    'image/jpeg',
    'image/png',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  ];

  const maxSize = 500 * 1024;

  if (!allowedTypes.includes(file.type)) {
    this.fileError2 =
      'Only PDF, JPG, JPEG, PNG, XLS, and XLSX files are allowed.';
    return;
  }

  if (file.size > maxSize) {
    this.fileError2 = 'File size must be 500Kb or less.';
    return;
  }

  // Cleanup old URL
  if (this.rawObjectUrl2) {
    URL.revokeObjectURL(this.rawObjectUrl2);
  }

  const objectUrl = URL.createObjectURL(file);
  this.rawObjectUrl2 = objectUrl;

  if (file.type === 'application/pdf') {
    this.previewUrl2 =
      this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
    this.fileType2 = 'pdf';
  }
  else if (file.type.startsWith('image/')) {
    this.previewUrl2 =
      this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
    this.fileType2 = 'image';
  }
  else {
    // Excel
    this.fileType2 = 'excel';
  }

  this.selectedFile2 = file;
  this.fileName2 = file.name;
}


  clearPreviousSelections() {
    this.selectedProjectId = null;
  }

  openSelfModal3(template: TemplateRef<any>) {
    this.empClientSideObj.clientSideId = '';
    this.empClientSideObj.projectId = this.timesheetObj.projectId;
    this.updateClientIdModalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
    this.getActiveProjectsAndClientSideIdByEmpId();
  }

  hideSelfModal3(): void {
    if (this.updateClientIdModalRef) {
      this.updateClientIdModalRef?.close();
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
    const d = new Date(this.fromDate);
    const localDateTime = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T00:00:00`;
    
    const payload = {
      empId: this.timesheetObj.empId,
      date: localDateTime,
      projectId: projectId
    };

    this.timesheetService.getOtherTeamMembersByDateAndProjectId(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.getTimesheetMetadata();
      } else {
        this.openNoOtherShadowResource(response.serviceResponse);
      }
    });
  }

  updateClientSideIdMapping(template: TemplateRef<any>) {
  if (!this.empClientSideObj.clientSideId || this.empClientSideObj.clientSideId.trim() === '') {
    this.openAlertMod(template, 'Please enter a valid Client Side ID.');
    return;
  }

  if(this.clientSideIdMandetoryFromBackend){
    if(this.empClientSideObj.clientSideId.toLowerCase().startsWith("na")){
      this.modalRef?.close();
      this.empClientSideObj.clientSideId = '';
      this.openAlertMod(template, 'As per the configuration defined by your project manager, Client IDs for this project cannot begin with “NA”. Kindly provide the valid Client ID assigned to you. For additional assistance, please reach out to your project manager.');
      return;
    }
  }
  if(this.timesheetObj.timesheetAppliedFor == 'team'){
    this.empClientSideObj.empId = this.timesheetObj.empId;
  }
  else{
    this.empClientSideObj.empId = this.currentUser.empId;
  }
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

  async onProjectChange(projId: any) {
    if(this.timesheetObj.timesheetAppliedFor == 'team'){
      this.getClientSideIdByProjectIdAndEmpId(projId, this.timesheetObj.empId);
    }
    else{
      this.getClientSideIdByProjectIdAndEmpId(projId, this.currentUser.empId);
    }
   const response:any = await this.timesheetService.isClientMandetory(+projId).pipe(first()).toPromise();
   this.clientSideIdMandetoryFromBackend = response.serviceResponse;
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
    //employeeTeamMapping has startDate and endDate as localDateTime
    const d = new Date(this.fromDate);
    const localDateTime = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T00:00:00`;
    
    const payload = {
      empId: empId,
      date: localDateTime,
      projectId: this.timesheetObj.projectId
    };

    this.timesheetService.getActiveProjectsAndClientSideIdByEmpId(payload).pipe(first()).subscribe((response: any) => {
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
    this.noNotAppliedYetModalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
  }

  hideNoNotAppliedYet(): void {
    if (this.noNotAppliedYetModalRef) {
      this.noNotAppliedYetModalRef?.close();
      this.resetTimesheetForm();
    }
  }

  onClientApprovalStatusChange(event: any, template: TemplateRef<any>): void {
    const selectedValue = event.target.value;
    if (selectedValue === 'no') {
      // this.resetTimesheetForm();
      this.openNoNotAppliedYet(template);
    }else if(selectedValue === 'pending'){
        this.selectedFile2 = null;
        this.fileName2 = '';
        this.previewUrl2 = null;
        this.rawObjectUrl2 = null;
        this.fileType2 = null;
    }

  }

  checkClientSideIdPresentOrNot(timesheetObj: Timesheet) {
    if (timesheetObj.clientSideId == null || timesheetObj.clientSideId == '')
      this.openclientSideIdNotMandatoryFound(this.clientSideIdNotMandatoryFound);
  }

  openclientSideIdNotMandatoryFound(template: TemplateRef<any>) {
    this.clientSideIdNotMandatoryFoundModalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
  }

  hideclientSideIdNotMandatoryFound(): void {
    if (this.clientSideIdNotMandatoryFoundModalRef) {
      this.clientSideIdNotMandatoryFoundModalRef?.close();
    }
  }

  getProjectName(projectId: number): string {
    const project = this.projectClientIdList?.find(p => p.projectId === projectId);
    return project ? project.projectName : '';
  }

  hideClientSideIdForm() {
    this.clientSideIdForm.close();
  }

  onCancelClientSideId(template: TemplateRef<any>) {
    this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.currentUser.empId);
    this.hideClientSideIdForm();
    this.openclientSideIdNotMandatoryFound(template);
  }

  hideNoClientSideIdProvided() {
    this.noClientSideIdProvided.close();
    this.resetTimesheetForm();
  }

  onProjectSelect(projectId: any) {
    if (this.timesheetObj.timesheetAppliedFor == "asShadow") {
      this.getEmployeeListByProjectId(projectId)
       this.checkIfProjectRequiresClientId(projectId);
    } else {
      this.checkIfProjectRequiresClientId(projectId);
    }
    this.getClientDetailsByProjectIdAndEmpId();
  }

  onProjectSelectBulk(projectId: any) {
    this.timesheetObj.projectId = projectId;
    this.resetBulkUploadForm('PROJECT');
    this.checkIfProjectRequiresClientId(projectId);
    this.getAllDisabledDateListForBulkDocSubmit(projectId);
    // this.filterClients(projectId);
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
  clientSideIdMandetoryFromBackend: boolean = false;
   checkIfProjectRequiresClientId(projectId: any) {
    // this.hasClientSideId = false;

    this.timesheetService.checkIfProjectRequiresClientId(projectId).pipe(first()).subscribe(async(response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequiresClientId = response.serviceResponse;
        this.projAlertRequireClientId = response.serviceResponse;
        if (this.projectRequiresClientId) {
          //is client id mandetory api call.
          // this.timesheetService.isClientMandetory(+projectId).pipe(first()).subscribe((response: any) => {
          //   if (response.serviceStatus == "Success") {
          //     this.clientSideIdMandetoryFromBackend = response.serviceResponse;
          //   }
          // });
          response = await this.timesheetService.isClientMandetory(+projectId).pipe(first()).toPromise();
          this.clientSideIdMandetoryFromBackend = response.serviceResponse;
           this.withVms = this.projectRequiresClientId && this.clientSideIdMandetoryFromBackend;

          this.clientSideIdNotMandatory = false;
          this.timesheetObj.clientSideId = null;
          this.timesheetObj.hasClientSideId = true;
          this.clientIdNeeded = true;
          this.onProjectRequiresClientId(projectId, this.currentUser.empId);
        } else {
          this.fetchEmploymentIdByEmpId();
          // this.clientIdNeeded = false;
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

  async onProjectRequiresClientId(projectId: any, empId: any) {

    this.timesheetObj.clientSideId == null;
    if (this.timesheetObj.timesheetAppliedFor == 'team') {

      await this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.timesheetObj.empId);

      if (this.empClientSideObj.clientSideId == null && this.projectRequiresClientId) {
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
          this.empClientSideObj.clientSideId=null;
          this.timesheetObj.clientSideId=null;

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
    this.clientSideIdForm = this.modalService.open(this.clientSideIdFormRef, { modalDialogClass: 'modal-lg' });
  }

  async getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any) {
    // this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.timesheetObj.clientSideId = response.serviceResponse;
    //     if (this.timesheetObj.clientSideId) {
    //       this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;
    //     }
    //   } else {
    //     console.error(response.serviceResponse);
    //   }
    // });

    //
    await this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).toPromise().then((response: any) => {
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
    this.selectedTimesheetId = null; // Reset timesheet ID for update flow
    this.isTimesheetForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isTimesheetTable = true;
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
    this.selectedFile2 = null;
    this.fileName2 = '';
    this.fileType2 = '';
    this.previewUrl2 = '';
    this.selectedFile = null;
    this.fileName1 = '';
    this.fileType1 = '';
    this.previewUrl1 = '';
    this.shadowForSelf = false;
    this.finalFromDate = null;
    this.finalFromDate = null ;
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
      this.noClientSideIdProvided = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    }
  }

  openAlertWithResetMod(template: TemplateRef<any>, message: any) {
    this.alertWithResetModRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest2() {
    this.alertWithResetModRef?.close();
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
    this.onFromDateChange();
    this.getProjectListForDateAndEmpId();
    this.makeApmosysInTime();
    this.makeApmosysOutTime();
    this.makeClientInTime();
    this.makeClientOutTime();

    let timesheet: Partial<Timesheet> = { empId: this.currentUser.empId };

    this.timesheetService.getLastFilledTimesheetByEmp(timesheet)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          const autoData = response.serviceResponse[0];
          // console.log("autoFillTimesheet: " + JSON.stringify(autoData));

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
            this.getClientDetailsByProjectIdAndEmpId();
          }
          this.timesheetObj.clientApprovalStatus = autoData.clientApprovalStatus;


          setTimeout(() => {
            const defaultClient = this.clientDropdownList?.find(c => c.clientId === autoData.clientId);
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

  get formattedEmployeeId(): string {
    let placeholder = "";
    if (this.currentUser.isApmosysProduct) {
      placeholder = `NA (AP-${this.currentUser.employeementId})`;
    }
    else {
      placeholder = `NA (A-${this.currentUser.employeementId})`
    }
    return placeholder;
  }

  filterClients(projectId: any) {
    this.filteredClients = [];
    this.filteredClients = this.clientList?.filter(
      client => client.projectId === projectId
    );
  }



isFullMonthSelected(): boolean {
  if (!this.finalFromDate || !this.finalToDate) return false;

  const from = new Date(this.finalFromDate);
  const to = new Date(this.finalToDate);

  const firstDay = new Date(from.getFullYear(), from.getMonth(), 1);
  const lastDay = new Date(from.getFullYear(), from.getMonth() + 1, 0);

  return (
    from.toDateString() === firstDay.toDateString() &&
    to.toDateString() === lastDay.toDateString()
  );
}


checkUploadEligibility() {
  if (!this.finalFromDate || !this.finalToDate) {
    this.isUploadAllowed = false;
    this.disableUploadTooltip = "Please select a valid date range!";
    return;
  }

  const from = new Date(this.finalFromDate);
  const to = new Date(this.finalToDate);
  const today = new Date();

  const currentMonth = today.getMonth(); // 0-11
  const currentYear = today.getFullYear();

  // Previous month calculation
  const prevMonth = currentMonth - 1;
  const prevMonthYear = prevMonth < 0 ? currentYear - 1 : currentYear;
  const adjustedPrevMonth = (prevMonth + 12) % 12;

  const lastDayOfCurrentMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

  // Check if selection is fully in previous month
  const isPreviousMonthSelection =
    from.getMonth() === adjustedPrevMonth &&
    to.getMonth() === adjustedPrevMonth &&
    from.getFullYear() === prevMonthYear &&
    to.getFullYear() === prevMonthYear;

  const isCurrentMonthLastDayUpload =
    from.getMonth() === currentMonth &&
    to.getMonth() === currentMonth &&
    from.getFullYear() === currentYear &&
    to.getFullYear() === currentYear &&
    today.getDate() === lastDayOfCurrentMonth;

  this.isUploadAllowed = isPreviousMonthSelection || isCurrentMonthLastDayUpload;

  // Tooltip message
  this.disableUploadTooltip = this.isUploadAllowed
    ? ""
    : "Bulk upload is permitted only for dates in the previous month or on the last day of the current month. Please select a valid date range.";
}

  onDayTypeChange(){
    this.getActiveProjectsAndClientSideIdByEmpId();
    this.onTimesheetDescriptionChange();
    this.resetTimesheetFormOnDateChange();
  }

onTimesheetAppliedForChange(value: string): void {

  this.timesheetObj.timesheetAppliedFor = value;



  if (value === 'self') {

    this.getAllTeamMemberList();

    this.getTimesheetMetadata();
    console.log("I am here");

    this.timesheetObj.isShadowTimesheet = false;
     } else if (value === 'asShadow') {

    this.resetTimesheetFormForAutoFill();

    this.timesheetObj.isShadowTimesheet = true;



  } else {

    this.resetTimesheetFormForAutoFill();

    this.getAllTeamMemberList();

    this.timesheetObj.isShadowTimesheet = false;

  }
  }
    
  resetTimesheetFormOnDateChange(value?:any){
    this.timeReset();
    // this.toDate = null;
    this.timesheetObj.projectId = null;
    this.timesheetObj.clientSideId = null;
    this.timesheetObj.hasClientSideId = false;
    this.timesheetObj.shadowEmpId = null;
    this.timesheetObj.clientApprovalStatus = null;
    this.timesheetObj.description = null;
    this.timesheetObj.officeInTime = null;
    this.timesheetObj.officeOutTime = null;
    this.timesheetObj.totalWorkingOfficeHours = null;
    if(value!=='isNightShiftModal'){    this.timesheetObj.isNightShift = null;}
    this.timesheetObj.clientInTime = null;
    this.timesheetObj.clientOutTime = null;
    this.timesheetObj.totalClientWorkingHours = null;
    this.timesheetObj.docId = null;
    this.selectedFile2 = null;
    this.fileName2 = '';
    this.fileType2 = '';
    this.previewUrl2 = '';
    this.selectedFile = null;
    this.fileName1 = '';
    this.fileType1 = '';
    this.previewUrl1 = '';
    this.shadowForSelf = false;
    this.finalFromDate = null;
    this.finalFromDate = null ;
    if (this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave" || this.timesheetObj.dayType == "Client Holiday" || this.timesheetObj.dayType == "Comp Off") {
      this.timesheetFillable = false
    }
    else {
      this.timesheetFillable = true;
    }
    this.allTimesheetActivities = [];
    this.addInputActivityField();
    this.syncTimes = false;
  }

  //  zoomIn() {
  //   if (this.zoomScale < 2.5) {
  //     this.zoomScale += 0.1;
  //     this.zoomLevel = Math.round(this.zoomScale * 100);
  //   }
  // }

  // zoomOut() {
  //   if (this.zoomScale > 0.5) {
  //     this.zoomScale -= 0.1;
  //     this.zoomLevel = Math.round(this.zoomScale * 100);
  //   }
  // }

  // get transformStyle() {
  //   return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.zoomScale})`;
  // }

  // startDrag(event: MouseEvent) {
  //   if (this.zoomScale <= 1) return; // drag only when zoomed

  //   this.isDragging = true;
  //   this.startX = event.clientX - this.translateX;
  //   this.startY = event.clientY - this.translateY;
  //   event.preventDefault();
  // }

  // onDrag(event: MouseEvent) {
  //   if (!this.isDragging) return;

  //   this.translateX = event.clientX - this.startX;
  //   this.translateY = event.clientY - this.startY;
  // }

  // endDrag() {
  //   this.isDragging = false;
  // }

  // resetPreviewState() {
  //   this.zoomScale = 1;
  //   this.zoomLevel = 100;
  //   this.translateX = 0;
  //   this.translateY = 0;
  //   this.isDragging = false;
  // }

  // openNoOtherShadowResource(message: any) {
  //   this.alertMessage = message;
  //   this.noOtherShadowResourceModalRef = this.modalService.open(this.noOtherShadowResourceTemp, { modalDialogClass: 'modal-md' });
  // }

  // hideNoOtherShadowResource(): void {
  //   if (this.noOtherShadowResourceModalRef) {
  //     this.noOtherShadowResourceModalRef?.close();
  //     this.resetTimesheetForm();
  //   }
  // }
  // getClientDetailsByProjectIdAndEmpId() {
  //   if(!this.timesheetObj.dayType || this.timesheetObj.dayType == "Public Holiday" || this.timesheetObj.dayType == "Week Off" || this.timesheetObj.dayType == "Leave" || this.timesheetObj.dayType == "Client Holiday" || this.timesheetObj.dayType == "Comp Off"){
  //     return;
  //   }
  //   this.clientDetails = '';
  //   this.projectList = [];

  //   const payload = {
  //     empId: this.timesheetObj.empId,
  //     projectId: this.timesheetObj.projectId
  //   };
  //   this.timesheetService.getClientDetailsByProjectIdAndEmpId(payload).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.clientDetails = response.serviceResponse;
  //       this.clientDropdownList = [this.clientDetails];
  //     } else {
  //       console.error(response.serviceResponse)
  //       this.openAlertMod(this.alertTemplate, response.serviceResponse);
  //     }
  //   });
  // }

  // onClientChange(activityObj: any) {
  //   activityObj.clientLocationList = this.clientDetails.clientLocations || [];
  //   activityObj.clientLocationId = null;
  //   activityObj.teamId = null;
  // }

  // private filterActivitiesByDepartment(activityList: any[]): any[] {

  //   if (!activityList?.length) {
  //     return [];
  //   }

  //   if (this.timesheetObj.timesheetAppliedFor === 'team') {
  //     const teamMember = this.teamMemberList.find(
  //       emp => emp.empId === this.timesheetObj.empId
  //     );

  //     return activityList.filter(activity =>
  //       activity.departmentList?.map(Number).includes(teamMember?.departmentId)
  //     );
  //   }

  //   const filteredList = activityList.filter(activity =>
  //     activity.departmentList?.map(Number).includes(this.currentUser.departmentId)
  //   );

  //   if (filteredList.length === 0) {
  //     this.openAlertMod(
  //       this.alertModalWithoutReload,
  //       'No activity found for your department!'
  //     );
  //   }

  //   return filteredList;
  // }

 

  downloadSelectedFile(): void {
  if (!this.rawObjectUrl2 || !this.selectedFile2) return;

  const a = document.createElement('a');
  a.href = this.rawObjectUrl2;
  a.download = this.fileName2;
  a.click();
}

isExcelMimeType(mimeType: string): boolean {
  return mimeType === 'application/vnd.ms-excel'
    || mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
}


downloadExcel(base64Data: string, mimeType: string, fileName: string) {

  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);


  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  } 

  const blob = new Blob(
    [new Uint8Array(byteNumbers)],
    { type: mimeType }
  );

  const url = window.URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  a.click();

  window.URL.revokeObjectURL(url);
}


  getClientDetailsByProjectIdAndEmpId() {
    this.clientDetails = '';
    this.projectList = [];
    
    const payload = {
      empId: this.timesheetObj.empId,
      projectId: this.timesheetObj.projectId
    };

    this.timesheetService.getClientDetailsByProjectIdAndEmpId(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clientDetails = response.serviceResponse;
        this.clientDropdownList = [this.clientDetails];
      } else {
        console.error(response.serviceResponse)
        this.openAlertWithResetMod(this.alertModalWithoutReload, response.serviceResponse);
      }
    });
  }

  onClientChange(activityObj: any) {
    activityObj.clientLocationList = this.clientDetails.clientLocations || [];
    activityObj.clientLocationId = null;
    activityObj.teamId = null;
  }

  private filterActivitiesByDepartment(activityList: any[]): any[] {

    if (!activityList?.length) {
      return [];
    }

    if (this.timesheetObj.timesheetAppliedFor === 'team') {
      const teamMember = this.teamMemberList.find(
        emp => emp.empId === this.timesheetObj.empId
      );

      return activityList.filter(activity =>
        activity.departmentList?.map(Number).includes(teamMember?.departmentId)
      );
    }

    const filteredList = activityList.filter(activity =>
      activity.departmentList?.map(Number).includes(this.currentUser.departmentId)
    );

    if (filteredList.length === 0) {
      this.openAlertMod(
        this.alertModalWithoutReload,
        'No activity found for your department!'
      );
    }

    return filteredList;
  }

  zoomIn() {
    if (this.zoomScale < 2.5) {
      this.zoomScale += 0.1;
      this.zoomLevel = Math.round(this.zoomScale * 100);
    }
  }

  zoomOut() {
    if (this.zoomScale > 0.5) {
      this.zoomScale -= 0.1;
      this.zoomLevel = Math.round(this.zoomScale * 100);
    }
  }

  get transformStyle() {
    return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.zoomScale})`;
  }

  startDrag(event: MouseEvent) {
    if (this.zoomScale <= 1) return; // drag only when zoomed

    this.isDragging = true;
    this.startX = event.clientX - this.translateX;
    this.startY = event.clientY - this.translateY;
    event.preventDefault();
  }

  onDrag(event: MouseEvent) {
    if (!this.isDragging) return;

    this.translateX = event.clientX - this.startX;
    this.translateY = event.clientY - this.startY;
  }

  endDrag() {
    this.isDragging = false;
  }

  resetPreviewState() {
    this.zoomScale = 1;
    this.zoomLevel = 100;
    this.translateX = 0;
    this.translateY = 0;
    this.isDragging = false;
  }

  openNoOtherShadowResource(message: any) {
    this.alertMessage = message;
    this.noOtherShadowResourceModalRef = this.modalService.open(this.noOtherShadowResourceTemp, { modalDialogClass: 'modal-md' });
  }

  hideNoOtherShadowResource(): void {
    if (this.noOtherShadowResourceModalRef) {
      this.noOtherShadowResourceModalRef?.close();
      this.resetTimesheetForm();
    }
  }

  // ============================================
  // HIERARCHICAL VIEW - Sample Data & Methods
  // ============================================

  /**
   * Load sample hierarchical timesheet data for UI design/testing
   */
  loadSampleHierarchicalData(): void {
    this.allMyTimesheets = [
      {
        timesheetId: 1,
        empId: 123,
        date: '2025-01-15',
        dayType: 'Working',
        dayTypeId: 1,
        status: 1, // 1=Pending, 2=Approved, 3=Rejected
        totalWorkingMinutes: 480, // 8 hours
        totalActivitiesMinutes: 450,
        isNightShift: false,
        workCheckIn: '2025-01-15 09:00:00',
        workCheckOut: '2025-01-15 18:00:00',
        createdBy: 456,
        createdOn: '2025-01-15 10:00:00',
        createdByName: 'John Doe',
        locationSessions: [
          {
            locationMappingId: 101,
            workLocationType: 'Office',
            workLocationTypeId: 1,
            locationInTime: '09:00:00',
            locationOutTime: '13:00:00',
            projects: [
              {
                timesheetId: 1,
                projectId: 501,
                projectName: 'Project Alpha',
                poNo: 'PO-2025-001',
                poId: 1001,
                status: 1,
                clientApprovalStatus: 1,
                totalClientWorkingMinutes: 240,
                clientSideId: 'CLIENT-001',
                activities: [
                  {
                    timesheetId: 1,
                    activityId: 201,
                    projectId: 501,
                    activity: 'Code Review',
                    description: 'Reviewed PR #123 for authentication module',
                    durationMinutes: 60
                  },
                  {
                    timesheetId: 1,
                    activityId: 202,
                    projectId: 501,
                    activity: 'Development',
                    description: 'Implemented user login feature',
                    durationMinutes: 120
                  }
                ]
              },
              {
                timesheetId: 1,
                projectId: 502,
                projectName: 'Project Beta',
                poNo: 'PO-2025-002',
                poId: 1002,
                status: 1,
                clientApprovalStatus: null,
                totalClientWorkingMinutes: 60,
                clientSideId: null,
                activities: [
                  {
                    timesheetId: 1,
                    activityId: 203,
                    projectId: 502,
                    activity: 'Meeting',
                    description: 'Team standup meeting',
                    durationMinutes: 60
                  }
                ]
              }
            ]
          },
          {
            locationMappingId: 102,
            workLocationType: 'Client Site',
            workLocationTypeId: 2,
            locationInTime: '14:00:00',
            locationOutTime: '18:00:00',
            projects: [
              {
                timesheetId: 1,
                projectId: 501,
                projectName: 'Project Alpha',
                poNo: 'PO-2025-001',
                poId: 1001,
                status: 1,
                clientApprovalStatus: 1,
                totalClientWorkingMinutes: 240,
                clientSideId: 'CLIENT-001',
                activities: [
                  {
                    timesheetId: 1,
                    activityId: 204,
                    projectId: 501,
                    activity: 'Testing',
                    description: 'Tested login functionality',
                    durationMinutes: 120
                  },
                  {
                    timesheetId: 1,
                    activityId: 205,
                    projectId: 501,
                    activity: 'Documentation',
                    description: 'Updated API documentation',
                    durationMinutes: 120
                  }
                ]
              }
            ]
          }
        ],
        documentData: [
          {
            docId: 1001,
            projectId: 501,
            docName: 'filled_document.pdf',
            docType: 'Filled',
            finalFlag: false,
            bulkApprovedDocId: null
          },
          {
            docId: 1002,
            projectId: 501,
            docName: 'approved_document.pdf',
            docType: 'Approved',
            finalFlag: true,
            bulkApprovedDocId: null
          }
        ]
      },
      {
        timesheetId: 2,
        empId: 123,
        date: '2025-01-16',
        dayType: 'Working',
        dayTypeId: 1,
        status: 2, // Approved
        totalWorkingMinutes: 480,
        totalActivitiesMinutes: 480,
        isNightShift: false,
        workCheckIn: '2025-01-16 09:00:00',
        workCheckOut: '2025-01-16 18:00:00',
        createdBy: 456,
        createdOn: '2025-01-16 10:00:00',
        createdByName: 'John Doe',
        locationSessions: [
          {
            locationMappingId: 103,
            workLocationType: 'Office',
            workLocationTypeId: 1,
            locationInTime: '09:00:00',
            locationOutTime: '18:00:00',
            projects: [
              {
                timesheetId: 2,
                projectId: 503,
                projectName: 'Project Gamma',
                poNo: 'PO-2025-003',
                poId: 1003,
                status: 2,
                clientApprovalStatus: 2,
                totalClientWorkingMinutes: 480,
                clientSideId: 'CLIENT-002',
                activities: [
                  {
                    timesheetId: 2,
                    activityId: 206,
                    projectId: 503,
                    activity: 'Development',
                    description: 'Worked on payment integration',
                    durationMinutes: 480
                  }
                ]
              }
            ]
          }
        ],
        documentData: [
          {
            docId: 1003,
            projectId: 503,
            docName: 'approved_vms.pdf',
            docType: 'Approved',
            finalFlag: true,
            bulkApprovedDocId: null
          }
        ]
      },
      {
        timesheetId: 3,
        empId: 123,
        date: '2025-01-17',
        dayType: 'Leave',
        dayTypeId: 4,
        leaveTypeId: 1,
        status: 1,
        totalWorkingMinutes: 0,
        totalActivitiesMinutes: 0,
        isNightShift: false,
        workCheckIn: null,
        workCheckOut: null,
        createdBy: 456,
        createdOn: '2025-01-17 10:00:00',
        createdByName: 'John Doe',
        locationSessions: [],
        documentData: []
      }
    ];

    // Format dates and times
    this.allMyTimesheets.forEach(timesheet => {
      timesheet.date = timesheet.date ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
      timesheet.workCheckIn = timesheet.workCheckIn ? moment(timesheet.workCheckIn).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.workCheckOut = timesheet.workCheckOut ? moment(timesheet.workCheckOut).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.createdOn = timesheet.createdOn ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
      timesheet.isNightShiftDisplay = timesheet.isNightShift ? 'Night Shift' : 'Regular Shift';
      timesheet.statusDisplay = this.mapStatusToString(timesheet.status);
    });
  }

  /**
   * Expansion state management methods
   */
  toggleTimesheetExpansion(timesheetId: number): void {
    if (this.expandedTimesheets.has(timesheetId)) {
      this.expandedTimesheets.delete(timesheetId);
      // Also collapse all locations and projects for this timesheet
      const locationKey = timesheetId.toString();
      this.expandedLocations.delete(locationKey);
      this.expandedProjects.delete(locationKey);
    } else {
      this.expandedTimesheets.add(timesheetId);
    }
  }

  toggleLocationExpansion(timesheetId: number, locationId: number): void {
    const key = timesheetId.toString();
    if (!this.expandedLocations.has(key)) {
      this.expandedLocations.set(key, new Set());
    }
    const locationSet = this.expandedLocations.get(key)!;
    
    if (locationSet.has(locationId)) {
      locationSet.delete(locationId);
      // Also collapse all projects for this location
      const projectKey = `${timesheetId}_${locationId}`;
      this.expandedProjects.delete(projectKey);
    } else {
      locationSet.add(locationId);
    }
  }

  toggleProjectExpansion(timesheetId: number, locationId: number, projectId: number): void {
    const key = `${timesheetId}_${locationId}`;
    if (!this.expandedProjects.has(key)) {
      this.expandedProjects.set(key, new Set());
    }
    const projectSet = this.expandedProjects.get(key)!;
    
    if (projectSet.has(projectId)) {
      projectSet.delete(projectId);
    } else {
      projectSet.add(projectId);
    }
  }

  isTimesheetExpanded(timesheetId: number): boolean {
    return this.expandedTimesheets.has(timesheetId);
  }

  isLocationExpanded(timesheetId: number, locationId: number): boolean {
    const key = timesheetId.toString();
    return this.expandedLocations.has(key) && this.expandedLocations.get(key)!.has(locationId);
  }

  isProjectExpanded(timesheetId: number, locationId: number, projectId: number): boolean {
    const key = `${timesheetId}_${locationId}`;
    return this.expandedProjects.has(key) && this.expandedProjects.get(key)!.has(projectId);
  }

  /**
   * Helper methods to check if expandable
   */
  hasLocations(timesheet: any): boolean {
    return timesheet.locationSessions && timesheet.locationSessions.length > 0;
  }

  hasProjects(location: any): boolean {
    return location.projects && location.projects.length > 0;
  }

  hasActivities(project: any): boolean {
    return project.activities && project.activities.length > 0;
  }

  /**
   * Calculate total hours for location
   */
  getTotalLocationHours(location: any): string {
    if (!location.locationInTime || !location.locationOutTime) return '0.00';
    // Handle both "HH:mm" and "HH:mm:ss" formats from backend
    const inTime = moment(location.locationInTime, ['HH:mm:ss', 'HH:mm'], true);
    const outTime = moment(location.locationOutTime, ['HH:mm:ss', 'HH:mm'], true);
    
    if (!inTime.isValid() || !outTime.isValid()) {
      return '0.00';
    }
    
    const diffMinutes = outTime.diff(inTime, 'minutes');
    // Handle case where outTime is next day (night shift)
    const adjustedDiff = diffMinutes < 0 ? diffMinutes + 1440 : diffMinutes;
    return (adjustedDiff / 60).toFixed(2);
  }

  /**
   * Calculate total hours for project
   */
  getTotalProjectHours(project: any): string {
    if (!project.activities || project.activities.length === 0) return '0.00';
    const totalMinutes = project.activities.reduce((sum: number, act: any) => {
      return sum + (act.durationMinutes || 0);
    }, 0);
    return (totalMinutes / 60).toFixed(2);
  }

  /**
   * Get documents for a specific project
   */
  getDocumentsForProject(timesheet: any, projectId: number): any[] {
    if (!timesheet.documentData) return [];
    return timesheet.documentData.filter((doc: any) => doc.projectId === projectId);
  }

  /**
   * Map status integer to string
   */
  mapStatusToString(status: number): string {
    switch (status) {
      case 1: return 'Pending';
      case 2: return 'Approved';
      case 3: return 'Rejected';
      case 4: return 'Partial';
      default: return 'Unknown';
    }
  }

  /**
   * Map client approval status integer to string
   */
  mapClientApprovalStatusToString(status: number | null): string {
    if (status === null) return 'NA';
    switch (status) {
      case 1: return 'Pending';
      case 2: return 'Approved';
      case 3: return 'Rejected';
      default: return 'NA';
    }
  }

  /**
   * Get total working hours from minutes
   */
  getTotalWorkingHours(minutes: number): string {
    if (!minutes) return '0.00';
    return (minutes / 60).toFixed(2);
  }

  /**
   * Find filled document in timesheet document data
   */
  findFilledDocument(timesheet: any): any {
    if (!timesheet || !timesheet.documentData || timesheet.documentData.length === 0) {
      return null;
    }
    return timesheet.documentData.find((d: any) => d.docType === 'Filled') || null;
  }

  /**
   * Find approved document in timesheet document data
   */
  findApprovedDocument(timesheet: any): any {
    if (!timesheet || !timesheet.documentData || timesheet.documentData.length === 0) {
      return null;
    }
    return timesheet.documentData.find((d: any) => d.docType === 'Approved') || null;
  }

  /**
   * Find filled document for a specific project
   */
  findFilledDocumentForProject(timesheet: any, projectId: number): any {
    const projectDocs = this.getDocumentsForProject(timesheet, projectId);
    return projectDocs.find((d: any) => d.docType === 'Filled') || null;
  }

  /**
   * Find approved document for a specific project
   */
  findApprovedDocumentForProject(timesheet: any, projectId: number): any {
    const projectDocs = this.getDocumentsForProject(timesheet, projectId);
    return projectDocs.find((d: any) => d.docType === 'Approved') || null;
  }

  /**
   * Calculate total activity hours for a location (sum of all activities across all projects in the location)
   */
  getTotalLocationActivityHours(location: any): number {
    if (!location || !location.projects || location.projects.length === 0) {
      return 0;
    }
    
    let totalMinutes = 0;
    for (const project of location.projects) {
      if (project.activities && project.activities.length > 0) {
        for (const activity of project.activities) {
          totalMinutes += activity.durationMinutes || 0;
        }
      }
    }
    
    return totalMinutes / 60; // Convert to hours
  }

}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
