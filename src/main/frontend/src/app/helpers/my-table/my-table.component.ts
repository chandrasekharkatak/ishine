
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { FilterStateService } from 'src/app/services/filter-state.service';
import { ValidationService } from 'src/app/services/validation.service';
import { environment } from 'src/environments/environment';
import { Page } from 'src/app/models/page';
import { firstValueFrom } from 'rxjs';
import { Feature } from 'src/app/models/feature';

export interface ColumnConfig {
  field: string;
  header: string;
  sortable?: boolean;
  searchable?: boolean;
}

export interface ActionConfig {
  action: string;
  icon: string;
  name: string;
}

export interface TableAction {
  type: string; // add more if needed
  row: any;
}

@Component({
  standalone: false,
  selector: 'app-my-table',
  templateUrl: './my-table.component.html',
  styleUrl: './my-table.component.css'
})

export class MyTableComponent {
  @Input() columns: ColumnConfig[] = [];
  @Input() apiUrl!: string;
  @Input() extraParams: any = {};
  @Input() rowActions: ActionConfig[] = [];
  @Input() defaultSortColumn!: string;
  @Input() isRowExpandable: boolean = false;
  @Input() isExportAllowed: boolean = false;
  @Input() isSearchAllowed: boolean = false;
  @Input() exportToExcelUrl!: string;
  @Input() subTableColumnConfig: ColumnConfig[] = [];
  @Input() subTableData: [] = [];
  @Input() defaultCellMergeColumn!: string;
  @Input() defaultSubTableCellMergeColumn!: string;
  @Input() mergeRowData: boolean = false;
  @Input() isLastColumnAction: boolean = true;
  @Input() feature = '';

  @Output() action = new EventEmitter<TableAction>();

  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
  alertMessageModalRef: NgbModalRef;

  currentUser: User;
  pageObj: Page = new Page;
  userMapping: any = {};

  private baseUrl: any = environment.baseUrl;

  page = 0;
  size = 10;
  totalRecords = 0;
  sortColumn: string = '';
  sortDirection: string = 'asc';
  expandedRow: any = null;

  filters: any = {};
  searchOnEnter: boolean = true;
  isSearchEnabled: boolean = false;

  alertMessage: any = '';

  data: any[] = [];
  searchableColumnList: string[] = [];
  searchQuery: { [key: string]: string } = {};
  hiddenChildCells: Set<string> = new Set();
  rowSpanCache = new Map<string, number>();
  childRowSpanCache = new Map<string, number>();

  constructor(
    private http: HttpClient,
    private exportExcelService: ExportExcelService,
    private readonly modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private filterStateService: FilterStateService,
    private validationService: ValidationService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }


  get noDetailsColumnCount(): number {
    let count = this.columns?.length + 1 || 0;
    if (this.isRowExpandable) count++;
    if (this.rowActions?.length > 0) count++;
    return count;
  }

  async ngOnInit() {
    this.mapSubFeatureFlag();
    this.hiddenChildCells = new Set();
    if ((this.apiUrl == undefined || this.apiUrl == null) && this.subTableData != undefined && this.subTableData != null) {
      this.data = this.subTableData;
      if (this.validationService.validateNullUndefinedEmptyStringTrim(this.defaultSubTableCellMergeColumn)) {
        this.defaultCellMergeColumn = this.defaultSubTableCellMergeColumn
      }
    }
    this.createSearchableColumnList();
    if (this.apiUrl != undefined && this.apiUrl != null) {
      await this.loadData();
    }
  }

  ngOnDestroy() {
    if (this.filterStateService.projectReportFilters) {
      this.filterStateService.projectReportFilters = {};
    }
  }
    
  private mapSubFeatureFlag() {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  createSearchableColumnList() {
    this.searchableColumnList = ["blank"];
    if (this.columns && this.columns?.length > 0) {
      this.columns.forEach(column => {
        if (!column?.searchable) {
          this.searchableColumnList.push("blank");
        } else {
          this.searchableColumnList.push(column?.field);
        }
      });
      if (this.rowActions?.length > 0) {
        this.searchableColumnList.push("blank");
      }
    }
  }

  determineUserType(): 'HOD' | 'ADMIN' | 'USER' {
    const deptName = String(this.currentUser?.departmentName || '').trim();
    const empRole = String(this.currentUser?.employeeRole || '').trim();
    const adminKeywords = ['Admin', 'Resource Management Group', 'Director', 'Super Admin', 'Accounts', 'HR'];

    const isAdmin = adminKeywords.some(keyword => deptName.includes(keyword) || empRole.includes(keyword));
    if (isAdmin) {
      return 'ADMIN';
    }
    if (empRole === 'HOD' || empRole === 'SuperAdmin' || empRole === 'Super Admin') {
      return 'HOD';
    }
    return 'USER';
  }

  async loadData() {
    if (this.apiUrl == undefined || this.apiUrl == null) {
      return;
    }
    this.data = [];
    this.totalRecords = 0;
    this.hiddenChildCells = new Set();
    this.pageObj.page = this.page || 0;
    this.pageObj.size = this.size;
    this.pageObj.sortColumn = this.sortColumn || this.defaultSortColumn;
    this.pageObj.sortDirection = this.sortDirection || 'asc';
    this.pageObj.searchFilter = this.filters;
    this.pageObj.extraFilter = this.extraParams;
    this.pageObj.currentUserEmpId = this.currentUser?.empId;
    this.pageObj.currentUserType = this.determineUserType();
    try {
      const response: any = await firstValueFrom(this.http.post<any>(this.baseUrl + this.apiUrl, this.pageObj));
      if (response != null && response?.serviceResponse != null && response?.serviceStatus === 'Success') {
        this.data = response?.serviceResponse?.content || [];
        this.data.forEach(element => {
        if (typeof element?.employeeRole === 'string') {
          element.employeeRole = element.employeeRole.replace(/^,+|,+$/g, '').trim();
        }
      });
        if (this.isRowExpandable) {
          this.isRowExpandable = false;
          this.data.forEach(element => {
            if (element?.expandedRowDetails?.length > 0) {
              this.isRowExpandable = true;
            }
          });
        } else {
          this.isRowExpandable = false;
        }
        if (this.isRowExpandable) {
          this.searchableColumnList.splice(1, 0, "blank");
        }
        this.searchableColumnList = [...this.searchableColumnList];
        this.totalRecords = response?.serviceResponse?.totalElements;
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong');
      }
    } catch (error) {
      this.openAlertMessageModal('Something went wrong!!');
    }
  }

  toggleRow(row: any) {
    this.rowSpanCache.clear();
    this.childRowSpanCache.clear();
    if (this.expandedRow === row) {
      this.expandedRow = null; // collapse
    } else {
      this.expandedRow = row; // expand
    }
  }

  isRowExpanded(row: any): boolean {
    return this.expandedRow === row;
  }

  onPageChange(event: PageEvent): void {
    this.size = event.pageSize;
    this.page = event.pageIndex;
    this.loadData();
  }

  onSort(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortDirection = sort.direction;
      this.loadData();
    }
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
      this.filterStateService.clearProjectReportFilters();
      this.loadData();
    }
  }

  onSearch(searchData: any) {
    this.page = 0;
    this.filters = searchData;
    this.filterStateService.projectReportFilters = this.filters;
    this.loadData();
  }

  triggerAction(actionType: string, row: any) {
    this.action.emit({ type: actionType, row });
  }

  openAlertMessageModal(modalMessage: any) {
    this.alertMessage = modalMessage;
    if (this.alertMessageModalRef) {
      this.closeAlertMessageModal();
    }
    this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();
    }
  }

  getRowSpan(row: any, column: any, parentIndex: any) {
    const cacheKey = `${parentIndex}_${column}`;
    if (this.rowSpanCache.has(cacheKey)) {
      return this.rowSpanCache.get(cacheKey)!;
    }

    let rowSpan = 1;
    [...this.hiddenChildCells].forEach(key => {
      if (key.startsWith(`${parentIndex}_${column}_`)) {
        this.hiddenChildCells.delete(key);
      }
    });

    if (this.mergeRowData && this.isRowExpanded(row) && row?.expandedRowDetails?.length > 0) {
      let parentColumnValue = row[column];
      if (this.validationService.validateNullUndefinedEmptyStringTrim(parentColumnValue)) {
        for (let index = 0; index < row.expandedRowDetails.length; index++) {
          const childValue = row.expandedRowDetails[index][column];
          if (this.validationService.validateNullUndefinedEmptyStringTrim(childValue)
            && this.compareParentAndChildColumnValue(parentColumnValue, childValue)) {
            rowSpan++;
            this.hiddenChildCells.add(`${parentIndex}_${column}_${index}`);
          } else {
            break;
          }
        }
      }
    }
    this.rowSpanCache.set(cacheKey, rowSpan);
    return rowSpan;
  }

  isChildCellHidden(parentIndex: any, childIndex: number, column: string): boolean {
    return this.hiddenChildCells.has(`${parentIndex}_${column}_${childIndex}`);
  }

  compareParentAndChildColumnValue(parentColumnValue: any, childColumnValue: any) {
    if ((typeof parentColumnValue === 'string') && (typeof childColumnValue === 'string')) {
      return parentColumnValue?.trim().toLowerCase() === childColumnValue?.trim().toLowerCase();
    } else {
      return childColumnValue === parentColumnValue;
    }
  }

  getChildRowSpan(parentIndex: number, row: any, column: string, childIndex: number) {
    const cacheKey = `${parentIndex}_${column}_${childIndex}`;
    if (this.childRowSpanCache.has(cacheKey)) {
      return this.childRowSpanCache.get(cacheKey)!;
    }

    let rowSpan = 1;
    if (this.mergeRowData && row?.expandedRowDetails?.length > 0 && childIndex < row.expandedRowDetails.length) {
      const currentValue = row.expandedRowDetails[childIndex][column];
      for (let next = childIndex + 1; next < row.expandedRowDetails.length; next++) {
        const nextValue = row.expandedRowDetails[next][column];
        if (this.validationService.validateNullUndefinedEmptyStringTrim(nextValue)
          && this.compareParentAndChildColumnValue(currentValue, nextValue)) {
          rowSpan++;
          this.hiddenChildCells.add(`${parentIndex}_${column}_${next}`);
        } else {
          break;
        }
      }
    }
    this.childRowSpanCache.set(cacheKey, rowSpan);
    return rowSpan;
  }

  downloadData(fullDownloadFlag:boolean) {
    if (this.apiUrl == undefined || this.apiUrl == null) {
      return;
    }
    let pageObj: Page = new Page;
    pageObj.page = 0;
    pageObj.size = fullDownloadFlag ?  100000 : this.size;
    pageObj.sortColumn = this.sortColumn || this.defaultSortColumn;
    pageObj.sortDirection = this.sortDirection || 'asc';
    pageObj.searchFilter = this.filters;
    pageObj.extraFilter = this.extraParams;
    pageObj.currentUserEmpId = this.currentUser?.empId;
    pageObj.currentUserType = this.determineUserType();

    this.http.post<any>(this.baseUrl + this.apiUrl, pageObj).subscribe(response => {
      if (response != null && response?.serviceResponse != null && response?.serviceStatus === 'Success') {
        let data = response?.serviceResponse?.content || [];
        if (!data?.length) {
          this.openAlertMessageModal(response.serviceResponse || 'No data to export');
          return;
        }

        const headers = this.columns.map(col => String(col.header ?? ''));
        const rows: any = [];
        data.forEach((row) => {
          const parentRow = this.columns.map(col => row[col.field] ?? '');
          rows.push(parentRow);
          if (this.mergeRowData && Array.isArray(row.expandedRowDetails) && row.expandedRowDetails.length > 0) {
            row.expandedRowDetails.forEach((child) => {
              const childRow = this.columns.map(col => child[col.field] ?? '');
              rows.push(childRow);
            });
          }
        });

        const worksheetData = [headers, ...rows];
        this.exportExcelService.exportGenericTableDataToExcel(worksheetData, 'Data.xlsx');
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong, Unable to download data at the moment.');
      }
    });
  }

}
