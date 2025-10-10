import { Directive,AfterViewInit, Component, ElementRef, TemplateRef, ViewChild, Input } from '@angular/core';
import { FormControl, NgModel } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import * as Highcharts from 'highcharts';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
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
  selector: 'app-hr-dashboard',
  templateUrl: './hr-dashboard.component.html',
  styleUrls: ['./hr-dashboard.component.css']
})
export class HrDashboardComponent implements AfterViewInit {

  @ViewChild('vmsChartContainer', { static: false }) vmsChartContainer!: ElementRef;
  @ViewChild('ishineChartContainer', { static: false }) ishineChartContainer!: ElementRef;
  @ViewChild('departmentChartContainer', { static: false }) departmentChartContainer!: ElementRef;
  @ViewChild('docRejectChart', { static: false }) docRejectChart!: ElementRef;
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  modalRef2?: BsModalRef;
  modalRef?: BsModalRef;
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
  employeeView:GetEmployeeViewForClientAttendanceStatus[] = [];
  employeeExcelView:GetEmployeeViewForClientAttendanceStatus[] = [];
  employeeViewColumns: any[] = ['employmentId', 'name', 'billable', 'billableType', 'mobileNo', 'email', 'departmentName', 'expectedFillCount', 'timesheetFilledCount', 'clientSideAttendancePendingCount', 'clientSideAttendanceApprovedCount', 'clientSideAttendanceNotFilledCount', 'projectName', 'poNo', 'projectType', 'projectManagers', 'clientName', 'apmosysRm', 'apmosysRmEmail', 'clientRm', 'team', 'teamLeadName'];
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
  projectView:ProjectViewForTimesheet[] = [];
  projectViewForExcel:ProjectViewForTimesheet[] = [];
  projectViewColumns: any[] = ['projectName','poNo','projectManagerName','projectType','clientName','apmosysRM','apmosysRMEmail','clientRM','totalExpectedFillCount','totalClientSideApprovedCount','clientSideApprovedPercent','totalClientSidePendingCount','clientSidePendingPercent','totalClientSideNotFilledCount','clientSideNotFilledPercent'];
  timesheetSummaryColumns:any[]=['blank','employeementId','employeeName','projectName','expectedEODCount','submittedCount','clientApprovedCount','clientPendingCount'];
  totalClientSideApprovedCount: any;
  eodNotFilledCount: any;
  totalClientSidePendingCount: any;
  totalDocumentApprovedCount: any;
  totalDocumentPendingCOunt: any;
  totalDocumentPendingCount: any;
  totalDocumentRejectedCount: any;
  toggleValue: Boolean=false;
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
  month :any;
  year :any;
  formattedMonthLabel: string;
  selectedMonth1: Date;
  currentUser: User;
  projectObj:Project=new Project();
  isClientDashboard: Boolean=true;
  dataForExcel: Boolean=false;


  //pagination
  page1: number = 1;
  totalItems:number = 0;
  pageSize:number = 10;

  //InsightPagination
  insightPage: number = 1;
  insightPageTotalItems:number = 0;
  insightPageSize:number = 10;



  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: BsModalService,
    private projectService:ProjectService,
    private resourceManagementService: ResourceManagementService,
    private exportExcelService: ExportExcelService,
    private sanitizer: DomSanitizer,private router: Router,
    private authenticationService: AuthenticationService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {

    const today = new Date();
    this.month = today.getMonth() + 1; // Months are 0-indexed in JS
    this.year = today.getFullYear();
  
    this.selectedMonth1 = new Date(this.year, this.month - 1, 1); 
    this.updateFormattedMonthLabel();
    this.legendEntries = Object.entries(this.legend).map(([code, value]) => ({
      code,
      label: value.label,
      color: value.color
    }));
    this.toggleValue = true;
    if(this.toggleValue){
   
      this.getProjectViewForClientAttendanceStatus(this.status,this.month,this.year);
    }
    this.getTimesheetDashboardCount(this.month,this.year);
    // this.generateMonthGrid();
    // this.fetchTimesheetData(this.selectedProjectId ,this.selectedEmpId);
    this.setLastUpdatedTime();
    this.getEmployeeByNameAndEmpld();
    this.getProjectByNameAndPoNo();
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
      title: { text: 'VMS Status Distribution', align: 'center' },
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
      console.log('Selected Employee ID:', this.selectedProjectId,selectedProject);

    }
  }
onDateRangeChange(): void {
  // You might add logic here to validate the date range, etc.
}

alertMessage: any;



openAlertMod(template: TemplateRef<any>, message: any) {
  this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  this.alertMessage = message;
}

openAlertMod1(template1: TemplateRef<any>, message: any) {
  this.modalRef2 = this.modalService.show(template1, { class: 'modal-sm' });
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
//         this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
//         this.fromDate = null;
//         this.toDate = null;
//         this.timesheetObj.empId = '' ;
//       },
//       (error) => {
//         console.error('Error fetching timesheet', error);
//       }
      
//     );
// }

searchTimesheet(template?: TemplateRef<any>, template1?: TemplateRef<any>,openModal: boolean = false) {
  if (!this.selectedEmpId || !this.fromDate || !this.toDate) {
    // alert('Please select an employee and valid datessss.');
        this.alertMessage = "Kindly provide all necessary details to search the timesheet  !!";
    this.openAlertMod1(template1, this.alertMessage);
    return;
  }

  // if (!this.selectedEmpId || this.selectedEmpId === null || this.selectedEmpId === '') {
  //   this.alertMessage = "Please enter Employee Name !!";
  //   this.openAlertMod(template, this.alertMessage);
  //   return;
  // }
  
  // if (!this.fromDate || this.fromDate === null ) {
  //   this.alertMessage = "Please enter Valid From Date !!";
  //   this.openAlertMod(template, this.alertMessage);
  //   return;
  // }
  
  // if (!this.toDate || this.toDate === null ) {
  //   this.alertMessage = "Please enter Valid To Date !!";
  //   this.openAlertMod(template, this.alertMessage);
  //   return;
  // }

  this.timesheetObj.empId = this.selectedEmpId;
  this.timesheetObj.fromDate = this.formatDate(this.fromDate);
  this.timesheetObj.toDate = this.formatDate(this.toDate);
  // this.timesheetObj.fromDate = this.fromDate;
  // this.timesheetObj.toDate = this.toDate;
  this.timesheetObj.page=this.insightPage
	this.timesheetObj.size=this.insightPageSize
  this.employeeTimesheet = [];
  this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj).subscribe(
    (data: any) => {
      if (data.serviceStatus === 'Success') {
        this.employeeTimesheet = data.serviceResponse;
        this.insightPageTotalItems = data.totalElements;
      }
      if (openModal && template) {
        this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
      }
      // Optional: reset fields if needed only during modal opening
      // if (openModal) {
      //   this.fromDate = null;
      //   this.toDate = null;
      //   this.timesheetObj.empId = '';
      // }
    },
    (error) => {
      console.error('Error fetching timesheet', error);
    }
  );

}

handlePageChange(event) {  
  if(this.pageSize != event.pageSize){
    this.page = 1;
    this.page1 = 1;
    this.pageSize = event.pageSize;
  }else{
    this.page = event.pageIndex+1;
    this.page1 = event.pageIndex+1;
    this.pageSize = event.pageSize;
  }
  if(!this.toggleValue){
      this.getEmployeeViewForClientAttendanceStatus(this.status,this.month,this.year);
    } else {
      this.getProjectViewForClientAttendanceStatus(this.status,this.month,this.year);
    }
}

formatDate(date: Date): string {
  return date.toISOString().split('T')[0];
}
cancelRequest() {
  if(this.modalRef){
  this.modalRef.hide();
  }
}
cancelRequest1() {
  this.modalRef.hide();
  this.modalRef2.hide();
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
  this.getTimesheetDashboardCount(this.month,this.year);
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
      console.log("Raw response:", response.serviceResponse);
      const nestedArray = response.serviceResponse;
      this.totalVmsFilledCount = nestedArray?.[0]?.[0] ?? 0;

      console.log("Total VMS completion:", this.totalVmsFilledCount);
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
      console.log("Total Ishine completion",this.totalIshineFilledCount);
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
  this.timesheetObj.empId= this.currentUser.empId;
  this.timesheetService.totalvmsNotFilled(this.timesheetObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      const nestedArray = response.serviceResponse;
      this.totalvmsNotFilled = nestedArray?.[0]?.[0] ?? 0;
      console.log("Total VMS not completion",this.totalvmsNotFilled);
    } else {
     // this.openAlertMod(this.alertTemplate, response.serviceResponse);
    }
  });
}

async ishineNotFilled() {
  this.timesheetObj.empId = this.currentUser.empId;
  this.timesheetObj.isClientDashboard=this.isClientDashboard;

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
      console.log("Total Ishine NOt completion",this.totalIshineNotFilledCount);
    } else {
     // this.openAlertMod(this.alertTemplate, response.serviceResponse);
    }
  });
}

onToggleChange(event: Event) {
  // Cast event target as HTMLInputElement to read checked property
  const isChecked = (event.target as HTMLInputElement).checked;
  console.log('Toggle is now:', isChecked);

  // Call your desired logic here
  // Example: update a property used for toggling rows
  this.toggleValue = !this.toggleValue;
  this.page1 = 1;
  this.totalItems = 0;
  this.pageSize = 10;

  this.getTimesheetDashboardCount(this.month,this.year);

  if(!this.toggleValue){
    this.status = 'All';
    this.getEmployeeViewForClientAttendanceStatus(this.status,this.month,this.year);
  } else {
    this.status = 'All';
    this.getProjectViewForClientAttendanceStatus(this.status,this.month,this.year);
  }

  // Add any other side effects or function calls you want here
}

filteredProject:any[]=[];
projectList:any[]=[];
  getProjectByNameAndPoNo() {
    this.projectObj.empId = this.currentUser.empId;
    this.projectObj.page =10
    this.projectObj.size=10
    this.projectObj.isClientDashboard=this.isClientDashboard;
    this.projectService.getProjectWithCliendSideID(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.projectList = response.serviceResponse;
        this.filteredProject =  this.projectList;
        console.log("test", this.filteredProject)
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }
employeeListAccordingToProject:any[]=[];
openProjectInsightModal() {
  if (!this.selectedProjectId || !this.fromDate || !this.toDate) {
     this.openAlertMod(this.alertTemplate, 'Please select an project and valid dates.');
    return;
  }
  this.getEmployeeTimesheetsByProject();
  this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
}  
getEmployeeTimesheetsByProject() {
  this.timesheetObj.projectId = this.selectedProjectId;
  this.timesheetObj.fromDate = this.formatDate(this.fromDate);
  this.timesheetObj.toDate = this.formatDate(this.toDate);
  this.timesheetObj.page=this.insightPage;
	this.timesheetObj.size=this.insightPageSize;
  this.timesheetObj.isClientDashboard=this.isClientDashboard
  this.timesheetObj.dataForExcel=false;

    this.projectService.getEmployeeTimesheetsByProject(this.timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.employeeListAccordingToProject = response.serviceResponse;
        this.insightPageTotalItems = response.totalElements;
        console.log("test",this.employeeListAccordingToProject);
        // this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
        // this.fromDate = null;
        // this.toDate = null; 
        // this.timesheetObj.projectId = '' ;
      } else {
        // this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  employeeListAccordingToProjectForExcel:any[]=[];
getEmployeeTimesheetsByProjectForExcel(template: TemplateRef<any>): Promise<void> {
  return new Promise((resolve, reject) => {
    this.page = 1;

    if (!this.selectedProjectId || !this.fromDate || !this.toDate) {
      this.openAlertMod(this.alertTemplate, 'Please select a project and valid dates.');
      reject('Invalid project or date selection');
      return;
    }

    this.timesheetObj.projectId = this.selectedProjectId;
    this.timesheetObj.fromDate = this.formatDate(this.fromDate);
    this.timesheetObj.toDate = this.formatDate(this.toDate);
    this.timesheetObj.page=this.page1;
    this.timesheetObj.size=this.pageSize;
    this.timesheetObj.isClientDashboard=this.isClientDashboard;
    this.timesheetObj.dataForExcel=true;
    this.projectService.getEmployeeTimesheetsByProject(this.timesheetObj)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.employeeListAccordingToProjectForExcel = response.serviceResponse;
            this.totalItems = response.totalElements;
            this.dataForExcel = false;
            this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
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


 timesheetRequestDTO :any ={
  status:'',
  month1:null,
  year: null
}
  getEmployeeViewForClientAttendanceStatus(status:any,month:any,year:any) {
    this.timesheetRequestDTO.status=status;
    this.timesheetRequestDTO.month1=month;
    this.timesheetRequestDTO.year=year;
    this.timesheetRequestDTO.empId = this.currentUser.empId;
    this.timesheetRequestDTO.isClientDashboard=this.isClientDashboard;
    this.timesheetRequestDTO.page=this.page1;
	  this.timesheetRequestDTO.size=this.pageSize;
    this.timesheetRequestDTO.dataForExcel=false;
    this.employeeView=[];
    this.timesheetService.getEmployeeViewForClientAttendanceStatus(this.timesheetRequestDTO).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.employeeView = response.serviceResponse;
        this.totalItems = response.totalElements;

        console.log("employeeView ::::::",this.employeeView);
      } else {
        this.openAlertMod1(this.alertTemplate, response.serviceResponse);
      }
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
    this.employeeExcelView = [];

    this.timesheetService.getEmployeeViewForClientAttendanceStatus(this.timesheetRequestDTO)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.employeeExcelView = response.serviceResponse;
            this.dataForExcel = false;
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
      this.modalRef2 = this.modalService.show(this.previewModal, { class: 'modal-lg' });
    }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;

    if (!this.isSearchEnabled) {
      this.filters = {};
    }
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

  onSearch(searchData: any) {
    this.filters = searchData;
  }

  async exportToExcel(): Promise<void> {
    this.excelName = "Employee Attendance View.xlsx";
    this.tableName = "Employee Info";
    await this.getAllEmployeeViewForClientAttendanceStatusForExcel(this.status,this.month,this.year);

    const exportData = this.employeeExcelView.map((x: any) => ({
      'Emp ID': x.employmentId || 'NA',
      'Employee': x.name || 'NA',
      'Billable': x.billable || 'NA',
      'Billable Type': x.billableType || 'NA',
      'Mobile No': x.mobileNo || 'NA',
      'Email': x.email || 'NA',
      'Department': x.departmentName || 'NA',
      'Expected DSR': x.expectedFillCount ?? 0,
      // 'Ishine DSR': x.timesheetFilledCount ?? 0,
      'Client Filled': x.clientSideAttendancePendingCount ?? 0,
      'Client Approved': x.clientSideAttendanceApprovedCount ?? 0,
      'Client Not Filled': x.clientSideAttendanceNotFilledCount ?? 0,
      'Project': x.projectName || 'NA',
      'PO No': x.poNo || 'NA',
      'Project Type': x.projectType || 'NA',
      'Manager': x.projectManagers || 'NA',
      'Client': x.clientName || 'NA',
      'Apmosys RM': x.apmosysRM || 'NA',
      'RM Email': x.apmosysRmEmail || 'NA',
      'Client RM': x.clientRM || 'NA',
      'Team': x.team || 'NA',
      'Team Lead': x.teamLeadName || 'NA'
  }));
  this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
}

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }

 
modalTitle = 'Timesheet Details';
async  exportToExcelEmployeeSummary(): Promise<void> {
    await  this.getEmployeeTimesheetsByProjectForExcel(this.timesheetSummaryTemplate);
      const onlySpecificDataArr = this.employeeListAccordingToProjectForExcel.map(
        x => ({
          "Emp ID": x.employeementId,
          "Name": x.employeeName,
          "Project Name": x.projectName,
          "Expected Timesheet Count": x.expectedEODCount,
          "Total Applied Count": x.submittedCount,
          "Client Approved Timesheet Count": x.clientApprovedCount,
          "Client Pending Timesheet Count": x.clientPendingCount,
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
    }



    refreshTimesheet(template: TemplateRef<any>) {
      this.page = 1;

      console.log("Selected Employee " ,this.selectedEmpId);
      console.log("Selected Fromdate " ,this.fromDate);

      console.log("Selected ToDate " ,this.toDate);


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

        const totalPages = Math.ceil(this.employeeTimesheet.length / 10);
        this.paginationArray = Array.from({ length: totalPages }, (_, i) => i + 1);

        console.log('Timesheet:', data);
        this.modalRef = this.modalService.show(template, { class: 'modal-xl' });

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
      O:   { label: 'Other Project',        color: '#1c1f23' },
      A:   { label: 'Absent',               color: '#8b0000' },
      NW:  { label: 'Non-Working Day',      color: '#343a40' },
      AH:  { label: 'Public Holiday',       color: '#0b3c5d' },
      WO:  { label: 'Week Off',             color: '#4b371c' },
      H:   { label: 'Holiday',              color: '#5a4b00' },
      CH:  { label: 'Client Holiday',       color: '#3e2f1c' },
      DA:  { label: 'Document Approved',    color: '#003366' },
      DP:  { label: 'Document Pending',     color: '#664400' },
      P:   { label: 'Present',              color: '#014421' },
      NA:  { label: 'Not Applicable',       color: '#2f4f4f' }
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
console.log("Hiii");
    const month = this.selectedMonth.getMonth() + 1;
    const year = this.selectedMonth.getFullYear();

    this.timesheetService.getEmployeeTimesheetAsCalender(projectId, month, year)
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

    projectViewClient:any={
      status:'',
      month1:null,
      year:null,
      empId:null
    }
    
    getProjectViewForClientAttendanceStatus(status:any,month:any,year:any) {
      this.projectViewClient.status =status;
      this.projectViewClient.month1 =month;
      this.projectViewClient.year =year;
      this.projectViewClient.empId = this.currentUser.empId;
      this.projectViewClient.page =this.page1
      this.projectViewClient.size=this.pageSize
      this.projectViewClient.isClientDashboard=this.isClientDashboard
      this.projectViewClient.dataForExcel=false; 
      console.log("test empId ",this.projectViewClient);
      this.timesheetService.getProjectViewForClientAttendanceStatus(this.projectViewClient).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.projectView = response.serviceResponse;
          this.totalItems = response.totalElements;
          this.dataForExcel=false;
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

  async exportProjectOverviewToExcel(): Promise<void> {
    const excelName = "Project Overview.xlsx";
    this.dataForExcel=true;
    await this.getProjectViewForClientAttendanceStatusForExcel(this.status,this.month,this.year);
    const exportData = this.projectViewForExcel.map((project: any) => ({
      'Project Name': project.projectName || 'NA',
      'PO Number': project.poNo || 'NA',
      'Project Type': project.projectType || 'NA',
      'Project Manager': project.projectManagerName || 'NA',
      'Client': project.clientName || 'NA',
      'Apmosys RM': project.apmosysRM || 'NA',
      'Apmosys RM Email': project.apmosysRMEmail || 'NA',
      'Client RM': project.clientRM || 'NA',
      'Expected Fill Count': project.totalExpectedFillCount ?? 0,
      // 'iShine Filled Count': project.totalIshineFilledCount ?? 0,
      'Client Pending %': (project.clientSidePendingPercent ?? 0) + '%',
      'Client Side Pending': project.totalClientSidePendingCount ?? 0,
      'Client Approved %': (project.clientSideApprovedPercent ?? 0) + '%',
      'Client Side Approved': project.totalClientSideApprovedCount ?? 0,
      'Client Not Filled %': (project.clientSideNotFilledPercent ?? 0) + '%',
      'Client Side Not Filled': project.totalClientSideNotFilledCount ?? 0
    }));

    this.exportExcelService.exportTableDataToExcel(exportData, excelName);
  }


  hoveredEmpId: string | null = null;
hideTimeout: any;

showPopup(empId: string) {
  clearTimeout(this.hideTimeout);
  this.hoveredEmpId = empId;
}

scheduleHidePopup() {
  this.hideTimeout = setTimeout(() => {
    this.hoveredEmpId = null;
  }, 200); // Delay to allow mouseenter on popup
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

  getTimesheetDashboardCount(month:any,year:any) {
    if(this.toggleValue){
      this.timesheetService.getTimesheetDashboardCountForProject(month,year,this.currentUser.empId,this.isClientDashboard).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.dashboardObj = response.serviceResponse;
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
      });
    } else {
      this.timesheetService.getTimesheetDashboardCountForEmployee(month,year,this.currentUser.empId,this.isClientDashboard).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.dashboardObj = response.serviceResponse;
          console.log("dashboardObj :::::::::",this.dashboardObj);
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
      });
    }
  }

  scrollToTable(status: string | null): void {
    this.status = status;
    this.getTableData(status,this.month,this.year);
    const element = document.getElementById('table-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  getTableData(status:string | null,month:any,year:any){
    if(!this.toggleValue){
      this.getEmployeeViewForClientAttendanceStatus(status,month,year);
    } else {
      this.getProjectViewForClientAttendanceStatus(status,month,year);
    }
  }

  monthSelected1(event: Date, datepicker: any) {
    // Set selected month as the first day of the selected month
    this.selectedMonth1 = new Date(event.getFullYear(), event.getMonth(), 1);
  
  
    this.month = this.selectedMonth1.getMonth() + 1; // Month is 0-indexed
    this.year = this.selectedMonth1.getFullYear();
  
    
    console.log("Selected Month:", this.month);
    console.log("Selected Year:", this.year);
    console.log("selectedMonth1 ::::::::", this.selectedMonth1);
  
    
    this.updateFormattedMonthLabel();
  
   
    this.getTimesheetDashboardCount(this.month, this.year);
    if (!this.toggleValue) {
      this.status = 'All';
      this.getEmployeeViewForClientAttendanceStatus(this.status, this.month, this.year);
      this.getTimesheetDashboardCount(this.month, this.year);
    } else {
      this.status = 'All';
      this.getTimesheetDashboardCount(this.month, this.year);
      this.getProjectViewForClientAttendanceStatus(this.status, this.month, this.year);
    }
     
    // this.calendarDir.navigateToCalendar(this.currMonth,this.currYear);
    
    // Close picker
    datepicker.close();
  }
  
  
  changeMonth1(date: Date) {
    
    if (!date) return;
    this.selectedMonth1 = new Date(date.getFullYear(), date.getMonth(), 1);
    this.updateFormattedMonthLabel();
  
    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
  }

  // monthSelected1(date: Date, datepicker: MatDatepicker<Date>) {
  //   this.month = date.getMonth() + 1; // JS months are 0-indexed
  //   this.year = date.getFullYear();
  
  //   // this.getTimesheetDashboardCount(this.month,this.year);
  //   this.formattedMonthLabel = this.getFormattedMonthLabel(this.month, this.year);
  
  //   datepicker.close();
  
  //   // Call any logic needed after selecting a month
  //   this.refreshDashboard(); // optional
  // }
  
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
  
  onDashboardToggleChange(){
      this.page1 = 1;
      this.totalItems = 0;
      this.pageSize = 10;

    this.isClientDashboard =!this.isClientDashboard  
    this.resetSearchField();
    if(!this.toggleValue){
    this.getEmployeeViewForClientAttendanceStatus(this.status,this.month,this.year);
    this.getTimesheetDashboardCount(this.month, this.year);
    this.ishineNotFilled();
   }else{
    this.getProjectViewForClientAttendanceStatus(this.status,this.month,this.year);
    this.getTimesheetDashboardCount(this.month, this.year);
    this.getProjectByNameAndPoNo();
    this.ishineNotFilled();
   }
  //  this.getEmployeeTimesheetsByProject(this.timesheetSummaryTemplate);

  }
  resetSearchField(){
    this.insightPage = 1
    this.insightPageSize = 10
    this.insightPageTotalItems = 10
    this.fromDate = null;
    this.toDate = null;
    this.selectedProjectId = 0;
    this.selectedEmpId = 0;
    this.timesheetObj.empId = '';
    this.timesheetObj.projectId = '' ;
    this.employeeCtrl.reset();
    this.projectPoCtrl.reset();
    this.fromDateRef.control.markAsPristine();
    this.fromDateRef.control.markAsUntouched();
    this.toDateRef.control.markAsPristine();
    this.toDateRef.control.markAsUntouched();

  }

  handleInsightPageChange(event, pageType) { 
  if(this.insightPageSize != event.pageSize){
    this.insightPage = 1;
    this.insightPageSize = event.pageSize;
  }else{
    this.insightPage = event.pageIndex+1;
    this.insightPageSize = event.pageSize;
  } 
  if(pageType == 'projectTimeSheetSearch') {
      this.getEmployeeTimesheetsByProject();
  }else if(pageType == 'empTimeSheetSearch') {
      this.searchTimesheet(null,null, false);
  }
}
}