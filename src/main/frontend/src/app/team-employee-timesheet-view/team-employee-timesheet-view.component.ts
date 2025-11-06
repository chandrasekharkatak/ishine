import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { GetEmployeeTimesheetAsCalender } from '../models/getEmployeeTimesheetAsCalender';
import { TimesheetService } from '../services/timesheet.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { ExportExcelService } from '../services/export-excel.service';
import { getEmployeeTimesheetAsCalenderByProjectId } from '../models/getEmployeeTimesheetAsCalenderByProjectId';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
// import * as XLSX from 'xlsx';
import * as XLSX from 'xlsx-js-style';


@Component({
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
  timesheetDataColumns: any[] = ['employmentId', 'clientSideId', 'employeeName', 'department', 'billableType', 'clientName', 'poNo', 'projectName', 'projectManagerName', 'teamName', 'startDate', 'expectedTimesheetFillCount','apmosysTimesheetFilledCount','clientSideNotFilledCount','clientSidePendingCount','clientSideApprovedCount',...Array.from({length: 31}, (_, i) => `d${i + 1}`),'present','weekOff','holiday','leave','compOff','na','halfDay','totalNoOfDays',];
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  modalRef2?: BsModalRef;
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
    DA: { label: 'Document Approved',   color: '#006400' },   // Dark green
    DP: { label: 'Document Pending',    color: '#F0AD4E' },   // Amber
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

  
  constructor(private route: ActivatedRoute,
    private modalService: BsModalService,
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
  }

  getEmployeeTimesheetAsCalenderByProjectId(projectId:any,month:any,year:any): void {
    this.timesheetAsCalenderByProjectId.projectId = projectId;
    this.timesheetAsCalenderByProjectId.month = month;
    this.timesheetAsCalenderByProjectId.year = year;
    this.timesheetAsCalenderByProjectId.empId = this.currentUser.empId;
    this.timesheetAsCalenderByProjectId.allEmp = false;

    this.timesheetService.getEmployeeTimesheetAsCalenderByProjectId(this.timesheetAsCalenderByProjectId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.timesheetData = response.serviceResponse;
          this.filteredTimesheetData = [...this.timesheetData];
        } else {
          this.openAlertMod(response.serviceResponse);
        }
    });
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
    this.modalRef = this.modalService.show(this.alertTemplate, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
   this.modalRef.hide();
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;

    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData: any) {
    this.filters = searchData;
    this.applyFilters();
    this.page = 1; 
  }

  applyFilters() {
    this.filteredTimesheetData = this.timesheetData.filter(item => {
      return Object.entries(this.filters).every(([key, value]) => {
        if (!value) return true;

        if (key.startsWith('d')) {
          const dayData = item.timesheetData?.[key];
          const dayStatus = (dayData?.status ?? '').toString().toLowerCase();
          return dayStatus.includes(value.toString().toLowerCase());
        }
        
        const itemValue = item[key];
        if (itemValue === null || itemValue === undefined) return false;
        return itemValue.toString().toLowerCase().includes(value.toString().toLowerCase());
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

    const exportData = this.timesheetData.map((x: any) => {
      const baseData: any = {
        'Emp ID': x.employmentId || 'NA',
        'Client Side ID': x.clientSideId || 'NA',
        'Employee': x.employeeName || 'NA',
        'Department': x.department || 'NA',
        'Billable Type': x.billableType || 'NA',
        'Client': x.clientName || 'NA',
        'PO No': x.poNo || 'NA',
        'Project': x.projectName || 'NA',
        'Manager': x.projectManagerName || 'NA',
        'Team': x.teamName || 'NA',
        'Start Date': formatDateTime(x.startDate) || 'NA',
        'Expected': x.expectedTimesheetFillCount ?? 0,
        'Filled': x.apmosysTimesheetFilledCount ?? 0,
        'Not Filled': x.clientSideNotFilledCount ?? 0,
        'Pending': x.clientSidePendingCount ?? 0,
        'Approved': x.clientSideApprovedCount ?? 0,
        'Present': x.present ?? 0,
        'WeekOff': x.weekOff ?? 0,
        'Holiday': x.holiday ?? 0,
        'Leave': x.leave ?? 0,
        'CompOff': x.compOff ?? 0,
        'Absent/OtherProject': x.na ?? 0,
        'HalfDay': x.halfDay ?? 0,
        'TotalNoOfDays': x.totalNoOfDays ?? 0
      };

      for (let i = 1; i <= 31; i++) {
        const dayKey = `d${i}`;
        const dayData = x.timesheetData?.[dayKey];
        baseData[`${i}`] = dayData ? `${dayData.status || '-'}` : '-';
      }

      return baseData;
    });

    const columns = [
      'Emp ID', 'Client Side ID', 'Employee', 'Department', 'Billable Type', 'Client', 'PO No', 'Project',
      'Manager', 'Team', 'Start Date', 'Expected', 'Filled', 'Not Filled', 'Pending', 'Approved',
      ...Array.from({ length: 31 }, (_, i) => `${i + 1}`)
    ];

    const worksheet = XLSX.utils.json_to_sheet(exportData, { header: columns });


    // Style headers (Row 1)
  columns.forEach((col, colIndex) => {
    const cell = XLSX.utils.encode_cell({ c: colIndex, r: 0 });
    if (worksheet[cell]) {
      worksheet[cell].s = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "193D8A" } },
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

  // Apply per-day color styling (Row 2 onwards)
  exportData.forEach((row, rowIndex) => {
    for (let colIndex = 16; colIndex < columns.length; colIndex++) { // days start from 16th column (0-based)
      const status = row[columns[colIndex]];
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
    }
  });

  const wideColumns = [
    'Present',
    'WeekOff',
    'Holiday',
    'Leave',
    'CompOff',
    'Absent/OtherProject',
    'HalfDay',
    'TotalNoOfDays'
  ];

  worksheet['!cols'] = columns.map((col, index) => {
    if (index < 16) {
      return { wch: 18 }; // existing logic for base columns
    } else if (wideColumns.includes(col)) {
      return { wch: 18 }; // wider columns for summary fields
    } else {
      return { wch: 4 }; // default width for day columns
    }
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
  this.month = event.getMonth() + 1;
  this.monthName = event.toLocaleString('default', { month: 'long' });
  this.year = event.getFullYear();

  this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
  this.updateFormattedMonthLabel();

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

}
