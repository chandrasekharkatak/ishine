import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/internal/operators/first';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

@Component({
  standalone: false,
  selector: 'app-my-reimbursement',
  templateUrl: './my-reimbursement.component.html',
  styleUrls: ['./my-reimbursement.component.css']
})
export class MyReimbursementComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  selectedReason: any;
  isTravel: boolean = false;
  alertMessage: any;
  modalRef:NgbModalRef;


  reimbursementInfo: MyReimbursement = new MyReimbursement();

  currentEmployeeInfo: Employee = new Employee();
  fromDate: string;
  toDate: string;
  currencyType: any;
  currentUser: any;
  amount: any;
  travelMode: any;
  distance: any;
  fromDateInput: any;
  toDateInput: any;
  purpose: any;
  fileInput: any;
  expenditureType: any;

expenditureTypeList:any[] = [];
foodTypeList:any[] = [];
travelModeList:any[] = [];
vehicleTypeList:any[] = [];

  reimbursementObj: any = {
    currencyType: '',
    currentUser: '',
    amount: 0,
    distance: '',
    travelMode: '',
    travelClass: '',
    expenditureType: '',
    projectId: null as number | null,
    fromDate: null,
    toDate: null,
    dateOfFood: null,
    purpose: '',
    fromLocation: '',
    toLocation: '',
    supportingDocument: null,
    kilometers: null,
    foodAllowanceType: null,
    vehicleType: '',
    othersProjectName: '',
    othersClientId: null as number | null,
    displayClientName: '',
    expenditureTypeDescription: ''
  };

  /** Projects for claim: team mapping, department-linked, optional BD "Others". */
  mappedProjectsForClaim: { projectId: number; projectName: string; clientName?: string; clientId?: number | null }[] = [];
  /** How {@link #mappedProjectsForClaim} was built (for empty-state messaging). */
  projectPickerSource: 'TEAM' | 'DEPARTMENT' | null = null;
  /** Server: employee is in Business Development — enables "Others" project option. */
  showOthersOption = false;
  /** Distinct clients for BD Others picker. */
  clientsForOthers: { clientId: number; clientName: string }[] = [];
  readonly RMB_OTHERS_PROJECT_ID = -1;



  constructor(private empService: EmployeeService,
    private authenticationService: AuthenticationService,
    private reimbursementService: ReimbursementService,
    private modalService: NgbModal,
    private sanitizer: DomSanitizer,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  /** Earliest / latest day for travel from/to dates and food date: previous calendar month only (yyyy-MM-dd). */
  claimDateMin = '';
  claimDateMax = '';
  /** From configured approval matrix for this employee. */
  approvalFlowSummary = '';
  /** When false, monthly submission window is closed (after configured day of month). */
  submissionWindowAllowed = true;
  submissionWindowMessage = '';

  ngOnInit(): void {
    this.refreshClaimDateBounds();
    void this.loadSubmissionWindowStatus();
    this.onGetEmployeeInfo();
    this.onGetExpenditureType();
    this.onGetTravelMode();
    this.onGetVehicleType();
    this.onGetFoodType();
  }

  /** While applying in month M, expense dates must fall in month M−1 only (no current month, no earlier months). */
  refreshClaimDateBounds(): void {
    const now = new Date();
    const y = now.getFullYear();
    const m = now.getMonth();
    const firstPrev = new Date(y, m - 1, 1);
    const lastPrev = new Date(y, m, 0);
    this.claimDateMin = this.formatDate(firstPrev);
    this.claimDateMax = this.formatDate(lastPrev);
    this.fromDate = this.claimDateMin;
    this.toDate = this.claimDateMax;
  }

  /** Short hint for date pickers, e.g. "April 2026 (2026-04-01 to 2026-04-30)." */
  get claimDateWindowHint(): string {
    if (!this.claimDateMin || !this.claimDateMax) {
      return '';
    }
    const start = new Date(this.claimDateMin + 'T12:00:00');
    const label = start.toLocaleString(undefined, { month: 'long', year: 'numeric' });
    return `${label} only (${this.claimDateMin} to ${this.claimDateMax}).`;
  }

  /** True when yyyy-MM-dd string lies inside [claimDateMin, claimDateMax]. */
  dateInClaimWindow(d: string | null | undefined): boolean {
    if (d == null || d === '') {
      return false;
    }
    const s = String(d).trim();
    return s >= this.claimDateMin && s <= this.claimDateMax;
  }

  onReasonSelect() {
    this.reimbursementObj.amount = 0;
    this.reimbursementObj.travelMode =null;
    this.reimbursementObj.vehicleType = null;
    console.log(this.reimbursementObj.expenditureType);
    if (this.reimbursementObj.expenditureType === 'Travel') {
      this.isTravel = true;
      console.log(this.isTravel);
    }
    else {
      this.isTravel = false;
    }
  }

  async onGetEmployeeInfo() {
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    console.log("currentUser  :::::::::: ", this.currentUser);

    const response: any = await this.empService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

      console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
    } else {
      console.error(response.serviceResponse);
    }
    // Load project picker even when profile API fails (e.g. legacy data); uses session empId.
    await this.loadMappedProjectsForReimbursement();
    await this.loadApprovalFlowSummary();
  }

  private async loadApprovalFlowSummary(): Promise<void> {
    const empId = this.currentEmployeeInfo?.empId ?? this.currentUser?.empId;
    if (empId == null) {
      return;
    }
    try {
      const res: any = await this.reimbursementService
        .resolveReimbursementApprovalMatrixForEmployee(Number(empId))
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success' && res.serviceResponse?.approvalFlowSummary) {
        this.approvalFlowSummary = res.serviceResponse.approvalFlowSummary;
      }
    } catch {
      this.approvalFlowSummary = '';
    }
  }

  async loadMappedProjectsForReimbursement() {
    this.mappedProjectsForClaim = [];
    this.projectPickerSource = null;
    this.showOthersOption = false;
    const empId = this.currentEmployeeInfo?.empId ?? this.currentUser?.empId;
    if (empId == null) {
      return;
    }
    try {
      const response: any = await this.reimbursementService
        .fetchReimbursementClaimProjectOptions({ empId })
        .pipe(first())
        .toPromise();
      if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
        return;
      }
      const bag = response.serviceResponse;
      this.projectPickerSource = bag.pickerSource === 'DEPARTMENT' ? 'DEPARTMENT' : 'TEAM';
      this.showOthersOption = !!bag.showOthersOption;
      const raw = (bag.projects || []) as any[];
      const seen = new Set<number>();
      const parsed: { projectId: number; projectName: string; clientName?: string; clientId?: number | null }[] = [];
      for (const p of raw) {
        const id = p.projectId != null ? Number(p.projectId) : NaN;
        if (!Number.isFinite(id) || seen.has(id)) {
          continue;
        }
        seen.add(id);
        const cn = p.clientName != null ? String(p.clientName).trim() : '';
        const cid = p.clientId != null && p.clientId !== '' ? Number(p.clientId) : null;
        parsed.push({
          projectId: id,
          projectName: p.projectName != null ? String(p.projectName) : `Project #${id}`,
          clientName: cn !== '' ? cn : undefined,
          clientId: Number.isFinite(cid as number) ? cid : null
        });
      }
      const othersRows = parsed.filter((r) => r.projectId === this.RMB_OTHERS_PROJECT_ID);
      const normalRows = parsed
        .filter((r) => r.projectId !== this.RMB_OTHERS_PROJECT_ID)
        .sort((a, b) => a.projectName.localeCompare(b.projectName));
      this.mappedProjectsForClaim = [...othersRows, ...normalRows];
      if (this.showOthersOption) {
        await this.loadClientsForOthers();
      } else {
        this.clientsForOthers = [];
      }
    } catch (e) {
      console.error('loadMappedProjectsForReimbursement', e);
      this.projectPickerSource = null;
      this.showOthersOption = false;
    }
  }

  async loadClientsForOthers(): Promise<void> {
    if (!this.showOthersOption) {
      this.clientsForOthers = [];
      return;
    }
    try {
      const response: any = await this.reimbursementService
        .fetchReimbursementClientsFromMaster()
        .pipe(first())
        .toPromise();
      if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
        return;
      }
      const raw = response.serviceResponse as any[];
      this.clientsForOthers = raw
        .filter((r) => r.clientId != null)
        .map((r) => ({
          clientId: Number(r.clientId),
          clientName: r.clientName != null ? String(r.clientName) : ''
        }))
        .filter((r) => Number.isFinite(r.clientId))
        .sort((a, b) => a.clientName.localeCompare(b.clientName));
    } catch (e) {
      console.error('loadClientsForOthers', e);
    }
  }

  emptyProjectPickerHint(): string {
    if (this.projectPickerSource === 'DEPARTMENT') {
      return 'No active projects are linked to your department scope. If this looks wrong, contact RMG.';
    }
    if (this.projectPickerSource === 'TEAM') {
      return 'You have no mapped projects. Contact RMG to assign a project before submitting reimbursement.';
    }
    return 'Projects could not be loaded. Refresh the page or try again later.';
  }

  clearExpenditureType(): void {
    this.reimbursementObj.expenditureType = '';
    this.reimbursementObj.expenditureTypeDescription = '';
    this.isTravel = false;
    this.reimbursementObj.travelMode = null;
    this.reimbursementObj.vehicleType = null;
  }

  onExpenditureTypeSelected(_event?: unknown): void {
    const name = this.reimbursementObj.expenditureType ? String(this.reimbursementObj.expenditureType) : '';
    const row = (this.expenditureTypeList || []).find((r: any) => String(r.expenditureTypeName) === name);
    this.reimbursementObj.expenditureTypeDescription = row?.description != null ? String(row.description) : '';
    this.onReasonSelect();
  }

  clearSelectedProject(): void {
    this.reimbursementObj.projectId = null;
    this.reimbursementObj.othersProjectName = '';
    this.reimbursementObj.othersClientId = null;
    this.reimbursementObj.displayClientName = '';
  }

  clearOthersClient(): void {
    this.reimbursementObj.othersClientId = null;
  }

  isOthersProjectSelected(): boolean {
    return Number(this.reimbursementObj.projectId) === this.RMB_OTHERS_PROJECT_ID;
  }

  showReadonlyClientForProject(): boolean {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    return Number.isFinite(id) && id !== this.RMB_OTHERS_PROJECT_ID;
  }

  onReimbursementProjectSelected(_event?: unknown): void {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    if (!Number.isFinite(id) || id === this.RMB_OTHERS_PROJECT_ID) {
      this.reimbursementObj.displayClientName = '';
      if (id === this.RMB_OTHERS_PROJECT_ID && this.showOthersOption && !this.clientsForOthers.length) {
        void this.loadClientsForOthers();
      }
      return;
    }
    const p = this.mappedProjectsForClaim.find((x) => x.projectId === id);
    this.reimbursementObj.displayClientName = p?.clientName ? String(p.clientName) : '';
  }

  private selectedProjectLabel(): string {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    if (!Number.isFinite(id)) {
      return '';
    }
    if (id === this.RMB_OTHERS_PROJECT_ID) {
      const pn = (this.reimbursementObj.othersProjectName || '').trim();
      const cid = this.reimbursementObj.othersClientId != null ? Number(this.reimbursementObj.othersClientId) : NaN;
      const cn = Number.isFinite(cid)
        ? (this.clientsForOthers.find((c) => c.clientId === cid)?.clientName || '')
        : '';
      return cn ? `${pn} (${cn})` : pn;
    }
    const p = this.mappedProjectsForClaim.find((x) => x.projectId === id);
    if (!p) {
      return '';
    }
    return p.clientName ? `${p.projectName} (${p.clientName})` : p.projectName;
  }

  onKilometersChange() {
    if (this.reimbursementObj.vehicleType === 'Car' && this.reimbursementObj.distance > 0) {
      this.reimbursementObj.amount = this.reimbursementObj.distance * 12;
    } else if (this.reimbursementObj.vehicleType === 'Bike' && this.reimbursementObj.distance > 0) {
      this.reimbursementObj.amount = this.reimbursementObj.distance * 6;

    } else {
      this.reimbursementObj.amount = 0;
    }
  }

  async resetForm(template: TemplateRef<any>) {
    this.editingClaimIndex = null;
    this.ticketClaims = [];
    this.reimbursementObj = {
      currencyType: '',
      currentUser: '',
      amount: 0,
      distance: '',
      travelMode: '',
      travelClass: '',
      expenditureType: '',
      projectId: null,
      fromDate: null,
      toDate: null,
      dateOfFood: null,
      purpose: '',
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      vehicleType: '',
      file: '',
      othersProjectName: '',
      othersClientId: null,
      displayClientName: '',
      expenditureTypeDescription: ''
    };
    const fileInput: HTMLInputElement | null = document.querySelector('input[type="file"]');
    if (fileInput) {
      fileInput.value = ''; // Clear the file input value
    }
    this.alertMessage = `Your form data has been successfully reset  !!!!!!`;
    this.openAlertMod(template, this.alertMessage);
  }

  ticketClaims: any[] = [];

  /** Index of draft claim loaded into the form for editing; `null` when adding a new claim. */
  editingClaimIndex: number | null = null;

  ticketTotalAmount(): number {
    return this.ticketClaims.reduce((s, c) => s + (Number(c.amount) || 0), 0);
  }

  removeTicketClaim(index: number) {
    if (this.editingClaimIndex === index) {
      this.cancelEditDraftClaim();
    } else if (this.editingClaimIndex !== null && index < this.editingClaimIndex) {
      this.editingClaimIndex--;
    }
    this.ticketClaims.splice(index, 1);
  }

  beginEditDraftClaim(index: number) {
    const c = this.ticketClaims[index];
    if (!c) {
      return;
    }
    this.editingClaimIndex = index;
    this.draftDocPreview = null;
    this.reimbursementObj.expenditureType = c.expenditureType || '';
    // onReasonSelect() resets amount and travel fields — re-apply claim values after it.
    this.onReasonSelect();
    this.reimbursementObj.expenditureTypeDescription = c.expenditureTypeDescription || '';
    this.reimbursementObj.amount = c.amount != null ? Number(c.amount) : 0;
    this.reimbursementObj.travelMode = c.travelMode || '';
    this.reimbursementObj.distance = c.distance != null ? c.distance : '';
    this.reimbursementObj.vehicleType = c.vehicleType || '';
    this.reimbursementObj.foodAllowanceType = c.foodAllowanceType || null;
    this.reimbursementObj.dateOfFood = c.dateOfFood || null;
    this.reimbursementObj.fromDate = c.fromDate || null;
    this.reimbursementObj.toDate = c.toDate || null;
    this.reimbursementObj.purpose = c.purpose || '';
    this.reimbursementObj.projectId = c.projectId != null ? Number(c.projectId) : null;
    if (Number(this.reimbursementObj.projectId) === this.RMB_OTHERS_PROJECT_ID) {
      this.reimbursementObj.othersProjectName = (c.othersProjectName || '').trim();
      this.reimbursementObj.othersClientId = c.othersClientId != null ? Number(c.othersClientId) : null;
      this.reimbursementObj.displayClientName = '';
    } else {
      this.reimbursementObj.othersProjectName = '';
      this.reimbursementObj.othersClientId = null;
      this.reimbursementObj.displayClientName = (c.clientName || '').trim();
      this.onReimbursementProjectSelected();
    }
    if (
      this.reimbursementObj.expenditureType === 'Travel' &&
      this.reimbursementObj.travelMode === 'Personal Vehicle' &&
      this.reimbursementObj.vehicleType &&
      this.reimbursementObj.distance
    ) {
      this.onKilometersChange();
    }
    this.invalidFromDate = false;
    this.invalidToDate = false;
    this.fileUploads = [{}];
    this.ensureClaimDocumentsList(c);
  }

  cancelEditDraftClaim() {
    this.editingClaimIndex = null;
    this.draftDocPreview = null;
    this.resetAfterSubmit();
    this.fileUploads = [{}];
    this.invalidFromDate = false;
    this.invalidToDate = false;
  }

  /** Modal: documents attached to a draft claim (before ticket submit). */
  draftDocsModalRef: NgbModalRef | null = null;
  draftDocsClaimIndex: number | null = null;
  draftDocPreview: SafeResourceUrl | string | null = null;

  ensureClaimDocumentsList(claim: any): { docId: number; fileName: string }[] {
    if (claim.documents?.length) {
      return claim.documents;
    }
    if (claim.docIds?.length) {
      claim.documents = claim.docIds.map((id: number) => ({ docId: id, fileName: 'Document #' + id }));
      return claim.documents;
    }
    claim.documents = [];
    return claim.documents;
  }

  openDraftDocumentsModal(template: TemplateRef<any>, claimIndex: number) {
    this.draftDocsClaimIndex = claimIndex;
    this.draftDocPreview = null;
    const c = this.ticketClaims[claimIndex];
    if (c) {
      this.ensureClaimDocumentsList(c);
    }
    this.draftDocsModalRef = this.modalService.open(template, {
      size: 'lg',
      backdrop: 'static',
      windowClass: 'rmb-docs-modal-window'
    });
  }

  closeDraftDocumentsModal() {
    this.draftDocsModalRef?.close();
    this.draftDocsModalRef = null;
    this.draftDocsClaimIndex = null;
    this.draftDocPreview = null;
  }

  getMimeTypeFromBase64(base64: string): string {
    const header = atob(base64.slice(0, 20));
    if (header.startsWith('%PDF')) {
      return 'application/pdf';
    }
    if (header.startsWith('\x89PNG')) {
      return 'image/png';
    }
    if (header.startsWith('\xFF\xD8\xFF')) {
      return 'image/jpeg';
    }
    return 'application/octet-stream';
  }

  async previewDraftDocument(docId: number) {
    this.draftDocPreview = null;
    const requestPayload = { docId };
    const response: any = await this.reimbursementService.previewDocumentReimbursment(requestPayload).pipe(first()).toPromise();
    if (response.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
      const base64Data = response.serviceResponse.documentBytes;
      const mimeType = this.getMimeTypeFromBase64(base64Data);
      if (mimeType === 'application/pdf') {
        const pdfUrl = `data:application/pdf;base64,${base64Data}`;
        this.draftDocPreview = this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl);
      } else if (mimeType.startsWith('image/')) {
        this.draftDocPreview = `data:${mimeType};base64,${base64Data}`;
      } else {
        const link = document.createElement('a');
        link.href = `data:application/octet-stream;base64,${base64Data}`;
        link.download = 'document';
        link.click();
      }
    }
  }

  clearDraftDocPreview() {
    this.draftDocPreview = null;
  }

  isDraftPdfPreview(): boolean {
    if (!this.draftDocPreview) {
      return false;
    }
    return this.draftDocPreview.toString().includes('application/pdf');
  }

  removeDocumentFromDraftClaim(docIndex: number, alertTemplate: TemplateRef<any>) {
    if (this.draftDocsClaimIndex === null) {
      return;
    }
    const claimIndex = this.draftDocsClaimIndex;
    const claim = this.ticketClaims[claimIndex];
    if (!claim?.documents) {
      return;
    }
    claim.documents.splice(docIndex, 1);
    claim.docIds = claim.documents.map((d: { docId: number }) => d.docId);
    if (claim.documents.length === 0) {
      this.closeDraftDocumentsModal();
      if (this.editingClaimIndex === claimIndex) {
        this.cancelEditDraftClaim();
      } else if (this.editingClaimIndex !== null && claimIndex < this.editingClaimIndex) {
        this.editingClaimIndex--;
      }
      this.ticketClaims.splice(claimIndex, 1);
      this.openAlertMod(alertTemplate, 'Claim removed because it had no documents left.');
    }
  }

  docCountForClaim(c: any): number {
    if (c?.documents?.length) {
      return c.documents.length;
    }
    return c?.docIds?.length || 0;
  }

  /** Documents for the claim currently in the form (edit mode). */
  documentsForEditingClaim(): { docId: number; fileName: string }[] {
    if (this.editingClaimIndex === null) {
      return [];
    }
    const claim = this.ticketClaims[this.editingClaimIndex];
    if (!claim) {
      return [];
    }
    return this.ensureClaimDocumentsList(claim);
  }

  hasDocumentsOnEditingClaim(): boolean {
    if (this.editingClaimIndex === null) {
      return false;
    }
    return this.docCountForClaim(this.ticketClaims[this.editingClaimIndex]) > 0;
  }

  removeDocumentFromEditingClaim(docIndex: number, alertTemplate: TemplateRef<any>) {
    if (this.editingClaimIndex === null) {
      return;
    }
    const claimIndex = this.editingClaimIndex;
    const claim = this.ticketClaims[claimIndex];
    if (!claim?.documents?.length) {
      this.ensureClaimDocumentsList(claim);
    }
    if (!claim?.documents?.length) {
      return;
    }
    claim.documents.splice(docIndex, 1);
    claim.docIds = claim.documents.map((d: { docId: number }) => d.docId);
    this.draftDocPreview = null;
    if (claim.documents.length === 0) {
      this.ticketClaims.splice(claimIndex, 1);
      this.cancelEditDraftClaim();
      this.openAlertMod(alertTemplate, 'Claim removed because it had no documents left.');
    }
  }

  private toIsoStartOfDay(dateStr: string | null): string | null {
    if (!dateStr) {
      return null;
    }
    return new Date(dateStr + 'T00:00:00').toISOString();
  }

  private toIsoEndOfDay(dateStr: string | null): string | null {
    if (!dateStr) {
      return null;
    }
    return new Date(dateStr + 'T23:59:59').toISOString();
  }

  async addClaimToTicket(template: TemplateRef<any>) {
    if (!this.isValidForm()) {
      return;
    }
    if (!this.reimbursementObj.expenditureType) {
      this.openAlertMod(template, 'Please select an Expenditure Type.');
      return;
    }
    if (!this.mappedProjectsForClaim.length) {
      this.openAlertMod(template, this.emptyProjectPickerHint());
      return;
    }
    if (this.reimbursementObj.projectId == null || this.reimbursementObj.projectId === '') {
      this.openAlertMod(template, 'Please select a Project.');
      return;
    }
    const selectedPid = Number(this.reimbursementObj.projectId);
    if (!this.mappedProjectsForClaim.some((p) => p.projectId === selectedPid)) {
      this.openAlertMod(template, 'Please select a valid project from the list.');
      return;
    }
    if (selectedPid === this.RMB_OTHERS_PROJECT_ID) {
      const on = (this.reimbursementObj.othersProjectName || '').trim();
      if (!on) {
        this.openAlertMod(template, 'Enter the project name for Others.');
        return;
      }
      if (this.reimbursementObj.othersClientId == null || this.reimbursementObj.othersClientId === '') {
        this.openAlertMod(template, 'Select a client for Others.');
        return;
      }
    }
    if (this.reimbursementObj.expenditureType === 'Travel') {
      if (!this.reimbursementObj.travelMode) {
        this.openAlertMod(template, 'Please select a Travel Mode.');
        return;
      }
      if (this.reimbursementObj.travelMode === 'Personal Vehicle') {
        if (!this.reimbursementObj.vehicleType) {
          this.openAlertMod(template, 'Please select a Vehicle Type.');
          return;
        }
        if (!this.reimbursementObj.distance || this.reimbursementObj.distance <= 0) {
          this.openAlertMod(template, 'Please enter valid Distance in KM.');
          return;
        }
      }
    }
    if (this.reimbursementObj.expenditureType === 'Food') {
      if (!this.reimbursementObj.foodAllowanceType) {
        this.openAlertMod(template, 'Please select Food Allowance Type.');
        return;
      }
      if (!this.reimbursementObj.dateOfFood) {
        this.openAlertMod(template, 'Please select the Fooding Date.');
        return;
      }
      if (!this.dateInClaimWindow(this.reimbursementObj.dateOfFood)) {
        this.openAlertMod(
          template,
          `Food expense date must be in the previous calendar month only (${this.claimDateWindowHint})`
        );
        return;
      }
    }
    if (!this.reimbursementObj.amount || this.reimbursementObj.amount <= 0) {
      this.openAlertMod(template, 'Please enter a valid Total Amount .');
      return;
    }
    if (this.reimbursementObj.expenditureType !== 'Food') {
      if (!this.reimbursementObj.fromDate) {
        this.openAlertMod(template, 'Please select From Date.');
        return;
      }
      if (!this.reimbursementObj.toDate) {
        this.openAlertMod(template, 'Please select To Date.');
        return;
      }
      if (String(this.reimbursementObj.toDate) < String(this.reimbursementObj.fromDate)) {
        this.openAlertMod(template, 'To date cannot be earlier than From date.');
        return;
      }
      if (!this.dateInClaimWindow(this.reimbursementObj.fromDate) || !this.dateInClaimWindow(this.reimbursementObj.toDate)) {
        this.openAlertMod(
          template,
          `From and To dates must be in the previous calendar month only (${this.claimDateWindowHint})`
        );
        return;
      }
    }
    if (!this.reimbursementObj.purpose || this.reimbursementObj.purpose.trim() === '') {
      this.openAlertMod(template, 'Please enter the Purpose.');
      return;
    }
    const isEdit = this.editingClaimIndex !== null;
    const hasNewUpload = !!(this.fileUploads?.length && this.fileUploads.some(f => f.file));
    if (isEdit) {
      const idx = this.editingClaimIndex as number;
      if (idx < 0 || idx >= this.ticketClaims.length) {
        this.cancelEditDraftClaim();
        this.openAlertMod(template, 'Edit session was reset because the claim is no longer in the ticket.');
        return;
      }
      this.ensureClaimDocumentsList(this.ticketClaims[idx]);
      const existingDocCount = this.ticketClaims[idx].documents?.length || 0;
      if (!hasNewUpload && existingDocCount === 0) {
        this.openAlertMod(template, 'This claim has no documents. Add at least one supporting document or cancel edit.');
        return;
      }
    } else if (!this.fileUploads || this.fileUploads.length === 0 || !this.fileUploads.some(f => f.file)) {
      this.openAlertMod(template, 'Please upload a valid document.');
      return;
    }
    try {
      const uploadNewFiles = async (): Promise<{ docId: number; fileName: string }[]> => {
        const uploaded: { docId: number; fileName: string }[] = [];
        for (const fileObj of this.fileUploads) {
          if (fileObj.file) {
            const fileFormData = new FormData();
            fileFormData.append('file', fileObj.file);
            fileFormData.append('displayName', fileObj.file.name);
            fileFormData.append('uploadedBy', this.currentEmployeeInfo.empId);
            const uploadResponse: any = await this.reimbursementService.uploadFileReimbursement(fileFormData)
              .pipe(first())
              .toPromise();
            if (uploadResponse.serviceStatus !== 'Success') {
              this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
              return [];
            }
            const docId = uploadResponse.serviceResponse.documentId;
            uploaded.push({ docId, fileName: fileObj.file.name });
          }
        }
        return uploaded;
      };

      if (isEdit) {
        const idx = this.editingClaimIndex as number;
        const existing = this.ticketClaims[idx];
        this.ensureClaimDocumentsList(existing);
        const documents: { docId: number; fileName: string }[] = existing.documents.map(
          (d: { docId: number; fileName: string }) => ({ docId: d.docId, fileName: d.fileName })
        );
        const newDocs = await uploadNewFiles();
        if (newDocs.length === 0 && hasNewUpload) {
          return;
        }
        documents.push(...newDocs);
        if (documents.length === 0) {
          this.openAlertMod(template, 'At least one supporting document is required.');
          return;
        }
        const docIds = documents.map(d => d.docId);
        const row = this.mappedProjectsForClaim.find((p) => p.projectId === selectedPid);
        this.ticketClaims[idx] = {
          expenditureType: this.reimbursementObj.expenditureType,
          expenditureTypeDescription: this.reimbursementObj.expenditureTypeDescription,
          amount: Number(this.reimbursementObj.amount),
          travelMode: this.reimbursementObj.travelMode,
          distance: this.reimbursementObj.distance,
          vehicleType: this.reimbursementObj.vehicleType,
          foodAllowanceType: this.reimbursementObj.foodAllowanceType,
          dateOfFood: this.reimbursementObj.dateOfFood,
          fromDate: this.reimbursementObj.fromDate,
          toDate: this.reimbursementObj.toDate,
          purpose: this.reimbursementObj.purpose.trim(),
          projectId: selectedPid,
          projectName: this.selectedProjectLabel(),
          othersProjectName: selectedPid === this.RMB_OTHERS_PROJECT_ID ? (this.reimbursementObj.othersProjectName || '').trim() : null,
          othersClientId: selectedPid === this.RMB_OTHERS_PROJECT_ID ? Number(this.reimbursementObj.othersClientId) : null,
          clientId: selectedPid !== this.RMB_OTHERS_PROJECT_ID && row?.clientId != null ? Number(row.clientId) : null,
          clientName: selectedPid !== this.RMB_OTHERS_PROJECT_ID ? (row?.clientName || this.reimbursementObj.displayClientName || '') : null,
          docIds,
          documents
        };
        this.cancelEditDraftClaim();
        this.openAlertMod(template, 'Claim updated in your ticket.');
        return;
      }

      const documents = await uploadNewFiles();
      if (documents.length === 0) {
        return;
      }
      const docIds = documents.map(d => d.docId);
      const row = this.mappedProjectsForClaim.find((p) => p.projectId === selectedPid);
      this.ticketClaims.push({
        expenditureType: this.reimbursementObj.expenditureType,
        expenditureTypeDescription: this.reimbursementObj.expenditureTypeDescription,
        amount: Number(this.reimbursementObj.amount),
        travelMode: this.reimbursementObj.travelMode,
        distance: this.reimbursementObj.distance,
        vehicleType: this.reimbursementObj.vehicleType,
        foodAllowanceType: this.reimbursementObj.foodAllowanceType,
        dateOfFood: this.reimbursementObj.dateOfFood,
        fromDate: this.reimbursementObj.fromDate,
        toDate: this.reimbursementObj.toDate,
        purpose: this.reimbursementObj.purpose.trim(),
        projectId: selectedPid,
        projectName: this.selectedProjectLabel(),
        othersProjectName: selectedPid === this.RMB_OTHERS_PROJECT_ID ? (this.reimbursementObj.othersProjectName || '').trim() : null,
        othersClientId: selectedPid === this.RMB_OTHERS_PROJECT_ID ? Number(this.reimbursementObj.othersClientId) : null,
        clientId: selectedPid !== this.RMB_OTHERS_PROJECT_ID && row?.clientId != null ? Number(row.clientId) : null,
        clientName: selectedPid !== this.RMB_OTHERS_PROJECT_ID ? (row?.clientName || this.reimbursementObj.displayClientName || '') : null,
        docIds,
        documents
      });
      this.resetAfterSubmit();
      this.fileUploads = [{}];
      this.openAlertMod(template, 'Claim added to your ticket. You may add more claims or submit the ticket.');
    } catch (error: any) {
      console.error('Error during add claim:', error);
      this.openAlertMod(template, error?.message || 'An unexpected error occurred.');
    }
  }

  async loadSubmissionWindowStatus(): Promise<void> {
    try {
      const res: any = await this.reimbursementService.getReimbursementSubmissionWindowStatus().toPromise();
      if (res?.serviceStatus === 'Success' && res.serviceResponse) {
        this.submissionWindowAllowed = res.serviceResponse.allowed !== false;
        this.submissionWindowMessage = res.serviceResponse.message || '';
      }
    } catch {
      this.submissionWindowAllowed = true;
      this.submissionWindowMessage = '';
    }
  }

  async submitEntireTicket(template: TemplateRef<any>) {
    if (!this.submissionWindowAllowed) {
      this.openAlertMod(template, this.submissionWindowMessage || 'Reimbursement submission is closed for this month.');
      return;
    }
    if (this.ticketClaims.length < 1) {
      this.openAlertMod(template, 'Use "Add claim to ticket" to add at least one claim before submitting.');
      return;
    }
    if (!this.currentUser?.hodId || !this.currentUser?.hodEmail) {
      this.openAlertMod(template, 'HOD information is missing on your profile. Please contact HR.');
      return;
    }
    await this.onGetEmployeeInfo();
    for (const c of this.ticketClaims) {
      if (c.projectId == null || c.projectId === '') {
        this.openAlertMod(template, 'Each claim must have a project. Edit any claim missing a project and update it.');
        return;
      }
      if (c.expenditureType === 'Food') {
        if (c.dateOfFood && !this.dateInClaimWindow(c.dateOfFood)) {
          this.openAlertMod(
            template,
            `Each food claim date must be in the previous calendar month only (${this.claimDateWindowHint}). Edit the invalid claim.`
          );
          return;
        }
      } else if (c.fromDate && c.toDate) {
        if (!this.dateInClaimWindow(c.fromDate) || !this.dateInClaimWindow(c.toDate)) {
          this.openAlertMod(
            template,
            `Each claim's From and To dates must be in the previous calendar month only (${this.claimDateWindowHint}). Edit the invalid claim.`
          );
          return;
        }
      }
      if (c.expenditureType !== 'Food' && c.fromDate && c.toDate && String(c.toDate) < String(c.fromDate)) {
        this.openAlertMod(
          template,
          'Each claim must have To date on or after From date. Edit the claim with invalid dates.'
        );
        return;
      }
    }
    const claims = this.ticketClaims.map((c, i) => {
      const pid = Number(c.projectId);
      const base: any = {
        lineNo: i + 1,
        expenditureType: c.expenditureType,
        amount: c.amount,
        travelMode: c.travelMode || null,
        distance: c.distance != null ? c.distance : null,
        vehicleType: c.vehicleType || null,
        foodAllowanceType: c.foodAllowanceType || null,
        dateOfFood: c.dateOfFood ? new Date(this.toIsoStartOfDay(c.dateOfFood)!).toISOString() : null,
        fromDate: c.fromDate ? new Date(this.toIsoStartOfDay(c.fromDate)!).toISOString() : null,
        toDate: c.toDate ? new Date(this.toIsoEndOfDay(c.toDate)!).toISOString() : null,
        purpose: c.purpose,
        projectId: pid,
        docIds: (c.documents && c.documents.length ? c.documents.map((d: { docId: number }) => d.docId) : c.docIds) || []
      };
      if (pid === this.RMB_OTHERS_PROJECT_ID) {
        base.othersProjectName = (c.othersProjectName || '').trim();
        base.clientId = c.othersClientId != null ? Number(c.othersClientId) : null;
      } else {
        base.othersProjectName = null;
        base.clientId = null;
      }
      return base;
    });
    const body = {
      empId: this.currentEmployeeInfo.empId,
      fullName: this.currentEmployeeInfo.name,
      email: this.currentEmployeeInfo.email,
      departmentName: this.currentEmployeeInfo.departmentName,
      designationName: this.currentEmployeeInfo.designationName,
      mobileNo: this.currentEmployeeInfo.mobileNo != null ? String(this.currentEmployeeInfo.mobileNo) : '',
      hodEmpId: this.currentUser.hodId,
      hodName: this.currentUser.hodName,
      hodEmail: this.currentUser.hodEmail,
      claims
    };
    try {
      const response: any = await this.reimbursementService.saveReimbursementTicket(body).pipe(first()).toPromise();
      if (response.serviceStatus === 'Success') {
        this.editingClaimIndex = null;
        this.ticketClaims = [];
        this.resetAfterSubmit();
        this.fileUploads = [{}];
        const ticketRef = this.displaySubmittedTicketRef(response.serviceResponse);
        const successMsg = ticketRef
          ? `Success! Your request has been registered with Ticket ID:- ${ticketRef}`
          : 'Success! Your reimbursement ticket was submitted.';
        this.openAlertMod(template, successMsg);
      } else {
        this.openAlertMod(template, response.serviceError || response.serviceResponse || 'Submit failed.');
      }
    } catch (error) {
      console.error('Error during ticket submit:', error);
      this.openAlertMod(template, 'An unexpected error occurred while submitting the ticket.');
    }
  }

  isValidForm() {
    return true;
  }

  /** Matches backend ticketDisplayRef: public ticketNo (APM-RMB-…) when set, else numeric ticketId. */
  private displaySubmittedTicketRef(servicePayload: unknown): string {
    if (!servicePayload || typeof servicePayload !== 'object') {
      return '';
    }
    const o = servicePayload as Record<string, unknown>;
    const ticketNo = o['ticketNo'];
    const ticketId = o['ticketId'];
    const fromNo = typeof ticketNo === 'string' ? ticketNo.trim() : String(ticketNo ?? '').trim();
    if (fromNo) {
      return fromNo;
    }
    if (ticketId != null && String(ticketId).trim() !== '') {
      return String(ticketId);
    }
    return '';
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }


  cancelRequest() {
    this.modalRef?.close();
    // location.reload();
  }

  cancelRequest2() {
    this.modalRef?.close();
  }

  cancelRequest1() {
    this.modalRef?.close();
    this.resetAfterSubmit();
    // location.reload();
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.reimbursementObj.supportingDocument = file;
    }
  }




  async resetAfterSubmit() {
    this.reimbursementObj = {
      currencyType: '',
      currentUser: '',
      amount: 0,
      distance: '',
      travelMode: '',
      travelClass: '',
      expenditureType: '',
      projectId: null,
      fromDate: null,
      toDate: null,
      dateOfFood: null,
      purpose: '',
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      vehicleType: '',
      file: '',
      othersProjectName: '',
      othersClientId: null,
      displayClientName: '',
      expenditureTypeDescription: ''
    };
    const fileInput: HTMLInputElement | null = document.querySelector('input[type="file"]');
    if (fileInput) {
      fileInput.value = ''; // Clear the file input value
    }
    this.invalidFromDate = false;
    this.invalidToDate = false;
  }


  invalidFromDate: boolean = false;
  invalidToDate: boolean = false;

  onFromDateChange(value: string) {
    this.invalidToDate = false;
    if (!value) {
      this.invalidFromDate = false;
      return;
    }
    if (!this.dateInClaimWindow(value)) {
      this.invalidFromDate = true;
      this.reimbursementObj.fromDate = '';
      this.reimbursementObj.toDate = null;
      return;
    }
    this.invalidFromDate = false;
    this.syncToDateAfterFromChange();
  }

  onToDateChange(value: string) {
    this.invalidToDate = false;
    if (!value || !this.reimbursementObj.fromDate) {
      return;
    }
    if (!this.dateInClaimWindow(value)) {
      this.reimbursementObj.toDate = null;
      this.invalidToDate = true;
      return;
    }
    if (String(value) < String(this.reimbursementObj.fromDate)) {
      this.reimbursementObj.toDate = null;
      this.invalidToDate = true;
    }
  }

  private syncToDateAfterFromChange(): void {
    const f = this.reimbursementObj.fromDate;
    const t = this.reimbursementObj.toDate;
    if (!f || !t) {
      return;
    }
    if (String(t) < String(f) || !this.dateInClaimWindow(t)) {
      this.reimbursementObj.toDate = null;
      this.invalidToDate = true;
    }
  }

  fileUploads: any[] = [{}];

  onFileChange1(event: any, index: number, template: TemplateRef<any>) {
    const file = event.target.files[0];
    if (!file) {

      this.fileUploads[index].file = null;
      return;
    }
    if (file) {
      const maxSizeInBytes = 1 * 1024 * 1024;

      if (file.size > maxSizeInBytes) {
        this.openAlertMod(template, "File size should be less than or equal to 1MB.");
        event.target.value = '';
        return;
      }

      this.fileUploads[index].file = file;
      this.fileUploads[index].uploadedBy = this.currentEmployeeInfo.empId;
      // this.reimbursementObj.supportingDocument = file;
    }
  }


  addInputSpecializationField() {
    if (this.fileUploads.length < 6) {
      this.fileUploads.push({});
    }
  }

  removeInputSpecializationField(index: number) {
    this.fileUploads.splice(index, 1);
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }



async onGetExpenditureType() {

  const response: any = await this.reimbursementService.onGetExpenditureType().toPromise();
  if (response.serviceStatus == "Success") {
  this.expenditureTypeList = response.serviceResponse;

  console.log("expenditureTypeList ::::::: : ", this.expenditureTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }

  async onGetTravelMode() {

  const response: any = await this.reimbursementService.getTravelMode().toPromise();
  if (response.serviceStatus == "Success") {
  this.travelModeList = response.serviceResponse;

  console.log("travelModelist ::::::: : ", this.travelModeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetVehicleType() {

  const response: any = await this.reimbursementService.onGetVehicleType().toPromise();
  if (response.serviceStatus == "Success") {
  this.vehicleTypeList = response.serviceResponse;

  console.log("vehicleTypeList ::::::: : ", this.vehicleTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetFoodType() {

  const response: any = await this.reimbursementService.onGetFoodType().toPromise();
  if (response.serviceStatus == "Success") {
  this.foodTypeList = response.serviceResponse;

  console.log("foodTypeList ::::::: : ", this.foodTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }



}








