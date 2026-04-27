import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { MatSelectChange } from '@angular/material/select';
import { GetEmployeeTimesheetAsCalender } from '../models/getEmployeeTimesheetAsCalender';
import { TimesheetService } from '../services/timesheet.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ExportExcelService } from '../services/export-excel.service';
import { getEmployeeTimesheetAsCalenderByProjectId } from '../models/getEmployeeTimesheetAsCalenderByProjectId';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { ResourceManagementService } from '../services/resource-management.service';
// import * as XLSX from 'xlsx';
import * as XLSX from 'xlsx-js-style';
import { ColorAxis } from 'highcharts';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';


@Component({
  standalone: false,
  selector: 'app-team-employee-timesheet-view',
  templateUrl: './team-employee-timesheet-view.component.html',
  styleUrls: ['./team-employee-timesheet-view.component.css']
})

export class TeamEmployeeTimesheetViewComponent implements OnInit {

  projectId: any;
  poProjectId: any;
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
  @ViewChild("alertDocTemplate")
  alertOfDocTemplate: TemplateRef<any>;
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
  maxDateValue: string = '';
  fromDateFilter: string = '';
  toDateFilter: string = '';
  originalFromDateFilter: string = '';
  originalToDateFilter: string = '';
  effectiveFromDateFilter: string = '';
  effectiveToDateFilter: string = '';
  queryFromDate: string | null = null;
  queryToDate: string | null = null;
  monthOptions: { value: string; label: string; month: number; year: number; startDate: Date; endDate: Date }[] = [];
  selectedMonthValue: string = '';
  readonly allPoOptionValue = '__ALL_PO__';
  selectedPoIdFilters: string[] = [];
  wasAllPoSelected: boolean = false;
  poIdOptions: { value: string; label: string }[] = [];
  poOptionsLoadedFromApi: boolean = false;
minYear!: Date;
maxYear!: Date;
  legend: { [key: string]: { label: string; color: string } } = {
    O:  { label: 'Other Project',       color: '#0da79fff' },
    A:  { label: 'Absent',              color: '#d8221cff' },   // Red (alert)
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
  hideTimeout: any;
  hoveredEmpId: string | null = null;
  timesheetAsCalenderByProjectId : getEmployeeTimesheetAsCalenderByProjectId = new getEmployeeTimesheetAsCalenderByProjectId();
  currentUser:User;
  formattedMonthLabel: string = '';
  isClientDashboard: boolean;
  daysInMonth: { dayNumber: number; dayName: string }[] = [];
  projectName: any;
  selectedProjectName: string | null = null;
  selectedEmpId: any;
  previewBase64!: string;
  previewMimeType!: string;
  @ViewChild("previewModal")
  previewModal: TemplateRef<any>;
  empId: number;
  previewUrl: SafeResourceUrl | null = null;
  fileType: string = '';
  mimeType: string = '';
  zoomScale = 1;
  zoomLevel = 100;
  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
alertMessageOfDoc: any;

  constructor(private route: ActivatedRoute,
    private modalService: NgbModal,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private authenticationService: AuthenticationService,
    private resourceManagementService: ResourceManagementService,
    private sanitizer: DomSanitizer,
  ){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
     const currentYear = this.currentDate.getFullYear();
  this.maxDateValue = this.formatDateForInput(this.currentDate);
  this.minYear = new Date(currentYear - 1, 0, 1);
  this.maxYear = new Date(currentYear, 11, 31);
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      this.poProjectId = params['poProjectId'];
      this.formattedMonthLabel = params['formattedMonthLabel'];
      this.isClientDashboard = params['isClientDashboard'] === 'true';
      this.queryFromDate = this.normalizeIncomingDate(params['fromDate']);
      this.queryToDate = this.normalizeIncomingDate(params['toDate']);
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
        this.selectedMonth = new Date(this.year, this.month - 1, 1);
        this.generateDaysForMonth(this.selectedMonth);
        if (this.isClientDashboard) {
          this.applyIncomingDateRangeToMonthContext();
          this.initializeDateRange();
          this.loadPoOptionsFromApi(() => {
            this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
          });
        } else {
          this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
        }
      } else {
        console.warn("projectId is missing in query params.");
      }
      console.log('Received projectId from query param:', this.projectId);
      console.log('Received poProjectId from query param:', this.poProjectId);
      console.log('Received month from query param:', this.formattedMonthLabel);
    });
    this.legendEntries = Object.entries(this.legend).map(([code, value]) => ({
      code,
      label: value.label,
      color: value.color
    }));
    if (this.year && this.month) {
      this.selectedMonth = new Date(this.year, this.month - 1, 1);
      this.generateDaysForMonth(this.selectedMonth);
      if (this.isClientDashboard) {
        this.initializeDateRange();
      }
    }
  }

  getEmployeeTimesheetAsCalenderByProjectId(projectId:any,month:any,year:any): void {
    const resolvedProjectId = this.resolveProjectIdFromParams();
    const resolvedPoProjectId = this.resolvePoProjectIdFromParams();

    if (resolvedProjectId === null && resolvedPoProjectId === null) {
      this.openAlertMod('No project is selected.');
      return;
    }

    this.timesheetAsCalenderByProjectId.projectId = resolvedProjectId;
    this.timesheetAsCalenderByProjectId.month = month;
    this.timesheetAsCalenderByProjectId.year = year;
    this.timesheetAsCalenderByProjectId.empId = this.currentUser.empId;
    this.timesheetAsCalenderByProjectId.fromDate = this.effectiveFromDateFilter || this.fromDateFilter || undefined;
    this.timesheetAsCalenderByProjectId.toDate = this.effectiveToDateFilter || this.toDateFilter || undefined;
    this.timesheetAsCalenderByProjectId.poNo = this.resolveSelectedPoNo();
    this.timesheetAsCalenderByProjectId.poProjectId = resolvedPoProjectId;
    // Always refresh list from latest API response.
    this.timesheetData = [];
    this.filteredTimesheetData = [];
    this.page = 1;
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
          poProjectId: item.poProjectId ?? null,
          poId: item.poId ?? item.poNo ?? 'NA',
          poNo: item.poNo ?? 'NA',
          projectName: item.projectName ?? 'NA',
          projectManagerName: item.projectManagerName ?? 'NA',
          teamName: item.teamName ?? 'NA',
          startDate: item.startDate ?? 'NA',

          timesheetData: this.fillTimesheetDays(item.timesheetData),
        }));

        if (!this.poOptionsLoadedFromApi) {
          this.buildPoIdOptions();
        }
        this.filteredTimesheetData = [...this.timesheetData];
        this.applyFilters();
      } else {
        this.openAlertMod(response.serviceResponse);
      }
    });
  }

  private resolvePoProjectIdFromParams(): number | null {
    const parsedQueryPoProjectId = Number(this.poProjectId);
    return Number.isFinite(parsedQueryPoProjectId) ? parsedQueryPoProjectId : null;
  }

  private resolveProjectIdFromParams(): number | null {
    const parsedProjectId = Number(this.projectId);
    return Number.isFinite(parsedProjectId) ? parsedProjectId : null;
  }

  private resolveSelectedPoNo(): string | string[] | null {
    const hasAllSelected = this.selectedPoIdFilters?.includes(this.allPoOptionValue);
    if (hasAllSelected) {
      return 'All';
    }

    const selectedPoNos = (this.selectedPoIdFilters || [])
      .filter(value => value && value !== this.allPoOptionValue)
      .map(value => value.toString().trim())
      .filter(value => value.length > 0);

    if (selectedPoNos.length > 1) {
      return selectedPoNos;
    }

    if (selectedPoNos.length === 1) {
      return [selectedPoNos[0]];
    }

    const poProjectId = this.resolvePoProjectIdFromParams();
    if (poProjectId === null) {
      return null;
    }

    const selectedOption = this.poIdOptions.find(option => Number(option.value) === poProjectId);
    return selectedOption?.label ? [selectedOption.label] : null;
  }

  private buildPoIdOptions(): void {
    const poMap = new Map<string, string>();
    this.timesheetData.forEach(item => {
      const poNo = (((item as any)?.poNo) ?? '').toString().trim();
      if (!poNo || poNo === 'NA') return;
      if (!poMap.has(poNo)) {
        poMap.set(poNo, poNo);
      }
    });

    this.poIdOptions = Array.from(poMap.entries())
      .map(([value, label]) => ({ value, label }))
      .sort((a, b) => a.label.localeCompare(b.label));

    if (!this.poIdOptions.length) {
      this.selectedPoIdFilters = [];
      this.wasAllPoSelected = false;
      return;
    }

    const validPoIds = new Set(this.poIdOptions.map(opt => opt.value));
    const kept = this.selectedPoIdFilters.filter(v => v === this.allPoOptionValue || validPoIds.has(v));
    this.selectedPoIdFilters = kept.length ? kept : [this.allPoOptionValue, ...this.poIdOptions.map(opt => opt.value)];
    this.wasAllPoSelected = this.selectedPoIdFilters.includes(this.allPoOptionValue);
  }

  private loadPoOptionsFromApi(onComplete?: () => void): void {
    if (!this.projectId || !this.originalFromDateFilter || !this.originalToDateFilter) {
      onComplete?.();
      return;
    }

    this.resourceManagementService
      .getAllPosForProjectAndDate(this.projectId, this.originalFromDateFilter, this.originalToDateFilter)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response?.serviceStatus === 'Success' && Array.isArray(response?.serviceResponse)) {
            const poMap = new Map<string, string>();
            response.serviceResponse.forEach((item: any) => {
              const poNo = (item?.poNo ?? '').toString().trim();
              if (!poNo) return;
              if (!poMap.has(poNo)) {
                poMap.set(poNo, poNo);
              }
            });

            this.poIdOptions = Array.from(poMap.entries())
              .map(([value, label]) => ({ value, label }))
              .sort((a, b) => a.label.localeCompare(b.label));

            this.poOptionsLoadedFromApi = this.poIdOptions.length > 0;
            if (this.poIdOptions.length > 0) {
              this.selectedPoIdFilters = [this.allPoOptionValue, ...this.poIdOptions.map(opt => opt.value)];
              this.wasAllPoSelected = true;
            } else {
              this.selectedPoIdFilters = [];
              this.wasAllPoSelected = false;
            }
          } else {
            this.openAlertMod(response?.serviceResponse || response?.serviceMessage || 'Unable to fetch PO details.');
            this.poOptionsLoadedFromApi = false;
            this.buildPoIdOptions();
          }
          onComplete?.();
        },
        error: (err: any) => {
          this.openAlertMod(err?.error || 'Error while fetching PO details.');
          this.poOptionsLoadedFromApi = false;
          this.buildPoIdOptions();
          onComplete?.();
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

  openAlertModForDocPreview(template: TemplateRef<any>, message: any) {
  this.modalRef= this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  this.alertMessage = message;
}

  cancelRequest() {
   this.modalRef?.close();
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
  const hasColumnFilters = !!this.filters && Object.keys(this.filters).length > 0;
  const baseFiltered = !hasColumnFilters
    ? [...this.timesheetData]
    : this.timesheetData.filter(item => {
        return Object.entries(this.filters).every(([key, value]) => {
          if (!value) return true;
          const filterValue = value.toString().toLowerCase().trim();
          const lowerKey = key.toLowerCase();

          if (lowerKey.startsWith('d')) {
            return true;
          }

          const matchedKey = Object.keys(item).find(k => k.toLowerCase() === lowerKey);
          if (!matchedKey) return false;

          const itemValue = (item[matchedKey] ?? '').toString().toLowerCase().trim();
          return itemValue.includes(filterValue);
        });
      });
  this.filteredTimesheetData = baseFiltered;
}

onPoIdFilterChange(event: MatSelectChange): void {
  let values = Array.isArray(event.value) ? [...event.value] : [];
  const poValues = this.poIdOptions.map(opt => opt.value);
  const hasAll = values.includes(this.allPoOptionValue);

  if (hasAll && !this.wasAllPoSelected) {
    values = [this.allPoOptionValue, ...poValues];
  } else if (hasAll && this.wasAllPoSelected) {
    const individual = values.filter(v => v !== this.allPoOptionValue);
    if (individual.length !== poValues.length) {
      values = individual;
    }
  }

  const selectedIndividuals = values.filter(v => v !== this.allPoOptionValue);
  if (selectedIndividuals.length === poValues.length && poValues.length > 0) {
    values = [this.allPoOptionValue, ...poValues];
  }

  if (values.length === 0 && poValues.length > 0) {
    values = [this.allPoOptionValue, ...poValues];
  }

  this.selectedPoIdFilters = values;
  this.wasAllPoSelected = this.selectedPoIdFilters.includes(this.allPoOptionValue);
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

onDateRangeChange(): void {
  if (!this.fromDateFilter || !this.toDateFilter) {
    return;
  }

  const fromDate = new Date(this.fromDateFilter);
  const toDate = new Date(this.toDateFilter);
  const now = new Date(this.maxDateValue);

  if (fromDate > now) {
    this.fromDateFilter = this.maxDateValue;
  }

  if (new Date(this.toDateFilter) > now) {
    this.toDateFilter = this.maxDateValue;
  }

  if (new Date(this.fromDateFilter) > new Date(this.toDateFilter)) {
    this.toDateFilter = this.fromDateFilter;
  }

  this.originalFromDateFilter = this.fromDateFilter;
  this.originalToDateFilter = this.toDateFilter;
  this.buildMonthOptionsFromRange();
  this.updateEffectiveDateRangeFromSelectedMonths();
}

onMonthFilterSelectionChange(): void {
  this.updateEffectiveDateRangeFromSelectedMonths();
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

  if (!this.isClientDashboard) {
    this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
  }
  this.updateFormattedMonthLabel();
  this.generateDaysForMonth(this.selectedMonth);
  if (this.isClientDashboard) {
    this.initializeDateRange();
  }

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

  private initializeDateRange(): void {
    const firstDay = new Date(this.selectedMonth.getFullYear(), this.selectedMonth.getMonth(), 1);
    const lastDay = new Date(this.selectedMonth.getFullYear(), this.selectedMonth.getMonth() + 1, 0);

    const defaultFrom = this.formatDateForInput(firstDay);
    const defaultTo = this.formatDateForInput(lastDay > this.currentDate ? this.currentDate : lastDay);

    const incomingFrom = this.queryFromDate || defaultFrom;
    const incomingTo = this.queryToDate || defaultTo;
    const normalizedRange = this.normalizeDateRange(incomingFrom, incomingTo);

    this.originalFromDateFilter = normalizedRange.fromDate;
    this.originalToDateFilter = normalizedRange.toDate;
    this.fromDateFilter = this.originalFromDateFilter;
    this.toDateFilter = this.originalToDateFilter;
    this.effectiveFromDateFilter = this.fromDateFilter;
    this.effectiveToDateFilter = this.toDateFilter;
    this.buildMonthOptionsFromRange();
    this.selectedMonthValue = this.monthOptions[0]?.value || '';
    this.updateEffectiveDateRangeFromSelectedMonths(false);
  }

  private buildMonthOptionsFromRange(): void {
    const start = new Date(this.originalFromDateFilter);
    const end = new Date(this.originalToDateFilter);
    this.monthOptions = [];

    if (isNaN(start.getTime()) || isNaN(end.getTime()) || start > end) {
      return;
    }

    let pointer = new Date(start.getFullYear(), start.getMonth(), 1);
    const endMarker = new Date(end.getFullYear(), end.getMonth(), 1);

    while (pointer <= endMarker) {
      const month = pointer.getMonth() + 1;
      const year = pointer.getFullYear();
      const value = `${year}-${String(month).padStart(2, '0')}`;
      const label = pointer.toLocaleString('default', { month: 'long', year: 'numeric' });
      const startDate = new Date(year, month - 1, 1);
      const endDate = new Date(year, month, 0);
      this.monthOptions.push({ value, label, month, year, startDate, endDate });
      pointer = new Date(year, month, 1);
    }
  }

  private updateEffectiveDateRangeFromSelectedMonths(fetchData: boolean = false): void {
    let nextEffectiveFrom = this.originalFromDateFilter;
    let nextEffectiveTo = this.originalToDateFilter;

    if (!this.selectedMonthValue) {
      nextEffectiveFrom = this.originalFromDateFilter;
      nextEffectiveTo = this.originalToDateFilter;
    } else {
      const selectedOption = this.monthOptions.find(opt => opt.value === this.selectedMonthValue);
      if (!selectedOption) {
        nextEffectiveFrom = this.originalFromDateFilter;
        nextEffectiveTo = this.originalToDateFilter;
      } else {
        const lowerBound = new Date(this.originalFromDateFilter);
        const upperBound = new Date(this.originalToDateFilter);
        const rangeStart = selectedOption.startDate > lowerBound ? selectedOption.startDate : lowerBound;
        const rangeEnd = selectedOption.endDate < upperBound ? selectedOption.endDate : upperBound;
        nextEffectiveFrom = this.formatDateForInput(rangeStart);
        nextEffectiveTo = this.formatDateForInput(rangeEnd);
      }
    }

    this.effectiveFromDateFilter = nextEffectiveFrom;
    this.effectiveToDateFilter = nextEffectiveTo;
    this.syncSelectedMonthFromRange(fetchData);
  }

  private syncSelectedMonthFromRange(fetchData: boolean): void {
    const effectiveFrom = new Date(this.effectiveFromDateFilter || this.fromDateFilter);
    if (isNaN(effectiveFrom.getTime())) {
      return;
    }

    const nextMonth = effectiveFrom.getMonth() + 1;
    const nextYear = effectiveFrom.getFullYear();
    const monthChanged = nextMonth !== this.month || nextYear !== this.year;

    this.month = nextMonth;
    this.year = nextYear;
    this.selectedMonth = new Date(nextYear, nextMonth - 1, 1);
    this.monthName = this.selectedMonth.toLocaleString('default', { month: 'long' });
    this.updateFormattedMonthLabel();
    this.generateDaysForMonth(this.selectedMonth);

    if (fetchData && monthChanged && this.projectId) {
      this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
    }
  }

  applyClientDashboardFilters(): void {
    if (!this.projectId) {
      return;
    }
    this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
  }

  get monthDisplayLabel(): string {
    const selectedOption = this.monthOptions.find(opt => opt.value === this.selectedMonthValue);
    return selectedOption?.label || 'Select Month';
  }

  get poDisplayLabel(): string {
    if (!this.selectedPoIdFilters?.length || this.selectedPoIdFilters.includes(this.allPoOptionValue)) {
      return 'All';
    }
    const labels = this.poIdOptions
      .filter(opt => this.selectedPoIdFilters.includes(opt.value))
      .map(opt => opt.label);
    return labels.length ? labels.join(', ') : 'Select PO No';
  }

  private formatDateForInput(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }

  private normalizeIncomingDate(value: any): string | null {
    if (!value) return null;
    const parsed = new Date(value);
    if (isNaN(parsed.getTime())) return null;
    return this.formatDateForInput(parsed);
  }

  private normalizeDateRange(fromDate: string, toDate: string): { fromDate: string; toDate: string } {
    const maxDate = new Date(this.maxDateValue);
    let from = new Date(fromDate);
    let to = new Date(toDate);

    if (isNaN(from.getTime())) from = new Date(this.maxDateValue);
    if (isNaN(to.getTime())) to = new Date(this.maxDateValue);

    if (from > maxDate) from = maxDate;
    if (to > maxDate) to = maxDate;
    if (from > to) {
      to = new Date(from);
    }

    return {
      fromDate: this.formatDateForInput(from),
      toDate: this.formatDateForInput(to)
    };
  }

  private applyIncomingDateRangeToMonthContext(): void {
    if (!this.queryFromDate) return;
    const from = new Date(this.queryFromDate);
    if (isNaN(from.getTime())) return;
    this.month = from.getMonth() + 1;
    this.year = from.getFullYear();
    this.monthName = from.toLocaleString('default', { month: 'long' });
    this.selectedMonth = new Date(this.year, this.month - 1, 1);
    this.updateFormattedMonthLabel();
  }

  downloadFinalDocuments() {

    if (!this.filteredTimesheetData || this.filteredTimesheetData.length === 0) {
      console.error('No timesheet data available');
      return;
    }

    const projectName = this.filteredTimesheetData[0].projectName;

    if (!projectName) {
      console.error('Project name not found in table data');
      return;
    }

    const payload:{
      empId:number;
      projectId:number;
      month:number;
      year:number;
    }[] = [];

    this.filteredTimesheetData.forEach(emp => {
      payload.push({
        empId: emp.empId,
        projectId: this.projectId,
        month: this.month,
        year: this.year
      });
    });

    const safeProjectName = projectName
      .replace(/\s+/g, '_')
      .replace(/[^a-zA-Z0-9_]/g, '');

    const fileName = `${safeProjectName}_${this.month}_${this.year}.zip`;

    this.timesheetService.downloadFinalDocuments(payload)
      .subscribe((blob: Blob) => {

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();

        window.URL.revokeObjectURL(url);
      },
        error => {
          if (error?.error instanceof Blob) {

      const reader = new FileReader();

      reader.onload = () => {
        const message = reader.result as string;
        this.openDocAlertMod(this.alertOfDocTemplate, message);
      };

      reader.readAsText(error.error);
      return;
    }
          console.error('Download failed', error);
        });
  }
  openDocAlertMod(template: TemplateRef<any>, message: string) {
  this.alertMessageOfDoc = message;
  this.modalRef = this.modalService.open(template, {
    modalDialogClass: 'modal-sm',
    centered: true
  });
}
previewName  : string = '';
viewEmployeeTimesheet(empId: any, projectId: any): void {
  this.selectedEmpId = empId;

  const payload = {
    projectId: this.projectId,
    month: this.month,
    year: this.year,
    empId: this.currentUser.empId,
    selectedEmpId: this.selectedEmpId
  };

  
  this.timesheetService.getDocumentsBySelectedEmpId(payload).subscribe({
    next: (res: any) => {
      if (res.serviceStatus === 'Success' && res.serviceResponse) {

        const docArray = res.serviceResponse[0];

        console.log('serviceResponse:', res.serviceResponse);
console.log('type:', typeof res.serviceResponse);
console.log('isArray:', Array.isArray(res.serviceResponse));

        const doc = {
          docMimeType: docArray.docMimeType,
          fileName: docArray.fileName,
          docData: docArray.docData
        };
        this.previewName = doc.fileName;
        if (doc.docData && doc.docMimeType) {
          this.showPreview(doc.docData, doc.docMimeType, doc.fileName);
        } else {
          this.openAlertModForDocPreview(this.alertTemplate, 'No valid document data found.');
        }

      } else {
        this.openAlertModForDocPreview(
          this.alertTemplate,
          res.serviceMessage || 'No document found.'
        );
      }
    },
    error: () => {
      this.openAlertModForDocPreview(this.alertTemplate, 'Error while fetching document.');
    }
  });
}

previewFileName : string = '';

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

    this.previewFileName = this.previewName || 'Document Preview';
    // this.modalRef2 = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-xxl modal-dialog-centered',scrollable: true });
    
    this.modalRef = this.modalService.open(this.previewModal, {
    modalDialogClass: 'modal-xl modal-dialog-centered',
    scrollable: false   
    });

  }

  resetPreviewState() {
  this.zoomScale = 1;
  this.zoomLevel = 100;
  this.translateX = 0;
  this.translateY = 0;
  this.isDragging = false;
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

    // URL.revokeObjectURL(blobUrl);
    // setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);  // ✅ revoke after download starts
  }

  //   private buildFileName(): string {
  //   const userName = this.userName || 'User';
  //   const day = this.dateObj?.day || 'Date';
  //   const month = this.formattedMonthLabel;
  //   const project = this.projectList.find(p => p.projectId === this.projectIdForDropDown);
  //   const projectName = project?.projectName || 'Project';
  //   const extension = this.getExtensionFromMime(this.previewMimeType);

  //   return `${userName} | ${day} ${month} | ${projectName}.${extension}`;
  // }

  buildFileName(): string {
  const name = this.previewFileName?.trim() || 'document';

  // Remove extension from filename
  const nameWithoutExt = name.includes('.')
    ? name.substring(0, name.lastIndexOf('.'))
    : name;

  const ext = name.includes('.')
    ? name.substring(name.lastIndexOf('.') + 1)
    : (this.previewMimeType?.split('/')[1] || 'bin');

  // Generate current timestamp → 20250401_143022
  const now = new Date();
  const timestamp =
    now.getFullYear().toString() +
    String(now.getMonth() + 1).padStart(2, '0') +
    String(now.getDate()).padStart(2, '0') + '_' +
    String(now.getHours()).padStart(2, '0') +
    String(now.getMinutes()).padStart(2, '0') +
    String(now.getSeconds()).padStart(2, '0');

  return `${nameWithoutExt}_${timestamp}.${ext}`;
  // e.g. → march_timesheet_20250401_143022.pdf
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


}
