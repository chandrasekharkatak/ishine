import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Activity } from '../models/activity';
import { Employee } from '../models/employee';
import { Team } from '../models/team';
import { environment } from 'src/environments/environment';
import { MigrateTeams } from '../models/migrateTeam';
import { PoDetails } from '../models/poDetails';
import { RmgTeamMember } from '../models/rmgTeamMember';
import { RmgTeam } from '../models/rmgTeam';
import { EmployeeProjectTimesheetDto } from '../models/employeeProjectTimesheetDto';

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private baseUrl: any = environment.baseUrl;

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

  checkTeamName(teamObj: Team) {
    return this.http.post(`${this.baseUrl}` + `api/checkTeamName`, teamObj);
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

  getAllTeamsByPoId(poId: any) {
    let httpParams = new HttpParams().append("poId", poId);
    return this.http.get(`${this.baseUrl}` + `api/getAllTeamsByPoId`, { params: httpParams });
  }

  getTeamDetailsByTeamId(teamId: any, projectId: any) {
    let httpParams = new HttpParams().append("teamId", teamId).append("projectId", projectId);
    return this.http.get(`${this.baseUrl}` + `api/getTeamDetailsByTeamId`, { params: httpParams });
  }

  getActiveTeamDetailsByPoId(poId: any) {
    let httpParams = new HttpParams().append("poId", poId);
    return this.http.get(`${this.baseUrl}` + `api/getActiveTeamDetailsByPoId`, { params: httpParams });
  }

  migrateTeam(migrateTeam: MigrateTeams) {
    return this.http.post(`${this.baseUrl}` + `api/migrateTeam`, migrateTeam);
  }

  getTeamDetailsByTeamIdsAndProjectId(poObj: PoDetails) {
    return this.http.post(`${this.baseUrl}` + `api/getTeamDetailsByTeamIdsAndProjectId`, poObj);
  }

  deleteSelectedTeams(deleteTeamsPo: PoDetails) {
    return this.http.post(`${this.baseUrl}` + `api/deleteSelectedTeams`, deleteTeamsPo);
  }

  addOrUpdateTeamDetails(updateTeamPoDetails: PoDetails) {
    return this.http.post(`${this.baseUrl}` + `api/addOrUpdateTeamDetails`, updateTeamPoDetails);
  }

  addOrUpdateTeamMembers(rmgTeam: RmgTeam) {
    return this.http.post(`${this.baseUrl}` + `api/addOrUpdateTeamMembers`, rmgTeam);
  }

  getActiveTeamDetailsByProjectId(projectId: any) {
    let httpParams = new HttpParams().append("projectId", projectId);
    return this.http.get(`${this.baseUrl}` + `api/getActiveTeamDetailsByProjectId`, { params: httpParams });
  }

  updateDefaultProjectCompletion(rmgTeamMember: RmgTeamMember) {
    return this.http.post(`${this.baseUrl}` + `api/updateDefaultProjectCompletion`, rmgTeamMember);
  }

  removeTeamMembersFromProject(rmgTeam :RmgTeam) {
    return this.http.post(`${this.baseUrl}` + `api/removeTeamMembersFromProject`, rmgTeam);
  }

  migrateTeamMembers(migrateTeam: MigrateTeams) {
    return this.http.post(`${this.baseUrl}` + `api/migrateTeamMembers`, migrateTeam);
  }

  validateEmployeeProjectStartDate(rmgTeamMember: RmgTeamMember) {
    return this.http.post(`${this.baseUrl}` + `api/validateEmployeeProjectStartDate`, rmgTeamMember);
  }

  extendTeamMembersEndDate(rmgTeam :RmgTeam) {
    return this.http.post(`${this.baseUrl}` + `api/extendTeamMembersEndDate`, rmgTeam);
  }

  getTeamDetailsByProjectId(poObj: PoDetails) {
    return this.http.post(`${this.baseUrl}` + `api/getTeamDetailsByProjectId`, poObj);
  }

  updateMemberShadowMapping(rmgTeamMember: RmgTeamMember) {
    return this.http.post(`${this.baseUrl}` + `api/updateMemberShadowMapping`, rmgTeamMember);
  }

  updateTeamMembersStartDateAndEndDate(employeeObj: EmployeeProjectTimesheetDto) {
    return this.http.post(`${this.baseUrl}` + `api/updateTeamMembersStartDateAndEndDate`, employeeObj);
  }

  getMaxEmployeeTeamMapStartDate(empId: any) {
    let httpParams = new HttpParams().append("empId", empId);
    return this.http.get(`${this.baseUrl}` + `api/getMaxEmployeeTeamMapStartDate`, { params: httpParams });
  }

  getTeamMemberDetailsByEmpIdAndProjectId(projectId: any, empId :any, employeeTeamMapId:any) {
    let httpParams = new HttpParams().append("projectId", projectId).append("empId", empId).append("employeeTeamMapId", employeeTeamMapId);
    return this.http.get(`${this.baseUrl}` + `api/getTeamMemberDetailsByEmpIdAndProjectId`, { params: httpParams });
  }
  
}