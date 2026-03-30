import { ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { User } from 'src/app/models/user';
import { AttendanceReconciliationService } from 'src/app/services/attendance-reconciliation.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { DatePipe } from '@angular/common';
import { Query } from 'src/app/models/query';
import { LeaveService } from 'src/app/services/leave.service';
import { UtilityService } from 'src/app/services/utility.service';
import { AppComponent } from 'src/app/app.component';
import { Observable } from 'rxjs';
import { finalize, first } from 'rxjs/operators';
import { Biomax } from 'src/app/models/biomax';
import * as Highcharts from 'highcharts';
import { EmployeeService } from 'src/app/services/employee.service';
import { SortPipe } from 'src/app/sort.pipe';
import { Feature } from 'src/app/models/feature';


class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

interface AttendanceSummary {
  totalEmployees: number;
  presentToday: number;
  lateArrivals: number;
  underNineHours: number;
  absentToday: number;
}

interface AttendanceStatusDistribution {
  onTime: number;
  late: number;
  /** Rows with no in-time (matches Detailed Log "Absent" filter). */
  absent: number;
  /** Employees in headcount with no row for the reference day (not the same as Absent in the log). */
  noRecord: number;
  total: number;
}

interface AttendanceTrendPoint {
  date: string;
  presentCount: number;
  absentCount: number;
}
@Component({
  standalone: false,
  selector: 'app-attendance-reconciliation',
  templateUrl: './attendance-reconciliation.component.html',
  styleUrls: ['./attendance-reconciliation.component.css']
})

export class AttendanceReconciliationComponent implements OnInit {

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  leaveReportColumns: any;
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef:NgbModalRef;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  isSearchEnabled: boolean = false;
  isLeaveReportTable: boolean = false;
  punchData: any;
  currentUser: User;
  attendanceReconciliationList: any[] = [];
  attendanceReconciliationOriginaldata: any[] = [];
  filteredData: any[] = [];
  viewMoreList: any[] = [];
  queryList: any[] = [];
  storedDataList: any[] = [];
  excelName: any;
  date: string;
  startDate: string;
  endDate: string;
  name: string;
  page = 1;
  totalRecords: number = 0;
  pageSize: number = 20;
  readonly pageSizeOptions = [10, 20, 50, 100, 200];
  /** Max rows fetched when column filters are on (client-side slice + paginate). */
  private readonly tableFetchPageSize = 10000;

  /** Tracks column filter presence for switching server vs full fetch (avoid refetch on every keystroke). */
  private prevColumnFiltersActive = false;

  /** KPI card filter: table shows matching rows (client-paged from full fetch). */
  cardFilter: 'all' | 'presentToday' | 'late' | 'underNine' | 'absent' = 'all';
  /** True when a KPI card filter is applied (full client-side slice of cached rows). Exposed for template. */
  tableClientPaging = false;
  private fullTableRowsCache: any[] | null = null;
  /**
   * Full date-range rows from API (one fetch). Used for "All" table view so pagination matches real row count
   * (~270) instead of inflated totalRecords from count query (~508).
   */
  private fullListCache: any[] | null = null;
  formattedDate: string;
  startformattedDate: string;
  endformattedDate: string;
  maxTodayDate: any;
  AttendancereConciliation: any[] = ['employeeCode', 'employeeName', 'logDate', 'inTime', 'outTime', 'totalDuration', 'departmentName', 'reportingManagerName'];
  timesheetColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By', 'Leave Type'];

  filters: any = {};
  fromDate: string = '';
  toDate: string = '';
  currentDate: string;
  Math = Math;

  /** True while a full-dataset Excel export is running. */
  exportInProgress = false;

  feature = 'Reports';
  userMapping: any = {};

  constructor(
    private modalService: NgbModal,
    private exportExcelService: ExportExcelService,
    private attendanceReconciliationService: AttendanceReconciliationService,
    private authenticationService: AuthenticationService,
    private datePipe: DatePipe,
    private leaveService: LeaveService,
    private utilityService: UtilityService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {
    this.maxTodayDate = new Date().toISOString().split('T')[0];
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x)
    const today = new Date();
    this.currentDate = today.toISOString().split('T')[0];
    this.startDate = this.currentDate;
    this.endDate = this.endDate;
    // this.formattedDate = this.formatDate(this.date);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return this.datePipe.transform(date, 'dd-MMM-yyyy')!;
  }

  private applyEmployeeRowFormatting(list: any[]): void {
    if (!list?.length) {
      return;
    }
    list.forEach((employee) => {
      employee.emp360 = employee.empId;
      employee.employeementId = String(employee.employeeCode);
      if (employee.employeementId.startsWith('A')) {
        employee.employeementId = employee.employeementId.substring(1);
      }
      employee.employeementId = 'A-'.concat(employee.employeementId);
    });
  }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
        featureMap.subFeatures?.forEach(sub => {
          this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
        });
    this.date = moment().format("YYYY-MM-DD");
    this.startDate = moment().format("YYYY-MM-DD");
    this.endDate = moment().format("YYYY-MM-DD");

    this.formattedDate = this.formatDate(this.date);
    this.startformattedDate = this.formatDate(this.startDate);
    this.endformattedDate = this.formatDate(this.endDate);;
    //  this.date=this.formattedDate;
    // console.log("ckeck date =======", this.startDate);
    // console.log("ckeck date =======", this.endDate);
    this.getBioMatricData(this.startformattedDate, this.endformattedDate);
  }

  onSearch(searchData: any) {
    this.filters = searchData;
    this.page = 1; // Reset to first page when searching
    
    // Check if any filter has value
    const hasFilters = searchData && Object.values(searchData).some(value => value && (value as string).trim() !== '');
    
    if (hasFilters) {
      // If there are active filters, search with filters
      this.getBioMatricData(this.startDate, this.endDate);
    } else {
      // If all filters are empty, get all data
      this.filters = {};
      this.getBioMatricData(this.startDate, this.endDate);
    }
  }

  /** True when any column filter input has non-empty text (matches colFilter pipe behavior). */
  hasActiveColumnFilters(): boolean {
    if (!this.filters || typeof this.filters !== 'object') {
      return false;
    }
    return Object.keys(this.filters).some(
      (k) => this.filters[k] != null && String(this.filters[k]).trim() !== ''
    );
  }

  /** ngx-pagination: server totals when no column filters; client length when filters or KPI card mode. */
  get paginateConfig(): { itemsPerPage: number; currentPage: number; totalItems?: number } {
    const base: { itemsPerPage: number; currentPage: number; totalItems?: number } = {
      itemsPerPage: this.pageSize,
      currentPage: this.page
    };
    if (this.tableClientPaging) {
      const n = this.cardFilteredRowCount;
      return { ...base, totalItems: n };
    }
    if (this.hasActiveColumnFilters()) {
      return base;
    }
    if (this.shouldUseFullListCache()) {
      return { ...base, totalItems: this.fullListCache!.length };
    }
    return { ...base, totalItems: this.totalRecords };
  }

  /** "All" + no column filters: paginate client-side over fullListCache (accurate total vs API count query). */
  shouldUseFullListCache(): boolean {
    return (
      !this.tableClientPaging &&
      this.cardFilter === 'all' &&
      !this.hasActiveColumnFilters() &&
      this.fullListCache != null &&
      this.fullListCache.length > 0
    );
  }

  /** Record count for footer when not in KPI-only footer block. */
  get tableRecordTotalForFooter(): number {
    if (this.tableClientPaging) {
      return this.cardFilteredRowCount;
    }
    if (this.shouldUseFullListCache()) {
      return this.fullListCache!.length;
    }
    return this.totalRecords;
  }

  /** Rows matching the active KPI card (for pagination totalItems + footer text). */
  get cardFilteredRowCount(): number {
    if (!this.tableClientPaging || !this.fullTableRowsCache?.length) {
      return 0;
    }
    return this.fullTableRowsCache.filter((r) => this.rowMatchesCardFilter(r)).length;
  }

  handlePageChange(event: number): void {
    this.page = event;
    if (this.shouldUseFullListCache()) {
      return;
    }
    if (this.tableClientPaging || this.hasActiveColumnFilters()) {
      return;
    }
    this.getBioMatricData(this.startDate, this.endDate);
  }

  onPageSizeChange(): void {
    this.pageSize = Number(this.pageSize);
    this.page = 1;
    if (this.shouldUseFullListCache()) {
      return;
    }
    if (this.tableClientPaging || this.hasActiveColumnFilters()) {
      return;
    }
    this.getBioMatricData(this.startDate, this.endDate);
  }

  /** Rows for the table + paginate pipe (server list or card-filtered full list). */
  get tableRowsSource(): any[] {
    if (this.tableClientPaging) {
      if (!this.fullTableRowsCache?.length) {
        return [];
      }
      return this.fullTableRowsCache.filter((r) => this.rowMatchesCardFilter(r));
    }
    if (this.shouldUseFullListCache()) {
      return this.fullListCache!;
    }
    return this.attendanceReconciliationList;
  }

  selectCardFilter(filter: 'all' | 'presentToday' | 'late' | 'underNine' | 'absent'): void {
    this.cardFilter = filter;
    this.page = 1;
    if (filter === 'all') {
      this.tableClientPaging = false;
      this.fullTableRowsCache = null;
      this.getBioMatricData(this.startDate, this.endDate);
      return;
    }
    this.tableClientPaging = true;
    this.fullTableRowsCache = null;
    // Always request max page size — do not tie to totalRecords alone (it can match current
    // page size and only 20 rows would load, while KPI counts use the full dataset).
    const size = this.tableFetchPageSize;
    const startFmt = this.formatDate(this.startDate);
    const endFmt = this.formatDate(this.endDate);
    this.attendanceReconciliationService.getBiomatricData(startFmt, endFmt, 1, size).subscribe((response: any) => {
      const rows: any[] = response?.serviceResponse?.data || [];
      this.applyEmployeeRowFormatting(rows);
      this.fullTableRowsCache = rows;
      this.cdr.markForCheck();
    });
  }

  rowMatchesCardFilter(row: any): boolean {
    switch (this.cardFilter) {
      case 'all':
        return true;
      case 'presentToday':
        return this.getAttendanceStatus(row) !== 'Absent';
      case 'late':
        return this.getAttendanceStatus(row) === 'Late';
      case 'underNine': {
        if (this.getAttendanceStatus(row) === 'Absent') {
          return false;
        }
        const m = this.getWorkedMinutes(row?.totalDuration);
        return m > 0 && m < 540;
      }
      case 'absent':
        return this.getAttendanceStatus(row) === 'Absent';
      default:
        return true;
    }
  }

  isCardActive(filter: 'all' | 'presentToday' | 'late' | 'underNine' | 'absent'): boolean {
    return this.cardFilter === filter;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  // getBioMatricData(startDate: string, endDate: string) {
  //   const startdateformat = this.formatDate(startDate);
  //   const enddateformat = this.formatDate(endDate);

  //   this.attendanceReconciliationService.getBiomatricData(startdateformat, enddateformat, this.page, this.pageSize).subscribe((response: any) => {
  //     this.attendanceReconciliationList = response.serviceResponse.data;
  //     this.totalRecords = response.serviceResponse.totalRecords;
  //     this.attendanceReconciliationList.forEach(employee => {

  //       employee.emp360 = employee.empId;

  //       employee.employeementId = String(employee.employeeCode);
  //       if (employee.employeementId.startsWith('A'))
  //         employee.employeementId = employee.employeementId.substring(1);
  //       employee.employeementId = "A-".concat(employee.employeementId);
  //     });

  //     // console.log("this.attendanceReconciliationList" , this.attendanceReconciliationList);
  //     this.attendanceReconciliationOriginaldata = [... this.attendanceReconciliationList];
  //     this.modalRef?.close();
  //   });
  // }

  getBioMatricData(startDate: string, endDate: string) {
    const startdateformat = this.formatDate(startDate);
    const enddateformat = this.formatDate(endDate);
    const searchParams = this.getActiveBiometricSearchParams();

    this.attendanceReconciliationService.getBiomatricDataWithSearch(
      startdateformat, 
      enddateformat, 
      this.page, 
      this.pageSize,
      searchParams
    ).subscribe((response: any) => {
      this.attendanceReconciliationList = response.serviceResponse.data;
      this.totalRecords = response.serviceResponse.totalRecords;
      this.applyEmployeeRowFormatting(this.attendanceReconciliationList);

      this.attendanceReconciliationOriginaldata = [...this.attendanceReconciliationList];
      this.fullListCache = null;
      if (this.isAttendanceVisible) {
        this.loadAttendanceDashboardData();
      }
      this.loadFullDatasetForCharts();
      this.modalRef?.close();
    });
  }

  getviewMoreData(template: TemplateRef<any>, punchrecords: any, name: any) {

    this.name = name;
    this.viewMoreList.push(punchrecords);
    const recordsString = this.viewMoreList[0];
    const recordsArray = recordsString.split(',').filter(record => record);
    this.punchData = [];

    for (let i = 0; i < recordsArray.length; i += 2) {
      if (i + 1 < recordsArray.length) {
        this.punchData.push({
          in: recordsArray[i],
          out: recordsArray[i + 1]
        });
      }
    }

    // console.log("Parsed Punch Data: ", this.punchData);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  // openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
  //   this.filterData.title = title;
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: '' });

  // }

  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    //console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;
    const startdateformat = this.formatDate(this.startDate);
    const enddateformat = this.formatDate(this.endDate);

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "",startDate : startdateformat,endDate : enddateformat},
    ];

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-CS-") && (data.isConsultant == 'true')) {
            queryObj.value = "A-CS-".concat(queryObj.value);
          }
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("AP-") && (data.IsApprenticeship == 'true')) {
            queryObj.value = "AP-".concat(queryObj.value);
          }

          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    //console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  toggleSearch() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    const hadColumnFilters = this.hasActiveColumnFilters();
    this.isSearchEnabled = !this.isSearchEnabled;
    
    if (!this.isSearchEnabled) {
      // Clear all filters
      this.filters = {};
      // Reset to first page
      this.page = 1;
      // Reload data without filters
      this.getBioMatricData(this.startDate, this.endDate);
    } else {
      // When opening search, clear any existing filters and keep current data
      this.filters = {};
      this.page = 1;
      this.prevColumnFiltersActive = false;
      if (hadColumnFilters && !this.tableClientPaging) {
        this.getBioMatricData(this.startDate, this.endDate);
      }
    }
  }

  resetFilters() {
    this.filters = {};
    this.page = 1;
    this.getBioMatricData(this.startDate, this.endDate);
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  exportToExcel(): void {
    this.excelName = 'attendanceReconciliation.xlsx';

    if (this.tableClientPaging && this.fullTableRowsCache?.length) {
      const filtered = this.fullTableRowsCache.filter((r) => this.rowMatchesCardFilter(r));
      this.applyEmployeeRowFormatting(filtered);
      this.exportExcelService.exportTableDataToExcel(
        this.mapAttendanceRowsForExport(filtered),
        this.excelName
      );
      return;
    }

    if (this.shouldUseFullListCache() && this.fullListCache?.length) {
      this.exportExcelService.exportTableDataToExcel(
        this.mapAttendanceRowsForExport([...this.fullListCache]),
        this.excelName
      );
      return;
    }

    const startFmt = this.formatDate(this.startDate);
    const endFmt = this.formatDate(this.endDate);
    const searchParams = this.getActiveBiometricSearchParams();
    /** Chunked pages so we never rely on one huge LIMIT (some stacks cap rows per response). */
    const chunkSize = 500;

    this.exportInProgress = true;
    this.fetchAllBioRowsForExport(startFmt, endFmt, searchParams, chunkSize)
      .pipe(finalize(() => (this.exportInProgress = false)))
      .subscribe({
        next: (fullRows: any[]) => {
          this.applyEmployeeRowFormatting(fullRows);
          this.exportExcelService.exportTableDataToExcel(
            this.mapAttendanceRowsForExport(fullRows),
            this.excelName
          );
        },
        error: () => {}
      });
  }

  /** Column filters for biomatric API — must match getBioMatricData. */
  private getActiveBiometricSearchParams(): Record<string, string> {
    if (!this.isSearchEnabled || !this.filters || Object.keys(this.filters).length === 0) {
      return {};
    }
    const activeFilters: Record<string, string> = {};
    Object.keys(this.filters).forEach((key) => {
      const v = this.filters[key];
      if (v != null && String(v).trim() !== '') {
        activeFilters[key] = String(v);
      }
    });
    return Object.keys(activeFilters).length > 0 ? activeFilters : {};
  }

  /**
   * Walks all API pages until totalRecords (or dashboard headcount) is reached or a page is empty.
   * Do not stop on a short page when totalTarget is still higher — avoids 271 vs 508 exports.
   */
  private fetchAllBioRowsForExport(
    startFmt: string,
    endFmt: string,
    searchParams: Record<string, string>,
    chunkSize: number
  ): Observable<any[]> {
    return new Observable<any[]>((subscriber) => {
      const acc: any[] = [];
      let totalTarget = Math.max(
        Number(this.totalRecords) || 0,
        Number(this.attendanceSummary?.totalEmployees) || 0
      );
      const maxPages = 400;

      const run = (page: number) => {
        if (page > maxPages) {
          subscriber.next(acc);
          subscriber.complete();
          return;
        }
        this.attendanceReconciliationService
          .getBiomatricDataWithSearch(startFmt, endFmt, page, chunkSize, searchParams)
          .subscribe({
            next: (response: any) => {
              const data: any[] = response?.serviceResponse?.data || [];
              const tr = Number(response?.serviceResponse?.totalRecords);
              if (Number.isFinite(tr) && tr > 0) {
                totalTarget = Math.max(totalTarget, tr);
              }
              acc.push(...data);
              const empty = data.length === 0;
              const reachedCount = totalTarget > 0 && acc.length >= totalTarget;
              const shortPageUnknownTotal = data.length < chunkSize && totalTarget === 0;
              if (empty || reachedCount || shortPageUnknownTotal) {
                subscriber.next(acc);
                subscriber.complete();
                return;
              }
              run(page + 1);
            },
            error: (err) => subscriber.error(err)
          });
      };
      run(1);
    });
  }

  private mapAttendanceRowsForExport(rows: any[]): any[] {
    return rows.map((x: any) => ({
      'Employee Id': x.employeeCode,
      'Employee Name': x.employeeName,
      'Department': x.departmentName ?? 'NA',
      'Reporting Manager': x.reportingManagerName ?? 'NA',
      'Log Date': x.logDate,
      'Log IN': x.inTime,
      'Log Out': x.outTime,
      'Total Working Hours': this.minutesToReadable(x.totalDuration),
      'Shift Duration': this.minutesToReadable(x.shiftDuration),
      'Shift Name': x.shiftName ?? 'NA',
      'Begin Time': x.beginTime ?? 'NA',
      'End Time': x.endTime ?? 'NA',
      'Early By': this.minutesToReadable(x.earlyBy),
      'Late By': this.minutesToReadable(x.lateBy),
      'API Status': x.status ?? 'NA',
      'Attendance Status': this.getAttendanceStatus(x)
    }));
  }

  private minutesToReadable(value: any): string {
    if (value === null || value === undefined || value === '') {
      return 'NA';
    }

    // Value may already be HH:mm format from API.
    if (typeof value === 'string' && value.includes(':')) {
      return value;
    }

    const totalMinutes = Number(value);
    if (Number.isNaN(totalMinutes)) {
      return String(value);
    }

    const hours = Math.floor(totalMinutes / 60);
    const mins = totalMinutes % 60;
    return `${hours} hour${hours !== 1 ? 's' : ''} ${mins} min${mins !== 1 ? 's' : ''}`;
  }

  exportToExcelviewMore(): void {
    this.excelName = 'ViewMoreData.xlsx';
    const onlySpecificDataArr = this.punchData.map(
      x => ({
        "IN": x.in,
        "Employee Name": this.name,
        "OUT": x.out,
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

  searchRecords() {
    console.log('Searching records from:', this.startDate, 'to:', this.endDate);
    if (this.startDate && this.endDate) {
      this.page = 1;
      this.cardFilter = 'all';
      this.tableClientPaging = false;
      this.fullTableRowsCache = null;
      this.getBioMatricData(this.startDate, this.endDate);
    } else {
      console.error('Start date or end date is missing');
    }
  }

  clickFilter:boolean=false;
  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    this.clickFilter = true;
    if (emittedArray[0].length != 0) {
      //console.log("queryList : ", emittedArray[0]);
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
            this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
          } else {
            this.storedDataList.push(object);
          }
        }
      });


      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }

        if (query.column == 'Employee Id') {
          query.value = query.value.split("-")[1];
        }
      });

      if (this.filterData.title == 'Filter Attendance Reconciliation') {
        this.getCustomLAttendanceApplicationsList(emittedArray[0], template);
      }
      // if (this.filterData.title == 'Filter Employee Report') {
      //   this.getCustomEmployeesList(emittedArray[0], template);
      // }
      // if (this.filterData.title == 'Filter Timesheet Report') {
      //   this.getCustomTimesheetApplicationsList(emittedArray[0], template);
      // }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      if (emittedArray[1] == 'Filter Attendance Reconciliation') {
        // this.showLeaveReportTable();
      }
      // if (emittedArray[1] == 'Filter Timesheet Report') {
      //   this.showTimesheetReportTable();
      // }
      // if (emittedArray[1] == 'Filter Employee Report') {
      //   this.showEmployeeReportTable();
      // }
    }
  }

  allAttendanceRepostList: any[] = [];

  getCustomLAttendanceApplicationsList(queryObjList: any, template: TemplateRef<any>) {
    // this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList.length == 0) {
      // this.getAllLeaveApplicationsList();
    } else {
      this.leaveService.getCustomLAttendanceApplicationsList(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allAttendanceRepostList = response.serviceResponse;
          console.log("this.allAttendanceRepostList" , this.allAttendanceRepostList);

          // this.allLeaveApplicationsList = this.allLeaveApplicationsList.filter((value, index, self) =>
          //   index === self.findIndex((t) => (
          //     t.employeementId === value.employeementId && t.fromDate === value.fromDate
          //   ))
          // )

          if (this.allAttendanceRepostList.length == 0) {
            this.openAlertMod(this.alertModal, "No Leave Application Report found ")
          }
          this.allAttendanceRepostList.forEach(leave => {
            // leave.employeementId = "A-".concat(leave.employeementId);
            // leave.employeementId = (leave.isConsultant === 'true' ? "A-CS-" : "A-").concat(leave.employeementId);
            leave.employeementId = this.utilityService.getFormattedEmployeeId(leave);
            leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            leave.updatedOn = (leave.updatedOn) ? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;

            if (leave.fromDateDayType != null) {
              leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
            }
            if (leave.toDateDayType != null) {
              leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
            }
          });
          //console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }


  // biomaxList:Biomax[]=[];
  // chartdata={
  //   workinghours:0,
  //   lessthenworkinghours:0,
  //   hovertime:0
  // }

  // getBiomatrixFilter() {

  //   let workinghours2 = 0;
  //   let lessthenworkinghours = 0;
  //   let hovertime = 0;

  //   const startdateformat = this.formatDate(this.startDate);
  //   const enddateformat = this.formatDate(this.endDate);

  //   this.attendanceReconciliationService.getBiomatricData(startdateformat, enddateformat).subscribe((response: any) => {
  //       this.biomaxList = response.serviceResponse;
  //       console.log("Check ============>" , this.biomaxList);

  //       this.biomaxList.forEach((filter5) => {
  //         let workinghours: number = parseInt(filter5.totalDuration);
  //         if (workinghours !== 0) {
  //           workinghours2 += 9; // Baseline working hours
  //           if (workinghours > 9) {
  //             hovertime += (workinghours - 9); // Overtime calculation
  //           }
  //           if (workinghours < 9) {
  //             lessthenworkinghours += workinghours; // Less than working hours calculation
  //           }
  //         }
  //       });


  //       if (this.biomaxList.length > 0) {
  //         // Set the chart data
  //         this.chartdata.hovertime = hovertime;
  //         this.chartdata.workinghours = workinghours2;
  //         this.chartdata.lessthenworkinghours = lessthenworkinghours;
  //         // Update the chart with new data
  //         this.updateChartData(this.chartdata);
  //       }
  //     });
  // }

  // private updateChartData(data: any): void {
  //   this.chartOptions = {
  //     chart: {
  //       type: 'pie'
  //     },
  //     title: {
  //       text: 'Work Hours Distribution'
  //     },
  //     credits: {
  //       enabled: false
  //     },
  //     colors: ['#FF5733', '#33FF57', '#3357FF'],
  //     series: [
  //       {
  //         type: 'pie',
  //         name: 'Work Hours',
  //         data: [
  //           { name: 'Working Hours', y: data.workinghours },
  //           { name: 'Less Than Working Hours', y: data.lessthenworkinghours },
  //           { name: 'Overtime', y: data.hovertime }
  //         ]
  //       }
  //     ]
  //   };

  //   // Update chart with new options
  //   Highcharts.chart('biomaxfiterContainer', this.chartOptions);
  // }

  biomaxList: Biomax[] = [];
  isAttendanceVisible = true;
  attendanceSummary: AttendanceSummary = {
    totalEmployees: 0,
    presentToday: 0,
    lateArrivals: 0,
    underNineHours: 0,
    absentToday: 0
  };
  statusDistribution: AttendanceStatusDistribution = {
    onTime: 0,
    late: 0,
    absent: 0,
    noRecord: 0,
    total: 0
  };
  weeklyTrend: AttendanceTrendPoint[] = [];
  private dashboardChartsLoading = false;
  private dashboardFullDataRequestSeq = 0;

  onAttendanceDashboardVisibilityChange(): void {
    if (this.isAttendanceVisible) {
      this.loadAttendanceDashboardData();
      this.loadFullDatasetForCharts();
    }
  }

  private loadAttendanceDashboardData(): void {
    if (this.totalRecords <= 0 && this.attendanceReconciliationList.length === 0) {
      this.attendanceSummary = { totalEmployees: 0, presentToday: 0, lateArrivals: 0, underNineHours: 0, absentToday: 0 };
      this.statusDistribution = { onTime: 0, late: 0, absent: 0, noRecord: 0, total: 0 };
      this.weeklyTrend = [];
      this.retryRenderDashboardCharts();
      return;
    }

    // Immediate UI population from already-loaded page rows.
    if (this.attendanceReconciliationList.length > 0) {
      const localSummary = this.buildSummaryFromList(this.attendanceReconciliationList);
      this.attendanceSummary = localSummary.summary;
      this.statusDistribution = localSummary.distribution;
      this.weeklyTrend = this.buildTrendFromList(this.attendanceReconciliationList);
      this.retryRenderDashboardCharts();
    }
  }

  /** Fetch all rows in date range (capped) so trend/pie are not limited to current page. */
  private loadFullDatasetForCharts(): void {
    this.dashboardChartsLoading = true;
    const reqId = ++this.dashboardFullDataRequestSeq;
    const size = Math.min(
      this.totalRecords > 0 ? this.totalRecords : Math.max(this.tableFetchPageSize, this.attendanceReconciliationList.length || 0),
      10000
    );
    const startFmt = this.formatDate(this.startDate);
    const endFmt = this.formatDate(this.endDate);

    this.attendanceReconciliationService.getBiomatricData(startFmt, endFmt, 1, Math.max(size, 1)).subscribe({
      next: (response: any) => {
        if (reqId !== this.dashboardFullDataRequestSeq) {
          return;
        }
        const rows: any[] = response?.serviceResponse?.data || [];
        this.applyEmployeeRowFormatting(rows);
        this.fullListCache = rows;
        const totalPages = Math.max(1, Math.ceil(rows.length / Math.max(1, this.pageSize)));
        if (this.page > totalPages) {
          this.page = 1;
        }

        this.weeklyTrend = this.buildTrendFromList(rows);

        const fromRows = this.buildSummaryFromList(rows);
        this.attendanceSummary = fromRows.summary;
        this.statusDistribution = fromRows.distribution;

        this.dashboardChartsLoading = false;
        this.cdr.markForCheck();
        this.retryRenderDashboardCharts();
      },
      error: () => {
        if (reqId !== this.dashboardFullDataRequestSeq) {
          return;
        }
        // Keep non-zero view from currently loaded table page when full fetch fails.
        if (this.attendanceReconciliationList.length > 0) {
          const localSummary = this.buildSummaryFromList(this.attendanceReconciliationList);
          this.attendanceSummary = localSummary.summary;
          this.statusDistribution = localSummary.distribution;
          this.weeklyTrend = this.buildTrendFromList(this.attendanceReconciliationList);
        }
        this.dashboardChartsLoading = false;
        this.retryRenderDashboardCharts();
      }
    });
  }

  private formatTrendLabel(raw: string): string {
    if (!raw) {
      return '';
    }
    const m = moment(raw, [moment.ISO_8601, 'YYYY-MM-DD', 'DD-MM-YYYY', 'DD-MMM-YYYY'], true);
    return m.isValid() ? m.format('DD-MMM') : String(raw);
  }

  private destroyHighchart(el: HTMLElement | null): void {
    const chart = (el as any)?.__hcChart;
    if (chart && typeof chart.destroy === 'function') {
      chart.destroy();
      (el as any).__hcChart = null;
    }
  }

  /**
   * KPI totals use the reference-day slice only. Total employees = rows in that slice (matches list/export),
   * not API totalRecords (count query can disagree with the data query).
   */
  private buildSummaryFromList(records: any[]): { summary: AttendanceSummary; distribution: AttendanceStatusDistribution } {
    const dayRows = this.getReferenceDayRows(records);
    let present = 0;
    let late = 0;
    let underNine = 0;
    let explicitAbsent = 0;

    dayRows.forEach((row: any) => {
      const status = this.getAttendanceStatus(row);
      if (status === 'Absent') {
        explicitAbsent++;
      } else {
        present++;
      }
      if (status === 'Late') {
        late++;
      }
      const workedMinutes = this.getWorkedMinutes(row?.totalDuration);
      if (workedMinutes > 0 && workedMinutes < 540) {
        underNine++;
      }
    });

    const employeeTotal = dayRows.length;
    const onTime = Math.max(present - late, 0);
    const noRecord = Math.max(employeeTotal - onTime - late - explicitAbsent, 0);

    return {
      summary: {
        totalEmployees: employeeTotal,
        presentToday: present,
        lateArrivals: late,
        underNineHours: underNine,
        absentToday: explicitAbsent
      },
      distribution: {
        onTime,
        late,
        absent: explicitAbsent,
        noRecord,
        total: employeeTotal
      }
    };
  }

  private getReferenceDayRows(records: any[]): any[] {
    if (!records?.length) {
      return [];
    }

    const endMoment = moment(this.endDate, 'YYYY-MM-DD', true).isValid()
      ? moment(this.endDate, 'YYYY-MM-DD')
      : moment(this.endDate);

    const sameAsEnd = records.filter((row: any) => {
      const rowMoment = this.parseLogDate(row?.logDate);
      return rowMoment != null && rowMoment.isValid() && rowMoment.isSame(endMoment, 'day');
    });

    if (sameAsEnd.length) {
      return sameAsEnd;
    }

    // If end date has no punches, use latest available date in range.
    const sorted = [...records]
      .filter((row: any) => !!row?.logDate)
      .sort((a: any, b: any) => {
        const ma = this.parseLogDate(a.logDate);
        const mb = this.parseLogDate(b.logDate);
        const va = ma?.isValid() ? ma.valueOf() : 0;
        const vb = mb?.isValid() ? mb.valueOf() : 0;
        return vb - va;
      });

    if (!sorted.length) {
      return [];
    }

    const latest = this.parseLogDate(sorted[0].logDate);
    if (!latest?.isValid()) {
      return [];
    }

    return records.filter((row: any) => {
      const rowMoment = this.parseLogDate(row?.logDate);
      return rowMoment != null && rowMoment.isValid() && rowMoment.isSame(latest, 'day');
    });
  }

  /** Parse attendance log date from API (AttendanceDate / logDate). */
  private parseLogDate(raw: any): moment.Moment | null {
    if (raw == null || raw === '') {
      return null;
    }
    const s = String(raw).trim();
    const formats = [
      'DD-MM-YYYY',
      'DD/MM/YYYY',
      'YYYY-MM-DD',
      'DD-MMM-YYYY',
      'DD-MMM-YY',
      'MMM DD, YYYY',
      moment.ISO_8601
    ];
    for (const f of formats) {
      const m = moment(s, f, true);
      if (m.isValid()) {
        return m;
      }
    }
    const loose = moment(s, formats, false);
    return loose.isValid() ? loose : null;
  }

  private getWorkedMinutes(duration: string): number {
    if (!duration) {
      return 0;
    }
    const chunks = duration.split(':');
    if (chunks.length < 2) {
      return 0;
    }
    const hours = Number(chunks[0]);
    const minutes = Number(chunks[1]);
    if (Number.isNaN(hours) || Number.isNaN(minutes)) {
      return 0;
    }
    return (hours * 60) + minutes;
  }

  private hasMeaningfulTrend(trend: AttendanceTrendPoint[]): boolean {
    if (!trend || !trend.length) {
      return false;
    }
    return trend.some(point => (point.presentCount || 0) > 0 || (point.absentCount || 0) > 0);
  }

  private buildTrendFromList(records: any[]): AttendanceTrendPoint[] {
    if (!records || !records.length) {
      return [];
    }

    const dayMap = new Map<string, any[]>();
    records.forEach((row: any) => {
      const dateKey = row?.logDate;
      if (!dateKey) {
        return;
      }
      if (!dayMap.has(dateKey)) {
        dayMap.set(dateKey, []);
      }
      dayMap.get(dateKey)!.push(row);
    });

    const sortedEntries = Array.from(dayMap.entries()).sort((a, b) => {
      const ma = moment(a[0], ['DD-MM-YYYY', 'DD-MMM-YYYY', 'YYYY-MM-DD'], true);
      const mb = moment(b[0], ['DD-MM-YYYY', 'DD-MMM-YYYY', 'YYYY-MM-DD'], true);
      return (ma.isValid() ? ma.valueOf() : 0) - (mb.isValid() ? mb.valueOf() : 0);
    });

    return sortedEntries.map(([date, rowsForDay]) => {
      const presentCount = rowsForDay.filter((r: any) => this.getAttendanceStatus(r) !== 'Absent').length;
      const absentCount = rowsForDay.filter((r: any) => this.getAttendanceStatus(r) === 'Absent').length;
      return { date: this.formatTrendLabel(date), presentCount, absentCount };
    });
  }

  private retryRenderDashboardCharts(retryCount: number = 0): void {
    const trendContainer = document.getElementById('attendanceTrendContainer');
    const statusContainer = document.getElementById('statusDistributionContainer');
    const maxRetries = 8;

    const trendReady = !!trendContainer && trendContainer.offsetWidth > 0;
    const statusReady = !!statusContainer && statusContainer.offsetWidth > 0;

    if (trendReady && statusReady) {
      this.renderWeeklyTrendChart();
      this.renderStatusDistributionChart();
      return;
    }

    if (retryCount < maxRetries) {
      setTimeout(() => this.retryRenderDashboardCharts(retryCount + 1), 120);
      return;
    }

    // Final attempt even if width is 0, to avoid no-chart state.
    this.renderWeeklyTrendChart();
    this.renderStatusDistributionChart();
  }

  private renderWeeklyTrendChart(): void {
    const trendContainer = document.getElementById('attendanceTrendContainer');
    if (!trendContainer) {
      return;
    }

    this.destroyHighchart(trendContainer);

    // Always render as weekday view (last 7 days ending selected end date).
    const endMoment = moment(this.endDate, 'YYYY-MM-DD', true).isValid() ? moment(this.endDate, 'YYYY-MM-DD') : moment();
    const datesLast7 = Array.from({ length: 7 }, (_, i) => endMoment.clone().subtract(6 - i, 'days'));
    const categories = datesLast7.map(d => d.format('ddd'));

    const trendMap = new Map<string, AttendanceTrendPoint>();
    this.weeklyTrend.forEach(point => {
      const normalized = moment(point.date, ['DD-MMM', 'DD-MM-YYYY', 'DD-MMM-YYYY', 'YYYY-MM-DD'], true);
      if (normalized.isValid()) {
        trendMap.set(normalized.format('DD-MMM'), point);
      }
    });

    const presentSeries = datesLast7.map(d => {
      const row = trendMap.get(d.format('DD-MMM'));
      return row ? Number(row.presentCount || 0) : 0;
    });
    const absentSeries = datesLast7.map(d => {
      const row = trendMap.get(d.format('DD-MMM'));
      return row ? Number(row.absentCount || 0) : 0;
    });

    const chart = Highcharts.chart(trendContainer, {
      chart: { type: 'column', backgroundColor: 'transparent' },
      title: { text: '' },
      credits: { enabled: false },
      xAxis: {
        categories,
        crosshair: true,
        labels: { style: { color: '#64748b', fontSize: '11px' } },
        lineColor: '#cbd5e1'
      },
      yAxis: {
        min: 0,
        allowDecimals: false,
        title: { text: 'Employees', style: { color: '#64748b' } },
        labels: { style: { color: '#64748b' } },
        gridLineColor: '#e2e8f0'
      },
      legend: { align: 'center', verticalAlign: 'bottom' },
      tooltip: { shared: true },
      plotOptions: {
        column: {
          borderRadius: 6,
          pointPadding: 0.12,
          groupPadding: 0.08
        }
      },
      series: [
        { type: 'column', name: 'Present', data: presentSeries, color: '#6366f1' },
        { type: 'column', name: 'Absent', data: absentSeries, color: '#ef4444' }
      ]
    } as Highcharts.Options);

    (trendContainer as any).__hcChart = chart;
  }

  private renderStatusDistributionChart(): void {
    const statusContainer = document.getElementById('statusDistributionContainer');
    if (!statusContainer) {
      return;
    }

    this.destroyHighchart(statusContainer);

    const onTime = Number(this.statusDistribution.onTime || 0);
    const late = Number(this.statusDistribution.late || 0);
    const absent = Number(this.statusDistribution.absent || 0);
    const noRecord = Number(this.statusDistribution.noRecord || 0);

    const chart = Highcharts.chart(statusContainer, {
      chart: { type: 'pie', backgroundColor: 'transparent' },
      title: { text: '' },
      credits: { enabled: false },
      tooltip: { pointFormat: '<b>{point.y}</b> employees' },
      plotOptions: {
        pie: {
          innerSize: '65%',
          size: '85%',
          borderWidth: 0,
          dataLabels: {
            enabled: true,
            format: '{point.y}',
            style: { textOutline: 'none', fontSize: '11px', fontWeight: '600' },
            distance: 12
          },
          showInLegend: true
        }
      },
      series: [{
        type: 'pie',
        name: 'Employees',
        data: [
          { name: 'On Time', y: onTime, color: '#10b981' },
          { name: 'Late', y: late, color: '#f59e0b' },
          { name: 'Absent', y: absent, color: '#ef4444' },
          { name: 'No record', y: noRecord, color: '#94a3b8' }
        ].filter(d => d.y > 0)
      }]
    } as Highcharts.Options);

    (statusContainer as any).__hcChart = chart;
  }

  /**
   * Late = first punch strictly after 10:00:00 (same calendar day as parsed time).
   * Handles API formats like "10:13AM", "10:13 AM", "22:30", HH:mm:ss.
   */
  getAttendanceStatus(attendance: any): string {
    const rawIn = attendance?.inTime ?? attendance?.firstIn ?? attendance?.beginTime;
    if (rawIn == null || rawIn === '' || String(rawIn).trim() === '' || String(rawIn).toUpperCase() === 'NA') {
      return 'Absent';
    }

    const parsed = this.parseInTime(String(rawIn));
    if (!parsed) {
      return 'Present';
    }

    const minutes = parsed.hours() * 60 + parsed.minutes() + parsed.seconds() / 60;
    const tenAmMinutes = 10 * 60;
    return minutes > tenAmMinutes ? 'Late' : 'Present';
  }

  /** Parse in-time string to a moment (today’s date); null if invalid. */
  private parseInTime(raw: string): moment.Moment | null {
    if (raw == null || raw === '') {
      return null;
    }
    let s = String(raw).trim();
    if (!s || s.toUpperCase() === 'NA') {
      return null;
    }
    // "10:13AM" / "9:44AM" -> insert space before AM/PM for strict parsers
    s = s.replace(/^(\d{1,2}:\d{2}(?::\d{2})?)(AM|PM)$/i, '$1 $2');

    const strictFormats = [
      'hh:mm A',
      'h:mm A',
      'hh:mm:ss A',
      'HH:mm',
      'H:mm',
      'HH:mm:ss',
      'hh:mmA',
      'h:mmA'
    ];
    for (const fmt of strictFormats) {
      const m = moment(s, fmt, true);
      if (m.isValid()) {
        return m;
      }
    }
    const loose = moment(s, strictFormats, false);
    return loose.isValid() ? loose : null;
  }

  getAttendanceStatusClass(attendance: any): string {
    const status = this.getAttendanceStatus(attendance);
    if (status === 'Late') {
      return 'status-chip late';
    }
    if (status === 'Absent') {
      return 'status-chip absent';
    }
    return 'status-chip present';
  }


}
