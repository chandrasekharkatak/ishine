import { Component, OnInit, Input, OnChanges, TemplateRef, ViewChild, HostListener } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { InterviewTrackerService } from 'src/app/services/interview-tracker.service';
import { Interview } from 'src/app/models/interview';
import { ScheduleInterviewComponent } from '../schedule-interview/schedule-interview.component';
import { first } from 'rxjs/operators';
import * as moment from 'moment';
import { environment } from 'src/environments/environment';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import * as XLSX from 'xlsx-js-style';
import { InterviewUserMapping } from '../interview-permissions';
import {
    hasStatusChangeDate,
    requiresStatusChangeDate
} from '../interview-status.helpers';

export type InterviewAggregateBy = 'client' | 'department' | 'employee' | 'project';

export type StatusBreakdown = Record<string, number>;

export interface InterviewAggregateRow {
    displayLabel: string;
    totalCount: number;
    items: Interview[];
    interviewStatusCounts: StatusBreakdown;
    selectionStatusCounts: StatusBreakdown;
    onboardingStatusCounts: StatusBreakdown;
}

@Component({
    standalone: false,
    selector: 'app-interview-list',
    templateUrl: './interview-list.component.html',
    styleUrls: ['./interview-list.component.css']
})
export class InterviewListComponent implements OnInit, OnChanges {

    @Input() filterObj: any = {};
    @Input() onEditRequest: any;
    @Input() userMapping: InterviewUserMapping = {
        view_interview: false,
        schedule_interview: false,
        edit_interview: false,
        delete_interview: false
    };

    @ViewChild('jdModal') jdModalTemplate: TemplateRef<any>;
    @ViewChild('viewModal') viewModalTemplate: TemplateRef<any>;
    @ViewChild('statusModal') statusModalTemplate: TemplateRef<any>;
    @ViewChild('resumeModal') resumeModalTemplate: TemplateRef<any>;

    /** Max rows fetched when building aggregated view (same filters as list). */
    private readonly aggregateFetchSize = 50000;

    aggregatedView = false;
    aggregateBy = '';
    readonly aggregateByOptions: { value: InterviewAggregateBy; label: string }[] = [
        { value: 'department', label: 'Department' },
        { value: 'project', label: 'Project' },
        { value: 'client', label: 'Client' },
        { value: 'employee', label: 'Employee' }
    ];

    private readonly interviewStatusOrder = ['Scheduled', 'Completed', 'Cancelled', 'Rescheduled'];
    private readonly selectionStatusOrder = ['Pending', 'Selected', 'Rejected', 'Hold'];
    private readonly onboardingStatusOrder = ['Not Applicable', 'Pending', 'Onboarded', 'Dropped'];

    aggregatedRows: InterviewAggregateRow[] = [];
    aggregateInterviewStatusKeys: string[] = [];
    aggregateSelectionStatusKeys: string[] = [];
    aggregateOnboardingStatusKeys: string[] = [];
    /** Full interview set used to build aggregated rows (also used for detail export). */
    aggregateSourceList: Interview[] = [];
    expandedAggregateLabel: string | null = null;

    interviewList: Interview[] = [];
    filteredList: Interview[] = [];
    totalCount: number = 0;
    page: number = 1;
    itemsPerPage: number = 20;

    sortDirection: string = 'desc';
    sortColumn: any = 'date';
    sortColumnType: any = 'date';

    isSearchEnabled: boolean = false;
    filters: any = {};
    columnFilters: any = {};

    modalRef: NgbModalRef;
    selectedInterview: Interview = new Interview();
    statusForm: any = {};
    statusFormErrors: { [key: string]: string } = {};
    private originalSelectionStatus = 'Pending';
    private originalOnboardingStatus = 'Not Applicable';
    resumeUrl: SafeResourceUrl = '';

    private baseUrl: string = environment.baseUrl;

    columns: any[] = [
        'blank', 'blank', 'title', 'date', 'client', 'role', 'project', 'departmentName',
        'employeeName', 'mode', 'interviewStatus', 'selectionStatus',
        'onboardingStatus', 'jd', 'blank', 'scheduledByName', 'interviewerName'
    ];

    constructor(
        private interviewTrackerService: InterviewTrackerService,
        private modalService: NgbModal,
        private sanitizer: DomSanitizer,
        private http: HttpClient
    ) { }

    ngOnInit(): void {
        if (this.userMapping.view_interview) {
            this.loadData();
        }
    }

    ngOnChanges(): void {
        if (this.filterObj && this.userMapping.view_interview) {
            this.page = 1;
            this.loadData();
        }
    }

    loadData(): void {
        if (this.aggregatedView) {
            if (!this.aggregateBy) {
                this.interviewList = [];
                this.filteredList = [];
                this.aggregatedRows = [];
                this.aggregateSourceList = [];
                this.aggregateInterviewStatusKeys = [];
                this.aggregateSelectionStatusKeys = [];
                this.aggregateOnboardingStatusKeys = [];
                this.expandedAggregateLabel = null;
                this.totalCount = 0;
                return;
            }
            this.loadAggregatedData();
            return;
        }

        const payload = {
            startDate: this.filterObj.startDate || null,
            endDate: this.filterObj.endDate || null,
            clients: this.filterObj.clients && this.filterObj.clients.length > 0 ? this.filterObj.clients : null,
            departmentIds: this.filterObj.departmentIds && this.filterObj.departmentIds.length > 0 ? this.filterObj.departmentIds : null,
            employeeIds: this.filterObj.employeeIds && this.filterObj.employeeIds.length > 0 ? this.filterObj.employeeIds : null,
            sortColumn: this.sortColumn,
            sortDirection: this.sortDirection,
            page: this.page - 1,
            size: this.itemsPerPage,
            titleFilter: this.columnFilters.title || null,
            dateFilter: this.columnFilters.date || null,
            clientFilter: this.columnFilters.client || null,
            roleFilter: this.columnFilters.role || null,
            projectFilter: this.columnFilters.project || null,
            departmentNameFilter: this.columnFilters.departmentName || null,
            employeeNameFilter: this.columnFilters.employeeName || null,
            modeFilter: this.columnFilters.mode || null,
            interviewStatusFilter: this.columnFilters.interviewStatus || null,
            selectionStatusFilter: this.columnFilters.selectionStatus || null,
            onboardingStatusFilter: this.columnFilters.onboardingStatus || null,
            jdFilter: this.columnFilters.jd || null,
            scheduledByNameFilter: this.columnFilters.scheduledByName || null,
            interviewerNameFilter: this.columnFilters.interviewerName || null
        };

        this.interviewTrackerService.getInterviewList(payload).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.interviewList = response.serviceResponse;
                this.filteredList = response.serviceResponse;
                this.totalCount = response.totalElements || 0;
            }
        });
    }

    private loadAggregatedData(): void {
        const payload = {
            startDate: this.filterObj.startDate || null,
            endDate: this.filterObj.endDate || null,
            clients: this.filterObj.clients && this.filterObj.clients.length > 0 ? this.filterObj.clients : null,
            departmentIds: this.filterObj.departmentIds && this.filterObj.departmentIds.length > 0 ? this.filterObj.departmentIds : null,
            employeeIds: this.filterObj.employeeIds && this.filterObj.employeeIds.length > 0 ? this.filterObj.employeeIds : null,
            sortColumn: this.sortColumn,
            sortDirection: this.sortDirection,
            page: 0,
            size: this.aggregateFetchSize,
            titleFilter: this.columnFilters.title || null,
            dateFilter: this.columnFilters.date || null,
            clientFilter: this.columnFilters.client || null,
            roleFilter: this.columnFilters.role || null,
            projectFilter: this.columnFilters.project || null,
            departmentNameFilter: this.columnFilters.departmentName || null,
            employeeNameFilter: this.columnFilters.employeeName || null,
            modeFilter: this.columnFilters.mode || null,
            interviewStatusFilter: this.columnFilters.interviewStatus || null,
            selectionStatusFilter: this.columnFilters.selectionStatus || null,
            onboardingStatusFilter: this.columnFilters.onboardingStatus || null,
            jdFilter: this.columnFilters.jd || null,
            scheduledByNameFilter: this.columnFilters.scheduledByName || null,
            interviewerNameFilter: this.columnFilters.interviewerName || null
        };

        this.interviewTrackerService.getInterviewList(payload).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                const list: Interview[] = response.serviceResponse;
                this.interviewList = [];
                this.filteredList = [];
                this.aggregateSourceList = list;
                this.refreshAggregateStatusColumns(list);
                this.aggregatedRows = this.buildAggregatedRows(list, this.aggregateBy as InterviewAggregateBy);
                this.expandedAggregateLabel = null;
                this.totalCount = this.aggregatedRows.length;
            } else {
                this.aggregateSourceList = [];
                this.aggregatedRows = [];
                this.aggregateInterviewStatusKeys = [];
                this.aggregateSelectionStatusKeys = [];
                this.aggregateOnboardingStatusKeys = [];
                this.totalCount = 0;
            }
        });
    }

    private getAggregateGroupKey(item: Interview, by: InterviewAggregateBy): string {
        if (by === 'client') {
            return (item.client != null && String(item.client).trim() !== '') ? String(item.client).trim() : '(No client)';
        }
        if (by === 'department') {
            return (item.departmentName != null && String(item.departmentName).trim() !== '')
                ? String(item.departmentName).trim()
                : '(No department)';
        }
        if (by === 'project') {
            return (item.project != null && String(item.project).trim() !== '')
                ? String(item.project).trim()
                : '(No project)';
        }
        return (item.employeeName != null && String(item.employeeName).trim() !== '')
            ? String(item.employeeName).trim()
            : '(No employee)';
    }

    private normalizeStatus(value: any): string {
        if (value == null || String(value).trim() === '') {
            return '(Not set)';
        }
        return String(value).trim();
    }

    private countStatusField(items: Interview[], field: 'interviewStatus' | 'selectionStatus' | 'onboardingStatus'): StatusBreakdown {
        const counts: StatusBreakdown = {};
        for (const item of items) {
            const key = this.normalizeStatus(item[field]);
            counts[key] = (counts[key] || 0) + 1;
        }
        return counts;
    }

    private collectStatusKeys(list: Interview[], field: 'interviewStatus' | 'selectionStatus' | 'onboardingStatus', knownOrder: string[]): string[] {
        const keys = new Set<string>();
        for (const item of list) {
            keys.add(this.normalizeStatus(item[field]));
        }
        const ordered: string[] = [];
        for (const status of knownOrder) {
            if (keys.has(status)) {
                ordered.push(status);
                keys.delete(status);
            }
        }
        return [...ordered, ...Array.from(keys).sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }))];
    }

    private refreshAggregateStatusColumns(list: Interview[]): void {
        this.aggregateInterviewStatusKeys = this.collectStatusKeys(list, 'interviewStatus', this.interviewStatusOrder);
        this.aggregateSelectionStatusKeys = this.collectStatusKeys(list, 'selectionStatus', this.selectionStatusOrder);
        this.aggregateOnboardingStatusKeys = this.collectStatusKeys(list, 'onboardingStatus', this.onboardingStatusOrder);
    }

    getAggregateTableColspan(): number {
        return 4 + this.aggregateInterviewStatusKeys.length
            + this.aggregateSelectionStatusKeys.length
            + this.aggregateOnboardingStatusKeys.length;
    }

    getStatusCount(row: InterviewAggregateRow, breakdown: 'interview' | 'selection' | 'onboarding', status: string): number {
        const map = breakdown === 'interview' ? row.interviewStatusCounts
            : breakdown === 'selection' ? row.selectionStatusCounts
                : row.onboardingStatusCounts;
        return map[status] || 0;
    }

    private buildAggregatedRows(list: Interview[], by: InterviewAggregateBy): InterviewAggregateRow[] {
        const groups = new Map<string, Interview[]>();
        for (const item of list) {
            const key = this.getAggregateGroupKey(item, by);
            if (!groups.has(key)) {
                groups.set(key, []);
            }
            groups.get(key)!.push(item);
        }
        return Array.from(groups.entries())
            .map(([displayLabel, items]) => ({
                displayLabel,
                totalCount: items.length,
                interviewStatusCounts: this.countStatusField(items, 'interviewStatus'),
                selectionStatusCounts: this.countStatusField(items, 'selectionStatus'),
                onboardingStatusCounts: this.countStatusField(items, 'onboardingStatus'),
                items: [...items].sort((a, b) => {
                    const dateA = a.date ? moment(a.date).valueOf() : 0;
                    const dateB = b.date ? moment(b.date).valueOf() : 0;
                    return dateB - dateA;
                })
            }))
            .sort((a, b) => b.totalCount - a.totalCount || a.displayLabel.localeCompare(b.displayLabel));
    }

    toggleAggregateRow(row: InterviewAggregateRow): void {
        this.expandedAggregateLabel = this.expandedAggregateLabel === row.displayLabel ? null : row.displayLabel;
    }

    isAggregateRowExpanded(row: InterviewAggregateRow): boolean {
        return this.expandedAggregateLabel === row.displayLabel;
    }

    onAggregatedViewChange(): void {
        if (this.aggregatedView) {
            this.aggregateBy = 'department';
        } else {
            this.aggregateBy = '';
            this.aggregatedRows = [];
            this.aggregateSourceList = [];
            this.aggregateInterviewStatusKeys = [];
            this.aggregateSelectionStatusKeys = [];
            this.aggregateOnboardingStatusKeys = [];
            this.expandedAggregateLabel = null;
        }
        this.page = 1;
        this.loadData();
    }

    onAggregateByChange(): void {
        this.page = 1;
        if (this.aggregatedView) {
            this.loadData();
        }
    }

    getAggregateColumnHeader(): string {
        switch (this.aggregateBy) {
            case 'client':
                return 'Client';
            case 'department':
                return 'Department';
            case 'employee':
                return 'Employee';
            case 'project':
                return 'Project';
            default:
                return 'Group';
        }
    }

    getAggregateGroupLabel(): string {
        return this.getAggregateColumnHeader();
    }

    onSearch(filterValues: any): void {
        this.columnFilters = filterValues;
        this.page = 1;
        this.loadData();
    }

    sortData(sort: Sort): void {
        if (this.aggregatedView) {
            if (!sort.active || sort.direction === '') {
                this.aggregatedRows = [...this.aggregatedRows].sort(
                    (a, b) => b.totalCount - a.totalCount || a.displayLabel.localeCompare(b.displayLabel)
                );
            } else {
                const headerParts = sort.active.split('|');
                const field = headerParts[0];
                const dir = sort.direction === 'asc' ? 1 : -1;
                this.aggregatedRows = [...this.aggregatedRows].sort((a, b) => {
                    if (field === 'count') {
                        return (a.totalCount - b.totalCount) * dir;
                    }
                    return a.displayLabel.localeCompare(b.displayLabel, undefined, { sensitivity: 'base' }) * dir;
                });
            }
            return;
        }

        if (!sort.active || sort.direction === '') {
            this.sortDirection = 'desc';
            this.sortColumn = 'date';
        } else {
            this.sortDirection = sort.direction;
            const headerParts = sort.active.split('|');
            this.sortColumn = headerParts[0];
            this.sortColumnType = headerParts[1] || 'string';
        }
        this.page = 1;
        this.loadData();
    }

    toggleSearch(): void {
        this.isSearchEnabled = !this.isSearchEnabled;
        if (!this.isSearchEnabled) {
            this.columnFilters = {};
            this.page = 1;
            this.loadData();
        }
    }

    handlePageChange(event: number): void {
        this.page = event;
        this.loadData();
    }

    openJdModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        this.modalRef = this.modalService.open(this.jdModalTemplate, { size: 'lg', centered: true });
    }

    openResumeModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        const fileName = interview.resumeFilePath.replace(/^\//, '');
        const url = `${this.baseUrl}api/interview/resume/${fileName}`;

        this.http.get(url, { responseType: 'blob', headers: { loader: 'true' } }).subscribe((blob: Blob) => {
            const objectUrl = URL.createObjectURL(blob);
            this.resumeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
            this.modalRef = this.modalService.open(this.resumeModalTemplate, { size: 'xl', centered: true });

            this.modalRef.result.then(() => {
                URL.revokeObjectURL(objectUrl);
            }).catch(() => {
                URL.revokeObjectURL(objectUrl);
            });
        });
    }

    openResumeInNewTab(): void {
        const fileName = this.selectedInterview.resumeFilePath.replace(/^\//, '');
        const url = `${this.baseUrl}api/interview/resume/${fileName}`;

        this.http.get(url, { responseType: 'blob', headers: { loader: 'true' } }).subscribe((blob: Blob) => {
            const objectUrl = URL.createObjectURL(blob);
            window.open(objectUrl, '_blank');
        });
    }

    getResumeUrl(filePath: string): string {
        if (!filePath) return '';
        const fileName = filePath.replace(/^\//, '');
        return `${this.baseUrl}api/interview/resume/${fileName}`;
    }

    openViewModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        this.modalRef = this.modalService.open(this.viewModalTemplate, { size: 'lg', centered: true });
    }

    deleteInterview(item: Interview): void {
        if (!this.userMapping.delete_interview || !item?.id) {
            return;
        }
        if (!confirm('Delete this interview? This cannot be undone.')) {
            return;
        }
        this.interviewTrackerService.deleteInterview(item.id).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success') {
                this.loadData();
            }
        });
    }

    openEditModal(interview: Interview): void {
        if (!this.userMapping.edit_interview) {
            return;
        }
        const modalRef = this.modalService.open(ScheduleInterviewComponent, {
            size: 'lg',
            centered: true,
            backdrop: 'static'
        });
        const componentInstance = modalRef.componentInstance as ScheduleInterviewComponent;
        componentInstance.setEditMode(interview);
        modalRef.result.then((result) => {
            if (result === 'saved') {
                this.page = 1;
                this.loadData();
            }
        }).catch(() => { });
    }

    openStatusModal(interview: Interview): void {
        if (!this.userMapping.edit_interview) {
            return;
        }
        this.selectedInterview = { ...interview };
        this.originalSelectionStatus = interview.selectionStatus || 'Pending';
        this.originalOnboardingStatus = interview.onboardingStatus || 'Not Applicable';
        this.statusFormErrors = {};
        this.statusForm = {
            interviewStatus: interview.interviewStatus || 'Scheduled',
            selectionStatus: interview.selectionStatus || 'Pending',
            onboardingStatus: interview.onboardingStatus || 'Not Applicable',
            interviewStatusChangeDate: interview.interviewStatusChangeDate || null,
            selectionStatusChangeDate: interview.selectionStatusChangeDate || null,
            onboardingStatusChangeDate: interview.onboardingStatusChangeDate || null,
            interviewRemarks: interview.interviewRemarks || '',
            selectionRemarks: interview.selectionRemarks || '',
            onboardingRemarks: interview.onboardingRemarks || ''
        };
        this.modalRef = this.modalService.open(this.statusModalTemplate, { size: 'xl', centered: true });
    }

    isSelectionStatusChangeDateRequired(): boolean {
        return requiresStatusChangeDate(this.originalSelectionStatus, this.statusForm.selectionStatus);
    }

    isOnboardingStatusChangeDateRequired(): boolean {
        return requiresStatusChangeDate(this.originalOnboardingStatus, this.statusForm.onboardingStatus);
    }

    private validateStatusForm(): boolean {
        this.statusFormErrors = {};
        if (this.isSelectionStatusChangeDateRequired() && !hasStatusChangeDate(this.statusForm.selectionStatusChangeDate)) {
            this.statusFormErrors['selectionStatusChangeDate'] = 'Selection status change date is required when moving from Pending';
        }
        if (this.isOnboardingStatusChangeDateRequired() && !hasStatusChangeDate(this.statusForm.onboardingStatusChangeDate)) {
            this.statusFormErrors['onboardingStatusChangeDate'] = 'Onboarding status change date is required when moving from Not Applicable or Pending';
        }
        return Object.keys(this.statusFormErrors).length === 0;
    }

    saveStatus(): void {
        if (!this.validateStatusForm()) {
            return;
        }
        const updateObj = new Interview();
        updateObj.id = this.selectedInterview.id;
        updateObj.interviewStatus = this.statusForm.interviewStatus;
        updateObj.selectionStatus = this.statusForm.selectionStatus;
        updateObj.onboardingStatus = this.statusForm.onboardingStatus;
        updateObj.interviewStatusChangeDate = this.statusForm.interviewStatusChangeDate;
        updateObj.selectionStatusChangeDate = this.statusForm.selectionStatusChangeDate;
        updateObj.onboardingStatusChangeDate = this.statusForm.onboardingStatusChangeDate;
        updateObj.interviewRemarks = this.statusForm.interviewRemarks;
        updateObj.selectionRemarks = this.statusForm.selectionRemarks;
        updateObj.onboardingRemarks = this.statusForm.onboardingRemarks;
        this.interviewTrackerService.updateInterviewStatus(updateObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success') {
                this.loadData();
                this.modalRef.close();
            }
        });
    }

    getStatusClass(status: string): string {
        if (!status) return '';
        const s = status.toLowerCase();
        if (s === 'scheduled' || s === 'pending') return 'badge-warning';
        if (s === 'completed' || s === 'selected' || s === 'onboarded') return 'badge-success';
        if (s === 'cancelled' || s === 'rejected') return 'badge-danger';
        if (s === 'rescheduled' || s === 'hold') return 'badge-info';
        return 'badge-secondary';
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return moment(dateStr).format('DD-MM-YYYY');
    }

    formatStatusChangeDate(dateStr: string | null | undefined): string {
        if (!dateStr || !String(dateStr).trim()) {
            return '—';
        }
        return this.formatDate(dateStr);
    }

    private mapInterviewToExportRow(item: Interview): Record<string, string | number> {
        return {
            'Title': item.title,
            'Date': this.formatDate(item.date),
            'Client': item.client,
            'Role': item.role,
            'Project': item.project || 'N/A',
            'Department': item.departmentName,
            'Employee': item.employeeName,
            'Mode': item.mode,
            'Interview Status': item.interviewStatus,
            'Interview Status Change Date': this.formatDate(item.interviewStatusChangeDate),
            'Selection Status': item.selectionStatus,
            'Selection Status Change Date': this.formatDate(item.selectionStatusChangeDate),
            'Onboarding Status': item.onboardingStatus,
            'Onboarding Status Change Date': this.formatDate(item.onboardingStatusChangeDate),
            'Interviewer': item.interviewerName || 'N/A',
            'Scheduled By': item.scheduledByName || 'N/A',
            'JD': item.jd ? (item.jd.length > 100 ? item.jd.substring(0, 100) + '...' : item.jd) : 'N/A',
            'Resume': item.resumeFileName || 'N/A'
        };
    }

    private applyExcelHeaderStyle(worksheet: XLSX.WorkSheet): void {
        const headerStyle = {
            font: { bold: true, color: { rgb: 'FFFFFF' } },
            fill: { fgColor: { rgb: '132B67' } },
            alignment: { horizontal: 'center', vertical: 'center' }
        };
        const range = XLSX.utils.decode_range(worksheet['!ref'] || 'A1');
        for (let C = range.s.c; C <= range.e.c; ++C) {
            const address = XLSX.utils.encode_col(C) + '1';
            if (!worksheet[address]) continue;
            worksheet[address].s = headerStyle;
        }
    }

    private readonly detailExportColWidths = [
        { wch: 30 }, { wch: 12 }, { wch: 20 }, { wch: 20 }, { wch: 25 },
        { wch: 15 }, { wch: 20 }, { wch: 10 }, { wch: 18 }, { wch: 18 },
        { wch: 18 }, { wch: 20 }, { wch: 15 }, { wch: 40 }, { wch: 20 }
    ];

    exportToExcel(): void {
        if (this.aggregatedView) {
            const label = this.getAggregateGroupLabel();
            const aggregateExportData = this.aggregatedRows.map(row => {
                const exportRow: Record<string, string | number> = {
                    [label]: row.displayLabel,
                    'Total interviews': row.totalCount
                };
                for (const status of this.aggregateInterviewStatusKeys) {
                    exportRow[`Interview - ${status}`] = this.getStatusCount(row, 'interview', status);
                }
                for (const status of this.aggregateSelectionStatusKeys) {
                    exportRow[`Selection - ${status}`] = this.getStatusCount(row, 'selection', status);
                }
                for (const status of this.aggregateOnboardingStatusKeys) {
                    exportRow[`Onboarding - ${status}`] = this.getStatusCount(row, 'onboarding', status);
                }
                return exportRow;
            });
            const aggregateSheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(aggregateExportData);
            this.applyExcelHeaderStyle(aggregateSheet);

            const detailExportData = this.aggregateSourceList.map(item => this.mapInterviewToExportRow(item));
            const detailSheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(
                detailExportData.length > 0 ? detailExportData : [{ 'Title': '' }]
            );
            detailSheet['!cols'] = this.detailExportColWidths;
            this.applyExcelHeaderStyle(detailSheet);

            const workbook: XLSX.WorkBook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, aggregateSheet, 'Aggregated');
            XLSX.utils.book_append_sheet(workbook, detailSheet, 'All details');
            XLSX.writeFile(workbook, 'Interview_Aggregated_' + moment().format('DD-MM-YYYY') + '.xlsx');
            return;
        }

        const exportData = this.filteredList.map(item => this.mapInterviewToExportRow(item));
        const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
        const workbook: XLSX.WorkBook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Interview List');
        worksheet['!cols'] = this.detailExportColWidths;
        this.applyExcelHeaderStyle(worksheet);
        XLSX.writeFile(workbook, 'Interview_List_' + moment().format('DD-MM-YYYY') + '.xlsx');
    }
}
