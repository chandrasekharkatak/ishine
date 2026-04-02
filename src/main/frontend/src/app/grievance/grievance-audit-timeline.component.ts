import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { first } from 'rxjs/operators';
import { GrievanceService } from '../services/grievance.service';

export interface AuditFieldChange {
  fieldName: string;
  oldValue: string;
  newValue: string;
}

export interface AuditTimelineEntry {
  auditId: number;
  eventType: string;
  summary: string;
  versionNo: number;
  changedByName: string;
  changedAt: string;
  reason?: string;
  hasFieldDetails: boolean;
  fieldChanges: AuditFieldChange[];
}

@Component({
  standalone: false,
  selector: 'app-grievance-audit-timeline',
  templateUrl: './grievance-audit-timeline.component.html',
  styleUrls: ['./grievance-audit-timeline.component.css'],
})
export class GrievanceAuditTimelineComponent implements OnChanges {
  @Input() ticketId: number;
  /** When true (e.g. inside a modal), skip the collapsible header and load the timeline immediately. */
  @Input() modalMode = false;

  expanded = new Set<number>();
  sectionOpen = false;
  compareOpen = false;
  loading = false;
  error = '';
  entries: AuditTimelineEntry[] = [];
  totalElements = 0;
  page = 1;
  pageSize = 15;
  fieldFilter = '';

  versions: { versionNo: number; changedAt: string; eventType: string; summary: string; changedByName: string }[] = [];
  compareV1: number | null = null;
  compareV2: number | null = null;
  diffLoading = false;
  diffError = '';
  diffChanges: AuditFieldChange[] = [];

  constructor(private grievanceService: GrievanceService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ticketId']) {
      this.reset();
    }
    if (this.ticketId && this.modalMode && (changes['ticketId'] || changes['modalMode'])) {
      this.sectionOpen = true;
      this.loadHistory(true);
      this.loadVersions();
    }
  }

  private reset(): void {
    this.entries = [];
    this.page = 1;
    this.error = '';
    this.expanded.clear();
    this.versions = [];
    this.diffChanges = [];
  }

  toggleSection(): void {
    this.sectionOpen = !this.sectionOpen;
    if (this.sectionOpen && this.entries.length === 0 && !this.loading) {
      this.loadHistory(true);
      this.loadVersions();
    }
  }

  toggleCompare(): void {
    this.compareOpen = !this.compareOpen;
    if (this.compareOpen && this.versions.length === 0) {
      this.loadVersions();
    }
  }

  loadHistory(reset: boolean): void {
    if (!this.ticketId) {
      return;
    }
    if (reset) {
      this.page = 1;
    }
    this.loading = true;
    this.error = '';
    this.grievanceService
      .getTicketAuditHistory(this.ticketId, this.page, this.pageSize, this.fieldFilter || undefined)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.loading = false;
          if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
            this.error = response.serviceResponse || 'Unable to load activity.';
            return;
          }
          const p = response.serviceResponse;
          const next = (p.content || []) as AuditTimelineEntry[];
          this.entries = reset ? next : [...this.entries, ...next];
          this.totalElements = typeof p.totalElements === 'number' ? p.totalElements : 0;
        },
        () => {
          this.loading = false;
          this.error = 'Unable to load activity.';
        }
      );
  }

  loadMore(): void {
    if (this.entries.length >= this.totalElements) {
      return;
    }
    this.page += 1;
    this.loadHistory(false);
  }

  loadVersions(): void {
    if (!this.ticketId) {
      return;
    }
    this.grievanceService
      .getTicketAuditVersions(this.ticketId)
      .pipe(first())
      .subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success' && Array.isArray(response.serviceResponse)) {
            this.versions = response.serviceResponse;
            if (this.versions.length >= 2 && this.compareV1 == null) {
              this.compareV1 = this.versions[0].versionNo;
              this.compareV2 = this.versions[this.versions.length - 1].versionNo;
            }
          }
        },
        () => {}
      );
  }

  applyFieldFilter(): void {
    this.loadHistory(true);
  }

  toggleExpand(auditId: number): void {
    if (this.expanded.has(auditId)) {
      this.expanded.delete(auditId);
    } else {
      this.expanded.add(auditId);
    }
  }

  isExpanded(auditId: number): boolean {
    return this.expanded.has(auditId);
  }

  eventLabel(type: string): string {
    return (type || '').replace(/_/g, ' ');
  }

  /** Two-letter avatar label from display name */
  actorInitials(name: string | null | undefined): string {
    const t = (name || '').trim();
    if (!t) {
      return '?';
    }
    const parts = t.split(/\s+/).filter(Boolean);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    return t.slice(0, 2).toUpperCase();
  }

  runCompare(): void {
    if (this.compareV1 == null || this.compareV2 == null || !this.ticketId) {
      return;
    }
    this.diffLoading = true;
    this.diffError = '';
    this.diffChanges = [];
    this.grievanceService
      .getTicketAuditDiff(this.ticketId, this.compareV1, this.compareV2)
      .pipe(first())
      .subscribe(
        (response: any) => {
          this.diffLoading = false;
          if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
            this.diffError = response.serviceResponse || 'Unable to compare versions.';
            return;
          }
          this.diffChanges = response.serviceResponse.changes || [];
        },
        () => {
          this.diffLoading = false;
          this.diffError = 'Unable to compare versions.';
        }
      );
  }
}
