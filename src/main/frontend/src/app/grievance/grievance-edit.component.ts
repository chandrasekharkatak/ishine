import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { first, startWith } from 'rxjs/operators';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { GrievanceService } from '../services/grievance.service';

@Component({
  standalone: false,
  selector: 'app-grievance-edit',
  templateUrl: './grievance-edit.component.html',
  styleUrls: ['./grievance-edit.component.css'],
})
export class GrievanceEditComponent implements OnInit, OnDestroy {
  @ViewChild('auditHistoryModal') auditHistoryModalTpl: TemplateRef<unknown>;

  currentUser: User;
  userMapping: any = {};
  ticketId = 0;
  loading = false;
  saving = false;
  alertMessage = '';
  alertType: 'success' | 'danger' | 'warning' = 'success';
  private readonly MAX_PROOF_FILE_SIZE_BYTES = 5 * 1024 * 1024;

  statusOptions: string[] = ['OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'RE-OPENED', 'CLOSED', 'REJECTED'];
  resolutionCategoryOptions: string[] = [
    'Bug Fix',
    'Configuration Change',
    'Data Correction',
    'Access / Permission',
    'Enhancement',
    'User Guidance',
    'Duplicate / Invalid Request',
    'Infrastructure / Environment',
    'Other',
  ];
  developmentUsers: any[] = [];
  /** Filtered list for searchable assignee dropdown */
  filteredDevelopmentUsers: any[] = [];
  assigneeFilterCtrl = new FormControl<string>('', { nonNullable: false });
  private assigneeFilterSub?: Subscription;
  categories: string[] = [];
  resolutionDocument: File | null = null;
  resolutionDocumentError = '';
  feedbackSubmitting = false;
  /** Proof attachments from grievance_document (documentId 0 = legacy single proof on ticket row). */
  proofDocuments: { documentId: number; fileName: string; sortOrder?: number }[] = [];

  private auditHistoryModalRef: NgbModalRef | null = null;

  ticketForm: any = {
    ticketId: null,
    ticketNumber: '',
    subject: '',
    category: '',
    subCategory: '',
    ticketFeature: '',
    issueScenario: '',
    description: '',
    priority: 'MEDIUM',
    status: 'OPEN',
    assignedToEmpId: null,
    assignedToName: '',
    resolutionRemarks: '',
    createdByName: '',
    createdByEmpId: null,
    createdByEmployeeId: null,
    createdByDepartment: '',
    createdByPhone: '',
    createdByProjectName: '',
    createdByClientName: '',
    createdByReportingManager: '',
    createdOn: null,
    proofFileName: '',
    resolutionCategory: '',
    actionTaken: '',
    resolvedBy: '',
    resolutionDate: null,
    resolutionDocName: '',
    feedbackRating: null,
    feedbackComments: '',
    feedbackOn: null,
    closedByCreator: 0,
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private grievanceService: GrievanceService,
    private authenticationService: AuthenticationService,
    private modalService: NgbModal
  ) {
    this.authenticationService.currentUser.subscribe((x) => (this.currentUser = x));
  }

  ngOnInit(): void {
    this.loadFeatureMapping();
    this.ticketId = Number(this.route.snapshot.paramMap.get('ticketId'));
    if (!this.ticketId) {
      this.router.navigate(['/grievance']);
      return;
    }
    if (this.canViewAllTickets()) {
      this.loadDevelopmentUsers();
    }
    this.loadCategories();
    this.loadTicket();
    this.assigneeFilterSub = this.assigneeFilterCtrl.valueChanges.pipe(startWith('')).subscribe(() => {
      this.applyAssigneeFilter();
    });
  }

  ngOnDestroy(): void {
    this.assigneeFilterSub?.unsubscribe();
    this.auditHistoryModalRef?.close();
  }

  openAuditHistoryModal(): void {
    if (!this.ticketForm?.ticketId || !this.auditHistoryModalTpl) {
      return;
    }
    this.auditHistoryModalRef = this.modalService.open(this.auditHistoryModalTpl, {
      modalDialogClass: 'modal-lg grievance-ticket-modal-dialog',
      scrollable: true,
      centered: true,
    });
  }

  /** Keep selected assignee visible even when filter would hide them */
  applyAssigneeFilter(): void {
    const term = (this.assigneeFilterCtrl.value || '').toLowerCase().trim();
    const list = this.developmentUsers || [];
    let next = term ? list.filter((u: any) => (u.name || '').toLowerCase().includes(term)) : [...list];
    const selectedId = this.ticketForm?.assignedToEmpId;
    if (selectedId != null) {
      const selected = list.find((u: any) => Number(u.empId) === Number(selectedId));
      if (selected && !next.some((u: any) => Number(u.empId) === Number(selectedId))) {
        next = [selected, ...next];
      }
    }
    this.filteredDevelopmentUsers = next;
  }

  onAssigneeSelectOpened(opened: boolean): void {
    if (opened) {
      this.assigneeFilterCtrl.setValue('', { emitEvent: true });
    }
  }

  private loadFeatureMapping() {
    const grievanceFeature = this.currentUser?.userMapping?.find(
      (userMap: any) =>
        userMap?.featureName?.toLowerCase() === 'grievance' ||
        userMap?.tabName?.toLowerCase() === 'grievance'
    );

    grievanceFeature?.subFeatures?.forEach((sub: any) => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  canViewAllTickets(): boolean {
    return (
      this.userMapping.view_all_tickets === true ||
      this.currentUser?.employeeRole?.toLowerCase()?.includes('admin') ||
      this.currentUser?.employeeRole?.toLowerCase()?.includes('vp') ||
      this.currentUser?.employeeRole?.toLowerCase()?.includes('project manager')
    );
  }

  loadDevelopmentUsers() {
    this.grievanceService
      .getDevelopmentUsers()
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.developmentUsers = response.serviceResponse || [];
        }
        this.applyAssigneeFilter();
      });
  }

  loadCategories() {
    this.grievanceService
      .getCategories()
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.categories = response.serviceResponse || [];
        }
      });
  }

  loadTicket() {
    this.loading = true;
    this.grievanceService
      .getTicketById(this.ticketId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loading = false;
          if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
            this.showAlert(response.serviceResponse || 'Unable to load ticket details.', 'danger');
            return;
          }
          const t = response.serviceResponse;
          this.ticketForm = {
            ticketId: t.ticketId,
            ticketNumber: t.ticketNumber,
            subject: t.subject,
            category: t.category || '',
            subCategory: t.subCategory || '',
            ticketFeature: t.ticketFeature || '',
            issueScenario: t.issueScenario || '',
            description: t.description,
            priority: t.priority || 'MEDIUM',
            status: t.status || 'OPEN',
            assignedToEmpId: t.assignedToEmpId,
            assignedToName: t.assignedToName || '',
            resolutionRemarks: t.resolutionRemarks || '',
            createdByName: t.createdByName || '',
            createdByEmpId: t.createdByEmpId,
            createdByEmployeeId: t.createdByEmployeeId,
            createdByDepartment: t.createdByDepartment || '',
            createdByPhone: t.createdByPhone || '',
            createdByProjectName: t.createdByProjectName || '',
            createdByClientName: t.createdByClientName || '',
            createdByReportingManager: t.createdByReportingManager || '',
            createdOn: t.createdOn,
            proofFileName: t.proofFileName || '',
            resolutionCategory: t.resolutionCategory || '',
            actionTaken: t.actionTaken || '',
            resolvedBy: t.resolvedBy || '',
            resolutionDate: t.resolutionDate,
            resolutionDocName: t.resolutionDocName || '',
            feedbackRating: t.feedbackRating || null,
            feedbackComments: t.feedbackComments || '',
            feedbackOn: t.feedbackOn || null,
            closedByCreator: t.closedByCreator ?? 0,
          };
          this.applyAssigneeFilter();
          this.loadProofDocuments();
        },
        () => {
          this.loading = false;
          this.showAlert('Unable to load ticket details.', 'danger');
        }
      );
  }

  loadProofDocuments() {
    this.grievanceService
      .listTicketProofDocuments(this.ticketId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success' && Array.isArray(response.serviceResponse)) {
            this.proofDocuments = response.serviceResponse;
          } else {
            this.proofDocuments = [];
          }
        },
        () => {
          this.proofDocuments = [];
        }
      );
  }

  /** When status is Resolved, resolution category, action taken, and remarks are required (supporting document stays optional). */
  resolutionRequired(): boolean {
    return this.ticketForm?.status === 'RESOLVED';
  }

  isResolutionSectionValid(): boolean {
    if (!this.resolutionRequired()) {
      return true;
    }
    return (
      !!String(this.ticketForm.resolutionCategory || '').trim() &&
      !!String(this.ticketForm.actionTaken || '').trim() &&
      !!String(this.ticketForm.resolutionRemarks || '').trim()
    );
  }

  /** Save allowed when base fields ok and, if Resolved, resolution block is complete. */
  canSaveTicket(): boolean {
    if (this.saving || !!this.resolutionDocumentError) {
      return false;
    }
    if (!this.ticketForm?.subject?.trim() || !this.ticketForm?.description?.trim()) {
      return false;
    }
    return this.isResolutionSectionValid();
  }

  private resolutionValidationMessage(): string | null {
    if (!this.resolutionRequired()) {
      return null;
    }
    const missing: string[] = [];
    if (!String(this.ticketForm.resolutionCategory || '').trim()) {
      missing.push('Resolution category');
    }
    if (!String(this.ticketForm.actionTaken || '').trim()) {
      missing.push('Action taken');
    }
    if (!String(this.ticketForm.resolutionRemarks || '').trim()) {
      missing.push('Resolution remarks');
    }
    return missing.length ? `Cannot mark as Resolved until you complete: ${missing.join('; ')}.` : null;
  }

  saveTicket() {
    if (!this.ticketForm.subject?.trim() || !this.ticketForm.description?.trim()) {
      this.showAlert('Subject and description are required.', 'warning');
      return;
    }
    const resolutionMsg = this.resolutionValidationMessage();
    if (resolutionMsg) {
      this.showAlert(resolutionMsg, 'warning');
      return;
    }

    const payload: any = {
      ticketId: this.ticketForm.ticketId,
      subject: this.ticketForm.subject,
      category: this.ticketForm.category,
      description: this.ticketForm.description,
      priority: this.ticketForm.priority,
      status: this.ticketForm.status,
      resolutionRemarks: this.ticketForm.resolutionRemarks,
      resolutionCategory: this.ticketForm.resolutionCategory,
      actionTaken: this.ticketForm.actionTaken,
    };

    if (this.canViewAllTickets() && this.ticketForm.assignedToEmpId) {
      payload.assignedToEmpId = Number(this.ticketForm.assignedToEmpId);
      const selected = this.developmentUsers.find(
        (u: any) => Number(u.empId) === Number(payload.assignedToEmpId)
      );
      payload.assignedToName = selected?.name || this.ticketForm.assignedToName;
    }

    this.saving = true;
    this.grievanceService
      .updateTicket(payload, this.resolutionDocument)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.saving = false;
          if (response.serviceStatus === 'Success') {
            Swal.fire({
              icon: 'success',
              title: 'Updated',
              text: `Ticket ${this.ticketForm.ticketNumber} updated successfully.`,
              confirmButtonText: 'OK',
            });
            this.loadTicket();
            this.resolutionDocument = null;
            this.resolutionDocumentError = '';
            return;
          }
          this.showAlert(response.serviceResponse || 'Unable to update ticket.', 'danger');
        },
        () => {
          this.saving = false;
          this.showAlert('Unable to update ticket.', 'danger');
        }
      );
  }

  onResolutionDocumentSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input?.files || input.files.length === 0) {
      this.resolutionDocument = null;
      this.resolutionDocumentError = '';
      return;
    }
    const selectedFile = input.files[0];
    if (selectedFile.size > this.MAX_PROOF_FILE_SIZE_BYTES) {
      this.resolutionDocument = null;
      this.resolutionDocumentError = 'Resolution document must be 5MB or smaller.';
      input.value = '';
      return;
    }
    this.resolutionDocumentError = '';
    this.resolutionDocument = selectedFile;
  }

  goBack() {
    this.router.navigate(['/grievance']);
  }

  downloadProofDocumentEntry(doc: { documentId: number; fileName: string }) {
    const docId = Number(doc.documentId);
    this.grievanceService
      .downloadProofDocument(this.ticketForm.ticketId, docId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          const contentDisposition = response.headers.get('content-disposition');
          const nameMatch = /filename=\"?([^\";]+)\"?/i.exec(contentDisposition || '');
          const filename = nameMatch?.[1] || doc.fileName || this.ticketForm.proofFileName || 'proof';
          const anchor = document.createElement('a');
          anchor.href = URL.createObjectURL(response.body);
          anchor.download = filename;
          anchor.click();
          URL.revokeObjectURL(anchor.href);
        },
        () => this.showAlert('Unable to download this proof file.', 'danger')
      );
  }

  downloadResolutionDocument() {
    this.grievanceService
      .downloadResolutionDocument(this.ticketForm.ticketId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          const contentDisposition = response.headers.get('content-disposition');
          const nameMatch = /filename=\"?([^\";]+)\"?/i.exec(contentDisposition || '');
          const filename = nameMatch?.[1] || this.ticketForm.resolutionDocName || 'resolution-document';
          const anchor = document.createElement('a');
          anchor.href = URL.createObjectURL(response.body);
          anchor.download = filename;
          anchor.click();
          URL.revokeObjectURL(anchor.href);
        },
        () => this.showAlert('Unable to download resolution document.', 'danger')
      );
  }

  isCreator(): boolean {
    return Number(this.currentUser?.empId) === Number(this.ticketForm?.createdByEmpId);
  }

  hasFeedbackSubmitted(): boolean {
    return !!this.ticketForm?.feedbackOn;
  }

  showFeedbackSection(): boolean {
    if (!this.isCreator()) {
      return false;
    }
    const st = this.ticketForm?.status;
    if (!['RESOLVED', 'CLOSED'].includes(st)) {
      return false;
    }
    if (this.hasFeedbackSubmitted()) {
      return true;
    }
    if (st === 'CLOSED' && Number(this.ticketForm?.closedByCreator) === 1) {
      return false;
    }
    return true;
  }

  canSubmitFeedback(): boolean {
    if (!this.showFeedbackSection() || this.hasFeedbackSubmitted()) {
      return false;
    }
    const st = this.ticketForm?.status;
    if (st === 'CLOSED' && Number(this.ticketForm?.closedByCreator) === 1) {
      return false;
    }
    return true;
  }

  setRating(rating: number) {
    this.ticketForm.feedbackRating = rating;
  }

  submitFeedback() {
    if (this.hasFeedbackSubmitted()) {
      return;
    }
    if (!this.canSubmitFeedback()) {
      return;
    }
    if (!this.ticketForm.feedbackRating || this.ticketForm.feedbackRating < 1 || this.ticketForm.feedbackRating > 5) {
      this.showAlert('Please provide rating between 1 and 5 stars.', 'warning');
      return;
    }
    this.feedbackSubmitting = true;
    const payload = {
      ticketId: this.ticketForm.ticketId,
      rating: this.ticketForm.feedbackRating,
      comments: this.ticketForm.feedbackComments || '',
    };
    this.grievanceService
      .submitFeedback(payload)
      .pipe(first())
      .subscribe(
      (response: any) => {
        this.feedbackSubmitting = false;
        if (response.serviceStatus === 'Success') {
          Swal.fire({
            icon: 'success',
            title: 'Feedback submitted',
            text: 'Thank you for sharing your feedback.',
            confirmButtonText: 'OK',
          });
          this.loadTicket();
          return;
        }
        this.showAlert(response.serviceResponse || 'Unable to submit.', 'danger');
      },
      () => {
        this.feedbackSubmitting = false;
        this.showAlert('Unable to submit.', 'danger');
      }
    );
  }

  getStatusLabel(status: string): string {
    return (status || '').replaceAll('_', ' ');
  }

  private showAlert(message: string, type: 'success' | 'danger' | 'warning') {
    this.alertMessage = message;
    this.alertType = type;
  }
}
