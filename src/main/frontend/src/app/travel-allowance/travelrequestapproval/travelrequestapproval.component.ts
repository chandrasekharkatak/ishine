import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { TravelTicketModalComponent } from '../travel-ticket-modal/travel-ticket-modal.component';
import {
  approvalLevelApproverColumnTitle,
  approvalLevelCell,
  approvalLevelStatusColumnTitle,
  buildTicketFilterColumns,
  resolveTableLevelColumns,
  RmbApprovalLevelColumn
} from '../../reimbursement/rmb-approval-levels.helper';

@Component({
  standalone: false,
  selector: 'app-travelrequestapproval',
  templateUrl: './travelrequestapproval.component.html',
  styleUrls: ['./travelrequestapproval.component.css']
})
export class TravelrequestapprovalComponent implements OnInit {
  readonly approvalLevelApproverColumnTitle = approvalLevelApproverColumnTitle;
  readonly approvalLevelStatusColumnTitle = approvalLevelStatusColumnTitle;

  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;

  currentEmployeeInfo: Employee = new Employee();
  currentUser: any;
  alertMessage: any;
  modalRef: NgbModalRef | null = null;

  ticketRequests: any[] = [];
  ticketListView: 'pending' | 'all' = 'pending';
  isSearchEnabledTickets = false;
  ticketFilters: any = {};
  private readonly ticketStaticFilterColumns = [
    'ticketNo',
    'fullName',
    'displayStatus',
    'workflowStage',
    'lineCount',
    'submittedOn'
  ];
  ticketActiveColumns: string[] = [...this.ticketStaticFilterColumns];
  tableLevelColumns: RmbApprovalLevelColumn[] = [];
  readonly approvalLevelCell = approvalLevelCell;
  sortColumn = '';
  sortColumnType = '';
  sortDirection = '';
  pageTickets = 1;
  private matrixLevelColumnsFallback: RmbApprovalLevelColumn[] = [];

  constructor(
    private modalService: NgbModal,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private travelDeskService: TravelDeskService
  ) {
    this.authenticationService.currentUser.subscribe((x) => (this.currentUser = x));
  }

  ngOnInit(): void {
    void this.onGetEmployeeInfo();
    void this.loadTicketRequests();
  }

  async onGetEmployeeInfo(): Promise<void> {
    const currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus === 'Success') {
      this.currentEmployeeInfo = response.serviceResponse;
    }
  }

  async loadTicketRequests(): Promise<void> {
    const tBody = { empId: this.currentUser.empId, email: this.currentUser.email };
    const req$ =
      this.ticketListView === 'pending'
        ? this.travelDeskService.fetchTravelDeskTicketsForApproval(tBody)
        : this.travelDeskService.fetchTravelDeskTicketsAssignedAll(tBody);
    const tResp: any = await req$.pipe(first()).toPromise();
    if (tResp?.serviceStatus === 'Success') {
      this.ticketRequests = tResp.serviceResponse || [];
    } else {
      this.ticketRequests = [];
    }
    await this.refreshTableLevelColumns();
  }

  get ticketTableColspan(): number {
    return 5 + this.tableLevelColumns.length * 2 + 1 + 1;
  }

  private async refreshTableLevelColumns(): Promise<void> {
    let matrixCols: RmbApprovalLevelColumn[] = [];
    const empId = Number(this.currentUser?.empId);
    if (empId) {
      try {
        const res: any = await this.travelDeskService
          .resolveTravelApprovalMatrixForEmployee(empId)
          .pipe(first())
          .toPromise();
        if (res?.serviceStatus === 'Success' && res.serviceResponse?.levelColumns) {
          matrixCols = res.serviceResponse.levelColumns;
        }
      } catch {
        matrixCols = [];
      }
    }
    this.matrixLevelColumnsFallback = matrixCols;
    this.tableLevelColumns = resolveTableLevelColumns(this.ticketRequests, matrixCols);
    this.ticketActiveColumns = buildTicketFilterColumns(
      this.ticketStaticFilterColumns,
      this.tableLevelColumns.length
    );
  }

  async setTicketListView(view: 'pending' | 'all'): Promise<void> {
    if (this.ticketListView === view) {
      return;
    }
    this.ticketListView = view;
    this.pageTickets = 1;
    this.ticketFilters = {};
    this.isSearchEnabledTickets = false;
    await this.loadTicketRequests();
  }

  shouldShowTicketAction(t: any): boolean {
    if (!t?.workflowStage) {
      return false;
    }
    if (this.ticketListView === 'pending' && t.workflowStage === 'PENDING_LEVEL') {
      return true;
    }
    if (t.workflowStage === 'PENDING_ADMIN') {
      const uid = String(this.currentUser?.empId ?? '');
      if (t.currentAssigneeEmpId != null && uid === String(t.currentAssigneeEmpId)) {
        return true;
      }
    }
    return false;
  }

  openTicketModal(t: any, viewOnly = false): void {
    const ref = this.modalService.open(TravelTicketModalComponent, {
      size: 'xl',
      backdrop: 'static',
      windowClass: 'trv-ticket-modal'
    });
    ref.componentInstance.ticket = JSON.parse(JSON.stringify(t));
    ref.componentInstance.actor = { empId: this.currentUser?.empId, email: this.currentUser?.email };
    ref.componentInstance.viewOnly = viewOnly;
    ref.result
      .then((r: any) => {
        if (r?.refreshed) {
          void this.loadTicketRequests();
        }
      })
      .catch(() => {});
  }

  sortTicketData(sort: Sort): void {
    if (sort.active) {
      const sortParams: string[] = sort.active.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleTicketSearch(): void {
    this.isSearchEnabledTickets = !this.isSearchEnabledTickets;
    if (!this.isSearchEnabledTickets) {
      this.ticketFilters = {};
    }
  }

  onTicketSearch(searchData: any): void {
    if (this.isSearchEnabledTickets) {
      this.ticketFilters = searchData;
    }
  }

  handleTicketPageChange(event: number): void {
    this.pageTickets = event;
  }

  openAlertMod(template: TemplateRef<any>, message: any): void {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest(): void {
    this.modalRef?.close();
    void this.loadTicketRequests();
  }
}
