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
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createDepartment`, deptObj);
  }

  updateDepartment(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateDepartment`, deptObj);
  }

  deleteDepartment(deptObj: Department) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteDepartment`, deptObj);
  }

  getAllDepartments() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllDepartments`);
  }
}
