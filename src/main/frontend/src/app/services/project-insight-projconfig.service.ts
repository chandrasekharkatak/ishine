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

  getDefaultSubGroupStructure(){
    
  }

  filterProjectInsight(data:any, limit:number= 10, page:number=0){
    if (data.createdAt) {
        // If it's a Date object
        if (data.createdAt instanceof Date) {
            data.createdAt = data.createdAt.toISOString().slice(0, 19);
        }
        // If it's already a string like "2025-07-17T00:00:00"
        else if (typeof data.createdAt === 'string') {
            data.createdAt = data.createdAt.split('.')[0]; // Remove milliseconds if present
        }
    }
    console.log(data);
    
    
    return this.http.post(`${this.baseUrl}` + `api/filter-project-insight?page=${page}&limit=${limit}`,{...data});
  }
}
