import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ReimbursementTicketModalComponent } from '../reimbursement-ticket-modal/reimbursement-ticket-modal.component';

@Component({
  standalone: false,
  selector: 'app-reimbursementapproval',
  templateUrl: './reimbursementapproval.component.html',
  styleUrls: ['./reimbursementapproval.component.css']
})
export class ReimbursementapprovalComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  //travelDeskInfo: MyTravelDesk;
  reimbursementInfo: MyReimbursement;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  reimbursementRequests: any = [];
  selectedReimbursementRequest: any = [];
  selectedTravelRequest: any = [];
  selectedReimbursementdataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  alertMessage: any;
  level: number;
  ticketRequests: any[] = [];
  selectedTicket: any = null;
  ticketModalRef: NgbModalRef | null = null;
  isSearchEnabledTickets = false;
  /** Pending = only tickets awaiting this actor's action; All = every ticket assigned in the workflow (any outcome). */
  ticketListView: 'pending' | 'all' = 'pending';
  ticketFilters: any = {};
  ticketActiveColumns: any[] = [
    'ticketNo',
    'fullName',
    'displayStatus',
    'workflowStage',
    'totalClaimAmount',
    'submittedOn',
    'level1ApproverName',
    'level1ApproverStatus',
    'level2ApproverName',
    'level2ApproverStatus',
    'level3ApproverName',
    'level3ApproverStatus'
  ];
  sortColumn = '';
  sortColumnType = '';
  sortDirection = '';
  pageTickets = 1;

  constructor(
    private modalService: NgbModal,
     private sanitizer: DomSanitizer,
     private employeeService : EmployeeService,
     private authenticationService: AuthenticationService,
     private travelDesk:TravelDeskService,
     private reimbursementService:ReimbursementService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.onGetReimbursementInfo();

  }

  async onGetReimbursementInfo() {
    if (this.isValidForm()) {

     this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
      reimbursementData.empId = this.currentUser.empId;

      console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);

      const response: any = await this.reimbursementService.fetchReimbursementDataforApproval(reimbursementData).toPromise();

      if (response.serviceStatus === "Success") {
        this.reimbursementRequests = response.serviceResponse.sort((a, b) => b.requestId - a.requestId);
        // this.reimbursementRequests = response.serviceResponse;
        console.log('Fetched Reimbursement Requests:', this.reimbursementRequests);
        this.selectedReimbursementRequest = this.reimbursementRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }

      await this.loadTicketRequests();
    }
  }

  async loadTicketRequests(): Promise<void> {
    const tBody = { empId: this.currentUser.empId, email: this.currentUser.email };
    const req$ = this.ticketListView === 'pending'
      ? this.reimbursementService.fetchReimbursementTicketsForApproval(tBody)
      : this.reimbursementService.fetchReimbursementTicketsAssignedAll(tBody);
    const tResp: any = await req$.toPromise();
    if (tResp.serviceStatus === 'Success') {
      this.ticketRequests = tResp.serviceResponse || [];
    } else {
      this.ticketRequests = [];
    }
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

  isValidForm() {
    return true;
  }

  shouldShowAction(row: any): boolean {
    console.log('row data :::::::::::::',row);
    if (row.level === 1) {
      return row.status === 'Pending';
    } else if (row.level === 2) {
      return row.status === 'Approved' && row.level2approverStatus === 'Pending';
    } else if (row.level === 3) {
      return (
        row.status === 'Approved' &&
        row.level2approverStatus === 'Approved' &&
        row.level3approverStatus === 'Pending'
      );
    }
    return false;
  }
  //pagination

  page = 1;
  handlePageChange(event) {
      this.page = event;
  }
  //alertMessage: any;
  modalRef:NgbModalRef;
  modalRef1:NgbModalRef;

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openAlertMod1(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
    //this.onGetReimbursementInfo();
    location.reload();
  }

  async actionRequest(template: TemplateRef<any>,template1: TemplateRef<any>) {

    if (!this.selectedReimbursementRequest.approverStatus) {
      this.openAlertMod1(template1, "Please select an Approver Status .");
      return;
    }
    this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

    let newtreimbursementData = new MyReimbursement();
    newtreimbursementData.requestId=this.selectedReimbursementRequest.requestId;
    newtreimbursementData.empId = this.selectedReimbursementRequest.empId;
    newtreimbursementData.status = this.selectedReimbursementRequest.approverStatus;
    newtreimbursementData.expenditureType = this.selectedReimbursementRequest.expenditureType;
    if(this.selectedReimbursementRequest.level == 1){
      newtreimbursementData.level1approverRemarks = this.selectedReimbursementRequest.approverRemarks;

    }else if(this.selectedReimbursementRequest.level == 2){
      newtreimbursementData.level2approverRemarks = this.selectedReimbursementRequest.approverRemarks;

    }else {
      newtreimbursementData.level3approverRemarks = this.selectedReimbursementRequest.approverRemarks;

    }


    // console.log('Approver Status ', newtravelData.approverStatus);

    // console.log('newtravelData :::::::::::::::::::::::::::::', newtravelData);


    try {
      const response: any = await this.reimbursementService.approveOrRejectReimbursement(newtreimbursementData).toPromise();

      console.log('AResponse Data :::::::::::::::::', response);

      console.log('Service Response data ::::::::',response.serviceResponse) ;

      if (response.serviceStatus === "Success") {

        if(this.selectedReimbursementRequest.approverStatus === "Rejected"){
          this.modalRef?.close();
          this.alertMessage = `Success! This request is Rejected successfully ..!!!!`;
          this.openAlertMod(template, this.alertMessage);
        }else{
          this.modalRef?.close();
        this.alertMessage = `Success! This request is approved successfully ..!!!!`;
        this.openAlertMod(template, this.alertMessage);

        console.log('Updated Travel Request:', this.selectedReimbursementRequest);

        }

      } else {
        console.error('Error updating travel request:', response.serviceResponse);
        alert('There was an issue updating the data.');
      }
      this.modalRef?.close();

    } catch (error) {
      console.error('Error during API call:', error);
      alert('An error occurred while updating the data. Please try again later.');
    }
  }

  closeModal() {
  this.modalRef?.close();
  this.onGetReimbursementInfo();
}

openEditModal(template: TemplateRef<any>, row: any) {
  this.selectedReimbursementRequest = { ...row };
  this.modalRef = this.modalService.open(template, { size: 'lg', backdrop: 'static' });
}

  shouldShowTicketAction(t: any): boolean {
    if (!t?.workflowStage) {
      return false;
    }
    if (t.workflowStage === 'PENDING_HOD') {
      const uid = String(this.currentUser?.empId ?? '');
      if (t.hodEmpId != null && t.hodEmpId !== '' && uid === String(t.hodEmpId)) {
        return true;
      }
      const hodMail = (t.hodEmail || '').trim().toLowerCase();
      const myMail = (this.currentUser?.email || '').trim().toLowerCase();
      return !!hodMail && !!myMail && hodMail === myMail;
    }
    return t.workflowStage === 'PENDING_HR' || t.workflowStage === 'PENDING_FINANCE';
  }

  openTicketModal(t: any) {
    const ref = this.modalService.open(ReimbursementTicketModalComponent, {
      size: 'xl',
      backdrop: 'static',
      windowClass: 'rmb-ticket-modal'
    });
    const copy = JSON.parse(JSON.stringify(t));
    ref.componentInstance.ticket = copy;
    ref.componentInstance.actor = { empId: this.currentUser?.empId, email: this.currentUser?.email };
    ref.result
      .then((r: any) => {
        if (r?.refreshed) {
          this.onGetReimbursementInfo();
        }
      })
      .catch(() => {});
  }

  sortTicketData(sort: Sort) {
    if (sort.active) {
      const sortParams: any[] = sort.active?.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleTicketSearch() {
    this.isSearchEnabledTickets = !this.isSearchEnabledTickets;
    if (!this.isSearchEnabledTickets) {
      this.ticketFilters = {};
    }
  }

  onTicketSearch(searchData: any) {
    if (this.isSearchEnabledTickets) {
      this.ticketFilters = searchData;
    }
  }

  handleTicketPageChange(event: any) {
    this.pageTickets = event;
  }

  openTicketActionModal(template: TemplateRef<any>, row: any) {
    this.selectedTicket = JSON.parse(JSON.stringify(row));
    const stage = row.workflowStage;
    const pending = stage === 'PENDING_HOD' ? 'PENDING_HOD'
      : stage === 'PENDING_HR' ? 'PENDING_HR' : 'PENDING_FINANCE';
    this.selectedTicket._pendingStatus = pending;
    if (pending !== 'PENDING_FINANCE') {
      this.selectedTicket._decisions = (row.claims || [])
        .filter((c: any) => c.claimStatus === pending)
        .map((c: any) => ({
          claimId: c.claimId,
          expenditureType: c.expenditureType,
          projectName: c.projectName,
          amount: c.amount,
          approved: true,
          remarks: ''
        }));
    } else {
      this.selectedTicket._financeAction = 'PAID';
      this.selectedTicket._financeRemarks = '';
    }
    this.ticketModalRef = this.modalService.open(template, { size: 'lg', backdrop: 'static' });
  }

  async submitTicketDecisions(alertTpl: TemplateRef<any>, errTpl: TemplateRef<any>) {
    if (!this.selectedTicket) {
      return;
    }
    const decs = this.selectedTicket._decisions || [];
    for (const d of decs) {
      if (d.approved === false && (!d.remarks || !String(d.remarks).trim())) {
        this.openAlertMod1(errTpl, 'Remarks are required for each rejected claim.');
        return;
      }
    }
    const body = {
      ticketId: this.selectedTicket.ticketId,
      actorEmpId: this.currentUser.empId,
      actorEmail: this.currentUser.email,
      decisions: decs.map((d: any) => ({
        claimId: d.claimId,
        approved: d.approved === true,
        remarks: d.remarks || ''
      }))
    };
    const stage = this.selectedTicket.workflowStage;
    let resp: any;
    if (stage === 'PENDING_HOD') {
      resp = await this.reimbursementService.processReimbursementTicketHod(body).pipe(first()).toPromise();
    } else {
      resp = await this.reimbursementService.processReimbursementTicketHr(body).pipe(first()).toPromise();
    }
    if (resp.serviceStatus === 'Success') {
      this.ticketModalRef?.close();
      this.openAlertMod(alertTpl, 'Ticket processed successfully.');
      await this.onGetReimbursementInfo();
    } else {
      this.openAlertMod1(errTpl, resp.serviceError || resp.serviceResponse || 'Update failed.');
    }
  }

  async submitFinanceTicketDecision(alertTpl: TemplateRef<any>, errTpl: TemplateRef<any>) {
    const act = this.selectedTicket._financeAction;
    const remarks = this.selectedTicket._financeRemarks;
    if (act === 'REJECTED' && (!remarks || !String(remarks).trim())) {
      this.openAlertMod1(errTpl, 'Remarks are required when rejecting.');
      return;
    }
    const body = {
      ticketId: this.selectedTicket.ticketId,
      actorEmpId: this.currentUser.empId,
      actorEmail: this.currentUser.email,
      action: act,
      remarks: remarks || ''
    };
    const resp: any = await this.reimbursementService.processReimbursementTicketFinance(body).pipe(first()).toPromise();
    if (resp.serviceStatus === 'Success') {
      this.ticketModalRef?.close();
      this.openAlertMod(alertTpl, 'Finance action recorded.');
      await this.onGetReimbursementInfo();
    } else {
      this.openAlertMod1(errTpl, resp.serviceError || resp.serviceResponse || 'Update failed.');
    }
  }

docUrl: string | null = null;
preview(template:TemplateRef<any>){
  const payload = { "docId": 297 };
  this.travelDesk.previewDocument(payload).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.docUrl = 'data:image/png;base64,' + response.serviceResponse.documentBytes;
    }
    else{
      this.openAlertMod(template, "Image not present");
    }
  });

}

cancelRequest2() {
  this.modalRef1.close();
}

}
