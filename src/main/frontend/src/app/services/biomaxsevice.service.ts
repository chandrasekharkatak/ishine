import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { biomaxRequest } from '../models/biomaxRequest';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class BiomaxseviceService {
  private apiUrl: any = environment.baseUrl+"api/biomaxRequest";
  constructor(private http: HttpClient) { }
  
  createBiomaxRequest(biomaxDto: biomaxRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, biomaxDto);
  }

  updateBiomaxRequest(id: number, biomaxDto: biomaxRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/${id}`, biomaxDto);
  }

  getByEmployeeId(empId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/getByEmployeeId/${empId}`);
  }

  getByReportingManagerEmployeeId(empId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/getByReportingManagerEmployeeId/${empId}`);
  }
  deletebiomaxRequest(id:number):Observable<any>{
    return this.http.delete(`${this.apiUrl}/deleteBioMaxRequest/${id}`);
  }
  getById(id:number):Observable<any>{
    return this.http.get(`${this.apiUrl}/getById/${id}`);
  }
  getBioMaxRequestType():Observable<any>{
    return this.http.get(`${this.apiUrl}/getBioMaxRequestType`);
  }
  getBioMaxRequestTypeCronJon():Observable<any>{
    return this.http.get(`${this.apiUrl}/getBioMaxRequestTypeCronJon`);
  }
  
  getBioMaxRequestTpoprojectclone():Observable<any>{
    return this.http.get(`${this.apiUrl}/poprojectclone`);
  }
}

