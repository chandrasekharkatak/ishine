import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, throwError } from 'rxjs';
import { Employee } from '../models/employee';
import { User } from '../models/user';
import { Query } from '../models/query';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  
  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  createEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/createEmployee`, employeeObj);
  }

  updateEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeeByEmpId`, employeeObj);
  }

  changeManagerMapping(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/changeManagerMapping`, employeeObj);
  }

  deleteEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/deleteEmployeeByEmpId`, employeeObj);
  }

  unlockAllTimesheet(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/unlockAllTimesheet`, employeeObj);
  }

  getAllEmployees() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployees`);
  }

  getEmployeeByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeByEmpId`, employeeObj);
  }

  updateEmployeeProfile(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeeProfileByEmpId`, employeeObj);
  }

  getAllEmployeesByRole(employeeObj: Employee) {	
    return this.http.post(`${this.baseUrl}` + `api/getAllEmployeesByRole`, employeeObj);	
  }

  getAllEmployeesByDepartmentIds(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getAllEmployeesByDepartmentIds`, employeeObj);
  }

  getAllManagers() {
    return this.http.get(`${this.baseUrl}` + `api/getAllManagers`);
  }

  checkEmployeementId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeementId`, employeeObj);
  }

  checkEmployeeMobileNo(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeeMobileNo`, employeeObj);
  }

  checkEmployeeAadharNumber(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeeAadharNumber`, employeeObj);
  }

  checkEmployeePanNumber(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeePanNumber`, employeeObj);
  }

  getAllEmployeesBirthDayToday() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesBirthDayToday`);
  }

  getHierarchyByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getHierarchyByEmpId`, employeeObj);
  }

  getHierarchyChartByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getHierarchyChartByEmpId`, employeeObj);
  }

  customQueryForEmployeeReport(queryObj: Query) {
    return this.http.post(`${this.baseUrl}` + `api/customQueryForEmployeeReport`, queryObj);
  }

  getEmployeeWorkLocationForSummary() {
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeWorkLocationForSummary`);
  }

  getEmployeeProfileCompletion(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeProfileCompletion`, employeeObj);
  }

  updateTimesheetLockCheck(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/updateTimesheetLockCheck`, employeeObj);
  }
  
  getEmployeeBasicInfo(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeBasicInfo`, employeeObj);
  }

  getEmployeeAuditInfo(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeAuditInfo`, employeeObj);
  }

  /* Profile Image Upload */
  previewImage(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/previewImage`,formData);
  }

  uploadImage(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadImage`,formData);
  }

  /* Employee Draft */
  createDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/createDraftEmployee`, employeeObj);
  }

  updateDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/updateDraftEmployeeById`, employeeObj);
  }

  deleteDraftEmployee(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/deleteDraftEmployeeById`, employeeObj);
  }

  getAllDraftEmployees(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getAllDraftEmployees`,employeeObj);
  }

  getDraftEmployeeByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getDraftEmployeeById`, employeeObj);
  }

  checkEmployeeEmail(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeeEmail`, employeeObj);
  }

  getDraftEmployeeByEmploymentId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getDraftEmployeeByEmploymentId`, employeeObj);
  }

  rejectDraftEmployeeApplication(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/rejectDraftEmployeeApplication`, employeeObj);
  }

  approveDraftEmployeeApplication(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/approveDraftEmployeeApplication`, employeeObj);
  }

  updateDraftStatusById(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/updateDraftStatusById`, employeeObj);
  }

  revokeDraftEmployeeApplication(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/revokeDraftEmployeeApplication`, employeeObj);
  }

   /* update Employee Password */

   updateEmployeePassword(user: User){
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeePassword`, user);
   }

   updateEmployeeForgotPassword(user: User){
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeeForgotPassword`, user);
   }

   /* check Employee old Password */

   checkEmployeeOldPassword(user: User){
    return this.http.post(`${this.baseUrl}` + `api/checkEmployeeOldPassword`, user);
   }	

   revokeAccount(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}` + `api/revokeAccount` , employeeObj);
   }

   findEmployeeWorkingHistory(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/findEmployeeWorkingHistory`,employeeObj);
   }

   /* Demographics Details API */

   addDemographicsInfo(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/addDemographicsInfo`,employeeObj);
   }

  //  added by anurag getTotalNoOfreporties
  getTotalNoOfreporties(employeeObj:any){
    return this.http.post(`${this.baseUrl}`+`api/getTotalNoOfreporties/`+employeeObj,employeeObj);
   }

   getDepartmentByHodId(empId :any){
    return this.http.post(`${this.baseUrl}`+`api/getDepartmentByHodId/`+empId,empId);
   }

   // getProjectsByDepartmentName
   getProjectsByDepartmentName(department : any){
    return this.http.post(`${this.baseUrl}`+`api/getProjectsByDepartmentName/`+department,department);
   }

  //  getTeamByProjectName 
  getTeamByProjectName(project : any){
    return this.http.post(`${this.baseUrl}`+`api/getTeamByProjectName/`+project,project);
   }

  //  getTeamMemberByTeamName
  getTeamMemberByTeamName(teamName : any){
    return this.http.post(`${this.baseUrl}`+`api/getTeamMemberByTeamName/`+teamName,teamName);
   }

  //  getManagerList
  getManagerList(){
    return this.http.get(`${this.baseUrl}`+`api/getManagerList/`);
   }

  //  setManagerToNewManager
  setManagerToNewManager(employee : any){
    return this.http.post(`${this.baseUrl}`+`api/setManagerToNewManager`,employee);
   }

  //  mapLeavesAndCompOffToNewManager
  mapLeavesAndCompOffToNewManager(employee:any){
    return this.http.post(`${this.baseUrl}`+`api/mapLeavesAndCompOffToNewManager`,employee);
  }

  // pipGenerateToUser
pipGenerateToUser(teamObj : Employee){
  return this.http.post(`${this.baseUrl}` + `api/pipGenerateToUser`, teamObj);
}

pipReturnFromUser(teamObj : Employee){
  return this.http.post(`${this.baseUrl}` + `api/pipReturnFromUser`, teamObj);
}

//getOverLapsLeaveForManager
getOverLapsLeaveForManager(leaveApp : Employee){
  return this.http.post(`${this.baseUrl}` + `api/getOverLapsLeaveForManager`, leaveApp);
}

// getPipReasons
getPipReasons(leaveApp : Employee){
  return this.http.post(`${this.baseUrl}` + `api/getPipReasons`, leaveApp);
}

// setExtendPeriodByPipId
setExtendPeriodByPipId(leaveApp : Employee){
  return this.http.post(`${this.baseUrl}` + `api/setExtendPeriodByPipId`, leaveApp);
}

customQueryForDepartmentWiseBillableEmployeeReport(selectedIds: any): Observable<any> {
  // Ensure selectedIds is an array
  if (!Array.isArray(selectedIds)) {
    console.error("SelectedIds is not an array");
    return throwError("SelectedIds must be an array");
  }

  // Ensure selectedIds contains only numbers (Long values)
  if (selectedIds.some(id => typeof id !== 'number')) {
    console.error("SelectedIds contains non-numeric values");
    return throwError("SelectedIds must contain only numbers");
  }

  return this.http.post<any>(`${this.baseUrl}api/customQueryForDepartmentWiseBillableEmployeeReport`, selectedIds);
}


}
