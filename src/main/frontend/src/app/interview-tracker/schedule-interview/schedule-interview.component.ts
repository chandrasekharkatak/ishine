import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { InterviewTrackerService } from 'src/app/services/interview-tracker.service';
import { Interview } from 'src/app/models/interview';
import { first } from 'rxjs/operators';
import * as moment from 'moment';
import { environment } from 'src/environments/environment';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import {
    hasStatusChangeDate,
    requiresStatusChangeDate,
    requiresStatusChangeDateOnSet
} from '../interview-status.helpers';

@Component({
    standalone: false,
    selector: 'app-schedule-interview',
    templateUrl: './schedule-interview.component.html',
    styleUrls: ['./schedule-interview.component.css']
})
export class ScheduleInterviewComponent implements OnInit {

    interviewForm: Interview = new Interview();
    clientList: any[] = [];
    departmentList: any[] = [];
    employeeList: any[] = [];
    filteredEmployeeList: any[] = [];
    projectList: any[] = [];
    selectedDepartment: any = null;
    selectedEmployee: any = null;
    selectedClient: string = '';

    filteredClientList: any[] = [];
    filteredProjectList: any[] = [];
    clientSearchText: string = '';
    projectSearchText: string = '';
    showClientDropdown: boolean = false;
    showProjectDropdown: boolean = false;

    modes = ['Online', 'Offline', 'Telephonic'];
    interviewStatusOptions = ['Scheduled', 'Completed', 'Cancelled', 'Rescheduled'];
    selectionStatusOptions = ['Pending', 'Selected', 'Rejected', 'Hold'];
    onboardingStatusOptions = ['Not Applicable', 'Pending', 'Onboarded', 'Dropped'];
    isSubmitting: boolean = false;
    errorMessage: string = '';
    selectedFileName: string = '';
    selectedFileSize: string = '';
    fileError: string = '';
    maxFileSize = 5 * 1024 * 1024;
    isEditMode: boolean = false;
    modalTitle: string = 'Schedule Interview';
    formSubmitted: boolean = false;
    formErrors: { [key: string]: string } = {};
    isLoadingEmployees: boolean = false;
    isEmployeeDropdownDisabled: boolean = true;
    isLoadingProjects: boolean = false;
    isProjectDropdownDisabled: boolean = true;
    existingResumeUrl: SafeResourceUrl = '';
    private baseUrl: string = environment.baseUrl;
    showResumePopup: boolean = false;
    resumeBlobUrl: SafeResourceUrl = '';
    isLoadingResume: boolean = false;
    /** Set when opening edit before dropdown API returns. */
    private editContextReady: boolean = false;
    private originalSelectionStatus: string = 'Pending';
    private originalOnboardingStatus: string = 'Not Applicable';

    constructor(
        public activeModal: NgbActiveModal,
        private interviewTrackerService: InterviewTrackerService,
        private cdr: ChangeDetectorRef,
        private sanitizer: DomSanitizer,
        private http: HttpClient
    ) { }

    ngOnInit(): void {
        this.loadDropdownData();
        if (!this.isEditMode) {
            this.interviewForm.date = moment().format('YYYY-MM-DD');
            this.interviewForm.time = '10:00:00';
            this.interviewForm.interviewStatus = 'Scheduled';
            this.interviewForm.selectionStatus = 'Pending';
            this.interviewForm.onboardingStatus = 'Not Applicable';
            this.interviewForm.interviewStatusChangeDate = null;
            this.interviewForm.selectionStatusChangeDate = null;
            this.interviewForm.onboardingStatusChangeDate = null;
        }
    }

    setEditMode(interview: Interview): void {
        this.isEditMode = true;
        this.editContextReady = true;
        this.modalTitle = 'Edit Interview';
        this.interviewForm = { ...interview };
        this.originalSelectionStatus = interview.selectionStatus || 'Pending';
        this.originalOnboardingStatus = interview.onboardingStatus || 'Not Applicable';
        this.clientSearchText = interview.client || '';
        this.projectSearchText = interview.project || '';
        this.selectedClient = interview.client || '';
        if (interview.resumeFileName) {
            this.selectedFileName = interview.resumeFileName;
            this.selectedFileSize = 'Attached';
            if (interview.resumeFilePath) {
                const fileName = interview.resumeFilePath.replace(/^\//, '');
                const url = `${this.baseUrl}api/interview/resume/${fileName}`;
                this.existingResumeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
            }
        }
        this.applyEditSelectionsIfReady();
    }

    loadDropdownData(): void {
        this.interviewTrackerService.getClientList().pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.clientList = response.serviceResponse.clients || [];
                this.departmentList = response.serviceResponse.departments || [];
                this.employeeList = response.serviceResponse.employees || [];
                this.filteredClientList = [...this.clientList];
                this.filteredProjectList = [...this.projectList];
                this.applyEditSelectionsIfReady();
            }
        });
    }

    /** Run after dropdown data and/or setEditMode so department/employee bind correctly in edit mode. */
    private applyEditSelectionsIfReady(): void {
        if (!this.isEditMode || !this.editContextReady) {
            return;
        }

        const deptId = this.normalizeId(this.interviewForm.departmentId);
        if (deptId != null) {
            let dept = this.departmentList.find((d: any) => this.normalizeId(d.id) === deptId);
            if (!dept) {
                const label = this.interviewForm.departmentName || `Department ${deptId}`;
                dept = { id: deptId, name: label };
                this.departmentList = [dept, ...this.departmentList];
            }
            this.selectedDepartment = dept || null;
            this.interviewForm.departmentId = deptId;
            if (dept) {
                this.loadEmployeesForDepartment(deptId);
            }
        }

        if (this.selectedClient) {
            this.loadProjectsByClient(this.selectedClient);
        }
        this.cdr.detectChanges();
    }

    private normalizeId(value: any): number | null {
        if (value == null || value === '') {
            return null;
        }
        const n = Number(value);
        return Number.isNaN(n) ? null : n;
    }

    loadEmployeesForDepartment(deptId: number): void {
        this.isLoadingEmployees = true;
        this.isEmployeeDropdownDisabled = true;
        this.interviewTrackerService.getEmployeesByDepartment(deptId).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.filteredEmployeeList = response.serviceResponse || [];
            } else {
                this.filteredEmployeeList = [];
            }
            if (this.isEditMode && this.interviewForm.employeeId) {
                const empId = this.normalizeId(this.interviewForm.employeeId);
                let emp = this.filteredEmployeeList.find((e: any) => this.normalizeId(e.id) === empId);
                if (!emp && this.interviewForm.employeeName) {
                    emp = { id: empId, name: this.interviewForm.employeeName };
                    this.filteredEmployeeList = [emp, ...this.filteredEmployeeList];
                }
                this.selectedEmployee = emp || null;
                if (empId != null) {
                    this.interviewForm.employeeId = empId;
                }
            }
            this.isLoadingEmployees = false;
            this.isEmployeeDropdownDisabled = false;
            this.cdr.detectChanges();
        });
    }

    filterClientList(): void {
        const term = this.clientSearchText.toLowerCase();
        if (!term) {
            this.filteredClientList = [...this.clientList];
        } else {
            this.filteredClientList = this.clientList.filter(c => c.toLowerCase().includes(term));
        }
        this.showClientDropdown = true;
    }

    filterProjectList(): void {
        const term = this.projectSearchText.toLowerCase();
        if (!term) {
            this.filteredProjectList = [...this.projectList];
        } else {
            this.filteredProjectList = this.projectList.filter(p => p.name.toLowerCase().includes(term));
        }
        this.showProjectDropdown = true;
    }

    loadProjectsByClient(clientName: string): void {
        this.isLoadingProjects = true;
        this.isProjectDropdownDisabled = true;
        this.projectList = [];
        this.filteredProjectList = [];
        if (!clientName) {
            this.isLoadingProjects = false;
            this.isProjectDropdownDisabled = true;
            return;
        }
        this.interviewTrackerService.getProjectsByClient(clientName).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.projectList = response.serviceResponse || [];
                this.filteredProjectList = [...this.projectList];
            } else {
                this.projectList = [];
                this.filteredProjectList = [];
            }
            this.isLoadingProjects = false;
            this.isProjectDropdownDisabled = false;
            this.cdr.detectChanges();
        });
    }

    canAddNewProject(): boolean {
        return this.projectSearchText && !this.projectList.find(p => p.name === this.projectSearchText);
    }

    selectClient(value: string): void {
        this.interviewForm.client = value;
        this.clientSearchText = value;
        this.selectedClient = value;
        this.showClientDropdown = false;
        this.interviewForm.project = '';
        this.projectSearchText = '';
        this.isProjectDropdownDisabled = false;
        this.loadProjectsByClient(value);
    }

    selectProject(value: string): void {
        this.interviewForm.project = value;
        this.projectSearchText = value;
        this.showProjectDropdown = false;
    }

    onClientBlur(): void {
        setTimeout(() => {
            this.showClientDropdown = false;
        }, 200);
    }

    onProjectBlur(): void {
        setTimeout(() => {
            this.showProjectDropdown = false;
        }, 200);
    }

    onClientInput(): void {
        this.interviewForm.client = this.clientSearchText;
        this.filterClientList();
        const isNewClient = this.clientSearchText && !this.clientList.includes(this.clientSearchText);
        this.isProjectDropdownDisabled = !isNewClient && !this.selectedClient;
        if (isNewClient) {
            this.projectList = [];
            this.filteredProjectList = [];
        }
    }

    onProjectInput(): void {
        this.interviewForm.project = this.projectSearchText;
        this.filterProjectList();
    }

    onDepartmentChange(selected: any): void {
        this.selectedDepartment = selected;
        if (selected && selected.id) {
            this.interviewForm.departmentId = selected.id;
            this.loadEmployeesForDepartment(selected.id);
        } else {
            this.interviewForm.departmentId = null;
            this.filteredEmployeeList = [];
            this.isLoadingEmployees = false;
            this.isEmployeeDropdownDisabled = true;
        }
        this.interviewForm.employeeId = null;
        this.selectedEmployee = null;
        this.clearFieldError('department');
        this.clearFieldError('employee');
    }

    onEmployeeChange(selected: any): void {
        this.selectedEmployee = selected;
        if (selected && selected.id) {
            this.interviewForm.employeeId = selected.id;
        } else {
            this.interviewForm.employeeId = null;
        }
        this.clearFieldError('employee');
    }

    validateForm(): string | null {
        this.formErrors = {};
        this.errorMessage = '';

        const fields = ['title', 'date', 'time', 'client', 'role', 'department', 'employee', 'mode', 'jd'];
        for (const field of fields) {
            let value: any;
            switch (field) {
                case 'title': value = this.interviewForm.title; break;
                case 'date': value = this.interviewForm.date; break;
                case 'time': value = this.interviewForm.time; break;
                case 'client': value = this.interviewForm.client; break;
                case 'role': value = this.interviewForm.role; break;
                case 'department': value = this.interviewForm.departmentId; break;
                case 'employee': value = this.interviewForm.employeeId; break;
                case 'mode': value = this.interviewForm.mode; break;
                case 'jd': value = this.interviewForm.jd; break;
            }

            if (!value || (typeof value === 'string' && !value.trim())) {
                this.formErrors[field] = `${this.getFieldLabel(field)} is required`;
                return field;
            }
        }

        const statusDateError = this.validateStatusChangeDates();
        if (statusDateError) {
            return statusDateError;
        }

        this.formSubmitted = true;
        return null;
    }

    isSelectionStatusChangeDateRequired(): boolean {
        if (this.isEditMode) {
            return requiresStatusChangeDate(this.originalSelectionStatus, this.interviewForm.selectionStatus);
        }
        return requiresStatusChangeDateOnSet(this.interviewForm.selectionStatus);
    }

    isOnboardingStatusChangeDateRequired(): boolean {
        if (this.isEditMode) {
            return requiresStatusChangeDate(this.originalOnboardingStatus, this.interviewForm.onboardingStatus);
        }
        return requiresStatusChangeDateOnSet(this.interviewForm.onboardingStatus);
    }

    private validateStatusChangeDates(): string | null {
        if (this.isSelectionStatusChangeDateRequired() && !hasStatusChangeDate(this.interviewForm.selectionStatusChangeDate)) {
            this.formErrors['selectionStatusChangeDate'] = 'Selection status change date is required when moving from Pending';
            return 'selectionStatusChangeDate';
        }
        if (this.isOnboardingStatusChangeDateRequired() && !hasStatusChangeDate(this.interviewForm.onboardingStatusChangeDate)) {
            this.formErrors['onboardingStatusChangeDate'] = 'Onboarding status change date is required when moving from Not Applicable or Pending';
            return 'onboardingStatusChangeDate';
        }
        return null;
    }

    getFieldLabel(field: string): string {
        const labels: any = {
            title: 'Title', date: 'Date', time: 'Time', client: 'Client',
            role: 'Role', department: 'Department', employee: 'Employee',
            mode: 'Mode', jd: 'Job Description',
            selectionStatusChangeDate: 'Selection status change date',
            onboardingStatusChangeDate: 'Onboarding status change date'
        };
        return labels[field] || field;
    }

    focusAndScrollToError(field: string): void {
        setTimeout(() => {
            const element = document.querySelector(`[name="${field}"]`) as HTMLElement;
            if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'center' });
                if (element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement || element instanceof HTMLSelectElement) {
                    element.focus();
                } else {
                    const input = element.querySelector('input, textarea, select') as HTMLElement;
                    if (input) input.focus();
                }
            }
        }, 100);
    }

    submitForm(): void {
        const firstErrorField = this.validateForm();
        if (firstErrorField) {
            this.focusAndScrollToError(firstErrorField);
            return;
        }

        this.isSubmitting = true;

        const resumeFile = this.interviewForm.resumeFile || null;
        let apiCall: any;
        if (this.isEditMode) {
            apiCall = this.interviewTrackerService.updateInterview(this.interviewForm, resumeFile);
        } else {
            apiCall = this.interviewTrackerService.scheduleInterview(this.interviewForm, resumeFile);
        }

        apiCall.pipe(first()).subscribe((response: any) => {
            this.isSubmitting = false;
            if (response.serviceStatus === 'Success') {
                this.activeModal.close('saved');
            } else {
                this.errorMessage = this.isEditMode ? 'Failed to update interview. Please try again.' : 'Failed to schedule interview. Please try again.';
            }
        });
    }

    clearFieldError(field: string): void {
        if (this.formErrors[field]) {
            delete this.formErrors[field];
        }
    }

    onFileSelect(event: any): void {
        this.fileError = '';
        const file = event.target.files[0];
        if (!file) return;

        if (file.type !== 'application/pdf') {
            this.fileError = 'Only PDF files are allowed';
            event.target.value = '';
            return;
        }

        if (file.size > this.maxFileSize) {
            this.fileError = 'File size must not exceed 5 MB';
            event.target.value = '';
            return;
        }

        this.selectedFileName = file.name;
        this.selectedFileSize = this.formatFileSize(file.size);
        this.interviewForm.resumeFile = file;
        this.interviewForm.resumeFileName = file.name;
    }

    removeFile(event: any): void {
        event.stopPropagation();
        this.selectedFileName = '';
        this.selectedFileSize = '';
        this.fileError = '';
        this.existingResumeUrl = '';
        this.interviewForm.resumeFile = null;
        this.interviewForm.resumeFileName = null;
        this.interviewForm.resumeFilePath = null;
    }

    formatFileSize(bytes: number): string {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    closeModal(): void {
        this.activeModal.dismiss();
    }

    openResumePopup(): void {
        if (!this.interviewForm.resumeFilePath) return;

        this.isLoadingResume = true;
        this.showResumePopup = true;
        const fileName = this.interviewForm.resumeFilePath.replace(/^\//, '');
        const url = `${this.baseUrl}api/interview/resume/${fileName}`;

        this.http.get(url, { responseType: 'blob', headers: { loader: 'true' } }).subscribe((blob: Blob) => {
            const objectUrl = URL.createObjectURL(blob);
            this.resumeBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
            this.isLoadingResume = false;
            this.cdr.detectChanges();
        }, (error) => {
            this.isLoadingResume = false;
        });
    }

    closeResumePopup(): void {
        if (this.resumeBlobUrl) {
            const url = this.resumeBlobUrl.toString();
            if (url.startsWith('blob:')) {
                URL.revokeObjectURL(url.replace('unsafe:', ''));
            }
        }
        this.showResumePopup = false;
        this.resumeBlobUrl = '';
    }

    openInNewTab(): void {
        if (!this.interviewForm.resumeFilePath) return;

        const fileName = this.interviewForm.resumeFilePath.replace(/^\//, '');
        const url = `${this.baseUrl}api/interview/resume/${fileName}`;

        this.http.get(url, { responseType: 'blob', headers: { loader: 'true' } }).subscribe((blob: Blob) => {
            const objectUrl = URL.createObjectURL(blob);
            window.open(objectUrl, '_blank');
        });
    }
}
