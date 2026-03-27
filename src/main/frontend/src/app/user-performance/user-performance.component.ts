
import { LocationStrategy } from '@angular/common';
import { ChangeDetectorRef, Component, HostListener, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from 'src/app/app.component';
import { Performance } from 'src/app/models/performance';
import { Log } from '../models/log';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { LogService } from 'src/app/services/log.service';
import { AuthenticationService } from '../services/authentication.service';
import { PerformanceService } from '../services/performance.service';
import { forkJoin, from, of, Observable } from 'rxjs';
import { catchError, concatMap, first, map, reduce } from 'rxjs/operators';
import { DepartmentService } from '../services/department.service';
import { EmployeeService } from '../services/employee.service';
import { Employee360Service } from '../services/employee360.service';
import { ExportExcelService } from '../services/export-excel.service';
import { UtilityService } from '../services/utility.service';
import { ValidationService } from '../services/validation.service';
import { AppreciationAndRewardsCount } from '../models/appreciationAndRewardCount';
import { Employee } from '../models/employee';
import { HrHodMangerApiForPerformnace } from '../models/hrHodMangerApiForPerformnace';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { Query } from 'src/app/models/query';
import { SortPipe } from 'src/app/sort.pipe';
class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

@Component({
  standalone: false,
  selector: 'app-user-performance',
  templateUrl: './user-performance.component.html',
  styleUrls: ['./user-performance.component.css']
})
export class UserPerformanceComponent implements OnInit {
  tabName:any = 'Performance ';
  feature = "Performance"
  currentUser:User;
  userMapping:any = {};
  mappTeamDashboard:boolean = false;

  log:Log;
  activeTab: string = 'performance-dashboard';
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild("approval_details_modal")
  approvalDetailsModalTemplate: TemplateRef<any>;
  @ViewChild("status_detail_modal")
  statusDetailModalTemplate: TemplateRef<any>;
  @ViewChild("hod_pending_alert")
  hodPendingAlertTemplate: TemplateRef<any>;
  /** sessionStorage key: show HOD queue popup once per browser session (bump when logic changes). */
  private static readonly HOD_QUEUE_ALERT_SESSION_KEY = 'pms-hod-queue-alert-v3';
  /** Prevents opening the HOD reminder twice in one visit (merge + ngAfterViewInit may both schedule). */
  private hodQueueAlertModalShown = false;
  /** True after `getAllEmployee` succeeds so the HOD popup waits for real table data, not an empty first paint. */
  private employeePerformanceListLoaded = false;
  page = 1;
  filters: any = {};
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef:NgbModalRef;
  approvalDetailsModalRef: NgbModalRef;
  summaryStatusModalRef: NgbModalRef;
  /** Title for status detail popup (e.g. "Not Started - Employees") */
  summaryStatusTitle = '';
  /** Filtered list of employees for the selected status in the popup */
  summaryStatusEmployees: any[] = [];
  /** When set, main table shows only employees in this status (from performance summary click). */
  summaryStatusFilter: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected' | null = null;
  /** Table list source: all employees (default), eligible, or not eligible. */
  tableListMode: 'eligible' | 'all' | 'notEligible' = 'all';
  /** Active filter from rating criteria count cards (NI, M-, M, M+, E). */
  ratingCategoryFilter: 'NI' | 'M-' | 'M' | 'M+' | 'E' | null = null;
  /** HOD: table shows only employees waiting for HOD approval (same set as popup / pending count). */
  hodActionQueueFilter = false;
  /** When true, employee table shows direct reports for the breadcrumb anchor (same API as My Team). */
  viewHierarchyEnabled = false;
  /** After first successful employee fetch, HOD users get hierarchy view on by default (once). */
  private hierarchyDefaultActivated = false;
  hierarchyRows: any[] = [];
  /** Breadcrumb trail for hierarchy drill-down; last item is the anchor for loaded rows. */
  hierarchyBreadCrumbs: { empId: any; label: string; employeementId?: any }[] = [];
  hierarchyLoading = false;
  /** From getAllManagers — used to show sitemap / drill-down for employees who have reportees. */
  hierarchyManagersList: any[] = [];
  private hierarchyManagersLoaded = false;
  private static readonly RATING_LABELS = ['NI', 'M-', 'M', 'M+', 'E'] as const;
  /** Performance instructions carousel (non-HR dashboard): 0 = criteria & styles, 1 = flow & note */
  instructionSlideIndex = 0;
  readonly instructionSlideCount = 2;
  /** Popup data for Manager/HOD/HR approval details and role-wise audit history. */
  approvalDetailsPopup: { role: string; name: string; id: any; employmentId: string; rating: string; feedback: string; history: any[]; yearWiseRatings: any[]; employeeName: string; employeeEmploymentId: string } | null = null;
  approvalDetailsLoading = false;
  submitPerformance: Performance = new Performance();
  updatePerformanceHr :Performance = new Performance();
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  queryList: any[] = [];
  storedDataList: any[] = [];
  data: string;
  employeeDataForExcel: any[] = [];
  selectedBulkEmpIds: Set<number> = new Set<number>();
  bulkHrRemark: string = '';
  bulkActionInProgress = false;
  performnace: Performance = new Performance();
  myMap: Map<string, string> = new Map();




  isperformanceDsah: boolean = false;
  isreviewPage: boolean = false;
  allEmployee: any[] = [];
  allEmployee1: any[] = [];
  allReviewType: any[] = [];
  allQauterCycle: any[] = [];
  eligibleEmployees: any[] = [];
  eligibleEmployees1: any[] = [];
  hrReviewStatus:any;
  // currentUser: any;
  // userMapping: any = {};
  EnabledAndActiveQuarterCycle: any[] = [];
  isSearchEnabled: boolean = false;
  selectedEmployee = new Employee();
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department', 'Designation',
    'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name',
    'Employment Status', 'Date Of Joining', 'Domain', 'Specialization', 'City', 'Blood Group',
    'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status',
    'Bank Name', 'Created By', 'State', 'Created On'];

  eligibleEmployeesColumns: any[] = ['employmentIdAcToET', 'name', 'designationName', 'departmentName','totalExperience', 'employmentstatus', 'dateOfJoining','completionStatus'];
  finalRating: number;
  hodRemarks: any;
  hrRemarks: any;
  recommendationsRemarks: any; 
  quarterId: any;
  rewardsCount:any;
  appreciationCount:any;
  isEditMode:boolean=false;
  appreciationAndRewardsCount:AppreciationAndRewardsCount=new AppreciationAndRewardsCount();
  departmentData: any[] = [
    // { department: 'HR', TotalNumberofemp: 10, ratinggivenbymanager: 7, pendingratinggivenbymanager: 3, managerName: 'Saxena' },
    // { department: 'Functional Testing', TotalNumberofemp: 15, ratinggivenbymanager: 10, pendingratinggivenbymanager: 5, managerName: 'Dev' },
    // { department: 'INHOUSE', TotalNumberofemp: 13, ratinggivenbymanager: 3, pendingratinggivenbymanager: 10, managerName: 'Mayur' },
    // { department: 'Devops', TotalNumberofemp: 20, ratinggivenbymanager: 10, pendingratinggivenbymanager: 10, managerName: 'vishal' },
    // { department: 'Finanace', TotalNumberofemp: 33, ratinggivenbymanager: 30, pendingratinggivenbymanager: 3, managerName: 'suresh' },
    // { department: 'Cloud Dep', TotalNumberofemp: 65, ratinggivenbymanager: 40, pendingratinggivenbymanager: 20, managerName: 'Jitendra' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },

  ];
  performanceChartRef: Highcharts.Chart | null = null;

  myList: { reviewLabel: any; silde: any; performanceRatingId: any; comment?: string }[] = [];
  myRateList: { reviewLabel: any; rate: any; performanceRatingId: any; comment?: string }[] = [];

  allQauterCycle2: any;
  confirmModalRef :any;
  confirmDiscard: boolean = false;
  @ViewChild('confirm_discard_modal') confirmDiscardModal!: TemplateRef<any>;
   
  constructor(
     private router: Router,
    private route: ActivatedRoute,
    private logService: LogService,
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private utilityService: UtilityService,
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private locationStrategy: LocationStrategy,
    private performanceSerive: PerformanceService,
    private exportExcelService: ExportExcelService,
    private performanceService: PerformanceService,
    private employee360Service: Employee360Service,
    private departmentService:DepartmentService,
    private cdr: ChangeDetectorRef
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.featureName = this.feature;
    });
  }
  ngAfterViewInit(): void {
    if (this.userMapping?.performance_action_by_hr) {
      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    if (this.performanceChartRef) {
      // Recalculate chart size after viewport/zoom changes.
      setTimeout(() => this.performanceChartRef?.reflow(), 80);
    }
  }

  async ngOnInit(): Promise<void> {
    try {
      this.getAllDepartments();

      // User mapping first so HR-only features (Performance Analysis chart) can be gated
      const featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap?.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });
      this.userDetailsForPerformanceView.empId = this.currentUser.empId;
      this.userDetailsForPerformanceView.hrvalidate = this.userMapping.performance_action_by_hr;

      await this.getALLdepartmentByEmployee();

      this.getCurrentUserDepartment();

      this.getAllReviveType();
      this.getAllQauterCycle();

      this.isperformanceDsah = true;
      console.log("usermappinghodhr",this.userMapping);
      console.log("hodddddd", this.userMapping.performance_action_by_hod);
      console.log("hrrrrrrrr", this.userMapping.performance_action_by_hr);
      console.log("rmmm", this.userMapping.performance_action_by_approvals_tos);
     

       
       this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();

    } catch (error) {
      console.error("Error in ngOnInit", error);
    }
  }

  performanceData = [
    { criteria: "Consistency", rating: 0 },
    { criteria: "Team Collaboration", rating: 0 },
    { criteria: "Innovation", rating: 4 },
    { criteria: "Communication Skills", rating: 5 },
    { criteria: "Stakeholder Feedback", rating: 3 },
    { criteria: "Problem Solving", rating: 4 },
    { criteria: "Decision Making", rating: 3 },
    { criteria: "Professionalism and Work Ethics", rating: 4 },
    { criteria: "Initiative Nature", rating: 4 }
  ];

  filterCriteria: any[] = [];
  filterCriteria1: any[] = [];
  filterRatingCriteria: any[] = [];
  filterCriteriaQuarter: any[] = [];

  // updateSlide(sliderIndex: number, newRating: number) {
  //   if (!this.myList[sliderIndex]) {
  //     this.myList[sliderIndex] = { reviewLabel: this.filterCriteria[sliderIndex]?.reviewLabel, silde: 0 };
  //   }
  //   this.myList[sliderIndex].silde = newRating;

  //   console.log("Slider Data:", this.myList);
  //   this.calculateFinalRating();
  // }

  // updateRating(ratingIndex: number, rating: number) {
  //   if (!this.myRateList[ratingIndex]) {
  //     this.myRateList[ratingIndex] = { reviewLabel: this.filterRatingCriteria[ratingIndex]?.reviewLabel, rate: 0 };
  //   }
  //   this.myRateList[ratingIndex].rate = rating;

  //   console.log("Rating Data:", this.myRateList);
  //   this.calculateFinalRating();
  // }

  updateSlide(index: number, newRating: number) {

    this.myList[index].silde = newRating;


    console.log("data log", this.myList);
    this.calculateFinalRating();
  }


  updateRating(index: number, rating: number) {
    this.myRateList[index].rate = rating;
    console.log("data log", this.myRateList);
    this.calculateFinalRating();
  }




  RatingData: any[] = [];

userDetailsForPerformanceView:HrHodMangerApiForPerformnace=new HrHodMangerApiForPerformnace();

  /** Normalize employee ids across APIs (string/number) for reliable matching. */
  private normalizeEmpId(empId: any): string {
    return empId == null ? '' : String(empId).trim();
  }

  /** Strips A-/AP- prefix from employeementId before POST (same as My Team hierarchy). */
  private stripEmployeementIdForHierarchyApi(employee: Employee): void {
    if (employee.employeementId != null && typeof employee.employeementId === 'string') {
      if (employee.employeementId.startsWith('A-')) {
        employee.employeementId = employee.employeementId.substring(2);
      } else if (employee.employeementId.startsWith('AP-')) {
        employee.employeementId = employee.employeementId.substring(3);
      }
    }
  }

  /** Overlay static performance fields when no row exists in allEmployee. */
  private applyStaticOverlayToEmp(emp: any): void {
    if (!this.static?.length || !emp?.empId) return;
    const staticData = this.static.find((item: any) => this.normalizeEmpId(item?.empId) === this.normalizeEmpId(emp.empId));
    if (!staticData) return;
    if (staticData.finalRating != null && staticData.finalRating !== '') {
      emp.finalRating = staticData.finalRating;
    } else if (staticData.averageRating != null && staticData.averageRating !== '') {
      emp.finalRating = staticData.averageRating;
    }
    if (staticData.completionStatus != null) emp.completionStatus = staticData.completionStatus;
    if (staticData.performanceStatusPercentage != null) emp.performanceStatusPercentage = staticData.performanceStatusPercentage;
    if (staticData.managerReviewStatus != null) emp.managerReviewStatus = staticData.managerReviewStatus;
    if (staticData.hodReviewStatus != null) emp.hodReviewStatus = staticData.hodReviewStatus;
    if (staticData.hrReviewStatus != null) emp.hrReviewStatus = staticData.hrReviewStatus;
  }

  /** First non-empty display value (hierarchy API omits many fields; perf row may have blanks). */
  private coalesceNonEmpty(...vals: any[]): any {
    for (const v of vals) {
      if (v === null || v === undefined) continue;
      if (typeof v === 'number' && !isNaN(v)) return v;
      const s = String(v).trim();
      if (s === '' || s === '-') continue;
      return v;
    }
    return undefined;
  }

  /** When allEmployee has duplicate empId rows, merge so we keep the best non-empty fields. */
  private mergeDuplicatePerfRows(rows: any[]): any {
    if (!rows?.length) return {};
    const out: any = { ...rows[0] };
    for (let i = 1; i < rows.length; i++) {
      const r = rows[i];
      Object.keys(r || {}).forEach((k) => {
        const v = r[k];
        if (v === null || v === undefined) return;
        if (typeof v === 'number' && !isNaN(v)) {
          if (out[k] == null || String(out[k]).trim() === '') out[k] = v;
          return;
        }
        const s = String(v).trim();
        if (s === '' || s === '-') return;
        if (out[k] == null || String(out[k]).trim() === '') out[k] = v;
      });
    }
    return out;
  }

  private getPerfRowByEmpId(empId: any): any | undefined {
    const id = this.normalizeEmpId(empId);
    if (!id) return undefined;
    const matches = (this.allEmployee || []).filter((e: any) => this.normalizeEmpId(e?.empId) === id);
    if (matches.length === 0) return undefined;
    if (matches.length === 1) return matches[0];
    return this.mergeDuplicatePerfRows(matches);
  }

  private mergeHierarchyApiRows(a: any, b: any): any {
    const out = { ...a };
    Object.keys(b || {}).forEach((k) => {
      const v = b[k];
      if (v === null || v === undefined) return;
      if (typeof v === 'number' && !isNaN(v)) {
        out[k] = v;
        return;
      }
      const s = String(v).trim();
      if (s === '' || s === '-') return;
      if (out[k] == null || String(out[k]).trim() === '') out[k] = v;
    });
    return out;
  }

  /** Backend hierarchy list can contain duplicate empId entries; collapse to one row per employee. */
  private dedupeHierarchyRawList(rawList: any[]): any[] {
    const byId = new Map<string, any>();
    for (const r of rawList || []) {
      const id = this.normalizeEmpId(r?.empId);
      if (!id) continue;
      const existing = byId.get(id);
      if (!existing) {
        byId.set(id, { ...r });
      } else {
        byId.set(id, this.mergeHierarchyApiRows(existing, r));
      }
    }
    return Array.from(byId.values());
  }

  /**
   * Merge hierarchy API row with performance list + static.
   * Backend getHierarchyByEmpId returns only a subset (name, email, jobRoleName, employeementId, …).
   * Start from perf (full row) and overlay non-empty API fields so empty strings in perf do not wipe API data.
   */
  mergeHierarchyRowWithPerformance(empFromApi: any): any {
    const perf = this.getPerfRowByEmpId(empFromApi?.empId);
    const merged: any = perf ? { ...perf } : { ...empFromApi };

    if (empFromApi) {
      Object.keys(empFromApi).forEach((k) => {
        const v = empFromApi[k];
        if (v === null || v === undefined) return;
        if (typeof v === 'number' && !isNaN(v)) {
          merged[k] = v;
          return;
        }
        const s = String(v).trim();
        if (s !== '' && s !== '-') merged[k] = v;
      });
    }

    merged.designationName = this.coalesceNonEmpty(
      merged.designationName,
      merged.jobRoleName,
      empFromApi?.jobRoleName,
      empFromApi?.designationName
    );
    merged.departmentName = this.coalesceNonEmpty(merged.departmentName, empFromApi?.departmentName);
    merged.employmentstatus = this.coalesceNonEmpty(merged.employmentstatus, empFromApi?.employmentstatus);
    merged.dateOfJoining = this.coalesceNonEmpty(merged.dateOfJoining, empFromApi?.dateOfJoining);
    merged.totalExperience = this.coalesceNonEmpty(
      merged.totalExperience,
      empFromApi?.totalExperience,
      empFromApi?.total_experience
    );
    merged.completionStatus = this.coalesceNonEmpty(merged.completionStatus, empFromApi?.completionStatus);
    merged.performanceStatusPercentage = this.coalesceNonEmpty(
      merged.performanceStatusPercentage,
      empFromApi?.performanceStatusPercentage
    );

    merged.employeementId = this.coalesceNonEmpty(merged.employeementId, empFromApi?.employeementId);
    merged.mobileNo = merged.mobileNo ?? merged.mobile_no ?? empFromApi?.mobileNo ?? empFromApi?.mobile_no;
    merged.workLocation = this.coalesceNonEmpty(
      merged.workLocation,
      merged.work_location,
      empFromApi?.workLocation,
      empFromApi?.work_location
    );
    merged.employmentIdAcToET = this.coalesceNonEmpty(
      merged.employmentIdAcToET,
      perf?.employmentIdAcToET,
      empFromApi?.employmentIdAcToET
    );
    if (!merged.employmentIdAcToET && merged.employeementId != null && merged.employeementId !== '') {
      merged.employmentIdAcToET = this.utilityService.appendEmployeementid(
        merged.isConsultant,
        String(merged.employeementId)
      );
    }

    merged.emp360 = merged.empId;
    if (merged.dateOfJoining && (merged.calculatedExperience == null || merged.calculatedExperience === '')) {
      merged.calculatedExperience = this.calculateExperienceFromDOJ(String(merged.dateOfJoining));
    }

    this.applyStaticOverlayToEmp(merged);

    merged.fullSearchText = [merged.name, merged.employmentIdAcToET, merged.departmentName, merged.designationName]
      .filter(Boolean).join(' ').toLowerCase();
    this.applyExperienceTotalsToEmployee(merged);
    return merged;
  }

  /** True if row is still missing fields the hierarchy API does not provide (need getEmployeeByEmpId). */
  private hierarchyRowNeedsEnrichment(row: any): boolean {
    return !this.coalesceNonEmpty(row?.departmentName)
      || !this.coalesceNonEmpty(row?.dateOfJoining)
      || !this.coalesceNonEmpty(row?.employmentIdAcToET)
      || !this.coalesceNonEmpty(row?.employmentstatus);
  }

  private mergeEmployeeDetailIntoHierarchyRow(row: any, detail: any): any {
    if (!detail) return row;
    const out: any = { ...row };
    out.departmentName = this.coalesceNonEmpty(row.departmentName, detail.departmentName);
    out.designationName = this.coalesceNonEmpty(row.designationName, detail.designationName, detail.jobRoleName);
    out.dateOfJoining = this.coalesceNonEmpty(row.dateOfJoining, detail.dateOfJoining);
    out.employmentstatus = this.coalesceNonEmpty(row.employmentstatus, detail.employmentstatus);
    out.totalExperience = this.coalesceNonEmpty(row.totalExperience, detail.totalExperience);
    out.employeementId = this.coalesceNonEmpty(row.employeementId, detail.employeementId);
    out.isConsultant = this.coalesceNonEmpty(row.isConsultant, detail.isConsultant);
    out.name = this.coalesceNonEmpty(row.name, detail.name);
    out.mobileNo = row.mobileNo ?? detail.mobileNo ?? row.mobile_no ?? detail.mobile_no;
    out.workLocation = this.coalesceNonEmpty(
      row.workLocation,
      detail.workLocation,
      row.work_location,
      detail.work_location
    );
    out.employmentIdAcToET = this.coalesceNonEmpty(row.employmentIdAcToET, detail.employmentIdAcToET);
    if (!out.employmentIdAcToET && out.employeementId != null && String(out.employeementId).trim() !== '') {
      out.employmentIdAcToET = this.utilityService.appendEmployeementid(out.isConsultant, String(out.employeementId));
    }
    if (out.dateOfJoining && (out.calculatedExperience == null || out.calculatedExperience === '')) {
      out.calculatedExperience = this.calculateExperienceFromDOJ(String(out.dateOfJoining));
    }
    this.applyStaticOverlayToEmp(out);
    out.fullSearchText = [out.name, out.employmentIdAcToET, out.departmentName, out.designationName]
      .filter(Boolean).join(' ').toLowerCase();
    this.applyExperienceTotalsToEmployee(out);
    return out;
  }

  private fetchEmployeeDetailForHierarchy(row: any): Observable<{ empIdKey: string; detail: any | null }> {
    const emp = new Employee();
    emp.empId = row.empId;
    return this.employeeService.getEmployeeByEmpId(emp).pipe(
      first(),
      map((response: any) => ({
        empIdKey: this.normalizeEmpId(row.empId),
        detail: response?.serviceStatus === 'Success' ? response.serviceResponse : null
      })),
      catchError(() => of({ empIdKey: this.normalizeEmpId(row.empId), detail: null }))
    );
  }

  /** Fill gaps for rows not fully present in allEmployee (chunked to avoid flooding the API). */
  private enrichHierarchyRowsInChunks(rows: any[], onDone?: () => void): void {
    const need = rows.filter((r) => this.hierarchyRowNeedsEnrichment(r));
    if (!need.length) {
      this.hierarchyRows = rows;
      this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
      this.page = 1;
      onDone?.();
      return;
    }
    const chunkSize = 8;
    const chunks: any[][] = [];
    for (let i = 0; i < need.length; i += chunkSize) {
      chunks.push(need.slice(i, i + chunkSize));
    }
    from(chunks)
      .pipe(
        concatMap((chunk) => forkJoin(chunk.map((r) => this.fetchEmployeeDetailForHierarchy(r)))),
        reduce((acc: { empIdKey: string; detail: any | null }[], val) => acc.concat(val), [])
      )
      .subscribe({
        next: (allParts) => {
          const detailById = new Map(allParts.map((p) => [p.empIdKey, p.detail]));
          this.hierarchyRows = rows.map((row) => {
            const key = this.normalizeEmpId(row.empId);
            const d = detailById.get(key);
            return d ? this.mergeEmployeeDetailIntoHierarchyRow(row, d) : row;
          });
          this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
          this.page = 1;
          onDone?.();
        },
        error: () => {
          this.hierarchyRows = rows;
          this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
          this.page = 1;
          onDone?.();
        }
      });
  }

  private setHierarchyIsHierarchyFlags(rows: any[]): void {
    const mgr = this.hierarchyManagersList || [];
    rows.forEach((row: any) => {
      const id = this.normalizeEmpId(row?.empId);
      row.isHierarchy = mgr.some((m: any) => this.normalizeEmpId(m?.managerId) === id);
    });
  }

  private ensureHierarchyManagersLoaded(done: () => void): void {
    if (this.hierarchyManagersLoaded) {
      done();
      return;
    }
    this.employeeService.getAllManagers().pipe(first()).subscribe({
      next: (response: any) => {
        if (response?.serviceStatus === 'Success') {
          this.hierarchyManagersList = response.serviceResponse || [];
        } else {
          this.hierarchyManagersList = [];
        }
        this.hierarchyManagersLoaded = true;
        done();
      },
      error: () => {
        this.hierarchyManagersList = [];
        this.hierarchyManagersLoaded = true;
        done();
      }
    });
  }

  /** Load direct reports for the given anchor (empId + employeementId for API). */
  loadHierarchyForAnchor(anchor: { empId: any; label?: string; employeementId?: any }): void {
    if (!anchor?.empId) return;
    const employee = new Employee();
    employee.empId = anchor.empId;
    let rawId = anchor.employeementId ?? this.currentUser?.employeementId;
    const fromAll = (this.allEmployee || []).find((e: any) => this.normalizeEmpId(e?.empId) === this.normalizeEmpId(anchor.empId));
    if (fromAll?.employeementId != null) {
      rawId = fromAll.employeementId;
    }
    employee.employeementId = rawId;
    this.stripEmployeementIdForHierarchyApi(employee);

    this.hierarchyLoading = true;
    this.hierarchyRows = [];
    this.employeeService.getHierarchyByEmpId(employee).pipe(first()).subscribe({
      next: (response: any) => {
        if (response?.serviceStatus === 'Success') {
          const rawList = this.dedupeHierarchyRawList(response.serviceResponse || []);
          const merged = rawList.map((r: any) => this.mergeHierarchyRowWithPerformance(r));
          const needEnrich = merged.some((r) => this.hierarchyRowNeedsEnrichment(r));
          if (!needEnrich) {
            this.hierarchyRows = merged;
            this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
            this.page = 1;
            this.hierarchyLoading = false;
          } else {
            this.enrichHierarchyRowsInChunks(merged, () => {
              this.hierarchyLoading = false;
            });
          }
        } else {
          this.hierarchyRows = [];
          this.hierarchyLoading = false;
          this.openAlertMod(this.alertTemplate, response?.serviceResponse || 'Could not load hierarchy.');
        }
      },
      error: () => {
        this.hierarchyLoading = false;
        this.hierarchyRows = [];
        this.openAlertMod(this.alertTemplate, 'Could not load hierarchy.');
      }
    });
  }

  /** Turn on hierarchy mode and load direct reports for the current user (HOD). */
  private enableHierarchyViewAndLoad(): void {
    if (!this.userMapping?.performance_action_by_hod || !this.currentUser?.empId) return;
    this.hodActionQueueFilter = false;
    this.viewHierarchyEnabled = true;
    this.hierarchyBreadCrumbs = [{
      empId: this.currentUser.empId,
      label: this.currentUser.name || 'Me',
      employeementId: this.currentUser.employeementId
    }];
    this.ensureHierarchyManagersLoaded(() => this.loadHierarchyForAnchor(this.hierarchyBreadCrumbs[0]));
  }

  onViewHierarchyToggle(enabled: boolean): void {
    if (!this.userMapping?.performance_action_by_hod) {
      this.clearHierarchyView();
      return;
    }
    if (!enabled) {
      this.clearHierarchyView();
      return;
    }
    if (!this.currentUser?.empId) {
      this.clearHierarchyView();
      return;
    }
    this.enableHierarchyViewAndLoad();
  }

  /** Drill into direct reports of the clicked row (name / sitemap). */
  drillPerformanceHierarchy(emp: any, event?: Event): void {
    event?.stopPropagation();
    event?.preventDefault();
    if (!emp?.isHierarchy || !this.viewHierarchyEnabled) return;
    this.hierarchyBreadCrumbs = [
      ...this.hierarchyBreadCrumbs,
      {
        empId: emp.empId,
        label: emp.name || 'Employee',
        employeementId: emp.employeementId
      }
    ];
    this.ensureHierarchyManagersLoaded(() => this.loadHierarchyForAnchor(this.hierarchyBreadCrumbs[this.hierarchyBreadCrumbs.length - 1]));
  }

  onPerformanceHierarchyBreadcrumbClick(index: number): void {
    if (index < 0 || index >= this.hierarchyBreadCrumbs.length) return;
    this.hierarchyBreadCrumbs = this.hierarchyBreadCrumbs.slice(0, index + 1);
    const anchor = this.hierarchyBreadCrumbs[this.hierarchyBreadCrumbs.length - 1];
    this.ensureHierarchyManagersLoaded(() => this.loadHierarchyForAnchor(anchor));
  }

  private clearHierarchyView(): void {
    this.viewHierarchyEnabled = false;
    this.hierarchyRows = [];
    this.hierarchyBreadCrumbs = [];
    this.hierarchyLoading = false;
  }

  /** ColFilter matches `fullSearchText`; set for every row so "All employees" search does not hide rows. */
  private applyFullSearchTextToAllEmployees(): void {
    (this.allEmployee || []).forEach((emp: any) => {
      emp.fullSearchText = [emp.name, emp.employmentIdAcToET, emp.departmentName, emp.designationName]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
    });
  }

  getAllEmployee() {
  this.userDetailsForPerformanceView.empId = this.currentUser.empId;
  this.userDetailsForPerformanceView.hrvalidate = this.userMapping.performance_action_by_hr;
   this.performanceSerive.getAllEmployeesForPerformance(this.userDetailsForPerformanceView).subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployee = response.serviceResponse;
          const mergedData = this.allEmployee.map(emp => {
            const empId = this.normalizeEmpId(emp?.empId);
            const staticData = this.static.find(item => this.normalizeEmpId(item?.empId) === empId);
            return {
                ...emp,
                completionStatus: staticData ? staticData.completionStatus : null,
                finalRating: (staticData && (staticData.finalRating != null && staticData.finalRating !== '')) ? staticData.finalRating : (staticData?.averageRating != null && staticData.averageRating !== '') ? staticData.averageRating : (emp.finalRating != null && emp.finalRating !== '' ? emp.finalRating : (emp.averageRating != null && emp.averageRating !== '' ? emp.averageRating : null)),
                performanceStatusPercentage: (staticData && (staticData.performanceStatusPercentage != null)) ? staticData.performanceStatusPercentage : (emp.performanceStatusPercentage != null ? emp.performanceStatusPercentage : null),
                managerReviewStatus: staticData?.managerReviewStatus ?? emp.managerReviewStatus,
                hodReviewStatus: staticData?.hodReviewStatus ?? emp.hodReviewStatus,
                hrReviewStatus: staticData?.hrReviewStatus ?? emp.hrReviewStatus
            };
        });
          console.log("allEmp", mergedData);

          // Use merged data so "All employees" view shows review status, final rating, approval status
          this.allEmployee = mergedData;

          const currentDate = new Date();
          const oneYearAgo = new Date(currentDate.getFullYear() - 1, 11, 31);

          // this.allEmployee = this.allEmployee.filter(employee => employee.empId !== this.currentUser.empId);
          this.eligibleEmployees = this.allEmployee.filter(employee => {
            // Append employee ID using utility service
            employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);

            // Calculate experience from date_of_joining
            if (employee.dateOfJoining) {
              employee.calculatedExperience = this.calculateExperienceFromDOJ(employee.dateOfJoining);
            }

            const joiningDate = new Date(employee.dateOfJoining);
   return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';

            // if (this.currentUser.employeeRole !== 'HR') {
            //   alert('You are not authorized..!!');
            // }


            return false;
          });
          this.eligibleEmployees.forEach(eligibleEmp => {
            eligibleEmp.emp360 = eligibleEmp.empId;
          });
          this.applyFullSearchTextToAllEmployees();
          this.employeePerformanceListLoaded = true;
          this.mergeStaticDataIntoEmployeeLists();
          this.page = 1;
          if (this.userMapping?.performance_action_by_hod && !this.hierarchyDefaultActivated) {
            this.hierarchyDefaultActivated = true;
            this.enableHierarchyViewAndLoad();
          }
          if (this.viewHierarchyEnabled && this.hierarchyRows?.length) {
            this.hierarchyRows = this.hierarchyRows.map((r: any) => this.mergeHierarchyRowWithPerformance(r));
            this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
          }

        } else {
          console.error(response.serviceResponse);
        }
      }

      )
  }


  toggleSearch() {
    this.sortColumn = null;
    this.sortColumnType = null;
    this.sortDirection = '';
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  /** When true, OK on the alert modal will close and redirect back to the main table. */
  alertRedirectToList = false;

  cancelRequest() {
    const shouldRedirect = this.alertRedirectToList;
    this.alertRedirectToList = false;
    this.modalRef?.close();
    if (shouldRedirect) {
      setTimeout(() => {
        this.isperformanceDsah = true;
        this.isreviewPage = false;
        this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();
      }, 150);
    }
  }


  getAllReviveType() {
    this.performanceSerive.getReviewType().subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allReviewType = response.serviceResponse;
          console.log("allReviewType", this.allReviewType);

        } else {
          console.error(response.serviceResponse);
        }
      });
  }


  async getALLdepartmentByEmployee() {
    if (!this.userMapping?.performance_action_by_hr) {
      return;
    }
    try {


      const response: any = await this.performanceSerive.getALLdepartmentByEmployee(this.userDetailsForPerformanceView).toPromise();

      if (response.serviceStatus === "Success") {






        this.departmentData = response.serviceResponse;

        console.log("departmentData", this.departmentData);
        setTimeout(() => {
          this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
        }, 100);
      } else {
        console.error(response.serviceResponse);
      }
    } catch (error) {
      console.error("Error fetching department data:", error);
    }
  }

  currentUserdepartmentName :string = 'all';
  selectedDepartment: string = 'All';
  departments:any[] =[];
  onDepartmentChange(event: any) {
    const val = event?.target?.value ?? this.currentUserdepartmentName;
    this.selectedDepartment = val;
    if (String(val).toLowerCase() === 'all' || val == null || val === '') {
      this.userDetailsForPerformanceView.deptId = null;
    } else {
      this.userDetailsForPerformanceView.deptId = val;
    }
    this.page = 1;
    this.getALLdepartmentByEmployee();
  }

  selectedQuarter:String = 'All'
  onQuarterChange(event: any){

    this.selectedQuarter = event.target.value;
    // }
    if(this.selectedQuarter === 'all'){
      this.selectedQuarter = null;
    }else{
      this.selectedQuarter = this.selectedQuarter;
    }
    console.log("Check quarter",this.selectedQuarter);

  }

  getAllDepartments(){
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // console.log('response -- ',response.serviceResponse);
        this.departments = response.serviceResponse;
        // console.log('dept response -- ',this.departments);

      }else {
        alert(response.serviceResponse)
      }
    });
  }

  getAllQauterCycle() {
    this.performanceSerive.getAllQuarterCycles().subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allQauterCycle = response.serviceResponse;
          this.EnabledAndActiveQuarterCycle = this.allQauterCycle.filter(quarter => quarter.isEnable && quarter.isActive);
          console.log("allQauterCycle", this.allQauterCycle);

        } else {
          console.error(response.serviceResponse);
        }
      });
  }


  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
    ];

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }

          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }





  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      console.log("queryList : ", emittedArray[0]);
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
            this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
          } else {
            this.storedDataList.push(object);
          }
        }
      });

      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }

        if (query.column == 'Employee Id') {
          query.value = query.value.split("-")[1];
        }
      });

      if (this.filterData.title == 'Filter All Employee') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);


      if (emittedArray[1] == 'Filter All Employee') {
        this.showTable();
      }
    }
  }


  showTable() {

    this.page = 1;
    this.data = '';
    this.filters = {};



    this.getAllEmployee();
  }


  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (!sort.active) return;
    const direction = sort.direction;
    if (!direction) {
      this.sortColumn = null;
      this.sortColumnType = null;
      this.sortDirection = '';
      return;
    }
    const sortParams = sort.active.split('|');
    this.sortColumn = sortParams[0] || null;
    this.sortColumnType = sortParams[1] || null;
    this.sortDirection = direction;
    this.page = 1;
  }

  name = 'EmployeeSheet.xlsx';

  /** Returns the current table list filtered and sorted (same as displayed, all pages). Used for export. */
  getExportList(): any[] {
    let list = this.getTableEmployeeList() || [];
    if (!Array.isArray(list)) return [];
    const filter = this.filters;
    if (filter && Object.keys(filter).length > 0) {
      const filterKeys = Object.keys(filter);
      const escapeRegExp = (s: string) => String(s).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      list = list.filter((item: any) =>
        filterKeys.every((keyName) => {
          const val = filter[keyName];
          return val == null || val === '' || new RegExp(escapeRegExp(String(val)), 'gi').test(String(item[keyName] ?? ''));
        })
      );
    }
    if (this.sortColumn != null && this.sortColumnType != null && this.sortDirection != null) {
      list = new SortPipe().transform([...list], [this.sortColumn, this.sortColumnType, this.sortDirection]);
    }
    return list;
  }

  /**
   * HR bulk accept/reject: selectable when HOD has submitted/approved this cycle (hodReviewStatus Submitted or Accepted)
   * and HR is still pending. Manager approval is not required for this checkbox.
   */
  canBulkHrAction(emp: any): boolean {
    if (!this.userMapping?.performance_action_by_hr) return false;
    if (!emp?.empId) return false;
    if (this.getApprovalStatus(emp, 'hr') !== 'Pending') return false;
    const hod = this.getApprovalStatus(emp, 'hod');
    return hod === 'Submitted' || hod === 'Accepted';
  }

  isBulkSelected(empId: any): boolean {
    return empId != null ? this.selectedBulkEmpIds.has(Number(empId)) : false;
  }

  toggleBulkSelection(emp: any, checked: boolean): void {
    const id = Number(emp?.empId);
    if (!id || !this.canBulkHrAction(emp)) return;
    if (checked) this.selectedBulkEmpIds.add(id);
    else this.selectedBulkEmpIds.delete(id);
  }

  toggleSelectAllBulk(checked: boolean): void {
    const rows = this.getExportList().filter((e: any) => this.canBulkHrAction(e));
    if (checked) {
      this.selectedBulkEmpIds.clear();
      rows.forEach((e: any) => this.selectedBulkEmpIds.add(Number(e.empId)));
    } else {
      this.selectedBulkEmpIds.clear();
    }
  }

  areAllBulkSelected(): boolean {
    const rows = this.getExportList().filter((e: any) => this.canBulkHrAction(e));
    return rows.length > 0 && rows.every((e: any) => this.selectedBulkEmpIds.has(Number(e.empId)));
  }

  clearBulkSelection(): void {
    this.selectedBulkEmpIds.clear();
    this.bulkHrRemark = '';
  }

  submitBulkHrAction(status: 'Accepted' | 'Rejected'): void {
    if (!this.userMapping?.performance_action_by_hr) return;
    if (this.selectedBulkEmpIds.size === 0) {
      this.openAlertMod(this.alertTemplate, 'Please select at least one employee for bulk action.');
      return;
    }
    const remark = (this.bulkHrRemark || '').trim();
    if (!remark) {
      this.openAlertMod(this.alertTemplate, 'Please enter HR remark for bulk action.');
      return;
    }
    const quarterId = this.EnabledAndActiveQuarterCycle?.[0]?.quarterId;
    if (!quarterId) {
      this.openAlertMod(this.alertTemplate, 'No active quarter found for bulk HR action.');
      return;
    }
    this.bulkActionInProgress = true;
    const payload: any = {
      empIds: Array.from(this.selectedBulkEmpIds),
      quarterId: quarterId,
      hrId: this.currentUser?.empId,
      hrReviewStatus: status,
      hrRemark: remark
    };
    this.performanceService.bulkSubmitEmployeePerformanceHR(payload).pipe(first()).subscribe((response: any) => {
      this.bulkActionInProgress = false;
      if (response?.serviceStatus === 'Success') {
        this.clearBulkSelection();
        this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();
        this.openAlertMod(this.alertTemplate, `Bulk ${status.toLowerCase()} completed successfully.`);
      } else {
        this.openAlertMod(this.alertTemplate, response?.serviceResponse || 'Bulk HR action failed.');
      }
    }, () => {
      this.bulkActionInProgress = false;
      this.openAlertMod(this.alertTemplate, 'Something went wrong while processing bulk HR action.');
    });
  }

  exportToExcel(): void {
    const list = this.getExportList();
    const onlySpecificDataArr = list.map((x: any) => ({
      "EmployeeId": (x.isConsultant === 'true' ? 'A-CS-' : 'A-') + (x.employeementId ?? x.employmentIdAcToET ?? ''),
      "Full Name": x.name,
      "EmailId": x.email ?? '',
      "Employment Status": x.employmentstatus ?? '',
      "Department Name": x.departmentName ?? '',
      "Billable Type": x.billableType ?? '',
      "Experience": x.experienceTotalCombined ?? this.getCombinedExperienceYears(x),
      "quarter Cycle": x.quarterycle || 'NULL',
      "financial Year": x.financialYear || 'NULL',
      "Current Status": x.completionStatus ?? 'NULL',
      "hod Name": x.hodName ?? 'NULL',
      "Final Rating": x.finalRating ?? 'NULL',
      "Manger Remark": x.hodRemarks ?? 'NULL',
      "Hod Remarks": x.hrRemarks ?? 'NULL',
      "Hod Review Status": x.hrReviewStatus ?? 'NULL'
    }));
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name);
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.eligibleEmployees = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployee();
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {



          this.allEmployee = response.serviceResponse;

          console.log("allEmp", this.allEmployee);

          const currentDate = new Date();
          const oneYearAgo = new Date();
          oneYearAgo.setFullYear(currentDate.getFullYear() - 1);  // Get the date one year ago

          this.eligibleEmployees = this.allEmployee.filter(employee => {
            // employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);
            const joiningDate = new Date(employee.dateOfJoining);
            return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
          });


          this.eligibleEmployees = this.eligibleEmployees.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId
            ))
          )

          if (this.eligibleEmployees.length == 0) {
            this.openAlertMod(this.alertTemplate, "No Data found")
          }
          this.eligibleEmployees.forEach(employee => {
            if (employee.isConsultant == 'true') {
              employee.employeementId = "A-".concat(employee.employeementId);
            } else {
              employee.employeementId = "A-CS-".concat(employee.employeementId);
            }
            // employee.employeementId = "A-".concat(employee.employeementId);
            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.fullSearchText = [employee.name, employee.employmentIdAcToET, employee.departmentName, employee.designationName].filter(Boolean).join(' ').toLowerCase();
          });
          console.log("allEmployeeList : ", this.eligibleEmployees)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any, redirectOnClose?: boolean) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
    this.alertRedirectToList = redirectOnClose === true;
  }

  searchTableText = '';

  onSearch(searchData) {
    this.filters = searchData;
    this.page = 1;
  }

  onSearchTable() {
    this.filters = { fullSearchText: this.searchTableText || '' };
    this.page = 1;
  }


  /** Compute final rating from criteria: normalize each rating by its max (condition), average, then scale to 0–5 for Overall Rating Summary. */
  calculateFinalRating() {
    let normalizedSum = 0;
    let count = 0;
    if (this.filterCriteria && this.myList) {
      this.myList.forEach((item, i) => {
        const criterion = this.filterCriteria[i];
        const max = criterion && criterion.condition != null ? Number(criterion.condition) : 5;
        if (max > 0 && item.silde !== undefined && item.silde !== null && !isNaN(Number(item.silde))) {
          normalizedSum += Math.min(1, Math.max(0, Number(item.silde) / max));
          count++;
        }
      });
    }
    if (this.filterRatingCriteria && this.myRateList) {
      this.myRateList.forEach((item, i) => {
        const criterion = this.filterRatingCriteria[i];
        const max = criterion && criterion.condition != null ? Number(criterion.condition) : 5;
        if (max > 0 && item.rate !== undefined && item.rate !== null && !isNaN(Number(item.rate))) {
          normalizedSum += Math.min(1, Math.max(0, Number(item.rate) / max));
          count++;
        }
      });
    }
    const avgNormalized = count > 0 ? normalizedSum / count : 0;
    this.finalRating = count > 0 ? Math.round(avgNormalized * 5 * 100) / 100 : null;
  }

  renderPlaceholderChart(chartName: string, chartId: string, departmentData: any) {
    if (!this.userMapping?.performance_action_by_hr) {
      return;
    }
    // Ensure departmentData is available and has the expected structure
    if (!departmentData || departmentData.length === 0) {
      console.error('Department data is empty or undefined.');
      return;
    }

    // Categories for the chart (department names); fix common typos for display
    const departmentNameFix: Record<string, string> = { 'Accountss': 'Accounts', 'Finanace': 'Finance' };
    const categories = departmentData.map((dep: any) => departmentNameFix[dep.department] || dep.department);

    // Rating and Pending Rating Percentages arrays
    let ratingPercentages = [];
    let pendingratingPercentages = [];

    // Handle chart rendering based on chartName ('Rating' or 'Pending')
    if (chartName === 'Rating') {
      // Single brand color for all bars (matches legend and looks consistent)
      const barColor = '#193d8a';
      const barData = departmentData.map((dep: any) => {
        const total = dep.TotalNumberofemp || 0;
        const rated = dep.ratinggivenbymanager ?? 0;
        const pct = total > 0 ? parseFloat(((rated / total) * 100).toFixed(1)) : 0;
        return {
          y: pct,
          color: barColor,
          rated,
          total,
          managerName: dep.managerName || '—'
        };
      });

      this.performanceChartRef = Highcharts.chart(chartId, {
        chart: {
          type: 'column',
          backgroundColor: 'transparent',
          style: { fontFamily: 'inherit' },
          spacing: [20, 16, 72, 16],
          plotBackgroundColor: 'transparent',
          plotBorderWidth: 0,
          plotShadow: false
        },
        title: { text: null },
        credits: { enabled: false },
        exporting: { enabled: false },
        legend: { enabled: false },
        xAxis: {
          categories: categories,
          title: { text: null },
          labels: {
            style: { fontSize: '11px', color: '#475569', fontWeight: '500' },
            autoRotation: [-45, -90],
            reserveSpace: true,
            x: -4,
            y: 14
          },
          lineColor: '#cbd5e1',
          tickColor: '#cbd5e1',
          tickLength: 6
        },
        yAxis: {
          title: {
            text: 'Percentage of Employees Rated',
            style: { fontSize: '12px', color: '#64748b', fontWeight: '600' }
          },
          min: 0,
          max: 100,
          tickInterval: 20,
          labels: {
            format: '{value}%',
            style: { fontSize: '11px', color: '#64748b' }
          },
          gridLineColor: '#e2e8f0',
          gridLineWidth: 1,
          lineColor: '#cbd5e1',
          tickLength: 0
        },
        plotOptions: {
          column: {
            borderRadius: 6,
            borderWidth: 0,
            pointPadding: 0.2,
            groupPadding: 0.25,
            shadow: false,
            dataLabels: {
              enabled: true,
              format: '{point.rated} / {point.total}',
              style: {
                fontSize: '11px',
                fontWeight: '700',
                textOutline: 'none',
                color: '#334155'
              },
              verticalAlign: 'top',
              y: -6
            }
          }
        },
        tooltip: {
          shared: false,
          useHTML: true,
          backgroundColor: '#ffffff',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          borderRadius: 8,
          shadow: true,
          style: { fontSize: '13px' },
          padding: 12,
          formatter: function (this: any) {
            const p = this.point;
            return `<div class="pms-chart-tooltip">
              <strong>${this.x}</strong><br/>
              <span>Rated: <b>${p.rated}</b> employees</span><br/>
              <span>Total: <b>${p.total}</b> employees</span><br/>
              <span>Percentage: <b>${p.y}%</b></span><br/>
              <span>HOD: ${p.managerName}</span>
            </div>`;
          }
        },
        series: [
          {
            name: 'Rated',
            type: 'column',
            data: barData
          }
        ]
      });

    } else {
      // Calculate Pending Rating Percentages
      pendingratingPercentages = departmentData.map(dep => {
        return (dep.pendingratinggivenbymanager / dep.TotalNumberofemp) * 100;
      });

      this.performanceChartRef = Highcharts.chart(chartId, {
        chart: {
          type: 'column'
        },
        title: {
          text: 'Department-wise Bell Curve'
        },
        credits: { enabled: false },
        xAxis: {
          categories: categories,
          title: { text: '' },
          labels: { enabled: false }
        },
        yAxis: {
          title: { text: 'Percentage of Pending' },
          max: 100
        },
        plotOptions: {
          column: {
            borderRadius: 10,
            colorByPoint: true,
            dataLabels: {
              enabled: true,
              format: '{point.category}', // Show department name
              verticalAlign: 'bottom', // Align labels at bottom of column
              y: -10, // Move label just above the column
              style: {
                fontSize: '12px',
                fontWeight: 'bold'
              }
            }
          }
        },
        tooltip: {
          pointFormatter: function () {
            // Access managerName through the departmentData array
            const managerName = departmentData[this.index].managerName;
            return `<b>Pending Percentage: ${this.y}%</b><br><b>HOD: ${managerName}</b>`;
          }
        },
        series: [
          {
            name: 'Pending Percentage',
            type: 'column',
            data: pendingratingPercentages.map((percentage, index) => ({
              y: parseFloat(percentage.toFixed(2)),
              managerName: departmentData[index].managerName // Accessing manager name directly from departmentData
            }))
          },
          {
            name: 'Bell Curve Line',
            type: 'spline',
            data: pendingratingPercentages,
            color: 'black',
            marker: { enabled: false }
          }
        ]
      });
    }
  }

  static:any[] = [];
  getAllEmployeesCurrentStatus(){
    this.performanceService.getAllEmployeesCurrentStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
         this.static = response.serviceResponse;
         this.mergeStaticDataIntoEmployeeLists();
         if (this.viewHierarchyEnabled && this.hierarchyRows?.length) {
           this.hierarchyRows = this.hierarchyRows.map((r: any) => this.mergeHierarchyRowWithPerformance(r));
           this.setHierarchyIsHierarchyFlags(this.hierarchyRows);
         }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /**
   * Merge finalRating, completion status, performanceStatusPercentage, and approval statuses from static (current status)
   * into both eligibleEmployees and allEmployee so counts + table filters stay in sync (important for HR/HOD when static loads after employee list).
   */
  mergeStaticDataIntoEmployeeLists() {
    if (this.static?.length) {
      const applyStaticToEmp = (emp: any) => {
        const empId = this.normalizeEmpId(emp?.empId);
        const staticData = this.static.find((item: any) => this.normalizeEmpId(item?.empId) === empId);
        if (!staticData) return;
        if (staticData.finalRating != null && staticData.finalRating !== '') {
          emp.finalRating = staticData.finalRating;
        } else if (staticData.averageRating != null && staticData.averageRating !== '') {
          emp.finalRating = staticData.averageRating;
        }
        if (staticData.completionStatus != null) {
          emp.completionStatus = staticData.completionStatus;
        }
        if (staticData.performanceStatusPercentage != null) {
          emp.performanceStatusPercentage = staticData.performanceStatusPercentage;
        }
        if (staticData.managerReviewStatus != null) {
          emp.managerReviewStatus = staticData.managerReviewStatus;
        }
        if (staticData.hodReviewStatus != null) {
          emp.hodReviewStatus = staticData.hodReviewStatus;
        }
        if (staticData.hrReviewStatus != null) {
          emp.hrReviewStatus = staticData.hrReviewStatus;
        }
      };

      (this.eligibleEmployees || []).forEach(applyStaticToEmp);
      (this.allEmployee || []).forEach(applyStaticToEmp);
    }
    (this.eligibleEmployees || []).forEach((emp: any) => this.applyExperienceTotalsToEmployee(emp));
    (this.allEmployee || []).forEach((emp: any) => this.applyExperienceTotalsToEmployee(emp));
    // Always schedule (previously we returned early when static was empty, so the HOD popup never ran).
    this.scheduleHodQueueReminder();
  }

  /**
   * Eligible employees in HOD's action queue: manager has progressed review (Ongoing/Submitted) or resubmit (Pending HOD),
   * and HOD still needs to act (hodReviewStatus is Pending — not Submitted/Accepted/Rejected).
   */
  isEmployeeInHodActionQueue(emp: any): boolean {
    const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
    const hod = this.getApprovalStatus(emp, 'hod');
    if (hod === 'Submitted' || hod === 'Accepted' || hod === 'Rejected') return false;
    if (s === 'ongoing' || s === 'submitted' || s === 'pending hod') return true;
    // Align with summary "Pending" when completion is Pending but HOD has not accepted yet (e.g. awaiting HR or in-flight).
    if (s === 'pending') return true;
    return false;
  }

  get hodPendingAcceptCount(): number {
    if (!this.userMapping?.performance_action_by_hod) return 0;
    const list = this.eligibleEmployees || [];
    let n = 0;
    for (const emp of list) {
      if (this.isEmployeeInHodActionQueue(emp)) n++;
    }
    return n;
  }

  /** After static data merges, optionally show one-time HOD reminder modal + refresh banner. */
  private scheduleHodQueueReminder(): void {
    if (!this.userMapping?.performance_action_by_hod || !this.isperformanceDsah) return;
    if (!this.employeePerformanceListLoaded) return;
    if (this.hodQueueAlertModalShown) return;
    const count = this.hodPendingAcceptCount;
    if (count <= 0) return;
    try {
      if (typeof sessionStorage !== 'undefined' && sessionStorage.getItem(UserPerformanceComponent.HOD_QUEUE_ALERT_SESSION_KEY)) {
        return;
      }
    } catch {
      /* ignore */
    }

    let attempts = 0;
    const maxAttempts = 25;
    const tryOpen = (): void => {
      attempts++;
      if (this.hodQueueAlertModalShown) return;
      if (this.hodPendingAcceptCount <= 0) return;
      if (this.hodPendingAlertTemplate) {
        try {
          if (typeof sessionStorage !== 'undefined') {
            sessionStorage.setItem(UserPerformanceComponent.HOD_QUEUE_ALERT_SESSION_KEY, '1');
          }
        } catch {
          /* ignore */
        }
        this.hodQueueAlertModalShown = true;
        this.modalRef = this.modalService.open(this.hodPendingAlertTemplate, {
          centered: true,
          backdrop: 'static',
          keyboard: true,
          modalDialogClass: 'pms-hod-queue-modal-dialog'
        });
        return;
      }
      if (attempts < maxAttempts) {
        setTimeout(tryOpen, 150);
      }
    };
    /** Open only after the window load event (or immediately if already loaded) plus a short delay so the dashboard paints first. */
    const afterPageReady = (fn: () => void): void => {
      if (typeof document !== 'undefined' && document.readyState === 'complete') {
        fn();
      } else if (typeof window !== 'undefined') {
        window.addEventListener('load', fn, { once: true });
      } else {
        fn();
      }
    };
    afterPageReady(() => setTimeout(tryOpen, 550));
  }

  dismissHodQueueAlert(): void {
    this.modalRef?.close();
  }

  /** Toolbar: reopen the HOD queue reminder (does not use sessionStorage). */
  openHodQueueReminderModal(): void {
    if (!this.hodPendingAlertTemplate || this.hodPendingAcceptCount <= 0) return;
    this.modalRef?.close();
    this.modalRef = this.modalService.open(this.hodPendingAlertTemplate, {
      centered: true,
      backdrop: 'static',
      keyboard: true,
      modalDialogClass: 'pms-hod-queue-modal-dialog'
    });
  }

  /** Show eligible employees that are waiting on HOD (same rows as the reminder popup count). */
  applyHodActionQueueTableFilter(scrollDelayMs?: number): void {
    this.clearHierarchyView();
    this.hodActionQueueFilter = true;
    this.tableListMode = 'eligible';
    this.summaryStatusFilter = null;
    this.ratingCategoryFilter = null;
    this.page = 1;
    this.scrollToEmployeeTable(scrollDelayMs ?? 150);
  }

  /** Toolbar "Review list" + modal secondary path: filter table to HOD queue only. */
  goToHodEmployeeList(): void {
    this.applyHodActionQueueTableFilter();
  }

  /** From HOD popup: close modal, then apply filter + scroll after close so the table re-renders. */
  onHodQueueAlertGoToList(): void {
    this.dismissHodQueueAlert();
    setTimeout(() => {
      this.applyHodActionQueueTableFilter(480);
      this.cdr.detectChanges();
    }, 220);
  }

  /**
   * Prior experience from employee record (`total_experience` / totalExperience), in years.
   */
  getPreviousYearExperienceNum(emp: any): number {
    const v = emp?.totalExperience ?? emp?.total_experience;
    if (v == null || v === '') return 0;
    const n = parseFloat(String(v).replace(/,/g, ''));
    return Number.isFinite(n) ? n : 0;
  }

  /**
   * Apmosys tenure: current date − date of joining (same as previous `calculatedExperience` logic).
   */
  getApmosysExperienceNum(emp: any): number {
    if (emp?.calculatedExperience != null && emp.calculatedExperience !== '') {
      const n = Number(emp.calculatedExperience);
      if (Number.isFinite(n)) return n;
    }
    if (emp?.dateOfJoining) {
      return this.calculateExperienceFromDOJ(String(emp.dateOfJoining));
    }
    return 0;
  }

  /** Display value: prior (DB) + Apmosys tenure. */
  getCombinedExperienceYears(emp: any): number {
    const sum = this.getPreviousYearExperienceNum(emp) + this.getApmosysExperienceNum(emp);
    return Math.round(sum * 10) / 10;
  }

  /** Accessible summary (matches bullet lines). */
  getExperienceTooltipText(emp: any): string {
    const p = Math.round(this.getPreviousYearExperienceNum(emp) * 10) / 10;
    const a = Math.round(this.getApmosysExperienceNum(emp) * 10) / 10;
    return `Previous year experience — ${p}. Apmosys Experience — ${a}.`;
  }

  /** Bullets for `app-info-tooltip` (dark popup, pastel dots — same pattern as Team Request → Timesheet). */
  getExperienceTooltipBullets(emp: any): string[] {
    const p = Math.round(this.getPreviousYearExperienceNum(emp) * 10) / 10;
    const a = Math.round(this.getApmosysExperienceNum(emp) * 10) / 10;
    return [`Previous year experience — ${p}`, `Apmosys Experience — ${a}`];
  }

  /**
   * Show the prior-vs-Apmosys breakdown tooltip only for employees with prior experience on the employee record
   * (`total_experience` / totalExperience). Freshers (0 prior) keep the combined number only, no info icon.
   */
  shouldShowExperienceBreakdownTooltip(emp: any): boolean {
    return this.getPreviousYearExperienceNum(emp) > 0;
  }

  /** Sets `experienceTotalCombined` for sorting / export. */
  applyExperienceTotalsToEmployee(emp: any): void {
    if (!emp) return;
    emp.experienceTotalCombined = this.getCombinedExperienceYears(emp);
  }

  /**
   * Calculate experience in years from date of joining to current date
   * Formula: current_date - date_of_joining
   * @param dateOfJoining - Date of joining in string format (YYYY-MM-DD)
   * @returns Experience in years (rounded to 1 decimal place)
   */
  calculateExperienceFromDOJ(dateOfJoining: string): number {
    if (!dateOfJoining) {
      return 0;
    }

    try {
      const doj = new Date(dateOfJoining);
      const today = new Date();
      
      // Calculate difference in milliseconds
      const diff = today.getTime() - doj.getTime();
      
      // Convert to years (considering leap years: 365.25 days per year)
      const experienceInYears = diff / (1000 * 60 * 60 * 24 * 365.25);
      
      // Round to 1 decimal place
      return Number(experienceInYears.toFixed(1));
    } catch (error) {
      console.error('Error calculating experience:', error);
      return 0;
    }
  }

  /** HR chart filter: a specific department is selected (not "All Departments"). */
  private isHrDepartmentFilterActive(): boolean {
    if (!this.userMapping?.performance_action_by_hr) return false;
    const v = this.currentUserdepartmentName;
    return v != null && String(v).trim() !== '' && String(v).toLowerCase() !== 'all';
  }

  /** True when employee belongs to the department selected in the HR Performance Analysis filter. */
  private employeeMatchesHrDepartmentFilter(emp: any): boolean {
    if (!this.isHrDepartmentFilterActive()) return true;
    const selected = String(this.currentUserdepartmentName).trim();
    const eid = emp?.departmentId ?? emp?.deptId;
    if (eid != null && String(eid) === selected) return true;
    const dept = (this.departments || []).find(d => String(d.deptId) === selected);
    if (dept?.name && emp?.departmentName) {
      return String(emp.departmentName).trim().toLowerCase() === String(dept.name).trim().toLowerCase();
    }
    return false;
  }

  /** All employees scoped to HR department filter (full list when filter is "all" or user is not HR). */
  getHrScopedAllEmployees(): any[] {
    const list = this.allEmployee || [];
    if (!this.isHrDepartmentFilterActive()) return list;
    return list.filter(e => this.employeeMatchesHrDepartmentFilter(e));
  }

  /** Eligible employees scoped to HR department filter. */
  getHrScopedEligibleEmployees(): any[] {
    const list = this.eligibleEmployees || [];
    if (!this.isHrDepartmentFilterActive()) return list;
    return list.filter(e => this.employeeMatchesHrDepartmentFilter(e));
  }

  /** Cycle card counts (same scope as HR chart when HR selects a department). */
  get hrScopedAllCount(): number {
    return this.getHrScopedAllEmployees().length;
  }
  get hrScopedEligibleCount(): number {
    return this.getHrScopedEligibleEmployees().length;
  }
  get hrScopedNotEligibleCount(): number {
    return Math.max(0, this.hrScopedAllCount - this.hrScopedEligibleCount);
  }

  /** Performance summary counts for eligible employees (for CYCLE ASSIGNED card). */
  get performanceSummary(): {
    notStarted: number;
    ongoing: number;
    pendingHod: number;
    completed: number;
    rejected: number;
    rated: number;
    avgRating: number | null;
  } {
    const list = this.getHrScopedEligibleEmployees();
    let notStarted = 0, ongoing = 0, pendingHod = 0, completed = 0, rejected = 0, rated = 0;
    let ratingSum = 0;
    list.forEach(emp => {
      const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
      if (s === 'not started') notStarted++;
      else if (s === 'ongoing' || s === 'submitted') ongoing++;
      else if (s === 'pending hod' || s === 'pending') pendingHod++;
      else if (s === 'completed') completed++;
      else if (s === 'rejected') rejected++;
      else notStarted++;
      const r = this.getEmployeeFinalRatingValue(emp);
      if (r != null && !isNaN(r)) {
        rated++;
        ratingSum += r <= 5 ? r : (r / 10) * 5;
      }
    });
    const avgRating = rated > 0 ? Math.round((ratingSum / rated) * 100) / 100 : null;
    return { notStarted, ongoing, pendingHod, completed, rejected, rated, avgRating };
  }

  /** Rating category bands (0–5 scale): NI, M-, M, M+, E. */
  private static readonly RATING_CATEGORIES: { max: number; label: string }[] = [
    { max: 1.5, label: 'NI' },
    { max: 2.4, label: 'M-' },
    { max: 3.4, label: 'M' },
    { max: 4.4, label: 'M+' },
    { max: 5, label: 'E' }
  ];
  private static readonly RATING_CATEGORY_RANGES: { label: typeof UserPerformanceComponent.RATING_LABELS[number]; range: string }[] = [
    { label: 'NI', range: '0 - 1.5' },
    { label: 'M-', range: '1.6 - 2.4' },
    { label: 'M', range: '2.5 - 3.4' },
    { label: 'M+', range: '3.5 - 4.4' },
    { label: 'E', range: '4.5 - 5' }
  ];

  /** Maps numeric rating (0–5) to category: NI (0–1.5), M- (1.6–2.4), M (2.5–3.4), M+ (3.5–4.4), E (4.5–5). */
  getRatingCategory(rating: number | null | undefined): string {
    if (rating == null || isNaN(Number(rating))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(rating)));
    for (const band of UserPerformanceComponent.RATING_CATEGORIES) {
      if (n <= band.max) return band.label;
    }
    return 'E';
  }

  /** Resolves final rating value from employee (finalRating or averageRating), normalized to 0–5. */
  private getEmployeeFinalRatingValue(emp: any): number | null {
    const r = emp?.finalRating ?? emp?.averageRating ?? emp?.final_rating ?? emp?.average_rating;
    if (r == null || r === '' || isNaN(Number(r))) return null;
    const num = Number(r);
    return num <= 5 ? num : (num / 10) * 5;
  }
  /** Returns 0-5 filled stars from employee final rating. Supports scale 5 or 10 (if > 5 treated as out of 10). Table UI. */
  getFinalRatingStars(emp: any): number {
    const num = this.getEmployeeFinalRatingValue(emp);
    if (num == null) return 0;
    return Math.min(5, Math.max(0, Math.round(num)));
  }
  /** Returns label for final rating column: category (e.g. "M+") with optional numeric "3.2 (M+)" or "N/A". */
  getFinalRatingLabel(emp: any): string {
    const num = this.getEmployeeFinalRatingValue(emp);
    if (num == null) return 'N/A';
    const category = this.getRatingCategory(num);
    const formatted = (Math.round(num * 10) / 10).toFixed(1);
    return `${formatted} (${category})`;
  }
  /** Returns 0-5 filled stars from averageRating (card). */
  getAverageRatingStars(): number {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return 0;
    return Math.min(5, Math.max(0, Math.round(Number(r))));
  }

  /** Per-star display for profile card (supports half stars, e.g. 2.5). Index 1–5. */
  getAverageStarDisplayForIndex(index: number): 'full' | 'half' | 'empty' {
    const r = this.getAverageRatingNumeric();
    if (r == null || index < 1 || index > 5) return 'empty';
    if (r >= index) return 'full';
    if (r >= index - 0.5) return 'half';
    return 'empty';
  }
  /** Returns average rating label for card: "X.X / 5.0" or "N/A". */
  getAverageRatingLabel(): string {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(r)));
    return (Math.round(n * 10) / 10).toFixed(1) + ' / 5.0';
  }

  /** Profile card: `employee.mobile_no` / mobileNo from DB. */
  getProfileMobileNo(emp: any): string {
    if (!emp) return '-';
    const v = emp.mobileNo ?? emp.mobile_no;
    if (v === null || v === undefined || v === '') return '-';
    return String(v);
  }

  /** Profile card: `employee.work_location` / workLocation from DB. */
  getProfileWorkLocation(emp: any): string {
    if (!emp) return '-';
    const v = emp.workLocation ?? emp.work_location;
    const s = v != null ? String(v).trim() : '';
    return s !== '' ? s : '-';
  }
  /** Numeric average rating (same source as card) for Overall Rating Summary. */
  getAverageRatingNumeric(): number | null {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return null;
    return Math.min(5, Math.max(0, Number(r)));
  }
  /** Performance score % from averageRating (same source as card). For Overall Rating Summary table. */
  getAverageRatingScorePercent(): number {
    const n = this.getAverageRatingNumeric();
    if (n == null) return 0;
    return Math.min(100, Math.round((n / 5) * 1000) / 10);
  }
  /** Performance category from averageRating: NI / M- / M / M+ / E. */
  getAverageRatingCategory(): string {
    return this.getRatingCategory(this.getAverageRatingNumeric());
  }

  /** Dashboard summary cards: count employees in each rating category (NI, M-, M, M+, E). */
  getRatingCategoryCounts(): { label: typeof UserPerformanceComponent.RATING_LABELS[number]; range: string; count: number }[] {
    const counts: Record<string, number> = { 'NI': 0, 'M-': 0, 'M': 0, 'M+': 0, 'E': 0 };
    (this.getHrScopedAllEmployees() || []).forEach((emp: any) => {
      const r = this.getEmployeeFinalRatingValue(emp);
      if (r == null) return;
      const category = this.getRatingCategory(r);
      if (counts[category] != null) counts[category] += 1;
    });
    return UserPerformanceComponent.RATING_CATEGORY_RANGES.map((item) => ({
      label: item.label,
      range: item.range,
      count: counts[item.label] || 0
    }));
  }
  /** Formatted average rating number only e.g. "2.0" (same source as card). For Overall Rating Summary Final Rating value. */
  getAverageRatingFormatted(): string {
    const n = this.getAverageRatingNumeric();
    if (n == null) return '0';
    return (Math.round(n * 10) / 10).toFixed(1);
  }
  /** Returns approval status for Manager/HOD/HR. Uses emp.managerReviewStatus, emp.hodReviewStatus, emp.hrReviewStatus or 'Pending'. */
  getApprovalStatus(emp: any, role: 'manager' | 'hod' | 'hr'): string {
    const key = role === 'manager' ? 'managerReviewStatus' : role === 'hod' ? 'hodReviewStatus' : 'hrReviewStatus';
    const s = emp?.[key];
    return s && (s === 'Submitted' || s === 'Accepted' || s === 'Rejected') ? s : 'Pending';
  }

  /**
   * Display-friendly review status for table.
   * After HOD submission, backend may return "Submitted"; show as "Pending".
   * "Completed" is shown only when HR has accepted (not from completionStatus alone).
   */
  getReviewStatusLabel(emp: any): string {
    const raw = (emp?.completionStatus || 'Not Started').toString().trim();
    const lower = raw.toLowerCase();
    const hr = this.getApprovalStatus(emp, 'hr');

    if (lower === 'rejected') return 'Rejected';

    if (lower === 'completed') {
      if (hr === 'Accepted') return 'Completed';
      if (hr === 'Rejected') return 'Rejected';
      return 'Pending';
    }

    if (lower === 'submitted' || lower === 'pending') return 'Pending';

    return raw;
  }

  /** CSS modifier for review status badge in table (segregated colors/icons). */
  getReviewStatusBadgeModifier(emp: any): string {
    const label = (this.getReviewStatusLabel(emp) || '').toLowerCase();
    if (label === 'rejected') return 'pms-badge-review-rejected';
    if (label === 'completed') return 'pms-badge-review-completed';
    if (label === 'pending') return 'pms-badge-review-pending';
    if (label === 'ongoing') return 'pms-badge-review-ongoing';
    if (label === 'not started') return 'pms-badge-review-not-started';
    return 'pms-badge-review-default';
  }

  /** Font Awesome icon classes for review status badge (includes base; use with [class] on <i>). */
  getReviewStatusIconClass(emp: any): string {
    const label = (this.getReviewStatusLabel(emp) || '').toLowerCase();
    const base = 'pms-badge-review-ico fas';
    if (label === 'rejected') return `${base} fa-times-circle`;
    if (label === 'completed') return `${base} fa-check-circle`;
    if (label === 'pending') return `${base} fa-clock`;
    if (label === 'ongoing') return `${base} fa-spinner fa-spin`;
    if (label === 'not started') return `${base} fa-circle`;
    return `${base} fa-minus-circle`;
  }

  /** Whether the approval icon is clickable (Submitted or Accepted). */
  canOpenApprovalDetails(emp: any, role: 'manager' | 'hod' | 'hr'): boolean {
    const status = this.getApprovalStatus(emp, role);
    return status === 'Submitted' || status === 'Accepted';
  }

  /** Open approval details popup for the given role (manager/hod/hr). Fetches details from API. */
  openApprovalDetails(emp: any, role: 'manager' | 'hod' | 'hr'): void {
    if (!this.canOpenApprovalDetails(emp, role)) return;
    const quarterId = this.EnabledAndActiveQuarterCycle?.[0]?.quarterId;
    if (!quarterId || !emp?.empId) return;
    const scrollY = window.scrollY || document.documentElement.scrollTop;
    this.approvalDetailsLoading = true;
    this.approvalDetailsPopup = {
      role: role === 'manager' ? 'Manager' : role === 'hod' ? 'HOD' : 'HR',
      name: '',
      id: '',
      employmentId: '',
      rating: '',
      feedback: '',
      history: [],
      yearWiseRatings: [],
      employeeName: emp?.name || '—',
      employeeEmploymentId: emp?.employmentIdAcToET || emp?.employeementId || '—'
    };
    this.approvalDetailsModalRef = this.modalService.open(this.approvalDetailsModalTemplate, {
      size: 'md',
      centered: true,
      windowClass: 'pms-approval-modal-window'
    });
    if (this.approvalDetailsModalRef.shown) {
      this.approvalDetailsModalRef.shown.subscribe(() => window.scrollTo(0, scrollY));
    } else {
      setTimeout(() => window.scrollTo(0, scrollY), 100);
    }
    this.performanceService.getApprovalDetails(emp.empId, quarterId).pipe(first()).subscribe((response: any) => {
      this.approvalDetailsLoading = false;
      if (response?.serviceStatus === 'Success' && response?.serviceResponse) {
        const data = response.serviceResponse[role];
        if (data) {
          this.approvalDetailsPopup = {
            role: role === 'manager' ? 'Manager' : role === 'hod' ? 'HOD' : 'HR',
            name: data.name || '—',
            id: data.id != null ? data.id : '—',
            employmentId: data.employmentId || '—',
            rating: data.rating != null && data.rating !== '' ? data.rating : '—',
            feedback: data.feedback || '—',
            history: Array.isArray(data.history) ? data.history : [],
            yearWiseRatings: Array.isArray(response.serviceResponse?.yearWiseRatings) ? response.serviceResponse.yearWiseRatings : [],
            employeeName: emp?.name || '—',
            employeeEmploymentId: emp?.employmentIdAcToET || emp?.employeementId || '—'
          };
        }
      }
    }, () => { this.approvalDetailsLoading = false; });
  }

  closeApprovalDetailsModal(): void {
    this.approvalDetailsModalRef?.close();
    this.approvalDetailsPopup = null;
  }

  /** Status filter config for summary boxes and table filter. */
  private readonly summaryStatusConfig: Record<string, { title: string; matchStatus: string[] }> = {
    notStarted: { title: 'Not Started', matchStatus: ['not started'] },
    pendingHod: { title: 'Pending', matchStatus: ['pending hod', 'pending'] },
    ongoing: { title: 'Ongoing', matchStatus: ['ongoing', 'submitted'] },
    completed: { title: 'Completed', matchStatus: ['completed'] },
    rejected: { title: 'Rejected', matchStatus: ['rejected'] }
  };

  /** Returns the list to display in the main table (by cycle filter and/or summary status). */
  getTableEmployeeList(): any[] {
    if (this.viewHierarchyEnabled) {
      let list = this.hierarchyRows || [];
      if (this.ratingCategoryFilter) {
        list = list.filter((emp: any) =>
          this.getRatingCategory(this.getEmployeeFinalRatingValue(emp)) === this.ratingCategoryFilter);
      }
      if (this.isHrDepartmentFilterActive()) {
        list = list.filter((emp: any) => this.employeeMatchesHrDepartmentFilter(emp));
      }
      return list;
    }
    let list: any[];
    if (this.tableListMode === 'all') {
      list = this.allEmployee || [];
    } else if (this.tableListMode === 'notEligible') {
      list = this.getNotEligibleEmployees();
    } else {
      list = this.eligibleEmployees || [];
      if (this.hodActionQueueFilter && this.userMapping?.performance_action_by_hod) {
        list = list.filter((emp: any) => this.isEmployeeInHodActionQueue(emp));
      } else if (this.summaryStatusFilter) {
        const matchStatus = this.summaryStatusConfig[this.summaryStatusFilter]?.matchStatus || [];
        list = list.filter(emp => {
          const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
          return matchStatus.includes(s);
        });
      }
    }
    if (this.ratingCategoryFilter) {
      list = (list || []).filter((emp: any) => this.getRatingCategory(this.getEmployeeFinalRatingValue(emp)) === this.ratingCategoryFilter);
    }
    if (this.isHrDepartmentFilterActive()) {
      list = (list || []).filter((emp: any) => this.employeeMatchesHrDepartmentFilter(emp));
    }
    return list;
  }

  /** Employees in allEmployee who are not in eligibleEmployees (by empId). */
  getNotEligibleEmployees(): any[] {
    const eligibleIds = new Set((this.getHrScopedEligibleEmployees() || []).map((e: any) => this.normalizeEmpId(e?.empId)));
    return (this.getHrScopedAllEmployees() || []).filter((a: any) => !eligibleIds.has(this.normalizeEmpId(a?.empId)));
  }

  /** Scroll the page to the Employee list table. */
  scrollToEmployeeTable(delayMs = 150): void {
    setTimeout(() => {
      const el = document.getElementById('employeeListTableSection');
      el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, delayMs);
  }

  /** Apply cycle filter: All / Eligible / Not eligible, then scroll to table. */
  applyCycleFilter(mode: 'all' | 'eligible' | 'notEligible'): void {
    this.clearHierarchyView();
    this.tableListMode = mode;
    this.summaryStatusFilter = null;
    this.ratingCategoryFilter = null;
    this.hodActionQueueFilter = false;
    this.page = 1;
    this.scrollToEmployeeTable();
  }

  /** Apply performance summary filter: show only employees in this status, then scroll to table. */
  applySummaryStatusFilter(statusKey: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected'): void {
    this.clearHierarchyView();
    this.tableListMode = 'eligible';
    this.summaryStatusFilter = statusKey;
    this.ratingCategoryFilter = null;
    this.hodActionQueueFilter = false;
    this.page = 1;
    this.scrollToEmployeeTable();
  }

  /** Clear table filter and show all employees (default view). */
  clearSummaryStatusFilter(): void {
    this.clearHierarchyView();
    this.summaryStatusFilter = null;
    this.ratingCategoryFilter = null;
    this.hodActionQueueFilter = false;
    this.tableListMode = 'all';
    this.page = 1;
  }

  /** Apply rating-category filter from counts (NI, M-, M, M+, E) and scroll to table. */
  applyRatingCategoryFilter(category: 'NI' | 'M-' | 'M' | 'M+' | 'E', event?: Event): void {
    event?.stopPropagation();
    event?.preventDefault();
    this.ratingCategoryFilter = category;
    this.summaryStatusFilter = null;
    this.hodActionQueueFilter = false;
    this.tableListMode = 'all';
    this.page = 1;
    this.scrollToEmployeeTable();
  }

  /** Performance instructions (non-HR): carousel navigation */
  goToInstructionSlide(index: number): void {
    if (index >= 0 && index < this.instructionSlideCount) {
      this.instructionSlideIndex = index;
    }
  }

  nextInstructionSlide(): void {
    if (this.instructionSlideIndex < this.instructionSlideCount - 1) {
      this.instructionSlideIndex++;
    }
  }

  prevInstructionSlide(): void {
    if (this.instructionSlideIndex > 0) {
      this.instructionSlideIndex--;
    }
  }

  /** Whether the table has an active filter (cycle or status) to show the "Showing..." bar. */
  hasTableFilter(): boolean {
    return (this.userMapping?.performance_action_by_hod && this.viewHierarchyEnabled) || this.summaryStatusFilter != null || this.ratingCategoryFilter != null || this.tableListMode !== 'all' || this.hodActionQueueFilter;
  }

  /** Label for current filter for the "Showing: ..." bar. */
  getTableFilterLabel(): string {
    if (this.userMapping?.performance_action_by_hod && this.viewHierarchyEnabled) return 'Hierarchy view';
    if (this.ratingCategoryFilter) return `Rating category: ${this.ratingCategoryFilter}`;
    if (this.hodActionQueueFilter) return 'Your HOD queue (manager submitted — pending your approval)';
    if (this.summaryStatusFilter) return this.summaryStatusConfig[this.summaryStatusFilter]?.title || '';
    if (this.tableListMode === 'eligible') return 'Eligible employees';
    if (this.tableListMode === 'notEligible') return 'Not eligible';
    return '';
  }

  /** True if this employee is in the eligible list for the current cycle (can be evaluated and has rating). */
  isEligibleEmployee(emp: any): boolean {
    if (!emp?.empId || !this.eligibleEmployees?.length) return false;
    const id = this.normalizeEmpId(emp.empId);
    return this.eligibleEmployees.some((e: any) => this.normalizeEmpId(e?.empId) === id);
  }

  /** Label for current summary filter (e.g. "Not Started") for the "Showing: ..." bar. */
  getSummaryStatusFilterLabel(): string {
    return this.summaryStatusFilter ? this.summaryStatusConfig[this.summaryStatusFilter]?.title || '' : '';
  }

  /** Opens popup listing employees for the given performance summary status (kept for any other use). */
  openStatusDetailPopup(statusKey: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected'): void {
    const config = this.summaryStatusConfig;
    const { title, matchStatus } = config[statusKey];
    const list = this.eligibleEmployees || [];
    this.summaryStatusEmployees = list.filter(emp => {
      const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
      return matchStatus.includes(s);
    });
    this.summaryStatusTitle = title + ' - Employees';
    this.summaryStatusModalRef = this.modalService.open(this.statusDetailModalTemplate, {
      size: 'xl',
      scrollable: true,
      windowClass: 'pms-status-detail-modal-window'
    });
  }

  closeStatusDetailPopup(): void {
    this.summaryStatusModalRef?.close();
    this.summaryStatusEmployees = [];
    this.summaryStatusTitle = '';
  }

  /** Numeric rating (0–5) for approval details popup star display. */
  getApprovalDetailRatingNum(): number {
    const r = this.approvalDetailsPopup?.rating;
    if (r == null || r === '' || r === '—') return 0;
    const n = Number(r);
    return isNaN(n) ? 0 : Math.min(5, Math.max(0, n));
  }

  /** Year-wise summary based on actual action/submission date (not quarter financial year). */
  getApprovalYearWiseSummary(): any[] {
    const history = this.approvalDetailsPopup?.history || [];
    if (!Array.isArray(history) || history.length === 0) return [];
    const map = new Map<string, { ratings: number[]; managers: Set<string>; records: number }>();
    history.forEach((h: any) => {
      if (!h?.actionDate) return;
      const dt = new Date(h.actionDate);
      if (isNaN(dt.getTime())) return;
      const year = String(dt.getFullYear());
      const rec = map.get(year) || { ratings: [], managers: new Set<string>(), records: 0 };
      rec.records += 1;
      const r = h?.rating;
      if (r != null && r !== '' && !isNaN(Number(r))) {
        const n = Number(r);
        rec.ratings.push(n <= 5 ? n : (n / 10) * 5);
      }
      if (h?.managerName) rec.managers.add(h.managerName);
      map.set(year, rec);
    });
    return Array.from(map.entries())
      .map(([year, rec]) => {
        const avg = rec.ratings.length ? rec.ratings.reduce((a, b) => a + b, 0) / rec.ratings.length : null;
        return {
          year,
          averageRating: avg != null ? (Math.round(avg * 100) / 100).toFixed(2) : '—',
          records: rec.records,
          managerNames: Array.from(rec.managers)
        };
      })
      .sort((a, b) => Number(b.year) - Number(a.year));
  }

  /** Detailed year-wise rows using actual action/submission date year. */
  getApprovalYearWiseDetailedRows(): any[] {
    const history = this.approvalDetailsPopup?.history || [];
    if (!Array.isArray(history) || history.length === 0) return [];
    const targetEmployeeName = this.approvalDetailsPopup?.employeeName || '—';
    const targetEmployeeId = this.approvalDetailsPopup?.employeeEmploymentId || '—';
    return history
      .filter((h: any) => h?.actionDate)
      .map((h: any) => {
        const dt = new Date(h.actionDate);
        return {
          year: !isNaN(dt.getTime()) ? dt.getFullYear() : '—',
          actionDate: h.actionDate,
          status: h.status || 'Pending',
          financialYear: h.financialYear || '—',
          quarterCycle: h.quarterCycle || '—',
          raterName: h.name || '—',
          employeeName: targetEmployeeName,
          employeeId: targetEmployeeId,
          rating: h.rating || '—',
          feedback: h.feedback || '—'
        };
      })
      .sort((a: any, b: any) => new Date(b.actionDate).getTime() - new Date(a.actionDate).getTime());
  }

  /** Feedback section title by role: Manager Feedback | HOD Feedback | HR remark/feedback */
  getFeedbackSectionTitle(): string {
    if (this.userMapping?.performance_action_by_hr) return 'HR remark/feedback';
    if (this.userMapping?.performance_action_by_hod) return 'HOD Feedback';
    return 'Manager Feedback';
  }

  /** Feedback section label by role for the remarks field */
  getFeedbackSectionLabel(): string {
    if (this.userMapping?.performance_action_by_hr) return 'HR Remarks';
    if (this.userMapping?.performance_action_by_hod) return 'HOD Remarks';
    return 'Manager/Reporting Manager Remarks';
  }

  /** Disabled state for feedback textarea: HOD can always edit; Manager when Ongoing/Completed; HR when not in edit mode */
  getFeedbackTextareaDisabled(): boolean {
    if (this.userMapping?.performance_action_by_hr) return !this.isEditMode;
    if (this.userMapping?.performance_action_by_hod) return false;
    return this.currentStatus === 'Ongoing' || this.currentStatus === 'Completed' || this.currentStatus === 'Pending';
  }

  onReview(eligiemployee: any) {
    this.isperformanceDsah = false;
    this.isreviewPage = true;
    this.selectedEmployee = eligiemployee;
    console.log("eligiemployee", eligiemployee);
    this.selectedEmployee.emp360 = eligiemployee.emp360;
    console.log("eligiemployee.emp360", eligiemployee.emp360);
    this.myList = [];
    this.myRateList = [];
    this.getCountOfRewardsAndAppreciation();
    if (this.EnabledAndActiveQuarterCycle?.length) {
      this.setQuartedId(this.EnabledAndActiveQuarterCycle[0].quarterId);
    }
  }

  back() {
    this.getAllEmployee();
    this.getAllEmployeesCurrentStatus();
    this.isperformanceDsah = true;
    this.isreviewPage = false;
    if (this.userMapping?.performance_action_by_hr) {
      setTimeout(() => {
        this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
      }, 100);
    }
  }

  toggleData(event) {
    if (!this.userMapping?.performance_action_by_hr) {
      return;
    }
    if (event.target.checked) {
      this.renderPlaceholderChart("Pending", "performanceId", this.departmentData);
    } else {
      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }
  }

  /** Maps UI `comment` to API `criteriaRemark` (employee_rating_performance.criteria_remark). */
  private trimCriteriaComment(value: any): string {
    if (value == null) return '';
    return String(value).trim();
  }

  /** Each criterion row in Final Review must have a non-empty comment before submit/update. */
  private getFinalReviewCriteriaCommentError(): string | null {
    for (let i = 0; i < this.filterCriteria.length; i++) {
      const label = (this.filterCriteria[i]?.reviewLabel || `Criterion ${i + 1}`).toString();
      const c = (this.myList[i]?.comment ?? '').toString().trim();
      if (!c) {
        return `Please enter a comment for "${label}".`;
      }
    }
    for (let i = 0; i < this.filterRatingCriteria.length; i++) {
      const label = (this.filterRatingCriteria[i]?.reviewLabel || `Criterion ${i + 1}`).toString();
      const c = (this.myRateList[i]?.comment ?? '').toString().trim();
      if (!c) {
        return `Please enter a comment for "${label}".`;
      }
    }
    return null;
  }

  submitReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    const commentErr = this.getFinalReviewCriteriaCommentError();
    if (commentErr) {
      this.alertMessage = commentErr;
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.currentStatus = this.currentStatus;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
    if(this.userMapping.performance_action_by_hod){
      this.submitPerformance.actionBy = 'HOD'
    }
    else if (this.userMapping.performance_action_by_approvals_tos) {
      this.submitPerformance.actionBy = 'RM';
    }
    this.submitPerformance.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: null,
          criteriaRemark: this.trimCriteriaComment(this.myList[index].comment)
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: null,
          criteriaRemark: this.trimCriteriaComment(this.myRateList[index].comment)
        });
      }
    });
    this.submitPerformance.finalRating = this.finalRating;
    this.submitPerformance.hodRemarks = this.hodRemarks;
    if (!this.validationService.validateNullUndefinedEmptyString(this.submitPerformance.hodRemarks)) {
      this.alertMessage = "Please justify your rating by providing remarks!";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    console.log(this.submitPerformance, "performance");
    this.performanceService.submitEmployeePerformanceHOD(this.submitPerformance).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }

  getRatingList(limit: number): number[] {
    return Array.from({ length: limit }, (_, i) => i + 1);
  }

  isClicked = false;
  enableDisableSubmit: boolean = false;
  performnace1: any = new Performance();

  /** Selected quarter object for the current quarterId (used when details are shown directly). */
  getSelectedQuarter(): any {
    if (!this.EnabledAndActiveQuarterCycle?.length || this.quarterId == null) return null;
    return this.EnabledAndActiveQuarterCycle.find((q: any) => q.quarterId === this.quarterId)
      || this.EnabledAndActiveQuarterCycle[0];
  }
  getSelectedQuarterIndex(): number {
    const q = this.getSelectedQuarter();
    if (!q) return 0;
    const i = this.EnabledAndActiveQuarterCycle.indexOf(q);
    return i >= 0 ? i : 0;
  }
  setQuartedId(quartId: any) {
    this.quarterId = quartId;
    this.myList = [];
    this.myRateList = [];
    this.submitPerformance.finalRating = '';
    this.hodRemarks = '';
    this.isClicked = !this.isClicked;
    this.isAcceptSelected = false;
    this.isRejectSelected = false;
    this.currentStatus = '';
    this.performnace.empId = this.selectedEmployee.empId;
    this.performnace.quarterId = quartId;
    this.HrAndHodView(this.performnace);
  }

  currentStatus: any;
  rejectStatus: any;

  /**
   * Applies shared header fields (remarks, HR status, final rating) from one row. Manager submits to
   * `managerRemarks` in DB; HOD uses `hodRemarks`. Loading only `hodRemarks` hid manager remarks after reopen.
   */
  private applyPerformanceReviewHeaderFromFirstRow(rows: any[]): void {
    if (!rows?.length) return;
    const row = rows[0];
    this.finalRating = row.finalRating;
    this.hrRemarks = row.hrRemark != null ? row.hrRemark : '';
    this.hrReviewStatus = row.hrReviewStatus;
    this.acceptReason = row.hrRemark;
    this.rejectStatus = row.rejectStatus;
    if (this.userMapping?.performance_action_by_hr) {
      return;
    }
    if (this.userMapping?.performance_action_by_hod) {
      this.hodRemarks = row.hodRemarks != null ? String(row.hodRemarks) : '';
      return;
    }
    const mgr = row.managerRemarks != null ? String(row.managerRemarks).trim() : '';
    const hod = row.hodRemarks != null ? String(row.hodRemarks).trim() : '';
    this.hodRemarks = mgr || hod || '';
  }

  HrAndHodView(performance: any) {
    this.performanceSerive.hrAndHodEmpoyeePerformanceView(performance).pipe(first()).subscribe((response: any) => {
      this.enableDisableSubmit = false;
      if (response.serviceStatus == "Success") {
        console.log('inside if block');
        this.performnace1 = response.serviceResponse;
        console.log("given by hod", this.performnace1);
        this.currentStatus = this.performnace1[0].completionStatus;
        this.enableDisableSubmit = !this.enableDisableSubmit;
        this.filterRatingCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Rating' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Slider' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: value.ratingValue, performanceRatingId: value.performanceRatingId, comment: (value != null && value.criteriaRemark != null && value.criteriaRemark !== '') ? value.criteriaRemark : '' });
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: value.ratingValue, performanceRatingId: value.performanceRatingId, comment: (value != null && value.criteriaRemark != null && value.criteriaRemark !== '') ? value.criteriaRemark : '' });
        });
        const headerRows = this.filterCriteria.length > 0 ? this.filterCriteria : this.filterRatingCriteria;
        this.applyPerformanceReviewHeaderFromFirstRow(headerRows);
        this.calculateFinalRating();
      } else {
        console.log('inside else part');
        this.currentStatus = 'Not Started';
        this.enableDisableSubmit = false;
        this.filterCriteriaQuarter = this.allReviewType.filter(item => item.quarterId === this.quarterId);
        this.filterCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Slider');
        this.filterRatingCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Rating');
        console.log('==================filter',this.filterRatingCriteria);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: 0, performanceRatingId: null, comment: '' });
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: 0, performanceRatingId: null, comment: '' });
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.calculateFinalRating();
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Ongoing':
        return '#FF6C37';
      case 'Completed':
        return '#04724D';
      case 'Rejected':
        return '#931621';
      case 'Pending HOD':
        return '#E67E22';
      case 'Pending':
        return '#d97706';
      default:
        return '#A8A8A8';
    }
  }
  /** Performance score as percentage (0-100) from finalRating out of 5. For Overall Rating Summary. */
  getPerformanceScorePercent(): number {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 0;
    return Math.min(100, Math.round((Number(r) / 5) * 1000) / 10);
  }
  /** Performance category from finalRating (criteria-based): NI / M- / M / M+ / E. */
  getPerformanceCategory(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 'N/A';
    return this.getRatingCategory(Number(r));
  }
  /** Average rating label for Overall Rating Summary from criteria (finalRating on 0–5 scale): "X.X / 5.0" or "N/A". */
  getCriteriaSummaryLabel(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(r)));
    return (Math.round(n * 10) / 10).toFixed(1) + ' / 5.0';
  }
  /** Rating label for a row e.g. "4.0/5.0". */
  getCriteriaRatingLabel(rate: number, maxStars: number): string {
    if (rate == null || isNaN(Number(rate))) return '0/' + (maxStars || 5);
    const n = Math.min(maxStars || 5, Math.max(0, Number(rate)));
    return (Math.round(n * 10) / 10).toFixed(1) + '/' + (maxStars || 5) + '.0';
  }
  /** Number of filled stars (0-5) for Overall Rating Summary from finalRating. */
  getFinalRatingStarsCount(): number {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 0;
    return Math.min(5, Math.max(0, Math.round(Number(r))));
  }
  /** Formatted final rating for display e.g. "3.67" or "0". */
  getFinalRatingFormatted(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return '0';
    return (Math.round(Number(r) * 100) / 100).toFixed(2);
  }

  isAcceptSelected: boolean = false;
  isRejectSelected: boolean = false;

  selectAction(action: string) {
    if (action === 'accept') {
      this.isRejectSelected = false;
      this.isAcceptSelected = true;
    } else if (action === 'reject') {
      this.isAcceptSelected = false;
      this.isRejectSelected = true;
    }
  }

  submitRemarkHr: Performance = new Performance();
  acceptReason: any;
  rejectReason: any;
  submitRemarksByHR(quarter: any, template: TemplateRef<any>, index: any) {
    const commentErr = this.getFinalReviewCriteriaCommentError();
    if (commentErr) {
      this.alertMessage = commentErr;
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (this.userMapping.performance_action_by_hod) {
      if ((this.isAcceptSelected || this.isRejectSelected) && !this.validationService.validateNullUndefinedEmptyString(this.hodRemarks)) {
        this.alertMessage = "Please enter HOD Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
    } else {
      // HR: use HR Remarks field for accept/reject reason (same section as manager-to-HOD flow)
      if (this.isAcceptSelected && !this.validationService.validateNullUndefinedEmptyString(this.hrRemarks)) {
        this.alertMessage = "Please enter HR Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (this.isRejectSelected && !this.validationService.validateNullUndefinedEmptyString(this.hrRemarks)) {
        this.alertMessage = "Please enter HR Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
    }
    this.submitRemarkHr.empId = this.selectedEmployee.empId;
    this.submitRemarkHr.quarterId = quarter.quarterId;
    this.submitRemarkHr.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitRemarkHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myList[index].comment)
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
        this.submitRemarkHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myRateList[index].comment)
        });
      }
    });

    this.submitRemarkHr.finalRating = this.finalRating;
    this.submitRemarkHr.employeePerformanceId = this.performnace1[0].employeePerformanceId;

    if (this.userMapping.performance_action_by_hod) {
      this.submitRemarkHr.hodId = this.currentUser.empId;
      this.submitRemarkHr.hodRemarks = this.hodRemarks;
      this.submitRemarkHr.hodReviewStatus = this.isAcceptSelected ? 'Accepted' : 'Rejected';
      this.performanceService.submitRemarksByHOD(this.submitRemarkHr).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse, true);
          const collapseElement = document.getElementById('collapse' + index);
          if (collapseElement) collapseElement.classList.remove('show');
        } else {
          this.openAlertMod(template, response.serviceResponse, true);
        }
      });
    } else {
      this.submitRemarkHr.hrReviewStatus = this.isAcceptSelected ? 'Accepted' : 'Rejected';
      this.submitRemarkHr.hrRemark = this.hrRemarks;
      this.submitRemarkHr.hrId = this.currentUser.empId;
      this.performanceService.submitEmployeePerformanceHR(this.submitRemarkHr).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse, true);
          const collapseElement = document.getElementById('collapse' + index);
          if (collapseElement) collapseElement.classList.remove('show');
        } else {
          this.openAlertMod(template, response.serviceResponse, true);
        }
      });
    }
  }

  onHodSubmit(quarter: any, template: TemplateRef<any>, index: any) {
    if (this.isAcceptSelected || this.isRejectSelected) {
      this.submitRemarksByHR(quarter, template, index);
    } else {
      this.updateReviewEmployee(quarter, template, index);
    }
  }

  /** HOD: Accept in one click – submit as Accepted using HOD Remarks. */
  onHodAccept(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = true;
    this.isRejectSelected = false;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HOD: Reject in one click – submit as Rejected using HOD Remarks. */
  onHodReject(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = false;
    this.isRejectSelected = true;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HR: Accept – set hr_review_status to Accepted using HR Remarks. */
  onHrAccept(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = true;
    this.isRejectSelected = false;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HR: Reject – set hr_review_status to Rejected using HR Remarks. */
  onHrReject(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = false;
    this.isRejectSelected = true;
    this.submitRemarksByHR(quarter, template, index);
  }

  updateReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    const commentErr = this.getFinalReviewCriteriaCommentError();
    if (commentErr) {
      this.alertMessage = commentErr;
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
    this.submitPerformance.employeePerformanceId = null;
    this.submitPerformance.actionBy = (this.currentStatus === 'Rejected' && (this.userMapping.performance_action_by_approvals_to || this.userMapping.performance_action_by_approvals_tos)) ? 'RM' : 'HOD';
    this.submitPerformance.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myList[index].comment)
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myRateList[index].comment)
        });
      }
    });
    this.submitPerformance.finalRating = this.finalRating;
    this.submitPerformance.hodRemarks = this.hodRemarks;
    console.log(this.submitPerformance, "performance");
    this.performanceService.updateEmployeePerformanceHOD(this.submitPerformance).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }
  
  averageRating!:any ;
  getCountOfRewardsAndAppreciation(){
    this.appreciationCount='';
    this.rewardsCount='';
    console.log("this.projectDetails ", this.rewardsCount);
    this.appreciationAndRewardsCount.empId = this.selectedEmployee.empId;
    this.employee360Service.getRewardsAndAppreciationCount(this.appreciationAndRewardsCount).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.rewardsCount = response.serviceResponse[0].rewardsCount;
            this.appreciationCount = response.serviceResponse[0].appreciationCount;
            this.averageRating = response.serviceResponse[0].averageRating;

            console.log("this.projectDetails ",  this.appreciationCount);

          }
        });
  }


  getCurrentUserDepartment() {
  this.performanceService.getCurrentUserDepartment(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // Find the department ID that matches the user's department name
        const userDept = this.departments.find(dept => dept.name === response.serviceResponse);
        
        if (userDept) {
          this.currentUserdepartmentName = userDept.deptId.toString();
          this.selectedDepartment = userDept.deptId.toString();
          this.userDetailsForPerformanceView.deptId = userDept.deptId;
          this.getALLdepartmentByEmployee();
        } else {
          this.currentUserdepartmentName = 'all';
        }
        
        console.log("currentUserdepartmentName", this.currentUserdepartmentName);
      } else {
        this.currentUserdepartmentName = 'all';
      }
    });
  }
  //  toggleEditMode() {
  //   this.isEditMode = !this.isEditMode;
  //   console.log("==========edit mode",this.isEditMode);
  // }
  backupMyList: any[] = [];
  backupMyRateList: any[] = [];
  
// Updated toggleEditMode method
toggleEditMode() {
  if (this.isEditMode) {
    const hasChanges = this.hasDataChanged();
    
    if (hasChanges) {
      this.confirmModalRef = this.modalService.open(this.confirmDiscardModal, {
        centered: true,
        backdrop: 'static',
        keyboard: false
      });
    } else {
      this.isEditMode = false;
      console.log("Edit mode OFF - No changes");
    }
  } else {
    
    this.createBackupData();
    this.isEditMode = true;
    console.log("Edit mode ON - Backup created");
  }
}

// Handle confirmation response
confirmDiscardChanges(discard: boolean) {
  this.confirmDiscard = discard;
  
  if (this.confirmDiscard) {
    // User chose to discard changes
    this.restoreBackupData();
    this.isEditMode = false;
    console.log("Edit mode OFF - Changes discarded");
  } else {
    // User chose to keep editing
    console.log("Edit mode still ON - User chose to keep editing");
  }
  
  // Close the confirmation modal
  this.confirmModalRef.close();
}

// Create backup of current data
createBackupData() {
  this.backupMyList = JSON.parse(JSON.stringify(this.myList));
  this.backupMyRateList = JSON.parse(JSON.stringify(this.myRateList));
  console.log("Backup created:", { backupMyList: this.backupMyList, backupMyRateList: this.backupMyRateList });
}

// Restore data from backup
restoreBackupData() {
  this.myList = JSON.parse(JSON.stringify(this.backupMyList));
  this.myRateList = JSON.parse(JSON.stringify(this.backupMyRateList));
  console.log("Data restored from backup");
}

// Check if data has changed
hasDataChanged(): boolean {
  const currentMyList = JSON.stringify(this.myList);
  const backupListStr = JSON.stringify(this.backupMyList);
  
  const currentMyRateList = JSON.stringify(this.myRateList);
  const backupRateListStr = JSON.stringify(this.backupMyRateList);
  
  const hasChanges = (currentMyList !== backupListStr) || (currentMyRateList !== backupRateListStr);
  
  console.log("Has changes:", hasChanges);
  return hasChanges;
}
  updateReviewByHr(quarter: any, template: TemplateRef<any>, index: any)
  {
    const commentErr = this.getFinalReviewCriteriaCommentError();
    if (commentErr) {
      this.alertMessage = commentErr;
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    const hrFeedback = (this.hrRemarks != null && this.hrRemarks !== '') ? this.hrRemarks.trim() : '';
    if (!hrFeedback) {
      this.alertMessage = 'Please enter HR remark/feedback.';
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    this.updatePerformanceHr.empId = this.selectedEmployee.empId;
    this.updatePerformanceHr.quarterId = quarter.quarterId;
    this.updatePerformanceHr.hrId = this.currentUser.empId;
    this.updatePerformanceHr.employeePerformanceId = null;
    this.updatePerformanceHr.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.updatePerformanceHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myList[index].comment)
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
        this.updatePerformanceHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
          criteriaRemark: this.trimCriteriaComment(this.myRateList[index].comment)
        });
      }
    });
    this.updatePerformanceHr.finalRating = this.finalRating;
    this.updatePerformanceHr.hodRemarks = this.hodRemarks;
    this.updatePerformanceHr.hrRemark = hrFeedback;
    console.log(this.updatePerformanceHr, "performance update by hrrrr");
   
    this.performanceService.updateEmployeePerformanceHr(this.updatePerformanceHr).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }

  /** Close the review panel (Cancel button). */
  cancelReview(index: number) {
    const collapseElement = document.getElementById('collapse' + index);
    if (collapseElement) collapseElement.classList.remove('show');
  }

  /** Whether criteria ratings (slider/stars) and comments can be edited. Manager: Not Started or Rejected (resubmit flow); HOD: always; HR: when isEditMode. */
  canEditRating(): boolean {
    if (this.userMapping?.performance_action_by_hr && this.isEditMode) return true;
    if (this.userMapping?.performance_action_by_hod) return true;
    if (this.userMapping?.performance_action_by_approvals_tos && (this.currentStatus === 'Not Started' || this.currentStatus === 'Rejected')) return true;
    if (this.userMapping?.performance_action_by_approvals_to && this.currentStatus === 'Rejected') return true;
    return false;
  }
}
