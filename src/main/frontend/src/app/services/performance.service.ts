import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { QuarterCycle } from '../models/quarterCycle';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  // getQuestionnaireReview(empId: any, quarter: any) {
  //   throw new Error('Method not implemented.');
  // }

  private baseUrl: any = environment.baseUrl;
  previousRoute: string;
  getPreviousRoute: any;

  constructor(private http: HttpClient) { }

  getAllEmployeePerformanceForQuarter(quarterId: number,  financialYear: string,empId:number , pages : number , itemsPerPage : number) {

    const params = new HttpParams()
      .set('empId', empId.toString())
      .set('financialYear', financialYear)
      .set('page', pages.toString())
      .set('size', itemsPerPage.toString())
      .set('quarterId',quarterId.toString());

    return this.http.get(`${this.baseUrl}`+`api/getAllEmployeePerformanceForQuarter`, { params }  );
  }

  exportExcelForEligiblePreview(fYear:string ,empId:number){
    return this.http.get(`${this.baseUrl}` + `api/exportExcelForEligiblePreview?fYear=${fYear}&empId=${empId}`);
  }

  getAllEmployee() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployees`);
  }

  getAllEmployeesForPerformance(details:any){
    return this.http.post(`${this.baseUrl}` + `api/getAllEmployeesForPerformance`, details);
  }

  createQuarterCycle(quarterCycle: QuarterCycle) {
    return this.http.post(`${this.baseUrl}` + `api/createQuarterCycle`, quarterCycle);
  }

  updateQuarterCycle(quarterCycle: QuarterCycle) {
    return this.http.post(`${this.baseUrl}` + `api/updateQuarterCycle`, quarterCycle);
  }

  getQuartersByYear(financialYear: any) {
    return this.http.get(`${this.baseUrl}` + `api/getQuartersByYear/` + financialYear);
  }

  getAllQuarterCycles() {
    return this.http.get(`${this.baseUrl}` + `api/getAllQuarterCycles`);
  }

  isEnable(quarterCycle: QuarterCycle) {
    return this.http.post(`${this.baseUrl}` + `api/isEnable`, quarterCycle);
  }

  addReviewType(reviewObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/addReviewType`, reviewObj);
  }

  getReviewType() {
    return this.http.get(`${this.baseUrl}` + `api/getReviewType`);
  }

  deleteReviewType(reviewObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/deleteReviewType`, reviewObj);
  }

  deleteQuarterCycle(quarterCycle: QuarterCycle) {
    return this.http.post(`${this.baseUrl}` + `api/isDelete`, quarterCycle);
  }

  getQuarterCycleById(quarterId: any) {
    return this.http.get(`${this.baseUrl}` + `api/getQuarterCycleById/` + quarterId);

  }

  getReviewTypeById(reviewObj: any) {
    return this.http.post(`${this.baseUrl}` + `api/getReviewTypeById`, reviewObj);
  }

  updateReviewType(reviewObjs: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/updateReviewType`, reviewObjs);

  }

  submitEmployeePerformanceHOD(performance: any) {
    return this.http.post(`${this.baseUrl}` + `api/submitEmployeePerformanceHOD`, performance);
  }

  hrAndHodEmpoyeePerformanceView(performance: any) {
    return this.http.post(`${this.baseUrl}` + `api/hrAndHODEmployeePerformanceView`, performance);
  }

  submitEmployeePerformanceHR(performance: any) {
    return this.http.post(`${this.baseUrl}` + `api/submitEmployeePerformanceHR`, performance);
  }

  updateEmployeePerformanceHOD(performance: any) {
    return this.http.post(`${this.baseUrl}` + `api/updateEmployeePerformanceHOD`, performance);
  }

  // getALLdepartmentByEmployee() {
  //   return this.http.get(`${this.baseUrl}` + `api/getAllDepartmentbyEmployeecont`);
  // }

  getAvailableQuarters() {
    return this.http.get(`${this.baseUrl}` + `api/getAllQuarters`);
  }

  getPerformanceStats(empId: number, quarter: number): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/appraisal-summaries/employee/${empId}/quarter/${quarter}`);
  }

  getKraKpiReview(empId: number, quarter: string): Observable<any> {

    return this.http.get(`${this.baseUrl}api/kra-kpi`, { params: { empId, quarter }, });
  }

  getQuestionnaireReview(empId: number, quarter: string): Observable<any> {

    return this.http.get(`${this.baseUrl}/questionnaire`, { params: { empId, quarter }, });
  }

  getAppraisalSummary(empId: number,quarterId:number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/appraisal-summaries/calculate/${empId}/${quarterId}`);
  }

  submitReview(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/submitReview`, payload);
  }

  getQuestionnares(departmentId: number, quarterId: number) {
    return this.http.get<any>(`${this.baseUrl}api/questionnaires/department/${departmentId}/quarter/${quarterId}`);
  }

  getKraKpi(currentEmp:number,quarterId:number) {
    return this.http.get<any>(`${this.baseUrl}api/kpi/employee/${currentEmp}/quarter/${quarterId}`);
  }

  getReviewLabelForEveryDepartment() {
    return this.http.get(`${this.baseUrl}` + `api/getReviewLabelForEveryDepartment`);
  }

  getTeamEmployeeListInTeamDashboard(empObj: any){
    return this.http.post(`${this.baseUrl}` + `api/getTeamEmployeeListInTeamDashboard`, empObj);
  }

  loadQuestionnaireQuestions(departmentId:any, quarterId:any){
    return this.http.get(`${this.baseUrl}` + `api/questionnaires/department/${departmentId}/quarter/${quarterId}`)
  }

  loadKpiList(quarterId: any, departmentId:any, employeeRole: String){
    return this.http.get(`${this.baseUrl}` +  `api/kpi/getKpisByQuarter/${quarterId}/Department/${departmentId}/EmployeeRole/${employeeRole}`)
  }

  getALLdepartmentByEmployee(details: any) {
    return this.http.post(`${this.baseUrl}` + `api/getAllDepartmentbyEmployeecont`, details);
  }


  /* ------------------------------ Review Apis --------------------------- */

  checkUserHaveTeam(userObj: any){
    return this.http.post(`${this.baseUrl}` + `api/checkUserHaveTeam`, userObj);
  }

  getawards(empId:number):Observable<any> {
    return this.http.get(`${this.baseUrl}api/getEmployeeRewardByOnlyEmpId/empId/${empId}`);
  }

  getQuestionnaireResponses(empId: number, quarterId: number) {
    return this.http.get<any>(`${this.baseUrl}api/qresponses/show/${empId}/${quarterId}`);
  }

  showresponse(empId: number, quarterId: number) {
    return this.http.get<any>(`${this.baseUrl}api/kpi-responses/show/kresponse/${empId}/${quarterId}`);
  }

  submitQuestionnaireResponses(questions: any[], empId: number, quarterId: number) {
    // Format the questions as DTOs for backend processing
    const dtos = questions.map(q => ({
      id: q.id || q.existingId,
      questionText: q.questionText,
      response: q.response || 0,
      managerRating: q.managerRating || 0,
      managerRemark: q.managerRemark || '',
      // Include any other fields needed
    }));
    
    console.log('Submitting to API:', JSON.stringify(dtos));
    
    return this.http.post<any>(
      `${this.baseUrl}api/qresponses/save/${empId}/quarter/${quarterId}`, 
      dtos
    ).pipe(
      tap(response => console.log('API response:', response)),
      catchError(err => {
        console.error('API error:', err);
        return throwError(() => err);
      })
    );
  }
 
  submitKpiResponses(responses: any[], empId: number, quarterId: number, empID:number) {
    
    return this.http.post<any>(`${this.baseUrl}api/kpi-responses/save/${empId}/quarter/${quarterId}/reviewer/${empID}`, responses);
  }

  addNewKRA(kra: any, empId: number, quarterId: number) {
    const payload = {
      
      description: kra.description,
      progress: kra.progress
    };
    
    return this.http.post<any>(`${this.baseUrl}api/kpi/employee/${empId}/quarter/${quarterId}/addkpiList`, payload);
  }

  getRewardsAndAppreciationCount(employeeDetails: any) {
    return this.http.post(`${this.baseUrl}` + `api/getRewardsAndAppreciationCount`, employeeDetails);
  }

  setPreviousRoute(route: string): void {
    this.previousRoute = route;
  }

  getAllEmployeesForPerformanceExcell(details:any){
    return this.http.post(`${this.baseUrl}` + `api/exportExcelForHodAndManger`, details);
  }

  getAllEmployeesCurrentStatus(){
    return this.http.get(`${this.baseUrl}` + `api/currentStatusForPerformanceTableView`);
  }

  addRemarkAsPerQuestion(marksObject: any){
    return this.http.post(`${this.baseUrl}` + `api/addRemarkAsPerQuestion`, marksObject);
  }
}


