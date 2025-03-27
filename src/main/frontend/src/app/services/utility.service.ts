import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';
import { first, map, switchMap } from 'rxjs/operators';
import * as moment from 'moment';
import { AppComponent } from '../app.component';
import { SortPipe } from '../sort.pipe';
import { of } from 'rxjs/internal/observable/of';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = environment.baseUrl;
  private employee360ViewUser: boolean = false;

  allEmployeeList360: any[] = [];

  constructor(private http: HttpClient) { }

  appendEmployeementid(isConsultant,emp): string {
    if(isConsultant == "true"){
      return "A-".concat(emp);
    }
    else 
      return "A-".concat(emp);
  }

  getFormattedEmployeeId(empObj: any): string {
    return this.appendEmployeementid(
      empObj.isConsultant,
      empObj.employeementId
    );
  }

  substringEmployeementid(isConsultant,emp): string {
    if(isConsultant == "true"){
      if (emp.startsWith("A-")) {
        return emp.substring(2); 
      } else {
        console.log("Invalid apprenticeship ID format");
        return emp; 
      }
      // return emp.substring(5);
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
      empObj.isApprenticeship
    );
  }

  substringEmploymentId2(isConsultant: string,  emp: string): string {
      if (emp.startsWith("A-")) {
        return emp.substring(2);
      } else if (emp.startsWith("A-")) {
        return emp.substring(2);
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

  getEmployeeDetailsFor360View() {
    return this.getAllEmployeesFor360View().pipe(
      first(),
      switchMap((response: any) => {
        if (response.serviceStatus == "Success") {
          let allEmployeeList360 = response.serviceResponse;
          console.log("allEmployeeListFor360 : ", allEmployeeList360);
  
          // Perform necessary formatting and data transformations
          allEmployeeList360.forEach(employeeObj => {
            employeeObj.employeementId = this.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
            employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  
            if (employeeObj.isConsultant === 'true') {
              employeeObj.employeeType = 'Consultant';
            } else if (employeeObj.isApprenticeship === 'true') {
              employeeObj.employeeType = 'Apprentice';
            } else {
              employeeObj.employeeType = 'Regular';
            }
          });
  
          // Return an observable that waits for the next data load
          const sortedEmployees = new SortPipe().transform(allEmployeeList360, ['name', 'string', 'asc']);
          return of(sortedEmployees);  // Using 'of' to wrap the sorted result in an observable
        } else {
          alert(response.serviceResponse);
          return of([]);  // Return an observable of an empty array if serviceStatus is not "Success"
        }
      })
    ).toPromise();
  }
  
  
  getAllEmployeesFor360View() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesFor360View`);
  }
  
}