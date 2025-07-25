import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import * as Highcharts from 'highcharts';
import { Employee } from 'src/app/models/employee';
import { first, map, startWith } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { Timesheet } from 'src/app/models/timesheet';


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
  selectedEmpId: number;
  filteredEmployees: Employee[] = [];
  employeeCtrl = new FormControl();
  employeeList: Employee[] = [];
  teamLeadsList: any[] = [];
  filteredTeamLeads: Employee[] = [];
  fromDate: Date | null = null;
  toDate: Date | null = null;
  employeeTimesheet: Object;
  timesheetObj: Timesheet = new Timesheet();


  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
  ) {}

  async ngOnInit(): Promise<void> {

    this.getEmployeeByNameAndEmpld();

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

      // this.timesheetService.getEmployeeMonthlyTimesheet(employee.empId).subscribe(
      //   timesheet => {
      //     console.log('Timesheet:', timesheet);
      //   },
      //   error => {
      //     console.error('Error fetching timesheet', error);
      //   }
      // );

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

searchTimesheet() {
  if (!this.selectedEmpId || !this.fromDate || !this.toDate) {
    alert('Please select an employee and valid dates.');
    return;
  }

  this.timesheetObj.empId = this.selectedEmpId;
  this.timesheetObj.fromDate = this.formatDate(this.fromDate);
  this.timesheetObj.toDate = this.formatDate(this.toDate);



  this.timesheetService.getEmployeeMonthlyTimesheet(this.timesheetObj)
    .subscribe(
      (data) => {
        this.employeeTimesheet = data;
        console.log('Timesheet:', data);
      },
      (error) => {
        console.error('Error fetching timesheet', error);
      }
    );
}

formatDate(date: Date): string {
  return date.toISOString().split('T')[0];
}



}
