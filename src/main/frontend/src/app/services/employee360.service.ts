import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { Timesheet } from '../models/timesheet';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Employee360Service {

  private baseUrl:any = environment.baseUrl;
  private navigationSubject = new Subject<void>();
  constructor(
    private router: Router,
    private http: HttpClient
  ) {}
  getNavigationEvent() {
    return this.navigationSubject.asObservable();
  }

  navigateToEmployee360(data: any) {
    this.router.navigate(['/employee-360/profile'], { state: { data } }).then(() => {
      this.navigationSubject.next();
    });
  }

  getLeaveDataPerMonthByEmpId(empId: Number) {
    return this.http.get(`${this.baseUrl}` + `api/getLeaveDataPerMonthByEmpId?empId=${empId}`);
  }

  getEmployeeDetails(empId: Number) {
    return this.http.get(`${this.baseUrl}` + `api/getEmployeeDetails?empId=${empId}`);
  }


  get360TimesheetDetails(status: string, empId: number, projectId:number, teamName:string,managerId:number,startDate:string,endDate:string) {
    return this.http.get(`${this.baseUrl}api/get360TimesheetDetails`, {
        params: {
            status: status,
            empId: empId.toString(),
            projectId:projectId.toString(),
            teamName:teamName,
            managerId:managerId.toString(),
            startDate:startDate,
            endDate:endDate
        }
    });
}

updateStatus(status: string, empId: number,date:string) {
  return this.http.get(`${this.baseUrl}api/updateStatus`, {
      params: {
          status: status,
          empId: empId.toString(),
          date:date
      }
  });
}

  // getAllLeaveTypes() {
  //   return this.http.get(`${this.baseUrl}` + `api/getAllLeaveTypes`);
  // }


  get360TimesheetsForHomePageByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/get360TimesheetsForHomePageByEmpId`, timesheetObj);
  }

}
