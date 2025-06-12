import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightProjconfigService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }


  getAllDomainData(){
    return this.http.get(`${this.baseUrl}` + `api/createProjectInsightQuestion`);
  }
}
