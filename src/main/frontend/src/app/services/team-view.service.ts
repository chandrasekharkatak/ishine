import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { Leave } from '../models/leave';
@Injectable({
  providedIn: 'root'
})
export class TeamViewService {
  
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

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

  getAllTeamMemberView(employeeObj : Employee){
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamMemberView`, employeeObj);
  }
  
}
