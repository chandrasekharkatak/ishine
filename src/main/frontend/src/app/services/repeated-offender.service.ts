import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RepeatedOffenderDashboardRequest } from '../models/repeated-offender-dashboard';

@Injectable({
  providedIn: 'root',
})
export class RepeatedOffenderService {
  private readonly baseUrl = environment.baseUrl;

  constructor(private http: HttpClient) {}

  /** Dedicated RO KPI API — not the legacy timesheet dashboard endpoint. */
  getSummary(body: RepeatedOffenderDashboardRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}api/repeated-offender/summary`, body);
  }

  /** Dedicated RO employee grid API — not getEmployeeViewForClientAttendanceStatus. */
  getEmployeeGrid(body: RepeatedOffenderDashboardRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}api/repeated-offender/employees`, body);
  }
}
