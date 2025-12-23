import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TeamDashboardService {


  private baseUrl: string = environment.baseUrl;
  
  constructor(private http: HttpClient) { }


  // getTeamMembersByTeamId(teamId: any): Observable<any> {
  //     return this.http.post(`${this.baseUrl}api/getTeamMembersByTeamId`, teamId);
  //   }
  
  // getAllTeams():Observable<any>{
  //   return this.http.get(`${this.baseUrl}api/getAllTeams`)

  findEmployeesInSameDepartmentAsCurrentUser(hodId: any): Observable<any> {
    return this.http.get(`${this.baseUrl}api/department-colleagues/${hodId}`);
  }
}

