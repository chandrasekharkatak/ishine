import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { KpiTemplate } from '../models/kpiTemplate';

@Injectable({
  providedIn: 'root'
})
export class KraKpiService {
  private apiUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllKpis(): Observable<any> {
    return this.http.get(`${this.apiUrl}api/kpi/getAllKpis`);
  }

  getKpiById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}api/kpi/getKpiById/${id}`);
  }
  
  getKraKpiTemplatesByDepartmentId(departmentId: number): Observable<any>{
    
    return this.http.get(`${this.apiUrl}api/kpi/department/${departmentId}`);

  }

  getKraKpiTemplateByDepartmentName(departmentName: string): Observable<any>{
    return this.http.get(`${this.apiUrl}api/kpi/department/name/${departmentName}`);
  }


  getKpisByQuarter(quarterId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}api/kpi/getKpisByQuarter/${quarterId}`);
   
  }
  createKpiTemplate(kpiTemplate: KpiTemplate , quarterId: number , departmentId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}api/kpi/createKpiTemplate/quarter/${quarterId}/department/${departmentId}`, kpiTemplate);
  }
  //  createQuestionnaireTemplate(questionnaireData: QuestionnaireDTO, quarterId: number, departmentId: number): Observable<any> {
  //     return this.http.post(`${this.apiUrl}api/questionnaires/createQuestionnaireTemplate/quarter/${quarterId}/department/${departmentId}`, questionnaireData);
  //   }

  updateKpiTemplate(id: number, kpiTemplate: KpiTemplate): Observable<any> {
    return this.http.put(`${this.apiUrl}api/kpi/updateKpi/${id}`, kpiTemplate);
  }

  deleteKpiTemplate(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}api/kpi/deleteKpi/${id}`);
  }
}