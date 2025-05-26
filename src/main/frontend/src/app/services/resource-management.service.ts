import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectFilterDTO } from '../models/projectFilterDTO';
import { Project } from '../models/project';

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


  completionDateOfProject(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/completionDateOfProject`,project);
  }
  
  combinedPOINTERNALList(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/combinedPOINTERNALList`,ProjectFilterDTO);
  }

  rbacInternalProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacInternalProjects`,ProjectFilterDTO);
  }

  rbacShankhProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacShankhProjects`,ProjectFilterDTO);
  }

  rbacShankhInternalProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacShankhInternalProjects`,ProjectFilterDTO);
  }

  getResourceRequirementByPoProjectId(id: any) {
    return this.http.get(`${this.baseUrl}`+`api/getResourceRequirementByPoProjectId`, {params: { id: id }});
  }

  getEmployeeInformation(empId: any){
    return this.http.get(`${this.baseUrl}`+`api/getEmployeeInformation`, {params: { empId: empId }});
  }

}
