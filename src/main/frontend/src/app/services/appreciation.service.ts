

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AppreciationService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
  constructor(private http: HttpClient) { }

  getAllEmployees() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/empdetails`);
  }
  submitAppreciation(data:any){
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/saveAppreciation`,data);

  }
}
