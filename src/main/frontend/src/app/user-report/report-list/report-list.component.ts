import { Component, OnInit, TemplateRef, ViewChild, Renderer2, ElementRef } from '@angular/core';
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
import { CdkDragDrop, moveItemInArray, CdkDragStart, CdkDragRelease } from "@angular/cdk/drag-drop";
import { JobRole } from 'src/app/models/jobRole';
import { LocationStrategy } from '@angular/common';
import { AppComponent } from 'src/app/app.component';
import * as moment from 'moment';

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

  allEmployeeList:any[] = [];

  allLeaveApplicationsList:any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList:any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

  allJobRoleList:any[] = [];
  personaWiseJobRole:any[] = [];
  accessControlList:any[] = [];
  mappedSubFeatureList:any[] = [];
  subfeatureList:any[] = [];

  updatedRoleSubFeature:any[] = [];
  hiddenColumnObj:any[] = [];
  showColumnList:any[] = [];

  insideCols:any[] = [];

  storedDataList:any[] = [];

  excelName:any;
  jobRoleName:any;
  departmentId:any;
  employeeRole:any;

  leaveColumns:any[] = ['Employee Id', 'Full Name', 'Leave Type','Team Name','Project Name','Client Name', 'Department', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  employeeColumns:any[] = ['Employee Id', 'Full Name', 'Department', 'Job Role', 'Manager','Team Name','Project Name','Client Name', 'Employment Status', 'Date Of Joining', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On'];
  timesheetColumns:any[] = ['Employee Id','Full Name','Date','Day Type','Status','Total Working Hour','Team Name','Project Name','Client Name','From Date','To Date','Created On','Updated On','Updated By'];
  queryList:any[] = [];
  filterData:any = new FilterData();

  columns: any[] = [];
  paginateData: any[] = [];
  pos:any;
  release:boolean = true;
  finalColumns: any[] = [];

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private leaveService : LeaveService,
    private jobRoleService : JobRoleService,
    private validationService : ValidationService,
    private renderer2: Renderer2,
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
    preventBackButton(){	
      history.pushState(null, null, location.href);	
      this.locationStrategy.onPopState(()=>{	
        history.pushState(null, null, location.href);	
      })	
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

    console.log(this.storedDataList, " : storeddatalist");
    

    this.storedDataList.forEach((object) => {
      if(object.filterName == 'Filter Leave Report'){
        console.log("hii");
        
        this.getCustomLeaveApplicationsList(object.queryList,this.alertModal);
      }
    });

    if(this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Leave Report')){
      this.getAllLeaveApplicationsList();
    }

    this.data = ''
  }

  showTimesheetReportTable(){
    this.isTimesheetReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;

    this.storedDataList.forEach((object) => {
      if(object.filterName == 'Filter Timesheet Report'){
        this.getCustomTimesheetApplicationsList(object.queryList,this.alertModal);
      }
    });

    if(this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Timesheet Report')){
      this.getAllTimesheetApplicationsList();
    }

    this.data = ''
  }

  showEmployeeReportTable(){
    this.isEmployeeReportTable = true;
    
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.isAccessControlListTable = false;

    this.storedDataList.forEach((object) => {
      if(object.filterName == 'Filter Employee Report'){
        this.getCustomEmployeesList(object.queryList,this.alertModal);
      }
    });

    if(this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Employee Report')){
      this.getAllEmployeeList();
    }

    this.data =''
  }

  showAccessControlListTable(){
    this.isAccessControlListTable = true;

    this.isEmployeeReportTable = false;
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.data ='';
    this.columns = [];
    this.paginateData = [];
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
          leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          leave.updatedOn = (leave.updatedOn)? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
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
    if(queryObjList.length == 0){
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
            leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            leave.updatedOn = (leave.updatedOn)? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
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
          timesheet.description = timesheet.description.replaceAll('<br>','')
          timesheet.date = (timesheet.date)? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime)? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime)? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn)? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.updatedOn = (timesheet.updatedOn)? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
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
          this.allTimesheetApplicationsList.forEach(timesheet => {
            timesheet.employeementId = "A-".concat(timesheet.employeementId);
            timesheet.description = timesheet.description.replaceAll('<br>','')
            timesheet.date = (timesheet.date)? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
            timesheet.officeInTime = (timesheet.officeInTime)? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.officeOutTime = (timesheet.officeOutTime)? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.createdOn = (timesheet.createdOn)? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.updatedOn = (timesheet.updatedOn)? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
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
          employee.profileCompletedPercent = employee.profileCompletedPercent + "%";
          employee.dateOfBirth = (employee.dateOfBirth)? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
          employee.dateOfJoining = (employee.dateOfJoining)? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employee.createdOn = (employee.createdOn)? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
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
            employee.dateOfBirth = (employee.dateOfBirth)? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining)? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn)? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });
    }
  }

// ACL Start

  selectPersona(event) {
    this.employeeRole = event.target.value;
    this.showColumnList = [];
    this.getAllJobRoleList(this.employeeRole);
  }

  getAllJobRoleList(persona: any) {
    this.personaWiseJobRole = [];
    this.columns = [];
    this.mappedSubFeatureList = [];
    this.subfeatureList = [];
    this.paginateData = [];
    this.finalColumns = [];


    this.jobRoleService.getAllSubFeatureList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.subfeatureList = response.serviceResponse;
        this.columns.push({ "field": "subFeature", "header": "Sub-Feature" });

        this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.allJobRoleList = response.serviceResponse;
            this.personaWiseJobRole = this.allJobRoleList.filter((x) => x.employeeRole == persona);
            this.personaWiseJobRole.forEach(role => { this.columns.push({ "field": role.jobRoleId, "header": role.name, "department":role.departmentName })})

            // segregating jobroles by department
            var final = [];
            this.columns.forEach(function (e) {
              var match = false;
              final.forEach(function (i) {
                if (e.department == i.department[0].department) {
                  match = true;
                }
              });
              if (!match) {
                var obj = {
                  "header": e.department,
                  "department" : [e]
                }
                final.push(obj);
              } else {
                final.forEach(function (i) {
                  if (e.department == i.department[0].department) {
                    i.department.push(e);
                  }
                });
              }
            });

            this.finalColumns = final;
            console.log(this.finalColumns, " : funal arr");

            // Generate Template
            this.subfeatureList.forEach(subfeature => {
              let paginateDataItem = {}

              this.columns.forEach((column, index) => {
                if (index === 0) {
                  paginateDataItem[column.field] = subfeature.subFeatureName;
                  paginateDataItem['subfeatureId'] = subfeature.subFeatureId;
                } else {
                  paginateDataItem[column.field] = false;
                }
              });
              this.paginateData.push(paginateDataItem)
            });
            console.log("paginateData : ", this.paginateData);

            this.personaWiseJobRole.forEach((role) => {
              this.employeeObj.jobRoleId = role.jobRoleId;
              this.jobRoleService.getMappedSubFeatureList(this.employeeObj).pipe(first()).subscribe((response: any) => {
                if (response.serviceStatus == "Success") {
                  const mappedSubFeatures = response.serviceResponse;
                  console.log("mappedSubFeatures : ", mappedSubFeatures);
                  mappedSubFeatures.forEach(subFeature => {
                    let mappedSubFeatureData = this.paginateData.find(data => {
                      const subFeatureName = data.subFeature;
                      if (subFeatureName == subFeature.subFeatureName)
                        return data;
                    });
                    if (mappedSubFeatureData)
                      mappedSubFeatureData[role.jobRoleId] = true;
                  });
                } else {
                  console.error(response.serviceResponse);
                }
              });
            });
          } else {
            console.error(response.serviceResponse)
          }
        });
      }
    });
  }

dropRow(event: CdkDragDrop<string[]>) {
  moveItemInArray(this.paginateData, event.previousIndex, event.currentIndex);
}

dropCol(event: CdkDragDrop<string[]>) {
  if(event.previousIndex != 0 && event.currentIndex !== 0){
    moveItemInArray(this.finalColumns, event.previousIndex, event.currentIndex);
  }
}

dropInsideCol(event: CdkDragDrop<string[]>) {
  if(event.previousIndex != 0 && event.currentIndex !== 0){

    this.insideCols = [];
    this.finalColumns.forEach((x) => {
      this.insideCols.push(...x.department);
    });

    moveItemInArray(this.insideCols, event.previousIndex, event.currentIndex);
  }
}

mouseDown(event,el:any=null){
  el=el || event.target
  this.pos={x:el.getBoundingClientRect().left-event.clientX+'px',
  y:el.getBoundingClientRect().top-event.clientY+'px',
  width:el.getBoundingClientRect().width+'px'
  }
}

onDragRelease(event: CdkDragRelease) {
  this.renderer2.setStyle(event.source.element.nativeElement,'margin-left','0px')
}

selectCellCheckbox(element:any, isAssigned:any, subFeatureId:any){
  const alreadyUpdatedMapping = this.updatedRoleSubFeature.findIndex((x) => x.subFeatureId == subFeatureId && x.jobRoleId == element);
  if(alreadyUpdatedMapping >= 0){
    this.updatedRoleSubFeature.splice(alreadyUpdatedMapping,1);
  }else{
    this.updatedRoleSubFeature.push({
      "jobRoleId":element,
      "isAssigned":isAssigned,
      "subFeatureId":subFeatureId
    });
  }
  console.log(this.updatedRoleSubFeature, " :   updatedRoleSubFeature");
}

updateJobRoleSubFeatureMapping(template: TemplateRef<any>){
  let jobRoleObj = new JobRole();
  jobRoleObj.updatedJobRoleFeatureMapping = this.updatedRoleSubFeature;

  this.jobRoleService.updateJobRoleSubFeatureMapping(jobRoleObj).pipe(first()).subscribe((response:any) => {
    if(response.serviceStatus == "Success"){
      this.openAlertMod(template,response.serviceResponse);
    }else{
      this.openAlertMod(template,response.serviceResponse);
    }
  });
}

hideColumn(column:any){
  this.hiddenColumnObj = this.columns.find(x => x.header == column);
  this.columns = this.columns.filter(x => x.header != column);
  this.showColumnList.push(this.hiddenColumnObj);
}

showColumn(){
  let headerId = this.employeeObj.columnHeader;
  let hiddenFound = this.showColumnList.find(x => x.field == headerId);
  if(hiddenFound){
    this.columns.push(hiddenFound);
    this.showColumnList.splice(hiddenFound,1);
    this.employeeObj.columnHeader = '';
  }
}

// ACL end

  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns:any[], title:any) {
    console.log("columns : ", columns);
    this.queryList = [];
    
    this.filterData.title  = title;
    this.filterData.columns = columns;

    this.storedDataList.forEach((data) => {
      if(data.filterName == title){
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  onFilterSubmit(emittedArray:any , template:TemplateRef<any>){
    console.log("queryList : ", emittedArray[0]);
    this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
    this.cancelRequest();

    emittedArray[1].forEach((object) => {
      if(Object.keys(object).length !== 0){
        if(this.storedDataList.find((x) => x.filterName == object.filterName)){
          this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
        }else{
          this.storedDataList.push(object);
        }
      }
    });
    

    emittedArray[0].forEach(query => {
      if(query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining'){
          query.value = (query.value)? moment(new Date(query.value)).format('YYYY-MM-DD') : '';
      }else if(query.column == 'Created On' || query.column == 'Updated On'){
        query.value = (query.value)? moment(new Date(query.value)).format('YYYY-MM-DD HH:mm:ss') : '';
      }
    });
    
    if(this.filterData.title == 'Filter Leave Report'){
      this.getCustomLeaveApplicationsList(emittedArray[0],template);
    }
    if(this.filterData.title == 'Filter Employee Report'){
      this.getCustomEmployeesList(emittedArray[0],template);
    }
    if(this.filterData.title == 'Filter Timesheet Report'){
      this.getCustomTimesheetApplicationsList(emittedArray[0],template);
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
            "description":x.description?.replaceAll('<br>', ' \n'),
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
            "Manager Name":x.managerName,
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
            "Proile Completion Perecentage":x.profileCompletedPercent,
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
