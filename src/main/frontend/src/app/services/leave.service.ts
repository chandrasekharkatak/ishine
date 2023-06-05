import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Leave } from '../models/leave';
import { Query } from '../models/query';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LeaveService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  /* Leave */
  applyLeave(leaveObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/applyLeave`, leaveObj);
  }

  updateLeaveStatus(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateLeaveStatus`, leaveObj);
  }

  getAllMyLeaveApplicationsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyLeaveApplicationsByEmpId`, leaveObj);
  }

  getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamsPendingLeaveApplicationsByManagerId`, leaveObj);
  }

  countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/countAllMyTeamsPendingLeaveApplicationsByManagerId`, leaveObj);
  }

  getLeaveLogsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getLeaveLogsByEmpId`, leaveObj);
  }

  getMyLeaveBalancesByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getMyLeaveBalancesByEmpId`, leaveObj);
  }

  updateLeavesByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateLeavesByEmpId`, leaveObj);
  }

  getAppliedLeaveApplicationsByEmpIdAndDateRange(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAppliedLeaveApplicationsByEmpIdAndDateRange`, leaveObj);
  }

  deletePendingLeave(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/deletePendingLeave`, leaveObj);
  }

  updatePendingLeave(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updatePendingLeave`, leaveObj);
  }

  revokeApprovedLeaveApplication(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/revokeApprovedLeaveApplication`, leaveObj);
  }

  getRevokeLeaveApplicationByEmpId(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getRevokeLeaveApplicationByEmpId`, leaveObj);
  }

  getAllMyTeamLeaveRevokeApplicationsByEmpId(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamLeaveRevokeApplicationsByEmpId`, leaveObj);
  }

  getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId`, leaveObj);
  }

  updateRevokeLeaveStatus(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateRevokeLeaveStatus`, leaveObj);
  }

  getOverlappedTeamMemberLeave(leaveObj : Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getOverlappedTeamMemberLeave`, leaveObj);
  }

  checkEmployeeInProbationByFromDate(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeeInProbationByFromDate`, leaveObj);
  }

  /* Leave Type */
  createLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/createLeaveType`, leaveObj);
  }

  updateLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateLeaveType`, leaveObj);
  }

  getAllLeaveTypes() {
    return this.http.get(`${this.baseUrl}` + `api/getAllLeaveTypes`);
  }

  getAllLeaveTypesByLeavePolicies(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllLeaveTypesByLeavePolicies`, leaveObj);
  }

  deleteLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/deleteLeaveType`, leaveObj);
  }

  changeLeaveTypeMapping(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/changeLeaveTypeMapping`, leaveObj);
  }

  checkLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/checkLeaveType`, leaveObj);
  }

  /* Comp off */
  getAllCompOffReasons(){
    return this.http.get(`${this.baseUrl}` + `api/getAllCompOffReasons`);
  }

  applyForCompOff(compOffObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/applyForCompOff`, compOffObj);
  }

  updateCompOffById(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateCompOffById`, compOffObj);
  }

  getAllCompOffRequestsByEmpId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllCompOffRequestsByEmpId`, compOffObj);
  }

  getPendingCompOffRequestsByManagerId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getPendingCompOffRequestsByManagerId`, compOffObj);
  }

  countPendingCompOffRequestsByManagerId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/countPendingCompOffRequestsByManagerId`, compOffObj);
  }

  updateCompOff(compOffObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateCompOff`, compOffObj);
  }

  deleteCompOff(compOffObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/deleteCompOff`, compOffObj);
  }

  getCompOffBalanceDetailsByEmpIdAndFromDate(compOffObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getCompOffBalanceDetailsByEmpIdAndFromDate`, compOffObj);
  }

  /* Leave Policy */
  addLeavePolicy(leavePolicyObj:Leave) {
    return this.http.post(`${this.baseUrl}` + `api/addLeavePolicy`, leavePolicyObj);
  }

  updateLeavePolicy(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/updateLeavePolicy`, leavePolicyObj);
  }

  deleteLeavePolicyByLeavePolicyMasterId(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/deleteLeavePolicyByLeavePolicyMasterId`, leavePolicyObj);
  }

  getAllLeavePolicy(){
    return this.http.get(`${this.baseUrl}` + `api/getAllLeavePolicy`);
  }

  getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(leavePolicyObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getLeavePolicyByEmployentStatusAndLeaveTypeMasterId`, leavePolicyObj);
  }

  getAllMyTeamApplicationsByEmpId(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamApplicationsByEmpId`, leaveObj);
  }

  /* Home - Leave Summary */
  countMyRejectedLeaveApplicationsByLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/countMyRejectedLeaveApplicationsByLeaveType`, leaveObj);
  }

  countMyApprovedLeaveApplicationsByLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/countMyApprovedLeaveApplicationsByLeaveType`, leaveObj);
  }

  countMyPendingLeaveApplicationsByLeaveType(leaveObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/countMyPendingLeaveApplicationsByLeaveType`, leaveObj);
  }

  /* Reports */
  leaveReport(){
    return this.http.get(`${this.baseUrl}` + `api/leaveReport`);
  }

  customQueryForLeaveReport(queryObj: Query) {
    return this.http.post(`${this.baseUrl}` + `api/customQueryForLeaveReport`, queryObj);
  }

  getValueOptionData(queryObj: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getValueOptionData`, queryObj);
  }

  /* Report Dashboard */
  getLast8DaysLeaveReport(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}` + `api/getLast8DaysLeaveReport`, leaveObj);
  }

  getLeaveTrendAnalysisReport(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getLeaveTrendAnalysisReport`, leaveObj);
  }

  customQueryForLeaveTrendAnalysisReport(queryObj: Query){
    return this.http.post(`${this.baseUrl}` + `api/customQueryForLeaveTrendAnalysisReport`, queryObj);
  }

  bulkApproveLeaveRequest(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}` + `api/bulkApproveLeaveRequest`, leaveObj);
  }

  bulkRejectLeaveRequest(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}`+ `api/bulkRejectLeaveRequest`, leaveObj);
  }

  getAllLeaveBalanceByEmpId(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}`+ `api/getAllLeaveBalanceByEmpId`, leaveObj);
  }

  getHolidayWeekOffSize(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}`+ `api/getHolidayWeekOffSize`, leaveObj);
  }
}