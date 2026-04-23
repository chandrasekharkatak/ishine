import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ExportExcelService } from '../services/export-excel.service';
import { GrievanceService } from '../services/grievance.service';

@Component({
  standalone: false,
  selector: 'app-grievance-issue-scenario-admin',
  templateUrl: './grievance-issue-scenario-admin.component.html',
  styleUrls: ['./grievance.component.css', './grievance-issue-scenario-admin.component.css'],
})
export class GrievanceIssueScenarioAdminComponent implements OnInit {
  loading = true;
  saving = false;
  listLoading = false;
  exportLoading = false;
  /** Server caps page size at 100; used when exporting all rows. */
  private readonly exportFetchSize = 100;
  scenarios: any[] = [];
  totalElements = 0;
  page = 1;
  readonly itemsPerPage = 10;
  sortColumn = 'tabKey';
  sortDirection: 'asc' | 'desc' = 'asc';
  alertMessage = '';
  alertType: 'success' | 'danger' | 'warning' = 'success';

  /** All portal tab names (same source as Raise Grievance category dropdown). */
  allCategories: string[] = [];
  subCategories: string[] = [];
  ticketFeatureOptions: string[] = [];
  loadingSubCategories = false;
  loadingTicketFeatures = false;

  form: {
    scenarioId: number | null;
    category: string;
    subCategory: string;
    ticketFeature: string;
    scenarioLabel: string;
    /** Not shown in UI; new = 0, edit keeps existing. Backend defaults null to 0. */
    sortOrder: number;
    isActive: boolean;
  } = {
    scenarioId: null,
    category: '',
    subCategory: '',
    ticketFeature: '',
    scenarioLabel: '',
    sortOrder: 0,
    isActive: true,
  };

  constructor(
    private grievanceService: GrievanceService,
    private router: Router,
    private exportExcelService: ExportExcelService,
  ) {}

  ngOnInit(): void {
    this.grievanceService
      .issueScenarioAdminEligible()
      .pipe(first())
      .subscribe(
        (response: any) => {
          const ok = response?.serviceStatus === 'Success' && response?.serviceResponse === true;
          if (!ok) {
            this.router.navigate(['/grievance']);
            return;
          }
          this.loading = false;
          this.loadCategories();
          this.loadList();
        },
        () => {
          this.router.navigate(['/grievance']);
        }
      );
  }

  loadCategories(): void {
    this.grievanceService
      .getCategories()
      .pipe(first())
      .subscribe(
        (response: any) => {
          if (response?.serviceStatus === 'Success') {
            this.allCategories = response.serviceResponse || [];
          } else {
            this.allCategories = [];
          }
        },
        () => {
          this.allCategories = [];
        }
      );
  }

  loadList(): void {
    this.listLoading = true;
    this.grievanceService
      .listAdminIssueScenarios(this.page, this.itemsPerPage, this.sortColumn, this.sortDirection)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.listLoading = false;
          if (response?.serviceStatus === 'Success') {
            const dto = response.serviceResponse;
            this.scenarios = dto?.content || [];
            this.totalElements = dto?.totalElements ?? 0;
            if (dto?.page != null) {
              this.page = dto.page;
            }
          } else {
            this.scenarios = [];
            this.totalElements = 0;
            this.showAlert(response?.serviceResponse || 'Unable to load scenarios.', 'danger');
          }
        },
        () => {
          this.listLoading = false;
          this.scenarios = [];
          this.totalElements = 0;
          this.showAlert('Unable to load scenarios.', 'danger');
        }
      );
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.page = 1;
    this.loadList();
  }

  getSortIcon(column: string): string {
    if (this.sortColumn !== column) {
      return 'fa fa-sort text-muted';
    }
    return this.sortDirection === 'asc' ? 'fa fa-sort-asc' : 'fa fa-sort-desc';
  }

  handlePageChange(pageNo: number): void {
    this.page = pageNo;
    this.loadList();
  }

  private loadFeaturesForCategoryAndOptionalSub(category: string, subCategory: string, afterLoad?: () => void): void {
    const c = (category || '').trim();
    if (!c) {
      this.ticketFeatureOptions = [];
      afterLoad?.();
      return;
    }
    this.loadingTicketFeatures = true;
    const sub = (subCategory || '').trim();
    this.grievanceService
      .getTicketFeatures(c, sub)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingTicketFeatures = false;
          if (response?.serviceStatus === 'Success') {
            this.ticketFeatureOptions = response.serviceResponse || [];
          } else {
            this.ticketFeatureOptions = [];
          }
          afterLoad?.();
        },
        () => {
          this.loadingTicketFeatures = false;
          this.ticketFeatureOptions = [];
          afterLoad?.();
        }
      );
  }

  onCategoryChange(): void {
    this.form.subCategory = '';
    this.form.ticketFeature = '';
    this.subCategories = [];
    this.ticketFeatureOptions = [];
    const c = (this.form.category || '').trim();
    if (!c) {
      return;
    }
    this.loadingSubCategories = true;
    this.grievanceService
      .getSubCategories(c)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingSubCategories = false;
          if (response?.serviceStatus === 'Success') {
            this.subCategories = response.serviceResponse || [];
          } else {
            this.subCategories = [];
          }
          this.loadFeaturesForCategoryAndOptionalSub(c, '');
        },
        () => {
          this.loadingSubCategories = false;
          this.subCategories = [];
        }
      );
  }

  onSubCategoryChange(): void {
    this.form.ticketFeature = '';
    this.ticketFeatureOptions = [];
    const c = (this.form.category || '').trim();
    const sub = (this.form.subCategory || '').trim();
    if (!c) {
      return;
    }
    this.loadFeaturesForCategoryAndOptionalSub(c, sub);
  }

  resetForm(): void {
    this.form = {
      scenarioId: null,
      category: '',
      subCategory: '',
      ticketFeature: '',
      scenarioLabel: '',
      sortOrder: 0,
      isActive: true,
    };
    this.subCategories = [];
    this.ticketFeatureOptions = [];
    this.alertMessage = '';
  }

  startEdit(row: any): void {
    const categoryGuess = ((row.categoryDisplayName || '') as string).trim() || this.guessCategoryFromTabKey(row.tabKey);
    this.form = {
      scenarioId: row.scenarioId,
      category: categoryGuess,
      subCategory: row.subFeatureName || '',
      ticketFeature: row.featureName || '',
      scenarioLabel: row.scenarioLabel || '',
      sortOrder: row.sortOrder != null ? Number(row.sortOrder) : 0,
      isActive: row.isActive !== 0,
    };
    this.subCategories = [];
    this.ticketFeatureOptions = [];
    if (!categoryGuess) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    this.loadingSubCategories = true;
    this.grievanceService
      .getSubCategories(categoryGuess)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loadingSubCategories = false;
          if (response?.serviceStatus === 'Success') {
            this.subCategories = response.serviceResponse || [];
          } else {
            this.subCategories = [];
          }
          const sub = (this.form.subCategory || '').trim();
          this.loadFeaturesForCategoryAndOptionalSub(categoryGuess, sub);
        },
        () => {
          this.loadingSubCategories = false;
          this.subCategories = [];
        }
      );
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  save(): void {
    const category = (this.form.category || '').trim();
    if (!category) {
      this.showAlert('Choose a portal category (same list as Raise Grievance).', 'warning');
      return;
    }
    if (!(this.form.scenarioLabel || '').trim()) {
      this.showAlert('Scenario label is required.', 'warning');
      return;
    }
    const payload: any = {
      categoryTabName: category,
      scenarioLabel: this.form.scenarioLabel.trim(),
      sortOrder: this.form.sortOrder,
      isActive: this.form.isActive ? 1 : 0,
    };
    const sub = (this.form.subCategory || '').trim();
    const feat = (this.form.ticketFeature || '').trim();
    if (sub) {
      payload.subFeatureName = sub;
    }
    if (feat) {
      payload.featureName = feat;
    }

    this.saving = true;
    const req$ =
      this.form.scenarioId != null
        ? this.grievanceService.updateAdminIssueScenario(this.form.scenarioId, payload)
        : this.grievanceService.createAdminIssueScenario(payload);

    req$.pipe(first()).subscribe(
      (response: any) => {
        this.saving = false;
        if (response?.serviceStatus === 'Success') {
          Swal.fire({
            icon: 'success',
            title: 'Saved',
            text: this.form.scenarioId != null ? 'Issue scenario updated.' : 'Issue scenario created.',
            confirmButtonText: 'OK',
          });
          this.resetForm();
          this.page = 1;
          this.loadList();
        } else {
          this.showAlert(response?.serviceResponse || 'Save failed.', 'danger');
        }
      },
      () => {
        this.saving = false;
        this.showAlert('Save failed.', 'danger');
      }
    );
  }

  confirmDeactivate(row: any): void {
    Swal.fire({
      title: 'Deactivate scenario?',
      text: row.scenarioLabel,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Deactivate',
      cancelButtonText: 'Cancel',
    }).then((r) => {
      if (!r.isConfirmed) {
        return;
      }
      this.grievanceService
        .deactivateAdminIssueScenario(row.scenarioId)
        .pipe(first())
        .subscribe(
          (response: any) => {
            if (response?.serviceStatus === 'Success') {
              Swal.fire({ icon: 'success', title: 'Deactivated', confirmButtonText: 'OK' });
              this.loadList();
            } else {
              Swal.fire({ icon: 'error', title: 'Error', text: response?.serviceResponse || 'Failed' });
            }
          },
          () => Swal.fire({ icon: 'error', title: 'Error', text: 'Request failed.' })
        );
    });
  }

  goBack(): void {
    this.router.navigate(['/grievance']);
  }

  /** Fetches every scenario (paginated API, up to 100 per request) and downloads Excel. */
  exportAllScenarios(): void {
    if (this.exportLoading || this.totalElements === 0) {
      return;
    }
    this.exportLoading = true;
    const sortCol = this.sortColumn;
    const sortDir = this.sortDirection;
    const size = this.exportFetchSize;
    this.grievanceService
      .listAdminIssueScenarios(1, size, sortCol, sortDir)
      .pipe(first())
      .subscribe({
        next: (res: any) => {
          if (res?.serviceStatus !== 'Success') {
            this.exportLoading = false;
            this.showAlert(res?.serviceResponse || 'Unable to export scenarios.', 'danger');
            return;
          }
          const dto = res.serviceResponse;
          const total = dto?.totalElements ?? 0;
          let rows: any[] = [...(dto?.content || [])];
          const totalPages = Math.max(1, Math.ceil(total / size));
          if (totalPages <= 1) {
            this.exportLoading = false;
            this.writeScenarioExportWorkbook(rows);
            return;
          }
          const rest$ = [];
          for (let p = 2; p <= totalPages; p++) {
            rest$.push(this.grievanceService.listAdminIssueScenarios(p, size, sortCol, sortDir).pipe(first()));
          }
          forkJoin(rest$).subscribe({
            next: (results: any[]) => {
              results.forEach((r) => {
                if (r?.serviceStatus === 'Success' && Array.isArray(r?.serviceResponse?.content)) {
                  rows = rows.concat(r.serviceResponse.content);
                }
              });
              this.exportLoading = false;
              this.writeScenarioExportWorkbook(rows);
            },
            error: () => {
              this.exportLoading = false;
              this.showAlert('Export failed.', 'danger');
            },
          });
        },
        error: () => {
          this.exportLoading = false;
          this.showAlert('Export failed.', 'danger');
        },
      });
  }

  private writeScenarioExportWorkbook(raw: any[]): void {
    const data = raw.map((row, idx) => ({
      'Sr No.': idx + 1,
      'Scenario ID': row.scenarioId,
      Category: this.displayModuleName(row),
      'Sub-category': row.subFeatureName || '',
      Feature: row.featureName || '',
      Label: row.scenarioLabel || '',
      Active: row.isActive === 1 ? 'Yes' : 'No',
      'Tab key (storage)': row.tabKey || '',
    }));
    const stamp = new Date().toISOString().slice(0, 10);
    this.exportExcelService.exportTableDataToExcel(data, `grievance-issue-scenarios-${stamp}.xlsx`);
  }

  private showAlert(message: string, type: 'success' | 'danger' | 'warning'): void {
    this.alertMessage = message;
    this.alertType = type;
  }

  /** Table: prefer API-enriched tab name, else readable fallback from stored key. */
  displayModuleName(row: any): string {
    const n = (row?.categoryDisplayName || '').trim();
    if (n) {
      return n;
    }
    return this.displayTabKey(row?.tabKey);
  }

  displayTabKey(t: string): string {
    const x = (t || '').toLowerCase();
    if (x === 'timesheet') {
      return 'Timesheet';
    }
    if (x === 'leave') {
      return 'Leave';
    }
    if (!t) {
      return '—';
    }
    return t.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
  }

  private guessCategoryFromTabKey(tabKey: string): string {
    const k = (tabKey || '').toLowerCase();
    if (!k) {
      return '';
    }
    const byLegacy =
      this.allCategories.find((c) => k === 'timesheet' && (c || '').toLowerCase().includes('timesheet')) ||
      this.allCategories.find((c) => k === 'leave' && (c || '').toLowerCase().includes('leave'));
    if (byLegacy) {
      return byLegacy;
    }
    const slug = k.replace(/_/g, '');
    return (
      this.allCategories.find((c) => (c || '').toLowerCase().replace(/[^a-z0-9]/g, '') === slug) || ''
    );
  }
}
