import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as moment from 'moment';
import { first } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { RMGDashboardProjectRequest } from 'src/app/models/rmgDashboardProjectRequest';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { FilterStateService } from 'src/app/services/filter-state.service';
import { LoaderService } from 'src/app/services/loader.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { moveItemInArray } from '@angular/cdk/drag-drop';
import { Feature } from 'src/app/models/feature';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';

export interface TableColumn {
  key: string;
  label: string;
  width?: number;
  sortable?: boolean;
  sticky?: boolean;
  visible?: boolean;
}

@Component({
  standalone: false,
  selector: 'app-rmg-project-table',
  templateUrl: './rmg-project-table.component.html',
  styleUrl: './rmg-project-table.component.css'
})

export class RmgProjectTableComponent implements OnInit, OnChanges {

  feature = "Resource Management";
  todaysDate: any
  currentUser: User;
  userMapping: any = {};
  currentBreadcrumbList: any[] = [];
  allBillableProjectTypes = ['tnm', 'fixed cost', 'monitoring'];
  allNonBillableProjectTypes = ['internalrndproducts', 'bench', 'internal'];

  columns = [
    { key: 'actions', label: 'Actions', minWidth: 120, sortable: false, searchable: false, visible: true, width: 120, searchValue: '' }
    , { key: 'name', label: 'Project Name', minWidth: 250, sortable: true, searchable: true, visible: true, width: 250, searchValue: '' }
    , { key: 'poNo', label: 'PO No.', minWidth: 200, sortable: true, searchable: true, visible: true, width: 200, searchValue: '' }
    , { key: 'poProjectType', label: 'Project Type', minWidth: 200, sortable: true, searchable: true, visible: true, width: 200, searchValue: '' }
    , { key: 'projectManagerName', label: 'Project Manager', minWidth: 220, sortable: true, searchable: true, visible: true, width: 220, searchValue: '' }
    , { key: 'clientName', label: 'Client', minWidth: 200, sortable: true, searchable: true, visible: true, width: 200, searchValue: '' }
    , { key: 'apmosysRM', label: 'ApMoSys RM', minWidth: 200, sortable: true, searchable: true, visible: true, width: 200, searchValue: '' }
    , { key: 'clientRM', label: 'Client RM', minWidth: 200, sortable: true, searchable: true, visible: true, width: 200, searchValue: '' }
    , { key: 'projectStartDate', label: 'Start Date', minWidth: 180, sortable: true, searchable: true, visible: true, width: 180, searchValue: '' }
    , { key: 'projectEndDate', label: 'End Date', minWidth: 180, sortable: true, searchable: true, visible: true, width: 180, searchValue: '' }
    , { key: 'state', label: 'State', minWidth: 180, sortable: true, searchable: true, visible: true, width: 180, searchValue: '' }
    , { key: 'createdOn', label: 'Created On', minWidth: 180, sortable: true, searchable: true, visible: true, width: 180, searchValue: '' }
    , { key: 'projectStatus', label: 'Ishine Project Status', minWidth: 220, sortable: true, searchable: true, visible: true, width: 220, searchValue: '' }
    , { key: 'draftStatus', label: 'Approval Status', minWidth: 220, sortable: true, searchable: true, visible: true, width: 220, searchValue: '' }
    , { key: 'activeResources', label: 'Active Resources', minWidth: 120, sortable: true, searchable: false, visible: true, width: 120, searchValue: '' }
    , { key: 'requiredResources', label: 'Required Resources', minWidth: 120, sortable: true, searchable: false, visible: true, width: 120, searchValue: '' }
  ];

  totalAndActiveAndTnmVisibleColumnsConfig: any[] = ['actions', 'name', 'requiredResources', 'activeResources', 'poProjectType', 'poNo', 'projectStartDate', 'projectEndDate', 'projectStatus', 'draftStatus', 'projectManagerName', 'clientName', 'clientRM', 'apmosysRM', 'state', 'createdOn'];
  fixedCostAndMonitoringVisibleColumnsConfig: any[] = ['actions', 'name', 'activeResources', 'poProjectType', 'poNo', 'projectStartDate', 'projectEndDate', 'projectStatus', 'draftStatus', 'projectManagerName', 'clientName', 'clientRM', 'apmosysRM', 'state', 'createdOn'];
  internalAndBenchVisibleColumnsConfig: any[] = ['actions', 'name', 'activeResources', 'poProjectType', 'projectStartDate', 'projectStatus', 'draftStatus', 'projectManagerName', 'clientName', 'clientRM', 'state', 'createdOn'];
  projectLifecycleExceptDeboardedAndNotStartedVisibleColumnsConfig: any[] = ['actions', 'name', 'requiredResources', 'activeResources', 'poProjectType', 'poNo', 'projectStartDate', 'projectEndDate', 'projectStatus', 'draftStatus', 'projectManagerName', 'clientName', 'clientRM', 'apmosysRM', 'state', 'createdOn'];
  projectLifecycleDeboardedAndNotStartedVisibleColumnsConfig: any[] = ['actions', 'name', 'requiredResources', 'poProjectType', 'poNo', 'projectStartDate', 'projectEndDate', 'projectStatus', 'draftStatus', 'projectManagerName', 'clientName', 'clientRM', 'apmosysRM', 'state', 'createdOn'];

  displayedColumns = this.columns.filter(c => c.visible !== false).map(c => c.key);
  filterColumns = this.columns.map(c => 'filter_' + c.key);

  @Input() selectedProjectStatusLabel: any = 'Project';
  @Input() projectStatus: any;
  @Input() selectedDepartmentIds: any;
  @Input() expiredTNMProjectFilter: any;
  @Input() fixedCostProjectFilter: any;
  @Input() timesheetApplicableProjectTypeFilter: any;

  @Output() actionTriggered = new EventEmitter<{ action: string; project: any; }>();

  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
  alertMessageModalRef: NgbModalRef;

  alertMessage: string = '';
  isManualReload:boolean = false;

  projectDetailsList: any[] = [];
  projectDetailsResolveMap: Record<string, { resolvedProjectViewId: string; redirected: boolean; resolvedProjectName?: string }> = {};
  /** Link-aware project name search (same contract as HR project view: serviceResponse1 / serviceResponse2). */
  rmgLinkedProjectSearchInfo: string | null = null;
  rmgLinkedPrimaryProjectIdsFromSearch: number[] = [];
  rmgLinkedSearchRowTooltipByProjectId: Record<number, string> = {};
  projectSummaryData: any[] = [];

  // Project Details Table
  searchOnEnter: boolean = true;
  isProjectSearchEnabled: boolean = false;
  /** Project Name column header (app-info-tooltip): linked / partial name search + PO filter hint. */
  projectNameSearchInfoTooltip: string[] = ['Search includes projects whose names were later linked to another project.', 'Linked results are shown as the current primary project with a link icon.',];
  totalProjectsCount: number = 0;
  projectPage: number = 1;
  projectPageSize: number = 10;
  pageSizeOptions: any[] = [5, 10, 20, 50];
  projectSortDirection: string = 'asc';
  projectSortColumn: string;
  projectSortColumnType: string;
  projectFilters: any = {};
  projectColumns: any[] = ["blank", "name", "poNo", "poProjectType", "projectManagerName", "clientName", "apmosysRM", "clientRM", "projectStartDate", "projectEndDate", "state", "createdOn", "projectStatus", "draftStatus"];
  poSearchTooltip: string[] = ['Shows only active POs.', 'Search includes both active and expired PO numbers.'];
  dateSearchTooltip: string[] = ['Search supports dd-mm-yyyy or yyyy-mm-dd date formats only.',];

  constructor(
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private filterStateService: FilterStateService,
    private breadcrumbService: BreadcrumbService,
    private modalService: NgbModal,
    private resourceManagementService: ResourceManagementService,
    private utilityService: UtilityService,
    public validationService: ValidationService,
  ) {
    this.todaysDate = new Date();
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
  }

  async ngOnInit() {
    this.mapSubFeatureFlag();
    this.projectPage = 1;
    this.projectPageSize = this.filterStateService?.projectPageSize ? this.filterStateService.projectPageSize : 10;
    if (this.filterStateService.projectReportFilters) {
      this.isProjectSearchEnabled = true;
      this.projectFilters = this.filterStateService.projectReportFilters;
    }
    await this.loadProjectData();
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (this.isManualReload) {
      return;
    }

    const relevantInputChanged =
      changes['projectStatus'] ||
      changes['selectedDepartmentIds'] ||
      changes['expiredTNMProjectFilter'] ||
      changes['fixedCostProjectFilter'] ||
      changes['timesheetApplicableProjectTypeFilter'];

    if (relevantInputChanged && !changes['projectStatus']?.firstChange) {
      await this.loadProjectData();
    }
  }

  async refreshProjectData() {
    this.isManualReload = true;
    try {
      await this.loadProjectData();
    } finally {
      this.isManualReload = false;
    }
  }

  async loadProjectData() {
    this.projectStatus = this.filterStateService?.selectedProjectStatus ? this.filterStateService.selectedProjectStatus : (this.projectStatus || 'TOTAL');
    this.expiredTNMProjectFilter = this.expiredTNMProjectFilter || 'allExpiredTNMProjectsCount';
    this.fixedCostProjectFilter = this.fixedCostProjectFilter || 'all';
    this.timesheetApplicableProjectTypeFilter = this.timesheetApplicableProjectTypeFilter || 'all';

    await this.updateColumnConfiguration(this.projectStatus);

    if (this.currentBreadcrumbList && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes('Project')) {
      this.projectStatus = 'ALL';
      this.isProjectSearchEnabled = true;
    } else {
      this.getProjectDetailsList(false);
    }
  }

  get currentUserType(): 'HOD' | 'ADMIN' | 'USER' {
    const deptName = String(this.currentUser?.departmentName || '').trim();
    const empRole = String(this.currentUser?.employeeRole || '').trim();
    const adminKeywords = ['Admin', 'Resource Management Group', 'Director', 'Super Admin', 'Accounts', 'HR'];

    const isAdmin = adminKeywords.some(keyword => deptName.includes(keyword) || empRole.includes(keyword));
    if (isAdmin) {
      return 'ADMIN';
    }
    if (empRole === 'HOD' || empRole === 'SuperAdmin' || empRole === 'Super Admin') {
      return 'HOD';
    }
    return 'USER';
  }

  get isAccounts(): boolean {
    const deptName = String(this.currentUser?.departmentName || '').trim();
    return deptName === 'Accounts';
  }

  private mapSubFeatureFlag() {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  async updateColumnConfiguration(projectStatus: string): Promise<void> {
    return new Promise(async (resolve) => {
      try {
        this.columns.forEach(col => {
          col.visible = false;
        });

        let visibleColumnsConfig: string[] = [];

        switch (projectStatus) {
          case 'ALL':
          case 'TOTAL':
          case 'TOTAL_TNM':
            visibleColumnsConfig = this.totalAndActiveAndTnmVisibleColumnsConfig;
            break;

          case 'TOTAL_FC':
          case 'TOTAL_MONITORING':
            visibleColumnsConfig = this.fixedCostAndMonitoringVisibleColumnsConfig;
            break;

          case 'TOTAL_INTERNAL':
          case 'BENCH':
            visibleColumnsConfig = this.internalAndBenchVisibleColumnsConfig;
            break;

          case 'OFFBOARDED':
          case 'NOT_STARTED':
            visibleColumnsConfig = this.projectLifecycleDeboardedAndNotStartedVisibleColumnsConfig;
            break;

          default:
            visibleColumnsConfig = this.totalAndActiveAndTnmVisibleColumnsConfig;
            break;
        }

        const orderedColumns = visibleColumnsConfig
          .map(key => {
            const column = this.columns.find(col => col.key === key);
            if (column) {
              column.visible = true;
            }
            return column;
          }).filter(Boolean);

        /* Add remaining hidden columns at end so master list remains intact */
        const hiddenColumns = this.columns.filter(
          col => !visibleColumnsConfig.includes(col.key)
        );

        this.columns = [
          ...orderedColumns,
          ...hiddenColumns
        ];


        this.displayedColumns = this.columns.filter(c => c.visible !== false).map(c => c.key);
        this.filterColumns = this.columns.map(c => 'filter_' + c.key);
        await Promise.resolve();
        resolve();
      } catch (error) {
        console.error('Error while updating column configuration:', error);
        resolve();
      }
    });
  }

  openAlertMessageModal(modalMessage: any) {
    this.alertMessage = modalMessage;
    if (this.alertMessageModalRef) {
      this.closeAlertMessageModal();
    }
    this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();

    }
  }

  getRMGRequestObject() {
    let rmgProjectRequest: RMGDashboardProjectRequest = new RMGDashboardProjectRequest();
    rmgProjectRequest.currentUserEmpId = this.currentUser.empId;
    rmgProjectRequest.currentUserType = this.currentUserType;

    // FILTERS
    rmgProjectRequest.projectStatus = this.projectStatus;
    rmgProjectRequest.departmentIds = this.selectedDepartmentIds;
    rmgProjectRequest.projectFilter = this.projectFilters;
    rmgProjectRequest.expiredProjectFilter = this.expiredTNMProjectFilter;
    rmgProjectRequest.fixedCostFilter = this.fixedCostProjectFilter;
    rmgProjectRequest.timesheetApplicableProjectTypeFilter = this.timesheetApplicableProjectTypeFilter;

    // PAGINATION
    rmgProjectRequest.page = this.projectPage || 0;
    rmgProjectRequest.pageSize = this.projectPageSize;
    rmgProjectRequest.sortColumn = this.projectSortColumn || 'name';
    rmgProjectRequest.sortDirection = this.projectSortDirection || 'asc';
    rmgProjectRequest.sortColumnType = this.projectSortColumnType || 'string';

    return rmgProjectRequest;
  }

  normalizeDate(dateInput: any) {
    if (!dateInput) {
      return null;
    }
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return null;
    }
    return moment(dateInput).startOf('day').format('YYYY-MM-DD');
  }

  onAction(action: string, project: any) {
    console.log(project);
    this.actionTriggered.emit({ action, project });
  }


  dropColumn(event: any) {
    moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
    this.displayedColumns = this.columns.map(c => c.key);
  }

  // Projects Table APIs & Methods Start
  onProjectSearch(column: any) {
    this.projectPage = 1;
    this.projectFilters = this.projectFilters = {
      ...this.projectFilters, [column.key]: column.searchValue
    };
    this.filterStateService.projectReportFilters = this.projectFilters;
    this.getProjectDetailsList(false);
  }

  onProjectPageSizeChange() {
    this.filterStateService.projectPageSize = this.projectPageSize;
    this.getProjectDetailsList(false);
  }

  sortProjectData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.projectSortColumn = sortParams[0];
      this.projectSortColumnType = sortParams[1];
      this.projectSortDirection = sort.direction;
      this.getProjectDetailsList(false);
    }
  }

  toggleProjectSearch(scrollToBottom: any = false): void {
    this.isProjectSearchEnabled = !this.isProjectSearchEnabled;
    if (!this.isProjectSearchEnabled) {
      this.projectFilters = {};
      this.filterStateService.clearProjectReportFilters();
      this.getProjectDetailsList(scrollToBottom);
    }
  }

  get totalPages(): number {
    return Math.ceil(this.totalProjectsCount / this.projectPageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get startIndex(): number {
    if (!this.totalProjectsCount || !this.projectPageSize) return 0;
    return (this.projectPage - 1) * this.projectPageSize + 1;
  }

  get endIndex(): number {
    if (!this.totalProjectsCount || !this.projectPageSize) return 0;
    return Math.min(this.projectPage * this.projectPageSize, this.totalProjectsCount);
  }

  get visiblePages(): (number | string)[] {
    const total = this.totalPages;
    const current = this.projectPage;
    const pages: (number | string)[] = [];
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    pages.push(1);

    if (current > 4) {
      pages.push('...');
    }
    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 3) {
      pages.push('...');
    }
    pages.push(total);
    return pages;
  }

  goToPage(page: number | string) {
    if (page !== '...' && typeof page === 'number') {
      this.projectPage = page;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList(false);
    }
  }

  nextProjectPage() {
    if (this.projectPage < this.totalPages) {
      this.projectPage++;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList(false);
    }
  }

  prevProjectPage() {
    if (this.projectPage > 1) {
      this.projectPage--;
      this.filterStateService.projectPageSize = this.projectPageSize;
      this.getProjectDetailsList(false);
    }
  }

  exportPageProjectDetailsToExcel(projectDetailsList: any[]): void {
    const excelName = "Project Report.xlsx";
    const exportData = projectDetailsList?.map(x => ({
      'Project Name': x.name || 'NA',
      'Active Resources': x.activeResources || 'NA',
      'Required Resources': x.requiredResources || 'NA',
      'Project Type': x.poProjectType || 'NA',
      'PO Number': x.poNo || 'NA',
      'Project Manager': x.projectManagers && x.projectManagers.length > 0 ? x.projectManagers[0].projectManagerName : 'NA',
      'Client': x.clientName || 'NA',
      'ApMSys RM': x.apmosysRM || 'NA',
      'Client RM': x.clientRM || 'NA',
      'Start Date': this.normalizeDate(x.projectStartDate) || 'NA',
      'End Date': this.normalizeDate(x.projectEndDate) || 'NA',
      'State': x.state || 'NA',
      'Created On': this.normalizeDate(x.createdOn) || 'NA',
      'PO Project Status': x.status || 'NA',
      'IShine Project Status': x.projectStatus || 'NA',
      'Approval Status': x.draftStatus || 'NA',
    }));
    this.exportExcelService.exportTableDataToExcel(exportData, excelName);
  }

  exportAllFilteredPageProjectDetailsToExcel() {
    let rmgProjectRequest = this.getRMGRequestObject();
    rmgProjectRequest.page = 0;
    rmgProjectRequest.pageSize = 100000;
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList?.content)) {
        const apiResponse = response?.serviceResponse?.projectList?.content || [];
        this.exportPageProjectDetailsToExcel(apiResponse);
      } else {
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }
    },
      (error) => {
        this.openAlertMessageModal('Something went wrong!!');
      });
  }

  getProjectDetailsList(scrollToBottom: any) {
    this.totalProjectsCount = 0;
    this.projectDetailsList = [];
    this.rmgLinkedProjectSearchInfo = null;
    this.rmgLinkedPrimaryProjectIdsFromSearch = [];
    this.rmgLinkedSearchRowTooltipByProjectId = {};
    let rmgProjectRequest = this.getRMGRequestObject();
    rmgProjectRequest.page = rmgProjectRequest.page ? (rmgProjectRequest.page - 1) || 0 : 0
    this.resourceManagementService.fetchProjectDetailsList(rmgProjectRequest).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus == "Success" && response?.serviceResponse != null && this.validationService.validateNullUndefinedEmptyList(response?.serviceResponse?.projectList?.content)) {
        const apiResponse = response?.serviceResponse?.projectList;
        this.totalProjectsCount = apiResponse?.totalElements || 0;
        this.projectDetailsList = [...apiResponse?.content];
        this.rmgLinkedProjectSearchInfo = response?.serviceResponse1 || null;
        this.absorbRmgServiceResponse2LinkedMeta(response?.serviceResponse2);
        this.populateProjectDetailsResolveMap(this.projectDetailsList);
        console.log(this.projectDetailsList, "lalallala");
      } else {
        this.rmgLinkedPrimaryProjectIdsFromSearch = [];
        this.rmgLinkedSearchRowTooltipByProjectId = {};
        this.openAlertMessageModal(response?.serviceResponse || 'Something went wrong!!');
      }

      // if (scrollToBottom) {
      //   setTimeout(() => this.scrollToTable());
      // }
    },
      (error) => {
        this.rmgLinkedPrimaryProjectIdsFromSearch = [];
        this.rmgLinkedSearchRowTooltipByProjectId = {};
        this.openAlertMessageModal('Something went wrong!!');
      });
  }

  isRmgLinkedSearchPrimaryRow(project: any): boolean {
    if (!this.rmgLinkedPrimaryProjectIdsFromSearch?.length) {
      return false;
    }
    const pid = Number(project?.projectId);
    if (!Number.isFinite(pid)) {
      return false;
    }
    return this.rmgLinkedPrimaryProjectIdsFromSearch.some(v => Number(v) === pid);
  }

  getRmgLinkedSearchRowTooltip(project: any): string {
    const pid = Number(project?.projectId);
    if (Number.isFinite(pid) && this.rmgLinkedSearchRowTooltipByProjectId[pid]) {
      return this.rmgLinkedSearchRowTooltipByProjectId[pid];
    }
    return this.rmgLinkedProjectSearchInfo || '';
  }

  private absorbRmgServiceResponse2LinkedMeta(raw: any): void {
    this.rmgLinkedPrimaryProjectIdsFromSearch = [];
    this.rmgLinkedSearchRowTooltipByProjectId = {};
    if (raw == null) {
      return;
    }
    if (typeof raw === 'string') {
      const s = raw.trim();
      if (!s) {
        return;
      }
      try {
        this.absorbRmgServiceResponse2LinkedMeta(JSON.parse(s));
        return;
      } catch {
        this.rmgLinkedPrimaryProjectIdsFromSearch = s.split(/[,;\s]+/g)
          .map(t => Number(t.trim()))
          .filter(v => Number.isFinite(v));
        return;
      }
    }
    if (Array.isArray(raw)) {
      this.rmgLinkedPrimaryProjectIdsFromSearch = raw
        .map(v => Number(v))
        .filter(v => Number.isFinite(v));
      return;
    }
    if (typeof raw === 'object') {
      const idsRaw = (raw as any).linkedPrimaryIds ?? (raw as any).linked_primary_ids;
      if (idsRaw != null && Array.isArray(idsRaw)) {
        this.rmgLinkedPrimaryProjectIdsFromSearch = idsRaw
          .map((v: any) => Number(v))
          .filter((v: number) => Number.isFinite(v));
      }
      const mapRaw = (raw as any).matchedLinkedNamesByPrimaryId
        ?? (raw as any).matched_linked_names_by_primary_id;
      if (mapRaw != null && typeof mapRaw === 'object' && !Array.isArray(mapRaw)) {
        for (const k of Object.keys(mapRaw)) {
          const pid = Number(k);
          const arr = (mapRaw as any)[k];
          const names = Array.isArray(arr)
            ? arr.map((x: any) => String(x == null ? '' : x).trim()).filter((t: string) => t.length > 0)
            : [];
          if (Number.isFinite(pid) && names.length > 0) {
            this.rmgLinkedSearchRowTooltipByProjectId[pid] = this.buildRmgLinkedSearchRowTooltipText(names);
          }
        }
        if (this.rmgLinkedPrimaryProjectIdsFromSearch.length === 0) {
          this.rmgLinkedPrimaryProjectIdsFromSearch = Object.keys(mapRaw)
            .map(k => Number(k))
            .filter(v => Number.isFinite(v));
        }
      }
    }
  }

  private buildRmgLinkedSearchRowTooltipText(names: string[]): string {
    const list = this.formatRmgEnglishNameList(names);
    return `Included via linked project whose name matches your search. Project :  ${list}.`;
  }

  private formatRmgEnglishNameList(parts: string[]): string {
    const seen = new Set<string>();
    const p: string[] = [];
    for (const x of parts) {
      const t = String(x || '').trim();
      if (!t || seen.has(t)) {
        continue;
      }
      seen.add(t);
      p.push(t);
    }
    if (p.length === 0) {
      return '';
    }
    if (p.length === 1) {
      return p[0];
    }
    if (p.length === 2) {
      return `${p[0]} and ${p[1]}`;
    }
    return `${p.slice(0, -1).join(', ')}, and ${p[p.length - 1]}`;
  }

  private populateProjectDetailsResolveMap(rows: any[]) {
    // Use numeric projectId for linked detection/copy-name to avoid ambiguity when
    // multiple linked projects share the same poProjectId (same "poXXXX" projectViewId).
    const ids = (rows || [])
      .map(r => r?.projectId)
      .filter(v => v !== null && v !== undefined && String(v).trim() !== '')
      .map(v => String(v));

    const unique = Array.from(new Set(ids));
    if (unique.length === 0) {
      this.projectDetailsResolveMap = {};
      return;
    }

    this.resourceManagementService.resolveProjectViewIds(unique).pipe(first()).subscribe((resp: any) => {
      if (resp?.serviceStatus === 'Success' && resp?.serviceResponse) {
        this.projectDetailsResolveMap = resp.serviceResponse || {};
      }
    });
  }

  copyPrimaryProjectName(projectViewId: any) {
    const key = String(projectViewId || '');
    const name = this.projectDetailsResolveMap?.[key]?.resolvedProjectName;
    if (!name) {
      this.openAlertMessageModal('Primary project name not available.');
      return;
    }
    navigator.clipboard.writeText(name)
      .then(() => this.openAlertMessageModal('Primary project name copied.'))
      .catch(() => this.openAlertMessageModal('Unable to copy.'));
  }

}
