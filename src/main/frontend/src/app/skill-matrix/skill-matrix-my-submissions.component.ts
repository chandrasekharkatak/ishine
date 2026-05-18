import { Component, OnDestroy, OnInit } from '@angular/core';
import { Sort, SortDirection } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { SkillMatrixService } from './skill-matrix.service';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-skill-matrix-my-submissions',
  templateUrl: './skill-matrix-my-submissions.component.html',
  styleUrls: ['./skill-matrix-placeholders.css', './skill-matrix-my-submissions.component.css']
})
export class SkillMatrixMySubmissionsComponent implements OnInit, OnDestroy {

  loading = false;
  error: string | null = null;
  page = 1;
  readonly pageSize = 15;
  totalRows = 0;
  isSearchEnabled = false;
  filters: Record<string, string> = {};
  private filterDebounceTimer: ReturnType<typeof setTimeout> | null = null;
  sortActive: string | null = null;
  sortDirection: SortDirection = '';

  readonly filterColumns = [
    'submissionId',
    'designation',
    'reportingManagerName',
    'managerApprovalStatus',
    'hodName',
    'hodApprovalStatus',
    'finalStatus',
    'status',
    'blank', // Submitted
    'blank', // Updated
    'blank'  // Action
  ];

  rows: Array<{
    submissionId: string;
    deptName: string;
    designation: string;
    reportingManagerName: string;
    managerApprovalStatus?: string;
    hodName?: string;
    hodApprovalStatus?: string;
    finalStatus?: string;
    status: string;
    submittedAt: Date | null;
    updatedAt: Date | null;
  }> = [];

  ynStatus(v: any): string {
    const s = String(v ?? '').trim().toLowerCase();
    if (!s) return '—';
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

  constructor(private skillMatrixService: SkillMatrixService, private router: Router) { }

  ngOnInit(): void {
    this.loadList(false);
  }

  ngOnDestroy(): void {
    if (this.filterDebounceTimer != null) {
      clearTimeout(this.filterDebounceTimer);
      this.filterDebounceTimer = null;
    }
  }

  toggleSearch(): void {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      if (this.filterDebounceTimer != null) {
        clearTimeout(this.filterDebounceTimer);
        this.filterDebounceTimer = null;
      }
      this.filters = {};
      this.page = 1;
      this.loadList(true);
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
      this.loadList(true);
    }, 120);
  }

  handlePageChange(p: number): void {
    this.page = p;
    this.loadList(true);
  }

  sortData(sort: Sort): void {
    this.sortActive = sort.active;
    this.sortDirection = sort.direction;
    this.page = 1;
    this.loadList(true);
  }

  private masterListSort(): { column: string; direction: 'asc' | 'desc' } | null {
    if (!this.sortActive || !this.sortDirection) {
      return null;
    }
    const column = this.sortActive.split('|')[0];
    const direction = this.sortDirection === 'desc' ? 'desc' : 'asc';
    return { column, direction };
  }

  private loadList(silent: boolean): void {
    if (!silent) {
      this.loading = true;
    }
    this.error = null;
    this.skillMatrixService.getMySubmissions(this.page - 1, this.pageSize, this.filters, this.masterListSort()).pipe(first()).subscribe({
      next: (res: any) => {
        this.loading = false;
        if (res?.serviceStatus !== 'Success' || !Array.isArray(res.serviceResponse)) {
          this.rows = [];
          this.totalRows = 0;
          this.error = res?.serviceResponse || 'Unexpected response';
          return;
        }
        this.rows = res.serviceResponse.map((r: any) => ({
          submissionId: String(r.submissionId ?? ''),
          deptName: String(r.deptName ?? ''),
          designation: String(r.designation ?? ''),
          reportingManagerName: String(r.reportingManagerName ?? ''),
          managerApprovalStatus: r.managerApprovalStatus != null ? String(r.managerApprovalStatus) : '',
          hodName: r.hodName != null ? String(r.hodName) : '',
          hodApprovalStatus: r.hodApprovalStatus != null ? String(r.hodApprovalStatus) : '',
          finalStatus: r.finalStatus != null ? String(r.finalStatus) : '',
          status: String(r.status ?? ''),
          submittedAt: r.submittedAt ? new Date(r.submittedAt) : null,
          updatedAt: r.updatedAt ? new Date(r.updatedAt) : null,
        }));
        this.totalRows = res.totalElements != null ? Number(res.totalElements) : this.rows.length;
      },
      error: () => {
        this.loading = false;
        this.rows = [];
        this.totalRows = 0;
        this.error = 'Could not load data right now. Please try again.';
      }
    });
  }

  isRejected(row: any): boolean {
    const s = String(row?.status || '').toLowerCase();
    return s === 'rejected';
  }

  editRejected(row: any): void {
    if (!row?.submissionId) return;
    this.router.navigate(['/skill-matrix/submit-for-review'], {
      queryParams: { submissionId: row.submissionId }
    });
  }

  addSkills(row: any): void {
    if (!row?.submissionId) return;
    this.router.navigate(['/skill-matrix/submit-for-review'], {
      queryParams: { mode: 'add-skills', baseSubmissionId: row.submissionId }
    });
  }
}
