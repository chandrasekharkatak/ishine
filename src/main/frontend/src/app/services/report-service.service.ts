import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { environment } from 'src/environments/environment';
@Injectable({
  providedIn: 'root'
})
export class ReportService {
  
  private baseUrl:any = environment.baseUrl;

  constructor(private http : HttpClient) { }

  getAllPieChartCount(){
    return this.http.get(`${this.baseUrl}` + `api/getAllGraphEmployeeSummary`);
  }

}
