import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectInsightQuestion } from '../models/projectInsightQuestion';
import { Document } from '../models/document';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }


  createProjectInsightQuestion(projObj: ProjectInsightQuestion){
    return this.http.post(`${this.baseUrl}` + `api/createProjectInsightQuestion`, projObj);
  }

  updateProjectInsightQuestion(projObj: ProjectInsightQuestion){
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

}
