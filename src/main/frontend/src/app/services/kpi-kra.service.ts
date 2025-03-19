// src/app/services/kpi-kra.service.ts
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

  getKpisByQuarter(quarterId: number): Observable<any> {
    // return this.http.get(`${this.apiUrl}api/kpi/quarter/${quarterId}`);
    return null;
  }
  createKpiTemplate(kpiTemplate: KpiTemplate): Observable<any> {
    return this.http.post(`${this.apiUrl}api/kpi/createKpi`, kpiTemplate);
  }

  updateKpiTemplate(id: number, kpiTemplate: KpiTemplate): Observable<any> {
    return this.http.put(`${this.apiUrl}api/kpi/updateKpi/${id}`, kpiTemplate);
  }

  deleteKpiTemplate(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}api/kpi/deleteKpi/${id}`);
  }
}