import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Sort } from '@angular/material/sort';
import { getEmployeeTimesheetAsCalender } from '../models/getEmployeeTimesheetAsCalender';
import { TimesheetService } from '../services/timesheet.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { ExportExcelService } from '../services/export-excel.service';
import { TimesheetData } from '../models/timesheetData';

@Component({
  selector: 'app-team-employee-timesheet-view',
  templateUrl: './team-employee-timesheet-view.component.html',
  styleUrls: ['./team-employee-timesheet-view.component.css']
})
export class TeamEmployeeTimesheetViewComponent implements OnInit {

  projectId: any;
  timesheetData: getEmployeeTimesheetAsCalender[] = [];
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

  constructor(private route: ActivatedRoute,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      if (this.projectId) {
        this.getEmployeeTimesheetAsCalender(this.projectId,7,2025);
      } else {
        console.warn("projectId is missing in query params.");
      }
      console.log('Received projectId from query param:', this.projectId);
    });
  }

  getEmployeeTimesheetAsCalender(projectId:any,month:any,year:any): void {
    this.timesheetService.getEmployeeTimesheetAsCalender(projectId,month,year).pipe(first()).subscribe((response: any) => {
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
        baseData[`D${i}`] = dayData ? `${dayData.status || '-'}` : '-';
      }

      return baseData;
    });

    this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }

}
