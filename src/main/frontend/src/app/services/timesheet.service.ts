import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Timesheet } from '../models/timesheet';
import { Query } from '../models/query';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TimesheetService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  /* Add & Update Timesheet */
  addTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/addTimesheet`, timesheetObj);
  }

  addTimesheetWithClient(timesheetObj: Timesheet, selectedFile:any) {
    const formData = new FormData();
  formData.append('dto', new Blob([JSON.stringify(timesheetObj)], { type: 'application/json' }));
  formData.append('doc', selectedFile);
    return this.http.post(`${this.baseUrl}` + `api/addTimesheetWithClient`, formData);
  }
  // ?doc=${selectedFile}
  
  updateTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheet`, timesheetObj);
  }

  getAllProjectsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectsByEmpId`, timesheetObj);
  }

  getAllActivitiesByProjectIdandEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllActivitiesByProjectIdandEmpId`, timesheetObj);
  }


  /* View My Timesheets */
  getAllMyTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTimesheetsByEmpId`, timesheetObj);
  }
  getAllMyTeamTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamTimesheets`, timesheetObj);
  }

  getAllMyActivitiesByTimesheetId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyActivitiesByTimesheetId`, timesheetObj);
  }
  getbackdatedTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getLast7DaysTimesheetsByEmpId`, timesheetObj);
  }

  /* View Reportee's Timesheets */
  getMyReporteesTimesheetRequests(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getMyReporteesTimesheetRequests`, timesheetObj);
  }
  countMyReporteesTimesheetRequests(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/countMyReporteesTimesheetRequests`, timesheetObj);
  }
  getMyReporteesApprovedTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets`, timesheetObj);
  }
  updateTimesheetRequestById(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheetRequestById`, timesheetObj);
  }
  revokeApprovedTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/revokeApprovedTimesheet`, timesheetObj);
  }

  /* Timesheet for Home Page */
  getTimesheetsForHomePageByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getTimesheetsForHomePageByEmpId`, timesheetObj);
  }

  /* Reports */
  timesheetReport(){
    return this.http.get(`${this.baseUrl}` + `api/timesheetReport`);
  }

  customTimesheetApplicationReport(queryObj: Query) {
    return this.http.post(`${this.baseUrl}` + `api/customTimesheetApplicationReport`, queryObj);
  }

  customQueryForTimesheetSummaryChart(queryObj: Query){
    return this.http.post(`${this.baseUrl}` + `api/customQueryForTimesheetSummaryChart`, queryObj);
  }

  getAllLeaveTimesheetsWithoutLeaveApplication(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllLeaveTimesheetsWithoutLeaveApplication`, timesheetObj);
  }


  /* Reports Dashboard */
  getLast9DaysTimesheetReport(){
    return this.http.get(`${this.baseUrl}` + `api/getLast9DaysTimesheetReport`);
  }

  bulkApproveTimesheetRequest(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}` + `api/bulkApproveTimesheetRequest`, timesheetObj);
  }

  bulkRejectTimesheetRequest(timesheetObj:Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/bulkRejectTimesheetRequest`, timesheetObj);
  }

  /* Advance filter [view Timesheet] */

  getCustomFilteredTimesheet(timesheetObj:Timesheet){
    return this.http.post(`${this.baseUrl}` + `api/getCustomFilteredTimesheet`, timesheetObj);
  }

  /* Server Date*/

  getServerDate(){
    return this.http.get(`${this.baseUrl}` + `api/getServerDate`);
  }

  getAllOrDeptWiseEmployeeTimesheetReport(FilteredTimesheet: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getAllOrDeptWiseEmployeeTimesheetReport`,FilteredTimesheet);
  }

  getLastFilledTimesheetByEmpId(empId: number): Observable<any> {
  const payload = { empId }; 
  return this.http.post<any>(`${this.baseUrl}`+`api/getLastFilledTimesheetByEmpId`, payload);
}

  getActiveProjectsByEmpId(empId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getActiveProjectsByEmpId?empId=${empId}`, null);
  }

  getClientSideIdByProjectId(projectId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getClientSideIdByProjectId?projectId=${projectId}`, null);
  }

  fetchEmploymentIdByEmpId(empId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/fetchEmploymentIdByEmpId?empId=${empId}`, null);
  }

  getEmployeeMonthlyTimesheet(timesheetObj: Timesheet){
    return this.http.get(`${this.baseUrl}` + `api/getOneMonthTimesheetReport`);
  }

  createClientSideIdMapping(empClientSideObj: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/createClientSideIdMapping`,empClientSideObj);
  }

  updateClientSideIdMapping(empClientSideObj: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/updateClientSideIdMapping`,empClientSideObj);
  }

  getActiveProjectsAndClientSideIdByEmpId(empId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getActiveProjectsAndClientSideIdByEmpId?empId=${empId}`, null);
  }

  addTimesheet2(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/addTimesheet?`, timesheetObj);
  }

  getEmployeeListByProjectId(projectId: any,currentUser: any){
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeListByProjectId?projectId=${projectId}&currentUser=${currentUser}`);
  }

  getClientSideIdByProjectIdAndEmpId(projectId: any,empId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getClientSideIdByProjectIdAndEmpId?projectId=${projectId}&empId=${empId}`, null);
  }

   getDocumentDataByDocId(docId: any){
    return this.http.get(`${this.baseUrl}` + `api/getDocumentDataByDocId?docId=${docId}`);
  }

}
