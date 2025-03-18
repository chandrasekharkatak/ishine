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

  // Get all goal templates
  getAllGoalTemplates(): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates`).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  // Get goal templates by department ID
  getGoalTemplatesByDepartmentId(departmentId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates/department/${departmentId}`);
  }

  // Get goal templates by department name
  getGoalTemplatesByDepartmentName(departmentName: string): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates/department/name/${departmentName}`);
  }

  // Get goal template by ID
  getGoalTemplateById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}api/goal-templates/${id}`);
  }

  // Create a new goal template
  createGoalTemplate(template: any): Observable<any> {
    return this.http.post(`${this.baseUrl}api/goal-templates`, template);
  }

  // Update an existing goal template
  updateGoalTemplate(templateId: number, template: any): Observable<any> {
    // Ensure we're sending the full updated template object
    return this.http.put(`${this.baseUrl}api/goal-templates/${templateId}`, template);
  }

  // Delete a goal template
  deleteGoalTemplate(id: number): Observable<any> {
    // Make sure we're using the correct endpoint and proper HTTP method
    return this.http.delete(`${this.baseUrl}api/goal-templates/${id}`);
  }

  
}