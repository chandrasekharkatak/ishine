import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UserPerformanceService {

  private baseUrl: string = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllGoalTemplates(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates`).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  getGoalTemplatesByDepartmentId(departmentId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates/department/${departmentId}`);
  }


  createGoalTemplate(template: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/goal-templates`, template);
  }

  updateGoalTemplate(templateId: number, template: any): Observable<any> {
    return this.http.put(`${this.baseUrl}api/goal-templates/${templateId}`, template);
  }

  deleteGoalTemplate(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}api/goal-templates/${id}`);
  }
}