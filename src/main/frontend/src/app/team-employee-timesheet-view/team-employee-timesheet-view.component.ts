import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { GetEmployeeTimesheetAsCalender } from '../models/getEmployeeTimesheetAsCalender';
import { TimesheetService } from '../services/timesheet.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { ExportExcelService } from '../services/export-excel.service';

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
  timesheetDataColumns: any[] = ['employmentId', 'clientSideId', 'employeeName', 'department', 'billableType', 'clientName', 'poNo', 'projectName', 'projectManagerName', 'teamName', 'startDate', 'expectedTimesheetFillCount','apmosysTimesheetFilledCount','clientSideNotFilledCount','clientSidePendingCount','clientSideApprovedCount',...Array.from({length: 31}, (_, i) => `d${i + 1}`)];
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  isSearchEnabled: boolean = false;
  excelName: any;
  tableName: any;
  selectedMonth = new Date();
  month:any;
  year:any;
  monthName:any;
  legend: { [key: string]: { label: string; color: string } } = {
    O:   { label: 'Other Project',        color: '#1c1f23' },
    A:   { label: 'Absent',               color: '#8b0000' },
    NW:  { label: 'Non-Working Day',      color: '#343a40' },
    AH:  { label: 'Apmosys Holiday',       color: '#0b3c5d' },
    WO:  { label: 'Week Off',             color: '#4b371c' },
    H:   { label: 'Holiday',              color: '#5a4b00' },
    CH:  { label: 'Client Holiday',       color: '#3e2f1c' },
    DA:  { label: 'Document Approved',    color: '#003366' },
    DP:  { label: 'Document Pending',     color: '#664400' },
    P:   { label: 'Present',              color: '#014421' },
    NA:  { label: 'Not Applicable',       color: '#2f4f4f' }
  };
  legendEntries: { code: string; label: string; color: string }[] = [];
  hideTimeout: any;
  hoveredEmpId: string | null = null;
  formattedMonthLabel: any;
  
  constructor(private route: ActivatedRoute,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService) { }

  ngOnInit(): void {
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
    this.timesheetService.getEmployeeTimesheetAsCalenderByProjectId(projectId,month,year).pipe(first()).subscribe((response: any) => {
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
        'Start Date': x.startDate || 'NA',
        'Expected': x.expectedTimesheetFillCount ?? 0,
        'Filled': x.apmosysTimesheetFilledCount ?? 0,
        'Not Filled': x.clientSideNotFilledCount ?? 0,
        'Pending': x.clientSidePendingCount ?? 0,
        'Approved': x.clientSideApprovedCount ?? 0
      };

      for (let i = 1; i <= 31; i++) {
        const dayKey = `d${i}`;
        const dayData = x.timesheetData?.[dayKey];
        baseData[`${i}`] = dayData ? `${dayData.status || '-'}` : '-';
      }

      return baseData;
    });

    this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }

  monthSelected(event: Date, datepicker: any) {
    this.selectedMonth = new Date(event.getFullYear(), event.getMonth(), 1);
    this.month = event.getMonth() + 1;
    this.monthName = event.toLocaleString('default', { month: 'long' });
    this.year = event.getFullYear();
    this.getEmployeeTimesheetAsCalenderByProjectId(this.projectId, this.month, this.year);
    datepicker.close();
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
