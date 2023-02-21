import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Department } from '../models/department';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

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

  getAllDepartments() {
    return this.http.get(`${this.baseUrl}` + `api/getAllDepartments`);
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
