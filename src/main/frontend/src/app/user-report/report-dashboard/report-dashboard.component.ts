import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { first, groupBy } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ReportService } from 'src/app/services/report-service.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Query } from 'src/app/models/query';
import * as moment from 'moment';
import { Leave } from 'src/app/models/leave';
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';
import { AppComponent } from 'src/app/app.component';
import { DepartmentService } from 'src/app/services/department.service';
import { DomainService } from 'src/app/services/domain.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';

//importing deleclation for cylinder chart..
declare var require: any;
const More = require('highcharts/highcharts-more');
More(Highcharts);

import Histogram from 'highcharts/modules/histogram-bellcurve';
Histogram(Highcharts);

import highcharts3D from 'highcharts/highcharts-3d';
highcharts3D(Highcharts);

import Cylinder from 'highcharts/modules/cylinder';
Cylinder(Highcharts);

const Exporting = require('highcharts/modules/exporting');
Exporting(Highcharts);

const ExportData = require('highcharts/modules/export-data');
ExportData(Highcharts);

const Accessibility = require('highcharts/modules/accessibility');
Accessibility(Highcharts);
import * as Highcharts from 'highcharts';
import { UtilityService } from 'src/app/services/utility.service';
import { Feature } from 'src/app/models/feature';
import { Employee360Service } from 'src/app/services/employee360.service';
import { SortPipe } from 'src/app/sort.pipe';

HC_exportData(HighCharts);

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

interface Project {
  projectId: string;
  projectName: string;
}

interface DepartmentKycCountDTO {
  departmentName: string;
  status: string;
  empCount: number;
}

@Component({
  selector: 'app-report-dashboard',
  templateUrl: './report-dashboard.component.html',
  styleUrls: ['./report-dashboard.component.css']
})
export class ReportDashboardComponent implements OnInit {

  @ViewChild("leave_summary_template")
  leaveSummaryTemplate: TemplateRef<any>;

  @ViewChild("timesheet_summary_template")
  timesheetSummaryTemplate: TemplateRef<any>;

  @ViewChild("employee_summary_template")
  employeeSummaryTemplate: TemplateRef<any>;

  @ViewChild("workLocation_summary_template")
  workLocationSummaryTemplate: TemplateRef<any>;

  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  isfileUpload: boolean = false;
  file: any;

  modalRef: BsModalRef = new BsModalRef();

  isleaveTimesheetDashboard: boolean = false;
  isEmployeeDashboard: boolean = false;
  isPendingByUser: boolean = false;
  isEmployeeResigned: boolean = false;
  departmentWiseEmployeeCount: any[] = [];
  data: any;
  leaveSumarryList: any[] = [];
  uniqueLeaveSumarryList: any[] = [];
  timsheetSummaryList: any[] = [];
  allEmployeeList: any[] = [];
  departmentWiseBillableEmployeeList: any[] = [];
  leaveTrendAnalysisList: any[] = [];
  allLeaveTypes: any[] = [];
  employeeWorkLocationList: any[] = [];

  departmentWiseEmployeeCategories: string[] = [];
  departmentWiseEmployeeData: { name: string, data: number[] }[] = [];


  billableChartCategories:any[] = [];

  modalTitle: any;
  modalSummaryList: any[] = [];
  countByLegend: any[] = [];
  storedDataList: any[] = [];

  zeroToFive: any;
  fiveToEight: any;
  eightToNine: any;
  nineToTen: any;
  tenAndAbove: any;
  noEODSubmitted: any;
  holiday: any;
  workingOnHoliday: any;

  dateFormat: any = 'YYYY-MM-DD';

  alertMessage: any;

  countOfAllEmployees: any;
  employeeInProbationAfter6MonthsCount = 0;
  apprenticeCountForDisplay = 0;
  consultantCountForDisplay = 0;
  regularCountForDisplay = 0;
  allResignEmployee: any;

  filterData: any = new FilterData();
  queryList: any[] = [];

  filters: any = {};
  isSearchEnabled: boolean = false;

  //Gender summary (pie)
  maleCount = 0;
  femaleCount = 0;
  otherCount = 0;
//Age summary(pie)
  countBetween18and25 = 0;
  countBetween25and35 = 0;
 countBetween35and45 = 0;
 countAbove45 = 0;

 //Employee status summary (pie)
  probationCount = 0;
  confirmedCount = 0;
  resignedCount = 0;
  inActiveCount = 0;
//Fresher-lateral summary (pie)
   fresherCount = 0;
   experienceCount = 0;
//Department wise Billable/Non-biallable employee summary (pie)
  internalBillableCount = 0;
  tnmBillableCount = 0;
  fcBillableCount = 0;
  shadowBillableCount = 0;
  benchBillableCount = 0;
//Billable Employee summary (pie)
  billableCount = 0;
  nonBillableCount = 0;
  otherBillableCount = 0;
//Employee Experience 
//Employee
  experienceCountBetween0and1 = 0;
  experienceCountBetween1and2 = 0;
  experienceCountBetween2and5 = 0;
  experienceCountBetween5and10 = 0;
  experienceCountAbove10 = 0;
//Apprentice
  experienceCountBetween0and1Apprentice = 0;
  experienceCountBetween1and2Apprentice = 0;
  experienceCountBetween2and5Apprentice = 0;
  experienceCountBetween5and10Apprentice = 0;
  experienceCountAbove10Apprentice = 0;
  //Consultant
  experienceCountBetween0and1Consultant = 0;
  experienceCountBetween1and2Consultant = 0;
  experienceCountBetween2and5Consultant = 0;
  experienceCountBetween5and10Consultant = 0;
  experienceCountAbove10Consultant = 0;
  

  leaveSummaryColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Leave Type', 'Department', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  timesheetSummaryColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By'];

  employeeColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Department', 'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name', 'Employment Status', 'Date Of Joining', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On', 'Experience'];
  employeeSummaryColumns: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'experience', 'departmentName', 'email', 'managerName', 'billable', 'billableType', 'projectName', 'clientName', 'dateOfJoining', 'mobileNo', 'employmentstatus', 'totalExperience', 'gender', 'workLocation', 'age', 'profileKycStatus'];
  workLocationSummaryColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'projectName', 'clientName', 'teamName', 'clientLocation', 'date'];
  LeaveTrendAnalysisGraphColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'departmentName', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'status'];
  leaveSummaryTableColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'departmentName', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'status'];
  timesheetSummaryTableColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'departmentName', 'email', 'managerName', 'mobileNo', 'pendingEodCount', 'legend'];
  eodSegregationTableColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'departmentName', 'email', 'managerName', 'mobileNo', 'date', 'dayType', 'totalWorkingHours'];

  billableChartByDepartmentColumns: any[] = ['Department'];

  resignedColumns: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'departmentName', 'dateOfResign', 'dateOfRelieving', 'managerName'];
  departmentIds: any[] = [];
  allDepartmentList: any[] = [];
  currentUser: User;
  show: number = -1;

  //property for cylinder charts
  public activity;
  public xData;
  public label;
  options: any;
  //end.........

  userMapping: any = {};
  feature = 'Reports';

  departmentKycData: any[] = [];
  kycChartData: any[] = [
    {
      name: 'Pending',
      data: []
    },
    {
      name: 'Completed',
      data: []
    }
  ];

  departmentCategories: string[] = [];
  selectedYear: number;

  constructor(
    private reportService: ReportService,
    private leaveService: LeaveService,
    private timesheetService: TimesheetService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private departmentService: DepartmentService,
    private domainService: DomainService,
    private authenticationService: AuthenticationService,
    public utilityService: UtilityService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.sectionViewInit();
    this.preventBackButton();
    this.loadDepartmentWiseKycData();
    this.loadJoinResignData();
    this.getAllPieChartCount();
    this.getAllEmployeeCountDepartmentWise();
    this.getAllBillableTypeCount();
    this.getEmployeeBillableSummary();
  }

  getSlicedProjects(projectList: Project[], count: number): Project[] {
    return projectList.slice(0, count);
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    this.employeeDashboard();
    this.getAllLeaveTypes();
  }

  leaveTimesheetDashboard() {
    this.isleaveTimesheetDashboard = true;
    this.isEmployeeDashboard = false;
    this.isEmployeeResigned = false;


    this.get8DaysLeaveReport();
    this.get9DayTimesheetReport();
  }

  employeeDashboard() {
    this.isEmployeeDashboard = true;
    this.isleaveTimesheetDashboard = false;
    this.isEmployeeResigned = false;

    // this.getAllEmployeeList();
    // this.getLeaveTrendAnalysisReport();
    // this.getAllBillableEmployeeData();
    // this.getEmployeeWorkLocation();
    this.findAllDepartment();
  }

  showFileUploadForm() {

    this.isfileUpload = true;
    this.isleaveTimesheetDashboard = false;


    this.isEmployeeDashboard = false;
    this.isEmployeeResigned = false;
    this.isEmployeeDashboard = false;


  }

  onBillableFileSelect(event: any, template: TemplateRef<any>) {
    const uploadedFiles = event.target.files;
    console.log("uploadedFiles ", uploadedFiles);
    this.file = uploadedFiles[0];
    const formData = new FormData();
    formData.append('file', this.file);

    this.domainService.billableFile(formData).pipe(first()).subscribe(
      (response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
  }

  employeeResigned() {
    this.isEmployeeDashboard = false;
    this.isleaveTimesheetDashboard = false;
    this.isEmployeeResigned = true;
    this.getAllResignedEmployees();
  }

  getAllResignedEmployees() {

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allResignEmployee = response.serviceResponse;
        this.allResignEmployee = this.allResignEmployee.filter(x => x.employmentstatus == 'Resigned');
        this.allResignEmployee.forEach(employee => {
          employee.employeementId = "A-".concat(employee.employeementId);
          // if(employee.isConsultant == 'true'){
          //   employee.employeementId = "A-CS-".concat(employee.employeementId);
          // }else{
          //   employee.employeementId = "A-".concat(employee.employeementId);
          // }
          if (employee.isConsultant == 'true') {
            employee.employeeType = "Consultant"
          } else if (employee.isApprenticeship == 'true') {
            employee.employeeType = "Apprentice"
          } else {
            employee.employeeType = "Regular"
          }
          employee.dateOfRelieving = (employee.dateOfResign) ? moment(employee.dateOfResign).add(employee.noticePeriod, 'days') : null;

          employee.dateOfResign = (employee.dateOfResign) ? moment(employee.dateOfResign).format(AppComponent.DATE_FORMAT) : null;
          employee.dateOfRelieving = (employee.dateOfRelieving) ? moment(employee.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employee.emp360 = employee.empId;

          // console.log("leave match ",matchingEmployee);
          employee.emp360Manager = employee.managerId;
        });

        // console.log("allResignEmployee : ", this.allResignEmployee)

      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  // dummy 

  // getAllBillableEmployeeData() {
  //   // getDepartmentWiseBillableData
  //   // console.log("Anurag check second mgetDepartmentWiseBillableData ");

  //   this.departmentWiseBillableEmployeeList = [];
  //   this.queryList = [];
  //   let leaveObj = new Leave();

  //   this.leaveService.getDepartmentWiseBillableData(leaveObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       console.log(response.serviceStatus);
  //       this.departmentWiseBillableEmployeeList = response.serviceResponse;
  //       console.log('API Data:', this.departmentWiseBillableEmployeeList);
  //       this.departmentWiseBillableEmployeeList.forEach((data) => {
  //         data.employeementId = "A-".concat(data.employeementId);
  //         // if(data.isConsultant == 'true'){
  //         //   data.employeementId = "A-CS-".concat(data.employeementId);
  //         // }else{
  //         //   data.employeementId = "A-".concat(data.employeementId);
  //         // }
  //         if (data.isConsultant == 'true') {
  //           data.employeeType = "Consultant"
  //         } else if (data.isApprenticeship == 'true') {
  //           data.employeeType = "Apprentice"
  //         } else {
  //           data.employeeType = "Regular"
  //         }
  //         let age = this.getAge(data.dateOfBirth);
  //         data.age = age;
  //         data.emp360 = data.empId;
  //         data.emp360Manager = data.managerId;
  //       })
  //       console.log("this.departmentWiseBillableEmployeeList ", this.departmentWiseBillableEmployeeList);
  //     }
  //     this.extractDataForBillable()
  //   })

  // }


  get8DaysLeaveReport() {
    this.leaveSumarryList = [];
    this.queryList = [];
    let leaveObj = new Leave();
    //console.log(leaveObj);
    leaveObj.startDate = moment().subtract(8, 'd').format(this.dateFormat);
    leaveObj.endDate = moment().format(this.dateFormat);

    //console.log(leaveObj.startDate, " leaveObj.startDate   ", leaveObj.endDate, "    leaveObj.endDate");

    this.leaveService.getLast8DaysLeaveReport(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveSumarryList = response.serviceResponse;

        this.leaveSumarryList.forEach((leave) => {
          if (leave.fromDateDayType != null) {
            leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
          }
          if (leave.toDateDayType != null) {
            leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
          }
          // console.log("leave match ",matchingEmployee);
          // console.log("leave match ",matchingEmployee);
          leave.emp360Manager = leave.managerId;
        });

        //console.log("leaveSumarryList : ", this.leaveSumarryList);
        this.extractLeaveReportData();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  extractLeaveReportData() {
    let pendingCount = 0;
    let approvedCount = 0;
    let rejectedCount = 0;

    this.uniqueLeaveSumarryList = this.leaveSumarryList.filter((value, index, self) =>
      index === self.findIndex((t) => (
        t.employeementId === value.employeementId && t.fromDate === value.fromDate
      ))
    )
    //console.log(this.uniqueLeaveSumarryList, " uniqueIds ");

    this.uniqueLeaveSumarryList.forEach(leaveStatus => {
      if (leaveStatus.status == "Pending") pendingCount++;
      else if (leaveStatus.status == "Approved") approvedCount++;
      else if (leaveStatus.status == "Rejected") rejectedCount++;
    });

    let leaveStatusData = [{
      name: "Pending",
      y: pendingCount
    },
    {
      name: "Approved",
      y: approvedCount
    },
    {
      name: "Rejected",
      y: rejectedCount
    }];

    //console.log("leaveStatusData : ", leaveStatusData);

    let checkLeaveStatusData = leaveStatusData.filter(data => data.y != 0);
    //console.log("checkLeaveStatusData :", checkLeaveStatusData);

    if (checkLeaveStatusData && checkLeaveStatusData.length != 0) {
      this.renderPieSummaryChart('Leave Summary Chart', 'leaveSummaryChart', leaveStatusData, 'Leaves', this.openLeaveSummaryTableModel.bind(this));
    } else {
      this.renderPlaceholderChart('Leave Summary Chart', 'leaveSummaryChart');
    }

  }

  getCustomLeaveReport(queryObjList: any, template: TemplateRef<any>) {
    this.leaveSumarryList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList == '') {
      this.get8DaysLeaveReport();
    } else {
      this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveSumarryList = response.serviceResponse;

          this.leaveSumarryList.forEach((leave) => {
            if (leave.fromDateDayType != null) {
              leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
            }
            if (leave.toDateDayType != null) {
              leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
            }
          });

          //console.log(this.leaveSumarryList, "  :  this.leaveSumarryList");
          this.extractLeaveReportData();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  }

  getDatesInRange(startDate, endDate) {
    const date = startDate;
    const dates = [];

    while (date <= endDate) {
      dates.push(new Date(date));
      date.setDate(date.getDate() + 1);
    }
    return dates;
  }

  getAllLeaveTypes() {
    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveTypes = response.serviceResponse;
        //console.log("leaveTypes : ", this.allLeaveTypes);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // getLeaveTrendAnalysisReport() {
  //   this.leaveSumarryList = [];
  //   let leaveObj = new Leave();

  //   leaveObj.startDate = moment().subtract(8, 'd').format(this.dateFormat);
  //   leaveObj.endDate = moment().format(this.dateFormat);

  //   this.leaveService.getLeaveTrendAnalysisReport(leaveObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.leaveTrendAnalysisList = response.serviceResponse;
  //       this.extractLeaveTrendAnalysisData(leaveObj);
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  // extractLeaveTrendAnalysisData(leaveObject: any) {
  //   this.leaveSumarryList = [];

  //   const d1 = new Date(leaveObject.startDate);
  //   const d2 = new Date(leaveObject.endDate);

  //   let dateRange = this.getDatesInRange(d1, d2);
  //   let formattedDateRange = [];

  //   dateRange.forEach((date) => {
  //     formattedDateRange.push(date.toISOString().split('T')[0]);
  //   })

  //   this.leaveTrendAnalysisList = this.leaveTrendAnalysisList.filter((value, index, self) =>
  //     index === self.findIndex((t) => (
  //       t.employeementId === value.employeementId && t.fromDate === value.fromDate
  //     ))
  //   )
  //   //console.log(this.leaveTrendAnalysisList, " uniqueTrendAnalysisList ");

  //   let chartData = [];
  //   let leaveTypes = [];

  //   let leaveObj = this.allLeaveTypes.forEach(obj => {
  //     leaveTypes.push(obj.leaveType);
  //   });

  //   let data = leaveTypes.map(leaveType => {
  //     chartData.push({ type: 'line', name: leaveType, data: [] });
  //     let leaveData = this.leaveTrendAnalysisList.filter(leaveTrendAnalysis => leaveTrendAnalysis.leaveType == leaveType)
  //     return leaveData;
  //   });

  //   //console.log("data :", data);
  //   //console.log("ChartData : ", chartData);
  //   //console.log("dateRange : ", dateRange);

  //   data.forEach(leaveDataArr => {
  //     dateRange.forEach(date => {
  //       let leaveType: any;
  //       let dataByDate = leaveDataArr.filter(leaveApplication => {
  //         leaveType = leaveApplication.leaveType;
  //         if (leaveApplication.fromDate == moment(date).format(this.dateFormat)) return leaveApplication;
  //       });
  //       chartData.find(chartDataObj => chartDataObj.name == leaveType)?.data.push(dataByDate.length)
  //     });
  //   });
  //   //console.log("Final ChartData : ", chartData);
  //   this.renderLineGraphChart('Leave Trend Analysis Graph', 'leaveTrendAnalysis', chartData, 'Leave Trend', formattedDateRange, this.openLeaveAnalysisTableModel.bind(this));
  // }

  // getCustomLeaveTrendAnalysisReport(queryObjList: any, template: TemplateRef<any>) {
  //   this.leaveTrendAnalysisList = [];

  //   let queryObj = new Query();
  //   queryObj.queryList = queryObjList;
  //   if (queryObjList == '') {
  //     this.getLeaveTrendAnalysisReport();
  //   } else {
  //     let tempFrom = "";
  //     let tempTo = "";
  //     queryObj.queryList.forEach((query) => {
  //       if (query.column == 'From Date') {
  //         tempFrom = query.value;
  //       }
  //       if (query.column == 'To Date') {
  //         tempTo = query.value;
  //       }
  //     });

  //     this.leaveService.customQueryForLeaveTrendAnalysisReport(queryObj).pipe(first()).subscribe((response: any) => {
  //       if (response.serviceStatus == "Success") {
  //         this.leaveTrendAnalysisList = response.serviceResponse;
  //         let leaveObj = new Leave();
  //         leaveObj.startDate = tempFrom;
  //         leaveObj.endDate = tempTo;

  //         if (leaveObj.startDate == "" && leaveObj.endDate == "") {
  //           this.openAlertMod(template, "Please Select Date Range.");
  //           this.getLeaveTrendAnalysisReport();
  //         }

  //         this.extractLeaveTrendAnalysisData(leaveObj);
  //       } else {
  //         this.openAlertMod(template, response.serviceResponse);
  //       }
  //     });
  //   }
  // }

  get9DayTimesheetReport() {
    this.timsheetSummaryList = [];

    this.timesheetService.getLast9DaysTimesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timsheetSummaryList = response.serviceResponse;
        // console.log("timsheetSummaryList : ", this.timsheetSummaryList);
        this.extractTimesheetReportData();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  extractTimesheetReportData() {

    let pendingByUserCount = 0;
    let pendingCount = 0;
    let approvedCount = 0;
    let rejectedCount = 0;

    let zeroToFiveCount = 0;
    let fiveToEightCount = 0;
    let eightToNineCount = 0;
    let nineToTenCount = 0;
    let tenAndAboveCount = 0;
    let noEODSubmittedCount = 0;
    let holidayCount = 0;
    let workingOnHolidayCount = 0;

    let totalListCount = 0;

    this.timsheetSummaryList = this.timsheetSummaryList.filter((value, index, self) =>
      index === self.findIndex((t) => (
        t.employeementId === value.employeementId && t.date === value.date && value.employmentstatus != 'InActive'
      ))
    )

    this.timsheetSummaryList.forEach(timesheet => {

      if (timesheet.legend == "Pending") pendingCount++;
      else if (timesheet.legend == "Approved") approvedCount++;
      else if (timesheet.legend == "Rejected") rejectedCount++;
      // else if (timesheet.legend == "Pending By User") pendingByUserCount++;

      if (timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 0 && timesheet.totalWorkingHours <= 5) zeroToFiveCount++;
      else if (timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 5 && timesheet.totalWorkingHours <= 8) fiveToEightCount++;
      else if (timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 8 && timesheet.totalWorkingHours <= 9) eightToNineCount++;
      else if (timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 9 && timesheet.totalWorkingHours <= 10) nineToTenCount++;
      else if (timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 10) tenAndAboveCount++;
      else if (timesheet.dayType == 'Holiday') holidayCount++;
      else if (timesheet.dayType == 'Non-working') workingOnHolidayCount++;

      if (timesheet.legend == "Pending By User" && timesheet.pendingEodCount > 0) {
        totalListCount = totalListCount + timesheet.pendingEodCount;
        pendingByUserCount = pendingByUserCount + timesheet.pendingEodCount;
      } else {
        totalListCount++;
      }

      timesheet.emp360 = timesheet.empId;
      timesheet.emp360Manager = timesheet.managerId;

    });

    this.zeroToFive = ((zeroToFiveCount / totalListCount) * 100).toFixed(2) + "%";
    this.fiveToEight = ((fiveToEightCount / totalListCount) * 100).toFixed(2) + "%";
    this.eightToNine = ((eightToNineCount / totalListCount) * 100).toFixed(2) + "%";
    this.nineToTen = ((nineToTenCount / totalListCount) * 100).toFixed(2) + "%";
    this.tenAndAbove = ((tenAndAboveCount / totalListCount) * 100).toFixed(2) + "%";
    this.noEODSubmitted = ((pendingByUserCount / totalListCount) * 100).toFixed(2) + "%";
    this.holiday = ((holidayCount / totalListCount) * 100).toFixed(2) + "%";
    this.workingOnHoliday = ((workingOnHolidayCount / totalListCount) * 100).toFixed(2) + "%";

    let timesheetData = [{
      name: "Pending",
      y: pendingCount
    },
    {
      name: "Approved",
      y: approvedCount
    },
    {
      name: "Rejected",
      y: rejectedCount
    },
    {
      name: "Pending By User",
      y: pendingByUserCount
    }];

    //console.log("timesheetData : ", timesheetData);

    let checkTimesheetData = timesheetData.filter(data => data.y != 0);
    //console.log("checkTimesheetData :", checkTimesheetData);

    if (checkTimesheetData && checkTimesheetData.length != 0) {
      this.renderPieSummaryChart('Timesheet Status Summary Chart', 'timesheetStatusSummary', timesheetData, 'Timesheet', this.openTimesheetSummaryTableModel.bind(this));
    } else {
      this.renderPlaceholderChart('Timesheet Status Summary Chart', 'timesheetStatusSummary');
    }
  }

  getCustomTimesheetReport(queryObjList: any, template: TemplateRef<any>) {
    this.timsheetSummaryList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList == '') {
      this.get9DayTimesheetReport();
    } else {

      let tempFrom = "";
      let tempTo = "";
      queryObj.queryList.forEach((query) => {
        if (query.column == 'From Date') {
          tempFrom = query.value;
        }
        if (query.column == 'To Date') {
          tempTo = query.value;
        }
      });

      if (tempFrom == "" && tempTo == "") {
        this.openAlertMod(template, "The date range should be mandatory for the employee.");
        this.get9DayTimesheetReport();
        return;
      }

      let _tempQueryList = JSON.parse(JSON.stringify(queryObj.queryList));
      //console.log(_tempQueryList, "_tempQueryList");
      let _filteredQueryList = _tempQueryList.filter((query) => {
        if (query.column == 'Employee Id' || query.column == 'Department' || query.column == 'Full Name' || query.column == 'Team Name' || query.column == 'Project Name' || query.column == 'Client Name') {
          return Object.assign({}, query);
        }
      });

      _filteredQueryList.forEach((query: Query, index, queries) => {
        if (index == (queries.length - 1)) {
          query.conjunction = "";
        }
      });
      queryObj.queryList1 = _filteredQueryList;
      //console.log( queryObj.queryList1," queryObj.queryList1");
      //console.log(queryObj.queryList, "queryObj.queryList")

      this.timesheetService.customQueryForTimesheetSummaryChart(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.timsheetSummaryList = response.serviceResponse;
          //console.log(this.timsheetSummaryList, " timsheetSummaryList");
          this.extractTimesheetReportData();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  }

  // getEmployeeWorkLocation(){
  //   this.employeeService.getEmployeeWorkLocationForSummary().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.employeeWorkLocationList = response.serviceResponse;

  //       this.employeeWorkLocationList = this.employeeWorkLocationList.filter((value, index, self) =>
  //         index === self.findIndex((t) => (
  //           t.clientLocation === value.clientLocation && t.date === value.date
  //         ))
  //       )

  //       let workLocationCount = this.employeeWorkLocationList.reduce((acc, child) => {
  //         if (!acc[child.clientLocation]) {
  //           acc[child.clientLocation] = 0;
  //         }
  //         acc[child.clientLocation]++;
  //         return acc;
  //       }, {});

  //       let employeeWorkLocationChartData = Object.entries(workLocationCount).map(([location, count]) => ([location, count]));

  //       let employeeWorkLocationCategories = employeeWorkLocationChartData.map(([location]) => ([location]));

  //       //console.log(employeeWorkLocationChartData, " employeeWorkLocationChartData")
  //       //console.log(this.employeeWorkLocationList , " : employeeWorkLocation");

  //       this.renderColumnBarSummaryChart('Employee Work Location Summary','employeeWorkLocationSummary',employeeWorkLocationChartData,employeeWorkLocationCategories,'employee', this.openWorkLocationSummaryTableModal.bind(this));
  //     } else{
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  // getEmployeeWorkLocation(): void {
  //   this.employeeService.getEmployeeWorkLocationForSummary().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus === "Success") {
  //       this.employeeWorkLocationList = response.serviceResponse;
  //       // console.log("Initial employeeWorkLocationList: ", this.employeeWorkLocationList);
  //       this.employeeWorkLocationList.forEach(data => {
  //         data.employeementId = "A-".concat(data.employeementId)
  //         // console.log("matchingEmployee",matchingEmployee);
  //         data.emp360 = data.empId;

  //         // if(data.isConsultant == 'true'){
  //         //   data.employeementId = "A-CS-".concat(data.employeementId)
  //         // }else {
  //         //   data.employeementId = "A-".concat(data.employeementId)
  //         // }
  //         if (data.isConsultant == 'true') {
  //           data.employeeType = "Consultant"
  //         } else if (data.isApprenticeship == 'true') {
  //           data.employeeType = "Apprentice"
  //         } else {
  //           data.employeeType = "Regular"
  //         }
  //       })
  //       // console.log("Initial employeeWorkLocationList: ", this.employeeWorkLocationList);

  //       // this.employeeWorkLocationList = this.employeeWorkLocationList.filter((value, index, self) =>
  //       //   index === self.findIndex((t) => (
  //       //     t.clientLocation === value.clientLocation && t.date === value.date
  //       //   ))
  //       // );
  //       // console.log("Filtered employeeWorkLocationList: ", this.employeeWorkLocationList);

  //       let workLocationCount = this.employeeWorkLocationList.reduce((acc, child) => {
  //         if (!acc[child.clientLocation]) {
  //           acc[child.clientLocation] = 0;
  //         }
  //         acc[child.clientLocation]++;
  //         return acc;
  //       }, {});

  //       let employeeWorkLocationChartData = Object.entries(workLocationCount).map(([location, count]) => ([location, count]));
  //       let employeeWorkLocationCategories = employeeWorkLocationChartData.map(([location]) => location);

  //       // console.log("employeeWorkLocationChartData: ", employeeWorkLocationChartData);
  //       // console.log("employeeWorkLocationCategories: ", employeeWorkLocationCategories);

  //       this.renderColumnBarSummaryChartForWorkLocation(
  //         'Employee Work Location Summary',
  //         'employeeWorkLocationSummary',
  //         employeeWorkLocationChartData,
  //         employeeWorkLocationCategories,
  //         'employee',
  //         this.openWorkLocationSummaryTableModal.bind(this)
  //       );
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  // openWorkLocationSummaryTableModal(category: any): void {
  //   this.modalTitle = "Work Location: " + category;
  //   this.modalSummaryList = this.employeeWorkLocationList.filter(x => x.clientLocation === category);
  //   console.log("Modal Summary List: ", this.modalSummaryList);

  //   this.modalRef = this.modalService.show(this.workLocationSummaryTemplate, { class: 'modal-xl' });
  // }


  // getAllEmployeeList() {
  //   this.allEmployeeList = [];
  //   this.queryList = [];
  //   this.employeeInProbationAfter6MonthsCount = 0;

  //   this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.allEmployeeList = response.serviceResponse;

  //       // this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != "InActive");
  //       for (let x of this.allEmployeeList) {
  //         x.employeementId = "A-".concat(x.employeementId);
  //         // x.employeementId =x.isConsultant ? "A-CS-".concat(x.employeementId) : "A-".concat(x.employeementId);
  //         if (x.isConsultant == 'true') {
  //           x.employeeType = "Consultant"
  //         } else if (x.isApprenticeship == 'true') {
  //           x.employeeType = "Apprentice"
  //         } else {
  //           x.employeeType = "Regular"
  //         }
  //         x.dateOfRelieving = moment(x.dateOfResign).add(x.noticePeriod, 'days').format(this.dateFormat);
  //         x.relievingMonth = moment(x.dateOfRelieving).format('MMMM');
  //         x.joiningMonth = moment(x.dateOfJoining).format('MMMM');

  //         // console.log("matching ",matchingEmployee)
  //         x.emp360 = x.empId;
  //         // console.log("matching ",matchingEmployee)
  //         x.emp360Manager = x.managerId;
  //       }
  //       console.log("allEmployeeList : ", this.allEmployeeList)
  //       // this.extractData();
  //     } else {
  //       alert(response.serviceResponse)
  //     }
  //   });
  // }

  // getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
  //   this.allEmployeeList = [];
  //   this.employeeInProbationAfter6MonthsCount = 0;

  //   let queryObj = new Query();
  //   queryObj.queryList = queryObjList;

  //   if (queryObjList == '') {
  //     this.getAllEmployeeList();
  //   } else {
  //     this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
  //       if (response.serviceStatus == "Success") {
  //         this.allEmployeeList = response.serviceResponse;

  //         //   this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
  //         //   index === self.findIndex((t) => (
  //         //     t.employeementId === value.employeementId
  //         //   ))
  //         // )

  //         if (this.allEmployeeList.length == 0) {
  //           this.openAlertMod(template, "No Data Found");
  //         }
  //         this.allEmployeeList.forEach(employee => {
  //           // if(employee.isConsultant == 'true'){
  //           //   employee.employeementId = "A-CS-".concat(employee.employeementId);
  //           // }else{
  //           //   employee.employeementId = "A-".concat(employee.employeementId);
  //           // }
  //           if (employee.isConsultant == 'true') {
  //             employee.employeeType = "Consultant"
  //           } else if (employee.isApprenticeship == 'true') {
  //             employee.employeeType = "Apprentice"
  //           } else {
  //             employee.employeeType = "Regular"
  //           }
  //           employee.employeementId = "A-".concat(employee.employeementId);
  //           employee.dateOfRelieving = moment(employee.dateOfResign).add(employee.noticePeriod, 'days').format(this.dateFormat);
  //           employee.relievingMonth = moment(employee.dateOfRelieving).format('MMMM');
  //           employee.joiningMonth = moment(employee.dateOfJoining).format('MMMM');

  //           employee.emp360 = employee.empId;
  //           employee.emp360Manager = employee.managerId;
  //         });
  //         // this.extractData();
  //         console.log("allEmployeeList : ", this.allEmployeeList)
  //       } else {
  //         this.openAlertMod(template, response.serviceResponse)
  //       }
  //     });
  //   }
  // }


  // getCustomDepartmentWiseBillableEmployeesList(departmentIds: any[]) {
  //   this.departmentWiseBillableEmployeeList = [];
  //   console.log("Selected Department IDs: ", departmentIds);
  //   this.departmentIds = departmentIds;
  //   if (departmentIds.length === 0) {
  //     this.getAllBillableEmployeeData();
  //   } else {
  //     this.employeeService.customQueryForDepartmentWiseBillableEmployeeReport(this.departmentIds).pipe(first()).subscribe((response: any) => {
  //       if (response.serviceStatus === "Success") {
  //         this.departmentWiseBillableEmployeeList = response.serviceResponse;
  //         this.departmentWiseBillableEmployeeList.forEach(employee => {
  //           let age = this.getAge(employee.dateOfBirth);
  //           employee.age = age;
  //         })

  //         if (this.departmentWiseBillableEmployeeList.length === 0) {
  //           this.openAlertMod(this.alertTemplate, "No Data Found");
  //         }

  //         this.departmentWiseBillableEmployeeList.forEach(employee => {
  //           employee.employeementId = "A-".concat(employee.employeementId);
  //         });

  //         // this.departmentWiseBillableEmployeeList.forEach(employee => {
  //         //   employee.employeementId = employee.isConsultant 
  //         //     ? "A-CS-".concat(employee.employeementId) 
  //         //     : "A-".concat(employee.employeementId);
  //         // }); 
  //         this.departmentWiseBillableEmployeeList.forEach(employee => {
  //           if (employee.isConsultant == 'true') {
  //             employee.employeeType = "Consultant"
  //           } else if (employee.isApprenticeship == 'true') {
  //             employee.employeeType = "Apprentice"
  //           } else {
  //             employee.employeeType = "Regular"
  //           }
  //         });

  //         this.extractDataForBillable();
  //         console.log("Department-wise Billable Employee List: ", this.departmentWiseBillableEmployeeList);
  //         console.log("Department-wise Billable Employee List: ", this.departmentWiseBillableEmployeeList.length);
  //       } else {
  //         this.openAlertMod(this.alertTemplate, response.serviceResponse);
  //       }
  //     });
  //   }
  // }


  onSelectionChange(event: any) {
    // this.getCustomDepartmentWiseBillableEmployeesList(this.departmentIds);
    // this.getAllBillableTypeCount(this.departmentIds);
  }

  profileKycStatus = "";


  extractDataForBillable() {

    // let internalBillableCount = 0;
    // let tnmBillableCount = 0;
    // let fcBillableCount = 0;
    // let shadowBillableCount = 0;
    // let benchBillableCount = 0;

    // const benchBillableEmployees: any[] = [];
    // const Tnmcount: any[] = [];

    // this.departmentWiseBillableEmployeeList.forEach((employee) => {
    //   // if (employee.billableType == 'TNM' && employee.employmentstatus != "InActive") tnmBillableCount++;
    //   // if (
    //   //   employee.billableType == 'TNM' &&
    //   //   employee.employmentstatus != "InActive" &&
    //   //   employee.isApprenticeship != 'true' &&
    //   //   employee.isConsultant != 'true' 
    //   // ) tnmBillableCount++;{
    //   //   Tnmcount.push(employee);
    //   // }
    //   // if (employee.billableType == 'Bench' && employee.employmentstatus != "InActive") {
    //   //   benchBillableCount++;
    //   //   benchBillableEmployees.push(employee);
    //   // }
    //   // if (employee.billableType == 'Fixed Cost' && employee.employmentstatus != "InActive") fcBillableCount++;
    //   // if (employee.billableType == 'Shadow' && employee.employmentstatus != "InActive") shadowBillableCount++;
    //   // if (employee.billableType == 'InternalRNDProducts' && employee.employmentstatus != "InActive") internalBillableCount++;

    //   let empTotalExperience = this.totalExperience(employee.dateOfJoining, employee.totalExperience);
    //   employee.totalExperience = empTotalExperience.toFixed(1);

    //   if (employee.profileCompletedPercent < 100.00) {
    //     this.profilestatus = "No";
    //     // employee.profileCompletedPercent = this.profilestatus;
    //     employee.profileKycStatus = this.profilestatus
    //   }
    //   if (employee.profileCompletedPercent >= 100.00) {
    //     this.profilestatus = "Yes";
    //     // employee.profileCompletedPercent = this.profilestatus
    //     employee.profileKycStatus = this.profilestatus
    //   }
    //   // console.log('Bench Billable Employees (not InActive):', benchBillableEmployees.length, benchBillableEmployees);
    // })

    // console.log("Anurag kyc issue",this.departmentWiseBillableEmployeeList)

    // billableChartByDepartment pie chart
    
    console.log("TNM Count: ", this.tnmBillableCount);

    let deptWiseBillableType = [{
      name: "TNM",
      y: this.tnmBillableCount
    },
    {
      name: "Fixed Cost",
      y: this.fcBillableCount
    },
    {
      name: "InternalRNDProducts",
      y: this.internalBillableCount
    },
    {
      name: "Bench",
      y: this.benchBillableCount
    },
    {
      name: "Shadow",
      y: this.shadowBillableCount
    }
    ]
    let checkDeptWiseData = deptWiseBillableType.filter(data => data.y != 0);

    if (checkDeptWiseData && checkDeptWiseData.length != 0) {
      // console.log("deptWiseBillabledata", deptWiseBillableType);
      deptWiseBillableType.forEach(data => console.log(data));
      this.renderPieSummaryChart('Department wise Billable/Non-Billable Employee Summary', 'billableChartByDepartment', deptWiseBillableType, 'Department wise Billable Data', this.openDepartmentWiseBillableEmployeeTableModal.bind(this));
    } else {
      this.renderPlaceholderChart('Department wise Billable/Non-Billable Employee Summary', 'billableChartByDepartment');
    }

  }

  // ...existing code in extractDataForBillable()...

  profilestatus = "";

  // extractData() {
  //   //Total Count
  //   // this.countOfAllEmployees = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive').length;
  //   //console.log("Count of all employees : ",this.countOfAllEmployees);

  //   //Fresher / Lateral Graph (Fresher/Experienced)
  //   //Graph Based Employment Status(Probation/Confirmed/Resigned/In-Active)
  //   //Male / Female Graph
  //   //Age Wise Graph
  //   // Total Experience Graph
  //   // Join vs Resign Graph
  //   // let fresherCount = 0;
  //   // let experienceCount = 0;

  //   // let probationCount = 0;
  //   // let confirmedCount = 0;
  //   // let resignedCount = 0;
  //   // let inActiveCount = 0;

  //   // let maleCount = 0;
  //   // let femaleCount = 0;
  //   // let otherCount = 0;

  //   // let countBetween18and25 = 0;
  //   // let countBetween25and35 = 0;
  //   // let countBetween35and45 = 0;
  //   // let countAbove45 = 0;

  //   // let experienceCountBetween0and1 = 0;
  //   // let experienceCountBetween0and1Apprentice = 0;
  //   // let experienceCountBetween0and1Consultant = 0;
  //   // let experienceCountBetween1and2 = 0;
  //   // let experienceCountBetween1and2Apprentice = 0;
  //   // let experienceCountBetween1and2Consultant = 0;
  //   // let experienceCountBetween2and5 = 0;
  //   // let experienceCountBetween2and5Apprentice = 0;
  //   // let experienceCountBetween2and5Consultant = 0;
  //   // let experienceCountBetween5and10 = 0;
  //   // let experienceCountBetween5and10Apprentice = 0;
  //   // let experienceCountBetween5and10Consultant = 0;
  //   // let experienceCountAbove10 = 0;
  //   // let experienceCountAbove10Apprentice = 0;
  //   // let experienceCountAbove10Consultant = 0;

  //   let joiningJanCount = 0;
  //   let joiningFebCount = 0;
  //   let joiningMarCount = 0;
  //   let joiningAprilCount = 0;
  //   let joiningMayCount = 0;
  //   let joiningJuneCount = 0;
  //   let joiningJulyCount = 0;
  //   let joiningAugCount = 0;
  //   let joiningSepCount = 0;
  //   let joiningOctoberCount = 0;
  //   let joiningNovCount = 0;
  //   let joiningDecCount = 0;

  //   let resignJanCount = 0;
  //   let resignFebCount = 0;
  //   let resignMarCount = 0;
  //   let resignAprilCount = 0;
  //   let resignMayCount = 0;
  //   let resignJuneCount = 0;
  //   let resignJulyCount = 0;
  //   let resignAugCount = 0;
  //   let resignSepCount = 0;
  //   let resignOctoberCount = 0;
  //   let resignNovCount = 0;
  //   let resignDecCount = 0;

  //   // let billableCount = 0;
  //   // let nonBillableCount = 0;
  //   // let otherBillableCount = 0;

  //   let joinApprenticeJanCount = 0;
  //   let joinApprenticeFebCount = 0;
  //   let joinApprenticeMarCount = 0;
  //   let joinApprenticeAprCount = 0;
  //   let joinApprenticeMayCount = 0;
  //   let joinApprenticeJunCount = 0;
  //   let joinApprenticeJulCount = 0;
  //   let joinApprenticeAugCount = 0;
  //   let joinApprenticeSepCount = 0;
  //   let joinApprenticeOctCount = 0;
  //   let joinApprenticeNovCount = 0;
  //   let joinApprenticeDecCount = 0;

  //   let joinConsultantJanCount = 0;
  //   let joinConsultantFebCount = 0;
  //   let joinConsultantMarCount = 0;
  //   let joinConsultantAprCount = 0;
  //   let joinConsultantMayCount = 0;
  //   let joinConsultantJunCount = 0;
  //   let joinConsultantJulCount = 0;
  //   let joinConsultantAugCount = 0;
  //   let joinConsultantSepCount = 0;
  //   let joinConsultantOctCount = 0;
  //   let joinConsultantNovCount = 0;
  //   let joinConsultantDecCount = 0;



  //   // this.allEmployeeList.forEach((employee) => {
  //   //   let currentYear = moment().year();
  //   //   let dateToday = moment().format(this.dateFormat);



  //   //   // if (employee.experience == 'Fresher' && employee.employmentstatus != 'InActive') fresherCount++;
  //   //   // else if (employee.experience == 'Experienced' && employee.employmentstatus != 'InActive') experienceCount++;

  //   //   // if (employee.employmentstatus == "Probation") probationCount++;
  //   //   // else if (employee.employmentstatus == "Confirmed") confirmedCount++;
  //   //   // else if (employee.employmentstatus == "Resigned") resignedCount++;
  //   //   // else if (employee.employmentstatus == "InActive") inActiveCount++;

  //   //   // if (employee.gender == 'male' && employee.employmentstatus != 'InActive') maleCount++;
  //   //   // else if (employee.gender == 'female' && employee.employmentstatus != 'InActive') femaleCount++;
  //   //   // else if (employee.gender == 'other' && employee.employmentstatus != 'InActive') otherCount++;

  //   //   // if (employee.dateOfBirth != null && employee.employmentstatus != 'InActive') {
  //   //   //   let age = this.getAge(employee.dateOfBirth);
  //   //   //   employee.age = age;
  //   //   //   //console.log(age);
  //   //   //   if (age >= 18 && age <= 25) countBetween18and25++;
  //   //   //   else if (age > 25 && age <= 35) countBetween25and35++;
  //   //   //   else if (age > 35 && age <= 45) countBetween35and45++;
  //   //   //   else if (age > 45) countAbove45++;
  //   //   // }

  //   //   // if(employee.dateOfJoining != null && employee.employmentstatus != 'InActive'){
  //   //   //   if(employee.totalExperience == null)employee.totalExperience = 0;
  //   //   //   let empTotalExperience = this.totalExperience(employee.dateOfJoining, employee.totalExperience);
  //   //   //   employee.totalExperience = empTotalExperience.toFixed(1);
  //   //   //   if(employee.totalExperience >= 0 && employee.totalExperience <= 1)experienceCountBetween0and1++
  //   //   //   else if(employee.totalExperience > 1 && employee.totalExperience <= 2)experienceCountBetween1and2++;
  //   //   //   else if(employee.totalExperience > 2 && employee.totalExperience <= 5)experienceCountBetween2and5++;
  //   //   //   else if(employee.totalExperience > 5 && employee.totalExperience <= 10)experienceCountBetween5and10++;
  //   //   //   else if(employee.totalExperience > 10)experienceCountAbove10++;
  //   //   // }
  //   //   // if (employee.dateOfJoining != null && employee.employmentstatus != 'InActive') {
  //   //   //   if (employee.totalExperience == null) employee.totalExperience = 0;

  //   //   //   let empTotalExperience = this.totalExperience(employee.dateOfJoining, employee.totalExperience);
  //   //   //   employee.totalExperience = empTotalExperience.toFixed(1);

  //   //     // Check for apprentices in the "0 to 1" experience category
  //   //   //   if ((employee.totalExperience >= 0 && employee.totalExperience <= 1) && (employee.isApprenticeship != 'true' || employee.isApprenticeship === null) && (employee.isConsultant != 'true' || employee.isConsultant === null)) {
  //   //   //     experienceCountBetween0and1++;
  //   //   //   } else if (employee.totalExperience >= 0 && employee.totalExperience <= 1 && employee.isApprenticeship === 'true') {
  //   //   //     experienceCountBetween0and1Apprentice++;
  //   //   //   } else if (employee.totalExperience >= 0 && employee.totalExperience <= 1 && employee.isConsultant === 'true') {
  //   //   //     experienceCountBetween0and1Consultant++;
  //   //   //   }
  //   //   //   else if ((employee.totalExperience > 1 && employee.totalExperience <= 2) && (employee.isApprenticeship != 'true' || employee.isApprenticeship === null) && (employee.isConsultant != 'true' || employee.isConsultant === null)) {
  //   //   //     experienceCountBetween1and2++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 1 && employee.totalExperience <= 2 && employee.isApprenticeship === 'true') {
  //   //   //     experienceCountBetween1and2Apprentice++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 1 && employee.totalExperience <= 2 && employee.isConsultant === 'true') {
  //   //   //     experienceCountBetween1and2Consultant++;
  //   //   //   }
  //   //   //   else if ((employee.totalExperience > 2 && employee.totalExperience <= 5) && (employee.isApprenticeship != 'true' || employee.isApprenticeship === null) && (employee.isConsultant != 'true' || employee.isConsultant === null)) {
  //   //   //     experienceCountBetween2and5++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 2 && employee.totalExperience <= 5 && employee.isApprenticeship === 'true') {
  //   //   //     experienceCountBetween2and5Apprentice++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 2 && employee.totalExperience <= 5 && employee.isConsultant === 'true') {
  //   //   //     experienceCountBetween2and5Consultant++;
  //   //   //   }
  //   //   //   else if ((employee.totalExperience > 5 && employee.totalExperience <= 10) && (employee.isApprenticeship != 'true' || employee.isApprenticeship === null) && (employee.isConsultant != 'true' || employee.isConsultant === null)) {
  //   //   //     experienceCountBetween5and10++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 5 && employee.totalExperience <= 10 && employee.isApprenticeship === 'true') {
  //   //   //     experienceCountBetween5and10Apprentice++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 5 && employee.totalExperience <= 10 && employee.isConsultant === 'true') {
  //   //   //     experienceCountBetween5and10Consultant++;
  //   //   //   }
  //   //   //   else if ((employee.totalExperience > 10) && (employee.isApprenticeship != 'true' || employee.isApprenticeship === null) && (employee.isConsultant != 'true' || employee.isConsultant === null)) {
  //   //   //     experienceCountAbove10++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 10 && employee.isApprenticeship === 'true') {
  //   //   //     experienceCountAbove10Apprentice++;
  //   //   //   }
  //   //   //   else if (employee.totalExperience > 10 && employee.isConsultant === 'true') {
  //   //   //     experienceCountAbove10Consultant++;
  //   //   //   }
  //   //   }


  //     // if(employee.joiningMonth == 'January' && moment(employee.dateOfJoining).year() == currentYear)joiningJanCount++;
  //     // else if(employee.joiningMonth == 'February' && moment(employee.dateOfJoining).year() == currentYear)joiningFebCount++;
  //     // else if(employee.joiningMonth == 'March' && moment(employee.dateOfJoining).year() == currentYear)joiningMarCount++;
  //     // else if(employee.joiningMonth == 'April' && moment(employee.dateOfJoining).year() == currentYear)joiningAprilCount++;
  //     // else if(employee.joiningMonth == 'May' && moment(employee.dateOfJoining).year() == currentYear)joiningMayCount++;
  //     // else if(employee.joiningMonth == 'June' && moment(employee.dateOfJoining).year() == currentYear)joiningJuneCount++;
  //     // else if(employee.joiningMonth == 'July' && moment(employee.dateOfJoining).year() == currentYear)joiningJulyCount++;
  //     // else if(employee.joiningMonth == 'August' && moment(employee.dateOfJoining).year() == currentYear)joiningAugCount++;
  //     // else if(employee.joiningMonth == 'September' && moment(employee.dateOfJoining).year() == currentYear)joiningSepCount++;
  //     // else if(employee.joiningMonth == 'October' && moment(employee.dateOfJoining).year() == currentYear)joiningOctoberCount++;
  //     // else if(employee.joiningMonth == 'November' && moment(employee.dateOfJoining).year() == currentYear)joiningNovCount++;
  //     // else if(employee.joiningMonth == 'December' && moment(employee.dateOfJoining).year() == currentYear)joiningDecCount++;

  //   //   if (moment(employee.dateOfJoining).year() === currentYear && employee.employmentstatus != 'InActive') {
  //   //     if (employee.joiningMonth === 'January') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeJanCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantJanCount++;
  //   //       } else {
  //   //         joiningJanCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'February') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeFebCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantFebCount++;
  //   //       } else {
  //   //         joiningFebCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'March') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeMarCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantMarCount++;
  //   //       } else {
  //   //         joiningMarCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'April') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeAprCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantAprCount++;
  //   //       } else {
  //   //         joiningAprilCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'May') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeMayCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantMayCount++;
  //   //       } else {
  //   //         joiningMayCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'June') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeJunCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantJunCount++;
  //   //       } else {
  //   //         joiningJuneCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'July') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeJulCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantJulCount++;
  //   //       } else {
  //   //         joiningJulyCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'August') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeAugCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantAugCount++;
  //   //       } else {
  //   //         joiningAugCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'September') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeSepCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantSepCount++;
  //   //       } else {
  //   //         joiningSepCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'October') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeOctCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantOctCount++;
  //   //       } else {
  //   //         joiningOctoberCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'November') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeNovCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantNovCount++;
  //   //       } else {
  //   //         joiningNovCount++;
  //   //       }
  //   //     } else if (employee.joiningMonth === 'December') {
  //   //       if (employee.isApprenticeship === 'true') {
  //   //         joinApprenticeDecCount++;
  //   //       } else if (employee.isConsultant === 'true') {
  //   //         joinConsultantDecCount++;
  //   //       } else {
  //   //         joiningDecCount++;
  //   //       }
  //   //     }
  //   //   }

  //   //   if (employee.relievingMonth == 'January' && moment(employee.dateOfRelieving).year() == currentYear) resignJanCount++;
  //   //   else if (employee.relievingMonth == 'February' && moment(employee.dateOfRelieving).year() == currentYear) resignFebCount++;
  //   //   else if (employee.relievingMonth == 'March' && moment(employee.dateOfRelieving).year() == currentYear) resignMarCount++;
  //   //   else if (employee.relievingMonth == 'April' && moment(employee.dateOfRelieving).year() == currentYear) resignAprilCount++;
  //   //   else if (employee.relievingMonth == 'May' && moment(employee.dateOfRelieving).year() == currentYear) resignMayCount++;
  //   //   else if (employee.relievingMonth == 'June' && moment(employee.dateOfRelieving).year() == currentYear) resignJuneCount++;
  //   //   else if (employee.relievingMonth == 'July' && moment(employee.dateOfRelieving).year() == currentYear) resignJulyCount++;
  //   //   else if (employee.relievingMonth == 'August' && moment(employee.dateOfRelieving).year() == currentYear) resignAugCount++;
  //   //   else if (employee.relievingMonth == 'September' && moment(employee.dateOfRelieving).year() == currentYear) resignSepCount++;
  //   //   else if (employee.relievingMonth == 'October' && moment(employee.dateOfRelieving).year() == currentYear) resignOctoberCount++;
  //   //   else if (employee.relievingMonth == 'November' && moment(employee.dateOfRelieving).year() == currentYear) resignNovCount++;
  //   //   else if (employee.relievingMonth == 'December' && moment(employee.dateOfRelieving).year() == currentYear) resignDecCount++;

  //   //   // if (employee.dateOfJoining != null && employee.employmentstatus != 'InActive') {
  //   //   //   if (moment(dateToday).diff(moment(employee.dateOfJoining), 'months', true) > 6 && employee.employmentstatus == 'Probation') this.employeeInProbationAfter6MonthsCount++;
  //   //   // }
  //   //   // if (employee.employmentstatus != 'InActive' && employee.isApprenticeship === 'true') this.apprenticeCountForDisplay++;
  //   //   // if (employee.employmentstatus != 'InActive' && employee.isConsultant === 'true') this.consultantCountForDisplay++;
  //   //   // if (employee.employmentstatus != 'InActive' && employee.isConsultant != 'true' && employee.isApprenticeship != 'true') this.regularCountForDisplay++;

  //   //   // if ((employee.billable == 'Yes' && employee.billableType != null) && employee.employmentstatus != "InActive") billableCount++;
  //   //   // if ((employee.billable == 'No' && employee.billableType != null) && employee.employmentstatus != "InActive") nonBillableCount++;
  //   //   // if ((employee.billable == "Yes" || employee.billable == "No" || employee.billable == null) && employee.billableType == null && employee.employmentstatus != 'InActive') otherBillableCount++;

  //   //   if (employee.profileCompletedPercent < 100.00) {
  //   //     this.profilestatus = "No";
  //   //     // employee.profileCompletedPercent = this.profilestatus;
  //   //     employee.profileKycStatus = this.profilestatus
  //   //   }
  //   //   if (employee.profileCompletedPercent >= 100.00) {
  //   //     this.profilestatus = "Yes";
  //   //     // employee.profileCompletedPercent = this.profilestatus
  //   //     employee.profileKycStatus = this.profilestatus
  //   //   }

  //   // });

  //   // let departmentList = this.groupBy(
  //   //   this.allEmployeeList.filter((x) => x.employmentstatus != 'InActive' && x.isConsultant != 'true' && x.isApprenticeship != 'true'),
  //   //   'departmentName'
  //   // );


  //   // let departmentList = this.groupBy(
  //   //   this.allEmployeeList.filter((x) => x.isConsultant != 'true' && x.isApprenticeship != 'true'),
  //   //   'departmentName'
  //   // );

  //   // let departmentListForApprentice = this.groupBy(
  //   //   this.allEmployeeList.filter(
  //   //     (x) => x.employmentstatus != 'InActive' && x.isApprenticeship === 'true' && x.isConsultant != 'true'
  //   //   ),
  //   //   'departmentName'
  //   // );

  //   // let departmentListForConsultant = this.groupBy(
  //   //   this.allEmployeeList.filter(
  //   //     (x) => x.employmentstatus != 'InActive' && x.isApprenticeship != 'true' && x.isConsultant === 'true'
  //   //   ),
  //   //   'departmentName'
  //   // );

  //   // console.log('departmentList -- ', departmentList);
  //   // console.log('departmentListForApprentice -- ', departmentListForApprentice);

  //   // let employeeByDepartment = [];

  //   // for (let department in departmentList) {
  //   //   let employeeCount = departmentList[department].length;

  //   //   let apprenticeCount = departmentListForApprentice[department]?.length || 0;

  //   //   let consultantCount = departmentListForConsultant[department]?.length || 0;

  //   //   employeeByDepartment.push({
  //   //     departmentName: department,
  //   //     employeeCount: employeeCount,
  //   //     apprenticeCount: apprenticeCount,
  //   //     consultantCount: consultantCount
  //   //   });
  //   // }

  //   // console.log('employeeByDepartment -- ', employeeByDepartment);

  //   //Department wise Employee Count
  //   // let departmentList = this.groupBy(this.allEmployeeList.filter(x => x.employmentstatus != 'InActive'),'departmentName');
  //   // let departmentListForApprentice = this.groupBy(this.allEmployeeList.filter(x => x.employmentstatus != 'InActive' && x.isApprenticeship === 'true'),'departmentName');
  //   // console.log('departmentListForApprentice -- ',departmentListForApprentice);

  //   // let employeeByDepartment = [];
  //   //   for (let department in departmentList) {
  //   //      //employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length})
  //   //     for (let departmentApprentice in departmentListForApprentice){
  //   //       if (department === departmentApprentice){
  //   //         employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length, apprenticeCount :departmentListForApprentice[departmentApprentice].length})
  //   //       }else{
  //   //         employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length})
  //   //       }                                                                                       ``
  //   //     }
  //   //   }

  //   // let employeeByDepartment = [];
  //   //   for (let department in departmentList) {
  //   //      //employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length})
  //   //     for (let departmentApprentice in departmentListForApprentice){
  //   //         employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length, apprenticeCount :departmentListForApprentice[departmentApprentice].length})
  //   //         employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length})
  //   //     }
  //   //   }
  //   //   console.log('employeeByDepartment --- ',employeeByDepartment);



  //   //       // Sort department names by employee count for departmentWiseEmployee chart
  //   // employeeByDepartment = this.sortDepartmentsByEmployeeCount(employeeByDepartment);

  //   //Age Wise Graph

  //   //console.log("Freshers count: ",fresherCount);
  //   //console.log("Experience count: ",experienceCount);
  //   //console.log("----------------------------------------------------");
  //   //console.log("Probation employees: ",probationCount);
  //   //console.log("Confirmed employees: ",confirmedCount);
  //   //console.log("Resigned employees: ",resignedCount);
  //   //console.log("In-Active employees: ",inActiveCount);
  //   //console.log("----------------------------------------------------");
  //   //console.log("Male employees: ",maleCount);
  //   //console.log("Female employees: ",femaleCount);
  //   //console.log("Other employees: ", otherCount);
  //   //console.log("----------------------------------------------------")
  //   //console.log(employeeByDepartment);
  //   //console.log("----------------------------------------------------")
  //   //console.log("Age 18-25: ",countBetween18and25);
  //   //console.log("Age 26-35: ",countBetween25and35);
  //   //console.log("Age 36-45: ",countBetween35and45);
  //   //console.log("Age above 45: ",countAbove45);
  //   //console.log("----------------------------------------------------")
  //   //console.log("experience 0-1: ",experienceCountBetween0and1);
  //   //console.log("experience 1-2: ",experienceCountBetween1and2);
  //   //console.log("experience 2-5: ",experienceCountBetween2and5);
  //   //console.log("experience 5-10: ",experienceCountBetween5and10);
  //   //console.log("experience Above 10: ",experienceCountAbove10);
  //   //console.log("----------------------------------------------------")
  //   //console.log("joiningJanCount ", joiningJanCount);


  //   /*
  //   Chart Data for - Employee Status Graph.
  //   */
  //   // let employeeStatusData = [{
  //   //   name: "Probation",
  //   //   y: this.probationCount
  //   // },
  //   // {
  //   //   name: "Confirmed",
  //   //   y: this.confirmedCount
  //   // },
  //   // {
  //   //   name: "Resigned",
  //   //   y: this.resignedCount
  //   // },
  //   // {
  //   //   name: "InActive",
  //   //   y: this.inActiveCount
  //   // }];

  //   // //console.log("leaveStatusData : ", employeeStatusData);

  //   // let checkEmployeeStatusData = employeeStatusData.filter(data => data.y != 0);
  //   // //console.log("checkEmployeeStatusData :", checkEmployeeStatusData);

  //   // if (checkEmployeeStatusData && checkEmployeeStatusData.length != 0) {
  //   //   this.renderPieSummaryChart('Employee Status Summary', 'employeeStatus', employeeStatusData, 'Employee Status', this.openEmployeeStatusTableModal.bind(this));
  //   // } else {
  //   //   this.renderPlaceholderChart('Employee Status Summary', 'employeeStatus');
  //   // }



  //   /*
  //         pie chart for billable non billable
  //         */

    // let billableTypeData = [{
    //   name: "Yes",
    //   y: this.billableCount
    // }, {
    //   name: "No",
    //   y: this.nonBillableCount
    // },
    // {
    //   name: "Other",
    //   y: this.otherBillableCount
    // }]

    // let checkEmployeeBillableData = billableTypeData.filter(data => data.y != 0);
    // // console.log(" checkEmployeeBillableData ",checkEmployeeBillableData);

    // if (checkEmployeeBillableData && checkEmployeeBillableData.length != 0) {
    //   this.renderPieSummaryChart('Billable Employee Summary', 'billableChart', billableTypeData, 'Billable Data', this.openBillableEmployeeTableModal.bind(this));
    // } else {
    //   this.renderPlaceholderChart('Billable Employee Summary', 'billableChart');
    // }



  //   //   /*
  //   //   Chart Data for - Department Wise Employee Summary Graph.
  //   //  */
  //   //   let departmentData = employeeByDepartment.map(dept => ({
  //   //     departmentName: dept.departmentName,
  //   //     employeeCount: dept.employeeCount,
  //   //     apprenticeCount: dept.apprenticeCount || 0,
  //   //     consultantCount: dept.consultantCount || 0
  //   //   }));

  //   //   departmentData.sort((a, b) => b.employeeCount - a.employeeCount);

  //   //   // Extract departmentWiseEmployeeData and departmentWiseEmployeeCategories after sorting
  //   //   // let departmentWiseEmployeeData = departmentData.map(dept => [dept.departmentName, dept.employeeCount]);
  //   //   // let departmentWiseEmployeeCategories = departmentData.map(dept => [dept.departmentName]);
  //   //   // Prepare data and categories for the chart
  //   //   let departmentWiseEmployeeData = departmentData.map(dept => ({
  //   //     name: dept.departmentName,
  //   //     data: [dept.employeeCount, dept.apprenticeCount, dept.consultantCount],
  //   //   }));
  //   //   let departmentWiseEmployeeCategories = departmentData.map(dept => dept.departmentName);

  //   //   let data = departmentWiseEmployeeData.forEach((d => {
  //   //     console.log("departmentWiseEmployeeCategories", d);
  //   //   }))


  //   //   this.renderDepartmentWiseEmployeeChart(
  //   //     'Department Wise Employee',
  //   //     'departmentWiseEmployee',
  //   //     this.departmentWiseEmployeeData,
  //   //     this.departmentWiseEmployeeCategories,
  //   //     'Department',
  //   //     this.openDepartmentWiseEmployeeModalTable.bind(this)
  //   //   );



  //   /*
  //   Chart Data for - Male / Female - Gender Summary Graph.
  //  */



  // //   let genderData = [{
  // //     name: "male",
  // //     y: this.maleCount
  // //   },
  // //   {
  // //     name: "female",
  // //     y: this.femaleCount
  // //   },
  // //   {
  // //     name: "other",
  // //     y: this.otherCount
  // //   }];

  // //   //console.log("genderData : ", genderData);

  // //   let checkGenderData = genderData.filter(data => data.y != 0);
  // //   //console.log("checkGenderData :", checkGenderData);

  // //   if (checkGenderData && checkGenderData.length != 0) {
  // //     this.renderPieSummaryChart('Gender Summary', 'genderSummary', genderData, 'Employee Summary', this.openGenderSummaryModalTable.bind(this));
  // //   } else {
  // //     this.renderPlaceholderChart('Gender Summary', 'genderSummary');
  // //   }

  // //   /*
  // //  Chart Data for - Age Summary Graph.
  // // */

  // //   let employeeAgeData = [{
  // //     name: "18 to 25",
  // //     y: this.countBetween18and25
  // //   },
  // //   {
  // //     name: "25 to 35",
  // //     y: this.countBetween25and35
  // //   },
  // //   {
  // //     name: "35 to 45",
  // //     y: this.countBetween35and45
  // //   },
  // //   {
  // //     name: "45+",
  // //     y: this.countAbove45
  // //   }];

  // //   //console.log("employeeAgeData : ", employeeAgeData);

  // //   let checkEmployeeAgeData = employeeAgeData.filter(data => data.y != 0);
  // //   //console.log("checkGenderData :", checkGenderData);

  // //   if (checkEmployeeAgeData && checkEmployeeAgeData.length != 0) {
  // //     this.renderPieSummaryChart('Age Summary', 'employeeAgeSummary', employeeAgeData, 'Employee Summary', this.openAgeSummayModalTable.bind(this));
  // //   } else {
  // //     this.renderPlaceholderChart('Age Summary', 'employeeAgeSummary');
  // //   }

  //   /*
  //   Chart Data for - Employee Experience Graph Data.
  //  */

  //   // let experienceData = [{
  //   //   name: "0 to 1",
  //   //   y: experienceCountBetween0and1
  //   // },{
  //   //   name: "1 to 2",
  //   //   y: experienceCountBetween1and2
  //   // },
  //   // {
  //   //   name: "2 to 5",
  //   //   y: experienceCountBetween2and5
  //   // },
  //   // {
  //   //   name: "5 to 10",
  //   //   y: experienceCountBetween5and10
  //   // },
  //   // {
  //   //   name: "10+",
  //   //   y: experienceCountAbove10
  //   // }];


  //   // let totalExperienceData = experienceData.map(exp => {
  //   //   return [exp.name, exp.y]
  //   // })

  //   // let totalExperienceCategories = experienceData.map(exp => {
  //   //   return [exp.name]
  //   // })

  // //   let experienceData = [
  // //     {
  // //       name: "0 to 1",
  // //       employeeCount: this.experienceCountBetween0and1,
  // //       apprenticeCount: this.experienceCountBetween0and1Apprentice,
  // //       consultantCount: this.experienceCountBetween0and1Consultant,
  // //     },
  // //     {
  // //       name: "1 to 2",
  // //       employeeCount: this.experienceCountBetween1and2,
  // //       apprenticeCount: this.experienceCountBetween1and2Apprentice,
  // //       consultantCount: this.experienceCountBetween1and2Consultant,
  // //     },
  // //     {
  // //       name: "2 to 5",
  // //       employeeCount: this.experienceCountBetween2and5,
  // //       apprenticeCount: this.experienceCountBetween2and5Apprentice,
  // //       consultantCount: this.experienceCountBetween2and5Consultant,
  // //     },
  // //     {
  // //       name: "5 to 10",
  // //       employeeCount: this.experienceCountBetween5and10,
  // //       apprenticeCount: this.experienceCountBetween5and10Apprentice,
  // //       consultantCount: this.experienceCountBetween5and10Consultant,
  // //     },
  // //     {
  // //       name: "10+",
  // //       employeeCount: this.experienceCountAbove10,
  // //       apprenticeCount: this.experienceCountAbove10Apprentice,
  // //       consultantCount: this.experienceCountAbove10Consultant,
  // //     },
  // //   ];

  // //   // Categories (Experience Ranges)
  // //   let totalExperienceCategories = experienceData.map(exp => exp.name);

  // //   // Series Data (for employees and apprentices)
  // //   let employeeSeries = experienceData.map(exp => exp.employeeCount);
  // //   let apprenticeSeries = experienceData.map(exp => exp.apprenticeCount);
  // //   let consultantSeries = experienceData.map(exp => exp.consultantCount);

  // //   //console.log(" totalExperienceData :", totalExperienceData);
  // //   //this.renderColumnBarSummaryChart('Employee Experience','employeeExperienceSummary',totalExperienceData,totalExperienceCategories,'Experience', this.openEmployeeExperienceModalTable.bind(this));
  // //   // this.renderColumnBarSummaryChart(
  // //   //   'Employee Experience',
  // //   //   'employeeExperienceSummary', 
  // //   //   totalExperienceCategories, 
  // //   //   employeeSeries,
  // //   //   apprenticeSeries,
  // //   //   this.openEmployeeExperienceModalTable.bind(this) 
  // //   // );
  // //   this.plotEmployeeExperienceCylinderGraph(
  // //     'Employee Experience',
  // //     'employeeExperienceSummary',
  // //     totalExperienceCategories,
  // //     employeeSeries,
  // //     apprenticeSeries,
  // //     consultantSeries,
  // //     this.openEmployeeExperienceModalTable.bind(this)
  // //   );


  // //   /*
  // //   Chart Data for - Employee Fresher - Lateral Graph Data.
  // //  */

  // //   let fresherLateralData = [{
  // //     name: "Fresher",
  // //     y: this.fresherCount
  // //   },
  // //   {
  // //     name: "Lateral",
  // //     y: this.experienceCount
  // //   }];

  // //   //console.log("genderData : ", genderData);

  // //   let checkFresherLateralData = fresherLateralData.filter(data => data.y != 0);
  // //   //console.log("checkFresherLateralData :", checkFresherLateralData);

  // //   if (checkFresherLateralData && checkFresherLateralData.length != 0) {
  // //     this.renderPieSummaryChart('Fresher - Lateral Summary', 'fresherLateralChart', fresherLateralData, 'Employee Summary', this.openFresherLateralModalTable.bind(this));
  // //   } else {
  // //     this.renderPlaceholderChart('Fresher - Lateral Summary', 'fresherLateralChart');
  // //   }

  //   /*
  //   Chart Data for - Employee Join VS Resign
  //  */

  //   // let empJoinResignData = [{
  //   //   name: "Regular",
  //   //   y: [joiningJanCount,joiningFebCount,joiningMarCount,joiningAprilCount,joiningMayCount,joiningJuneCount,joiningJulyCount,joiningAugCount,joiningSepCount,joiningOctoberCount,joiningNovCount,joiningDecCount]
  //   // },
  //   // {
  //   //   name :"Apprentice",
  //   //   stack : "Joined",
  //   //   y: [joinApprenticeJanCount, joinApprenticeFebCount,joinApprenticeMarCount, joinApprenticeAprCount, joinApprenticeMayCount,joinApprenticeJunCount, joinApprenticeJulCount, joinApprenticeAugCount,joinApprenticeSepCount,joinApprenticeOctCount,joinApprenticeNovCount, joinApprenticeDecCount]
  //   // },
  //   // {
  //   //   name : "Consultant",
  //   //   stack:"joined",
  //   //   y:[joinConsultantJanCount, joinConsultantFebCount,joinConsultantMarCount,joinConsultantAprCount,joinConsultantMayCount,joinConsultantJunCount,joinConsultantJulCount,joinConsultantAugCount,joinConsultantSepCount,joinConsultantOctCount,joinConsultantNovCount,joinConsultantDecCount]
  //   // },
  //   // {
  //   //   name: "Resigned",
  //   //   y: [resignJanCount,resignFebCount,resignMarCount,resignAprilCount,resignMayCount,resignJuneCount,resignJulyCount,resignAugCount,resignSepCount,resignOctoberCount,resignNovCount,resignDecCount]
  //   // }]

  //   //----------------------------------------------------------------------------------------------------------

  //   // let empJoinResignData = [
  //   //   {
  //   //     name: "Regular",
  //   //     y: joinedRegularCount,  // Joined Regular
  //   //     stack: "joined"
  //   //   },
  //   //   {
  //   //     name: "Apprentice",
  //   //     y: joinedApprenticeCount,  // Joined Apprentice
  //   //     stack: "joined"
  //   //   },
  //   //   {
  //   //     name: "Consultant",
  //   //     y: joinedConsultantCount,  // Joined Consultant
  //   //     stack: "joined"
  //   //   },
  //   //   {
  //   //     name: "Resigned",
  //   //     y: resignCounts,  // Resigned employees
  //   //     stack: "resigned"
  //   //   }
  //   // ];
  //   // let empJoinResignData = [
  //   //   {
  //   //     name: 'Joined',
  //   //     data: [joinedRegularCount, joinedApprenticeCount, joinedConsultantCount] // Stack for Joined data
  //   //   },
  //   //   {
  //   //     name: 'Resigned',
  //   //     // data: [resignJanCount] // Single bar for Resigned data
  //   //     data: [resignJanCount,resignFebCount,resignMarCount,resignAprilCount,resignMayCount,resignJuneCount,resignJulyCount,resignAugCount,resignSepCount,resignOctoberCount,resignNovCount,resignDecCount]
  //   //   }
  //   // ];

  //   // let finalEmpJoinResignData = empJoinResignData.map(x => {
  //   //   return {name : x.name, data : x.data}
  //   // })

  //   //for static data
  //   // let joinedRegularCounts = [120, 150, 100, 140, 130, 160, 180, 170, 110, 200, 210, 220];
  //   // let joinedApprenticeCounts = [50, 60, 40, 70, 50, 80, 90, 100, 60, 110, 120, 130];
  //   // let joinedConsultantCounts = [30, 40, 20, 30, 40, 50, 60, 70, 40, 50, 60, 70];
  //   // let resignCounts = [20, 25, 18, 23, 22, 30, 35, 40, 28, 45, 50, 55];

  //   // let finalEmpJoinResignData = [
  //   //   {
  //   //     name: 'Joined Regular',
  //   //     // data: joinedRegularCounts,
  //   //     data: joinedRegularCount,
  //   //     stack: 'joined',  // All "Joined" categories share the same stack
  //   //     color: '#1f77b4'  // Color for Regular (optional)
  //   //   },
  //   //   {
  //   //     name: 'Joined Apprentice',
  //   //     data: joinedApprenticeCount,
  //   //     stack: 'joined',  // Same stack for all "Joined" categories
  //   //     color: '#ff7f0e'  // Color for Apprentice (optional)
  //   //   },
  //   //   {
  //   //     name: 'Joined Consultant',
  //   //     data: joinedConsultantCount,
  //   //     stack: 'joined',  // Same stack for all "Joined" categories
  //   //     color: '#2ca02c'  // Color for Consultant (optional)
  //   //   },
  //   //   {
  //   //     name: 'Resigned',
  //   //     data: resignCounts,
  //   //     stack: 'resigned',  // Resigned has a separate stack
  //   //     color: '#d62728'  // Color for Resigned (optional)
  //   //   }
  //   // ];

  //   // let finalEmpJoinResignData = [
  //   //   {
  //   //     name: 'Joined Regular',
  //   //     // data: joinedRegularCounts,
  //   //     // data: joinedRegularCount,
  //   //     data: [
  //   //       joiningJanCount, joiningFebCount, joiningMarCount, joiningAprilCount, joiningMayCount,
  //   //       joiningJuneCount, joiningJulyCount, joiningAugCount, joiningSepCount, joiningOctoberCount,
  //   //       joiningNovCount, joiningDecCount
  //   //     ],
  //   //     stack: 'joined',  // All "Joined" categories share the same stack
  //   //     color: '#1f77b4'  // Color for Regular (optional)
  //   //   },
  //   //   {
  //   //     name: 'Joined Apprentice',
  //   //     // data: joinedApprenticeCount,
  //   //     data: [
  //   //       joinApprenticeJanCount,
  //   //       joinApprenticeFebCount,
  //   //       joinApprenticeMarCount,
  //   //       joinApprenticeAprCount,
  //   //       joinApprenticeMayCount,
  //   //       joinApprenticeJunCount,
  //   //       joinApprenticeJulCount,
  //   //       joinApprenticeAugCount,
  //   //       joinApprenticeSepCount,
  //   //       joinApprenticeOctCount,
  //   //       joinApprenticeNovCount,
  //   //       joinApprenticeDecCount
  //   //     ],
  //   //     stack: 'joined',  // Same stack for all "Joined" categories
  //   //     color: '#ff7f0e'  // Color for Apprentice (optional)
  //   //   },
  //   //   {
  //   //     name: 'Joined Consultant',
  //   //     // data: joinedConsultantCount,
  //   //     data: [
  //   //       joinConsultantJanCount,
  //   //       joinConsultantFebCount,
  //   //       joinConsultantMarCount,
  //   //       joinConsultantAprCount,
  //   //       joinConsultantMayCount,
  //   //       joinConsultantJunCount,
  //   //       joinConsultantJulCount,
  //   //       joinConsultantAugCount,
  //   //       joinConsultantSepCount,
  //   //       joinConsultantOctCount,
  //   //       joinConsultantNovCount,
  //   //       joinConsultantDecCount
  //   //     ],
  //   //     stack: 'joined',  // Same stack for all "Joined" categories
  //   //     color: '#2ca02c'  // Color for Consultant (optional)
  //   //   },
  //   //   {
  //   //     name: 'Resigned',
  //   //     // data: resignCounts,
  //   //     data: [resignJanCount, resignFebCount, resignMarCount, resignAprilCount, resignMayCount, resignJuneCount, resignJulyCount, resignAugCount, resignSepCount, resignOctoberCount, resignNovCount, resignDecCount],
  //   //     stack: 'resigned',  // Resigned has a separate stack
  //   //     color: '#d62728'  // Color for Resigned (optional)
  //   //   }
  //   // ];


  //   // console.log("finalEmpJoinResignData :", finalEmpJoinResignData);
  //   // this.renderMultiBarChart('Employee Join VS Resign', 'employeeJoinAndResign', finalEmpJoinResignData, 'Employee', this.openEmployeeJoinResignModalTable.bind(this));
  //   // this.renderMultiBarChart('Employee Join VS Resign','employeeJoinAndResign',empJoinResignData,'Employee', this.openEmployeeJoinResignModalTable.bind(this));

  //   /*
  //    Chart Data for - Employee KYC by Department
  // */
  //   //console.log("departmentList : ", departmentList);
  //   // const CHECK_PERCENT = 100.00;
  //   // let kycChartData = [{
  //   //   name: 'Pending',
  //   //   data: [],
  //   //   stack: 'base'
  //   // }, {
  //   //   name: 'Completed',
  //   //   data: [],
  //   //   stack: 'base'
  //   // }];

  //   // let departmentCategories = employeeByDepartment.map(dept => {
  //   //   return [dept.departmentName]
  //   // });

  //   // let departmentKycData = Object.entries(departmentList).map(entry => {
  //   //   //console.log("entry : ", entry);
  //   //   const name = entry[0];
  //   //   const employeeList: any = entry[1];

  //   //   let pendingCount = 0;
  //   //   let completedCount = 0;

  //   //   employeeList.forEach(employee => {
  //   //     if (employee.profileCompletedPercent == CHECK_PERCENT) {
  //   //       completedCount++;
  //   //     } else {
  //   //       pendingCount++;
  //   //     }
  //   //   });

  //   //   return {
  //   //     pending: pendingCount,
  //   //     completed: completedCount,
  //   //     departmentName: name
  //   //   }
  //   // });

  //   //console.log("departmentKycData : ", departmentKycData);
  //   // departmentKycData.forEach(dept => {
  //   //   kycChartData[0].data.push(dept.pending);
  //   //   kycChartData[1].data.push(dept.completed);
  //   // });

  //   //console.log("kycChartData : ", kycChartData);

  //   // this.renderStackBarChart('Employee KYC Summary', 'employeeKycSummary', kycChartData, departmentCategories, 'Employee', this.openDepartmentWiseEmployeeKycModalTable.bind(this))


  //   //  bar for billable type employee

  //   //  console.log("departmentList: ", departmentList);

  //   const BILLABLE_TYPES = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];

  //   console.log("employeeByDepartment" + employeeByDepartment);
  //   console.log("departmentList (input):", departmentList);
  //   // Explicitly type departmentCategoriesforbilabale
  //   let departmentCategoriesforbilabale: string[][] = employeeByDepartment.map(dept => [dept.departmentName]);

  //   console.log("departmentCategoriesforbilabale: ", departmentCategoriesforbilabale);

  //   // Initialize billableChartData structure
  //   let billableChartData = BILLABLE_TYPES.map(billableType => ({
  //     name: billableType,
  //     data: [],
  //     stack: 'base'
  //   }));
  //   console.log("billableChartData: ", billableChartData);

  //   // Ensure TypeScript recognizes departmentList correctly
  //   if (typeof departmentList === 'object' && departmentList !== null) {
  //     // Calculate billable counts for each department
  //     let departmentBillableData = Object.entries(departmentList as Record<string, any>).map(entry => {
  //       const name = entry[0];
  //       const employeeList = entry[1] as any[]; // Assuming employeeList is an array of objects

  //       // Initialize billableCounts object for each department
  //       const billableCounts: Record<string, number> = {};
  //       BILLABLE_TYPES.forEach(billableType => {
  //         billableCounts[billableType] = 0;
  //       });

  //       // Count billable types for each employee in the department
  //       employeeList.forEach(employee => {
  //         billableCounts[employee.billableType]++;
  //       });

  //       console.log("billableCounts: ", billableCounts);

  //       // Convert billableCounts object to array
  //       const billableCountsArray = BILLABLE_TYPES.map(billableType => billableCounts[billableType]);

  //       return {
  //         billableCounts: billableCountsArray,
  //         departmentName: name
  //       };
  //     });


  //     // Sort departmentBillableData based on the sum of billableCounts in descending order
  //     departmentBillableData.sort((dept1, dept2) => {
  //       const sum1 = dept1.billableCounts.reduce((acc, val) => acc + val, 0);
  //       const sum2 = dept2.billableCounts.reduce((acc, val) => acc + val, 0);
  //       return sum2 - sum1; // Sort in descending order
  //     });
  //     console.log("departmentBillableData after sorting: ", departmentBillableData);
  //     // console.log("departmentBillableData: ", departmentBillableData);

  //     // Update billableChartData with sorted data
  //     departmentBillableData.forEach(dept => {
  //       dept.billableCounts.forEach((count, index) => {
  //         billableChartData[index].data.push(count);
  //       });
  //     });
  //     console.log("billableChartData after update: ", billableChartData);
  //     // Update departmentCategoriesforbilabale with sorted department names
  //     departmentCategoriesforbilabale = departmentBillableData.map(dept => [dept.departmentName]);
  //     console.log("departmentCategoriesforbilabale after update: ", departmentCategoriesforbilabale);

  //     // console.log("billableChartData: ", billableChartData);

  //     // Render the chart with sorted data
  //     // this.renderStackBarChart(
  //     //   'Employee Billable/Non-Billable Summary',
  //     //   'billableEmployeeSummary',
  //     //   billableChartData,
  //     //   this.departmentWiseEmployeeCategories,
  //     //   'Employee',
  //     //   this.openDepartmentWiseBillableEmployeeModalTable.bind(this)
  //     // );
  //   } else {
  //     console.error("departmentList is not in expected format.");
  //   }


  // }

  groupBy(objectArray, property) {
    return objectArray.reduce((acc, obj) => {
      const key = obj[property];
      if (!acc[key]) {
        acc[key] = [];
      }
      // Add object to list for given key's value
      acc[key].push(obj);
      return acc;
    }, {});
  }

  multipleGroupByArray(dataArray, groupPropertyArray) {
    const groups = {};
    dataArray.forEach(item => {
      const group = JSON.stringify(groupPropertyArray(item));
      groups[group] = groups[group] || [];
      groups[group].push(item);
    });
    return Object.keys(groups).map(function (group) {
      return groups[group];
    });
  }

  getAge(dateString) {
    var today = new Date();
    var birthDate = new Date(dateString);
    var age = today.getFullYear() - birthDate.getFullYear();
    var m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  totalExperience(dateOfJoining, workExperience) {
    var today = new Date().getTime();
    var joiningDate = new Date(dateOfJoining).getTime();

    let difference = (today - joiningDate);
    var experienceInApmosys = difference / (1000 * 60 * 60 * 24 * 365);
    var employeeTotalExperience = experienceInApmosys + workExperience;
    return employeeTotalExperience;
  }

  // leave / Timesheet Summary Dashboard Chart

  renderPieSummaryChart(chartName: any, chartId: any, chartData: any, labelName: any, openMod: any) {
    let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];

    if (chartId == "genderSummary") {
      colors = ['#88D2B8', '#D288A2', '#F33323'];
    }

    if (chartId == 'billableChartByDepartment') {
      colors = ['#DDDF00', '#64E572', '#ED561B', '#88D2B8', '#D288A2']
    }

    HighCharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }

      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      accessibility: {
        point: {
          valueSuffix: '%'
        }
      },
      exporting: {
        enabled: true,
        buttons: {
          contextButton: {
            menuItems: [//"printChart",
              "viewFullscreen",
              "downloadPNG",
              "downloadJPEG",
              "downloadCSV",
              "downloadXLS",
              "downloadPDFDocument"
            ]
          }
        }
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          events: {
            click: function (event) {
              if (chartId == 'leaveSummaryChart') {
                openMod(event.point.name);
              }
              if (chartId == 'timesheetStatusSummary') {
                openMod(event.point.name);
              }
              if (chartId == 'employeeStatus') {
                openMod(event.point.name);
              }
              if (chartId == 'genderSummary') {
                openMod(event.point.name);
              }
              if (chartId == 'employeeAgeSummary') {
                openMod(event.point.name);
              } if (chartId == 'fresherLateralChart') {
                openMod(event.point.name);
              } if (chartId == 'billableChart') {
                openMod(event.point.name);
              } if (chartId == 'billableChartByDepartment') {
                openMod(event.point.name);
              }
            },
          },
          dataLabels: {
            enabled: true,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          },
          showInLegend: true
        }
      },
      legend: {
        enabled: true,
        labelFormatter: function () {
          const point: any = this;
          return this.name + ` : ${point.y}`;
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
      colors: colors
    });
  }

  // sortDepartmentsByEmployeeCount(departments: any[]): any[] {
  //   return departments.sort((a, b) => b.employeeCount - a.employeeCount);
  // }


  // Employee Summary Dashboard
  // Employee Summary Dashboard
  renderColumnBarSummaryChart(chartName: any, chartId: any, chartData: any, categories: any, labelName: any, openMod: any) {



    if (chartId == 'employeeExperienceSummary') {

      var labels = ['0 to 1', '1 to 2', '2 to 5', '5 to 10', '10+'];
      this.options = {
        chart: {
          type: 'cylinder',
          options3d: {
            enabled: true,
            alpha: 20,
            beta: 15,
            depth: 50,
            viewDistance: 30
          }
        },
        title: {
          text: chartName,
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '20'
          },
        },
        credits: {
          enabled: false,
        },
        plotOptions: {
          series: {
            depth: 25,
            colorByPoint: true,
            cursor: 'pointer',
            point: {
              events: {
                click: function (event) {
                  if (chartId == 'employeeExperienceSummary') {
                    openMod(event.point.name);
                  }
                }
              },
            },
          }


        },
        xAxis: {
          opposite: false,
          labels: {
            overflow: 'justify',
            style: {
              fontWeight: 'bold',
              color: '#000000',
              fontSize: '12'
            },
            formatter: function () {
              return labels[this.pos]
            }
          }
        }, yAxis: {
          min: 0,
          title: {
            text: 'No. Of Employees',
            align: 'high',
            style: {
              fontWeight: 'bold',
              color: '#000000',
            }
          }, labels: {
            overflow: 'justify',
            style: {
              fontWeight: 'bold',
              color: '#000000',
              fontSize: '12'
            }
          },
        },

        series: [{
          data: chartData,
          name: 'No. Of Employees',
          showInLegend: false
        }]
      };

      Highcharts.chart(chartId, this.options);

    } else {
      HighCharts.chart(chartId, {
        chart: {
          type: 'column',
        },
        title: {
          text: chartName,
          style: {
            fontWeight: 'bold',
            color: '#000000'
          }
        },
        xAxis: {
          categories: categories,
          labels: {
            overflow: 'justify',
            style: {
              fontWeight: 'bold',
              color: '#000000',
              fontSize: '12'
            }
          },
        },
        yAxis: {
          min: 0,
          title: {
            text: 'No. Of Employees',
            align: 'high',
            style: {
              fontWeight: 'bold',
              color: '#000000',
            }
          },
          labels: {
            overflow: 'justify',
            style: {
              fontWeight: 'bold',
              color: '#000000',
              fontSize: '12'
            }
          },
        },
        tooltip: {
          valuePrefix: 'No. ',
        },
        plotOptions: {
          series: {
            cursor: 'pointer',
            point: {
              events: {
                click: function (event) {
                  if (chartId == 'employeeExperienceSummary') {
                    openMod(event.point.name);
                  }
                  if (chartId == 'departmentWiseEmployee') {
                    openMod(event.point.name);
                  }
                  if (chartId == 'employeeWorkLocationSummary') {
                    openMod(event.point.name);
                  }
                }
              },
            },
          },
          bar: {
            dataLabels: {
              enabled: true,
            },
            showInLegend: true
          },
        },
        credits: {
          enabled: false,
        },
        legend: {
          enabled: false
        },
        series: [
          {
            type: 'column',
            name: labelName,
            data: chartData
          },
        ],
      });
    }
  }
  //sandeep

  // renderDepartmentWiseEmployeeChart(chartName:any, chartId:any, chartData:any, categories:any, labelName:any, openMod:any) {
  //   const employeeCounts = chartData.map((dept) => dept.data[0]); // Employee counts
  //   const apprenticeCounts = chartData.map((dept) => dept.data[1]); // Apprentice counts

  //   console.log('Id of chart --',chartId);

  //   // Render the Highcharts column chart
  //   // var chart = Highcharts.chart(chartId, {
  //   //@ts-ignore
  //     // var chart = Highcharts.chart(chartId, {

  //     // this.options = {
  //   //  HighCharts.chart(chartId, {
  //     (Highcharts as any).chart(chartId, {
  //       chart: { type: 'column' },

  //     // chart: {
  //     //   type: 'column', // Column chart type
  //     // },
  //     title: {
  //       text: chartName,
  //       style: {
  //         fontWeight: 'bold',
  //         color: '#000000',
  //       },
  //     },
  //     xAxis: {
  //       categories: categories, // X-axis labels (department names)
  //       labels: {
  //         overflow: 'justify',
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //           fontSize: '12px',
  //         },
  //       },
  //     },
  //     yAxis: {
  //       min: 0,
  //       title: {
  //         text: 'No. Of Employees', // Y-axis title
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //         },
  //       },
  //     },
  //     tooltip: {
  //       shared: true, // Combine tooltips for stacked columns
  //       valueSuffix: ' employees',
  //     },
  //     plotOptions: {
  //       column: {
  //         stacking: 'normal', // Stack employee and apprentice counts
  //         dataLabels: {
  //           enabled: true, // Show data labels on columns
  //         },
  //       },
  //       series: {
  //         cursor: 'pointer',
  //         point: {
  //           events: {
  //             click: function (event) {
  //               openMod(event.point.category); // Pass the clicked department name to the callback
  //             },
  //           },
  //         },
  //       },
  //     },
  //     credits: {
  //       enabled: false, // Disable the Highcharts watermark
  //     },
  //     legend: {
  //       enabled: true, // Enable legend to differentiate series
  //     },
  //     series: [
  //       {
  //         name: 'Employee Count',
  //         data: employeeCounts, // Employee counts for each department
  //         color: '#3498db', // Blue color for employees
  //       },
  //       {
  //         name: 'Apprentice Count',
  //         data: apprenticeCounts, // Apprentice counts for each department
  //         color: '#2ecc71', // Green color for apprentices
  //       },
  //     ],
  //   });
  // }
  renderDepartmentWiseEmployeeChart(chartName: any, chartId: any, chartData: any, categories: any, labelName: any, openMod: any) {
    const employeeCounts = chartData.map((dept: any) => dept.data[0]);
    const apprenticeCounts = chartData.map((dept: any) => dept.data[1]);
    const consultantCounts = chartData.map((dept: any) => dept.data[2]);

    (Highcharts as any).chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000',
        },
      },
      xAxis: {
        categories: categories,
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '12px',
          },
        },
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          style: {
            fontWeight: 'bold',
            color: '#000000',
          },
        },
      },
      tooltip: {
        shared: true,
        valueSuffix: ' employees',
      },
      plotOptions: {
        column: {
          stacking: 'normal',
          dataLabels: {
            enabled: true,
          },
        },
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function (event: any) {
                const departmentName = event.point.category;
                const seriesName = this.series.name;
                openMod(departmentName, seriesName);
              },
            },
          },
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: true,
      },
      series: [
        {
          name: 'Employee Count',
          data: employeeCounts,
          color: '#e74c3c',
        },
        {
          name: 'Apprentice Count',
          data: apprenticeCounts,
          color: '#2ecc71',
        },
        {
          name: 'Consultant Count',
          data: consultantCounts,
          color: '#2234bd',
        },
      ],
    });
  }

  // plotEmployeeExperienceGraph(chartName, chartId, categories, employeeSeries, apprenticeSeries, openMod) {
  //   (Highcharts as any).chart(chartId, {
  //     chart: {
  //       type: 'cylinder', // Stacked column chart
  //     },
  //     title: {
  //       text: chartName,
  //       style: {
  //         fontWeight: 'bold',
  //         color: '#000000',
  //       },
  //     },
  //     xAxis: {
  //       categories: categories, // Experience ranges (e.g., "0 to 1", "1 to 2", etc.)
  //       labels: {
  //         overflow: 'justify',
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //           fontSize: '12px',
  //         },
  //       },
  //     },
  //     yAxis: {
  //       min: 0,
  //       title: {
  //         text: 'No. Of Employees',
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //         },
  //       },
  //     },
  //     tooltip: {
  //       shared: true, // Combine tooltips for stacked columns
  //       valueSuffix: ' employees',
  //     },
  //     plotOptions: {
  //       column: {
  //         stacking: 'normal', // Stack employee and apprentice counts
  //         dataLabels: {
  //           enabled: true, // Show data labels on columns
  //         },
  //       },
  //       series: {
  //         cursor: 'pointer',
  //         point: {
  //           events: {
  //             click: function (event) {
  //               openMod(event.point.category); // Pass clicked category to callback
  //             },
  //           },
  //         },
  //       },
  //     },
  //     credits: {
  //       enabled: false, // Disable Highcharts watermark
  //     },
  //     legend: {
  //       enabled: true, // Enable legend to differentiate series
  //     },
  //     series: [
  //       {
  //         name: 'Employees',
  //         data: employeeSeries, // Employee counts for each experience range
  //         color: '#3498db', // Blue color for employees
  //       },
  //       {
  //         name: 'Apprentices',
  //         data: apprenticeSeries, // Apprentice counts for each experience range
  //         color: '#2ecc71', // Green color for apprentices
  //       },
  //     ],
  //   });
  // }
  // plotEmployeeExperienceCylinderGraph(chartName, chartId, categories, employeeSeries, apprenticeSeries,consultantSeries, openMod) {
  //  (Highcharts as any).chart(chartId, {
  //   chart: {
  //       type: 'cylinder', // Cylinder chart type
  //       options3d: {
  //         enabled: true,
  //         alpha: 15,
  //         beta: 15,
  //         depth: 50,
  //         viewDistance: 25,
  //       },
  //     },
  //     title: {
  //       text: chartName,
  //       style: {
  //         fontWeight: 'bold',
  //         color: '#000000',
  //       },
  //     },
  //     xAxis: {
  //       categories: categories, // Experience ranges (e.g., "0 to 1", "1 to 2", etc.)
  //       labels: {
  //         overflow: 'justify',
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //           fontSize: '12px',
  //         },
  //       },
  //     },
  //     yAxis: {
  //       min: 0,
  //       title: {
  //         text: 'No. Of Employees',
  //         style: {
  //           fontWeight: 'bold',
  //           color: '#000000',
  //         },
  //       },
  //     },
  //     tooltip: {
  //       shared: true,
  //       valueSuffix: ' employees',
  //     },
  //     plotOptions: {
  //       cylinder: {
  //         stacking: 'normal', // Stack employee and apprentice counts
  //         depth: 25, // 3D depth for the cylinders
  //         dataLabels: {
  //           enabled: true, // Show data labels on cylinders
  //         },
  //       },
  //       series: {
  //         cursor: 'pointer',
  //         point: {
  //           events: {
  //             click: function (event) {
  //               const clickedCategory = event.point.category;
  //               const clickedSeries = event.point.series.name;

  //               if (clickedSeries === 'Employees') {
  //                 openMod(clickedCategory, 'employee');
  //               } else if (clickedSeries === 'Apprentices') {
  //                 openMod(clickedCategory, 'apprentice'); 
  //               }else if (clickedSeries === 'Consultant') {
  //                 openMod(clickedCategory, 'consultant'); 
  //               }
  //             },
  //           },
  //         },
  //       },
  //     },
  //     credits: {
  //       enabled: false,
  //     },
  //     legend: {
  //       enabled: true,
  //     },
  //     series: [
  //       {
  //         name: 'Employees',
  //         data: employeeSeries, 
  //         color: '#3498db', 
  //       },
  //       {
  //         name: 'Apprentices',
  //         data: apprenticeSeries, 
  //         color: '#e74c3c', 
  //       },
  //       {
  //         name: 'Consultant',
  //         data: consultantSeries,
  //         color: '#2d9687',
  //       },
  //     ],
  //   });
  // }
  //sandeep
  plotEmployeeExperienceCylinderGraph(chartName, chartId, categories, employeeSeries, apprenticeSeries, consultantSeries, openMod) {
    (Highcharts as any).chart(chartId, {
      chart: {
        type: 'cylinder',
        // options3d: {
        //   enabled: true,
        //   alpha: 25,
        //   beta: 20,
        //   depth: 60,
        //   viewDistance: 40,
        // },
        backgroundColor: '#f4f6f7',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#34495e',
          fontSize: '18px',
        },
      },
      xAxis: {
        categories: categories,
        labels: {
          style: {
            fontWeight: 'bold',
            color: '#34495e',
            fontSize: '14px',
          },
        },
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          style: {
            fontWeight: 'bold',
            color: '#34495e',
          },
        },
      },
      tooltip: {
        shared: true,
        valueSuffix: ' employees',
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        style: {
          color: '#ffffff',
          fontSize: '14px',
        },
        // formatter: function () {
        //   return <b>${this.point.category}</b><br>${this.series.name}: <b>${this.y}</b> employees;
        // },
      },
      plotOptions: {
        cylinder: {
          stacking: 'normal',
          depth: 30,
          dataLabels: {
            enabled: true,
            color: '#ffffff',
            style: {
              fontWeight: 'bold',
              textOutline: 'none',
            },
            formatter: function () {
              return this.y;
            },
          },
          colorByPoint: true,
        },
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function (event) {
                const clickedCategory = event.point.category;
                const clickedSeries = event.point.series.name;

                if (clickedSeries === 'Employees') {
                  openMod(clickedCategory, 'employee');
                } else if (clickedSeries === 'Apprentices') {
                  openMod(clickedCategory, 'apprentice');
                } else if (clickedSeries === 'Consultant') {
                  openMod(clickedCategory, 'consultant');
                }
              },
            },
          },
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: true,
        itemStyle: {
          fontWeight: 'bold',
          fontSize: '14px',
          color: '#34495e',
        },
        symbolHeight: 12,
        symbolWidth: 12,
        symbolRadius: 3,
      },
      series: [
        {
          name: 'Employees',
          data: employeeSeries,
          color: 'url(#gradEmployee)',
        },
        {
          name: 'Apprentices',
          data: apprenticeSeries,
          color: 'url(#gradApprentice)',
        },
        {
          name: 'Consultant',
          data: consultantSeries,
          color: 'url(#gradConsultant)',
        },
      ],
      defs: {
        gradients: [
          {
            id: 'gradEmployee',
            stops: [
              [0, '#3498db'],
              [1, '#2980b9'],
            ],
          },
          {
            id: 'gradApprentice',
            stops: [
              [0, '#e74c3c'],
              [1, '#c0392b'],
            ],
          },
          {
            id: 'gradConsultant',
            stops: [
              [0, '#2d9687'],
              [1, '#16a085'],
            ],
          },
        ],
      },
    });
  }


  // added by anurag 

  renderColumnBarSummaryChartForWorkLocation(chartName: any, chartId: any, chartData: any, categories: any, labelName: any, openMod: any) {

    HighCharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      xAxis: {
        categories: categories,
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '12'
          }
        },
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. of Timesheets Filled at Work Location',
          align: 'high',
          style: {
            fontWeight: 'bold',
            color: '#000000',
          }
        },
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '12'
          }
        },
      },
      tooltip: {
        valuePrefix: 'No. ',
      },
      plotOptions: {
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function (event) {
                if (chartId == 'employeeExperienceSummary') {
                  openMod(event.point.name);
                }
                // if(chartId == 'departmentWiseEmployee'){
                //   openMod(event.point.name);
                // }
                if (chartId == 'employeeWorkLocationSummary') {
                  openMod(event.point.name);
                }
              }
            },
          },
        },
        bar: {
          dataLabels: {
            enabled: true,
          },
          showInLegend: true
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: false
      },
      series: [
        {
          type: 'column',
          name: labelName,
          data: chartData
        },
      ],
    });
  }



  renderMultiBarChart(chartName: any, chartId: any, chartData: any, labelName: any, openMod: any) {

    HighCharts.chart(chartId, {
      //   //   chart: {
      //   //     type: 'column',
      //   //   },
      //   //   title: {
      //   //     text: chartName,
      //   //     style:{
      //   //       fontWeight: 'bold',
      //   //       color: '#000000'
      //   //     }
      //   //   },
      //   //   xAxis: {
      //   //     categories: [
      //   //       "Jan",
      //   //       "Feb",
      //   //       "Mar",
      //   //       "Apr",
      //   //       "May",
      //   //       "Jun",
      //   //       "Jul",
      //   //       "Aug",
      //   //       "Sep",
      //   //       "Oct",
      //   //       "Nov",
      //   //       "Dec"
      //   //     ],
      //   //     labels:{
      //   //       style:{
      //   //         fontWeight: 'bold',
      //   //         color: '#000000'
      //   //       }
      //   //     }
      //   //   },
      //   //   yAxis: {
      //   //     min: 0,
      //   //     title: {
      //   //       text: 'No. Of Employees',
      //   //       align: 'high',
      //   //       style:{
      //   //         fontWeight: 'bold',
      //   //         color: '#000000'
      //   //       }
      //   //     },
      //   //     labels: {
      //   //       overflow: 'justify',
      //   //       style:{
      //   //         fontWeight: 'bold',
      //   //         color: '#000000'
      //   //       }
      //   //     },
      //   //   },
      //   //   tooltip: {
      //   //     valuePrefix: 'No. ',
      //   //   },
      //   //   plotOptions: {
      //   //     series: {
      //   //       cursor: 'pointer',
      //   //       point: {
      //   //         events: {
      //   //           click: function(event) {
      //   //             if(chartId == 'employeeJoinAndResign'){
      //   //               openMod(event.point.category, event.point.series.name);
      //   //             }
      //   //           }
      //   //         },
      //   //       },
      //   //     },
      //   //     bar: {
      //   //       dataLabels: {
      //   //         enabled: true,
      //   //       },
      //   //       showInLegend: true
      //   //     },
      //   //   },
      //   //   credits: {
      //   //     enabled: false,
      //   //   },
      //   //   legend: {
      //   //     enabled: true
      //   //   },
      //   //   series: chartData
      //   // });
      //   Highcharts.chart('employeeJoinAndResign', {
      //     chart: {
      //       type: 'column'
      //     },
      //     title: {
      //       text: 'Employee Join VS Resign'
      //     },
      //     xAxis: {
      //       categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      //     },
      //     yAxis: {
      //       min: 0,
      //       title: {
      //         text: 'Number of Employees'
      //       },
      //       stackLabels: {
      //         enabled: true
      //       }
      //     },
      //     plotOptions: {
      //       column: {
      //         stacking: 'normal'
      //       }
      //     },
      //     // series: finalEmpJoinResignData // Pass the formatted data
      //     series: chartData // Pass the formatted data

      //   });

      // }
      //renderMultiBarChart(chartName, chartId, chartData, labelName, openMod) {
      //Highcharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000',
        }
      },
      xAxis: {
        categories: [
          'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December', // Months as categories
        ],
        labels: {
          style: {
            fontWeight: 'bold',
            color: '#000000',
          }
        }
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          align: 'high',
          style: {
            fontWeight: 'bold',
            color: '#000000',
          }
        },
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
          }
        },
      },
      tooltip: {
        valuePrefix: 'No. ',
      },
      plotOptions: {
        series: {
          cursor: 'pointer',
          stacking: 'normal',  // Enable stacking for the 'Joined' categories
          point: {
            events: {
              click: function (event) {
                if (chartId === 'employeeJoinAndResign') {
                  openMod(event.point.category, event.point.series.name);
                }
              }
            }
          },
        },
        bar: {
          dataLabels: {
            enabled: true,
          },
          showInLegend: true,
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: true,
      },
      series: chartData,
    });
  }


  renderLineGraphChart(chartName: any, chartId: any, chartData: any, labelName: any, category: any, openMod: any) {
    HighCharts.chart(chartId, {
      title: {
        text: chartName,
        style: {
          color: '#000000',
          fontWeight: 'bold'
        }
      },
      yAxis: {
        title: {
          text: 'Number of Leaves',
          style: {
            color: '#000000',
            fontWeight: 'bold'
          }
        },
        labels: {
          overflow: 'justify',
          style: {
            color: '#000000',
            fontWeight: 'bold'
          }
        }
      },
      xAxis: {
        categories: category,
        labels: {
          overflow: 'justify',
          style: {
            color: '#000000',
            fontWeight: 'bold'
          }
        }
      },
      plotOptions: {
        series: {
          label: {
            connectorAllowed: false
          },
          point: {
            events: {
              click: function (event) {
                if (chartId == 'leaveTrendAnalysis') {
                  openMod(event.point.series.name, this.category);
                }
              }
            },
          },
        }
      },
      credits: {
        enabled: false,
      },
      series: chartData
    });
  }

  renderStackBarChart(chartName: any, chartId: any, chartData: any, categories: any, labelName: any, openMod: any) {

    let colors = ['#ED561B', '#64E572'];

    if (chartId == "billableEmployeeSummary") {
      colors = ['#DDDF00', '#64E572', '#ED561B', '#88D2B8', '#D288A2']
    }

    HighCharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      xAxis: {
        categories: categories,
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '12'
          }
        },
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          align: 'high',
          style: {
            fontWeight: 'bold',
            color: '#000000',
          }
        },
        labels: {
          overflow: 'justify',
          style: {
            fontWeight: 'bold',
            color: '#000000',
            fontSize: '12'
          }
        },
        tickInterval: 50,
        endOnTick: false
      },
      plotOptions: {
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function (event) {
                let category = event.point.category[0]
                let series = event.point.series.name;

                openMod(category, series);
              }
            },
          },
        },
        column: {
          stacking: 'normal',
          dataLabels: {
            enabled: true,
          },
          showInLegend: true
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: true
      },
      series: chartData,
      colors: colors
    });
  }

  renderPlaceholderChart(chartName: any, chartId: any) {
    HighCharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      accessibility: {
        point: {
          valueSuffix: '%'
        }
      },
      plotOptions: {
        series: {
          enableMouseTracking: false
        },
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: true,
            format: '<h4>No data to display<h4>'
          },
          showInLegend: false
        }
      },
      series: [{
        name: "error",
        colorByPoint: true,
        type: undefined,
        data: [{ name: 'data', y: 1 }]
      }],
      colors:
        ['#b0b0b0'],
    });
  }

  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    //console.log("columns : ", columns);
    this.queryList = [];
    let dateFormat = 'DD-MM-YYYY';

    this.filterData.title = title;
    this.filterData.columns = columns;

    if (this.filterData.title == 'Filter Timesheet Summary' || this.filterData.title == 'Filter Leave Trend Chart') {

      let fromDate = moment().subtract(8, 'd').format(dateFormat);
      let toDate = moment().format(dateFormat);

      this.queryList = [
        { column: "From Date", operator: ">=", value: fromDate, conjunction: "AND" },
        { column: "To Date", operator: "<=", value: toDate, conjunction: "" }
      ];
    }

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }
          // if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-CS-") && (data.isConsultant == 'true')){
          //   queryObj.value = "A-CS-".concat(queryObj.value);
          // }
          // queryObj.employeeType =(queryObj.isConsultant === 'true') ? "Consultant" :((queryObj.isApprenticeship === 'true') ? "Apprentice" : "Regular");
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }
          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    //console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      //console.log("queryList : ", emittedArray[0]);
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          this.storedDataList.push(object);
        }
      });

      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }

        if (query.column == 'Employee Id') {
          query.value = query.value.split("-")[1];
        }
      });

      //console.log("updated queryList : ", emittedArray[0]);

      if (this.filterData.title == 'Filter Employee Report') {
        // this.getCustomEmployeesList(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Leave Summary') {
        this.getCustomLeaveReport(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Leave Trend Chart') {
        // this.getCustomLeaveTrendAnalysisReport(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Timesheet Summary') {
        this.getCustomTimesheetReport(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      if (emittedArray[1] == 'Filter Employee Report') {
        // this.getAllEmployeeList();
      }
      if (emittedArray[1] == 'Filter Leave Summary') {
        this.get8DaysLeaveReport();
      }
      if (emittedArray[1] == 'Filter Leave Trend Chart') {
        // this.getLeaveTrendAnalysisReport();
      }
      if (emittedArray[1] == 'Filter Timesheet Summary') {
        this.get9DayTimesheetReport();
      }
    }
  }

  // export excel

  exportToExcelLeaveSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": "A-".concat(x.employeementId),
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Name": x.employeeName,
        "Department Name": x.departmentName,
        "From Date": (x.fromDate) ? moment(x.fromDate).format(AppComponent.DATE_FORMAT) : null,
        "To Date": (x.toDate) ? moment(x.toDate).format(AppComponent.DATE_FORMAT) : null,
        "From Date Day Type": x.fromDateDayType,
        "To Date Day Type": x.toDateDayType,
        "Status": x.status
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

  exportToExcelTimesheetSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": "A-".concat(x.employeementId),
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Name": x.employeeName,
        "Department Name": x.departmentName,
        "Timesheet Date": (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null,
        "Day Type": x.dayType,
        "Email Id": x.email,
        "Manager Name": x.managerName,
        "Mobile No.": x.mobileNo,
        "Pending EOD Count": x.pendingEodCount,
        "Total Working Hour": x.totalWorkingHours,
        "Type": x.legend
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"));
  }

  exportToExcelEODSegregation(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": "A-".concat(x.employeementId),
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Name": x.employeeName,
        "Department Name": x.departmentName,
        "Email Id": x.email,
        "Manager Name": x.managerName,
        "Mobile No.": x.mobileNo,
        "Timesheet Date": (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null,
        "Day Type": x.dayType,
        "Total Working Hours": x.totalWorkingHours
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"));
  }

  exportToExcelEmployeeSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": x.employeementId,
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Name": x.name,
        "Department Name": x.departmentName,
        "Experience": x.experience,
        "Email Id": x.email,
        "Date Of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
        "Manager Name": x.managerName,
        "Billable": x.billable,
        "Billable Type": x.billableType,
        "Project Name": x.projectName,
        "Client Name": x.clientName,
        "Team Name": x.teamName,
        "Mobile No.": x.mobileNo,
        "Status": x.employmentstatus,
        "Total Experience": x.totalExperience,
        "Gender": x.gender,
        "Work Location": x.workLocation,
        "Age": x.age,
        "KYC Status": x.profileKycStatus
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

  exportToExcelWorkLocationSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": x.employeementId,
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Employee Name": x.employeeName,
        "Project Name": x.projectName,
        "Client Name": x.clientName,
        "Team Name": x.teamName,
        "Client Location": x.clientLocation,
        "Working Date": (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

  // export global data
  name = "EmployeeReport.xlsx"
  exportGlobalData(): void {
    const onlySpecificDataArr = this.allEmployeeList.map(
      x => ({
        "Emp ID": x.employeementId,
        "Employee Type": x.employeeType,
        "Employee Name": x.name,
        "Department Name": x.departmentName,
        "Date Of Resign": (x.dateOfResign) ? moment(x.dateOfResign).format(AppComponent.DATE_FORMAT) : null,
        "Date Of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
        "Reporting To": x.managerName,
        "Experience": x.experience,
        "Email Id": x.email,
        "Date Of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
        "Manager Name": x.managerName,
        "Billable": x.billable,
        "Billable Type": x.billableType,
        "Mobile No.": x.mobileNo,
        "Status": x.employmentstatus,
        "Total Experience": x.totalExperience,
        "Gender": x.gender,
        "Age": x.age,
        "KYC Status": x.profileKycStatus,
        "Project Name": x.projectName,
        "Client Name": x.clientName,
        "Team Name": x.teamName,
        "Client Location": x.clientLocation,
        "Working Date": (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }






  // allResignEmployee

  exportToExcelResignedEmployee(): void {
    const onlySpecificDataArr = this.allResignEmployee.map(
      x => ({
        "Emp ID": x.employeementId,
        // "Emp ID": (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId),
        "Employee Type": ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
        "Employee Name": x.name,
        "Department": x.departmentName,
        "Date Of Resign": (x.dateOfResign) ? moment(x.dateOfResign).format(AppComponent.DATE_FORMAT) : null,
        "Date Of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
        "Reporting To": x.managerName
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, "Resigned Employee".concat(".xlsx"))
  }

  //Pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // Models

  openEodSegregation(template: TemplateRef<any>, titleName: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    let modalTableList = this.timsheetSummaryList;
    this.modalSummaryList = [];
    this.resetSearch();

    if (titleName == "Employee Worked Between 0 to 5 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours >= 0 && x.totalWorkingHours <= 5);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 5 to 8 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 5 && x.totalWorkingHours <= 8);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 8 to 9 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 8 && x.totalWorkingHours <= 9);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 9 to 10 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 9 && x.totalWorkingHours <= 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Employee Worked More than 10 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "No Timesheet Submitted") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User" && x.pendingEodCount > 0);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Holiday") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Holiday");
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if (titleName == "Working On Holiday") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Non-working");
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
  }

  openLeaveSummaryTableModel(statusName: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    let modalTableList = this.uniqueLeaveSumarryList;
    this.modalSummaryList = [];
    this.resetSearch();

    this.data = ''
    this.page = 1;
    this.modalTitle = statusName + " Leave Summary";
    this.modalSummaryList = modalTableList.filter(x => x.status == statusName);
    this.modalSummaryList.forEach(x => {
      x.employeeType = ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular'))
    });
    this.modalSummaryList.forEach(y => {
      y.emp360 = y.empId;
      y.emp360Manager = y.managerId;
    });
    console.log('modalSummaryList --', this.modalSummaryList)
    this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
  }

  openTimesheetSummaryTableModel(legendName: any) {
    let modalTableList = this.timsheetSummaryList;
    this.modalSummaryList = [];
    this.countByLegend = [];
    this.resetSearch();
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = ''
    this.page = 1;
    this.modalTitle = legendName + " Timesheet Summary";
    this.modalSummaryList = modalTableList.filter(x => x.legend == legendName);
    console.log(this.modalSummaryList, "this.modalSummaryListttttttttttttt")
    //console.log(this.countByLegend);
    this.modalSummaryList.forEach(x => {
      if (!this.countByLegend.find(employee => employee.employeementId == x.employeementId)) {
        this.countByLegend.push({
          employeementId: x.employeementId,
          employeeType: ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant' : 'Regular')),
          employeeName: x.employeeName,
          departmentName: x.departmentName,
          email: x.email,
          mobileNo: x.mobileNo,
          managerName: x.managerName,
          pendingEodCount: x.pendingEodCount,
          legend: x.legend,
          isConsultant: x.isConsultant,
          count: this.modalSummaryList.filter(y => y.employeementId == x.employeementId).length
        });
      }
    });
    // console.log(this.countByLegend,"modalSummaryList");
    this.modalSummaryList = this.countByLegend;
    // let employee360 = this.employeesFor360;
    // for(let x of employee360){
    //   x.employeementId = Number(x.employeementId.substring(2));
    // }
    for (let y of this.modalSummaryList) {
      y.emp360 = y.empId;
      y.emp360Manager = y.managerId;
    }
    // console.log(this.modalSummaryList, "this.checked")
    this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
    if (legendName == 'Pending By User') {
      this.isPendingByUser = true;
    } else {
      this.isPendingByUser = false;
    }
  }

  openDepartmentWiseBillableEmployeeTableModal(department: any) {
    console.log("Hii, billable modal call", department)
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();

    // let modalTableList = this.departmentWiseBillableEmployeeList.filter(x => x.employmentstatus != 'InActive' && x.billableType == department);
    //     let modalTableList = this.departmentWiseBillableEmployeeList.filter(
    //   x =>
    //     x.employmentstatus != 'InActive' &&
    //     x.isConsultant != 'true' &&
    //     x.isApprenticeship != 'true' &&
    //     x.billableType == department
    // );

    let modalTableList;
    if (department == "TNM") {
      modalTableList = this.departmentWiseBillableEmployeeList.filter(
        x =>
          x.employmentstatus != 'InActive' &&
          x.isConsultant != 'true' &&
          x.isApprenticeship != 'true' &&
          x.billableType == department
      );
    }
    else {
      modalTableList = this.departmentWiseBillableEmployeeList.filter(
        x =>
          x.employmentstatus != 'InActive' &&
          x.billableType == department
      );
    }
    // Log the count and a sample of the filtered data
    console.log(`Filtered count for department "${department}":`, modalTableList.length);
    if (modalTableList.length > 0) {
      console.log("Sample filtered employee:", modalTableList[0]);
    }
    this.page = 1;
    // this.modalTitle = department+" wise Billable Employee" ;
    if (department == "TNM")
      this.modalTitle = department + " wise Billable Employee";
    else
      this.modalTitle = department + " wise Non-Billable Employee";
    this.modalSummaryList = modalTableList;
    console.log("this.modalSummaryList   anurag ", this.modalSummaryList)
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openBillableEmployeeTableModal(billable: any) {
    console.log("Hii, billable modal call", billable)
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();

    let modalTableList = null;
    if (billable != "Other") {
      modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive' && x.billable == billable && x.billableType != null);
    } else {
      modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive' && (x.billable == 'Yes' || x.billable == 'No' || x.billable == null) && x.billableType == null);
    }

    // Log employees whose department is 'Traing' and billableType is 'Bench'
    const traingBenchEmployees = modalTableList.filter(
      x => x.departmentName === 'Traing' && x.billableType === 'Bench'
    );
    console.log('Employees in Traing department with Bench billableType:', traingBenchEmployees.length, traingBenchEmployees);

    this.page = 1;
    this.modalTitle = "Employee In " + billable;
    this.modalSummaryList = modalTableList;
    console.log("modelsheet" + " " + this.modalSummaryList);

    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openTotalCountModal(title: any) {
    // const checkKyc = 100.00;
    // let status = null;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();

    let dateToday = moment().format(this.dateFormat);
    let modalTableList = this.allEmployeeList;
    this.page = 1;
    this.modalTitle = title;

    if (title == 'All Active Employee') {
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus != "InActive");
      // this.modalSummaryList.forEach(obj =>{

      //   if(obj.profileCompletedPercent < checkKyc){
      //     status = "No";
      //     obj.profileCompletedPercent = status;
      //   }else{
      //     status = "Yes";
      //     obj.profileCompletedPercent = status;
      //   }
      // })
    } else if (title == 'Employee In Probation(After 6 months)') {
      this.modalSummaryList = modalTableList.filter(x => moment(dateToday).diff(moment(x.dateOfJoining), 'months', true) > 6 && x.employmentstatus == 'Probation');
    } else if (title == 'Apprentice Count') {
      this.modalSummaryList = modalTableList.filter(x => x.isApprenticeship === 'true' && x.employmentstatus != 'InActive');
    } else if (title == 'Consultant Count') {
      this.modalSummaryList = modalTableList.filter(x => x.isConsultant === 'true' && x.employmentstatus != 'InActive');
    } else if (title == 'Regular Count') {
      this.modalSummaryList = modalTableList.filter(x => x.isConsultant != 'true' && x.isConsultant != 'true' && x.employmentstatus != 'InActive');
    }
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeStatusTableModal(status: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();

    let modalTableList = this.allEmployeeList;
    this.page = 1;
    this.modalTitle = "Employee In " + status;
    this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == status);
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openGenderSummaryModalTable(gender: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();

    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    this.page = 1;
    this.modalTitle = gender + " Employee Data";
    this.modalSummaryList = modalTableList.filter(x => x.gender == gender);
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openAgeSummayModalTable(age: any) {
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    if (age == "18 to 25") {
      this.page = 1;
      this.modalTitle = "Employee Age Between 18 to 25";
      this.modalSummaryList = modalTableList.filter(x => x.age >= 18 && x.age <= 25);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if (age == "25 to 35") {
      this.page = 1;
      this.modalTitle = "Employee Age Between 25 to 35";
      this.modalSummaryList = modalTableList.filter(x => x.age > 25 && x.age <= 35);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if (age == "35 to 45") {
      this.page = 1;
      this.modalTitle = "Employee Age Between 35 to 45";
      this.modalSummaryList = modalTableList.filter(x => x.age > 35 && x.age <= 45);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if (age == "45+") {
      this.page = 1;
      this.modalTitle = "Employee Age Above 45";
      this.modalSummaryList = modalTableList.filter(x => x.age > 45);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openDepartmentWiseEmployeeModalTable(pointName: any, seriesName: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = '';
    this.modalSummaryList = [];
    this.resetSearch();

    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus !== 'InActive');

    console.log("Filtered Active Employees:", modalTableList);
    console.log("Clicked Department:", pointName);
    console.log("Clicked Series:", seriesName);

    this.page = 1;
    this.modalTitle = seriesName + " in " + pointName;

    this.modalSummaryList = modalTableList.filter(x =>
      x.departmentName === pointName &&
      (
        (seriesName === 'Employee Count' && x.isApprenticeship != 'true' && x.isConsultant != 'true' && x.isApprenticeship != 'true') ||
        (seriesName === 'Apprentice Count' && x.isApprenticeship === 'true' && x.isConsultant != 'true') ||
        (seriesName === 'Consultant Count' && x.isConsultant === 'true' && x.isApprenticeship != 'true')
      )
    );


    console.log("Final Data for Modal:", this.modalSummaryList);

    this.modalSummaryList.forEach((dept) => {
      dept.dateOfJoining = dept.dateOfJoining ? moment(dept.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
    });

    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeExperienceModalTable(pointName: any, type: string) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = '';
    this.modalSummaryList = [];
    this.resetSearch();

    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');

    let filteredList;
    if (type === 'employee') {
      if (pointName == "0 to 1") {
        this.page = 1;
        this.modalTitle = "Employee(s) with 0 to 1 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience != null && x.totalExperience >= 0 && x.totalExperience <= 1 && x.isApprenticeship != 'true' && x.isConsultant != 'true');
      } else if (pointName == "1 to 2") {
        this.page = 1;
        this.modalTitle = "Employee(s) with 1 to 2 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 1 && x.totalExperience <= 2 && x.isApprenticeship != 'true' && x.isConsultant != 'true');
      } else if (pointName == "2 to 5") {
        this.page = 1;
        this.modalTitle = "Employee(s) with 2 to 5 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 2 && x.totalExperience <= 5 && x.isApprenticeship != 'true' && x.isConsultant != 'true');
      } else if (pointName == "5 to 10") {
        this.page = 1;
        this.modalTitle = "Employee(s) with 5 to 10 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 5 && x.totalExperience <= 10 && x.isApprenticeship != 'true' && x.isConsultant != 'true');
      } else if (pointName == "10+") {
        this.page = 1;
        this.modalTitle = "Employee(s) with 10+ YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 10 && x.isApprenticeship != 'true' && x.isConsultant != 'true');
      }
    } else if (type === 'apprentice') {
      // this.page = 1;
      // this.modalTitle = "Apprentice(s)";
      // filteredList = modalTableList.filter(x => x.isApprenticeship === 'true'&& x.totalExperience >= 0 && x.totalExperience <= 1);
      if (pointName == "0 to 1") {
        this.page = 1;
        this.modalTitle = "Apprentice(s) with 0 to 1 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience != null && x.totalExperience >= 0 && x.totalExperience <= 1 && x.isApprenticeship === 'true');
      } else if (pointName == "1 to 2") {
        this.page = 1;
        this.modalTitle = "Apprentice(s) with 1 to 2 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 1 && x.totalExperience <= 2 && x.isApprenticeship === 'true');
      } else if (pointName == "2 to 5") {
        this.page = 1;
        this.modalTitle = "Apprentice(s) with 2 to 5 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 2 && x.totalExperience <= 5 && x.isApprenticeship === 'true');
      } else if (pointName == "5 to 10") {
        this.page = 1;
        this.modalTitle = "Apprentice(s) with 5 to 10 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 5 && x.totalExperience <= 10 && x.isApprenticeship === 'true');
      } else if (pointName == "10+") {
        this.page = 1;
        this.modalTitle = "Apprentice(s) with 10+ YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 10 && x.isApprenticeship === 'true');
      }
    } else if (type === 'consultant') {
      // this.page = 1;
      // this.modalTitle = "Consultant(s)";
      // filteredList = modalTableList.filter(x => x.isConsultant === 'true'&& x.totalExperience >= 0 && x.totalExperience <= 1);
      if (pointName == "0 to 1") {
        this.page = 1;
        this.modalTitle = "Consultant(s) with 0 to 1 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience != null && x.totalExperience >= 0 && x.totalExperience <= 1 && x.isConsultant === 'true');
      } else if (pointName == "1 to 2") {
        this.page = 1;
        this.modalTitle = "Consultant(s) with 1 to 2 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 1 && x.totalExperience <= 2 && x.isConsultant === 'true');
      } else if (pointName == "2 to 5") {
        this.page = 1;
        this.modalTitle = "Consultant(s) with 2 to 5 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 2 && x.totalExperience <= 5 && x.isConsultant === 'true');
      } else if (pointName == "5 to 10") {
        this.page = 1;
        this.modalTitle = "Consultant(s) with 5 to 10 YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 5 && x.totalExperience <= 10 && x.isConsultant === 'true');
      } else if (pointName == "10+") {
        this.page = 1;
        this.modalTitle = "Consultant(s) with 10+ YOE";
        filteredList = modalTableList.filter(x => x.totalExperience > 10 && x.isConsultant === 'true');
      }
    }

    this.modalSummaryList = filteredList;
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }


  openFresherLateralModalTable(pointName: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();

    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    this.page = 1;
    this.modalTitle = "Employee(s) " + pointName;
    if (pointName == 'Lateral') {
      this.modalSummaryList = modalTableList.filter(x => x.experience == 'Experienced');
    } else {
      this.modalSummaryList = modalTableList.filter(x => x.experience == 'Fresher');
    }
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeJoinResignModalTable(category: any, name: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = '';
    this.modalSummaryList = [];
    this.resetSearch();

    console.log('category --', category, 'name --', name)

    let modalTableList = this.allEmployeeList;
    let dateToday = moment().year();

    // Handle the "Joined Regular", "Joined Apprentice", and "Joined Consultant" categories
    if (name === 'Joined Regular' || name === 'Joined Apprentice' || name === 'Joined Consultant') {
      this.page = 1;
      this.modalTitle = `Employee(s) ${name} in ${category}`;

      // Filter by joining month and the employee type (Regular, Apprentice, Consultant)
      this.modalSummaryList = modalTableList.filter(x => {
        const joinMonthMatches = x.joiningMonth === category;
        const joinYearMatches = moment(x.dateOfJoining).year() === dateToday;

        // Filter based on the specific category (Joined Regular, Joined Apprentice, Joined Consultant)
        if (name === 'Joined Regular') {
          return joinMonthMatches && joinYearMatches && x.isApprenticeship === 'false';
        }
        if (name === 'Joined Apprentice') {
          return joinMonthMatches && joinYearMatches && x.isApprenticeship === 'true';
        }
        if (name === 'Joined Consultant') {
          return joinMonthMatches && joinYearMatches && x.isConsultant === 'true'; // Assuming `isConsultant` is a valid property
        }
        return false; // Default return in case no matches are found
      });

      // Log to debug
      console.log("Filtered Employees for", name, this.modalSummaryList);

      // Show modal
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }

    // Handle the "Resigned" category (existing logic)
    if (name === 'Resigned') {
      this.page = 1;
      this.modalTitle = `Employee(s) ${name} in ${category}`;
      this.modalSummaryList = modalTableList.filter(x => {
        const relievingMonthMatches = x.relievingMonth === category;
        const relievingYearMatches = moment(x.dateOfRelieving).year() === dateToday;
        return relievingMonthMatches && relievingYearMatches;
      });

      // Log to debug
      console.log("Filtered Resigned Employees:", this.modalSummaryList);

      // Show modal
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }


  openLeaveAnalysisTableModel(pointName: any, category: any) {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();

    let modalTableList = this.leaveTrendAnalysisList;
    this.page = 1;
    this.modalTitle = pointName + " taken on " + category;
    this.modalSummaryList = modalTableList.filter(x => x.leaveType == pointName && x.fromDate == category);
    this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-xl' });
  }

  // openWorkLocationSummaryTableModal(category:any){
  //   this.sortColumn=[];
  //   this.sortColumnType=[];
  //   this.sortDirection='';
  //   this.data =''
  //   this.modalSummaryList = [];
  //   this.resetSearch();

  //   let modalTableList = this.employeeWorkLocationList;
  //     this.page=1;
  //     this.modalTitle = "Work Location : " + category;
  //     this.modalSummaryList = modalTableList.filter(x => x.clientLocation == category);
  //     this.modalRef = this.modalService.show(this.workLocationSummaryTemplate, { class: 'modal-xl' });
  // }

  // openDepartmentWiseBillableEmployeeModalTable(deptName: any, billableType: any) {

  //   const billableTypeList = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];
  //   this.data = '';
  //   this.modalSummaryList = [];
  //   this.resetSearch();
  //   this.sortColumn = [];
  //   this.sortColumnType = [];
  //   this.sortDirection = '';
  //   let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
  //   this.page = 1;
  //   console.log("modalTableList   ", modalTableList);
  //   this.modalTitle = `Employee(s) with ${billableType} Billable Type`;
  //   if (billableTypeList.includes(billableType)) {
  //     this.modalSummaryList = modalTableList.filter(x => x.departmentName == deptName && x.billableType == billableType);
  //   } else {
  //     console.error('Invalid billable type');
  //     return;
  //   }
  //   this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  // }

  // openDepartmentWiseBillableEmployeeModalTable(deptName: any, billableType: any) {
  //   const billableTypeList = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];
  //   this.data = '';
  //   this.modalSummaryList = [];
  //   this.resetSearch();
  //   this.sortColumn = [];
  //   this.sortColumnType = [];
  //   this.sortDirection = '';
  //   // Use the same filter as departmentList for consistency
  //   let modalTableList = this.allEmployeeList.filter(
  //     x =>
  //       x.employmentstatus != 'InActive' &&
  //       x.isConsultant != 'true' &&
  //       x.isApprenticeship != 'true'
  //   );
  //   this.page = 1;
  //   this.modalTitle = `Employee(s) with ${billableType} Billable Type`;
  //   if (billableTypeList.includes(billableType)) {
  //     this.modalSummaryList = modalTableList.filter(
  //       x => x.departmentName == deptName && x.billableType == billableType
  //     );
  //   } else {
  //     console.error('Invalid billable type');
  //     return;
  //   }
  //   this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  // }

  //   openDepartmentWiseBillableEmployeeModalTable(deptName: any, billableType: any) {

  //   const billableTypeList = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];
  //   this.data = '';
  //   this.modalSummaryList = [];
  //   this.resetSearch();
  //   this.sortColumn = [];
  //   this.sortColumnType = [];
  //   this.sortDirection = '';



  //   // Use departmentWiseBillableEmployeeList for custom department selection
  //   let modalTableList = this.departmentWiseBillableEmployeeList.filter(
  //     x =>
  //       x.employmentstatus != 'InActive' &&
  //       x.isConsultant != 'true' &&
  //       x.isApprenticeship != 'true'
  //   );
  //   console.log("modalTableList   ", modalTableList);

  //   this.page = 1;
  //   this.modalTitle = `Employee(s) with ${billableType} Billable Type`;
  //   if (billableTypeList.includes(billableType)) {
  //     this.modalSummaryList = modalTableList.filter(
  //       x => x.departmentName == deptName && x.billableType == billableType

  //     );
  //     console.log("Filtered Employees:", this.modalSummaryList, "for Department:", deptName, "and Billable Type:", billableType)
  //   } else {
  //     console.error('Invalid billable type');
  //     return;
  //   }
  //   this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  // }

  openDepartmentWiseBillableEmployeeModalTable(deptName: any, billableType: any) {
    const billableTypeList = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];
    this.data = '';
    this.modalSummaryList = [];
    this.resetSearch();
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    // Use the same filter as departmentList for consistency
    let modalTableList = this.allEmployeeList.filter(
      x =>
        x.employmentstatus != 'InActive' &&
        x.isConsultant != 'true' &&
        x.isApprenticeship != 'true'
    );
    this.page = 1;
    this.modalTitle = `Employee(s) with ${billableType} Billable Type`;
    if (billableTypeList.includes(billableType)) {
      this.modalSummaryList = modalTableList.filter(
        x => x.departmentName == deptName && x.billableType == billableType
      );
    } else {
      console.error('Invalid billable type');
      return;
    }
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  findAllDepartment() {
    this.allDepartmentList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDepartmentList = response.serviceResponse;
        // this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
        // console.log("allDepartmentList : ", this.allDepartmentList)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }



  openDepartmentWiseEmployeeKycModalTable(deptName: any, status: any) {
    console.log("dept ment Name ", deptName)
    console.log("status   in kyc ", status)
    const CHECK_PERCENT = 100.00;
    this.data = ''
    this.modalSummaryList = [];
    this.resetSearch();
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    this.page = 1;
    this.modalTitle = "Employee(s) with KYC " + status;
    if (status == "Pending") {
      this.modalSummaryList = modalTableList.filter(x => x.departmentName == deptName && x.profileCompletedPercent < CHECK_PERCENT);
    } else {
      this.modalSummaryList = modalTableList.filter(x => x.departmentName == deptName && x.profileCompletedPercent >= CHECK_PERCENT);
    }
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = 'desc';
    }
  }

  // sortData(sort: Sort) {
  //   if (sort.active) {
  //     let sortParams: any[] = sort.active?.split("|");
  //     this.sortColumn = sortParams[0];
  //     this.sortColumnType = sortParams[1];
  //     this.sortDirection = sort.direction;

  //     // Special case for Department Wise Employee Chart
  //     if (this.modalTitle.includes("Employee(s) with")) {
  //       // This condition identifies the chart type as Department Wise Employee Chart
  //       if (this.sortColumn === 'employeeCount') {
  //         this.sortColumn = 'employeeCount'; // Ensure you're sorting by employee count
  //         this.sortDirection = 'desc'; // Always descending for this chart
  //       }
  //     }
  //   }
  // }


  toggleSearch() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

    loadDepartmentWiseKycData() {
    this.reportService.getDepartmentWiseKycCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.processDepartmentKycData(response.serviceResponse);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

    processDepartmentKycData(kycDataList: DepartmentKycCountDTO[]) {
    // Clear previous data
    this.kycChartData[0].data = [];
    this.kycChartData[1].data = [];
    this.departmentCategories = [];
    
    // Group data by department
    const departmentMap = new Map<string, { pending: number, completed: number }>();
    
    kycDataList.forEach(item => {
      if (!departmentMap.has(item.departmentName)) {
        departmentMap.set(item.departmentName, { pending: 0, completed: 0 });
      }
      
      const deptData = departmentMap.get(item.departmentName)!;
      if (item.status === 'Pending') {
        deptData.pending = item.empCount;
      } else if (item.status === 'Completed') {
        deptData.completed = item.empCount;
      }
    });

    // Convert map to array and sort by department name
    this.departmentKycData = Array.from(departmentMap.entries())
      .map(([departmentName, counts]) => ({
        departmentName,
        pending: counts.pending,
        completed: counts.completed
      }))
      .sort((a, b) => a.departmentName.localeCompare(b.departmentName));

    // Prepare chart data
    this.departmentKycData.forEach(dept => {
      this.departmentCategories.push(dept.departmentName);
      this.kycChartData[0].data.push(dept.pending);
      this.kycChartData[1].data.push(dept.completed);
    });

    // Render the chart
    this.renderStackBarChart(
      'Employee KYC Summary', 
      'employeeKycSummary', 
      this.kycChartData, 
      this.departmentCategories, 
      'Employee', 
      this.openDepartmentWiseEmployeeKycModalTable.bind(this)
    );
  }


  loadJoinResignData(): void {
    let selectedYear = this.selectedYear || moment().year(); 
    this.reportService.getJoinVsResignCount(selectedYear).pipe(first()).subscribe((response: any) => {
        const monthlyData = response?.serviceResponse || [];

        const joiningRegular = Array(12).fill(0);
        const joiningApprentice = Array(12).fill(0);
        const joiningConsultant = Array(12).fill(0);
        const resigning = Array(12).fill(0);

        const monthIndexMap: { [key: string]: number } = {
          January: 0, February: 1, March: 2, April: 3, May: 4, June: 5,
          July: 6, August: 7, September: 8, October: 9, November: 10, December: 11
        };

        monthlyData.forEach(item => {
          const idx = monthIndexMap[item.monthName];
          if (idx !== undefined) {
            joiningRegular[idx] = item.regularCount || 0;
            joiningApprentice[idx] = item.apprenticeCount || 0;
            joiningConsultant[idx] = item.consultantCount || 0;
            resigning[idx] = item.resignCount || 0;
          }
        });

        const chartData = [
          {
            name: 'Joined Regular',
            data: joiningRegular,
            stack: 'joined',
            color: '#1f77b4'
          },
          {
            name: 'Joined Apprentice',
            data: joiningApprentice,
            stack: 'joined',
            color: '#ff7f0e'
          },
          {
            name: 'Joined Consultant',
            data: joiningConsultant,
            stack: 'joined',
            color: '#2ca02c'
          },
          {
            name: 'Resigned',
            data: resigning,
            stack: 'resigned',
            color: '#d62728'
          }
        ];

        this.renderMultiBarChart('Employee Join VS Resign', 'employeeJoinAndResign', chartData, 'Employee', this.openEmployeeJoinResignModalTable.bind(this));
      },
      (error) => {
        console.error('API call failed:', error);
      }
    );
  }  getAllPieChartCount() {

    this.reportService.getAllPieChartCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //Gender summary count
        this.maleCount = response.serviceResponse[0].genderMale;
        this.femaleCount = response.serviceResponse[0].genderFemale;
        this.otherCount = response.serviceResponse[0].genderOther;
        //Age summary count
        this.countBetween18and25 = response.serviceResponse[0].age18to25;
        this.countBetween25and35 = response.serviceResponse[0].age25to35;
        this.countBetween35and45 = response.serviceResponse[0].age35to45;
        this.countAbove45 = response.serviceResponse[0].ageAbove45;
        //Employee status summary
        this.probationCount = response.serviceResponse[0].employeeStatusProbation;
        this.confirmedCount = response.serviceResponse[0].employeeStatusConfirmed;
        this.resignedCount = response.serviceResponse[0].employeeStatusResigned;
        this.inActiveCount = response.serviceResponse[0].employeeStatusInActive;
        //Fresher-Lateral summary 
        this.fresherCount = response.serviceResponse[0].fresherCount;
        this.experienceCount = response.serviceResponse[0].lateralCount;
        //Department-wise Billable/Non-billable Employee summary
        this.tnmBillableCount = response.serviceResponse[0].tnm;
        this.fcBillableCount = response.serviceResponse[0].fixedCost;
        this.internalBillableCount = response.serviceResponse[0].internalRNDProducts;
        this.benchBillableCount = response.serviceResponse[0].bench;
        this.shadowBillableCount = response.serviceResponse[0].shadow;
        //Billable summary 
        this.billableCount = response.serviceResponse[0].billableYes;
        this.nonBillableCount = response.serviceResponse[0].billableNo;
        //Employee experience
        //Employee
        this.experienceCountBetween0and1 = response.serviceResponse[0].employeeYears0to1;
        this.experienceCountBetween1and2 = response.serviceResponse[0].employeeYears1to2;
        this.experienceCountBetween2and5 = response.serviceResponse[0].employeeYears2to5;
        this.experienceCountBetween5and10 = response.serviceResponse[0].employeeYears5to10;
        this.experienceCountAbove10 = response.serviceResponse[0].employeeYearsAbove10;
        //Apprentice
        this.experienceCountBetween0and1Apprentice = response.serviceResponse[0].apprenticeYears0to1;
        this.experienceCountBetween1and2Apprentice = response.serviceResponse[0].apprenticeYears1to2;
        this.experienceCountBetween2and5Apprentice = response.serviceResponse[0].apprenticeYears2to5;
        this.experienceCountBetween5and10Apprentice = response.serviceResponse[0].apprenticeYears5to10;
        this.experienceCountAbove10Apprentice = response.serviceResponse[0].apprenticeYearsAbove10;
        //Consultant
        this.experienceCountBetween0and1Consultant = response.serviceResponse[0].consultantYear0to1;
        this.experienceCountBetween1and2Consultant = response.serviceResponse[0].consultantYear1to2;
        this.experienceCountBetween2and5Consultant = response.serviceResponse[0].consultantYear2to5;
        this.experienceCountBetween5and10Consultant = response.serviceResponse[0].consultantYear5to10;
        this.experienceCountAbove10Consultant = response.serviceResponse[0].consultantYearAbove10;

        this.countOfAllEmployees = response.serviceResponse[0].totalEmployeeCountDisplay;
        this.employeeInProbationAfter6MonthsCount = response.serviceResponse[0].probationCountDisplay;
        this.apprenticeCountForDisplay = response.serviceResponse[0].apprenticeCountDisplay;
        this.consultantCountForDisplay = response.serviceResponse[0].consultantCountDisplay;
        this.regularCountForDisplay = response.serviceResponse[0].regularCountDisplay;
        this.renderAllPieCharts();
        console.log("Male count", this.maleCount);

    
      } else {
        console.error(response.serviceResponse);
      }
    });
  
  }

  renderAllPieCharts() {
        let employeeStatusData = [{
      name: "Probation",
      y: this.probationCount
    },
    {
      name: "Confirmed",
      y: this.confirmedCount
    },
    {
      name: "Resigned",
      y: this.resignedCount
    },
    {
      name: "InActive",
      y: this.inActiveCount
    }];

    //console.log("leaveStatusData : ", employeeStatusData);

    let checkEmployeeStatusData = employeeStatusData.filter(data => data.y != 0);
    //console.log("checkEmployeeStatusData :", checkEmployeeStatusData);

    if (checkEmployeeStatusData && checkEmployeeStatusData.length != 0) {
      this.renderPieSummaryChart('Employee Status Summary', 'employeeStatus', employeeStatusData, 'Employee Status', this.openEmployeeStatusTableModal.bind(this));
    } else {
      this.renderPlaceholderChart('Employee Status Summary', 'employeeStatus');
    }

        let genderData = [{
      name: "male",
      y: this.maleCount
    },
    {
      name: "female",
      y: this.femaleCount
    },
    {
      name: "other",
      y: this.otherCount
    }];

    //console.log("genderData : ", genderData);

    let checkGenderData = genderData.filter(data => data.y != 0);
    //console.log("checkGenderData :", checkGenderData);

    if (checkGenderData && checkGenderData.length != 0) {
      this.renderPieSummaryChart('Gender Summary', 'genderSummary', genderData, 'Employee Summary', this.openGenderSummaryModalTable.bind(this));
    } else {
      this.renderPlaceholderChart('Gender Summary', 'genderSummary');
    }

    /*
   Chart Data for - Age Summary Graph.
  */

    let employeeAgeData = [{
      name: "18 to 25",
      y: this.countBetween18and25
    },
    {
      name: "25 to 35",
      y: this.countBetween25and35
    },
    {
      name: "35 to 45",
      y: this.countBetween35and45
    },
    {
      name: "45+",
      y: this.countAbove45
    }];

    //console.log("employeeAgeData : ", employeeAgeData);

    let checkEmployeeAgeData = employeeAgeData.filter(data => data.y != 0);
    //console.log("checkGenderData :", checkGenderData);

    if (checkEmployeeAgeData && checkEmployeeAgeData.length != 0) {
      this.renderPieSummaryChart('Age Summary', 'employeeAgeSummary', employeeAgeData, 'Employee Summary', this.openAgeSummayModalTable.bind(this));
    } else {
      this.renderPlaceholderChart('Age Summary', 'employeeAgeSummary');
    }
    let experienceData = [
      {
        name: "0 to 1",
        employeeCount: this.experienceCountBetween0and1,
        apprenticeCount: this.experienceCountBetween0and1Apprentice,
        consultantCount: this.experienceCountBetween0and1Consultant,
      },
      {
        name: "1 to 2",
        employeeCount: this.experienceCountBetween1and2,
        apprenticeCount: this.experienceCountBetween1and2Apprentice,
        consultantCount: this.experienceCountBetween1and2Consultant,
      },
      {
        name: "2 to 5",
        employeeCount: this.experienceCountBetween2and5,
        apprenticeCount: this.experienceCountBetween2and5Apprentice,
        consultantCount: this.experienceCountBetween2and5Consultant,
      },
      {
        name: "5 to 10",
        employeeCount: this.experienceCountBetween5and10,
        apprenticeCount: this.experienceCountBetween5and10Apprentice,
        consultantCount: this.experienceCountBetween5and10Consultant,
      },
      {
        name: "10+",
        employeeCount: this.experienceCountAbove10,
        apprenticeCount: this.experienceCountAbove10Apprentice,
        consultantCount: this.experienceCountAbove10Consultant,
      },
    ];

    // Categories (Experience Ranges)
    let totalExperienceCategories = experienceData.map(exp => exp.name);

    // Series Data (for employees and apprentices)
    let employeeSeries = experienceData.map(exp => exp.employeeCount);
    let apprenticeSeries = experienceData.map(exp => exp.apprenticeCount);
    let consultantSeries = experienceData.map(exp => exp.consultantCount);

    //console.log(" totalExperienceData :", totalExperienceData);
    //this.renderColumnBarSummaryChart('Employee Experience','employeeExperienceSummary',totalExperienceData,totalExperienceCategories,'Experience', this.openEmployeeExperienceModalTable.bind(this));
    // this.renderColumnBarSummaryChart(
    //   'Employee Experience',
    //   'employeeExperienceSummary', 
    //   totalExperienceCategories, 
    //   employeeSeries,
    //   apprenticeSeries,
    //   this.openEmployeeExperienceModalTable.bind(this) 
    // );
    this.plotEmployeeExperienceCylinderGraph(
      'Employee Experience',
      'employeeExperienceSummary',
      totalExperienceCategories,
      employeeSeries,
      apprenticeSeries,
      consultantSeries,
      this.openEmployeeExperienceModalTable.bind(this)
    );


    /*
    Chart Data for - Employee Fresher - Lateral Graph Data.
   */

    let fresherLateralData = [{
      name: "Fresher",
      y: this.fresherCount
    },
    {
      name: "Lateral",
      y: this.experienceCount
    }];

    //console.log("genderData : ", genderData);

    let checkFresherLateralData = fresherLateralData.filter(data => data.y != 0);
    //console.log("checkFresherLateralData :", checkFresherLateralData);

    if (checkFresherLateralData && checkFresherLateralData.length != 0) {
      this.renderPieSummaryChart('Fresher - Lateral Summary', 'fresherLateralChart', fresherLateralData, 'Employee Summary', this.openFresherLateralModalTable.bind(this));
    } else {
      this.renderPlaceholderChart('Fresher - Lateral Summary', 'fresherLateralChart');
    }
        let billableTypeData = [{
      name: "Yes",
      y: this.billableCount
    }, {
      name: "No",
      y: this.nonBillableCount
    },
    {
      name: "Other",
      y: this.otherBillableCount
    }]

    let checkEmployeeBillableData = billableTypeData.filter(data => data.y != 0);
    // console.log(" checkEmployeeBillableData ",checkEmployeeBillableData);

    if (checkEmployeeBillableData && checkEmployeeBillableData.length != 0) {
      this.renderPieSummaryChart('Billable Employee Summary', 'billableChart', billableTypeData, 'Billable Data', this.openBillableEmployeeTableModal.bind(this));
    } else {
      this.renderPlaceholderChart('Billable Employee Summary', 'billableChart');
    }
  }





  //added by Dibya to call Department Wise Employee Count Graph
  renderDepartmentWiseEmployeeChartWrapper() {
    console.log("departmentWiseEmployeeData:", this.departmentWiseEmployeeData);
    console.log("departmentWiseEmployeeCategories:", this.departmentWiseEmployeeCategories);

    this.renderDepartmentWiseEmployeeChart(
      'Department Wise Employee',
      'departmentWiseEmployee',
      this.departmentWiseEmployeeData,
      this.departmentWiseEmployeeCategories,
      'Department',
      this.openDepartmentWiseEmployeeModalTable.bind(this)
    );
  }

  //added by Dibya custom api for department wise employee count
  getAllEmployeeCountDepartmentWise() {
    this.reportService.getAllEmployeeCountDepartmentWise().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const rawData = response.serviceResponse; // Array of arrays
        const departmentMap: { [key: string]: any } = {};

        rawData.forEach(([departmentName, type, count]) => {
          if (!departmentName) return;

          if (!departmentMap[departmentName]) {
            departmentMap[departmentName] = {
              departmentName,
              employeeCount: 0,
              apprenticeCount: 0,
              consultantCount: 0
            };
          }

          if (type === "Employee") {
            departmentMap[departmentName].employeeCount += count;
          } else if (type === "Apprentice") {
            departmentMap[departmentName].apprenticeCount += count;
          } else if (type === "Consultant") {
            departmentMap[departmentName].consultantCount += count;
          }
        });

        const departmentData = Object.values(departmentMap);

        // Sort departments by employee count descending
        departmentData.sort((a: any, b: any) => b.employeeCount - a.employeeCount);

        this.departmentWiseEmployeeData = departmentData.map(dept => ({
          name: dept.departmentName,
          data: [
            dept.employeeCount,
            dept.apprenticeCount,
            dept.consultantCount
          ]
        }));

        this.departmentWiseEmployeeCategories = departmentData.map(dept => dept.departmentName);

        this.renderDepartmentWiseEmployeeChartWrapper();
        //  Log the transformed data for verification
        console.log("departmentWiseEmployeeData:", this.departmentWiseEmployeeData);
        console.log("departmentWiseEmployeeCategories:", this.departmentWiseEmployeeCategories);

      } else {
        console.error(" Failed to get department-wise employee count:", response.serviceResponse);
      }
    });
  }


//added by Dibya to call Department Wise Billable API
 getAllBillableTypeCount(departmentIds?: number[]) {
  const selectedDeptIds = departmentIds && departmentIds.length > 0 ? departmentIds : [];

  this.reportService.getDepartmentWiseBillableNonBillableSummary(selectedDeptIds).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      const rawData = response.serviceResponse;
      const processedData = this.prepareBillableChartData(rawData);
      this.renderDepartmentWiseBillablePieChart(processedData);
      console.log("Processed Billable Chart Data:", processedData);
    } else {
      console.error("Error fetching department billable data:", response.serviceResponse);
      this.renderPlaceholderChart(
        'Department wise Billable/Non-Billable Employee Summary',
        'billableChartByDepartment'
      );
    }
  });
}




  //added by Transform raw  for departmentwise billable employee count response into chart format
  prepareBillableChartData(rawData: any[]): { name: string, y: number }[] {
    const billableTypeMap: { [key: string]: number } = {};

    rawData.forEach(([_, billableType, count]) => {
      if (!billableTypeMap[billableType]) {
        billableTypeMap[billableType] = 0;
      }
      billableTypeMap[billableType] += count;
    });

    return Object.keys(billableTypeMap).map(type => ({
      name: type,
      y: billableTypeMap[type]
    }));
  }


  // added by Dibya to Renders chart using processed data fro Department Wise Billable Employee
  renderDepartmentWiseBillablePieChart(data: { name: string, y: number }[]) {
    const filteredData = data.filter(entry => entry.y !== 0);
     
    if (filteredData.length > 0) {
      this.renderPieSummaryChart(
        'Department wise Billable/Non-Billable Employee Summary',
        'billableChartByDepartment',
        filteredData,
        'Department wise Billable Data',
        this.openDepartmentWiseBillableEmployeeTableModal.bind(this)
      );
    } else {
      this.renderPlaceholderChart(
        'Department wise Billable/Non-Billable Employee Summary',
        'billableChartByDepartment'
      );
    }
  }


  getEmployeeBillableSummary() {
  const selectedDeptIds: number[] = []; // Empty array to fetch all departments

  this.reportService.getDepartmentWiseBillableNonBillableSummary(selectedDeptIds).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      const rawData = response.serviceResponse;

      const billableChartData = this.prepareBillableChartDataBillabe(rawData);
      this.renderEmployeeBillableSummaryChartWrapper(billableChartData);

    } else {
      console.error("Error fetching employee billable summary data:", response.serviceResponse);
      this.renderPlaceholderChart(
        'Employee Billable/Non-Billable Summary',
        'billableEmployeeSummary'
      );
    }
  });
}


prepareBillableChartDataBillabe(rawData: any[]): any[] {
  const departmentTotalCountMap = new Map<string, number>(); // Total count per department
  const departmentSet = new Set<string>();
  const typeSet = new Set<string>();

  // First pass: collect all types and total count per department
  rawData.forEach(([dept, type, count]) => {
    if (!dept || !type) return;

    departmentSet.add(dept);
    typeSet.add(type);

    // Track total count per department
    departmentTotalCountMap.set(dept, (departmentTotalCountMap.get(dept) || 0) + count);
  });

  // Sort departments by total count descending
  const sortedDepartments = Array.from(departmentSet).sort((a, b) => {
    const aCount = departmentTotalCountMap.get(a) || 0;
    const bCount = departmentTotalCountMap.get(b) || 0;
    return bCount - aCount;
  });

  const typeList = Array.from(typeSet);
  const typeToDataMap = new Map<string, number[]>();

  // Initialize arrays for each type
  typeList.forEach(type => {
    typeToDataMap.set(type, new Array(sortedDepartments.length).fill(0));
  });

  // Populate data arrays
  rawData.forEach(([dept, type, count]) => {
    const deptIndex = sortedDepartments.indexOf(dept);
    if (deptIndex !== -1 && type) {
      const dataArr = typeToDataMap.get(type);
      if (dataArr) {
        dataArr[deptIndex] += count;
      }
    }
  });

  // Save department names to use on x-axis
  this.billableChartCategories = sortedDepartments;

  // Return chart series data
  return Array.from(typeToDataMap.entries()).map(([type, data]) => ({
    name: type,
    data: data,
    stack: 'base'
  }));
}

renderEmployeeBillableSummaryChartWrapper(billableChartData: any[]) {
  this.renderStackBarChart(
    'Employee Billable/Non-Billable Summary',
    'billableEmployeeSummary',
    billableChartData,
    this.billableChartCategories, // X-axis: departments
    'Employee',
    this.openDepartmentWiseBillableEmployeeModalTable.bind(this)
  );
}








}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}


