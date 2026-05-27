import { Component, Input, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import {
  isHotelTravelReasonName,
  lineTravelClassDisplay,
  lineTravelModeDisplay,
  tripTypeDisplayLabel
} from '../travel-policy.helper';

type TicketStage = 'PENDING_LEVEL' | 'PENDING_ADMIN' | 'COMPLETED' | 'REJECTED' | string;

@Component({
  standalone: false,
  selector: 'app-travel-ticket-modal',
  templateUrl: './travel-ticket-modal.component.html',
  styleUrls: ['./travel-ticket-modal.component.css']
})
export class TravelTicketModalComponent {
  /**
   * NgbModal sets {@code componentInstance.ticket = ...} imperatively; that does not reliably
   * trigger {@code ngOnChanges}. Use an {@code @Input()} setter so audit + finance defaults run
   * whenever the ticket is assigned.
   */
  private _ticket: any;

  @Input()
  set ticket(value: any) {
    this._ticket = value;
    this.onTicketBound();
  }
  get ticket(): any {
    return this._ticket;
  }

  @Input() actor: { empId: any; email: string } | null = null;

  /** Read-only inspection (no approve/reject/finance controls). */
  @Input() viewOnly = false;

  @ViewChild('rejectReasonModal', { static: true })
  rejectReasonModalTpl!: TemplateRef<any>;
  @ViewChild('proofsModal', { static: true })
  proofsModalTpl!: TemplateRef<any>;
  @ViewChild('rejectionInfoModal', { static: true })
  rejectionInfoModalTpl!: TemplateRef<any>;
  @ViewChild('auditHistoryModal', { static: true })
  auditHistoryModalTpl!: TemplateRef<any>;

  selectedClaim: any | null = null;
  selectedClaimDocIds: number[] = [];
  selectedDocument: SafeResourceUrl | string | null = null;
  docError: string | null = null;

  rejectModalRef: NgbModalRef | null = null;
  rejectReason = '';
  rejectModalHeading = 'Rejection reason';
  /** When open, describes approve vs reject and single row vs all pending lines. */
  remarksModalContext: { kind: 'approve' | 'reject'; bulk: boolean } | null = null;
  private pendingRemarksDecision: any | null = null;

  proofsModalRef: NgbModalRef | null = null;
  proofsModalKind: 'supporting' | 'booking' = 'supporting';

  rejectionInfoRef: NgbModalRef | null = null;
  rejectionInfoTitle = 'Rejection reason';
  rejectionInfoMessage = '';

  historyModalRef: NgbModalRef | null = null;

  /** Audit trail: who acted, when, and remarks (visible to all approvers). */
  ticketAuditLog: any[] = [];
  ticketAuditLoading = false;
  ticketAuditError: string | null = null;

  constructor(
    public activeModal: NgbActiveModal,
    private modalService: NgbModal,
    private travelDeskService: TravelDeskService,
    private sanitizer: DomSanitizer
  ) {}

  /** Resolve DB ticket id from view payload (modal is opened with JSON-cloned list rows). */
  private resolveTicketId(): number | null {
    const t = this._ticket;
    if (!t) {
      return null;
    }
    const raw = t.ticketId != null ? t.ticketId : t.id;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  }

  private onTicketBound(): void {
    const tid = this.resolveTicketId();
    if (false && tid != null) {
      this.loadTicketAudit(tid);
    } else {
      this.ticketAuditLog = [];
      this.ticketAuditLoading = false;
      this.ticketAuditError = null;
    }

    const t = this._ticket;
    if (!t) {
      return;
    }
    if (this.isTravelAdminStage) {
      const a = t._adminAction;
      if (a !== 'BOOKED' && a !== 'REJECTED') {
        t._adminAction = 'BOOKED';
      }
      if (t._adminRemarks == null) {
        t._adminRemarks = '';
      }
      this.ensureLineFulfillments();
    }
  }

  get stage(): TicketStage {
    return this.ticket?.workflowStage;
  }

  get isTravelAdminStage(): boolean {
    return !this.viewOnly && this.stage === 'PENDING_ADMIN';
  }

  /** @deprecated Use {@link isTravelAdminStage}. */
  get isFinanceStage(): boolean {
    return this.isTravelAdminStage;
  }

  get isDecisionStage(): boolean {
    return !this.viewOnly && this.stage === 'PENDING_LEVEL';
  }

  get showBookingAmountColumn(): boolean {
    if (this.isTravelAdminStage) {
      return true;
    }
    return !!this.ticket?.lines?.some((line: any) => line?.bookingAmount != null && Number(line.bookingAmount) > 0);
  }

  adminPendingLines(): any[] {
    if (!this.ticket?.lines?.length) return [];
    return this.ticket.lines.filter((c: any) => c?.lineStatus === 'PENDING_ADMIN');
  }

  lineStatusLabel(c: any): string {
    if (c?.lineStatusDisplay) return String(c.lineStatusDisplay);
    const st = c?.lineStatus != null ? String(c.lineStatus) : '';
    if (st === 'FULFILLED') return 'Booked';
    if (st === 'PENDING_ADMIN') return 'Pending travel admin';
    if (st === 'PENDING_APPROVAL') return 'Pending approval';
    if (st.includes('REJECTED')) return 'Rejected';
    return st || '—';
  }

  ensureLineFulfillments(): any[] {
    if (!this.ticket) return [];
    const pending = this.adminPendingLines();
    if (!Array.isArray(this.ticket._lineFulfillments)) {
      this.ticket._lineFulfillments = pending.map((c: any) => ({
        lineId: c.lineId,
        bookingReference: c._bookingReference || c.bookingReference || '',
        bookingAmount: c._bookingAmount != null ? c._bookingAmount : c.bookingAmount,
        adminProofDocIds: [...(c._adminProofDocIds || c.adminProofDocIds || [])],
        adminProofFiles: [] as { docId: number; fileName: string }[]
      }));
      return this.ticket._lineFulfillments;
    }
    const byLine = new Map<number, any>();
    for (const f of this.ticket._lineFulfillments) {
      const id = Number(f?.lineId);
      if (Number.isFinite(id)) byLine.set(id, f);
    }
    this.ticket._lineFulfillments = pending.map((c: any) => {
      const id = Number(c.lineId);
      const existing = byLine.get(id);
      if (existing) return existing;
      return {
        lineId: c.lineId,
        bookingReference: '',
        bookingAmount: null,
        adminProofDocIds: [],
        adminProofFiles: []
      };
    });
    return this.ticket._lineFulfillments;
  }

  fulfillmentForLine(lineId: any): any | null {
    const id = Number(lineId);
    if (!Number.isFinite(id)) return null;
    return this.ensureLineFulfillments().find((f: any) => Number(f.lineId) === id) || null;
  }

  bookingProofCount(c: any): number {
    const ids = c?.adminProofDocIds;
    return Array.isArray(ids) ? ids.length : 0;
  }

  /** True when every pending line has at least one booking proof uploaded. */
  canMarkAsBooked(): boolean {
    const fulfillments = this.ensureLineFulfillments();
    if (!fulfillments.length) {
      return false;
    }
    return fulfillments.every(
      (f: any) =>
        Array.isArray(f?.adminProofDocIds) &&
        f.adminProofDocIds.length > 0 &&
        f?.bookingAmount != null &&
        Number(f.bookingAmount) > 0
    );
  }

  bookingAmountLabel(amount: any): string {
    const value = Number(amount);
    if (!Number.isFinite(value) || value <= 0) {
      return '—';
    }
    return `Rs. ${value.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;
  }

  canSubmitTravelAdmin(): boolean {
    const act = this.ticket?._adminAction || 'BOOKED';
    if (act === 'REJECTED') {
      return !!String(this.ticket?._adminRemarks || '').trim();
    }
    return this.canMarkAsBooked();
  }

  rejectedCount(): number {
    if (!this.isDecisionStage) return 0;
    return this.ensureDecisions().filter((d: any) => d?.approved === false).length;
  }

  /** Build decisions array from pending lines if missing. */
  ensureDecisions(): any[] {
    if (!this.ticket) return [];
    if (Array.isArray(this.ticket._decisions) && this.ticket._decisions.length) return this.ticket._decisions;
    const pendingStatus = 'PENDING_APPROVAL';
    const decs = (this.ticket.lines || [])
      .filter((c: any) => c?.lineStatus === pendingStatus)
      .map((c: any) => ({
        lineId: c.lineId,
        lineNo: c.lineNo,
        requestType: c.requestType,
        projectName: c.projectName,
        amount: c.amount,
        approved: null as boolean | null,
        remarks: ''
      }));
    this.ticket._decisions = decs;
    return decs;
  }

  decisionForClaim(lineId: any): any | null {
    const id = Number(lineId);
    if (!Number.isFinite(id)) return null;
    const decs = this.ensureDecisions();
    return decs.find((d: any) => Number(d.lineId) === id) || null;
  }

  /** Count of lines awaiting HOD/HR decision in this modal (drives bulk actions). */
  pendingDecisionCount(): number {
    return this.ensureDecisions().length;
  }

  openApproveAllReason(): void {
    if (this.pendingDecisionCount() <= 1) {
      return;
    }
    this.remarksModalContext = { kind: 'approve', bulk: true };
    this.pendingRemarksDecision = null;
    this.rejectModalHeading = 'Approve all requests';
    this.rejectReason = '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  openRejectAllReason(): void {
    if (this.pendingDecisionCount() <= 1) {
      return;
    }
    this.remarksModalContext = { kind: 'reject', bulk: true };
    this.pendingRemarksDecision = null;
    this.rejectModalHeading = 'Reject all requests';
    this.rejectReason = '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  openApproveReason(claim: any): void {
    const d = this.decisionForClaim(claim?.lineId);
    if (!d) {
      return;
    }
    this.remarksModalContext = { kind: 'approve', bulk: false };
    this.rejectModalHeading = 'Approval comments';
    this.pendingRemarksDecision = d;
    this.rejectReason = d.approved === true ? String(d.remarks || '').trim() : '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  openRejectReason(claim: any): void {
    const d = this.decisionForClaim(claim?.lineId);
    if (!d) {
      return;
    }
    this.remarksModalContext = { kind: 'reject', bulk: false };
    this.rejectModalHeading = 'Rejection reason';
    this.pendingRemarksDecision = d;
    this.rejectReason = d.approved === false ? String(d.remarks || '').trim() : '';
    this.docError = null;
    this.rejectModalRef = this.modalService.open(this.rejectReasonModalTpl, {
      backdrop: 'static',
      size: 'md'
    });
  }

  confirmRejectReason(): void {
    const text = (this.rejectReason || '').trim();
    const ctx = this.remarksModalContext;
    if (!text) {
      this.docError = ctx?.kind === 'approve' ? 'Approval comments are required.' : 'Rejection reason is required.';
      return;
    }
    if (!ctx) {
      return;
    }
    if (ctx.bulk) {
      for (const d of this.ensureDecisions()) {
        if (ctx.kind === 'reject') {
          d.approved = false;
          d.remarks = text;
        } else {
          d.approved = true;
          d.remarks = text;
        }
      }
    } else {
      if (!this.pendingRemarksDecision) {
        return;
      }
      if (ctx.kind === 'reject') {
        this.pendingRemarksDecision.approved = false;
        this.pendingRemarksDecision.remarks = text;
      } else {
        this.pendingRemarksDecision.approved = true;
        this.pendingRemarksDecision.remarks = text;
      }
    }
    this.pendingRemarksDecision = null;
    this.remarksModalContext = null;
    this.rejectModalHeading = 'Rejection reason';
    this.rejectReason = '';
    this.rejectModalRef?.close();
    this.rejectModalRef = null;
  }

  cancelRejectReason(): void {
    this.pendingRemarksDecision = null;
    this.remarksModalContext = null;
    this.rejectModalHeading = 'Rejection reason';
    this.rejectReason = '';
    this.rejectModalRef?.dismiss();
    this.rejectModalRef = null;
  }

  isRejectedStatus(status: any): boolean {
    const s = status != null ? String(status) : '';
    return s.includes('REJECTED');
  }

  loadTicketAudit(_ticketId: number): void {
    this.ticketAuditLoading = false;
    this.ticketAuditError = null;
    this.ticketAuditLog = [];
  }

  openAuditHistoryModal(): void {
    if (this.historyModalRef) {
      return;
    }
    this.historyModalRef = this.modalService.open(this.auditHistoryModalTpl, {
      size: 'lg',
      backdrop: true,
      centered: true,
      scrollable: false,
      windowClass: 'rmbtm-history-modal',
      modalDialogClass: 'rmbtm-history-modal-dialog'
    });
    this.historyModalRef.result.then(
      () => {
        this.historyModalRef = null;
      },
      () => {
        this.historyModalRef = null;
      }
    );
  }

  closeAuditHistoryModal(): void {
    this.historyModalRef?.close();
    this.historyModalRef = null;
  }

  formatAuditAction(action: string | null | undefined): string {
    const a = (action || '').trim();
    switch (a) {
      case 'TICKET_SUBMITTED':
        return 'Ticket submitted';
      case 'HOD_CLAIM_DECISION':
        return 'HOD — claim decision';
      case 'HR_CLAIM_DECISION':
        return 'HR — claim decision';
      case 'FINANCE_PAID':
        return 'Finance — marked paid';
      case 'FINANCE_REJECTED':
        return 'Finance — rejected';
      default:
        if (!a) {
          return 'Activity';
        }
        return a
          .replace(/_/g, ' ')
          .toLowerCase()
          .replace(/\b\w/g, (ch) => ch.toUpperCase());
    }
  }

  formatAuditActor(row: any): string {
    const name = String(row?.actorDisplayName ?? '').trim();
    if (name) {
      return name;
    }
    const email = String(row?.actorEmail ?? '').trim();
    if (email) {
      return email;
    }
    const eid = row?.actorEmpId;
    if (eid != null && String(eid).trim() !== '') {
      return `Employee ID: ${eid}`;
    }
    return '—';
  }

  private travelSegmentCount(): number {
    return (this.ticket?.lines || []).filter(
      (c: any) => !isHotelTravelReasonName(c?.requestType)
    ).length;
  }

  tripTypeLabel(c: any): string {
    return tripTypeDisplayLabel(c?.tripType, c, this.travelSegmentCount());
  }

  travelModeLabel(c: any): string {
    return lineTravelModeDisplay(c);
  }

  travelClassLabel(c: any): string {
    return lineTravelClassDisplay(c);
  }

  routeLabel(c: any): string {
    if (isHotelTravelReasonName(c?.requestType) || c?.tripType === 'HOTEL' || c?.hotelCategory) {
      const city = String(c?.city || '').trim();
      if (city) {
        return city;
      }
    }
    const from = String(c?.fromLocation || '').trim();
    const to = String(c?.toLocation || '').trim();
    if (from || to) {
      return `${from || '—'} – ${to || '—'}`;
    }
    return '—';
  }

  claimLabelForAudit(lineId: any): string {
    const id = Number(lineId);
    if (!Number.isFinite(id)) {
      return '';
    }
    const c = (this.ticket?.lines || []).find((x: any) => Number(x?.lineId) === id);
    if (c?.lineNo != null) {
      return `Request ${c.lineNo}`;
    }
    return `Request #${id}`;
  }

  hasAuditRemarks(row: any): boolean {
    return String(row?.remarks ?? '').trim().length > 0;
  }

  rejectionReasonForClaim(c: any): string {
    const st = c?.lineStatus != null ? String(c.lineStatus) : '';
    if (st === 'LEVEL_REJECTED' || st === 'ADMIN_REJECTED') {
      return (c?.approverRemarks || '').trim();
    }
    if (st === 'HOD_REJECTED') return (c?.hodRemarks || '').trim();
    if (st === 'HR_REJECTED') return (c?.hrRemarks || '').trim();
    if (st === 'FINANCE_REJECTED') return (this.ticket?.financeRejectReason || '').trim();
    return (c?.approverRemarks || '').trim();
  }

  openRejectionInfo(c: any): void {
    const st = c?.lineStatus != null ? String(c.lineStatus) : 'REJECTED';
    this.rejectionInfoTitle = `Rejection reason (${st})`;
    const msg = this.rejectionReasonForClaim(c);
    this.rejectionInfoMessage = msg || 'No remarks were provided.';
    this.rejectionInfoRef = this.modalService.open(this.rejectionInfoModalTpl, { backdrop: 'static', size: 'md' });
  }

  closeRejectionInfo(): void {
    this.rejectionInfoRef?.close();
    this.rejectionInfoRef = null;
  }

  onAdminActionChange(action: string): void {
    if (!this.ticket) return;
    this.docError = null;
    if (this.ticket._adminRemarks == null) {
      this.ticket._adminRemarks = '';
    }
  }

  onFinanceActionChange(action: string): void {
    this.onAdminActionChange(action);
  }

  async onBookingProofSelected(line: any, event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input?.files?.[0];
    if (!file || !this.ticket || !this.actor) {
      return;
    }
    const fulfillment = this.fulfillmentForLine(line?.lineId);
    if (!fulfillment) {
      return;
    }
    this.docError = null;
    try {
      const fileFormData = new FormData();
      fileFormData.append('file', file);
      fileFormData.append('displayName', file.name);
      fileFormData.append('uploadedBy', String(this.actor.empId));
      const uploadResponse: any = await this.travelDeskService
        .uploadKycDocument(fileFormData)
        .pipe(first())
        .toPromise();
      if (uploadResponse?.serviceStatus !== 'Success') {
        this.docError = uploadResponse?.serviceResponse || uploadResponse?.serviceError || 'Upload failed.';
        return;
      }
      const docId = Number(uploadResponse.serviceResponse.documentId);
      if (!Number.isFinite(docId)) {
        this.docError = 'Upload did not return a document id.';
        return;
      }
      if (!Array.isArray(fulfillment.adminProofDocIds)) {
        fulfillment.adminProofDocIds = [];
      }
      fulfillment.adminProofDocIds.push(docId);
      if (!Array.isArray(fulfillment.adminProofFiles)) {
        fulfillment.adminProofFiles = [];
      }
      fulfillment.adminProofFiles.push({ docId, fileName: file.name });
      line._adminProofDocIds = fulfillment.adminProofDocIds;
    } catch (e: any) {
      this.docError = e?.message || 'Upload failed.';
    } finally {
      input.value = '';
    }
  }

  private getMimeTypeFromBase64(base64: string): string {
    const header = atob(base64.slice(0, 20));
    if (header.startsWith('%PDF')) return 'application/pdf';
    if (header.startsWith('\x89PNG')) return 'image/png';
    if (header.startsWith('\xFF\xD8\xFF')) return 'image/jpeg';
    return 'application/octet-stream';
  }

  openProofsModal(claim: any, kind: 'supporting' | 'booking' = 'supporting'): void {
    this.proofsModalKind = kind;
    this.selectedClaim = claim || null;
    const raw = kind === 'booking' ? claim?.adminProofDocIds || [] : claim?.docIds || [];
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
      const response: any = await this.travelDeskService
        .previewDocument({ docId })
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
      if (d.approved === true && (!d.remarks || !String(d.remarks).trim())) {
        this.docError = 'Approval comments are required for each approved claim.';
        return;
      }
    }
    const body = {
      ticketId: this.ticket.ticketId,
      actorEmpId: this.actor.empId,
      actorEmail: this.actor.email,
      decisions: decs.map((d: any) => ({
        lineId: d.lineId,
        approved: d.approved === true,
        remarks: d.remarks || ''
      }))
    };
    this.docError = null;
    const stage = this.stage;
    const resp: any = stage === 'PENDING_ADMIN'
      ? await this.travelDeskService.processTravelDeskTicketApproval(body).pipe(first()).toPromise()
      : await this.travelDeskService.processTravelDeskTicketApproval(body).pipe(first()).toPromise();
    if (resp?.serviceStatus === 'Success') {
      this.activeModal.close({ refreshed: true });
      return;
    }
    this.docError = resp?.serviceError || resp?.serviceResponse || 'Update failed.';
  }

  async submitTravelAdminAction() {
    if (!this.ticket || !this.actor) return;
    const act = this.ticket._adminAction || 'BOOKED';
    const remarks = this.ticket._adminRemarks;
    if (act === 'BOOKED' && this.adminPendingLines().length === 0) {
      this.docError = 'No requests are pending Travel Admin booking.';
      return;
    }
    if (act === 'REJECTED' && (!remarks || !String(remarks).trim())) {
      this.docError = 'Please enter a rejection reason.';
      return;
    }
    if (act === 'BOOKED' && !this.canMarkAsBooked()) {
      this.docError = 'Upload booking proof for each pending request before marking as booked.';
      return;
    }
    const body: any = {
      ticketId: this.ticket.ticketId,
      actorEmpId: this.actor.empId,
      actorEmail: this.actor.email,
      action: act,
      remarks: act === 'REJECTED' ? String(remarks || '').trim() : ''
    };
    if (act === 'BOOKED') {
      const fulfillments = this.ensureLineFulfillments();
      for (const f of fulfillments) {
        if (!f.adminProofDocIds?.length) {
          this.docError = 'Upload at least one booking proof for each pending request.';
          return;
        }
        if (f.bookingAmount == null || Number(f.bookingAmount) <= 0) {
          this.docError = 'Enter the booking amount for each pending request.';
          return;
        }
      }
      body.lineFulfillments = fulfillments.map((f: any) => ({
        lineId: f.lineId,
        bookingReference: String(f.bookingReference).trim(),
        bookingAmount: Number(f.bookingAmount),
        adminProofDocIds: f.adminProofDocIds
      }));
    }
    this.docError = null;
    const resp: any = await this.travelDeskService.processTravelDeskTicketApproval(body).pipe(first()).toPromise();
    if (resp?.serviceStatus === 'Success') {
      this.activeModal.close({ refreshed: true });
      return;
    }
    this.docError = resp?.serviceError || resp?.serviceResponse || 'Update failed.';
  }

  async submitFinanceAction() {
    await this.submitTravelAdminAction();
  }
}

