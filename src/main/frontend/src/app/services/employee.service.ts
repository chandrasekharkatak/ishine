import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Employee } from '../models/employee';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
  constructor(private http: HttpClient) { }

  createEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createEmployee`, employeeObj);
  }

  updateEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateEmployeeByEmpId`, employeeObj);
  }

  deleteEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteEmployeeByEmpId`, employeeObj);
  }

  getAllEmployees() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllEmployees`);
  }

  getEmployeeByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getEmployeeByEmpId`, employeeObj);
  }

  updateEmployeeProfile(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateEmployeeProfileByEmpId`, employeeObj);
  }

  getAllEmployeesByRole(employeeObj: Employee) {	
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllEmployeesByRole`, employeeObj);	
  }

  getAllEmployeesByDepartmentIds(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllEmployeesByDepartmentIds`, employeeObj);
  }

  checkEmployeementId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeementId`, employeeObj);
  }

  checkEmployeeMobileNo(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeeMobileNo`, employeeObj);
  }

  checkEmployeeAadharNumber(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeeAadharNumber`, employeeObj);
  }

  checkEmployeePanNumber(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeePanNumber`, employeeObj);
  }

  getAllEmployeesBirthDayToday() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllEmployeesBirthDayToday`);
  }

  getHierarchyByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getHierarchyByEmpId`, employeeObj);
  }

  /* Profile Image Upload */
  previewImage(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/previewImage`,formData);
  }

  uploadImage(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`employeeportal/api/uploadImage`,formData);
  }

  /* Employee Draft */
  createDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createDraftEmployee`, employeeObj);
  }

  updateDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateDraftEmployeeById`, employeeObj);
  }

  deleteDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteDraftEmployeeById`, employeeObj);
  }

  getAllDraftEmployees() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllDraftEmployees`);
  }

  getDraftEmployeeByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getDraftEmployeeById`, employeeObj);
  }

  checkEmployeeEmail(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeeEmail`, employeeObj);
  }

  getDraftEmployeeByEmploymentId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getDraftEmployeeByEmploymentId`, employeeObj);
  }

   /* update Employee Password */

   updateEmployeePassword(user: User){
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateEmployeePassword`, user);
   }

   /* check Employee old Password */

   checkEmployeeOldPassword(user: User){
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkEmployeeOldPassword`, user);
   }	

   revokeAccount(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/revokeAccount` , employeeObj);
   }
}
