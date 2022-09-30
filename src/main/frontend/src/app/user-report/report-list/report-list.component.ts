import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { LeaveService } from 'src/app/services/leave.service';
import { Feature } from 'src/app/models/feature';
import { Query } from 'src/app/models/query';

class FilterData{
  title:any;
  columns:any;
  queryList:any;
}
@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {

  feature = 'Reports';
  currentUser: User;
  userMapping: any = {};

  data:string; //Search Data 

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isLeaveReportTable:boolean = false;
  isTimesheetReportTable:boolean = false;
  isEmployeeReportTable:boolean = false;

  allEmployeeList:any[] = [];

  allLeaveApplicationsList:any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList:any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

  excelName:any;

  leaveColumns:any[] = ['employeementId', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'noOfDays', 'reason', 'status', 'managerName', 'hodName', 'createdOn', 'updatedOn', 'leaveStatusUpdatedByName'];
  queryList:any[] = [];
  filterData:any = new FilterData();

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private leaveService : LeaveService,
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
  }

  sectionViewInit() {
    if(this.userMapping.leave_report){
      this.showLeaveReportTable();
    }else if(this.userMapping.timesheet_report){
      this.showTimesheetReportTable();
    }else if(this.userMapping.employee_report){
      this.showEmployeeReportTable();
    }
  }

  showLeaveReportTable(){
    this.isLeaveReportTable = true;

    this.isTimesheetReportTable = false;
    this.isEmployeeReportTable = false;

    this.getAllLeaveApplicationsList();
  }

  showTimesheetReportTable(){
    this.isTimesheetReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isEmployeeReportTable = false;

    this.getAllTimesheetApplicationsList();
  }

  showEmployeeReportTable(){
    this.isEmployeeReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;

    this.getAllEmployeeList();
  }

  // Leave Report 
  getAllLeaveApplicationsList() {
    this.allLeaveApplicationsList = [];

    this.leaveService.leaveReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveApplicationsList = response.serviceResponse;
        this.allLeaveApplicationsList.forEach(leave => {
          leave.employeementId = "A-".concat(leave.employeementId);
        });
        console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getCustomLeaveApplicationsList(queryObjList:any) {
    this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveApplicationsList = response.serviceResponse;
        this.allLeaveApplicationsList.forEach(leave => {
          leave.employeementId = "A-".concat(leave.employeementId);
        });
        console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // Timesheet Report
  getAllTimesheetApplicationsList() {
    this.allTimesheetApplicationsList = [];

    this.timesheetService.timesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTimesheetApplicationsList = response.serviceResponse;
        this.allTimesheetApplicationsList.forEach(timesheet => {
          timesheet.employeementId = "A-".concat(timesheet.employeementId);
        });
        console.log("allTimesheetApplicationsList : ", this.allTimesheetApplicationsList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }


  // Employee Report 
  getAllEmployeeList() {
    this.allEmployeeList = [];

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.allEmployeeList.forEach(employee => {
          employee.employeementId = "A-".concat(employee.employeementId);
        });
        console.log("allEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns:any[], title:any) {
    console.log("columns : ", columns);
    
    this.filterData.title  = title;
    this.filterData.columns = columns;
    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  onFilterSubmit(queryList:any){
    console.log("queryList : ", queryList);
    this.queryList = queryList;
    this.cancelRequest();

    this.getCustomLeaveApplicationsList(queryList)
  }


  //pagination 	
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // Excel Export 
  exportToExcel(): void {

    if(this.isLeaveReportTable == true){
      this.excelName = 'leaveReport.xlsx';

        const onlySpecificDataArr = this.allLeaveApplicationsList.map(
          x => ({
            "Employeement Id": x.employeementId,
            "Employee Name":x.employeeName,
            "Leave Type":x.leaveType,
            "From Date":x.fromDate,
            "To Date":x.toDate,
            "No Of Days":x.noOfDays,
            "Reason":x.reason,
            "Status":x.status,
            "Manager Name":x.managerName,
            "Created On":x.createdOn,
            "Updated On":x.updatedOn,
            "Leave Status Updated By Name":x.leaveStatusUpdatedByName
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
    }

    if(this.isTimesheetReportTable == true){
      this.excelName = 'timesheetReport.xlsx';

        const onlySpecificDataArr = this.allTimesheetApplicationsList.map(
          x => ({
            "Employeement Id": x.employeementId,
            "Employee Name":x.employeeName,
            "date":x.date,
            "dayType":x.dayType,
            "description":x.description,
            "status":x.status,
            "totalWorkingHours":x.totalWorkingHours,
            "createdOn":x.createdOn,
            "updatedOn":x.updatedOn,
            "timesheetStatusUpdatedByName":x.timesheetStatusUpdatedByName
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
    }

    if(this.isEmployeeReportTable == true){
      this.excelName = 'EmployeeReport.xlsx';

        const onlySpecificDataArr = this.allEmployeeList.map(
          x => ({
            "Emp Id": x.employeementId,
            "Name": x.name,		
            "Email": x.email,		
            "Employment Status": x.employmentstatus,
            "Date Of Joining": x.dateOfJoining,
            "Aadhar":x.aadhar,
            "About Me":x.aboutMe,
            "address":x.address,
            "permanentAddress":x.permanentAddress,
            "city":x.city,
            "Blood Group":x.bloodGroup,
            "date Of Birth":x.dateOfBirth,
            "gender":x.gender,
            "fatherName":x.fatherName,
            "mobileNo":x.mobileNo,
            "panNumber":x.panNumber,
            "placeOfBirth":x.placeOfBirth,
            "workLocation":x.workLocation,
            "probationPeriod":x.probationPeriod,
            "noticePeriod":x.noticePeriod,
            "country":x.country,
            "emergencyContactMobile":x.emergencyContactMobile,
            "emergencyContactPerson":x.emergencyContactPerson,
            "landline":x.landline,
            "maritalStatus":x.maritalStatus,
            "motherTongue":x.motherTongue,
            "alternateMobileNo":x.alternateMobileNo,
            "pincode":x.pincode,
            "relation":x.relation,
            "state":x.state,
            "viewsOnOrganisation":x.viewsOnOrganisation,
            "passportNumber":x.passportNumber,
            "bankAccountNo":x.bankAccountNo,
            "bankIFSCCode":x.bankIFSCCode,
            "bankName":x.bankName,
            "pfAccountNumber":x.pfAccountNumber,
            "previousPfAccountNumber":x.previousPfAccountNumber,
            "uan":x.uan,
            "esicNumber":x.esicNumber,
            "graduationType":x.graduationType,
            "pursuing":x.pursuing,
            "passingGrade":x.passingGrade,
            "yearOfPassing":x.yearOfPassing,
            "createdBy":x.createdBy,
            "createdOn":x.createdOn 
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}

function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
