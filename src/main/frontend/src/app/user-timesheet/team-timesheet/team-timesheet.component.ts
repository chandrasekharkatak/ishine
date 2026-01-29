import { LocationStrategy } from '@angular/common';
import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { ProjectBasedBulkUploadPayload } from './types';


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

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

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
  allTimesheetColumnsVMS: any[] = ['blank', 'blank', 'employeementId', 'clientSideId', 'name', 'teamName', 'departmentName', 'projectName', 'clientName', 'billableType', 'employeeRole', 'spoc', 'projectManagerName', 'poNo', 'startdate', 'totalExpectedFillCount', 'totalIshineFilledCount', 'totalClientSideNotFilledCount', 'totalClientSidePendingCount', 'totalClientSideApprovedCount']
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
  alertMessageModalRef: NgbModalRef;
  zoomScale = 1;
  zoomLevel = 100;
  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
  maxMonth = '';
  minMonth = '';
  allProjects: {projectId: number, projectName: string}[] = [];
  projectObj: { [projectId: number]: { empId: number; name: string }[] } = {};
  minusDaysData: {minusDays: number, checkMinusDaysForBulkUpload: boolean} = {minusDays: 45, checkMinusDaysForBulkUpload: true};

  constructor(
    public validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private inputValidationService: InputValidationService,
    private employee360Service: Employee360Service,
    private employeeService: EmployeeService,
    private utilityService: UtilityService,
    private bodyComponent: BodyComponent,
    private sanitizer: DomSanitizer,

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {
    const now = new Date();
    this.today = now.toISOString().split('T')[0];
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.sectionViewInit();
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
    if (this.userMapping.view_my_teams_timesheets) {
      this.showAllTimesheetsTable();
    } else if (this.userMapping.view_my_teams_timesheets_requests || this.userMapping.update_timesheet_request || this.userMapping.revoke_reportee_timesheet) {
      this.showAllTimesheetRequestsTable();
    }
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
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
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
  getMyReporteesTimesheetRequests() {
    this.allTeamTimesheetRequests = [];
    this.isSelectAll = false
    this.bulkApprove = []
    this.bulkReject = []

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    timesheetObj.fromDate = "";
    timesheetObj.toDate = "";

    // this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.allTeamTimesheetRequests = response.serviceResponse;
    //     this.allTeamTimesheetRequests.forEach((timesheet, index) => {
    //       timesheet.checkId = "timesheet" + index;
    //       timesheet.employeementId = "A-".concat(timesheet.employeementId);
    //       timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
    //       timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
    //       timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
    //       timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
    //     });

    //     this.allTeamTimesheetRequests.forEach(timesheet => {
    //       timesheet.emp360 = timesheet.empId;
    //       timesheet.emp360CreatedBy = timesheet.createdBy;
    //     });

    //     //console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
    //   } else {
    //     console.error(response.serviceResponse)
    //   }
    // });

    timesheetObj.fromDate = this.fromDate;
    timesheetObj.toDate = this.toDate;

    this.timesheetService.getAllEmployeeDSROfRM(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequestsProjectView = response.serviceResponse;
        // this.allTeamTimesheetRequests.forEach((timesheet, index) => {
        //   timesheet.checkId = "timesheet" + index;
        //   timesheet.employeementId = "A-".concat(timesheet.employeementId);
        //   timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
        //   timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
        //   timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
        //   timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        // });

        // this.allTeamTimesheetRequests.forEach(timesheet => {
        //   timesheet.emp360 = timesheet.empId;
        //   timesheet.emp360CreatedBy = timesheet.createdBy;
        // });

        console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequestsProjectView);
      } else {
        console.error(response.serviceResponse)
      }
    });

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
  page1 = 1;
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
  items = 10;
  handleItemsChange(event) {
    this.items = event;

  }


  //sorting timesheet
  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

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
      this.onBulkApproval(alertTemplate);
    }
  }

  bulkApproveWithoutNightShiftRequest(template: TemplateRef<any>) {
    this.bulkApprove = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if (this.bulkApprove.length !== 0) {
      this.onBulkApproval(template);
    } else {
      this.cancelRequest();
      this.showAllTimesheetRequestsTable();
    }
  }

  openBulkRejectModal(nightShiftTemplate: TemplateRef<any>, bulkRejectTimesheet: TemplateRef<any>) {

    this.selectedRejectReason = '';
    this.timesheetObj.rejectReason = '';

    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    //console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.open(nightShiftTemplate, { modalDialogClass: 'modal-lg' });
    } else {
      this.openBulkRejectTimesheet(bulkRejectTimesheet);
    }
  }

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

  onBulkApproval(template: TemplateRef<any>) {
    this.cancelRequest();
    //console.log("Updated Bulk List : ", this.bulkApprove);
    let timesheetObj = new Timesheet();
    timesheetObj.bulkApprovedList = this.bulkApprove;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.status = "Approved"
    //console.log("For Bulk Update : ", timesheetObj);
    timesheetObj.bulkApprovedList.forEach((x) => {
      x.employeementId = x.employeementId.substring(2)
    })
    const empId = timesheetObj.bulkApprovedList.length > 0
      ? timesheetObj.bulkApprovedList[0].empId
      : null;
    console.log("test", empId);
    this.timesheetService.bulkApproveTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Approved Successfully ");
        this.showAllTimesheetRequests(empId);
        this.bulkApprove = [];
        this.bulkReject = [];
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  OnBulkReject(template: TemplateRef<any>) {
    //console.log("Updated Bulk List : ", this.bulkReject);
    let timesheetObj = new Timesheet();
    timesheetObj.bulkRejectList = this.bulkReject;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.status = "Rejected"
    timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
    timesheetObj.rejectionId = this.selectedRejectReason;
    //console.log("For Bulk Update : ", timesheetObj);
    timesheetObj.bulkRejectList.forEach((y) => {
      y.employeementId = y.employeementId.substring(2);
    })
    this.timesheetService.bulkRejectTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Rejected Successfully ");
        this.showAllTimesheetRequestsTable();
        this.bulkApprove = [];
        this.bulkReject = [];
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

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
  onSearch(searchData) {
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
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

  // onMonthYearChange() {
  //   this.resetBulkUploadForm('MONTH');
  //   this.getMyReporteesAndTheirProjects();
  //   const [year, month] = this.timesheetObj.monthYear.split('-');

  //   const selectedYear = parseInt(year);
  //   const selectedMonth = parseInt(month);

  //   // Get current date info
  //   const now = new Date();
  //   const currentYear = now.getFullYear();
  //   const currentMonth = now.getMonth() + 1; // JavaScript months are 0-indexed

  //   // Calculate the previous month and previous-previous month
  //   const previousMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  //   const previousMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  //   // Check if selected month is the just previous month
  //   const isJustPreviousMonth = (selectedYear === previousMonthYear && selectedMonth === previousMonth);

  //   // Set minDate based on month selection
  //   // If just previous month: allow from 1st
  //   // If older months (previous' previous and beyond): allow from 15th
  //   if (isJustPreviousMonth) {
  //     this.minDate = this.timesheetObj.monthYear + '-01';
  //   } else {
  //     this.minDate = this.timesheetObj.monthYear + '-15';
  //   }

  //   const lastDay = new Date(selectedYear, selectedMonth, 0).getDate();
  //   this.maxDate = `${year}-${month}-${lastDay.toString().padStart(2, '0')}`;
  // }

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
  


  // getMyReporteesAndTheirProjects() {

  //   this.timesheetObj.managerId = this.currentUser.empId;
  //   this.timesheetService.getMyReporteesAndClientSideProjectsInMonthYear(this.timesheetObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.reporteesAndTheirProject = response.serviceResponse;
  //       // this.reportees = this.reporteesAndTheirProject;

  //       this.allProjects = this.reporteesAndTheirProject.reduce((acc, rp) => {
  //         if (!acc.some(p => p.projectId === rp.projectId)) {
  //           acc.push(rp);
  //         }
  //         return acc;
  //       }, []);

  //       console.log("test", this.rejectReasons);
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });

  // }

  // Simple approach using a Map
    
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
    if (!this.searchText) return this.allTeamTimesheetRequestsProjectView;
    const lowerSearchText = this.searchText.toLowerCase();

    return this.allTeamTimesheetRequestsProjectView.filter(item =>
      item.name?.toLowerCase().includes(lowerSearchText) ||
      item.employeementId?.toString().toLowerCase().includes(lowerSearchText) ||
      item.clientSideId?.toLowerCase().includes(lowerSearchText) ||
      item.departmentName?.toLowerCase().includes(lowerSearchText) ||
      item.projectName?.toLowerCase().includes(lowerSearchText) ||
      item.poNo?.toLowerCase().includes(lowerSearchText) ||
      item.teamName?.toLowerCase().includes(lowerSearchText)
    );
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
    this.filteredData().forEach(r => (r.selected = checked));
    this.updateSelectedRows(null);
  }
  clearAllSelections() {
    this.filteredData().forEach(r => r.selected = false);
    this.selectedRows = [];
  }

  bulkApprove1(template: TemplateRef<any>) {
    let timesheetObj = new Timesheet();
    timesheetObj.status = "Approved"
    const rawData = this.selectedRows;
    const empDetails = rawData.map(item => ({
      empId: item.empId,
      teamId: item.teamId,
      status: "Approved",
      timesheetStatusUpdatedBy: this.currentUser.empId
    }));

    const payload = {
      pendingApprovalList: empDetails
    };
    this.timesheetService.approveTimesheetRequest(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clearAllSelections();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }


  bulkReject1(template: TemplateRef<any>) {
    const rawData = this.selectedRows;
    const empDetails = rawData.map(item => ({
      empId: item.empId,
      teamId: item.teamId,
      status: "Rejected",
      timesheetStatusUpdatedBy: this.currentUser.empId,
      rejectionId: this.selectedRejectReason,
      rejectReason: this.timesheetObj.rejectReason
    }));
    const payload = {
      pendingApprovalList: empDetails
    };
    this.timesheetService.approveTimesheetRequest(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clearAllSelections();
        this.openAlertMod(template, "Timesheets rejected successfully");
      } else {
        this.openAlertMod(template, "Timesheets rejection failed");
      }

    });
  }

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


  // openReqMod(template: TemplateRef<any>) {

  //   this.filters = {};
  //   this.isSearchEnabled = false;
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  // }

  opnenbulkRejectTimesheet(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }



  exportExcel1() {
    if (this.isAllTimesheetRequestTable == true) {
      this.excelName = 'AllTeamTimesheetDetails.xlsx';

      this.allTeamTimesheetDataForExcel = this.allTeamTimesheetRequestsProjectView;
      const onlySpecificDataArr = this.allTeamTimesheetDataForExcel.map(x => ({
        "Employee Id": x.employeementId,
        "Client Side Id": x.clientSideId,
        "Employee Name": x.name,
        "Team Name": x.teamName,
        "Department Name": x.departmentName,
        "Project Name": x.projectName,
        "PONO": x.poNo,
        "Client Name": x.clientName,
        "Billable Type": x.billableType,
        "Employee Role": x.employeeRole,
        "SPOC": x.spoc,
        "Project Manager Name": x.projectManagerName,
        "Start Date": x.startdate,
        "Total Expected Fill Count": x.totalExpectedFillCount,
        "Total Filled Count": x.totalIshineFilledCount,
        "TotalClient Side Not Filled Count": x.totalClientSideNotFilledCount,
        "Total Client Side Pending Count": x.totalClientSidePendingCount,
        "Total Client Side Approved Count": x.totalClientSideApprovedCount
      }));


      const fieldDetails = [
        { Field: "Employee Id", Description: "Unique identifier for employee" },
        { Field: "Client Side Id", Description: "Client's identification for the employee" },
        { Field: "Employee Name", Description: "Name of the employee" },
        { Field: "Team Name", Description: "Team where employee belongs" },
        { Field: "Department Name", Description: "Department name" },
        { Field: "Project Name", Description: "Project employee is assigned to" },
        { Field: "PONO", Description: "Purchase Order Number" },
        { Field: "Client Name", Description: "Name of the client" },
        { Field: "Billable Type", Description: "Billable or Non-billable status" },
        { Field: "Employee Role", Description: "Role of the employee" },
        { Field: "SPOC", Description: "Single Point of Contact" },
        { Field: "Project Manager Name", Description: "Name of project manager" },
        { Field: "Start Date", Description: "Project start date" },
        { Field: "Total Expected Fill Count", Description: "The Total Expected Fill Count represents the net number of working days the employee is expected to contribute during the current month on a project, adjusted for their project start date, any leaves taken, and all applicable holidays." },
        { Field: "Total Filled Count", Description: "Total Timesheet filled from Ishine system" },
        { Field: "Total Client Side Not Filled Count", Description: "Client side unfilled counts" },
        { Field: "Total Client Side Pending Count", Description: "Pending approvals count of Client Side Attendance" },
        { Field: "Total Client Side Approved Count", Description: "Approved counts by client" }
      ];


      const wb = XLSX.utils.book_new();


      const dataSheet = XLSX.utils.json_to_sheet(onlySpecificDataArr);
      const detailsSheet = XLSX.utils.json_to_sheet(fieldDetails);


      XLSX.utils.book_append_sheet(wb, dataSheet, "Timesheet Data");
      XLSX.utils.book_append_sheet(wb, detailsSheet, "Field Details");


      XLSX.writeFile(wb, this.excelName);
    }
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

getReporteesFromProjectId(): { empId: number; name: string }[] {
  const projectId = this.timesheetObj.selectedProjectId;
  console.log("Project Id : ", projectId);
  console.log("Project Object : ", this.projectObj[projectId]);
  
  return this.projectObj[projectId] ?? [];

}

  getPreviousMinusDays() {
    this.timesheetService.getPreviousMinusDays().subscribe({
      next: (response: any) => {

        this.minusDaysData = response.serviceResponse || {
          checkMinusDaysForBulkUpload: true,
          minusDays: 45
        };

        // if(response.serviceError != null ){
          this.openAlertMod(this.alertTemplate, `Document upload is only valid for past ${this.minusDaysData.minusDays} days`);
        // }

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

}
