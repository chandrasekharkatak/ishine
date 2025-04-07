import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AttendanceReconciliationService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }
 
  getBiomatricData(startDate:string,endDate:string){
    let httpParams = new HttpParams()
    .append("startDate",startDate).append("endDate",endDate);
    return this.http.get(`${this.baseUrl}` + `api/getBioData`, {params: httpParams});
  }

  getviewMoreData(id:any,date:string){
    let httpParams = new HttpParams()
    .append("empId",id).
    append("date",date);
    return this.http.get(`${this.baseUrl}` + `api/getBioDataById`, {params: httpParams});
  }
}