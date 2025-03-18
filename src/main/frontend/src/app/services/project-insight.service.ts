import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ProjectInsightQuestion } from '../models/projectInsightQuestion';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }


  createProjectInsightQuestion(projObj: ProjectInsightQuestion){
    return this.http.post(`${this.baseUrl}` + `api/createProjectInsightQuestion`, projObj);
  }
}
