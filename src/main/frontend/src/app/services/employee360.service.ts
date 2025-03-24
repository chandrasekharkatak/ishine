import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { Timesheet } from '../models/timesheet';
import { Subject } from 'rxjs';
import { BehaviorSubject } from 'rxjs';
import { Employee } from '../models/employee';
import { Leave } from '../models/leave';
import { Team } from '../models/team';
import { Project } from '../models/project';

@Injectable({
  providedIn: 'root'
})
export class Employee360Service {

  private baseUrl:any = environment.baseUrl;

  private lmsbaseurl:any=environment.lmsbaseurl;
  private navigationSubject = new Subject<void>();
  private employeeDataSource = new BehaviorSubject<any>(null);
  private employeesFor360Source = new BehaviorSubject<any[]>([]); 

  employeesFor360$ = this.employeesFor360Source.asObservable(); 
  currentEmployeeData = this.employeeDataSource.asObservable();


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

  // get360TimesheetDetails(status: string, empId: number, projectId:number, teamName:string,managerId:number,startDate:string,endDate:string) {
  get360TimesheetDetails(status: string, empId: number, projectId:number, teamName:string,startDate:string,endDate:string) {
    return this.http.get(`${this.baseUrl}api/get360TimesheetDetails`, {
        params: {
            status: status,
            empId: empId.toString(),
            projectId:projectId.toString(),
            teamName:teamName,
            // managerId:managerId.toString(),
            startDate:startDate,
            endDate:endDate
        }
    });
}

updateStatus(status: string, timesheetIds:number[],updatedBy: number) {
  return this.http.get(`${this.baseUrl}api/updateStatus`, {
      params: {
          status: status,
          timesheetIds: timesheetIds,
          updatedBy:updatedBy.toString()
      }
  });
}

getAll360LeaveApplicationsByEmpId(leaveObj: Leave) {
  return this.http.post(`${this.baseUrl}` + `api/getAll360LeaveApplicationsByEmpId`, leaveObj);
}
  // getAllLeaveTypes() {
  //   return this.http.get(`${this.baseUrl}` + `api/getAllLeaveTypes`);
  // }



  get360TimesheetsForHomePageByEmpId(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/get360TimesheetsForHomePageByEmpId`, timesheetObj);
  }

  changeEmployeeData(data: any) {
    this.employeeDataSource.next(data);
  }

  getEmployeeDetailsForBiomax(biomaxFilter:any){
    return this.http.post(`${this.baseUrl}` + `api/biomax`,biomaxFilter);
  }

  get360PendingCompOffRequestsByEmpId(compOffObj: Leave) {
    return this.http.post(`${this.baseUrl}` + `api/get360PendingCompOffRequestsByEmpId`, compOffObj);
  }


  getBioOverTimeandState(EmployeDTO: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/employee360state`, EmployeDTO);
  }

  //added by rahul singh
 getTeamTImeSheet(team:Team){
  return this.http.post(`${this.baseUrl}`+`api/getTeamMembersByTeamIdBiomax`,team);

}

  setEmployeesFor360(employees: any[]) {
    this.employeesFor360Source.next(employees);
  }
//added by rahul for project
getTeamMemberByTeamId(teamId:any){
  return this.http.get(`${this.baseUrl}`+`api/getTeamMemberByTeamId`+teamId);
} 

  getProjectInfo(project:Project){
    return this.http.post(`${this.baseUrl}`+`api/getProjectInfo`,project);
  }

  getTeamInfo(project:Project){
    return this.http.post(`${this.baseUrl}`+`api/getTeamInfo`,project);
  }
  getLmsData(email: any) {
    const bearerToken = 'Nguif3kxwSDzmojAtj6M93aJlfJqsAWj9blFug4JWkHsoQ2LYgWiApqDe1GZqmpV';  // Use the actual token without "Bearer"
    const body = { email: email };
  
    return this.http.post(
      `${this.lmsbaseurl}api/get_user_enrolled_details`,
      body,
      {
        headers: {
          'Authorization': `Bearer ${bearerToken}`,  // Add the "Bearer" prefix here
          'Content-Type': 'application/json'
        }
      }
    );
  }
  
}
