

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AppreciationService {

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  getAllEmployees() {
    return this.http.get(`${this.baseUrl}` + `api/empdetails`);
  }
  submitAppreciation(data:any){
    return this.http.post(`${this.baseUrl}` + `api/saveAppreciation`,data);

  }
  getAppreciateEmployeeByCurrentUser(employeeObj:any){
    return this.http.post(`${this.baseUrl}` + `api/getAppreciateEmployeeByCurrentUser`,employeeObj);

  }
  CountMyAppreciationBYcurrentUser(employeeObj: any){
    return this.http.post(`${this.baseUrl}` + `api/CountMyAppreciationBYcurrentUser`,employeeObj);
  }

  appreciationSentByCurrentUser(employeeObj: any){
     return this.http.post(`${this.baseUrl}` + `api/appreciationSentByCurrentUser`,employeeObj);
  }

   appreciationReceivedByCurrentUser(employeeObj: any){
     return this.http.post(`${this.baseUrl}` + `api/appreciationReceivedByCurrentUser`,employeeObj);
  }
}
