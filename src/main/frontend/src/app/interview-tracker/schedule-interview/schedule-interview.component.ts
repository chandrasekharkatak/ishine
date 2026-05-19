import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { InterviewTrackerService } from 'src/app/services/interview-tracker.service';
import { Interview } from 'src/app/models/interview';
import { first } from 'rxjs/operators';
import * as moment from 'moment';

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

    constructor(
        public activeModal: NgbActiveModal,
        private interviewTrackerService: InterviewTrackerService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadDropdownData();
        if (!this.isEditMode) {
            this.interviewForm.date = moment().format('YYYY-MM-DD');
            this.interviewForm.time = '10:00:00';
            this.interviewForm.interviewStatus = 'Scheduled';
            this.interviewForm.selectionStatus = 'Pending';
            this.interviewForm.onboardingStatus = 'Not Applicable';
        }
    }

    setEditMode(interview: Interview): void {
        this.isEditMode = true;
        this.modalTitle = 'Edit Interview';
        this.interviewForm = { ...interview };
        this.clientSearchText = interview.client || '';
        this.projectSearchText = interview.project || '';
        this.selectedClient = interview.client || '';
        if (interview.resumeFileName) {
            this.selectedFileName = interview.resumeFileName;
            this.selectedFileSize = 'Attached';
        }
        if (this.interviewForm.departmentId) {
            this.selectedDepartment = this.departmentList.find((d: any) => d.id === this.interviewForm.departmentId) || null;
            this.loadEmployeesForDepartment(this.interviewForm.departmentId);
        }
        if (this.selectedClient) {
            this.loadProjectsByClient(this.selectedClient);
        }
    }

    loadDropdownData(): void {
        this.interviewTrackerService.getClientList().pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.clientList = response.serviceResponse.clients || [];
                this.departmentList = response.serviceResponse.departments || [];
                this.employeeList = response.serviceResponse.employees || [];
                this.filteredClientList = [...this.clientList];
                this.filteredProjectList = [...this.projectList];
            }
        });
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
                this.selectedEmployee = this.filteredEmployeeList.find((e: any) => e.id === this.interviewForm.employeeId) || null;
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

    validateForm(): boolean {
        this.formErrors = {};
        this.errorMessage = '';
        let isValid = true;

        if (!this.interviewForm.title || !this.interviewForm.title.trim()) {
            this.formErrors['title'] = 'Title is required';
            isValid = false;
        }
        if (!this.interviewForm.date) {
            this.formErrors['date'] = 'Date is required';
            isValid = false;
        }
        if (!this.interviewForm.time) {
            this.formErrors['time'] = 'Time is required';
            isValid = false;
        }
        if (!this.interviewForm.client || !this.interviewForm.client.trim()) {
            this.formErrors['client'] = 'Client is required';
            isValid = false;
        }
        if (!this.interviewForm.role || !this.interviewForm.role.trim()) {
            this.formErrors['role'] = 'Role is required';
            isValid = false;
        }
        if (!this.interviewForm.departmentId) {
            this.formErrors['department'] = 'Department is required';
            isValid = false;
        }
        if (!this.interviewForm.employeeId) {
            this.formErrors['employee'] = 'Employee is required';
            isValid = false;
        }
        if (!this.interviewForm.mode) {
            this.formErrors['mode'] = 'Mode is required';
            isValid = false;
        }
        if (!this.interviewForm.jd || !this.interviewForm.jd.trim()) {
            this.formErrors['jd'] = 'Job Description is required';
            isValid = false;
        }

        this.formSubmitted = true;
        return isValid;
    }

    clearFieldError(field: string): void {
        if (this.formErrors[field]) {
            delete this.formErrors[field];
        }
    }

    submitForm(): void {
        if (!this.validateForm()) {
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
        this.interviewForm.resumeFile = null;
        this.interviewForm.resumeFileName = null;
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
}
