import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';


export interface CustomFilter {
  column: string;
  operator: string;
  value: string;
  conjunction: string;
}


export interface ReportsQueryPayload {
  queryList: CustomFilter[];
  // You can add other properties from the backend DTO if needed
  // fetchDate?: string;
  // typeOfLeave?: string;
}
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


   customgetLeaveTrendDetails(queryObj: Query): Observable<any> {
    const url = `${this.baseUrl}api/customgetLeaveTrendDetails`;
    return this.http.post(url, queryObj);
  }

  customgetGraphEmployeeSummary(queryObj: Query): Observable<any> {
  const url = `${this.baseUrl}api/graph-employee-summary`;
  return this.http.post(url, queryObj);
}

customgetJoinVsResignCount(payload: Query, year: number): Observable<any> {
    const requestBody = {
      queryList: payload.queryList || [],
      year: year
    };
    return this.http.post<any>(`${this.baseUrl}api/customgetJoiningVsResignationCount`, requestBody);
  }
  
  customgetWorkLocationDetails(payload: Query): Observable<any> {
    const url = `${this.baseUrl}api/work-location-details`; // Use the new POST endpoint
    return this.http.post(url, payload);
}
}