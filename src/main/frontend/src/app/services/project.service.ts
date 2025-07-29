import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Project } from '../models/project';
import { FCProjectMilestone } from '../models/fcProjectMilestone';
import { MilestoneUpdatedLog } from '../models/MilestoneUpdatedLog';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private baseUrl:any = environment.baseUrl;


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

  createProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/createProject`, projectObj);
  }

  updateProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/updateProject`, projectObj);
  }

  deleteProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/deleteProject`, projectObj);
  }

  getProjectByProjectId(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/getProjectByProjectId`, project);
  }

  checkProjectName(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/checkProjectName`, project);
  }

  getAllMyProjectByEmpId(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/getAllMyProjectByEmpId`, project);
  }

  getExistingProjectsAndTeamsByEmployee(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/getExistingProjectsAndTeamsByEmployee`, project);
  }

  updateProjectResourceAsInActive(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/updateProjectResourceAsInActive`, project);
  }

  updateProjectStartAndEndDate(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/updateProjectStartAndEndDate`, project);
  }
  
  deleteTeam(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/deleteTeamByTeamId`, project);
  }
  //added by rahul for project
getTeamMemberByTeamId(teamId:any){
  return this.http.get(`${this.baseUrl}`+`api/getTeamMemberByTeamId/`+teamId);
} 
updateProjectResourcesAsInActiveBulk(project: any){
  return this.http.post(`${this.baseUrl}` + `api/updateProjectResourcesAsInActiveBulk`, project);
}

deleteTeamsByIdsBulk(teamObj: any){
  return this.http.post(`${this.baseUrl}` + `api/deleteTeamsByIdsBulk`, teamObj);
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

}
