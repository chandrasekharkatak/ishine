import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectInsight } from '../models/projectInsight';
import { Document } from '../models/document';
import { ProjectInsightGroupDetails } from '../models/projectInsightGroupDetails';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createProjectInsightQuestion(projObj: ProjectInsight) {
    return this.http.post(`${this.baseUrl}` + `api/createProjectInsightQuestion`, projObj);
  }

  updateProjectInsightQuestion(projObj: ProjectInsight) {
    return this.http.post(`${this.baseUrl}` + `api/updateProjectInsightQuestion`, projObj);
  }

  getAllProjectInsightList(insightObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightList`, insightObj);
  }

  getAllQuestionsByProjectId(projectObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllQuestionsByProjectId`, projectObj);
  }

  getAllProjectInsightResponsesByProjectId(projectObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightResponsesByProjectId`, projectObj);
  }

  saveProjectInsightResponse(projectObj: any, files: File[]) {
    const formData = new FormData();
    formData.append("projectInsightDTO", new Blob([JSON.stringify(projectObj)], { type: "application/json" }));
    files.forEach((file, index) => {
      formData.append("files", file);
    });
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightResponse`, formData);
  }

  getUserUploadedFileForQuestion(documentObj: Document) {
    return this.http.post(`${this.baseUrl}` + `api/getUserUploadedFileForQuestion`, documentObj);
  }

  getAllProjectInsightContributionList(projectObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightContributionList`, projectObj);
  }

  onSearchTerm(searchTerm: string) {
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get(`${this.baseUrl}` + `api/onSearchTerm`, { params });
  }

  suggestSearchOption(searchTerm: string) {
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get(`${this.baseUrl}` + `api/suggestSearchOption`, { params });
  }

  getFilterList() {
    return this.http.get(`${this.baseUrl}` + `api/getFilterList`);
  }
  saveReviewPoints(projObj: ProjectInsight) {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(projObj)], { type: 'application/json' });
    formData.append('projectInsightDTO', blob);
    return this.http.post(`${this.baseUrl}` + `api/saveReviewPoints`, formData);
  }

  /*
  User contribution apis
  */

  createUserContribution(contributionObject: any) {
    return this.http.post(`${this.baseUrl}` + `api/createUserContribution`, contributionObject);
  }

  getContibutionByEmpId(contributionObject: any) {
    return this.http.post(`${this.baseUrl}` + `api/getContibutionByEmpId`, contributionObject);
  }

  getUserContributionForReview(contributionObject: any) {
    return this.http.post(`${this.baseUrl}` + `api/getUserContributionForReview`, contributionObject);
  }

  processUserContribution(contributionObject: any) {
    return this.http.post(`${this.baseUrl}` + `api/processUserContribution`, contributionObject);
  }

  getAllProjectInsightQuestionsByProjectIdAndEmpId(projectObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightQuestionsByProjectIdAndEmpId`, projectObj);
  }

  onSaveAndAssign(payload: any) {
    return this.http.post(`${this.baseUrl}` + `api/onSaveAndAssign`, payload);
  }

  onSaveAsDraft(payload: any) {
    return this.http.post(`${this.baseUrl}` + `api/onSaveAsDraft`, payload);
  }

  getAllProjectInsight(domainName?: number | string, unique_name?: string) {
    if (!domainName && !unique_name) {
      return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsight`);
    }
    if (!unique_name) {
      return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsight`, {
        params: {
          domainName
        }
      });
    }
    return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsight`, {
      params: {
        domainName,
        unique_name
      }
    });
  }

  getProjectInsightByInsightId(projectInsightId: any) {
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightByInsightId/` + `${projectInsightId}`);
  }

  deleteProjectInsightById(projectInsightId: any) {
    return this.http.get(`${this.baseUrl}` + `api/deleteProjectInsightById/` + `${projectInsightId}`);
  }

  getProjectInsightByAssignedToEmpId(empObject: any) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectInsightByAssignedToEmpId`, empObject);
  }

  searchProjectInsight(keyword: string, page: number, limit: number) {
    return this.http.get(environment.baseUrl + 'api/search-project-insight', { params: { search: keyword, page: page, limit: limit } });
  }

  getReviewersForQuestion(object: any) {
    return this.http.post(`${this.baseUrl}` + `api/getReviewersForQuestion`, object);
  }

  onSaveResponseAsDraft(object: any) {
    return this.http.post(`${this.baseUrl}` + `api/onSaveResponseAsDraft`, object);
  }

  getProjectInsightDetailsByObjectId(objectId: any) {
    const params = new HttpParams().set('id', objectId.toString());
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightDetailsByObjectId/`, { params });
  }

  getAllProjectInsightGroupsByParentId(parentId: any, parentType: any) {
    const params = new HttpParams().set('parentId', parentId).set('parentType', parentType);
    return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsightGroupsByParentId/`, { params });
  }

  getProjectInsightGroupDetailsByObjectId(objectId: any) {
    const params = new HttpParams().set('id', objectId.toString());
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightGroupDetailsByObjectId/`, { params });
  }

  getProjectInsightQuestionDetailsByObjectId(objectId: any) {
    const params = new HttpParams().set('id', objectId.toString());
    return this.http.post(`${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByObjectId/`, { params });
  }

  getProjectInsightQuestionDetailsByParentIdAndParentType(parentId: any, parentType: any) {
    const params = new HttpParams().set('parentId', parentId).set('parentType', parentType);
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByParentIdAndParentType/`, { params });
  }

  getProjectInsightGroupDetailsByParentIdAndParentType(parentId: any, parentType: any) {
    const params = new HttpParams().set('parentId', parentId).set('parentType', parentType);
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightGroupDetailsByParentIdAndParentType/`, { params });
  }

  saveProjectInsightDetails(projectInsightDetailsDTO: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightDetails`, projectInsightDetailsDTO);
  }

  saveProjectInsightGroupDetails(projectInsightDetailsDTO: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightGroupDetails`, projectInsightDetailsDTO);
  }

  saveProjectInsightStaticGroupDetails(projectInsightGroupDetails: ProjectInsightGroupDetails) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightStaticGroupDetails`, projectInsightGroupDetails);
  }


  saveProjectInsightQuestionDetails(projectInsightQuestionDetails: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightQuestionDetails`, projectInsightQuestionDetails);
  }

  deleteProjectInsightQuestionDetails(projectInsightQuestionDetails: any) {
    return this.http.post(`${this.baseUrl}` + `api/deleteProjectInsightQuestionDetails`, projectInsightQuestionDetails);
  }

  getAllDomainData() {
    return this.http.get(`${this.baseUrl}` + `api/createProjectInsightQuestion`);
  }

  filterProjectInsight(data: any, limit: number = 10, page: number = 0) {
    if (data.createdAt) {
      // If it's a Date object
      if (data.createdAt instanceof Date) {
        data.createdAt = data.createdAt.toISOString().slice(0, 19);
      }
      // If it's already a string like "2025-07-17T00:00:00"
      else if (typeof data.createdAt === 'string') {
        data.createdAt = data.createdAt.split('.')[0]; // Remove milliseconds if present
      }
    }
    console.log(data);
    return this.http.post(`${this.baseUrl}` + `api/filter-project-insight?page=${page}&limit=${limit}`, { ...data });
  }

}
