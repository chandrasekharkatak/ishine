import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = environment.baseUrl;
  private employee360ViewUser: boolean = false;

  constructor(private http: HttpClient) { }

  appendEmployeementid(isConsultant,isApprenticeship,emp): string {
    if(isConsultant == "true"){
      return "A-CS-".concat(emp);
    }
    else if(isApprenticeship == "true"){
      return  "AP-".concat(emp);
    }
    else 
      return "A-".concat(emp);
  }

  getFormattedEmployeeId(empObj: any): string {
    return this.appendEmployeementid(
      empObj.isConsultant,
      empObj.isApprenticeship,
      empObj.employeementId
    );
  }

  substringEmployeementid(isConsultant,isApprenticeship,isRegular,emp): string {
    if(isConsultant == "true"){
      if (emp.startsWith("A-CS-")) {
        return emp.substring(5); 
      } else {
        console.log("Invalid apprenticeship ID format");
        return emp; 
      }
      // return emp.substring(5);
    }
    else if(isApprenticeship == "true"){
      if (emp.startsWith("AP-")) {
        return emp.substring(3); 
      } else {
        console.log("Invalid apprenticeship ID format");
        return emp; 
      }
    }
    else if(isRegular == 'true'){
      if (emp.startsWith("A-")) {
        return emp.substring(2); 
      } else {
        console.log("Invalid apprenticeship ID format");
        return emp; 
      }
      // return emp.substring(2);
    }
    else {
      console.error("invalid data found");
      //return emp;
    }
  }

  getEmployeeIdSubstring(empObj: any): string {  

    console.log("yess",empObj)

    return this.substringEmployeementid(
      empObj.isConsultant,
      empObj.isApprenticeship,
      empObj.isRegular,
      empObj.employeementId
    );
  }

  substringEmploymentId2(isConsultant: string, isApprenticeship: string, emp: string): string {
      if (emp.startsWith("A-CS-")) {
        return emp.substring(5);
      } else if (emp.startsWith("AP-")) {
        return emp.substring(3);
      } else if (emp.startsWith("A-")) {
        return emp.substring(2); // Regular employees
      } else {
        console.error("Invalid regular employee ID format");
        return emp; // Return as-is if format is invalid
      }
  }

  getEmployeeIdSubstring2(empObj: any): string {  

    console.log("yess",empObj)

    return this.substringEmploymentId2(
      empObj.isConsultant,
      empObj.isApprenticeship,
      empObj.employeementId
    );
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
  

  getCustomQueryData(query: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryData`, query);
  }

  setEmployee360ViewAccess(hasAccess: boolean): void {
    this.employee360ViewUser = hasAccess;
  }

  getEmployee360ViewAccess(): boolean {
    return this.employee360ViewUser;
  }

}
