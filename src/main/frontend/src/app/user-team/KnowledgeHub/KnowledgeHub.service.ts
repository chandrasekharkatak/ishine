import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class KnowledgeHubService {

  baseUrl = environment.baseUrl;

  constructor(private http:HttpClient) { }

  search(query: string, limit: number, skip: number) {
    return this.http.get<any>(`${this.baseUrl}api/knowledgehub/search`, {
      params: {
        query,
        limit: limit.toString(),
        skip: skip.toString()
      }
    });
  }

  getProjectDetails(projectId: string) {
    return this.http.get<any>(`${this.baseUrl}api/knowledgehub/project-details/${projectId}`);
  }


}
