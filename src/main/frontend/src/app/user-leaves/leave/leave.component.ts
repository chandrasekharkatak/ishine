import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
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
import * as XLSX from 'xlsx';
import * as moment from 'moment';
import { Sort } from '@angular/material/sort';
import { Employee } from 'src/app/models/employee';
import { TeamViewService } from 'src/app/services/team-view.service';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';
import { dateFormat } from 'highcharts';
import { AppComponent } from 'src/app/app.component';
import { PortalService } from 'src/app/services/portal.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { DepartmentService } from 'src/app/services/department.service';
import { Department } from 'src/app/models/department';

@Component({
  standalone: false,
  selector: 'app-leave',
  templateUrl: './leave.component.html',
  styleUrls: ['./leave.component.css']
})
export class LeaveComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>

  data: string;
  allowedLeaveDays: any;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  //flags
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  // isTable: boolean = false;
  isLeaveHistoryTable: boolean = false;
  isLeaveBalanceTable: boolean = false;
  isLeaveApplicationsTable: boolean = false;
  isLeaveLogTable: boolean = false;

  isSelfLeaveHistory: boolean = false;
  isTeamLeaveHistory: boolean = false;

  isLeaveRevokeApplicationTable: boolean = false;
  isSelfLeaveRevokeApplication: boolean = false;
  isTeamLeaveRevokeApplication: boolean = false;
  isOverlapsedLeaveTable: boolean = false;

  isCompOffLeave: boolean = false;

  //modal
  alertMessage: any;
  modalRef:NgbModalRef;

  //obj
  feature = "Leave";
  currentUser: User;
  userMapping: any = {};
  log: Log;

  leaveObj: Leave = new Leave();

  // for revoke approved leave
  dateToday: any = new Date();

  leaveTypes: any[] = [];
  leaveHistoryList: any[] = [];
  leaveLogList: any[] = [];
  leaveBalanceList: any[] = [];
  revokeLeaveApplicationList: any[] = [];

  //excel
  excelName = '';
  elementName = '';
  leaveApplicationListDataForExcel: any[] = [];

  holidayList: any;
  holidayDates: any[] = [];

  leavePolicyRules: any[] = [];
  leavePolicyObj: Leave = new Leave();

  teamMemberList: any[] = [];

  items = 20;
  isSelectAll: boolean = false;
  isSelect: boolean = false;
  bulkLeaveApprove: any = [];
  bulkLeaveReject: any = [];
  LeaveObj = new Leave();
  filterLeaveHistoryList: any;
  leaveHistoryListForTable: any[] = [];
  overLappingTeamMemberList: any[] = [];

  leaveBalance: any[] = [];
  leaveDetails = [];
  holidayWeekOffList: any = [];
  holidayWeekOffCount: any;

  level1MinNoOfDays: any;
  level1ApprovalTo: any;
  level2MinNoOfDays: any;
  level2ApprovalTo: any;
  level2ApproverName: any;
  level2ApproverEmail: any;

  weekOffExcludedDepartmentList: any[] = [];
  previouslyAppliedLeavesList: any[] = [];
  getListOfAppliedLeaveBetweenFromAndToDate: any[] = [];
  isWeekOffsExcluded: boolean = false;

  filters: any = {};
  isSearchEnabled: boolean = false;
  selfLeaveHistoryColumns: any[] = ['blank', 'leaveType', 'fromDate', 'toDate', 'noOfDaysDisplay', 'status', 'createdByName', 'createdOn', 'reason', 'currentApprovalLevel', 'approverName', 'managerApprovalStatus', 'level2ApproverName', 'level2ApprovalStatus', 'level3ApproverName', 'level3ApprovalStatus', 'remark','blank'];
  teamLeaveHistoryColumns: any[] = ['blank', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'noOfDaysDisplay', 'status', 'createdOn', 'reason', 'currentApprovalLevel', 'approverName', 'managerApprovalStatus', 'level2ApproverName', 'level2ApprovalStatus', 'level3ApproverName', 'level3ApprovalStatus', 'remark','blank'];
  leaveBalColumns: any[] = ['leaveType', 'totalLeaveBalance', 'pendingForApproval', 'balance'];
  leaveLogColumns: any[] = ['rowNumber', 'leaveType', 'updateBalanceBy', 'balance', 'message', 'createdOn'];
  selfLeaveRevokeHistoryColumns: any[] = ['blank', 'leaveType', 'fromDate', 'toDate', 'noOfDays', 'status', 'createdByName', 'createdOn', 'revokeReason', 'approverName', 'remark'];
  teamLeaveRevokeHistoryColumns: any[] = ['blank', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'noOfDays', 'status', 'createdOn', 'revokeReason', 'approverName', 'remark'];
  overlapsedLeaveColumns: any[] = ['employeementId', 'name', 'fromDate', 'toDate'];
  leaveApprovedColumns: any[] = ['blank', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'noOfDays', 'status', 'createdByName', 'createdOn', 'reason','blank'];


  availableCompOffDetails: any[] = [];
  tableName: string;
  approvedLeaveLogList: any[];
  isLeaveApprovedByMeTable: boolean = false;

  constructor(
    public validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private datePipe: DatePipe,
    private leaveService: LeaveService,
    private holidayService: HolidayService,
    private exportExcelService: ExportExcelService,
    private teamViewService: TeamViewService,
    private logService: LogService,
    private locationStrategy: LocationStrategy,
    private portalService: PortalService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.featureName = this.feature;
    });
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
    console.log("this.currentUser : ", this.currentUser);
    //console.log("logInfo : ", this.log);

    // this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.maternityType = '';
    this.leaveObj.fromDateDayType = 0;
    this.leaveObj.toDateDayType = 0;
    this.leaveObj.leaveAppliedFor = "self"

    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    // this.getAllLeaveTypes();
    this.getAllLeaveTypesByLeavePolicies(this.currentUser);
    //console.log('this.currentUser    gender ',this.currentUser);
    this.getAllPortalConfigData();
    // this.dateToday = this.datePipe.transform(this.dateToday,'dd-MM-yyyy');
    this.preventBackButton();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if (this.userMapping.apply_for_leave) {
      this.showCreateForm();
    } else if (this.userMapping.view_leave_history || this.userMapping.update_leave || this.userMapping.delete_leave_application || this.userMapping.revoke_leave_application) {
      this.showLeaveHistoryTable();
    } else if (this.userMapping.view_leave_balance) {
      this.showLeaveBalanceTable();
    } else if (this.userMapping.view_leave_balance_log) {
      this.showLeaveLogTable();
    } else if (this.userMapping.approvedLeaves) {
      this.showApprovedLeaveLogTable();
    } else if (this.userMapping.view_emp_leave_exclusion) {
      this.showEmpLeaveExclusion();
    }

  }

  disableMannualDateInput() {
    return false;
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;

    this.isUpdation = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;
    this.isLeaveRevokeApplicationTable = false;

    this.isSelfLeaveHistory = false;
    this.isTeamLeaveHistory = false;

    this.isSelfLeaveRevokeApplication = false;
    this.isTeamLeaveRevokeApplication = false;
    this.reset();
    this.getAllHolidays();
    // this.getAllLeaveTypes();
    this.getAllMyLeaveApplicationsByEmpId(this.currentUser);
    this.leaveObj.fromDateDayType = ''
    this.leaveObj.toDateDayType = ''

  }

  showLeaveHistoryTable() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveHistoryTable = true;
    this.isLeaveBalanceTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = ''

    this.isSelfLeaveHistory = true;
    this.isTeamLeaveHistory = false;

    this.isSelfLeaveRevokeApplication = false;
    this.isTeamLeaveRevokeApplication = false;

    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;
    this.showSelfLeaveHistoryTable();

  }

  showSelfLeaveHistoryTable() {
    this.isLeaveHistoryTable = true;
    this.isSelfLeaveHistory = true;

    this.isLeaveBalanceTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = ''

    this.isSelfLeaveRevokeApplication = false;
    this.isTeamLeaveRevokeApplication = false;

    this.isTeamLeaveHistory = false;
    this.isOverlapsedLeaveTable = false;

    this.filters = {};
    this.isSearchEnabled = false;
    this.getAllMyLeaveApplicationsByEmpId(this.currentUser);
  }

  showTeamLeaveHistoryTable() {
    this.isLeaveHistoryTable = true;
    this.isTeamLeaveHistory = true;

    this.isLeaveBalanceTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = '';

    this.isSelfLeaveHistory = false;
    this.isOverlapsedLeaveTable = false;
    this.filters = {};
    this.isSearchEnabled = false;
    this.getAllTeamMemberList()
    this.getAllMyTeamApplicationsByEmpId(this.currentUser);
  }

  showLeaveBalanceTable() {
    this.isLeaveBalanceTable = true;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveHistoryTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;
    this.getMyLeaveBalancesByEmpId();
  }

  showLeaveLogTable() {
    this.isLeaveLogTable = true;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;
    this.getLeaveLogsByEmpId();
  }

  showLeaveRevokeApplicationTable() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveRevokeApplicationTable = true;
    this.isSelfLeaveRevokeApplication = true;

    this.isLeaveLogTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;

    this.isTeamLeaveRevokeApplication = false;
    this.getRevokeLeaveApplicationByEmpId();
  }

  showSelfLeaveRevokeApplication() {
    this.isLeaveRevokeApplicationTable = true;
    this.isSelfLeaveRevokeApplication = true;

    this.isLeaveLogTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;

    this.isTeamLeaveRevokeApplication = false;
    this.getRevokeLeaveApplicationByEmpId();
  }

  showTeamLeaveRevokeApplication() {
    this.isLeaveRevokeApplicationTable = true;
    this.isTeamLeaveRevokeApplication = true;

    this.isLeaveLogTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;

    this.isWeekOffsExcluded = false;
    this.isSelfLeaveRevokeApplication = false;
    this.getAllMyTeamLeaveRevokeApplicationsByEmpId(this.currentUser);
  }


  reset() {
    this.leaveObj = new Leave();
    this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.fromDateDayType = 0;
    this.leaveObj.toDateDayType = 0;
    this.leaveObj.leaveAppliedFor = "self"
    this.isOverlapsedLeaveTable = false;
    // to set empId of current User in leaveObj for initial Leave Application
    this.getLeaveMetadata();

    this.leaveHistoryList = [];
    this.leaveLogList = [];
    this.leaveBalanceList = [];
    this.leaveDetails = [];
    this.overLappingTeamMemberList = [];
    this.leaveObj.maternityType = '';
  }

  showUpdateForm(leaveHistory: Leave) {
    this.isForm = true;
    this.isUpdation = true;
    this.isCreation = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isOverlapsedLeaveTable = false;
    this.leaveObj.maternityType = leaveHistory.maternityType
    this.leaveObj = Object.assign({}, leaveHistory);

    this.leaveObj.fromDateDayType = leaveHistory.fromDateDayType;
    this.leaveObj.toDateDayType = leaveHistory.toDateDayType;
    this.leaveObj.fromDate = (this.leaveObj.fromDate) ? moment(this.leaveObj.fromDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;
    this.leaveObj.toDate = (this.leaveObj.toDate) ? moment(this.leaveObj.toDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;
    //console.log("    update form   ",this.leaveObj);
    if (this.isSelfLeaveHistory) {
      this.leaveObj.leaveAppliedFor = "self";
      this.leaveObj.empId = this.currentUser.empId;
    } else if (this.isTeamLeaveHistory) {
      this.leaveObj.leaveAppliedFor = "team";
    }

    //console.log("this.leaveObj for Update : ", this.leaveObj);
    this.getLeaveMetadata();
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  openDeleteLeave(template: TemplateRef<any>, leaveHistory: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.leaveObj = leaveHistory;
    //console.log(this.leaveObj);
  }

  openRevokeApprovedLeaveApplication(template: TemplateRef<any>, leaveHistory: any) {
    this.modalRef = this.modalService.open(template);
    this.leaveObj = leaveHistory;
    if (this.isSelfLeaveHistory) {
      this.leaveObj.leaveAppliedFor = 'self';
    } else {
      this.leaveObj.leaveAppliedFor = 'team';
    }
    //console.log(this.leaveObj);
  }

  openRevokeLeaveRejectModal(template: TemplateRef<any>, leave: any) {
    this.leaveObj.rejectReason = '';
    this.cancelRequest();
    this.leaveObj = leave;
    this.modalRef = this.modalService.open(template);
  }

  isCompOffSelected(leaveTypeMasterId: any) {
    const isCompOff = this.leaveTypes.find(x => x.leaveTypeMasterId == leaveTypeMasterId);
    if (isCompOff.leaveTypeCode == 'CO') {
      this.isCompOffLeave = true;
    } else {
      this.isCompOffLeave = false;
    }
  }

  validateLeavetObj(leaveObj: Leave, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.leaveAppliedFor)) {
      this.alertMessage = "Please select Leave Application For !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.leaveTypeMasterId)) {
      this.alertMessage = "Please select Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leaveObj.leaveTypeMasterId == 18) {
      if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.maternityType)) {
        this.alertMessage = "Please select Maternity Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.fromDate)) {
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (leaveObj.leaveTypeCode != 'ML') {
      if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.fromDateDayType)) {
        this.alertMessage = "Please select from day type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }


    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.toDate)) {
      this.alertMessage = "Please select To Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (this.leaveObj.leaveTypeCode != 'ML') {
      if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.toDateDayType)) {
        this.alertMessage = "Please select to day type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }


    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.reason)) {
      this.alertMessage = "Please enter Leave reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    else if (!this.validationService.validateActivityTimesheetDiscription(leaveObj.reason?.trim())) {
      this.alertMessage = "Please enter valid Leave reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  validateRevokeObj(leaveObj: Leave, template: TemplateRef<any>) {

    this.cancelRequest();
    if (!this.validationService.validateNullUndefinedEmptyString(leaveObj.revokeReason)) {
      this.alertMessage = "Please enter revoke reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  resetLeaveObj() {
    this.leaveDetails = [];
    this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.fromDate = '';
    this.leaveObj.fromDateDayType = '';
    this.leaveObj.toDate = '';
    this.leaveObj.toDateDayType = '';
    this.leaveObj.noOfDays = '';
    this.leaveObj.reason = '';
  }

  setLeaveTypeCode(leaveTypeMasterId: any) {

    let leaveType = this.leaveTypes.find(leaveType => leaveType.leaveTypeMasterId == leaveTypeMasterId);
    this.leaveObj.leaveTypeCode = leaveType.leaveTypeCode;
    //console.log(" ln 585 ",leaveType);
    // resetting Data for previously selected leave Type
    this.leaveObj.fromDate = '';
    this.leaveObj.fromDateDayType = '';
    this.leaveObj.toDate = '';
    this.leaveObj.toDateDayType = '';
    this.leaveObj.noOfDays = '';
    this.leaveObj.reason = '';

    this.isOverlapsedLeaveTable = false;
    this.overLappingTeamMemberList = [];
    this.leaveObj.maternityType = '';

  }

  setPolicyObj(leaveTypeMasterId: any) {
    this.leavePolicyObj = new Leave();
    let leavePolicyObj = this.leaveTypes.find(leaveType => leaveType.leaveTypeMasterId == leaveTypeMasterId);
    this.leavePolicyObj = Object.assign({}, leavePolicyObj);
    //console.log("  leavePolicyObj     ",leavePolicyObj);
  }

  async checkPolicy(leaveObj: Leave, leavePolicyObj: Leave, template: TemplateRef<any>) {
    let flag = true;

    // One Time Leave Application Count
    if (leavePolicyObj.oneTimeLeave == "Yes") {
      if (leavePolicyObj.leaveTypeCode != "ML") {
        if (leavePolicyObj.oneTimeLeaveMinCount > leaveObj.noOfDays) {
          this.alertMessage = `Leave Application days are not meeting minimum limit of ${leavePolicyObj.oneTimeLeaveMinCount} day(s) !!`
          this.openAlertMod(template, this.alertMessage);
          flag = false;
        }
        if (leavePolicyObj.oneTimeLeaveCount < leaveObj.noOfDays) {
          this.alertMessage = `Leave Application days are exceeding limit of ${leavePolicyObj.oneTimeLeaveCount} day(s) !!`
          this.openAlertMod(template, this.alertMessage);
          flag = false;
        }
      }

    }

    // Leave Probation Period
    if (leavePolicyObj.probation == "Yes") {
      let date = this.leaveObj.fromDate;
      let dateOfJoining = new Date();;
      if (this.currentUser?.dateOfJoining) {
        dateOfJoining = new Date(this.currentUser.dateOfJoining)
      }

      const START_DAY_COUNT = 1;
      const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
      let probationDays = START_DAY_COUNT + diff(dateOfJoining, date);

      if (probationDays < leavePolicyObj.probationPeriod) {
        this.alertMessage = `Probation Period of ${leavePolicyObj.probationPeriod} days is not completed, Please try after ${leavePolicyObj.probationPeriod - probationDays} day(s)!!`
        this.openAlertMod(template, this.alertMessage);
        flag = false;
      }
    }

    // Leave Locking Period
    if (leavePolicyObj.lockingPeriod == "Yes") {
      let currentYear = new Date().getFullYear();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      const financialStartDate: Date = new Date(currentYear, (leavePolicyObj.financialYearStartMonth - 1), leavePolicyObj.financialYearStartDate, 0, 0, 0, 0);
      let checkDate = this.leaveObj.fromDate;

      let startDate: Date = financialStartDate;
      let endDate: Date = new Date(startDate.getTime() + (leavePolicyObj.lockingPeriodValue * DAY_IN_MS));

      await this.checkDateInRange(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template).then(response => {
        if (!response) flag = false;
      });
    }

    return flag;
  }

  async checkDateInRange(startDate: Date, endDate: Date, checkDate: Date, leaveObj: Leave, leavePolicyObj: Leave, template: TemplateRef<any>) {
    let flag = true;
    if ((checkDate <= endDate && checkDate >= startDate)) {
      //Check Leave Applications Count
      let approvedLeaveApplications = [];
      let leaveApplicationCount = 0;
      let totalAppliedLeaves = 0;

      let leaveAppObj = new Leave();
      leaveAppObj.empId = this.currentUser.empId;
      leaveAppObj.leaveTypeMasterId = leaveObj.leaveTypeMasterId;
      leaveAppObj.fromDate = startDate;
      leaveAppObj.toDate = endDate;

      const response: any = await this.leaveService.getAppliedLeaveApplicationsByEmpIdAndDateRange(leaveAppObj).toPromise();
      if (response.serviceStatus == "Success") {
        approvedLeaveApplications = response.serviceResponse;

        totalAppliedLeaves = approvedLeaveApplications.reduce((acc, currLeave) => {
          acc += currLeave.noOfDays;
          return acc;
        }, 0);

        leaveApplicationCount = totalAppliedLeaves + leaveObj.noOfDays;
        if (leaveApplicationCount > leavePolicyObj.lockingValue) {
          this.alertMessage = `Leave Application days are exceeding limit of ${leavePolicyObj.lockingValue} days, Available only ${leavePolicyObj.lockingValue - totalAppliedLeaves} day(s) for ${leavePolicyObj.lockingPeriodValue} days period !!`
          this.openAlertMod(template, this.alertMessage);
          flag = false;
        }
      } else {
        console.error(response.serviceResponse);
      }
      return flag;
    }
    else {
      this.updateStartDate(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template);
    }
    return flag;
  }

  updateStartDate(startDate: Date, endDate: Date, checkDate: Date, leaveObj: Leave, leavePolicyObj: Leave, template: TemplateRef<any>) {
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    startDate = endDate;
    endDate = new Date(startDate.getTime() + (leavePolicyObj.lockingPeriodValue * DAY_IN_MS));

    this.checkDateInRange(startDate, endDate, checkDate, leaveObj, leavePolicyObj, template);
  }

  // Leave Application
  fromDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    let BACKDATED_LEAVE_PERIOD = 30;
    let FUTUREDATED_LEAVE_PERIOD = 180;
    const time = d?.getTime();

    if (this.currentUser.leaveBackdatedLockDays) {
      BACKDATED_LEAVE_PERIOD = this.currentUser.leaveBackdatedLockDays;
    }
    if (this.currentUser.leaveFuturedatedLockDays) {
      FUTUREDATED_LEAVE_PERIOD = this.currentUser.leaveFuturedatedLockDays;
    }

    let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
    let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));

    //If isUpdate is true then unlock the dates of current leave
    if (this.isUpdation == true) {
      this.previouslyAppliedLeavesList = this.previouslyAppliedLeavesList.filter(x => x.fromDate != this.leaveObj.fromDate);
    }

    if (this.leaveObj.leaveAppliedFor == 'self') {
      if (this.leaveObj.leaveTypeCode == 'ML') {
        return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
      } else
        if (this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)) {
          return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
        } else {
          return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x => x.getTime() == time) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
        }
    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
      if (this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)) {
        return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
      } else {
        return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x => x.getTime() == time) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
      }
    }


  }

  holidayHighlight: MatCalendarCellClassFunction<Date> = (cellDate, view) => {
    // //console.log("leaveObj  ::  ",this.leaveObj);
    // Only highligh dates inside the month view.
    if (view === 'month') {
      const time = cellDate.getTime()

      // Highlight the holidays.
      if (this.leaveObj.leaveAppliedFor == 'self') {
        if (this.leaveObj.leaveTypeCode == 'ML') {
          this.isWeekOffsExcluded = true;
          return '';
        } else {
          if (this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)) {
            return '';
          } else {
            return (this.holidayDates.find(x => x.getTime() == time)) ? 'holiday-date' : '';
          }
        }

      } else {
        let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
        if (this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)) {
          return '';
        } else {
          return (this.holidayDates.find(x => x.getTime() == time)) ? 'holiday-date' : '';
        }
      }
    }
    return '';
  }

  toDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const time = d?.getTime();
    const currentDate = new Date();
    let FUTUREDATED_LEAVE_PERIOD = 180;
    const DAY_IN_MS = 24 * 60 * 60 * 1000;

    if (this.currentUser.leaveFuturedatedLockDays) {
      FUTUREDATED_LEAVE_PERIOD = this.currentUser.leaveFuturedatedLockDays;
    }

    let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));

    if (!this.leaveObj.fromDate) {
      return false;
    }
    let checkDate = this.leaveObj.fromDate;

    //If isUpdate is true then unlock the dates of current leave
    if (this.isUpdation == true) {
      this.previouslyAppliedLeavesList = this.previouslyAppliedLeavesList.filter(x => x.toDate != this.leaveObj.toDate);
    }

    if (this.leaveObj.leaveAppliedFor == 'self') {
      if (this.leaveObj.leaveTypeCode == 'ML') {
        return ((moment(d).format(dateFormat) >= moment(this.leaveObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat))) ? true : false;
      } else
        if (this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)) {
          return ((moment(d).format(dateFormat) >= moment(this.leaveObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat))) ? true : false;
        } else {
          return ((moment(d).format(dateFormat) >= moment(this.leaveObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x => x.getTime() == time) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat))) ? true : false;
        }
    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
      if (this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)) {
        return ((moment(d).format(dateFormat) >= moment(this.leaveObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat))) ? true : false;
      } else {
        return ((moment(d).format(dateFormat) >= moment(this.leaveObj.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x => x.getTime() == time) && !this.previouslyAppliedLeavesList.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat))) ? true : false;
      }
    }
  }

  setMinToDate(template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.fromDate)) {
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let fromDate = this.leaveObj.fromDate;
    let toDate = document.getElementById('toDate');
    toDate?.setAttribute('min', fromDate);
  }
  syncDayTypeIfSameDate(source: 'from' | 'to') {
  if (
    this.leaveObj.fromDate &&
    this.leaveObj.toDate &&
    moment(this.leaveObj.fromDate).format('YYYY-MM-DD') ===
    moment(this.leaveObj.toDate).format('YYYY-MM-DD')
  ) {
    if (source === 'from') {
      this.leaveObj.toDateDayType = this.leaveObj.fromDateDayType;
    } else {
      this.leaveObj.fromDateDayType = this.leaveObj.toDateDayType;
    }
  }
}


  resetToDate() {
    this.leaveObj.toDate = '';
    this.leaveObj.noOfDays = '';
    this.overLappingTeamMemberList = [];

    //Modified by Priyadarshini
    // let leaveType = this.leaveTypes.find(x => x.leaveTypeMasterId == this.leaveObj.leaveTypeMasterId);
    // if(leaveType != null && (leaveType.leaveTypeCode != 'CL' && leaveType.leaveTypeCode != 'PL' && leaveType.leaveTypeCode != 'LWP')){
    //   if(this.currentUser.probationPeriod != null && this.currentUser.dateOfJoining != null){
    //     if(this.currentUser.employmentstatus != 'Probation'){
    //       this.openAlertMod(this.alertTemplate, "Only LWP & CompOff can be applied during probation period.");
    //       setTimeout(() => {
    //         this.leaveObj.fromDate = '';
    //       }, 100)
    //     }
    //   }
    // }
  }

  async setNoOfDays(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    this.isWeekOffsExcluded = false;

    if (!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.fromDate)) {
      this.alertMessage = "Please select from date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (this.toDateFilter == null) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.leaveObj.toDate)) {
        this.alertMessage = "Please select To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.leaveObj.fromDate || !this.leaveObj.toDate) {
      this.leaveObj.noOfDays = null;
      return false
    }

    //Get Overlaping leave Application
    this.getOverlappedTeamMemberLeave();

    //Check if leave has been already applied between from date & toDate
    let fromDate = moment(this.leaveObj.fromDate).format(dateFormat);
    let toDate = moment(this.leaveObj.toDate).format(dateFormat);
    //console.log(" from date and todate ",fromDate,toDate);
    let isLeaveContained = this.previouslyAppliedLeavesList.find(object => object.toDate <= toDate && object.fromDate >= fromDate);

    if (isLeaveContained) {
      this.leaveObj.toDate = null;
      this.leaveObj.fromDate = null;
      this.openAlertMod(this.alertTemplate, "Already Leave has been applied between the dates, please update the existing leave.");
      return;
    }


    if (this.leaveObj.leaveAppliedFor == 'self') {
      if (this.leaveObj.leaveTypeCode == 'ML') {
        this.isWeekOffsExcluded = true;
        if (moment(this.leaveObj.fromDate).format(dateFormat) == moment(this.leaveObj.toDate).format(dateFormat)) {
          if (this.leaveObj.toDateDayType == null) {
            this.leaveObj.toDateDayType = 0;
          }

        }
        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        if ((moment(this.leaveObj.toDate).format('YYYY-MM-DD') ===
moment(this.leaveObj.fromDate).format('YYYY-MM-DD')) && this.leaveObj.fromDateDayType==0.5 &&   this.leaveObj.toDateDayType == 0.5){
          this.leaveObj.noOfDays=0.5;
        }else{
        this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType) - this.holidayWeekOffCount;
        }
        //console.log(" Maternity leave apply :: no of days test ::  ",this.leaveObj.noOfDays);
      } else {
        this.isWeekOffsExcluded = false;
        // get Holiday Count
        let getHolidayCountObj = new Leave();
        getHolidayCountObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat)
        getHolidayCountObj.toDate = moment(this.leaveObj.toDate).format(dateFormat)

        //const holidayWeekOffCount = 0;
        this.leaveObj.state = this.currentUser.workLocation;
        let response: any = await this.leaveService.getHolidayWeekOffSize(this.leaveObj).toPromise();
        if (response.serviceStatus == "Success") {
          this.holidayWeekOffList = response.serviceResponse;
          //console.log(" holidayWeekOffList  Anurag  ::  ",this.holidayWeekOffList);
          this.holidayWeekOffCount = this.holidayWeekOffList.length;
        } else {
          //console.log(response.serviceResponse);
          this.holidayWeekOffCount = 0;
        }

        if (moment(this.leaveObj.fromDate).format(dateFormat) == moment(this.leaveObj.toDate).format(dateFormat)) {
          if (this.leaveObj.toDateDayType == null) {
            this.leaveObj.toDateDayType = 0;
          }

        }

        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        if ((moment(this.leaveObj.toDate).format('YYYY-MM-DD') ===
moment(this.leaveObj.fromDate).format('YYYY-MM-DD')) && this.leaveObj.fromDateDayType==0.5 &&   this.leaveObj.toDateDayType == 0.5){
          this.leaveObj.noOfDays=0.5;
        }else{
        this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType) - this.holidayWeekOffCount;
        }
      }
      if (this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)) {
        this.isWeekOffsExcluded = true;
        if (moment(this.leaveObj.fromDate).format(dateFormat) == moment(this.leaveObj.toDate).format(dateFormat)) {
          if (this.leaveObj.toDateDayType == null) {
            this.leaveObj.toDateDayType = 0;
          }

        }

        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        if ((moment(this.leaveObj.toDate).format('YYYY-MM-DD') ===
moment(this.leaveObj.fromDate).format('YYYY-MM-DD')) && this.leaveObj.fromDateDayType==0.5 &&   this.leaveObj.toDateDayType == 0.5){
          this.leaveObj.noOfDays=0.5;
        }else{
        this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType) - this.holidayWeekOffCount;
        }
      }
    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
      if (this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)) {
        this.isWeekOffsExcluded = true;
        if (moment(this.leaveObj.fromDate).format(dateFormat) == moment(this.leaveObj.toDate).format(dateFormat)) {
          if (this.leaveObj.toDateDayType == null) {
            this.leaveObj.toDateDayType = 0;
          }
        }

        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        if ((moment(this.leaveObj.toDate).format('YYYY-MM-DD') ===
moment(this.leaveObj.fromDate).format('YYYY-MM-DD')) && this.leaveObj.fromDateDayType==0.5 &&   this.leaveObj.toDateDayType == 0.5){
          this.leaveObj.noOfDays=0.5;
        }else{
        this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType) - this.holidayWeekOffCount;
        }
      } else {
        this.isWeekOffsExcluded = false;
        // get Holiday Count
        let getHolidayCountObj = new Leave();
        getHolidayCountObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat)
        getHolidayCountObj.toDate = moment(this.leaveObj.toDate).format(dateFormat)

        //const holidayWeekOffCount = 0;
        let response: any = await this.leaveService.getHolidayWeekOffSize(this.leaveObj).toPromise();
        if (response.serviceStatus == "Success") {
          this.holidayWeekOffList = response.serviceResponse;
          this.holidayWeekOffCount = this.holidayWeekOffList.length;
        } else {
          //console.log(response.serviceResponse);
          this.holidayWeekOffCount = 0;
        }

        if (moment(this.leaveObj.fromDate).format(dateFormat) == moment(this.leaveObj.toDate).format(dateFormat)) {
            if (this.leaveObj.toDateDayType == null) {
              this.leaveObj.toDateDayType = 0;
            }

        }

        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        if ((moment(this.leaveObj.toDate).format('YYYY-MM-DD') ===
moment(this.leaveObj.fromDate).format('YYYY-MM-DD')) && this.leaveObj.fromDateDayType==0.5 &&   this.leaveObj.toDateDayType == 0.5){
          this.leaveObj.noOfDays=0.5;
        }else{
        this.leaveObj.noOfDays = (START_DAY_COUNT - this.leaveObj.fromDateDayType) + (diff(this.leaveObj.fromDate, this.leaveObj.toDate) - this.leaveObj.toDateDayType) - this.holidayWeekOffCount;
        }
      }
    }
  }
  getHolidayWeekOffSize() {
    const dateFormat = 'YYYY-MM-DD';
    this.leaveObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat)
    this.leaveObj.toDate = moment(this.leaveObj.toDate).format(dateFormat)
    //console.log(this.leaveObj.fromDate, this.leaveObj.toDate,  "this.leaveObj.fromDate, this.leaveObj.toDate");
    this.leaveService.getHolidayWeekOffSize(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.holidayWeekOffList = response.serviceResponse;
        this.holidayWeekOffCount = this.holidayWeekOffList.length;
        //console.log(this.holidayWeekOffCount, "holidayWeekOffCount");
        //console.log(this.holidayWeekOffList , ": this.holidayWeekOffSize ");
      } else {
        // this.openAlertMod(template, response.serviceResponse);
        //console.log(response.serviceResponse);

      }
    });
  }

  onApplyLeave(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    this.leaveObj.reason = this.leaveObj.reason?.trim();
    
    console.log("Leave code",this.leaveObj.leaveTypeCode);

    // Restrict CL for next year
if (this.leaveObj.leaveTypeCode === 'CL') {

  const today = moment(); // current date
  const currentYear = today.year();

  const fromDate = moment(this.leaveObj.fromDate);
  const toDate = moment(this.leaveObj.toDate);
 
  if (
    fromDate.year() > currentYear ||
    toDate.year() > currentYear ||
    fromDate.year() !== toDate.year()
  ) {
    this.openAlertMod(
      template,
      'Casual Leave (CL) cannot be applied for next year or across year boundaries.'
    );
    return;
  }
}


    let inputValidated: boolean = this.validateLeavetObj(this.leaveObj, template)
    if (!inputValidated) return;

    this.checkPolicy(this.leaveObj, this.leavePolicyObj, template).then(response => {
      if (!response) return;

      if (this.leaveObj.noOfDays >= this.level1MinNoOfDays && this.leaveObj.noOfDays < this.level2MinNoOfDays) {
        this.leaveObj.finalApprovalLevel = 2;
      } else if (this.leaveObj.noOfDays >= this.level2MinNoOfDays) {
        if (!this.level2ApprovalTo || this.level2ApprovalTo === "") {
          this.leaveObj.finalApprovalLevel = 2;
        } else {
          this.leaveObj.finalApprovalLevel = 3;
        }
      } else {
        this.leaveObj.finalApprovalLevel = 1;
      }

      if (this.leaveObj.leaveAppliedFor == 'self') {
        // Level 1
        if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
          this.leaveObj.managerId = this.currentUser.reportingManagerId;
          this.leaveObj.approverEmail = this.currentUser.reportingManagerEmail;
          this.leaveObj.approverName = this.currentUser.reportingManagerName;
        } else {
          this.leaveObj.managerId = this.currentUser.managerId;
          this.leaveObj.approverEmail = this.currentUser.managerEmail;
          this.leaveObj.approverName = this.currentUser.managerName;
        }

        // Level 2
        if (this.leaveObj.finalApprovalLevel == 2 || this.leaveObj.finalApprovalLevel == 3) {
          if (this.leaveObj.empId == this.currentUser.hodId || this.level1ApprovalTo == "Reporting Manager") {
            if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
              this.leaveObj.level2ApproverId = this.currentUser.reportingManagerId;
              this.leaveObj.level2ApproverEmail = this.currentUser.reportingManagerEmail;
              this.leaveObj.level2ApproverName = this.currentUser.reportingManagerName;
            } else {
              this.leaveObj.level2ApproverId = this.currentUser.managerId;
              this.leaveObj.level2ApproverEmail = this.currentUser.managerEmail;
              this.leaveObj.level2ApproverName = this.currentUser.managerName;
            }
          } else {
            this.leaveObj.level2ApproverId = this.currentUser.hodId;
            this.leaveObj.level2ApproverEmail = this.currentUser.hodEmail;
            this.leaveObj.level2ApproverName = this.currentUser.hodName;
          }
        }

        // Level 3
        if (this.leaveObj.finalApprovalLevel == 3) {
          if (this.leaveObj.empId == this.level2ApprovalTo) {
            if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
              this.leaveObj.level3ApproverId = this.currentUser.reportingManagerId;
              this.leaveObj.level3ApproverEmail = this.currentUser.reportingManagerEmail;
              this.leaveObj.level3ApproverName = this.currentUser.reportingManagerName;
            } else {
              this.leaveObj.level3ApproverId = this.currentUser.managerId;
              this.leaveObj.level3ApproverEmail = this.currentUser.managerEmail;
              this.leaveObj.level3ApproverName = this.currentUser.managerName;
            }
          } else {
            this.leaveObj.level3ApproverId = this.level2ApprovalTo;
            this.leaveObj.level3ApproverEmail = this.level2ApproverEmail;
            this.leaveObj.level3ApproverName = this.level2ApproverName;
          }
        }

      } else {
        // Approvers for Team Member
        let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
        //console.log(this.leaveObj.empId," data filtered on the basis of teamMember ");

        // Level 1
        if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
          this.leaveObj.managerId = teamMember.reportingManagerId;
          this.leaveObj.approverEmail = teamMember.reportingManagerEmail;
          this.leaveObj.approverName = teamMember.reportingManagerName;
          this.leaveObj.leaveEmpId = teamMember.empId;
          this.leaveObj.empId = this.leaveObj.leaveEmpId;
          this.LeaveObj.seniorManagerId = teamMember.managerId;
          //console.log("Reporting manager ",this.leaveObj.seniorManagerId);

          this.leaveObj.empId = this.leaveObj.leaveEmpId;
        } else {
          this.leaveObj.managerId = teamMember.managerId;
          this.leaveObj.approverEmail = teamMember.managerEmail;
          this.leaveObj.approverName = teamMember.managerName;
          this.leaveObj.leaveEmpId = teamMember.empId;
          // this.leaveObj.empId = teamMember.empId;
          this.leaveObj.empId = this.leaveObj.leaveEmpId;
          this.LeaveObj.seniorManagerId = teamMember.reportingManagerId;
          //console.log("Reporting manager ",this.leaveObj.seniorManagerId);
        }

        // Level 2
        if (this.leaveObj.finalApprovalLevel == 2 || this.leaveObj.finalApprovalLevel == 3) {
          if (this.leaveObj.empId == teamMember.hodId || this.level1ApprovalTo == "Reporting Manager") {
            if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
              this.leaveObj.level2ApproverId = teamMember.reportingManagerId;
              this.leaveObj.level2ApproverEmail = teamMember.reportingManagerEmail;
              this.leaveObj.level2ApproverName = teamMember.reportingManagerName;
              this.leaveObj.leaveEmpId = teamMember.empId;
              // this.leaveObj.empId = teamMember.empId;
              this.leaveObj.empId = this.leaveObj.leaveEmpId;
              this.LeaveObj.seniorManagerId = teamMember.reportingManagerId;
              //console.log("Reporting manager ",this.leaveObj.seniorManagerId);
            } else {
              this.leaveObj.level2ApproverId = teamMember.managerId;
              this.leaveObj.level2ApproverEmail = teamMember.managerEmail;
              this.leaveObj.level2ApproverName = teamMember.managerName;
              this.leaveObj.leaveEmpId = teamMember.empId;
              // this.leaveObj.empId = teamMember.empId;
              this.leaveObj.empId = this.leaveObj.leaveEmpId;
              this.LeaveObj.seniorManagerId = teamMember.reportingManagerId;
              //console.log("Reporting manager ",this.leaveObj.seniorManagerId);
            }
          } else {
            this.leaveObj.level2ApproverId = teamMember.hodId;
            this.leaveObj.level2ApproverEmail = teamMember.hodEmail;
            this.leaveObj.level2ApproverName = teamMember.hodName;
            this.leaveObj.leaveEmpId = teamMember.empId;
            // this.leaveObj.empId = teamMember.empId;
            this.leaveObj.empId = this.leaveObj.leaveEmpId;
          }
        }

        // Level 3
        if (this.leaveObj.finalApprovalLevel == 3) {
          if (this.leaveObj.empId == this.level2ApprovalTo) {
            if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
              this.leaveObj.level3ApproverId = teamMember.reportingManagerId;
              this.leaveObj.level3ApproverEmail = teamMember.reportingManagerEmail;
              this.leaveObj.level3ApproverName = teamMember.reportingManagerName;
              this.leaveObj.leaveEmpId = teamMember.empId;
              this.leaveObj.empId = this.leaveObj.leaveEmpId;
              this.LeaveObj.seniorManagerId = teamMember.reportingManagerId;
              //console.log("Reporting manager ",this.leaveObj.seniorManagerId);
              // this.leaveObj.empId = teamMember.empId;
            } else {
              this.leaveObj.level3ApproverId = teamMember.managerId;
              this.leaveObj.level3ApproverEmail = teamMember.managerEmail;
              this.leaveObj.level3ApproverName = teamMember.managerName;
              this.leaveObj.leaveEmpId = teamMember.empId;
              this.leaveObj.empId = this.leaveObj.leaveEmpId;
              // this.leaveObj.empId = teamMember.empId;
            }
          } else {
            this.leaveObj.level3ApproverId = this.level2ApprovalTo;
            this.leaveObj.level3ApproverEmail = this.level2ApproverEmail;
            this.leaveObj.level3ApproverName = this.level2ApproverName;
            this.leaveObj.leaveEmpId = teamMember.empId;
            this.leaveObj.empId = this.leaveObj.leaveEmpId;
            // this.leaveObj.empId = teamMember.empId;
          }
        }
      }

      // this.leaveObj.empId = this.currentUser.empId; //! this empId will set in getLeaveMetadata()

      this.leaveObj.createdBy = this.currentUser.empId;
      this.leaveObj.isWeekOffsExcluded = this.isWeekOffsExcluded;

      this.leaveObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat)
      this.leaveObj.toDate = moment(this.leaveObj.toDate).format(dateFormat)

      //console.log("Apply Leave : ", this.leaveObj);
      if (this.leaveObj.leaveTypeCode == 'ML') {
        this.leaveObj.fromDateDayType = "0.0";
        this.LeaveObj.toDateDayType = "0.0";
      }
      this.leaveObj.state = this.currentUser.workLocation;
      //console.log("leaveObj  ",this.leaveObj);
      this.leaveService.applyLeave(this.leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          if (this.leaveObj.leaveAppliedFor == 'self') {
            this.showSelfLeaveHistoryTable();
          } else {
            this.showTeamLeaveHistoryTable();
          }
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    });
  }

  onUpdateLeave(template: TemplateRef<any>) {
    this.cancelRequest();
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateLeavetObj(this.leaveObj, template)
    if (!inputValidated) return;

    if (this.leaveObj.noOfDays >= this.level1MinNoOfDays && this.leaveObj.noOfDays < this.level2MinNoOfDays) {
      this.leaveObj.finalApprovalLevel = 2;
    } else if (this.leaveObj.noOfDays >= this.level2MinNoOfDays) {
       if (!this.level2ApprovalTo || this.level2ApprovalTo === "") {
          this.leaveObj.finalApprovalLevel = 2;
        } else {
          this.leaveObj.finalApprovalLevel = 3;
        }
    } else {
      this.leaveObj.finalApprovalLevel = 1;
    }

    if (this.leaveObj.leaveAppliedFor == 'self') {
      // Level 1
      if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
        this.leaveObj.managerId = this.currentUser.reportingManagerId;
        this.leaveObj.approverEmail = this.currentUser.reportingManagerEmail;
        this.leaveObj.approverName = this.currentUser.reportingManagerName;
      } else {
        this.leaveObj.managerId = this.currentUser.managerId;
        this.leaveObj.approverEmail = this.currentUser.managerEmail;
        this.leaveObj.approverName = this.currentUser.managerName;
      }

      // Level 2
      if (this.leaveObj.finalApprovalLevel == 2 || this.leaveObj.finalApprovalLevel == 3) {
        if (this.leaveObj.empId == this.currentUser.hodId) {
          if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
            this.leaveObj.level2ApproverId = this.currentUser.reportingManagerId;
            this.leaveObj.level2ApproverEmail = this.currentUser.reportingManagerEmail;
            this.leaveObj.level2ApproverName = this.currentUser.reportingManagerName;
          } else {
            this.leaveObj.level2ApproverId = this.currentUser.managerId;
            this.leaveObj.level2ApproverEmail = this.currentUser.managerEmail;
            this.leaveObj.level2ApproverName = this.currentUser.managerName;
          }
        } else {
          this.leaveObj.level2ApproverId = this.currentUser.hodId;
          this.leaveObj.level2ApproverEmail = this.currentUser.hodEmail;
          this.leaveObj.level2ApproverName = this.currentUser.hodName;
        }
      }

      // Level 3
      if (this.leaveObj.finalApprovalLevel == 3) {
        if (this.leaveObj.empId == this.level2ApprovalTo) {
          if (this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager") {
            this.leaveObj.level3ApproverId = this.currentUser.reportingManagerId;
            this.leaveObj.level3ApproverEmail = this.currentUser.reportingManagerEmail;
            this.leaveObj.level3ApproverName = this.currentUser.reportingManagerName;
          } else {
            this.leaveObj.level3ApproverId = this.currentUser.managerId;
            this.leaveObj.level3ApproverEmail = this.currentUser.managerEmail;
            this.leaveObj.level3ApproverName = this.currentUser.managerName;
          }
        } else {
          this.leaveObj.level3ApproverId = this.currentUser.hodId;
          this.leaveObj.level3ApproverEmail = this.currentUser.hodEmail;
          this.leaveObj.level3ApproverName = this.currentUser.hodName;
        }
      }


    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.leaveEmpId)

      // Level 1
      if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
        this.leaveObj.managerId = teamMember.reportingManagerId;
        this.leaveObj.approverEmail = teamMember.reportingManagerEmail;
        this.leaveObj.approverName = teamMember.reportingManagerName;
      } else {
        this.leaveObj.managerId = teamMember.managerId;
        this.leaveObj.approverName = teamMember.managerName;
        this.leaveObj.approverEmail = teamMember.managerEmail
      }

      // Level 2
      if (this.leaveObj.finalApprovalLevel == 2 || this.leaveObj.finalApprovalLevel == 3) {
        if (this.leaveObj.leaveEmpId == teamMember.hodId) {
          if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
            this.leaveObj.level2ApproverId = teamMember.reportingManagerId;
            this.leaveObj.level2ApproverEmail = teamMember.reportingManagerEmail;
            this.leaveObj.level2ApproverName = teamMember.reportingManagerName;
          } else {
            this.leaveObj.level2ApproverId = teamMember.managerId;
            this.leaveObj.level2ApproverEmail = teamMember.managerEmail;
            this.leaveObj.level2ApproverName = teamMember.managerName;
          }
        } else {
          this.leaveObj.level2ApproverId = teamMember.hodId;
          this.leaveObj.level2ApproverEmail = teamMember.hodEmail;
          this.leaveObj.level2ApproverName = teamMember.hodName;
        }
      }

      // Level 3
      if (this.leaveObj.finalApprovalLevel == 3) {
        if (this.leaveObj.leaveEmpId == teamMember.hodId) {
          if (teamMember.reportingManagerId != null && teamMember.approvalsTo == "Reporting Manager") {
            this.leaveObj.level3ApproverId = teamMember.reportingManagerId;
            this.leaveObj.level3ApproverEmail = teamMember.reportingManagerEmail;
            this.leaveObj.level3ApproverName = teamMember.reportingManagerName;
          } else {
            this.leaveObj.level3ApproverId = teamMember.managerId;
            this.leaveObj.level3ApproverEmail = teamMember.managerEmail;
            this.leaveObj.level3ApproverName = teamMember.managerName;
          }
        } else {
          this.leaveObj.level3ApproverId = teamMember.hodId;
          this.leaveObj.level3ApproverEmail = teamMember.hodEmail;
          this.leaveObj.level3ApproverName = teamMember.hodName;
        }
      }
    }

    this.leaveObj.updatedBy = this.currentUser.empId;
    this.leaveObj.isWeekOffsExcluded = this.isWeekOffsExcluded;
    this.leaveObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat)
    this.leaveObj.toDate = moment(this.leaveObj.toDate).format(dateFormat)
    //console.log(" this.leaveObj : ", this.leaveObj);
    //console.log("leave emp id : ", this.leaveObj.leaveEmpId);


    this.leaveService.updatePendingLeave(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        if (this.leaveObj.leaveAppliedFor == 'self') {
          this.showSelfLeaveHistoryTable();
        } else {
          this.showTeamLeaveHistoryTable();
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getOverlappedTeamMemberLeave(overLapLeaveTemplate?: TemplateRef<any>) {
    let leaveObj = new Leave();
    const dateFormat = 'YYYY-MM-DD';

    this.isOverlapsedLeaveTable = true;

    leaveObj.empId = this.currentUser.empId;
    leaveObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat);
    leaveObj.toDate = moment(this.leaveObj.toDate).format(dateFormat);

    this.leaveService.getOverlappedTeamMemberLeave(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.overLappingTeamMemberList = response.serviceResponse;
        this.overLappingTeamMemberList = this.overLappingTeamMemberList.filter(x => x.empId != this.currentUser.empId);

        this.overLappingTeamMemberList.forEach((object) => {
          object.employeementId = ("A-").concat(object.employeementId);
          object.fromDate = (object.fromDate) ? moment(object.fromDate).format(AppComponent.DATE_FORMAT) : null;
          object.toDate = (object.toDate) ? moment(object.toDate).format(AppComponent.DATE_FORMAT) : null;
        });

        if (overLapLeaveTemplate != null && overLapLeaveTemplate != undefined) {
          this.modalRef = this.modalService.open(overLapLeaveTemplate, { modalDialogClass: 'modal-lg' });
        }
        //console.log("this.overLappingTeamMemberList : ", this.overLappingTeamMemberList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  deletePendingLeave(template: TemplateRef<any>) {
    this.cancelRequest();
    this.leaveObj.empId = this.currentUser.empId;
    this.leaveObj.email = this.currentUser.email;
    this.leaveObj.employeeName = this.currentUser.name;
    this.leaveObj.employeementId = this.currentUser.employeementId;
    this.leaveObj.managerId = this.currentUser.managerId;
    this.leaveObj.managerEmail = this.currentUser.managerEmail;
    this.leaveObj.managerName = this.currentUser.managerName;
    //console.log(" this.leaveObj ",this.leaveObj)
    this.leaveService.deletePendingLeave(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log("After Delete this.leaveObj : ", this.leaveObj);
        this.openAlertMod(template, response.serviceResponse);
        if (this.isSelfLeaveHistory) {
          this.showSelfLeaveHistoryTable();
        } else {
          this.showTeamLeaveHistoryTable();
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  getLeaveMetadata() {
    //console.log("leave Obj For getLeaveMetadata : ", this.leaveObj);
    let userObj: User = new User();
    if (this.leaveObj.leaveAppliedFor == 'self') {
      this.leaveObj.empId = this.currentUser.empId;
      userObj.empId = this.currentUser.empId;
      userObj.employmentstatus = this.currentUser.employmentstatus;
      this.leaveObj.name = this.currentUser.name;
      this.leaveObj.email = this.currentUser.email;
      this.leaveObj.employeementId = this.currentUser.employeementId;
    } else {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
      //console.log("Team Member : ", teamMember);
      userObj.empId = teamMember.empId;
      userObj.employmentstatus = teamMember.employmentstatus;
      this.leaveObj.name = teamMember.name;
      this.leaveObj.email = teamMember.email;
      this.leaveObj.employeementId = teamMember.employeementId;
    }

    // this.getAllLeaveTypesByLeavePolicies(userObj);
    this.getAllMyLeaveApplicationsByEmpId(userObj);
  }

  getAllTeamMemberList() {
    this.teamMemberList = [];
    this.leaveDetails = [];
    this.leaveObj.empId = '';
    this.leaveObj.leaveTypeMasterId = '';
    this.leaveObj.fromDate = '';
    this.leaveObj.toDate = '';
    this.leaveObj.noOfDays = '';
    this.leaveObj.reason = '';
    this.leaveObj.fromDateDayType = '';
    this.leaveObj.toDateDayType = '';

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

  getAllMyLeaveApplicationsByEmpId(userObj: User) {
    this.leaveHistoryList = [];
    this.leaveHistoryListForTable = [];

    let leaveObj = new Leave();
    leaveObj.empId = userObj.empId;
    this.leaveService.getAllMyLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.previouslyAppliedLeavesList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.previouslyAppliedLeavesList = this.previouslyAppliedLeavesList.filter(leaveApplication => (leaveApplication.status != 'Rejected' && leaveApplication.status != 'Revoked'));
        this.leaveHistoryList = response.serviceResponse;
        this.leaveHistoryListForTable = response.serviceResponse;
        //console.log(" leaveHistory ln 1455   ",this.leaveHistoryList);
        this.leaveHistoryList.forEach(leave => {
          leave.noOfDaysDisplay = (leave.noOfDays) ? leave.noOfDays + " day(s)" : null;


          //console.log(" leave in foreach   ",leave);
          //console.log("  this.leaveObj.leaveTypeMasterId   ",this.leaveObj.leaveTypeMasterId);
        });

        this.leaveHistoryListForTable.forEach(leave => {

          leave.checkDate = new Date(leave.fromDate);
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          if (!leave.currentApprovalLevel && !leave.finalApprovalLevel) {
            leave.currentApprovalLevel = 1;
            leave.finalApprovalLevel = 1;
          }
        });
        //console.log("leaveHistoryListForTable : ", this.leaveHistoryListForTable);

        //console.log("leaveHistoryList : ", this.leaveHistoryList);
        // this.leaveHistoryList = this.leaveHistoryList.filter(leaveApplication => leaveApplication.status !== 'Rejected');
        // this.filteredMyLeaveApplication();

      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // filteredMyLeaveApplication(){
  //   this.filterLeaveHistoryList = this.leaveHistoryList.filter(leaveApplication => leaveApplication.status !== 'Rejected');
  //   //console.log("allEmployeeList : ", this.filterLeaveHistoryList)
  // }

  getAllMyTeamApplicationsByEmpId(userObj: User) {
    this.leaveHistoryList = [];

    let leaveObj = new Leave();
    leaveObj.empId = userObj.empId;
    this.leaveService.getAllMyTeamApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveHistoryList = response.serviceResponse;
        this.previouslyAppliedLeavesList = JSON.parse(JSON.stringify(response.serviceResponse));
        this.previouslyAppliedLeavesList = this.previouslyAppliedLeavesList.filter(leaveApplication => (leaveApplication.status != 'Rejected' && leaveApplication.status != 'Revoked'));

        this.leaveHistoryList.forEach(leave => {
          leave.checkDate = new Date(leave.fromDate);
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          leave.noOfDaysDisplay = (leave.noOfDays) ? leave.noOfDays + " day(s)" : null;
          if (!leave.currentApprovalLevel && !leave.finalApprovalLevel) {
            leave.currentApprovalLevel = 1;
            leave.finalApprovalLevel = 1;
          }
        });
        //console.log("leaveHistoryList : ", this.leaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllLeaveTypes() {
    // this.leaveTypes = [];

    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        console.log("leaveTypes : ", this.leaveTypes);

        //console.log("getAllLeaveTypes     ",this.getAllLeaveTypes);

      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllLeaveTypesByLeavePolicies(userObj: User) {
    // this.leaveTypes = [];

    let leaveObj = new Leave();
    leaveObj.employmentStatus = userObj.employmentstatus;
    leaveObj.gender = this.currentUser.gender;
    leaveObj.maritalStatus = this.currentUser.maritalStatus;

    //console.log("   leaveObj   ",leaveObj);

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        //console.log("leaveTypes : ", this.leaveTypes);

        //console.log("getAllLeaveTypesByLeavePolicies    ",this.leaveTypes);

      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getLeaveLogsByEmpId() {
    this.leaveLogList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.getLeaveLogsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveLogList = response.serviceResponse;
        this.leaveLogList.forEach(log => {
          log.createdOn = (log.createdOn) ? moment(log.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log("leaveLogList : ", this.leaveLogList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getMyLeaveBalancesByEmpId() {
    this.leaveBalanceList = [];

    let leaveObj = new Leave();
    leaveObj.employeementId = this.currentUser.employeementId;
    if (this.currentUser.isApmosysProduct === 'true') {
      leaveObj.employeeType = 'Apmosys Product';
    } else {
      leaveObj.employeeType = 'Other';
    }
    this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        this.leaveBalanceList = this.leaveBalanceList.map(leaveType => {
          if (leaveType.leaveTypeCode !== "LWP") {
            leaveType.totalLeaveBalance = leaveType.pendingForApproval + leaveType.balance;
          } else {
            leaveType.totalLeaveBalance = leaveType.balance;

          }
          return leaveType;
        });
        //console.log("leaveBalanceList : ", this.leaveBalanceList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllHolidays() {
    this.holidayList = [];
    this.holidayDates = [];
    let holidayObj = new Holiday();
    holidayObj.state = this.currentUser.workLocation;
    //console.log(" Worklocation ::  ",this.currentUser);
    this.holidayService.getAllHolidays(holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayDates = this.holidayList.map(holiday => new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')));
        //   this.holidayDates = this.holidayList.map(holiday => ({
        //     date: new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')),
        //     state: holiday.state
        // }));

        //console.log("holidayDates : ", this.holidayDates);
        //console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  onRevokeApprovedLeaveApplication(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateRevokeObj(this.leaveObj, template)
    if (!inputValidated) return;

    this.cancelRequest();

    this.leaveObj.createdBy = this.currentUser.empId;
    this.leaveService.revokeApprovedLeaveApplication(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        if (this.leaveObj.leaveAppliedFor == 'self') {
          this.showSelfLeaveHistoryTable();
        } else {
          this.showTeamLeaveHistoryTable();
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
        console.error(response.serviceResponse);
      }
    });
  }

  getRevokeLeaveApplicationByEmpId() {
    this.revokeLeaveApplicationList = [];

    this.leaveObj.empId = this.currentUser.empId;
    this.leaveService.getRevokeLeaveApplicationByEmpId(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.revokeLeaveApplicationList = response.serviceResponse;
        this.revokeLeaveApplicationList.forEach(leave => {
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log(this.revokeLeaveApplicationList, " : revokeLeaveApplicationList");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // added by anurag
  findLeaveDaysOnMaternitySelect(leaveObj: Leave) {
    let leaveDays = null;
    leaveObj.fromDate = '';
    this.leaveObj.toDate = '';
    this.leaveObj.noOfDays = '';
    let leave = new Leave();
    //console.log("maternityType    ",leaveObj);
    leave.maternityType = leaveObj.maternityType;
    //console.log("maternityType    ",leave);
    this.leaveService.getMaternityLeaveDaysByMaternityType(leave).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        leave = response.serviceResponse;
        // this.leaveObj.noOfDays = leave.maternityLeaveDays
        this.leaveObj.maternityType = leave.maternityType;
        this.allowedLeaveDays = leave.maternityLeaveDays;
        //console.log(leaveDays,"   leaveDays  ",leave.maternityLeaveDays ,"  this.leaveObj.noOfDays  ")
      }
    });
  }

  getAppliedPreviousLeaveByFromAndToDate(fromDate: Date, toDate: Date, template: TemplateRef<any>) {
    this.getListOfAppliedLeaveBetweenFromAndToDate = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    leaveObj.fromDate = fromDate;
    leaveObj.toDate = toDate;
    this.leaveService.getLeaveAppliedListByFromAndToDate(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.getListOfAppliedLeaveBetweenFromAndToDate = response.serviceResponse;
        //console.log(" leave find ",this.getListOfAppliedLeaveBetweenFromAndToDate);

      } else {
        this.getListOfAppliedLeaveBetweenFromAndToDate = response.serviceResponse;
        //console.log(" else leave find ",this.getListOfAppliedLeaveBetweenFromAndToDate);
        this.leaveObj.fromDate = '';
        this.leaveObj.toDate = '';
        this.leaveObj.noOfDays = '';
        this.openAlertMod(template, " Maternity Leaves already applied in between " + fromDate + " to " + toDate);

      }
    });


  }

  getAllMyTeamLeaveRevokeApplicationsByEmpId(user: User) {
    this.revokeLeaveApplicationList = [];

    this.leaveObj.empId = user.empId;
    this.leaveService.getAllMyTeamLeaveRevokeApplicationsByEmpId(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.revokeLeaveApplicationList = response.serviceResponse;
        this.revokeLeaveApplicationList.forEach(leave => {
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log(this.revokeLeaveApplicationList, " : revokeLeaveApplicationList");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // Portal Config Data for Leave Escalation
  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let portalConfigList = response.serviceResponse;
        for (let portal of portalConfigList) {

          if (portal.configName == 'Leave Approval Escalation (Level 1)') {
            this.level1MinNoOfDays = portal.configPeriod;
            this.level1ApprovalTo = portal.configValue;
          }
          if (portal.configName == 'Leave Approval Escalation (Level 2)') {
            this.level2MinNoOfDays = portal.configPeriod;
            this.level2ApprovalTo = JSON.parse(portal.configValue);
          }
          if (portal.configName == 'Leave week-off/holiday exclusion') {
            let parsedValue = portal.configValue;
            try {
              parsedValue = JSON.parse(portal.configValue);
            } catch (e) {
              parsedValue = portal.configValue;
            }

            // Ensure it's always an array
            this.weekOffExcludedDepartmentList = Array.isArray(parsedValue)
              ? parsedValue
              : [parsedValue];

            console.log("this.weekOffExcludedDepartmentList:", this.weekOffExcludedDepartmentList);
          }
        }

        if (this.level2ApprovalTo) {
          let employee = new Employee();
          employee.empId = this.level2ApprovalTo;
          this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              let employeeData = response.serviceResponse;
              this.level2ApproverName = employeeData.name;
              this.level2ApproverEmail = employeeData.email;
            } else {
              console.error(response.serviceResponse)
            }
          });
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllLeaveBalanceByEmpId(leaveObj: Leave) {
    //console.log("employyid:  ", leaveObj.empId )
    //console.log("leaveTypeMasterId:  ", leaveObj.leaveTypeMasterId )
    this.leaveService.getAllLeaveBalanceByEmpId(this.leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log("Leave Balance Response : ", response)
        this.leaveBalance = response.serviceResponse;
        this.leaveDetails = this.leaveBalance.map(user => `${user.leaveType} : ${user.balance}`);

        //console.log(this.leaveDetails,   "     : leaveDetails");

      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // CompOff Details
  getAvailableCompOffDetails(leaveObj: Leave, template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';

    if (leaveObj.leaveTypeCode == "CO") {
      let compOffObj = new Leave();
      compOffObj.empId = leaveObj.empId;
      compOffObj.fromDate = moment(this.leaveObj.fromDate).format(dateFormat);

      this.leaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.availableCompOffDetails = response.serviceResponse;
          //console.log("this.availableCompOffDetails : ", this.availableCompOffDetails);
          let balanceAvailable = 0;

          this.availableCompOffDetails.forEach(compOff => {
            balanceAvailable = balanceAvailable + compOff.noOfDays;
          });

          this.leaveBalance.forEach(leave => {
            leave.balance = balanceAvailable;
          });

          this.leaveDetails = this.leaveBalance.map(user => `${user.leaveType} : ${user.balance} before ${compOffObj.fromDate}`);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

    if (this.leaveObj.maternityType === 'miscarriage' || this.leaveObj.maternityType === 'adoption' || this.leaveObj.maternityType === 'fullMaternity') {
      //console.log(this.leaveObj);
      let isLeaveApplied = null;
      let fromDate = moment(this.leaveObj.fromDate); // Create a moment object for fromDate
      let toDate = fromDate.clone().add(this.allowedLeaveDays, 'days'); // Add maternity leave days to fromDate
      this.leaveObj.toDate = toDate.format(dateFormat); // Assign formatted toDate to leaveObj.toDate

      let fromDate1 = fromDate;
      this.leaveObj.fromDate = fromDate1.format(dateFormat);

      //console.log(" fromDate in format    ",this.leaveObj.fromDate);

      this.getAppliedPreviousLeaveByFromAndToDate(this.leaveObj.fromDate, this.leaveObj.toDate, template);
      //  isLeaveApplied = !this.previouslyAppliedLeavesList.find(leaveApplication =>  moment(fromDate).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat));
      this.leaveObj.fromDateDayType = '0';
      this.leaveObj.toDateDayType = '0';
      //console.log("   this.getListOfAppliedLeaveBetweenFromAndToDate     ",this.getListOfAppliedLeaveBetweenFromAndToDate);

      if (!this.getListOfAppliedLeaveBetweenFromAndToDate || this.getListOfAppliedLeaveBetweenFromAndToDate.length === 0) {


        const START_DAY_COUNT = 1;
        const diff = (e, t) => Math.abs(Math.floor((new Date(e).getTime() - new Date(t).getTime()) / (1000 * 60 * 60 * 24)));
        this.leaveObj.noOfDays = (diff(fromDate, toDate));

        //console.log(" this.leaveObj.toDate ", this.leaveObj.toDate, "  toDate  ", toDate.format(dateFormat));

      }

    }

  }

  // exportToExcel(id:any): void {
  //   if (this.isLeaveHistoryTable == true) {
  //     this.excelName = 'MyLeaveHistory.xlsx';
  //     this.tableName = 'Leave History Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }

  //   // Commented section kept as in your original code
  //   // if (this.isLeaveLogTable == true) {
  //   //   this.excelName = 'MyLeaveLogs.xlsx';
  //   //   this.tableName = 'Leave Logs Table';
  //   //   this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   // }

  //   if (this.isSelfLeaveRevokeApplication == true) {
  //     this.excelName = 'MyRevokeLeaveHistory.xlsx';
  //     this.tableName = 'Self Revoke Leave Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }

  //   if (this.isTeamLeaveRevokeApplication == true) {
  //     this.excelName = 'TeamRevokeLeaveHistory.xlsx';
  //     this.tableName = 'Team Revoke Leave Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }

  //   if (this.isOverlapsedLeaveTable == true) {
  //     this.excelName = 'overlapsedLeaveReport.xlsx';
  //     this.tableName = 'Overlapped Leave Report';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }
  // }
  exportToExcel(): void {

    if (this.isLeaveHistoryTable == true) {
      this.elementName = 'history-table';
      this.excelName = 'MyLeaveHistory.xlsx';
    }

    // if(this.isLeaveLogTable == true){
    //   this.elementName = 'log-table';
    //   this.excelName = 'MyLeaveLogs.xlsx';
    // }

    if (this.isSelfLeaveRevokeApplication == true) {
      this.elementName = 'revoke-history-table';
      this.excelName = 'MyRevokeLeaveHistory.xlsx';
    }

    if (this.isTeamLeaveRevokeApplication == true) {
      this.elementName = 'revoke-history-table';
      this.excelName = 'TeamRevokeLeaveHistory.xlsx';
    }

    if (this.isOverlapsedLeaveTable == true) {
      this.elementName = 'overlapsedInfo';
      this.excelName = 'overlapsedLeaveReport.xlsx';
    }

    let element = document.getElementById(this.elementName);
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(element);

    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

    XLSX.writeFile(book, this.excelName);
  }

  // this.leaveHistoryListForTable

  exportToExcelForLeave() {
    this.excelName = 'MyLeaveHistory.xlsx';

    const onlySpecificDataArr: any = this.leaveHistoryList.map(
      x => ({
        "Name": x.employeeName,
        "Leave Type": x.leaveType,
        "From Date": x.fromDate,
        "To Date": x.toDate,
        "Duration": (x.noOfDays + " day(s)"),
        "Status": x.status,
        "Applied By": x.createdByName,
        "Applied On": x.createdOn,
        "Reason": x.reason,
        "Approved By": x.approverName,
        "Remark": x.remark
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
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


  showApprovedLeaveLogTable() {
    this.isLeaveApprovedByMeTable = true;
    this.isLeaveLogTable = false;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;
    this.getApprovedLeaveLogsByEmpId();
  }

  showEmpLeaveExclusion() {
    this.isLeaveApprovedByMeTable = false;
    this.isLeaveLogTable = false;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isLeaveRevokeApplicationTable = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isOverlapsedLeaveTable = false;
  }

  getApprovedLeaveLogsByEmpId() {
    this.approvedLeaveLogList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    leaveObj.managerId = this.currentUser.empId;
    leaveObj.approverEmail = this.currentUser.email;
    this.leaveService.getApprovedLeaveLogsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.approvedLeaveLogList = response.serviceResponse;
        this.approvedLeaveLogList.forEach(log => {
          log.createdOn = (log.createdOn) ? moment(log.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("approvedLeaveLogList : ", this.approvedLeaveLogList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  onRevoke(template: TemplateRef<any>,) {
    this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)) {
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.onUpdateLeaveStatus(template, this.leaveObj, 3);
  }


  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId) {
    this.cancelRequest();
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    leaveApplication.rejectReason = leaveApplication.rejectReason?.trim();

    console.log("leaveApplication : ", leaveApplication);

    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  openLeaveRejectModal(template: TemplateRef<any>, leave: any) {
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }

  exportToExcelForApprovedLeave() {
    this.excelName = 'ApprovedLeaveLog.xlsx';

    const exportData = this.approvedLeaveLogList.map((x, index) => ({
      "Sr. No": index + 1,
      "Employee Name": x.employeeName,
      "Leave Type": x.leaveType,
      "From Date": x.fromDate,
      "To Date": x.toDate,
      "Duration": x.noOfDays + " day(s)",
      "Status": x.status,
      "Applied By": x.createdByName,
      "Applied On": x.createdOn,
      "Reason": x.reason
    }));

    this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
  }


}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}


