import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Employee } from '../models/employee';

@Injectable({
  providedIn: 'root'
})
export class ExitService {
  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* Employee EXIT  */

  updateEmployeeResignationDetails(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/updateEmployeeResignationDetails`,employeeObj);
   }

   getEmployeeResignationDetails(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeResignationDetails`,employeeObj);
   }

   getEmployeeExitAssetDetails(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeExitAssetDetails`,employeeObj);
   }

   getEmployeeInfo(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/getEmployeeInfo`,employeeObj);
   }

   setDeptHeadConcent(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/setDeptHeadConcent`,employeeObj);
   }

   sentMailForConsent(employeeObj:Employee){
    return this.http.post(`${this.baseUrl}`+`api/sentMailForConsent`,employeeObj);
   }

   getExitInterviewQuestion(){
    return this.http.get(`${this.baseUrl}`+`api/getExitInterviewQuestion`);
   }
}
