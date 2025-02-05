import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Query } from '../models/query';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = environment.baseUrl;
  private employee360ViewUser: boolean = false;

  constructor(private http: HttpClient) { }

  // appendEmployeementid(isConsultant,emp): string {
  //   if(isConsultant == "true")
  //     return "A-CS-".concat(emp);
  //   else
  //     return "A-".concat(emp);
  // }

  appendEmployeementid(isConsultant,emp): string {
    if(isConsultant == "true")
      return "A-".concat(emp);
    else
      return "A-".concat(emp);
  }

  // substringEmployeementid(isConsultant,emp): string {
  //   if(emp.startsWith("A-CS-")){
  //     return emp.substring(5);
  //   }
  //   else if (emp.startsWith("A-")) {
  //     return emp.substring(2);
  //   }
  //   else {
  //     console.error("invalid data found");
  //     //return emp;
  //   }
  // }

  substringEmployeementid(isConsultant,emp): string {
    if (emp.startsWith("A-")) {
      return emp.substring(2);
    }
    else {
      console.error("invalid data found");
      //return emp;
    }
  }

  // substringEmployeementid2(isConsultant,employeementId): string {
  //   if(employeementId.startsWith("A-CS-")){
  //     return employeementId.substring(5);
  //   }
  //   else if (employeementId.startsWith("A-")) {
  //     return employeementId.substring(2);
  //   }
  //   else {
  //     return employeementId;
  //   }
  // }

  substringEmployeementid2(isConsultant,employeementId): string {
    if (employeementId.startsWith("A-")) {
      return employeementId.substring(2);
    }
    else {
      return employeementId;
    }
  }

  getCustomQueryData(query: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryData`, query);
  }

  setEmployee360ViewAccess(hasAccess: boolean): void {
    this.employee360ViewUser = hasAccess;
  }

  getEmployee360ViewAccess(): boolean {
    return this.employee360ViewUser;
  }

  transformData(response: any): any {
    const groupedData: any = {};
  
    response.serviceResponse.forEach((record: any) => {
      const monthYear = record.leaveDate;
      const leaveType = record.leaveType;
      const status = record.status;
      const count = parseInt(record.total_leave_records, 10);
  
      if (!groupedData[monthYear]) {
        groupedData[monthYear] = {};
      }
      if (!groupedData[monthYear][leaveType]) {
        groupedData[monthYear][leaveType] = { Approved: 0, Pending: 0, Rejected: 0, Revoked: 0 };
      }
  
      groupedData[monthYear][leaveType][status] += count;
    });
  
    return groupedData;
  }

}
