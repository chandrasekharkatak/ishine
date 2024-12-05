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
    if (isConsultant === "true") {
      if (emp.startsWith("A-CS-")) {
        return emp.substring(5);
      } else {
        console.error("Invalid consultant ID format");
        return emp; // Return as-is if format is invalid
      }
    } else if (isApprenticeship === "true") {
      if (emp.startsWith("AP-")) {
        return emp.substring(3);
      } else {
        console.error("Invalid apprenticeship ID format");
        return emp; // Return as-is if format is invalid
      }
    } else {
      if (emp.startsWith("A-")) {
        return emp.substring(2); // Regular employees
      } else {
        console.error("Invalid regular employee ID format");
        return emp; // Return as-is if format is invalid
      }
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
