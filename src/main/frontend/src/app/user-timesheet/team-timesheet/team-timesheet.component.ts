import { LocationStrategy } from '@angular/common';
import { Component,Input,Output,EventEmitter, ElementRef, OnInit, TemplateRef, ViewChild, NgModule } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { catchError, first, map } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { BodyComponent } from 'src/app/body/body.component';
import { EmployeeClientSideIdMapping } from 'src/app/models/employeeClientSideIdMapping';
import { Feature } from 'src/app/models/feature';
import { ProjectClientSideId } from 'src/app/models/projectClientSideId';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { InputValidationService } from 'src/app/services/input-validation.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';
import { ActivatedRoute } from '@angular/router';
import { TimesheetNewService } from 'src/app/services/timesheet-new.service';
import { ProjectBasedBulkUploadPayload } from './types';
import { HttpClient } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import {LoaderService} from 'src/app/services/loader.service';import { MatSortModule } from '@angular/material/sort';
import { firstValueFrom, Observable, of, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { ExcelDownloadService } from 'src/app/services/excel-download-service';




@Component({
  standalone: false,
  selector: 'app-team-timesheet',
  templateUrl: './team-timesheet.component.html',
  styleUrls: ['./team-timesheet.component.css']
})
export class TeamTimesheetComponent implements OnInit {

  @ViewChild("night_shift_template_revampd")
  nightShiftConfirmModal: TemplateRef<any>;

  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;

  @ViewChild('fileInput') fileInput!: ElementRef;

  @ViewChild("clientSideIdForm")
  clientSideIdFormRef: TemplateRef<any>;

  @ViewChild("clientSideIdNotMandatoryFound")
  clientSideIdNotMandatoryFound: TemplateRef<any>;

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('statusModal') statusModal: any;
  @ViewChild("update_clientId")
  updateClientId: TemplateRef<any>;

  @ViewChild('compOffConfirmModal')
  compOffConfirmModal!: TemplateRef<any>;

  data: string;
  feature = "Team Timesheets";
  currentUser: User;
  userMapping: any = {};
  rejectEntries = [];
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  clientSideIdForm: NgbModalRef;
  updateClientIdModalRef: NgbModalRef;
  bulkRejectReasonIds: number[] = [];;
bulkRejectRemark: string = '';
rejectReasonError = false;
rejectRemarkError = false;

  //flags
  isAllTimesheetTable: boolean = false;
  isAllTimesheetRequestTable: boolean = false;

  //excel
  excelName = '';
  allTeamTimesheetDataForExcel: any[] = [];
  allTeamTimesheetRequestDataForExcel: any[] = [];

  //modal
  alertMessage: any;
  modalRef:NgbModalRef;
  allTeamTimesheets: any[] = [];
  allTeamTimesheetRequests: Timesheet[] = [];
  selectedTimesheetIds: number[] = [];

  // Status Count
  pendingCount = 0;
  approvedCount = 0;
  rejectedCount = 0;

  timesheetObj: Timesheet = new Timesheet();
  startDate: any;
  endDate: any;
  /** 'currentMonth' | 'previousMonth' | 'custom' - drives request date filter and visibility of custom date inputs */
  requestDateRangeType: 'currentMonth' | 'previousMonth' | 'custom' = 'currentMonth';

  isSelectAll: boolean = false;
  isSelect: boolean = false;
  bulkApprove: any = [];
  bulkReject: any = [];

  tableName: String;
  searchText: string = '';
  selectedRows: any[] = [];
  allTeamTimesheetRequestsProjectView: any[] = [];
  reporteeList: any[] = [];
  fromDate: any;
  toDate: any;
  filters: any = {};
  filters1: any = {};
  isSearchEnabled: boolean = false;
  isSearchEnabled1: boolean = false;
  timesheetApplicationsColumns: any[] = ['blank', 'blank', 'employeementId', 'employeeName', 'date', 'dayType', 'description', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'isNightShift', 'status'];
  timesheetApplicationCount: any = 0;
  allTimesheetColumns: any[] = ['blank', 'blank', 'blank', 'employmentIdAcToET', 'employeeName', 'date', 'dayType', 'description', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'isNightShift', 'status', , 'clientInTime', 'clientOutTime', 'totalClientWorkingHours', 'clientApprovalStatus', 'filledDocument', 'approvedDocument', 'createdOn'];
  allTimesheetReqColumns: any[] = ['blank', 'blank', 'employeementId', 'employeeName', 'date', 'dayType', 'description', 'createdByName', 'totalTime', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'isNightShift', 'status', 'clientInTime', 'clientOutTime', 'totalClientWorkingHours', 'clientApprovalStatus'];
  allTimesheetColumnsVMS: any[] = ['blank', 'blank', 'employmentId', 'employeeName', 'date', 'dayType', 'workCheckIn', 'workCheckOut', 'locationCount', 'projectCount', 'appliedBy', 'appliedOn', 'blank'];
  allTimesheetColumnsVMSStatusChange: any[] = [
  'blank',   // Sr No (now becomes first column)
  'employmentId',
  'employeeName',
  'date',
  'dayType',
  'workCheckIn',
  'workCheckOut',
  'locationCount',
  'projectCount',
  'appliedBy',
  'appliedOn',
  'blank'    // action column
];

  previewUrl: any;
  fileType: '' | 'pdf' | 'image' | null = null;
  docData: any;
  mimeType: any;
  selectedRejectReason: any;
  today: any;
  rejectReasons: any[] = [];
  projectId: any;
  empId: any;
  employeeTeamId: any;
  isClientSidePresent: any;
  selectedMonth: any;
  isTMBulkUpload: boolean = false;
  reporteesAndTheirProject: any[] = [];
  reportees: any[] = [];
  projects: any[] = [];
  projectRequiresClientId: Boolean = false;
  clientSideIdMandetoryFromBackend: boolean = false;
  clientSideIdNotMandatory: Boolean = false;
  clientIdNeeded: boolean = false;
  disableList: any;
  disableListFormatted: Date[] = [];
  finalFromDate: any = null;
  finalToDate: any = null;
  minDate: string;
  maxDate: string;
  isUploadAllowed: boolean = false;
  disableUploadTooltip = "Bulk upload is permitted only for the complete previous month or on the last day of the current month . Please select a date range that falls entirely within the allowed period to enable uploading. ";
  previewUrl2: SafeResourceUrl | null = null;
  fileName2: any = null;
  fileError2: string = '';
  fileType2: '' | 'pdf' | 'image' | 'excel' | null = null;
  rawObjectUrl2: string | null = null;
  selectedFile2: File | null = null;
  previewUrl1: SafeResourceUrl | null = null;
  // activePreviewUrl: SafeResourceUrl | null = null;
  activeFileType: string | null = null;
  selectedFile: File | null = null;
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  projectClientIdList: ProjectClientSideId[] = [];
  clientSideIdNotMandatoryFoundModalRef: NgbModalRef;
  alertMessageModalRef: NgbModalRef;
  zoomScale = 1;
  zoomLevel = 100;
  startX = 0;
  startY = 0;
  isAllSelected: boolean = false;
  http :any;
clientFilter: boolean = false;
safePdfUrl:SafeResourceUrl | null = null;
  documentData: any;
alertModal: TemplateRef<any>;
  maxMonth = '';
  minMonth = '';
  allProjects: {projectId: number, projectName: string}[] = [];
  projectObj: { [projectId: number]: { empId: number; name: string }[] } = {};
  minusDaysData: {minusDays: number, checkMinusDaysForBulkUpload: boolean} = {minusDays: 45, checkMinusDaysForBulkUpload: true};
  modalMessage: any;
  modalTitle: string;
  safePreviewUrl: SafeResourceUrl | null = null;
  activePreviewUrl: string | null = null;
  currentObjectUrl: string | null = null;
  isFullscreen = false;
  // Image transform state
  scale = 1;
  rotationDeg = 0;
  translateX = 0;
  translateY = 0;

// Drag state
 isDragging = false;
 dragStartX = 0;
 dragStartY = 0;
 dragOriginX = 0;
 dragOriginY = 0;
activeRawObjectUrl: string | null = null;
rejectionReasons:any;


  constructor(
    public validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private inputValidationService: InputValidationService,
    private employee360Service: Employee360Service,
    private employeeService: EmployeeService,
    private utilityService: UtilityService,
    private bodyComponent: BodyComponent,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute,
    private loaderService: LoaderService,
    private excelDownloadService: ExcelDownloadService
     


  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  //   this.safePdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
  // 'assets/Ishine_Timesheet_TNM.pdf');
  }

  async ngOnInit(): Promise<void> {
    this.searchSubject
    .pipe(debounceTime(400)) // 500ms debounce time
    .subscribe(filters => {
      this.filters = filters;
      this.page1 = 0;
      this.getMyReporteesTimesheetRequests();
    });
    const now = new Date();
    this.today = now.toISOString().split('T')[0];
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    const view = this.route.snapshot.queryParams['view'];

  if (
    view === 'requests' &&
    (this.userMapping.view_my_teams_timesheets_requests ||
     this.userMapping.update_timesheet_request ||
     this.userMapping.revoke_reportee_timesheet)
  ) {
    this.showAllTimesheetRequestsTable();
  } else {
    this.sectionViewInit(); // existing fallback
  }
    this.preventBackButton();
    this.getRejectionReason();
    this.thisMonthValidation();

    this.selectedMonth = new Date(2025, 4, 1);



    // // Set maxMonth to previous month (current month is NOT allowed for selection)
    // // Set minMonth to the month of (today - 45 days)
    // const currentMonth = now.getMonth() + 1; // 1-12
    // const currentYear = now.getFullYear();

    // // Calculate previous month (maxMonth) - current month is blocked
    // const prevMonth = currentMonth === 1 ? 12 : currentMonth - 1;
    // const prevMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;
    // this.maxMonth = `${prevMonthYear}-${String(prevMonth).padStart(2, '0')}`;

    // if(this.minusDaysData.checkMinusDaysForBulkUpload){
    //   const fortyFiveDaysAgo = new Date(now);
    //   fortyFiveDaysAgo.setDate(now.getDate() - this.minusDaysData.minusDays);
    //   const minMonthValue = fortyFiveDaysAgo.getMonth() + 1; // 1-12
    //   const minMonthYear = fortyFiveDaysAgo.getFullYear();
    //   this.minMonth = `${minMonthYear}-${String(minMonthValue).padStart(2, '0')}`;
    // } else {
    //   this.minMonth = null;
    // }

  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
   if (this.userMapping.view_my_teams_timesheets_requests || this.userMapping.update_timesheet_request || this.userMapping.revoke_reportee_timesheet) {
      this.showAllTimesheetRequestsTable();
    }
  }


getTotalDocCount(projectId: number): number {
  if (!this.selectedTimesheet?.documentData?.length) {
    return 0;
  }

  return this.selectedTimesheet.documentData
    .filter(d => d.docsProjectId === projectId)
    .reduce((count, d) => {
      let c = 0;
      if (d.docId != null) c++;               // pending
      if (d.bulkApprovedDocId != null) c++;   // approved
      return count + c;
    }, 0);
}



  disableMannualDateInput() {
    return false;
  }

  showAllTimesheetsTable() {
    this.pendingCount = null;
    this.approvedCount = null;
    this.rejectedCount = null;
    this.isAllTimesheetTable = true;

    this.isAllTimesheetRequestTable = false;
    this.isTMBulkUpload = false;
    this.getAllTeamTimesheets();
    this.page = 1;
    this.data = ''
  }

  showAllTimesheetRequestsTable() {

    this.getRejectionReason();
    this.sortColumn = 'date';
    this.sortDirection = 'DESC';
    this.isAllTimesheetRequestTable = true;
    this.isTMBulkUpload = false;
    this.isAllTimesheetTable = false;

    this.requestDateRangeType = 'currentMonth';
    this.applyRequestDateRangeAndLoad();
    this.page1 = 0;
    this.data = '';
  }

  /** Apply current/previous month or custom dates and reload request list and status counts. */
  applyRequestDateRangeAndLoad() {
    const today = new Date();
    if (this.requestDateRangeType === 'currentMonth') {
      const fromDate = new Date(today.getFullYear(), today.getMonth(), 1);
      this.startDate = moment(fromDate).format(AppComponent.DB_DATE_FORMAT);
      this.endDate = moment(today).format(AppComponent.DB_DATE_FORMAT);
    } else if (this.requestDateRangeType === 'previousMonth') {
      const prevMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);
      const lastDayPrev = new Date(today.getFullYear(), today.getMonth(), 0);
      this.startDate = moment(prevMonth).format(AppComponent.DB_DATE_FORMAT);
      this.endDate = moment(lastDayPrev).format(AppComponent.DB_DATE_FORMAT);
    }
    this.getTimesheetStatusCountsByEmpId();
    this.getMyReporteesTimesheetRequests();
  }

  /** Switch to custom date range and clear start/end so user can pick fresh. */
  switchToCustomDateRangeForRequests() {
    this.requestDateRangeType = 'custom';
    this.startDate = null;
    this.endDate = null;
  }

  showTMBulkUpload() {

    this.pendingCount = null;
    this.approvedCount = null;
    this.rejectedCount = null;

    if(this.isTMBulkUpload){
      return;
    }
    this.isAllTimesheetTable = false;
    this.isAllTimesheetRequestTable = false;
    this.isTMBulkUpload = true;
    this.resetBulkUploadForm('MONTH');
    this.resetBulkUploadForm('EMP');
    this.resetBulkUploadForm('PROJECT');
    this.resetBulkUploadForm('UPLOAD');
    this.getPreviousMinusDays();
  }

  getAllTeamTimesheets(template?: TemplateRef<any>) {
    this.allTeamTimesheets = [];

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
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Approved";
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;
    timesheetObj.empIds = this.empIds;
    console.log("timesheet obj  : ", timesheetObj)
    this.timesheetService.getMyReporteesApprovedTimesheets2(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheets = response.serviceResponse;
        this.allTeamTimesheets.forEach((x, index) => {
          x.checkId = "timesheet" + index;
          x.employmentIdAcToET = (x.employmentIdAcToET);
          x.date = (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null;
          x.officeInTime = (x.officeInTime) ? moment(x.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.officeOutTime = (x.officeOutTime) ? moment(x.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.createdOn = (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          x.emp360 = x.empId;

        });

        // console.log("allTeamTimesheets :", this.allTeamTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  validateDescription(event: any, activityObj: any): void {
    const input = event.target.value;
    const sanitizedValue = this.inputValidationService.validateInput(input, 'Rejaction Remark');

    activityObj.description = sanitizedValue;
    event.target.value = sanitizedValue; // reflect the change in the UI
  }
  validateDescription2(event: any, activityObj: any): void {
    const input = event.target.value;
    const sanitizedValue = this.inputValidationService.validateInput(input, 'Description');

    activityObj.description = sanitizedValue;
    event.target.value = sanitizedValue; // reflect the change in the UI
  }


page1: number = 0;
items: number = 10;
totalRecords: number = 0;
totalPages: number = 0;

 getMyReporteesTimesheetRequests() {

  if (this.requestDateRangeType === 'custom' && (!this.startDate || !this.endDate)) {
    this.allTeamTimesheetRequestsProjectView = [];
    this.totalRecords = 0;
    this.totalPages = 0;
    return;
  }

  this.loaderService.requestStarted();

  const payload: any = {
    empId: this.currentUser.empId,
    clientFilter: this.clientFilter,
    page: this.page1,
    size: Number(this.items),
    sortBy: this.sortColumn || 'date',
    sortDir: this.sortDirection || 'DESC',
    status: this.selectedStatus
  };

  /* 🔹 COLUMN FILTERS */
  this.addIfPresent(payload, 'employmentId', this.filters.employmentId);
  this.addIfPresent(payload, 'employeeName', this.filters.employeeName);
  this.addIfPresent(payload, 'date', this.filters.date);
  this.addIfPresent(payload, 'dayType', this.filters.dayType);
  this.addIfPresent(payload, 'projectName', this.filters.projectName);
  this.addIfPresent(payload, 'poNo', this.filters.poNo);
  this.addIfPresent(payload, 'shadowEmpName', this.filters.shadowEmp);
  this.addIfPresent(payload, 'shadowFor', this.filters.shadowFor);
  this.addIfPresent(payload, 'workCheckIn', this.filters.workCheckIn);
  this.addIfPresent(payload, 'workCheckOut', this.filters.workCheckOut);
  this.addIfPresent(payload, 'locationCount', this.filters.locationCount);
  this.addIfPresent(payload, 'projectCount', this.filters.projectCount);
  this.addIfPresent(payload, 'appliedBy', this.filters.appliedBy);
  this.addIfPresent(payload, 'appliedOn', this.filters.appliedOn);

  this.addIfPresent(payload, 'startDate', this.startDate);
  this.addIfPresent(payload, 'endDate', this.endDate);

  this.timesheetService.getMyReporteesTimesheetRequests(payload)
    .pipe(finalize(() => this.loaderService.requestEnded()))
    .subscribe((res: any) => {

      if (res.serviceStatus === 'Success') {

        this.allTeamTimesheetRequestsProjectView =
          res.serviceResponse.content || [];

        this.totalRecords =
          res.serviceResponse.totalElements || 0;

        this.totalPages =
          res.serviceResponse.totalPages || 0;
      }
    });
    //console.log(this.items);

}

onPageSizeChange() {
  this.items = Number(this.items);
  this.page1 = 0;
  this.isAllSelected = false;
  this.getMyReporteesTimesheetRequests();
}



onPageChange(event: any) {
  this.page1 = event.pageIndex;
this.items = event.pageSize;

  this.getMyReporteesTimesheetRequests(); // 🔥 BACKEND HIT
}

sortData(sort: Sort) {
  if (!sort.active || sort.direction === '') return;
 if (sort.active === 'employmentId') {
    this.sortColumn = 'employeement_Id'; // raw DB column
  } else {
    this.sortColumn = sort.active;
  }
  this.sortDirection = sort.direction.toUpperCase() as 'ASC' | 'DESC';
  this.page1 = 0;
  this.getMyReporteesTimesheetRequests();
}





  addIfPresent(payload: any, key: string, value: any) {
    if (value !== undefined && value !== null && value !== '') {
      payload[key] = value;
    }
  }


// sortData(sort: Sort) {
//   if (!sort.active) return;

//   this.sortColumn = sort.active;      // ✅ string
//   this.sortDirection = sort.direction.toUpperCase();
//   this.getMyReporteesTimesheetRequests();
// }



// onGlobalSearchChange() {
//   this.page1 = 1; // reset pagination
//   this.getMyReporteesTimesheetRequests();
// }


  /* Approve / Reject Timesheet requests */
  updateTimesheetRequestById(template: TemplateRef<any>, timesheet: Timesheet, status: any) {
    let timesheetObj = Object.assign({}, timesheet);
    timesheetObj.status = status
    timesheetObj.timesheetStatusUpdatedBy = this.currentUser.empId;
    timesheetObj.employeementId = timesheetObj.employeementId;
    timesheetObj.timesheetStatusUpdatedBy = this.currentUser.empId;
    timesheetObj.rejectionId = this.selectedRejectReason;

    this.timesheetService.updateTimesheetRequestById(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllTimesheetRequests(timesheet.empId);
        this.getAllTeamTimesheets();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  rejectTimesheetRequest(template: TemplateRef<any>) {
    this.updateTimesheetRequestById(template, this.timesheetObj, 'Rejected');
  }

  opnenRejectTimesheet(template: TemplateRef<any>, timesheet: any) {
    this.cancelRequest();
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }

  // openBulkRejectTimesheet

  openBulkRejectTimesheet(template: TemplateRef<any>) {

    this.cancelRequest();

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
    this.getRejectionReason();
  }


  /* View TImesheet details */
  getAllMyActivitiesByTimesheetId(timesheet: any) {
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

  revokeApprovedTimesheet(template: TemplateRef<any>) {
    this.cancelRequest();
    let timesheetObj = Object.assign({}, this.timesheetObj);
    this.timesheetService.revokeApprovedTimesheet(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllTimesheetsTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  exportToExcel(): void {

    if (this.isAllTimesheetTable == true) {
      this.excelName = 'AllTeamApprovedTimesheet.xlsx';

      this.allTeamTimesheetDataForExcel = this.allTeamTimesheets;
      const onlySpecificDataArr = this.allTeamTimesheetDataForExcel.map(
        x => ({
          "Employee Id": x.employmentIdAcToET,
          "Name": x.employeeName,
          "Date": x.date,
          "Day Type": x.dayType,
          "Activity": x.description?.replaceAll('<br>', ' \n'),
          "Applied By": x.createdByName,
          "Working Hours": x.totalTime,
          "Office In Time": x.officeInTime,
          "Office Out Time": x.officeOutTime,
          "Total Office Working Hours": x.totalWorkingOfficeHours,
          "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
          "Status": x.status
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

    if (this.isAllTimesheetRequestTable == true) {
      this.excelName = 'AllTeamTimeSheetRequest.xlsx'

      this.allTeamTimesheetRequestDataForExcel = this.allTeamTimesheetRequests;
      const onlySpecificDataArr = this.allTeamTimesheetRequestDataForExcel.map(
        x => ({
          "Employee Id": x.employeementId,
          "Name": x.employeeName,
          "Date": x.date,
          "Day Type": x.dayType,
          "Timesheet Details": x.description?.replaceAll('<br>', ' \n'),
          "Applied By": x.createdByName,
          "Working Hours": x.totalTime,
          "Office In Time": x.officeInTime,
          "Office Out Time": x.officeOutTime,
          "Total Office Working Hours": x.totalWorkingOfficeHours,
          "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
          "Status": x.status
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
    }
  }



  //modals
  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj: Timesheet) {
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.getAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openRevokeApprovedTimesheet(template: TemplateRef<any>, timesheet: any) {
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }

  // opnenRejectTimesheet(template: TemplateRef<any>, timesheet: any){
  //   this.timesheetObj = timesheet;
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  // }

  cancelRequest() {
    this.modalRef?.close();
  }

  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
    this.isSelectAll = false;
    this.bulkApprove = []
    this.bulkReject = []
    this.allTeamTimesheetRequests.forEach(x => {
      x.isSelected = false;
    });

  }
  handlePageChange1(event) {
    this.page1 = event;
    this.isSelectAll = false;
    this.bulkApprove = []
    this.bulkReject = []
    this.allTeamTimesheetRequestsProjectView.forEach(x => {
      x.isSelected = false;
    });

  }
  handleItemsChange(event) {
    this.items = event;

  }


  //sorting timesheet
  // sortData(sort: Sort) {
  //   //console.log(sort);
  //   if (sort.active) {
  //     let sortParams: any[] = sort.active?.split("|");
  //     this.sortColumn = sortParams[0];
  //     this.sortColumnType = sortParams[1];
  //     this.sortDirection = sort.direction;
  //   }
  // }

  selectAll(event) {
    this.bulkApprove = [];
    this.bulkReject = [];

    const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
    checkboxes.forEach((checkbox: any) => {
      //console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedTimesheet = this.allTeamTimesheetRequests.find((_timesheet, index) => _timesheet.checkId == checkboxIndex);

      if (event.target.checked) {
        checkbox.checked = true;
        this.bulkApprove.push(checkedTimesheet);
        this.bulkReject.push(checkedTimesheet);
      } else {
        checkbox.checked = false;
        this.bulkApprove.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkApprove.splice(index, 1);
        });
        this.bulkReject.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkReject.splice(index, 1);
        });
      }
    });
  }

  selectAllTimesheet(event) {
    this.bulkApprove = [];
    this.bulkReject = [];

    const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
    checkboxes.forEach((checkbox: any) => {
      //console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedTimesheet = this.allTeamTimesheets.find((_timesheet, index) => _timesheet.checkId == checkboxIndex);

      if (event.target.checked) {
        checkbox.checked = true;
        this.bulkApprove.push(checkedTimesheet);
        this.bulkReject.push(checkedTimesheet);
      } else {
        checkbox.checked = false;
        this.bulkApprove.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkApprove.splice(index, 1);
        });
        this.bulkReject.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkReject.splice(index, 1);
        });
      }
    });
  }

  select(timesheetObj, event) {

    //console.log("clicked on : ", timesheetObj);

    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkApprove.push(timesheetObj);
      this.bulkReject.push(timesheetObj);
    } else {
      event.target.classList.remove('checked');
      const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
      if (checkboxes.length !== this.items) this.isSelectAll = false
      this.bulkApprove.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkApprove.splice(index, 1);
      });
      this.bulkReject.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkReject.splice(index, 1);
      });
    }

    //console.log("Updated Bulk List : ", this.bulkApprove);
  }

  openBulkApprovalModal(nightShiftTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    //console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.open(nightShiftTemplate, { modalDialogClass: 'modal-lg' });
    } else {
      this.bulkApproveByIds();
    }
  }

  bulkApproveWithoutNightShiftRequest(template: TemplateRef<any>) {
    this.bulkApprove = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if (this.bulkApprove.length !== 0) {
      this.bulkApproveByIds();
    } else {
      this.cancelRequest();
      this.showAllTimesheetRequestsTable();
    }
  }

  // openBulkRejectModal(nightShiftTemplate: TemplateRef<any>, bulkRejectTimesheet: TemplateRef<any>) {

  //   this.selectedRejectReason = '';
  //   this.timesheetObj.rejectReason = '';

  //   const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
  //   //console.log(isNightShiftFound, " : isNightShiftFound");

  //   if (isNightShiftFound.length != 0) {
  //     this.modalRef = this.modalService.open(nightShiftTemplate, { modalDialogClass: 'modal-lg' });
  //   } else {
  //     this.openBulkRejectTimesheet(bulkRejectTimesheet);
  //   }
  // }

  bulkRejectWithoutNightShiftRequest(bulkRejectTimesheet: TemplateRef<any>) {
    this.timesheetObj.rejectReason = null;
    this.bulkReject = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if (this.bulkReject.length !== 0) {
      this.openBulkRejectTimesheet(bulkRejectTimesheet);
    } else {
      this.cancelRequest();
      this.showAllTimesheetRequestsTable();
    }
  }

  // onBulkApproval(template: TemplateRef<any>) {
  //   this.cancelRequest();
  //   //console.log("Updated Bulk List : ", this.bulkApprove);
  //   let timesheetObj = new Timesheet();
  //   timesheetObj.bulkApprovedList = this.bulkApprove;
  //   timesheetObj.updatedBy = this.currentUser.empId;
  //   timesheetObj.status = "Approved"
  //   //console.log("For Bulk Update : ", timesheetObj);
  //   timesheetObj.bulkApprovedList.forEach((x) => {
  //     x.employeementId = x.employeementId.substring(2)
  //   })
  //   const empId = timesheetObj.bulkApprovedList.length > 0
  //     ? timesheetObj.bulkApprovedList[0].empId
  //     : null;
  //   console.log("test", empId);
  //   this.timesheetService.bulkApproveTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, "All Selected Timesheets Approved Successfully ");
  //       this.showAllTimesheetRequests(empId);
  //       this.bulkApprove = [];
  //       this.bulkReject = [];
  //     } else {
  //       console.error(response.serviceResponse)
  //     }
  //   });

  // }

  // OnBulkReject(template: TemplateRef<any>) {
  //   //console.log("Updated Bulk List : ", this.bulkReject);
  //   let timesheetObj = new Timesheet();
  //   timesheetObj.bulkRejectList = this.bulkReject;
  //   timesheetObj.updatedBy = this.currentUser.empId;
  //   timesheetObj.status = "Rejected"
  //   timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
  //   timesheetObj.rejectionId = this.selectedRejectReason;
  //   //console.log("For Bulk Update : ", timesheetObj);
  //   timesheetObj.bulkRejectList.forEach((y) => {
  //     y.employeementId = y.employeementId.substring(2);
  //   })
  //   this.timesheetService.bulkRejectTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, "All Selected Timesheets Rejected Successfully ");
  //       this.showAllTimesheetRequestsTable();
  //       this.bulkApprove = [];
  //       this.bulkReject = [];
  //     } else {
  //       console.error(response.serviceResponse)
  //     }
  //   });

  // }

  getDoscForPreview(docId: any) {
    console.log(docId, ":docId");
    this.timesheetService.getDocumentDataByDocId(docId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log(response.serviceResponse);
        this.docData = response.serviceResponse.docData;
        console.log(typeof (this.docData), ":docDataType")
        this.mimeType = response.serviceResponse.docMimeType
        this.showPreview(this.docData, this.mimeType)
      }
    });
  }
  getFinalDocumentDataByDocId(timesheetId: any, docId: any) {
    console.log(docId, ":docId");
    this.timesheetService.getFinalDocumentDataByDocId(timesheetId, docId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log(response.serviceResponse);
        this.docData = response.serviceResponse.docData;
        console.log(typeof (this.docData), ":docDataType")
        this.mimeType = response.serviceResponse.docMimeType
        this.showPreview(this.docData, this.mimeType)
      }
    });
  }
  showPreview(base64Data: string, mimeType: string) {
    const dataUrl = `data:${mimeType};base64,${base64Data}`;
    this.resetPreviewState();
    this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.fileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.fileType = 'image';
    } else {
      this.fileType = '';
    }


    // Open modal
    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
      this.getMyReporteesTimesheetRequests();
    this.getTimesheetStatusCountsByEmpId();
    }

  }

  toggleSearch1() {
    this.isSearchEnabled1 = !this.isSearchEnabled1;
    if (!this.isSearchEnabled1) {
      this.filters1 = {};
    }

  }

  onSearch1(searchData) {
    this.filters1 = searchData;
    console.log("Updated Filter : ", this.filters);
  }
  // onSearch(searchData: any) {
  //   this.filters = searchData;
  //   this.page1 = 0;
  //   this.getMyReporteesTimesheetRequests();
  // }

  onSearch(searchData: any) {
    this.searchSubject.next(searchData);
  }

  getRejectionReason() {

    this.timesheetService.getRejectionReason().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.rejectReasons = response.serviceResponse;
        console.log("test", this.rejectReasons);
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  onMonthYearChange() {
    this.resetBulkUploadForm('MONTH');
    this.getMyReporteesAndTheirProjects();

    const [year, month] = this.timesheetObj.monthYear.split('-');
    const selectedYear = Number(year);
    const selectedMonth = Number(month);

    const now = new Date();

    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    const prevMonth = currentMonth === 1 ? 12 : currentMonth - 1;
    const prevMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

    const lastDay = new Date(selectedYear, selectedMonth, 0).getDate();
    this.maxDate = `${year}-${month}-${lastDay.toString().padStart(2, '0')}`;

    if (this.minusDaysData?.checkMinusDaysForBulkUpload) {

      const minusDays =
        this.minusDaysData.minusDays && this.minusDaysData.minusDays > 0
          ? this.minusDaysData.minusDays
          : 45;

      const expectedDate = new Date(now);
      expectedDate.setDate(now.getDate() - minusDays);

      const expectedYear = expectedDate.getFullYear();
      const expectedMonth = expectedDate.getMonth() + 1;

      // Rule: current month never allowed
      if (selectedYear === currentYear && selectedMonth === currentMonth) {
        throw new Error('Current month is not allowed');
      }

      // Rule: determine allowed start day
      const isPreviousMonth =
        expectedYear === prevMonthYear &&
        expectedMonth === prevMonth;

      const startDay = isPreviousMonth ? '01' : selectedMonth === expectedMonth ? '15' : '01';

      this.minDate = `${year}-${month}-${startDay}`;

    } else {
      this.minDate = null;
    }
  }




  getMyReporteesAndTheirProjects() {
      this.timesheetObj.managerId = this.currentUser.empId;

      this.timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(this.timesheetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.reporteesAndTheirProject = response.serviceResponse;

          const projectMap = new Map<number, { empId: number; name: string }[]>();

          for (const employee of this.reporteesAndTheirProject) {
            const empId = employee.empId;
            const empName = employee.name;

            if (employee.projectList) {
              for (const project of employee.projectList) {
                const projectId = project.projectId;

                if (projectId) {
                  if (!projectMap.has(projectId)) {
                    projectMap.set(projectId, []);
                  }

                  projectMap.get(projectId).push({ empId, name: empName });
                }
              }
            }
          }

          console.log("Project Map:", projectMap);

          this.projectObj = Object.fromEntries(projectMap);
          const projectsWithDetails = response.serviceResponse.flatMap(employee =>
            employee.projectList?.map(project => ({
              projectId: project.projectId,
              projectName: project.projectName
            })) || []
          );

          // Remove duplicates
          const uniqueProjects: { projectId: number; projectName: string }[] = Array.from(
            new Map<number, { projectId: number; projectName: string }>(
              projectsWithDetails
                .filter(p => p.projectId)
                .map(p => [p.projectId, p])
            ).values()
          );

          this.allProjects = uniqueProjects;
          console.log("All Projects:", this.allProjects);


          // console.log("Project Object:", this.projectObj);

        } else {
          console.error(response.serviceResponse);
        }
      });
    }

 onReporteeChange(empIds: number[]) {
    //  this.resetBulkUploadForm('EMP');
    // const selectedEmp = this.reporteesAndTheirProject.find(
    //   emp => emp.empId === empId
    // );

    console.log("Empids: ", empIds);


    this.timesheetObj.projectId = this.timesheetObj.selectedProjectId;
    this.timesheetObj.empIds = empIds;
  }


  onProjectSelectBulk(projectId: any) {
    // this.resetBulkUploadForm('PROJECT');

    this.timesheetObj.empIds = null;

    this.reportees = this.reporteesAndTheirProject.map(rp =>{
      if(rp.projectId === projectId){
        return rp;
      }
    })

    // this.checkIfProjectRequiresClientId(projectId);
    // this.getAllDisabledDateListForBulkDocSubmit(projectId);

  }

  getAllDisabledDateListForBulkDocSubmit(projectId: any) {
    if (projectId != null) {
      this.timesheetService.getAllDisabledDateListForBulkDocSubmit(projectId, this.timesheetObj.selectedEmpId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.disableList = response.serviceResponse;
          this.disableListFormatted = this.disableList.map(d => new Date(d));
        }
      });
    }
    else {

    }

  }

  checkIfProjectRequiresClientId(projectId: any) {
    // this.hasClientSideId = false;

    this.timesheetService.checkIfProjectRequiresClientId(projectId).pipe(first()).subscribe(async (response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectRequiresClientId = response.serviceResponse;
        if (this.projectRequiresClientId) {
          //is client id mandetory api call.
          // this.timesheetService.isClientMandetory(+projectId).pipe(first()).subscribe((response: any) => {
          //   if (response.serviceStatus == "Success") {
          //     this.clientSideIdMandetoryFromBackend = response.serviceResponse;
          //   }
          // });
          response = await this.timesheetService.isClientMandetory(+projectId).pipe(first()).toPromise();
          this.clientSideIdMandetoryFromBackend = response.serviceResponse;

          this.clientSideIdNotMandatory = false;
          this.timesheetObj.clientSideId = null;
          this.timesheetObj.hasClientSideId = true;
          this.clientIdNeeded = true;
          this.onProjectRequiresClientId(projectId, this.timesheetObj.selectedEmpId);
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

  fetchEmploymentIdByEmpId() {
    this.timesheetService.fetchEmploymentIdByEmpId(this.timesheetObj.selectedEmpId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.employmentId = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
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

  checkUploadEligibility() {
    if (!this.finalFromDate || !this.finalToDate) {
      this.isUploadAllowed = false;
      this.disableUploadTooltip = "Please select a valid date range!";
      return;
    }

    // const from = new Date(this.finalFromDate);
    // const to = new Date(this.finalToDate);
    // const today = new Date();

    // const currentMonth = today.getMonth();
    // const currentYear = today.getFullYear();

    // const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
    // const prevMonthYear = currentMonth === 0 ? currentYear - 1 : currentYear;

    // const fortyFiveDaysAgo = new Date(today);
    // fortyFiveDaysAgo.setDate(today.getDate() - this.minusDaysData.minusDays);
    // const minMonthValue = fortyFiveDaysAgo.getMonth();
    // const minMonthYear = fortyFiveDaysAgo.getFullYear();

    // const isPreviousMonthSelection =
    //   from.getMonth() === prevMonth &&
    //   to.getMonth() === prevMonth &&
    //   from.getFullYear() === prevMonthYear &&
    //   to.getFullYear() === prevMonthYear;

    // const isOlderMonthSelection =
    //   from.getMonth() === minMonthValue &&
    //   to.getMonth() === minMonthValue &&
    //   from.getFullYear() === minMonthYear &&
    //   to.getFullYear() === minMonthYear;

    //   if(this.minusDaysData.checkMinusDaysForBulkUpload){
    //     this.isUploadAllowed = isPreviousMonthSelection || isOlderMonthSelection;
    //   } else {
      //   }
    this.isUploadAllowed = true;

    this.disableUploadTooltip = this.isUploadAllowed
      ? ""
      : `Bulk upload is permitted only for the previous month (from 1st) or older months within ${this.minusDaysData.minusDays} days (from 15th). Please select a valid date range.`;

      if(this.finalToDate < this.finalFromDate ){
        this.finalToDate = null;
      }
  }


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

  finalFile:File|null = null;
  async onFinalFileSelected(event: any): Promise<void> {
    const file: File = event.target.files[0];
    this.finalFile = file;
    this.fileError2 = '';
    this.previewUrl2 = null;
    this.fileType2 = null;

    if (!file) return;

    // also "xlsx","xls"
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel','application/vnd.oasis.opendocument.spreadsheet'];
    const maxSize = 500 * 1024;

    if (!allowedTypes.includes(file.type)) {
      this.fileError2 = 'Only PDF, JPG, JPEG, PNG, XLSX, and XLS files are allowed.';
      this.finalFile = null;
      return;
    }

    if (file.size > maxSize) {
      this.fileError2 = 'File size must be 500Kb or less.';
      this.finalFile = null;
      return;
    }

    if (this.rawObjectUrl2) {
      URL.revokeObjectURL(this.rawObjectUrl2);
    }

    const objectUrl = URL.createObjectURL(file);
    this.rawObjectUrl2 = objectUrl;
    // this.fileType2 = file.type === 'application/pdf' ? 'pdf' : 'image';
    const excelTypes = [
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ];
    // ✅ Detect file type ONCE
    this.fileType2 =
    file.type === 'application/pdf' ? 'pdf' :
    excelTypes.includes(file.type) ? 'excel' : 'image';
    if(this.fileType2 == 'image' || this.fileType2 == 'pdf'){
      this.previewUrl2 = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
    }
    else{
      this.previewUrl2 = null;
    }
    this.selectedFile2 = await this.renameFile(file, this.timesheetObj.projectId, 'Approved');
    console.log("selectedFile2: ",this.selectedFile2);
    this.fileName2 = file.name;
  }


  openPreviewModalForTwo(docType: 'doc1' | 'doc2'): void {
    this.openPreview();
    this.previewUrl = docType === 'doc1' ? this.previewUrl1 : this.previewUrl2;
    this.fileType = docType === 'doc1'
      ? (this.selectedFile?.type === 'application/pdf' ? 'pdf' : 'image')
      : (this.selectedFile2?.type === 'application/pdf' ? 'pdf' : 'image');

    this.modalRef = this.modalService.open(this.previewModal, {modalDialogClass: 'modal-lg' });
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
    console.log(this.selectedFile2);
    if (this.selectedFile2 != null && this.finalFromDate != null && this.finalToDate != null && this.currentUser.empId != null) {
      // this.timesheetService.bulkFinalDocumentUpload(this.selectedFile2, this.finalFromDate, this.finalToDate, this.timesheetObj.selectedEmpId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      //   if (response.serviceStatus === "Success") {
      //     this.resetBulkUploadForm('UPLOAD');

      //     this.openAlertMod(template, response.serviceResponse);
      //   } else {
      //     this.openAlertMod(template, response.serviceResponse);
      //   }
      // });

      const payload: ProjectBasedBulkUploadPayload = {
        createdBy: this.currentUser.empId,
        empIds: this.timesheetObj.empIds,
        fromDate: this.finalFromDate,
        toDate: this.finalToDate,
        projectId: this.timesheetObj.projectId,
      }
      console.log("This payload: ",payload);

      this.timesheetService.bulkFinalUploadProjectBased(payload, this.selectedFile2).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.resetBulkUploadForm('UPLOAD');
          this.alertMessage = "Success";
          this.openAlertMod(template, response.serviceResponse);
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
      this.timesheetObj.selectedEmpId = null;
      this.timesheetObj.selectedProjectId = null;
      this.reportees = [];
      this.projects = [];
    }

    if (level === 'EMP') {
      this.timesheetObj.selectedProjectId = null;
      this.projects = [];
    }

    if (level === 'PROJECT') {


    }

    if (level === 'UPLOAD') {
      this.timesheetObj.monthYear = null;
      this.timesheetObj.selectedEmpId = null;
      this.timesheetObj.selectedProjectId = null;
      this.reportees = [];
      this.projects = [];
    }
  }



  async onProjectRequiresClientId(projectId: any, empId: any) {
    this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.clientSideId = response.serviceResponse;
        if (this.timesheetObj.clientSideId) {
          this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;

        }
      } else {
        console.error(response.serviceResponse);
        this.empClientSideObj.clientSideId = null;
        this.timesheetObj.clientSideId = null;

      }
      if (this.timesheetObj.clientSideId == null && this.projectRequiresClientId) {
        this.getActiveProjectsAndClientSideIdByEmpId();
        this.empClientSideObj.projectId = projectId;

        this.openClientSideIdForm();


      }
    });

  }




  openClientSideIdForm() {
    this.empClientSideObj.clientSideId = '';
    this.clientSideIdForm = this.modalService.open(this.clientSideIdFormRef, {modalDialogClass: 'modal-lg' });
  }

  getProjectName(projectId: number): string {
    const project = this.projectClientIdList?.find(p => p.projectId === projectId);
    return project ? project.projectName : '';
  }

  hideClientSideIdForm() {
    this.clientSideIdForm?.close();
  }

  onCancelClientSideId(template: TemplateRef<any>) {
    this.getClientSideIdByProjectIdAndEmpId(this.timesheetObj.projectId, this.timesheetObj.selectedEmpId);
    this.hideClientSideIdForm();
    this.openclientSideIdNotMandatoryFound(template);
  }

  openclientSideIdNotMandatoryFound(template: TemplateRef<any>) {
    this.clientSideIdNotMandatoryFoundModalRef = this.modalService.open(template, {modalDialogClass: 'modal-md' });
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


  getActiveProjectsAndClientSideIdByEmpId() {
    var empId: any;

    empId = this.timesheetObj.selectedEmpId;

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
    // this.resetTimeonDayTypeChange();
  }






  searchTextReportee = '';
  filteredReportees: any[] = [];
  empIds: any[] = [];
  isAllReporteesSelected = false;

  getMyReportees() {
    this.timesheetObj.managerId = this.currentUser.empId;
    this.timesheetService.getMyReportees(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList = response.serviceResponse;
        this.filteredReportees = this.reporteeList;
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  compareById(item1: any, item2: any): boolean {
    return item1 === item2;
  }


  updateSelectedSkillNames() {
    const selectedReportees = this.reporteeList.filter(reportee =>
      this.empIds.includes(reportee.empId)
    );


  }

  resetSkillSearch() {
    this.searchTextReportee = '';
    this.filteredReportees = [...this.reporteeList];
  }

  filterSkills() {
    const lower = this.searchTextReportee.toLowerCase();
    const selectedIds = this.empIds || [];
    this.filteredReportees = this.reporteeList.filter(reportee =>
      reportee.employeeName.toLowerCase().includes(lower) ||
      selectedIds.includes(reportee.empId)
    );
  }

  clearSkills(event: Event) {
    event.stopPropagation();
    this.empIds = [];
    this.isAllReporteesSelected = false;
    this.searchTextReportee = '';
    this.filteredReportees = [...this.reporteeList];
  }

  toggleSelectAllSkills() {
    this.isAllReporteesSelected = !this.isAllReporteesSelected;
    this.empIds = this.isAllReporteesSelected
      ? this.filteredReportees.map(r => r.empId)
      : [];
  }



  showDropdown: boolean = false;
  suggestionList: any[] = [];
  hideDropdown(): void {

    setTimeout(() => (this.showDropdown = false), 150);
  }
  filteredData() {
    let data = this.allTeamTimesheetRequestsProjectView || [];

    if (this.searchText && this.searchText.trim()) {
      const search = this.searchText.toLowerCase().trim();

      data = data.filter(item =>
        /* ✅ EMPLOYEE NAME */
        item.employeeName?.toLowerCase().includes(search)

        /* ✅ EMP ID (NUMBER OR STRING BOTH) */
        || item.empId?.toString().toLowerCase().includes(search)
        || item.employeementId?.toString().toLowerCase().includes(search)

        /* OTHER FIELDS */
        || item.clientSideId?.toLowerCase().includes(search)
        || item.departmentName?.toLowerCase().includes(search)
        || item.projectName?.toLowerCase().includes(search)
        || item.poNo?.toLowerCase().includes(search)
        || item.teamName?.toLowerCase().includes(search)
      );
    }

    /* COLUMN FILTER (toggle search row) */
    if (this.filters && Object.keys(this.filters).length) {
      Object.keys(this.filters).forEach(key => {
        const value = this.filters[key];
        if (value) {
          data = data.filter(item =>
            item[key]?.toString().toLowerCase().includes(value.toLowerCase())
          );
        }
      });
    }

    return data;
  }


  filterSuggestions(): void {
    if (!this.searchText) {
      this.suggestionList = [];
      return;
    }

    const lowerSearchText = this.searchText.toLowerCase();

    const employeeMatches = this.allTeamTimesheetRequestsProjectView
      .filter(item =>
        item.name?.toLowerCase().includes(lowerSearchText) ||
        item.employeementId?.toString().toLowerCase().includes(lowerSearchText) ||
        item.clientSideId?.toLowerCase().includes(lowerSearchText)
      )
      .map(item => ({
        display: `${item.name} (${item.employeementId})`,
        value: item.name,
        type: 'employee'
      }));

    const projectMatches = this.allTeamTimesheetRequestsProjectView
      .filter(item =>
        item.teamName?.toLowerCase().includes(lowerSearchText) ||
        item.projectName?.toLowerCase().includes(lowerSearchText) ||
        item.poNo?.toLowerCase().includes(lowerSearchText)
      )
      .map(item => ({
        display: item.projectName,
        value: item.projectName,
        type: 'project'
      }));

    const departmentMatches = this.allTeamTimesheetRequestsProjectView
      .filter(item =>
        item.departmentName?.toLowerCase().includes(lowerSearchText)
      )
      .map(item => ({
        display: item.departmentName,
        value: item.departmentName,
        type: 'department'
      }));

    // Merge and deduplicate suggestions
    const seen = new Set();
    this.suggestionList = [...employeeMatches, ...projectMatches, ...departmentMatches].filter(item => {
      if (seen.has(item.display)) return false;
      seen.add(item.display);
      return true;
    });
  }

  selectSuggestion(item: any): void {
    this.searchText = item.value;
    this.showDropdown = false;
  }


  updateSelectedRows(row: any) {
    this.selectedRows = this.filteredData().filter(r => r.selected);
    console.log("test", this.selectedRows);
  }

  toggleAllRows(event: any) {
    const checked = event.target.checked;

    this.filteredData().forEach((timesheet: any) => {
      timesheet.selected = checked;
      this.onEmployeeToggle(timesheet); // 🔥 hierarchy call
    });

    this.updateSelectedRows(null);
  }


  clearAllSelections() {
    this.filteredData().forEach(r => r.selected = false);
    this.selectedRows = [];
  }

  // bulkApprove1(template: TemplateRef<any>) {
  //   let timesheetObj = new Timesheet();
  //   timesheetObj.status = "Approved"
  //   const rawData = this.selectedRows;
  //   const empDetails = rawData.map(item => ({
  //     empId: item.empId,
  //     teamId: item.teamId,
  //     status: "Approved",
  //     timesheetStatusUpdatedBy: this.currentUser.empId
  //   }));

  //   const payload = {
  //     pendingApprovalList: empDetails
  //   };
  //   this.timesheetService.approveTimesheetRequest(payload).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.clearAllSelections();
  //       this.openAlertMod(template, response.serviceResponse);
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });

  // }

  getSelectedTimesheetIds(): number[] {
    return this.selectedRows
      .map(row => row.timesheetId)
      .filter(id => id != null);
  }
  getSelectedTimesheets(): any[] {
  return this.selectedRows.filter(row => row?.timesheetId != null);
}


  // bulkReject1(template: TemplateRef<any>) {
  //   const rawData = this.selectedRows;
  //   const empDetails = rawData.map(item => ({
  //     empId: item.empId,
  //     teamId: item.teamId,
  //     status: "Rejected",
  //     timesheetStatusUpdatedBy: this.currentUser.empId,
  //     rejectionId: this.selectedRejectReason,
  //     rejectReason: this.timesheetObj.rejectReason
  //   }));
  //   const payload = {
  //     pendingApprovalList: empDetails
  //   };
  //   this.timesheetService.approveTimesheetRequest(payload).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.clearAllSelections();
  //       this.openAlertMod(template, "Timesheets rejected successfully");
  //     } else {
  //       this.openAlertMod(template, "Timesheets rejection failed");
  //     }
  //   });
  // }



  showTimesheetRequests(template: TemplateRef<any>, details: any): void {
    this.isAllTimesheetRequestTable = true;
    this.allTeamTimesheetRequests = [];
    let timesheetObj = new Timesheet();
    timesheetObj.empId = details.empId;
    timesheetObj.teamId = details.teamId;
    this.employeeTeamId = details.teamId;
    this.isClientSidePresent = details.clientSideId;
    timesheetObj.fromDate = "";
    timesheetObj.toDate = "";
    this.empId = details.empId;
    this.projectId = details.projectId;
    if (this.toDate != null && this.fromDate != null) {
      timesheetObj.fromDate = this.fromDate;
      timesheetObj.toDate = this.toDate;
    }

    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        this.allTeamTimesheetRequests.forEach((timesheet, index) => {
          timesheet.checkId = "timesheet" + index;
          timesheet.employeementId = "A-".concat(timesheet.employeementId);
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        this.allTeamTimesheetRequests.forEach(timesheet => {
          timesheet.emp360 = timesheet.empId;
          timesheet.emp360CreatedBy = timesheet.createdBy;
        });
        //console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
      } else {
        console.error(response.serviceResponse)
      }
    });
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    // Load timesheets for this employee
    // this.loadAllTeamTimesheetRequests(employeementId);
  }
  showAllTimesheetRequests(details: any) {
    this.allTeamTimesheetRequests = [];
    let timesheetObj = new Timesheet();
    timesheetObj.empId = details;
    timesheetObj.teamId = this.employeeTeamId;
    if (this.toDate != null && this.fromDate != null) {
      timesheetObj.fromDate = this.fromDate;
      timesheetObj.toDate = this.toDate;
    }
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        this.allTeamTimesheetRequests.forEach((timesheet, index) => {
          timesheet.checkId = "timesheet" + index;
          timesheet.employeementId = "A-".concat(timesheet.employeementId);
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        this.allTeamTimesheetRequests.forEach(timesheet => {
          timesheet.emp360 = timesheet.empId;
          timesheet.emp360CreatedBy = timesheet.createdBy;
        });
      } else {
        console.error(response.serviceResponse)
      }
    });
  }



  onBulkRejectTimesheet(template: TemplateRef<any>) {
    this.timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();

    if (!this.validationService.validateActivityTimesheetDiscription(this.timesheetObj.rejectReason)) {
      this.alertMessage = "Please enter Valid Reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    let timesheetObj = new Timesheet();
    timesheetObj.bulkRejectList = this.bulkReject;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
    timesheetObj.status = "Rejected"
    timesheetObj.rejectionId = this.selectedRejectReason;
    timesheetObj.bulkRejectList.forEach((item) => {
      item.employeementId = item.employeementId;
    })
    this.timesheetService.bulkRejectTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Rejected Successfully ");
        this.bulkApprove = [];
        this.bulkReject = [];
        // this.timesheetApplicationCount;
        // this.countMyReporteesTimesheetRequests();
        this.getMyReporteesTimesheetRequests();
        this.getAllTeamTimesheets();
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  updateClientSideIdMapping(template: TemplateRef<any>) {
    if (!this.empClientSideObj.clientSideId || this.empClientSideObj.clientSideId.trim() === '') {
      this.openAlertMod(template, 'Please enter a valid Client Side ID.');
      return;
    }

    if (this.clientSideIdMandetoryFromBackend) {
      if (this.empClientSideObj.clientSideId.toLowerCase().startsWith("na")) {
        this.modalRef?.close();
        this.empClientSideObj.clientSideId = '';
        this.openAlertMod(template, 'As per the configuration defined by your project manager, Client IDs for this project cannot begin with “NA”. Kindly provide the valid Client ID assigned to you. For additional assistance, please reach out to your project manager.');
        return;
      }
    }
    if (this.timesheetObj.timesheetAppliedFor == 'team') {
      console.log("Timesheet obj : ", this.timesheetObj);
      this.empClientSideObj.empId = this.timesheetObj.empId;
    }
    else {
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

  resetUpdateClientSideId() {
    this.empClientSideObj = new EmployeeClientSideIdMapping();
  }


  hideclientSideIdNotMandatoryFound(): void {
    if (this.clientSideIdNotMandatoryFoundModalRef) {
      this.clientSideIdNotMandatoryFoundModalRef?.close();
    }
  }



  openSelfModal3(template: TemplateRef<any>) {
    this.empClientSideObj.clientSideId = '';
    this.empClientSideObj.projectId = this.timesheetObj.projectId;
    this.updateClientIdModalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
    this.getActiveProjectsAndClientSideIdByEmpId();
  }


  opnenbulkRejectTimesheet(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }





  prepareFlatTimesheetData(timesheets: any[]): any[] {
    const flatList: any[] = [];

    timesheets.forEach(timesheet => {
      (timesheet.locationSessions || []).forEach(location => {
        (location.projects || []).forEach(project => {
          (project.activities || []).forEach(activity => {

            flatList.push({
              // ===== TIMESHEET LEVEL =====
              "Employee Id": timesheet.employmentId,
              "Employee Name": timesheet.employeeName,
              "Date": timesheet.date,
              "Day Type": timesheet.dayType,
              "Applied By": timesheet.appliedBy,
              "Applied On": timesheet.appliedOn,

              // ===== LOCATION LEVEL =====
              "Work Location": location.workLocationType,
              "Location In": location.locationInTime,
              "Location Out": location.locationOutTime,

              // ===== PROJECT LEVEL =====
              "Project Name": project.projectName,
              "PO No": project.poNo || '-',
              "Shadow Employee": project.shadowEmp ? 'Yes' : 'No',

              // ===== ACTIVITY LEVEL =====
              "Activity Team": activity.teamName,
              "Activity": activity.activity,
              "Description": activity.activityDescription || '-',
              "Hours (Minutes)": activity.durationMinutes
            });

          });
        });
      });
    });

    return flatList;
  }


  exportExcel1() {
    this.excelName = 'Timesheet_Flat_Report.xlsx';

    const flatData = this.prepareFlatTimesheetData(
      this.filteredData()
    );

    if (!flatData.length) {
      return;
    }

    const wb = XLSX.utils.book_new();
    const sheet = XLSX.utils.json_to_sheet(flatData);

    XLSX.utils.book_append_sheet(wb, sheet, 'Timesheet Data');
    XLSX.writeFile(wb, this.excelName);
  }

  resetDateFilter() {
    this.toDate = '';
    this.fromDate = '';
    this.getMyReporteesTimesheetRequests();
  }




  get transformStyle() {
    if (this.zoomScale <= 1) {
      return `scale(${this.zoomScale})`; // no translate when normal
    }
    return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.zoomScale})`;
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


expandedTimesheetIndex: number | null = null;
expandedProjectKey: string | null = null;

toggleProject(tIndex: number,l: number, pIndex: number):void {
  const key = `${tIndex}-${l}-${pIndex}`;
  this.expandedProjectKey =
    this.expandedProjectKey === key ? null : key;
}
/* EMPLOYEE ACCORDION */

/* MAIN ACCORDION */
toggleAccordion(index: number,timesheet:any): void {
  if (this.expandedTimesheetIndex === index) {
    this.expandedTimesheetIndex = null;
    this.expandedProjectKey = null;   // close project accordion
     this.selectedTimesheet = null;
  } else {
    this.expandedTimesheetIndex = index;
    this.expandedProjectKey = null;   // reset project when switching employee
     this.selectedTimesheet = timesheet;
  }
}





   onEmployeeToggle(timesheet: any) {
    if (!timesheet.locationSessions) return;

    timesheet.locationSessions.forEach((loc: any) => {
      if (!loc.projects) return;

      loc.projects.forEach((project: any) => {
        project.isSelected = timesheet.selected;
      });
    });
  }



  onProjectToggle(timesheet: any, project: any) {
    const allProjects =
      timesheet.locationSessions
        ?.flatMap((l: any) => l.projects || []) || [];

    // Parent (Timesheet) checked ONLY if all projects checked
    timesheet.selected =
      allProjects.length > 0 &&
      allProjects.every((p: any) => p.isSelected === true);
  }


viewDoc(docUrl: string) {
  if (!docUrl) return;

  window.open(docUrl, '_blank');
}

isDocPopupOpen = false;

closeDocumentPopup() {
  this.isDocPopupOpen = false;
  this.selectedTimesheet = null;
}


/* =======================
   DOCUMENT VIEWER POPUP
   ======================= */

   @ViewChild('documentViewerModal') documentViewerModal!: TemplateRef<any>;
   @ViewChild('documentViewerModalToggle') documentViewerModalToggle: TemplateRef<any>;

   activeDocProject: any = null;
   activeDocType: 'Pending' | 'Approved' = 'Pending';
   isToggleMode: boolean = false;
   activePreviewFile: any = null;
   selectedTimesheet: any = null;

   /* OPEN POPUP */
openDocumentPopup(
  timesheet: any,
  docType: 'Pending' | 'Approved',
  toggleMode: boolean = true,
  project?: any
): void {
  console.log("Opening document");
  this.selectedTimesheet = timesheet;
  this.isToggleMode = toggleMode;
  console.log("Project",project);

  if (project) {

    this.activeDocProject = project;
  } else {
    const firstDoc = timesheet.documentData?.[0];
    if (firstDoc) {
      this.activeDocProject = this.getProjectById(firstDoc.docsProjectId, timesheet);
    } else {
      this.activeDocProject = undefined;
    }
  }

  this.activeDocType =  docType ? docType : 'Pending';


if (toggleMode) {

  this.modalRef = this.modalService.open(
    this.documentViewerModalToggle,
    { modalDialogClass: 'modal-xl', backdrop: 'static' }
  );

  this.getDocument(this.activeDocType);

} else {

  this.modalRef = this.modalService.open(
    this.documentViewerModal,
    { modalDialogClass: 'modal-xl', backdrop: 'static' }
  );

  this.loadActiveDocument();
}
}

getProjectById(projectId: number, timesheet: any) {
  for (const loc of timesheet.locationSessions || []) {
    const proj = loc.projects?.find(p => p.projectId === projectId);
    if (proj) return proj;
  }
  return undefined;
}



   /* PROJECT LIST */
   getUniqueProjectsFromTimesheet(timesheet: any): any[] {
     const map = new Map<number, any>();

     (timesheet.locationSessions || []).forEach(loc => {
       (loc.projects || []).forEach(proj => {
         map.set(proj.projectId, proj);
       });
     });

     return Array.from(map.values());
   }

   /* LEFT PROJECT CLICK */
   selectProjectForDoc(project: any, type: 'Pending' | 'Approved'): void {
     this.activeDocProject = project;
     this.switchDocType(type);
    //  this.setDefaultDocForProject();
   }

   /* TOGGLE PENDING / APPROVED */
   switchDocType(type: 'Pending' | 'Approved'): void {
     this.activeDocType = type;
     this.loadActiveDocument();
   }

   /* DEFAULT DOC */
   setDefaultDocForProject(): void {
     if (!this.activeDocProject) {
       this.activePreviewFile = null;
       return;
     }

     if (this.hasApprovedDoc(this.activeDocProject.projectId)) {
       this.activeDocType = 'Approved';
     } else if (this.hasPendingDoc(this.activeDocProject.projectId)) {
       this.activeDocType = 'Pending';
     } else {
       this.activePreviewFile = null;
       return;
     }

     this.loadActiveDocument();
   }

   /* LOAD DOC */
loadActiveDocument(): void {

  if (!this.selectedTimesheet) {
    this.safePdfUrl = null;
    return;
  }

  this.getDocument(this.activeDocType);

}


   /* HELPERS */
hasPendingDoc(projectId: number): boolean {
  return this.selectedTimesheet?.documentData?.some(
    d => d.docId != null && d.docsProjectId === projectId
  ) || false;
}

hasApprovedDoc(projectId: number): boolean {
  return this.selectedTimesheet?.documentData?.some(
    d => d.bulkApprovedDocId != null && d.docsProjectId === projectId
  ) || false;
}


getPendingCount(projectId: number): number {
  // Count number of pending docs for this project
  return this.selectedTimesheet?.documentData?.filter(
    d => d.docsProjectId === projectId && d.docId != null
  ).length || 0;
}

getApprovedCount(projectId: number): number {
  // Count number of approved docs for this project
  return this.selectedTimesheet?.documentData?.filter(
    d => d.docsProjectId === projectId && d.bulkApprovedDocId != null
  ).length || 0;
}


  getFilledStatus(projectId: number): string {

  const docs = this.selectedTimesheet?.documentData || [];

  const hasPending = docs.some(d => d.docId != null);
  const hasApproved = docs.some(d => d.bulkApprovedDocId != null);

  if (hasPending && hasApproved) return 'Pending , Approved';
  if (hasPending) return 'Pending';
  if (hasApproved) return 'Approved';

  return '';
}

  //  getDocument(projectId: number, type: 'Pending' | 'Approved'): any {
  //    return this.selectedTimesheet?.doumentData?.find(d =>
  //      d.projectId === projectId &&
  //      (type === 'Pending' ? !d.finalFlag : d.finalFlag)
  //    );
  //  }


// getDocument(type: 'Pending' | 'Approved'): void {

//   const doc = this.selectedTimesheet?.documentData?.find(d =>
//     type === 'Pending' ? !d.finalFlag : d.finalFlag
//   );
//   if (!doc?.docId) {
//     this.activePreviewFile = null;
//     this.safePdfUrl = null;
//     return;
//   }


//   if (type === 'Pending') {
//     console.log("Pending DocId:", doc.docId);
//   } else if (type === 'Approved') {

//     console.log("Approved bulkApprovedDocId:", doc.bulkApprovedDocId);
//   }


//   this.timesheetNewService
//     .getDocumentById(doc.docId, doc?.finalFlag)
//     .subscribe({
//       next: (blob: Blob) => {

//         const fileURL = URL.createObjectURL(blob);

//         this.safePdfUrl =
//           this.sanitizer.bypassSecurityTrustResourceUrl(fileURL);

//       },
//       error: err => {
//         console.error("Document fetch failed", err);
//         this.activePreviewFile = null;
//       }
//     });
// }


// getDocument(type: 'Pending' | 'Approved'): void {

//   const doc = this.selectedTimesheet?.documentData[0];


//   if (!doc?.docId) {
//     this.activePreviewFile = null;
//     this.safePdfUrl = null;
//     return;
//   }


//   let docType: boolean = doc.finalFlag;


//   let docIdToSend: number = doc.docId;

//   if (type === 'Approved' && doc.bulkApprovedDocId) {
//     docIdToSend = doc.bulkApprovedDocId;
//   }


//   if (type === 'Pending') {
//     console.log("Pending DocId:", doc.docId);
//   } else {
//     docType = true;
//     console.log("Approved bulkApprovedDocId:", doc.bulkApprovedDocId);
//   }


//   this.timesheetNewService
//     .getDocumentById(docIdToSend, docType)
//     .subscribe({
//       next: (blob: Blob) => {

//         const fileURL = URL.createObjectURL(blob);

//         this.safePdfUrl =
//           this.sanitizer.bypassSecurityTrustResourceUrl(fileURL);

//       },
//       error: err => {
//         console.error("Document fetch failed", err);
//         this.activePreviewFile = null;
//       }
//     });
// }




getDocument(type: 'Pending' | 'Approved'): void {
 if (!this.selectedTimesheet || !this.activeDocProject) {
    this.clearPreview();
    return;
  }

  const doc = this.selectedTimesheet.documentData?.find(d => d.docsProjectId === this.activeDocProject.projectId);

  if (!doc) {
    console.log(`No document found for project ${this.activeDocProject.projectId}`);
    this.clearPreview();
    return;
  }

  const docIdToSend = type === 'Approved' ? doc.bulkApprovedDocId : doc.docId;

  if (!docIdToSend) {
    console.log(`No ${type} document available for project ${this.activeDocProject.projectId}`);
    this.clearPreview();
    return;
  }



  this.safePdfUrl=null;

  // Fetch document from backend
  this.timesheetNewService.getDocumentById(docIdToSend, type === 'Approved')
    .subscribe({
      next: (blob: Blob) => {
        this.clearPreview();
        this.activeRawObjectUrl = URL.createObjectURL(blob);
        const mime = (blob?.type || '').toLowerCase();
        this.currentObjectUrl = URL.createObjectURL(blob);
        const excelTypes = [
          'application/vnd.ms-excel',
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        ];
        if (mime.includes('pdf')) {
          this.activeFileType = 'pdf';
          this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.currentObjectUrl);
        } else if (mime.startsWith('image/')) {
          this.activeFileType = 'image';
          this.activePreviewUrl = this.currentObjectUrl;
        } else if(excelTypes.includes(mime)) {
          this.excelDownloadService.openConfirmAndDownload(blob,null);
        } 
        else {
          // fallback: try showing as pdf (some backends send application/octet-stream)
          // if it fails in iframe, user can still download
          this.activeFileType = 'pdf';
          this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.currentObjectUrl);
        }
      },
      error: err => {
        console.error("Document fetch failed", err);
        this.safePdfUrl = null;
      }
    });
}

  //  Navigation CARDS

   selectedStatus: number = 1;
   private searchSubject = new Subject<any>();
   onStatusChange(status: number) {
    this.selectedStatus = status;
    console.log("Status changed to:", this.selectedStatus);
    this.page1 = 0; // pagination reset
    this.isSearchEnabled = false;
    this.expandedTimesheetIndex = null;
    this.expandedProjectKey = null;
    this.filters = {};
    this.getMyReporteesTimesheetRequests();
    this.getTimesheetStatusCountsByEmpId();
  }


  getTimesheetStatusCountsByEmpId() {
    const payload : any = {
      managerId: this.currentUser.empId,
      clientFilter: this.clientFilter,
      startDate: this.startDate,
      endDate: this.endDate
    };

    this.loaderService.requestStarted();

    this.timesheetNewService
  .getMyReporteesTimesheetRequestsCount(payload)
  .pipe(finalize(() => this.loaderService.requestEnded()))
  .subscribe({
    next: (res: any) => {
      console.log("Status Count Response", res);
      if (res && res.serviceResponse) {
        this.pendingCount = 0;
        this.approvedCount = 0;
        this.rejectedCount = 0;
        res.serviceResponse.forEach((item: any) => {
          const status = item[0]?.toLowerCase();
          const count = Number(item[1]) || 0;
          if (status === 'pending') {
            this.pendingCount = count;
          }
          else if (status === 'approved') {
            this.approvedCount = count;
          }
          else if (status === 'rejected') {
            this.rejectedCount = count;
          }
        });
      }
    }
  });
}

selectedProjects: any[] = [];
selectedRejectReasons: any[] = [];
rejectRemark: string = '';

projectList: any[] = [];

// openRejectPopup(template: any, timesheet: any) {
//   this.selectedTimesheet = timesheet;
//   this.selectedProjects = [];
//   this.selectedRejectReasons = [];
//   this.rejectRemark = '';

//   // Flatten project list from timesheet (UI only)
//   this.projectList =
//     timesheet?.locationSessions?.flatMap((l: any) => l.projects) || [];

//   this.modalRef = this.modalService.open(template, {
//     modalDialogClass: 'modal-lg',
//     backdrop: 'static'
//   });

// }

  // BULK APPROVAL
  bulkApproveByIds(confirmNightShift: boolean = false, ids?: number[]) {

    // const timesheetIds  = ids || this.getSelectedTimesheetIds();
     const selectedTimesheets = ids
    ? this.getSelectedTimesheets().filter(ts => ids.includes(ts.timesheetId))
    : this.getSelectedTimesheets();

    console.log("selectedTimesheets",  this.getSelectedTimesheets());


  const timesheetIds = selectedTimesheets.map(ts => ts.timesheetId);
    if (!timesheetIds.length) return;

    const hasCompOff = selectedTimesheets.some(ts =>
      ts.dayType?.toLowerCase() === 'comp off'
    );

    if (hasCompOff) {

      const modalRef = this.modalService.open(this.compOffConfirmModal, { centered: true });

      modalRef.result.then((result) => {
        if (result === 'APPROVE') {
          this.executeBulkApprove(selectedTimesheets,confirmNightShift);
        }
      }).catch(() => {});

      return;
    }

    this.executeBulkApprove(selectedTimesheets,confirmNightShift);


  }


// BULK REJECT (FIXED)
// BulkRejectByIds(
//   template: TemplateRef<any>,
//   rejectReason: string
// ) {

//   const timesheetIds = this.getSelectedTimesheetIds();

//   if (!timesheetIds.length) {
//     this.openAlertMod(template, 'No timesheets selected');
//     return;
//   }

//   if (!rejectReason || !rejectReason.trim()) {
//     this.openAlertMod(template, 'Reject reason is required');
//     return;
//   }

//   const payload = {
//     timesheetIds,
//     projectIds: [],   // ⭐ ADD THIS

//     status: 'REJECTED',
//     updatedBy: this.currentUser.empId,
//     rejectReason: rejectReason.trim(),
//     rejectRemark: rejectReason.trim()
//   };

//   this.timesheetNewService
//     .bulkRejectTimesheetsByIds1(payload)
//     .subscribe({
//       next: (res: any) => {
//         if (res?.serviceStatus === 'Success') {
//           this.clearAllSelections();
//           this.modalRef?.close();
//           this.getMyReporteesTimesheetRequests();
//           this.openAlertMod(
//             template,
//             res.serviceResponse || 'Timesheets rejected successfully'
//           );
//         } else {
//           this.openAlertMod(
//             template,
//             res?.serviceResponse || 'Bulk rejection failed'
//           );
//         }
//       },
//       error: () => {
//         this.openAlertMod(template, 'Bulk rejection failed');
//       }
//     });


// }



openBulkRejectModal(bulkRejectTimesheet: TemplateRef<any>): void {
  this.selectedRejectReason = '';
  this.timesheetObj.rejectReason = '';

  this.modalRef = this.modalService.open(
    bulkRejectTimesheet,
    { modalDialogClass: 'modal-lg', backdrop: 'static' }
  );
}
hasClientApprovalPending(timesheet: any): boolean {

  if (!timesheet?.locationSessions?.length) {
    return false;
  }

  return timesheet.locationSessions.some((location: any) =>
    (location.projects || []).some((project: any) =>
      Number(project.clientApprovalStatus) === 1
    )
  );

}

// SINGLE TIMESHEET APPROVE
approveSingleTimesheet(timesheet: any) {

  if (!timesheet?.timesheetId) {
    alert('Invalid timesheet');
    return;
  }

  const payload = {
    timesheetIds: [timesheet.timesheetId],
    status: 'APPROVED',
    updatedBy: this.currentUser.empId,
    rmId: this.currentUser.empId
  };

  this.loaderService.requestStarted();

  this.timesheetNewService
    .bulkApproveTimesheetsByIds1(payload)
    .pipe(finalize(() => this.loaderService.requestEnded()))
    .subscribe({
      next: (res: any) => {
         this.modalTitle = 'Result';
        let message = '';

        if (res?.serviceStatus === 'Success') {

          const processed = res?.serviceResponse?.processed || [];
          const skipped = Array.isArray(res?.serviceResponse?.skipped)
            ? res.serviceResponse.skipped
            : [];

          if (processed.length) {
            message += `${processed.length} timesheet(s) approved successfully.\n`;
          }

          if (skipped.length) {

              message += `
                <p><strong>Skipped Timesheets</strong></p>
                <table class="table table-bordered table-sm">
                  <thead>
                    <tr>
                      <th>EMP ID</th>
                      <th>Date</th>
                      <th>Reason</th>
                    </tr>
                  </thead>
                  <tbody>
              `;

              skipped.forEach((item: any) => {
                message += `
                  <tr>
                    <td>${item.employmentId}</td>
                    <td>${item.date}</td>
                    <td>${item.reason}</td>
                  </tr>
                `;
              });

              message += `</tbody></table>`;
            }

          this.clearAllSelections();
          // this.selectedStatus = 2;
          this.onStatusChange(2);
          this.page1 = 0;

          this.getMyReporteesTimesheetRequests();
          this.getTimesheetStatusCountsByEmpId();
          this.modalMessage = message;

        } else {
           this.modalTitle = 'Error';
          this.modalMessage =
            res?.serviceResponse || 'Timesheet approval failed';
        }
        this.modalService.open(this.statusModal, { centered: true });

      },
      error: (err) => {
        this.modalTitle = 'Error';

        this.modalMessage =
          err?.error?.message ||
          err?.error?.serviceResponse ||
          'Timesheet approval failed';

        this.modalService.open(this.statusModal, { centered: true });
      }
    });
}

// Single reject state
selectedTimesheetForReject: any = null;
singleRejectReason: string = '';

openSingleRejectModal(
  template: TemplateRef<any>,
  timesheet: any
): void {

  this.selectedTimesheetForReject = timesheet;
  this.singleRejectReason = '';

  this.modalRef = this.modalService.open(
    template,
    { modalDialogClass: 'modal-md', backdrop: 'static' }
  );
}
getAvailableProjects(currentIndex: number) {
  // Get all selected project IDs except current row
  const selectedProjectIds = this.rejectEntries
    .filter((_, index) => index !== currentIndex)
    .flatMap(entry => entry.projectIds || []);

  // Return projects that are NOT already selected
  return this.projectList.filter(
    project => !selectedProjectIds.includes(project.projectId)
  );
}
canAddMoreProjects(): boolean {
  const allSelectedProjectIds = this.rejectEntries
    .flatMap(entry => entry.projectIds || []);

  const uniqueSelected = [...new Set(allSelectedProjectIds)];

  return uniqueSelected.length < this.projectList.length;
}
// SINGLE TIMESHEET REJECT
// submitSingleReject() {

//   if (!this.selectedTimesheetForReject?.timesheetId) {
//     alert('Invalid timesheet');
//     return;
//   }

//   if (!this.singleRejectReason || !this.singleRejectReason.trim()) {
//     alert('Reject reason is required');
//     return;
//   }

//   const payload = {
//     timesheetIds: [this.selectedTimesheetForReject.timesheetId],
//     projectIds: [],   // ⭐ ADD THIS
//     status: 'REJECTED',
//     updatedBy: this.currentUser.empId,
//     rejectReason: this.singleRejectReason.trim(),
//     rejectRemark: this.singleRejectReason.trim()
//   };

//   this.timesheetNewService
//     .bulkRejectTimesheetsByIds1(payload)
//     .subscribe({
//       next: (res: any) => {
//         if (res?.serviceStatus === 'Success') {
//           this.modalRef?.close(); // ✅ close ONLY current modal
//           this.clearAllSelections();
//           this.getMyReporteesTimesheetRequests();
//           alert(res.serviceResponse || 'Timesheet rejected successfully');
//         } else {
//           alert(res?.serviceResponse || 'Timesheet rejection failed');
//         }
//       },
//       error: () => {
//         alert('Timesheet rejection failed');
//       }
//     });
// }


// Single Project Approve

approveSingleProject(timesheet: any, project: any, location: any) {

  if (!project?.projectId || !location?.locationMappingId) {
    return;
  }
  const payload = {
    timesheetId: timesheet.timesheetId,
    locationMappingId: location.locationMappingId,
    projectIds: [project.projectId],
    status: 'APPROVED',
    updatedBy: this.currentUser.empId
  };
  this.timesheetNewService
    .approveRejectProjects(payload)
    .subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.getMyReporteesTimesheetRequests();
          alert(res.serviceResponse || 'Project approved successfully');
        } else {
          alert(res?.serviceResponse || 'Project approval failed');
        }
      },
      error: () => {
        alert('Project approval failed');
      }
    });
}

// Single project Reject

selectedProjectForReject: any = null;
selectedProjectTimesheet: any = null;
selectedProjectLocation: any = null;
singleProjectRejectReason: string = '';

submitSingleProjectReject() {

  if (
    !this.selectedProjectForReject?.projectId ||
    !this.selectedProjectLocation?.locationMappingId ||
    !this.selectedProjectTimesheet?.timesheetId
  ) {
    alert('Invalid project data');
    return;
  }

  if (!this.singleProjectRejectReason.trim()) {
    alert('Reject reason is required');
    return;
  }

  const payload = {
    timesheetId: this.selectedProjectTimesheet.timesheetId,
    locationMappingId: this.selectedProjectLocation.locationMappingId,
    projectIds: [this.selectedProjectForReject.projectId],
    status: 'REJECTED',
    updatedBy: this.currentUser.empId,
    rmId: this.currentUser.empId,
    rejectReason: this.singleProjectRejectReason.trim()
  };

  this.timesheetNewService
    .approveRejectProjects(payload)
    .subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.getMyReporteesTimesheetRequests();
          alert(res.serviceResponse || 'Project rejected successfully');
        } else {
          alert(res?.serviceResponse || 'Project rejection failed');
        }
      },
      error: () => {
        alert('Project rejection failed');
      }
    });
}

openSingleProjectRejectModal(
  template: TemplateRef<any>,
  timesheet: any,
  project: any,
  location: any,
  event: MouseEvent
): void {

  event.stopPropagation();
  this.selectedProjectForReject = project;
  this.selectedProjectTimesheet = timesheet;
  this.selectedProjectLocation = location;
  this.singleProjectRejectReason = '';

  this.modalRef = this.modalService.open(
    template,
    { modalDialogClass: 'modal-md', backdrop: 'static' }
  );
}

getReporteesFromProjectId(): { empId: number; name: string }[] {
  const projectId = this.timesheetObj.selectedProjectId;

  return this.projectObj[projectId] ?? [];

}

  getPreviousMinusDays() {
    this.timesheetService.getPreviousMinusDays().subscribe({
      next: (response: any) => {

        this.minusDaysData = response.serviceResponse || {
          checkMinusDaysForBulkUpload: true,
          minusDays: 45
        };

        if(response.serviceError != null ){
          this.openAlertMod(this.alertTemplate, `Document upload is only valid for past ${this.minusDaysData.minusDays} days`);
        }

        const now = new Date();

        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth();

        const prevMonthDate = new Date(currentYear, currentMonth - 1, 1);
        this.maxMonth = `${prevMonthDate.getFullYear()}-${String(prevMonthDate.getMonth() + 1).padStart(2, '0')}`;

        if (this.minusDaysData.checkMinusDaysForBulkUpload) {

          const minusDays = this.minusDaysData.minusDays && this.minusDaysData.minusDays > 0
            ? this.minusDaysData.minusDays
            : 45;

          const expectedDate = new Date(now);
          expectedDate.setDate(now.getDate() - minusDays);

          const expectedYear = expectedDate.getFullYear();
          const expectedMonth = expectedDate.getMonth();

          this.minMonth = `${expectedYear}-${String(expectedMonth + 1).padStart(2, '0')}`;

          const isPreviousMonth =
            expectedYear === prevMonthDate.getFullYear() &&
            expectedMonth === prevMonthDate.getMonth();

          const minDateObj = isPreviousMonth
            ? new Date(expectedYear, expectedMonth, 1)
            : new Date(expectedYear, expectedMonth, 15);

          const maxDateObj = new Date(expectedYear, expectedMonth + 1, 0);

          this.minDate = this.toDateString(minDateObj);
          this.maxDate = this.toDateString(maxDateObj);

        } else {
          this.minMonth = null;
          this.minDate = null;
          this.maxDate = null;
        }

      },
    });
  }

  private toDateString(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  // renameFile(file: File, projectId: number, docType: 'Filled' | 'Approved'): File {
  //   const ext = file.name.substring(file.name.lastIndexOf('.'));
  //   const safeDocType = docType.toLowerCase(); // optional
  //   const newFileName = `${projectId}_${safeDocType}_${file.name}`;

  //   return new File([file], newFileName, { type: file.type });
  // }


  //   if (!this.selectedTimesheet?.timesheetId) {
  //     alert("Invalid timesheet");
  //     return;
  //   }

  //   if (!this.selectedRejectReasons?.length) {
  //     alert("Select at least one reject reason");
  //     return;
  //   }

  //   if (!this.selectedProjects?.length) {
  //     alert("Select at least one project");
  //     return;
  //   }
  //   const projectIds = this.selectedProjects
  //     .map(p => typeof p === 'object' ? Number(p.projectId) : Number(p))
  //     .filter(x => !!x);

  //   const rejectReasonString = this.selectedRejectReasons.map(r => {

  //     if (typeof r === 'object') return r.rejectionReason;

  //     const match = this.rejectReasons.find(x => x.rejectionId === r);
  //     return match?.rejectionReason;

  //   }).filter(Boolean).join(', ');

  //   const rejectRemarkText =
  //     this.timesheetObj.rejectReason?.trim() || '';

  //   const payload = {
  //     timesheetIds: [Number(this.selectedTimesheet.timesheetId)],
  //     projectIds: projectIds,
  //     status: 'REJECTED',
  //     updatedBy: Number(this.currentUser.empId),

  //     rejectReason: rejectReasonString,
  // rejectRemark: this.timesheetObj.rejectReason?.trim() || ''
  //   };



  //   this.timesheetNewService
  //     .bulkRejectTimesheetsByIds1(payload)
  //     .subscribe({
  //       next: (res: any) => {
  //          if (res?.serviceStatus === 'Success') {

  //         this.modalRef?.close();
  //         this.getMyReporteesTimesheetRequests();

  //         this.statusModal(
  //           'Success',
  //           res.serviceResponse || 'Timesheet rejected successfully'
  //         );

  //       } else {
  //         this.statusModal(
  //           'Error',
  //           res?.serviceResponse || 'Timesheet rejection failed'
  //         );
  //       }
  //     },
  //     error: () => {
  //       this.statusModal('Error', 'Something went wrong');
  //     }
  //   });
  // }




  // openRejectPopup(template: any, timesheet: any) {

  //   this.selectedTimesheet = timesheet;
  //   this.selectedProjects = [];
  //   this.selectedRejectReasons = [];
  //   this.rejectRemark = '';

  //   /* ⭐ FLATTEN + REMOVE DUPLICATES */
  //   const map = new Map();

  //   (timesheet?.locationSessions || []).forEach((loc: any) => {
  //     (loc.projects || []).forEach((proj: any) => {
  //       map.set(proj.projectId, proj);
  //     });
  //   });

  //   this.projectList = Array.from(map.values());

  //   console.log("Project List →", this.projectList); // ⭐ CHECK

  //   this.modalRef = this.modalService.open(template, {
  //     modalDialogClass: 'modal-lg',
  //     backdrop: 'static'
  //   });
  // }
  submitSingleTimesheetReject() {

  if (!this.selectedTimesheet?.timesheetId) {
    this.modalTitle = 'Error';
    this.modalMessage = 'Invalid timesheet';
    this.modalService.open(this.statusModal, { centered: true });
    return;
  }

  const projectRejections = this.rejectEntries.map(entry => ({

    projectIds: (entry.projectIds || []).map((p: any) =>
    typeof p === 'object' ? p.projectId : p
  ),

  rejectionIds: entry.reasons.map((r: any) =>
    typeof r === 'object' ? r.rejectionId : r
  ),

  rejectRemark: entry.remark?.trim() || ''

}));

  const payload = {
    timesheetIds: [Number(this.selectedTimesheet.timesheetId)],
    status: 'REJECTED',
    rmId: Number(this.currentUser.empId),
    updatedBy: Number(this.currentUser.empId),
    projectRejections
  };

  this.loaderService.requestStarted();

  this.timesheetNewService
    .bulkRejectTimesheetsByIds1(payload)
    .pipe(finalize(() => this.loaderService.requestEnded()))
    .subscribe({
      next: (res: any) => {
      this.modalTitle = 'Result';
        let message = '';
      if (res?.serviceStatus === 'Success') {
        const processed = res?.serviceResponse?.processed || [];
          const skipped = Array.isArray(res?.serviceResponse?.skipped)
            ? res.serviceResponse.skipped
            : [];

          if (processed.length) {
            message += `${processed.length} timesheet(s) rejected successfully.\n`;
          }

          if (skipped.length) {

          message += `
            <p><strong>Skipped Timesheets</strong></p>
            <table class="table table-bordered table-sm">
              <thead>
                <tr>
                  <th>EMP ID</th>
                  <th>Date</th>
                  <th>Reason</th>
                </tr>
              </thead>
              <tbody>
          `;

          skipped.forEach((item: any) => {
            message += `
              <tr>
                <td>${item.employmentId}</td>
                <td>${item.date}</td>
                <td>${item.reason}</td>
              </tr>
            `;
          });

          message += `</tbody></table>`;
        }

          this.modalRef?.close();
          this.page1 = 0;
          // this.onStatusChange(3);
          // this.getMyReporteesTimesheetRequests();
          // this.getTimesheetStatusCountsByEmpId();
          this.modalMessage = message;
      } else {
          this.modalTitle = 'Error';
          this.modalMessage =
            res?.serviceResponse || 'Timesheet rejection failed';

      }
       this.modalService.open(this.statusModal, { centered: true });
      },
      error: () => {
        this.modalTitle = 'Error';
        this.modalMessage = 'Timesheet rejection failed';
        this.modalService.open(this.statusModal, { centered: true });
      }
    });
    this.getTimesheetStatusCountsByEmpId();
}


  openRejectPopup(template: any, timesheet: any) {

  this.selectedTimesheet = timesheet;

  const map = new Map();

  (timesheet?.locationSessions || []).forEach((loc: any) => {
    (loc.projects || []).forEach((proj: any) => {
      map.set(proj.projectId, proj);
    });
  });

  this.projectList = Array.from(map.values());

  this.rejectEntries = [
    {
      projectIds: [],
      reasons: [],
      remark: ''
    }
  ];

  if (this.projectList.length === 1) {
    this.rejectEntries[0].projectIds = [this.projectList[0].projectId];
  }

  console.log("Project List →", this.projectList);

  this.modalRef = this.modalService.open(template, {
    modalDialogClass: 'modal-lg',
    backdrop: 'static'
  });
}
addRejectRow() {
  this.rejectEntries.push({
    projectIds: [],
    reasons: [],
    remark: ''
  });
}

removeRejectRow(index: number) {
  this.rejectEntries.splice(index, 1);
}
isRejectFormValid(): boolean {
  return this.rejectEntries.every(entry =>
    entry.projectIds?.length &&
    entry.reasons?.length &&
    entry.remark?.trim().length > 0
  );
}


onClientFilterChange() {
  this.page1 = 0;
  this.getMyReporteesTimesheetRequests();
  this.getTimesheetStatusCountsByEmpId();
}

hasPreview(): boolean {
  return !!this.safePreviewUrl || !!this.activePreviewUrl;
}
get imageTransform(): string {
    return `
    translate(-50%, -50%)
    translate(${this.translateX}px, ${this.translateY}px)
    scale(${this.scale})
    rotate(${this.rotationDeg}deg)
  `;
  }
clearPreview(): void {
  this.isFullscreen = false;
  this.safePreviewUrl = null;
  this.activePreviewUrl = null;
  this.activeFileType = 'unknown';
  this.resetTransformations();

  if (this.currentObjectUrl) {
    URL.revokeObjectURL(this.currentObjectUrl);
    this.currentObjectUrl = null;
  }
}
zoomIn(): void {
  console.log("Zoomed in");
  this.scale = Math.min(this.scale + 0.2, 3);
}

zoomOut(): void {
  this.scale = Math.max(this.scale - 0.2, 0.5);
}

rotate(): void {
  this.rotationDeg = (this.rotationDeg + 90) % 360;
}

resetTransformations(): void {
  this.scale = 1;
  this.rotationDeg = 0;
  this.translateX = 0;
  this.translateY = 0;
  this.isDragging = false;
}

/* ---------------- DRAG / PAN ---------------- */

// startDrag(event: MouseEvent): void {
//   if (this.activeFileType !== 'image') return;

//   this.isDragging = true;
//   this.dragStartX = event.clientX;
//   this.dragStartY = event.clientY;
//   this.dragOriginX = this.translateX;
//   this.dragOriginY = this.translateY;
// }

 startDrag(event: MouseEvent): void {
    this.isDragging = true;
    this.startX = event.clientX - this.translateX;
    this.startY = event.clientY - this.translateY;
  }

onDrag(event: MouseEvent): void {
  if (!this.isDragging || this.activeFileType !== 'image') return;
    this.translateX = event.clientX - this.startX;
    this.translateY = event.clientY - this.startY;
}

  stopDrag(): void {
    this.isDragging = false;
  }

/* ---------------- FULLSCREEN ---------------- */

toggleFullscreen(): void {
  this.isFullscreen = !this.isFullscreen;

  // Optional UX: reset drag when toggling
  // this.resetTransformations();
}

/* ---------------- DOWNLOAD ---------------- */

downloadActiveFile(fileName = 'image-preview'): void {

  // Ensure we have a raw object URL
  if (!this.activeRawObjectUrl || this.activeFileType !== 'image') return;

  const link = document.createElement('a');
  link.href = this.activeRawObjectUrl;
  link.download = fileName;
  link.click();
}

openRejectReasonsModal(data: any, template: TemplateRef<any>) {
  this.rejectionReasons = data;
  this.modalService.open(
    template,
    { modalDialogClass: 'modal-lg', backdrop: 'static' }
  );
}

executeBulkApprove(selectedTimesheets: any[],confirmNightShift: boolean) {
  const timesheetIds = selectedTimesheets.map(ts => ts.timesheetId);
  const payload = {
    timesheetIds,
    status: 'APPROVED',
    updatedBy: this.currentUser.empId,
    rmId : this.currentUser.empId,
    confirmNightShift
  };

  this.loaderService.requestStarted();

  this.timesheetNewService
    .bulkApproveTimesheetsByIds1(payload)
    .pipe(finalize(() => this.loaderService.requestEnded()))
    .subscribe({
      next: (res: any) => {
        const response = res?.serviceResponse;

        if (response?.requiresNightShiftConfirmation) {

          const modalRef = this.modalService.open(this.nightShiftConfirmModal, { centered: true });

          modalRef.result.then((result) => {

            if (result === 'YES') {
              this.bulkApproveByIds(true, timesheetIds);
            } else {
              this.bulkApproveByIds(true, response.normalTimesheets);
            }

          }).catch(() => {});

          return;
        }
        this.modalTitle = 'Result';
      let message = '';
        if (res?.serviceStatus === 'Success') {

        const processed = res?.serviceResponse?.processed || [];
        const skipped = Array.isArray(res?.serviceResponse?.skipped)
          ? res.serviceResponse.skipped
          : [];

        if (processed.length) {
            message += `<p><strong>${processed.length} timesheet(s) approved successfully.</strong></p>`;
          }

        if (skipped.length) {

            message += `
              <p><strong>Skipped Timesheets</strong></p>
              <table class="table table-bordered table-sm">
                <thead>
                  <tr>
                    <th>EMP ID</th>
                    <th>Date</th>
                    <th>Reason</th>
                  </tr>
                </thead>
                <tbody>
            `;

            skipped.forEach((item: any) => {
              message += `
                <tr>
                  <td>${item.employmentId}</td>
                  <td>${item.date}</td>
                  <td>${item.reason}</td>
                </tr>
              `;
            });

            message += `</tbody></table>`;
          }


        this.clearAllSelections();
        this.onStatusChange(2);
        this.page1 = 0;
        this.getMyReporteesTimesheetRequests();
        this.getTimesheetStatusCountsByEmpId();
        this.modalMessage =message;

      } else {
          this.modalTitle = 'Error';
          this.modalMessage =
          res?.serviceResponse || 'Bulk approval failed';
        }
        this.modalService.open(this.statusModal, { centered: true });
      }

    });

}
openBulkRejectPopup(modal: any, ids?: number[]) {

  const selectedTimesheets = ids
  ? this.getSelectedTimesheets().filter(ts => ids.includes(ts.timesheetId))
  : this.getSelectedTimesheets();

  const timesheetIds = selectedTimesheets.map(ts => ts.timesheetId);



  this.selectedTimesheetIds = timesheetIds;

  this.bulkRejectReasonIds = null;
  this.bulkRejectRemark = '';

  this.modalService.open(modal, {
  centered: true,
  modalDialogClass: 'modal-lg',
  backdrop: 'static'
  });
}

confirmBulkReject(modal: any) {

 this.rejectReasonError = !this.bulkRejectReasonIds || this.bulkRejectReasonIds.length === 0;
  this.rejectRemarkError = !this.bulkRejectRemark || !this.bulkRejectRemark.trim();

   if (this.rejectReasonError || this.rejectRemarkError) {
    return;
  }

const payload = {timesheetIds: this.selectedTimesheetIds,
                status: "REJECTED",
                updatedBy: this.currentUser.empId,
                rmId: this.currentUser.empId,
                rejectMode: "BULK",
                rejectionReasonId: this.bulkRejectReasonIds,
                rejectRemark: this.bulkRejectRemark};

this.loaderService.requestStarted();

this.timesheetNewService.processBulkTimesheets(payload).pipe(finalize(() => this.loaderService.requestEnded())).subscribe({next: (res: any) => {

    modal.close();

    this.modalTitle = 'Result';
    let message = '';

    if (res?.serviceStatus === 'Success') {

      const processed = res?.serviceResponse?.processed || [];
      const skipped = Array.isArray(res?.serviceResponse?.skipped)
        ? res.serviceResponse.skipped
        : [];

      if (processed.length) {
        message += `<p><strong>${processed.length} timesheet(s) rejected successfully.</strong></p>`;
      }

      if (skipped.length) {

        message += `
          <p><strong>Skipped Timesheets</strong></p>
          <table class="table table-bordered table-sm">
            <thead>
              <tr>
                <th>EMP ID</th>
                <th>Date</th>
                <th>Reason</th>
              </tr>
            </thead>
            <tbody>
        `;

        skipped.forEach((item: any) => {
          message += `
            <tr>
              <td>${item.employmentId}</td>
              <td>${item.date}</td>
              <td>${item.reason}</td>
            </tr>
          `;
        });

        message += `</tbody></table>`;
      }

      this.modalMessage = message;

      this.clearAllSelections();
      this.onStatusChange(3);
      this.page1 = 0;

      this.getMyReporteesTimesheetRequests();
      this.getTimesheetStatusCountsByEmpId();

    } else {

      this.modalTitle = 'Error';
      this.modalMessage = res?.serviceResponse || 'Bulk rejection failed';

    }

    this.modalService.open(this.statusModal, { centered: true });

  }
});
}

openPreview() {
  this.zoomScale = 1;
  this.translateX = 0;
  this.translateY = 0;
}
downloadFile(): void {
  if (this.rawObjectUrl2) {
    // Newly selected file — use object URL
    const a = document.createElement('a');
    console.log("A",a);
    // console.log("Raw object url",file);
    a.href = this.rawObjectUrl2;
    a.download = this.finalFile.name || 'download';
    a.click();
  } 
}

   async renameFile(file: File, projectId: number, docType: 'Filled' | 'Approved'): Promise<File> {
    
      try{
      const ext = file.name.includes('.') ?file.name.substring(file.name.lastIndexOf('.')): '';
      // const safeDocType = docType.toLowerCase(); // optional
      // // const newFileName = `${this.currentUser.empId}_${projectId}_${}_${safeDocType}${ext}`;
      // const newFileName = `${projectId}_${this.fromDate}_${this.dayType}_${safeDocType}${ext}`;
  
      const response: any = await firstValueFrom(this.timesheetService.generateFileName({
        projectId: projectId,
        extension: ext,
        docType: docType
      }));
      const newFileName = response.fileName;
  
      return new File([file], newFileName, { type: file.type });
    }
     catch(error){
      this.handleError(error,"Generating unique file name",true,"Unable to generate unique file name")
      return null;
    }
  
    }

    private handleError(error: any, context: string, showToUser: boolean = false, userMessage?: string): void {
      const errorMessage = error?.message || error?.toString() || 'An unexpected error occurred';
      console.error(`[${context}]`, error);
      
      if (showToUser) {
        const message = userMessage || `Error: ${errorMessage}. Please try again.`;
        this.openAlertMod(this.alertTemplate, message);
      }
    }
}





function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);


}

