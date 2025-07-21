import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiSourceService {

  private idToRemoveSubject = new BehaviorSubject<number | null>(null);
  idToRemove$ = this.idToRemoveSubject.asObservable();

  setIdToRemove(id: number | null) {
    this.idToRemoveSubject.next(id);
  }

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }

  getAllApiSourceList() {
    return this.http.get(`${this.baseUrl}` + `api/getAllApiList`);
  }

  loadDynamicApi(apiurl: any){
    return this.http.get(`${this.baseUrl}` + apiurl);
  }

  getAllNextFieldAndOption(id: any, type: string) {
    return this.http.get<any[]>(`${this.baseUrl}` + `api/getAllNextFieldAndOption/${type}/${id}`);
  }

  getAllPRojectWithDomain(){
    return this.http.get<any[]>(`${this.baseUrl}` + `api/get-all-domain-with-projects`);
  }


}
