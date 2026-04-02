import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class GrievanceService {
  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) {}

  createTicket(formData: FormData) {
    return this.http.post(`${this.baseUrl}api/grievance/createTicket`, formData);
  }

  getMyTicketsPaged(page: number, size: number, sortBy: string, sortDir: 'asc' | 'desc') {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get(`${this.baseUrl}api/grievance/myTickets`, { params });
  }

  getAllTicketsPaged(page: number, size: number, sortBy: string, sortDir: 'asc' | 'desc') {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get(`${this.baseUrl}api/grievance/allTickets`, { params });
  }

  getAssignedTicketsPaged(page: number, size: number, sortBy: string, sortDir: 'asc' | 'desc') {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get(`${this.baseUrl}api/grievance/assignedTickets`, { params });
  }

  getEmployee360TicketCounts(targetEmpId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/employee360/${targetEmpId}/counts`);
  }

  getEmployee360TicketsRaised(
    targetEmpId: number,
    page: number,
    size: number,
    sortBy: string,
    sortDir: 'asc' | 'desc',
    fromDate?: string | null,
    toDate?: string | null
  ) {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }
    return this.http.get(`${this.baseUrl}api/grievance/employee360/${targetEmpId}/raised`, { params });
  }

  getEmployee360TicketsAssigned(
    targetEmpId: number,
    page: number,
    size: number,
    sortBy: string,
    sortDir: 'asc' | 'desc',
    fromDate?: string | null,
    toDate?: string | null
  ) {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }
    return this.http.get(`${this.baseUrl}api/grievance/employee360/${targetEmpId}/assigned`, { params });
  }

  exportMyTickets() {
    return this.http.get(`${this.baseUrl}api/grievance/myTickets/export`);
  }

  exportAllTickets() {
    return this.http.get(`${this.baseUrl}api/grievance/allTickets/export`);
  }

  exportAssignedTickets() {
    return this.http.get(`${this.baseUrl}api/grievance/assignedTickets/export`);
  }

  getDevelopmentUsers() {
    return this.http.get(`${this.baseUrl}api/grievance/developmentUsers`);
  }

  getCategories() {
    return this.http.get(`${this.baseUrl}api/grievance/categories`);
  }

  getSubCategories(categoryTabName: string) {
    const params = new HttpParams().set('category', categoryTabName);
    return this.http.get(`${this.baseUrl}api/grievance/subCategories`, { params });
  }

  getTicketFeatures(categoryTabName: string, subCategory: string) {
    const params = new HttpParams().set('category', categoryTabName).set('subCategory', subCategory);
    return this.http.get(`${this.baseUrl}api/grievance/ticketFeatures`, { params });
  }

  getIssueScenarios(categoryTabName: string, subCategory: string, ticketFeature: string) {
    const params = new HttpParams()
      .set('category', categoryTabName)
      .set('subCategory', subCategory)
      .set('ticketFeature', ticketFeature);
    return this.http.get(`${this.baseUrl}api/grievance/issueScenarios`, { params });
  }

  getTicketById(ticketId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/ticket/${ticketId}`);
  }

  getTicketAuditHistory(ticketId: number, page: number, size: number, field?: string) {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (field && field.trim()) {
      params = params.set('field', field.trim());
    }
    return this.http.get(`${this.baseUrl}api/grievance/tickets/${ticketId}/history`, { params });
  }

  getTicketAuditVersions(ticketId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/tickets/${ticketId}/versions`);
  }

  getTicketAuditDiff(ticketId: number, version1: number, version2: number) {
    const params = new HttpParams().set('version1', String(version1)).set('version2', String(version2));
    return this.http.get(`${this.baseUrl}api/grievance/tickets/${ticketId}/diff`, { params });
  }

  updateTicket(payload: any, resolutionDocument?: File | null) {
    const formData = new FormData();
    formData.append('payload', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    if (resolutionDocument) {
      formData.append('resolutionDocument', resolutionDocument);
    }
    return this.http.post(`${this.baseUrl}api/grievance/updateTicket`, formData);
  }

  submitFeedback(payload: any) {
    return this.http.post(`${this.baseUrl}api/grievance/submitFeedback`, payload);
  }

  withdrawTicket(ticketId: number) {
    return this.http.post(`${this.baseUrl}api/grievance/withdrawTicket/${ticketId}`, {});
  }

  reopenTicket(ticketId: number) {
    return this.http.post(`${this.baseUrl}api/grievance/reopenTicket/${ticketId}`, {});
  }

  downloadProof(ticketId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/downloadProof/${ticketId}`, {
      responseType: 'blob',
      observe: 'response',
    });
  }

  listTicketProofDocuments(ticketId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/ticket/${ticketId}/proofDocuments`);
  }

  downloadProofDocument(ticketId: number, documentId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/downloadProofDocument/${ticketId}/${documentId}`, {
      responseType: 'blob',
      observe: 'response',
    });
  }

  downloadResolutionDocument(ticketId: number) {
    return this.http.get(`${this.baseUrl}api/grievance/downloadResolutionDocument/${ticketId}`, {
      responseType: 'blob',
      observe: 'response',
    });
  }

  issueScenarioAdminEligible() {
    return this.http.get(`${this.baseUrl}api/grievance/admin/issueScenarios/eligible`);
  }

  listAdminIssueScenarios(page: number, size: number, sortBy: string, sortDir: 'asc' | 'desc') {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get(`${this.baseUrl}api/grievance/admin/issueScenarios`, { params });
  }

  createAdminIssueScenario(payload: any) {
    return this.http.post(`${this.baseUrl}api/grievance/admin/issueScenarios`, payload);
  }

  updateAdminIssueScenario(scenarioId: number, payload: any) {
    return this.http.put(`${this.baseUrl}api/grievance/admin/issueScenarios/${scenarioId}`, payload);
  }

  deactivateAdminIssueScenario(scenarioId: number) {
    return this.http.delete(`${this.baseUrl}api/grievance/admin/issueScenarios/${scenarioId}`);
  }
}
