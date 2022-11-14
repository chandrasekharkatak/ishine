import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Portal } from 'src/app/models/portal';
import { enableAppreciation } from '../models/enableAppreciation';

@Injectable({
  providedIn: 'root'
})
export class PortalService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getPortalConfig() {
    return this.http.get(`${this.baseUrl}` + `api/getPortalConfig`);
  }
  updatePortalConfig(portalObj: Portal) {
    return this.http.post(`${this.baseUrl}` + `api/updatePortalConfig`,portalObj);
  }
  generatePerviousMonthDSR() {
    return this.http.get(`${this.baseUrl}` + `api/generatePerviousMonthDSR`);
  }
  getAllEmployees(){
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployees`);

  }

  enableAppreciation(data:any){
    return this.http.post(`${this.baseUrl}` + `api/enableAppreciation`,data);


  }
  getAllEvent(){
    return this.http.get(`${this.baseUrl}` + `api/getAllAppreciationEvent`);

  }

  viewAppreciations(appreciationObj : enableAppreciation){
    return this.http.post(`${this.baseUrl}` + `api/viewAppreciation`,appreciationObj);

  }
}
