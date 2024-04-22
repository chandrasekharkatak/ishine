import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { QueryTable } from '../models/queryTable';

@Injectable({
  providedIn: 'root'
})
export class QueryTableService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http : HttpClient) { }

  createQuery(query : any){
    return this.http.post(`${this.baseUrl}api/query/createQuery`,query);
  }

  getNonPublishedQuery(){
    return this.http.get(`${this.baseUrl}api/query/getNonPublishedQuery`)
  }

  getPublishedQuery(){
    return this.http.get(`${this.baseUrl}api/query/getPublishedQuery`)
  }

  getQueryDataForPreview(query: QueryTable) {
    return this.http.post(`${this.baseUrl}` + `api/query/getQueryDataForPreview`, query);
  }

  getQueryDetailsByQueryId(query : QueryTable){
    return this.http.post(`${this.baseUrl}api/query/getQueryDetailsByQueryId`,query);
  }

  updateQuery(query : QueryTable){
    return this.http.post(`${this.baseUrl}api/query/updateQuery`,query);
  }

  deleteQuery(query : QueryTable){
    return this.http.post(`${this.baseUrl}api/query/deleteQuery`,query);
  }

  publishQuery(query : QueryTable){
    return this.http.post(`${this.baseUrl}api/query/publishQuery`,query);
  }

}
