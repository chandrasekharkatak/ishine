import { Component, AfterViewInit, ElementRef, ViewChild, TemplateRef } from '@angular/core';
import { FormControl } from '@angular/forms';
import * as Highcharts from 'highcharts';
import { Employee } from 'src/app/models/employee';
import { first, map, startWith } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { Timesheet } from 'src/app/models/timesheet';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ResourceManagementService } from 'src/app/services/resource-management.service';



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
  selectedEmpId: any;
  filteredEmployees: Employee[] = [];
  employeeCtrl = new FormControl();
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

  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: BsModalService,
    private resourceManagementService: ResourceManagementService,
  ) {}

  async ngOnInit(): Promise<void> {

    this.setLastUpdatedTime();
    this.getEmployeeByNameAndEmpld();
    this.TotalEmployeeCount();
    this.vmsCompletion();

    this.employeeCtrl.valueChanges
    .pipe(
      startWith(''),
      map(value => typeof value === 'string' ? value : value?.name || ''),
      map(name => this.filterEmployees(name))
    )
    .subscribe(filtered => {
      this.filteredEmployees = filtered;
    });
  }

  ngAfterViewInit(): void {
    this.renderVmsChart();
    this.renderIshineChart();
    this.renderDepartmentChart();
    this.renderDocRejectChart();
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
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => {
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

  filterEmployees(searchText: string) {
    const lowerText = (searchText || '').toLowerCase();
    return this.employeeList.filter(emp =>
      emp.name.toLowerCase().includes(lowerText) ||
      emp.employmentId.toLowerCase().includes(lowerText)
    );
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


onDateRangeChange(): void {
  // You might add logic here to validate the date range, etc.
}

alertMessage: any;
modalRef: BsModalRef = new BsModalRef();

openAlertMod(template: TemplateRef<any>, message: any) {
  this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  this.alertMessage = message;
}

searchTimesheet(template: TemplateRef<any> ) {

this.page = 1;
const totalPages = Math.ceil(this.employeeTimesheet.length / 10);
this.paginationArray = Array.from({ length: totalPages }, (_, i) => i + 1);

  if (!this.selectedEmpId || !this.fromDate || !this.toDate) {
    alert('Please select an employee and valid dates.');
    return;
  }

  this.timesheetObj.empId = this.selectedEmpId;
  this.timesheetObj.fromDate = this.formatDate(this.fromDate);
  this.timesheetObj.toDate = this.formatDate(this.toDate);


  console.log("empId ::::::::",this.timesheetObj.empId);
  console.log("fromDate ::::::::",this.timesheetObj.fromDate);
  console.log("toDate ::::::::",this.timesheetObj.toDate);


  this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj)
    .subscribe(
      (data: any[]) => {  
        this.employeeTimesheet = data;
        console.log('Timesheet:', data);
        this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
        //this.filteredEmployees = [];
        this.fromDate = null;
        this.toDate = null;
        this.timesheetObj.empId = '' ;
      },
      (error) => {
        console.error('Error fetching timesheet', error);
      }
      
    );
}

   //pagination
      handlePageChange(event) {
       this.page = event;
   }

formatDate(date: Date): string {
  return date.toISOString().split('T')[0];
}
cancelRequest() {
  this.modalRef.hide();
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
 
 // this.loadDashboardData(); 
  this.setLastUpdatedTime();
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
      this.openAlertMod(this.alertTemplate, response.serviceResponse);
    }
  });
}

vmsCompletion() {
  this.timesheetObj.clientApprovalStatus = "pending"
  this.timesheetService.totalVmsFilledCount(this.timesheetObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.totalEmployees = response.serviceResponse;
    } else {
      this.openAlertMod(this.alertTemplate, response.serviceResponse);
    }
  });
}


ishineCompletion() {
  this.timesheetService.totalIshineFilledCount(this.timesheetObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      console.log(response.serviceResponse);
      this.totalEmployees = response.serviceResponse;
    } else {
      this.openAlertMod(this.alertTemplate, response.serviceResponse);
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


}
