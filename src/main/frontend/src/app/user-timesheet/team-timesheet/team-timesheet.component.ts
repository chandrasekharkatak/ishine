import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';


@Component({
  selector: 'app-team-timesheet',
  templateUrl: './team-timesheet.component.html',
  styleUrls: ['./team-timesheet.component.css']
})
export class TeamTimesheetComponent implements OnInit {

  data: string;
  feature = "Team Timesheets";
  currentUser: User;
  userMapping: any = {};

  //flags 
  isAllTimesheetTable: boolean = false;
  isAllTimesheetRequestTable: boolean = false;

  //excel
  excelName = '';
  allTeamTimesheetDataForExcel: any[] = [];
  allTeamTimesheetRequestDataForExcel: any[] = [];

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  allTeamTimesheets: any[] = [];
  allTeamTimesheetRequests: Timesheet[] = [];

  timesheetObj: Timesheet = new Timesheet();
  startDate: any;
  endDate: any;

  isSelectAll: boolean = false;
  isSelect: boolean = false;
  bulkApprove: any = [];
  bulkReject: any = [];

  constructor(
    public validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy
  ) {
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
    this.getAllTeamTimesheets();
    this.page = 1;
    this.data = ''
  }

  showAllTimesheetRequestsTable() {
    this.isAllTimesheetRequestTable = true;

    this.isAllTimesheetTable = false;

    this.getMyReporteesTimesheetRequests();
    this.page = 1;
    this.data = ''
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
    console.log("timesheet obj  : ", timesheetObj)
    this.timesheetService.getMyReporteesApprovedTimesheets(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheets = response.serviceResponse;

        for (let x of this.allTeamTimesheets) {
          x.employeementId = "A-".concat(x.employeementId);
          x.date = (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null;
          x.officeInTime = (x.officeInTime) ? moment(x.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.officeOutTime = (x.officeOutTime) ? moment(x.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.createdOn = (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        }

        console.log("allTeamTimesheets :", this.allTeamTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getMyReporteesTimesheetRequests() {
    this.allTeamTimesheetRequests = [];
    this.isSelectAll = false
    this.bulkApprove = []
    this.bulkReject = []

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        for (let x of this.allTeamTimesheetRequests) {
          x.employeementId = "A-".concat(x.employeementId);
          x.date = (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null;
          x.officeInTime = (x.officeInTime) ? moment(x.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.officeOutTime = (x.officeOutTime) ? moment(x.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          x.createdOn = (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        }
        console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
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
    timesheetObj.employeementId = timesheetObj.employeementId.substring(2);
    timesheetObj.timesheetStatusUpdatedBy = this.currentUser.empId;

    this.timesheetService.updateTimesheetRequestById(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllTimesheetRequestsTable()
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
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  // openBulkRejectTimesheet

  openBulkRejectTimesheet(template: TemplateRef<any>) {

    this.cancelRequest();

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


  /* View TImesheet details */
  getAllMyActivitiesByTimesheetId(timesheet: any) {
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;

    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  revokeApprovedTimesheet(template: TemplateRef<any>) {
    this.cancelRequest();
    let timesheetObj = Object.assign({}, this.timesheetObj);
    timesheetObj.employeementId = timesheetObj.employeementId?.substring(2)

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
      this.excelName = 'AllTeamTimesheet.xlsx';

      this.allTeamTimesheetDataForExcel = this.allTeamTimesheets;
      const onlySpecificDataArr = this.allTeamTimesheetDataForExcel.map(
        x => ({
          "Employee Id": x.employeementId,
          "Employee Name": x.employeeName,
          "Date": x.date,
          "Day Type": x.dayType,
          "Timesheet Details": x.description?.replaceAll('<br>', ' \n'),
          "Total Time": x.totalTime,
          "Office In Time": x.officeInTime,
          "Office Out Time": x.officeOutTime,
          "Total Office Working Hours": x.totalWorkingOfficeHours,
          "Status": x.status,
          "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
          "Leave Type": x.leaveType,
          "Remarks": x.remarks
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
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openRevokeApprovedTimesheet(template: TemplateRef<any>, timesheet: any) {
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  // opnenRejectTimesheet(template: TemplateRef<any>, timesheet: any){
  //   this.timesheetObj = timesheet;
  //   this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  // }

  cancelRequest() {
    this.modalRef.hide();
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

  items = 10;
  handleItemsChange(event) {
    this.items = event;

  }


  //sorting timesheet	
  sortTimeSheet(sort: Sort) {
    //console.log(sort);	
    const data = this.allTeamTimesheets;


    if (!sort.active || sort.direction === '') {
      this.allTeamTimesheets = data;
      return;
    } else {
      this.allTeamTimesheets = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'employeementId':
              return compare(a.employeementId, b.employeementId, isAsc)
            case 'employeeName':
              return compare(a.employeeName, b.employeeName, isAsc)
            case 'date':
              return compare(a.date, b.date, isAsc)
            case 'dayType':
              return compare(a.dayType, b.dayType, isAsc)
            case 'description':
              return compare(a.description, b.description, isAsc);
            case 'totalTime':
              return compare(new Date(a.totalTime).getTime(), new Date(b.totalTime).getTime(), isAsc);
            case 'officeInTime':
              return compare(new Date(a.officeInTime).getTime(), new Date(b.officeInTime).getTime(), isAsc);
            case 'officeOutTime':
              return compare(new Date(a.officeOutTime).getTime(), new Date(b.officeOutTime).getTime(), isAsc);
            case 'totalWorkingOfficeHours':
              return compare(a.totalWorkingOfficeHours, b.totalWorkingOfficeHours, isAsc);
            case 'status':
              return compare(a.status, b.status, isAsc)
            case 'remarks':
              return compare(a.remarks, b.remarks, isAsc);
            case 'leaveType':
              return compare(a.leaveType, b.leaveType, isAsc);
            default:
              return 0;
          }
        }
      )
    }


  }

  sortAllTimesheet(sort: Sort) {
    ////console.log(sort);	
    const data = this.allTeamTimesheetRequests;
    if (!sort.active || sort.direction === '') {
      this.allTeamTimesheetRequests = data;
      return;
    } else {
      this.allTeamTimesheetRequests = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'employeementId':
              return compare(a.employeementId, b.employeementId, isAsc)
            case 'employeeName':
              return compare(a.employeeName, b.employeeName, isAsc)
            case 'date':
              return compare(a.date, b.date, isAsc)
            case 'dayType':
              return compare(a.dayType, b.dayType, isAsc)
            case 'description':
              return compare(a.description, b.description, isAsc);
            case 'createdByName':
              return compare(a.createdByName, b.createdByName, isAsc)
            case 'totalTime':
              return compare(new Date(a.totalTime).getTime(), new Date(b.totalTime).getTime(), isAsc);
            case 'officeInTime':
              return compare(new Date(a.officeInTime).getTime(), new Date(b.officeInTime).getTime(), isAsc);
            case 'officeOutTime':
              return compare(new Date(a.officeOutTime).getTime(), new Date(b.officeOutTime).getTime(), isAsc);
            case 'totalWorkingOfficeHours':
              return compare(a.totalWorkingOfficeHours, b.totalWorkingOfficeHours, isAsc);
            case 'status':
              return compare(a.status, b.status, isAsc)
            default:
              return 0;
          }
        }
      )
    }
  }
  sortMyTimesheetView(sort: Sort) {
    //console.log(sort);
    const data = this.timesheetObj.allTimesheetActivities;
    if (!sort.active || sort.direction === '') {
      this.timesheetObj.allTimesheetActivities = data;
      return;
    } else {
      this.timesheetObj.allTimesheetActivities = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'clientName':
              return compare(a.clientName, b.clientName, isAsc)
            case 'clientLocation':
              return compare(a.clientLocation, b.clientLocation, isAsc)
            case 'projectName':
              return compare(a.projectName, b.projectName, isAsc)

            case 'teamName':
              return compare(a.teamName, b.teamName, isAsc);
            case 'description':
              return compare(a.description, b.description, isAsc);
            case 'activity':
              return compare(a.activity, b.activity, isAsc);
            case 'completionTime':
              return compare(a.completionTime, b.completionTime, isAsc);

            default:
              return 0;
          }
        }
      )
    }


  }

  selectAll(event) {
    this.bulkApprove = [];
    this.bulkReject = [];

    const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
    checkboxes.forEach((checkbox: any) => {
      console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedTimesheet = this.allTeamTimesheetRequests.find((_timesheet, index) => index == checkboxIndex);

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

    console.log("clicked on : ", timesheetObj);

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

    console.log("Updated Bulk List : ", this.bulkApprove);
  }

  openBulkApprovalModal(nightShiftTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.show(nightShiftTemplate, { class: 'modal-lg' });
    } else {
      this.onBulkApproval(alertTemplate);
    }
  }

  bulkApproveWithoutNightShiftRequest(template: TemplateRef<any>) {
    this.bulkApprove = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if(this.bulkApprove.length !== 0){
      this.onBulkApproval(template);
    }else{
      this.cancelRequest();
      this.showAllTimesheetRequestsTable();
    }
  }

  openBulkRejectModal(nightShiftTemplate: TemplateRef<any>, bulkRejectTimesheet: TemplateRef<any>) {
    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.show(nightShiftTemplate, { class: 'modal-lg' });
    } else {
      this.openBulkRejectTimesheet(bulkRejectTimesheet);
    }
  }

  bulkRejectWithoutNightShiftRequest(bulkRejectTimesheet: TemplateRef<any>) {
    this.timesheetObj.rejectReason = null;
    this.bulkReject = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if(this.bulkReject.length !== 0){
      this.openBulkRejectTimesheet(bulkRejectTimesheet);
    }else{
      this.cancelRequest();
      this.showAllTimesheetRequestsTable();
    }
  }

  onBulkApproval(template: TemplateRef<any>) {
    this.cancelRequest();
    console.log("Updated Bulk List : ", this.bulkApprove);
    let timesheetObj = new Timesheet();
    timesheetObj.bulkApprovedList = this.bulkApprove;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.status = "Approved"
    console.log("For Bulk Update : ", timesheetObj);
    timesheetObj.bulkApprovedList.forEach((x) => {
      x.employeementId = x.employeementId.substring(2)
    })
    this.timesheetService.bulkApproveTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Approved Successfully ");
        this.showAllTimesheetRequestsTable();
        this.bulkApprove = [];
        this.bulkReject = [];
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  OnBulkReject(template: TemplateRef<any>) {
    console.log("Updated Bulk List : ", this.bulkReject);
    let timesheetObj = new Timesheet();
    timesheetObj.bulkRejectList = this.bulkReject;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.status = "Rejected"
    timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
    console.log("For Bulk Update : ", timesheetObj);
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



}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}


