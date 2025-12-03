import { AfterViewInit, OnInit,Component, ElementRef, TemplateRef, ViewChild, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
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
  // @ViewChild('clientSideFilter',{ static: false }) clientSideFilter!: ElementRef;
  @ViewChild('vmsChartContainer', { static: false }) vmsChartContainer!: ElementRef;
  @ViewChild('ishineChartContainer', { static: false }) ishineChartContainer!: ElementRef;
  @ViewChild('departmentChartContainer', { static: false }) departmentChartContainer!: ElementRef;
  @ViewChild('docRejectChart', { static: false }) docRejectChart!: ElementRef;
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  modalRef3: BsModalRef;
  @ViewChild("previewModal")
  previewModal: TemplateRef<any>;
  empId: number;
  previewUrl: SafeResourceUrl | null = null;    
  fileType: string = '';                       
  mimeType: string = '';                       
  previewFileName: string = '';                 
  docData: string = '';                       
  selectedProjectId!: any;
  selectedEmpId!: any;
  clientSideFilter: any;
  selectedMonth: Date = new Date();
  userName: string = '';
  userEmpId: any;
  weekDays: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  timesheetCalender: any[] = [];

  monthGrid: any[][] = [];

  legend: { [key: string]: { label: string; color: string } } = {
    O:  { label: 'Other Project',       color: '#6C757D' },   // Neutral gray
    A:  { label: 'Absent',              color: '#D9534F' },   // Red (alert)
    NW: { label: 'Non-Working Day',     color: '#8E8E8E' },   // Muted gray
    AH: { label: 'ApMoSys Holiday',     color: '#0275D8' },   // Corporate blue
    WO: { label: 'Week Off',            color: '#795548' },   // Brownish neutral
    H:  { label: 'Holiday',             color: '#FFC107' },   // Golden yellow
    CH: { label: 'Client Holiday',      color: '#FF9800' },   // Orange
    CA: { label: 'Client Approved',   color: '#006400' },   // Dark green
    CN: { label: 'Client Not-Approved',    color: '#F0AD4E' },   // Amber
    P:  { label: 'Present',             color: '#28A745' },   // Bright green
    NA: { label: 'Not Applicable',      color: '#9E9E9E' },   // Light gray
    L:  { label: 'Leave',               color: '#C21807' },   // Deep red
    AP: { label: 'Timesheet Approved',  color: '#007E33' },   // Strong green
    PE: { label: 'Timesheet Pending',   color: '#FFB300' },   // Bright amber
  };
  legendEntries: { code: string; label: string; color: string }[] = [];
  formattedMonthLabel: any;
    currentDate = new Date();
  minYear!: Date;
  maxYear!: Date;

  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: BsModalService,
    private projectService:ProjectService,
    private resourceManagementService: ResourceManagementService,
    private exportExcelService: ExportExcelService,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute,) { }

  ngOnInit(): void {
  const currentYear = this.currentDate.getFullYear();
  this.minYear = new Date(currentYear - 1, 0, 1); 
  this.maxYear = new Date(currentYear, 11, 31); 
    this.route.queryParams.subscribe(params => {
      const projectId = +params['projectId'];
      const empId = +params['empId'];
      const clientSideFilter = params['clientSideFilter'] === 'true';
      this.clientSideFilter = clientSideFilter
      this.empId=empId;
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

  openAlertModForFutureDate(template1: TemplateRef<any>, message: any) {
    this.modalRef2 = this.modalService.show(template1, { class: 'modal-sm' });
    this.alertMessage = message;
  }

monthSelected(event: Date, datepicker: any) {
  const now = new Date();

  if (
    event.getFullYear() > now.getFullYear() ||
    (event.getFullYear() === now.getFullYear() && event.getMonth() > now.getMonth())
  ) {
    this.openAlertModForFutureDate(this.alertTemplate, "Future months are not allowed!");
    datepicker.close();
    return;
  }

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

  
    this.timesheetService.getEmployeeTimesheetAsCalender(empId, month, year,this.clientSideFilter)
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
              this.userEmpId = employeeData.empId;
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

      const dateObj: any = {
        day,
        date,
        isToday: this.isToday(date),
        isWeekend: date.getDay() === 0 || date.getDay() === 6,
        isHoliday: data?.status === 'H',
        attendance: data?.status || null,
        intime: data?.inTime || null,
        outtime: data?.outTime || null,


        showEye: !!data && (data.inTime || data.outTime || data.status)

      };

      week.push(dateObj);

      if (week.length === 7) {
        grid.push(week);
        week = [];
      }
    }

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

  onDateClick(dateObj: any): void {
    if (!dateObj || !dateObj.date) {
      this.openAlertMod(this.alertTemplate, "Invalid date selection.");
      return;
    }

    const payload = {
      empId: this.empId,
      date: this.formatDate(dateObj.date)
    };

    console.log("Fetching document for:", payload);

    this.timesheetService.getDocumentsByEmpAndDate(payload).subscribe({
      next: (res: any) => {
        if (res.serviceStatus === 'Success' && res.serviceResponse) {
          const docs = res.serviceResponse;

          let doc =
            docs.find((d: any) => d.clientApprovalStatus?.toLowerCase() === 'approved') ||
            docs.find((d: any) => d.clientApprovalStatus?.toLowerCase() === 'pending');

          if (doc.docData && doc.docMimeType) {
            this.showPreview(doc.docData, doc.docMimeType, doc.fileName);
          } else {
            this.openAlertMod(this.alertTemplate, "No valid document data found.");
          }
        } else {
          this.openAlertMod(this.alertTemplate, res.serviceMessage || 'No document found.');
        }
      },
      error: (err) => {
        console.error('Error fetching document:', err);
        this.openAlertMod(this.alertTemplate, 'Error while fetching document.');
      }
    });
  }

  private formatDate(date: Date): string {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = ('0' + (d.getMonth() + 1)).slice(-2);
    const day = ('0' + d.getDate()).slice(-2);
    return `${year}-${month}-${day}`;
  }

  showPreview(base64Data: string, mimeType: string, fileName?: string): void {
    const dataUrl = `data:${mimeType};base64,${base64Data}`;
    this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.fileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.fileType = 'image';
    } else {
      this.fileType = 'other';
    }

    this.previewFileName = fileName || 'Document';
    this.modalRef2 = this.modalService.show(this.previewModal, { class: 'modal-xl modal-dialog-centered' });
  }



}
