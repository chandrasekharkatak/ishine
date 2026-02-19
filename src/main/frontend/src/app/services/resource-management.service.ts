import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Project } from '../models/project';
import { ProjectFilterDTO } from '../models/projectFilterDTO';

import { updateHasClientSideId } from '../models/updateHasClientSideId';
import { LiftAndShift } from '../models/liftAndShift';
import { RestoreProjectPayload } from '../models/restoreProjectPayload';
import { RMGDashboardProjectRequest } from '../models/rmgDashboardProjectRequest';

@Injectable({
  providedIn: 'root'
})
export class ResourceManagementService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDraftProjectInfo(projectObj: Project) {
    return this.http.post(`${this.baseUrl}` + `api/createDraftProjectInfo`, projectObj);
  }

  getTeamListByProjectName(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/getTeamListByProjectName`, projectObj);
  }

  alreadyCreatedTeam(){
    return this.http.get(`${this.baseUrl}` + `api/alreadyCreatedTeam`);
  }

  getPendingForApprovalProject(){
    return this.http.get(`${this.baseUrl}` + `api/getPendingForApprovalProject`);
  }

  approvePendingProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/approvePendingProject`, projectObj);
  }

  rejectPendingProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/rejectPendingProject`, projectObj);
  }

  sendProjectApproval(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/sendProjectApproval`, projectObj);
  }

  bulkSyncProject(projectObj: Project){
    return this.http.post(`${this.baseUrl}` + `api/bulkSyncProject`, projectObj);
  }

  getInternalProject(){
    return this.http.get(`${this.baseUrl}` + `api/getInternalProject`);
  }

  getPoProjectDetailsForPoProjects(){
    return this.http.get(`${this.baseUrl}` + `api/getPoProjectDetailsForPoProjects`);
  }

  syncPoProjectDetailsByProjectId(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/syncPoProjectDetailsByProjectId`,project);
  }

  sendEmailNotificationToBDTeam(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/sendEmailNotificationToBDTeam`,project);
  }


  completionDateOfProject(project: Project){
    return this.http.post(`${this.baseUrl}` + `api/completionDateOfProject`,project);
  }

  combinedPOINTERNALList(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/combinedPOINTERNALList`,ProjectFilterDTO);
  }

  rbacInternalProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacInternalProjects`,ProjectFilterDTO);
  }

  rbacShankhProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacShankhProjects`,ProjectFilterDTO);
  }

  rbacAllShankhInternalProjects(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacAllShankhInternalProjects`,ProjectFilterDTO);
  }

  rbacBothShankhInternal(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/rbacBothShankhInternal`,ProjectFilterDTO);
  }
  projectLessEmployees(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/projectLessEmployees`,ProjectFilterDTO);
  }

  // getResourceRequirementByPoProjectId(id: any) {
  //   return this.http.get(`${this.baseUrl}`+`api/getResourceRequirementByPoProjectId`, {params: { id: id}});
  // }

  getResourceRequirementByPoProjectId(id: any,type:string) {
    return this.http.get(`${this.baseUrl}`+`api/getResourceRequirementByPoProjectId`, {params: { id: id, type: type }});
  }

  getEmployeeInformation(empId: any){
    return this.http.get(`${this.baseUrl}`+`api/getEmployeeInformation`, {params: { empId: empId }});
  }

  totalEmployeeCount(){
    return this.http.get(`${this.baseUrl}`+`api/totalEmployeeCount`);
  }

  exceptionEmployeeReport(ProjectFilterDTO:ProjectFilterDTO){
    return this.http.post(`${this.baseUrl}` + `api/exceptionEmployeeReport`,ProjectFilterDTO);
  }

  getPreviousDefaultProjectDetails(empId: any){
    return this.http.get(`${this.baseUrl}`+`api/getPreviousDefaultProjectDetails`, {params: { empId: empId }});
  }

  setDefaultProjectUpdateBillable(defaultProjectUpdate){
    return this.http.post(`${this.baseUrl}` + `api/setDefaultProjectUpdateBillable`,defaultProjectUpdate);
  }

  getProjectDetailsForBulkDefaultUpdate(){
    return this.http.get(`${this.baseUrl}`+`api/getProjectDetailsForBulkDefaultUpdate`);
  }

  getEmployeeInformationBulk(empIds: any){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeInformationBulk`, empIds);
  }

  setProjectMappingAndDefaultProject(setDefaultProjectObj: any){
    return this.http.post(`${this.baseUrl}`+`api/setProjectMappingAndDefaultProject`, setDefaultProjectObj);
  }

  getEmployeeInformationForDefaultProject(setDefaultProjectObj: any){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeInformationForDefaultProject`, setDefaultProjectObj);
  }
  getAllResourceRequirementForProject(data:Project){
    return this.http.post(`${this.baseUrl}`+`api/getAllResourceRequirementForProject`,data);
  }

  getBenchEmployeeMoreThan30Days(projectFilterDTO){
    return this.http.post(`${this.baseUrl}`+`api/getBenchEmployeeMoreThan30Days`,projectFilterDTO);
  }

  getProjectTimesheetSummary(request:any){
    return this.http.post(`${this.baseUrl}` + `api/getProjectTimesheetSummary`,request);
  }


  getEmployeesWithoutBillability(projectFilterDTO){

 return this.http.post(`${this.baseUrl}`+`api/getEmployessWithoutBillable`,projectFilterDTO);
  }

  getProjectsunfilledTimesheet(payload:any){
 return this.http.post(`${this.baseUrl}`+`api/rbacUnfilledTimesheetsProjects`,payload);
  }

  combinedPOINTERNALCountList(projectFilterDTO:ProjectFilterDTO) {
    return this.http.post(`${this.baseUrl}` + `api/combinedPOINTERNALCountList`, projectFilterDTO);
  }

  combinedPOINTERNALDataList(projectFilterDTO:ProjectFilterDTO) {
    return this.http.post(`${this.baseUrl}` + `api/combinedPOINTERNALDataList`, projectFilterDTO);
  }

  getFixedCostProjectList(payload: any) {
    return this.http.post(`${this.baseUrl}` + `api/getCompletedFixedCostProjects`, payload);
  }

  getFixedCostCount(ProjectFilterDTO: ProjectFilterDTO) {
    return this.http.post(`${this.baseUrl}` + `api/getFixedCostCount`, ProjectFilterDTO);
  }

  updateHasClientSideId(obj: updateHasClientSideId){
    return this.http.post(`${this.baseUrl}` + `api/updateHasClientSideId`, obj);
  }

  getActiveProjectList(){
    return this.http.get(`${this.baseUrl}`+`api/getActiveProjectList`);
  }

  liftAndShiftTeams(obj: LiftAndShift){
    return this.http.post(`${this.baseUrl}` + `api/liftAndShiftTeams`, obj);
  }

  fetchHasClientSideId(obj: updateHasClientSideId){
    return this.http.post(`${this.baseUrl}` + `api/fetchHasClientSideId`, obj);
  }
  //   getFixedCostCount(ProjectFilterDTO: ProjectFilterDTO) {
  //   return this.http.post(`${this.baseUrl}` + `api/getFixedCostCount`, ProjectFilterDTO);
  // }
  //   getFixedCostProjectList(payload: any) {
  //   return this.http.post(`${this.baseUrl}` + `api/getCompletedFixedCostProjects`, payload);
  // }

  getCertficatesRbac(filterMatrix:any){
     return this.http.post(`${this.baseUrl}` + `api/matrixCertificationDropdownRbac`, filterMatrix);
  }

  getDepartmentsRbac(filterMatrix:any){
     return this.http.post(`${this.baseUrl}` + `api/matrixDepartmentDropdownRbac`, filterMatrix);
  }


  getEmployeeCountSDeptwise(filterMatrix:any){
      return this.http.post(`${this.baseUrl}` + `api/empCountSEDepartmentsWise`, filterMatrix);
  }

  searchEmployeesBySkillsAndCertificates(filterMatrixObj:any){
    return this.http.post(`${this.baseUrl}` + `api/searchEmployeesBySkillsAndCertificates`, filterMatrixObj);
  }

  searchEnployeesNotInSearch(filterMatrixObj:any){
     return this.http.post(`${this.baseUrl}` + `api/searchEmployeesNotInSearch`, filterMatrixObj);
  }

  restorePreviousStateOfProject(obj: RestoreProjectPayload){
    return this.http.post(`${this.baseUrl}` + `api/restorePreviousStateOfProject`, obj);
  }


  getProjectAssignedDataByProjectId(id:number,totalRequirements:number){
    return this.http.get(`${this.baseUrl}`+`api/getProjectAssignedDataByProjectId`,{params:{id:id,totalRequirements:totalRequirements}})
  }

  getProjectConfigurationDetailsByProjectId(projectId: any, isAllProjects:boolean) {
    let httpParams = new HttpParams().append("projectId", projectId).append("isAllProjects", isAllProjects);
    return this.http.get(`${this.baseUrl}` + `api/getProjectConfigurationDetailsByProjectId`, { params: httpParams });
  }

  getEmployeesInformation(empId: any, projectId: any) {
    let httpParams = new HttpParams().append("empIds", empId).append("projectId", projectId);
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeInformation`, { params: httpParams });
  }

  getResourceRequirementByPoId(poId: any) {
    let httpParams = new HttpParams().append("poId", poId);
    return this.http.get(`${this.baseUrl}` + `api/getResourceRequirementByPoId`, { params: httpParams });
  }

  getActivePoDetailsByProjectId(projectId: any) {
    let httpParams = new HttpParams().append("projectId", projectId);
    return this.http.get(`${this.baseUrl}` + `api/getActivePoDetailsByProjectId`, { params: httpParams });
  }

  getResourceRequirementByTeamId(teamId: any) {
    let httpParams = new HttpParams().append("teamId", teamId);
    return this.http.get(`${this.baseUrl}` + `api/getResourceRequirementByTeamId`, { params: httpParams });
  }

  getResourceRequirementCountByProjectId(projectId: any, projectType: any) {
    let httpParams = new HttpParams().append("projectId", projectId).append("projectType", projectType);
    return this.http.get(`${this.baseUrl}` + `api/getResourceRequirementCountByProjectId`, { params: httpParams });
  }

  getResourceRequirementDetailsByProjectId(projectId: any, projectType: any, currentActivePO: boolean) {
    let httpParams = new HttpParams().append("projectId", projectId).append("projectType", projectType).append("currentActivePO", currentActivePO);
    return this.http.get(`${this.baseUrl}` + `api/getResourceRequirementDetailsByProjectId`, { params: httpParams });
  }

  fetchProjectDetailsList(rmgProjectRequest: RMGDashboardProjectRequest) {
    return this.http.post(`${this.baseUrl}` + `api/fetchProjectDetailsList`, rmgProjectRequest);
  } 
  
  getEmployeeCountByEmployeeGroup(rmgProjectRequest: RMGDashboardProjectRequest) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeCountByEmployeeGroup`, rmgProjectRequest);
  }

  getProjectStatusCount(rmgProjectRequest: RMGDashboardProjectRequest) {
    return this.http.post(`${this.baseUrl}` + `api/getProjectStatusCount`, rmgProjectRequest);
  }

  getUnfilledTimesheetProjectDetailsList(rmgProjectRequest: RMGDashboardProjectRequest) {
    return this.http.post(`${this.baseUrl}` + `api/getUnfilledTimesheetProjectDetailsList`, rmgProjectRequest);
  }

  getUnfilledTimesheetProjectDetailsCount(rmgProjectRequest: RMGDashboardProjectRequest) {
    return this.http.post(`${this.baseUrl}` + `api/getUnfilledTimesheetProjectDetailsCount`, rmgProjectRequest);
  }

}

