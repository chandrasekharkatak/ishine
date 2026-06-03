import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { forkJoin } from 'rxjs';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

export interface RmbRolePolicyListRow {
  expensePolicyId: number;
  policyNo: string;
  representativeJobRoleId: number;
  jobRoleName: string;
  jobRoleNames: string[];
  policySummary: string;
  policyCount: number;
  updatedByName?: string;
  updatedOn?: string;
}

@Component({
  standalone: false,
  selector: 'app-reimbursement-role-policy-config',
  templateUrl: './reimbursement-role-policy-config.component.html',
  styleUrls: [
    './reimbursement-role-policy-config.component.css',
    '../reimbursement-config-master.css'
  ]
})
export class ReimbursementRolePolicyConfigComponent implements OnInit, OnChanges {
  readonly foodTeamMealLimitKey = 'Food (Team meal)';
  @Input() expenditureTypeList: any[] = [];
  @Input() travelModeList: any[] = [];
  @Input() vehicleTypeList: any[] = [];
  @Input() foodTypeList: any[] = [];

  /** Loaded from API so the form works even when parent @Input lists are empty. */
  masterExpenditureTypes: any[] = [];
  masterTravelModes: any[] = [];
  masterVehicleTypes: any[] = [];
  masterFoodTypes: any[] = [];
  mastersLoading = false;
  mastersError: string | null = null;

  currentUser: User;
  jobRoleOptions: { jobRoleId: number | string; name: string }[] = [];
  private allJobRoles: { jobRoleId: number | string; name: string; departmentId?: number | string }[] = [];
  selectedJobRoleIds: (number | string)[] = [];
  loading = false;
  saving = false;
  listLoading = false;

  showFormView = false;
  isEditMode = false;
  editJobRoleName = '';
  editExpensePolicyId: number | null = null;
  editPolicyNo = '';

  policyList: RmbRolePolicyListRow[] = [];
  items = 10;
  page = 1;
  isSearchEnabled = false;
  isPolicyTable = true;
  filters: Record<string, unknown> = {};
  sortColumn: string;
  sortColumnType: string;
  sortDirection = 'asc';
  readonly policyColumns = ['blank', 'policyNo', 'jobRoleName', 'policySummary', 'policyCount', 'updatedByName', 'updatedOn'];

  amountLimits: Record<string, string> = {};
  /** Travel mode name -> max amount per day (₹). */
  travelModeDailyLimits: Record<string, string> = {};
  allowedTravelModes: Record<string, boolean> = {};
  allowedVehicleTypes: Record<string, boolean> = {};
  vehicleRatesPerKm: Record<string, string> = {};
  allowedFoodTypes: Record<string, boolean> = {};
  restrictTravelModes = false;
  restrictVehicleTypes = false;
  restrictFoodTypes = false;

  constructor(
    private authenticationService: AuthenticationService,
    private jobRoleService: JobRoleService,
    private reimbursementService: ReimbursementService
  ) {
    this.authenticationService.currentUser.subscribe(x => (this.currentUser = x));
  }

  ngOnInit(): void {
    void this.bootstrap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['expenditureTypeList'] || changes['travelModeList'] || changes['vehicleTypeList'] || changes['foodTypeList']) {
      this.mergeMastersFromInputs();
      this.initCheckboxMaps();
    }
  }

  private async bootstrap(): Promise<void> {
    await Promise.all([this.loadJobRoles(), this.loadReimbursementMasters()]);
    await this.loadPolicyList();
  }

  private mergeMastersFromInputs(): void {
    if (!this.masterExpenditureTypes.length && this.expenditureTypeList?.length) {
      this.masterExpenditureTypes = [...this.expenditureTypeList];
    }
    if (!this.masterTravelModes.length && this.travelModeList?.length) {
      this.masterTravelModes = [...this.travelModeList];
    }
    if (!this.masterVehicleTypes.length && this.vehicleTypeList?.length) {
      this.masterVehicleTypes = [...this.vehicleTypeList];
    }
    if (!this.masterFoodTypes.length && this.foodTypeList?.length) {
      this.masterFoodTypes = [...this.foodTypeList];
    }
  }

  private async loadReimbursementMasters(): Promise<void> {
    this.mastersLoading = true;
    this.mastersError = null;
    try {
      const res: any = await forkJoin({
        expenditure: this.reimbursementService.onGetExpenditureType(),
        travel: this.reimbursementService.getTravelMode(),
        vehicle: this.reimbursementService.onGetVehicleType(),
        food: this.reimbursementService.onGetFoodType()
      })
        .pipe(first())
        .toPromise();

      this.masterExpenditureTypes = this.extractMasterList(res?.expenditure);
      this.masterTravelModes = this.extractMasterList(res?.travel);
      this.masterVehicleTypes = this.extractMasterList(res?.vehicle);
      this.masterFoodTypes = this.extractMasterList(res?.food);
      this.mergeMastersFromInputs();
      this.initCheckboxMaps();

      if (
        !this.masterExpenditureTypes.length &&
        !this.masterTravelModes.length &&
        !this.masterVehicleTypes.length &&
        !this.masterFoodTypes.length
      ) {
        this.mastersError =
          'No reimbursement master data found. Configure Expenditure Type, Travel Mode, Vehicle Type, and Food Allowance Type tabs first.';
      }
    } catch {
      this.mastersError = 'Could not load reimbursement master data.';
      this.mergeMastersFromInputs();
      this.initCheckboxMaps();
    } finally {
      this.mastersLoading = false;
    }
  }

  private extractMasterList(response: any): any[] {
    if (response?.serviceStatus !== 'Success' || !Array.isArray(response.serviceResponse)) {
      return [];
    }
    return response.serviceResponse.filter((row: any) => this.isActiveMaster(row));
  }

  private isActiveMaster(row: any): boolean {
    const active = row?.isActive;
    if (active == null || active === '') {
      return true;
    }
    const s = String(active).trim().toUpperCase();
    return s !== 'N' && s !== 'NO' && s !== 'FALSE' && s !== '0';
  }

  expenditureTypeName(e: any): string {
    return String(e?.expenditureTypeName ?? e?.name ?? '').trim();
  }

  /** Blocks minus, plus, and scientific notation keys. */
  onPositiveAmountKeyDown(event: KeyboardEvent): void {
    if (event.key === '-' || event.key === '+' || event.key === 'e' || event.key === 'E') {
      event.preventDefault();
    }
  }

  /** Allow digits and one decimal point; max 2 digits after decimal. Blocks letters and symbols. */
  onPositiveAmountKeyPress(event: KeyboardEvent): void {
    const key = event.key;
    if (
      key === 'Backspace' ||
      key === 'Delete' ||
      key === 'Tab' ||
      key === 'ArrowLeft' ||
      key === 'ArrowRight' ||
      key === 'Home' ||
      key === 'End'
    ) {
      return;
    }
    const input = event.target as HTMLInputElement;
    const value = input?.value ?? '';
    const start = input?.selectionStart ?? value.length;
    const end = input?.selectionEnd ?? value.length;
    if (key === '.' || key === 'Decimal') {
      if (value.includes('.') || value.slice(start, end).includes('.')) {
        event.preventDefault();
      }
      return;
    }
    if (!/^\d$/.test(key)) {
      event.preventDefault();
      return;
    }
    const next = value.slice(0, start) + key + value.slice(end);
    if (!/^\d{0,12}(\.\d{0,2})?$/.test(next)) {
      event.preventDefault();
    }
  }

  onPositiveAmountFieldChange(record: Record<string, string>, key: string, value: string): void {
    record[key] = this.sanitizePositiveAmountInput(value);
  }

  onPositiveAmountPaste(event: ClipboardEvent, record: Record<string, string>, key: string): void {
    event.preventDefault();
    const input = event.target as HTMLInputElement;
    const pasted = event.clipboardData?.getData('text') ?? '';
    const value = input?.value ?? record[key] ?? '';
    const start = input?.selectionStart ?? value.length;
    const end = input?.selectionEnd ?? value.length;
    const merged = value.slice(0, start) + pasted + value.slice(end);
    record[key] = this.sanitizePositiveAmountInput(merged);
  }

  private formatAmountForInput(value: number | string): string {
    const n = Number(value);
    if (!Number.isFinite(n) || n <= 0) {
      return '';
    }
    const rounded = Math.round(n * 100) / 100;
    return Number.isInteger(rounded) ? String(rounded) : rounded.toFixed(2);
  }

  private sanitizePositiveAmountInput(value: string): string {
    if (value == null || value === '') {
      return '';
    }
    let cleaned = String(value).replace(/[^\d.]/g, '');
    const dot = cleaned.indexOf('.');
    if (dot >= 0) {
      const intPart = cleaned.slice(0, dot).replace(/\./g, '') || '0';
      const fracPart = cleaned.slice(dot + 1).replace(/\./g, '').slice(0, 2);
      cleaned = fracPart.length ? `${intPart}.${fracPart}` : `${intPart}.`;
    }
    return cleaned;
  }

  /**
   * Parses a positive amount (max 2 decimal places).
   * @returns null if empty, false if invalid, otherwise the rounded amount.
   */
  private parsePositiveAmount(raw: string, fieldLabel: string): number | null | false {
    let trimmed = String(raw ?? '').trim();
    if (trimmed.endsWith('.')) {
      trimmed = trimmed.slice(0, -1);
    }
    if (!trimmed) {
      return null;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) {
      void Swal.fire({
        icon: 'warning',
        title: 'Invalid amount',
        text: `${fieldLabel}: enter a positive number with up to 2 decimal places (no letters or special characters).`
      });
      return false;
    }
    const amt = Number(trimmed);
    if (!Number.isFinite(amt) || amt <= 0) {
      void Swal.fire({
        icon: 'warning',
        title: 'Invalid amount',
        text: `${fieldLabel}: amount must be greater than zero.`
      });
      return false;
    }
    return Math.round(amt * 100) / 100;
  }

  /** Not shown in expense policy amount limits (travel uses per-mode limits below). */
  private static readonly EXCLUDED_AMOUNT_LIMIT_TYPES = new Set([
    'travel',
    'software and hardware purchase',
    'miscellaneous'
  ]);

  /** Expenditure types for per-day amount limits (excludes Travel and other non-policy types). */
  nonTravelExpenditureTypes(): any[] {
    return (this.masterExpenditureTypes || []).filter(e => {
      const key = this.expenditureTypeName(e).toLowerCase();
      return key && !ReimbursementRolePolicyConfigComponent.EXCLUDED_AMOUNT_LIMIT_TYPES.has(key);
    });
  }

  hasFoodAmountLimit(): boolean {
    return this.nonTravelExpenditureTypes().some(e => this.expenditureTypeName(e).toLowerCase() === 'food');
  }

  hasSelectedRoles(): boolean {
    return this.selectedRoleCount > 0;
  }

  get selectedRoleCount(): number {
    return this.uniqueSelectedRoleNames().length;
  }

  get isMultipleRolesSelected(): boolean {
    return this.selectedRoleCount > 1;
  }

  selectedJobRoleLabels(): string[] {
    return this.uniqueSelectedRoleNames().sort((a, b) => a.localeCompare(b));
  }

  private uniqueSelectedRoleNames(): string[] {
    const seen = new Set<string>();
    const labels: string[] = [];
    for (const id of this.normalizedSelectedJobRoleIds()) {
      const jr = this.findJobRoleById(id);
      const label = String(jr?.name ?? '').trim();
      const nk = this.normRoleName(label);
      if (nk && !seen.has(nk)) {
        seen.add(nk);
        labels.push(label || String(id));
      }
    }
    return labels;
  }

  private normId(v: unknown): string {
    if (v == null || v === '') {
      return '';
    }
    return String(v);
  }

  private normRoleName(name: unknown): string {
    return String(name ?? '')
      .trim()
      .toLowerCase()
      .replace(/\s+/g, ' ');
  }

  private findJobRoleById(roleId: number | string): { jobRoleId: number | string; name: string } | undefined {
    const id = this.normId(roleId);
    if (!id) {
      return undefined;
    }
    return this.allJobRoles.find(jr => this.normId(jr.jobRoleId) === id);
  }

  private buildUniqueJobRoleOptions(
    roles: { jobRoleId: number | string; name: string; departmentId?: number | string }[]
  ): { jobRoleId: number | string; name: string }[] {
    const byName = new Map<string, { jobRoleId: number | string; name: string }>();
    for (const jr of roles || []) {
      const nameKey = this.normRoleName(jr.name);
      if (!nameKey) {
        continue;
      }
      const existing = byName.get(nameKey);
      if (!existing) {
        byName.set(nameKey, { jobRoleId: jr.jobRoleId, name: jr.name });
        continue;
      }
      const existingLabel = String(existing.name || '');
      const nextLabel = String(jr.name || '');
      if (nextLabel.localeCompare(existingLabel, undefined, { sensitivity: 'base' }) < 0) {
        byName.set(nameKey, { jobRoleId: jr.jobRoleId, name: jr.name });
      }
    }
    return [...byName.values()].sort((a, b) =>
      String(a.name || '').localeCompare(String(b.name || ''))
    );
  }

  private collapseRepresentativeJobRoleIds(roleIds: number[]): number[] {
    if (!roleIds.length) {
      return [];
    }
    const byName = new Map<string, number>();
    for (const id of roleIds) {
      const jr = this.findJobRoleById(id);
      const nk = this.normRoleName(jr?.name);
      if (!nk) {
        continue;
      }
      if (!byName.has(nk)) {
        const fromOpt = this.jobRoleOptions.find(r => this.normRoleName(r.name) === nk);
        byName.set(nk, fromOpt ? Number(fromOpt.jobRoleId) : id);
      }
    }
    return [...byName.values()];
  }

  private normalizedSelectedJobRoleIds(): number[] {
    return (this.selectedJobRoleIds || [])
      .map(id => Number(id))
      .filter(id => Number.isFinite(id) && id > 0);
  }

  private async loadJobRoles(): Promise<void> {
    try {
      const res: any = await this.jobRoleService.getAllJobRole().pipe(first()).toPromise();
      if (res?.serviceStatus === 'Success') {
        this.allJobRoles = (res.serviceResponse || []).map((jr: any) => ({
          jobRoleId: jr.jobRoleId,
          name: jr.name,
          departmentId: jr.departmentId
        }));
        this.jobRoleOptions = this.buildUniqueJobRoleOptions(this.allJobRoles);
      }
    } catch {
      this.allJobRoles = [];
      this.jobRoleOptions = [];
    }
  }

  async loadPolicyList(): Promise<void> {
    this.listLoading = true;
    try {
      const res: any = await this.reimbursementService
        .listAllReimbursementRolePolicies()
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
        this.policyList = res.serviceResponse.map((row: any) => ({
          expensePolicyId: Number(row.expensePolicyId),
          policyNo: row.policyNo || String(row.expensePolicyId ?? ''),
          representativeJobRoleId: Number(row.representativeJobRoleId),
          jobRoleName: row.jobRoleName || '—',
          jobRoleNames: Array.isArray(row.jobRoleNames) ? row.jobRoleNames : [],
          policySummary: row.policySummary || '—',
          policyCount: Number(row.policyCount) || 0,
          updatedByName: row.updatedByName || '',
          updatedOn: row.updatedOn || ''
        }));
      } else {
        this.policyList = [];
      }
    } catch {
      this.policyList = [];
    } finally {
      this.listLoading = false;
    }
  }

  handlePageChange(event: number): void {
    this.page = event;
  }

  sortData(sort: Sort): void {
    if (sort.active) {
      const sortParams: string[] = sort.active.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData: Record<string, unknown>): void {
    this.filters = searchData;
    this.page = 1;
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  async openAddForm(): Promise<void> {
    if (!this.masterExpenditureTypes.length && !this.mastersLoading) {
      await this.loadReimbursementMasters();
    }
    this.isEditMode = false;
    this.editJobRoleName = '';
    this.editExpensePolicyId = null;
    this.editPolicyNo = '';
    this.selectedJobRoleIds = [];
    this.resetDraft();
    this.showFormView = true;
  }

  async openEditForm(row: RmbRolePolicyListRow): Promise<void> {
    if (!this.masterExpenditureTypes.length && !this.mastersLoading) {
      await this.loadReimbursementMasters();
    }
    this.isEditMode = true;
    this.editExpensePolicyId = row.expensePolicyId;
    this.editPolicyNo = row.policyNo;
    this.editJobRoleName = row.jobRoleName;
    this.selectedJobRoleIds = row.jobRoleNames.length
      ? this.mapRoleNamesToRepresentativeIds(row.jobRoleNames)
      : [row.representativeJobRoleId];
    this.resetDraft();
    this.showFormView = true;
    await this.loadPoliciesForSelection();
  }

  cancelForm(): void {
    this.showFormView = false;
    this.isEditMode = false;
    this.editJobRoleName = '';
    this.editExpensePolicyId = null;
    this.editPolicyNo = '';
    this.selectedJobRoleIds = [];
    this.resetDraft();
  }

  private initCheckboxMaps(): void {
    for (const t of this.masterTravelModes || []) {
      const name = t?.modeType || t?.travelModeName || '';
      if (name && this.allowedTravelModes[name] === undefined) {
        this.allowedTravelModes[name] = false;
      }
      if (name && this.travelModeDailyLimits[name] === undefined) {
        this.travelModeDailyLimits[name] = '';
      }
    }
    for (const v of this.masterVehicleTypes || []) {
      const name = v?.vehicleTypeName || '';
      if (name && this.allowedVehicleTypes[name] === undefined) {
        this.allowedVehicleTypes[name] = false;
      }
      if (name && this.vehicleRatesPerKm[name] === undefined) {
        this.vehicleRatesPerKm[name] = '';
      }
    }
    for (const f of this.masterFoodTypes || []) {
      const name = f?.foodTypeName || '';
      if (name && this.allowedFoodTypes[name] === undefined) {
        this.allowedFoodTypes[name] = false;
      }
    }
    for (const e of this.nonTravelExpenditureTypes()) {
      const name = this.expenditureTypeName(e);
      if (name && this.amountLimits[name] === undefined) {
        this.amountLimits[name] = '';
      }
    }
  }

  private mapRoleNamesToRepresentativeIds(roleNames: string[]): number[] {
    const ids: number[] = [];
    for (const name of roleNames || []) {
      const nk = this.normRoleName(name);
      const match = this.jobRoleOptions.find(r => this.normRoleName(r.name) === nk);
      if (match) {
        ids.push(Number(match.jobRoleId));
      }
    }
    return this.collapseRepresentativeJobRoleIds(ids.map(id => Number(id)));
  }

  async onJobRoleSelectionChange(): Promise<void> {
    if (this.isEditMode) {
      return;
    }
    this.selectedJobRoleIds = this.collapseRepresentativeJobRoleIds(this.normalizedSelectedJobRoleIds());
    const ids = this.normalizedSelectedJobRoleIds();
    if (!ids.length) {
      this.resetDraft();
      return;
    }
    await this.loadPoliciesForSelection();
  }

  private async loadPoliciesForSelection(): Promise<void> {
    const ids = this.normalizedSelectedJobRoleIds();
    if (!ids.length) {
      return;
    }
    this.loading = true;
    this.resetDraft();
    try {
      const res: any = await this.reimbursementService
        .fetchReimbursementRolePolicy(
          this.isEditMode ? undefined : ids[0],
          this.isEditMode ? this.editExpensePolicyId ?? undefined : undefined
        )
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success' && res.serviceResponse?.policies) {
        this.applyPoliciesFromRows(res.serviceResponse.policies);
        if (this.isEditMode && Array.isArray(res.serviceResponse.jobRoleIds)) {
          this.selectedJobRoleIds = res.serviceResponse.jobRoleIds;
        }
        if (res.serviceResponse?.policyNo) {
          this.editPolicyNo = res.serviceResponse.policyNo;
        }
      }
    } finally {
      this.loading = false;
    }
  }

  private applyPoliciesFromRows(policies: any[]): void {
    for (const p of policies || []) {
      const cat = String(p.policyCategory || '').toUpperCase();
      const item = p.itemName || '';
      if (cat === 'AMOUNT_LIMIT' && item) {
        this.amountLimits[item] = p.maxAmount != null ? this.formatAmountForInput(p.maxAmount) : '';
      } else if (cat === 'TRAVEL_MODE' && item) {
        this.restrictTravelModes = true;
        this.allowedTravelModes[item] = p.allowed !== false;
      } else if (cat === 'TRAVEL_MODE_LIMIT' && item && !this.isPersonalVehicleTravelMode(item)) {
        this.travelModeDailyLimits[item] = p.maxAmount != null ? this.formatAmountForInput(p.maxAmount) : '';
      } else if (cat === 'VEHICLE_TYPE' && item) {
        this.restrictVehicleTypes = true;
        this.allowedVehicleTypes[item] = p.allowed !== false;
      } else if (cat === 'VEHICLE_RATE' && item) {
        this.vehicleRatesPerKm[item] = p.maxAmount != null ? this.formatAmountForInput(p.maxAmount) : '';
      } else if (cat === 'FOOD_ALLOWANCE' && item) {
        this.restrictFoodTypes = true;
        this.allowedFoodTypes[item] = p.allowed !== false;
      }
    }
  }

  private resetDraft(): void {
    this.amountLimits = {};
    this.travelModeDailyLimits = {};
    this.allowedTravelModes = {};
    this.allowedVehicleTypes = {};
    this.vehicleRatesPerKm = {};
    this.allowedFoodTypes = {};
    this.restrictTravelModes = false;
    this.restrictVehicleTypes = false;
    this.restrictFoodTypes = false;
    this.initCheckboxMaps();
  }

  private buildPoliciesPayload(): any[] | null {
    const policies: any[] = [];
    for (const e of this.nonTravelExpenditureTypes()) {
      const name = this.expenditureTypeName(e);
      const raw = name ? String(this.amountLimits[name] || '').trim() : '';
      if (name && raw) {
        const amt = this.parsePositiveAmount(raw, `${name} max per day`);
        if (amt === false) {
          return null;
        }
        policies.push({ policyCategory: 'AMOUNT_LIMIT', itemName: name, maxAmount: amt, allowed: true });
      }
    }

    // Optional: separate Team meal / working lunch limit (per member per day).
    const teamMealRaw = String(this.amountLimits[this.foodTeamMealLimitKey] || '').trim();
    if (teamMealRaw) {
      const amt = this.parsePositiveAmount(teamMealRaw, `Team meal / working lunch max per member per day`);
      if (amt === false) {
        return null;
      }
      policies.push({ policyCategory: 'AMOUNT_LIMIT', itemName: this.foodTeamMealLimitKey, maxAmount: amt, allowed: true });
    }
    for (const [name, rawLimit] of Object.entries(this.travelModeDailyLimits)) {
      if (this.isPersonalVehicleTravelMode(name)) {
        continue;
      }
      const limitStr = String(rawLimit || '').trim();
      if (!name || !limitStr) {
        continue;
      }
      const amt = this.parsePositiveAmount(limitStr, `${name} max per day`);
      if (amt === false) {
        return null;
      }
      policies.push({ policyCategory: 'TRAVEL_MODE_LIMIT', itemName: name, maxAmount: amt, allowed: true });
    }
    if (this.restrictTravelModes) {
      for (const [name, allowed] of Object.entries(this.allowedTravelModes)) {
        if (allowed) {
          policies.push({ policyCategory: 'TRAVEL_MODE', itemName: name, allowed: true });
        }
      }
    }
    if (this.restrictVehicleTypes) {
      for (const [name, allowed] of Object.entries(this.allowedVehicleTypes)) {
        if (allowed) {
          policies.push({ policyCategory: 'VEHICLE_TYPE', itemName: name, allowed: true });
        }
      }
    }
    for (const [name, rawRate] of Object.entries(this.vehicleRatesPerKm)) {
      const rateStr = String(rawRate || '').trim();
      if (!name || !rateStr) {
        continue;
      }
      const rate = this.parsePositiveAmount(rateStr, `${name} per km rate`);
      if (rate === false) {
        return null;
      }
      policies.push({ policyCategory: 'VEHICLE_RATE', itemName: name, maxAmount: rate, allowed: true });
    }
    if (this.restrictFoodTypes) {
      for (const [name, allowed] of Object.entries(this.allowedFoodTypes)) {
        if (allowed) {
          policies.push({ policyCategory: 'FOOD_ALLOWANCE', itemName: name, allowed: true });
        }
      }
    }
    return policies;
  }

  async save(): Promise<void> {
    const selectedIds = this.normalizedSelectedJobRoleIds();
    if (!selectedIds.length) {
      void Swal.fire({ icon: 'warning', title: 'Select at least one job role.' });
      return;
    }
    const policies = this.buildPoliciesPayload();
    if (policies == null) {
      return;
    }
    this.saving = true;
    try {
      const res: any = await this.reimbursementService
        .saveReimbursementRolePolicy({
          expensePolicyId: this.isEditMode ? this.editExpensePolicyId ?? undefined : undefined,
          jobRoleIds: selectedIds,
          updatedBy: Number(this.currentUser?.empId),
          policies
        })
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success') {
        const applied = res?.serviceResponse?.appliedJobRoleIds;
        const roleTitles = res?.serviceResponse?.jobRoleNames?.length
          ?? this.selectedJobRoleLabels();
        void Swal.fire({
          icon: 'success',
          title: 'Saved',
          text: `Expense policy ${res?.serviceResponse?.policyNo ?? ''} updated for ${roleTitles.length} role title(s)`
            + (Array.isArray(applied) ? ` across ${applied.length} job role record(s).` : '.')
        });
        this.cancelForm();
        await this.loadPolicyList();
      } else {
        void Swal.fire({
          icon: 'error',
          title: 'Save failed',
          text: res?.serviceError || res?.serviceResponse || 'Unexpected error.'
        });
      }
    } catch (e: any) {
      void Swal.fire({ icon: 'error', title: 'Save failed', text: e?.message || 'Unexpected error.' });
    } finally {
      this.saving = false;
    }
  }

  async deletePolicy(row: RmbRolePolicyListRow): Promise<void> {
    const result = await Swal.fire({
      icon: 'warning',
      title: 'Delete expense policy?',
      html: `Remove expense policy <strong>${row.policyNo}</strong> for <strong>${row.jobRoleName}</strong>?`,
      showCancelButton: true,
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#d33'
    });
    if (!result.isConfirmed) {
      return;
    }
    try {
      const res: any = await this.reimbursementService
        .deleteReimbursementRolePolicy(undefined, row.expensePolicyId)
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success') {
        void Swal.fire({ icon: 'success', title: 'Deleted', text: res.serviceMessage || 'Expense policy deleted.' });
        await this.loadPolicyList();
      } else {
        void Swal.fire({
          icon: 'error',
          title: 'Delete failed',
          text: res?.serviceError || res?.serviceResponse || 'Unexpected error.'
        });
      }
    } catch (e: any) {
      void Swal.fire({ icon: 'error', title: 'Delete failed', text: e?.message || 'Unexpected error.' });
    }
  }

  isPersonalVehicleTravelMode(mode?: string | null): boolean {
    const m = String(mode ?? '')
      .trim()
      .toLowerCase();
    return m.includes('own vehicle') || m.includes('personal vehicle');
  }

  travelModeNames(): string[] {
    return (this.masterTravelModes || [])
      .map(t => t?.modeType || t?.travelModeName)
      .filter(Boolean);
  }

  vehicleTypeNames(): string[] {
    return (this.masterVehicleTypes || []).map(v => v?.vehicleTypeName).filter(Boolean);
  }

  foodTypeNames(): string[] {
    return (this.masterFoodTypes || []).map(f => f?.foodTypeName).filter(Boolean);
  }
}
