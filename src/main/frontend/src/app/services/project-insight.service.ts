import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectInsight } from '../models/projectInsight';
import { Document } from '../models/document';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }


  createProjectInsightQuestion(projObj: ProjectInsight){
    return this.http.post(`${this.baseUrl}` + `api/createProjectInsightQuestion`, projObj);
  }

  updateProjectInsightQuestion(projObj: ProjectInsight){
    return this.http.post(`${this.baseUrl}` + `api/updateProjectInsightQuestion`, projObj);
  }

  getAllProjectInsightList(insightObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightList`, insightObj);
  }

  getAllQuestionsByProjectId(projectObj: any){
    return this.http.post(`${this.baseUrl}` + `api/getAllQuestionsByProjectId`, projectObj);
  }

  getAllProjectInsightResponsesByProjectId(projectObj: any){
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightResponsesByProjectId`, projectObj);
  }

  saveProjectInsightResponse(projectObj: any, files: File[]){
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

  suggestSearchOption(searchTerm: string){
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get(`${this.baseUrl}` + `api/suggestSearchOption`, { params });
  }

  getFilterList(){
    return this.http.get(`${this.baseUrl}` + `api/getFilterList`);
  }
  saveReviewPoints(projObj: ProjectInsight){
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(projObj)], { type: 'application/json' });
    formData.append('projectInsightDTO', blob);
    return this.http.post(`${this.baseUrl}` + `api/saveReviewPoints`, formData);
  }
  
  /*
  User contribution apis
  */ 

  createUserContribution(contributionObject: any){
    return this.http.post(`${this.baseUrl}` + `api/createUserContribution`, contributionObject);
  }

  getContibutionByEmpId(contributionObject: any){
    return this.http.post(`${this.baseUrl}` + `api/getContibutionByEmpId`, contributionObject);
  }

  getUserContributionForReview(contributionObject: any){
    return this.http.post(`${this.baseUrl}` + `api/getUserContributionForReview`, contributionObject);
  }

  processUserContribution(contributionObject: any){
    return this.http.post(`${this.baseUrl}` + `api/processUserContribution`, contributionObject);
  }
  
  getAllProjectInsightQuestionsByProjectIdAndEmpId(projectObj: any){
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightQuestionsByProjectIdAndEmpId`, projectObj);
  }

  onSaveAndAssign(payload: any){
    return this.http.post(`${this.baseUrl}` + `api/onSaveAndAssign`, payload);
  }

  onSaveAsDraft(payload: any){
    return this.http.post(`${this.baseUrl}` + `api/onSaveAsDraft`, payload);
  }

  getAllProjectInsight(domain?: string) {
    if(!domain){
      return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsight`);
    }
    return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsight`, {
      params: {
        domain
      }
    });
  }

  getProjectInsightByInsightId(projectInsightId: any){
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightByInsightId/`+ `${projectInsightId}`);
  }

  deleteProjectInsightById(projectInsightId: any){
    return this.http.get(`${this.baseUrl}` + `api/deleteProjectInsightById/`+ `${projectInsightId}`);
  }

  getProjectInsightByAssignedToEmpId(empObject: any){
    return this.http.post(`${this.baseUrl}` + `api/getProjectInsightByAssignedToEmpId`, empObject);
  }

  searchProjectInsight(keyword: string) {
    return this.http.get(`${this.baseUrl}` + `api/searchProjectInsight?q=${encodeURIComponent(keyword)}`);
  }

  getReviewersForQuestion(object: any){
    return this.http.post(`${this.baseUrl}` + `api/getReviewersForQuestion`, object);
  }

  onSaveResponseAsDraft(object: any){
    return this.http.post(`${this.baseUrl}` + `api/onSaveResponseAsDraft`, object);
  }

  getProjectInsightDetailsByObjectId(objectId: any) {
    const params = new HttpParams().set('id', objectId.toString());
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightDetailsByObjectId/`, { params });
  }

  getProjectInsightGroupDetailsByObjectId(objectId: any) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectInsightGroupDetailsByObjectId/`,objectId);
  }

  getProjectInsightQuestionDetailsByObjectId(objectId: any) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByObjectId/`,objectId);
  }

  getProjectInsightQuestionDetailsByParentIdAndParentType(parentId: any, parentType: any) {
    const params = new HttpParams().set('parentId', parentId).set('parentType', parentType);
    return this.http.get(`${this.baseUrl}` + `api/getProjectInsightQuestionDetailsByParentIdAndParentType/`, { params });
  }

  saveProjectInsightDetails(projectInsightDetailsDTO: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightDetails`, projectInsightDetailsDTO);
  }

  saveProjectInsightGroupDetails(projectInsightDetailsDTO: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightGroupDetails`, projectInsightDetailsDTO);
  }

  saveProjectInsightQuestionDetails(projectInsightDetailsDTO: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightQuestionDetails`, projectInsightDetailsDTO);
  }

}
