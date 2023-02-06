import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Project } from '../models/project';

@Injectable({
  providedIn: 'root'
})
export class ResourceManagementService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

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
  
}
