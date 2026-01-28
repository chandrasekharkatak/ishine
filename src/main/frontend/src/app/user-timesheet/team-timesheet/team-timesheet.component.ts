import { LocationStrategy } from '@angular/common';
import { Component,Input,Output,EventEmitter, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
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


@Component({
  standalone: false,
  selector: 'app-team-timesheet',
  templateUrl: './team-timesheet.component.html',
  styleUrls: ['./team-timesheet.component.css']
})
export class TeamTimesheetComponent implements OnInit {

  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;

  @ViewChild('fileInput') fileInput!: ElementRef;

  @ViewChild("clientSideIdForm")
  clientSideIdFormRef: TemplateRef<any>;

  @ViewChild("clientSideIdNotMandatoryFound")
  clientSideIdNotMandatoryFound: TemplateRef<any>;


  @ViewChild("update_clientId")
  updateClientId: TemplateRef<any>;

  data: string;
  feature = "Team Timesheets";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  clientSideIdForm: NgbModalRef;
  updateClientIdModalRef: NgbModalRef;

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


  timesheetObj: Timesheet = new Timesheet();
  startDate: any;
  endDate: any;

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
  fileType2: '' | 'pdf' | 'image' | null = null;
  rawObjectUrl2: string | null = null;
  selectedFile2: File | null = null;
  previewUrl1: SafeResourceUrl | null = null;
  activePreviewUrl: SafeResourceUrl | null = null;
  activeFileType: string | null = null;
  selectedFile: File | null = null;
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  projectClientIdList: ProjectClientSideId[] = [];
  clientSideIdNotMandatoryFoundModalRef: NgbModalRef;
  zoomScale = 1;
  zoomLevel = 100;
  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
clientFilter: boolean = false;
safePdfUrl:SafeResourceUrl | null = null;
  documentData: any;
alertModal: TemplateRef<any>;

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


  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.safePdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
  'assets/Ishine_Timesheet_TNM.pdf');
  }

  async ngOnInit(): Promise<void> {
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
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if (this.userMapping.view_my_teams_timesheets) {
      this.showAllTimesheetsTable();
    } else if (this.userMapping.view_my_teams_timesheets_requests || this.userMapping.update_timesheet_request || this.userMapping.revoke_reportee_timesheet) {
      this.showAllTimesheetRequestsTable();
    }
  }

  getTotalDocCount(projectId: number): number {
    return this.documentData?.filter(d => d.projectId === projectId).length || 0;
  }


  disableMannualDateInput() {
    return false;
  }

  showAllTimesheetsTable() {
    this.isAllTimesheetTable = true;

    this.isAllTimesheetRequestTable = false;
    this.isTMBulkUpload = false;
    this.getAllTeamTimesheets();
    this.page = 1;
    this.data = ''
  }

  showAllTimesheetRequestsTable() {
    this.sortColumn = 'date';
    this.sortDirection = 'DESC';
    // this.sortColumnType = [];
    // this.sortDirection = '';
    this.isAllTimesheetRequestTable = true;
    this.isTMBulkUpload = false;

    this.isAllTimesheetTable = false;

    this.getMyReporteesTimesheetRequests();
    this.page = 1;
    this.data = ''
  }

  showTMBulkUpload() {
    this.isAllTimesheetTable = false;
    this.isAllTimesheetRequestTable = false;
    this.isTMBulkUpload = true;
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
  getMyReporteesTimesheetRequests() {

    const payload: any = {
      empId: this.currentUser.empId,
      clientFilter: this.clientFilter,
      page: this.page1 - 1,
      size :this.items,
      sortBy: this.sortColumn || 'date',
      sortDir: this.sortDirection || 'DESC',
      status : this.selectedStatus,
    };

    /* 🔹 GLOBAL SEARCH (as-is) */
    // if (this.searchText?.trim()) {
    //   payload.globalSearch = this.searchText.trim();
    // }

    /* 🔹 COLUMN FILTERS (toggle search) */
    this.addIfPresent(payload, 'employmentId', this.filters.employmentId);
    this.addIfPresent(payload, 'employeeName', this.filters.employeeName);
    this.addIfPresent(payload, 'date', this.filters.date);
    this.addIfPresent(payload, 'dayType', this.filters.dayType);
    this.addIfPresent(payload, 'projectName', this.filters.projectName);
    this.addIfPresent(payload, 'poNo', this.filters.poNo);
    this.addIfPresent(payload, 'shadowEmpName', this.filters.shadowEmp);
    this.addIfPresent(payload, 'shadowFor', this.filters.shadowFor);


    this.timesheetService
    .getMyReporteesTimesheetRequests(payload)
    .subscribe((res: any) => {

      if (res.serviceStatus === 'Success') {

        this.allTeamTimesheetRequestsProjectView =
        res.serviceResponse.content;

      this.totalRecords =
        res.serviceResponse.totalElements;



      }
    });
  }
  page1: number = 1;
items: number = 10;
totalRecords: number = 0;
totalPages: number = 0;

onPageSizeChange() {
  this.page1 = 1;
  this.getMyReporteesTimesheetRequests();
}



onPageChange(page: number) {
  this.page1 = page;
  this.getMyReporteesTimesheetRequests(); // 🔥 BACKEND HIT
}

sortData(sort: Sort) {
  if (!sort.active || sort.direction === '') return;

  this.sortColumn = sort.active;
  this.sortDirection = sort.direction.toUpperCase() as 'ASC' | 'DESC';
  this.page1 = 1;
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



onGlobalSearchChange() {
  this.page1 = 1; // reset pagination
  this.getMyReporteesTimesheetRequests();
}


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
  onSearch(searchData: any) {
    this.filters = searchData;   // 🔥 column wise values
    this.page1 = 1;
    this.getMyReporteesTimesheetRequests();
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
}




  getMyReporteesAndTheirProjects() {

    this.timesheetObj.managerId = this.currentUser.empId;
    this.timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteesAndTheirProject = response.serviceResponse;
        this.reportees = this.reporteesAndTheirProject;
        console.log("test", this.rejectReasons);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  onReporteeChange(empId: number) {
     this.resetBulkUploadForm('EMP');
    const selectedEmp = this.reporteesAndTheirProject.find(
      emp => emp.empId === empId
    );

    this.projects = selectedEmp ? selectedEmp.projectList : [];
    this.timesheetObj.selectedProjectId = null;
  }


  onProjectSelectBulk(projectId: any) {
    this.resetBulkUploadForm('PROJECT');
    // this.checkIfProjectRequiresClientId(projectId);
    this.getAllDisabledDateListForBulkDocSubmit(projectId);

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


  openPreviewModalForTwo(docType: 'doc1' | 'doc2'): void {
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
    if (this.selectedFile2 != null && this.finalFromDate != null && this.finalToDate != null && this.currentUser.empId != null) {
      this.timesheetService.bulkFinalDocumentUpload(this.selectedFile2, this.finalFromDate, this.finalToDate, this.timesheetObj.selectedEmpId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.resetBulkUploadForm('UPLOAD');

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


expandedTimesheetIndex: number | null = null;
expandedProjectKey: string | null = null;

toggleProject(tIndex: number, pIndex: number):void {
  const key = `${tIndex}-${pIndex}`;
  this.expandedProjectKey =
    this.expandedProjectKey === key ? null : key;
}
/* EMPLOYEE ACCORDION */

/* MAIN ACCORDION */
toggleAccordion(index: number): void {
  if (this.expandedTimesheetIndex === index) {
    this.expandedTimesheetIndex = null;
    this.expandedProjectKey = null;   // close project accordion
  } else {
    this.expandedTimesheetIndex = index;
    this.expandedProjectKey = null;   // reset project when switching employee
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

   activeDocProject: any = null;
   activeDocType: 'Pending' | 'Approved' = 'Pending';
   activePreviewFile: any = null;
   selectedTimesheet: any = null;

   /* OPEN POPUP */
   openDocumentPopup(timesheet: any): void {
     this.selectedTimesheet = timesheet;

     const projects = this.getUniqueProjectsFromTimesheet(timesheet);

    //  if (!projects.length) {
    //    this.openAlertMod(this.alertTemplate, 'No projects available');
    //    return;
    //  }

     this.activeDocProject = projects[0];
     this.setDefaultDocForProject();

     this.modalRef = this.modalService.open(
       this.documentViewerModal,
       { modalDialogClass: 'modal-xl', backdrop: 'static' }
     );
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
   selectProjectForDoc(project: any): void {
     this.activeDocProject = project;
     this.setDefaultDocForProject();
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

     if (this.hasPendingDoc(this.activeDocProject.projectId)) {
       this.activeDocType = 'Pending';
     } else if (this.hasApprovedDoc(this.activeDocProject.projectId)) {
       this.activeDocType = 'Approved';
     } else {
       this.activePreviewFile = null;
       return;
     }

     this.loadActiveDocument();
   }

   /* LOAD DOC */
   loadActiveDocument(): void {
     if (!this.activeDocProject) {
       this.activePreviewFile = null;
       return;
     }

     this.activePreviewFile =
       this.getDocument(
         this.activeDocProject.projectId,
         this.activeDocType
       );
   }

   /* HELPERS */
   hasPendingDoc(projectId: number): boolean {
     return this.selectedTimesheet?.documentData?.some(
       d => d.projectId === projectId && !d.finalFlag
     );
   }

   hasApprovedDoc(projectId: number): boolean {
     return this.selectedTimesheet?.documentData?.some(
       d => d.projectId === projectId && d.finalFlag
     );
   }

   getDocument(projectId: number, type: 'Pending' | 'Approved'): any {
     return this.selectedTimesheet?.documentData?.find(d =>
       d.projectId === projectId &&
       (type === 'Pending' ? !d.finalFlag : d.finalFlag)
     );
   }

  //  Navigation CARDS

   selectedStatus: number = 0; // default = Pending
   onStatusChange(status: number) {
    this.selectedStatus = status;
    this.page1 = 1; // pagination reset
    this.getMyReporteesTimesheetRequests();
  }



  // BULK APPROVAL
  bulkApproveByIds() {

    const timesheetIds = this.getSelectedTimesheetIds();
    if (!timesheetIds.length) return;

    const payload = {
      timesheetIds,
      status: 'APPROVED',
      updatedBy: this.currentUser.empId
    };

    this.timesheetNewService
      .bulkApproveTimesheetsByIds1(payload)
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success') {
            this.clearAllSelections();
            this.getMyReporteesTimesheetRequests();
            alert(res.serviceResponse || 'Timesheets approved successfully');
          } else {
            alert(res?.serviceResponse || 'Bulk approval failed');
          }
        }

      });

  }


// BULK REJECT (FIXED)
BulkRejectByIds(
  template: TemplateRef<any>,
  rejectReason: string
) {

  const timesheetIds = this.getSelectedTimesheetIds();

  if (!timesheetIds.length) {
    this.openAlertMod(template, 'No timesheets selected');
    return;
  }

  if (!rejectReason || !rejectReason.trim()) {
    this.openAlertMod(template, 'Reject reason is required');
    return;
  }

  const payload = {
    timesheetIds,
    status: 'REJECTED',
    updatedBy: this.currentUser.empId,
    rejectReason: rejectReason.trim()
  };

  this.timesheetNewService
    .bulkRejectTimesheetsByIds1(payload)
    .subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.clearAllSelections();
          this.modalRef?.close();
          this.getMyReporteesTimesheetRequests();
          this.openAlertMod(
            template,
            res.serviceResponse || 'Timesheets rejected successfully'
          );
        } else {
          this.openAlertMod(
            template,
            res?.serviceResponse || 'Bulk rejection failed'
          );
        }
      },
      error: () => {
        this.openAlertMod(template, 'Bulk rejection failed');
      }
    });


}



openBulkRejectModal(bulkRejectTimesheet: TemplateRef<any>): void {
  this.selectedRejectReason = '';
  this.timesheetObj.rejectReason = '';

  this.modalRef = this.modalService.open(
    bulkRejectTimesheet,
    { modalDialogClass: 'modal-lg', backdrop: 'static' }
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
    updatedBy: this.currentUser.empId
  };

  this.timesheetNewService
    .bulkApproveTimesheetsByIds1(payload)
    .subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.clearAllSelections();
          this.getMyReporteesTimesheetRequests();
          alert(res.serviceResponse || 'Timesheet approved successfully');
        } else {
          alert(res?.serviceResponse || 'Timesheet approval failed');
        }
      },
      error: () => {
        alert('Timesheet approval failed');
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

// SINGLE TIMESHEET REJECT
submitSingleReject() {

  if (!this.selectedTimesheetForReject?.timesheetId) {
    alert('Invalid timesheet');
    return;
  }

  if (!this.singleRejectReason || !this.singleRejectReason.trim()) {
    alert('Reject reason is required');
    return;
  }

  const payload = {
    timesheetIds: [this.selectedTimesheetForReject.timesheetId],
    status: 'REJECTED',
    updatedBy: this.currentUser.empId,
    rejectReason: this.singleRejectReason.trim()
  };

  this.timesheetNewService
    .bulkRejectTimesheetsByIds1(payload)
    .subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close(); // ✅ close ONLY current modal
          this.clearAllSelections();
          this.getMyReporteesTimesheetRequests();
          alert(res.serviceResponse || 'Timesheet rejected successfully');
        } else {
          alert(res?.serviceResponse || 'Timesheet rejection failed');
        }
      },
      error: () => {
        alert('Timesheet rejection failed');
      }
    });
}


// Single Project Approve

approveSingleProject(timesheet: any, project: any, location: any) {

  if (!project?.projectId || !location?.locationMappingId) {
    return;
  }

  const payload = {
    timesheetId: timesheet.timesheetId,
    locationMappingId: location.locationMappingId, // ✅ REQUIRED
    projectIds: [project.projectId],                // ✅ ARRAY
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

// Single proejct Reject

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

  event.stopPropagation();   // 🔥 CRITICAL

  this.selectedProjectForReject = project;
  this.selectedProjectTimesheet = timesheet;
  this.selectedProjectLocation = location;
  this.singleProjectRejectReason = '';

  this.modalRef = this.modalService.open(
    template,
    { modalDialogClass: 'modal-md', backdrop: 'static' }
  );
}





}



function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}


