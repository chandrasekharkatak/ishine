import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Project } from '../models/project';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getAllProjectListByProjectManagerId(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `/api/getAllProjectListByProjectManagerId`, projectObj);
  }
}
