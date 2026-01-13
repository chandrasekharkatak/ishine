import { AfterViewInit, OnInit,Component, ElementRef, TemplateRef, ViewChild, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import * as Highcharts from 'highcharts';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first, map, startWith } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { getEmployeeTimesheetAsCalenderByProjectId } from 'src/app/models/getEmployeeTimesheetAsCalenderByProjectId';
import { GetEmployeeViewForClientAttendanceStatus } from 'src/app/models/getEmployeeViewForClientAttendanceStatus';
import { ProjectViewForTimesheet } from 'src/app/models/projectViewForTimesheet';
import { Timesheet } from 'src/app/models/timesheet';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
@Component({
  standalone: false,
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
  modalRef3: NgbModalRef;
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
  projectList: any[] = [];
  payload: getEmployeeTimesheetAsCalenderByProjectId = new getEmployeeTimesheetAsCalenderByProjectId();
  monthGrid: any[][] = [];
  projectIdForDropDown: any;

  legend: { [key: string]: { label: string; color: string } } = {
    O:  { label: 'Other Project',       color: '#6C757D' },   // Neutral gray
    A:  { label: 'Absent',              color: '#D9534F' },   // Red (alert)
    NW: { label: 'Non-Working Day',     color: '#8E8E8E' },   // Muted gray
    AH: { label: 'ApMoSys Holiday',     color: '#0275D8' },   // Corporate blue
    WO: { label: 'Week Off',            color: '#795548' },   // Brownish neutral
    CO: { label: 'Comp Off',            color: '#295748' },   // Brownish neutral
    H:  { label: 'Holiday',             color: '#FFC107' },   // Golden yellow
    CH: { label: 'Client Holiday',      color: '#FF9800' },   // Orange
    CA: { label: 'Client Approved',   color: '#006400' },   // Dark green
    CN: { label: 'Client Not-Approved',    color: '#F0AD4E' },   // Amber
    // 🔴 Rejected by RM (improved differentiation)
    CA_R: {
      label: 'Client Approved But Rejected By RM',
      color: '#B71C1C' // Dark red (high-impact rejection)
    },
    CN_R: {
      label: 'Client Not-Approved But Rejected By RM',
      color: '#E57373' // Soft red (lower severity rejection)
    },
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
  @ViewChild("alert_message_projectDropDown")
  projectDropDownAlert: TemplateRef<any>;
  projectDropDownAlertRef: NgbModalRef;

  zoomScale = 1;
  zoomLevel = 100;
  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
  previewBase64!: string;
  previewMimeType!: string;
  dateObj:any;

  constructor(private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private modalService: NgbModal,
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
      this.projectIdForDropDown = projectId;

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
      this.getProjectByMonthRangeAndEmpId();

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
modalRef:NgbModalRef;
modalRef2:NgbModalRef;

openAlertMod(template: TemplateRef<any>, message: any) {
  this.modalRef3= this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  this.alertMessage = message;
}

openAlertMod1(template1: TemplateRef<any>, message: any) {
  this.modalRef2 = this.modalService.open(template1, { modalDialogClass: 'modal-sm' });
  this.alertMessage = message;
}

  openAlertModForFutureDate(template1: TemplateRef<any>, message: any) {
    this.modalRef2 = this.modalService.open(template1, { modalDialogClass: 'modal-sm' });
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
  this.getProjectByMonthRangeAndEmpId(true);

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
    this.payload.empId = empId;
    this.payload.month = this.selectedMonth.getMonth() + 1;
    this.payload.year = this.selectedMonth.getFullYear();
    this.payload.projectId = projectId;
    this.payload.allEmp = !this.clientSideFilter;

    this.timesheetService.getEmployeeTimesheetAsCalender(this.payload)
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
      this.modalRef3.close();
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

    this.dateObj = dateObj;

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
    this.resetPreviewState();
    this.previewBase64 = base64Data;
    this.previewMimeType = mimeType;
    this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.fileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.fileType = 'image';
    } else if (
      mimeType === 'application/vnd.ms-excel' ||
      mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ) {
      this.fileType = 'excel';
    }else {
      this.fileType = 'other';
    }

    // this.previewFileName = fileName || 'Document Preview';
    // this.modalRef2 = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-xxl modal-dialog-centered',scrollable: true });

    this.modalRef = this.modalService.open(this.previewModal, {
    modalDialogClass: 'modal-xl modal-dialog-centered',
    scrollable: false
    });

  }

  getProjectByMonthRangeAndEmpId(fromMonthChange: boolean = false){
      const selectedDate = new Date(this.selectedMonth);
      this.payload.empId = this.empId;
      this.payload.month = selectedDate.getMonth() + 1;
      this.payload.year = selectedDate.getFullYear();

    console.log("getProjectByMonthRangeAndEmpId for:", this.payload);

    this.timesheetService.getProjectByMonthRangeAndEmpId(this.payload).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse?.length) {
          this.projectList = response.serviceResponse;
          if (this.projectList.length > 0) {
            const exists = this.projectList.some(
              project => project.projectId === this.projectIdForDropDown
            );
            if (!exists) {
              this.projectIdForDropDown = this.projectList[0].projectId;
            }
            if (fromMonthChange) {
              this.fetchTimesheetData(this.projectIdForDropDown, this.empId);
            }
          }
        } else {
          this.openProjectDropDownAlert(response.serviceResponse);
        }
      },
      error: (err) => {
        console.error('Error fetching projectList:', err);
        this.openProjectDropDownAlert('Error while fetching projectList.');
      }
    });
  }

  onProjectSelectionChange(projectId:any){
    if (!projectId || !this.empId) return;
    this.fetchTimesheetData(projectId,this.empId);
  }

  openProjectDropDownAlert( message: any) {
    this.projectDropDownAlertRef = this.modalService.open(this.projectDropDownAlert, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  hideProjectDropDownAlert() {
    this.projectDropDownAlertRef?.close();
  }


zoomIn() {
  if (this.zoomScale < 2.5) {
    this.zoomScale += 0.1;
    this.zoomLevel = Math.round(this.zoomScale * 100);
  }
}

zoomOut() {
  if (this.zoomScale > 0.5) {
    this.zoomScale -= 0.1;
    this.zoomLevel = Math.round(this.zoomScale * 100);
  }
}


get transformStyle() {
  return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.zoomScale})`;
}

startDrag(event: MouseEvent) {
  if (this.zoomScale <= 1) return; // drag only when zoomed

  this.isDragging = true;
  this.startX = event.clientX - this.translateX;
  this.startY = event.clientY - this.translateY;
  event.preventDefault();
}

onDrag(event: MouseEvent) {
  if (!this.isDragging) return;

  this.translateX = event.clientX - this.startX;
  this.translateY = event.clientY - this.startY;
}

endDrag() {
  this.isDragging = false;
}

resetPreviewState() {
  this.zoomScale = 1;
  this.zoomLevel = 100;
  this.translateX = 0;
  this.translateY = 0;
  this.isDragging = false;
}

  downloadFile(): void {
    if (!this.previewBase64 || !this.previewMimeType) {
      return;
    }

    const byteCharacters = atob(this.previewBase64);
    const byteNumbers = new Array(byteCharacters.length);

    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }

    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: this.previewMimeType });

    const blobUrl = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = this.buildFileName();
    link.click();

    URL.revokeObjectURL(blobUrl);
  }

  private buildFileName(): string {
    const userName = this.userName || 'User';
    const day = this.dateObj?.day || 'Date';
    const month = this.formattedMonthLabel;
    const project = this.projectList.find(p => p.projectId === this.projectIdForDropDown);
    const projectName = project?.projectName || 'Project';
    // const extension = this.fileType;
    const extension = this.getExtensionFromMime(this.previewMimeType);

    return `${userName} | ${day} ${month} | ${projectName}.${extension}`;
  }

  private getExtensionFromMime(mimeType: string): string {
    switch (mimeType) {
      case 'application/pdf':
        return 'pdf';
      case 'image/jpeg':
        return 'jpeg';
      case 'image/jpg':
        return 'jpeg';
      case 'image/png':
        return 'jpeg';
      case 'image/webp':
        return 'jpeg';
      case 'application/vnd.ms-excel':
      return 'xls';
      case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
      return 'xlsx';
      default:
        return 'file';
    }
  }

  getDocsForPreview(docId: any) {
  this.timesheetService.getDocumentDataByDocId(docId)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.docData = response.serviceResponse.docData;
        this.mimeType = response.serviceResponse.docMimeType;

        // Excel → Download
        if (
          this.mimeType === 'application/vnd.ms-excel' ||
          this.mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        ) {
          const fileName = response.serviceResponse.docName || 'document.xlsx';
          this.downloadExcel(this.docData, this.mimeType, fileName);
        }
        // PDF / Image → Preview
        else {
          this.showPreview(this.docData, this.mimeType);
        }
      }
    });
}


  downloadExcel(base64Data: string, mimeType: string, fileName: string) {

  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const blob = new Blob(
    [new Uint8Array(byteNumbers)],
    { type: mimeType }
  );

  const url = window.URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  a.click();

  window.URL.revokeObjectURL(url);
}

}
