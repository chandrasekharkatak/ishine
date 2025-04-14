import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';



@Injectable({
  providedIn: 'root'
})
export class GoalService {
  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  updateGoal(goalId: number,empId : number, payload: any): Observable<any> {
    return this.http.put(`${this.baseUrl}`+`api/EmployeeGoals/save/${goalId}/employee/${empId}`, payload);
  }
  

  getGoalsByEmployeeAndQuarter(empId: number, quarter: number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goalQuarter/employee/${empId}/quarter/${quarter}`);
  }
}
