import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { GetEmployeeTimesheetAsCalender } from '../models/getEmployeeTimesheetAsCalender';
import { TimesheetService } from '../services/timesheet.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ExportExcelService } from '../services/export-excel.service';
import { getEmployeeTimesheetAsCalenderByProjectId } from '../models/getEmployeeTimesheetAsCalenderByProjectId';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
// import * as XLSX from 'xlsx';
import * as XLSX from 'xlsx-js-style';
import { ColorAxis } from 'highcharts';


@Component({
  standalone: false,
  selector: 'app-team-employee-timesheet-view',
  templateUrl: './team-employee-timesheet-view.component.html',
  styleUrls: ['./team-employee-timesheet-view.component.css']
})

export class TeamEmployeeTimesheetViewComponent implements OnInit {

  projectId: any;
  timesheetData: GetEmployeeTimesheetAsCalender[] = [];
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  filteredTimesheetData: any[] = [];
  filters: any = {};
  // timesheetDataColumns: any[] = ['employmentId', 'clientSideId', 'employeeName', 'department', 'billableType', 'clientName', 'poNo', 'projectName', 'projectManagerName', 'teamName', 'startDate', 'expectedTimesheetFillCount','apmosysTimesheetFilledCount','clientSideNotFilledCount','clientSidePendingCount','clientSideApprovedCount',...Array.from({length: 31}, (_, i) => `d${i + 1}`),'present','weekOff','holiday','leave','compOff','na','halfDay','totalNoOfDays',];
    timesheetDataColumns: any[] = ['employmentId', 'clientSideId', 'employeeName', 'employmentStatus', 'projectStatus', 'department', 'billableType', 'clientName', 'poNo', 'projectName', 'projectManagerName', 'teamName', 'startDate', 'endDate', 'expectedTimesheetFillCount','apmosysTimesheetFilledCount','clientSideNotFilledCount','clientSidePendingCount','clientSideApprovedCount',];
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  alertMessage: any;
  modalRef:NgbModalRef;
  modalRef2?: NgbModalRef;
  isSearchEnabled: boolean = false;
  excelName: any;
  tableName: any;
  selectedMonth = new Date();
  month:any;
  year:any;
  monthName:any;
  currentDate = new Date();
minYear!: Date;
maxYear!: Date;
  legend: { [key: string]: { label: string; color: string } } = {
    O:  { label: 'Other Project',       color: '#0da79fff' },   
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
  hideTimeout: any;
  hoveredEmpId: string | null = null;
  timesheetAsCalenderByProjectId : getEmployeeTimesheetAsCalenderByProjectId = new getEmployeeTimesheetAsCalenderByProjectId();
  currentUser:User;
  formattedMonthLabel: string = '';
  isClientDashboard: boolean;
  daysInMonth: { dayNumber: number; dayName: string }[] = [];
  
  constructor(private route: ActivatedRoute,
    private modalService: NgbModal,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private authenticationService: AuthenticationService
  ){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
     const currentYear = this.currentDate.getFullYear();
  this.minYear = new Date(currentYear - 1, 0, 1); 
  this.maxYear = new Date(currentYear, 11, 31); 
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      this.formattedMonthLabel = params['formattedMonthLabel'];
      this.isClientDashboard = params['isClientDashboard'] === 'true';
      if (this.projectId) {
        if (this.formattedMonthLabel) {
          const [monthName, yearStr] = this.formattedMonthLabel.split(' ');
          const date = new Date(`${monthName} 1, ${yearStr}`);

          this.month = date.getMonth() + 1;
          this.year = date.getFullYear();
          this.monthName = monthName;

          console.log(`Parsed from formattedMonthLabel → Month: ${this.month}, Year: ${this.year}`);
        } else {
          const today = new Date();
          this.month = today.getMonth() + 1;
          this.year = today.getFullYear();
          this.monthName = today.toLocaleString('default', { month: 'long' });
        }
        this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
      } else {
        console.warn("projectId is missing in query params.");
      }
      console.log('Received projectId from query param:', this.projectId);
      console.log('Received month from query param:', this.formattedMonthLabel);
    });
    this.legendEntries = Object.entries(this.legend).map(([code, value]) => ({
      code,
      label: value.label,
      color: value.color
    }));
    this.selectedMonth = new Date(this.year, this.month - 1, 1);
    this.generateDaysForMonth(this.selectedMonth);
  }

  getEmployeeTimesheetAsCalenderByProjectId(projectId:any,month:any,year:any): void {
    this.timesheetAsCalenderByProjectId.projectId = projectId;
    this.timesheetAsCalenderByProjectId.month = month;
    this.timesheetAsCalenderByProjectId.year = year;
    this.timesheetAsCalenderByProjectId.empId = this.currentUser.empId;
    if(this.isClientDashboard){
      this.timesheetAsCalenderByProjectId.allEmp = !this.isClientDashboard;
    console.log("this.timesheetAsCalenderByProjectId.allEmp - if -",this.timesheetAsCalenderByProjectId.allEmp)
    } else {
      this.timesheetAsCalenderByProjectId.allEmp = !this.isClientDashboard;
    console.log("this.timesheetAsCalenderByProjectId.allEmp - else -",this.timesheetAsCalenderByProjectId.allEmp)
    }

    this.timesheetService.getEmployeeTimesheetAsCalenderByProjectId(this.timesheetAsCalenderByProjectId)
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

          timesheetData: this.fillTimesheetDays(item.timesheetData),
        }));

        this.filteredTimesheetData = [...this.timesheetData];
      } else {
        this.openAlertMod(response.serviceResponse);
      }
    });
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

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  openAlertMod( message: any) {
    this.modalRef = this.modalService.open(this.alertTemplate, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
   this.modalRef.close();
  }


  toggleSearch(): void {
  this.isSearchEnabled = !this.isSearchEnabled;

  if (!this.isSearchEnabled) {
    this.filters = {};
    this.filteredTimesheetData = [...this.timesheetData]; // reset to original
  }
}

onSearch(searchData: any): void {
  this.filters = searchData;
  console.log('Search emitted:', searchData);
  this.applyFilters();
  this.page = 1; 
}

applyFilters(): void {
  if (!this.filters || Object.keys(this.filters).length === 0) {
    this.filteredTimesheetData = [...this.timesheetData];
    return;
  }

console.log('Filter keys:', Object.keys(this.filters));
console.log('Data keys:', Object.keys(this.timesheetData[0]));

  this.filteredTimesheetData = this.timesheetData.filter(item => {
    return Object.entries(this.filters).every(([key, value]) => {
      if (!value) return true;
      const filterValue = value.toString().toLowerCase().trim();

      const lowerKey = key.toLowerCase();

      if (lowerKey.startsWith('d')) {
        // const dayStatus = (item.timesheetData?.[key]?.status ?? '').toString().toLowerCase();
        // return dayStatus.includes(filterValue);
      }

      // Match against any key ignoring case (e.g., "Department" or "department")
      const matchedKey = Object.keys(item).find(k => k.toLowerCase() === lowerKey);
      if (!matchedKey) return false;

      const itemValue = (item[matchedKey] ?? '').toString().toLowerCase().trim();
      return itemValue.includes(filterValue);
    });
  });
}

  exportToExcel(): void {
    this.excelName = "Team Attendance View.xlsx";
    this.tableName = "Employee Info";
    const legendColors = this.legend;

    const formatDateTime = (dateString: any) => {
      if (!dateString) return 'NA';
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'NA';
      return `${date.getDate().toString().padStart(2, '0')}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, '0')}-${date.getFullYear()} ${date
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
        'Start Date': formatDateTime(x.startDate) || 'NA',
        'End Date': formatDateTime(x.endDate) || 'NA',
        'Expected': x.expectedTimesheetFillCount ?? 0,
        'Client Attendance Filled': x.apmosysTimesheetFilledCount ?? 0,
        'Client Attendance Not Filled': x.clientSideNotFilledCount ?? 0,
        'Client Not-Approved': x.clientSidePendingCount ?? 0,
        'Client Approved': x.clientSideApprovedCount ?? 0,
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
      if (index < 19) return { wch: 20 };
      if (wideColumns.includes(col)) return { wch: 20 };
      return { wch: 8 }; // days slightly wider now
    });

    worksheet['!freeze'] = { xSplit: 0, ySplit: 1 };

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, this.tableName);
    XLSX.writeFile(workbook, this.excelName);
  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
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
  this.month = event.getMonth() + 1;
  this.monthName = event.toLocaleString('default', { month: 'long' });
  this.year = event.getFullYear();

  this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
  this.updateFormattedMonthLabel();
  this.generateDaysForMonth(this.selectedMonth);

  datepicker.close();
}


  updateFormattedMonthLabel() {
  const options: Intl.DateTimeFormatOptions = { month: 'short', year: 'numeric' };
  this.formattedMonthLabel = this.selectedMonth.toLocaleDateString('en-US', options);
}

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

}
