import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class KnowledgeHubService {

  baseUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }

  // highlight(text: any, query: string, searchPerformed: boolean): string {
  //   if (!searchPerformed) return text;
  //   if (!query || text == null) {
  //     return typeof text === 'string' ? text : JSON.stringify(text);
  //   }

  //   const textStr = typeof text === 'string' ? text : JSON.stringify(text, null, 2);
  //   const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  //   const regex = new RegExp(escapedQuery, 'gi');

  //   return textStr.replace(regex, match => `<span class="highlight">${match}</span>`);
  // }

  highlight(text: any, query: string, searchPerformed: boolean): string {
    if (!searchPerformed) return text ?? '';

    if (!query || text == null) {
      return typeof text === 'string' ? text : JSON.stringify(text);
    }

    const textStr = typeof text === 'string' ? text : String(text);
    const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(escapedQuery, 'gi');

    return textStr.replace(regex, match => `<span class="highlight">${match}</span>`);
  }


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

  onSearchTerm(knowledgeObj: any) {
    return this.http.post<any>(`${this.baseUrl}` + `api/knowledgehub/onSearchTerm`, knowledgeObj);
  }

  loadProjectSearchObject(knowledgeObj: any) {
    return this.http.post<any>(`${this.baseUrl}` + `api/knowledgehub/loadProjectSearchObject`, knowledgeObj);
  }

  syncAllDataWithFlatSearch() {
    return this.http.get<any>(`${this.baseUrl}` + `api/knowledgehub/syncAllDataWithFlatSearch`);
  }

  getAllFacetsForKeyword(knowledgeObj: any) {
    return this.http.post<any>(`${this.baseUrl}` + `api/knowledgehub/getAllFacetsForKeyword`, knowledgeObj);
  }

}
