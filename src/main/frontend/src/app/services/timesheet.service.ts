import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Timesheet } from '../models/timesheet';
import { Query } from '../models/query';

@Injectable({
  providedIn: 'root'
})
export class TimesheetService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* Add & Update Timesheet */
  addTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/addTimesheet`, timesheetObj);
  }

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


}
