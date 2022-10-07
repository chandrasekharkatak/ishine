import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JobRole } from '../models/jobRole';

@Injectable({
  providedIn: 'root'
})
export class JobRoleService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  createJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createJobRole`, jobRoleObj);
  }

  updateJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateJobRole`, jobRoleObj);
  }

  deleteJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteJobRole`, jobRoleObj);
  }

  getAllJobRole() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllJobRole`);
  }

  changeEmployeeJobRoleMapping(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/changeEmployeeJobRoleMapping`, jobRoleObj);
  }
  
  checkJobRole(jobRoleObj : JobRole) {
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/checkJobRole`, jobRoleObj);
  }
}
