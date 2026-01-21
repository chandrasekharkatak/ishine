import { HttpClient,HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Activity } from '../models/activity';
import { Employee } from '../models/employee';
import { Team } from '../models/team';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  // Team Configuration 
  createTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/createTeam`, teamObj);
  }

  updateTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/updateTeam`, teamObj);
  }

  deleteTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/deleteTeam`, teamObj);
  }

  getAllTeamsByProjectId(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getAllTeamsByProjectId`, teamObj);
  }

  getTeamMembersByTeamId(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getTeamMembersByTeamId`, teamObj);
  }

  getAllMyTeamsByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getAllMyTeamsByEmpId`, employeeObj);
  }

  getMappedActivityPreview(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getMappedActivityPreview`, teamObj);
  }

  getMappedActivityInUpdateTeam(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getMappedActivityInUpdateTeam`, teamObj);
  }

  getAllTeams() {
    return this.http.get(`${this.baseUrl}` + `api/getAllTeams`);
  }

  // Activity Configuration
  createActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `api/createActivity`, activityObj);
  }

  updateActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `api/updateActivity`, activityObj);
  }

  deleteActivity(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `api/deleteActivity`, activityObj);
  }

  getAllActivitiesByProjectIdAndTeamId(activityObj: Activity) {
    return this.http.post(`${this.baseUrl}` + `api/getAllActivitiesByProjectIdAndTeamId`, activityObj);
  }

  checkTeamName(teamObj :Team){
    return this.http.post(`${this.baseUrl}` + `api/checkTeamName` , teamObj);
  }

  // Activity template 

  createActivityTemplate(team: Team) {
    return this.http.post(`${this.baseUrl}` + `api/createActivityTemplate`, team);
  }

  getActivityTemplate(team: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getActivityTemplate`, team);
  }

  getActivityTemplateById(team: Team) {
    return this.http.post(`${this.baseUrl}` + `api/getActivityTemplateById`, team);
  }

  updateActivityTemplate(team: Team) {
    return this.http.post(`${this.baseUrl}` + `api/updateActivityTemplate`, team);
  }

  getAllTeamsAndRoleWiseMembersByPoId(poId: any) {
    let httpParams = new HttpParams().append("poId", poId);
    return this.http.get(`${this.baseUrl}` + `api/getAllTeamsAndRoleWiseMembersByPoId`, { params: httpParams });
  }

}
