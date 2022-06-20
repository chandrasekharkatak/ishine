import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Leave } from '../models/leave';

@Injectable({
  providedIn: 'root'
})
export class LeaveService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  applyLeave(leaveObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/applyLeave`, leaveObj);
  }

  updateLeaveStatus(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateLeaveStatus`, leaveObj);
  }

  getAllMyLeaveApplicationsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllMyLeaveApplicationsByEmpId`, leaveObj);
  }

  getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllMyTeamsPendingLeaveApplicationsByManagerId`, leaveObj);
  }

  getLeaveLogsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getLeaveLogsByEmpId`, leaveObj);
  }

  getMyLeaveBalancesByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getMyLeaveBalancesByEmpId`, leaveObj);
  }


  /* Leave Type */
  createLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createLeaveType`, leaveObj);
  }

  updateLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateLeaveType`, leaveObj);
  }

  getAllLeaveTypes() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllLeaveTypes`);
  }


  /* Comp off */
  getAllCompOffReasons(){
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllCompOffReasons`);
  }

  applyForCompOff(compOffObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/applyForCompOff`, compOffObj);
  }

  updateCompOffById(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateCompOffById`, compOffObj);
  }

  getAllCompOffRequestsByEmpId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllCompOffRequestsByEmpId`, compOffObj);
  }

  getPendingCompOffRequestsByManagerId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getPendingCompOffRequestsByManagerId`, compOffObj);
  }
}
