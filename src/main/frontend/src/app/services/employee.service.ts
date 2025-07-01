import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Employee } from '../models/employee';
import { Query } from '../models/query';
import { User } from '../models/user';

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

  
  getAllEmployeesFor360View() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesFor360View`);
  }

  // getEmployeeByAppreciationName(requestBody: any): Observable<any> {
  //   return this.http.post(`${this.baseUrl}api/getEmployeeByAppreciationName`, requestBody,{
  //     headers: { 'Content-Type': 'application/json' },
  // });
  // }

  getEmployeeByEmpId(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getEmployeeByEmpId`, employeeObj);
  }
  getEmployeeAppreciationByEmpId(requestPayload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getEmployeeAppreciationByEmpId`, requestPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
  } 
  getDateRangesForDropdown(currentEmp:any){
    return this.http.post(`${this.baseUrl}` + `api/getDateRangesForDropdown`,currentEmp,{
      headers: { 'Content-Type': 'application/json' },
    });
  }
  getTeamAppreciationByEmpId(requestPayload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/getTeamAppreciationByEmpId`, requestPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
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

  getAllEmployeesWorkAnniversaryToday() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesWorkAnniversaryToday`);
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
   getProjectsByDepartmentName(department : Employee){
    return this.http.post(`${this.baseUrl}`+`api/getProjectsByDepartmentName`,department);
   }

  //  getTeamByProjectName 
  getTeamByProjectName(project : any){
    return this.http.post(`${this.baseUrl}`+`api/getTeamByProjectName/`+project,project);
   }

  //  getTeamMemberByTeamName
  // getTeamMemberByTeamName(teamName : any){
  //   return this.http.post(`${this.baseUrl}`+`api/getTeamMemberByTeamName/`+teamName,teamName);
  //  }

  getTeamMemberByTeamName(teamName : Employee){
    return this.http.post(`${this.baseUrl}`+`api/getTeamMemberByTeamName`,teamName);
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

// employee.service.ts
getManagerByEmpId(empId: string) {
  return this.http.get(`/api/employee/${empId}/manager`);
}

getReporteesListByManagerId(empObj: Employee){
  // console.log("getReporteesListByManagerId ",empObj)
  return this.http.post(`${this.baseUrl}`+`api/getReporteesListByManagerId`,empObj);
}

isEmployeeOnBench(onbench: Employee) {
  // const params = new HttpParams().set('empId', empId.toString());
  return this.http.post(`${this.baseUrl}api/isEmployeeOnBench`, onbench);
}

IsValidateLMSPORTAL(obj:any){

  return this.http.post(`${this.baseUrl}api/IsValidateLMSPORTAL`, obj);
}

getReporteesListByReportingManagerId(empObj: Employee){
  // console.log("getReporteesListByManagerId ",empObj)
  return this.http.post(`${this.baseUrl}`+`api/getReporteesListByReportingManagerId`,empObj);
}

setReportingManagerToNewManager(employee : any){
  return this.http.post(`${this.baseUrl}`+`api/setReportingManagerToNewManager`,employee);
}

getAllEmployeesReportByProjectTypeInConsolidated():Observable<any[]>{
  return this.http.get<any[]>(`${this.baseUrl}`+`api/getAllEmployeesReportByProjectTypeInConsolidated`);
}

getPoProjectDetailsBOthPOAndInternal(){
  return this.http.get(`${this.baseUrl}`+`api/getPoProjectDetailsBOthPOAndInternal`);
}

getAllEmployeesReportByProjectType(){
  return this.http.get(`${this.baseUrl}`+`api/getAllEmployeesReportByProjectType`);
}

//Update Default ProjectName for employee
updateDefaultProject(newemployeeObj : any){
  return this.http.post(`${this.baseUrl}` + `api/updateDefaultProject`, newemployeeObj);
 }

 getExpiredPo(){
  return this.http.get(`${this.baseUrl}` + `api/getExpiredPo`);
 }

 sendExpiredPoEmail(employeeDTO:any){
  return this.http.post(`${this.baseUrl}` + `api/sendExpiredPoEmail`,employeeDTO);
 }

 getEmployeeProjectReport(employeeReport:any){
  return this.http.post(`${this.baseUrl}` + `api/getEmployeeProjectReport`,employeeReport);

 }

 getPoProjectSync(){
  return this.http.get(`${this.baseUrl}` + `api/poprojectclone`);
 }

 updateEmployeeReportBillableType(employee:any){
  return this.http.post(`${this.baseUrl}` + `api/updateEmployeeReportBillableType`,employee);
 }

 updateBulkBillableEmployeeReport(payload:any){
   return this.http.post(`${this.baseUrl}` + `api/updateBulkBillableEmployeeReport`,payload);
 }

 getEmployeeByNameAndEmpld(){
  return this.http.get(`${this.baseUrl}` + `api/getEmployeeByNameAndEmpld`);
 }

 getProjectsAccToDepartmentSelected(payload:any){
  return this.http.post(`${this.baseUrl}` + `api/getProjectsAccToDepartmentAndProjectType`,payload);
 }

 getAllEmployeesBasedOnUserLogined(details:any){
  return this.http.post(`${this.baseUrl}` + `api/getAllEmployeesBasedOnUserLogined`,details);
 }

 getTotalActiveEmployeeCount(){
  return this.http.get(`${this.baseUrl}` + `api/totalEmployeeCount`);
 }

 getTotalActiveEmployeeCountInDepartments(employeeReport:any){
  return this.http.post(`${this.baseUrl}` + `api/totalEmployeeCountInDepartments`,employeeReport);
 }

 projectLessEmployeesDepartmentWise(employeeReport:any){
  return this.http.post(`${this.baseUrl}` + `api/projectLessEmployeesDepartmentWise`,employeeReport);
 }

 employeesMappedProjectsDepartmentWise(employeeReport:any){
  return this.http.post(`${this.baseUrl}` + `api/employeesMappedProjectsDepartmentWise`,employeeReport);
 }
 
 getAllPieGraphListSummary(params: any){
  return this.http.post(`${this.baseUrl}`+`api/getAllPieGraphListSummary`, params)
}



//Employee Confirmation
extendemployee(payload: any) {
  return this.http.post(`${this.baseUrl}` + `api/extendEmployeeProbation`, payload);
}

confirmEmployee(payload: any) {
  return this.http.post(`${this.baseUrl}` + `api/confirmEmployeeFromProbation`, payload);
}
}