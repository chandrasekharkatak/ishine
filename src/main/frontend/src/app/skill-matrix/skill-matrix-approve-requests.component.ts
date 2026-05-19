import { Component, OnDestroy, OnInit } from '@angular/core';
import { Sort, SortDirection } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { SkillMatrixService } from './skill-matrix.service';
import Swal from 'sweetalert2';

@Component({
  standalone: false,
  selector: 'app-skill-matrix-approve-requests',
  templateUrl: './skill-matrix-approve-requests.component.html',
  styleUrls: ['./skill-matrix-placeholders.css', './skill-matrix-approve-requests.component.css']
})
export class SkillMatrixApproveRequestsComponent implements OnInit, OnDestroy {

  loading = false;
  error: string | null = null;
  status: string = '';

  activeTab: 'requests' | 'byskill' | 'approved' | 'done' = 'requests';

  page = 1;
  readonly pageSize = 15;
  totalRows = 0;

  isSearchEnabled = false;
  filters: Record<string, string> = {};
  /** Must align to visible table columns (non-filter cols use 'blank'). */
  readonly filterColumns = [
    'blank', // Sr No
    'employeeName',
    'designation',
    'managerName',
    'managerApprovalStatus',
    'hodName',
    'hodApprovalStatus',
    'finalStatus',
    'blank', // Submitted
    'blank'  // Action
  ];

  /** Approved tab has no Action column; align to its visible columns. */
  readonly approvedFilterColumns = [
    'blank', // Sr No
    'employeeName',
    'designation',
    'managerName',
    'managerApprovalStatus',
    'hodName',
    'hodApprovalStatus',
    'finalStatus',
    'blank' // Submitted
  ];
  sortActive: string | null = null;
  sortDirection: SortDirection = '';

  queueRows: any[] = [];
  detail: any | null = null;
  detailLoading = false;
  openSkillRatingIds = new Set<number>();
  readonly levelBlocks = [1, 2, 3, 4, 5];
  detailPage = 1; // ngx-pagination is 1-based
  detailPageSize = 10;
  detailSkillsTotal = 0;
  modalSkill: any | null = null;
  isHodViewer = false;
  modalIndex = 0;
  skillMeta: any[] = [];
  skillMetaLoading = false;
  selectedSkillRatingIds = new Set<number>();
  // Reject-all uses SweetAlert modal to match portal popups.

  // Skill view
  svLoading = false;
  svError: string | null = null;
  svPage = 1;
  readonly svPageSize = 15;
  svTotal = 0;
  svIsSearchEnabled = false;
  svFilters: Record<string, string> = {};
  /** Align to: SrNo, Skill, Category, Employee, Dept, Decision, Action */
  readonly svFilterColumns = ['blank', 'skillName', 'skillCategory', 'employeeName', 'decision', 'blank'];
  svSortActive: string | null = null;
  svSortDirection: SortDirection = '';
  svRows: any[] = [];

  // Completed
  doneIsSearchEnabled = false;
  doneFilters: Record<string, string> = {};
  readonly doneFilterColumns = [
    'blank', // Sr No
    'employeeName',
    'designation',
    'managerName',
    'managerApprovalStatus',
    'hodName',
    'hodApprovalStatus',
    'finalStatus',
    'blank', // Submitted
    'blank'  // Action
  ];
  doneSortActive: string | null = null;
  doneSortDirection: SortDirection = '';

  private filterDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private skillMatrixService: SkillMatrixService) { }

  ngOnInit(): void {
    this.loadQueue(false);
  }

  ynStatus(v: any): string {
    const s = String(v ?? '').trim().toLowerCase();
    if (!s) {
      return '—';
    }
    if (s === 'yes') return 'Yes';
    if (s === 'no') return 'No';
    if (s === 'pending') return 'Pending';
    return s;
  }

  finalStatusLabel(v: any): string {
    const s = String(v ?? '').trim().toLowerCase();
    if (!s) return 'Pending';
    if (s === 'approved') return 'Approved';
    if (s === 'rejected') return 'Rejected';
    if (s === 'pending') return 'Pending';
    return s;
  }

  finalStatusBadgeClass(v: any): string {
    const s = String(v ?? '').trim().toLowerCase();
    if (s === 'approved') return 'b-gr';
    if (s === 'rejected') return 'b-re';
    return 'b-gy';
  }

  ngOnDestroy(): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
      this.filterDebounceTimer = null;
    }
  }

  switchTab(t: 'requests' | 'byskill' | 'approved' | 'done'): void {
    this.activeTab = t;
    this.detail = null;
    this.page = 1;
    if (t === 'byskill') {
      this.svPage = 1;
      this.loadSkillView(false);
    } else {
      this.loadQueue(true);
    }
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
      this.page = 1;
      this.loadQueue(true);
    }
  }

  onSearch(searchData: Record<string, string>): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
    }
    this.filterDebounceTimer = setTimeout(() => {
      this.filterDebounceTimer = null;
      this.filters = searchData || {};
      this.page = 1;
      this.loadQueue(true);
    }, 120);
  }

  toggleSvSearch(): void {
    this.svIsSearchEnabled = !this.svIsSearchEnabled;
    if (!this.svIsSearchEnabled) {
      this.svFilters = {};
      this.svPage = 1;
      this.loadSkillView(true);
    }
  }

  onSvSearch(searchData: Record<string, string>): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
    }
    this.filterDebounceTimer = setTimeout(() => {
      this.filterDebounceTimer = null;
      this.svFilters = searchData || {};
      this.svPage = 1;
      this.loadSkillView(true);
    }, 120);
  }

  toggleDoneSearch(): void {
    this.doneIsSearchEnabled = !this.doneIsSearchEnabled;
    if (!this.doneIsSearchEnabled) {
      this.doneFilters = {};
      this.page = 1;
      this.loadQueue(true);
    }
  }

  onDoneSearch(searchData: Record<string, string>): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
    }
    this.filterDebounceTimer = setTimeout(() => {
      this.filterDebounceTimer = null;
      this.doneFilters = searchData || {};
      this.page = 1;
      this.loadQueue(true);
    }, 120);
  }

  handlePageChange(p: number): void {
    this.page = p;
    this.loadQueue(true);
  }

  sortData(sort: Sort): void {
    this.sortActive = sort.active;
    this.sortDirection = sort.direction;
    this.page = 1;
    this.loadQueue(true);
  }

  sortSkillView(sort: Sort): void {
    this.svSortActive = sort.active;
    this.svSortDirection = sort.direction;
    this.svPage = 1;
    this.loadSkillView(true);
  }

  sortDone(sort: Sort): void {
    this.doneSortActive = sort.active;
    this.doneSortDirection = sort.direction;
    this.page = 1;
    this.loadQueue(true);
  }

  private masterListSort(): { column: string; direction: 'asc' | 'desc' } | null {
    if (!this.sortActive || !this.sortDirection) {
      return null;
    }
    const column = this.sortActive.split('|')[0];
    const direction = this.sortDirection === 'desc' ? 'desc' : 'asc';
    return { column, direction };
  }

  private masterListSortSkillView(): { column: string; direction: 'asc' | 'desc' } | null {
    if (!this.svSortActive || !this.svSortDirection) {
      return null;
    }
    const column = this.svSortActive.split('|')[0];
    const direction = this.svSortDirection === 'desc' ? 'desc' : 'asc';
    return { column, direction };
  }

  private masterListSortDone(): { column: string; direction: 'asc' | 'desc' } | null {
    if (!this.doneSortActive || !this.doneSortDirection) {
      return null;
    }
    const column = this.doneSortActive.split('|')[0];
    const direction = this.doneSortDirection === 'desc' ? 'desc' : 'asc';
    return { column, direction };
  }

  handleSvPageChange(p: number): void {
    this.svPage = p;
    this.loadSkillView(true);
  }

  onSvFiltersChange(): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
    }
    this.filterDebounceTimer = setTimeout(() => {
      this.filterDebounceTimer = null;
      this.svPage = 1;
      this.loadSkillView(true);
    }, 150);
  }

  openDetail(row: any): void {
    if (!row?.submissionId) {
      return;
    }
    this.detailPage = 1;
    this.loadDetail(row.submissionId, this.detailPage);
    this.loadSkillMeta(row.submissionId);
  }

  private loadDetail(submissionId: string, page1: number): void {
    this.detailLoading = true;
    this.error = null;
    this.skillMatrixService.getApproveSubmissionDetail(submissionId, Math.max(0, (page1 || 1) - 1), this.detailPageSize).pipe(first()).subscribe({
      next: (res: any) => {
        this.detailLoading = false;
        if (res?.serviceStatus !== 'Success') {
          this.error = res?.serviceResponse || 'Failed to load detail.';
          return;
        }
        this.detail = res.serviceResponse;
        this.isHodViewer = String(this.detail?.viewerRole || '').toLowerCase() === 'hod';
        this.detailSkillsTotal = Number(this.detail?.skillsTotal ?? this.detail?.skillsTotalElements ?? this.detail?.skills?.length ?? 0);
        // reset selection on page load (matches prototype behavior)
        this.selectedSkillRatingIds.clear();
      },
      error: () => {
        this.detailLoading = false;
        this.error = 'Could not load details right now. Please try again.';
      }
    });
  }

  decisionLabelForViewer(decision: string | null | undefined): string {
    return this.decisionLabel(decision);
  }

  private loadSkillMeta(submissionId: string, onDone?: () => void): void {
    this.skillMetaLoading = true;
    this.skillMatrixService.getApproveSubmissionSkillsMeta(submissionId).pipe(first()).subscribe({
      next: (res: any) => {
        this.skillMetaLoading = false;
        if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
          this.skillMeta = [];
          if (onDone) onDone();
          return;
        }
        this.skillMeta = res.serviceResponse;
        if (onDone) onDone();
      },
      error: () => {
        this.skillMetaLoading = false;
        this.skillMeta = [];
        if (onDone) onDone();
      }
    });
  }

  handleDetailPageChange(p: number): void {
    this.detailPage = p;
    const sid = this.detail?.submissionId;
    if (!sid) return;
    this.loadDetail(sid, p);
  }

  onDetailPageSizeChange(): void {
    this.detailPage = 1;
    const sid = this.detail?.submissionId;
    if (!sid) return;
    this.loadDetail(sid, 1);
  }

  // Prototype helpers
  toggleAllOnPage(checked: boolean): void {
    if (!this.detail?.skills?.length) return;
    if (checked) {
      this.detail.skills.forEach((s: any) => {
        if (s?.skillRatingId != null) this.selectedSkillRatingIds.add(Number(s.skillRatingId));
      });
    } else {
      this.detail.skills.forEach((s: any) => {
        if (s?.skillRatingId != null) this.selectedSkillRatingIds.delete(Number(s.skillRatingId));
      });
    }
  }

  toggleRowSelect(skill: any, checked: boolean): void {
    const rid = skill?.skillRatingId;
    if (rid == null) return;
    const n = Number(rid);
    if (checked) this.selectedSkillRatingIds.add(n);
    else this.selectedSkillRatingIds.delete(n);
  }

  isRowSelected(skill: any): boolean {
    const rid = skill?.skillRatingId;
    if (rid == null) return false;
    return this.selectedSkillRatingIds.has(Number(rid));
  }

  allOnPageSelected(): boolean {
    const rows: any[] = this.detail?.skills || [];
    if (!rows.length) return false;
    return rows.every((s) => s?.skillRatingId != null && this.selectedSkillRatingIds.has(Number(s.skillRatingId)));
  }

  pendingBadgeText(): string {
    const p = Number(this.detail?.pendingCount ?? 0);
    return p > 0 ? `${p} pending` : 'All reviewed';
  }

  confirmRejectAll(): void {
    if (Number(this.detail?.pendingCount ?? 0) <= 0) return;
    const cnt = Number(this.detail?.pendingCount ?? 0);
    Swal.fire({
      title: `Reject ${cnt} pending skill(s)?`,
      text: 'Provide one comment. It will be applied to all pending skills.',
      input: 'textarea',
      inputPlaceholder: 'Comment (required)',
      inputAttributes: { 'aria-label': 'Comment' },
      showCancelButton: true,
      confirmButtonText: 'Reject all',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#193d8a',
      cancelButtonColor: '#6c757d',
      preConfirm: (value) => {
        const v = String(value || '').trim();
        if (!v) {
          Swal.showValidationMessage('Comment is required.');
          return;
        }
        return v;
      }
    }).then((r) => {
      if (!r.isConfirmed) return;
      const cmt = String(r.value || '').trim();
      this.bulkRejectAllPending(cmt);
    });
  }

  bulkApproveAllPending(): void {
    if (!this.detail?.submissionId) return;
    this.skillMatrixService.bulkApprovePendingSkills(this.detail.submissionId).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') {
          this.error = res?.serviceResponse || 'Bulk approve failed.';
          return;
        }
        // refresh meta + page
        this.loadSkillMeta(this.detail.submissionId);
        this.loadDetail(this.detail.submissionId, this.detailPage);
      },
      error: () => {
        this.error = 'Bulk approve failed.';
      }
    });
  }

  bulkRejectAllPending(managerComment: string): void {
    if (!this.detail?.submissionId) return;
    const cmt = (managerComment || '').trim();
    if (!cmt) return;
    this.skillMatrixService.bulkRejectPendingSkills(this.detail.submissionId, { managerComment: cmt }).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') {
          this.error = res?.serviceResponse || 'Bulk reject failed.';
          return;
        }
        this.loadSkillMeta(this.detail.submissionId);
        this.loadDetail(this.detail.submissionId, this.detailPage);
      },
      error: () => {
        this.error = 'Bulk reject failed.';
      }
    });
  }

  detailTotalPages(): number {
    const total = Math.max(0, Number(this.detailSkillsTotal || 0));
    const size = Math.max(1, Number(this.detailPageSize || 10));
    return Math.max(1, Math.ceil(total / size));
  }

  detailPagerPages(): number[] {
    const totalPages = this.detailTotalPages();
    const cur = Math.min(totalPages, Math.max(1, this.detailPage));
    const span = 5;
    let start = Math.max(1, cur - Math.floor(span / 2));
    let end = Math.min(totalPages, start + span - 1);
    start = Math.max(1, end - span + 1);
    const out: number[] = [];
    for (let p = start; p <= end; p++) out.push(p);
    return out;
  }

  goDetailPage(p: number): void {
    const totalPages = this.detailTotalPages();
    const next = Math.min(totalPages, Math.max(1, p));
    if (next === this.detailPage) return;
    this.handleDetailPageChange(next);
  }

  detailRangeStart(): number {
    if (this.detailSkillsTotal <= 0) return 0;
    return (this.detailPage - 1) * this.detailPageSize + 1;
  }

  detailRangeEnd(): number {
    if (this.detailSkillsTotal <= 0) return 0;
    return Math.min(this.detailPage * this.detailPageSize, this.detailSkillsTotal);
  }

  closeDetail(): void {
    this.detail = null;
    this.openSkillRatingIds.clear();
    this.loadQueue(true);
  }

  isSkillOpen(skill: any): boolean {
    const id = skill?.skillRatingId;
    if (id == null) return false;
    return this.openSkillRatingIds.has(Number(id));
  }

  toggleSkill(skill: any): void {
    const id = skill?.skillRatingId;
    if (id == null) return;
    const n = Number(id);
    if (this.openSkillRatingIds.has(n)) {
      this.openSkillRatingIds.delete(n);
    } else {
      // accordion: open one at a time
      this.openSkillRatingIds.clear();
      this.openSkillRatingIds.add(n);
    }
  }

  decisionLabel(decision: string | null | undefined): string {
    const d = String(decision || '').trim().toLowerCase();
    if (!d || d === 'pending') return 'Pending';
    if (d === 'approved') return 'Approved';
    if (d === 'rejected') return 'Rejected';
    if (d === 'adjusted') return 'Adjusted';
    if (d === 'sent_back') return 'Sent back';
    return String(decision).replace(/_/g, ' ');
  }

  effectiveDecision(skill: any): 'approved' | 'adjusted' | 'sent_back' | 'rejected' | null {
    if (!skill) return null;
    const d = (skill._draftDecision ?? (this.isHodViewer ? skill.hodDecision : skill.managerDecision)) as any;
    if (d === 'approved' || d === 'adjusted' || d === 'sent_back' || d === 'rejected') return d;
    return null;
  }

  decide(skill: any, decision: 'approved' | 'adjusted' | 'sent_back' | 'rejected', managerRating?: number | null): void {
    if (!this.detail?.submissionId || !skill?.skillRatingId) {
      return;
    }
    // Client-side validation (show professional popup instead of raw API errors)
    if (!this.isHodViewer && decision === 'adjusted') {
      const r = Number(managerRating ?? skill.managerRating ?? skill.selfRating ?? 0);
      if (!r || r < 1 || r > 5) {
        Swal.fire({
          icon: 'warning',
          title: 'Manager Rating is required',
          text: 'Please select the corrected rating (L1 to L5) before adjusting.',
          confirmButtonText: 'OK',
          confirmButtonColor: '#193d8a'
        });
        return;
      }
    }
    if (this.decisionNeedsComment(decision)) {
      const cmt = String(skill?.managerComment || '').trim();
      if (!cmt) {
        Swal.fire({
          icon: 'warning',
          title: 'Comment is required',
          text: this.isHodViewer ? 'Please enter a comment to reject.' : 'Please enter a comment to proceed with Adjust / Send back / Reject.',
          confirmButtonText: 'OK',
          confirmButtonColor: '#193d8a'
        });
        return;
      }
    }
    const body: any = {
      skillRatingId: skill.skillRatingId,
      decision,
      managerRating: (!this.isHodViewer && decision === 'adjusted') ? (managerRating ?? skill.managerRating ?? skill.selfRating) : null,
      managerComment: skill.managerComment || null
    };
    this.skillMatrixService.saveApproveSkillDecision(this.detail.submissionId, body).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') {
          const msg = String(res?.serviceResponse || 'Save failed.');
          if (msg.toLowerCase().includes('managercomment is required')) {
            Swal.fire({
              icon: 'warning',
              title: 'Comment is required',
              text: this.isHodViewer ? 'Please enter a comment to reject.' : 'Please enter a comment to proceed with Adjust / Send back / Reject.',
              confirmButtonText: 'OK',
              confirmButtonColor: '#193d8a'
            });
            return;
          }
          this.error = msg;
          return;
        }
        if (this.isHodViewer) {
          skill.hodDecision = decision;
          if (decision === 'approved' || decision === 'rejected') {
            skill.finalDecision = decision;
          }
        } else {
          skill.managerDecision = decision;
        }
        // clear any local draft state only after a successful save
        if (skill._draftDecision != null) {
          skill._draftDecision = null;
        }
        if (!this.isHodViewer && decision === 'adjusted') {
          skill.managerRating = body.managerRating;
        }

        const title =
          decision === 'approved' ? 'Approved successfully' :
          decision === 'rejected' ? 'Rejected successfully' :
          decision === 'sent_back' ? 'Sent back successfully' :
          'Adjusted successfully';

        Swal.fire({
          icon: 'success',
          title,
          timer: 1400,
          showConfirmButton: false
        });
      },
      error: () => this.error = 'Could not save right now. Please try again.'
    });
  }

  beginDecision(skill: any, decision: 'approved' | 'adjusted' | 'sent_back' | 'rejected'): void {
    if (!skill) return;
    // Do not set the saved decision until backend confirms.
    // We use a draft decision to show the relevant UI (comment/rating) immediately.
    skill._draftDecision = decision;
    if (decision === 'adjusted') {
      // ensure managerRating has a default so UI renders correctly
      if (skill.managerRating == null) {
        skill.managerRating = skill.selfRating;
      }
    }
    // Only auto-save for Approve; others need comment/rating first.
    if (decision === 'approved') {
      this.decide(skill, 'approved');
      return;
    }
    // bring comment/rating area into view
    setTimeout(() => {
      const el = document.querySelector('.smSkillPage .smModalInputs');
      if (el && 'scrollIntoView' in el) {
        try { (el as any).scrollIntoView({ behavior: 'smooth', block: 'start' }); } catch {}
      }
    }, 0);
  }

  setManagerComment(skill: any, v: string): void {
    skill.managerComment = v;
  }

  setAdjustedRating(skill: any, rating: number): void {
    skill.managerRating = rating;
  }

  openSkillModal(skill: any): void {
    if (!skill) return;
    if (!this.detail?.submissionId) return;
    const submissionId = this.detail.submissionId;
    const rid = Number(skill?.skillRatingId);
    if (!rid) return;

    // Open immediately (optimistic) so UI responds instantly.
    // This row may be a light DTO; we will replace it with full detail once fetched.
    this.modalSkill = {
      ...skill,
      whatCanYouDo: skill?.whatCanYouDo ?? '',
      subskills: skill?.subskills ?? [],
      certifications: skill?.certifications ?? [],
      trainings: skill?.trainings ?? [],
      projectNames: skill?.projectNames ?? [],
      _loading: true
    };

    // Ensure meta is loaded so Prev/Next works reliably.
    const ensureMeta = (cb: () => void) => {
      if (this.skillMeta && this.skillMeta.length) {
        cb();
        return;
      }
      this.loadSkillMeta(submissionId, () => cb());
    };

    ensureMeta(() => {
      const idx = this.skillMeta.findIndex((x) => Number(x?.skillRatingId) === rid);
      this.modalIndex = idx >= 0 ? idx : 0;
    });

    // Always load full detail for the page view (table row is a light DTO).
    this.skillMatrixService.getApproveSubmissionSkillDetail(submissionId, rid).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') {
          Swal.fire({
            icon: 'error',
            title: 'Could not open skill',
            text: String(res?.serviceResponse || 'Please try again.'),
            confirmButtonText: 'OK',
            confirmButtonColor: '#193d8a'
          });
          this.modalSkill = null;
          return;
        }
        this.modalSkill = { ...(res.serviceResponse || {}), _draftDecision: null, _loading: false };
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Could not open skill',
          text: 'Please try again.',
          confirmButtonText: 'OK',
          confirmButtonColor: '#193d8a'
        });
        this.modalSkill = null;
      }
    });

    try {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch {
      // ignore
    }
  }

  closeSkillModal(): void {
    this.modalSkill = null;
  }

  modalNavText(): string {
    const total = this.skillMeta?.length || 0;
    if (!total) return '';
    return `${this.modalIndex + 1} / ${total}`;
  }

  navigateModal(delta: number): void {
    const total = this.skillMeta?.length || 0;
    if (!total || !this.detail?.submissionId) return;
    let next = this.modalIndex + delta;
    if (next < 0) next = 0;
    if (next > total - 1) next = total - 1;
    if (next === this.modalIndex) return;
    this.modalIndex = next;
    const rid = Number(this.skillMeta[this.modalIndex]?.skillRatingId);
    if (!rid) return;
    this.skillMatrixService.getApproveSubmissionSkillDetail(this.detail.submissionId, rid).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') return;
        this.modalSkill = { ...(res.serviceResponse || {}), _draftDecision: null };
      }
    });
  }

  decisionNeedsComment(decision: string | null | undefined): boolean {
    if (!decision) return false;
    if (this.isHodViewer) {
      return decision === 'rejected';
    }
    return decision === 'adjusted' || decision === 'sent_back' || decision === 'rejected';
  }

  submitReview(): void {
    if (!this.detail?.submissionId) {
      return;
    }
    if (this.isHodViewer) {
      this.submitHodDecision('approved');
      return;
    }
    this.skillMatrixService.submitApproveReview(this.detail.submissionId, {
      submissionId: this.detail.submissionId,
      overallComment: null
    }).pipe(first()).subscribe({
      next: (res: any) => {
        if (res?.serviceStatus !== 'Success') {
          this.error = res?.serviceResponse || 'Submit failed.';
          return;
        }
        this.closeDetail();
      },
      error: () => this.error = 'Could not submit right now. Please try again.'
    });
  }

  submitHodDecision(decision: 'approved' | 'rejected'): void {
    if (!this.detail?.submissionId) return;
    const go = (comment: string | null) => {
      this.skillMatrixService.submitHodDecision(this.detail.submissionId, { decision, comment }).pipe(first()).subscribe({
        next: (res: any) => {
          if (res?.serviceStatus !== 'Success') {
            this.error = res?.serviceResponse || 'Submit failed.';
            return;
          }
          this.closeDetail();
        },
        error: () => this.error = 'Could not submit right now. Please try again.'
      });
    };

    if (decision === 'rejected') {
      Swal.fire({
        customClass: { popup: 'smSwalTiny' },
        showCancelButton: true,
        confirmButtonText: 'Reject',
        cancelButtonText: 'Cancel',
        html: `
          <div style="text-align:left;font-size:12px">
            <div style="font-weight:600;margin-bottom:6px">Comment is required</div>
            <textarea id="sm_hod_rej_cmt" class="swal2-textarea" style="margin:0;width:100%;min-height:80px" placeholder="Enter rejection comment"></textarea>
          </div>
        `,
        preConfirm: () => {
          const el = document.getElementById('sm_hod_rej_cmt') as HTMLTextAreaElement | null;
          const v = (el?.value || '').trim();
          if (!v) {
            Swal.showValidationMessage('Comment is required');
            return false as any;
          }
          return v;
        }
      }).then(r => {
        if (!r.isConfirmed) return;
        go(String(r.value || '').trim());
      });
    } else {
      go(null);
    }
  }

  getStats() {
    const rows = this.queueRows || [];
    const awaiting = rows.filter(r => ['submitted', 'under_review', 'changes_requested'].includes(String(r.status || '').toLowerCase())).length;
    const overdue = rows.filter(r => this.isOverdue(r)).length;
    const skillsToReview = rows.reduce((a, r) => a + Math.max(0, Number(r.totalSkills || 0) - Number(r.reviewedSkills || 0)), 0);
    // Completed should mean FINAL decision done (after HOD), not just manager-reviewed skills.
    const completed = rows.filter(r => ['approved', 'rejected'].includes(String(r.status || '').toLowerCase())).length;
    return { awaiting, overdue, skillsToReview, completed };
  }

  reviewedCount(skills: any[] | null | undefined): number {
    if (!skills || !skills.length) return 0;
    let n = 0;
    for (const s of skills) {
      const d = String(s?.decision || '').toLowerCase();
      if (d && d !== 'pending') n += 1;
    }
    return n;
  }

  pendingCount(skills: any[] | null | undefined): number {
    if (!skills || !skills.length) return 0;
    return skills.length - this.reviewedCount(skills);
  }

  progressPercent(skills: any[] | null | undefined): number {
    const total = skills?.length || 0;
    if (!total) return 0;
    return Math.round((this.reviewedCount(skills) / total) * 100);
  }

  reviewedTotal(): number {
    const total = Number(this.detail?.skillsTotal ?? 0);
    const pending = Number(this.detail?.pendingCount ?? 0);
    if (!total) return 0;
    return Math.max(0, total - Math.max(0, pending));
  }

  progressPercentTotal(): number {
    const total = Number(this.detail?.skillsTotal ?? 0);
    if (!total) return 0;
    return Math.round((this.reviewedTotal() / total) * 100);
  }

  isOverdue(row: any): boolean {
    if (!row?.reviewDeadline) {
      return false;
    }
    const d = new Date(row.reviewDeadline);
    const now = new Date();
    d.setHours(0, 0, 0, 0);
    now.setHours(0, 0, 0, 0);
    return d.getTime() < now.getTime();
  }

  daysLeft(row: any): number | null {
    if (!row?.reviewDeadline) {
      return null;
    }
    const d = new Date(row.reviewDeadline);
    const now = new Date();
    d.setHours(0, 0, 0, 0);
    now.setHours(0, 0, 0, 0);
    return Math.round((d.getTime() - now.getTime()) / (24 * 60 * 60 * 1000));
  }

  initials(name: string): string {
    const t = (name || '').trim();
    if (!t) return '—';
    const parts = t.split(/\s+/).filter(Boolean);
    return (parts[0]?.[0] || '').toUpperCase() + (parts[1]?.[0] || parts[0]?.[1] || '').toUpperCase();
  }

  private loadQueue(silent: boolean): void {
    if (!silent) {
      this.loading = true;
      this.status = '';
    }
    this.error = null;
    const filters: Record<string, string> = { ...(this.filters || {}) };
    if (this.activeTab === 'approved') {
      filters.status = 'approved';
    } else if (this.activeTab === 'done') {
      Object.assign(filters, this.doneFilters || {});
      filters.status = 'done';
    }

    this.skillMatrixService.getApproveRequestsQueue(this.page - 1, this.pageSize, filters, this.masterListSort()).pipe(first()).subscribe({
      next: (res: any) => {
        this.loading = false;
        if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
          this.queueRows = [];
          this.totalRows = 0;
          this.error = res?.serviceResponse || 'Unexpected response';
          this.status = '';
          return;
        }
        this.queueRows = res.serviceResponse;
        this.totalRows = res.totalElements != null ? Number(res.totalElements) : this.queueRows.length;
        this.status = '';
      },
      error: () => {
        this.loading = false;
        this.queueRows = [];
        this.totalRows = 0;
        this.error = 'Could not load data right now. Please try again.';
        this.status = '';
      }
    });
  }

  private loadSkillView(silent: boolean): void {
    if (!silent) {
      this.svLoading = true;
    }
    this.svError = null;
    const filters: Record<string, string> = { ...(this.svFilters || {}) };
    this.skillMatrixService.getApproveSkillView(this.svPage - 1, this.svPageSize, filters, this.masterListSortSkillView()).pipe(first()).subscribe({
      next: (res: any) => {
        this.svLoading = false;
        if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
          this.svRows = [];
          this.svTotal = 0;
          this.svError = res?.serviceResponse || 'Unexpected response';
          return;
        }
        this.svRows = res.serviceResponse;
        this.svTotal = res.totalElements != null ? Number(res.totalElements) : this.svRows.length;
      },
      error: () => {
        this.svLoading = false;
        this.svRows = [];
        this.svTotal = 0;
        this.svError = 'Could not load data right now. Please try again.';
      }
    });
  }
}
