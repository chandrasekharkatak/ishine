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
/** Master lists before job-role filtering. */
allTravelModeList: any[] = [];
allVehicleTypeList: any[] = [];
allFoodTypeList: any[] = [];
/** Resolved limits/eligibility for logged-in employee's job role. */
rolePolicy: any = null;

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
    businessJustification: '',
    hodApproval: false,
    preApprovalDate: null as string | null,
    fromLocation: '',
    toLocation: '',
    supportingDocument: null,
    kilometers: null,
    foodAllowanceType: null,
    teamMemberCount: null as number | null,
    vehicleType: '',
    othersProjectName: '',
    pocProjectName: '',
    othersClientId: null as number | null,
    clientPickerKey: null as string | null,
    prospectiveClientName: '',
    recurringExpense: false,
    pocProject: false,
    displayClientName: '',
    displayPoNo: '',
    expenditureTypeDescription: ''
  };

  /** Projects for claim: team mapping, department-linked, optional "Others". */
  mappedProjectsForClaim: { projectId: number; projectName: string; clientName?: string; clientId?: number | null; poNo?: string }[] = [];
  /** How {@link #mappedProjectsForClaim} was built (for empty-state messaging). */
  projectPickerSource: 'TEAM' | 'DEPARTMENT' | null = null;
  /** Master + reimbursement clients for Others / POC picker. */
  clientsForManualProject: {
    pickerKey: string;
    clientId?: number | null;
    reimbursementClientId?: number | null;
    clientName: string;
    clientCategory?: string;
    prospectiveEntry?: boolean;
  }[] = [];
  readonly RMB_OTHERS_PROJECT_ID = -1;
  readonly RMB_POC_PROJECT_ID = -2;
  readonly PROSPECTIVE_CLIENT_PICKER_KEY = 'PROSPECTIVE_NEW';

  /** Text binding for total amount (digits and one decimal only; synced to reimbursementObj.amount). */
  claimAmountInput = '';

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
  /** Always true: late submissions are auto-carried to the next processing cycle. */
  submissionWindowAllowed = true;
  submissionWindowMessage = '';
  carriedToNextCycle = false;
  processingCycleLabel = '';

  isClaimModeSelected(): boolean {
    return !!this.reimbursementObj?.recurringExpense || !!this.reimbursementObj?.pocProject;
  }

  /**
   * UI constraint: pre-approval must be strictly before From Date (when From Date exists).
   * Returns yyyy-MM-dd max for date input, else null.
   */
  preApprovalDateMax(): string | null {
    const from = this.reimbursementObj?.fromDate;
    if (!from) {
      return null;
    }
    // from is yyyy-MM-dd; use noon to avoid timezone edge cases
    const d = new Date(String(from) + 'T12:00:00');
    if (Number.isNaN(d.getTime())) {
      return null;
    }
    d.setDate(d.getDate() - 1);
    return this.formatDate(d);
  }

  ngOnInit(): void {
    this.refreshClaimDateBounds();
    void this.loadSubmissionWindowStatus();
    if (this.currentUser?.empId != null) {
      void this.loadMappedProjectsForReimbursement();
    }
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
    this.claimAmountInput = '';
    this.reimbursementObj.travelMode =null;
    this.reimbursementObj.vehicleType = null;
    this.reimbursementObj.foodAllowanceType = null;
    this.reimbursementObj.teamMemberCount = null;
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
    await this.loadRolePolicyForEmployee();
  }

  private async loadRolePolicyForEmployee(): Promise<void> {
    const empId = this.currentEmployeeInfo?.empId ?? this.currentUser?.empId;
    if (empId == null) {
      return;
    }
    try {
      const res: any = await this.reimbursementService
        .resolveReimbursementRolePolicyForEmployee(Number(empId))
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success') {
        this.rolePolicy = res.serviceResponse || null;
        this.applyRolePolicyFilters();
      }
    } catch {
      this.rolePolicy = null;
    }
  }

  private applyRolePolicyFilters(): void {
    const p = this.rolePolicy;
    if (!p) {
      this.travelModeList = [...(this.allTravelModeList || [])];
      this.vehicleTypeList = [...(this.allVehicleTypeList || [])];
      this.foodTypeList = [...(this.allFoodTypeList || [])];
      return;
    }
    if (p.travelModeRestricted && Array.isArray(p.allowedTravelModes) && p.allowedTravelModes.length) {
      const allowed = new Set(p.allowedTravelModes.map((x: string) => String(x).toLowerCase()));
      this.travelModeList = (this.allTravelModeList || []).filter((t: any) => {
        const name = String(t?.modeType || t?.travelModeName || '').toLowerCase();
        return allowed.has(name);
      });
    } else {
      this.travelModeList = [...(this.allTravelModeList || [])];
    }
    if (p.vehicleTypeRestricted && Array.isArray(p.allowedVehicleTypes) && p.allowedVehicleTypes.length) {
      const allowed = new Set(p.allowedVehicleTypes.map((x: string) => String(x).toLowerCase()));
      this.vehicleTypeList = (this.allVehicleTypeList || []).filter((v: any) =>
        allowed.has(String(v?.vehicleTypeName || '').toLowerCase())
      );
    } else {
      this.vehicleTypeList = [...(this.allVehicleTypeList || [])];
    }
    if (p.foodAllowanceRestricted && Array.isArray(p.allowedFoodAllowanceTypes) && p.allowedFoodAllowanceTypes.length) {
      const allowed = new Set(p.allowedFoodAllowanceTypes.map((x: string) => String(x).toLowerCase()));
      this.foodTypeList = (this.allFoodTypeList || []).filter((f: any) =>
        allowed.has(String(f?.foodTypeName || '').toLowerCase())
      );
    } else {
      this.foodTypeList = [...(this.allFoodTypeList || [])];
    }
    if (this.reimbursementObj?.travelMode && !this.travelModeList.some((t: any) =>
      (t?.modeType || t?.travelModeName) === this.reimbursementObj.travelMode)) {
      this.reimbursementObj.travelMode = null;
    }
    if (this.reimbursementObj?.vehicleType && !this.vehicleTypeList.some((v: any) =>
      v?.vehicleTypeName === this.reimbursementObj.vehicleType)) {
      this.reimbursementObj.vehicleType = null;
    }
    if (this.reimbursementObj?.foodAllowanceType && !this.foodTypeList.some((f: any) =>
      f?.foodTypeName === this.reimbursementObj.foodAllowanceType)) {
      this.reimbursementObj.foodAllowanceType = null;
    }
  }

  /** Per-day amount limit for the employee's job role and expenditure type (not Travel — use travel mode limit). */
  getRoleAmountLimit(expenditureType: string | null | undefined): number | null {
    const exp = expenditureType ? String(expenditureType).trim() : '';
    if (!exp || exp.toLowerCase() === 'travel') {
      return null;
    }
    const limits = this.rolePolicy?.amountLimits;
    if (!limits || limits[exp] == null) {
      return null;
    }
    const n = Number(limits[exp]);
    return Number.isFinite(n) && n > 0 ? n : null;
  }

  /** Team meal / working lunch limit (per member per day). Falls back to regular Food per-day limit. */
  getRoleTeamMealPerMemberPerDayLimit(): number | null {
    const limits = this.rolePolicy?.amountLimits;
    if (limits) {
      // Prefer exact key, but also support legacy / variant labels.
      const direct = limits['Food (Team meal)'];
      const tryParse = (v: any): number | null => {
        const n = Number(v);
        return Number.isFinite(n) && n > 0 ? n : null;
      };
      const parsedDirect = tryParse(direct);
      if (parsedDirect != null) {
        return parsedDirect;
      }
      const norm = (s: any) =>
        String(s ?? '')
          .trim()
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '');
      for (const [k, v] of Object.entries(limits)) {
        const nk = norm(k);
        if (nk.includes('food') && (nk.includes('teammeal') || nk.includes('workinglunch'))) {
          const parsed = tryParse(v);
          if (parsed != null) {
            return parsed;
          }
        }
      }
    }
    return this.getRoleAmountLimit('Food');
  }

  /** Per-day cap for the selected travel mode (Cab, Train, etc.). Not used for Own/Personal Vehicle (₹/km rate applies). */
  getRoleTravelModeDailyLimit(travelMode: string | null | undefined): number | null {
    const mode = travelMode ? String(travelMode).trim() : '';
    if (this.isPersonalVehicleTravelMode(mode)) {
      return null;
    }
    const limits = this.rolePolicy?.travelModeDailyLimits as Record<string, number> | undefined;
    if (!mode || !limits) {
      return null;
    }
    const direct = limits[mode];
    if (direct != null) {
      const n = Number(direct);
      return Number.isFinite(n) && n > 0 ? n : null;
    }
    const modeLower = mode.toLowerCase();
    for (const [key, val] of Object.entries(limits)) {
      if (String(key).trim().toLowerCase() === modeLower) {
        const n = Number(val);
        return Number.isFinite(n) && n > 0 ? n : null;
      }
    }
    return null;
  }

  countClaimInclusiveDays(fromDate: string | null | undefined, toDate: string | null | undefined): number {
    if (!fromDate || !toDate) {
      return 0;
    }
    const fromIso = this.toIsoStartOfDay(fromDate);
    const toIso = this.toIsoStartOfDay(toDate);
    if (!fromIso || !toIso) {
      return 0;
    }
    const from = new Date(fromIso);
    const to = new Date(toIso);
    const diffMs = to.getTime() - from.getTime();
    if (diffMs < 0) {
      return 0;
    }
    return Math.floor(diffMs / (24 * 60 * 60 * 1000)) + 1;
  }

  getRoleEligibleAmountTotal(
    expenditureType: string | null | undefined,
    fromDate: string | null | undefined,
    toDate: string | null | undefined,
    foodAllowanceType?: string | null,
    travelMode?: string | null,
    teamMemberCount?: number | null
  ): number | null {
    const exp = expenditureType ? String(expenditureType).trim() : '';
    if (exp === 'Food' && !this.isTeamMealFoodAllowanceType(foodAllowanceType)) {
      return null;
    }
    const perDay =
      exp.toLowerCase() === 'travel'
        ? this.getRoleTravelModeDailyLimit(travelMode ?? this.reimbursementObj?.travelMode)
        : this.getRoleAmountLimit(expenditureType);
    if (perDay == null) {
      return null;
    }
    const days = this.countClaimInclusiveDays(fromDate, toDate);
    if (days <= 0) {
      return null;
    }
    if (exp === 'Food' && this.isTeamMealFoodAllowanceType(foodAllowanceType)) {
      const members = this.parsedTeamMemberCount(teamMemberCount);
      if (members == null || members < 2) {
        return null;
      }
      const perMemberPerDay = this.getRoleTeamMealPerMemberPerDayLimit();
      if (perMemberPerDay == null) {
        return null;
      }
      return perMemberPerDay * days * members;
    }
    return perDay * days;
  }

  isCombinedDailyFoodAllowanceType(foodAllowanceType: string | null | undefined): boolean {
    if (this.isTeamMealFoodAllowanceType(foodAllowanceType)) {
      return false;
    }
    const n = String(foodAllowanceType || '').trim().toLowerCase();
    return n.includes('breakfast') || n.includes('lunch') || n.includes('dinner');
  }

  isTeamMealFoodAllowanceType(foodAllowanceType?: string | null): boolean {
    const n = String(foodAllowanceType ?? this.reimbursementObj?.foodAllowanceType ?? '').trim().toLowerCase();
    return n.includes('team meal') || n.includes('working lunch');
  }

  parsedTeamMemberCount(value?: number | string | null): number | null {
    const n = Number(value ?? this.reimbursementObj?.teamMemberCount);
    if (!Number.isFinite(n) || n < 1 || !Number.isInteger(n)) {
      return null;
    }
    return n;
  }

  onFoodAllowanceTypeChange(): void {
    if (!this.isTeamMealFoodAllowanceType()) {
      this.reimbursementObj.teamMemberCount = null;
    }
  }

  onTeamMemberCountChange(value: string | number): void {
    const trimmed = String(value ?? '').trim();
    if (!trimmed) {
      this.reimbursementObj.teamMemberCount = null;
      return;
    }
    const n = Number(trimmed);
    this.reimbursementObj.teamMemberCount = Number.isFinite(n) ? Math.trunc(n) : null;
  }

  validateTeamMealLimitMessage(
    amount: number,
    fromDate?: string | null,
    toDate?: string | null,
    teamMemberCount?: number | null,
    foodAllowanceType?: string | null
  ): string | null {
    if (!this.isTeamMealFoodAllowanceType(foodAllowanceType)) {
      return null;
    }
    const perDay = this.getRoleTeamMealPerMemberPerDayLimit();
    if (perDay == null) {
      return null;
    }
    const members = this.parsedTeamMemberCount(teamMemberCount);
    if (members == null || members < 2) {
      return 'Enter the number of team members (minimum 2 for a team meal / working lunch).';
    }
    if (!this.isValidClaimAmount(amount)) {
      return null;
    }
    const days = this.countClaimInclusiveDays(fromDate, toDate);
    const dayFactor = days > 0 ? days : 1;
    const maxTotal = Math.round(perDay * members * dayFactor * 100) / 100;
    const perMemberPerDay = Math.round((amount / (members * dayFactor)) * 100) / 100;
    if (amount > maxTotal + 0.009) {
      if (days <= 0) {
        return `Team meal exceeds eligible limit. Allowed: ₹ ${perDay.toLocaleString('en-IN')} per member per day.`;
      }
      return `Team meal exceeds eligible limit. Allowed: ₹ ${perDay.toLocaleString('en-IN')} per member per day.`;
    }
    return null;
  }

  private localDateKey(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private formatClaimDayLabel(day: string): string {
    const raw = String(day || '').trim();
    if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) {
      const [y, m, d] = raw.split('-');
      return `${d}-${m}-${y}`;
    }
    return raw;
  }

  private spreadClaimAmountByDay(
    fromDate: string,
    toDate: string,
    amount: number,
    dailyTotals: Map<string, number>
  ): void {
    const days = this.countClaimInclusiveDays(fromDate, toDate);
    if (days <= 0 || !Number.isFinite(amount)) {
      return;
    }
    const perDay = amount / days;
    const fromIso = this.toIsoStartOfDay(fromDate);
    const toIso = this.toIsoStartOfDay(toDate);
    if (!fromIso || !toIso) {
      return;
    }
    const cursor = new Date(fromIso);
    const end = new Date(toIso);
    while (cursor.getTime() <= end.getTime()) {
      const key = this.localDateKey(cursor);
      dailyTotals.set(key, (dailyTotals.get(key) || 0) + perDay);
      cursor.setDate(cursor.getDate() + 1);
    }
  }

  /** Max amount allowed for a date range after other non-team Food claims in the ticket. */
  private maxSharedFoodAmountForDateRange(
    fromDate: string | null | undefined,
    toDate: string | null | undefined,
    claims: any[],
    excludeIndex: number | null = null
  ): number | null {
    const perDay = this.getRoleAmountLimit('Food');
    if (perDay == null) {
      return null;
    }
    const days = this.countClaimInclusiveDays(fromDate, toDate);
    if (days <= 0) {
      return null;
    }
    const existingTotals = new Map<string, number>();
    claims.forEach((c, i) => {
      if (excludeIndex != null && i === excludeIndex) {
        return;
      }
      if (c?.expenditureType !== 'Food' || this.isTeamMealFoodAllowanceType(c.foodAllowanceType)) {
        return;
      }
      if (!c.fromDate || !c.toDate) {
        return;
      }
      this.spreadClaimAmountByDay(String(c.fromDate), String(c.toDate), Number(c.amount), existingTotals);
    });
    const fromIso = this.toIsoStartOfDay(fromDate);
    const toIso = this.toIsoStartOfDay(toDate);
    if (!fromIso || !toIso) {
      return null;
    }
    let remaining = 0;
    const cursor = new Date(fromIso);
    const end = new Date(toIso);
    while (cursor.getTime() <= end.getTime()) {
      const key = this.localDateKey(cursor);
      const used = existingTotals.get(key) || 0;
      remaining += Math.max(0, perDay - used);
      cursor.setDate(cursor.getDate() + 1);
    }
    return Math.round(remaining * 100) / 100;
  }

  private buildSharedFoodLimitErrorMessage(
    perDay: number,
    dailyTotals: Map<string, number>,
    draft?: { fromDate?: string; toDate?: string; amount?: number } | null,
    excludeIndex: number | null = null,
    claims: any[] = []
  ): string | null {
    const failingDays: { day: string; total: number }[] = [];
    for (const [day, total] of dailyTotals) {
      if (total > perDay + 0.009) {
        failingDays.push({ day, total: Math.round(total * 100) / 100 });
      }
    }
    if (!failingDays.length) {
      return null;
    }
    const parts: string[] = [
      'Food claims in this ticket exceed your eligible daily limit.',
      `Allowed: ₹ ${perDay.toLocaleString('en-IN')} per day shared across all food claims (except team meals).`
    ];
    // Keep message short (no per-day breakdown / totals).
    return parts.join(' ');
  }

  validateSharedFoodDailyLimitsForTicket(
    claims: any[],
    draft?: { expenditureType?: string; foodAllowanceType?: string; fromDate?: string; toDate?: string; amount?: number } | null,
    excludeIndex: number | null = null
  ): string | null {
    const perDay = this.getRoleAmountLimit('Food');
    if (perDay == null) {
      return null;
    }
    const dailyTotals = new Map<string, number>();
    claims.forEach((c, i) => {
      if (excludeIndex != null && i === excludeIndex) {
        return;
      }
      if (c?.expenditureType !== 'Food' || this.isTeamMealFoodAllowanceType(c.foodAllowanceType)) {
        return;
      }
      if (!c.fromDate || !c.toDate) {
        return;
      }
      this.spreadClaimAmountByDay(String(c.fromDate), String(c.toDate), Number(c.amount), dailyTotals);
    });
    if (draft?.expenditureType === 'Food' && !this.isTeamMealFoodAllowanceType(draft.foodAllowanceType)
      && draft.fromDate && draft.toDate) {
      this.spreadClaimAmountByDay(String(draft.fromDate), String(draft.toDate), Number(draft.amount), dailyTotals);
    }
    return this.buildSharedFoodLimitErrorMessage(perDay, dailyTotals, draft ?? undefined, excludeIndex, claims);
  }

  validateCombinedFoodDailyLimitsForTicket(
    claims: any[],
    draft?: { expenditureType?: string; foodAllowanceType?: string; fromDate?: string; toDate?: string; amount?: number } | null,
    excludeIndex: number | null = null
  ): string | null {
    return this.validateSharedFoodDailyLimitsForTicket(claims, draft, excludeIndex);
  }

  /** Per-day limit for current claim fields (team meal uses separate policy key). */
  private resolvePerDayLimitForCurrentClaim(): number | null {
    const exp = this.reimbursementObj?.expenditureType;
    const isTravel = String(exp || '').trim().toLowerCase() === 'travel';
    if (isTravel) {
      return this.getRoleTravelModeDailyLimit(this.reimbursementObj?.travelMode);
    }
    if (exp === 'Food' && this.isTeamMealFoodAllowanceType()) {
      return this.getRoleTeamMealPerMemberPerDayLimit();
    }
    return this.getRoleAmountLimit(exp);
  }

  roleAmountLimitHint(): string {
    const exp = this.reimbursementObj?.expenditureType;
    const isTravel = String(exp || '').trim().toLowerCase() === 'travel';
    const perDay = this.resolvePerDayLimitForCurrentClaim();
    if (perDay == null) {
      if (isTravel && this.reimbursementObj?.travelMode) {
        return 'No per-day limit configured for this travel mode.';
      }
      if (isTravel) {
        return 'Select a travel mode to see your per-day limit.';
      }
      return '';
    }
    if (exp === 'Food' && this.isTeamMealFoodAllowanceType()) {
      const members = this.parsedTeamMemberCount();
      const days = this.countClaimInclusiveDays(this.reimbursementObj?.fromDate, this.reimbursementObj?.toDate);
      if (members != null && members >= 2 && days > 0) {
        return `Team meal limit: ₹ ${perDay.toLocaleString('en-IN')} per member per day.`;
      }
      if (members != null && members >= 2) {
        return `Team meal limit: ₹ ${perDay.toLocaleString('en-IN')} per member per day.`;
      }
      return `Team meal limit: ₹ ${perDay.toLocaleString('en-IN')} per member per day.`;
    }
    if (exp === 'Food' && !this.isTeamMealFoodAllowanceType()) {
      const days = this.countClaimInclusiveDays(this.reimbursementObj?.fromDate, this.reimbursementObj?.toDate);
      if (days > 0) {
        const grossTotal = Math.round(perDay * days * 100) / 100;
        const remaining = this.maxSharedFoodAmountForDateRange(
          this.reimbursementObj?.fromDate,
          this.reimbursementObj?.toDate,
          this.ticketClaims,
          this.editingClaimIndex
        );
        if (remaining != null && remaining < grossTotal - 0.009) {
          return `Food limit: ₹ ${perDay.toLocaleString('en-IN')} per day × ${days} day(s) = ₹ ${grossTotal.toLocaleString('en-IN')}. Remaining for this claim after other food claims in ticket: ₹ ${remaining.toLocaleString('en-IN')}.`;
        }
        return `Food limit: ₹ ${perDay.toLocaleString('en-IN')} per day × ${days} day(s) = ₹ ${grossTotal.toLocaleString('en-IN')} (shared across all food claims in this ticket).`;
      }
      return `Food limit: ₹ ${perDay.toLocaleString('en-IN')} per day shared across all food claims in this ticket (select From and To dates to see total).`;
    }
    const days = this.countClaimInclusiveDays(this.reimbursementObj?.fromDate, this.reimbursementObj?.toDate);
    const modeLabel = isTravel && this.reimbursementObj?.travelMode
      ? ` (${this.reimbursementObj.travelMode})`
      : '';
    if (days > 0) {
      const total = perDay * days;
      return `Eligible limit for your role${modeLabel}: ₹ ${perDay.toLocaleString('en-IN')} per day × ${days} day(s) = ₹ ${total.toLocaleString('en-IN')}`;
    }
    return `Eligible limit for your role${modeLabel}: ₹ ${perDay.toLocaleString('en-IN')} per day (select From and To dates to see total).`;
  }

  /** Inline client-side check: blocks Add Claim when amount exceeds role / travel-mode cap. */
  roleAmountLimitExceededMessage(): string | null {
    if (this.isPersonalVehicleTravelMode()) {
      return null;
    }
    const exp = this.reimbursementObj?.expenditureType;
    if (!exp) {
      return null;
    }
    const amount = Number(this.reimbursementObj?.amount);
    if (!this.isValidClaimAmount(amount)) {
      return null;
    }
    if (exp === 'Food' && this.isTeamMealFoodAllowanceType()) {
      return this.validateTeamMealLimitMessage(
        amount,
        this.reimbursementObj.fromDate,
        this.reimbursementObj.toDate,
        this.reimbursementObj.teamMemberCount
      );
    }
    if (exp === 'Food' && !this.isTeamMealFoodAllowanceType()) {
      return this.validateSharedFoodDailyLimitsForTicket(
        this.ticketClaims,
        {
          expenditureType: this.reimbursementObj.expenditureType,
          foodAllowanceType: this.reimbursementObj.foodAllowanceType,
          fromDate: this.reimbursementObj.fromDate,
          toDate: this.reimbursementObj.toDate,
          amount
        },
        this.editingClaimIndex
      );
    }
    const isTravel = String(exp).trim().toLowerCase() === 'travel';
    const perDay = this.resolvePerDayLimitForCurrentClaim();
    if (perDay == null) {
      return null;
    }
    const days = this.countClaimInclusiveDays(this.reimbursementObj?.fromDate, this.reimbursementObj?.toDate);
    const scope = isTravel && this.reimbursementObj?.travelMode
      ? `travel mode "${this.reimbursementObj.travelMode}"`
      : `your job role (${exp})`;
    if (days <= 0) {
      if (amount > perDay + 0.009) {
        return `Amount exceeds eligible limit. Allowed: ₹ ${perDay.toLocaleString('en-IN')} per day for ${scope}.`;
      }
      return null;
    }
    const maxTotal = perDay * days;
    if (amount > maxTotal + 0.009) {
      return `Amount exceeds eligible limit. Allowed: ₹ ${perDay.toLocaleString('en-IN')} per day for ${scope}.`;
    }
    return null;
  }

  isRoleAmountLimitExceeded(): boolean {
    return this.roleAmountLimitExceededMessage() != null;
  }

  async loadMappedProjectsForReimbursement() {
    this.mappedProjectsForClaim = [];
    this.projectPickerSource = null;
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
      const raw = (bag.projects || []) as any[];
      const seen = new Set<number>();
      const parsed: { projectId: number; projectName: string; clientName?: string; clientId?: number | null; poNo?: string }[] = [];
      for (const p of raw) {
        const id = p.projectId != null ? Number(p.projectId) : NaN;
        if (!Number.isFinite(id) || seen.has(id)) {
          continue;
        }
        seen.add(id);
        const cn = p.clientName != null ? String(p.clientName).trim() : '';
        const cid = p.clientId != null && p.clientId !== '' ? Number(p.clientId) : null;
        const poNo = p.poNo != null ? String(p.poNo).trim() : '';
        parsed.push({
          projectId: id,
          projectName: p.projectName != null ? String(p.projectName) : `Project #${id}`,
          clientName: cn !== '' ? cn : undefined,
          clientId: Number.isFinite(cid as number) ? cid : null,
          poNo: poNo !== '' ? poNo : undefined
        });
      }
      const othersRows = parsed.filter((r) => r.projectId === this.RMB_OTHERS_PROJECT_ID);
      const normalRows = parsed
        .filter((r) => r.projectId !== this.RMB_OTHERS_PROJECT_ID)
        .sort((a, b) => a.projectName.localeCompare(b.projectName));
      if (!othersRows.length) {
        othersRows.push({
          projectId: this.RMB_OTHERS_PROJECT_ID,
          projectName: 'Others',
          clientName: '',
          clientId: null,
          poNo: undefined
        });
      }
      this.mappedProjectsForClaim = [...othersRows, ...normalRows];
      await this.loadClientsForManualProject();
    } catch (e) {
      console.error('loadMappedProjectsForReimbursement', e);
      this.projectPickerSource = null;
      this.mappedProjectsForClaim = [];
    }
  }

  async loadClientsForManualProject(force = false): Promise<void> {
    if (!force && this.clientsForManualProject.length > 0) {
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
      this.clientsForManualProject = raw
        .filter((r) => r?.pickerKey)
        .map((r) => ({
          pickerKey: String(r.pickerKey),
          clientId: r.clientId != null ? Number(r.clientId) : null,
          reimbursementClientId: r.reimbursementClientId != null ? Number(r.reimbursementClientId) : null,
          clientName: r.clientName != null ? String(r.clientName) : '',
          clientCategory: r.clientCategory != null ? String(r.clientCategory) : '',
          prospectiveEntry: r.prospectiveEntry === true
        }));
    } catch (e) {
      console.error('loadClientsForManualProject', e);
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
    this.reimbursementObj.pocProjectName = '';
    this.reimbursementObj.othersClientId = null;
    this.reimbursementObj.clientPickerKey = null;
    this.reimbursementObj.prospectiveClientName = '';
    this.reimbursementObj.pocProject = false;
    this.reimbursementObj.displayClientName = '';
    this.reimbursementObj.displayPoNo = '';
  }

  clearManualProjectClient(): void {
    this.reimbursementObj.othersClientId = null;
    this.reimbursementObj.clientPickerKey = null;
    this.reimbursementObj.prospectiveClientName = '';
  }

  isOthersProjectSelected(): boolean {
    return Number(this.reimbursementObj.projectId) === this.RMB_OTHERS_PROJECT_ID;
  }

  isPocProjectSelected(): boolean {
    return Number(this.reimbursementObj.projectId) === this.RMB_POC_PROJECT_ID
      || this.reimbursementObj.pocProject === true;
  }

  isManualProjectSelected(): boolean {
    return this.isOthersProjectSelected() || this.isPocProjectSelected();
  }

  isProspectiveClientEntry(): boolean {
    return this.reimbursementObj.clientPickerKey === this.PROSPECTIVE_CLIENT_PICKER_KEY;
  }

  prospectiveClientNameDuplicate = false;

  private normalizeClientName(name: string): string {
    return String(name || '')
      .trim()
      .replace(/\s+/g, ' ')
      .toLowerCase();
  }

  /** Returns true if the entered prospective client name already exists in master or reimbursement client lists. */
  private prospectiveClientAlreadyExists(name: string): boolean {
    const n = this.normalizeClientName(name);
    if (!n) {
      return false;
    }
    // clientsForManualProject includes both master clients (M:*) and reimbursement clients (R:*).
    return (this.clientsForManualProject || []).some((c) => {
      if (!c || c.prospectiveEntry === true) {
        return false;
      }
      const existing = this.normalizeClientName(String(c.clientName || ''));
      return existing !== '' && existing === n;
    });
  }

  async onProspectiveClientNameChange(value: string): Promise<void> {
    this.reimbursementObj.prospectiveClientName = value;
    if (!this.isProspectiveClientEntry()) {
      this.prospectiveClientNameDuplicate = false;
      return;
    }
    // Ensure client list is loaded (best-effort) so the check is accurate.
    if (!this.clientsForManualProject || this.clientsForManualProject.length === 0) {
      await this.loadClientsForManualProject(true);
    }
    this.prospectiveClientNameDuplicate = this.prospectiveClientAlreadyExists(value || '');
  }

  manualProjectNameLabel(): string {
    return this.isPocProjectSelected() ? 'Project name (POC)' : 'Project name (Others)';
  }

  get manualProjectNameModel(): string {
    return this.isPocProjectSelected()
      ? (this.reimbursementObj.pocProjectName || '')
      : (this.reimbursementObj.othersProjectName || '');
  }

  set manualProjectNameModel(value: string) {
    if (this.isPocProjectSelected()) {
      this.reimbursementObj.pocProjectName = value;
    } else {
      this.reimbursementObj.othersProjectName = value;
    }
  }

  onPocProjectChange(): void {
    if (this.reimbursementObj.pocProject) {
      // Checkbox-only: don't show "POC - Project" in project dropdown.
      // We still submit sentinel -2 in the payload when this flag is true.
      this.reimbursementObj.projectId = null;
      this.reimbursementObj.othersProjectName = '';
      void this.loadClientsForManualProject();
      return;
    }
    this.reimbursementObj.pocProjectName = '';
    this.clearManualProjectClient();
  }

  onManualClientPickerChange(): void {
    if (!this.isProspectiveClientEntry()) {
      this.reimbursementObj.prospectiveClientName = '';
      this.prospectiveClientNameDuplicate = false;
    }
    const key = this.reimbursementObj.clientPickerKey;
    if (!key || key === this.PROSPECTIVE_CLIENT_PICKER_KEY) {
      this.reimbursementObj.othersClientId = null;
      return;
    }
    if (key.startsWith('M:')) {
      this.reimbursementObj.othersClientId = Number(key.slice(2));
      return;
    }
    this.reimbursementObj.othersClientId = null;
  }

  private buildClientPayloadFromForm(): {
    clientId: number | null;
    reimbursementClientId: number | null;
    prospectiveClientName: string | null;
    clientCategory: string | null;
    clientName: string;
  } {
    const key = this.reimbursementObj.clientPickerKey;
    if (key === this.PROSPECTIVE_CLIENT_PICKER_KEY) {
      const name = (this.reimbursementObj.prospectiveClientName || '').trim();
      return {
        clientId: null,
        reimbursementClientId: null,
        prospectiveClientName: name || null,
        clientCategory: 'PROSPECTIVE_NEW_CLIENT',
        clientName: name
      };
    }
    if (key && key.startsWith('R:')) {
      const rid = Number(key.slice(2));
      const row = this.clientsForManualProject.find((c) => c.pickerKey === key);
      return {
        clientId: null,
        reimbursementClientId: Number.isFinite(rid) ? rid : null,
        prospectiveClientName: null,
        clientCategory: row?.clientCategory || 'PROSPECTIVE_NEW_CLIENT',
        clientName: row?.clientName || ''
      };
    }
    const cid = key && key.startsWith('M:') ? Number(key.slice(2))
      : (this.reimbursementObj.othersClientId != null ? Number(this.reimbursementObj.othersClientId) : NaN);
    const row = this.clientsForManualProject.find((c) => c.pickerKey === `M:${cid}`)
      || this.clientsForManualProject.find((c) => c.clientId === cid);
    return {
      clientId: Number.isFinite(cid) ? cid : null,
      reimbursementClientId: null,
      prospectiveClientName: null,
      clientCategory: 'MASTER',
      clientName: row?.clientName || ''
    };
  }

  private buildClaimDraftFromForm(selectedPid: number, row: { clientId?: number | null; clientName?: string; poNo?: string } | undefined): any {
    const manual = selectedPid === this.RMB_OTHERS_PROJECT_ID || selectedPid === this.RMB_POC_PROJECT_ID;
    const clientFields = manual ? this.buildClientPayloadFromForm() : null;
    return {
      expenditureType: this.reimbursementObj.expenditureType,
      expenditureTypeDescription: this.reimbursementObj.expenditureTypeDescription,
      amount: Number(this.reimbursementObj.amount),
      travelMode: this.reimbursementObj.travelMode,
      distance: this.reimbursementObj.distance,
      vehicleType: this.reimbursementObj.vehicleType,
      foodAllowanceType: this.reimbursementObj.foodAllowanceType,
      teamMemberCount: this.isTeamMealFoodAllowanceType()
        ? this.parsedTeamMemberCount(this.reimbursementObj.teamMemberCount)
        : null,
      dateOfFood: null,
      fromDate: this.reimbursementObj.fromDate,
      toDate: this.reimbursementObj.toDate,
      purpose: this.reimbursementObj.purpose.trim(),
      businessJustification: (this.reimbursementObj.businessJustification || '').trim(),
      hodApproval: true,
      preApprovalDate: this.reimbursementObj.preApprovalDate,
      projectId: selectedPid,
      projectName: this.selectedProjectLabel(),
      recurringExpense: !!this.reimbursementObj.recurringExpense,
      pocProject: selectedPid === this.RMB_POC_PROJECT_ID || !!this.reimbursementObj.pocProject,
      othersProjectName: selectedPid === this.RMB_OTHERS_PROJECT_ID
        ? (this.reimbursementObj.othersProjectName || '').trim() : null,
      pocProjectName: selectedPid === this.RMB_POC_PROJECT_ID
        ? (this.reimbursementObj.pocProjectName || '').trim() : null,
      othersClientId: clientFields?.clientId ?? null,
      clientId: !manual && row?.clientId != null ? Number(row.clientId) : clientFields?.clientId ?? null,
      reimbursementClientId: clientFields?.reimbursementClientId ?? null,
      prospectiveClientName: clientFields?.prospectiveClientName ?? null,
      clientCategory: clientFields?.clientCategory ?? null,
      clientName: manual
        ? (clientFields?.clientName || '')
        : (row?.clientName || this.reimbursementObj.displayClientName || ''),
      poNo: manual ? null : (row?.poNo || this.reimbursementObj.displayPoNo || null),
      clientPickerKey: this.reimbursementObj.clientPickerKey
    };
  }

  private restoreClientPickerFromClaim(c: any): void {
    if (c.prospectiveClientName) {
      this.reimbursementObj.clientPickerKey = this.PROSPECTIVE_CLIENT_PICKER_KEY;
      this.reimbursementObj.prospectiveClientName = c.prospectiveClientName;
      this.reimbursementObj.othersClientId = null;
      return;
    }
    if (c.reimbursementClientId != null) {
      this.reimbursementObj.clientPickerKey = `R:${c.reimbursementClientId}`;
      this.reimbursementObj.prospectiveClientName = '';
      this.reimbursementObj.othersClientId = null;
      return;
    }
    if (c.othersClientId != null || c.clientId != null) {
      const cid = c.othersClientId != null ? c.othersClientId : c.clientId;
      this.reimbursementObj.clientPickerKey = `M:${cid}`;
      this.reimbursementObj.othersClientId = Number(cid);
      this.reimbursementObj.prospectiveClientName = '';
      return;
    }
    this.reimbursementObj.clientPickerKey = null;
    this.reimbursementObj.prospectiveClientName = '';
    this.reimbursementObj.othersClientId = null;
  }

  assignedHodName(): string {
    return String(this.currentUser?.hodName ?? '').trim();
  }

  assignedHodEmail(): string {
    return String(this.currentUser?.hodEmail ?? '').trim();
  }

  showReadonlyClientForProject(): boolean {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    return Number.isFinite(id) && id !== this.RMB_OTHERS_PROJECT_ID;
  }

  showReadonlyPoNoForProject(): boolean {
    return this.showReadonlyClientForProject() && !this.isManualProjectSelected();
  }

  onReimbursementProjectSelected(_event?: unknown): void {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    if (!Number.isFinite(id)) {
      this.reimbursementObj.displayClientName = '';
      this.reimbursementObj.displayPoNo = '';
      this.reimbursementObj.othersProjectName = '';
      this.reimbursementObj.pocProjectName = '';
      this.clearManualProjectClient();
      this.reimbursementObj.pocProject = false;
      return;
    }
    if (id === this.RMB_OTHERS_PROJECT_ID) {
      this.reimbursementObj.pocProject = false;
      this.reimbursementObj.pocProjectName = '';
      this.reimbursementObj.displayClientName = '';
      this.reimbursementObj.displayPoNo = '';
      void this.loadClientsForManualProject();
      return;
    }
    if (id === this.RMB_POC_PROJECT_ID) {
      this.reimbursementObj.pocProject = true;
      this.reimbursementObj.othersProjectName = '';
      this.reimbursementObj.displayClientName = '';
      this.reimbursementObj.displayPoNo = '';
      void this.loadClientsForManualProject();
      return;
    }
    this.reimbursementObj.pocProject = false;
    this.reimbursementObj.othersProjectName = '';
    this.reimbursementObj.pocProjectName = '';
    this.clearManualProjectClient();
    const p = this.mappedProjectsForClaim.find((x) => x.projectId === id);
    this.reimbursementObj.displayClientName = p?.clientName ? String(p.clientName) : '';
    this.reimbursementObj.displayPoNo = p?.poNo ? String(p.poNo) : '';
  }

  private selectedProjectLabel(): string {
    const id = this.reimbursementObj.projectId != null ? Number(this.reimbursementObj.projectId) : NaN;
    if (!Number.isFinite(id)) {
      return '';
    }
    if (id === this.RMB_OTHERS_PROJECT_ID || id === this.RMB_POC_PROJECT_ID) {
      const pn = id === this.RMB_POC_PROJECT_ID
        ? (this.reimbursementObj.pocProjectName || '').trim()
        : (this.reimbursementObj.othersProjectName || '').trim();
      const clientFields = this.buildClientPayloadFromForm();
      const cn = clientFields.clientName || '';
      const prefix = id === this.RMB_POC_PROJECT_ID ? 'POC: ' : '';
      return cn ? `${prefix}${pn} (${cn})` : `${prefix}${pn}`;
    }
    const p = this.mappedProjectsForClaim.find((x) => x.projectId === id);
    if (!p) {
      return '';
    }
    return p.clientName ? `${p.projectName} (${p.clientName})` : p.projectName;
  }

  recalculatePersonalVehicleAmount(): void {
    if (!this.isPersonalVehicleTravelMode()) {
      return;
    }
    const rate = this.getVehicleRatePerKm(this.reimbursementObj.vehicleType);
    const dist = Number(this.reimbursementObj.distance);
    if (rate == null || !(dist > 0)) {
      this.reimbursementObj.amount = 0;
      this.syncClaimAmountInputFromModel();
      return;
    }
    const days = this.countClaimInclusiveDays(this.reimbursementObj.fromDate, this.reimbursementObj.toDate);
    const dayFactor = days > 0 ? days : 1;
    this.reimbursementObj.amount = Math.round(dist * rate * dayFactor * 100) / 100;
    this.syncClaimAmountInputFromModel();
  }

  getPersonalVehicleExpectedAmount(
    vehicleType?: string | null,
    distance?: number | string | null,
    fromDate?: string | null,
    toDate?: string | null
  ): number | null {
    const rate = this.getVehicleRatePerKm(vehicleType ?? this.reimbursementObj.vehicleType);
    const dist = Number(distance ?? this.reimbursementObj.distance);
    if (rate == null || !(dist > 0)) {
      return null;
    }
    const days = this.countClaimInclusiveDays(
      fromDate ?? this.reimbursementObj.fromDate,
      toDate ?? this.reimbursementObj.toDate
    );
    const dayFactor = days > 0 ? days : 1;
    return Math.round(dist * rate * dayFactor * 100) / 100;
  }

  onKilometersChange() {
    this.recalculatePersonalVehicleAmount();
  }

  onVehicleTypeChange(): void {
    this.onKilometersChange();
  }

  onTravelModeChange(): void {
    if (!this.isPersonalVehicleTravelMode()) {
      this.reimbursementObj.vehicleType = null;
      this.reimbursementObj.distance = null;
    } else {
      this.recalculatePersonalVehicleAmount();
    }
  }

  isPersonalVehicleTravelMode(mode?: string | null): boolean {
    const m = String(mode ?? this.reimbursementObj?.travelMode ?? '').trim().toLowerCase();
    return m.includes('own vehicle') || m.includes('personal vehicle');
  }

  private normalizeVehicleTypeKey(name: string | null | undefined): string {
    return String(name || '').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  getVehicleRatePerKm(vehicleType: string | null | undefined): number | null {
    const vt = vehicleType ? String(vehicleType).trim() : '';
    if (!vt) {
      return null;
    }
    const rates = this.rolePolicy?.vehicleRatesPerKm as Record<string, number> | undefined;
    if (!rates) {
      return null;
    }
    const key = this.normalizeVehicleTypeKey(vt);
    for (const [name, rate] of Object.entries(rates)) {
      if (this.normalizeVehicleTypeKey(name) === key) {
        const n = Number(rate);
        return Number.isFinite(n) && n > 0 ? n : null;
      }
    }
    return null;
  }

  vehicleRateMissingHint(): string {
    if (!this.isPersonalVehicleTravelMode() || !this.reimbursementObj?.vehicleType) {
      return '';
    }
    if (this.getVehicleRatePerKm(this.reimbursementObj.vehicleType) != null) {
      return '';
    }
    return `Per km rate is not configured in Expense Policy for "${this.reimbursementObj.vehicleType}". Contact HR.`;
  }

  vehicleRateHint(): string {
    if (!this.isPersonalVehicleTravelMode() || !this.reimbursementObj?.vehicleType) {
      return '';
    }
    const rate = this.getVehicleRatePerKm(this.reimbursementObj.vehicleType);
    const dist = Number(this.reimbursementObj.distance);
    if (rate == null) {
      return '';
    }
    const days = this.countClaimInclusiveDays(this.reimbursementObj.fromDate, this.reimbursementObj.toDate);
    if (dist > 0 && days > 0) {
      const total = this.getPersonalVehicleExpectedAmount();
      return `Eligible: ₹ ${rate.toLocaleString('en-IN')}/km × ${dist} km × ${days} day(s) = ₹ ${total!.toLocaleString('en-IN')}`;
    }
    return `Eligible rate: ₹ ${rate.toLocaleString('en-IN')} per km. Total = km × rate × days (select From and To dates).`;
  }

  /** Keep claim amount text in sync when amount is set programmatically (e.g. travel auto-calc). */
  syncClaimAmountInputFromModel(): void {
    const a = this.reimbursementObj?.amount;
    if (a == null || a === 0 || !Number.isFinite(Number(a))) {
      this.claimAmountInput = '';
      return;
    }
    this.claimAmountInput = String(Number(a));
  }

  /** From/To date: calendar only — block keyboard typing and paste. */
  onClaimDateKeydown(event: KeyboardEvent): void {
    if (event.key === 'Tab') {
      return;
    }
    event.preventDefault();
  }

  onClaimDatePaste(event: ClipboardEvent): void {
    event.preventDefault();
  }

  openClaimDatePicker(event: Event): void {
    const el = event.target as HTMLInputElement;
    if (el && typeof el.showPicker === 'function') {
      try {
        el.showPicker();
      } catch {
        // showPicker may throw if not triggered by user gesture
      }
    }
  }

  onClaimAmountKeydown(event: KeyboardEvent): void {
    if (this.isPersonalVehicleTravelMode()) {
      event.preventDefault();
      return;
    }
    if (event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    const k = event.key;
    const nav = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (nav.includes(k)) {
      return;
    }
    if (k === '.') {
      const input = event.target as HTMLInputElement;
      if (input.value.includes('.')) {
        event.preventDefault();
      }
      return;
    }
    if (!/^\d$/.test(k)) {
      event.preventDefault();
    }
  }

  onClaimAmountPaste(event: ClipboardEvent): void {
    if (this.isPersonalVehicleTravelMode()) {
      event.preventDefault();
      return;
    }
    event.preventDefault();
    const input = event.target as HTMLInputElement;
    const raw = event.clipboardData?.getData('text') ?? '';
    const merged = this.sanitizeClaimAmountString(input.value + raw.replace(/[^\d.]/g, ''));
    this.applyClaimAmountString(merged);
  }

  onClaimAmountInputChange(value: string): void {
    if (this.isPersonalVehicleTravelMode()) {
      return;
    }
    this.applyClaimAmountString(this.sanitizeClaimAmountString(String(value ?? '')));
  }

  private sanitizeClaimAmountString(value: string): string {
    let v = value.replace(/[^\d.]/g, '');
    const dot = v.indexOf('.');
    if (dot !== -1) {
      v = v.slice(0, dot + 1) + v.slice(dot + 1).replace(/\./g, '');
      const [intPart, decPart = ''] = v.split('.');
      v = intPart + '.' + decPart.slice(0, 2);
    }
    return v;
  }

  private applyClaimAmountString(v: string): void {
    this.claimAmountInput = v;
    if (v === '' || v === '.') {
      this.reimbursementObj.amount = 0;
      return;
    }
    const n = parseFloat(v);
    this.reimbursementObj.amount = Number.isFinite(n) ? n : 0;
  }

  private isValidClaimAmount(amount: unknown): boolean {
    const n = Number(amount);
    return Number.isFinite(n) && n > 0;
  }

  onClaimPurposeKeydown(event: KeyboardEvent): void {
    if (event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    const k = event.key;
    const nav = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Enter'];
    if (nav.includes(k)) {
      return;
    }
    if (k.length === 1 && !/^[a-zA-Z0-9\s]$/.test(k)) {
      event.preventDefault();
    }
  }

  onClaimPurposePaste(event: ClipboardEvent): void {
    event.preventDefault();
    const raw = (event.clipboardData?.getData('text') ?? '').replace(/[^a-zA-Z0-9\s]/g, '');
    const el = event.target as HTMLTextAreaElement;
    const start = el.selectionStart ?? 0;
    const end = el.selectionEnd ?? 0;
    const val = this.reimbursementObj.purpose || '';
    this.reimbursementObj.purpose = val.slice(0, start) + raw + val.slice(end);
  }

  onClaimPurposeInputChange(value: string): void {
    this.reimbursementObj.purpose = String(value ?? '').replace(/[^a-zA-Z0-9\s]/g, '');
  }

  async resetForm(template: TemplateRef<any>) {
    this.editingClaimIndex = null;
    this.ticketClaims = [];
    this.claimAmountInput = '';
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
      businessJustification: '',
      hodApproval: false,
      preApprovalDate: null,
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      teamMemberCount: null,
      vehicleType: '',
      file: '',
      othersProjectName: '',
      pocProjectName: '',
      othersClientId: null,
      clientPickerKey: null,
      prospectiveClientName: '',
      recurringExpense: false,
      pocProject: false,
      displayClientName: '',
      displayPoNo: '',
      expenditureTypeDescription: ''
    };
    this.fileUploads = [{}];
    this.preApprovalFileUploads = [{}];
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

  hasPendingTicket(): boolean {
    return Array.isArray(this.ticketClaims) && this.ticketClaims.length > 0;
  }

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
    this.syncClaimAmountInputFromModel();
    this.reimbursementObj.travelMode = c.travelMode || '';
    this.reimbursementObj.distance = c.distance != null ? c.distance : '';
    this.reimbursementObj.vehicleType = c.vehicleType || '';
    this.reimbursementObj.foodAllowanceType = c.foodAllowanceType || null;
    this.reimbursementObj.teamMemberCount = c.teamMemberCount != null ? Number(c.teamMemberCount) : null;
    this.reimbursementObj.dateOfFood = c.dateOfFood || null;
    this.reimbursementObj.fromDate = c.fromDate || null;
    this.reimbursementObj.toDate = c.toDate || null;
    this.reimbursementObj.purpose = c.purpose || '';
    this.reimbursementObj.businessJustification = c.businessJustification || '';
    this.reimbursementObj.hodApproval = true;
    this.reimbursementObj.preApprovalDate = c.preApprovalDate || null;
    this.reimbursementObj.recurringExpense = !!c.recurringExpense;
    this.reimbursementObj.pocProject = !!c.pocProject;
    this.reimbursementObj.projectId = c.projectId != null ? Number(c.projectId) : null;
    if (c.pocProject && this.reimbursementObj.projectId == null) {
      this.reimbursementObj.projectId = this.RMB_POC_PROJECT_ID;
    }
    if (Number(this.reimbursementObj.projectId) === this.RMB_OTHERS_PROJECT_ID) {
      this.reimbursementObj.othersProjectName = (c.othersProjectName || c.projectName || '').trim();
      this.reimbursementObj.pocProjectName = '';
      this.reimbursementObj.displayClientName = '';
      this.reimbursementObj.displayPoNo = '';
      void this.loadClientsForManualProject();
      this.restoreClientPickerFromClaim(c);
    } else if (Number(this.reimbursementObj.projectId) === this.RMB_POC_PROJECT_ID || c.pocProject) {
      this.reimbursementObj.projectId = this.RMB_POC_PROJECT_ID;
      this.reimbursementObj.pocProject = true;
      this.reimbursementObj.pocProjectName = (c.pocProjectName || c.projectName || '').trim();
      this.reimbursementObj.othersProjectName = '';
      this.reimbursementObj.displayClientName = '';
      this.reimbursementObj.displayPoNo = '';
      void this.loadClientsForManualProject();
      this.restoreClientPickerFromClaim(c);
    } else {
      this.reimbursementObj.othersProjectName = '';
      this.reimbursementObj.pocProjectName = '';
      this.clearManualProjectClient();
      this.reimbursementObj.displayClientName = (c.clientName || '').trim();
      this.onReimbursementProjectSelected();
      if (c.poNo) {
        this.reimbursementObj.displayPoNo = String(c.poNo).trim();
      }
    }
    if (
      this.reimbursementObj.expenditureType === 'Travel' &&
      this.isPersonalVehicleTravelMode() &&
      this.reimbursementObj.vehicleType &&
      this.reimbursementObj.distance
    ) {
      this.onKilometersChange();
    }
    this.invalidFromDate = false;
    this.invalidToDate = false;
    this.fileUploads = [{}];
    this.preApprovalFileUploads = [{}];
    this.ensureClaimDocumentsList(c);
    this.ensureClaimPreApprovalDocumentsList(c);
  }

  cancelEditDraftClaim() {
    this.editingClaimIndex = null;
    this.draftDocPreview = null;
    this.resetAfterSubmit();
    this.fileUploads = [{}];
    this.preApprovalFileUploads = [{}];
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

  ensureClaimPreApprovalDocumentsList(claim: any): { docId: number; fileName: string }[] {
    if (claim.preApprovalDocuments?.length) {
      return claim.preApprovalDocuments;
    }
    if (claim.preApprovalDocIds?.length) {
      claim.preApprovalDocuments = claim.preApprovalDocIds.map((id: number) => ({
        docId: id,
        fileName: 'HOD pre-approval #' + id
      }));
      return claim.preApprovalDocuments;
    }
    claim.preApprovalDocuments = [];
    return claim.preApprovalDocuments;
  }

  preApprovalDocCountForClaim(c: any): number {
    if (c?.preApprovalDocuments?.length) {
      return c.preApprovalDocuments.length;
    }
    return c?.preApprovalDocIds?.length || 0;
  }

  documentsForEditingClaimPreApproval(): { docId: number; fileName: string }[] {
    if (this.editingClaimIndex === null) {
      return [];
    }
    const claim = this.ticketClaims[this.editingClaimIndex];
    if (!claim) {
      return [];
    }
    return this.ensureClaimPreApprovalDocumentsList(claim);
  }

  hasPreApprovalDocumentsOnEditingClaim(): boolean {
    return this.documentsForEditingClaimPreApproval().length > 0;
  }

  openDraftDocumentsModal(template: TemplateRef<any>, claimIndex: number) {
    this.draftDocsClaimIndex = claimIndex;
    this.draftDocPreview = null;
    const c = this.ticketClaims[claimIndex];
    if (c) {
      this.ensureClaimDocumentsList(c);
      this.ensureClaimPreApprovalDocumentsList(c);
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
    this.draftDocCache.clear();
  }

  canViewDraftFile(fileName: string | null | undefined): boolean {
    const n = String(fileName || '').trim().toLowerCase();
    return /\.(png|jpe?g|gif|webp|bmp)$/i.test(n);
  }

  private draftDocCache = new Map<number, {
    docId: number;
    fileName: string;
    mimeType: string;
    base64: string;
  }>();

  private normalizeDraftFileName(ticketFileName: string | null | undefined, fallbackName: string | null | undefined, docId: number): string {
    const raw = String(ticketFileName || fallbackName || '').trim();
    if (!raw) {
      return `document-${docId}`;
    }
    const idx = raw.indexOf('_');
    return idx >= 0 ? raw.substring(idx + 1) : raw;
  }

  private mimeFromDraftFileName(fileName: string): string | null {
    const n = fileName.toLowerCase();
    if (n.endsWith('.pdf')) {
      return 'application/pdf';
    }
    if (n.endsWith('.csv')) {
      return 'text/csv';
    }
    if (n.endsWith('.xlsx')) {
      return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    }
    if (n.endsWith('.xls') || n.endsWith('.xlsm')) {
      return 'application/vnd.ms-excel';
    }
    if (n.endsWith('.docx')) {
      return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    }
    if (n.endsWith('.doc')) {
      return 'application/msword';
    }
    if (n.endsWith('.png')) {
      return 'image/png';
    }
    if (n.endsWith('.jpg') || n.endsWith('.jpeg')) {
      return 'image/jpeg';
    }
    if (n.endsWith('.gif')) {
      return 'image/gif';
    }
    if (n.endsWith('.webp')) {
      return 'image/webp';
    }
    return null;
  }

  private getMimeTypeFromBase64(base64: string): string {
    try {
      const header = atob(base64.slice(0, 24));
      if (header.startsWith('%PDF')) {
        return 'application/pdf';
      }
      if (header.startsWith('\x89PNG')) {
        return 'image/png';
      }
      if (header.startsWith('\xFF\xD8\xFF')) {
        return 'image/jpeg';
      }
      if (header.startsWith('GIF8')) {
        return 'image/gif';
      }
      if (header.startsWith('PK')) {
        return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      }
    } catch {
      // fall through
    }
    return 'application/octet-stream';
  }

  private resolveDraftMimeType(base64: string, fileName: string): string {
    const fromBytes = this.getMimeTypeFromBase64(base64);
    if (fromBytes !== 'application/octet-stream') {
      return fromBytes;
    }
    return this.mimeFromDraftFileName(fileName) || fromBytes;
  }

  private triggerDraftBrowserDownload(view: { docId: number; fileName: string; mimeType: string; base64: string }): void {
    const byteCharacters = atob(view.base64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const blob = new Blob([new Uint8Array(byteNumbers)], { type: view.mimeType || 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = view.fileName || `document-${view.docId}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  private async ensureDraftDocLoaded(docId: number, fallbackFileName?: string | null): Promise<{
    docId: number;
    fileName: string;
    mimeType: string;
    base64: string;
  } | null> {
    const cached = this.draftDocCache.get(docId);
    if (cached) {
      return cached;
    }
    const response: any = await this.reimbursementService
      .previewDocumentReimbursment({ docId })
      .pipe(first())
      .toPromise();
    if (response?.serviceStatus !== 'Success' || !response?.serviceResponse?.documentBytes) {
      return null;
    }
    const base64Data = response.serviceResponse.documentBytes;
    const fileName = this.normalizeDraftFileName(
      response.serviceResponse.ticketFileName,
      fallbackFileName,
      docId
    );
    const view = {
      docId,
      fileName,
      mimeType: this.resolveDraftMimeType(base64Data, fileName),
      base64: base64Data
    };
    this.draftDocCache.set(docId, view);
    return view;
  }

  async downloadDraftDocument(docId: number, fileName?: string | null): Promise<void> {
    const view = await this.ensureDraftDocLoaded(docId, fileName);
    if (!view) {
      return;
    }
    this.triggerDraftBrowserDownload(view);
  }

  async previewDraftDocument(docId: number, fileName?: string | null) {
    this.draftDocPreview = null;
    const view = await this.ensureDraftDocLoaded(docId, fileName);
    if (!view) {
      return;
    }
    if (!this.canViewDraftFile(view.fileName)) {
      this.triggerDraftBrowserDownload(view);
      return;
    }
    this.draftDocPreview = `data:${view.mimeType};base64,${view.base64}`;
  }

  clearDraftDocPreview() {
    this.draftDocPreview = null;
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
    this.removeDraftClaimIfNoDocumentsLeft(claimIndex, alertTemplate);
  }

  removePreApprovalDocumentFromDraftClaim(docIndex: number, alertTemplate: TemplateRef<any>) {
    if (this.draftDocsClaimIndex === null) {
      return;
    }
    const claimIndex = this.draftDocsClaimIndex;
    const claim = this.ticketClaims[claimIndex];
    const preDocs = this.ensureClaimPreApprovalDocumentsList(claim);
    if (!preDocs.length) {
      return;
    }
    preDocs.splice(docIndex, 1);
    claim.preApprovalDocIds = preDocs.map((d: { docId: number }) => d.docId);
    this.removeDraftClaimIfNoDocumentsLeft(claimIndex, alertTemplate);
  }

  private removeDraftClaimIfNoDocumentsLeft(claimIndex: number, alertTemplate: TemplateRef<any>): void {
    const claim = this.ticketClaims[claimIndex];
    if (!claim || this.totalDocCountForClaim(claim) > 0) {
      return;
    }
    this.closeDraftDocumentsModal();
    if (this.editingClaimIndex === claimIndex) {
      this.cancelEditDraftClaim();
    } else if (this.editingClaimIndex !== null && claimIndex < this.editingClaimIndex) {
      this.editingClaimIndex--;
    }
    this.ticketClaims.splice(claimIndex, 1);
    this.openAlertMod(alertTemplate, 'Claim removed because it had no documents left.');
  }

  supportingDocCountForClaim(c: any): number {
    if (c?.documents?.length) {
      return c.documents.length;
    }
    return c?.docIds?.length || 0;
  }

  totalDocCountForClaim(c: any): number {
    return this.supportingDocCountForClaim(c) + this.preApprovalDocCountForClaim(c);
  }

  docCountForClaim(c: any): number {
    return this.totalDocCountForClaim(c);
  }

  docCountTitleForClaim(c: any): string {
    const bills = this.supportingDocCountForClaim(c);
    const hod = this.preApprovalDocCountForClaim(c);
    const parts: string[] = [];
    if (bills > 0) {
      parts.push(`${bills} expense bill${bills === 1 ? '' : 's'}`);
    }
    if (hod > 0) {
      parts.push(`${hod} HOD pre-approval email${hod === 1 ? '' : 's'}`);
    }
    return parts.length ? parts.join(', ') : 'No documents';
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
    const selectedPid = this.reimbursementObj.pocProject === true
      ? this.RMB_POC_PROJECT_ID
      : Number(this.reimbursementObj.projectId);
    if (this.reimbursementObj.pocProject !== true) {
      if (this.reimbursementObj.projectId == null || this.reimbursementObj.projectId === '') {
        this.openAlertMod(template, 'Please select a Project.');
        return;
      }
      if (!this.mappedProjectsForClaim.some((p) => p.projectId === selectedPid)) {
        this.openAlertMod(template, 'Please select a valid project from the list.');
        return;
      }
    }
    if (selectedPid === this.RMB_OTHERS_PROJECT_ID) {
      const on = (this.reimbursementObj.othersProjectName || '').trim();
      if (!on) {
        this.openAlertMod(template, 'Enter the project name for Others.');
        return;
      }
    }
    if (selectedPid === this.RMB_POC_PROJECT_ID) {
      const pn = (this.reimbursementObj.pocProjectName || '').trim();
      if (!pn) {
        this.openAlertMod(template, 'Enter the project name for POC.');
        return;
      }
    }
    if (this.isManualProjectSelected()) {
      const clientFields = this.buildClientPayloadFromForm();
      if (this.isProspectiveClientEntry()) {
        if (!clientFields.prospectiveClientName) {
          this.openAlertMod(template, 'Enter the prospective new client name.');
          return;
        }
        if (this.prospectiveClientAlreadyExists(clientFields.prospectiveClientName)) {
          this.openAlertMod(
            template,
            'This client already exists. Please select the client from the dropdown instead of using Prospective New Client.'
          );
          return;
        }
      } else if (clientFields.clientId == null && clientFields.reimbursementClientId == null) {
        this.openAlertMod(template, 'Select a client or choose Prospective New Client.');
        return;
      }
    }
    if (this.reimbursementObj.expenditureType === 'Travel') {
      if (!this.reimbursementObj.travelMode) {
        this.openAlertMod(template, 'Please select a Travel Mode.');
        return;
      }
      if (this.isPersonalVehicleTravelMode()) {
        if (!this.reimbursementObj.vehicleType) {
          this.openAlertMod(template, 'Please select a Vehicle Type.');
          return;
        }
        if (!this.reimbursementObj.distance || this.reimbursementObj.distance <= 0) {
          this.openAlertMod(template, 'Please enter valid total distance covered per day (KM).');
          return;
        }
      }
    }
    if (this.reimbursementObj.expenditureType === 'Food') {
      if (!this.reimbursementObj.foodAllowanceType) {
        this.openAlertMod(template, 'Please select Food Allowance Type.');
        return;
      }
      if (this.isTeamMealFoodAllowanceType()) {
        const teamErr = this.validateTeamMealLimitMessage(
          Number(this.reimbursementObj.amount),
          this.reimbursementObj.fromDate,
          this.reimbursementObj.toDate,
          this.reimbursementObj.teamMemberCount
        );
        if (teamErr) {
          this.openAlertMod(template, teamErr);
          return;
        }
      }
    }
    if (!this.isValidClaimAmount(this.reimbursementObj.amount)) {
      this.openAlertMod(template, 'Please enter a valid Total Amount greater than 0 (numbers and decimal only).');
      return;
    }
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
    if (this.isPersonalVehicleTravelMode()) {
      const rate = this.getVehicleRatePerKm(this.reimbursementObj.vehicleType);
      if (rate == null) {
        this.openAlertMod(
          template,
          `Per km rate is not configured in Expense Policy for "${this.reimbursementObj.vehicleType}". Contact HR.`
        );
        return;
      }
      const expected = this.getPersonalVehicleExpectedAmount();
      if (expected != null) {
        const actual = Number(this.reimbursementObj.amount);
        const rate = this.getVehicleRatePerKm(this.reimbursementObj.vehicleType);
        const days = this.countClaimInclusiveDays(this.reimbursementObj.fromDate, this.reimbursementObj.toDate);
        if (Math.abs(actual - expected) > 0.02) {
          this.openAlertMod(
            template,
            `Amount must be ₹ ${expected.toLocaleString('en-IN')} (₹ ${rate!.toLocaleString('en-IN')} per km × ${this.reimbursementObj.distance} km × ${days} day(s)) for ${this.reimbursementObj.vehicleType}.`
          );
          return;
        }
      }
    }
    const roleLimitErr = this.roleAmountLimitExceededMessage();
    if (roleLimitErr) {
      this.openAlertMod(template, roleLimitErr);
      return;
    }
    if (!this.dateInClaimWindow(this.reimbursementObj.fromDate) || !this.dateInClaimWindow(this.reimbursementObj.toDate)) {
      this.openAlertMod(
        template,
        `From and To dates must be in the previous calendar month only (${this.claimDateWindowHint})`
      );
      return;
    }
    if (!this.reimbursementObj.purpose || this.reimbursementObj.purpose.trim() === '') {
      this.openAlertMod(template, 'Please enter the Purpose.');
      return;
    }
    if (/[^a-zA-Z0-9\s]/.test(this.reimbursementObj.purpose)) {
      this.openAlertMod(template, 'Purpose must contain only letters, numbers, and spaces.');
      return;
    }
    if (!this.reimbursementObj.businessJustification || this.reimbursementObj.businessJustification.trim() === '') {
      this.openAlertMod(template, 'Please enter the Business Justification.');
      return;
    }
    if (!this.assignedHodName() || !this.currentUser?.hodId) {
      this.openAlertMod(template, 'Your assigned HOD is missing on your profile. Contact HR before adding a claim.');
      return;
    }
    if (!this.reimbursementObj.preApprovalDate) {
      this.openAlertMod(template, 'Please select the HOD pre-approval date.');
      return;
    }
    if (this.reimbursementObj.fromDate) {
      const pa = String(this.reimbursementObj.preApprovalDate);
      const fd = String(this.reimbursementObj.fromDate);
      if (pa >= fd) {
        this.openAlertMod(template, 'HOD pre-approval date must be before the claim From date.');
        return;
      }
    }
    const isEdit = this.editingClaimIndex !== null;
    const hasNewUpload = !!(this.fileUploads?.length && this.fileUploads.some(f => f.file));
    const hasNewPreApprovalUpload = !!(this.preApprovalFileUploads?.length
      && this.preApprovalFileUploads.some(f => f.file));
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
    const existingPreCount = isEdit
      ? this.ensureClaimPreApprovalDocumentsList(this.ticketClaims[this.editingClaimIndex as number]).length
      : 0;
    if (!hasNewPreApprovalUpload && existingPreCount === 0) {
      this.openAlertMod(template, 'Please attach at least one email from your assigned HOD (' + this.assignedHodName() + ').');
      return;
    }
    try {
      const uploadNewFiles = async (slots: { file?: File }[]): Promise<{ docId: number; fileName: string }[]> => {
        const uploaded: { docId: number; fileName: string }[] = [];
        for (const fileObj of slots || []) {
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

      const mergePreApprovalDocuments = async (
        existing: { docId: number; fileName: string }[]
      ): Promise<{ docId: number; fileName: string }[] | null> => {
        const documents = existing.map((d) => ({ docId: d.docId, fileName: d.fileName }));
        const newDocs = await uploadNewFiles(this.preApprovalFileUploads);
        if (newDocs.length === 0 && hasNewPreApprovalUpload) {
          return null;
        }
        documents.push(...newDocs);
        if (documents.length === 0) {
          this.openAlertMod(template, 'At least one HOD pre-approval email (from ' + this.assignedHodName() + ') is required.');
          return null;
        }
        return documents;
      };

      if (isEdit) {
        const idx = this.editingClaimIndex as number;
        const existing = this.ticketClaims[idx];
        this.ensureClaimDocumentsList(existing);
        const documents: { docId: number; fileName: string }[] = existing.documents.map(
          (d: { docId: number; fileName: string }) => ({ docId: d.docId, fileName: d.fileName })
        );
        const newDocs = await uploadNewFiles(this.fileUploads);
        if (newDocs.length === 0 && hasNewUpload) {
          return;
        }
        documents.push(...newDocs);
        if (documents.length === 0) {
          this.openAlertMod(template, 'At least one supporting document is required.');
          return;
        }
        const preApprovalDocuments = await mergePreApprovalDocuments(
          this.ensureClaimPreApprovalDocumentsList(existing)
        );
        if (preApprovalDocuments === null) {
          return;
        }
        const docIds = documents.map(d => d.docId);
        const preApprovalDocIds = preApprovalDocuments.map(d => d.docId);
        const row = this.mappedProjectsForClaim.find((p) => p.projectId === selectedPid);
        this.ticketClaims[idx] = {
          ...this.buildClaimDraftFromForm(selectedPid, row),
          docIds,
          documents,
          preApprovalDocIds,
          preApprovalDocuments
        };
        this.cancelEditDraftClaim();
        this.openAlertMod(template, 'Claim updated in your ticket.');
        return;
      }

      const documents = await uploadNewFiles(this.fileUploads);
      if (documents.length === 0) {
        return;
      }
      const preApprovalDocuments = await mergePreApprovalDocuments([]);
      if (preApprovalDocuments === null) {
        return;
      }
      const docIds = documents.map(d => d.docId);
      const preApprovalDocIds = preApprovalDocuments.map(d => d.docId);
      const row = this.mappedProjectsForClaim.find((p) => p.projectId === selectedPid);
      this.ticketClaims.push({
        ...this.buildClaimDraftFromForm(selectedPid, row),
        docIds,
        documents,
        preApprovalDocIds,
        preApprovalDocuments
      });
      this.resetAfterSubmit();
      this.fileUploads = [{}];
      this.preApprovalFileUploads = [{}];
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
        this.submissionWindowAllowed = true;
        this.submissionWindowMessage = res.serviceResponse.message || '';
        this.carriedToNextCycle = res.serviceResponse.carriedToNextCycle === true;
        this.processingCycleLabel = res.serviceResponse.processingCycleLabel || '';
      }
    } catch {
      this.submissionWindowAllowed = true;
      this.submissionWindowMessage = '';
    }
  }

  async submitEntireTicket(template: TemplateRef<any>) {
    if (this.ticketClaims.length < 1) {
      this.openAlertMod(template, 'Use "Add claim to ticket" to add at least one claim before submitting.');
      return;
    }
    if (!this.currentUser?.hodId || !this.currentUser?.hodEmail) {
      this.openAlertMod(template, 'HOD information is missing on your profile. Please contact HR.');
      return;
    }
    await this.onGetEmployeeInfo();
    for (let i = 0; i < this.ticketClaims.length; i++) {
      const c = this.ticketClaims[i];
      if (c.projectId == null || c.projectId === '') {
        this.openAlertMod(template, 'Each claim must have a project. Edit any claim missing a project and update it.');
        return;
      }
      if (c.expenditureType === 'Food') {
        if (!c.foodAllowanceType) {
          this.openAlertMod(template, 'Each Food claim must include Food Allowance Type. Edit the incomplete claim.');
          return;
        }
      }
      if (!c.fromDate || !c.toDate) {
        this.openAlertMod(template, 'Each claim must have From and To dates. Edit the incomplete claim.');
        return;
      }
      if (c.fromDate && c.toDate) {
        if (!this.dateInClaimWindow(c.fromDate) || !this.dateInClaimWindow(c.toDate)) {
          this.openAlertMod(
            template,
            `Each claim's From and To dates must be in the previous calendar month only (${this.claimDateWindowHint}). Edit the invalid claim.`
          );
          return;
        }
      }
      if (c.fromDate && c.toDate && String(c.toDate) < String(c.fromDate)) {
        this.openAlertMod(
          template,
          'Each claim must have To date on or after From date. Edit the claim with invalid dates.'
        );
        return;
      }
      if (!c.businessJustification || String(c.businessJustification).trim() === '') {
        this.openAlertMod(template, 'Each claim must include Business Justification. Edit the incomplete claim.');
        return;
      }
      if (!c.preApprovalDate) {
        this.openAlertMod(template, 'Each claim must have a HOD pre-approval date. Edit the incomplete claim.');
        return;
      }
      if (c.fromDate && String(c.preApprovalDate) >= String(c.fromDate)) {
        this.openAlertMod(
          template,
          'Each claim must have HOD pre-approval date before From date. Edit the claim with invalid dates.'
        );
        return;
      }
      if (this.preApprovalDocCountForClaim(c) < 1) {
        this.openAlertMod(template, 'Each claim must include at least one HOD pre-approval email (from your assigned HOD only). Edit the incomplete claim.');
        return;
      }
      if (c.expenditureType === 'Food' && this.isTeamMealFoodAllowanceType(c.foodAllowanceType)) {
        const teamErr = this.validateTeamMealLimitMessage(
          Number(c.amount),
          c.fromDate,
          c.toDate,
          c.teamMemberCount,
          c.foodAllowanceType
        );
        if (teamErr) {
          this.openAlertMod(template, `Claim ${i + 1}: ${teamErr}`);
          return;
        }
        continue;
      }
      if (c.expenditureType === 'Food' && !this.isTeamMealFoodAllowanceType(c.foodAllowanceType)) {
        continue;
      }
      const roleMaxTotal = this.getRoleEligibleAmountTotal(
        c.expenditureType,
        c.fromDate,
        c.toDate,
        c.foodAllowanceType,
        c.travelMode,
        c.teamMemberCount
      );
      if (roleMaxTotal != null && Number(c.amount) > roleMaxTotal) {
        const isTravel = c.expenditureType === 'Travel';
        const perDay = isTravel
          ? this.getRoleTravelModeDailyLimit(c.travelMode)
          : this.getRoleAmountLimit(c.expenditureType);
        const days = this.countClaimInclusiveDays(c.fromDate, c.toDate);
        const scope = isTravel && c.travelMode
          ? `travel mode "${c.travelMode}"`
          : `your job role (${c.expenditureType})`;
        this.openAlertMod(
          template,
          `Claim ${i + 1}: amount exceeds eligible limit of ₹ ${roleMaxTotal.toLocaleString('en-IN')} (₹ ${perDay!.toLocaleString('en-IN')} per day × ${days} day(s)) for ${scope}. Edit the claim.`
        );
        return;
      }
    }
    const sharedFoodErr = this.validateSharedFoodDailyLimitsForTicket(this.ticketClaims);
    if (sharedFoodErr) {
      this.openAlertMod(template, sharedFoodErr);
      return;
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
        teamMemberCount: this.isTeamMealFoodAllowanceType(c.foodAllowanceType)
          ? this.parsedTeamMemberCount(c.teamMemberCount)
          : null,
        dateOfFood: null,
        fromDate: c.fromDate ? new Date(this.toIsoStartOfDay(c.fromDate)!).toISOString() : null,
        toDate: c.toDate ? new Date(this.toIsoEndOfDay(c.toDate)!).toISOString() : null,
        purpose: c.purpose,
        businessJustification: c.businessJustification,
        hodApproval: true,
        preApprovalDate: c.preApprovalDate
          ? new Date(this.toIsoStartOfDay(c.preApprovalDate)!).toISOString()
          : null,
        projectId: pid,
        docIds: (c.documents && c.documents.length ? c.documents.map((d: { docId: number }) => d.docId) : c.docIds) || [],
        preApprovalDocIds: (c.preApprovalDocuments && c.preApprovalDocuments.length
          ? c.preApprovalDocuments.map((d: { docId: number }) => d.docId)
          : c.preApprovalDocIds) || []
      };
      base.recurringExpense = !!c.recurringExpense;
      base.pocProject = !!c.pocProject;
      if (pid === this.RMB_OTHERS_PROJECT_ID) {
        base.othersProjectName = (c.othersProjectName || '').trim();
        base.pocProjectName = null;
      } else if (pid === this.RMB_POC_PROJECT_ID) {
        base.pocProjectName = (c.pocProjectName || '').trim();
        base.othersProjectName = null;
      } else {
        base.othersProjectName = null;
        base.pocProjectName = null;
        base.poNo = c.poNo ? String(c.poNo).trim() : null;
      }
      if (pid === this.RMB_OTHERS_PROJECT_ID || pid === this.RMB_POC_PROJECT_ID) {
        base.clientId = c.clientId != null ? Number(c.clientId) : null;
        base.reimbursementClientId = c.reimbursementClientId != null ? Number(c.reimbursementClientId) : null;
        base.prospectiveClientName = c.prospectiveClientName ? String(c.prospectiveClientName).trim() : null;
        base.clientCategory = c.clientCategory || null;
      } else {
        base.clientId = null;
        base.reimbursementClientId = null;
        base.prospectiveClientName = null;
        base.clientCategory = null;
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
        this.preApprovalFileUploads = [{}];
        const ticketRef = this.displaySubmittedTicketRef(response.serviceResponse);
        const cycleNote = response.serviceMessage
          || (this.processingCycleLabel ? ` Processing cycle: ${this.processingCycleLabel}.` : '');
        const successMsg = ticketRef
          ? `Success! Your request has been registered with Ticket ID:- ${ticketRef}.${cycleNote ? ' ' + cycleNote : ''}`
          : `Success! Your reimbursement ticket was submitted.${cycleNote ? ' ' + cycleNote : ''}`;
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
    this.claimAmountInput = '';
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
      businessJustification: '',
      hodApproval: false,
      preApprovalDate: null,
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      teamMemberCount: null,
      vehicleType: '',
      file: '',
      othersProjectName: '',
      pocProjectName: '',
      othersClientId: null,
      clientPickerKey: null,
      prospectiveClientName: '',
      recurringExpense: false,
      pocProject: false,
      displayClientName: '',
      displayPoNo: '',
      expenditureTypeDescription: ''
    };
    this.fileUploads = [{}];
    this.preApprovalFileUploads = [{}];
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
      this.recalculatePersonalVehicleAmount();
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
    this.recalculatePersonalVehicleAmount();
  }

  onToDateChange(value: string) {
    this.invalidToDate = false;
    if (!value || !this.reimbursementObj.fromDate) {
      this.recalculatePersonalVehicleAmount();
      return;
    }
    if (!this.dateInClaimWindow(value)) {
      this.reimbursementObj.toDate = null;
      this.invalidToDate = true;
      this.recalculatePersonalVehicleAmount();
      return;
    }
    if (String(value) < String(this.reimbursementObj.fromDate)) {
      this.reimbursementObj.toDate = null;
      this.invalidToDate = true;
    }
    this.recalculatePersonalVehicleAmount();
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
  preApprovalFileUploads: any[] = [{}];

  addPreApprovalFileSlot(): void {
    if (this.preApprovalFileUploads.length < 6) {
      this.preApprovalFileUploads.push({});
    }
  }

  removePreApprovalFileSlot(index: number): void {
    this.preApprovalFileUploads.splice(index, 1);
  }

  onPreApprovalFileChange(event: any, index: number, template: TemplateRef<any>): void {
    const file = event?.target?.files?.[0];
    if (!file) {
      if (this.preApprovalFileUploads[index]) {
        this.preApprovalFileUploads[index].file = null;
      }
      return;
    }
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      this.openAlertMod(template, 'File size must not exceed 10 MB.');
      event.target.value = '';
      return;
    }
    this.preApprovalFileUploads[index].file = file;
    this.preApprovalFileUploads[index].uploadedBy = this.currentEmployeeInfo.empId;
  }

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
  this.allTravelModeList = response.serviceResponse || [];
  this.applyRolePolicyFilters();

  console.log("travelModelist ::::::: : ", this.travelModeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetVehicleType() {

  const response: any = await this.reimbursementService.onGetVehicleType().toPromise();
  if (response.serviceStatus == "Success") {
  this.allVehicleTypeList = response.serviceResponse || [];
  this.applyRolePolicyFilters();

  console.log("vehicleTypeList ::::::: : ", this.vehicleTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetFoodType() {

  const response: any = await this.reimbursementService.onGetFoodType().toPromise();
  if (response.serviceStatus == "Success") {
  this.allFoodTypeList = response.serviceResponse || [];
  this.applyRolePolicyFilters();

  console.log("foodTypeList ::::::: : ", this.foodTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }



}








