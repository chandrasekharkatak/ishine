import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Project } from '../models/project';
import { environment } from 'src/environments/environment';
import { ProjectFilterDTO } from '../models/projectFilterDTO';

@Injectable({
  providedIn: 'root'
})
export class ResourceManagementService {
  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDraftProjectInfo(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/createDraftProjectInfo`, projectObj);
  }

  getTeamListByProjectName(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/getTeamListByProjectName`, projectObj);
  }

  alreadyCreatedTeam(){
    return this.http.get(`${this.baseUrl}` + `api/alreadyCreatedTeam`);
  }

  getPendingForApprovalProject(){
    return this.http.get(`${this.baseUrl}` + `api/getPendingForApprovalProject`);
  }

  approvePendingProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/approvePendingProject`, projectObj);
  }

  rejectPendingProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/rejectPendingProject`, projectObj);
  }

  sendProjectApproval(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/sendProjectApproval`, projectObj);
  }

  bulkSyncProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/bulkSyncProject`, projectObj);
  }

  getInternalProject(){
    return this.http.get(`${this.baseUrl}` + `api/getInternalProject`);
  }

  getPoProjectDetailsForPoProjects(){
    return this.http.get(`${this.baseUrl}` + `api/getPoProjectDetailsForPoProjects`);
  }

  syncPoProjectDetailsByProjectId(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/syncPoProjectDetailsByProjectId`,project);
  }

  sendEmailNotificationToBDTeam(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/sendEmailNotificationToBDTeam`,project);
  }
  
  combinedPOINTERNALList(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/combinedPOINTERNALList`,ProjectFilterDTO);
  }
}
