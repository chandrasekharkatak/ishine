import { Component, Input, OnChanges, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

type TicketStage = 'PENDING_HOD' | 'PENDING_HR' | 'PENDING_FINANCE' | string;

@Component({
  standalone: false,
  selector: 'app-reimbursement-ticket-modal',
  templateUrl: './reimbursement-ticket-modal.component.html',
  styleUrls: ['./reimbursement-ticket-modal.component.css']
})
export class ReimbursementTicketModalComponent implements OnChanges {
  @Input() ticket: any;
  @Input() actor: { empId: any; email: string } | null = null;

  @ViewChild('rejectReasonModal', { static: true })
  rejectReasonModalTpl!: TemplateRef<any>;
  @ViewChild('proofsModal', { static: true })
  proofsModalTpl!: TemplateRef<any>;
  @ViewChild('rejectionInfoModal', { static: true })
  rejectionInfoModalTpl!: TemplateRef<any>;

  selectedClaim: any | null = null;
  selectedClaimDocIds: number[] = [];
  selectedDocument: SafeResourceUrl | string | null = null;
  docError: string | null = null;

  rejectModalRef: NgbModalRef | null = null;
  rejectReason = '';
  rejectModalHeading = 'Rejection reason';
  /** When true, confirming rejection applies the same reason to every pending claim at this stage. */
  rejectAllMode = false;
  private pendingRejectDecision: any | null = null;

  proofsModalRef: NgbModalRef | null = null;

  rejectionInfoRef: NgbModalRef | null = null;
  rejectionInfoTitle = 'Rejection reason';
  rejectionInfoMessage = '';

  constructor(
    public activeModal: NgbActiveModal,
    private modalService: NgbModal,
    private reimbursementService: ReimbursementService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['ticket'] || !this.ticket) {
      return;
    }
    if (this.isFinanceStage) {
      const a = this.ticket._financeAction;
      if (a !== 'PAID' && a !== 'REJECTED') {
        this.ticket._financeAction = 'PAID';
      }
      if (this.ticket._financeRemarks == null) {
        this.ticket._financeRemarks = '';
      }
      if (this.ticket._financeAction !== 'REJECTED') {
        this.ticket._financeRemarks = '';
      }
    }
  }

  get stage(): TicketStage {
    return this.ticket?.workflowStage;
  }

  get isFinanceStage(): boolean {
    return this.stage === 'PENDING_FINANCE';
  }

  get isDecisionStage(): boolean {
    return this.stage === 'PENDING_HOD' || this.stage === 'PENDING_HR';
  }

  /** Claims that Finance can still pay or reject (excludes HOD/HR-rejected lines). */
  financePendingClaims(): any[] {
    if (!this.ticket?.claims?.length) return [];
    return this.ticket.claims.filter((c: any) => c?.claimStatus === 'PENDING_FINANCE');
  }

  approvedAmount(): number {
    if (!this.ticket) return 0;
    if (this.isFinanceStage) {
      let sum = 0;
      for (const c of this.financePendingClaims()) {
        const amount = Number(c?.amount);
        if (Number.isFinite(amount)) sum += amount;
      }
      return sum;
    }
    if (!this.isDecisionStage) {
      const v = Number(this.ticket?.totalClaimAmount);
      return Number.isFinite(v) ? v : 0;
    }
    const decs = this.ensureDecisions();
    const byId = new Map<number, any>();
    for (const d of decs) {
      const id = Number(d?.claimId);
      if (Number.isFinite(id)) byId.set(id, d);
    }
    let sum = 0;
    for (const c of this.ticket?.claims || []) {
      const id = Number(c?.claimId);
      const amount = Number(c?.amount);
      if (!Number.isFinite(id) || !Number.isFinite(amount)) continue;
      const d = byId.get(id);
      // Only include claims currently being decided in this stage.
      if (!d) continue;
      if (d.approved === true) sum += amount;
    }
    return sum;
  }

  rejectedCount(): number {
    if (!this.isDecisionStage) return 0;
    return this.ensureDecisions().filter((d: any) => d?.approved === false).length;
  }

  /** Build decisions array from pending claims if missing. */
  ensureDecisions(): any[] {
    if (!this.ticket) return [];
    if (Array.isArray(this.ticket._decisions) && this.ticket._decisions.length) return this.ticket._decisions;
    const pendingStatus = this.stage === 'PENDING_HOD' ? 'PENDING_HOD' : this.stage === 'PENDING_HR' ? 'PENDING_HR' : '';
    const decs = (this.ticket.claims || [])
      .filter((c: any) => c?.claimStatus === pendingStatus)
      .map((c: any) => ({
        claimId: c.claimId,
        lineNo: c.lineNo,
        expenditureType: c.expenditureType,
        projectName: c.projectName,
        amount: c.amount,
        approved: null as boolean | null,
        remarks: ''
      }));
    this.ticket._decisions = decs;
    return decs;
  }

  decisionForClaim(claimId: any): any | null {
    const id = Number(claimId);
    if (!Number.isFinite(id)) return null;
    const decs = this.ensureDecisions();
    return decs.find((d: any) => Number(d.claimId) === id) || null;
  }

  /** Count of claim lines awaiting HOD/HR decision in this modal (drives bulk actions). */
  pendingDecisionCount(): number {
    return this.ensureDecisions().length;
  }

  approveAllPending(): void {
    this.docError = null;
    for (const d of this.ensureDecisions()) {
      d.approved = true;
      d.remarks = '';
    }
  }

  openRejectAllReason(): void {
    if (this.pendingDecisionCount() <= 1) {
      return;
    }
    this.rejectAllMode = true;
    this.pendingRejectDecision = null;
    this.rejectModalHeading = 'Reject all claims';
    this.rejectReason = '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  markApprove(claim: any): void {
    const d = this.decisionForClaim(claim?.claimId);
    if (!d) return;
    d.approved = true;
    d.remarks = '';
    this.docError = null;
  }

  openRejectReason(claim: any): void {
    const d = this.decisionForClaim(claim?.claimId);
    if (!d) return;
    this.rejectAllMode = false;
    this.rejectModalHeading = 'Rejection reason';
    this.pendingRejectDecision = d;
    this.rejectReason = d.remarks || '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  confirmRejectReason(): void {
    const reason = (this.rejectReason || '').trim();
    if (!reason) {
      this.docError = 'Rejection reason is required.';
      return;
    }
    if (this.rejectAllMode) {
      for (const d of this.ensureDecisions()) {
        d.approved = false;
        d.remarks = reason;
      }
    } else {
      if (!this.pendingRejectDecision) {
        return;
      }
      this.pendingRejectDecision.approved = false;
      this.pendingRejectDecision.remarks = reason;
    }
    this.pendingRejectDecision = null;
    this.rejectAllMode = false;
    this.rejectModalHeading = 'Rejection reason';
    this.rejectReason = '';
    this.rejectModalRef?.close();
    this.rejectModalRef = null;
  }

  cancelRejectReason(): void {
    this.pendingRejectDecision = null;
    this.rejectAllMode = false;
    this.rejectModalHeading = 'Rejection reason';
    this.rejectReason = '';
    this.rejectModalRef?.dismiss();
    this.rejectModalRef = null;
  }

  isRejectedStatus(status: any): boolean {
    const s = status != null ? String(status) : '';
    return s.includes('REJECTED');
  }

  rejectionReasonForClaim(c: any): string {
    const st = c?.claimStatus != null ? String(c.claimStatus) : '';
    if (st === 'HOD_REJECTED') return (c?.hodRemarks || '').trim();
    if (st === 'HR_REJECTED') return (c?.hrRemarks || '').trim();
    if (st === 'FINANCE_REJECTED') return (this.ticket?.financeRejectReason || '').trim();
    return '';
  }

  openRejectionInfo(c: any): void {
    const st = c?.claimStatus != null ? String(c.claimStatus) : 'REJECTED';
    this.rejectionInfoTitle = `Rejection reason (${st})`;
    const msg = this.rejectionReasonForClaim(c);
    this.rejectionInfoMessage = msg || 'No remarks were provided.';
    this.rejectionInfoRef = this.modalService.open(this.rejectionInfoModalTpl, { backdrop: 'static', size: 'md' });
  }

  closeRejectionInfo(): void {
    this.rejectionInfoRef?.close();
    this.rejectionInfoRef = null;
  }

  onFinanceActionChange(action: string): void {
    if (!this.ticket) return;
    this.docError = null;
    if (action !== 'REJECTED') {
      this.ticket._financeRemarks = '';
    } else if (this.ticket._financeRemarks == null) {
      this.ticket._financeRemarks = '';
    }
  }

  private getMimeTypeFromBase64(base64: string): string {
    const header = atob(base64.slice(0, 20));
    if (header.startsWith('%PDF')) return 'application/pdf';
    if (header.startsWith('\x89PNG')) return 'image/png';
    if (header.startsWith('\xFF\xD8\xFF')) return 'image/jpeg';
    return 'application/octet-stream';
  }

  openProofsModal(claim: any): void {
    this.selectedClaim = claim || null;
    const raw = claim?.docIds || [];
    this.selectedClaimDocIds = Array.isArray(raw) ? raw.map((x: any) => Number(x)).filter((n: number) => Number.isFinite(n)) : [];
    this.selectedDocument = null;
    this.docError = null;
    this.proofsModalRef = this.modalService.open(this.proofsModalTpl, {
      size: 'xl',
      backdrop: 'static',
      windowClass: 'rmbtm-proof-modal'
    });
    if (this.selectedClaimDocIds.length > 0) void this.previewDoc(this.selectedClaimDocIds[0]);
  }

  closeProofsModal(): void {
    this.proofsModalRef?.close();
    this.proofsModalRef = null;
    this.selectedDocument = null;
    this.selectedClaim = null;
    this.selectedClaimDocIds = [];
    this.docError = null;
  }

  async previewDoc(docId: number) {
    this.selectedDocument = null;
    this.docError = null;
    try {
      const response: any = await this.reimbursementService
        .previewDocumentReimbursment({ docId })
        .pipe(first())
        .toPromise();
      if (response?.serviceStatus !== 'Success' || !response?.serviceResponse?.documentBytes) {
        this.docError = response?.serviceError || response?.serviceMessage || 'Document not available.';
        return;
      }
      const base64Data = response.serviceResponse.documentBytes;
      const mimeType = this.getMimeTypeFromBase64(base64Data);
      if (mimeType === 'application/pdf') {
        const pdfUrl = `data:application/pdf;base64,${base64Data}`;
        this.selectedDocument = this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl);
        return;
      }
      if (mimeType.startsWith('image/')) {
        this.selectedDocument = `data:${mimeType};base64,${base64Data}`;
        return;
      }
      const link = document.createElement('a');
      link.href = `data:application/octet-stream;base64,${base64Data}`;
      link.download = 'document';
      link.click();
    } catch (e: any) {
      this.docError = e?.message || 'Failed to load document.';
    }
  }

  async submitDecisions() {
    if (!this.ticket || !this.actor) return;
    const decs = this.ensureDecisions();
    for (const d of decs) {
      if (d.approved !== true && d.approved !== false) {
        this.docError = 'Please approve/reject each pending claim before submitting.';
        return;
      }
      if (d.approved === false && (!d.remarks || !String(d.remarks).trim())) {
        this.docError = 'Remarks are required for each rejected claim.';
        return;
      }
    }
    const body = {
      ticketId: this.ticket.ticketId,
      actorEmpId: this.actor.empId,
      actorEmail: this.actor.email,
      decisions: decs.map((d: any) => ({
        claimId: d.claimId,
        approved: d.approved === true,
        remarks: d.remarks || ''
      }))
    };
    this.docError = null;
    const stage = this.stage;
    const resp: any = stage === 'PENDING_HOD'
      ? await this.reimbursementService.processReimbursementTicketHod(body).pipe(first()).toPromise()
      : await this.reimbursementService.processReimbursementTicketHr(body).pipe(first()).toPromise();
    if (resp?.serviceStatus === 'Success') {
      this.activeModal.close({ refreshed: true });
      return;
    }
    this.docError = resp?.serviceError || resp?.serviceResponse || 'Update failed.';
  }

  async submitFinanceAction() {
    if (!this.ticket || !this.actor) return;
    const act = this.ticket._financeAction || 'PAID';
    const remarks = this.ticket._financeRemarks;
    if (act === 'PAID' && this.financePendingClaims().length === 0) {
      this.docError = 'No claims are pending finance; there is nothing to mark as paid.';
      return;
    }
    if (act === 'REJECTED' && (!remarks || !String(remarks).trim())) {
      this.docError = 'Please enter a rejection reason.';
      return;
    }
    const body = {
      ticketId: this.ticket.ticketId,
      actorEmpId: this.actor.empId,
      actorEmail: this.actor.email,
      action: act,
      remarks: remarks || ''
    };
    this.docError = null;
    const resp: any = await this.reimbursementService.processReimbursementTicketFinance(body).pipe(first()).toPromise();
    if (resp?.serviceStatus === 'Success') {
      this.activeModal.close({ refreshed: true });
      return;
    }
    this.docError = resp?.serviceError || resp?.serviceResponse || 'Update failed.';
  }
}

