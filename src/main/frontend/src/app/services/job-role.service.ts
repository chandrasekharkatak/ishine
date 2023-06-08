import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JobRole } from '../models/jobRole';
import { Employee } from '../models/employee';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class JobRoleService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `api/createJobRole`, jobRoleObj);
  }

  updateJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `api/updateJobRole`, jobRoleObj);
  }

  deleteJobRole(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `api/deleteJobRole`, jobRoleObj);
  }

  getAllJobRole() {
    return this.http.get(`${this.baseUrl}` + `api/getAllJobRole`);
  }

  changeEmployeeJobRoleMapping(jobRoleObj: JobRole) {
    return this.http.post(`${this.baseUrl}` + `api/changeEmployeeJobRoleMapping`, jobRoleObj);
  }
  
  checkJobRole(jobRoleObj : JobRole) {
    return this.http.post(`${this.baseUrl}`+`api/checkJobRole`, jobRoleObj);
  }

  updateJobRoleSubFeatureMapping(jobRoleObj :JobRole){
    return this.http.post(`${this.baseUrl}`+`api/updateJobRoleSubFeatureMapping`, jobRoleObj);
  }

  /*
  Report API
  */

  getAllSubFeatureList(){
    return this.http.get(`${this.baseUrl}`+`api/getAllSubFeatureList`);
  }

  getMappedSubFeatureList(employeeObj : Employee){
    return this.http.post(`${this.baseUrl}`+`api/getMappedSubFeatureList`, employeeObj);
  }

  getDefaultMapping(){
    return this.http.get(`${this.baseUrl}`+`api/getDefaultMapping`);
  }

  updateDefaultFeatureMapping(jobRoleObj :JobRole){
    return this.http.post(`${this.baseUrl}`+`api/updateDefaultFeatureMapping`, jobRoleObj);
  }

}
