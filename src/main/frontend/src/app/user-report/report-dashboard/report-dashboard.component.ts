import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { first, groupBy, take } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ReportService , ReportsQueryPayload, CustomFilter } from 'src/app/services/report-service.service';
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
import { forkJoin } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

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
import { EmployeeIdUtilService } from 'src/app/services/employee-id-util.service';
import { Feature } from 'src/app/models/feature';
import { Employee360Service } from 'src/app/services/employee360.service';
import { SortPipe } from 'src/app/sort.pipe';
import { Router } from '@angular/router';
import { query } from '@angular/animations';


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
export interface PieParamPayload {
  inActiveFlag?: number;
  billable?: string;
  billableType?: string;
  employmentstatus?: string;
  gender?: string;
  experience?: string;
  lowerAge?: number;
  upperAge?: number;
  queryList: CustomFilter[]; // The crucial part
}

@Component({
  standalone: false,
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
  sortBy='name';
  isfileUpload: boolean = false;
  file: any;

  modalRef:NgbModalRef;

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
  apmosysProductCountForDisplay = 0;
  apmosysProductConsultantCountForDisplay = 0;
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
  //Apmosys Product
  experienceCountBetween0and1ApmosysProduct = 0;
  experienceCountBetween1and2ApmosysProduct = 0;
  experienceCountBetween2and5ApmosysProduct = 0;
  experienceCountBetween5and10ApmosysProduct = 0;
  experienceCountAbove10ApmosysProduct = 0;
  experienceCountBetween0and1ApmosysProductConsultant = 0;
  experienceCountBetween1and2ApmosysProductConsultant = 0;
  experienceCountBetween2and5ApmosysProductConsultant = 0;
  experienceCountBetween5and10ApmosysProductConsultant = 0;
  experienceCountAbove10ApmosysProductConsultant = 0;

  leaveSummaryColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Leave Type', 'Department', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  timesheetSummaryColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By'];

  employeeColumns: any[] = ['Employee Id', 'employeeType', 'Full Name', 'Department', 'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name', 'Employment Status', 'Date Of Joining', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On', 'Experience'];
  employeeSummaryColumns: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'experience', 'departmentName', 'email', 'managerName', 'billable', 'billableType', 'projectName', 'clientName', 'dateOfJoining', 'mobileNo', 'employmentstatus', 'totalExperience','totalCurrentExperience', 'gender', 'workLocation', 'age', 'profileKycStatus'];
  workLocationSummaryColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'projectName', 'clientName', 'workLocation', 'clientLocation', 'departmentName','managerName','billable','billableType','totalExperience'];
  LeaveTrendAnalysisGraphColumns: any[] = ['blank', 'employeementId', 'employeeType', 'employeeName', 'departmentName', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'status'];
  leaveSummaryTableColumns: any[] = ['blank', 'employmentIdAcToET', 'employeeType', 'employeeName', 'departmentName','managerName', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'status','typeOfLeave'];
  timesheetSummaryTableColumns: any[] = ['blank', 'employmentIdAcToET', 'employeeType', 'employeeName', 'departmentName', 'email', 'managerName', 'mobileNo', 'pendingEodCount', 'legend'];
  eodSegregationTableColumns: any[] = ['blank', 'employmentIdAcToET', 'employeeType', 'employeeName', 'departmentName', 'email', 'managerName', 'mobileNo', 'date', 'dayType', 'totalWorkingHours'];

  billableChartByDepartmentColumns: any[] = ['Department'];

  resignedColumns: any[] = ['blank', 'employmentIdAcToET', 'employeeType', 'name', 'departmentName', 'dateOfResign', 'dateOfRelieving', 'managerName'];
  departmentIds: any[] = [];
  allDepartmentList: any[] = [];
  currentUser: User;
  show: number = -1;
  yearList: number[] = [];
  queryObj = new Query();

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



  // This array will hold the state of the user's applied filters
  activeFilters: CustomFilter[] = [];
  departmentCategories: string[] = [];
  selectedYear: number;
  isLoading: boolean;
  modalLeaveData: any[];
  modalSummary: { date: string; leaveType: string; totalDays: number; employeeCount: number; };
  summaryData: any;
  previousLeaveTypes: any[];
  previousFormattedDateRange: any[];
  totalResignEmployees = 0;
 pageSize = 10;
 totalPages = 1;
  size: number = 10;

  constructor(
    private reportService: ReportService,
    private leaveService: LeaveService,
    private timesheetService: TimesheetService,
    private modalService: NgbModal,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private departmentService: DepartmentService,
    private domainService: DomainService,
    private authenticationService: AuthenticationService,
    public utilityService: UtilityService,
    private employeeIdUtilService: EmployeeIdUtilService,
    public route: Router
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  private displayEmploymentId(emp: any): string {
    return this.employeeIdUtilService.generateEmploymentId(
      emp?.employeementId ?? emp?.employmentIdAcToET,
      emp?.isApmosysProduct,
      emp?.isConsultant,
      emp?.employeeType
    ) ?? emp?.employeementId;
  }

  private displayEmployeeType(emp: any): string {
    if (emp?.employeeType) {
      return emp.employeeType;
    }
    return this.employeeIdUtilService.resolveEmployeeType(
      emp?.isApmosysProduct,
      emp?.isConsultant,
      emp?.isApprenticeship
    );
  }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.sectionViewInit();
    this.preventBackButton();
    this.selectedYear = moment().year();
    this.yearList = [];
    for (let i = 0; i < 10; i++) {
      this.yearList.push(moment().year() - i);
    }



  }


  // Method to custom join and resign count
  processJoinResignCount(response: any){
    // This null check makes it safe
    const monthlyData = response?.serviceResponse || [];

    const joiningRegular = Array(12).fill(0);
    const joiningApprentice = Array(12).fill(0);
    const joiningConsultant = Array(12).fill(0);
    const resigning = Array(12).fill(0);
    const joiningApmosysProduct = Array(12).fill(0);
    const joiningApmosysProductConsultant = Array(12).fill(0);

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
        joiningApmosysProduct[idx] = item.apmosysProductCount || 0;
        joiningApmosysProductConsultant[idx] = item.apmosysProductConsultantCount || 0;
      }
    });

    const chartData = [
      { name: 'Regular', data: joiningRegular, stack: 'joined', color: '#1f77b4' },
      { name: 'apprenticeship', data: joiningApprentice, stack: 'joined', color: '#ff7f0e' },
      { name: 'consultant', data: joiningConsultant, stack: 'joined', color: '#2ca02c' },
      { name: 'Apmosys Product Consultant', data: joiningApmosysProductConsultant, stack: 'joined', color: '#9b59b6' },
      { name: 'Apmosys Product', data: joiningApmosysProduct, stack: 'joined', color: '#D288A2' },
      { name: 'resign', data: resigning, stack: 'resigned', color: '#d62728' }
    ];

    this.renderMultiBarChart('Employee Join VS Resign', 'employeeJoinAndResign', chartData, 'Employee', this.openEmployeeJoinResignModalTable.bind(this));
  }


  //-------end--------------
   fetchLeaveTrendDetails(queryObjList: any): void {
    this.leaveTrendAnalysisList = [];
        let queryObj = new Query();
    queryObj.queryList = queryObjList;

    this.reportService.customgetLeaveTrendDetails(queryObj).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.leaveTrendAnalysisList = response.serviceResponse || [];
          this.extractLeaveTrendAnalysisData();
          console.log('Successfully fetched filtered data:', this.leaveTrendAnalysisList);
        } else {
          this.leaveTrendAnalysisList = [];
          this.extractLeaveTrendAnalysisData();
          console.error('API Error:', response.serviceResponse);
        }
      },
      error: (err) => {
        console.error('HTTP Error:', err);
      }
    });
  }
onFilterChange(filter: CustomFilter): void {
    const existingFilterIndex = this.activeFilters.findIndex(f => f.column === filter.column);

    if (existingFilterIndex > -1) {
      this.activeFilters[existingFilterIndex] = filter;
    } else {
      this.activeFilters.push(filter);
    }

  }
    clearFilters(): void {
    this.activeFilters = [];
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

    this.findAllDepartment();
    this.getEmployeeWorkLocation();
    // this.loadDashboardData(this.queryList);
    this.getLeaveTrendDetails();

    let selectedYear = this.selectedYear || moment().year();
    const selectedDeptIds: number[] = [];

  forkJoin({
    pieChart: this.reportService.getAllPieChartCount().pipe(
      catchError(error => of({ serviceStatus: 'Error', error }))
    ),
    deptWiseCount: this.reportService.getDepartmentWiseBillableNonBillableSummary(selectedDeptIds).pipe(
      catchError(error => of({ serviceStatus: 'Error', error }))
    ),
    billableSummary: this.reportService.getAllEmployeeCountDepartmentWise().pipe(
      catchError(error => of({ serviceStatus: 'Error', error }))
    ),
    departmentKyc: this.reportService.getDepartmentWiseKycCount().pipe(
      catchError(error => of({ serviceStatus: 'Error', error }))
    ),
    joinResign: this.reportService.getJoinVsResignCount(selectedYear).pipe(
      catchError(error => of({ serviceStatus: 'Error', error })))
}).subscribe(results => {
    // Pass the entire API response to renderAllPieCharts
    if (results.pieChart.serviceStatus === 'Success') {
      this.renderAllPieCharts(results.pieChart);
    } else {
      console.error('PieChart failed:', results.pieChart.error);
      this.renderAllPieCharts(null); // Render placeholders on failure
    }

  if (results.deptWiseCount.serviceStatus === 'Success') {
    const billableChartData = this.prepareBillableChartDataBillabe(results.deptWiseCount.serviceResponse);
    this.renderEmployeeBillableSummaryChartWrapper(billableChartData);
  } else {
    console.error('DeptWiseCount failed:', results.deptWiseCount.error);
  }

  if (results.billableSummary.serviceStatus === 'Success') {
    this.processDepartmentWiseCount(results.billableSummary.serviceResponse);
  } else {
    console.error('BillableSummary failed:', results.billableSummary.error);
  }

  if (results.departmentKyc.serviceStatus === 'Success') {
    this.processDepartmentKycData(results.departmentKyc.serviceResponse);
  } else {
    console.error('DepartmentKyc failed:', results.departmentKyc.error);
  }

  if (results.joinResign.serviceStatus === 'Success') {
    this.processJoinResignCount(results.joinResign);
  } else {
    console.error('JoinVsResign failed:', results.joinResign.error);
  }

});
  }


  loadDashboardData(queryObjList: any): void {
    this.isLoading = true;


    this.queryObj.queryList = queryObjList;
    let selectedYear = this.selectedYear || moment().year();

    forkJoin({
      graphSummary: this.reportService.customgetGraphEmployeeSummary(this.queryObj).pipe(catchError(() => of(null))),
      leaveTrend: this.reportService.customgetLeaveTrendDetails(this.queryObj).pipe(catchError(() => of(null))),
      joinResign: this.reportService.customgetJoinVsResignCount(this.queryObj, selectedYear).pipe(catchError(() => of(null))),

      workLocation: this.reportService.customgetWorkLocationDetails(this.queryObj).pipe(catchError(() => of(null)))
    }).subscribe({
      next: (results) => {
        this.renderAllPieCharts(results.graphSummary);

        // --- Process Join vs. Resign ---
        if ( results.joinResign.serviceStatus === 'Success') {
          this.processJoinResignCount(results.joinResign);
        } else {
          console.error('JoinVsResign failed or no data returned.');
          // Render an empty chart if the call fails
          this.processJoinResignCount(null);
        }
        if (results.workLocation && results.workLocation.serviceStatus === 'Success') {
          this.renderWorkLocationData(results.workLocation.serviceResponse);
        } else {
          console.error("Failed to fetch Work Location data.");
          this.renderWorkLocationData([]); // Pass empty array to render placeholder
        }

        if (results.leaveTrend && results.leaveTrend.serviceStatus === 'Success') {
          this.leaveTrendAnalysisList = results.leaveTrend.serviceResponse;
          this.extractLeaveTrendAnalysisData();
        } else {
           console.error("Failed to fetch leave trend or no data returned.");
           this.leaveTrendAnalysisList = [];
           this.extractLeaveTrendAnalysisData();
        }






        this.isLoading = false;
      }
      ,


      error: (err) => {
        console.error("Major error fetching dashboard data", err);
        this.renderAllPieCharts(null);
        this.leaveTrendAnalysisList = [];
        this.extractLeaveTrendAnalysisData();
        this.processJoinResignCount(null); // Clear join/resign chart on error
        this.isLoading = false;
      }
    });
  }

  renderAllPieCharts(summaryData: any) {
    if (!summaryData || summaryData.serviceStatus !== 'Success' || !summaryData.serviceResponse || summaryData.serviceResponse.length === 0) {
      console.warn("No valid summary data found. Rendering placeholder charts.");

      this.maleCount = 0; this.femaleCount = 0; this.otherCount = 0;
      this.countBetween18and25 = 0; this.countBetween25and35 = 0; this.countBetween35and45 = 0; this.countAbove45 = 0;
      this.probationCount = 0; this.confirmedCount = 0; this.resignedCount = 0; this.inActiveCount = 0;
      this.fresherCount = 0; this.experienceCount = 0;
      this.tnmBillableCount = 0; this.fcBillableCount = 0; this.internalBillableCount = 0; this.benchBillableCount = 0; this.shadowBillableCount = 0;
      this.billableCount = 0; this.nonBillableCount = 0; this.otherBillableCount = 0;
      this.experienceCountBetween0and1 = 0; this.experienceCountBetween1and2 = 0; this.experienceCountBetween2and5 = 0; this.experienceCountBetween5and10 = 0; this.experienceCountAbove10 = 0;
      this.experienceCountBetween0and1Apprentice = 0; this.experienceCountBetween1and2Apprentice = 0; this.experienceCountBetween2and5Apprentice = 0; this.experienceCountBetween5and10Apprentice = 0; this.experienceCountAbove10Apprentice = 0;
      this.experienceCountBetween0and1Consultant = 0; this.experienceCountBetween1and2Consultant = 0; this.experienceCountBetween2and5Consultant = 0; this.experienceCountBetween5and10Consultant = 0; this.experienceCountAbove10Consultant = 0;
      this.countOfAllEmployees = 0; this.employeeInProbationAfter6MonthsCount = 0; this.apprenticeCountForDisplay = 0; this.consultantCountForDisplay = 0; this.regularCountForDisplay = 0; this.apmosysProductCountForDisplay = 0; this.apmosysProductConsultantCountForDisplay = 0;

      this.renderPlaceholderChart('Employee Status Summary', 'employeeStatus');
      this.renderPlaceholderChart('Gender Summary', 'genderSummary');
      this.renderPlaceholderChart('Age Summary', 'employeeAgeSummary');
      this.plotEmployeeExperienceColumnGraph('Employee Experience', 'employeeExperienceSummary', [], [], [], [], [], [], this.openEmployeeExperienceModalTable.bind(this));
      this.renderPlaceholderChart('Fresher - Lateral Summary', 'fresherLateralChart');
      this.renderPlaceholderChart('Billable Employee Summary', 'billableChart');
      this.renderPlaceholderChart('Employee Billable/Non-Billable Summary', 'billableChartByDepartment');
      return;
    }

    // If we reach here, data is valid. Safely extract it.
    const data = summaryData.serviceResponse[0];

    // Safely assign all count properties, with a fallback to 0
    this.maleCount = data.genderMale || 0;
    this.femaleCount = data.genderFemale || 0;
    this.otherCount = data.genderOther || 0;
    this.countBetween18and25 = data.age18to25 || 0;
    this.countBetween25and35 = data.age25to35 || 0;
    this.countBetween35and45 = data.age35to45 || 0;
    this.countAbove45 = data.ageAbove45 || 0;
    this.probationCount = data.employeeStatusProbation || 0;
    this.confirmedCount = data.employeeStatusConfirmed || 0;
    this.resignedCount = data.employeeStatusResigned || 0;
    this.inActiveCount = data.employeeStatusInActive || 0;
    this.fresherCount = data.fresherCount || 0;
    this.experienceCount = data.lateralCount || 0;
    this.tnmBillableCount = data.tnm || 0;
    this.fcBillableCount = data.fixedCost || 0;
    this.internalBillableCount = data.internalRNDProducts || 0;
    this.benchBillableCount = data.bench || 0;
    this.shadowBillableCount = data.shadow || 0;
    this.billableCount = data.billableYes || 0;
    this.nonBillableCount = data.billableNo || 0;
    this.otherBillableCount = data.billableOther || 0;
    this.experienceCountBetween0and1 = data.employeeYears0to1 || 0;
    this.experienceCountBetween1and2 = data.employeeYears1to2 || 0;
    this.experienceCountBetween2and5 = data.employeeYears2to5 || 0;
    this.experienceCountBetween5and10 = data.employeeYears5to10 || 0;
    this.experienceCountAbove10 = data.employeeYearsAbove10 || 0;
    this.experienceCountBetween0and1Apprentice = data.apprenticeYears0to1 || 0;
    this.experienceCountBetween1and2Apprentice = data.apprenticeYears1to2 || 0;
    this.experienceCountBetween2and5Apprentice = data.apprenticeYears2to5 || 0;
    this.experienceCountBetween5and10Apprentice = data.apprenticeYears5to10 || 0;
    this.experienceCountAbove10Apprentice = data.apprenticeYearsAbove10 || 0;
    this.experienceCountBetween0and1Consultant = data.consultantYear0to1 || 0;
    this.experienceCountBetween1and2Consultant = data.consultantYear1to2 || 0;
    this.experienceCountBetween2and5Consultant = data.consultantYear2to5 || 0;
    this.experienceCountBetween5and10Consultant = data.consultantYear5to10 || 0;
    this.experienceCountAbove10Consultant = data.consultantYearAbove10 || 0;
    this.countOfAllEmployees = data.totalEmployeeCountDisplay || 0;
    this.employeeInProbationAfter6MonthsCount = data.probationCountDisplay || 0;
    this.apprenticeCountForDisplay = data.apprenticeCountDisplay || 0;
    this.consultantCountForDisplay = data.consultantCountDisplay || 0;
    this.regularCountForDisplay = data.regularCountDisplay || 0;
    this.apmosysProductCountForDisplay = data.apmosysProductDisplay || 0;
    this.apmosysProductConsultantCountForDisplay = data.apmosysProductConsultantCountDisplay || 0;

    this.experienceCountBetween0and1ApmosysProduct = data.apmosysProductYear0to1 || 0;
    this.experienceCountBetween1and2ApmosysProduct = data.apmosysProductYear1to2 || 0;
    this.experienceCountBetween2and5ApmosysProduct = data.apmosysProductYear2to5 || 0;
    this.experienceCountBetween5and10ApmosysProduct = data.apmosysProductYear5to10 || 0;
    this.experienceCountAbove10ApmosysProduct = data.apmosysProductYearAbove10 || 0;
    this.experienceCountBetween0and1ApmosysProductConsultant = data.apmosysProductConsultantYear0to1 || 0;
    this.experienceCountBetween1and2ApmosysProductConsultant = data.apmosysProductConsultantYear1to2 || 0;
    this.experienceCountBetween2and5ApmosysProductConsultant = data.apmosysProductConsultantYear2to5 || 0;
    this.experienceCountBetween5and10ApmosysProductConsultant = data.apmosysProductConsultantYear5to10 || 0;
    this.experienceCountAbove10ApmosysProductConsultant = data.apmosysProductConsultantYearAbove10 || 0;
    // --- Now build and render the charts with the sanitized data ---

    // Employee Status Chart
    const employeeStatusData = [
      { name: "Probation", y: this.probationCount },
      { name: "Confirmed", y: this.confirmedCount },
      { name: "Resigned", y: this.resignedCount },
      { name: "InActive", y: this.inActiveCount }
    ];
    if (employeeStatusData.some(d => d.y > 0)) {
      this.renderPieSummaryChart('Employee Status Summary', 'employeeStatus', employeeStatusData.filter(d => d.y > 0), 'Employee Status', this.openEmployeeStatusTableModal.bind(this));
    } else {
      this.renderPlaceholderChart('Employee Status Summary', 'employeeStatus');
    }

    // Gender Chart
    const genderData = [
      { name: "male", y: this.maleCount },
      { name: "female", y: this.femaleCount },
      { name: "other", y: this.otherCount }
    ];
    if (genderData.some(d => d.y > 0)) {
        this.renderPieSummaryChart('Gender Summary', 'genderSummary', genderData.filter(d => d.y > 0), 'Employee Summary', this.openGenderSummaryModalTable.bind(this));
    } else {
        this.renderPlaceholderChart('Gender Summary', 'genderSummary');
    }

    // Age Chart
    const employeeAgeData = [
      { name: "18 to 25", y: this.countBetween18and25 },
      { name: "25 to 35", y: this.countBetween25and35 },
      { name: "35 to 45", y: this.countBetween35and45 },
      { name: "45+", y: this.countAbove45 }
    ];
    if (employeeAgeData.some(d => d.y > 0)) {
        this.renderPieSummaryChart('Age Summary', 'employeeAgeSummary', employeeAgeData.filter(d => d.y > 0), 'Employee Summary', this.openAgeSummayModalTable.bind(this));
    } else {
        this.renderPlaceholderChart('Age Summary', 'employeeAgeSummary');
    }

    // Experience Chart
    const experienceData = [
      { name: "0 to 1", employeeCount: this.experienceCountBetween0and1, apprenticeCount: this.experienceCountBetween0and1Apprentice, consultantCount: this.experienceCountBetween0and1Consultant, apmosysProductConsultantCount: this.experienceCountBetween0and1ApmosysProductConsultant, apmosysProductCount: this.experienceCountBetween0and1ApmosysProduct },
      { name: "1 to 2", employeeCount: this.experienceCountBetween1and2, apprenticeCount: this.experienceCountBetween1and2Apprentice, consultantCount: this.experienceCountBetween1and2Consultant, apmosysProductConsultantCount: this.experienceCountBetween1and2ApmosysProductConsultant, apmosysProductCount: this.experienceCountBetween1and2ApmosysProduct },
      { name: "2 to 5", employeeCount: this.experienceCountBetween2and5, apprenticeCount: this.experienceCountBetween2and5Apprentice, consultantCount: this.experienceCountBetween2and5Consultant, apmosysProductConsultantCount: this.experienceCountBetween2and5ApmosysProductConsultant, apmosysProductCount: this.experienceCountBetween2and5ApmosysProduct },
      { name: "5 to 10", employeeCount: this.experienceCountBetween5and10, apprenticeCount: this.experienceCountBetween5and10Apprentice, consultantCount: this.experienceCountBetween5and10Consultant, apmosysProductConsultantCount: this.experienceCountBetween5and10ApmosysProductConsultant, apmosysProductCount: this.experienceCountBetween5and10ApmosysProduct },
      { name: "10+", employeeCount: this.experienceCountAbove10, apprenticeCount: this.experienceCountAbove10Apprentice, consultantCount: this.experienceCountAbove10Consultant, apmosysProductConsultantCount: this.experienceCountAbove10ApmosysProductConsultant, apmosysProductCount: this.experienceCountAbove10ApmosysProduct },
    ];
    const totalExperienceCategories = experienceData.map(exp => exp.name);
    const employeeSeries = experienceData.map(exp => exp.employeeCount);
    const apprenticeSeries = experienceData.map(exp => exp.apprenticeCount);
    const consultantSeries = experienceData.map(exp => exp.consultantCount);
    const apmosysProductConsultantSeries = experienceData.map(exp => exp.apmosysProductConsultantCount);
    const apmosysProductSeries = experienceData.map(exp => exp.apmosysProductCount);
    this.plotEmployeeExperienceColumnGraph('Employee Experience', 'employeeExperienceSummary', totalExperienceCategories, employeeSeries, apprenticeSeries, consultantSeries, apmosysProductConsultantSeries, apmosysProductSeries, this.openEmployeeExperienceModalTable.bind(this));

    // Fresher/Lateral Chart
    const fresherLateralData = [
      { name: "Fresher", y: this.fresherCount },
      { name: "Lateral", y: this.experienceCount }
    ];
    if (fresherLateralData.some(d => d.y > 0)) {
        this.renderPieSummaryChart('Fresher - Lateral Summary', 'fresherLateralChart', fresherLateralData, 'Employee Summary', this.openFresherLateralModalTable.bind(this));
    } else {
        this.renderPlaceholderChart('Fresher - Lateral Summary', 'fresherLateralChart');
    }

    // Billable Chart
    const billableTypeData = [
      { name: "Yes", y: this.billableCount },
      { name: "No", y: this.nonBillableCount },
      { name: "Other", y: this.otherBillableCount }
    ];
    if (billableTypeData.some(d => d.y > 0)) {
        this.renderPieSummaryChart('Billable Employee Summary', 'billableChart', billableTypeData, 'Billable Data', this.openBillableEmployeeTableModal.bind(this));
    } else {
        this.renderPlaceholderChart('Billable Employee Summary', 'billableChart');
    }

    // Department-wise Billable Chart
    const deptWiseBillableType = [
      { name: "TNM", y: this.tnmBillableCount },
      { name: "Fixed Cost", y: this.fcBillableCount },
      { name: "InternalRNDProducts", y: this.internalBillableCount },
      { name: "Bench", y: this.benchBillableCount },
      { name: "Shadow", y: this.shadowBillableCount }
    ];
    if (deptWiseBillableType.some(d => d.y > 0)) {
        this.renderPieSummaryChart('Employee Billable/Non-Billable Summary', 'billableChartByDepartment', deptWiseBillableType.filter(d => d.y > 0), 'Department wise Billable Data', this.openDepartmentWiseBillableEmployeeTableModal.bind(this));
    } else {
        this.renderPlaceholderChart('Employee Billable/Non-Billable Summary', 'billableChartByDepartment');
    }
  }
  // --- End of Corrected renderAllPieCharts Function ---



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

  changePage(newPage: number) {
  if (newPage < 1 || newPage > this.totalPages) return;
  this.page = newPage;
  this.getAllResignedEmployees();
}

  getAllResignedEmployees() {
    const backendPage = this.page - 1;
    this.reportService.getAllResignedEmployees(backendPage, this.size, this.sortBy).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allResignEmployee = Array.isArray(response.serviceResponse.content)
          ? response.serviceResponse.content
          : [];

        this.totalResignEmployees = response.serviceResponse.totalElements;
        this.totalPages = response.serviceResponse.totalPages;
        this.allResignEmployee.forEach(employee => {
          employee.employeementId = this.displayEmploymentId(employee);
          employee.employeeType = employee.isApprenticeship === 'true'
            ? 'Apprentice'
            : this.employeeIdUtilService.resolveEmployeeType(
              employee.isApmosysProduct,
              employee.isConsultant,
              employee.isApprenticeship
            ) === 'Regular'
              ? 'On roll'
              : this.displayEmployeeType(employee);
          employee.dateOfRelieving = (employee.dateOfResign) ? moment(employee.dateOfResign).add(employee.noticePeriod, 'days') : null;

          employee.dateOfResign = (employee.dateOfResign) ? moment(employee.dateOfResign).format(AppComponent.DATE_FORMAT) : null;
          employee.dateOfRelieving = (employee.dateOfRelieving) ? moment(employee.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employee.emp360 = employee.empId;

          employee.emp360Manager = employee.managerId;
        });


      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  getPageNumbers(): number[] {
  const pages: number[] = [];
  const maxPagesToShow = 5;
  let startPage = Math.max(1, this.page - Math.floor(maxPagesToShow / 2));
  let endPage = Math.min(this.totalPages, startPage + maxPagesToShow - 1);

  startPage = Math.max(1, endPage - maxPagesToShow + 1);

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
}

  get8DaysLeaveReport() {
    this.leaveSumarryList = [];
    this.queryList = [];
    let leaveObj = new Leave();
    leaveObj.startDate = moment().subtract(8, 'd').format(this.dateFormat);
    leaveObj.endDate = moment().format(this.dateFormat);

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
          leave.emp360Manager = leave.managerId;
        });

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


    let checkLeaveStatusData = leaveStatusData.filter(data => data.y != 0);

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
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  get9DayTimesheetReport() {
    this.timsheetSummaryList = [];

    this.timesheetService.getLast9DaysTimesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timsheetSummaryList = response.serviceResponse;
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


    let checkTimesheetData = timesheetData.filter(data => data.y != 0);

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

      this.timesheetService.customQueryForTimesheetSummaryChart(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.timsheetSummaryList = response.serviceResponse;
          this.extractTimesheetReportData();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  }
//  openWorkLocationSummaryTableModal(category: any): void {
//   this.modalTitle = "Work Location: " + category;
//   this.modalSummaryList = [];

//   console.log("Fetching details for location: ", category);

//   const requestPayload = {
//     workLocation: category,
//     queryList: this.queryList || []

//   };

//   this.reportService.getWorkLocationSummaryDetails(requestPayload)
//     .pipe(first())
//     .subscribe(
//       (response: any) => {
//         if (response.serviceStatus === "Success") {
//           this.modalSummaryList = response.serviceResponse;
//           console.log("Modal Summary List from backend: ", this.modalSummaryList);

//           this.modalSummaryList.forEach(data => {
//             if (data.employeementId && !data.employeementId.startsWith('A-')) {
//               data.employeementId = "A-".concat(data.employeementId);
//             }

//             if (!data.employeeType) {
//               if (data.isConsultant === 'true') {
//                 data.employeeType = "Consultant";
//               } else if (data.isApprenticeship === 'true') {
//                 data.employeeType = "Apprentice";
//               } else {
//                 data.employeeType = "Regular";
//               }
//             }
//           });

//           this.modalRef = this.modalService.open(this.workLocationSummaryTemplate, {
//             class: 'modal-xl'
//           });

//         } else {
//           console.error('Error fetching work location summary:', response.serviceResponse);
//         }
//       },
//       (error) => {
//         console.error('API call failed:', error);
//       }
//     );
// }
  openWorkLocationSummaryTableModal(category: any): void {
    // 1. Reset Modal State
    this.modalTitle = "Work Location: " + category;
    this.modalSummaryList = [];
    this.page = 1;

    // 2. Prepare Specific Drill-down and Active Filter Payload
   const requestPayload = {
    workLocation: category,
    queryList: this.queryList || []

  };

    console.log("Sending payload for Work Location drill-down:", requestPayload);

    // 3. Call the Service
    this.reportService.getWorkLocationSummaryDetails(requestPayload).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.modalSummaryList = response.serviceResponse;
          this.modalSummaryList.forEach((emp: any) => {
              emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
                emp.totalExperience,emp.dateOfJoining);
            });

          // Process data if needed (e.g., setting employeeType)
          this.modalSummaryList.forEach(data => {
            // if (data.employeementId && !data.employeementId.startsWith('A-')) {
            //   data.employeementId = "A-".concat(data.employeementId);
            // }
            if (!data.employeeType) {
              data.employeeType = this.displayEmployeeType(data);
            }
            if (data.employeementId) {
              data.employeementId = this.displayEmploymentId(data);
            }
          });

          // this.modalTitle = `Filtered Employees at ${location} (${this.modalSummaryList.length})`;
          this.modalRef = this.modalService.open(this.workLocationSummaryTemplate, { modalDialogClass: 'modal-xl' });

        } else {
          console.error('Error fetching work location summary:', response.serviceResponse);
        }
      },
      error: (error) => {
        console.error('API call failed for work location summary:', error);
      }
    });
  }

  onSelectionChange(event: any) {
    this.loadJoinResignData();

  }



  extractDataForBillable() {

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
      deptWiseBillableType.forEach(data => console.log(data));
      this.renderPieSummaryChart('Department wise Billable/Non-Billable Employee Summary', 'billableChartByDepartment', deptWiseBillableType, 'Department wise Billable Data', this.openDepartmentWiseBillableEmployeeTableModal.bind(this));
    } else {
      this.renderPlaceholderChart('Department wise Billable/Non-Billable Employee Summary', 'billableChartByDepartment');
    }

  }

  profilestatus = "";

openDepartmentWiseEmployeeKycModalTable(deptName: any, status: any) {
    console.log("Opening department wise employee KYC modal", deptName, status);

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;

    if (!deptName || !status) {
        console.warn('Department name and status parameters are required');
        return;
    }

    this.modalTitle = "Employee(s) with KYC " + status;

    let isUserInfoUpdated: string | null = null;

    if (status === 'Pending') {
        isUserInfoUpdated = 'false';
    } else if (status === 'Completed') {
        isUserInfoUpdated = 'true';
    }

    const requestParams = {
        deptId: this.getDepartmentIdsByName(deptName),
        isUserInfoUpdated: isUserInfoUpdated
    };

    console.log("API Request Parameters:", requestParams);

    this.reportService.getDepartmentwiseEmployeeKyc(requestParams).pipe(first()).subscribe((response: any) => {
        console.log('API response:', response);

        if (response.serviceStatus === 'Success') {
            this.modalSummaryList = response.serviceResponse;
            this.modalSummaryList.forEach((emp: any) => {
              emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
                emp.totalExperience, emp.dateOfJoining);
              if (emp.employeeType) {
                emp.employeeType = this.employeeIdUtilService.formatEmployeeTypeLabel(emp.employeeType);
              }
              const formattedId = this.employeeIdUtilService.generateEmploymentId(
                emp.employeementId,
                emp.isApmosysProduct,
                emp.isConsultant,
                emp.employeeType
              );
              if (formattedId) {
                emp.employeementId = formattedId;
              }
              if (emp.dateOfJoining) {
                emp.dateOfJoining = moment(emp.dateOfJoining).format(AppComponent.DATE_FORMAT);
              }
            });

            console.log(`Filtered count for department "${deptName}" with KYC status "${status}":`, this.modalSummaryList.length);

            if (this.modalSummaryList.length > 0) {
                console.log("Sample filtered employee:", this.modalSummaryList[0]);
            }

            this.modalTitle = `Employee(s) with KYC ${status} in ${deptName} (${this.modalSummaryList.length})`;

            console.log("Final Data for Modal:", this.modalSummaryList);

            this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

        } else {
            console.error('API Error:', response.serviceResponse);
        }
    }, (error) => {
        console.error('API call failed:', error);
    });
}

  groupBy(objectArray, property) {
    return objectArray.reduce((acc, obj) => {
      const key = obj[property];
      if (!acc[key]) {
        acc[key] = [];
      }
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
            menuItems: [
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

  renderDepartmentWiseEmployeeChart(chartName: any, chartId: any, chartData: any, categories: any, labelName: any, openMod: any) {
    const employeeCounts = chartData.map((dept: any) => dept.data[0]);
    const apprenticeCounts = chartData.map((dept: any) => dept.data[1]);
    const consultantCounts = chartData.map((dept: any) => dept.data[2]);
    const apmosysProductConsultantCounts = chartData.map((dept: any) => dept.data[3] || 0);
    const apmosysProductCounts = chartData.map((dept: any) => dept.data[4] || 0);

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
          name: 'Employee',
          data: employeeCounts,
          color: '#e74c3c',
        },
        {
          name: 'Apprentice',
          data: apprenticeCounts,
          color: '#2ecc71',
        },
        {
          name: 'Consultant',
          data: consultantCounts,
          color: '#2234bd',
        },
        {
          name: 'Apmosys Product Consultant',
          data: apmosysProductConsultantCounts,
          color: '#9b59b6',
        },
        {
          name: 'Apmosys Product',
          data: apmosysProductCounts,
          color: '#f1c40f',
        },
      ],
    });
  }

  plotEmployeeExperienceColumnGraph(chartName, chartId, categories, employeeSeries, apprenticeSeries, consultantSeries, apmosysProductConsultantSeries, apmosysProductSeries, openMod) {
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
        stackLabels: {
          enabled: false,
        }
      },
      tooltip: {
        shared: true,
        backgroundColor: 'rgba(255, 255, 255, 0.75)',
        style: {
          color: '#0d0d0d',
          fontSize: '14px',
        },
        formatter: function () {
          const total = this.points.reduce((sum, point) => sum + point.y, 0);
          let tooltipHtml = `<b>${this.x}</b><br/>`;
          this.points.forEach(point => {
            if (point.y > 0) {
              tooltipHtml += `${point.series.name}: ${point.y}<br/>`;
            }
          });
          tooltipHtml += `<b>Total: ${total}</b>`;
          return tooltipHtml;
        }
      },
      plotOptions: {
        column: {
          stacking: 'normal',
          dataLabels: {
            enabled: true,
            color: '#ffffff',
            style: {
              fontWeight: 'bold',
              textOutline: 'none',
            },
            formatter: function () {
              if (this.y > 0) {
                return this.y;
              }
              return null;
            },
          },
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
                } else if (clickedSeries === 'Apmosys Product Consultant') {
                  openMod(clickedCategory, 'apmosysProductConsultant');
                } else if (clickedSeries === 'Apmosys Product') {
                  openMod(clickedCategory, 'apmosysProduct');
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
      colors: ['#176fc2', '#faa614', '#89fc72', '#9b59b6', '#e74c3c'],
      series: [
        {
          name: 'Employees',
          data: employeeSeries,
        },
        {
          name: 'Apprentices',
          data: apprenticeSeries,
        },
        {
          name: 'Consultant',
          data: consultantSeries,
        },
        {
          name: 'Apmosys Product Consultant',
          data: apmosysProductConsultantSeries,
        },
        {
          name: 'Apmosys Product',
          data: apmosysProductSeries,
        },
      ],
    });
  }

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
          text: 'No. of Employees',
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
          'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December',
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
          stacking: 'normal',
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
                let category = event.point.category;
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

  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
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
          // if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
          //   queryObj.value = "A-".concat(queryObj.value);
          // }
          // if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
          //   queryObj.value = "A-".concat(queryObj.value);
          // }
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

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
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

        // if (query.column == 'Employee Id') {
        //   query.value = query.value.split("-")[1];
        // }
      });

      console.log("updated queryList : ", emittedArray[0]);

      if (this.filterData.title == 'Filter Employee Report') {
         this.loadDashboardData(emittedArray[0]);
      }
      if (this.filterData.title == 'Filter Leave Summary') {
        this.getCustomLeaveReport(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Leave Trend Chart') {
        this.fetchLeaveTrendDetails(emittedArray[0]);
      }
      if (this.filterData.title == 'Filter Timesheet Summary') {
        this.getCustomTimesheetReport(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      if (emittedArray[1] == 'Filter Employee Report') {
        this.employeeDashboard();
        this.queryList = [];
        this.queryObj= new Query();
      }
      if (emittedArray[1] == 'Filter Leave Summary') {
        this.get8DaysLeaveReport();
      }
      if (emittedArray[1] == 'Filter Leave Trend Chart') {
        this.fetchLeaveTrendDetails(emittedArray[0]);
      }
      if (emittedArray[1] == 'Filter Timesheet Summary') {
        this.get9DayTimesheetReport();
      }
    }
  }

  exportToExcelLeaveSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Emp ID": x.employeementId ?? this.displayEmploymentId(x),
        "Employee Type": this.employeeIdUtilService.formatEmployeeTypeLabel(x.employeeType ?? this.displayEmployeeType(x)),
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
        "Emp ID": this.displayEmploymentId(x),
        "Employee Type": this.displayEmployeeType(x),
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
        "Emp ID": this.displayEmploymentId(x),
        "Employee Type": this.displayEmployeeType(x),
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
        "Emp ID": x.employeementId ?? this.displayEmploymentId(x),
        "Employee Type": this.employeeIdUtilService.formatEmployeeTypeLabel(x.employeeType ?? this.displayEmployeeType(x)),
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
        "Total Previous Work Experience": x.totalExperience,
        "Total Experience": x.totalCurrentExperience,
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
        "Emp ID": this.displayEmploymentId(x),
        "Employee Type": this.displayEmployeeType(x),
        "Employee Name": x.name,
        "Project Name": x.projectName,
        "Client Name": x.clientName,
        "Gender": x.gender,
        "Client Location": x.clientLocation,
        "Department Name": x.departmentName,
        "Manager Name": x.managerName,
        "Billable": x.billable,
        "Billable Type": x.billableType,
        "Total Previous Work Experience": x.totalExperience,
        "Total Experience": x.totalCurrentExperience


        // "Working Date": (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

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
        "Total Previous Work Experience": x.totalExperience,
        "Total Experience": x.totalCurrentExperience,
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

  exportToExcelResignedEmployee(): void {
    const onlySpecificDataArr = this.allResignEmployee.map(
      x => ({
        "Emp ID": x.employeementId,
        "Employee Type": this.displayEmployeeType(x),
        "Employee Name": x.name,
        "Department": x.departmentName,
        "Date Of Resign": (x.dateOfResign) ? moment(x.dateOfResign).format(AppComponent.DATE_FORMAT) : null,
        "Date Of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
        "Reporting To": x.managerName
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, "Resigned Employee".concat(".xlsx"))
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
    this.getAllResignedEmployees();
  }

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
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 5 to 8 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 5 && x.totalWorkingHours <= 8);
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 8 to 9 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 8 && x.totalWorkingHours <= 9);
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Employee Worked Between 9 to 10 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 9 && x.totalWorkingHours <= 10);
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Employee Worked More than 10 hour") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours > 10);
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "No Timesheet Submitted") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User" && x.pendingEodCount > 0);
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Holiday") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Holiday");
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    if (titleName == "Working On Holiday") {
      this.page = 1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Non-working");
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
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
      x.employeeType = this.displayEmployeeType(x);
    });
    this.modalSummaryList.forEach(y => {
      y.emp360 = y.empId;
      y.emp360Manager = y.managerId;
    });
    console.log('modalSummaryList --', this.modalSummaryList)
    this.modalRef = this.modalService.open(this.leaveSummaryTemplate, { modalDialogClass: 'modal-lg' });
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
    this.modalSummaryList.forEach(x => {
      if (!this.countByLegend.find(employee => employee.employmentIdAcToET == x.employmentIdAcToET)) {
        this.countByLegend.push({
          employmentIdAcToET: x.employmentIdAcToET,
          employeeType: this.displayEmployeeType(x),
          employeeName: x.employeeName,
          departmentName: x.departmentName,
          email: x.email,
          mobileNo: x.mobileNo,
          managerName: x.managerName,
          pendingEodCount: x.pendingEodCount,
          legend: x.legend,
          isConsultant: x.isConsultant,
          count: this.modalSummaryList.filter(y => y.employmentIdAcToET == x.employmentIdAcToET).length
        });
      }
    });
    this.modalSummaryList = this.countByLegend;

    for (let y of this.modalSummaryList) {
      y.emp360 = y.empId;
      y.emp360Manager = y.managerId;
    }
    this.modalRef = this.modalService.open(this.timesheetSummaryTemplate, { modalDialogClass: 'modal-xl' });
    if (legendName == 'Pending By User') {
      this.isPendingByUser = true;
    } else {
      this.isPendingByUser = false;
    }
  }

 openDepartmentWiseBillableEmployeeTableModal   (department: any) {
    console.log("Hii, billable modal call", department);

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;

    if (!department) {
        console.warn('Department parameter is required');
        return;
    }

    if (department == "TNM") {
        this.modalTitle = department + " wise Billable Employee";
    } else {
        this.modalTitle = department + " wise Non-Billable Employee";
    }

    const requestParams = {
        inActiveFlag: 0,
        employmentstatus: null,
        billable: null,
        billableType: department,
        gender: null,
        experience: null,
        lowerAge: null,
        upperAge: null,

        isConsultant: department == "TNM" ? 'false' : null,
        isApprenticeship: department == "TNM" ? 'false' : null,
        queryList: this.queryList || []

    };

    this.employeeService.getAllPieGraphListSummary(requestParams).pipe(first()).subscribe((response: any) => {
        console.log('API response:', response);
        if (response.serviceStatus == 'Success') {

            this.modalSummaryList = response.serviceResponse;
            this.modalSummaryList.forEach((emp: any) => {
              emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
                emp.totalExperience,emp.dateOfJoining);
            });

            console.log(`Filtered count for department "${department}":`, this.modalSummaryList.length);
            if (this.modalSummaryList.length > 0) {
                console.log("Sample filtered employee:", this.modalSummaryList[0]);
            }

            console.log("this.modalSummaryList   anurag ", this.modalSummaryList);


            if (department == "TNM") {
                this.modalTitle = `${department} wise Billable Employee (${this.modalSummaryList.length})`;
            } else {
                this.modalTitle = `${department} wise Non-Billable Employee (${this.modalSummaryList.length})`;
            }

            this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

        } else {
            console.error(response.serviceResponse);
        }
    });
}

openBillableEmployeeTableModal(billable: any) {
  console.log("Hii, billable modal call", billable);
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.modalSummaryList = [];
  this.data = '';
  this.resetSearch();
  this.page = 1;

  const requestParams = {
    inActiveFlag: 0,
    employmentstatus: null,
    billable: billable !== "Other" ? billable : null,
    billableType: null,
    gender: null,
    experience: null,
    lowerAge: null,
    upperAge: null,
    queryList: this.queryList || []
  };

  if (billable === "Other") {
    requestParams.billable = "Yes_No_Null";
    requestParams.billableType = null;
  }

  this.employeeService.getAllPieGraphListSummary(requestParams).pipe(first()).subscribe((response: any) => {
    console.log('API response:', response);
    if (response.serviceStatus == 'Success') {
      this.modalSummaryList = response.serviceResponse;
       this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience,emp.dateOfJoining);
        });
      const traingBenchEmployees = this.modalSummaryList.filter(
        x => x.departmentName === 'Traing' && x.billableType === 'Bench'
      );
      console.log('Employees in Traing department with Bench billableType:', traingBenchEmployees.length, traingBenchEmployees);

      console.log("modelsheet" + " " + this.modalSummaryList);
      this.modalTitle = `Employee In ${billable} (${this.modalSummaryList.length})`;
      this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

    } else {
      console.error(response.serviceResponse);
    }
  });
}

//   openTotalCountModal(title: any) {
//   this.sortColumn = [];
//   this.sortColumnType = [];
//   this.sortDirection = '';
//   this.data = ''
//   this.modalSummaryList = [];
//   this.resetSearch();

//   let dateToday = moment().format(this.dateFormat);
//   this.page = 1;
//   this.modalTitle = title;

//   let requestBody: any = {
//     apprentice: false,
//     consultant: false,
//     regular: false,
//     probation: false,
//     allEmp: false,
//     query:this.queryList || [],
//   };

//   switch(title) {
//     case 'All Active Employee':
//       requestBody.allEmp = true;
//       break;
//     case 'Employee In Probation(After 6 months)':
//       requestBody.probation = true;
//       break;
//     case 'Apprentice Count':
//       requestBody.apprentice = true;
//       break;
//     case 'Consultant Count':
//       requestBody.consultant = true;
//       break;
//     case 'Regular Count':
//       requestBody.regular = true;
//       break;
//     default:
//       requestBody.allEmp = true;
//   }

//   this.reportService.getEmployeeDetailsByEmploymentType(requestBody).subscribe({
//     next: (response: any) => {
//       if (response.serviceStatus === 'Success') {
//         this.modalSummaryList = response.serviceResponse;

//       } else {
//         this.modalSummaryList = [];
//       }

//       this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
//     },
//     error: (error: any) => {
//       console.error('Error fetching employee details:', error);
//       this.modalSummaryList = [];
//     }
//   });
// }
openTotalCountModal(title: any) {
    // 1. Reset Modal State
    this.modalSummaryList = [];
    this.page = 1;
    this.resetSearch();
    this.modalTitle = title;

    // 2. Prepare Specific Drill-down Parameters (the boolean flags)
    let requestBody:any= {
      apprentice: false,
      consultant: false,
      regular: false,
      apmosysProductConsultant: false,
      probation: false,
      allEmp: false,
      isApmosysProduct: 'false',
    };

    switch(title) {
      case 'All Active Employee':
        requestBody.allEmp = true;
        break;
      case 'Employee In Probation(After 6 months)':
        requestBody.probation = true;
        break;
      case 'Apprentice Count':
        requestBody.apprentice = true;
        break;
      case 'Consultant Count':
        requestBody.consultant = true;
        break;
      case 'Regular Count':
        requestBody.regular = true;
        break;
      case 'Apmosys Product Count':
        requestBody.isApmosysProduct = 'true';
        break;
      case 'APMOSYS Product Consultant Count':
        requestBody.apmosysProductConsultant = true;
        break;
      default:
        requestBody.allEmp = true;
    }

    // 3. Combine with Active Dashboard Filters
    const payload: any= {
        ...requestBody,
        queryList: this.queryList || [] // **The crucial part**
    };

    console.log("Sending payload for Employment Type drill-down:", payload);

    // 4. Call the Service
    this.reportService.getEmployeeDetailsByEmploymentType(payload).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.modalSummaryList = response.serviceResponse;
          this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience, emp.dateOfJoining);
          if (!emp.employeeType) {
            emp.employeeType = this.displayEmployeeType(emp);
          } else {
            emp.employeeType = this.employeeIdUtilService.formatEmployeeTypeLabel(emp.employeeType);
          }
          if (emp.employeementId) {
            emp.employeementId = this.displayEmploymentId(emp);
          }
        });
          this.modalTitle = `${title} (${this.modalSummaryList.length} records)`;
        } else {
          this.modalSummaryList = [];
          this.modalTitle = `${title} (0 records)`;
        }
        this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
      },
      error: (error: any) => {
        console.error('Error fetching employee details by type:', error);
        this.modalSummaryList = [];
        this.modalTitle = `Error loading: ${title}`;
        this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
      }
    });
  }


  openEmployeeStatusTableModal(status: any) {

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;
    this.modalTitle = "Employee In " + status;

    if (!status) {
      console.warn('Status parameter is required');
      return;
    }

    const requestParams = {
      inActiveFlag: status === 'InActive' ? 1 : 0,
      employmentstatus: status,
      billable: null,
      billableType: null,
      gender: null,
      experience: null,
      lowerAge: null,
      upperAge: null,
      queryList: this.queryList || []
    };

    this.employeeService.getAllPieGraphListSummary(requestParams).pipe(first()).subscribe((response: any) =>{
      console.log('API response:', response);
      if(response.serviceStatus == 'Success'){

          this.modalSummaryList = response.serviceResponse ;
          this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience,emp.dateOfJoining);
        });

          console.log('Modal summary list:', this.modalSummaryList);
          this.modalTitle = `Employee In ${status} (${this.modalSummaryList.length})`;
          this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

      }else{
        console.error(response.serviceResponse)
      }

    });
  }


openGenderSummaryModalTable(gender: any) {
    // 1. Reset the modal's state
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.data = '';
    this.modalSummaryList = [];
    this.resetSearch();
    this.page = 1;

    // 2. Log current active filters for debugging
    console.log("Current active filters:", this.activeFilters);

    // 3. Define the specific parameters for THIS drill-down action
    const drilldownParams: Partial<PieParamPayload> = {
      inActiveFlag: 0, // 0 = Active employees
      gender: gender,
      // Set other parameters to null to not interfere with custom filters
      employmentstatus: null,
      billable: null,
      billableType: null,
      experience: null,
      lowerAge: null,
      upperAge: null,
      queryList: this.queryList || []
    };

    // 4. Create the final payload combining drill-down params with active filters
    const payload: PieParamPayload = {
      ...drilldownParams,
      // Ensure we're passing the current dashboard filters
      queryList: this.queryList|| []
       // Fallback to empty array if null
    };

    console.log("Sending payload for Gender drill-down:", payload);

    // 5. Call the service
    this.employeeService.getAllPieGraphListSummary(payload).pipe(first()).subscribe({
      next: (response: any) => {
        console.log('API response for Gender drill-down:', response);

        if (response.serviceStatus === 'Success') {
          this.modalSummaryList = response.serviceResponse;

          this.modalSummaryList.forEach((emp: any) => {
            emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
              emp.totalExperience,emp.dateOfJoining);
          });

          // Create a more descriptive title
          let filterDescription = '';
          if (this.activeFilters && this.activeFilters.length > 0) {
            filterDescription = ` (with ${this.activeFilters.length} active filters)`;
          }

          this.modalTitle = `${gender} Employees${filterDescription} (${this.modalSummaryList.length} records)`;
          this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
        } else {
          console.error("API Error on drill-down:", response.serviceResponse);
          // Handle error case
          this.modalTitle = `Error loading ${gender} employees`;
          this.modalSummaryList = [];
        }
      },
      error: (err) => {
        console.error("HTTP Error on drill-down:", err);
        this.modalTitle = `Error loading ${gender} employees`;
        this.modalSummaryList = [];
      }
    });
}

 openAgeSummayModalTable(age: any) {

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;

    this.modalTitle = "Employee Age " + age;

    console.log("CZurrent active filters:", this.queryList);


    if (!age) {
      console.warn('Age parameter is required');
      return;
    }
    let lowerAge = null;
    let upperAge = null;

    if (age === "18 to 25") {
      lowerAge = 18;
      upperAge = 25;
    } else if (age === "25 to 35") {
      lowerAge = 26;
      upperAge = 35;
    } else if (age === "35 to 45") {
      lowerAge = 36;
      upperAge = 45;
    } else if (age === "45+") {
      lowerAge = 46;
      upperAge = null;
    }


    const requestParams = {
      inActiveFlag: 0,
      employmentstatus: null,
      billable: null,
      billableType: null,
      gender: null,
      experience: null,
      lowerAge: lowerAge,
      upperAge: upperAge,
      queryList: this.queryList || []
    };
    this.employeeService.getAllPieGraphListSummary(requestParams).pipe(first()).subscribe((response: any) => {
      console.log('API response:', response);
      if (response.serviceStatus == 'Success') {

        this.modalSummaryList = response.serviceResponse;
        this.modalSummaryList.forEach((emp: any) => {
            emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
              emp.totalExperience,emp.dateOfJoining);
          });

        if (age === "18 to 25") {
          this.modalTitle = `Employee Age Between 18 to 25 (${this.modalSummaryList.length})`;
        } else if (age === "25 to 35") {
          this.modalTitle = `Employee Age Between 25 to 35 (${this.modalSummaryList.length})`;
        } else if (age === "35 to 45") {
          this.modalTitle = `Employee Age Between 35 to 45 (${this.modalSummaryList.length})`;
        } else if (age === "45+") {
          this.modalTitle = `Employee Age Above 45 (${this.modalSummaryList.length})`;
        }

        this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

openDepartmentWiseEmployeeModalTable(pointName: any, seriesName: any) {
    console.log("Opening department wise employee modal", pointName, seriesName);

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;

    if (!pointName || !seriesName) {
        console.warn('Department and series parameters are required');
        return;
    }

    this.modalTitle = seriesName + " in " + pointName;

    let employeeType = null;

    if (seriesName === 'Employee') {
        employeeType = 'regular';
    } else if (seriesName === 'Apprentice') {
        employeeType = 'apprentice';
    } else if (seriesName === 'Consultant') {
        employeeType = 'consultant';
    } else if (seriesName === 'Apmosys Product Consultant') {
        employeeType = 'apmosys_product_consultant';
    } else if (seriesName === 'Apmosys Product') {
        employeeType = 'apmosys_product';
    }

    const requestParams = {
        deptId: this.getDepartmentIdsByName(pointName),
        employeeType: employeeType,
    };

    console.log("API Request Parameters:", requestParams);

    this.reportService.getDepartmentwiseEmployee(requestParams).pipe(first()).subscribe((response: any) => {
        console.log('API response:', response);

        if (response.serviceStatus === 'Success') {
            this.modalSummaryList = response.serviceResponse;
            this.modalSummaryList.forEach((emp: any) => {
              emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
                emp.totalExperience, emp.dateOfJoining);
              if (emp.employeementId) {
                emp.employeementId = this.displayEmploymentId(emp);
              }
              if (emp.employeeType) {
                emp.employeeType = this.employeeIdUtilService.formatEmployeeTypeLabel(emp.employeeType);
              } else {
                emp.employeeType = this.displayEmployeeType(emp);
              }
            });

            console.log(`Filtered count for department "${pointName}" and series "${seriesName}":`, this.modalSummaryList.length);

            if (this.modalSummaryList.length > 0) {
                console.log("Sample filtered employee:", this.modalSummaryList[0]);
            }

            this.modalSummaryList.forEach((employee) => {
                employee.dateOfJoining = employee.dateOfJoining ?
                    moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            });

            this.modalTitle = `${seriesName} in ${pointName} (${this.modalSummaryList.length})`;

            console.log("Final Data for Modal:", this.modalSummaryList);

            this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

        } else {
            console.error('API Error:', response.serviceResponse);
        }
    }, (error) => {
        console.error('API call failed:', error);
    });
}

  openEmployeeExperienceModalTable(pointName: string, type: string) {
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.data = '';
  this.modalSummaryList = [];
  this.resetSearch();

  let lowerValue = 0;
  let upperValue = 1000;

  switch(pointName) {
    case "0 to 1":
      lowerValue = 0;
      upperValue = 1;
      break;
    case "1 to 2":
      lowerValue = 1;
      upperValue = 2;
      break;
    case "2 to 5":
      lowerValue = 2;
      upperValue = 5;
      break;
    case "5 to 10":
      lowerValue = 5;
      upperValue = 10;
      break;
    case "10+":
      lowerValue = 10;
      upperValue = 1000;
      break;
    default:
      break;
  }

  let employeeType = '';
  if (type === 'employee') employeeType = 'regular';
  else if (type === 'apprentice') employeeType = 'apprentice';
  else if (type === 'consultant') employeeType = 'consultant';
  else if (type === 'apmosysProduct') employeeType = 'apmosys_product';
  else if (type === 'apmosysProductConsultant') employeeType = 'apmosys_product_consultant';

  const payload = {
    employeeType: employeeType,
    lowerValue: lowerValue,
    upperValue: upperValue,
    queryList: this.queryList || []

  }

  this.reportService.getEmployeesByExperience(payload).pipe(first()).subscribe(
    (response: any) => {
      if(response.serviceStatus == 'Success') {
      this.modalSummaryList = response.serviceResponse;
      this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience, emp.dateOfJoining);
          if (emp.employeementId) {
            emp.employeementId = this.displayEmploymentId(emp);
          }
          if (emp.employeeType) {
            emp.employeeType = this.employeeIdUtilService.formatEmployeeTypeLabel(emp.employeeType);
          }
        });

      this.page = 1;
      const typeLabel = type === 'apmosysProductConsultant' ? 'Apmosys Product Consultant' : this.capitalizeFirstLetter(type);
      this.modalTitle = `${typeLabel}(s) with ${pointName} YOE`;

      this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
      }
    },
    error => {
      console.error('Error fetching employee experience data', error);
    }
  );
}

capitalizeFirstLetter(text: string) {
  if (!text) return text;
  return text.charAt(0).toUpperCase() + text.slice(1);
}



   openFresherLateralModalTable(pointName: any, status: any) {

    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.modalSummaryList = [];
    this.data = '';
    this.resetSearch();
    this.page = 1;
    this.modalTitle = "Employee(s) " + pointName;

    if (!pointName) {
      console.warn('PointName parameter is required');
      return;
    }

    let experienceValue = null;
    if (pointName === 'Lateral') {
      experienceValue = 'Experienced';
    } else if (pointName === 'Fresher') {
      experienceValue = 'Fresher';
    } else {
      experienceValue = pointName;
    }

    const requestParams = {
      inActiveFlag: status === 'InActive' ? 1 : 0,
      employmentstatus: null,
      billable: null,
      billableType: null,
      gender: null,
      experience: experienceValue,
      lowerAge: null,
      upperAge: null,
      queryList: this.queryList || []
    };
    this.employeeService.getAllPieGraphListSummary(requestParams).pipe(first()).subscribe((response: any) => {
      console.log('API response:', response);
      if (response.serviceStatus == 'Success') {

        this.modalSummaryList = response.serviceResponse;
          this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience,emp.dateOfJoining);
        });
        console.log('Modal summary list:', this.modalSummaryList);
        this.modalTitle = `Employee(s) ${pointName} (${this.modalSummaryList.length})`;
        this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  openEmployeeJoinResignModalTable(category: string, name: string) {
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.data = '';
  this.modalSummaryList = [];
  this.resetSearch();

  const request = {
    monthName: category,
    employeeType: name.toLowerCase(),
    year: this.selectedYear || moment().year(),
    queryList: this.queryList || []
  };

  this.reportService.getJoinVsResignEmployeeDetails(request).subscribe(
    (response: any) => {
      console.log('Response from getJoinVsResignEmployeeDetails service:', response);

      if (response.serviceStatus === 'Success' && response.serviceResponse) {
        let modalTableList = response.serviceResponse;
        modalTableList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience,emp.dateOfJoining);
        });

        this.page = 1;
        this.modalTitle = `Employee(s) ${name} in ${category} ${this.selectedYear}`;

        if (name === 'Resigned' || name === 'resign') {
          modalTableList = modalTableList.filter(emp => emp.employmentstatus === 'InActive');
        }

        this.modalSummaryList = modalTableList.map(emp => ({
          employeementId: emp.employeementId,
          employeeType: emp.employeeType,
          name: emp.name,
          experience: emp.experience,
          departmentName: emp.departmentName,
          email: emp.email,
          managerName: emp.managerName,
          billable: emp.billable,
          billableType: emp.billableType,
          projectName: emp.projectName,
          clientName: emp.clientName,
          dateOfJoining: emp.dateOfJoining,
          dateOfRelieving: emp.dateOfRelieving || null,
          mobileNo: emp.mobileNo,
          employmentstatus: emp.employmentstatus,
          totalExperience: emp.totalExperience,
          totalCurrentExperience: emp.totalCurrentExperience,
          gender: emp.gender,
          workLocation: emp.workLocation,
          age: emp.age,
          profileKycStatus: emp.profileKycStatus,
          empId: emp.empId,
          managerId: emp.managerId
        }));

        console.log('Filtered Employees for', name, this.modalSummaryList);
        this.modalRef = this.modalService.open(this.employeeSummaryTemplate, { modalDialogClass: 'modal-xl' });

      } else {
        console.error('Invalid response from service:', response);
        this.modalSummaryList = [];
      }
    },
    error => {
      console.error('Error calling getJoinVsResignEmployeeDetails service:', error);
      this.modalSummaryList = [];
    }
  );
}

  findAllDepartment() {
    this.allDepartmentList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDepartmentList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }



openDepartmentWiseBillableEmployeeModalTable(deptName: any, billableType: any) {
  const billableTypeList = ['Shadow', 'Bench', 'Fixed Cost', 'TNM', 'InternalRNDProducts'];

  this.data = '';
  this.modalSummaryList = [];
  this.resetSearch();
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.page = 1;
  this.modalTitle = `Employee(s) with ${billableType} Billable Type`;

  if (!billableTypeList.includes(billableType)) {
    console.error('Invalid billable type');
    return;
  }

  this.isLoading = true;

  const requestPayload = {
    deptId: this.getDepartmentIdsByName(deptName),
    billableType: billableType
  };

  console.log('Request payload:', requestPayload);

  this.reportService.getEmployeeDetailsByDepartmentAndBillableType(requestPayload)
    .subscribe({
      next: (response: any) => {
        this.isLoading = false;
        console.log('API Response:', response);

        if (response.serviceStatus === 'Success' && response.serviceResponse) {
          this.modalSummaryList = response.serviceResponse.filter(
            (employee: any) => employee.departmentName === deptName
          );

          this.modalSummaryList.forEach((emp: any) => {
          emp.totalCurrentExperience = this.employeeService.calculateTotalExperience(
            emp.totalExperience,emp.dateOfJoining);
        });

          this.modalRef = this.modalService.open(this.employeeSummaryTemplate, {
            modalDialogClass: 'modal-xl'
          });
        } else {
          console.warn('No employee data found or API returned error:', response);
          this.modalSummaryList = [];
          this.modalRef = this.modalService.open(this.employeeSummaryTemplate, {
            modalDialogClass: 'modal-xl'
          });
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error fetching employee details:', error);
        this.modalSummaryList = [];
      }
    });
}
private getDepartmentIdsByName(deptName: string): number[] {
  const department = this.allDepartmentList.find(dept => dept.name === deptName);
  console.log("Department found:", department);
  return department ? [department.deptId] : [];

}

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

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
    this.page=1;
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
    this.kycChartData[0].data = [];
    this.kycChartData[1].data = [];
    this.departmentCategories = [];

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

    this.departmentKycData = Array.from(departmentMap.entries())
      .map(([departmentName, counts]) => ({
        departmentName,
        pending: counts.pending,
        completed: counts.completed
      }))
      .sort((a, b) => (b.pending + b.completed) - (a.pending + a.completed));

    this.departmentKycData.forEach(dept => {
      this.departmentCategories.push(dept.departmentName);
      this.kycChartData[0].data.push(dept.pending);
      this.kycChartData[1].data.push(dept.completed);
    });

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

    if (this.queryObj) {
          this.reportService.customgetJoinVsResignCount(this.queryObj, selectedYear).pipe(first()).subscribe((response: any) => {
          this.processJoinResignCount(response);
      },
      (error) => {
        console.error('API call failed:', error);
      }
    );
    }else {
    this.reportService.getJoinVsResignCount(selectedYear).pipe(first()).subscribe((response: any) => {
          this.processJoinResignCount(response);
      },
      (error) => {
        console.error('API call failed:', error);
      }
    );
    }
  }

  // processJoinResignCount(response: any){
  //           const monthlyData = response?.serviceResponse || [];

  //       const joiningRegular = Array(12).fill(0);
  //       const joiningApprentice = Array(12).fill(0);
  //       const joiningConsultant = Array(12).fill(0);
  //       const resigning = Array(12).fill(0);

  //       const monthIndexMap: { [key: string]: number } = {
  //         January: 0, February: 1, March: 2, April: 3, May: 4, June: 5,
  //         July: 6, August: 7, September: 8, October: 9, November: 10, December: 11
  //       };

  //       monthlyData.forEach(item => {
  //         const idx = monthIndexMap[item.monthName];
  //         if (idx !== undefined) {
  //           joiningRegular[idx] = item.regularCount || 0;
  //           joiningApprentice[idx] = item.apprenticeCount || 0;
  //           joiningConsultant[idx] = item.consultantCount || 0;
  //           resigning[idx] = item.resignCount || 0;
  //         }
  //       });

  //       const chartData = [
  //         {
  //           name: 'Regular',
  //           data: joiningRegular,
  //           stack: 'joined',
  //           color: '#1f77b4'
  //         },
  //         {
  //           name: 'apprenticeship',
  //           data: joiningApprentice,
  //           stack: 'joined',
  //           color: '#ff7f0e'
  //         },
  //         {
  //           name: 'consultant',
  //           data: joiningConsultant,
  //           stack: 'joined',
  //           color: '#2ca02c'
  //         },
  //         {
  //           name: 'resign',
  //           data: resigning,
  //           stack: 'resigned',
  //           color: '#d62728'
  //         }
  //       ];

  //       this.renderMultiBarChart('Employee Join VS Resign', 'employeeJoinAndResign', chartData, 'Employee', this.openEmployeeJoinResignModalTable.bind(this));
  // }

  getAllPieChartCount() {
    this.reportService.getAllPieChartCount().pipe(first()).subscribe((response: any) => {
      this.renderAllPieCharts(response);
    });
  }

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

  getAllEmployeeCountDepartmentWise() {
    this.reportService.getAllEmployeeCountDepartmentWise().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.processDepartmentWiseCount(response.serviceResponse);

      } else {
        console.error(" Failed to get department-wise employee count:", response.serviceResponse);
      }
    });
  }

  processDepartmentWiseCount(response: any) {
            const rawData = response;
        const departmentMap: { [key: string]: any } = {};

        rawData.forEach(([departmentName, type, count]) => {
          if (!departmentName) return;

          if (!departmentMap[departmentName]) {
            departmentMap[departmentName] = {
              departmentName,
              employeeCount: 0,
              apprenticeCount: 0,
              consultantCount: 0,
              ApmosysProductConsultantCount: 0,
              ApmosysProductCount: 0
            };
          }

          if (type === "Employee") {
            departmentMap[departmentName].employeeCount += count;
          } else if (type === "Apprentice") {
            departmentMap[departmentName].apprenticeCount += count;
          } else if (type === "Consultant") {
            departmentMap[departmentName].consultantCount += count;
          } else if (type === "ApmosysProductConsultant") {
            departmentMap[departmentName].ApmosysProductConsultantCount += count;
          } else if (type === "ApmosysProduct") {
            departmentMap[departmentName].ApmosysProductCount += count;
          }
        });

        const departmentData = Object.values(departmentMap);

        departmentData.sort((a: any, b: any) => b.employeeCount - a.employeeCount);

        this.departmentWiseEmployeeData = departmentData.map(dept => ({
          name: dept.departmentName,
          data: [
            dept.employeeCount,
            dept.apprenticeCount,
            dept.consultantCount,
            dept.ApmosysProductConsultantCount,
            dept.ApmosysProductCount
          ]
        }));

        this.departmentWiseEmployeeCategories = departmentData.map(dept => dept.departmentName);

        this.renderDepartmentWiseEmployeeChartWrapper();
        console.log("departmentWiseEmployeeData:", this.departmentWiseEmployeeData);
        console.log("departmentWiseEmployeeCategories:", this.departmentWiseEmployeeCategories);
  }

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
  const selectedDeptIds: number[] = [];

  this.reportService.getDepartmentWiseBillableNonBillableSummary(selectedDeptIds).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      const rawData = response.serviceResponse;

      const billableChartData = this.prepareBillableChartDataBillabe(rawData);
      this.renderEmployeeBillableSummaryChartWrapper(billableChartData);

    } else {
      console.error("Error fetching employee billable summary data:", response.serviceResponse);
      this.renderPlaceholderChart(
        'Department wise Billable/Non-Billable Employee Summary',
        'billableEmployeeSummary'
      );
    }
  });
}


prepareBillableChartDataBillabe(rawData: any[]): any[] {
  const departmentTotalCountMap = new Map<string, number>();
  const departmentSet = new Set<string>();
  const typeSet = new Set<string>();

  rawData.forEach(([dept, type, count]) => {
    if (!dept || !type) return;

    departmentSet.add(dept);
    typeSet.add(type);

    departmentTotalCountMap.set(dept, (departmentTotalCountMap.get(dept) || 0) + count);
  });

  const sortedDepartments = Array.from(departmentSet).sort((a, b) => {
    const aCount = departmentTotalCountMap.get(a) || 0;
    const bCount = departmentTotalCountMap.get(b) || 0;
    return bCount - aCount;
  });

  const typeList = Array.from(typeSet);
  const typeToDataMap = new Map<string, number[]>();

  typeList.forEach(type => {
    typeToDataMap.set(type, new Array(sortedDepartments.length).fill(0));
  });

  rawData.forEach(([dept, type, count]) => {
    const deptIndex = sortedDepartments.indexOf(dept);
    if (deptIndex !== -1 && type) {
      const dataArr = typeToDataMap.get(type);
      if (dataArr) {
        dataArr[deptIndex] += count;
      }
    }
  });

  this.billableChartCategories = sortedDepartments;

  return Array.from(typeToDataMap.entries()).map(([type, data]) => ({
    name: type,
    data: data,
    stack: 'base'
  }));
}

renderEmployeeBillableSummaryChartWrapper(billableChartData: any[]) {
  this.renderStackBarChart(
    'Department wise Billable/Non-Billable Employee Summary',
    'billableEmployeeSummary',
    billableChartData,
    this.billableChartCategories,
    'Employee',
    this.openDepartmentWiseBillableEmployeeModalTable.bind(this)
  );
}

  getLeaveTrendDetails() {
  this.leaveSumarryList = [];

  this.reportService.getLeaveTrendDetails().pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.leaveTrendAnalysisList = response.serviceResponse;
      this.extractLeaveTrendAnalysisData();
    } else {
      console.error(response.serviceResponse);
    }
  });
}

extractLeaveTrendAnalysisData() {
  let chartData = [];
  let formattedDateRange = [];

  if (this.leaveTrendAnalysisList && this.leaveTrendAnalysisList.length > 0) {

    const leaveTypes = [...new Set(this.leaveTrendAnalysisList.map(item => item.typeOfLeave))];
    const uniqueDates = [...new Set(this.leaveTrendAnalysisList.map(item => item.leaveDate))];
    const sortedDates = uniqueDates.sort((a, b) => new Date(a).getTime() - new Date(b).getTime());

    formattedDateRange = sortedDates.map(date => moment(date).format('MMM DD'));

    this.previousLeaveTypes = leaveTypes;
    this.previousFormattedDateRange = formattedDateRange;

    chartData = leaveTypes.map(leaveType => {
      return {
        type: 'line',
        name: leaveType,
        data: sortedDates.map(date => {
          const dayData = this.leaveTrendAnalysisList.find(item =>
            item.typeOfLeave === leaveType && item.leaveDate === date
          );
          return dayData ? dayData.totalLeaveDays : 0;
        })
      };
    });

  } else {

    console.log("No data found. Re-rendering with previous structure and zero values.");

    formattedDateRange = this.previousFormattedDateRange;

    chartData = this.previousLeaveTypes.map(leaveType => {
      return {
        type: 'line',
        name: leaveType,
        data: new Array(this.previousFormattedDateRange.length).fill(0)
      };
    });
  }

  console.log("Rendering chart with Date Range:", formattedDateRange);
  console.log("Rendering chart with Data:", chartData);

  this.renderLineGraphChart(
    'Leave Trend Analysis Graph',
    'leaveTrendAnalysis',
    chartData,
    'No. of Leaves',
    formattedDateRange,
    this.openLeaveAnalysisTableModel.bind(this)
  );
}

renderLineGraphChart(title: string, containerId: string, seriesData: any[], yAxisTitle: string, categories: string[], clickCallback?: Function) {
  const chartOptions: Highcharts.Options = {
    chart: {
      type: 'line',
      height: 300
    },
    title: {
      text: title,
      style: {
          color: '#000000',
          fontWeight: 'bold'
        }
    },
    xAxis: {
      categories: categories,
      labels: {
          overflow: 'justify',
          style: {
            color: '#000000',
            fontWeight: 'bold'
          }
      }
    },
    yAxis: {
      title: {
        text: yAxisTitle,
        style: {
            color: '#000000',
            fontWeight: 'bold'
          }
      },
      min: 0,
      labels: {
          overflow: 'justify',
          style: {
            color: '#000000',
            fontWeight: 'bold'
          }
        }
    },
    tooltip: {
      shared: true,
      formatter: function() {
        let tooltipText = `<b>${this.x}</b><br/>`;
        this.points?.forEach(point => {
          tooltipText += `<span style="color:${point.color}">${point.series.name}</span>: <b>${point.y}</b> days<br/>`;
        });
        return tooltipText;
      }
    },
    plotOptions: {
      line: {
        dataLabels: {
          enabled: false
        },
        enableMouseTracking: true,
        marker: {
          enabled: true,
          radius: 4
        }
      },
      series: {
        cursor: 'pointer',
        label: {
            connectorAllowed: false
          },
        point: {
          events: {
            click: function() {
              if (clickCallback) {
                clickCallback(this.category, this.series.name, this.y);
              }
            }
          }
        }
      }
    },
    credits: {
        enabled: false,
    },
    legend: {
      enabled: true,
      align: 'center',
      verticalAlign: 'bottom'
    },
    series: seriesData
  };

  Highcharts.chart(containerId, chartOptions);
}

convertDateFormat(dateStr: string): string {
  try {
    if (dateStr.includes('-') && dateStr.length === 10) {
      return dateStr;
    }

    const currentYear = new Date().getFullYear();
    const parsedDate = moment(`${dateStr} ${currentYear}`, 'MMM DD YYYY');

    if (!parsedDate.isValid()) {
      console.error('Invalid date format:', dateStr);
      return dateStr;
    }

    return parsedDate.format('YYYY-MM-DD');
  } catch (error) {
    console.error('Error converting date format:', error);
    return dateStr;
  }
}

// openLeaveAnalysisTableModel(date: string, leaveType: string, value: number) {
//   console.log(`Clicked on ${leaveType} for ${date}: ${value} days`);

//   const formattedDate = this.convertDateFormat(date);

//   const request = {
//     fetchDate: formattedDate,
//     typeOfLeave: leaveType,
//     queryList: this.queryList || []
//   };

//   console.log('Request payload:', request);

//   this.leaveService.getLeaveTrendAnalysis(request).pipe(first()).subscribe((response: any) => {
//     if (response.serviceStatus == "Success") {
//       const detailedLeaveData = response.serviceResponse;
//       console.log('Detailed leave data:', detailedLeaveData);

//       this.modalTitle = `${leaveType} Details - ${moment(date).format('MMM DD, YYYY')}`;
//       this.modalSummaryList = detailedLeaveData;
//       this.modalSummary = {
//         date: date,
//         leaveType: leaveType,
//         totalDays: value,
//         employeeCount: detailedLeaveData.length
//       };

//       this.modalRef = this.modalService.open(this.leaveSummaryTemplate, { modalDialogClass: 'modal-xl' });
//     } else {
//       console.error('Error fetching detailed leave analysis:', response.serviceResponse);
//     }
//   }, (error) => {
//     console.error('Service call failed:', error);
//   });
// }
openLeaveAnalysisTableModel(date: string, leaveType: string, value: number) {
    console.log(`Drilling down on ${leaveType} for date: ${date}`);

    // 1. Prepare Specific Drill-down Parameters
    const formattedDate = this.convertDateFormat(date);

    // 2. Combine with Active Dashboard Filters
   const request = {
    fetchDate: formattedDate,
    typeOfLeave: leaveType,
    queryList: this.queryList || []
  };

    console.log('Sending payload for Leave Trend drill-down:', request);

    // 3. Call the Service
    this.leaveService.getLeaveTrendAnalysis(request).pipe(first()).subscribe({
        next: (response: any) => {
            if (response.serviceStatus == "Success") {
                const detailedLeaveData = response.serviceResponse;
                console.log('Detailed leave data from drill-down:', detailedLeaveData);

                this.modalTitle = `${leaveType} Details - ${moment(date, 'MMM DD').format('MMM DD, YYYY')}`;
                this.modalSummaryList = detailedLeaveData;
                this.modalSummaryList.forEach((emp: any) => {
                  if (emp.employeeType) {
                    emp.employeeType = this.employeeIdUtilService.formatEmployeeTypeLabel(emp.employeeType);
                  }
                });

                this.modalRef = this.modalService.open(this.leaveSummaryTemplate, { modalDialogClass: 'modal-xl' });
            } else {
                console.error('Error fetching detailed leave analysis:', response.serviceResponse);
            }
        },
        error: (error) => {
            console.error('Service call failed for leave trend drill-down:', error);
        }
    });
  }

getEmployeeWorkLocation(): void {
  this.reportService.getWorkLocationDetails().pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      this.renderWorkLocationData(response.serviceResponse);

    } else {
      console.error('Error fetching work location details:', response.serviceResponse);
    }
  }, (error) => {
    console.error('API call failed:', error);
  });
}

renderWorkLocationData(workLocationData: any[]): void {
  if (!workLocationData || workLocationData.length === 0) {
    this.renderColumnBarSummaryChartForWorkLocation('Employee Work Location Summary', 'employeeWorkLocationSummary', [], [], 'employee', this.openWorkLocationSummaryTableModal.bind(this));
    return;
  }

  // Your existing logic to process and render the chart
  const employeeWorkLocationChartData = workLocationData.map((item: any) => ([
    item.clientLocation,
    item.employeeCOUNT
  ]));

  const employeeWorkLocationCategories = workLocationData.map((item: any) => item.clientLocation);

  this.renderColumnBarSummaryChartForWorkLocation(
    'Employee Work Location Summary',
    'employeeWorkLocationSummary',
    employeeWorkLocationChartData,
    employeeWorkLocationCategories,
    'employee',
    this.openWorkLocationSummaryTableModal.bind(this)
  );
}

fetchGraphSummary(queryObjList: any): void {
  let queryObj = new Query();
    queryObj.queryList = queryObjList;

  this.reportService.customgetGraphEmployeeSummary(queryObj).pipe(first()).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === "Success" && response.serviceResponse.length > 0) {
        this.summaryData = response.serviceResponse[0];
        console.log('Fetched graph summary:', this.summaryData);
      } else {
        console.error('API Error or no data:', response.serviceResponse);
      }
    },
    error: (err) => {
      console.error('HTTP Error:', err);
    }
  });
}

}


function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
