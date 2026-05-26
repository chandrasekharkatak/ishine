import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { forkJoin } from 'rxjs';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

/** Department row from {@code api/getAllDepartments}. */
export interface TrvRsDeptRow {
  deptId: string;
  name: string;
}

/** Normalized employee row for dropdowns (from {@code api/getAllEmployeesByDepartmentIds}). */
export interface TrvRsEmpRow {
  empId: string;
  name: string;
  departmentId: string;
  jobRoleId: string;
}

export interface TrvRsLevelDraft {
  order: number;
  departmentIds: string[];
  jobRoleIds: string[];
  /** System mode: reporting manager, HOD, pool, or specific person in scope. */
  routingMode: string;
  /** Empty = any one in pool (where applicable). */
  specificEmployeeId: string;
  /** Populated from {@code api/getAllEmployeesByDepartmentIds} for this level’s department selection. */
  _employeesInDepts?: TrvRsEmpRow[];
  employeesLoading?: boolean;
}

export interface TrvRsAdminDraft {
  departmentId: string;
  assigneeEmployeeId: string;
}

export interface TrvRsRuleSetDraft {
  id: string;
  /** Persisted {@code travel_approval_matrix.matrix_id}; null until first save. */
  matrixId?: number | null;
  name: string;
  /** Submitter must match these departments (optional; empty = all). */
  applicabilityDepartmentIds: string[];
  /** Submitter must match one of these job roles within applicability departments (optional). */
  applicabilityJobRoleIds: string[];
  levels: TrvRsLevelDraft[];
  admin: TrvRsAdminDraft;
  saving?: boolean;
  /** Table display (search / sort). */
  applicabilitySummary?: string;
  /** Table hover tooltip content for roles in applicability scope. */
  applicabilityRolesTooltip?: string;
  levelsSummary?: string;
  createdByName?: string;
  createdOn?: string;
  updatedByName?: string;
  updatedOn?: string;
}

@Component({
  standalone: false,
  selector: 'app-travel-approval-matrix-config',
  templateUrl: './travel-approval-matrix-config.component.html',
  styleUrls: ['./travel-approval-matrix-config.component.css']
})
export class TravelApprovalMatrixConfigComponent implements OnInit {
  readonly maxLevels = 5;

  readonly routingOptions: { value: string; label: string; hint: string }[] = [
    {
      value: 'REPORTING_MANAGER',
      label: 'Reporting manager',
      hint: 'Approver comes from org data (submitter’s manager). Eligibility is set under the approval matrix name, not here.'
    },
    {
      value: 'HOD_SUBMITTER_DEPT',
      label: 'HOD of submitter’s department',
      hint: 'Approver is the HOD of the submitter’s own department. Eligibility is set under the approval matrix name, not here.'
    },
    {
      value: 'POOL_ANY_IN_SCOPE',
      label: 'Pool in scope (any one approver)',
      hint: 'Use the multi-select lists on this level: departments and job roles. Leave Assign to empty so any pool member may approve, or pick one person.'
    },
    {
      value: 'SPECIFIC_IN_SCOPE',
      label: 'Named approver in selected department(s)',
      hint: 'Pick departments on this level, then choose one named approver after the list loads.'
    }
  ];

  departmentsList: TrvRsDeptRow[] = [];
  /** All job roles from {@code api/getAllJobRole} (filtered per level by department). */
  allJobRoles: any[] = [];

  mastersLoading = true;
  mastersError: string | null = null;

  /** Saved matrices shown in the table. */
  ruleSets: TrvRsRuleSetDraft[] = [];
  /** Add / edit form draft (separate from table list). */
  activeDraft: TrvRsRuleSetDraft | null = null;
  showFormView = false;

  items = 10;
  page = 1;
  isSearchEnabled = false;
  isMatrixTable = true;
  filters: Record<string, unknown> = {};
  sortColumn: string;
  sortColumnType: string;
  sortDirection = 'asc';
  readonly matrixColumns = [
    'blank',
    'name',
    'applicabilitySummary',
    'levelsSummary',
    'createdByName',
    'createdOn',
    'updatedByName',
    'updatedOn'
  ];

  currentUser: User;

  /** Travel Admin assignee lists keyed by {@code deptId} string. */
  private readonly adminEmployeesByDept = new Map<string, TrvRsEmpRow[]>();
  private adminEmployeesLoading = false;

  constructor(
    private readonly departmentService: DepartmentService,
    private readonly jobRoleService: JobRoleService,
    private readonly employeeService: EmployeeService,
    private readonly travelDeskService: TravelDeskService,
    private readonly authenticationService: AuthenticationService
  ) {
    this.authenticationService.currentUser.subscribe((x) => (this.currentUser = x));
  }

  ngOnInit(): void {
    this.loadMasters();
  }

  private loadMasters(): void {
    this.mastersLoading = true;
    this.mastersError = null;
    forkJoin({
      depts: this.departmentService.getAllDepartments(),
      roles: this.jobRoleService.getAllJobRole()
    })
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          const dResp = res.depts;
          const rResp = res.roles;
          if (dResp?.serviceStatus === 'Success' && Array.isArray(dResp.serviceResponse)) {
            this.departmentsList = dResp.serviceResponse
              .map((row: any) => ({
                deptId: this.normId(row.deptId),
                name: (row.name ?? row.departmentName ?? 'Department') as string
              }))
              .sort((a, b) => a.name.localeCompare(b.name));
          } else {
            this.departmentsList = [];
            this.mastersError =
              (typeof dResp?.serviceResponse === 'string' && dResp.serviceResponse) ||
              'Could not load departments.';
          }
          if (rResp?.serviceStatus === 'Success' && Array.isArray(rResp.serviceResponse)) {
            this.allJobRoles = rResp.serviceResponse;
          } else {
            this.allJobRoles = [];
            if (!this.mastersError) {
              this.mastersError =
                (typeof rResp?.serviceResponse === 'string' && rResp.serviceResponse) ||
                'Could not load job roles.';
            }
          }
          this.mastersLoading = false;
          this.loadSavedMatrices();
        },
        error: () => {
          this.mastersLoading = false;
          this.mastersError = 'Could not load departments or job roles.';
          this.loadSavedMatrices();
        }
      });
  }

  private loadSavedMatrices(): void {
    this.travelDeskService
      .getAllTravelApprovalMatrices()
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
            this.ruleSets = res.serviceResponse.map((row: any) => this.mapApiMatrixToDraft(row));
          } else {
            this.ruleSets = [];
          }
        },
        error: () => {
          this.ruleSets = [];
        }
      });
  }

  private mapApiMatrixToDraft(row: any): TrvRsRuleSetDraft {
    const deptIds = (row.applicabilityDepartmentIds ?? []).map((id: unknown) => this.normId(id));
    const roleIds = (row.applicabilityJobRoleIds ?? []).map((id: unknown) => this.normId(id));
    const draft: TrvRsRuleSetDraft = {
      id: this.genId('rs'),
      matrixId: row.matrixId != null ? Number(row.matrixId) : null,
      saving: false,
      name: row.name ?? 'Approval matrix',
      createdByName: row.createdByName ?? '',
      createdOn: row.createdOn ?? '',
      updatedByName: row.updatedByName ?? '',
      updatedOn: row.updatedOn ?? '',
      applicabilityDepartmentIds: deptIds,
      applicabilityJobRoleIds: this.collapseRepresentativeJobRoleIds(roleIds, deptIds),
      levels: (row.levels ?? []).map((l: any) => {
        const levelDeptIds = (l.departmentIds ?? []).map((id: unknown) => this.normId(id));
        const levelRoleIds = (l.jobRoleIds ?? []).map((id: unknown) => this.normId(id));
        return {
          order: Number(l.order) || 1,
          departmentIds: levelDeptIds,
          jobRoleIds: this.collapseRepresentativeJobRoleIds(levelRoleIds, levelDeptIds),
          routingMode: l.routing ?? 'REPORTING_MANAGER',
          specificEmployeeId: l.assigneeEmployeeId != null ? this.normId(l.assigneeEmployeeId) : '',
          _employeesInDepts: [],
          employeesLoading: false
        };
      }),
      admin: {
        departmentId: row.admin?.departmentId != null ? this.normId(row.admin.departmentId) : '',
        assigneeEmployeeId:
          row.admin?.assigneeEmployeeId != null ? this.normId(row.admin.assigneeEmployeeId) : ''
      }
    };
    this.enrichDraftForTable(draft);
    return draft;
  }

  private enrichDraftForTable(rs: TrvRsRuleSetDraft): void {
    const deptLabels = this.applicabilityDepartmentLabels(rs);
    const roleLabels = this.applicabilityJobRoleLabels(rs);
    // Table should show only department names; roles are shown via hover tooltip.
    rs.applicabilitySummary = deptLabels.length ? deptLabels.join(', ') : 'All departments';
    rs.applicabilityRolesTooltip = roleLabels.length ? roleLabels.join(', ') : 'All roles';
    rs.levelsSummary = `${rs.levels?.length ?? 0} level(s) + admin`;
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

  openAddForm(): void {
    const rs: TrvRsRuleSetDraft = {
      id: this.genId('rs'),
      matrixId: null,
      name: `Approval matrix ${this.ruleSets.length + 1}`,
      applicabilityDepartmentIds: [],
      applicabilityJobRoleIds: [],
      levels: [this.newLevel(1)],
      admin: { departmentId: this.defaultAdminDeptId(), assigneeEmployeeId: '' },
      saving: false
    };
    this.activeDraft = rs;
    this.showFormView = true;
    this.prefetchAdminEmployees(rs.admin.departmentId);
  }

  openEditForm(row: TrvRsRuleSetDraft): void {
    const restored = this.restoreDraftFromBackup(this.serializeDraftForBackup(row));
    this.activeDraft = {
      id: this.genId('rs-edit'),
      matrixId: restored.matrixId ?? row.matrixId,
      name: restored.name ?? row.name,
      applicabilityDepartmentIds: restored.applicabilityDepartmentIds ?? [],
      applicabilityJobRoleIds: restored.applicabilityJobRoleIds ?? [],
      levels: restored.levels ?? [],
      admin: restored.admin ?? { departmentId: '', assigneeEmployeeId: '' },
      saving: false,
      createdByName: row.createdByName,
      createdOn: row.createdOn,
      updatedByName: row.updatedByName,
      updatedOn: row.updatedOn
    };
    this.ensureAdminDept(this.activeDraft);
    this.showFormView = true;
    this.activeDraft.levels.forEach((l) => {
      this.normalizeLevelRouting(l);
      this.loadEmployeesForLevel(l);
    });
    this.prefetchAdminEmployees(this.activeDraft.admin.departmentId);
  }

  backToList(): void {
    this.showFormView = false;
    this.activeDraft = null;
  }

  cancelForm(): void {
    this.backToList();
  }

  /** One representative {@code jobRoleId} per unique role title (for multi-select display). */
  private collapseRepresentativeJobRoleIds(roleIds: string[], deptIds: string[]): string[] {
    if (!roleIds?.length) {
      return [];
    }
    const options = this.rolesForDepartments(deptIds);
    const byName = new Map<string, string>();
    for (const id of roleIds) {
      const jr = this.findJobRoleById(id);
      const nk = this.normRoleName(jr?.name);
      if (!nk) {
        continue;
      }
      if (!byName.has(nk)) {
        const fromOpt = options.find((r) => this.normRoleName(r.name) === nk);
        byName.set(nk, fromOpt ? this.normId(fromOpt.jobRoleId) : this.normId(id));
      }
    }
    return [...byName.values()];
  }

  private normId(v: unknown): string {
    if (v == null || v === '') {
      return '';
    }
    return String(v);
  }

  /** Case-insensitive role name key (same title in multiple departments → one entry in lists). */
  private normRoleName(name: unknown): string {
    return String(name ?? '')
      .trim()
      .toLowerCase()
      .replace(/\s+/g, ' ');
  }

  private findJobRoleById(roleId: string): any | undefined {
    const id = this.normId(roleId);
    if (!id) {
      return undefined;
    }
    return this.allJobRoles.find((jr) => this.normId(jr.jobRoleId) === id);
  }

  /** All {@code jobRoleId} values that share the same role title as any selected id. */
  expandJobRoleIds(selectedIds: string[]): string[] {
    if (!selectedIds?.length || !this.allJobRoles.length) {
      return [];
    }
    const nameKeys = new Set<string>();
    for (const id of selectedIds) {
      const jr = this.findJobRoleById(id);
      const nk = this.normRoleName(jr?.name);
      if (nk) {
        nameKeys.add(nk);
      }
    }
    if (!nameKeys.size) {
      return selectedIds.map((x) => this.normId(x)).filter(Boolean);
    }
    const out = new Set<string>();
    for (const jr of this.allJobRoles) {
      const nk = this.normRoleName(jr.name);
      if (nameKeys.has(nk)) {
        const rid = this.normId(jr.jobRoleId);
        if (rid) {
          out.add(rid);
        }
      }
    }
    return [...out];
  }

  private uniqueJobRoleLabels(ids: string[], deptIds: string[]): string[] {
    const optionRows = this.rolesForDepartments(deptIds);
    const seen = new Set<string>();
    const labels: string[] = [];
    for (const id of ids ?? []) {
      const row =
        optionRows.find((r) => this.normId(r.jobRoleId) === this.normId(id)) ?? this.findJobRoleById(id);
      const label = String(row?.name ?? '').trim() || this.normId(id);
      const nk = this.normRoleName(label);
      if (nk && !seen.has(nk)) {
        seen.add(nk);
        labels.push(label);
      }
    }
    return labels.sort((a, b) => a.localeCompare(b));
  }

  /** After {@code app-my-select} updates matrix departments, prune job roles that no longer apply. */
  afterApplicabilityDepartmentsChange(rs: TrvRsRuleSetDraft): void {
    const allowedNames = new Set(
      this.rolesForDepartments(rs.applicabilityDepartmentIds ?? []).map((r) => this.normRoleName(r.name))
    );
    const kept = (rs.applicabilityJobRoleIds ?? []).filter((id) => {
      const jr = this.findJobRoleById(id);
      return jr && allowedNames.has(this.normRoleName(jr.name));
    });
    // Important: the roles dropdown options are de-duped by role title. If we keep raw ids from
    // other departments, the select can't map them to an option label and shows ids instead.
    rs.applicabilityJobRoleIds = this.collapseRepresentativeJobRoleIds(
      kept.map((x) => this.normId(x)).filter(Boolean),
      rs.applicabilityDepartmentIds ?? []
    );
    this.enrichDraftForTable(rs);
  }

  applicabilityDepartmentLabels(rs: TrvRsRuleSetDraft): string[] {
    const ids = rs.applicabilityDepartmentIds ?? [];
    return ids.map((id) => {
      const row = this.departmentsList.find((d) => this.normId(d.deptId) === this.normId(id));
      return row?.name || this.normId(id);
    });
  }

  applicabilityJobRoleLabels(rs: TrvRsRuleSetDraft): string[] {
    return this.uniqueJobRoleLabels(rs.applicabilityJobRoleIds ?? [], rs.applicabilityDepartmentIds ?? []);
  }

  levelApproverDepartmentLabels(level: TrvRsLevelDraft): string[] {
    const ids = level.departmentIds ?? [];
    return ids.map((id) => {
      const row = this.departmentsList.find((d) => this.normId(d.deptId) === this.normId(id));
      return row?.name || this.normId(id);
    });
  }

  levelApproverJobRoleLabels(level: TrvRsLevelDraft): string[] {
    return this.uniqueJobRoleLabels(level.jobRoleIds ?? [], level.departmentIds ?? []);
  }

  afterLevelApproverDepartmentsChange(level: TrvRsLevelDraft): void {
    this.onLevelDepartmentsChange(level);
  }

  afterLevelApproverRolesChange(level: TrvRsLevelDraft): void {
    this.onLevelRolesChange(level);
  }

  private defaultAdminDeptId(): string {
    const fin = this.departmentsList.find((d) => /admin/i.test(d.name || ''));
    return fin?.deptId ?? this.departmentsList[0]?.deptId ?? '';
  }

  private ensureAdminDept(rs: TrvRsRuleSetDraft): void {
    const cur = this.normId(rs.admin.departmentId);
    if (!cur || !this.departmentsList.some((d) => d.deptId === cur)) {
      rs.admin.departmentId = this.defaultAdminDeptId();
      rs.admin.assigneeEmployeeId = '';
      this.prefetchAdminEmployees(rs.admin.departmentId);
    }
  }

  /** Older drafts may miss applicability arrays. */
  private ensureApplicabilityShape(rs: TrvRsRuleSetDraft): void {
    if (!Array.isArray(rs.applicabilityDepartmentIds)) {
      rs.applicabilityDepartmentIds = [];
    }
    if (!Array.isArray(rs.applicabilityJobRoleIds)) {
      rs.applicabilityJobRoleIds = [];
    }
  }

  private nextId = 1;
  private genId(prefix: string): string {
    return `${prefix}-${this.nextId++}`;
  }

  isNewMatrix(rs: TrvRsRuleSetDraft | null): boolean {
    return rs != null && rs.matrixId == null;
  }

  private removeFromTable(rs: TrvRsRuleSetDraft): void {
    this.ruleSets = this.ruleSets.filter((r) => {
      if (rs.matrixId != null && r.matrixId != null) {
        return r.matrixId !== rs.matrixId;
      }
      return r.id !== rs.id;
    });
  }

  private serializeDraftForBackup(rs: TrvRsRuleSetDraft): string {
    return JSON.stringify({
      matrixId: rs.matrixId,
      name: rs.name,
      applicabilityDepartmentIds: rs.applicabilityDepartmentIds ?? [],
      applicabilityJobRoleIds: rs.applicabilityJobRoleIds ?? [],
      levels: (rs.levels ?? []).map((l) => ({
        order: l.order,
        departmentIds: l.departmentIds ?? [],
        jobRoleIds: l.jobRoleIds ?? [],
        routingMode: l.routingMode,
        specificEmployeeId: l.specificEmployeeId ?? ''
      })),
      admin: { ...rs.admin }
    });
  }

  private restoreDraftFromBackup(json: string): Partial<TrvRsRuleSetDraft> {
    const raw = JSON.parse(json);
    return {
      matrixId: raw.matrixId,
      name: raw.name,
      applicabilityDepartmentIds: [...(raw.applicabilityDepartmentIds ?? [])],
      applicabilityJobRoleIds: [...(raw.applicabilityJobRoleIds ?? [])],
      levels: (raw.levels ?? []).map((l: any) => ({
        order: l.order,
        departmentIds: [...(l.departmentIds ?? [])],
        jobRoleIds: [...(l.jobRoleIds ?? [])],
        routingMode: l.routingMode,
        specificEmployeeId: l.specificEmployeeId ?? '',
        _employeesInDepts: [],
        employeesLoading: false
      })),
      admin: { ...raw.admin }
    };
  }

  saveRuleSet(): void {
    if (this.activeDraft) {
      this.persistRuleSet(this.activeDraft, false);
    }
  }

  updateRuleSet(): void {
    if (this.activeDraft) {
      this.persistRuleSet(this.activeDraft, true);
    }
  }

  deleteRuleSet(rs: TrvRsRuleSetDraft): void {
    const title = rs.name?.trim() || 'this approval matrix';
    void Swal.fire({
      icon: 'warning',
      title: 'Delete approval matrix?',
      text: `"${title}" will be removed permanently.`,
      showCancelButton: true,
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#dc3545'
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }
      if (this.isNewMatrix(rs)) {
        this.backToList();
        return;
      }
      rs.saving = true;
      this.travelDeskService
        .deleteTravelApprovalMatrix(Number(rs.matrixId))
        .pipe(first())
        .subscribe({
          next: (res: any) => {
            rs.saving = false;
            if (res?.serviceStatus === 'Success') {
              this.removeFromTable(rs);
              if (this.activeDraft?.id === rs.id) {
                this.backToList();
              }
              void Swal.fire({ icon: 'success', title: 'Deleted', text: 'Approval matrix was deleted.', confirmButtonText: 'OK' });
            } else {
              void Swal.fire({
                icon: 'error',
                title: 'Delete failed',
                text: res?.serviceError || res?.serviceResponse || 'Could not delete approval matrix.'
              });
            }
          },
          error: () => {
            rs.saving = false;
            void Swal.fire({ icon: 'error', title: 'Delete failed', text: 'Could not reach the server.' });
          }
        });
    });
  }

  newLevel(order: number): TrvRsLevelDraft {
    return {
      order,
      departmentIds: [],
      jobRoleIds: [],
      routingMode: 'REPORTING_MANAGER',
      specificEmployeeId: '',
      _employeesInDepts: [],
      employeesLoading: false
    };
  }

  addLevel(rs: TrvRsRuleSetDraft): void {
    if (rs.levels.length >= this.maxLevels) {
      return;
    }
    const next = rs.levels.length + 1;
    rs.levels = [...rs.levels, this.newLevel(next)];
    this.renumberLevels(rs);
  }

  removeLevel(rs: TrvRsRuleSetDraft, index: number): void {
    if (rs.levels.length <= 1) {
      return;
    }
    rs.levels = rs.levels.filter((_, i) => i !== index);
    this.renumberLevels(rs);
  }

  private renumberLevels(rs: TrvRsRuleSetDraft): void {
    rs.levels.forEach((l, i) => (l.order = i + 1));
  }

  /** Unique job role titles for selected departments (one row per name, even if ids differ per dept). */
  rolesForDepartments(deptIds: string[]): any[] {
    if (!deptIds?.length || !this.allJobRoles.length) {
      return [];
    }
    const deptSet = new Set(deptIds.map((x) => this.normId(x)));
    const byName = new Map<string, any>();
    for (const jr of this.allJobRoles) {
      const dj = this.normId(jr.departmentId);
      if (!deptSet.has(dj)) {
        continue;
      }
      const nameKey = this.normRoleName(jr.name);
      if (!nameKey) {
        continue;
      }
      const existing = byName.get(nameKey);
      if (!existing) {
        byName.set(nameKey, jr);
        continue;
      }
      const existingLabel = String(existing.name || '');
      const nextLabel = String(jr.name || '');
      if (nextLabel.localeCompare(existingLabel, undefined, { sensitivity: 'base' }) < 0) {
        byName.set(nameKey, jr);
      }
    }
    return [...byName.values()].sort((a, b) =>
      String(a.name || '').localeCompare(String(b.name || ''))
    );
  }

  /** Coerce legacy or invalid routing values (e.g. removed job-role route options). */
  private normalizeLevelRouting(level: TrvRsLevelDraft): void {
    const ok = this.routingOptions.some((o) => o.value === level.routingMode);
    if (!ok) {
      level.routingMode = 'REPORTING_MANAGER';
      level.specificEmployeeId = '';
    }
  }

  onLevelDepartmentsChange(level: TrvRsLevelDraft): void {
    this.normalizeLevelRouting(level);
    const allowedNames = new Set(
      this.rolesForDepartments(level.departmentIds).map((r) => this.normRoleName(r.name))
    );
    const kept = (level.jobRoleIds ?? []).filter((id) => {
      const jr = this.findJobRoleById(id);
      return jr && allowedNames.has(this.normRoleName(jr.name));
    });
    level.jobRoleIds = this.collapseRepresentativeJobRoleIds(
      kept.map((x) => this.normId(x)).filter(Boolean),
      level.departmentIds ?? []
    );
    this.loadEmployeesForLevel(level);
    this.syncSpecificEmployee(level);
  }

  onLevelRolesChange(level: TrvRsLevelDraft): void {
    this.syncSpecificEmployee(level);
  }

  onRoutingChange(level: TrvRsLevelDraft): void {
    this.normalizeLevelRouting(level);
    if (level.routingMode !== 'POOL_ANY_IN_SCOPE' && level.routingMode !== 'SPECIFIC_IN_SCOPE') {
      level.specificEmployeeId = '';
    }
    if (level.routingMode === 'SPECIFIC_IN_SCOPE') {
      level.jobRoleIds = [];
    }
    this.syncSpecificEmployee(level);
  }

  private loadEmployeesForLevel(level: TrvRsLevelDraft): void {
    if (!level.departmentIds.length) {
      level._employeesInDepts = [];
      level.employeesLoading = false;
      this.syncSpecificEmployee(level);
      return;
    }
    level.employeesLoading = true;
    const empObj = new Employee();
    empObj.departmentList = level.departmentIds.map((id) => {
      const d = new Department();
      d.deptId = id as any;
      return d;
    });
    this.employeeService
      .getAllEmployeesByDepartmentIds(empObj)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          level.employeesLoading = false;
          if (response?.serviceStatus === 'Success' && Array.isArray(response.serviceResponse)) {
            level._employeesInDepts = response.serviceResponse.map((row: any) => ({
              empId: this.normId(row.empId),
              name: (row.name ?? row.empName ?? row.employeeName ?? 'Employee') as string,
              departmentId: this.normId(row.departmentId),
              jobRoleId: this.normId(row.jobRoleId)
            }));
          } else {
            level._employeesInDepts = [];
          }
          this.syncSpecificEmployee(level);
        },
        error: () => {
          level.employeesLoading = false;
          level._employeesInDepts = [];
          this.syncSpecificEmployee(level);
        }
      });
  }

  /** Pool: selected departments + selected job roles. */
  employeesForPool(level: TrvRsLevelDraft): TrvRsEmpRow[] {
    const rows = level._employeesInDepts ?? [];
    if (!level.departmentIds.length || !level.jobRoleIds.length) {
      return [];
    }
    const ds = new Set(level.departmentIds.map((x) => this.normId(x)));
    const rs = new Set(this.expandJobRoleIds(level.jobRoleIds));
    return rows.filter((e) => ds.has(e.departmentId) && rs.has(e.jobRoleId)).sort((a, b) => a.name.localeCompare(b.name));
  }

  /** Specific person: employees in selected departments only (any role). */
  employeesForSpecificDeptOnly(level: TrvRsLevelDraft): TrvRsEmpRow[] {
    const rows = level._employeesInDepts ?? [];
    if (!level.departmentIds.length) {
      return [];
    }
    const ds = new Set(level.departmentIds.map((x) => this.normId(x)));
    return rows.filter((e) => ds.has(e.departmentId)).sort((a, b) => a.name.localeCompare(b.name));
  }

  /** Assignee dropdown options for current routing + scope. */
  assigneeOptionsForLevel(level: TrvRsLevelDraft): TrvRsEmpRow[] {
    if (level.routingMode === 'POOL_ANY_IN_SCOPE') {
      return this.employeesForPool(level);
    }
    if (level.routingMode === 'SPECIFIC_IN_SCOPE') {
      return this.employeesForSpecificDeptOnly(level);
    }
    return [];
  }

  showAssigneeDropdown(level: TrvRsLevelDraft): boolean {
    return level.routingMode === 'POOL_ANY_IN_SCOPE' || level.routingMode === 'SPECIFIC_IN_SCOPE';
  }

  assigneeRequired(level: TrvRsLevelDraft): boolean {
    return level.routingMode === 'SPECIFIC_IN_SCOPE';
  }

  assigneeSelectDisabled(level: TrvRsLevelDraft): boolean {
    if (level.employeesLoading) {
      return true;
    }
    if (level.routingMode === 'POOL_ANY_IN_SCOPE') {
      return !level.departmentIds.length || !this.assigneeOptionsForLevel(level).length;
    }
    if (level.routingMode === 'SPECIFIC_IN_SCOPE') {
      return !level.departmentIds.length || !this.assigneeOptionsForLevel(level).length;
    }
    return true;
  }

  routingHint(mode: string): string {
    return this.routingOptions.find((o) => o.value === mode)?.hint ?? '';
  }

  /** Route dropdown label: level 1 is post-submit, later levels are post-approval. */
  routeToFieldLabel(level: TrvRsLevelDraft): string {
    if (level.order === 1) {
      return 'After the employee submits the ticket, route to';
    }
    return 'After the previous level approves, route to';
  }

  /** Reporting manager or HOD — approver from hierarchy, not from scope grids. */
  isHierarchyRouting(level: TrvRsLevelDraft): boolean {
    return level.routingMode === 'REPORTING_MANAGER' || level.routingMode === 'HOD_SUBMITTER_DEPT';
  }

  scopeSectionTitle(level: TrvRsLevelDraft): string {
    switch (level.routingMode) {
      case 'POOL_ANY_IN_SCOPE':
        return 'Approver pool — pick departments, then job roles';
      case 'SPECIFIC_IN_SCOPE':
        return 'Pick one or more departments, then choose the named approver';
      default:
        return 'Departments & job roles';
    }
  }

  scopeSectionLead(level: TrvRsLevelDraft): string {
    switch (level.routingMode) {
      case 'POOL_ANY_IN_SCOPE':
        return 'Search and pick departments first, then job roles. The assignee list uses both.';
      case 'SPECIFIC_IN_SCOPE':
        return 'Search and pick one or more departments, wait for the list to load, then choose exactly one person in Assign to below.';
      default:
        return '';
    }
  }

  showLevelJobRoleMultiSelect(level: TrvRsLevelDraft): boolean {
    return level.routingMode === 'POOL_ANY_IN_SCOPE';
  }

  private syncSpecificEmployee(level: TrvRsLevelDraft): void {
    const pool = this.assigneeOptionsForLevel(level);
    const ids = new Set(pool.map((e) => e.empId));
    const cur = this.normId(level.specificEmployeeId);
    if (cur && !ids.has(cur)) {
      level.specificEmployeeId = '';
    }
  }

  onAdminDepartmentChange(rs: TrvRsRuleSetDraft): void {
    rs.admin.assigneeEmployeeId = '';
    this.prefetchAdminEmployees(rs.admin.departmentId);
  }

  private prefetchAdminEmployees(deptId: string): void {
    const id = this.normId(deptId);
    if (!id) {
      return;
    }
    if (this.adminEmployeesByDept.has(id)) {
      return;
    }
    this.adminEmployeesLoading = true;
    const empObj = new Employee();
    const d = new Department();
    d.deptId = id as any;
    empObj.departmentList = [d];
    this.employeeService
      .getAllEmployeesByDepartmentIds(empObj)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          this.adminEmployeesLoading = false;
          if (response?.serviceStatus === 'Success' && Array.isArray(response.serviceResponse)) {
            const list: TrvRsEmpRow[] = response.serviceResponse.map((row: any) => ({
              empId: this.normId(row.empId),
              name: (row.name ?? row.empName ?? row.employeeName ?? 'Employee') as string,
              departmentId: this.normId(row.departmentId),
              jobRoleId: this.normId(row.jobRoleId)
            }));
            this.adminEmployeesByDept.set(id, list.sort((a, b) => a.name.localeCompare(b.name)));
          } else {
            this.adminEmployeesByDept.set(id, []);
          }
        },
        error: () => {
          this.adminEmployeesLoading = false;
          this.adminEmployeesByDept.set(id, []);
        }
      });
  }

  adminEmployees(deptId: string): TrvRsEmpRow[] {
    return this.adminEmployeesByDept.get(this.normId(deptId)) ?? [];
  }

  private buildMatrixPayload(rs: TrvRsRuleSetDraft): any {
    return {
      matrixId: rs.matrixId ?? null,
      name: rs.name?.trim(),
      applicabilityDepartmentIds: (rs.applicabilityDepartmentIds ?? [])
        .map((id) => Number(id))
        .filter((n) => !Number.isNaN(n)),
      applicabilityJobRoleIds: this.expandJobRoleIds(rs.applicabilityJobRoleIds ?? [])
        .map((id) => Number(id))
        .filter((n) => !Number.isNaN(n)),
      levels: rs.levels.map((l) => ({
        order: l.order,
        departmentIds: (l.departmentIds ?? []).map((id) => Number(id)).filter((n) => !Number.isNaN(n)),
        jobRoleIds: this.expandJobRoleIds(l.jobRoleIds ?? [])
          .map((id) => Number(id))
          .filter((n) => !Number.isNaN(n)),
        routing: l.routingMode,
        assigneeEmployeeId: l.specificEmployeeId ? Number(l.specificEmployeeId) : null
      })),
      admin: {
        departmentId: rs.admin.departmentId ? Number(rs.admin.departmentId) : null,
        assigneeEmployeeId: rs.admin.assigneeEmployeeId ? Number(rs.admin.assigneeEmployeeId) : null
      }
    };
  }

  private validateRuleSet(rs: TrvRsRuleSetDraft): string | null {
    if (!rs.name?.trim()) {
      return 'Approval matrix name is required.';
    }
    if (!rs.levels?.length) {
      return `"${rs.name}" must have at least one approval level.`;
    }
    return null;
  }

  private applySavedMatrixToDraft(rs: TrvRsRuleSetDraft, row: any): void {
    const mapped = this.mapApiMatrixToDraft(row);
    rs.matrixId = mapped.matrixId;
    rs.name = mapped.name;
    rs.applicabilityDepartmentIds = mapped.applicabilityDepartmentIds;
    rs.applicabilityJobRoleIds = mapped.applicabilityJobRoleIds;
    rs.levels = mapped.levels;
    rs.admin = mapped.admin;
    this.enrichDraftForTable(rs);
    this.ensureAdminDept(rs);
    this.ensureApplicabilityShape(rs);
    rs.levels.forEach((l) => {
      this.normalizeLevelRouting(l);
      this.loadEmployeesForLevel(l);
    });
  }

  private persistRuleSet(rs: TrvRsRuleSetDraft, isUpdate: boolean): void {
    if (!this.currentUser?.empId) {
      void Swal.fire({ icon: 'warning', title: 'Not signed in', text: 'Cannot save without a logged-in user.' });
      return;
    }
    const validationError = this.validateRuleSet(rs);
    if (validationError) {
      void Swal.fire({ icon: 'warning', title: 'Cannot save', text: validationError });
      return;
    }
    if (isUpdate && this.isNewMatrix(rs)) {
      void Swal.fire({ icon: 'warning', title: 'Not saved yet', text: 'Use Save for a new approval matrix first.' });
      return;
    }
    rs.saving = true;
    const payload = {
      createdBy: Number(this.currentUser.empId),
      matrices: [this.buildMatrixPayload(rs)]
    };
    this.travelDeskService
      .saveTravelApprovalMatrix(payload)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          rs.saving = false;
          if (res?.serviceStatus === 'Success' && res.serviceResponse) {
            this.applySavedMatrixToDraft(rs, res.serviceResponse);
            const saved = this.mapApiMatrixToDraft(res.serviceResponse);
            const idx = this.ruleSets.findIndex((r) => r.matrixId === saved.matrixId);
            if (idx >= 0) {
              this.ruleSets = [...this.ruleSets.slice(0, idx), saved, ...this.ruleSets.slice(idx + 1)];
            } else {
              this.ruleSets = [...this.ruleSets, saved];
            }
            this.backToList();
            void Swal.fire({
              icon: 'success',
              title: isUpdate ? 'Updated' : 'Saved',
              text: isUpdate
                ? 'Approval matrix was updated successfully.'
                : 'Approval matrix was saved successfully.',
              confirmButtonText: 'OK'
            });
          } else {
            void Swal.fire({
              icon: 'error',
              title: isUpdate ? 'Update failed' : 'Save failed',
              text: res?.serviceError || res?.serviceResponse || 'Could not save approval matrix.'
            });
          }
        },
        error: () => {
          rs.saving = false;
          void Swal.fire({
            icon: 'error',
            title: isUpdate ? 'Update failed' : 'Save failed',
            text: 'Could not reach the server. Ensure database tables exist (see UAT SQL 022).'
          });
        }
      });
  }
}
