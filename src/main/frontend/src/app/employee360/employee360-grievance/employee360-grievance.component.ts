import { AfterViewInit, Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { GrievanceService } from 'src/app/services/grievance.service';
import { EncryptionService } from 'src/app/services/EncryptionService';

@Component({
  standalone: false,
  selector: 'app-employee360-grievance',
  templateUrl: './employee360-grievance.component.html',
  styleUrls: ['./employee360-grievance.component.css'],
})
export class Employee360GrievanceComponent implements OnInit, AfterViewInit {
  sortDirection = 'desc';
  sortColumn: string | undefined;
  sortColumnType: string | undefined;
  currentUser: User | undefined;
  isSearchEnabled = false;
  filters: Record<string, unknown> = {};
  page = 1;
  items = 10;
  currentBreadcrumbList: Breadcrumb[] = [];

  targetEmpId: number | null = null;
  /** Tickets raised by the profile employee */
  raisedList: any[] = [];
  /** Tickets assigned to the profile employee */
  assignedList: any[] = [];
  raisedCount = 0;
  assignedCount = 0;
  totalElements = 0;
  /** false = raised, true = assigned */
  showAssignedView = false;
  fromDate: string | null = null;
  toDate: string | null = null;
  accessDenied = false;
  loading = false;

  raisedColumns = [
    'blank',
    'ticketNumber',
    'subject',
    'category',
    'subCategory',
    'ticketFeature',
    'status',
    'priority',
    'createdOn',
    'assignedToName',
  ];
  assignedColumns = [
    'blank',
    'ticketNumber',
    'subject',
    'category',
    'subCategory',
    'ticketFeature',
    'status',
    'priority',
    'createdOn',
    'createdByName',
  ];

  constructor(
    private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,
    private grievanceService: GrievanceService,
    private encryptionService: EncryptionService
  ) {
    this.authenticationService.currentUser.subscribe((x) => (this.currentUser = x));
    this.breadcrumbService.currentBreadcrumb.subscribe((x) => (this.currentBreadcrumbList = x));
  }

  ngOnInit(): void {
    const findIdx = this.currentBreadcrumbList.findIndex((x) => x.title === 'Grievance');
    if (findIdx >= 0) {
      this.currentBreadcrumbList.splice(findIdx + 1);
      this.removeActiveTab();
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      const breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = 'Grievance';
      breadcrumbObject.url = '/employee-360/grievance';
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

    const employeeObject = this.readEmployee360Data();
    if (!employeeObject?.empId) {
      this.accessDenied = true;
      return;
    }
    this.targetEmpId = Number(employeeObject.empId);
    this.loadCountsThenTickets();
  }

  ngAfterViewInit(): void {
    this.setActiveTab();
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  private readEmployee360Data(): any {
    const encrypted = sessionStorage.getItem('employee360Data');
    if (!encrypted) {
      return null;
    }
    const decrypted = this.encryptionService.decrypt(encrypted);
    if (!decrypted) {
      return null;
    }
    try {
      return JSON.parse(decrypted);
    } catch {
      return null;
    }
  }

  private loadCountsThenTickets(): void {
    if (this.targetEmpId == null) {
      return;
    }
    this.grievanceService.getEmployee360TicketCounts(this.targetEmpId).subscribe({
      next: (response: any) => {
        if (response?.serviceStatus === 'Success' && response.serviceResponse) {
          this.raisedCount = Number(response.serviceResponse.raisedCount ?? 0);
          this.assignedCount = Number(response.serviceResponse.assignedCount ?? 0);
          this.accessDenied = false;
          this.loadCurrentView();
        } else {
          this.accessDenied = true;
        }
      },
      error: () => {
        this.accessDenied = true;
      },
    });
  }

  loadCurrentView(): void {
    if (this.showAssignedView) {
      this.loadAssigned();
    } else {
      this.loadRaised();
    }
  }

  loadRaised(): void {
    if (this.targetEmpId == null) {
      return;
    }
    this.loading = true;
    this.grievanceService
      .getEmployee360TicketsRaised(
        this.targetEmpId,
        this.page,
        this.items,
        'createdOn',
        'desc',
        this.fromDate,
        this.toDate
      )
      .subscribe({
        next: (response: any) => {
          this.loading = false;
          if (response?.serviceStatus === 'Success' && response.serviceResponse?.content != null) {
            this.raisedList = response.serviceResponse.content;
            this.totalElements = Number(response.serviceResponse.totalElements ?? 0);
            this.accessDenied = false;
          } else if (response?.serviceStatus !== 'Success') {
            this.raisedList = [];
            this.totalElements = 0;
            this.accessDenied = true;
          }
        },
        error: () => {
          this.loading = false;
          this.raisedList = [];
          this.accessDenied = true;
        },
      });
  }

  loadAssigned(): void {
    if (this.targetEmpId == null) {
      return;
    }
    this.loading = true;
    this.grievanceService
      .getEmployee360TicketsAssigned(
        this.targetEmpId,
        this.page,
        this.items,
        'createdOn',
        'desc',
        this.fromDate,
        this.toDate
      )
      .subscribe({
        next: (response: any) => {
          this.loading = false;
          if (response?.serviceStatus === 'Success' && response.serviceResponse?.content != null) {
            this.assignedList = response.serviceResponse.content;
            this.totalElements = Number(response.serviceResponse.totalElements ?? 0);
            this.accessDenied = false;
          } else if (response?.serviceStatus !== 'Success') {
            this.assignedList = [];
            this.totalElements = 0;
            this.accessDenied = true;
          }
        },
        error: () => {
          this.loading = false;
          this.assignedList = [];
          this.accessDenied = true;
        },
      });
  }

  onDateRangeChange(): void {
    this.page = 1;
    this.loadCurrentView();
  }

  toggleAssignedView(): void {
    if (this.assignedCount < 1) {
      return;
    }
    this.showAssignedView = !this.showAssignedView;
    this.page = 1;
    this.filters = {};
    this.totalElements = 0;
    this.loadCurrentView();
  }

  get toggleButtonLabel(): string {
    return this.showAssignedView ? "View Raised Tickets" : 'Assigned Tickets';
  }

  setActiveTab(): void {
    document.querySelector('#grievance-tab .nav-link')?.classList.add('active');
  }

  removeActiveTab(): void {
    const tab = document.getElementById('Employee360Tab')?.querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }

  handlePageChange(event: number): void {
    this.page = event;
    this.loadCurrentView();
  }

  sortData(sort: Sort): void {
    if (sort.active) {
      const sortParams = sort.active.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData: Record<string, unknown>): void {
    this.filters = searchData;
  }

  /** Same pattern as Employee 360 Appreciation: export visible table to Excel. */
  exportToExcel(tableId: string): void {
    const fileName = `${tableId}.xlsx`;
    const table = document.getElementById(tableId);
    if (!table) {
      console.error('Table not found:', tableId);
      return;
    }
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Grievance');
    const excelBuffer: unknown = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const data: Blob = new Blob([excelBuffer as BlobPart], { type: 'application/octet-stream' });
    saveAs(data, fileName);
  }
}
