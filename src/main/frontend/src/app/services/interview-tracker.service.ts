import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Interview } from '../models/interview';
import { environment } from 'src/environments/environment';
import * as moment from 'moment';

@Injectable({
    providedIn: 'root'
})
export class InterviewTrackerService {

    private baseUrl: any = environment.baseUrl;

    constructor(private http: HttpClient) { }

    getInterviewList(filterObj: any) {
        const payload = {
            startDate: filterObj.startDate || null,
            endDate: filterObj.endDate || null,
            clients: filterObj.clients && filterObj.clients.length > 0 ? filterObj.clients : null,
            departmentIds: filterObj.departmentIds && filterObj.departmentIds.length > 0 ? filterObj.departmentIds : null,
            employeeIds: filterObj.employeeIds && filterObj.employeeIds.length > 0 ? filterObj.employeeIds : null,
            sortColumn: filterObj.sortColumn || null,
            sortDirection: filterObj.sortDirection || null,
            page: 0,
            size: 1000
        };
        const formData = new FormData();
        formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        return this.http.post(`${this.baseUrl}api/getAllInterviews`, formData);
    }

    scheduleInterview(interviewObj: Interview, resumeFile?: File) {
        const payload = {
            title: interviewObj.title,
            date: interviewObj.date,
            time: interviewObj.time,
            client: interviewObj.client,
            role: interviewObj.role,
            project: interviewObj.project,
            departmentId: interviewObj.departmentId,
            employeeId: interviewObj.employeeId,
            mode: interviewObj.mode,
            interviewStatus: interviewObj.interviewStatus,
            selectionStatus: interviewObj.selectionStatus,
            onboardingStatus: interviewObj.onboardingStatus,
            jd: interviewObj.jd,
            interviewerName: interviewObj.interviewerName,
            scheduledById: interviewObj.scheduledById,
            additionalNotes: interviewObj.additionalNotes,
            createdBy: 1
        };

        const formData = new FormData();
        formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        if (resumeFile) {
            formData.append('resume', resumeFile);
        }
        return this.http.post(`${this.baseUrl}api/scheduleInterview`, formData);
    }

    updateInterview(interviewObj: Interview, resumeFile?: File) {
        const payload = {
            id: interviewObj.id,
            title: interviewObj.title,
            date: interviewObj.date,
            time: interviewObj.time,
            client: interviewObj.client,
            role: interviewObj.role,
            project: interviewObj.project,
            departmentId: interviewObj.departmentId,
            employeeId: interviewObj.employeeId,
            mode: interviewObj.mode,
            interviewStatus: interviewObj.interviewStatus,
            selectionStatus: interviewObj.selectionStatus,
            onboardingStatus: interviewObj.onboardingStatus,
            interviewRemarks: interviewObj.interviewRemarks,
            selectionRemarks: interviewObj.selectionRemarks,
            onboardingRemarks: interviewObj.onboardingRemarks,
            jd: interviewObj.jd,
            interviewerName: interviewObj.interviewerName,
            additionalNotes: interviewObj.additionalNotes,
            updatedBy: 1
        };

        const formData = new FormData();
        formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        if (resumeFile) {
            formData.append('resume', resumeFile);
        }
        return this.http.post(`${this.baseUrl}api/updateInterview`, formData);
    }

    getDashboardStats(filterObj: any) {
        return this.getInterviewList(filterObj);
    }

    getClientList() {
        return this.http.get(`${this.baseUrl}api/getInterviewDropdownData`);
    }

    getDepartmentList() {
        return this.http.get(`${this.baseUrl}api/getInterviewDropdownData`);
    }

    getEmployeeList() {
        return this.http.get(`${this.baseUrl}api/getInterviewDropdownData`);
    }

    getEmployeesByDepartment(departmentId: number) {
        return this.http.get(`${this.baseUrl}api/getEmployeesByDepartment?departmentId=${departmentId}`);
    }

    getProjectsByClient(clientName: string) {
        return this.http.get(`${this.baseUrl}api/getProjectsByClient?clientName=${encodeURIComponent(clientName)}`);
    }

    getProjectList() {
        return this.http.get(`${this.baseUrl}api/getInterviewDropdownData`);
    }

    getInterviewById(id: any) {
        const formData = new FormData();
        formData.append('dto', new Blob([JSON.stringify({ id: id })], { type: 'application/json' }));
        return this.http.post(`${this.baseUrl}api/getInterviewById`, formData);
    }
}
