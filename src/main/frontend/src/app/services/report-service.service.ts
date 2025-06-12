import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
@Injectable({
  providedIn: 'root'
})
export class ReportService {
  
  private baseUrl:any = environment.baseUrl;

  constructor(private http : HttpClient) { }

  getAllPieChartCount(): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/getAllGraphEmployeeSummary`);
  }

    getDepartmentWiseKycCount(): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/getDepartmentWiseKycCount`);
  }

    getJoinVsResignCount(selectedYear: number): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getJoiningVsResignationCount`, { year: selectedYear });
  }

    getAllEmployeeCountDepartmentWise(): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeeCountDepartmentWise`);
  }

  getDepartmentWiseBillableNonBillableSummary(departmentIds: number[]) {
  return this.http.post<any>(
    `${this.baseUrl}`+`api/getDepartmentWiseBillableNonBillableSummary`,
    { departmentIds }  // sending even empty list if nothing is selected
  );
  }

  getEmployeeDetailsByDepartmentAndBillableType(requestPayload: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/getEmployeeBillableAndNonBillable`, 
      requestPayload
    );
  }

  getEmployeeDetailsByEmploymentType(requestPayload: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/getEmployeeDetailsByEmploymentType`, 
      requestPayload
    );
  }

    getEmployeesByExperience(requestPayload: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/getEmployeesByExperience`, 
      requestPayload
    );
  }

  getDepartmentwiseEmployee(requestPayload: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/getDepartmentwiseEmployeesByType`, 
      requestPayload
    );
  }

    getDepartmentwiseEmployeeKyc(requestPayload: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/getEmployeeDetailsByDepartmentAndKyc`, 
      requestPayload
    );
  }

  getJoinVsResignEmployeeDetails(request: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getJoinVsResignEmployeeDetails`,request);
  }

  getLeaveTrendDetails(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/getLeaveTrendDetails`);
  }

  getWorkLocationDetails(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/getWorkLocationDetails`);
  }

 getWorkLocationSummaryDetails(requestPayload: any): Observable<any> { 
   return this.http.post(`${this.baseUrl}api/getWorkLocationSummaryDetails`, requestPayload);
 }

}
