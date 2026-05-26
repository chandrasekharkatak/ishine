import { HttpClient, HttpEvent, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface SkillMatrixMasterListSort {
  column: string;
  direction: 'asc' | 'desc';
}

@Injectable({
  providedIn: 'root'
})
export class SkillMatrixService {

  private baseUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }

  private masterListParams(
    page: number,
    size: number,
    filters: Record<string, string>,
    sort?: SkillMatrixMasterListSort | null
  ): HttpParams {
    let p = new HttpParams().set('page', String(page)).set('size', String(size));
    Object.entries(filters || {}).forEach(([k, v]) => {
      if (v != null && String(v).trim() !== '') {
        p = p.set(k, String(v).trim());
      }
    });
    if (sort?.column && sort.direction) {
      p = p.set('sortColumn', sort.column);
      p = p.set('sortDirection', sort.direction);
    }
    return p;
  }

  pingSubmitForReview(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/ping`);
  }

  /** Employee + department (read-only) and skills for that department from HRMS / skill matrix masters. */
  getSubmitForReviewContext(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/context`);
  }

  getSubmitLockStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/lock-status`);
  }

  getApprovedBaseline(submissionId: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}api/skill-matrix/submit-for-review/approved-baseline/${encodeURIComponent(submissionId)}`
    );
  }

  getSubmitSkillPool(skillType: 'Required' | 'Optional', q: string = ''): Observable<any> {
    let p = new HttpParams().set('skillType', skillType);
    const t = (q ?? '').trim();
    if (t) {
      p = p.set('q', t);
    }
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/skill-pool`, { params: p });
  }

  getSubmitSkillCategories(q: string = ''): Observable<any> {
    const t = (q ?? '').trim();
    const params = t ? new HttpParams().set('q', t) : new HttpParams();
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/skill-categories`, { params });
  }

  proposeSubmitSkill(body: { skillName: string; categoryId: number }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/submit-for-review/propose-skill`, body);
  }

  getCustomSkillRequests(page: number, size: number): Observable<any> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get(`${this.baseUrl}api/skill-matrix/approve-requests/custom-skill-requests`, { params });
  }

  decideCustomSkillRequest(
    requestId: number,
    body: { decision: 'approved' | 'rejected'; skillType?: 'Required' | 'Optional' | null; comment?: string | null }
  ): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/approve-requests/custom-skill-requests/${encodeURIComponent(String(requestId))}/decision`,
      body
    );
  }

  getSubmitProjectHistory(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/projects`);
  }

  updateExistingProjects(body: { submissionId: string; projects: any[] }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/submit-for-review/projects/update`, body);
  }

  loadSubmitDraft(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/draft`);
  }

  loadSubmitDraftById(submissionId: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}api/skill-matrix/submit-for-review/draft/${encodeURIComponent(submissionId)}`
    );
  }

  saveSubmitDraft(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/submit-for-review/draft`, body);
  }

  submitSubmitForReview(body: { submissionId: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/submit-for-review/submit`, body);
  }

  uploadSubmitCertificate(file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(`${this.baseUrl}api/skill-matrix/submit-for-review/certificates/upload`, fd);
  }

  /** Read-only domain stack for Step 4 (Submit for review users). */
  getSubmitSkillDomains(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/skill-domains`);
  }

  getSubmitSkillSubdomains(domainId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/skill-subdomains`, {
      params: new HttpParams().set('domainId', String(domainId))
    });
  }

  getSubmitSkillDomainFeatures(domainId: number, subdomainId?: number | null): Observable<any> {
    let p = new HttpParams().set('domainId', String(domainId));
    if (subdomainId != null && subdomainId !== undefined) {
      p = p.set('subdomainId', String(subdomainId));
    }
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/skill-domain-features`, { params: p });
  }

  /** Step 5 aspiration chips from {@code skillmatrix_aspiration_chip_master} (per dept, else global dept_id=0). */
  getSubmitAspirationChips(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/submit-for-review/aspiration-chips`);
  }

  pingMySubmissions(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/my-submissions/ping`);
  }

  getMySubmissions(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/my-submissions`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  pingApproveRequests(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/approve-requests/ping`);
  }

  getApproveRequestsQueue(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/approve-requests/queue`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  getApproveSubmissionDetail(submissionId: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get(`${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}`, { params });
  }

  getApproveSubmissionSkillsMeta(submissionId: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/skills-meta`
    );
  }

  getApproveSubmissionSkillDetail(submissionId: string, skillRatingId: number): Observable<any> {
    return this.http.get(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/skill/${encodeURIComponent(String(skillRatingId))}`
    );
  }

  bulkApprovePendingSkills(submissionId: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/bulk-approve-pending`,
      {}
    );
  }

  bulkRejectPendingSkills(submissionId: string, body: { managerComment: string }): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/bulk-reject-pending`,
      body
    );
  }

  saveApproveSkillDecision(submissionId: string, body: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/decision`,
      body
    );
  }

  submitApproveReview(submissionId: string, body: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/approve-requests/submission/${encodeURIComponent(submissionId)}/submit`,
      body
    );
  }

  submitHodDecision(submissionId: string, body: { decision: 'approved' | 'rejected'; comment?: string | null }): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/hod-requests/submission/${encodeURIComponent(submissionId)}/decision`,
      body
    );
  }

  getApproveSkillView(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/approve-requests/skill-view`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  pingMasterConfiguration(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/ping`);
  }

  getSkillCategories(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/skill-categories`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSkillCategory(body: { categoryName: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/skill-categories`, body);
  }

  updateSkillCategory(id: number, body: { categoryName: string }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/skill-categories/${id}`, body);
  }

  deleteSkillCategory(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/skill-categories/${id}`);
  }

  getSkillsMaster(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/skills`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSkill(body: {
    skillName: string;
    categoryId: number;
    skillType?: string;
    isActive?: boolean;
    departmentId: number;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/skills`, body);
  }

  updateSkill(id: number, body: {
    skillName: string;
    categoryId: number;
    skillType?: string;
    isActive?: boolean;
    departmentId: number;
  }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/skills/${id}`, body);
  }

  deleteSkill(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/skills/${id}`);
  }

  getSubskillsMaster(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/subskills`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSubskill(body: { skillId: number; subskillName: string; isActive?: boolean }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/subskills`, body);
  }

  updateSubskill(id: number, body: { skillId: number; subskillName: string; isActive?: boolean }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/subskills/${id}`, body);
  }

  deleteSubskill(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/subskills/${id}`);
  }

  getSkillDomains(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domains`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSkillDomain(body: { domainName: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domains`, body);
  }

  updateSkillDomain(id: number, body: { domainName: string }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domains/${id}`, body);
  }

  deleteSkillDomain(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domains/${id}`);
  }

  getSkillSubdomains(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/skill-subdomains`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSkillSubdomain(body: { domainId: number; subdomainName: string; isActive?: boolean }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/skill-subdomains`, body);
  }

  updateSkillSubdomain(id: number, body: { domainId: number; subdomainName: string; isActive?: boolean }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/skill-subdomains/${id}`, body);
  }

  deleteSkillSubdomain(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/skill-subdomains/${id}`);
  }

  getSkillDomainFeatures(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domain-features`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createSkillDomainFeature(body: {
    domainId: number;
    subdomainId?: number | null;
    featureName: string;
    isActive?: boolean;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domain-features`, body);
  }

  updateSkillDomainFeature(id: number, body: {
    domainId: number;
    subdomainId?: number | null;
    featureName: string;
    isActive?: boolean;
  }): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domain-features/${id}`, body);
  }

  deleteSkillDomainFeature(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/skill-domain-features/${id}`);
  }

  getAspirationChipsMaster(
    page: number,
    size: number,
    filters: Record<string, string> = {},
    sort?: SkillMatrixMasterListSort | null
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/aspiration-chips`, {
      params: this.masterListParams(page, size, filters, sort)
    });
  }

  createAspirationChipMaster(body: {
    deptId: number;
    chipLabel: string;
    sortOrder?: number;
    isActive?: boolean;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/aspiration-chips`, body);
  }

  updateAspirationChipMaster(
    id: number,
    body: { deptId: number; chipLabel: string; sortOrder?: number; isActive?: boolean }
  ): Observable<any> {
    return this.http.put(`${this.baseUrl}api/skill-matrix/master-configuration/aspiration-chips/${id}`, body);
  }

  deleteAspirationChipMaster(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/skill-matrix/master-configuration/aspiration-chips/${id}`);
  }

  downloadBulkTemplate(masterType: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}api/skill-matrix/master-configuration/bulk/template/${masterType}`, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  bulkUploadMaster(masterType: string, file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(`${this.baseUrl}api/skill-matrix/master-configuration/bulk/upload/${masterType}`, fd);
  }

  /** Same endpoint as {@link #bulkUploadMaster}; emits upload progress then the JSON response body. */
  bulkUploadMasterWithProgress(masterType: string, file: File): Observable<HttpEvent<unknown>> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(
      `${this.baseUrl}api/skill-matrix/master-configuration/bulk/upload/${masterType}`,
      fd,
      { reportProgress: true, observe: 'events', responseType: 'json' }
    );
  }
}
