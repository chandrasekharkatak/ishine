import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Activity } from '../models/activity';
import { Team } from '../models/team';

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  // Team Configuration 
  createTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createTeam`, teamObj);
  }

  updateTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateTeam`, teamObj);
  }

  deleteTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteTeam`, teamObj);
  }

  getAllTeamsByProjectId(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllTeamsByProjectId`, teamObj);
  }

  getTeamMembersByTeamId(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getTeamMembersByTeamId`, teamObj);
  }


  // Activity Configuration
  createActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/createActivity`, activityObj);
  }

  updateActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateActivity`, activityObj);
  }

  deleteActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/deleteActivity`, activityObj);
  }

  getAllActivitiesByProjectIdAndTeamId(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllActivitiesByProjectIdAndTeamId`, activityObj);
  }

  checkTeamName(teamObj :Team){
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/checkTeamName` , teamObj);
  }

}
