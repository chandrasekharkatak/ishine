import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';
import { Timesheet } from '../models/timesheet';
import { TimesheetRejectReason } from '../models/timesheetRejectionReasons';
import { EncryptionService } from './EncryptionService';
import { getEmployeeTimesheetAsCalenderByProjectId } from '../models/getEmployeeTimesheetAsCalenderByProjectId';
import { EmployeeTimesheetDTO } from '../models/EmployeeTimesheetDTO';
import { ProjectBasedBulkUploadPayload } from '../user-timesheet/team-timesheet/types';

@Injectable({
  providedIn: 'root'
})
export class TimesheetService {
  bulkApproveTimesheetsByIds(timesheetIds: number[]) {
    throw new Error('Method not implemented.');
  }
  bulkRejectTimesheetsByIds(payload: { rejectedBy: any; rejectionId: any; rejectReason: any; timesheetIds: any; }) {
    throw new Error('Method not implemented.');
  }
  bulkApproveDocuments(payload: { approvedBy: any; documents: any[]; }) {
    throw new Error('Method not implemented.');
  }


  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient, private encryptionService : EncryptionService) { }

  /* Add & Update Timesheet */
  addTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/addTimesheet`, timesheetObj);
  }

  addTimesheetWithClient(timesheetObj: Timesheet, selectedFile:any,selectedFile2:any) {
    const encryptedDto = this.encryptionService.encrypt(JSON.stringify(timesheetObj))
    const formData = new FormData();
  formData.append('dto', encryptedDto);
  formData.append('doc1', selectedFile);
  formData.append('doc2', selectedFile2);
    return this.http.post(`${this.baseUrl}` + `api/addTimesheetWithClient`, formData);
  }

  addTimesheetWithClientNew(timesheetObj: any, selectedFile:any,selectedFile2:any) {
    const encryptedDto = this.encryptionService.encrypt(JSON.stringify(timesheetObj))
    const formData = new FormData();
    formData.append('dto', encryptedDto);
    formData.append('filledDoc', selectedFile);
    formData.append('approvedDoc', selectedFile2);
    return this.http.post(`${this.baseUrl}` + `api/addTimesheetWithClientNew`, formData);
  }


  // ?doc=${selectedFile}

  updateTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheet`, timesheetObj);
  }

  updateTimesheetWithClient(timesheetObj: Timesheet, selectedFile:any,selectedFile2:any) {
    const encryptedDto = this.encryptionService.encrypt(JSON.stringify(timesheetObj))
    const formData = new FormData();
  formData.append('dto', encryptedDto);
  formData.append('doc1', selectedFile);
  formData.append('doc2', selectedFile2);
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheet`, formData);
  }

  bulkFinalDocumentUpload(finalFile: File, fromDate: string, toDate: string, empId: number,currentUserEmpId: number): Observable<any> {
  const formData = new FormData();
  formData.append('finalFile', finalFile);
  formData.append('fromDate', fromDate);
  formData.append('toDate', toDate);
  formData.append('empId', empId.toString());
  formData.append('createdBy',currentUserEmpId.toString());
    return this.http.post(`${this.baseUrl}` + `api/bulkFinalDocumentUpload`, formData);

  }

  getAllDisabledDateListForBulkDocSubmit(projectId : any, empId : any){
    const params = new HttpParams()
    .set('projectId', projectId)
    .set('empId', empId);

    return this.http.get(`${this.baseUrl}` + `api/getAllDisabledDateListForBulkDocSubmit`,{params});

  }

  getAllActivitiesByProjectIdandEmpId(timesheetObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllActivitiesByProjectIdandEmpId`, timesheetObj);
  }


  /* View My Timesheets */
  // getAllMyTimesheetsByEmpId(timesheetObj: Timesheet) {
  //   return this.http.post(`${this.baseUrl}` + `api/getAllMyTimesheetsByEmpId`, timesheetObj);
  // }
  getAllMyTeamTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamTimesheets`, timesheetObj);
  }

  getAllMyActivitiesByTimesheetId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyActivitiesByTimesheetId`, timesheetObj);
  }
  getbackdatedTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getLast7DaysTimesheetsByEmpId`, timesheetObj);
  }

  countMyReporteesTimesheetRequests(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/countMyReporteesTimesheetRequests`, timesheetObj);
  }
  getMyReporteesApprovedTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets`, timesheetObj);
  }
   getMyReporteesApprovedTimesheets2(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets2`, timesheetObj);
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

  /* Lightweight summary API for My Timesheets mini dashboard */
  getMyTimesheetSummary(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/summary`, timesheetObj);
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

  getActiveProjectsAndClientSideIdByEmpId(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getActiveProjectsAndClientSideIdByEmpId`, payload);
  }

  addTimesheet2(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/addTimesheet?`, timesheetObj);
  }

  getEmployeeListByProjectId(projectId: any, currentUser: any, date?: string) {
    let url = `${this.baseUrl}api/getEmployeeListByProjectId?projectId=${projectId}&currentUser=${currentUser}`;
    if (date) {
      url += `&date=${date}`;
    }
    return this.http.get(url);
  }

  getClientSideIdByProjectIdAndEmpId(projectId: any,empId: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getClientSideIdByProjectIdAndEmpId?projectId=${projectId}&empId=${empId}`, null);
  }

   getDocumentDataByDocId(docId: any){
    return this.http.get(`${this.baseUrl}` + `api/getDocumentDataByDocId?docId=${docId}`);
  }

   getFinalDocumentDataByDocId(timesheetId: any, docId: any) {
  return this.http.get(`${this.baseUrl}api/getFinalDocumentDataByDocId?timesheetId=${timesheetId}&docId=${docId}`);
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

  // getEmployeeViewForClientAttendanceStatus(details:any){
  //   return this.http.post(`${this.baseUrl}`+`api/getEmployeeViewForClientAttendanceStatus`,details);
  // }

  getRejectionReason(){
    return this.http.get(`${this.baseUrl}`+`api/getRejectionReason`);
  }

  setTimesheetRejectReason(rejectReasonObj: TimesheetRejectReason){
    return this.http.post(`${this.baseUrl}`+`api/setTimesheetRejectReason`,rejectReasonObj);
  }

  getRejectionReasonById(rejectionId:any){
    return this.http.get(`${this.baseUrl}`+`api/getRejectionReasonById?rejectionId=${rejectionId}`);
  }

  getProjectViewForClientAttendanceStatus(details:any){
    return this.http.post(`${this.baseUrl}`+`api/getProjectViewForClientAttendanceStatus`,details);
  }

  updateActiveByRejectIdId(rejectReasonObj: TimesheetRejectReason){
    return this.http.post(`${this.baseUrl}`+`api/updateActiveByRejectIdId`,rejectReasonObj);
  }


  getEmployeeTimesheetAsCalender(payload: any) {
    return this.http.post(`${this.baseUrl}api/getEmployeeTimesheetAsCalender`,payload);
  }



  // getMyReporteesTimesheetRequests(payload:any){
  //   return this.http.post(`${this.baseUrl}`+`api/getMyReporteesTimesheetRequests`,payload);
  // }

   approveTimesheetRequest(payload:any){
    return this.http.post(`${this.baseUrl}`+`api/approveTimesheetRequest`,payload);
  }

  getEmployeeTimesheetAsCalenderByProjectId(timesheetAsCalenderByProjectId:getEmployeeTimesheetAsCalenderByProjectId) {
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeTimesheetAsCalenderByProjectId`,timesheetAsCalenderByProjectId);
  }

  // getTimesheetDashboardCountForEmployee(month: any, year: any, empId: any,isClientDashboard:any,selectedBillableType:any,selectedEmployeeStatus : any,clientSideFilter:String) {
  //   const payload = {
  //     month: month,
  //     year: year,
  //     empId: empId,
  //     isClientDashboard: isClientDashboard,
  //     selectedBillableType :selectedBillableType,
  //     selectedEmployeeStatus :selectedEmployeeStatus,
  //     clientSideFilter:clientSideFilter
  //   };

  //   return this.http.post(`${this.baseUrl}api/getTimesheetDashboardCountForEmployee`, payload);
  // }

  getTimesheetDashboardCountForEmployee(
  month: any,
  year: any,
  empId: any,
  isClientDashboard: any,
  selectedBillableTypes: any,
  selectedEmployeeStatus: any,
  clientSideFilter: string,
  multiPos: any
) {
  const payload = {
    month: month,
    year: year,
    empId: empId,
    isClientDashboard: isClientDashboard,
    selectedBillableTypes: selectedBillableTypes,
    selectedEmployeeStatus: selectedEmployeeStatus,
    clientSideFilter: clientSideFilter,
    multiPOs: multiPos
  };

  return this.http.post(`${this.baseUrl}api/getTimesheetDashboardCountForEmployee`, payload);
}


  getTimesheetDashboardCountForProject(month: any, year: any,empId:any,isClientDashboard:any,selectedBillableType:any,selectedProjectStatus:any) {
    return this.http.get(`${this.baseUrl}api/getTimesheetDashboardCountForProject?month=${month}&year=${year}&empId=${empId}&isClientDashboard=${isClientDashboard}&billableType=${selectedBillableType}&projectActive=${selectedProjectStatus}`);
  }
 getLastFilledTimesheetByEmp(emp: Partial<Timesheet>) {
  return this.http.post(`${this.baseUrl}api/getLastFilledTimesheetByEmp`, emp);
}
  getDocumentsByEmpAndDate(empId: number, date: string,projectId:any): Observable<HttpResponse<Blob>> {
    const params = new HttpParams()
      .set('empId', empId.toString())
      .set('date', date)
      .set('projectId', projectId);

    return this.http.post(`${this.baseUrl}api/v2/timesheet/getDocumentsByEmpAndDate`, null, {
      params: params,
      responseType: 'blob',
      observe: 'response'
    });
  }

isEmployeeInTNMProject(empId: any): Observable<any> {
   return this.http.post(`${this.baseUrl}api/isInTNMProject?empId=${empId}`, null);
}


  getEmployeeSummaryOnExport(details:any){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeSummaryOnExport`,details);
  }

  getEmployeeViewForClientAttendanceStatus(timesheetAsCalenderByProjectId:getEmployeeTimesheetAsCalenderByProjectId) {
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeViewForClientAttendanceStatus`,timesheetAsCalenderByProjectId);
  }
  isClientMandetory(projectId:number){
    return this.http.post(`${this.baseUrl}`+`api/isClientIdMandetory`,projectId)
  }

  getProjectByMonthRangeAndEmpId(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getProjectByMonthRangeAndEmpId`, payload);
  }

  getMyReportees(timesheetObj:any){
     return this.http.post(`${this.baseUrl}` + `api/getMyReportees`, timesheetObj);
  }
// ------>
  getClientDetailsByProjectIdAndEmpId(timesheetObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getClientDetailsByProjectIdAndEmpId`, timesheetObj);
  }

  getOtherTeamMembersByDateAndProjectId(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getOtherTeamMembersByDateAndProjectId`, payload);
  }

  getMyReporteesAndClientSideProjectsInMonthYear(timesheetObj:any){
    return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/getMyReporteesAndClientSideProjectsInMonthYear`, timesheetObj);
  }

  getMyProjectsInMonthYear(timesheetObj:any){
     return this.http.post(`${this.baseUrl}` + `api/getMyProjectsInMonthYear`, timesheetObj);
  }

  wasEmployeeInClientProjCurrAndPrevMon(empId:any){
     return this.http.post(`${this.baseUrl}api/wasEmployeeInClientProjCurrAndPrevMon?empId=${empId}`, null);
  }

  downloadFinalDocuments(payload: any) {
  return this.http.post(
    `${this.baseUrl}api/v2/timesheet/downloadFinalDocuments`,
    payload,
    {
      responseType: 'blob'
    }
  );
}

getDocumentsBySelectedEmpId(payload: any): Observable<any> {
  return this.http.post(`${this.baseUrl}api/getDocumentsBySelectedEmpId`, payload);
}

getDepartmentStatusSummary(payload: any) {
  return this.http.post(
    `${this.baseUrl}api/getDepartmentStatusSummary`,
    payload
  );
}

/**
 * Evicts dashboard caches (timesheetDashboardCountForEmployee, employeeViewForClientAttendanceStatus,
 * departmentStatusSummary) so the next API calls return fresh data. Call before refresh on HR dashboard.
 */
evictTimesheetDashboardCache(): Observable<any> {
  return this.http.post(`${this.baseUrl}api/evictTimesheetDashboardCache`, {});
}

  bulkFinalUploadProjectBased(payload: ProjectBasedBulkUploadPayload, file: File) {
    const formData = new FormData();

    formData.append('finalFile', file);

    formData.append(
      'finalBulkUploadDTO',
      new Blob([JSON.stringify(payload)], { type: 'application/json' })
    );

    return this.http.post(
      `${this.baseUrl}api/v2/timesheet/bulkFinalUploadProjectBased`,
      formData
    );
  }

  getPreviousMinusDays(){
    return this.http.get(
      `${this.baseUrl}api/v2/timesheet/getPreviousMinusDays`
    )
  }

  getMyReporteesTimesheetRequests(payload:any){
    return this.http.post(`${this.baseUrl}`+`api/getMyReporteesTimesheetRequestsNew`,payload);
  }

  getEmployeeTeamDepartmentId(teamId:number,empId:number,date:string){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeTeamDepartment`,{teamId,empId,date});
  }

  getAllHalfDayLeaves(empId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}api/v2/timesheet/half-day/${empId}`);
  }

  generateFileName(request: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}api/v2/timesheet/generate-name`,
      request
    );
  }
 
}
