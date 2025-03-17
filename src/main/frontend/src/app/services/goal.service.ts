import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';



@Injectable({
  providedIn: 'root'
})
export class GoalService {
  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  updateGoal(goalId: number, payload: any): Observable<any> {
    return this.http.put(`${this.baseUrl}`+`/api/goals/${goalId}`, payload);
  }
  
}
