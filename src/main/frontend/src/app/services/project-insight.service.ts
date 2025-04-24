import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectInsight } from '../models/projectInsightQuestion';
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
  
}
