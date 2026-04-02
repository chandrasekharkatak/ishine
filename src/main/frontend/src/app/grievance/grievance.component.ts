import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ExportExcelService } from '../services/export-excel.service';
import { Employee } from '../models/employee';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { GrievanceService } from '../services/grievance.service';

@Component({
  standalone: false,
  selector: 'app-grievance',
  templateUrl: './grievance.component.html',
  styleUrls: ['./grievance.component.css'],
})
export class GrievanceComponent implements OnInit {
  @ViewChild('ticketViewModal') ticketViewModal: TemplateRef<any>;
  @ViewChild('closureFeedbackModal') closureFeedbackModal: TemplateRef<any>;

  private readonly MAX_PROOF_FILE_SIZE_BYTES = 5 * 1024 * 1024;
  private readonly MAX_PROOF_FILES = 5;
  private ticketViewModalRef: NgbModalRef | null = null;
  viewTicketDetail: any = null;
  viewDetailLoading = false;
  private feedbackModalRef: NgbModalRef | null = null;
  feedbackTicketForRating: any = null;
  closureFeedbackRating: number | null = null;
  closureFeedbackComments = '';
  closureFeedbackSubmitting = false;
  currentUser: User;
  /** Profile from getEmployeeByEmpId for raise-form context (phone, primary project, client, reporting manager). */
  raiseFormEmployeeProfile: any = null;
  private loadedRaiseFormProfileEmpId: any = null;
  userMapping: any = {};
  tickets: any[] = [];
  totalElements = 0;
  page = 1;
  itemsPerPage = 20;
  sortColumn: string = 'createdOn';
  sortDirection: 'asc' | 'desc' = 'desc';
  loading = false;
  exportLoading = false;
  isSubmitting = false;
  isSearchEnabled = false;
  filters: any = {};
  myTicketSearchColumns: string[] = [
    'blank',
    'displayTicketId',
    'subject',
    'category',
    'priority',
    'status',
    'assignedToName',
    'createdOnText',
    'blank',
  ];
  allTicketSearchColumns: string[] = [
    'blank',
    'displayTicketId',
    'subject',
    'category',
    'priority',
    'status',
    'createdByName',
    'assignedToName',
    'createdOnText',
    'blank',
  ];
  alertMessage = '';
  alertType: 'success' | 'danger' | 'warning' = 'success';

  grievanceForm: any = {
    subject: '',
    category: '',
    subCategory: '',
    ticketFeature: '',
    issueScenario: '',
    description: '',
    priority: 'MEDIUM',
    proofFiles: [] as File[],
  };
  categories: string[] = [];
  subCategories: string[] = [];
  loadingSubCategories = false;
  ticketFeatureOptions: string[] = [];
  loadingTicketFeatures = false;
  /** Populated for Timesheet / Leave when API returns curated labels; empty for other modules. */
  issueScenarioOptions: string[] = [];
  loadingIssueScenarios = false;
  proofFileError = '';
  activeSection: 'raise' | 'list' = 'raise';
  /** Primary list is my vs all; assigned is tickets where current user is assignee (HR / Development or RBAC). */
  listMode: 'my' | 'all' | 'assigned' = 'my';

  constructor(
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private grievanceService: GrievanceService,
    private exportExcelService: ExportExcelService,
    private router: Router,
    private modalService: NgbModal,
    private datePipe: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe((x) => {
      this.currentUser = x;
      if (x?.empId != null && x.empId !== '' && this.loadedRaiseFormProfileEmpId !== x.empId) {
        this.loadRaiseFormEmployeeProfile();
      }
    });
  }

  ngOnInit(): void {
    this.loadFeatureMapping();
    this.listMode = this.canViewAllTickets() ? 'all' : 'my';
    if (!this.canRaiseTicket()) {
      this.activeSection = 'list';
    }
    this.loadCategories();
    this.loadTickets();
  }

  loadCategories() {
    this.grievanceService
      .getCategories()
      .pipe(first())
      .subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.categories = response.serviceResponse || [];
          } else {
            this.categories = [];
          }
        },
        () => {
          this.categories = [];
        }
      );
  }

  onGrievanceCategoryChange(): void {
    this.grievanceForm.subCategory = '';
    this.grievanceForm.ticketFeature = '';
    this.grievanceForm.issueScenario = '';
    this.subCategories = [];
    this.ticketFeatureOptions = [];
    this.issueScenarioOptions = [];
    const c = (this.grievanceForm.category || '').trim();
    if (!c) {
      return;
    }
    this.loadingSubCategories = true;
    this.grievanceService
      .getSubCategories(c)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingSubCategories = false;
          if (response.serviceStatus === 'Success') {
            this.subCategories = response.serviceResponse || [];
          } else {
            this.subCategories = [];
          }
        },
        () => {
          this.loadingSubCategories = false;
          this.subCategories = [];
        }
      );
  }

  onGrievanceSubCategoryChange(): void {
    this.grievanceForm.ticketFeature = '';
    this.grievanceForm.issueScenario = '';
    this.ticketFeatureOptions = [];
    this.issueScenarioOptions = [];
    const c = (this.grievanceForm.category || '').trim();
    const sub = (this.grievanceForm.subCategory || '').trim();
    if (!c || !sub) {
      return;
    }
    this.loadingTicketFeatures = true;
    this.grievanceService
      .getTicketFeatures(c, sub)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingTicketFeatures = false;
          if (response.serviceStatus === 'Success') {
            this.ticketFeatureOptions = response.serviceResponse || [];
            if (this.ticketFeatureOptions.length === 1) {
              this.grievanceForm.ticketFeature = this.ticketFeatureOptions[0];
              this.loadIssueScenariosForSelection();
            }
          } else {
            this.ticketFeatureOptions = [];
          }
        },
        () => {
          this.loadingTicketFeatures = false;
          this.ticketFeatureOptions = [];
          this.issueScenarioOptions = [];
          this.grievanceForm.issueScenario = '';
        }
      );
  }

  onGrievanceTicketFeatureChange(): void {
    this.loadIssueScenariosForSelection();
  }

  private loadIssueScenariosForSelection(): void {
    const c = (this.grievanceForm.category || '').trim();
    const sub = (this.grievanceForm.subCategory || '').trim();
    const feat = (this.grievanceForm.ticketFeature || '').trim();
    this.grievanceForm.issueScenario = '';
    this.issueScenarioOptions = [];
    if (!c || !sub || !feat) {
      return;
    }
    this.loadingIssueScenarios = true;
    this.grievanceService
      .getIssueScenarios(c, sub, feat)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingIssueScenarios = false;
          if (response.serviceStatus === 'Success') {
            this.issueScenarioOptions = response.serviceResponse || [];
          } else {
            this.issueScenarioOptions = [];
          }
        },
        () => {
          this.loadingIssueScenarios = false;
          this.issueScenarioOptions = [];
        }
      );
  }

  showRaiseSection() {
    this.activeSection = 'raise';
  }

  showPrimaryListSection() {
    this.listMode = this.canViewAllTickets() ? 'all' : 'my';
    this.activeSection = 'list';
    this.filters = {};
    this.loadTickets();
  }

  showAssignedTicketsSection() {
    if (!this.canViewAssignedTickets()) {
      return;
    }
    this.listMode = 'assigned';
    this.activeSection = 'list';
    this.filters = {};
    this.loadTickets();
  }

  getListTitle(): string {
    if (this.listMode === 'assigned') {
      return 'Assigned Tickets';
    }
    return this.canViewAllTickets() ? 'All Grievance Tickets' : 'My Grievance Tickets';
  }

  /** Read-only display on raise form (stored on submit from session). */
  getRaisedByEmployeeIdDisplay(): string {
    const v = this.currentUser?.employeementId ?? this.currentUser?.empId;
    return v != null && String(v).trim() !== '' ? String(v) : '—';
  }

  getRaisedByNameDisplay(): string {
    const n = this.currentUser?.name;
    return n != null && String(n).trim() !== '' ? String(n).trim() : '—';
  }

  getRaisedByDepartmentDisplay(): string {
    const d = this.currentUser?.departmentName;
    return d != null && String(d).trim() !== '' ? String(d).trim() : '—';
  }

  private displayOrDash(v: any): string {
    if (v == null) {
      return '—';
    }
    const s = String(v).trim();
    return s !== '' ? s : '—';
  }

  getRaisedByPhoneDisplay(): string {
    const p = this.raiseFormEmployeeProfile;
    const mobile = p?.mobileNo ?? p?.alternateMobileNo;
    if (mobile != null && String(mobile).trim() !== '') {
      return String(mobile);
    }
    return '—';
  }

  getRaisedByProjectDisplay(): string {
    return this.displayOrDash(this.raiseFormEmployeeProfile?.defaultProjectName);
  }

  getRaisedByClientDisplay(): string {
    return this.displayOrDash(this.raiseFormEmployeeProfile?.clientName);
  }

  getRaisedByReportingManagerDisplay(): string {
    const p = this.raiseFormEmployeeProfile;
    const fromProfile = p?.reportingManagerName;
    if (fromProfile != null && String(fromProfile).trim() !== '') {
      return String(fromProfile).trim();
    }
    return this.displayOrDash(this.currentUser?.reportingManagerName ?? this.currentUser?.managerName);
  }

  private loadRaiseFormEmployeeProfile(): void {
    const id = this.currentUser?.empId;
    if (id == null || id === '') {
      return;
    }
    this.loadedRaiseFormProfileEmpId = id;
    const emp = new Employee();
    emp.empId = id;
    this.employeeService
      .getEmployeeByEmpId(emp)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response?.serviceStatus === 'Success' && response.serviceResponse) {
            this.raiseFormEmployeeProfile = response.serviceResponse;
          } else {
            this.raiseFormEmployeeProfile = null;
          }
        },
        error: () => {
          this.raiseFormEmployeeProfile = null;
        },
      });
  }

  /** Column count for empty-state row (includes Assigned To; optional Raised By). */
  getListColspan(): number {
    return 9 + (this.showRaisedByColumn() ? 1 : 0);
  }

  showRaisedByColumn(): boolean {
    return this.canViewAllTickets() || this.listMode === 'assigned';
  }

  getTicketSearchColumns(): string[] {
    if (this.listMode === 'assigned' || this.canViewAllTickets()) {
      return this.allTicketSearchColumns;
    }
    return this.myTicketSearchColumns;
  }

  loadFeatureMapping() {
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

  /** Sub-feature from DB (Assigned Tickets) or same department rules as backend (HR / Development). */
  canViewAssignedTickets(): boolean {
    if (this.userMapping.assigned_tickets === true) {
      return true;
    }
    return this.isHrOrDevelopmentDepartmentUser();
  }

  private isHrOrDevelopmentDepartmentUser(): boolean {
    const d = (this.currentUser?.departmentName || '').toLowerCase().trim();
    if (!d) {
      return false;
    }
    if (d.includes('development')) {
      return true;
    }
    if (d === 'hr' || d.includes('human resource')) {
      return true;
    }
    return false;
  }

  canRaiseTicket(): boolean {
    if (this.userMapping.raise_ticket === undefined) {
      return true;
    }
    return this.userMapping.raise_ticket === true;
  }

  loadTickets() {
    this.loading = true;
    const request$ =
      this.listMode === 'assigned'
        ? this.grievanceService.getAssignedTicketsPaged(
            this.page,
            this.itemsPerPage,
            this.sortColumn,
            this.sortDirection
          )
        : this.canViewAllTickets()
          ? this.grievanceService.getAllTicketsPaged(this.page, this.itemsPerPage, this.sortColumn, this.sortDirection)
          : this.grievanceService.getMyTicketsPaged(this.page, this.itemsPerPage, this.sortColumn, this.sortDirection);

    request$.pipe(first()).subscribe(
      (response: any) => {
        this.loading = false;
        if (response.serviceStatus === 'Success') {
          const pageData = response.serviceResponse || {};
          const content = pageData.content || [];
          this.tickets = content.map((ticket: any) => ({
            ...ticket,
            displayTicketId: ticket.ticketNumber || '',
            createdOnText: this.formatCreatedOnForFilter(ticket.createdOn),
          }));
          this.totalElements = typeof pageData.totalElements === 'number' ? pageData.totalElements : 0;
        } else {
          this.tickets = [];
          this.totalElements = 0;
          this.showAlert(response.serviceResponse || 'Unable to load tickets', 'warning');
        }
      },
      () => {
        this.loading = false;
        this.tickets = [];
        this.totalElements = 0;
        this.showAlert('Unable to load tickets', 'danger');
      }
    );
  }

  submitTicket() {
    if (!this.canRaiseTicket()) {
      this.showAlert('You do not have permission to raise grievance tickets.', 'warning');
      return;
    }
    if (!this.grievanceForm.subject?.trim() || !this.grievanceForm.description?.trim()) {
      this.showAlert('Subject and description are required.', 'warning');
      return;
    }
    if (!this.grievanceForm.category?.trim()) {
      this.showAlert('Category is required.', 'warning');
      return;
    }
    if (this.loadingSubCategories) {
      this.showAlert('Please wait for sub-categories to finish loading.', 'warning');
      return;
    }
    if (this.subCategories.length === 0) {
      this.showAlert(
        'Sub-category is required but none are configured for this category. Choose another category or contact your administrator.',
        'warning'
      );
      return;
    }
    if (!this.grievanceForm.subCategory?.trim()) {
      this.showAlert('Sub-category is required.', 'warning');
      return;
    }
    if (this.loadingTicketFeatures) {
      this.showAlert('Please wait for features to finish loading.', 'warning');
      return;
    }
    if (this.ticketFeatureOptions.length === 0) {
      this.showAlert(
        'Feature is required but none were found for this sub-category. Try another sub-category or contact your administrator.',
        'warning'
      );
      return;
    }
    if (!this.grievanceForm.ticketFeature?.trim()) {
      this.showAlert('Feature is required.', 'warning');
      return;
    }
    if (!this.grievanceForm.proofFiles?.length) {
      this.showAlert('At least one proof document is required.', 'warning');
      return;
    }
    if (this.proofFileError) {
      this.showAlert(this.proofFileError, 'warning');
      return;
    }

    this.isSubmitting = true;
    const formData = new FormData();
    formData.append('subject', this.grievanceForm.subject);
    formData.append('category', this.grievanceForm.category || '');
    formData.append('subCategory', this.grievanceForm.subCategory || '');
    formData.append('ticketFeature', this.grievanceForm.ticketFeature || '');
    formData.append('issueScenario', (this.grievanceForm.issueScenario || '').trim());
    formData.append('description', this.grievanceForm.description);
    formData.append('priority', this.grievanceForm.priority || 'MEDIUM');
    for (const f of this.grievanceForm.proofFiles as File[]) {
      formData.append('proofFiles', f, f.name);
    }
    this.grievanceService
      .createTicket(formData)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.isSubmitting = false;
          if (response.serviceStatus === 'Success') {
            const createdTicket = response.serviceResponse || {};
            const displayTicketId = createdTicket.ticketNumber || 'APMOSYS_0000';
            this.alertMessage = '';
            Swal.fire({
              icon: 'success',
              title: 'Submitted',
              text: `Grievance ticket ${displayTicketId} created successfully.`,
              confirmButtonText: 'OK',
            });
            this.grievanceForm = {
              subject: '',
              category: '',
              subCategory: '',
              ticketFeature: '',
              issueScenario: '',
              description: '',
              priority: 'MEDIUM',
              proofFiles: [],
            };
            this.subCategories = [];
            this.ticketFeatureOptions = [];
            this.issueScenarioOptions = [];
            this.proofFileError = '';
            this.page = 1;
            this.loadTickets();
          } else {
            this.showAlert(response.serviceResponse || 'Failed to create ticket', 'danger');
          }
        },
        () => {
          this.isSubmitting = false;
          this.showAlert('Failed to create ticket', 'danger');
        }
      );
  }

  private readonly proofAllowedExtensions = ['.pdf', '.png', '.jpg', '.jpeg', '.doc', '.docx'];

  onProofSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input?.files?.length) {
      return;
    }
    const incoming = Array.from(input.files);
    input.value = '';
    this.proofFileError = '';
    const current: File[] = [...(this.grievanceForm.proofFiles || [])];
    for (const f of incoming) {
      if (current.length >= this.MAX_PROOF_FILES) {
        this.proofFileError = `You can attach at most ${this.MAX_PROOF_FILES} files.`;
        break;
      }
      if (f.size > this.MAX_PROOF_FILE_SIZE_BYTES) {
        this.proofFileError = `Each file must be 5 MB or smaller (${f.name}).`;
        return;
      }
      const lower = f.name.toLowerCase();
      const extOk = this.proofAllowedExtensions.some((ext) => lower.endsWith(ext));
      if (!extOk) {
        this.proofFileError = `Unsupported file type: ${f.name}`;
        return;
      }
      const dup = current.some((x) => x.name === f.name && x.size === f.size);
      if (!dup) {
        current.push(f);
      }
    }
    this.grievanceForm.proofFiles = current;
  }

  removeProofFile(index: number) {
    const arr = [...(this.grievanceForm.proofFiles || [])];
    arr.splice(index, 1);
    this.grievanceForm.proofFiles = arr;
    this.proofFileError = '';
  }

  clearProofFiles() {
    this.grievanceForm.proofFiles = [];
    this.proofFileError = '';
  }

  openEditPage(ticket: any) {
    if (!ticket?.ticketId) {
      return;
    }
    this.router.navigate(['/grievance', ticket.ticketId, 'edit']);
  }

  sortBy(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.page = 1;
    this.loadTickets();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) {
      return 'fa fa-sort text-muted';
    }
    return this.sortDirection === 'asc' ? 'fa fa-sort-asc' : 'fa fa-sort-desc';
  }

  handlePageChange(pageNo: number) {
    this.page = pageNo;
    this.loadTickets();
  }

  onSearch(searchData: any) {
    this.filters = searchData || {};
  }

  toggleSearchFilters() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
    this.page = 1;
  }

  exportTickets() {
    this.exportLoading = true;
    const request$ =
      this.listMode === 'assigned'
        ? this.grievanceService.exportAssignedTickets()
        : this.canViewAllTickets()
          ? this.grievanceService.exportAllTickets()
          : this.grievanceService.exportMyTickets();
    request$.pipe(first()).subscribe(
      (response: any) => {
        this.exportLoading = false;
        if (response.serviceStatus !== 'Success') {
          this.showAlert(response.serviceResponse || 'Unable to export tickets.', 'danger');
          return;
        }
        const list = response.serviceResponse || [];
        const rows = list.map((ticket: any, index: number) => this.buildGrievanceTicketExportRow(ticket, index));
        const exportName =
          this.listMode === 'assigned'
            ? 'assigned-grievance-tickets.xlsx'
            : this.canViewAllTickets()
              ? 'all-grievance-tickets.xlsx'
              : 'my-grievance-tickets.xlsx';
        this.exportExcelService.exportTableDataToExcel(rows, exportName);
      },
      () => {
        this.exportLoading = false;
        this.showAlert('Unable to export tickets.', 'danger');
      }
    );
  }

  canEditTicket(ticket: any): boolean {
    if (this.canViewAllTickets()) {
      return true;
    }
    const currentEmpId = Number(this.currentUser?.empId);
    const assignedEmpId = Number(ticket?.assignedToEmpId);
    const createdByEmpId = Number(ticket?.createdByEmpId);
    if (!currentEmpId || !assignedEmpId) {
      return false;
    }
    // Edit action only for assignee, not for ticket creator.
    return currentEmpId === assignedEmpId && currentEmpId !== createdByEmpId;
  }

  canWithdrawTicket(ticket: any): boolean {
    const currentEmpId = Number(this.currentUser?.empId);
    const createdByEmpId = Number(ticket?.createdByEmpId);
    const st = (ticket?.status || '').toUpperCase();
    if (st === 'CLOSED' || st === 'WITHDRAWN') {
      return false;
    }
    return !!currentEmpId && currentEmpId === createdByEmpId;
  }

  private isTicketClosed(ticket: any): boolean {
    return (ticket?.status || '').toUpperCase() === 'CLOSED';
  }

  /** Creator self-closed with rating (cannot reopen). */
  private isClosedByCreator(ticket: any): boolean {
    return Number(ticket?.closedByCreator) === 1;
  }

  /** Creator: reopen RESOLVED, or CLOSED tickets not self-closed with feedback. */
  canReopenTicket(ticket: any): boolean {
    const currentEmpId = Number(this.currentUser?.empId);
    const createdByEmpId = Number(ticket?.createdByEmpId);
    if (!currentEmpId || currentEmpId !== createdByEmpId) {
      return false;
    }
    const st = (ticket?.status || '').toUpperCase();
    if (st === 'RESOLVED') {
      return true;
    }
    return this.isTicketClosed(ticket) && !this.isClosedByCreator(ticket);
  }

  /** Creator: feedback on Resolved (does not close) or on system-closed tickets (once). */
  canSubmitTicketFeedback(ticket: any): boolean {
    if (!this.canViewTicketSummary(ticket)) {
      return false;
    }
    const currentEmpId = Number(this.currentUser?.empId);
    const createdByEmpId = Number(ticket?.createdByEmpId);
    if (!currentEmpId || currentEmpId !== createdByEmpId || ticket?.feedbackOn) {
      return false;
    }
    const st = (ticket?.status || '').toUpperCase();
    if (st === 'RESOLVED') {
      return true;
    }
    return this.isTicketClosed(ticket) && !this.isClosedByCreator(ticket);
  }

  /** View summary: creator (own tickets), assignee, or users with view-all. */
  canViewTicketSummary(ticket: any): boolean {
    if (this.canViewAllTickets()) {
      return true;
    }
    const currentEmpId = Number(this.currentUser?.empId);
    const createdByEmpId = Number(ticket?.createdByEmpId);
    const assignedEmpId = Number(ticket?.assignedToEmpId);
    const st = (ticket?.status || '').toUpperCase();
    if (currentEmpId && assignedEmpId && currentEmpId === assignedEmpId) {
      return st !== 'WITHDRAWN';
    }
    if (!currentEmpId || currentEmpId !== createdByEmpId) {
      return false;
    }
    if (st === 'WITHDRAWN') {
      return false;
    }
    return true;
  }

  /** Modal copy: resolved vs system-closed feedback. */
  get closureFeedbackIsResolved(): boolean {
    return (this.feedbackTicketForRating?.status || '').toUpperCase() === 'RESOLVED';
  }

  openTicketView(ticket: any) {
    if (!ticket?.ticketId || !this.ticketViewModal) {
      return;
    }
    this.viewTicketDetail = null;
    this.viewDetailLoading = true;
    this.ticketViewModalRef = this.modalService.open(this.ticketViewModal, {
      modalDialogClass: 'modal-lg grievance-ticket-modal-dialog',
      scrollable: true,
      centered: true,
    });
    this.grievanceService
      .getTicketById(ticket.ticketId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.viewDetailLoading = false;
          if (response.serviceStatus === 'Success' && response.serviceResponse) {
            this.viewTicketDetail = response.serviceResponse;
          } else {
            Swal.fire({
              icon: 'warning',
              title: 'Unable to load',
              text: response.serviceResponse || 'Could not load ticket details.',
              confirmButtonText: 'OK',
            });
            this.closeTicketViewModal();
          }
        },
        () => {
          this.viewDetailLoading = false;
          this.showAlert('Unable to load ticket details.', 'danger');
          this.closeTicketViewModal();
        }
      );
  }

  closeTicketViewModal() {
    this.ticketViewModalRef?.close();
    this.ticketViewModalRef = null;
    this.viewTicketDetail = null;
  }

  reopenTicket(ticket: any) {
    if (!ticket?.ticketId) {
      return;
    }
    Swal.fire({
      icon: 'question',
      title: 'Reopen ticket?',
      text: `Reopen ${ticket.displayTicketId || ticket.ticketNumber}? Status will change to RE-OPENED.`,
      showCancelButton: true,
      confirmButtonText: 'Yes, reopen',
      cancelButtonText: 'Cancel',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }
      this.grievanceService
        .reopenTicket(ticket.ticketId)
        .pipe(first())
        .subscribe(
          (response: any) => {
            if (response.serviceStatus === 'Success') {
              Swal.fire({
                icon: 'success',
                title: 'Reopened',
                text: 'Ticket status is now RE-OPENED.',
                confirmButtonText: 'OK',
              });
              this.loadTickets();
            } else {
              this.showAlert(response.serviceResponse || 'Unable to reopen ticket.', 'danger');
            }
          },
          () => this.showAlert('Unable to reopen ticket.', 'danger')
        );
    });
  }

  openClosureFeedbackModal(ticket: any) {
    if (!ticket?.ticketId || !this.closureFeedbackModal) {
      return;
    }
    this.feedbackTicketForRating = ticket;
    this.closureFeedbackRating = null;
    this.closureFeedbackComments = '';
    this.feedbackModalRef = this.modalService.open(this.closureFeedbackModal, {
      modalDialogClass: 'modal-md grievance-ticket-modal-dialog',
      scrollable: true,
      centered: true,
    });
  }

  closeClosureFeedbackModal() {
    this.feedbackModalRef?.close();
    this.feedbackModalRef = null;
    this.feedbackTicketForRating = null;
    this.closureFeedbackRating = null;
    this.closureFeedbackComments = '';
  }

  setClosureRating(star: number) {
    this.closureFeedbackRating = star;
  }

  submitClosureFeedback() {
    const t = this.feedbackTicketForRating;
    if (!t?.ticketId || !this.closureFeedbackRating || this.closureFeedbackRating < 1 || this.closureFeedbackRating > 5) {
      Swal.fire({ icon: 'warning', title: 'Rating required', text: 'Please select 1 to 5 stars.', confirmButtonText: 'OK' });
      return;
    }
    const payload = {
      ticketId: t.ticketId,
      rating: this.closureFeedbackRating,
      comments: (this.closureFeedbackComments || '').trim(),
    };
    this.closureFeedbackSubmitting = true;
    this.grievanceService
      .submitFeedback(payload)
      .pipe(first())
      .subscribe(
      (response: any) => {
        this.closureFeedbackSubmitting = false;
        if (response.serviceStatus === 'Success') {
          const msg = 'Your feedback has been recorded.';
          Swal.fire({ icon: 'success', title: 'Thank you', text: msg, confirmButtonText: 'OK' });
          this.closeClosureFeedbackModal();
          this.loadTickets();
        } else {
          this.showAlert(response.serviceResponse || 'Unable to submit.', 'danger');
        }
      },
      () => {
        this.closureFeedbackSubmitting = false;
        this.showAlert('Unable to submit.', 'danger');
      }
    );
  }

  withdrawTicket(ticket: any) {
    Swal.fire({
      icon: 'warning',
      title: 'Withdraw Ticket',
      text: `Are you sure you want to withdraw ticket ${ticket.displayTicketId}?`,
      showCancelButton: true,
      confirmButtonText: 'Yes, withdraw',
      cancelButtonText: 'Cancel',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }
      this.grievanceService
        .withdrawTicket(ticket.ticketId)
        .pipe(first())
        .subscribe(
          (response: any) => {
            if (response.serviceStatus === 'Success') {
              Swal.fire({
                icon: 'success',
                title: 'Withdrawn',
                text: 'Ticket withdrawn successfully.',
                confirmButtonText: 'OK',
              });
              this.loadTickets();
            } else {
              this.showAlert(response.serviceResponse || 'Unable to withdraw ticket.', 'danger');
            }
          },
          () => this.showAlert('Unable to withdraw ticket.', 'danger')
        );
    });
  }

  private showAlert(message: string, type: 'success' | 'danger' | 'warning') {
    this.alertMessage = message;
    this.alertType = type;
  }

  /**
   * Excel row with full ticket fields. Omits proof_file_path and resolution_doc_path only;
   * file names (proof / resolution) are included.
   */
  private buildGrievanceTicketExportRow(ticket: any, index: number): Record<string, string | number> {
    return {
      'Sr No.': index + 1,
      'Ticket record ID': ticket.ticketId ?? '',
      'Ticket ID': ticket.ticketNumber || '',
      Status: this.getDisplayStatus(ticket.status),
      Priority: ticket.priority || '',
      Subject: ticket.subject || '',
      Description: ticket.description || '',
      Category: ticket.category || '',
      'Sub-category': ticket.subCategory || '',
      Feature: ticket.ticketFeature || '',
      'Issue scenario': ticket.issueScenario || '',
      'Raised by name': ticket.createdByName || '',
      'Raised by employee ID': ticket.createdByEmployeeId ?? '',
      'Raised by portal emp ID': ticket.createdByEmpId ?? '',
      'Department (at raise)': ticket.createdByDepartment || '',
      'Phone (at raise)': ticket.createdByPhone || '',
      'Project (at raise)': ticket.createdByProjectName || '',
      'Client (at raise)': ticket.createdByClientName || '',
      'Reporting manager (at raise)': ticket.createdByReportingManager || '',
      'Created on': this.formatExportDateTime(ticket.createdOn),
      'Assigned to': ticket.assignedToName || '',
      'Assigned to emp ID': ticket.assignedToEmpId ?? '',
      'Updated on': this.formatExportDateTime(ticket.updatedOn),
      'Updated by emp ID': ticket.updatedBy ?? '',
      'Resolution category': ticket.resolutionCategory || '',
      'Resolution date': this.formatExportDateTime(ticket.resolutionDate),
      'Resolved by': ticket.resolvedBy || '',
      'Action taken': ticket.actionTaken || '',
      'Resolution remarks': ticket.resolutionRemarks || '',
      'Proof file name (legacy)': ticket.proofFileName || '',
      'Resolution document name': ticket.resolutionDocName || '',
      'Feedback rating': ticket.feedbackRating ?? '',
      'Feedback comments': ticket.feedbackComments || '',
      'Feedback on': this.formatExportDateTime(ticket.feedbackOn),
      'Closed by creator': ticket.closedByCreator ?? '',
      Active: ticket.isActive ?? '',
    };
  }

  private formatExportDateTime(value: any): string {
    if (value == null || value === '') {
      return '';
    }
    try {
      return new Date(value).toLocaleString('en-GB');
    } catch {
      return '';
    }
  }

  getDisplayStatus(status: string): string {
    return (status || '').replaceAll('_', ' ');
  }

  private formatCreatedOnForFilter(value: any): string {
    if (!value) {
      return '';
    }
    return this.datePipe.transform(value, 'dd-MMM-yyyy hh:mm a') || '';
  }

}
