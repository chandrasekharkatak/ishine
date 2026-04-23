import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AttendanceReconciliationService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }
 
  /**
   * Same data as search API; backend only exposes POST /api/getBioData (GET is removed).
   */
  getBiomatricData(startDate: string, endDate: string, pageNumber: number = 1, pageSize: number = 20) {
    return this.getBiomatricDataWithSearch(startDate, endDate, pageNumber, pageSize, {});
  }

  getviewMoreData(id:any,date:string){
    let httpParams = new HttpParams()
    .append("empId",id).
    append("date",date);
    return this.http.get(`${this.baseUrl}` + `api/getBioDataById`, {params: httpParams});
  }

  getBiomatricDataWithSearch(startDate: string, endDate: string, pageNumber: number, pageSize: number, searchParams: any) {
    const url = `${this.baseUrl}api/getBioData`;
    return this.http.post(url, {
        startDate: startDate,
        endDate: endDate,
        pageNumber: pageNumber,
        pageSize: pageSize,
        searchParams: searchParams
    });
}
}