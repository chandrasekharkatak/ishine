import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Query } from '../models/query';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  appendEmployeementid(emp): string {
    return "A-".concat(emp);
  }

  substringEmployeementid(emp): string {
    if (emp.startsWith("A-")) {
      return emp.substring(2);
    } else {
      console.error("invalid data found")
    }
  }


  getCustomQueryData(query: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryData`, query);
  }

}
