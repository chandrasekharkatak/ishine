import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Portal } from 'src/app/models/portal';

@Injectable({
  providedIn: 'root'
})
export class PortalService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  getPortalConfig() {
    return this.http.get(`${this.baseUrl}` + `/api/getPortalConfig`);
  }

  updatePortalConfig(portalObj: Portal) {
    return this.http.post(`${this.baseUrl}` + `/api/updatePortalConfig`,portalObj);
  }

}
