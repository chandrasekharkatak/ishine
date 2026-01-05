import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})

export class ProjectInsightFacetService {
  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllProjectInsightFacetCategory() {
    return this.http.get(`${this.baseUrl}` + `api/getAllProjectInsightFacetCategory`);
  }

  saveProjectInsightFacetCategory(projectInsightFacetCategoryDTOList: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightFacetCategoryList`, projectInsightFacetCategoryDTOList);
  }

}
