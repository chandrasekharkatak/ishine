import { HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, ElementRef, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort, SortDirection } from '@angular/material/sort';
import { NgbModal, NgbModalOptions, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subscription, firstValueFrom } from 'rxjs';
import { first, finalize } from 'rxjs/operators';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import OrgChart from '@balkangraph/orgchart.js';
import * as d3 from 'd3';
import { DepartmentService } from '../services/department.service';
import { SkillMatrixMasterListSort, SkillMatrixService } from './skill-matrix.service';

export type MasterConfigSegment =
  | 'categories'
  | 'skills'
  | 'subskills'
  | 'domains'
  | 'subdomains'
  | 'domainFeatures'
  | 'aspirationChips'
  | 'hierarchyView';

@Component({
  standalone: false,
  selector: 'app-skill-matrix-master-configuration',
  templateUrl: './skill-matrix-master-configuration.component.html',
  styleUrls: ['./skill-matrix-placeholders.css', './skill-matrix-master-configuration.component.css']
})
export class SkillMatrixMasterConfigurationComponent implements OnInit, OnDestroy {

  @ViewChild('domainOrgChartContainer', { static: false }) domainOrgChartContainer?: ElementRef<HTMLElement>;
  private domainOrgChart: any = null;

  @ViewChild('vizRadialSvg', { static: false }) vizRadialSvg?: ElementRef<SVGSVGElement>;
  @ViewChild('vizTreeSvg', { static: false }) vizTreeSvg?: ElementRef<SVGSVGElement>;
  @ViewChild('vizTooltip', { static: false }) vizTooltip?: ElementRef<HTMLElement>;

  readonly pageSize = 15;
  /** Short delay so filters feel responsive without firing the API on every keyup. */
  private static readonly FILTER_DEBOUNCE_MS = 120;

  segment: MasterConfigSegment = 'categories';
  loading = false;
  page = 1;

  totalCategories = 0;
  totalSkills = 0;
  totalSubskills = 0;
  totalDomains = 0;
  totalSubdomains = 0;
  totalDomainFeatures = 0;
  totalAspirationChips = 0;

  /** Hierarchy View (read-only). */
  hierarchySkillsLoading = false;
  hierarchyDomainsLoading = false;
  hierarchySkillsError: string | null = null;
  hierarchyDomainsError: string | null = null;
  hierarchyCategoryId: number | null = null;
  hierarchyDepartmentId: number | null = null;
  hierarchyViewMode: 'skills' | 'domains' = 'skills';
  domainHierarchyDisplay: 'table' | 'orgChart' = 'table';

  /** Prototype viz mode (shared for skill+domain). */
  hierarchyVizMode: 'radial' | 'tree' = 'radial';
  vizSelectedDeptId: number | null = null;
  vizSelectedDomainId: number | null = null;
  vizLegend: Array<{ key: string; label: string; color: string; bg: string }> = [];
  hierarchyDomainOptions: { domainId: number; domainName: string }[] = [];

  private vizSkillLoadedOnce = false;
  private vizSkillByDeptId = new Map<number, any>(); // root tree data by dept
  private vizDeptSummaries: Array<any> = [];
  private vizDomainSummaries: Array<any> = [];
  private vizTreeRoot: any = null; // d3.hierarchy
  private vizTreeZoom: any = null;
  private vizTreeKey: string | null = null;
  hierarchyDeptTree: {
    deptId: number;
    deptName: string;
    skills: { skillId: number; skillName: string; subskills: string[] }[];
  }[] = [];
  hierarchyDomainTree: {
    domainId: number;
    domainName: string;
    subdomains: { subdomainId: number; subdomainName: string; features: string[] }[];
    domainFeatures: string[];
  }[] = [];
  private hierarchyDomainsLoadedOnce = false;

  isSearchEnabled = false;
  filters: Record<string, string> = {};

  private filterDebounceTimer: ReturnType<typeof setTimeout> | null = null;
  /** Incremented on each list HTTP call; stale responses are ignored. */
  private latestListRequestId = 0;

  /** Mat-sort-header ids use `field|type` (same pattern as dept-config); API receives `field` only. */
  categoriesSortActive: string | null = null;
  categoriesSortDirection: SortDirection = '';
  skillsSortActive: string | null = null;
  skillsSortDirection: SortDirection = '';
  subskillsSortActive: string | null = null;
  subskillsSortDirection: SortDirection = '';
  domainsSortActive: string | null = null;
  domainsSortDirection: SortDirection = '';
  subdomainsSortActive: string | null = null;
  subdomainsSortDirection: SortDirection = '';
  domainFeaturesSortActive: string | null = null;
  domainFeaturesSortDirection: SortDirection = '';
  aspirationChipsSortActive: string | null = null;
  aspirationChipsSortDirection: SortDirection = '';

  readonly categoryFilterColumns = ['blank', 'categoryName'];
  readonly skillsFilterColumns = [
    'blank',
    'skillName',
    'categoryName',
    'skillType',
    'activeDisplay',
    'departmentName'
  ];
  readonly subskillsFilterColumns = ['blank', 'subskillName', 'skillName', 'activeDisplay'];
  readonly domainFilterColumns = ['blank', 'domainName'];
  readonly subdomainFilterColumns = ['blank', 'subdomainName', 'domainName', 'activeDisplay'];
  readonly domainFeatureFilterColumns = ['blank', 'featureName', 'domainName', 'subdomainName', 'activeDisplay'];
  readonly aspirationChipFilterColumns = [
    'blank',
    'chipLabel',
    'sortOrder',
    'departmentName',
    'activeDisplay'
  ];

  categories: { categoryId: number; categoryName: string }[] = [];
  skills: {
    skillId: number;
    skillName: string;
    categoryId: number;
    categoryName: string;
    skillType: string;
    isActive: boolean;
    activeDisplay: string;
    departmentId: number;
  }[] = [];
  subskills: {
    subskillId: number;
    skillId: number;
    skillName: string;
    subskillName: string;
    isActive: boolean;
    activeDisplay: string;
  }[] = [];
  domains: { domainId: number; domainName: string }[] = [];
  subdomains: {
    subdomainId: number;
    domainId: number;
    domainName: string;
    subdomainName: string;
    isActive: boolean;
    activeDisplay: string;
  }[] = [];
  domainFeatures: {
    featureId: number;
    domainId: number;
    domainName: string;
    subdomainId: number | null;
    subdomainName: string | null;
    featureName: string;
    isActive: boolean;
    activeDisplay: string;
  }[] = [];
  aspirationChips: {
    chipId: number;
    deptId: number;
    departmentName: string;
    chipLabel: string;
    sortOrder: number;
    isActive: boolean;
    activeDisplay: string;
  }[] = [];

  categoryForm = { categoryName: '' };
  categoryEditId: number | null = null;

  skillForm = {
    skillName: '',
    categoryId: null as number | null,
    skillType: null as string | null,
    isActive: true,
    departmentId: null as number | null
  };
  skillEditId: number | null = null;
  categoryOptions: { categoryId: number; categoryName: string }[] = [];
  /** Departments from `department` master (name in UI; `deptId` sent on save). */
  departmentOptions: { deptId: number; name: string }[] = [];
  readonly skillTypeSelectOptions = [
    { code: 'Optional', label: 'Optional' },
    { code: 'Required', label: 'Required' }
  ];
  skillOptionsForSubskill: { skillId: number; skillName: string }[] = [];

  subskillForm = {
    skillId: null as number | null,
    subskillName: '',
    isActive: true
  };
  subskillEditId: number | null = null;

  domainForm = { domainName: '' };
  domainEditId: number | null = null;

  subdomainForm = {
    domainId: null as number | null,
    subdomainName: '',
    isActive: true
  };
  subdomainEditId: number | null = null;
  domainOptionsForSubdomain: { domainId: number; domainName: string }[] = [];
  /** Sub domains of the currently selected domain in the Domain Feature modal. */
  subdomainOptionsForFeature: { subdomainId: number; subdomainName: string }[] = [];

  domainFeatureForm = {
    domainId: null as number | null,
    subdomainId: null as number | null,
    featureName: '',
    isActive: true
  };
  domainFeatureEditId: number | null = null;

  aspirationChipForm = {
    deptId: 0 as number | null,
    chipLabel: '',
    sortOrder: 0,
    isActive: true
  };
  aspirationChipEditId: number | null = null;
  /** Stable reference for `app-my-select` (do not bind a method that returns a new array each CD — freezes the page). */
  aspirationChipDeptOptions: { deptId: number; name: string }[] = [{ deptId: 0, name: 'Global (all departments)' }];

  modalRef: NgbModalRef | null = null;

  @ViewChild('categoryModalTpl') categoryModalTpl: TemplateRef<any>;
  @ViewChild('skillModalTpl') skillModalTpl: TemplateRef<any>;
  @ViewChild('subskillModalTpl') subskillModalTpl: TemplateRef<any>;
  @ViewChild('domainModalTpl') domainModalTpl: TemplateRef<any>;
  @ViewChild('subdomainModalTpl') subdomainModalTpl: TemplateRef<any>;
  @ViewChild('domainFeatureModalTpl') domainFeatureModalTpl: TemplateRef<any>;
  @ViewChild('aspirationChipModalTpl') aspirationChipModalTpl: TemplateRef<any>;
  @ViewChild('bulkUploadModalTpl') bulkUploadModalTpl: TemplateRef<any>;

  /** Bulk upload modal: file is chosen first; upload runs only after explicit Upload click. */
  bulkUploadMasterType: string | null = null;
  bulkUploadSelectedFile: File | null = null;
  /** pick → uploading (bytes to server) → processing (server handling) → done | error */
  bulkUploadPhase: 'pick' | 'uploading' | 'processing' | 'done' | 'error' = 'pick';
  /** Last byte counts from `HttpEventType.UploadProgress` (drives first segment of the bar). */
  bulkUploadBytesLoaded = 0;
  bulkUploadBytesTotal: number | null = null;
  /** 0–100 for progress bar + label: bytes map to ~0–42%, then eases toward ~92% until the response arrives. */
  bulkUploadUiPercent = 0;
  bulkUploadResult: { inserted: number; failed: number; totalRecords: number; rowErrors: string[] } | null = null;
  bulkUploadErrorMessage: string | null = null;
  private bulkUploadHttpSub: Subscription | null = null;
  private bulkUploadProgressTickId: ReturnType<typeof setInterval> | null = null;

  constructor(
    private skillMatrixService: SkillMatrixService,
    private departmentService: DepartmentService,
    private modalService: NgbModal
  ) { }

  /** Opens master-configuration modals with shared styling (`windowClass` applies outside component DOM). */
  private openMasterModal(content: TemplateRef<any>, size?: 'sm' | 'lg' | 'xl'): NgbModalRef {
    const options: NgbModalOptions = {
      centered: true,
      backdrop: 'static',
      windowClass: 'skill-matrix-master-modal'
    };
    if (size) {
      options.size = size;
    }
    return this.modalService.open(content, options);
  }

  ngOnInit(): void {
    this.selectSegment('categories');
  }

  ngOnDestroy(): void {
    this.clearFilterDebounce();
    this.clearBulkUploadProgressTick();
    this.bulkUploadHttpSub?.unsubscribe();
    this.bulkUploadHttpSub = null;
    this.domainOrgChart?.destroy?.();
    this.domainOrgChart = null;
    this.vizTreeZoom = null;
  }

  private clearFilterDebounce(): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
      this.filterDebounceTimer = null;
    }
  }

  selectSegment(seg: MasterConfigSegment): void {
    this.clearFilterDebounce();
    this.segment = seg;
    this.page = 1;
    this.filters = {};
    this.isSearchEnabled = false;
    if (seg === 'categories') {
      this.loadCategories();
    } else if (seg === 'skills') {
      let pending = 2;
      const step = (): void => {
        pending--;
        if (pending === 0) {
          this.loadSkills();
        }
      };
      this.ensureCategoryOptions(step);
      this.loadDepartmentOptions(step);
    } else if (seg === 'subskills') {
      this.ensureSkillOptionsForSubskill(() => this.loadSubskills());
    } else if (seg === 'domains') {
      this.loadDomains();
    } else if (seg === 'subdomains') {
      this.ensureDomainOptionsForSubdomain(() => this.loadSubdomains());
    } else if (seg === 'domainFeatures') {
      this.loadDomainFeatures();
    } else if (seg === 'aspirationChips') {
      this.loadDepartmentOptions(() => this.loadAspirationChips());
    } else {
      // hierarchy view: ensure lookups then load trees
      this.hierarchyViewMode = 'skills';
      this.domainHierarchyDisplay = 'table';
      this.hierarchyVizMode = 'radial';
      this.hierarchyCategoryId = null;
      this.hierarchyDepartmentId = null;
      this.domainOrgChart?.destroy?.();
      this.domainOrgChart = null;
      let pending = 2;
      const step = (): void => {
        pending--;
        if (pending === 0) {
          void this.loadHierarchyDomainsOnce();
          void this.loadVizSkillDataOnce();
          setTimeout(() => this.renderPrototypeViz(), 0);
        }
      };
      this.ensureCategoryOptions(step);
      this.loadDepartmentOptions(step);
    }
  }

  setHierarchyViewMode(mode: 'skills' | 'domains'): void {
    this.hierarchyViewMode = mode;
    if (mode !== 'domains') {
      this.domainHierarchyDisplay = 'table';
      this.domainOrgChart?.destroy?.();
      this.domainOrgChart = null;
    } else if (this.domainHierarchyDisplay === 'orgChart') {
      setTimeout(() => this.renderDomainOrgChart(), 0);
    }
    setTimeout(() => this.renderPrototypeViz(), 0);
  }

  setHierarchyVizMode(mode: 'radial' | 'tree'): void {
    this.hierarchyVizMode = mode;
    setTimeout(() => this.renderPrototypeViz(), 0);
  }

  onVizDeptChange(deptId: number | null): void {
    this.vizSelectedDeptId = deptId;
    this.vizTreeRoot = null;
    this.vizTreeKey = null;
    this.renderPrototypeTree();
  }

  onVizDomainChange(domainId: number | null): void {
    this.vizSelectedDomainId = domainId;
    this.vizTreeRoot = null;
    this.vizTreeKey = null;
    this.renderPrototypeTree();
  }

  vizExpandCollapseAll(expand: boolean): void {
    if (!this.vizTreeRoot) return;
    const walk = (n: any): void => {
      if (expand) {
        if (n.data?._children) {
          n.children = n.data._children;
          n.data._children = null;
        }
      } else {
        if (n.children && n.data?.type !== 'root') {
          n.data._children = n.children;
          n.children = null;
        }
      }
      (n.children ?? []).forEach(walk);
    };
    walk(this.vizTreeRoot);
    this.renderPrototypeTree();
  }

  vizExport(format: 'csv' | 'xlsx'): void {
    if (this.hierarchyViewMode === 'skills') {
      if (this.vizSelectedDeptId == null) {
        void this.exportAllSkillHierarchy(format);
        return;
      }
      const root = this.vizSkillByDeptId.get(Number(this.vizSelectedDeptId));
      const rows: any[] = [];
      const deptName = root?.name ?? '';
      (root?.children ?? []).forEach((cat: any) => {
        (cat.children ?? []).forEach((sk: any) => {
          const subs = sk.children ?? [];
          if (!subs.length) rows.push({ department: deptName, category: cat.name, skill: sk.name, subskill: '' });
          subs.forEach((ss: any) => rows.push({ department: deptName, category: cat.name, skill: sk.name, subskill: ss.name }));
        });
      });
      const filenameBase = this.safeExportFilename(`skill-hierarchy_${deptName || 'department'}`);
      this.exportRows(rows, filenameBase, format);
      return;
    }

    // domains
    if (this.vizSelectedDomainId == null) {
      this.exportDomainHierarchy(format);
      return;
    }
    const dom = this.hierarchyDomainTree.find((d) => Number(d.domainId) === Number(this.vizSelectedDomainId));
    const rows: any[] = [];
    (dom?.domainFeatures ?? []).forEach((f) => rows.push({ domain: dom.domainName, subdomain: '', feature: f, scope: 'Domain' }));
    (dom?.subdomains ?? []).forEach((su: any) => {
      if (!su.features?.length) rows.push({ domain: dom.domainName, subdomain: su.subdomainName, feature: '', scope: 'Subdomain' });
      (su.features ?? []).forEach((f: any) => rows.push({ domain: dom.domainName, subdomain: su.subdomainName, feature: f, scope: 'Subdomain' }));
    });
    const filenameBase = this.safeExportFilename(`domain-hierarchy_${dom?.domainName || 'domain'}`);
    this.exportRows(rows, filenameBase, format);
  }

  setDomainHierarchyDisplay(mode: 'table' | 'orgChart'): void {
    this.domainHierarchyDisplay = mode;
    if (mode === 'orgChart') {
      setTimeout(() => this.renderDomainOrgChart(), 0);
    } else {
      this.domainOrgChart?.destroy?.();
      this.domainOrgChart = null;
    }
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.clearFilterDebounce();
      this.filters = {};
      this.page = 1;
      this.reloadCurrentList(false);
    }
  }

  /**
   * Column filter bar fires on every keyup; debounce so we do not call the API / toggle loading each keystroke.
   */
  onSearch(searchData: Record<string, string>): void {
    this.clearFilterDebounce();
    this.filterDebounceTimer = setTimeout(() => {
      this.filterDebounceTimer = null;
      this.filters = searchData || {};
      this.page = 1;
      this.reloadCurrentList(true);
    }, SkillMatrixMasterConfigurationComponent.FILTER_DEBOUNCE_MS);
  }

  handlePageChange(event: number): void {
    this.page = event;
    this.reloadCurrentList(false);
  }

  sortDataCategories(sort: Sort): void {
    this.applyMatSortState(sort, 'cat');
    this.page = 1;
    this.loadCategories();
  }

  sortDataSkills(sort: Sort): void {
    this.applyMatSortState(sort, 'skills');
    this.page = 1;
    this.loadSkills();
  }

  sortDataSubskills(sort: Sort): void {
    this.applyMatSortState(sort, 'subskills');
    this.page = 1;
    this.loadSubskills();
  }

  sortDataDomains(sort: Sort): void {
    this.applyMatSortState(sort, 'domains');
    this.page = 1;
    this.loadDomains();
  }

  sortDataSubdomains(sort: Sort): void {
    this.applyMatSortState(sort, 'subdomains');
    this.page = 1;
    this.loadSubdomains();
  }

  sortDataDomainFeatures(sort: Sort): void {
    this.applyMatSortState(sort, 'domainFeatures');
    this.page = 1;
    this.loadDomainFeatures();
  }

  sortDataAspirationChips(sort: Sort): void {
    this.applyMatSortState(sort, 'aspirationChips');
    this.page = 1;
    this.loadAspirationChips();
  }

  private applyMatSortState(
    sort: Sort,
    table: 'cat' | 'skills' | 'subskills' | 'domains' | 'subdomains' | 'domainFeatures' | 'aspirationChips'
  ): void {
    if (sort.direction === '') {
      switch (table) {
        case 'cat':
          this.categoriesSortActive = null;
          this.categoriesSortDirection = '';
          break;
        case 'skills':
          this.skillsSortActive = null;
          this.skillsSortDirection = '';
          break;
        case 'subskills':
          this.subskillsSortActive = null;
          this.subskillsSortDirection = '';
          break;
        case 'domains':
          this.domainsSortActive = null;
          this.domainsSortDirection = '';
          break;
        case 'subdomains':
          this.subdomainsSortActive = null;
          this.subdomainsSortDirection = '';
          break;
        case 'domainFeatures':
          this.domainFeaturesSortActive = null;
          this.domainFeaturesSortDirection = '';
          break;
        case 'aspirationChips':
          this.aspirationChipsSortActive = null;
          this.aspirationChipsSortDirection = '';
          break;
      }
    } else if (sort.active) {
      switch (table) {
        case 'cat':
          this.categoriesSortActive = sort.active;
          this.categoriesSortDirection = sort.direction;
          break;
        case 'skills':
          this.skillsSortActive = sort.active;
          this.skillsSortDirection = sort.direction;
          break;
        case 'subskills':
          this.subskillsSortActive = sort.active;
          this.subskillsSortDirection = sort.direction;
          break;
        case 'domains':
          this.domainsSortActive = sort.active;
          this.domainsSortDirection = sort.direction;
          break;
        case 'subdomains':
          this.subdomainsSortActive = sort.active;
          this.subdomainsSortDirection = sort.direction;
          break;
        case 'domainFeatures':
          this.domainFeaturesSortActive = sort.active;
          this.domainFeaturesSortDirection = sort.direction;
          break;
        case 'aspirationChips':
          this.aspirationChipsSortActive = sort.active;
          this.aspirationChipsSortDirection = sort.direction;
          break;
      }
    }
  }

  private masterListSortFromMat(active: string | null, direction: SortDirection): SkillMatrixMasterListSort | null {
    const dir = direction as string;
    if (!active || !dir || dir === '') {
      return null;
    }
    const column = active.split('|')[0];
    if (!column) {
      return null;
    }
    return { column, direction: dir === 'desc' ? 'desc' : 'asc' };
  }

  private reloadCurrentList(silent: boolean): void {
    if (this.segment === 'categories') {
      this.loadCategories(silent);
    } else if (this.segment === 'skills') {
      this.loadSkills(silent);
    } else if (this.segment === 'subskills') {
      this.loadSubskills(silent);
    } else if (this.segment === 'domains') {
      this.loadDomains(silent);
    } else if (this.segment === 'subdomains') {
      this.loadSubdomains(silent);
    } else if (this.segment === 'domainFeatures') {
      this.loadDomainFeatures(silent);
    } else if (this.segment === 'aspirationChips') {
      this.loadAspirationChips(silent);
    } else {
      // hierarchy view is not a paginated list; ignore search reload.
    }
  }

  /** On API/network failure or non-success: keep the grid visible with an empty list (no blocking error banner). */
  private applyListLoadFailure(): void {
    if (this.segment === 'categories') {
      this.categories = [];
      this.totalCategories = 0;
    } else if (this.segment === 'skills') {
      this.skills = [];
      this.totalSkills = 0;
    } else if (this.segment === 'subskills') {
      this.subskills = [];
      this.totalSubskills = 0;
    } else if (this.segment === 'domains') {
      this.domains = [];
      this.totalDomains = 0;
    } else if (this.segment === 'subdomains') {
      this.subdomains = [];
      this.totalSubdomains = 0;
    } else if (this.segment === 'domainFeatures') {
      this.domainFeatures = [];
      this.totalDomainFeatures = 0;
    } else if (this.segment === 'aspirationChips') {
      this.aspirationChips = [];
      this.totalAspirationChips = 0;
    } else {
      // hierarchy view: use its own error state
    }
  }

  onHierarchyCategoryChange(categoryId: number | null): void {
    this.hierarchyCategoryId = categoryId;
    void this.loadHierarchySkillsForSelectedCategory();
  }

  onHierarchyDepartmentChange(departmentId: number | null): void {
    this.hierarchyDepartmentId = departmentId;
    void this.loadHierarchySkillsForSelectedCategory();
  }

  exportSkillHierarchy(format: 'csv' | 'xlsx'): void {
    if (this.hierarchyCategoryId == null) {
      this.swalError('Select category first');
      return;
    }
    const rows = this.buildSkillHierarchyExportRows();
    const categoryName =
      this.categoryOptions.find((c) => Number(c.categoryId) === Number(this.hierarchyCategoryId))?.categoryName ?? 'category';
    const deptName =
      this.hierarchyDepartmentId == null
        ? 'all-departments'
        : this.departmentOptions.find((d) => Number(d.deptId) === Number(this.hierarchyDepartmentId))?.name ?? 'department';
    const filenameBase = this.safeExportFilename(`skill-hierarchy_${categoryName}_${deptName}`);
    this.exportRows(rows, filenameBase, format);
  }

  async exportAllSkillHierarchy(format: 'csv' | 'xlsx'): Promise<void> {
    try {
      Swal.fire({
        title: 'Preparing export…',
        allowOutsideClick: false,
        allowEscapeKey: false,
        didOpen: () => Swal.showLoading(),
      });

      const pageSize = 100; // backend caps at 100
      const [skills, subskills] = await Promise.all([
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillsMaster(p, s, {}, null),
          (r) => ({
            skillId: Number(r.skillId),
            skillName: String(r.skillName ?? ''),
            categoryId: r.categoryId != null ? Number(r.categoryId) : null,
            departmentId: r.departmentId != null ? Number(r.departmentId) : null,
            isActive: r.isActive !== false
          })
        ),
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSubskillsMaster(p, s, {}, null),
          (r) => ({
            skillId: Number(r.skillId),
            subskillName: String(r.subskillName ?? ''),
            isActive: r.isActive !== false
          })
        ),
      ]);

      const categoryNameById = new Map<number, string>(
        (this.categoryOptions ?? []).map((c) => [Number(c.categoryId), String(c.categoryName ?? '')])
      );
      const deptNameById = new Map<number, string>(
        (this.departmentOptions ?? []).map((d) => [Number(d.deptId), String(d.name ?? '')])
      );
      const subsBySkillId = new Map<number, string[]>();
      subskills
        .filter((s: any) => s.isActive)
        .forEach((su: any) => {
          const id = su.skillId;
          const cur = subsBySkillId.get(id) ?? [];
          cur.push(su.subskillName);
          subsBySkillId.set(id, cur);
        });

      const rows: Array<{ category: string; department: string; skill: string; subskill: string }> = [];
      skills
        .filter((s: any) => s.isActive)
        .forEach((sk: any) => {
          const catId = sk.categoryId != null ? Number(sk.categoryId) : -1;
          const did = sk.departmentId != null ? Number(sk.departmentId) : -1;
          const category = catId === -1 ? '—' : (categoryNameById.get(catId) || String(catId));
          const department = did === -1 ? '—' : (deptNameById.get(did) || String(did));
          const subs = (subsBySkillId.get(sk.skillId) ?? []).slice().sort((a, b) => a.localeCompare(b));
          if (!subs.length) {
            rows.push({ category, department, skill: sk.skillName, subskill: '' });
            return;
          }
          subs.forEach((ss) => rows.push({ category, department, skill: sk.skillName, subskill: ss }));
        });

      rows.sort((a, b) =>
        a.category.localeCompare(b.category) ||
        a.department.localeCompare(b.department) ||
        a.skill.localeCompare(b.skill) ||
        a.subskill.localeCompare(b.subskill)
      );

      const filenameBase = this.safeExportFilename('skill-hierarchy_all-categories_all-departments');
      this.exportRows(rows, filenameBase, format);
    } catch {
      this.swalError('Could not export all skill hierarchy');
    } finally {
      Swal.close();
    }
  }

  exportDomainHierarchy(format: 'csv' | 'xlsx'): void {
    const rows = this.buildDomainHierarchyExportRows();
    const filenameBase = this.safeExportFilename('domain-hierarchy');
    this.exportRows(rows, filenameBase, format);
  }

  private buildSkillHierarchyExportRows(): Array<{
    category: string;
    department: string;
    skill: string;
    subskill: string;
  }> {
    const categoryName =
      this.categoryOptions.find((c) => Number(c.categoryId) === Number(this.hierarchyCategoryId))?.categoryName ?? '';
    const out: Array<{ category: string; department: string; skill: string; subskill: string }> = [];
    this.hierarchyDeptTree.forEach((d) => {
      d.skills.forEach((sk) => {
        if (!sk.subskills?.length) {
          out.push({ category: categoryName, department: d.deptName, skill: sk.skillName, subskill: '' });
          return;
        }
        sk.subskills.forEach((ss) => out.push({ category: categoryName, department: d.deptName, skill: sk.skillName, subskill: ss }));
      });
    });
    return out;
  }

  private buildDomainHierarchyExportRows(): Array<{
    domain: string;
    subdomain: string;
    feature: string;
    scope: 'Domain' | 'Subdomain';
  }> {
    const out: Array<{ domain: string; subdomain: string; feature: string; scope: 'Domain' | 'Subdomain' }> = [];
    this.hierarchyDomainTree.forEach((dom) => {
      (dom.domainFeatures ?? []).forEach((f) => out.push({ domain: dom.domainName, subdomain: '', feature: f, scope: 'Domain' }));
      (dom.subdomains ?? []).forEach((su) => {
        if (!su.features?.length) {
          out.push({ domain: dom.domainName, subdomain: su.subdomainName, feature: '', scope: 'Subdomain' });
          return;
        }
        su.features.forEach((f) => out.push({ domain: dom.domainName, subdomain: su.subdomainName, feature: f, scope: 'Subdomain' }));
      });
    });
    return out;
  }

  private exportRows(rows: any[], filenameBase: string, format: 'csv' | 'xlsx'): void {
    const ws = XLSX.utils.json_to_sheet(rows ?? []);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Hierarchy');

    if (format === 'csv') {
      const csv = XLSX.utils.sheet_to_csv(ws);
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
      saveAs(blob, `${filenameBase}.csv`);
      return;
    }

    const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buf], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, `${filenameBase}.xlsx`);
  }

  private safeExportFilename(base: string): string {
    const d = new Date();
    const yyyy = String(d.getFullYear());
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const clean = String(base ?? 'export')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/-+/g, '-')
      .replace(/(^-|-$)/g, '');
    return `${clean}_${yyyy}${mm}${dd}`;
  }

  private async fetchAllPages<T>(
    pageSize: number,
    fetchPage: (page: number, size: number) => Observable<any>,
    mapRow: (row: any) => T
  ): Promise<T[]> {
    const out: T[] = [];
    let page = 0;
    let total: number | null = null;
    while (true) {
      const res = await firstValueFrom(fetchPage(page, pageSize).pipe(first()));
      if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
        break;
      }
      const raw = res.serviceResponse as any[];
      raw.forEach((r) => out.push(mapRow(r)));
      total = total ?? (res.totalElements != null ? Number(res.totalElements) : null);
      if (total != null && out.length >= total) {
        break;
      }
      if (raw.length < pageSize) {
        break;
      }
      page += 1;
      // safety: avoid infinite loops if backend misreports totals
      if (page > 250) {
        break;
      }
    }
    return out;
  }

  private async loadHierarchySkillsForSelectedCategory(): Promise<void> {
    if (this.hierarchyCategoryId == null) {
      this.hierarchyDeptTree = [];
      return;
    }
    this.hierarchySkillsLoading = true;
    this.hierarchySkillsError = null;
    const categoryId = this.hierarchyCategoryId;
    const selectedDeptId = this.hierarchyDepartmentId;
    try {
      const pageSize = 100; // backend caps at 100
      const [skills, subskills] = await Promise.all([
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillsMaster(p, s, {}, null),
          (r) => ({
            skillId: Number(r.skillId),
            skillName: String(r.skillName ?? ''),
            categoryId: r.categoryId != null ? Number(r.categoryId) : null,
            departmentId: r.departmentId != null ? Number(r.departmentId) : null,
            isActive: r.isActive !== false
          })
        ),
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSubskillsMaster(p, s, {}, null),
          (r) => ({
            skillId: Number(r.skillId),
            subskillName: String(r.subskillName ?? ''),
            isActive: r.isActive !== false
          })
        ),
      ]);

      // left: category → department → skills → subskills
      const deptNameById = new Map<number, string>(
        (this.departmentOptions ?? []).map((d) => [Number(d.deptId), String(d.name ?? '')])
      );
      const subsBySkillId = new Map<number, string[]>();
      subskills
        .filter((s: any) => s.isActive)
        .forEach((su: any) => {
          const id = su.skillId;
          const cur = subsBySkillId.get(id) ?? [];
          cur.push(su.subskillName);
          subsBySkillId.set(id, cur);
        });
      const deptMap = new Map<number, { deptId: number; deptName: string; skills: any[] }>();
      skills
        .filter((s: any) => s.isActive && s.categoryId === categoryId)
        .forEach((sk: any) => {
          const did = sk.departmentId != null ? Number(sk.departmentId) : -1;
          if (selectedDeptId != null && did !== Number(selectedDeptId)) {
            return;
          }
          const dnm = did === -1 ? '—' : deptNameById.get(did) || String(did);
          if (!deptMap.has(did)) {
            deptMap.set(did, { deptId: did, deptName: dnm, skills: [] });
          }
          deptMap.get(did)!.skills.push({
            skillId: sk.skillId,
            skillName: sk.skillName,
            subskills: (subsBySkillId.get(sk.skillId) ?? []).slice().sort((a, b) => a.localeCompare(b))
          });
        });
      const deptTree = Array.from(deptMap.values()).map((d) => ({
        deptId: d.deptId,
        deptName: d.deptName,
        skills: d.skills.sort((a, b) => a.skillName.localeCompare(b.skillName))
      }));
      deptTree.sort((a, b) => a.deptName.localeCompare(b.deptName));
      this.hierarchyDeptTree = deptTree;
    } catch (e) {
      this.hierarchyDeptTree = [];
      this.hierarchySkillsError = 'Could not load skill/subskill hierarchy.';
    } finally {
      this.hierarchySkillsLoading = false;
    }
  }

  private async loadHierarchyDomainsOnce(): Promise<void> {
    if (this.hierarchyDomainsLoadedOnce) {
      return;
    }
    this.hierarchyDomainsLoading = true;
    this.hierarchyDomainsError = null;
    try {
      const pageSize = 100;
      const [domains, subdomains, features] = await Promise.all([
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillDomains(p, s, {}, null),
          (r) => ({ domainId: Number(r.domainId), domainName: String(r.domainName ?? '') })
        ),
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillSubdomains(p, s, {}, null),
          (r) => ({
            subdomainId: Number(r.subdomainId),
            domainId: Number(r.domainId),
            subdomainName: String(r.subdomainName ?? ''),
            isActive: r.isActive !== false
          })
        ),
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillDomainFeatures(p, s, {}, null),
          (r) => ({
            featureId: Number(r.featureId),
            domainId: Number(r.domainId),
            subdomainId: r.subdomainId != null ? Number(r.subdomainId) : null,
            featureName: String(r.featureName ?? ''),
            isActive: r.isActive !== false
          })
        )
      ]);

      const subsByDomainId = new Map<number, any[]>();
      subdomains
        .filter((s: any) => s.isActive)
        .forEach((su: any) => {
          const cur = subsByDomainId.get(su.domainId) ?? [];
          cur.push(su);
          subsByDomainId.set(su.domainId, cur);
        });
      const featsDomainOnly = new Map<number, string[]>();
      const featsBySubdomainId = new Map<number, string[]>();
      features
        .filter((f: any) => f.isActive)
        .forEach((f: any) => {
          if (f.subdomainId == null) {
            const cur = featsDomainOnly.get(f.domainId) ?? [];
            cur.push(f.featureName);
            featsDomainOnly.set(f.domainId, cur);
          } else {
            const sid = Number(f.subdomainId);
            const cur = featsBySubdomainId.get(sid) ?? [];
            cur.push(f.featureName);
            featsBySubdomainId.set(sid, cur);
          }
        });
      this.hierarchyDomainTree = (domains as any[])
        .slice()
        .sort((a, b) => a.domainName.localeCompare(b.domainName))
        .map((d: any) => {
          const subs = (subsByDomainId.get(d.domainId) ?? [])
            .slice()
            .sort((a, b) => a.subdomainName.localeCompare(b.subdomainName))
            .map((su: any) => ({
              subdomainId: su.subdomainId,
              subdomainName: su.subdomainName,
              features: (featsBySubdomainId.get(su.subdomainId) ?? []).slice().sort((a, b) => a.localeCompare(b))
            }));
          return {
            domainId: d.domainId,
            domainName: d.domainName,
            subdomains: subs,
            domainFeatures: (featsDomainOnly.get(d.domainId) ?? []).slice().sort((a, b) => a.localeCompare(b))
          };
        });
      this.hierarchyDomainsLoadedOnce = true;
      this.hierarchyDomainOptions = this.hierarchyDomainTree.map((d) => ({ domainId: d.domainId, domainName: d.domainName }));
      this.buildVizDomainSummaries();
      if (this.hierarchyViewMode === 'domains') {
        setTimeout(() => this.renderPrototypeViz(), 0);
      }
      if (this.hierarchyViewMode === 'domains' && this.domainHierarchyDisplay === 'orgChart') {
        setTimeout(() => this.renderDomainOrgChart(), 0);
      }
    } catch (e) {
      this.hierarchyDomainsError = 'Could not load domain hierarchy.';
      this.hierarchyDomainTree = [];
    } finally {
      this.hierarchyDomainsLoading = false;
      // render/refresh prototype viz after domains load completes
      if (this.segment === 'hierarchyView' && this.hierarchyViewMode === 'domains') {
        setTimeout(() => this.renderPrototypeViz(), 0);
      }
    }
  }

  private async loadVizSkillDataOnce(): Promise<void> {
    if (this.vizSkillLoadedOnce) return;
    try {
      const pageSize = 100;
      const [skills, subskills] = await Promise.all([
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSkillsMaster(p, s, {}, null),
          (r) => ({
            skillId: Number(r.skillId),
            skillName: String(r.skillName ?? ''),
            categoryId: r.categoryId != null ? Number(r.categoryId) : null,
            categoryName: String(r.categoryName ?? ''),
            departmentId: r.departmentId != null ? Number(r.departmentId) : null,
            isActive: r.isActive !== false
          })
        ),
        this.fetchAllPages(
          pageSize,
          (p, s) => this.skillMatrixService.getSubskillsMaster(p, s, {}, null),
          (r) => ({
            subskillName: String(r.subskillName ?? ''),
            skillId: Number(r.skillId),
            isActive: r.isActive !== false
          })
        )
      ]);

      const subsBySkillId = new Map<number, string[]>();
      (subskills as any[]).filter((s) => s.isActive).forEach((su) => {
        const cur = subsBySkillId.get(su.skillId) ?? [];
        cur.push(su.subskillName);
        subsBySkillId.set(su.skillId, cur);
      });

      const deptNameById = new Map<number, string>((this.departmentOptions ?? []).map((d) => [Number(d.deptId), String(d.name ?? '')]));

      // dept -> category -> skills
      const deptMap = new Map<number, Map<string, any[]>>();
      (skills as any[]).filter((s) => s.isActive).forEach((sk) => {
        const did = sk.departmentId != null ? Number(sk.departmentId) : -1;
        const cat = sk.categoryName || (sk.categoryId != null ? String(sk.categoryId) : '—');
        if (!deptMap.has(did)) deptMap.set(did, new Map<string, any[]>());
        const catMap = deptMap.get(did)!;
        if (!catMap.has(cat)) catMap.set(cat, []);
        catMap.get(cat)!.push({
          name: sk.skillName,
          type: 'skill',
          children: (subsBySkillId.get(sk.skillId) ?? []).slice().sort((a, b) => a.localeCompare(b)).map((s) => ({ name: s, type: 'sub', children: [] }))
        });
      });

      this.vizSkillByDeptId.clear();
      deptMap.forEach((catMap, did) => {
        const deptName = did === -1 ? '—' : deptNameById.get(did) || String(did);
        const root = {
          name: deptName,
          id: did,
          type: 'root',
          children: Array.from(catMap.entries())
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map(([catName, skillArr]) => ({
              name: catName,
              type: 'category',
              children: (skillArr ?? []).slice().sort((a, b) => a.name.localeCompare(b.name))
            }))
        };
        this.vizSkillByDeptId.set(did, root);
      });

      this.buildVizDeptSummaries();
      this.vizSkillLoadedOnce = true;
    } catch {
      // keep empty; UI will show empty radial
      this.vizSkillLoadedOnce = true;
    } finally {
      // async loads can finish after initial render; refresh the viz once data arrives
      if (this.segment === 'hierarchyView' && this.hierarchyViewMode === 'skills') {
        setTimeout(() => this.renderPrototypeViz(), 0);
      }
    }
  }

  private themePalette(): Array<{ key: string; label: string; color: string; bg: string }> {
    return [
      { key: 'primary', label: 'Primary', color: 'var(--dark-cornflower-blue, #193d8a)', bg: 'rgba(25, 61, 138, 0.10)' },
      { key: 'accent', label: 'Accent', color: 'var(--pacific-blue, #00a8b8)', bg: 'rgba(0, 168, 184, 0.12)' },
      { key: 'deep', label: 'Deep', color: 'var(--royal-blue-dark, #132b67)', bg: 'rgba(19, 43, 103, 0.10)' },
      { key: 'danger', label: 'Alert', color: 'var(--dark-red, #960200)', bg: 'rgba(150, 2, 0, 0.10)' },
      { key: 'neutral', label: 'Neutral', color: 'var(--middle-grey, #857c6f)', bg: 'rgba(133, 124, 111, 0.12)' },
      { key: 'alt', label: 'Alt', color: '#534AB7', bg: 'rgba(83, 74, 183, 0.12)' },
    ];
  }

  private buildVizDeptSummaries(): void {
    const palette = this.themePalette();
    this.vizLegend = palette.map((p) => ({ ...p }));

    const subsCount = (root: any): number => {
      let c = 0;
      (root?.children ?? []).forEach((cat: any) => {
        (cat.children ?? []).forEach((sk: any) => {
          c += (sk.children ?? []).length;
        });
      });
      return c;
    };

    this.vizDeptSummaries = (this.departmentOptions ?? []).map((d, idx) => {
      const did = Number(d.deptId);
      const g = palette[idx % palette.length];
      const root = this.vizSkillByDeptId.get(did);
      const skillCount = (root?.children ?? []).reduce((acc: number, cat: any) => acc + (cat.children ?? []).length, 0);
      const subCount = subsCount(root);
      return { id: did, name: String(d.name ?? ''), abbr: String(d.name ?? '').slice(0, 3).toUpperCase(), group: g, skillCount, subCount };
    });
  }

  private buildVizDomainSummaries(): void {
    const palette = this.themePalette();
    this.vizLegend = palette.map((p) => ({ ...p }));
    this.vizDomainSummaries = (this.hierarchyDomainTree ?? []).map((d, idx) => {
      const g = palette[idx % palette.length];
      const subCount = (d.subdomains ?? []).length;
      const featCount = (d.domainFeatures ?? []).length + (d.subdomains ?? []).reduce((acc: number, su: any) => acc + (su.features ?? []).length, 0);
      return { id: Number(d.domainId), name: String(d.domainName ?? ''), abbr: String(d.domainName ?? '').slice(0, 3).toUpperCase(), group: g, subCount, featCount };
    });
  }

  private renderPrototypeViz(): void {
    if (this.hierarchyVizMode === 'radial') {
      this.renderPrototypeRadial();
    } else {
      this.renderPrototypeTree();
    }
  }

  private renderPrototypeRadial(): void {
    const svgEl = this.vizRadialSvg?.nativeElement;
    if (!svgEl) return;
    const svg = d3.select(svgEl);
    svg.selectAll('*').remove();

    const W = 680;
    const H = 500;
    const cx = 340;
    const cy = 250;
    const R = 190;
    const data = this.hierarchyViewMode === 'skills' ? this.vizDeptSummaries : this.vizDomainSummaries;
    const N = data.length || 1;
    const tip = this.vizTooltip?.nativeElement;

    svg.append('circle')
      .attr('cx', cx)
      .attr('cy', cy)
      .attr('r', 195)
      .attr('fill', 'none')
      .attr('stroke', 'var(--sm-tree-border)')
      .attr('stroke-width', 0.8)
      .attr('stroke-dasharray', '4,4');

    svg.append('circle')
      .attr('cx', cx)
      .attr('cy', cy)
      .attr('r', 125)
      .attr('fill', 'none')
      .attr('stroke', 'var(--sm-tree-border)')
      .attr('stroke-width', 0.6)
      .attr('stroke-dasharray', '3,3');

    data.forEach((d: any, i: number) => {
      const a = -Math.PI / 2 + (i / N) * 2 * Math.PI;
      const dx = cx + Math.cos(a) * R;
      const dy = cy + Math.sin(a) * R;
      const grp = d.group;
      svg.append('line')
        .attr('x1', cx)
        .attr('y1', cy)
        .attr('x2', dx)
        .attr('y2', dy)
        .attr('stroke', grp.color)
        .attr('stroke-width', 0.8)
        .attr('opacity', 0.22);
    });

    const nodeG = svg
      .selectAll('g.smhv-node')
      .data(data)
      .enter()
      .append('g')
      .attr('class', 'smhv-node')
      .attr('cursor', 'pointer')
      .attr('transform', (_: any, i: number) => {
        const a = -Math.PI / 2 + (i / N) * 2 * Math.PI;
        const dx = cx + Math.cos(a) * R;
        const dy = cy + Math.sin(a) * R;
        return `translate(${dx},${dy})`;
      })
      .on('mouseenter', function (ev: any, d: any) {
        d3.select(this).select('circle').attr('r', 28).attr('stroke-width', 2);
        if (!tip) return;
        tip.style.display = 'block';
        tip.style.left = `${ev.offsetX + 12}px`;
        tip.style.top = `${ev.offsetY + 12}px`;
        if (d.skillCount != null) {
          tip.innerHTML = `<strong>${d.name}</strong><br>${d.skillCount} skills · ${d.subCount} sub-skills`;
        } else {
          tip.innerHTML = `<strong>${d.name}</strong><br>${d.subCount} subdomains · ${d.featCount} features`;
        }
      })
      .on('mouseleave', function () {
        d3.select(this).select('circle').attr('r', 24).attr('stroke-width', 1.5);
        if (tip) tip.style.display = 'none';
      })
      .on('click', (_: any, d: any) => {
        this.setHierarchyVizMode('tree');
        if (this.hierarchyViewMode === 'skills') {
          this.vizSelectedDeptId = d.id;
        } else {
          this.vizSelectedDomainId = d.id;
        }
        setTimeout(() => this.renderPrototypeTree(), 0);
      });

    nodeG
      .append('circle')
      .attr('r', 24)
      .attr('fill', (d: any) => d.group.bg)
      .attr('stroke', (d: any) => d.group.color)
      .attr('stroke-width', 1.5);

    nodeG
      .append('text')
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'central')
      .attr('font-size', 8.5)
      .attr('font-weight', 600)
      .attr('fill', (d: any) => d.group.color)
      .text((d: any) => d.abbr);

    // center
    const center = svg.append('g').attr('transform', `translate(${cx},${cy})`);
    center.append('circle').attr('r', 52).attr('fill', 'rgba(25,61,138,0.10)').attr('stroke', 'var(--dark-cornflower-blue, #193d8a)').attr('stroke-width', 1.5);
    center.append('circle').attr('r', 38).attr('fill', 'var(--dark-cornflower-blue, #193d8a)');
    center.append('text').attr('text-anchor', 'middle').attr('dy', '-6').attr('font-size', 9).attr('font-weight', 600).attr('fill', '#fff').text(this.hierarchyViewMode === 'skills' ? 'Skill' : 'Domain');
    center.append('text').attr('text-anchor', 'middle').attr('dy', '7').attr('font-size', 9).attr('font-weight', 600).attr('fill', '#fff').text('Matrix');
    center.append('text').attr('text-anchor', 'middle').attr('dy', '24').attr('font-size', 8).attr('fill', 'rgba(255,255,255,0.7)').text(`${data.length} items`);
  }

  private renderPrototypeTree(): void {
    const svgEl = this.vizTreeSvg?.nativeElement;
    if (!svgEl) return;
    const svg = d3.select(svgEl);
    svg.selectAll('*').remove();

    const tip = this.vizTooltip?.nativeElement;
    if (tip) tip.style.display = 'none';

    const selectedKey =
      this.hierarchyViewMode === 'skills'
        ? (this.vizSelectedDeptId != null ? `skills:${Number(this.vizSelectedDeptId)}` : null)
        : (this.vizSelectedDomainId != null ? `domains:${Number(this.vizSelectedDomainId)}` : null);

    const rootData =
      this.hierarchyViewMode === 'skills'
        ? (this.vizSelectedDeptId != null ? this.vizSkillByDeptId.get(Number(this.vizSelectedDeptId)) : null)
        : (this.vizSelectedDomainId != null ? this.buildDomainTreeRoot(Number(this.vizSelectedDomainId)) : null);

    if (!rootData) {
      // nothing selected
      svg.attr('width', 680).attr('height', 220);
      svg.append('text')
        .attr('x', 20)
        .attr('y', 30)
        .attr('fill', 'var(--sm-tree-muted)')
        .attr('font-size', 12)
        .text(this.hierarchyViewMode === 'skills' ? 'Select a department to view tree.' : 'Select a domain to view tree.');
      return;
    }

    const HSEP = 210;
    const VSEP = 58;
    const NODE_W = 180;
    const NODE_H = 44;

    // Preserve expand/collapse state while user stays on same selection.
    if (selectedKey && this.vizTreeKey === selectedKey && this.vizTreeRoot) {
      // keep existing state
    } else {
      const hierarchy = d3.hierarchy(rootData);
      const collapseDepth = this.hierarchyViewMode === 'skills' ? 3 : 2;
      hierarchy.descendants().forEach((d: any) => {
        // Skills: keep Category + Skill expanded (so subskills are visible)
        // Domains: keep Domain collapsed by default (subdomains/features appear after expand)
        if (d.depth >= collapseDepth && d.children) {
          d.data._children = d.children;
          d.children = null;
        }
      });
      this.vizTreeRoot = hierarchy;
      this.vizTreeKey = selectedKey;
    }

    const layout = d3.tree().nodeSize([VSEP, HSEP]);
    layout(this.vizTreeRoot);
    const nodes = this.vizTreeRoot.descendants();
    const links = this.vizTreeRoot.links();

    const minX = Number(d3.min(nodes, (d: any) => d.x) ?? 0);
    const maxX = Number(d3.max(nodes, (d: any) => d.x) ?? 0);
    const maxY = Number(d3.max(nodes, (d: any) => d.y) ?? 0);
    const height = Math.max(300, (maxX - minX) + 120);
    const width = Math.max(820, maxY + NODE_W + 60);
    svg.attr('width', width).attr('height', height);

    const g = svg.append('g').attr('transform', `translate(30,${-minX + 60})`);
    const zoom = d3.zoom().scaleExtent([0.3, 2]).on('zoom', (ev: any) => g.attr('transform', ev.transform));
    svg.call(zoom as any);
    this.vizTreeZoom = zoom;

    g.selectAll('.link')
      .data(links)
      .enter()
      .append('path')
      .attr('class', 'link')
      .attr('fill', 'none')
      .attr('stroke', 'var(--sm-tree-border)')
      .attr('stroke-width', 1)
      .attr('d', (d: any) => {
        const sx = d.source.y + NODE_W;
        const sy = d.source.x;
        const tx = d.target.y;
        const ty = d.target.x;
        const mx = (sx + tx) / 2;
        return `M${sx},${sy}C${mx},${sy} ${mx},${ty} ${tx},${ty}`;
      });

    const node = g
      .selectAll('.node')
      .data(nodes)
      .enter()
      .append('g')
      .attr('class', 'node')
      .attr('transform', (d: any) => `translate(${d.y},${d.x - NODE_H / 2})`)
      .attr('cursor', 'pointer')
      .on('click', (_: any, d: any) => {
        if (d.children) {
          d.data._children = d.children;
          d.children = null;
        } else if (d.data._children) {
          d.children = d.data._children;
          d.data._children = null;
        }
        this.renderPrototypeTree();
      });

    const color = 'var(--dark-cornflower-blue, #193d8a)';
    node
      .append('rect')
      .attr('width', NODE_W)
      .attr('height', NODE_H)
      .attr('rx', 10)
      .attr('fill', '#fff')
      .attr('stroke', color)
      .attr('stroke-width', 0.7);

    node
      .append('text')
      .attr('x', NODE_W / 2)
      .attr('y', 18)
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'central')
      .attr('font-size', 10)
      .attr('fill', 'var(--sm-tree-text)')
      .text((d: any) => {
        const nm = String(d.data.name ?? '');
        return nm.length > 26 ? nm.slice(0, 25) + '…' : nm;
      });

    node
      .append('text')
      .attr('x', NODE_W / 2)
      .attr('y', 34)
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'central')
      .attr('font-size', 8)
      .attr('fill', 'var(--sm-tree-muted)')
      .text((d: any) => String(d.data.type ?? '').toUpperCase());

    // Skill node subskill count hint (so users can see subskills exist)
    node.each(function (d: any) {
      if (String(d.data.type ?? '') !== 'skill') return;
      const subCount =
        (Array.isArray(d.children) ? d.children.length : 0) +
        (Array.isArray(d.data._children) ? d.data._children.length : 0);
      if (!subCount) return;
      const gg = d3.select(this);
      gg.append('rect')
        .attr('x', NODE_W - 62)
        .attr('y', NODE_H - 14)
        .attr('width', 58)
        .attr('height', 12)
        .attr('rx', 6)
        .attr('fill', 'rgba(25, 61, 138, 0.10)')
        .attr('stroke', 'rgba(25, 61, 138, 0.18)')
        .attr('stroke-width', 0.6);
      gg.append('text')
        .attr('x', NODE_W - 33)
        .attr('y', NODE_H - 8)
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'central')
        .attr('font-size', 8)
        .attr('fill', 'var(--dark-cornflower-blue, #193d8a)')
        .text(`${subCount} sub`);
    });

    node.each(function (d: any) {
      const hasChildren = !!(d.children || d.data._children);
      if (!hasChildren) return;
      const isOpen = !!d.children;
      const gg = d3.select(this);
      gg.append('circle')
        .attr('cx', NODE_W)
        .attr('cy', NODE_H / 2)
        .attr('r', 8)
        .attr('fill', '#fff')
        .attr('stroke', color)
        .attr('stroke-width', 0.7);
      gg.append('text')
        .attr('x', NODE_W)
        .attr('y', NODE_H / 2)
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'central')
        .attr('font-size', 10)
        .attr('fill', color)
        .text(isOpen ? '–' : '+');
    });
  }

  private buildDomainTreeRoot(domainId: number): any | null {
    const dom = this.hierarchyDomainTree.find((d) => Number(d.domainId) === Number(domainId));
    if (!dom) return null;
    return {
      name: dom.domainName,
      type: 'domain',
      id: dom.domainId,
      children: [
        ...(dom.domainFeatures ?? []).map((f) => ({ name: f, type: 'feature', children: [] })),
        ...(dom.subdomains ?? []).map((su: any) => ({
          name: su.subdomainName,
          type: 'subdomain',
          children: (su.features ?? []).map((f: any) => ({ name: f, type: 'feature', children: [] }))
        }))
      ]
    };
  }

  private renderDomainOrgChart(): void {
    const container = this.domainOrgChartContainer?.nativeElement;
    if (!container) {
      return;
    }
    if (!Array.isArray(this.hierarchyDomainTree) || this.hierarchyDomainTree.length === 0) {
      return;
    }

    this.domainOrgChart?.destroy?.();
    this.domainOrgChart = null;
    container.innerHTML = '';

    const nodes: any[] = [];
    let uid = 0;
    const nextId = (prefix: string): string => `${prefix}-${++uid}`;

    this.hierarchyDomainTree.forEach((dom) => {
      const domId = nextId(`dom-${dom.domainId}`);
      nodes.push({ id: domId, pid: null, name: dom.domainName, title: 'Domain' });

      (dom.domainFeatures ?? []).forEach((f) => {
        nodes.push({ id: nextId('feat'), pid: domId, name: f, title: 'Feature' });
      });

      (dom.subdomains ?? []).forEach((su) => {
        const suId = nextId(`sub-${su.subdomainId}`);
        nodes.push({ id: suId, pid: domId, name: su.subdomainName, title: 'Subdomain' });
        (su.features ?? []).forEach((f) => nodes.push({ id: nextId('feat'), pid: suId, name: f, title: 'Feature' }));
      });
    });

    // Theme-aligned hierarchy boxes.
    OrgChart.templates.smHierarchy = Object.assign({}, OrgChart.templates.ana);
    OrgChart.templates.smHierarchy.size = [240, 110];
    OrgChart.templates.smHierarchy.node =
      '<rect x="0" y="0" height="{h}" width="{w}" fill="#FFFFFF" stroke-width="2" stroke="#193d8a" rx="8" ry="8"></rect>' +
      '<rect x="0" y="0" height="26" width="{w}" fill="#193d8a" rx="8" ry="8"></rect>' +
      '<rect x="0" y="18" height="10" width="{w}" fill="#193d8a"></rect>';
    OrgChart.templates.smHierarchy.field_0 =
      '<foreignObject x="10" y="34" width="220" height="52">' +
      '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size:14px;font-weight:700;color:#0f172a;text-align:center;line-height:1.25;overflow:hidden;text-overflow:ellipsis;">{val}</div>' +
      '</foreignObject>';
    OrgChart.templates.smHierarchy.field_1 =
      '<foreignObject x="10" y="86" width="220" height="20">' +
      '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size:11px;font-weight:600;color:#64748b;text-align:center;letter-spacing:0.08em;text-transform:uppercase;">{val}</div>' +
      '</foreignObject>';

    this.domainOrgChart = new OrgChart(container, {
      nodes,
      template: 'smHierarchy',
      enableSearch: false,
      layout: OrgChart.normal,
      orientation: OrgChart.orientation.top,
      nodeBinding: { field_0: 'name', field_1: 'title' },
      mouseScrool: OrgChart.action.scroll,
      nodeMouseClick: OrgChart.action.none,
      scaleInitial: 0.75,
      padding: 50,
      siblingSeparation: 70,
      subtreeSeparation: 90
    });
  }

  private loadCategories(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSkillCategories(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.categoriesSortActive, this.categoriesSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.categories = raw.map((c: any) => ({
            categoryId: c.categoryId,
            categoryName: c.categoryName ?? ''
          }));
          this.totalCategories = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadSkills(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSkillsMaster(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.skillsSortActive, this.skillsSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.skills = raw.map((s: any) => ({
            skillId: s.skillId,
            skillName: s.skillName ?? '',
            categoryId: s.categoryId,
            categoryName: s.categoryName ?? '',
            skillType: s.skillType ?? '',
            isActive: s.isActive,
            activeDisplay: this.activeLabel(s.isActive),
            departmentId: s.departmentId
          }));
          this.totalSkills = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadSubskills(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSubskillsMaster(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.subskillsSortActive, this.subskillsSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.subskills = raw.map((su: any) => ({
            subskillId: su.subskillId,
            skillId: su.skillId,
            skillName: su.skillName ?? '',
            subskillName: su.subskillName ?? '',
            isActive: su.isActive,
            activeDisplay: this.activeLabel(su.isActive)
          }));
          this.totalSubskills = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadDomains(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSkillDomains(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.domainsSortActive, this.domainsSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.domains = raw.map((d: any) => ({
            domainId: d.domainId,
            domainName: d.domainName ?? ''
          }));
          this.totalDomains = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadSubdomains(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSkillSubdomains(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.subdomainsSortActive, this.subdomainsSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.subdomains = raw.map((su: any) => ({
            subdomainId: su.subdomainId,
            domainId: su.domainId,
            domainName: su.domainName ?? '',
            subdomainName: su.subdomainName ?? '',
            isActive: su.isActive,
            activeDisplay: this.activeLabel(su.isActive)
          }));
          this.totalSubdomains = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadDomainFeatures(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService.getSkillDomainFeatures(
      this.page - 1,
      this.pageSize,
      this.filters,
      this.masterListSortFromMat(this.domainFeaturesSortActive, this.domainFeaturesSortDirection)
    ).pipe(first()).subscribe({
      next: (res: any) => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.domainFeatures = raw.map((f: any) => ({
            featureId: f.featureId,
            domainId: f.domainId,
            domainName: f.domainName ?? '',
            subdomainId: f.subdomainId != null ? Number(f.subdomainId) : null,
            subdomainName: f.subdomainName != null ? String(f.subdomainName) : null,
            featureName: f.featureName ?? '',
            isActive: f.isActive,
            activeDisplay: this.activeLabel(f.isActive)
          }));
          this.totalDomainFeatures = res.totalElements != null ? Number(res.totalElements) : raw.length;
        } else {
          this.applyListLoadFailure();
        }
      },
      error: () => {
        if (requestId !== this.latestListRequestId) {
          return;
        }
        this.loading = false;
        this.applyListLoadFailure();
      }
    });
  }

  private loadAspirationChips(silent = false): void {
    const requestId = ++this.latestListRequestId;
    if (!silent) {
      this.loading = true;
    }
    this.skillMatrixService
      .getAspirationChipsMaster(
        this.page - 1,
        this.pageSize,
        this.filters,
        this.masterListSortFromMat(this.aspirationChipsSortActive, this.aspirationChipsSortDirection)
      )
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (requestId !== this.latestListRequestId) {
            return;
          }
          this.loading = false;
          if (res?.serviceStatus === 'Success') {
            const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
            this.aspirationChips = raw.map((r: any) => ({
              chipId: +r.chipId,
              deptId: r.deptId != null ? +r.deptId : 0,
              departmentName: String(r.departmentName ?? ''),
              chipLabel: String(r.chipLabel ?? ''),
              sortOrder: r.sortOrder != null ? +r.sortOrder : 0,
              isActive: r.isActive !== false,
              activeDisplay: this.activeLabel(r.isActive)
            }));
            this.totalAspirationChips = res.totalElements != null ? Number(res.totalElements) : raw.length;
          } else {
            this.applyListLoadFailure();
          }
        },
        error: () => {
          if (requestId !== this.latestListRequestId) {
            return;
          }
          this.loading = false;
          this.applyListLoadFailure();
        }
      });
  }

  private rebuildAspirationChipDeptOptions(): void {
    this.aspirationChipDeptOptions = [
      { deptId: 0, name: 'Global (all departments)' },
      ...this.departmentOptions.map((d) => ({ deptId: d.deptId, name: d.name }))
    ];
  }

  private ensureCategoryOptions(done: () => void): void {
    this.skillMatrixService.getSkillCategories(0, 500, {}).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.categoryOptions = raw.map((c: any) => ({ categoryId: c.categoryId, categoryName: c.categoryName ?? '' }));
        }
        done();
      },
      error: () => done()
    });
  }

  private ensureSkillOptionsForSubskill(done: () => void): void {
    this.skillMatrixService.getSkillsMaster(0, 500, {}).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.skillOptionsForSubskill = raw.map((s: any) => ({ skillId: s.skillId, skillName: s.skillName ?? '' }));
        }
        done();
      },
      error: () => done()
    });
  }

  private ensureDomainOptionsForSubdomain(done: () => void): void {
    this.skillMatrixService.getSkillDomains(0, 500, {}).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          const raw = Array.isArray(res.serviceResponse) ? res.serviceResponse : [];
          this.domainOptionsForSubdomain = raw.map((d: any) => ({
            domainId: d.domainId,
            domainName: d.domainName ?? ''
          }));
        }
        done();
      },
      error: () => done()
    });
  }

  /** Sub domains for the selected domain in the Domain Feature modal (dropdown). */
  private refreshSubdomainOptionsForFeature(domainId: number | null, done?: () => void): void {
    if (domainId == null) {
      this.subdomainOptionsForFeature = [];
      done?.();
      return;
    }
    this.skillMatrixService.getSkillSubdomains(0, 500, { domainId: String(domainId) }).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
          this.subdomainOptionsForFeature = res.serviceResponse.map((s: any) => ({
            subdomainId: s.subdomainId,
            subdomainName: s.subdomainName ?? ''
          }));
        } else {
          this.subdomainOptionsForFeature = [];
        }
        done?.();
      },
      error: () => {
        this.subdomainOptionsForFeature = [];
        done?.();
      }
    });
  }

  onDomainFeatureDomainChanged(): void {
    this.domainFeatureForm.subdomainId = null;
    this.refreshSubdomainOptionsForFeature(this.domainFeatureForm.domainId);
  }

  featureScopeLabel(row: typeof this.domainFeatures[0]): string {
    if (row.subdomainId == null) {
      return 'Domain';
    }
    return row.subdomainName || '—';
  }

  /** Loads departments for the skill modal and for the Skills grid column label. */
  private loadDepartmentOptions(done?: () => void): void {
    this.departmentService.getAllDepartments().pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
          this.departmentOptions = res.serviceResponse
            .map((d: any) => ({
              deptId: Number(d.deptId),
              name: (d.name ?? '').toString()
            }))
            .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }));
        } else {
          this.departmentOptions = [];
        }
        done?.();
      },
      error: () => {
        this.departmentOptions = [];
        done?.();
      }
    });
  }

  /** Resolve department name for the Skills table when lookup is loaded. */
  departmentDisplay(departmentId: number | null | undefined): string {
    if (departmentId == null) {
      return '—';
    }
    const found = this.departmentOptions.find((d) => d.deptId === departmentId);
    return found ? found.name : String(departmentId);
  }

  openAddCategory(): void {
    this.categoryEditId = null;
    this.categoryForm = { categoryName: '' };
    this.modalRef = this.openMasterModal(this.categoryModalTpl);
  }

  editCategory(row: { categoryId: number; categoryName: string }): void {
    this.categoryEditId = row.categoryId;
    this.categoryForm = { categoryName: row.categoryName };
    this.modalRef = this.openMasterModal(this.categoryModalTpl);
  }

  saveCategory(): void {
    const name = (this.categoryForm.categoryName || '').trim();
    if (!name) {
      return;
    }
    const req$ = this.categoryEditId == null
      ? this.skillMatrixService.createSkillCategory({ categoryName: name })
      : this.skillMatrixService.updateSkillCategory(this.categoryEditId, { categoryName: name });
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadCategories();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteCategory(row: { categoryId: number; categoryName: string }): void {
    this.confirmDelete(
      'Delete category?',
      `Delete category "${row.categoryName}"?`,
      this.skillMatrixService.deleteSkillCategory(row.categoryId),
      () => this.loadCategories(),
      'Delete failed'
    );
  }

  openAddSkill(): void {
    let pending = 2;
    const open = (): void => {
      pending--;
      if (pending !== 0) {
        return;
      }
      this.skillEditId = null;
      this.skillForm = {
        skillName: '',
        categoryId: null,
        skillType: null,
        isActive: true,
        departmentId: null
      };
      this.modalRef = this.openMasterModal(this.skillModalTpl, 'lg');
    };
    this.ensureCategoryOptions(open);
    this.loadDepartmentOptions(open);
  }

  editSkill(row: typeof this.skills[0]): void {
    let pending = 2;
    const open = (): void => {
      pending--;
      if (pending !== 0) {
        return;
      }
      this.skillEditId = row.skillId;
      const st = (row.skillType || '').trim();
      this.skillForm = {
        skillName: row.skillName,
        categoryId: row.categoryId,
        skillType: st === 'Required' || st === 'Optional' ? st : null,
        isActive: row.isActive !== false,
        departmentId: row.departmentId
      };
      this.modalRef = this.openMasterModal(this.skillModalTpl, 'lg');
    };
    this.ensureCategoryOptions(open);
    this.loadDepartmentOptions(open);
  }

  saveSkill(): void {
    const name = (this.skillForm.skillName || '').trim();
    if (
      !name
      || this.skillForm.categoryId == null
      || this.skillForm.skillType == null
      || this.skillForm.departmentId == null
    ) {
      this.swalWarning('Skill name, category, type, and department are required.');
      return;
    }
    const body = {
      skillName: name,
      categoryId: this.skillForm.categoryId,
      skillType: this.skillForm.skillType,
      isActive: this.skillForm.isActive,
      departmentId: this.skillForm.departmentId
    };
    const req$ = this.skillEditId == null
      ? this.skillMatrixService.createSkill(body)
      : this.skillMatrixService.updateSkill(this.skillEditId, body);
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadSkills();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteSkill(row: typeof this.skills[0]): void {
    this.confirmDelete(
      'Delete skill?',
      `Delete skill "${row.skillName}"?`,
      this.skillMatrixService.deleteSkill(row.skillId),
      () => this.loadSkills(),
      'Delete failed'
    );
  }

  openAddSubskill(): void {
    this.ensureSkillOptionsForSubskill(() => {
      this.subskillEditId = null;
      this.subskillForm = {
        skillId: null,
        subskillName: '',
        isActive: true
      };
      this.modalRef = this.openMasterModal(this.subskillModalTpl, 'lg');
    });
  }

  editSubskill(row: typeof this.subskills[0]): void {
    this.ensureSkillOptionsForSubskill(() => {
      this.subskillEditId = row.subskillId;
      this.subskillForm = {
        skillId: row.skillId,
        subskillName: row.subskillName,
        isActive: row.isActive !== false
      };
      this.modalRef = this.openMasterModal(this.subskillModalTpl, 'lg');
    });
  }

  saveSubskill(): void {
    const name = (this.subskillForm.subskillName || '').trim();
    if (!name || this.subskillForm.skillId == null) {
      this.swalWarning('Subskill name and skill are required.');
      return;
    }
    const body = {
      skillId: this.subskillForm.skillId,
      subskillName: name,
      isActive: this.subskillForm.isActive
    };
    const req$ = this.subskillEditId == null
      ? this.skillMatrixService.createSubskill(body)
      : this.skillMatrixService.updateSubskill(this.subskillEditId, body);
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadSubskills();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteSubskill(row: typeof this.subskills[0]): void {
    this.confirmDelete(
      'Delete subskill?',
      `Delete subskill "${row.subskillName}"?`,
      this.skillMatrixService.deleteSubskill(row.subskillId),
      () => this.loadSubskills(),
      'Delete failed'
    );
  }

  openAddDomain(): void {
    this.domainEditId = null;
    this.domainForm = { domainName: '' };
    this.modalRef = this.openMasterModal(this.domainModalTpl);
  }

  editDomain(row: { domainId: number; domainName: string }): void {
    this.domainEditId = row.domainId;
    this.domainForm = { domainName: row.domainName };
    this.modalRef = this.openMasterModal(this.domainModalTpl);
  }

  saveDomain(): void {
    const name = (this.domainForm.domainName || '').trim();
    if (!name) {
      return;
    }
    const req$ = this.domainEditId == null
      ? this.skillMatrixService.createSkillDomain({ domainName: name })
      : this.skillMatrixService.updateSkillDomain(this.domainEditId, { domainName: name });
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadDomains();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteDomain(row: { domainId: number; domainName: string }): void {
    this.confirmDelete(
      'Delete domain?',
      `Delete domain "${row.domainName}"?`,
      this.skillMatrixService.deleteSkillDomain(row.domainId),
      () => this.loadDomains(),
      'Delete failed'
    );
  }

  openAddSubdomain(): void {
    this.ensureDomainOptionsForSubdomain(() => {
      this.subdomainEditId = null;
      this.subdomainForm = {
        domainId: null,
        subdomainName: '',
        isActive: true
      };
      this.modalRef = this.openMasterModal(this.subdomainModalTpl, 'lg');
    });
  }

  editSubdomain(row: typeof this.subdomains[0]): void {
    this.ensureDomainOptionsForSubdomain(() => {
      this.subdomainEditId = row.subdomainId;
      this.subdomainForm = {
        domainId: row.domainId,
        subdomainName: row.subdomainName,
        isActive: row.isActive !== false
      };
      this.modalRef = this.openMasterModal(this.subdomainModalTpl, 'lg');
    });
  }

  saveSubdomain(): void {
    const name = (this.subdomainForm.subdomainName || '').trim();
    if (!name || this.subdomainForm.domainId == null) {
      this.swalWarning('Sub domain name and domain are required.');
      return;
    }
    const body = {
      domainId: this.subdomainForm.domainId,
      subdomainName: name,
      isActive: this.subdomainForm.isActive
    };
    const req$ = this.subdomainEditId == null
      ? this.skillMatrixService.createSkillSubdomain(body)
      : this.skillMatrixService.updateSkillSubdomain(this.subdomainEditId, body);
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadSubdomains();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteSubdomain(row: typeof this.subdomains[0]): void {
    this.confirmDelete(
      'Delete sub domain?',
      `Delete sub domain "${row.subdomainName}"?`,
      this.skillMatrixService.deleteSkillSubdomain(row.subdomainId),
      () => this.loadSubdomains(),
      'Delete failed'
    );
  }

  openAddDomainFeature(): void {
    this.ensureDomainOptionsForSubdomain(() => {
      this.domainFeatureEditId = null;
      this.domainFeatureForm = {
        domainId: null,
        subdomainId: null,
        featureName: '',
        isActive: true
      };
      this.subdomainOptionsForFeature = [];
      this.modalRef = this.openMasterModal(this.domainFeatureModalTpl, 'lg');
    });
  }

  editDomainFeature(row: typeof this.domainFeatures[0]): void {
    this.ensureDomainOptionsForSubdomain(() => {
      this.refreshSubdomainOptionsForFeature(row.domainId, () => {
        this.domainFeatureEditId = row.featureId;
        this.domainFeatureForm = {
          domainId: row.domainId,
          subdomainId: row.subdomainId,
          featureName: row.featureName,
          isActive: row.isActive !== false
        };
        this.modalRef = this.openMasterModal(this.domainFeatureModalTpl, 'lg');
      });
    });
  }

  saveDomainFeature(): void {
    const name = (this.domainFeatureForm.featureName || '').trim();
    if (!name || this.domainFeatureForm.domainId == null) {
      this.swalWarning('Feature name and domain are required.');
      return;
    }
    const body: {
      domainId: number;
      subdomainId?: number | null;
      featureName: string;
      isActive: boolean;
    } = {
      domainId: this.domainFeatureForm.domainId,
      featureName: name,
      isActive: this.domainFeatureForm.isActive
    };
    body.subdomainId = this.domainFeatureForm.subdomainId != null ? this.domainFeatureForm.subdomainId : null;
    const req$ = this.domainFeatureEditId == null
      ? this.skillMatrixService.createSkillDomainFeature(body)
      : this.skillMatrixService.updateSkillDomainFeature(this.domainFeatureEditId, body);
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadDomainFeatures();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteDomainFeature(row: typeof this.domainFeatures[0]): void {
    this.confirmDelete(
      'Delete feature?',
      `Delete feature "${row.featureName}"?`,
      this.skillMatrixService.deleteSkillDomainFeature(row.featureId),
      () => this.loadDomainFeatures(),
      'Delete failed'
    );
  }

  openAddAspirationChip(): void {
    this.loadDepartmentOptions(() => {
      this.rebuildAspirationChipDeptOptions();
      this.aspirationChipEditId = null;
      this.aspirationChipForm = {
        deptId: 0,
        chipLabel: '',
        sortOrder: 0,
        isActive: true
      };
      this.modalRef = this.openMasterModal(this.aspirationChipModalTpl, 'lg');
    });
  }

  editAspirationChip(row: typeof this.aspirationChips[0]): void {
    this.loadDepartmentOptions(() => {
      this.rebuildAspirationChipDeptOptions();
      this.aspirationChipEditId = row.chipId;
      this.aspirationChipForm = {
        deptId: row.deptId,
        chipLabel: row.chipLabel,
        sortOrder: row.sortOrder,
        isActive: row.isActive !== false
      };
      this.modalRef = this.openMasterModal(this.aspirationChipModalTpl, 'lg');
    });
  }

  saveAspirationChip(): void {
    const label = (this.aspirationChipForm.chipLabel || '').trim();
    if (!label || this.aspirationChipForm.deptId == null) {
      this.swalWarning('Chip label and department scope are required.');
      return;
    }
    const body = {
      deptId: this.aspirationChipForm.deptId,
      chipLabel: label,
      sortOrder: this.aspirationChipForm.sortOrder != null ? Number(this.aspirationChipForm.sortOrder) : 0,
      isActive: this.aspirationChipForm.isActive
    };
    const req$ =
      this.aspirationChipEditId == null
        ? this.skillMatrixService.createAspirationChipMaster(body)
        : this.skillMatrixService.updateAspirationChipMaster(this.aspirationChipEditId, body);
    req$.pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success') {
          this.modalRef?.close();
          this.modalRef = null;
          this.loadAspirationChips();
        } else {
          this.swalError(res?.serviceResponse || 'Save failed');
        }
      },
      error: () => this.swalError('Save failed')
    });
  }

  confirmDeleteAspirationChip(row: typeof this.aspirationChips[0]): void {
    this.confirmDelete(
      'Delete aspiration chip?',
      `Delete chip "${row.chipLabel}"?`,
      this.skillMatrixService.deleteAspirationChipMaster(row.chipId),
      () => this.loadAspirationChips(),
      'Delete failed'
    );
  }

  activeLabel(v: boolean | null | undefined): string {
    if (v === true) {
      return 'Yes';
    }
    if (v === false) {
      return 'No';
    }
    return '—';
  }

  openBulkUploadModal(masterType: string): void {
    this.resetBulkUploadModalState(false);
    this.bulkUploadMasterType = masterType;
    this.bulkUploadPhase = 'pick';
    const options: NgbModalOptions = {
      centered: true,
      backdrop: 'static',
      windowClass: 'skill-matrix-master-modal',
      size: 'lg',
      beforeDismiss: () => !this.isBulkUploadBusy()
    };
    this.modalRef = this.modalService.open(this.bulkUploadModalTpl, options);
    this.modalRef.result.catch(() => {}).finally(() => {
      this.bulkUploadHttpSub?.unsubscribe();
      this.bulkUploadHttpSub = null;
      this.resetBulkUploadModalState(true);
      this.modalRef = null;
    });
  }

  onBulkModalFileChange(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.bulkUploadSelectedFile = file;
    }
  }

  clearBulkModalFile(input?: HTMLInputElement): void {
    this.bulkUploadSelectedFile = null;
    if (input) {
      input.value = '';
    }
  }

  executeBulkUploadFromModal(): void {
    if (!this.bulkUploadSelectedFile || !this.bulkUploadMasterType || this.isBulkUploadBusy()) {
      return;
    }
    this.clearBulkUploadProgressTick();
    this.bulkUploadPhase = 'uploading';
    this.bulkUploadBytesLoaded = 0;
    this.bulkUploadBytesTotal = null;
    this.bulkUploadUiPercent = 0;
    this.bulkUploadResult = null;
    this.bulkUploadErrorMessage = null;
    this.startBulkUploadProgressTick();
    this.bulkUploadHttpSub?.unsubscribe();
    this.bulkUploadHttpSub = this.skillMatrixService
      .bulkUploadMasterWithProgress(this.bulkUploadMasterType, this.bulkUploadSelectedFile)
      .subscribe({
        next: (event: HttpEvent<unknown>) => {
          if (event.type === HttpEventType.UploadProgress) {
            const total = event.total;
            this.bulkUploadBytesLoaded = event.loaded;
            if (total != null && total > 0) {
              this.bulkUploadBytesTotal = total;
              this.bulkUploadUiPercent = Math.min(42, Math.round((42 * event.loaded) / total));
              if (event.loaded >= total) {
                this.bulkUploadPhase = 'processing';
                this.bulkUploadUiPercent = Math.max(this.bulkUploadUiPercent, Math.min(42, Math.round((42 * event.loaded) / total)));
              }
            } else {
              this.bulkUploadBytesTotal = null;
            }
          }
          if (event.type === HttpEventType.Response) {
            const resp = event as HttpResponse<unknown>;
            this.handleBulkUploadHttpResponse(resp.body);
          }
        },
        error: (err: unknown) => {
          this.clearBulkUploadProgressTick();
          this.bulkUploadPhase = 'error';
          this.bulkUploadUiPercent = 0;
          const anyErr = err as { error?: { serviceResponse?: string }; statusText?: string };
          this.bulkUploadErrorMessage =
            anyErr?.error?.serviceResponse ||
            anyErr?.statusText ||
            'Upload failed. Please try again.';
          this.bulkUploadHttpSub = null;
        },
        complete: () => {
          this.bulkUploadHttpSub = null;
        }
      });
  }

  isBulkUploadBusy(): boolean {
    return this.bulkUploadPhase === 'uploading' || this.bulkUploadPhase === 'processing';
  }

  bulkUploadMasterLabel(): string {
    switch (this.bulkUploadMasterType) {
      case 'skill-categories':
        return 'Skill Category Master';
      case 'skills':
        return 'Skills Master';
      case 'subskills':
        return 'Subskills Master';
      case 'skill-domains':
        return 'Domain Master';
      case 'skill-subdomains':
        return 'Sub Domain Master';
      case 'skill-domain-features':
        return 'Domain Feature Master';
      default:
        return 'Master data';
    }
  }

  get bulkUploadShowProgressSection(): boolean {
    return this.bulkUploadPhase !== 'pick';
  }

  get bulkUploadProgressBarMode(): 'determinate' | 'indeterminate' {
    return 'determinate';
  }

  get bulkUploadProgressBarValue(): number {
    if (this.bulkUploadPhase === 'error') {
      return 0;
    }
    if (this.bulkUploadPhase === 'done') {
      return 100;
    }
    if (this.bulkUploadPhase === 'uploading' || this.bulkUploadPhase === 'processing') {
      return Math.min(100, Math.max(0, Math.round(this.bulkUploadUiPercent)));
    }
    return 0;
  }

  get bulkUploadProgressCaption(): string {
    switch (this.bulkUploadPhase) {
      case 'uploading':
        return 'Uploading file…';
      case 'processing':
        return 'Processing spreadsheet …';
      case 'done':
        return 'Finished';
      case 'error':
        return 'Stopped';
      default:
        return '';
    }
  }

  get bulkUploadPercentDisplay(): string {
    if (this.bulkUploadPhase === 'uploading' || this.bulkUploadPhase === 'processing') {
      return `${Math.min(100, Math.max(0, Math.round(this.bulkUploadUiPercent)))}%`;
    }
    if (this.bulkUploadPhase === 'done') {
      return '100%';
    }
    return '—';
  }

  parseBulkRowError(line: string): { row: string; message: string } {
    const m = /^Row\s+(\d+):\s*(.*)$/i.exec(line?.trim() || '');
    if (m) {
      return { row: m[1], message: m[2] || '—' };
    }
    return { row: '—', message: line || '—' };
  }

  bulkUploadAnotherFile(fileInput?: HTMLInputElement): void {
    this.clearBulkUploadProgressTick();
    this.bulkUploadPhase = 'pick';
    this.bulkUploadSelectedFile = null;
    this.bulkUploadResult = null;
    this.bulkUploadErrorMessage = null;
    this.bulkUploadBytesLoaded = 0;
    this.bulkUploadBytesTotal = null;
    this.bulkUploadUiPercent = 0;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  closeBulkUploadModal(): void {
    if (this.isBulkUploadBusy()) {
      return;
    }
    this.modalRef?.dismiss('close');
  }

  private handleBulkUploadHttpResponse(body: unknown): void {
    this.clearBulkUploadProgressTick();
    const res = body as { serviceStatus?: string; serviceResponse?: unknown };
    if (!res || res.serviceStatus !== 'Success') {
      const msg =
        typeof res?.serviceResponse === 'string'
          ? res.serviceResponse
          : 'Bulk upload failed';
      this.bulkUploadPhase = 'error';
      this.bulkUploadErrorMessage = msg;
      this.bulkUploadUiPercent = 0;
      return;
    }
    const svc = res.serviceResponse as { inserted?: number; failed?: number; rowErrors?: string[] };
    const inserted = svc?.inserted ?? 0;
    const failed = svc?.failed ?? 0;
    this.bulkUploadResult = {
      inserted,
      failed,
      totalRecords: inserted + failed,
      rowErrors: Array.isArray(svc?.rowErrors) ? svc.rowErrors : []
    };
    this.bulkUploadPhase = 'done';
    this.bulkUploadUiPercent = 100;
    if (this.bulkUploadMasterType) {
      this.refreshMasterListAfterBulk(this.bulkUploadMasterType);
    }
  }

  private resetBulkUploadModalState(clearMasterType: boolean): void {
    this.clearBulkUploadProgressTick();
    this.bulkUploadHttpSub?.unsubscribe();
    this.bulkUploadHttpSub = null;
    if (clearMasterType) {
      this.bulkUploadMasterType = null;
    }
    this.bulkUploadSelectedFile = null;
    this.bulkUploadPhase = 'pick';
    this.bulkUploadBytesLoaded = 0;
    this.bulkUploadBytesTotal = null;
    this.bulkUploadUiPercent = 0;
    this.bulkUploadResult = null;
    this.bulkUploadErrorMessage = null;
  }

  private startBulkUploadProgressTick(): void {
    this.clearBulkUploadProgressTick();
    this.bulkUploadProgressTickId = setInterval(() => this.tickBulkUploadUiProgress(), 200);
  }

  private clearBulkUploadProgressTick(): void {
    if (this.bulkUploadProgressTickId != null) {
      clearInterval(this.bulkUploadProgressTickId);
      this.bulkUploadProgressTickId = null;
    }
  }

  /** Keeps the bar moving during the single HTTP round-trip (no row-level server events). */
  private tickBulkUploadUiProgress(): void {
    const maxUpload = 42;
    const capProcessing = 92;
    if (this.bulkUploadPhase === 'uploading') {
      const t = this.bulkUploadBytesTotal;
      const loaded = this.bulkUploadBytesLoaded;
      if (t != null && t > 0) {
        this.bulkUploadUiPercent = Math.min(maxUpload, Math.round((maxUpload * loaded) / t));
      } else {
        this.bulkUploadUiPercent = Math.min(28, this.bulkUploadUiPercent + 2);
      }
    } else if (this.bulkUploadPhase === 'processing') {
      const step = Math.max(1, Math.round((capProcessing - this.bulkUploadUiPercent) * 0.08));
      this.bulkUploadUiPercent = Math.min(capProcessing, this.bulkUploadUiPercent + step);
    }
  }

  downloadBulkTemplate(masterType: string): void {
    this.skillMatrixService.downloadBulkTemplate(masterType).pipe(first()).subscribe({
      next: (resp) => {
        const blob = resp.body;
        if (!blob) {
          this.swalError('Empty download');
          return;
        }
        const cd = resp.headers.get('Content-Disposition');
        let filename = 'template.xlsx';
        if (cd) {
          const m = /filename\*=(?:UTF-8'')?([^;]+)|filename="([^"]+)"|filename=([^;\s]+)/i.exec(cd);
          if (m) {
            filename = decodeURIComponent((m[1] || m[2] || m[3] || '').replace(/["']/g, '').trim());
          }
        }
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename || 'template.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.swalError('Could not download template')
    });
  }

  private refreshMasterListAfterBulk(masterType: string): void {
    switch (masterType) {
      case 'skill-categories':
        this.loadCategories();
        break;
      case 'skills':
        this.loadSkills();
        break;
      case 'subskills':
        this.loadSubskills();
        break;
      case 'skill-domains':
        this.loadDomains();
        break;
      case 'skill-subdomains':
        this.loadSubdomains();
        break;
      case 'skill-domain-features':
        this.loadDomainFeatures();
        break;
      default:
        break;
    }
  }

  private swalError(message: string): void {
    void Swal.fire({
      icon: 'error',
      title: 'Error',
      text: message,
      confirmButtonColor: '#193d8a'
    });
  }

  private swalWarning(message: string): void {
    void Swal.fire({
      icon: 'warning',
      text: message,
      confirmButtonColor: '#193d8a'
    });
  }

  private confirmDelete(
    title: string,
    text: string,
    req: Observable<any>,
    onSuccess: () => void,
    failMsg: string
  ): void {
    void Swal.fire({
      title,
      text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      focusCancel: true
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }
      req.pipe(first()).subscribe({
        next: (res: any) => {
          if (res?.serviceStatus === 'Success') {
            onSuccess();
          } else {
            this.swalError(res?.serviceResponse || failMsg);
          }
        },
        error: () => this.swalError(failMsg)
      });
    });
  }
}
