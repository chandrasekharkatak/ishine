import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiSourceService {

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  getAllApiSourceList() {
    return this.http.get(`${this.baseUrl}` + `api/getAllApiList`);
  }


}
