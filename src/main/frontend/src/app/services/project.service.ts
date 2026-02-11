import { HttpClient,HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from 'src/environments/environment';
import { MilestoneUpdatedLog } from '../models/MilestoneUpdatedLog';
import { Project } from '../models/project';
import { RmgTeamMember } from '../models/rmgTeamMember';
import { PoDetails } from '../models/poDetails';
import { RmgProject } from '../models/rmgProject';
import { EmployeeOtherActiveProject } from '../models/employeeOtherActiveProject';


@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private baseUrl: any = environment.baseUrl;
  public projectMap = new Map<string, any>();

  constructor(private http: HttpClient) { }

  getAllProjectListByProjectManagerId(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectListByProjectManagerId`, projectObj);
  }

  getAllClients() {
    return this.http.get(`${this.baseUrl}` + `api/getAllClients`);
  }

  getAllProjects() {
    return this.http.get(`${this.baseUrl}` + `api/getAllProjects`);
  }

  getAllProjectsList() {
    return this.http.get(`${this.baseUrl}` + `api/getAllProjectsList`);
  }

  createProject(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/createProject`, projectObj);
  }

  updateProject(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/updateProject`, projectObj);
  }

  deleteProject(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/deleteProject`, projectObj);
  }

  getProjectByProjectId(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectByProjectId`, project);
  }

  checkProjectName(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/checkProjectName`, project);
  }

  getAllMyProjectByEmpId(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyProjectByEmpId`, project);
  }

  getExistingProjectsAndTeamsByEmployee(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getExistingProjectsAndTeamsByEmployee`, project);
  }

  updateProjectResourceAsInActive(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/updateProjectResourceAsInActive`, project);
  }

  updateProjectStartAndEndDate(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/updateProjectStartAndEndDate`, project);
  }

  deleteTeam(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/deleteTeamByTeamId`, project);
  }
  
  //added by rahul for project
  getTeamMemberByTeamId(teamId: any) {
    return this.http.get(`${this.baseUrl}` + `api/getTeamMemberByTeamId/` + teamId);
  }
  updateProjectResourcesAsInActiveBulk(project: any) {
    return this.http.post(`${this.baseUrl}` + `api/updateProjectResourcesAsInActiveBulk`, project);
  }

  getProjectByName(project: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectByName`, project);
  }

deleteTeamsByIdsBulk(teamObj: any){
  return this.http.post(`${this.baseUrl}` + `api/deleteTeamsByIdsBulk`, teamObj);
}
getProjectWithCliendSideID(projectObj:any){
  return this.http.post(`${this.baseUrl}` + `api/getProjectWithCliendSideID`,projectObj);
}

getEmployeeTimesheetsByProject(teamObj: any){
  return this.http.post(`${this.baseUrl}` + `api/getEmployeeTimesheetsByProject`, teamObj);
}

  getAllProjectFCLineItemListByProjectId(projectObjTemp: Project) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectFCLineItemListByProjectId`, projectObjTemp);
  }

  updateMilestoneById(formData: FormData): Observable<any> {
    return this.http.put(`${this.baseUrl}` + `api/updateMilestoneById`, formData);
  }

  getMilestoneById(milestoneId:number): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/getMilestoneById`, { params: { milestoneId: milestoneId} });
  }

  getAllMilestoneExtendReason():Observable<any>{
    return this.http.get(`${this.baseUrl}` + `api/getAllMilestoneExtendReason`);
  }

 getAllMilestoneToBeExpired(rmEmail: number): Observable<any> {

  return this.http.post(`${this.baseUrl}`+`api/getAllMilestoneToBeExpired`, rmEmail);
}


updateMilestoneExtendedDate(MilestoneUpdatedLog: MilestoneUpdatedLog): Observable<any> {
  return this.http.put(`${this.baseUrl}` + `api/updateMilestoneExtendedDate`, MilestoneUpdatedLog);

}

getClientVsDepartment(payload){
  return this.http.post(`${this.baseUrl}` + `api/getProjectStructure`, payload)
}
   getClientAndProjectReport(payload){
       return this.http.post(`${this.baseUrl}` + `api/getClientAndProjectReport`, payload)
   }

   
   getClientAndProjectReportDataList(payload){
       return this.http.post(`${this.baseUrl}` + `api/getClientAndProjectReportDataList`, payload)
   }
   getEmployeeProjectCount(payload){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeProjectCount`,payload);
   }

  getEmployeeExistingProjectDetailsByEmpId(empId: any) {
    let httpParams = new HttpParams().append("empId", empId);
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeExistingProjectDetailsByEmpId`, { params: httpParams });
  }

  updateEmployeeProjectMappingAsInActive(rmgTeamMember: RmgTeamMember) {
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeeProjectMappingAsInActive`, rmgTeamMember);
  }

  saveProjectInformation(projectObj: RmgProject) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInformation`, projectObj);
  }

  updateMappingToOtherProjectAsDefault(otherActiveProject : EmployeeOtherActiveProject) {
    return this.http.post(`${this.baseUrl}` + `api/updateMappingToOtherProjectAsDefault`, otherActiveProject);
  }
  
  
}
