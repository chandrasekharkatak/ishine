import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Timesheet } from '../models/timesheet';

@Injectable({
  providedIn: 'root'
})
export class TimesheetService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* Add & Update Timesheet */
  addTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/addTimesheet`, timesheetObj);
  }

  updateTimesheet(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateTimesheet`, timesheetObj);
  }

  getAllProjectsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllProjectsByEmpId`, timesheetObj);
  }

  getAllActivitiesByProjectIdandEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllActivitiesByProjectIdandEmpId`, timesheetObj);
  }


  /* View My Timesheets */
  getAllMyTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllMyTimesheetsByEmpId`, timesheetObj);
  }
  getAllMyActivitiesByTimesheetId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllMyActivitiesByTimesheetId`, timesheetObj);
  }
  getbackdatedTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getLast7DaysTimesheetsByEmpId`, timesheetObj);
  }

  /* View Reportee's Timesheets */
  getMyReporteesTimesheetRequests(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getMyReporteesTimesheetRequests`, timesheetObj);
  }
  getMyReporteesApprovedTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getMyReporteesApprovedTimesheets`, timesheetObj);
  }
  updateTimesheetRequestById(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateTimesheetRequestById`, timesheetObj);
  }
}
