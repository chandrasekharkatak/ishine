import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { JobRoleService } from 'src/app/services/job-role.service';
import { ValidationService } from 'src/app/services/validation.service';
import { Employee } from 'src/app/models/employee';

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

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  feature = 'Reports';
  currentUser: User;
  userMapping: any = {};

  data:string; //Search Data
  designationData:string;   //Search Designation

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  employeeObj:Employee = new Employee();

  isLeaveReportTable:boolean = false;
  isTimesheetReportTable:boolean = false;
  isEmployeeReportTable:boolean = false;
  isAccessControlListTable:boolean = false;
  isAccessControlListByPersona:boolean = false;

  allEmployeeList:any[] = [];

  allLeaveApplicationsList:any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList:any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

  allJobRoleList:any[] = [];
  accessControlList:any[] = [];

  excelName:any;
  jobRoleName:any;
  departmentId:any;
  employeeRole:any;

  leaveColumns:any[] = ['Employee Id', 'Full Name', 'Leave Type','Team Name','Project Name','Client Name', 'Department', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  employeeColumns:any[] = ['Employee Id', 'Full Name', 'Department', 'Job Role', 'Manager','Team Name','Project Name','Client Name', 'Employment Status', 'Date Of Joining', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On'];
  timesheetColumns:any[] = ['Employee Id','Full Name','Date','Day Type','Status','Total Working Hour','Team Name','Project Name','Client Name','From Date','To Date','Created On','Updated On','Updated By'];
  queryList:any[] = [];
  filterData:any = new FilterData(); 

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private leaveService : LeaveService,
    private jobRoleService : JobRoleService,
    private validationService : ValidationService,
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

     this.getAllJobRoleList();

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
    this.isAccessControlListTable = false;

    this.getAllLeaveApplicationsList();
    this.data = ''
  }

  showTimesheetReportTable(){
    this.isTimesheetReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;

    this.getAllTimesheetApplicationsList();
    this.data = ''
  }

  showEmployeeReportTable(){
    this.isEmployeeReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.isAccessControlListTable = false;

    this.getAllEmployeeList();
    this.data =''
  }

  showAccessControlListTable(){
    this.isAccessControlListTable = true;

    this.isEmployeeReportTable = false;
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;

    this.getAllEmployeeList();
    this.data =''
  }

  // Leave Report 
  getAllLeaveApplicationsList() {
    this.queryList=[];
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

  getCustomLeaveApplicationsList(queryObjList:any , template:TemplateRef<any>) {
    this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.getAllLeaveApplicationsList();
    }else {
      this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allLeaveApplicationsList = response.serviceResponse;

          this.allLeaveApplicationsList = this.allLeaveApplicationsList.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId && t.fromDate === value.fromDate
            ))
          )

          if(this.allLeaveApplicationsList.length != 0){
            this.openAlertMod(template, "Leave Application Report found ")
          }else {
            this.openAlertMod(template, "No Leave Application Report found ")
          }
          this.allLeaveApplicationsList.forEach(leave => {
            leave.employeementId = "A-".concat(leave.employeementId);
          });
          console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });  
    }
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

  getCustomTimesheetApplicationsList(queryObjList:any , template:TemplateRef<any>) {
    this.allTimesheetApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.getAllTimesheetApplicationsList();
    }else {
      this.timesheetService.customTimesheetApplicationReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allTimesheetApplicationsList = response.serviceResponse;

          this.allTimesheetApplicationsList = this.allTimesheetApplicationsList.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId && t.date === value.date
            ))
          )

          if(this.allTimesheetApplicationsList.length != 0){
            this.openAlertMod(template, "Timesheet Application Report found ");
          }else {
            this.openAlertMod(template, "No Timesheet Application Report found ");
          }
          this.allTimesheetApplicationsList.forEach(leave => {
            leave.employeementId = "A-".concat(leave.employeementId);
          });
          console.log("allTimesheetApplicationsList : ", this.allTimesheetApplicationsList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });  
    }
  }

  // Employee Report 
  getAllEmployeeList() {
    this.queryList=[];
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

  getCustomEmployeesList(queryObjList:any , template : TemplateRef<any>) {
    this.allEmployeeList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.getAllEmployeeList();
    }else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;

          this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.employeementId === value.employeementId
          ))
        )

          if(this.allEmployeeList.length != 0){
            this.openAlertMod(template, "Employee Report found")
          }else{
            this.openAlertMod(template, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
          });
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });
    }
  }

  getAllJobRoleList() {
    this.allJobRoleList = [];

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allJobRoleList = response.serviceResponse;
        console.log("allJobRoleList : ", this.allJobRoleList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  selectedJobRole(event){
    let value = event.target.value;
    let string = value.split(/(\d+)/);

    this.jobRoleName = string[0];
    this.departmentId = string[1];

    this.getAccessControlListData(this.alertModal);
  }

  selectPersona(event) {
    this.employeeRole = event.target.value;
    if(this.jobRoleName != null){
      this.getAccessControlListData(this.alertModal);
    }else{
      this.employeeObj.employeeRole = this.employeeRole;
      this.jobRoleService.getAccessControlListByPersona(this.employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.isAccessControlListByPersona = true;
          console.log(this.isAccessControlListByPersona);
          
          this.accessControlList = response.serviceResponse;
          console.log("accessControlList : ", this.accessControlList);
        } else {
          console.error(response.serviceResponse)
        }
      });
    }
  }

  getAccessControlListData(template: TemplateRef<any>) {
    if(!this.validationService.validateNullUndefinedEmptyString(this.jobRoleName)){
      this.alertMessage = "Please select Designation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(!this.validationService.validateNullUndefinedEmptyString(this.employeeRole)){
      this.alertMessage = "Please select Persona (Employee Role) !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.employeeObj.jobRoleName = this.jobRoleName;
    this.employeeObj.employeeRole = this.employeeRole;
    this.employeeObj.departmentId = this.departmentId;
    
    this.jobRoleService.getAccessControlListData(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isAccessControlListByPersona = false;
        this.accessControlList = response.serviceResponse;
        console.log("accessControlList : ", this.accessControlList);
      } else {
        console.error(response.serviceResponse)
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

  onFilterSubmit(queryList:any , template:TemplateRef<any>){
    console.log("queryList : ", queryList);
    this.queryList = queryList;
    this.cancelRequest();
    
    if(this.filterData.title == 'Filter Leave Report'){
      this.getCustomLeaveApplicationsList(queryList,template);
    }
    if(this.filterData.title == 'Filter Employee Report'){
      this.getCustomEmployeesList(queryList,template);
    }
    if(this.filterData.title == 'Filter Timesheet Report'){
      this.getCustomTimesheetApplicationsList(queryList,template);
    }
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
            "Employee Id": x.employeementId,
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
            "Updated By":x.leaveStatusUpdatedByName
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
            "Employee Id": x.employeementId,
            "Full Name": x.name,		
            "Email Id": x.email,		
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
            "Probation Period":x.probationPeriod,
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
            "State":x.state,
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

    if(this.isAccessControlListTable == true){
      this.excelName = 'ACLReport.xlsx';

        const onlySpecificDataArr = this.accessControlList.map(
          x => ({
            "Department Name": x.departmentName,
            "Designation": x.jobRoleName,
            "Employee Role": x.employeeRole,
            "Tab Name": x.tabName,
            "Feature Name": x.featureName,
            "Sub-Feature Name": x.subFeatureName,
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
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
