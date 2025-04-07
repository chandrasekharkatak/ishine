import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Help } from '../models/help';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { Appreciation } from '../models/appreciation';


@Injectable({
  providedIn: 'root'
})
export class HelpService {
  private baseUrl:any = environment.baseUrl;

  constructor(
    private http: HttpClient
  ) { }

  uploadHelpDocument(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadHelpDocument`,formData);
  }

  getAllHelpDocument(){
    return this.http.get(`${this.baseUrl}` + `api/getAllHelpDocument`);
  }

  deleteHelpDocument(helpObj: Help){
    return this.http.post(`${this.baseUrl}`+`api/deleteHelpDocument`,helpObj);
  }

  downloadHelpDocument(helpDocId: string) {
    return this.http.get(`${this.baseUrl}` + `api/downloadHelpDocument/${helpDocId}`, {
      responseType: 'blob'
    });
  }

  getMyAppreciationDetails(appreciationObj: Appreciation): Observable<any> {
    return this.http.post(`${this.baseUrl}`+`api/getMyAppreciationDetails`, appreciationObj);
  }

  getTeamAppreciationDetails(aprreciationObj : Appreciation): Observable<any>{
    return this.http.post(`${this.baseUrl}`+`api/getTeamAppreciationDetails`, aprreciationObj);
  }

}
