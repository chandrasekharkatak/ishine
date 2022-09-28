import { Component, OnInit } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { LeaveService } from 'src/app/services/leave.service';

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {

  feature = 'Reports';
  currentUser: User;

  data:string; //Search Data 

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isLeaveReportTable:boolean = false;
  isTimesheetReportTable:boolean = false;
  isEmployeeReportTable:boolean = false;

  allEmployeeList:any[] = [];
  employeeDataForExcel: any[] = [];

  allLeaveApplicationsList:any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList:any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

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
    this.sectionViewInit();
  }

  sectionViewInit() {
    this.showLeaveReportTable();
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


  //pagination 	
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // sorting

  sortData(sort: Sort) {
    console.log(sort);
    let data = this.allEmployeeList;
    if (!sort.active || sort.direction === '') {
      this.allEmployeeList = data;
      return;
    }
    else {
      this.allEmployeeList = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            case 'employeementId':
              return compare(a.employeementId, b.employeementId, isAsc);

            case 'name':
              return compare(a.name.toLowerCase(), b.name.toLowerCase(), isAsc);

            case 'email':
              return compare(a.email, b.email, isAsc);
            case 'dateOfJoining':

              return compare(new Date(a.dateOfJoining).getTime(), new Date(b.dateOfJoining).getTime(), isAsc);
            default:
              return 0;
          }
        }
      )
    }
  }

  // Excel Export 
  name = 'EmployeeSheet.xlsx';
  exportToExcel(): void {
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeDataForExcel = response.serviceResponse;
      }
      const onlySpecificDataArr = this.employeeDataForExcel.map(
        x => ({
          "Emp Id": "A-".concat(x.employeementId),
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
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }
}

function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
