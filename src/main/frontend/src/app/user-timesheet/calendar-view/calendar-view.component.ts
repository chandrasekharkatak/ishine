import { AfterViewInit, OnInit,Component, ElementRef, TemplateRef, ViewChild, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import * as Highcharts from 'highcharts';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, map, startWith } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { GetEmployeeViewForClientAttendanceStatus } from 'src/app/models/getEmployeeViewForClientAttendanceStatus';
import { ProjectViewForTimesheet } from 'src/app/models/projectViewForTimesheet';
import { Timesheet } from 'src/app/models/timesheet';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
@Component({
  selector: 'app-calendar-view',
  templateUrl: './calendar-view.component.html',
  styleUrls: ['./calendar-view.component.css']
})
export class CalendarViewComponent implements OnInit {

  @ViewChild('vmsChartContainer', { static: false }) vmsChartContainer!: ElementRef;
  @ViewChild('ishineChartContainer', { static: false }) ishineChartContainer!: ElementRef;
  @ViewChild('departmentChartContainer', { static: false }) departmentChartContainer!: ElementRef;
  @ViewChild('docRejectChart', { static: false }) docRejectChart!: ElementRef;
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  modalRef3: BsModalRef;
  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;


  selectedProjectId!: any;
  selectedEmpId!: any;

  selectedMonth: Date = new Date();
  userName: string = '';
  weekDays: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  timesheetCalender: any[] = [];

  monthGrid: any[][] = [];

  legend: { [key: string]: { label: string; color: string } } = {
    O:   { label: 'Other Project',        color: '#1c1f23' },
    A:   { label: 'Absent',               color: '#8b0000' },
    NW:  { label: 'Non-Working Day',      color: '#343a40' },
    AH:  { label: 'Apmosys Holiday',       color: '#0b3c5d' },
    W:  { label: 'Week Off',             color: '#4b371c' },
    H:   { label: 'Holiday',              color: '#5a4b00' },
    CH:  { label: 'Client Holiday',       color: '#3e2f1c' },
    DA:  { label: 'Document Approved',    color: '#003366' },
    DP:  { label: 'Document Pending',     color: '#664400' },
    P:   { label: 'Present',              color: '#014421' },
    NA:  { label: 'Not Applicable',       color: '#2f4f4f' }
  };
  legendEntries: { code: string; label: string; color: string }[] = [];
  formattedMonthLabel: any;


  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: BsModalService,
    private projectService:ProjectService,
    private resourceManagementService: ResourceManagementService,
    private exportExcelService: ExportExcelService,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute,) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const projectId = +params['projectId'];
      const empId = +params['empId'];
      const formattedMonthLabel = params['formattedMonthLabel'];
      console.log("formattedMonthLabel",formattedMonthLabel)

      if (formattedMonthLabel) {
        const date = new Date(formattedMonthLabel);
        if (!isNaN(date.getTime())) {
          this.selectedMonth = new Date(date.getFullYear(), date.getMonth(), 1);
        } else {
          this.selectedMonth = new Date();
        }
      } else {
        this.selectedMonth = new Date();
      }

      this.updateFormattedMonthLabel();

      if (projectId && empId) {
        this.fetchTimesheetData(projectId, empId);
      } else {
        console.warn("projectId or empId missing in query params.");
      }
      });

    this.legendEntries = Object.entries(this.legend).map(([code, value]) => ({
      code,
      label: value.label,
      color: value.color
    }));
  }

  alertMessage: any;
modalRef: BsModalRef = new BsModalRef();
modalRef2: BsModalRef = new BsModalRef();

openAlertMod(template: TemplateRef<any>, message: any) {
  this.modalRef3= this.modalService.show(template, { class: 'modal-sm' });
  this.alertMessage = message;
}

openAlertMod1(template1: TemplateRef<any>, message: any) {
  this.modalRef2 = this.modalService.show(template1, { class: 'modal-sm' });
  this.alertMessage = message;
}

  monthSelected(event: Date, datepicker: any) {
    this.selectedMonth = new Date(event.getFullYear(), event.getMonth(), 1);
    this.updateFormattedMonthLabel();
  
    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
  
    datepicker.close();
  }
  
  changeMonth(date: Date) {
    if (!date) return;
    this.selectedMonth = new Date(date.getFullYear(), date.getMonth(), 1);
    this.updateFormattedMonthLabel();
  
    if (this.selectedProjectId && this.selectedEmpId) {
      this.fetchTimesheetData(this.selectedProjectId, this.selectedEmpId);
    }
  }
  
  updateFormattedMonthLabel() {
    this.formattedMonthLabel = this.selectedMonth.toLocaleString('default', {
      month: 'short',
      year: 'numeric'
    }); // E.g. "Aug 2025"
  }


  // fetchTimesheetData(projectId: any, empId: any, formattedMonthLabel?: any): void {
  //   this.selectedProjectId = projectId;
  // this.selectedEmpId = empId;

  // if (this.formattedMonthLabel) {
  //   const date = new Date(this.formattedMonthLabel); // ISO string → Date
  //   if (!isNaN(date.getTime())) {
  //     this.selectedMonth = new Date(date.getFullYear(), date.getMonth(), 1);
  //   } else {
  //     this.selectedMonth = new Date(); // fallback
  //   }
  // } else {
  //   this.selectedMonth = new Date(); // fallback
  // }

  // const month = this.selectedMonth.getMonth() + 1;
  // const year = this.selectedMonth.getFullYear();

  // console.log('Using month/year for API:', month, year);
  
  //   this.timesheetService.getEmployeeTimesheetAsCalender(empId, month, year)
  //     .pipe(first())
  //     .subscribe({
  //       next: (response: any) => {
  //         if (response.serviceStatus === 'Success' && response.serviceResponse?.length) {
  //           this.timesheetCalender = response.serviceResponse;
  //           // const employeeData =response.serviceResponse;
  //           const employeeData = response.serviceResponse.find((emp: any) => emp.empId === empId);
  //  console.log("Filtered Employee Data",employeeData);
  //           if (employeeData) {
  //             this.userName = employeeData.employeeName;
  //             this.buildCalendarGrid(employeeData.timesheetData);
  //           } else {
  //             this.openAlertMod(this.alertTemplate, `Employee ID ${empId} not found in the data.`);
  //           }
  //         } else {
  //           this.openAlertMod(this.alertTemplate, response.serviceResponse || 'No data found.');
  //         }
  //       },
  //       error: (err) => {
  //         console.error('Error fetching timesheet data:', err);
  //         this.openAlertMod(this.alertTemplate, 'Something went wrong. Please try again later.');
  //       }
  //     });
  // }

  fetchTimesheetData(projectId: number, empId: number): void {
    this.selectedProjectId = projectId;
    this.selectedEmpId = empId;
      
    const month = this.selectedMonth.getMonth() + 1;
    const year = this.selectedMonth.getFullYear();

  
    this.timesheetService.getEmployeeTimesheetAsCalender(empId, month, year)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success' && response.serviceResponse?.length) {
            this.timesheetCalender = response.serviceResponse;
            // const employeeData =response.serviceResponse;
            const employeeData = response.serviceResponse.find((emp: any) => emp.empId === empId);
   console.log("Filtered Employee Data",employeeData);
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
  
   cancelRequest1() {
   if (this.modalRef3) {
      this.modalRef3.hide();
    }
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
      // console.log("data",data);
      // console.log("date",date);
      // console.log("year",year);
      // console.log("day",day);
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

}
