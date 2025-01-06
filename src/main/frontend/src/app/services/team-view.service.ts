import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { Leave } from '../models/leave';
import { environment } from 'src/environments/environment';
@Injectable({
  providedIn: 'root'
})
export class TeamViewService {
  
  private baseUrl:any = environment.baseUrl;

  constructor(private http : HttpClient) { }

  getAllTeamView(employeeObj : Employee){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamView`, employeeObj);
  }

  getAllTeamLeaveHistoryView(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamLeaveHistoryView`, leaveObj);
  }

  getAllTeamCompOffHistoryView(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamCompOffHistoryView`, leaveObj);
  }

  getAllTeamCompOffHistoryViewByEmpId(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamCompOffHistoryViewByEmpId`, leaveObj);
  }

  getAllTeamMemberView(employeeObj : Employee){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamMemberView`, employeeObj);
  }
  
  getDepartmentLeaveHistory(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getDepartmentLeaveHistory`, leaveObj);
  }

  revokeReporteeLeave(leaveObj: Leave){
    return this.http.post(`${this.baseUrl}`+ `api/revokeReporteeLeave`, leaveObj);
  }

  getDepartmentPendingLeaveHistory(leaveObj : Leave){
    return this.http.post(`${this.baseUrl}` + `api/getDepartmentPendingLeaveHistory`, leaveObj);
  }

}
