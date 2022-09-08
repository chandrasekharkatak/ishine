import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Leave } from '../models/leave';

@Injectable({
  providedIn: 'root'
})
export class LeaveService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* Leave */
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

  countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/countAllMyTeamsPendingLeaveApplicationsByManagerId`, leaveObj);
  }

  getLeaveLogsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getLeaveLogsByEmpId`, leaveObj);
  }

  getMyLeaveBalancesByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getMyLeaveBalancesByEmpId`, leaveObj);
  }

  updateLeavesByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateLeavesByEmpId`, leaveObj);
  }

  getAppliedLeaveApplicationsByEmpIdAndDateRange(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAppliedLeaveApplicationsByEmpIdAndDateRange`, leaveObj);
  }

  deletePendingLeave(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deletePendingLeave`, leaveObj);
  }

  updatePendingLeave(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updatePendingLeave`, leaveObj);
  }

  revokeApprovedLeaveApplication(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/revokeApprovedLeaveApplication`, leaveObj);
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

  getAllLeaveTypesByLeavePolicies(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllLeaveTypesByLeavePolicies`, leaveObj);
  }

  deleteLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteLeaveType`, leaveObj);
  }

  changeLeaveTypeMapping(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/changeLeaveTypeMapping`, leaveObj);
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

  countPendingCompOffRequestsByManagerId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/countPendingCompOffRequestsByManagerId`, compOffObj);
  }

  /* Leave Policy */
  addLeavePolicy(leavePolicyObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/addLeavePolicy`, leavePolicyObj);
  }

  updateLeavePolicy(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateLeavePolicy`, leavePolicyObj);
  }

  deleteLeavePolicyByLeavePolicyMasterId(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteLeavePolicyByLeavePolicyMasterId`, leavePolicyObj);
  }

  getAllLeavePolicy(){
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllLeavePolicy`);
  }

  getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getLeavePolicyByEmployentStatusAndLeaveTypeMasterId`, leavePolicyObj);
  }


  /* Home - Leave Summary */
  countMyApprovedLeaveApplicationsByLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/countMyApprovedLeaveApplicationsByLeaveType`, leaveObj);
  }

  countMyPendingLeaveApplicationsByLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/countMyPendingLeaveApplicationsByLeaveType`, leaveObj);
  }
}
