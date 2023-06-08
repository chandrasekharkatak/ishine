import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Project } from '../models/project';
import { environment } from 'src/environments/environment';

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

}
