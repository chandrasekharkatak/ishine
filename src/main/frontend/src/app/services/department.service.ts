import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Department } from '../models/department';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDepartment(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/createDepartment`, deptObj);
  }

  updateDepartment(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/updateDepartment`, deptObj);
  }

  deleteDepartment(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/deleteDepartment`, deptObj);
  }

  getAllDepartmentsByProjectId(deptObj: any){
    return this.http.post(`${this.baseUrl}` + `api/getAllDepartmentsByProjectId`, deptObj);
  }

  getAllDepartments() {
    return this.http.get(`${this.baseUrl}` + `api/getAllDepartments`);
  }
  getAllDepartmentsFromId(data:any){
    return this.http.get(`${this.baseUrl}` + `api/getAllDepartmentsFromId/${data}`);
  }
  updateDepartmentHolidayMappings(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/updateDepartmentHolidayMappings`, deptObj);
  }

  changeDepartmentJobRoleMapping(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/changeDepartmentJobRoleMapping`, deptObj);
  }

  checkDepartmentName(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `api/checkDepartmentName`, deptObj);
  }
}
