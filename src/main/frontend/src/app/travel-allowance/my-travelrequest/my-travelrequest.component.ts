import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/internal/operators/first';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ActivatedRoute, Router } from '@angular/router';
import {
  TRAVEL_POLICY_ADVANCE_DAYS,
  TRAVEL_POLICY_MAX_FUTURE_DAYS,
  addDaysIso,
  hotelCategoryLabel,
  isActiveMasterRow,
  isHotelTravelReasonName,
  parseIsoDate,
  suggestedMetroHotelCategory,
  tripTypeCodeFromPlanner,
  tripTypeDisplayLabel,
  lineTravelModeDisplay,
  lineTravelClassDisplay
} from '../travel-policy.helper';


@Component({
  standalone: false,
  selector: 'app-my-travelrequest',
  templateUrl: './my-travelrequest.component.html',
  styleUrls: ['./my-travelrequest.component.css']
})
// export class TravelAllowanceComponent implements OnInit {
export class MyTravelrequestComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild('kycDocumentPreviewModal')
  kycDocumentPreviewModal: TemplateRef<any>;

  isUpdateProfile: boolean = false;
  travelDeskInfo: MyTravelDesk = new MyTravelDesk();
  currentUser: any;
  currentEmployeeInfo: Employee = new Employee();
  file: any;
  fileName: any;
  feature = "Profile";
  userMapping: any = {};
  cityOptions: string[] = [];
  activeCitySuggestionKey: string | null = null;
  travelModelistByReason: any[] = [];
  travelClasslistByReason: any[] = [];
  citylistBySubCategory: any[] = [];
  selectedTravelReasons: string = '';
  private citySuggestionBlurTimer: ReturnType<typeof setTimeout> | null = null;


  //modal
  alertMessage: any;
  modalRef:NgbModalRef;


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;



  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  travelDeskObj: any = {
    associatedTravelRequest: '',
    travelMode: '',
    travelClass: '',
    fromDate: null,
    toDate: null,
    purposeOfTravel: '',
    fromLocation: '',
    toLocation: '',
    supportingDocument: null,
    hotelCategory: '',
    tlSubCategory: '',
    docIds: [],
    kycDocumentId: '',
    projectId: null as number | null,
    othersProjectName: '',
    othersClientId: null as number | null,
    displayClientName: ''
  };

  mappedProjectsForTravel: { projectId: number; projectName: string; clientName?: string; clientId?: number | null }[] = [];
  projectPickerSource: 'TEAM' | 'DEPARTMENT' | null = null;
  showOthersOption = false;
  clientsForOthers: { clientId: number; clientName: string }[] = [];
  readonly TRAVEL_OTHERS_PROJECT_ID = -1;
  ticketLines: any[] = [];
  ticketKycDocumentId: number | null = null;
  /** Original upload name for ticket-level KYC (shown on edit; file inputs cannot repopulate). */
  ticketKycDisplayName: string | null = null;
  editingLineIndex: number | null = null;
  draftDocsModalRef: NgbModalRef | null = null;
  kycPreviewModalRef: NgbModalRef | null = null;
  draftDocsLineIndex: number | null = null;
  draftDocPreview: SafeResourceUrl | string | null = null;
  private kycPreviewObjectUrl: string | null = null;
  locationStrategy: any;
  domainSpecializationList: any[];
  todayDate: string;
  travelReasonlist: any[] = [];
  hotelSubCategorylist: any[] = [];
  hotelCategorylist: any[] = [];
  hotelSubCategorySelectId: number | null = null;
  maxTravelRequestDate: any = new Date();


  constructor(
    private validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private sanitizer: DomSanitizer,
    private travelDesk: TravelDeskService,
    private reimbursementService: ReimbursementService,
    private domainService: DomainService,
    private router : Router


  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.onGetTravelReason();
    void this.loadCityAutocompleteOptions();

    const today = new Date();
    const future = new Date();
    future.setDate(today.getDate() + 60);
    today.setDate(today.getDate() + 7);
    this.todayDate = today.toISOString().split('T')[0];
    this.maxTravelRequestDate = future.toISOString().split('T')[0];
    this.onGetEmployeeInfo();


    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    this.preventBackButton();
    this.onGetHotelCategory();

    //this.updateCityOptions(cityCategory);

  }
  async loadCityAutocompleteOptions(): Promise<void> {
    try {
      const response: any = await this.travelDesk.getCity().pipe(first()).toPromise();
      if (response?.serviceStatus !== 'Success') {
        console.error('Failed to fetch city suggestions:', response?.serviceError || response?.serviceResponse);
        return;
      }
      const uniqueCities = new Set<string>();
      for (const row of (response.serviceResponse || []).filter(isActiveMasterRow)) {
        const cityName = row?.cityName != null ? String(row.cityName).trim() : '';
        if (cityName) {
          uniqueCities.add(cityName);
        }
      }
      this.cityOptions = Array.from(uniqueCities).sort((a, b) => a.localeCompare(b));
    } catch (error) {
      console.error('Error fetching city suggestions:', error);
    }
  }

  get hotelCityOptions(): string[] {
    const scoped = (this.citylistBySubCategory || [])
      .map((row: { cityName?: string }) => row?.cityName != null ? String(row.cityName).trim() : '')
      .filter((city: string) => !!city);
    const source = scoped.length ? scoped : this.cityOptions;
    return this.uniqueCityOptions(source);
  }

  private uniqueCityOptions(source: string[]): string[] {
    const unique = new Map<string, string>();
    for (const city of source || []) {
      const trimmed = (city || '').trim();
      if (!trimmed) {
        continue;
      }
      const key = trimmed.toLowerCase();
      if (!unique.has(key)) {
        unique.set(key, trimmed);
      }
    }
    return Array.from(unique.values()).sort((a, b) => a.localeCompare(b));
  }

  private cityFieldKey(kind: string, index?: number): string {
    return index == null ? kind : `${kind}-${index}`;
  }

  private getCityFieldValue(kind: string, index?: number): string {
    switch (kind) {
      case 'from':
        return this.travelDeskObj.fromLocation || '';
      case 'to':
        return this.travelDeskObj.toLocation || '';
      case 'hotel':
        return this.travelDeskObj.selectedCity || '';
      case 'mc-from':
        return this.multiCityLegs[index ?? 0]?.fromLocation || '';
      case 'mc-to':
        return this.multiCityLegs[index ?? 0]?.toLocation || '';
      default:
        return '';
    }
  }

  private setCityFieldValue(kind: string, value: string, index?: number): void {
    switch (kind) {
      case 'from':
        this.travelDeskObj.fromLocation = value;
        break;
      case 'to':
        this.travelDeskObj.toLocation = value;
        break;
      case 'hotel':
        this.travelDeskObj.selectedCity = value;
        break;
      case 'mc-from':
        if (this.multiCityLegs[index ?? 0]) {
          this.multiCityLegs[index ?? 0].fromLocation = value;
        }
        break;
      case 'mc-to':
        if (this.multiCityLegs[index ?? 0]) {
          this.multiCityLegs[index ?? 0].toLocation = value;
        }
        break;
    }
  }

  getCitySuggestions(kind: string, index?: number): string[] {
    const source = kind === 'hotel' ? this.hotelCityOptions : this.cityOptions;
    if (!source.length) {
      return [];
    }
    const query = this.getCityFieldValue(kind, index).trim().toLowerCase();
    if (!query) {
      return [];
    }
    const startsWith = source.filter((city) => city.toLowerCase().startsWith(query));
    const contains = source.filter((city) => !startsWith.includes(city) && city.toLowerCase().includes(query));
    return [...startsWith, ...contains].slice(0, 8);
  }

  showCitySuggestions(kind: string, index?: number): boolean {
    return this.activeCitySuggestionKey === this.cityFieldKey(kind, index)
      && this.getCitySuggestions(kind, index).length > 0;
  }

  onCityInputFocus(kind: string, index?: number): void {
    if (this.citySuggestionBlurTimer) {
      clearTimeout(this.citySuggestionBlurTimer);
      this.citySuggestionBlurTimer = null;
    }
    this.activeCitySuggestionKey = this.getCitySuggestions(kind, index).length
      ? this.cityFieldKey(kind, index)
      : null;
  }

  onCityInputChange(kind: string, index?: number): void {
    this.activeCitySuggestionKey = this.getCitySuggestions(kind, index).length
      ? this.cityFieldKey(kind, index)
      : null;
  }

  onCityInputBlur(): void {
    if (this.citySuggestionBlurTimer) {
      clearTimeout(this.citySuggestionBlurTimer);
    }
    this.citySuggestionBlurTimer = setTimeout(() => {
      this.activeCitySuggestionKey = null;
    }, 120);
  }

  selectCitySuggestion(event: MouseEvent, city: string, kind: string, index?: number): void {
    event.preventDefault();
    this.setCityFieldValue(kind, city, index);
    this.activeCitySuggestionKey = null;
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }




  disableMannualDateInput() {
    return false;
  }



  async onGetEmployeeInfo() {
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;


    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      void this.loadMappedProjectsForTravel();
    } else {
      console.error(response.serviceResponse);
    }

    setTimeout(() => {
      this.currentEmployeeInfo.documentList && this.currentEmployeeInfo.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
          let objectURL = 'data:image/*;base64,' + doc.documentBytes;
          let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
          preview.setAttribute('src', src);
        }
      });
    }, 500);
  }

  cancelRequest() {
    this.modalRef?.close();
    location.reload();
  }

  cancelRequest3() {
    this.modalRef?.close();
  }

  cancelRequest1() {
    this.modalRef?.close();
  }


  async loadMappedProjectsForTravel() {
    this.mappedProjectsForTravel = [];
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
      const othersRows = parsed.filter((r) => r.projectId === this.TRAVEL_OTHERS_PROJECT_ID);
      const normalRows = parsed
        .filter((r) => r.projectId !== this.TRAVEL_OTHERS_PROJECT_ID)
        .sort((a, b) => a.projectName.localeCompare(b.projectName));
      // Travel: always offer "Others" (manual project name + client from clients master)
      if (!othersRows.length) {
        othersRows.push({
          projectId: this.TRAVEL_OTHERS_PROJECT_ID,
          projectName: 'Others',
          clientName: '',
          clientId: null
        });
      }
      this.mappedProjectsForTravel = [...othersRows, ...normalRows];
      await this.loadClientsForOthers();
    } catch (e) {
      console.error('loadMappedProjectsForTravel', e);
      this.mappedProjectsForTravel = [];
      this.projectPickerSource = null;
      this.showOthersOption = false;
    }
  }

  async loadClientsForOthers(): Promise<void> {
    if (this.clientsForOthers.length > 0) {
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
      return 'You have no mapped projects. Contact RMG to assign a project before submitting a Travel Request.';
    }
    return 'Projects could not be loaded. Refresh the page or try again later.';
  }

  clearSelectedProject(): void {
    this.travelDeskObj.projectId = null;
    this.travelDeskObj.othersProjectName = '';
    this.travelDeskObj.othersClientId = null;
    this.travelDeskObj.displayClientName = '';
  }

  clearOthersClient(): void {
    this.travelDeskObj.othersClientId = null;
  }

  isOthersProjectSelected(): boolean {
    return Number(this.travelDeskObj.projectId) === this.TRAVEL_OTHERS_PROJECT_ID;
  }

  showReadonlyClientForProject(): boolean {
    const id = this.travelDeskObj.projectId != null ? Number(this.travelDeskObj.projectId) : NaN;
    return Number.isFinite(id) && id !== this.TRAVEL_OTHERS_PROJECT_ID;
  }

  onTravelProjectSelected(_event?: unknown): void {
    const id = this.travelDeskObj.projectId != null ? Number(this.travelDeskObj.projectId) : NaN;
    if (!Number.isFinite(id)) {
      this.travelDeskObj.displayClientName = '';
      this.travelDeskObj.othersProjectName = '';
      this.travelDeskObj.othersClientId = null;
      return;
    }
    if (id === this.TRAVEL_OTHERS_PROJECT_ID) {
      this.travelDeskObj.displayClientName = '';
      if (!this.clientsForOthers.length) {
        void this.loadClientsForOthers();
      }
      return;
    }
    this.travelDeskObj.othersProjectName = '';
    this.travelDeskObj.othersClientId = null;
    const p = this.mappedProjectsForTravel.find((x) => x.projectId === id);
    this.travelDeskObj.displayClientName = p?.clientName ? String(p.clientName) : '';
  }

  projectSectionHint(): string {
    if (this.isOthersProjectSelected()) {
      return 'Enter the project name and choose a client from the master list.';
    }
    return 'Select a mapped project. Client is filled automatically from the project mapping.';
  }

  private validateProjectAndClient(template: TemplateRef<any>): boolean {
    if (!this.mappedProjectsForTravel.length) {
      this.openAlertMod(template, this.emptyProjectPickerHint());
      return false;
    }
    if (this.travelDeskObj.projectId == null || this.travelDeskObj.projectId === '') {
      this.openAlertMod(template, 'Please select a project.');
      return false;
    }
    const selectedPid = Number(this.travelDeskObj.projectId);
    if (!this.mappedProjectsForTravel.some((p) => p.projectId === selectedPid)) {
      this.openAlertMod(template, 'Please select a valid project from the list.');
      return false;
    }
    if (selectedPid === this.TRAVEL_OTHERS_PROJECT_ID) {
      if (!(this.travelDeskObj.othersProjectName || '').trim()) {
        this.openAlertMod(template, 'Enter the project name for Others.');
        return false;
      }
      if (this.travelDeskObj.othersClientId == null || this.travelDeskObj.othersClientId === '') {
        this.openAlertMod(template, 'Select a client for Others.');
        return false;
      }
    }
    return true;
  }

  isPlannerDetailsVisible(): boolean {
    return this.plannerStep === 'details' || this.editingLineIndex !== null;
  }

  primaryPlannerButtonLabel(): string {
    if (this.editingLineIndex !== null) {
      return 'Update request';
    }
    if (this.plannerStep === 'route') {
      return 'Plan Trip';
    }
    return 'Add to ticket';
  }

  get linkedDraftTravelLine(): any | null {
    if (!this.ticketLines?.length) {
      return null;
    }
    return (
      [...this.ticketLines].reverse().find((ln) => !this.isHotelTravelReasonName(ln?.requestType)) || null
    );
  }

  /** Compact route + departure for hotel tab (linked flight). */
  draftTravelItinerarySummary(): string {
    const ln = this.linkedDraftTravelLine;
    if (!ln) {
      return '';
    }
    const route = `${(ln.fromLocation || '—').trim()} → ${(ln.toLocation || '—').trim()}`;
    const dep = this.formatPlannerDate(ln.fromDate) || '—';
    return `${route} · ${dep}`;
  }

  hasDraftTravelSegment(): boolean {
    return !!this.linkedDraftTravelLine;
  }

  private syncTicketKycFromLines(): void {
    for (const ln of this.ticketLines) {
      const raw = ln?.kycDocumentId;
      if (raw == null || raw === '') {
        continue;
      }
      const id = Number(raw);
      if (isNaN(id)) {
        continue;
      }
      if (this.ticketKycDocumentId == null) {
        this.ticketKycDocumentId = id;
        this.travelDeskObj.kycDocumentId = String(id);
      }
      if (!this.ticketKycDisplayName && ln.kycDisplayName) {
        this.ticketKycDisplayName = String(ln.kycDisplayName);
      }
    }
  }

  private applyKycUploadResult(uploadResponse: any): void {
    const doc = uploadResponse?.serviceResponse;
    const docId = Number(doc?.documentId);
    if (!Number.isFinite(docId)) {
      return;
    }
    this.ticketKycDocumentId = docId;
    this.travelDeskObj.kycDocumentId = String(docId);
    this.ticketKycDisplayName =
      (doc?.displayName && String(doc.displayName).trim()) ||
      this.selectedFileName ||
      this.humanizeKycStoredFileName(doc?.fileName) ||
      `KYC document #${docId}`;
    this.propagateTicketKycToLines();
  }

  private propagateTicketKycToLines(): void {
    if (this.ticketKycDocumentId == null) {
      return;
    }
    for (const ln of this.ticketLines) {
      ln.kycDocumentId = this.ticketKycDocumentId;
      if (this.ticketKycDisplayName) {
        ln.kycDisplayName = this.ticketKycDisplayName;
      }
    }
  }

  private humanizeKycStoredFileName(fileName: string | null | undefined): string | null {
    if (!fileName) {
      return null;
    }
    const m = String(fileName).match(/^\d+_(.+)$/);
    return m ? m[1] : String(fileName);
  }

  /** Resolve display name for attached KYC (from lines or preview API). */
  async refreshTicketKycAttachmentUi(): Promise<void> {
    this.syncTicketKycFromLines();
    const docId = this.resolveKycDocumentId();
    if (docId == null) {
      this.ticketKycDisplayName = null;
      return;
    }
    if (this.ticketKycDisplayName) {
      return;
    }
    try {
      const response: any = await this.travelDesk
        .previewDocument({ docId })
        .pipe(first())
        .toPromise();
      if (response?.serviceStatus === 'Success') {
        const stored = response.serviceResponse?.ticketFileName;
        const label = this.humanizeKycStoredFileName(stored);
        if (label) {
          this.ticketKycDisplayName = label;
          this.propagateTicketKycToLines();
        }
      }
    } catch {
      /* preview optional for label only */
    }
    if (!this.ticketKycDisplayName) {
      this.ticketKycDisplayName = `KYC document #${docId}`;
    }
  }

  ticketKycAttachmentLabel(): string {
    return (
      this.selectedFileName ||
      this.ticketKycDisplayName ||
      (this.ticketKycDocumentId != null ? `KYC document #${this.ticketKycDocumentId}` : '')
    );
  }

  hasTicketKycAttached(): boolean {
    return this.ticketKycDocumentId != null || this.resolveKycDocumentId() != null;
  }

  private normalizeDocumentBytesToBase64(raw: unknown): string | null {
    if (raw == null) {
      return null;
    }
    if (typeof raw === 'string') {
      return raw;
    }
    if (Array.isArray(raw)) {
      const bytes = new Uint8Array(raw as number[]);
      let binary = '';
      for (let i = 0; i < bytes.length; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return btoa(binary);
    }
    return null;
  }

  routeSummaryLabel(): string {
    if (this.isHotelLodgingActive()) {
      if (this.linkedDraftTravelLine && this.plannerStep === 'route') {
        return this.draftTravelItinerarySummary();
      }
      const city = (this.travelDeskObj.selectedCity || '').trim() || '—';
      const inD = this.formatPlannerDate(this.travelDeskObj.fromDate) || '—';
      const outD = this.formatPlannerDate(this.travelDeskObj.toDate) || '—';
      return `${city} · ${inD} – ${outD}`;
    }
    if (this.isMultiCityTravelPlanner()) {
      const legs = this.getCompleteMultiCityLegs();
      if (!legs.length) {
        return 'Multi-city';
      }
      return legs.map((l) => `${(l.fromLocation || '').trim()} → ${(l.toLocation || '').trim()}`).join(' · ');
    }
    const from = (this.travelDeskObj.fromLocation || '').trim() || '—';
    const to = (this.travelDeskObj.toLocation || '').trim() || '—';
    const dep = this.formatPlannerDate(this.travelDeskObj.fromDate) || '—';
    if (this.tripType === 'round' && this.travelDeskObj.toDate) {
      const ret = this.formatPlannerDate(this.travelDeskObj.toDate);
      return `${from} → ${to} · ${dep} – ${ret}`;
    }
    return `${from} → ${to} · ${dep}`;
  }

  onPrimaryPlannerAction(template: TemplateRef<any>): void {
    if (this.editingLineIndex !== null || this.plannerStep === 'details') {
      void this.submitForm(template);
      return;
    }
    const routeError = this.validateRouteStepOnly();
    if (routeError) {
      this.openAlertMod(template, routeError);
      return;
    }
    this.plannerStep = 'details';
    this.showPlannerDetails = true;
    void this.refreshTicketKycAttachmentUi();
    if (this.isHotelLodgingActive()) {
      if (!this.travelDeskObj.associatedTravelRequest) {
        const hotelReason = this.resolveHotelReasonName();
        if (hotelReason) {
          this.travelDeskObj.associatedTravelRequest = hotelReason;
          void this.onTravelReasonChange(hotelReason);
        }
      }
      if (!this.hotelCategorylist?.length) {
        void this.onGetHotelCategory();
      }
    }
  }

  backToRoutePlanner(): void {
    this.plannerStep = 'route';
  }

  private resetPlannerToRouteStep(): void {
    if (this.editingLineIndex === null) {
      this.plannerStep = 'route';
    }
  }

  validateRouteStepOnly(): string | null {
    const zoneToday = new Date();
    zoneToday.setHours(0, 0, 0, 0);
    const minDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_ADVANCE_DAYS);
    const maxDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_MAX_FUTURE_DAYS);
    const minDate = parseIsoDate(minDateStr);
    const maxDate = parseIsoDate(maxDateStr);

    const checkDate = (dateStr: string | null, label: string): string | null => {
      if (!dateStr) {
        return `Please select ${label}.`;
      }
      const d = parseIsoDate(dateStr);
      if (!d || !minDate || !maxDate) {
        return 'Invalid date.';
      }
      if (d < minDate) {
        return `Travel must be planned at least ${TRAVEL_POLICY_ADVANCE_DAYS} days in advance (ApMoSys Travel Policy).`;
      }
      if (d > maxDate) {
        return `Date cannot be more than ${TRAVEL_POLICY_MAX_FUTURE_DAYS} days from today.`;
      }
      return null;
    };

    if (this.isHotelLodgingActive()) {
      if (!(this.travelDeskObj.selectedCity || '').trim()) {
        return 'Please enter the hotel city.';
      }
      const inErr = checkDate(this.travelDeskObj.fromDate, 'check-in date');
      if (inErr) {
        return inErr;
      }
      if (!this.travelDeskObj.toDate) {
        return 'Please select check-out date.';
      }
      const from = parseIsoDate(this.travelDeskObj.fromDate);
      const to = parseIsoDate(this.travelDeskObj.toDate);
      if (from && to && to < from) {
        return 'Check-out cannot be before check-in.';
      }
      const toErr = checkDate(this.travelDeskObj.toDate, 'check-out date');
      if (toErr) {
        return toErr;
      }
      return null;
    }

    if (this.isMultiCityTravelPlanner()) {
      const legs = this.getCompleteMultiCityLegs();
      if (legs.length < 2) {
        return 'Enter at least two complete city segments for multi-city travel.';
      }
      let prev: Date | null = null;
      for (let i = 0; i < legs.length; i++) {
        const leg = legs[i];
        const label = `City ${i + 1}`;
        if (!(leg.fromLocation || '').trim()) {
          return `Please enter from location for ${label}.`;
        }
        if (!(leg.toLocation || '').trim()) {
          return `Please enter to location for ${label}.`;
        }
        const dErr = checkDate(leg.fromDate, `departure date for ${label}`);
        if (dErr) {
          return dErr;
        }
        const from = parseIsoDate(leg.fromDate!);
        if (prev && from && from < prev) {
          return 'Departure dates must be in chronological order.';
        }
        prev = from;
      }
      return null;
    }

    if (!(this.travelDeskObj.fromLocation || '').trim()) {
      return 'Please enter from location.';
    }
    if (!(this.travelDeskObj.toLocation || '').trim()) {
      return 'Please enter to location.';
    }
    const depErr = checkDate(this.travelDeskObj.fromDate, 'departure date');
    if (depErr) {
      return depErr;
    }
    if (this.tripType === 'round') {
      if (!this.travelDeskObj.toDate) {
        return 'Please select return date.';
      }
      const from = parseIsoDate(this.travelDeskObj.fromDate);
      const to = parseIsoDate(this.travelDeskObj.toDate);
      if (from && to && to < from) {
        return 'Return date cannot be before departure.';
      }
      return checkDate(this.travelDeskObj.toDate, 'return date');
    }
    return null;
  }

  async submitForm(template: TemplateRef<any>) {
    const policyError = this.validateLineForPolicy();
    if (policyError) {
      this.openAlertMod(template, policyError);
      return;
    }
    if (this.isValidForm()) {
      if (!this.validateProjectAndClient(template)) {
        return;
      }
      if (!this.travelDeskObj.associatedTravelRequest) {
        this.openAlertMod(template, "Please select an Associated Travel Request.");
        return;
      }
      if (this.bookingTypeTab === 'hotel' && !this.resolveHotelReasonName()) {
        this.openAlertMod(
          template,
          'Hotel travel reason is not configured. Add a travel reason for hotels (e.g. "Hotel & Lodging") under Configuration → Travel.'
        );
        return;
      }
      if (this.isHotelLodgingActive()) {
        if (!this.travelDeskObj.hotelCategory) {
          this.openAlertMod(template, 'Please select a hotel category.');
          return;
        }
        if (!this.travelDeskObj.tlSubCategory) {
          this.openAlertMod(template, 'Please select a hotel sub-category.');
          return;
        }
        if (!this.travelDeskObj.selectedCity) {
          this.openAlertMod(template, 'Please select a city.');
          return;
        }
      } else if (!this.isHotelTravelReason(this.travelDeskObj.associatedTravelRequest) && !this.isMultiCityTravelPlanner()) {

        if (!this.travelDeskObj.travelMode) {
          this.openAlertMod(template, "Please select a Travel Mode.");
          return;
        }

        if (!this.travelDeskObj.travelClass) {
          this.openAlertMod(template, "Please select a Travel Class.");
          return;
        }
        if (!this.travelDeskObj.fromLocation) {
          this.openAlertMod(template, "Please Enter from location.");
          return;
        }
        if (!this.travelDeskObj.toLocation) {
          this.openAlertMod(template, "Please Enter to location.");
          return;
        }
      }
      if (!this.isMultiCityTravelPlanner() && !this.travelDeskObj.fromDate) {
        this.openAlertMod(template, "Please Select from date.");
        return;
      }
      const needsReturnDate =
        this.isHotelLodgingActive() ||
        (!this.isHotelTravelReason(this.travelDeskObj.associatedTravelRequest) &&
          this.tripType !== 'oneway' &&
          this.tripType !== 'multicity');
      if (needsReturnDate && !this.travelDeskObj.toDate) {
        this.openAlertMod(template, this.isHotelLodgingActive() ? 'Please select check-out date.' : 'Please select return date.');
        return;
      }
      if (!this.travelDeskObj.purposeOfTravel) {
        this.openAlertMod(template, "Please enter the Purpose Of Travel.");
        return;
      }
      if (!this.selectedFile && this.ticketKycDocumentId == null) {
        this.openAlertMod(template, 'Please select the KYC Document for Travel (required once per ticket).');
        return;
      }
      try {
        const isEdit = this.editingLineIndex !== null;

        if (isEdit) {
          const idx = this.editingLineIndex as number;
          if (idx < 0 || idx >= this.ticketLines.length) {
            this.cancelEditDraftLine();
            this.openAlertMod(
              template,
              'Edit session was reset because the request is no longer in the ticket.'
            );
            return;
          }
          this.ensureLineDocumentsList(this.ticketLines[idx]);
        }

        if (this.selectedFile) {
            try {
              const fileFormData = new FormData();
            fileFormData.append('file', this.selectedFile);
            fileFormData.append('displayName', this.selectedFileName);
            fileFormData.append('uploadedBy', this.currentEmployeeInfo.empId);
            const uploadResponse11: any = await this.travelDesk
              .uploadKycDocument(fileFormData)
                .pipe(first())
                .toPromise();
            if (uploadResponse11?.serviceStatus !== 'Success') {
              this.openAlertMod(template, `KYC upload failed: ${uploadResponse11?.serviceResponse || 'Unknown error'}`);
                return;
            }
            this.applyKycUploadResult(uploadResponse11);
            this.showPlannerDetails = true;
          } catch (error: any) {
            this.openAlertMod(template, `KYC upload failed: ${error?.message || error}`);
            return;
          }
        }

        const newDocs: { docId: number; fileName: string }[] = [];

        if (isEdit) {
          const idx = this.editingLineIndex as number;
          const existing = this.ticketLines[idx];
          this.ensureLineDocumentsList(existing);
          const documents = existing.documents.map((d: { docId: number; fileName: string }) => ({
            docId: d.docId,
            fileName: d.fileName
          }));
          documents.push(...newDocs);
          if (this.isMultiCityTravelPlanner()) {
            this.ticketLines[idx] = this.buildLinePayloadFromLeg(this.multiCityLegs[0], documents);
          } else {
            this.ticketLines[idx] = this.buildLinePayload(documents);
          }
          this.propagateTicketKycToLines();
          this.editingLineIndex = null;
          this.resetLineFormFields();
          this.multiCityLegs = [this.defaultMultiCityLeg(), this.defaultMultiCityLeg()];
          this.syncTicketKycFromLines();
          this.resetPlannerToRouteStep();
          this.openAlertMod(template, 'Travel Request updated in your ticket.');
          return;
        }

        this.syncTicketKycFromLines();

        if (this.isMultiCityTravelPlanner()) {
          const legs = this.getCompleteMultiCityLegs();
          if (legs.length < 2) {
            this.openAlertMod(template, 'Enter at least two complete city segments for multi-city travel.');
              return;
            }
          const payloads = legs.map((leg) => this.buildLinePayloadFromLeg(leg, newDocs));
          this.ticketLines.push(...payloads);
          this.propagateTicketKycToLines();
          this.resetLineFormFields();
          this.multiCityLegs = [this.defaultMultiCityLeg(), this.defaultMultiCityLeg()];
          this.openAlertMod(
            template,
            `Added ${payloads.length} multi-city segment(s) to your ticket. Add a hotel or submit when ready.`
          );
          this.resetPlannerToRouteStep();
        } else {
          this.ticketLines.push(this.buildLinePayload(newDocs));
          this.propagateTicketKycToLines();
          this.resetLineFormFields();
          this.resetPlannerToRouteStep();
          this.openAlertMod(template, 'Travel Request added to your ticket. Add more requests or submit the ticket.');
        }
      } catch (error) {
        console.error("Error during submit:", error);
        this.openAlertMod(template, "An unexpected error occurred while submitting the request.");
      }
    }
  }


  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }



  onPaste(e) {
    e.preventDefault();
    return false;
  }



  preventScroll(event: WheelEvent): void {
    event.preventDefault();
  }



  removeTicketLine(index: number): void {
    if (this.editingLineIndex === index) {
      this.cancelEditDraftLine();
    } else if (this.editingLineIndex !== null && index < this.editingLineIndex) {
      this.editingLineIndex--;
    }
    this.ticketLines.splice(index, 1);
    if (this.ticketLines.length === 0) {
      this.ticketKycDocumentId = null;
      this.ticketKycDisplayName = null;
    }
  }

  async beginEditDraftLine(index: number): Promise<void> {
    const ln = this.ticketLines[index];
    if (!ln) {
            return;
          }
    this.restoringDraftLineEdit = true;
    try {
    this.pausedEditLineIndex = null;
    this.editingLineIndex = index;
    this.plannerStep = 'details';
    this.draftDocPreview = null;
    this.syncTicketKycFromLines();
    await this.refreshTicketKycAttachmentUi();
    this.syncBookingTabFromReason();
    if (this.ticketKycDocumentId != null) {
      this.showPlannerDetails = true;
      this.travelDeskObj.kycDocumentId = String(this.ticketKycDocumentId);
    }
    const pid = ln.projectId != null ? Number(ln.projectId) : null;
    this.travelDeskObj.projectId = pid;
    if (pid === this.TRAVEL_OTHERS_PROJECT_ID) {
      this.travelDeskObj.othersProjectName = ln.projectName || '';
      this.travelDeskObj.othersClientId = ln.clientId != null ? Number(ln.clientId) : null;
      this.travelDeskObj.displayClientName = '';
      if (!this.clientsForOthers.length) {
        void this.loadClientsForOthers();
      }
    } else {
      this.travelDeskObj.othersProjectName = '';
      this.travelDeskObj.othersClientId = null;
      this.travelDeskObj.displayClientName = ln.clientName || '';
    }
    const savedReason = ln.requestType || '';
    const savedMode = ln.travelMode || '';
    const savedClass = ln.travelClass || '';
    this.travelDeskObj.associatedTravelRequest = savedReason;
    this.syncBookingTabFromReason();
    await this.onTravelReasonChange(savedReason, { preserveMode: true, preserveClass: true });
    this.travelDeskObj.travelMode = savedMode;
    if (savedMode) {
      await this.onModeChange(savedMode, { preserveClass: true });
    }
    this.travelDeskObj.travelClass = savedClass;
    this.travelDeskObj.hotelCategory = ln.hotelCategory || '';
    this.travelDeskObj.tlSubCategory = ln.hotelSubCategory || '';
    if (this.travelDeskObj.hotelCategory) {
      await this.onHotelCategoryChange(this.travelDeskObj.hotelCategory);
      if (this.travelDeskObj.tlSubCategory && this.hotelSubCategorylist?.length) {
        const sub = this.hotelSubCategorylist.find(
          (s: { hotelSubCategoryName?: string }) => s.hotelSubCategoryName === this.travelDeskObj.tlSubCategory
        );
        if (sub?.id != null) {
          this.hotelSubCategorySelectId = Number(sub.id);
          await this.onSubCategoryChanges(sub.id);
        }
      }
    }
    this.travelDeskObj.selectedCity = ln.city || '';
    const tripCode = (ln.tripType || '').toUpperCase();
    if (tripCode === 'MULTI_CITY') {
      this.tripType = 'multicity';
      this.multiCityLegs = [
        {
          fromLocation: ln.fromLocation || '',
          toLocation: ln.toLocation || '',
          fromDate: this.toDateInputValue(ln.fromDate)
        },
        this.defaultMultiCityLeg()
      ];
    } else {
      if (tripCode === 'ONE_WAY') {
        this.tripType = 'oneway';
      } else if (tripCode === 'ROUND') {
        this.tripType = 'round';
      } else {
        this.tripType = 'round';
      }
      this.travelDeskObj.fromLocation = ln.fromLocation || '';
      this.travelDeskObj.toLocation = ln.toLocation || '';
      this.travelDeskObj.fromDate = this.toDateInputValue(ln.fromDate);
      this.travelDeskObj.toDate = this.toDateInputValue(ln.toDate);
    }
    this.travelDeskObj.purposeOfTravel = ln.purpose || '';
    this.selectedFile = null;
    this.selectedFileName = null;
    this.ensureLineDocumentsList(ln);
    } finally {
      this.restoringDraftLineEdit = false;
    }
  }

  cancelEditDraftLine(): void {
    this.editingLineIndex = null;
    this.pausedEditLineIndex = null;
    this.draftDocPreview = null;
    this.resetLineFormFields();
    this.resetPlannerToRouteStep();
  }

  ensureLineDocumentsList(line: any): { docId: number; fileName: string }[] {
    if (line.documents?.length) {
      return line.documents;
    }
    if (line.docIds?.length) {
      line.documents = line.docIds.map((id: number) => ({
        docId: Number(id),
        fileName: 'Document #' + id
      }));
      return line.documents;
    }
    line.documents = [];
    return line.documents;
  }

  docCountForLine(line: any): number {
    if (line?.documents?.length) {
      return line.documents.length;
    }
    return line?.docIds?.length || 0;
  }

  documentsForEditingLine(): { docId: number; fileName: string }[] {
    if (this.editingLineIndex === null) {
      return [];
    }
    const line = this.ticketLines[this.editingLineIndex];
    return line ? this.ensureLineDocumentsList(line) : [];
  }

  hasDocumentsOnEditingLine(): boolean {
    return this.docCountForLine(this.ticketLines[this.editingLineIndex]) > 0;
  }

  openDraftDocumentsModal(template: TemplateRef<any>, lineIndex: number): void {
    this.draftDocsLineIndex = lineIndex;
    this.draftDocPreview = null;
    const ln = this.ticketLines[lineIndex];
    if (ln) {
      this.ensureLineDocumentsList(ln);
    }
    this.draftDocsModalRef = this.modalService.open(template, {
      size: 'lg',
      backdrop: 'static',
      windowClass: 'trv-docs-modal-window'
    });
  }

  closeDraftDocumentsModal(): void {
    this.draftDocsModalRef?.close();
    this.draftDocsModalRef = null;
    this.draftDocsLineIndex = null;
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

  async previewDraftDocument(docId: number): Promise<void> {
    this.revokeKycObjectUrl();
    this.draftDocPreview = null;
    const response: any = await this.travelDesk
      .previewDocument({ docId })
      .pipe(first())
      .toPromise();
    if (response?.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
      const base64Data = this.normalizeDocumentBytesToBase64(
        response.serviceResponse.documentBytes
      );
      if (!base64Data) {
        return;
      }
      const storedName = response.serviceResponse?.ticketFileName;
      if (docId === this.resolveKycDocumentId() && storedName && !this.ticketKycDisplayName) {
        this.ticketKycDisplayName =
          this.humanizeKycStoredFileName(storedName) || this.ticketKycDisplayName;
      }
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

  canPreviewKycDocument(): boolean {
    return !!(this.selectedFile || this.resolveKycDocumentId());
  }

  resolveKycDocumentId(): number | null {
    if (this.ticketKycDocumentId != null) {
      return this.ticketKycDocumentId;
    }
    const raw = this.travelDeskObj?.kycDocumentId;
    if (raw == null || raw === '') {
      return null;
    }
    const id = Number(raw);
    return isNaN(id) ? null : id;
  }

  async previewKycDocument(alertTemplate: TemplateRef<any>): Promise<void> {
    this.revokeKycObjectUrl();
    this.draftDocPreview = null;
    if (this.selectedFile) {
      const preview = await this.buildLocalFilePreview(this.selectedFile);
      if (!preview) {
        this.openAlertMod(alertTemplate, 'Preview is not available for this file type.');
        return;
      }
      this.draftDocPreview = preview;
      this.openKycPreviewModal();
      return;
    }
    const docId = this.resolveKycDocumentId();
    if (docId == null) {
      this.openAlertMod(alertTemplate, 'Select or upload a KYC document first.');
      return;
    }
    this.openKycPreviewModal();
    await this.previewDraftDocument(docId);
    if (!this.draftDocPreview) {
      this.closeKycPreviewModal();
      this.openAlertMod(alertTemplate, 'Could not load KYC document preview.');
    }
  }

  private buildLocalFilePreview(file: File): Promise<SafeResourceUrl | string | null> {
    const isPdf =
      file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (isPdf) {
      this.revokeKycObjectUrl();
      this.kycPreviewObjectUrl = URL.createObjectURL(file);
      return Promise.resolve(
        this.sanitizer.bypassSecurityTrustResourceUrl(this.kycPreviewObjectUrl)
      );
    }
    const isImage =
      file.type.startsWith('image/') ||
      /\.(jpe?g|png|gif|bmp|webp|tiff?|svg)$/i.test(file.name);
    if (!isImage) {
      return Promise.resolve(null);
    }
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = () => resolve((reader.result as string) || null);
      reader.onerror = () => resolve(null);
      reader.readAsDataURL(file);
    });
  }

  openKycPreviewModal(): void {
    this.kycPreviewModalRef?.close();
    this.kycPreviewModalRef = this.modalService.open(
      this.kycDocumentPreviewModal,
      {
        size: 'lg',
        backdrop: 'static',
        windowClass: 'trv-docs-modal-window'
      }
    );
  }

  closeKycPreviewModal(): void {
    this.kycPreviewModalRef?.close();
    this.kycPreviewModalRef = null;
    this.clearDraftDocPreview();
  }

  private revokeKycObjectUrl(): void {
    if (this.kycPreviewObjectUrl) {
      URL.revokeObjectURL(this.kycPreviewObjectUrl);
      this.kycPreviewObjectUrl = null;
    }
  }

  clearDraftDocPreview(): void {
    this.revokeKycObjectUrl();
    this.draftDocPreview = null;
  }

  isKycPdfPreview(): boolean {
    if (this.selectedFile) {
      return (
        this.selectedFile.type === 'application/pdf' ||
        this.selectedFile.name.toLowerCase().endsWith('.pdf')
      );
    }
    return this.isDraftPdfPreview();
  }

  isDraftPdfPreview(): boolean {
    if (!this.draftDocPreview) {
    return false;
    }
    return this.draftDocPreview.toString().includes('application/pdf');
  }

  removeDocumentFromDraftLine(docIndex: number, alertTemplate: TemplateRef<any>): void {
    if (this.draftDocsLineIndex === null) {
      return;
    }
    const lineIndex = this.draftDocsLineIndex;
    const line = this.ticketLines[lineIndex];
    if (!line?.documents) {
      return;
    }
    line.documents.splice(docIndex, 1);
    line.docIds = line.documents.map((d: { docId: number }) => d.docId);
    if (line.documents.length === 0) {
      this.closeDraftDocumentsModal();
    }
  }

  removeDocumentFromEditingLine(docIndex: number, alertTemplate: TemplateRef<any>): void {
    if (this.editingLineIndex === null) {
      return;
    }
    const lineIndex = this.editingLineIndex;
    const line = this.ticketLines[lineIndex];
    this.ensureLineDocumentsList(line);
    if (!line?.documents?.length) {
      return;
    }
    line.documents.splice(docIndex, 1);
    line.docIds = line.documents.map((d: { docId: number }) => d.docId);
    this.draftDocPreview = null;
  }

  private toDateInputValue(d: any): string | null {
    if (!d) {
      return null;
    }
    if (typeof d === 'string' && d.length >= 10) {
      return d.substring(0, 10);
    }
    const parsed = new Date(d);
    return isNaN(parsed.getTime()) ? null : parsed.toISOString().split('T')[0];
  }

  private buildLinePayload(documents: { docId: number; fileName: string }[]): any {
    return this.buildLinePayloadFromLeg(
      {
        fromLocation: this.travelDeskObj.fromLocation,
        toLocation: this.travelDeskObj.toLocation,
        fromDate: this.travelDeskObj.fromDate
      },
      documents,
      this.travelDeskObj.toDate ||
        (this.tripType === 'oneway' && !this.isHotelLodgingActive() ? this.travelDeskObj.fromDate : null)
    );
  }

  private buildLinePayloadFromLeg(
    leg: { fromLocation: string; toLocation: string; fromDate: string | null },
    documents: { docId: number; fileName: string }[],
    toDateOverride?: string | null
  ): any {
    const selectedPid = Number(this.travelDeskObj.projectId);
    const projectRow = this.mappedProjectsForTravel.find((p) => p.projectId === selectedPid);
    const kycId = this.ticketKycDocumentId ?? (this.travelDeskObj.kycDocumentId
      ? Number(this.travelDeskObj.kycDocumentId) : null);
    if (kycId != null) {
      this.ticketKycDocumentId = kycId;
    }
    const docIds = documents.map((d) => d.docId);
    const segmentToDate =
      toDateOverride != null
        ? toDateOverride
        : this.tripType === 'multicity'
          ? leg.fromDate
          : null;
    return {
      requestType: this.travelDeskObj.associatedTravelRequest,
      travelMode: this.travelDeskObj.travelMode || null,
      travelClass: this.travelDeskObj.travelClass || null,
      tripType: tripTypeCodeFromPlanner(this.bookingTypeTab, this.tripType),
      purpose: (this.travelDeskObj.purposeOfTravel || '').trim(),
      fromLocation: (leg.fromLocation || '').trim() || null,
      toLocation: (leg.toLocation || '').trim() || null,
      fromDate: leg.fromDate,
      toDate: segmentToDate,
      hotelCategory: this.travelDeskObj.hotelCategory || null,
      hotelSubCategory: this.travelDeskObj.tlSubCategory || null,
      city: this.travelDeskObj.selectedCity || null,
      projectId: selectedPid,
      projectName: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID
        ? (projectRow?.projectName || '') : (this.travelDeskObj.othersProjectName || '').trim(),
      clientId: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID && projectRow?.clientId != null
        ? Number(projectRow.clientId)
        : (selectedPid === this.TRAVEL_OTHERS_PROJECT_ID ? Number(this.travelDeskObj.othersClientId) : null),
      clientName: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID
        ? (projectRow?.clientName || this.travelDeskObj.displayClientName || '') : null,
      docIds,
      documents,
      kycDocumentId: this.ticketKycDocumentId,
      kycDisplayName: this.ticketKycDisplayName
    };
  }

  private resetLineFormFields(): void {
    this.travelDeskObj = {
      associatedTravelRequest: '',
      travelMode: '',
      travelClass: '',
      fromDate: null,
      toDate: null,
      purposeOfTravel: '',
      fromLocation: '',
      toLocation: '',
      supportingDocument: null,
      hotelCategory: '',
      tlSubCategory: '',
      selectedCity: '',
      docIds: [],
      kycDocumentId: this.ticketKycDocumentId != null ? String(this.ticketKycDocumentId) : '',
      projectId: null,
      othersProjectName: '',
      othersClientId: null,
      displayClientName: ''
    };
    this.selectedFile = null;
    this.selectedFileName = null;
    this.travelModelistByReason = [];
    this.travelClasslistByReason = [];
    this.hotelSubCategorySelectId = null;
    this.hotelSubCategorylist = [];
    this.citylistBySubCategory = [];
    if (this.tripType === 'multicity') {
      this.multiCityLegs = [this.defaultMultiCityLeg(), this.defaultMultiCityLeg()];
    }
  }

  async submitEntireTicket(template: TemplateRef<any>): Promise<void> {
    if (this.ticketLines.length < 1) {
      this.openAlertMod(template, 'Use "Add To Ticket" to add at least one Travel Request before submitting.');
      return;
    }
    if (!this.currentUser?.hodId || !this.currentUser?.hodEmail) {
      this.openAlertMod(template, 'HOD information is missing on your profile. Please contact HR.');
      return;
    }
    const lines = this.ticketLines.map((ln) => ({
      requestType: ln.requestType,
      travelMode: ln.travelMode,
      travelClass: ln.travelClass,
      tripType: ln.tripType,
      purpose: ln.purpose,
      fromLocation: ln.fromLocation,
      toLocation: ln.toLocation,
      fromDate: ln.fromDate ? new Date(ln.fromDate).toISOString() : null,
      toDate: ln.toDate ? new Date(ln.toDate).toISOString() : null,
      hotelCategory: ln.hotelCategory,
      hotelSubCategory: ln.hotelSubCategory,
      city: ln.city,
      projectId: ln.projectId,
      projectName: ln.projectName,
      clientId: ln.clientId,
      clientName: ln.clientName,
      docIds: ln.docIds || [],
      kycDocumentId: ln.kycDocumentId ?? this.ticketKycDocumentId
    }));
    const body = {
      empId: this.currentEmployeeInfo.empId,
      fullName: this.currentEmployeeInfo.name,
      email: this.currentEmployeeInfo.email,
      departmentName: this.currentEmployeeInfo.departmentName,
      designationName: this.currentEmployeeInfo.designationName,
      mobileNo: this.currentEmployeeInfo.mobileNo != null ? String(this.currentEmployeeInfo.mobileNo) : '',
      managerEmpId: this.currentUser.hodId,
      managerName: this.currentUser.hodName,
      managerEmail: this.currentUser.hodEmail,
      lines
    };
    try {
      const response: any = await this.travelDesk.saveTravelDeskTicket(body).pipe(first()).toPromise();
      if (response.serviceStatus === 'Success') {
        const ref = response.serviceResponse?.ticketNo || response.serviceResponse?.ticketId;
    this.ticketLines = [];
    this.ticketKycDocumentId = null;
    this.ticketKycDisplayName = null;
    this.editingLineIndex = null;
        this.resetLineFormFields();
        this.openAlertMod(
          template,
          ref ? `Success! Your Travel Ticket was registered: ${ref}` : 'Success! Your Travel Ticket was submitted.'
        );
      } else {
        this.openAlertMod(template, response.serviceError || response.serviceResponse || 'Submit failed.');
      }
    } catch (error: any) {
      console.error('Error during ticket submit:', error);
      const msg =
        error?.error?.serviceError ||
        error?.error?.serviceResponse ||
        error?.message ||
        'An unexpected error occurred while submitting the ticket.';
      this.openAlertMod(template, msg);
    }
  }

  resetForm(template: TemplateRef<any>) {
    this.editingLineIndex = null;
    this.draftDocPreview = null;
    this.bookingTypeTab = 'travel';
    this.tripType = 'round';
    this.plannerStep = 'route';
    this.multiCityLegs = [this.defaultMultiCityLeg(), this.defaultMultiCityLeg()];
    this.travelDeskObj = {
      associatedTravelRequest: '',
      travelMode: '',
      travelClass: '',
      fromLocation: '',
      toLocation: '',
      fromDate: '',
      toDate: '',
      purposeOfTravel: '',
      supportingDocument: null,
      hotelCategory: '',
      tlSubCategory: '',
      selectedCity: '',
      docIds: [],
      kycDocumentId: '',
      projectId: null,
      othersProjectName: '',
      othersClientId: null,
      displayClientName: ''
    };
    this.travelModelistByReason = [];
    this.travelClasslistByReason = [];
    this.alertMessage = `Your form data has been successfully reset  !!!!!!`;
    this.openAlertMod(template, this.alertMessage);

  }


  isValidForm(): boolean {
    return this.validateLineForPolicy() === null;
  }

  validateLineForPolicy(): string | null {
    if (this.isMultiCityTravelPlanner()) {
      return this.validateMultiCityLegsForPolicy();
    }
    const zoneToday = new Date();
    zoneToday.setHours(0, 0, 0, 0);
    const minDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_ADVANCE_DAYS);
    const maxDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_MAX_FUTURE_DAYS);
    const minDate = parseIsoDate(minDateStr);
    const maxDate = parseIsoDate(maxDateStr);

    if (this.isHotelLodgingActive()) {
      if (!this.travelDeskObj.hotelCategory) {
        return 'Please select a hotel category.';
      }
      if (!this.travelDeskObj.tlSubCategory) {
        return 'Please select a hotel sub-category.';
      }
      if (!this.travelDeskObj.selectedCity) {
        return 'Please select a city.';
      }
    } else {
      if (!this.travelDeskObj.associatedTravelRequest) {
        return 'Please select a travel reason.';
      }
      if (!this.travelDeskObj.travelMode) {
        return 'Please select a travel mode.';
      }
      if (!this.travelDeskObj.travelClass) {
        return 'Please select a travel class.';
      }
      if (!(this.travelDeskObj.fromLocation || '').trim()) {
        return 'Please enter from location.';
      }
      if (!(this.travelDeskObj.toLocation || '').trim()) {
        return 'Please enter to location.';
      }
    }

    if (!this.travelDeskObj.fromDate) {
      return this.isHotelLodgingActive() ? 'Please select check-in date.' : 'Please select departure date.';
    }
    const from = parseIsoDate(this.travelDeskObj.fromDate);
    if (!from || !minDate || !maxDate) {
      return 'Invalid date.';
    }
    if (from < minDate) {
      return `Travel must be planned at least ${TRAVEL_POLICY_ADVANCE_DAYS} days in advance (ApMoSys Travel Policy).`;
    }
    if (from > maxDate) {
      return `Start date cannot be more than ${TRAVEL_POLICY_MAX_FUTURE_DAYS} days from today.`;
    }

    const needsReturn =
      this.isHotelLodgingActive() ||
      (!isHotelTravelReasonName(this.travelDeskObj.associatedTravelRequest) &&
        this.tripType !== 'oneway' &&
        this.tripType !== 'multicity');
    if (needsReturn) {
      if (!this.travelDeskObj.toDate) {
        return this.isHotelLodgingActive() ? 'Please select check-out date.' : 'Please select return date.';
      }
      const to = parseIsoDate(this.travelDeskObj.toDate);
      if (!to) {
        return 'Invalid end date.';
      }
      if (to < from) {
        return 'End date cannot be before start date.';
      }
      if (to > maxDate) {
        return `End date cannot be more than ${TRAVEL_POLICY_MAX_FUTURE_DAYS} days from today.`;
      }
    }

    if (!(this.travelDeskObj.purposeOfTravel || '').trim()) {
      return 'Please enter the purpose of travel.';
    }
    return null;
  }

  private getCompleteMultiCityLegs(): { fromLocation: string; toLocation: string; fromDate: string | null }[] {
    return this.multiCityLegs.filter(
      (leg) =>
        (leg.fromLocation || '').trim() &&
        (leg.toLocation || '').trim() &&
        !!leg.fromDate
    );
  }

  private validateMultiCityLegsForPolicy(): string | null {
    const zoneToday = new Date();
    zoneToday.setHours(0, 0, 0, 0);
    const minDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_ADVANCE_DAYS);
    const maxDateStr = addDaysIso(zoneToday, TRAVEL_POLICY_MAX_FUTURE_DAYS);
    const minDate = parseIsoDate(minDateStr);
    const maxDate = parseIsoDate(maxDateStr);

    if (!this.travelDeskObj.associatedTravelRequest) {
      return 'Please select a travel reason.';
    }
    if (!this.travelDeskObj.travelMode) {
      return 'Please select a travel mode.';
    }
    if (!this.travelDeskObj.travelClass) {
      return 'Please select a travel class.';
    }
    if (!(this.travelDeskObj.purposeOfTravel || '').trim()) {
      return 'Please enter the purpose of travel.';
    }
    const legs = this.getCompleteMultiCityLegs();
    if (legs.length < 2) {
      return 'Add at least two complete city segments for a multi-city trip.';
    }
    let prevDate: Date | null = null;
    for (let i = 0; i < legs.length; i++) {
      const leg = legs[i];
      const label = `City ${i + 1}`;
      if (!(leg.fromLocation || '').trim()) {
        return `Please enter from location for ${label}.`;
      }
      if (!(leg.toLocation || '').trim()) {
        return `Please enter to location for ${label}.`;
      }
      if (!leg.fromDate) {
        return `Please select departure date for ${label}.`;
      }
      const from = parseIsoDate(leg.fromDate);
      if (!from || !minDate || !maxDate) {
        return `Invalid date for ${label}.`;
      }
      if (from < minDate) {
        return `${label}: travel must be planned at least ${TRAVEL_POLICY_ADVANCE_DAYS} days in advance.`;
      }
      if (from > maxDate) {
        return `${label}: date cannot be more than ${TRAVEL_POLICY_MAX_FUTURE_DAYS} days from today.`;
      }
      if (prevDate && from < prevDate) {
        return 'Multi-city departure dates must be in chronological order.';
      }
      prevDate = from;
    }
    return null;
  }

  get policyAdvanceHint(): string {
    return `Requests must be submitted at least ${TRAVEL_POLICY_ADVANCE_DAYS} days before travel (policy).`;
  }

  get policyMetroHint(): string | null {
    if (this.bookingTypeTab !== 'hotel' || this.travelDeskObj.hotelCategory) {
      return null;
    }
    const suggested = suggestedMetroHotelCategory(this.currentEmployeeInfo?.designationName);
    return suggested ? `Suggested metro tier: ${suggested} (from your designation).` : null;
  }

  hotelCategoryOptionLabel(category: { hotelCategory?: string; description?: string }): string {
    return hotelCategoryLabel(category);
  }

  onSubCategoryChange(subCategory: string) {
    switch (subCategory) {
      case 'Metros':
        this.cityOptions = [
          'Bengaluru', 'Chennai', 'Delhi', 'Hyderabad', 'Kolkata', 'Mumbai'
        ];
        break;
      case 'Tier 1':
        this.cityOptions = [
          'Agra', 'Ahmedabad', 'Amritsar', 'Baroda', 'Bhubaneswar', 'Chandigarh',
          'Coimbatore', 'Gangtok', 'Guwahati', 'Indore', 'Jaipur', 'Lucknow',
          'Nagpur', 'Panaji', 'Patna', 'Pune', 'Srinagar', 'Trivendrum', 'Udaipur'
        ];
        break;
      case 'Tier 2':
        this.cityOptions = [
          'Bhopal', 'Cuttack', 'Ghaziabad', 'Jamshedpur', 'Jodhpur', 'Jammu',
          'Kanpur', 'Ludhiana', 'Madurai', 'Mangalore', 'Mohali', 'Nasik',
          'Rajkot', 'Ranchi', 'Siliguri', 'Surat', 'Varanasi', 'Vijaywada', 'Vizag'
        ];
        break;
      default:
        this.cityOptions = [];
    }
  }


  // onFileChange(event: any, template: TemplateRef<any>) {
  //   const file = event.target.files[0];
  //   if (file) {
  //     this.travelDeskObj.supportingDocument = file;
  //   }
  //     if (!this.travelDeskObj.supportingDocument) {
  //       this.openAlertMod(template, "Please upload a file.");
  //       return;
  //     }

  //     const formData = new FormData();
  //     formData.append('file', this.travelDeskObj.supportingDocument);

  //     this.domainService.employeeBulkUpload(formData).pipe(first()).subscribe(
  //       (response: any) => {
  //         if (response.serviceStatus === "Success") {
  //           this.openAlertMod(template, response.serviceResponse);
  //         } else if (response.serviceStatus === "Fail") {
  //           this.openAlertMod(template, `Error found: ${response.serviceResponse}`);
  //           event.target.value = '';
  //         } else {
  //           this.openAlertMod(template, response.serviceResponse);
  //         }
  //       },
  //       (error) => {
  //         this.openAlertMod(template, "An error occurred while processing the upload.");
  //         console.error(error);
  //       }
  //     );
  // }

  //pagination
  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.travelDeskObj.supportingDocument = file;
    }
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
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

  async onGetTravelReason() {

    const response: any = await this.travelDesk.getTravelReason().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelReasonlist = (response.serviceResponse || []).filter(isActiveMasterRow);

      console.log("travelReasonlist   ::::::: : ", this.travelReasonlist);

    } else {
      console.error(response.serviceResponse);
    }
  }


  showPlannerDetails = false;
  /** Step 1: route & dates only; step 2: policy fields and add to ticket. */
  plannerStep: 'route' | 'details' = 'route';
  tripType: 'oneway' | 'round' | 'multicity' = 'round';
  multiCityLegs: { fromLocation: string; toLocation: string; fromDate: string | null }[] = [
    { fromLocation: '', toLocation: '', fromDate: null },
    { fromLocation: '', toLocation: '', fromDate: null }
  ];
  bookingTypeTab: 'travel' | 'hotel' = 'travel';
  /** When switching to Hotels after a flight line, try to select this city once the city list loads. */
  private pendingHotelCityFromTravel: string | null = null;
  /** Travel line edit paused while user switches to Hotels tab on same ticket. */
  private pausedEditLineIndex: number | null = null;
  /** Suppresses dropdown selectionChange while beginEditDraftLine repopulates fields. */
  private restoringDraftLineEdit = false;
  readonly HOTEL_REASON_NAME = 'Hotel & Lodging';

  setTripType(type: 'oneway' | 'round' | 'multicity'): void {
    if (type === 'multicity' && this.tripType !== 'multicity') {
      this.ensureMultiCityLegsFromPlanner();
    }
    if (type !== 'multicity' && this.tripType === 'multicity') {
      this.syncPlannerFromFirstMultiCityLeg();
    }
    this.tripType = type;
    this.resetPlannerToRouteStep();
    if (type === 'oneway') {
      this.travelDeskObj.toDate = null;
    }
    if (type === 'multicity') {
      this.travelDeskObj.toDate = null;
    }
  }

  isMultiCityTravelPlanner(): boolean {
    return this.bookingTypeTab === 'travel' && this.tripType === 'multicity';
  }

  private defaultMultiCityLeg(): { fromLocation: string; toLocation: string; fromDate: string | null } {
    return { fromLocation: '', toLocation: '', fromDate: null };
  }

  private ensureMultiCityLegsFromPlanner(): void {
    if (this.multiCityLegs.length >= 2) {
      return;
    }
    const from = (this.travelDeskObj.fromLocation || '').trim();
    const to = (this.travelDeskObj.toLocation || '').trim();
    const date = this.travelDeskObj.fromDate || null;
    this.multiCityLegs = [
      { fromLocation: from, toLocation: to, fromDate: date },
      { fromLocation: to, toLocation: '', fromDate: null }
    ];
  }

  private syncPlannerFromFirstMultiCityLeg(): void {
    const first = this.multiCityLegs[0];
    if (!first) {
        return;
    }
    this.travelDeskObj.fromLocation = first.fromLocation;
    this.travelDeskObj.toLocation = first.toLocation;
    this.travelDeskObj.fromDate = first.fromDate;
  }

  addMultiCityLeg(): void {
    if (this.multiCityLegs.length >= 5) {
      return;
    }
    const prev = this.multiCityLegs[this.multiCityLegs.length - 1];
    this.multiCityLegs.push({
      fromLocation: (prev?.toLocation || '').trim(),
      toLocation: '',
      fromDate: null
    });
  }

  removeMultiCityLeg(index: number): void {
    if (this.multiCityLegs.length <= 2 || index < 0 || index >= this.multiCityLegs.length) {
      return;
    }
    this.multiCityLegs.splice(index, 1);
  }

  swapMultiCityLegLocations(index: number): void {
    const leg = this.multiCityLegs[index];
    if (!leg) {
      return;
    }
    const from = leg.fromLocation;
    leg.fromLocation = leg.toLocation;
    leg.toLocation = from;
  }

  isReturnDateDisabled(): boolean {
    return this.tripType === 'oneway' || this.tripType === 'multicity' || !this.travelDeskObj.fromDate;
  }

  formatPlannerDate(value: string | Date | null): string {
    if (!value) {
      return '';
    }
    const dt = value instanceof Date ? value : new Date(value);
    if (isNaN(dt.getTime())) {
      return '';
    }
    return dt.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      weekday: 'long'
    });
  }

  swapLocations(): void {
    const from = this.travelDeskObj.fromLocation;
    this.travelDeskObj.fromLocation = this.travelDeskObj.toLocation;
    this.travelDeskObj.toLocation = from;
  }

  /** Name of the hotel travel-reason row in master data (fuzzy match). */
  resolveHotelReasonName(): string | null {
    const list = this.travelReasonlist || [];
    const exact = list.find(
      (r) => (r?.travelReasonName || '').trim() === this.HOTEL_REASON_NAME
    );
    if (exact?.travelReasonName) {
      return String(exact.travelReasonName).trim();
    }
    const fuzzy = list.find((r) => this.isHotelTravelReasonName(r?.travelReasonName));
    return fuzzy?.travelReasonName ? String(fuzzy.travelReasonName).trim() : null;
  }

  get hotelReasonName(): string {
    return this.resolveHotelReasonName() || this.HOTEL_REASON_NAME;
  }

  isHotelTravelReasonName(name: string | null | undefined): boolean {
    if (!name) {
      return false;
    }
    const n = String(name).toLowerCase().trim();
    if (n === this.HOTEL_REASON_NAME.toLowerCase()) {
      return true;
    }
    return n.includes('hotel') && (n.includes('lodg') || n.includes('accommod') || n.includes('stay'));
  }

  isHotelTravelReason(name: string | null | undefined): boolean {
    if (this.bookingTypeTab === 'hotel') {
      return true;
    }
    const resolved = this.resolveHotelReasonName();
    if (resolved && name === resolved) {
      return true;
    }
    return this.isHotelTravelReasonName(name);
  }

  hasHotelBookingOption(): boolean {
    if (this.resolveHotelReasonName()) {
      return true;
    }
    const cats = this.hotelCategorylist;
    return Array.isArray(cats) && cats.length > 0;
  }

  get travelReasonsForTravelTab(): any[] {
    return (this.travelReasonlist || []).filter(
      (r) => !this.isHotelTravelReasonName(r?.travelReasonName)
    );
  }

  isHotelLodgingActive(): boolean {
    return this.bookingTypeTab === 'hotel';
  }

  selectBookingTab(tab: 'travel' | 'hotel'): void {
    if (tab === this.bookingTypeTab) {
      return;
    }

    if (this.editingLineIndex !== null) {
      const ln = this.ticketLines[this.editingLineIndex];
      const editingHotel = this.isHotelTravelReasonName(ln?.requestType);
      if (tab === 'hotel' && !editingHotel) {
        this.pausedEditLineIndex = this.editingLineIndex;
        this.editingLineIndex = null;
      } else if (tab === 'travel' && editingHotel) {
        this.cancelEditDraftLine();
      }
    }

    if (tab === 'travel' && this.pausedEditLineIndex !== null) {
      const idx = this.pausedEditLineIndex;
      this.pausedEditLineIndex = null;
      this.bookingTypeTab = tab;
      void this.beginEditDraftLine(idx);
      return;
    }

    this.bookingTypeTab = tab;

    if (tab === 'hotel') {
      const hotelReason = this.hotelReasonName;
      this.travelDeskObj.associatedTravelRequest = hotelReason || '';
      this.travelDeskObj.travelMode = '';
      this.travelDeskObj.travelClass = '';
      this.travelModelistByReason = [];
      this.travelClasslistByReason = [];
      this.prefillHotelContextFromDraftTravel();
      this.plannerStep = 'route';
      void this.refreshTicketKycAttachmentUi().then(() => {
        if (this.ticketKycDocumentId) {
          this.showPlannerDetails = true;
        }
      });
      void this.onTravelReasonChange(hotelReason || '', { preserveMode: true, preserveClass: true });
      void this.onGetHotelCategory();
      return;
    }

    this.pendingHotelCityFromTravel = null;
    if (this.isHotelTravelReasonName(this.travelDeskObj.associatedTravelRequest)) {
      this.travelDeskObj.hotelCategory = '';
      this.travelDeskObj.tlSubCategory = '';
      this.travelDeskObj.selectedCity = '';
      this.hotelSubCategorySelectId = null;
      this.hotelSubCategorylist = [];
      this.citylistBySubCategory = [];
    }
    if (this.editingLineIndex === null) {
      this.resetPlannerToRouteStep();
    }
  }

  private syncBookingTabFromReason(): void {
    this.bookingTypeTab = this.isHotelTravelReasonName(this.travelDeskObj.associatedTravelRequest)
      ? 'hotel'
      : 'travel';
  }

  /** Copy project, dates, and destination city hint from the latest flight line on the draft ticket. */
  private prefillHotelContextFromDraftTravel(): void {
    const lastTravel = [...this.ticketLines]
      .reverse()
      .find((ln) => !this.isHotelTravelReasonName(ln?.requestType));
    if (!lastTravel) {
      return;
    }
    if (lastTravel.projectId != null && this.travelDeskObj.projectId == null) {
      this.travelDeskObj.projectId = lastTravel.projectId;
      this.travelDeskObj.othersProjectName = lastTravel.projectName || '';
      this.travelDeskObj.displayClientName = lastTravel.clientName || '';
    }
    if (!this.travelDeskObj.purposeOfTravel && lastTravel.purpose) {
      this.travelDeskObj.purposeOfTravel = lastTravel.purpose;
    }
    if (!this.travelDeskObj.fromDate && lastTravel.fromDate) {
      this.travelDeskObj.fromDate = this.toDateInputValue(lastTravel.fromDate);
    }
    if (!this.travelDeskObj.toDate && lastTravel.toDate) {
      this.travelDeskObj.toDate = this.toDateInputValue(lastTravel.toDate);
    }
    const dest = (lastTravel.toLocation || '').trim();
    if (dest) {
      this.pendingHotelCityFromTravel = dest;
      this.tryApplyPendingHotelCity();
    }
  }

  private tryApplyPendingHotelCity(): void {
    const target = (this.pendingHotelCityFromTravel || '').trim();
    if (!target || !this.citylistBySubCategory?.length) {
      return;
    }
    const norm = (s: string) => s.toLowerCase().replace(/\s+/g, ' ');
    const t = norm(target);
    const match = this.citylistBySubCategory.find((c: { cityName?: string }) => {
      const name = norm(String(c?.cityName || ''));
      return name === t || name.includes(t) || t.includes(name);
    });
    if (match?.cityName) {
      this.travelDeskObj.selectedCity = match.cityName;
      this.pendingHotelCityFromTravel = null;
    }
  }

  travelTabIcon(reasonName: string): string {
    const n = (reasonName || '').toLowerCase();
    if (n.includes('hotel') || n.includes('lodging')) {
      return 'fa-bed';
    }
    if (n.includes('bus')) {
      return 'fa-bus';
    }
    if (n.includes('train')) {
      return 'fa-subway';
    }
    if (n.includes('cab') || n.includes('car')) {
      return 'fa-car';
    }
    return 'fa-plane';
  }

  formatLineDateRange(line: any): string {
    const fmt = (d: any) => {
      if (!d) {
        return '—';
      }
      const dt = new Date(d);
      if (isNaN(dt.getTime())) {
        return String(d);
      }
      return dt.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    };
    const a = fmt(line.fromDate);
    const b = fmt(line.toDate);
    return a === b ? a : `${a} – ${b}`;
  }

  lineRouteLabel(line: any): string {
    if (this.isHotelTravelReasonName(line.requestType)) {
      const city = line.city || '—';
      return line.hotelCategory ? `${city} · ${line.hotelCategory}` : city;
    }
    const from = line.fromLocation || '—';
    const to = line.toLocation || '—';
    const route = `${from} → ${to}`;
    if ((line.tripType || '').toUpperCase() === 'MULTI_CITY') {
      const mcLines = this.ticketLines.filter(
        (l) => (l.tripType || '').toUpperCase() === 'MULTI_CITY'
      );
      const idx = mcLines.indexOf(line);
      if (mcLines.length > 1 && idx >= 0) {
        return `Leg ${idx + 1}: ${route}`;
      }
      return `Multi-city · ${route}`;
    }
    return route;
  }

  lineBookingSummary(line: any): string {
    const trip = tripTypeDisplayLabel(
      line?.tripType,
      line,
      this.ticketLines.filter((l) => !this.isHotelTravelReasonName(l.requestType)).length
    );
    const mode = lineTravelModeDisplay(line);
    const cls = lineTravelClassDisplay(line);
    const parts = [trip, mode, cls].filter((p) => p && p !== '—');
    return parts.length ? parts.join(' · ') : '—';
  }

  onTravelReasonSelected(_event?: unknown): void {
    if (this.restoringDraftLineEdit) {
      return;
    }
    if (this.isHotelTravelReasonName(this.travelDeskObj.associatedTravelRequest)) {
      this.bookingTypeTab = 'hotel';
    } else if (this.bookingTypeTab === 'hotel') {
      this.bookingTypeTab = 'travel';
    }
    void this.onTravelReasonChange(this.travelDeskObj.associatedTravelRequest);
  }

  onTravelModeSelected(_event?: unknown): void {
    if (this.restoringDraftLineEdit) {
      return;
    }
    void this.onModeChange(this.travelDeskObj.travelMode);
  }

  async onTravelReasonChange(
    selectedTravelReasons: string,
    options?: { preserveMode?: boolean; preserveClass?: boolean }
  ) {
    if (!options?.preserveMode) {
    this.travelDeskObj.travelMode = '';
    }
    if (!options?.preserveClass) {
    this.travelDeskObj.travelClass = '';
    }
    this.travelModelistByReason = [];
    if (!options?.preserveClass) {
    this.travelClasslistByReason = [];
    }
    if (this.isHotelTravelReasonName(selectedTravelReasons)) {
      void this.onGetHotelCategory();
    }
    try {
      const response: any = await this.travelDesk.getTravelModeByReason(selectedTravelReasons).toPromise();
      if (response.serviceStatus === "Success") {
        this.travelModelistByReason = (response.serviceResponse || []).filter(isActiveMasterRow);
        console.log("Filtered travel modes:", this.travelModelistByReason);
      } else {
        console.error("Failed to fetch travel modes:", response.serviceResponse);
      }

    } catch (error) {
      console.error("Error fetching travel modes:", error);
    }
  }


  async onModeChange(selectedTravelMode: string, options?: { preserveClass?: boolean }) {
    if (!options?.preserveClass) {
    this.travelDeskObj.travelClass = '';
    }
    this.travelClasslistByReason = [];
    const mode = this.travelModelistByReason?.find((m: any) => m.modeType === selectedTravelMode);
    if (!mode?.travelModeId) {
      return;
    }
    try {
      const response: any = await this.travelDesk.getTravelClassByMode(mode.travelModeId).toPromise();
      if (response.serviceStatus === 'Success') {
        this.travelClasslistByReason = (response.serviceResponse || []).filter(isActiveMasterRow);
      } else {
        console.error('Failed to fetch travel classes:', response.serviceError || response.serviceResponse);
      }
    } catch (error) {
      console.error('Error fetching travel classes:', error);
    }
  }



  async onGetHotelCategory() {

    const response: any = await this.travelDesk.getHotelCategory().toPromise();
    if (response.serviceStatus == "Success") {
      this.hotelCategorylist = (response.serviceResponse || []).filter(isActiveMasterRow);

      console.log("hotelCategorylist   ::::::: : ", this.hotelCategorylist);

    } else {
      console.error(response.serviceResponse);
    }
  }



  async onHotelCategoryChange(categoryName: string): Promise<void> {
    this.hotelSubCategorySelectId = null;
    this.travelDeskObj.tlSubCategory = '';
    this.travelDeskObj.selectedCity = '';
    this.hotelSubCategorylist = [];
    this.citylistBySubCategory = [];
    if (!(categoryName || '').trim()) {
      return;
    }
    try {
      const response: any = await this.travelDesk
        .getHotelSubCategoryByCategory(categoryName.trim())
        .toPromise();
      if (response.serviceStatus === 'Success') {
        this.hotelSubCategorylist = (response.serviceResponse || []).filter(isActiveMasterRow);
    } else {
        console.error('Failed to fetch hotel sub-categories:', response.serviceError || response.serviceResponse);
      }
    } catch (error) {
      console.error('Error fetching hotel sub-categories:', error);
    }
  }

  async onSubCategoryChanges(subCategoryId: string | number): Promise<void> {
    const id = Number(subCategoryId);
    const preserveCity = (this.travelDeskObj.selectedCity || '').trim();
    this.citylistBySubCategory = [];
    this.travelDeskObj.selectedCity = '';
    const sub = (this.hotelSubCategorylist || []).find((s: { id?: number }) => Number(s.id) === id);
    this.travelDeskObj.tlSubCategory = sub?.hotelSubCategoryName || '';
    if (!id || isNaN(id)) {
      return;
    }
    try {
      const response: any = await this.travelDesk.getCitiesByHotelSubCategoryId(id).toPromise();
      if (response.serviceStatus === 'Success') {
        this.citylistBySubCategory = (response.serviceResponse || []).filter(isActiveMasterRow);
        this.tryApplyPendingHotelCity();
        if (!(this.travelDeskObj.selectedCity || '').trim() && preserveCity) {
          this.travelDeskObj.selectedCity = preserveCity;
        }
      } else {
        console.error('Failed to fetch cities:', response.serviceError || response.serviceResponse);
      }
    } catch (error) {
      console.error('Error fetching cities:', error);
    }
  }


  selectedFileName: string | null = null;
  selectedFile: File | null = null;
  maxFileSizeMB = 1;

  onFileSelected(event: Event, template: TemplateRef<any>): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      const allowedTypes = [

        'application/pdf',
        'image/jpeg',
        'image/jpg',
        'image/png',
        'image/gif',
        'image/bmp',
        'image/webp',
        'image/tiff',
        'image/tif',
        'image/svg+xml',
        'application/postscript',
      ];

      const allowedExtensions = [
        '.pdf',
        '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp',
        '.tiff', '.tif', '.svg', '.eps'
      ];


      const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();

      // Validate file type
      if (!allowedTypes.includes(file.type) && !allowedExtensions.includes(fileExtension)) {
        this.openAlertMod(template, 'Please select only PDF files, images or scanned copies.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      if (file.size > this.maxFileSizeMB * 1024 * 1024) {
        this.openAlertMod(template, 'File size should not exceed 1 MB.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      if (file.size === 0) {
        this.openAlertMod(template, 'Selected file is empty. Please choose a valid file.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      this.selectedFileName = file.name;
      this.selectedFile = file;

      console.log("KYC Document - Valid file selected:", {
        name: this.selectedFileName,
        type: file.type,
        size: `${(file.size / 1024 / 1024).toFixed(2)} MB`,
        extension: fileExtension
      });

    } else {

      this.selectedFileName = null;
      this.selectedFile = null;
    }
  }


  private getFileTypeDescription(file: File): string {
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();

    if (file.type === 'application/pdf' || extension === '.pdf') {
      return 'PDF Document';
    } else if (file.type.startsWith('image/')) {
      return 'Image File';
    } else {
      return 'Document';
    }
  }


   


}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

