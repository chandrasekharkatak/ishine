import { Directive, AfterViewInit, Component, ElementRef, TemplateRef, ViewChild, Input, HostListener } from '@angular/core';
import { FormControl, NgModel } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import * as Highcharts from 'highcharts';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first, map, startWith } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { GetEmployeeViewForClientAttendanceStatus } from 'src/app/models/getEmployeeViewForClientAttendanceStatus';
import { Project } from 'src/app/models/project';
import { ProjectViewForTimesheet } from 'src/app/models/projectViewForTimesheet';
import { Timesheet } from 'src/app/models/timesheet';
import { TimesheetDashboardCount } from 'src/app/models/timesheetDasboardCount';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { NavigateToCalenderViewDirective } from 'src/app/directives/navigate-to-calender-view.directive';
import * as XLSX from 'xlsx-js-style';
import { GetEmployeeTimesheetAsCalender } from 'src/app/models/getEmployeeTimesheetAsCalender';
import { getEmployeeTimesheetAsCalenderByProjectId } from 'src/app/models/getEmployeeTimesheetAsCalenderByProjectId';
import { EmployeeTimesheetResponse } from 'src/app/models/employeeTimesheetResponse';
import { getProjectViewList } from 'src/app/models/getProjectViewList';
import { Feature } from 'src/app/models/feature';


interface DayCell {
  date: Date;
  day: number | '';
  isToday: boolean;
  isWeekend: boolean;
  isHoliday: boolean;
  attendance?: string;
  intime?: string;
  outtime?: string;
}

export interface TimesheetDayData {
  status: string;
  inTime: string | null;
  outTime: string | null;
}

export interface EmployeeTimesheet {
  empId: number;
  employeeName: string;
  clientSideId: string;
  startDate: string;
  teamName: string;
  teamId: number;
  spoc: string | null;
  billableType: string;
  employeeRole: string;
  department: string;
  projectId: number;
  projectName: string;
  projectManagerName: string;
  poNo: string;
  clientName: string;
  reportingManagerId: number;
  monthName: string;
  expectedTimesheetFillCount: number;
  apmosysTimesheetFilledCount: number;
  clientSideNotFilledCount: number;
  clientSidePendingCount: number;
  clientSideApprovedCount: number;
  employmentId: string;
  timesheetData: { [key: string]: TimesheetDayData };
}


@Component({
  standalone: false,
  selector: 'app-hr-dashboard',
  templateUrl: './hr-dashboard.component.html',
  styleUrls: ['./hr-dashboard.component.css']
})
export class HrDashboardComponent implements AfterViewInit {

  @ViewChild('vmsChartContainer', { static: false }) vmsChartContainer!: ElementRef;
  @ViewChild('ishineChartContainer', { static: false }) ishineChartContainer!: ElementRef;
  @ViewChild('departmentChartContainer', { static: false }) departmentChartContainer!: ElementRef;
  @ViewChild('docRejectChart', { static: false }) docRejectChart!: ElementRef;
  @ViewChild('insightValidationTemplate') insightValidationTemplate!: TemplateRef<any>;
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  alertTemplate_insight: TemplateRef<any>;
  modalRef2?: NgbModalRef;
  modalRef?: NgbModalRef;
  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;
  @ViewChild('timesheet_summary_template') timesheetSummaryTemplate!: TemplateRef<any>;
  @ViewChild('fromDateRef') fromDateRef: NgModel;
  @ViewChild('toDateRef') toDateRef: NgModel;

  // selectedEmpId: any;
  // selectedProjectId:any;
  filteredEmployees: Employee[] = [];
  employeeCtrl = new FormControl();
  projectPoCtrl = new FormControl();
  employeeList: Employee[] = [];
  teamLeadsList: any[] = [];
  filteredTeamLeads: Employee[] = [];
  fromDate: Date | null = null;
  toDate: Date | null = null;
  // employeeTimesheet: Object;
  employeeTimesheet: any[] = [];
  timesheetObj: Timesheet = new Timesheet();
  page: number = 1;
  paginationArray: number[] = [];
  lastUpdated: string = '';
  totalEmployees = 0;
  VmsRejectioncount: any[] = [];
  hodCount: number = 0;
  hrCount: number = 0;
  rmCount: number = 0;
  totalVmsFilledCount: any;
  totalIshineFilledCount: any;
  totalvmsNotFilled: any;
  totalIshineNotFilledCount: any;
  totalExpectedEmployees: any;
  employeeView: GetEmployeeViewForClientAttendanceStatus[] = [];
  employeeExcelView: GetEmployeeViewForClientAttendanceStatus[] = [];
  // employeeViewColumns: any[] = ['employmentId', 'clientSideId','employeeName','employmentStatus', 'projectStatus','department', 'billableType','clientName', 'poNo','projectName','mobileNo', 'email', 'departmentName', 'expectedFillCount', 'clientSideAttendancePendingCount', 'clientSideAttendanceApprovedCount', 'clientSideAttendanceNotFilledCount', 'projectType', 'projectManagers', 'clientName', 'apmosysRm', 'apmosysRmEmail', 'clientRm', 'team', 'teamLeadName'];
   employeeViewColumns: any[] = [
  'employmentId',
  'employeeName',
  'employmentStatus',
  'projectStatus',
  'department',
  'billableType',
  'clientSideId',
  'clientName',
  'poNo',
  'projectName',
  'projectManagerName',
  'teamName',
  'startDate',
  'endDate'
];

  filters: any = {};
  isSearchEnabled: boolean = false;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  excelName: any;
  tableName: any;
  previewUrl: any;
  fileType: '' | 'pdf' | 'image' | null = null;
  docData: any;
  mimeType: any;
  projectView: ProjectViewForTimesheet[] = [];
  projectViewForExcel: ProjectViewForTimesheet[] = [];
  projectViewColumns: any[] = ['projectName', 'poNo', 'startDate','endDate','totalEmployees', 'projectManagerName', 'projectType', 'clientName', 'apmosysRm', 'apmosysRmEmail', 'clientRm', 'totalExpectedFillCount', 'totalClientSideApprovedCount', 'blank', 'totalClientSidePendingCount', 'blank', 'totalClientSideNotFilledCount', 'blank','active'];
  timesheetSummaryColumns: any[] = ['blank', 'employmentId', 'name', 'blank', 'blank', 'blank', 'blank', 'blank'];
  totalClientSideApprovedCount: any;
  eodNotFilledCount: any;
  totalClientSidePendingCount: any;
  totalDocumentApprovedCount: any;
  totalDocumentPendingCOunt: any;
  totalDocumentPendingCount: any;
  totalDocumentRejectedCount: any;
  toggleValue: Boolean = false;
  timesheetCalender: any;
  dashboardObj: TimesheetDashboardCount = new TimesheetDashboardCount();
  selectedProjectId: number | null = null;
  selectedEmpId: number | null = null;
  status: String = 'All';
  showCalendar = false;
  hoveredProjectId: any;
  hidePopupTimeout: any;
  // month:any = 7;
  // year:any = 2025;
  month: any;
  year: any;
  formattedMonthLabel: string;
  selectedMonth1: Date;
  currentUser: User;
  projectObj:Project=new Project();
  isClientDashboard: boolean=true;
  // isClientDashboard: boolean=false;
  dataForExcel: Boolean=false;


  //pagination
  page1: number = 1;
  totalItems: number = 0;
  pageSize: number = 20;

  //InsightPagination
  insightPage: number = 1;
  insightPageTotalItems: number = 0;
  insightPageSize: number = 10;

  // selectedBillableType: string = 'All';
selectedBillableTypes: string[] = ['TNM','TNM(Shadow)'];
  columnDataToSearch: any;
  currentColumnFilter: any = null;
  isInsightSearchEnabled: boolean = false;
selectedProjectStatus: string = 'All';
selectedEmployeeStatus : string = 'All';
  projectViewFilters = {
    projectName: '',
    poNo: '',
    startDate: '',
    endDate: '',
    totalEmployees: '',
    projectManagerName: '',
    projectType: '',
    clientName: '',
    apmosysRm: '',
    apmosysRmEmail: '',
    clientRm: '',
    totalExpectedFillCount: '',
    totalClientSideApprovedCount: '',
    totalClientSidePendingCount: '',
    totalClientSideNotFilledCount: '',
    active:''
  };

  // employeeViewColumnsFilters = {
  //   employmentId: '',
  //   clientSideId:'',
  //   name: '',
  //   billable: '',
  //   billableType: '',
  //   mobileNo: '',
  //   email: '',
  //   departmentName: '',
  //   expectedFillCount: '',
  //   clientSideAttendancePendingCount: '',
  //   clientSideAttendanceApprovedCount: '',
  //   clientSideAttendanceNotFilledCount: '',
  //   projectName: '',
  //   poNo: '',
  //   projectType: '',
  //   projectManagers: '',
  //   clientName: '',
  //   apmosysRm: '',
  //   apmosysRmEmail: '',
  //   clientRm: '',
  //   team: '',
  //   teamLeadName: ''
  // };

  employeeViewColumnsFilters = {
  employmentId: '',
  clientSideId: '',
  employeeName: '',
  employmentStatus: '',
  projectStatus: '',
  department: '',
  billableType: '',
  clientName: '',
  poNo: '',
  projectName: '',
  projectManagerName: '',
  teamName: '',
  startDate: '',
  endDate: ''
};

  timesheetSummaryColumnsFilters = {
    employmentId: '',
    name: '',
  }

  currentDate = new Date();
  minYear!: Date;
  maxYear!: Date;
  today: Date = new Date();
  menuVisible = false;
  timesheetData: EmployeeTimesheetResponse[] = [];
  timesheetAsCalenderByProjectId: getEmployeeTimesheetAsCalenderByProjectId = new getEmployeeTimesheetAsCalenderByProjectId();
  projectViewClient: getProjectViewList =new getProjectViewList ();
  @ViewChild("alert_message_all_employee")
  alertTemplateAllEmployee: TemplateRef<any>;
  modalRefAllEmployee?: NgbModalRef;
  today2: string = new Date().toISOString().split('T')[0];
  modalRefForInsightValidation?: NgbModalRef;

  filteredTimesheetData: EmployeeTimesheetResponse[] = [];
  daysInMonth: { dayNumber: number; dayName: string }[] = [];
  timesheetDataColumns: any[] = ['employmentId', 'clientSideId', 'employeeName', 'employmentStatus', 'projectStatus', 'department', 'billableType', 'clientName', 'poNo', 'projectName', 'projectActive', 'projectManagerName', 'teamName', 'startDate', 'endDate', 'expectedTimesheetFillCount','apmosysTimesheetFilledCount','clientSideNotFilledCount','clientSidePendingCount','clientSideApprovedCount'];
 days: number[] = Array.from({ length: 31 }, (_, i) => i + 1);

 hoveredEmpId: number | null = null;
 hideTimeout: any;
//  isExpanded: { [key: string]: boolean } = {};
isExpanded: any = {};
viewClientIdFlag:string = "ALL";
currentSelectedStatus ="";
billableTypes: string[] = [];
// billableTypes: string[] = ['All','TNM', 'Fixed Cost', 'Shadow','Bench', 'InternalRNDProducts'];
employeeBillableTypes: string[] = ['TNM', 'Fixed Cost', 'TNM(Shadow)', 'Fixed Cost(Shadow)','Bench', 'InternalRNDProducts','All'];
projectBillableTypes: string[] = ['TNM', 'Fixed Cost', 'Monitoring','All'];
selectedStatus:String = "All" ;
newSelectedStatus:String = "";

selectedTile: any = null;
selectedDepartments: any[] = [];
isDeptCollapsed = false;
selectedDeptId: string | null = null;

isDeptTableCollapsed = true;
departmentTableData: any[] = [];
isDeptTableLoading = false;

employeeViewBullet : string[] = ["Provides a resource-centric, month-wise overview across projects.",
"Displays timesheet completion and approval status for each individual resource.",
"Indicates the compliance state for the selected month.",
"Helps identify resources with missing, pending, or rejected timesheets.",
"Supports managerial action to ensure compliance before invoicing or payroll processing."];

projectViewBullet : string[] =["Provides a consolidated, month-wise view at the project level.",
"Displays overall timesheet status and compliance health for the project.",
"Shows approval readiness and invoicing eligibility of mapped resources.",
"Helps identify defaulters and pending approvals at a glance.",
"Enables quick project-level assessment without drilling down to individual employee details."];

tiles: any[] = [];
tileGroups: any[] = [];
  userMapping:any = {};
  feature = "Timesheets Dashboard";
  isEmployeeRepeatedFlag: boolean;
  repetedDeptId: string;
  allowedEmpid: number | null = null;
  authorizedEmp: boolean = false;

 globalPoConflictSelection: 'Yes' | 'No' | 'All' = 'All';
isGlobalPoDropdownOpen = false;

  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: NgbModal,
    private projectService: ProjectService,
    private resourceManagementService: ResourceManagementService,
    private exportExcelService: ExportExcelService,
    private sanitizer: DomSanitizer, private router: Router,
    private authenticationService: AuthenticationService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {

    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log("*ngIf=userMapping.export_timesheet_details",this.userMapping.export_timesheet_details)
    const currentYear = this.currentDate.getFullYear();
    this.minYear = new Date(currentYear - 1, 0, 1);
    this.maxYear = new Date(currentYear, 11, 31);
    const today = new Date();
    this.month = today.getMonth() + 1;
    this.year = today.getFullYear();
    // this.onBillableTypeChange(this.selectedBillableType);
    this.updateBillableTypes();
  this.getTableData('All', this.month, this.year);
    this.selectedMonth1 = new Date(this.year, this.month - 1, 1);
    this.updateFormattedMonthLabel();
    this.legendEntries = Object.entries(this.legend).map(([code, value]) => ({
      code,
      label: value.label,
      color: value.color
    }));
    // this.toggleValue = true;
    this.toggleValue = false;
    if (this.toggleValue) {
      this.currentColumnFilter = { ...this.projectViewFilters };
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }else{
      this.currentColumnFilter = {};
      this.employeeViewColumns.forEach(col => this.currentColumnFilter[col] = "");
      // this.currentColumnFilter = { ...this.employeeViewColumns };
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
    this.getTimesheetDashboardCount(this.month, this.year);
    // this.generateMonthGrid();
    // this.fetchTimesheetData(this.selectedProjectId ,this.selectedEmpId);
    this.setLastUpdatedTime();
    this.getEmployeeByNameAndEmpld();
    this.getProjectByNameAndPoNo();
    this.loadDepartmentStatusSummary(this.status, this.month, this.year);
    // this.TotalEmployeeCount();
    // this.vmsCompletion();
    // this.ishineCompletion();
    // this.vmsNotFilled();
    // this.ishineNotFilled();

    this.employeeCtrl.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value?.name || ''),
        map(name => this.filterEmployees(name))
      )
      .subscribe(filtered => {
        this.filteredEmployees = filtered;
      });

    this.projectPoCtrl.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value?.projectName || ''),
        map(name => this.filterProject(name))
      )
      .subscribe(filtered => {
        this.filteredProject = filtered;
      });

    this.generateDaysForMonth(this.selectedMonth1);
  }

  ngAfterViewInit(): void {
    this.renderVmsChart();
    this.renderIshineChart();
    this.renderDepartmentChart();
    this.renderDocRejectChart();
    this.getVmsDocumentApprovalStatusWiseCount();
  }

  renderVmsChart(): void {
    Highcharts.chart(this.vmsChartContainer.nativeElement, {
      chart: { type: 'pie' },
      title: { text: 'Client Attendance Status Distribution', align: 'center' },
      plotOptions: {
        pie: {
          innerSize: '70%',
          dataLabels: { enabled: false }
        }
      },
      series: [{
        name: 'Count',
        type: 'pie',
        data: [
          { name: 'Filled But Not Approved', y: 50, color: '#fd7e14' },
          { name: 'Filled And Approved', y: 120, color: '#28a745' },
          { name: 'Not Filled', y: 78, color: "#FF0000" }
        ]
      }],
      legend: { align: 'center', verticalAlign: 'bottom' }
    });
  }

  renderIshineChart(): void {
    Highcharts.chart(this.ishineChartContainer.nativeElement, {
      chart: { type: 'pie' },
      title: { text: 'IShine Status Distribution', align: 'center' },
      plotOptions: {
        pie: {
          innerSize: '70%',
          dataLabels: { enabled: false }
        }
      },
      series: [{
        name: 'Count',
        type: 'pie',
        data: [
          { name: 'Filled But Not Approved', y: 60, color: '#fd7e14' },
          { name: 'Filled And Approved', y: 130, color: '#28a745' },
          { name: 'Not Filled', y: 58, color: '#FF0000' }
        ]
      }],
      legend: { align: 'center', verticalAlign: 'bottom' }
    });
  }

  renderDepartmentChart(): void {
    Highcharts.chart(this.departmentChartContainer.nativeElement, {
      chart: { type: 'column' },
      title: { text: 'Department Wise Employee' },
      xAxis: {
        categories: ['FT', 'AT', 'DevOps', 'IT'],
        title: { text: 'Department' }
      },
      yAxis: {
        min: 0,
        title: { text: 'Employees' }
      },
      series: [{
        name: 'Employees',
        type: 'column',
        data: [30, 70, 60, 100],
        colorByPoint: true,
        colors: ['#6c757d', '#007bff', '#17a2b8', '#001f3f']
      }],
      legend: { enabled: false }
    });
  }

  renderDocRejectChart(): void {
    Highcharts.chart(this.docRejectChart.nativeElement, {
      chart: {
        type: 'pie',
        height: 300
      },
      title: { text: 'Document Reject Status', align: 'center' },
      plotOptions: {
        pie: {
          dataLabels: { enabled: false },
          showInLegend: true,
          size: '90%'
        }
      },
      legend: {
        align: 'center',
        verticalAlign: 'bottom',
        layout: 'vertical'
      },
      series: [{
        name: 'Rejected By',
        type: 'pie',
        data: [
          { name: 'By Hod', y: 25, color: '#48b684' },            // Green
          { name: 'By Reporting Manager', y: 45, color: '#497dea' },  // Blue
          { name: 'By HR', y: 30, color: '#eb6524' }             // Orange
        ]
      }]
    });
  }

  getEmployeeByNameAndEmpld() {
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.isClientDashboard = this.isClientDashboard;
    this.employeeService.getEmployeeByNameAndEmpidForTimesheet(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeList = response.serviceResponse;
        this.filteredEmployees = this.employeeList;
        this.teamLeadsList = this.employeeList;
        this.filteredTeamLeads = this.teamLeadsList;
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  displayEmployee(emp: any): string {
    console.log("emp", emp);  // This is helpful for debugging
    if (typeof emp === 'string') {
      return emp;  // User is typing or you manually set value to string
    }
    return emp && emp.name ? emp.name : '';
  }
  displayProject(project: any): string {
    console.log("emp", project);  // This is helpful for debugging
    if (typeof project === 'string') {
      return project;  // User is typing or you manually set value to string
    }
    return project && project.projectName ? project.projectName : '';
  }
  filterEmployees(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.employeeList.filter(emp =>
      emp.name.toLowerCase().includes(lowerText) ||
      emp.employmentId.toLowerCase().includes(lowerText)
    );
  }
  filterProject(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.projectList.filter(emp => {
      const pName = emp.projectName ? emp.projectName.toLowerCase() : '';
      const pPoNo = emp.poNo ? emp.poNo.toLowerCase() : '';
      return pName.includes(lowerText) || pPoNo.includes(lowerText);
    });
  }

  onEmployeeSelected(event: any) {
    const selectedEmp = event.option.value;
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      this.employeeCtrl.setValue(selectedEmp.name);
      console.log('Selected Employee ID:', this.selectedEmpId);

    }
  }
  onEmployeeInputChange() {
    if (!this.employeeCtrl.value || this.employeeCtrl.value.trim() === '') {
      this.selectedEmpId = 0;
    }
  }

  onProjectInputChange() {
    if (!this.projectPoCtrl.value || this.projectPoCtrl.value.trim() === '') {
      this.selectedProjectId = 0;
    }
  }

  onProjectSelected(event: any) {
    const selectedProject = event.option.value;
    if (selectedProject) {
      this.selectedProjectId = selectedProject.projectId;
      this.projectPoCtrl.setValue(selectedProject.projectName);
      console.log('Selected Employee ID:', this.selectedProjectId, selectedProject);

    }
  }
  onDateRangeChange(): void {
    // You might add logic here to validate the date range, etc.
  }

  alertMessage: any;



  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openAlertMod1(template1: TemplateRef<any>, message: any) {
    this.modalRef2 = this.modalService.open(template1, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  // searchTimesheet(template: TemplateRef<any> ) {

  // this.page = 1;
  // const totalPages = Math.ceil(this.employeeTimesheet.length / 10);
  // this.paginationArray = Array.from({ length: totalPages }, (_, i) => i + 1);

  //   if (!this.selectedEmpId || !this.fromDate || !this.toDate) {
  //     alert('Please select an employee and valid dates.');
  //     return;
  //   }

  //   this.timesheetObj.empId = this.selectedEmpId;
  //   this.timesheetObj.fromDate = this.formatDate(this.fromDate);
  //   this.timesheetObj.toDate = this.formatDate(this.toDate);


  //   console.log("empId ::::::::",this.timesheetObj.empId);
  //   console.log("fromDate ::::::::",this.timesheetObj.fromDate);
  //   console.log("toDate ::::::::",this.timesheetObj.toDate);


  //   this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj)
  //     .subscribe(
  //       (data: any[]) => {
  //         this.employeeTimesheet = data;
  //         console.log('Timesheet:', data);
  //         this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  //         this.fromDate = null;
  //         this.toDate = null;
  //         this.timesheetObj.empId = '' ;
  //       },
  //       (error) => {
  //         console.error('Error fetching timesheet', error);
  //       }

  //     );
  // }



  searchTimesheet(
  template?: TemplateRef<any>,
  template1?: TemplateRef<any>,
  openModal: boolean = false
) {

  if (!this.selectedEmpId) {
    this.alertMessage = "Please select an Employee !!";
    this.openAlertMod1(template1, this.alertMessage);
    return;
  }

  if (!this.fromDate) {
    this.alertMessage = "Please select a From Date !!";
    this.openAlertMod1(template1, this.alertMessage);
    return;
  }

  if (!this.toDate) {
    this.alertMessage = "Please select a To Date !!";
    this.openAlertMod1(template1, this.alertMessage);
    return;
  }

  if (new Date(this.fromDate) > new Date(this.toDate)) {
    this.alertMessage = "From Date cannot be greater than To Date !!";
    this.openAlertMod1(template1, this.alertMessage);
    return;
  }

  this.timesheetObj.empId = this.selectedEmpId;
  this.timesheetObj.fromDate = this.fromDate;
  this.timesheetObj.toDate = this.toDate;
  this.timesheetObj.page = this.insightPage;
  this.timesheetObj.size = this.insightPageSize;

  this.employeeTimesheet = [];

  this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj).subscribe(
    (data: any) => {
      if (data.serviceStatus === "Success") {
        this.employeeTimesheet = data.serviceResponse;
        this.insightPageTotalItems = data.totalElements;
      }

      if (openModal && template) {
         this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
      }
    },
    (error) => {
      console.error("Error fetching timesheet", error);
    }
  );
}

  handlePageChange(event) {
    if (this.pageSize != event.pageSize) {
      this.page = 1;
      this.page1 = 1;
      this.pageSize = event.pageSize;
    } else {
      this.page = event.pageIndex + 1;
      this.page1 = event.pageIndex + 1;
      this.pageSize = event.pageSize;
    }
    if (!this.toggleValue) {
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
      this.getEmployeeByNameAndEmpld();
    } else {
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
  cancelRequest() {
    if (this.modalRef) {
      this.modalRef?.close();
    }
  }
  cancelRequest1() {
    // this.modalRef?.close();
    this.modalRef2.close();
  }

  previewDocument(entry: any): void {
    // Handle document preview logic here
    console.log("Preview document for entry:", entry);
  }

  approveTimesheet(entry: any): void {
    // Handle approve logic
    console.log("Approved:", entry);
  }

  rejectTimesheet(entry: any): void {
    // Handle reject logic
    console.log("Rejected:", entry);
  }

  refreshDashboard(): void {
    this.getTimesheetDashboardCount(this.month, this.year);
    // this.loadDashboardData();
    this.setLastUpdatedTime();
    this.TotalEmployeeCount();
    // this.vmsCompletion();
    // this.vmsCompletion();
    this.ishineCompletion();
    // this.vmsNotFilled();
    // this.ishineNotFilled();
  }

  setLastUpdatedTime(): void {
    const now = new Date();
    const hours = now.getHours() % 12 || 12;
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const ampm = now.getHours() >= 12 ? 'PM' : 'AM';

    this.lastUpdated = `Today, ${hours}:${minutes} ${ampm}`;
  }


  TotalEmployeeCount() {
    this.resourceManagementService.totalEmployeeCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.totalEmployees = response.serviceResponse;
      } else {
        this.openAlertMod1(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  vmsCompletion() {
    this.timesheetObj.clientApprovalStatus = "pending";
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetService.totalVmsFilledCount(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        // console.log("Raw response:", response.serviceResponse);
        const nestedArray = response.serviceResponse;
        this.totalVmsFilledCount = nestedArray?.[0]?.[0] ?? 0;

        // console.log("Total VMS completion:", this.totalVmsFilledCount);
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }



  ishineCompletion() {
    this.timesheetService.totalIshineFilledCount(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        const nestedArray = response.serviceResponse;
        this.totalIshineFilledCount = nestedArray?.[0]?.[0] ?? 0;
        console.log("Total Ishine completion", this.totalIshineFilledCount);
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  finalDocumentApproval() {
    this.timesheetService.finalDocumentApproval(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        this.totalEmployees = response.serviceResponse;
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }
  // VmsRejectioncount:any[]=[];
  // getVmsDocumentApprovalStatusWiseCount(){
  //    this.timesheetService.getVmsDocumentApprovalStatusWiseCount().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus === "Success") {
  //       console.log(response.serviceResponse);
  //       this.VmsRejectioncount = response.serviceResponse;
  //       console.log("test",this.VmsRejectioncount);
  //     } else {
  //       this.openAlertMod(this.alertTemplate, response.serviceResponse);
  //     }
  //   });
  // }


  getVmsDocumentApprovalStatusWiseCount() {
    this.timesheetService.getVmsDocumentApprovalStatusWiseCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.VmsRejectioncount = response.serviceResponse;

        this.hodCount = 0;
        this.hrCount = 0;
        this.rmCount = 0;

        for (const item of this.VmsRejectioncount) {
          switch (item.reason) {
            case 'Rejected By HOD':
              this.hodCount = item.count;
              break;
            case 'Rejected By HR':
              this.hrCount = item.count;
              break;
            case 'Rejected By RM':
              this.rmCount = item.count;
              break;
          }
        }

      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  vmsNotFilled() {
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetService.totalvmsNotFilled(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        const nestedArray = response.serviceResponse;
        this.totalvmsNotFilled = nestedArray?.[0]?.[0] ?? 0;
        // console.log("Total VMS not completion", this.totalvmsNotFilled);
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  async ishineNotFilled() {
    this.timesheetObj.empId = this.currentUser.empId;
    this.timesheetObj.isClientDashboard = this.isClientDashboard;

    await this.timesheetService.totalIshineNotFilledCount(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log(response.serviceResponse);
        const nestedArray = response.serviceResponse;
        this.totalClientSideApprovedCount = nestedArray?.[0]?.[0] ?? 0;
        this.totalClientSidePendingCount = nestedArray?.[0]?.[1] ?? 0;
        this.eodNotFilledCount = nestedArray?.[0]?.[2] ?? 0;
        this.totalDocumentApprovedCount = nestedArray?.[0]?.[4] ?? 0;
        this.totalDocumentRejectedCount = nestedArray?.[0]?.[5] ?? 0;


        this.totalExpectedEmployees = nestedArray?.[0]?.[0] ?? 0;
        this.totalIshineNotFilledCount = nestedArray?.[0]?.[1] ?? 0;
        console.log("Total Ishine NOt completion", this.totalIshineNotFilledCount);
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  // onToggleChange(event: Event) {
  //   // Cast event target as HTMLInputElement to read checked property
  //   const isChecked = (event.target as HTMLInputElement).checked;
  //   console.log('Toggle is now:', isChecked);

  //   // Call your desired logic here
  //   // Example: update a property used for toggling rows
  //   this.toggleValue = !this.toggleValue;
  //   this.page1 = 1;
  //   this.totalItems = 0;
  //   this.pageSize = 10;
  //   this.isSearchEnabled = false;

  //   this.getTimesheetDashboardCount(this.month, this.year);

  //   if (!this.toggleValue) {
  //     this.status = 'All';
  //     // this.selectedStatus = this.status;
  //     this.currentColumnFilter = { ...this.employeeViewColumnsFilters };
  //     this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
  //   } else {
  //     this.status = 'All';
  //     this.currentColumnFilter = { ...this.projectViewFilters };
  //     this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
  //   }
  //   this.selectedStatus = this.status;

  //   // Add any other side effects or function calls you want here
  // }
updateBillableTypes() {

  if (this.toggleValue) {
    this.billableTypes = [...this.projectBillableTypes];
  } else {
    this.billableTypes = [...this.employeeBillableTypes];
  }

  // Reset selections
  // this.selectedBillableTypes = [];
}
  onToggleChange(event: Event) {

  const isChecked = (event.target as HTMLInputElement).checked;
  console.log('Toggle is now:', isChecked);

  this.toggleValue = !this.toggleValue;
  this.page1 = 1;
  this.totalItems = 0;
  this.pageSize = 20;
  this.isSearchEnabled = false;

  this.getTimesheetDashboardCount(this.month, this.year);

  if (!this.toggleValue) {
    // Employee View
    this.billableTypes = [...this.employeeBillableTypes];
    this.status = 'All';
    this.currentColumnFilter = { ...this.employeeViewColumnsFilters };
    this.selectedBillableTypes = ['TNM', 'TNM(Shadow)'];
    this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
  } else {
    // Project View
    this.billableTypes = [...this.projectBillableTypes];
    this.status = 'All';
    this.currentColumnFilter = { ...this.projectViewFilters };
    this.selectedBillableTypes = ['TNM'];
    this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
  }

  // this.selectedBillableTypes = [];

  this.selectedStatus = this.status;
}


  filteredProject: any[] = [];
  projectList: any[] = [];
  getProjectByNameAndPoNo() {
    this.projectObj.empId = this.currentUser.empId;
    this.projectObj.page = 10
    this.projectObj.size = 10
    this.projectObj.isClientDashboard = this.isClientDashboard;
    this.projectService.getProjectWithCliendSideID(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.projectList = response.serviceResponse;
        this.filteredProject = this.projectList;
        console.log("test", this.filteredProject)
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }
  employeeListAccordingToProject: any[] = [];
  // openProjectInsightModal() {

  //   if (!this.selectedProjectId || !this.fromDate || !this.toDate) {
  //     this.openInsightValidationModal(this.insightValidationTemplate, 'Please select a project and valid dates.');
  //     return;
  //   }

  //   this.currentColumnFilter = { ...this.timesheetSummaryColumnsFilters }
  //   this.getEmployeeTimesheetsByProject();
  //   this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
  // }

  openProjectInsightModal() {

  if (!this.selectedProjectId) {
    this.openInsightValidationModal(
      this.insightValidationTemplate,
      'Please select a project.'
    );
    return;
  }

  if (!this.fromDate) {
    this.openInsightValidationModal(
      this.insightValidationTemplate,
      'Please select a From Date.'
    );
    return;
  }

  if (!this.toDate) {
    this.openInsightValidationModal(
      this.insightValidationTemplate,
      'Please select a To Date.'
    );
    return;
  }

  if (new Date(this.fromDate) > new Date(this.toDate)) {
    this.openInsightValidationModal(
      this.insightValidationTemplate,
      'From Date cannot be greater than To Date.'
    );
    return;
  }

  this.currentColumnFilter = { ...this.timesheetSummaryColumnsFilters };
  this.getEmployeeTimesheetsByProject();
  this.modalRef = this.modalService.open(this.timesheetSummaryTemplate, { modalDialogClass: 'modal-xl' });
}

  getEmployeeTimesheetsByProject() {
    this.timesheetObj.projectId = this.selectedProjectId;
    this.timesheetObj.fromDate = this.formatDate(this.fromDate);
    this.timesheetObj.toDate = this.formatDate(this.toDate);
    this.timesheetObj.page = this.insightPage;
    this.timesheetObj.size = this.insightPageSize;
    this.timesheetObj.isClientDashboard = this.isClientDashboard
    this.timesheetObj.dataForExcel = false;
    this.timesheetObj.columnFilter = this.currentColumnFilter == null ? this.timesheetSummaryColumnsFilters : this.currentColumnFilter;
    this.employeeListAccordingToProject = []
    this.timesheetObj.sortBy = this.sortColumnType;
    this.timesheetObj.sortDirection = this.sortDirection as 'asc' | 'desc';

    this.projectService.getEmployeeTimesheetsByProject(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeListAccordingToProject = response.serviceResponse;
        this.insightPageTotalItems = response.totalElements;
        console.log("test", this.employeeListAccordingToProject);
        // this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
        // this.fromDate = null;
        // this.toDate = null;
        // this.timesheetObj.projectId = '' ;
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  employeeListAccordingToProjectForExcel: any[] = [];


  openInsightValidationModal(template: TemplateRef<any>, message: string): void {
    this.alertMessage = message;
    this.modalRefForInsightValidation = this.modalService.open(template, {
      modalDialogClass: 'modal-sm insight-validation-alert-modal'
    });
  }

  closeInsightValidationModal(): void {
    this.modalRefForInsightValidation?.close();
  }
  getEmployeeTimesheetsByProjectForExcel(template: TemplateRef<any>): Promise<void> {
    return new Promise((resolve, reject) => {
      this.page = 1;

      if (!this.selectedProjectId || !this.fromDate || !this.toDate) {
        this.openInsightValidationModal(this.insightValidationTemplate, 'Please select a project and valid dates.');
        return;
      }

      this.timesheetObj.projectId = this.selectedProjectId;
      this.timesheetObj.fromDate = this.formatDate(this.fromDate);
      this.timesheetObj.toDate = this.formatDate(this.toDate);
      this.timesheetObj.page = this.page1;
      this.timesheetObj.size = this.pageSize;
      this.timesheetObj.isClientDashboard = this.isClientDashboard;
      this.timesheetObj.dataForExcel = true;
      this.timesheetObj.sortDirection = 'asc';
      this.projectService.getEmployeeTimesheetsByProject(this.timesheetObj)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === 'Success') {
              this.employeeListAccordingToProjectForExcel = response.serviceResponse;
              this.totalItems = response.totalElements;
              this.dataForExcel = false;
              this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
              this.fromDate = null;
              this.toDate = null;
              this.timesheetObj.projectId = '';
              resolve();
            } else {
              this.openAlertMod(this.alertTemplate, response.serviceResponse);
              reject(response.serviceResponse);
            }
          },
          error: (err) => {
            console.error("Error fetching employee timesheets:", err);
            reject(err);
          }
        });
    });
  }


  timesheetRequestDTO: any = {
    status: '',
    month1: null,
    year: null,
    billableType: ''
  }
  // getEmployeeViewForClientAttendanceStatus(status: any, month: any, year: any) {
  //   this.timesheetRequestDTO.status = status;
  //   this.timesheetRequestDTO.month1 = month;
  //   this.timesheetRequestDTO.year = year;
  //   this.timesheetRequestDTO.empId = this.currentUser.empId;
  //   this.timesheetRequestDTO.isClientDashboard = this.isClientDashboard;
  //   this.timesheetRequestDTO.page = this.page1;
  //   this.timesheetRequestDTO.size = this.pageSize;
  //   this.timesheetRequestDTO.dataForExcel = false;
  //   this.timesheetRequestDTO.billableType = this.selectedBillableType;
  //   this.timesheetRequestDTO.columnFilter = this.currentColumnFilter == null ? this.employeeViewColumnsFilters : this.currentColumnFilter;
  //   this.timesheetRequestDTO.sortBy = this.sortColumn ?? 'name';
  //   this.timesheetRequestDTO.sortDirection = this.sortDirection ?? 'asc';

  //   this.employeeView = [];
  //   this.timesheetService.getEmployeeViewForClientAttendanceStatus(this.timesheetRequestDTO).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus === "Success") {
  //       this.employeeView = response.serviceResponse;
  //       this.totalItems = response.totalElements;

  //       console.log("employeeView ::::::", this.employeeView);
  //     } else {
  //       this.openAlertMod1(this.alertTemplate, response.serviceResponse);
  //     }
  //   });
  // }

  getEmployeeViewForClientAttendanceStatus(status: any, month: any, year: any,deptId?: string | null,isEmployeeRepeated: boolean = false) {
    this.timesheetData =[];
    this.timesheetAsCalenderByProjectId.month = month;
    this.timesheetAsCalenderByProjectId.year = year;
    this.timesheetAsCalenderByProjectId.empId = this.currentUser.empId;
    this.timesheetAsCalenderByProjectId.status = status;
    this.timesheetAsCalenderByProjectId.multiPOs = this.globalPoConflictSelection;
    this.timesheetAsCalenderByProjectId.billableType = this.selectedBillableTypes;
    this.timesheetAsCalenderByProjectId.employeeActive = this.selectedEmployeeStatus;
    this.timesheetAsCalenderByProjectId.page=this.page1??1;
	  this.timesheetAsCalenderByProjectId.size=this.pageSize??20;
    this.timesheetAsCalenderByProjectId.sortBy=this.sortColumn??'name';
	  this.timesheetAsCalenderByProjectId.sortDirection=this.sortDirection??'asc';
     //Filter by client id present or not.
    this.timesheetAsCalenderByProjectId.clientSideFilter = this.viewClientIdFlag;
      // this.timesheetAsCalenderByProjectId.filters = this.employeeViewColumnsFilters;
      this.timesheetAsCalenderByProjectId.filters = this.currentColumnFilter == null ? this.employeeViewColumnsFilters : this.currentColumnFilter;

    if (deptId) {
      this.timesheetAsCalenderByProjectId.deptId = deptId;
    } else {
      // optional: clear previous filter
      delete this.timesheetAsCalenderByProjectId.deptId;
    }

    this.timesheetAsCalenderByProjectId.isEmployeeRepeated = isEmployeeRepeated;

    this.repetedDeptId = this.timesheetAsCalenderByProjectId.deptId ;
    this.isEmployeeRepeatedFlag = this.timesheetAsCalenderByProjectId.isEmployeeRepeated ;

    if(this.isClientDashboard){
      this.timesheetAsCalenderByProjectId.allEmp = !this.isClientDashboard;
    console.log("this.timesheetAsCalenderByProjectId.allEmp - if -",this.timesheetAsCalenderByProjectId.allEmp)
    } else {
      this.timesheetAsCalenderByProjectId.allEmp = !this.isClientDashboard;
    console.log("this.timesheetAsCalenderByProjectId.allEmp - else -",this.timesheetAsCalenderByProjectId.allEmp)
    }

    this.timesheetService.getEmployeeViewForClientAttendanceStatus(this.timesheetAsCalenderByProjectId)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.timesheetData = response.serviceResponse.map((item: any) => ({
          ...item,
          employmentId: item.employmentId ?? 'NA',
          clientSideId: item.clientSideId ?? 'NA',
          employeeName: item.employeeName ?? 'NA',
          department: item.department ?? 'NA',
          billableType: item.billableType ?? 'NA',
          clientName: item.clientName ?? 'NA',
          poNo: item.poNo ?? 'NA',
          projectName: item.projectName ?? 'NA',
          projectManagerName: item.projectManagerName ?? 'NA',
          teamName: item.teamName ?? 'NA',
          startDate: item.startDate ?? 'NA',
          endDate :item.endDate ?? 'NA',

          timesheetData: this.fillTimesheetDays(item.timesheetData),
        }));

        this.filteredTimesheetData = [...this.timesheetData];
        // this.totalItems = this.getCountByStatus(status);
        this.totalItems = response.totalElements;
        console.log("this.filteredTimesheetData",this.filteredTimesheetData);
      } else {
        // this.openAlertMod(response.serviceResponse);
      }
    });

    this.getTimesheetDashboardCount(this.month, this.year);
  }

getCountByStatus(status: string) {
  switch(status) {

    case 'All':
      return this.dashboardObj.summary.totalApplicableCount;

    case 'Pending':
      return this.dashboardObj.summary.clientSidePendingCount;

    case 'Defaulter':
      return this.dashboardObj.summary.defaulterCount;

    case 'Approved':
      return this.dashboardObj.summary.approvedCount;

    case 'Total_defaulter':
      return this.dashboardObj.summary.totaldefaulterCount;

    default:
      return 0;
  }
}


  fillTimesheetDays(timesheetData: any = {}): any {
    const updated = { ...timesheetData };
    for (let d = 1; d <= 31; d++) {
      const key = 'd' + d;
      if (!updated[key]) {
        updated[key] = { status: 'NA' };
      } else if (!updated[key].status) {
        updated[key].status = 'NA';
      }
    }
    return updated;
  }

  generateDaysForMonth(date: Date) {
    const year = date.getFullYear();
    const month = date.getMonth();
    const daysCount = new Date(year, month + 1, 0).getDate();

    this.daysInMonth = Array.from({ length: daysCount }, (_, i) => {
      const day = i + 1;
      const weekday = new Date(year, month, day).toLocaleDateString('en-US', { weekday: 'short' }); // Mon, Tue...
      return { dayNumber: day, dayName: weekday };
    });
  }

  getAllEmployeeViewForClientAttendanceStatusForExcel(status: any, month: any, year: any): Promise<void> {
    return new Promise((resolve, reject) => {
      this.timesheetRequestDTO.status = status;
      this.timesheetRequestDTO.month1 = month;
      this.timesheetRequestDTO.year = year;
      this.timesheetRequestDTO.empId = this.currentUser.empId;
      this.timesheetRequestDTO.isClientDashboard = this.isClientDashboard;
      this.timesheetRequestDTO.page = this.page1;
      this.timesheetRequestDTO.size = this.pageSize;
      this.timesheetRequestDTO.dataForExcel = true;
      this.timesheetRequestDTO.sortDirection = 'asc';
      this.employeeExcelView = [];

      this.timesheetService.getEmployeeViewForClientAttendanceStatus(this.timesheetRequestDTO)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.employeeExcelView = response.serviceResponse;
              this.dataForExcel = false;
              this.totalItems = this.getCountByStatus(status);
              console.log("employeeExcelView ::::::", this.employeeExcelView);
              resolve();
            } else {
              this.openAlertMod1(this.alertTemplate, response.serviceResponse);
              reject(response.serviceResponse);
            }
          },
          error: (error) => {
            console.error("Error fetching Excel data:", error);
            reject(error);
          }
        });
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
    this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.fileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.fileType = 'image';
    } else {
      this.fileType = '';
    }


    // Open modal
    this.modalRef2 = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;

    if (!this.isSearchEnabled) {
      this.filters = {};
      this.toggleValue ? this.currentColumnFilter = { ...this.projectViewFilters } : this.currentColumnFilter = { ...this.employeeViewColumnsFilters };
      if (!this.toggleValue) {
        this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
      } else {
        this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
      }
    }
  }

  toggleSearchInInsight(): void {
    this.isInsightSearchEnabled = !this.isInsightSearchEnabled;

    if (!this.isInsightSearchEnabled) {
      this.currentColumnFilter = { ...this.timesheetSummaryColumnsFilters };
      this.getEmployeeTimesheetsByProject();
    }
  }

  sortData(sort: Sort, type?: any) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
    if (type == 'timesheet_summary_template') {
      this.getEmployeeTimesheetsByProject();
    }
    if (!this.toggleValue) {
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
    } else {
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }

  }

  onSearch(searchData: any) {
    this.filters = searchData;
  }

  async exportToExcel(): Promise<void> {
    this.excelName = "Employee Attendance View.xlsx";
    this.tableName = "Employee Info";
    // await this.getAllEmployeeViewForClientAttendanceStatusForExcel(this.status, this.month, this.year);

    // const exportData = this.employeeExcelView.map((x: any) => ({
    //   'Emp ID': x.employmentId || 'NA',
    //   'Employee': x.name || 'NA',
    //   'Billable': x.billable || 'NA',
    //   'Billable Type': x.billableType || 'NA',
    //   'Mobile No': x.mobileNo || 'NA',
    //   'Email': x.email || 'NA',
    //   'Department': x.departmentName || 'NA',
    //   'Expected DSR': x.expectedFillCount ?? x.expectedIshineFillCount ?? 0,
    //   // 'Ishine DSR': x.timesheetFilledCount ?? 0,
    //   'Client Attendance Filled': x.clientSideAttendancePendingCount ?? x.ishinePendingTimesheetCount ?? 0,
    //   'Client Approved': x.clientSideAttendanceApprovedCount ?? x.ishineApprovedTimesheetCount ?? 0,
    //   'Client Attendance Not Filled': x.clientSideAttendanceNotFilledCount ?? x.ishineNotFilledTimesheetCount ?? 0,
    //   'Project': x.projectName || 'NA',
    //   'Project Active': x.projectActive || 'NA',
    //   'PO No': x.poNo || 'NA',
    //   'Project Type': x.projectType || 'NA',
    //   'Manager': x.projectManagers || 'NA',
    //   'Client': x.clientName || 'NA',
    //   'Apmosys RM': x.apmosysRM || 'NA',
    //   'RM Email': x.apmosysRmEmail || 'NA',
    //   'Client RM': x.clientRM || 'NA',
    //   'Team': x.team || 'NA',
    //   'Team Lead': x.teamLeadName || 'NA'
    // }));
    // this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);

    this.getEmployeeSummaryOnExport(this.month, this.year);


  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }


  modalTitle = 'Timesheet Details';
  async exportToExcelEmployeeSummary(): Promise<void> {
    await this.getEmployeeTimesheetsByProjectForExcel(this.timesheetSummaryTemplate);
    const onlySpecificDataArr = this.employeeListAccordingToProjectForExcel.map(
      x => ({
        "Emp ID": x.employeementId,
        "Name": x.employeeName,
        "Project Name": x.projectName,
        "Expected Timesheet Count": x.expectedEODCount,
        "Total Applied Count": x.submittedCount,
        "Client Approved Timesheet Count": x.clientApprovedCount,
        "Client Attendance Pending Timesheet Count": x.clientPendingCount,
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }



  refreshTimesheet(template: TemplateRef<any>) {
    this.page = 1;

    console.log("Selected Employee ", this.selectedEmpId);
    console.log("Selected Fromdate ", this.fromDate);

    console.log("Selected ToDate ", this.toDate);


    if (!this.selectedEmpId || !this.fromDate || !this.toDate) {
      alert('Please select an employee and valid dates.');
      return;
    }

    this.timesheetObj.empId = this.selectedEmpId;
    // this.timesheetObj.fromDate = this.formatDate(this.fromDate);
    // this.timesheetObj.toDate = this.formatDate(this.toDate);
    this.timesheetObj.fromDate = this.fromDate;
    this.timesheetObj.toDate = this.toDate;

    console.log("empId ::::::::", this.timesheetObj.empId);
    console.log("fromDate ::::::::", this.timesheetObj.fromDate);
    console.log("toDate ::::::::", this.timesheetObj.toDate);

    this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj)
      .subscribe(
        (data: any[]) => {
          this.employeeTimesheet = data;

          const totalPages = Math.ceil(this.employeeTimesheet.length / 20);
          this.paginationArray = Array.from({ length: totalPages }, (_, i) => i + 1);

          console.log('Timesheet:', data);
          this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });

          // Reset form fields after search
          this.fromDate = null;
          this.toDate = null;
          this.timesheetObj.empId = '';
        },
        (error) => {
          console.error('Error fetching timesheet', error);
        }
      );
  }



  // monthGrid: DayCell[][] = [];
  selectedMonth = new Date();
  userName: string = '';
  weekDays: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  monthGrid: any[][] = [];

  legend: { [key: string]: { label: string; color: string } } = {
    O: { label: 'Other Project', color: '#1c1f23' },
    A: { label: 'Absent', color: '#8b0000' },
    NW: { label: 'Non-Working Day', color: '#343a40' },
    AH: { label: 'ApMoSys Holiday', color: '#0b3c5d' },
    WO: { label: 'Week Off', color: '#4b371c' },
    CO: { label: 'Comp Off', color: '#1b371c' },
    // H:   { label: 'Holiday',              color: '#5a4b00' },
    CH: { label: 'Client Holiday', color: '#3e2f1c' },
    CA: { label: 'Client Approved', color: '#003366' },
    CN: { label: 'Client Not-Approved', color: '#664400' },
    // 🔴 Rejected by RM (improved differentiation)
    CA_R: {
      label: 'Client Approved But Rejected By RM',
      color: '#B71C1C' // Dark red (high-impact rejection)
    },
    CN_R: {
      label: 'Client Not-Approved But Rejected By RM',
      color: '#E57373' // Soft red (lower severity rejection)
    },
    P: { label: 'Present', color: '#014421' },
    NA: { label: 'Not Applicable', color: '#2f4f4f' },
    L:{label:'On Leave', color:"#a3002c"},
    AP:{label:'Approved', color:"#298811ff"},
    PE:{label:'Pending', color:"#664400"}
  };



  legendEntries: { code: string; label: string; color: string }[] = [];

  // get legendEntries() {
  //   return Object.entries(this.legend).map(([code, { label, color }]) => ({ code, label, color }));
  // }

  monthSelected(event: Date, datepicker: any) {
    this.selectedMonth = new Date(event.getFullYear(), event.getMonth(), 1);
    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
    datepicker.close();
  }

  changeMonth(date: Date) {
    if (!date) return;
    this.selectedMonth = new Date(date.getFullYear(), date.getMonth(), 1);
    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
  }


  fetchTimesheetData(projectId: number, empId: number): void {
    this.selectedProjectId = projectId;
    this.selectedEmpId = empId;

    const payload = {
      empId: this.selectedEmpId,
      projectId: this.selectedProjectId,
      month: this.selectedMonth.getMonth() + 1,
      year: this.selectedMonth.getFullYear()
    }

    this.timesheetService.getEmployeeTimesheetAsCalender(payload)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success' && response.serviceResponse?.length) {
            this.timesheetCalender = response.serviceResponse;

            const employeeData = response.serviceResponse.find((emp: any) => emp.empId === empId);
            console.log("Filtered Employee Data", employeeData);
            if (employeeData) {
              this.userName = employeeData.employeeName;
              this.buildCalendarGrid(employeeData.timesheetData);
            } else {
              this.openAlertMod(this.alertTemplate, `Employee ID ${empId} not found in the data.`);
            }
          } else {
            this.openAlertMod(this.alertTemplate, response.serviceResponse || 'No data found.');
          }
        },
        error: (err) => {
          console.error('Error fetching timesheet data:', err);
          this.openAlertMod(this.alertTemplate, 'Something went wrong. Please try again later.');
        }
      });
  }





  buildCalendarGrid(timesheetData: { [key: string]: any }): void {
    const year = this.selectedMonth.getFullYear();
    const month = this.selectedMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();

    let grid: any[][] = [];
    let week: any[] = new Array(firstDay.getDay()).fill({});

    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const key = 'd' + day;
      const data = timesheetData[key];

      const dateObj: any = {
        day,
        date,
        isToday: this.isToday(date),
        isWeekend: date.getDay() === 0 || date.getDay() === 6,
        isHoliday: data?.status === 'H',
        attendance: data?.status || null,
        intime: data?.inTime || null,
        outtime: data?.outTime || null
      };

      week.push(dateObj);

      if (week.length === 7) {
        grid.push(week);
        week = [];
      }
    }

    // Push last week if not empty
    if (week.length > 0) {
      while (week.length < 7) week.push({});
      grid.push(week);
    }

    this.monthGrid = grid;
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  }

  generateMonthGrid() {
    // Replace this with your real backend data. For demo: random codes and times.
    const month = this.selectedMonth.getMonth();
    const year = this.selectedMonth.getFullYear();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayWeekIdx = (firstDayOfMonth.getDay() + 6) % 7; // Monday=0

    const daysInMonth = lastDayOfMonth.getDate();
    const today = new Date();
    let grid: DayCell[][] = [];
    let week: DayCell[] = [];

    const attendanceCodes = ['P', 'A', 'PT', 'RP', 'P', 'P', 'A'];
    const holidays = [13]; // Example: 13th is a holiday

    for (let i = 0; i < firstDayWeekIdx; ++i) {
      week.push({ date: new Date(0), day: '', isToday: false, isWeekend: false, isHoliday: false });
    }
    for (let day = 1; day <= daysInMonth; ++day) {
      const date = new Date(year, month, day);
      const weekday = (date.getDay() + 6) % 7;
      const isWeekend = weekday >= 5;
      const isHoliday = holidays.includes(day);
      const isToday = today.getFullYear() === year && today.getMonth() === month && today.getDate() === day;

      // Demo data - replace with API lookup!
      let attendance = '';
      let intime = '', outtime = '';
      if (isHoliday) {
        attendance = 'H';
      } else {
        attendance = attendanceCodes[(day + 1) % attendanceCodes.length];
        intime = attendance === 'A' ? '' : '09:' + (10 + day % 50).toString().padStart(2, '0');
        outtime = attendance === 'A' ? '' : '18:' + (20 + day % 40).toString().padStart(2, '0');
      }

      week.push({
        date, day, isToday, isWeekend, isHoliday,
        attendance, intime, outtime
      });
      if (week.length === 7) {
        grid.push(week);
        week = [];
      }
    }
    if (week.length) {
      while (week.length < 7) {
        week.push({ date: new Date(0), day: '', isToday: false, isWeekend: false, isHoliday: false });
      }
      grid.push(week);
    }
    this.monthGrid = grid;
  }



  getProjectViewForClientAttendanceStatus(status: any, month: any, year: any) {
    this.projectViewClient.status = status;
    this.projectViewClient.multiPOs = this.globalPoConflictSelection;
    this.projectViewClient.month1 = month;
    this.projectViewClient.year = year;
    this.projectViewClient.empId = this.currentUser.empId;
    // this.projectViewClient.page = this.page1
    // this.projectViewClient.size = this.pageSize
    this.projectViewClient.page=this.page1??1;
	  this.projectViewClient.size=this.pageSize??20;
    this.projectViewClient.isClientDashboard = this.isClientDashboard
    this.projectViewClient.dataForExcel = false;
    this.projectViewClient.billableTypes = this.selectedBillableTypes;
    this.projectViewClient.projectActive = this.selectedProjectStatus;
    this.projectViewClient.columnFilter = this.currentColumnFilter == null ? this.projectViewFilters : this.currentColumnFilter;
    this.projectViewClient.sortBy = this.sortColumn ?? 'project_name';
    this.projectViewClient.sortDirection = this.sortDirection ?? 'asc';

    console.log("test empId ", this.projectViewClient);
    this.projectView = [];
    this.timesheetService.getProjectViewForClientAttendanceStatus(this.projectViewClient).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.projectView = response.serviceResponse;
        this.totalItems = response.totalElements;
        this.dataForExcel = false;
      } else {
        this.openAlertMod1(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  getProjectViewForClientAttendanceStatusForExcel(status: any, month: any, year: any): Promise<void> {
    return new Promise((resolve, reject) => {
      this.projectViewClient.status = status;
      this.projectViewClient.month1 = month;
      this.projectViewClient.year = year;
      this.projectViewClient.empId = this.currentUser.empId;
      this.projectViewClient.page = this.page1;
      this.projectViewClient.size = this.pageSize;
      this.projectViewClient.isClientDashboard = this.isClientDashboard;
      this.projectViewClient.dataForExcel = true;

      console.log("test empId ", this.projectViewClient);

      this.timesheetService.getProjectViewForClientAttendanceStatus(this.projectViewClient)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.projectViewForExcel = response.serviceResponse;
              this.totalItems = response.totalElements;
              resolve();
            } else {
              this.openAlertMod1(this.alertTemplate, response.serviceResponse);
              reject(response.serviceResponse);
            }
          },
          error: (error) => {
            console.error("Error fetching project Excel data:", error);
            reject(error);
          }
        });
    });
  }


  onSearch2(searchData2: any) {
    this.filters = searchData2;
  }

// hoveredEmpId: number | null = null;

// showPopup(empId: number) {
//   clearTimeout(this.hideTimeout);
//   this.hoveredEmpId = empId;
// }


showPopup(empId: number) {
  clearTimeout(this.hideTimeout);
  this.hoveredEmpId = empId;
}

scheduleHidePopup() {
  this.hideTimeout = setTimeout(() => {
    this.hoveredEmpId = null;
  }, 200);
}

cancelHidePopup() {
  clearTimeout(this.hideTimeout);
}

  viewProfile(empId: string) {
    console.log('View profile:', empId);
    // navigation logic
  }

  viewCalender(email: string) {
    window.location.href = `mailto:${email}`;
  }

  viewDocuments(empId: string) {
    console.log('Employee Documents:', empId);
    // confirm and delete logic
  }
  goToCalendar(projectId: number, empId: number): void {
    this.router.navigate(['/calendar-view', projectId, empId]);
  }

  showCalendarView(projectId: number, empId: number): void {
    this.selectedProjectId = projectId;
    this.selectedEmpId = empId;
    this.showCalendar = true;
  }

  openCalendarInNewTab(project: any) {
    const urlTree = this.router.createUrlTree(
      ['/user-timesheet/calendar-view'],
      {
        queryParams: {
          projectId: project.projectId,
          empId: project.empId
        }
      }
    );

    const serializedUrl = this.router.serializeUrl(urlTree);

    const fullUrl = `${window.location.origin}/#${serializedUrl}`;

    console.log('Opening calendar view URL:', fullUrl);

    window.open(fullUrl, '_blank');
  }

  showProjectPopup(projectId: number): void {
    this.hoveredProjectId = projectId;
    clearTimeout(this.hidePopupTimeout);
  }

  scheduleHideProjectPopup(): void {
    this.hidePopupTimeout = setTimeout(() => {
      this.hoveredProjectId = null;
    }, 300);
  }

  cancelHideProjectPopup(): void {
    clearTimeout(this.hidePopupTimeout);
  }

  getTimesheetDashboardCount(month: any, year: any) {
    if (this.toggleValue) {
      this.timesheetService.getTimesheetDashboardCountForProject(month, year, this.currentUser.empId, this.isClientDashboard, this.selectedBillableTypes,this.selectedProjectStatus).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.dashboardObj = response.serviceResponse;
          console.log("Dashboard Obj ",this.dashboardObj);
          this.getTiles();
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
      });
    } else {
      console.log(this.globalPoConflictSelection,":poConflictSelectionByTile")
      console.log(this.selectedEmployeeStatus,":selectedEmployeeStatus")
      this.timesheetService.getTimesheetDashboardCountForEmployee(month, year, this.currentUser.empId, this.isClientDashboard, this.selectedBillableTypes,this.selectedEmployeeStatus,this.viewClientIdFlag,this.globalPoConflictSelection).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.dashboardObj = response.serviceResponse;
          this.getTiles();
          console.log("dashboardObj :::::::::", this.dashboardObj);
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
      });
    }
  }


  scrollToTable(status: string | null): void {
    this.selectedStatus = status;
    this.status = status;
      // Reset pagination
    this.page1 = 1;
  this.pageSize = 20;
    this.getTableData(status, this.month, this.year);
    const element = document.getElementById('table-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  getTableData(status: String | null, month: any, year: any,deptId?: string | null,isEmployeeRepeated: boolean = false, ) {
    if (!this.toggleValue) {
      this.getEmployeeViewForClientAttendanceStatus(status, month, year,deptId,isEmployeeRepeated);
    } else {
      this.getProjectViewForClientAttendanceStatus(status, month, year);
    }
  }

  monthSelected1(event: Date, datepicker: any) {
    const selected = new Date(event.getFullYear(), event.getMonth(), 1);
    const now = new Date();

    if (
      selected.getFullYear() > now.getFullYear() ||
      (selected.getFullYear() === now.getFullYear() && selected.getMonth() > now.getMonth())
    ) {
      console.warn('Future months are not allowed');
      this.openAlertMod1(this.alertTemplate, "Future months are not allowed!");
      datepicker.close();
      return;
    }

    this.selectedMonth1 = selected;
    this.month = selected.getMonth() + 1;
    this.generateDaysForMonth(this.selectedMonth1);
    this.year = selected.getFullYear();

    console.log("Selected Month:", this.month);
    console.log("Selected Year:", this.year);
    console.log("selectedMonth1 ::::::::", this.selectedMonth1);

    this.updateFormattedMonthLabel();

    this.getTimesheetDashboardCount(this.month, this.year);

    if (!this.toggleValue) {
      this.status = this.selectedStatus;
      this.loadDepartmentStatusSummary(this.status, this.month, this.year);
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
      this.getTimesheetDashboardCount(this.month, this.year);
    } else {
      this.status = this.selectedStatus;
      this.getTimesheetDashboardCount(this.month, this.year);
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
    // this.selectedStatus = this.status;
    datepicker.close();
  }



  changeMonth1(date: Date) {

    if (!date) return;
    this.selectedMonth1 = new Date(date.getFullYear(), date.getMonth(), 1);
    this.updateFormattedMonthLabel();
    this.generateDaysForMonth(this.selectedMonth1);

    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
  }



  // getFormattedMonthLabel(month: number, year: number): string {
  //   const monthNames = [
  //     'January', 'February', 'March', 'April', 'May', 'June',
  //     'July', 'August', 'September', 'October', 'November', 'December'
  //   ];
  //   return `${monthNames[month - 1]} ${year}`;
  // }

  updateFormattedMonthLabel() {
    this.formattedMonthLabel = this.selectedMonth1.toLocaleString('default', {
      month: 'short',
      year: 'numeric'
    });
  }

  exportToExcelForProjectView(): void {
    this.excelName = "Project View.xlsx";
    this.tableName = "Project Info";

    const exportData = this.projectView.map((project: any) => ({
      'Project': project.projectName || 'NA',
      'PO No': project.poNo || 'NA',
      'Manager': project.projectManagerName || 'NA',
      'Type': project.projectType || 'NA',
      'Client': project.clientName || 'NA',
      'Apmosys RM': project.apmosysRM || 'NA',
      'RM Email': project.apmosysRMEmail || 'NA',
      'Client RM': project.clientRM || 'NA',
      'Expected DSR': project.totalExpectedFillCount ?? 0,
      // 'iShine Filled': project.totalIshineFilledCount ?? 0, // Uncomment if needed
      'Clinet Approved': project.totalClientSideApprovedCount ?? 0,
      'Approved %': project.clientSideApprovedPercent ? `${project.clientSideApprovedPercent}%` : '0%',
      'Clinet Not-Approved': project.totalClientSidePendingCount ?? 0,
      'Pending %': project.clientSidePendingPercent ? `${project.clientSidePendingPercent}%` : '0%',
      'Client Attendance Not Filled': project.totalClientSideNotFilledCount ?? 0,
      'Not Filled %': project.clientSideNotFilledPercent ? `${project.clientSideNotFilledPercent}%` : '0%',
      'Project Status':project.active || 'NA'
    }));

    this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
  }

  // Prepare tiles based on dashboard toggle
getTiles() {
  this.tileGroups = []; // Reset groups

  const summary = this.dashboardObj.summary;
  const deptWise = this.dashboardObj.departmentWise || {};

  const getDepartments = (status: string) =>
    deptWise[status]?.departments || [];

  this.tileGroups = [];

  // -------------------------
  // CASE 1: toggleValue == false
  // -------------------------
  if (!this.toggleValue) {

    if (this.isClientDashboard) {

      this.tileGroups = [
        // GROUP 1
        [
          { status: 'All', label: 'Total Applicable Emp', value: summary.totalApplicableCount, class: 'border-start-primary bg-light-blue', icon: 'bi bi-people-fill', departments: getDepartments('All') },
          { status: 'Approved', label: 'Ready For Invoicing', value: summary.approvedCount, class: 'border-start-success bg-light-green', icon: 'bi bi-patch-check',departments: getDepartments('Approved') }
        ],

        // GROUP 2
        [
          { status: 'Total_defaulter', label: 'Defaulter', value: summary.totaldefaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle',departments: getDepartments('Defaulter') },
          { status: 'Pending', label: 'CS Approval Pending', value: summary.clientSidePendingCount, class: 'border-start-warning bg-light-yellow', icon: 'bi bi-hourglass-split',departments: getDepartments('ClientSidePending') },
          { status: 'Defaulter', label: 'IShine Not Filled', value: summary.defaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle',departments: getDepartments('Defaulter') }
        ]
      ];

    } else {

      this.tileGroups = [
        // GROUP 1
        [
          { status: 'All', label: 'Total Applicable Emp', value: summary.totalApplicableCount, class: 'border-start-primary bg-light-blue', icon: 'bi bi-people-fill' },
          { status: 'Approved', label: 'Ready For Invoicing', value: summary.approvedCount, class: 'border-start-success bg-light-green', icon: 'bi bi-patch-check' }
        ],

        // GROUP 2
        [
          { status: 'Total_defaulter', label: 'Defaulter', value: summary.totaldefaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle' },
          { status: 'Pending', label: 'CS Approval Pending', value: summary.clientSidePendingCount, class: 'border-start-warning bg-light-yellow', icon: 'bi bi-hourglass-split' },
          { status: 'Defaulter', label: 'IShine Not Filled', value: summary.defaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle' }
        ]
      ];
    }

    return; // Exit — done

  }

  // -------------------------
  // CASE 2: toggleValue == true
  // Return a single tile group
  // -------------------------
  if (this.isClientDashboard) {
    this.tileGroups = [
      [
        { status: 'All', label: 'Total Applicable Emp', value: this.dashboardObj.totalApplicableCount, class: 'border-start-primary bg-light-blue', icon: 'bi bi-people-fill' },
        { status: 'Approved', label: 'Ready For Invoicing', value: this.dashboardObj.approvedCount, class: 'border-start-success bg-light-green', icon: 'bi bi-patch-check' },
        { status: 'Pending', label: 'CS Approval Pending', value: this.dashboardObj.clientSidePendingCount, class: 'border-start-warning bg-light-yellow', icon: 'bi bi-hourglass-split' },
        { status: 'Defaulter', label: 'Defaulter', value: this.dashboardObj.defaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle' }
      ]
    ];
  } else {
    this.tileGroups = [
      [
        { status: 'All', label: 'Total Applicable Emp', value: this.dashboardObj.totalApplicableCount, class: 'border-start-primary bg-light-blue', icon: 'bi bi-people-fill' },
        { status: 'Approved', label: 'Ready For Invoicing', value: this.dashboardObj.approvedCount, class: 'border-start-success bg-light-green', icon: 'bi bi-patch-check' },
        { status: 'Pending', label: 'CS Approval Pending', value: this.dashboardObj.clientSidePendingCount, class: 'border-start-warning bg-light-yellow', icon: 'bi bi-hourglass-split' },
        { status: 'Defaulter', label: 'Defaulter', value: this.dashboardObj.defaulterCount, class: 'border-start-danger bg-light-red', icon: 'bi bi-exclamation-circle' }
      ]
    ];
  }

  this. handleDefaultTileSelection();

}


getTileColor(tileClass: string): string {
  if (tileClass.includes('border-start-primary')) return '#0d6efd';
  if (tileClass.includes('border-start-success')) return '#198754';
  if (tileClass.includes('border-start-warning')) return '#ffc107';
  if (tileClass.includes('border-start-danger'))  return '#dc3545';
  return '#6c757d';
}


getTileInfo(status: string): string[] {
  switch (status) {

    case 'All':
      return ['Represents the total number of employees required to submit timesheets for the selected month.',
'Calculated based on active project mapping for the selected month.',
'Considers the resource engagement period within the selected month.',
'Determined by the applicable billing type.'];

    case 'Approved':
      return ['Includes resources who have completed iShine timesheets for the selected period.',
'Confirms submission of valid client-side approval proof, where applicable (e.g., approved VMS timesheets).',
'Indicates no pending actions or approvals.',
'Records are fully compliant with timesheet requirements.',
'Resources in this status are eligible for invoicing.'];

    case 'Pending':
      return ['Includes resources who have filled iShine timesheets for the selected period.',
'Indicates that client-side approval has not yet been received.',
'Approved client-side documents are awaited from the client (not yet uploaded).',
'Typically applicable to resources with mandatory client-side IDs or client-side VMS systems.',
'Status applies when approval proof is pending for more than 2 days after timesheet completion.'];

    case 'Total_defaulter':
    case 'Defaulter':
      return ['Includes resources who have not filled iShine timesheets for two or more applicable days.',
'Also applies where required client-side approval proof is missing or delayed.',
'Indicates non-compliance with defined timelines.',
'Resources in this status are not eligible for invoicing.',
'Status is cleared only after all pending timesheet entries and approvals are completed.'];

    default:
      return ['Includes resources who have not filled their iShine timesheet for the selected period.',
'Status is shown irrespective of client-side attendance or approvals.',
'Indicates missing or incomplete timesheet entries in iShine.',
'Immediate action required from the resource.',
'If not resolved, the resource may be marked as a Defaulter.'];
  }
}




  onDashboardToggleChange() {
    this.page1 = 1;
    this.totalItems = 0;
    this.pageSize = 20;
    this.newSelectedStatus = "";

    console.log(this.status);

    if(this.selectedStatus == 'Total_defaulter'){
      this.newSelectedStatus = "All";
      this.status = "All"
    }else{
      this.status = this.selectedStatus;
    }

    this.isClientDashboard = !this.isClientDashboard;
    this.resetSearchField();
    this.isSearchEnabled = false;
    if (!this.toggleValue) {
      this.getEmployeeByNameAndEmpld();
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
      this.getTimesheetDashboardCount(this.month, this.year);
      this.ishineNotFilled();
    } else {
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
      this.getTimesheetDashboardCount(this.month, this.year);
      this.getProjectByNameAndPoNo();
      this.ishineNotFilled();
    }
    //  this.getEmployeeTimesheetsByProject(this.timesheetSummaryTemplate);
    this.selectedStatus = this.status;
  }
  resetSearchField() {
    this.insightPage = 1
    this.insightPageSize = 10
    this.insightPageTotalItems = 0
    this.fromDate = null;
    this.toDate = null;
    this.selectedProjectId = 0;
    this.selectedEmpId = 0;
    this.timesheetObj.empId = '';
    this.timesheetObj.projectId = '';
    this.employeeCtrl?.reset();
    this.projectPoCtrl?.reset();

    if (this.fromDateRef?.control) {
      this.fromDateRef.control.markAsPristine();
      this.fromDateRef.control.markAsUntouched();
    }

    if (this.toDateRef?.control) {
      this.toDateRef.control.markAsPristine();
      this.toDateRef.control.markAsUntouched();
    }
  }

  handleInsightPageChange(event, pageType) {
    if (this.insightPageSize != event.pageSize) {
      this.insightPage = 1;
      this.insightPageSize = event.pageSize;
    } else {
      this.insightPage = event.pageIndex + 1;
      this.insightPageSize = event.pageSize;
    }
    if (pageType == 'projectTimeSheetSearch') {
      this.getEmployeeTimesheetsByProject();
    } else if (pageType == 'empTimeSheetSearch') {
      this.searchTimesheet(null, null, false);
    }
  }
  onProjectViewSearch() {
    console.log("currentColumnFilter ::::::::", this.currentColumnFilter);
    if (
      !this.validateField(this.currentColumnFilter.poNo, /^[A-Za-z0-9/-]+$/, "Please enter valid PO Number.") ||
      !this.validateField(this.currentColumnFilter.totalEmployees, /^\d+$/, "Total Employees must be a number.") ||
      !this.validateField(this.currentColumnFilter.projectManagerName, /^[A-Za-z.,\s]+$/, "Manager name must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.startDate, /^\d{4}-\d{2}-\d{2}$/, "Please fill a proper date for start date.") ||
      !this.validateField(this.currentColumnFilter.endDate, /^\d{4}-\d{2}-\d{2}$/, "Please fill a proper date for end date.") ||
      !this.validateField(this.currentColumnFilter.projectType, /^[A-Za-z]+$/, "Project Type must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.clientName, /^[A-Za-z][A-Za-z.\s]*$/, "Client name must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.apmosysRm, /^[A-Za-z][A-Za-z.\s]*$/, "Apmosys RM must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.clientRm, /^[A-Za-z][A-Za-z.\s]*$/, "Client RM must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.totalExpectedFillCount, /^\d+$/, "Expected DSR must be a number.") ||
      !this.validateField(this.currentColumnFilter.totalClientSideApprovedCount, /^\d+$/, "Client Approved must be a number.") ||
      !this.validateField(this.currentColumnFilter.totalClientSidePendingCount, /^\d+$/, "Clinet Approval Pending must be a number.") ||
      !this.validateField(this.currentColumnFilter.totalClientSideNotFilledCount, /^\d+$/, "Clinet Attendance Not Filled must be a number.")
    ) {
      return;
    }
    this.page = 1;
    this.page1 = 1;
    this.pageSize = 20;
    this.totalItems = 0;
    this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
  }

 onEmployeeViewSearch() {
    if (
      !this.validateField(this.currentColumnFilter.employmentId, /^(a|ap)-\d{1,10}$|^\d{1,10}$/i, "Employment ID must be in format A-123456, AP-123456, or 123456'") ||
      !this.validateField(this.currentColumnFilter.clientSideId ,/^[A-Za-z][A-Za-z0-9\s]*$/, "Employee Name must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.employeeName, /^[A-Za-z][A-Za-z.\s]*$/, "Employee Name must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.employmentStatus ,/^[A-Za-z]+$/, "Employment status must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.projectStatus ,/^[A-Za-z]+$/, "Project status must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.department, /^[A-Za-z]+$/, "Department must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.billableType, /^[A-Za-z]+$/, "Billable Type must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.clientName, /^[A-Za-z][A-Za-z.\s]*$/, "Client name must only contain characters.") ||
      !this.validateField(this.currentColumnFilter.poNo, /^[A-Za-z0-9/.\s-]+$/, "Please enter valid PO Number.") ||
      !this.validateField(this.currentColumnFilter.projectManagerName, /^[A-Za-z.,\s]+$/, "Project Manager name must only contain characters.")
      // !this.validateField(this.currentColumnFilter.teamName,/^[A-Za-z0-9\/.\s]+$/, "Please enter valid Team Name .")
    ) {
      return;
    }
    this.page = 1;
    this.page1 = 1;
    this.pageSize = 20;
    this.totalItems = 0;
    console.log('Repeted Flag :::::::::',this.isEmployeeRepeatedFlag) ;

    if(this.isEmployeeRepeatedFlag === true){
    this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year,this.repetedDeptId,this.isEmployeeRepeatedFlag);

    }else{
    this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);

    }

  }

  onProjectInsightSearch() {
    if (
      !this.validateField(this.currentColumnFilter.employmentId, /^(a|ap)-\d{1,10}$|^\d{1,10}$/i, "Employment ID must be in format A-123456, AP-123456, or 123456'") ||
      !this.validateField(this.currentColumnFilter.name, /^[A-Za-z][A-Za-z.\s]*$/, "Employee Name must only contain characters.")
    ) {
      return;
    }
    this.insightPage = 1
    this.insightPageSize = 10
    this.insightPageTotalItems = 0
    this.getEmployeeTimesheetsByProject();

  }

  onlyCharaterCheck(columnValue: any) {
    if (!/^[A-Za-z]+(\.[A-Za-z]+)*$/.test(columnValue)) {
      return false;
    } else {
      return true;
    }
  }
  onlyDigitCheck(columnValue: any) {
    if (!/^\d+$/.test(columnValue)) {
      return false;
    } else {
      return true;
    }
  }

  validateField(value, validation, errorMessage) {
    if (value.trim() !== '' && !validation.test(value)) {
      this.openAlertMod1(this.alertTemplate, errorMessage);
      return false;
    }
    return true;
  }

  isFirstOccurrence(empId: any, index: number): boolean {
    return this.employeeView.findIndex(e => e.empId === empId) === index;
  }



  // onBillableTypeChange(event: any) {
  //   console.log('Selected Billable Type:', this.selectedBillableTypes);
  //   this.getTimesheetDashboardCount(this.month, this.year);
  //   if (!this.toggleValue) {
  //     this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
  //   } else {
  //     this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
  //   }
  // }

  dropdownOpen = false;

toggleDropdown() {
  this.dropdownOpen = !this.dropdownOpen;
}
allSelected: boolean = false;

toggleSelectAll() {
  if (this.selectedBillableTypes.length === this.billableTypes.length) {
    this.selectedBillableTypes = [];
  } else {
    this.selectedBillableTypes = [...this.billableTypes];
  }
  this.onBillableTypeChangeManual();
}

selectAllBillableTypes() {
  this.selectedBillableTypes = [...this.billableTypes];
  this.onBillableTypeChangeManual();
}

// deselectAllBillableTypes() {
//   this.billableTypes = [];
//   this.selectedBillableTypes = ['TNM'];
// this.onBillableTypeChangeManual();
// }

onBillableTypeChange(event: any) {
  const selected = event.value as string[];

  if (!selected || selected.length === 0) {
    this.selectedBillableTypes = ['TNM'];
  } else {
    this.selectedBillableTypes = selected;
  }

  this.onBillableTypeChangeManual();
}

onBillableTypeChangeManual() {
  console.log('Selected Billable Types:', this.selectedBillableTypes);

  this.getTimesheetDashboardCount(this.month, this.year);

  if (!this.toggleValue) {
    this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
  } else {
    this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
  }
}

    onProjectStatusChange(event: any) {
    console.log('Selected Project Status:', this.selectedProjectStatus);
    this.getTimesheetDashboardCount(this.month, this.year);
    if (!this.toggleValue) {
      // this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
    } else {
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
  }

      onEmployeeStatusChange(event: any) {
    console.log('Selected Project Status:', this.selectedProjectStatus);
    this.getTimesheetDashboardCount(this.month, this.year);
    if (!this.toggleValue) {
       this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
    } else {
      // this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
  }

  toggleMenu(): void {
    this.menuVisible = !this.menuVisible;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.col-md-1')) {
      this.menuVisible = false;
    }
  }

  exportToExcelMenu(type: string) {
    this.menuVisible = false;
    if (type === 'project') {
      this.exportProjectOverviewToExcel();
    } else if (type === 'projectWithEmployee') {
      this.getEmployeeSummaryOnExport(this.month, this.year);
    }
  }

  async exportProjectOverviewToExcel(): Promise<void> {
    const excelName = "Project Overview.xlsx";
    this.dataForExcel = true;
    await this.getProjectViewForClientAttendanceStatusForExcel(this.status, this.month, this.year);
    const exportData = this.projectViewForExcel.map((project: any) => ({
      'Project Name': project.projectName || 'NA',
      'PO Number': project.poNo || 'NA',
      'Resource Count': project.totalEmployees || 'NA',
      'Project Type': project.projectType || 'NA',
      'Project Manager': project.projectManagerName || 'NA',
      'Client': project.clientName || 'NA',
      'Apmosys RM': project.apmosysRM || 'NA',
      'Apmosys RM Email': project.apmosysRMEmail || 'NA',
      'Client RM': project.clientRM || 'NA',
      'Expected Fill Count': project.totalExpectedFillCount ?? 0,
      // 'iShine Filled Count': project.totalIshineFilledCount ?? 0,
      'Client Approval Pending %': (project.clientSidePendingPercent ?? 0) + '%',
      'Client Side Pending': project.totalClientSidePendingCount ?? 0,
      'Client Approved %': (project.clientSideApprovedPercent ?? 0) + '%',
      'Client Side Approved': project.totalClientSideApprovedCount ?? 0,
      'Client Attendance Not Filled %': (project.clientSideNotFilledPercent ?? 0) + '%',
      'Client Side Not Filled': project.totalClientSideNotFilledCount ?? 0,
      'Project Status':
            project.active === 'true'
              ? 'Active'
              : project.active === 'false'
                ? 'Inactive'
                : 'NA'
    }));
    this.exportExcelService.exportTableDataToExcel(exportData, excelName);
  }

  getEmployeeSummaryOnExport(month: any, year: any): void {
    this.timesheetAsCalenderByProjectId.month = month;
    this.timesheetAsCalenderByProjectId.year = year;
    this.timesheetAsCalenderByProjectId.empId = this.currentUser.empId;
    this.timesheetAsCalenderByProjectId.sortBy=this.sortColumn??'name';
	  this.timesheetAsCalenderByProjectId.sortDirection=this.sortDirection??'asc';
    this.timesheetAsCalenderByProjectId.status = this.status ;
    this.timesheetAsCalenderByProjectId.clientSideFilter = this.viewClientIdFlag;
    // this.timesheetAsCalenderByProjectId.clientSideFilter = this.viewClientIdFlag;
    // this.timesheetAsCalenderByProjectId.filters = this.employeeViewColumnsFilters;
    this.timesheetAsCalenderByProjectId.filters = this.currentColumnFilter == null ? this.employeeViewColumnsFilters : this.currentColumnFilter;

    if (!this.isClientDashboard) {
      this.timesheetAsCalenderByProjectId.allEmp = true;
      console.log("selectedBillableType ", this.selectedBillableTypes);
      this.timesheetAsCalenderByProjectId.billableType = this.selectedBillableTypes;
      this.timesheetAsCalenderByProjectId.projectActive = this.selectedProjectStatus;
    } else {
      this.timesheetAsCalenderByProjectId.allEmp = false;
    }

    this.timesheetService.getEmployeeSummaryOnExport(this.timesheetAsCalenderByProjectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.timesheetData = response.serviceResponse;
        this.exportToExcelForAllProject();
      } else {
        this.openAlertModAllEmployee(response.serviceResponse);
      }
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
    });
  }

  openAlertModAllEmployee(message: any) {
    this.modalRefAllEmployee = this.modalService.open(this.alertTemplate, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  closeAlertModAllEmployee() {
    this.modalRefAllEmployee.close();
  }

  exportToExcelForAllProject(): void {
    if (this.selectedStatus=='All') {
      if(this.toggleValue){
        this.excelName  = "Total_Applicable_Project.xlsx";

      }else{
        this.excelName  = "Total_Applicable_Employee.xlsx";

      }
    }
    if (this.selectedStatus=='Approved') this.excelName  = "Ready_For_Invoicing.xlsx";
    if (this.selectedStatus=='Pending') this.excelName  = "CS_Approval_Pending.xlsx";
    if (this.selectedStatus=='Defaulter') this.excelName  = "Defaulters.xlsx";
    if (this.selectedStatus=='Total_defaulter') this.excelName  = "Total_Defaulters.xlsx";

    // this.excelName = "All_Employee_Project_Overview.xlsx";
    this.tableName = "Employee Info";
    const legendColors = this.legend;
    const formatDateTime = (dateString: any) => {
    if (!dateString) return 'NA';

      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'NA';

      return `${date.getFullYear()}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date
        .getHours()
        .toString()
        .padStart(2, '0')}:${date
        .getMinutes()
        .toString()
        .padStart(2, '0')}:${date
        .getSeconds()
        .toString()
        .padStart(2, '0')}`;
    };

    const year = this.year || new Date().getFullYear();
    const month = (this.month ?? new Date().getMonth() + 1) - 1; // zero-based
    const daysCount = new Date(year, month + 1, 0).getDate();
    const daysInMonth = Array.from({ length: daysCount }, (_, i) => {
      const day = i + 1;
      const weekday = new Date(year, month, day).toLocaleDateString('en-US', { weekday: 'short' });
      return { dayNumber: day, dayName: weekday };
    });

    const exportData = this.timesheetData.map((x: any) => {
      console.log('Project Status Raw:', x.projectName, x.active);

      const baseData: any = {
        'Emp ID': x.employmentId || 'NA',
        'Client Side ID': x.clientSideId || 'NA',
        'Employee': x.employeeName || 'NA',
        'Employment Status': x.employmentStatus || 'NA',
        'Project Mapping': x.projectStatus || 'NA',
        'Department': x.department || 'NA',
        'Billable Type': x.billableType || 'NA',
        'Client': x.clientName || 'NA',
        'PO No': x.poNo || 'NA',
        'Project': x.projectName || 'NA',
        'Manager': x.projectManagerName || 'NA',
        'Team': x.teamName || 'NA',
        'Start Date': (x.startDate) || 'NA',
        'End Date': (x.endDate) || 'NA',
        'Expected': x.expectedTimesheetFillCount ?? 0,
        'Client Attendance Filled': x.apmosysTimesheetFilledCount ?? 0,
        'Client Attendance Not Filled': x.clientSideNotFilledCount ?? 0,
        'Client Not-Approved': x.clientSidePendingCount ?? 0,
        'Client Approved': x.clientSideApprovedCount ?? 0,
        // 'Project Status': x.projectActive || 'NA'
      };

      daysInMonth.forEach(day => {
        const dayKey = `d${day.dayNumber}`;
        const dayData = x.timesheetData?.[dayKey];
        baseData[`${day.dayName}-${day.dayNumber}`] = dayData ? `${dayData.status || '-'}` : '-';
      });

      baseData['Present'] = x.present ?? 0;
      baseData['Ready For Invoicing'] = x.readyForInvoicing ?? 0;
      baseData['WeekOff'] = x.weekOff ?? 0;
      baseData['Holiday'] = x.holiday ?? 0;
      baseData['Leave'] = x.leave ?? 0;
      baseData['CompOff'] = x.compOff ?? 0;
      baseData['Absent/OtherProject'] = x.na ?? 0;
      baseData['HalfDay'] = x.halfDay ?? 0;
      baseData['TotalNoOfDays'] = x.totalNoOfDays ?? 0;

      return baseData;
    });

    const columns = [
      'Emp ID', 'Client Side ID', 'Employee', 'Employment Status', 'Project Mapping', 'Department',
      'Billable Type', 'Client', 'PO No', 'Project', 'Manager', 'Team',
      'Start Date', 'End Date', 'Expected', 'Client Attendance Filled',
      'Client Attendance Not Filled', 'Client Not-Approved', 'Client Approved',
      // 'Project Status',
      ...daysInMonth.map(d => `${d.dayName}-${d.dayNumber}`),
      'Present', 'Ready For Invoicing', 'WeekOff', 'Holiday',
      'Leave', 'CompOff', 'Absent/OtherProject', 'HalfDay', 'TotalNoOfDays'
    ];

    const worksheet = XLSX.utils.json_to_sheet(exportData, { header: columns });

    columns.forEach((col, colIndex) => {
      const cell = XLSX.utils.encode_cell({ c: colIndex, r: 0 });

      const isSundayHeader = col.startsWith('Sun-');
      const fillColor = isSundayHeader ? "9BA6B1" : "193D8A";

      if (worksheet[cell]) {
        worksheet[cell].s = {
          font: { bold: true, color: { rgb: "FFFFFF" } },
          fill: { fgColor: { rgb: fillColor } },
          alignment: { horizontal: "center", vertical: "center", wrapText: true },
          border: {
            top: { style: "thin", color: { rgb: "000000" } },
            bottom: { style: "thin", color: { rgb: "000000" } },
            left: { style: "thin", color: { rgb: "000000" } },
            right: { style: "thin", color: { rgb: "000000" } }
          }
        };
      }
    });

    exportData.forEach((row, rowIndex) => {
      if (row['Employment Status'] === 'InActive') {
        const empColIndex = columns.indexOf('Employee');
        const empCell = XLSX.utils.encode_cell({ c: empColIndex, r: rowIndex + 1 });
        if (worksheet[empCell]) {
          worksheet[empCell].s = {
            font: { color: { rgb: "FFFFFF" }, bold: true },
            fill: { fgColor: { rgb: "FF0000" } },
            alignment: { horizontal: "center", vertical: "center" },
            border: {
              top: { style: "thin", color: { rgb: "000000" } },
              bottom: { style: "thin", color: { rgb: "000000" } },
              left: { style: "thin", color: { rgb: "000000" } },
              right: { style: "thin", color: { rgb: "000000" } }
            }
          };
        }
      }

          // Apply color styling for each day column
      daysInMonth.forEach(day => {
        const colName = `${day.dayName}-${day.dayNumber}`;
        const colIndex = columns.indexOf(colName);
        const status = row[colName];
        const color = legendColors[status]?.color?.replace('#', '').toUpperCase() || '999999';
        const cell = XLSX.utils.encode_cell({ c: colIndex, r: rowIndex + 1 });

        if (worksheet[cell]) {
          worksheet[cell].s = {
            font: { color: { rgb: "FFFFFF" }, bold: true },
            fill: { fgColor: { rgb: color } },
            alignment: { horizontal: "center", vertical: "center" },
            border: {
              top: { style: "thin", color: { rgb: "000000" } },
              bottom: { style: "thin", color: { rgb: "000000" } },
              left: { style: "thin", color: { rgb: "000000" } },
              right: { style: "thin", color: { rgb: "000000" } }
            }
          };
        }
      });
    });

    const wideColumns = [
      'Present', 'Ready For Invoicing', 'WeekOff', 'Holiday',
      'Leave', 'CompOff', 'Absent/OtherProject', 'HalfDay', 'TotalNoOfDays'
    ];

    worksheet['!cols'] = columns.map((col, index) => {
      if (index < 20) return { wch: 20 };
      if (wideColumns.includes(col)) return { wch: 20 };
      return { wch: 8 }; // days slightly wider now
    });

    worksheet['!freeze'] = { xSplit: 0, ySplit: 1 };

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, this.tableName);
    XLSX.writeFile(workbook, this.excelName);
  }

// toggleExpand(empId: any) {
//   if (!(empId in this.isExpanded)) {
//     this.isExpanded[empId] = false;
//   }
//   this.isExpanded[empId] = !this.isExpanded[empId];
// }
  expandedEmployees: Set<number> = new Set();


  toggleEmployeeExpand(empId: number): void {
    if (this.expandedEmployees.has(empId)) {
      this.expandedEmployees.delete(empId);
    } else {
      this.expandedEmployees.add(empId);
    }
  }

  isEmployeeExpanded(empId: number): boolean {
    return this.expandedEmployees.has(empId);
  }

toggleExpand(empId: any) {
  this.isExpanded[empId] = !this.isExpanded[empId];
}

trackByEmp(index: number, emp: any) {
  return emp.empId || index;
}
trackByProj(index: number, proj: any) {
  return proj.projectId || index;
}

// toggleClientId(event:Event){

//   const isChecked = (event.target as HTMLInputElement).checked;
//   console.log('Toggle is now:', isChecked);

//   // Call your desired logic here
//   // Example: update a property used for toggling rows
//   this.viewClientIdFlag = !this.viewClientIdFlag;
//   this.page1 = 1;
//   this.totalItems = 0;
//   this.pageSize = 10;
//   this.isSearchEnabled = false;

//   this.getTimesheetDashboardCount(this.month, this.year);

//   // if (!this.toggleValue) {

//   //   this.status = 'All';
//     this.currentColumnFilter = { ...this.employeeViewColumnsFilters };
//     this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
//   // }
//   // else {
//   //   this.status = 'All';
//   //   this.currentColumnFilter = { ...this.projectViewFilters };
//   //   this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
//   // }

// }


// Updated method for select dropdown
onClientIdFilterChange(event: any): void {
  const selectedValue = event.target.value;
  console.log('Client ID filter changed to:', selectedValue);

  // Update the filter value
  this.viewClientIdFlag = selectedValue;

  // Reset pagination and search
  this.page1 = 1;
  this.totalItems = 0;
  this.pageSize = 20;
  this.isSearchEnabled = false;

  this.getTimesheetDashboardCount(this.month, this.year);


    this.currentColumnFilter = { ...this.employeeViewColumnsFilters };
    this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);

}
isTileSelected(status: string | null): boolean {
  return this.selectedStatus === status;

}

showTabs = false;

anomalyTabs = [
{ title: 'Missing Attendance', count: 12 },
{ title: 'Incorrect Check-In', count: 4 },
{ title: 'Missing Project Tag', count: 9 },
{ title: 'Overlapping Entries', count: 3 }
];


toggleTabs() {
this.showTabs = !this.showTabs;
}

onTileClick(tile: any) {
  if (this.selectedTile?.status === tile.status) {
    this.selectedTile = null;
    this.selectedDepartments = [];
  } else {
    this.selectedTile = tile;
    this.selectedDepartments = tile.departments || [];
    this.isDeptCollapsed = false; 
  }

  this.scrollToTable(tile.status);
}


onDepartmentClick(status: string, deptId: string) {
  console.log('Department clicked:', status, deptId);

  // Example: scroll / filter table
  this.scrollToTableBasedOnDept(status,deptId);

  // OR emit / set filters
  // this.selectedDeptCode = deptCode;
}

toggleDeptCollapse() {
  this.isDeptCollapsed = !this.isDeptCollapsed;
}

  scrollToTableBasedOnDept(status: string | null , deptId?: string,isEmployeeRepeated: boolean = false): void {
    this.selectedStatus = status;
    this.status = status;
    this.selectedDeptId = deptId ?? null;
      // Reset pagination
    this.page1 = 1;
  this.pageSize = 20;
    this.getTableData(status, this.month, this.year ,this.selectedDeptId,isEmployeeRepeated);
    const element = document.getElementById('table-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  handleDefaultTileSelection() {
 const defaultStatus = 'All';

  const defaultTile = this.tileGroups
    ?.flat()
    .find(tile => tile.status === defaultStatus);

  if (defaultTile) {
    this.onTileClick(defaultTile);
  }
}

//Pivot table changes dept wise

toggleDeptTableCollapse(): void {
  this.isDeptTableCollapsed = !this.isDeptTableCollapsed;

  if (!this.isDeptTableCollapsed && this.departmentTableData.length === 0) {
    this.loadDepartmentStatusSummary(this.status, this.month, this.year);
  }
}

loadDepartmentStatusSummary(status: any, month: any, year: any): void {

  this.allowedEmpid = this.currentUser.empId ;
  if(this.allowedEmpid == 6){
  this.authorizedEmp = true ;
  }

  // this.authorizedEmp = true ;

  const payload = {
    empId:this.currentUser.empId,
    month: this.month,
    year: this.year,
    clientDashboard: this.isClientDashboard,
    billableTypes:this.selectedBillableTypes,
    employeeActive:this.selectedEmployeeStatus

  };

  this.isDeptTableLoading = true;

this.timesheetService.getDepartmentStatusSummary(payload)
  .subscribe({
    next: (res: any) => {
      this.departmentTableData = res?.serviceResponse || [];
      console.log("Row data :::::",this.departmentTableData);
      this.isDeptTableLoading = false;
    },
    error: (err) => {
      console.error('Error loading department summary', err);
      this.departmentTableData = [];
      this.isDeptTableLoading = false;
    }
  });
}

private readonly STATUS_MAP: { [key: string]: string } = {
  TOTAL: 'All',
  READY: 'Approved',
  PENDING: 'Pending',
  DEFAULTER: 'Total_defaulter',
  NOT_FILLED: 'Defaulter'
};

onDeptCountClick(row: any, type: string): void {

  const status = this.STATUS_MAP[type];

  if (!status) {
    console.warn('Unknown click type:', type);
    return;
  }

  const deptId = row.deptId; 

  this.scrollToTableBasedOnDept(status, deptId);
}

onRepeatClick(
  event: MouseEvent,
  row: any,
  type: 'APPROVED_REPEAT' | 'PENDING_REPEAT' | 'DEFAULTER_REPEAT'
): void {

  event.stopPropagation();

  const deptId = row.deptId;

  let status = '';

  switch (type) {
    case 'APPROVED_REPEAT':
      status = 'Approved';
      break;

    case 'PENDING_REPEAT':
      status = 'Pending';
      break;

    case 'DEFAULTER_REPEAT':
      status = 'Defaulter';
      break;
  }

  console.log('Hiiiii :::::',deptId);
  // 👉 Call your existing flow
   this.scrollToTableBasedOnDept(status, deptId,true);
}


@HostListener('document:click', ['$event'])
onOutSideClick(event: MouseEvent): void {
  const target = event.target as HTMLElement;

  // keep your existing close logic if any
  if (!target.closest('.col-md-1')) {
    this.menuVisible = false;
  }

  // close global dropdown on outside click
  if (!target.closest('.po-global-filter')) {
    this.isGlobalPoDropdownOpen = false;
  }
}

toggleGlobalPoConflictDropdown(event: MouseEvent) {
  event.stopPropagation();
  this.isGlobalPoDropdownOpen = !this.isGlobalPoDropdownOpen;
}

getGlobalPoConflictLabel(): string {
  if (this.globalPoConflictSelection === 'Yes') return 'PO Mapping: With Conflict';
  if (this.globalPoConflictSelection === 'No') return 'PO Mapping: Without Conflict';
  return 'PO Mapping: All';
}

selectGlobalPoConflict(val: 'Yes' | 'No' | 'All', event: MouseEvent) {
  event.stopPropagation();
  this.globalPoConflictSelection = val;
  this.isGlobalPoDropdownOpen = false;
  // Refresh current view + tiles count
  this.getTableData(this.selectedStatus, this.month, this.year, this.selectedDeptId, this.isEmployeeRepeatedFlag);
  this.getTimesheetDashboardCount(this.month, this.year);
}

clearGlobalPoConflict(event: MouseEvent) {
  event.stopPropagation();
  this.globalPoConflictSelection = 'All';
  this.isGlobalPoDropdownOpen = false;

  this.getTableData(this.selectedStatus, this.month, this.year, this.selectedDeptId, this.isEmployeeRepeatedFlag);
  this.getTimesheetDashboardCount(this.month, this.year);
}


}
