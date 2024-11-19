import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Query } from '../models/query';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  appendEmployeementid(emp): string {
    return "A-".concat(emp);
  }

  substringEmployeementid(emp): string {
    if (emp.startsWith("A-")) {
      return emp.substring(2);
    }
    else {
      console.error("invalid data found");
      //return emp;
    }
  }

  substringEmployeementid2(employeementId): string {
    if (employeementId.startsWith("A-")) {
      return employeementId.substring(2);
    }
    else {
      return employeementId;
    }
  }

  // substringEmployeementid2(employeementId: string): string {
  //   if (employeementId.startsWith("A-CS-")) {
  //       return employeementId.substring(5); // Removes "A-CS-"
  //   } else if (employeementId.startsWith("A-")) {
  //       return employeementId.substring(2); // Removes "A-"
  //   } else {
  //       return employeementId;
  //   } 
  // }

  getCustomQueryData(query: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryData`, query);
  }

}
