import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';
import { Timesheet } from '../models/timesheet';
import { TimesheetRejectReason } from '../models/timesheetRejectionReasons';

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

  addTimesheetWithClient(timesheetObj: Timesheet, selectedFile:any,selectedFile2:any) {
    const formData = new FormData();
  formData.append('dto', new Blob([JSON.stringify(timesheetObj)], { type: 'application/json' }));
  formData.append('doc1', selectedFile);
  formData.append('doc2', selectedFile2);
    return this.http.post(`${this.baseUrl}` + `api/addTimesheetWithClient`, formData);
  }
  // ?doc=${selectedFile}
  
  updateTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheet`, timesheetObj);
  }

  updateTimesheetWithClient(timesheetObj: Timesheet, selectedFile:any) {
     const formData = new FormData();
  formData.append('dto', new Blob([JSON.stringify(timesheetObj)], { type: 'application/json' }));
  formData.append('doc', selectedFile);
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheet`, formData);
  }

  bulkFinalDocumentUpload(finalFile: File, fromDate: string, toDate: string, empId: number): Observable<any> {
  const formData = new FormData();
  formData.append('finalFile', finalFile);
  formData.append('fromDate', fromDate);
  formData.append('toDate', toDate);
  formData.append('empId', empId.toString());
    return this.http.post(`${this.baseUrl}` + `api/bulkFinalDocumentUpload`, formData);

  }

  getAllDisabledDateListForBulkDocSubmit(projectId : any, empId : any){
    const params = new HttpParams()
    .set('projectId', projectId)
    .set('empId', empId);

    return this.http.get(`${this.baseUrl}` + `api/getAllDisabledDateListForBulkDocSubmit`,{params});

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
    return this.http.post(`${this.baseUrl}` + `api/getOneMonthTimesheetReport`,timesheetObj);
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

  checkIfProjectRequiresClientId(projectId:any){
    return this.http.get(`${this.baseUrl}` + `api/checkIfProjectRequiresClientId?projectId=${projectId}`);
  }
  totalVmsFilledCount(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/totalVmsFilledCount`,timesheetObj);
  }

  totalIshineFilledCount(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/totalIshineFilledCount`,timesheetObj);
  }

  finalDocumentApproval(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/finalDocumentApproval`,timesheetObj);
  }

   getVmsDocumentApprovalStatusWiseCount(){
    return this.http.get(`${this.baseUrl}`+`api/getVmsDocumentApprovalStatusWiseCount`);
   }
  totalvmsNotFilled(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/totalvmsNotFilled`,timesheetObj);
  }

  totalIshineNotFilledCount(timesheetObj: Timesheet){
    return this.http.post(`${this.baseUrl}`+`api/totalIshineNotFilledCount`,timesheetObj);
  }

  getEmployeeViewForClientAttendanceStatus(){
    return this.http.get(`${this.baseUrl}`+`api/getEmployeeViewForClientAttendanceStatus`);
  }

  getRejectionReason(){
    return this.http.get(`${this.baseUrl}`+`api/getRejectionReason`);
  }

  setTimesheetRejectReason(rejectReasonObj: TimesheetRejectReason){
    return this.http.post(`${this.baseUrl}`+`api/setTimesheetRejectReason`,rejectReasonObj);
  }

  getRejectionReasonById(rejectionId:any){
    return this.http.get(`${this.baseUrl}`+`api/getRejectionReasonById?rejectionId=${rejectionId}`);
  }

  getProjectViewForClientAttendanceStatus(){
    return this.http.get(`${this.baseUrl}`+`api/getProjectViewForClientAttendanceStatus`);
  }

  updateActiveByRejectIdId(rejectReasonObj: TimesheetRejectReason){
    return this.http.post(`${this.baseUrl}`+`api/updateActiveByRejectIdId`,rejectReasonObj);
  }


  getEmployeeTimesheetAsCalender(projectId: any, month: any, year: any) {
    return this.http.get(`${this.baseUrl}api/getEmployeeTimesheetAsCalender?projectId=${projectId}&month=${month}&year=${year}`);
  }
  
   
  getAllEmployeeDSROfRM(payload:any){
    return this.http.post(`${this.baseUrl}`+`api/getAllEmployeeDSROfRM`,payload);
  }
  
   approveTimesheetRequest(payload:any){
    return this.http.post(`${this.baseUrl}`+`api/approveTimesheetRequest`,payload);
  }
  
}
