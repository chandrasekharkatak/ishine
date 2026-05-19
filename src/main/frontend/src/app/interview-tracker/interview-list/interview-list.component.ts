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

@Component({
    standalone: false,
    selector: 'app-interview-list',
    templateUrl: './interview-list.component.html',
    styleUrls: ['./interview-list.component.css']
})
export class InterviewListComponent implements OnInit, OnChanges {

    @Input() filterObj: any = {};
    @Input() onEditRequest: any;

    @ViewChild('jdModal') jdModalTemplate: TemplateRef<any>;
    @ViewChild('feedbackModal') feedbackModalTemplate: TemplateRef<any>;
    @ViewChild('viewModal') viewModalTemplate: TemplateRef<any>;
    @ViewChild('statusModal') statusModalTemplate: TemplateRef<any>;
    @ViewChild('resumeModal') resumeModalTemplate: TemplateRef<any>;

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

    modalRef: NgbModalRef;
    selectedInterview: Interview = new Interview();
    statusForm: any = {};
    resumeUrl: SafeResourceUrl = '';

    private baseUrl: string = environment.baseUrl;

    columns: any[] = [
        'title', 'date', 'client', 'role', 'project', 'departmentName',
        'employeeName', 'mode', 'interviewStatus', 'selectionStatus',
        'onboardingStatus', 'jd', 'scheduledByName', 'interviewerName'
    ];

    constructor(
        private interviewTrackerService: InterviewTrackerService,
        private modalService: NgbModal,
        private sanitizer: DomSanitizer,
        private http: HttpClient
    ) { }

    ngOnInit(): void {
        this.loadData();
    }

    ngOnChanges(): void {
        if (this.filterObj) {
            this.page = 1;
            this.loadData();
        }
    }

    loadData(): void {
        const payload = {
            startDate: this.filterObj.startDate || null,
            endDate: this.filterObj.endDate || null,
            clients: this.filterObj.clients && this.filterObj.clients.length > 0 ? this.filterObj.clients : null,
            departmentIds: this.filterObj.departmentIds && this.filterObj.departmentIds.length > 0 ? this.filterObj.departmentIds : null,
            employeeIds: this.filterObj.employeeIds && this.filterObj.employeeIds.length > 0 ? this.filterObj.employeeIds : null,
            sortColumn: this.sortColumn,
            sortDirection: this.sortDirection,
            page: 0,
            size: 1000
        };

        this.interviewTrackerService.getInterviewList(payload).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.interviewList = response.serviceResponse;
                this.totalCount = response.totalElements || this.interviewList.length;
                this.applyClientFilters();
            }
        });
    }

    applyClientFilters(): void {
        let list = [...this.interviewList];
        if (Object.keys(this.filters).length > 0) {
            list = list.filter(item => {
                return Object.keys(this.filters).every(key => {
                    if (!this.filters[key]) return true;
                    const val = (item[key] || '').toString().toLowerCase();
                    return val.includes(this.filters[key].toLowerCase());
                });
            });
        }
        this.filteredList = list;
    }

    sortData(sort: Sort): void {
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

    onSearch(filterValues: any): void {
        this.filters = filterValues;
        this.page = 1;
        this.applyClientFilters();
    }

    toggleSearch(): void {
        this.isSearchEnabled = !this.isSearchEnabled;
        if (!this.isSearchEnabled) {
            this.filters = {};
            this.applyClientFilters();
        }
    }

    handlePageChange(event: number): void {
        this.page = event;
    }

    openJdModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        this.modalRef = this.modalService.open(this.jdModalTemplate, { size: 'lg', centered: true });
    }

    openResumeModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        const fileName = interview.resumeFilePath.replace(/^\//, '');
        const url = `${this.baseUrl}api/interview/resume/${fileName}`;

        this.http.get(url, { responseType: 'blob' }).subscribe((blob: Blob) => {
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

        this.http.get(url, { responseType: 'blob' }).subscribe((blob: Blob) => {
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

    openEditModal(interview: Interview): void {
        const modalRef = this.modalService.open(ScheduleInterviewComponent, {
            size: 'lg',
            centered: true,
            backdrop: 'static'
        });
        const componentInstance = modalRef.componentInstance as ScheduleInterviewComponent;
        componentInstance.setEditMode(interview);
        modalRef.result.then((result) => {
            if (result === 'saved') {
                this.loadData();
            }
        }).catch(() => { });
    }

    openStatusModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        this.statusForm = {
            interviewStatus: interview.interviewStatus || 'Scheduled',
            selectionStatus: interview.selectionStatus || 'Pending',
            onboardingStatus: interview.onboardingStatus || 'Not Applicable',
            interviewRemarks: interview.interviewRemarks || '',
            selectionRemarks: interview.selectionRemarks || '',
            onboardingRemarks: interview.onboardingRemarks || ''
        };
        this.modalRef = this.modalService.open(this.statusModalTemplate, { size: 'md', centered: true });
    }

    saveStatus(): void {
        const updateObj = new Interview();
        updateObj.id = this.selectedInterview.id;
        updateObj.interviewStatus = this.statusForm.interviewStatus;
        updateObj.selectionStatus = this.statusForm.selectionStatus;
        updateObj.onboardingStatus = this.statusForm.onboardingStatus;
        updateObj.interviewRemarks = this.statusForm.interviewRemarks;
        updateObj.selectionRemarks = this.statusForm.selectionRemarks;
        updateObj.onboardingRemarks = this.statusForm.onboardingRemarks;
        this.interviewTrackerService.updateInterview(updateObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success') {
                this.loadData();
                this.modalRef.close();
            }
        });
    }

    openFeedbackModal(interview: Interview): void {
        this.selectedInterview = { ...interview };
        this.modalRef = this.modalService.open(this.feedbackModalTemplate, { size: 'lg', centered: true });
    }

    saveFeedback(): void {
        const updateObj = new Interview();
        updateObj.id = this.selectedInterview.id;
        updateObj.interviewRemarks = this.selectedInterview.interviewRemarks;
        updateObj.selectionRemarks = this.selectedInterview.selectionRemarks;
        updateObj.onboardingRemarks = this.selectedInterview.onboardingRemarks;
        this.interviewTrackerService.updateInterview(updateObj).pipe(first()).subscribe((response: any) => {
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
        return moment(dateStr).format('DD MMM YYYY');
    }
}
